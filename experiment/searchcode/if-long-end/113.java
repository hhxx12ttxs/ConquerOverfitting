/*
 * The MIT License
 * 
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi,
 * Eric Lefevre-Ardant, Erik Ramfelt, Michael B. Donohue, Alan Harder,
 * Manufacture Francaise des Pneumatiques Michelin, Romain Seguy
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson;

import hudson.Launcher.LocalLauncher;
import hudson.Launcher.RemoteLauncher;
import jenkins.model.Jenkins;
import hudson.model.TaskListener;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.remoting.DelegatingCallable;
import hudson.remoting.Future;
import hudson.remoting.Pipe;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import hudson.remoting.RemoteInputStream;
import hudson.remoting.Which;
import hudson.security.AccessControlled;
import hudson.util.DirScanner;
import hudson.util.IOException2;
import hudson.util.HeadBufferingStream;
import hudson.util.FormValidation;
import hudson.util.IOUtils;

import static hudson.Util.*;
import static hudson.util.jna.GNUCLibrary.LIBC;
import static hudson.FilePath.TarCompression.GZIP;
import hudson.org.apache.tools.tar.TarInputStream;
import hudson.util.io.Archiver;
import hudson.util.io.ArchiverFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.tar.TarEntry;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.fileupload.FileItem;
import org.kohsuke.stapler.Stapler;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

import com.sun.jna.Native;
import hudson.os.PosixException;
import java.util.Enumeration;
import java.util.logging.Logger;
import org.apache.tools.ant.taskdefs.Chmod;

import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipEntry;
        
/**
 * {@link File} like object with remoting support.
 *
 * <p>
 * Unlike {@link File}, which always implies a file path on the current computer,
 * {@link FilePath} represents a file path on a specific slave or the master.
 *
 * Despite that, {@link FilePath} can be used much like {@link File}. It exposes
 * a bunch of operations (and we should add more operations as long as they are
 * generally useful), and when invoked against a file on a remote node, {@link FilePath}
 * executes the necessary code remotely, thereby providing semi-transparent file
 * operations.
 *
 * <h2>Using {@link FilePath} smartly</h2>
 * <p>
 * The transparency makes it easy to write plugins without worrying too much about
 * remoting, by making it works like NFS, where remoting happens at the file-system
 * layer.
 *
 * <p>
 * But one should note that such use of remoting may not be optional. Sometimes,
 * it makes more sense to move some computation closer to the data, as opposed to
 * move the data to the computation. For example, if you are just computing a MD5
 * digest of a file, then it would make sense to do the digest on the host where
 * the file is located, as opposed to send the whole data to the master and do MD5
 * digesting there.
 *
 * <p>
 * {@link FilePath} supports this "code migration" by in the
 * {@link #act(FileCallable)} method. One can pass in a custom implementation
 * of {@link FileCallable}, to be executed on the node where the data is located.
 * The following code shows the example:
 *
 * <pre>
 * FilePath file = ...;
 *
 * // make 'file' a fresh empty directory.
 * file.act(new FileCallable&lt;Void>() {
 *   // if 'file' is on a different node, this FileCallable will
 *   // be transfered to that node and executed there.
 *   public Void invoke(File f,VirtualChannel channel) {
 *     // f and file represents the same thing
 *     f.deleteContents();
 *     f.mkdirs();
 *   }
 * });
 * </pre>
 *
 * <p>
 * When {@link FileCallable} is transfered to a remote node, it will be done so
 * by using the same Java serialization scheme that the remoting module uses.
 * See {@link Channel} for more about this. 
 *
 * <p>
 * {@link FilePath} itself can be sent over to a remote node as a part of {@link Callable}
 * serialization. For example, sending a {@link FilePath} of a remote node to that
 * node causes {@link FilePath} to become "local". Similarly, sending a
 * {@link FilePath} that represents the local computer causes it to become "remote."
 *
 * @author Kohsuke Kawaguchi
 */
public final class FilePath implements Serializable {
    /**
     * When this {@link FilePath} represents the remote path,
     * this field is always non-null on master (the field represents
     * the channel to the remote slave.) When transferred to a slave via remoting,
     * this field reverts back to null, since it's transient.
     *
     * When this {@link FilePath} represents a path on the master,
     * this field is null on master. When transferred to a slave via remoting,
     * this field becomes non-null, representing the {@link Channel}
     * back to the master.
     *
     * This is used to determine whether we are running on the master or the slave.
     */
    private transient VirtualChannel channel;

    // since the platform of the slave might be different, can't use java.io.File
    private final String remote;

    /**
     * Creates a {@link FilePath} that represents a path on the given node.
     *
     * @param channel
     *      To create a path that represents a remote path, pass in a {@link Channel}
     *      that's connected to that machine. If null, that means the local file path.
     */
    public FilePath(VirtualChannel channel, String remote) {
        this.channel = channel;
        this.remote = normalize(remote);
    }

    /**
     * To create {@link FilePath} that represents a "local" path.
     *
     * <p>
     * A "local" path means a file path on the computer where the
     * constructor invocation happened.
     */
    public FilePath(File localPath) {
        this.channel = null;
        this.remote = normalize(localPath.getPath());
    }

    /**
     * Construct a path starting with a base location.
     * @param base starting point for resolution, and defines channel
     * @param rel a path which if relative will be resolved against base
     */
    public FilePath(FilePath base, String rel) {
        this.channel = base.channel;
        this.remote = normalize(resolvePathIfRelative(base, rel));
    }

    private String resolvePathIfRelative(FilePath base, String rel) {
        if(isAbsolute(rel)) return rel;
        if(base.isUnix()) {
            // shouldn't need this replace, but better safe than sorry
            return base.remote+'/'+rel.replace('\\','/');
        } else {
            // need this replace, see Slave.getWorkspaceFor and AbstractItem.getFullName, nested jobs on Windows
            // slaves will always have a rel containing at least one '/' character. JENKINS-13649
            return base.remote+'\\'+rel.replace('/','\\');
        }
    }

    /**
     * Is the given path name an absolute path?
     */
    private static boolean isAbsolute(String rel) {
        return rel.startsWith("/") || DRIVE_PATTERN.matcher(rel).matches();
    }

    private static final Pattern DRIVE_PATTERN = Pattern.compile("[A-Za-z]:[\\\\/].*"),
            ABSOLUTE_PREFIX_PATTERN = Pattern.compile("^(\\\\\\\\|(?:[A-Za-z]:)?[\\\\/])[\\\\/]*");

    /**
     * {@link File#getParent()} etc cannot handle ".." and "." in the path component very well,
     * so remove them.
     */
    private static String normalize(String path) {
        StringBuilder buf = new StringBuilder();
        // Check for prefix designating absolute path
        Matcher m = ABSOLUTE_PREFIX_PATTERN.matcher(path);
        if (m.find()) {
            buf.append(m.group(1));
            path = path.substring(m.end());
        }
        boolean isAbsolute = buf.length() > 0;
        // Split remaining path into tokens, trimming any duplicate or trailing separators
        List<String> tokens = new ArrayList<String>();
        int s = 0, end = path.length();
        for (int i = 0; i < end; i++) {
            char c = path.charAt(i);
            if (c == '/' || c == '\\') {
                tokens.add(path.substring(s, i));
                s = i;
                // Skip any extra separator chars
                while (++i < end && ((c = path.charAt(i)) == '/' || c == '\\')) { }
                // Add token for separator unless we reached the end
                if (i < end) tokens.add(path.substring(s, s+1));
                s = i;
            }
        }
        if (s < end) tokens.add(path.substring(s));
        // Look through tokens for "." or ".."
        for (int i = 0; i < tokens.size();) {
            String token = tokens.get(i);
            if (token.equals(".")) {
                tokens.remove(i);
                if (tokens.size() > 0)
                    tokens.remove(i > 0 ? i - 1 : i);
            } else if (token.equals("..")) {
                if (i == 0) {
                    // If absolute path, just remove: /../something
                    // If relative path, not collapsible so leave as-is
                    tokens.remove(0);
                    if (tokens.size() > 0) token += tokens.remove(0);
                    if (!isAbsolute) buf.append(token);
                } else {
                    // Normalize: remove something/.. plus separator before/after
                    i -= 2;
                    for (int j = 0; j < 3; j++) tokens.remove(i);
                    if (i > 0) tokens.remove(i-1);
                    else if (tokens.size() > 0) tokens.remove(0);
                }
            } else
                i += 2;
        }
        // Recombine tokens
        for (String token : tokens) buf.append(token);
        if (buf.length() == 0) buf.append('.');
        return buf.toString();
    }

    /**
     * Checks if the remote path is Unix.
     */
    boolean isUnix() {
        // if the path represents a local path, there' no need to guess.
        if(!isRemote())
            return File.pathSeparatorChar!=';';
            
        // note that we can't use the usual File.pathSeparator and etc., as the OS of
        // the machine where this code runs and the OS that this FilePath refers to may be different.

        // Windows absolute path is 'X:\...', so this is usually a good indication of Windows path
        if(remote.length()>3 && remote.charAt(1)==':' && remote.charAt(2)=='\\')
            return false;
        // Windows can handle '/' as a path separator but Unix can't,
        // so err on Unix side
        return remote.indexOf("\\")==-1;
    }

    /**
     * Gets the full path of the file on the remote machine.
     *
     */
    public String getRemote() {
        return remote;
    }

    /**
     * Creates a zip file from this directory or a file and sends that to the given output stream.
     *
     * @deprecated as of 1.315. Use {@link #zip(OutputStream)} that has more consistent name.
     */
    public void createZipArchive(OutputStream os) throws IOException, InterruptedException {
        zip(os);
    }

    /**
     * Creates a zip file from this directory or a file and sends that to the given output stream.
     */
    public void zip(OutputStream os) throws IOException, InterruptedException {
        zip(os,(FileFilter)null);
    }

    public void zip(FilePath dst) throws IOException, InterruptedException {
        OutputStream os = dst.write();
        try {
            zip(os);
        } finally {
            os.close();
        }
    }
    
    /**
     * Creates a zip file from this directory by using the specified filter,
     * and sends the result to the given output stream.
     *
     * @param filter
     *      Must be serializable since it may be executed remotely. Can be null to add all files.
     *
     * @since 1.315
     */
    public void zip(OutputStream os, FileFilter filter) throws IOException, InterruptedException {
        archive(ArchiverFactory.ZIP,os,filter);
    }

    /**
     * Creates a zip file from this directory by only including the files that match the given glob.
     *
     * @param glob
     *      Ant style glob, like "**&#x2F;*.xml". If empty or null, this method
     *      works like {@link #createZipArchive(OutputStream)}
     *
     * @since 1.129
     * @deprecated as of 1.315
     *      Use {@link #zip(OutputStream,String)} that has more consistent name.
     */
    public void createZipArchive(OutputStream os, final String glob) throws IOException, InterruptedException {
        archive(ArchiverFactory.ZIP,os,glob);
    }

    /**
     * Creates a zip file from this directory by only including the files that match the given glob.
     *
     * @param glob
     *      Ant style glob, like "**&#x2F;*.xml". If empty or null, this method
     *      works like {@link #createZipArchive(OutputStream)}
     *
     * @since 1.315
     */
    public void zip(OutputStream os, final String glob) throws IOException, InterruptedException {
        archive(ArchiverFactory.ZIP,os,glob);
    }

    /**
     * Uses the given scanner on 'this' directory to list up files and then archive it to a zip stream.
     */
    public int zip(OutputStream out, DirScanner scanner) throws IOException, InterruptedException {
        return archive(ArchiverFactory.ZIP, out, scanner);
    }

    /**
     * Archives this directory into the specified archive format, to the given {@link OutputStream}, by using
     * {@link DirScanner} to choose what files to include.
     *
     * @return
     *      number of files/directories archived. This is only really useful to check for a situation where nothing
     *      is archived.
     */
    public int archive(final ArchiverFactory factory, OutputStream os, final DirScanner scanner) throws IOException, InterruptedException {
        final OutputStream out = (channel!=null)?new RemoteOutputStream(os):os;
        return act(new FileCallable<Integer>() {
            public Integer invoke(File f, VirtualChannel channel) throws IOException {
                Archiver a = factory.create(out);
                try {
                    scanner.scan(f,a);
                } finally {
                    a.close();
                }
                return a.countEntries();
            }

            private static final long serialVersionUID = 1L;
        });
    }

    public int archive(final ArchiverFactory factory, OutputStream os, final FileFilter filter) throws IOException, InterruptedException {
        return archive(factory,os,new DirScanner.Filter(filter));
    }

    public int archive(final ArchiverFactory factory, OutputStream os, final String glob) throws IOException, InterruptedException {
        return archive(factory,os,new DirScanner.Glob(glob,null));
    }

    /**
     * When this {@link FilePath} represents a zip file, extracts that zip file.
     *
     * @param target
     *      Target directory to expand files to. All the necessary directories will be created.
     * @since 1.248
     * @see #unzipFrom(InputStream)
     */
    public void unzip(final FilePath target) throws IOException, InterruptedException {
        target.act(new FileCallable<Void>() {

            public Void invoke(File dir, VirtualChannel channel) throws IOException {
                if (FilePath.this.isRemote())
                    unzip(dir, FilePath.this.read()); // use streams
                else
                    unzip(dir, new File(FilePath.this.getRemote())); // shortcut to local file
                return null;
            }
            private static final long serialVersionUID = 1L;
        });
    }

    /**
     * When this {@link FilePath} represents a tar file, extracts that tar file.
     *
     * @param target
     *      Target directory to expand files to. All the necessary directories will be created.
     * @param compression
     *      Compression mode of this tar file.
     * @since 1.292
     * @see #untarFrom(InputStream, TarCompression)
     */
    public void untar(final FilePath target, final TarCompression compression) throws IOException, InterruptedException {
        target.act(new FileCallable<Void>() {
            public Void invoke(File dir, VirtualChannel channel) throws IOException {
                readFromTar(FilePath.this.getName(),dir,compression.extract(FilePath.this.read()));
                return null;
            }
            private static final long serialVersionUID = 1L;
        });
    }

    /**
     * Reads the given InputStream as a zip file and extracts it into this directory.
     *
     * @param _in
     *      The stream will be closed by this method after it's fully read.
     * @since 1.283
     * @see #unzip(FilePath)
     */
    public void unzipFrom(InputStream _in) throws IOException, InterruptedException {
        final InputStream in = new RemoteInputStream(_in);
        act(new FileCallable<Void>() {
            public Void invoke(File dir, VirtualChannel channel) throws IOException {
                unzip(dir, in);
                return null;
            }
            private static final long serialVersionUID = 1L;
        });
    }

    private void unzip(File dir, InputStream in) throws IOException {
        File tmpFile = File.createTempFile("tmpzip", null); // uses java.io.tmpdir
        try {
            IOUtils.copy(in, tmpFile);
            unzip(dir,tmpFile);
        }
        finally {
            tmpFile.delete();
        }
    }

    private void unzip(File dir, File zipFile) throws IOException {
        dir = dir.getAbsoluteFile();    // without absolutization, getParentFile below seems to fail
        ZipFile zip = new ZipFile(zipFile);
        @SuppressWarnings("unchecked")
        Enumeration<ZipEntry> entries = zip.getEntries();

        try {
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                File f = new File(dir, e.getName());
                if (e.isDirectory()) {
                    f.mkdirs();
                } else {
                    File p = f.getParentFile();
                    if (p != null) {
                        p.mkdirs();
                    }
                    IOUtils.copy(zip.getInputStream(e), f);
                    try {
                        FilePath target = new FilePath(f);
                        int mode = e.getUnixMode();
                        if (mode!=0)    // Ant returns 0 if the archive doesn't record the access mode
                            target.chmod(mode);
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.WARNING, "unable to set permissions", ex);
                    }
                    f.setLastModified(e.getTime());
                }
            }
        } finally {
            zip.close();
        }
    }

    /**
     * Absolutizes this {@link FilePath} and returns the new one.
     */
    public FilePath absolutize() throws IOException, InterruptedException {
        return new FilePath(channel,act(new FileCallable<String>() {
            public String invoke(File f, VirtualChannel channel) throws IOException {
                return f.getAbsolutePath();
            }
        }));
    }

    /**
     * Creates a symlink to the specified target.
     *
     * @param target
     *      The file that the symlink should point to.
     * @param listener
     *      If symlink creation requires a help of an external process, the error will be reported here.
     * @since 1.456
     */
    public void symlinkTo(final String target, final TaskListener listener) throws IOException, InterruptedException {
        act(new FileCallable<Void>() {
            public Void invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                Util.createSymlink(f.getParentFile(),target,f.getName(),listener);
                return null;
            }
        });
    }
    
    /**
     * Resolves symlink, if the given file is a symlink. Otherwise return null.
     * <p>
     * If the resolution fails, report an error.
     *
     * @since 1.456
     */
    public String readLink() throws IOException, InterruptedException {
        return act(new FileCallable<String>() {
            public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                return Util.resolveSymlink(f);
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilePath that = (FilePath) o;

        if (channel != null ? !channel.equals(that.channel) : that.channel != null) return false;
        return remote.equals(that.remote);

    }

    @Override
    public int hashCode() {
        return 31 * (channel != null ? channel.hashCode() : 0) + remote.hashCode();
    }
    
    /**
     * Supported tar file compression methods.
     */
    public enum TarCompression {
        NONE {
            public InputStream extract(InputStream in) {
                return in;
            }
            public OutputStream compress(OutputStream out) {
                return out;
            }
        },
        GZIP {
            public InputStream extract(InputStream _in) throws IOException {
                HeadBufferingStream in = new HeadBufferingStream(_in,SIDE_BUFFER_SIZE);
                try {
                    return new GZIPInputStream(in,8192);
                } catch (IOException e) {
                    // various people reported "java.io.IOException: Not in GZIP format" here, so diagnose this problem better
                    in.fillSide();
                    throw new IOException2(e.getMessage()+"\nstream="+Util.toHexString(in.getSideBuffer()),e);
                }
            }
            public OutputStream compress(OutputStream out) throws IOException {
                return new GZIPOutputStream(new BufferedOutputStream(out));
            }
        };

        public abstract InputStream extract(InputStream in) throws IOException;
        public abstract OutputStream compress(OutputStream in) throws IOException;
    }

    /**
     * Reads the given InputStream as a tar file and extracts it into this directory.
     *
     * @param _in
     *      The stream will be closed by this method after it's fully read.
     * @param compression
     *      The compression method in use.
     * @since 1.292
     */
    public void untarFrom(InputStream _in, final TarCompression compression) throws IOException, InterruptedException {
        try {
            final InputStream in = new RemoteInputStream(_in);
            act(new FileCallable<Void>() {
                public Void invoke(File dir, VirtualChannel channel) throws IOException {
                    readFromTar("input stream",dir, compression.extract(in));
                    return null;
                }
                private static final long serialVersionUID = 1L;
            });
        } finally {
            IOUtils.closeQuietly(_in);
        }
    }

    /**
     * Given a tgz/zip file, extracts it to the given target directory, if necessary.
     *
     * <p>
     * This method is a convenience method designed for installing a binary package to a location
     * that supports upgrade and downgrade. Specifically,
     *
     * <ul>
     * <li>If the target directory doesn't exist {@linkplain #mkdirs() it'll be created}.
     * <li>The timestamp of the .tgz file is left in the installation directory upon extraction.
     * <li>If the timestamp left in the directory doesn't match with the timestamp of the current archive file,
     *     the directory contents will be discarded and the archive file will be re-extracted.
     * <li>If the connection is refused but the target directory already exists, it is left alone.
     * </ul>
     *
     * @param archive
     *      The resource that represents the tgz/zip file. This URL must support the "Last-Modified" header.
     *      (Most common usage is to get this from {@link ClassLoader#getResource(String)})
     * @param listener
     *      If non-null, a message will be printed to this listener once this method decides to
     *      extract an archive.
     * @return
     *      true if the archive was extracted. false if the extraction was skipped because the target directory
     *      was considered up to date.
     * @since 1.299
     */
    public boolean installIfNecessaryFrom(URL archive, TaskListener listener, String message) throws IOException, InterruptedException {
        try {
            URLConnection con;
            try {
                con = ProxyConfiguration.open(archive);
                con.connect();
            } catch (IOException x) {
                if (this.exists()) {
                    // Cannot connect now, so assume whatever was last unpacked is still OK.
                    if (listener != null) {
                        listener.getLogger().println("Skipping installation of " + archive + " to " + remote + ": " + x);
                    }
                    return false;
                } else {
                    throw x;
                }
            }
            long sourceTimestamp = con.getLastModified();
            FilePath timestamp = this.child(".timestamp");

            if(this.exists()) {
                if(timestamp.exists() && sourceTimestamp ==timestamp.lastModified())
                    return false;   // already up to date
                this.deleteContents();
            } else {
                this.mkdirs();
            }

            if(listener!=null)
                listener.getLogger().println(message);

            // for HTTP downloads, enable automatic retry for added resilience
            InputStream in = archive.getProtocol().startsWith("http") ? ProxyConfiguration.getInputStream(archive) : con.getInputStream();
            CountingInputStream cis = new CountingInputStream(in);
            try {
                if(archive.toExternalForm().endsWith(".zip"))
                    unzipFrom(cis);
            else
                untarFrom(cis,GZIP);
            } catch (IOException e) {
                throw new IOException2(String.format("Failed to unpack %s (%d bytes read of total %d)",
                        archive,cis.getByteCount(),con.getContentLength()),e);
            }
            timestamp.touch(sourceTimestamp);
            return true;
        } catch (IOException e) {
            throw new IOException2("Failed to install "+archive+" to "+remote,e);
        }
    }

    /**
     * Reads the URL on the current VM, and writes all the data to this {@link FilePath}
     * (this is different from resolving URL remotely.)
     *
     * @since 1.293
     */
    public void copyFrom(URL url) throws IOException, InterruptedException {
        InputStream in = url.openStream();
        try {
            copyFrom(in);
        } finally {
            in.close();
        }
    }

    /**
     * Replaces the content of this file by the data from the given {@link InputStream}.
     *
     * @since 1.293
     */
    public void copyFrom(InputStream in) throws IOException, InterruptedException {
        OutputStream os = write();
        try {
            IOUtils.copy(in, os);
        } finally {
            os.close();
        }
    }

    /**
     * Convenience method to call {@link FilePath#copyTo(FilePath)}.
     * 
     * @since 1.311
     */
    public void copyFrom(FilePath src) throws IOException, InterruptedException {
        src.copyTo(this);
    }

    /**
     * Place the data from {@link FileItem} into the file location specified by this {@link FilePath} object.
     */
    public void copyFrom(FileItem file) throws IOException, InterruptedException {
        if(channel==null) {
            try {
                file.write(new File(remote));
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException2(e);
            }
        } else {
            InputStream i = file.getInputStream();
            OutputStream o = write();
            try {
                IOUtils.copy(i,o);
            } finally {
                try {
                    o.close();
                } finally {
                    i.close();
                }
            }
        }
    }

    /**
     * Code that gets executed on the machine where the {@link FilePath} is local.
     * Used to act on {@link FilePath}.
     *
     * @see FilePath#act(FileCallable)
     */
    public interface FileCallable<T> extends Serializable {
        /**
         * Performs the computational task on the node where the data is located.
         *
         * <p>
         * All the exceptions are forwarded to the caller.
         *
         * @param f
         *      {@link File} that represents the local file that {@link FilePath} has represented.
         * @param channel
         *      The "back pointer" of the {@link Channel} that represents the communication
         *      with the node from where the code was sent.
         */
        T invoke(File f, VirtualChannel channel) throws IOException, InterruptedException;
    }

    /**
     * Executes some program on the machine that this {@link FilePath} exists,
     * so that one can perform local file operations.
     */
    public <T> T act(final FileCallable<T> callable) throws IOException, InterruptedException {
        return act(callable,callable.getClass().getClassLoader());
    }

    private <T> T act(final FileCallable<T> callable, ClassLoader cl) throws IOException, InterruptedException {
        if(channel!=null) {
            // run this on a remote system
            try {
                DelegatingCallable<T,IOException> wrapper = new FileCallableWrapper<T>(callable, cl);
                Jenkins instance = Jenkins.getInstance();
                if (instance != null) { // this happens during unit tests
                    ExtensionList<FileCallableWrapperFactory> factories = instance.getExtensionList(FileCallableWrapperFactory.class);
                    for (FileCallableWrapperFactory factory : factories) {
                        wrapper = factory.wrap(wrapper);
                    }
                }

                return channel.call(wrapper);
            } catch (TunneledInterruptedException e) {
                throw (InterruptedException)new InterruptedException(e.getMessage()).initCause(e);
            } catch (AbortException e) {
                throw e;    // pass through so that the caller can catch it as AbortException
            } catch (IOException e) {
                // wrap it into a new IOException so that we get the caller's stack trace as well.
                throw new IOException2("remote file operation failed: "+remote+" at "+channel,e);
            }
        } else {
            // the file is on the local machine.
            return callable.invoke(new File(remote), Jenkins.MasterComputer.localChannel);
        }
    }

    /**
     * This extension point allows to contribute a wrapper around a fileCallable so that a plugin can "intercept" a
     * call.
     * <p>The {@link #wrap(hudson.remoting.DelegatingCallable)} method itself will be executed on master
     * (and may collect contextual data if needed) and the returned wrapper will be executed on remote.
     *
     * @since 1.482
     * @see AbstractInterceptorCallableWrapper
     */
    public static abstract class FileCallableWrapperFactory implements ExtensionPoint {

        public abstract <T> DelegatingCallable<T,IOException> wrap(DelegatingCallable<T,IOException> callable);

    }

    /**
     * Abstract {@link DelegatingCallable} that exposes an Before/After pattern for
     * {@link hudson.FilePath.FileCallableWrapperFactory} that want to implement AOP-style interceptors
     * @since 1.482
     */
    public static abstract class AbstractInterceptorCallableWrapper<T> implements DelegatingCallable<T, IOException> {

        private final DelegatingCallable<T, IOException> callable;

        public AbstractInterceptorCallableWrapper(DelegatingCallable<T, IOException> callable) {
            this.callable = callable;
        }

        @Override
        public final ClassLoader getClassLoader() {
            return callable.getClassLoader();
        }

        public final T call() throws IOException {
            before();
            try {
                return callable.call();
            } finally {
                after();
            }
        }

        /**
         * Executed before the actual FileCallable is invoked. This code will run on remote
         */
        protected void before() {}

        /**
         * Executed after the actual FileCallable is invoked (even if this one failed). This code will run on remote
         */
        protected void after() {}
    }


    /**
     * Executes some program on the machine that this {@link FilePath} exists,
     * so that one can perform local file operations.
     */
    public <T> Future<T> actAsync(final FileCallable<T> callable) throws IOException, InterruptedException {
        try {
            DelegatingCallable<T,IOException> wrapper = new FileCallableWrapper<T>(callable);
            Jenkins instance = Jenkins.getInstance();
            if (instance != null) { // this happens during unit tests
                ExtensionList<FileCallableWrapperFactory> factories = instance.getExtensionList(FileCallableWrapperFactory.class);
                for (FileCallableWrapperFactory factory : factories) {
                    wrapper = factory.wrap(wrapper);
                }
            }

            return (channel!=null ? channel : Jenkins.MasterComputer.localChannel)
                .callAsync(wrapper);
        } catch (IOException e) {
            // wrap it into a new IOException so that we get the caller's stack trace as well.
            throw new IOException2("remote file operation failed",e);
        }
    }

    /**
     * Executes some program on the machine that this {@link FilePath} exists,
     * so that one can perform local file operations.
     */
    public <V,E extends Throwable> V act(Callable<V,E> callable) throws IOException, InterruptedException, E {
        if(channel!=null) {
            // run this on a remote system
            return channel.call(callable);
        } else {
            // the file is on the local machine
            return callable.call();
        }
    }

    /**
     * Converts this file to the URI, relative to the machine
     * on which this file is available.
     */
    public URI toURI() throws IOException, InterruptedException {
        return act(new FileCallable<URI>() {
            public URI invoke(File f, VirtualChannel channel) {
                return f.toURI();
            }
        });
    }

    /**
     * Creates this directory.
     */
    public void mkdirs() throws IOException, InterruptedException {
        if(!act(new FileCallable<Boolean>() {
            public Boolean invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                if(f.mkdirs() || f.exists())
                    return true;    // OK

                // following Ant <mkdir> task to avoid possible race condition.
                Thread.sleep(10);

                return f.mkdirs() || f.exists();
            }
        }))
            throw new IOException("Failed to mkdirs: "+remote);
    }

    /**
     * Deletes this directory, including all its contents recursively.
     */
    public void deleteRecursive() throws IOException, InterruptedException {
        act(new FileCallable<Void>() {
            public Void invoke(File f, VirtualChannel channel) throws IOException {
                Util.deleteRecursive(f);
                return null;
            }
        });
    }

    /**
     * Deletes all the contents of this directory, but not the directory itself
     */
    public void deleteContents() throws IOException, InterruptedException {
        act(new FileCallable<Void>() {
            public Void invoke(File f, VirtualChannel channel) throws IOException {
                Util.deleteContentsRecursive(f);
                return null;
            }
        });
    }

    /**
     * Gets the file name portion except the extension.
     *
     * For example, "foo" for "foo.txt" and "foo.tar" for "foo.tar.gz".
     */
    public String getBaseName() {
        String n = getName();
        int idx = n.lastIndexOf('.');
        if (idx<0)  return n;
        return n.substring(0,idx);
    }
    /**
     * Gets just the file name portion without directories.
     *
     * For example, "foo.txt" for "../abc/foo.txt"
     */
    public String getName() {
        String r = remote;
        if(r.endsWith("\\") || r.endsWith("/"))
            r = r.substring(0,r.length()-1);

        int len = r.length()-1;
        while(len>=0) {
            char ch = r.charAt(len);
            if(ch=='\\' || ch=='/')
                break;
            len--;
        }

        return r.substring(len+1);
    }

    /**
     * Short for {@code getParent().child(rel)}. Useful for getting other files in the same directory. 
     */
    public FilePath sibling(String rel) {
        return getParent().child(rel);
    }

    /**
     * Returns a {@link FilePath} by adding the given suffix to this path name.
     */
    public FilePath withSuffix(String suffix) {
        return new FilePath(channel,remote+suffix);
    }

    /**
     * The same as {@link FilePath#FilePath(FilePath,String)} but more OO.
     * @param relOrAbsolute a relative or absolute path
     * @return a file on the same channel
     */
    public FilePath child(String relOrAbsolute) {
        return new FilePath(this,relOrAbsolute);
    }

    /**
     * Gets the parent file.
     * @return parent FilePath or null if there is no parent
     */
    public FilePath getParent() {
        int i = remote.length() - 2;
        for (; i >= 0; i--) {
            char ch = remote.charAt(i);
            if(ch=='\\' || ch=='/')
                break;
        }

        return i >= 0 ? new FilePath( channel, remote.substring(0,i+1) ) : null;
    }

    /**
     * Creates a temporary file in the directory that this {@link FilePath} object designates.
     *
     * @param prefix
     *      The prefix string to be used in generating the file's name; must be
     *      at least three characters long
     * @param suffix
     *      The suffix string to be used in generating the file's name; may be
     *      null, in which case the suffix ".tmp" will be used
     * @return
     *      The new FilePath pointing to the temporary file
     * @see File#createTempFile(String, String)
     */
    public FilePath createTempFile(final String prefix, final String suffix) throws IOException, InterruptedException {
        try {
            return new FilePath(this,act(new FileCallable<String>() {
                public String invoke(File dir, VirtualChannel channel) throws IOException {
                    File f = File.createTempFile(prefix, suffix, dir);
                    return f.getName();
                }
            }));
        } catch (IOException e) {
            throw new IOException2("Failed to create a temp file on "+remote,e);
        }
    }

    /**
     * Creates a temporary file in this directory and set the contents to the
     * given text (encoded in the platform default encoding)
     *
     * @param prefix
     *      The prefix string to be used in generating the file's name; must be
     *      at least three characters long
     * @param suffix
     *      The suffix string to be used in generating the file's name; may be
     *      null, in which case the suffix ".tmp" will be used
     * @param contents
     *      The initial contents of the temporary file.
     * @return
     *      The new FilePath pointing to the temporary file
     * @see File#createTempFile(String, String)
     */
    public FilePath createTextTempFile(final String prefix, final String suffix, final String contents) throws IOException, InterruptedException {
        return createTextTempFile(prefix,suffix,contents,true);
    }

    /**
     * Creates a temporary file in this directory (or the system temporary
     * directory) and set the contents to the given text (encoded in the
     * platform default encoding)
     *
     * @param prefix
     *      The prefix string to be used in generating the file's name; must be
     *      at least three characters long
     * @param suffix
     *      The suffix string to be used in generating the file's name; may be
     *      null, in which case the suffix ".tmp" will be used
     * @param contents
     *      The initial contents of the temporary file.
     * @param inThisDirectory
     *      If true, then create this temporary in the directory pointed to by
     *      this.
     *      If false, then the temporary file is created in the system temporary
     *      directory (java.io.tmpdir)
     * @return
     *      The new FilePath pointing to the temporary file
     * @see File#createTempFile(String, String)
     */
    public FilePath createTextTempFile(final String prefix, final String suffix, final String contents, final boolean inThisDirectory) throws IOException, InterruptedException {
        try {
            return new FilePath(channel,act(new FileCallable<String>() {
                public String invoke(File dir, VirtualChannel channel) throws IOException {
                    if(!inThisDirectory)
                        dir = new File(System.getProperty("java.io.tmpdir"));
                    else
                        dir.mkdirs();

                    File f;
                    try {
                        f = File.createTempFile(prefix, suffix, dir);
                    } catch (IOException e) {
                        throw new IOException2("Failed to create a temporary directory in "+dir,e);
                    }

                    Writer w = new FileWriter(f);
                    w.write(contents);
                    w.close();

                    return f.getAbsolutePath();
                }
            }));
        } catch (IOException e) {
            throw new IOException2("Failed to create a temp file on "+remote,e);
        }
    }

    /**
     * Creates a temporary directory inside the directory represented by 'this'
     *
     * @param prefix
     *      The prefix string to be used in generating the directory's name;
     *      must be at least three characters long
     * @param suffix
     *      The suffix string to be used in generating the directory's name; may
     *      be null, in which case the suffix ".tmp" will be used
     * @return
     *      The new FilePath pointing to the temporary directory
     * @since 1.311
     * @see File#createTempFile(String, String)
     */
    public FilePath createTempDir(final String prefix, final String suffix) throws IOException, InterruptedException {
        try {
            return new FilePath(this,act(new FileCallable<String>() {
                public String invoke(File dir, VirtualChannel channel) throws IOException {
                    File f = File.createTempFile(prefix, suffix, dir);
                    f.delete();
                    f.mkdir();
                    return f.getName();
                }
            }));
        } catch (IOException e) {
            throw new IOException2("Failed to create a temp directory on "+remote,e);
        }
    }

    /**
     * Deletes this file.
     * @throws IOException if it exists but could not be successfully deleted
     * @return true, for a modicum of compatibility
     */
    public boolean delete() throws IOException, InterruptedException {
        act(new FileCallable<Void>() {
            public Void invoke(File f, VirtualChannel channel) throws IOException {
                Util.deleteFile(f);
                return null;
            }
        });
        return true;
    }

    /**
     * Checks if the file exists.
     */
    public boolean exists() throws IOException, InterruptedException {
        return act(new FileCallable<Boolean>() {
            public Boolean invoke(File f, VirtualChannel channel) throws IOException {
                return f.exists();
            }
        });
    }

    /**
     * Gets the last modified time stamp of this file, by using the clock
     * of the machine where this file actually resides.
     *
     * @see File#lastModified()
     * @see #touch(long)
     */
    public long lastModified() throws IOException, InterruptedException {
        return act(new FileCallable<Long>() {
            public Long invoke(File f, VirtualChannel channel) throws IOException {
                return f.lastModified();
            }
        });
    }

    /**
     * Creates a file (if not already exist) and sets the timestamp.
     *
     * @since 1.299
     */
    public void touch(final long timestamp) throws IOException, InterruptedException {
        act(new FileCallable<Void>() {
            private static final long serialVersionUID = -5094638816500738429L;
            public Void invoke(File f, VirtualChannel channel) throws IOException {
                if(!f.exists())
                    new FileOutputStream(f).close();
                if(!f.setLastModified(timestamp))
                    throw new IOException("Failed to set the timestamp of "+f+" to "+timestamp);
                return null;
            }
        });
    }
    
    private void setLastModifiedIfPossible(final long timestamp) throws IOException, InterruptedException {
        String message = act(new FileCallable<String>() {
            private static final long serialVersionUID = -828220335793641630L;
            public String invoke(File f, VirtualChannel channel) throws IOException {
                if(!f.setLastModified(timestamp)) {
                    if (Functions.isWindows()) {
                        // On Windows this seems to fail often. See JENKINS-11073
                        // Therefore don't fail, but just log a warning
                        return "Failed to set the timestamp of "+f+" to "+timestamp;
                    } else {
                        throw new IOException("Failed to set the timestamp of "+f+" to "+timestamp);
                    }
                }
                return null;
            }
        });

        if (message!=null) {
            LOGGER.warning(message);
        }
    }

    /**
     * Checks if the file is a directory.
     */
    public boolean isDirectory() throws IOException, InterruptedException {
        return act(new FileCallable<Boolean>() {
            public Boolean invoke(File f, VirtualChannel channel) throws IOException {
                return f.isDirectory();
            }
        });
    }
    
    /**
     * Returns the file size in bytes.
     *
     * @since 1.129
     */
    public long length() throws IOException, InterruptedException {
        return act(new FileCallable<Long>() {
            public Long invoke(File f, VirtualChannel channel) throws IOException {
                return f.length();
            }
        });
    }

    /**
     * Sets the file permission.
     *
     * On Windows, no-op.
     *
     * @param mask
     *      File permission mask. To simplify the permission copying,
     *      if the parameter is -1, this method becomes no-op.
     *      <p>
     *      please note mask is expected to be an octal if you use <a href="http://en.wikipedia.org/wiki/Chmod">chmod command line values</a>,
     *      so preceded by a '0' in java notation, ie <code>chmod(0644)</code>
     *
     * @since 1.303
     * @see #mode()
     */
    public void chmod(final int mask) throws IOException, InterruptedException {
        if(!isUnix() || mask==-1)   return;
        act(new FileCallable<Void>() {
            public Void invoke(File f, VirtualChannel channel) throws IOException {
                _chmod(f, mask);

                return null;
            }
        });
    }

    /**
     * Run chmod via libc if we can, otherwise fall back to Ant.
     */
    private static void _chmod(File f, int mask) throws IOException {
        if (Functions.isWindows())  return; // noop

        try {
            if(LIBC.chmod(f.getAbsolutePath(),mask)!=0) {
                throw new IOException("Failed to chmod "+f+" : "+LIBC.strerror(Native.getLastError()));
            }
        } catch(NoClassDefFoundError e) {  // cf. https://groups.google.com/group/hudson-dev/browse_thread/thread/6d16c3e8ea0dbc9?hl=fr
            _chmodAnt(f, mask);
        } catch(UnsatisfiedLinkError e2) { // HUDSON-8155: use Ant's chmod task on non-GNU C systems
            _chmodAnt(f, mask);
        }
    }

    private static void _chmodAnt(File f, int mask) {
        if (!CHMOD_WARNED) { // only warn this once to avoid flooding the log
            CHMOD_WARNED = true;
            LOGGER.warning("GNU C Library not available: Using Ant's chmod task instead.");
        }
        Chmod chmodTask = new Chmod();
        chmodTask.setProject(new Project());
        chmodTask.setFile(f);
        chmodTask.setPerm(Integer.toOctalString(mask));
        chmodTask.execute();
    }

    private static boolean CHMOD_WARNED = false;

    /**
     * Gets the file permission bit mask.
     *
     * @return
     *      -1 on Windows, since such a concept doesn't make sense.
     * @since 1.311
     * @see #chmod(int)
     */
    public int mode() throws IOException, InterruptedException, PosixException {
        if(!isUnix())   return -1;
        return act(new FileCallable<Integer>() {
            public Integer invoke(File f, VirtualChannel channel) throws IOException {
                return IOUtils.mode(f);
            }
        });
    }

    /**
     * List up files and directories in this directory.
     *
     * <p>
     * This method returns direct children of the directory denoted by the 'this' object.
     */
    public List<FilePath> list() throws IOException, InterruptedException {
        return list((FileFilter)null);
    }

    /**
     * List up subdirectories.
     *
     * @return can be empty but never null. Doesn't contain "." and ".."
     */
    public List<FilePath> listDirectories() throws IOException, InterruptedException {
        return list(new DirectoryFilter());
    }

    private static final class DirectoryFilter implements FileFilter, Serializable {
        public boolean accept(File f) {
            return f.isDirectory();
        }
        private static final long serialVersionUID = 1L;
    }

    /**
     * List up files in this directory, just like {@link File#listFiles(FileFilter)}.
     *
     * @param filter
     *      The optional filter used to narrow down the result.
     *      If non-null, must be {@link Serializable}.
     *      If this {@link FilePath} represents a remote path,
     *      the filter object will be executed on the remote machine.
     */
    public List<FilePath> list(final FileFilter filter) throws IOException, InterruptedException {
        if (filter != null && !(filter instanceof Serializable)) {
            throw new IllegalArgumentException("Non-serializable filter of " + filter.getClass());
        }
        return act(new FileCallable<List<FilePath>>() {
            public List<FilePath> invoke(File f, VirtualChannel channel) throws IOException {
                File[] children = f.listFiles(filter);
                if(children ==null)     return null;

                ArrayList<FilePath> r = new ArrayList<FilePath>(children.length);
                for (File child : children)
                    r.add(new FilePath(child));

                return r;
            }
        }, (filter!=null?filter:this).getClass().getClassLoader());
    }

    /**
     * List up files in this directory that matches the given Ant-style filter.
     *
     * @param includes
     *      See {@link FileSet} for the syntax. String like "foo/*.zip" or "foo/*&#42;/*.xml"
     * @return
     *      can be empty but always non-null.
     */
    public FilePath[] list(final String includes) throws IOException, InterruptedException {
        return list(includes, null);
    }

    /**
     * List up files in this directory that matches the given Ant-style filter.
     *
     * @param includes
     * @param excludes
     *      See {@link FileSet} for the syntax. String like "foo/*.zip" or "foo/*&#42;/*.xml"
     * @return
     *      can be empty but always non-null.
     * @since 1.407
     */
    public FilePath[] list(final String includes, final String excludes) throws IOException, InterruptedException {
        return list(includes, excludes, true);
    }

    /**
     * List up files in this directory that matches the given Ant-style filter.
     *
     * @param includes
     * @param excludes
     *      See {@link FileSet} for the syntax. String like "foo/*.zip" or "foo/*&#42;/*.xml"
     * @param defaultExcludes whether to use the ant default excludes
     * @return
     *      can be empty but always non-null.
     * @since 1.465
     */
    public FilePath[] list(final String includes, final String excludes, final boolean defaultExcludes) throws IOException, InterruptedException {
        return act(new FileCallable<FilePath[]>() {
            public FilePath[] invoke(File f, VirtualChannel channel) throws IOException {
                String[] files = glob(f, includes, excludes, defaultExcludes);

                FilePath[] r = new FilePath[files.length];
                for( int i=0; i<r.length; i++ )
                    r[i] = new FilePath(new File(f,files[i]));

                return r;
            }
        });
    }

    /**
     * Runs Ant glob expansion.
     *
     * @return
     *      A set of relative file names from the base directory.
     */
    private static String[] glob(File dir, String includes, String excludes, boolean defaultExcludes) throws IOException {
        if(isAbsolute(includes))
            throw new IOException("Expecting Ant GLOB pattern, but saw '"+includes+"'. See http://ant.apache.org/manual/Types/fileset.html for syntax");
        FileSet fs = Util.createFileSet(dir,includes,excludes);
        fs.setDefaultexcludes(defaultExcludes);
        DirectoryScanner ds = fs.getDirectoryScanner(new Project());
        String[] files = ds.getIncludedFiles();
        return files;
    }

    /**
     * Reads this file.
     */
    public InputStream read() throws IOException {
        if(channel==null)
            return new FileInputStream(new File(remote));

        final Pipe p = Pipe.createRemoteToLocal();
        channel.callAsync(new Callable<Void,IOException>() {
            public Void call() throws IOException {
                FileInputStream fis=null;
                try {
                    fis = new FileInputStream(new File(remote));
                    Util.copyStream(fis,p.getOut());
                    return null;
                } finally {
                    IOUtils.closeQuietly(fis);
                    IOUtils.closeQuietly(p.getOut());
                }
            }
        });

        return p.getIn();
    }

    /**
     * Reads this file into a string, by using the current system encoding.
     */
    public String readToString() throws IOException {
        InputStream in = read();
        try {
            return IOUtils.toString(in);
        } finally {
            in.close();
        }
    }

    /**
     * Writes to this file.
     * If this file already exists, it will be overwritten.
     * If the directory doesn't exist, it will be created.
     *
     * <P>
     * I/O operation to remote {@link FilePath} happens asynchronously, meaning write operations to the returned
     * {@link OutputStream} will return without receiving a confirmation from the remote that the write happened.
     * I/O operations also happens asynchronously from the {@link Channel#call(Callable)} operations, so if
     * you write to a remote file and then execute {@link Channel#call(Callable)} and try to access the newly copied
     * file, it might not be fully written yet.
     *
     * <p>
     *
     */
    public OutputStream write() throws IOException, InterruptedException {
        if(channel==null) {
            File f = new File(remote).getAbsoluteFile();
            f.getParentFile().mkdirs();
            return new FileOutputStream(f);
        }

        return channel.call(new Callable<OutputStream,IOException>() {
            public OutputStream call() throws IOException {
                File f = new File(remote).getAbsoluteFile();
                f.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(f);
                return new RemoteOutputStream(fos);
            }
        });
    }

    /**
     * Overwrites this file by placing the given String as the content.
     *
     * @param encoding
     *      Null to use the platform default encoding.
     * @since 1.105
     */
    public void write(final String content, final String encoding) throws IOException, InterruptedException {
        act(new FileCallable<Void>() {
            public Void invoke(File f, VirtualChannel channel) throws IOException {
                f.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(f);
                Writer w = encoding != null ? new OutputStreamWriter(fos, encoding) : new OutputStreamWriter(fos);
                try {
                    w.write(content);
                } finally {
                    w.close();
                }
                return null;
            }
        });
    }

    /**
     * Computes the MD5 digest of the file in hex string.
     */
    public String digest() throws IOException, InterruptedException {
        return act(new FileCallable<String>() {
            public String invoke(File f, VirtualChannel channel) throws IOException {
                return Util.getDigestOf(new FileInputStream(f));
            }
        });
    }

    /**
     * Rename this file/directory to the target filepath.  This FilePath and the target must
     * be on the some host
     */
    public void renameTo(final FilePath target) throws IOException, InterruptedException {
    	if(this.channel != target.channel) {
    		throw new IOException("renameTo target must be on the same host");
    	}
        act(new FileCallable<Void>() {
            public Void invoke(File f, VirtualChannel channel) throws IOException {
            	f.renameTo(new File(target.remote));
                return null;
            }
        });
    }

    /**
     * Moves all the contents of this directory into the specified directory, then delete this directory itself.
     *
     * @since 1.308.
     */
    public void moveAllChildrenTo(final FilePath target) throws IOException, InterruptedException {
        if(this.channel != target.channel) {
            throw new IOException("pullUpTo target must be on the same host");
        }
        act(new FileCallable<Void>() {
            public Void invoke(File f, VirtualChannel channel) throws IOException {
                File t = new File(target.getRemote());
                
                for(File child : f.listFiles()) {
                    File target = new File(t, child.getName());
                    if(!child.renameTo(target))
                        throw new IOException("Failed to rename "+child+" to "+target);
                }
                f.delete();
                return null;
            }
        });
    }

    /**
     * Copies this file to the specified target.
     */
    public void copyTo(FilePath target) throws IOException, InterruptedException {
        try {
            OutputStream out = target.write();
            try {
                copyTo(out);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new IOException2("Failed to copy "+this+" to "+target,e);
        }
    }

    /**
     * Copies this file to the specified target, with file permissions and other meta attributes intact.
     * @since 1.311
     */
    public void copyToWithPermission(FilePath target) throws IOException, InterruptedException {
        copyTo(target);
        // copy file permission
        target.chmod(mode());
        target.setLastModifiedIfPossible(lastModified());
    }

    /**
     * Sends the contents of this file into the given {@link OutputStream}.
     */
    public void copyTo(OutputStream os) throws IOException, InterruptedException {
        final OutputStream out = new RemoteOutputStream(os);

        act(new FileCallable<Void>() {
            private static final long serialVersionUID = 4088559042349254141L;
            public Void invoke(File f, VirtualChannel channel) throws IOException {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(f);
                    Util.copyStream(fis,out);
                    return null;
                } finally {
                    IOUtils.closeQuietly(fis);
                    IOUtils.closeQuietly(out);
                }
            }
        });

        // make sure the writes fully got delivered to 'os' before we return.
        // this is needed because I/O operation is asynchronous
        syncIO();
    }

    /**
     * With fix to JENKINS-11251 (remoting 2.15), this is no longer necessary.
     * But I'm keeping it for a while so that users who manually deploy slave.jar has time to deploy new version
     * before this goes away.
     */
    private void syncIO() throws InterruptedException {
        try {
            if (channel!=null)
                _syncIO();
        } catch (AbstractMethodError e) {
            // legacy slave.jar. Handle this gracefully
            try {
                LOGGER.log(Level.WARNING,"Looks like an old slave.jar. Please update "+ Which.jarFile(Channel.class)+" to the new version",e);
            } catch (IOException _) {
                // really ignore this time
            }
        }
    }

    /**
     * A pointless function to work around what appears to be a HotSpot problem. See JENKINS-5756 and bug 6933067
     * on BugParade for more details.
     */
    private void _syncIO() throws InterruptedException {
        channel.syncLocalIO();
    }

    /**
     * Remoting interface used for {@link FilePath#copyRecursiveTo(String, FilePath)}.
     *
     * TODO: this might not be the most efficient way to do the copy.
     */
    interface RemoteCopier {
        /**
         * @param fileName
         *      relative path name to the output file. Path separator must be '/'.
         */
        void open(String fileName) throws IOException;
        void write(byte[] buf, int len) throws IOException;
        void close() throws IOException;
    }

    /**
     * Copies the contents of this directory recursively into the specified target directory.
     * 
     * @return
     *      the number of files copied.
     * @since 1.312 
     */
    public int copyRecursiveTo(FilePath target) throws IOException, InterruptedException {
        return copyRecursiveTo("**/*",target);
    }

    /**
     * Copies the files that match the given file mask to the specified target node.
     *
     * @param fileMask
     *      Ant G

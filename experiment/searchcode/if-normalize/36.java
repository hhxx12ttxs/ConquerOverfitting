/*
 * Copyright  2001-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.izforge.izpack.util.file;

import com.izforge.izpack.util.OsVersion;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.StringCharacterIterator;
import java.util.Random;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;

/**
 * This class also encapsulates methods which allow Files to be
 * referred to using abstract path names which are translated to native
 * system file paths at runtime as well as copying files or setting
 * their last modification time.
 */

public class FileUtils
{

    private static final FileUtils PRIMARY_INSTANCE = new FileUtils();

    //get some non-crypto-grade randomness from various places.
    private static Random rand = new Random(System.currentTimeMillis()
            + Runtime.getRuntime().freeMemory());

    private static final int BUF_SIZE = 8192;

    // for toURI
    private static boolean[] isSpecial = new boolean[256];
    private static char[] escapedChar1 = new char[256];
    private static char[] escapedChar2 = new char[256];

    /**
     * The granularity of timestamps under FAT.
     */
    public static final long FAT_FILE_TIMESTAMP_GRANULARITY = 2000;

    /**
     * The granularity of timestamps under Unix.
     */
    public static final long UNIX_FILE_TIMESTAMP_GRANULARITY = 1000;


    // stolen from FilePathToURI of the Xerces-J team

    static
    {
        for (int i = 0; i <= 0x20; i++)
        {
            isSpecial[i] = true;
            escapedChar1[i] = Character.forDigit(i >> 4, 16);
            escapedChar2[i] = Character.forDigit(i & 0xf, 16);
        }
        isSpecial[0x7f] = true;
        escapedChar1[0x7f] = '7';
        escapedChar2[0x7f] = 'F';
        char[] escChs = {'<', '>', '#', '%', '"', '{', '}',
                '|', '\\', '^', '~', '[', ']', '`'};
        int len = escChs.length;
        char ch;
        for (int i = 0; i < len; i++)
        {
            ch = escChs[i];
            isSpecial[ch] = true;
            escapedChar1[ch] = Character.forDigit(ch >> 4, 16);
            escapedChar2[ch] = Character.forDigit(ch & 0xf, 16);
        }
    }

    /**
     * Wrapper around java.io.File#createTempFile(String, String) that caters for
     * Mac OS X issues with "+" characters ending up in paths. It delegates to
     * java.io.File.createTempFile(String, String) on every platform but Mac OS X, where
     * it delegates to java.io.File.createTempFile(String, String, new File("/tmp").
     *
     * @param prefix temporary file prefix
     * @param suffix temporary file suffix
     * @return a temporary file
     * @throws java.io.IOException when a temporary file cannot be allocated
     * @see java.io.File#createTempFile(String, String)
     */
    public static File createTempFile(String prefix, String suffix) throws IOException
    {
        if (OsVersion.IS_OSX)
        {
            return File.createTempFile(prefix, suffix, new File("/tmp"));
        }
        else
        {
            return File.createTempFile(prefix, suffix);
        }
    }

    /**
     * Method to retrieve The FileUtils, which is shared by all users of this
     * method.
     *
     * @return an instance of FileUtils.
     */
    public static FileUtils getFileUtils()
    {
        return PRIMARY_INSTANCE;
    }

    /**
     * Empty constructor.
     */
    protected FileUtils()
    {
    }

    /**
     * Get the URL for a file taking into account # characters.
     *
     * @param file the file whose URL representation is required.
     * @return The FileURL value.
     * @throws MalformedURLException if the URL representation cannot be
     *                               formed.
     */
    public URL getFileURL(File file) throws MalformedURLException
    {
        return new URL(toURI(file.getAbsolutePath()));
    }

    /**
     * Convenience method to copy a file from a source to a destination.
     * No filtering is performed.
     *
     * @param sourceFile Name of file to copy from.
     *                   Must not be <code>null</code>.
     * @param destFile   Name of file to copy to.
     *                   Must not be <code>null</code>.
     * @throws IOException if the copying fails.
     */
    public void copyFile(String sourceFile, String destFile,
                         boolean overwrite, boolean preserveLastModified)
            throws IOException
    {
        copyFile(new File(sourceFile), new File(destFile), overwrite, preserveLastModified);
    }

    /**
     * Convenience method to copy a file from a source to a destination.
     * No filtering is performed.
     *
     * @param sourceFile the file to copy from.
     *                   Must not be <code>null</code>.
     * @param destFile   the file to copy to.
     *                   Must not be <code>null</code>.
     * @throws IOException if the copying fails.
     */
    public void copyFile(File sourceFile, File destFile) throws IOException
    {
        copyFile(sourceFile, destFile, false, false);
    }

    /**
     * Convenience method to copy a file from a source to a
     * destination specifying if token filtering must be used, if
     * filter chains must be used, if source files may overwrite
     * newer destination files and the last modified time of
     * <code>destFile</code> file should be made equal
     * to the last modified time of <code>sourceFile</code>.
     *
     * @param sourceFile           the file to copy from.
     *                             Must not be <code>null</code>.
     * @param destFile             the file to copy to.
     *                             Must not be <code>null</code>.
     * @param filters              the collection of filters to apply to this copy.
     * @param filterChains         filterChains to apply during the copy.
     * @param overwrite            Whether or not the destination file should be
     *                             overwritten if it already exists.
     * @param preserveLastModified Whether or not the last modified time of
     *                             the resulting file should be set to that
     *                             of the source file.
     * @param inputEncoding        the encoding used to read the files.
     * @param outputEncoding       the encoding used to write the files.
     * @param project              the project instance.
     * @throws IOException if the copying fails.
     */
    public void copyFile(File sourceFile, File destFile,
                         /*FilterSetCollection filters, Vector filterChains,*/
                         boolean overwrite, boolean preserveLastModified
                         /*String inputEncoding, String outputEncoding,
                         Project project*/)
            throws IOException
    {

        if (overwrite || !destFile.exists()
                || destFile.lastModified() < sourceFile.lastModified())
        {

            if (destFile.exists() && destFile.isFile())
            {
                destFile.delete();
            }
            // ensure that parent dir of dest file exists!
            // not using getParentFile method to stay 1.1 compat
            File parent = destFile.getParentFile();
            if (parent != null && !parent.exists())
            {
                parent.mkdirs();
            }

            FileInputStream in = null;
            FileOutputStream out = null;
            try
            {
                in = new FileInputStream(sourceFile);
                out = new FileOutputStream(destFile);

                byte[] buffer = new byte[BUF_SIZE];
                int count = 0;
                do
                {
                    out.write(buffer, 0, count);
                    count = in.read(buffer, 0, buffer.length);
                }
                while (count != -1);
            }
            finally
            {
                close(out);
                close(in);
            }

            if (preserveLastModified)
            {
                setFileLastModified(destFile, sourceFile.lastModified());
            }
        }
    }

    /**
     * Calls File.setLastModified(long time). Originally written to
     * to dynamically bind to that call on Java1.2+.
     *
     * @param file the file whose modified time is to be set
     * @param time the time to which the last modified time is to be set.
     *             if this is -1, the current time is used.
     */
    public void setFileLastModified(File file, long time)
    {
        file.setLastModified((time < 0) ? System.currentTimeMillis() : time);
    }

    /**
     * Interpret the filename as a file relative to the given file
     * unless the filename already represents an absolute filename.
     *
     * @param file     the "reference" file for relative paths. This
     *                 instance must be an absolute file and must not contain
     *                 &quot;./&quot; or &quot;../&quot; sequences (same for \ instead
     *                 of /).  If it is null, this call is equivalent to
     *                 <code>new java.io.File(filename)</code>.
     * @param filename a file name.
     * @return an absolute file that doesn't contain &quot;./&quot; or
     *         &quot;../&quot; sequences and uses the correct separator for
     *         the current platform.
     */
    public File resolveFile(File file, String filename) throws Exception
    {
        filename = filename.replace('/', File.separatorChar)
                .replace('\\', File.separatorChar);

        // deal with absolute files
        if (isAbsolutePath(filename))
        {
            return normalize(filename);
        }
        if (file == null)
        {
            return new File(filename);
        }
        File helpFile = new File(file.getAbsolutePath());
        StringTokenizer tok = new StringTokenizer(filename, File.separator);
        while (tok.hasMoreTokens())
        {
            String part = tok.nextToken();
            if (part.equals(".."))
            {
                helpFile = helpFile.getParentFile();
                if (helpFile == null)
                {
                    String msg = "The file or path you specified ("
                            + filename + ") is invalid relative to "
                            + file.getPath();
                    throw new Exception(msg);
                }
            }
            else if (part.equals("."))
            {
                // Do nothing here
            }
            else
            {
                helpFile = new File(helpFile, part);
            }
        }
        return new File(helpFile.getAbsolutePath());
    }

    /**
     * Verifies that the specified filename represents an absolute path.
     *
     * @param filename the filename to be checked.
     * @return true if the filename represents an absolute path.
     */
    public static boolean isAbsolutePath(String filename)
    {
        if (filename.startsWith(File.separator))
        {
            // common for all os
            return true;
        }
        if (OsVersion.IS_WINDOWS && filename.length() >= 2
                && Character.isLetter(filename.charAt(0))
                && filename.charAt(1) == ':')
        {
            // Actually on windows the : must be followed by a \ for
            // the path to be absolute, else the path is relative
            // to the current working directory on that drive.
            // (Every drive may have another current working directory)
            return true;
        }
        return false;
    }

    /**
     * &quot;Normalize&quot; the given absolute path.
     * <p/>
     * <p>This includes:
     * <ul>
     * <li>Uppercase the drive letter if there is one.</li>
     * <li>Remove redundant slashes after the drive spec.</li>
     * <li>Resolve all ./, .\, ../ and ..\ sequences.</li>
     * <li>DOS style paths that start with a drive letter will have
     * \ as the separator.</li>
     * </ul>
     * Unlike <code>File#getCanonicalPath()</code> this method
     * specifically does not resolve symbolic links.
     *
     * @param path the path to be normalized.
     * @return the normalized version of the path.
     * @throws java.lang.NullPointerException if the file path is
     *                                        equal to null.
     */
    public File normalize(String path) throws Exception
    {
        String orig = path;

        path = path.replace('/', File.separatorChar)
                .replace('\\', File.separatorChar);

        // make sure we are dealing with an absolute path
        int colon = path.indexOf(":");

        if (!isAbsolutePath(path))
        {
            String msg = path + " is not an absolute path";
            throw new Exception(msg);
        }
        boolean dosWithDrive = false;
        String root = null;
        // Eliminate consecutive slashes after the drive spec
        if ((OsVersion.IS_WINDOWS && path.length() >= 2
                && Character.isLetter(path.charAt(0))
                && path.charAt(1) == ':'))
        {

            dosWithDrive = true;

            char[] ca = path.replace('/', '\\').toCharArray();
            StringBuffer sbRoot = new StringBuffer();
            for (int i = 0; i < colon; i++)
            {
                sbRoot.append(Character.toUpperCase(ca[i]));
            }
            sbRoot.append(':');
            if (colon + 1 < path.length())
            {
                sbRoot.append(File.separatorChar);
            }
            root = sbRoot.toString();

            // Eliminate consecutive slashes after the drive spec
            StringBuffer sbPath = new StringBuffer();
            for (int i = colon + 1; i < ca.length; i++)
            {
                if ((ca[i] != '\\')
                        || (ca[i] == '\\' && ca[i - 1] != '\\'))
                {
                    sbPath.append(ca[i]);
                }
            }
            path = sbPath.toString().replace('\\', File.separatorChar);
        }
        else
        {
            if (path.length() == 1)
            {
                root = File.separator;
                path = "";
            }
            else if (path.charAt(1) == File.separatorChar)
            {
                // UNC drive
                root = File.separator + File.separator;
                path = path.substring(2);
            }
            else
            {
                root = File.separator;
                path = path.substring(1);
            }
        }
        Stack<String> s = new Stack<String>();
        s.push(root);
        StringTokenizer tok = new StringTokenizer(path, File.separator);
        while (tok.hasMoreTokens())
        {
            String thisToken = tok.nextToken();
            if (".".equals(thisToken))
            {
                continue;
            }
            else if ("..".equals(thisToken))
            {
                if (s.size() < 2)
                {
                    throw new Exception("Cannot resolve path " + orig);
                }
                else
                {
                    s.pop();
                }
            }
            else
            { // plain component
                s.push(thisToken);
            }
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.size(); i++)
        {
            if (i > 1)
            {
                // not before the filesystem root and not after it, since root
                // already contains one
                sb.append(File.separatorChar);
            }
            sb.append(s.elementAt(i));
        }
        path = sb.toString();
        if (dosWithDrive)
        {
            path = path.replace('/', '\\');
        }
        return new File(path);
    }

    /**
     * Returns a VMS String representation of a <code>File</code> object.
     * This is useful since the JVM by default internally converts VMS paths
     * to Unix style.
     * The returned String is always an absolute path.
     *
     * @param f The <code>File</code> to get the VMS path for.
     * @return The absolute VMS path to <code>f</code>.
     */
    public String toVMSPath(File f) throws Exception
    {
        // format: "DEVICE:[DIR.SUBDIR]FILE"
        String osPath;
        String path = normalize(f.getAbsolutePath()).getPath();
        String name = f.getName();
        boolean isAbsolute = path.charAt(0) == File.separatorChar;
        // treat directories specified using .DIR syntax as files
        boolean isDirectory = f.isDirectory()
                && !name.regionMatches(true, name.length() - 4, ".DIR", 0, 4);

        String device = null;
        StringBuffer directory = null;
        String file = null;

        int index = 0;

        if (isAbsolute)
        {
            index = path.indexOf(File.separatorChar, 1);
            if (index == -1)
            {
                return path.substring(1) + ":[000000]";
            }
            else
            {
                device = path.substring(1, index++);
            }
        }
        if (isDirectory)
        {
            directory = new StringBuffer(path.substring(index).
                    replace(File.separatorChar, '.'));
        }
        else
        {
            int dirEnd =
                    path.lastIndexOf(File.separatorChar, path.length());
            if (dirEnd == -1 || dirEnd < index)
            {
                file = path.substring(index);
            }
            else
            {
                directory = new StringBuffer(path.substring(index, dirEnd).
                        replace(File.separatorChar, '.'));
                index = dirEnd + 1;
                if (path.length() > index)
                {
                    file = path.substring(index);
                }
            }
        }
        if (!isAbsolute && directory != null)
        {
            directory.insert(0, '.');
        }
        osPath = ((device != null) ? device + ":" : "")
                + ((directory != null) ? "[" + directory + "]" : "")
                + ((file != null) ? file : "");
        return osPath;
    }

    /**
     * Create a temporary file in a given directory.
     * <p/>
     * <p>The file denoted by the returned abstract pathname did not
     * exist before this method was invoked, any subsequent invocation
     * of this method will yield a different file name.</p>
     * <p>
     * The filename is prefixNNNNNsuffix where NNNN is a random number.
     * </p>
     * <p>This method is different from File.createTempFile() of JDK 1.2
     * as it doesn't create the file itself.  It uses the location pointed
     * to by java.io.tmpdir when the parentDir attribute is null.</p>
     *
     * @param prefix    prefix before the random number.
     * @param suffix    file extension; include the '.'.
     * @param parentDir Directory to create the temporary file in;
     *                  java.io.tmpdir used if not specified.
     * @return a File reference to the new temporary file.
     */
    public File createTempFile(String prefix, String suffix, File parentDir)
    {
        File result = null;
        String parent = (parentDir == null)
                ? System.getProperty("java.io.tmpdir")
                : parentDir.getPath();

        DecimalFormat fmt = new DecimalFormat("#####");
        synchronized (rand)
        {
            do
            {
                result = new File(parent,
                        prefix + fmt.format(Math.abs(rand.nextInt()))
                                + suffix);
            }
            while (result.exists());
        }
        return result;
    }

    /**
     * Compares the contents of two files.
     *
     * @param f1 the file whose content is to be compared.
     * @param f2 the other file whose content is to be compared.
     * @return true if the content of the files is the same.
     * @throws IOException if the files cannot be read.
     */
    public boolean contentEquals(File f1, File f2) throws Exception
    {
        return contentEquals(f1, f2, false);
    }

    /**
     * Compares the contents of two files.
     *
     * @param f1       the file whose content is to be compared.
     * @param f2       the other file whose content is to be compared.
     * @param textfile true if the file is to be treated as a text file and
     *                 differences in kind of line break are to be ignored.
     * @return true if the content of the files is the same.
     * @throws IOException if the files cannot be read.
     */
    public boolean contentEquals(File f1, File f2, boolean textfile) throws Exception
    {
        if (f1.exists() != f2.exists())
        {
            return false;
        }
        if (!f1.exists())
        {
            // two not existing files are equal
            return true;
        }
        // should the following two be switched?  If f1 and f2 refer to the same file,
        // isn't their content equal regardless of whether that file is a directory?
        if (f1.isDirectory() || f2.isDirectory())
        {
            // don't want to compare directory contents for now
            return false;
        }
        if (fileNameEquals(f1, f2))
        {
            // same filename => true
            return true;
        }
        return textfile ? textEquals(f1, f2) : binaryEquals(f1, f2);
    }

    /**
     * Binary compares the contents of two files.
     * <p>
     * simple but sub-optimal comparision algorithm. written for working
     * rather than fast. Better would be a block read into buffers followed
     * by long comparisions apart from the final 1-7 bytes.
     * </p>
     *
     * @param f1 the file whose content is to be compared.
     * @param f2 the other file whose content is to be compared.
     * @return true if the content of the files is the same.
     * @throws IOException if the files cannot be read.
     */
    private boolean binaryEquals(File f1, File f2) throws IOException
    {
        if (f1.length() != f2.length())
        {
            // different size =>false
            return false;
        }

        InputStream in1 = null;
        InputStream in2 = null;
        try
        {
            in1 = new BufferedInputStream(new FileInputStream(f1));
            in2 = new BufferedInputStream(new FileInputStream(f2));

            int expectedByte = in1.read();
            while (expectedByte != -1)
            {
                if (expectedByte != in2.read())
                {
                    return false;
                }
                expectedByte = in1.read();
            }
            if (in2.read() != -1)
            {
                return false;
            }
            return true;
        }
        finally
        {
            close(in1);
            close(in2);
        }
    }

    /**
     * Text compares the contents of two files.
     * <p/>
     * Ignores different kinds of line endings.
     *
     * @param f1 the file whose content is to be compared.
     * @param f2 the other file whose content is to be compared.
     * @return true if the content of the files is the same.
     * @throws IOException if the files cannot be read.
     */
    private boolean textEquals(File f1, File f2) throws IOException
    {
        BufferedReader in1 = null;
        BufferedReader in2 = null;
        try
        {
            in1 = new BufferedReader(new FileReader(f1));
            in2 = new BufferedReader(new FileReader(f2));

            String expected = in1.readLine();
            while (expected != null)
            {
                if (!expected.equals(in2.readLine()))
                {
                    return false;
                }
                expected = in1.readLine();
            }
            if (in2.readLine() != null)
            {
                return false;
            }
            return true;
        }
        finally
        {
            close(in1);
            close(in2);
        }
    }

    /**
     * This was originally an emulation of {@link File#getParentFile} for JDK 1.1,
     * but it is now implemented using that method (Ant 1.6.3 onwards).
     *
     * @param f the file whose parent is required.
     * @return the given file's parent, or null if the file does not have a
     *         parent.
     */
    public File getParentFile(File f)
    {
        return (f == null) ? null : f.getParentFile();
    }

    /**
     * Read from reader till EOF.
     *
     * @param rdr the reader from which to read.
     * @return the contents read out of the given reader.
     * @throws IOException if the contents could not be read out from the
     *                     reader.
     */
    public static final String readFully(Reader rdr) throws IOException
    {
        return readFully(rdr, BUF_SIZE);
    }

    /**
     * Read from reader till EOF.
     *
     * @param rdr        the reader from which to read.
     * @param bufferSize the buffer size to use when reading.
     * @return the contents read out of the given reader.
     * @throws IOException if the contents could not be read out from the
     *                     reader.
     */
    public static final String readFully(Reader rdr, int bufferSize)
            throws IOException
    {
        if (bufferSize <= 0)
        {
            throw new IllegalArgumentException("Buffer size must be greater "
                    + "than 0");
        }
        final char[] buffer = new char[bufferSize];
        int bufferLength = 0;
        StringBuffer textBuffer = null;
        while (bufferLength != -1)
        {
            bufferLength = rdr.read(buffer);
            if (bufferLength > 0)
            {
                textBuffer = (textBuffer == null) ? new StringBuffer() : textBuffer;
                textBuffer.append(new String(buffer, 0, bufferLength));
            }
        }
        return (textBuffer == null) ? null : textBuffer.toString();
    }

    /**
     * This was originally an emulation of File.createNewFile for JDK 1.1,
     * but it is now implemented using that method (Ant 1.6.3 onwards).
     * <p/>
     * <p>This method has historically <strong>not</strong> guaranteed that the
     * operation was atomic. In its current implementation it is.
     *
     * @param f the file to be created.
     * @return true if the file did not exist already.
     * @throws IOException on error.
     */
    public boolean createNewFile(File f) throws IOException
    {
        return f.createNewFile();
    }

    /**
     * Create a new file, optionally creating parent directories.
     *
     * @param f      the file to be created.
     * @param mkdirs <code>boolean</code> whether to create parent directories.
     * @return true if the file did not exist already.
     * @throws IOException on error.
     */
    public boolean createNewFile(File f, boolean mkdirs) throws IOException
    {
        File parent = f.getParentFile();
        if (mkdirs && !(parent.exists()))
        {
            parent.mkdirs();
        }
        return f.createNewFile();
    }

    /**
     * Checks whether a given file is a symbolic link.
     * <p/>
     * <p>It doesn't really test for symbolic links but whether the
     * canonical and absolute paths of the file are identical--this
     * may lead to false positives on some platforms.</p>
     *
     * @param parent the parent directory of the file to test
     * @param name   the name of the file to test.
     * @return true if the file is a symbolic link.
     * @throws IOException on error.
     */
    public boolean isSymbolicLink(File parent, String name)
            throws IOException
    {
        if (parent == null)
        {
            File f = new File(name);
            parent = f.getParentFile();
            name = f.getName();
        }
        File toTest = new File(parent.getCanonicalPath(), name);
        return !toTest.getAbsolutePath().equals(toTest.getCanonicalPath());
    }

    /**
     * Removes a leading path from a second path.
     *
     * @param leading The leading path, must not be null, must be absolute.
     * @param path    The path to remove from, must not be null, must be absolute.
     * @return path's normalized absolute if it doesn't start with
     *         leading; path's path with leading's path removed otherwise.
     */
    public String removeLeadingPath(File leading, File path) throws Exception
    {
        String l = normalize(leading.getAbsolutePath()).getAbsolutePath();
        String p = normalize(path.getAbsolutePath()).getAbsolutePath();
        if (l.equals(p))
        {
            return "";
        }

        // ensure that l ends with a /
        // so we never think /foo was a parent directory of /foobar
        if (!l.endsWith(File.separator))
        {
            l += File.separator;
        }
        return (p.startsWith(l)) ? p.substring(l.length()) : p;
    }

    /**
     * Constructs a <code>file:</code> URI that represents the
     * external form of the given pathname.
     * <p/>
     * <p>Will be an absolute URI if the given path is absolute.</p>
     * <p/>
     * <p>This code doesn't handle non-ASCII characters properly.</p>
     *
     * @param path the path in the local file system.
     * @return the URI version of the local path.
     */
    public String toURI(String path)
    {
        boolean isDir = (new File(path)).isDirectory();

        StringBuffer sb = new StringBuffer("file:");

        // catch exception if normalize thinks this is not an absolute path
        try
        {
            path = normalize(path).getAbsolutePath();
            sb.append("//");
            // add an extra slash for filesystems with drive-specifiers
            if (!path.startsWith(File.separator))
            {
                sb.append("/");
            }
        }
        catch (Exception e)
        {
            // relative path
        }

        path = path.replace('\\', '/');

        CharacterIterator iter = new StringCharacterIterator(path);
        for (char c = iter.first(); c != CharacterIterator.DONE;
             c = iter.next())
        {
            if (c < 256 && isSpecial[c])
            {
                sb.append('%');
                sb.append(escapedChar1[c]);
                sb.append(escapedChar2[c]);
            }
            else
            {
                sb.append(c);
            }
        }
        if (isDir && !path.endsWith("/"))
        {
            sb.append('/');
        }
        return sb.toString();
    }

    /**
     * Compares two filenames.
     * <p/>
     * <p>Unlike java.io.File#equals this method will try to compare
     * the absolute paths and &quot;normalize&quot; the filenames
     * before comparing them.</p>
     *
     * @param f1 the file whose name is to be compared.
     * @param f2 the other file whose name is to be compared.
     * @return true if the file are for the same file.
     */
    public boolean fileNameEquals(File f1, File f2) throws Exception
    {
        return normalize(f1.getAbsolutePath())
                .equals(normalize(f2.getAbsolutePath()));
    }

    /**
     * Renames a file, even if that involves crossing file system boundaries.
     * <p/>
     * <p>This will remove <code>to</code> (if it exists), ensure that
     * <code>to</code>'s parent directory exists and move
     * <code>from</code>, which involves deleting <code>from</code> as
     * well.</p>
     *
     * @param from the file to move.
     * @param to   the new file name.
     * @throws IOException if anything bad happens during this
     *                     process.  Note that <code>to</code> may have been deleted
     *                     already when this happens.
     */
    public void rename(File from, File to) throws IOException
    {
        if (to.exists() && !to.delete())
        {
            throw new IOException("Failed to delete " + to
                    + " while trying to rename " + from);
        }
        File parent = to.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs())
        {
            throw new IOException("Failed to create directory " + parent
                    + " while trying to rename " + from);
        }
        if (!from.renameTo(to))
        {
            copyFile(from, to);
            if (!from.delete())
            {
                throw new IOException("Failed to delete " + from
                        + " while trying to rename it.");
            }
        }
    }

    /**
     * Get the granularity of file timestamps.
     * The choice is made based on OS, which is incorrect--it should really be
     * by filesystem. We do not have an easy way to probe for file systems,
     * however.
     *
     * @return the difference, in milliseconds, which two file timestamps must have
     *         in order for the two files to be given a creation order.
     */
    public long getFileTimestampGranularity()
    {
        return OsVersion.IS_WINDOWS
                ? FAT_FILE_TIMESTAMP_GRANULARITY : UNIX_FILE_TIMESTAMP_GRANULARITY;
    }

    /**
     * Returns true if the source is older than the dest.
     * If the dest file does not exist, then the test returns false; it is
     * implicitly not up do date.
     *
     * @param source      source file (should be the older).
     * @param dest        dest file (should be the newer).
     * @param granularity an offset added to the source time.
     * @return true if the source is older than the dest after accounting
     *         for granularity.
     */
    public boolean isUpToDate(File source, File dest, long granularity)
    {
        //do a check for the destination file existing
        if (!dest.exists())
        {
            //if it does not, then the file is not up to date.
            return false;
        }
        long sourceTime = source.lastModified();
        long destTime = dest.lastModified();
        return isUpToDate(sourceTime, destTime, granularity);
    }


    /**
     * Returns true if the source is older than the dest.
     *
     * @param source source file (should be the older).
     * @param dest   dest file (should be the newer).
     * @return true if the source is older than the dest, taking the granularity into account.
     */
    public boolean isUpToDate(File source, File dest)
    {
        return isUpToDate(source, dest, getFileTimestampGranularity());
    }

    /**
     * Compare two timestamps for being up to date using
     * the specified granularity.
     *
     * @param sourceTime  timestamp of source file.
     * @param destTime    timestamp of dest file.
     * @param granularity os/filesys granularity.
     * @return true if the dest file is considered up to date.
     */
    public boolean isUpToDate(long sourceTime, long destTime, long granularity)
    {
        if (destTime == -1)
        {
            return false;
        }
        return destTime >= sourceTime + granularity;
    }

    /**
     * Compare two timestamps for being up to date using the
     * current granularity.
     *
     * @param sourceTime timestamp of source file.
     * @param destTime   timestamp of dest file.
     * @return true if the dest file is considered up to date.
     */
    public boolean isUpToDate(long sourceTime, long destTime)
    {
        return isUpToDate(sourceTime, destTime, getFileTimestampGranularity());
    }

    /**
     * Unconditionally close a <tt>Closable</tt>.
     * <p/>
     * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param closeable the <tt>Closable</tt>to close. May be <tt>null</tt> or already closed
     */
    public static void close(Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch (IOException ignore)
            {
                // do nothing
            }
        }
    }

    /**
     * Unconditionally close a <tt>ZipFile</tt>.
     * <p/>
     * Equivalent to {@link ZipFile#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param file the <tt>ZipFile</tt>to close. May be <tt>null</tt> or already closed
     */
    public static void close(ZipFile file)
    {
        if (file != null)
        {
            try
            {
                file.close();
            }
            catch (IOException ignore)
            {
                // do nothing
            }
        }
    }

    /**
     * Delete the file with {@link File#delete()} if the argument is not null.
     * Do nothing on a null argument.
     *
     * @param file file to delete.
     */
    public static void delete(File file)
    {
        if (file != null)
        {
            file.delete();
        }
    }

    /**
     * Delete a directory recursively
     *
     * @param fileToDelete
     */
    public static boolean deleteRecursively(File fileToDelete)
    {
        boolean retval = true;
        if (fileToDelete.isDirectory())
        {
            for (File fileInDir : fileToDelete.listFiles())
            {
                retval &= deleteRecursively(fileInDir);
            }
        }
        retval &= fileToDelete.delete();
        return retval;
    }


}


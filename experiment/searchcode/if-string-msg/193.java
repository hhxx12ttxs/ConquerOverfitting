/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.updater;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.io.RawInputStreamFacade;
import org.sonatype.nexus.index.context.DocumentFilter;
import org.sonatype.nexus.index.context.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.NexusAnalyzer;
import org.sonatype.nexus.index.context.NexusIndexWriter;
import org.sonatype.nexus.index.fs.Lock;
import org.sonatype.nexus.index.fs.Locker;
import org.sonatype.nexus.index.incremental.IncrementalHandler;
import org.sonatype.nexus.index.updater.IndexDataReader.IndexDataReadResult;
import org.sonatype.nexus.index.updater.jetty.JettyResourceFetcher;

/**
 * A default index updater implementation
 *
 * @author Jason van Zyl
 * @author Eugene Kuleshov
 */
@Component( role = IndexUpdater.class )
public class DefaultIndexUpdater
    extends AbstractLogEnabled
    implements IndexUpdater
{

    @Requirement( role = IncrementalHandler.class )
    IncrementalHandler incrementalHandler;

    @Requirement( role = IndexUpdateSideEffect.class )
    private List<IndexUpdateSideEffect> sideEffects;

    public DefaultIndexUpdater( final IncrementalHandler handler, final List<IndexUpdateSideEffect> mySideeffects  )
    {
        incrementalHandler = handler;
        sideEffects = mySideeffects;
    }

    public DefaultIndexUpdater(  )
    {
        
    }

    public IndexUpdateResult fetchAndUpdateIndex( final IndexUpdateRequest updateRequest )
        throws IOException
    {
        IndexUpdateResult result = new IndexUpdateResult();
        
        IndexingContext context = updateRequest.getIndexingContext();

        ResourceFetcher fetcher = null;

        if ( !updateRequest.isOffline() )
        {
            fetcher = updateRequest.getResourceFetcher();

            // If no resource fetcher passed in, use the wagon fetcher by default
            // and put back in request for future use
            if( fetcher == null )
            {
                fetcher =
                    new JettyResourceFetcher().addTransferListener( updateRequest.getTransferListener() )
                        .setAuthenticationInfo( updateRequest.getAuthenticationInfo() )
                        .setProxyInfo( updateRequest.getProxyInfo() );
    
                updateRequest.setResourceFetcher( fetcher );
            }
            fetcher.connect( context.getId(), context.getIndexUpdateUrl() );
        }

        File cacheDir = updateRequest.getLocalIndexCacheDir();
        Locker locker = updateRequest.getLocker();
        Lock lock = locker != null && cacheDir != null ? locker.lock( cacheDir ) : null;
        try
        {
            if ( cacheDir != null )
            {
                LocalCacheIndexAdaptor cache = new LocalCacheIndexAdaptor( cacheDir, result );

                if ( !updateRequest.isOffline() )
                {
                    cacheDir.mkdirs();
        
                    try
                    {
                        fetchAndUpdateIndex( updateRequest, fetcher, cache );
                        cache.commit();
                    }
                    finally
                    {
                        fetcher.disconnect();
                    }
                }

                fetcher = cache.getFetcher();
            }
            else if ( updateRequest.isOffline() )
            {
                throw new IllegalArgumentException( "LocalIndexCacheDir can not be null in offline mode" );
            }
    
            try
            {
                if ( !updateRequest.isCacheOnly() )
                {
                    LuceneIndexAdaptor target = new LuceneIndexAdaptor( updateRequest );
                    result.setTimestamp( fetchAndUpdateIndex( updateRequest, fetcher, target ) );
                    target.commit();
                }
            }
            finally
            {
                fetcher.disconnect();
            }
        }
        finally
        {
            if ( lock != null )
            {
                lock.release();
            }
        }
        
        return result;
    }

    /**
     * @deprecated use {@link #fetchAndUpdateIndex(IndexingContext, ResourceFetcher)}
     */
    @Deprecated
    public Date fetchAndUpdateIndex( final IndexingContext context, final TransferListener listener )
        throws IOException
    {
        return fetchAndUpdateIndex( context, listener, null );
    }

    /**
     * @deprecated use {@link #fetchAndUpdateIndex(IndexingContext, ResourceFetcher)}
     */
    @Deprecated
    public Date fetchAndUpdateIndex( final IndexingContext context, final TransferListener listener, final ProxyInfo proxyInfo )
        throws IOException
    {
        IndexUpdateRequest updateRequest = new IndexUpdateRequest( context );

        updateRequest.setResourceFetcher( new JettyResourceFetcher().addTransferListener( listener )
            .setProxyInfo( proxyInfo )
        );

        return fetchAndUpdateIndex( updateRequest ).getTimestamp();
    }

    public Properties fetchIndexProperties( final IndexingContext context, final ResourceFetcher fetcher )
        throws IOException
    {
        fetcher.connect( context.getId(), context.getIndexUpdateUrl() );
        try
        {
            Properties properties = downloadIndexProperties( fetcher );
            storeIndexProperties( context.getIndexDirectoryFile(), properties );
            return properties;
        }
        finally
        {
            fetcher.disconnect();
        }
    }

    /**
     * @deprecated use {@link #fetchIndexProperties(IndexingContext, ResourceFetcher)}
     */
    @Deprecated
    public Properties fetchIndexProperties( final IndexingContext context, final TransferListener listener, final ProxyInfo proxyInfo )
        throws IOException
    {
        return fetchIndexProperties( context, new JettyResourceFetcher().addTransferListener( listener )
            .setProxyInfo( proxyInfo )
        );
    }

    private Date loadIndexDirectory( final IndexUpdateRequest updateRequest, final ResourceFetcher fetcher, final boolean merge, final String remoteIndexFile )
        throws IOException
    {
        File indexDir = File.createTempFile( remoteIndexFile, ".dir" );
        indexDir.delete();
        indexDir.mkdirs();

        FSDirectory directory = FSDirectory.getDirectory( indexDir );

        BufferedInputStream is = null;

        try
        {
            is = new BufferedInputStream( fetcher.retrieve( remoteIndexFile ) );

            Date timestamp = null;

            if( remoteIndexFile.endsWith( ".gz" ) )
            {
                timestamp = DefaultIndexUpdater.unpackIndexData( is, directory, //
                                                                 updateRequest.getIndexingContext()
                );
            }
            else
            {
                // legacy transfer format
                timestamp = unpackIndexArchive( is, directory, //
                                                updateRequest.getIndexingContext()
                );
            }

            if( updateRequest.getDocumentFilter() != null )
            {
                filterDirectory( directory, updateRequest.getDocumentFilter() );
            }

            if( merge )
            {
                updateRequest.getIndexingContext().merge( directory );
            }
            else
            {
                updateRequest.getIndexingContext().replace( directory );
            }
            if( sideEffects != null && sideEffects.size() > 0 )
            {
                getLogger().info( IndexUpdateSideEffect.class.getName() + " extensions found: " + sideEffects.size() );
                for( IndexUpdateSideEffect sideeffect : sideEffects )
                {
                    sideeffect.updateIndex( directory, updateRequest.getIndexingContext(), merge );
                }
            }

            return timestamp;
        }
        finally
        {
            IOUtil.close( is );

            if( directory != null )
            {
                directory.close();
            }

            try
            {
                FileUtils.deleteDirectory( indexDir );
            }
            catch( IOException ex )
            {
                // ignore
            }
        }
    }

    /**
     * Unpack legacy index archive into a specified Lucene <code>Directory</code>
     *
     * @param is        a <code>ZipInputStream</code> with index data
     * @param directory Lucene <code>Directory</code> to unpack index data to
     *
     * @return {@link Date} of the index update or null if it can't be read
     */
    public static Date unpackIndexArchive( final InputStream is, final Directory directory, final IndexingContext context )
        throws IOException
    {
        File indexArchive = File.createTempFile( "nexus-index", "" );

        File indexDir = new File( indexArchive.getAbsoluteFile().getParentFile(), indexArchive.getName() + ".dir" );

        indexDir.mkdirs();

        FSDirectory fdir = FSDirectory.getDirectory( indexDir );

        try
        {
            unpackDirectory( fdir, is );
            copyUpdatedDocuments( fdir, directory, context );

            Date timestamp = IndexUtils.getTimestamp( fdir );
            IndexUtils.updateTimestamp( directory, timestamp );
            return timestamp;
        }
        finally
        {
            IndexUtils.close( fdir );
            indexArchive.delete();
            IndexUtils.delete( indexDir );
        }
    }

    private static void unpackDirectory( final Directory directory, final InputStream is )
        throws IOException
    {
        byte[] buf = new byte[4096];

        ZipEntry entry;

        ZipInputStream zis = null;

        try
        {
            zis = new ZipInputStream( is );

            while( ( entry = zis.getNextEntry() ) != null )
            {
                if( entry.isDirectory() || entry.getName().indexOf( '/' ) > -1 )
                {
                    continue;
                }

                IndexOutput io = directory.createOutput( entry.getName() );
                try
                {
                    int n = 0;

                    while( ( n = zis.read( buf ) ) != -1 )
                    {
                        io.writeBytes( buf, n );
                    }
                }
                finally
                {
                    IndexUtils.close( io );
                }
            }
        }
        finally
        {
            IndexUtils.close( zis );
        }
    }

    private static void copyUpdatedDocuments( final Directory sourcedir, final Directory targetdir, final IndexingContext context )
        throws CorruptIndexException, LockObtainFailedException, IOException
    {
        IndexWriter w = null;
        IndexReader r = null;
        try
        {
            r = IndexReader.open( sourcedir );
            w = new IndexWriter( targetdir, false, new NexusAnalyzer(), true );

            for( int i = 0; i < r.maxDoc(); i++ )
            {
                if( !r.isDeleted( i ) )
                {
                    w.addDocument( IndexUtils.updateDocument( r.document( i ), context ) );
                }
            }

            w.optimize();
            w.commit();
        }
        finally
        {
            IndexUtils.close( w );
            IndexUtils.close( r );
        }
    }

    private static void filterDirectory( final Directory directory, final DocumentFilter filter )
        throws IOException
    {
        IndexReader r = null;
        try
        {
            r = IndexReader.open( directory );

            int numDocs = r.maxDoc();

            for( int i = 0; i < numDocs; i++ )
            {
                if( r.isDeleted( i ) )
                {
                    continue;
                }

                Document d = r.document( i );

                if( !filter.accept( d ) )
                {
                    r.deleteDocument( i );
                }
            }
        }
        finally
        {
            IndexUtils.close( r );
        }

        IndexWriter w = null;
        try
        {
            // analyzer is unimportant, since we are not adding/searching to/on index, only reading/deleting
            w = new IndexWriter( directory, new NexusAnalyzer() );

            w.optimize();

            w.commit();
        }
        finally
        {
            IndexUtils.close( w );
        }
    }

    private Properties loadIndexProperties( final File indexDirectoryFile )
    {
        String remoteIndexProperties = IndexingContext.INDEX_FILE + ".properties";

        File indexProperties = new File( indexDirectoryFile, remoteIndexProperties );

        FileInputStream fis = null;

        try
        {
            Properties properties = new Properties();

            fis = new FileInputStream( indexProperties );

            properties.load( fis );

            return properties;
        }
        catch( IOException e )
        {
            getLogger().debug( "Unable to read remote properties stored locally", e );
        }
        finally
        {
            IOUtil.close( fis );
        }

        return null;
    }

    private Properties downloadIndexProperties( final ResourceFetcher fetcher )
        throws IOException
    {
        InputStream fis = fetcher.retrieve( IndexingContext.INDEX_FILE + ".properties" );
        try
        {
            Properties properties = new Properties();

            properties.load( fis );

            return properties;
        }
        finally
        {
            IOUtil.close( fis );
        }
    }

    private void storeIndexProperties( final File dir, final Properties properties )
        throws IOException
    {
        File file = new File( dir, IndexingContext.INDEX_FILE + ".properties" );
        if ( properties != null )
        {
            OutputStream os = new BufferedOutputStream( new FileOutputStream( file ) );
            try
            {
                properties.store( os, null );
            }
            finally
            {
                IOUtil.close( os );
            }
        }
        else
        {
           file.delete(); 
        }
    }

    public Date getTimestamp( final Properties properties, final String key )
    {
        String indexTimestamp = properties.getProperty( key );

        if( indexTimestamp != null )
        {
            try
            {
                SimpleDateFormat df = new SimpleDateFormat( IndexingContext.INDEX_TIME_FORMAT );
                df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
                return df.parse( indexTimestamp );
            }
            catch( ParseException ex )
            {
            }
        }
        return null;
    }

    /**
     * Unpack index data using specified Lucene Index writer
     *
     * @param is  an input stream to unpack index data from
     * @param w   a writer to save index data
     * @param ics a collection of index creators for updating unpacked documents.
     */
    public static Date unpackIndexData( final InputStream is, final Directory d, final IndexingContext context )
        throws IOException
    {
        NexusIndexWriter w = new NexusIndexWriter( d, new NexusAnalyzer(), true );
        try
        {
            IndexDataReader dr = new IndexDataReader( is );

            IndexDataReadResult result = dr.readIndex( w, context );

            return result.getTimestamp();
        }
        finally
        {
            IndexUtils.close( w );
        }
    }

    /**
     * A ResourceFetcher implementation based on Wagon
     */
//    public static class WagonFetcher
//        extends AbstractResourceFetcher
//        implements ResourceFetcher
//    {
//
//        private final WagonManager wagonManager;
//
//        private final TransferListener listener;
//
//        private final AuthenticationInfo authenticationInfo;
//
//        private final ProxyInfo proxyInfo;
//
//        private Wagon wagon = null;
//
//        public WagonFetcher( final WagonManager wagonManager, final TransferListener listener,
//                             final AuthenticationInfo authenticationInfo, final ProxyInfo proxyInfo )
//        {
//            this.wagonManager = wagonManager;
//            this.listener = listener;
//            this.authenticationInfo = authenticationInfo;
//            this.proxyInfo = proxyInfo;
//        }
//
//        public void connect( final String id, final String url )
//            throws IOException
//        {
//            Repository repository = new Repository( id, url );
//
//            try
//            {
//                wagon = wagonManager.getWagon( repository );
//
//                if( listener != null )
//                {
//                    wagon.addTransferListener( listener );
//                }
//
//                // when working in the context of Maven, the WagonManager is already
//                // populated with proxy information from the Maven environment
//
//                if( authenticationInfo != null )
//                {
//                    if( proxyInfo != null )
//                    {
//                        wagon.connect( repository, authenticationInfo, proxyInfo );
//                    }
//                    else
//                    {
//                        wagon.connect( repository, authenticationInfo );
//                    }
//                }
//                else
//                {
//                    if( proxyInfo != null )
//                    {
//                        wagon.connect( repository, proxyInfo );
//                    }
//                    else
//                    {
//                        wagon.connect( repository );
//                    }
//                }
//            }
//            catch( AuthenticationException ex )
//            {
//                String msg = "Authentication exception connecting to " + repository;
//                logError( msg, ex );
//                throw new IOException( msg );
//            }
//            catch( WagonException ex )
//            {
//                String msg = "Wagon exception connecting to " + repository;
//                logError( msg, ex );
//                throw new IOException( msg );
//            }
//        }
//
//        public void disconnect()
//        {
//            if( wagon != null )
//            {
//                try
//                {
//                    wagon.disconnect();
//                }
//                catch( ConnectionException ex )
//                {
//                    logError( "Failed to close connection", ex );
//                }
//            }
//        }
//
//        public void retrieve( final String name, final File targetFile )
//            throws IOException, FileNotFoundException
//        {
//            try
//            {
//                wagon.get( name, targetFile );
//            }
//            catch( AuthorizationException e )
//            {
//                String msg = "Authorization exception retrieving " + name;
//                logError( msg, e );
//                throw new IOException( msg );
//            }
//            catch( ResourceDoesNotExistException e )
//            {
//                String msg = "Resource " + name + " does not exist";
//                logError( msg, e );
//                throw new FileNotFoundException( msg );
//            }
//            catch( WagonException e )
//            {
//                String msg = "Transfer for " + name + " failed";
//                logError( msg, e );
//                throw new IOException( msg + "; " + e.getMessage() );
//            }
//        }
//
//        private void logError( final String msg, final Exception ex )
//        {
//            if( listener != null )
//            {
//                listener.debug( msg + "; " + ex.getMessage() );
//            }
//        }
//    }

    /**
     * Filesystem-based ResourceFetcher implementation
     */
    public static class FileFetcher
        implements ResourceFetcher
    {

        private final File basedir;

        public FileFetcher( File basedir )
        {
            this.basedir = basedir;
        }

        public void connect( String id, String url )
            throws IOException
        {
            // don't need to do anything
        }

        public void disconnect()
            throws IOException
        {
            // don't need to do anything
        }

        public void retrieve( String name, File targetFile )
            throws IOException, FileNotFoundException
        {
            FileUtils.copyFile( getFile( name ), targetFile );

        }

        public InputStream retrieve( String name )
            throws IOException, FileNotFoundException
        {
            return new FileInputStream( getFile( name ) );
        }

        private File getFile( String name )
        {
            return new File( basedir, name );
        }

    }


    private abstract class IndexAdaptor
    {
        protected final File dir;

        private Properties properties;
        
        protected IndexAdaptor( File dir )
        {
            this.dir = dir;
        }

        public Properties getProperties()
        {
            if ( properties == null )
            {
                properties = loadIndexProperties( dir );
            }
            return properties;
        }

        public abstract void addIndexChunk( ResourceFetcher source, String filename )
            throws IOException;

        public abstract Date setIndexFile( ResourceFetcher source, String string ) throws IOException;

        public Properties setProperties( ResourceFetcher source ) throws IOException
        {
            this.properties = downloadIndexProperties( source );
            return properties;
        }

        public abstract Date getTimestamp();

        public void commit()
            throws IOException
        {
            storeIndexProperties( dir, properties );
        }
    }

    private class LuceneIndexAdaptor extends IndexAdaptor
    {
        private final IndexUpdateRequest updateRequest;

        public LuceneIndexAdaptor( IndexUpdateRequest updateRequest )
        {
            super( updateRequest.getIndexingContext().getIndexDirectoryFile() );
            this.updateRequest = updateRequest;
        }

        public Date getTimestamp()
        {
            return updateRequest.getIndexingContext().getTimestamp();
        }

        public void addIndexChunk(ResourceFetcher source, String filename ) throws IOException
        {
            loadIndexDirectory( updateRequest, source, true, filename );
        }

        public Date setIndexFile( ResourceFetcher source, String filename ) throws IOException
        {
            return loadIndexDirectory( updateRequest, source, false, filename );
        }
    }

    private class LocalCacheIndexAdaptor extends IndexAdaptor
    {
        private static final String CHUNKS_FILENAME = "chunks.lst";
        private static final String CHUNKS_FILE_ENCODING = "UTF-8";
        
        private final IndexUpdateResult result;
        
        private final ArrayList<String> newChunks = new ArrayList<String>();

        public LocalCacheIndexAdaptor( File dir, IndexUpdateResult result )
        {
            super( dir );
            this.result = result;
        }

        public Date getTimestamp()
        {
            Properties properties = getProperties();
            if ( properties == null )
            {
                return null;
            }

            Date timestamp = DefaultIndexUpdater.this.getTimestamp( properties, IndexingContext.INDEX_TIMESTAMP );

            if ( timestamp == null )
            {
                timestamp = DefaultIndexUpdater.this.getTimestamp( properties, IndexingContext.INDEX_LEGACY_TIMESTAMP );
            }

            return timestamp;
        }

        public void addIndexChunk( ResourceFetcher source, String filename )
            throws IOException
        {
            File chunk = new File( dir, filename );
            FileUtils.copyStreamToFile( new RawInputStreamFacade( source.retrieve( filename ) ), chunk );
            newChunks.add( filename );
        }

        public Date setIndexFile( ResourceFetcher source, String filename )
            throws IOException
        {
            cleanCacheDirectory( dir );

            result.setFullUpdate( true );

            File target = new File( dir, filename );
            FileUtils.copyStreamToFile( new RawInputStreamFacade( source.retrieve( filename ) ), target );

            return null;
        }

        @Override
        public void commit()
            throws IOException
        {
            File chunksFile = new File( dir, CHUNKS_FILENAME );
            BufferedOutputStream os = new BufferedOutputStream( new FileOutputStream( chunksFile, true ) );
            try
            {
                Writer w = new OutputStreamWriter( os, CHUNKS_FILE_ENCODING );
                for ( String filename : newChunks )
                {
                    w.write( filename + "\n" );
                }
                w.flush();
            }
            finally
            {
                IOUtil.close( os );
            }
            super.commit();
        }
        
        public List<String> getChunks()
            throws IOException
        {
            ArrayList<String> chunks = new ArrayList<String>();
            
            File chunksFile = new File( dir, CHUNKS_FILENAME );
            BufferedReader r =
                new BufferedReader( new InputStreamReader( new FileInputStream( chunksFile ), CHUNKS_FILE_ENCODING ) );
            try
            {
                String str;
                while ( (str = r.readLine()) != null )
                {
                    chunks.add( str );
                }
            }
            finally
            {
                IOUtil.close( r );
            }
            return chunks;
        }

        public ResourceFetcher getFetcher()
        {
            return new LocalIndexCacheFetcher( dir )
            {
                @Override
                public List<String> getChunks() throws IOException
                {
                    return LocalCacheIndexAdaptor.this.getChunks();
                }
            };
        }
    }

    static abstract class LocalIndexCacheFetcher
        extends FileFetcher
    {
        public LocalIndexCacheFetcher( File basedir )
        {
            super( basedir );
        }

        public abstract List<String> getChunks()
            throws IOException;
    }

    private Date fetchAndUpdateIndex( final IndexUpdateRequest updateRequest, ResourceFetcher source, IndexAdaptor target )
        throws IOException
    {
        Date targetTimestamp = target.getTimestamp();

        if( !updateRequest.isForceFullUpdate() )
        {
            Properties localProperties = target.getProperties();

            // this will download and store properties in the target, so next run 
            // target.getProperties() will retrieve it
            Properties remoteProperties = target.setProperties( source );

            Date updateTimestamp = getTimestamp( remoteProperties, IndexingContext.INDEX_TIMESTAMP );

            // If new timestamp is missing, dont bother checking incremental, we have an old file
            if( updateTimestamp != null )
            {
                List<String> filenames =
                    incrementalHandler.loadRemoteIncrementalUpdates( updateRequest, localProperties, remoteProperties );

                // if we have some incremental files, merge them in
                if( filenames != null )
                {
                    for( String filename : filenames )
                    {
                        target.addIndexChunk( source, filename );
                    }

                    return updateTimestamp;
                }
            }
            else
            {
                updateTimestamp = getTimestamp( remoteProperties, IndexingContext.INDEX_LEGACY_TIMESTAMP );
            }

            // if incremental cant be done for whatever reason, simply use old logic of
            // checking the timestamp, if the same, nothing to do
            if( updateTimestamp != null && targetTimestamp != null && !updateTimestamp.after( targetTimestamp ) )
            {
                return null; // index is up to date
            }
        }
        else
        {
            // create index properties during forced full index download
            target.setProperties( source );            
        }

        try
        {
            Date timestamp = target.setIndexFile( source, IndexingContext.INDEX_FILE + ".gz" );
            if ( source instanceof LocalIndexCacheFetcher )
            {
                // local cache has inverse organization compared to remote indexes,
                // i.e. initial index file and delta chunks to apply on top of it
                for ( String filename : ((LocalIndexCacheFetcher) source).getChunks() )
                {
                    target.addIndexChunk( source, filename );
                }
            }
            return timestamp;
        }
        catch( FileNotFoundException ex )
        {
            // try to look for legacy index transfer format
            return target.setIndexFile( source, IndexingContext.INDEX_FILE + ".zip" );
        }
    }

    /**
     * Cleans specified cache directory. If present, Locker.LOCK_FILE will not
     * be deleted.
     */
    protected void cleanCacheDirectory( File dir )
        throws IOException
    {
        File[] members = dir.listFiles();
        if ( members == null )
        {
            return;
        }

        for ( File member : members )
        {
            if ( !Locker.LOCK_FILE.equals( member.getName() ) )
            {
                FileUtils.forceDelete( member );
            }
        }
    }

}


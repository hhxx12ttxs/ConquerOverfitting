/*
 * $Id$
 *
 * Copyright (c) 2013 Runal House.
 * All Rights Reserved.
 */
package net.runal.jtool.preferences;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.runal.jtool.net.Credentials;
import net.runal.jtool.net.Request;
import net.runal.jtool.net.Response;
import net.runal.jtool.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author yasuo
 */
public class KernelConfig {

    private static void log(Level level, String pattern, Object... args) {
        Logger.getLogger(KernelConfig.class.getName()).log(level, pattern, args);
    }
    private static Exception log(Level level, String msg, Throwable thrown) {
        Logger.getLogger(KernelConfig.class.getName()).log(level, msg, thrown);
        if (thrown instanceof Error) {
            throw (Error) thrown;
        } else {
            return (Exception) thrown;
        }
    }

////////////////////////////////////////////////////////////////////////////////

    public static void initProxy() {
        // PROXY設定
        try {
            String httpProxyHost = KernelConfig.getProperty("http.proxyHost");
            String httpProxyPort = KernelConfig.getProperty("http.proxyPort");
            String httpsProxyHost = KernelConfig.getProperty("https.proxyHost");
            String httpsProxyPort = KernelConfig.getProperty("https.proxyPort");
            String httpNonProxyHosts = KernelConfig.getProperty("http.nonProxyHosts");

            String ftpProxyHost = KernelConfig.getProperty("ftp.proxyHost"); 
            String ftpProxyPort = KernelConfig.getProperty("ftp.proxyPort");
            String ftpNonProxyHosts = KernelConfig.getProperty("ftp.nonProxyHosts");

            String socksProxyHost = KernelConfig.getProperty("socksProxyHost"); 
            String socksProxyPort = KernelConfig.getProperty("socksProxyPort"); 

            boolean http = httpProxyHost != null;
            http |= httpProxyPort != null;
            http |= httpsProxyHost != null;
            http |= httpsProxyPort != null;
            http |= httpNonProxyHosts != null;

            boolean ftp = ftpProxyHost != null;
            ftp |= ftpProxyPort != null;
            ftp |= ftpNonProxyHosts != null;

            boolean socks = socksProxyHost != null; 
            socks |= socksProxyPort != null; 

            if(http || ftp || socks) {
                log(Level.INFO, "init proxy.");
                if(http) {
                    if(httpProxyHost == null) {
                        System.clearProperty("http.proxyHost");
                    } else {
                        System.setProperty("http.proxyHost", httpProxyHost);
                    }
                    if(httpProxyPort == null) {
                        System.clearProperty("http.proxyPort");
                    } else {
                        System.setProperty("http.proxyPort", httpProxyPort);
                    }
                    if(httpsProxyHost == null) {
                        System.clearProperty("https.proxyHost");
                    } else {
                        System.setProperty("https.proxyHost", httpsProxyHost);
                    }
                    if(httpsProxyPort == null) {
                        System.clearProperty("https.proxyPort");
                    } else {
                        System.setProperty("https.proxyPort", httpsProxyPort);
                    }
                    if(httpNonProxyHosts == null) {
                        System.clearProperty("http.nonProxyHosts");
                    } else {
                        System.setProperty("http.nonProxyHosts", httpNonProxyHosts);
                    }
                } else {
                    System.clearProperty("http.proxyHost");
                    System.clearProperty("http.proxyPort");
                    System.clearProperty("https.proxyHost");
                    System.clearProperty("https.proxyPort");
                    System.clearProperty("http.nonProxyHosts");
                }
                if(ftp) {
                    if(ftpProxyHost == null) {
                        System.clearProperty("ftp.proxyHost");
                    } else {
                        System.setProperty("ftp.proxyHost", ftpProxyHost);
                    }
                    if(ftpProxyPort == null) {
                        System.clearProperty("ftp.proxyPort");
                    } else {
                        System.setProperty("ftp.proxyPort", ftpProxyPort);
                    }
                    if(ftpNonProxyHosts == null) {
                        System.clearProperty("ftp.nonProxyHosts");
                    } else {
                        System.setProperty("ftp.nonProxyHosts", ftpNonProxyHosts);
                    }
                } else {
                    System.clearProperty("ftp.proxyHost");
                    System.clearProperty("ftp.proxyPort");
                    System.clearProperty("ftp.nonProxyHosts");
                }
                if(socks) {
                    if(socksProxyHost == null) {
                        System.clearProperty("socksProxyHost");
                    } else {
                        System.setProperty("socksProxyHost", socksProxyHost);
                    }
                    if(socksProxyPort == null) {
                        System.clearProperty("socksProxyPort");
                    } else {
                        System.setProperty("socksProxyPort", socksProxyPort);
                    }
                } else {
                    System.clearProperty("socksProxyHost");
                    System.clearProperty("socksProxyPort");
                }
            }
        } catch(SecurityException ex) {
            log(Level.WARNING, null, ex);
        }
    }

////////////////////////////////////////////////////////////////////////////////

    public static final String CONNECTION_STRING_SUFFIX = ".ConnectionString";
    public static final String CONNECTION_STRING_KEY = "net.runal.jtool.Config" +CONNECTION_STRING_SUFFIX;
    public static final String ETC_CONTAINER_NAME = "etc";
    public static final String PROPERTIES_FILE_NAME  = ".properties";
    public static final String CONFIG_FILE_NAME = "config" +PROPERTIES_FILE_NAME;

    static final KernelConfig config;
    static {
        config = new KernelConfig();
        synchronized(config) {
            // Application properties
            try {
                URL url = config.getClass().getResource("/META-INF" +"/" +CONFIG_FILE_NAME);
                log(Level.CONFIG, "config: {0}", url);
                if(url != null) {
                    InputStream in = null;
                    try {
                        in = url.openStream();
                        config.local.load(in);
                    } catch(IOException ex) {
                        log(Level.SEVERE, null, ex);
                    } finally {
                        if(in != null) {
                            in.close();
                        }
                    }
                }
            } catch (IOException ex) {
                log(Level.SEVERE, null, ex);
            }
            // User properties
            try {
                String home = System.getProperty("user.home");
                File file = new File(home, PROPERTIES_FILE_NAME);
                if (file.canRead()) {
                    FileInputStream is = null;
                    try {
                        is = new FileInputStream(file);
                        config.local.load(is);
                    } catch (IOException ex) {
                        log(Level.SEVERE, null, ex);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }
            } catch (IOException ex) {
                log(Level.SEVERE, null, ex);
            } catch(SecurityException ex) {
                log(Level.WARNING, null, ex);
            }
            //
            initProxy();
        }
    }

    private final Map<String,UserConfig> map;
    private final CloudConfig cloud;
    private final LocalConfig local;
    KernelConfig() {
        map = new TreeMap<String,UserConfig>();
        local = new LocalConfig();
        cloud = new CloudConfig();
    } 

    static Properties getProperties() {
        synchronized(config) {
            Properties prop = new Properties(config.local.getProperties());
            prop.putAll(config.cloud.getProperties());
            return prop;
        }
    }

    static void init(Properties p) {
        synchronized(config) {
            for(String name :p.stringPropertyNames()) {
                String value = p.getProperty(name);
                if(!config.local.contents(name, value)) {
                    config.cloud.setProperty(name, value);
                }
            }
        }
    }

    public static String getProperty(String name) {
        synchronized(config) {
            String value = config.cloud.getProperty(name);
            if(value == null) {
                value = config.local.getProperty(name);
            }
            return value;
        }
    }

    public static String getProperty(String name, String defaultValue) {
        String value = KernelConfig.getProperty(name);
        if(value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public static void setProperty(String name, String value) {
        synchronized(config) {
            config.cloud.setProperty(name, value);
        }
    }

    public static boolean getBoolean(String key) {
        String value = KernelConfig.getProperty(key);
        return Boolean.valueOf(value);
    }

////////////////////////////////////////////////////////////////////////////////

    public static Map<String,UserConfig> getInstances() {
        synchronized(config) {
            return Collections.unmodifiableMap(config.map);
        }
    }

    public static UserConfig getInstance(String userid) {
        synchronized(config) {
            if(config.map.containsKey(userid)) {
                return config.map.get(userid);
            } else {
                UserConfig conf = new UserConfig(userid);
                config.map.put(conf.getId(), conf);
                return conf;
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////

//    /**
//     * Contsiner for user
//     * 
//     * @param userid
//     * @return
//     * @throws URISyntaxException
//     * @throws InvalidKeyException
//     * @throws StorageException 
//     */
//    private static CloudBlobContainer getContainer(String userid) throws URISyntaxException, InvalidKeyException, StorageException {
//        final String local = config.local.getProperty(CONNECTION_STRING_KEY);
//        final String cloud = config.cloud.getProperty(CONNECTION_STRING_KEY);
//        final String connectionString;
//        if(local == null) {
//            if(cloud == null) {
//                return null;
//            } else {
//                connectionString = cloud;
//            }
//        } else {
//            if(cloud == null) {
//                connectionString = local;
//            } else {
//                if(local.equals(cloud)) {
//                    connectionString = cloud;
//                } else {
//                    connectionString = cloud;
//                }
//            }
//        }
//
//        CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
//
//        CloudBlobClient client = storageAccount.createCloudBlobClient();
//        CloudBlobContainer container = client.getContainerReference(userid);
//
//        return container;
//    }
//
//    private static CloudBlob getBlob(UserConfig user) throws URISyntaxException, InvalidKeyException, StorageException {
//        CloudBlobContainer container = getContainer(user.getId());
//        CloudBlob blob = container.getBlockBlobReference(PROPERTIES_FILE_NAME);
//        return blob;
//    }

//    static void download(UserConfig user) {
//        try {
//            CloudBlob blob = getBlob(user);
//
//            long length = blob.getProperties().getLength();
//            final ByteArrayOutputStream out = new ByteArrayOutputStream((int)length);
//            blob.download(out);
//            out.close();
//
//            final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
//            Properties prop = new Properties();
//            prop.load(in);
//            in.close();
//            user.init(prop);
//        } catch (Exception ex) {
//            log(Level.SEVERE, null, ex);
//        }
//    }

//    static void upload(UserConfig user) {
//        try {
//            CloudBlob blob = getBlob(user);
//
//            StringBuilder comments = new StringBuilder();
//            comments.append(user.getClass().getName());
//            comments.append(" ");
//
//            comments.append(user.getId());
//
//            comments.append(" ");
//            comments.append(PROPERTIES_FILE_NAME);
//
//            final ByteArrayOutputStream out = new ByteArrayOutputStream();
//            Properties prop = user.getProperties();
//            prop.store(out, comments.toString());
//            out.close();
//
//            final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
//            blob.upload(in, -1);
//            in.close();
//        } catch (Exception ex) {
//            log(Level.SEVERE, null, ex);
//        }
//    }

//    private static CloudBlob getBlob() throws StorageException, URISyntaxException, InvalidKeyException {
//        CloudBlobContainer container = getContainer(ETC_CONTAINER_NAME);
//        CloudBlob blob = container.getBlockBlobReference(CONFIG_FILE_NAME);
//        return blob;
//    }
//
//    public static void download() {
//        synchronized(config) {
//            try {
//                CloudBlob blob = getBlob();
//                config.cloud.download(blob);
//            } catch (Exception ex) {
//                log(Level.SEVERE, null, ex);
//            }
//        }
//    }
//
//    private static String comments() {
//        StringBuilder sb = new StringBuilder();
//        sb.append(config.getClass().getName());
//        sb.append(" ");
//
//        String username;
//        try {
//            username = System.getProperty("user.name");
//        } catch(SecurityException ex) {
//            username = "unknown";
//        }
//        log(Level.CONFIG, "username: {0}", username);
//        sb.append(username);
//
//        sb.append(" ");
//        sb.append(PROPERTIES_FILE_NAME);
//
//        return sb.toString();
//    }
//
//    public static void upload() {
//        synchronized(config) {
//            try {
//                CloudBlob blob = getBlob();
//                config.cloud.upload(blob, comments());
//            } catch (Exception ex) {
//                log(Level.SEVERE, null, ex);
//            }
//        }
//    }

////////////////////////////////////////////////////////////////////////////////

    public static Map<String,String> parse(String s) {
        Map<String,String> map = new LinkedHashMap<String, String>();

        if(s != null) {
            Pattern p = Pattern.compile("([^=]+)=(.+)");
            String[] part = s.split(";");
            for(int i = 0; i < part.length; i++) {
                Matcher m = p.matcher(part[i]);
                if(m.matches()) {
                    String key = m.group(1);
                    String value = m.group(2);
                    map.put(key, value);
                } else {
                    log(Level.WARNING, "invalid. part[{0,number,0}/{1,number,0}]", i +1, part.length);
                }
            }
        }

        return map;
    }

    public static String getConnectionString(final String prefix) {
        String key = prefix + CONNECTION_STRING_SUFFIX;
        String connectionString = getProperty(key);
        log(Level.CONFIG, "{0}={1}", key, connectionString);
        if (connectionString == null) {
            connectionString = getProperty(CONNECTION_STRING_SUFFIX);
            log(Level.CONFIG, "{0}={1}", CONNECTION_STRING_SUFFIX, connectionString);
        }
        return connectionString;
    }

    static class BlobProperties {

        private URI uri;
        private long lastModified;
        private int contentLength;

        private BlobProperties() {
        }

        public Date getLastModified() {
            return new Date(lastModified);
        }

        public long getLength() {
            return contentLength;
        }

    }
    static class Blob {

        private final Response response;
        private final BlobProperties properties;
        Blob(Response aResponse) {
            response = aResponse;
            properties = new BlobProperties();
            properties.lastModified = response.getLastModified();
            properties.contentLength = response.getContentLength();
            try {
                properties.uri = response.getURL().toURI();
            } catch (URISyntaxException ex) {
                Logger.getLogger(KernelConfig.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public URI getUri() {
            return properties.uri;
        }

        public BlobProperties getProperties() {
            return properties;
        }

        public void download(OutputStream out) {
            try {
                Request request = response.newRequest();
                request.setRequestMethod("GET");
                request.openConnection();
                Response newResponse = request.request();
                if(newResponse.getResponseCode() == 200) {
                    InputStream in = new FileInputStream(newResponse.getFile());
                    byte[] buf = new byte[65535];
                    for(int read = in.read(buf); read != -1; read = in.read(buf)) {
                        out.write(buf, 0, read);
                    }
                    in.close();
                } else {
                    
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(KernelConfig.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(KernelConfig.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private String toBlockid(long block) throws IOException {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(8);
            DataOutputStream out = new DataOutputStream(bout);
            //数値を出力する
            out.writeLong(block);
            // BASE64エンコード
            byte[] bytes = bout.toByteArray();
            return Base64.encode(bytes);
        }
        public void upload(InputStream in, int length) {
            try {
                //
                final String method = "PUT";
                final String contentType = null;

                //                        M       k       bytes
                final long BLOCK_SIZE = (4L * 1024L * 1024L);
                Map<String, Response> map = new LinkedHashMap<String, Response>();
                Request req = response.newRequest();

                //
                long blocks = 0;
                do {
                    long blockLength = Math.min(BLOCK_SIZE, length);
                    String blockid = toBlockid(++blocks);

                    //
                    req.setRequestProperty("x-ms-blob-type", "BlockBlob");
                    req.setQuery("timeout", "90");
                    req.setQuery("comp", "block");
                    req.setQuery("blockid", blockid);

                    // 
                    req.setRequestMethod(method);
                    req.openConnection();
                    final Response res;
                    res = req.request(in, contentType, blockLength);
                    switch(res.getResponseCode()) {
                        case 201:
                            map.put(blockid, res);
                            // 
                            req = res.newRequest();
                            break;
                        default:
                            // 異常応答処理必要
                            throw new IOException("Failed. " +res);
                    }

                    //
                    length -= blockLength;
                } while(0 < length);

                // ブロックIDリスト送信
                ByteArrayInputStream content = null;
                try {
                    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    final DocumentBuilder builder = factory.newDocumentBuilder();
                    final Document doc = builder.newDocument();
                    final Element root = doc.createElement("BlockList");
                    doc.appendChild(root);

                    //
                    for(Map.Entry<String,Response> entry :map.entrySet()) {
                        final Element block = doc.createElement("Latest");
                        block.setTextContent(entry.getKey());
                        root.appendChild(block);
                    }

                    //
                    TransformerFactory tfactory = TransformerFactory.newInstance();
                    Transformer transformer = tfactory.newTransformer();

                    //
                    ByteArrayOutputStream buf = new ByteArrayOutputStream();
                    transformer.transform(new DOMSource(doc), new StreamResult(buf));

                    //
                    content = new ByteArrayInputStream(buf.toByteArray());
                } catch (TransformerException ex) {
                    log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    log(Level.SEVERE, null, ex);
                }

                if(content != null) {
                    //
                    req.setQuery("timeout", "90");
                    req.setQuery("comp", "blocklist");

                    //
                    req.setRequestMethod(method);
                    req.openConnection();
                    final Response res;
                    res = req.request(content, "application/xml", content.available());
                    switch(res.getResponseCode()) {
                        case 201:
                            break;
                        default:
                            // 後処理必要
                            throw new IOException("Failed. " +res);
                    }

                    //
                    content.close();
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(KernelConfig.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(KernelConfig.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    static Blob getBlob(UserConfig user) throws IOException {
        final String local = config.local.getProperty(CONNECTION_STRING_KEY);
        final String cloud = config.cloud.getProperty(CONNECTION_STRING_KEY);
        final String connectionString;
        if(local == null) {
            if(cloud == null) {
                return null;
            } else {
                connectionString = cloud;
            }
        } else {
            if(cloud == null) {
                connectionString = local;
            } else {
                if(local.equals(cloud)) {
                    connectionString = cloud;
                } else {
                    connectionString = cloud;
                }
            }
        }
        Map<String,String> map = parse(connectionString);
        String defaultEndpointsProtocol = map.get("DefaultEndpointsProtocol");
        String blobEndpoint = map.get("BlobEndpoint");
        String accountName = map.get("AccountName");
        String accountKey = map.get("AccountKey");
        
        
        if(accountName == null) {
            throw new RuntimeException();
        }
        if(accountKey == null) {
            throw new RuntimeException();
        }
        Credentials credentials = new Credentials(accountName, accountKey);
        String endpoint;
        if(defaultEndpointsProtocol == null) {
            if(blobEndpoint == null) {
                throw new RuntimeException();
            } else {
                endpoint = blobEndpoint;
            }
        } else {
            if(blobEndpoint == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(defaultEndpointsProtocol);
                sb.append("://");
                sb.append(accountName);
                sb.append(".blob.core.windows.net/");
                endpoint = sb.toString();
            } else {
                throw new RuntimeException();
            }
        }
        Request request = new Request(endpoint, user.getId(), PROPERTIES_FILE_NAME);
        request.setRequestMethod("HEAD");
        request.setCredentials(credentials);
        request.openConnection();
        Response response = request.request();
        if(response.getResponseCode() == 200) {
            return new Blob(response);
        } else {
            throw new IOException();            
        }
    }

    static void download(UserConfig user) {
        try {
            Blob blob = getBlob(user);

            long length = blob.getProperties().getLength();
            final ByteArrayOutputStream out = new ByteArrayOutputStream((int)length);
            blob.download(out);
            out.close();

            final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            Properties prop = new Properties();
            prop.load(in);
            in.close();
            user.init(prop);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    static void upload(UserConfig user) {
        try {
            Blob blob = getBlob(user);

            StringBuilder comments = new StringBuilder();
            comments.append(user.getClass().getName());
            comments.append(" ");

            comments.append(user.getId());

            comments.append(" ");
            comments.append(PROPERTIES_FILE_NAME);

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            Properties prop = user.getProperties();
            prop.store(out, comments.toString());
            out.close();

            final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            blob.upload(in, in.available());
            in.close();
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }
    }

////////////////////////////////////////////////////////////////////////////////
}


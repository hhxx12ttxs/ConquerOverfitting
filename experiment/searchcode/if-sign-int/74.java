/*
 * JetS3t : Java S3 Toolkit
 * Project hosted at http://bitbucket.org/jmurty/jets3t/
 *
 * Copyright 2008-2011 James Murty, 2008 Zmanda Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jets3t.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.CredentialsProvider;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.GrantAndPermission;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.acl.Permission;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.impl.rest.httpclient.RestStorageService;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;
import org.jets3t.service.model.MultipartCompleted;
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.MultipleDeleteResult;
import org.jets3t.service.model.NotificationConfig;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3BucketLoggingStatus;
import org.jets3t.service.model.S3BucketVersioningStatus;
import org.jets3t.service.model.S3DeleteMarker;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.S3Version;
import org.jets3t.service.model.StorageBucket;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.model.WebsiteConfig;
import org.jets3t.service.model.container.ObjectKeyAndVersion;
import org.jets3t.service.mx.MxDelegate;
import org.jets3t.service.security.AWSDevPayCredentials;
import org.jets3t.service.security.ProviderCredentials;
import org.jets3t.service.utils.MultipartUtils;
import org.jets3t.service.utils.RestUtils;
import org.jets3t.service.utils.ServiceUtils;
import org.jets3t.service.utils.signedurl.SignedUrlHandler;

/**
 * A service that handles communication with S3, offering all the operations that can be performed
 * on S3 accounts.
 * <p>
 * This class must be extended by implementation classes that perform the communication with S3 via
 * a particular interface, such as REST or SOAP. The JetS3t suite includes a REST implementation
 * in {@link org.jets3t.service.impl.rest.httpclient.RestS3Service}.
 * </p>
 * <p>
 * Implementations of <code>S3Service</code> must be thread-safe as they will probably be used by
 * the multi-threaded service class {@link org.jets3t.service.multithread.S3ServiceMulti}.
 * </p>
 * <p>
 * This class uses properties obtained through {@link Jets3tProperties}. For more information on
 * these properties please refer to
 * <a href="http://www.jets3t.org/toolkit/configuration.html">JetS3t Configuration</a>
 * </p>
 *
 * @author James Murty
 * @author Nikolas Coukouma
 */
public abstract class S3Service extends RestStorageService implements SignedUrlHandler {

    private static final Log log = LogFactory.getLog(S3Service.class);

    protected S3Service(ProviderCredentials credentials, String invokingApplicationDescription,
        CredentialsProvider credentialsProvider, Jets3tProperties jets3tProperties)
    {
        super(credentials, invokingApplicationDescription, credentialsProvider,
            jets3tProperties);
    }

    /**
     * Construct an <code>S3Service</code> identified by the given user credentials.
     *
     * @param credentials
     * the S3 user credentials to use when communicating with S3, may be null in which case the
     * communication is done as an anonymous user.
     * @param invokingApplicationDescription
     * a short description of the application using the service, suitable for inclusion in a
     * user agent string for REST/HTTP requests. Ideally this would include the application's
     * version number, for example: <code>Cockpit/0.7.3</code> or <code>My App Name/1.0</code>
     * @param jets3tProperties
     * JetS3t properties that will be applied within this service.
     */
    protected S3Service(ProviderCredentials credentials, String invokingApplicationDescription,
        Jets3tProperties jets3tProperties)
    {
        super(credentials, invokingApplicationDescription, null, jets3tProperties);
    }

    /**
     * Construct an <code>S3Service</code> identified by the given user credentials.
     *
     * @param credentials
     * the S3 user credentials to use when communicating with S3, may be null in which case the
     * communication is done as an anonymous user.
     * @param invokingApplicationDescription
     * a short description of the application using the service, suitable for inclusion in a
     * user agent string for REST/HTTP requests. Ideally this would include the application's
     * version number, for example: <code>Cockpit/0.7.3</code> or <code>My App Name/1.0</code>
     */
    protected S3Service(ProviderCredentials credentials, String invokingApplicationDescription)
    {
        this(credentials, invokingApplicationDescription,
            Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME));
    }

    /**
     * Construct an <code>S3Service</code> identified by the given user credentials.
     *
     * @param credentials
     * the S3 user credentials to use when communicating with S3, may be null in which case the
     * communication is done as an anonymous user.
     */
    protected S3Service(ProviderCredentials credentials) {
        this(credentials, null);
    }

    /**
     * @return the credentials identifying the service user, or null for anonymous.
     * @deprecated 0.8.0 use {@link #getProviderCredentials()} instead
     */
    @Deprecated
    public ProviderCredentials getAWSCredentials() {
        return credentials;
    }

    /**
     * Returns the URL representing an object in S3 without a signature. This URL
     * can only be used to download publicly-accessible objects.
     *
     * @param bucketName
     * the name of the bucket that contains the object.
     * @param objectKey
     * the key name of the object.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     * @param isHttps
     * if true, the signed URL will use the HTTPS protocol. If false, the signed URL will
     * use the HTTP protocol.
     * @param isDnsBucketNamingDisabled
     * if true, the signed URL will not use the DNS-name format for buckets eg.
     * <tt>jets3t.s3.amazonaws.com</tt>. Unless you have a specific reason to disable
     * DNS bucket naming, leave this value false.
     *
     * @return
     * the object's URL.
     *
     * @throws S3ServiceException
     */
    public String createUnsignedObjectUrl(String bucketName, String objectKey,
        boolean isVirtualHost, boolean isHttps, boolean isDnsBucketNamingDisabled)
        throws S3ServiceException
    {
        // Create a signed GET URL then strip away the signature query components.
        String signedGETUrl = createSignedUrl("GET", bucketName, objectKey,
            null, null, 0, isVirtualHost, isHttps, isDnsBucketNamingDisabled);
        return signedGETUrl.split("\\?")[0];
    }

    /**
     * Generates a signed URL string that will grant access to an S3 resource (bucket or object)
     * to whoever uses the URL up until the time specified.
     *
     * @param method
     * the HTTP method to sign, such as GET or PUT (note that S3 does not support POST requests).
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param specialParamName
     * the name of a request parameter to add to the URL generated by this method. 'Special'
     * parameters may include parameters that specify the kind of S3 resource that the URL
     * will refer to, such as 'acl', 'torrent', 'logging', or 'location'.
     * @param headersMap
     * headers to add to the signed URL, may be null.
     * Headers that <b>must</b> match between the signed URL and the actual request include:
     * content-md5, content-type, and any header starting with 'x-amz-'.
     * @param secondsSinceEpoch
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *  <b>Note:</b> This time is specified in seconds since the epoch, not milliseconds.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     * @param isHttps
     * if true, the signed URL will use the HTTPS protocol. If false, the signed URL will
     * use the HTTP protocol.
     * @param isDnsBucketNamingDisabled
     * if true, the signed URL will not use the DNS-name format for buckets eg.
     * <tt>jets3t.s3.amazonaws.com</tt>. Unless you have a specific reason to disable
     * DNS bucket naming, leave this value false.
     *
     * @return
     * a URL signed in such a way as to grant access to an S3 resource to whoever uses it.
     *
     * @throws S3ServiceException
     */
    public String createSignedUrl(String method, String bucketName, String objectKey,
        String specialParamName, Map<String, Object> headersMap, long secondsSinceEpoch,
        boolean isVirtualHost, boolean isHttps, boolean isDnsBucketNamingDisabled)
        throws S3ServiceException
    {
        try {
            String s3Endpoint = this.getEndpoint();
            String uriPath = "";

            String hostname = (isVirtualHost
                ? bucketName
                : ServiceUtils.generateS3HostnameForBucket(
                    bucketName, isDnsBucketNamingDisabled, s3Endpoint));

            if (headersMap == null) {
                headersMap = new HashMap<String, Object>();
            }

            // If we are using an alternative hostname, include the hostname/bucketname in the resource path.
            String virtualBucketPath = "";
            if (!s3Endpoint.equals(hostname)) {
                int subdomainOffset = hostname.lastIndexOf("." + s3Endpoint);
                if (subdomainOffset > 0) {
                    // Hostname represents an S3 sub-domain, so the bucket's name is the CNAME portion
                    virtualBucketPath = hostname.substring(0, subdomainOffset) + "/";
                } else {
                    // Hostname represents a virtual host, so the bucket's name is identical to hostname
                    virtualBucketPath = hostname + "/";
                }
                uriPath = (objectKey != null ? RestUtils.encodeUrlPath(objectKey, "/") : "");
            } else {
                uriPath = bucketName + (objectKey != null ? "/" + RestUtils.encodeUrlPath(objectKey, "/") : "");
            }

            if (specialParamName != null) {
                uriPath += "?" + specialParamName + "&";
            } else {
                uriPath += "?";
            }

            // Include any DevPay tokens in signed request
            if (credentials instanceof AWSDevPayCredentials) {
                AWSDevPayCredentials devPayCredentials = (AWSDevPayCredentials) credentials;
                if (devPayCredentials.getProductToken() != null) {
                    String securityToken = devPayCredentials.getUserToken()
                        + "," + devPayCredentials.getProductToken();
                    headersMap.put(Constants.AMZ_SECURITY_TOKEN, securityToken);
                } else {
                    headersMap.put(Constants.AMZ_SECURITY_TOKEN, devPayCredentials.getUserToken());
                }

                uriPath += Constants.AMZ_SECURITY_TOKEN + "=" +
                    RestUtils.encodeUrlString((String) headersMap.get(Constants.AMZ_SECURITY_TOKEN)) + "&";
            }

            uriPath += "AWSAccessKeyId=" + credentials.getAccessKey();
            uriPath += "&Expires=" + secondsSinceEpoch;

            // Include Requester Pays header flag, if the flag is included as a request parameter.
            if (specialParamName != null
                && specialParamName.toLowerCase().indexOf(Constants.REQUESTER_PAYS_BUCKET_FLAG) >= 0)
            {
                String[] requesterPaysHeaderAndValue = Constants.REQUESTER_PAYS_BUCKET_FLAG.split("=");
                headersMap.put(requesterPaysHeaderAndValue[0], requesterPaysHeaderAndValue[1]);
            }

            String serviceEndpointVirtualPath = this.getVirtualPath();

            String canonicalString = RestUtils.makeServiceCanonicalString(method,
                serviceEndpointVirtualPath + "/" + virtualBucketPath + uriPath,
                renameMetadataKeys(headersMap), String.valueOf(secondsSinceEpoch),
                this.getRestHeaderPrefix(), this.getResourceParameterNames());
            if (log.isDebugEnabled()) {
                log.debug("Signing canonical string:\n" + canonicalString);
            }

            String signedCanonical = ServiceUtils.signWithHmacSha1(credentials.getSecretKey(),
                canonicalString);
            String encodedCanonical = RestUtils.encodeUrlString(signedCanonical);
            uriPath += "&Signature=" + encodedCanonical;

            if (isHttps) {
                int httpsPort = this.getHttpsPort();
                return "https://" + hostname
                    + (httpsPort != 443 ? ":" + httpsPort : "")
                    + serviceEndpointVirtualPath
                    + "/" + uriPath;
            } else {
                int httpPort = this.getHttpPort();
                return "http://" + hostname
                + (httpPort != 80 ? ":" + httpPort : "")
                + serviceEndpointVirtualPath
                + "/" + uriPath;
            }
        } catch (ServiceException se) {
            throw new S3ServiceException(se);
        } catch (UnsupportedEncodingException e) {
            throw new S3ServiceException(e);
        }
    }

    /**
     * Generates a signed URL string that will grant access to an S3 resource (bucket or object)
     * to whoever uses the URL up until the time specified. The URL will use the default
     * JetS3t property settings in the <tt>jets3t.properties</tt> file to determine whether
     * to generate HTTP or HTTPS links (<tt>s3service.https-only</tt>), and whether to disable
     * DNS bucket naming (<tt>s3service.disable-dns-buckets</tt>).
     *
     * @param method
     * the HTTP method to sign, such as GET or PUT (note that S3 does not support POST requests).
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param specialParamName
     * the name of a request parameter to add to the URL generated by this method. 'Special'
     * parameters may include parameters that specify the kind of S3 resource that the URL
     * will refer to, such as 'acl', 'torrent', 'logging' or 'location'.
     * @param headersMap
     * headers to add to the signed URL, may be null.
     * Headers that <b>must</b> match between the signed URL and the actual request include:
     * content-md5, content-type, and any header starting with 'x-amz-'.
     * @param secondsSinceEpoch
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *  <b>Note:</b> This time is specified in seconds since the epoch, not milliseconds.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     *
     * @return
     * a URL signed in such a way as to grant access to an S3 resource to whoever uses it.
     *
     * @throws S3ServiceException
     */
    public String createSignedUrl(String method, String bucketName, String objectKey,
        String specialParamName, Map<String, Object> headersMap, long secondsSinceEpoch,
        boolean isVirtualHost) throws S3ServiceException
    {
        boolean isHttps = this.isHttpsOnly();
        boolean disableDnsBuckets = this.getDisableDnsBuckets();

        return createSignedUrl(method, bucketName, objectKey, specialParamName,
            headersMap, secondsSinceEpoch, isVirtualHost, isHttps, disableDnsBuckets);
    }

    /**
     * Generates a signed URL string that will grant access to an S3 resource (bucket or object)
     * to whoever uses the URL up until the time specified.
     *
     * @param method
     * the HTTP method to sign, such as GET or PUT (note that S3 does not support POST requests).
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param specialParamName
     * the name of a request parameter to add to the URL generated by this method. 'Special'
     * parameters may include parameters that specify the kind of S3 resource that the URL
     * will refer to, such as 'acl', 'torrent', 'logging' or 'location'.
     * @param headersMap
     * headers to add to the signed URL, may be null.
     * Headers that <b>must</b> match between the signed URL and the actual request include:
     * content-md5, content-type, and any header starting with 'x-amz-'.
     * @param secondsSinceEpoch
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *  <b>Note:</b> This time is specified in seconds since the epoch, not milliseconds.
     *
     * @return
     * a URL signed in such a way as to grant access to an S3 resource to whoever uses it.
     *
     * @throws S3ServiceException
     */
    public String createSignedUrl(String method, String bucketName, String objectKey,
        String specialParamName, Map<String, Object> headersMap, long secondsSinceEpoch)
        throws S3ServiceException
    {
        return createSignedUrl(method, bucketName, objectKey, specialParamName, headersMap,
            secondsSinceEpoch, false);
    }


    /**
     * Generates a signed GET URL.
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     *
     * @return
     * a URL signed in such a way as to grant GET access to an S3 resource to whoever uses it.
     * @throws S3ServiceException
     */
    public String createSignedGetUrl(String bucketName, String objectKey,
        Date expiryTime, boolean isVirtualHost) throws S3ServiceException
    {
        long secondsSinceEpoch = expiryTime.getTime() / 1000;
        return createSignedUrl("GET", bucketName, objectKey, null, null,
            secondsSinceEpoch, isVirtualHost);
    }


    /**
     * Generates a signed GET URL.
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *
     * @return
     * a URL signed in such a way as to grant GET access to an S3 resource to whoever uses it.
     * @throws S3ServiceException
     */
    public String createSignedGetUrl(String bucketName, String objectKey,
        Date expiryTime) throws S3ServiceException
    {
        return createSignedGetUrl(bucketName, objectKey, expiryTime, false);
    }


    /**
     * Generates a signed PUT URL.
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param headersMap
     * headers to add to the signed URL, may be null.
     * Headers that <b>must</b> match between the signed URL and the actual request include:
     * content-md5, content-type, and any header starting with 'x-amz-'.
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     *
     * @return
     * a URL signed in such a way as to allow anyone to PUT an object into S3.
     * @throws S3ServiceException
     */
    public String createSignedPutUrl(String bucketName, String objectKey,
        Map<String, Object> headersMap, Date expiryTime, boolean isVirtualHost)
        throws S3ServiceException
    {
        long secondsSinceEpoch = expiryTime.getTime() / 1000;
        return createSignedUrl("PUT", bucketName, objectKey, null, headersMap,
            secondsSinceEpoch, isVirtualHost);
    }


    /**
     * Generates a signed PUT URL.
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param headersMap
     * headers to add to the signed URL, may be null.
     * Headers that <b>must</b> match between the signed URL and the actual request include:
     * content-md5, content-type, and any header starting with 'x-amz-'.
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *
     * @return
     * a URL signed in such a way as to allow anyone to PUT an object into S3.
     * @throws S3ServiceException
     */
    public String createSignedPutUrl(String bucketName, String objectKey,
        Map<String, Object> headersMap, Date expiryTime) throws S3ServiceException
    {
        return createSignedPutUrl(bucketName, objectKey, headersMap, expiryTime, false);
    }


    /**
     * Generates a signed DELETE URL.
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     *
     * @return
     * a URL signed in such a way as to allow anyone do DELETE an object in S3.
     * @throws S3ServiceException
     */
    public String createSignedDeleteUrl(String bucketName, String objectKey,
        Date expiryTime, boolean isVirtualHost) throws S3ServiceException
    {
        long secondsSinceEpoch = expiryTime.getTime() / 1000;
        return createSignedUrl("DELETE", bucketName, objectKey, null, null,
            secondsSinceEpoch, isVirtualHost);
    }


    /**
     * Generates a signed DELETE URL.
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *
     * @return
     * a URL signed in such a way as to allow anyone do DELETE an object in S3.
     * @throws S3ServiceException
     */
    public String createSignedDeleteUrl(String bucketName, String objectKey,
        Date expiryTime) throws S3ServiceException
    {
        return createSignedDeleteUrl(bucketName, objectKey, expiryTime, false);
    }


    /**
     * Generates a signed HEAD URL.
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     *
     * @return
     * a URL signed in such a way as to grant HEAD access to an S3 resource to whoever uses it.
     * @throws S3ServiceException
     */
    public String createSignedHeadUrl(String bucketName, String objectKey,
        Date expiryTime, boolean isVirtualHost) throws S3ServiceException
    {
        long secondsSinceEpoch = expiryTime.getTime() / 1000;
        return createSignedUrl("HEAD", bucketName, objectKey, null, null,
            secondsSinceEpoch, isVirtualHost);
    }


    /**
     * Generates a signed HEAD URL.
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *
     * @return
     * a URL signed in such a way as to grant HEAD access to an S3 resource to whoever uses it.
     * @throws S3ServiceException
     */
    public String createSignedHeadUrl(String bucketName, String objectKey,
        Date expiryTime) throws S3ServiceException
    {
        return createSignedHeadUrl(bucketName, objectKey, expiryTime, false);
    }

    /**
     * Generates a signed URL string that will grant access to an S3 resource (bucket or object)
     * to whoever uses the URL up until the time specified.
     *
     * @deprecated 0.7.4
     *
     * @param method
     * the HTTP method to sign, such as GET or PUT (note that S3 does not support POST requests).
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param specialParamName
     * the name of a request parameter to add to the URL generated by this method. 'Special'
     * parameters may include parameters that specify the kind of S3 resource that the URL
     * will refer to, such as 'acl', 'torrent', 'logging', or 'location'.
     * @param headersMap
     * headers to add to the signed URL, may be null.
     * Headers that <b>must</b> match between the signed URL and the actual request include:
     * content-md5, content-type, and any header starting with 'x-amz-'.
     * @param credentials
     * the credentials of someone with sufficient privileges to grant access to the bucket/object
     * @param secondsSinceEpoch
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *  <b>Note:</b> This time is specified in seconds since the epoch, not milliseconds.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     * @param isHttps
     * if true, the signed URL will use the HTTPS protocol. If false, the signed URL will
     * use the HTTP protocol.
     * @param isDnsBucketNamingDisabled
     * if true, the signed URL will not use the DNS-name format for buckets eg.
     * <tt>jets3t.s3.amazonaws.com</tt>. Unless you have a specific reason to disable
     * DNS bucket naming, leave this value false.
     *
     * @return
     * a URL signed in such a way as to grant access to an S3 resource to whoever uses it.
     *
     * @throws S3ServiceException
     */
    @Deprecated
    public static String createSignedUrl(String method, String bucketName, String objectKey,
        String specialParamName, Map<String, Object> headersMap, ProviderCredentials credentials,
        long secondsSinceEpoch, boolean isVirtualHost, boolean isHttps,
        boolean isDnsBucketNamingDisabled) throws S3ServiceException
    {
        S3Service s3Service = new RestS3Service(credentials);
        return s3Service.createSignedUrl(method, bucketName, objectKey,
            specialParamName, headersMap, secondsSinceEpoch,
            isVirtualHost, isHttps, isDnsBucketNamingDisabled);
    }

    /**
     * Generates a signed URL string that will grant access to an S3 resource (bucket or object)
     * to whoever uses the URL up until the time specified. The URL will use the default
     * JetS3t property settings in the <tt>jets3t.properties</tt> file to determine whether
     * to generate HTTP or HTTPS links (<tt>s3service.https-only</tt>), and whether to disable
     * DNS bucket naming (<tt>s3service.disable-dns-buckets</tt>).
     *
     * @deprecated 0.7.4
     *
     * @param method
     * the HTTP method to sign, such as GET or PUT (note that S3 does not support POST requests).
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param specialParamName
     * the name of a request parameter to add to the URL generated by this method. 'Special'
     * parameters may include parameters that specify the kind of S3 resource that the URL
     * will refer to, such as 'acl', 'torrent', 'logging' or 'location'.
     * @param headersMap
     * headers to add to the signed URL, may be null.
     * Headers that <b>must</b> match between the signed URL and the actual request include:
     * content-md5, content-type, and any header starting with 'x-amz-'.
     * @param credentials
     * the credentials of someone with sufficient privileges to grant access to the bucket/object
     * @param secondsSinceEpoch
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *  <b>Note:</b> This time is specified in seconds since the epoch, not milliseconds.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     *
     * @return
     * a URL signed in such a way as to grant access to an S3 resource to whoever uses it.
     *
     * @throws S3ServiceException
     */
    @Deprecated
    public String createSignedUrl(String method, String bucketName, String objectKey,
        String specialParamName, Map<String, Object> headersMap, ProviderCredentials credentials,
        long secondsSinceEpoch, boolean isVirtualHost) throws S3ServiceException
    {
        boolean isHttps = this.getHttpsOnly();
        boolean disableDnsBuckets = this.getDisableDnsBuckets();

        return createSignedUrl(method, bucketName, objectKey, specialParamName,
            headersMap, credentials, secondsSinceEpoch, isVirtualHost, isHttps,
            disableDnsBuckets);
    }

    /**
     * Generates a signed URL string that will grant access to an S3 resource (bucket or object)
     * to whoever uses the URL up until the time specified.
     *
     * @deprecated 0.7.4
     *
     * @param method
     * the HTTP method to sign, such as GET or PUT (note that S3 does not support POST requests).
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param specialParamName
     * the name of a request parameter to add to the URL generated by this method. 'Special'
     * parameters may include parameters that specify the kind of S3 resource that the URL
     * will refer to, such as 'acl', 'torrent', 'logging' or 'location'.
     * @param headersMap
     * headers to add to the signed URL, may be null.
     * Headers that <b>must</b> match between the signed URL and the actual request include:
     * content-md5, content-type, and any header starting with 'x-amz-'.
     * @param credentials
     * the credentials of someone with sufficient privileges to grant access to the bucket/object
     * @param secondsSinceEpoch
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *  <b>Note:</b> This time is specified in seconds since the epoch, not milliseconds.
     *
     * @return
     * a URL signed in such a way as to grant access to an S3 resource to whoever uses it.
     *
     * @throws S3ServiceException
     */
    @Deprecated
    public String createSignedUrl(String method, String bucketName, String objectKey,
        String specialParamName, Map<String, Object> headersMap, ProviderCredentials credentials,
        long secondsSinceEpoch) throws S3ServiceException
    {
        return createSignedUrl(method, bucketName, objectKey, specialParamName, headersMap,
            credentials, secondsSinceEpoch, false);
    }


    /**
     * Generates a signed GET URL.
     *
     * @deprecated 0.7.4
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param credentials
     * the credentials of someone with sufficient privileges to grant access to the bucket/object
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     *
     * @return
     * a URL signed in such a way as to grant GET access to an S3 resource to whoever uses it.
     * @throws S3ServiceException
     */
    @Deprecated
    public String createSignedGetUrl(String bucketName, String objectKey,
        ProviderCredentials credentials, Date expiryTime, boolean isVirtualHost)
        throws S3ServiceException
    {
        long secondsSinceEpoch = expiryTime.getTime() / 1000;
        return createSignedUrl("GET", bucketName, objectKey, null, null,
            credentials, secondsSinceEpoch, isVirtualHost);
    }


    /**
     * Generates a signed GET URL.
     *
     * @deprecated 0.7.4
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param credentials
     * the credentials of someone with sufficient privileges to grant access to the bucket/object
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *
     * @return
     * a URL signed in such a way as to grant GET access to an S3 resource to whoever uses it.
     * @throws S3ServiceException
     */
    @Deprecated
    public String createSignedGetUrl(String bucketName, String objectKey,
        ProviderCredentials credentials, Date expiryTime)
        throws S3ServiceException
    {
        return createSignedGetUrl(bucketName, objectKey, credentials, expiryTime, false);
    }


    /**
     * Generates a signed PUT URL.
     *
     * @deprecated 0.7.4
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param headersMap
     * headers to add to the signed URL, may be null.
     * Headers that <b>must</b> match between the signed URL and the actual request include:
     * content-md5, content-type, and any header starting with 'x-amz-'.
     * @param credentials
     * the credentials of someone with sufficient privileges to grant access to the bucket/object
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     *
     * @return
     * a URL signed in such a way as to allow anyone to PUT an object into S3.
     * @throws S3ServiceException
     */
    @Deprecated
    public String createSignedPutUrl(String bucketName, String objectKey,
        Map<String, Object> headersMap, ProviderCredentials credentials, Date expiryTime,
        boolean isVirtualHost) throws S3ServiceException
    {
        long secondsSinceEpoch = expiryTime.getTime() / 1000;
        return createSignedUrl("PUT", bucketName, objectKey, null, headersMap,
            credentials, secondsSinceEpoch, isVirtualHost);
    }


    /**
     * Generates a signed PUT URL.
     *
     * @deprecated 0.7.4
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param headersMap
     * headers to add to the signed URL, may be null.
     * Headers that <b>must</b> match between the signed URL and the actual request include:
     * content-md5, content-type, and any header starting with 'x-amz-'.
     * @param credentials
     * the credentials of someone with sufficient privileges to grant access to the bucket/object
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *
     * @return
     * a URL signed in such a way as to allow anyone to PUT an object into S3.
     * @throws S3ServiceException
     */
    @Deprecated
    public String createSignedPutUrl(String bucketName, String objectKey,
        Map<String, Object> headersMap, ProviderCredentials credentials, Date expiryTime)
        throws S3ServiceException
    {
        return createSignedPutUrl(bucketName, objectKey, headersMap, credentials, expiryTime, false);
    }


    /**
     * Generates a signed DELETE URL.
     *
     * @deprecated 0.7.4
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param credentials
     * the credentials of someone with sufficient privileges to grant access to the bucket/object
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     *
     * @return
     * a URL signed in such a way as to allow anyone do DELETE an object in S3.
     * @throws S3ServiceException
     */
    @Deprecated
    public String createSignedDeleteUrl(String bucketName, String objectKey,
        ProviderCredentials credentials, Date expiryTime, boolean isVirtualHost)
        throws S3ServiceException
    {
        long secondsSinceEpoch = expiryTime.getTime() / 1000;
        return createSignedUrl("DELETE", bucketName, objectKey, null, null,
            credentials, secondsSinceEpoch, isVirtualHost);
    }


    /**
     * Generates a signed DELETE URL.
     *
     * @deprecated 0.7.4
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param credentials
     * the credentials of someone with sufficient privileges to grant access to the bucket/object
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *
     * @return
     * a URL signed in such a way as to allow anyone do DELETE an object in S3.
     * @throws S3ServiceException
     */
    @Deprecated
    public String createSignedDeleteUrl(String bucketName, String objectKey,
        ProviderCredentials credentials, Date expiryTime)
        throws S3ServiceException
    {
        return createSignedDeleteUrl(bucketName, objectKey, credentials, expiryTime, false);
    }


    /**
     * Generates a signed HEAD URL.
     *
     * @deprecated 0.7.4
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param credentials
     * the credentials of someone with sufficient privileges to grant access to the bucket/object
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     * @param isVirtualHost
     * if this parameter is true, the bucket name is treated as a virtual host name. To use
     * this option, the bucket name must be a valid DNS name that is an alias to an S3 bucket.
     *
     * @return
     * a URL signed in such a way as to grant HEAD access to an S3 resource to whoever uses it.
     * @throws S3ServiceException
     */
    @Deprecated
    public String createSignedHeadUrl(String bucketName, String objectKey,
        ProviderCredentials credentials, Date expiryTime, boolean isVirtualHost)
        throws S3ServiceException
    {
        long secondsSinceEpoch = expiryTime.getTime() / 1000;
        return createSignedUrl("HEAD", bucketName, objectKey, null, null,
            credentials, secondsSinceEpoch, isVirtualHost);
    }


    /**
     * Generates a signed HEAD URL.
     *
     * @deprecated 0.7.4
     *
     * @param bucketName
     * the name of the bucket to include in the URL, must be a valid bucket name.
     * @param objectKey
     * the name of the object to include in the URL, if null only the bucket name is used.
     * @param credentials
     * the credentials of someone with sufficient privileges to grant access to the bucket/object
     * @param expiryTime
     * the time after which URL's signature will no longer be valid. This time cannot be null.
     *
     * @return
     * a URL signed in such a way as to grant HEAD access to an S3 resource to whoever uses it.
     * @throws S3ServiceException
     */
    @Deprecated
    public String createSignedHeadUrl(String bucketName, String objectKey,
        ProviderCredentials credentials, Date expiryTime)
        throws S3ServiceException
    {
        return createSignedHeadUrl(bucketName, objectKey, credentials, expiryTime, false);
    }

    /**
     * Generates a URL string that will return a Torrent file for an object in S3,
     * which file can be downloaded and run in a BitTorrent client.
     *
     * @param bucketName
     * the name of the bucket containing the object.
     * @param objectKey
     * the name of the object.
     * @return
     * a URL to a Torrent file representing the object.
     */
    public String createTorrentUrl(String bucketName, String objectKey)
    {
        String s3Endpoint = this.getEndpoint();
        String serviceEndpointVirtualPath = this.getVirtualPath();
        int httpPort = this.getHttpPort();
        boolean disableDnsBuckets = this.getDisableDnsBuckets();

        try {
            String bucketNameInPath =
                !disableDnsBuckets && ServiceUtils.isBucketNameValidDNSName(bucketName)
                ? ""
                : RestUtils.encodeUrlString(bucketName) + "/";
            String urlPath =
                RestUtils.encodeUrlPath(serviceEndpointVirtualPath, "/")
                + "/" + bucketNameInPath
                + RestUtils.encodeUrlPath(objectKey, "/");
            return "http://" + ServiceUtils.generateS3HostnameForBucket(
                bucketName, disableDnsBuckets, s3Endpoint)
                + (httpPort != 80 ? ":" + httpPort : "")
                + urlPath
                + "?torrent";
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a policy document condition statement to represent an operation.
     *
     * @param operation
     * the name of the test operation this condition statement will apply.
     * @param name
     * the name of the data item the condition applies to.
     * @param value
     * the test value that will be used by the condition operation.
     * @return
     * a condition statement that can be included in the policy document
     * belonging to an S3 POST form.
     */
    public static String generatePostPolicyCondition(String operation, String name, String value) {
        return "[\"" + operation + "\", \"$" + name + "\", \"" + value + "\"]";
    }

    /**
     * Generates a policy document condition statement that will allow the named
     * data item in a POST request to take on any value.
     *
     * @param name
     * the name of the data item that will be allowed to take on any value.
     * @return
     * a condition statement that can be included in the policy document
     * belonging to an S3 POST form.
     */
    public static String generatePostPolicyCondition_AllowAnyValue(String name) {
        return "[\"starts-with\", \"$" + name + "\", \"\"]";
    }

    /**
     * Generates a policy document condition statement to represent an
     * equality test.
     *
     * @param name
     * the name of the data item that will be tested.
     * @param value
     * the value that the named data item must match.
     * @return
     * a condition statement that can be included in the policy document
     * belonging to an S3 POST form.
     */
    public static String generatePostPolicyCondition_Equality(String name, String value) {
        return "{\"" + name + "\": \"" + value + "\"}";
    }

    /**
     * Generates a policy document condition statement to represent an
     * equality test.
     *
     * @param name
     * the name of the data item that will be tested.
     * @param values
     * a list of values, one of which must match the named data item.
     * @return
     * a condition statement that can be included in the policy document
     * belonging to an S3 POST form.
     */
    public static String generatePostPolicyCondition_Equality(String name, String[] values) {
        return "{\"" + name + "\": \"" + ServiceUtils.join(values, ",") + "\"}";
    }

    /**
     * Generates a policy document condition statement to represent an
     * equality test.
     *
     * @param name
     * the name of the data item that will be tested.
     * @param values
     * a list of values, one of which must match the named data item.
     * @return
     * a condition statement that can be included in the policy document
     * belonging to an S3 POST form.
     */
    public static String generatePostPolicyCondition_Equality(String name, List<String> values) {
        return "{\"" + name + "\": \"" + ServiceUtils.join(values, ",") + "\"}";
    }

    /**
     * Generates a policy document condition statement to represent a test that
     * imposes a limit on the minimum and maximum amount of data the user can
     * upload via a POST form.
     *
     * @param min
     * the minimum number of bytes the user must upload. This value should be
     * greater than or equal to zero.
     * @param max
     * the maximum number of bytes the user can upload. This value must be
     * greater than or equal to the min value.
     * @return
     * a condition statement that can be included in the policy document
     * belonging to an S3 POST form.
     */
    public static String generatePostPolicyCondition_Range(int min, int max) {
        return "[\"content-length-range\", " + min + ", " + max + "]";
    }


    /**
     * Generates an <b>unauthenticated</b> HTML POST form that can be used to
     * upload files or data to S3 from a standard web browser.
     * <p>
     * Because the generated form is unauthenticated, it will not contain a
     * policy document and will only allow uploads to be sent to S3 buckets
     * that are publicly writable.
     *
     * @param bucketName
     * the name of the target bucket to which the data will be uploaded.
     * @param key
     * the key name for the object that will store the data. The key name can
     * include the special variable <tt>${filename}</tt> which expands to the
     * name of the file the user uploaded in the form.
     * @return
     * A form document that can be included in a UTF-8 encoded HTML web page
     * to allow uploads to a publicly-writable S3 bucket via a web browser.
     *
     * @throws S3ServiceException
     * @throws UnsupportedEncodingException
     */
    public static String buildPostForm(String bucketName, String key)
        throws S3ServiceException, UnsupportedEncodingException
    {
        return buildPostForm(bucketName, key, null, null, null, null, null, true);
    }


    /**
     * Generates an HTML POST form that can be used to upload files or data to
     * S3 from a standard web browser.
     * <p>
     * Depending on the parameter values provided, this method will generate an
     * authenticated or unauthenticated form. If the form is unauthenticated, it
     * will not include a policy document and will therefore not have an
     * expiry date or any usage conditions. Unauthenticated forms may only be
     * used to upload data to a publicly writable bucket.
     * <p>
     * If both the expiration and conditions parameters are non-null, the form
     * will include a policy document and will be authenticated. In this case,
     * you must provide your AWS credentials to sign the authenticated form.
     *
     * @param bucketName
     * the name of the target bucket to which the data will be uploaded.
     * @param key
     * the key name for the object that will store the data. The key name can
     * include the special variable <tt>${filename}</tt> which expands to the
     * name of the file the user uploaded in the form.
     * @param credentials
     * your Storage Provideer credentials. Credentials are only required if the form
     * includes policy document conditions, otherwise this can be null.
     * @param expiration
     * the expiration date beyond which the form will cease to work. If this
     * parameter is null, the generated form will not include a policy document
     * and will not have an expiry date.
     * @param conditions
     * the policy conditions applied to the form, specified as policy document
     * condition statements. These statements can be generated with the
     * convenience method {@link #generatePostPolicyCondition(String, String, String)}
     * and its siblings. If this parameter is null, the generated form will not
     * include a policy document and will not apply any usage conditions.
     * @param inputFields
     * optional input field strings that will be added to the form. Each string
     * must be a valid HTML form input field definition, such as
     * <tt>&lt;input type="hidden" name="acl" value="public-read"></tt>
     * @param textInput
     * an optional input field definition that is used instead of the default
     * file input field <tt>&lt;input name=\"file\" type=\"file\"></tt>. If this
     * parameter is null, the default file input field will be used to allow
     * file uploads. If this parameter is non-null, the provided string must
     * define an input field named "file" that allows the user to provide input,
     * such as <tt>&lt;textarea name="file" cols="60" rows="3">&lt;/textarea></tt>
     * @param isSecureHttp
     * if this parameter is true the form will upload data to S3 using HTTPS,
     * otherwise it will use HTTP.
     * @return
     * A form document that can be included in a UTF-8 encoded HTML web page
     * to allow uploads to S3 via a web browser.
     *
     * @throws S3ServiceException
     * @throws UnsupportedEncodingException
     */
    public static String buildPostForm(String bucketName, String key,
        ProviderCredentials credentials, Date expiration, String[] conditions,
        String[] inputFields, String textInput, boolean isSecureHttp)
        throws S3ServiceException, UnsupportedEncodingException
    {
        return buildPostForm(bucketName, key, credentials, expiration,
                conditions, inputFields, textInput, isSecureHttp,
                false, "Upload to Amazon S3");
    }

    /**
     * Generates an HTML POST form that can be used to upload files or data to
     * S3 from a standard web browser.
     * <p>
     * Depending on the parameter values provided, this method will generate an
     * authenticated or unauthenticated form. If the form is unauthenticated, it
     * will not include a policy document and will therefore not have an
     * expiry date or any usage conditions. Unauthenticated forms may only be
     * used to upload data to a publicly writable bucket.
     * <p>
     * If both the expiration and conditions parameters are non-null, the form
     * will include a policy document and will be authenticated. In this case,
     * you must provide your AWS credentials to sign the authenticated form.
     *
     * @param bucketName
     * the name of the target bucket to which the data will be uploaded.
     * @param key
     * the key name for the object that will store the data. The key name can
     * include the special variable <tt>${filename}</tt> which expands to the
     * name of the file the user uploaded in the form.
     * @param credentials
     * your Storage Provider credentials. Credentials are only required if the form
     * includes policy document conditions, otherwise this can be null.
     * @param expiration
     * the expiration date beyond which the form will cease to work. If this
     * parameter is null, the generated form will not include a policy document
     * and will not have an expiry date.
     * @param conditions
     * the policy conditions applied to the form, specified as policy document
     * condition statements. These statements can be generated with the
     * convenience method {@link #generatePostPolicyCondition(String, String, String)}
     * and its siblings. If this parameter is null, the generated form will not
     * include a policy document and will not apply any usage conditions.
     * @param inputFields
     * optional input field strings that will be added to the form. Each string
     * must be a valid HTML form input field definition, such as
     * <tt>&lt;input type="hidden" name="acl" value="public-read"></tt>
     * @param textInput
     * an optional input field definition that is used instead of the default
     * file input field <tt>&lt;input name=\"file\" type=\"file\"></tt>. If this
     * parameter is null, the default file input field will be used to allow
     * file uploads. If this parameter is non-null, the provided string must
     * define an input field named "file" that allows the user to provide input,
     * such as <tt>&lt;textarea name="file" cols="60" rows="3">&lt;/textarea></tt>
     * @param isSecureHttp
     * if this parameter is true the form will upload data to S3 using HTTPS,
     * otherwise it will use HTTP.
     * @param usePathStyleUrl
     * if true the deprecated path style URL will be used to specify the bucket
     * name, for example: http://s3.amazon.com/BUCKET_NAME. If false, the
     * recommended sub-domain style will be used, for example:
     * http://BUCKET_NAME.s3.amazon.com/.
     * The path style can be useful for accessing US-based buckets with SSL,
     * however non-US buckets are inaccessible with this style URL.
     * @param submitButtonName
     * the name to display on the form's submit button.
     *
     * @return
     * A form document that can be included in a UTF-8 encoded HTML web page
     * to allow uploads to S3 via a web browser.
     *
     * @throws S3ServiceException
     * @throws UnsupportedEncodingException
     */
    public static String buildPostForm(String bucketName, String key,
        ProviderCredentials credentials, Date expiration, String[] conditions,
        String[] inputFields, String textInput, boolean isSecureHttp,
        boolean usePathStyleUrl, String submitButtonName)
        throws S3ServiceException, UnsupportedEncodingException
    {
        List<String> myInputFields = new ArrayList<String>();

        // Form is only authenticated if a policy is specified.
        if (expiration != null || conditions != null) {
            // Generate policy document
            String policyDocument =
                "{\"expiration\": \"" + ServiceUtils.formatIso8601Date(expiration)
                + "\", \"conditions\": [" + ServiceUtils.join(conditions, ",") + "]}";
            if (log.isDebugEnabled()) {
                log.debug("Policy document for POST form:\n" + policyDocument);
            }

            // Add the base64-encoded policy document as the 'policy' form field
            String policyB64 = ServiceUtils.toBase64(
                policyDocument.getBytes(Constants.DEFAULT_ENCODING));
            myInputFields.add("<input type=\"hidden\" name=\"policy\" value=\""
                + policyB64 + "\">");

            // Add the AWS access key as the 'AWSAccessKeyId' field
            myInputFields.add("<input type=\"hidden\" name=\"AWSAccessKeyId\" " +
                "value=\"" + credentials.getAccessKey() + "\">");

            // Add signature for encoded policy document as the 'AWSAccessKeyId' field
            String signature;
            try {
                signature = ServiceUtils.signWithHmacSha1(
                    credentials.getSecretKey(), policyB64);
            } catch (ServiceException se) {
                throw new S3ServiceException(se);
            }
            myInputFields.add("<input type=\"hidden\" name=\"signature\" " +
                "value=\"" + signature + "\">");
        }

        // Include any additional user-specified form fields
        if (inputFields != null) {
            myInputFields.addAll(Arrays.asList(inputFields));
        }

        // Add the vital 'file' input item, which may be a textarea or file.
        if (textInput != null) {
            // Use a caller-specified string as the input field.
            myInputFields.add(textInput);
        } else {
            myInputFields.add("<input name=\"file\" type=\"file\">");
        }

        // Construct a URL to refer to the target bucket using either the
        // deprecated path style, or the recommended sub-domain style. The
        // HTTPS protocol will be used if the secure HTTP option is enabled.
        String url = null;
        if (usePathStyleUrl) {
            url = "http" + (isSecureHttp? "s" : "") +
                "://s3.amazonaws.com/" +  bucketName;
        } else {
            // Sub-domain URL style
            url = "http" + (isSecureHttp? "s" : "") +
                "://" + bucketName + ".s3.amazonaws.com/";
        }

        // Construct the entire form.
        String form =
          "<form action=\"" + url + "\" method=\"post\" " +
              "enctype=\"multipart/form-data\">\n" +
            "<input type=\"hidden\" name=\"key\" value=\"" + key + "\">\n" +
            ServiceUtils.join(myInputFields, "\n") +
            "\n<br>\n" +
            "<input type=\"submit\" value=\"" + submitButtonName + "\">\n" +
          "</form>";

        if (log.isDebugEnabled()) {
            log.debug("POST Form:\n" + form);
        }
        return form;
    }

    /////////////////////////////////////////////////
    // Methods below this point perform actions in S3
    /////////////////////////////////////////////////

    @Override
    public S3Bucket[] listAllBuckets() throws S3ServiceException {
        try {
            StorageBucket[] buckets = super.listAllBuckets();
            return S3Bucket.cast(buckets);
        } catch (ServiceException se) {
            throw new S3ServiceException(se);
        }
    }

    @Override
    public S3Object getObject(String bucketName, String objectKey) throws S3ServiceException {
        try {
            return (S3Object) super.getObject(bucketName, objectKey);
        } catch (ServiceException se) {
            throw new S3ServiceException(se);
        }
    }

    /**
     * Lists the objects in a bucket.
     *
     * @deprecated 0.8.0
     *
     * <p>
     * The objects returned by this method contain only minimal information
     * such as the object's size, ETag, and LastModified timestamp. To retrieve
     * the objects' metadata you must perform follow-up <code>getObject</code>
     * or <code>getObjectDetails</code> operations.
     * <p>
     * This method can be performed by anonymous services. Anonymous services
     * can only list the objects in a publicly-readable bucket.
     *
     * @param bucket
     * the bucket whose contents will be listed.
     * This must be a valid S3Bucket object that is non-null and contains a name.
     * @return
     * the set of objects contained in a bucket.
     * @throws S3ServiceException
     */
    @Deprecated
    public S3Object[] listObjects(S3Bucket bucket) throws S3ServiceException {
        try {
            assertValidBucket(bucket, "listObjects");
            return listObjects(bucket, null, null, Constants.DEFAULT_OBJECT_LIST_CHUNK_SIZE);
        } catch (ServiceException se) {
            throw new S3ServiceException(se);
        }
    }

    @Override
    public S3Object[] listObjects(String bucketName) throws S3ServiceException {
        try {
            return S3Object.cast(super.listObjects(bucketName));
        } catch (ServiceException se) {
            throw new S3ServiceException(se);
        }
    }

    @Override
    public S3Object[] listObjects(String bucketName, String prefix,
        String delimiter, long maxListingLength) throws S3ServiceException
    {
        try {
            return S3Object.cast(super.listObjects(bucketName, prefix, delimiter, maxListingLength));
        } catch (ServiceException se) {
            throw new S3ServiceException(se);
        }
    }

    /**
     * Lists the objects in a bucket matching a prefix and delimiter.
     *
     * @deprecated 0.8.0
     *
     * <p>
     * The objects returned by this method contain only minimal information
     * such as the object's size, ETag, and LastModified timestamp. To retrieve
     * the objects' metadata you must perform follow-up <code>getObject</code>
     * or <code>getObjectDetails</code> operations.
     * <p>
     * This method can be performed by anonymous services. Anonymous services
     * can only list the objects in a publicly-readable bucket.
     * <p>
     * NOTE: If you supply a delimiter value that could cause CommonPrefixes
     * ("subdirectory paths") to be included in the results from S3, use the
     * {@link #listObjectsChunked(String, String, String, long, String, boolean)}
     * method instead of this one to obtain both object and CommonPrefix values.
     *
     * @param bucket
     * the bucket whose contents will be listed.
     * This must be a valid S3Bucket object that is non-null and contains a name.
     * @param prefix
     * only objects with a key that starts with this prefix will be listed
     * @param delimiter
     * only list objects with key names up to this delimiter, may be null.
     * See note above.
     * <b>Note</b>: If a non-null delimiter is specified, the prefix must include enough text to
     * reach the first occurrence of the delimiter in the bucket's keys, or no results will be returned.
     * @return
     * the set of objects contained in a bucket whose keys start with the given prefix.
     * @throws S3ServiceException
     */
    @Deprecated
    public S3Object

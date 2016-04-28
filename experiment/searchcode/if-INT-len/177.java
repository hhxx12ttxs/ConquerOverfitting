/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
// 
// This code is based on org/apache/catalina/servlets/DefaultServlet.java   
//
// ===================================================================================
//  
// The default resource-serving servlet for most web applications,
// used to serve static resources such as HTML pages and images.
//
// @author Craig R. McClanahan
// @author Remy Maucherat
// Read more: http://kickjava.com/src/org/apache/catalina/servlets/DefaultServlet.java.htm#ixzz0PMwsYZQW
// ====================================================================================

package ru.adv.mozart.servlet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;

import ru.adv.logger.TLogger;


/**
 * Static resource handler
 * 
 */
public class StaticResourceHandler {

    private static final String JAVAX_SERVLET_INCLUDE_CONTEXT_PATH = "javax.servlet.include.context_path";
    protected static int BUFFER_SIZE_MIN = 2048;
    protected static final String mimeSeparation = "CATALINA_MIME_BOUNDARY";

    private TLogger logger = new TLogger(StaticResourceHandler.class);
    protected int bufferOutputSize = BUFFER_SIZE_MIN;
    protected int bufferInputSize = BUFFER_SIZE_MIN;
    protected static MessageDigest md5Helper;
    private MimeTypeResolver mimeTypeResolver = null;
    private DirectoryContext directoryContext = null;
    private String textSourceEncoding = null;
    private long expiresDelta;
    private String urlPrefix;
    

    // --------------------------------------------------------- Public Methods

    /**
     * Initialize this servlet.
     */
    public StaticResourceHandler(String urlPrefix, StaticResourceHandlerContext ctx) throws ServletException {

    	this.urlPrefix = urlPrefix; 
        bufferOutputSize = ctx.getOutputBufferSize();
        bufferInputSize = ctx.getInputBufferSize();
        directoryContext = ctx.getDirectoryContext();
        mimeTypeResolver = ctx.getMimeTypeResolver();
        textSourceEncoding = ctx.getTextSourceEncoding();
        expiresDelta = ctx.getExpires();
        
        // Sanity check on the specified buffer sizes
        if (bufferOutputSize < BUFFER_SIZE_MIN)
            bufferOutputSize = BUFFER_SIZE_MIN;

        // Load the MD5 helper used to calculate signatures.
        try {
            md5Helper = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
        
        log(toString());

    }
    
    public String toString() {
        return getClass().getName() +
            " (" + 
                getDirectoryContext().toString() + 
            " )" ;
    }


    private boolean isDebug() {
        return logger.isDebugEnabled();
    }

    // ------------------------------------------------------ Protected Methods

    private void log(String message) {
   		logger.debug(message);
    }

    /**
     * Return the relative path associated with this servlet.
     *
     * @param request The servlet request we are processing
     */
    private String getRelativePath(HttpServletRequest request) {
        String path = new UrlPathHelper().getPathWithinApplication(request);
        if ( urlPrefix!=null &&  path.startsWith(urlPrefix) ) {
        	path = path.substring(urlPrefix.length());
        }
        return path;
    }


    /**
     * Process a GET request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/bufferSize error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	
        try {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                // Serve the requested resource, including the data content
                serveResource(request, response, true);
            } else if ("HEAD".equalsIgnoreCase(request.getMethod())) {
                // Serve the requested resource, without the data content
                serveResource(request, response, false);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            }
            
        } catch( IOException ex ) {
            // we probably have this check somewhere else too.
            if( ex.getMessage() != null && ex.getMessage().indexOf("Broken pipe") >= 0 ) {
                // ignore it.
            } else {
                throw ex;
            }
        }
    }
    

    /**
     * Check if the conditions specified in the optional If headers are
     * satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets all the specified conditions,
     * and false if any of the conditions is not satisfied, in which case
     * request processing is stopped
     */
    private boolean checkIfHeaders(HttpServletRequest request,
                                     HttpServletResponse response,
                                     ResourceInfo resourceInfo)
        throws IOException {

        return checkIfMatch(request, response, resourceInfo)
            && checkIfModifiedSince(request, response, resourceInfo)
            && checkIfNoneMatch(request, response, resourceInfo)
            && checkIfUnmodifiedSince(request, response, resourceInfo);

    }


    /**
     * Get the ETag associated with a file.
     *
     * @param resourceInfo File object
     */
    private String getETag(ResourceInfo resourceInfo) {
        if (resourceInfo.strongETag != null) {
            return resourceInfo.strongETag;
        } else if (resourceInfo.weakETag != null) {
            return resourceInfo.weakETag;
        } else {
            return "W/\"" + resourceInfo.length + "-"
                + resourceInfo.date + "\"";
        }
    }

    private DirectoryContext getDirectoryContext() {
        return directoryContext;
    }

    /**
     * Serve the specified resource, optionally including the data content.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param content Should the content be included?
     *
     * @exception IOException if an input/bufferSize error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    private void serveResource(HttpServletRequest request,
                                 HttpServletResponse response,
                                 boolean content)
        throws IOException, ServletException {

        // Identify the requested resource path
        String path = getRelativePath(request);
        
        if (isDebug()) {
        	log("Serving resource '" + path + "' headers "+( content ? "and data" : "only") + " into "+directoryContext.toString() );
        }
        
        ResourceInfo resourceInfo = new ResourceInfo(path, getDirectoryContext());
        
        if (!resourceInfo.exists) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                               request.getRequestURI());
            return;
        }

        if (path.endsWith("/") || (path.endsWith("\\"))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    request.getRequestURI());
            return;
        }

        // Checking If headers
        boolean included = (request.getAttribute(JAVAX_SERVLET_INCLUDE_CONTEXT_PATH) != null);
        if (!included && !checkIfHeaders(request, response, resourceInfo)) {
            return;
        }

        // Find content type.
        String contentType = getResourceContentType(resourceInfo);

        Vector ranges = null;

        // Parse range specifier
        ranges = parseRange(request, response, resourceInfo);
        
        // ETag header
        response.setHeader("ETag", getETag(resourceInfo));
        
        // Last-Modified header
        if (isDebug()){
            log("serveFile:  lastModified='" + resourceInfo.httpDate+ "'");
        }
        response.setHeader("Last-Modified", resourceInfo.httpDate);
        
        Output out = new Output();
        
        if ( ((ranges == null) || (ranges.isEmpty()))
               && (request.getHeader("Range") == null) ) {

            // Set the appropriate bufferSize headers
            if (contentType != null) {
                if (isDebug()) {
                    log("contentType='" +contentType + "'");
                }
                response.setContentType(contentType);
            }
            String expiresDate = HttpDateFormat.formatDate(
                    System.currentTimeMillis()+expiresDelta
            );
            if (isDebug()) {
                log("Expires='" + expiresDate + "'");
            }
            response.setHeader("Expires", expiresDate);
            
            long contentLength = resourceInfo.length;
            if ( contentLength >= 0 ) {
                if (isDebug()) {
                    log("contentLength=" + contentLength);
                }
                response.setContentLength((int) contentLength);
            }

            // Copy the input stream to our output stream (if requested)
            if (content) {
                out.init(response, contentType);
                try {
                    response.setBufferSize(bufferOutputSize);
                } catch (IllegalStateException e) {
                    // Silent catch
                }
                if (out.ostream != null) {
                    copy(resourceInfo, out.ostream);
                } else {
                    copy(resourceInfo, out.writer);
                }
            }

        } else {

            if ((ranges == null) || (ranges.isEmpty()))
                return;

            // Partial content response.

            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

            if (ranges.size() == 1) {

                Range range = (Range) ranges.elementAt(0);
                response.addHeader("Content-Range", "bytes "
                                   + range.start
                                   + "-" + range.end + "/"
                                   + range.length);
                response.setContentLength((int) (range.end - range.start + 1));

                if (contentType != null) {
                    log("serveFile:  contentType='" + contentType + "'");
                    response.setContentType(contentType);
                }

                if (content) {
                    out.init(response, contentType);
                    try {
                        response.setBufferSize(bufferOutputSize);
                    } catch (IllegalStateException e) {
                        // Silent catch
                    }
                    if (out.ostream != null) {
                        copy(resourceInfo, out.ostream, range);
                    } else {
                        copy(resourceInfo, out.writer, range);
                    }
                }

            } else {

                response.setContentType("multipart/byteranges; boundary=" + mimeSeparation);

                if (content) {
                    out.init(response, contentType);
                    try {
                        response.setBufferSize(bufferOutputSize);
                    } catch (IllegalStateException e) {
                        // Silent catch
                    }
                    if (out.ostream != null) {
                        copy(resourceInfo, out.ostream, ranges.elements(),
                             contentType);
                    } else {
                        copy(resourceInfo, out.writer, ranges.elements(),
                             contentType);
                    }
                }

            }

        }

    }

    /**
     * @param resourceInfo
     * @return
     */
    private String getResourceContentType(ResourceInfo resourceInfo) {
        String contentType = mimeTypeResolver.getMimeType(resourceInfo.path);
        if ( !StringUtils.hasLength(contentType) && resourceInfo.file!=null ) {
        	contentType = mimeTypeResolver.getMimeType( resourceInfo.file.getName() );
        }
        if (contentType!=null && contentType.startsWith("text")) {
            contentType += "; charset="+textSourceEncoding;
        }
        return contentType;
    }


    /**
     * Parse the range header.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Vector of ranges
     */
    private Vector parseRange(HttpServletRequest request,
                                HttpServletResponse response,
                                ResourceInfo resourceInfo)
        throws IOException {

        // Checking If-Range
        String headerValue = request.getHeader("If-Range");

        if (headerValue != null) {

            long headerValueTime = (-1L);
            try {
                headerValueTime = request.getDateHeader("If-Range");
            } catch (Exception e) {
                ;
            }

            String eTag = getETag(resourceInfo);
            long lastModified = resourceInfo.date;

            if (headerValueTime == (-1L)) {

                // If the ETag the client gave does not match the entity
                // etag, then the entire entity is returned.
                if (!eTag.equals(headerValue.trim()))
                    return null;

            } else {

                // If the timestamp of the entity the client got is older than
                // the last modification date of the entity, the entire entity
                // is returned.
                if (lastModified > (headerValueTime + 1000))
                    return null;

            }

        }

        long fileLength = resourceInfo.length;

        if (fileLength == 0)
            return null;

        // Retrieving the range header (if any is specified
        String rangeHeader = request.getHeader("Range");

        if (rangeHeader == null)
            return null;
        // bytes is the only range unit supported (and I don't see the point
        // of adding new ones).
        if (!rangeHeader.startsWith("bytes")) {
            response.addHeader("Content-Range", "bytes */" + fileLength);
            response.sendError
                (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return null;
        }

        rangeHeader = rangeHeader.substring(6);

        // Vector which will contain all the ranges which are successfully
        // parsed.
        Vector result = new Vector();
        StringTokenizer commaTokenizer = new StringTokenizer(rangeHeader, ",");

        // Parsing the range list
        while (commaTokenizer.hasMoreTokens()) {
            String rangeDefinition = commaTokenizer.nextToken().trim();

            Range currentRange = new Range();
            currentRange.length = fileLength;

            int dashPos = rangeDefinition.indexOf('-');

            if (dashPos == -1) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError
                    (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            if (dashPos == 0) {

                try {
                    long offset = Long.parseLong(rangeDefinition);
                    currentRange.start = fileLength + offset;
                    currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range",
                                       "bytes */" + fileLength);
                    response.sendError
                        (HttpServletResponse
                         .SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            } else {

                try {
                    currentRange.start = Long.parseLong
                        (rangeDefinition.substring(0, dashPos));
                    if (dashPos < rangeDefinition.length() - 1)
                        currentRange.end = Long.parseLong
                            (rangeDefinition.substring
                             (dashPos + 1, rangeDefinition.length()));
                    else
                        currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range",
                                       "bytes */" + fileLength);
                    response.sendError
                        (HttpServletResponse
                         .SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            }

            if (!currentRange.validate()) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError
                    (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            result.addElement(currentRange);
        }

        return result;
    }

 
    // -------------------------------------------------------- Private Methods


    /**
     * Check if the if-match condition is satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets the specified condition,
     * and false if the condition is not satisfied, in which case request
     * processing is stopped
     */
    private boolean checkIfMatch(HttpServletRequest request,
                                 HttpServletResponse response,
                                 ResourceInfo resourceInfo)
        throws IOException {

        String eTag = getETag(resourceInfo);
        String headerValue = request.getHeader("If-Match");
        if (headerValue != null) {
            if (headerValue.indexOf('*') == -1) {

                StringTokenizer commaTokenizer = new StringTokenizer
                    (headerValue, ",");
                boolean conditionSatisfied = false;

                while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
                    String currentToken = commaTokenizer.nextToken();
                    if (currentToken.trim().equals(eTag))
                        conditionSatisfied = true;
                }

                // If none of the given ETags match, 412 Precodition failed is
                // sent back
                if (!conditionSatisfied) {
                    response.sendError
                        (HttpServletResponse.SC_PRECONDITION_FAILED);
                    return false;
                }

            }
        }
        return true;

    }


    /**
     * Check if the if-modified-since condition is satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets the specified condition,
     * and false if the condition is not satisfied, in which case request
     * processing is stopped
     */
    private boolean checkIfModifiedSince(HttpServletRequest request,
                                         HttpServletResponse response,
                                         ResourceInfo resourceInfo)
        throws IOException {
        try {
            long headerValue = request.getDateHeader("If-Modified-Since");
            long lastModified = resourceInfo.date;
            if (headerValue != -1) {
                // If an If-None-Match header has been specified, if modified since
                // is ignored.
                if ((request.getHeader("If-None-Match") == null)
                    && (lastModified <= headerValue + 1000)) {
                    // The entity has not been modified since the date
                    // specified by the client. This is not an error case.
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return false;
                }
            }
        } catch(IllegalArgumentException illegalArgument) {
        	logger.warning(illegalArgument);
        }
        return true;

    }


    /**
     * Check if the if-none-match condition is satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets the specified condition,
     * and false if the condition is not satisfied, in which case request
     * processing is stopped
     */
    private boolean checkIfNoneMatch(HttpServletRequest request,
                                     HttpServletResponse response,
                                     ResourceInfo resourceInfo)
        throws IOException {

        String eTag = getETag(resourceInfo);
        String headerValue = request.getHeader("If-None-Match");
        if (headerValue != null) {

            boolean conditionSatisfied = false;

            if (!headerValue.equals("*")) {

                StringTokenizer commaTokenizer =
                    new StringTokenizer(headerValue, ",");

                while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
                    String currentToken = commaTokenizer.nextToken();
                    if (currentToken.trim().equals(eTag))
                        conditionSatisfied = true;
                }

            } else {
                conditionSatisfied = true;
            }

            if (conditionSatisfied) {

                // For GET and HEAD, we should respond with
                // 304 Not Modified.
                // For every other method, 412 Precondition Failed is sent
                // back.
                if ( ("GET".equals(request.getMethod()))
                     || ("HEAD".equals(request.getMethod())) ) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return false;
                } else {
                    response.sendError
                        (HttpServletResponse.SC_PRECONDITION_FAILED);
                    return false;
                }
            }
        }
        return true;

    }


    /**
     * Check if the if-unmodified-since condition is satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets the specified condition,
     * and false if the condition is not satisfied, in which case request
     * processing is stopped
     */
    private boolean checkIfUnmodifiedSince(HttpServletRequest request,
                                           HttpServletResponse response,
                                           ResourceInfo resourceInfo)
        throws IOException {
        try {
            long lastModified = resourceInfo.date;
            long headerValue = request.getDateHeader("If-Unmodified-Since");
            if (headerValue != -1) {
                if ( lastModified > (headerValue + 1000)) {
                    // The entity has not been modified since the date
                    // specified by the client. This is not an error case.
                    response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                    return false;
                }
            }
        } catch(IllegalArgumentException illegalArgument) {
            return true;
        }
        return true;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resourceInfo The resource information 
     * @param ostream The bufferSize stream to write to
     *
     * @exception IOException if an input/bufferSize error occurs
     */
    private void copy(ResourceInfo resourceInfo, ServletOutputStream ostream)
        throws IOException {

        IOException exception = null;

        // Optimization: If the binary content has already been loaded, send
        // it directly
        //TODO use memory cahce here for resourceInfo.file  
/*        if (resourceInfo.file != null) {
            byte buffer[] = resourceInfo.file.getContent();
            if (buffer != null) {
                ostream.write(buffer, 0, buffer.length);
                return;
            }
        }
*/
        InputStream resourceInputStream = resourceInfo.getStream();
        InputStream istream = new BufferedInputStream(resourceInputStream, bufferInputSize);

        // Copy the input stream to the bufferSize stream
        exception = copyRange(istream, ostream);

        // Clean up the input stream
        try {
            istream.close();
        } catch (Throwable t) {
            ;
        }

        // Rethrow any exception that has occurred
        if (exception != null)
            throw exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resourceInfo The resource info
     * @param writer The writer to write to
     *
     * @exception IOException if an input/bufferSize error occurs
     */
    private void copy(ResourceInfo resourceInfo, PrintWriter writer)
        throws IOException {

        IOException exception = null;

        InputStream resourceInputStream = resourceInfo.getStream();
        // TODO : i18n ?
        Reader reader = new InputStreamReader(resourceInputStream);

        // Copy the input stream to the bufferSize stream
        exception = copyRange(reader, writer);

        // Clean up the reader
        try {
            reader.close();
        } catch (Throwable t) {
            ;
        }

        // Rethrow any exception that has occurred
        if (exception != null)
            throw exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * bufferSize stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resourceInfo The ResourceInfo object
     * @param ostream The bufferSize stream to write to
     * @param range Range the client wanted to retrieve
     * @exception IOException if an input/bufferSize error occurs
     */
    private void copy(ResourceInfo resourceInfo, ServletOutputStream ostream,
                      Range range)
        throws IOException {

        IOException exception = null;

        InputStream resourceInputStream = resourceInfo.getStream();
        InputStream istream =
            new BufferedInputStream(resourceInputStream, bufferInputSize);
        exception = copyRange(istream, ostream, range.start, range.end);

        // Clean up the input stream
        try {
            istream.close();
        } catch (Throwable t) {
            ;
        }

        // Rethrow any exception that has occurred
        if (exception != null)
            throw exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * bufferSize stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resourceInfo The ResourceInfo object
     * @param writer The writer to write to
     * @param range Range the client wanted to retrieve
     * @exception IOException if an input/bufferSize error occurs
     */
    private void copy(ResourceInfo resourceInfo, PrintWriter writer,
                      Range range)
        throws IOException {

        IOException exception = null;

        InputStream resourceInputStream = resourceInfo.getStream();
        Reader reader = new InputStreamReader(resourceInputStream);
        exception = copyRange(reader, writer, range.start, range.end);

        // Clean up the input stream
        try {
            reader.close();
        } catch (Throwable t) {
            ;
        }

        // Rethrow any exception that has occurred
        if (exception != null)
            throw exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * bufferSize stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resourceInfo The ResourceInfo object
     * @param ostream The bufferSize stream to write to
     * @param ranges Enumeration of the ranges the client wanted to retrieve
     * @param contentType Content type of the resource
     * @exception IOException if an input/bufferSize error occurs
     */
    private void copy(ResourceInfo resourceInfo, ServletOutputStream ostream,
                      Enumeration ranges, String contentType)
        throws IOException {

        IOException exception = null;

        while ( (exception == null) && (ranges.hasMoreElements()) ) {

            InputStream resourceInputStream = resourceInfo.getStream();
            InputStream istream =       // TODO: internationalization???????
                new BufferedInputStream(resourceInputStream, bufferInputSize);

            Range currentRange = (Range) ranges.nextElement();

            // Writing MIME header.
            ostream.println();
            ostream.println("--" + mimeSeparation);
            if (contentType != null) {
                ostream.println("Content-Type: " + contentType);
            }
            ostream.println("Content-Range: bytes " + currentRange.start
                           + "-" + currentRange.end + "/"
                           + currentRange.length);
            ostream.println();

            // Printing content
            exception = copyRange(istream, ostream, currentRange.start,
                                  currentRange.end);

            try {
                istream.close();
            } catch (Throwable t) {
                ;
            }

        }

        ostream.println();
        ostream.print("--" + mimeSeparation + "--");

        // Rethrow any exception that has occurred
        if (exception != null)
            throw exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * bufferSize stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resourceInfo The ResourceInfo object
     * @param writer The writer to write to
     * @param ranges Enumeration of the ranges the client wanted to retrieve
     * @param contentType Content type of the resource
     * @exception IOException if an input/bufferSize error occurs
     */
    private void copy(ResourceInfo resourceInfo, PrintWriter writer,
                      Enumeration ranges, String contentType)
        throws IOException {

        IOException exception = null;

        while ( (exception == null) && (ranges.hasMoreElements()) ) {

            InputStream resourceInputStream = resourceInfo.getStream();
            Reader reader = new InputStreamReader(resourceInputStream);

            Range currentRange = (Range) ranges.nextElement();

            // Writing MIME header.
            writer.println();
            writer.println("--" + mimeSeparation);
            if (contentType != null) {
                writer.println("Content-Type: " + contentType);
            }
            writer.println("Content-Range: bytes " + currentRange.start
                           + "-" + currentRange.end + "/"
                           + currentRange.length);
            writer.println();

            // Printing content
            exception = copyRange(reader, writer, currentRange.start,
                                  currentRange.end);

            try {
                reader.close();
            } catch (Throwable t) {
                ;
            }

        }

        writer.println();
        writer.print("--" + mimeSeparation + "--");

        // Rethrow any exception that has occurred
        if (exception != null)
            throw exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * bufferSize stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param istream The input stream to read from
     * @param ostream The bufferSize stream to write to
     * @return Exception which occurred during processing
     */
    private IOException copyRange(InputStream istream,
                                  ServletOutputStream ostream) {

        // Copy the input stream to the bufferSize stream
        IOException exception = null;
        byte buffer[] = new byte[bufferInputSize];
        int len = buffer.length;
        while (true) {
            try {
                len = istream.read(buffer);
                if (len == -1)
                    break;
                ostream.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * bufferSize stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param reader The reader to read from
     * @param writer The writer to write to
     * @return Exception which occurred during processing
     */
    private IOException copyRange(Reader reader, PrintWriter writer) {

        // Copy the input stream to the bufferSize stream
        IOException exception = null;
        char buffer[] = new char[bufferInputSize];
        int len = buffer.length;
        while (true) {
            try {
                len = reader.read(buffer);
                if (len == -1)
                    break;
                writer.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * bufferSize stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param istream The input stream to read from
     * @param ostream The bufferSize stream to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing
     */
    private IOException copyRange(InputStream istream,
                                  ServletOutputStream ostream,
                                  long start, long end) {

        log("Serving bytes:" + start + "-" + end);

        try {
            istream.skip(start);
        } catch (IOException e) {
            return e;
        }

        IOException exception = null;
        long bytesToRead = end - start + 1;

        byte buffer[] = new byte[bufferInputSize];
        int len = buffer.length;
        while ( (bytesToRead > 0) && (len >= buffer.length)) {
            try {
                len = istream.read(buffer);
                if (bytesToRead >= len) {
                    ostream.write(buffer, 0, len);
                    bytesToRead -= len;
                } else {
                    ostream.write(buffer, 0, (int) bytesToRead);
                    bytesToRead = 0;
                }
            } catch (IOException e) {
                exception = e;
                len = -1;
            }
            if (len < buffer.length)
                break;
        }

        return exception;

    }


    /**
     * Copy the contents of the specified input stream to the specified
     * bufferSize stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param reader The reader to read from
     * @param writer The writer to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing
     */
    private IOException copyRange(Reader reader, PrintWriter writer,
                                  long start, long end) {

        try {
            reader.skip(start);
        } catch (IOException e) {
            return e;
        }

        IOException exception = null;
        long bytesToRead = end - start + 1;

        char buffer[] = new char[bufferInputSize];
        int len = buffer.length;
        while ( (bytesToRead > 0) && (len >= buffer.length)) {
            try {
                len = reader.read(buffer);
                if (bytesToRead >= len) {
                    writer.write(buffer, 0, len);
                    bytesToRead -= len;
                } else {
                    writer.write(buffer, 0, (int) bytesToRead);
                    bytesToRead = 0;
                }
            } catch (IOException e) {
                exception = e;
                len = -1;
            }
            if (len < buffer.length)
                break;
        }

        return exception;

    }



    // ------------------------------------------------------ Range Inner Class


    private class Range {

        public long start;
        public long end;
        public long length;

        /**
         * Validate range.
         */
        public boolean validate() {
            if (end >= length)
                end = length - 1;
            return ( (start >= 0) && (end >= 0) && (start <= end)
                     && (length > 0) );
        }

        public void recycle() {
            start = 0;
            end = 0;
            length = 0;
        }

    }


    // ----------------------------------------------  ResourceInfo Inner Class


    protected class ResourceInfo {


        /**
         * Constructor.
         *
         * @param path Path name of the file
         */
        public ResourceInfo(String path, DirectoryContext context) {
            set(path, context);
        }

        public Resource file;
        public String path;
        public long creationDate;
        public long date;
        public long length;
        public String httpDate;
        public String weakETag;
        public String strongETag;
        public boolean exists;

        public void recycle() {
            file = null;
            path = null;
            creationDate = 0;
            httpDate = null;
            date = 0;
            length = -1;
            weakETag = null;
            strongETag = null;
            exists = false;
        }


        public void set(String path, DirectoryContext context) {
            
            recycle();
            
            this.path = path;
            file = context.lookup(path);
            exists = (file!=null);
            
            if (exists) {
                creationDate = file.getCretaedTime();
                date = file.getLastModifiedDate();
                httpDate = HttpDateFormat.formatDate(date);
                weakETag = file.getETag();
                strongETag = file.getETag(true);
                length = file.getContentLength();
            }
            log(this.toString());
        }


        /**
         * Test if the associated resource exists.
         */
        public boolean exists() {
            return exists;
        }


        /**
         * String representation.
         */
        public String toString() {
            StringBuffer buff = new StringBuffer();
            buff.append("ResourseInfo:");
            buff.append(" path="+path);
            buff.append(" exists="+exists);
            buff.append(" file="+file);
            buff.append(" date="+date);
            buff.append(" length="+length);
            return buff.toString();
        }

        /**
         * Get IS from resource.
         */
        public InputStream getStream() throws IOException {
            if (file != null)
                return (file.streamContent());
            else
                return null;
        }


    }

    class Output {
        ServletOutputStream ostream = null;
        PrintWriter writer = null;
        
        void init(HttpServletResponse response, String contentType) throws IOException {
            // Trying to retrieve the servlet output stream
            try {
                ostream = response.getOutputStream();
            } catch (IllegalStateException e) {
                // If it fails, we try to get a Writer instead if we're
                // trying to serve a text file
                if ( (contentType == null)
                     || (contentType.startsWith("text")) ) {
                    writer = response.getWriter();
                } else {
                    throw e;
                }
            }
        }
        
    }

}


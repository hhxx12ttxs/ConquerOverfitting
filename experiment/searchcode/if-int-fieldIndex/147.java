
package FI.realitymodeler.server;

import FI.realitymodeler.*;
import FI.realitymodeler.common.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;
import javax.servlet.*;
import javax.servlet.http.*;

class PostValues {
    Map<String, String> servletPathTable = null;
    Map<String, String> values = null;
    HttpServletRequest req = null;

    PostValues(Map<String, String> values) {
        this.values = values;
    }

    PostValues(HttpServletRequest req) {
        this.req = req;
    }

    boolean containsParameter(String name) {
        return values != null ? values.containsKey(name) : req.getParameterValues(name) != null;
    }

    String getParameterValue(String name) {
        return values != null ? values.get(name) : Support.getParameterValue(req.getParameterValues(name));
    }

    Enumeration<String> getParameterNames() {
        return values != null ? new IteratorEnumeration<String>(values.keySet().iterator()) : req.getParameterNames();
    }

    public String toString() {
        return getClass().getName() + "{servletPathTable=" + servletPathTable +
            ", values=" + values + ", req=" + req + "}";
    }

}

public class Messaging extends HttpServlet {
    static final long serialVersionUID = 0L;
    static final int TO = 0, CC = 1, BCC = 2;
    static String fieldNames[] = {"to", "cc", "bcc"};
    static String emptyPage = "<html><head><title>Empty</title></head><body></body></html>";
    static String messagesBaseName = "FI.realitymodeler.server.resources.Messages";
    static String mailProtocols[] = {"pop3", "imap4", "msg"};
    static String newsProtocols[] = {"nntp", "msg"};

    Map<String, String> servletPathTable = null;
    ResourceBundle defaultMessages = null;

    /** Returns empty page to client. */
    public static void getEmpty(HttpServletResponse res)
        throws IOException {
        res.setContentLength(emptyPage.length());
        res.getWriter().print(emptyPage);
    }

    /** Returns page containing two frames to client. */
    public static void getFrames(HttpServletRequest req, HttpServletResponse res,
                                 String path, String title, ResourceBundle messages)
        throws IOException {
        PrintWriter writer = res.getWriter();
        writer.println("<html><head><title>" + Support.htmlString(title) + "</title></head><frameset rows=\"*,*\">");
        String href = req.getQueryString();
        if (href != null) href += "&";
        else href = "";
        href = "\"" + path + "?" + href + "done=\" name=\"list\"";
        writer.println("<frame src=" + href + ">");
        writer.println("<frame src=\"" + path + "?empty=\" name=\"message\">");
        writer.println("<noframes><a href=" + href + ">" + Support.htmlString(messages.getString("noFramesVersion")) + "</a></noframes>");
        writer.println("</frameset></html>");
    }

    /** Gets response from url connection returning it to client.
        @param fct filter content type or null */
    public static void getResponse(W3URLConnection uc, HttpServletResponse res,
                                   byte b[], String fct, String msg,
                                   Cookie cookie, boolean forward)
        throws ServletException, IOException {
        InputStream in = uc.getInputStream();
        if (in == null && msg != null) {
            res.sendError(HttpURLConnection.HTTP_OK, msg);
            return;
        }
        res.setStatus(uc.getResponseCode());
        String key;
        if (uc.getResponseCode() == HttpURLConnection.HTTP_OK && forward) res.setContentType("message/rfc822");
        else for (int i = 1; (key = uc.getHeaderFieldKey(i)) != null; i++) res.setHeader(key, uc.getHeaderField(i));
        if (in == null) return;
        if (fct != null) res.setContentType(fct);
        if (cookie != null) res.addCookie(cookie);
        OutputStream out = res.getOutputStream();
        String charsetName = uc.getCharsetName();
        if (uc.getResponseCode() == HttpURLConnection.HTTP_OK && forward) {
            for (int i = 1; (key = uc.getHeaderFieldKey(i)) != null; i++)
                Support.writeBytes(out, key + ": " + uc.getHeaderField(i) + "\r\n", charsetName);
            Support.writeBytes(out, "\r\n", charsetName);
        }
        for (int n; (n = in.read(b)) > 0;) out.write(b, 0, n);
        uc.complete();
    }

    public static void getResponse(W3URLConnection ucs[], HttpServletResponse res, byte b[], String msg)
        throws IOException {
        boolean gotResponse = false;
        for (int i = 0; i < ucs.length; i++)
            if (ucs[i] != null) {
                InputStream in = ucs[i].getInputStream();
                if (in == null) continue;
                if (gotResponse) {
                    for (int n; (n = in.read(b)) > 0;);
                    continue;
                }
                res.setStatus(ucs[i].getResponseCode());
                String key;
                for (int j = 1; (key = ucs[i].getHeaderFieldKey(j)) != null; j++) res.setHeader(key, ucs[i].getHeaderField(j));
                OutputStream out = res.getOutputStream();
                for (int n; (n = in.read(b)) > 0;) out.write(b, 0, n);
                gotResponse = true;
            }
        if (!gotResponse) res.sendError(HttpURLConnection.HTTP_OK, msg);
    }

    /** Gets input from url connection.
        @param fct filter content type or null */
    public static void get(HttpServletRequest req, HttpServletResponse res,
                           W3URLConnection uc, String fct, Cookie cookie, boolean forward)
        throws ServletException, IOException {
        Enumeration headerNameEnum = req.getHeaderNames();
        while (headerNameEnum.hasMoreElements()) {
            String name = (String)headerNameEnum.nextElement();
            Enumeration headerEnum = req.getHeaders(name);
            while (headerEnum.hasMoreElements())
                uc.addRequestProperty(name, (String)headerEnum.nextElement());
        }
        byte b[] = new byte[Support.bufferLength];
        getResponse(uc, res, b, fct, null, cookie, forward);
    }

    /** Puts client data to url connection. */
    public static void put(Servlet servlet, HttpServletRequest req, HttpServletResponse res, String msg)
        throws ServletException, IOException {
        StringTokenizer st = new StringTokenizer(req.getPathInfo(), "/");
        String protocol = st.nextToken(), path = st.nextToken("");
        W3URLConnection uc = null;
        try {
            uc = W3URLConnection.openConnection(new URL(protocol + (protocol.equals("mailto") || protocol.equals("sms") ? ":" : ":/") + path));
            uc.setServletContext(servlet.getServletConfig().getServletContext());
            uc.setHttpSession(req.getSession(true));
            Enumeration headerNameEnum = req.getHeaderNames();
            while (headerNameEnum.hasMoreElements()) {
                String name = (String)headerNameEnum.nextElement();
                Enumeration headerEnum = req.getHeaders(name);
                while (headerEnum.hasMoreElements())
                    uc.addRequestProperty(name, (String)headerEnum.nextElement());
            }
            byte b[] = new byte[Support.bufferLength];
            try {
                OutputStream out = uc.getOutputStream();
                InputStream in = req.getInputStream();
                for (int n; (n = in.read(b)) > 0;) out.write(b, 0, n);
                out.close();
            } catch (LoginException ex) {}
            getResponse(uc, res, b, null, msg, null, false);
        } finally {
            if (uc != null) uc.disconnect();
        }
    }

    private static void transferBody(HeaderList headerList, InputStream in, OutputStream out, String boundary)
        throws IOException, ParseException {
        String ct = headerList.getHeaderValue("content-type"), ct0 = null;
        Map<String, String> ctParams = new HashMap<String, String>();
        if (ct != null) ct0 = Support.getParameters(ct, ctParams).toLowerCase();
        HeaderList headerList1 = new HeaderList();
        Decoder decoder = Support.getDecoder(headerList, headerList1);
        if (ct0 != null)
            if (ct0.startsWith("multipart/")) {
                String boundary1 = ctParams.get("boundary");
                if (boundary1 == null) throw new IOException("No boundary in multipart content");
                boundary1 = "--" + boundary1;
                InputStream in1 = new ConvertInputStream(in, boundary1, "\r", true, true);
                boundary1 = "\r\n" + boundary1;
                boundary += "_";
                headerList.replace(new Header("Content-Type", ct0 + "; boundary=\"" + boundary + "\""));
                String boundary2 = "--" + boundary;
                Support.sendHeaderList(out, headerList);
                while (in1.read() != -1);
                for (;;) {
                    int c;
                    if ((c = in.read()) == '-' && (c = in.read()) == '-') break;
                    while (c != -1 && c != '\n') c = in.read();
                    if (c == -1) break;
                    in1 = new ConvertInputStream(in, boundary1, false, true);
                    headerList = new HeaderList(in1);
                    Support.writeBytes(out, "\r\n" + boundary2 + "\r\n", null);
                    transferBody(headerList, in1, out, boundary);
                } while (in.read() != -1);
                Support.writeBytes(out, "\r\n" + boundary2 + "--\r\n", null);
                out.flush();
                return;
            } else if (ct0.equals("message/rfc822")) {
                Support.sendHeaderList(out, headerList);
                transferBody(headerList1, in, out, boundary);
                return;
            } else if (ct0.equals("message/external-body")) {
                Support.sendHeaderList(out, headerList);
                transferBody(headerList1, in, out, boundary);
                return;
            }
        if (ct0 == null || ct0.startsWith("text/")) {
            headerList.replace(new Header("Content-Transfer-Encoding", "quoted-printable"));
            Support.sendHeaderList(out, headerList);
            out = new EncoderOutputStream(out, new QPEncoder());
        } else {
            headerList.replace(new Header("Content-Transfer-Encoding", "base64"));
            Support.sendHeaderList(out, headerList);
            out = new EncoderOutputStream(out, new BASE64Encoder());
        }
        if (decoder == null) {
            byte b[] = new byte[Support.bufferLength];
            for (int n; (n = in.read(b)) > 0;) out.write(b, 0, n);
        } else decoder.decodeStream(in, out);
        out.close();
    }

    /** Returns output stream sending data to mail- and news servers. */
    static W3URLConnection[] getSendStream(ServletContext servletContext, HttpServletRequest req,
                                            Map<String, String> values, String charsetName,
                                            OutputStream outVal[], boolean multi)
        throws ServletException, IOException, InterruptedException, ParseException {
        PostValues postValues = values != null ? new PostValues(values) : new PostValues(req);
        boolean forward = postValues.containsParameter("forward");
        int targets = (postValues.containsParameter("mail") ? 1 : 0) | (postValues.containsParameter("news") ? 2 : 0);
        if (targets == 0) throw new ServletException("No targets found");
        if (!postValues.containsParameter("to") || !postValues.containsParameter("cc") || !postValues.containsParameter("bcc") ||
            (targets & 2) != 0 && !postValues.containsParameter("host") ||
            !postValues.containsParameter("subject") || !postValues.containsParameter("from")) throw new ServletException("Invalid form contents");
        StringBuffer mailto = new StringBuffer(), msg = new StringBuffer(), to = new StringBuffer(), cc = new StringBuffer(), bcc = new StringBuffer(), newsgroups = new StringBuffer();
        String address[] = new String[2], host = postValues.getParameterValue("host"),
            sendHost = postValues.getParameterValue("sendHost");
        if (host == null || (host = host.trim()).equals("")) host = req.getServerName();
        if (sendHost == null || (sendHost = sendHost.trim()).equals("")) sendHost = null;
        boolean secureSend = postValues.containsParameter("secureSend");
        for (int fieldIndex = fieldNames.length - 1; fieldIndex >= 0; fieldIndex--) {
            String fieldName = fieldNames[fieldIndex], fieldValue = postValues.getParameterValue(fieldName);
            if (fieldValue == null) continue;
            StringTokenizer st = new StringTokenizer(fieldValue, ",");
            while (st.hasMoreTokens()) {
                String s = st.nextToken().trim();
                StringBuffer header;
                int x;
                if (s.indexOf('@') != -1) {
                    StringBuffer sb;
                    if (s.startsWith("@")) {
                        if ((targets & ((s = s.substring(1)).indexOf('@') != -1 ? 1 : 2)) == 0) continue;
                        sb = msg;
                    } else {
                        if ((targets & 1) == 0) continue;
                        sb = mailto;
                    }
                    if (W3URLConnection.getAddress(address, s, host)[0] == null) throw new ServletException("Invalid address " + s);
                    if (sb.length() > 0) sb.append(',');
                    sb.append(address[0]);
                    s = address[0];
                    if (address[1] != null && !address[1].equals(address[0])) s += " (" + address[1] + ")";
                    header = fieldIndex == 0 ? to : cc;
                } else if ((targets & 2) == 0) continue;
                else header = newsgroups;
                if (fieldIndex == 2) continue;
                if (header.length() > 0) header.append(',');
                header.append(s);
            }
        }
        String from = null, organization = null,
            replyTo = postValues.getParameterValue("replyTo"),
            subject = postValues.getParameterValue("subject"),
            priority = postValues.getParameterValue("priority");
        StringTokenizer st = new StringTokenizer(postValues.getParameterValue("from"), ",");
        if (st.hasMoreTokens()) {
            from = st.nextToken().trim();
            if (st.hasMoreTokens()) organization = st.nextToken().trim();
        }
        W3URLConnection.getAddress(address, from, host);
        boolean copy = postValues.containsParameter("copy") && address[0] != null;
        Vector<W3URLConnection> ucVec = new Vector<W3URLConnection>();
        if ((targets & 1) != 0 && mailto.length() > 0 || copy) {
            if (copy) {
                if (mailto.length() > 0) mailto.append(',');
                mailto.append(address[0]);
            }
            W3URLConnection uc = new W3SmtpURLConnection(new URL((secureSend ? "s" : "") + "mailto:" + (sendHost != null ? "//" + sendHost + "/" : "") + mailto.toString()));
            uc.setServletContext(servletContext);
            uc.setHttpSession(req.getSession(true));
            String s;
            if ((s = req.getHeader("authorization")) != null) uc.setRequestProperty("Authorization", s);
            String notificationTo = null;
            if (from != null) uc.setRequestProperty("From", notificationTo = Support.encodeWords(from, charsetName));
            if (replyTo != null && !replyTo.equals("")) uc.setRequestProperty("Reply-To", notificationTo = Support.encodeWords(replyTo, charsetName));
            uc.setRequestProperty("To", Support.encodeWords(to.toString(), charsetName));
            if (cc.length() > 0) uc.setRequestProperty("CC", Support.encodeWords(cc.toString(), charsetName));
            if (notificationTo != null)  uc.setRequestProperty("Disposition-Notification-To", notificationTo);
            ucVec.addElement(uc);
        }
        if ((targets & 2) != 0 && newsgroups.length() > 0) {
            W3URLConnection uc = new W3NntpURLConnection(new URL("nntp://" + postValues.getParameterValue("host") + "/" + newsgroups.toString()));
            uc.setServletContext(servletContext);
            uc.setHttpSession(req.getSession(true));
            String s;
            if ((s = req.getHeader("authorization")) != null) uc.setRequestProperty("Authorization", s);
            if (from != null) uc.setRequestProperty("From", Support.encodeWords(from, charsetName));
            if (replyTo != null && !replyTo.equals("")) uc.setRequestProperty("Reply-To", Support.encodeWords(replyTo, charsetName));
            ucVec.addElement(uc);
        }
        if (msg.length() > 0) {
            W3URLConnection uc = new W3MsgURLConnection(new URL("msg:" + msg.toString()));
            uc.setServletContext(servletContext);
            uc.setHttpSession(req.getSession(true));
            String s;
            if ((s = req.getHeader("authorization")) != null) uc.setRequestProperty("Authorization", s);
            if (from != null) uc.setRequestProperty("From", Support.encodeWords(from, charsetName));
            if (replyTo != null && !replyTo.equals("")) uc.setRequestProperty("Reply-To", Support.encodeWords(replyTo, charsetName));
            uc.setRequestProperty("To", Support.encodeWords(to.toString(), charsetName));
            if (cc.length() > 0) uc.setRequestProperty("CC", Support.encodeWords(cc.toString(), charsetName));
            ucVec.addElement(uc);
        }
        if (ucVec.isEmpty()) throw new ServletException("No recipients specified");
        W3URLConnection ucs[] = new W3URLConnection[ucVec.size()];
        for (int i = 0; i < ucs.length; i++) ucs[i] = ucVec.elementAt(i);
        OutputStream outs[] = new OutputStream[ucs.length];
        for (int i = 0; i < outs.length; i++) {
            if (organization != null) ucs[i].setRequestProperty("Organization", Support.encodeWords(organization, charsetName));
            ucs[i].setRequestProperty("Subject", Support.encodeWords(subject, charsetName));
            ucs[i].setRequestProperty("Content-Type", multi ? "multipart/mixed; boundary=\"_\"" : ("text/plain; charset=" + (charsetName != null ? charsetName : "iso-8859-1")));
            ucs[i].setRequestProperty("Content-Transfer-Encoding", multi ? "8bit" : "quoted-printable");
            if (priority != null) ucs[i].setRequestProperty("X-Priority", priority);
            outs[i] = ucs[i].getOutputStream();
            if (outs[i] == null) {
                outVal[0] = null;
                return ucs;
            }
        }
        outVal[0] = new CloneOutputStream(outs);
        if (!forward) return ucs;
        st = new StringTokenizer(req.getPathInfo(), "/");
        String protocol = st.nextToken(), path = st.nextToken("").trim();
        OutputStream out = outVal[0];
        W3URLConnection uc = null;
        try {
            uc = W3URLConnection.openConnection(new URL(protocol + ":/" + Support.canonizePath(path) + "?forward="));
            uc.setServletContext(servletContext);
            uc.setHttpSession(req.getSession(true));
            String s;
            if ((s = req.getHeader("authorization")) != null) uc.setRequestProperty("Authorization", s);
            Support.writeBytes(out, "\r\n--_\r\n", null);
            Support.writeBytes(out, "Content-Type: message/rfc822\r\n", null);
            Support.writeBytes(out, "Content-Disposition: inline\r\n\r\n", null);
            InputStream in = uc.getInputStream();
            HeaderList headerList = uc.getHeaderList();
            transferBody(headerList, in, out, "_");
            out.flush();
        } finally {
            if (uc != null) uc.disconnect();
        }
        return ucs;
    }

    public static void get(Servlet servlet, HttpServletRequest req, HttpServletResponse res,
                           ResourceBundle defaultMessages, String name, String hostServer,
                           Map<String, String> servletPathTable)
        throws ServletException, IOException, InterruptedException {
        if (req.getParameterValues("empty") != null) {
            getEmpty(res);
            return;
        }
        byte b[] = new byte[Support.bufferLength];
        boolean hasHost;
        String action, root = "", path = req.getPathInfo(), s,
            categories = (s = Support.getParameterValue(req.getParameterValues("categories"))) != null ? s  : "",
            category = (s = Support.getParameterValue(req.getParameterValues("category"))) != null ? s : "",
            date = (s = Support.getParameterValue(req.getParameterValues("date"))) != null ? s.trim() : "",
            from = (s = Support.getParameterValue(req.getParameterValues("from"))) != null ? s.trim() : "",
            replyTo = (s = Support.getParameterValue(req.getParameterValues("replyTo"))) != null ? s.trim() : "",
            host = (hasHost = (s = Support.getParameterValue(req.getParameterValues("host"))) != null && !(s = s.trim()).equals("")) ? s :
            hostServer != null ? hostServer : req.getServerName(),
            sendHost = (s = Support.getParameterValue(req.getParameterValues("sendHost"))) != null ? s : "",
            group = (s = Support.getParameterValue(req.getParameterValues("group"))) != null ? s : "",
            keywords = (s = Support.getParameterValue(req.getParameterValues("keywords"))) != null ? s.trim() : "",
            itemsInPage = (s = Support.getParameterValue(req.getParameterValues("itemsInPage"))) != null ? s.trim() : "10",
            cc = (s = Support.getParameterValue(req.getParameterValues("cc"))) != null ? s.trim() : "",
            bcc = (s = Support.getParameterValue(req.getParameterValues("bcc"))) != null ? s.trim() : "",
            protocolName = (s = Support.getParameterValue(req.getParameterValues("protocol"))) != null ? s.trim() : (name.equals("mail") ? "pop3" : "nntp"),
            protocol = protocolName,
            sign = (s = Support.getParameterValue(req.getParameterValues("sign"))) != null ? s.trim() : "",
            subject = (s = Support.getParameterValue(req.getParameterValues("subject"))) != null ? s.trim() : "",
            to = (s = Support.getParameterValue(req.getParameterValues("to"))) != null ? s.trim() : "",
            alsoto = (s = Support.getParameterValue(req.getParameterValues("alsoto"))) != null ? s.trim() : "",
            alsocc = (s = Support.getParameterValue(req.getParameterValues("alsocc"))) != null ? s.trim() : "",
            text = (s = Support.getParameterValue(req.getParameterValues("text"))) != null ? s.trim() : "",
            hostName = name.equals("news") ? req.getServerName() : host,
            charsetName = null;
        boolean secure = req.getParameterValues("secure") != null,
            secureSend = req.getParameterValues("secureSend") != null;
        if (secure) protocol += "s";
        if (path == null) path = "";
        ResourceBundle messages = W3URLConnection.getLanguage(req, res, defaultMessages, messagesBaseName);
        if (messages == null) return;
        try {
            charsetName = messages.getString("CHARSET");
        } catch (MissingResourceException ex) {}
        if (charsetName == null) charsetName = "UTF-8";
        if (!text.equals("") && req.getParameterValues("new") == null) {
            OutputStream outVal[] = new OutputStream[1];
            W3URLConnection ucs[] = null;
            try {
                ucs = getSendStream(servlet.getServletConfig().getServletContext(), req, null, charsetName, outVal, false);
                if (outVal[0] != null) {
                    outVal[0].write(new QPEncoder().encode(text.getBytes()).getBytes());
                    outVal[0].close();
                }
                getResponse(ucs, res, b, messages.getString("itemSent"));
            } catch (ParseException ex) {
                throw new ServletException(ex);
            } finally {
                if (ucs != null)
                    for (int i = 0; i < ucs.length; i++)
                        if (ucs[i] != null) ucs[i].disconnect();
            }
            return;
        }
        boolean forward = req.getParameterValues("forward") != null;
        int number = (s = Support.getParameterValue(req.getParameterValues("number"))) != null && !(s = s.trim()).equals("") ? Integer.parseInt(s) : 1;
        if (number > 100) throw new ServletException("Number of attachments exceeds the maximum 100");
        boolean fromSet;
        if (from.equals("")) {
            Cookie cookie = W3URLConnection.getCookie(servlet.getClass().getName(), req.getCookies());
            if (cookie != null) from = java.net.URLDecoder.decode(cookie.getValue(), "8859_1");
            else if (req.getRemoteUser() != null) from = req.getRemoteUser() + "@" + hostName;
            fromSet = false;
        } else fromSet = true;
        boolean mobile = (s = req.getHeader("user-agent")) != null && s.trim().equalsIgnoreCase("mobile") ||
            req.getParameterValues("mobile") != null;
        StringTokenizer st = new StringTokenizer(path, "/");
        int tc = st.countTokens();
        if (tc > 0) protocol = st.nextToken();
        if (tc > 1) host = st.nextToken();
        else root += host + "/";
        int tc1 = tc;
        if (!name.equals("news")) tc1 = tc + 1;
        else if (tc > 2) group = st.nextToken();
        else if (!group.equals("")) root += group + "/";
        String index = tc1 > 3 ? st.nextToken() : null, id = tc1 > 4 ? st.nextToken() : null;
        int i = (action = req.getRequestURI()).indexOf('?');
        if (i != -1) action = action.substring(0, i).trim();
        if (path.length() > 0 && (i = path.indexOf('/', 1)) != -1) path = path.substring(i);
        else path = "/";
        if (!root.equals("") && !path.endsWith("/")) path += "/";
        path += root;
        if (tc == 0) root = protocol + "/" + root;
        if (!action.endsWith("/")) root = action.substring(action.lastIndexOf('/') + 1) + "/" + root;
        if (!name.equals("news") && req.getParameterValues("delete") != null) {
            if (id == null) {
                res.sendError(HttpURLConnection.HTTP_OK, messages.getString("invalidFormContents"));
                return;
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            W3URLConnection uc = null;
            try {
                uc = W3URLConnection.openConnection(new URL(protocol + ":/" + path));
                Support.writeBytes(bout, "from=&category=destruction&" + id + "=", uc.getCharsetName());
                uc.setServletContext(servlet.getServletConfig().getServletContext());
                uc.setHttpSession(req.getSession(true));
                if ((s = req.getHeader("authorization")) != null) uc.setRequestProperty("Authorization", s);
                uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                uc.setRequestProperty("Content-Length", String.valueOf(bout.size()));
                try {
                    OutputStream out = uc.getOutputStream();
                    bout.writeTo(out);
                    out.close();
                } catch (LoginException ex) {}
                getResponse(uc, res, b, null, messages.getString("itemDeleted"), null, false);
            } finally {
                if (uc != null) uc.disconnect();
            }
            return;
        }
        InputStream in = null;
        W3URLConnection uc = null;
        if (req.getParameterValues("form") != null)
            try {
                String title = null;
                for (s = to;; s = from) {
                    String address[] = new String[2];
                    st = new StringTokenizer(s, ",");
                    if (st.hasMoreTokens()) {
                        W3URLConnection.getAddress(address, st.nextToken(), hostName);
                        if ((title = address[s == to ? 1 : 0]) != null) {
                            if (st.hasMoreTokens()) title += "...";
                            break;
                        }
                    }
                    if (s == from) break;
                }
                res.setHeader("Window-Target", "composition");
                res.setContentType(Support.htmlType);
                boolean quote = false;
                String quoteCharset = null;
                if (id != null && req.getParameterValues("auto") != null) {
                    uc = W3URLConnection.openConnection(new URL(protocol + ":/" + path + "?" + req.getQueryString() + "&plain="));
                    uc.setServletContext(servlet.getServletConfig().getServletContext());
                    uc.setHttpSession(req.getSession(true));
                    uc.setUseCaches(true);
                    if ((s = req.getHeader("authorization")) != null) uc.setRequestProperty("Authorization", s);
                    in = uc.getInputStream();
                    if (uc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        String contentType = uc.getHeaderField("content-type");
                        if (contentType != null) {
                            Map<String, String> params = new HashMap<String, String>();
                            Support.getParameters(contentType, params);
                            quoteCharset = params.get("charset");
                        }
                        quote = true;
                    } else if (uc.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        getResponse(uc, res, b, null, null, null, false);
                        return;
                    }
                    res.setCharacterEncoding(uc.getCharsetName());
                }
                PrintWriter writer = res.getWriter();
                s = Support.htmlString(messages.getString("sendItem"));
                writer.println("<html><head>");
                writer.println("<meta http-equiv=\"Window-Target\" content=\"composition\">");
                writer.println("<style type=\"text/css\">textarea {font: bold verdana}</style>");
                writer.println("<title>" + (title != null ? title : s) + "</title></head><body>");
                writer.println("<h1>" + s + "</h1>");
                writer.println("<form action=\"" + action + "\" " +
                               (mobile ? "method=get" : "enctype=\"multipart/form-data\" method=post") + ">");
                if (!mobile) writer.println("<input name=number type=hidden value=\"" + number + "\">");
                if (req.getParameterValues("auto") != null) writer.println("<input name=auto type=hidden value=on>");
                if (name.equals("news")) {
                    writer.println("<input name=news type=checkbox checked>" +
                                   Support.htmlString(messages.getString("postToGroups")) + "<br>");
                    writer.println("<input name=mail type=checkbox>" +
                                   Support.htmlString(messages.getString("mailToPersons")) + "<br>");
                } else writer.println("<input name=mail type=hidden value=on>");
                if (charsetName != null) writer.println("<input name=charset type=hidden value=\"" + Support.htmlString(charsetName) + "\">");
                if (sendHost != null) writer.println("<input name=sendHost type=hidden value=\"" + Support.htmlString(sendHost) + "\">");
                if (req.getParameterValues("secureSend") != null) writer.println("<input name=secureSend type=hidden value=on>");
                writer.println("<input name=copy type=checkbox" +
                               (req.getParameterValues("copy") != null ? " checked" : "") + ">" +
                               Support.htmlString(messages.getString("sendCopyToSelf")) + "<br>");
                if (!mobile) {
                    writer.println("<input name=use type=checkbox" +
                                   (req.getParameterValues("use") != null ? " checked" : "") + ">" +
                                   Support.htmlString(messages.getString("useSignatureFile")) + "<br>");
                    writer.println("<input type=reset value=\"" + Support.htmlString(messages.getString("defaults")) + "\">");
                    writer.println("<input name=new type=submit value=\"" + Support.htmlString(messages.getString("getNewForm")) + "\">");
                    if (id != null) writer.println("<input name=manual type=submit value=\"" + Support.htmlString(messages.getString("quoteOriginal")) + "\">");
                }
                writer.println("<input type=submit value=\"" + Support.htmlString(messages.getString("sendItem")) + "\">");
                if (id != null) writer.println("<input name=forward type=submit value=\"" + Support.htmlString(messages.getString("forwardItem")) + "\">");
                writer.println("<table>");
                if (name.equals("news")) writer.println("<tr><th>" + Support.htmlString(messages.getString("host")) + "</th><td><input name=host type=text value=\"" + Support.htmlString(host) + "\" size=55></td></tr>");
                if (req.getParameterValues("also") != null) {
                    if (!alsoto.equals("")) to += (to.equals("") ? "" : ",") + alsoto;
                    if (!alsocc.equals("")) cc += (cc.equals("") ? "" : ",") + alsocc;
                }
                writer.println("<tr><th>" + Support.htmlString(messages.getString("priority")) + "</th><td><select name=priority>");
                for (i = 1; i <= 5; i++) writer.println("<option" + (i == 3 ? " selected" : "") + ">" + i + "</option>");
                writer.println("</select></td></tr>");
                writer.println("<tr><th>" + Support.htmlString(messages.getString("to")) + "</th><td><input name=to type=text value=\"" + Support.htmlString(to) + "\" size=55></td></tr>");
                writer.println("<tr><th>" + Support.htmlString(messages.getString("cc")) + "</th><td><input name=cc type=text value=\"" + Support.htmlString(cc) + "\" size=55></td></tr>");
                writer.println("<tr><th>" + Support.htmlString(messages.getString("bcc")) + "</th><td><input name=bcc type=text value=\"" + Support.htmlString(bcc) + "\" size=55></td></tr>");
                writer.println("<tr><th>" + Support.htmlString(messages.getString("from")) + "</th><td><input name=from type=text value=\"" + Support.htmlString(from) + "\" size=55></td></tr>");
                writer.println("<tr><th>" + Support.htmlString(messages.getString("replyTo")) + "</th><td><input name=replyTo type=text value=\"" + Support.htmlString(replyTo) + "\" size=55></td></tr>");
                writer.println("<tr><th>" + Support.htmlString(messages.getString("subject")) + "</th><td><input name=subject type=text value=\"" + Support.htmlString(subject) + "\" size=55></td></tr>");
                if (!mobile) {
                    writer.println("<tr><th>" + Support.htmlString(messages.getString("number")) + "</th><td><input name=number type=text value=\"" + number + "\" size=55></td></tr>");
                    for (i = 1; i <= number; i++)
                        writer.println("<tr><th>" + Support.htmlString(messages.getString("attachment")) + "</th><td><input name=file" + i + " type=file accept=\"*/*\" size=55 maxlength=275></td></tr>");
                }
                writer.println("</table>");
                writer.println("<textarea name=\"text\" rows=\"20\" cols=\"76\" wrap=\"hard\">");
                writer.flush();
                Writer textWriter = new HtmlWriter(writer);
                if (charsetName != null) {
                    String charsetKey = charsetName.toLowerCase();
                    if (charsetKey.equals("windows-1251") || charsetKey.equals("koi8-r"))
                        textWriter = new CyrillicWriter(textWriter);
                }
                if (quote) {
                    char chars[] = new char[Support.bufferLength];
                    Reader reader = quoteCharset != null ? new InputStreamReader(in, quoteCharset) : new InputStreamReader(in);
                    for (int n; (n = reader.read(chars)) > 0;) textWriter.write(chars, 0, n);
                } else textWriter.write(text);
                textWriter.flush();
                writer.println("</textarea>");
                if (!mobile) writer.println("<table><tr><th>" + Support.htmlString(messages.getString("signature")) +
                                            "</th><td><input name=sign type=file value=\"" + Support.htmlString(sign) + "\" accept=\"*/*\" size=55 maxlength=275></td></tr></table>");
                writer.println("</form></body></html>");
                writer.flush();
                return;
            } finally {
                if (uc != null) uc.disconnect();
            }
        if ((name.equals("news") ? tc < 2 && (group.equals("") && keywords.equals("") || req.getParameterValues("ask") != null) :
             tc == 0 && req.getParameterValues("ask") != null) && req.getParameterValues("go") == null || req.getParameterValues("new") != null) {
            if (req.getParameterValues("new") != null && req.getParameterValues("ask") == null) {
                if (tc < 2 && !protocol.equals("") && hasHost) {
                    res.sendRedirect(req.getRequestURI() + "?ask=" + (req.getQueryString() != null ? "&" + req.getQueryString() : ""));
                    return;
                }
            }
            PrintWriter writer = res.getWriter();
            s = Support.htmlString(messages.getString(name));
            writer.println("<html><head><title>" + (!category.equals("") ? category : !host.equals("") ? host : s) + "</title></head><body>");
            writer.println("<h1>" + s + "</h1>");
            writer.println("<form action=\"" + action + "\" method=get>");
            writer.println("<input name=go type=hidden value=on>");
            writer.println("<input name=only type=checkbox" + (req.getParameterValues("only") != null ? " checked" : "") + ">" + Support.htmlString(messages.getString("getOnlyNewItems")) + "<br>");
            writer.println("<input name=copy type=checkbox" + (req.getParameterValues("copy") != null ? " checked" : "") + ">" + Support.htmlString(messages.getString("sendCopyToSelf")) + "<br>");
            writer.println("<input name=itemOffset type=checkbox value=0 " + (req.getParameterValues("itemOffset") != null ? " checked" : "") + ">" + Support.htmlString(messages.getString("paged")) + "<br>");
            if (!mobile) {
                writer.println("<input name=secure type=checkbox" + (req.getParameterValues("secure") != null ? " checked" : "") + ">" + Support.htmlString(messages.getString("secure")) + "<br>");
                writer.println("<input name=secureSend type=checkbox" + (req.getParameterValues("secureSend") != null ? " checked" : "") + ">" + Support.htmlString(messages.getString("secureSend")) + "<br>");
                writer.println("<input name=frames type=checkbox" +
                               (req.getParameterValues("frames") != null ? " checked" : "") + ">" +
                               Support.htmlString(messages.getString("useFrames")) + "<br>");
                writer.println("<input name=header type=checkbox" +
                               (req.getParameterValues("header") != null ? " checked" : "") + ">" +
                               Support.htmlString(messages.getString("showHeader")) + "<br>");
                writer.println("<input name=auto type=checkbox" +
                               (req.getParameterValues("auto") != null ? " checked" : "") + ">" +
                               Support.htmlString(messages.getString("quoteAutomatically")) + "<br>");
                writer.println("<input type=reset value=\"" + Support.htmlString(messages.getString("defaults")) + "\">");
                writer.println("<input name=new type=submit value=\"" + Support.htmlString(messages.getString("getNewForm")) + "\">");
                writer.println("<input name=form type=submit value=\"" + Support.htmlString(messages.getString("getSendingForm")) + "\">");
                writer.println("<input name=contents type=submit value=\"" + Support.htmlString(messages.getString("getAllContents")) + "\">");
            }
            writer.println("<input type=submit value=\"" + Support.htmlString(messages.getString("getItems")) + "\">");
            writer.println("<table>");
            if (!mobile) {
                writer.print("<tr><th>" + Support.htmlString(messages.getString("protocol")) + "</th><td>");
                if (tc < 1) {
                    writer.println("<select name=protocol>");
                    String protocols[] = name.equals("mail") ? mailProtocols : newsProtocols;
                    for (i = 0; i < protocols.length; i++)
                        writer.println("<option" + (protocolName.equals(protocols[i]) ? " selected" : "") + ">" + protocols[i] + "</option>");
                    writer.print("</select>");
                } else writer.print(protocol);
                writer.println("</td></tr>");
            }
            if (!mobile || host.equals("")) writer.println("<tr><th>" + Support.htmlString(messages.getString("host")) + "</th><td>" + (tc < 2 ? "<input name=host type=text value=\"" + Support.htmlString(host) + "\" size=55>" : host) + "</td></tr>");
            if (!mobile || sendHost.equals("")) writer.println("<tr><th>" + Support.htmlString(messages.getString("sendHost")) + "</th><td>" + (tc < 2 ? "<input name=sendHost type=text value=\"" + Support.htmlString(sendHost) + "\" size=55>" : sendHost) + "</td></tr>");
            if (protocol.equals("nntp")) writer.println("<tr><th>" + Support.htmlString(messages.getString("group")) + "</th><td>" + (tc < 3 ? "<input name=group type=text value=\"" + Support.htmlString(group) + "\" size=55>" : Support.htmlString(group)) + "</td></tr>");
            writer.println("<tr><th>" + Support.htmlString(messages.getString("keywords")) + "</th><td><input name=keywords type=text value=\"" + Support.htmlString(keywords) + "\" size=55></td></tr>");
            writer.println("<tr><th>" + Support.htmlString(messages.getString("itemsInPage")) + "</th><td><input name=itemsInPage type=text value=\"" + Support.htmlString(itemsInPage) + "\" size=55></td></tr>");
            writer.println("<tr><th>" + Support.htmlString(messages.getString("date")) + "</th><td><input name=date type=text value=\"" + Support.htmlString(date) + "\" size=55></td><td>(" + W3URLConnection.userDateFormats[0].toPattern() + ")</td></tr>");
            writer.println("<tr><th>" + Support.htmlString(messages.getString("from")) + "</th><td><input name=from type=text value=\"" + Support.htmlString(from) + "\" size=55></td></tr>");
            writer.println("<tr><th>" + Support.htmlString(messages.getString("replyTo")) + "</th><td><input name=replyTo type=text value=\"" + Support.htmlString(replyTo) + "\" size=55></td></tr>");
            writer.println("<tr><th>" + Support.htmlString(messages.getString("to")) + "</th><td><input name=to type=text value=\"" + Support.htmlString(to) + "\" size=55></td></tr>");
            writer.println("<tr><th>" + Support.htmlString(messages.getString("category")) + "</th><td><select name=category><option>");
            writer.println("<option value=\"Sent Mail\">" + Support.htmlString(messages.getString("sentMail")));
            StringTokenizer categoryTok = new StringTokenizer(categories, ",");
            while (categoryTok.hasMoreTokens()) writer.println("<option>" + categoryTok.nextToken().trim());
            writer.println("</select></td></tr>");
            if (!mobile) {
                writer.println("<tr><th>" + Support.htmlString(messages.getString("categories")) + "</th><td><input name=categories type=text value=\"" + Support.htmlString(categories) + "\" size=55 maxlength=275></td></tr>");
                writer.println("<tr><th>" + Support.htmlString(messages.getString("signature")) + "</th><td><input name=sign type=text value=\"" + Support.htmlString(sign) + "\" size=55 maxlength=275></td><td>" +
                               Support.htmlString(messages.getString("(filename)")) + "</td></tr>");
            }
            writer.println("</table>");
            writer.println("</form></body></html>");
            return;
        }
        if (req.getParameterValues("contents") != null) {
            if (name.equals("news") ? tc < 3 && group != null && !group.equals("") : tc < 2) {
                String uri = req.getRequestURI();
                if (!uri.endsWith("/")) uri = uri.substring(0, uri.lastIndexOf('/') + 1);
                uri += root;
                if ((s = req.getQueryString()) != null && !(s = s.trim()).equals("")) uri += "?" + s;
                res.sendRedirect(uri);
                return;
            }
        } else {
            if (req.getParameterValues("frames") != null && req.getParameterValues("done") == null) {
                getFrames(req, res, action, !group.equals("") ? group : !host.equals("") ? host : messages.getString(name), messages);
                return;
            }
        }
        if ((s = req.getQueryString()) != null && !(s = s.trim()).equals("")) path += "?" + s;
        uc = null;
        try {
            uc = W3URLConnection.openConnection(new URL(protocol + ":/" + path));
            uc.setServletContext(servlet.getServletConfig().getServletContext());
            uc.setHttpSession(req.getSession(true));
            uc.setUseCaches(true);
            Cookie cookie;
            if (fromSet && tc == 0) {
                cookie = new Cookie(servlet.getClass().getName(), URLEncoder.encode(from, "8859_1"));
                cookie.setMaxAge(1000 * 24 * 60 * 60);
            } else cookie = null;
            uc.setRequestRoot(root);
            uc.setServletPathTable(servletPathTable);
            if (!category.equals("")) uc.setRequestProperty("X-Category", category);
            if (!categories.equals("")) uc.setRequestProperty("X-Categories", categories);
            s = req.getHeader("cache-control");
            if (s != null) uc.setRequestProperty("Cache-Control", s);
            s = req.getHeader("pragma");
            if (s != null) uc.setRequestProperty("Pragma", s);
            get(req, res, uc, keywords.equals("") ? null : "text/search; start=<!--start-->; end=<!--end-->; search=" + keywords, cookie, forward);
            uc.complete();
        } finally {
            if (uc != null) uc.disconnect();
        }
    }

    /** Sends item to MSG-, SMTP- and/or NNTP servers or deletes items. */
    public static void post(Servlet servlet, HttpServletRequest req, HttpServletResponse res,
                            ResourceBundle defaultMessages, String protocol)
        throws ServletException, IOException, InterruptedException {
        W3URLConnection ucs[] = null;
        try {
            ResourceBundle messages = W3URLConnection.getLanguage(req, res, defaultMessages, messagesBaseName);
            if (messages == null) return;
            String charsetName = null;
            try {
                charsetName = messages.getString("CHARSET");
            } catch (MissingResourceException ex) {}
            if (charsetName == null) charsetName = "UTF-8";
            byte b[] = new byte[Support.bufferLength];
            InputStream in = req.getInputStream();
            Map<String, String> values = new HashMap<String, String>(),
                params = new HashMap<String, String>();
            String ct = Support.getParameters(req.getHeader("content-type"), params).toLowerCase();
            if (ct.equals("application/x-www-form-urlencoded")) {
                if (!req.getPathInfo().endsWith("/move")) {
                    FI.realitymodeler.common.URLDecoder urld = new FI.realitymodeler.common.URLDecoder(true);
                    for (;;) {
                        String name = new String(urld.decodeStream(in), "8859_1"), value;
                        if (urld.c != '=' || name.equals("text")) break;
                        value = new String(urld.decodeStream(in), "8859_1");
                        if (urld.c != '&') break;
                        values.put(name, value);
                    }
                    OutputStream outVal[] = new OutputStream[1];
                    ucs = getSendStream(servlet.getServletConfig().getServletContext(), req,
                                        values, charsetName, outVal, false);
                    if (outVal[0] != null) {
                        InputStream in1 = new DecoderInputStream(in, urld);
                        OutputStream out = new EncoderOutputStream(outVal[0], new QPEncoder());
                        for (int n; (n = in1.read(b)) > 0;) out.write(b, 0, n);
                        out.close();
                        outVal[0].close();
                    }
                    getResponse(ucs, res, b, messages.getString("itemSent"));
                } else put(servlet, req, res, messages.getString("itemsMoved"));
                return;
            }
            if (!ct.equals("multipart/form-data")) throw new ServletException("Wrong content type");
            Decoder decoder = null;
            String boundary = params.get("boundary");
            if (boundary != null) boundary = "--" + boundary;
            else if ((boundary = req.getHeader("boundary")) == null) throw new ServletException("Missing boundary");
            else decoder = new FI.realitymodeler.common.URLDecoder();
            InputStream in1 = new ConvertInputStream(in, boundary, "\r", true, true);
            while (in1.read(b) > 0);
            boundary = "\r\n" + boundary;
            OutputStream outVal[] = new OutputStream[1];
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StringBuffer sb = new StringBuffer();
            boolean multi = false;
            for (;;) {
                int c;
                if ((c = in.read()) == '-' && (c = in.read()) == '-') break;
                while (c != -1 && c != '\n') c = in.read();
                if (c == -1) break;
                in1 = new ConvertInputStream(in, boundary, false, true);
                HeaderList headerList;
                try {
                    headerList = new HeaderList(in1);
                } catch (ParseException ex) {
                    throw new IOException(ex.toString());
                }
                String cd = headerList.getHeaderValue("content-disposition");
                if (cd == null) throw new ServletException("Missing content-disposition");
                Support.getParameters(cd, params = new HashMap<String, String>(), true);
                String name = params.get("name");
                if (name == null) throw new ServletException("Missing name in content-disposition");
                if (name.equals("use") || name.equals("forward")) multi = true;
                else if (name.startsWith("file") || name.equals("sign") || name.equals("text") && !values.containsKey("new")) {
                    String filename = null;
                    if (values.containsKey("manual") || values.containsKey("new") ||
                        !name.equals("text") && ((filename = params.get("filename")) == null ||
                                                 (filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1).trim()).equals(""))) {
                        while (in1.read(b) > 0);
                        continue;
                    } else if (!name.equals("text") || values.containsKey("use") || values.containsKey("forward")) multi = true;
                    if (ucs == null) {
                        ucs = getSendStream(servlet.getServletConfig().getServletContext(), req,
                                            values, charsetName, outVal, multi);
                        if (outVal[0] == null) break;
                    }
                    if (name.equals("text")) in1 = Support.getTextInputStream(in1);
                    OutputStream out;
                    if (multi) {
                        Support.writeBytes(outVal[0], "\r\n--_\r\n", null);
                        if (name.equals("text")) {
                            Support.writeBytes(outVal[0], "Content-Type: text/plain; charset="
                                               + (charsetName != null ? charsetName : "iso-8859-1")
                                               + "\r\n", null);
                            Support.writeBytes(outVal[0], "Content-Disposition: inline\r\n", null);
                            Support.writeBytes(outVal[0], "Content-Transfer-Encoding: quoted-printable\r\n\r\n", null);
                            out = new EncoderOutputStream(outVal[0], new QPEncoder());
                        } else {
                            if ((ct = servlet.getServletConfig().getServletContext().getMimeType(filename)) == null) ct = "application/octet-stream";
                            Support.writeBytes(outVal[0], "Content-Type: " + ct
                                               + "; name=\"" + filename + "\"\r\n", null);
                            Support.writeBytes(outVal[0], "Content-Disposition: " + (ct.startsWith("image/")
                                                                                     ? "inline" : "attachment")
                                               + "; filename=\"" + filename + "\"\r\n", null);
                            Support.writeBytes(outVal[0], "Content-Transfer-Encoding: base64\r\n\r\n", null);
                            out = new EncoderOutputStream(outVal[0], new BASE64Encoder());
                        }
                    } else out = new EncoderOutputStream(outVal[0], new QPEncoder());
                    if (decoder == null) for (int n; (n = in1.read(b)) > 0;) out.write(b, 0, n);
                    else decoder.decodeStream(in1, out);
                    out.close();
                    continue;
                }
                if (decoder == null) {
                    while ((c = in1.read()) != -1) bout.write(c);
                    values.put(name, new String(bout.toByteArray(), "8859_1"));
                    bout.reset();
                } else values.put(name, new String(decoder.decodeStream(in1), "8859_1"));
            }
            if (in != null) while (in.read(b) > 0);
            if (values.containsKey("new") || values.containsKey("manual")) {
                values.put("form", "on");
                if (values.containsKey("new")) values.put("new", "on");
                if (values.containsKey("manual")) values.put("auto", "on");
                String s = values.get("number");
                int number = s != null ? Integer.parseInt(s) : 1;
                if (number > 100) throw new ServletException("Number of attachments exceeds the maximum 100");
                sb = new StringBuffer();
                Iterator valueIter = values.entrySet().iterator();
                while (valueIter.hasNext()) {
                    Map.Entry entry = (Map.Entry)valueIter.next();
                    String name = (String)entry.getKey(), value = (String)entry.getValue();
                    if (sb.length() > 0) sb.append('&');
                    sb.append(URLEncoder.encode(name, "8859_1") + "=" + URLEncoder.encode(value, "8859_1"));
                }
                res.sendRedirect(req.getRequestURI() + "?" + sb.toString());
                return;
            }
            if (outVal[0] != null) {
                if (multi) Support.writeBytes(outVal[0], "\r\n--_--\r\n", null);
                if (outVal[0] != null) outVal[0].close();
            }
            if (ucs != null) getResponse(ucs, res, b, messages.getString("itemSent"));
            else res.sendError(HttpURLConnection.HTTP_OK, messages.getString("invalidFormContents"));
        } catch (ParseException ex) {
            throw new ServletException(ex);
        } finally {
            if (ucs != null)
                for (int i = 0; i < ucs.length; i++)
                    if (ucs[i] != null) ucs[i].disconnect();
        }
    }

    public void init(ServletConfig config)
        throws ServletException {
        super.init(config);
        defaultMessages = W3URLConnection.getMessages(messagesBaseName);
        servletPathTable = (Map<String, String>)config.getServletContext().getAttribute("FI.realitymodeler.server.W3Server/servletPathTable");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        try {
            post(this, req, res, defaultMessages, "msg");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void doPut(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        put(this, req, res, null);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        try {
            get(this, req, res, defaultMessages, "mail", null, servletPathTable);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void doHead(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        doGet(req, res);
    }

}


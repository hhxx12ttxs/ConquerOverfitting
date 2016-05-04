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
// -*- java -*-
// $Id: Mozart.java 1175 2009-07-08 08:59:04Z vic $
// $Name:  $

package ru.adv.mozart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.adv.http.Query;
import ru.adv.io.InputOutputException;
import ru.adv.io.UnknownIOSourceException;
import ru.adv.logger.TLogger;
import ru.adv.mozart.framework.HostContext;
import ru.adv.mozart.framework.MAppContext;
import ru.adv.mozart.framework.Manager;
import ru.adv.mozart.framework.MozartRedirectToLocation;
import ru.adv.mozart.framework.RedirectToStaticFileException;
import ru.adv.mozart.framework.RequestContext;
import ru.adv.mozart.processor.ProcessorUtils;
import ru.adv.util.ErrorCodeException;
import ru.adv.util.Files;
import ru.adv.util.HTTPUtil;
import ru.adv.util.InputOutput;
import ru.adv.util.MIMEType;
import ru.adv.util.Path;
import ru.adv.util.Status;
import ru.adv.util.Stream;
import ru.adv.util.Strings;
import ru.adv.xml.formatter.Formatter;
import ru.adv.xml.formatter.HTMLFormatter;
import ru.adv.xml.parser.Parser;
import ru.adv.xml.parser.ParserException;
import ru.adv.xml.transformer.Transformer;
import ru.adv.xml.transformer.XSLTFile;

/**
 * ?????????? servlet, ???????????? XSL ????????????? XML ??????.
 * 
 * @author <a href="mailto:support@adv.ru">ADV</a>
 * @version $Revision: 1.48 $
 */

public class Mozart extends HttpServlet implements Status {

	private static final long serialVersionUID = -2948477030853019732L;
	private static final String SERVLET_ERROR_REQUEST_URI = "javax.servlet.error.request_uri";
	private static final String SERVLET_ERROR_STATUS_CODE = "javax.servlet.error.status_code";
	private static final String MOZART_ERR404_HANDLED     = Mozart.class.getName() + ".error404.handled";
	public static final String  QUERY_ORIGINAL_URL        = "mozart-original-url";
	private static final String XSL_SYNTAX_CHECK          = "/xslt-syntax-check.xml";

	private HostContext         hostContext              = null;
	private Set<RequestStatus>  registeredRequests       = new TreeSet<RequestStatus>();
	private long                startTime;
	private ServletContext servletContext;
	
	private TLogger logger = new TLogger(Mozart.class);

	/**
	 * ???? ????? ?????????? ?????? ? ??????? ????? servlet.
	 */
	public static String version() {
		return Defaults.NAME + " ver. " + Defaults.VERSION;
	}

	public HostContext getHostContext() {
		return hostContext;
	}
	
	public ServletContext getServletContext() {
		return servletContext!=null ? servletContext : super.getServletContext() ;
	}

	/**
	 * init Mozart with ready {@link HostContext}
	 * @param hostContext
	 */
	public void init(HostContext hostContext, ServletConfig servletConfig) throws Exception {
		
		this.servletContext = servletConfig.getServletContext();
		
		Assert.notNull(hostContext, "hostContext is null");
		this.hostContext = hostContext;
		this.hostContext.init(servletConfig);
		
		logger.info(version() + ": initiated.");
		
		startTime = System.currentTimeMillis(); 
	}

	/**
	 * ???? ????? ?????????????? servlet.</br> ?????????? ??
	 * {@link Defaults#CONFIG} ????????, ??????? ???????? ??? ????? Mozart
	 * ???????.</br> ????? ?????????
	 * {@link ru.adv.mozart.framework.HostContext} ? ????????????????
	 * ??????????? ???????.</br> ??? ??????,
	 * {@link ru.adv.mozart.framework.HostContext} ?????????? ????????
	 * ???????????? ? {@link Defaults}.
	 * 
	 * @throws javax.servlet.ServletException
	 * @see Defaults
	 * @see ru.adv.mozart.framework.HostContext
	 */
	public void init(ServletConfig config) throws ServletException {
		Assert.isTrue(false, "This API is depricated");
	}
	
	
	/**
	 * ??? ???? ??????? ?????, ??????? ???????????? ??????????? ???????.<br>
	 * ??????? {@link ru.adv.mozart.framework.RequestContext} ? ???????? ??????
	 * {@link ru.adv.mozart.framework.Manager#handle()} ???????
	 * {@link ru.adv.mozart.framework.Manager}.<br>
	 * 
	 * @throws javax.servlet.ServletException
	 * @throws java.io.IOException
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Create RequestContext
		RequestContext rcontext = null;
		RequestStatus requestStatus = null;
		String requestURI = getApplicationRequestURI(request);
		if (isConnectionLimitReached() && (!Defaults.DEFAULT_STATUS_URL.equals(requestURI))) {
			sendTemporaryUnavailable(response);
			logger.warning("Reached max connection for: " + request.getServerName());
			return;
		}
		try {
			requestStatus = new RequestStatus(request);
			registerRequest(requestStatus);
			
			if(hostContext.getStatisticURI().equals(requestURI)){
				sendQueryStatistic(request, response);
			} else {
				if (isRequestedDirectoryWithoutTrailingSlash(requestURI)) {
					logger.debug( "send redirect to="+requestURI);
					sendRedirect(request, response, RequestContext.calculateContextPath(request)+requestURI+"/");
					return;
				}
				try {
					getHostContext().watch( getServletContext() );
					if (request.getUserPrincipal() != null) {
						logger.debug("Remote user = " + request.getUserPrincipal().toString());
					}
					Query query = Query.getOrCreateQuery(request, getHostContext().getUploadDir());
					String queryPath = calculateQueryPath(request, query);
					MAppContext appContext;
					try {
						appContext = hostContext.getMAppContext(queryPath);
					} catch (RedirectToStaticFileException e) {
						sendStaticFile(response, e.getInputOutput());
						return;
					} catch (MozartRedirectToLocation e) {
						queryPath = e.getRedirectTo();  
						try {
							appContext = hostContext.getMAppContext(queryPath);
						} catch (MozartRedirectToLocation e1) {
							response.sendError(404, getApplicationRequestURI(request));
							return;
						}
					}
					rcontext = new RequestContext(request, response, query, queryPath, appContext);
					if (XSL_SYNTAX_CHECK.equals(requestURI)) {
						checkXSLTs(rcontext);
					} else {
						new Manager(rcontext, getHostContext()).handle();
					}
					response.flushBuffer();
				} catch (IOException e) {
					if (!(e.getClass().getName().indexOf("ClientAbortException") >= 0)) {
						throw e;
					}
				} catch (InputOutputException e) {
					if (!(e.getException() != null && e.getException().getClass().getName().indexOf("ClientAbortException") >= 0)) {
						throw e;
					}
				}
			}
		} catch (ErrorCodeException e) {
			logger.logErrorStackTrace("",e);
			errorHandler(request, response, rcontext != null ? rcontext.getQuery() : null, e);
		} catch (Throwable e) {
			logger.logFatalStackTrace(e);
			errorHandler(request, response, rcontext != null ? rcontext.getQuery() : null, new ErrorCodeException(e));
		} finally {
			unRegisterRequest(requestStatus);
			if (rcontext != null) {
				rcontext.destroy();
			}
			response.flushBuffer();
		}
	}

	private boolean isRequestedDirectoryWithoutTrailingSlash(String uri) {
		boolean result = false;
		if (!uri.endsWith("/")) {
			String file = Path.getFileName(uri);
			if (file.lastIndexOf(".") == -1) {
				result = true;
			}
		}
		return result;
	}

	private boolean isConnectionLimitReached() {
		return getRequestsCount() >= getHostContext().getLimitConnectionCount();
	}

	private void sendTemporaryUnavailable(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		response.setContentType("text/plain; charset=" + Defaults.ENCODING);
		response.setIntHeader("Retry-After", 60); // wait for 1 min
		OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream(), Defaults.ENCODING);
		out.write("503 Service Temporary Unavailable");
		out.flush();
	}

	private int getRequestsCount() {
		synchronized (this) {
			return registeredRequests.size();
		}
	}

	private void checkXSLTs(RequestContext context) {
		HttpServletResponse response = context.getResponse();
		response.setContentType("text/plain; charset=" + Defaults.ENCODING);
		try {
			response.getWriter().println("XSLT templates check " + context.getServerName());
			response.getWriter().flush();
			context.getResponse().getWriter().println();
			context.getResponse().getWriter().println("templates directory:");
			context.getResponse().getWriter().println();
			Path templatesPath = hostContext.getMAppContext(XSL_SYNTAX_CHECK).getTemplatesPath();
			File dir = new File(templatesPath.getRoot());
			checkDir(dir, "/", context);
			response.getWriter().flush();
			response.setStatus(200);
		} catch (Exception e) {
			try {
				e.printStackTrace(response.getWriter());
			} catch (IOException e1) {
				logger.error(e1);
			}
			response.setStatus(500);
		}
	}

	private void checkDir(File dir, String root, RequestContext context) throws IOException {
		context.getResponse().getWriter().println();
		context.getResponse().getWriter().println("Checking path " + dir);
		context.getResponse().getWriter().println();
		File[] files = dir.listFiles();
		List<File> dirs = new ArrayList<File>();
		for (File file : files) {
			if (file.isDirectory()) {
				dirs.add(file);
			} else {
				String filename = root + file.getName();
				if (filename.toLowerCase().endsWith(".xsl")) {
					checkTransformer(filename, context);
				}
			}
		}
		for (File file : dirs) {
			checkDir(file, root + file.getName() + "/", context);
		}
	}

	private void checkTransformer(String filename, RequestContext context) throws IOException {
		PrintWriter writer = context.getResponse().getWriter();
		try {
			InputOutput io = ProcessorUtils.getIO(context, filename);
			Transformer transformer = new XSLTFile(io, context.getXslCache());
			transformer.transform(createSourceDoc(), "/xslt.xml");
		} catch (ParserException e) {
			sendErrorMessage(writer, filename, e);
		} catch (TransformerException e) {
			sendErrorMessage(writer, filename, e);
		} catch (UnknownIOSourceException e) {
			sendErrorMessage(writer, filename, e);
		} catch (RuntimeException e) {
			sendErrorMessage(writer, filename, e);
		}

	}

	private void sendErrorMessage(PrintWriter writer, String filename, Exception e) {
		writer.print(Strings.pad(Files.getName(filename), 24));
		writer.print('\t');
		writer.println(e.getMessage());
		writer.flush();
	}

	private Document createSourceDoc() {
		Document result;
		result = Parser.createEmptyDocument();
		Element root = result.createElement("root");
		result.appendChild(root);
		return result;
	}

	private void registerRequest(RequestStatus status) {
		synchronized (this) {
			registeredRequests.add(status);
		}
	}

	private void unRegisterRequest(RequestStatus status) {
		if (status != null) {
			synchronized (this) {
				registeredRequests.remove(status);
			}
		}
	}

	private Set<RequestStatus> getRegisteredRequests() {
		return registeredRequests;
	}

	private String calculateQueryPath(HttpServletRequest request, Query query) {
		// Check original URL from query
		String queryPath = query.getFirst(QUERY_ORIGINAL_URL);
		Object wentFrom = request.getAttribute(SERVLET_ERROR_REQUEST_URI);
		boolean error404NotHandledYet = request.getAttribute(MOZART_ERR404_HANDLED) == null;
		if (queryPath != null) {
			logger.debug(QUERY_ORIGINAL_URL + " = " + queryPath);
			query.remove(QUERY_ORIGINAL_URL);
		} else if (wentFrom != null && error404NotHandledYet && request.getAttribute(SERVLET_ERROR_STATUS_CODE).toString().equals("404")) {
			// if dispatcher invokes 404 error page
			queryPath = wentFrom.toString();
			request.setAttribute(MOZART_ERR404_HANDLED, "yes");
		} else {
			queryPath = getApplicationRequestURI(request) ;
		}
		return queryPath;
	}

	/**
	 * @return URI to search in SiteTree
	 */
	private String getApplicationRequestURI(HttpServletRequest request) {
		if (StringUtils.hasLength( request.getPathInfo() )) {
			// for Mozart servlets mapped by URL prefix
			// example: 
			//   <servlet-name>editor</servlet-name>
		    //   <url-pattern>/admin/editor/*</url-pattern>
			// So SiteTree contains links from own htdocs root 
			return request.getPathInfo();  
		}
		return StringUtils.hasText(request.getContextPath()) ?
				// remove servlet context path
				fixedRequestURI(request.getRequestURI()).substring(request.getContextPath().length()) : 
					fixedRequestURI(request.getRequestURI()) ;
	}

	private String fixedRequestURI(String requestURI) {
		return Path.removeDoubleSlashesAtStart(requestURI);
	}

	private void sendRedirect(HttpServletRequest request, HttpServletResponse response, String redirectTo) throws IOException {
		HTTPUtil.sendRedirect(request, response, redirectTo, hostContext.getProxyPort());
	}

	/**
	 * send static file to response
	 * 
	 * @param response
	 * @param io
	 */
	private void sendStaticFile(HttpServletResponse response, InputOutput io) throws IOException {
		// TODO replace to StaticResourceHandler invoation
		response.setStatus(HttpServletResponse.SC_OK);
		MIMEType mt = new MIMEType(io.toString());
		response.setContentType(mt.getContentType(Defaults.ENCODING));
		response.setDateHeader("Expires", System.currentTimeMillis() + getHostContext().getMaxExpires());
		if (io.getSize() > 0) {
			response.setContentLength((int) io.getSize());
		}
		try {
			InputStream is = io.getInputStream();
			OutputStream os = response.getOutputStream();
			Stream.readTo(is, os);
			is.close();
			os.flush();
		} catch (InputOutputException e) {
			throw new IOException(e.toString());
		}
	}

	// default error handler
	private void errorHandler(HttpServletRequest request, HttpServletResponse response, Query query, ErrorCodeException e) {
		try {
			if (isTemporaryUnavailable(e)) {
				sendTemporaryUnavailable(response);
			} else if (isBadQuery(e, request)) {
				// 400 Bad Request
				sendError400(response);
			} else if (isInvalidatedSessionException(e)) {
				// 500 Error
				showErrorPage(response, e);
			} else {
				// 500 Error
				if (isAllowDebugFromIp(request.getRemoteAddr())) {
					showEmptyPage(response, e, request, query);
				} else {
					showErrorPage(response, e);
				}
			}
			OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream(), Defaults.ENCODING);
			out.flush();
			out.close();
		} catch (Exception ex) {
			logger.error(ex);
			logger.logStackTrace(ex);
		}
	}

	private boolean isInvalidatedSessionException(ErrorCodeException e) {
		return e.getCode() == ErrorCodeException.SESSION_INVALIDATED;
	}

	private boolean isBadQuery(ErrorCodeException e, HttpServletRequest request) {
		return (e.getCode() == ErrorCodeException.DB_CANNOT_CREATE_SQL_WHERE || // bad value for SQL WHERE
				e.getCode() == ErrorCodeException.INVALID_QUERY ) && // can't parse query
				!hostContext.canRemoteIpUseDebugModes(request.getRemoteAddr());
	}

	private boolean isTemporaryUnavailable(ErrorCodeException e) {
		return e.getCode() == ErrorCodeException.DB_TEMPORARY_UNAVAILABLE;
	}

	private void showErrorPage(HttpServletResponse response, ErrorCodeException e) throws IOException {
		Exception ex = sendNiceHTML(response, e);
		if (ex != null) {
			showErrorHTML(response, e, ex);
		}
	}

	private void sendError400(HttpServletResponse response) throws IOException {
		OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream(), Defaults.ENCODING);
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType("text/plain; charset=" + Defaults.ENCODING);
		out.write("400 Bad request");
		out.flush();
	}

	private void setError500HtmlStatus(HttpServletResponse response) {
		response.setStatus(500);
		response.setContentType("text/html; charset=" + Defaults.ENCODING);
	}

	private Exception sendNiceHTML(HttpServletResponse response, ErrorCodeException e) {
		setError500HtmlStatus(response);
		Exception result = null;
		Transformer transformer = hostContext.getErrorTransformer();
		if (transformer == null) {
			result = hostContext.getErrorXslException();
		} else {
			try {
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				document.appendChild(e.toXML(document));
				document = transformer.transform(document, "");
				Formatter formatter = new HTMLFormatter(Defaults.ENCODING);
				OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream(), Defaults.ENCODING);
				formatter.format(document, out);
			} catch (Exception ex) {
				logger.error(ex);
				result = ex;
			}
		}
		return result;
	}

	private void showEmptyPage(HttpServletResponse response, ErrorCodeException e, HttpServletRequest request, Query query) throws IOException {
		setError500HtmlStatus(response);
		OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream(), Defaults.ENCODING);
		out.write("<html><body bgcolor=\"white\" fgcolor=\"black\"></body></html>");
	}

	private void showErrorHTML(HttpServletResponse response, ErrorCodeException e, Exception ex) throws IOException {
		setError500HtmlStatus(response);
		OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream(), Defaults.ENCODING);
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		out.write("<html><head><title>Error</title></head><body bgcolor=\"white\" fgcolor=\"black\">\n");
		out.write("<p>\n");
		out.write("<b>\n");
		out.write(e.getMessage());
		out.write("</b>\n");
		out.write("</p>\n");
		out.write("<p>\n");
		out.write("Error: ");
		out.write("<plaintext>\n");
		e.printStackTrace(new PrintWriter(out, true));
		out.write("\n</plaintext>\n");
		out.write("<p>\n");
		out.write("<p>\n");
		out.write("XSL Error: ");
		out.write("<plaintext>\n");
		ex.printStackTrace(new PrintWriter(out, true));
		out.write("\n</plaintext>\n");
		out.write("<p>\n");
		out.write("</body></html>");
	}


	public void sendQueryStatistic(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setStatus(200);
		RequestDispatcher rd = request.getRequestDispatcher(Defaults.DEFAUL_STATISTIC_URL);
		rd.forward(request, response);
	}

	private boolean isAllowDebugFromIp(String ip) {
		return !hostContext.canRemoteIpUseDebugModes(ip);
	}

	/**
	 * ??????????? ??? ??????? ??????????? ??? ??????
	 */
	public void destroy() {
		logger.info(version() + ": start destroying.");
		synchronized (this) {
			registeredRequests.clear();
		}
		hostContext.destroy();
	}

	/**
	 * ?????????? ?????? ? ??????????? ? servlet
	 */
	public String getServletInfo() {
		return version();
	}

	private static DecimalFormat memFormat = new DecimalFormat("############,###");

	public synchronized List<String> getStatus() {
		List<String> status = new ArrayList<String>();
		status.add("Version: " + version());
		status.add("Uptime: " + Strings.formatAge(System.currentTimeMillis() - startTime));
		status.add("Memory usage: " + memFormat.format(Runtime.getRuntime().freeMemory()) + "/" + memFormat.format(Runtime.getRuntime().totalMemory()));
		status.add("Number of threads: " + getRegisteredRequests().size() + "/" + getHostContext().getLimitConnectionCount());
		for (RequestStatus stat : getRegisteredRequests()) {
			status.add( stat.getThreadName() 
					+ " [" + stat.getRemoteIP() + "] " 
					+ (System.currentTimeMillis() - stat.getStartTime()) + "ms " 
					+ stat.getRequestUri()
			);
		}
		return status;
	}

	class RequestStatus implements Comparable<RequestStatus> {

		private long   startTime;
		private Thread thread;
		private String requestURI;
		private String remoteIP;

		RequestStatus(HttpServletRequest request) {
			requestURI = getApplicationRequestURI(request);
			startTime = System.currentTimeMillis();
			thread = Thread.currentThread();
			remoteIP = request.getRemoteAddr();
		}

		@Override
		public int compareTo(RequestStatus o) {
			int result = 0;
			final long diff = getStartTime() - ((RequestStatus) o).getStartTime();
			if (diff > 0) {
				result = 1;
			} else if (diff < 0) {
				result = -1;
			}
			return result;
		}

		void interruptRequest() {
			if (thread.isAlive())
				thread.interrupt();
		}

		long getStartTime() {
			return startTime;
		}

		String getThreadName() {
			return thread.getName();
		}

		String getRequestUri() {
			return requestURI;
		}

		String getRemoteIP() {
			return remoteIP;
		}

	}

}


package kz.edu.sdu.sea.apps.web.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import kz.edu.sdu.sea.apps.ejb.client.ISearcherLocal;
import kz.edu.sdu.sea.apps.ejb.client.dto.SearchResultDTO;
import kz.edu.sdu.sea.apps.web.util.RenderToResponse;


public class SearchServlet extends HttpServlet {

	private static final long serialVersionUID = -7247101338883560575L;

	Logger log = Logger.getLogger(getClass());

	@EJB
	private ISearcherLocal iSearcher;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String path = req.getRequestURI();
		log.info("#####################DOGET######################");
		log.info(path);
		if( path.equals("/sea/web") ) {
			log.info("#####################INDEX_PAGE######################");
			int page = 1;
			int step = 1;
			if( req.getParameter("page") == null )
				RenderToResponse.renderPage(RenderToResponse.INDEX_PAGE, req, resp);
			else {
				page = Integer.parseInt(""+req.getParameter("page"));
				step = Integer.parseInt(""+req.getParameter("step"));
				
				List<SearchResultDTO> result = (List<SearchResultDTO>) req.getSession().getAttribute("result");
				int toIndex = page * 10;
				if( page*10 - 1 > result.size() ) toIndex = result.size();  
				
				List<SearchResultDTO> showResult = result.subList((page - 1) * 10, toIndex);
				int pages = (int)(Math.ceil((double)result.size() / 10));
				
				log.info("pages #:"+pages);
				log.info("steps: #"+(int)(Math.ceil((double)pages / 10)));
				
				req.setAttribute("result", showResult);
				req.setAttribute("pages", pages);
				req.setAttribute("maxStep", (int)(Math.ceil((double)pages / 10)));
				req.setAttribute("results", result.size());
				
				RenderToResponse.renderPage(RenderToResponse.SEARCH_RESULT_PAGE, req, resp);
			}
				
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String path = req.getRequestURI();
		log.info("#####################DO_POST######################");
		log.info(path);
		req.setCharacterEncoding("UTF-8");
		int page = 1;
		if( path.equals("/sea/web") ) {
			String keyword = new String(req.getParameter("q").getBytes(), "UTF-8");
			
			List<SearchResultDTO> result = iSearcher.search(keyword);

			
			req.getSession().setAttribute("result", result);
			int toIndex = page * 10;
			if( page*10 - 1 > result.size() ) toIndex = result.size();  
				
			List<SearchResultDTO> showResult = result.subList((page - 1) * 10, toIndex);
			
			int pages = (int)(Math.ceil((double)result.size() / 10));
			
			log.info("pages #:"+pages);
			log.info("steps: #"+(int)(Math.ceil((double)pages / 10)));
			
			req.setAttribute("result", showResult);
			req.setAttribute("pages", pages);
			req.setAttribute("maxStep", (int)(Math.ceil((double)pages / 10)));
			req.setAttribute("results", result.size());
			req.getSession().setAttribute("q", keyword);
			RenderToResponse.renderPage(RenderToResponse.SEARCH_RESULT_PAGE, req, resp);
		}
		
		
		
	}

}


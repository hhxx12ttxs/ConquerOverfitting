package com.zadania.zadanie2.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.zadania.zadanie2.domain.Counter;
import com.zadania.zadanie2.domain.Log;

@WebServlet(urlPatterns="/")
public class MainForm extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	
	
	@Override
	public void init() throws ServletException {
		getServletContext().setAttribute("globalLogged", new Counter());
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		response.setCharacterEncoding("utf-8");
		
		
		HttpSession session = request.getSession();
		if (session.getAttribute("log") == null) {
			session.setAttribute("log", new Log());
		}
		Log logged = (Log) session.getAttribute("log");
		
		PrintWriter out = response.getWriter();
		out.println("<html><head></head><body>");
		if (request.getParameter("act") != null && request.getParameter("act").equals("reg")){
			out.println("<form action='.' method='post'>" +
					"Nick:<input name='nick' type='text' /><br />" +
					"Pssw:<input name='pass' type='password' /><br />" +
					"Który z smakołyków jest najlepszy?:<br />" +
					"<input type='radio' name='jd' value='ciastko' />Ciastko<br />" +
					"<input type='radio' name='jd' value='karmel' />Karmel<br />" +
					"<input type='radio' name='jd' value='czekolada' />Czekolada<br />" +
					"<input type='radio' name='jd' value='paluszki' />Paluszki<br />" +
					"Co posiadasz?:" +
					"<input type='checkbox' name='bel' value='samochód' />Samochód<br />" +
					"<input type='checkbox' name='bel' value='Namiot' />Namiot<br />" +
					"<input type='checkbox' name='bel' value='ciężarówkę' />Ciężarówkę<br />" +
					"<input type='checkbox' name='bel' value='patyk' />Patyk<br />" +
					"<input name='sub' value='Wyślij' type='submit' />" +
					"</form>");
					
		}
		else {
			if (request.getParameter("act") != null && request.getParameter("act").equals("wyloguj")){
				if (logged.isLog() == true){
					Counter gCount = (Counter) request.getServletContext().getAttribute("globalLogged");
					gCount.decCounter();
					logged.setLog(false);
				}
			}
			out.println("<form action='logged' method='post'>" +
					"<input name='nick' type='text' />" +
					"<input name='pssw' type='password' />" +
					"<input name='sub' type='submit' value='Zaloguj' />" +
					"</form>" +
					"<a href='?act=reg'>Zarejestruj się</a>");
		}
		out.println("</body></html>");
		out.close();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int l = 0;
		
		response.setContentType("text/html");
		response.setCharacterEncoding("utf-8");
		
		HttpSession session = request.getSession();
		Log logged = (Log) session.getAttribute("log");
		Counter gCount = (Counter) request.getServletContext().getAttribute("globalLogged");
		if (logged.isLog() == false){
			gCount.incCounter();
			logged.setLog(true);
		}
		PrintWriter out = response.getWriter();
		out.println("<html><head></head><body>");
		String sBel = "";
		for (String bel : request.getParameterValues("bel")) {
			sBel += bel + " ";
			if (bel.equals("patyk")){
				l+=1;
			}
		}
		if (request.getParameter("jd").equals("paluszki")){
			l+=2;
		}
		out.println("<h3> Witaj " + request.getParameter("nick") + "!</h3><br />" +
				"Lubisz:" + request.getParameter("jd") + "<br />" +
				"Posiadasz: " + sBel +"<br />");
		if (l==3){
			out.println("<b>Przykro mi z powodu paluszków i patyka :(</b><br />");
		}
		else if(l==2){
			out.println("<b>Słone?</b><br />");
		}
		else if(l==1){
			out.println("<b>Mam nadzieję, że to nie jedyne co posiadasz :/</b><br />");
		}
		out.println("Obecnie aktywnych:" + gCount.getCounter() +"<br />");
		out.println("<a href='?act=wyloguj'>Wyloguj</a>");
		out.println("</body></html>");
		out.close();
	}
	
}


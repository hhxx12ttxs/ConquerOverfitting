package com.pingpong.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pingpong.service.ShowMomentService;
import com.pingpong.utils.Result;
import com.pingpong.utils.TextUtils;

public class ShowMomentAction extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/javascript; charset=utf-8");

		String hallIdStr = request.getParameter("hallId");
		if (TextUtils.isEmpty(hallIdStr)) {
			response.getWriter().print(
					Result.generateBaseResult(Result.RESULT_NEED_LOGIN_ID));
			return;
		}
		int hallId = Integer.parseInt(hallIdStr);
		String sinceIdStr = request.getParameter("sinceId");
		String beforeIdStr = request.getParameter("beforeId");
		String countStr = request.getParameter("count");

		int sinceId = 0, beforeId = 0, count = 20;
		if (!TextUtils.isEmpty(sinceIdStr)) {
			sinceId = Integer.parseInt(sinceIdStr);
		}

		if (!TextUtils.isEmpty(beforeIdStr)) {
			beforeId = Integer.parseInt(beforeIdStr);
		}

		if (!TextUtils.isEmpty(countStr)) {
			count = Integer.parseInt(countStr);
		}

		ShowMomentService service = new ShowMomentService();
		String result = service.showMoment(hallId, sinceId, beforeId, count);
		response.getWriter().print(result);
	}

}


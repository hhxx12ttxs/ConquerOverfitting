package com.pingpong.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pingpong.service.ShowInfoService;
import com.pingpong.utils.TextUtils;

public class ShowInfoAction extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int hallId = Integer.parseInt(request.getParameter("userId"));
		String sinceIdStr = request.getParameter("sinceId");
		String beforeIdStr = request.getParameter("beforeId");
		String countStr = request.getParameter("count");

		int sinceId, beforeId, count;

		if (!TextUtils.isEmpty(sinceIdStr)) {
			sinceId = Integer.parseInt(sinceIdStr);
		} else {
			sinceId = 0;
		}

		if (!TextUtils.isEmpty(beforeIdStr)) {
			beforeId = Integer.parseInt(beforeIdStr);
		} else {
			beforeId = 0;
		}

		if (!TextUtils.isEmpty(countStr)) {
			count = Integer.parseInt(countStr);
		} else {
			count = 20;
		}

		ShowInfoService service = new ShowInfoService();
		String result = service.getBallInfo(hallId, sinceId, beforeId, count);
		response.getWriter().print(result);
	}

}


package com.pingpong.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.Text;

import com.pingpong.service.ShowGameService;
import com.pingpong.utils.Result;
import com.pingpong.utils.TextUtils;

public class ShowGameAction extends HttpServlet {

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

		response.setContentType("text/javascript; charset=utf-8");

		String userIdStr = request.getParameter("userId");
		String hallIdStr = request.getParameter("hallId");
		String sinceIdStr = request.getParameter("sinceId");
		String beforeIdStr = request.getParameter("beforeId");
		String countStr = request.getParameter("count");
		String area = request.getParameter("area");

		int userId, hallId = -1, sinceId = -1, beforeId = -1, count = -1;

		if (TextUtils.isEmpty(userIdStr)) {
			response.getWriter().print(
					Result.generateBaseResult(Result.RESULT_NEED_LOGIN_ID));
			return;
		}

		userId = Integer.parseInt(userIdStr);

		if (!TextUtils.isEmpty(hallIdStr)) {
			hallId = Integer.parseInt(hallIdStr);
		}

		if (!TextUtils.isEmpty(sinceIdStr)) {
			sinceId = Integer.parseInt(sinceIdStr);
		}

		if (!TextUtils.isEmpty(beforeIdStr)) {
			beforeId = Integer.parseInt(beforeIdStr);
		}

		if (!TextUtils.isEmpty(countStr)) {
			count = Integer.parseInt(countStr);
		}

		ShowGameService service = new ShowGameService();
		String result = service.showGame(userId, hallId, sinceId, beforeId,
				count, area);
		response.getWriter().print(result);
	}

}


package com.pingpong.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pingpong.service.UpdateHallService;
import com.pingpong.utils.Result;
import com.pingpong.utils.TextUtils;

public class UpdateHallLocationAction extends HttpServlet {

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

		String userIdStr = request.getParameter("userId");
		if (TextUtils.isEmpty(userIdStr)) {
			response.getWriter().print(
					Result.generateBaseResult(Result.RESULT_NEED_LOGIN_ID));
			return;
		}
		int userId = Integer.parseInt(userIdStr);
		String hallLatitude = request.getParameter("hallLatitude");
		String hallLongitude = request.getParameter("hallLongitude");
		UpdateHallService service = new UpdateHallService();
		String result = service.updateHallLocation(userId, hallLatitude,
				hallLongitude);
		response.getWriter().print(result);

	}

}


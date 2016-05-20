/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package control.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Status.StatusDao;

/**
 *
 * @author T2n
 */
public class postStatus extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String StatusPost = request.getParameter("StatusContent");
            int IdUserShow = Integer.parseInt(request.getParameter("IdUserShow"));
            int IdUserSesion = (Integer) (request.getSession().getAttribute("IdUserSesion"));

            StatusDao statusDao = new StatusDao();
            //qua insert vao csdl 
            //nguoi post status chac chan phai la : IdUserSesion
            //nguoi duoc post status chac chan phai la IdUserShow
//            khi chinh 1 nguoi post status len tuong nh minh thi IdUserShow trung voi IdUserSesion
            boolean postStatus = statusDao.postStatusByIdUser(StatusPost, IdUserShow, IdUserSesion);

            if (postStatus) {
                response.sendRedirect("personal.jsp?IdUserShow=" + String.valueOf(IdUserShow));
            }



        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}


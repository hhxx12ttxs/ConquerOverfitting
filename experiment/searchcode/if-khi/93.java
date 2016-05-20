package control.servlet;

import model.user.UserDao;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.jasper.tagplugins.jstl.core.Catch;

public class login extends HttpServlet {

    PrintWriter out;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        out = response.getWriter();
        try {

            String Email = request.getParameter("Email");
            String Password = request.getParameter("Password");
            
            out.println(Email);
            out.println(Password);

            UserDao userDao = new UserDao();
            out.println("1");
            boolean checkLogin = userDao.checkLogin(Email.trim(), Password.trim());
            out.println("2");
            if (checkLogin) {
                out.println("3");
                out.write("ton tai user");
                // neu ton tai user thi tao 1 session// sau do chuyen huong toi trang personal
                int IdUser = userDao.getIdUserWithEmail(Email.trim());
                String Username = userDao.getUsernameWithEmail(Email.trim());

                request.getSession().setAttribute("EmailSesion", Email.trim());
                request.getSession().setAttribute("PasswordSesion", Password.trim());
                request.getSession().setAttribute("IdUserSesion", IdUser);
                request.getSession().setAttribute("UsernameSesion", Username);
                userDao.UpDateStt(IdUser,1);
                //sau khi login thanh cong ta toa ra 1 session giua server voi user
                // session nay se luu lai thong tin cua user dang nhap thanh cong 
                response.sendRedirect("personal.jsp?IdUserShow=" + IdUser);
                out.write("xx : " + Password);
            } else {
                out.println("khong ton tai user");
                response.sendRedirect("loginerror.jsp");
            }//end else


        } catch (Exception ex) {
//            response.sendRedirect("loginerror.jsp");
            ex.printStackTrace();
        } finally {
            out.close();
        }

    }//end

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);//luon goi toi method processRequest
    }//end

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);//luon goi toi method processRequest
    }//end

    @Override
    public String getServletInfo() {
        return "Short description";
    }//end
}//end  clcass


package controllers;

import java.util.Date;

import javax.persistence.NoResultException;

import org.apache.commons.lang.StringUtils;

import models.AdminUser;
import play.Play;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
    private static final play.api.Logger logger = play.api.Logger.apply(Application.class);
    
    public static final String APPLICATION_ENV = 
            Play.application().configuration().getString("application.env", "dev");
    
    public static final String APPLICATION_BASE_URL = 
            Play.application().configuration().getString("application.baseUrl");
    
    public static final Long APPLICATION_SESSION_TIMEOUT = 
            Play.application().configuration().getLong("application.sessionTimeout", 60L);
    
    public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";
	public static final String USER_ROLE = "user";

	public static Result getPost(int offset, int limit) {
        return ok();// TODO: null check need to be added
    }
    
	public static String getLoggedInUser() {
	    try {
	        final String loggedInUser = session().get("loggedInUser");
	        if (loggedInUser != null) {
	            // see if the session is expired
	            String loggedInTime = session().get("loggedInTime");
	            if (loggedInTime != null) {
	                long previousT = Long.valueOf(loggedInTime);
	                long currentT = new Date().getTime();
	                long timeout = APPLICATION_SESSION_TIMEOUT * 1000 * 60;
	                long diff = (currentT - previousT);
	                if (diff > timeout) {
	                    // session expired
	                    session().clear();
	                    logger.underlyingLogger().info(String.format("Admin user session expired after %d mins - %s",APPLICATION_SESSION_TIMEOUT,loggedInUser));
	                    return null;
	                } 
	            }
	     
	            // update time in session
	            session("loggedInTime", Long.toString(new Date().getTime()));
	            return loggedInUser;
	        }
	    } catch (Exception e) {
	        return null;
	    }
        return null;
	}
        
	@Transactional
	public static Result index() {
		final String loggedInUser = getLoggedInUser();
        if (loggedInUser == null) {
        	return ok(views.html.login.render());
        }
		return ok(views.html.home.render());
	}

	@Transactional
	public static Result login() {
		DynamicForm form = DynamicForm.form().bindFromRequest();
		String name = form.get("name");
	    String password = form.get("pass");
		try {
			AdminUser adminUser = AdminUser.doLogin(name,password);
			if(adminUser != null) {
				session().put("loggedInUser", adminUser.getUserName());
				logger.underlyingLogger().info("Admin user logged in - "+adminUser.getUserName());
				return ok(views.html.home.render());
			} else {
			    logger.underlyingLogger().info("Admin user not exist in - "+name);
			}
		} catch(NoResultException e) { 
		    logger.underlyingLogger().error("Admin user failed to log in - "+name);
		}
		return ok(views.html.login.render());
	}
	
	@Transactional
	public static Result logout() {
	    logger.underlyingLogger().info("Admin user logged out - "+getLoggedInUser());
		session().clear();
		return ok(views.html.login.render());
	}	
}


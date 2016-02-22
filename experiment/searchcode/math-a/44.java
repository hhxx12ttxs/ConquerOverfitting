package org.shs.user.dao;

import org.shs.common.dao.IBaseDAO;
import org.shs.user.form.LoginForm;
import org.shs.user.model.UserModel;


/**
 * Lists all the methods which need to be implemented by the implementation 
 * class for the User module.
 * <br/>
 * <br/><b>Created:</b>&nbsp;&nbsp; 12-Nov-2008 00:15:34
 *
 * @author anirvan
 *
 */
public interface IUserDAO extends IBaseDAO
{
   /**
    * Validate the credentials of the user trying to log into
    * the system
    * @param userData The bean encapsulating the loggers details
    * @return null (If user is not authorized); 
    *         bean model (if user can access the system)
    * @throws Exception if a problem is encountered by the
    *         implementation class of this DAO interface.
    */
   public UserModel validateUser(LoginForm loginData) throws Exception; 
   
}

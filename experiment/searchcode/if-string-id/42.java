package org.shs.user.model;

public class UserModel
{
   /**
    * The username of the logged in user
    */
   private String username = null;
   
   /**
    * The user-id [system-wide ID is constant 
    * so we need to use the same variable in 
    * every form bean] 
    */
   private String id;
   
   /**
    * The full name of the current user
    */
   private String name = null;
   
   /**
    * The role of the logged in user
    */
   private String role = null;
   
   /**
    * Any message to be shown to the user
    */
   private String message = null;
   
   /**
    * The current URL being requested by the user
    */
   private String requestedURL = null;
   
   /**
    * Is this a registered user?
    */
   private boolean isValidated = false;
   
   /**
    * Is there an error?
    */
   private boolean isError = false;
   
   /**
    * Returns the username of the logged in user
    * @return The validated username
    */
   public String getUsername()
   {
      return username;
   }
   
   /**
    * Sets the username of the logged in user
    * @param username The validated username
    */
   public void setUsername(String username)
   {
      this.username = username;
   }
   
   /**
    * Returns the message to be shown
    * @return The message for the user
    */
   public String getMessage()
   {
      return message;
   }
   
   /**
    * Sets the message to be shown
    * @param message The message for the user
    */
   public void setMessage(String message)
   {
      this.message = message;
   }

   /**
    * Returns the role of logged in user
    * @return the logged in user's role
    */
   public String getRole()
   {
      return role;
   }

   /**
    * Sets the role of logged in user
    * @param role The logged in user's role
    */
   public void setRole(String role)
   {
      this.role = role;
   }

   /**
    * Returns the validity of this user
    * @return the validity of the user
    */
   public boolean isValidated()
   {
      return isValidated;
   }

   /**
    * Sets the validity of the user
    * @param isValidated 
    */
   public void setIsValidated(boolean isValidated)
   {
      this.isValidated = isValidated;
   }
   
   /**
    * Returns the user-id of the logged person
    * @return the user-id of the logged person
    */
   public String getId()
   {
      return id;
   }

   /**
    * Sets the user-id of the logged person
    * @param id The user-id of the logged person
    */
   public void setId(String id)
   {
      this.id = id;
   }

   /**
    * Returns the full name of the advertiser
    * @return the full name of the advertiser
    */
   public String getName()
   {
      return name;
   }

   /**
    * Sets the full name of the user
    * @param name the full name of the user
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Returns the current URL being requested by the user
    * @return the current URL being requested by the user
    */
   public String getRequestedURL()
   {
      return requestedURL;
   }

   /**
    * Sets the current URL being requested by the user
    * @param requestedURL the current URL being requested by the user
    */
   public void setRequestedURL(String requestedURL)
   {
      this.requestedURL = requestedURL;
   }

   /**
    * Returns the error state, if any.
    * @return
    */
   public boolean isError()
   {
      return isError;
   }

   /**
    * Sets the error state
    * @param isError
    */
   public void setError(boolean isError)
   {
      this.isError = isError;
   }
}


/*
 * THIS FILE WAS GENERATED 17.04.2008 BY TASKPLAY.
 */

package keymind.keywatch.domainmodel.eventDomain;

import java.util.*;

	/**
	 * 
	 */
	public class Notification implements com.google.gwt.user.client.rpc.IsSerializable
	{
		protected Integer Id;

		/** Get Id */
		public Integer getId()
		{
			return Id;
		}


		/** Set Id */
		public void setId(Integer val)
		{
			Id = val;
		}

		protected Date Created;

		/** Get Created */
		public Date getCreated()
		{
			return Created;
		}


		/** Set Created */
		public void setCreated(Date val)
		{
			if (val!=null){
				Created= new java.util.Date(val.getTime());
			} else Created= null;
		}

		protected String Source;

		/** Get Source */
		public String getSource()
		{
			return Source;
		}


		/** Set Source */
		public void setSource(String val)
		{
			Source = val;
		}

		protected String Description;

		/** Get Description */
		public String getDescription()
		{
			return Description;
		}


		/** Set Description */
		public void setDescription(String val)
		{
			Description = val;
		}

		protected Severity Severity;

		/** Get Severity */
		public Severity getSeverity()
		{
			return Severity;
		}


		/** Set Severity */
		public void setSeverity(Severity val)
		{
			Severity = val;
		}

		protected User FromUser;

		/** Get FromUser */
		public User getFromUser()
		{
			return FromUser;
		}


		/** Set FromUser */
		public void setFromUser(User val)
		{
			FromUser = val;
		}


		public  Notification () 
		{

		}


		public  Notification (Integer id) 
		{
			this.Id = id;
		}


	}

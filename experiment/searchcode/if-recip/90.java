/**	$Id: SendMailDelegate.java,v 1.3 2002/07/22 23:47:30 jonc Exp $
 *
 *	Sending email

 */
package wsl.mdn.html;

import java.util.Vector;
import java.io.IOException;
import javax.servlet.ServletException;

import org.apache.ecs.html.*;
import org.apache.ecs.*;

import wsl.fw.servlet.*;
import wsl.fw.util.Util;
import wsl.fw.resource.ResId;
import wsl.fw.html.*;
import wsl.fw.datasource.*;
import wsl.fw.msgserver.*;
import wsl.fw.security.User;
import wsl.mdn.guiconfig.MsgServerAction;

public class SendMailDelegate
	extends MdnHtmlServletDelegate
{
	//--------------------------------------------------------------------------
	// resources
	public static final ResId
		TEXT_TITLE			= new ResId ("SendMailDelegate.text.Title"),
		TEXT_SEND_SUCCESS	= new ResId ("SendMailDelegate.text.SendSuccess"),
		TEXT_SEND_ERROR		= new ResId ("SendMailDelegate.text.SendError");

	//--------------------------------------------------------------------------
	// construction

	/**
	 * Default ctor
	 */
	public
	SendMailDelegate ()
	{
	}

	//--------------------------------------------------------------------------
	/**
	 * Handle actions for the EditRecordDelegate.
	 * @throws IOException, standard exception thrown by servlet methods.
	 * @throws ServletException, standard exception thrown by servlet methods.
	 */
	public void
	run ()
		throws IOException, ServletException
	{
		try
		{
			/*
			 *	Get request parameters
			 */
			String strIndex = _request.getParameter (
								MdnHtmlServlet.PV_RECORD_INDEX);
			String text = _request.getParameter (ReplyMailDelegate.RP_TEXT);

			/*
			 *	Get the MailMessage in question
			 */
			UserState userState = getUserState ();
			int index = Integer.parseInt (strIndex);
			PagedItrMsgDelegate pmd = (PagedItrMsgDelegate)
										userState.
										getCurrentPagedItDelegate ();
			MailMessageDobj mm = (MailMessageDobj) pmd.getMessage (index);

			/*
			 *	Build the Email Body
			 */
			String subject = "Re: " + mm.getSubject ();
			String recip = mm.getSenderEmail ();

			SessionMailMsgDobj reply = new SessionMailMsgDobj (
											userState.getSessionId ());
			reply.setSubject (subject);
			reply.setText (text == null ? "" : text);
			reply.setRecipient (recip);
			reply.setUserId (userState.getUser ().getId ());

			/*
			 *	Hand the message off to the message server
			 */
			MsgServerAction msa = (MsgServerAction) userState.getCurrentMenu();

			MessageServer ms =
					userState.
					getCache ().
					getMessageServer (new Integer (msa.getMsgServerId ()));

			DataSource ds = ms.createImpl ();

			if (ds.insert (reply))
			{
				/*
				 *	If it sent ok, we present the user with the MailBox
				 */
				fwdToMailbox ();

			} else
			{
				/*
				 *	Present failure result
				 */
				String str = WslHtmlUtil.esc (TEXT_SEND_ERROR.getText ());

				MdnClientCell client = new MdnClientCell ();
				client.setAlign (AlignType.CENTER);
				client.setHelpUrl ("/help/mdnhelp.html#bShowRecord");

				WslHtmlTable table = new WslHtmlTable ();
				client.addElement (table);

				TD cell = new TD (MdnHtmlServlet.getTitleElement (str));
				TR row = new TR (cell);
				cell.setColSpan (2);
				table.addElement (row);

				outputClientCell (client);
			}

		} catch (Exception e)
		{
			onError (e);
		}
	}
}


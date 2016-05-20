/**	$Id: SendMailDelegate.java,v 1.3 2002/07/23 21:44:51 jonc Exp $
 *
 *	Sending off email
 *
 */
package wsl.mdn.wap;

import java.util.Vector;
import java.io.IOException;
import javax.servlet.ServletException;

import org.apache.ecs.wml.*;

import wsl.fw.servlet.*;
import wsl.fw.util.Util;
import wsl.fw.resource.ResId;
import wsl.fw.wml.WEUtil;
import wsl.fw.datasource.*;
import wsl.fw.msgserver.*;
import wsl.fw.security.User;
import wsl.mdn.guiconfig.MsgServerAction;

public class SendMailDelegate extends MdnWmlServletDelegate
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
	public SendMailDelegate ()
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
								MdnWmlServlet.PV_RECORD_INDEX);
			String text = _request.getParameter (ReplyMailDelegate.RP_TEXT);

			/*
			 *	Get the MailMessage in question
			 */
			UserState userState = getUserState ();
			PagedItrMsgDelegate pmd = (PagedItrMsgDelegate)
										userState.
										getCurrentPagedItDelegate ();
			int index = Integer.parseInt (strIndex);
			MailMessageDobj mObj = (MailMessageDobj) pmd.getMessage (index);

			/*
			 *	Build the Email Body
			 */
			String subject = "Re: " + mObj.getSubject ();
			String recip = mObj.getSenderEmail ();

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

			String str;
			if (ds.insert (reply))
			{
				/*
				 *	If it sent ok, we present the user with the MailBox
				 */
				fwdToMailbox ();

			} else
			{
				/*
				 *	Present failure
				 */
				String msg = WEUtil.esc (TEXT_SEND_ERROR.getText ());

				Card card = new Card ();
				card.setTitle (WEUtil.esc (TEXT_TITLE.getText ()));
				P p = new P ();
				card.addElement (p);

				p.addElement (msg);

				/*
				 *	Bottom bits
				 */
				Do doMain = new Do (DoType.OPTIONS,
									MdnWmlServlet.TEXT_MAIN.getText ());
				Go goMain = new Go (makeHref (MdnWmlServlet.ACT_MAINMENU),
									Method.GET);
				doMain.addElement (goMain);
				card.addElement (doMain);

				// Back
				Do doOp = new Do (DoType.PREV,
								  MdnWmlServlet.TEXT_BACK.getText ());
				doOp.addElement (new Prev ());
				card.addElement (doOp);

				wmlOutput (card, true);
			}

		} catch (Exception e)
		{
			onError (e);
		}
	}
}


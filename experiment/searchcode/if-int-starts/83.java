/**	$Id: ImapFolderItr.java,v 1.19 2004/10/28 23:42:52 tecris Exp $
 *
 *	Iterator for IMAP folders
 *  	- presents the messages in reverse receipt order
 *
 */
package wsl.fw.msgserver;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

import wsl.fw.datasource.RecordItr;
import wsl.fw.resource.ResId;
import wsl.fw.msgserver.converters.WordReader;
import wsl.fw.html.WslHtmlParser;

import javax.mail.MessagingException;

public class ImapFolderItr
	extends RecordItr
{

	private WslHtmlParser parser = new WslHtmlParser ();
	private static final ResId
		LABEL_ATT_SHOWN		= new ResId ("ImapFolder.label.AttachDisplayed"),
		LABEL_ATT_HIDDEN	= new ResId ("ImapFolder.label.AttachUnshown"),
		LABEL_SUBJECT		= new ResId ("ImapFolder.label.NoSubject");

	/**
	 *	Iterator state
	 */
	private javax.mail.Folder _folder;
	private int _posn;
	private final String _folderId;
	private final boolean _unreadOnly;

	private static final String
		EMPTY_STRING		= "",
		SEMICOLON			= ";",
		MSWORD_EXTENSION	= ".doc",
		
		NEW_LINE			= "\n",
		
		MIME_HTML			= "text/html",
		MIME_TEXT			= "text/plain",
		MIME_RFC822			= "message/rfc822",
		MIME_ALTERNATIVE	= "multipart/alternative",
		MIME_OCTET			= "application/octet-stream",
		MIME_MSWORD			= "application/msword";

	/**
	 *	Constructor
	 */
	ImapFolderItr (
	 javax.mail.Folder folder,
	 boolean unreadOnly)
		throws MessagingException
	{
		_folder = folder;
		_unreadOnly = unreadOnly;
		_folderId = _folder.getURLName ().toString ();

		_posn = _folder.getMessageCount ();
	}

	/**
	 *	Required class method
	 */
	public boolean
	hasNext ()
	{
		try
		{
			/*
			 *	Note that _folder.getMessage (int) starts its numbering
			 *	from 1, not 0; so we test for > 0
			 */
			while ( _posn > 0)
			{
				Flags flags = _folder.getMessage (_posn).getFlags ();
				Flags.Flag sys [] = flags.getSystemFlags ();

				/*
				 *	Look thru' the flags for SEEN, DELETED
				 */
				boolean seen = false;
				boolean deleted = false;

				for (int i = 0; i < sys.length; i++)
				{
					if (sys [i] == Flags.Flag.SEEN)
						seen = true;
					else if (sys [i] == Flags.Flag.DELETED)
						deleted = true;
				}

				/*
				 *	Return undeleted messages or
				 *	only those unread if required
				 */
				if (!deleted && (!_unreadOnly || !seen))
					return true;

				_posn--;		// current doesn't fit requirements
			}

		} catch (MessagingException e)
		{
		}
		return false;
	}

	public Object
	next ()
	{
		MailMessage mail;
		try
		{
			javax.mail.Message message = _folder.getMessage (_posn--);

			/*
			 *	Once we get the Content, the system marks the message
			 *	as read. We don't want this. We want to keep the SEEN
			 *	flag the way it was prior to us reading it. So let's
			 *	inspect and hold it's state..
			 */
			boolean unread = true;
			Flags flags = message.getFlags ();
			Flags.Flag sys [] = flags.getSystemFlags ();

			for (int i = 0; i < sys.length; i++)
			{
				if (sys [i] == Flags.Flag.SEEN)
				{
					unread = false;
					break;
				}
			}

			/*
			 *	Extract content
			 */
			String content = parseMessage(message, false);

			/*
			 *	If the message was originally unread, we restore
			 *	it's unread state
			 */
			if (unread)
				message.setFlag (Flags.Flag.SEEN, false);

			String 
				subjectStr,
				senderStr,
				recvdStr,
				recpStr,
				textStr;


			/*
			 *	There can be multiple From, but we'll just pick the first one
			 */
			try
			{
				Address from [] = message.getFrom ();
				InternetAddress sender = (InternetAddress) from [0];
				senderStr = sender.getAddress ();
			}
			catch (Exception e)
			{
				senderStr = EMPTY_STRING;
			}

			try{
				subjectStr = ((message.getSubject ()==null)?
												LABEL_SUBJECT.getText ():
												message.getSubject ());
			}
			catch (Exception e)
			{
				subjectStr = LABEL_SUBJECT.getText ();
			}
			try
			{
				recvdStr = message.getReceivedDate ().toString ();
			}
			catch (Exception e)
			{
				recvdStr = EMPTY_STRING;
			}
			try
			{
				recpStr = InternetAddress.toString (message.getAllRecipients ());
			}
			catch (Exception e)
			{
				recpStr = EMPTY_STRING;
			}

			/*
			 *	Construct MailDobj
			 */
			mail = new MailMessage (
						Integer.toString (_posn + 1),	// id (folder position)
						subjectStr,						// Subject:
						content,						// Body
						"",								// type
						senderStr,						// From:
						recpStr,
						recvdStr,
						"false",						// unread
						senderStr);						// email

		} catch (Exception e)
		{
			e.printStackTrace ();
			String error = e.getMessage ()!=null?
							e.getMessage ():"Error";
			mail = new MailMessage (
						"",								// id
						"**Incompatible email format**",// Subject:
						error,							// Body
						"",								// type
						"**MDN system**",				// From:
						"",								// To:
						"",								// Date:
						"false",						// unread
						"");							// email
		}
		return new MailMessageDobj (_folderId, mail);
	}

	/**
	 * Get the contents from a given 
	 * javax.mail.Message  message.
	 * From lessons learned so far:
	 * Message parsing logic:
	 * 		if MIME type text/plain - 
	 * 			this is the simplest message 
	 * 			go get the text content
	 * 		if content of type Multipart
	 * 			if MIME type multipart/alternative
	 * 				go and get the HTML content
	 * 			else
	 * 				if MIME type RFC 822 this is a nested message 
	 * 					go and parse recursive 
	 * 				else iterate through all the mime parts
	 * 				and read only those of interest
	 * 				(ms word attachments, text, html etc)
	 * 			
	 * @param message The javax.mail.Message to process
	 * @param isAttachment boolean to indicate if this 
	 * message is an attachment
	 * @return message content
	 * @throws Exception
	 */
	private String parseMessage (
	 javax.mail.Message message,
	 boolean isAttachment)
		throws Exception
	{

		/*
		 *	Extract content
		 */
		String content = EMPTY_STRING;
		if (message.isMimeType(MIME_TEXT))
			content = isAttachment?
					"["
					+ LABEL_ATT_SHOWN.getText ()
					+ " " + this.getMime(message.getContentType())
					+ "]\n"
					+ message.getContent ().toString ().trim() + NEW_LINE:
					message.getContent().toString().trim() + NEW_LINE;
		else if (message.getContent () instanceof Multipart)
		{
			/*
			 * This is a multipart/alternative messsage with
			 * no attachment(s).Is multipart only because 
			 * it presents the same content in alternative 
			 * formats (plain text and html for example) 
			 */
			if (message.isMimeType(MIME_ALTERNATIVE))			
				content += getHtmlContent((Multipart) message.getContent (), isAttachment);
			else 
			{
				/*
				 * this is a multipart message with attachment(s)
				 * How to detect attachments?
				 * From "Java Mail Tutorial":
				 * Getting attachments out of your messages is a little 
				 * more involved then sending them, as MIME has no simple 
				 * notion of attachments. The content of your message is 
				 * a Multipart object when it has attachments. You then 
				 * need to process each Part, to get the main content and 
				 * the attachment(s). Parts marked with a disposition of 
				 * Part.ATTACHMENT from part.getDisposition() are clearly 
				 * attachments. However, attachments can also come across 
				 * with no disposition (and a non-text MIME type) or a 
				 * disposition of Part.INLINE. When the disposition is 
				 * either Part.ATTACHMENT or Part.INLINE, you can save off 
				 * the content for that message part. Just get the original 
				 * filename with getFileName() and the input stream with 
				 * getInputStream(). 
				 */
				Multipart mp = (Multipart) message.getContent ();
				for (int i = 0, n = mp.getCount (); i < n; i++)
				{
					Part part = mp.getBodyPart (i);
					String mime = getMime(part.getContentType());
					String text = EMPTY_STRING;
					/*
					 * Filter out attachment(s)
					 * See above paragraph/documentation
					 */
					if (part.getFileName ()==null && 
							!part.isMimeType(MIME_RFC822))
					{
						/* 
						 * Check for messages sent as multipart/alternative
						 * A multipart/alternative message contains the same
						 * data/text in alternative formats(text, html, etc)
						 * For multipart/alternative messages we need the 
						 * text part only
						 */ 
						if (part.isMimeType(MIME_ALTERNATIVE))
							content += getHtmlContent((Multipart) part.getContent (), isAttachment);
						else 
							content += isAttachment?
									"["
									+ LABEL_ATT_SHOWN.getText ()
									+ " " + mime
									+ "]\n"
									+ part.getContent ().toString ().trim() + NEW_LINE:
									part.getContent().toString().trim() + NEW_LINE;

					} else if ((part.isMimeType (MIME_MSWORD)||
								part.isMimeType (MIME_OCTET)) &&
								part.getFileName ().endsWith (MSWORD_EXTENSION))
					{
						// THIS IS A MS Word document attachment
						WordReader doc = new
								WordReader (part.getInputStream ());
						content += "["
									+ LABEL_ATT_SHOWN.getText ()
									+ " " + mime
									+ "]\n"
									+ doc.getAllText () 
									+ NEW_LINE;
					} else  if (part.isMimeType (MIME_TEXT))
					{
						// THIS IS A plain text attachment
						content += isAttachment?
									"["
									+ LABEL_ATT_SHOWN.getText ()
									+ " " + mime
									+ "]\n"
									+ part.getContent ().toString ().trim() + NEW_LINE:
									part.getContent ().toString ().trim() + NEW_LINE;
					} else if (part.isMimeType(MIME_RFC822) &&
							part.getContent () instanceof com.sun.mail.imap.IMAPNestedMessage)
					{
						/*	
						 * This is a nested message(com.sun.mail.imap.IMAPNestedMessage).
						 * Used google to find how to handle com.sun.mail.imap.IMAPNestedMessage						 						 
						 */
						javax.mail.Message nestedMessage = ((javax.mail.Message)part.getContent ()); 
						if ( nestedMessage.isMimeType(MIME_TEXT))
						{
							content += "["
								+ LABEL_ATT_SHOWN.getText ()
								+ " " + getMime(nestedMessage.getContentType())
								+ "]\n"
								+ nestedMessage.getContent().toString() + NEW_LINE;
						}
						else
							content += parseMessage(nestedMessage, true);
						
					}else
					{
						// attachment not displayed
						content += isAttachment?
									"["
									+ LABEL_ATT_HIDDEN.getText ()
									+ " " + mime
									+ "]\n":
									EMPTY_STRING + NEW_LINE;
					} 
				}
			}				
		} 
		return content;
	}
	
	/**
	 * Searches the specified Multipart body part container 
	 * first for an HTML mime part, and if not found, does a 
	 * search for a TEXT mime part and  retrieves the content.
	 * 
	 * @param multiPart - The Multipart container to search
	 * @param isAttachment - boolean is this an attachment
	 * @return HTML content
	 * @throws Exception
	 */
	private String 
	getHtmlContent (
	 Multipart multiPart,
	 boolean isAttachment)
		throws Exception
	{
		String 
			text 		= null,
			content 	= EMPTY_STRING,
			mime		= null;
		for (int j = 0; j < multiPart.getCount (); j++)
		{
			Part alternativePart = multiPart.getBodyPart (j);
			if (((MimeBodyPart) alternativePart).isMimeType (MIME_HTML))
			{
				text = parser.parseHtml (alternativePart.getContent().toString().trim());
				if (isAttachment)
					content += "["
						+ LABEL_ATT_SHOWN.getText ()
						+ " " + getMime(alternativePart.getContentType())
						+ "]\n"
						+ text 
						+ NEW_LINE;
				else
					content = text + NEW_LINE;
				// HTML content found let's get out
				return content;
			} else if (((MimeBodyPart) alternativePart).isMimeType (MIME_TEXT))
			{
				text = alternativePart.getContent().toString().trim();
				mime = getMime(alternativePart.getContentType());
			}
		}	
		/*
		 * no html part found, if text part found return it.
		 */
		if (text!=null)
			if (isAttachment)
				content += "["
					+ LABEL_ATT_SHOWN.getText ()
					+ " " + mime
					+ "]\n"
					+ text 
					+ NEW_LINE;
			else
				content = text + NEW_LINE;
		return content;		
	}
	
	/**
	 * String handler.
	 * For a given String returns a substring containing 
	 * the original string up to the first occurence 
	 * of the colon character(not included).
	 * 
	 * @param s The String to trim
	 * @return the trimmed String 
	 */
	private String 
	getMime (
	 String s)
	{
		return s.indexOf(SEMICOLON)!=-1?s.substring (0, s.indexOf (SEMICOLON)):s;
	}
}


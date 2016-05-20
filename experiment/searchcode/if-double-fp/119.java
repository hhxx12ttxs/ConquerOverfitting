/* MessageUtils.java

   Copyright (c) 2009 Juergen Schlierf, All Rights Reserved
   
   This file is part of Cubusmail (http://code.google.com/p/cubusmail/).
	
   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.
	
   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.
	
   You should have received a copy of the GNU Lesser General Public
   License along with Cubusmail. If not, see <http://www.gnu.org/licenses/>.
   
 */
package com.cubusmail.mail.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.BodyTerm;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.sun.mail.imap.IMAPFolder;

import com.cubusmail.core.CubusConstants;
import com.cubusmail.gwtui.domain.MessageListFields;
import com.cubusmail.gwtui.domain.SearchFields;
import com.cubusmail.mail.IMailFolder;

/**
 * Util class for general message functions.
 * 
 * @author Juergen Schlierf
 */
public class MessageUtils {

	/**
	 * Type of address string
	 * 
	 * @author Juergen Schlierf
	 */
	public enum AddressStringType {
		// personal name and Email
		COMPLETE,

		// personal name or email if therse no personal name
		PERSONAL,

		// personal name or empty if therse no personal name
		PERSONAL_ONLY,

		// email only
		EMAIL
	};

	public static final int BUFSIZE = 8192; // 8K

	private static Logger log = Logger.getLogger( MessageUtils.class.getName() );

	/**
	 * @param part
	 * @return
	 * @throws MessagingException
	 */
	public static boolean isImagepart( Part part ) throws MessagingException {

		return part.isMimeType( "image/png" ) || part.isMimeType( "image/gif" ) || part.isMimeType( "image/jpg" )
				|| part.isMimeType( "image/jpeg" );
	}

	/**
	 * @param emailAddress
	 * @param displayName
	 * @return
	 */
	public static String toInternetAddress( String emailAddress, String displayName ) {

		InternetAddress address;
		try {
			if ( !StringUtils.isEmpty( displayName ) ) {
				address = new InternetAddress( emailAddress, displayName );
				return address.toUnicodeString();
			}
			else {
				return emailAddress;
			}
		}
		catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Parses the adresslist.
	 * 
	 * @param addresslist
	 * @param charset
	 * @return
	 */
	public static InternetAddress[] parseInternetAddress( String addresslist, String charset )
			throws MessagingException {

		if ( addresslist != null ) {
			addresslist = addresslist.replace( ';', ',' );
			InternetAddress[] addressArray = InternetAddress.parse( addresslist );

			if ( addressArray != null ) {
				for (InternetAddress address : addressArray) {
					String personal = address.getPersonal();
					if ( personal != null ) {
						try {
							address.setPersonal( personal, charset );
						}
						catch (UnsupportedEncodingException e) {
							log.error( e.getMessage(), e );
						}
					}
				}
			}

			return addressArray;
		}

		return null;
	}

	/**
	 * Format the date.
	 * 
	 * @param date
	 * @return
	 */
	public static String formatMailDate( Date date ) {

		if ( date != null ) {
			DateFormat format = SimpleDateFormat.getDateTimeInstance( SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT,
					Locale.GERMAN );

			return format.format( date );
		}
		else {
			return "";
		}
	}

	/**
	 * Format the size of a part like message or attachment.
	 * 
	 * @param size
	 * @return
	 */
	public static String formatPartSize( int size, NumberFormat format ) {

		String value = null;
		if ( size >= 1024 ) {
			value = format.format( size / 1024 ) + " KB";
		}
		else {
			if ( size > 0 ) {
				value = Integer.toString( size ) + " B";
			}
			else {
				value = "n/a";
			}
		}
		return value;
	}

	/**
	 * @param locale
	 * @return
	 */
	public static NumberFormat createSizeFormat( Locale locale ) {

		NumberFormat sizeFormat = DecimalFormat.getNumberInstance( locale );
		sizeFormat.setGroupingUsed( true );
		sizeFormat.setMaximumFractionDigits( 0 );
		sizeFormat.setMinimumFractionDigits( 0 );

		return sizeFormat;
	}

	/**
	 * Calculate the real size bytes.
	 * 
	 * @param orgSize
	 * @return
	 */
	public static int calculateAttachmentSize( int orgSize ) {

		if ( orgSize > 0 ) {
			double size = (double) orgSize;
			size = size * CubusConstants.MESSAGE_SIZE_FACTOR;
			return (int) Math.round( size );
		}
		return orgSize;
	}

	/**
	 * @param addressArray
	 * @param emailAddress
	 * @return
	 */
	public static boolean findEmailAddress( Address[] addressArray, Address addressToFind ) {

		if ( addressArray != null ) {
			for (Address address : addressArray) {
				if ( address.equals( addressToFind ) ) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param msg
	 * @return
	 * @throws MessagingException
	 */
	public static boolean isHtmlMessage( Message msg ) throws MessagingException {

		return msg.isMimeType( "text/html" );
	}

	/**
	 * Method for checking if the message has attachments.
	 */
	public static List<MimePart> attachmentsFromPart( Part part ) throws MessagingException, IOException {

		List<MimePart> attachmentParts = new ArrayList<MimePart>();
		if ( part instanceof MimePart ) {
			MimePart mimePart = (MimePart) part;

			if ( part.isMimeType( "multipart/*" ) ) {
				Multipart mp = (Multipart) mimePart.getContent();
				for (int i = 0; i < mp.getCount(); i++) {
					MimeBodyPart bodyPart = (MimeBodyPart) mp.getBodyPart( i );
					if ( Part.ATTACHMENT.equals( bodyPart.getDisposition() )
							|| Part.INLINE.equals( bodyPart.getDisposition() ) ) {
						attachmentParts.add( bodyPart );
					}
				}
			}
			else if ( part.isMimeType( "application/*" ) ) {
				attachmentParts.add( mimePart );
			}
		}

		return attachmentParts;
	}

	/**
	 * @param part
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	public static boolean hasAttachments( Part part ) throws MessagingException, IOException {

		try {
			if ( part.isMimeType( "multipart/*" ) ) {
				Multipart mp = (Multipart) part.getContent();
				for (int i = 0; i < mp.getCount(); i++) {
					MimeBodyPart bodyPart = (MimeBodyPart) mp.getBodyPart( i );
					if ( Part.ATTACHMENT.equals( bodyPart.getDisposition() )
							|| Part.INLINE.equals( bodyPart.getDisposition() ) ) {
						return true;
					}
				}
			}
		}
		catch (Exception e) {
			log.error( e.getMessage(), e );
		}

		return false;
	}

	/**
	 * @param addressArray
	 * @param personalOnly
	 * @return
	 * @throws MessagingException
	 */
	public static String getMailAdressString( Address[] addressArray, AddressStringType type )
			throws MessagingException {

		String addressString = "";

		if ( addressArray != null ) {
			for (int i = 0; i < addressArray.length; i++) {

				String address = getMailAdressString( addressArray[i], type );
				if ( i < (addressArray.length - 1) ) {
					addressString += address + ", ";
				}
				else {
					addressString += address;
				}
			}
		}
		else {
			addressString = "";
		}

		return addressString;
	}

	/**
	 * @param address
	 * @param personalOnly
	 * @return
	 * @throws MessagingException
	 */
	public static String getMailAdressString( Address address, AddressStringType type ) throws MessagingException {

		String addressString = null;

		if ( type == AddressStringType.PERSONAL || type == AddressStringType.PERSONAL_ONLY ) {
			addressString = ((InternetAddress) address).getPersonal();
			if ( StringUtils.isEmpty( addressString ) && type == AddressStringType.PERSONAL ) {
				addressString = ((InternetAddress) address).getAddress();
			}
		}
		else if ( type == AddressStringType.COMPLETE ) {
			addressString = ((InternetAddress) address).toUnicodeString();
		}
		else if ( type == AddressStringType.EMAIL ) {
			addressString = ((InternetAddress) address).getAddress();
		}

		return addressString;
	}

	/**
	 * @param data
	 * @return
	 */
	public static String decodeText( String data ) {

		int start = 0, i;
		StringBuffer sb = new StringBuffer();
		while ((i = data.indexOf( "=?", start )) >= 0) {
			sb.append( data.substring( start, i ) );
			int end = data.indexOf( "?=", i );
			if ( end < 0 ) {
				break;
			}
			String s = data.substring( i, end + 2 );
			try {
				sb.append( MimeUtility.decodeWord( s ) );
			}
			catch (Exception e) {
				sb.append( s );
			}
			start = end + 2;
		}
		if ( start == 0 )
			return data;
		if ( start < data.length() )
			sb.append( data.substring( start ) );
		return sb.toString();
	}

	/**
	 * @param msg
	 * @param flag
	 * @param readFlag
	 * @throws MessagingException
	 */
	public static void setMessageFlag( Message msg, Flags.Flag flag, boolean readFlag ) throws MessagingException {

		boolean currentFlag = msg.isSet( flag );

		if ( currentFlag != readFlag ) {
			msg.setFlag( flag, readFlag );
		}
	}

	/**
	 * @param msg
	 * @return
	 * @throws MessagingException
	 */
	public static boolean isMessageReadFlag( Message msg ) throws MessagingException {

		return msg.isSet( Flags.Flag.SEEN );

	}

	/**
	 * 
	 * 
	 * @param msg
	 * @return
	 */
	public static int getMessagePriority( Message msg ) {

		int prio = CubusConstants.PRIORITY_NONE;

		try {
			String header[] = msg.getHeader( CubusConstants.FETCH_ITEM_PRIORITY );
			if ( header != null && header.length > 0 && header[0].length() > 0 ) {
				String first = header[0].substring( 0, 1 );
				if ( StringUtils.isNumeric( first ) ) {
					return Integer.parseInt( first );
				}
			}
		}
		catch (MessagingException e) {
			log.error( e.getMessage(), e );
		}

		return prio;
	}

	/**
	 * Sort the messages.
	 * 
	 * @param fieldName
	 * @param ascending
	 */
	public static void sortMessages( Message[] msgs, String fieldName, boolean ascending ) {

		if ( msgs != null && msgs.length > 0 ) {
			if ( !StringUtils.isEmpty( fieldName ) ) {
				Arrays.sort( msgs, new MessageComparator( fieldName, ascending ) );
			}
			else {
				// reverse order
				ArrayUtils.reverse( msgs );
			}
		}
	}

	/**
	 * Filter the messages.
	 * 
	 * @param msgs
	 * @param searchFields
	 * @param searchText
	 * @return
	 */
	public static Message[] quickFilterMessages( Message[] msgs, String searchFields, String searchText ) {

		if ( !StringUtils.isEmpty( searchFields ) && !StringUtils.isEmpty( searchText ) ) {
			searchFields = StringUtils.remove( searchFields, '[' );
			searchFields = StringUtils.remove( searchFields, ']' );
			searchFields = StringUtils.remove( searchFields, '\"' );
			String[] fields = StringUtils.split( searchFields, ',' );
			List<Message> filteredMsgs = new ArrayList<Message>();

			Date searchDate = null;
			try {
				searchDate = DateUtils.parseDate( searchText, new String[] { "dd.MM.yyyy" } );
			}
			catch (Exception e) {
				// do nothing
			}

			try {
				for (Message message : msgs) {
					boolean contains = false;
					for (String searchField : fields) {
						if ( MessageListFields.SUBJECT.name().equals( searchField ) ) {
							String subject = message.getSubject();
							contains = StringUtils.containsIgnoreCase( subject, searchText );
						}
						else if ( MessageListFields.FROM.name().equals( searchField ) ) {
							String from = MessageUtils.getMailAdressString( message.getFrom(),
									AddressStringType.COMPLETE );
							contains = StringUtils.containsIgnoreCase( from, searchText ) || contains;
						}
						else if ( searchDate != null && MessageListFields.DATE.name().equals( searchField ) ) {
							Date sendDate = message.getSentDate();
							contains = (sendDate != null && DateUtils.isSameDay( searchDate, sendDate )) || contains;
						}
					}
					if ( contains ) {
						filteredMsgs.add( message );
					}
				}
			}
			catch (MessagingException ex) {
				log.warn( ex.getMessage(), ex );
			}

			return filteredMsgs.toArray( new Message[0] );
		}

		return msgs;
	}

	/**
	 * @param mailFolder
	 * @param msgs
	 * @param extendedSearchFields
	 * @param params
	 * @return
	 */
	public static Message[] filterMessages( IMailFolder mailFolder, Message[] msgs, String extendedSearchFields,
			String[][] params ) {

		if ( !StringUtils.isEmpty( extendedSearchFields ) ) {
			String[] fields = StringUtils.split( extendedSearchFields, ',' );

			List<Message> filteredMsgs = new ArrayList<Message>();
			String fromValue = getParamValue( params, SearchFields.FROM.name() );
			String toValue = getParamValue( params, SearchFields.TO.name() );
			String ccValue = getParamValue( params, SearchFields.CC.name() );
			String subjectValue = getParamValue( params, SearchFields.SUBJECT.name() );
			String contentValue = getParamValue( params, SearchFields.CONTENT.name() );
			String dateFromValue = getParamValue( params, SearchFields.DATE_FROM.name() );
			String dateToValue = getParamValue( params, SearchFields.DATE_TO.name() );

			try {
				// Body search
				if ( StringUtils.contains( extendedSearchFields, SearchFields.CONTENT.name() ) ) {
					BodyTerm term = new BodyTerm( contentValue );
					msgs = mailFolder.search( term, msgs );
					if ( msgs == null ) {
						msgs = new Message[0];
					}
				}

				for (Message message : msgs) {
					boolean contains = true;
					for (String searchField : fields) {
						if ( SearchFields.FROM.name().equals( searchField ) ) {
							String from = MessageUtils.getMailAdressString( message.getFrom(),
									AddressStringType.COMPLETE );
							contains = StringUtils.containsIgnoreCase( from, fromValue );
						}
						if ( contains && SearchFields.TO.name().equals( searchField ) ) {
							String to = MessageUtils.getMailAdressString( message
									.getRecipients( Message.RecipientType.TO ), AddressStringType.COMPLETE );
							if ( !StringUtils.isEmpty( to ) ) {
								contains = StringUtils.containsIgnoreCase( to, toValue );
							}
							else {
								contains = false;
							}
						}
						if ( contains && SearchFields.CC.name().equals( searchField ) ) {
							String cc = MessageUtils.getMailAdressString( message
									.getRecipients( Message.RecipientType.CC ), AddressStringType.COMPLETE );
							if ( !StringUtils.isEmpty( cc ) ) {
								contains = StringUtils.containsIgnoreCase( cc, ccValue );
							}
							else {
								contains = false;
							}
						}
						if ( contains && SearchFields.SUBJECT.name().equals( searchField ) ) {
							if ( !StringUtils.isEmpty( message.getSubject() ) ) {
								contains = StringUtils.containsIgnoreCase( message.getSubject(), subjectValue );
							}
							else {
								contains = false;
							}
						}
						if ( contains && SearchFields.DATE_FROM.name().equals( searchField ) ) {
							Date dateFrom = new Date( Long.parseLong( dateFromValue ) );
							if ( message.getSentDate() != null ) {
								contains = !message.getSentDate().before( dateFrom );
							}
							else {
								contains = false;
							}
						}
						if ( contains && SearchFields.DATE_TO.name().equals( searchField ) ) {
							Date dateTo = new Date( Long.parseLong( dateToValue ) );
							if ( message.getSentDate() != null ) {
								contains = !message.getSentDate().after( dateTo );
							}
							else {
								contains = false;
							}
						}
					}
					if ( contains ) {
						filteredMsgs.add( message );
					}
				}
			}
			catch (MessagingException ex) {
				log.warn( ex.getMessage() );
			}

			return filteredMsgs.toArray( new Message[0] );
		}

		return msgs;
	}

	/**
	 * @param complete
	 * @param sortfield
	 * @return
	 */
	public static FetchProfile createFetchProfile( boolean complete, String sortfield ) {

		FetchProfile fp = new FetchProfile();
		if ( complete ) {
			fp.add( FetchProfile.Item.ENVELOPE );
			fp.add( FetchProfile.Item.FLAGS );
			fp.add( FetchProfile.Item.CONTENT_INFO );
			fp.add( IMAPFolder.FetchProfileItem.SIZE );
			fp.add( CubusConstants.FETCH_ITEM_PRIORITY );
			fp.add( UIDFolder.FetchProfileItem.UID );
		}
		else {
			if ( sortfield != null ) {
				if ( MessageListFields.ATTACHMENT_FLAG.name().equals( sortfield ) ) {
					fp.add( FetchProfile.Item.CONTENT_INFO );
				}
				else if ( MessageListFields.SUBJECT.name().equals( sortfield )
						|| MessageListFields.FROM.name().equals( sortfield )
						|| MessageListFields.TO.name().equals( sortfield )
						|| MessageListFields.DATE.name().equals( sortfield ) ) {
					fp.add( FetchProfile.Item.ENVELOPE );
				}
				else if ( MessageListFields.SIZE.name().equals( sortfield ) ) {
					fp.add( IMAPFolder.FetchProfileItem.SIZE );
				}
			}
		}
		return fp;
	}

	/**
	 * @param params
	 * @param paramName
	 * @return
	 */
	public static String getParamValue( String[][] params, String paramName ) {

		String result = null;

		if ( params != null ) {
			for (int i = 0; i < params.length; i++) {
				if ( params[i] != null && params[i].length == 2 && paramName.equals( params[i][0] ) ) {
					return params[i][1];
				}
			}
		}

		return result;
	}
}


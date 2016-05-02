/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)Mailbox.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

/***************************************************************************
 *
 *          Copyright (c) 2005, SeeBeyond Technology Corporation,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 ***************************************************************************/
package com.sun.jbi.smtpbc.extensions;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

import com.sun.jbi.internationalization.Messages;

/**
 * The Mailbox class represents a "mailbox" token defined by RFC 822.
 * The grammar for this is defined below:
 * <p>
 * <blockquote>
 *    mailbox     =  addr-spec                    ; simple address
 *                /  phrase route-addr            ; name & addr-spec
 *
 *    route-addr  =  "<" [route] addr-spec ">"
 *
 *    route       =  1#("@" domain) ":"           ; path-relative
 *
 *    addr-spec   =  local-part "@" domain        ; global address
 *
 *    local-part  =  word *("." word)             ; uninterpreted
 *                                                ; case-preserved
 *
 *    domain      =  sub-domain *("." sub-domain)
 *
 *    sub-domain  =  domain-ref / domain-literal
 *
 *    domain-ref  =  atom                         ; symbolic reference
 * </blockquote>
 * <p>
 * In addition to following the aforementioned grammar, this class provides
 * facilities to strip out comments and remove white spaces.  Comments are
 * nested, parenthetical statements that may show up in an address.  Moreover,
 * although spaces, special characters, and control characters are not allowed
 * in "atom"s, instead of throwing exceptions, this class simply removes them.
 * This behavior is in line with example A.1.4 in Appendix A.1 of RFC 822.
 * <p>
 * Furthermore, this class doesn NOT deal with URL encoded addresses.  Users of
 * this class must properly pass in unencoded "mailbox" tokens.
 *
 * @author       Alexander Fung
 * @version      
 *
 */
public class Mailbox {

    private String mLocalPart;
    private String mDomain;
    private String mPhrase = "";
    private String[] mRouteTokens;

    private static final Messages mMessages = Messages.getMessages(Mailbox.class);

    private static final Logger mLogger = Messages.getLogger(Mailbox.class);
    
    public Mailbox() {}

    public Mailbox(final String mailbox) throws InvalidMailboxException {
        this();
        unmarshal(mailbox);
    }

    public String getAddressSpec() {
        return mLocalPart + "@" + mDomain;
    }

    public String getNormalizedAddressSpec() {
        return normalize(stripComments(getAddressSpec()));
    }

    public String getLocalPart() {
        return mLocalPart;
    }

    public String getNormalizedLocalPart() {
        return normalize(stripComments(mLocalPart));
    }

    public String getDomain() {
        return mDomain;
    }

    public String getNormalizedDomain() {
        return normalize(stripComments(mDomain));
    }

    public String getPhrase() {
        return mPhrase;
    }

    public String getRoute() {
        String route = "";
        if (mRouteTokens != null) {
            for (String element : mRouteTokens) {
                route = route + element + ",";
            }
            final char[] characters = route.toCharArray();
            characters[characters.length - 1] = ':';
            route = new String(characters);
        }
        return route;
    }

    public final void unmarshal(final String mailbox) 
        throws InvalidMailboxException {
        if(mLogger.isLoggable(Level.FINE)){
        	mLogger.log(Level.FINE,mMessages.getString("Mailbox.parsingMailbox",mailbox));
        }
        if (mailbox.indexOf('<') != -1) {
            parseFullAddressSpecification(mailbox);
        } else {
            parseAddressSpecification(mailbox);
        }
        
        //Validataing the normalized email address
        try {
            InternetAddress emailAddress = new InternetAddress(getNormalizedAddressSpec());
        } catch(AddressException e){
            throw new InvalidMailboxException(e.getMessage(),e);
        }
    }

    public String marshal() throws InvalidMailboxException {
        
        if (mPhrase.equals("")) {
            return getAddressSpec();
        } else {
            final String route = getRoute();
            if (route.equals("")) {
                return mPhrase + " <" + getAddressSpec() + ">";
            } else {
                return mPhrase + " <"+getRoute()+" "+getAddressSpec()+">";
            }
        }
    }

    protected void parseFullAddressSpecification(final String fullAddrSpec)
        throws InvalidMailboxException {

        final int bracket = fullAddrSpec.indexOf("<");
        if (bracket == -1) {
            throw new InvalidMailboxException();
        }
        
        mPhrase = fullAddrSpec.substring(0, bracket).trim();
        parseRouteAddress(fullAddrSpec.substring(bracket,
                                                 fullAddrSpec.length()));
        
    }

    protected void parseRouteAddress(final String routeAddr) 
        throws InvalidMailboxException {
        
        if (!routeAddr.startsWith("<") || !routeAddr.endsWith(">")) {
            throw new InvalidMailboxException();
        }

        final String newRouteAddr = routeAddr.substring(1, routeAddr.length() -1);
        final int colon = newRouteAddr.indexOf(':');
        if (colon == -1) {
            parseAddressSpecification(newRouteAddr);
        } else {
            parseRoute(newRouteAddr.substring(0, colon));
            parseAddressSpecification(newRouteAddr.substring(colon + 2,
                                                             newRouteAddr.length()));
        }
    }

    protected void parseRoute(final String route) {
        mRouteTokens = route.split(",");
    }

    protected void parseAddressSpecification(final String addrSpec) 
        throws InvalidMailboxException {
        
        final int atSign = addrSpec.indexOf('@');
        if (atSign != -1) {
            mLocalPart = addrSpec.substring(0, atSign);
            mDomain = addrSpec.substring(atSign + 1, addrSpec.length());
            if(mLocalPart.length() == 0){
            	throw new InvalidMailboxException();
            }
            if(mDomain.length() == 0){
            	throw new InvalidMailboxException();
            }
        } else {
            throw new InvalidMailboxException();
        }
    }

    /**
     * All atoms are any characters except specials, space, and control
     * characters.  For now, just remove space 
     *
     * @param        
     * @return       
     * @exception    
     * @see          
     */
    protected String normalize(final String token) {
        if (token.startsWith("\"") && token.endsWith("\"")) {
            return token;
        }

        return token.replace(" ", "");
    }

    protected String stripComments(final String token) {
        if (token.startsWith("\"") && token.endsWith("\"")) {
            return token;
        }

        String tmp = token;
        boolean hasParens = (tmp.indexOf('(') != -1);
        while (hasParens) {
            Stack parens = new Stack();
            char[] chars = tmp.toCharArray();
            hasParens = false;
            for (int ii = 0; ii < chars.length; ii++) {
                if (chars[ii] == '(') {
                    parens.push(new Integer(ii));
                }
                if (chars[ii] == ')') {
                    // Found a match
                    final int start = ((Integer)parens.pop()).intValue();
                    
                    // Remove the string
                    tmp = tmp.replace(tmp.substring(start, ii + 1), "");

                    // Reinitialize everything and start over
                    parens = new Stack();
                    chars = tmp.toCharArray();
                    hasParens = true;
                    break;
                }
            }
        }

        return tmp;
    }
}


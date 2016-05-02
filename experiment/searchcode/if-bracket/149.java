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
 * @(#)SMTPMailbox.java 
 *
 * Copyright 2004-2009 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */
package com.sun.jbi.binding.email.protocol.send.smtp;

import java.util.Stack;
import java.util.logging.Logger;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.sun.jbi.binding.email.I18n;

/**
 * The SMTPMailbox class represents a "mailbox" token defined by RFC 822.
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
 * Furthermore, this class does NOT deal with URL encoded addresses.  Users of
 * this class must properly pass in unencoded "mailbox" tokens.
 *
 * @author Harry Liu (Harry.Liu@sun.com)
 *
 */
public class SMTPMailbox {

    private static final Logger logger = Logger.getLogger(SMTPMailbox.class.getName());
    private String mLocalPart;
    private String mDomain;
    private String mPhrase = "";
    private String[] mRouteTokens;

    public SMTPMailbox(final String mailbox) throws Exception {
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
        StringBuilder routeBuilder = new StringBuilder();
        if (mRouteTokens != null) {
            for (int ii = 0; ii < mRouteTokens.length; ii++) {
                routeBuilder.append(mRouteTokens[ii]).append(",");
            }
            //remove last comma
            routeBuilder.deleteCharAt(routeBuilder.length() - 1);
            //replace by :
            routeBuilder.append(":");
        }
        return routeBuilder.toString();
    }

    public final void unmarshal(String mailbox) throws AddressException, Exception {
        I18n.finer(logger, "EMAILBC-2031: Validating the mailbox address {0}", mailbox);
        if (mailbox.indexOf('<') != -1) {
            parseFullAddressSpecification(mailbox);
        } else {
            parseAddressSpecification(mailbox);
        }

        //Validating the normalized email address
        try {
            InternetAddress emailAddress = new InternetAddress(getNormalizedAddressSpec());
        } catch (AddressException e) {
            throw e;
        }
    }

    public String marshal() throws Exception {

        if (mPhrase.equals("")) {
            return getAddressSpec();
        } else {
            final String route = getRoute();
            if (route.equals("")) {
                return mPhrase + " <" + getAddressSpec() + ">";
            } else {
                return mPhrase + " <" + getRoute() + " " + getAddressSpec() + ">";
            }
        }
    }

    protected void parseFullAddressSpecification(final String fullAddrSpec)
            throws Exception {

        final int bracket = fullAddrSpec.indexOf("<");
        if (bracket == -1) {
            throw new Exception();
        }

        mPhrase = fullAddrSpec.substring(0, bracket).trim();
        parseRouteAddress(fullAddrSpec.substring(bracket,
                fullAddrSpec.length()));

    }

    protected void parseRouteAddress(final String routeAddr)
            throws Exception {

        if (!routeAddr.startsWith("<") || !routeAddr.endsWith(">")) {
            throw new Exception();
        }

        final String newRouteAddr = routeAddr.substring(1, routeAddr.length() - 1);
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
            throws Exception {

        final int atSign = addrSpec.indexOf('@');
        if (atSign != -1) {
            mLocalPart = addrSpec.substring(0, atSign);
            mDomain = addrSpec.substring(atSign + 1, addrSpec.length());
            if (mLocalPart.length() == 0) {
                throw new Exception();
            }
            if (mDomain.length() == 0) {
                throw new Exception();
            }
        } else {
            throw new Exception();
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
            Stack<Integer> parens = new Stack<Integer>();
            char[] chars = tmp.toCharArray();
            hasParens = false;
            for (int ii = 0; ii < chars.length; ii++) {
                if (chars[ii] == '(') {
                    parens.push(Integer.valueOf(ii));
                }
                if (chars[ii] == ')') {
                    // Found a match
                    final int start = parens.pop().intValue();

                    // Remove the string
                    tmp = tmp.replace(tmp.substring(start, ii + 1), "");

                    // Reinitialize everything and start over
                    parens = new Stack<Integer>();
                    chars = tmp.toCharArray();
                    hasParens = true;
                    break;
                }
            }
        }

        return tmp;
    }
}


/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/

package org.bedework.selfreg.common.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.bedework.selfreg.common.SelfregConfigProperties;
import org.bedework.selfreg.common.exception.SelfregException;

/** A mailer which provides some minimal functionality for testing.
 * We do not consider many issues such as spam prevention, efficiency in
 * mailing to large lists, etc.
 *
 * @author  Mike Douglass douglm@bedework.edu
 */
public class Mailer implements MailerIntf {
  private boolean debug;

  private SelfregConfigProperties config;

  private Session sess;

  private transient Logger log;

  @Override
  public void init(final SelfregConfigProperties config) throws SelfregException {
    debug = getLog().isDebugEnabled();
    this.config = config;

    Properties props = new Properties();

    props.put("mail." + config.getMailProtocol() + ".class", config.getMailProtocolClass());
    props.put("mail." + config.getMailProtocol() + ".host", config.getMailServerIp());
    if (config.getMailServerPort() != null) {
      props.put("mail." + config.getMailProtocol() + ".port",
                config.getMailServerPort());
    }

    //  add handlers for main MIME types
    MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();
    mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
    mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
    mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
    mc.addMailcap("text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_html");
    mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
    mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
    CommandMap.setDefaultCommandMap(mc);

    sess = Session.getInstance(props);
    sess.setDebug(debug);
  }

  @Override
  public Collection<String> listLists() throws SelfregException {
    debugMsg("listLists called");
    return new ArrayList<String>();
  }

  @Override
  public void post(final Message val) throws SelfregException {
    debugMsg("Mailer called with:");
    debugMsg(val.toString());

    if (config.getMailDisabled()) {
      return;
    }

    try {
      /* Create a message with the appropriate mime-type
       */
      MimeMessage msg = new MimeMessage(sess);

      msg.setFrom(new InternetAddress(val.getFrom()));

      InternetAddress[] tos = new InternetAddress[val.getMailTo().length];

      int i = 0;
      for (String recip: val.getMailTo()) {
        tos[i] = new InternetAddress(recip);
        i++;
      }

      msg.setRecipients(javax.mail.Message.RecipientType.TO, tos);

      msg.setSubject(val.getSubject());
      msg.setSentDate(new Date());

      msg.setContent(val.getContent(), "text/plain");

      Transport tr = sess.getTransport(config.getMailProtocol());

      tr.connect();
      tr.sendMessage(msg, tos);
    } catch (Throwable t) {
      if (debug) {
        t.printStackTrace();
      }

      throw new SelfregException(t);
    }
  }

  private Logger getLog() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  private void debugMsg(final String msg) {
    getLog().debug(msg);
  }
}


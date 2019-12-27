/*******************************************************************************
 * Copyright (c) 2014 Ministry of Transport and Communications (Finland).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Semantum Oy - initial API and implementation
 *******************************************************************************/
package fi.semantum.strategia;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import fi.semantum.strategia.configurations.Configuration;

public class Email {
	
	public static void main(String[] args) throws Exception {
		String[] emails = {Configuration.getRECEIVER_EMAIL()}; 
		send(emails, "Tiedote", "T‰rke‰‰ tietoa!");
	}
	
	public static String smtpLocalhost() {
		return Configuration.getSmtpLocalhost();
	}
	
	public static String smtpHost() {
		return Configuration.getSmtpHost();
	}

	public static String smtpFrom() {
		return Configuration.getSmtpFrom();
	}

	public static void send(String[] emails, String subject, String body) throws MessagingException {
	
		  Properties mailProps = new Properties();
		  
		  mailProps.put("mail.smtp.localhost", smtpLocalhost());
	      mailProps.put("mail.smtp.from", smtpFrom());
	      mailProps.put("mail.smtp.host", smtpHost());
	      mailProps.put("mail.smtp.port", 25);
	      mailProps.put("mail.smtp.auth", false);
	      
	      Session mailSession = Session.getDefaultInstance(mailProps);

	      MimeMessage message = new MimeMessage(mailSession);
	      message.setFrom(new InternetAddress(Configuration.getSENDER_EMAIL()));
	      InternetAddress dests[] = new InternetAddress[emails.length];
	      for (int i = 0; i < emails.length; i++) {
	          dests[i] = new InternetAddress(emails[i].trim().toLowerCase());
	      }
	      message.setRecipients(Message.RecipientType.TO, dests);
	      message.setSubject(subject, "UTF-8");
	      Multipart mp = new MimeMultipart();
	      MimeBodyPart mbp = new MimeBodyPart();
	      mbp.setContent(body, "text/html;charset=utf-8");
	      mp.addBodyPart(mbp);
	      message.setContent(mp);
	      message.setSentDate(new java.util.Date());

	      Transport.send(message);
		
	}

}

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
package fi.semantum.strategia.configurations;

public class Configuration {
	
	////////////////////////////////////////////////////////////////////
	//Default constants:
	////////////////////////////////////////////////////////////////////
	
	private static final String DEFAULT_WIKI_ADDRESS = ""; //"https://www.digitulosohjaus.fi/strategiakartta";
	private static final String DEFAULT_SMTP_LOCAL_HOST = "localhost"; //"www.digitulosohjaus.fi";
	private static final String DEFAULT_SMTP_HOST = "-";
	private static final String DEFAULT_SMTP_FROM = "contact@semantum.fi"; //"strategia@digitulosohjaus.fi";
	
	private static final String DEFAULT_TARGET_WEBSITE_LINK = "http://localhost"; //"https://www.eduskunta.fi";
	private static final String DEFAULT_RECEIVER_EMAIL = "contact@semantum.fi"; //"stkartta@gmail.com";
	private static final String DEFAULT_SENDER_EMAIL = "contact@semantum.fi"; //"strategiakartta@simupedia.com";
	private static final String DEFAULT_HTTPS_WIKI_ADDRESS = "http://localhost"; //"https://www.digitulosohjaus.fi";
	private static final String DEFAULT_GUEST_GROUP_NAME = "GuestGroup";
	private static final String DEFAULT_GUEST_ACCOUNT_NAME = "Guest";
	private static final String DEFAULT_GUEST_ACCOUNT_PASSWORD = "";
	private static final String DEFAULT_ADMIN_GROUP_NAME = "SystemGroup";
	private static final String DEFAULT_ADMIN_ACCOUNT_PASSWORD = "";
	private static final String DEFAULT_ADMIN_ACCOUNT_EMAIL = "contact@semantum.fi";
	private static final String DEFAULT_WIKI_PREFIX = "DefaultWikiAddress_"; //"Strategiakartta_";
	private static final int DEFAULT_MAX_SHORT_DESCRIPTION_LENGTH = 150;
	private static final int DEFAULT_MAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE = 50;

	private static final int DEFAULT_MAX_COMMENT_DELETION_HOURS = 24;
	private static final boolean DEFAULT_TABLE_INPUT_YEAR_RESTRICTION_ON = true;
	
	////////////////////////////////////////////////////////////////////
	//Changeable variables:
	////////////////////////////////////////////////////////////////////
	
	private static String WIKI_ADDRESS = null;
	private static String SMTP_LOCAL_HOST = null;
	private static String SMTP_HOST = null;
	private static String SMTP_FROM = null;
	private static String TARGET_WEBSITE_LINK = null;
	private static String RECEIVER_EMAIL = null;
	private static String SENDER_EMAIL = null;
	private static String HTTPS_WIKI_ADDRESS = null;
	private static String GUEST_GROUP_NAME = null;
	private static String GUEST_ACCOUNT_NAME = null;
	private static String GUEST_ACCOUNT_PASSWORD = null;
	private static String ADMIN_GROUP_NAME = null;
	private static String ADMIN_ACCOUNT_PASSWORD = null;
	private static String ADMIN_ACCOUNT_EMAIL = null;
	private static String WIKI_PREFIX = null;
	private static Integer MAX_SHORT_DESCRIPTION_LENGTH = null;
	private static Integer MAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE = null;
	private static Integer MAX_COMMENT_DELETION_HOURS = null;
	private static Boolean TABLE_INPUT_YEAR_RESTRICTION_ON = null;
	
	////////////////////////////////////////////////////////////////////
	//Getters for constants:
	////////////////////////////////////////////////////////////////////
	
	public static String getWikiAddress() 			{ return WIKI_ADDRESS; }
	public static String getSmtpLocalhost() 		{ return SMTP_LOCAL_HOST; }
	public static String getSmtpHost() 				{ return SMTP_HOST; }
	public static String getSmtpFrom() 				{ return SMTP_FROM; }
	public static String getTARGET_WEBSITE_LINK()	{ return TARGET_WEBSITE_LINK; }
	public static String getRECEIVER_EMAIL()		{ return RECEIVER_EMAIL; }
	public static String getSENDER_EMAIL()			{ return SENDER_EMAIL; }
	public static String getHTTPS_WIKI_ADDRESS()	{ return HTTPS_WIKI_ADDRESS; }
	public static String getGUEST_GROUP_NAME()		{ return GUEST_GROUP_NAME; }
	public static String getGUEST_ACCOUNT_NAME()	{ return GUEST_ACCOUNT_NAME; }
	public static String getGUEST_ACCOUNT_PASSWORD(){ return GUEST_ACCOUNT_PASSWORD; }
	public static String getADMIN_GROUP_NAME()		{ return ADMIN_GROUP_NAME; }
	public static String getADMIN_ACCOUNT_PASSWORD(){ return ADMIN_ACCOUNT_PASSWORD; }
	public static String getADMIN_ACCOUNT_EMAIL()	{ return ADMIN_ACCOUNT_EMAIL; }
	public static String getWIKI_PREFIX()			{ return WIKI_PREFIX; }
	//public static String getPRINTING_DIR_NAME()		{ return PRINTING_DIR_NAME; }
	public static int 	getMAX_SHORT_DESCRIPTION_LENGTH()				{ return MAX_SHORT_DESCRIPTION_LENGTH; }
	public static int 	getMAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE()	{ return MAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE; }
	public static int   getMAX_COMMENT_DELETION_HOURS()     			{ return MAX_COMMENT_DELETION_HOURS;}
	public static boolean getTABLE_INPUT_YEAR_RESTRICTION_ON()			{ return TABLE_INPUT_YEAR_RESTRICTION_ON; }
	
	////////////////////////////////////////////////////////////////////
	//Environment variables:
	////////////////////////////////////////////////////////////////////
	
	private static final String ENV_KEY_WIKI_ADDRESS 		= "strategia_kartta_wiki_address";
	private static final String ENV_KEY_SMTP_LOCAL_HOST 	= "strategia_kartta_smtp_localhost";
	private static final String ENV_KEY_SMTP_HOST 			= "strategia_kartta_smtp_host";
	private static final String ENV_KEY_SMTP_FROM 			= "strategia_kartta_smtp_from";
	private static final String ENV_TARGET_WEBSITE_LINK 	= "strategia_kartta_target_website_link";
	private static final String ENV_RECEIVER_EMAIL 			= "strategia_kartta_receiver_email";
	private static final String ENV_SENDER_EMAIL 			= "strategia_kartta_sender_email";
	private static final String ENV_HTTPS_WIKI_ADDRESS 		= "strategia_kartta_https_wiki_address";
	private static final String ENV_GUEST_GROUP_NAME 		= "strategia_kartta_guest_group_name";
	private static final String ENV_GUEST_ACCOUNT_NAME 		= "strategia_kartta_guest_account_name";
	private static final String ENV_GUEST_ACCOUNT_PASSWORD 	= "strategia_kartta_guest_account_password";
	private static final String ENV_ADMIN_GROUP_NAME 		= "strategia_kartta_admin_group_name";
	private static final String ENV_ADMIN_ACCOUNT_PASSWORD 	= "strategia_kartta_admin_account_password";
	private static final String ENV_ADMIN_ACCOUNT_EMAIL 	= "strategia_kartta_admin_account_email";
	private static final String ENV_WIKI_PREFIX 			= "strategia_kartta_wiki_prefix";
	private static final String ENV_MAX_SHORT_DESCRIPTION_LENGTH = "strategia_kartta_max_short_description_length";
	private static final String ENV_MAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE = "strategia_kartta_max_visible_change_suggestions_per_page";
	private static final String ENV_MAX_COMMENT_DELETION_HOURS = "strategia_kartta_max_comment_deletion_hours";
	private static final String ENV_TABLE_INPUT_YEAR_RESTRICTION_ON = "strategia_kartta_table_input_year_restriction_on";
	
	/**
	 * Initialize constants by reading environment variables. Do only once.
	 */
	public static void init() {
		if(!initialized) {
		//Initialize constants
			updateWikiAddress();
			updateSmtpLocalHost();
			updateSmtpHost();
			updateSmtpFrom();
			updateTARGET_WEBSITE_LINK();
			updateRECEIVER_EMAIL();
			updateSENDER_EMAIL();
			updateHTTPS_WIKI_ADDRESS();
			updateGUEST_GROUP_NAME();
			updateGUEST_ACCOUNT_NAME();
			updateGUEST_ACCOUNT_PASSWORD();
			updateADMIN_GROUP_NAME();
			updateADMIN_ACCOUNT_PASSWORD();
			updateADMIN_ACCOUNT_EMAIL();
			updateWIKI_PREFIX();
			updateMAX_SHORT_DESCRIPTION_LENGTH();
			updateMAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE();
			updateMAX_COMMENT_DELETION_HOURS();
			updateTABLE_INPUT_YEAR_RESTRICTION_ON();
		} else {
			System.out.println("Constants already initialized!");
		}
	}
	
	private static boolean initialized = false;
	
	////////////////////////////////////////////////////////////////////
	//Update functions:
	////////////////////////////////////////////////////////////////////
	
	private static void updateMAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE() {
		String systemValue = System.getenv(ENV_MAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE);
		if(systemValue == null) {
			MAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE = DEFAULT_MAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE;
		} else {
			try {
				MAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE = Integer.parseInt(systemValue);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				MAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE = DEFAULT_MAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE;
			}
		}
		System.out.println("Using MAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE " + MAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE);
	}
	
	private static void updateMAX_SHORT_DESCRIPTION_LENGTH() {
		String systemValue = System.getenv(ENV_MAX_SHORT_DESCRIPTION_LENGTH);
		if(systemValue == null) {
			MAX_SHORT_DESCRIPTION_LENGTH = DEFAULT_MAX_SHORT_DESCRIPTION_LENGTH;
		} else {
			try {
			MAX_SHORT_DESCRIPTION_LENGTH = Integer.parseInt(systemValue);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				MAX_SHORT_DESCRIPTION_LENGTH = DEFAULT_MAX_SHORT_DESCRIPTION_LENGTH;
			}
		}
		System.out.println("Using MAX_SHORT_DESCRIPTION_LENGTH " + MAX_SHORT_DESCRIPTION_LENGTH);
	}
	
	private static void updateHTTPS_WIKI_ADDRESS() {
		String systemValue = System.getenv(ENV_HTTPS_WIKI_ADDRESS);
		if(systemValue == null) {
			HTTPS_WIKI_ADDRESS = DEFAULT_HTTPS_WIKI_ADDRESS;
		} else {
			HTTPS_WIKI_ADDRESS = systemValue;
		}
		System.out.println("Using HTTPS_WIKI_ADDRESS " + HTTPS_WIKI_ADDRESS);
	}
	
	private static void updateGUEST_GROUP_NAME() {
		String systemValue = System.getenv(ENV_GUEST_GROUP_NAME);
		if(systemValue == null) {
			GUEST_GROUP_NAME = DEFAULT_GUEST_GROUP_NAME;
		} else {
			GUEST_GROUP_NAME = systemValue;
		}
		System.out.println("Using GUEST_GROUP_NAME " + GUEST_GROUP_NAME);
	}
	
	private static void updateGUEST_ACCOUNT_NAME() {
		String systemValue = System.getenv(ENV_GUEST_ACCOUNT_NAME);
		if(systemValue == null) {
			GUEST_ACCOUNT_NAME = DEFAULT_GUEST_ACCOUNT_NAME;
		} else {
			GUEST_ACCOUNT_NAME = systemValue;
		}
		System.out.println("Using GUEST_ACCOUNT_NAME " + GUEST_ACCOUNT_NAME);
	}
	
	private static void updateGUEST_ACCOUNT_PASSWORD() {
		String systemValue = System.getenv(ENV_GUEST_ACCOUNT_PASSWORD);
		if(systemValue == null) {
			GUEST_ACCOUNT_PASSWORD = DEFAULT_GUEST_ACCOUNT_PASSWORD;
		} else {
			GUEST_ACCOUNT_PASSWORD = systemValue;
		}
		System.out.println("Using GUEST_ACCOUNT_PASSWORD " + GUEST_ACCOUNT_PASSWORD);
	}
	
	private static void updateADMIN_GROUP_NAME() {
		String systemValue = System.getenv(ENV_ADMIN_GROUP_NAME);
		if(systemValue == null) {
			ADMIN_GROUP_NAME = DEFAULT_ADMIN_GROUP_NAME;
		} else {
			ADMIN_GROUP_NAME = systemValue;
		}
		System.out.println("Using ADMIN_GROUP_NAME " + ADMIN_GROUP_NAME);
	}
	
	private static void updateADMIN_ACCOUNT_PASSWORD() {
		String systemValue = System.getenv(ENV_ADMIN_ACCOUNT_PASSWORD);
		if(systemValue == null) {
			ADMIN_ACCOUNT_PASSWORD = DEFAULT_ADMIN_ACCOUNT_PASSWORD;
		} else {
			ADMIN_ACCOUNT_PASSWORD = systemValue;
		}
		System.out.println("Using ADMIN_ACCOUNT_PASSWORD " + ADMIN_ACCOUNT_PASSWORD);
	}
	private static void updateADMIN_ACCOUNT_EMAIL() {
		String systemValue = System.getenv(ENV_ADMIN_ACCOUNT_EMAIL);
		if(systemValue == null) {
			ADMIN_ACCOUNT_EMAIL = DEFAULT_ADMIN_ACCOUNT_EMAIL;
		} else {
			ADMIN_ACCOUNT_EMAIL = systemValue;
		}
		System.out.println("Using ADMIN_ACCOUNT_EMAIL " + ADMIN_ACCOUNT_EMAIL);
	}
	
	private static void updateWIKI_PREFIX() {
		String systemValue = System.getenv(ENV_WIKI_PREFIX);
		if(systemValue == null) {
			WIKI_PREFIX = DEFAULT_WIKI_PREFIX;
		} else {
			WIKI_PREFIX = systemValue;
		}
		System.out.println("Using WIKI_PREFIX " + WIKI_PREFIX);
	}
	
	private static void updateSENDER_EMAIL() {
		String systemValue = System.getenv(ENV_SENDER_EMAIL);
		if(systemValue == null) {
			SENDER_EMAIL = DEFAULT_SENDER_EMAIL;
		} else {
			SENDER_EMAIL = systemValue;
		}
		System.out.println("Using SENDER_EMAIL " + SENDER_EMAIL);
	}
	
	private static void updateRECEIVER_EMAIL() {
		String systemValue = System.getenv(ENV_RECEIVER_EMAIL);
		if(systemValue == null) {
			RECEIVER_EMAIL = DEFAULT_RECEIVER_EMAIL;
		} else {
			RECEIVER_EMAIL = systemValue;
		}
		System.out.println("Using RECEIVER_EMAIL " + RECEIVER_EMAIL);
	}
	
	private static void updateTARGET_WEBSITE_LINK() {
		String systemValue = System.getenv(ENV_TARGET_WEBSITE_LINK);
		if(systemValue == null) {
			TARGET_WEBSITE_LINK = DEFAULT_TARGET_WEBSITE_LINK;
		} else {
			TARGET_WEBSITE_LINK = systemValue;
		}
		System.out.println("Using TARGET_WEBSITE_LINK " + TARGET_WEBSITE_LINK);
	}
	
	private static void updateWikiAddress() {
		String systemEnvWikiAddress = System.getenv(ENV_KEY_WIKI_ADDRESS);
		if(systemEnvWikiAddress == null) {
			WIKI_ADDRESS = DEFAULT_WIKI_ADDRESS;
		} else {
			WIKI_ADDRESS = systemEnvWikiAddress;
		}
		System.out.println("Using WIKI_ADDRESS: " + WIKI_ADDRESS);
	}
	
	private static void updateSmtpLocalHost() {
		String smtpEnvLocalHost = System.getenv(ENV_KEY_SMTP_LOCAL_HOST); 
		if(smtpEnvLocalHost == null) {
			SMTP_LOCAL_HOST = DEFAULT_SMTP_LOCAL_HOST;
		} else {
			SMTP_LOCAL_HOST = smtpEnvLocalHost;
		}
		System.out.println("Using SMTP_LOCAL_HOST: " + SMTP_LOCAL_HOST);
	}
	
	private static void updateSmtpHost() {
		String smtpEnvHost = System.getenv(ENV_KEY_SMTP_HOST); 
		if(smtpEnvHost == null) {
			SMTP_HOST = DEFAULT_SMTP_HOST;
		} else {
			SMTP_HOST = smtpEnvHost;
		}
		System.out.println("Using SMTP_HOST: " + SMTP_HOST);
	}
	
	private static void updateSmtpFrom() {
		String smtpEnvFrom = System.getenv(ENV_KEY_SMTP_FROM); 
		if(smtpEnvFrom == null) {
			SMTP_FROM = DEFAULT_SMTP_FROM;
		} else {
			SMTP_FROM = smtpEnvFrom;
		}
		System.out.println("Using SMTP_FROM: " + SMTP_FROM);
	}
	
	private static void updateMAX_COMMENT_DELETION_HOURS() {
		String value = System.getenv(ENV_MAX_COMMENT_DELETION_HOURS); 
		if(value == null) {
			MAX_COMMENT_DELETION_HOURS = DEFAULT_MAX_COMMENT_DELETION_HOURS;
		} else {
			try {
				int i = Integer.parseInt(value);
				if(i > 0) {
					MAX_COMMENT_DELETION_HOURS = i;
				} else {
					System.err.println("Error pasing max_comment_deletion_hours: Smaller than 0! Set to default.");
					MAX_COMMENT_DELETION_HOURS = DEFAULT_MAX_COMMENT_DELETION_HOURS;
				}
			} catch (NumberFormatException e) {
				System.err.println("Error pasing max_comment_deletion_hours. Set to default.");
				MAX_COMMENT_DELETION_HOURS = DEFAULT_MAX_COMMENT_DELETION_HOURS;
			}
		}
		
		System.out.println("Using MAX_COMMENT_DELETION_HOURS: " + MAX_COMMENT_DELETION_HOURS);
	}

	private static void updateTABLE_INPUT_YEAR_RESTRICTION_ON() {
		String restrictionOn = System.getenv(ENV_TABLE_INPUT_YEAR_RESTRICTION_ON); 
		if(restrictionOn == null) {
			TABLE_INPUT_YEAR_RESTRICTION_ON = DEFAULT_TABLE_INPUT_YEAR_RESTRICTION_ON;
		} else {
			Boolean b = Boolean.parseBoolean(restrictionOn.toLowerCase());
			TABLE_INPUT_YEAR_RESTRICTION_ON = b;
		}
		System.out.println("Using TABLE_INPUT_YEAR_RESTRICTION_ON: " + TABLE_INPUT_YEAR_RESTRICTION_ON);
	}
	
}

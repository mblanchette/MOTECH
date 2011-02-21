/**
 * MOTECH PLATFORM OPENSOURCE LICENSE AGREEMENT
 *
 * Copyright (c) 2010 The Trustees of Columbia University in the City of
 * New York and Grameen Foundation USA.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Grameen Foundation USA, Columbia University, or
 * their respective contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GRAMEEN FOUNDATION USA, COLUMBIA UNIVERSITY
 * AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL GRAMEEN FOUNDATION
 * USA, COLUMBIA UNIVERSITY OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.motechproject.mobile.smslibgw;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.http.BulkSmsHTTPGateway;

/**
 * An implementation of both the sending and receiving side of the MoTeCH mobile
 * messaging interfaces, to work with a bulk sms gateway via smslib.
 * 
 * @author batkinson
 * @author mblanchette
 * 
 */
public class BulkSmsGatewayImpl extends SmslibGatewayImpl {

	private Log log = LogFactory.getLog(BulkSmsGatewayImpl.class);

	private String gatewayId = "bulksms";
	private String username = null;
	private String password = null;
	private String providerUrl = "http://usa.bulksms.com:5567";
	private String encoding = "ISO-8859-1";

	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setProviderUrl(String providerUrl) {
		this.providerUrl = providerUrl;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Creates the bulk sms gateway implementation to use. Throws an exception
	 * if the providerUrl cannot be set.
	 * 
	 * @throws GatewayException
	 */
	@Override
	AGateway createGateway() throws GatewayException {
		if (username == null || password == null) {
			throw new GatewayException(
					"Username or password missing for Bulk SMS Gateway");
		}
		BulkSmsHTTPGateway gw = new BulkSmsHTTPGateway(gatewayId, username,
				password);

		/*
		 * This bit is required because smslib hard-codes the server URL and
		 * didn't provide a way to override it. It appears that bulksms migrated
		 * to a new location. Reflection-san, arigatou gozaimasu!!
		 */
		Field providerField;
		try {
			providerField = gw.getClass().getDeclaredField("providerUrl");
			providerField.setAccessible(true);
			providerField.set(gw, providerUrl);
		} catch (Exception e) {
			throw new GatewayException(
					"Failed to set providerUrl on Bulk SMS Gateway using reflection");
		}

		return gw;
	}

	/**
	 * Encodes the outbound message text so that the HTTP sending operation
	 * succeeds when strange characters are present. Returns original message if
	 * encoding fails.
	 */
	@Override
	public String handleOutboundMessageText(String messageText) {
		try {
			return URLEncoder.encode(messageText, encoding);
		} catch (UnsupportedEncodingException e) {
			log.error("Invalid encoding for Bulk SMS Gateway: " + encoding, e);
		}
		return messageText;
	}
}

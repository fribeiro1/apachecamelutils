/*
 * Copyright 2011 Upic
 * 
 * This file is part of upic-ldap.
 *
 * upic-ldap is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * upic-ldap is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with upic-ldap. If not, see <http://www.gnu.org/licenses/>.
 */
package br.com.upic.camel.ldap;

import static br.com.upic.camel.ldap.LdapConstants.HEADER_PASSWORD;
import static br.com.upic.camel.ldap.LdapConstants.HEADER_USER;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LdapEndpoint extends ProcessorEndpoint {
	private static Log LOG = LogFactory.getLog("upic-ldap");

	private String initialContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";

	private String providerUrl;

	private String securityAuthentication;

	public LdapEndpoint(String uri, String providerUrl,
			LdapComponent component) {
		super(uri, component);

		this.providerUrl = providerUrl;
	}

	@Override
	protected void onExchange(Exchange exchange) throws Exception {
		LOG.info("Setting up the context");

		Hashtable<String, String> conf = new Hashtable<String, String>();

		LOG.debug("Initial Context Factory = " + initialContextFactory);

		conf.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);

		LOG.debug("Provider URL = " + providerUrl);

		conf.put(Context.PROVIDER_URL, providerUrl);

		LOG.debug("Security Authentication = " + securityAuthentication);

		conf.put(Context.SECURITY_AUTHENTICATION, securityAuthentication);

		Message in = exchange.getIn();

		String user = in.getHeader(HEADER_USER, String.class);

		LOG.debug("User = " + user);

		conf.put(Context.SECURITY_PRINCIPAL, user);

		String password = in.getHeader(HEADER_PASSWORD, String.class);

		LOG.debug("Password = " + password);

		conf.put(Context.SECURITY_CREDENTIALS, password);

		LOG.info("Authenticating in directory");

		Message out = exchange.getOut();

		try {
			new InitialContext(conf);

			out.setBody(true);
		} catch (AuthenticationException e) {
			LOG.error(e.getMessage(), e);

			out.setBody(false);
		}

	}

	public void setInitialContextFactory(String initialContextFactory) {
		this.initialContextFactory = initialContextFactory;
	}

	public void setSecurityAuthentication(String securityAuthentication) {
		this.securityAuthentication = securityAuthentication;
	}

}
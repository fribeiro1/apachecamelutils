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

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

public class LdapComponent extends DefaultComponent {

	@Override
	protected Endpoint createEndpoint(String uri, String remaining,
			Map<String, Object> params) {
		return new LdapEndpoint(uri, remaining, this);
	}

}
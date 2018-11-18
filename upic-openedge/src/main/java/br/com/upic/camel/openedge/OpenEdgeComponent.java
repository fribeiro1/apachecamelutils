/*
 * Copyright 2011 Upic
 * 
 * This file is part of upic-openedge.
 *
 * upic-openedge is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * upic-openedge is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with upic-openedge. If not, see <http://www.gnu.org/licenses/>.
 */
package br.com.upic.camel.openedge;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

public final class OpenEdgeComponent extends DefaultComponent {

	@Override
	protected Endpoint createEndpoint(final String uri, final String remaining,
			final Map<String, Object> params) throws Exception {
		return new OpenEdgeEndpoint(uri, remaining, this);
	}

}
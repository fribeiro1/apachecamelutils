/*
 * Copyright 2011 Upic
 * 
 * This file is part of Apache Camel Utils.
 *
 * Apache Camel Utils is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Apache Camel Utils is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with Apache Camel Utils. If not, see <http://www.gnu.org/licenses/>.
 */
package br.com.upic.camel.json;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.json.JSONObject;

@Converter
public final class JsonTypeConverter {

	@Converter
	public static byte[] toByteArray(final JSONObject value,
			final Exchange exchange) {
		return (byte[]) exchange.getContext().getTypeConverter().convertTo(
				byte[].class, value.toString());
	}

}
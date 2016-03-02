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

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.camel.Exchange;
import org.apache.camel.TypeConverter;
import org.apache.camel.spi.DataFormat;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;

public final class JsonDataFormat implements DataFormat {
	private static final String DEFAULT_KEY = "result";

	@Override
	public void marshal(final Exchange exchange, final Object graph,
			final OutputStream stream) throws Exception {
		final Object body = exchange.getIn().getBody();

		if (body instanceof JSONObject)
			return;

		final TypeConverter converter = exchange.getContext()
				.getTypeConverter();

		if (!(body instanceof Document)) {
			final JSONObject obj = new JSONObject();

			obj.put(DEFAULT_KEY, body);

			stream.write((byte[]) (converter.convertTo(byte[].class, exchange,
					obj)));
		} else {
			stream.write((byte[]) (converter.convertTo(byte[].class, exchange,
					XML.toJSONObject(converter.convertTo(String.class,
							exchange, body)))));
		}

		exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
	}

	@Override
	public Object unmarshal(final Exchange exchange, final InputStream stream) {
		throw new UnsupportedOperationException();
	}

}
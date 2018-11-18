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

import java.util.Iterator;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;

import br.com.upic.schemas.camel.openedge._2012._12.CharField;
import br.com.upic.schemas.camel.openedge._2012._12.DateField;
import br.com.upic.schemas.camel.openedge._2012._12.DateTimeField;
import br.com.upic.schemas.camel.openedge._2012._12.DateTimeTZField;
import br.com.upic.schemas.camel.openedge._2012._12.DecField;
import br.com.upic.schemas.camel.openedge._2012._12.Field;
import br.com.upic.schemas.camel.openedge._2012._12.IntField;
import br.com.upic.schemas.camel.openedge._2012._12.LogField;
import br.com.upic.schemas.camel.openedge._2012._12.Row;

import com.progress.open4gl.InputResultSet;

public final class OpenEdgeResultSet extends InputResultSet {
	private Row currentRow;

	private Iterator<Row> rowIterator;

	public OpenEdgeResultSet(final Iterator<Row> rowIterator) {
		this.rowIterator = rowIterator;
	}

	public Object getObject(final int columnIndex) {
		final JAXBElement<Field> fieldElement = currentRow
				.getFieldElementList().get(columnIndex - 1);

		if (!fieldElement.isNil()) {
			final Field field = fieldElement.getValue();

			if (field instanceof CharField)
				return DatatypeConverter.parseString((String) field
						.getContent().get(0));
			else if (field instanceof DateField)
				return DatatypeConverter.parseDate((String) field.getContent()
						.get(0));
			else if (field instanceof DateTimeField)
				return DatatypeConverter.parseDateTime((String) field
						.getContent().get(0));
			else if (field instanceof DateTimeTZField)
				return DatatypeConverter.parseDateTime((String) field
						.getContent().get(0));
			else if (field instanceof DecField)
				return DatatypeConverter.parseDecimal((String) field
						.getContent().get(0));
			else if (field instanceof IntField)
				return DatatypeConverter.parseInt((String) field.getContent()
						.get(0));
			else if (field instanceof LogField)
				return DatatypeConverter.parseBoolean((String) field
						.getContent().get(0));

		}

		return null;
	}

	public boolean next() {
		final boolean result = rowIterator.hasNext();

		if (result)
			currentRow = (Row) rowIterator.next();

		return result;
	}

}
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

import static br.com.upic.camel.openedge.OpenEdgeConstants.SESSION_MODEL_FREE;
import static br.com.upic.camel.openedge.OpenEdgeConstants.SESSION_MODEL_MANAGED;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.Exchange;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import br.com.upic.schemas.camel.openedge._2012._12.CharArrayParam;
import br.com.upic.schemas.camel.openedge._2012._12.CharField;
import br.com.upic.schemas.camel.openedge._2012._12.CharFieldMetaData;
import br.com.upic.schemas.camel.openedge._2012._12.CharParam;
import br.com.upic.schemas.camel.openedge._2012._12.DateArrayParam;
import br.com.upic.schemas.camel.openedge._2012._12.DateField;
import br.com.upic.schemas.camel.openedge._2012._12.DateFieldMetaData;
import br.com.upic.schemas.camel.openedge._2012._12.DateParam;
import br.com.upic.schemas.camel.openedge._2012._12.DateTimeArrayParam;
import br.com.upic.schemas.camel.openedge._2012._12.DateTimeField;
import br.com.upic.schemas.camel.openedge._2012._12.DateTimeFieldMetaData;
import br.com.upic.schemas.camel.openedge._2012._12.DateTimeParam;
import br.com.upic.schemas.camel.openedge._2012._12.DateTimeTZArrayParam;
import br.com.upic.schemas.camel.openedge._2012._12.DateTimeTZField;
import br.com.upic.schemas.camel.openedge._2012._12.DateTimeTZFieldMetaData;
import br.com.upic.schemas.camel.openedge._2012._12.DateTimeTZParam;
import br.com.upic.schemas.camel.openedge._2012._12.DecArrayParam;
import br.com.upic.schemas.camel.openedge._2012._12.DecField;
import br.com.upic.schemas.camel.openedge._2012._12.DecFieldMetaData;
import br.com.upic.schemas.camel.openedge._2012._12.DecParam;
import br.com.upic.schemas.camel.openedge._2012._12.FieldMetaData;
import br.com.upic.schemas.camel.openedge._2012._12.IntArrayParam;
import br.com.upic.schemas.camel.openedge._2012._12.IntField;
import br.com.upic.schemas.camel.openedge._2012._12.IntFieldMetaData;
import br.com.upic.schemas.camel.openedge._2012._12.IntParam;
import br.com.upic.schemas.camel.openedge._2012._12.LogArrayParam;
import br.com.upic.schemas.camel.openedge._2012._12.LogField;
import br.com.upic.schemas.camel.openedge._2012._12.LogFieldMetaData;
import br.com.upic.schemas.camel.openedge._2012._12.LogParam;
import br.com.upic.schemas.camel.openedge._2012._12.ObjectFactory;
import br.com.upic.schemas.camel.openedge._2012._12.OpenEdgeRequest;
import br.com.upic.schemas.camel.openedge._2012._12.OpenEdgeResponse;
import br.com.upic.schemas.camel.openedge._2012._12.Param;
import br.com.upic.schemas.camel.openedge._2012._12.Row;
import br.com.upic.schemas.camel.openedge._2012._12.TempTableMetaData;
import br.com.upic.schemas.camel.openedge._2012._12.TempTableParam;

import com.progress.open4gl.Parameter;
import com.progress.open4gl.ProResultSet;
import com.progress.open4gl.ProResultSetMetaDataImpl;
import com.progress.open4gl.RunTime4GLErrorException;
import com.progress.open4gl.RunTimeProperties;
import com.progress.open4gl.javaproxy.OpenAppObject;
import com.progress.open4gl.javaproxy.ParamArray;

public final class OpenEdgeEndpoint extends ProcessorEndpoint {
	private static final DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory
			.newInstance();

	private static final Log LOG = LogFactory.getLog("upic-openedge");

	private static final String NS = "http://schemas.upic.com.br/camel/openedge/2012/12";

	private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

	private static final int PARAM_MODE_INPUT = 1;

	private static final int PARAM_MODE_INPUT_OUTPUT = 3;

	private DocumentBuilder builder;

	private Marshaller marshaller;

	private String password = "";

	private String sessionModel = SESSION_MODEL_FREE;

	private Unmarshaller unmarshaller;

	private String url;

	private String user = "";

	public OpenEdgeEndpoint(final String uri, final String url,
			final OpenEdgeComponent component) throws Exception {
		super(uri, component);

		this.url = url;

		builder = BUILDER_FACTORY.newDocumentBuilder();

		final JAXBContext ctx = JAXBContext.newInstance(
				"br.com.upic.schemas.camel.openedge._2012._12", getClass()
						.getClassLoader());

		marshaller = ctx.createMarshaller();

		unmarshaller = ctx.createUnmarshaller();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void onExchange(final Exchange exchange) throws Exception {

		try {
			LOG.info("Unmarshalling the request");

			final OpenEdgeRequest req = (OpenEdgeRequest) unmarshaller
					.unmarshal(exchange.getIn().getBody(Document.class));

			LOG.info("Setting the input params");

			final List<JAXBElement<Param>> reqParamElementList = req
					.getParamElementList();

			final ParamArray proParams = new ParamArray(
					reqParamElementList.size());

			for (final JAXBElement<Param> reqParamElement : reqParamElementList) {
				final Param reqParam = reqParamElement.getValue();

				final int paramMode = reqParam.getMode();

				final boolean paramNil = reqParamElement.isNil();

				final int paramId = reqParam.getId();

				if (reqParam instanceof CharArrayParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil)) {
						final List<String> strList = new ArrayList<String>();

						for (final JAXBElement<String> valueElement : reqParam
								.getValueElementList()) {

							if (!valueElement.isNil())
								strList.add(DatatypeConverter
										.parseString(valueElement.getValue()));
							else
								strList.add(null);

						}

						proParams.addCharacterArray(paramId,
								strList.toArray(new String[] {}), paramMode,
								strList.size());
					} else {
						proParams
								.addCharacterArray(paramId, null, paramMode, 0);
					}

				} else if (reqParam instanceof CharParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil))
						proParams.addCharacter(
								paramId,
								DatatypeConverter.parseString(reqParam
										.getValueElementList().get(0)
										.getValue()), paramMode);
					else
						proParams.addCharacter(paramId, null, paramMode);

				} else if (reqParam instanceof DateArrayParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil)) {
						final List<GregorianCalendar> calList = new ArrayList<GregorianCalendar>();

						for (final JAXBElement<String> valueElement : reqParam
								.getValueElementList()) {

							if (!valueElement.isNil()) {
								final GregorianCalendar cal = new GregorianCalendar();

								cal.setTime(DatatypeConverter.parseDate(
										valueElement.getValue()).getTime());

								calList.add(cal);
							} else {
								calList.add(null);
							}

						}

						proParams.addDateArray(paramId,
								calList.toArray(new GregorianCalendar[] {}),
								paramMode, calList.size());
					} else {
						proParams.addDateArray(paramId, null, paramMode, 0);
					}

				} else if (reqParam instanceof DateParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil)) {
						final GregorianCalendar cal = new GregorianCalendar();

						cal.setTime(DatatypeConverter.parseDate(
								reqParam.getValueElementList().get(0)
										.getValue()).getTime());

						proParams.addDate(paramId, cal, paramMode);
					} else {
						proParams.addDate(paramId, null, paramMode);
					}

				} else if (reqParam instanceof DateTimeArrayParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil)) {
						final List<GregorianCalendar> calList = new ArrayList<GregorianCalendar>();

						for (final JAXBElement<String> valueElement : reqParam
								.getValueElementList()) {

							if (!valueElement.isNil()) {
								final GregorianCalendar cal = new GregorianCalendar();

								cal.setTime(DatatypeConverter.parseDateTime(
										valueElement.getValue()).getTime());

								calList.add(cal);
							} else {
								calList.add(null);
							}

						}

						proParams.addDatetimeArray(paramId,
								calList.toArray(new GregorianCalendar[] {}),
								paramMode, calList.size());
					} else {
						proParams.addDatetimeArray(paramId, null, paramMode, 0);
					}

				} else if (reqParam instanceof DateTimeParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil)) {
						final GregorianCalendar cal = new GregorianCalendar();

						cal.setTime(DatatypeConverter.parseDateTime(
								reqParam.getValueElementList().get(0)
										.getValue()).getTime());

						proParams.addDatetime(paramId, cal, paramMode);
					} else {
						proParams.addDatetime(paramId, null, paramMode);
					}

				} else if (reqParam instanceof DateTimeTZArrayParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil)) {
						final List<GregorianCalendar> calList = new ArrayList<GregorianCalendar>();

						for (final JAXBElement<String> valueElement : reqParam
								.getValueElementList()) {

							if (!valueElement.isNil()) {
								final GregorianCalendar cal = new GregorianCalendar();

								cal.setTime(DatatypeConverter.parseDateTime(
										valueElement.getValue()).getTime());

								calList.add(cal);
							} else {
								calList.add(null);
							}

						}

						proParams.addDatetimeTZArray(paramId,
								calList.toArray(new GregorianCalendar[] {}),
								paramMode, calList.size());
					} else {
						proParams.addDatetimeTZArray(paramId, null, paramMode,
								0);
					}

				} else if (reqParam instanceof DateTimeTZParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil)) {
						final GregorianCalendar cal = new GregorianCalendar();

						cal.setTime(DatatypeConverter.parseDateTime(
								reqParam.getValueElementList().get(0)
										.getValue()).getTime());

						proParams.addDatetimeTZ(paramId, cal, paramMode);
					} else {
						proParams.addDatetimeTZ(paramId, null, paramMode);
					}

				} else if (reqParam instanceof DecArrayParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil)) {
						final List<BigDecimal> bdList = new ArrayList<BigDecimal>();

						for (final JAXBElement<String> valueElement : reqParam
								.getValueElementList()) {

							if (!valueElement.isNil())
								bdList.add(DatatypeConverter
										.parseDecimal(valueElement.getValue()));
							else
								bdList.add(null);

						}

						proParams.addDecimalArray(paramId,
								bdList.toArray(new BigDecimal[] {}), paramMode,
								bdList.size());
					} else {
						proParams.addDecimalArray(paramId, null, paramMode, 0);
					}

				} else if (reqParam instanceof DecParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil))
						proParams.addDecimal(
								paramId,
								DatatypeConverter.parseDecimal(reqParam
										.getValueElementList().get(0)
										.getValue()), paramMode);
					else
						proParams.addDecimal(paramId, null, paramMode);

				} else if (reqParam instanceof IntArrayParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil)) {
						final List<Integer> intList = new ArrayList<Integer>();

						for (final JAXBElement<String> valueElement : reqParam
								.getValueElementList()) {

							if (!reqParamElement.isNil())
								intList.add(DatatypeConverter
										.parseInt(valueElement.getValue()));
							else
								intList.add(null);

						}

						proParams.addIntegerArray(paramId,
								intList.toArray(new Integer[] {}), paramMode,
								intList.size());
					} else {
						proParams.addIntegerArray(paramId, (Integer[]) null,
								paramMode, 0);
					}

				} else if (reqParam instanceof IntParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil))
						proParams.addInteger(
								paramId,
								new Integer(DatatypeConverter.parseInt(reqParam
										.getValueElementList().get(0)
										.getValue())), paramMode);
					else
						proParams.addInteger(paramId, null, paramMode);

				} else if (reqParam instanceof LogArrayParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil)) {
						final List<Boolean> boolList = new ArrayList<Boolean>();

						for (final JAXBElement<String> valueElement : reqParam
								.getValueElementList()) {

							if (!valueElement.isNil())
								boolList.add(DatatypeConverter
										.parseBoolean(valueElement.getValue()));
							else
								boolList.add(null);

						}

						proParams.addLogicalArray(paramId,
								boolList.toArray(new Boolean[] {}), paramMode,
								boolList.size());
					} else {
						proParams.addLogicalArray(paramId, (Boolean[]) null,
								paramMode, 0);
					}

				} else if (reqParam instanceof LogParam) {

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil))
						proParams.addLogical(
								paramId,
								new Boolean(DatatypeConverter
										.parseBoolean(reqParam
												.getValueElementList().get(0)
												.getValue())), paramMode);
					else
						proParams.addLogical(paramId, null, paramMode);

				} else if (reqParam instanceof TempTableParam) {
					final TempTableMetaData ttMetaData = reqParam
							.getTempTableMetaData();

					final List<FieldMetaData> fieldMetaDataList = ttMetaData
							.getFieldMetaDataList();

					final ProResultSetMetaDataImpl proRsMetaData = new ProResultSetMetaDataImpl(
							fieldMetaDataList.size());

					for (final FieldMetaData fieldMetaData : fieldMetaDataList) {

						if (fieldMetaData instanceof CharFieldMetaData)
							proRsMetaData.setFieldMetaData(
									fieldMetaData.getId(),
									fieldMetaData.getName(),
									fieldMetaData.getExtent(),
									Parameter.PRO_CHARACTER);
						else if (fieldMetaData instanceof DateFieldMetaData)
							proRsMetaData.setFieldMetaData(
									fieldMetaData.getId(),
									fieldMetaData.getName(),
									fieldMetaData.getExtent(),
									Parameter.PRO_DATE);
						else if (fieldMetaData instanceof DateTimeFieldMetaData)
							proRsMetaData.setFieldMetaData(
									fieldMetaData.getId(),
									fieldMetaData.getName(),
									fieldMetaData.getExtent(),
									Parameter.PRO_DATETIME);
						else if (fieldMetaData instanceof DateTimeTZFieldMetaData)
							proRsMetaData.setFieldMetaData(
									fieldMetaData.getId(),
									fieldMetaData.getName(),
									fieldMetaData.getExtent(),
									Parameter.PRO_DATETIMETZ);
						else if (fieldMetaData instanceof DecFieldMetaData)
							proRsMetaData.setFieldMetaData(
									fieldMetaData.getId(),
									fieldMetaData.getName(),
									fieldMetaData.getExtent(),
									Parameter.PRO_DECIMAL);
						else if (fieldMetaData instanceof IntFieldMetaData)
							proRsMetaData.setFieldMetaData(
									fieldMetaData.getId(),
									fieldMetaData.getName(),
									fieldMetaData.getExtent(),
									Parameter.PRO_INTEGER);
						else if (fieldMetaData instanceof LogFieldMetaData)
							proRsMetaData.setFieldMetaData(
									fieldMetaData.getId(),
									fieldMetaData.getName(),
									fieldMetaData.getExtent(),
									Parameter.PRO_LOGICAL);

					}

					if (((paramMode == PARAM_MODE_INPUT) || (paramMode == PARAM_MODE_INPUT_OUTPUT))
							&& (!paramNil))
						proParams.addTable(paramId, new OpenEdgeResultSet(
								reqParam.getRowList().iterator()), paramMode,
								proRsMetaData);
					else
						proParams.addTable(paramId, null, paramMode,
								proRsMetaData);

				}

			}

			LOG.info("Connecting to the AppServer");

			if (SESSION_MODEL_MANAGED == sessionModel)
				RunTimeProperties.setSessionModel(0);
			else if (SESSION_MODEL_FREE == sessionModel)
				RunTimeProperties.setSessionModel(1);

			final OpenAppObject proAppObject = new OpenAppObject(url, user,
					password, null, null);

			proAppObject.runProc(req.getProgram(), proParams);

			LOG.info("Marshalling the response");

			final OpenEdgeResponse res = OBJECT_FACTORY
					.createOpenEdgeResponse();

			res.setResult(proParams.getProcReturnString());

			LOG.info("Setting the output params");

			for (final JAXBElement<Param> reqParamElement : reqParamElementList) {
				final Param reqParam = reqParamElement.getValue();

				final int paramMode = reqParam.getMode();

				if (paramMode != PARAM_MODE_INPUT) {
					final int paramId = reqParam.getId();

					final String paramName = reqParam.getName();

					if (reqParam instanceof CharArrayParam) {
						final JAXBElement resParamElement = new JAXBElement<CharArrayParam>(
								new QName(NS, "Param"), CharArrayParam.class,
								null);

						final CharArrayParam resParam = OBJECT_FACTORY
								.createCharArrayParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final String[] proParam = (String[]) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null) {

							for (int i = 0; i < proParam.length; i++) {
								final JAXBElement<String> valueElement = new JAXBElement<String>(
										new QName(NS, "Value"), String.class,
										null);

								if (proParam[i] != null)
									valueElement.setValue(DatatypeConverter
											.printString(proParam[i]));
								else
									valueElement.setNil(true);

								resParam.getValueElementList()
										.add(valueElement);
							}

							resParamElement.setValue(resParam);
						} else {
							resParamElement.setNil(true);
						}

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof CharParam) {
						final JAXBElement resParamElement = new JAXBElement<CharParam>(
								new QName(NS, "Param"), CharParam.class, null);

						final CharParam resParam = OBJECT_FACTORY
								.createCharParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final JAXBElement<String> valueElement = new JAXBElement<String>(
								new QName(NS, "Value"), String.class, null);

						final String proParam = (String) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null)
							valueElement.setValue(DatatypeConverter
									.printString(proParam));
						else
							valueElement.setNil(true);

						resParam.getValueElementList().add(valueElement);

						resParamElement.setValue(resParam);

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof DateArrayParam) {
						final JAXBElement resParamElement = new JAXBElement<DateArrayParam>(
								new QName(NS, "Param"), DateArrayParam.class,
								null);

						final DateArrayParam resParam = OBJECT_FACTORY
								.createDateArrayParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final Date[] proParam = (Date[]) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null) {

							for (int i = 0; i < proParam.length; i++) {
								final JAXBElement<String> valueElement = new JAXBElement<String>(
										new QName(NS, "Value"), String.class,
										null);

								if (proParam[i] != null) {
									final Calendar cal = Calendar.getInstance();

									cal.setTime(proParam[i]);

									valueElement.setValue(DatatypeConverter
											.printDate(cal));
								} else {
									valueElement.setNil(true);
								}

								resParam.getValueElementList()
										.add(valueElement);
							}

							resParamElement.setValue(resParam);
						} else {
							resParamElement.setNil(true);
						}

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof DateParam) {
						final JAXBElement resParamElement = new JAXBElement<DateParam>(
								new QName(NS, "Param"), DateParam.class, null);

						final DateParam resParam = OBJECT_FACTORY
								.createDateParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final JAXBElement<String> valueElement = new JAXBElement<String>(
								new QName(NS, "Value"), String.class, null);

						final Date proParam = (Date) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null) {
							final Calendar cal = Calendar.getInstance();

							cal.setTime(proParam);

							valueElement.setValue(DatatypeConverter
									.printDate(cal));
						} else {
							valueElement.setNil(true);
						}

						resParam.getValueElementList().add(valueElement);

						resParamElement.setValue(resParam);

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof DateTimeArrayParam) {
						final JAXBElement resParamElement = new JAXBElement<DateTimeArrayParam>(
								new QName(NS, "Param"),
								DateTimeArrayParam.class, null);

						final DateTimeArrayParam resParam = OBJECT_FACTORY
								.createDateTimeArrayParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final GregorianCalendar[] proParam = (GregorianCalendar[]) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null) {

							for (int i = 0; i < proParam.length; i++) {
								final JAXBElement<String> valueElement = new JAXBElement<String>(
										new QName(NS, "Value"), String.class,
										null);

								if (proParam[i] != null)
									valueElement.setValue(DatatypeConverter
											.printDateTime(proParam[i]));
								else
									valueElement.setNil(true);

								resParam.getValueElementList()
										.add(valueElement);
							}

							resParamElement.setValue(resParam);
						} else {
							resParamElement.setNil(true);
						}

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof DateTimeParam) {
						final JAXBElement resParamElement = new JAXBElement<DateTimeParam>(
								new QName(NS, "Param"), DateTimeParam.class,
								null);

						final DateTimeParam resParam = OBJECT_FACTORY
								.createDateTimeParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final JAXBElement<String> valueElement = new JAXBElement<String>(
								new QName(NS, "Value"), String.class, null);

						final GregorianCalendar proParam = (GregorianCalendar) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null)
							valueElement.setValue(DatatypeConverter
									.printDateTime(proParam));
						else
							valueElement.setNil(true);

						resParam.getValueElementList().add(valueElement);

						resParamElement.setValue(resParam);

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof DateTimeTZArrayParam) {
						final JAXBElement resParamElement = new JAXBElement<DateTimeTZArrayParam>(
								new QName(NS, "Param"),
								DateTimeTZArrayParam.class, null);

						final DateTimeTZArrayParam resParam = OBJECT_FACTORY
								.createDateTimeTZArrayParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final GregorianCalendar[] proParam = (GregorianCalendar[]) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null) {

							for (int i = 0; i < proParam.length; i++) {
								final JAXBElement<String> valueElement = new JAXBElement<String>(
										new QName(NS, "Value"), String.class,
										null);

								if (proParam[i] != null)
									valueElement.setValue(DatatypeConverter
											.printDateTime(proParam[i]));
								else
									valueElement.setNil(true);

								resParam.getValueElementList()
										.add(valueElement);
							}

							resParamElement.setValue(resParam);
						} else {
							resParamElement.setNil(true);
						}

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof DateTimeTZParam) {
						final JAXBElement resParamElement = new JAXBElement<DateTimeTZParam>(
								new QName(NS, "Param"), DateTimeTZParam.class,
								null);

						final DateTimeTZParam resParam = OBJECT_FACTORY
								.createDateTimeTZParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final JAXBElement<String> valueElement = new JAXBElement<String>(
								new QName(NS, "Value"), String.class, null);

						final GregorianCalendar proParam = (GregorianCalendar) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null)
							valueElement.setValue(DatatypeConverter
									.printDateTime(proParam));
						else
							valueElement.setNil(true);

						resParam.getValueElementList().add(valueElement);

						resParamElement.setValue(resParam);

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof DecArrayParam) {
						final JAXBElement resParamElement = new JAXBElement<DecArrayParam>(
								new QName(NS, "Param"), DecArrayParam.class,
								null);

						final DecArrayParam resParam = OBJECT_FACTORY
								.createDecArrayParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final BigDecimal[] proParam = (BigDecimal[]) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null) {

							for (int i = 0; i < proParam.length; i++) {
								final JAXBElement<String> valueElement = new JAXBElement<String>(
										new QName(NS, "Value"), String.class,
										null);

								if (proParam[i] != null)
									valueElement.setValue(DatatypeConverter
											.printDecimal(proParam[i]));
								else
									valueElement.setNil(true);

								resParam.getValueElementList()
										.add(valueElement);
							}

							resParamElement.setValue(resParam);
						} else {
							resParamElement.setNil(true);
						}

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof DecParam) {
						final JAXBElement resParamElement = new JAXBElement<DecParam>(
								new QName(NS, "Param"), DecParam.class, null);

						final DecParam resParam = OBJECT_FACTORY
								.createDecParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final JAXBElement<String> valueElement = new JAXBElement<String>(
								new QName(NS, "Value"), String.class, null);

						final BigDecimal proParam = (BigDecimal) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null)
							valueElement.setValue(DatatypeConverter
									.printDecimal(proParam));
						else
							valueElement.setNil(true);

						resParam.getValueElementList().add(valueElement);

						resParamElement.setValue(resParam);

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof IntArrayParam) {
						final JAXBElement resParamElement = new JAXBElement<IntArrayParam>(
								new QName(NS, "Param"), IntArrayParam.class,
								null);

						final IntArrayParam resParam = OBJECT_FACTORY
								.createIntArrayParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final Integer[] proParam = (Integer[]) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null) {

							for (int i = 0; i < proParam.length; i++) {
								final JAXBElement<String> valueElement = new JAXBElement<String>(
										new QName(NS, "Value"), String.class,
										null);

								if (proParam[i] != null)
									valueElement.setValue(DatatypeConverter
											.printInt(proParam[i]));
								else
									valueElement.setNil(true);

								resParam.getValueElementList()
										.add(valueElement);
							}

							resParamElement.setValue(resParam);
						} else {
							resParamElement.setNil(true);
						}

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof IntParam) {
						final JAXBElement resParamElement = new JAXBElement<IntParam>(
								new QName(NS, "Param"), IntParam.class, null);

						final IntParam resParam = OBJECT_FACTORY
								.createIntParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final JAXBElement<String> valueElement = new JAXBElement<String>(
								new QName(NS, "Value"), String.class, null);

						final Integer proParam = (Integer) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null)
							valueElement.setValue(DatatypeConverter
									.printInt(proParam));
						else
							valueElement.setNil(true);

						resParam.getValueElementList().add(valueElement);

						resParamElement.setValue(resParam);

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof LogArrayParam) {
						final JAXBElement resParamElement = new JAXBElement<LogArrayParam>(
								new QName(NS, "Param"), LogArrayParam.class,
								null);

						final LogArrayParam resParam = OBJECT_FACTORY
								.createLogArrayParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final Boolean[] proParam = (Boolean[]) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null) {

							for (int i = 0; i < proParam.length; i++) {
								final JAXBElement<String> valueElement = new JAXBElement<String>(
										new QName(NS, "Value"), String.class,
										null);

								if (proParam[i] != null)
									valueElement.setValue(DatatypeConverter
											.printBoolean(proParam[i]));
								else
									valueElement.setNil(true);

								resParam.getValueElementList()
										.add(valueElement);
							}

							resParamElement.setValue(resParam);
						} else {
							resParamElement.setNil(true);
						}

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof LogParam) {
						final JAXBElement resParamElement = new JAXBElement<LogParam>(
								new QName(NS, "Param"), LogParam.class, null);

						final LogParam resParam = OBJECT_FACTORY
								.createLogParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final JAXBElement<String> valueElement = new JAXBElement<String>(
								new QName(NS, "Value"), String.class, null);

						final Boolean proParam = (Boolean) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null)
							valueElement.setValue(DatatypeConverter
									.printBoolean(proParam));
						else
							valueElement.setNil(true);

						resParam.getValueElementList().add(valueElement);

						resParamElement.setValue(resParam);

						res.getParamElementList().add(resParamElement);
					} else if (reqParam instanceof TempTableParam) {
						final JAXBElement resParamElement = new JAXBElement<TempTableParam>(
								new QName(NS, "Param"), TempTableParam.class,
								null);

						final TempTableParam resParam = OBJECT_FACTORY
								.createTempTableParam();

						resParam.setId(paramId);

						resParam.setName(paramName);

						final ProResultSet proParam = (ProResultSet) proParams
								.getOutputParameter(resParam.getId());

						if (proParam != null) {

							while (proParam.next()) {
								final Row row = OBJECT_FACTORY.createRow();

								final TempTableMetaData ttMetaData = reqParam
										.getTempTableMetaData();

								for (final FieldMetaData fieldMetaData : ttMetaData
										.getFieldMetaDataList()) {
									final int fieldExtent = fieldMetaData
											.getExtent();

									final int fieldId = fieldMetaData.getId();

									final String fieldName = fieldMetaData
											.getName();

									if (fieldMetaData instanceof CharFieldMetaData) {

										if (fieldExtent < 1) {
											final JAXBElement fieldElement = new JAXBElement<CharField>(
													new QName(NS, "Field"),
													CharField.class, null);

											final CharField field = OBJECT_FACTORY
													.createCharField();

											field.setId(fieldId);

											field.setName(fieldName);

											final String proValue = proParam
													.getString(field.getId());

											if (proValue != null) {
												field.getContent()
														.add(DatatypeConverter
																.printString(proValue));

												fieldElement.setValue(field);
											} else {
												fieldElement.setNil(true);
											}

											row.getFieldElementList().add(
													fieldElement);
										} else {

											for (int i = 1; i <= fieldExtent; i++) {
												final JAXBElement fieldElement = new JAXBElement<CharField>(
														new QName(NS, "Field"),
														CharField.class, null);

												final CharField field = OBJECT_FACTORY
														.createCharField();

												field.setId(fieldId);

												field.setName(fieldName);

												final String proValue = proParam
														.getString(
																field.getId(),
																i);

												if (proValue != null) {
													field.getContent()
															.add(DatatypeConverter
																	.printString(proValue));

													fieldElement
															.setValue(field);
												} else {
													fieldElement.setNil(true);
												}

												row.getFieldElementList().add(
														fieldElement);
											}

										}

									} else if (fieldMetaData instanceof DateFieldMetaData) {

										if (fieldExtent < 1) {
											final JAXBElement fieldElement = new JAXBElement<DateField>(
													new QName(NS, "Field"),
													DateField.class, null);

											final DateField field = OBJECT_FACTORY
													.createDateField();

											field.setId(fieldId);

											field.setName(fieldName);

											final Date proValue = proParam
													.getDate(field.getId());

											if (proValue != null) {
												final Calendar cal = Calendar
														.getInstance();

												cal.setTime(proValue);

												field.getContent()
														.add(DatatypeConverter
																.printDate(cal));

												fieldElement.setValue(field);
											} else {
												fieldElement.setNil(true);
											}

											row.getFieldElementList().add(
													fieldElement);
										} else {

											for (int i = 1; i <= fieldExtent; i++) {
												final JAXBElement fieldElement = new JAXBElement<DateField>(
														new QName(NS, "Field"),
														DateField.class, null);

												final DateField field = OBJECT_FACTORY
														.createDateField();

												field.setId(fieldId);

												field.setName(fieldName);

												final Date proValue = proParam
														.getDate(field.getId(),
																i);

												if (proValue != null) {
													final Calendar cal = Calendar
															.getInstance();

													cal.setTime(proValue);

													field.getContent()
															.add(DatatypeConverter
																	.printDate(cal));

													fieldElement
															.setValue(field);
												} else {
													fieldElement.setNil(true);
												}

												row.getFieldElementList().add(
														fieldElement);
											}

										}

									} else if (fieldMetaData instanceof DateTimeFieldMetaData) {

										if (fieldExtent < 1) {
											final JAXBElement fieldElement = new JAXBElement<DateTimeField>(
													new QName(NS, "Field"),
													DateTimeField.class, null);

											final DateTimeField field = OBJECT_FACTORY
													.createDateTimeField();

											field.setId(fieldId);

											field.setName(fieldName);

											final GregorianCalendar proValue = (GregorianCalendar) proParam
													.getObject(field.getId());

											if (proValue != null) {
												field.getContent()
														.add(DatatypeConverter
																.printDateTime(proValue));

												fieldElement.setValue(field);
											} else {
												fieldElement.setNil(true);
											}

											row.getFieldElementList().add(
													fieldElement);
										} else {

											for (int i = 1; i <= fieldExtent; i++) {
												final JAXBElement fieldElement = new JAXBElement<DateTimeField>(
														new QName(NS, "Field"),
														DateTimeField.class,
														null);

												final DateTimeField field = OBJECT_FACTORY
														.createDateTimeField();

												field.setId(fieldId);

												field.setName(fieldName);

												final GregorianCalendar proValue = (GregorianCalendar) proParam
														.getObject(
																field.getId(),
																i);

												if (proValue != null) {
													field.getContent()
															.add(DatatypeConverter
																	.printDateTime(proValue));

													fieldElement
															.setValue(field);
												} else {
													fieldElement.setNil(true);
												}

												row.getFieldElementList().add(
														fieldElement);
											}

										}

									} else if (fieldMetaData instanceof DateTimeTZFieldMetaData) {

										if (fieldExtent < 1) {
											final JAXBElement fieldElement = new JAXBElement<DateTimeTZField>(
													new QName(NS, "Field"),
													DateTimeTZField.class, null);

											final DateTimeTZField field = OBJECT_FACTORY
													.createDateTimeTZField();

											field.setId(fieldId);

											field.setName(fieldName);

											final GregorianCalendar proValue = (GregorianCalendar) proParam
													.getObject(field.getId());

											if (proValue != null) {
												field.getContent()
														.add(DatatypeConverter
																.printDateTime(proValue));

												fieldElement.setValue(field);
											} else {
												fieldElement.setNil(true);
											}

											row.getFieldElementList().add(
													fieldElement);
										} else {

											for (int i = 1; i <= fieldExtent; i++) {
												final JAXBElement fieldElement = new JAXBElement<DateTimeTZField>(
														new QName(NS, "Field"),
														DateTimeTZField.class,
														null);

												final DateTimeTZField field = OBJECT_FACTORY
														.createDateTimeTZField();

												field.setId(fieldId);

												field.setName(fieldName);

												final GregorianCalendar proValue = (GregorianCalendar) proParam
														.getObject(
																field.getId(),
																i);

												if (proValue != null) {
													field.getContent()
															.add(DatatypeConverter
																	.printDateTime(proValue));

													fieldElement
															.setValue(field);
												} else {
													fieldElement.setNil(true);
												}

												row.getFieldElementList().add(
														fieldElement);
											}

										}

									} else if (fieldMetaData instanceof DecFieldMetaData) {

										if (fieldExtent < 1) {
											final JAXBElement fieldElement = new JAXBElement<DecField>(
													new QName(NS, "Field"),
													DecField.class, null);

											final DecField field = OBJECT_FACTORY
													.createDecField();

											field.setId(fieldId);

											field.setName(fieldName);

											final BigDecimal proValue = proParam
													.getBigDecimal(field
															.getId());

											if (proValue != null) {
												field.getContent()
														.add(DatatypeConverter
																.printDecimal(proValue));

												fieldElement.setValue(field);
											} else {
												fieldElement.setNil(true);
											}

											row.getFieldElementList().add(
													fieldElement);
										} else {

											for (int i = 1; i <= fieldExtent; i++) {
												final JAXBElement fieldElement = new JAXBElement<DecField>(
														new QName(NS, "Field"),
														DecField.class, null);

												final DecField field = OBJECT_FACTORY
														.createDecField();

												field.setId(fieldId);

												field.setName(fieldName);

												final BigDecimal proValue = proParam
														.getBigDecimal(
																field.getId(),
																i, 0);

												if (proValue != null) {
													field.getContent()
															.add(DatatypeConverter
																	.printDecimal(proValue));

													fieldElement
															.setValue(field);
												} else {
													fieldElement.setNil(true);
												}

												row.getFieldElementList().add(
														fieldElement);
											}

										}

									} else if (fieldMetaData instanceof IntFieldMetaData) {

										if (fieldExtent < 1) {
											final JAXBElement fieldElement = new JAXBElement<IntField>(
													new QName(NS, "Field"),
													IntField.class, null);

											final IntField field = OBJECT_FACTORY
													.createIntField();

											field.setId(fieldId);

											field.setName(fieldName);

											final String proValue = proParam
													.getString(field.getId());

											if (proValue != null) {
												field.getContent()
														.add(DatatypeConverter
																.printInt(Integer
																		.parseInt(proValue)));

												fieldElement.setValue(field);
											} else {
												fieldElement.setNil(true);
											}

											row.getFieldElementList().add(
													fieldElement);
										} else {

											for (int i = 1; i <= fieldExtent; i++) {
												final JAXBElement fieldElement = new JAXBElement<IntField>(
														new QName(NS, "Field"),
														IntField.class, null);

												final IntField field = OBJECT_FACTORY
														.createIntField();

												field.setId(fieldId);

												field.setName(fieldName);

												final String proValue = proParam
														.getString(
																field.getId(),
																i);

												if (proValue != null) {
													field.getContent()
															.add(DatatypeConverter
																	.printInt(Integer
																			.parseInt(proValue)));

													fieldElement
															.setValue(field);
												} else {
													fieldElement.setNil(true);
												}

												row.getFieldElementList().add(
														fieldElement);
											}

										}

									} else if (fieldMetaData instanceof LogFieldMetaData) {

										if (fieldExtent < 1) {
											final JAXBElement fieldElement = new JAXBElement<LogField>(
													new QName(NS, "Field"),
													LogField.class, null);

											final LogField field = OBJECT_FACTORY
													.createLogField();

											field.setId(fieldId);

											field.setName(fieldName);

											final String proValue = proParam
													.getString(field.getId());

											if (proValue != null) {
												field.getContent()
														.add(DatatypeConverter
																.printBoolean(Boolean
																		.valueOf(proValue)));

												fieldElement.setValue(field);
											} else {
												fieldElement.setNil(true);
											}

											row.getFieldElementList().add(
													fieldElement);
										} else {

											for (int i = 1; i <= fieldExtent; i++) {
												final JAXBElement fieldElement = new JAXBElement<LogField>(
														new QName(NS, "Field"),
														LogField.class, null);

												final LogField field = OBJECT_FACTORY
														.createLogField();

												field.setId(fieldId);

												field.setName(fieldName);

												final String proValue = proParam
														.getString(
																field.getId(),
																i);

												if (proValue != null) {
													field.getContent()
															.add(DatatypeConverter
																	.printBoolean(Boolean
																			.valueOf(proValue)));

													fieldElement
															.setValue(field);
												} else {
													fieldElement.setNil(true);
												}

												row.getFieldElementList().add(
														fieldElement);
											}

										}

									}

								}

								resParam.getRowList().add(row);
							}

							resParamElement.setValue(resParam);
						} else {
							resParamElement.setNil(true);
						}

						res.getParamElementList().add(resParamElement);
					}

				}

			}

			final Document doc = builder.newDocument();

			marshaller.marshal(res, doc);

			exchange.getOut().setBody(doc);
		} catch (final RunTime4GLErrorException e) {
			LOG.error(e.getProcReturnString(), e);

			throw new OpenEdgeException(e.getProcReturnString(), e);
		}

	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public void setSessionModel(final String sessionModel) {
		this.sessionModel = sessionModel;
	}

	public void setUser(final String user) {
		this.user = user;
	}

}
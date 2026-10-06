/*******************************************************************************
 * Copyright (c) 2009, 2011 Standards for Technology in Automotive Retail, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     David Carver - bug 280547 - initial API and implementation.
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package info.fingo.xactus.processor.internal.function;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import info.fingo.xactus.api.DynamicContext;
import info.fingo.xactus.api.EvaluationContext;
import info.fingo.xactus.api.ResultBuffer;
import info.fingo.xactus.api.ResultSequence;
import info.fingo.xactus.processor.DynamicError;
import info.fingo.xactus.processor.internal.SeqType;
import info.fingo.xactus.processor.internal.types.CalendarType;
import info.fingo.xactus.processor.internal.types.QName;
import info.fingo.xactus.processor.internal.types.XSDayTimeDuration;
import info.fingo.xactus.processor.internal.types.XSTime;

/**
 * Adjusts an xs:dateTime value to a specific timezone, or to no timezone at
 * all. If <code>$timezone</code> is the empty sequence, returns an
 * <code>xs:dateTime</code> without timezone. Otherwise, returns an
 * <code>xs:dateTime</code> with a timezone.
 */
public class FnAdjustTimeToTimeZone extends Function {

	private static Collection _expected_args = null;
	private static final XSDayTimeDuration minDuration = new XSDayTimeDuration(
			0, 14, 0, 0, true);
	private static final XSDayTimeDuration maxDuration = new XSDayTimeDuration(
			0, 14, 0, 0, false);

	/**
	 * Constructor for FnDateTime.
	 */
	public FnAdjustTimeToTimeZone() {
		super(new QName("adjust-time-to-timezone"), 1, 2);
	}

	/**
	 * Evaluate arguments.
	 *
	 * @param args
	 *            argument expressions.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of evaluation.
	 */
	public ResultSequence evaluate(Collection args, EvaluationContext ec) {
		return adjustTime(args, ec.getDynamicContext());
	}

	/**
	 * Evaluate the function using the arguments passed.
	 *
	 * @param args
	 *            Result from the expressions evaluation.
	 * @param sc
	 *            Result of static context operation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the fn:dateTime operation.
	 */
	public static ResultSequence adjustTime(Collection args,
			DynamicContext dc) throws DynamicError {

		Collection cargs = Function.convert_arguments(args, expectedArgs());

		// get args
		Iterator argiter = cargs.iterator();
		ResultSequence arg1 = (ResultSequence) argiter.next();
		if (arg1.empty()) {
			return ResultBuffer.EMPTY;
		}
		ResultSequence arg2 = ResultBuffer.EMPTY;
		if (argiter.hasNext()) {
			arg2 = (ResultSequence) argiter.next();
		}
		XSTime time = (XSTime) arg1.first();
		XSDayTimeDuration timezone = null;

		if (arg2.empty()) {
			if (time.timezoned()) {
				XSTime localized = new XSTime(time.calendar(), null);
				return localized;
			} else {
				return arg1;
			}
		}


		XMLGregorianCalendar xmlCalendar = null;
		Calendar normalized = null;

		if (time.tz() != null) {
			normalized = time.normalizeCalendar(time.calendar(), time.tz());
			xmlCalendar = _datatypeFactory.newXMLGregorianCalendar((GregorianCalendar) normalized);
		} else {
			xmlCalendar = _datatypeFactory.newXMLGregorianCalendarTime(time.hour(), time.minute(), (int)time.second(), 0);
		}

		timezone = (XSDayTimeDuration) arg2.first();
		if (timezone.lt(minDuration, dc) || timezone.gt(maxDuration, dc)) {
			throw DynamicError.invalidTimezone();
		}

		if (time.tz() == null) {
			return new XSTime(time.calendar(), timezone);
		}

		Duration duration = _datatypeFactory.newDuration(timezone.getStringValue());
		xmlCalendar.add(duration);

		return new XSTime(CalendarType.toGregorianCalendar(normalized, xmlCalendar), timezone);
	}

	/**
	 * Obtain a list of expected arguments.
	 *
	 * @return Result of operation.
	 */
	public synchronized static Collection expectedArgs() {
		if (_expected_args == null) {
			_expected_args = new ArrayList();
			_expected_args
					.add(new SeqType(new XSTime(), SeqType.OCC_QMARK));
			_expected_args.add(new SeqType(new XSDayTimeDuration(),
					SeqType.OCC_QMARK));
		}

		return _expected_args;
	}
}

/*******************************************************************************
 * Copyright (c) 2005, 2011 Jesper Steen Moeller, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Moeller - bug 285152 - implement fn:normalize-unicocde
 *     Jesper Steen Moller  - bug 290337 - Revisit use of ICU
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package info.fingo.xactus.processor.internal.function;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import info.fingo.xactus.api.DynamicContext;
import info.fingo.xactus.api.EvaluationContext;
import info.fingo.xactus.api.ResultSequence;
import info.fingo.xactus.processor.DynamicError;
import info.fingo.xactus.processor.internal.SeqType;
import info.fingo.xactus.processor.internal.types.QName;
import info.fingo.xactus.processor.internal.types.XSString;

/**
 * <p>
 * Function to normalize unicode.
 * </p>
 *
 * <p>
 * Usage: fn:normalize-unicode($arg as xs:string?) as xs:string
 * or fn:normalize-unicode($arg as xs:string?, $normalization-form as xs:string) as xs:string
 * </p>
 *
 * <p>
 * This function returns the normalized value of the first argument in the normalization form specified by the second argument.
 * </p>
 *
 */
public class FnNormalizeUnicode extends Function {
	private static Collection _expected_args = null;
	private static W3CNormalizer normalizer = null;

	/**
	 * Constructor for FnNormalizeUnicode
	 */
	public FnNormalizeUnicode() {
		super(new QName("normalize-unicode"), 1, 2);
	}

	/**
	 * The common interface which normalizers must adhere to
	 */
	public interface W3CNormalizer {
		String normalize(String argument, String normalizationForm) throws DynamicError;
	};

	/**
	 * W3C normalizer implemented via java.text.Normalizer
	 */
	static class JDKNormalizer implements W3CNormalizer {

		private Map modeMap = new HashMap();
		{
			// Can't handle "FULLY-NORMALIZED" yet

			modeMap.put("NFC", Normalizer.Form.NFC);
			modeMap.put("NFD", Normalizer.Form.NFD);
			modeMap.put("NFKC", Normalizer.Form.NFKC);
			modeMap.put("NFKD", Normalizer.Form.NFKD);
		}

		public String normalize(String argument, String normalizationForm)
				throws DynamicError {
			Normalizer.Form mode = (Normalizer.Form)modeMap.get(normalizationForm);
			if (mode != null) {
				return Normalizer.normalize(argument, mode);
			} else {
				throw DynamicError.unsupported_normalization_form(normalizationForm);
			}
		}
	}

	/**
	 * Evaluate the arguments.
	 *
	 * @param args
	 *            are evaluated.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return The evaluation of the space in the arguments being normalized.
	 */
	public ResultSequence evaluate(Collection args, EvaluationContext ec) throws DynamicError {
		return normalize_unicode(args, ec.getDynamicContext());
	}

	/**
	 * Normalize unicode codepoints in the argument.
	 *
	 * @param args
	 *            are used to obtain the input string and optionally the normalization type from.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return The result of normalizing the space in the arguments.
	 */
	public static ResultSequence normalize_unicode(Collection args, DynamicContext d_context)
			throws DynamicError {
		assert args.size() >= 1 && args.size() <= 2;

		Collection cargs = Function.convert_arguments(args, expected_args());

		Iterator cargsIterator = cargs.iterator();
		ResultSequence arg1 = (ResultSequence) cargsIterator.next();


		String normalizationType = "NFC";
		if (cargsIterator.hasNext()) {
			ResultSequence arg2 = (ResultSequence)cargsIterator.next();
			// Trim and convert to upper as per the spec
			if (arg2.empty()) {
				normalizationType = "";
			} else {
				normalizationType = ((XSString) arg2.first()).value().trim().toUpperCase();
			}
		}

		String argument = "";
		if (! arg1.empty()) argument = ((XSString) arg1.first()).value();

		String normalized = normalizationType.equals("") ? argument : getNormalizer().normalize(argument, normalizationType);
		return new XSString(normalized);
	}

	private static W3CNormalizer getNormalizer() {
		if (normalizer == null) {
			normalizer = new JDKNormalizer();
		}
		return normalizer;
	}

	/**
	 * Calculate the expected arguments.
	 *
	 * @return The expected arguments.
	 */
	public synchronized static Collection expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList();
			_expected_args.add(new SeqType(new XSString(), SeqType.OCC_QMARK));
			_expected_args.add(new SeqType(new XSString(), SeqType.OCC_NONE));
		}

		return _expected_args;
	}
}

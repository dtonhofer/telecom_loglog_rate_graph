package com.mplify.logging.layout;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * Specialized functions
 * 
 * 2012.06.07 - Code carved out of AlphaLayout yields this
 ******************************************************************************/

class PreformattedInt {

	private final static int limit = 100;

	/**
	 * Cannot be instantiated
	 */

	private PreformattedInt() {
		// NOP
	}

	/**
	 * Pre-store string for a few integers
	 */

	private final static String[] preformatted;

	static {
		preformatted = new String[limit];
		for (int i = 0; i < limit; i++) {
			preformatted[i] = String.format("%02d", Integer.valueOf(i));
		}
	}

	/**
	 * Return an integer formatted to two places (with at most one leading 0)
	 * 0...9 -> "00" ... "09" and after that X is just printed as "X"
	 */

	public static String formatInt(int x) {
		if (0 <= x && x < limit) {
			return preformatted[x];
		} else {
			return Integer.toString(x);
		}
	}
}

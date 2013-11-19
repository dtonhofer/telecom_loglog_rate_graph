package com.mplify.logging.storyhelpers;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * A class encapsulating a string; tells 'beautifyVectorOfStrings' to NOT check
 * for ':' inside the string and separate along it.
 * 
 * 2005.11.28 - Moved to project m3p_tomcat_common, package com.mplify.logging 
 * 2011.07.22 - Generalized to use "Object" as constructor argument instead of
 *              only "String"
 ******************************************************************************/

public class AsIsString {

	/*
	 * String is not null. If the constructor is passed null, the string
	 * becomes "(null)"
	 */

	private final String str;

	/**
	 * An arbitrary object can be passed; its toString() method is called at
	 * once for stringification. Passing null yields "(null)". Nothing is
	 * trimmed!
	 */

	public AsIsString(Object x) {
		String myStr;
		if (x == null) {
			myStr = "(null)";
		}
		else {
			myStr = Doublet.safelyStringify(x);
			if (myStr == null) {
				myStr = "(null)";
			}
		}
		this.str = myStr;
		assert this.str != null;
	}

	/**
	 * Return the stringified object. Never returns null. Nothing has been
	 * trimmed! This may be multiline!
	 */

	@Override
	public String toString() {
		return str;
	}

	/**
	 * Quickly check whether this is multiline text
	 */

	public boolean isMultiline() {
		return str.indexOf('\n') >= 0;
	}

}
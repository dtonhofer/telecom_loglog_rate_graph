package com.mplify.logging;

import java.util.Collections;
import java.util.List;

import com.mplify.checkers.Check;
import com.mplify.logging.storyhelpers.Dedent;
import com.mplify.logging.storyhelpers.Doublet;
import com.mplify.logging.storyhelpers.Indent;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * A representation of a Stack Trace that is easier to display using Velocity
 * Template Language. Inside a .vm file, we reference instances of this type
 * and access their internal variables using the getFoo() functions.
 * Must be public otherwise Velocity won't be able to access it, of course
 * 
 * 2004.10.08 - Reviewed
 * 2004.12.05 - Reviewd as there is unclear stuff here. Also added a 
 *              way to directly construct from a Throwable. The recursive
 *              calls have been made simpler/clearer. Reorganization is an
 *              amazing thing. Added toString() methods.
 * 2005.05.02 - Copied from Mobilux project to the newly created package
 *              com.mplify.store
 * 2006.12.19 - Modified to use generics
 * 2007.01.03 - All the static encoders/decoders have been moved to 
 *              "ThrowableMarshaller"
 * 2007.04.11 - ThrowableEntry uses List instead of array for "traceLines"
 * 2009.01.31 - Code somewhat rearranged, the class "ThrowableEntryChain"
 *              was created
 * 2011.10.14 - Some code review making things easier to read.
 ******************************************************************************/

public class ThrowableEntry {

	private final String message; // message of exception, may be null
	private final String className; // class name of exception, never null (but possibly an invalid class name)
	
	/*
	 * Stack positions, stringified, never null, unmodifiable (maybe empty?) 
	 * Top Of Stack comes first; should be an ArrayList for efficient access
	 */
	
	private final List<String> stackTrace;

	/**
	 * Constructor. The "traceLines" ArrayList is made immutable and used. Caller should release it!
	 * The List is sorted  "Top Of Stack comes first"
	 */

	public ThrowableEntry(String className, String message, List<String> stackTrace) {
	    Check.notNull(className,"class name");
	    Check.notNull(stackTrace,"stack trace");
		this.className = className;
		this.stackTrace = Collections.unmodifiableList(stackTrace); // use as is
		this.message = message; // may be null
	}

	/**
	 * Get the possibly null message
	 */

	public String getMessage() {
		return message;
	}

	/**
	 * Get the non-null class name of the Throwable
	 */

	public String getClassName() {
	    assert className!=null;
		return className;
	}

	/**
	 * Get the non-null and unmodifiable list of stack positions (may be empty)
	 */

	public List<String> getStackTrace() {
	    assert stackTrace!=null;
		return stackTrace;
	}

	/**
	 * Make a 'story' out of this ThrowableEntry
	 * If "reverseLines", then the lines are ordered from shallowest
	 * stack entry to deepest stack entry, which looks "more natural"
	 */

//	@SuppressWarnings("unchecked")
	public Story toStory(boolean topOfStackWrittenLast) {
	    Story res = new Story();
		// res.add(new Doublet("Class",className));
		// res.add(new Doublet("Message",message));
	    res.add(new Doublet("Class" , className));
        res.add(new Doublet("Message" , message));
		res.add(Indent.CI);		
		if (topOfStackWrittenLast) {
            for (int i=stackTrace.size()-1;i>=0;i--) {
                res.add(stackTrace.get(i));
            }           
		}
		else {		    
		    for (String x : stackTrace) {
		        res.add(x);
		    }
		}
		res.add(Dedent.CI);
		return res;
	}
	
	
}
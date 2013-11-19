package com.mplify.checkers;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released by M-PLIFY S.A. 
 *          under the MIT License: http://opensource.org/licenses/MIT  
 *******************************************************************************
 *******************************************************************************
 * Runtime Exception (i.e. no need to declare it) thrown by the "checks"
 ******************************************************************************/

@SuppressWarnings("serial")
public class CheckFailedException extends RuntimeException {

    public CheckFailedException() {
        super();
    }

    public CheckFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CheckFailedException(String message) {
        super(message);
    }

    public CheckFailedException(Throwable cause) {
        super(cause);
    }
    
}
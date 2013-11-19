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
 * Runtime Exception (so no need to declare it) thrown by the various "checks"
 ******************************************************************************/

@SuppressWarnings("serial")
public class UnexpectedDataException extends RuntimeException {

    public UnexpectedDataException() {
        super();
    }

    public UnexpectedDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedDataException(String message) {
        super(message);
    }

    public UnexpectedDataException(Throwable cause) {
        super(cause);
    }
    
}
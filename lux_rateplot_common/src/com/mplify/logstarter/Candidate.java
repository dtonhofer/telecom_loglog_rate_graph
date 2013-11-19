package com.mplify.logstarter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;

import com.mplify.checkers.Check;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2009, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * A candidate for "LOG4J configuration instructions". Designates a resource or
 * a file.
 * 
 * This is not currently an URI, but one might consider going that way, as an
 * URI can express both a file or a resource location (the latter using the
 * unapproved 'classpath:' scheme for example, but it might make for
 * messy/verbose code. See also http://en.wikipedia.org/wiki/File_URI_scheme)
 * 
 * 
 * 2009.11.05 - Moved out of "Log4JStarter" to toplevel
 ******************************************************************************/

class Candidate {

    private final static String CLASS = Candidate.class.getName();

    private final File fileName; // not null if we want a file
    private final String resourceName; // not null if we want a resource

    /**
     * Constructor if a resource has been given
     */

    public Candidate(String resourceName) {
        Check.notNull(resourceName, "resource name");
        this.resourceName = resourceName;
        this.fileName = null;
    }

    /**
     * Constructor if a file has been given
     */

    public Candidate(File fileName) {
        Check.notNull(fileName, "file name");
        this.resourceName = null;
        this.fileName = fileName;
    }

    /**
     * Check whether this designates a resource (as opposed to a file; it's
     * either one or the other)
     */

    public boolean isResource() {
        return (resourceName != null);
    }

    /**
     * Stringification
     */

    @Override
    public String toString() {
        if (isResource()) {
            return "resource '" + resourceName + "'";
        } else {
            return "file '" + fileName + "'";
        }
    }

    /**
     * Getter. May return null if this designates a resource
     */

    public File getFileName() {
        return fileName;
    }

    /**
     * Getter. May return null if this designates a file
     */

    public String getResourceName() {
        return resourceName;
    }

    /**
     * Does this look like XML?
     */

    public boolean isXML() {
        if (fileName != null) {
            return resourceName.endsWith(".xml");
        } else if (resourceName != null) {
            return resourceName.endsWith(".xml");
        } else {
            return false; // cannot happen
        }
    }

    /**
     * Does this look like Groovy?
     */

    public boolean isGroovy() {
        if (fileName != null) {
            return resourceName.endsWith(".groovy");
        } else if (resourceName != null) {
            return resourceName.endsWith(".groovy");
        } else {
            return false; // cannot happen
        }
    }

    /**
     * URL-ization. May return the URL to an existing file (at least when we
     * checked) or to an existing resource or null.
     */

    public URL asURL(StatusManager sm) {
        Check.notNull(sm);
        if (fileName != null) {
            // In the case of files, it's up to us to check for existence
            if (!fileName.canRead() || !fileName.isFile()) {
                sm.add(new WarnStatus("File '" + fileName + "' cannot be read or is not a file", CLASS));
                return null;
            } else {
                try {
                    URL res = fileName.toURI().toURL();
                    // File may not exist, but the URL won't be null for that
                    assert res != null;
                    return res;
                } catch (MalformedURLException exe) {
                    if (sm != null) {
                        sm.add(new ErrorStatus("Could not obtain URL from '" + fileName + "'", CLASS, exe));
                    }
                    return null;
                }
            }
        } else if (resourceName != null) {
            ClassLoader cl = getMyClassLoader();
            URL res = cl.getResource(resourceName);
            // if the resource cannot be found, the resulting URL will be null
            return res;
        } else {
            return null; // cannot happen
        }
    }

    /*
     * Helper
     */

    private ClassLoader getMyClassLoader() {
        ClassLoader cl = this.getClass().getClassLoader();
        if (cl == null) {
            // "null" has the special meaning "bootstrap class loader"
            return ClassLoader.getSystemClassLoader();
        } else {
            return cl;
        }
    }

}
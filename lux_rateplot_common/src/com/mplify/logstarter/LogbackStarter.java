package com.mplify.logstarter;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferUtil;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.classic.util.EnvUtil;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.StatusPrinter;

import com.mplify.checkers.Check;
import com.mplify.sysprop.SystemProperties;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2011, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * A loader/initializer for logging
 * 
 * 2009.08.27 - Originally created as "Log4JStarter".
 * 2009.09.11 - Moved some stuff out to a common "SystemProperties" class.
 * 2010.09.01 - Some light review
 * 2011.10.26 - Rewritten for Logback
 * 2011.10.31 - Added call to SLF4JBridgeHandler.install()
 * 2012.01.14 - Commented out debugging aids (aka. "System.out")
 ******************************************************************************/

public class LogbackStarter {

    private final static String CLASS = LogbackStarter.class.getName();

    /**
     * Lock initialization and also show its status
     */

    private static final AtomicBoolean initializationDone = new AtomicBoolean(false);

    /**
     * Testcase properties are in this hardcoded resource.
     */

    private final static String LOGBACK_RESOURCE_FOR_TESTCASE = "m3p/immediate/properties/local/logback.xml";

    /**
     * Fully qualify the resource name 'nonQualifiedResourceName' by the package "myPackage".
     * If 'myPackage' is null, the 'default package' is assumed. If 'nonQualifiedResourceName' is empty (it can't be null),
     * the fully qualified package name suffixed with '/' is returned. In case the pair (null,"") is passed, "" is returned.
     */

    public static String makeFullyQualifiedResourceName(Package myPackage, String nonQualifiedResourceName) {
        Check.notNullAndNotOnlyWhitespace(nonQualifiedResourceName, "non-qualified resource name");
        if (myPackage != null) {
            String packagePath = myPackage.getName().replace('.', '/');
            String resourceName = packagePath + "/" + nonQualifiedResourceName.trim();
            return resourceName;
        } else {
            return nonQualifiedResourceName;
        }
    }

    /**
     * Fully qualify the resource name 'nonQualifiedResourceName' by the package of the class "clazz".     
     * If "clazz" is null, default package is assumed.
     */

    public static String makeFullyQualifiedResourceName(Class<?> clazz, String nonQualifiedResourceName) {
        Package myPackage = (clazz != null) ? clazz.getPackage() : null;
        return makeFullyQualifiedResourceName(myPackage, nonQualifiedResourceName);
    }

    /**
     * Building the list of "candidates" to check for logback configuration.
     * This is done by examining System properties, and building an ordered list of candidates from there.
     */

    @SuppressWarnings("unused")
    private static List<Candidate> buildListOfCandidates(Package pack, Class<?> clazz) {
        //
        // Prepare something to log "status messages"
        //
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusManager sm = lc.getStatusManager();
        if (sm != null) {
            sm.add(new InfoStatus("Setting up list of candidate configuration sources in thread '" + Thread.currentThread().getName() + "'", CLASS));
        }
        //
        // Extract system properties, which may or may not have been set
        //
        boolean throwIfNotFound, normalize;
        String instance = SystemProperties.getInstance(throwIfNotFound = false, normalize = true);
        String stage = SystemProperties.getStage(throwIfNotFound = false, normalize = true);
        String confighome = SystemProperties.getConfighome(throwIfNotFound = false);
        //
        // fill a list of candidates to check, in order of priority
        //
        List<Candidate> res = new LinkedList<Candidate>();
        if (SystemProperties.STAGE_TESTCASE.equals(stage)) {
            //
            // If this is "testcase" stage, then just look for a known resource
            //
            res.add(new Candidate(LOGBACK_RESOURCE_FOR_TESTCASE));
        } else {
            //
            // If package or class given, add resource-based candidates
            //
            if (clazz != null) {
                res.add(new Candidate(makeFullyQualifiedResourceName(clazz, "logback.groovy")));
                res.add(new Candidate(makeFullyQualifiedResourceName(clazz, "logback.xml")));
            }
            if (pack != null) {
                res.add(new Candidate(makeFullyQualifiedResourceName(pack, "logback.groovy")));
                res.add(new Candidate(makeFullyQualifiedResourceName(pack, "logback.xml")));
            }
            //
            // File-based candidates come first
            //
            if (confighome != null) {
                File configDir = new File(confighome);
                if (!configDir.exists()) {
                    if (sm != null) {
                        sm.add(new ErrorStatus("The '" + SystemProperties.PROP_CONFIGHOME + "' system property is '" + confighome + "' -- such a directory does not exist", CLASS));
                    }
                } else if (!configDir.isDirectory()) {
                    if (sm != null) {
                        sm.add(new ErrorStatus("The '" + SystemProperties.PROP_CONFIGHOME + "' system property is '" + confighome + "' -- this is not a directory", CLASS));
                    }
                } else {
                    if (instance != null && stage != null) {
                        res.add(new Candidate(new File(configDir, instance + "." + stage + ".logback.groovy")));
                        res.add(new Candidate(new File(configDir, instance + "." + stage + ".logback.xml")));
                    }
                    if (instance != null) {
                        res.add(new Candidate(new File(configDir, instance + ".logback.groovy")));
                        res.add(new Candidate(new File(configDir, instance + ".logback.xml")));
                    }
                    if (stage != null) {
                        res.add(new Candidate(new File(configDir, stage + ".logback.groovy")));
                        res.add(new Candidate(new File(configDir, stage + ".logback.xml")));
                    }
                    res.add(new Candidate(new File(configDir, "logback.groovy")));
                    res.add(new Candidate(new File(configDir, "logback.xml")));
                }
            }
            //
            // Then, resource-based candidates
            //
            {
                if (instance != null && stage != null) {
                    res.add(new Candidate(instance + "." + stage + ".logback.groovy"));
                    res.add(new Candidate(instance + "." + stage + ".logback.xml"));
                }
                if (instance != null) {
                    res.add(new Candidate(instance + ".logback.groovy"));
                    res.add(new Candidate(instance + ".logback.xml"));
                }
                if (stage != null) {
                    res.add(new Candidate(stage + ".logback.groovy"));
                    res.add(new Candidate(stage + ".logback.xml"));
                }
                res.add(new Candidate("logback.groovy"));
                res.add(new Candidate("logback.xml"));
            }
            //
            // Finally, there is logback's default configuration, which we don't list here
            //
        }
        if (sm != null) {
            sm.add(new InfoStatus("Config candidates are:", CLASS));
            int i = 0;
            for (Candidate c : res) {
                sm.add(new InfoStatus(i + ") " + c, CLASS));
                i++;
            }
        }
        return res;
    }

    /**
     * Constructor. Call this to kick off initialization. An instance of this class is useless though so throw it away immediately.
     * (the compiler may issue a warning about a "useless code" in that case).
     */

    public LogbackStarter() {
        this(null, null, true);
    }

    /**
     * Constructor. Call this to kick off initialization. An instance of this class is useless though so throw it away immediately.
     * (the compiler may issue a warning about a "useless code" in that case).
     * A non-null package may be passed to make the configurator look for the configuration resource in that package first.
     */

    public LogbackStarter(Package pack) {
        this(pack, null, true);
    }

    /**
     * Constructor. Call this to kick off initialization. An instance of this class is useless though so throw it away immediately.
     * (the compiler may issue a warning about a "useless code" in that case).
     * A non-null class may be passed to make the configurator look for the configuration resource in that class' package first.
     */

    public LogbackStarter(Class<?> clazz) {
        this(null, clazz, true);
    }

    /**
     * Private constructor actually doing the work
     */

    private LogbackStarter(Package pack, Class<?> clazz, boolean printStatusAtEnd) {
        synchronized (initializationDone) {
            if (!initializationDone.get()) {
                //
                // Redirects Java Util Logging (JUL) into SLF4J, with some overhead
                // This operation does not print anything
                //
                SLF4JBridgeHandler.install();                 
                //
                // Build a list of candidate sources for configuration
                // This operation does not print anything
                //
                List<Candidate> candidates = buildListOfCandidates(pack, clazz);                
                //
                // Configure according to the best candidate source
                //
                configureLoggerContextWithFirstGoodCandidate(candidates, printStatusAtEnd);
                //
                // "Lock out changes" 
                //
                initializationDone.set(true);                
            }
        }
    }
    
    /**
     * Helper
     */

    private Candidate configureLoggerContextWithFirstGoodCandidate_inner(List<Candidate> candidates,boolean groovyAvailable,StatusManager sm, LoggerContext lc) {
        assert candidates != null;
        assert sm != null;
        assert lc != null;
        Iterator<Candidate> iter = candidates.iterator();
        boolean configDone = false;
        Candidate c = null;
        
    while (!configDone && iter.hasNext()) {
        c = iter.next();
        
        URL url = c.asURL(sm);
        
        sm.add(new InfoStatus("In config loop: Checking configuration candidate " + c, CLASS));
        sm.add(new InfoStatus("In config loop:   with URL " + url, CLASS));
        
        if (url != null) {
            try {
                lc.reset();
                
                
                
                // System.out.println("Configure!");
                
                //
                // This print the contents of "sm", twice, after which adding to SM will cause
                // a message to be printed twice...
                //
                
                configDone = configureByResource(url, lc, sm, groovyAvailable);
                
                // System.out.println("Configured!");
                
                
            } catch (ConfigException exe) {
                sm.add(new WarnStatus("In config loop: While configuring from " + c + " -- skipping", CLASS, exe));
            }
        } else {
            sm.add(new InfoStatus("In config loop: URL from " + c + " is (null) -- skipping", CLASS));
        }
    }
    return c;
    }

    /**
     * Helper
     */

    private void configureLoggerContextWithFirstGoodCandidate(List<Candidate> candidates, boolean printStatusAtEnd) {
        assert candidates != null;
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        assert lc != null;
        StatusManager sm = lc.getStatusManager();
        assert sm != null;
        //
        // Loop until configuration succeeded or no more candidates
        //
        
        Candidate c = configureLoggerContextWithFirstGoodCandidate_inner(candidates, EnvUtil.isGroovyAvailable(), sm, lc);

        
        // System.out.println("inner loop done");
        
        //
        // From here on, sm.add prints the text to Stdout twice...
        //

        if (c!=null) {
            sm.add(new InfoStatus("Configuration using " + c + " succeeded", CLASS));            
        } else {
            sm.add(new InfoStatus("No candidate could be used for logback configuration -- trying autoconfiguration", CLASS));
            try {
                (new ContextInitializer(lc)).autoConfig();
            } catch (Exception exe) {
                sm.add(new WarnStatus("While autoconfiguring -- giving up", CLASS, exe));
            }
        }               
        if (printStatusAtEnd) {            
            StatusPrinter.printInCaseOfErrorsOrWarnings(lc);            
        }        
    }

    /**
     * Helper. More or less a copy of ch.qos.logback.classic.util.ContextInitializer.configureByResource() but one
     * which does better error handling. Returns true if configuration was done.
     */

    public boolean configureByResource(URL url, LoggerContext lc, StatusManager sm, boolean groovyAvailable) throws ConfigException {
        Check.notNull(url, "URL");
        Check.notNull(lc, "logger context");
        Check.notNull(sm, "status manager");
        if (url.toString().endsWith("groovy")) {
            try {
                if (groovyAvailable) {
                    // avoid directly referring to GafferConfigurator so as to avoid
                    // loading groovy.lang.GroovyObject . See also http://jira.qos.ch/browse/LBCLASSIC-214
                    GafferUtil.runGafferConfiguratorOn(lc, this, url);
                    // Being here may mean that configuration succeeded
                    // It may also mean that the Groovy had errors and nothing happened....
                    // Let's be optimistic and return "true".
                    return true;
                } else {
                    sm.add(new WarnStatus("Groovy classes are not available on the class path. Cannot configure from " + url, CLASS));
                    return false;
                }
            } catch (Exception exe) {
                throw new ConfigException("While configuring from " + url, exe);
            }
        } else if (url.toString().endsWith("xml")) {
            try {
                
                // System.out.println("Joran config start");
                
                JoranConfigurator configurator = new JoranConfigurator();
                
                // System.out.println("Joran config set context");
                
                configurator.setContext(lc);
                
                // System.out.println("Joran config do configure");
                
                configurator.doConfigure(url);
                
                // System.out.println("Joran config done");
                
                // Being here may mean that configuration succeeded
                // It may also mean that the XML had errors and nothing happened.
                // Let's be optimistic and return "true".
                return true;
            } catch (Exception exe) {
                throw new ConfigException("While configuring from " + url, exe);
            }
        } else {
            return false;
        }
    }
}

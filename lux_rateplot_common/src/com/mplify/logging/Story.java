package com.mplify.logging;

import java.io.IOException;

import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.mplify.checkers.Check;
import com.mplify.logging.LoglevelAid.Loglevel;
import com.mplify.logging.storyhelpers.AsIsString;
import com.mplify.logging.storyhelpers.ConcatMe;
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
 * A structure that holds multiline text in memory for later logging, either
 * by transformation into string, writing to a log4j logger or an appendable
 * or a print stream. 
 * 
 * 2004.10.19 - Created
 * 2005.05.22 - Column indentation now uses LogFacilities.indentString(n)
 *              instead of a loop.
 * 2005.11.28 - Moved to project m3p_tomcat_common, package com.mplify.logging
 * 2007.04.10 - Pretty extensive review for better handling                
 * 2009.01.31 - Added functions using StringBuilder in parallel to those
 *              using StringBuffer.
 *              ConcatMe and cutUpStory() made public so they can be used from
 *              AlphaLayout and SenderThread
 * 2010.10.22 - Moved writeSimplifiedDump() to here
 * 2011.06.09 - Added cutLines()
 * 2011.07.22 - Added cookStoryIntoStringWithBeautification() which also takes
 *              additional indents 
 * 2011.08.03 - Rewrite! Instead of a bunch of static methods on class
 *              "StoryHandling", that deal in Vectors, now using the "Story"
 *              class instead and associating the methods with that.
 * 2011.09.05 - toString() made useless call to cutUp()
 * 2011.10.17 - write(Appendable a) now writes final EOL; bug in cutUpMultiline()
 *              fixed.
 * 2011.10.19 - Adapted for LOG4J --> SLF4J migration   
 * 2011.11.07 - Added a "add()" for a List of Strings           
 * 2012.04.11 - "add()" methods made chainable and added "toMonolineString()"
 * 2012.12.10 - Changed "toMonolineString()" for nicer output
 * 2012.12.28 - "cannotHappen()" throws an Error instead of an 
 *              IllegalStateException. Badass! 
 ******************************************************************************/

public class Story {

    private static final String LINE_SEP; 

    static {
        String ls = System.getProperty("line.separator");
        LINE_SEP = (ls == null) ? "\n" : ls;
    }
    
    /**
     * When printing, there is a separator between the attribute and the values. This is the String that's being
     * printed.
     */

    private final static String SEPARATOR = ": ";

    /**
     * The Story keeps a simple linked list of Indent, Dedent, AsIsString and Doublet and Story. In order to avoid
     * allocating many "AsIsString", we will interprete a String also as an "AsIsString". null is not allowed.
     */

    private List<Object> list = new LinkedList();

    /**
     * Constructor for an initially empty story.
     */

    public Story() {
        // NOP
    }

    /**
     * Constructor for a story that is initialized to a set of String. "array" may be null or empty.
     */

    public Story(String[] array) {
        this(array, null);
    }

    /**
     * Constructor for a story that is initialized to a set of String. "array" may be null or empty.
     * Each line in the passed array will be indented by the number of spaces given by "indent" if
     * "indent" is not null.
     */

    public Story(String[] array, Indent indent) {
        if (indent != null) {
            list.add(indent);
        }
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    list.add(array[i]);
                }
            }
        }
        if (indent != null) {
            list.add(new Dedent());
        }
    }

    /**
     * Is there any text in the story?
     */

    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * How many elements in the story? This is only useful if one knows that each element
     * corresponds to exactly one line. 
     */

    public int size() {
        return list.size();
    }

    /**
     * Helper
     */

    private int constrain(int x) {
        int xx = Math.max(x, 0);
        xx = Math.min(xx, list.size());
        return xx;
    }

    /**
     * Adding text at a precise position (out-of-range is handled leniently). Passing (null) reduces to a NOP.
     * In order to permit chaining, "this" is returned after addition. 
     */

    public Story add(int x, Doublet doublet) {
        if (doublet != null) {
            list.add(constrain(x), doublet);
        }
        return this;        
    }

    /**
     * Adding text at a precise position (out-of-range is handled leniently). Passing (null) reduces to a NOP.
     * In order to permit chaining, "this" is returned after addition. 
     */

    public Story add(int x, Indent indent) {
        if (indent != null) {
            list.add(constrain(x), indent);
        }
        return this;        
    }

    /**
     * Adding text at a precise position (out-of-range is handled leniently). Passing (null) reduces to a NOP.
     * In order to permit chaining, "this" is returned after addition. 
     */

    public Story add(int x, Dedent dedent) {
        if (dedent != null) {
            list.add(constrain(x), dedent);
        }
        return this;        
    }

    /**
     * Adding text at a precise position (out-of-range is handled leniently). Passing (null) reduces to a NOP.
     * In order to permit chaining, "this" is returned after addition. 
     */

    public Story add(int x, AsIsString string) {
        if (string != null) {
            list.add(constrain(x), string);
        }
        return this;        
    }

    /**
     * Adding text at a precise position (out-of-range is handled leniently). Passing (null) reduces to a NOP.
     * In order to permit chaining, "this" is returned after addition. 
     */

    public Story add(int x, String rawString) {
        if (rawString != null) {
            list.add(constrain(x), rawString);
        }
        return this;        
    }

    /**
     * Adding text at a precise position (out-of-range is handled leniently). Passing (null) reduces to a NOP.
     * In order to permit chaining, "this" is returned after addition. 
     */

    public Story add(int x, Story story) {
        if (story != null) {
            list.add(constrain(x), story);
        }
        return this;        
    }

    /**
     * Adding text to the end of the story. Passing (null) reduces to a NOP.
     * In order to permit chaining, "this" is returned after addition. 
     */

    public Story add(Doublet doublet) {
        if (doublet != null) {
            list.add(doublet);
        }
        return this;        
    }

    /**
     * Adding text to the end of the story. Passing (null) reduces to a NOP.
     * In order to permit chaining, "this" is returned after addition. 
     */

    public Story add(Indent indent) {
        if (indent != null) {
            list.add(indent);
        }
        return this;        
    }

    /**
     * Adding text to the end of the story. Passing (null) reduces to a NOP.
     * In order to permit chaining, "this" is returned after addition. 
     */

    public Story add(Dedent dedent) {
        if (dedent != null) {
            list.add(dedent);
        }
        return this;        
    }

    /**
     * Adding text to the end of the story. Passing (null) reduces to a NOP.
     * In order to permit chaining, "this" is returned after addition. 
     */

    public Story add(AsIsString string) {
        if (string != null) {
            list.add(string);
        }
        return this;        
    }

    /**
     * Adding text to the end of the story. Passing (null) reduces to a NOP.
     * In order to permit chaining, "this" is returned after addition.  
     */

    public Story add(String string) {
        if (string != null) {
            list.add(string);
        }
        return this;
    }

    /**
     * Adding text to the end of the story. Passing (null) reduces to a NOP.
     * The story is added but note that it could be possible to add stuff to it even after addition!!
     * In order to permit chaining, "this" is returned after addition. 
     */

    public Story add(Story story) {
        if (story != null && !story.isEmpty()) {
            list.add(story);
        }
        return this;
    }

    /**
     * Fusing another Story into the current list, instead of adding the Story. Passing (null) reduces to a NOP.
     * This means formatting will be different, with the ":" aligned across all lines.
     * In order to permit chaining, "this" is returned after addition. 
     */

    public Story fuse(Story story) {
        if (story != null && !story.isEmpty()) {
            list.addAll(story.list);
        }
        return this;
    }

    /**
     * Sugar: Add an "intro string" and the Strings in "list" separated by "separator" to the story.
     * Like this: "intro a,b,c,d,e,f" for intro = "intro", strlist = (a,b,c,d,e,f) and separator = ","
     * List can be null or empty and may contain nulls (which are rendered as "(null)"). If "intro" is null, no intro is added.
     * Also returns the resulting composite that has been added.
     */
    
    public String add(String intro, List<String> strlist, String separator) {
        StringBuilder buf = new StringBuilder();
        if (intro != null) {
            buf.append(intro);
        }
        if (strlist!=null) {            
            boolean addSep = false;
            for (String str : strlist) {
                if (addSep) {
                    buf.append(separator==null ? "" : separator);
                }
                buf.append(str==null ? "(null)" : str);
                addSep = true;
            }
        }
        String x = buf.toString(); 
        list.add(x);
        return x;
    }
    
    /**
     * Sugar. If "list" is empty or null, this is a NOP (i.e. even the header isn't written out).
     * (null)s in the "strlist" are not added either.
     */
    
    public void addHeaderAndIndentedList(String header, List<String> strlist) {
        if (list!=null && !list.isEmpty()) {            
            add(header); // If header is null, this is a NOP
            add(Indent.CI);
            for (String s : strlist) {
                add(s);
            }                
            add(Dedent.CI);
        }                
    }
    
    /**
     * Check the equality of two 'stories' and a return a story detailing the differences. The
     * returned story is "empty" if no differences were detected. This is used in test cases in 
     * an evident way.  
     */

    public Story equalsStories(Story other) {
        Check.notNull(other,"story");
        return equalsStories(other, new HashSet<Story>());
    }

    /**
     * The recursive checker for story comparison
     */
    
    private Story equalsStories(Story other, Set<Story> recursiveCheckSet) {
//        Logger logger = LOGGER_equalsStories;
        assert other != null;
        assert !recursiveCheckSet.contains(this);
        recursiveCheckSet.add(this);
        Story res = new Story();
        Iterator<Object> leftIter = this.list.iterator();
        Iterator<Object> rightIter = other.list.iterator();
        while (leftIter.hasNext() && rightIter.hasNext()) {
            Object leftEntry = leftIter.next();
            Object rightEntry = rightIter.next();
            assert leftEntry != null;
            assert rightEntry != null;
            if (leftEntry instanceof Indent && rightEntry instanceof Indent) {
                // Ok for this
            } else if (leftEntry instanceof Dedent && rightEntry instanceof Dedent) {
                // Ok for this
            } else if ((leftEntry instanceof AsIsString || leftEntry instanceof String) && (rightEntry instanceof AsIsString || rightEntry instanceof String)) {
                String leftString = leftEntry.toString();
                String rightString = rightEntry.toString();
                if (!leftString.equals(rightString)) {
                    res.add("Difference found: '" + LogFacilities.mangleString(leftString) + "' vs. '" + LogFacilities.mangleString(rightString) + "'");
                }
            } else if (leftEntry instanceof Doublet && rightEntry instanceof Doublet) {
                Doublet leftDoublet = (Doublet) leftEntry;
                Doublet rightDoublet = (Doublet) rightEntry;
                String leftString = leftDoublet.toString();
                String rightString = rightDoublet.toString();
                if (!leftString.equals(rightString)) {
                    res.add("Difference found: '" + LogFacilities.mangleString(leftString) + "' vs. '" + LogFacilities.mangleString(rightString) + "'");
                }
            } else if (leftEntry instanceof Story && rightEntry instanceof Story) {
                Story leftStory = (Story) leftEntry;
                Story rightStory = (Story) rightEntry;
                // RECURSIVE CALL; note the "recursive Check Set" used to make sure we don't recursive forever in a bad structure
                res.add(leftStory.equalsStories(rightStory, recursiveCheckSet));
            } else {
                // Type difference
                res.add("Difference found: left entry is of type " + leftEntry.getClass().getName() + ", right entry is of type " + rightEntry.getClass().getName());
            }
        }
        //
        // Check out the remainders
        //
        while (leftIter.hasNext()) {
            String leftEntry = leftIter.next().toString();
            res.add("Difference found: left side has additional entry '" + LogFacilities.mangleString(leftEntry) + "'");
        }
        while (rightIter.hasNext()) {
            String rightEntry = rightIter.next().toString();
            res.add("Difference found: right side has additional entry '" + LogFacilities.mangleString(rightEntry) + "'");
        }
        return res;
    }

    /**
     * Write a story to the given logger at the given level. Level testing is performed internall.y
     */

    public void write(Logger logger, Loglevel level) {
        write(logger, level, null);
    }

    /**
     * Helper to catch exceptions that one does not expect to occur.
     * Possibilities: Do nothing, log to stdout, rethrow as RuntimeException, rethrow as Error (in particular, AssertionError)
     * Here we throw as an Error, which makes sense because calling this really
     * should simply not happen.
     */

    private static void cannotHappen(Throwable exe) {
        throw new Error("Can't happen: " + LogFacilitiesForThrowables.throwableToOneLinerDoublet(exe));
    }

    /**
     * Write a story to the given logger at the given level. Level testing is performed internally.
     * Throwable may be null, otherwise it is passed to the logger using the separate exception-logging
     * call. This is basically syntactic sugar for easy story+throwable logging.
     */

    public void write(Logger logger, Loglevel level, Throwable throwable) {
        Check.notNull(logger,"logger");
        Check.notNull(level,"level");
        if (LoglevelAid.isEnabledFor(logger,level)) {
            String txt = null;
            try {
                StringBuilder buf = new StringBuilder();
                write(buf); // use the writeStory() working on "appendable";
                txt = buf.toString();
            } catch (IOException exe) {
                cannotHappen(exe);
            }
            if (throwable != null) {
                // Use the special Log4j function taking a throwable
                LoglevelAid.log(logger, level, txt, throwable);
            } else {
                // Use the special Log4j function NOT taking a throwable
                LoglevelAid.log(logger, level, txt);
            }
        }
    }

    /**
     * Write the story to a Writer.
     */

    public void write(Writer w) throws IOException {
        Check.notNull(w,"writer");
        String txt = null;
        try {
            StringBuilder buf = new StringBuilder();
            write(buf); // use the writeStory() working on "appendable";
            txt = buf.toString();
        } catch (IOException exe) {
            cannotHappen(exe);
        }
        w.append(txt);
    }

    /**
     * Write the story to a PrintStream.
     */

    public void write(PrintStream p) {
        Check.notNull(p,"print stream");
        String txt = null;
        try {
            StringBuilder buf = new StringBuilder();
            write(buf); // use the writeStory() working on "appendable";
            txt = buf.toString();
        } catch (IOException exe) {
            cannotHappen(exe);
        }
        p.append(txt);
    }

    /**
     * Write the story to an Appendable.
     * If the Story is empty, nothing will be written.
     */

    public void write(Appendable a) throws IOException {
        Check.notNull(a,"appendable");
        // >>>>>
        List<ConcatMe> cmList = cutUp();
        // <<<<<
        boolean addEol = false;
        for (ConcatMe cm : cmList) {
            if (addEol) {
                a.append(LINE_SEP);
            }
            cm.concat(a); // cm knows how to write itself to an Appendable
            addEol = true;
        }
        // TODO: Depending on how this is logged, the final EOL may be too much
        if (addEol) {
            a.append(LINE_SEP);
        }
    }

    /**
     * Write the story to an Appendable, expecting no Exception 
     */

    public void writeShielded(Appendable a) {
        try {
            write(a);
        } catch (Exception exe) {
            cannotHappen(exe); // throws an Error, just the Exception declaration
        }
    }

    /**
     * Transform to a *multiline* String.
     */

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        try {
            // uses the writeStory() working on "appendable"
            write(buf); 
        } catch (Exception exe) {
            // throws an Error with the exception data
            cannotHappen(exe); 
        }
        return buf.toString();
    }

    /**
     * Naively transform to a *monoline* String, as far as possible.
     */

    public String toMonolineString() {
        StringBuilder buf = new StringBuilder();
        boolean addSep = false;
        for (Object obj : list) {
            if (addSep) {
                buf.append(", ");
            }
            if (obj != null) {
                if (obj instanceof Doublet) {
                    Doublet d = (Doublet) obj;
                    buf.append(d.getLeft().trim()); // is this monoline? In principle, yes
                    buf.append(" = '");
                    buf.append(LogFacilities.mangleString(d.getRight())); // is this monoline? Not necessarily, so apply mangleString()
                    buf.append("'");
                } else if (obj instanceof Story) {
                    Story substory = (Story)obj;
                    buf.append("[");
                    buf.append(substory.toMonolineString());
                    buf.append("]");
                } else if (obj instanceof Indent) {
                	buf.append("[");
                } else if (obj instanceof Dedent) {
                	buf.append("]");
                } else {
                    buf.append(LogFacilities.mangleString(obj.toString())); // possibly not monoline, so apply mangleString()
                }
            }
            addSep = true;
        }
        return buf.toString();
    }

    /**
     * Helper that cuts up a story in a List of individual one-liner-strings, with adjustment of the
     * separator between the Doublet LEFT and RIGHT sides. Clients may have to call this, so it's public.
     */

    public List<ConcatMe> cutUp() {
        Set<Story> recursiveCheckSet = new HashSet();
        int absoluteIndent = 0;
        return cutUp(absoluteIndent, recursiveCheckSet);
    }

    /**
     * Hidden cutter (checking for infinite recursive call messes) (and knowing about an "initial indent") 
     */

    private List<ConcatMe> cutUp(int absoluteIndent, Set<Story> recursiveCheckSet) {
        assert recursiveCheckSet != null;
        assert !recursiveCheckSet.contains(this);
        recursiveCheckSet.add(this);
        String absoluteIndentStr = LogFacilities.getSpaceString(absoluteIndent);
        List<ConcatMe> res = new LinkedList();
        //
        // "indentsStack" is a stack of the indentations, with the topmost element being the one currently applied
        //
        Stack<String> indentsStack = new Stack();
        indentsStack.push(absoluteIndentStr);
        //
        // Find out at what position the ":" separating the doublet's left and right part is
        //
        int doubletLeft = 0;
        for (Object obj : list) {
            if (obj instanceof Doublet) {
                doubletLeft = Math.max(doubletLeft, ((Doublet) obj).getLeft().length());
            }
        }
        String secondaryLineFiller = LogFacilities.getSpaceString(doubletLeft + SEPARATOR.length());
        //
        // Create Strings
        //
        // Case of String:
        //
        // <------ absolute indent + current indent --------->[TEXT OF STRING OR AS IS STRING]
        // this string is on "indents stack" [POSSIBLY MULTIPLE LINES]
        //
        // cutUpMultiline() is called with:
        //
        // firstLineFiller == ""
        // secondaryLineFiller == ""
        //
        // Case of Doublet. The ": " SEPARATORs shall all be aligned
        //
        // <------ absolute indent + current indent --------->[LEFT TEXT xxx][yyyyyy][: ][LINE 1]
        // <------ absolute indent + current indent --------->[LEFT TEXT][yyyyyyyyyy][: ][LINE 1]
        // <------ absolute indent + current indent --------->[LEFT TEXT xxxxxxxxxxx][: ][LINE 1]
        // <------ absolute indent + current indent --------->[LEFT TEXT xx][yyyyyyy][: ][LINE 1]
        // <------ absolute indent + current indent --------->[ secondaryLineFiller ][LINE 2]
        // <------ absolute indent + current indent --------->[ secondaryLineFiller ][LINE 3]
        //
        // cutUpMultiline() is called with:
        //
        // firstLineFiller == "[LEFT TEXT xx][yyyyyyy][: ]"
        // secondaryLineFiller == "[   secondaryLineFiller   ]"
        //
        for (Object obj : list) {
            if (obj instanceof String || obj instanceof AsIsString) {
                cutUpMultiline(res, obj.toString(), "", "", indentsStack.peek());
            } else if (obj instanceof Doublet) {
                Doublet x = (Doublet) obj;
                int addSpacesOnLeft = doubletLeft - x.getLeft().length();
                assert doubletLeft >= 0;
                String firstLineFiller = x.getLeft() + LogFacilities.getSpaceString(addSpacesOnLeft) + SEPARATOR;
                String text = x.getRight(); // possibly multiline, so needs to be cut up
                cutUpMultiline(res, text, firstLineFiller, secondaryLineFiller, indentsStack.peek());
            } else if (obj instanceof Indent) {
                // indent some more
                int delta = ((Indent) obj).getCount();
                String indent = LogFacilities.getSpaceString(indentsStack.peek().length() + delta);
                indentsStack.push(indent);
            } else if (obj instanceof Dedent) {
                // make sure that superfluous dedents don't cause problems
                if (!indentsStack.isEmpty()) {
                    indentsStack.pop();
                }
                if (indentsStack.isEmpty()) {
                    indentsStack.push(absoluteIndentStr);
                }
            } else if (obj instanceof Story) {
                // RECURSIVE CALL
                Story subStory = (Story) obj;
                List<ConcatMe> subList = subStory.cutUp(indentsStack.peek().length(), recursiveCheckSet);
                res.addAll(subList);
            } else {
                throw new IllegalStateException("Can't happen: Found " + ((obj == null) ? "(null)" : obj.getClass().getName()));
            }
        }
        return res;
    }

    /**
     * Helper that cuts a multiline String into separate substrings, putting them into 'res', each one prefixed by
     * 'filler' (if 'filler' is not null) except maybe the first line which may instead be prefixed by 
     * 'firstLineFiller' (if 'firstLineFiller' is not null). 'indent' is the basic indentation String, and must not
     * be null.
     */

    private static void cutUpMultiline(List<ConcatMe> res, String text, String firstLineFiller, String otherLineFiller, String indent) {
        assert res != null;
        assert text != null;
        assert indent != null;
        if (posOfFirstLinebreak(text) < 0) {
            // Generally the case: a simple one-liner without CR nor LF
            res.add(makeFirstLine(indent, firstLineFiller, otherLineFiller, text));
        } else {
            try {
                // More rarely the case: multiline; break apart with the Java "LineNumberReader"
                // Note that the "LineNumberReader" suppresses the last line if it's empty
                LineNumberReader lnr = new LineNumberReader(new StringReader(text));
                res.add(makeFirstLine(indent, firstLineFiller, otherLineFiller, lnr.readLine()));
                {
                    String line;
                    while ((line = lnr.readLine()) != null) {
                        res.add(makeSubsequentLine(indent, otherLineFiller, line));
                    }
                }
            } catch (IOException exe) {
                cannotHappen(exe);
            }
        }
    }

    /**
     * Helper
     */

    private static ConcatMe makeFirstLine(String indent, String firstLineFiller, String otherLineFiller, String text) {
        if (firstLineFiller != null) {
            return new ConcatMe(indent, firstLineFiller, text);
        } else {
            return makeSubsequentLine(indent, otherLineFiller, text);
        }
    }

    /**
     * Helper
     */

    private static ConcatMe makeSubsequentLine(String indent, String filler, String text) {
        if (filler != null) {
            return new ConcatMe(indent, filler, text);
        } else {
            return new ConcatMe(indent, text);
        }
    }

    /**
     * Helper
     */

    private static int posOfFirstLinebreak(String x) {
        assert x != null;
        int crPos = x.indexOf('\r');
        int lfPos = x.indexOf('\n');
        if (crPos >= 0 && lfPos >= 0) {
            // both LF and CR exist, take first position
            return Math.min(crPos, lfPos);
        } else {
            // one or both of the positions does not exist, take the highest one ("max") as
            // -1 means 'not exists'. If both do not exist, this return -1, which is correct
            return Math.max(crPos, lfPos);
        }
    }

}

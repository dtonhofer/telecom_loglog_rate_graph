package com.mplify.logging;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.mplify.checkers.Check;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * This is just a "list of ThrowableEntry", with list element "n+1" being the
 * "cause" of list element "n". 
 * 
 * Formerly we just had List<ThrowableEntry>. This class makes things clearer.
 * 
 * 2009.01.31 - Created from existing code
 * 2011.10.15 - Added "toStory2()" which formats the long chains of
 *              "cause exception" in a readable way. This enlarges the
 *              code considerably.
 * 2011.10.28 - Additional fixing of code
 * 2011.12.06 - Reversed ordering of stacktraces when writing causes 
 *              side-by-side 
 *              In: buildHeaderColumns()
 *              In: addStackPositionsToStory()
 * 2012.06.10 - Fixed formatting problem, reviewed a bit.              
 ******************************************************************************/

public class ThrowableEntryChain {

    /**
     * Ordered from last rethrown (shortest stack, in principle) to primary cause (longest stack, in principle)
     * This is actually an unmodifiable ArrayList, so indexed access is fast. 
     */

    private final List<ThrowableEntry> chain; // not null, immutable, may have length 0

    /**
     * Transform a possibly nested Throwable into a ThrowableEntryChain. If the passed 'exe' is (null), the empty chain
     * is generated
     */

    public ThrowableEntryChain(Throwable throwable) {
        if (throwable != null) {
            LinkedList<ThrowableEntry> res = new LinkedList();
            appendThrowableAndItsCausesToList(res, throwable);
            this.chain = Collections.unmodifiableList(new ArrayList(res));
        } else {
            this.chain = Collections.unmodifiableList(new ArrayList());
        }
    }

    /**
     * Construct from an existing chain, which must not be null but may be empty
     * The passed "chain" should be dereferenced by the caller ASAP, so that it
     * is fully controlled by this instance. 
     */

    public ThrowableEntryChain(List<ThrowableEntry> chainIn) {
        Check.notNull(chainIn, "chain");
        this.chain = Collections.unmodifiableList(new ArrayList(chainIn));
    }

    /**
     * Construct an empty chain
     */

    public ThrowableEntryChain() {
        this.chain = Collections.unmodifiableList(new ArrayList<ThrowableEntry>(0));
    }

    /**
     * Helper. Return a (modifiable) ArrayList<String>, which is an ArrayList for fast indexed access.
     */

    private static ArrayList<String> makeStackTraceOfStrings(Throwable throwable) {
        assert throwable != null;
        ArrayList<String> res;
        StackTraceElement[] arrayIn = throwable.getStackTrace();
        if (arrayIn == null) {
            arrayIn = new StackTraceElement[0]; // not supposed to happen, but I'm paranoid
        }
        res = new ArrayList(arrayIn.length);
        for (int i = 0; i < arrayIn.length; i++) {
            res.add(arrayIn[i].toString()); // just stringify
        }
        return res;
    }

    /**
     * Helper method
     */

    private static void appendThrowableAndItsCausesToList(LinkedList<ThrowableEntry> res, Throwable throwable) {
        assert throwable != null;
        assert res != null;
        // this exception
        {
            String className = throwable.getClass().getName(); // not null
            String message = throwable.getMessage(); // may be null
            res.add(new ThrowableEntry(className, message, makeStackTraceOfStrings(throwable)));
        }
        // its cause; a tail-recursive call
        if (throwable.getCause() != null) {
            appendThrowableAndItsCausesToList(res, throwable.getCause());
        }
    }

    /**
     * Make a 'story'
     * If "reverseLines", then the lines are ordered from shallowest
     * stack entry to deepest stack entry, which looks "more natural"
     */

//    @SuppressWarnings("unchecked")
    public Story toStory(boolean reverseLines) {
        Story res = new Story();
        boolean addLead = false;
        for (ThrowableEntry te : chain) {
            if (addLead) {
                res.add("~~~ caused by ~~~");
            }
            res.add(te.toStory(reverseLines));
            addLead = true;
        }
        return res;
    }

    /**
     * Make a multiline string
     * If "reverseLines", then the lines are ordered from shallowest
     * stack entry to deepest stack entry, which looks "more natural"
     */

//    @SuppressWarnings("unchecked")
    public String toMultilineString(boolean reverseLines) {
        return toStory(reverseLines).toString();
    }

    /**
     * Chain empty?
     */

    public boolean isEmpty() {
        return chain.isEmpty();
    }

    /**
     * Get the underlying cloned list (use sparingly!)
     */

    public List<ThrowableEntry> getCopyOfUnderlyingList() {
        return new LinkedList(chain);
    }

    /**
     * Helper
     */

    private int determineMaxStackDepthInThrowableChain() {
        int res = 0;
        for (ThrowableEntry te : chain) {
            res = Math.max(res, te.getStackTrace().size());
        }
        return res;
    }

    /**
     * Helper. Build a matrix of "stack positions" arranged like this:
     * 
     * primary exc. rethrown   rethrown   rethrown 
     * +----------+----------+----------+----------+
     * | TOS pos  | (null)   | (null)   | (null)   |
     * | pos C    | TOS pos  | (null)   | (null)   |
     * | pos B    | pos B    | TOS pos  | (null)   |
     * | pos A    | pos A    | pos A    | TOS pos  |
     * | pos U    | pos U    | pos U    | pos U    |
     * +----------+----------+----------+----------+
     * 
     * With the dimensions and directions:
     * 
     *     +------------> second dimension [0.. chain.size()[
     *     |
     *     |
     *     |
     *     V
     *     first dimension [0..maxStackPos[
     *     
     * Mostly, the "primnary exception" would have the most "stack position"
     * and the subsequent rethrown exceptions would have less and less positions,
     * but that is not necessarily the case.
     */

    private static class StackPosMatrixCombo {

        public final String[][] stackPosMatrix; // not null, dim 0 = stack for each throwable, blank filled at first positions, dim 1 = chain of throwables
        public final int[] maxWidthArray; // not null, max text width for each of the stack lines

        public StackPosMatrixCombo(String[][] stackPosMatrix, int[] maxWidthArray) {
            assert stackPosMatrix != null;
            assert maxWidthArray != null;
            assert stackPosMatrix.length >= 0;
            assert stackPosMatrix.length == 0 || (stackPosMatrix[0].length == maxWidthArray.length); // also for all the other stack traces
            this.stackPosMatrix = stackPosMatrix;
            this.maxWidthArray = maxWidthArray;
        }

    }

    private String[][] initializeStackPosMatrix(int maxStackDepth) {
        int chainSize = chain.size();
        String[][] stackPosMatrix = new String[maxStackDepth][]; // note that the stack trace is along dimension 0
        for (int i = 0; i < maxStackDepth; i++) {
            stackPosMatrix[i] = new String[chainSize];
        }
        return stackPosMatrix;
    }

    private StackPosMatrixCombo buildStackPosMatrixCombo() {
        int chainSize = chain.size();
        int maxStackDepth = determineMaxStackDepthInThrowableChain(); // may return 0 - there would not be any max stack position....
        // this matrix shall be filled
        String[][] stackPosMatrix = initializeStackPosMatrix(maxStackDepth);
        // this array informs about how wide the text of any column is
        int[] maxWidthArray = new int[chainSize];
        // iterate over all the throwables, i=0 is the "primary cause"
        for (int curChain = 0; curChain < chainSize; curChain++) {
            maxWidthArray[curChain] = 30; // set to 30 chars at least to handle the special case of a chain with no entries  
            //
            // TopOfStack is at position 0; the returned structure is an ArrayList, so indexed access is ok
            //
            List<String> stackTrace = chain.get(curChain).getStackTrace();
            //
            // Fill matrix from the bottom up (from maxStackPos-1, moving towards, but not necessarily reaching, 0)
            // "curStackPos" reads from the stack, "curMatrixColPos" writes to the matrix
            //
            int curMatrixColPos = maxStackDepth - 1;
            for (int curStackPos = stackTrace.size() - 1; curStackPos >= 0; curStackPos--) {
                String txt = stackTrace.get(curStackPos);
                stackPosMatrix[curMatrixColPos][curChain] = txt; // note that the stack trace is along dimension 0
                maxWidthArray[curChain] = Math.max(maxWidthArray[curChain], txt.length());
                curMatrixColPos--;
            }
        }
        return new StackPosMatrixCombo(stackPosMatrix, maxWidthArray);
    }

    /**
     * Helper
     */

    private static List<String> cutUpString(String txt) {
        List<String> res = new LinkedList();
        try {
            // More rarely the case: multiline; break apart with the Java "LineNumberReader"
            // Note that the "LineNumberReader" suppresses the last line if it's empty
            LineNumberReader lnr = new LineNumberReader(new StringReader(txt));
            String line;
            while ((line = lnr.readLine()) != null) {
                res.add(line);
            }
        } catch (IOException exe) {
            throw new IllegalStateException("Cannot happen", exe);
        }
        return res;
    }

    /**
     * Helper
     */

    private static List<String> buildHeaderLinesFromThrowableEntry(ThrowableEntry te, int maxColWidth) {
        assert te != null;
        assert maxColWidth > 0;
        List<String> res = new LinkedList();
        //
        // first generate the Strings
        //
        res.add(te.getClassName()); // classname is not null
        String message = te.getMessage(); // may be null
        if (message != null) {
            res.addAll(cutUpString(message));
        }
        //
        // then: lines that are longer than "maxColWidth" are cut up
        //
        for (int i = 0; i < res.size(); i++) {
            String cur = res.get(i);
            if (cur.length() > maxColWidth) {
                // replace line i by lines i, i+1
                res.remove(i);
                res.add(i, cur.substring(0, maxColWidth));
                res.add(i + 1, cur.substring(maxColWidth));
            }
        }
        return res;
    }

    /**
     * Helper. Returns the list (over the chain) of the List of header lines, the first line
     * always being the class name
     */

    private List<List<String>> buildHeaderColumns(int[] maxWidthArray) {
        int chainSize = chain.size();
        assert maxWidthArray != null;
        assert maxWidthArray.length == chainSize;
        List<List<String>> res = new LinkedList();
        // iterate over all the chains, with the "primary cause" first
        // for (int curChain = chain.size()-1; curChain >=0; curChain--) {
        for (int curChain = 0; curChain < chainSize; curChain++) {
            res.add(buildHeaderLinesFromThrowableEntry(chain.get(curChain), maxWidthArray[curChain]));
        }
        return res;
    }

    /**
     * Helper
     */

    private static int determineMaxHeaderLineCount(List<List<String>> headerLines) {
        int res = 0;
        for (List<String> l : headerLines) {
            res = Math.max(l.size(), res);
        }
        return res;
    }

    /**
     * Helper
     */

    private static String SEPARATOR = "  ";
    private static String LEFT_INTRO = "| ";
    private static String LEFT_INTRO_EQUALS = "= ";
    private static String SEPARATOR_DASH = "--";
    private static String LEFT_INTRO_DASH = "+-";

    private static void addHeaderColumnsToStory(List<List<String>> headerColumns, int[] maxColWidth, Story res) {
        int maxHeaderLineCount = determineMaxHeaderLineCount(headerColumns);
        for (int curLine = 0; curLine < maxHeaderLineCount; curLine++) {
            StringBuilder buf = new StringBuilder();
            boolean addSep = false;
            int curColumnIndex = 0;
            for (List<String> curColumn : headerColumns) {
                if (addSep) {
                    buf.append(SEPARATOR);
                }
                buf.append(LEFT_INTRO);
                if (curLine < curColumn.size()) {
                    // justified text, original text may be shorter or longer than the column
                    String txt = (curColumn.get(curLine) + LogFacilities.getSpaceString(maxColWidth[curColumnIndex])).substring(0, maxColWidth[curColumnIndex]);
                    buf.append(txt);
                } else {
                    // just whitespace to replace missing text
                    buf.append(LogFacilities.getSpaceString(maxColWidth[curColumnIndex]));
                }
                addSep = true;
                curColumnIndex++;
            }
            res.add(LogFacilities.rightSideTrim(buf.toString(), 0)); // add this to the story, with whitespace on the right ripped away
        }
        //
        // Add a separator line
        //
        {
            StringBuilder buf = new StringBuilder();
            boolean addSep = false;
            int headerColumnCount = headerColumns.size();
            for (int curColumnIndex = 0; curColumnIndex < headerColumnCount; curColumnIndex++) {
                if (addSep) {
                    buf.append(SEPARATOR_DASH);
                }
                buf.append(LEFT_INTRO_DASH);
                buf.append(LogFacilities.getDashString(maxColWidth[curColumnIndex]));
                addSep = true;
                curColumnIndex++;
            }
            res.add(buf.toString());
        }
    }

    /**
     * Helper
     */

    private static void addStackPositionsToStory(String[][] stackPosMatrix, int[] maxColWidth, Story res) {
        for (int curStackPos = 0; curStackPos < stackPosMatrix.length; curStackPos++) {
            StringBuilder buf = new StringBuilder();
            boolean addSep = false;
            int chainSize = stackPosMatrix[curStackPos].length; // always the same, actuall
            for (int curChain = 0; curChain < chainSize; curChain++) {
                if (addSep) {
                    buf.append(SEPARATOR);
                }
                // extra: if a previous column exists and its content is the same as the current one, use an "=" to indicate equality
                String curTxt = stackPosMatrix[curStackPos][curChain];
                if (curChain > 0 && curTxt != null && curTxt.equals(stackPosMatrix[curStackPos][curChain - 1])) {
                    buf.append(LEFT_INTRO_EQUALS);
                } else {
                    buf.append(LEFT_INTRO);
                }
                if (curTxt == null) {
                    buf.append(LogFacilities.getSpaceString(maxColWidth[curChain]));
                } else {
                    buf.append(curTxt);
                    buf.append(LogFacilities.getSpaceString(Math.max(0, maxColWidth[curChain] - curTxt.length())));
                }
                addSep = true;
            }
            res.add(LogFacilities.rightSideTrim(buf.toString(), 0)); // add this to the story, with whitespace on the right ripped away

        }
    }

    /**
     * Reduce the chain into a common (but rather wide) representation
     */

    public Story toStory2() {
        Story res = new Story();
        //
        // Create a matrix of strings to print (and determine the width of each column at the same time)
        //
        StackPosMatrixCombo x = buildStackPosMatrixCombo();
        String[][] stackPosMatrix = x.stackPosMatrix; // not null, dim 0 = stack for each throwable, blank filled at first positions; dim 1 = chain of throwables
        int[] maxWidthArray = x.maxWidthArray; // how wide any txt width is supposed to be, size of stackPosMatrix's dim 1
        //
        // Create a matrix of "headers", which give the throwable's class name and multiline message.
        // The primary cause is at stackPosMatrix[*]
        //
        List<List<String>> headerColumns = buildHeaderColumns(maxWidthArray);
        //
        // The result is then concatenated into lines spanning all the headers
        //
        addHeaderColumnsToStory(headerColumns, maxWidthArray, res);
        //
        // Compose strings to print, line by line
        //
        addStackPositionsToStory(stackPosMatrix, maxWidthArray, res);
        return res;
    }
}

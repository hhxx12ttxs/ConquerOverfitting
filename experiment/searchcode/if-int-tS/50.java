/*
 * Copyright (c) 2008, 2009, 2010
 *  Universitaet Tuebingen.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 * * Redistributions of source code must retain the above copyright 
 *  notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of the University of Tuebingen nor the names of
 *   the contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package org.isabelle.mledit;

import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.isabelle.mledit.mllexer.MLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import static org.isabelle.mledit.mllexer.MLTokenId.*;

/**
 *
 * @author gast
 */
public class MLIndentTask implements IndentTask {

    private final static Logger LOG = Logger.getLogger(MLIndentTask.class.getName());
    private final Context ctx;
    public static final int TAB_SIZE = 4;

    MLIndentTask(Context context) {
        this.ctx = context;
    }

    public void reindent() throws BadLocationException {
        Document doc = ctx.document();
        TokenHierarchy<Document> th = TokenHierarchy.get(doc);
        List<TokenSequence<?>> tss = th.embeddedTokenSequences(ctx.startOffset(), false);

        if (tss.size() >= 1) {
            ts = (TokenSequence<MLTokenId>) tss.get(tss.size() - 1);
            if (ts.language() == MLTokenId.language()) {
                indentLine(ctx.lineStartOffset(ctx.startOffset()));
            }
        }
    }

    public ExtraLock indentLock() {
        return null;
    }

    private void indentLine(int lineStartOffset) throws BadLocationException {
        System.out.println("In line " + ctx.document().getText(lineStartOffset, lineLength(lineStartOffset)));
        int nonWSOffset = firstNonWhite(lineStartOffset);
        MLTokenId curTok = null;
        if (nonWSOffset != -1) {
            ts.move(nonWSOffset);
            ts.moveNext();
            curTok = ts.token().id();
        }
        MLTokenId tok = ts.token().id();
        switch (tok) {
            case COMMENT:
                indentAsPrevious(lineStartOffset);
                break;
            default:
                indentByStructure(lineStartOffset, curTok);
        }
    }

    /**
     * Indent at the same depth as the previous one
     */
    private void indentAsPrevious(int lineStartOffset) throws BadLocationException {
        if (lineStartOffset > 0) {
            int prevLine = ctx.lineStartOffset(lineStartOffset - 1);
            setIndent(lineStartOffset, ctx.lineIndent(prevLine));
        } else {
            setIndent(lineStartOffset, 0);
        }
    }
    private TokenSequence<MLTokenId> ts;
    private Stack<Token<MLTokenId>> nesting = new Stack<Token<MLTokenId>>();
    private boolean sequenceAlign;
    private boolean sawNonWhite;

    /**
     * The real indentation engine: find a matching keyword, if any
     */
    private void indentByStructure(int lineStartOffset, MLTokenId curTok) throws BadLocationException {
        nesting.clear();
        sawNonWhite = false;
        sequenceAlign = false;

        while (ts.movePrevious()) {
            checkSequenceAlign();
            int matchOff = matchIndent(ts.token().id(), curTok);
            if (matchOff != -1 && nesting.isEmpty()) {
                if (sequenceAlign) {
                    matchOff = 0;
                }
                setIndent(lineStartOffset, tokenIndent(ts) + matchOff);
                return;
            }
            if (isOpeningToken(ts)) {
                if (nesting.isEmpty()) {
                    int off = TAB_SIZE;
                    if (sequenceAlign) {
                        off = ts.token().length();
                    }
                    setIndent(lineStartOffset, tokenIndent(ts) + off);
                    return;
                } else {
                    popNesting(ts);
                }
            }
            if (isClosingToken(ts)) {
                pushNesting(ts);
            }
        }
        indentAsPrevious(lineStartOffset);
    }

    private void checkSequenceAlign() {
        if (!sawNonWhite && !sequenceAlign) {
            final MLTokenId id = ts.token().id();
            switch (id) {
                case WHITE:
                    break;
                case SEMI:
                    sequenceAlign = true;
                default:
                    sawNonWhite = true;
            }
        }
    }

    private boolean isOpeningToken(TokenSequence<MLTokenId> ts) {
        switch (ts.token().id()) {
            case LPAR:
            case IF:
            case THEN:
            case LET:
            case IN:
                return true;
            default:
                return false;
        }
    }

    private boolean isClosingToken(TokenSequence<MLTokenId> ts) {
        return isClosingToken(ts.token().id());
    }

    private boolean isClosingToken(MLTokenId tok) {
        if (tok == null) {
            return false;
        }
        switch (tok) {
            case RPAR:
            case THEN:
            case ELSE:
            case END:
            case IN:
                return true;
            default:
                return false;
        }
    }

    private boolean matchingPair(MLTokenId opening, MLTokenId closing) {
        return opening == LPAR && closing == RPAR ||
                opening == LET && closing == IN ||
                opening == IN && closing == END ||
                opening == STRUCT && closing == END ||
                opening == SIG && closing == END;
    }

    private void pushNesting(TokenSequence<MLTokenId> ts) {
        nesting.push(ts.token());
    }

    private void popNesting(TokenSequence<MLTokenId> ts) {
        Token<MLTokenId> closing = nesting.pop();
        Token<MLTokenId> opening = ts.token();
        if (!matchingPair(opening.id(), closing.id())) {
            LOG.warning("unexpected pair " + opening + "-" + closing);
        }
    }

    private int matchIndent(MLTokenId a, MLTokenId b) {
        if (a == LET && (b == IN || b == END) ||
                a == IN && b == END ||
                a == STRUCT && b == END ||
                a == SIG && b == END ||
                a == LPAR && b == RPAR ||
                a == IF && (b == THEN || b == ELSE) ||
                a == THEN && b == ELSE) {
            return 0;
        }
        if (a == CASE && b == PIPE ||
                a == DATATYPE && b == PIPE) {
            return 2;
        }
        if ((a == CASE || a == DATATYPE) && !isClosingToken(b)) {
            return 4;
        }
        if (a == FUN && b == VAL ||
                a == VAL && b == FUN) {
            return 0;
        }
        if ((a == STRUCT || a == SIG) && (b == VAL || b == FUN || b == OPEN)) {
            return 0;
        }

        if (a == FUN && b != VAL && !isClosingToken(b) ||
                a == FN && !isClosingToken(b)) {
            return 2;
        }
        return -1;
    }

    private int firstNonWhite(int lineStartOffset) {
        try {
            Document doc = ctx.document();
            for (int i = lineStartOffset;
                    i < doc.getLength(); i++) {
                char c = doc.getText(i, 1).charAt(0);
                if (!Character.isWhitespace(c)) {
                    return i;
                }
                if (c == '\n') {
                    return -1;
                }
            }
            return -1;
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private int tokenIndent(TokenSequence<MLTokenId> ts) throws BadLocationException {
        return indent(ctx.lineStartOffset(ts.offset()), ts.offset());
    }

    private int indent(int start, int end) throws BadLocationException {
        String txt = ctx.document().getText(start, end - start);
        int n = 0;
        for (int i = 0; i != txt.length(); i++) {
            if (txt.charAt(i) == '\t') {
                n += TAB_SIZE;
            } else {
                n++;
            }
        }
        return n;
    }

    private void setIndent(int lineStartOffset, int lineIndent) throws BadLocationException {
        Document doc = ctx.document();
        /*System.out.println("BEFORE\n" + doc.getText(0, doc.getLength()));*/
        ctx.modifyIndent(lineStartOffset, lineIndent);
        /*System.out.println("AFTER " + lineStartOffset + " @ " + lineIndent + "\n" +
                doc.getText(0, doc.getLength()));*/
        try {
            if (ctx.caretOffset() < lineStartOffset + lineIndent) {
                ctx.setCaretOffset(lineStartOffset + lineIndent);
            }
        } catch (NullPointerException ex) {
            // caret may not be present, in which case the logic
            // fails. Not documented in Context
        }
    }

    private int lineLength(int lineStartOffset) throws BadLocationException {
        int i = 0;
        Document doc = ctx.document();
        while (lineStartOffset + i < doc.getLength() &&
                doc.getText(lineStartOffset + i, 1).charAt(0) != '\n') {
            i++;
        }
        return i;
    }
}


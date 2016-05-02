/*
 * Copyright 2011 Ed Venaglia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.venaglia.nondairy.util;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TypingTarget;
import com.intellij.openapi.util.Key;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.diff.FlyweightCapableTreeStructure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.soap.Node;
import java.util.*;
import java.util.logging.Logger;

public class LoggingPsiBuilder implements PsiBuilder {

    public static class Node {
        public LoggingMarker marker;
        public List<Node> children;
        public int start;
        public int end;

        public Node(LoggingMarker marker, List<Node> children, int start, int end) {
            this.marker = marker;
            this.children = children;
            this.start = start;
            this.end = end;
        }

        public String toString() {
            return marker == null ? "ROOT" : String.format("%s - %s (%d to %d)", marker.tokenType, marker.complete ? "completed" : marker.dropped ? "dropped" : "INCOMPLETE", start, end);
        }
    }
    public static class MarkerStack {

        List<LoggingMarker> markers = new ArrayList<LoggingMarker>();

        public void push(LoggingMarker marker) {
            markers.add(marker);
        }

        public void precede(LoggingMarker marker, LoggingMarker before) {
            markers.add(markers.indexOf(before), marker);
        }

        public void completeSince(LoggingMarker marker) {
            int index = markers.indexOf(marker);
            if (index < 0) throw new RuntimeException("shit");
            LoggingMarker completeAfter = markers.get(markers.size() - 1);
            for(LoggingMarker toComplete : markers.subList(index, markers.size())) {
                toComplete.complete = true;
                toComplete.completeAfter = completeAfter;
            }
        }
        public void dropSince(LoggingMarker marker) {
            int index = markers.indexOf(marker);
            if (index < 0) throw new RuntimeException("shit");
            for(LoggingMarker toComplete : markers.subList(index, markers.size())) {
                toComplete.dropped = true;
            }
        }

        private void verifyAllCompleteBeforeOrAt(List<LoggingMarker> toVerify, int atIndex) {
            for(LoggingMarker marker : toVerify) {
                if (!((marker.complete && marker.completeAfter != null && markers.indexOf(marker.completeAfter) <= atIndex) || marker.dropped))
                    throw new RuntimeException("a ha!");
            }
        }

        // If a marker starts before this marker and complete on or after this marker, it is a parent
        // This marker may have completed parents already.  It must complete before those parents.
        private void verifyNoParentsCompletedFirst(List<LoggingMarker> previousMarkers, LoggingMarker markerVerifying, LoggingMarker markerEndAfter) {
            List<LoggingMarker> unsafeCompleteAfters = markers.subList(markers.indexOf(markerVerifying), markers.indexOf(markerEndAfter));

            for(LoggingMarker marker : previousMarkers) {
                if (marker.complete && unsafeCompleteAfters.contains(marker.completeAfter)) {
                    throw new RuntimeException("Completing in the wrong spot!");
                }
            }
        }

        private void verifyCanComplete(LoggingMarker marker) {
            //everything after this block started must already be complete
            verifyAllCompleteBeforeOrAt(markers.subList(markers.indexOf(marker) + 1, markers.size()), markers.size() - 1);

            //everything before this block started must not complete until AFTER this block completes
            verifyNoParentsCompletedFirst(markers.subList(0, markers.indexOf(marker)), marker, markers.get(markers.size() - 1));
        }

        public void complete(LoggingMarker marker) {
            verifyCanComplete(marker);
            marker.complete = true;
            marker.completeAfter = markers.get(markers.size() - 1);
        }

        public void completeBefore(LoggingMarker marker, LoggingMarker before) {
            int markerIndex = markers.indexOf(marker);
            int beforeIndex = markers.indexOf(before);
            if (beforeIndex <= markerIndex) {
                throw new RuntimeException("a ha!!");
            }
            if (markerIndex < 0) {
                throw new RuntimeException("a ha!!");
            }
            verifyAllCompleteBeforeOrAt(markers.subList(markerIndex + 1, beforeIndex), beforeIndex - 1);
            verifyNoParentsCompletedFirst(markers.subList(0, markers.indexOf(marker)), marker, before);
            marker.complete = true;
            marker.completeAfter = markers.get(beforeIndex - 1);
        }

        public void drop(LoggingMarker marker) {
            marker.dropped = true;
        }

        public Node toTree() {
            verifyAllCompleteBeforeOrAt(markers, markers.size());
            return new Node(null, getNodes(markers), 0, markers.size());
        }

        private Node fromMarkerAndDescendants(LoggingMarker marker, List<LoggingMarker> descendants) {
            return new Node(marker, descendants == null ? null : getNodes(descendants),
                           markers.indexOf(marker), marker.completeAfter == null ? markers.size() : markers.indexOf(marker.completeAfter));
        }

        private String getRange(LoggingMarker marker) {
            return markers.indexOf(marker) + " to " + (marker.completeAfter == null ? markers.size() : markers.indexOf(marker.completeAfter));
        }

        private List<Node> getNodes(List<LoggingMarker> markerList) {
            List<Node> nodes = new ArrayList<Node>();

            int nextIndex = 0;
            while(nextIndex < markerList.size()) {
                LoggingMarker nextRoot = markerList.get(nextIndex);
                if (nextRoot.dropped) {
                    nextIndex++;
                    continue;
                }

                if (nextRoot.completeAfter == null)
                    throw new RuntimeException("wtf");

                int nextSibling = markerList.indexOf(nextRoot.completeAfter) + 1;

                if (nextSibling <= 0) {
                    throw new RuntimeException("hmm...");
                }

                Node toAdd = nextIndex == nextSibling - 1 ?
                    fromMarkerAndDescendants(nextRoot, null) :
                    fromMarkerAndDescendants(nextRoot, markerList.subList(nextIndex + 1, nextSibling));

                if (toAdd != null) {
                    nodes.add(toAdd);
                }

                nextIndex = nextSibling;
            }

            return nodes;
        }
    }

    private final PsiBuilder psiBuilder;
    public final Map<Marker, StackTraceElement> markers = new HashMap<Marker, StackTraceElement>();
    public MarkerStack stack = new MarkerStack();

    public LoggingPsiBuilder(PsiBuilder builder) {
        this.psiBuilder = builder;
    }

    @Nullable
    public <T> T getUserData(@NotNull Key<T> tKey) {
        return psiBuilder.getUserData(tKey);
    }

    public <T> void putUserData(@NotNull Key<T> tKey, @Nullable T t) {
        psiBuilder.putUserData(tKey, t);
    }

    public Project getProject() {
        return psiBuilder.getProject();
    }

    public CharSequence getOriginalText() {
        return psiBuilder.getOriginalText();
    }

    public void advanceLexer() {
        psiBuilder.advanceLexer();
    }

    @Nullable
    public IElementType getTokenType() {
        return psiBuilder.getTokenType();
    }

    public void setTokenTypeRemapper(ITokenTypeRemapper iTokenTypeRemapper) {
        psiBuilder.setTokenTypeRemapper(iTokenTypeRemapper);
    }

    public void remapCurrentToken(IElementType iElementType) {
        psiBuilder.remapCurrentToken(iElementType);
    }

    public void setWhitespaceSkippedCallback(WhitespaceSkippedCallback whitespaceSkippedCallback) {
        psiBuilder.setWhitespaceSkippedCallback(whitespaceSkippedCallback);
    }

    @Nullable
    public IElementType lookAhead(int i) {
        return psiBuilder.lookAhead(i);
    }

    @Nullable
    public IElementType rawLookup(int i) {
        return psiBuilder.rawLookup(i);
    }

    public int rawTokenTypeStart(int i) {
        return psiBuilder.rawTokenTypeStart(i);
    }

    @Nullable
    public String getTokenText() {
        return psiBuilder.getTokenText();
    }

    public int getCurrentOffset() {
        return psiBuilder.getCurrentOffset();
    }

    public PsiBuilder.Marker mark() {
        LoggingMarker marker = new LoggingMarker(psiBuilder.mark());
        stack.push(marker);
        return marker;
    }

    @Override
    public <T> T getUserDataUnprotected(@NotNull Key<T> tKey) {
        return psiBuilder.getUserDataUnprotected(tKey);
    }

    @Override
    public <T> void putUserDataUnprotected(@NotNull Key<T> tKey, @Nullable T t) {
        psiBuilder.putUserDataUnprotected(tKey, t);
    }

    public class LoggingMarker implements PsiBuilder.Marker {

        Marker marker;
        boolean complete = false;
        boolean dropped = false;
        String errorMessage = null;
        String tokenType = null;
        LoggingMarker completeAfter = null;

        public LoggingMarker(Marker marker) {
            log("mark");
            this.marker = marker;
            StackTraceElement traceElement = new Throwable().getStackTrace()[2];
            markers.put(this, traceElement);
        }

        @Override
        public PsiBuilder.Marker precede() {
            log("precede");
            LoggingMarker newMarker = new LoggingMarker(marker.precede());
            stack.precede(newMarker, this);
            return newMarker;
        }

        @Override
        public void drop() {
            log("drop");
            stack.drop(this);
            marker.drop();
            markers.remove(this);
        }

        @Override
        public void rollbackTo() {
            log("rollbackTo");
            marker.rollbackTo();
            stack.dropSince(this);
            markers.remove(this);
        }

        @Override
        public void done(IElementType iElementType) {
            log("done %s", iElementType);
            marker.done(iElementType);
            tokenType = iElementType.toString();
            stack.complete(this);
            markers.remove(this);
        }

        @Override
        public void collapse(IElementType iElementType) {
            log("collapse %s", iElementType);
            marker.collapse(iElementType);
            tokenType = iElementType.toString();
            stack.completeSince(this);
            markers.remove(this);
        }

        @Override
        public void doneBefore(IElementType iElementType, PsiBuilder.Marker marker) {
            log("doneBefore %s %s", iElementType, marker);
            marker.doneBefore(iElementType, marker);
            tokenType = iElementType.toString();
            stack.completeBefore(this, (LoggingMarker) marker);
            markers.remove(this);
        }

        @Override
        public void doneBefore(IElementType iElementType, PsiBuilder.Marker marker, String s) {
            log("doneBefore %s %s %s", iElementType, marker, s);
            marker.doneBefore(iElementType, marker, s);
            tokenType = iElementType.toString();
            stack.completeBefore(this, (LoggingMarker) marker);
            markers.remove(this);
        }

        @Override
        public void error(String s) {
            log("error %s", s);
            marker.error(s);
            tokenType = "error";
            errorMessage = s;
            stack.complete(this);
            markers.remove(this);
        }

        @Override
        public void errorBefore(String s, PsiBuilder.Marker marker) {
            log("errorBefore %s", marker);
            marker.errorBefore(s, marker);
            tokenType = "error";
            errorMessage = s;
            stack.completeBefore(this, (LoggingMarker) marker);
            markers.remove(this);
        }

        @Override
        public void setCustomEdgeTokenBinders(@Nullable WhitespacesAndCommentsBinder whitespacesAndCommentsBinder, @Nullable WhitespacesAndCommentsBinder whitespacesAndCommentsBinder1) {
            log("setCustomEdgeTokenBinders %s %s", whitespacesAndCommentsBinder, whitespacesAndCommentsBinder1);
            marker.setCustomEdgeTokenBinders(whitespacesAndCommentsBinder, whitespacesAndCommentsBinder1);
        }
    }


    public void error(String s) {
        psiBuilder.error(s);
    }

    public boolean eof() {
        return psiBuilder.eof();
    }

    public ASTNode getTreeBuilt() {
        return psiBuilder.getTreeBuilt();
    }

    public FlyweightCapableTreeStructure<LighterASTNode> getLightTree() {
        return psiBuilder.getLightTree();
    }

    public void setDebugMode(boolean b) {
        psiBuilder.setDebugMode(b);
    }

    public void enforceCommentTokens(TokenSet tokenSet) {
        psiBuilder.enforceCommentTokens(tokenSet);
    }

    @Nullable
    public LighterASTNode getLatestDoneMarker() {
        return psiBuilder.getLatestDoneMarker();
    }

    private static void log(String s, Object... o) {
        PluginManager.getLogger().warn(String.format(s, o));
    }
}


/*
 * Copyright 2006 Simon Pepping.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: Paragraph.java 39 2006-06-29 18:38:06Z simon $ */

package cc.creativecomputing.gui.text.linebreaking;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A list of linebreaking elements
 */
public class Paragraph{
    
    public enum WhiteSpaceTreatment { IGNORE, PRESERVE, SURROUNDING, BEFORE, AFTER };
    
    public static Penalty START_PENALTY = new PenaltyImpl();
    public static int LINE_PENALTY = 10;
    public static int DOUBLE_HYPHEN_DEMERITS = 10000;
    public static int FINAL_HYPHEN_DEMERITS = 5000;
    public static int ADJ_DEMERITS = 10000;
    private static final int NUM_ADJ_CLASSES = 4;

    private WhiteSpaceTreatment wst;
    private List<Float> reqLineLengths;
    private int tolerance;
    private int looseness = 0;
    private int linePenalty = LINE_PENALTY;
    private int doubleHyphenDemerits = DOUBLE_HYPHEN_DEMERITS;
    private int finalHyphenDemerits = FINAL_HYPHEN_DEMERITS;
    private int adjDemerits = ADJ_DEMERITS;
    
    private List<Element> _myElements = new ArrayList<Element>();
    
    /**
     * @param wst the white space treatment policy of this paragraph
     * @param reqLineLengths the target line lengths
     * @param tolerance the tolerance parameter for the linebreak decisions
     */
    public Paragraph(WhiteSpaceTreatment wst, List<Float> reqLineLengths, int tolerance, int looseness) {
        super();
        this.wst = wst;
        this.reqLineLengths = reqLineLengths;
        this.tolerance = tolerance;
        this.looseness = looseness;
    }
    
    /**
     * @param wst the white space treatment policy of this paragraph
     * @param reqLineLength the target line length
     * @param tolerance the tolerance parameter for the linebreak decisions
     */
    public Paragraph(WhiteSpaceTreatment wst, int reqLineLength,
                     int tolerance, int looseness) {
        super();
        this.wst = wst;
        this.reqLineLengths = new ArrayList<Float>(1);
        reqLineLengths.add(new Float(reqLineLength));
        this.tolerance = tolerance;
        this.looseness = looseness;
    }
    
    /**
     * @param wst
     * @param reqLineLengths
     * @param tolerance
     * @param linePenalty
     * @param doubleHyphenDemerits
     * @param finalHyphenDemerits
     * @param adjDemerits
     */
    public Paragraph(WhiteSpaceTreatment wst, List<Float> reqLineLengths,
                     int tolerance, int looseness,
                     int linePenalty, int doubleHyphenDemerits,
                     int finalHyphenDemerits, int adjDemerits) {
        super();
        this.wst = wst;
        this.reqLineLengths = reqLineLengths;
        this.tolerance = tolerance;
        this.looseness = looseness;
        this.linePenalty = linePenalty;
        this.doubleHyphenDemerits = doubleHyphenDemerits;
        this.finalHyphenDemerits = finalHyphenDemerits;
        this.adjDemerits = adjDemerits;
    }

    /**
     * @return Returns the target line length.
     */
    public float getReqLineLength() {
        return reqLineLengths.get(0);
    }

    /**
     * @return Returns the target line length.
     */
    public float getMaxReqLineLength() {
        float maxReqLineLength = 0;
        for (Float reqLineLength : reqLineLengths) {
            if (maxReqLineLength < reqLineLength) {
                maxReqLineLength = reqLineLength;
            }
        }
        return maxReqLineLength;
    }

    /**
     * @return Returns the white space treatment policy of this paragraph.
     */
    public WhiteSpaceTreatment getWhiteSpaceTreatment() {
        return wst;
    }

    /**
     * @param adjDemerits The adjDemerits to set.
     */
    public void setAdjDemerits(int adjDemerits) {
        this.adjDemerits = adjDemerits;
    }

    /**
     * @param doubleHyphenDemerits The doubleHyphenDemerits to set.
     */
    public void setDoubleHyphenDemerits(int doubleHyphenDemerits) {
        this.doubleHyphenDemerits = doubleHyphenDemerits;
    }

    /**
     * @param finalHyphenDemerits The finalHyphenDemerits to set.
     */
    public void setFinalHyphenDemerits(int finalHyphenDemerits) {
        this.finalHyphenDemerits = finalHyphenDemerits;
    }

    /**
     * @param linePenalty The linePenalty to set.
     */
    public void setLinePenalty(int linePenalty) {
        this.linePenalty = linePenalty;
    }

    /**
     * Calculate the best linebreaking points
     * @return the node at the end of the paragraph
     * @throws LineBreakingException If the paragraph is not well formed
     */
    public Node doLineBreaking() throws LineBreakingException {

        // check if final element is a forced break
        Element finalElt = _myElements.get(_myElements.size() - 1);
        if (!(finalElt instanceof EOPPenalty)) {
            throw new LineBreakingException
                ("Malformed Paragraph: last element is not an End-Of-Paragraph penalty");
        }
        
        // return node
        Node bestNodeByClass[] = null;
        
        // total width of the boxes up to and including this element
        MinOptMax totalBoxWidth = new MinOptMax();

        // the list of active nodes
        int numActiveLists;
        List<List<Node>> activeLists;
        if (looseness == 0) {
            numActiveLists = reqLineLengths.size();
            activeLists = new ArrayList<List<Node>>(numActiveLists);
        } else {
            numActiveLists = Integer.MAX_VALUE;
            activeLists = new ArrayList<List<Node>>();
        }
        List<Node> finalNodes = null;
        
        // add the start penalty and the corresponding node
        if (!(_myElements.get(0) instanceof Penalty)) {
        	_myElements.add(0, START_PENALTY);
        }
        Node startNode = new Node(0, 0, 1, -1, 0, 0, 0, null);
        List<Node> activeList;
        activeList = new ArrayList<Node>(1);
        activeLists.add(0, activeList);
        activeList.add(startNode);
        calcAfter(startNode, totalBoxWidth);
        
        // main loop over each element of the paragraph
        for (int lbPos = 1; lbPos < _myElements.size(); ++lbPos) {
            Element elt = _myElements.get(lbPos);
            totalBoxWidth.add(elt.getWidth());
            // find the next linebreak opportunity
            if (!(elt instanceof Penalty)) {
                continue;
            }
            // check if the penalty is a forced break before the end of the paragraph
            if (lbPos != _myElements.size() - 1 && elt instanceof EOPPenalty) {
                throw new LineBreakingException
                ("Malformed Paragraph: a forced break before the end of the paragraph");
            }
            // store the data of this linebreak opportunity in a temporary node
            Node tempNode = new Node(0, 0, 0, -2, 0, 0, lbPos, null);
            calcBefore(tempNode, totalBoxWidth);
            
            // find the best node for this linebreak opportunity
            boolean hasActiveList = false;
            ListIterator<List<Node>> iter = activeLists.listIterator();
            while (iter.hasNext()) {
                int index = iter.nextIndex();
                activeList = iter.next();
                if (activeList == null) {
                    bestNodeByClass = null;
                    continue;
                }
                bestNodeByClass = findBestNode(activeList.listIterator(), tempNode);
                if (activeList.isEmpty()) {
                    iter.set(null);
                } else {
                    hasActiveList = true;
                }

                List<Node> nextActiveList = null;
                if (elt instanceof EOPPenalty) {
                    nextActiveList = finalNodes;
                } else {
                    if (index == numActiveLists - 1) {
                        nextActiveList = iter.previous();
                        iter.next();
                    } else if (iter.hasNext()) {
                        nextActiveList = iter.next();
                        iter.previous();
                    } else {
                        iter.add(null);
                        iter.previous();
                    }
                }
                // and add it to the active list
                calcAfter(tempNode, totalBoxWidth);
                for (int i = 0; i < NUM_ADJ_CLASSES; ++i) {
                    if (bestNodeByClass[i] != null) {
                        bestNodeByClass[i].copyBefore(tempNode);
                        bestNodeByClass[i].copyAfter(tempNode);
                        if (nextActiveList == null) {
                            nextActiveList = new ArrayList<Node>();
                        }
                        nextActiveList.add(bestNodeByClass[i]);
                    }
                }
                if (nextActiveList != null) {
                    if (elt instanceof EOPPenalty) {
                        finalNodes = nextActiveList;
                    } else {
                        iter.set(nextActiveList);
                    }
                    hasActiveList = true;
                }
                
            }
            // the process has stopped, no set of feasible linebreaks found
            if (!hasActiveList) {
                // this only occurs if bestNode is null, and it will return null
                break;
            }
        }
        
        // result for the last element investigated;
        // null if no set of feasible breaks was found;
        // otherwise the best node for the final forced break.
        if (finalNodes == null) {
            return null;
        }

        Node bestNode = null;
        double minDemerits = Double.MAX_VALUE;
        for (Node node : finalNodes) {
            double demerits = node.getDemerits();
            if (demerits < minDemerits) {
                minDemerits = demerits;
                bestNode = node;
            }
        }
        
        if (bestNode != null && looseness != 0) {
            int bestLooseness = 0;
            int bestLineNumber = bestNode.getLineNumber();
            minDemerits = Double.MAX_VALUE;
            for (Node node : finalNodes) {
                int currLooseness = node.getLineNumber() - bestLineNumber;
                double demerits = node.getDemerits();
                // The first two conditions apply to nodes with the best looseness up to now;
                // the third condition applies to subsequent nodes with the best looseness.
                // Due to the starting state with bestLooseness = 0,
                // and the fact that bestLooseness is only moved in the direction of looseness,
                // the first condition applies to the case looseness < 0,
                // and the second condition to the case looseness > 0.
                if ((looseness <= currLooseness && currLooseness < bestLooseness)
                        || (bestLooseness < currLooseness && currLooseness <= looseness)
                        || (currLooseness == bestLooseness && demerits < minDemerits)) {
                    bestLooseness = currLooseness;
                    minDemerits = demerits;
                    bestNode = node;
                }
            }
        }

        // Change the widthAfter of the penalty of the last line but one,
        // in agreement with the value used in the linebreak calculations.
        // Create a new MinOptMax, because the one used may be shared with other penalties.
        Node previous = bestNode.getPrevious();
        Penalty penalty = (Penalty) _myElements.get(previous.getLBPos());
        MinOptMax w = new MinOptMax(penalty.getWidthAfter());
        w.add(((EOPPenalty) _myElements.get(_myElements.size() - 1)).getLastLineAdditionalMaxStart());
        penalty.setWidthAfter(w);

        return bestNode;
    }

    // calculate the data for the line before the linebreak and store them in the node
    private void calcBefore(Node node, MinOptMax totalBoxWidth) {
        int pos = node.getLBPos();
        MinOptMax totalBoxWidthBefore = new MinOptMax(totalBoxWidth);
        MinOptMax BPWidth = new MinOptMax();
        // decrement the width of the (box)penalty itself
        totalBoxWidthBefore.decr(_myElements.get(pos).getWidth());
        // suppress before linebreak
        if (wst == WhiteSpaceTreatment.BEFORE || wst == WhiteSpaceTreatment.SURROUNDING) {
            while (--pos >= 0 && _myElements.get(pos).isSuppressible() || _myElements.get(pos).isBP()) {
                Element e = _myElements.get(pos);
                totalBoxWidthBefore.decr(e.getWidth());
                if (e.isBP()) {
                    BPWidth.add(e.getWidth());
                }
            }
            // set pos to the first position after the line elements
            ++pos;
        }
        node.setLineBeforeEndPos(pos);
        node.setBPWidthBefore(BPWidth);
        node.setTotalBoxWidthBefore(totalBoxWidthBefore);
    }
    
    // calculate the data for the line after the linebreak and store them in the node
    private void calcAfter(Node node, MinOptMax totalBoxWidth) {
        int pos = node.getLBPos();
        MinOptMax totalBoxWidthAfter = new MinOptMax(totalBoxWidth);
        MinOptMax BPWidth = new MinOptMax();
        // suppress after linebreak
        if (wst == WhiteSpaceTreatment.AFTER || wst == WhiteSpaceTreatment.SURROUNDING) {
            while (++pos < _myElements.size()  && (_myElements.get(pos).isSuppressible() || _myElements.get(pos).isBP())) {
                Element e = _myElements.get(pos);
                totalBoxWidthAfter.add(e.getWidth());
                if (e.isBP()) {
                    BPWidth.add(e.getWidth());
                }
            }
        } else {
            // without suppression set pos to the position after the linebreak
            ++pos;
        }
        node.setLineAfterStartPos(pos);
        node.setBPWidthAfter(BPWidth);
        node.setTotalBoxWidthAfter(totalBoxWidthAfter);
    }
    
    private Node[] findBestNode(ListIterator<Node> activeIter, Node endNode) {
        int lbPos = endNode.getLBPos();
        int endPos = endNode.getLineBeforeEndPos();
        Penalty pen = (Penalty) _myElements.get(lbPos);
        // the width of the penalty before the linebreak
        // and the border/padding width before the linebreak
        // are the same for each active node
        MinOptMax preLineLength = new MinOptMax(pen.getWidthBefore());
        preLineLength.add(endNode.getBPWidthBefore());

        double minTotalDemerits = Double.MAX_VALUE;
        double[] minTotalDemeritsByClass = new double[NUM_ADJ_CLASSES];
        for (int i = 0; i < NUM_ADJ_CLASSES; ++i) {
            minTotalDemeritsByClass[i] = Double.MAX_VALUE;
        }
        Node[] bestNodeByClass = new Node[NUM_ADJ_CLASSES];
        
        // consider the penalty in each active node as a possible line start,
        // i.e. as the linebreak of the preceding line
        while (activeIter.hasNext()) {
            Node activeNode = activeIter.next();
            
            // for debugging only
            int prevLBPos =  activeNode.getLBPos();
            if (prevLBPos == lbPos) {
                continue;
            }
            Penalty prevPen = (Penalty) _myElements.get(prevLBPos);
            int lineNumber = activeNode.getLineNumber() + 1;
            
            MinOptMax lineLength = new MinOptMax(preLineLength);
            // add the width after the preceding linebreak
            // and the border/padding width after the preceding linebreak
            // to the linelength
            lineLength.add(new MinOptMax(prevPen.getWidthAfter()));
            if (pen instanceof EOPPenalty) {
                lineLength.add(((EOPPenalty) pen).getLastLineAdditionalMaxStart());
            }
            lineLength.add(activeNode.getBPWidthAfter());
            int startPos = activeNode.getLineAfterStartPos();

            // add the width of the line elements to the line length;
            // note: penalties return 0 width
            MinOptMax l = MinOptMax.decr(endNode.getTotalBoxWidthBefore(),
                                         activeNode.getTotalBoxWidthAfter());
            lineLength.add(l);

            // calculate adjustment ratio
            float reqLineLength;
            if (lineNumber < reqLineLengths.size()) {
                reqLineLength = reqLineLengths.get(lineNumber);
            } else {
                reqLineLength = reqLineLengths.get(reqLineLengths.size() - 1);
            }
            double adjRatio = calcAdjRatio(lineLength, reqLineLength);
            // line is too long; deactivate active node
            if (adjRatio < -1) {
                activeIter.remove();
                continue;
            }
            
            // line is too short; not a feasible node
            if (adjRatio > tolerance) {
                continue;
            }
            
            int adjClass = calcAdjClass(adjRatio);
            // calculate demerits
            double demerits = calcDemerits(adjRatio, pen.getValue());
            if (pen instanceof EOPPenalty && prevPen.isHyphenated()) {
                demerits += finalHyphenDemerits;
            } else if (pen.isHyphenated() && prevPen.isHyphenated()) {
                demerits += doubleHyphenDemerits;
            }
            double totalDemerits = demerits + activeNode.getDemerits();
            if (1 < Math.abs(activeNode.getAdjClass() - adjClass)) {
                totalDemerits += adjDemerits;
            }

            // keep this node if it is the best node up to now
            if (totalDemerits < minTotalDemeritsByClass[adjClass]) {
                minTotalDemeritsByClass[adjClass] = totalDemerits;
                Node node = new Node(totalDemerits, adjRatio, adjClass, lineNumber,
                                     startPos, endPos, lbPos, activeNode);
                node.setLineNumber(lineNumber);
                bestNodeByClass[adjClass] = node;
                if (totalDemerits < minTotalDemerits) {
                    minTotalDemerits = totalDemerits;
                }
            }
            
        } // end of loop over active nodes

        for (int i = 0; i < NUM_ADJ_CLASSES; ++i) {
            if (bestNodeByClass[i] != null &&
                    minTotalDemerits + ADJ_DEMERITS < bestNodeByClass[i].getDemerits()) {
                bestNodeByClass[i] = null;
            }
        }
        return bestNodeByClass;
    }
    
    private double calcAdjRatio(MinOptMax lineLength, float reqLineLength) {
        double adjRatio;
        float adjustment = reqLineLength - lineLength.getOpt();
        float stretch = lineLength.getStretch();
        float shrink = lineLength.getShrink();
        if (adjustment == 0) {
            adjRatio = 0;
        } else if (adjustment > 0) {
            // infinite stretch; adjustment ratio = 0
            if (lineLength.hasInfiniteStretch()) {
                adjRatio = 0;
            } else if (stretch == 0) {
                adjRatio = Double.POSITIVE_INFINITY;
            } else {
                adjRatio = (double) adjustment/(double) stretch;
            }
        } else {
            if (shrink == 0) {
                adjRatio = Double.NEGATIVE_INFINITY;
            } else {
                adjRatio = (double) adjustment/(double) shrink;
            }
        }
        return adjRatio;
    }
    
    private int calcAdjClass(double adjRatio) {
        if (adjRatio < -.5) {
            return 0;
        } else if (adjRatio < .5) {
            return 1;
        } else if (adjRatio < 1) {
            return 2;
        } else {
            return 3;
        }
    }

    private double calcDemerits(double adjRatio, int penValue) {
        double demerits;
        double absAdjRatio = Math.abs(adjRatio);
        double badness = 100 * absAdjRatio * absAdjRatio * absAdjRatio;
        if (penValue == EOPPenalty.FORCED_BREAK) {
            double d = linePenalty + badness;
            demerits = d * d;
        } else if (penValue >= 0) {
            double d = linePenalty + badness + penValue;
            demerits = d * d;
        } else {
            double d = linePenalty + badness;
            demerits = d * d - (penValue * penValue);
        }
        return demerits;
    }
    
    public List<Element> elements(){
    	return _myElements;
    }
    
}


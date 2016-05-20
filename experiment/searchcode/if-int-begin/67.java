package net.moraleboost.junsai.learner;

import java.io.IOException;

import net.moraleboost.junsai.DictionaryException;
import net.moraleboost.junsai.LearnerException;
import net.moraleboost.junsai.dictionary.AbstractFeatureIndex;

public abstract class AbstractLearnerTagger
{
    private LearnerTokenizer tokenizer;
    private AbstractFeatureIndex featureIndex;
    private LearnerNode[] beginNodeList;
    private LearnerNode[] endNodeList;
    
    public AbstractLearnerTagger(
            LearnerTokenizer tokenizer,
            AbstractFeatureIndex featureIndex)
    {
        this.tokenizer = tokenizer;
        this.featureIndex = featureIndex;
    }
    
    protected void initList(int len)
    {
        beginNodeList = new LearnerNode[len+2];
        endNodeList = new LearnerNode[len+2];
        
        endNodeList[0] = (LearnerNode)tokenizer.getBOSNode();
        endNodeList[0].sentenceLength = len;
        beginNodeList[len] = (LearnerNode)tokenizer.getEOSNode();
    }

    /**
     * ????????
     * 
     * @param str ????
     * @param begin ?????????(str???????????)
     * @param end ?????????(str???????????)
     * @param off lookup?????????(begin????????)
     * @return
     */
    protected LearnerNode lookup(CharSequence str, int begin, int end, int off)
    {
        if (beginNodeList[off] != null) {
            return beginNodeList[off];
        }
        
        LearnerNode m = (LearnerNode)tokenizer.lookup(str, begin+off, end);
        beginNodeList[off] = m;
        return m;
    }
    
    protected void connect(int pos, LearnerNode node)
    throws LearnerException, DictionaryException, IOException
    {
        LearnerPath path;
        
        for (LearnerNode rNode=node; rNode!=null; rNode=(LearnerNode)rNode.bnext) {
            for (LearnerNode lNode=endNodeList[pos]; lNode!=null; lNode=(LearnerNode)lNode.enext) {
                path = new LearnerPath();
                path.rnode = rNode;
                path.lnode = lNode;
                path.lnext = rNode.lpath;
                rNode.lpath = path;
                path.rnext = lNode.rpath;
                lNode.rpath = path;
                featureIndex.buildFeature(path);
                if (path.fvector == null) {
                    throw new LearnerException("fvector is NULL.");
                }
            }
            
            int x = rNode.rlength() + pos;
            rNode.enext = endNodeList[x];
            endNodeList[x] = rNode;
        }
    }
    
    protected void buildLattice(CharSequence str, int begin, int end)
    throws LearnerException, DictionaryException, IOException
    {
        int len = end - begin;
        
        for (int pos=0; pos<=len; ++pos) {
            if (endNodeList[pos] == null) {
                continue;
            }
            connect(pos, lookup(str, begin, end, pos));
        }
        
        if (endNodeList[len] == null) {
            beginNodeList[len] = lookup(str, begin, end, len);
            for (int pos=len; pos>=0; --pos) {
                if (endNodeList[pos] != null) {
                    connect(pos, beginNodeList[len]);
                    break;
                }
            }
        }
    }
    
    protected void viterbi(int len)
    {
        for (int pos=0; pos<=len; ++pos) {
            for (LearnerNode node=beginNodeList[pos]; node!=null; node=(LearnerNode)node.bnext) {
                double bestc = -1e37;
                LearnerNode best = null;
                featureIndex.calcCost(node);
                for (LearnerPath path=(LearnerPath)node.lpath; path!=null; path=(LearnerPath)path.lnext) {
                    featureIndex.calcCost(path);
                    double cost = path.cost + ((LearnerNode)path.lnode).cost;
                    if (cost > bestc) {
                        bestc = cost;
                        best = (LearnerNode)path.lnode;
                    }
                }
                
                node.prev = best;
                node.cost = bestc;
            }
        }
        
        LearnerNode node = beginNodeList[len]; // EOS
        for (LearnerNode prev; node.prev!=null;) {
            prev = (LearnerNode)node.prev;
            prev.next = node;
            node = prev;
        }
    }
    
    protected LearnerNode getNewNode()
    {
        return (LearnerNode)tokenizer.getNewNode();
    }
    
    protected LearnerNode getBeginNode(int pos)
    {
        return beginNodeList[pos];
    }
    
    protected LearnerNode getEndNode(int pos)
    {
        return endNodeList[pos];
    }
}


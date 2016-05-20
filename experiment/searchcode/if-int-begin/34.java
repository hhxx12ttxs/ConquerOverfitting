package net.moraleboost.junsai.tagger;

import java.util.Arrays;

import net.moraleboost.junsai.DictionaryException;
import net.moraleboost.junsai.Node;
import net.moraleboost.junsai.dictionary.CharProperty;
import net.moraleboost.junsai.dictionary.Dictionary;
import net.moraleboost.junsai.dictionary.Token;
import net.moraleboost.junsai.dictionary.CharProperty.CharInfo;
import net.moraleboost.junsai.doublearray.Trie;

public abstract class AbstractTokenizer
{
    private static class UnknownToken
    {
        public int base;
        public int size;
    }
    
    public static final int DEFAULT_MAX_GROUPING_SIZE = 24;
    public static final int DARESULTS_SIZE = 512;
    public static final String BOS_KEY = "BOS/EOS";

    private Dictionary unkDictionary;
    private Dictionary[] dictionaries;
    private CharProperty charProperty;
    private UnknownToken[] unkTokens;
    private CharInfo space;
    private String bosFeature;
    private String unkFeature;
    private int maxGroupingSize;
    private Trie.Result[] daResults;
    
    private int id;

    public AbstractTokenizer(
            Dictionary[] dictionaries,
            Dictionary unknownDictionary,
            CharProperty charProperty,
            String bosFeature,
            String unkFeature,
            int maxGroupingSize)
    throws DictionaryException
    {
        // double-array?common prefix search?????
        daResults = new Trie.Result[DARESULTS_SIZE];
        for (int i=0; i<DARESULTS_SIZE; ++i) {
            daResults[i] = new Trie.Result();
        }
        
        reset(dictionaries, unknownDictionary,
                charProperty, bosFeature, unkFeature, maxGroupingSize);
    }
    
    public String getBosFeature()
    {
        return bosFeature;
    }
    
    public String getUnkFeature()
    {
        return unkFeature;
    }
    
    public int getMaxGroupingSize()
    {
        return maxGroupingSize;
    }
    
    public void clear()
    {
        id = 0;
    }
    
    public void reset(
            Dictionary[] dictionaries,
            Dictionary unknownDictionary,
            CharProperty charProperty,
            String bosFeature,
            String unkFeature,
            int maxGroupingSize)
    throws DictionaryException
    {
        this.dictionaries = Arrays.copyOf(dictionaries, dictionaries.length);
        this.unkDictionary = unknownDictionary;
        this.charProperty = charProperty;
        
        unkTokens = new UnknownToken[charProperty.getSize()];
        Trie.Result r = new Trie.Result();
        for (int i=0; i<charProperty.getSize(); ++i) {
            String key = charProperty.name(i);
            if (!unkDictionary.exactMatchSearch(key, r)) {
                throw new DictionaryException(
                        "Can't find UNK category: " + key);
            }
            
            Token token = new Token();
            unkDictionary.token(r, 0, token);

            UnknownToken unkToken = new UnknownToken();
            unkToken.base = Dictionary.baseIndex(r);
            unkToken.size = Dictionary.numTokens(r);
            unkTokens[i] = unkToken;
        }
        
        space = new CharInfo();
        charProperty.getCharInfo(' ', space);
        
        this.bosFeature = bosFeature;
        this.unkFeature = unkFeature;
        this.maxGroupingSize = maxGroupingSize;
        
        if (bosFeature == null || bosFeature.isEmpty()) {
            throw new DictionaryException("BOS feature is not specified.");
        }
        
        if (maxGroupingSize <= 0) {
            this.maxGroupingSize = DEFAULT_MAX_GROUPING_SIZE;
        }
    }
    
    /**
     * str?begin???????????????Node??????
     * ????????????????null????
     * 
     * @param str ?????????????????
     * @param begin ????????????
     * @param end ????
     * @return ?????????????Node??????????????????null?
     */
    public Node lookup(CharSequence str, int begin, int end)
    {
        // ???
        //   begin: str??????????
        //   begin2: begin????????????????????
        //   begin3: group, length???????????????????
        
        CharInfo cinfo = new CharInfo();
        AbstractNode node = null;
        
        // ????????????
        int begin2 = charProperty.seekToOtherType(str, begin, end, space, cinfo);
        if (begin2 >= end) {
            // ???????????????????
            return null;
        }
        
        // ???????begin2??65535???????
        end = ((end - begin2) > 65535) ? (begin2 + 65535) : end;
        
        // ?????????????
        // node??bnext??????????????????
        for (Dictionary d: dictionaries) {
            int n = d.commonPrefixSearch(
                    str, begin2, (end-begin2), daResults, DARESULTS_SIZE);
            for (int i=0; i<n; ++i) {
                int base = Dictionary.baseIndex(daResults[i]);
                int size = Dictionary.numTokens(daResults[i]);
                
                for (int j=0; j<size; ++j) {
                    AbstractNode newNode = getNewNode();
                    readNodeInfo(d, base+j, newNode);
                    setNodeMembers(begin, begin2, begin2 + daResults[i].length,
                            str.subSequence(begin2, begin2 + daResults[i].length).toString(),
                            Node.STAT_NOR_NODE, cinfo.getDefaultType(), node, newNode);
                    node = newNode;
                }
            }
        }
        
        // ?????????????????invoke??????????????
        // ???????
        if (node != null && !cinfo.getInvoke()) {
            return node;
        }
        
        // ?????????????????????????
        // cinfo?invoke??????????
        // ????begin2 < end ?????????
        int begin3 = begin2 + 1; // begin3 may be equal to end
        int groupBegin3 = -1;
        
        // ?????????????????????????????
        // ????????????????
        if (cinfo.getGroup()) {
            int tmp = begin3;
            CharInfo fail = new CharInfo();
            begin3 = charProperty.seekToOtherType(str, begin3, end, cinfo, fail);
            // ?????????maxGroupingSize????????????
            // ????????
            if ((begin3 - tmp) <= maxGroupingSize) {
                node = addUnknownNode(cinfo, str, begin, begin2, begin3, node);
            }
            groupBegin3 = begin3;
            begin3 = tmp;
        }
        
        // cinfo.length?1????????
        // ?????????????????
        // 1??????????????????
        //
        // str[begin2..begin2+1]
        // str[begin2..begin2+2]
        // ...
        // str[begin2..begin2+cinfo.length]
        //
        CharInfo extension = new CharInfo();
        for (int i=1; i<=cinfo.getLength(); ++i) {
            if (begin3 > end) {
                break;
            }
            if (begin3 != groupBegin3) {
                node = addUnknownNode(cinfo, str, begin, begin2, begin3, node);
            } else {
                // ??????????????????????
            }
            
            if (begin3 < end) {
                charProperty.getCharInfo(str.charAt(begin3), extension);
                if (!cinfo.isKindOf(extension)) {
                    break;
                }
            }
            ++begin3;
        }
        
        return node;
    }
    
    private AbstractNode addUnknownNode(
            CharInfo cinfo,
            CharSequence sentence,
            int rbegin, // ????????????
            int begin,  // ???????????
            int end,    // ????
            AbstractNode node)
    {
        UnknownToken unkToken = unkTokens[cinfo.getDefaultType()];
        for (int i=0; i<unkToken.size; ++i) {
            AbstractNode newNode = getNewNode();
            readNodeInfo(unkDictionary, unkToken.base+i, newNode);
            setNodeMembers(rbegin, begin, end,
                    sentence.subSequence(begin, end).toString(),
                    Node.STAT_UNK_NODE, cinfo.getDefaultType(), node, newNode);

            newNode.id = id - 1;
            if (unkFeature != null && !unkFeature.isEmpty()) {
                newNode.feature = unkFeature;
            }
            
            node = newNode;
        }
        
        return node;
    }
    
    public AbstractNode getNewNode()
    {
        AbstractNode node = createNewNode();
        node.id = id; ++id;
        return node;
    }
    
    public AbstractNode getBOSNode()
    {
        AbstractNode n = getNewNode();
        
        n.surface = BOS_KEY;
        n.feature = bosFeature;
        n.isbest = true;
        n.id = id - 1;
        n.stat = Node.STAT_BOS_NODE;
        
        return n;
    }
    
    public AbstractNode getEOSNode()
    {
        AbstractNode n = getBOSNode();
        
        n.stat = Node.STAT_EOS_NODE;
        
        return n;
    }
    
    protected void setNodeMembers(int rbegin, int begin, int end,
            String surface, short stat, int charType, AbstractNode bnext,
            AbstractNode n)
    {
        n.rbegin = rbegin;
        n.begin = begin;
        n.end = end;
        n.surface = surface;
        n.stat = stat;
        n.charType = charType;
        n.bnext = bnext;
    }

    protected abstract void readNodeInfo(Dictionary d, int idx, AbstractNode n);
    protected abstract AbstractNode createNewNode();
}


package net.moraleboost.junsai.tagger;

import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.moraleboost.junsai.Node;
import net.moraleboost.junsai.util.CsvUtil;

import static java.text.CharacterIterator.DONE;

/**
 * Node?????????????
 * ?????????????????????
 * 
 * @author taketa
 *
 */
public class NodeFormatter
{
    private static interface Format
    {
        public boolean format(
                CharSequence sentence, int offset, int len, TaggerNode node, Appendable out)
        throws IOException;
    }
    
    private class FormatLattice implements Format
    {
        public boolean format(CharSequence sentence,
                int offset, int len, TaggerNode node, Appendable out)
        throws IOException
        {
            // node == bosNode
            for (TaggerNode n = (TaggerNode)node.next; n.next != null; n = (TaggerNode)n.next) {
                out.append(n.surface);
                out.append("\t");
                if (n.feature != null) {
                    out.append(n.feature);
                }
                out.append("\n");
            }
            out.append("EOS\n");
            
            return true;
        }
    }
    
    private class FormatWakati implements Format
    {
        public boolean format(CharSequence sentence,
                int offset, int len, TaggerNode node, Appendable out)
        throws IOException
        {
            for (TaggerNode n = (TaggerNode)node.next; n.next != null; n = (TaggerNode)n.next) {
                out.append(n.surface);
                out.append(' ');
            }
            out.append('\n');
            return true;
        }
    }
    
    private class FormatNone implements Format
    {
        public boolean format(CharSequence sentence,
                int offset, int len, TaggerNode node, Appendable out)
        {
            return true;
        }
    }
    
    private class FormatDump implements Format
    {
        /*
         * ID SURFACE FEATURE
         * BEGIN END
         * RCATTR LCATTR
         * POSID CHARTYPE STAT ISBEST
         * ALPHA BETA PROB COST
         * (PATH_LNODE_ID:PATH_COST:PATH_PROB)*
         */
        public boolean format(CharSequence sentence,
                int offset, int len, TaggerNode node, Appendable out)
        throws IOException
        {
            for (TaggerNode n = node; n != null; n = (TaggerNode)n.next) {
                out.append(Integer.toString(n.id));
                out.append(' ');
                if (n.stat == Node.STAT_BOS_NODE) {
                    out.append("BOS");
                } else if (n.stat == Node.STAT_EOS_NODE) {
                    out.append("EOS");
                } else {
                    out.append(n.surface);
                }
                
                out.append(' ').append(n.feature);
                out.append(' ').append(Integer.toString(n.begin));
                out.append(' ').append(Integer.toString(n.end));
                out.append(' ').append(Integer.toString(n.rcAttr));
                out.append(' ').append(Integer.toString(n.lcAttr));
                out.append(' ').append(Integer.toString(n.posid));
                out.append(' ').append(Integer.toString(n.charType));
                out.append(' ').append(Integer.toString(n.stat));
                out.append(' ').append(n.isbest ? '1' : '0');
                out.append(' ').append(Float.toString(n.alpha));
                out.append(' ').append(Float.toString(n.beta));
                out.append(' ').append(Float.toString(n.prob));
                out.append(' ').append(Integer.toString(n.cost));
                
                for (TaggerPath path = (TaggerPath)n.lpath; path != null; path = (TaggerPath)path.lnext) {
                    out.append(' ').append(Integer.toString(path.lnode.id));
                    out.append(':').append(Integer.toString(path.cost));
                    out.append(':').append(Float.toString(path.prob));
                }
                
                out.append('\n');
            }
            
            return true;
        }
    }
    
    private class FormatEM implements Format
    {
        private static final float MIN_PROB = 0.0001f;
        
        public boolean format(CharSequence sentence,
                int offset, int len, TaggerNode node, Appendable out)
        throws IOException
        {
            for (TaggerNode n = node; n != null; n = (TaggerNode)n.next) {
                if (n.prob >= MIN_PROB) {
                    out.append("U\t");
                    if (n.stat == Node.STAT_BOS_NODE) {
                        out.append("BOS");
                    } else if (n.stat == Node.STAT_EOS_NODE) {
                        out.append("EOS");
                    } else {
                        out.append(n.surface);
                    }
                    out.append('\t').append(n.feature);
                    out.append('\t').append(Float.toString(n.prob));
                    out.append('\n');
                }
                
                for (TaggerPath path = (TaggerPath)n.lpath; path != null; path = (TaggerPath)path.lnext) {
                    if (path.prob >= MIN_PROB) {
                        out.append("B\t");
                        out.append(path.lnode.feature).append('\t');
                        out.append(n.feature).append('\t');
                        out.append(Float.toString(path.prob)).append('\n');
                    }
                }
            }
            out.append("EOS\n");

            return true;
        }
    }
    
    private class FormatUser implements Format
    {
        public boolean format(CharSequence sentence,
                int offset, int len, TaggerNode node, Appendable out)
        throws IOException
        {
            // BOS
            if (!NodeFormatter.formatNode(bosFormat, sentence, offset, len, node, out)) {
                return false;
            }
            
            // ??
            String fmt;
            TaggerNode n = (TaggerNode)node.next;
            for (; n.next != null; n = (TaggerNode)n.next) {
                
                if (n.stat == Node.STAT_UNK_NODE) {
                    fmt = unkFormat;
                } else {
                    fmt = nodeFormat;
                }
                
                if (!NodeFormatter.formatNode(fmt, sentence, offset, len, n, out)) {
                    return false;
                }
            }
            
            // EOS
            return NodeFormatter.formatNode(eosFormat, sentence, offset, len, n, out);
        }
    }
    
    public static final int OUTPUT_FORMAT_LATTICE = 0;
    public static final int OUTPUT_FORMAT_WAKATI = 1;
    public static final int OUTPUT_FORMAT_NONE = 2;
    public static final int OUTPUT_FORMAT_DUMP = 3;
    public static final int OUTPUT_FORMAT_EM = 4;
    public static final int OUTPUT_FORMAT_USER = 5;

    private static final Log LOGGER = LogFactory.getLog(NodeFormatter.class);
    
    private String nodeFormat;
    private String bosFormat;
    private String eosFormat;
    private String unkFormat;
    private String eonFormat;
    
    private Format formatFunc;
    
    public NodeFormatter(
            int outputFormat,
            String nodeFormat,
            String unkFormat,
            String bosFormat,
            String eosFormat,
            String eonFormat)
    {
        this.nodeFormat = nodeFormat;
        this.unkFormat = unkFormat;
        this.bosFormat = bosFormat;
        this.eosFormat = eosFormat;
        this.eonFormat = eonFormat;

        setOutputFormat(outputFormat);
    }
    
    public int getOutputFormat()
    {
        if (formatFunc instanceof FormatLattice) {
            return OUTPUT_FORMAT_LATTICE;
        } else if (formatFunc instanceof FormatWakati) {
            return OUTPUT_FORMAT_WAKATI;
        } else if (formatFunc instanceof FormatNone) {
            return OUTPUT_FORMAT_NONE;
        } else if (formatFunc instanceof FormatDump) {
            return OUTPUT_FORMAT_DUMP;
        } else if (formatFunc instanceof FormatEM) {
            return OUTPUT_FORMAT_EM;
        } else {
            return OUTPUT_FORMAT_USER;
        }
    }
    
    public void setOutputFormat(int outputFormat)
    {
        switch (outputFormat) {
        case OUTPUT_FORMAT_LATTICE:
            this.formatFunc = new FormatLattice();
            break;
        case OUTPUT_FORMAT_WAKATI:
            this.formatFunc = new FormatWakati();
            break;
        case OUTPUT_FORMAT_NONE:
            this.formatFunc = new FormatNone();
            break;
        case OUTPUT_FORMAT_DUMP:
            this.formatFunc = new FormatDump();
            break;
        case OUTPUT_FORMAT_EM:
            this.formatFunc = new FormatEM();
            break;
        default:
            this.formatFunc = new FormatUser();
            break;
        }
    }
    
    public String getNodeFormat()
    {
        return nodeFormat;
    }

    public void setNodeFormat(String nodeFormat)
    {
        this.nodeFormat = nodeFormat;
    }

    public String getBosFormat()
    {
        return bosFormat;
    }

    public void setBosFormat(String bosFormat)
    {
        this.bosFormat = bosFormat;
    }

    public String getEosFormat()
    {
        return eosFormat;
    }

    public void setEosFormat(String eosFormat)
    {
        this.eosFormat = eosFormat;
    }

    public String getUnkFormat()
    {
        return unkFormat;
    }

    public void setUnkFormat(String unkFormat)
    {
        this.unkFormat = unkFormat;
    }

    public String getEonFormat()
    {
        return eonFormat;
    }

    public void setEonFormat(String eonFormat)
    {
        this.eonFormat = eonFormat;
    }

    // node?BOS??????????????????????????
    public boolean format(
            CharSequence sentence, int offset, int len, TaggerNode node, Appendable out)
    throws IOException
    {
        return formatFunc.format(sentence, offset, len, node, out);
    }
    
    // ?????????????????
    public boolean formatNode(
            CharSequence sentence, int offset, int len, TaggerNode node, Appendable out)
    throws IOException
    {
        switch (node.stat) {
        case Node.STAT_BOS_NODE:
            return NodeFormatter.formatNode(
                    bosFormat, sentence, offset, len, node, out);
        case Node.STAT_EOS_NODE:
            return NodeFormatter.formatNode(
                    eosFormat, sentence, offset, len, node, out);
        case Node.STAT_EON_NODE:
            return NodeFormatter.formatNode(
                    eonFormat, sentence, offset, len, node, out);
        case Node.STAT_NOR_NODE:
            return NodeFormatter.formatNode(
                    nodeFormat, sentence, offset, len, node, out);
        case Node.STAT_UNK_NODE:
            return NodeFormatter.formatNode(
                    unkFormat, sentence, offset, len, node, out);
        }
        
        return false;
    }
    
    // ????????format?????????
    public static boolean formatNode(
            String format,
            CharSequence sentence, int offset, int len, TaggerNode node, Appendable out)
    throws IOException
    {
        if (format == null || format.isEmpty()) {
            // format?null???????????????????
            return true;
        }
        
        StringCharacterIterator iter = new StringCharacterIterator(format);
        char c;
        
        for (c=iter.first(); c!=DONE; c=iter.next()) {
            switch (c) {
            case '\\': // escape sequences
                c = iter.next();
                if (c == DONE) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Format ends with an escape character.");
                    }
                    return false;
                } else {
                    out.append(getEscapedChar(c));
                }
                break;
                
            case '%': // macros
                if (!formatMacro(iter, sentence, offset, len, node, out)) {
                    return false;
                }
                break;
                
            default:
                out.append(c);
                break;
            }
        }
        
        return true;
    }
    
    private static boolean formatMacro(
            CharacterIterator iter,
            CharSequence sentence, int offset, int len, TaggerNode node, Appendable out)
    throws IOException
    {
        String[] featureCol = null;
        
        char c = iter.next();
        if (c == DONE) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Immature macro.");
            }
            return false;
        }
        
        switch (c) {
        case 'S': // ???????
            out.append(sentence, offset, offset+len);
            break;
        case 'L': // ????????
            out.append(Integer.toString(len));
            break;
        case 'm': // ???
            out.append(node.surface);
            break;
        case 'M': // ?????????????
            out.append(sentence, node.rbegin, node.end);
            break;
        case 'h': // ??ID
            out.append(Integer.toString(node.posid));
            break;
        case '%': // ?????
            out.append('%');
            break;
        case 'c': // ???
            out.append(Short.toString(node.wcost));
            break;
        case 'H': // ??
            out.append(node.feature);
            break;
        case 't': // charType
            out.append(Integer.toString(node.charType));
            break;
        case 's': // stat
            out.append(Short.toString(node.stat));
            break;
        case 'P': // ??
            out.append(Float.toString(node.prob));
            break;
        case 'p': // ?????????????
            if (!formatMacro2(iter, sentence, offset, len, node, out)) {
                return false;
            }
            break;
        case 'F': // fall through
        case 'f':
            if (node.feature == null || node.feature.isEmpty()) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("No feature information available.");
                }
                return false;
            }
            if (featureCol == null) {
                featureCol = CsvUtil.tokenize(node.feature, 64);
            }
            if (!formatFeature(iter, sentence, offset, len, node, featureCol, out)) {
                return false;
            }
            break;
        default:
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unknown meta char: " + c);
            }
            return false;
        }
        
        return true;
    }
    
    // format %p macro
    private static boolean formatMacro2(
            CharacterIterator iter,
            CharSequence sentence, int offset, int len, TaggerNode node, Appendable out)
    throws IOException
    {
        char c = iter.next();
        if (c == DONE) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Immature %p macro.");
            }
            return false;
        }
        
        switch (c) {
        case 'i': // ???ID
            out.append(Integer.toString(node.id));
            break;
        case 'S': // ?????????
            out.append(sentence, node.rbegin, node.begin);
            break;
        case 's': // ?????????????????
            out.append(Integer.toString(node.begin));
            break;
        case 'e': // ???????
            out.append(Integer.toString(node.end));
            break;
        case 'C': // ?????
            out.append(Integer.toString(node.cost - ((TaggerNode)node.prev).cost - node.wcost));
            break;
        case 'w': // ?????
            out.append(Integer.toString(node.wcost));
            break;
        case 'c': // ??????
            out.append(Integer.toString(node.cost));
            break;
        case 'n': // ??????
            out.append(Integer.toString(node.cost - ((TaggerNode)node.prev).cost));
            break;
        case 'b': // ???????'*'?????????' '??????
            out.append(node.isbest ? '*' : ' ');
            break;
        case 'P': // ??
            out.append(Float.toString(node.prob));
            break;
        case 'A': // ?????????????????????????
            out.append(Float.toString(node.alpha));
            break;
        case 'B': // ????????????????????????
            out.append(Float.toString(node.beta));
            break;
        case 'l': // ????
            out.append(Integer.toString(node.length()));
            break;
        case 'L': // ????????????
            out.append(Integer.toString(node.rlength()));
            break;
        case 'h': { // ????lcAttr, rcAttr
            char cc = iter.next();
            if (cc == DONE) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Immature %ph macro.");
                }
                return false;
            }
            switch (cc) {
            case 'l': // lcAttr
                out.append(Integer.toString(node.lcAttr));
                break;
            case 'r': // rcAttr
                out.append(Integer.toString(node.rcAttr));
                break;
            default:
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Unknown meta char: p" + cc);
                }
                return false;
            }
            break;
        }
        case 'p': // ????
            if (!formatPath(iter, sentence, offset, len, node, out)) {
                return false;
            }
            break;
        default:
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unknown meta char: p" + c);
            }
            return false;
        }
        
        return true;
    }
    
    // format %pp macro
    private static boolean formatPath(
            CharacterIterator iter,
            CharSequence sentence, int offset, int len, TaggerNode node, Appendable out)
    throws IOException
    {
        char mode = iter.next();
        char sep = iter.next();
        if (mode == DONE || sep == DONE) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Immature %pp macro.");
            }
            return false;
        }
        
        if (sep == '\\') {
            sep = iter.next();
            if (sep == DONE) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Immature escape sequence.");
                }
                return false;
            }
            sep = getEscapedChar(sep);
        }
        
        if (node.lpath == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("No path information. Use -l option.");
            }
            return false;
        }
        
        for (TaggerPath path = (TaggerPath)node.lpath; path != null; path = (TaggerPath)path.lnext) {
            if (path != node.lpath) {
                out.append(sep);
            }
            switch (mode) {
            case 'i':
                out.append(Integer.toString(path.lnode.id));
                break;
            case 'c':
                out.append(Integer.toString(path.cost));
                break;
            case 'P':
                out.append(Float.toString(path.prob));
                break;
            default:
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Unknown meta char: pp" + mode);
                }
                return false;
            }
        }
        
        return true;
    }
    
    // %F or %f
    // %F?????????????????????????????
    // %f???????????????
    // (?)
    // %F\t[1]
    // %f[2,3,4]
    private static boolean formatFeature(
            CharacterIterator iter,
            CharSequence sentence, int offset, int len, TaggerNode node, String[] col, Appendable out)
    throws IOException
    {
        char separator = '\t';
        char c;
        
        if (iter.current() == 'F') {
            // ????????
            c = iter.next();
            if (c == DONE) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Immature macro: %F");
                }
                return false;
            }
            if (c == '\\') {
                c = iter.next();
                if (c == DONE) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Immature escape sequence.");
                    }
                }
                separator = getEscapedChar(c);
            } else {
                separator = c;
            }
        }
        
        if (iter.next() != '[') {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Can't find [.");
            }
            return false;
        }
        
        int n = 0;
        boolean sep = false;
        boolean isfil = false;
        
        main:
        while ((c=iter.next()) != DONE) {
            switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                n = 10 * n + (int)(c - '0');
                break;
            case ',':
            case ']':
                if (n >= col.length) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Index out of range.");
                    }
                    return false;
                }
                isfil = !col[n].startsWith("*");
                if (isfil) {
                    if (sep) {
                        out.append(separator);
                    }
                    out.append(col[n]);
                }
                if (c == ']') {
                    break main;
                }
                sep = isfil;
                n = 0;
                break;
            default:
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Can't find ].");
                }
                return false;
            }
        }
        
        return true;
    }
    
    public static char getEscapedChar(char c)
    {
        switch (c) {
        case '0':
            return '\0';
        case 'b':
            return '\b';
        case 't':
            return '\t';
        case 'n':
            return '\n';
        case 'f':
            return '\f';
        case 'r':
            return '\r';
        case 's':
            return ' ';
        case '\\':
            return '\\';
        default:
            break;
        // \v, \a?Java???????????????
        // case 'v': return '\v';
        // case 'a': return '\a';
        }
        
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Unknown escape sequence found: " + "\\" + c);
        }
        return '\0';
    }
}


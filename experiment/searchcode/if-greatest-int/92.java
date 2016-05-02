// $ANTLR 3.0.1 etc/XQFT.g 2008-04-28 20:25:50

    package no.ntnu.xqft.parse;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

public class XQFTParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AST_MODULE", "AST_FLWOR", "AST_FORCLAUSE", "AST_LETCLAUSE", "AST_ORDERBYCLAUSE", "AST_WHERECLAUSE", "AST_RETURNCLAUSE", "AST_QUANTIFIEDEXPR", "AST_TYPESWITCHEXPR", "AST_CASECLAUSE", "AST_IFEXPR", "AST_PATHEXPR_DBL", "AST_PATHEXPR_SGL", "AST_PATHEXPR_REL", "AST_STEPEXPR", "AST_RELATIVEPATHEXPR", "AST_FTSELECTION", "AST_FTPOSFILTER", "AST_FUNCTIONCALL", "AST_FUNCTIONDECL", "AST_PREDICATE", "AST_DIRELEMCONSTRUCTOR", "AST_DIRELEMCONTENT", "AST_ENCLOSEDEXPR", "SYNTH_PR_PATHEXPR", "SYNTH_PR_LVL", "XQUERY", "VERSION", "StringLiteral", "ENCODING", "SEMICOLONSi", "MODULE", "NAMESPACE", "EQSi", "COMMASi", "DECLARE", "DEFAULT", "ELEMENT", "FUNCTION", "COLLATION", "ORDER", "EMPTY", "GREATEST", "LEAST", "BOUNDARYSPACE", "PRESERVE", "STRIP", "BASE_URI", "CONSTRUCTION", "ORDERING", "ORDERED", "UNORDERED", "COPY_NAMESPACES", "NOPRESERVE", "INHERIT", "NOINHERIT", "IMPORT", "SCHEMA", "AT", "VARIABLE", "DOLLARSi", "ASSIGNSi", "EXTERNAL", "QName", "AS", "LPARSi", "RPARSi", "LBRACESi", "RBRACSi", "OPTION", "FTOPTION", "EMPTY_SEQUENCE", "ITEM", "DOCUMENT_NODE", "QUESTIONSi", "STARSi", "SCHEMA_ELEMENT", "ATTRIBUTE", "SCHEMA_ATTRIBUTE", "PROCESSING_INSTRUCTION", "COMMENT", "TEXT", "NODE", "PLUSSi", "RETURN", "FOR", "IN", "SCORE", "LET", "WHERE", "BY", "STABLE", "ASCENDING", "DESCENDING", "SOME", "EVERY", "SATISFIES", "TYPESWITCH", "CASE", "IF", "THEN", "ELSE", "OR", "AND", "FTCONTAINS", "TO", "MINUSSi", "DIV", "IDIV", "MOD", "UNION", "PIPESi", "INTERSECT", "EXCEPT", "INSTANCE", "OF", "TREAT", "CASTABLE", "CAST", "WITHOUT", "CONTENT", "EQ", "NE", "LT", "LE", "GT", "GE", "NEQSi", "LTSi", "LTOREQSi", "GTSi", "GTOREQSi", "IS", "NODEBEFORESi", "NODEAFTERSi", "WEIGHT", "FTOR", "FTAND", "NOT", "FTNOT", "IntegerLiteral", "DecimalLiteral", "DoubleLiteral", "ANY", "WORD", "ALL", "WORDS", "PHRASE", "OCCURS", "TIMES", "EXACTLY", "MOST", "FROM", "LPRAGSi", "PragmaContents", "RPRAGSi", "WINDOW", "SENTENCES", "PARAGRAPHS", "DISTANCE", "SAME", "DIFFERENT", "SENTENCE", "PARAGRAPH", "START", "END", "ENTIRE", "WITH", "WILDCARDS", "STEMMING", "THESAURUS", "STOP", "LANGUAGE", "INSENSITIVE", "SENSITIVE", "LOWERCASE", "UPPERCASE", "DIACRITICS", "RELATIONSHIP", "LEVELS", "VALIDATE", "LAX", "STRICT", "SLASHSi", "DBLSLASHSi", "DOTSi", "PARENT", "ANCESTOR", "PRECEDING_SIBLING", "PRECEDING", "ANCESTOR_OR_SELF", "DBLCOLONSi", "COLONSi", "DOTDOTSi", "CHILD", "DESCENDANT", "SELF", "DESCENDANT_OR_SELF", "FOLLOWING_SIBLING", "FOLLOWING", "ATSi", "LBRACKSi", "RBRACKSi", "RSELFTERMSi", "LENDTAGSi", "QUOTSi", "QuotAttributeContent", "APOSSi", "AposAttributeContent", "ElementContent", "LCDATASi", "CDataContents", "RCDATASi", "LCOMMENTSi", "DirCommentContent", "RCOMMENTSi", "LPISi", "PiTarget", "DirPiContents", "RPISi", "DOCUMENT", "NCName", "CDataSectionLEX", "DirPIConstructor", "DirCommentConstLEX", "PragmaLEX", "Comment", "NumberLEX", "LexSigns", "S", "LexLiterals", "TOKENSWITCH", "PredefinedEntityRef", "CharRef", "NotChar", "AMPERSi", "QuotAttrContentChar", "AposAttrContentChar", "ElementContentChar", "CREFDECSi", "CREFHEXSi", "Name", "Letter", "UNDERSCORESi", "NameChar", "Digit", "CombiningChar", "Extender", "SHARPSi", "LXQCOMMENTSi", "RXQCOMMENTSi", "Digits", "NCNameStartChar", "NCNameChar", "EXCLSi", "BACKSLASHSi", "ExtraChar", "BaseChar", "Ideographic", "CleanChar", "Char"
    };
    public static final int NotChar=238;
    public static final int LENDTAGSi=208;
    public static final int QUOTSi=209;
    public static final int END=169;
    public static final int NOINHERIT=59;
    public static final int LBRACESi=71;
    public static final int INSTANCE=118;
    public static final int CONTENT=124;
    public static final int STOP=175;
    public static final int LAX=185;
    public static final int IMPORT=60;
    public static final int PIPESi=115;
    public static final int APOSSi=211;
    public static final int AposAttrContentChar=241;
    public static final int DBLCOLONSi=195;
    public static final int Letter=246;
    public static final int ORDER=44;
    public static final int CASTABLE=121;
    public static final int DIACRITICS=181;
    public static final int RCOMMENTSi=219;
    public static final int TYPESWITCH=101;
    public static final int MODULE=35;
    public static final int VERSION=31;
    public static final int AST_PATHEXPR_SGL=16;
    public static final int Ideographic=262;
    public static final int AST_STEPEXPR=18;
    public static final int DBLSLASHSi=188;
    public static final int EXACTLY=154;
    public static final int FTNOT=143;
    public static final int DECLARE=39;
    public static final int GTSi=134;
    public static final int TOKENSWITCH=235;
    public static final int ElementContentChar=242;
    public static final int OCCURS=152;
    public static final int UNION=114;
    public static final int STRIP=50;
    public static final int NameChar=248;
    public static final int PLUSSi=87;
    public static final int AST_WHERECLAUSE=9;
    public static final int QuotAttrContentChar=240;
    public static final int SAME=164;
    public static final int QUESTIONSi=78;
    public static final int AST_IFEXPR=14;
    public static final int FTCONTAINS=108;
    public static final int DESCENDING=97;
    public static final int FTOPTION=74;
    public static final int AST_ORDERBYCLAUSE=8;
    public static final int DOCUMENT_NODE=77;
    public static final int ANCESTOR_OR_SELF=194;
    public static final int LCDATASi=214;
    public static final int ANY=147;
    public static final int RELATIONSHIP=182;
    public static final int GE=130;
    public static final int CREFHEXSi=244;
    public static final int AST_MODULE=4;
    public static final int GTOREQSi=135;
    public static final int ELSE=105;
    public static final int WORD=148;
    public static final int SENSITIVE=178;
    public static final int SELF=200;
    public static final int DISTANCE=163;
    public static final int ExtraChar=260;
    public static final int LPISi=220;
    public static final int CREFDECSi=243;
    public static final int NODEBEFORESi=137;
    public static final int TEXT=85;
    public static final int CombiningChar=250;
    public static final int PARENT=190;
    public static final int NCName=225;
    public static final int SCORE=91;
    public static final int FTOR=140;
    public static final int AST_ENCLOSEDEXPR=27;
    public static final int PragmaLEX=229;
    public static final int Digit=249;
    public static final int EMPTY_SEQUENCE=75;
    public static final int EXTERNAL=66;
    public static final int UNDERSCORESi=247;
    public static final int RCDATASi=216;
    public static final int DOLLARSi=64;
    public static final int CAST=122;
    public static final int AST_FUNCTIONCALL=22;
    public static final int MOD=113;
    public static final int EXCEPT=117;
    public static final int NOPRESERVE=57;
    public static final int OR=106;
    public static final int S=233;
    public static final int RPARSi=70;
    public static final int BY=94;
    public static final int COLONSi=196;
    public static final int SCHEMA_ELEMENT=80;
    public static final int SLASHSi=187;
    public static final int WEIGHT=139;
    public static final int AST_RETURNCLAUSE=10;
    public static final int PRECEDING_SIBLING=192;
    public static final int UPPERCASE=180;
    public static final int AST_LETCLAUSE=7;
    public static final int SEMICOLONSi=34;
    public static final int FROM=156;
    public static final int STEMMING=173;
    public static final int TIMES=153;
    public static final int EMPTY=45;
    public static final int ASCENDING=96;
    public static final int CleanChar=263;
    public static final int IntegerLiteral=144;
    public static final int SENTENCES=161;
    public static final int NE=126;
    public static final int AposAttributeContent=212;
    public static final int COMMENT=84;
    public static final int MINUSSi=110;
    public static final int NCNameChar=257;
    public static final int ENTIRE=170;
    public static final int WITH=171;
    public static final int IN=90;
    public static final int AST_DIRELEMCONTENT=26;
    public static final int SOME=98;
    public static final int MOST=155;
    public static final int RETURN=88;
    public static final int AST_FTSELECTION=20;
    public static final int LET=92;
    public static final int IF=103;
    public static final int QName=67;
    public static final int AST_PATHEXPR_DBL=15;
    public static final int Extender=251;
    public static final int NODE=86;
    public static final int FOR=89;
    public static final int PRESERVE=49;
    public static final int DEFAULT=40;
    public static final int AST_FTPOSFILTER=21;
    public static final int DirCommentContent=218;
    public static final int LEVELS=183;
    public static final int LPRAGSi=157;
    public static final int COMMASi=38;
    public static final int ATTRIBUTE=81;
    public static final int CHILD=198;
    public static final int Digits=255;
    public static final int NOT=142;
    public static final int OPTION=73;
    public static final int ELEMENT=41;
    public static final int Comment=230;
    public static final int INSENSITIVE=177;
    public static final int DOCUMENT=224;
    public static final int AS=68;
    public static final int ENCODING=33;
    public static final int BOUNDARYSPACE=48;
    public static final int SYNTH_PR_LVL=29;
    public static final int TREAT=120;
    public static final int NAMESPACE=36;
    public static final int LXQCOMMENTSi=253;
    public static final int LEAST=47;
    public static final int THEN=104;
    public static final int WORDS=150;
    public static final int AST_PATHEXPR_REL=17;
    public static final int PiTarget=221;
    public static final int AST_RELATIVEPATHEXPR=19;
    public static final int CDataContents=215;
    public static final int PARAGRAPHS=162;
    public static final int NCNameStartChar=256;
    public static final int QuotAttributeContent=210;
    public static final int BASE_URI=51;
    public static final int AND=107;
    public static final int TO=109;
    public static final int AST_QUANTIFIEDEXPR=11;
    public static final int FUNCTION=42;
    public static final int LexSigns=232;
    public static final int NEQSi=131;
    public static final int LANGUAGE=176;
    public static final int LE=128;
    public static final int LOWERCASE=179;
    public static final int BACKSLASHSi=259;
    public static final int SCHEMA=61;
    public static final int CONSTRUCTION=52;
    public static final int STARSi=79;
    public static final int RPRAGSi=159;
    public static final int WILDCARDS=172;
    public static final int DOTDOTSi=197;
    public static final int DIFFERENT=165;
    public static final int LTOREQSi=133;
    public static final int AST_TYPESWITCHEXPR=12;
    public static final int AT=62;
    public static final int INTERSECT=116;
    public static final int AST_FORCLAUSE=6;
    public static final int GREATEST=46;
    public static final int DOTSi=189;
    public static final int BaseChar=261;
    public static final int Char=264;
    public static final int EQ=125;
    public static final int AST_FLWOR=5;
    public static final int LT=127;
    public static final int OF=119;
    public static final int AST_FUNCTIONDECL=23;
    public static final int WINDOW=160;
    public static final int Name=245;
    public static final int FOLLOWING=203;
    public static final int CASE=102;
    public static final int RXQCOMMENTSi=254;
    public static final int DESCENDANT_OR_SELF=201;
    public static final int CDataSectionLEX=226;
    public static final int THESAURUS=174;
    public static final int RSELFTERMSi=207;
    public static final int FTAND=141;
    public static final int DecimalLiteral=145;
    public static final int PragmaContents=158;
    public static final int EQSi=37;
    public static final int DIV=111;
    public static final int PARAGRAPH=167;
    public static final int ALL=149;
    public static final int EXCLSi=258;
    public static final int DirPIConstructor=227;
    public static final int WHERE=93;
    public static final int WITHOUT=123;
    public static final int SCHEMA_ATTRIBUTE=82;
    public static final int EVERY=99;
    public static final int XQUERY=30;
    public static final int IDIV=112;
    public static final int SATISFIES=100;
    public static final int ATSi=204;
    public static final int DESCENDANT=199;
    public static final int STRICT=186;
    public static final int RBRACKSi=206;
    public static final int FOLLOWING_SIBLING=202;
    public static final int SYNTH_PR_PATHEXPR=28;
    public static final int STABLE=95;
    public static final int DoubleLiteral=146;
    public static final int START=168;
    public static final int AMPERSi=239;
    public static final int VALIDATE=184;
    public static final int PRECEDING=193;
    public static final int COPY_NAMESPACES=56;
    public static final int ORDERING=53;
    public static final int ASSIGNSi=65;
    public static final int UNORDERED=55;
    public static final int NumberLEX=231;
    public static final int PredefinedEntityRef=236;
    public static final int LTSi=132;
    public static final int AST_DIRELEMCONSTRUCTOR=25;
    public static final int SENTENCE=166;
    public static final int INHERIT=58;
    public static final int AST_CASECLAUSE=13;
    public static final int LexLiterals=234;
    public static final int SHARPSi=252;
    public static final int IS=136;
    public static final int GT=129;
    public static final int PHRASE=151;
    public static final int StringLiteral=32;
    public static final int RPISi=223;
    public static final int DirPiContents=222;
    public static final int ITEM=76;
    public static final int ORDERED=54;
    public static final int DirCommentConstLEX=228;
    public static final int PROCESSING_INSTRUCTION=83;
    public static final int NODEAFTERSi=138;
    public static final int CharRef=237;
    public static final int COLLATION=43;
    public static final int LPARSi=69;
    public static final int ANCESTOR=191;
    public static final int AST_PREDICATE=24;
    public static final int VARIABLE=63;
    public static final int EOF=-1;
    public static final int RBRACSi=72;
    public static final int ElementContent=213;
    public static final int LCOMMENTSi=217;
    public static final int LBRACKSi=205;

        public XQFTParser(TokenStream input) {
            super(input);
            ruleMemo = new HashMap[185+1];
         }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return tokenNames; }
    public String getGrammarFileName() { return "etc/XQFT.g"; }


        
        /* Root scope */
        //Scope currentScope = new Scope();       // @init-ting her ogsaa
        XQFTLexer lexer;
    /*
        public void setTokenStream(TokenStream input) {
                    String inputz =  "some $x in (1, 2) satisfies $x + $x = 3";
                                CharStream cs = new ANTLRStringStream(inputz);
            lexer = (XQFTLexer)input.getTokenSource();
            lexer.setCharStream(cs);
            UnbufferedCommonTokenStream tokenz = new UnbufferedCommonTokenStream();
            tokenz.setTokenSource(lexer);
            super.setTokenStream(tokenz);
            setTreeAdaptor(new XQFTTreeAdaptor());
         
        }*/

        public void setLexer(XQFTLexer lex)
        {
            this.lexer=lex;
        }
        
        public XQFTLexer getLexer()
        {
            return this.lexer;
        }

        protected void mismatch(IntStream input, int ttype, BitSet follow)
            throws RecognitionException
        {
            throw new MismatchedTokenException(ttype, input);
        }

        public void recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow)
            throws RecognitionException
        {
            throw e;
        }



    public static class module_return extends ParserRuleReturnScope {
        XQFTTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start module
    // etc/XQFT.g:312:1: module : ( versionDecl )? ( libraryModule | mainModule ) -> ^( AST_MODULE ( versionDecl )? ( libraryModule )? ( mainModule )? ) ;
    public final module_return module() throws RecognitionException {
        module_return retval = new module_return();
        retval.start = input.LT(1);

        XQFTTree root_0 = null;

        versionDecl_return versionDecl1 = null;

        libraryModule_return libraryModule2 = null;

        mainModule_return mainModule3 = null;


        RewriteRuleSubtreeStream stream_libraryModule=new RewriteRuleSubtreeStream(adaptor,"rule libraryModule");
        RewriteRuleSubtreeStream stream_versionDecl=new RewriteRuleSubtreeStream(adaptor,"rule versionDecl");
        RewriteRuleSubtreeStream stream_mainModule=new RewriteRuleSubtreeStream(adaptor,"rule mainModule");
        try {
            // etc/XQFT.g:312:8: ( ( versionDecl )? ( libraryModule | mainModule ) -> ^( AST_MODULE ( versionDecl )? ( libraryModule )? ( mainModule )? ) )
            // etc/XQFT.g:312:9: ( versionDecl )? ( libraryModule | mainModule )
            {
            if ( backtracking==0 ) {
              lexer.state = State.DEFAULT;
            }
            // etc/XQFT.g:313:5: ( versionDecl )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==XQUERY) ) {
                int LA1_1 = input.LA(2);

                if ( (LA1_1==VERSION) ) {
                    alt1=1;
                }
            }
            switch (alt1) {
                case 1 :
                    // etc/XQFT.g:313:6: versionDecl
                    {
                    pushFollow(FOLLOW_versionDecl_in_module197);
                    versionDecl1=versionDecl();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_versionDecl.add(versionDecl1.getTree());

                    }
                    break;

            }

            // etc/XQFT.g:314:5: ( libraryModule | mainModule )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==MODULE) ) {
                int LA2_1 = input.LA(2);

                if ( (LA2_1==NAMESPACE) ) {
                    alt2=1;
                }
                else if ( (LA2_1==EOF||(LA2_1>=EQSi && LA2_1<=COMMASi)||LA2_1==LPARSi||LA2_1==STARSi||LA2_1==PLUSSi||(LA2_1>=OR && LA2_1<=INSTANCE)||(LA2_1>=TREAT && LA2_1<=CAST)||(LA2_1>=EQ && LA2_1<=NODEAFTERSi)||(LA2_1>=SLASHSi && LA2_1<=DBLSLASHSi)||LA2_1==COLONSi||LA2_1==LBRACKSi) ) {
                    alt2=2;
                }
                else {
                    if (backtracking>0) {failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("314:5: ( libraryModule | mainModule )", 2, 1, input);

                    throw nvae;
                }
            }
            else if ( ((LA2_0>=XQUERY && LA2_0<=ENCODING)||LA2_0==NAMESPACE||(LA2_0>=DECLARE && LA2_0<=DOLLARSi)||(LA2_0>=EXTERNAL && LA2_0<=LPARSi)||(LA2_0>=OPTION && LA2_0<=DOCUMENT_NODE)||(LA2_0>=STARSi && LA2_0<=UNION)||(LA2_0>=INTERSECT && LA2_0<=GE)||LA2_0==LTSi||LA2_0==IS||(LA2_0>=WEIGHT && LA2_0<=LPRAGSi)||(LA2_0>=WINDOW && LA2_0<=ANCESTOR_OR_SELF)||(LA2_0>=DOTDOTSi && LA2_0<=ATSi)||LA2_0==LCOMMENTSi||LA2_0==LPISi||(LA2_0>=DOCUMENT && LA2_0<=NCName)) ) {
                alt2=2;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("314:5: ( libraryModule | mainModule )", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // etc/XQFT.g:314:6: libraryModule
                    {
                    pushFollow(FOLLOW_libraryModule_in_module207);
                    libraryModule2=libraryModule();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_libraryModule.add(libraryModule2.getTree());

                    }
                    break;
                case 2 :
                    // etc/XQFT.g:314:22: mainModule
                    {
                    pushFollow(FOLLOW_mainModule_in_module211);
                    mainModule3=mainModule();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) stream_mainModule.add(mainModule3.getTree());

                    }
                    break;

            }


            // AST REWRITE
            // elements: mainModule, libraryModule, versionDecl
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (XQFTTree)adaptor.nil();
            // 315:5: -> ^( AST_MODULE ( versionDecl )? ( libraryModule )? ( mainModule )? )
            {
                // etc/XQFT.g:315:8: ^( AST_MODULE ( versionDecl )? ( libraryModule )? ( mainModule )? )
                {
                XQFTTree root_1 = (XQFTTree)adaptor.nil();
                root_1 = (XQFTTree)adaptor.becomeRoot(adaptor.create(AST_MODULE, "AST_MODULE"), root_1);

                // etc/XQFT.g:315:21: ( versionDecl )?
                if ( stream_versionDecl.hasNext() ) {
                    adaptor.addChild(root_1, stream_versionDecl.next());

                }
                stream_versionDecl.reset();
                // etc/XQFT.g:315:34: ( libraryModule )?
                if ( stream_libraryModule.hasNext() ) {
                    adaptor.addChild(root_1, stream_libraryModule.next());

                }
                stream_libraryModule.reset();
                // etc/XQFT.g:315:49: ( mainModule )?
                if ( stream_mainModule.hasNext() ) {
                    adaptor.addChild(root_1, stream_mainModule.next());

                }
                stream_mainModule.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (XQFTTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

            catch (RecognitionException e) {
                throw e;
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end module

    public static class versionDecl_return extends ParserRuleReturnScope {
        XQFTTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start versionDecl
    // etc/XQFT.g:318:5: versionDecl : XQUERY VERSION StringLiteral ( ENCODING StringLiteral )? SEMICOLONSi ;
    public final versionDecl_return versionDecl() throws RecognitionException {
        versionDecl_return retval = new versionDecl_return();
        retval.start = input.LT(1);

        XQFTTree root_0 = null;

        Token XQUERY4=null;
        Token VERSION5=null;
        Token StringLiteral6=null;
        Token ENCODING7=null;
        Token StringLiteral8=null;
        Token SEMICOLONSi9=null;

        XQFTTree XQUERY4_tree=null;
        XQFTTree VERSION5_tree=null;
        XQFTTree StringLiteral6_tree=null;
        XQFTTree ENCODING7_tree=null;
        XQFTTree StringLiteral8_tree=null;
        XQFTTree SEMICOLONSi9_tree=null;

        try {
            // etc/XQFT.g:318:17: ( XQUERY VERSION StringLiteral ( ENCODING StringLiteral )? SEMICOLONSi )
            // etc/XQFT.g:318:19: XQUERY VERSION StringLiteral ( ENCODING StringLiteral )? SEMICOLONSi
            {
            root_0 = (XQFTTree)adaptor.nil();

            XQUERY4=(Token)input.LT(1);
            match(input,XQUERY,FOLLOW_XQUERY_in_versionDecl244); if (failed) return retval;
            if ( backtracking==0 ) {
            XQUERY4_tree = (XQFTTree)adaptor.create(XQUERY4);
            adaptor.addChild(root_0, XQUERY4_tree);
            }
            VERSION5=(Token)input.LT(1);
            match(input,VERSION,FOLLOW_VERSION_in_versionDecl246); if (failed) return retval;
            if ( backtracking==0 ) {
            VERSION5_tree = (XQFTTree)adaptor.create(VERSION5);
            adaptor.addChild(root_0, VERSION5_tree);
            }
            StringLiteral6=(Token)input.LT(1);
            match(input,StringLiteral,FOLLOW_StringLiteral_in_versionDecl248); if (failed) return retval;
            if ( backtracking==0 ) {
            StringLiteral6_tree = (XQFTTree)adaptor.create(StringLiteral6);
            adaptor.addChild(root_0, StringLiteral6_tree);
            }
            // etc/XQFT.g:318:48: ( ENCODING StringLiteral )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==ENCODING) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // etc/XQFT.g:318:49: ENCODING StringLiteral
                    {
                    ENCODING7=(Token)input.LT(1);
                    match(input,ENCODING,FOLLOW_ENCODING_in_versionDecl251); if (failed) return retval;
                    if ( backtracking==0 ) {
                    ENCODING7_tree = (XQFTTree)adaptor.create(ENCODING7);
                    adaptor.addChild(root_0, ENCODING7_tree);
                    }
                    StringLiteral8=(Token)input.LT(1);
                    match(input,StringLiteral,FOLLOW_StringLiteral_in_versionDecl253); if (failed) return retval;
                    if ( backtracking==0 ) {
                    StringLiteral8_tree = (XQFTTree)adaptor.create(StringLiteral8);
                    adaptor.addChild(root_0, StringLiteral8_tree);
                    }

                    }
                    break;

            }

            SEMICOLONSi9=(Token)input.LT(1);
            match(input,SEMICOLONSi,FOLLOW_SEMICOLONSi_in_versionDecl257); if (failed) return retval;
            if ( backtracking==0 ) {
            SEMICOLONSi9_tree = (XQFTTree)adaptor.create(SEMICOLONSi9);
            adaptor.addChild(root_0, SEMICOLONSi9_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (XQFTTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

            catch (RecognitionException e) {
                throw e;
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end versionDecl

    public static class libraryModule_return extends ParserRuleReturnScope {
        XQFTTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start libraryModule
    // etc/XQFT.g:319:5: libraryModule : moduleDecl prolog ;
    public final libraryModule_return libraryModule() throws RecognitionException {
        libraryModule_return retval = new libraryModule_return();
        retval.start = input.LT(1);

        XQFTTree root_0 = null;

        moduleDecl_return moduleDecl10 = null;

        prolog_return prolog11 = null;



        try {
            // etc/XQFT.g:319:19: ( moduleDecl prolog )
            // etc/XQFT.g:319:21: moduleDecl prolog
            {
            root_0 = (XQFTTree)adaptor.nil();

            pushFollow(FOLLOW_moduleDecl_in_libraryModule268);
            moduleDecl10=moduleDecl();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, moduleDecl10.getTree());
            pushFollow(FOLLOW_prolog_in_libraryModule270);
            prolog11=prolog();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, prolog11.getTree());

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (XQFTTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

            catch (RecognitionException e) {
                throw e;
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end libraryModule

    public static class moduleDecl_return extends ParserRuleReturnScope {
        XQFTTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start moduleDecl
    // etc/XQFT.g:320:9: moduleDecl : MODULE NAMESPACE ncNameorKeyword EQSi uriLiteral SEMICOLONSi ;
    public final moduleDecl_return moduleDecl() throws RecognitionException {
        moduleDecl_return retval = new moduleDecl_return();
        retval.start = input.LT(1);

        XQFTTree root_0 = null;

        Token MODULE12=null;
        Token NAMESPACE13=null;
        Token EQSi15=null;
        Token SEMICOLONSi17=null;
        ncNameorKeyword_return ncNameorKeyword14 = null;

        uriLiteral_return uriLiteral16 = null;


        XQFTTree MODULE12_tree=null;
        XQFTTree NAMESPACE13_tree=null;
        XQFTTree EQSi15_tree=null;
        XQFTTree SEMICOLONSi17_tree=null;

        try {
            // etc/XQFT.g:320:20: ( MODULE NAMESPACE ncNameorKeyword EQSi uriLiteral SEMICOLONSi )
            // etc/XQFT.g:320:22: MODULE NAMESPACE ncNameorKeyword EQSi uriLiteral SEMICOLONSi
            {
            root_0 = (XQFTTree)adaptor.nil();

            MODULE12=(Token)input.LT(1);
            match(input,MODULE,FOLLOW_MODULE_in_moduleDecl285); if (failed) return retval;
            if ( backtracking==0 ) {
            MODULE12_tree = (XQFTTree)adaptor.create(MODULE12);
            adaptor.addChild(root_0, MODULE12_tree);
            }
            NAMESPACE13=(Token)input.LT(1);
            match(input,NAMESPACE,FOLLOW_NAMESPACE_in_moduleDecl287); if (failed) return retval;
            if ( backtracking==0 ) {
            NAMESPACE13_tree = (XQFTTree)adaptor.create(NAMESPACE13);
            adaptor.addChild(root_0, NAMESPACE13_tree);
            }
            pushFollow(FOLLOW_ncNameorKeyword_in_moduleDecl289);
            ncNameorKeyword14=ncNameorKeyword();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, ncNameorKeyword14.getTree());
            EQSi15=(Token)input.LT(1);
            match(input,EQSi,FOLLOW_EQSi_in_moduleDecl291); if (failed) return retval;
            if ( backtracking==0 ) {
            EQSi15_tree = (XQFTTree)adaptor.create(EQSi15);
            adaptor.addChild(root_0, EQSi15_tree);
            }
            pushFollow(FOLLOW_uriLiteral_in_moduleDecl293);
            uriLiteral16=uriLiteral();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, uriLiteral16.getTree());
            SEMICOLONSi17=(Token)input.LT(1);
            match(input,SEMICOLONSi,FOLLOW_SEMICOLONSi_in_moduleDecl295); if (failed) return retval;
            if ( backtracking==0 ) {
            SEMICOLONSi17_tree = (XQFTTree)adaptor.create(SEMICOLONSi17);
            adaptor.addChild(root_0, SEMICOLONSi17_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (XQFTTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

            catch (RecognitionException e) {
                throw e;
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end moduleDecl

    public static class uriLiteral_return extends ParserRuleReturnScope {
        XQFTTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start uriLiteral
    // etc/XQFT.g:321:13: uriLiteral : StringLiteral ;
    public final uriLiteral_return uriLiteral() throws RecognitionException {
        uriLiteral_return retval = new uriLiteral_return();
        retval.start = input.LT(1);

        XQFTTree root_0 = null;

        Token StringLiteral18=null;

        XQFTTree StringLiteral18_tree=null;

        try {
            // etc/XQFT.g:321:24: ( StringLiteral )
            // etc/XQFT.g:321:26: StringLiteral
            {
            root_0 = (XQFTTree)adaptor.nil();

            StringLiteral18=(Token)input.LT(1);
            match(input,StringLiteral,FOLLOW_StringLiteral_in_uriLiteral314); if (failed) return retval;
            if ( backtracking==0 ) {
            StringLiteral18_tree = (XQFTTree)adaptor.create(StringLiteral18);
            adaptor.addChild(root_0, StringLiteral18_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (XQFTTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

            catch (RecognitionException e) {
                throw e;
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end uriLiteral

    public static class mainModule_return extends ParserRuleReturnScope {
        XQFTTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start mainModule
    // etc/XQFT.g:323:5: mainModule : prolog queryBody ;
    public final mainModule_return mainModule() throws RecognitionException {
        mainModule_return retval = new mainModule_return();
        retval.start = input.LT(1);

        XQFTTree root_0 = null;

        prolog_return prolog19 = null;

        queryBody_return queryBody20 = null;



        try {
            // etc/XQFT.g:323:16: ( prolog queryBody )
            // etc/XQFT.g:323:18: prolog queryBody
            {
            root_0 = (XQFTTree)adaptor.nil();

            pushFollow(FOLLOW_prolog_in_mainModule326);
            prolog19=prolog();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, prolog19.getTree());
            pushFollow(FOLLOW_queryBody_in_mainModule328);
            queryBody20=queryBody();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, queryBody20.getTree());

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (XQFTTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

            catch (RecognitionException e) {
                throw e;
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end mainModule

    public static class queryBody_return extends ParserRuleReturnScope {
        XQFTTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start queryBody
    // etc/XQFT.g:324:9: queryBody : expr ;
    public final queryBody_return queryBody() throws RecognitionException {
        queryBody_return retval = new queryBody_return();
        retval.start = input.LT(1);

        XQFTTree root_0 = null;

        expr_return expr21 = null;



        try {
            // etc/XQFT.g:324:19: ( expr )
            // etc/XQFT.g:324:21: expr
            {
            root_0 = (XQFTTree)adaptor.nil();

            pushFollow(FOLLOW_expr_in_queryBody343);
            expr21=expr();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, expr21.getTree());

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (XQFTTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

            catch (RecognitionException e) {
                throw e;
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end queryBody

    public static class expr_return extends ParserRuleReturnScope {
        XQFTTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start expr
    // etc/XQFT.g:325:13: expr : exprSingle ( COMMASi exprSingle )* -> ( exprSingle )+ ;
    public final expr_return expr() throws RecognitionException {
        expr_return retval = new expr_return();
        retval.start = input.LT(1);

        XQFTTree root_0 = null;

        Token COMMASi23=null;
        exprSingle_return exprSingle22 = null;

        exprSingle_return exprSingle24 = null;


        XQFTTree COMMASi23_tree=null;
        RewriteRuleTokenStream stream_COMMASi=new RewriteRuleTokenStream(adaptor,"token COMMASi");
        RewriteRuleSubtreeStream stream_exprSingle=new RewriteRuleSubtreeStream(adaptor,"rule exprSingle");
        try {
            // etc/XQFT.g:325:18: ( exprSingle ( COMMASi exprSingle )* -> ( exprSingle )+ )
            // etc/XQFT.g:325:20: exprSingle ( COMMASi exprSingle )*
            {
            pushFollow(FOLLOW_exprSingle_in_expr362);
            exprSingle22=exprSingle();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) stream_exprSingle.add(exprSingle22.getTree());
            // etc/XQFT.g:325:31: ( COMMASi exprSingle )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==COMMASi) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // etc/XQFT.g:325:32: COMMASi exprSingle
            	    {
            	    COMMASi23=(Token)input.LT(1);
            	    match(input,COMMASi,FOLLOW_COMMASi_in_expr365); if (failed) return retval;
            	    if ( backtracking==0 ) stream_COMMASi.add(COMMASi23);

            	    pushFollow(FOLLOW_exprSingle_in_expr367);
            	    exprSingle24=exprSingle();
            	    _fsp--;
            	    if (failed) return retval;
            	    if ( backtracking==0 ) stream_exprSingle.add(exprSingle24.getTree());

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            // AST REWRITE
            // elements: exprSingle
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            if ( backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (XQFTTree)adaptor.nil();
            // 326:20: -> ( exprSingle )+
            {
                if ( !(stream_exprSingle.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_exprSingle.hasNext() ) {
                    adaptor.addChild(root_0, stream_exprSingle.next());

                }
                stream_exprSingle.reset();

            }

            }

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (XQFTTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

            catch (RecognitionException e) {
                throw e;
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end expr

    public static class prolog_return extends ParserRuleReturnScope {
        XQFTTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start prolog
    // etc/XQFT.g:330:1: prolog : ( ( ( IMPORT ( SCHEMA | MODULE ) )=> importStmt {...}? | DECLARE ( setter {...}? | varDecl | functionDecl | optionDecl | ftOptionDecl ) ) SEMICOLONSi )* ;
    public final prolog_return prolog() throws RecognitionException {
        prolog_return retval = new prolog_return();
        retval.start = input.LT(1);

        XQFTTree root_0 = null;

        Token DECLARE26=null;
        Token SEMICOLONSi32=null;
        importStmt_return importStmt25 = null;

        setter_return setter27 = null;

        varDecl_return varDecl28 = null;

        functionDecl_return functionDecl29 = null;

        optionDecl_return optionDecl30 = null;

        ftOptionDecl_return ftOptionDecl31 = null;


        XQFTTree DECLARE26_tree=null;
        XQFTTree SEMICOLONSi32_tree=null;

        boolean start = true;
        try {
            // etc/XQFT.g:331:30: ( ( ( ( IMPORT ( SCHEMA | MODULE ) )=> importStmt {...}? | DECLARE ( setter {...}? | varDecl | functionDecl | optionDecl | ftOptionDecl ) ) SEMICOLONSi )* )
            // etc/XQFT.g:332:5: ( ( ( IMPORT ( SCHEMA | MODULE ) )=> importStmt {...}? | DECLARE ( setter {...}? | varDecl | functionDecl | optionDecl | ftOptionDecl ) ) SEMICOLONSi )*
            {
            root_0 = (XQFTTree)adaptor.nil();

            // etc/XQFT.g:332:5: ( ( ( IMPORT ( SCHEMA | MODULE ) )=> importStmt {...}? | DECLARE ( setter {...}? | varDecl | functionDecl | optionDecl | ftOptionDecl ) ) SEMICOLONSi )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==DECLARE) ) {
                    int LA7_46 = input.LA(2);

                    if ( (LA7_46==NAMESPACE||LA7_46==DEFAULT||LA7_46==FUNCTION||LA7_46==BOUNDARYSPACE||(LA7_46>=BASE_URI && LA7_46<=ORDERING)||LA7_46==COPY_NAMESPACES||LA7_46==VARIABLE||(LA7_46>=OPTION && LA7_46<=FTOPTION)) ) {
                        alt7=1;
                    }


                }
                else if ( (LA7_0==IMPORT) ) {
                    int LA7_79 = input.LA(2);

                    if ( (LA7_79==MODULE||LA7_79==SCHEMA) ) {
                        alt7=1;
                    }


                }


                switch (alt7) {
            	case 1 :
            	    // etc/XQFT.g:333:7: ( ( IMPORT ( SCHEMA | MODULE ) )=> importStmt {...}? | DECLARE ( setter {...}? | varDecl | functionDecl | optionDecl | ftOptionDecl ) ) SEMICOLONSi
            	    {
            	    // etc/XQFT.g:333:7: ( ( IMPORT ( SCHEMA | MODULE ) )=> importStmt {...}? | DECLARE ( setter {...}? | varDecl | functionDecl | optionDecl | ftOptionDecl ) )
            	    int alt6=2;
            	    int LA6_0 = input.LA(1);

            	    if ( (LA6_0==IMPORT) && (synpred1())) {
            	        alt6=1;
            	    }
            	    else if ( (LA6_0==DECLARE) ) {
            	        alt6=2;
            	    }
            	    else {
            	        if (backtracking>0) {failed=true; return retval;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("333:7: ( ( IMPORT ( SCHEMA | MODULE ) )=> importStmt {...}? | DECLARE ( setter {...}? | varDecl | functionDecl | optionDecl | ftOptionDecl ) )", 6, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt6) {
            	        case 1 :
            	            // etc/XQFT.g:334:7: ( IMPORT ( SCHEMA | MODULE ) )=> importStmt {...}?
            	            {
            	            pushFollow(FOLLOW_importStmt_in_prolog480);
            	            importStmt25=importStmt();
            	            _fsp--;
            	            if (failed) return retval;
            	            if ( backtracking==0 ) adaptor.addChild(root_0, importStmt25.getTree());
            	            if ( !(start) ) {
            	                if (backtracking>0) {failed=true; return retval;}
            	                throw new FailedPredicateException(input, "prolog", "start");
            	            }

            	            }
            	            break;
            	        case 2 :
            	            // etc/XQFT.g:335:8: DECLARE ( setter {...}? | varDecl | functionDecl | optionDecl | ftOptionDecl )
            	            {
            	            DECLARE26=(Token)input.LT(1);
            	            match(input,DECLARE,FOLLOW_DECLARE_in_prolog491); if (failed) return retval;
            	            if ( backtracking==0 ) {
            	            DECLARE26_tree = (XQFTTree)adaptor.create(DECLARE26);
            	            adaptor.addChild(root_0, DECLARE26_tree);
            	            }
            	            // etc/XQFT.g:335:16: ( setter {...}? | varDecl | functionDecl | optionDecl | ftOptionDecl )
            	            int alt5=5;
            	            switch ( input.LA(1) ) {
            	            case NAMESPACE:
            	            case DEFAULT:
            	            case BOUNDARYSPACE:
            	            case BASE_URI:
            	            case CONSTRUCTION:
            	            case ORDERING:
            	            case COPY_NAMESPACES:
            	                {
            	                alt5=1;
            	                }
            	                break;
            	            case VARIABLE:
            	                {
            	                alt5=2;
            	                }
            	                break;
            	            case FUNCTION:
            	                {
            	                alt5=3;
            	                }
            	                break;
            	            case OPTION:
            	                {
            	                alt5=4;
            	                }
            	                break;
            	            case FTOPTION:
            	                {
            	                alt5=5;
            	                }
            	                break;
            	            default:
            	                if (backtracking>0) {failed=true; return retval;}
            	                NoViableAltException nvae =
            	                    new NoViableAltException("335:16: ( setter {...}? | varDecl | functionDecl | optionDecl | ftOptionDecl )", 5, 0, input);

            	                throw nvae;
            	            }

            	            switch (alt5) {
            	                case 1 :
            	                    // etc/XQFT.g:336:9: setter {...}?
            	                    {
            	                    pushFollow(FOLLOW_setter_in_prolog503);
            	                    setter27=setter();
            	                    _fsp--;
            	                    if (failed) return retval;
            	                    if ( backtracking==0 ) adaptor.addChild(root_0, setter27.getTree());
            	                    if ( !(start) ) {
            	                        if (backtracking>0) {failed=true; return retval;}
            	                        throw new FailedPredicateException(input, "prolog", "start");
            	                    }

            	                    }
            	                    break;
            	                case 2 :
            	                    // etc/XQFT.g:337:10: varDecl
            	                    {
            	                    pushFollow(FOLLOW_varDecl_in_prolog516);
            	                    varDecl28=varDecl();
            	                    _fsp--;
            	                    if (failed) return retval;
            	                    if ( backtracking==0 ) adaptor.addChild(root_0, varDecl28.getTree());
            	                    if ( backtracking==0 ) {
            	                      start=false;
            	                    }

            	                    }
            	                    break;
            	                case 3 :
            	                    // etc/XQFT.g:338:10: functionDecl
            	                    {
            	                    pushFollow(FOLLOW_functionDecl_in_prolog529);
            	                    functionDecl29=functionDecl();
            	                    _fsp--;
            	                    if (failed) return retval;
            	                    if ( backtracking==0 ) adaptor.addChild(root_0, functionDecl29.getTree());
            	                    if ( backtracking==0 ) {
            	                      start=false;
            	                    }

            	                    }
            	                    break;
            	                case 4 :
            	                    // etc/XQFT.g:339:10: optionDecl
            	                    {
            	                    pushFollow(FOLLOW_optionDecl_in_prolog542);
            	                    optionDecl30=optionDecl();
            	                    _fsp--;
            	                    if (failed) return retval;
            	                    if ( backtracking==0 ) adaptor.addChild(root_0, optionDecl30.getTree());
            	                    if ( backtracking==0 ) {
            	                      start=false;
            	                    }

            	                    }
            	                    break;
            	                case 5 :
            	                    // etc/XQFT.g:340:10: ftOptionDecl
            	                    {
            	                    pushFollow(FOLLOW_ftOptionDecl_in_prolog555);
            	                    ftOptionDecl31=ftOptionDecl();
            	                    _fsp--;
            	                    if (failed) return retval;
            	                    if ( backtracking==0 ) adaptor.addChild(root_0, ftOptionDecl31.getTree());
            	                    if ( backtracking==0 ) {
            	                      start=false;
            	                    }

            	                    }
            	                    break;

            	            }


            	            }
            	            break;

            	    }

            	    SEMICOLONSi32=(Token)input.LT(1);
            	    match(input,SEMICOLONSi,FOLLOW_SEMICOLONSi_in_prolog577); if (failed) return retval;
            	    if ( backtracking==0 ) {
            	    SEMICOLONSi32_tree = (XQFTTree)adaptor.create(SEMICOLONSi32);
            	    adaptor.addChild(root_0, SEMICOLONSi32_tree);
            	    }

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (XQFTTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

            catch (RecognitionException e) {
                throw e;
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end prolog

    public static class setter_return extends ParserRuleReturnScope {
        XQFTTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start setter
    // etc/XQFT.g:346:1: setter : ( DEFAULT ( | defaultNamespaceDecl | defaultCollationDecl | emptyOrderDecl ) | namespaceDecl | boundarySpaceDecl | baseURIDecl | constructionDecl | orderingModeDecl | copyNamespacesDecl );
    public final setter_return setter() throws RecognitionException {
        setter_return retval = new setter_return();
        retval.start = input.LT(1);

        XQFTTree root_0 = null;

        Token DEFAULT33=null;
        defaultNamespaceDecl_return defaultNamespaceDecl34 = null;

        defaultCollationDecl_return defaultCollationDecl35 = null;

        emptyOrderDecl_return emptyOrderDecl36 = null;

        namespaceDecl_return namespaceDecl37 = null;

        boundarySpaceDecl_return boundarySpaceDecl38 = null;

        baseURIDecl_return baseURIDecl39 = null;

        constructionDecl_return constructionDecl40 = null;

        orderingModeDecl_return orderingModeDecl41 = null;

        copyNamespacesDecl_return copyNamespacesDecl42 = null;


        XQFTTree DEFAULT33_tree=null;

        try {
            // etc/XQFT.g:346:7: ( DEFAULT ( | defaultNamespaceDecl | defaultCollationDecl | emptyOrderDecl ) | namespaceDecl | boundarySpaceDecl | baseURIDecl | constructionDecl | orderingModeDecl | copyNamespacesDecl )
            int alt9=7;
            switch ( input.LA(1) ) {
            case DEFAULT:
                {
                alt9=1;
                }
                break;
            case NAMESPACE:
                {
                alt9=2;
                }
                break;
            case BOUNDARYSPACE:
                {
                alt9=3;
                }
                break;
            case BASE_URI:
                {
                alt9=4;
                }
                break;
            case CONSTRUCTION:
                {
                alt9=5;
                }
                break;
            case ORDERING:
                {
                alt9=6;
                }
                break;
            case COPY_NAMESPACES:
                {
                alt9=7;
                }
                break;
            default:
                if (backtracking>0) {failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("346:1: setter : ( DEFAULT ( | defaultNamespaceDecl | defaultCollationDecl | emptyOrderDecl ) | namespaceDecl | boundarySpaceDecl | baseURIDecl | constructionDecl | orderingModeDecl | copyNamespacesDecl );", 9, 0, input);

                throw nvae;
            }

            switch (alt9) {
                case 1 :
                    // etc/XQFT.g:347:5: DEFAULT ( | defaultNamespaceDecl | defaultCollationDecl | emptyOrderDecl )
                    {
                    root_0 = (XQFTTree)adaptor.nil();

                    DEFAULT33=(Token)input.LT(1);
                    match(input,DEFAULT,FOLLOW_DEFAULT_in_setter599); if (failed) return retval;
                    if ( backtracking==0 ) {
                    DEFAULT33_tree = (XQFTTree)adaptor.create(DEFAULT33);
                    adaptor.addChild(root_0, DEFAULT33_tree);
                    }
                    // etc/XQFT.g:347:13: ( | defaultNamespaceDecl | defaultCollationDecl | emptyOrderDecl )
                    int alt8=4;
                    switch ( input.LA(1) ) {
                    case SEMICOLONSi:
                        {
                        alt8=1;
                        }
                        break;
                    case ELEMENT:
                    case FUNCTION:
                        {
                        alt8=2;
                        }
                        break;
                    case COLLATION:
                        {
                        alt8=3;
                        }
                        break;
                    case ORDER:
                        {
                        alt8=4;
                        }
                        break;
                    default:
                        if (backtracking>0) {failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("347:13: ( | defaultNamespaceDecl | defaultCollationDecl | emptyOrderDecl )", 8, 0, input);

                        throw nvae;
                    }

                    switch (alt8) {
                        case 1 :
                            // etc/XQFT.g:348:13: 
                            {
                            }
                            break;
                        case 2 :
                            // etc/XQFT.g:348:15: defaultNamespaceDecl
                            {
                            pushFollow(FOLLOW_defaultNamespaceDecl_in_setter617);
                            defaultNamespaceDecl34=defaultNamespaceDecl();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) adaptor.addChild(root_0, defaultNamespaceDecl34.getTree());

                            }
                            break;
                        case 3 :
                            // etc/XQFT.g:349:15: defaultCollationDecl
                            {
                            pushFollow(FOLLOW_defaultCollationDecl_in_setter633);
                            defaultCollationDecl35=defaultCollationDecl();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) adaptor.addChild(root_0, defaultCollationDecl35.getTree());

                            }
                            break;
                        case 4 :
                            // etc/XQFT.g:350:15: emptyOrderDecl
                            {
                            pushFollow(FOLLOW_emptyOrderDecl_in_setter649);
                            emptyOrderDecl36=emptyOrderDecl();
                            _fsp--;
                            if (failed) return retval;
                            if ( backtracking==0 ) adaptor.addChild(root_0, emptyOrderDecl36.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // etc/XQFT.g:352:11: namespaceDecl
                    {
                    root_0 = (XQFTTree)adaptor.nil();

                    pushFollow(FOLLOW_namespaceDecl_in_setter675);
                    namespaceDecl37=namespaceDecl();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, namespaceDecl37.getTree());

                    }
                    break;
                case 3 :
                    // etc/XQFT.g:353:11: boundarySpaceDecl
                    {
                    root_0 = (XQFTTree)adaptor.nil();

                    pushFollow(FOLLOW_boundarySpaceDecl_in_setter687);
                    boundarySpaceDecl38=boundarySpaceDecl();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, boundarySpaceDecl38.getTree());

                    }
                    break;
                case 4 :
                    // etc/XQFT.g:354:11: baseURIDecl
                    {
                    root_0 = (XQFTTree)adaptor.nil();

                    pushFollow(FOLLOW_baseURIDecl_in_setter699);
                    baseURIDecl39=baseURIDecl();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, baseURIDecl39.getTree());

                    }
                    break;
                case 5 :
                    // etc/XQFT.g:355:11: constructionDecl
                    {
                    root_0 = (XQFTTree)adaptor.nil();

                    pushFollow(FOLLOW_constructionDecl_in_setter712);
                    constructionDecl40=constructionDecl();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, constructionDecl40.getTree());

                    }
                    break;
                case 6 :
                    // etc/XQFT.g:356:11: orderingModeDecl
                    {
                    root_0 = (XQFTTree)adaptor.nil();

                    pushFollow(FOLLOW_orderingModeDecl_in_setter725);
                    orderingModeDecl41=orderingModeDecl();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, orderingModeDecl41.getTree());

                    }
                    break;
                case 7 :
                    // etc/XQFT.g:357:11: copyNamespacesDecl
                    {
                    root_0 = (XQFTTree)adaptor.nil();

                    pushFollow(FOLLOW_copyNamespacesDecl_in_setter738);
                    copyNamespacesDecl42=copyNamespacesDecl();
                    _fsp--;
                    if (failed) return retval;
                    if ( backtracking==0 ) adaptor.addChild(root_0, copyNamespacesDecl42.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (XQFTTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

            catch (RecognitionException e) {
                throw e;
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end setter

    public static class defaultNamespaceDecl_return extends ParserRuleReturnScope {
        XQFTTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start defaultNamespaceDecl
    // etc/XQFT.g:360:1: defaultNamespaceDecl : ( ELEMENT | FUNCTION ) NAMESPACE uriLiteral ;
    public final defaultNamespaceDecl_return defaultNamespaceDecl() throws RecognitionException {
        defaultNamespaceDecl_return retval = new defaultNamespaceDecl_return();
        retval.start = input.LT(1);

        XQFTTree root_0 = null;

        Token set43=null;
        Token NAMESPACE44=null;
        uriLiteral_return uriLiteral45 = null;


        XQFTTree set43_tree=null;
        XQFTTree NAMESPACE44_tree=null;

        try {
            // etc/XQFT.g:360:22: ( ( ELEMENT | FUNCTION ) NAMESPACE uriLiteral )
            // etc/XQFT.g:360:24: ( ELEMENT | FUNCTION ) NAMESPACE uriLiteral
            {
            root_0 = (XQFTTree)adaptor.nil();

            set43=(Token)input.LT(1);
            if ( (input.LA(1)>=ELEMENT && input.LA(1)<=FUNCTION) ) {
                input.consume();
                if ( backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set43));
                errorRecovery=false;failed=false;
            }
            else {
                if (backtracking>0) {failed=true; return retval;}
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recoverFromMismatchedSet(input,mse,FOLLOW_set_in_defaultNamespaceDecl747);    throw mse;
            }

            NAMESPACE44=(Token)input.LT(1);
            match(input,NAMESPACE,FOLLOW_NAMESPACE_in_defaultNamespaceDecl755); if (failed) return retval;
            if ( backtracking==0 ) {
            NAMESPACE44_tree = (XQFTTree)adaptor.create(NAMESPACE44);
            adaptor.addChild(root_0, NAMESPACE44_tree);
            }
            pushFollow(FOLLOW_uriLiteral_in_defaultNamespaceDecl757);
            uriLiteral45=uriLiteral();
            _fsp--;
            if (failed) return retval;
            if ( backtracking==0 ) adaptor.addChild(root_0, uriLiteral45.getTree());

            }

            retval.stop = input.LT(-1);

            if ( backtracking==0 ) {
                retval.tree = (XQFTTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

            catch (RecognitionException e) {
                throw e;
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end defaultNamespaceDecl

    public static class defaultCollationDecl_return extends ParserRuleReturnScope {
        XQFTTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start defaultCollationDecl
    // etc/XQFT.g:361:1: defaultCollationDecl : COLLATION uriLiteral ;
    public final defaultCollationDecl_return defaultCollationDecl() throws RecognitionException {
        defaultCollationDecl_return retval = new defaultCollationDecl_return();
        retval.start = input.LT(1);

        XQFTTree root_0 = null;

        Token COLLATION46=null;
        uriLiteral_return uriLiteral47 = null;


        XQFTTree COLLATION46_tree=null;

        try {
            // etc/XQFT.g:361:22: ( COLLATION uriLiteral )
            // etc/XQFT.g:361:24: COLLATION uriLiteral
            {
            root_0 = (XQFTTree)adaptor.nil();

            COLLATION46=(Token)input.LT(1);
            match(input,COLLATION,FOLLOW_COLLATION_in_defaultCollationDecl764); if (failed) return retval;
            if ( backtracking==0 ) {
            COLLATION46_tree = (XQFTTree)adaptor.create(COLLATION46);
            adaptor.addChild(root_0, COLLATION46_tree);
            }
            pushFollow(FOLLOW_

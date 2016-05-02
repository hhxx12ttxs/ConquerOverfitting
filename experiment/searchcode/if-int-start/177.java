// $ANTLR 3.3 Nov 30, 2010 12:50:56 /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g 2011-09-05 22:31:20

package edu.vub.at.parser;
//@SuppressWarnings("unused")// source code lvl > 1.5


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

public class AmbientTalkParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AGBEGIN", "AGSTATEMENT", "AGDEFINITION", "AGTYPEDEFINITION", "AGIMPORT", "AGDOCUMENTATION", "AGDOCSTART", "AGDOCEND", "AGFIELDDEF", "AGDEFFUN", "AGMULTIDEF", "AGTABLEDEF", "AGEXTERNFIELDDEF", "AGEXTERNDEFFUN", "AGVARIABLEASSIGNMENT", "AGTABLEASSIGNMENT", "AGMULTIASSIGNMENT", "AGFIELDASSIGNMENT", "AGTABULATION", "AGNUMBER", "AGFRACTION", "AGTEXT", "AGLOOKUP", "AGSYMBOL", "AGSELF", "AGTABLE", "AGAPPLICATION", "AGCLOSURE", "AGQUOTE", "AGQUOTEBEGIN", "AGUNQUOTE", "AGUNQUOTESPLICE", "AGFIELDSELECT", "AGASYNCMSG", "AGDELEGATIONMSG", "AGUNIVERSALMSG", "AGSPLICE", "AGASSVAR", "AGSEND", "AGMESSAGE", "AGDELMSG", "AGUNIVMSG", "AGSELECT", "AGASSSYMBOL", "AGCOMPARE", "AGADDITION", "AGMULTIPLY", "AGPOWER", "AGKEY", "AGKEYSYMBOL", "AGTEXTLIST", "AGALIASUNQUOTATION", "AGALIAS", "SMC", "SINGLE_LINE_DOC", "MULTI_LINE_DOC", "DEF", "EQL", "LPR", "RPR", "LBC", "RBC", "LBR", "RBR", "DOT", "TDEF", "SST", "COM", "IMPORT", "ALIAS", "EXCLUDE", "NAME", "CMP", "ADD", "MUL", "POW", "NUMBER", "FRACTION", "TEXT", "SELF", "PIP", "CAT", "LKU", "ASSNAME", "BQU", "HSH", "SEL", "KEY", "ARW", "CAR", "USD", "COL", "STR", "LETTER", "DIGIT", "CMPCHAR", "ADDCHAR", "MULCHAR", "POWCHAR", "SCALE", "SIGN", "EXPONENT", "ESC_SEQ", "UNICODE_ESC", "OCTAL_ESC", "HEX_DIGIT", "WHITESPACE", "SINGLE_LINE_COMMENT", "MULTI_LINE_COMMENT", "'keyworddef'"
    };
    public static final int EOF=-1;
    public static final int T__113=113;
    public static final int AGBEGIN=4;
    public static final int AGSTATEMENT=5;
    public static final int AGDEFINITION=6;
    public static final int AGTYPEDEFINITION=7;
    public static final int AGIMPORT=8;
    public static final int AGDOCUMENTATION=9;
    public static final int AGDOCSTART=10;
    public static final int AGDOCEND=11;
    public static final int AGFIELDDEF=12;
    public static final int AGDEFFUN=13;
    public static final int AGMULTIDEF=14;
    public static final int AGTABLEDEF=15;
    public static final int AGEXTERNFIELDDEF=16;
    public static final int AGEXTERNDEFFUN=17;
    public static final int AGVARIABLEASSIGNMENT=18;
    public static final int AGTABLEASSIGNMENT=19;
    public static final int AGMULTIASSIGNMENT=20;
    public static final int AGFIELDASSIGNMENT=21;
    public static final int AGTABULATION=22;
    public static final int AGNUMBER=23;
    public static final int AGFRACTION=24;
    public static final int AGTEXT=25;
    public static final int AGLOOKUP=26;
    public static final int AGSYMBOL=27;
    public static final int AGSELF=28;
    public static final int AGTABLE=29;
    public static final int AGAPPLICATION=30;
    public static final int AGCLOSURE=31;
    public static final int AGQUOTE=32;
    public static final int AGQUOTEBEGIN=33;
    public static final int AGUNQUOTE=34;
    public static final int AGUNQUOTESPLICE=35;
    public static final int AGFIELDSELECT=36;
    public static final int AGASYNCMSG=37;
    public static final int AGDELEGATIONMSG=38;
    public static final int AGUNIVERSALMSG=39;
    public static final int AGSPLICE=40;
    public static final int AGASSVAR=41;
    public static final int AGSEND=42;
    public static final int AGMESSAGE=43;
    public static final int AGDELMSG=44;
    public static final int AGUNIVMSG=45;
    public static final int AGSELECT=46;
    public static final int AGASSSYMBOL=47;
    public static final int AGCOMPARE=48;
    public static final int AGADDITION=49;
    public static final int AGMULTIPLY=50;
    public static final int AGPOWER=51;
    public static final int AGKEY=52;
    public static final int AGKEYSYMBOL=53;
    public static final int AGTEXTLIST=54;
    public static final int AGALIASUNQUOTATION=55;
    public static final int AGALIAS=56;
    public static final int SMC=57;
    public static final int SINGLE_LINE_DOC=58;
    public static final int MULTI_LINE_DOC=59;
    public static final int DEF=60;
    public static final int EQL=61;
    public static final int LPR=62;
    public static final int RPR=63;
    public static final int LBC=64;
    public static final int RBC=65;
    public static final int LBR=66;
    public static final int RBR=67;
    public static final int DOT=68;
    public static final int TDEF=69;
    public static final int SST=70;
    public static final int COM=71;
    public static final int IMPORT=72;
    public static final int ALIAS=73;
    public static final int EXCLUDE=74;
    public static final int NAME=75;
    public static final int CMP=76;
    public static final int ADD=77;
    public static final int MUL=78;
    public static final int POW=79;
    public static final int NUMBER=80;
    public static final int FRACTION=81;
    public static final int TEXT=82;
    public static final int SELF=83;
    public static final int PIP=84;
    public static final int CAT=85;
    public static final int LKU=86;
    public static final int ASSNAME=87;
    public static final int BQU=88;
    public static final int HSH=89;
    public static final int SEL=90;
    public static final int KEY=91;
    public static final int ARW=92;
    public static final int CAR=93;
    public static final int USD=94;
    public static final int COL=95;
    public static final int STR=96;
    public static final int LETTER=97;
    public static final int DIGIT=98;
    public static final int CMPCHAR=99;
    public static final int ADDCHAR=100;
    public static final int MULCHAR=101;
    public static final int POWCHAR=102;
    public static final int SCALE=103;
    public static final int SIGN=104;
    public static final int EXPONENT=105;
    public static final int ESC_SEQ=106;
    public static final int UNICODE_ESC=107;
    public static final int OCTAL_ESC=108;
    public static final int HEX_DIGIT=109;
    public static final int WHITESPACE=110;
    public static final int SINGLE_LINE_COMMENT=111;
    public static final int MULTI_LINE_COMMENT=112;

    // delegates
    // delegators


        public AmbientTalkParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public AmbientTalkParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return AmbientTalkParser.tokenNames; }
    public String getGrammarFileName() { return "/home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g"; }


      //TODO: see atgrammar.g from original parser
      CommonTree keywords2canonical(CommonTree keywordparameterlist) {
        return null;
      }


    public static class program_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "program"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:116:1: program : globalstatementlist ;
    public final AmbientTalkParser.program_return program() throws RecognitionException {
        AmbientTalkParser.program_return retval = new AmbientTalkParser.program_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.globalstatementlist_return globalstatementlist1 = null;



        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:117:3: ( globalstatementlist )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:117:5: globalstatementlist
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_globalstatementlist_in_program477);
            globalstatementlist1=globalstatementlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, globalstatementlist1.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "program"

    public static class globalstatementlist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "globalstatementlist"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:122:1: globalstatementlist : ( EOF -> ^( AGBEGIN ) | stmts+= statement ( SMC stmts+= statement )* ( EOF | SMC EOF ) -> ^( AGBEGIN ( $stmts)* ) );
    public final AmbientTalkParser.globalstatementlist_return globalstatementlist() throws RecognitionException {
        AmbientTalkParser.globalstatementlist_return retval = new AmbientTalkParser.globalstatementlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        Token SMC3=null;
        Token EOF4=null;
        Token SMC5=null;
        Token EOF6=null;
        List list_stmts=null;
        RuleReturnScope stmts = null;
        CommonTree EOF2_tree=null;
        CommonTree SMC3_tree=null;
        CommonTree EOF4_tree=null;
        CommonTree SMC5_tree=null;
        CommonTree EOF6_tree=null;
        RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
        RewriteRuleTokenStream stream_SMC=new RewriteRuleTokenStream(adaptor,"token SMC");
        RewriteRuleSubtreeStream stream_statement=new RewriteRuleSubtreeStream(adaptor,"rule statement");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:123:3: ( EOF -> ^( AGBEGIN ) | stmts+= statement ( SMC stmts+= statement )* ( EOF | SMC EOF ) -> ^( AGBEGIN ( $stmts)* ) )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==EOF) ) {
                alt3=1;
            }
            else if ( ((LA3_0>=SINGLE_LINE_DOC && LA3_0<=DEF)||LA3_0==LPR||LA3_0==LBC||LA3_0==LBR||(LA3_0>=DOT && LA3_0<=TDEF)||LA3_0==IMPORT||(LA3_0>=NAME && LA3_0<=SELF)||LA3_0==LKU||(LA3_0>=BQU && LA3_0<=HSH)||(LA3_0>=ARW && LA3_0<=USD)) ) {
                alt3=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:123:5: EOF
                    {
                    EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_globalstatementlist492); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EOF.add(EOF2);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 124:5: -> ^( AGBEGIN )
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:124:8: ^( AGBEGIN )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGBEGIN, "AGBEGIN"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:125:5: stmts+= statement ( SMC stmts+= statement )* ( EOF | SMC EOF )
                    {
                    pushFollow(FOLLOW_statement_in_globalstatementlist510);
                    stmts=statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_statement.add(stmts.getTree());
                    if (list_stmts==null) list_stmts=new ArrayList();
                    list_stmts.add(stmts.getTree());

                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:125:22: ( SMC stmts+= statement )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( (LA1_0==SMC) ) {
                            int LA1_2 = input.LA(2);

                            if ( ((LA1_2>=SINGLE_LINE_DOC && LA1_2<=DEF)||LA1_2==LPR||LA1_2==LBC||LA1_2==LBR||(LA1_2>=DOT && LA1_2<=TDEF)||LA1_2==IMPORT||(LA1_2>=NAME && LA1_2<=SELF)||LA1_2==LKU||(LA1_2>=BQU && LA1_2<=HSH)||(LA1_2>=ARW && LA1_2<=USD)) ) {
                                alt1=1;
                            }


                        }


                        switch (alt1) {
                    	case 1 :
                    	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:125:23: SMC stmts+= statement
                    	    {
                    	    SMC3=(Token)match(input,SMC,FOLLOW_SMC_in_globalstatementlist513); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_SMC.add(SMC3);

                    	    pushFollow(FOLLOW_statement_in_globalstatementlist517);
                    	    stmts=statement();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_statement.add(stmts.getTree());
                    	    if (list_stmts==null) list_stmts=new ArrayList();
                    	    list_stmts.add(stmts.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop1;
                        }
                    } while (true);

                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:125:46: ( EOF | SMC EOF )
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==EOF) ) {
                        alt2=1;
                    }
                    else if ( (LA2_0==SMC) ) {
                        alt2=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 2, 0, input);

                        throw nvae;
                    }
                    switch (alt2) {
                        case 1 :
                            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:125:47: EOF
                            {
                            EOF4=(Token)match(input,EOF,FOLLOW_EOF_in_globalstatementlist522); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EOF.add(EOF4);


                            }
                            break;
                        case 2 :
                            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:125:53: SMC EOF
                            {
                            SMC5=(Token)match(input,SMC,FOLLOW_SMC_in_globalstatementlist526); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_SMC.add(SMC5);

                            EOF6=(Token)match(input,EOF,FOLLOW_EOF_in_globalstatementlist528); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EOF.add(EOF6);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: stmts
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: stmts
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_stmts=new RewriteRuleSubtreeStream(adaptor,"token stmts",list_stmts);
                    root_0 = (CommonTree)adaptor.nil();
                    // 126:5: -> ^( AGBEGIN ( $stmts)* )
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:126:8: ^( AGBEGIN ( $stmts)* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGBEGIN, "AGBEGIN"), root_1);

                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:126:18: ( $stmts)*
                        while ( stream_stmts.hasNext() ) {
                            adaptor.addChild(root_1, stream_stmts.nextTree());

                        }
                        stream_stmts.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "globalstatementlist"

    public static class statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "statement"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:131:1: statement : (docs+= documentation )* (def= definition -> ^( AGSTATEMENT $def ( $docs)* ) | tdef= typedefinition -> ^( AGSTATEMENT $tdef ( $docs)* ) | imp= importstatement -> ^( AGSTATEMENT $imp ( $docs)* ) | ass= assignment -> ^( AGSTATEMENT $ass ( $docs)* ) | exp= expression -> ^( AGSTATEMENT $exp ( $docs)* ) ) ;
    public final AmbientTalkParser.statement_return statement() throws RecognitionException {
        AmbientTalkParser.statement_return retval = new AmbientTalkParser.statement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        List list_docs=null;
        AmbientTalkParser.definition_return def = null;

        AmbientTalkParser.typedefinition_return tdef = null;

        AmbientTalkParser.importstatement_return imp = null;

        AmbientTalkParser.assignment_return ass = null;

        AmbientTalkParser.expression_return exp = null;

        RuleReturnScope docs = null;
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_assignment=new RewriteRuleSubtreeStream(adaptor,"rule assignment");
        RewriteRuleSubtreeStream stream_definition=new RewriteRuleSubtreeStream(adaptor,"rule definition");
        RewriteRuleSubtreeStream stream_importstatement=new RewriteRuleSubtreeStream(adaptor,"rule importstatement");
        RewriteRuleSubtreeStream stream_documentation=new RewriteRuleSubtreeStream(adaptor,"rule documentation");
        RewriteRuleSubtreeStream stream_typedefinition=new RewriteRuleSubtreeStream(adaptor,"rule typedefinition");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:132:3: ( (docs+= documentation )* (def= definition -> ^( AGSTATEMENT $def ( $docs)* ) | tdef= typedefinition -> ^( AGSTATEMENT $tdef ( $docs)* ) | imp= importstatement -> ^( AGSTATEMENT $imp ( $docs)* ) | ass= assignment -> ^( AGSTATEMENT $ass ( $docs)* ) | exp= expression -> ^( AGSTATEMENT $exp ( $docs)* ) ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:132:5: (docs+= documentation )* (def= definition -> ^( AGSTATEMENT $def ( $docs)* ) | tdef= typedefinition -> ^( AGSTATEMENT $tdef ( $docs)* ) | imp= importstatement -> ^( AGSTATEMENT $imp ( $docs)* ) | ass= assignment -> ^( AGSTATEMENT $ass ( $docs)* ) | exp= expression -> ^( AGSTATEMENT $exp ( $docs)* ) )
            {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:132:5: (docs+= documentation )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>=SINGLE_LINE_DOC && LA4_0<=MULTI_LINE_DOC)) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:132:7: docs+= documentation
            	    {
            	    pushFollow(FOLLOW_documentation_in_statement562);
            	    docs=documentation();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_documentation.add(docs.getTree());
            	    if (list_docs==null) list_docs=new ArrayList();
            	    list_docs.add(docs.getTree());


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:133:7: (def= definition -> ^( AGSTATEMENT $def ( $docs)* ) | tdef= typedefinition -> ^( AGSTATEMENT $tdef ( $docs)* ) | imp= importstatement -> ^( AGSTATEMENT $imp ( $docs)* ) | ass= assignment -> ^( AGSTATEMENT $ass ( $docs)* ) | exp= expression -> ^( AGSTATEMENT $exp ( $docs)* ) )
            int alt5=5;
            alt5 = dfa5.predict(input);
            switch (alt5) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:133:9: def= definition
                    {
                    pushFollow(FOLLOW_definition_in_statement577);
                    def=definition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_definition.add(def.getTree());


                    // AST REWRITE
                    // elements: docs, def
                    // token labels: 
                    // rule labels: retval, def
                    // token list labels: 
                    // rule list labels: docs
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_def=new RewriteRuleSubtreeStream(adaptor,"rule def",def!=null?def.tree:null);
                    RewriteRuleSubtreeStream stream_docs=new RewriteRuleSubtreeStream(adaptor,"token docs",list_docs);
                    root_0 = (CommonTree)adaptor.nil();
                    // 133:31: -> ^( AGSTATEMENT $def ( $docs)* )
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:133:34: ^( AGSTATEMENT $def ( $docs)* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSTATEMENT, "AGSTATEMENT"), root_1);

                        adaptor.addChild(root_1, stream_def.nextTree());
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:133:55: ( $docs)*
                        while ( stream_docs.hasNext() ) {
                            adaptor.addChild(root_1, stream_docs.nextTree());

                        }
                        stream_docs.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:134:9: tdef= typedefinition
                    {
                    pushFollow(FOLLOW_typedefinition_in_statement611);
                    tdef=typedefinition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_typedefinition.add(tdef.getTree());


                    // AST REWRITE
                    // elements: tdef, docs
                    // token labels: 
                    // rule labels: retval, tdef
                    // token list labels: 
                    // rule list labels: docs
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_tdef=new RewriteRuleSubtreeStream(adaptor,"rule tdef",tdef!=null?tdef.tree:null);
                    RewriteRuleSubtreeStream stream_docs=new RewriteRuleSubtreeStream(adaptor,"token docs",list_docs);
                    root_0 = (CommonTree)adaptor.nil();
                    // 134:31: -> ^( AGSTATEMENT $tdef ( $docs)* )
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:134:34: ^( AGSTATEMENT $tdef ( $docs)* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSTATEMENT, "AGSTATEMENT"), root_1);

                        adaptor.addChild(root_1, stream_tdef.nextTree());
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:134:55: ( $docs)*
                        while ( stream_docs.hasNext() ) {
                            adaptor.addChild(root_1, stream_docs.nextTree());

                        }
                        stream_docs.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:135:9: imp= importstatement
                    {
                    pushFollow(FOLLOW_importstatement_in_statement639);
                    imp=importstatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_importstatement.add(imp.getTree());


                    // AST REWRITE
                    // elements: docs, imp
                    // token labels: 
                    // rule labels: retval, imp
                    // token list labels: 
                    // rule list labels: docs
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_imp=new RewriteRuleSubtreeStream(adaptor,"rule imp",imp!=null?imp.tree:null);
                    RewriteRuleSubtreeStream stream_docs=new RewriteRuleSubtreeStream(adaptor,"token docs",list_docs);
                    root_0 = (CommonTree)adaptor.nil();
                    // 135:31: -> ^( AGSTATEMENT $imp ( $docs)* )
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:135:34: ^( AGSTATEMENT $imp ( $docs)* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSTATEMENT, "AGSTATEMENT"), root_1);

                        adaptor.addChild(root_1, stream_imp.nextTree());
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:135:55: ( $docs)*
                        while ( stream_docs.hasNext() ) {
                            adaptor.addChild(root_1, stream_docs.nextTree());

                        }
                        stream_docs.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:136:9: ass= assignment
                    {
                    pushFollow(FOLLOW_assignment_in_statement668);
                    ass=assignment();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignment.add(ass.getTree());


                    // AST REWRITE
                    // elements: docs, ass
                    // token labels: 
                    // rule labels: retval, ass
                    // token list labels: 
                    // rule list labels: docs
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_ass=new RewriteRuleSubtreeStream(adaptor,"rule ass",ass!=null?ass.tree:null);
                    RewriteRuleSubtreeStream stream_docs=new RewriteRuleSubtreeStream(adaptor,"token docs",list_docs);
                    root_0 = (CommonTree)adaptor.nil();
                    // 136:31: -> ^( AGSTATEMENT $ass ( $docs)* )
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:136:34: ^( AGSTATEMENT $ass ( $docs)* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSTATEMENT, "AGSTATEMENT"), root_1);

                        adaptor.addChild(root_1, stream_ass.nextTree());
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:136:55: ( $docs)*
                        while ( stream_docs.hasNext() ) {
                            adaptor.addChild(root_1, stream_docs.nextTree());

                        }
                        stream_docs.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:137:9: exp= expression
                    {
                    pushFollow(FOLLOW_expression_in_statement702);
                    exp=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_expression.add(exp.getTree());


                    // AST REWRITE
                    // elements: docs, exp
                    // token labels: 
                    // rule labels: exp, retval
                    // token list labels: 
                    // rule list labels: docs
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_exp=new RewriteRuleSubtreeStream(adaptor,"rule exp",exp!=null?exp.tree:null);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_docs=new RewriteRuleSubtreeStream(adaptor,"token docs",list_docs);
                    root_0 = (CommonTree)adaptor.nil();
                    // 137:31: -> ^( AGSTATEMENT $exp ( $docs)* )
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:137:34: ^( AGSTATEMENT $exp ( $docs)* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSTATEMENT, "AGSTATEMENT"), root_1);

                        adaptor.addChild(root_1, stream_exp.nextTree());
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:137:55: ( $docs)*
                        while ( stream_docs.hasNext() ) {
                            adaptor.addChild(root_1, stream_docs.nextTree());

                        }
                        stream_docs.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "statement"

    public static class documentation_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "documentation"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:144:1: documentation : (s= SINGLE_LINE_DOC -> ^( AGDOCUMENTATION $s) | m= MULTI_LINE_DOC -> ^( AGDOCUMENTATION $m) ) ;
    public final AmbientTalkParser.documentation_return documentation() throws RecognitionException {
        AmbientTalkParser.documentation_return retval = new AmbientTalkParser.documentation_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token s=null;
        Token m=null;

        CommonTree s_tree=null;
        CommonTree m_tree=null;
        RewriteRuleTokenStream stream_MULTI_LINE_DOC=new RewriteRuleTokenStream(adaptor,"token MULTI_LINE_DOC");
        RewriteRuleTokenStream stream_SINGLE_LINE_DOC=new RewriteRuleTokenStream(adaptor,"token SINGLE_LINE_DOC");

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:145:3: ( (s= SINGLE_LINE_DOC -> ^( AGDOCUMENTATION $s) | m= MULTI_LINE_DOC -> ^( AGDOCUMENTATION $m) ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:145:5: (s= SINGLE_LINE_DOC -> ^( AGDOCUMENTATION $s) | m= MULTI_LINE_DOC -> ^( AGDOCUMENTATION $m) )
            {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:145:5: (s= SINGLE_LINE_DOC -> ^( AGDOCUMENTATION $s) | m= MULTI_LINE_DOC -> ^( AGDOCUMENTATION $m) )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==SINGLE_LINE_DOC) ) {
                alt6=1;
            }
            else if ( (LA6_0==MULTI_LINE_DOC) ) {
                alt6=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:145:7: s= SINGLE_LINE_DOC
                    {
                    s=(Token)match(input,SINGLE_LINE_DOC,FOLLOW_SINGLE_LINE_DOC_in_documentation752); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SINGLE_LINE_DOC.add(s);



                    // AST REWRITE
                    // elements: s
                    // token labels: s
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_s=new RewriteRuleTokenStream(adaptor,"token s",s);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 145:25: -> ^( AGDOCUMENTATION $s)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:145:28: ^( AGDOCUMENTATION $s)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGDOCUMENTATION, "AGDOCUMENTATION"), root_1);

                        adaptor.addChild(root_1, stream_s.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:146:7: m= MULTI_LINE_DOC
                    {
                    m=(Token)match(input,MULTI_LINE_DOC,FOLLOW_MULTI_LINE_DOC_in_documentation771); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MULTI_LINE_DOC.add(m);



                    // AST REWRITE
                    // elements: m
                    // token labels: m
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_m=new RewriteRuleTokenStream(adaptor,"token m",m);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 146:25: -> ^( AGDOCUMENTATION $m)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:146:28: ^( AGDOCUMENTATION $m)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGDOCUMENTATION, "AGDOCUMENTATION"), root_1);

                        adaptor.addChild(root_1, stream_m.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "documentation"

    public static class definition_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "definition"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:168:1: definition : lc= DEF (fld= field_definition -> ^( AGDEFINITION[$lc,\"definition\"] $fld) | key= keyworded_definition -> ^( AGDEFINITION[$lc,\"definition\"] $key) | can= canonical_definition -> ^( AGDEFINITION[$lc,\"definition\"] $can) | mul= multivalue_definition -> ^( AGDEFINITION[$lc,\"definition\"] $mul) | tab= table_definition -> ^( AGDEFINITION[$lc,\"definition\"] $tab) | ext= external_definition -> ^( AGDEFINITION[$lc,\"definition\"] $ext) ) ;
    public final AmbientTalkParser.definition_return definition() throws RecognitionException {
        AmbientTalkParser.definition_return retval = new AmbientTalkParser.definition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        AmbientTalkParser.field_definition_return fld = null;

        AmbientTalkParser.keyworded_definition_return key = null;

        AmbientTalkParser.canonical_definition_return can = null;

        AmbientTalkParser.multivalue_definition_return mul = null;

        AmbientTalkParser.table_definition_return tab = null;

        AmbientTalkParser.external_definition_return ext = null;


        CommonTree lc_tree=null;
        RewriteRuleTokenStream stream_DEF=new RewriteRuleTokenStream(adaptor,"token DEF");
        RewriteRuleSubtreeStream stream_multivalue_definition=new RewriteRuleSubtreeStream(adaptor,"rule multivalue_definition");
        RewriteRuleSubtreeStream stream_table_definition=new RewriteRuleSubtreeStream(adaptor,"rule table_definition");
        RewriteRuleSubtreeStream stream_keyworded_definition=new RewriteRuleSubtreeStream(adaptor,"rule keyworded_definition");
        RewriteRuleSubtreeStream stream_field_definition=new RewriteRuleSubtreeStream(adaptor,"rule field_definition");
        RewriteRuleSubtreeStream stream_canonical_definition=new RewriteRuleSubtreeStream(adaptor,"rule canonical_definition");
        RewriteRuleSubtreeStream stream_external_definition=new RewriteRuleSubtreeStream(adaptor,"rule external_definition");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:169:3: (lc= DEF (fld= field_definition -> ^( AGDEFINITION[$lc,\"definition\"] $fld) | key= keyworded_definition -> ^( AGDEFINITION[$lc,\"definition\"] $key) | can= canonical_definition -> ^( AGDEFINITION[$lc,\"definition\"] $can) | mul= multivalue_definition -> ^( AGDEFINITION[$lc,\"definition\"] $mul) | tab= table_definition -> ^( AGDEFINITION[$lc,\"definition\"] $tab) | ext= external_definition -> ^( AGDEFINITION[$lc,\"definition\"] $ext) ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:169:5: lc= DEF (fld= field_definition -> ^( AGDEFINITION[$lc,\"definition\"] $fld) | key= keyworded_definition -> ^( AGDEFINITION[$lc,\"definition\"] $key) | can= canonical_definition -> ^( AGDEFINITION[$lc,\"definition\"] $can) | mul= multivalue_definition -> ^( AGDEFINITION[$lc,\"definition\"] $mul) | tab= table_definition -> ^( AGDEFINITION[$lc,\"definition\"] $tab) | ext= external_definition -> ^( AGDEFINITION[$lc,\"definition\"] $ext) )
            {
            lc=(Token)match(input,DEF,FOLLOW_DEF_in_definition821); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DEF.add(lc);

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:170:5: (fld= field_definition -> ^( AGDEFINITION[$lc,\"definition\"] $fld) | key= keyworded_definition -> ^( AGDEFINITION[$lc,\"definition\"] $key) | can= canonical_definition -> ^( AGDEFINITION[$lc,\"definition\"] $can) | mul= multivalue_definition -> ^( AGDEFINITION[$lc,\"definition\"] $mul) | tab= table_definition -> ^( AGDEFINITION[$lc,\"definition\"] $tab) | ext= external_definition -> ^( AGDEFINITION[$lc,\"definition\"] $ext) )
            int alt7=6;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:170:7: fld= field_definition
                    {
                    pushFollow(FOLLOW_field_definition_in_definition831);
                    fld=field_definition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_field_definition.add(fld.getTree());


                    // AST REWRITE
                    // elements: fld
                    // token labels: 
                    // rule labels: retval, fld
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_fld=new RewriteRuleSubtreeStream(adaptor,"rule fld",fld!=null?fld.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 170:47: -> ^( AGDEFINITION[$lc,\"definition\"] $fld)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:170:50: ^( AGDEFINITION[$lc,\"definition\"] $fld)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGDEFINITION, lc, "definition"), root_1);

                        adaptor.addChild(root_1, stream_fld.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:171:7: key= keyworded_definition
                    {
                    pushFollow(FOLLOW_keyworded_definition_in_definition871);
                    key=keyworded_definition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_keyworded_definition.add(key.getTree());


                    // AST REWRITE
                    // elements: key
                    // token labels: 
                    // rule labels: retval, key
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_key=new RewriteRuleSubtreeStream(adaptor,"rule key",key!=null?key.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 171:47: -> ^( AGDEFINITION[$lc,\"definition\"] $key)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:171:50: ^( AGDEFINITION[$lc,\"definition\"] $key)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGDEFINITION, lc, "definition"), root_1);

                        adaptor.addChild(root_1, stream_key.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:172:7: can= canonical_definition
                    {
                    pushFollow(FOLLOW_canonical_definition_in_definition907);
                    can=canonical_definition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_canonical_definition.add(can.getTree());


                    // AST REWRITE
                    // elements: can
                    // token labels: 
                    // rule labels: retval, can
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_can=new RewriteRuleSubtreeStream(adaptor,"rule can",can!=null?can.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 172:47: -> ^( AGDEFINITION[$lc,\"definition\"] $can)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:172:50: ^( AGDEFINITION[$lc,\"definition\"] $can)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGDEFINITION, lc, "definition"), root_1);

                        adaptor.addChild(root_1, stream_can.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:173:7: mul= multivalue_definition
                    {
                    pushFollow(FOLLOW_multivalue_definition_in_definition943);
                    mul=multivalue_definition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_multivalue_definition.add(mul.getTree());


                    // AST REWRITE
                    // elements: mul
                    // token labels: 
                    // rule labels: retval, mul
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_mul=new RewriteRuleSubtreeStream(adaptor,"rule mul",mul!=null?mul.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 173:47: -> ^( AGDEFINITION[$lc,\"definition\"] $mul)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:173:50: ^( AGDEFINITION[$lc,\"definition\"] $mul)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGDEFINITION, lc, "definition"), root_1);

                        adaptor.addChild(root_1, stream_mul.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:174:7: tab= table_definition
                    {
                    pushFollow(FOLLOW_table_definition_in_definition978);
                    tab=table_definition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_table_definition.add(tab.getTree());


                    // AST REWRITE
                    // elements: tab
                    // token labels: 
                    // rule labels: retval, tab
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_tab=new RewriteRuleSubtreeStream(adaptor,"rule tab",tab!=null?tab.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 174:47: -> ^( AGDEFINITION[$lc,\"definition\"] $tab)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:174:50: ^( AGDEFINITION[$lc,\"definition\"] $tab)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGDEFINITION, lc, "definition"), root_1);

                        adaptor.addChild(root_1, stream_tab.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:175:7: ext= external_definition
                    {
                    pushFollow(FOLLOW_external_definition_in_definition1018);
                    ext=external_definition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_external_definition.add(ext.getTree());


                    // AST REWRITE
                    // elements: ext
                    // token labels: 
                    // rule labels: retval, ext
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_ext=new RewriteRuleSubtreeStream(adaptor,"rule ext",ext!=null?ext.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 175:47: -> ^( AGDEFINITION[$lc,\"definition\"] $ext)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:175:50: ^( AGDEFINITION[$lc,\"definition\"] $ext)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGDEFINITION, lc, "definition"), root_1);

                        adaptor.addChild(root_1, stream_ext.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "definition"

    public static class field_definition_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "field_definition"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:180:1: field_definition : nam= variable_or_assignment ( EQL exp= expression )? -> ^( AGFIELDDEF[\"field-definition\"] $nam ( $exp)? ) ;
    public final AmbientTalkParser.field_definition_return field_definition() throws RecognitionException {
        AmbientTalkParser.field_definition_return retval = new AmbientTalkParser.field_definition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQL7=null;
        AmbientTalkParser.variable_or_assignment_return nam = null;

        AmbientTalkParser.expression_return exp = null;


        CommonTree EQL7_tree=null;
        RewriteRuleTokenStream stream_EQL=new RewriteRuleTokenStream(adaptor,"token EQL");
        RewriteRuleSubtreeStream stream_variable_or_assignment=new RewriteRuleSubtreeStream(adaptor,"rule variable_or_assignment");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:181:3: (nam= variable_or_assignment ( EQL exp= expression )? -> ^( AGFIELDDEF[\"field-definition\"] $nam ( $exp)? ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:181:5: nam= variable_or_assignment ( EQL exp= expression )?
            {
            pushFollow(FOLLOW_variable_or_assignment_in_field_definition1067);
            nam=variable_or_assignment();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_variable_or_assignment.add(nam.getTree());
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:181:32: ( EQL exp= expression )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==EQL) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:181:33: EQL exp= expression
                    {
                    EQL7=(Token)match(input,EQL,FOLLOW_EQL_in_field_definition1070); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQL.add(EQL7);

                    pushFollow(FOLLOW_expression_in_field_definition1074);
                    exp=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_expression.add(exp.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: nam, exp
            // token labels: 
            // rule labels: exp, retval, nam
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_exp=new RewriteRuleSubtreeStream(adaptor,"rule exp",exp!=null?exp.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_nam=new RewriteRuleSubtreeStream(adaptor,"rule nam",nam!=null?nam.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 182:5: -> ^( AGFIELDDEF[\"field-definition\"] $nam ( $exp)? )
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:182:8: ^( AGFIELDDEF[\"field-definition\"] $nam ( $exp)? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGFIELDDEF, "field-definition"), root_1);

                adaptor.addChild(root_1, stream_nam.nextTree());
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:182:46: ( $exp)?
                if ( stream_exp.hasNext() ) {
                    adaptor.addChild(root_1, stream_exp.nextTree());

                }
                stream_exp.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "field_definition"

    public static class keyworded_definition_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "keyworded_definition"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:188:1: keyworded_definition : 'keyworddef' ;
    public final AmbientTalkParser.keyworded_definition_return keyworded_definition() throws RecognitionException {
        AmbientTalkParser.keyworded_definition_return retval = new AmbientTalkParser.keyworded_definition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal8=null;

        CommonTree string_literal8_tree=null;

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:189:3: ( 'keyworddef' )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:189:5: 'keyworddef'
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal8=(Token)match(input,113,FOLLOW_113_in_keyworded_definition1118); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            string_literal8_tree = (CommonTree)adaptor.create(string_literal8);
            adaptor.addChild(root_0, string_literal8_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "keyworded_definition"

    public static class canonical_definition_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "canonical_definition"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:193:1: canonical_definition : nam= variable_or_assignment lc= LPR parameterlist RPR (ann= annotation )? ( LBC body= statementlist RBC )? -> ^( AGDEFFUN[$lc,\"canonical-definition\"] $nam $body) ;
    public final AmbientTalkParser.canonical_definition_return canonical_definition() throws RecognitionException {
        AmbientTalkParser.canonical_definition_return retval = new AmbientTalkParser.canonical_definition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token RPR10=null;
        Token LBC11=null;
        Token RBC12=null;
        AmbientTalkParser.variable_or_assignment_return nam = null;

        AmbientTalkParser.annotation_return ann = null;

        AmbientTalkParser.statementlist_return body = null;

        AmbientTalkParser.parameterlist_return parameterlist9 = null;


        CommonTree lc_tree=null;
        CommonTree RPR10_tree=null;
        CommonTree LBC11_tree=null;
        CommonTree RBC12_tree=null;
        RewriteRuleTokenStream stream_RPR=new RewriteRuleTokenStream(adaptor,"token RPR");
        RewriteRuleTokenStream stream_LPR=new RewriteRuleTokenStream(adaptor,"token LPR");
        RewriteRuleTokenStream stream_RBC=new RewriteRuleTokenStream(adaptor,"token RBC");
        RewriteRuleTokenStream stream_LBC=new RewriteRuleTokenStream(adaptor,"token LBC");
        RewriteRuleSubtreeStream stream_variable_or_assignment=new RewriteRuleSubtreeStream(adaptor,"rule variable_or_assignment");
        RewriteRuleSubtreeStream stream_parameterlist=new RewriteRuleSubtreeStream(adaptor,"rule parameterlist");
        RewriteRuleSubtreeStream stream_annotation=new RewriteRuleSubtreeStream(adaptor,"rule annotation");
        RewriteRuleSubtreeStream stream_statementlist=new RewriteRuleSubtreeStream(adaptor,"rule statementlist");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:194:3: (nam= variable_or_assignment lc= LPR parameterlist RPR (ann= annotation )? ( LBC body= statementlist RBC )? -> ^( AGDEFFUN[$lc,\"canonical-definition\"] $nam $body) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:194:5: nam= variable_or_assignment lc= LPR parameterlist RPR (ann= annotation )? ( LBC body= statementlist RBC )?
            {
            pushFollow(FOLLOW_variable_or_assignment_in_canonical_definition1134);
            nam=variable_or_assignment();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_variable_or_assignment.add(nam.getTree());
            lc=(Token)match(input,LPR,FOLLOW_LPR_in_canonical_definition1142); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPR.add(lc);

            pushFollow(FOLLOW_parameterlist_in_canonical_definition1144);
            parameterlist9=parameterlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_parameterlist.add(parameterlist9.getTree());
            RPR10=(Token)match(input,RPR,FOLLOW_RPR_in_canonical_definition1146); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPR.add(RPR10);

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:196:8: (ann= annotation )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==CAT) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:196:8: ann= annotation
                    {
                    pushFollow(FOLLOW_annotation_in_canonical_definition1154);
                    ann=annotation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_annotation.add(ann.getTree());

                    }
                    break;

            }

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:197:5: ( LBC body= statementlist RBC )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==LBC) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:197:7: LBC body= statementlist RBC
                    {
                    LBC11=(Token)match(input,LBC,FOLLOW_LBC_in_canonical_definition1163); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBC.add(LBC11);

                    pushFollow(FOLLOW_statementlist_in_canonical_definition1167);
                    body=statementlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_statementlist.add(body.getTree());
                    RBC12=(Token)match(input,RBC,FOLLOW_RBC_in_canonical_definition1169); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBC.add(RBC12);


                    }
                    break;

            }



            // AST REWRITE
            // elements: body, nam
            // token labels: 
            // rule labels: body, retval, nam
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_body=new RewriteRuleSubtreeStream(adaptor,"rule body",body!=null?body.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_nam=new RewriteRuleSubtreeStream(adaptor,"rule nam",nam!=null?nam.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 198:5: -> ^( AGDEFFUN[$lc,\"canonical-definition\"] $nam $body)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:198:8: ^( AGDEFFUN[$lc,\"canonical-definition\"] $nam $body)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGDEFFUN, lc, "canonical-definition"), root_1);

                adaptor.addChild(root_1, stream_nam.nextTree());
                adaptor.addChild(root_1, stream_body.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "canonical_definition"

    public static class multivalue_definition_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "multivalue_definition"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:203:1: multivalue_definition : lc= LBR pars= parameterlist RBR ( EQL vals= expression )? -> ^( AGMULTIDEF[$lc,\"multival-definition\"] $pars ( $vals)? ) ;
    public final AmbientTalkParser.multivalue_definition_return multivalue_definition() throws RecognitionException {
        AmbientTalkParser.multivalue_definition_return retval = new AmbientTalkParser.multivalue_definition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token RBR13=null;
        Token EQL14=null;
        AmbientTalkParser.parameterlist_return pars = null;

        AmbientTalkParser.expression_return vals = null;


        CommonTree lc_tree=null;
        CommonTree RBR13_tree=null;
        CommonTree EQL14_tree=null;
        RewriteRuleTokenStream stream_EQL=new RewriteRuleTokenStream(adaptor,"token EQL");
        RewriteRuleTokenStream stream_RBR=new RewriteRuleTokenStream(adaptor,"token RBR");
        RewriteRuleTokenStream stream_LBR=new RewriteRuleTokenStream(adaptor,"token LBR");
        RewriteRuleSubtreeStream stream_parameterlist=new RewriteRuleSubtreeStream(adaptor,"rule parameterlist");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:204:3: (lc= LBR pars= parameterlist RBR ( EQL vals= expression )? -> ^( AGMULTIDEF[$lc,\"multival-definition\"] $pars ( $vals)? ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:204:5: lc= LBR pars= parameterlist RBR ( EQL vals= expression )?
            {
            lc=(Token)match(input,LBR,FOLLOW_LBR_in_multivalue_definition1208); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBR.add(lc);

            pushFollow(FOLLOW_parameterlist_in_multivalue_definition1212);
            pars=parameterlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_parameterlist.add(pars.getTree());
            RBR13=(Token)match(input,RBR,FOLLOW_RBR_in_multivalue_definition1214); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBR.add(RBR13);

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:204:35: ( EQL vals= expression )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==EQL) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:204:36: EQL vals= expression
                    {
                    EQL14=(Token)match(input,EQL,FOLLOW_EQL_in_multivalue_definition1217); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQL.add(EQL14);

                    pushFollow(FOLLOW_expression_in_multivalue_definition1221);
                    vals=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_expression.add(vals.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: pars, vals
            // token labels: 
            // rule labels: retval, vals, pars
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_vals=new RewriteRuleSubtreeStream(adaptor,"rule vals",vals!=null?vals.tree:null);
            RewriteRuleSubtreeStream stream_pars=new RewriteRuleSubtreeStream(adaptor,"rule pars",pars!=null?pars.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 205:5: -> ^( AGMULTIDEF[$lc,\"multival-definition\"] $pars ( $vals)? )
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:205:8: ^( AGMULTIDEF[$lc,\"multival-definition\"] $pars ( $vals)? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGMULTIDEF, lc, "multival-definition"), root_1);

                adaptor.addChild(root_1, stream_pars.nextTree());
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:205:54: ( $vals)?
                if ( stream_vals.hasNext() ) {
                    adaptor.addChild(root_1, stream_vals.nextTree());

                }
                stream_vals.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "multivalue_definition"

    public static class table_definition_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "table_definition"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:209:1: table_definition : nam= variable_or_assignment lc= LBR siz= expression RBR ( LBC init= statementlist RBC )? -> ^( AGTABLEDEF[$lc,\"table-definition\"] $nam $siz ( $init)? ) ;
    public final AmbientTalkParser.table_definition_return table_definition() throws RecognitionException {
        AmbientTalkParser.table_definition_return retval = new AmbientTalkParser.table_definition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token RBR15=null;
        Token LBC16=null;
        Token RBC17=null;
        AmbientTalkParser.variable_or_assignment_return nam = null;

        AmbientTalkParser.expression_return siz = null;

        AmbientTalkParser.statementlist_return init = null;


        CommonTree lc_tree=null;
        CommonTree RBR15_tree=null;
        CommonTree LBC16_tree=null;
        CommonTree RBC17_tree=null;
        RewriteRuleTokenStream stream_RBC=new RewriteRuleTokenStream(adaptor,"token RBC");
        RewriteRuleTokenStream stream_RBR=new RewriteRuleTokenStream(adaptor,"token RBR");
        RewriteRuleTokenStream stream_LBR=new RewriteRuleTokenStream(adaptor,"token LBR");
        RewriteRuleTokenStream stream_LBC=new RewriteRuleTokenStream(adaptor,"token LBC");
        RewriteRuleSubtreeStream stream_variable_or_assignment=new RewriteRuleSubtreeStream(adaptor,"rule variable_or_assignment");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_statementlist=new RewriteRuleSubtreeStream(adaptor,"rule statementlist");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:210:3: (nam= variable_or_assignment lc= LBR siz= expression RBR ( LBC init= statementlist RBC )? -> ^( AGTABLEDEF[$lc,\"table-definition\"] $nam $siz ( $init)? ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:210:5: nam= variable_or_assignment lc= LBR siz= expression RBR ( LBC init= statementlist RBC )?
            {
            pushFollow(FOLLOW_variable_or_assignment_in_table_definition1257);
            nam=variable_or_assignment();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_variable_or_assignment.add(nam.getTree());
            lc=(Token)match(input,LBR,FOLLOW_LBR_in_table_definition1261); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBR.add(lc);

            pushFollow(FOLLOW_expression_in_table_definition1265);
            siz=expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expression.add(siz.getTree());
            RBR15=(Token)match(input,RBR,FOLLOW_RBR_in_table_definition1267); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBR.add(RBR15);

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:211:5: ( LBC init= statementlist RBC )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==LBC) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:211:7: LBC init= statementlist RBC
                    {
                    LBC16=(Token)match(input,LBC,FOLLOW_LBC_in_table_definition1275); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBC.add(LBC16);

                    pushFollow(FOLLOW_statementlist_in_table_definition1279);
                    init=statementlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_statementlist.add(init.getTree());
                    RBC17=(Token)match(input,RBC,FOLLOW_RBC_in_table_definition1281); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBC.add(RBC17);


                    }
                    break;

            }



            // AST REWRITE
            // elements: siz, init, nam
            // token labels: 
            // rule labels: retval, nam, init, siz
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_nam=new RewriteRuleSubtreeStream(adaptor,"rule nam",nam!=null?nam.tree:null);
            RewriteRuleSubtreeStream stream_init=new RewriteRuleSubtreeStream(adaptor,"rule init",init!=null?init.tree:null);
            RewriteRuleSubtreeStream stream_siz=new RewriteRuleSubtreeStream(adaptor,"rule siz",siz!=null?siz.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 212:5: -> ^( AGTABLEDEF[$lc,\"table-definition\"] $nam $siz ( $init)? )
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:212:8: ^( AGTABLEDEF[$lc,\"table-definition\"] $nam $siz ( $init)? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGTABLEDEF, lc, "table-definition"), root_1);

                adaptor.addChild(root_1, stream_nam.nextTree());
                adaptor.addChild(root_1, stream_siz.nextTree());
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:212:55: ( $init)?
                if ( stream_init.hasNext() ) {
                    adaptor.addChild(root_1, stream_init.nextTree());

                }
                stream_init.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "table_definition"

    public static class external_definition_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "external_definition"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:218:1: external_definition : rec= variable_or_assignment lc= DOT ( ( variable LPR )=>fun= canonical_definition -> ^( AGEXTERNDEFFUN[$lc,\"external-deffun\"] $rec $fun) | nam= variable ( EQL val= expression )? -> ^( AGEXTERNFIELDDEF[$lc,\"external-fielddef\"] $rec $nam ( $val)? ) ) ;
    public final AmbientTalkParser.external_definition_return external_definition() throws RecognitionException {
        AmbientTalkParser.external_definition_return retval = new AmbientTalkParser.external_definition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token EQL18=null;
        AmbientTalkParser.variable_or_assignment_return rec = null;

        AmbientTalkParser.canonical_definition_return fun = null;

        AmbientTalkParser.variable_return nam = null;

        AmbientTalkParser.expression_return val = null;


        CommonTree lc_tree=null;
        CommonTree EQL18_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleTokenStream stream_EQL=new RewriteRuleTokenStream(adaptor,"token EQL");
        RewriteRuleSubtreeStream stream_variable_or_assignment=new RewriteRuleSubtreeStream(adaptor,"rule variable_or_assignment");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_canonical_definition=new RewriteRuleSubtreeStream(adaptor,"rule canonical_definition");
        RewriteRuleSubtreeStream stream_variable=new RewriteRuleSubtreeStream(adaptor,"rule variable");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:219:3: (rec= variable_or_assignment lc= DOT ( ( variable LPR )=>fun= canonical_definition -> ^( AGEXTERNDEFFUN[$lc,\"external-deffun\"] $rec $fun) | nam= variable ( EQL val= expression )? -> ^( AGEXTERNFIELDDEF[$lc,\"external-fielddef\"] $rec $nam ( $val)? ) ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:219:5: rec= variable_or_assignment lc= DOT ( ( variable LPR )=>fun= canonical_definition -> ^( AGEXTERNDEFFUN[$lc,\"external-deffun\"] $rec $fun) | nam= variable ( EQL val= expression )? -> ^( AGEXTERNFIELDDEF[$lc,\"external-fielddef\"] $rec $nam ( $val)? ) )
            {
            pushFollow(FOLLOW_variable_or_assignment_in_external_definition1323);
            rec=variable_or_assignment();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_variable_or_assignment.add(rec.getTree());
            lc=(Token)match(input,DOT,FOLLOW_DOT_in_external_definition1327); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DOT.add(lc);

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:220:5: ( ( variable LPR )=>fun= canonical_definition -> ^( AGEXTERNDEFFUN[$lc,\"external-deffun\"] $rec $fun) | nam= variable ( EQL val= expression )? -> ^( AGEXTERNFIELDDEF[$lc,\"external-fielddef\"] $rec $nam ( $val)? ) )
            int alt14=2;
            alt14 = dfa14.predict(input);
            switch (alt14) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:220:7: ( variable LPR )=>fun= canonical_definition
                    {
                    pushFollow(FOLLOW_canonical_definition_in_external_definition1345);
                    fun=canonical_definition();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_canonical_definition.add(fun.getTree());


                    // AST REWRITE
                    // elements: rec, fun
                    // token labels: 
                    // rule labels: retval, rec, fun
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_rec=new RewriteRuleSubtreeStream(adaptor,"rule rec",rec!=null?rec.tree:null);
                    RewriteRuleSubtreeStream stream_fun=new RewriteRuleSubtreeStream(adaptor,"rule fun",fun!=null?fun.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 221:7: -> ^( AGEXTERNDEFFUN[$lc,\"external-deffun\"] $rec $fun)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:221:10: ^( AGEXTERNDEFFUN[$lc,\"external-deffun\"] $rec $fun)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGEXTERNDEFFUN, lc, "external-deffun"), root_1);

                        adaptor.addChild(root_1, stream_rec.nextTree());
                        adaptor.addChild(root_1, stream_fun.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:222:7: nam= variable ( EQL val= expression )?
                    {
                    pushFollow(FOLLOW_variable_in_external_definition1374);
                    nam=variable();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_variable.add(nam.getTree());
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:222:20: ( EQL val= expression )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==EQL) ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:222:21: EQL val= expression
                            {
                            EQL18=(Token)match(input,EQL,FOLLOW_EQL_in_external_definition1377); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQL.add(EQL18);

                            pushFollow(FOLLOW_expression_in_external_definition1381);
                            val=expression();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_expression.add(val.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: rec, val, nam
                    // token labels: 
                    // rule labels: val, retval, nam, rec
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_val=new RewriteRuleSubtreeStream(adaptor,"rule val",val!=null?val.tree:null);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_nam=new RewriteRuleSubtreeStream(adaptor,"rule nam",nam!=null?nam.tree:null);
                    RewriteRuleSubtreeStream stream_rec=new RewriteRuleSubtreeStream(adaptor,"rule rec",rec!=null?rec.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 223:7: -> ^( AGEXTERNFIELDDEF[$lc,\"external-fielddef\"] $rec $nam ( $val)? )
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:223:10: ^( AGEXTERNFIELDDEF[$lc,\"external-fielddef\"] $rec $nam ( $val)? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGEXTERNFIELDDEF, lc, "external-fielddef"), root_1);

                        adaptor.addChild(root_1, stream_rec.nextTree());
                        adaptor.addChild(root_1, stream_nam.nextTree());
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:223:64: ( $val)?
                        if ( stream_val.hasNext() ) {
                            adaptor.addChild(root_1, stream_val.nextTree());

                        }
                        stream_val.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "external_definition"

    public static class typedefinition_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "typedefinition"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:231:1: typedefinition : lc= TDEF nam= variable ( SST parents+= variable ( COM parents+= variable )* )? -> ^( AGTYPEDEFINITION $nam ( $parents)* ) ;
    public final AmbientTalkParser.typedefinition_return typedefinition() throws RecognitionException {
        AmbientTalkParser.typedefinition_return retval = new AmbientTalkParser.typedefinition_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token SST19=null;
        Token COM20=null;
        List list_parents=null;
        AmbientTalkParser.variable_return nam = null;

        RuleReturnScope parents = null;
        CommonTree lc_tree=null;
        CommonTree SST19_tree=null;
        CommonTree COM20_tree=null;
        RewriteRuleTokenStream stream_TDEF=new RewriteRuleTokenStream(adaptor,"token TDEF");
        RewriteRuleTokenStream stream_COM=new RewriteRuleTokenStream(adaptor,"token COM");
        RewriteRuleTokenStream stream_SST=new RewriteRuleTokenStream(adaptor,"token SST");
        RewriteRuleSubtreeStream stream_variable=new RewriteRuleSubtreeStream(adaptor,"rule variable");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:232:3: (lc= TDEF nam= variable ( SST parents+= variable ( COM parents+= variable )* )? -> ^( AGTYPEDEFINITION $nam ( $parents)* ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:232:5: lc= TDEF nam= variable ( SST parents+= variable ( COM parents+= variable )* )?
            {
            lc=(Token)match(input,TDEF,FOLLOW_TDEF_in_typedefinition1431); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_TDEF.add(lc);

            pushFollow(FOLLOW_variable_in_typedefinition1435);
            nam=variable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_variable.add(nam.getTree());
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:232:26: ( SST parents+= variable ( COM parents+= variable )* )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==SST) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:232:27: SST parents+= variable ( COM parents+= variable )*
                    {
                    SST19=(Token)match(input,SST,FOLLOW_SST_in_typedefinition1438); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SST.add(SST19);

                    pushFollow(FOLLOW_variable_in_typedefinition1442);
                    parents=variable();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_variable.add(parents.getTree());
                    if (list_parents==null) list_parents=new ArrayList();
                    list_parents.add(parents.getTree());

                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:232:49: ( COM parents+= variable )*
                    loop15:
                    do {
                        int alt15=2;
                        int LA15_0 = input.LA(1);

                        if ( (LA15_0==COM) ) {
                            alt15=1;
                        }


                        switch (alt15) {
                    	case 1 :
                    	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:232:50: COM parents+= variable
                    	    {
                    	    COM20=(Token)match(input,COM,FOLLOW_COM_in_typedefinition1445); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COM.add(COM20);

                    	    pushFollow(FOLLOW_variable_in_typedefinition1449);
                    	    parents=variable();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_variable.add(parents.getTree());
                    	    if (list_parents==null) list_parents=new ArrayList();
                    	    list_parents.add(parents.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop15;
                        }
                    } while (true);


                    }
                    break;

            }



            // AST REWRITE
            // elements: nam, parents
            // token labels: 
            // rule labels: retval, nam
            // token list labels: 
            // rule list labels: parents
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_nam=new RewriteRuleSubtreeStream(adaptor,"rule nam",nam!=null?nam.tree:null);
            RewriteRuleSubtreeStream stream_parents=new RewriteRuleSubtreeStream(adaptor,"token parents",list_parents);
            root_0 = (CommonTree)adaptor.nil();
            // 233:5: -> ^( AGTYPEDEFINITION $nam ( $parents)* )
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:233:8: ^( AGTYPEDEFINITION $nam ( $parents)* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGTYPEDEFINITION, "AGTYPEDEFINITION"), root_1);

                adaptor.addChild(root_1, stream_nam.nextTree());
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:233:32: ( $parents)*
                while ( stream_parents.hasNext() ) {
                    adaptor.addChild(root_1, stream_parents.nextTree());

                }
                stream_parents.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "typedefinition"

    public static class importstatement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "importstatement"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:244:1: importstatement : lc= IMPORT host= expression (alias= aliasbindings )? (exclude= excludelist )? -> ^( AGIMPORT[$lc,\"import\"] $host ( $alias)? ( $exclude)? ) ;
    public final AmbientTalkParser.importstatement_return importstatement() throws RecognitionException {
        AmbientTalkParser.importstatement_return retval = new AmbientTalkParser.importstatement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        AmbientTalkParser.expression_return host = null;

        AmbientTalkParser.aliasbindings_return alias = null;

        AmbientTalkParser.excludelist_return exclude = null;


        CommonTree lc_tree=null;
        RewriteRuleTokenStream stream_IMPORT=new RewriteRuleTokenStream(adaptor,"token IMPORT");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_excludelist=new RewriteRuleSubtreeStream(adaptor,"rule excludelist");
        RewriteRuleSubtreeStream stream_aliasbindings=new RewriteRuleSubtreeStream(adaptor,"rule aliasbindings");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:245:3: (lc= IMPORT host= expression (alias= aliasbindings )? (exclude= excludelist )? -> ^( AGIMPORT[$lc,\"import\"] $host ( $alias)? ( $exclude)? ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:245:5: lc= IMPORT host= expression (alias= aliasbindings )? (exclude= excludelist )?
            {
            lc=(Token)match(input,IMPORT,FOLLOW_IMPORT_in_importstatement1493); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IMPORT.add(lc);

            pushFollow(FOLLOW_expression_in_importstatement1497);
            host=expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expression.add(host.getTree());
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:245:36: (alias= aliasbindings )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==ALIAS) ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:245:36: alias= aliasbindings
                    {
                    pushFollow(FOLLOW_aliasbindings_in_importstatement1501);
                    alias=aliasbindings();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_aliasbindings.add(alias.getTree());

                    }
                    break;

            }

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:245:59: (exclude= excludelist )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==EXCLUDE) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:245:59: exclude= excludelist
                    {
                    pushFollow(FOLLOW_excludelist_in_importstatement1506);
                    exclude=excludelist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_excludelist.add(exclude.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: host, exclude, alias
            // token labels: 
            // rule labels: retval, host, alias, exclude
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_host=new RewriteRuleSubtreeStream(adaptor,"rule host",host!=null?host.tree:null);
            RewriteRuleSubtreeStream stream_alias=new RewriteRuleSubtreeStream(adaptor,"rule alias",alias!=null?alias.tree:null);
            RewriteRuleSubtreeStream stream_exclude=new RewriteRuleSubtreeStream(adaptor,"rule exclude",exclude!=null?exclude.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 246:5: -> ^( AGIMPORT[$lc,\"import\"] $host ( $alias)? ( $exclude)? )
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:246:8: ^( AGIMPORT[$lc,\"import\"] $host ( $alias)? ( $exclude)? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGIMPORT, lc, "import"), root_1);

                adaptor.addChild(root_1, stream_host.nextTree());
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:246:39: ( $alias)?
                if ( stream_alias.hasNext() ) {
                    adaptor.addChild(root_1, stream_alias.nextTree());

                }
                stream_alias.reset();
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:246:47: ( $exclude)?
                if ( stream_exclude.hasNext() ) {
                    adaptor.addChild(root_1, stream_exclude.nextTree());

                }
                stream_exclude.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "importstatement"

    public static class aliasbindings_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "aliasbindings"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:248:1: aliasbindings : lc= ALIAS ( ( HSH )=>lc= ALIAS uqo= unquotation -> ^( AGALIASUNQUOTATION[$lc,\"alias-unquotation\"] $uqo) | bnd+= aliasbinding ( COM bnd+= aliasbinding )* -> ^( AGTABLE[$lc,\"table\"] $bnd) ) ;
    public final AmbientTalkParser.aliasbindings_return aliasbindings() throws RecognitionException {
        AmbientTalkParser.aliasbindings_return retval = new AmbientTalkParser.aliasbindings_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token COM21=null;
        List list_bnd=null;
        AmbientTalkParser.unquotation_return uqo = null;

        RuleReturnScope bnd = null;
        CommonTree lc_tree=null;
        CommonTree COM21_tree=null;
        RewriteRuleTokenStream stream_COM=new RewriteRuleTokenStream(adaptor,"token COM");
        RewriteRuleTokenStream stream_ALIAS=new RewriteRuleTokenStream(adaptor,"token ALIAS");
        RewriteRuleSubtreeStream stream_aliasbinding=new RewriteRuleSubtreeStream(adaptor,"rule aliasbinding");
        RewriteRuleSubtreeStream stream_unquotation=new RewriteRuleSubtreeStream(adaptor,"rule unquotation");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:249:3: (lc= ALIAS ( ( HSH )=>lc= ALIAS uqo= unquotation -> ^( AGALIASUNQUOTATION[$lc,\"alias-unquotation\"] $uqo) | bnd+= aliasbinding ( COM bnd+= aliasbinding )* -> ^( AGTABLE[$lc,\"table\"] $bnd) ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:249:5: lc= ALIAS ( ( HSH )=>lc= ALIAS uqo= unquotation -> ^( AGALIASUNQUOTATION[$lc,\"alias-unquotation\"] $uqo) | bnd+= aliasbinding ( COM bnd+= aliasbinding )* -> ^( AGTABLE[$lc,\"table\"] $bnd) )
            {
            lc=(Token)match(input,ALIAS,FOLLOW_ALIAS_in_aliasbindings1543); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ALIAS.add(lc);

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:250:5: ( ( HSH )=>lc= ALIAS uqo= unquotation -> ^( AGALIASUNQUOTATION[$lc,\"alias-unquotation\"] $uqo) | bnd+= aliasbinding ( COM bnd+= aliasbinding )* -> ^( AGTABLE[$lc,\"table\"] $bnd) )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==ALIAS) && (synpred2_AmbientTalk())) {
                alt20=1;
            }
            else if ( ((LA20_0>=NAME && LA20_0<=POW)||LA20_0==SELF) ) {
                alt20=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:250:7: ( HSH )=>lc= ALIAS uqo= unquotation
                    {
                    lc=(Token)match(input,ALIAS,FOLLOW_ALIAS_in_aliasbindings1559); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ALIAS.add(lc);

                    pushFollow(FOLLOW_unquotation_in_aliasbindings1563);
                    uqo=unquotation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_unquotation.add(uqo.getTree());


                    // AST REWRITE
                    // elements: uqo
                    // token labels: 
                    // rule labels: uqo, retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_uqo=new RewriteRuleSubtreeStream(adaptor,"rule uqo",uqo!=null?uqo.tree:null);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 251:7: -> ^( AGALIASUNQUOTATION[$lc,\"alias-unquotation\"] $uqo)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:251:10: ^( AGALIASUNQUOTATION[$lc,\"alias-unquotation\"] $uqo)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGALIASUNQUOTATION, lc, "alias-unquotation"), root_1);

                        adaptor.addChild(root_1, stream_uqo.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:252:7: bnd+= aliasbinding ( COM bnd+= aliasbinding )*
                    {
                    pushFollow(FOLLOW_aliasbinding_in_aliasbindings1589);
                    bnd=aliasbinding();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_aliasbinding.add(bnd.getTree());
                    if (list_bnd==null) list_bnd=new ArrayList();
                    list_bnd.add(bnd.getTree());

                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:252:25: ( COM bnd+= aliasbinding )*
                    loop19:
                    do {
                        int alt19=2;
                        int LA19_0 = input.LA(1);

                        if ( (LA19_0==COM) ) {
                            alt19=1;
                        }


                        switch (alt19) {
                    	case 1 :
                    	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:252:26: COM bnd+= aliasbinding
                    	    {
                    	    COM21=(Token)match(input,COM,FOLLOW_COM_in_aliasbindings1592); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COM.add(COM21);

                    	    pushFollow(FOLLOW_aliasbinding_in_aliasbindings1596);
                    	    bnd=aliasbinding();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_aliasbinding.add(bnd.getTree());
                    	    if (list_bnd==null) list_bnd=new ArrayList();
                    	    list_bnd.add(bnd.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop19;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: bnd
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: bnd
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_bnd=new RewriteRuleSubtreeStream(adaptor,"token bnd",list_bnd);
                    root_0 = (CommonTree)adaptor.nil();
                    // 253:7: -> ^( AGTABLE[$lc,\"table\"] $bnd)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:253:10: ^( AGTABLE[$lc,\"table\"] $bnd)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGTABLE, lc, "table"), root_1);

                        adaptor.addChild(root_1, stream_bnd.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "aliasbindings"

    public static class aliasbinding_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "aliasbinding"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:256:1: aliasbinding : name1= importname lc= EQL name2= importname -> ^( AGALIAS[$lc,\"aliasbinding\"] $name1 $name2) ;
    public final AmbientTalkParser.aliasbinding_return aliasbinding() throws RecognitionException {
        AmbientTalkParser.aliasbinding_return retval = new AmbientTalkParser.aliasbinding_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        AmbientTalkParser.importname_return name1 = null;

        AmbientTalkParser.importname_return name2 = null;


        CommonTree lc_tree=null;
        RewriteRuleTokenStream stream_EQL=new RewriteRuleTokenStream(adaptor,"token EQL");
        RewriteRuleSubtreeStream stream_importname=new RewriteRuleSubtreeStream(adaptor,"rule importname");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:257:3: (name1= importname lc= EQL name2= importname -> ^( AGALIAS[$lc,\"aliasbinding\"] $name1 $name2) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:257:5: name1= importname lc= EQL name2= importname
            {
            pushFollow(FOLLOW_importname_in_aliasbinding1634);
            name1=importname();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_importname.add(name1.getTree());
            lc=(Token)match(input,EQL,FOLLOW_EQL_in_aliasbinding1638); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQL.add(lc);

            pushFollow(FOLLOW_importname_in_aliasbinding1642);
            name2=importname();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_importname.add(name2.getTree());


            // AST REWRITE
            // elements: name2, name1
            // token labels: 
            // rule labels: retval, name1, name2
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_name1=new RewriteRuleSubtreeStream(adaptor,"rule name1",name1!=null?name1.tree:null);
            RewriteRuleSubtreeStream stream_name2=new RewriteRuleSubtreeStream(adaptor,"rule name2",name2!=null?name2.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 258:5: -> ^( AGALIAS[$lc,\"aliasbinding\"] $name1 $name2)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:258:8: ^( AGALIAS[$lc,\"aliasbinding\"] $name1 $name2)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGALIAS, lc, "aliasbinding"), root_1);

                adaptor.addChild(root_1, stream_name1.nextTree());
                adaptor.addChild(root_1, stream_name2.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "aliasbinding"

    public static class importname_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "importname"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:263:1: importname : variable ;
    public final AmbientTalkParser.importname_return importname() throws RecognitionException {
        AmbientTalkParser.importname_return retval = new AmbientTalkParser.importname_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.variable_return variable22 = null;



        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:264:3: ( variable )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:264:5: variable
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_variable_in_importname1674);
            variable22=variable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, variable22.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "importname"

    public static class excludelist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "excludelist"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:267:1: excludelist : ( ( EXCLUDE HSH )=> EXCLUDE uqo= unquotation | lc= EXCLUDE exc+= importname ( COM exc+= importname )* -> ^( AGTABLE[$lc,\"table\"] $exc) );
    public final AmbientTalkParser.excludelist_return excludelist() throws RecognitionException {
        AmbientTalkParser.excludelist_return retval = new AmbientTalkParser.excludelist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token EXCLUDE23=null;
        Token COM24=null;
        List list_exc=null;
        AmbientTalkParser.unquotation_return uqo = null;

        RuleReturnScope exc = null;
        CommonTree lc_tree=null;
        CommonTree EXCLUDE23_tree=null;
        CommonTree COM24_tree=null;
        RewriteRuleTokenStream stream_COM=new RewriteRuleTokenStream(adaptor,"token COM");
        RewriteRuleTokenStream stream_EXCLUDE=new RewriteRuleTokenStream(adaptor,"token EXCLUDE");
        RewriteRuleSubtreeStream stream_importname=new RewriteRuleSubtreeStream(adaptor,"rule importname");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:268:3: ( ( EXCLUDE HSH )=> EXCLUDE uqo= unquotation | lc= EXCLUDE exc+= importname ( COM exc+= importname )* -> ^( AGTABLE[$lc,\"table\"] $exc) )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==EXCLUDE) ) {
                int LA22_1 = input.LA(2);

                if ( (LA22_1==HSH) && (synpred3_AmbientTalk())) {
                    alt22=1;
                }
                else if ( ((LA22_1>=NAME && LA22_1<=POW)||LA22_1==SELF) ) {
                    alt22=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 22, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:268:5: ( EXCLUDE HSH )=> EXCLUDE uqo= unquotation
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    EXCLUDE23=(Token)match(input,EXCLUDE,FOLLOW_EXCLUDE_in_excludelist1697); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EXCLUDE23_tree = (CommonTree)adaptor.create(EXCLUDE23);
                    adaptor.addChild(root_0, EXCLUDE23_tree);
                    }
                    pushFollow(FOLLOW_unquotation_in_excludelist1701);
                    uqo=unquotation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, uqo.getTree());

                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:269:5: lc= EXCLUDE exc+= importname ( COM exc+= importname )*
                    {
                    lc=(Token)match(input,EXCLUDE,FOLLOW_EXCLUDE_in_excludelist1709); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EXCLUDE.add(lc);

                    pushFollow(FOLLOW_importname_in_excludelist1713);
                    exc=importname();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_importname.add(exc.getTree());
                    if (list_exc==null) list_exc=new ArrayList();
                    list_exc.add(exc.getTree());

                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:269:32: ( COM exc+= importname )*
                    loop21:
                    do {
                        int alt21=2;
                        int LA21_0 = input.LA(1);

                        if ( (LA21_0==COM) ) {
                            alt21=1;
                        }


                        switch (alt21) {
                    	case 1 :
                    	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:269:33: COM exc+= importname
                    	    {
                    	    COM24=(Token)match(input,COM,FOLLOW_COM_in_excludelist1716); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COM.add(COM24);

                    	    pushFollow(FOLLOW_importname_in_excludelist1720);
                    	    exc=importname();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_importname.add(exc.getTree());
                    	    if (list_exc==null) list_exc=new ArrayList();
                    	    list_exc.add(exc.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop21;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: exc
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: exc
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_exc=new RewriteRuleSubtreeStream(adaptor,"token exc",list_exc);
                    root_0 = (CommonTree)adaptor.nil();
                    // 270:5: -> ^( AGTABLE[$lc,\"table\"] $exc)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:270:8: ^( AGTABLE[$lc,\"table\"] $exc)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGTABLE, lc, "table"), root_1);

                        adaptor.addChild(root_1, stream_exc.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "excludelist"

    public static class assignment_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignment"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:277:1: assignment : ( assign_variable | assign_multi | assign_field );
    public final AmbientTalkParser.assignment_return assignment() throws RecognitionException {
        AmbientTalkParser.assignment_return retval = new AmbientTalkParser.assignment_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.assign_variable_return assign_variable25 = null;

        AmbientTalkParser.assign_multi_return assign_multi26 = null;

        AmbientTalkParser.assign_field_return assign_field27 = null;



        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:278:3: ( assign_variable | assign_multi | assign_field )
            int alt23=3;
            alt23 = dfa23.predict(input);
            switch (alt23) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:278:5: assign_variable
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assign_variable_in_assignment1753);
                    assign_variable25=assign_variable();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assign_variable25.getTree());

                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:281:5: assign_multi
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assign_multi_in_assignment1766);
                    assign_multi26=assign_multi();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assign_multi26.getTree());

                    }
                    break;
                case 3 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:282:5: assign_field
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assign_field_in_assignment1774);
                    assign_field27=assign_field();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assign_field27.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assignment"

    public static class assign_variable_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assign_variable"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:284:1: assign_variable : var= variable lc= EQL val= expression -> ^( AGVARIABLEASSIGNMENT[$lc,\"assign-variable\"] $var $val) ;
    public final AmbientTalkParser.assign_variable_return assign_variable() throws RecognitionException {
        AmbientTalkParser.assign_variable_return retval = new AmbientTalkParser.assign_variable_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        AmbientTalkParser.variable_return var = null;

        AmbientTalkParser.expression_return val = null;


        CommonTree lc_tree=null;
        RewriteRuleTokenStream stream_EQL=new RewriteRuleTokenStream(adaptor,"token EQL");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_variable=new RewriteRuleSubtreeStream(adaptor,"rule variable");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:285:3: (var= variable lc= EQL val= expression -> ^( AGVARIABLEASSIGNMENT[$lc,\"assign-variable\"] $var $val) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:285:5: var= variable lc= EQL val= expression
            {
            pushFollow(FOLLOW_variable_in_assign_variable1793);
            var=variable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_variable.add(var.getTree());
            lc=(Token)match(input,EQL,FOLLOW_EQL_in_assign_variable1797); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQL.add(lc);

            pushFollow(FOLLOW_expression_in_assign_variable1801);
            val=expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expression.add(val.getTree());


            // AST REWRITE
            // elements: val, var
            // token labels: 
            // rule labels: val, retval, var
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_val=new RewriteRuleSubtreeStream(adaptor,"rule val",val!=null?val.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_var=new RewriteRuleSubtreeStream(adaptor,"rule var",var!=null?var.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 286:5: -> ^( AGVARIABLEASSIGNMENT[$lc,\"assign-variable\"] $var $val)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:286:8: ^( AGVARIABLEASSIGNMENT[$lc,\"assign-variable\"] $var $val)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGVARIABLEASSIGNMENT, lc, "assign-variable"), root_1);

                adaptor.addChild(root_1, stream_var.nextTree());
                adaptor.addChild(root_1, stream_val.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assign_variable"

    public static class assign_table_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assign_table"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:288:1: assign_table : opr= NAME tab= tabulation lc= EQL val= expression -> ^( AGTABLEASSIGNMENT[$lc,\"assign-table\"] $opr $tab $val) ;
    public final AmbientTalkParser.assign_table_return assign_table() throws RecognitionException {
        AmbientTalkParser.assign_table_return retval = new AmbientTalkParser.assign_table_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token opr=null;
        Token lc=null;
        AmbientTalkParser.tabulation_return tab = null;

        AmbientTalkParser.expression_return val = null;


        CommonTree opr_tree=null;
        CommonTree lc_tree=null;
        RewriteRuleTokenStream stream_NAME=new RewriteRuleTokenStream(adaptor,"token NAME");
        RewriteRuleTokenStream stream_EQL=new RewriteRuleTokenStream(adaptor,"token EQL");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_tabulation=new RewriteRuleSubtreeStream(adaptor,"rule tabulation");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:290:3: (opr= NAME tab= tabulation lc= EQL val= expression -> ^( AGTABLEASSIGNMENT[$lc,\"assign-table\"] $opr $tab $val) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:290:5: opr= NAME tab= tabulation lc= EQL val= expression
            {
            opr=(Token)match(input,NAME,FOLLOW_NAME_in_assign_table1836); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_NAME.add(opr);

            pushFollow(FOLLOW_tabulation_in_assign_table1840);
            tab=tabulation();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tabulation.add(tab.getTree());
            lc=(Token)match(input,EQL,FOLLOW_EQL_in_assign_table1844); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQL.add(lc);

            pushFollow(FOLLOW_expression_in_assign_table1848);
            val=expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expression.add(val.getTree());


            // AST REWRITE
            // elements: opr, tab, val
            // token labels: opr
            // rule labels: val, retval, tab
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleTokenStream stream_opr=new RewriteRuleTokenStream(adaptor,"token opr",opr);
            RewriteRuleSubtreeStream stream_val=new RewriteRuleSubtreeStream(adaptor,"rule val",val!=null?val.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_tab=new RewriteRuleSubtreeStream(adaptor,"rule tab",tab!=null?tab.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 291:5: -> ^( AGTABLEASSIGNMENT[$lc,\"assign-table\"] $opr $tab $val)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:291:8: ^( AGTABLEASSIGNMENT[$lc,\"assign-table\"] $opr $tab $val)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGTABLEASSIGNMENT, lc, "assign-table"), root_1);

                adaptor.addChild(root_1, stream_opr.nextNode());
                adaptor.addChild(root_1, stream_tab.nextTree());
                adaptor.addChild(root_1, stream_val.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assign_table"

    public static class assign_multi_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assign_multi"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:293:1: assign_multi : LBR vars+= parameter ( COM vars+= parameter )* RBR lc= EQL vals+= commalist -> ^( AGMULTIASSIGNMENT[$lc,\"assign-multi\"] ( $vars)+ $vals) ;
    public final AmbientTalkParser.assign_multi_return assign_multi() throws RecognitionException {
        AmbientTalkParser.assign_multi_return retval = new AmbientTalkParser.assign_multi_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token LBR28=null;
        Token COM29=null;
        Token RBR30=null;
        List list_vars=null;
        List list_vals=null;
        RuleReturnScope vars = null;
        RuleReturnScope vals = null;
        CommonTree lc_tree=null;
        CommonTree LBR28_tree=null;
        CommonTree COM29_tree=null;
        CommonTree RBR30_tree=null;
        RewriteRuleTokenStream stream_EQL=new RewriteRuleTokenStream(adaptor,"token EQL");
        RewriteRuleTokenStream stream_RBR=new RewriteRuleTokenStream(adaptor,"token RBR");
        RewriteRuleTokenStream stream_COM=new RewriteRuleTokenStream(adaptor,"token COM");
        RewriteRuleTokenStream stream_LBR=new RewriteRuleTokenStream(adaptor,"token LBR");
        RewriteRuleSubtreeStream stream_parameter=new RewriteRuleSubtreeStream(adaptor,"rule parameter");
        RewriteRuleSubtreeStream stream_commalist=new RewriteRuleSubtreeStream(adaptor,"rule commalist");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:294:3: ( LBR vars+= parameter ( COM vars+= parameter )* RBR lc= EQL vals+= commalist -> ^( AGMULTIASSIGNMENT[$lc,\"assign-multi\"] ( $vars)+ $vals) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:294:5: LBR vars+= parameter ( COM vars+= parameter )* RBR lc= EQL vals+= commalist
            {
            LBR28=(Token)match(input,LBR,FOLLOW_LBR_in_assign_multi1881); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBR.add(LBR28);

            pushFollow(FOLLOW_parameter_in_assign_multi1891);
            vars=parameter();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_parameter.add(vars.getTree());
            if (list_vars==null) list_vars=new ArrayList();
            list_vars.add(vars.getTree());

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:295:23: ( COM vars+= parameter )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==COM) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:295:24: COM vars+= parameter
            	    {
            	    COM29=(Token)match(input,COM,FOLLOW_COM_in_assign_multi1894); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COM.add(COM29);

            	    pushFollow(FOLLOW_parameter_in_assign_multi1898);
            	    vars=parameter();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_parameter.add(vars.getTree());
            	    if (list_vars==null) list_vars=new ArrayList();
            	    list_vars.add(vars.getTree());


            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);

            RBR30=(Token)match(input,RBR,FOLLOW_RBR_in_assign_multi1906); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBR.add(RBR30);

            lc=(Token)match(input,EQL,FOLLOW_EQL_in_assign_multi1914); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQL.add(lc);

            pushFollow(FOLLOW_commalist_in_assign_multi1918);
            vals=commalist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_commalist.add(vals.getTree());
            if (list_vals==null) list_vals=new ArrayList();
            list_vals.add(vals.getTree());



            // AST REWRITE
            // elements: vals, vars
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: vars, vals
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_vars=new RewriteRuleSubtreeStream(adaptor,"token vars",list_vars);
            RewriteRuleSubtreeStream stream_vals=new RewriteRuleSubtreeStream(adaptor,"token vals",list_vals);
            root_0 = (CommonTree)adaptor.nil();
            // 298:5: -> ^( AGMULTIASSIGNMENT[$lc,\"assign-multi\"] ( $vars)+ $vals)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:298:8: ^( AGMULTIASSIGNMENT[$lc,\"assign-multi\"] ( $vars)+ $vals)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGMULTIASSIGNMENT, lc, "assign-multi"), root_1);

                if ( !(stream_vars.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_vars.hasNext() ) {
                    adaptor.addChild(root_1, stream_vars.nextTree());

                }
                stream_vars.reset();
                adaptor.addChild(root_1, stream_vals.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assign_multi"

    public static class assign_field_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assign_field"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:300:1: assign_field : opr= variable DOT var= variable lc= EQL val= expression -> ^( AGFIELDASSIGNMENT[$lc,\"assign-field\"] $opr $var $val) ;
    public final AmbientTalkParser.assign_field_return assign_field() throws RecognitionException {
        AmbientTalkParser.assign_field_return retval = new AmbientTalkParser.assign_field_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token DOT31=null;
        AmbientTalkParser.variable_return opr = null;

        AmbientTalkParser.variable_return var = null;

        AmbientTalkParser.expression_return val = null;


        CommonTree lc_tree=null;
        CommonTree DOT31_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleTokenStream stream_EQL=new RewriteRuleTokenStream(adaptor,"token EQL");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_variable=new RewriteRuleSubtreeStream(adaptor,"rule variable");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:303:3: (opr= variable DOT var= variable lc= EQL val= expression -> ^( AGFIELDASSIGNMENT[$lc,\"assign-field\"] $opr $var $val) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:303:5: opr= variable DOT var= variable lc= EQL val= expression
            {
            pushFollow(FOLLOW_variable_in_assign_field1957);
            opr=variable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_variable.add(opr.getTree());
            DOT31=(Token)match(input,DOT,FOLLOW_DOT_in_assign_field1959); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DOT.add(DOT31);

            pushFollow(FOLLOW_variable_in_assign_field1963);
            var=variable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_variable.add(var.getTree());
            lc=(Token)match(input,EQL,FOLLOW_EQL_in_assign_field1967); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQL.add(lc);

            pushFollow(FOLLOW_expression_in_assign_field1971);
            val=expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expression.add(val.getTree());


            // AST REWRITE
            // elements: opr, var, val
            // token labels: 
            // rule labels: val, retval, var, opr
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_val=new RewriteRuleSubtreeStream(adaptor,"rule val",val!=null?val.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_var=new RewriteRuleSubtreeStream(adaptor,"rule var",var!=null?var.tree:null);
            RewriteRuleSubtreeStream stream_opr=new RewriteRuleSubtreeStream(adaptor,"rule opr",opr!=null?opr.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 304:5: -> ^( AGFIELDASSIGNMENT[$lc,\"assign-field\"] $opr $var $val)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:304:8: ^( AGFIELDASSIGNMENT[$lc,\"assign-field\"] $opr $var $val)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGFIELDASSIGNMENT, lc, "assign-field"), root_1);

                adaptor.addChild(root_1, stream_opr.nextTree());
                adaptor.addChild(root_1, stream_var.nextTree());
                adaptor.addChild(root_1, stream_val.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assign_field"

    public static class expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expression"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:315:1: expression : comparand ( CMP comparand )* ;
    public final AmbientTalkParser.expression_return expression() throws RecognitionException {
        AmbientTalkParser.expression_return retval = new AmbientTalkParser.expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CMP33=null;
        AmbientTalkParser.comparand_return comparand32 = null;

        AmbientTalkParser.comparand_return comparand34 = null;


        CommonTree CMP33_tree=null;

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:316:3: ( comparand ( CMP comparand )* )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:316:5: comparand ( CMP comparand )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_comparand_in_expression2012);
            comparand32=comparand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, comparand32.getTree());
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:316:15: ( CMP comparand )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==CMP) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:316:16: CMP comparand
            	    {
            	    CMP33=(Token)match(input,CMP,FOLLOW_CMP_in_expression2015); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    CMP33_tree = (CommonTree)adaptor.create(CMP33);
            	    root_0 = (CommonTree)adaptor.becomeRoot(CMP33_tree, root_0);
            	    }
            	    pushFollow(FOLLOW_comparand_in_expression2018);
            	    comparand34=comparand();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, comparand34.getTree());

            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expression"

    public static class comparand_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comparand"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:323:1: comparand : term ( ADD term )* ;
    public final AmbientTalkParser.comparand_return comparand() throws RecognitionException {
        AmbientTalkParser.comparand_return retval = new AmbientTalkParser.comparand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ADD36=null;
        AmbientTalkParser.term_return term35 = null;

        AmbientTalkParser.term_return term37 = null;


        CommonTree ADD36_tree=null;

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:324:3: ( term ( ADD term )* )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:324:5: term ( ADD term )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_term_in_comparand2043);
            term35=term();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, term35.getTree());
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:324:10: ( ADD term )*
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==ADD) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:324:11: ADD term
            	    {
            	    ADD36=(Token)match(input,ADD,FOLLOW_ADD_in_comparand2046); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    ADD36_tree = (CommonTree)adaptor.create(ADD36);
            	    root_0 = (CommonTree)adaptor.becomeRoot(ADD36_tree, root_0);
            	    }
            	    pushFollow(FOLLOW_term_in_comparand2049);
            	    term37=term();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, term37.getTree());

            	    }
            	    break;

            	default :
            	    break loop26;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comparand"

    public static class term_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "term"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:329:1: term : factor ( MUL factor )* ;
    public final AmbientTalkParser.term_return term() throws RecognitionException {
        AmbientTalkParser.term_return retval = new AmbientTalkParser.term_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MUL39=null;
        AmbientTalkParser.factor_return factor38 = null;

        AmbientTalkParser.factor_return factor40 = null;


        CommonTree MUL39_tree=null;

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:330:3: ( factor ( MUL factor )* )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:330:5: factor ( MUL factor )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_factor_in_term2068);
            factor38=factor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, factor38.getTree());
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:330:12: ( MUL factor )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==MUL) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:330:13: MUL factor
            	    {
            	    MUL39=(Token)match(input,MUL,FOLLOW_MUL_in_term2071); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    MUL39_tree = (CommonTree)adaptor.create(MUL39);
            	    root_0 = (CommonTree)adaptor.becomeRoot(MUL39_tree, root_0);
            	    }
            	    pushFollow(FOLLOW_factor_in_term2074);
            	    factor40=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, factor40.getTree());

            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "term"

    public static class factor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "factor"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:335:1: factor : invocation ( POW invocation )* ;
    public final AmbientTalkParser.factor_return factor() throws RecognitionException {
        AmbientTalkParser.factor_return retval = new AmbientTalkParser.factor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token POW42=null;
        AmbientTalkParser.invocation_return invocation41 = null;

        AmbientTalkParser.invocation_return invocation43 = null;


        CommonTree POW42_tree=null;

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:336:3: ( invocation ( POW invocation )* )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:336:5: invocation ( POW invocation )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_invocation_in_factor2091);
            invocation41=invocation();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, invocation41.getTree());
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:336:16: ( POW invocation )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==POW) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:336:17: POW invocation
            	    {
            	    POW42=(Token)match(input,POW,FOLLOW_POW_in_factor2094); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    POW42_tree = (CommonTree)adaptor.create(POW42);
            	    root_0 = (CommonTree)adaptor.becomeRoot(POW42_tree, root_0);
            	    }
            	    pushFollow(FOLLOW_invocation_in_factor2097);
            	    invocation43=invocation();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, invocation43.getTree());

            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "factor"

    public static class invocation_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "invocation"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:342:1: invocation : opr= operand curried_invocation[opr.tree] ;
    public final AmbientTalkParser.invocation_return invocation() throws RecognitionException {
        AmbientTalkParser.invocation_return retval = new AmbientTalkParser.invocation_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.operand_return opr = null;

        AmbientTalkParser.curried_invocation_return curried_invocation44 = null;



        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:343:3: (opr= operand curried_invocation[opr.tree] )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:343:5: opr= operand curried_invocation[opr.tree]
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_operand_in_invocation2118);
            opr=operand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, opr.getTree());
            pushFollow(FOLLOW_curried_invocation_in_invocation2120);
            curried_invocation44=curried_invocation(opr.tree);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, curried_invocation44.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "invocation"

    public static class operand_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "operand"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:350:1: operand : (nbr= NUMBER -> ^( AGNUMBER[$nbr,\"number\"] $nbr) | frc= FRACTION -> ^( AGFRACTION[$frc,\"fraction\"] $frc) | txt= TEXT -> ^( AGTEXT[$txt,\"text\"] $txt) | symbol | table | subexpression | block | unary | lookup | quotation | unquotation | message | async_message | delegation_message | universal_message );
    public final AmbientTalkParser.operand_return operand() throws RecognitionException {
        AmbientTalkParser.operand_return retval = new AmbientTalkParser.operand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token nbr=null;
        Token frc=null;
        Token txt=null;
        AmbientTalkParser.symbol_return symbol45 = null;

        AmbientTalkParser.table_return table46 = null;

        AmbientTalkParser.subexpression_return subexpression47 = null;

        AmbientTalkParser.block_return block48 = null;

        AmbientTalkParser.unary_return unary49 = null;

        AmbientTalkParser.lookup_return lookup50 = null;

        AmbientTalkParser.quotation_return quotation51 = null;

        AmbientTalkParser.unquotation_return unquotation52 = null;

        AmbientTalkParser.message_return message53 = null;

        AmbientTalkParser.async_message_return async_message54 = null;

        AmbientTalkParser.delegation_message_return delegation_message55 = null;

        AmbientTalkParser.universal_message_return universal_message56 = null;


        CommonTree nbr_tree=null;
        CommonTree frc_tree=null;
        CommonTree txt_tree=null;
        RewriteRuleTokenStream stream_TEXT=new RewriteRuleTokenStream(adaptor,"token TEXT");
        RewriteRuleTokenStream stream_FRACTION=new RewriteRuleTokenStream(adaptor,"token FRACTION");
        RewriteRuleTokenStream stream_NUMBER=new RewriteRuleTokenStream(adaptor,"token NUMBER");

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:351:3: (nbr= NUMBER -> ^( AGNUMBER[$nbr,\"number\"] $nbr) | frc= FRACTION -> ^( AGFRACTION[$frc,\"fraction\"] $frc) | txt= TEXT -> ^( AGTEXT[$txt,\"text\"] $txt) | symbol | table | subexpression | block | unary | lookup | quotation | unquotation | message | async_message | delegation_message | universal_message )
            int alt29=15;
            alt29 = dfa29.predict(input);
            switch (alt29) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:351:5: nbr= NUMBER
                    {
                    nbr=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_operand2140); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NUMBER.add(nbr);



                    // AST REWRITE
                    // elements: nbr
                    // token labels: nbr
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_nbr=new RewriteRuleTokenStream(adaptor,"token nbr",nbr);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 351:21: -> ^( AGNUMBER[$nbr,\"number\"] $nbr)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:351:24: ^( AGNUMBER[$nbr,\"number\"] $nbr)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGNUMBER, nbr, "number"), root_1);

                        adaptor.addChild(root_1, stream_nbr.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:352:5: frc= FRACTION
                    {
                    frc=(Token)match(input,FRACTION,FOLLOW_FRACTION_in_operand2168); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FRACTION.add(frc);



                    // AST REWRITE
                    // elements: frc
                    // token labels: frc
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_frc=new RewriteRuleTokenStream(adaptor,"token frc",frc);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 352:21: -> ^( AGFRACTION[$frc,\"fraction\"] $frc)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:352:24: ^( AGFRACTION[$frc,\"fraction\"] $frc)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGFRACTION, frc, "fraction"), root_1);

                        adaptor.addChild(root_1, stream_frc.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:353:5: txt= TEXT
                    {
                    txt=(Token)match(input,TEXT,FOLLOW_TEXT_in_operand2190); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TEXT.add(txt);



                    // AST REWRITE
                    // elements: txt
                    // token labels: txt
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_txt=new RewriteRuleTokenStream(adaptor,"token txt",txt);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 353:21: -> ^( AGTEXT[$txt,\"text\"] $txt)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:353:24: ^( AGTEXT[$txt,\"text\"] $txt)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGTEXT, txt, "text"), root_1);

                        adaptor.addChild(root_1, stream_txt.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:354:5: symbol
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_symbol_in_operand2222);
                    symbol45=symbol();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, symbol45.getTree());

                    }
                    break;
                case 5 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:355:5: table
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_table_in_operand2228);
                    table46=table();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, table46.getTree());

                    }
                    break;
                case 6 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:356:5: subexpression
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_subexpression_in_operand2234);
                    subexpression47=subexpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, subexpression47.getTree());

                    }
                    break;
                case 7 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:357:5: block
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_block_in_operand2240);
                    block48=block();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, block48.getTree());

                    }
                    break;
                case 8 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:358:5: unary
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_unary_in_operand2246);
                    unary49=unary();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, unary49.getTree());

                    }
                    break;
                case 9 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:359:5: lookup
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_lookup_in_operand2252);
                    lookup50=lookup();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, lookup50.getTree());

                    }
                    break;
                case 10 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:360:5: quotation
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_quotation_in_operand2258);
                    quotation51=quotation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, quotation51.getTree());

                    }
                    break;
                case 11 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:361:5: unquotation
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_unquotation_in_operand2264);
                    unquotation52=unquotation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, unquotation52.getTree());

                    }
                    break;
                case 12 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:362:5: message
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_message_in_operand2270);
                    message53=message();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, message53.getTree());

                    }
                    break;
                case 13 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:363:5: async_message
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_async_message_in_operand2276);
                    async_message54=async_message();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, async_message54.getTree());

                    }
                    break;
                case 14 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:364:5: delegation_message
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_delegation_message_in_operand2282);
                    delegation_message55=delegation_message();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, delegation_message55.getTree());

                    }
                    break;
                case 15 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:365:5: universal_message
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_universal_message_in_operand2288);
                    universal_message56=universal_message();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, universal_message56.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "operand"

    public static class symbol_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "symbol"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:369:1: symbol : (nam= NAME -> ^( AGSYMBOL $nam) | slf= SELF -> ^( AGSELF ) );
    public final AmbientTalkParser.symbol_return symbol() throws RecognitionException {
        AmbientTalkParser.symbol_return retval = new AmbientTalkParser.symbol_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token nam=null;
        Token slf=null;

        CommonTree nam_tree=null;
        CommonTree slf_tree=null;
        RewriteRuleTokenStream stream_NAME=new RewriteRuleTokenStream(adaptor,"token NAME");
        RewriteRuleTokenStream stream_SELF=new RewriteRuleTokenStream(adaptor,"token SELF");

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:371:3: (nam= NAME -> ^( AGSYMBOL $nam) | slf= SELF -> ^( AGSELF ) )
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==NAME) ) {
                alt30=1;
            }
            else if ( (LA30_0==SELF) ) {
                alt30=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }
            switch (alt30) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:371:5: nam= NAME
                    {
                    nam=(Token)match(input,NAME,FOLLOW_NAME_in_symbol2306); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NAME.add(nam);



                    // AST REWRITE
                    // elements: nam
                    // token labels: nam
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_nam=new RewriteRuleTokenStream(adaptor,"token nam",nam);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 371:14: -> ^( AGSYMBOL $nam)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:371:17: ^( AGSYMBOL $nam)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSYMBOL, "AGSYMBOL"), root_1);

                        adaptor.addChild(root_1, stream_nam.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:372:5: slf= SELF
                    {
                    slf=(Token)match(input,SELF,FOLLOW_SELF_in_symbol2323); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SELF.add(slf);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 372:14: -> ^( AGSELF )
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:372:17: ^( AGSELF )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSELF, "AGSELF"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "symbol"

    public static class variable_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "variable"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:375:1: variable : ( symbol | operator );
    public final AmbientTalkParser.variable_return variable() throws RecognitionException {
        AmbientTalkParser.variable_return retval = new AmbientTalkParser.variable_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.symbol_return symbol57 = null;

        AmbientTalkParser.operator_return operator58 = null;



        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:376:3: ( symbol | operator )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==NAME||LA31_0==SELF) ) {
                alt31=1;
            }
            else if ( ((LA31_0>=CMP && LA31_0<=POW)) ) {
                alt31=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:376:5: symbol
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_symbol_in_variable2343);
                    symbol57=symbol();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, symbol57.getTree());

                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:377:5: operator
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_operator_in_variable2349);
                    operator58=operator();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, operator58.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "variable"

    public static class variable_or_assignment_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "variable_or_assignment"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:380:1: variable_or_assignment : ( variable | field_assignment );
    public final AmbientTalkParser.variable_or_assignment_return variable_or_assignment() throws RecognitionException {
        AmbientTalkParser.variable_or_assignment_return retval = new AmbientTalkParser.variable_or_assignment_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.variable_return variable59 = null;

        AmbientTalkParser.field_assignment_return field_assignment60 = null;



        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:381:3: ( variable | field_assignment )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( ((LA32_0>=NAME && LA32_0<=POW)||LA32_0==SELF) ) {
                alt32=1;
            }
            else if ( (LA32_0==ASSNAME) ) {
                alt32=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:381:5: variable
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_variable_in_variable_or_assignment2364);
                    variable59=variable();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, variable59.getTree());

                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:382:5: field_assignment
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_field_assignment_in_variable_or_assignment2370);
                    field_assignment60=field_assignment();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, field_assignment60.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "variable_or_assignment"

    public static class table_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "table"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:386:1: table : LBR commalist RBR ;
    public final AmbientTalkParser.table_return table() throws RecognitionException {
        AmbientTalkParser.table_return retval = new AmbientTalkParser.table_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBR61=null;
        Token RBR63=null;
        AmbientTalkParser.commalist_return commalist62 = null;


        CommonTree LBR61_tree=null;
        CommonTree RBR63_tree=null;

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:387:3: ( LBR commalist RBR )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:387:5: LBR commalist RBR
            {
            root_0 = (CommonTree)adaptor.nil();

            LBR61=(Token)match(input,LBR,FOLLOW_LBR_in_table2384); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBR61_tree = (CommonTree)adaptor.create(LBR61);
            adaptor.addChild(root_0, LBR61_tree);
            }
            pushFollow(FOLLOW_commalist_in_table2392);
            commalist62=commalist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) root_0 = (CommonTree)adaptor.becomeRoot(commalist62.getTree(), root_0);
            RBR63=(Token)match(input,RBR,FOLLOW_RBR_in_table2399); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBR63_tree = (CommonTree)adaptor.create(RBR63);
            adaptor.addChild(root_0, RBR63_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "table"

    public static class commalist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "commalist"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:391:1: commalist : slots+= argument ( COM slots+= argument )* -> ^( AGTABLE[\"slots\"] ( $slots)+ ) ;
    public final AmbientTalkParser.commalist_return commalist() throws RecognitionException {
        AmbientTalkParser.commalist_return retval = new AmbientTalkParser.commalist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COM64=null;
        List list_slots=null;
        RuleReturnScope slots = null;
        CommonTree COM64_tree=null;
        RewriteRuleTokenStream stream_COM=new RewriteRuleTokenStream(adaptor,"token COM");
        RewriteRuleSubtreeStream stream_argument=new RewriteRuleSubtreeStream(adaptor,"rule argument");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:392:3: (slots+= argument ( COM slots+= argument )* -> ^( AGTABLE[\"slots\"] ( $slots)+ ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:392:5: slots+= argument ( COM slots+= argument )*
            {
            pushFollow(FOLLOW_argument_in_commalist2413);
            slots=argument();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argument.add(slots.getTree());
            if (list_slots==null) list_slots=new ArrayList();
            list_slots.add(slots.getTree());

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:392:21: ( COM slots+= argument )*
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( (LA33_0==COM) ) {
                    alt33=1;
                }


                switch (alt33) {
            	case 1 :
            	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:392:22: COM slots+= argument
            	    {
            	    COM64=(Token)match(input,COM,FOLLOW_COM_in_commalist2416); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COM.add(COM64);

            	    pushFollow(FOLLOW_argument_in_commalist2420);
            	    slots=argument();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_argument.add(slots.getTree());
            	    if (list_slots==null) list_slots=new ArrayList();
            	    list_slots.add(slots.getTree());


            	    }
            	    break;

            	default :
            	    break loop33;
                }
            } while (true);



            // AST REWRITE
            // elements: slots
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: slots
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_slots=new RewriteRuleSubtreeStream(adaptor,"token slots",list_slots);
            root_0 = (CommonTree)adaptor.nil();
            // 393:5: -> ^( AGTABLE[\"slots\"] ( $slots)+ )
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:393:8: ^( AGTABLE[\"slots\"] ( $slots)+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGTABLE, "slots"), root_1);

                if ( !(stream_slots.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_slots.hasNext() ) {
                    adaptor.addChild(root_1, stream_slots.nextTree());

                }
                stream_slots.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "commalist"

    public static class subexpression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "subexpression"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:397:1: subexpression : LPR expression RPR ;
    public final AmbientTalkParser.subexpression_return subexpression() throws RecognitionException {
        AmbientTalkParser.subexpression_return retval = new AmbientTalkParser.subexpression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPR65=null;
        Token RPR67=null;
        AmbientTalkParser.expression_return expression66 = null;


        CommonTree LPR65_tree=null;
        CommonTree RPR67_tree=null;

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:398:3: ( LPR expression RPR )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:398:5: LPR expression RPR
            {
            root_0 = (CommonTree)adaptor.nil();

            LPR65=(Token)match(input,LPR,FOLLOW_LPR_in_subexpression2451); if (state.failed) return retval;
            pushFollow(FOLLOW_expression_in_subexpression2454);
            expression66=expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) root_0 = (CommonTree)adaptor.becomeRoot(expression66.getTree(), root_0);
            RPR67=(Token)match(input,RPR,FOLLOW_RPR_in_subexpression2457); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "subexpression"

    public static class block_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "block"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:403:1: block : lc= LBC ( PIP pars= parameterlist PIP )? body= statementlist RBC -> ^( AGCLOSURE[$lc,\"closure\"] ( $pars)* ( $body)* ) ;
    public final AmbientTalkParser.block_return block() throws RecognitionException {
        AmbientTalkParser.block_return retval = new AmbientTalkParser.block_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token PIP68=null;
        Token PIP69=null;
        Token RBC70=null;
        AmbientTalkParser.parameterlist_return pars = null;

        AmbientTalkParser.statementlist_return body = null;


        CommonTree lc_tree=null;
        CommonTree PIP68_tree=null;
        CommonTree PIP69_tree=null;
        CommonTree RBC70_tree=null;
        RewriteRuleTokenStream stream_PIP=new RewriteRuleTokenStream(adaptor,"token PIP");
        RewriteRuleTokenStream stream_RBC=new RewriteRuleTokenStream(adaptor,"token RBC");
        RewriteRuleTokenStream stream_LBC=new RewriteRuleTokenStream(adaptor,"token LBC");
        RewriteRuleSubtreeStream stream_parameterlist=new RewriteRuleSubtreeStream(adaptor,"rule parameterlist");
        RewriteRuleSubtreeStream stream_statementlist=new RewriteRuleSubtreeStream(adaptor,"rule statementlist");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:404:3: (lc= LBC ( PIP pars= parameterlist PIP )? body= statementlist RBC -> ^( AGCLOSURE[$lc,\"closure\"] ( $pars)* ( $body)* ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:404:5: lc= LBC ( PIP pars= parameterlist PIP )? body= statementlist RBC
            {
            lc=(Token)match(input,LBC,FOLLOW_LBC_in_block2475); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBC.add(lc);

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:405:7: ( PIP pars= parameterlist PIP )?
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==PIP) ) {
                alt34=1;
            }
            switch (alt34) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:405:9: PIP pars= parameterlist PIP
                    {
                    PIP68=(Token)match(input,PIP,FOLLOW_PIP_in_block2485); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PIP.add(PIP68);

                    pushFollow(FOLLOW_parameterlist_in_block2489);
                    pars=parameterlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_parameterlist.add(pars.getTree());
                    PIP69=(Token)match(input,PIP,FOLLOW_PIP_in_block2491); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PIP.add(PIP69);


                    }
                    break;

            }

            pushFollow(FOLLOW_statementlist_in_block2504);
            body=statementlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_statementlist.add(body.getTree());
            RBC70=(Token)match(input,RBC,FOLLOW_RBC_in_block2510); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBC.add(RBC70);



            // AST REWRITE
            // elements: pars, body
            // token labels: 
            // rule labels: body, retval, pars
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_body=new RewriteRuleSubtreeStream(adaptor,"rule body",body!=null?body.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_pars=new RewriteRuleSubtreeStream(adaptor,"rule pars",pars!=null?pars.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 408:5: -> ^( AGCLOSURE[$lc,\"closure\"] ( $pars)* ( $body)* )
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:408:8: ^( AGCLOSURE[$lc,\"closure\"] ( $pars)* ( $body)* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGCLOSURE, lc, "closure"), root_1);

                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:408:35: ( $pars)*
                while ( stream_pars.hasNext() ) {
                    adaptor.addChild(root_1, stream_pars.nextTree());

                }
                stream_pars.reset();
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:408:42: ( $body)*
                while ( stream_body.hasNext() ) {
                    adaptor.addChild(root_1, stream_body.nextTree());

                }
                stream_body.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "block"

    public static class parameterlist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "parameterlist"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:410:1: parameterlist : (pars+= parameter ( COM pars+= parameter )* ( COM )? )? -> ^( AGTABLE[\"parameterlist\"] ( $pars)* ) ;
    public final AmbientTalkParser.parameterlist_return parameterlist() throws RecognitionException {
        AmbientTalkParser.parameterlist_return retval = new AmbientTalkParser.parameterlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COM71=null;
        Token COM72=null;
        List list_pars=null;
        RuleReturnScope pars = null;
        CommonTree COM71_tree=null;
        CommonTree COM72_tree=null;
        RewriteRuleTokenStream stream_COM=new RewriteRuleTokenStream(adaptor,"token COM");
        RewriteRuleSubtreeStream stream_parameter=new RewriteRuleSubtreeStream(adaptor,"rule parameter");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:411:3: ( (pars+= parameter ( COM pars+= parameter )* ( COM )? )? -> ^( AGTABLE[\"parameterlist\"] ( $pars)* ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:411:5: (pars+= parameter ( COM pars+= parameter )* ( COM )? )?
            {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:411:5: (pars+= parameter ( COM pars+= parameter )* ( COM )? )?
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( ((LA37_0>=NAME && LA37_0<=POW)||LA37_0==SELF||LA37_0==CAT||LA37_0==ASSNAME) ) {
                alt37=1;
            }
            switch (alt37) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:411:7: pars+= parameter ( COM pars+= parameter )* ( COM )?
                    {
                    pushFollow(FOLLOW_parameter_in_parameterlist2545);
                    pars=parameter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_parameter.add(pars.getTree());
                    if (list_pars==null) list_pars=new ArrayList();
                    list_pars.add(pars.getTree());

                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:411:23: ( COM pars+= parameter )*
                    loop35:
                    do {
                        int alt35=2;
                        int LA35_0 = input.LA(1);

                        if ( (LA35_0==COM) ) {
                            int LA35_1 = input.LA(2);

                            if ( ((LA35_1>=NAME && LA35_1<=POW)||LA35_1==SELF||LA35_1==CAT||LA35_1==ASSNAME) ) {
                                alt35=1;
                            }


                        }


                        switch (alt35) {
                    	case 1 :
                    	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:411:24: COM pars+= parameter
                    	    {
                    	    COM71=(Token)match(input,COM,FOLLOW_COM_in_parameterlist2548); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COM.add(COM71);

                    	    pushFollow(FOLLOW_parameter_in_parameterlist2552);
                    	    pars=parameter();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_parameter.add(pars.getTree());
                    	    if (list_pars==null) list_pars=new ArrayList();
                    	    list_pars.add(pars.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop35;
                        }
                    } while (true);

                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:411:46: ( COM )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==COM) ) {
                        alt36=1;
                    }
                    switch (alt36) {
                        case 1 :
                            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:411:46: COM
                            {
                            COM72=(Token)match(input,COM,FOLLOW_COM_in_parameterlist2556); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COM.add(COM72);


                            }
                            break;

                    }


                    }
                    break;

            }



            // AST REWRITE
            // elements: pars
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: pars
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_pars=new RewriteRuleSubtreeStream(adaptor,"token pars",list_pars);
            root_0 = (CommonTree)adaptor.nil();
            // 412:5: -> ^( AGTABLE[\"parameterlist\"] ( $pars)* )
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:412:8: ^( AGTABLE[\"parameterlist\"] ( $pars)* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGTABLE, "parameterlist"), root_1);

                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:412:35: ( $pars)*
                while ( stream_pars.hasNext() ) {
                    adaptor.addChild(root_1, stream_pars.nextTree());

                }
                stream_pars.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "parameterlist"

    public static class parameter_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "parameter"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:414:1: parameter : ( ( variable_or_quotation EQL )=>var= variable_or_quotation EQL exp= expression -> ^( AGASSVAR[\"var-set\"] $var $exp) | voq= variable_or_quotation -> $voq | lc= CAT voq= variable_or_quotation -> ^( AGSPLICE[$lc,\"splice\"] $voq) );
    public final AmbientTalkParser.parameter_return parameter() throws RecognitionException {
        AmbientTalkParser.parameter_return retval = new AmbientTalkParser.parameter_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token EQL73=null;
        AmbientTalkParser.variable_or_quotation_return var = null;

        AmbientTalkParser.expression_return exp = null;

        AmbientTalkParser.variable_or_quotation_return voq = null;


        CommonTree lc_tree=null;
        CommonTree EQL73_tree=null;
        RewriteRuleTokenStream stream_EQL=new RewriteRuleTokenStream(adaptor,"token EQL");
        RewriteRuleTokenStream stream_CAT=new RewriteRuleTokenStream(adaptor,"token CAT");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_variable_or_quotation=new RewriteRuleSubtreeStream(adaptor,"rule variable_or_quotation");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:415:3: ( ( variable_or_quotation EQL )=>var= variable_or_quotation EQL exp= expression -> ^( AGASSVAR[\"var-set\"] $var $exp) | voq= variable_or_quotation -> $voq | lc= CAT voq= variable_or_quotation -> ^( AGSPLICE[$lc,\"splice\"] $voq) )
            int alt38=3;
            alt38 = dfa38.predict(input);
            switch (alt38) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:415:5: ( variable_or_quotation EQL )=>var= variable_or_quotation EQL exp= expression
                    {
                    pushFollow(FOLLOW_variable_or_quotation_in_parameter2597);
                    var=variable_or_quotation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_variable_or_quotation.add(var.getTree());
                    EQL73=(Token)match(input,EQL,FOLLOW_EQL_in_parameter2599); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQL.add(EQL73);

                    pushFollow(FOLLOW_expression_in_parameter2603);
                    exp=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_expression.add(exp.getTree());


                    // AST REWRITE
                    // elements: exp, var
                    // token labels: 
                    // rule labels: exp, retval, var
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_exp=new RewriteRuleSubtreeStream(adaptor,"rule exp",exp!=null?exp.tree:null);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_var=new RewriteRuleSubtreeStream(adaptor,"rule var",var!=null?var.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 416:5: -> ^( AGASSVAR[\"var-set\"] $var $exp)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:416:8: ^( AGASSVAR[\"var-set\"] $var $exp)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGASSVAR, "var-set"), root_1);

                        adaptor.addChild(root_1, stream_var.nextTree());
                        adaptor.addChild(root_1, stream_exp.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:417:5: voq= variable_or_quotation
                    {
                    pushFollow(FOLLOW_variable_or_quotation_in_parameter2628);
                    voq=variable_or_quotation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_variable_or_quotation.add(voq.getTree());


                    // AST REWRITE
                    // elements: voq
                    // token labels: 
                    // rule labels: retval, voq
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_voq=new RewriteRuleSubtreeStream(adaptor,"rule voq",voq!=null?voq.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 418:5: -> $voq
                    {
                        adaptor.addChild(root_0, stream_voq.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:419:5: lc= CAT voq= variable_or_quotation
                    {
                    lc=(Token)match(input,CAT,FOLLOW_CAT_in_parameter2645); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CAT.add(lc);

                    pushFollow(FOLLOW_variable_or_quotation_in_parameter2649);
                    voq=variable_or_quotation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_variable_or_quotation.add(voq.getTree());


                    // AST REWRITE
                    // elements: voq
                    // token labels: 
                    // rule labels: retval, voq
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_voq=new RewriteRuleSubtreeStream(adaptor,"rule voq",voq!=null?voq.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 420:5: -> ^( AGSPLICE[$lc,\"splice\"] $voq)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:420:8: ^( AGSPLICE[$lc,\"splice\"] $voq)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSPLICE, lc, "splice"), root_1);

                        adaptor.addChild(root_1, stream_voq.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "parameter"

    public static class variable_or_quotation_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "variable_or_quotation"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:422:1: variable_or_quotation : variable_or_assignment ;
    public final AmbientTalkParser.variable_or_quotation_return variable_or_quotation() throws RecognitionException {
        AmbientTalkParser.variable_or_quotation_return retval = new AmbientTalkParser.variable_or_quotation_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.variable_or_assignment_return variable_or_assignment74 = null;



        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:426:3: ( variable_or_assignment )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:426:5: variable_or_assignment
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_variable_or_assignment_in_variable_or_quotation2684);
            variable_or_assignment74=variable_or_assignment();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, variable_or_assignment74.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "variable_or_quotation"

    public static class statementlist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "statementlist"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:429:1: statementlist : (stmts+= statement ( SMC stmts+= statement )* ( SMC )? )? -> ^( AGBEGIN ( $stmts)* ) ;
    public final AmbientTalkParser.statementlist_return statementlist() throws RecognitionException {
        AmbientTalkParser.statementlist_return retval = new AmbientTalkParser.statementlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SMC75=null;
        Token SMC76=null;
        List list_stmts=null;
        RuleReturnScope stmts = null;
        CommonTree SMC75_tree=null;
        CommonTree SMC76_tree=null;
        RewriteRuleTokenStream stream_SMC=new RewriteRuleTokenStream(adaptor,"token SMC");
        RewriteRuleSubtreeStream stream_statement=new RewriteRuleSubtreeStream(adaptor,"rule statement");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:430:3: ( (stmts+= statement ( SMC stmts+= statement )* ( SMC )? )? -> ^( AGBEGIN ( $stmts)* ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:430:5: (stmts+= statement ( SMC stmts+= statement )* ( SMC )? )?
            {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:430:5: (stmts+= statement ( SMC stmts+= statement )* ( SMC )? )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( ((LA41_0>=SINGLE_LINE_DOC && LA41_0<=DEF)||LA41_0==LPR||LA41_0==LBC||LA41_0==LBR||(LA41_0>=DOT && LA41_0<=TDEF)||LA41_0==IMPORT||(LA41_0>=NAME && LA41_0<=SELF)||LA41_0==LKU||(LA41_0>=BQU && LA41_0<=HSH)||(LA41_0>=ARW && LA41_0<=USD)) ) {
                alt41=1;
            }
            switch (alt41) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:430:7: stmts+= statement ( SMC stmts+= statement )* ( SMC )?
                    {
                    pushFollow(FOLLOW_statement_in_statementlist2703);
                    stmts=statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_statement.add(stmts.getTree());
                    if (list_stmts==null) list_stmts=new ArrayList();
                    list_stmts.add(stmts.getTree());

                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:430:24: ( SMC stmts+= statement )*
                    loop39:
                    do {
                        int alt39=2;
                        int LA39_0 = input.LA(1);

                        if ( (LA39_0==SMC) ) {
                            int LA39_1 = input.LA(2);

                            if ( ((LA39_1>=SINGLE_LINE_DOC && LA39_1<=DEF)||LA39_1==LPR||LA39_1==LBC||LA39_1==LBR||(LA39_1>=DOT && LA39_1<=TDEF)||LA39_1==IMPORT||(LA39_1>=NAME && LA39_1<=SELF)||LA39_1==LKU||(LA39_1>=BQU && LA39_1<=HSH)||(LA39_1>=ARW && LA39_1<=USD)) ) {
                                alt39=1;
                            }


                        }


                        switch (alt39) {
                    	case 1 :
                    	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:430:25: SMC stmts+= statement
                    	    {
                    	    SMC75=(Token)match(input,SMC,FOLLOW_SMC_in_statementlist2706); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_SMC.add(SMC75);

                    	    pushFollow(FOLLOW_statement_in_statementlist2710);
                    	    stmts=statement();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_statement.add(stmts.getTree());
                    	    if (list_stmts==null) list_stmts=new ArrayList();
                    	    list_stmts.add(stmts.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop39;
                        }
                    } while (true);

                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:430:48: ( SMC )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0==SMC) ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:430:48: SMC
                            {
                            SMC76=(Token)match(input,SMC,FOLLOW_SMC_in_statementlist2714); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_SMC.add(SMC76);


                            }
                            break;

                    }


                    }
                    break;

            }



            // AST REWRITE
            // elements: stmts
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: stmts
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_stmts=new RewriteRuleSubtreeStream(adaptor,"token stmts",list_stmts);
            root_0 = (CommonTree)adaptor.nil();
            // 431:5: -> ^( AGBEGIN ( $stmts)* )
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:431:8: ^( AGBEGIN ( $stmts)* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGBEGIN, "AGBEGIN"), root_1);

                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:431:18: ( $stmts)*
                while ( stream_stmts.hasNext() ) {
                    adaptor.addChild(root_1, stream_stmts.nextTree());

                }
                stream_stmts.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "statementlist"

    public static class unary_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unary"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:439:1: unary : ( ( operator ( LPR | LBR | DOT | ARW | USD ) )=> operator | ( operator invocation )=> (opr= operator arg= invocation ) -> ^( AGAPPLICATION[\"application\"] $opr ^( AGTABLE[\"argument-table\"] $arg) ) | operator );
    public final AmbientTalkParser.unary_return unary() throws RecognitionException {
        AmbientTalkParser.unary_return retval = new AmbientTalkParser.unary_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.operator_return opr = null;

        AmbientTalkParser.invocation_return arg = null;

        AmbientTalkParser.operator_return operator77 = null;

        AmbientTalkParser.operator_return operator78 = null;


        RewriteRuleSubtreeStream stream_invocation=new RewriteRuleSubtreeStream(adaptor,"rule invocation");
        RewriteRuleSubtreeStream stream_operator=new RewriteRuleSubtreeStream(adaptor,"rule operator");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:440:3: ( ( operator ( LPR | LBR | DOT | ARW | USD ) )=> operator | ( operator invocation )=> (opr= operator arg= invocation ) -> ^( AGAPPLICATION[\"application\"] $opr ^( AGTABLE[\"argument-table\"] $arg) ) | operator )
            int alt42=3;
            alt42 = dfa42.predict(input);
            switch (alt42) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:440:5: ( operator ( LPR | LBR | DOT | ARW | USD ) )=> operator
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_operator_in_unary2779);
                    operator77=operator();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) root_0 = (CommonTree)adaptor.becomeRoot(operator77.getTree(), root_0);

                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:441:5: ( operator invocation )=> (opr= operator arg= invocation )
                    {
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:441:31: (opr= operator arg= invocation )
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:441:33: opr= operator arg= invocation
                    {
                    pushFollow(FOLLOW_operator_in_unary2799);
                    opr=operator();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_operator.add(opr.getTree());
                    pushFollow(FOLLOW_invocation_in_unary2803);
                    arg=invocation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_invocation.add(arg.getTree());

                    }



                    // AST REWRITE
                    // elements: arg, opr
                    // token labels: 
                    // rule labels: arg, retval, opr
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg",arg!=null?arg.tree:null);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_opr=new RewriteRuleSubtreeStream(adaptor,"rule opr",opr!=null?opr.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 442:5: -> ^( AGAPPLICATION[\"application\"] $opr ^( AGTABLE[\"argument-table\"] $arg) )
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:442:8: ^( AGAPPLICATION[\"application\"] $opr ^( AGTABLE[\"argument-table\"] $arg) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGAPPLICATION, "application"), root_1);

                        adaptor.addChild(root_1, stream_opr.nextTree());
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:442:44: ^( AGTABLE[\"argument-table\"] $arg)
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGTABLE, "argument-table"), root_2);

                        adaptor.addChild(root_2, stream_arg.nextTree());

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:443:5: operator
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_operator_in_unary2833);
                    operator78=operator();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) root_0 = (CommonTree)adaptor.becomeRoot(operator78.getTree(), root_0);

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "unary"

    public static class operator_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "operator"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:445:1: operator : (cmp= CMP -> ^( AGCOMPARE $cmp) | add= ADD -> ^( AGADDITION $add) | mul= MUL -> ^( AGMULTIPLY $mul) | pow= POW -> ^( AGPOWER $pow) );
    public final AmbientTalkParser.operator_return operator() throws RecognitionException {
        AmbientTalkParser.operator_return retval = new AmbientTalkParser.operator_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token cmp=null;
        Token add=null;
        Token mul=null;
        Token pow=null;

        CommonTree cmp_tree=null;
        CommonTree add_tree=null;
        CommonTree mul_tree=null;
        CommonTree pow_tree=null;
        RewriteRuleTokenStream stream_POW=new RewriteRuleTokenStream(adaptor,"token POW");
        RewriteRuleTokenStream stream_MUL=new RewriteRuleTokenStream(adaptor,"token MUL");
        RewriteRuleTokenStream stream_ADD=new RewriteRuleTokenStream(adaptor,"token ADD");
        RewriteRuleTokenStream stream_CMP=new RewriteRuleTokenStream(adaptor,"token CMP");

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:446:3: (cmp= CMP -> ^( AGCOMPARE $cmp) | add= ADD -> ^( AGADDITION $add) | mul= MUL -> ^( AGMULTIPLY $mul) | pow= POW -> ^( AGPOWER $pow) )
            int alt43=4;
            switch ( input.LA(1) ) {
            case CMP:
                {
                alt43=1;
                }
                break;
            case ADD:
                {
                alt43=2;
                }
                break;
            case MUL:
                {
                alt43=3;
                }
                break;
            case POW:
                {
                alt43=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }

            switch (alt43) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:446:5: cmp= CMP
                    {
                    cmp=(Token)match(input,CMP,FOLLOW_CMP_in_operator2848); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CMP.add(cmp);



                    // AST REWRITE
                    // elements: cmp
                    // token labels: cmp
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_cmp=new RewriteRuleTokenStream(adaptor,"token cmp",cmp);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 446:13: -> ^( AGCOMPARE $cmp)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:446:16: ^( AGCOMPARE $cmp)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGCOMPARE, "AGCOMPARE"), root_1);

                        adaptor.addChild(root_1, stream_cmp.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:447:5: add= ADD
                    {
                    add=(Token)match(input,ADD,FOLLOW_ADD_in_operator2868); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ADD.add(add);



                    // AST REWRITE
                    // elements: add
                    // token labels: add
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_add=new RewriteRuleTokenStream(adaptor,"token add",add);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 447:13: -> ^( AGADDITION $add)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:447:16: ^( AGADDITION $add)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGADDITION, "AGADDITION"), root_1);

                        adaptor.addChild(root_1, stream_add.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:448:5: mul= MUL
                    {
                    mul=(Token)match(input,MUL,FOLLOW_MUL_in_operator2887); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MUL.add(mul);



                    // AST REWRITE
                    // elements: mul
                    // token labels: mul
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_mul=new RewriteRuleTokenStream(adaptor,"token mul",mul);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 448:13: -> ^( AGMULTIPLY $mul)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:448:16: ^( AGMULTIPLY $mul)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGMULTIPLY, "AGMULTIPLY"), root_1);

                        adaptor.addChild(root_1, stream_mul.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:449:5: pow= POW
                    {
                    pow=(Token)match(input,POW,FOLLOW_POW_in_operator2906); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_POW.add(pow);



                    // AST REWRITE
                    // elements: pow
                    // token labels: pow
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_pow=new RewriteRuleTokenStream(adaptor,"token pow",pow);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 449:13: -> ^( AGPOWER $pow)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:449:16: ^( AGPOWER $pow)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGPOWER, "AGPOWER"), root_1);

                        adaptor.addChild(root_1, stream_pow.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "operator"

    public static class lookup_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "lookup"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:453:1: lookup : lc= LKU vka= variable_or_keyword_or_assignment -> ^( AGLOOKUP[$lc,\"lookup\"] $vka) ;
    public final AmbientTalkParser.lookup_return lookup() throws RecognitionException {
        AmbientTalkParser.lookup_return retval = new AmbientTalkParser.lookup_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        AmbientTalkParser.variable_or_keyword_or_assignment_return vka = null;


        CommonTree lc_tree=null;
        RewriteRuleTokenStream stream_LKU=new RewriteRuleTokenStream(adaptor,"token LKU");
        RewriteRuleSubtreeStream stream_variable_or_keyword_or_assignment=new RewriteRuleSubtreeStream(adaptor,"rule variable_or_keyword_or_assignment");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:454:3: (lc= LKU vka= variable_or_keyword_or_assignment -> ^( AGLOOKUP[$lc,\"lookup\"] $vka) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:454:5: lc= LKU vka= variable_or_keyword_or_assignment
            {
            lc=(Token)match(input,LKU,FOLLOW_LKU_in_lookup2936); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LKU.add(lc);

            pushFollow(FOLLOW_variable_or_keyword_or_assignment_in_lookup2940);
            vka=variable_or_keyword_or_assignment();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_variable_or_keyword_or_assignment.add(vka.getTree());


            // AST REWRITE
            // elements: vka
            // token labels: 
            // rule labels: retval, vka
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_vka=new RewriteRuleSubtreeStream(adaptor,"rule vka",vka!=null?vka.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 455:5: -> ^( AGLOOKUP[$lc,\"lookup\"] $vka)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:455:8: ^( AGLOOKUP[$lc,\"lookup\"] $vka)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGLOOKUP, lc, "lookup"), root_1);

                adaptor.addChild(root_1, stream_vka.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "lookup"

    public static class variable_or_keyword_or_assignment_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "variable_or_keyword_or_assignment"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:457:1: variable_or_keyword_or_assignment : ( variable_or_field_assignment )=> variable_or_field_assignment ;
    public final AmbientTalkParser.variable_or_keyword_or_assignment_return variable_or_keyword_or_assignment() throws RecognitionException {
        AmbientTalkParser.variable_or_keyword_or_assignment_return retval = new AmbientTalkParser.variable_or_keyword_or_assignment_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.variable_or_field_assignment_return variable_or_field_assignment79 = null;



        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:458:3: ( ( variable_or_field_assignment )=> variable_or_field_assignment )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:458:5: ( variable_or_field_assignment )=> variable_or_field_assignment
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_variable_or_field_assignment_in_variable_or_keyword_or_assignment2972);
            variable_or_field_assignment79=variable_or_field_assignment();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, variable_or_field_assignment79.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "variable_or_keyword_or_assignment"

    public static class variable_or_field_assignment_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "variable_or_field_assignment"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:463:1: variable_or_field_assignment : ( field_assignment | variable );
    public final AmbientTalkParser.variable_or_field_assignment_return variable_or_field_assignment() throws RecognitionException {
        AmbientTalkParser.variable_or_field_assignment_return retval = new AmbientTalkParser.variable_or_field_assignment_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.field_assignment_return field_assignment80 = null;

        AmbientTalkParser.variable_return variable81 = null;



        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:464:3: ( field_assignment | variable )
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==ASSNAME) ) {
                alt44=1;
            }
            else if ( ((LA44_0>=NAME && LA44_0<=POW)||LA44_0==SELF) ) {
                alt44=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                throw nvae;
            }
            switch (alt44) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:464:5: field_assignment
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_field_assignment_in_variable_or_field_assignment2989);
                    field_assignment80=field_assignment();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, field_assignment80.getTree());

                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:465:5: variable
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_variable_in_variable_or_field_assignment2995);
                    variable81=variable();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, variable81.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "variable_or_field_assignment"

    public static class field_assignment_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "field_assignment"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:467:1: field_assignment : fas= ASSNAME -> ^( AGASSSYMBOL[$fas,\"field-assignment\"] $fas) ;
    public final AmbientTalkParser.field_assignment_return field_assignment() throws RecognitionException {
        AmbientTalkParser.field_assignment_return retval = new AmbientTalkParser.field_assignment_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token fas=null;

        CommonTree fas_tree=null;
        RewriteRuleTokenStream stream_ASSNAME=new RewriteRuleTokenStream(adaptor,"token ASSNAME");

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:468:3: (fas= ASSNAME -> ^( AGASSSYMBOL[$fas,\"field-assignment\"] $fas) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:468:5: fas= ASSNAME
            {
            fas=(Token)match(input,ASSNAME,FOLLOW_ASSNAME_in_field_assignment3009); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ASSNAME.add(fas);



            // AST REWRITE
            // elements: fas
            // token labels: fas
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleTokenStream stream_fas=new RewriteRuleTokenStream(adaptor,"token fas",fas);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 469:5: -> ^( AGASSSYMBOL[$fas,\"field-assignment\"] $fas)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:469:8: ^( AGASSSYMBOL[$fas,\"field-assignment\"] $fas)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGASSSYMBOL, fas, "field-assignment"), root_1);

                adaptor.addChild(root_1, stream_fas.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "field_assignment"

    public static class quotation_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "quotation"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:479:1: quotation : lc= BQU ( ( LBC )=> LBC stmts= statementlist RBC -> ^( AGQUOTEBEGIN[$lc,\"quote-begin\"] $stmts) | fa= field_assignment -> ^( AGQUOTE[$lc,\"quote\"] $fa) | qexp= operand -> ^( AGQUOTE[$lc,\"quote\"] $qexp) ) ;
    public final AmbientTalkParser.quotation_return quotation() throws RecognitionException {
        AmbientTalkParser.quotation_return retval = new AmbientTalkParser.quotation_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token LBC82=null;
        Token RBC83=null;
        AmbientTalkParser.statementlist_return stmts = null;

        AmbientTalkParser.field_assignment_return fa = null;

        AmbientTalkParser.operand_return qexp = null;


        CommonTree lc_tree=null;
        CommonTree LBC82_tree=null;
        CommonTree RBC83_tree=null;
        RewriteRuleTokenStream stream_RBC=new RewriteRuleTokenStream(adaptor,"token RBC");
        RewriteRuleTokenStream stream_BQU=new RewriteRuleTokenStream(adaptor,"token BQU");
        RewriteRuleTokenStream stream_LBC=new RewriteRuleTokenStream(adaptor,"token LBC");
        RewriteRuleSubtreeStream stream_statementlist=new RewriteRuleSubtreeStream(adaptor,"rule statementlist");
        RewriteRuleSubtreeStream stream_field_assignment=new RewriteRuleSubtreeStream(adaptor,"rule field_assignment");
        RewriteRuleSubtreeStream stream_operand=new RewriteRuleSubtreeStream(adaptor,"rule operand");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:480:3: (lc= BQU ( ( LBC )=> LBC stmts= statementlist RBC -> ^( AGQUOTEBEGIN[$lc,\"quote-begin\"] $stmts) | fa= field_assignment -> ^( AGQUOTE[$lc,\"quote\"] $fa) | qexp= operand -> ^( AGQUOTE[$lc,\"quote\"] $qexp) ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:480:5: lc= BQU ( ( LBC )=> LBC stmts= statementlist RBC -> ^( AGQUOTEBEGIN[$lc,\"quote-begin\"] $stmts) | fa= field_assignment -> ^( AGQUOTE[$lc,\"quote\"] $fa) | qexp= operand -> ^( AGQUOTE[$lc,\"quote\"] $qexp) )
            {
            lc=(Token)match(input,BQU,FOLLOW_BQU_in_quotation3040); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BQU.add(lc);

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:481:7: ( ( LBC )=> LBC stmts= statementlist RBC -> ^( AGQUOTEBEGIN[$lc,\"quote-begin\"] $stmts) | fa= field_assignment -> ^( AGQUOTE[$lc,\"quote\"] $fa) | qexp= operand -> ^( AGQUOTE[$lc,\"quote\"] $qexp) )
            int alt45=3;
            alt45 = dfa45.predict(input);
            switch (alt45) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:482:9: ( LBC )=> LBC stmts= statementlist RBC
                    {
                    LBC82=(Token)match(input,LBC,FOLLOW_LBC_in_quotation3065); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBC.add(LBC82);

                    pushFollow(FOLLOW_statementlist_in_quotation3069);
                    stmts=statementlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_statementlist.add(stmts.getTree());
                    RBC83=(Token)match(input,RBC,FOLLOW_RBC_in_quotation3071); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBC.add(RBC83);



                    // AST REWRITE
                    // elements: stmts
                    // token labels: 
                    // rule labels: retval, stmts
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_stmts=new RewriteRuleSubtreeStream(adaptor,"rule stmts",stmts!=null?stmts.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 483:9: -> ^( AGQUOTEBEGIN[$lc,\"quote-begin\"] $stmts)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:483:12: ^( AGQUOTEBEGIN[$lc,\"quote-begin\"] $stmts)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGQUOTEBEGIN, lc, "quote-begin"), root_1);

                        adaptor.addChild(root_1, stream_stmts.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:485:9: fa= field_assignment
                    {
                    pushFollow(FOLLOW_field_assignment_in_quotation3110);
                    fa=field_assignment();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_field_assignment.add(fa.getTree());


                    // AST REWRITE
                    // elements: fa
                    // token labels: 
                    // rule labels: retval, fa
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_fa=new RewriteRuleSubtreeStream(adaptor,"rule fa",fa!=null?fa.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 486:9: -> ^( AGQUOTE[$lc,\"quote\"] $fa)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:486:12: ^( AGQUOTE[$lc,\"quote\"] $fa)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGQUOTE, lc, "quote"), root_1);

                        adaptor.addChild(root_1, stream_fa.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:491:9: qexp= operand
                    {
                    pushFollow(FOLLOW_operand_in_quotation3151);
                    qexp=operand();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_operand.add(qexp.getTree());


                    // AST REWRITE
                    // elements: qexp
                    // token labels: 
                    // rule labels: retval, qexp
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_qexp=new RewriteRuleSubtreeStream(adaptor,"rule qexp",qexp!=null?qexp.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 492:9: -> ^( AGQUOTE[$lc,\"quote\"] $qexp)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:492:12: ^( AGQUOTE[$lc,\"quote\"] $qexp)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGQUOTE, lc, "quote"), root_1);

                        adaptor.addChild(root_1, stream_qexp.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "quotation"

    public static class unquotation_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unquotation"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:497:1: unquotation : lc= HSH (uexp= operand -> ^( AGUNQUOTE[$lc,\"unquote\"] $uexp) | CAT uexp= operand -> ^( AGUNQUOTESPLICE[$lc,\"unquote-splice\"] $uexp) ) ;
    public final AmbientTalkParser.unquotation_return unquotation() throws RecognitionException {
        AmbientTalkParser.unquotation_return retval = new AmbientTalkParser.unquotation_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token CAT84=null;
        AmbientTalkParser.operand_return uexp = null;


        CommonTree lc_tree=null;
        CommonTree CAT84_tree=null;
        RewriteRuleTokenStream stream_HSH=new RewriteRuleTokenStream(adaptor,"token HSH");
        RewriteRuleTokenStream stream_CAT=new RewriteRuleTokenStream(adaptor,"token CAT");
        RewriteRuleSubtreeStream stream_operand=new RewriteRuleSubtreeStream(adaptor,"rule operand");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:498:3: (lc= HSH (uexp= operand -> ^( AGUNQUOTE[$lc,\"unquote\"] $uexp) | CAT uexp= operand -> ^( AGUNQUOTESPLICE[$lc,\"unquote-splice\"] $uexp) ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:498:5: lc= HSH (uexp= operand -> ^( AGUNQUOTE[$lc,\"unquote\"] $uexp) | CAT uexp= operand -> ^( AGUNQUOTESPLICE[$lc,\"unquote-splice\"] $uexp) )
            {
            lc=(Token)match(input,HSH,FOLLOW_HSH_in_unquotation3193); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_HSH.add(lc);

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:499:5: (uexp= operand -> ^( AGUNQUOTE[$lc,\"unquote\"] $uexp) | CAT uexp= operand -> ^( AGUNQUOTESPLICE[$lc,\"unquote-splice\"] $uexp) )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==LPR||LA46_0==LBC||LA46_0==LBR||LA46_0==DOT||(LA46_0>=NAME && LA46_0<=SELF)||LA46_0==LKU||(LA46_0>=BQU && LA46_0<=HSH)||(LA46_0>=ARW && LA46_0<=USD)) ) {
                alt46=1;
            }
            else if ( (LA46_0==CAT) ) {
                alt46=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:499:7: uexp= operand
                    {
                    pushFollow(FOLLOW_operand_in_unquotation3203);
                    uexp=operand();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_operand.add(uexp.getTree());


                    // AST REWRITE
                    // elements: uexp
                    // token labels: 
                    // rule labels: retval, uexp
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_uexp=new RewriteRuleSubtreeStream(adaptor,"rule uexp",uexp!=null?uexp.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 500:7: -> ^( AGUNQUOTE[$lc,\"unquote\"] $uexp)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:500:10: ^( AGUNQUOTE[$lc,\"unquote\"] $uexp)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGUNQUOTE, lc, "unquote"), root_1);

                        adaptor.addChild(root_1, stream_uexp.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:501:7: CAT uexp= operand
                    {
                    CAT84=(Token)match(input,CAT,FOLLOW_CAT_in_unquotation3227); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CAT.add(CAT84);

                    pushFollow(FOLLOW_operand_in_unquotation3231);
                    uexp=operand();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_operand.add(uexp.getTree());


                    // AST REWRITE
                    // elements: uexp
                    // token labels: 
                    // rule labels: retval, uexp
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_uexp=new RewriteRuleSubtreeStream(adaptor,"rule uexp",uexp!=null?uexp.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 502:7: -> ^( AGUNQUOTESPLICE[$lc,\"unquote-splice\"] $uexp)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:502:10: ^( AGUNQUOTESPLICE[$lc,\"unquote-splice\"] $uexp)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGUNQUOTESPLICE, lc, "unquote-splice"), root_1);

                        adaptor.addChild(root_1, stream_uexp.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "unquotation"

    public static class curried_invocation_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "curried_invocation"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:507:1: curried_invocation[Tree functor] : ( ( LPR | LBR | DOT | SEL | ARW | USD | CAR )=>i= invoke_expression[functor] curried_invocation[i.tree] | -> ^() );
    public final AmbientTalkParser.curried_invocation_return curried_invocation(Tree functor) throws RecognitionException {
        AmbientTalkParser.curried_invocation_return retval = new AmbientTalkParser.curried_invocation_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.invoke_expression_return i = null;

        AmbientTalkParser.curried_invocation_return curried_invocation85 = null;



        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:508:3: ( ( LPR | LBR | DOT | SEL | ARW | USD | CAR )=>i= invoke_expression[functor] curried_invocation[i.tree] | -> ^() )
            int alt47=2;
            alt47 = dfa47.predict(input);
            switch (alt47) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:508:5: ( LPR | LBR | DOT | SEL | ARW | USD | CAR )=>i= invoke_expression[functor] curried_invocation[i.tree]
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_invoke_expression_in_curried_invocation3306);
                    i=invoke_expression(functor);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, i.getTree());
                    pushFollow(FOLLOW_curried_invocation_in_curried_invocation3309);
                    curried_invocation85=curried_invocation(i.tree);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, curried_invocation85.getTree());

                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:510:5: 
                    {

                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 510:5: -> ^()
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:510:8: ^()
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(functor, root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "curried_invocation"

    public static class invoke_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "invoke_expression"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:515:1: invoke_expression[Tree functor] : (lc= LPR args= commalist RPR -> ^( AGAPPLICATION[$lc,\"application\"] $args) | tab= tabulation -> ^( AGTABULATION[\"tabulation\"] $tab) | ( DOT variable LPR )=>msg= message -> ^( AGSEND[\"send\"] $msg) | fsl= zero_arity_invocation -> ^( AGSEND[\"zero-send\"] $fsl) | sel= selection -> ^( AGSELECT[\"selection\"] $sel) | ( ARW variable LPR )=>amsg= async_message -> ^( AGSEND[\"async-send\"] $amsg) | ( CAR variable LPR )=>del= delegation_message -> ^( AGSEND[\"delegation-send\"] $del) | ( USD expression )=>usd= universal_message -> ^( AGSEND[\"universal-send\"] $usd) );
    public final AmbientTalkParser.invoke_expression_return invoke_expression(Tree functor) throws RecognitionException {
        AmbientTalkParser.invoke_expression_return retval = new AmbientTalkParser.invoke_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token RPR86=null;
        AmbientTalkParser.commalist_return args = null;

        AmbientTalkParser.tabulation_return tab = null;

        AmbientTalkParser.message_return msg = null;

        AmbientTalkParser.zero_arity_invocation_return fsl = null;

        AmbientTalkParser.selection_return sel = null;

        AmbientTalkParser.async_message_return amsg = null;

        AmbientTalkParser.delegation_message_return del = null;

        AmbientTalkParser.universal_message_return usd = null;


        CommonTree lc_tree=null;
        CommonTree RPR86_tree=null;
        RewriteRuleTokenStream stream_RPR=new RewriteRuleTokenStream(adaptor,"token RPR");
        RewriteRuleTokenStream stream_LPR=new RewriteRuleTokenStream(adaptor,"token LPR");
        RewriteRuleSubtreeStream stream_message=new RewriteRuleSubtreeStream(adaptor,"rule message");
        RewriteRuleSubtreeStream stream_delegation_message=new RewriteRuleSubtreeStream(adaptor,"rule delegation_message");
        RewriteRuleSubtreeStream stream_universal_message=new RewriteRuleSubtreeStream(adaptor,"rule universal_message");
        RewriteRuleSubtreeStream stream_async_message=new RewriteRuleSubtreeStream(adaptor,"rule async_message");
        RewriteRuleSubtreeStream stream_zero_arity_invocation=new RewriteRuleSubtreeStream(adaptor,"rule zero_arity_invocation");
        RewriteRuleSubtreeStream stream_tabulation=new RewriteRuleSubtreeStream(adaptor,"rule tabulation");
        RewriteRuleSubtreeStream stream_commalist=new RewriteRuleSubtreeStream(adaptor,"rule commalist");
        RewriteRuleSubtreeStream stream_selection=new RewriteRuleSubtreeStream(adaptor,"rule selection");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:516:3: (lc= LPR args= commalist RPR -> ^( AGAPPLICATION[$lc,\"application\"] $args) | tab= tabulation -> ^( AGTABULATION[\"tabulation\"] $tab) | ( DOT variable LPR )=>msg= message -> ^( AGSEND[\"send\"] $msg) | fsl= zero_arity_invocation -> ^( AGSEND[\"zero-send\"] $fsl) | sel= selection -> ^( AGSELECT[\"selection\"] $sel) | ( ARW variable LPR )=>amsg= async_message -> ^( AGSEND[\"async-send\"] $amsg) | ( CAR variable LPR )=>del= delegation_message -> ^( AGSEND[\"delegation-send\"] $del) | ( USD expression )=>usd= universal_message -> ^( AGSEND[\"universal-send\"] $usd) )
            int alt48=8;
            alt48 = dfa48.predict(input);
            switch (alt48) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:517:5: lc= LPR args= commalist RPR
                    {
                    lc=(Token)match(input,LPR,FOLLOW_LPR_in_invoke_expression3343); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPR.add(lc);

                    pushFollow(FOLLOW_commalist_in_invoke_expression3347);
                    args=commalist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_commalist.add(args.getTree());
                    RPR86=(Token)match(input,RPR,FOLLOW_RPR_in_invoke_expression3349); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPR.add(RPR86);



                    // AST REWRITE
                    // elements: args
                    // token labels: 
                    // rule labels: retval, args
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_args=new RewriteRuleSubtreeStream(adaptor,"rule args",args!=null?args.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 518:5: -> ^( AGAPPLICATION[$lc,\"application\"] $args)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:518:8: ^( AGAPPLICATION[$lc,\"application\"] $args)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGAPPLICATION, lc, "application"), root_1);

                        adaptor.addChild(root_1, functor);
                        adaptor.addChild(root_1, stream_args.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:520:5: tab= tabulation
                    {
                    pushFollow(FOLLOW_tabulation_in_invoke_expression3378);
                    tab=tabulation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tabulation.add(tab.getTree());


                    // AST REWRITE
                    // elements: tab
                    // token labels: 
                    // rule labels: retval, tab
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_tab=new RewriteRuleSubtreeStream(adaptor,"rule tab",tab!=null?tab.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 521:5: -> ^( AGTABULATION[\"tabulation\"] $tab)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:521:8: ^( AGTABULATION[\"tabulation\"] $tab)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGTABULATION, "tabulation"), root_1);

                        adaptor.addChild(root_1, functor);
                        adaptor.addChild(root_1, stream_tab.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:524:5: ( DOT variable LPR )=>msg= message
                    {
                    pushFollow(FOLLOW_message_in_invoke_expression3424);
                    msg=message();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_message.add(msg.getTree());


                    // AST REWRITE
                    // elements: msg
                    // token labels: 
                    // rule labels: retval, msg
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_msg=new RewriteRuleSubtreeStream(adaptor,"rule msg",msg!=null?msg.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 525:5: -> ^( AGSEND[\"send\"] $msg)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:525:8: ^( AGSEND[\"send\"] $msg)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSEND, "send"), root_1);

                        adaptor.addChild(root_1, functor);
                        adaptor.addChild(root_1, stream_msg.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:527:5: fsl= zero_arity_invocation
                    {
                    pushFollow(FOLLOW_zero_arity_invocation_in_invoke_expression3453);
                    fsl=zero_arity_invocation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_zero_arity_invocation.add(fsl.getTree());


                    // AST REWRITE
                    // elements: fsl
                    // token labels: 
                    // rule labels: retval, fsl
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_fsl=new RewriteRuleSubtreeStream(adaptor,"rule fsl",fsl!=null?fsl.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 528:5: -> ^( AGSEND[\"zero-send\"] $fsl)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:528:8: ^( AGSEND[\"zero-send\"] $fsl)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSEND, "zero-send"), root_1);

                        adaptor.addChild(root_1, functor);
                        adaptor.addChild(root_1, stream_fsl.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:530:5: sel= selection
                    {
                    pushFollow(FOLLOW_selection_in_invoke_expression3482);
                    sel=selection();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_selection.add(sel.getTree());


                    // AST REWRITE
                    // elements: sel
                    // token labels: 
                    // rule labels: retval, sel
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_sel=new RewriteRuleSubtreeStream(adaptor,"rule sel",sel!=null?sel.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 531:5: -> ^( AGSELECT[\"selection\"] $sel)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:531:8: ^( AGSELECT[\"selection\"] $sel)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSELECT, "selection"), root_1);

                        adaptor.addChild(root_1, functor);
                        adaptor.addChild(root_1, stream_sel.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:533:5: ( ARW variable LPR )=>amsg= async_message
                    {
                    pushFollow(FOLLOW_async_message_in_invoke_expression3523);
                    amsg=async_message();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_async_message.add(amsg.getTree());


                    // AST REWRITE
                    // elements: amsg
                    // token labels: 
                    // rule labels: retval, amsg
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_amsg=new RewriteRuleSubtreeStream(adaptor,"rule amsg",amsg!=null?amsg.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 534:5: -> ^( AGSEND[\"async-send\"] $amsg)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:534:8: ^( AGSEND[\"async-send\"] $amsg)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSEND, "async-send"), root_1);

                        adaptor.addChild(root_1, functor);
                        adaptor.addChild(root_1, stream_amsg.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:536:5: ( CAR variable LPR )=>del= delegation_message
                    {
                    pushFollow(FOLLOW_delegation_message_in_invoke_expression3565);
                    del=delegation_message();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_delegation_message.add(del.getTree());


                    // AST REWRITE
                    // elements: del
                    // token labels: 
                    // rule labels: retval, del
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_del=new RewriteRuleSubtreeStream(adaptor,"rule del",del!=null?del.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 537:5: -> ^( AGSEND[\"delegation-send\"] $del)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:537:8: ^( AGSEND[\"delegation-send\"] $del)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSEND, "delegation-send"), root_1);

                        adaptor.addChild(root_1, functor);
                        adaptor.addChild(root_1, stream_del.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 8 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:539:5: ( USD expression )=>usd= universal_message
                    {
                    pushFollow(FOLLOW_universal_message_in_invoke_expression3605);
                    usd=universal_message();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_universal_message.add(usd.getTree());


                    // AST REWRITE
                    // elements: usd
                    // token labels: 
                    // rule labels: retval, usd
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_usd=new RewriteRuleSubtreeStream(adaptor,"rule usd",usd!=null?usd.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 540:5: -> ^( AGSEND[\"universal-send\"] $usd)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:540:8: ^( AGSEND[\"universal-send\"] $usd)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGSEND, "universal-send"), root_1);

                        adaptor.addChild(root_1, functor);
                        adaptor.addChild(root_1, stream_usd.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "invoke_expression"

    public static class tabulation_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tabulation"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:543:1: tabulation : lc= LBR idx= expression RBR -> ^( AGTABULATION[$lc,\"tabulation-inner\"] $idx) ;
    public final AmbientTalkParser.tabulation_return tabulation() throws RecognitionException {
        AmbientTalkParser.tabulation_return retval = new AmbientTalkParser.tabulation_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        Token RBR87=null;
        AmbientTalkParser.expression_return idx = null;


        CommonTree lc_tree=null;
        CommonTree RBR87_tree=null;
        RewriteRuleTokenStream stream_RBR=new RewriteRuleTokenStream(adaptor,"token RBR");
        RewriteRuleTokenStream stream_LBR=new RewriteRuleTokenStream(adaptor,"token LBR");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:545:3: (lc= LBR idx= expression RBR -> ^( AGTABULATION[$lc,\"tabulation-inner\"] $idx) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:545:5: lc= LBR idx= expression RBR
            {
            lc=(Token)match(input,LBR,FOLLOW_LBR_in_tabulation3639); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBR.add(lc);

            pushFollow(FOLLOW_expression_in_tabulation3643);
            idx=expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expression.add(idx.getTree());
            RBR87=(Token)match(input,RBR,FOLLOW_RBR_in_tabulation3645); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBR.add(RBR87);



            // AST REWRITE
            // elements: idx
            // token labels: 
            // rule labels: retval, idx
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_idx=new RewriteRuleSubtreeStream(adaptor,"rule idx",idx!=null?idx.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 546:5: -> ^( AGTABULATION[$lc,\"tabulation-inner\"] $idx)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:546:8: ^( AGTABULATION[$lc,\"tabulation-inner\"] $idx)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGTABULATION, lc, "tabulation-inner"), root_1);

                adaptor.addChild(root_1, stream_idx.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tabulation"

    public static class zero_arity_invocation_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "zero_arity_invocation"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:549:1: zero_arity_invocation : lc= DOT var= variable -> ^( AGFIELDSELECT[$lc,\"field-selection\"] $var) ;
    public final AmbientTalkParser.zero_arity_invocation_return zero_arity_invocation() throws RecognitionException {
        AmbientTalkParser.zero_arity_invocation_return retval = new AmbientTalkParser.zero_arity_invocation_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        AmbientTalkParser.variable_return var = null;


        CommonTree lc_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_variable=new RewriteRuleSubtreeStream(adaptor,"rule variable");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:550:3: (lc= DOT var= variable -> ^( AGFIELDSELECT[$lc,\"field-selection\"] $var) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:550:5: lc= DOT var= variable
            {
            lc=(Token)match(input,DOT,FOLLOW_DOT_in_zero_arity_invocation3678); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DOT.add(lc);

            pushFollow(FOLLOW_variable_in_zero_arity_invocation3682);
            var=variable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_variable.add(var.getTree());


            // AST REWRITE
            // elements: var
            // token labels: 
            // rule labels: retval, var
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_var=new RewriteRuleSubtreeStream(adaptor,"rule var",var!=null?var.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 551:5: -> ^( AGFIELDSELECT[$lc,\"field-selection\"] $var)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:551:8: ^( AGFIELDSELECT[$lc,\"field-selection\"] $var)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGFIELDSELECT, lc, "field-selection"), root_1);

                adaptor.addChild(root_1, stream_var.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "zero_arity_invocation"

    public static class selection_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selection"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:553:1: selection : SEL variable_or_keyword_or_assignment ;
    public final AmbientTalkParser.selection_return selection() throws RecognitionException {
        AmbientTalkParser.selection_return retval = new AmbientTalkParser.selection_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEL88=null;
        AmbientTalkParser.variable_or_keyword_or_assignment_return variable_or_keyword_or_assignment89 = null;


        CommonTree SEL88_tree=null;

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:554:3: ( SEL variable_or_keyword_or_assignment )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:554:5: SEL variable_or_keyword_or_assignment
            {
            root_0 = (CommonTree)adaptor.nil();

            SEL88=(Token)match(input,SEL,FOLLOW_SEL_in_selection3708); if (state.failed) return retval;
            pushFollow(FOLLOW_variable_or_keyword_or_assignment_in_selection3711);
            variable_or_keyword_or_assignment89=variable_or_keyword_or_assignment();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) root_0 = (CommonTree)adaptor.becomeRoot(variable_or_keyword_or_assignment89.getTree(), root_0);

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selection"

    public static class application_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "application"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:563:1: application : c= canonical (a= annotation )? -> ^( AGAPPLICATION[\"canonical-application\"] $c ( $a)? ) ;
    public final AmbientTalkParser.application_return application() throws RecognitionException {
        AmbientTalkParser.application_return retval = new AmbientTalkParser.application_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.canonical_return c = null;

        AmbientTalkParser.annotation_return a = null;


        RewriteRuleSubtreeStream stream_annotation=new RewriteRuleSubtreeStream(adaptor,"rule annotation");
        RewriteRuleSubtreeStream stream_canonical=new RewriteRuleSubtreeStream(adaptor,"rule canonical");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:573:3: (c= canonical (a= annotation )? -> ^( AGAPPLICATION[\"canonical-application\"] $c ( $a)? ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:573:5: c= canonical (a= annotation )?
            {
            pushFollow(FOLLOW_canonical_in_application3740);
            c=canonical();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_canonical.add(c.getTree());
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:573:18: (a= annotation )?
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==CAT) ) {
                alt49=1;
            }
            switch (alt49) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:573:18: a= annotation
                    {
                    pushFollow(FOLLOW_annotation_in_application3744);
                    a=annotation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_annotation.add(a.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: c, a
            // token labels: 
            // rule labels: retval, c, a
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_c=new RewriteRuleSubtreeStream(adaptor,"rule c",c!=null?c.tree:null);
            RewriteRuleSubtreeStream stream_a=new RewriteRuleSubtreeStream(adaptor,"rule a",a!=null?a.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 574:5: -> ^( AGAPPLICATION[\"canonical-application\"] $c ( $a)? )
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:574:8: ^( AGAPPLICATION[\"canonical-application\"] $c ( $a)? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGAPPLICATION, "canonical-application"), root_1);

                adaptor.addChild(root_1, stream_c.nextTree());
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:574:52: ( $a)?
                if ( stream_a.hasNext() ) {
                    adaptor.addChild(root_1, stream_a.nextTree());

                }
                stream_a.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "application"

    public static class canonical_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "canonical"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:577:1: canonical : var= variable LPR args= commalist RPR ;
    public final AmbientTalkParser.canonical_return canonical() throws RecognitionException {
        AmbientTalkParser.canonical_return retval = new AmbientTalkParser.canonical_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPR90=null;
        Token RPR91=null;
        AmbientTalkParser.variable_return var = null;

        AmbientTalkParser.commalist_return args = null;


        CommonTree LPR90_tree=null;
        CommonTree RPR91_tree=null;

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:578:3: (var= variable LPR args= commalist RPR )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:578:5: var= variable LPR args= commalist RPR
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_variable_in_canonical3779);
            var=variable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, var.getTree());
            LPR90=(Token)match(input,LPR,FOLLOW_LPR_in_canonical3781); if (state.failed) return retval;
            pushFollow(FOLLOW_commalist_in_canonical3786);
            args=commalist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, args.getTree());
            RPR91=(Token)match(input,RPR,FOLLOW_RPR_in_canonical3788); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "canonical"

    public static class annotation_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "annotation"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:587:1: annotation : ( CAT )=> CAT annotation_expression ;
    public final AmbientTalkParser.annotation_return annotation() throws RecognitionException {
        AmbientTalkParser.annotation_return retval = new AmbientTalkParser.annotation_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CAT92=null;
        AmbientTalkParser.annotation_expression_return annotation_expression93 = null;


        CommonTree CAT92_tree=null;

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:588:3: ( ( CAT )=> CAT annotation_expression )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:588:5: ( CAT )=> CAT annotation_expression
            {
            root_0 = (CommonTree)adaptor.nil();

            CAT92=(Token)match(input,CAT,FOLLOW_CAT_in_annotation3810); if (state.failed) return retval;
            pushFollow(FOLLOW_annotation_expression_in_annotation3813);
            annotation_expression93=annotation_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) root_0 = (CommonTree)adaptor.becomeRoot(annotation_expression93.getTree(), root_0);

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "annotation"

    public static class annotation_expression_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "annotation_expression"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:591:1: annotation_expression : symbol ;
    public final AmbientTalkParser.annotation_expression_return annotation_expression() throws RecognitionException {
        AmbientTalkParser.annotation_expression_return retval = new AmbientTalkParser.annotation_expression_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.symbol_return symbol94 = null;



        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:592:3: ( symbol )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:592:5: symbol
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_symbol_in_annotation_expression3827);
            symbol94=symbol();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, symbol94.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "annotation_expression"

    public static class keywordlist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "keywordlist"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:614:1: keywordlist : sk+= singlekeyword (sk+= singlekeyword )* ;
    public final AmbientTalkParser.keywordlist_return keywordlist() throws RecognitionException {
        AmbientTalkParser.keywordlist_return retval = new AmbientTalkParser.keywordlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        List list_sk=null;
        RuleReturnScope sk = null;

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:615:3: (sk+= singlekeyword (sk+= singlekeyword )* )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:615:5: sk+= singlekeyword (sk+= singlekeyword )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_singlekeyword_in_keywordlist3868);
            sk=singlekeyword();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, sk.getTree());
            if (list_sk==null) list_sk=new ArrayList();
            list_sk.add(sk.getTree());

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:615:23: (sk+= singlekeyword )*
            loop50:
            do {
                int alt50=2;
                int LA50_0 = input.LA(1);

                if ( (LA50_0==KEY) ) {
                    alt50=1;
                }


                switch (alt50) {
            	case 1 :
            	    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:615:24: sk+= singlekeyword
            	    {
            	    pushFollow(FOLLOW_singlekeyword_in_keywordlist3873);
            	    sk=singlekeyword();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, sk.getTree());
            	    if (list_sk==null) list_sk=new ArrayList();
            	    list_sk.add(sk.getTree());


            	    }
            	    break;

            	default :
            	    break loop50;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "keywordlist"

    public static class singlekeyword_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "singlekeyword"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:617:1: singlekeyword : ( KEY )=> KEY argument ;
    public final AmbientTalkParser.singlekeyword_return singlekeyword() throws RecognitionException {
        AmbientTalkParser.singlekeyword_return retval = new AmbientTalkParser.singlekeyword_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token KEY95=null;
        AmbientTalkParser.argument_return argument96 = null;


        CommonTree KEY95_tree=null;

        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:618:3: ( ( KEY )=> KEY argument )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:618:5: ( KEY )=> KEY argument
            {
            root_0 = (CommonTree)adaptor.nil();

            KEY95=(Token)match(input,KEY,FOLLOW_KEY_in_singlekeyword3893); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            KEY95_tree = (CommonTree)adaptor.create(KEY95);
            root_0 = (CommonTree)adaptor.becomeRoot(KEY95_tree, root_0);
            }
            pushFollow(FOLLOW_argument_in_singlekeyword3896);
            argument96=argument();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, argument96.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "singlekeyword"

    public static class argument_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argument"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:620:1: argument : expression ;
    public final AmbientTalkParser.argument_return argument() throws RecognitionException {
        AmbientTalkParser.argument_return retval = new AmbientTalkParser.argument_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        AmbientTalkParser.expression_return expression97 = null;



        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:621:3: ( expression )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:621:5: expression
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_expression_in_argument3908);
            expression97=expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, expression97.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "argument"

    public static class message_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "message"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:640:1: message : lc= DOT ( ( variable LPR )=>app= application -> ^( AGMESSAGE[$lc,\"message\"] $app) | var= variable -> ^( AGFIELDSELECT[$lc,\"field-selection-msg\"] $var) ) ;
    public final AmbientTalkParser.message_return message() throws RecognitionException {
        AmbientTalkParser.message_return retval = new AmbientTalkParser.message_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        AmbientTalkParser.application_return app = null;

        AmbientTalkParser.variable_return var = null;


        CommonTree lc_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_application=new RewriteRuleSubtreeStream(adaptor,"rule application");
        RewriteRuleSubtreeStream stream_variable=new RewriteRuleSubtreeStream(adaptor,"rule variable");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:641:3: (lc= DOT ( ( variable LPR )=>app= application -> ^( AGMESSAGE[$lc,\"message\"] $app) | var= variable -> ^( AGFIELDSELECT[$lc,\"field-selection-msg\"] $var) ) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:641:5: lc= DOT ( ( variable LPR )=>app= application -> ^( AGMESSAGE[$lc,\"message\"] $app) | var= variable -> ^( AGFIELDSELECT[$lc,\"field-selection-msg\"] $var) )
            {
            lc=(Token)match(input,DOT,FOLLOW_DOT_in_message3971); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DOT.add(lc);

            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:642:5: ( ( variable LPR )=>app= application -> ^( AGMESSAGE[$lc,\"message\"] $app) | var= variable -> ^( AGFIELDSELECT[$lc,\"field-selection-msg\"] $var) )
            int alt51=2;
            switch ( input.LA(1) ) {
            case NAME:
                {
                int LA51_1 = input.LA(2);

                if ( (synpred16_AmbientTalk()) ) {
                    alt51=1;
                }
                else if ( (true) ) {
                    alt51=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 51, 1, input);

                    throw nvae;
                }
                }
                break;
            case SELF:
                {
                int LA51_2 = input.LA(2);

                if ( (synpred16_AmbientTalk()) ) {
                    alt51=1;
                }
                else if ( (true) ) {
                    alt51=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 51, 2, input);

                    throw nvae;
                }
                }
                break;
            case CMP:
                {
                int LA51_3 = input.LA(2);

                if ( (synpred16_AmbientTalk()) ) {
                    alt51=1;
                }
                else if ( (true) ) {
                    alt51=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 51, 3, input);

                    throw nvae;
                }
                }
                break;
            case ADD:
                {
                int LA51_4 = input.LA(2);

                if ( (synpred16_AmbientTalk()) ) {
                    alt51=1;
                }
                else if ( (true) ) {
                    alt51=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 51, 4, input);

                    throw nvae;
                }
                }
                break;
            case MUL:
                {
                int LA51_5 = input.LA(2);

                if ( (synpred16_AmbientTalk()) ) {
                    alt51=1;
                }
                else if ( (true) ) {
                    alt51=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 51, 5, input);

                    throw nvae;
                }
                }
                break;
            case POW:
                {
                int LA51_6 = input.LA(2);

                if ( (synpred16_AmbientTalk()) ) {
                    alt51=1;
                }
                else if ( (true) ) {
                    alt51=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 51, 6, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                throw nvae;
            }

            switch (alt51) {
                case 1 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:644:7: ( variable LPR )=>app= application
                    {
                    pushFollow(FOLLOW_application_in_message4005);
                    app=application();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_application.add(app.getTree());


                    // AST REWRITE
                    // elements: app
                    // token labels: 
                    // rule labels: app, retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_app=new RewriteRuleSubtreeStream(adaptor,"rule app",app!=null?app.tree:null);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 644:43: -> ^( AGMESSAGE[$lc,\"message\"] $app)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:644:46: ^( AGMESSAGE[$lc,\"message\"] $app)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGMESSAGE, lc, "message"), root_1);

                        adaptor.addChild(root_1, stream_app.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:646:7: var= variable
                    {
                    pushFollow(FOLLOW_variable_in_message4032);
                    var=variable();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_variable.add(var.getTree());


                    // AST REWRITE
                    // elements: var
                    // token labels: 
                    // rule labels: retval, var
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_var=new RewriteRuleSubtreeStream(adaptor,"rule var",var!=null?var.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 646:20: -> ^( AGFIELDSELECT[$lc,\"field-selection-msg\"] $var)
                    {
                        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:646:23: ^( AGFIELDSELECT[$lc,\"field-selection-msg\"] $var)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGFIELDSELECT, lc, "field-selection-msg"), root_1);

                        adaptor.addChild(root_1, stream_var.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "message"

    public static class async_message_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "async_message"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:652:1: async_message : lc= ARW apl= application -> ^( AGASYNCMSG[$lc,\"async-message\"] $apl) ;
    public final AmbientTalkParser.async_message_return async_message() throws RecognitionException {
        AmbientTalkParser.async_message_return retval = new AmbientTalkParser.async_message_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        AmbientTalkParser.application_return apl = null;


        CommonTree lc_tree=null;
        RewriteRuleTokenStream stream_ARW=new RewriteRuleTokenStream(adaptor,"token ARW");
        RewriteRuleSubtreeStream stream_application=new RewriteRuleSubtreeStream(adaptor,"rule application");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:653:3: (lc= ARW apl= application -> ^( AGASYNCMSG[$lc,\"async-message\"] $apl) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:653:5: lc= ARW apl= application
            {
            lc=(Token)match(input,ARW,FOLLOW_ARW_in_async_message4065); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ARW.add(lc);

            pushFollow(FOLLOW_application_in_async_message4069);
            apl=application();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_application.add(apl.getTree());


            // AST REWRITE
            // elements: apl
            // token labels: 
            // rule labels: retval, apl
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_apl=new RewriteRuleSubtreeStream(adaptor,"rule apl",apl!=null?apl.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 654:5: -> ^( AGASYNCMSG[$lc,\"async-message\"] $apl)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:654:8: ^( AGASYNCMSG[$lc,\"async-message\"] $apl)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGASYNCMSG, lc, "async-message"), root_1);

                adaptor.addChild(root_1, stream_apl.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "async_message"

    public static class delegation_message_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "delegation_message"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:659:1: delegation_message : lc= CAR apl= application -> ^( AGDELMSG[$lc,\"delegation-message\"] $apl) ;
    public final AmbientTalkParser.delegation_message_return delegation_message() throws RecognitionException {
        AmbientTalkParser.delegation_message_return retval = new AmbientTalkParser.delegation_message_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        AmbientTalkParser.application_return apl = null;


        CommonTree lc_tree=null;
        RewriteRuleTokenStream stream_CAR=new RewriteRuleTokenStream(adaptor,"token CAR");
        RewriteRuleSubtreeStream stream_application=new RewriteRuleSubtreeStream(adaptor,"rule application");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:660:3: (lc= CAR apl= application -> ^( AGDELMSG[$lc,\"delegation-message\"] $apl) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:660:5: lc= CAR apl= application
            {
            lc=(Token)match(input,CAR,FOLLOW_CAR_in_delegation_message4100); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CAR.add(lc);

            pushFollow(FOLLOW_application_in_delegation_message4104);
            apl=application();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_application.add(apl.getTree());


            // AST REWRITE
            // elements: apl
            // token labels: 
            // rule labels: retval, apl
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_apl=new RewriteRuleSubtreeStream(adaptor,"rule apl",apl!=null?apl.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 661:5: -> ^( AGDELMSG[$lc,\"delegation-message\"] $apl)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:661:8: ^( AGDELMSG[$lc,\"delegation-message\"] $apl)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGDELMSG, lc, "delegation-message"), root_1);

                adaptor.addChild(root_1, stream_apl.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "delegation_message"

    public static class universal_message_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "universal_message"
    // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:666:1: universal_message : lc= USD apl= invocation -> ^( AGUNIVMSG[$lc,\"universal-message\"] $apl) ;
    public final AmbientTalkParser.universal_message_return universal_message() throws RecognitionException {
        AmbientTalkParser.universal_message_return retval = new AmbientTalkParser.universal_message_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token lc=null;
        AmbientTalkParser.invocation_return apl = null;


        CommonTree lc_tree=null;
        RewriteRuleTokenStream stream_USD=new RewriteRuleTokenStream(adaptor,"token USD");
        RewriteRuleSubtreeStream stream_invocation=new RewriteRuleSubtreeStream(adaptor,"rule invocation");
        try {
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:667:3: (lc= USD apl= invocation -> ^( AGUNIVMSG[$lc,\"universal-message\"] $apl) )
            // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:667:5: lc= USD apl= invocation
            {
            lc=(Token)match(input,USD,FOLLOW_USD_in_universal_message4135); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_USD.add(lc);

            pushFollow(FOLLOW_invocation_in_universal_message4139);
            apl=invocation();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_invocation.add(apl.getTree());


            // AST REWRITE
            // elements: apl
            // token labels: 
            // rule labels: retval, apl
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_apl=new RewriteRuleSubtreeStream(adaptor,"rule apl",apl!=null?apl.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 668:5: -> ^( AGUNIVMSG[$lc,\"universal-message\"] $apl)
            {
                // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:668:8: ^( AGUNIVMSG[$lc,\"universal-message\"] $apl)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AGUNIVMSG, lc, "universal-message"), root_1);

                adaptor.addChild(root_1, stream_apl.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "universal_message"

    // $ANTLR start synpred1_AmbientTalk
    public final void synpred1_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:220:7: ( variable LPR )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:220:8: variable LPR
        {
        pushFollow(FOLLOW_variable_in_synpred1_AmbientTalk1336);
        variable();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPR,FOLLOW_LPR_in_synpred1_AmbientTalk1338); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_AmbientTalk

    // $ANTLR start synpred2_AmbientTalk
    public final void synpred2_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:250:7: ( HSH )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:250:8: HSH
        {
        match(input,HSH,FOLLOW_HSH_in_synpred2_AmbientTalk1552); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_AmbientTalk

    // $ANTLR start synpred3_AmbientTalk
    public final void synpred3_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:268:5: ( EXCLUDE HSH )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:268:6: EXCLUDE HSH
        {
        match(input,EXCLUDE,FOLLOW_EXCLUDE_in_synpred3_AmbientTalk1690); if (state.failed) return ;
        match(input,HSH,FOLLOW_HSH_in_synpred3_AmbientTalk1692); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_AmbientTalk

    // $ANTLR start synpred4_AmbientTalk
    public final void synpred4_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:415:5: ( variable_or_quotation EQL )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:415:6: variable_or_quotation EQL
        {
        pushFollow(FOLLOW_variable_or_quotation_in_synpred4_AmbientTalk2588);
        variable_or_quotation();

        state._fsp--;
        if (state.failed) return ;
        match(input,EQL,FOLLOW_EQL_in_synpred4_AmbientTalk2590); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_AmbientTalk

    // $ANTLR start synpred5_AmbientTalk
    public final void synpred5_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:440:5: ( operator ( LPR | LBR | DOT | ARW | USD ) )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:440:7: operator ( LPR | LBR | DOT | ARW | USD )
        {
        pushFollow(FOLLOW_operator_in_synpred5_AmbientTalk2752);
        operator();

        state._fsp--;
        if (state.failed) return ;
        if ( input.LA(1)==LPR||input.LA(1)==LBR||input.LA(1)==DOT||input.LA(1)==ARW||input.LA(1)==USD ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }


        }
    }
    // $ANTLR end synpred5_AmbientTalk

    // $ANTLR start synpred6_AmbientTalk
    public final void synpred6_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:441:5: ( operator invocation )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:441:7: operator invocation
        {
        pushFollow(FOLLOW_operator_in_synpred6_AmbientTalk2788);
        operator();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_invocation_in_synpred6_AmbientTalk2790);
        invocation();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_AmbientTalk

    // $ANTLR start synpred8_AmbientTalk
    public final void synpred8_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:482:9: ( LBC )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:482:10: LBC
        {
        match(input,LBC,FOLLOW_LBC_in_synpred8_AmbientTalk3060); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_AmbientTalk

    // $ANTLR start synpred9_AmbientTalk
    public final void synpred9_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:508:5: ( LPR | LBR | DOT | SEL | ARW | USD | CAR )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:
        {
        if ( input.LA(1)==LPR||input.LA(1)==LBR||input.LA(1)==DOT||input.LA(1)==SEL||(input.LA(1)>=ARW && input.LA(1)<=USD) ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }


        }
    }
    // $ANTLR end synpred9_AmbientTalk

    // $ANTLR start synpred10_AmbientTalk
    public final void synpred10_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:524:5: ( DOT variable LPR )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:524:7: DOT variable LPR
        {
        match(input,DOT,FOLLOW_DOT_in_synpred10_AmbientTalk3412); if (state.failed) return ;
        pushFollow(FOLLOW_variable_in_synpred10_AmbientTalk3414);
        variable();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPR,FOLLOW_LPR_in_synpred10_AmbientTalk3416); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_AmbientTalk

    // $ANTLR start synpred11_AmbientTalk
    public final void synpred11_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:533:5: ( ARW variable LPR )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:533:7: ARW variable LPR
        {
        match(input,ARW,FOLLOW_ARW_in_synpred11_AmbientTalk3511); if (state.failed) return ;
        pushFollow(FOLLOW_variable_in_synpred11_AmbientTalk3513);
        variable();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPR,FOLLOW_LPR_in_synpred11_AmbientTalk3515); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_AmbientTalk

    // $ANTLR start synpred12_AmbientTalk
    public final void synpred12_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:536:5: ( CAR variable LPR )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:536:7: CAR variable LPR
        {
        match(input,CAR,FOLLOW_CAR_in_synpred12_AmbientTalk3553); if (state.failed) return ;
        pushFollow(FOLLOW_variable_in_synpred12_AmbientTalk3555);
        variable();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPR,FOLLOW_LPR_in_synpred12_AmbientTalk3557); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_AmbientTalk

    // $ANTLR start synpred13_AmbientTalk
    public final void synpred13_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:539:5: ( USD expression )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:539:7: USD expression
        {
        match(input,USD,FOLLOW_USD_in_synpred13_AmbientTalk3595); if (state.failed) return ;
        pushFollow(FOLLOW_expression_in_synpred13_AmbientTalk3597);
        expression();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_AmbientTalk

    // $ANTLR start synpred16_AmbientTalk
    public final void synpred16_AmbientTalk_fragment() throws RecognitionException {   
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:644:7: ( variable LPR )
        // /home/bcorne/ATDOC/Documenter/grammar/AmbientTalk.g:644:9: variable LPR
        {
        pushFollow(FOLLOW_variable_in_synpred16_AmbientTalk3995);
        variable();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPR,FOLLOW_LPR_in_synpred16_AmbientTalk3997); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred16_AmbientTalk

    // Delegated rules

    public final boolean synpred6_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred10_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred10_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred9_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred9_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred16_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred16_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred4_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred13_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred13_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred12_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred12_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred8_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred8_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred11_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred11_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_AmbientTalk() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_AmbientTalk_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA5 dfa5 = new DFA5(this);
    protected DFA7 dfa7 = new DFA7(this);
    protected DFA14 dfa14 = new DFA14(this);
    protected DFA23 dfa23 = new DFA23(this);
    protected DFA29 dfa29 = new DFA29(this);
    protected DFA38 dfa38 = new DFA38(this);
    protected DFA42 dfa42 = new DFA42(this);
    protected DFA45 dfa45 = new DFA45(this);
    protected DFA47 dfa47 = new DFA47(this);
    protected DFA48 dfa48 = new DFA48(this);
    static final String DFA5_eotS =
        "\51\uffff";
    static final String DFA5_eofS =
        "\4\uffff\6\13\13\uffff\14\13\1\uffff\1\13\6\uffff";
    static final String DFA5_minS =
        "\1\74\3\uffff\6\71\1\76\2\uffff\2\113\6\75\14\71\1\76\1\71\6\75";
    static final String DFA5_maxS =
        "\1\136\3\uffff\7\136\2\uffff\2\123\32\136";
    static final String DFA5_acceptS =
        "\1\uffff\1\1\1\2\1\3\7\uffff\1\5\1\4\34\uffff";
    static final String DFA5_specialS =
        "\51\uffff}>";
    static final String[] DFA5_transitionS = {
            "\1\1\1\uffff\1\13\1\uffff\1\13\1\uffff\1\12\1\uffff\1\13\1\2"+
            "\2\uffff\1\3\2\uffff\1\4\1\6\1\7\1\10\1\11\3\13\1\5\2\uffff"+
            "\1\13\1\uffff\2\13\2\uffff\3\13",
            "",
            "",
            "",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\15\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\15\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\1\uffff\3\13\1\uffff\1\16\6\uffff\11"+
            "\13\2\uffff\1\13\1\uffff\3\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\1\uffff\3\13\1\uffff\1\16\6\uffff\11"+
            "\13\2\uffff\1\13\1\uffff\3\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\1\uffff\3\13\1\uffff\1\16\6\uffff\11"+
            "\13\2\uffff\1\13\1\uffff\3\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\1\uffff\3\13\1\uffff\1\16\6\uffff\11"+
            "\13\2\uffff\1\13\1\uffff\3\13\1\uffff\3\13",
            "\1\13\1\uffff\1\13\1\uffff\1\13\1\uffff\1\13\6\uffff\1\17\1"+
            "\21\1\22\1\23\1\24\3\13\1\20\1\uffff\1\14\1\13\1\14\2\13\2\uffff"+
            "\3\13",
            "",
            "",
            "\1\25\1\27\1\30\1\31\1\32\3\uffff\1\26",
            "\1\33\1\35\1\36\1\37\1\40\3\uffff\1\34",
            "\1\14\1\13\3\uffff\1\13\1\42\1\13\2\uffff\1\41\4\uffff\4\13"+
            "\12\uffff\1\13\1\uffff\3\13",
            "\1\14\1\13\3\uffff\1\13\1\42\1\13\2\uffff\1\41\4\uffff\4\13"+
            "\12\uffff\1\13\1\uffff\3\13",
            "\1\14\1\13\1\uffff\1\13\1\uffff\1\13\1\42\1\13\2\uffff\1\41"+
            "\3\uffff\11\13\2\uffff\1\13\1\uffff\3\13\1\uffff\3\13",
            "\1\14\1\13\1\uffff\1\13\1\uffff\1\13\1\42\1\13\2\uffff\1\41"+
            "\3\uffff\11\13\2\uffff\1\13\1\uffff\3\13\1\uffff\3\13",
            "\1\14\1\13\1\uffff\1\13\1\uffff\1\13\1\42\1\13\2\uffff\1\41"+
            "\3\uffff\11\13\2\uffff\1\13\1\uffff\3\13\1\uffff\3\13",
            "\1\14\1\13\1\uffff\1\13\1\uffff\1\13\1\42\1\13\2\uffff\1\41"+
            "\3\uffff\11\13\2\uffff\1\13\1\uffff\3\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\13\1\uffff\1\13\1\uffff\1\13\1\uffff\1\13\6\uffff\1\43\1"+
            "\45\1\46\1\47\1\50\3\13\1\44\1\uffff\1\14\1\13\1\14\2\13\2\uffff"+
            "\3\13",
            "\1\13\3\uffff\1\14\1\13\2\uffff\2\13\1\uffff\1\13\7\uffff\4"+
            "\13\12\uffff\1\13\1\uffff\3\13",
            "\1\14\1\13\3\uffff\1\13\1\42\1\13\2\uffff\1\41\4\uffff\4\13"+
            "\12\uffff\1\13\1\uffff\3\13",
            "\1\14\1\13\3\uffff\1\13\1\42\1\13\2\uffff\1\41\4\uffff\4\13"+
            "\12\uffff\1\13\1\uffff\3\13",
            "\1\14\1\13\1\uffff\1\13\1\uffff\1\13\1\42\1\13\2\uffff\1\41"+
            "\3\uffff\11\13\2\uffff\1\13\1\uffff\3\13\1\uffff\3\13",
            "\1\14\1\13\1\uffff\1\13\1\uffff\1\13\1\42\1\13\2\uffff\1\41"+
            "\3\uffff\11\13\2\uffff\1\13\1\uffff\3\13\1\uffff\3\13",
            "\1\14\1\13\1\uffff\1\13\1\uffff\1\13\1\42\1\13\2\uffff\1\41"+
            "\3\uffff\11\13\2\uffff\1\13\1\uffff\3\13\1\uffff\3\13",
            "\1\14\1\13\1\uffff\1\13\1\uffff\1\13\1\42\1\13\2\uffff\1\41"+
            "\3\uffff\11\13\2\uffff\1\13\1\uffff\3\13\1\uffff\3\13"
    };

    static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
    static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
    static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
    static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
    static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
    static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
    static final short[][] DFA5_transition;

    static {
        int numStates = DFA5_transitionS.length;
        DFA5_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
        }
    }

    class DFA5 extends DFA {

        public DFA5(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 5;
            this.eot = DFA5_eot;
            this.eof = DFA5_eof;
            this.min = DFA5_min;
            this.max = DFA5_max;
            this.accept = DFA5_accept;
            this.special = DFA5_special;
            this.transition = DFA5_transition;
        }
        public String getDescription() {
            return "133:7: (def= definition -> ^( AGSTATEMENT $def ( $docs)* ) | tdef= typedefinition -> ^( AGSTATEMENT $tdef ( $docs)* ) | imp= importstatement -> ^( AGSTATEMENT $imp ( $docs)* ) | ass= assignment -> ^( AGSTATEMENT $ass ( $docs)* ) | exp= expression -> ^( AGSTATEMENT $exp ( $docs)* ) )";
        }
    }
    static final String DFA7_eotS =
        "\16\uffff";
    static final String DFA7_eofS =
        "\1\uffff\7\12\6\uffff";
    static final String DFA7_minS =
        "\1\102\7\71\6\uffff";
    static final String DFA7_maxS =
        "\1\161\7\104\6\uffff";
    static final String DFA7_acceptS =
        "\10\uffff\1\2\1\4\1\1\1\3\1\5\1\6";
    static final String DFA7_specialS =
        "\16\uffff}>";
    static final String[] DFA7_transitionS = {
            "\1\11\10\uffff\1\1\1\3\1\4\1\5\1\6\3\uffff\1\2\3\uffff\1\7\31"+
            "\uffff\1\10",
            "\1\12\3\uffff\1\12\1\13\2\uffff\1\12\1\14\1\uffff\1\15",
            "\1\12\3\uffff\1\12\1\13\2\uffff\1\12\1\14\1\uffff\1\15",
            "\1\12\3\uffff\1\12\1\13\2\uffff\1\12\1\14\1\uffff\1\15",
            "\1\12\3\uffff\1\12\1\13\2\uffff\1\12\1\14\1\uffff\1\15",
            "\1\12\3\uffff\1\12\1\13\2\uffff\1\12\1\14\1\uffff\1\15",
            "\1\12\3\uffff\1\12\1\13\2\uffff\1\12\1\14\1\uffff\1\15",
            "\1\12\3\uffff\1\12\1\13\2\uffff\1\12\1\14\1\uffff\1\15",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "170:5: (fld= field_definition -> ^( AGDEFINITION[$lc,\"definition\"] $fld) | key= keyworded_definition -> ^( AGDEFINITION[$lc,\"definition\"] $key) | can= canonical_definition -> ^( AGDEFINITION[$lc,\"definition\"] $can) | mul= multivalue_definition -> ^( AGDEFINITION[$lc,\"definition\"] $mul) | tab= table_definition -> ^( AGDEFINITION[$lc,\"definition\"] $tab) | ext= external_definition -> ^( AGDEFINITION[$lc,\"definition\"] $ext) )";
        }
    }
    static final String DFA14_eotS =
        "\12\uffff";
    static final String DFA14_eofS =
        "\1\uffff\6\11\3\uffff";
    static final String DFA14_minS =
        "\1\113\6\71\3\uffff";
    static final String DFA14_maxS =
        "\1\127\6\101\3\uffff";
    static final String DFA14_acceptS =
        "\7\uffff\2\1\1\2";
    static final String DFA14_specialS =
        "\1\2\1\0\1\3\1\1\1\4\1\5\1\6\3\uffff}>";
    static final String[] DFA14_transitionS = {
            "\1\1\1\3\1\4\1\5\1\6\3\uffff\1\2\3\uffff\1\7",
            "\1\11\3\uffff\1\11\1\10\2\uffff\1\11",
            "\1\11\3\uffff\1\11\1\10\2\uffff\1\11",
            "\1\11\3\uffff\1\11\1\10\2\uffff\1\11",
            "\1\11\3\uffff\1\11\1\10\2\uffff\1\11",
            "\1\11\3\uffff\1\11\1\10\2\uffff\1\11",
            "\1\11\3\uffff\1\11\1\10\2\uffff\1\11",
            "",
            "",
            ""
    };

    static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
    static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
    static final char[] DFA14_min = DFA.unpackEncodedStringToUnsignedChars(DFA14_minS);
    static final char[] DFA14_max = DFA.unpackEncodedStringToUnsignedChars(DFA14_maxS);
    static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
    static final short[] DFA14_special = DFA.unpackEncodedString(DFA14_specialS);
    static final short[][] DFA14_transition;

    static {
        int numStates = DFA14_transitionS.length;
        DFA14_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
        }
    }

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = DFA14_eot;
            this.eof = DFA14_eof;
            this.min = DFA14_min;
            this.max = DFA14_max;
            this.accept = DFA14_accept;
            this.special = DFA14_special;
            this.transition = DFA14_transition;
        }
        public String getDescription() {
            return "220:5: ( ( variable LPR )=>fun= canonical_definition -> ^( AGEXTERNDEFFUN[$lc,\"external-deffun\"] $rec $fun) | nam= variable ( EQL val= expression )? -> ^( AGEXTERNFIELDDEF[$lc,\"external-fielddef\"] $rec $nam ( $val)? ) )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA14_1 = input.LA(1);

                         
                        int index14_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_1==LPR) && (synpred1_AmbientTalk())) {s = 8;}

                        else if ( (LA14_1==EOF||LA14_1==SMC||LA14_1==EQL||LA14_1==RBC) ) {s = 9;}

                         
                        input.seek(index14_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA14_3 = input.LA(1);

                         
                        int index14_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_3==LPR) && (synpred1_AmbientTalk())) {s = 8;}

                        else if ( (LA14_3==EOF||LA14_3==SMC||LA14_3==EQL||LA14_3==RBC) ) {s = 9;}

                         
                        input.seek(index14_3);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA14_0 = input.LA(1);

                         
                        int index14_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_0==NAME) ) {s = 1;}

                        else if ( (LA14_0==SELF) ) {s = 2;}

                        else if ( (LA14_0==CMP) ) {s = 3;}

                        else if ( (LA14_0==ADD) ) {s = 4;}

                        else if ( (LA14_0==MUL) ) {s = 5;}

                        else if ( (LA14_0==POW) ) {s = 6;}

                        else if ( (LA14_0==ASSNAME) && (synpred1_AmbientTalk())) {s = 7;}

                         
                        input.seek(index14_0);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA14_2 = input.LA(1);

                         
                        int index14_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_2==LPR) && (synpred1_AmbientTalk())) {s = 8;}

                        else if ( (LA14_2==EOF||LA14_2==SMC||LA14_2==EQL||LA14_2==RBC) ) {s = 9;}

                         
                        input.seek(index14_2);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA14_4 = input.LA(1);

                         
                        int index14_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_4==LPR) && (synpred1_AmbientTalk())) {s = 8;}

                        else if ( (LA14_4==EOF||LA14_4==SMC||LA14_4==EQL||LA14_4==RBC) ) {s = 9;}

                         
                        input.seek(index14_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA14_5 = input.LA(1);

                         
                        int index14_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_5==LPR) && (synpred1_AmbientTalk())) {s = 8;}

                        else if ( (LA14_5==EOF||LA14_5==SMC||LA14_5==EQL||LA14_5==RBC) ) {s = 9;}

                         
                        input.seek(index14_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA14_6 = input.LA(1);

                         
                        int index14_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA14_6==LPR) && (synpred1_AmbientTalk())) {s = 8;}

                        else if ( (LA14_6==EOF||LA14_6==SMC||LA14_6==EQL||LA14_6==RBC) ) {s = 9;}

                         
                        input.seek(index14_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 14, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA23_eotS =
        "\12\uffff";
    static final String DFA23_eofS =
        "\12\uffff";
    static final String DFA23_minS =
        "\1\102\6\75\3\uffff";
    static final String DFA23_maxS =
        "\1\123\6\104\3\uffff";
    static final String DFA23_acceptS =
        "\7\uffff\1\2\1\1\1\3";
    static final String DFA23_specialS =
        "\12\uffff}>";
    static final String[] DFA23_transitionS = {
            "\1\7\10\uffff\1\1\1\3\1\4\1\5\1\6\3\uffff\1\2",
            "\1\10\6\uffff\1\11",
            "\1\10\6\uffff\1\11",
            "\1\10\6\uffff\1\11",
            "\1\10\6\uffff\1\11",
            "\1\10\6\uffff\1\11",
            "\1\10\6\uffff\1\11",
            "",
            "",
            ""
    };

    static final short[] DFA23_eot = DFA.unpackEncodedString(DFA23_eotS);
    static final short[] DFA23_eof = DFA.unpackEncodedString(DFA23_eofS);
    static final char[] DFA23_min = DFA.unpackEncodedStringToUnsignedChars(DFA23_minS);
    static final char[] DFA23_max = DFA.unpackEncodedStringToUnsignedChars(DFA23_maxS);
    static final short[] DFA23_accept = DFA.unpackEncodedString(DFA23_acceptS);
    static final short[] DFA23_special = DFA.unpackEncodedString(DFA23_specialS);
    static final short[][] DFA23_transition;

    static {
        int numStates = DFA23_transitionS.length;
        DFA23_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA23_transition[i] = DFA.unpackEncodedString(DFA23_transitionS[i]);
        }
    }

    class DFA23 extends DFA {

        public DFA23(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 23;
            this.eot = DFA23_eot;
            this.eof = DFA23_eof;
            this.min = DFA23_min;
            this.max = DFA23_max;
            this.accept = DFA23_accept;
            this.special = DFA23_special;
            this.transition = DFA23_transition;
        }
        public String getDescription() {
            return "277:1: assignment : ( assign_variable | assign_multi | assign_field );";
        }
    }
    static final String DFA29_eotS =
        "\20\uffff";
    static final String DFA29_eofS =
        "\20\uffff";
    static final String DFA29_minS =
        "\1\76\17\uffff";
    static final String DFA29_maxS =
        "\1\136\17\uffff";
    static final String DFA29_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1"+
        "\15\1\16\1\17";
    static final String DFA29_specialS =
        "\20\uffff}>";
    static final String[] DFA29_transitionS = {
            "\1\6\1\uffff\1\7\1\uffff\1\5\1\uffff\1\14\6\uffff\1\4\4\10\1"+
            "\1\1\2\1\3\1\4\2\uffff\1\11\1\uffff\1\12\1\13\2\uffff\1\15\1"+
            "\16\1\17",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA29_eot = DFA.unpackEncodedString(DFA29_eotS);
    static final short[] DFA29_eof = DFA.unpackEncodedString(DFA29_eofS);
    static final char[] DFA29_min = DFA.unpackEncodedStringToUnsignedChars(DFA29_minS);
    static final char[] DFA29_max = DFA.unpackEncodedStringToUnsignedChars(DFA29_maxS);
    static final short[] DFA29_accept = DFA.unpackEncodedString(DFA29_acceptS);
    static final short[] DFA29_special = DFA.unpackEncodedString(DFA29_specialS);
    static final short[][] DFA29_transition;

    static {
        int numStates = DFA29_transitionS.length;
        DFA29_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA29_transition[i] = DFA.unpackEncodedString(DFA29_transitionS[i]);
        }
    }

    class DFA29 extends DFA {

        public DFA29(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 29;
            this.eot = DFA29_eot;
            this.eof = DFA29_eof;
            this.min = DFA29_min;
            this.max = DFA29_max;
            this.accept = DFA29_accept;
            this.special = DFA29_special;
            this.transition = DFA29_transition;
        }
        public String getDescription() {
            return "350:1: operand : (nbr= NUMBER -> ^( AGNUMBER[$nbr,\"number\"] $nbr) | frc= FRACTION -> ^( AGFRACTION[$frc,\"fraction\"] $frc) | txt= TEXT -> ^( AGTEXT[$txt,\"text\"] $txt) | symbol | table | subexpression | block | unary | lookup | quotation | unquotation | message | async_message | delegation_message | universal_message );";
        }
    }
    static final String DFA38_eotS =
        "\13\uffff";
    static final String DFA38_eofS =
        "\13\uffff";
    static final String DFA38_minS =
        "\1\113\7\75\3\uffff";
    static final String DFA38_maxS =
        "\1\127\7\124\3\uffff";
    static final String DFA38_acceptS =
        "\10\uffff\1\3\1\1\1\2";
    static final String DFA38_specialS =
        "\1\uffff\1\6\1\1\1\0\1\5\1\3\1\2\1\4\3\uffff}>";
    static final String[] DFA38_transitionS = {
            "\1\1\1\3\1\4\1\5\1\6\3\uffff\1\2\1\uffff\1\10\1\uffff\1\7",
            "\1\11\1\uffff\1\12\3\uffff\1\12\3\uffff\1\12\14\uffff\1\12",
            "\1\11\1\uffff\1\12\3\uffff\1\12\3\uffff\1\12\14\uffff\1\12",
            "\1\11\1\uffff\1\12\3\uffff\1\12\3\uffff\1\12\14\uffff\1\12",
            "\1\11\1\uffff\1\12\3\uffff\1\12\3\uffff\1\12\14\uffff\1\12",
            "\1\11\1\uffff\1\12\3\uffff\1\12\3\uffff\1\12\14\uffff\1\12",
            "\1\11\1\uffff\1\12\3\uffff\1\12\3\uffff\1\12\14\uffff\1\12",
            "\1\11\1\uffff\1\12\3\uffff\1\12\3\uffff\1\12\14\uffff\1\12",
            "",
            "",
            ""
    };

    static final short[] DFA38_eot = DFA.unpackEncodedString(DFA38_eotS);
    static final short[] DFA38_eof = DFA.unpackEncodedString(DFA38_eofS);
    static final char[] DFA38_min = DFA.unpackEncodedStringToUnsignedChars(DFA38_minS);
    static final char[] DFA38_max = DFA.unpackEncodedStringToUnsignedChars(DFA38_maxS);
    static final short[] DFA38_accept = DFA.unpackEncodedString(DFA38_acceptS);
    static final short[] DFA38_special = DFA.unpackEncodedString(DFA38_specialS);
    static final short[][] DFA38_transition;

    static {
        int numStates = DFA38_transitionS.length;
        DFA38_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA38_transition[i] = DFA.unpackEncodedString(DFA38_transitionS[i]);
        }
    }

    class DFA38 extends DFA {

        public DFA38(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 38;
            this.eot = DFA38_eot;
            this.eof = DFA38_eof;
            this.min = DFA38_min;
            this.max = DFA38_max;
            this.accept = DFA38_accept;
            this.special = DFA38_special;
            this.transition = DFA38_transition;
        }
        public String getDescription() {
            return "414:1: parameter : ( ( variable_or_quotation EQL )=>var= variable_or_quotation EQL exp= expression -> ^( AGASSVAR[\"var-set\"] $var $exp) | voq= variable_or_quotation -> $voq | lc= CAT voq= variable_or_quotation -> ^( AGSPLICE[$lc,\"splice\"] $voq) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA38_3 = input.LA(1);

                         
                        int index38_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA38_3==EQL) && (synpred4_AmbientTalk())) {s = 9;}

                        else if ( (LA38_3==RPR||LA38_3==RBR||LA38_3==COM||LA38_3==PIP) ) {s = 10;}

                         
                        input.seek(index38_3);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA38_2 = input.LA(1);

                         
                        int index38_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA38_2==EQL) && (synpred4_AmbientTalk())) {s = 9;}

                        else if ( (LA38_2==RPR||LA38_2==RBR||LA38_2==COM||LA38_2==PIP) ) {s = 10;}

                         
                        input.seek(index38_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA38_6 = input.LA(1);

                         
                        int index38_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA38_6==EQL) && (synpred4_AmbientTalk())) {s = 9;}

                        else if ( (LA38_6==RPR||LA38_6==RBR||LA38_6==COM||LA38_6==PIP) ) {s = 10;}

                         
                        input.seek(index38_6);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA38_5 = input.LA(1);

                         
                        int index38_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA38_5==EQL) && (synpred4_AmbientTalk())) {s = 9;}

                        else if ( (LA38_5==RPR||LA38_5==RBR||LA38_5==COM||LA38_5==PIP) ) {s = 10;}

                         
                        input.seek(index38_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA38_7 = input.LA(1);

                         
                        int index38_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA38_7==EQL) && (synpred4_AmbientTalk())) {s = 9;}

                        else if ( (LA38_7==RPR||LA38_7==RBR||LA38_7==COM||LA38_7==PIP) ) {s = 10;}

                         
                        input.seek(index38_7);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA38_4 = input.LA(1);

                         
                        int index38_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA38_4==EQL) && (synpred4_AmbientTalk())) {s = 9;}

                        else if ( (LA38_4==RPR||LA38_4==RBR||LA38_4==COM||LA38_4==PIP) ) {s = 10;}

                         
                        input.seek(index38_4);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA38_1 = input.LA(1);

                         
                        int index38_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA38_1==EQL) && (synpred4_AmbientTalk())) {s = 9;}

                        else if ( (LA38_1==RPR||LA38_1==RBR||LA38_1==COM||LA38_1==PIP) ) {s = 10;}

                         
                        input.seek(index38_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 38, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA42_eotS =
        "\32\uffff";
    static final String DFA42_eofS =
        "\32\uffff";
    static final String DFA42_minS =
        "\1\114\4\76\25\uffff";
    static final String DFA42_maxS =
        "\1\117\4\136\25\uffff";
    static final String DFA42_acceptS =
        "\5\uffff\23\2\1\1\1\3";
    static final String DFA42_specialS =
        "\1\uffff\1\2\1\1\1\0\1\3\25\uffff}>";
    static final String[] DFA42_transitionS = {
            "\1\1\1\2\1\3\1\4",
            "\1\5\1\uffff\1\24\1\uffff\1\6\1\uffff\1\7\6\uffff\1\22\1\16"+
            "\1\15\1\14\1\13\1\17\1\20\1\21\1\23\2\uffff\1\25\1\uffff\1\26"+
            "\1\27\2\uffff\1\10\1\11\1\12",
            "\1\5\1\uffff\1\24\1\uffff\1\6\1\uffff\1\7\6\uffff\1\22\1\16"+
            "\1\15\1\14\1\13\1\17\1\20\1\21\1\23\2\uffff\1\25\1\uffff\1\26"+
            "\1\27\2\uffff\1\10\1\11\1\12",
            "\1\5\1\uffff\1\24\1\uffff\1\6\1\uffff\1\7\6\uffff\1\22\1\16"+
            "\1\15\1\14\1\13\1\17\1\20\1\21\1\23\2\uffff\1\25\1\uffff\1\26"+
            "\1\27\2\uffff\1\10\1\11\1\12",
            "\1\5\1\uffff\1\24\1\uffff\1\6\1\uffff\1\7\6\uffff\1\22\1\16"+
            "\1\15\1\14\1\13\1\17\1\20\1\21\1\23\2\uffff\1\25\1\uffff\1\26"+
            "\1\27\2\uffff\1\10\1\11\1\12",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA42_eot = DFA.unpackEncodedString(DFA42_eotS);
    static final short[] DFA42_eof = DFA.unpackEncodedString(DFA42_eofS);
    static final char[] DFA42_min = DFA.unpackEncodedStringToUnsignedChars(DFA42_minS);
    static final char[] DFA42_max = DFA.unpackEncodedStringToUnsignedChars(DFA42_maxS);
    static final short[] DFA42_accept = DFA.unpackEncodedString(DFA42_acceptS);
    static final short[] DFA42_special = DFA.unpackEncodedString(DFA42_specialS);
    static final short[][] DFA42_transition;

    static {
        int numStates = DFA42_transitionS.length;
        DFA42_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA42_transition[i] = DFA.unpackEncodedString(DFA42_transitionS[i]);
        }
    }

    class DFA42 extends DFA {

        public DFA42(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 42;
            this.eot = DFA42_eot;
            this.eof = DFA42_eof;
            this.min = DFA42_min;
            this.max = DFA42_max;
            this.accept = DFA42_accept;
            this.special = DFA42_special;
            this.transition = DFA42_transition;
        }
        public String getDescription() {
            return "439:1: unary : ( ( operator ( LPR | LBR | DOT | ARW | USD ) )=> operator | ( operator invocation )=> (opr= operator arg= invocation ) -> ^( AGAPPLICATION[\"application\"] $opr ^( AGTABLE[\"argument-table\"] $arg) ) | operator );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA42_3 = input.LA(1);

                         
                        int index42_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA42_3==LPR) && (synpred6_AmbientTalk())) {s = 5;}

                        else if ( (LA42_3==LBR) && (synpred6_AmbientTalk())) {s = 6;}

                        else if ( (LA42_3==DOT) && (synpred6_AmbientTalk())) {s = 7;}

                        else if ( (LA42_3==ARW) && (synpred6_AmbientTalk())) {s = 8;}

                        else if ( (LA42_3==CAR) && (synpred6_AmbientTalk())) {s = 9;}

                        else if ( (LA42_3==USD) && (synpred6_AmbientTalk())) {s = 10;}

                        else if ( (LA42_3==POW) && (synpred6_AmbientTalk())) {s = 11;}

                        else if ( (LA42_3==MUL) && (synpred6_AmbientTalk())) {s = 12;}

                        else if ( (LA42_3==ADD) && (synpred6_AmbientTalk())) {s = 13;}

                        else if ( (LA42_3==CMP) && (synpred6_AmbientTalk())) {s = 14;}

                        else if ( (LA42_3==NUMBER) && (synpred6_AmbientTalk())) {s = 15;}

                        else if ( (LA42_3==FRACTION) && (synpred6_AmbientTalk())) {s = 16;}

                        else if ( (LA42_3==TEXT) && (synpred6_AmbientTalk())) {s = 17;}

                        else if ( (LA42_3==NAME) && (synpred6_AmbientTalk())) {s = 18;}

                        else if ( (LA42_3==SELF) && (synpred6_AmbientTalk())) {s = 19;}

                        else if ( (LA42_3==LBC) && (synpred6_AmbientTalk())) {s = 20;}

                        else if ( (LA42_3==LKU) && (synpred6_AmbientTalk())) {s = 21;}

                        else if ( (LA42_3==BQU) && (synpred6_AmbientTalk())) {s = 22;}

                        else if ( (LA42_3==HSH) && (synpred6_AmbientTalk())) {s = 23;}

                        else if ( (synpred5_AmbientTalk()) ) {s = 24;}

                        else if ( (true) ) {s = 25;}

                         
                        input.seek(index42_3);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA42_2 = input.LA(1);

                         
                        int index42_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA42_2==LPR) && (synpred6_AmbientTalk())) {s = 5;}

                        else if ( (LA42_2==LBR) && (synpred6_AmbientTalk())) {s = 6;}

                        else if ( (LA42_2==DOT) && (synpred6_AmbientTalk())) {s = 7;}

                        else if ( (LA42_2==ARW) && (synpred6_AmbientTalk())) {s = 8;}

                        else if ( (LA42_2==CAR) && (synpred6_AmbientTalk())) {s = 9;}

                        else if ( (LA42_2==USD) && (synpred6_AmbientTalk())) {s = 10;}

                        else if ( (LA42_2==POW) && (synpred6_AmbientTalk())) {s = 11;}

                        else if ( (LA42_2==MUL) && (synpred6_AmbientTalk())) {s = 12;}

                        else if ( (LA42_2==ADD) && (synpred6_AmbientTalk())) {s = 13;}

                        else if ( (LA42_2==CMP) && (synpred6_AmbientTalk())) {s = 14;}

                        else if ( (LA42_2==NUMBER) && (synpred6_AmbientTalk())) {s = 15;}

                        else if ( (LA42_2==FRACTION) && (synpred6_AmbientTalk())) {s = 16;}

                        else if ( (LA42_2==TEXT) && (synpred6_AmbientTalk())) {s = 17;}

                        else if ( (LA42_2==NAME) && (synpred6_AmbientTalk())) {s = 18;}

                        else if ( (LA42_2==SELF) && (synpred6_AmbientTalk())) {s = 19;}

                        else if ( (LA42_2==LBC) && (synpred6_AmbientTalk())) {s = 20;}

                        else if ( (LA42_2==LKU) && (synpred6_AmbientTalk())) {s = 21;}

                        else if ( (LA42_2==BQU) && (synpred6_AmbientTalk())) {s = 22;}

                        else if ( (LA42_2==HSH) && (synpred6_AmbientTalk())) {s = 23;}

                        else if ( (synpred5_AmbientTalk()) ) {s = 24;}

                        else if ( (true) ) {s = 25;}

                         
                        input.seek(index42_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA42_1 = input.LA(1);

                         
                        int index42_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA42_1==LPR) && (synpred6_AmbientTalk())) {s = 5;}

                        else if ( (LA42_1==LBR) && (synpred6_AmbientTalk())) {s = 6;}

                        else if ( (LA42_1==DOT) && (synpred6_AmbientTalk())) {s = 7;}

                        else if ( (LA42_1==ARW) && (synpred6_AmbientTalk())) {s = 8;}

                        else if ( (LA42_1==CAR) && (synpred6_AmbientTalk())) {s = 9;}

                        else if ( (LA42_1==USD) && (synpred6_AmbientTalk())) {s = 10;}

                        else if ( (LA42_1==POW) && (synpred6_AmbientTalk())) {s = 11;}

                        else if ( (LA42_1==MUL) && (synpred6_AmbientTalk())) {s = 12;}

                        else if ( (LA42_1==ADD) && (synpred6_AmbientTalk())) {s = 13;}

                        else if ( (LA42_1==CMP) && (synpred6_AmbientTalk())) {s = 14;}

                        else if ( (LA42_1==NUMBER) && (synpred6_AmbientTalk())) {s = 15;}

                        else if ( (LA42_1==FRACTION) && (synpred6_AmbientTalk())) {s = 16;}

                        else if ( (LA42_1==TEXT) && (synpred6_AmbientTalk())) {s = 17;}

                        else if ( (LA42_1==NAME) && (synpred6_AmbientTalk())) {s = 18;}

                        else if ( (LA42_1==SELF) && (synpred6_AmbientTalk())) {s = 19;}

                        else if ( (LA42_1==LBC) && (synpred6_AmbientTalk())) {s = 20;}

                        else if ( (LA42_1==LKU) && (synpred6_AmbientTalk())) {s = 21;}

                        else if ( (LA42_1==BQU) && (synpred6_AmbientTalk())) {s = 22;}

                        else if ( (LA42_1==HSH) && (synpred6_AmbientTalk())) {s = 23;}

                        else if ( (synpred5_AmbientTalk()) ) {s = 24;}

                        else if ( (true) ) {s = 25;}

                         
                        input.seek(index42_1);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA42_4 = input.LA(1);

                         
                        int index42_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA42_4==LPR) && (synpred6_AmbientTalk())) {s = 5;}

                        else if ( (LA42_4==LBR) && (synpred6_AmbientTalk())) {s = 6;}

                        else if ( (LA42_4==DOT) && (synpred6_AmbientTalk())) {s = 7;}

                        else if ( (LA42_4==ARW) && (synpred6_AmbientTalk())) {s = 8;}

                        else if ( (LA42_4==CAR) && (synpred6_AmbientTalk())) {s = 9;}

                        else if ( (LA42_4==USD) && (synpred6_AmbientTalk())) {s = 10;}

                        else if ( (LA42_4==POW) && (synpred6_AmbientTalk())) {s = 11;}

                        else if ( (LA42_4==MUL) && (synpred6_AmbientTalk())) {s = 12;}

                        else if ( (LA42_4==ADD) && (synpred6_AmbientTalk())) {s = 13;}

                        else if ( (LA42_4==CMP) && (synpred6_AmbientTalk())) {s = 14;}

                        else if ( (LA42_4==NUMBER) && (synpred6_AmbientTalk())) {s = 15;}

                        else if ( (LA42_4==FRACTION) && (synpred6_AmbientTalk())) {s = 16;}

                        else if ( (LA42_4==TEXT) && (synpred6_AmbientTalk())) {s = 17;}

                        else if ( (LA42_4==NAME) && (synpred6_AmbientTalk())) {s = 18;}

                        else if ( (LA42_4==SELF) && (synpred6_AmbientTalk())) {s = 19;}

                        else if ( (LA42_4==LBC) && (synpred6_AmbientTalk())) {s = 20;}

                        else if ( (LA42_4==LKU) && (synpred6_AmbientTalk())) {s = 21;}

                        else if ( (LA42_4==BQU) && (synpred6_AmbientTalk())) {s = 22;}

                        else if ( (LA42_4==HSH) && (synpred6_AmbientTalk())) {s = 23;}

                        else if ( (synpred5_AmbientTalk()) ) {s = 24;}

                        else if ( (true) ) {s = 25;}

                         
                        input.seek(index42_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 42, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA45_eotS =
        "\26\uffff";
    static final String DFA45_eofS =
        "\26\uffff";
    static final String DFA45_minS =
        "\1\76\1\0\24\uffff";
    static final String DFA45_maxS =
        "\1\136\1\0\24\uffff";
    static final String DFA45_acceptS =
        "\2\uffff\1\2\1\3\21\uffff\1\1";
    static final String DFA45_specialS =
        "\1\uffff\1\0\24\uffff}>";
    static final String[] DFA45_transitionS = {
            "\1\3\1\uffff\1\1\1\uffff\1\3\1\uffff\1\3\6\uffff\11\3\2\uffff"+
            "\1\3\1\2\2\3\2\uffff\3\3",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA45_eot = DFA.unpackEncodedString(DFA45_eotS);
    static final short[] DFA45_eof = DFA.unpackEncodedString(DFA45_eofS);
    static final char[] DFA45_min = DFA.unpackEncodedStringToUnsignedChars(DFA45_minS);
    static final char[] DFA45_max = DFA.unpackEncodedStringToUnsignedChars(DFA45_maxS);
    static final short[] DFA45_accept = DFA.unpackEncodedString(DFA45_acceptS);
    static final short[] DFA45_special = DFA.unpackEncodedString(DFA45_specialS);
    static final short[][] DFA45_transition;

    static {
        int numStates = DFA45_transitionS.length;
        DFA45_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA45_transition[i] = DFA.unpackEncodedString(DFA45_transitionS[i]);
        }
    }

    class DFA45 extends DFA {

        public DFA45(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 45;
            this.eot = DFA45_eot;
            this.eof = DFA45_eof;
            this.min = DFA45_min;
            this.max = DFA45_max;
            this.accept = DFA45_accept;
            this.special = DFA45_special;
            this.transition = DFA45_transition;
        }
        public String getDescription() {
            return "481:7: ( ( LBC )=> LBC stmts= statementlist RBC -> ^( AGQUOTEBEGIN[$lc,\"quote-begin\"] $stmts) | fa= field_assignment -> ^( AGQUOTE[$lc,\"quote\"] $fa) | qexp= operand -> ^( AGQUOTE[$lc,\"quote\"] $qexp) )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA45_1 = input.LA(1);

                         
                        int index45_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_AmbientTalk()) ) {s = 21;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index45_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 45, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA47_eotS =
        "\12\uffff";
    static final String DFA47_eofS =
        "\1\10\11\uffff";
    static final String DFA47_minS =
        "\1\71\7\0\2\uffff";
    static final String DFA47_maxS =
        "\1\136\7\0\2\uffff";
    static final String DFA47_acceptS =
        "\10\uffff\1\2\1\1";
    static final String DFA47_specialS =
        "\1\uffff\1\3\1\6\1\4\1\5\1\1\1\0\1\2\2\uffff}>";
    static final String[] DFA47_transitionS = {
            "\1\10\4\uffff\1\1\1\10\1\uffff\1\10\1\2\1\10\1\3\2\uffff\1\10"+
            "\1\uffff\2\10\1\uffff\4\10\4\uffff\1\10\5\uffff\1\4\1\10\1\5"+
            "\1\6\1\7",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA47_eot = DFA.unpackEncodedString(DFA47_eotS);
    static final short[] DFA47_eof = DFA.unpackEncodedString(DFA47_eofS);
    static final char[] DFA47_min = DFA.unpackEncodedStringToUnsignedChars(DFA47_minS);
    static final char[] DFA47_max = DFA.unpackEncodedStringToUnsignedChars(DFA47_maxS);
    static final short[] DFA47_accept = DFA.unpackEncodedString(DFA47_acceptS);
    static final short[] DFA47_special = DFA.unpackEncodedString(DFA47_specialS);
    static final short[][] DFA47_transition;

    static {
        int numStates = DFA47_transitionS.length;
        DFA47_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA47_transition[i] = DFA.unpackEncodedString(DFA47_transitionS[i]);
        }
    }

    class DFA47 extends DFA {

        public DFA47(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 47;
            this.eot = DFA47_eot;
            this.eof = DFA47_eof;
            this.min = DFA47_min;
            this.max = DFA47_max;
            this.accept = DFA47_accept;
            this.special = DFA47_special;
            this.transition = DFA47_transition;
        }
        public String getDescription() {
            return "507:1: curried_invocation[Tree functor] : ( ( LPR | LBR | DOT | SEL | ARW | USD | CAR )=>i= invoke_expression[functor] curried_invocation[i.tree] | -> ^() );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA47_6 = input.LA(1);

                         
                        int index47_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_AmbientTalk()) ) {s = 9;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index47_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA47_5 = input.LA(1);

                         
                        int index47_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_AmbientTalk()) ) {s = 9;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index47_5);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA47_7 = input.LA(1);

                         
                        int index47_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_AmbientTalk()) ) {s = 9;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index47_7);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA47_1 = input.LA(1);

                         
                        int index47_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_AmbientTalk()) ) {s = 9;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index47_1);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA47_3 = input.LA(1);

                         
                        int index47_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_AmbientTalk()) ) {s = 9;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index47_3);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA47_4 = input.LA(1);

                         
                        int index47_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_AmbientTalk()) ) {s = 9;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index47_4);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA47_2 = input.LA(1);

                         
                        int index47_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_AmbientTalk()) ) {s = 9;}

                        else if ( (true) ) {s = 8;}

                         
                        input.seek(index47_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 47, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA48_eotS =
        "\20\uffff";
    static final String DFA48_eofS =
        "\20\uffff";
    static final String DFA48_minS =
        "\1\76\2\uffff\1\113\4\uffff\6\0\2\uffff";
    static final String DFA48_maxS =
        "\1\136\2\uffff\1\123\4\uffff\6\0\2\uffff";
    static final String DFA48_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\5\1\6\1\7\1\10\6\uffff\1\3\1\4";
    static final String DFA48_specialS =
        "\1\1\7\uffff\1\3\1\4\1\0\1\6\1\2\1\5\2\uffff}>";
    static final String[] DFA48_transitionS = {
            "\1\1\3\uffff\1\2\1\uffff\1\3\25\uffff\1\4\1\uffff\1\5\1\6\1"+
            "\7",
            "",
            "",
            "\1\10\1\12\1\13\1\14\1\15\3\uffff\1\11",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA48_eot = DFA.unpackEncodedString(DFA48_eotS);
    static final short[] DFA48_eof = DFA.unpackEncodedString(DFA48_eofS);
    static final char[] DFA48_min = DFA.unpackEncodedStringToUnsignedChars(DFA48_minS);
    static final char[] DFA48_max = DFA.unpackEncodedStringToUnsignedChars(DFA48_maxS);
    static final short[] DFA48_accept = DFA.unpackEncodedString(DFA48_acceptS);
    static final short[] DFA48_special = DFA.unpackEncodedString(DFA48_specialS);
    static final short[][] DFA48_transition;

    static {
        int numStates = DFA48_transitionS.length;
        DFA48_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA48_transition[i] = DFA.unpackEncodedString(DFA48_transitionS[i]);
        }
    }

    class DFA48 extends DFA {

        public DFA48(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 48;
            this.eot = DFA48_eot;
            this.eof = DFA48_eof;
            this.min = DFA48_min;
            this.max = DFA48_max;
            this.accept = DFA48_accept;
            this.special = DFA48_special;
            this.transition = DFA48_transition;
        }
        public String getDescription() {
            return "515:1: invoke_expression[Tree functor] : (lc= LPR args= commalist RPR -> ^( AGAPPLICATION[$lc,\"application\"] $args) | tab= tabulation -> ^( AGTABULATION[\"tabulation\"] $tab) | ( DOT variable LPR )=>msg= message -> ^( AGSEND[\"send\"] $msg) | fsl= zero_arity_invocation -> ^( AGSEND[\"zero-send\"] $fsl) | sel= selection -> ^( AGSELECT[\"selection\"] $sel) | ( ARW variable LPR )=>amsg= async_message -> ^( AGSEND[\"async-send\"] $amsg) | ( CAR variable LPR )=>del= delegation_message -> ^( AGSEND[\"delegation-send\"] $del) | ( USD expression )=>usd= universal_message -> ^( AGSEND[\"universal-send\"] $usd) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA48_10 = input.LA(1);

                         
                        int index48_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_AmbientTalk()) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index48_10);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA48_0 = input.LA(1);

                         
                        int index48_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA48_0==LPR) ) {s = 1;}

                        else if ( (LA48_0==LBR) ) {s = 2;}

                        else if ( (LA48_0==DOT) ) {s = 3;}

                        else if ( (LA48_0==SEL) ) {s = 4;}

                        else if ( (LA48_0==ARW) && (synpred11_AmbientTalk())) {s = 5;}

                        else if ( (LA48_0==CAR) && (synpred12_AmbientTalk())) {s = 6;}

                        else if ( (LA48_0==USD) && (synpred13_AmbientTalk())) {s = 7;}

                         
                        input.seek(index48_0);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA48_12 = input.LA(1);

                         
                        int index48_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_AmbientTalk()) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index48_12);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA48_8 = input.LA(1);

                         
                        int index48_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_AmbientTalk()) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index48_8);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA48_9 = input.LA(1);

                         
                        int index48_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_AmbientTalk()) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index48_9);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA48_13 = input.LA(1);

                         
                        int index48_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_AmbientTalk()) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index48_13);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA48_11 = input.LA(1);

                         
                        int index48_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_AmbientTalk()) ) {s = 14;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index48_11);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 48, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_globalstatementlist_in_program477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EOF_in_globalstatementlist492 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statement_in_globalstatementlist510 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_SMC_in_globalstatementlist513 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_statement_in_globalstatementlist517 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_EOF_in_globalstatementlist522 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SMC_in_globalstatementlist526 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_globalstatementlist528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_documentation_in_statement562 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_definition_in_statement577 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_typedefinition_in_statement611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_importstatement_in_statement639 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignment_in_statement668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_statement702 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SINGLE_LINE_DOC_in_documentation752 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MULTI_LINE_DOC_in_documentation771 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEF_in_definition821 = new BitSet(new long[]{0x0000000000000000L,0x000200000088F804L});
    public static final BitSet FOLLOW_field_definition_in_definition831 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyworded_definition_in_definition871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_canonical_definition_in_definition907 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multivalue_definition_in_definition943 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_definition_in_definition978 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_external_definition_in_definition1018 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_or_assignment_in_field_definition1067 = new BitSet(new long[]{0x2000000000000002L});
    public static final BitSet FOLLOW_EQL_in_field_definition1070 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_expression_in_field_definition1074 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_113_in_keyworded_definition1118 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_or_assignment_in_canonical_definition1134 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_LPR_in_canonical_definition1142 = new BitSet(new long[]{0x8000000000000000L,0x0000000000A8F800L});
    public static final BitSet FOLLOW_parameterlist_in_canonical_definition1144 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_RPR_in_canonical_definition1146 = new BitSet(new long[]{0x0000000000000002L,0x0000000000200001L});
    public static final BitSet FOLLOW_annotation_in_canonical_definition1154 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_LBC_in_canonical_definition1163 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF937L});
    public static final BitSet FOLLOW_statementlist_in_canonical_definition1167 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RBC_in_canonical_definition1169 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBR_in_multivalue_definition1208 = new BitSet(new long[]{0x0000000000000000L,0x0000000000A8F808L});
    public static final BitSet FOLLOW_parameterlist_in_multivalue_definition1212 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RBR_in_multivalue_definition1214 = new BitSet(new long[]{0x2000000000000002L});
    public static final BitSet FOLLOW_EQL_in_multivalue_definition1217 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_expression_in_multivalue_definition1221 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_or_assignment_in_table_definition1257 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_LBR_in_table_definition1261 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_expression_in_table_definition1265 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RBR_in_table_definition1267 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_LBC_in_table_definition1275 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF937L});
    public static final BitSet FOLLOW_statementlist_in_table_definition1279 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RBC_in_table_definition1281 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_or_assignment_in_external_definition1323 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_DOT_in_external_definition1327 = new BitSet(new long[]{0x0000000000000000L,0x000000000088F800L});
    public static final BitSet FOLLOW_canonical_definition_in_external_definition1345 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_external_definition1374 = new BitSet(new long[]{0x2000000000000002L});
    public static final BitSet FOLLOW_EQL_in_external_definition1377 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_expression_in_external_definition1381 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TDEF_in_typedefinition1431 = new BitSet(new long[]{0x0000000000000000L,0x000000000008F800L});
    public static final BitSet FOLLOW_variable_in_typedefinition1435 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_SST_in_typedefinition1438 = new BitSet(new long[]{0x0000000000000000L,0x000000000008F800L});
    public static final BitSet FOLLOW_variable_in_typedefinition1442 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COM_in_typedefinition1445 = new BitSet(new long[]{0x0000000000000000L,0x000000000008F800L});
    public static final BitSet FOLLOW_variable_in_typedefinition1449 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_IMPORT_in_importstatement1493 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_expression_in_importstatement1497 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000600L});
    public static final BitSet FOLLOW_aliasbindings_in_importstatement1501 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_excludelist_in_importstatement1506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALIAS_in_aliasbindings1543 = new BitSet(new long[]{0x0000000000000000L,0x000000000008FA00L});
    public static final BitSet FOLLOW_ALIAS_in_aliasbindings1559 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_unquotation_in_aliasbindings1563 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_aliasbinding_in_aliasbindings1589 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COM_in_aliasbindings1592 = new BitSet(new long[]{0x0000000000000000L,0x000000000008FA00L});
    public static final BitSet FOLLOW_aliasbinding_in_aliasbindings1596 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_importname_in_aliasbinding1634 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_EQL_in_aliasbinding1638 = new BitSet(new long[]{0x0000000000000000L,0x000000000008FA00L});
    public static final BitSet FOLLOW_importname_in_aliasbinding1642 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_importname1674 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXCLUDE_in_excludelist1697 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_unquotation_in_excludelist1701 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXCLUDE_in_excludelist1709 = new BitSet(new long[]{0x0000000000000000L,0x000000000008FA00L});
    public static final BitSet FOLLOW_importname_in_excludelist1713 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COM_in_excludelist1716 = new BitSet(new long[]{0x0000000000000000L,0x000000000008FA00L});
    public static final BitSet FOLLOW_importname_in_excludelist1720 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_assign_variable_in_assignment1753 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assign_multi_in_assignment1766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assign_field_in_assignment1774 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_assign_variable1793 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_EQL_in_assign_variable1797 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_expression_in_assign_variable1801 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_assign_table1836 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_tabulation_in_assign_table1840 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_EQL_in_assign_table1844 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_expression_in_assign_table1848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBR_in_assign_multi1881 = new BitSet(new long[]{0x0000000000000000L,0x0000000000A8F800L});
    public static final BitSet FOLLOW_parameter_in_assign_multi1891 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000088L});
    public static final BitSet FOLLOW_COM_in_assign_multi1894 = new BitSet(new long[]{0x0000000000000000L,0x0000000000A8F800L});
    public static final BitSet FOLLOW_parameter_in_assign_multi1898 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000088L});
    public static final BitSet FOLLOW_RBR_in_assign_multi1906 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_EQL_in_assign_multi1914 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_commalist_in_assign_multi1918 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_assign_field1957 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_DOT_in_assign_field1959 = new BitSet(new long[]{0x0000000000000000L,0x000000000008F800L});
    public static final BitSet FOLLOW_variable_in_assign_field1963 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_EQL_in_assign_field1967 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_expression_in_assign_field1971 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparand_in_expression2012 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_CMP_in_expression2015 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_comparand_in_expression2018 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_term_in_comparand2043 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002000L});
    public static final BitSet FOLLOW_ADD_in_comparand2046 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_term_in_comparand2049 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002000L});
    public static final BitSet FOLLOW_factor_in_term2068 = new BitSet(new long[]{0x0000000000000002L,0x0000000000004000L});
    public static final BitSet FOLLOW_MUL_in_term2071 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_factor_in_term2074 = new BitSet(new long[]{0x0000000000000002L,0x0000000000004000L});
    public static final BitSet FOLLOW_invocation_in_factor2091 = new BitSet(new long[]{0x0000000000000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_POW_in_factor2094 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_invocation_in_factor2097 = new BitSet(new long[]{0x0000000000000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_operand_in_invocation2118 = new BitSet(new long[]{0x5C00000000000000L,0x00000000774FF935L});
    public static final BitSet FOLLOW_curried_invocation_in_invocation2120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_operand2140 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FRACTION_in_operand2168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_operand2190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_symbol_in_operand2222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_in_operand2228 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subexpression_in_operand2234 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_block_in_operand2240 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unary_in_operand2246 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lookup_in_operand2252 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quotation_in_operand2258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unquotation_in_operand2264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_message_in_operand2270 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_async_message_in_operand2276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delegation_message_in_operand2282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_universal_message_in_operand2288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_symbol2306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELF_in_symbol2323 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_symbol_in_variable2343 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_operator_in_variable2349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_variable_or_assignment2364 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_assignment_in_variable_or_assignment2370 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBR_in_table2384 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_commalist_in_table2392 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RBR_in_table2399 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argument_in_commalist2413 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COM_in_commalist2416 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_argument_in_commalist2420 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_LPR_in_subexpression2451 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_expression_in_subexpression2454 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_RPR_in_subexpression2457 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBC_in_block2475 = new BitSet(new long[]{0x5C00000000000000L,0x00000000735FF937L});
    public static final BitSet FOLLOW_PIP_in_block2485 = new BitSet(new long[]{0x0000000000000000L,0x0000000000B8F800L});
    public static final BitSet FOLLOW_parameterlist_in_block2489 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_PIP_in_block2491 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF937L});
    public static final BitSet FOLLOW_statementlist_in_block2504 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RBC_in_block2510 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameter_in_parameterlist2545 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COM_in_parameterlist2548 = new BitSet(new long[]{0x0000000000000000L,0x0000000000A8F800L});
    public static final BitSet FOLLOW_parameter_in_parameterlist2552 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COM_in_parameterlist2556 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_or_quotation_in_parameter2597 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_EQL_in_parameter2599 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_expression_in_parameter2603 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_or_quotation_in_parameter2628 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CAT_in_parameter2645 = new BitSet(new long[]{0x0000000000000000L,0x000000000088F800L});
    public static final BitSet FOLLOW_variable_or_quotation_in_parameter2649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_or_assignment_in_variable_or_quotation2684 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statement_in_statementlist2703 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_SMC_in_statementlist2706 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_statement_in_statementlist2710 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_SMC_in_statementlist2714 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_operator_in_unary2779 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_operator_in_unary2799 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_invocation_in_unary2803 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_operator_in_unary2833 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CMP_in_operator2848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ADD_in_operator2868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MUL_in_operator2887 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_POW_in_operator2906 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LKU_in_lookup2936 = new BitSet(new long[]{0x0000000000000000L,0x000000000088F800L});
    public static final BitSet FOLLOW_variable_or_keyword_or_assignment_in_lookup2940 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_or_field_assignment_in_variable_or_keyword_or_assignment2972 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_assignment_in_variable_or_field_assignment2989 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_variable_or_field_assignment2995 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASSNAME_in_field_assignment3009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BQU_in_quotation3040 = new BitSet(new long[]{0x5C00000000000000L,0x0000000073CFF935L});
    public static final BitSet FOLLOW_LBC_in_quotation3065 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF937L});
    public static final BitSet FOLLOW_statementlist_in_quotation3069 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RBC_in_quotation3071 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_assignment_in_quotation3110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_operand_in_quotation3151 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HSH_in_unquotation3193 = new BitSet(new long[]{0x5C00000000000000L,0x00000000736FF935L});
    public static final BitSet FOLLOW_operand_in_unquotation3203 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CAT_in_unquotation3227 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_operand_in_unquotation3231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_invoke_expression_in_curried_invocation3306 = new BitSet(new long[]{0x5C00000000000000L,0x00000000774FF935L});
    public static final BitSet FOLLOW_curried_invocation_in_curried_invocation3309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPR_in_invoke_expression3343 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_commalist_in_invoke_expression3347 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_RPR_in_invoke_expression3349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tabulation_in_invoke_expression3378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_message_in_invoke_expression3424 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_zero_arity_invocation_in_invoke_expression3453 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selection_in_invoke_expression3482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_async_message_in_invoke_expression3523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delegation_message_in_invoke_expression3565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_universal_message_in_invoke_expression3605 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBR_in_tabulation3639 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_expression_in_tabulation3643 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RBR_in_tabulation3645 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_zero_arity_invocation3678 = new BitSet(new long[]{0x0000000000000000L,0x000000000008F800L});
    public static final BitSet FOLLOW_variable_in_zero_arity_invocation3682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEL_in_selection3708 = new BitSet(new long[]{0x0000000000000000L,0x000000000088F800L});
    public static final BitSet FOLLOW_variable_or_keyword_or_assignment_in_selection3711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_canonical_in_application3740 = new BitSet(new long[]{0x0000000000000002L,0x0000000000200000L});
    public static final BitSet FOLLOW_annotation_in_application3744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_canonical3779 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_LPR_in_canonical3781 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_commalist_in_canonical3786 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_RPR_in_canonical3788 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CAT_in_annotation3810 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080800L});
    public static final BitSet FOLLOW_annotation_expression_in_annotation3813 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_symbol_in_annotation_expression3827 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singlekeyword_in_keywordlist3868 = new BitSet(new long[]{0x0000000000000002L,0x0000000008000000L});
    public static final BitSet FOLLOW_singlekeyword_in_keywordlist3873 = new BitSet(new long[]{0x0000000000000002L,0x0000000008000000L});
    public static final BitSet FOLLOW_KEY_in_singlekeyword3893 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_argument_in_singlekeyword3896 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_argument3908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_message3971 = new BitSet(new long[]{0x0000000000000000L,0x000000000008F800L});
    public static final BitSet FOLLOW_application_in_message4005 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_message4032 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARW_in_async_message4065 = new BitSet(new long[]{0x0000000000000000L,0x000000000008F800L});
    public static final BitSet FOLLOW_application_in_async_message4069 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CAR_in_delegation_message4100 = new BitSet(new long[]{0x0000000000000000L,0x000000000008F800L});
    public static final BitSet FOLLOW_application_in_delegation_message4104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_USD_in_universal_message4135 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_invocation_in_universal_message4139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_synpred1_AmbientTalk1336 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_LPR_in_synpred1_AmbientTalk1338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HSH_in_synpred2_AmbientTalk1552 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXCLUDE_in_synpred3_AmbientTalk1690 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_HSH_in_synpred3_AmbientTalk1692 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_or_quotation_in_synpred4_AmbientTalk2588 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_EQL_in_synpred4_AmbientTalk2590 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_operator_in_synpred5_AmbientTalk2752 = new BitSet(new long[]{0x4000000000000000L,0x0000000050000014L});
    public static final BitSet FOLLOW_set_in_synpred5_AmbientTalk2754 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_operator_in_synpred6_AmbientTalk2788 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_invocation_in_synpred6_AmbientTalk2790 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBC_in_synpred8_AmbientTalk3060 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred9_AmbientTalk3268 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_synpred10_AmbientTalk3412 = new BitSet(new long[]{0x0000000000000000L,0x000000000008F800L});
    public static final BitSet FOLLOW_variable_in_synpred10_AmbientTalk3414 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_LPR_in_synpred10_AmbientTalk3416 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARW_in_synpred11_AmbientTalk3511 = new BitSet(new long[]{0x0000000000000000L,0x000000000008F800L});
    public static final BitSet FOLLOW_variable_in_synpred11_AmbientTalk3513 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_LPR_in_synpred11_AmbientTalk3515 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CAR_in_synpred12_AmbientTalk3553 = new BitSet(new long[]{0x0000000000000000L,0x000000000008F800L});
    public static final BitSet FOLLOW_variable_in_synpred12_AmbientTalk3555 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_LPR_in_synpred12_AmbientTalk3557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_USD_in_synpred13_AmbientTalk3595 = new BitSet(new long[]{0x5C00000000000000L,0x00000000734FF935L});
    public static final BitSet FOLLOW_expression_in_synpred13_AmbientTalk3597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_synpred16_AmbientTalk3995 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_LPR_in_synpred16_AmbientTalk3997 = new BitSet(new long[]{0x0000000000000002L});

}

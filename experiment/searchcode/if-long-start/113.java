// $ANTLR 3.3 Nov 30, 2010 12:45:30 C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g 2011-06-09 13:59:19

	package su.nsk.iae.reflexdt.core.dom;
	import su.nsk.iae.reflexdt.core.dom.*;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

public class ReflexParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACKET", "RBRACKET", "SEMI", "COMMA", "DOT", "ASSIGN", "LOGICAL_NOT", "LOGICAL_AND", "LOGICAL_OR", "EQ", "NOT_EQ", "QUES", "COLON", "INC", "DEC", "PLUS", "MINUS", "STAR", "DIV", "MOD", "NOT", "AND", "OR", "MOD2", "GT", "LT", "PLUSE_EQ", "MINUS_EQ", "STARE_EQ", "SLASHE_EQ", "AMPE_EQ", "BARE_EQ", "CARETE_EQ", "PERSENT_EQ", "LSHIFT_EQ", "RSHIFT_EQ", "PROGRAM", "REGISTER_DECL", "FORMAL_ARGS", "VAR_DECL", "VAR_REF", "BLOCK", "SITUATION", "TACT_DECL", "CONST_DECL", "UNARY_PLUS", "UNARY_MINUS", "PRE_INC", "PRE_DEC", "POST_INC", "POST_DEC", "FUNCTION_CALL", "PARENTESIZED_EXPR", "PORT_LINK_LIST", "PORT_LINK", "FUNCTION_DECL", "PROCESS_DECL", "FOR", "STATE_DECL", "IF", "SWITCH", "LOOP", "CONTINUE", "BREAK", "TIMEOUT", "STOP", "ERROR", "START", "CONT", "PROCESS_NAME", "STATE_NAME", "IN", "ITERATION_STATEMENT", "EXPR", "LOCAL_MODIFIER", "FOR_PROC_LIST_MODIFIER", "FOR_ALL_MODIFIER", "VOID", "INT", "SHORT", "FLOAT", "DOUBLE", "LOG", "LONG", "INTPUT", "OUTPUT", "RAM", "ARGS", "INTEGER_LITERAL", "IDENTIFIER", "CHAR_LITERAL", "STRING_LITERAL", "FLOAT_LITERAL", "PROGR", "TACT", "CONST", "INPUT", "PROC", "LOCAL", "ALL", "FROM", "STATE", "ELSE", "NEXT", "CASE", "DEFAULT", "ACTIVE", "PASSIVE", "SIGNED", "UNSIGNED", "ENUM", "FUNCTION", "CMARK", "UNKNOWNMARK", "Letter", "HexDigit", "LongSuffix", "Exponent", "WS", "EscapeSequence", "OctalEscape", "COMMENT", "'8'", "'16'", "'<='", "'>='", "'<<'", "'>>'"
    };
    public static final int EOF=-1;
    public static final int T__130=130;
    public static final int T__131=131;
    public static final int T__132=132;
    public static final int T__133=133;
    public static final int T__134=134;
    public static final int T__135=135;
    public static final int LPAREN=4;
    public static final int RPAREN=5;
    public static final int LBRACE=6;
    public static final int RBRACE=7;
    public static final int LBRACKET=8;
    public static final int RBRACKET=9;
    public static final int SEMI=10;
    public static final int COMMA=11;
    public static final int DOT=12;
    public static final int ASSIGN=13;
    public static final int LOGICAL_NOT=14;
    public static final int LOGICAL_AND=15;
    public static final int LOGICAL_OR=16;
    public static final int EQ=17;
    public static final int NOT_EQ=18;
    public static final int QUES=19;
    public static final int COLON=20;
    public static final int INC=21;
    public static final int DEC=22;
    public static final int PLUS=23;
    public static final int MINUS=24;
    public static final int STAR=25;
    public static final int DIV=26;
    public static final int MOD=27;
    public static final int NOT=28;
    public static final int AND=29;
    public static final int OR=30;
    public static final int MOD2=31;
    public static final int GT=32;
    public static final int LT=33;
    public static final int PLUSE_EQ=34;
    public static final int MINUS_EQ=35;
    public static final int STARE_EQ=36;
    public static final int SLASHE_EQ=37;
    public static final int AMPE_EQ=38;
    public static final int BARE_EQ=39;
    public static final int CARETE_EQ=40;
    public static final int PERSENT_EQ=41;
    public static final int LSHIFT_EQ=42;
    public static final int RSHIFT_EQ=43;
    public static final int PROGRAM=44;
    public static final int REGISTER_DECL=45;
    public static final int FORMAL_ARGS=46;
    public static final int VAR_DECL=47;
    public static final int VAR_REF=48;
    public static final int BLOCK=49;
    public static final int SITUATION=50;
    public static final int TACT_DECL=51;
    public static final int CONST_DECL=52;
    public static final int UNARY_PLUS=53;
    public static final int UNARY_MINUS=54;
    public static final int PRE_INC=55;
    public static final int PRE_DEC=56;
    public static final int POST_INC=57;
    public static final int POST_DEC=58;
    public static final int FUNCTION_CALL=59;
    public static final int PARENTESIZED_EXPR=60;
    public static final int PORT_LINK_LIST=61;
    public static final int PORT_LINK=62;
    public static final int FUNCTION_DECL=63;
    public static final int PROCESS_DECL=64;
    public static final int FOR=65;
    public static final int STATE_DECL=66;
    public static final int IF=67;
    public static final int SWITCH=68;
    public static final int LOOP=69;
    public static final int CONTINUE=70;
    public static final int BREAK=71;
    public static final int TIMEOUT=72;
    public static final int STOP=73;
    public static final int ERROR=74;
    public static final int START=75;
    public static final int CONT=76;
    public static final int PROCESS_NAME=77;
    public static final int STATE_NAME=78;
    public static final int IN=79;
    public static final int ITERATION_STATEMENT=80;
    public static final int EXPR=81;
    public static final int LOCAL_MODIFIER=82;
    public static final int FOR_PROC_LIST_MODIFIER=83;
    public static final int FOR_ALL_MODIFIER=84;
    public static final int VOID=85;
    public static final int INT=86;
    public static final int SHORT=87;
    public static final int FLOAT=88;
    public static final int DOUBLE=89;
    public static final int LOG=90;
    public static final int LONG=91;
    public static final int INTPUT=92;
    public static final int OUTPUT=93;
    public static final int RAM=94;
    public static final int ARGS=95;
    public static final int INTEGER_LITERAL=96;
    public static final int IDENTIFIER=97;
    public static final int CHAR_LITERAL=98;
    public static final int STRING_LITERAL=99;
    public static final int FLOAT_LITERAL=100;
    public static final int PROGR=101;
    public static final int TACT=102;
    public static final int CONST=103;
    public static final int INPUT=104;
    public static final int PROC=105;
    public static final int LOCAL=106;
    public static final int ALL=107;
    public static final int FROM=108;
    public static final int STATE=109;
    public static final int ELSE=110;
    public static final int NEXT=111;
    public static final int CASE=112;
    public static final int DEFAULT=113;
    public static final int ACTIVE=114;
    public static final int PASSIVE=115;
    public static final int SIGNED=116;
    public static final int UNSIGNED=117;
    public static final int ENUM=118;
    public static final int FUNCTION=119;
    public static final int CMARK=120;
    public static final int UNKNOWNMARK=121;
    public static final int Letter=122;
    public static final int HexDigit=123;
    public static final int LongSuffix=124;
    public static final int Exponent=125;
    public static final int WS=126;
    public static final int EscapeSequence=127;
    public static final int OctalEscape=128;
    public static final int COMMENT=129;

    // delegates
    // delegators


        public ReflexParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public ReflexParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            this.state.ruleMemo = new HashMap[194+1];
             
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return ReflexParser.tokenNames; }
    public String getGrammarFileName() { return "C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g"; }


    	boolean engSyntax = false;
    	
    	boolean checkKeywordLang(String englishKeyword, String russianKeyword)
    	{
    		return ((engSyntax && input.LT(1).getText().equals(englishKeyword))
    			|| (!engSyntax && input.LT(1).getText().equals(russianKeyword)));
    	}

    	void recoveryError(CommonTree node)
    	{
    		System.out.println(node!=null?node.getText():"");
    	}

    	protected Object recoverFromMismatchedToken(IntStream arg0, int arg1,
        			BitSet arg2) throws RecognitionException
        	{
        		// TODO Auto-generated method stub
        		//return null;//super.recoverFromMismatchedToken(arg0, arg1, arg2);
        		throw new RecognitionException();
        	}
    	public void recover(IntStream input, RecognitionException re)
        	{
        		// TODO Auto-generated method stub
        		//super.recover(input, re);
        	}


    public static class program_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "program"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:152:1: program : progrKey identifier LBRACE tactDeclaration ( constDeclaration )* ( functionDeclaration )* ( registerDeclaration )* ( processDeclaration )+ RBRACE -> ^( PROGRAM[$start.getStartIndex(),$text.length(),\"PROGRAM\"] identifier tactDeclaration ( constDeclaration )* ( registerDeclaration )* ( processDeclaration )+ ) ;
    public final ReflexParser.program_return program() throws RecognitionException {
        ReflexParser.program_return retval = new ReflexParser.program_return();
        retval.start = input.LT(1);
        int program_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken LBRACE3=null;
        CommonToken RBRACE9=null;
        ReflexParser.progrKey_return progrKey1 = null;

        ReflexParser.identifier_return identifier2 = null;

        ReflexParser.tactDeclaration_return tactDeclaration4 = null;

        ReflexParser.constDeclaration_return constDeclaration5 = null;

        ReflexParser.functionDeclaration_return functionDeclaration6 = null;

        ReflexParser.registerDeclaration_return registerDeclaration7 = null;

        ReflexParser.processDeclaration_return processDeclaration8 = null;


        ASTNode LBRACE3_tree=null;
        ASTNode RBRACE9_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_progrKey=new RewriteRuleSubtreeStream(adaptor,"rule progrKey");
        RewriteRuleSubtreeStream stream_functionDeclaration=new RewriteRuleSubtreeStream(adaptor,"rule functionDeclaration");
        RewriteRuleSubtreeStream stream_registerDeclaration=new RewriteRuleSubtreeStream(adaptor,"rule registerDeclaration");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        RewriteRuleSubtreeStream stream_processDeclaration=new RewriteRuleSubtreeStream(adaptor,"rule processDeclaration");
        RewriteRuleSubtreeStream stream_tactDeclaration=new RewriteRuleSubtreeStream(adaptor,"rule tactDeclaration");
        RewriteRuleSubtreeStream stream_constDeclaration=new RewriteRuleSubtreeStream(adaptor,"rule constDeclaration");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 1) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:153:2: ( progrKey identifier LBRACE tactDeclaration ( constDeclaration )* ( functionDeclaration )* ( registerDeclaration )* ( processDeclaration )+ RBRACE -> ^( PROGRAM[$start.getStartIndex(),$text.length(),\"PROGRAM\"] identifier tactDeclaration ( constDeclaration )* ( registerDeclaration )* ( processDeclaration )+ ) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:154:2: progrKey identifier LBRACE tactDeclaration ( constDeclaration )* ( functionDeclaration )* ( registerDeclaration )* ( processDeclaration )+ RBRACE
            {
            if ( state.backtracking==0 ) {

              		if (((CommonToken)retval.start).getText() == null)
              			return null;
              		if (((CommonToken)retval.start).getText().equals("Progr")) engSyntax=true;
              	
            }
            pushFollow(FOLLOW_progrKey_in_program585);
            progrKey1=progrKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_progrKey.add(progrKey1.getTree());
            pushFollow(FOLLOW_identifier_in_program587);
            identifier2=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier2.getTree());
            LBRACE3=(CommonToken)match(input,LBRACE,FOLLOW_LBRACE_in_program591); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE3);

            pushFollow(FOLLOW_tactDeclaration_in_program593);
            tactDeclaration4=tactDeclaration();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tactDeclaration.add(tactDeclaration4.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:160:25: ( constDeclaration )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==CONST||LA1_0==ENUM) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:0:0: constDeclaration
            	    {
            	    pushFollow(FOLLOW_constDeclaration_in_program595);
            	    constDeclaration5=constDeclaration();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_constDeclaration.add(constDeclaration5.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:160:43: ( functionDeclaration )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==FUNCTION) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:0:0: functionDeclaration
            	    {
            	    pushFollow(FOLLOW_functionDeclaration_in_program598);
            	    functionDeclaration6=functionDeclaration();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_functionDeclaration.add(functionDeclaration6.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:160:64: ( registerDeclaration )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>=OUTPUT && LA3_0<=RAM)||LA3_0==INPUT) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:0:0: registerDeclaration
            	    {
            	    pushFollow(FOLLOW_registerDeclaration_in_program601);
            	    registerDeclaration7=registerDeclaration();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_registerDeclaration.add(registerDeclaration7.getTree());

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:160:85: ( processDeclaration )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==PROC) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:0:0: processDeclaration
            	    {
            	    pushFollow(FOLLOW_processDeclaration_in_program604);
            	    processDeclaration8=processDeclaration();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_processDeclaration.add(processDeclaration8.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);

            RBRACE9=(CommonToken)match(input,RBRACE,FOLLOW_RBRACE_in_program607); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE9);



            // AST REWRITE
            // elements: identifier, tactDeclaration, registerDeclaration, constDeclaration, processDeclaration
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 161:2: -> ^( PROGRAM[$start.getStartIndex(),$text.length(),\"PROGRAM\"] identifier tactDeclaration ( constDeclaration )* ( registerDeclaration )* ( processDeclaration )+ )
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:161:5: ^( PROGRAM[$start.getStartIndex(),$text.length(),\"PROGRAM\"] identifier tactDeclaration ( constDeclaration )* ( registerDeclaration )* ( processDeclaration )+ )
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new Program(PROGRAM, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "PROGRAM"), root_1);

                adaptor.addChild(root_1, stream_identifier.nextTree());
                adaptor.addChild(root_1, stream_tactDeclaration.nextTree());
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:164:3: ( constDeclaration )*
                while ( stream_constDeclaration.hasNext() ) {
                    adaptor.addChild(root_1, stream_constDeclaration.nextTree());

                }
                stream_constDeclaration.reset();
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:165:3: ( registerDeclaration )*
                while ( stream_registerDeclaration.hasNext() ) {
                    adaptor.addChild(root_1, stream_registerDeclaration.nextTree());

                }
                stream_registerDeclaration.reset();
                if ( !(stream_processDeclaration.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_processDeclaration.hasNext() ) {
                    adaptor.addChild(root_1, stream_processDeclaration.nextTree());

                }
                stream_processDeclaration.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 1, program_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "program"

    public static class tactDeclaration_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tactDeclaration"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:170:1: tactDeclaration : tactKey INTEGER_LITERAL ';' -> ^( TACT_DECL[$start.getStartIndex(),$text.length(),\"TACT_DECL\"] INTEGER_LITERAL[$INTEGER_LITERAL.getStartIndex(),$INTEGER_LITERAL.text.length(),$INTEGER_LITERAL] ) ;
    public final ReflexParser.tactDeclaration_return tactDeclaration() throws RecognitionException {
        ReflexParser.tactDeclaration_return retval = new ReflexParser.tactDeclaration_return();
        retval.start = input.LT(1);
        int tactDeclaration_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken INTEGER_LITERAL11=null;
        CommonToken char_literal12=null;
        ReflexParser.tactKey_return tactKey10 = null;


        ASTNode INTEGER_LITERAL11_tree=null;
        ASTNode char_literal12_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleTokenStream stream_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token INTEGER_LITERAL");
        RewriteRuleSubtreeStream stream_tactKey=new RewriteRuleSubtreeStream(adaptor,"rule tactKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 2) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:171:2: ( tactKey INTEGER_LITERAL ';' -> ^( TACT_DECL[$start.getStartIndex(),$text.length(),\"TACT_DECL\"] INTEGER_LITERAL[$INTEGER_LITERAL.getStartIndex(),$INTEGER_LITERAL.text.length(),$INTEGER_LITERAL] ) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:171:4: tactKey INTEGER_LITERAL ';'
            {
            pushFollow(FOLLOW_tactKey_in_tactDeclaration659);
            tactKey10=tactKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tactKey.add(tactKey10.getTree());
            INTEGER_LITERAL11=(CommonToken)match(input,INTEGER_LITERAL,FOLLOW_INTEGER_LITERAL_in_tactDeclaration661); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_INTEGER_LITERAL.add(INTEGER_LITERAL11);

            char_literal12=(CommonToken)match(input,SEMI,FOLLOW_SEMI_in_tactDeclaration665); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SEMI.add(char_literal12);



            // AST REWRITE
            // elements: INTEGER_LITERAL
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 173:2: -> ^( TACT_DECL[$start.getStartIndex(),$text.length(),\"TACT_DECL\"] INTEGER_LITERAL[$INTEGER_LITERAL.getStartIndex(),$INTEGER_LITERAL.text.length(),$INTEGER_LITERAL] )
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:173:5: ^( TACT_DECL[$start.getStartIndex(),$text.length(),\"TACT_DECL\"] INTEGER_LITERAL[$INTEGER_LITERAL.getStartIndex(),$INTEGER_LITERAL.text.length(),$INTEGER_LITERAL] )
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new TactDeclaration(TACT_DECL, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "TACT_DECL"), root_1);

                adaptor.addChild(root_1, new IntegerLiteral(INTEGER_LITERAL, INTEGER_LITERAL11.getStartIndex(), (INTEGER_LITERAL11!=null?INTEGER_LITERAL11.getText():null).length(), INTEGER_LITERAL11));

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 2, tactDeclaration_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "tactDeclaration"

    public static class constDeclaration_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constDeclaration"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:178:1: constDeclaration : ( ( constKey id= identifier exp= logical_or_expression ) -> ^( CONST_DECL[$start.getStartIndex(),$text.length(),\"CONST_DECL\"] $id $exp) | enumDeclaration ) ';' ;
    public final ReflexParser.constDeclaration_return constDeclaration() throws RecognitionException {
        ReflexParser.constDeclaration_return retval = new ReflexParser.constDeclaration_return();
        retval.start = input.LT(1);
        int constDeclaration_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal15=null;
        ReflexParser.identifier_return id = null;

        ReflexParser.logical_or_expression_return exp = null;

        ReflexParser.constKey_return constKey13 = null;

        ReflexParser.enumDeclaration_return enumDeclaration14 = null;


        ASTNode char_literal15_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_constKey=new RewriteRuleSubtreeStream(adaptor,"rule constKey");
        RewriteRuleSubtreeStream stream_logical_or_expression=new RewriteRuleSubtreeStream(adaptor,"rule logical_or_expression");
        RewriteRuleSubtreeStream stream_enumDeclaration=new RewriteRuleSubtreeStream(adaptor,"rule enumDeclaration");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 3) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:179:2: ( ( ( constKey id= identifier exp= logical_or_expression ) -> ^( CONST_DECL[$start.getStartIndex(),$text.length(),\"CONST_DECL\"] $id $exp) | enumDeclaration ) ';' )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:180:2: ( ( constKey id= identifier exp= logical_or_expression ) -> ^( CONST_DECL[$start.getStartIndex(),$text.length(),\"CONST_DECL\"] $id $exp) | enumDeclaration ) ';'
            {
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:180:2: ( ( constKey id= identifier exp= logical_or_expression ) -> ^( CONST_DECL[$start.getStartIndex(),$text.length(),\"CONST_DECL\"] $id $exp) | enumDeclaration )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==CONST) ) {
                alt5=1;
            }
            else if ( (LA5_0==ENUM) ) {
                alt5=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:180:4: ( constKey id= identifier exp= logical_or_expression )
                    {
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:180:4: ( constKey id= identifier exp= logical_or_expression )
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:180:5: constKey id= identifier exp= logical_or_expression
                    {
                    pushFollow(FOLLOW_constKey_in_constDeclaration707);
                    constKey13=constKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_constKey.add(constKey13.getTree());
                    pushFollow(FOLLOW_identifier_in_constDeclaration711);
                    id=identifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_identifier.add(id.getTree());
                    pushFollow(FOLLOW_logical_or_expression_in_constDeclaration715);
                    exp=logical_or_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_logical_or_expression.add(exp.getTree());

                    }



                    // AST REWRITE
                    // elements: exp, id
                    // token labels: 
                    // rule labels: id, exp, retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id",id!=null?id.tree:null);
                    RewriteRuleSubtreeStream stream_exp=new RewriteRuleSubtreeStream(adaptor,"rule exp",exp!=null?exp.tree:null);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 181:2: -> ^( CONST_DECL[$start.getStartIndex(),$text.length(),\"CONST_DECL\"] $id $exp)
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:181:5: ^( CONST_DECL[$start.getStartIndex(),$text.length(),\"CONST_DECL\"] $id $exp)
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot(new ConstDeclaration(CONST_DECL, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "CONST_DECL"), root_1);

                        adaptor.addChild(root_1, stream_id.nextTree());
                        adaptor.addChild(root_1, stream_exp.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:182:4: enumDeclaration
                    {
                    pushFollow(FOLLOW_enumDeclaration_in_constDeclaration739);
                    enumDeclaration14=enumDeclaration();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_enumDeclaration.add(enumDeclaration14.getTree());

                    }
                    break;

            }

            char_literal15=(CommonToken)match(input,SEMI,FOLLOW_SEMI_in_constDeclaration745); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SEMI.add(char_literal15);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 3, constDeclaration_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "constDeclaration"

    public static class functionDeclaration_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "functionDeclaration"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:187:1: functionDeclaration : functionKey type= typeSpecifier id= identifier sig= formalArguments ';' -> ^( FUNCTION_DECL[$start.getStartIndex(),$text.length(),\"FUNCTION_DECL\"] $type $id $sig) ;
    public final ReflexParser.functionDeclaration_return functionDeclaration() throws RecognitionException {
        ReflexParser.functionDeclaration_return retval = new ReflexParser.functionDeclaration_return();
        retval.start = input.LT(1);
        int functionDeclaration_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal17=null;
        ReflexParser.typeSpecifier_return type = null;

        ReflexParser.identifier_return id = null;

        ReflexParser.formalArguments_return sig = null;

        ReflexParser.functionKey_return functionKey16 = null;


        ASTNode char_literal17_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_formalArguments=new RewriteRuleSubtreeStream(adaptor,"rule formalArguments");
        RewriteRuleSubtreeStream stream_functionKey=new RewriteRuleSubtreeStream(adaptor,"rule functionKey");
        RewriteRuleSubtreeStream stream_typeSpecifier=new RewriteRuleSubtreeStream(adaptor,"rule typeSpecifier");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 4) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:188:2: ( functionKey type= typeSpecifier id= identifier sig= formalArguments ';' -> ^( FUNCTION_DECL[$start.getStartIndex(),$text.length(),\"FUNCTION_DECL\"] $type $id $sig) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:188:4: functionKey type= typeSpecifier id= identifier sig= formalArguments ';'
            {
            pushFollow(FOLLOW_functionKey_in_functionDeclaration757);
            functionKey16=functionKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_functionKey.add(functionKey16.getTree());
            pushFollow(FOLLOW_typeSpecifier_in_functionDeclaration761);
            type=typeSpecifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_typeSpecifier.add(type.getTree());
            pushFollow(FOLLOW_identifier_in_functionDeclaration765);
            id=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(id.getTree());
            pushFollow(FOLLOW_formalArguments_in_functionDeclaration769);
            sig=formalArguments();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_formalArguments.add(sig.getTree());
            char_literal17=(CommonToken)match(input,SEMI,FOLLOW_SEMI_in_functionDeclaration772); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SEMI.add(char_literal17);



            // AST REWRITE
            // elements: type, sig, id
            // token labels: 
            // rule labels: id, retval, type, sig
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id",id!=null?id.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type",type!=null?type.tree:null);
            RewriteRuleSubtreeStream stream_sig=new RewriteRuleSubtreeStream(adaptor,"rule sig",sig!=null?sig.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 190:2: -> ^( FUNCTION_DECL[$start.getStartIndex(),$text.length(),\"FUNCTION_DECL\"] $type $id $sig)
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:190:5: ^( FUNCTION_DECL[$start.getStartIndex(),$text.length(),\"FUNCTION_DECL\"] $type $id $sig)
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new FunctionDeclaration(FUNCTION_DECL, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "FUNCTION_DECL"), root_1);

                adaptor.addChild(root_1, stream_type.nextTree());
                adaptor.addChild(root_1, stream_id.nextTree());
                adaptor.addChild(root_1, stream_sig.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 4, functionDeclaration_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "functionDeclaration"

    public static class formalArguments_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "formalArguments"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:194:1: formalArguments : '(' args+= typeSpecifier ( ',' args+= typeSpecifier )* ')' -> ^( FORMAL_ARGS[$start.getStartIndex(),$text.length(),\"FORMAL_ARGS\"] ( $args)+ ) ;
    public final ReflexParser.formalArguments_return formalArguments() throws RecognitionException {
        ReflexParser.formalArguments_return retval = new ReflexParser.formalArguments_return();
        retval.start = input.LT(1);
        int formalArguments_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal18=null;
        CommonToken char_literal19=null;
        CommonToken char_literal20=null;
        List list_args=null;
        RuleReturnScope args = null;
        ASTNode char_literal18_tree=null;
        ASTNode char_literal19_tree=null;
        ASTNode char_literal20_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_typeSpecifier=new RewriteRuleSubtreeStream(adaptor,"rule typeSpecifier");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 5) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:195:2: ( '(' args+= typeSpecifier ( ',' args+= typeSpecifier )* ')' -> ^( FORMAL_ARGS[$start.getStartIndex(),$text.length(),\"FORMAL_ARGS\"] ( $args)+ ) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:195:4: '(' args+= typeSpecifier ( ',' args+= typeSpecifier )* ')'
            {
            char_literal18=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_formalArguments808); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(char_literal18);

            pushFollow(FOLLOW_typeSpecifier_in_formalArguments812);
            args=typeSpecifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_typeSpecifier.add(args.getTree());
            if (list_args==null) list_args=new ArrayList();
            list_args.add(args.getTree());

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:195:28: ( ',' args+= typeSpecifier )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==COMMA) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:195:29: ',' args+= typeSpecifier
            	    {
            	    char_literal19=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_formalArguments815); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(char_literal19);

            	    pushFollow(FOLLOW_typeSpecifier_in_formalArguments819);
            	    args=typeSpecifier();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_typeSpecifier.add(args.getTree());
            	    if (list_args==null) list_args=new ArrayList();
            	    list_args.add(args.getTree());


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            char_literal20=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_formalArguments823); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(char_literal20);



            // AST REWRITE
            // elements: args
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: args
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_args=new RewriteRuleSubtreeStream(adaptor,"token args",list_args);
            root_0 = (ASTNode)adaptor.nil();
            // 196:2: -> ^( FORMAL_ARGS[$start.getStartIndex(),$text.length(),\"FORMAL_ARGS\"] ( $args)+ )
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:196:5: ^( FORMAL_ARGS[$start.getStartIndex(),$text.length(),\"FORMAL_ARGS\"] ( $args)+ )
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new FunctionArguments(FORMAL_ARGS, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "FORMAL_ARGS"), root_1);

                if ( !(stream_args.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_args.hasNext() ) {
                    adaptor.addChild(root_1, stream_args.nextTree());

                }
                stream_args.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 5, formalArguments_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "formalArguments"

    public static class typeSpecifier_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "typeSpecifier"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:201:1: typeSpecifier : ( voidKey -> VOID[$start.getStartIndex(),$text.length(),\"VOID\"] | shortKey -> SHORT[$start.getStartIndex(),$text.length(),\"SHORT\"] | intKey -> INT[$start.getStartIndex(),$text.length(),\"INT\"] | longKey -> LONG[$start.getStartIndex(),$text.length(),\"LONG\"] | floatKey -> FLOAT[$start.getStartIndex(),$text.length(),\"FLOAT\"] | doubleKey -> DOUBLE[$start.getStartIndex(),$text.length(),\"DOUBLE\"] | logKey -> LOG[$start.getStartIndex(),$text.length(),\"LOG\"] );
    public final ReflexParser.typeSpecifier_return typeSpecifier() throws RecognitionException {
        ReflexParser.typeSpecifier_return retval = new ReflexParser.typeSpecifier_return();
        retval.start = input.LT(1);
        int typeSpecifier_StartIndex = input.index();
        ASTNode root_0 = null;

        ReflexParser.voidKey_return voidKey21 = null;

        ReflexParser.shortKey_return shortKey22 = null;

        ReflexParser.intKey_return intKey23 = null;

        ReflexParser.longKey_return longKey24 = null;

        ReflexParser.floatKey_return floatKey25 = null;

        ReflexParser.doubleKey_return doubleKey26 = null;

        ReflexParser.logKey_return logKey27 = null;


        RewriteRuleSubtreeStream stream_voidKey=new RewriteRuleSubtreeStream(adaptor,"rule voidKey");
        RewriteRuleSubtreeStream stream_doubleKey=new RewriteRuleSubtreeStream(adaptor,"rule doubleKey");
        RewriteRuleSubtreeStream stream_intKey=new RewriteRuleSubtreeStream(adaptor,"rule intKey");
        RewriteRuleSubtreeStream stream_floatKey=new RewriteRuleSubtreeStream(adaptor,"rule floatKey");
        RewriteRuleSubtreeStream stream_shortKey=new RewriteRuleSubtreeStream(adaptor,"rule shortKey");
        RewriteRuleSubtreeStream stream_logKey=new RewriteRuleSubtreeStream(adaptor,"rule logKey");
        RewriteRuleSubtreeStream stream_longKey=new RewriteRuleSubtreeStream(adaptor,"rule longKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 6) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:202:2: ( voidKey -> VOID[$start.getStartIndex(),$text.length(),\"VOID\"] | shortKey -> SHORT[$start.getStartIndex(),$text.length(),\"SHORT\"] | intKey -> INT[$start.getStartIndex(),$text.length(),\"INT\"] | longKey -> LONG[$start.getStartIndex(),$text.length(),\"LONG\"] | floatKey -> FLOAT[$start.getStartIndex(),$text.length(),\"FLOAT\"] | doubleKey -> DOUBLE[$start.getStartIndex(),$text.length(),\"DOUBLE\"] | logKey -> LOG[$start.getStartIndex(),$text.length(),\"LOG\"] )
            int alt7=7;
            switch ( input.LA(1) ) {
            case VOID:
                {
                alt7=1;
                }
                break;
            case SHORT:
                {
                alt7=2;
                }
                break;
            case INT:
                {
                alt7=3;
                }
                break;
            case LONG:
                {
                alt7=4;
                }
                break;
            case FLOAT:
                {
                alt7=5;
                }
                break;
            case DOUBLE:
                {
                alt7=6;
                }
                break;
            case LOG:
                {
                alt7=7;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }

            switch (alt7) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:202:4: voidKey
                    {
                    pushFollow(FOLLOW_voidKey_in_typeSpecifier855);
                    voidKey21=voidKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_voidKey.add(voidKey21.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 202:12: -> VOID[$start.getStartIndex(),$text.length(),\"VOID\"]
                    {
                        adaptor.addChild(root_0, new Type(VOID, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "VOID"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:203:4: shortKey
                    {
                    pushFollow(FOLLOW_shortKey_in_typeSpecifier868);
                    shortKey22=shortKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_shortKey.add(shortKey22.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 203:13: -> SHORT[$start.getStartIndex(),$text.length(),\"SHORT\"]
                    {
                        adaptor.addChild(root_0, new Type(SHORT, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "SHORT"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:204:4: intKey
                    {
                    pushFollow(FOLLOW_intKey_in_typeSpecifier881);
                    intKey23=intKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_intKey.add(intKey23.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 204:11: -> INT[$start.getStartIndex(),$text.length(),\"INT\"]
                    {
                        adaptor.addChild(root_0, new Type(INT, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "INT"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:205:4: longKey
                    {
                    pushFollow(FOLLOW_longKey_in_typeSpecifier894);
                    longKey24=longKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_longKey.add(longKey24.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 205:12: -> LONG[$start.getStartIndex(),$text.length(),\"LONG\"]
                    {
                        adaptor.addChild(root_0, new Type(LONG, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "LONG"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:206:4: floatKey
                    {
                    pushFollow(FOLLOW_floatKey_in_typeSpecifier907);
                    floatKey25=floatKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_floatKey.add(floatKey25.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 206:13: -> FLOAT[$start.getStartIndex(),$text.length(),\"FLOAT\"]
                    {
                        adaptor.addChild(root_0, new Type(FLOAT, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "FLOAT"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:207:4: doubleKey
                    {
                    pushFollow(FOLLOW_doubleKey_in_typeSpecifier920);
                    doubleKey26=doubleKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_doubleKey.add(doubleKey26.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 207:14: -> DOUBLE[$start.getStartIndex(),$text.length(),\"DOUBLE\"]
                    {
                        adaptor.addChild(root_0, new Type(DOUBLE, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "DOUBLE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:208:4: logKey
                    {
                    pushFollow(FOLLOW_logKey_in_typeSpecifier933);
                    logKey27=logKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_logKey.add(logKey27.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 208:11: -> LOG[$start.getStartIndex(),$text.length(),\"LOG\"]
                    {
                        adaptor.addChild(root_0, new Type(LOG, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "LOG"));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 6, typeSpecifier_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "typeSpecifier"

    public static class registerDeclaration_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "registerDeclaration"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:215:1: registerDeclaration : registerType identifier intLiteralOrConst intLiteralOrConst bit ';' -> ^( REGISTER_DECL[$start.getStartIndex(),$text.length(),\"REGISTER_DECL\"] registerType identifier intLiteralOrConst intLiteralOrConst bit ) ;
    public final ReflexParser.registerDeclaration_return registerDeclaration() throws RecognitionException {
        ReflexParser.registerDeclaration_return retval = new ReflexParser.registerDeclaration_return();
        retval.start = input.LT(1);
        int registerDeclaration_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal33=null;
        ReflexParser.registerType_return registerType28 = null;

        ReflexParser.identifier_return identifier29 = null;

        ReflexParser.intLiteralOrConst_return intLiteralOrConst30 = null;

        ReflexParser.intLiteralOrConst_return intLiteralOrConst31 = null;

        ReflexParser.bit_return bit32 = null;


        ASTNode char_literal33_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_bit=new RewriteRuleSubtreeStream(adaptor,"rule bit");
        RewriteRuleSubtreeStream stream_registerType=new RewriteRuleSubtreeStream(adaptor,"rule registerType");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        RewriteRuleSubtreeStream stream_intLiteralOrConst=new RewriteRuleSubtreeStream(adaptor,"rule intLiteralOrConst");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 7) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:216:2: ( registerType identifier intLiteralOrConst intLiteralOrConst bit ';' -> ^( REGISTER_DECL[$start.getStartIndex(),$text.length(),\"REGISTER_DECL\"] registerType identifier intLiteralOrConst intLiteralOrConst bit ) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:216:4: registerType identifier intLiteralOrConst intLiteralOrConst bit ';'
            {
            pushFollow(FOLLOW_registerType_in_registerDeclaration954);
            registerType28=registerType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_registerType.add(registerType28.getTree());
            pushFollow(FOLLOW_identifier_in_registerDeclaration956);
            identifier29=identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_identifier.add(identifier29.getTree());
            pushFollow(FOLLOW_intLiteralOrConst_in_registerDeclaration958);
            intLiteralOrConst30=intLiteralOrConst();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_intLiteralOrConst.add(intLiteralOrConst30.getTree());
            pushFollow(FOLLOW_intLiteralOrConst_in_registerDeclaration960);
            intLiteralOrConst31=intLiteralOrConst();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_intLiteralOrConst.add(intLiteralOrConst31.getTree());
            pushFollow(FOLLOW_bit_in_registerDeclaration962);
            bit32=bit();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bit.add(bit32.getTree());
            char_literal33=(CommonToken)match(input,SEMI,FOLLOW_SEMI_in_registerDeclaration966); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SEMI.add(char_literal33);



            // AST REWRITE
            // elements: registerType, identifier, bit, intLiteralOrConst, intLiteralOrConst
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 218:2: -> ^( REGISTER_DECL[$start.getStartIndex(),$text.length(),\"REGISTER_DECL\"] registerType identifier intLiteralOrConst intLiteralOrConst bit )
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:218:5: ^( REGISTER_DECL[$start.getStartIndex(),$text.length(),\"REGISTER_DECL\"] registerType identifier intLiteralOrConst intLiteralOrConst bit )
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new RegisterDeclaration(REGISTER_DECL, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "REGISTER_DECL"), root_1);

                adaptor.addChild(root_1, stream_registerType.nextTree());
                adaptor.addChild(root_1, stream_identifier.nextTree());
                adaptor.addChild(root_1, stream_intLiteralOrConst.nextTree());
                adaptor.addChild(root_1, stream_intLiteralOrConst.nextTree());
                adaptor.addChild(root_1, stream_bit.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 7, registerDeclaration_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "registerDeclaration"

    public static class bit_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "bit"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:223:1: bit : ( '8' | '16' );
    public final ReflexParser.bit_return bit() throws RecognitionException {
        ReflexParser.bit_return retval = new ReflexParser.bit_return();
        retval.start = input.LT(1);
        int bit_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken set34=null;

        ASTNode set34_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 8) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:223:5: ( '8' | '16' )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:
            {
            root_0 = (ASTNode)adaptor.nil();

            set34=(CommonToken)input.LT(1);
            if ( (input.LA(1)>=130 && input.LA(1)<=131) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (ASTNode)adaptor.create(set34));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 8, bit_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "bit"

    public static class registerType_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "registerType"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:226:1: registerType : ( inputKey -> INPUT[$start.getStartIndex(),$text.length(),\"INPUT\"] | outputKey -> OUTPUT[$start.getStartIndex(),$text.length(),\"OUTPUT\"] | ramKey -> RAM[$start.getStartIndex(),$text.length(),\"RAM\"] );
    public final ReflexParser.registerType_return registerType() throws RecognitionException {
        ReflexParser.registerType_return retval = new ReflexParser.registerType_return();
        retval.start = input.LT(1);
        int registerType_StartIndex = input.index();
        ASTNode root_0 = null;

        ReflexParser.inputKey_return inputKey35 = null;

        ReflexParser.outputKey_return outputKey36 = null;

        ReflexParser.ramKey_return ramKey37 = null;


        RewriteRuleSubtreeStream stream_ramKey=new RewriteRuleSubtreeStream(adaptor,"rule ramKey");
        RewriteRuleSubtreeStream stream_inputKey=new RewriteRuleSubtreeStream(adaptor,"rule inputKey");
        RewriteRuleSubtreeStream stream_outputKey=new RewriteRuleSubtreeStream(adaptor,"rule outputKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 9) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:227:2: ( inputKey -> INPUT[$start.getStartIndex(),$text.length(),\"INPUT\"] | outputKey -> OUTPUT[$start.getStartIndex(),$text.length(),\"OUTPUT\"] | ramKey -> RAM[$start.getStartIndex(),$text.length(),\"RAM\"] )
            int alt8=3;
            switch ( input.LA(1) ) {
            case INPUT:
                {
                alt8=1;
                }
                break;
            case OUTPUT:
                {
                alt8=2;
                }
                break;
            case RAM:
                {
                alt8=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }

            switch (alt8) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:227:4: inputKey
                    {
                    pushFollow(FOLLOW_inputKey_in_registerType1017);
                    inputKey35=inputKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_inputKey.add(inputKey35.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 227:13: -> INPUT[$start.getStartIndex(),$text.length(),\"INPUT\"]
                    {
                        adaptor.addChild(root_0, new Type(INPUT, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "INPUT"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:228:4: outputKey
                    {
                    pushFollow(FOLLOW_outputKey_in_registerType1030);
                    outputKey36=outputKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_outputKey.add(outputKey36.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 228:14: -> OUTPUT[$start.getStartIndex(),$text.length(),\"OUTPUT\"]
                    {
                        adaptor.addChild(root_0, new Type(OUTPUT, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "OUTPUT"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:229:4: ramKey
                    {
                    pushFollow(FOLLOW_ramKey_in_registerType1043);
                    ramKey37=ramKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ramKey.add(ramKey37.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 229:11: -> RAM[$start.getStartIndex(),$text.length(),\"RAM\"]
                    {
                        adaptor.addChild(root_0, new Type(RAM, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "RAM"));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 9, registerType_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "registerType"

    public static class intLiteralOrConst_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "intLiteralOrConst"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:232:1: intLiteralOrConst : (il= INTEGER_LITERAL -> INTEGER_LITERAL[$il.getStartIndex(),$il.text.length(),$il] | identifier );
    public final ReflexParser.intLiteralOrConst_return intLiteralOrConst() throws RecognitionException {
        ReflexParser.intLiteralOrConst_return retval = new ReflexParser.intLiteralOrConst_return();
        retval.start = input.LT(1);
        int intLiteralOrConst_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken il=null;
        ReflexParser.identifier_return identifier38 = null;


        ASTNode il_tree=null;
        RewriteRuleTokenStream stream_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token INTEGER_LITERAL");

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 10) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:233:2: (il= INTEGER_LITERAL -> INTEGER_LITERAL[$il.getStartIndex(),$il.text.length(),$il] | identifier )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==INTEGER_LITERAL) ) {
                alt9=1;
            }
            else if ( (LA9_0==IDENTIFIER) ) {
                alt9=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:233:4: il= INTEGER_LITERAL
                    {
                    il=(CommonToken)match(input,INTEGER_LITERAL,FOLLOW_INTEGER_LITERAL_in_intLiteralOrConst1065); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INTEGER_LITERAL.add(il);



                    // AST REWRITE
                    // elements: INTEGER_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 233:23: -> INTEGER_LITERAL[$il.getStartIndex(),$il.text.length(),$il]
                    {
                        adaptor.addChild(root_0, new IntegerLiteral(INTEGER_LITERAL, il.getStartIndex(), (il!=null?il.getText():null).length(), il));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:234:4: identifier
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_identifier_in_intLiteralOrConst1079);
                    identifier38=identifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier38.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 10, intLiteralOrConst_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "intLiteralOrConst"

    public static class processDeclaration_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "processDeclaration"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:237:1: processDeclaration : procKey id= IDENTIFIER '{' ( variableDeclaration )* ( stateDeclaration )+ '}' -> ^( PROCESS_DECL[$start.getStartIndex(),$text.length(),\"PROCESS_DECL\"] PROCESS_NAME[$id.getStartIndex(),$id.text.length(),$id] ( variableDeclaration )* ( stateDeclaration )+ ) ;
    public final ReflexParser.processDeclaration_return processDeclaration() throws RecognitionException {
        ReflexParser.processDeclaration_return retval = new ReflexParser.processDeclaration_return();
        retval.start = input.LT(1);
        int processDeclaration_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken id=null;
        CommonToken char_literal40=null;
        CommonToken char_literal43=null;
        ReflexParser.procKey_return procKey39 = null;

        ReflexParser.variableDeclaration_return variableDeclaration41 = null;

        ReflexParser.stateDeclaration_return stateDeclaration42 = null;


        ASTNode id_tree=null;
        ASTNode char_literal40_tree=null;
        ASTNode char_literal43_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_variableDeclaration=new RewriteRuleSubtreeStream(adaptor,"rule variableDeclaration");
        RewriteRuleSubtreeStream stream_procKey=new RewriteRuleSubtreeStream(adaptor,"rule procKey");
        RewriteRuleSubtreeStream stream_stateDeclaration=new RewriteRuleSubtreeStream(adaptor,"rule stateDeclaration");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 11) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:238:2: ( procKey id= IDENTIFIER '{' ( variableDeclaration )* ( stateDeclaration )+ '}' -> ^( PROCESS_DECL[$start.getStartIndex(),$text.length(),\"PROCESS_DECL\"] PROCESS_NAME[$id.getStartIndex(),$id.text.length(),$id] ( variableDeclaration )* ( stateDeclaration )+ ) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:238:4: procKey id= IDENTIFIER '{' ( variableDeclaration )* ( stateDeclaration )+ '}'
            {
            pushFollow(FOLLOW_procKey_in_processDeclaration1094);
            procKey39=procKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_procKey.add(procKey39.getTree());
            id=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_processDeclaration1098); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(id);

            char_literal40=(CommonToken)match(input,LBRACE,FOLLOW_LBRACE_in_processDeclaration1100); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(char_literal40);

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:238:30: ( variableDeclaration )*
            loop10:
            do {
                int alt10=2;
                alt10 = dfa10.predict(input);
                switch (alt10) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:0:0: variableDeclaration
            	    {
            	    pushFollow(FOLLOW_variableDeclaration_in_processDeclaration1102);
            	    variableDeclaration41=variableDeclaration();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_variableDeclaration.add(variableDeclaration41.getTree());

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:238:51: ( stateDeclaration )+
            int cnt11=0;
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==STATE) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:0:0: stateDeclaration
            	    {
            	    pushFollow(FOLLOW_stateDeclaration_in_processDeclaration1105);
            	    stateDeclaration42=stateDeclaration();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_stateDeclaration.add(stateDeclaration42.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt11 >= 1 ) break loop11;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(11, input);
                        throw eee;
                }
                cnt11++;
            } while (true);

            char_literal43=(CommonToken)match(input,RBRACE,FOLLOW_RBRACE_in_processDeclaration1108); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(char_literal43);



            // AST REWRITE
            // elements: variableDeclaration, stateDeclaration
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 239:2: -> ^( PROCESS_DECL[$start.getStartIndex(),$text.length(),\"PROCESS_DECL\"] PROCESS_NAME[$id.getStartIndex(),$id.text.length(),$id] ( variableDeclaration )* ( stateDeclaration )+ )
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:239:5: ^( PROCESS_DECL[$start.getStartIndex(),$text.length(),\"PROCESS_DECL\"] PROCESS_NAME[$id.getStartIndex(),$id.text.length(),$id] ( variableDeclaration )* ( stateDeclaration )+ )
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new ProcessDeclaration(PROCESS_DECL, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "PROCESS_DECL"), root_1);

                adaptor.addChild(root_1, new Identifier(PROCESS_NAME, id.getStartIndex(), (id!=null?id.getText():null).length(), id));
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:242:3: ( variableDeclaration )*
                while ( stream_variableDeclaration.hasNext() ) {
                    adaptor.addChild(root_1, stream_variableDeclaration.nextTree());

                }
                stream_variableDeclaration.reset();
                if ( !(stream_stateDeclaration.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_stateDeclaration.hasNext() ) {
                    adaptor.addChild(root_1, stream_stateDeclaration.nextTree());

                }
                stream_stateDeclaration.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {

                    	root_0 = (ASTNode)adaptor.nil();
                       
                        	ASTNode root_1 = (ASTNode)adaptor.nil();
                        	root_1 = (ASTNode)adaptor.becomeRoot(new ProcessDeclaration(PROCESS_DECL, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "PROCESS_DECL"), root_1);
                        
            	        if (id != null)
                        		adaptor.addChild(root_1, new Identifier(PROCESS_NAME, id.getStartIndex(), (id!=null?id.getText():null).length(), id));
                        
            	        if (stream_variableDeclaration.hasNext())
            	        {
            		        while ( stream_variableDeclaration.hasNext() )
            		        	adaptor.addChild(root_1, stream_variableDeclaration.nextTree());
            	        }
                       
            		if (stream_stateDeclaration.hasNext())
            		{
            			while ( stream_stateDeclaration.hasNext() )
            				adaptor.addChild(root_1, stream_stateDeclaration.nextTree());
            		}

                        adaptor.addChild(root_0, root_1);

                        retval.tree = root_0;
                    
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 11, processDeclaration_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "processDeclaration"

    public static class identifier_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "identifier"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:272:1: identifier : IDENTIFIER -> IDENTIFIER[$IDENTIFIER.getStartIndex(),$IDENTIFIER.text.length(),$IDENTIFIER] ;
    public final ReflexParser.identifier_return identifier() throws RecognitionException {
        ReflexParser.identifier_return retval = new ReflexParser.identifier_return();
        retval.start = input.LT(1);
        int identifier_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken IDENTIFIER44=null;

        ASTNode IDENTIFIER44_tree=null;
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 12) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:273:2: ( IDENTIFIER -> IDENTIFIER[$IDENTIFIER.getStartIndex(),$IDENTIFIER.text.length(),$IDENTIFIER] )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:273:4: IDENTIFIER
            {
            IDENTIFIER44=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_identifier1164); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER44);



            // AST REWRITE
            // elements: IDENTIFIER
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 274:2: -> IDENTIFIER[$IDENTIFIER.getStartIndex(),$IDENTIFIER.text.length(),$IDENTIFIER]
            {
                adaptor.addChild(root_0, new Identifier(IDENTIFIER, IDENTIFIER44.getStartIndex(), (IDENTIFIER44!=null?IDENTIFIER44.getText():null).length(), IDENTIFIER44));

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 12, identifier_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "identifier"

    public static class variableDeclaration_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "variableDeclaration"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:277:1: variableDeclaration : (type= typeSpecifier varName= identifier (port= portLinkList )? mods= visibleModifier -> ^( VAR_DECL[$start.getStartIndex(),$text.length(),\"VAR_DECL\"] $type $varName ( $port)? $mods) | from= fromKey procKey procName= IDENTIFIER vars+= identifier ( ',' vars+= identifier )* -> ^( VAR_REF[$start.getStartIndex(),$text.length(),\"VAR_REF\"] PROCESS_NAME[$procName.getStartIndex(),$procName.text.length(),$procName] ( $vars)+ ) | enumDeclaration ) ';' ;
    public final ReflexParser.variableDeclaration_return variableDeclaration() throws RecognitionException {
        ReflexParser.variableDeclaration_return retval = new ReflexParser.variableDeclaration_return();
        retval.start = input.LT(1);
        int variableDeclaration_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken procName=null;
        CommonToken char_literal46=null;
        CommonToken char_literal48=null;
        List list_vars=null;
        ReflexParser.typeSpecifier_return type = null;

        ReflexParser.identifier_return varName = null;

        ReflexParser.portLinkList_return port = null;

        ReflexParser.visibleModifier_return mods = null;

        ReflexParser.fromKey_return from = null;

        ReflexParser.procKey_return procKey45 = null;

        ReflexParser.enumDeclaration_return enumDeclaration47 = null;

        RuleReturnScope vars = null;
        ASTNode procName_tree=null;
        ASTNode char_literal46_tree=null;
        ASTNode char_literal48_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleSubtreeStream stream_procKey=new RewriteRuleSubtreeStream(adaptor,"rule procKey");
        RewriteRuleSubtreeStream stream_portLinkList=new RewriteRuleSubtreeStream(adaptor,"rule portLinkList");
        RewriteRuleSubtreeStream stream_visibleModifier=new RewriteRuleSubtreeStream(adaptor,"rule visibleModifier");
        RewriteRuleSubtreeStream stream_typeSpecifier=new RewriteRuleSubtreeStream(adaptor,"rule typeSpecifier");
        RewriteRuleSubtreeStream stream_enumDeclaration=new RewriteRuleSubtreeStream(adaptor,"rule enumDeclaration");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        RewriteRuleSubtreeStream stream_fromKey=new RewriteRuleSubtreeStream(adaptor,"rule fromKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 13) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:278:2: ( (type= typeSpecifier varName= identifier (port= portLinkList )? mods= visibleModifier -> ^( VAR_DECL[$start.getStartIndex(),$text.length(),\"VAR_DECL\"] $type $varName ( $port)? $mods) | from= fromKey procKey procName= IDENTIFIER vars+= identifier ( ',' vars+= identifier )* -> ^( VAR_REF[$start.getStartIndex(),$text.length(),\"VAR_REF\"] PROCESS_NAME[$procName.getStartIndex(),$procName.text.length(),$procName] ( $vars)+ ) | enumDeclaration ) ';' )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:279:2: (type= typeSpecifier varName= identifier (port= portLinkList )? mods= visibleModifier -> ^( VAR_DECL[$start.getStartIndex(),$text.length(),\"VAR_DECL\"] $type $varName ( $port)? $mods) | from= fromKey procKey procName= IDENTIFIER vars+= identifier ( ',' vars+= identifier )* -> ^( VAR_REF[$start.getStartIndex(),$text.length(),\"VAR_REF\"] PROCESS_NAME[$procName.getStartIndex(),$procName.text.length(),$procName] ( $vars)+ ) | enumDeclaration ) ';'
            {
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:279:2: (type= typeSpecifier varName= identifier (port= portLinkList )? mods= visibleModifier -> ^( VAR_DECL[$start.getStartIndex(),$text.length(),\"VAR_DECL\"] $type $varName ( $port)? $mods) | from= fromKey procKey procName= IDENTIFIER vars+= identifier ( ',' vars+= identifier )* -> ^( VAR_REF[$start.getStartIndex(),$text.length(),\"VAR_REF\"] PROCESS_NAME[$procName.getStartIndex(),$procName.text.length(),$procName] ( $vars)+ ) | enumDeclaration )
            int alt14=3;
            alt14 = dfa14.predict(input);
            switch (alt14) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:279:4: type= typeSpecifier varName= identifier (port= portLinkList )? mods= visibleModifier
                    {
                    pushFollow(FOLLOW_typeSpecifier_in_variableDeclaration1193);
                    type=typeSpecifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_typeSpecifier.add(type.getTree());
                    pushFollow(FOLLOW_identifier_in_variableDeclaration1197);
                    varName=identifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_identifier.add(varName.getTree());
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:279:46: (port= portLinkList )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==ASSIGN) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:0:0: port= portLinkList
                            {
                            pushFollow(FOLLOW_portLinkList_in_variableDeclaration1201);
                            port=portLinkList();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_portLinkList.add(port.getTree());

                            }
                            break;

                    }

                    pushFollow(FOLLOW_visibleModifier_in_variableDeclaration1206);
                    mods=visibleModifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_visibleModifier.add(mods.getTree());


                    // AST REWRITE
                    // elements: mods, port, type, varName
                    // token labels: 
                    // rule labels: port, retval, varName, type, mods
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_port=new RewriteRuleSubtreeStream(adaptor,"rule port",port!=null?port.tree:null);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_varName=new RewriteRuleSubtreeStream(adaptor,"rule varName",varName!=null?varName.tree:null);
                    RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type",type!=null?type.tree:null);
                    RewriteRuleSubtreeStream stream_mods=new RewriteRuleSubtreeStream(adaptor,"rule mods",mods!=null?mods.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 280:3: -> ^( VAR_DECL[$start.getStartIndex(),$text.length(),\"VAR_DECL\"] $type $varName ( $port)? $mods)
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:280:6: ^( VAR_DECL[$start.getStartIndex(),$text.length(),\"VAR_DECL\"] $type $varName ( $port)? $mods)
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot(new VariableDeclaration(VAR_DECL, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "VAR_DECL"), root_1);

                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_varName.nextTree());
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:281:19: ( $port)?
                        if ( stream_port.hasNext() ) {
                            adaptor.addChild(root_1, stream_port.nextTree());

                        }
                        stream_port.reset();
                        adaptor.addChild(root_1, stream_mods.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:283:4: from= fromKey procKey procName= IDENTIFIER vars+= identifier ( ',' vars+= identifier )*
                    {
                    pushFollow(FOLLOW_fromKey_in_variableDeclaration1248);
                    from=fromKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_fromKey.add(from.getTree());
                    pushFollow(FOLLOW_procKey_in_variableDeclaration1250);
                    procKey45=procKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_procKey.add(procKey45.getTree());
                    procName=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_variableDeclaration1254); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IDENTIFIER.add(procName);

                    pushFollow(FOLLOW_identifier_in_variableDeclaration1258);
                    vars=identifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_identifier.add(vars.getTree());
                    if (list_vars==null) list_vars=new ArrayList();
                    list_vars.add(vars.getTree());

                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:283:62: ( ',' vars+= identifier )*
                    loop13:
                    do {
                        int alt13=2;
                        int LA13_0 = input.LA(1);

                        if ( (LA13_0==COMMA) ) {
                            alt13=1;
                        }


                        switch (alt13) {
                    	case 1 :
                    	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:283:63: ',' vars+= identifier
                    	    {
                    	    char_literal46=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_variableDeclaration1261); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(char_literal46);

                    	    pushFollow(FOLLOW_identifier_in_variableDeclaration1265);
                    	    vars=identifier();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_identifier.add(vars.getTree());
                    	    if (list_vars==null) list_vars=new ArrayList();
                    	    list_vars.add(vars.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop13;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: vars
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: vars
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_vars=new RewriteRuleSubtreeStream(adaptor,"token vars",list_vars);
                    root_0 = (ASTNode)adaptor.nil();
                    // 284:3: -> ^( VAR_REF[$start.getStartIndex(),$text.length(),\"VAR_REF\"] PROCESS_NAME[$procName.getStartIndex(),$procName.text.length(),$procName] ( $vars)+ )
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:284:6: ^( VAR_REF[$start.getStartIndex(),$text.length(),\"VAR_REF\"] PROCESS_NAME[$procName.getStartIndex(),$procName.text.length(),$procName] ( $vars)+ )
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot(new VariableDeclaration(VAR_REF, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "VAR_REF"), root_1);

                        adaptor.addChild(root_1, new Identifier(PROCESS_NAME, procName.getStartIndex(), (procName!=null?procName.getText():null).length(), procName));
                        if ( !(stream_vars.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_vars.hasNext() ) {
                            adaptor.addChild(root_1, stream_vars.nextTree());

                        }
                        stream_vars.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:288:4: enumDeclaration
                    {
                    pushFollow(FOLLOW_enumDeclaration_in_variableDeclaration1306);
                    enumDeclaration47=enumDeclaration();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_enumDeclaration.add(enumDeclaration47.getTree());

                    }
                    break;

            }

            char_literal48=(CommonToken)match(input,SEMI,FOLLOW_SEMI_in_variableDeclaration1312); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SEMI.add(char_literal48);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {

            		reportError(re);
            		//System.out.println(procName);
            		//System.out.prinvisibleModifiertln(var);			
            			
            		root_0 = (ASTNode)adaptor.nil();
            		ASTNode root_1 = (ASTNode)adaptor.nil();
            		
            		if (((CommonToken)retval.start).getText().equals((from!=null?input.toString(from.start,from.stop):null)))
            		{
            			root_1 = (ASTNode)adaptor.becomeRoot(new VariableDeclaration(VAR_REF, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "VAR_REF"), root_1);
            			
            			if (procName != null)
            			{
            				adaptor.addChild(root_1, new Identifier(PROCESS_NAME, procName.getStartIndex(), (procName!=null?procName.getText():null).length(), procName));
            				         		 
            				if (list_vars != null)
            				{
            					RewriteRuleSubtreeStream stream_vars=new RewriteRuleSubtreeStream(adaptor,"token vars",list_vars);
            				
            					if ( !(stream_vars.hasNext()) ) 
            					{
            						throw new RewriteEarlyExitException();
            					}
            					while ( stream_vars.hasNext() ) 
            					{
            						adaptor.addChild(root_1, stream_vars.nextTree());
            					
            				        }
            					stream_vars.reset();
            				}
            			}
            		}
            		else if (((CommonToken)retval.start).getText().equals((type!=null?input.toString(type.start,type.stop):null)))
            		{
                                    root_1 = (ASTNode)adaptor.becomeRoot(new VariableDeclaration(VAR_DECL, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "VAR_DECL"), root_1);

                                    adaptor.addChild(root_1, type.tree);
                                    if (varName != null)
                                    	adaptor.addChild(root_1, varName.tree);
                                    
                                    if (port != null )
                                        adaptor.addChild(root_1, port.tree);
                                    
                                    if (mods != null)
                                    	adaptor.addChild(root_1, mods.tree);                     
            		}
            			
            		adaptor.addChild(root_0, root_1);
            			
            		retval.tree = root_0;
                     			
                     		//System.out.println(root_0.toStringTree());
            	
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 13, variableDeclaration_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "variableDeclaration"

    public static class portLinkList_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "portLinkList"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:347:1: portLinkList : '=' '{' port+= portLink ( ',' port+= portLink )* '}' -> ^( PORT_LINK_LIST[$start.getStartIndex(),$text.length(),\"PORT_LINK_LIST\"] ( $port)+ ) ;
    public final ReflexParser.portLinkList_return portLinkList() throws RecognitionException {
        ReflexParser.portLinkList_return retval = new ReflexParser.portLinkList_return();
        retval.start = input.LT(1);
        int portLinkList_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal49=null;
        CommonToken char_literal50=null;
        CommonToken char_literal51=null;
        CommonToken char_literal52=null;
        List list_port=null;
        RuleReturnScope port = null;
        ASTNode char_literal49_tree=null;
        ASTNode char_literal50_tree=null;
        ASTNode char_literal51_tree=null;
        ASTNode char_literal52_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleTokenStream stream_ASSIGN=new RewriteRuleTokenStream(adaptor,"token ASSIGN");
        RewriteRuleSubtreeStream stream_portLink=new RewriteRuleSubtreeStream(adaptor,"rule portLink");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 14) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:348:2: ( '=' '{' port+= portLink ( ',' port+= portLink )* '}' -> ^( PORT_LINK_LIST[$start.getStartIndex(),$text.length(),\"PORT_LINK_LIST\"] ( $port)+ ) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:348:4: '=' '{' port+= portLink ( ',' port+= portLink )* '}'
            {
            char_literal49=(CommonToken)match(input,ASSIGN,FOLLOW_ASSIGN_in_portLinkList1333); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ASSIGN.add(char_literal49);

            char_literal50=(CommonToken)match(input,LBRACE,FOLLOW_LBRACE_in_portLinkList1335); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(char_literal50);

            pushFollow(FOLLOW_portLink_in_portLinkList1339);
            port=portLink();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_portLink.add(port.getTree());
            if (list_port==null) list_port=new ArrayList();
            list_port.add(port.getTree());

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:348:27: ( ',' port+= portLink )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==COMMA) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:348:28: ',' port+= portLink
            	    {
            	    char_literal51=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_portLinkList1342); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(char_literal51);

            	    pushFollow(FOLLOW_portLink_in_portLinkList1346);
            	    port=portLink();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_portLink.add(port.getTree());
            	    if (list_port==null) list_port=new ArrayList();
            	    list_port.add(port.getTree());


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            char_literal52=(CommonToken)match(input,RBRACE,FOLLOW_RBRACE_in_portLinkList1350); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(char_literal52);



            // AST REWRITE
            // elements: port
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: port
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_port=new RewriteRuleSubtreeStream(adaptor,"token port",list_port);
            root_0 = (ASTNode)adaptor.nil();
            // 349:2: -> ^( PORT_LINK_LIST[$start.getStartIndex(),$text.length(),\"PORT_LINK_LIST\"] ( $port)+ )
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:349:5: ^( PORT_LINK_LIST[$start.getStartIndex(),$text.length(),\"PORT_LINK_LIST\"] ( $port)+ )
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new PortLinkList(PORT_LINK_LIST, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "PORT_LINK_LIST"), root_1);

                if ( !(stream_port.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_port.hasNext() ) {
                    adaptor.addChild(root_1, stream_port.nextTree());

                }
                stream_port.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {

            		
                        	root_0 = (ASTNode)adaptor.nil();
                       
                            ASTNode root_1 = (ASTNode)adaptor.nil();
                            root_1 = (ASTNode)adaptor.becomeRoot(new PortLinkList(PORT_LINK_LIST, ((CommonToken)retval.start).getStartIndex(),input.toString(retval.start,input.LT(-1)).length(),"PORT_LINK_LIST"), root_1);

                            if ( list_port != null )
                            {
                            	RewriteRuleSubtreeStream stream_port=new RewriteRuleSubtreeStream(adaptor,"token port",list_port);
            	                while ( stream_port.hasNext() )
            	                    adaptor.addChild(root_1, stream_port.nextTree());                  
                            }

                            adaptor.addChild(root_0, root_1);
                       

                        	retval.tree = root_0;
            	
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 14, portLinkList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "portLinkList"

    public static class visibleModifier_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "visibleModifier"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:374:1: visibleModifier : ( localKey -> LOCAL_MODIFIER[$start.getStartIndex(),$text.length(),\"LOCAL_MODIFIER\"] | forKey ( ( procKey procs+= identifier ( ',' procs+= identifier )* ) -> ^( FOR_PROC_LIST_MODIFIER[$start.getStartIndex(),$text.length(),\"FOR_PROC_LIST_MODIFIER\"] ( $procs)+ ) | allKey -> FOR_ALL_MODIFIER[$start.getStartIndex(),$text.length(),\"FOR_ALL_MODIFIER\"] ) );
    public final ReflexParser.visibleModifier_return visibleModifier() throws RecognitionException {
        ReflexParser.visibleModifier_return retval = new ReflexParser.visibleModifier_return();
        retval.start = input.LT(1);
        int visibleModifier_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal56=null;
        List list_procs=null;
        ReflexParser.localKey_return localKey53 = null;

        ReflexParser.forKey_return forKey54 = null;

        ReflexParser.procKey_return procKey55 = null;

        ReflexParser.allKey_return allKey57 = null;

        RuleReturnScope procs = null;
        ASTNode char_literal56_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_allKey=new RewriteRuleSubtreeStream(adaptor,"rule allKey");
        RewriteRuleSubtreeStream stream_procKey=new RewriteRuleSubtreeStream(adaptor,"rule procKey");
        RewriteRuleSubtreeStream stream_identifier=new RewriteRuleSubtreeStream(adaptor,"rule identifier");
        RewriteRuleSubtreeStream stream_forKey=new RewriteRuleSubtreeStream(adaptor,"rule forKey");
        RewriteRuleSubtreeStream stream_localKey=new RewriteRuleSubtreeStream(adaptor,"rule localKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 15) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:375:2: ( localKey -> LOCAL_MODIFIER[$start.getStartIndex(),$text.length(),\"LOCAL_MODIFIER\"] | forKey ( ( procKey procs+= identifier ( ',' procs+= identifier )* ) -> ^( FOR_PROC_LIST_MODIFIER[$start.getStartIndex(),$text.length(),\"FOR_PROC_LIST_MODIFIER\"] ( $procs)+ ) | allKey -> FOR_ALL_MODIFIER[$start.getStartIndex(),$text.length(),\"FOR_ALL_MODIFIER\"] ) )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==LOCAL) ) {
                alt18=1;
            }
            else if ( (LA18_0==FOR) ) {
                alt18=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:375:4: localKey
                    {
                    pushFollow(FOLLOW_localKey_in_visibleModifier1392);
                    localKey53=localKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_localKey.add(localKey53.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 375:13: -> LOCAL_MODIFIER[$start.getStartIndex(),$text.length(),\"LOCAL_MODIFIER\"]
                    {
                        adaptor.addChild(root_0, new VisibleModifier(LOCAL_MODIFIER, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "LOCAL_MODIFIER"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:376:4: forKey ( ( procKey procs+= identifier ( ',' procs+= identifier )* ) -> ^( FOR_PROC_LIST_MODIFIER[$start.getStartIndex(),$text.length(),\"FOR_PROC_LIST_MODIFIER\"] ( $procs)+ ) | allKey -> FOR_ALL_MODIFIER[$start.getStartIndex(),$text.length(),\"FOR_ALL_MODIFIER\"] )
                    {
                    pushFollow(FOLLOW_forKey_in_visibleModifier1405);
                    forKey54=forKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_forKey.add(forKey54.getTree());
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:377:3: ( ( procKey procs+= identifier ( ',' procs+= identifier )* ) -> ^( FOR_PROC_LIST_MODIFIER[$start.getStartIndex(),$text.length(),\"FOR_PROC_LIST_MODIFIER\"] ( $procs)+ ) | allKey -> FOR_ALL_MODIFIER[$start.getStartIndex(),$text.length(),\"FOR_ALL_MODIFIER\"] )
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==PROC) ) {
                        alt17=1;
                    }
                    else if ( (LA17_0==ALL) ) {
                        alt17=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 17, 0, input);

                        throw nvae;
                    }
                    switch (alt17) {
                        case 1 :
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:377:4: ( procKey procs+= identifier ( ',' procs+= identifier )* )
                            {
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:377:4: ( procKey procs+= identifier ( ',' procs+= identifier )* )
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:377:5: procKey procs+= identifier ( ',' procs+= identifier )*
                            {
                            pushFollow(FOLLOW_procKey_in_visibleModifier1412);
                            procKey55=procKey();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_procKey.add(procKey55.getTree());
                            pushFollow(FOLLOW_identifier_in_visibleModifier1416);
                            procs=identifier();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_identifier.add(procs.getTree());
                            if (list_procs==null) list_procs=new ArrayList();
                            list_procs.add(procs.getTree());

                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:377:31: ( ',' procs+= identifier )*
                            loop16:
                            do {
                                int alt16=2;
                                int LA16_0 = input.LA(1);

                                if ( (LA16_0==COMMA) ) {
                                    alt16=1;
                                }


                                switch (alt16) {
                            	case 1 :
                            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:377:32: ',' procs+= identifier
                            	    {
                            	    char_literal56=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_visibleModifier1419); if (state.failed) return retval; 
                            	    if ( state.backtracking==0 ) stream_COMMA.add(char_literal56);

                            	    pushFollow(FOLLOW_identifier_in_visibleModifier1423);
                            	    procs=identifier();

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) stream_identifier.add(procs.getTree());
                            	    if (list_procs==null) list_procs=new ArrayList();
                            	    list_procs.add(procs.getTree());


                            	    }
                            	    break;

                            	default :
                            	    break loop16;
                                }
                            } while (true);


                            }



                            // AST REWRITE
                            // elements: procs
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: procs
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                            RewriteRuleSubtreeStream stream_procs=new RewriteRuleSubtreeStream(adaptor,"token procs",list_procs);
                            root_0 = (ASTNode)adaptor.nil();
                            // 378:3: -> ^( FOR_PROC_LIST_MODIFIER[$start.getStartIndex(),$text.length(),\"FOR_PROC_LIST_MODIFIER\"] ( $procs)+ )
                            {
                                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:378:6: ^( FOR_PROC_LIST_MODIFIER[$start.getStartIndex(),$text.length(),\"FOR_PROC_LIST_MODIFIER\"] ( $procs)+ )
                                {
                                ASTNode root_1 = (ASTNode)adaptor.nil();
                                root_1 = (ASTNode)adaptor.becomeRoot(new VisibleModifier(FOR_PROC_LIST_MODIFIER, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "FOR_PROC_LIST_MODIFIER"), root_1);

                                if ( !(stream_procs.hasNext()) ) {
                                    throw new RewriteEarlyExitException();
                                }
                                while ( stream_procs.hasNext() ) {
                                    adaptor.addChild(root_1, stream_procs.nextTree());

                                }
                                stream_procs.reset();

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:380:4: allKey
                            {
                            pushFollow(FOLLOW_allKey_in_visibleModifier1455);
                            allKey57=allKey();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_allKey.add(allKey57.getTree());


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

                            root_0 = (ASTNode)adaptor.nil();
                            // 381:3: -> FOR_ALL_MODIFIER[$start.getStartIndex(),$text.length(),\"FOR_ALL_MODIFIER\"]
                            {
                                adaptor.addChild(root_0, new VisibleModifier(FOR_ALL_MODIFIER, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "FOR_ALL_MODIFIER"));

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 15, visibleModifier_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "visibleModifier"

    public static class portLink_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "portLink"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:385:1: portLink : IDENTIFIER '[' INTEGER_LITERAL ']' -> PORT_LINK[$start.getStartIndex(),$text.length(),$IDENTIFIER.text, Integer.parseInt($INTEGER_LITERAL.text)] ;
    public final ReflexParser.portLink_return portLink() throws RecognitionException {
        ReflexParser.portLink_return retval = new ReflexParser.portLink_return();
        retval.start = input.LT(1);
        int portLink_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken IDENTIFIER58=null;
        CommonToken char_literal59=null;
        CommonToken INTEGER_LITERAL60=null;
        CommonToken char_literal61=null;

        ASTNode IDENTIFIER58_tree=null;
        ASTNode char_literal59_tree=null;
        ASTNode INTEGER_LITERAL60_tree=null;
        ASTNode char_literal61_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleTokenStream stream_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token INTEGER_LITERAL");

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 16) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:386:2: ( IDENTIFIER '[' INTEGER_LITERAL ']' -> PORT_LINK[$start.getStartIndex(),$text.length(),$IDENTIFIER.text, Integer.parseInt($INTEGER_LITERAL.text)] )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:386:4: IDENTIFIER '[' INTEGER_LITERAL ']'
            {
            IDENTIFIER58=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_portLink1491); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER58);

            char_literal59=(CommonToken)match(input,LBRACKET,FOLLOW_LBRACKET_in_portLink1493); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(char_literal59);

            INTEGER_LITERAL60=(CommonToken)match(input,INTEGER_LITERAL,FOLLOW_INTEGER_LITERAL_in_portLink1495); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_INTEGER_LITERAL.add(INTEGER_LITERAL60);

            char_literal61=(CommonToken)match(input,RBRACKET,FOLLOW_RBRACKET_in_portLink1497); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(char_literal61);



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

            root_0 = (ASTNode)adaptor.nil();
            // 387:2: -> PORT_LINK[$start.getStartIndex(),$text.length(),$IDENTIFIER.text, Integer.parseInt($INTEGER_LITERAL.text)]
            {
                adaptor.addChild(root_0, new PortLink(PORT_LINK, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), (IDENTIFIER58!=null?IDENTIFIER58.getText():null), Integer.parseInt((INTEGER_LITERAL60!=null?INTEGER_LITERAL60.getText():null))));

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 16, portLink_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "portLink"

    public static class stateDeclaration_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "stateDeclaration"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:390:1: stateDeclaration : stateKey IDENTIFIER compoundStatement -> ^( STATE_DECL[$start.getStartIndex(),$text.length(),\"STATE_DECL\"] STATE_NAME[$IDENTIFIER.getStartIndex(),$IDENTIFIER.text.length(),$IDENTIFIER] compoundStatement ) ;
    public final ReflexParser.stateDeclaration_return stateDeclaration() throws RecognitionException {
        ReflexParser.stateDeclaration_return retval = new ReflexParser.stateDeclaration_return();
        retval.start = input.LT(1);
        int stateDeclaration_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken IDENTIFIER63=null;
        ReflexParser.stateKey_return stateKey62 = null;

        ReflexParser.compoundStatement_return compoundStatement64 = null;


        ASTNode IDENTIFIER63_tree=null;
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleSubtreeStream stream_compoundStatement=new RewriteRuleSubtreeStream(adaptor,"rule compoundStatement");
        RewriteRuleSubtreeStream stream_stateKey=new RewriteRuleSubtreeStream(adaptor,"rule stateKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 17) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:391:2: ( stateKey IDENTIFIER compoundStatement -> ^( STATE_DECL[$start.getStartIndex(),$text.length(),\"STATE_DECL\"] STATE_NAME[$IDENTIFIER.getStartIndex(),$IDENTIFIER.text.length(),$IDENTIFIER] compoundStatement ) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:391:4: stateKey IDENTIFIER compoundStatement
            {
            pushFollow(FOLLOW_stateKey_in_stateDeclaration1517);
            stateKey62=stateKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_stateKey.add(stateKey62.getTree());
            IDENTIFIER63=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_stateDeclaration1519); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER63);

            pushFollow(FOLLOW_compoundStatement_in_stateDeclaration1521);
            compoundStatement64=compoundStatement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_compoundStatement.add(compoundStatement64.getTree());


            // AST REWRITE
            // elements: compoundStatement
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 392:2: -> ^( STATE_DECL[$start.getStartIndex(),$text.length(),\"STATE_DECL\"] STATE_NAME[$IDENTIFIER.getStartIndex(),$IDENTIFIER.text.length(),$IDENTIFIER] compoundStatement )
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:392:5: ^( STATE_DECL[$start.getStartIndex(),$text.length(),\"STATE_DECL\"] STATE_NAME[$IDENTIFIER.getStartIndex(),$IDENTIFIER.text.length(),$IDENTIFIER] compoundStatement )
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new StateDeclaration(STATE_DECL, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "STATE_DECL"), root_1);

                adaptor.addChild(root_1, new Identifier(STATE_NAME, IDENTIFIER63.getStartIndex(), (IDENTIFIER63!=null?IDENTIFIER63.getText():null).length(), IDENTIFIER63));
                adaptor.addChild(root_1, stream_compoundStatement.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 17, stateDeclaration_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "stateDeclaration"

    public static class enumDeclaration_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "enumDeclaration"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:399:1: enumDeclaration options {k=3; } : enumKey '{' enumeratorList '}' ;
    public final ReflexParser.enumDeclaration_return enumDeclaration() throws RecognitionException {
        ReflexParser.enumDeclaration_return retval = new ReflexParser.enumDeclaration_return();
        retval.start = input.LT(1);
        int enumDeclaration_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal66=null;
        CommonToken char_literal68=null;
        ReflexParser.enumKey_return enumKey65 = null;

        ReflexParser.enumeratorList_return enumeratorList67 = null;


        ASTNode char_literal66_tree=null;
        ASTNode char_literal68_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 18) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:401:2: ( enumKey '{' enumeratorList '}' )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:401:4: enumKey '{' enumeratorList '}'
            {
            root_0 = (ASTNode)adaptor.nil();

            pushFollow(FOLLOW_enumKey_in_enumDeclaration1573);
            enumKey65=enumKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, enumKey65.getTree());
            char_literal66=(CommonToken)match(input,LBRACE,FOLLOW_LBRACE_in_enumDeclaration1575); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal66_tree = (ASTNode)adaptor.create(char_literal66);
            adaptor.addChild(root_0, char_literal66_tree);
            }
            pushFollow(FOLLOW_enumeratorList_in_enumDeclaration1577);
            enumeratorList67=enumeratorList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, enumeratorList67.getTree());
            char_literal68=(CommonToken)match(input,RBRACE,FOLLOW_RBRACE_in_enumDeclaration1579); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            char_literal68_tree = (ASTNode)adaptor.create(char_literal68);
            adaptor.addChild(root_0, char_literal68_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 18, enumDeclaration_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "enumDeclaration"

    public static class enumeratorList_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "enumeratorList"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:404:1: enumeratorList : enumerator ( ',' enumerator )* ;
    public final ReflexParser.enumeratorList_return enumeratorList() throws RecognitionException {
        ReflexParser.enumeratorList_return retval = new ReflexParser.enumeratorList_return();
        retval.start = input.LT(1);
        int enumeratorList_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal70=null;
        ReflexParser.enumerator_return enumerator69 = null;

        ReflexParser.enumerator_return enumerator71 = null;


        ASTNode char_literal70_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 19) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:405:2: ( enumerator ( ',' enumerator )* )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:405:4: enumerator ( ',' enumerator )*
            {
            root_0 = (ASTNode)adaptor.nil();

            pushFollow(FOLLOW_enumerator_in_enumeratorList1590);
            enumerator69=enumerator();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, enumerator69.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:405:15: ( ',' enumerator )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==COMMA) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:405:16: ',' enumerator
            	    {
            	    char_literal70=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_enumeratorList1593); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    char_literal70_tree = (ASTNode)adaptor.create(char_literal70);
            	    adaptor.addChild(root_0, char_literal70_tree);
            	    }
            	    pushFollow(FOLLOW_enumerator_in_enumeratorList1595);
            	    enumerator71=enumerator();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, enumerator71.getTree());

            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 19, enumeratorList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "enumeratorList"

    public static class enumerator_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "enumerator"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:408:1: enumerator : IDENTIFIER ( '=' logical_or_expression )? ;
    public final ReflexParser.enumerator_return enumerator() throws RecognitionException {
        ReflexParser.enumerator_return retval = new ReflexParser.enumerator_return();
        retval.start = input.LT(1);
        int enumerator_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken IDENTIFIER72=null;
        CommonToken char_literal73=null;
        ReflexParser.logical_or_expression_return logical_or_expression74 = null;


        ASTNode IDENTIFIER72_tree=null;
        ASTNode char_literal73_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 20) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:409:2: ( IDENTIFIER ( '=' logical_or_expression )? )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:409:4: IDENTIFIER ( '=' logical_or_expression )?
            {
            root_0 = (ASTNode)adaptor.nil();

            IDENTIFIER72=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_enumerator1608); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IDENTIFIER72_tree = (ASTNode)adaptor.create(IDENTIFIER72);
            adaptor.addChild(root_0, IDENTIFIER72_tree);
            }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:409:15: ( '=' logical_or_expression )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==ASSIGN) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:409:16: '=' logical_or_expression
                    {
                    char_literal73=(CommonToken)match(input,ASSIGN,FOLLOW_ASSIGN_in_enumerator1611); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal73_tree = (ASTNode)adaptor.create(char_literal73);
                    adaptor.addChild(root_0, char_literal73_tree);
                    }
                    pushFollow(FOLLOW_logical_or_expression_in_enumerator1613);
                    logical_or_expression74=logical_or_expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, logical_or_expression74.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 20, enumerator_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "enumerator"

    public static class additiveExpression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "additiveExpression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:415:1: additiveExpression : ( multiplicativeExpression ) ( ( '+' | '-' ) multiplicativeExpression )* ;
    public final ReflexParser.additiveExpression_return additiveExpression() throws RecognitionException {
        ReflexParser.additiveExpression_return retval = new ReflexParser.additiveExpression_return();
        retval.start = input.LT(1);
        int additiveExpression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken set76=null;
        ReflexParser.multiplicativeExpression_return multiplicativeExpression75 = null;

        ReflexParser.multiplicativeExpression_return multiplicativeExpression77 = null;


        ASTNode set76_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 21) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:416:2: ( ( multiplicativeExpression ) ( ( '+' | '-' ) multiplicativeExpression )* )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:416:4: ( multiplicativeExpression ) ( ( '+' | '-' ) multiplicativeExpression )*
            {
            root_0 = (ASTNode)adaptor.nil();

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:416:4: ( multiplicativeExpression )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:416:5: multiplicativeExpression
            {
            pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression1630);
            multiplicativeExpression75=multiplicativeExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, multiplicativeExpression75.getTree());

            }

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:416:31: ( ( '+' | '-' ) multiplicativeExpression )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( ((LA21_0>=PLUS && LA21_0<=MINUS)) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:416:32: ( '+' | '-' ) multiplicativeExpression
            	    {
            	    set76=(CommonToken)input.LT(1);
            	    set76=(CommonToken)input.LT(1);
            	    if ( (input.LA(1)>=PLUS && input.LA(1)<=MINUS) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) root_0 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(set76), root_0);
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression1643);
            	    multiplicativeExpression77=multiplicativeExpression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, multiplicativeExpression77.getTree());

            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 21, additiveExpression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "additiveExpression"

    public static class multiplicativeExpression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "multiplicativeExpression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:419:1: multiplicativeExpression : ( unaryExpression ) ( ( '*' | '/' | '%' ) unaryExpression )* ;
    public final ReflexParser.multiplicativeExpression_return multiplicativeExpression() throws RecognitionException {
        ReflexParser.multiplicativeExpression_return retval = new ReflexParser.multiplicativeExpression_return();
        retval.start = input.LT(1);
        int multiplicativeExpression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken set79=null;
        ReflexParser.unaryExpression_return unaryExpression78 = null;

        ReflexParser.unaryExpression_return unaryExpression80 = null;


        ASTNode set79_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 22) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:420:2: ( ( unaryExpression ) ( ( '*' | '/' | '%' ) unaryExpression )* )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:420:4: ( unaryExpression ) ( ( '*' | '/' | '%' ) unaryExpression )*
            {
            root_0 = (ASTNode)adaptor.nil();

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:420:4: ( unaryExpression )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:420:5: unaryExpression
            {
            pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression1657);
            unaryExpression78=unaryExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, unaryExpression78.getTree());

            }

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:420:22: ( ( '*' | '/' | '%' ) unaryExpression )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( ((LA22_0>=STAR && LA22_0<=MOD)) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:420:23: ( '*' | '/' | '%' ) unaryExpression
            	    {
            	    set79=(CommonToken)input.LT(1);
            	    set79=(CommonToken)input.LT(1);
            	    if ( (input.LA(1)>=STAR && input.LA(1)<=MOD) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) root_0 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(set79), root_0);
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression1674);
            	    unaryExpression80=unaryExpression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, unaryExpression80.getTree());

            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 22, multiplicativeExpression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "multiplicativeExpression"

    public static class unaryExpression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unaryExpression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:423:1: unaryExpression : ( PLUS unaryExpression -> ^( UNARY_PLUS[$PLUS, \"UNARY_PLUS\"] unaryExpression ) | MINUS unaryExpression -> ^( UNARY_MINUS[$MINUS, \"UNARY_MINUS\"] unaryExpression ) | NOT unaryExpression -> ^( NOT[$NOT, \"NOT\"] unaryExpression ) | LOGICAL_NOT unaryExpression -> ^( LOGICAL_NOT[$LOGICAL_NOT, \"LOGICAL_NOT\"] unaryExpression ) | INC postfixExpression -> ^( PRE_INC[$INC, \"PRE_INC\"] postfixExpression ) | DEC postfixExpression -> ^( PRE_DEC[$DEC, \"PRE_DEC\"] postfixExpression ) | postfixExpression );
    public final ReflexParser.unaryExpression_return unaryExpression() throws RecognitionException {
        ReflexParser.unaryExpression_return retval = new ReflexParser.unaryExpression_return();
        retval.start = input.LT(1);
        int unaryExpression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken PLUS81=null;
        CommonToken MINUS83=null;
        CommonToken NOT85=null;
        CommonToken LOGICAL_NOT87=null;
        CommonToken INC89=null;
        CommonToken DEC91=null;
        ReflexParser.unaryExpression_return unaryExpression82 = null;

        ReflexParser.unaryExpression_return unaryExpression84 = null;

        ReflexParser.unaryExpression_return unaryExpression86 = null;

        ReflexParser.unaryExpression_return unaryExpression88 = null;

        ReflexParser.postfixExpression_return postfixExpression90 = null;

        ReflexParser.postfixExpression_return postfixExpression92 = null;

        ReflexParser.postfixExpression_return postfixExpression93 = null;


        ASTNode PLUS81_tree=null;
        ASTNode MINUS83_tree=null;
        ASTNode NOT85_tree=null;
        ASTNode LOGICAL_NOT87_tree=null;
        ASTNode INC89_tree=null;
        ASTNode DEC91_tree=null;
        RewriteRuleTokenStream stream_DEC=new RewriteRuleTokenStream(adaptor,"token DEC");
        RewriteRuleTokenStream stream_INC=new RewriteRuleTokenStream(adaptor,"token INC");
        RewriteRuleTokenStream stream_LOGICAL_NOT=new RewriteRuleTokenStream(adaptor,"token LOGICAL_NOT");
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_unaryExpression=new RewriteRuleSubtreeStream(adaptor,"rule unaryExpression");
        RewriteRuleSubtreeStream stream_postfixExpression=new RewriteRuleSubtreeStream(adaptor,"rule postfixExpression");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 23) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:424:2: ( PLUS unaryExpression -> ^( UNARY_PLUS[$PLUS, \"UNARY_PLUS\"] unaryExpression ) | MINUS unaryExpression -> ^( UNARY_MINUS[$MINUS, \"UNARY_MINUS\"] unaryExpression ) | NOT unaryExpression -> ^( NOT[$NOT, \"NOT\"] unaryExpression ) | LOGICAL_NOT unaryExpression -> ^( LOGICAL_NOT[$LOGICAL_NOT, \"LOGICAL_NOT\"] unaryExpression ) | INC postfixExpression -> ^( PRE_INC[$INC, \"PRE_INC\"] postfixExpression ) | DEC postfixExpression -> ^( PRE_DEC[$DEC, \"PRE_DEC\"] postfixExpression ) | postfixExpression )
            int alt23=7;
            alt23 = dfa23.predict(input);
            switch (alt23) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:424:4: PLUS unaryExpression
                    {
                    PLUS81=(CommonToken)match(input,PLUS,FOLLOW_PLUS_in_unaryExpression1688); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS81);

                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression1690);
                    unaryExpression82=unaryExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_unaryExpression.add(unaryExpression82.getTree());


                    // AST REWRITE
                    // elements: unaryExpression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 424:27: -> ^( UNARY_PLUS[$PLUS, \"UNARY_PLUS\"] unaryExpression )
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:424:30: ^( UNARY_PLUS[$PLUS, \"UNARY_PLUS\"] unaryExpression )
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(UNARY_PLUS, PLUS81, "UNARY_PLUS"), root_1);

                        adaptor.addChild(root_1, stream_unaryExpression.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:425:4: MINUS unaryExpression
                    {
                    MINUS83=(CommonToken)match(input,MINUS,FOLLOW_MINUS_in_unaryExpression1706); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS83);

                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression1708);
                    unaryExpression84=unaryExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_unaryExpression.add(unaryExpression84.getTree());


                    // AST REWRITE
                    // elements: unaryExpression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 425:27: -> ^( UNARY_MINUS[$MINUS, \"UNARY_MINUS\"] unaryExpression )
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:425:30: ^( UNARY_MINUS[$MINUS, \"UNARY_MINUS\"] unaryExpression )
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(UNARY_MINUS, MINUS83, "UNARY_MINUS"), root_1);

                        adaptor.addChild(root_1, stream_unaryExpression.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:426:4: NOT unaryExpression
                    {
                    NOT85=(CommonToken)match(input,NOT,FOLLOW_NOT_in_unaryExpression1723); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT85);

                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression1725);
                    unaryExpression86=unaryExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_unaryExpression.add(unaryExpression86.getTree());


                    // AST REWRITE
                    // elements: NOT, unaryExpression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 426:26: -> ^( NOT[$NOT, \"NOT\"] unaryExpression )
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:426:29: ^( NOT[$NOT, \"NOT\"] unaryExpression )
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(NOT, NOT85, "NOT"), root_1);

                        adaptor.addChild(root_1, stream_unaryExpression.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:427:4: LOGICAL_NOT unaryExpression
                    {
                    LOGICAL_NOT87=(CommonToken)match(input,LOGICAL_NOT,FOLLOW_LOGICAL_NOT_in_unaryExpression1741); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LOGICAL_NOT.add(LOGICAL_NOT87);

                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression1743);
                    unaryExpression88=unaryExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_unaryExpression.add(unaryExpression88.getTree());


                    // AST REWRITE
                    // elements: unaryExpression, LOGICAL_NOT
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 427:33: -> ^( LOGICAL_NOT[$LOGICAL_NOT, \"LOGICAL_NOT\"] unaryExpression )
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:427:36: ^( LOGICAL_NOT[$LOGICAL_NOT, \"LOGICAL_NOT\"] unaryExpression )
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(LOGICAL_NOT, LOGICAL_NOT87, "LOGICAL_NOT"), root_1);

                        adaptor.addChild(root_1, stream_unaryExpression.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:428:4: INC postfixExpression
                    {
                    INC89=(CommonToken)match(input,INC,FOLLOW_INC_in_unaryExpression1758); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INC.add(INC89);

                    pushFollow(FOLLOW_postfixExpression_in_unaryExpression1760);
                    postfixExpression90=postfixExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_postfixExpression.add(postfixExpression90.getTree());


                    // AST REWRITE
                    // elements: postfixExpression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 428:27: -> ^( PRE_INC[$INC, \"PRE_INC\"] postfixExpression )
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:428:30: ^( PRE_INC[$INC, \"PRE_INC\"] postfixExpression )
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(PRE_INC, INC89, "PRE_INC"), root_1);

                        adaptor.addChild(root_1, stream_postfixExpression.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:429:4: DEC postfixExpression
                    {
                    DEC91=(CommonToken)match(input,DEC,FOLLOW_DEC_in_unaryExpression1775); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DEC.add(DEC91);

                    pushFollow(FOLLOW_postfixExpression_in_unaryExpression1777);
                    postfixExpression92=postfixExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_postfixExpression.add(postfixExpression92.getTree());


                    // AST REWRITE
                    // elements: postfixExpression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 429:27: -> ^( PRE_DEC[$DEC, \"PRE_DEC\"] postfixExpression )
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:429:30: ^( PRE_DEC[$DEC, \"PRE_DEC\"] postfixExpression )
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(PRE_DEC, DEC91, "PRE_DEC"), root_1);

                        adaptor.addChild(root_1, stream_postfixExpression.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:430:4: postfixExpression
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_postfixExpression_in_unaryExpression1792);
                    postfixExpression93=postfixExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, postfixExpression93.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 23, unaryExpression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "unaryExpression"

    public static class postfixExpression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "postfixExpression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:433:1: postfixExpression : ( primaryExpression -> primaryExpression ) ( INC -> ^( POST_INC[$INC, \"POST_INC\"] $postfixExpression) | DEC -> ^( POST_DEC[$DEC, \"POST_DEC\"] $postfixExpression) | LPAREN ( arguments )? RPAREN -> ^( FUNCTION_CALL $postfixExpression ( arguments )? ) )? ;
    public final ReflexParser.postfixExpression_return postfixExpression() throws RecognitionException {
        ReflexParser.postfixExpression_return retval = new ReflexParser.postfixExpression_return();
        retval.start = input.LT(1);
        int postfixExpression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken INC95=null;
        CommonToken DEC96=null;
        CommonToken LPAREN97=null;
        CommonToken RPAREN99=null;
        ReflexParser.primaryExpression_return primaryExpression94 = null;

        ReflexParser.arguments_return arguments98 = null;


        ASTNode INC95_tree=null;
        ASTNode DEC96_tree=null;
        ASTNode LPAREN97_tree=null;
        ASTNode RPAREN99_tree=null;
        RewriteRuleTokenStream stream_INC=new RewriteRuleTokenStream(adaptor,"token INC");
        RewriteRuleTokenStream stream_DEC=new RewriteRuleTokenStream(adaptor,"token DEC");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_arguments=new RewriteRuleSubtreeStream(adaptor,"rule arguments");
        RewriteRuleSubtreeStream stream_primaryExpression=new RewriteRuleSubtreeStream(adaptor,"rule primaryExpression");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 24) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:434:2: ( ( primaryExpression -> primaryExpression ) ( INC -> ^( POST_INC[$INC, \"POST_INC\"] $postfixExpression) | DEC -> ^( POST_DEC[$DEC, \"POST_DEC\"] $postfixExpression) | LPAREN ( arguments )? RPAREN -> ^( FUNCTION_CALL $postfixExpression ( arguments )? ) )? )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:434:4: ( primaryExpression -> primaryExpression ) ( INC -> ^( POST_INC[$INC, \"POST_INC\"] $postfixExpression) | DEC -> ^( POST_DEC[$DEC, \"POST_DEC\"] $postfixExpression) | LPAREN ( arguments )? RPAREN -> ^( FUNCTION_CALL $postfixExpression ( arguments )? ) )?
            {
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:434:4: ( primaryExpression -> primaryExpression )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:434:7: primaryExpression
            {
            pushFollow(FOLLOW_primaryExpression_in_postfixExpression1807);
            primaryExpression94=primaryExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_primaryExpression.add(primaryExpression94.getTree());


            // AST REWRITE
            // elements: primaryExpression
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 434:26: -> primaryExpression
            {
                adaptor.addChild(root_0, stream_primaryExpression.nextTree());

            }

            retval.tree = root_0;}
            }

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:436:2: ( INC -> ^( POST_INC[$INC, \"POST_INC\"] $postfixExpression) | DEC -> ^( POST_DEC[$DEC, \"POST_DEC\"] $postfixExpression) | LPAREN ( arguments )? RPAREN -> ^( FUNCTION_CALL $postfixExpression ( arguments )? ) )?
            int alt25=4;
            switch ( input.LA(1) ) {
                case INC:
                    {
                    alt25=1;
                    }
                    break;
                case DEC:
                    {
                    alt25=2;
                    }
                    break;
                case LPAREN:
                    {
                    alt25=3;
                    }
                    break;
            }

            switch (alt25) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:436:4: INC
                    {
                    INC95=(CommonToken)match(input,INC,FOLLOW_INC_in_postfixExpression1823); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INC.add(INC95);



                    // AST REWRITE
                    // elements: postfixExpression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 436:12: -> ^( POST_INC[$INC, \"POST_INC\"] $postfixExpression)
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:436:15: ^( POST_INC[$INC, \"POST_INC\"] $postfixExpression)
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(POST_INC, INC95, "POST_INC"), root_1);

                        adaptor.addChild(root_1, stream_retval.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:437:4: DEC
                    {
                    DEC96=(CommonToken)match(input,DEC,FOLLOW_DEC_in_postfixExpression1842); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DEC.add(DEC96);



                    // AST REWRITE
                    // elements: postfixExpression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 437:12: -> ^( POST_DEC[$DEC, \"POST_DEC\"] $postfixExpression)
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:437:15: ^( POST_DEC[$DEC, \"POST_DEC\"] $postfixExpression)
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(POST_DEC, DEC96, "POST_DEC"), root_1);

                        adaptor.addChild(root_1, stream_retval.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:438:4: LPAREN ( arguments )? RPAREN
                    {
                    LPAREN97=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_postfixExpression1861); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN97);

                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:438:11: ( arguments )?
                    int alt24=2;
                    alt24 = dfa24.predict(input);
                    switch (alt24) {
                        case 1 :
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:0:0: arguments
                            {
                            pushFollow(FOLLOW_arguments_in_postfixExpression1863);
                            arguments98=arguments();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_arguments.add(arguments98.getTree());

                            }
                            break;

                    }

                    RPAREN99=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_postfixExpression1866); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN99);



                    // AST REWRITE
                    // elements: arguments, postfixExpression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 438:29: -> ^( FUNCTION_CALL $postfixExpression ( arguments )? )
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:438:32: ^( FUNCTION_CALL $postfixExpression ( arguments )? )
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(FUNCTION_CALL, "FUNCTION_CALL"), root_1);

                        adaptor.addChild(root_1, stream_retval.nextTree());
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:438:67: ( arguments )?
                        if ( stream_arguments.hasNext() ) {
                            adaptor.addChild(root_1, stream_arguments.nextTree());

                        }
                        stream_arguments.reset();

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

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 24, postfixExpression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "postfixExpression"

    public static class primaryExpression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "primaryExpression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:449:1: primaryExpression : ( identifier | literal | LPAREN expression RPAREN -> ^( PARENTESIZED_EXPR[$LPAREN, \"PARENTESIZED_EXPR\"] expression ) | situationExpression );
    public final ReflexParser.primaryExpression_return primaryExpression() throws RecognitionException {
        ReflexParser.primaryExpression_return retval = new ReflexParser.primaryExpression_return();
        retval.start = input.LT(1);
        int primaryExpression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken LPAREN102=null;
        CommonToken RPAREN104=null;
        ReflexParser.identifier_return identifier100 = null;

        ReflexParser.literal_return literal101 = null;

        ReflexParser.expression_return expression103 = null;

        ReflexParser.situationExpression_return situationExpression105 = null;


        ASTNode LPAREN102_tree=null;
        ASTNode RPAREN104_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 25) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:450:2: ( identifier | literal | LPAREN expression RPAREN -> ^( PARENTESIZED_EXPR[$LPAREN, \"PARENTESIZED_EXPR\"] expression ) | situationExpression )
            int alt26=4;
            switch ( input.LA(1) ) {
            case IDENTIFIER:
                {
                alt26=1;
                }
                break;
            case INTEGER_LITERAL:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case FLOAT_LITERAL:
                {
                alt26=2;
                }
                break;
            case LPAREN:
                {
                alt26=3;
                }
                break;
            case PROC:
                {
                alt26=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }

            switch (alt26) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:450:4: identifier
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_identifier_in_primaryExpression1894);
                    identifier100=identifier();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, identifier100.getTree());

                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:451:4: literal
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_literal_in_primaryExpression1899);
                    literal101=literal();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, literal101.getTree());

                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:452:4: LPAREN expression RPAREN
                    {
                    LPAREN102=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_primaryExpression1904); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN102);

                    pushFollow(FOLLOW_expression_in_primaryExpression1906);
                    expression103=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_expression.add(expression103.getTree());
                    RPAREN104=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_primaryExpression1908); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN104);



                    // AST REWRITE
                    // elements: expression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 452:29: -> ^( PARENTESIZED_EXPR[$LPAREN, \"PARENTESIZED_EXPR\"] expression )
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:452:32: ^( PARENTESIZED_EXPR[$LPAREN, \"PARENTESIZED_EXPR\"] expression )
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(PARENTESIZED_EXPR, LPAREN102, "PARENTESIZED_EXPR"), root_1);

                        adaptor.addChild(root_1, stream_expression.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:453:4: situationExpression
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_situationExpression_in_primaryExpression1922);
                    situationExpression105=situationExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, situationExpression105.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 25, primaryExpression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "primaryExpression"

    public static class literal_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "literal"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:456:1: literal : (il= INTEGER_LITERAL -> INTEGER_LITERAL[$il.getStartIndex(),$il.text.length(),$il] | cl= CHAR_LITERAL -> CHAR_LITERAL[$cl.getStartIndex(),$cl.text.length(),$cl] | sl= STRING_LITERAL -> STRING_LITERAL[$sl.getStartIndex(),$sl.text.length(),$sl] | fl= FLOAT_LITERAL -> FLOAT_LITERAL[$fl.getStartIndex(),$fl.text.length(),$fl] );
    public final ReflexParser.literal_return literal() throws RecognitionException {
        ReflexParser.literal_return retval = new ReflexParser.literal_return();
        retval.start = input.LT(1);
        int literal_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken il=null;
        CommonToken cl=null;
        CommonToken sl=null;
        CommonToken fl=null;

        ASTNode il_tree=null;
        ASTNode cl_tree=null;
        ASTNode sl_tree=null;
        ASTNode fl_tree=null;
        RewriteRuleTokenStream stream_FLOAT_LITERAL=new RewriteRuleTokenStream(adaptor,"token FLOAT_LITERAL");
        RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
        RewriteRuleTokenStream stream_CHAR_LITERAL=new RewriteRuleTokenStream(adaptor,"token CHAR_LITERAL");
        RewriteRuleTokenStream stream_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token INTEGER_LITERAL");

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 26) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:457:5: (il= INTEGER_LITERAL -> INTEGER_LITERAL[$il.getStartIndex(),$il.text.length(),$il] | cl= CHAR_LITERAL -> CHAR_LITERAL[$cl.getStartIndex(),$cl.text.length(),$cl] | sl= STRING_LITERAL -> STRING_LITERAL[$sl.getStartIndex(),$sl.text.length(),$sl] | fl= FLOAT_LITERAL -> FLOAT_LITERAL[$fl.getStartIndex(),$fl.text.length(),$fl] )
            int alt27=4;
            switch ( input.LA(1) ) {
            case INTEGER_LITERAL:
                {
                alt27=1;
                }
                break;
            case CHAR_LITERAL:
                {
                alt27=2;
                }
                break;
            case STRING_LITERAL:
                {
                alt27=3;
                }
                break;
            case FLOAT_LITERAL:
                {
                alt27=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;
            }

            switch (alt27) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:457:7: il= INTEGER_LITERAL
                    {
                    il=(CommonToken)match(input,INTEGER_LITERAL,FOLLOW_INTEGER_LITERAL_in_literal1938); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INTEGER_LITERAL.add(il);



                    // AST REWRITE
                    // elements: INTEGER_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 457:26: -> INTEGER_LITERAL[$il.getStartIndex(),$il.text.length(),$il]
                    {
                        adaptor.addChild(root_0, new IntegerLiteral(INTEGER_LITERAL, il.getStartIndex(), (il!=null?il.getText():null).length(), il));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:458:7: cl= CHAR_LITERAL
                    {
                    cl=(CommonToken)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_literal1956); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CHAR_LITERAL.add(cl);



                    // AST REWRITE
                    // elements: CHAR_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 458:24: -> CHAR_LITERAL[$cl.getStartIndex(),$cl.text.length(),$cl]
                    {
                        adaptor.addChild(root_0, new CharacterLiteral(CHAR_LITERAL, cl.getStartIndex(), (cl!=null?cl.getText():null).length(), cl));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:459:7: sl= STRING_LITERAL
                    {
                    sl=(CommonToken)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_literal1976); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(sl);



                    // AST REWRITE
                    // elements: STRING_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 459:26: -> STRING_LITERAL[$sl.getStartIndex(),$sl.text.length(),$sl]
                    {
                        adaptor.addChild(root_0, new StringLiteral(STRING_LITERAL, sl.getStartIndex(), (sl!=null?sl.getText():null).length(), sl));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:460:7: fl= FLOAT_LITERAL
                    {
                    fl=(CommonToken)match(input,FLOAT_LITERAL,FOLLOW_FLOAT_LITERAL_in_literal1995); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FLOAT_LITERAL.add(fl);



                    // AST REWRITE
                    // elements: FLOAT_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (ASTNode)adaptor.nil();
                    // 460:25: -> FLOAT_LITERAL[$fl.getStartIndex(),$fl.text.length(),$fl]
                    {
                        adaptor.addChild(root_0, new FloatLiteral(FLOAT_LITERAL, fl.getStartIndex(), (fl!=null?fl.getText():null).length(), fl));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 26, literal_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "literal"

    public static class situationExpression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "situationExpression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:463:1: situationExpression : procKey id= IDENTIFIER inKey stateKey stateId= stateIdent -> ^( SITUATION[$start.getStartIndex(),$text.length(),\"SITUATION\"] PROCESS_NAME[$id.getStartIndex(),$id.text.length(),$id] $stateId) ;
    public final ReflexParser.situationExpression_return situationExpression() throws RecognitionException {
        ReflexParser.situationExpression_return retval = new ReflexParser.situationExpression_return();
        retval.start = input.LT(1);
        int situationExpression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken id=null;
        ReflexParser.stateIdent_return stateId = null;

        ReflexParser.procKey_return procKey106 = null;

        ReflexParser.inKey_return inKey107 = null;

        ReflexParser.stateKey_return stateKey108 = null;


        ASTNode id_tree=null;
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleSubtreeStream stream_procKey=new RewriteRuleSubtreeStream(adaptor,"rule procKey");
        RewriteRuleSubtreeStream stream_inKey=new RewriteRuleSubtreeStream(adaptor,"rule inKey");
        RewriteRuleSubtreeStream stream_stateIdent=new RewriteRuleSubtreeStream(adaptor,"rule stateIdent");
        RewriteRuleSubtreeStream stream_stateKey=new RewriteRuleSubtreeStream(adaptor,"rule stateKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 27) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:464:2: ( procKey id= IDENTIFIER inKey stateKey stateId= stateIdent -> ^( SITUATION[$start.getStartIndex(),$text.length(),\"SITUATION\"] PROCESS_NAME[$id.getStartIndex(),$id.text.length(),$id] $stateId) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:464:4: procKey id= IDENTIFIER inKey stateKey stateId= stateIdent
            {
            pushFollow(FOLLOW_procKey_in_situationExpression2018);
            procKey106=procKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_procKey.add(procKey106.getTree());
            id=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_situationExpression2022); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IDENTIFIER.add(id);

            pushFollow(FOLLOW_inKey_in_situationExpression2024);
            inKey107=inKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_inKey.add(inKey107.getTree());
            pushFollow(FOLLOW_stateKey_in_situationExpression2026);
            stateKey108=stateKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_stateKey.add(stateKey108.getTree());
            pushFollow(FOLLOW_stateIdent_in_situationExpression2030);
            stateId=stateIdent();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_stateIdent.add(stateId.getTree());


            // AST REWRITE
            // elements: stateId
            // token labels: 
            // rule labels: retval, stateId
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_stateId=new RewriteRuleSubtreeStream(adaptor,"rule stateId",stateId!=null?stateId.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 465:2: -> ^( SITUATION[$start.getStartIndex(),$text.length(),\"SITUATION\"] PROCESS_NAME[$id.getStartIndex(),$id.text.length(),$id] $stateId)
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:465:5: ^( SITUATION[$start.getStartIndex(),$text.length(),\"SITUATION\"] PROCESS_NAME[$id.getStartIndex(),$id.text.length(),$id] $stateId)
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new SituationExpression(SITUATION, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "SITUATION"), root_1);

                adaptor.addChild(root_1, new Identifier(PROCESS_NAME, id.getStartIndex(), (id!=null?id.getText():null).length(), id));
                adaptor.addChild(root_1, stream_stateId.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {

            		root_0 = (ASTNode)adaptor.nil();
                        
                            ASTNode root_1 = (ASTNode)adaptor.nil();
                            root_1 = (ASTNode)adaptor.becomeRoot(new SituationExpression(SITUATION, ((CommonToken)retval.start).getStartIndex(),input.toString(retval.start,input.LT(-1)).length(),"SITUATION"), root_1);
            		
            		if (id != null)
                 			adaptor.addChild(root_1, new Identifier(PROCESS_NAME, id.getStartIndex(),(id!=null?id.getText():null).length(),id));
                            if (stateId != null)
            	                adaptor.addChild(root_1, stateId.tree);

                            adaptor.addChild(root_0, root_1);               

                        	retval.tree = root_0;
            	
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 27, situationExpression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "situationExpression"

    public static class stateIdent_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "stateIdent"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:487:1: stateIdent : ( IDENTIFIER -> STATE_NAME[$IDENTIFIER.getStartIndex(),$IDENTIFIER.text.length(),$IDENTIFIER] | stopKey -> STATE_NAME[$stopKey.start.getStartIndex(),$stopKey.start.getText().length(),$stopKey.start] | errorKey -> STATE_NAME[$errorKey.start.getStartIndex(),$errorKey.start.getText().length(),$errorKey.start] | activeKey -> STATE_NAME[$activeKey.start.getStartIndex(),$activeKey.start.getText().length(),$activeKey.start] | passiveKey -> STATE_NAME[$passiveKey.start.getStartIndex(),$passiveKey.start.getText().length(),$passiveKey.start] );
    public final ReflexParser.stateIdent_return stateIdent() throws RecognitionException {
        ReflexParser.stateIdent_return retval = new ReflexParser.stateIdent_return();
        retval.start = input.LT(1);
        int stateIdent_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken IDENTIFIER109=null;
        ReflexParser.stopKey_return stopKey110 = null;

        ReflexParser.errorKey_return errorKey111 = null;

        ReflexParser.activeKey_return activeKey112 = null;

        ReflexParser.passiveKey_return passiveKey113 = null;


        ASTNode IDENTIFIER109_tree=null;
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleSubtreeStream stream_stopKey=new RewriteRuleSubtreeStream(adaptor,"rule stopKey");
        RewriteRuleSubtreeStream stream_passiveKey=new RewriteRuleSubtreeStream(adaptor,"rule passiveKey");
        RewriteRuleSubtreeStream stream_activeKey=new RewriteRuleSubtreeStream(adaptor,"rule activeKey");
        RewriteRuleSubtreeStream stream_errorKey=new RewriteRuleSubtreeStream(adaptor,"rule errorKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 28) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:488:2: ( IDENTIFIER -> STATE_NAME[$IDENTIFIER.getStartIndex(),$IDENTIFIER.text.length(),$IDENTIFIER] | stopKey -> STATE_NAME[$stopKey.start.getStartIndex(),$stopKey.start.getText().length(),$stopKey.start] | errorKey -> STATE_NAME[$errorKey.start.getStartIndex(),$errorKey.start.getText().length(),$errorKey.start] | activeKey -> STATE_NAME[$activeKey.start.getStartIndex(),$activeKey.start.getText().length(),$activeKey.start] | passiveKey -> STATE_NAME[$passiveKey.start.getStartIndex(),$passiveKey.start.getText().length(),$passiveKey.start] )
            int alt28=5;
            switch ( input.LA(1) ) {
            case IDENTIFIER:
                {
                alt28=1;
                }
                break;
            case STOP:
                {
                alt28=2;
                }
                break;
            case ERROR:
                {
                alt28=3;
                }
                break;
            case ACTIVE:
                {
                alt28=4;
                }
                break;
            case PASSIVE:
                {
                alt28=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }

            switch (alt28) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:488:4: IDENTIFIER
                    {
                    IDENTIFIER109=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_stateIdent2078); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IDENTIFIER.add(IDENTIFIER109);



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

                    root_0 = (ASTNode)adaptor.nil();
                    // 488:16: -> STATE_NAME[$IDENTIFIER.getStartIndex(),$IDENTIFIER.text.length(),$IDENTIFIER]
                    {
                        adaptor.addChild(root_0, new Identifier(STATE_NAME, IDENTIFIER109.getStartIndex(), (IDENTIFIER109!=null?IDENTIFIER109.getText():null).length(), IDENTIFIER109));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:489:4: stopKey
                    {
                    pushFollow(FOLLOW_stopKey_in_stateIdent2092);
                    stopKey110=stopKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_stopKey.add(stopKey110.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 489:13: -> STATE_NAME[$stopKey.start.getStartIndex(),$stopKey.start.getText().length(),$stopKey.start]
                    {
                        adaptor.addChild(root_0, new Identifier(STATE_NAME, (stopKey110!=null?((CommonToken)stopKey110.start):null).getStartIndex(), (stopKey110!=null?((CommonToken)stopKey110.start):null).getText().length(), (stopKey110!=null?((CommonToken)stopKey110.start):null)));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:490:4: errorKey
                    {
                    pushFollow(FOLLOW_errorKey_in_stateIdent2106);
                    errorKey111=errorKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_errorKey.add(errorKey111.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 490:14: -> STATE_NAME[$errorKey.start.getStartIndex(),$errorKey.start.getText().length(),$errorKey.start]
                    {
                        adaptor.addChild(root_0, new Identifier(STATE_NAME, (errorKey111!=null?((CommonToken)errorKey111.start):null).getStartIndex(), (errorKey111!=null?((CommonToken)errorKey111.start):null).getText().length(), (errorKey111!=null?((CommonToken)errorKey111.start):null)));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:491:4: activeKey
                    {
                    pushFollow(FOLLOW_activeKey_in_stateIdent2120);
                    activeKey112=activeKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_activeKey.add(activeKey112.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 491:15: -> STATE_NAME[$activeKey.start.getStartIndex(),$activeKey.start.getText().length(),$activeKey.start]
                    {
                        adaptor.addChild(root_0, new Identifier(STATE_NAME, (activeKey112!=null?((CommonToken)activeKey112.start):null).getStartIndex(), (activeKey112!=null?((CommonToken)activeKey112.start):null).getText().length(), (activeKey112!=null?((CommonToken)activeKey112.start):null)));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:492:4: passiveKey
                    {
                    pushFollow(FOLLOW_passiveKey_in_stateIdent2134);
                    passiveKey113=passiveKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_passiveKey.add(passiveKey113.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 492:15: -> STATE_NAME[$passiveKey.start.getStartIndex(),$passiveKey.start.getText().length(),$passiveKey.start]
                    {
                        adaptor.addChild(root_0, new Identifier(STATE_NAME, (passiveKey113!=null?((CommonToken)passiveKey113.start):null).getStartIndex(), (passiveKey113!=null?((CommonToken)passiveKey113.start):null).getText().length(), (passiveKey113!=null?((CommonToken)passiveKey113.start):null)));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 28, stateIdent_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "stateIdent"

    public static class arguments_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arguments"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:495:1: arguments : args+= expression ( ',' args+= expression )* -> ^( ARGS[$start.getStartIndex(),$text.length(),\"ARGS\"] ( $args)+ ) ;
    public final ReflexParser.arguments_return arguments() throws RecognitionException {
        ReflexParser.arguments_return retval = new ReflexParser.arguments_return();
        retval.start = input.LT(1);
        int arguments_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal114=null;
        List list_args=null;
        RuleReturnScope args = null;
        ASTNode char_literal114_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 29) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:496:2: (args+= expression ( ',' args+= expression )* -> ^( ARGS[$start.getStartIndex(),$text.length(),\"ARGS\"] ( $args)+ ) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:496:6: args+= expression ( ',' args+= expression )*
            {
            pushFollow(FOLLOW_expression_in_arguments2158);
            args=expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expression.add(args.getTree());
            if (list_args==null) list_args=new ArrayList();
            list_args.add(args.getTree());

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:496:23: ( ',' args+= expression )*
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( (LA29_0==COMMA) ) {
                    alt29=1;
                }


                switch (alt29) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:496:24: ',' args+= expression
            	    {
            	    char_literal114=(CommonToken)match(input,COMMA,FOLLOW_COMMA_in_arguments2161); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(char_literal114);

            	    pushFollow(FOLLOW_expression_in_arguments2165);
            	    args=expression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_expression.add(args.getTree());
            	    if (list_args==null) list_args=new ArrayList();
            	    list_args.add(args.getTree());


            	    }
            	    break;

            	default :
            	    break loop29;
                }
            } while (true);



            // AST REWRITE
            // elements: args
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: args
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_args=new RewriteRuleSubtreeStream(adaptor,"token args",list_args);
            root_0 = (ASTNode)adaptor.nil();
            // 497:2: -> ^( ARGS[$start.getStartIndex(),$text.length(),\"ARGS\"] ( $args)+ )
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:497:5: ^( ARGS[$start.getStartIndex(),$text.length(),\"ARGS\"] ( $args)+ )
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new FunctionArguments(ARGS, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "ARGS"), root_1);

                if ( !(stream_args.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_args.hasNext() ) {
                    adaptor.addChild(root_1, stream_args.nextTree());

                }
                stream_args.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 29, arguments_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "arguments"

    public static class expression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:502:1: expression : assignExpr= assignmentExpression -> ^( EXPR[$start.getStartIndex(),$text.length(),\"EXPRESSION\"] $assignExpr) ;
    public final ReflexParser.expression_return expression() throws RecognitionException {
        ReflexParser.expression_return retval = new ReflexParser.expression_return();
        retval.start = input.LT(1);
        int expression_StartIndex = input.index();
        ASTNode root_0 = null;

        ReflexParser.assignmentExpression_return assignExpr = null;


        RewriteRuleSubtreeStream stream_assignmentExpression=new RewriteRuleSubtreeStream(adaptor,"rule assignmentExpression");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 30) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:503:2: (assignExpr= assignmentExpression -> ^( EXPR[$start.getStartIndex(),$text.length(),\"EXPRESSION\"] $assignExpr) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:503:4: assignExpr= assignmentExpression
            {
            pushFollow(FOLLOW_assignmentExpression_in_expression2201);
            assignExpr=assignmentExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignmentExpression.add(assignExpr.getTree());
            if ( state.backtracking==0 ) {

              		if (assignExpr != null)
              			throw new RecognitionException();
              		
              	
            }


            // AST REWRITE
            // elements: assignExpr
            // token labels: 
            // rule labels: retval, assignExpr
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr",assignExpr!=null?assignExpr.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 509:2: -> ^( EXPR[$start.getStartIndex(),$text.length(),\"EXPRESSION\"] $assignExpr)
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:509:5: ^( EXPR[$start.getStartIndex(),$text.length(),\"EXPRESSION\"] $assignExpr)
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new Expression(EXPR, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "EXPRESSION"), root_1);

                adaptor.addChild(root_1, stream_assignExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {

                        	root_0 = (ASTNode)adaptor.nil();
                           
                            ASTNode root_1 = (ASTNode)adaptor.nil();
                            root_1 = (ASTNode)adaptor.becomeRoot(new Expression(EXPR, ((CommonToken)retval.start).getStartIndex(),input.toString(retval.start,input.LT(-1)).length(),"EXPRESSION"), root_1);

            		if (assignExpr != null)
            	                adaptor.addChild(root_1, (assignExpr!=null?((ASTNode)assignExpr.tree):null));

                            adaptor.addChild(root_0, root_1);

                        	retval.tree = root_0;
            	
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 30, expression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "expression"

    public static class assignmentExpression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignmentExpression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:527:1: assignmentExpression : logical_or_expression ( assignmentOperator assignmentExpression )? ;
    public final ReflexParser.assignmentExpression_return assignmentExpression() throws RecognitionException {
        ReflexParser.assignmentExpression_return retval = new ReflexParser.assignmentExpression_return();
        retval.start = input.LT(1);
        int assignmentExpression_StartIndex = input.index();
        ASTNode root_0 = null;

        ReflexParser.logical_or_expression_return logical_or_expression115 = null;

        ReflexParser.assignmentOperator_return assignmentOperator116 = null;

        ReflexParser.assignmentExpression_return assignmentExpression117 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 31) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:528:2: ( logical_or_expression ( assignmentOperator assignmentExpression )? )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:528:4: logical_or_expression ( assignmentOperator assignmentExpression )?
            {
            root_0 = (ASTNode)adaptor.nil();

            pushFollow(FOLLOW_logical_or_expression_in_assignmentExpression2240);
            logical_or_expression115=logical_or_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, logical_or_expression115.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:528:26: ( assignmentOperator assignmentExpression )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==ASSIGN||(LA30_0>=PLUSE_EQ && LA30_0<=RSHIFT_EQ)) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:528:27: assignmentOperator assignmentExpression
                    {
                    pushFollow(FOLLOW_assignmentOperator_in_assignmentExpression2243);
                    assignmentOperator116=assignmentOperator();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) root_0 = (ASTNode)adaptor.becomeRoot(assignmentOperator116.getTree(), root_0);
                    pushFollow(FOLLOW_assignmentExpression_in_assignmentExpression2246);
                    assignmentExpression117=assignmentExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignmentExpression117.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 31, assignmentExpression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "assignmentExpression"

    public static class assignmentOperator_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignmentOperator"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:531:1: assignmentOperator : ( '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|=' );
    public final ReflexParser.assignmentOperator_return assignmentOperator() throws RecognitionException {
        ReflexParser.assignmentOperator_return retval = new ReflexParser.assignmentOperator_return();
        retval.start = input.LT(1);
        int assignmentOperator_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken set118=null;

        ASTNode set118_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 32) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:532:2: ( '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|=' )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:
            {
            root_0 = (ASTNode)adaptor.nil();

            set118=(CommonToken)input.LT(1);
            if ( input.LA(1)==ASSIGN||(input.LA(1)>=PLUSE_EQ && input.LA(1)<=RSHIFT_EQ) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (ASTNode)adaptor.create(set118));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 32, assignmentOperator_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "assignmentOperator"

    public static class constantExpression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constantExpression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:546:1: constantExpression : logical_or_expression ;
    public final ReflexParser.constantExpression_return constantExpression() throws RecognitionException {
        ReflexParser.constantExpression_return retval = new ReflexParser.constantExpression_return();
        retval.start = input.LT(1);
        int constantExpression_StartIndex = input.index();
        ASTNode root_0 = null;

        ReflexParser.logical_or_expression_return logical_or_expression119 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 33) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:547:2: ( logical_or_expression )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:547:4: logical_or_expression
            {
            root_0 = (ASTNode)adaptor.nil();

            pushFollow(FOLLOW_logical_or_expression_in_constantExpression2322);
            logical_or_expression119=logical_or_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, logical_or_expression119.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 33, constantExpression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "constantExpression"

    public static class logical_or_expression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logical_or_expression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:550:1: logical_or_expression : logical_and_expression ( '||' logical_and_expression )* ;
    public final ReflexParser.logical_or_expression_return logical_or_expression() throws RecognitionException {
        ReflexParser.logical_or_expression_return retval = new ReflexParser.logical_or_expression_return();
        retval.start = input.LT(1);
        int logical_or_expression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken string_literal121=null;
        ReflexParser.logical_and_expression_return logical_and_expression120 = null;

        ReflexParser.logical_and_expression_return logical_and_expression122 = null;


        ASTNode string_literal121_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 34) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:551:2: ( logical_and_expression ( '||' logical_and_expression )* )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:551:4: logical_and_expression ( '||' logical_and_expression )*
            {
            root_0 = (ASTNode)adaptor.nil();

            pushFollow(FOLLOW_logical_and_expression_in_logical_or_expression2333);
            logical_and_expression120=logical_and_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, logical_and_expression120.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:551:27: ( '||' logical_and_expression )*
            loop31:
            do {
                int alt31=2;
                int LA31_0 = input.LA(1);

                if ( (LA31_0==LOGICAL_OR) ) {
                    alt31=1;
                }


                switch (alt31) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:551:28: '||' logical_and_expression
            	    {
            	    string_literal121=(CommonToken)match(input,LOGICAL_OR,FOLLOW_LOGICAL_OR_in_logical_or_expression2336); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    string_literal121_tree = (ASTNode)adaptor.create(string_literal121);
            	    root_0 = (ASTNode)adaptor.becomeRoot(string_literal121_tree, root_0);
            	    }
            	    pushFollow(FOLLOW_logical_and_expression_in_logical_or_expression2339);
            	    logical_and_expression122=logical_and_expression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, logical_and_expression122.getTree());

            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 34, logical_or_expression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "logical_or_expression"

    public static class logical_and_expression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logical_and_expression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:554:1: logical_and_expression : inclusive_or_expression ( '&&' inclusive_or_expression )* ;
    public final ReflexParser.logical_and_expression_return logical_and_expression() throws RecognitionException {
        ReflexParser.logical_and_expression_return retval = new ReflexParser.logical_and_expression_return();
        retval.start = input.LT(1);
        int logical_and_expression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken string_literal124=null;
        ReflexParser.inclusive_or_expression_return inclusive_or_expression123 = null;

        ReflexParser.inclusive_or_expression_return inclusive_or_expression125 = null;


        ASTNode string_literal124_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 35) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:555:2: ( inclusive_or_expression ( '&&' inclusive_or_expression )* )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:555:4: inclusive_or_expression ( '&&' inclusive_or_expression )*
            {
            root_0 = (ASTNode)adaptor.nil();

            pushFollow(FOLLOW_inclusive_or_expression_in_logical_and_expression2352);
            inclusive_or_expression123=inclusive_or_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, inclusive_or_expression123.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:555:28: ( '&&' inclusive_or_expression )*
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==LOGICAL_AND) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:555:29: '&&' inclusive_or_expression
            	    {
            	    string_literal124=(CommonToken)match(input,LOGICAL_AND,FOLLOW_LOGICAL_AND_in_logical_and_expression2355); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    string_literal124_tree = (ASTNode)adaptor.create(string_literal124);
            	    root_0 = (ASTNode)adaptor.becomeRoot(string_literal124_tree, root_0);
            	    }
            	    pushFollow(FOLLOW_inclusive_or_expression_in_logical_and_expression2358);
            	    inclusive_or_expression125=inclusive_or_expression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, inclusive_or_expression125.getTree());

            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 35, logical_and_expression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "logical_and_expression"

    public static class inclusive_or_expression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "inclusive_or_expression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:558:1: inclusive_or_expression : exclusive_or_expression ( '|' exclusive_or_expression )* ;
    public final ReflexParser.inclusive_or_expression_return inclusive_or_expression() throws RecognitionException {
        ReflexParser.inclusive_or_expression_return retval = new ReflexParser.inclusive_or_expression_return();
        retval.start = input.LT(1);
        int inclusive_or_expression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal127=null;
        ReflexParser.exclusive_or_expression_return exclusive_or_expression126 = null;

        ReflexParser.exclusive_or_expression_return exclusive_or_expression128 = null;


        ASTNode char_literal127_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 36) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:559:2: ( exclusive_or_expression ( '|' exclusive_or_expression )* )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:559:4: exclusive_or_expression ( '|' exclusive_or_expression )*
            {
            root_0 = (ASTNode)adaptor.nil();

            pushFollow(FOLLOW_exclusive_or_expression_in_inclusive_or_expression2371);
            exclusive_or_expression126=exclusive_or_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, exclusive_or_expression126.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:559:28: ( '|' exclusive_or_expression )*
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( (LA33_0==OR) ) {
                    alt33=1;
                }


                switch (alt33) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:559:29: '|' exclusive_or_expression
            	    {
            	    char_literal127=(CommonToken)match(input,OR,FOLLOW_OR_in_inclusive_or_expression2374); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    char_literal127_tree = (ASTNode)adaptor.create(char_literal127);
            	    root_0 = (ASTNode)adaptor.becomeRoot(char_literal127_tree, root_0);
            	    }
            	    pushFollow(FOLLOW_exclusive_or_expression_in_inclusive_or_expression2377);
            	    exclusive_or_expression128=exclusive_or_expression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, exclusive_or_expression128.getTree());

            	    }
            	    break;

            	default :
            	    break loop33;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 36, inclusive_or_expression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "inclusive_or_expression"

    public static class exclusive_or_expression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "exclusive_or_expression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:562:1: exclusive_or_expression : and_expression ( '^' and_expression )* ;
    public final ReflexParser.exclusive_or_expression_return exclusive_or_expression() throws RecognitionException {
        ReflexParser.exclusive_or_expression_return retval = new ReflexParser.exclusive_or_expression_return();
        retval.start = input.LT(1);
        int exclusive_or_expression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal130=null;
        ReflexParser.and_expression_return and_expression129 = null;

        ReflexParser.and_expression_return and_expression131 = null;


        ASTNode char_literal130_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 37) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:563:2: ( and_expression ( '^' and_expression )* )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:563:4: and_expression ( '^' and_expression )*
            {
            root_0 = (ASTNode)adaptor.nil();

            pushFollow(FOLLOW_and_expression_in_exclusive_or_expression2390);
            and_expression129=and_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, and_expression129.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:563:19: ( '^' and_expression )*
            loop34:
            do {
                int alt34=2;
                int LA34_0 = input.LA(1);

                if ( (LA34_0==MOD2) ) {
                    alt34=1;
                }


                switch (alt34) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:563:20: '^' and_expression
            	    {
            	    char_literal130=(CommonToken)match(input,MOD2,FOLLOW_MOD2_in_exclusive_or_expression2393); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    char_literal130_tree = (ASTNode)adaptor.create(char_literal130);
            	    root_0 = (ASTNode)adaptor.becomeRoot(char_literal130_tree, root_0);
            	    }
            	    pushFollow(FOLLOW_and_expression_in_exclusive_or_expression2396);
            	    and_expression131=and_expression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, and_expression131.getTree());

            	    }
            	    break;

            	default :
            	    break loop34;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 37, exclusive_or_expression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "exclusive_or_expression"

    public static class and_expression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "and_expression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:566:1: and_expression : equality_expression ( '&' equality_expression )* ;
    public final ReflexParser.and_expression_return and_expression() throws RecognitionException {
        ReflexParser.and_expression_return retval = new ReflexParser.and_expression_return();
        retval.start = input.LT(1);
        int and_expression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal133=null;
        ReflexParser.equality_expression_return equality_expression132 = null;

        ReflexParser.equality_expression_return equality_expression134 = null;


        ASTNode char_literal133_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 38) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:567:2: ( equality_expression ( '&' equality_expression )* )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:567:4: equality_expression ( '&' equality_expression )*
            {
            root_0 = (ASTNode)adaptor.nil();

            pushFollow(FOLLOW_equality_expression_in_and_expression2409);
            equality_expression132=equality_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, equality_expression132.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:567:24: ( '&' equality_expression )*
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==AND) ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:567:25: '&' equality_expression
            	    {
            	    char_literal133=(CommonToken)match(input,AND,FOLLOW_AND_in_and_expression2412); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    char_literal133_tree = (ASTNode)adaptor.create(char_literal133);
            	    root_0 = (ASTNode)adaptor.becomeRoot(char_literal133_tree, root_0);
            	    }
            	    pushFollow(FOLLOW_equality_expression_in_and_expression2415);
            	    equality_expression134=equality_expression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, equality_expression134.getTree());

            	    }
            	    break;

            	default :
            	    break loop35;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 38, and_expression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "and_expression"

    public static class equality_expression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "equality_expression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:569:1: equality_expression : relational_expression ( ( '==' | '!=' ) relational_expression )* ;
    public final ReflexParser.equality_expression_return equality_expression() throws RecognitionException {
        ReflexParser.equality_expression_return retval = new ReflexParser.equality_expression_return();
        retval.start = input.LT(1);
        int equality_expression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken set136=null;
        ReflexParser.relational_expression_return relational_expression135 = null;

        ReflexParser.relational_expression_return relational_expression137 = null;


        ASTNode set136_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 39) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:570:2: ( relational_expression ( ( '==' | '!=' ) relational_expression )* )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:570:4: relational_expression ( ( '==' | '!=' ) relational_expression )*
            {
            root_0 = (ASTNode)adaptor.nil();

            pushFollow(FOLLOW_relational_expression_in_equality_expression2427);
            relational_expression135=relational_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, relational_expression135.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:570:26: ( ( '==' | '!=' ) relational_expression )*
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( ((LA36_0>=EQ && LA36_0<=NOT_EQ)) ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:570:27: ( '==' | '!=' ) relational_expression
            	    {
            	    set136=(CommonToken)input.LT(1);
            	    set136=(CommonToken)input.LT(1);
            	    if ( (input.LA(1)>=EQ && input.LA(1)<=NOT_EQ) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) root_0 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(set136), root_0);
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_relational_expression_in_equality_expression2437);
            	    relational_expression137=relational_expression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, relational_expression137.getTree());

            	    }
            	    break;

            	default :
            	    break loop36;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 39, equality_expression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "equality_expression"

    public static class relational_expression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "relational_expression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:573:1: relational_expression : shift_expression ( ( '<' | '>' | '<=' | '>=' ) shift_expression )* ;
    public final ReflexParser.relational_expression_return relational_expression() throws RecognitionException {
        ReflexParser.relational_expression_return retval = new ReflexParser.relational_expression_return();
        retval.start = input.LT(1);
        int relational_expression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken set139=null;
        ReflexParser.shift_expression_return shift_expression138 = null;

        ReflexParser.shift_expression_return shift_expression140 = null;


        ASTNode set139_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 40) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:574:2: ( shift_expression ( ( '<' | '>' | '<=' | '>=' ) shift_expression )* )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:574:4: shift_expression ( ( '<' | '>' | '<=' | '>=' ) shift_expression )*
            {
            root_0 = (ASTNode)adaptor.nil();

            pushFollow(FOLLOW_shift_expression_in_relational_expression2450);
            shift_expression138=shift_expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, shift_expression138.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:574:21: ( ( '<' | '>' | '<=' | '>=' ) shift_expression )*
            loop37:
            do {
                int alt37=2;
                int LA37_0 = input.LA(1);

                if ( ((LA37_0>=GT && LA37_0<=LT)||(LA37_0>=132 && LA37_0<=133)) ) {
                    alt37=1;
                }


                switch (alt37) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:574:22: ( '<' | '>' | '<=' | '>=' ) shift_expression
            	    {
            	    set139=(CommonToken)input.LT(1);
            	    set139=(CommonToken)input.LT(1);
            	    if ( (input.LA(1)>=GT && input.LA(1)<=LT)||(input.LA(1)>=132 && input.LA(1)<=133) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) root_0 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(set139), root_0);
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_shift_expression_in_relational_expression2464);
            	    shift_expression140=shift_expression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, shift_expression140.getTree());

            	    }
            	    break;

            	default :
            	    break loop37;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 40, relational_expression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "relational_expression"

    public static class shift_expression_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shift_expression"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:577:1: shift_expression : additiveExpression ( ( '<<' | '>>' ) additiveExpression )* ;
    public final ReflexParser.shift_expression_return shift_expression() throws RecognitionException {
        ReflexParser.shift_expression_return retval = new ReflexParser.shift_expression_return();
        retval.start = input.LT(1);
        int shift_expression_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken set142=null;
        ReflexParser.additiveExpression_return additiveExpression141 = null;

        ReflexParser.additiveExpression_return additiveExpression143 = null;


        ASTNode set142_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 41) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:578:2: ( additiveExpression ( ( '<<' | '>>' ) additiveExpression )* )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:578:4: additiveExpression ( ( '<<' | '>>' ) additiveExpression )*
            {
            root_0 = (ASTNode)adaptor.nil();

            pushFollow(FOLLOW_additiveExpression_in_shift_expression2478);
            additiveExpression141=additiveExpression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, additiveExpression141.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:578:23: ( ( '<<' | '>>' ) additiveExpression )*
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( ((LA38_0>=134 && LA38_0<=135)) ) {
                    alt38=1;
                }


                switch (alt38) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:578:24: ( '<<' | '>>' ) additiveExpression
            	    {
            	    set142=(CommonToken)input.LT(1);
            	    set142=(CommonToken)input.LT(1);
            	    if ( (input.LA(1)>=134 && input.LA(1)<=135) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) root_0 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(set142), root_0);
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_additiveExpression_in_shift_expression2488);
            	    additiveExpression143=additiveExpression();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, additiveExpression143.getTree());

            	    }
            	    break;

            	default :
            	    break loop38;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 41, shift_expression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "shift_expression"

    public static class statement_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "statement"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:583:1: statement : ( labeledStatement | compoundStatement | expressionStatement | ifStatement | switchStatement | iterationStatement | jumpStatement | controlProcessStatement | passStatement | timeoutStatement );
    public final ReflexParser.statement_return statement() throws RecognitionException {
        ReflexParser.statement_return retval = new ReflexParser.statement_return();
        retval.start = input.LT(1);
        int statement_StartIndex = input.index();
        ASTNode root_0 = null;

        ReflexParser.labeledStatement_return labeledStatement144 = null;

        ReflexParser.compoundStatement_return compoundStatement145 = null;

        ReflexParser.expressionStatement_return expressionStatement146 = null;

        ReflexParser.ifStatement_return ifStatement147 = null;

        ReflexParser.switchStatement_return switchStatement148 = null;

        ReflexParser.iterationStatement_return iterationStatement149 = null;

        ReflexParser.jumpStatement_return jumpStatement150 = null;

        ReflexParser.controlProcessStatement_return controlProcessStatement151 = null;

        ReflexParser.passStatement_return passStatement152 = null;

        ReflexParser.timeoutStatement_return timeoutStatement153 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 42) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:584:2: ( labeledStatement | compoundStatement | expressionStatement | ifStatement | switchStatement | iterationStatement | jumpStatement | controlProcessStatement | passStatement | timeoutStatement )
            int alt39=10;
            alt39 = dfa39.predict(input);
            switch (alt39) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:584:4: labeledStatement
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_labeledStatement_in_statement2503);
                    labeledStatement144=labeledStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, labeledStatement144.getTree());

                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:585:4: compoundStatement
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_compoundStatement_in_statement2508);
                    compoundStatement145=compoundStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, compoundStatement145.getTree());

                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:586:4: expressionStatement
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_expressionStatement_in_statement2513);
                    expressionStatement146=expressionStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expressionStatement146.getTree());

                    }
                    break;
                case 4 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:587:4: ifStatement
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_ifStatement_in_statement2518);
                    ifStatement147=ifStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ifStatement147.getTree());

                    }
                    break;
                case 5 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:588:4: switchStatement
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_switchStatement_in_statement2523);
                    switchStatement148=switchStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, switchStatement148.getTree());

                    }
                    break;
                case 6 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:589:4: iterationStatement
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_iterationStatement_in_statement2528);
                    iterationStatement149=iterationStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, iterationStatement149.getTree());

                    }
                    break;
                case 7 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:590:4: jumpStatement
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_jumpStatement_in_statement2533);
                    jumpStatement150=jumpStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, jumpStatement150.getTree());

                    }
                    break;
                case 8 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:591:4: controlProcessStatement
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_controlProcessStatement_in_statement2538);
                    controlProcessStatement151=controlProcessStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, controlProcessStatement151.getTree());

                    }
                    break;
                case 9 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:592:4: passStatement
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_passStatement_in_statement2543);
                    passStatement152=passStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, passStatement152.getTree());

                    }
                    break;
                case 10 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:593:4: timeoutStatement
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_timeoutStatement_in_statement2548);
                    timeoutStatement153=timeoutStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, timeoutStatement153.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 42, statement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "statement"

    public static class labeledStatement_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "labeledStatement"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:596:1: labeledStatement : ( caseKey constantExpression ':' statement | defaultKey ':' statement );
    public final ReflexParser.labeledStatement_return labeledStatement() throws RecognitionException {
        ReflexParser.labeledStatement_return retval = new ReflexParser.labeledStatement_return();
        retval.start = input.LT(1);
        int labeledStatement_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal156=null;
        CommonToken char_literal159=null;
        ReflexParser.caseKey_return caseKey154 = null;

        ReflexParser.constantExpression_return constantExpression155 = null;

        ReflexParser.statement_return statement157 = null;

        ReflexParser.defaultKey_return defaultKey158 = null;

        ReflexParser.statement_return statement160 = null;


        ASTNode char_literal156_tree=null;
        ASTNode char_literal159_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 43) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:597:2: ( caseKey constantExpression ':' statement | defaultKey ':' statement )
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==CASE) ) {
                alt40=1;
            }
            else if ( (LA40_0==DEFAULT) ) {
                alt40=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }
            switch (alt40) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:597:4: caseKey constantExpression ':' statement
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_caseKey_in_labeledStatement2559);
                    caseKey154=caseKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) root_0 = (ASTNode)adaptor.becomeRoot(caseKey154.getTree(), root_0);
                    pushFollow(FOLLOW_constantExpression_in_labeledStatement2562);
                    constantExpression155=constantExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, constantExpression155.getTree());
                    char_literal156=(CommonToken)match(input,COLON,FOLLOW_COLON_in_labeledStatement2564); if (state.failed) return retval;
                    pushFollow(FOLLOW_statement_in_labeledStatement2567);
                    statement157=statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, statement157.getTree());

                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:598:4: defaultKey ':' statement
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_defaultKey_in_labeledStatement2572);
                    defaultKey158=defaultKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) root_0 = (ASTNode)adaptor.becomeRoot(defaultKey158.getTree(), root_0);
                    char_literal159=(CommonToken)match(input,COLON,FOLLOW_COLON_in_labeledStatement2575); if (state.failed) return retval;
                    pushFollow(FOLLOW_statement_in_labeledStatement2578);
                    statement160=statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, statement160.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 43, labeledStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "labeledStatement"

    public static class compoundStatement_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "compoundStatement"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:601:1: compoundStatement : '{' ( statement )* '}' -> ^( BLOCK[$start.getStartIndex(),$text.length(),\"BLOCK\"] ( statement )* ) ;
    public final ReflexParser.compoundStatement_return compoundStatement() throws RecognitionException {
        ReflexParser.compoundStatement_return retval = new ReflexParser.compoundStatement_return();
        retval.start = input.LT(1);
        int compoundStatement_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal161=null;
        CommonToken char_literal163=null;
        ReflexParser.statement_return statement162 = null;


        ASTNode char_literal161_tree=null;
        ASTNode char_literal163_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_statement=new RewriteRuleSubtreeStream(adaptor,"rule statement");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 44) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:602:2: ( '{' ( statement )* '}' -> ^( BLOCK[$start.getStartIndex(),$text.length(),\"BLOCK\"] ( statement )* ) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:602:4: '{' ( statement )* '}'
            {
            char_literal161=(CommonToken)match(input,LBRACE,FOLLOW_LBRACE_in_compoundStatement2589); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(char_literal161);

            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:602:8: ( statement )*
            loop41:
            do {
                int alt41=2;
                alt41 = dfa41.predict(input);
                switch (alt41) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:0:0: statement
            	    {
            	    pushFollow(FOLLOW_statement_in_compoundStatement2591);
            	    statement162=statement();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_statement.add(statement162.getTree());

            	    }
            	    break;

            	default :
            	    break loop41;
                }
            } while (true);

            char_literal163=(CommonToken)match(input,RBRACE,FOLLOW_RBRACE_in_compoundStatement2594); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(char_literal163);



            // AST REWRITE
            // elements: statement
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 603:2: -> ^( BLOCK[$start.getStartIndex(),$text.length(),\"BLOCK\"] ( statement )* )
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:603:5: ^( BLOCK[$start.getStartIndex(),$text.length(),\"BLOCK\"] ( statement )* )
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new Block(BLOCK, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "BLOCK"), root_1);

                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:603:68: ( statement )*
                while ( stream_statement.hasNext() ) {
                    adaptor.addChild(root_1, stream_statement.nextTree());

                }
                stream_statement.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 44, compoundStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "compoundStatement"

    public static class expressionStatement_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expressionStatement"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:610:1: expressionStatement : ( ';' | expression ';' );
    public final ReflexParser.expressionStatement_return expressionStatement() throws RecognitionException {
        ReflexParser.expressionStatement_return retval = new ReflexParser.expressionStatement_return();
        retval.start = input.LT(1);
        int expressionStatement_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal164=null;
        CommonToken char_literal166=null;
        ReflexParser.expression_return expression165 = null;


        ASTNode char_literal164_tree=null;
        ASTNode char_literal166_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 45) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:611:2: ( ';' | expression ';' )
            int alt42=2;
            alt42 = dfa42.predict(input);
            switch (alt42) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:611:4: ';'
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    char_literal164=(CommonToken)match(input,SEMI,FOLLOW_SEMI_in_expressionStatement2621); if (state.failed) return retval;

                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:612:4: expression ';'
                    {
                    root_0 = (ASTNode)adaptor.nil();

                    pushFollow(FOLLOW_expression_in_expressionStatement2627);
                    expression165=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expression165.getTree());
                    char_literal166=(CommonToken)match(input,SEMI,FOLLOW_SEMI_in_expressionStatement2629); if (state.failed) return retval;

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 45, expressionStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "expressionStatement"

    public static class ifStatement_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ifStatement"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:615:1: ifStatement : ifKey '(' expression ')' ifSt= statement ( ( elseKey )=> elseKey elseSt= statement )? -> ^( IF[$start.getStartIndex(),$text.length(),\"IF\"] expression $ifSt ( $elseSt)? ) ;
    public final ReflexParser.ifStatement_return ifStatement() throws RecognitionException {
        ReflexParser.ifStatement_return retval = new ReflexParser.ifStatement_return();
        retval.start = input.LT(1);
        int ifStatement_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal168=null;
        CommonToken char_literal170=null;
        ReflexParser.statement_return ifSt = null;

        ReflexParser.statement_return elseSt = null;

        ReflexParser.ifKey_return ifKey167 = null;

        ReflexParser.expression_return expression169 = null;

        ReflexParser.elseKey_return elseKey171 = null;


        ASTNode char_literal168_tree=null;
        ASTNode char_literal170_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_statement=new RewriteRuleSubtreeStream(adaptor,"rule statement");
        RewriteRuleSubtreeStream stream_ifKey=new RewriteRuleSubtreeStream(adaptor,"rule ifKey");
        RewriteRuleSubtreeStream stream_elseKey=new RewriteRuleSubtreeStream(adaptor,"rule elseKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 46) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:616:2: ( ifKey '(' expression ')' ifSt= statement ( ( elseKey )=> elseKey elseSt= statement )? -> ^( IF[$start.getStartIndex(),$text.length(),\"IF\"] expression $ifSt ( $elseSt)? ) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:616:4: ifKey '(' expression ')' ifSt= statement ( ( elseKey )=> elseKey elseSt= statement )?
            {
            pushFollow(FOLLOW_ifKey_in_ifStatement2641);
            ifKey167=ifKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ifKey.add(ifKey167.getTree());
            char_literal168=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_ifStatement2643); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(char_literal168);

            pushFollow(FOLLOW_expression_in_ifStatement2645);
            expression169=expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expression.add(expression169.getTree());
            char_literal170=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_ifStatement2647); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(char_literal170);

            pushFollow(FOLLOW_statement_in_ifStatement2651);
            ifSt=statement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_statement.add(ifSt.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:616:44: ( ( elseKey )=> elseKey elseSt= statement )?
            int alt43=2;
            alt43 = dfa43.predict(input);
            switch (alt43) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:616:45: ( elseKey )=> elseKey elseSt= statement
                    {
                    pushFollow(FOLLOW_elseKey_in_ifStatement2659);
                    elseKey171=elseKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_elseKey.add(elseKey171.getTree());
                    pushFollow(FOLLOW_statement_in_ifStatement2663);
                    elseSt=statement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_statement.add(elseSt.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: ifSt, elseSt, expression
            // token labels: 
            // rule labels: retval, ifSt, elseSt
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_ifSt=new RewriteRuleSubtreeStream(adaptor,"rule ifSt",ifSt!=null?ifSt.tree:null);
            RewriteRuleSubtreeStream stream_elseSt=new RewriteRuleSubtreeStream(adaptor,"rule elseSt",elseSt!=null?elseSt.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 617:2: -> ^( IF[$start.getStartIndex(),$text.length(),\"IF\"] expression $ifSt ( $elseSt)? )
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:617:5: ^( IF[$start.getStartIndex(),$text.length(),\"IF\"] expression $ifSt ( $elseSt)? )
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new IfStatement(IF, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "IF"), root_1);

                adaptor.addChild(root_1, stream_expression.nextTree());
                adaptor.addChild(root_1, stream_ifSt.nextTree());
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:617:84: ( $elseSt)?
                if ( stream_elseSt.hasNext() ) {
                    adaptor.addChild(root_1, stream_elseSt.nextTree());

                }
                stream_elseSt.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 46, ifStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "ifStatement"

    public static class switchStatement_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "switchStatement"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:621:1: switchStatement : switchKey '(' expr= expression ')' block= switchBlockLabels -> ^( SWITCH[$start.getStartIndex(),$text.length(),\"SWITCH\"] $expr $block) ;
    public final ReflexParser.switchStatement_return switchStatement() throws RecognitionException {
        ReflexParser.switchStatement_return retval = new ReflexParser.switchStatement_return();
        retval.start = input.LT(1);
        int switchStatement_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal173=null;
        CommonToken char_literal174=null;
        ReflexParser.expression_return expr = null;

        ReflexParser.switchBlockLabels_return block = null;

        ReflexParser.switchKey_return switchKey172 = null;


        ASTNode char_literal173_tree=null;
        ASTNode char_literal174_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_switchKey=new RewriteRuleSubtreeStream(adaptor,"rule switchKey");
        RewriteRuleSubtreeStream stream_switchBlockLabels=new RewriteRuleSubtreeStream(adaptor,"rule switchBlockLabels");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 47) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:622:2: ( switchKey '(' expr= expression ')' block= switchBlockLabels -> ^( SWITCH[$start.getStartIndex(),$text.length(),\"SWITCH\"] $expr $block) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:622:4: switchKey '(' expr= expression ')' block= switchBlockLabels
            {
            pushFollow(FOLLOW_switchKey_in_switchStatement2699);
            switchKey172=switchKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_switchKey.add(switchKey172.getTree());
            char_literal173=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_switchStatement2701); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(char_literal173);

            pushFollow(FOLLOW_expression_in_switchStatement2705);
            expr=expression();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expression.add(expr.getTree());
            char_literal174=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_switchStatement2707); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(char_literal174);

            pushFollow(FOLLOW_switchBlockLabels_in_switchStatement2711);
            block=switchBlockLabels();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_switchBlockLabels.add(block.getTree());


            // AST REWRITE
            // elements: expr, block
            // token labels: 
            // rule labels: retval, block, expr
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_block=new RewriteRuleSubtreeStream(adaptor,"rule block",block!=null?block.tree:null);
            RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr",expr!=null?expr.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 623:2: -> ^( SWITCH[$start.getStartIndex(),$text.length(),\"SWITCH\"] $expr $block)
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:623:5: ^( SWITCH[$start.getStartIndex(),$text.length(),\"SWITCH\"] $expr $block)
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new SwitchStatement(SWITCH, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "SWITCH"), root_1);

                adaptor.addChild(root_1, stream_expr.nextTree());
                adaptor.addChild(root_1, stream_block.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 47, switchStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "switchStatement"

    public static class switchBlockLabels_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "switchBlockLabels"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:626:1: switchBlockLabels : '{' ( labeledStatement )+ '}' ;
    public final ReflexParser.switchBlockLabels_return switchBlockLabels() throws RecognitionException {
        ReflexParser.switchBlockLabels_return retval = new ReflexParser.switchBlockLabels_return();
        retval.start = input.LT(1);
        int switchBlockLabels_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal175=null;
        CommonToken char_literal177=null;
        ReflexParser.labeledStatement_return labeledStatement176 = null;


        ASTNode char_literal175_tree=null;
        ASTNode char_literal177_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 48) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:627:2: ( '{' ( labeledStatement )+ '}' )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:627:4: '{' ( labeledStatement )+ '}'
            {
            root_0 = (ASTNode)adaptor.nil();

            char_literal175=(CommonToken)match(input,LBRACE,FOLLOW_LBRACE_in_switchBlockLabels2742); if (state.failed) return retval;
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:627:9: ( labeledStatement )+
            int cnt44=0;
            loop44:
            do {
                int alt44=2;
                int LA44_0 = input.LA(1);

                if ( ((LA44_0>=CASE && LA44_0<=DEFAULT)) ) {
                    alt44=1;
                }


                switch (alt44) {
            	case 1 :
            	    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:0:0: labeledStatement
            	    {
            	    pushFollow(FOLLOW_labeledStatement_in_switchBlockLabels2745);
            	    labeledStatement176=labeledStatement();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, labeledStatement176.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt44 >= 1 ) break loop44;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(44, input);
                        throw eee;
                }
                cnt44++;
            } while (true);

            char_literal177=(CommonToken)match(input,RBRACE,FOLLOW_RBRACE_in_switchBlockLabels2748); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 48, switchBlockLabels_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "switchBlockLabels"

    public static class iterationStatement_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "iterationStatement"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:630:1: iterationStatement : forKey '(' beginAction= expressionStatement condition= expressionStatement (contAction= expression )? ')' statement -> ^( ITERATION_STATEMENT $beginAction $condition $contAction statement ) ;
    public final ReflexParser.iterationStatement_return iterationStatement() throws RecognitionException {
        ReflexParser.iterationStatement_return retval = new ReflexParser.iterationStatement_return();
        retval.start = input.LT(1);
        int iterationStatement_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal179=null;
        CommonToken char_literal180=null;
        ReflexParser.expressionStatement_return beginAction = null;

        ReflexParser.expressionStatement_return condition = null;

        ReflexParser.expression_return contAction = null;

        ReflexParser.forKey_return forKey178 = null;

        ReflexParser.statement_return statement181 = null;


        ASTNode char_literal179_tree=null;
        ASTNode char_literal180_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_statement=new RewriteRuleSubtreeStream(adaptor,"rule statement");
        RewriteRuleSubtreeStream stream_expressionStatement=new RewriteRuleSubtreeStream(adaptor,"rule expressionStatement");
        RewriteRuleSubtreeStream stream_forKey=new RewriteRuleSubtreeStream(adaptor,"rule forKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 49) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:631:2: ( forKey '(' beginAction= expressionStatement condition= expressionStatement (contAction= expression )? ')' statement -> ^( ITERATION_STATEMENT $beginAction $condition $contAction statement ) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:633:2: forKey '(' beginAction= expressionStatement condition= expressionStatement (contAction= expression )? ')' statement
            {
            pushFollow(FOLLOW_forKey_in_iterationStatement2765);
            forKey178=forKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_forKey.add(forKey178.getTree());
            char_literal179=(CommonToken)match(input,LPAREN,FOLLOW_LPAREN_in_iterationStatement2767); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(char_literal179);

            pushFollow(FOLLOW_expressionStatement_in_iterationStatement2771);
            beginAction=expressionStatement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expressionStatement.add(beginAction.getTree());
            pushFollow(FOLLOW_expressionStatement_in_iterationStatement2775);
            condition=expressionStatement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expressionStatement.add(condition.getTree());
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:633:85: (contAction= expression )?
            int alt45=2;
            alt45 = dfa45.predict(input);
            switch (alt45) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:0:0: contAction= expression
                    {
                    pushFollow(FOLLOW_expression_in_iterationStatement2779);
                    contAction=expression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_expression.add(contAction.getTree());

                    }
                    break;

            }

            char_literal180=(CommonToken)match(input,RPAREN,FOLLOW_RPAREN_in_iterationStatement2782); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(char_literal180);

            pushFollow(FOLLOW_statement_in_iterationStatement2784);
            statement181=statement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_statement.add(statement181.getTree());


            // AST REWRITE
            // elements: statement, contAction, condition, beginAction
            // token labels: 
            // rule labels: retval, condition, contAction, beginAction
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_condition=new RewriteRuleSubtreeStream(adaptor,"rule condition",condition!=null?condition.tree:null);
            RewriteRuleSubtreeStream stream_contAction=new RewriteRuleSubtreeStream(adaptor,"rule contAction",contAction!=null?contAction.tree:null);
            RewriteRuleSubtreeStream stream_beginAction=new RewriteRuleSubtreeStream(adaptor,"rule beginAction",beginAction!=null?beginAction.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 634:2: -> ^( ITERATION_STATEMENT $beginAction $condition $contAction statement )
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:634:5: ^( ITERATION_STATEMENT $beginAction $condition $contAction statement )
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot((ASTNode)adaptor.create(ITERATION_STATEMENT, "ITERATION_STATEMENT"), root_1);

                adaptor.addChild(root_1, stream_beginAction.nextTree());
                adaptor.addChild(root_1, stream_condition.nextTree());
                adaptor.addChild(root_1, stream_contAction.nextTree());
                adaptor.addChild(root_1, stream_statement.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 49, iterationStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "iterationStatement"

    public static class jumpStatement_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "jumpStatement"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:637:1: jumpStatement : ( contKey -> CONTINUE[$start.getStartIndex(),$text.length(),\"CONTINUE\"] | breakKey -> BREAK[$start.getStartIndex(),$text.length(),\"BREAK\"] ) ';' ;
    public final ReflexParser.jumpStatement_return jumpStatement() throws RecognitionException {
        ReflexParser.jumpStatement_return retval = new ReflexParser.jumpStatement_return();
        retval.start = input.LT(1);
        int jumpStatement_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken char_literal184=null;
        ReflexParser.contKey_return contKey182 = null;

        ReflexParser.breakKey_return breakKey183 = null;


        ASTNode char_literal184_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_breakKey=new RewriteRuleSubtreeStream(adaptor,"rule breakKey");
        RewriteRuleSubtreeStream stream_contKey=new RewriteRuleSubtreeStream(adaptor,"rule contKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 50) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:638:2: ( ( contKey -> CONTINUE[$start.getStartIndex(),$text.length(),\"CONTINUE\"] | breakKey -> BREAK[$start.getStartIndex(),$text.length(),\"BREAK\"] ) ';' )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:638:4: ( contKey -> CONTINUE[$start.getStartIndex(),$text.length(),\"CONTINUE\"] | breakKey -> BREAK[$start.getStartIndex(),$text.length(),\"BREAK\"] ) ';'
            {
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:638:4: ( contKey -> CONTINUE[$start.getStartIndex(),$text.length(),\"CONTINUE\"] | breakKey -> BREAK[$start.getStartIndex(),$text.length(),\"BREAK\"] )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==CONT) ) {
                alt46=1;
            }
            else if ( (LA46_0==BREAK) ) {
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
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:638:5: contKey
                    {
                    pushFollow(FOLLOW_contKey_in_jumpStatement2814);
                    contKey182=contKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_contKey.add(contKey182.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 638:13: -> CONTINUE[$start.getStartIndex(),$text.length(),\"CONTINUE\"]
                    {
                        adaptor.addChild(root_0, new JumpStatement(CONTINUE, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "CONTINUE"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:639:4: breakKey
                    {
                    pushFollow(FOLLOW_breakKey_in_jumpStatement2827);
                    breakKey183=breakKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_breakKey.add(breakKey183.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 639:13: -> BREAK[$start.getStartIndex(),$text.length(),\"BREAK\"]
                    {
                        adaptor.addChild(root_0, new JumpStatement(BREAK, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "BREAK"));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }

            char_literal184=(CommonToken)match(input,SEMI,FOLLOW_SEMI_in_jumpStatement2841); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SEMI.add(char_literal184);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 50, jumpStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "jumpStatement"

    public static class controlProcessStatement_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "controlProcessStatement"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:644:1: controlProcessStatement : ( ( stopKey ( procKey id1= IDENTIFIER -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] PROCESS_NAME[$id1.getStartIndex(),$id1.text.length(),$id1] ) | -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] ) ) | errorKey ( procKey id2= IDENTIFIER -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] PROCESS_NAME[$id2.getStartIndex(),$id2.text.length(),$id2] ) | -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] ) ) ) | ( startKey procKey id3= IDENTIFIER -> ^( START[$start.getStartIndex(),$text.length(),\"START\"] PROCESS_NAME[$id3.getStartIndex(),$id3.text.length(),$id3] ) | contKey procKey id4= IDENTIFIER -> ^( CONT[$start.getStartIndex(),$text.length(),\"CONT\"] PROCESS_NAME[$id4.getStartIndex(),$id4.text.length(),$id4] ) ) ) ';' ;
    public final ReflexParser.controlProcessStatement_return controlProcessStatement() throws RecognitionException {
        ReflexParser.controlProcessStatement_return retval = new ReflexParser.controlProcessStatement_return();
        retval.start = input.LT(1);
        int controlProcessStatement_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken id1=null;
        CommonToken id2=null;
        CommonToken id3=null;
        CommonToken id4=null;
        CommonToken char_literal193=null;
        ReflexParser.stopKey_return stopKey185 = null;

        ReflexParser.procKey_return procKey186 = null;

        ReflexParser.errorKey_return errorKey187 = null;

        ReflexParser.procKey_return procKey188 = null;

        ReflexParser.startKey_return startKey189 = null;

        ReflexParser.procKey_return procKey190 = null;

        ReflexParser.contKey_return contKey191 = null;

        ReflexParser.procKey_return procKey192 = null;


        ASTNode id1_tree=null;
        ASTNode id2_tree=null;
        ASTNode id3_tree=null;
        ASTNode id4_tree=null;
        ASTNode char_literal193_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleSubtreeStream stream_stopKey=new RewriteRuleSubtreeStream(adaptor,"rule stopKey");
        RewriteRuleSubtreeStream stream_procKey=new RewriteRuleSubtreeStream(adaptor,"rule procKey");
        RewriteRuleSubtreeStream stream_errorKey=new RewriteRuleSubtreeStream(adaptor,"rule errorKey");
        RewriteRuleSubtreeStream stream_contKey=new RewriteRuleSubtreeStream(adaptor,"rule contKey");
        RewriteRuleSubtreeStream stream_startKey=new RewriteRuleSubtreeStream(adaptor,"rule startKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 51) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:645:2: ( ( ( stopKey ( procKey id1= IDENTIFIER -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] PROCESS_NAME[$id1.getStartIndex(),$id1.text.length(),$id1] ) | -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] ) ) | errorKey ( procKey id2= IDENTIFIER -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] PROCESS_NAME[$id2.getStartIndex(),$id2.text.length(),$id2] ) | -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] ) ) ) | ( startKey procKey id3= IDENTIFIER -> ^( START[$start.getStartIndex(),$text.length(),\"START\"] PROCESS_NAME[$id3.getStartIndex(),$id3.text.length(),$id3] ) | contKey procKey id4= IDENTIFIER -> ^( CONT[$start.getStartIndex(),$text.length(),\"CONT\"] PROCESS_NAME[$id4.getStartIndex(),$id4.text.length(),$id4] ) ) ) ';' )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:646:2: ( ( stopKey ( procKey id1= IDENTIFIER -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] PROCESS_NAME[$id1.getStartIndex(),$id1.text.length(),$id1] ) | -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] ) ) | errorKey ( procKey id2= IDENTIFIER -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] PROCESS_NAME[$id2.getStartIndex(),$id2.text.length(),$id2] ) | -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] ) ) ) | ( startKey procKey id3= IDENTIFIER -> ^( START[$start.getStartIndex(),$text.length(),\"START\"] PROCESS_NAME[$id3.getStartIndex(),$id3.text.length(),$id3] ) | contKey procKey id4= IDENTIFIER -> ^( CONT[$start.getStartIndex(),$text.length(),\"CONT\"] PROCESS_NAME[$id4.getStartIndex(),$id4.text.length(),$id4] ) ) ) ';'
            {
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:646:2: ( ( stopKey ( procKey id1= IDENTIFIER -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] PROCESS_NAME[$id1.getStartIndex(),$id1.text.length(),$id1] ) | -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] ) ) | errorKey ( procKey id2= IDENTIFIER -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] PROCESS_NAME[$id2.getStartIndex(),$id2.text.length(),$id2] ) | -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] ) ) ) | ( startKey procKey id3= IDENTIFIER -> ^( START[$start.getStartIndex(),$text.length(),\"START\"] PROCESS_NAME[$id3.getStartIndex(),$id3.text.length(),$id3] ) | contKey procKey id4= IDENTIFIER -> ^( CONT[$start.getStartIndex(),$text.length(),\"CONT\"] PROCESS_NAME[$id4.getStartIndex(),$id4.text.length(),$id4] ) ) )
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( ((LA51_0>=STOP && LA51_0<=ERROR)) ) {
                alt51=1;
            }
            else if ( ((LA51_0>=START && LA51_0<=CONT)) ) {
                alt51=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                throw nvae;
            }
            switch (alt51) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:646:3: ( stopKey ( procKey id1= IDENTIFIER -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] PROCESS_NAME[$id1.getStartIndex(),$id1.text.length(),$id1] ) | -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] ) ) | errorKey ( procKey id2= IDENTIFIER -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] PROCESS_NAME[$id2.getStartIndex(),$id2.text.length(),$id2] ) | -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] ) ) )
                    {
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:646:3: ( stopKey ( procKey id1= IDENTIFIER -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] PROCESS_NAME[$id1.getStartIndex(),$id1.text.length(),$id1] ) | -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] ) ) | errorKey ( procKey id2= IDENTIFIER -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] PROCESS_NAME[$id2.getStartIndex(),$id2.text.length(),$id2] ) | -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] ) ) )
                    int alt49=2;
                    int LA49_0 = input.LA(1);

                    if ( (LA49_0==STOP) ) {
                        alt49=1;
                    }
                    else if ( (LA49_0==ERROR) ) {
                        alt49=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 49, 0, input);

                        throw nvae;
                    }
                    switch (alt49) {
                        case 1 :
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:646:4: stopKey ( procKey id1= IDENTIFIER -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] PROCESS_NAME[$id1.getStartIndex(),$id1.text.length(),$id1] ) | -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] ) )
                            {
                            pushFollow(FOLLOW_stopKey_in_controlProcessStatement2856);
                            stopKey185=stopKey();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_stopKey.add(stopKey185.getTree());
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:646:13: ( procKey id1= IDENTIFIER -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] PROCESS_NAME[$id1.getStartIndex(),$id1.text.length(),$id1] ) | -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] ) )
                            int alt47=2;
                            int LA47_0 = input.LA(1);

                            if ( (LA47_0==PROC) ) {
                                alt47=1;
                            }
                            else if ( (LA47_0==SEMI) ) {
                                alt47=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 47, 0, input);

                                throw nvae;
                            }
                            switch (alt47) {
                                case 1 :
                                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:646:14: procKey id1= IDENTIFIER
                                    {
                                    pushFollow(FOLLOW_procKey_in_controlProcessStatement2860);
                                    procKey186=procKey();

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) stream_procKey.add(procKey186.getTree());
                                    id1=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_controlProcessStatement2864); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_IDENTIFIER.add(id1);



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

                                    root_0 = (ASTNode)adaptor.nil();
                                    // 646:38: -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] PROCESS_NAME[$id1.getStartIndex(),$id1.text.length(),$id1] )
                                    {
                                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:646:41: ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] PROCESS_NAME[$id1.getStartIndex(),$id1.text.length(),$id1] )
                                        {
                                        ASTNode root_1 = (ASTNode)adaptor.nil();
                                        root_1 = (ASTNode)adaptor.becomeRoot(new ControlProcessStatement(STOP, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "STOP"), root_1);

                                        adaptor.addChild(root_1, new Identifier(PROCESS_NAME, id1.getStartIndex(), (id1!=null?id1.getText():null).length(), id1));

                                        adaptor.addChild(root_0, root_1);
                                        }

                                    }

                                    retval.tree = root_0;}
                                    }
                                    break;
                                case 2 :
                                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:648:12: 
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

                                    root_0 = (ASTNode)adaptor.nil();
                                    // 648:12: -> ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] )
                                    {
                                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:648:15: ^( STOP[$start.getStartIndex(),$text.length(),\"STOP\"] )
                                        {
                                        ASTNode root_1 = (ASTNode)adaptor.nil();
                                        root_1 = (ASTNode)adaptor.becomeRoot(new ControlProcessStatement(STOP, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "STOP"), root_1);

                                        adaptor.addChild(root_0, root_1);
                                        }

                                    }

                                    retval.tree = root_0;}
                                    }
                                    break;

                            }


                            }
                            break;
                        case 2 :
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:650:4: errorKey ( procKey id2= IDENTIFIER -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] PROCESS_NAME[$id2.getStartIndex(),$id2.text.length(),$id2] ) | -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] ) )
                            {
                            pushFollow(FOLLOW_errorKey_in_controlProcessStatement2928);
                            errorKey187=errorKey();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_errorKey.add(errorKey187.getTree());
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:650:13: ( procKey id2= IDENTIFIER -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] PROCESS_NAME[$id2.getStartIndex(),$id2.text.length(),$id2] ) | -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] ) )
                            int alt48=2;
                            int LA48_0 = input.LA(1);

                            if ( (LA48_0==PROC) ) {
                                alt48=1;
                            }
                            else if ( (LA48_0==SEMI) ) {
                                alt48=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 48, 0, input);

                                throw nvae;
                            }
                            switch (alt48) {
                                case 1 :
                                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:650:14: procKey id2= IDENTIFIER
                                    {
                                    pushFollow(FOLLOW_procKey_in_controlProcessStatement2931);
                                    procKey188=procKey();

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) stream_procKey.add(procKey188.getTree());
                                    id2=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_controlProcessStatement2935); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_IDENTIFIER.add(id2);



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

                                    root_0 = (ASTNode)adaptor.nil();
                                    // 650:37: -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] PROCESS_NAME[$id2.getStartIndex(),$id2.text.length(),$id2] )
                                    {
                                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:650:40: ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] PROCESS_NAME[$id2.getStartIndex(),$id2.text.length(),$id2] )
                                        {
                                        ASTNode root_1 = (ASTNode)adaptor.nil();
                                        root_1 = (ASTNode)adaptor.becomeRoot(new ControlProcessStatement(ERROR, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "ERROR"), root_1);

                                        adaptor.addChild(root_1, new Identifier(PROCESS_NAME, id2.getStartIndex(), (id2!=null?id2.getText():null).length(), id2));

                                        adaptor.addChild(root_0, root_1);
                                        }

                                    }

                                    retval.tree = root_0;}
                                    }
                                    break;
                                case 2 :
                                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:652:12: 
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

                                    root_0 = (ASTNode)adaptor.nil();
                                    // 652:12: -> ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] )
                                    {
                                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:652:15: ^( ERROR[$start.getStartIndex(),$text.length(),\"ERROR\"] )
                                        {
                                        ASTNode root_1 = (ASTNode)adaptor.nil();
                                        root_1 = (ASTNode)adaptor.becomeRoot(new ControlProcessStatement(ERROR, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "ERROR"), root_1);

                                        adaptor.addChild(root_0, root_1);
                                        }

                                    }

                                    retval.tree = root_0;}
                                    }
                                    break;

                            }


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:655:3: ( startKey procKey id3= IDENTIFIER -> ^( START[$start.getStartIndex(),$text.length(),\"START\"] PROCESS_NAME[$id3.getStartIndex(),$id3.text.length(),$id3] ) | contKey procKey id4= IDENTIFIER -> ^( CONT[$start.getStartIndex(),$text.length(),\"CONT\"] PROCESS_NAME[$id4.getStartIndex(),$id4.text.length(),$id4] ) )
                    {
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:655:3: ( startKey procKey id3= IDENTIFIER -> ^( START[$start.getStartIndex(),$text.length(),\"START\"] PROCESS_NAME[$id3.getStartIndex(),$id3.text.length(),$id3] ) | contKey procKey id4= IDENTIFIER -> ^( CONT[$start.getStartIndex(),$text.length(),\"CONT\"] PROCESS_NAME[$id4.getStartIndex(),$id4.text.length(),$id4] ) )
                    int alt50=2;
                    int LA50_0 = input.LA(1);

                    if ( (LA50_0==START) ) {
                        alt50=1;
                    }
                    else if ( (LA50_0==CONT) ) {
                        alt50=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 50, 0, input);

                        throw nvae;
                    }
                    switch (alt50) {
                        case 1 :
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:655:5: startKey procKey id3= IDENTIFIER
                            {
                            pushFollow(FOLLOW_startKey_in_controlProcessStatement3003);
                            startKey189=startKey();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_startKey.add(startKey189.getTree());
                            pushFollow(FOLLOW_procKey_in_controlProcessStatement3005);
                            procKey190=procKey();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_procKey.add(procKey190.getTree());
                            id3=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_controlProcessStatement3009); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_IDENTIFIER.add(id3);



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

                            root_0 = (ASTNode)adaptor.nil();
                            // 655:37: -> ^( START[$start.getStartIndex(),$text.length(),\"START\"] PROCESS_NAME[$id3.getStartIndex(),$id3.text.length(),$id3] )
                            {
                                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:655:40: ^( START[$start.getStartIndex(),$text.length(),\"START\"] PROCESS_NAME[$id3.getStartIndex(),$id3.text.length(),$id3] )
                                {
                                ASTNode root_1 = (ASTNode)adaptor.nil();
                                root_1 = (ASTNode)adaptor.becomeRoot(new ControlProcessStatement(START, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "START"), root_1);

                                adaptor.addChild(root_1, new Identifier(PROCESS_NAME, id3.getStartIndex(), (id3!=null?id3.getText():null).length(), id3));

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:657:4: contKey procKey id4= IDENTIFIER
                            {
                            pushFollow(FOLLOW_contKey_in_controlProcessStatement3041);
                            contKey191=contKey();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_contKey.add(contKey191.getTree());
                            pushFollow(FOLLOW_procKey_in_controlProcessStatement3043);
                            procKey192=procKey();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_procKey.add(procKey192.getTree());
                            id4=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_controlProcessStatement3047); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_IDENTIFIER.add(id4);



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

                            root_0 = (ASTNode)adaptor.nil();
                            // 657:35: -> ^( CONT[$start.getStartIndex(),$text.length(),\"CONT\"] PROCESS_NAME[$id4.getStartIndex(),$id4.text.length(),$id4] )
                            {
                                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:657:38: ^( CONT[$start.getStartIndex(),$text.length(),\"CONT\"] PROCESS_NAME[$id4.getStartIndex(),$id4.text.length(),$id4] )
                                {
                                ASTNode root_1 = (ASTNode)adaptor.nil();
                                root_1 = (ASTNode)adaptor.becomeRoot(new ControlProcessStatement(CONT, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "CONT"), root_1);

                                adaptor.addChild(root_1, new Identifier(PROCESS_NAME, id4.getStartIndex(), (id4!=null?id4.getText():null).length(), id4));

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }
                    break;

            }

            char_literal193=(CommonToken)match(input,SEMI,FOLLOW_SEMI_in_controlProcessStatement3084); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SEMI.add(char_literal193);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {

            		root_0 = (ASTNode)adaptor.nil();
            		ASTNode root_1 = (ASTNode)adaptor.nil();
            		
            		switch(((CommonToken)retval.start).getType())
                       	{
            			case STOP: 
            				{
            				root_1 = (ASTNode)adaptor.becomeRoot(
            					new ControlProcessStatement(STOP, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "STOP"), root_1);
            				if (id1 != null)
            					adaptor.addChild(root_1, new Identifier(PROCESS_NAME, id1.getStartIndex(), (id1!=null?id1.getText():null).length(), id1));	
            				}
            				break;
            			case ERROR: 
            				{
            				root_1 = (ASTNode)adaptor.becomeRoot(
            					new ControlProcessStatement(ERROR, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "ERROR"), root_1);
            				if (id2 != null)
            					adaptor.addChild(root_1, new Identifier(PROCESS_NAME, id2.getStartIndex(), (id2!=null?id2.getText():null).length(), id2));	
            				}
            				break;
            			case START: 
            				{
            				root_1 = (ASTNode)adaptor.becomeRoot(
            					new ControlProcessStatement(START, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "START"), root_1);
            				if (id3 != null)
            					adaptor.addChild(root_1, new Identifier(PROCESS_NAME, id3.getStartIndex(), (id3!=null?id3.getText():null).length(), id3));	
            				}
            				break;
            			case CONT: 
            				{
            				root_1 = (ASTNode)adaptor.becomeRoot(
            					new ControlProcessStatement(CONT, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "CONT"), root_1);
            				if (id4 != null)
            					adaptor.addChild(root_1, new Identifier(PROCESS_NAME, id4.getStartIndex(), (id4!=null?id4.getText():null).length(), id4));	
            				}
            				break;
                       	}
                       	adaptor.addChild(root_0, root_1);
                       	retval.tree = root_0;           	
            	
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 51, controlProcessStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "controlProcessStatement"

    public static class passStatement_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "passStatement"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:711:1: passStatement : ( ( inKey ( stateKey id= IDENTIFIER -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_STATE\"] STATE_NAME[$id.getStartIndex(),$id.text.length(),$id] ) | nextKey -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_NEXT\"] ) ) ) | loopKey -> ^( LOOP[$start.getStartIndex(),$text.length(),\"LOOP\"] ) ) ';' ;
    public final ReflexParser.passStatement_return passStatement() throws RecognitionException {
        ReflexParser.passStatement_return retval = new ReflexParser.passStatement_return();
        retval.start = input.LT(1);
        int passStatement_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken id=null;
        CommonToken char_literal198=null;
        ReflexParser.inKey_return inKey194 = null;

        ReflexParser.stateKey_return stateKey195 = null;

        ReflexParser.nextKey_return nextKey196 = null;

        ReflexParser.loopKey_return loopKey197 = null;


        ASTNode id_tree=null;
        ASTNode char_literal198_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleTokenStream stream_IDENTIFIER=new RewriteRuleTokenStream(adaptor,"token IDENTIFIER");
        RewriteRuleSubtreeStream stream_inKey=new RewriteRuleSubtreeStream(adaptor,"rule inKey");
        RewriteRuleSubtreeStream stream_nextKey=new RewriteRuleSubtreeStream(adaptor,"rule nextKey");
        RewriteRuleSubtreeStream stream_stateKey=new RewriteRuleSubtreeStream(adaptor,"rule stateKey");
        RewriteRuleSubtreeStream stream_loopKey=new RewriteRuleSubtreeStream(adaptor,"rule loopKey");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 52) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:712:2: ( ( ( inKey ( stateKey id= IDENTIFIER -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_STATE\"] STATE_NAME[$id.getStartIndex(),$id.text.length(),$id] ) | nextKey -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_NEXT\"] ) ) ) | loopKey -> ^( LOOP[$start.getStartIndex(),$text.length(),\"LOOP\"] ) ) ';' )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:713:2: ( ( inKey ( stateKey id= IDENTIFIER -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_STATE\"] STATE_NAME[$id.getStartIndex(),$id.text.length(),$id] ) | nextKey -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_NEXT\"] ) ) ) | loopKey -> ^( LOOP[$start.getStartIndex(),$text.length(),\"LOOP\"] ) ) ';'
            {
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:713:2: ( ( inKey ( stateKey id= IDENTIFIER -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_STATE\"] STATE_NAME[$id.getStartIndex(),$id.text.length(),$id] ) | nextKey -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_NEXT\"] ) ) ) | loopKey -> ^( LOOP[$start.getStartIndex(),$text.length(),\"LOOP\"] ) )
            int alt53=2;
            int LA53_0 = input.LA(1);

            if ( (LA53_0==IN) ) {
                alt53=1;
            }
            else if ( (LA53_0==LOOP) ) {
                alt53=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;
            }
            switch (alt53) {
                case 1 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:714:4: ( inKey ( stateKey id= IDENTIFIER -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_STATE\"] STATE_NAME[$id.getStartIndex(),$id.text.length(),$id] ) | nextKey -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_NEXT\"] ) ) )
                    {
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:714:4: ( inKey ( stateKey id= IDENTIFIER -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_STATE\"] STATE_NAME[$id.getStartIndex(),$id.text.length(),$id] ) | nextKey -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_NEXT\"] ) ) )
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:714:6: inKey ( stateKey id= IDENTIFIER -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_STATE\"] STATE_NAME[$id.getStartIndex(),$id.text.length(),$id] ) | nextKey -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_NEXT\"] ) )
                    {
                    pushFollow(FOLLOW_inKey_in_passStatement3115);
                    inKey194=inKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_inKey.add(inKey194.getTree());
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:715:6: ( stateKey id= IDENTIFIER -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_STATE\"] STATE_NAME[$id.getStartIndex(),$id.text.length(),$id] ) | nextKey -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_NEXT\"] ) )
                    int alt52=2;
                    int LA52_0 = input.LA(1);

                    if ( (LA52_0==STATE) ) {
                        alt52=1;
                    }
                    else if ( (LA52_0==NEXT) ) {
                        alt52=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 52, 0, input);

                        throw nvae;
                    }
                    switch (alt52) {
                        case 1 :
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:715:7: stateKey id= IDENTIFIER
                            {
                            pushFollow(FOLLOW_stateKey_in_passStatement3123);
                            stateKey195=stateKey();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_stateKey.add(stateKey195.getTree());
                            id=(CommonToken)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_passStatement3127); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_IDENTIFIER.add(id);



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

                            root_0 = (ASTNode)adaptor.nil();
                            // 715:30: -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_STATE\"] STATE_NAME[$id.getStartIndex(),$id.text.length(),$id] )
                            {
                                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:715:33: ^( IN[$start.getStartIndex(),$text.length(),\"IN_STATE\"] STATE_NAME[$id.getStartIndex(),$id.text.length(),$id] )
                                {
                                ASTNode root_1 = (ASTNode)adaptor.nil();
                                root_1 = (ASTNode)adaptor.becomeRoot(new PassStatement(IN, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "IN_STATE"), root_1);

                                adaptor.addChild(root_1, new Identifier(STATE_NAME, id.getStartIndex(), (id!=null?id.getText():null).length(), id));

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:717:8: nextKey
                            {
                            pushFollow(FOLLOW_nextKey_in_passStatement3163);
                            nextKey196=nextKey();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_nextKey.add(nextKey196.getTree());


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

                            root_0 = (ASTNode)adaptor.nil();
                            // 717:18: -> ^( IN[$start.getStartIndex(),$text.length(),\"IN_NEXT\"] )
                            {
                                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:717:21: ^( IN[$start.getStartIndex(),$text.length(),\"IN_NEXT\"] )
                                {
                                ASTNode root_1 = (ASTNode)adaptor.nil();
                                root_1 = (ASTNode)adaptor.becomeRoot(new PassStatement(IN, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "IN_NEXT"), root_1);

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:720:6: loopKey
                    {
                    pushFollow(FOLLOW_loopKey_in_passStatement3196);
                    loopKey197=loopKey();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_loopKey.add(loopKey197.getTree());


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

                    root_0 = (ASTNode)adaptor.nil();
                    // 720:16: -> ^( LOOP[$start.getStartIndex(),$text.length(),\"LOOP\"] )
                    {
                        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:720:19: ^( LOOP[$start.getStartIndex(),$text.length(),\"LOOP\"] )
                        {
                        ASTNode root_1 = (ASTNode)adaptor.nil();
                        root_1 = (ASTNode)adaptor.becomeRoot(new PassStatement(LOOP, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "LOOP"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }

            char_literal198=(CommonToken)match(input,SEMI,FOLLOW_SEMI_in_passStatement3215); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_SEMI.add(char_literal198);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {

            		root_0 = (ASTNode)adaptor.nil();
            		ASTNode root_1 = (ASTNode)adaptor.nil();
            		
            		switch(((CommonToken)retval.start).getType())
                       	{
            			case IN: 
            				{
            				root_1 = (ASTNode)adaptor.becomeRoot(
            					new PassStatement(IN, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "IN"), root_1);
            				if (id != null)
            					adaptor.addChild(root_1, new Identifier(STATE_NAME, id.getStartIndex(), (id!=null?id.getText():null).length(), id));	
            				}
            				break;
            			case LOOP: 
            				{
            				root_1 = (ASTNode)adaptor.becomeRoot(
            					new PassStatement(LOOP, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "LOOP"), root_1);
            				}
            				break;
                       	}
                       	adaptor.addChild(root_0, root_1);
                       	retval.tree = root_0;           	
            	
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 52, passStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "passStatement"

    public static class timeoutStatement_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "timeoutStatement"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:750:1: timeoutStatement : timeoutKey time= intLiteralOrConst stmt= statement -> ^( TIMEOUT[$start.getStartIndex(),$text.length(),\"TIMEOUT\"] $time $stmt) ;
    public final ReflexParser.timeoutStatement_return timeoutStatement() throws RecognitionException {
        ReflexParser.timeoutStatement_return retval = new ReflexParser.timeoutStatement_return();
        retval.start = input.LT(1);
        int timeoutStatement_StartIndex = input.index();
        ASTNode root_0 = null;

        ReflexParser.intLiteralOrConst_return time = null;

        ReflexParser.statement_return stmt = null;

        ReflexParser.timeoutKey_return timeoutKey199 = null;


        RewriteRuleSubtreeStream stream_statement=new RewriteRuleSubtreeStream(adaptor,"rule statement");
        RewriteRuleSubtreeStream stream_timeoutKey=new RewriteRuleSubtreeStream(adaptor,"rule timeoutKey");
        RewriteRuleSubtreeStream stream_intLiteralOrConst=new RewriteRuleSubtreeStream(adaptor,"rule intLiteralOrConst");
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 53) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:751:2: ( timeoutKey time= intLiteralOrConst stmt= statement -> ^( TIMEOUT[$start.getStartIndex(),$text.length(),\"TIMEOUT\"] $time $stmt) )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:751:4: timeoutKey time= intLiteralOrConst stmt= statement
            {
            pushFollow(FOLLOW_timeoutKey_in_timeoutStatement3237);
            timeoutKey199=timeoutKey();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_timeoutKey.add(timeoutKey199.getTree());
            pushFollow(FOLLOW_intLiteralOrConst_in_timeoutStatement3241);
            time=intLiteralOrConst();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_intLiteralOrConst.add(time.getTree());
            pushFollow(FOLLOW_statement_in_timeoutStatement3245);
            stmt=statement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_statement.add(stmt.getTree());


            // AST REWRITE
            // elements: time, stmt
            // token labels: 
            // rule labels: retval, time, stmt
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_time=new RewriteRuleSubtreeStream(adaptor,"rule time",time!=null?time.tree:null);
            RewriteRuleSubtreeStream stream_stmt=new RewriteRuleSubtreeStream(adaptor,"rule stmt",stmt!=null?stmt.tree:null);

            root_0 = (ASTNode)adaptor.nil();
            // 752:2: -> ^( TIMEOUT[$start.getStartIndex(),$text.length(),\"TIMEOUT\"] $time $stmt)
            {
                // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:752:5: ^( TIMEOUT[$start.getStartIndex(),$text.length(),\"TIMEOUT\"] $time $stmt)
                {
                ASTNode root_1 = (ASTNode)adaptor.nil();
                root_1 = (ASTNode)adaptor.becomeRoot(new TimeoutStatement(TIMEOUT, ((CommonToken)retval.start).getStartIndex(), input.toString(retval.start,input.LT(-1)).length(), "TIMEOUT"), root_1);

                adaptor.addChild(root_1, stream_time.nextTree());
                adaptor.addChild(root_1, stream_stmt.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 53, timeoutStatement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "timeoutStatement"

    public static class progrKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "progrKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:758:1: progrKey : {...}? PROGR ;
    public final ReflexParser.progrKey_return progrKey() throws RecognitionException {
        ReflexParser.progrKey_return retval = new ReflexParser.progrKey_return();
        retval.start = input.LT(1);
        int progrKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken PROGR200=null;

        ASTNode PROGR200_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 54) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:759:5: ({...}? PROGR )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:759:7: {...}? PROGR
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("Progr", "\u041F\u0440\u043E\u0433\u0440"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "progrKey", "checkKeywordLang(\"Progr\", \"\\u041F\\u0440\\u043E\\u0433\\u0440\")");
            }
            PROGR200=(CommonToken)match(input,PROGR,FOLLOW_PROGR_in_progrKey3285); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            PROGR200_tree = (ASTNode)adaptor.create(PROGR200);
            adaptor.addChild(root_0, PROGR200_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 54, progrKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "progrKey"

    public static class tactKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tactKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:762:1: tactKey : {...}? TACT ;
    public final ReflexParser.tactKey_return tactKey() throws RecognitionException {
        ReflexParser.tactKey_return retval = new ReflexParser.tactKey_return();
        retval.start = input.LT(1);
        int tactKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken TACT201=null;

        ASTNode TACT201_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 55) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:763:5: ({...}? TACT )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:763:7: {...}? TACT
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("TACT", "\u0422\u0410\u041A\u0422"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "tactKey", "checkKeywordLang(\"TACT\", \"\\u0422\\u0410\\u041A\\u0422\")");
            }
            TACT201=(CommonToken)match(input,TACT,FOLLOW_TACT_in_tactKey3305); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            TACT201_tree = (ASTNode)adaptor.create(TACT201);
            adaptor.addChild(root_0, TACT201_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 55, tactKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "tactKey"

    public static class constKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:766:1: constKey : {...}? CONST ;
    public final ReflexParser.constKey_return constKey() throws RecognitionException {
        ReflexParser.constKey_return retval = new ReflexParser.constKey_return();
        retval.start = input.LT(1);
        int constKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken CONST202=null;

        ASTNode CONST202_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 56) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:767:5: ({...}? CONST )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:767:7: {...}? CONST
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("CONST", "\u041A\u041E\u041D\u0421\u0422"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "constKey", "checkKeywordLang(\"CONST\", \"\\u041A\\u041E\\u041D\\u0421\\u0422\")");
            }
            CONST202=(CommonToken)match(input,CONST,FOLLOW_CONST_in_constKey3325); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CONST202_tree = (ASTNode)adaptor.create(CONST202);
            adaptor.addChild(root_0, CONST202_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 56, constKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "constKey"

    public static class inputKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "inputKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:770:1: inputKey : {...}? INPUT ;
    public final ReflexParser.inputKey_return inputKey() throws RecognitionException {
        ReflexParser.inputKey_return retval = new ReflexParser.inputKey_return();
        retval.start = input.LT(1);
        int inputKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken INPUT203=null;

        ASTNode INPUT203_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 57) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:771:5: ({...}? INPUT )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:771:7: {...}? INPUT
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("INPUT", "\u0412\u0425\u041E\u0414"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "inputKey", "checkKeywordLang(\"INPUT\", \"\\u0412\\u0425\\u041E\\u0414\")");
            }
            INPUT203=(CommonToken)match(input,INPUT,FOLLOW_INPUT_in_inputKey3349); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INPUT203_tree = (ASTNode)adaptor.create(INPUT203);
            adaptor.addChild(root_0, INPUT203_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 57, inputKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "inputKey"

    public static class outputKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "outputKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:774:1: outputKey : {...}? OUTPUT ;
    public final ReflexParser.outputKey_return outputKey() throws RecognitionException {
        ReflexParser.outputKey_return retval = new ReflexParser.outputKey_return();
        retval.start = input.LT(1);
        int outputKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken OUTPUT204=null;

        ASTNode OUTPUT204_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 58) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:775:5: ({...}? OUTPUT )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:775:7: {...}? OUTPUT
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("OUTPUT", "\u0412\u042B\u0425\u041E\u0414"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "outputKey", "checkKeywordLang(\"OUTPUT\", \"\\u0412\\u042B\\u0425\\u041E\\u0414\")");
            }
            OUTPUT204=(CommonToken)match(input,OUTPUT,FOLLOW_OUTPUT_in_outputKey3369); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            OUTPUT204_tree = (ASTNode)adaptor.create(OUTPUT204);
            adaptor.addChild(root_0, OUTPUT204_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 58, outputKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "outputKey"

    public static class procKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "procKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:778:1: procKey : {...}? PROC ;
    public final ReflexParser.procKey_return procKey() throws RecognitionException {
        ReflexParser.procKey_return retval = new ReflexParser.procKey_return();
        retval.start = input.LT(1);
        int procKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken PROC205=null;

        ASTNode PROC205_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 59) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:779:5: ({...}? PROC )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:779:7: {...}? PROC
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("PROC", "\u041F\u0420\u041E\u0426"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "procKey", "checkKeywordLang(\"PROC\", \"\\u041F\\u0420\\u041E\\u0426\")");
            }
            PROC205=(CommonToken)match(input,PROC,FOLLOW_PROC_in_procKey3390); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            PROC205_tree = (ASTNode)adaptor.create(PROC205);
            adaptor.addChild(root_0, PROC205_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 59, procKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "procKey"

    public static class localKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "localKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:782:1: localKey : {...}? LOCAL ;
    public final ReflexParser.localKey_return localKey() throws RecognitionException {
        ReflexParser.localKey_return retval = new ReflexParser.localKey_return();
        retval.start = input.LT(1);
        int localKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken LOCAL206=null;

        ASTNode LOCAL206_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 60) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:783:5: ({...}? LOCAL )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:783:7: {...}? LOCAL
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("LOCAL", "\u041B\u041E\u041A\u0410\u041B"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "localKey", "checkKeywordLang(\"LOCAL\", \"\\u041B\\u041E\\u041A\\u0410\\u041B\")");
            }
            LOCAL206=(CommonToken)match(input,LOCAL,FOLLOW_LOCAL_in_localKey3410); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LOCAL206_tree = (ASTNode)adaptor.create(LOCAL206);
            adaptor.addChild(root_0, LOCAL206_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 60, localKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "localKey"

    public static class forKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "forKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:786:1: forKey : {...}? FOR ;
    public final ReflexParser.forKey_return forKey() throws RecognitionException {
        ReflexParser.forKey_return retval = new ReflexParser.forKey_return();
        retval.start = input.LT(1);
        int forKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken FOR207=null;

        ASTNode FOR207_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 61) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:787:5: ({...}? FOR )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:787:7: {...}? FOR
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("FOR", "\u0414\u041B\u042F"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "forKey", "checkKeywordLang(\"FOR\", \"\\u0414\\u041B\\u042F\")");
            }
            FOR207=(CommonToken)match(input,FOR,FOLLOW_FOR_in_forKey3431); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FOR207_tree = (ASTNode)adaptor.create(FOR207);
            adaptor.addChild(root_0, FOR207_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 61, forKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "forKey"

    public static class allKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "allKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:790:1: allKey : {...}? ALL ;
    public final ReflexParser.allKey_return allKey() throws RecognitionException {
        ReflexParser.allKey_return retval = new ReflexParser.allKey_return();
        retval.start = input.LT(1);
        int allKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken ALL208=null;

        ASTNode ALL208_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 62) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:791:5: ({...}? ALL )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:791:7: {...}? ALL
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("ALL", "\u0412\u0421\u0415\u0425"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "allKey", "checkKeywordLang(\"ALL\", \"\\u0412\\u0421\\u0415\\u0425\")");
            }
            ALL208=(CommonToken)match(input,ALL,FOLLOW_ALL_in_allKey3452); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ALL208_tree = (ASTNode)adaptor.create(ALL208);
            adaptor.addChild(root_0, ALL208_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 62, allKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "allKey"

    public static class fromKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fromKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:794:1: fromKey : {...}? FROM ;
    public final ReflexParser.fromKey_return fromKey() throws RecognitionException {
        ReflexParser.fromKey_return retval = new ReflexParser.fromKey_return();
        retval.start = input.LT(1);
        int fromKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken FROM209=null;

        ASTNode FROM209_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 63) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:795:5: ({...}? FROM )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:795:7: {...}? FROM
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("FROM", "\u0418\u0417"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "fromKey", "checkKeywordLang(\"FROM\", \"\\u0418\\u0417\")");
            }
            FROM209=(CommonToken)match(input,FROM,FOLLOW_FROM_in_fromKey3473); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FROM209_tree = (ASTNode)adaptor.create(FROM209);
            adaptor.addChild(root_0, FROM209_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 63, fromKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "fromKey"

    public static class logKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:798:1: logKey : {...}? LOG ;
    public final ReflexParser.logKey_return logKey() throws RecognitionException {
        ReflexParser.logKey_return retval = new ReflexParser.logKey_return();
        retval.start = input.LT(1);
        int logKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken LOG210=null;

        ASTNode LOG210_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 64) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:799:5: ({...}? LOG )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:799:7: {...}? LOG
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("LOG", "\u041B\u041E\u0413"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "logKey", "checkKeywordLang(\"LOG\", \"\\u041B\\u041E\\u0413\")");
            }
            LOG210=(CommonToken)match(input,LOG,FOLLOW_LOG_in_logKey3494); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LOG210_tree = (ASTNode)adaptor.create(LOG210);
            adaptor.addChild(root_0, LOG210_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 64, logKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "logKey"

    public static class intKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "intKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:802:1: intKey : {...}? INT ;
    public final ReflexParser.intKey_return intKey() throws RecognitionException {
        ReflexParser.intKey_return retval = new ReflexParser.intKey_return();
        retval.start = input.LT(1);
        int intKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken INT211=null;

        ASTNode INT211_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 65) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:803:5: ({...}? INT )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:803:7: {...}? INT
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("INT", "\u0426\u0415\u041B"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "intKey", "checkKeywordLang(\"INT\", \"\\u0426\\u0415\\u041B\")");
            }
            INT211=(CommonToken)match(input,INT,FOLLOW_INT_in_intKey3515); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INT211_tree = (ASTNode)adaptor.create(INT211);
            adaptor.addChild(root_0, INT211_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 65, intKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "intKey"

    public static class stateKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "stateKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:806:1: stateKey : {...}? STATE ;
    public final ReflexParser.stateKey_return stateKey() throws RecognitionException {
        ReflexParser.stateKey_return retval = new ReflexParser.stateKey_return();
        retval.start = input.LT(1);
        int stateKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken STATE212=null;

        ASTNode STATE212_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 66) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:807:5: ({...}? STATE )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:807:7: {...}? STATE
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("STATE", "\u0421\u041E\u0421\u0422"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "stateKey", "checkKeywordLang(\"STATE\", \"\\u0421\\u041E\\u0421\\u0422\")");
            }
            STATE212=(CommonToken)match(input,STATE,FOLLOW_STATE_in_stateKey3535); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            STATE212_tree = (ASTNode)adaptor.create(STATE212);
            adaptor.addChild(root_0, STATE212_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 66, stateKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "stateKey"

    public static class stopKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "stopKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:810:1: stopKey : {...}? STOP ;
    public final ReflexParser.stopKey_return stopKey() throws RecognitionException {
        ReflexParser.stopKey_return retval = new ReflexParser.stopKey_return();
        retval.start = input.LT(1);
        int stopKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken STOP213=null;

        ASTNode STOP213_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 67) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:811:5: ({...}? STOP )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:811:7: {...}? STOP
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("STOP", "\u0421\u0422\u041E\u041F"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "stopKey", "checkKeywordLang(\"STOP\", \"\\u0421\\u0422\\u041E\\u041F\")");
            }
            STOP213=(CommonToken)match(input,STOP,FOLLOW_STOP_in_stopKey3556); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            STOP213_tree = (ASTNode)adaptor.create(STOP213);
            adaptor.addChild(root_0, STOP213_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 67, stopKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "stopKey"

    public static class contKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "contKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:814:1: contKey : {...}? CONT ;
    public final ReflexParser.contKey_return contKey() throws RecognitionException {
        ReflexParser.contKey_return retval = new ReflexParser.contKey_return();
        retval.start = input.LT(1);
        int contKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken CONT214=null;

        ASTNode CONT214_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 68) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:815:5: ({...}? CONT )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:815:7: {...}? CONT
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("CONT", "\u041F\u0420\u041E\u0414"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "contKey", "checkKeywordLang(\"CONT\", \"\\u041F\\u0420\\u041E\\u0414\")");
            }
            CONT214=(CommonToken)match(input,CONT,FOLLOW_CONT_in_contKey3577); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CONT214_tree = (ASTNode)adaptor.create(CONT214);
            adaptor.addChild(root_0, CONT214_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 68, contKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "contKey"

    public static class startKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "startKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:818:1: startKey : {...}? START ;
    public final ReflexParser.startKey_return startKey() throws RecognitionException {
        ReflexParser.startKey_return retval = new ReflexParser.startKey_return();
        retval.start = input.LT(1);
        int startKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken START215=null;

        ASTNode START215_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 69) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:819:5: ({...}? START )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:819:7: {...}? START
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("START", "\u0421\u0422\u0410\u0420\u0422"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "startKey", "checkKeywordLang(\"START\", \"\\u0421\\u0422\\u0410\\u0420\\u0422\")");
            }
            START215=(CommonToken)match(input,START,FOLLOW_START_in_startKey3597); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            START215_tree = (ASTNode)adaptor.create(START215);
            adaptor.addChild(root_0, START215_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 69, startKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "startKey"

    public static class timeoutKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "timeoutKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:822:1: timeoutKey : {...}? TIMEOUT ;
    public final ReflexParser.timeoutKey_return timeoutKey() throws RecognitionException {
        ReflexParser.timeoutKey_return retval = new ReflexParser.timeoutKey_return();
        retval.start = input.LT(1);
        int timeoutKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken TIMEOUT216=null;

        ASTNode TIMEOUT216_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 70) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:823:5: ({...}? TIMEOUT )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:823:7: {...}? TIMEOUT
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("TIMEOUT", "\u0422\u0410\u0419\u041C\u0410\u0423\u0422"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "timeoutKey", "checkKeywordLang(\"TIMEOUT\", \"\\u0422\\u0410\\u0419\\u041C\\u0410\\u0423\\u0422\")");
            }
            TIMEOUT216=(CommonToken)match(input,TIMEOUT,FOLLOW_TIMEOUT_in_timeoutKey3617); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            TIMEOUT216_tree = (ASTNode)adaptor.create(TIMEOUT216);
            adaptor.addChild(root_0, TIMEOUT216_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 70, timeoutKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "timeoutKey"

    public static class ifKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ifKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:826:1: ifKey : {...}? IF ;
    public final ReflexParser.ifKey_return ifKey() throws RecognitionException {
        ReflexParser.ifKey_return retval = new ReflexParser.ifKey_return();
        retval.start = input.LT(1);
        int ifKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken IF217=null;

        ASTNode IF217_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 71) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:827:5: ({...}? IF )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:827:7: {...}? IF
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("IF", "\u0415\u0421\u041B\u0418"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "ifKey", "checkKeywordLang(\"IF\", \"\\u0415\\u0421\\u041B\\u0418\")");
            }
            IF217=(CommonToken)match(input,IF,FOLLOW_IF_in_ifKey3638); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IF217_tree = (ASTNode)adaptor.create(IF217);
            adaptor.addChild(root_0, IF217_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 71, ifKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "ifKey"

    public static class elseKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elseKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:830:1: elseKey : {...}? ELSE ;
    public final ReflexParser.elseKey_return elseKey() throws RecognitionException {
        ReflexParser.elseKey_return retval = new ReflexParser.elseKey_return();
        retval.start = input.LT(1);
        int elseKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken ELSE218=null;

        ASTNode ELSE218_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 72) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:831:5: ({...}? ELSE )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:831:7: {...}? ELSE
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("ELSE", "\u0418\u041D\u0410\u0427\u0415"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "elseKey", "checkKeywordLang(\"ELSE\", \"\\u0418\\u041D\\u0410\\u0427\\u0415\")");
            }
            ELSE218=(CommonToken)match(input,ELSE,FOLLOW_ELSE_in_elseKey3659); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ELSE218_tree = (ASTNode)adaptor.create(ELSE218);
            adaptor.addChild(root_0, ELSE218_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 72, elseKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "elseKey"

    public static class inKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "inKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:834:1: inKey : {...}? IN ;
    public final ReflexParser.inKey_return inKey() throws RecognitionException {
        ReflexParser.inKey_return retval = new ReflexParser.inKey_return();
        retval.start = input.LT(1);
        int inKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken IN219=null;

        ASTNode IN219_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 73) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:835:5: ({...}? IN )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:835:7: {...}? IN
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("IN", "\u0412"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "inKey", "checkKeywordLang(\"IN\", \"\\u0412\")");
            }
            IN219=(CommonToken)match(input,IN,FOLLOW_IN_in_inKey3680); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IN219_tree = (ASTNode)adaptor.create(IN219);
            adaptor.addChild(root_0, IN219_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 73, inKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "inKey"

    public static class nextKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "nextKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:838:1: nextKey : {...}? NEXT ;
    public final ReflexParser.nextKey_return nextKey() throws RecognitionException {
        ReflexParser.nextKey_return retval = new ReflexParser.nextKey_return();
        retval.start = input.LT(1);
        int nextKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken NEXT220=null;

        ASTNode NEXT220_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 74) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:839:5: ({...}? NEXT )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:839:7: {...}? NEXT
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("NEXT", "\u0421\u041B\u0415\u0414\u0423\u042E\u0429\u0415\u0415"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "nextKey", "checkKeywordLang(\"NEXT\", \"\\u0421\\u041B\\u0415\\u0414\\u0423\\u042E\\u0429\\u0415\\u0415\")");
            }
            NEXT220=(CommonToken)match(input,NEXT,FOLLOW_NEXT_in_nextKey3700); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            NEXT220_tree = (ASTNode)adaptor.create(NEXT220);
            adaptor.addChild(root_0, NEXT220_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 74, nextKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "nextKey"

    public static class ramKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ramKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:842:1: ramKey : {...}? RAM ;
    public final ReflexParser.ramKey_return ramKey() throws RecognitionException {
        ReflexParser.ramKey_return retval = new ReflexParser.ramKey_return();
        retval.start = input.LT(1);
        int ramKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken RAM221=null;

        ASTNode RAM221_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 75) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:843:5: ({...}? RAM )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:843:7: {...}? RAM
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("RAM", "\u041E\u0417\u0423"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "ramKey", "checkKeywordLang(\"RAM\", \"\\u041E\\u0417\\u0423\")");
            }
            RAM221=(CommonToken)match(input,RAM,FOLLOW_RAM_in_ramKey3720); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RAM221_tree = (ASTNode)adaptor.create(RAM221);
            adaptor.addChild(root_0, RAM221_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 75, ramKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "ramKey"

    public static class errorKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "errorKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:846:1: errorKey : {...}? ERROR ;
    public final ReflexParser.errorKey_return errorKey() throws RecognitionException {
        ReflexParser.errorKey_return retval = new ReflexParser.errorKey_return();
        retval.start = input.LT(1);
        int errorKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken ERROR222=null;

        ASTNode ERROR222_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 76) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:847:5: ({...}? ERROR )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:847:7: {...}? ERROR
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("ERROR", "\u041E\u0428\u0418\u0411\u041A\u0410"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "errorKey", "checkKeywordLang(\"ERROR\", \"\\u041E\\u0428\\u0418\\u0411\\u041A\\u0410\")");
            }
            ERROR222=(CommonToken)match(input,ERROR,FOLLOW_ERROR_in_errorKey3740); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ERROR222_tree = (ASTNode)adaptor.create(ERROR222);
            adaptor.addChild(root_0, ERROR222_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 76, errorKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "errorKey"

    public static class shortKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shortKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:850:1: shortKey : {...}? SHORT ;
    public final ReflexParser.shortKey_return shortKey() throws RecognitionException {
        ReflexParser.shortKey_return retval = new ReflexParser.shortKey_return();
        retval.start = input.LT(1);
        int shortKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken SHORT223=null;

        ASTNode SHORT223_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 77) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:851:5: ({...}? SHORT )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:851:7: {...}? SHORT
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("SHORT", "\u041A\u0426\u0415\u041B"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "shortKey", "checkKeywordLang(\"SHORT\", \"\\u041A\\u0426\\u0415\\u041B\")");
            }
            SHORT223=(CommonToken)match(input,SHORT,FOLLOW_SHORT_in_shortKey3760); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SHORT223_tree = (ASTNode)adaptor.create(SHORT223);
            adaptor.addChild(root_0, SHORT223_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 77, shortKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "shortKey"

    public static class longKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "longKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:854:1: longKey : {...}? LONG ;
    public final ReflexParser.longKey_return longKey() throws RecognitionException {
        ReflexParser.longKey_return retval = new ReflexParser.longKey_return();
        retval.start = input.LT(1);
        int longKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken LONG224=null;

        ASTNode LONG224_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 78) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:855:5: ({...}? LONG )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:855:7: {...}? LONG
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("LONG", "\u0414\u0426\u0415\u041B"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "longKey", "checkKeywordLang(\"LONG\", \"\\u0414\\u0426\\u0415\\u041B\")");
            }
            LONG224=(CommonToken)match(input,LONG,FOLLOW_LONG_in_longKey3780); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LONG224_tree = (ASTNode)adaptor.create(LONG224);
            adaptor.addChild(root_0, LONG224_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 78, longKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "longKey"

    public static class switchKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "switchKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:858:1: switchKey : {...}? SWITCH ;
    public final ReflexParser.switchKey_return switchKey() throws RecognitionException {
        ReflexParser.switchKey_return retval = new ReflexParser.switchKey_return();
        retval.start = input.LT(1);
        int switchKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken SWITCH225=null;

        ASTNode SWITCH225_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 79) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:859:5: ({...}? SWITCH )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:859:7: {...}? SWITCH
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("SWITCH", "\u0420\u0410\u0417\u0411\u041E\u0420"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "switchKey", "checkKeywordLang(\"SWITCH\", \"\\u0420\\u0410\\u0417\\u0411\\u041E\\u0420\")");
            }
            SWITCH225=(CommonToken)match(input,SWITCH,FOLLOW_SWITCH_in_switchKey3800); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SWITCH225_tree = (ASTNode)adaptor.create(SWITCH225);
            adaptor.addChild(root_0, SWITCH225_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 79, switchKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "switchKey"

    public static class caseKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "caseKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:862:1: caseKey : {...}? CASE ;
    public final ReflexParser.caseKey_return caseKey() throws RecognitionException {
        ReflexParser.caseKey_return retval = new ReflexParser.caseKey_return();
        retval.start = input.LT(1);
        int caseKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken CASE226=null;

        ASTNode CASE226_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 80) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:863:5: ({...}? CASE )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:863:7: {...}? CASE
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("CASE", "\u0421\u041B\u0423\u0427\u0410\u0419"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "caseKey", "checkKeywordLang(\"CASE\", \"\\u0421\\u041B\\u0423\\u0427\\u0410\\u0419\")");
            }
            CASE226=(CommonToken)match(input,CASE,FOLLOW_CASE_in_caseKey3820); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CASE226_tree = (ASTNode)adaptor.create(CASE226);
            adaptor.addChild(root_0, CASE226_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 80, caseKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "caseKey"

    public static class breakKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "breakKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:866:1: breakKey : {...}? BREAK ;
    public final ReflexParser.breakKey_return breakKey() throws RecognitionException {
        ReflexParser.breakKey_return retval = new ReflexParser.breakKey_return();
        retval.start = input.LT(1);
        int breakKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken BREAK227=null;

        ASTNode BREAK227_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 81) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:867:5: ({...}? BREAK )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:867:7: {...}? BREAK
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("BREAK", "\u041A\u041E\u041D\u0415\u0426"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "breakKey", "checkKeywordLang(\"BREAK\", \"\\u041A\\u041E\\u041D\\u0415\\u0426\")");
            }
            BREAK227=(CommonToken)match(input,BREAK,FOLLOW_BREAK_in_breakKey3840); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            BREAK227_tree = (ASTNode)adaptor.create(BREAK227);
            adaptor.addChild(root_0, BREAK227_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 81, breakKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "breakKey"

    public static class floatKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "floatKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:870:1: floatKey : {...}? FLOAT ;
    public final ReflexParser.floatKey_return floatKey() throws RecognitionException {
        ReflexParser.floatKey_return retval = new ReflexParser.floatKey_return();
        retval.start = input.LT(1);
        int floatKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken FLOAT228=null;

        ASTNode FLOAT228_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 82) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:871:5: ({...}? FLOAT )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:871:7: {...}? FLOAT
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("FLOAT", "\u041F\u041B\u0410\u0412"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "floatKey", "checkKeywordLang(\"FLOAT\", \"\\u041F\\u041B\\u0410\\u0412\")");
            }
            FLOAT228=(CommonToken)match(input,FLOAT,FOLLOW_FLOAT_in_floatKey3860); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FLOAT228_tree = (ASTNode)adaptor.create(FLOAT228);
            adaptor.addChild(root_0, FLOAT228_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 82, floatKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "floatKey"

    public static class doubleKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "doubleKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:874:1: doubleKey : {...}? DOUBLE ;
    public final ReflexParser.doubleKey_return doubleKey() throws RecognitionException {
        ReflexParser.doubleKey_return retval = new ReflexParser.doubleKey_return();
        retval.start = input.LT(1);
        int doubleKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken DOUBLE229=null;

        ASTNode DOUBLE229_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 83) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:875:5: ({...}? DOUBLE )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:875:7: {...}? DOUBLE
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("DOUBLE", "\u0414\u041F\u041B\u0410\u0412"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "doubleKey", "checkKeywordLang(\"DOUBLE\", \"\\u0414\\u041F\\u041B\\u0410\\u0412\")");
            }
            DOUBLE229=(CommonToken)match(input,DOUBLE,FOLLOW_DOUBLE_in_doubleKey3880); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DOUBLE229_tree = (ASTNode)adaptor.create(DOUBLE229);
            adaptor.addChild(root_0, DOUBLE229_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 83, doubleKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "doubleKey"

    public static class defaultKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "defaultKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:878:1: defaultKey : {...}? DEFAULT ;
    public final ReflexParser.defaultKey_return defaultKey() throws RecognitionException {
        ReflexParser.defaultKey_return retval = new ReflexParser.defaultKey_return();
        retval.start = input.LT(1);
        int defaultKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken DEFAULT230=null;

        ASTNode DEFAULT230_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 84) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:879:5: ({...}? DEFAULT )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:879:7: {...}? DEFAULT
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("DEFAULT", "\u0423\u041C\u041E\u041B\u0427\u0410\u041D\u0418\u0415"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "defaultKey", "checkKeywordLang(\"DEFAULT\", \"\\u0423\\u041C\\u041E\\u041B\\u0427\\u0410\\u041D\\u0418\\u0415\")");
            }
            DEFAULT230=(CommonToken)match(input,DEFAULT,FOLLOW_DEFAULT_in_defaultKey3900); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DEFAULT230_tree = (ASTNode)adaptor.create(DEFAULT230);
            adaptor.addChild(root_0, DEFAULT230_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 84, defaultKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "defaultKey"

    public static class activeKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "activeKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:882:1: activeKey : {...}? ACTIVE ;
    public final ReflexParser.activeKey_return activeKey() throws RecognitionException {
        ReflexParser.activeKey_return retval = new ReflexParser.activeKey_return();
        retval.start = input.LT(1);
        int activeKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken ACTIVE231=null;

        ASTNode ACTIVE231_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 85) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:883:5: ({...}? ACTIVE )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:883:7: {...}? ACTIVE
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("ACTIVE", "\u0410\u041A\u0422\u0418\u0412\u041D\u041E\u0415"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "activeKey", "checkKeywordLang(\"ACTIVE\", \"\\u0410\\u041A\\u0422\\u0418\\u0412\\u041D\\u041E\\u0415\")");
            }
            ACTIVE231=(CommonToken)match(input,ACTIVE,FOLLOW_ACTIVE_in_activeKey3920); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ACTIVE231_tree = (ASTNode)adaptor.create(ACTIVE231);
            adaptor.addChild(root_0, ACTIVE231_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 85, activeKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "activeKey"

    public static class passiveKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "passiveKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:886:1: passiveKey : {...}? PASSIVE ;
    public final ReflexParser.passiveKey_return passiveKey() throws RecognitionException {
        ReflexParser.passiveKey_return retval = new ReflexParser.passiveKey_return();
        retval.start = input.LT(1);
        int passiveKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken PASSIVE232=null;

        ASTNode PASSIVE232_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 86) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:887:5: ({...}? PASSIVE )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:887:7: {...}? PASSIVE
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("PASSIVE", "\u041F\u0410\u0421\u0421\u0418\u0412\u041D\u041E\u0415"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "passiveKey", "checkKeywordLang(\"PASSIVE\", \"\\u041F\\u0410\\u0421\\u0421\\u0418\\u0412\\u041D\\u041E\\u0415\")");
            }
            PASSIVE232=(CommonToken)match(input,PASSIVE,FOLLOW_PASSIVE_in_passiveKey3940); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            PASSIVE232_tree = (ASTNode)adaptor.create(PASSIVE232);
            adaptor.addChild(root_0, PASSIVE232_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 86, passiveKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "passiveKey"

    public static class loopKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "loopKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:890:1: loopKey : {...}? LOOP ;
    public final ReflexParser.loopKey_return loopKey() throws RecognitionException {
        ReflexParser.loopKey_return retval = new ReflexParser.loopKey_return();
        retval.start = input.LT(1);
        int loopKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken LOOP233=null;

        ASTNode LOOP233_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 87) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:891:5: ({...}? LOOP )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:891:7: {...}? LOOP
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("LOOP", "\u0417\u0410\u0426\u0418\u041A\u041B\u0418\u0422\u042C"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "loopKey", "checkKeywordLang(\"LOOP\", \"\\u0417\\u0410\\u0426\\u0418\\u041A\\u041B\\u0418\\u0422\\u042C\")");
            }
            LOOP233=(CommonToken)match(input,LOOP,FOLLOW_LOOP_in_loopKey3964); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LOOP233_tree = (ASTNode)adaptor.create(LOOP233);
            adaptor.addChild(root_0, LOOP233_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 87, loopKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "loopKey"

    public static class signedKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "signedKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:894:1: signedKey : {...}? SIGNED ;
    public final ReflexParser.signedKey_return signedKey() throws RecognitionException {
        ReflexParser.signedKey_return retval = new ReflexParser.signedKey_return();
        retval.start = input.LT(1);
        int signedKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken SIGNED234=null;

        ASTNode SIGNED234_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 88) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:895:5: ({...}? SIGNED )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:895:7: {...}? SIGNED
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("SIGNED", "\u0417\u041D\u0410\u041A\u041E\u0412\u041E\u0415"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "signedKey", "checkKeywordLang(\"SIGNED\", \"\\u0417\\u041D\\u0410\\u041A\\u041E\\u0412\\u041E\\u0415\")");
            }
            SIGNED234=(CommonToken)match(input,SIGNED,FOLLOW_SIGNED_in_signedKey3984); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SIGNED234_tree = (ASTNode)adaptor.create(SIGNED234);
            adaptor.addChild(root_0, SIGNED234_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 88, signedKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "signedKey"

    public static class unsignedKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unsignedKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:898:1: unsignedKey : {...}? UNSIGNED ;
    public final ReflexParser.unsignedKey_return unsignedKey() throws RecognitionException {
        ReflexParser.unsignedKey_return retval = new ReflexParser.unsignedKey_return();
        retval.start = input.LT(1);
        int unsignedKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken UNSIGNED235=null;

        ASTNode UNSIGNED235_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 89) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:899:5: ({...}? UNSIGNED )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:899:7: {...}? UNSIGNED
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("UNSIGNED", "\u0411\u0415\u0417\u0417\u041D\u0410\u041A\u041E\u0412\u041E\u0415"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "unsignedKey", "checkKeywordLang(\"UNSIGNED\", \"\\u0411\\u0415\\u0417\\u0417\\u041D\\u0410\\u041A\\u041E\\u0412\\u041E\\u0415\")");
            }
            UNSIGNED235=(CommonToken)match(input,UNSIGNED,FOLLOW_UNSIGNED_in_unsignedKey4004); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            UNSIGNED235_tree = (ASTNode)adaptor.create(UNSIGNED235);
            adaptor.addChild(root_0, UNSIGNED235_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 89, unsignedKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "unsignedKey"

    public static class enumKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "enumKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:902:1: enumKey : {...}? ENUM ;
    public final ReflexParser.enumKey_return enumKey() throws RecognitionException {
        ReflexParser.enumKey_return retval = new ReflexParser.enumKey_return();
        retval.start = input.LT(1);
        int enumKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken ENUM236=null;

        ASTNode ENUM236_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 90) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:903:5: ({...}? ENUM )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:903:7: {...}? ENUM
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("ENUM", "\u041F\u0415\u0420\u0415\u0427\u0418\u0421\u041B\u0415\u041D\u0418\u0415"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "enumKey", "checkKeywordLang(\"ENUM\", \"\\u041F\\u0415\\u0420\\u0415\\u0427\\u0418\\u0421\\u041B\\u0415\\u041D\\u0418\\u0415\")");
            }
            ENUM236=(CommonToken)match(input,ENUM,FOLLOW_ENUM_in_enumKey4024); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ENUM236_tree = (ASTNode)adaptor.create(ENUM236);
            adaptor.addChild(root_0, ENUM236_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 90, enumKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "enumKey"

    public static class functionKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "functionKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:906:1: functionKey : {...}? FUNCTION ;
    public final ReflexParser.functionKey_return functionKey() throws RecognitionException {
        ReflexParser.functionKey_return retval = new ReflexParser.functionKey_return();
        retval.start = input.LT(1);
        int functionKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken FUNCTION237=null;

        ASTNode FUNCTION237_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 91) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:907:5: ({...}? FUNCTION )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:907:7: {...}? FUNCTION
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("FUNCTION", "\u0424\u0423\u041D\u041A\u0426\u0418\u042F"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "functionKey", "checkKeywordLang(\"FUNCTION\", \"\\u0424\\u0423\\u041D\\u041A\\u0426\\u0418\\u042F\")");
            }
            FUNCTION237=(CommonToken)match(input,FUNCTION,FOLLOW_FUNCTION_in_functionKey4044); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FUNCTION237_tree = (ASTNode)adaptor.create(FUNCTION237);
            adaptor.addChild(root_0, FUNCTION237_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 91, functionKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "functionKey"

    public static class voidKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "voidKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:910:1: voidKey : {...}? VOID ;
    public final ReflexParser.voidKey_return voidKey() throws RecognitionException {
        ReflexParser.voidKey_return retval = new ReflexParser.voidKey_return();
        retval.start = input.LT(1);
        int voidKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken VOID238=null;

        ASTNode VOID238_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 92) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:911:5: ({...}? VOID )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:911:7: {...}? VOID
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("VOID", "\u041F\u0423\u0421\u0422\u041E"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "voidKey", "checkKeywordLang(\"VOID\", \"\\u041F\\u0423\\u0421\\u0422\\u041E\")");
            }
            VOID238=(CommonToken)match(input,VOID,FOLLOW_VOID_in_voidKey4068); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            VOID238_tree = (ASTNode)adaptor.create(VOID238);
            adaptor.addChild(root_0, VOID238_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 92, voidKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "voidKey"

    public static class cMarkKey_return extends ParserRuleReturnScope {
        ASTNode tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cMarkKey"
    // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:914:1: cMarkKey : {...}? CMARK ;
    public final ReflexParser.cMarkKey_return cMarkKey() throws RecognitionException {
        ReflexParser.cMarkKey_return retval = new ReflexParser.cMarkKey_return();
        retval.start = input.LT(1);
        int cMarkKey_StartIndex = input.index();
        ASTNode root_0 = null;

        CommonToken CMARK239=null;

        ASTNode CMARK239_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 93) ) { return retval; }
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:915:5: ({...}? CMARK )
            // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:915:7: {...}? CMARK
            {
            root_0 = (ASTNode)adaptor.nil();

            if ( !((checkKeywordLang("#C", "#\u0421\u0418"))) ) {
                if (state.backtracking>0) {state.failed=true; return retval;}
                throw new FailedPredicateException(input, "cMarkKey", "checkKeywordLang(\"#C\", \"#\\u0421\\u0418\")");
            }
            CMARK239=(CommonToken)match(input,CMARK,FOLLOW_CMARK_in_cMarkKey4088); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CMARK239_tree = (ASTNode)adaptor.create(CMARK239);
            adaptor.addChild(root_0, CMARK239_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (ASTNode)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (ASTNode)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 93, cMarkKey_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "cMarkKey"

    // $ANTLR start synpred85_Reflex
    public final void synpred85_Reflex_fragment() throws RecognitionException {   
        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:590:4: ( jumpStatement )
        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:590:4: jumpStatement
        {
        pushFollow(FOLLOW_jumpStatement_in_synpred85_Reflex2533);
        jumpStatement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred85_Reflex

    // $ANTLR start synpred86_Reflex
    public final void synpred86_Reflex_fragment() throws RecognitionException {   
        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:591:4: ( controlProcessStatement )
        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:591:4: controlProcessStatement
        {
        pushFollow(FOLLOW_controlProcessStatement_in_synpred86_Reflex2538);
        controlProcessStatement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred86_Reflex

    // $ANTLR start synpred91_Reflex
    public final void synpred91_Reflex_fragment() throws RecognitionException {   
        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:616:45: ( elseKey )
        // C:\\Documents and Settings\\alex\\grammars\\Reflex\\Reflex.g:616:46: elseKey
        {
        pushFollow(FOLLOW_elseKey_in_synpred91_Reflex2655);
        elseKey();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred91_Reflex

    // Delegated rules

    public final boolean synpred91_Reflex() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred91_Reflex_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred85_Reflex() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred85_Reflex_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred86_Reflex() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred86_Reflex_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA10 dfa10 = new DFA10(this);
    protected DFA14 dfa14 = new DFA14(this);
    protected DFA23 dfa23 = new DFA23(this);
    protected DFA24 dfa24 = new DFA24(this);
    protected DFA39 dfa39 = new DFA39(this);
    protected DFA41 dfa41 = new DFA41(this);
    protected DFA42 dfa42 = new DFA42(this);
    protected DFA43 dfa43 = new DFA43(this);
    protected DFA45 dfa45 = new DFA45(this);
    static final String DFA10_eotS =
        "\13\uffff";
    static final String DFA10_eofS =
        "\13\uffff";
    static final String DFA10_minS =
        "\1\125\12\uffff";
    static final String DFA10_maxS =
        "\1\166\12\uffff";
    static final String DFA10_acceptS =
        "\1\uffff\1\2\1\1\10\uffff";
    static final String DFA10_specialS =
        "\13\uffff}>";
    static final String[] DFA10_transitionS = {
            "\7\2\20\uffff\1\2\1\1\10\uffff\1\2",
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

    static final short[] DFA10_eot = DFA.unpackEncodedString(DFA10_eotS);
    static final short[] DFA10_eof = DFA.unpackEncodedString(DFA10_eofS);
    static final char[] DFA10_min = DFA.unpackEncodedStringToUnsignedChars(DFA10_minS);
    static final char[] DFA10_max = DFA.unpackEncodedStringToUnsignedChars(DFA10_maxS);
    static final short[] DFA10_accept = DFA.unpackEncodedString(DFA10_acceptS);
    static final short[] DFA10_special = DFA.unpackEncodedString(DFA10_specialS);
    static final short[][] DFA10_transition;

    static {
        int numStates = DFA10_transitionS.length;
        DFA10_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA10_transition[i] = DFA.unpackEncodedString(DFA10_transitionS[i]);
        }
    }

    class DFA10 extends DFA {

        public DFA10(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 10;
            this.eot = DFA10_eot;
            this.eof = DFA10_eof;
            this.min = DFA10_min;
            this.max = DFA10_max;
            this.accept = DFA10_accept;
            this.special = DFA10_special;
            this.transition = DFA10_transition;
        }
        public String getDescription() {
            return "()* loopback of 238:30: ( variableDeclaration )*";
        }
    }
    static final String DFA14_eotS =
        "\12\uffff";
    static final String DFA14_eofS =
        "\12\uffff";
    static final String DFA14_minS =
        "\1\125\11\uffff";
    static final String DFA14_maxS =
        "\1\166\11\uffff";
    static final String DFA14_acceptS =
        "\1\uffff\1\1\6\uffff\1\2\1\3";
    static final String DFA14_specialS =
        "\12\uffff}>";
    static final String[] DFA14_transitionS = {
            "\7\1\20\uffff\1\10\11\uffff\1\11",
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
            return "279:2: (type= typeSpecifier varName= identifier (port= portLinkList )? mods= visibleModifier -> ^( VAR_DECL[$start.getStartIndex(),$text.length(),\"VAR_DECL\"] $type $varName ( $port)? $mods) | from= fromKey procKey procName= IDENTIFIER vars+= identifier ( ',' vars+= identifier )* -> ^( VAR_REF[$start.getStartIndex(),$text.length(),\"VAR_REF\"] PROCESS_NAME[$procName.getStartIndex(),$procName.text.length(),$procName] ( $vars)+ ) | enumDeclaration )";
        }
    }
    static final String DFA23_eotS =
        "\16\uffff";
    static final String DFA23_eofS =
        "\16\uffff";
    static final String DFA23_minS =
        "\1\4\15\uffff";
    static final String DFA23_maxS =
        "\1\151\15\uffff";
    static final String DFA23_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\6\uffff";
    static final String DFA23_specialS =
        "\16\uffff}>";
    static final String[] DFA23_transitionS = {
            "\1\7\11\uffff\1\4\6\uffff\1\5\1\6\1\1\1\2\3\uffff\1\3\103\uffff"+
            "\5\7\4\uffff\1\7",
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
            return "423:1: unaryExpression : ( PLUS unaryExpression -> ^( UNARY_PLUS[$PLUS, \"UNARY_PLUS\"] unaryExpression ) | MINUS unaryExpression -> ^( UNARY_MINUS[$MINUS, \"UNARY_MINUS\"] unaryExpression ) | NOT unaryExpression -> ^( NOT[$NOT, \"NOT\"] unaryExpression ) | LOGICAL_NOT unaryExpression -> ^( LOGICAL_NOT[$LOGICAL_NOT, \"LOGICAL_NOT\"] unaryExpression ) | INC postfixExpression -> ^( PRE_INC[$INC, \"PRE_INC\"] postfixExpression ) | DEC postfixExpression -> ^( PRE_DEC[$DEC, \"PRE_DEC\"] postfixExpression ) | postfixExpression );";
        }
    }
    static final String DFA24_eotS =
        "\17\uffff";
    static final String DFA24_eofS =
        "\17\uffff";
    static final String DFA24_minS =
        "\1\4\16\uffff";
    static final String DFA24_maxS =
        "\1\151\16\uffff";
    static final String DFA24_acceptS =
        "\1\uffff\1\1\14\uffff\1\2";
    static final String DFA24_specialS =
        "\17\uffff}>";
    static final String[] DFA24_transitionS = {
            "\1\1\1\16\10\uffff\1\1\6\uffff\4\1\3\uffff\1\1\103\uffff\5"+
            "\1\4\uffff\1\1",
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

    static final short[] DFA24_eot = DFA.unpackEncodedString(DFA24_eotS);
    static final short[] DFA24_eof = DFA.unpackEncodedString(DFA24_eofS);
    static final char[] DFA24_min = DFA.unpackEncodedStringToUnsignedChars(DFA24_minS);
    static final char[] DFA24_max = DFA.unpackEncodedStringToUnsignedChars(DFA24_maxS);
    static final short[] DFA24_accept = DFA.unpackEncodedString(DFA24_acceptS);
    static final short[] DFA24_special = DFA.unpackEncodedString(DFA24_specialS);
    static final short[][] DFA24_transition;

    static {
        int numStates = DFA24_transitionS.length;
        DFA24_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA24_transition[i] = DFA.unpackEncodedString(DFA24_transitionS[i]);
        }
    }

    class DFA24 extends DFA {

        public DFA24(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 24;
            this.eot = DFA24_eot;
            this.eof = DFA24_eof;
            this.min = DFA24_min;
            this.max = DFA24_max;
            this.accept = DFA24_accept;
            this.special = DFA24_special;
            this.transition = DFA24_transition;
        }
        public String getDescription() {
            return "438:11: ( arguments )?";
        }
    }
    static final String DFA39_eotS =
        "\35\uffff";
    static final String DFA39_eofS =
        "\35\uffff";
    static final String DFA39_minS =
        "\1\4\24\uffff\1\0\7\uffff";
    static final String DFA39_maxS =
        "\1\161\24\uffff\1\0\7\uffff";
    static final String DFA39_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\1\3\15\uffff\1\4\1\5\1\6\1\uffff\1\7\1"+
        "\10\2\uffff\1\11\1\uffff\1\12";
    static final String DFA39_specialS =
        "\25\uffff\1\0\7\uffff}>";
    static final String[] DFA39_transitionS = {
            "\1\4\1\uffff\1\3\3\uffff\1\4\3\uffff\1\4\6\uffff\4\4\3\uffff"+
            "\1\4\44\uffff\1\24\1\uffff\1\22\1\23\1\32\1\uffff\1\26\1\34"+
            "\3\27\1\25\2\uffff\1\32\20\uffff\5\4\4\uffff\1\4\6\uffff\2\1",
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
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA39_eot = DFA.unpackEncodedString(DFA39_eotS);
    static final short[] DFA39_eof = DFA.unpackEncodedString(DFA39_eofS);
    static final char[] DFA39_min = DFA.unpackEncodedStringToUnsignedChars(DFA39_minS);
    static final char[] DFA39_max = DFA.unpackEncodedStringToUnsignedChars(DFA39_maxS);
    static final short[] DFA39_accept = DFA.unpackEncodedString(DFA39_acceptS);
    static final short[] DFA39_special = DFA.unpackEncodedString(DFA39_specialS);
    static final short[][] DFA39_transition;

    static {
        int numStates = DFA39_transitionS.length;
        DFA39_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA39_transition[i] = DFA.unpackEncodedString(DFA39_transitionS[i]);
        }
    }

    class DFA39 extends DFA {

        public DFA39(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 39;
            this.eot = DFA39_eot;
            this.eof = DFA39_eof;
            this.min = DFA39_min;
            this.max = DFA39_max;
            this.accept = DFA39_accept;
            this.special = DFA39_special;
            this.transition = DFA39_transition;
        }
        public String getDescription() {
            return "583:1: statement : ( labeledStatement | compoundStatement | expressionStatement | ifStatement | switchStatement | iterationStatement | jumpStatement | controlProcessStatement | passStatement | timeoutStatement );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA39_21 = input.LA(1);

                         
                        int index39_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred85_Reflex()&&(checkKeywordLang("CONT", "\u041F\u0420\u041E\u0414")))) ) {s = 22;}

                        else if ( ((synpred86_Reflex()&&(checkKeywordLang("CONT", "\u041F\u0420\u041E\u0414")))) ) {s = 23;}

                         
                        input.seek(index39_21);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 39, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA41_eotS =
        "\36\uffff";
    static final String DFA41_eofS =
        "\36\uffff";
    static final String DFA41_minS =
        "\1\4\35\uffff";
    static final String DFA41_maxS =
        "\1\161\35\uffff";
    static final String DFA41_acceptS =
        "\1\uffff\1\2\1\1\33\uffff";
    static final String DFA41_specialS =
        "\36\uffff}>";
    static final String[] DFA41_transitionS = {
            "\1\2\1\uffff\1\2\1\1\2\uffff\1\2\3\uffff\1\2\6\uffff\4\2\3"+
            "\uffff\1\2\44\uffff\1\2\1\uffff\3\2\1\uffff\6\2\2\uffff\1\2"+
            "\20\uffff\5\2\4\uffff\1\2\6\uffff\2\2",
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

    static final short[] DFA41_eot = DFA.unpackEncodedString(DFA41_eotS);
    static final short[] DFA41_eof = DFA.unpackEncodedString(DFA41_eofS);
    static final char[] DFA41_min = DFA.unpackEncodedStringToUnsignedChars(DFA41_minS);
    static final char[] DFA41_max = DFA.unpackEncodedStringToUnsignedChars(DFA41_maxS);
    static final short[] DFA41_accept = DFA.unpackEncodedString(DFA41_acceptS);
    static final short[] DFA41_special = DFA.unpackEncodedString(DFA41_specialS);
    static final short[][] DFA41_transition;

    static {
        int numStates = DFA41_transitionS.length;
        DFA41_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA41_transition[i] = DFA.unpackEncodedString(DFA41_transitionS[i]);
        }
    }

    class DFA41 extends DFA {

        public DFA41(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 41;
            this.eot = DFA41_eot;
            this.eof = DFA41_eof;
            this.min = DFA41_min;
            this.max = DFA41_max;
            this.accept = DFA41_accept;
            this.special = DFA41_special;
            this.transition = DFA41_transition;
        }
        public String getDescription() {
            return "()* loopback of 602:8: ( statement )*";
        }
    }
    static final String DFA42_eotS =
        "\17\uffff";
    static final String DFA42_eofS =
        "\17\uffff";
    static final String DFA42_minS =
        "\1\4\16\uffff";
    static final String DFA42_maxS =
        "\1\151\16\uffff";
    static final String DFA42_acceptS =
        "\1\uffff\1\1\1\2\14\uffff";
    static final String DFA42_specialS =
        "\17\uffff}>";
    static final String[] DFA42_transitionS = {
            "\1\2\5\uffff\1\1\3\uffff\1\2\6\uffff\4\2\3\uffff\1\2\103\uffff"+
            "\5\2\4\uffff\1\2",
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
            return "610:1: expressionStatement : ( ';' | expression ';' );";
        }
    }
    static final String DFA43_eotS =
        "\41\uffff";
    static final String DFA43_eofS =
        "\1\2\40\uffff";
    static final String DFA43_minS =
        "\1\4\1\0\37\uffff";
    static final String DFA43_maxS =
        "\1\161\1\0\37\uffff";
    static final String DFA43_acceptS =
        "\2\uffff\1\2\35\uffff\1\1";
    static final String DFA43_specialS =
        "\1\uffff\1\0\37\uffff}>";
    static final String[] DFA43_transitionS = {
            "\1\2\1\uffff\2\2\2\uffff\1\2\3\uffff\1\2\6\uffff\4\2\3\uffff"+
            "\1\2\44\uffff\1\2\1\uffff\3\2\1\uffff\6\2\2\uffff\1\2\20\uffff"+
            "\5\2\4\uffff\1\2\4\uffff\1\1\1\uffff\2\2",
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

    static final short[] DFA43_eot = DFA.unpackEncodedString(DFA43_eotS);
    static final short[] DFA43_eof = DFA.unpackEncodedString(DFA43_eofS);
    static final char[] DFA43_min = DFA.unpackEncodedStringToUnsignedChars(DFA43_minS);
    static final char[] DFA43_max = DFA.unpackEncodedStringToUnsignedChars(DFA43_maxS);
    static final short[] DFA43_accept = DFA.unpackEncodedString(DFA43_acceptS);
    static final short[] DFA43_special = DFA.unpackEncodedString(DFA43_specialS);
    static final short[][] DFA43_transition;

    static {
        int numStates = DFA43_transitionS.length;
        DFA43_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA43_transition[i] = DFA.unpackEncodedString(DFA43_transitionS[i]);
        }
    }

    class DFA43 extends DFA {

        public DFA43(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 43;
            this.eot = DFA43_eot;
            this.eof = DFA43_eof;
            this.min = DFA43_min;
            this.max = DFA43_max;
            this.accept = DFA43_accept;
            this.special = DFA43_special;
            this.transition = DFA43_transition;
        }
        public String getDescription() {
            return "616:44: ( ( elseKey )=> elseKey elseSt= statement )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA43_1 = input.LA(1);

                         
                        int index43_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred91_Reflex()&&(checkKeywordLang("ELSE", "\u0418\u041D\u0410\u0427\u0415")))) ) {s = 32;}

                        else if ( ((checkKeywordLang("ELSE", "\u0418\u041D\u0410\u0427\u0415"))) ) {s = 2;}

                         
                        input.seek(index43_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 43, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA45_eotS =
        "\17\uffff";
    static final String DFA45_eofS =
        "\17\uffff";
    static final String DFA45_minS =
        "\1\4\16\uffff";
    static final String DFA45_maxS =
        "\1\151\16\uffff";
    static final String DFA45_acceptS =
        "\1\uffff\1\1\14\uffff\1\2";
    static final String DFA45_specialS =
        "\17\uffff}>";
    static final String[] DFA45_transitionS = {
            "\1\1\1\16\10\uffff\1\1\6\uffff\4\1\3\uffff\1\1\103\uffff\5"+
            "\1\4\uffff\1\1",
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
            return "633:85: (contAction= expression )?";
        }
    }
 

    public static final BitSet FOLLOW_progrKey_in_program585 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_identifier_in_program587 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LBRACE_in_program591 = new BitSet(new long[]{0x0000000000000000L,0x0000004000000000L});
    public static final BitSet FOLLOW_tactDeclaration_in_program593 = new BitSet(new long[]{0x0000000000000000L,0x00C0038060000000L});
    public static final BitSet FOLLOW_constDeclaration_in_program595 = new BitSet(new long[]{0x0000000000000000L,0x00C0038060000000L});
    public static final BitSet FOLLOW_functionDeclaration_in_program598 = new BitSet(new long[]{0x0000000000000000L,0x00C0038060000000L});
    public static final BitSet FOLLOW_registerDeclaration_in_program601 = new BitSet(new long[]{0x0000000000000000L,0x00C0038060000000L});
    public static final BitSet FOLLOW_processDeclaration_in_program604 = new BitSet(new long[]{0x0000000000000080L,0x00C0038060000000L});
    public static final BitSet FOLLOW_RBRACE_in_program607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tactKey_in_tactDeclaration659 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
    public static final BitSet FOLLOW_INTEGER_LITERAL_in_tactDeclaration661 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_tactDeclaration665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constKey_in_constDeclaration707 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_identifier_in_constDeclaration711 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_logical_or_expression_in_constDeclaration715 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_enumDeclaration_in_constDeclaration739 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_constDeclaration745 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionKey_in_functionDeclaration757 = new BitSet(new long[]{0x0000000000000000L,0x000000000FE00000L});
    public static final BitSet FOLLOW_typeSpecifier_in_functionDeclaration761 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_identifier_in_functionDeclaration765 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_formalArguments_in_functionDeclaration769 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_functionDeclaration772 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_formalArguments808 = new BitSet(new long[]{0x0000000000000000L,0x000000000FE00000L});
    public static final BitSet FOLLOW_typeSpecifier_in_formalArguments812 = new BitSet(new long[]{0x0000000000000820L});
    public static final BitSet FOLLOW_COMMA_in_formalArguments815 = new BitSet(new long[]{0x0000000000000000L,0x000000000FE00000L});
    public static final BitSet FOLLOW_typeSpecifier_in_formalArguments819 = new BitSet(new long[]{0x0000000000000820L});
    public static final BitSet FOLLOW_RPAREN_in_formalArguments823 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_voidKey_in_typeSpecifier855 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_shortKey_in_typeSpecifier868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_intKey_in_typeSpecifier881 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_longKey_in_typeSpecifier894 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_floatKey_in_typeSpecifier907 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_doubleKey_in_typeSpecifier920 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logKey_in_typeSpecifier933 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_registerType_in_registerDeclaration954 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_identifier_in_registerDeclaration956 = new BitSet(new long[]{0x0000000000000000L,0x0000000300000000L});
    public static final BitSet FOLLOW_intLiteralOrConst_in_registerDeclaration958 = new BitSet(new long[]{0x0000000000000000L,0x0000000300000000L});
    public static final BitSet FOLLOW_intLiteralOrConst_in_registerDeclaration960 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x000000000000000CL});
    public static final BitSet FOLLOW_bit_in_registerDeclaration962 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_registerDeclaration966 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_bit0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_inputKey_in_registerType1017 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_outputKey_in_registerType1030 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ramKey_in_registerType1043 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_LITERAL_in_intLiteralOrConst1065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_intLiteralOrConst1079 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_procKey_in_processDeclaration1094 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_processDeclaration1098 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LBRACE_in_processDeclaration1100 = new BitSet(new long[]{0x0000000000000000L,0x004030800FE00000L});
    public static final BitSet FOLLOW_variableDeclaration_in_processDeclaration1102 = new BitSet(new long[]{0x0000000000000000L,0x004030800FE00000L});
    public static final BitSet FOLLOW_stateDeclaration_in_processDeclaration1105 = new BitSet(new long[]{0x0000000000000080L,0x004030800FE00000L});
    public static final BitSet FOLLOW_RBRACE_in_processDeclaration1108 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_identifier1164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_typeSpecifier_in_variableDeclaration1193 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_identifier_in_variableDeclaration1197 = new BitSet(new long[]{0x0000000000002000L,0x0000040000000002L});
    public static final BitSet FOLLOW_portLinkList_in_variableDeclaration1201 = new BitSet(new long[]{0x0000000000002000L,0x0000040000000002L});
    public static final BitSet FOLLOW_visibleModifier_in_variableDeclaration1206 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_fromKey_in_variableDeclaration1248 = new BitSet(new long[]{0x0000000000000000L,0x00C0038060000000L});
    public static final BitSet FOLLOW_procKey_in_variableDeclaration1250 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_variableDeclaration1254 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_identifier_in_variableDeclaration1258 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_COMMA_in_variableDeclaration1261 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_identifier_in_variableDeclaration1265 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_enumDeclaration_in_variableDeclaration1306 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_variableDeclaration1312 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASSIGN_in_portLinkList1333 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LBRACE_in_portLinkList1335 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_portLink_in_portLinkList1339 = new BitSet(new long[]{0x0000000000000880L});
    public static final BitSet FOLLOW_COMMA_in_portLinkList1342 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_portLink_in_portLinkList1346 = new BitSet(new long[]{0x0000000000000880L});
    public static final BitSet FOLLOW_RBRACE_in_portLinkList1350 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_localKey_in_visibleModifier1392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forKey_in_visibleModifier1405 = new BitSet(new long[]{0x0000000000000000L,0x00C00B8060000000L});
    public static final BitSet FOLLOW_procKey_in_visibleModifier1412 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_identifier_in_visibleModifier1416 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_visibleModifier1419 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_identifier_in_visibleModifier1423 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_allKey_in_visibleModifier1455 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_portLink1491 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_portLink1493 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
    public static final BitSet FOLLOW_INTEGER_LITERAL_in_portLink1495 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RBRACKET_in_portLink1497 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stateKey_in_stateDeclaration1517 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_stateDeclaration1519 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_compoundStatement_in_stateDeclaration1521 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enumKey_in_enumDeclaration1573 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LBRACE_in_enumDeclaration1575 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_enumeratorList_in_enumDeclaration1577 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RBRACE_in_enumDeclaration1579 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enumerator_in_enumeratorList1590 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_enumeratorList1593 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_enumerator_in_enumeratorList1595 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_IDENTIFIER_in_enumerator1608 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_ASSIGN_in_enumerator1611 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_logical_or_expression_in_enumerator1613 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression1630 = new BitSet(new long[]{0x0000000001800002L});
    public static final BitSet FOLLOW_set_in_additiveExpression1634 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression1643 = new BitSet(new long[]{0x0000000001800002L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression1657 = new BitSet(new long[]{0x000000000E000002L});
    public static final BitSet FOLLOW_set_in_multiplicativeExpression1661 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression1674 = new BitSet(new long[]{0x000000000E000002L});
    public static final BitSet FOLLOW_PLUS_in_unaryExpression1688 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression1690 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_unaryExpression1706 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression1708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_unaryExpression1723 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression1725 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOGICAL_NOT_in_unaryExpression1741 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression1743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INC_in_unaryExpression1758 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_postfixExpression_in_unaryExpression1760 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEC_in_unaryExpression1775 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_postfixExpression_in_unaryExpression1777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_postfixExpression_in_unaryExpression1792 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primaryExpression_in_postfixExpression1807 = new BitSet(new long[]{0x0000000000600012L});
    public static final BitSet FOLLOW_INC_in_postfixExpression1823 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEC_in_postfixExpression1842 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_postfixExpression1861 = new BitSet(new long[]{0x0000000011E04030L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_arguments_in_postfixExpression1863 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RPAREN_in_postfixExpression1866 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_primaryExpression1894 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_primaryExpression1899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_primaryExpression1904 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_expression_in_primaryExpression1906 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RPAREN_in_primaryExpression1908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_situationExpression_in_primaryExpression1922 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGER_LITERAL_in_literal1938 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_literal1956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_literal1976 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_LITERAL_in_literal1995 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_procKey_in_situationExpression2018 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_situationExpression2022 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_inKey_in_situationExpression2024 = new BitSet(new long[]{0x0000000000000000L,0x004030800FE00000L});
    public static final BitSet FOLLOW_stateKey_in_situationExpression2026 = new BitSet(new long[]{0x0000000000000000L,0x000C000200000600L});
    public static final BitSet FOLLOW_stateIdent_in_situationExpression2030 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_stateIdent2078 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stopKey_in_stateIdent2092 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_errorKey_in_stateIdent2106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_activeKey_in_stateIdent2120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_passiveKey_in_stateIdent2134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_arguments2158 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_COMMA_in_arguments2161 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_expression_in_arguments2165 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_assignmentExpression_in_expression2201 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logical_or_expression_in_assignmentExpression2240 = new BitSet(new long[]{0x00000FFC00002002L});
    public static final BitSet FOLLOW_assignmentOperator_in_assignmentExpression2243 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_assignmentExpression_in_assignmentExpression2246 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_assignmentOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logical_or_expression_in_constantExpression2322 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logical_and_expression_in_logical_or_expression2333 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_LOGICAL_OR_in_logical_or_expression2336 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_logical_and_expression_in_logical_or_expression2339 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_inclusive_or_expression_in_logical_and_expression2352 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_LOGICAL_AND_in_logical_and_expression2355 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_inclusive_or_expression_in_logical_and_expression2358 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_exclusive_or_expression_in_inclusive_or_expression2371 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_OR_in_inclusive_or_expression2374 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_exclusive_or_expression_in_inclusive_or_expression2377 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_and_expression_in_exclusive_or_expression2390 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_MOD2_in_exclusive_or_expression2393 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_and_expression_in_exclusive_or_expression2396 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_equality_expression_in_and_expression2409 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_AND_in_and_expression2412 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_equality_expression_in_and_expression2415 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_relational_expression_in_equality_expression2427 = new BitSet(new long[]{0x0000000000060002L});
    public static final BitSet FOLLOW_set_in_equality_expression2430 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_relational_expression_in_equality_expression2437 = new BitSet(new long[]{0x0000000000060002L});
    public static final BitSet FOLLOW_shift_expression_in_relational_expression2450 = new BitSet(new long[]{0x0000000300000002L,0x0000000000000000L,0x0000000000000030L});
    public static final BitSet FOLLOW_set_in_relational_expression2453 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_shift_expression_in_relational_expression2464 = new BitSet(new long[]{0x0000000300000002L,0x0000000000000000L,0x0000000000000030L});
    public static final BitSet FOLLOW_additiveExpression_in_shift_expression2478 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000000C0L});
    public static final BitSet FOLLOW_set_in_shift_expression2481 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_additiveExpression_in_shift_expression2488 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000000C0L});
    public static final BitSet FOLLOW_labeledStatement_in_statement2503 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_compoundStatement_in_statement2508 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionStatement_in_statement2513 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ifStatement_in_statement2518 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_switchStatement_in_statement2523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iterationStatement_in_statement2528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_jumpStatement_in_statement2533 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_controlProcessStatement_in_statement2538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_passStatement_in_statement2543 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_timeoutStatement_in_statement2548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_caseKey_in_labeledStatement2559 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_constantExpression_in_labeledStatement2562 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_labeledStatement2564 = new BitSet(new long[]{0x0000000011E06450L,0x00C3079F60009FBAL});
    public static final BitSet FOLLOW_statement_in_labeledStatement2567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defaultKey_in_labeledStatement2572 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_COLON_in_labeledStatement2575 = new BitSet(new long[]{0x0000000011E06450L,0x00C3079F60009FBAL});
    public static final BitSet FOLLOW_statement_in_labeledStatement2578 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_compoundStatement2589 = new BitSet(new long[]{0x0000000011E064D0L,0x00C3079F60009FBAL});
    public static final BitSet FOLLOW_statement_in_compoundStatement2591 = new BitSet(new long[]{0x0000000011E064D0L,0x00C3079F60009FBAL});
    public static final BitSet FOLLOW_RBRACE_in_compoundStatement2594 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMI_in_expressionStatement2621 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_expressionStatement2627 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_expressionStatement2629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ifKey_in_ifStatement2641 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_LPAREN_in_ifStatement2643 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_expression_in_ifStatement2645 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RPAREN_in_ifStatement2647 = new BitSet(new long[]{0x0000000011E06450L,0x00C3079F60009FBAL});
    public static final BitSet FOLLOW_statement_in_ifStatement2651 = new BitSet(new long[]{0x0000000000000002L,0x0000400000000000L});
    public static final BitSet FOLLOW_elseKey_in_ifStatement2659 = new BitSet(new long[]{0x0000000011E06450L,0x00C3079F60009FBAL});
    public static final BitSet FOLLOW_statement_in_ifStatement2663 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_switchKey_in_switchStatement2699 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_LPAREN_in_switchStatement2701 = new BitSet(new long[]{0x0000000011E04010L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_expression_in_switchStatement2705 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RPAREN_in_switchStatement2707 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_switchBlockLabels_in_switchStatement2711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_switchBlockLabels2742 = new BitSet(new long[]{0x0000000000000000L,0x0003000000000000L});
    public static final BitSet FOLLOW_labeledStatement_in_switchBlockLabels2745 = new BitSet(new long[]{0x0000000000000080L,0x0003000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_switchBlockLabels2748 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forKey_in_iterationStatement2765 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_LPAREN_in_iterationStatement2767 = new BitSet(new long[]{0x0000000011E04410L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_expressionStatement_in_iterationStatement2771 = new BitSet(new long[]{0x0000000011E04410L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_expressionStatement_in_iterationStatement2775 = new BitSet(new long[]{0x0000000011E04030L,0x00C0039F60000000L});
    public static final BitSet FOLLOW_expression_in_iterationStatement2779 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RPAREN_in_iterationStatement2782 = new BitSet(new long[]{0x0000000011E06450L,0x00C3079F60009FBAL});
    public static final BitSet FOLLOW_statement_in_iterationStatement2784 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_contKey_in_jumpStatement2814 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_breakKey_in_jumpStatement2827 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_jumpStatement2841 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stopKey_in_controlProcessStatement2856 = new BitSet(new long[]{0x0000000000000400L,0x00C0038060000000L});
    public static final BitSet FOLLOW_procKey_in_controlProcessStatement2860 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_controlProcessStatement2864 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_errorKey_in_controlProcessStatement2928 = new BitSet(new long[]{0x0000000000000400L,0x00C0038060000000L});
    public static final BitSet FOLLOW_procKey_in_controlProcessStatement2931 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_controlProcessStatement2935 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_startKey_in_controlProcessStatement3003 = new BitSet(new long[]{0x0000000000000000L,0x00C0038060000000L});
    public static final BitSet FOLLOW_procKey_in_controlProcessStatement3005 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_controlProcessStatement3009 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_contKey_in_controlProcessStatement3041 = new BitSet(new long[]{0x0000000000000000L,0x00C0038060000000L});
    public static final BitSet FOLLOW_procKey_in_controlProcessStatement3043 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_controlProcessStatement3047 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_controlProcessStatement3084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_inKey_in_passStatement3115 = new BitSet(new long[]{0x0000000000000000L,0x0040B0800FE00000L});
    public static final BitSet FOLLOW_stateKey_in_passStatement3123 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_passStatement3127 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_nextKey_in_passStatement3163 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_loopKey_in_passStatement3196 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_passStatement3215 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_timeoutKey_in_timeoutStatement3237 = new BitSet(new long[]{0x0000000000000000L,0x0000000300000000L});
    public static final BitSet FOLLOW_intLiteralOrConst_in_timeoutStatement3241 = new BitSet(new long[]{0x0000000011E06450L,0x00C3079F60009FBAL});
    public static final BitSet FOLLOW_statement_in_timeoutStatement3245 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PROGR_in_progrKey3285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TACT_in_tactKey3305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONST_in_constKey3325 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INPUT_in_inputKey3349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OUTPUT_in_outputKey3369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PROC_in_procKey3390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOCAL_in_localKey3410 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forKey3431 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALL_in_allKey3452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_fromKey3473 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOG_in_logKey3494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_intKey3515 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STATE_in_stateKey3535 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STOP_in_stopKey3556 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONT_in_contKey3577 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_START_in_startKey3597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TIMEOUT_in_timeoutKey3617 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_ifKey3638 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELSE_in_elseKey3659 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_inKey3680 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEXT_in_nextKey3700 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RAM_in_ramKey3720 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ERROR_in_errorKey3740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SHORT_in_shortKey3760 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LONG_in_longKey3780 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SWITCH_in_switchKey3800 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CASE_in_caseKey3820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BREAK_in_breakKey3840 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_floatKey3860 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_in_doubleKey3880 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEFAULT_in_defaultKey3900 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ACTIVE_in_activeKey3920 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PASSIVE_in_passiveKey3940 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOOP_in_loopKey3964 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SIGNED_in_signedKey3984 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNSIGNED_in_unsignedKey4004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ENUM_in_enumKey4024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FUNCTION_in_functionKey4044 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VOID_in_voidKey4068 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CMARK_in_cMarkKey4088 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_jumpStatement_in_synpred85_Reflex2533 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_controlProcessStatement_in_synpred86_Reflex2538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_elseKey_in_synpred91_Reflex2655 = new BitSet(new long[]{0x0000000000000002L});

}

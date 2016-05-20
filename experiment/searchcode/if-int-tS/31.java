// $ANTLR 3.4 C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g 2012-01-29 03:29:52

	package com.icona.antlr.main;  
	import com.icona.tree.nodes.BasicModifier;
	import com.icona.tree.nodes.Declaration;
	import com.icona.tree.nodes.Expression;
	import com.icona.tree.nodes.Expression.AssignmentExpression;
	import com.icona.tree.nodes.Expression.BinaryExpression;
	import com.icona.tree.nodes.Expression.ListExpression;
	import com.icona.tree.nodes.Expression.PostfixExpression;
	import com.icona.tree.nodes.Expression.PrimaryExpression;
	import com.icona.tree.nodes.Expression.TertiaryExpression;
	import com.icona.tree.nodes.Expression.UnaryExpression;
	import com.icona.tree.nodes.GenericRef.RefType;
	import com.icona.tree.nodes.Identifier;
	import com.icona.tree.nodes.Node;
	import com.icona.tree.nodes.Operator.AssignmentOperator;
	import com.icona.tree.nodes.Operator.AssignmentOperator.AssignmentSymbol;
	import com.icona.tree.nodes.Operator.BinaryOperator;
	import com.icona.tree.nodes.Operator.BinaryOperator.BinarySymbol;
	import com.icona.tree.nodes.Operator.UnaryOperator;
	import com.icona.tree.nodes.Operator.UnaryOperator.UnarySymbol;
	import com.icona.tree.nodes.SourceFile;
	import com.icona.tree.nodes.Specifier;
	import com.icona.tree.nodes.Statement;
	import com.icona.tree.nodes.Statement.ListDeclaration;
	import com.icona.tree.nodes.Type;
	import com.icona.tree.nodes.Type.ArrayType;
	import com.icona.tree.nodes.Type.BasicDataType;
	import com.icona.tree.nodes.MethodType;
	import com.icona.tree.nodes.FunctionDefinition;
	import com.icona.tree.nodes.Protocol;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class ObjectiveCParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "CHARACTER_LITERAL", "DECIMAL_LITERAL", "EscapeSequence", "Exponent", "FLOATING_POINT_LITERAL", "FloatTypeSuffix", "HEX_LITERAL", "HexDigit", "IDENTIFIER", "IntegerTypeSuffix", "LETTER", "OCTAL_LITERAL", "OctalEscape", "STRING_LITERAL", "UnicodeEscape", "WS", "'!'", "'!='", "'%'", "'%='", "'&&'", "'&'", "'&='", "'('", "')'", "'*'", "'*='", "'+'", "'++'", "'+='", "','", "'-'", "'--'", "'-='", "'...'", "'/'", "'/='", "':'", "';'", "'<'", "'<<'", "'<<='", "'<='", "'='", "'=='", "'>'", "'>='", "'>>'", "'>>='", "'?'", "'@end'", "'@implementation'", "'@interface'", "'@optional'", "'@package'", "'@private'", "'@protected'", "'@protocol'", "'@public'", "'@required'", "'BOOL'", "'['", "']'", "'^'", "'^='", "'auto'", "'case'", "'char'", "'const'", "'default'", "'do'", "'double'", "'else'", "'extern'", "'float'", "'for'", "'if'", "'int'", "'long'", "'register'", "'short'", "'signed'", "'static'", "'switch'", "'unichar'", "'unsigned'", "'void'", "'volatile'", "'while'", "'{'", "'|'", "'|='", "'||'", "'}'", "'~'"
    };

    public static final int EOF=-1;
    public static final int T__20=20;
    public static final int T__21=21;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int T__24=24;
    public static final int T__25=25;
    public static final int T__26=26;
    public static final int T__27=27;
    public static final int T__28=28;
    public static final int T__29=29;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int T__50=50;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__59=59;
    public static final int T__60=60;
    public static final int T__61=61;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int T__64=64;
    public static final int T__65=65;
    public static final int T__66=66;
    public static final int T__67=67;
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__70=70;
    public static final int T__71=71;
    public static final int T__72=72;
    public static final int T__73=73;
    public static final int T__74=74;
    public static final int T__75=75;
    public static final int T__76=76;
    public static final int T__77=77;
    public static final int T__78=78;
    public static final int T__79=79;
    public static final int T__80=80;
    public static final int T__81=81;
    public static final int T__82=82;
    public static final int T__83=83;
    public static final int T__84=84;
    public static final int T__85=85;
    public static final int T__86=86;
    public static final int T__87=87;
    public static final int T__88=88;
    public static final int T__89=89;
    public static final int T__90=90;
    public static final int T__91=91;
    public static final int T__92=92;
    public static final int T__93=93;
    public static final int T__94=94;
    public static final int T__95=95;
    public static final int T__96=96;
    public static final int T__97=97;
    public static final int T__98=98;
    public static final int CHARACTER_LITERAL=4;
    public static final int DECIMAL_LITERAL=5;
    public static final int EscapeSequence=6;
    public static final int Exponent=7;
    public static final int FLOATING_POINT_LITERAL=8;
    public static final int FloatTypeSuffix=9;
    public static final int HEX_LITERAL=10;
    public static final int HexDigit=11;
    public static final int IDENTIFIER=12;
    public static final int IntegerTypeSuffix=13;
    public static final int LETTER=14;
    public static final int OCTAL_LITERAL=15;
    public static final int OctalEscape=16;
    public static final int STRING_LITERAL=17;
    public static final int UnicodeEscape=18;
    public static final int WS=19;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public ObjectiveCParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public ObjectiveCParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String[] getTokenNames() { return ObjectiveCParser.tokenNames; }
    public String getGrammarFileName() { return "C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g"; }


    	SourceFile src=new SourceFile(null,-1);
    	
    	public SourceFile getSrc(){
    		return src;
    	}
    	private Declaration mergeAndCreateDeclaration(Declaration declaration,Identifier identifier,ArrayType arrayType, Expression initializations){
    		Declaration decl=(Declaration)declaration.clone();
    		decl.setIdentifier(identifier);
    		decl.setArrayDimensions(arrayType);
    		decl.setListExpression(initializations);
    		
    		return decl;
    	}
    	
    	private BinaryExpression createAndMergeBinaryExpression(Expression parent,int line_no,BinarySymbol op,Expression prev,BinaryExpression addTo,Expression newExpression){
    		BinaryExpression temp=new BinaryExpression(parent,line_no,op);
    		
    		temp.setLhs((Expression)prev);
    		temp.setRhs((Expression)newExpression);
    		addTo.setRhs((Expression)temp);
    		
    		return temp;
    	}
    	
    	private AssignmentExpression createAndMergeAssignmentExpression(Expression parent,int line_no,AssignmentSymbol op,Expression prev,AssignmentExpression addTo,Expression newExpression){
    		AssignmentExpression temp=new AssignmentExpression(parent,line_no,op);
    		
    		temp.setTarget((Expression)prev);
    		temp.setValue((Expression)newExpression);
    		addTo.setValue((Expression)temp);
    		
    		return temp;
    	}



    // $ANTLR start "translation_unit"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:84:1: translation_unit : (ed= external_declaration )+ EOF ;
    public final void translation_unit() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:85:2: ( (ed= external_declaration )+ EOF )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:86:3: (ed= external_declaration )+ EOF
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:86:3: (ed= external_declaration )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0 >= CHARACTER_LITERAL && LA1_0 <= DECIMAL_LITERAL)||LA1_0==FLOATING_POINT_LITERAL||LA1_0==HEX_LITERAL||LA1_0==IDENTIFIER||LA1_0==OCTAL_LITERAL||LA1_0==STRING_LITERAL||LA1_0==20||LA1_0==25||LA1_0==27||LA1_0==29||LA1_0==32||(LA1_0 >= 35 && LA1_0 <= 36)||LA1_0==42||(LA1_0 >= 55 && LA1_0 <= 56)||LA1_0==61||LA1_0==64||(LA1_0 >= 69 && LA1_0 <= 75)||(LA1_0 >= 77 && LA1_0 <= 93)||LA1_0==98) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:87:4: ed= external_declaration
            	    {
            	    pushFollow(FOLLOW_external_declaration_in_translation_unit66);
            	    external_declaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            match(input,EOF,FOLLOW_EOF_in_translation_unit74); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "translation_unit"



    // $ANTLR start "external_declaration"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:90:1: external_declaration : (d= declaration |stmt= statement | class_interface | class_implementation | protocol_declaration | protocol_declaration_list );
    public final void external_declaration() throws RecognitionException {
        ListDeclaration d =null;

        Statement stmt =null;


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:92:2: (d= declaration |stmt= statement | class_interface | class_implementation | protocol_declaration | protocol_declaration_list )
            int alt2=6;
            switch ( input.LA(1) ) {
            case 69:
                {
                int LA2_1 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 1, input);

                    throw nvae;

                }
                }
                break;
            case 83:
                {
                int LA2_2 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 2, input);

                    throw nvae;

                }
                }
                break;
            case 86:
                {
                int LA2_3 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 3, input);

                    throw nvae;

                }
                }
                break;
            case 77:
                {
                int LA2_4 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 4, input);

                    throw nvae;

                }
                }
                break;
            case 90:
                {
                int LA2_5 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 5, input);

                    throw nvae;

                }
                }
                break;
            case 71:
                {
                int LA2_6 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 6, input);

                    throw nvae;

                }
                }
                break;
            case 84:
                {
                int LA2_7 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 7, input);

                    throw nvae;

                }
                }
                break;
            case 82:
                {
                int LA2_8 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 8, input);

                    throw nvae;

                }
                }
                break;
            case 81:
                {
                int LA2_9 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 9, input);

                    throw nvae;

                }
                }
                break;
            case 78:
                {
                int LA2_10 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 10, input);

                    throw nvae;

                }
                }
                break;
            case 75:
                {
                int LA2_11 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 11, input);

                    throw nvae;

                }
                }
                break;
            case 64:
                {
                int LA2_12 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 12, input);

                    throw nvae;

                }
                }
                break;
            case 85:
                {
                int LA2_13 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 13, input);

                    throw nvae;

                }
                }
                break;
            case 89:
                {
                int LA2_14 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 14, input);

                    throw nvae;

                }
                }
                break;
            case 88:
                {
                int LA2_15 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 15, input);

                    throw nvae;

                }
                }
                break;
            case 72:
                {
                int LA2_16 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 16, input);

                    throw nvae;

                }
                }
                break;
            case 91:
                {
                int LA2_17 = input.LA(2);

                if ( (synpred2_ObjectiveC()) ) {
                    alt2=1;
                }
                else if ( (synpred3_ObjectiveC()) ) {
                    alt2=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 17, input);

                    throw nvae;

                }
                }
                break;
            case CHARACTER_LITERAL:
            case DECIMAL_LITERAL:
            case FLOATING_POINT_LITERAL:
            case HEX_LITERAL:
            case IDENTIFIER:
            case OCTAL_LITERAL:
            case STRING_LITERAL:
            case 20:
            case 25:
            case 27:
            case 29:
            case 32:
            case 35:
            case 36:
            case 42:
            case 70:
            case 73:
            case 74:
            case 79:
            case 80:
            case 87:
            case 92:
            case 93:
            case 98:
                {
                alt2=2;
                }
                break;
            case 56:
                {
                alt2=3;
                }
                break;
            case 55:
                {
                alt2=4;
                }
                break;
            case 61:
                {
                int LA2_40 = input.LA(2);

                if ( (synpred6_ObjectiveC()) ) {
                    alt2=5;
                }
                else if ( (true) ) {
                    alt2=6;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 40, input);

                    throw nvae;

                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }

            switch (alt2) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:93:3: d= declaration
                    {
                    pushFollow(FOLLOW_declaration_in_external_declaration89);
                    d=declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:94:5: stmt= statement
                    {
                    pushFollow(FOLLOW_statement_in_external_declaration97);
                    stmt=statement();

                    state._fsp--;
                    if (state.failed) return ;

                    if ( state.backtracking==0 ) {src.addChild((Statement)stmt);}

                    }
                    break;
                case 3 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:95:5: class_interface
                    {
                    pushFollow(FOLLOW_class_interface_in_external_declaration106);
                    class_interface();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:96:5: class_implementation
                    {
                    pushFollow(FOLLOW_class_implementation_in_external_declaration112);
                    class_implementation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:97:5: protocol_declaration
                    {
                    pushFollow(FOLLOW_protocol_declaration_in_external_declaration119);
                    protocol_declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:98:8: protocol_declaration_list
                    {
                    pushFollow(FOLLOW_protocol_declaration_list_in_external_declaration128);
                    protocol_declaration_list();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "external_declaration"



    // $ANTLR start "class_name"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:102:1: class_name : IDENTIFIER ;
    public final void class_name() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:102:11: ( IDENTIFIER )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:103:2: IDENTIFIER
            {
            match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_class_name141); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "class_name"



    // $ANTLR start "superclass_name"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:105:1: superclass_name : IDENTIFIER ;
    public final void superclass_name() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:105:16: ( IDENTIFIER )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:106:2: IDENTIFIER
            {
            match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_superclass_name149); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "superclass_name"



    // $ANTLR start "class_interface"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:112:1: class_interface : '@interface' ( class_name ( ':' superclass_name )? ( instance_variables )? ( interface_declaration_list )? ) '@end' ;
    public final void class_interface() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:112:16: ( '@interface' ( class_name ( ':' superclass_name )? ( instance_variables )? ( interface_declaration_list )? ) '@end' )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:114:2: '@interface' ( class_name ( ':' superclass_name )? ( instance_variables )? ( interface_declaration_list )? ) '@end'
            {
            match(input,56,FOLLOW_56_in_class_interface167); if (state.failed) return ;

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:115:2: ( class_name ( ':' superclass_name )? ( instance_variables )? ( interface_declaration_list )? )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:116:2: class_name ( ':' superclass_name )? ( instance_variables )? ( interface_declaration_list )?
            {
            pushFollow(FOLLOW_class_name_in_class_interface173);
            class_name();

            state._fsp--;
            if (state.failed) return ;

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:116:13: ( ':' superclass_name )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==41) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:116:14: ':' superclass_name
                    {
                    match(input,41,FOLLOW_41_in_class_interface176); if (state.failed) return ;

                    pushFollow(FOLLOW_superclass_name_in_class_interface178);
                    superclass_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:118:2: ( instance_variables )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==93) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:118:4: instance_variables
                    {
                    pushFollow(FOLLOW_instance_variables_in_class_interface187);
                    instance_variables();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:119:2: ( interface_declaration_list )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==31||LA5_0==35||LA5_0==64||LA5_0==69||(LA5_0 >= 71 && LA5_0 <= 72)||LA5_0==75||(LA5_0 >= 77 && LA5_0 <= 78)||(LA5_0 >= 81 && LA5_0 <= 86)||(LA5_0 >= 88 && LA5_0 <= 91)) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:119:4: interface_declaration_list
                    {
                    pushFollow(FOLLOW_interface_declaration_list_in_class_interface195);
                    interface_declaration_list();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }


            match(input,54,FOLLOW_54_in_class_interface204); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "class_interface"



    // $ANTLR start "interface_declaration_list"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:123:1: interface_declaration_list : ( declaration | class_method_declaration | instance_method_declaration )+ ;
    public final void interface_declaration_list() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:123:27: ( ( declaration | class_method_declaration | instance_method_declaration )+ )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:124:2: ( declaration | class_method_declaration | instance_method_declaration )+
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:124:2: ( declaration | class_method_declaration | instance_method_declaration )+
            int cnt6=0;
            loop6:
            do {
                int alt6=4;
                switch ( input.LA(1) ) {
                case 69:
                    {
                    int LA6_2 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 83:
                    {
                    int LA6_3 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 86:
                    {
                    int LA6_4 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 77:
                    {
                    int LA6_5 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 90:
                    {
                    int LA6_6 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 71:
                    {
                    int LA6_7 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 84:
                    {
                    int LA6_8 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 82:
                    {
                    int LA6_9 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 81:
                    {
                    int LA6_10 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 78:
                    {
                    int LA6_11 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 75:
                    {
                    int LA6_12 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 64:
                    {
                    int LA6_13 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 85:
                    {
                    int LA6_14 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 89:
                    {
                    int LA6_15 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 88:
                    {
                    int LA6_16 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 72:
                    {
                    int LA6_17 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 91:
                    {
                    int LA6_18 = input.LA(2);

                    if ( (synpred10_ObjectiveC()) ) {
                        alt6=1;
                    }


                    }
                    break;
                case 31:
                    {
                    int LA6_19 = input.LA(2);

                    if ( (synpred11_ObjectiveC()) ) {
                        alt6=2;
                    }


                    }
                    break;
                case 35:
                    {
                    int LA6_20 = input.LA(2);

                    if ( (synpred12_ObjectiveC()) ) {
                        alt6=3;
                    }


                    }
                    break;

                }

                switch (alt6) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:124:3: declaration
            	    {
            	    pushFollow(FOLLOW_declaration_in_interface_declaration_list214);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:124:17: class_method_declaration
            	    {
            	    pushFollow(FOLLOW_class_method_declaration_in_interface_declaration_list218);
            	    class_method_declaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 3 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:124:44: instance_method_declaration
            	    {
            	    pushFollow(FOLLOW_instance_method_declaration_in_interface_declaration_list222);
            	    instance_method_declaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "interface_declaration_list"



    // $ANTLR start "class_method_declaration"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:127:1: class_method_declaration : ( '+' method_declaration ) ;
    public final void class_method_declaration() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:127:25: ( ( '+' method_declaration ) )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:128:2: ( '+' method_declaration )
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:128:2: ( '+' method_declaration )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:128:3: '+' method_declaration
            {
            match(input,31,FOLLOW_31_in_class_method_declaration237); if (state.failed) return ;

            pushFollow(FOLLOW_method_declaration_in_class_method_declaration239);
            method_declaration();

            state._fsp--;
            if (state.failed) return ;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "class_method_declaration"



    // $ANTLR start "instance_method_declaration"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:131:1: instance_method_declaration : ( '-' method_declaration ) ;
    public final void instance_method_declaration() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:131:28: ( ( '-' method_declaration ) )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:132:2: ( '-' method_declaration )
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:132:2: ( '-' method_declaration )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:132:3: '-' method_declaration
            {
            match(input,35,FOLLOW_35_in_instance_method_declaration251); if (state.failed) return ;

            pushFollow(FOLLOW_method_declaration_in_instance_method_declaration253);
            method_declaration();

            state._fsp--;
            if (state.failed) return ;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "instance_method_declaration"



    // $ANTLR start "method_declaration"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:135:1: method_declaration : ( method_type )? method_selector ';' ;
    public final void method_declaration() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:135:19: ( ( method_type )? method_selector ';' )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:136:2: ( method_type )? method_selector ';'
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:136:2: ( method_type )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==27) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:136:4: method_type
                    {
                    pushFollow(FOLLOW_method_type_in_method_declaration266);
                    method_type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            pushFollow(FOLLOW_method_selector_in_method_declaration271);
            method_selector();

            state._fsp--;
            if (state.failed) return ;

            match(input,42,FOLLOW_42_in_method_declaration273); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "method_declaration"



    // $ANTLR start "instance_variables"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:139:1: instance_variables : '{' instance_variable_declaration '}' ;
    public final void instance_variables() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:139:19: ( '{' instance_variable_declaration '}' )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:140:2: '{' instance_variable_declaration '}'
            {
            match(input,93,FOLLOW_93_in_instance_variables284); if (state.failed) return ;

            pushFollow(FOLLOW_instance_variable_declaration_in_instance_variables286);
            instance_variable_declaration();

            state._fsp--;
            if (state.failed) return ;

            match(input,97,FOLLOW_97_in_instance_variables288); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "instance_variables"



    // $ANTLR start "instance_variable_declaration"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:143:1: instance_variable_declaration : ( visibility_specification | struct_declarator_list instance_variables )+ ;
    public final void instance_variable_declaration() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:143:30: ( ( visibility_specification | struct_declarator_list instance_variables )+ )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:144:2: ( visibility_specification | struct_declarator_list instance_variables )+
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:144:2: ( visibility_specification | struct_declarator_list instance_variables )+
            int cnt8=0;
            loop8:
            do {
                int alt8=3;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0 >= 58 && LA8_0 <= 60)||LA8_0==62) ) {
                    alt8=1;
                }
                else if ( (LA8_0==IDENTIFIER||LA8_0==41) ) {
                    alt8=2;
                }


                switch (alt8) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:144:3: visibility_specification
            	    {
            	    pushFollow(FOLLOW_visibility_specification_in_instance_variable_declaration300);
            	    visibility_specification();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:144:30: struct_declarator_list instance_variables
            	    {
            	    pushFollow(FOLLOW_struct_declarator_list_in_instance_variable_declaration304);
            	    struct_declarator_list();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    pushFollow(FOLLOW_instance_variables_in_instance_variable_declaration306);
            	    instance_variables();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt8 >= 1 ) break loop8;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        throw eee;
                }
                cnt8++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "instance_variable_declaration"



    // $ANTLR start "visibility_specification"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:147:1: visibility_specification : ( '@private' | '@protected' | '@package' | '@public' );
    public final void visibility_specification() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:147:25: ( '@private' | '@protected' | '@package' | '@public' )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:
            {
            if ( (input.LA(1) >= 58 && input.LA(1) <= 60)||input.LA(1)==62 ) {
                input.consume();
                state.errorRecovery=false;
                state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "visibility_specification"



    // $ANTLR start "struct_declarator_list"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:154:1: struct_declarator_list : struct_declarator ( ',' struct_declarator )* ;
    public final void struct_declarator_list() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:154:24: ( struct_declarator ( ',' struct_declarator )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:154:26: struct_declarator ( ',' struct_declarator )*
            {
            pushFollow(FOLLOW_struct_declarator_in_struct_declarator_list344);
            struct_declarator();

            state._fsp--;
            if (state.failed) return ;

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:154:44: ( ',' struct_declarator )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==34) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:154:45: ',' struct_declarator
            	    {
            	    match(input,34,FOLLOW_34_in_struct_declarator_list347); if (state.failed) return ;

            	    pushFollow(FOLLOW_struct_declarator_in_struct_declarator_list349);
            	    struct_declarator();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "struct_declarator_list"



    // $ANTLR start "struct_declarator"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:156:1: struct_declarator : ( declarator | ( declarator )? ':' constant );
    public final void struct_declarator() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:156:19: ( declarator | ( declarator )? ':' constant )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==IDENTIFIER) ) {
                int LA11_1 = input.LA(2);

                if ( (synpred20_ObjectiveC()) ) {
                    alt11=1;
                }
                else if ( (true) ) {
                    alt11=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 11, 1, input);

                    throw nvae;

                }
            }
            else if ( (LA11_0==41) ) {
                alt11=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;

            }
            switch (alt11) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:156:21: declarator
                    {
                    pushFollow(FOLLOW_declarator_in_struct_declarator360);
                    declarator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:156:34: ( declarator )? ':' constant
                    {
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:156:34: ( declarator )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==IDENTIFIER) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:156:34: declarator
                            {
                            pushFollow(FOLLOW_declarator_in_struct_declarator364);
                            declarator();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    match(input,41,FOLLOW_41_in_struct_declarator367); if (state.failed) return ;

                    pushFollow(FOLLOW_constant_in_struct_declarator369);
                    constant();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "struct_declarator"



    // $ANTLR start "class_implementation"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:158:1: class_implementation : '@implementation' ( class_name ( ':' superclass_name )? ( implementation_definition_list )? ) '@end' ;
    public final void class_implementation() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:158:21: ( '@implementation' ( class_name ( ':' superclass_name )? ( implementation_definition_list )? ) '@end' )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:159:2: '@implementation' ( class_name ( ':' superclass_name )? ( implementation_definition_list )? ) '@end'
            {
            match(input,55,FOLLOW_55_in_class_implementation378); if (state.failed) return ;

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:160:2: ( class_name ( ':' superclass_name )? ( implementation_definition_list )? )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:161:2: class_name ( ':' superclass_name )? ( implementation_definition_list )?
            {
            pushFollow(FOLLOW_class_name_in_class_implementation384);
            class_name();

            state._fsp--;
            if (state.failed) return ;

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:161:13: ( ':' superclass_name )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==41) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:161:15: ':' superclass_name
                    {
                    match(input,41,FOLLOW_41_in_class_implementation388); if (state.failed) return ;

                    pushFollow(FOLLOW_superclass_name_in_class_implementation390);
                    superclass_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:162:2: ( implementation_definition_list )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==31||LA13_0==35||LA13_0==64||LA13_0==69||(LA13_0 >= 71 && LA13_0 <= 72)||LA13_0==75||(LA13_0 >= 77 && LA13_0 <= 78)||(LA13_0 >= 81 && LA13_0 <= 86)||(LA13_0 >= 88 && LA13_0 <= 91)) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:162:4: implementation_definition_list
                    {
                    pushFollow(FOLLOW_implementation_definition_list_in_class_implementation398);
                    implementation_definition_list();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }


            match(input,54,FOLLOW_54_in_class_implementation407); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "class_implementation"



    // $ANTLR start "implementation_definition_list"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:167:1: implementation_definition_list : ( function_definition | declaration | class_method_definition | instance_method_definition )+ ;
    public final void implementation_definition_list() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:167:31: ( ( function_definition | declaration | class_method_definition | instance_method_definition )+ )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:168:2: ( function_definition | declaration | class_method_definition | instance_method_definition )+
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:168:2: ( function_definition | declaration | class_method_definition | instance_method_definition )+
            int cnt14=0;
            loop14:
            do {
                int alt14=5;
                switch ( input.LA(1) ) {
                case 69:
                    {
                    int LA14_3 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 83:
                    {
                    int LA14_4 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 86:
                    {
                    int LA14_5 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 77:
                    {
                    int LA14_6 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 90:
                    {
                    int LA14_7 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 71:
                    {
                    int LA14_8 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 84:
                    {
                    int LA14_9 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 82:
                    {
                    int LA14_10 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 81:
                    {
                    int LA14_11 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 78:
                    {
                    int LA14_12 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 75:
                    {
                    int LA14_13 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 64:
                    {
                    int LA14_14 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 85:
                    {
                    int LA14_15 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 89:
                    {
                    int LA14_16 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 88:
                    {
                    int LA14_17 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 72:
                    {
                    int LA14_18 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 91:
                    {
                    int LA14_19 = input.LA(2);

                    if ( (synpred24_ObjectiveC()) ) {
                        alt14=1;
                    }
                    else if ( (synpred25_ObjectiveC()) ) {
                        alt14=2;
                    }


                    }
                    break;
                case 31:
                    {
                    alt14=3;
                    }
                    break;
                case 35:
                    {
                    alt14=4;
                    }
                    break;

                }

                switch (alt14) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:168:3: function_definition
            	    {
            	    pushFollow(FOLLOW_function_definition_in_implementation_definition_list419);
            	    function_definition();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:168:25: declaration
            	    {
            	    pushFollow(FOLLOW_declaration_in_implementation_definition_list423);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 3 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:168:39: class_method_definition
            	    {
            	    pushFollow(FOLLOW_class_method_definition_in_implementation_definition_list427);
            	    class_method_definition();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 4 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:168:65: instance_method_definition
            	    {
            	    pushFollow(FOLLOW_instance_method_definition_in_implementation_definition_list431);
            	    instance_method_definition();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt14 >= 1 ) break loop14;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(14, input);
                        throw eee;
                }
                cnt14++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "implementation_definition_list"



    // $ANTLR start "class_method_definition"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:170:1: class_method_definition : ( '+' method_definition ) ;
    public final void class_method_definition() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:170:24: ( ( '+' method_definition ) )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:171:2: ( '+' method_definition )
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:171:2: ( '+' method_definition )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:171:3: '+' method_definition
            {
            match(input,31,FOLLOW_31_in_class_method_definition442); if (state.failed) return ;

            pushFollow(FOLLOW_method_definition_in_class_method_definition444);
            method_definition();

            state._fsp--;
            if (state.failed) return ;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "class_method_definition"



    // $ANTLR start "instance_method_definition"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:174:1: instance_method_definition : ( '-' method_definition ) ;
    public final void instance_method_definition() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:174:27: ( ( '-' method_definition ) )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:175:2: ( '-' method_definition )
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:175:2: ( '-' method_definition )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:175:3: '-' method_definition
            {
            match(input,35,FOLLOW_35_in_instance_method_definition456); if (state.failed) return ;

            pushFollow(FOLLOW_method_definition_in_instance_method_definition458);
            method_definition();

            state._fsp--;
            if (state.failed) return ;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "instance_method_definition"



    // $ANTLR start "method_definition"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:178:1: method_definition : ( method_type )? method_selector compound_statement ;
    public final void method_definition() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:178:18: ( ( method_type )? method_selector compound_statement )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:179:2: ( method_type )? method_selector compound_statement
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:179:2: ( method_type )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==27) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:179:3: method_type
                    {
                    pushFollow(FOLLOW_method_type_in_method_definition471);
                    method_type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            pushFollow(FOLLOW_method_selector_in_method_definition475);
            method_selector();

            state._fsp--;
            if (state.failed) return ;

            pushFollow(FOLLOW_compound_statement_in_method_definition479);
            compound_statement();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "method_definition"



    // $ANTLR start "method_selector"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:181:1: method_selector : ( selector | ( ( keyword_declarator )+ ( parameter_list )? ) );
    public final void method_selector() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:181:16: ( selector | ( ( keyword_declarator )+ ( parameter_list )? ) )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==IDENTIFIER) ) {
                int LA18_1 = input.LA(2);

                if ( (LA18_1==42||LA18_1==93) ) {
                    alt18=1;
                }
                else if ( (LA18_1==41) ) {
                    alt18=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 1, input);

                    throw nvae;

                }
            }
            else if ( (LA18_0==41) ) {
                alt18=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;

            }
            switch (alt18) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:182:2: selector
                    {
                    pushFollow(FOLLOW_selector_in_method_selector489);
                    selector();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:182:12: ( ( keyword_declarator )+ ( parameter_list )? )
                    {
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:182:12: ( ( keyword_declarator )+ ( parameter_list )? )
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:182:13: ( keyword_declarator )+ ( parameter_list )?
                    {
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:182:13: ( keyword_declarator )+
                    int cnt16=0;
                    loop16:
                    do {
                        int alt16=2;
                        int LA16_0 = input.LA(1);

                        if ( (LA16_0==IDENTIFIER||LA16_0==41) ) {
                            alt16=1;
                        }


                        switch (alt16) {
                    	case 1 :
                    	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:182:13: keyword_declarator
                    	    {
                    	    pushFollow(FOLLOW_keyword_declarator_in_method_selector493);
                    	    keyword_declarator();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt16 >= 1 ) break loop16;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(16, input);
                                throw eee;
                        }
                        cnt16++;
                    } while (true);


                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:182:33: ( parameter_list )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==64||LA17_0==69||(LA17_0 >= 71 && LA17_0 <= 72)||LA17_0==75||(LA17_0 >= 77 && LA17_0 <= 78)||(LA17_0 >= 81 && LA17_0 <= 86)||(LA17_0 >= 88 && LA17_0 <= 91)) ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:182:34: parameter_list
                            {
                            pushFollow(FOLLOW_parameter_list_in_method_selector497);
                            parameter_list();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "method_selector"



    // $ANTLR start "keyword_declarator"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:185:1: keyword_declarator : ( selector )? ':' ( method_type )* IDENTIFIER ;
    public final void keyword_declarator() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:185:19: ( ( selector )? ':' ( method_type )* IDENTIFIER )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:186:2: ( selector )? ':' ( method_type )* IDENTIFIER
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:186:2: ( selector )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==IDENTIFIER) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:186:2: selector
                    {
                    pushFollow(FOLLOW_selector_in_keyword_declarator511);
                    selector();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            match(input,41,FOLLOW_41_in_keyword_declarator514); if (state.failed) return ;

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:186:16: ( method_type )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==27) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:186:16: method_type
            	    {
            	    pushFollow(FOLLOW_method_type_in_keyword_declarator516);
            	    method_type();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);


            match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_keyword_declarator519); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "keyword_declarator"



    // $ANTLR start "parameter_list"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:189:1: parameter_list : parameter_declaration_list ( ',' '...' )? ;
    public final void parameter_list() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:189:16: ( parameter_declaration_list ( ',' '...' )? )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:190:2: parameter_declaration_list ( ',' '...' )?
            {
            pushFollow(FOLLOW_parameter_declaration_list_in_parameter_list531);
            parameter_declaration_list();

            state._fsp--;
            if (state.failed) return ;

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:190:29: ( ',' '...' )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==34) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:190:31: ',' '...'
                    {
                    match(input,34,FOLLOW_34_in_parameter_list535); if (state.failed) return ;

                    match(input,38,FOLLOW_38_in_parameter_list537); if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "parameter_list"



    // $ANTLR start "parameter_declaration"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:193:1: parameter_declaration : declaration_specifiers ( ( declarator )? | abstract_declarator ) ;
    public final void parameter_declaration() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:194:3: ( declaration_specifiers ( ( declarator )? | abstract_declarator ) )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:194:5: declaration_specifiers ( ( declarator )? | abstract_declarator )
            {
            pushFollow(FOLLOW_declaration_specifiers_in_parameter_declaration554);
            declaration_specifiers();

            state._fsp--;
            if (state.failed) return ;

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:194:28: ( ( declarator )? | abstract_declarator )
            int alt23=2;
            switch ( input.LA(1) ) {
            case IDENTIFIER:
                {
                alt23=1;
                }
                break;
            case 34:
                {
                int LA23_2 = input.LA(2);

                if ( (synpred36_ObjectiveC()) ) {
                    alt23=1;
                }
                else if ( (true) ) {
                    alt23=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 23, 2, input);

                    throw nvae;

                }
                }
                break;
            case 42:
                {
                int LA23_3 = input.LA(2);

                if ( (synpred36_ObjectiveC()) ) {
                    alt23=1;
                }
                else if ( (true) ) {
                    alt23=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 23, 3, input);

                    throw nvae;

                }
                }
                break;
            case 93:
                {
                int LA23_4 = input.LA(2);

                if ( (synpred36_ObjectiveC()) ) {
                    alt23=1;
                }
                else if ( (true) ) {
                    alt23=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 23, 4, input);

                    throw nvae;

                }
                }
                break;
            case 28:
                {
                int LA23_5 = input.LA(2);

                if ( (synpred36_ObjectiveC()) ) {
                    alt23=1;
                }
                else if ( (true) ) {
                    alt23=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 23, 5, input);

                    throw nvae;

                }
                }
                break;
            case EOF:
                {
                int LA23_6 = input.LA(2);

                if ( (synpred36_ObjectiveC()) ) {
                    alt23=1;
                }
                else if ( (true) ) {
                    alt23=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 23, 6, input);

                    throw nvae;

                }
                }
                break;
            case 65:
                {
                alt23=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;

            }

            switch (alt23) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:194:29: ( declarator )?
                    {
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:194:29: ( declarator )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0==IDENTIFIER) ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:194:29: declarator
                            {
                            pushFollow(FOLLOW_declarator_in_parameter_declaration557);
                            declarator();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:194:43: abstract_declarator
                    {
                    pushFollow(FOLLOW_abstract_declarator_in_parameter_declaration562);
                    abstract_declarator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "parameter_declaration"



    // $ANTLR start "parameter_declaration_list"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:197:1: parameter_declaration_list : parameter_declaration ( ',' parameter_declaration )* ;
    public final void parameter_declaration_list() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:198:3: ( parameter_declaration ( ',' parameter_declaration )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:198:5: parameter_declaration ( ',' parameter_declaration )*
            {
            pushFollow(FOLLOW_parameter_declaration_in_parameter_declaration_list577);
            parameter_declaration();

            state._fsp--;
            if (state.failed) return ;

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:198:27: ( ',' parameter_declaration )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==34) ) {
                    int LA24_1 = input.LA(2);

                    if ( (LA24_1==64||LA24_1==69||(LA24_1 >= 71 && LA24_1 <= 72)||LA24_1==75||(LA24_1 >= 77 && LA24_1 <= 78)||(LA24_1 >= 81 && LA24_1 <= 86)||(LA24_1 >= 88 && LA24_1 <= 91)) ) {
                        alt24=1;
                    }


                }


                switch (alt24) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:198:29: ',' parameter_declaration
            	    {
            	    match(input,34,FOLLOW_34_in_parameter_declaration_list581); if (state.failed) return ;

            	    pushFollow(FOLLOW_parameter_declaration_in_parameter_declaration_list583);
            	    parameter_declaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "parameter_declaration_list"



    // $ANTLR start "abstract_declarator"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:201:1: abstract_declarator returns [ArrayType arrayType] : ( ( '[' (c= constant_expression )? ']' )+ |);
    public final ArrayType abstract_declarator() throws RecognitionException {
        ArrayType arrayType = null;


        Expression c =null;



        	arrayType =null;
        	Expression dimension=null;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:208:3: ( ( '[' (c= constant_expression )? ']' )+ |)
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==65) ) {
                alt27=1;
            }
            else if ( (LA27_0==EOF||LA27_0==28||LA27_0==34||LA27_0==42||LA27_0==93) ) {
                alt27=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return arrayType;}
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;

            }
            switch (alt27) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:216:3: ( '[' (c= constant_expression )? ']' )+
                    {
                    if ( state.backtracking==0 ) { arrayType =new ArrayType(); }

                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:217:4: ( '[' (c= constant_expression )? ']' )+
                    int cnt26=0;
                    loop26:
                    do {
                        int alt26=2;
                        int LA26_0 = input.LA(1);

                        if ( (LA26_0==65) ) {
                            alt26=1;
                        }


                        switch (alt26) {
                    	case 1 :
                    	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:217:5: '[' (c= constant_expression )? ']'
                    	    {
                    	    match(input,65,FOLLOW_65_in_abstract_declarator639); if (state.failed) return arrayType;

                    	    if ( state.backtracking==0 ) {dimension=null;}

                    	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:219:4: (c= constant_expression )?
                    	    int alt25=2;
                    	    int LA25_0 = input.LA(1);

                    	    if ( ((LA25_0 >= CHARACTER_LITERAL && LA25_0 <= DECIMAL_LITERAL)||LA25_0==FLOATING_POINT_LITERAL||LA25_0==HEX_LITERAL||LA25_0==IDENTIFIER||LA25_0==OCTAL_LITERAL||LA25_0==STRING_LITERAL||LA25_0==20||LA25_0==25||LA25_0==27||LA25_0==29||LA25_0==32||(LA25_0 >= 35 && LA25_0 <= 36)||LA25_0==98) ) {
                    	        alt25=1;
                    	    }
                    	    switch (alt25) {
                    	        case 1 :
                    	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:219:5: c= constant_expression
                    	            {
                    	            pushFollow(FOLLOW_constant_expression_in_abstract_declarator653);
                    	            c=constant_expression();

                    	            state._fsp--;
                    	            if (state.failed) return arrayType;

                    	            if ( state.backtracking==0 ) {
                    	              		dimension=c;	
                    	              	}

                    	            }
                    	            break;

                    	    }


                    	    if ( state.backtracking==0 ) {arrayType.addDimensionExpression(dimension);}

                    	    match(input,66,FOLLOW_66_in_abstract_declarator667); if (state.failed) return arrayType;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt26 >= 1 ) break loop26;
                    	    if (state.backtracking>0) {state.failed=true; return arrayType;}
                                EarlyExitException eee =
                                    new EarlyExitException(26, input);
                                throw eee;
                        }
                        cnt26++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:224:5: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return arrayType;
    }
    // $ANTLR end "abstract_declarator"



    // $ANTLR start "abstract_declarator_suffix"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:226:1: abstract_declarator_suffix : ( '[' ( constant_expression )? ']' | '(' ( parameter_declaration_list )? ')' );
    public final void abstract_declarator_suffix() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:229:3: ( '[' ( constant_expression )? ']' | '(' ( parameter_declaration_list )? ')' )
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==65) ) {
                alt30=1;
            }
            else if ( (LA30_0==27) ) {
                alt30=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;

            }
            switch (alt30) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:229:5: '[' ( constant_expression )? ']'
                    {
                    match(input,65,FOLLOW_65_in_abstract_declarator_suffix689); if (state.failed) return ;

                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:229:9: ( constant_expression )?
                    int alt28=2;
                    int LA28_0 = input.LA(1);

                    if ( ((LA28_0 >= CHARACTER_LITERAL && LA28_0 <= DECIMAL_LITERAL)||LA28_0==FLOATING_POINT_LITERAL||LA28_0==HEX_LITERAL||LA28_0==IDENTIFIER||LA28_0==OCTAL_LITERAL||LA28_0==STRING_LITERAL||LA28_0==20||LA28_0==25||LA28_0==27||LA28_0==29||LA28_0==32||(LA28_0 >= 35 && LA28_0 <= 36)||LA28_0==98) ) {
                        alt28=1;
                    }
                    switch (alt28) {
                        case 1 :
                            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:229:9: constant_expression
                            {
                            pushFollow(FOLLOW_constant_expression_in_abstract_declarator_suffix691);
                            constant_expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    match(input,66,FOLLOW_66_in_abstract_declarator_suffix694); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:230:5: '(' ( parameter_declaration_list )? ')'
                    {
                    match(input,27,FOLLOW_27_in_abstract_declarator_suffix700); if (state.failed) return ;

                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:230:10: ( parameter_declaration_list )?
                    int alt29=2;
                    int LA29_0 = input.LA(1);

                    if ( (LA29_0==64||LA29_0==69||(LA29_0 >= 71 && LA29_0 <= 72)||LA29_0==75||(LA29_0 >= 77 && LA29_0 <= 78)||(LA29_0 >= 81 && LA29_0 <= 86)||(LA29_0 >= 88 && LA29_0 <= 91)) ) {
                        alt29=1;
                    }
                    switch (alt29) {
                        case 1 :
                            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:230:10: parameter_declaration_list
                            {
                            pushFollow(FOLLOW_parameter_declaration_list_in_abstract_declarator_suffix703);
                            parameter_declaration_list();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    match(input,28,FOLLOW_28_in_abstract_declarator_suffix706); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "abstract_declarator_suffix"



    // $ANTLR start "selector"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:232:1: selector : IDENTIFIER ;
    public final void selector() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:232:9: ( IDENTIFIER )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:233:1: IDENTIFIER
            {
            match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_selector716); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "selector"



    // $ANTLR start "method_type"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:235:1: method_type : '(' type_name ')' ;
    public final void method_type() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:235:12: ( '(' type_name ')' )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:236:1: '(' type_name ')'
            {
            match(input,27,FOLLOW_27_in_method_type723); if (state.failed) return ;

            pushFollow(FOLLOW_type_name_in_method_type725);
            type_name();

            state._fsp--;
            if (state.failed) return ;

            match(input,28,FOLLOW_28_in_method_type727); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "method_type"



    // $ANTLR start "type_name"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:238:1: type_name returns [MethodType methodType] : sql= specifier_qualifier_list ad= abstract_declarator ;
    public final MethodType type_name() throws RecognitionException {
        MethodType methodType = null;


        ObjectiveCParser.specifier_qualifier_list_return sql =null;

        ArrayType ad =null;



        	methodType =new MethodType(null,-1);

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:244:2: (sql= specifier_qualifier_list ad= abstract_declarator )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:245:2: sql= specifier_qualifier_list ad= abstract_declarator
            {
            pushFollow(FOLLOW_specifier_qualifier_list_in_type_name750);
            sql=specifier_qualifier_list();

            state._fsp--;
            if (state.failed) return methodType;

            if ( state.backtracking==0 ) {
            		methodType.setModifier((sql!=null?sql.modifier:null));
            		methodType.setSpecifier((sql!=null?sql.specifier:null));
            		methodType.setDataType((sql!=null?sql.dataType:null));

            	}

            pushFollow(FOLLOW_abstract_declarator_in_type_name760);
            ad=abstract_declarator();

            state._fsp--;
            if (state.failed) return methodType;

            if ( state.backtracking==0 ) {
                	methodType.setArrayType(ad);
                }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return methodType;
    }
    // $ANTLR end "type_name"


    public static class specifier_qualifier_list_return extends ParserRuleReturnScope {
        public BasicModifier modifier;
        public Specifier specifier;
        public BasicDataType dataType;
    };


    // $ANTLR start "specifier_qualifier_list"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:258:1: specifier_qualifier_list returns [BasicModifier modifier,Specifier specifier,BasicDataType dataType] : (ts= type_specifier |tq= type_qualifier )+ ;
    public final ObjectiveCParser.specifier_qualifier_list_return specifier_qualifier_list() throws RecognitionException {
        ObjectiveCParser.specifier_qualifier_list_return retval = new ObjectiveCParser.specifier_qualifier_list_return();
        retval.start = input.LT(1);


        ObjectiveCParser.type_specifier_return ts =null;

        int tq =0;


         
        //Setting parent to null as its parent rule can define itself who the parent is
        	retval.modifier =new BasicModifier(null);
        	retval.specifier =new Specifier(null);
        	retval.dataType =new BasicDataType();

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:266:2: ( (ts= type_specifier |tq= type_qualifier )+ )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:267:3: (ts= type_specifier |tq= type_qualifier )+
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:267:3: (ts= type_specifier |tq= type_qualifier )+
            int cnt31=0;
            loop31:
            do {
                int alt31=3;
                int LA31_0 = input.LA(1);

                if ( (LA31_0==64||LA31_0==71||LA31_0==75||LA31_0==78||(LA31_0 >= 81 && LA31_0 <= 82)||(LA31_0 >= 84 && LA31_0 <= 85)||(LA31_0 >= 88 && LA31_0 <= 90)) ) {
                    alt31=1;
                }
                else if ( (LA31_0==72||LA31_0==91) ) {
                    alt31=2;
                }


                switch (alt31) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:268:3: ts= type_specifier
            	    {
            	    pushFollow(FOLLOW_type_specifier_in_specifier_qualifier_list794);
            	    ts=type_specifier();

            	    state._fsp--;
            	    if (state.failed) return retval;

            	    if ( state.backtracking==0 ) { 
            	    			
            	    			if((ts!=null?ts.nativeType:null)!=null){
            	    				retval.dataType.setDataType((ts!=null?ts.nativeType:null));
            	    				//((declaration_scope)declaration_stack.peek()).tempDecl.dataType.setDataType((ts!=null?ts.nativeType:null));
            	    			}
            	    			else{	
            	    				retval.specifier.setSpecifier((ts!=null?ts.nativeSpecifier:null));
            	    				//((declaration_scope)declaration_stack.peek()).tempDecl.specifier.setSpecifier((ts!=null?ts.nativeSpecifier:null));
            	    			}
            	    		}

            	    }
            	    break;
            	case 2 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:281:5: tq= type_qualifier
            	    {
            	    pushFollow(FOLLOW_type_qualifier_in_specifier_qualifier_list811);
            	    tq=type_qualifier();

            	    state._fsp--;
            	    if (state.failed) return retval;

            	    if ( state.backtracking==0 ) {
            	    			retval.modifier.setModifier(tq);
            	    			//((declaration_scope)declaration_stack.peek()).tempDecl.modifier.setModifier(tq);
            	    	 }

            	    }
            	    break;

            	default :
            	    if ( cnt31 >= 1 ) break loop31;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(31, input);
                        throw eee;
                }
                cnt31++;
            } while (true);


            }

            retval.stop = input.LT(-1);


        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "specifier_qualifier_list"



    // $ANTLR start "function_definition"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:290:1: function_definition returns [FunctionDefinition functionDefinition] : ds= declaration_specifiers declarator c= compound_statement ;
    public final FunctionDefinition function_definition() throws RecognitionException {
        FunctionDefinition functionDefinition = null;


        ObjectiveCParser.declaration_specifiers_return ds =null;

        Statement.CompoundStatement c =null;

        ObjectiveCParser.declarator_return declarator1 =null;



        	functionDefinition =new FunctionDefinition(null,-1);

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:297:2: (ds= declaration_specifiers declarator c= compound_statement )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:298:2: ds= declaration_specifiers declarator c= compound_statement
            {
            pushFollow(FOLLOW_declaration_specifiers_in_function_definition853);
            ds=declaration_specifiers();

            state._fsp--;
            if (state.failed) return functionDefinition;

            if ( state.backtracking==0 ) {
            		functionDefinition.setModifier((ds!=null?ds.modifier:null));
            		functionDefinition.setDataType((ds!=null?ds.dataType:null));
            		functionDefinition.setSpecifier((ds!=null?ds.specifier:null));
            	}

            pushFollow(FOLLOW_declarator_in_function_definition857);
            declarator1=declarator();

            state._fsp--;
            if (state.failed) return functionDefinition;

            if ( state.backtracking==0 ) {
            		functionDefinition.setIdentifier((declarator1!=null?declarator1.identifier:null));
            		functionDefinition.setArrayType((declarator1!=null?declarator1.arrayType:null));
            	}

            pushFollow(FOLLOW_compound_statement_in_function_definition865);
            c=compound_statement();

            state._fsp--;
            if (state.failed) return functionDefinition;

            if ( state.backtracking==0 ) {
            		functionDefinition.setCompoundStatement(c);
            	}

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return functionDefinition;
    }
    // $ANTLR end "function_definition"



    // $ANTLR start "protocol_interface_declaration"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:316:1: protocol_interface_declaration : ( interface_declaration_list )* ( qualified_protocol_interface_declaration )* ;
    public final void protocol_interface_declaration() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:316:31: ( ( interface_declaration_list )* ( qualified_protocol_interface_declaration )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:317:6: ( interface_declaration_list )* ( qualified_protocol_interface_declaration )*
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:317:6: ( interface_declaration_list )*
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==31||LA32_0==35||LA32_0==64||LA32_0==69||(LA32_0 >= 71 && LA32_0 <= 72)||LA32_0==75||(LA32_0 >= 77 && LA32_0 <= 78)||(LA32_0 >= 81 && LA32_0 <= 86)||(LA32_0 >= 88 && LA32_0 <= 91)) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:317:6: interface_declaration_list
            	    {
            	    pushFollow(FOLLOW_interface_declaration_list_in_protocol_interface_declaration883);
            	    interface_declaration_list();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);


            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:317:34: ( qualified_protocol_interface_declaration )*
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( (LA33_0==57||LA33_0==63) ) {
                    alt33=1;
                }


                switch (alt33) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:317:34: qualified_protocol_interface_declaration
            	    {
            	    pushFollow(FOLLOW_qualified_protocol_interface_declaration_in_protocol_interface_declaration886);
            	    qualified_protocol_interface_declaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop33;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "protocol_interface_declaration"



    // $ANTLR start "qualified_protocol_interface_declaration"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:319:1: qualified_protocol_interface_declaration : ( '@optional' ( interface_declaration_list )* | '@required' ( interface_declaration_list )* );
    public final void qualified_protocol_interface_declaration() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:319:41: ( '@optional' ( interface_declaration_list )* | '@required' ( interface_declaration_list )* )
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==57) ) {
                alt36=1;
            }
            else if ( (LA36_0==63) ) {
                alt36=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;

            }
            switch (alt36) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:320:7: '@optional' ( interface_declaration_list )*
                    {
                    match(input,57,FOLLOW_57_in_qualified_protocol_interface_declaration901); if (state.failed) return ;

                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:320:19: ( interface_declaration_list )*
                    loop34:
                    do {
                        int alt34=2;
                        int LA34_0 = input.LA(1);

                        if ( (LA34_0==31||LA34_0==35||LA34_0==64||LA34_0==69||(LA34_0 >= 71 && LA34_0 <= 72)||LA34_0==75||(LA34_0 >= 77 && LA34_0 <= 78)||(LA34_0 >= 81 && LA34_0 <= 86)||(LA34_0 >= 88 && LA34_0 <= 91)) ) {
                            alt34=1;
                        }


                        switch (alt34) {
                    	case 1 :
                    	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:320:19: interface_declaration_list
                    	    {
                    	    pushFollow(FOLLOW_interface_declaration_list_in_qualified_protocol_interface_declaration903);
                    	    interface_declaration_list();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop34;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:321:7: '@required' ( interface_declaration_list )*
                    {
                    match(input,63,FOLLOW_63_in_qualified_protocol_interface_declaration912); if (state.failed) return ;

                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:321:19: ( interface_declaration_list )*
                    loop35:
                    do {
                        int alt35=2;
                        int LA35_0 = input.LA(1);

                        if ( (LA35_0==31||LA35_0==35||LA35_0==64||LA35_0==69||(LA35_0 >= 71 && LA35_0 <= 72)||LA35_0==75||(LA35_0 >= 77 && LA35_0 <= 78)||(LA35_0 >= 81 && LA35_0 <= 86)||(LA35_0 >= 88 && LA35_0 <= 91)) ) {
                            alt35=1;
                        }


                        switch (alt35) {
                    	case 1 :
                    	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:321:19: interface_declaration_list
                    	    {
                    	    pushFollow(FOLLOW_interface_declaration_list_in_qualified_protocol_interface_declaration914);
                    	    interface_declaration_list();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop35;
                        }
                    } while (true);


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "qualified_protocol_interface_declaration"



    // $ANTLR start "protocol_declaration"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:324:1: protocol_declaration returns [Protocol protocol] : '@protocol' pName= protocol_name (pList= protocol_reference_list )? ( protocol_interface_declaration )? '@end' ;
    public final Protocol protocol_declaration() throws RecognitionException {
        Protocol protocol = null;


        String pName =null;

        ObjectiveCParser.protocol_reference_list_return pList =null;



           
            protocol = new Protocol(null, -1);
            
            
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:333:5: ( '@protocol' pName= protocol_name (pList= protocol_reference_list )? ( protocol_interface_declaration )? '@end' )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:335:2: '@protocol' pName= protocol_name (pList= protocol_reference_list )? ( protocol_interface_declaration )? '@end'
            {
            match(input,61,FOLLOW_61_in_protocol_declaration952); if (state.failed) return protocol;

            pushFollow(FOLLOW_protocol_name_in_protocol_declaration962);
            pName=protocol_name();

            state._fsp--;
            if (state.failed) return protocol;

            if ( state.backtracking==0 ) { protocol.setProtocolName( pName ); }

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:339:2: (pList= protocol_reference_list )?
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==43) ) {
                alt37=1;
            }
            switch (alt37) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:339:4: pList= protocol_reference_list
                    {
                    pushFollow(FOLLOW_protocol_reference_list_in_protocol_declaration975);
                    pList=protocol_reference_list();

                    state._fsp--;
                    if (state.failed) return protocol;

                    }
                    break;

            }


            if ( state.backtracking==0 ) { protocol.setProtocolList((pList!=null?input.toString(pList.start,pList.stop):null)); }

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:341:2: ( protocol_interface_declaration )?
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==31||LA38_0==35||LA38_0==57||(LA38_0 >= 63 && LA38_0 <= 64)||LA38_0==69||(LA38_0 >= 71 && LA38_0 <= 72)||LA38_0==75||(LA38_0 >= 77 && LA38_0 <= 78)||(LA38_0 >= 81 && LA38_0 <= 86)||(LA38_0 >= 88 && LA38_0 <= 91)) ) {
                alt38=1;
            }
            else if ( (LA38_0==54) ) {
                int LA38_2 = input.LA(2);

                if ( (synpred52_ObjectiveC()) ) {
                    alt38=1;
                }
            }
            switch (alt38) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:341:4: protocol_interface_declaration
                    {
                    pushFollow(FOLLOW_protocol_interface_declaration_in_protocol_declaration987);
                    protocol_interface_declaration();

                    state._fsp--;
                    if (state.failed) return protocol;

                    }
                    break;

            }


            match(input,54,FOLLOW_54_in_protocol_declaration996); if (state.failed) return protocol;

            if ( state.backtracking==0 ) { System.out.println( protocol.toString()  ); }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return protocol;
    }
    // $ANTLR end "protocol_declaration"



    // $ANTLR start "protocol_declaration_list"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:347:1: protocol_declaration_list : ( '@protocol' protocol_list ';' ) ;
    public final void protocol_declaration_list() throws RecognitionException {
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:347:26: ( ( '@protocol' protocol_list ';' ) )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:348:2: ( '@protocol' protocol_list ';' )
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:348:2: ( '@protocol' protocol_list ';' )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:348:3: '@protocol' protocol_list ';'
            {
            match(input,61,FOLLOW_61_in_protocol_declaration_list1011); if (state.failed) return ;

            pushFollow(FOLLOW_protocol_list_in_protocol_declaration_list1013);
            protocol_list();

            state._fsp--;
            if (state.failed) return ;

            match(input,42,FOLLOW_42_in_protocol_declaration_list1014); if (state.failed) return ;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "protocol_declaration_list"


    public static class protocol_reference_list_return extends ParserRuleReturnScope {
        public String pList;
    };


    // $ANTLR start "protocol_reference_list"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:351:1: protocol_reference_list returns [String pList] : '<' pNames= protocol_list '>' ;
    public final ObjectiveCParser.protocol_reference_list_return protocol_reference_list() throws RecognitionException {
        ObjectiveCParser.protocol_reference_list_return retval = new ObjectiveCParser.protocol_reference_list_return();
        retval.start = input.LT(1);


        ObjectiveCParser.protocol_list_return pNames =null;




          retval.pList = null; 
          
         
        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:363:2: ( '<' pNames= protocol_list '>' )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:364:3: '<' pNames= protocol_list '>'
            {
            match(input,43,FOLLOW_43_in_protocol_reference_list1044); if (state.failed) return retval;

            pushFollow(FOLLOW_protocol_list_in_protocol_reference_list1052);
            pNames=protocol_list();

            state._fsp--;
            if (state.failed) return retval;

            match(input,49,FOLLOW_49_in_protocol_reference_list1055); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {
              
              retval.pList = (pNames!=null?input.toString(pNames.start,pNames.stop):null);
             
             }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "protocol_reference_list"


    public static class protocol_list_return extends ParserRuleReturnScope {
        public String pNames;
    };


    // $ANTLR start "protocol_list"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:366:1: protocol_list returns [String pNames] : name= protocol_name ( ',' protocol_name )* ;
    public final ObjectiveCParser.protocol_list_return protocol_list() throws RecognitionException {
        ObjectiveCParser.protocol_list_return retval = new ObjectiveCParser.protocol_list_return();
        retval.start = input.LT(1);


        String name =null;


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:368:2: (name= protocol_name ( ',' protocol_name )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:369:2: name= protocol_name ( ',' protocol_name )*
            {
            pushFollow(FOLLOW_protocol_name_in_protocol_list1075);
            name=protocol_name();

            state._fsp--;
            if (state.failed) return retval;

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:369:23: ( ',' protocol_name )*
            loop39:
            do {
                int alt39=2;
                int LA39_0 = input.LA(1);

                if ( (LA39_0==34) ) {
                    alt39=1;
                }


                switch (alt39) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:369:24: ',' protocol_name
            	    {
            	    match(input,34,FOLLOW_34_in_protocol_list1078); if (state.failed) return retval;

            	    pushFollow(FOLLOW_protocol_name_in_protocol_list1080);
            	    protocol_name();

            	    state._fsp--;
            	    if (state.failed) return retval;

            	    }
            	    break;

            	default :
            	    break loop39;
                }
            } while (true);


            if ( state.backtracking==0 ) { retval.pNames = name;  }

            }

            retval.stop = input.LT(-1);


        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "protocol_list"



    // $ANTLR start "protocol_name"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:371:1: protocol_name returns [String text] : v= IDENTIFIER ;
    public final String protocol_name() throws RecognitionException {
        String text = null;


        Token v=null;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:373:2: (v= IDENTIFIER )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:374:2: v= IDENTIFIER
            {
            v=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_protocol_name1104); if (state.failed) return text;

            if ( state.backtracking==0 ) {text = (v!=null?v.getText():null);}

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return text;
    }
    // $ANTLR end "protocol_name"


    protected static class declaration_scope {
        ListDeclaration arrDeclarations;
        Declaration tempDecl;
    }
    protected Stack declaration_stack = new Stack();



    // $ANTLR start "declaration"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:376:1: declaration returns [ListDeclaration declarations] : ds= declaration_specifiers (idl= init_declarator_list[$declaration::tempDecl] )? ';' ;
    public final ListDeclaration declaration() throws RecognitionException {
        declaration_stack.push(new declaration_scope());
        ListDeclaration declarations = null;


        ObjectiveCParser.declaration_specifiers_return ds =null;

        ListDeclaration idl =null;



        	((declaration_scope)declaration_stack.peek()).arrDeclarations =new ListDeclaration(null,-1);
        	((declaration_scope)declaration_stack.peek()).tempDecl =new Declaration((Node)src,-1);
        	

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:390:2: (ds= declaration_specifiers (idl= init_declarator_list[$declaration::tempDecl] )? ';' )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:391:2: ds= declaration_specifiers (idl= init_declarator_list[$declaration::tempDecl] )? ';'
            {
            pushFollow(FOLLOW_declaration_specifiers_in_declaration1138);
            ds=declaration_specifiers();

            state._fsp--;
            if (state.failed) return declarations;

            if ( state.backtracking==0 ) {
            	
            		(ds!=null?ds.modifier:null).setParent((Statement)((declaration_scope)declaration_stack.peek()).tempDecl);
            		(ds!=null?ds.specifier:null).setParent((Statement)((declaration_scope)declaration_stack.peek()).tempDecl);
            		
            		((declaration_scope)declaration_stack.peek()).tempDecl.setModifier((ds!=null?ds.modifier:null));
            		((declaration_scope)declaration_stack.peek()).tempDecl.setSpecifier((ds!=null?ds.specifier:null));
            		((declaration_scope)declaration_stack.peek()).tempDecl.setDataType((ds!=null?ds.dataType:null));
            		
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:402:2: (idl= init_declarator_list[$declaration::tempDecl] )?
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==IDENTIFIER) ) {
                alt40=1;
            }
            switch (alt40) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:403:3: idl= init_declarator_list[$declaration::tempDecl]
                    {
                    pushFollow(FOLLOW_init_declarator_list_in_declaration1150);
                    idl=init_declarator_list(((declaration_scope)declaration_stack.peek()).tempDecl);

                    state._fsp--;
                    if (state.failed) return declarations;

                    if ( state.backtracking==0 ) {
                    			
                    		}

                    }
                    break;

            }


            match(input,42,FOLLOW_42_in_declaration1161); if (state.failed) return declarations;

            }

            if ( state.backtracking==0 ) {
            	declarations =((declaration_scope)declaration_stack.peek()).arrDeclarations;
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
            declaration_stack.pop();
        }
        return declarations;
    }
    // $ANTLR end "declaration"


    public static class declaration_specifiers_return extends ParserRuleReturnScope {
        public BasicModifier modifier;
        public Specifier specifier;
        public BasicDataType dataType;
    };


    // $ANTLR start "declaration_specifiers"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:410:1: declaration_specifiers returns [BasicModifier modifier,Specifier specifier,BasicDataType dataType] : (s= storage_class_specifier |ts= type_specifier |tq= type_qualifier )+ ;
    public final ObjectiveCParser.declaration_specifiers_return declaration_specifiers() throws RecognitionException {
        ObjectiveCParser.declaration_specifiers_return retval = new ObjectiveCParser.declaration_specifiers_return();
        retval.start = input.LT(1);


        int s =0;

        ObjectiveCParser.type_specifier_return ts =null;

        int tq =0;


         
        //Setting parent to null as its parent rule can define itself who the parent is
        	retval.modifier =new BasicModifier(null);
        	retval.specifier =new Specifier(null);
        	retval.dataType =new BasicDataType();

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:419:2: ( (s= storage_class_specifier |ts= type_specifier |tq= type_qualifier )+ )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:420:2: (s= storage_class_specifier |ts= type_specifier |tq= type_qualifier )+
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:420:2: (s= storage_class_specifier |ts= type_specifier |tq= type_qualifier )+
            int cnt41=0;
            loop41:
            do {
                int alt41=4;
                switch ( input.LA(1) ) {
                case 69:
                case 77:
                case 83:
                case 86:
                    {
                    alt41=1;
                    }
                    break;
                case 64:
                case 71:
                case 75:
                case 78:
                case 81:
                case 82:
                case 84:
                case 85:
                case 88:
                case 89:
                case 90:
                    {
                    alt41=2;
                    }
                    break;
                case 72:
                case 91:
                    {
                    alt41=3;
                    }
                    break;

                }

                switch (alt41) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:421:3: s= storage_class_specifier
            	    {
            	    pushFollow(FOLLOW_storage_class_specifier_in_declaration_specifiers1190);
            	    s=storage_class_specifier();

            	    state._fsp--;
            	    if (state.failed) return retval;

            	    if ( state.backtracking==0 ) {
            	    			retval.modifier.setModifier(s);
            	    			//((declaration_scope)declaration_stack.peek()).tempDecl.modifier.setModifier(s);//TODO: To be removed
            	    		}

            	    }
            	    break;
            	case 2 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:427:5: ts= type_specifier
            	    {
            	    pushFollow(FOLLOW_type_specifier_in_declaration_specifiers1206);
            	    ts=type_specifier();

            	    state._fsp--;
            	    if (state.failed) return retval;

            	    if ( state.backtracking==0 ) {
            	    			
            	    			if((ts!=null?ts.nativeType:null)!=null){
            	    				retval.dataType.setDataType((ts!=null?ts.nativeType:null));
            	    				//((declaration_scope)declaration_stack.peek()).tempDecl.dataType.setDataType((ts!=null?ts.nativeType:null));
            	    			}
            	    			else{	
            	    				retval.specifier.setSpecifier((ts!=null?ts.nativeSpecifier:null));
            	    				//((declaration_scope)declaration_stack.peek()).tempDecl.specifier.setSpecifier((ts!=null?ts.nativeSpecifier:null));
            	    			}
            	    		}

            	    }
            	    break;
            	case 3 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:440:5: tq= type_qualifier
            	    {
            	    pushFollow(FOLLOW_type_qualifier_in_declaration_specifiers1221);
            	    tq=type_qualifier();

            	    state._fsp--;
            	    if (state.failed) return retval;

            	    if ( state.backtracking==0 ) {
            	    			retval.modifier.setModifier(tq);
            	    			//((declaration_scope)declaration_stack.peek()).tempDecl.modifier.setModifier(tq);
            	    		}

            	    }
            	    break;

            	default :
            	    if ( cnt41 >= 1 ) break loop41;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(41, input);
                        throw eee;
                }
                cnt41++;
            } while (true);


            }

            retval.stop = input.LT(-1);


        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "declaration_specifiers"



    // $ANTLR start "init_declarator_list"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:450:1: init_declarator_list[Declaration tempDecl] returns [ListDeclaration declarationList] : id1= init_declarator ( ',' idx= init_declarator )* ;
    public final ListDeclaration init_declarator_list(Declaration tempDecl) throws RecognitionException {
        ListDeclaration declarationList = null;


        ObjectiveCParser.init_declarator_return id1 =null;

        ObjectiveCParser.init_declarator_return idx =null;



        	declarationList =new ListDeclaration(null,-1);

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:455:2: (id1= init_declarator ( ',' idx= init_declarator )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:455:3: id1= init_declarator ( ',' idx= init_declarator )*
            {
            pushFollow(FOLLOW_init_declarator_in_init_declarator_list1257);
            id1=init_declarator();

            state._fsp--;
            if (state.failed) return declarationList;

            if ( state.backtracking==0 ) {
            		declarationList.add(mergeAndCreateDeclaration(tempDecl,(id1!=null?id1.identifier:null),(id1!=null?id1.arrayType:null),(id1!=null?id1.initializations:null)));

            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:460:2: ( ',' idx= init_declarator )*
            loop42:
            do {
                int alt42=2;
                int LA42_0 = input.LA(1);

                if ( (LA42_0==34) ) {
                    alt42=1;
                }


                switch (alt42) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:461:3: ',' idx= init_declarator
            	    {
            	    match(input,34,FOLLOW_34_in_init_declarator_list1268); if (state.failed) return declarationList;

            	    pushFollow(FOLLOW_init_declarator_in_init_declarator_list1272);
            	    idx=init_declarator();

            	    state._fsp--;
            	    if (state.failed) return declarationList;

            	    if ( state.backtracking==0 ) {
            	    			declarationList.add(mergeAndCreateDeclaration(tempDecl,(idx!=null?idx.identifier:null),(idx!=null?idx.arrayType:null),(idx!=null?idx.initializations:null)));
            	    		}

            	    }
            	    break;

            	default :
            	    break loop42;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return declarationList;
    }
    // $ANTLR end "init_declarator_list"


    public static class init_declarator_return extends ParserRuleReturnScope {
        public Identifier identifier;
        public ArrayType arrayType;
        public Expression initializations;
    };


    // $ANTLR start "init_declarator"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:470:1: init_declarator returns [Identifier identifier,ArrayType arrayType, Expression initializations ] : d= declarator ( '=' exp1= initializer )? ;
    public final ObjectiveCParser.init_declarator_return init_declarator() throws RecognitionException {
        ObjectiveCParser.init_declarator_return retval = new ObjectiveCParser.init_declarator_return();
        retval.start = input.LT(1);


        ObjectiveCParser.declarator_return d =null;

        ListExpression exp1 =null;



        retval.initializations =null;
        ListExpression listExpression=null;
        //ListExpression listExpression=new ListExpression(null,-1);

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:482:3: (d= declarator ( '=' exp1= initializer )? )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:482:3: d= declarator ( '=' exp1= initializer )?
            {
            pushFollow(FOLLOW_declarator_in_init_declarator1315);
            d=declarator();

            state._fsp--;
            if (state.failed) return retval;

            if ( state.backtracking==0 ) {
            	 	retval.identifier =(d!=null?d.identifier:null);
            	 	retval.arrayType =(d!=null?d.arrayType:null);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:487:2: ( '=' exp1= initializer )?
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==47) ) {
                alt43=1;
            }
            switch (alt43) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:487:3: '=' exp1= initializer
                    {
                    match(input,47,FOLLOW_47_in_init_declarator1323); if (state.failed) return retval;

                    pushFollow(FOLLOW_initializer_in_init_declarator1331);
                    exp1=initializer();

                    state._fsp--;
                    if (state.failed) return retval;

                    if ( state.backtracking==0 ) {
                    		listExpression=new ListExpression(null,-1);
                    		listExpression=exp1;
                    	}

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            if ( state.backtracking==0 ) {
            retval.initializations =(Expression)listExpression;
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "init_declarator"



    // $ANTLR start "initializer"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:497:1: initializer returns [ListExpression expression ] : (exp1= assignment_expression | '{' exp2= initializer ( ',' exp3= initializer )* '}' );
    public final ListExpression initializer() throws RecognitionException {
        ListExpression expression = null;


        AssignmentExpression exp1 =null;

        ListExpression exp2 =null;

        ListExpression exp3 =null;



        expression =null;
        ListExpression listExpression=new ListExpression(null,-1);

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:507:5: (exp1= assignment_expression | '{' exp2= initializer ( ',' exp3= initializer )* '}' )
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( ((LA45_0 >= CHARACTER_LITERAL && LA45_0 <= DECIMAL_LITERAL)||LA45_0==FLOATING_POINT_LITERAL||LA45_0==HEX_LITERAL||LA45_0==IDENTIFIER||LA45_0==OCTAL_LITERAL||LA45_0==STRING_LITERAL||LA45_0==20||LA45_0==25||LA45_0==27||LA45_0==29||LA45_0==32||(LA45_0 >= 35 && LA45_0 <= 36)||LA45_0==98) ) {
                alt45=1;
            }
            else if ( (LA45_0==93) ) {
                alt45=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return expression;}
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                throw nvae;

            }
            switch (alt45) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:507:5: exp1= assignment_expression
                    {
                    pushFollow(FOLLOW_assignment_expression_in_initializer1367);
                    exp1=assignment_expression();

                    state._fsp--;
                    if (state.failed) return expression;

                    if ( state.backtracking==0 ) {
                    			listExpression.addExpression(exp1);
                    		}

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:511:4: '{' exp2= initializer ( ',' exp3= initializer )* '}'
                    {
                    match(input,93,FOLLOW_93_in_initializer1374); if (state.failed) return expression;

                    pushFollow(FOLLOW_initializer_in_initializer1378);
                    exp2=initializer();

                    state._fsp--;
                    if (state.failed) return expression;

                    if ( state.backtracking==0 ) {
                    			listExpression.addExpression(exp2);

                    		
                    		}

                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:517:3: ( ',' exp3= initializer )*
                    loop44:
                    do {
                        int alt44=2;
                        int LA44_0 = input.LA(1);

                        if ( (LA44_0==34) ) {
                            alt44=1;
                        }


                        switch (alt44) {
                    	case 1 :
                    	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:517:4: ',' exp3= initializer
                    	    {
                    	    match(input,34,FOLLOW_34_in_initializer1387); if (state.failed) return expression;

                    	    pushFollow(FOLLOW_initializer_in_initializer1391);
                    	    exp3=initializer();

                    	    state._fsp--;
                    	    if (state.failed) return expression;

                    	    if ( state.backtracking==0 ) {
                    	    			listExpression.addExpression(exp3);
                    	    			}

                    	    }
                    	    break;

                    	default :
                    	    break loop44;
                        }
                    } while (true);


                    match(input,97,FOLLOW_97_in_initializer1405); if (state.failed) return expression;

                    }
                    break;

            }
            if ( state.backtracking==0 ) {
            expression =listExpression; //At the end of rule returning the list expression
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "initializer"


    public static class declarator_return extends ParserRuleReturnScope {
        public Identifier identifier;
        public ArrayType arrayType;
    };


    // $ANTLR start "declarator"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:525:1: declarator returns [Identifier identifier,ArrayType arrayType] : d= direct_declarator ;
    public final ObjectiveCParser.declarator_return declarator() throws RecognitionException {
        ObjectiveCParser.declarator_return retval = new ObjectiveCParser.declarator_return();
        retval.start = input.LT(1);


        ObjectiveCParser.direct_declarator_return d =null;


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:527:2: (d= direct_declarator )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:530:3: d= direct_declarator
            {
            pushFollow(FOLLOW_direct_declarator_in_declarator1434);
            d=direct_declarator();

            state._fsp--;
            if (state.failed) return retval;

            if ( state.backtracking==0 ) {
            	 	retval.identifier =(d!=null?d.identifier:null);
            	 	retval.arrayType =(d!=null?d.arrayType:null);
            	 }

            }

            retval.stop = input.LT(-1);


        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "declarator"


    public static class direct_declarator_return extends ParserRuleReturnScope {
        public Identifier identifier;
        public ArrayType arrayType;
    };


    // $ANTLR start "direct_declarator"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:537:1: direct_declarator returns [Identifier identifier,ArrayType arrayType] : id= identifier (ds= declarator_suffix )* ;
    public final ObjectiveCParser.direct_declarator_return direct_declarator() throws RecognitionException {
        ObjectiveCParser.direct_declarator_return retval = new ObjectiveCParser.direct_declarator_return();
        retval.start = input.LT(1);


        ObjectiveCParser.identifier_return id =null;

        int ds =0;



        	retval.identifier =null;
        	retval.arrayType =null;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:545:2: (id= identifier (ds= declarator_suffix )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:546:4: id= identifier (ds= declarator_suffix )*
            {
            pushFollow(FOLLOW_identifier_in_direct_declarator1467);
            id=identifier();

            state._fsp--;
            if (state.failed) return retval;

            if ( state.backtracking==0 ) {
            	  	retval.identifier =new Identifier((id!=null?input.toString(id.start,id.stop):null));
            	  }

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:550:4: (ds= declarator_suffix )*
            loop46:
            do {
                int alt46=2;
                int LA46_0 = input.LA(1);

                if ( (LA46_0==27||LA46_0==65) ) {
                    alt46=1;
                }


                switch (alt46) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:551:5: ds= declarator_suffix
            	    {
            	    pushFollow(FOLLOW_declarator_suffix_in_direct_declarator1486);
            	    ds=declarator_suffix();

            	    state._fsp--;
            	    if (state.failed) return retval;

            	    if ( state.backtracking==0 ) {
            	    	  		if(retval.arrayType==null)
            	    	  			retval.arrayType =new ArrayType();
            	    	  		
            	    	  		if(ds==-1)
            	    	  			retval.arrayType.addDimension();
            	    	  		else
            	    	  			retval.arrayType.addDimension(ds);
            	    	  	}

            	    }
            	    break;

            	default :
            	    break loop46;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "direct_declarator"



    // $ANTLR start "declarator_suffix"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:568:1: declarator_suffix returns [int dimension] : ( '[' (c= constant )? ']' | '(' ( parameter_list )? ')' );
    public final int declarator_suffix() throws RecognitionException {
        int dimension = 0;


        ObjectiveCParser.constant_return c =null;



        	dimension =-1;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:573:2: ( '[' (c= constant )? ']' | '(' ( parameter_list )? ')' )
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==65) ) {
                alt49=1;
            }
            else if ( (LA49_0==27) ) {
                alt49=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return dimension;}
                NoViableAltException nvae =
                    new NoViableAltException("", 49, 0, input);

                throw nvae;

            }
            switch (alt49) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:574:2: '[' (c= constant )? ']'
                    {
                    match(input,65,FOLLOW_65_in_declarator_suffix1531); if (state.failed) return dimension;

                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:575:3: (c= constant )?
                    int alt47=2;
                    int LA47_0 = input.LA(1);

                    if ( ((LA47_0 >= CHARACTER_LITERAL && LA47_0 <= DECIMAL_LITERAL)||LA47_0==FLOATING_POINT_LITERAL||LA47_0==HEX_LITERAL||LA47_0==OCTAL_LITERAL) ) {
                        alt47=1;
                    }
                    switch (alt47) {
                        case 1 :
                            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:576:4: c= constant
                            {
                            pushFollow(FOLLOW_constant_in_declarator_suffix1543);
                            c=constant();

                            state._fsp--;
                            if (state.failed) return dimension;

                            if ( state.backtracking==0 ) {
                            				dimension =Integer.parseInt((c!=null?input.toString(c.start,c.stop):null));
                            			}

                            }
                            break;

                    }


                    match(input,66,FOLLOW_66_in_declarator_suffix1557); if (state.failed) return dimension;

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:584:4: '(' ( parameter_list )? ')'
                    {
                    match(input,27,FOLLOW_27_in_declarator_suffix1572); if (state.failed) return dimension;

                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:584:8: ( parameter_list )?
                    int alt48=2;
                    int LA48_0 = input.LA(1);

                    if ( (LA48_0==64||LA48_0==69||(LA48_0 >= 71 && LA48_0 <= 72)||LA48_0==75||(LA48_0 >= 77 && LA48_0 <= 78)||(LA48_0 >= 81 && LA48_0 <= 86)||(LA48_0 >= 88 && LA48_0 <= 91)) ) {
                        alt48=1;
                    }
                    switch (alt48) {
                        case 1 :
                            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:584:8: parameter_list
                            {
                            pushFollow(FOLLOW_parameter_list_in_declarator_suffix1574);
                            parameter_list();

                            state._fsp--;
                            if (state.failed) return dimension;

                            }
                            break;

                    }


                    match(input,28,FOLLOW_28_in_declarator_suffix1577); if (state.failed) return dimension;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return dimension;
    }
    // $ANTLR end "declarator_suffix"


    public static class type_specifier_return extends ParserRuleReturnScope {
        public BasicDataType.NativeType nativeType;
        public Specifier.NativeSpecifier nativeSpecifier;
    };


    // $ANTLR start "type_specifier"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:590:1: type_specifier returns [BasicDataType.NativeType nativeType,Specifier.NativeSpecifier nativeSpecifier] : ( 'void' | 'char' | 'short' | 'long' | 'int' | 'float' | 'double' | 'BOOL' | 'signed' | 'unsigned' | 'unichar' );
    public final ObjectiveCParser.type_specifier_return type_specifier() throws RecognitionException {
        ObjectiveCParser.type_specifier_return retval = new ObjectiveCParser.type_specifier_return();
        retval.start = input.LT(1);



        	retval.nativeType =null;
        	retval.nativeSpecifier =null;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:596:2: ( 'void' | 'char' | 'short' | 'long' | 'int' | 'float' | 'double' | 'BOOL' | 'signed' | 'unsigned' | 'unichar' )
            int alt50=11;
            switch ( input.LA(1) ) {
            case 90:
                {
                alt50=1;
                }
                break;
            case 71:
                {
                alt50=2;
                }
                break;
            case 84:
                {
                alt50=3;
                }
                break;
            case 82:
                {
                alt50=4;
                }
                break;
            case 81:
                {
                alt50=5;
                }
                break;
            case 78:
                {
                alt50=6;
                }
                break;
            case 75:
                {
                alt50=7;
                }
                break;
            case 64:
                {
                alt50=8;
                }
                break;
            case 85:
                {
                alt50=9;
                }
                break;
            case 89:
                {
                alt50=10;
                }
                break;
            case 88:
                {
                alt50=11;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 50, 0, input);

                throw nvae;

            }

            switch (alt50) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:597:4: 'void'
                    {
                    match(input,90,FOLLOW_90_in_type_specifier1608); if (state.failed) return retval;

                    if ( state.backtracking==0 ) {retval.nativeType =Type.BasicDataType.NativeType.VOID;}

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:598:4: 'char'
                    {
                    match(input,71,FOLLOW_71_in_type_specifier1615); if (state.failed) return retval;

                    if ( state.backtracking==0 ) {retval.nativeType =Type.BasicDataType.NativeType.CHAR;}

                    }
                    break;
                case 3 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:599:4: 'short'
                    {
                    match(input,84,FOLLOW_84_in_type_specifier1623); if (state.failed) return retval;

                    if ( state.backtracking==0 ) {retval.nativeSpecifier =Specifier.NativeSpecifier.SHORT;}

                    }
                    break;
                case 4 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:600:4: 'long'
                    {
                    match(input,82,FOLLOW_82_in_type_specifier1629); if (state.failed) return retval;

                    if ( state.backtracking==0 ) {retval.nativeSpecifier =Specifier.NativeSpecifier.LONG;}

                    }
                    break;
                case 5 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:601:4: 'int'
                    {
                    match(input,81,FOLLOW_81_in_type_specifier1637); if (state.failed) return retval;

                    if ( state.backtracking==0 ) {retval.nativeType =Type.BasicDataType.NativeType.INT;}

                    }
                    break;
                case 6 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:602:4: 'float'
                    {
                    match(input,78,FOLLOW_78_in_type_specifier1644); if (state.failed) return retval;

                    if ( state.backtracking==0 ) {retval.nativeType =Type.BasicDataType.NativeType.FLOAT;}

                    }
                    break;
                case 7 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:603:4: 'double'
                    {
                    match(input,75,FOLLOW_75_in_type_specifier1651); if (state.failed) return retval;

                    if ( state.backtracking==0 ) {retval.nativeType =Type.BasicDataType.NativeType.DOUBLE;}

                    }
                    break;
                case 8 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:604:4: 'BOOL'
                    {
                    match(input,64,FOLLOW_64_in_type_specifier1658); if (state.failed) return retval;

                    if ( state.backtracking==0 ) {retval.nativeType =Type.BasicDataType.NativeType.BOOLEAN;}

                    }
                    break;
                case 9 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:605:4: 'signed'
                    {
                    match(input,85,FOLLOW_85_in_type_specifier1665); if (state.failed) return retval;

                    if ( state.backtracking==0 ) {retval.nativeSpecifier =Specifier.NativeSpecifier.SIGNED;}

                    }
                    break;
                case 10 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:606:4: 'unsigned'
                    {
                    match(input,89,FOLLOW_89_in_type_specifier1672); if (state.failed) return retval;

                    if ( state.backtracking==0 ) {retval.nativeSpecifier =Specifier.NativeSpecifier.UNSIGNED;}

                    }
                    break;
                case 11 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:607:4: 'unichar'
                    {
                    match(input,88,FOLLOW_88_in_type_specifier1679); if (state.failed) return retval;

                    if ( state.backtracking==0 ) {retval.nativeType =Type.BasicDataType.NativeType.UNICHAR;}

                    }
                    break;

            }
            retval.stop = input.LT(-1);


        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "type_specifier"



    // $ANTLR start "type_qualifier"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:610:1: type_qualifier returns [int modifierType] : ( 'const' | 'volatile' );
    public final int type_qualifier() throws RecognitionException {
        int modifierType = 0;


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:612:2: ( 'const' | 'volatile' )
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==72) ) {
                alt51=1;
            }
            else if ( (LA51_0==91) ) {
                alt51=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return modifierType;}
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                throw nvae;

            }
            switch (alt51) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:613:4: 'const'
                    {
                    match(input,72,FOLLOW_72_in_type_qualifier1703); if (state.failed) return modifierType;

                    if ( state.backtracking==0 ) {modifierType =BasicModifier.CONST;}

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:614:4: 'volatile'
                    {
                    match(input,91,FOLLOW_91_in_type_qualifier1711); if (state.failed) return modifierType;

                    if ( state.backtracking==0 ) {modifierType =BasicModifier.VOLATILE;}

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return modifierType;
    }
    // $ANTLR end "type_qualifier"



    // $ANTLR start "storage_class_specifier"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:617:1: storage_class_specifier returns [int modifierType] : ( 'auto' | 'register' | 'static' | 'extern' );
    public final int storage_class_specifier() throws RecognitionException {
        int modifierType = 0;


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:620:2: ( 'auto' | 'register' | 'static' | 'extern' )
            int alt52=4;
            switch ( input.LA(1) ) {
            case 69:
                {
                alt52=1;
                }
                break;
            case 83:
                {
                alt52=2;
                }
                break;
            case 86:
                {
                alt52=3;
                }
                break;
            case 77:
                {
                alt52=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return modifierType;}
                NoViableAltException nvae =
                    new NoViableAltException("", 52, 0, input);

                throw nvae;

            }

            switch (alt52) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:621:4: 'auto'
                    {
                    match(input,69,FOLLOW_69_in_storage_class_specifier1735); if (state.failed) return modifierType;

                    if ( state.backtracking==0 ) {modifierType =BasicModifier.AUTO;}

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:622:4: 'register'
                    {
                    match(input,83,FOLLOW_83_in_storage_class_specifier1743); if (state.failed) return modifierType;

                    if ( state.backtracking==0 ) {modifierType =BasicModifier.REGISTER;}

                    }
                    break;
                case 3 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:623:4: 'static'
                    {
                    match(input,86,FOLLOW_86_in_storage_class_specifier1751); if (state.failed) return modifierType;

                    if ( state.backtracking==0 ) {modifierType =BasicModifier.STATIC;}

                    }
                    break;
                case 4 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:624:4: 'extern'
                    {
                    match(input,77,FOLLOW_77_in_storage_class_specifier1759); if (state.failed) return modifierType;

                    if ( state.backtracking==0 ) {modifierType =BasicModifier.EXTERN;}

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return modifierType;
    }
    // $ANTLR end "storage_class_specifier"



    // $ANTLR start "statement"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:629:1: statement returns [Statement statement] : (sel_stmt= selection_statement |exp= expression ';' |cmp_stmt= compound_statement |itr_stmt= iteration_statement |label_stmt= labeled_statement |decl= declaration | ';' );
    public final Statement statement() throws RecognitionException {
        Statement statement = null;


        Statement sel_stmt =null;

        Expression exp =null;

        Statement.CompoundStatement cmp_stmt =null;

        Statement itr_stmt =null;

        Statement label_stmt =null;

        ListDeclaration decl =null;



        statement =null;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:634:4: (sel_stmt= selection_statement |exp= expression ';' |cmp_stmt= compound_statement |itr_stmt= iteration_statement |label_stmt= labeled_statement |decl= declaration | ';' )
            int alt53=7;
            switch ( input.LA(1) ) {
            case 80:
            case 87:
                {
                alt53=1;
                }
                break;
            case IDENTIFIER:
                {
                int LA53_2 = input.LA(2);

                if ( ((LA53_2 >= 21 && LA53_2 <= 26)||(LA53_2 >= 29 && LA53_2 <= 31)||(LA53_2 >= 33 && LA53_2 <= 35)||LA53_2==37||(LA53_2 >= 39 && LA53_2 <= 40)||(LA53_2 >= 42 && LA53_2 <= 53)||(LA53_2 >= 67 && LA53_2 <= 68)||(LA53_2 >= 94 && LA53_2 <= 96)) ) {
                    alt53=2;
                }
                else if ( (LA53_2==41) ) {
                    alt53=5;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return statement;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 53, 2, input);

                    throw nvae;

                }
                }
                break;
            case CHARACTER_LITERAL:
            case DECIMAL_LITERAL:
            case FLOATING_POINT_LITERAL:
            case HEX_LITERAL:
            case OCTAL_LITERAL:
            case STRING_LITERAL:
            case 20:
            case 25:
            case 27:
            case 29:
            case 32:
            case 35:
            case 36:
            case 98:
                {
                alt53=2;
                }
                break;
            case 93:
                {
                alt53=3;
                }
                break;
            case 74:
            case 79:
            case 92:
                {
                alt53=4;
                }
                break;
            case 70:
            case 73:
                {
                alt53=5;
                }
                break;
            case 64:
            case 69:
            case 71:
            case 72:
            case 75:
            case 77:
            case 78:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 88:
            case 89:
            case 90:
            case 91:
                {
                alt53=6;
                }
                break;
            case 42:
                {
                alt53=7;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return statement;}
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;

            }

            switch (alt53) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:635:4: sel_stmt= selection_statement
                    {
                    pushFollow(FOLLOW_selection_statement_in_statement1791);
                    sel_stmt=selection_statement();

                    state._fsp--;
                    if (state.failed) return statement;

                    if ( state.backtracking==0 ) {
                         statement =sel_stmt;
                         }

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:639:7: exp= expression ';'
                    {
                    pushFollow(FOLLOW_expression_in_statement1808);
                    exp=expression();

                    state._fsp--;
                    if (state.failed) return statement;

                    match(input,42,FOLLOW_42_in_statement1810); if (state.failed) return statement;

                    if ( state.backtracking==0 ) { statement =(Statement)exp; }

                    }
                    break;
                case 3 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:640:7: cmp_stmt= compound_statement
                    {
                    pushFollow(FOLLOW_compound_statement_in_statement1821);
                    cmp_stmt=compound_statement();

                    state._fsp--;
                    if (state.failed) return statement;

                    if ( state.backtracking==0 ) { statement =cmp_stmt; }

                    }
                    break;
                case 4 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:641:7: itr_stmt= iteration_statement
                    {
                    pushFollow(FOLLOW_iteration_statement_in_statement1832);
                    itr_stmt=iteration_statement();

                    state._fsp--;
                    if (state.failed) return statement;

                    if ( state.backtracking==0 ) {statement =itr_stmt; }

                    }
                    break;
                case 5 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:642:7: label_stmt= labeled_statement
                    {
                    pushFollow(FOLLOW_labeled_statement_in_statement1845);
                    label_stmt=labeled_statement();

                    state._fsp--;
                    if (state.failed) return statement;

                    if ( state.backtracking==0 ) { statement = label_stmt; }

                    }
                    break;
                case 6 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:643:7: decl= declaration
                    {
                    pushFollow(FOLLOW_declaration_in_statement1857);
                    decl=declaration();

                    state._fsp--;
                    if (state.failed) return statement;

                    if ( state.backtracking==0 ) {statement = decl;}

                    }
                    break;
                case 7 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:644:7: ';'
                    {
                    match(input,42,FOLLOW_42_in_statement1867); if (state.failed) return statement;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return statement;
    }
    // $ANTLR end "statement"



    // $ANTLR start "labeled_statement"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:647:1: labeled_statement returns [Statement Stmt] : (v= identifier ':' s= statement | 'case' ce= constant_expression ':' s= statement | 'default' ':' s= statement );
    public final Statement labeled_statement() throws RecognitionException {
        Statement Stmt = null;


        ObjectiveCParser.identifier_return v =null;

        Statement s =null;

        Expression ce =null;




           Statement.LabelStatement lStmt = null;
           Statement.CaseDefaultStatement cStmt = null;


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:655:3: (v= identifier ':' s= statement | 'case' ce= constant_expression ':' s= statement | 'default' ':' s= statement )
            int alt54=3;
            switch ( input.LA(1) ) {
            case IDENTIFIER:
                {
                alt54=1;
                }
                break;
            case 70:
                {
                alt54=2;
                }
                break;
            case 73:
                {
                alt54=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return Stmt;}
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                throw nvae;

            }

            switch (alt54) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:655:6: v= identifier ':' s= statement
                    {
                    pushFollow(FOLLOW_identifier_in_labeled_statement1896);
                    v=identifier();

                    state._fsp--;
                    if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { lStmt = new Statement.LabelStatement( null, - 1 );  lStmt.setIdentifier( (v!=null?v.identifier:null) ); }

                    match(input,41,FOLLOW_41_in_labeled_statement1905); if (state.failed) return Stmt;

                    pushFollow(FOLLOW_statement_in_labeled_statement1924);
                    s=statement();

                    state._fsp--;
                    if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { lStmt.setStatement( s ); Stmt = ( Statement ) lStmt; }

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:659:5: 'case' ce= constant_expression ':' s= statement
                    {
                    match(input,70,FOLLOW_70_in_labeled_statement1938); if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { cStmt = new Statement.CaseDefaultStatement( null, - 1); }

                    pushFollow(FOLLOW_constant_expression_in_labeled_statement1951);
                    ce=constant_expression();

                    state._fsp--;
                    if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { cStmt.setConstExpression( ce ); }

                    match(input,41,FOLLOW_41_in_labeled_statement1961); if (state.failed) return Stmt;

                    pushFollow(FOLLOW_statement_in_labeled_statement1972);
                    s=statement();

                    state._fsp--;
                    if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { cStmt.setStatement( s ); Stmt = ( Statement ) cStmt; }

                    }
                    break;
                case 3 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:664:5: 'default' ':' s= statement
                    {
                    match(input,73,FOLLOW_73_in_labeled_statement1986); if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) {cStmt = new Statement.CaseDefaultStatement( null, - 1 ); }

                    match(input,41,FOLLOW_41_in_labeled_statement1994); if (state.failed) return Stmt;

                    pushFollow(FOLLOW_statement_in_labeled_statement2005);
                    s=statement();

                    state._fsp--;
                    if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { cStmt.setStatement( s ); Stmt = ( Statement ) cStmt; }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return Stmt;
    }
    // $ANTLR end "labeled_statement"



    // $ANTLR start "selection_statement"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:670:1: selection_statement returns [Statement statement] : ( 'if' '(' exp= expression ')' stmtIf= statement ( 'else' stmtElse= statement )? | 'switch' '(' exp= expression ')' switch_stmt= statement );
    public final Statement selection_statement() throws RecognitionException {
        Statement statement = null;


        Expression exp =null;

        Statement stmtIf =null;

        Statement stmtElse =null;

        Statement switch_stmt =null;



        statement =null;
        Statement.If statement_i=null;
        Statement.Switch statement_s=null;


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:683:3: ( 'if' '(' exp= expression ')' stmtIf= statement ( 'else' stmtElse= statement )? | 'switch' '(' exp= expression ')' switch_stmt= statement )
            int alt56=2;
            int LA56_0 = input.LA(1);

            if ( (LA56_0==80) ) {
                alt56=1;
            }
            else if ( (LA56_0==87) ) {
                alt56=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return statement;}
                NoViableAltException nvae =
                    new NoViableAltException("", 56, 0, input);

                throw nvae;

            }
            switch (alt56) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:684:3: 'if' '(' exp= expression ')' stmtIf= statement ( 'else' stmtElse= statement )?
                    {
                    match(input,80,FOLLOW_80_in_selection_statement2036); if (state.failed) return statement;

                    match(input,27,FOLLOW_27_in_selection_statement2038); if (state.failed) return statement;

                    pushFollow(FOLLOW_expression_in_selection_statement2046);
                    exp=expression();

                    state._fsp--;
                    if (state.failed) return statement;

                    if ( state.backtracking==0 ) {
                       statement_i=new Statement.If(null,-1);
                        statement_i.setExpression(exp);
                       }

                    match(input,28,FOLLOW_28_in_selection_statement2055); if (state.failed) return statement;

                    pushFollow(FOLLOW_statement_in_selection_statement2063);
                    stmtIf=statement();

                    state._fsp--;
                    if (state.failed) return statement;

                    if ( state.backtracking==0 ) {
                          statement_i.setIfStatement(stmtIf);
                        
                        }

                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:694:5: ( 'else' stmtElse= statement )?
                    int alt55=2;
                    int LA55_0 = input.LA(1);

                    if ( (LA55_0==76) ) {
                        int LA55_1 = input.LA(2);

                        if ( (synpred88_ObjectiveC()) ) {
                            alt55=1;
                        }
                    }
                    switch (alt55) {
                        case 1 :
                            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:694:6: 'else' stmtElse= statement
                            {
                            match(input,76,FOLLOW_76_in_selection_statement2071); if (state.failed) return statement;

                            pushFollow(FOLLOW_statement_in_selection_statement2080);
                            stmtElse=statement();

                            state._fsp--;
                            if (state.failed) return statement;

                            if ( state.backtracking==0 ) {
                                  statement_i.setElseStatement(stmtElse);
                                  
                                 }

                            }
                            break;

                    }


                    if ( state.backtracking==0 ) {statement =(Statement)statement_i;}

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:700:8: 'switch' '(' exp= expression ')' switch_stmt= statement
                    {
                    match(input,87,FOLLOW_87_in_selection_statement2099); if (state.failed) return statement;

                    match(input,27,FOLLOW_27_in_selection_statement2108); if (state.failed) return statement;

                    pushFollow(FOLLOW_expression_in_selection_statement2126);
                    exp=expression();

                    state._fsp--;
                    if (state.failed) return statement;

                    if ( state.backtracking==0 ) {
                           statement_s=new Statement.Switch(null,-1);
                            statement_s.setExpression(exp);
                           }

                    match(input,28,FOLLOW_28_in_selection_statement2145); if (state.failed) return statement;

                    pushFollow(FOLLOW_statement_in_selection_statement2156);
                    switch_stmt=statement();

                    state._fsp--;
                    if (state.failed) return statement;

                    if ( state.backtracking==0 ) {
                           statement_s.setSwitchStatement(switch_stmt);
                           statement =(Statement)statement_s;
                           }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return statement;
    }
    // $ANTLR end "selection_statement"



    // $ANTLR start "iteration_statement"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:715:1: iteration_statement returns [Statement Stmt] : ( 'while' '(' e= expression ')' s= statement | 'do' s= statement 'while' '(' e= expression ')' ';' | 'for' '(' (e1= expression |decl= declaration )? ';' (e2= expression )? ';' (e3= expression )? ')' s= statement );
    public final Statement iteration_statement() throws RecognitionException {
        Statement Stmt = null;


        Expression e =null;

        Statement s =null;

        Expression e1 =null;

        ListDeclaration decl =null;

        Expression e2 =null;

        Expression e3 =null;



            Stmt = null;
            Statement.While whileStmt = null; 
            Statement.ForLoop forStmt = null;
            Statement.doWhile doStmt = null;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:723:3: ( 'while' '(' e= expression ')' s= statement | 'do' s= statement 'while' '(' e= expression ')' ';' | 'for' '(' (e1= expression |decl= declaration )? ';' (e2= expression )? ';' (e3= expression )? ')' s= statement )
            int alt60=3;
            switch ( input.LA(1) ) {
            case 92:
                {
                alt60=1;
                }
                break;
            case 74:
                {
                alt60=2;
                }
                break;
            case 79:
                {
                alt60=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return Stmt;}
                NoViableAltException nvae =
                    new NoViableAltException("", 60, 0, input);

                throw nvae;

            }

            switch (alt60) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:723:5: 'while' '(' e= expression ')' s= statement
                    {
                    match(input,92,FOLLOW_92_in_iteration_statement2189); if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) {  whileStmt = new Statement.While( null, -1 ); }

                    match(input,27,FOLLOW_27_in_iteration_statement2200); if (state.failed) return Stmt;

                    pushFollow(FOLLOW_expression_in_iteration_statement2215);
                    e=expression();

                    state._fsp--;
                    if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { whileStmt.setExpression( e ); }

                    match(input,28,FOLLOW_28_in_iteration_statement2223); if (state.failed) return Stmt;

                    pushFollow(FOLLOW_statement_in_iteration_statement2236);
                    s=statement();

                    state._fsp--;
                    if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { whileStmt.setStatement( s );  Stmt = ( Statement ) whileStmt; }

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:729:5: 'do' s= statement 'while' '(' e= expression ')' ';'
                    {
                    match(input,74,FOLLOW_74_in_iteration_statement2247); if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { doStmt = new Statement.doWhile( null, -1 ); }

                    pushFollow(FOLLOW_statement_in_iteration_statement2265);
                    s=statement();

                    state._fsp--;
                    if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { doStmt.setStatement( s ); }

                    match(input,92,FOLLOW_92_in_iteration_statement2278); if (state.failed) return Stmt;

                    match(input,27,FOLLOW_27_in_iteration_statement2291); if (state.failed) return Stmt;

                    pushFollow(FOLLOW_expression_in_iteration_statement2307);
                    e=expression();

                    state._fsp--;
                    if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { doStmt.setExpression( e ); }

                    match(input,28,FOLLOW_28_in_iteration_statement2321); if (state.failed) return Stmt;

                    match(input,42,FOLLOW_42_in_iteration_statement2334); if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { Stmt = ( Statement ) doStmt; }

                    }
                    break;
                case 3 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:737:5: 'for' '(' (e1= expression |decl= declaration )? ';' (e2= expression )? ';' (e3= expression )? ')' s= statement
                    {
                    match(input,79,FOLLOW_79_in_iteration_statement2353); if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { forStmt = new Statement.ForLoop( null, -1 ); }

                    match(input,27,FOLLOW_27_in_iteration_statement2365); if (state.failed) return Stmt;

                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:739:10: (e1= expression |decl= declaration )?
                    int alt57=3;
                    int LA57_0 = input.LA(1);

                    if ( ((LA57_0 >= CHARACTER_LITERAL && LA57_0 <= DECIMAL_LITERAL)||LA57_0==FLOATING_POINT_LITERAL||LA57_0==HEX_LITERAL||LA57_0==IDENTIFIER||LA57_0==OCTAL_LITERAL||LA57_0==STRING_LITERAL||LA57_0==20||LA57_0==25||LA57_0==27||LA57_0==29||LA57_0==32||(LA57_0 >= 35 && LA57_0 <= 36)||LA57_0==98) ) {
                        alt57=1;
                    }
                    else if ( (LA57_0==64||LA57_0==69||(LA57_0 >= 71 && LA57_0 <= 72)||LA57_0==75||(LA57_0 >= 77 && LA57_0 <= 78)||(LA57_0 >= 81 && LA57_0 <= 86)||(LA57_0 >= 88 && LA57_0 <= 91)) ) {
                        alt57=2;
                    }
                    switch (alt57) {
                        case 1 :
                            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:739:12: e1= expression
                            {
                            pushFollow(FOLLOW_expression_in_iteration_statement2388);
                            e1=expression();

                            state._fsp--;
                            if (state.failed) return Stmt;

                            if ( state.backtracking==0 ) { forStmt.setExprInit( e1 ); }

                            }
                            break;
                        case 2 :
                            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:739:73: decl= declaration
                            {
                            pushFollow(FOLLOW_declaration_in_iteration_statement2396);
                            decl=declaration();

                            state._fsp--;
                            if (state.failed) return Stmt;

                            if ( state.backtracking==0 ) {forStmt.setDeclInit(decl); }

                            }
                            break;

                    }


                    match(input,42,FOLLOW_42_in_iteration_statement2413); if (state.failed) return Stmt;

                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:741:10: (e2= expression )?
                    int alt58=2;
                    int LA58_0 = input.LA(1);

                    if ( ((LA58_0 >= CHARACTER_LITERAL && LA58_0 <= DECIMAL_LITERAL)||LA58_0==FLOATING_POINT_LITERAL||LA58_0==HEX_LITERAL||LA58_0==IDENTIFIER||LA58_0==OCTAL_LITERAL||LA58_0==STRING_LITERAL||LA58_0==20||LA58_0==25||LA58_0==27||LA58_0==29||LA58_0==32||(LA58_0 >= 35 && LA58_0 <= 36)||LA58_0==98) ) {
                        alt58=1;
                    }
                    switch (alt58) {
                        case 1 :
                            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:741:12: e2= expression
                            {
                            pushFollow(FOLLOW_expression_in_iteration_statement2430);
                            e2=expression();

                            state._fsp--;
                            if (state.failed) return Stmt;

                            if ( state.backtracking==0 ) { forStmt.setExprCondition( e2 ); }

                            }
                            break;

                    }


                    match(input,42,FOLLOW_42_in_iteration_statement2446); if (state.failed) return Stmt;

                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:743:10: (e3= expression )?
                    int alt59=2;
                    int LA59_0 = input.LA(1);

                    if ( ((LA59_0 >= CHARACTER_LITERAL && LA59_0 <= DECIMAL_LITERAL)||LA59_0==FLOATING_POINT_LITERAL||LA59_0==HEX_LITERAL||LA59_0==IDENTIFIER||LA59_0==OCTAL_LITERAL||LA59_0==STRING_LITERAL||LA59_0==20||LA59_0==25||LA59_0==27||LA59_0==29||LA59_0==32||(LA59_0 >= 35 && LA59_0 <= 36)||LA59_0==98) ) {
                        alt59=1;
                    }
                    switch (alt59) {
                        case 1 :
                            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:743:12: e3= expression
                            {
                            pushFollow(FOLLOW_expression_in_iteration_statement2463);
                            e3=expression();

                            state._fsp--;
                            if (state.failed) return Stmt;

                            if ( state.backtracking==0 ) { forStmt.setExprLast( e3 ); }

                            }
                            break;

                    }


                    match(input,28,FOLLOW_28_in_iteration_statement2478); if (state.failed) return Stmt;

                    pushFollow(FOLLOW_statement_in_iteration_statement2493);
                    s=statement();

                    state._fsp--;
                    if (state.failed) return Stmt;

                    if ( state.backtracking==0 ) { forStmt.setStatement( s ); Stmt = ( Statement ) forStmt;  }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return Stmt;
    }
    // $ANTLR end "iteration_statement"



    // $ANTLR start "compound_statement"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:748:1: compound_statement returns [Statement.CompoundStatement statement] : '{' (stmt_list= statement_list )? '}' ;
    public final Statement.CompoundStatement compound_statement() throws RecognitionException {
        Statement.CompoundStatement statement = null;


        Statement.StatementList stmt_list =null;



        statement =new Statement.CompoundStatement(null,-1);

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:753:4: ( '{' (stmt_list= statement_list )? '}' )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:754:4: '{' (stmt_list= statement_list )? '}'
            {
            match(input,93,FOLLOW_93_in_compound_statement2518); if (state.failed) return statement;

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:756:5: (stmt_list= statement_list )?
            int alt61=2;
            int LA61_0 = input.LA(1);

            if ( ((LA61_0 >= CHARACTER_LITERAL && LA61_0 <= DECIMAL_LITERAL)||LA61_0==FLOATING_POINT_LITERAL||LA61_0==HEX_LITERAL||LA61_0==IDENTIFIER||LA61_0==OCTAL_LITERAL||LA61_0==STRING_LITERAL||LA61_0==20||LA61_0==25||LA61_0==27||LA61_0==29||LA61_0==32||(LA61_0 >= 35 && LA61_0 <= 36)||LA61_0==42||LA61_0==64||(LA61_0 >= 69 && LA61_0 <= 75)||(LA61_0 >= 77 && LA61_0 <= 93)||LA61_0==98) ) {
                alt61=1;
            }
            switch (alt61) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:756:6: stmt_list= statement_list
                    {
                    pushFollow(FOLLOW_statement_list_in_compound_statement2532);
                    stmt_list=statement_list();

                    state._fsp--;
                    if (state.failed) return statement;

                    if ( state.backtracking==0 ) {
                           statement.setStatement(stmt_list);
                       
                        }

                    }
                    break;

            }


            match(input,97,FOLLOW_97_in_compound_statement2547); if (state.failed) return statement;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return statement;
    }
    // $ANTLR end "compound_statement"



    // $ANTLR start "statement_list"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:764:1: statement_list returns [Statement.StatementList statement] : (stmt= statement )+ ;
    public final Statement.StatementList statement_list() throws RecognitionException {
        Statement.StatementList statement = null;


        Statement stmt =null;



        statement =new Statement.StatementList(null,-1);

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:769:2: ( (stmt= statement )+ )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:770:2: (stmt= statement )+
            {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:770:2: (stmt= statement )+
            int cnt62=0;
            loop62:
            do {
                int alt62=2;
                int LA62_0 = input.LA(1);

                if ( ((LA62_0 >= CHARACTER_LITERAL && LA62_0 <= DECIMAL_LITERAL)||LA62_0==FLOATING_POINT_LITERAL||LA62_0==HEX_LITERAL||LA62_0==IDENTIFIER||LA62_0==OCTAL_LITERAL||LA62_0==STRING_LITERAL||LA62_0==20||LA62_0==25||LA62_0==27||LA62_0==29||LA62_0==32||(LA62_0 >= 35 && LA62_0 <= 36)||LA62_0==42||LA62_0==64||(LA62_0 >= 69 && LA62_0 <= 75)||(LA62_0 >= 77 && LA62_0 <= 93)||LA62_0==98) ) {
                    alt62=1;
                }


                switch (alt62) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:771:2: stmt= statement
            	    {
            	    pushFollow(FOLLOW_statement_in_statement_list2570);
            	    stmt=statement();

            	    state._fsp--;
            	    if (state.failed) return statement;

            	    if ( state.backtracking==0 ) {
            	     	if(stmt!=null) statement.addStatement(stmt); 
            	     }

            	    }
            	    break;

            	default :
            	    if ( cnt62 >= 1 ) break loop62;
            	    if (state.backtracking>0) {state.failed=true; return statement;}
                        EarlyExitException eee =
                            new EarlyExitException(62, input);
                        throw eee;
                }
                cnt62++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return statement;
    }
    // $ANTLR end "statement_list"



    // $ANTLR start "expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:779:1: expression returns [Expression expression] : exp1= assignment_expression ( ',' expx= assignment_expression )* ;
    public final Expression expression() throws RecognitionException {
        Expression expression = null;


        AssignmentExpression exp1 =null;

        AssignmentExpression expx =null;



        expression =null;
        ListExpression listExpression=new ListExpression(null,-1);

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:788:2: (exp1= assignment_expression ( ',' expx= assignment_expression )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:789:2: exp1= assignment_expression ( ',' expx= assignment_expression )*
            {
            pushFollow(FOLLOW_assignment_expression_in_expression2604);
            exp1=assignment_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) {
            		listExpression.addExpression(exp1);
            		//src.addChild((Statement) exp1);//TODO the src.addChild here should be removed, only for testing purpose
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:794:2: ( ',' expx= assignment_expression )*
            loop63:
            do {
                int alt63=2;
                int LA63_0 = input.LA(1);

                if ( (LA63_0==34) ) {
                    alt63=1;
                }


                switch (alt63) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:795:3: ',' expx= assignment_expression
            	    {
            	    match(input,34,FOLLOW_34_in_expression2615); if (state.failed) return expression;

            	    pushFollow(FOLLOW_assignment_expression_in_expression2619);
            	    expx=assignment_expression();

            	    state._fsp--;
            	    if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {
            	    			listExpression.addExpression(expx);
            	    			//src.addChild((Statement) expx);//TODO the src.addChild here should be removed, only for testing purpose
            	    		}

            	    }
            	    break;

            	default :
            	    break loop63;
                }
            } while (true);


            }

            if ( state.backtracking==0 ) {
            expression =(Expression)listExpression; //At the end of rule returning the list expression
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "expression"



    // $ANTLR start "assignment_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:802:1: assignment_expression returns [AssignmentExpression expression] : exp1= conditional_expression (op= assignment_operator expx= assignment_expression )? ;
    public final AssignmentExpression assignment_expression() throws RecognitionException {
        AssignmentExpression expression = null;


        TertiaryExpression exp1 =null;

        AssignmentSymbol op =null;

        AssignmentExpression expx =null;



        	Expression prev=null; //Will tell the prvious expression added;
        	AssignmentExpression addTo=null; //Will tell where to add 
        	expression =new AssignmentExpression(null,-1,null);
        	addTo=expression;
        	AssignmentSymbol symbol=null;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:811:2: (exp1= conditional_expression (op= assignment_operator expx= assignment_expression )? )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:812:2: exp1= conditional_expression (op= assignment_operator expx= assignment_expression )?
            {
            pushFollow(FOLLOW_conditional_expression_in_assignment_expression2649);
            exp1=conditional_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) {
            		expression.setTarget((Expression)exp1);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:816:2: (op= assignment_operator expx= assignment_expression )?
            int alt64=2;
            int LA64_0 = input.LA(1);

            if ( (LA64_0==23||LA64_0==26||LA64_0==30||LA64_0==33||LA64_0==37||LA64_0==40||LA64_0==45||LA64_0==47||LA64_0==52||LA64_0==68||LA64_0==95) ) {
                alt64=1;
            }
            switch (alt64) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:817:3: op= assignment_operator expx= assignment_expression
                    {
                    pushFollow(FOLLOW_assignment_operator_in_assignment_expression2663);
                    op=assignment_operator();

                    state._fsp--;
                    if (state.failed) return expression;

                    if ( state.backtracking==0 ) {symbol=op;}

                    pushFollow(FOLLOW_assignment_expression_in_assignment_expression2674);
                    expx=assignment_expression();

                    state._fsp--;
                    if (state.failed) return expression;

                    if ( state.backtracking==0 ) {
                    			if(prev == null){
                    				expression.setOperator(op);
                    				expression.setValue((Expression) expx);
                    			}
                    			else{
                    				addTo=createAndMergeAssignmentExpression(null,-1,expression.getOperator(),prev,addTo,(Expression)expx);
                    			}
                    			prev=expx;
                    			
                    		}

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "assignment_expression"



    // $ANTLR start "assignment_operator"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:833:1: assignment_operator returns [AssignmentSymbol symbol] : ( '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|=' );
    public final AssignmentSymbol assignment_operator() throws RecognitionException {
        AssignmentSymbol symbol = null;


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:835:2: ( '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|=' )
            int alt65=11;
            switch ( input.LA(1) ) {
            case 47:
                {
                alt65=1;
                }
                break;
            case 30:
                {
                alt65=2;
                }
                break;
            case 40:
                {
                alt65=3;
                }
                break;
            case 23:
                {
                alt65=4;
                }
                break;
            case 33:
                {
                alt65=5;
                }
                break;
            case 37:
                {
                alt65=6;
                }
                break;
            case 45:
                {
                alt65=7;
                }
                break;
            case 52:
                {
                alt65=8;
                }
                break;
            case 26:
                {
                alt65=9;
                }
                break;
            case 68:
                {
                alt65=10;
                }
                break;
            case 95:
                {
                alt65=11;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return symbol;}
                NoViableAltException nvae =
                    new NoViableAltException("", 65, 0, input);

                throw nvae;

            }

            switch (alt65) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:836:4: '='
                    {
                    match(input,47,FOLLOW_47_in_assignment_operator2704); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =AssignmentOperator.AssignmentSymbol.EQUAL;}

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:837:4: '*='
                    {
                    match(input,30,FOLLOW_30_in_assignment_operator2711); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =AssignmentOperator.AssignmentSymbol.MUL_EQUAL;}

                    }
                    break;
                case 3 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:838:4: '/='
                    {
                    match(input,40,FOLLOW_40_in_assignment_operator2718); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =AssignmentOperator.AssignmentSymbol.DIV_EQUAL;}

                    }
                    break;
                case 4 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:839:4: '%='
                    {
                    match(input,23,FOLLOW_23_in_assignment_operator2725); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =AssignmentOperator.AssignmentSymbol.MOD_EQUAL;}

                    }
                    break;
                case 5 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:840:4: '+='
                    {
                    match(input,33,FOLLOW_33_in_assignment_operator2732); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =AssignmentOperator.AssignmentSymbol.ADD_EQUAL;}

                    }
                    break;
                case 6 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:841:4: '-='
                    {
                    match(input,37,FOLLOW_37_in_assignment_operator2739); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =AssignmentOperator.AssignmentSymbol.SUB_EQUAL;}

                    }
                    break;
                case 7 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:842:4: '<<='
                    {
                    match(input,45,FOLLOW_45_in_assignment_operator2746); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =AssignmentOperator.AssignmentSymbol.SHL_EQUAL;}

                    }
                    break;
                case 8 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:843:4: '>>='
                    {
                    match(input,52,FOLLOW_52_in_assignment_operator2753); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =AssignmentOperator.AssignmentSymbol.SHR_EQUAL;}

                    }
                    break;
                case 9 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:844:4: '&='
                    {
                    match(input,26,FOLLOW_26_in_assignment_operator2760); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =AssignmentOperator.AssignmentSymbol.BITWISE_AND_EQUAL;}

                    }
                    break;
                case 10 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:845:4: '^='
                    {
                    match(input,68,FOLLOW_68_in_assignment_operator2767); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =AssignmentOperator.AssignmentSymbol.BITWISE_XOR_EQUAL;}

                    }
                    break;
                case 11 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:846:4: '|='
                    {
                    match(input,95,FOLLOW_95_in_assignment_operator2774); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =AssignmentOperator.AssignmentSymbol.BITWISE_OR_EQUAL;}

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return symbol;
    }
    // $ANTLR end "assignment_operator"



    // $ANTLR start "conditional_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:850:1: conditional_expression returns [TertiaryExpression expression] : exp1= logical_or_expression ( '?' exp2= logical_or_expression ':' exp3= logical_or_expression )? ;
    public final TertiaryExpression conditional_expression() throws RecognitionException {
        TertiaryExpression expression = null;


        BinaryExpression exp1 =null;

        BinaryExpression exp2 =null;

        BinaryExpression exp3 =null;



        	expression =new TertiaryExpression(null,-1);

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:855:2: (exp1= logical_or_expression ( '?' exp2= logical_or_expression ':' exp3= logical_or_expression )? )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:856:2: exp1= logical_or_expression ( '?' exp2= logical_or_expression ':' exp3= logical_or_expression )?
            {
            pushFollow(FOLLOW_logical_or_expression_in_conditional_expression2801);
            exp1=logical_or_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) {
            		expression.setConditionExpression((Expression)exp1);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:860:2: ( '?' exp2= logical_or_expression ':' exp3= logical_or_expression )?
            int alt66=2;
            int LA66_0 = input.LA(1);

            if ( (LA66_0==53) ) {
                alt66=1;
            }
            switch (alt66) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:861:3: '?' exp2= logical_or_expression ':' exp3= logical_or_expression
                    {
                    match(input,53,FOLLOW_53_in_conditional_expression2812); if (state.failed) return expression;

                    pushFollow(FOLLOW_logical_or_expression_in_conditional_expression2816);
                    exp2=logical_or_expression();

                    state._fsp--;
                    if (state.failed) return expression;

                    if ( state.backtracking==0 ) {
                    			expression.setIfExpression((Expression)exp2);
                    		}

                    match(input,41,FOLLOW_41_in_conditional_expression2825); if (state.failed) return expression;

                    pushFollow(FOLLOW_logical_or_expression_in_conditional_expression2829);
                    exp3=logical_or_expression();

                    state._fsp--;
                    if (state.failed) return expression;

                    if ( state.backtracking==0 ) {
                    			expression.setElseExpression((Expression)exp3);
                    		}

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "conditional_expression"



    // $ANTLR start "constant_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:871:1: constant_expression returns [Expression expression] : exp= conditional_expression ;
    public final Expression constant_expression() throws RecognitionException {
        Expression expression = null;


        TertiaryExpression exp =null;


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:873:3: (exp= conditional_expression )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:873:3: exp= conditional_expression
            {
            pushFollow(FOLLOW_conditional_expression_in_constant_expression2856);
            exp=conditional_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) { expression = exp; }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "constant_expression"



    // $ANTLR start "logical_or_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:875:1: logical_or_expression returns [BinaryExpression expression] : exp1= logical_and_expression ( '||' expx= logical_and_expression )* ;
    public final BinaryExpression logical_or_expression() throws RecognitionException {
        BinaryExpression expression = null;


        BinaryExpression exp1 =null;

        BinaryExpression expx =null;



        	Expression prev=null; //Will tell the prvious expression added;
        	BinaryExpression addTo=null; //Will tell where to add
        	expression =new BinaryExpression(null,-1,null);
        	addTo=expression;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:883:2: (exp1= logical_and_expression ( '||' expx= logical_and_expression )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:884:2: exp1= logical_and_expression ( '||' expx= logical_and_expression )*
            {
            pushFollow(FOLLOW_logical_and_expression_in_logical_or_expression2879);
            exp1=logical_and_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) {
            		expression.setLhs((Expression)exp1);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:888:2: ( '||' expx= logical_and_expression )*
            loop67:
            do {
                int alt67=2;
                int LA67_0 = input.LA(1);

                if ( (LA67_0==96) ) {
                    alt67=1;
                }


                switch (alt67) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:889:3: '||' expx= logical_and_expression
            	    {
            	    match(input,96,FOLLOW_96_in_logical_or_expression2890); if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {expression.setOperator(BinaryOperator.LogicalOperator.OR);}

            	    pushFollow(FOLLOW_logical_and_expression_in_logical_or_expression2902);
            	    expx=logical_and_expression();

            	    state._fsp--;
            	    if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {
            	    			if(prev == null){
            	    				expression.setRhs((Expression) expx);
            	    			}
            	    			else{
            	    				addTo=createAndMergeBinaryExpression(null,-1,expression.getOperator(),prev,addTo,(Expression)expx);
            	    			}
            	    			prev=expx;
            	    			
            	    		}

            	    }
            	    break;

            	default :
            	    break loop67;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "logical_or_expression"



    // $ANTLR start "logical_and_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:903:1: logical_and_expression returns [BinaryExpression expression] : exp1= inclusive_or_expression ( '&&' expx= inclusive_or_expression )* ;
    public final BinaryExpression logical_and_expression() throws RecognitionException {
        BinaryExpression expression = null;


        BinaryExpression exp1 =null;

        BinaryExpression expx =null;



        	Expression prev=null; //Will tell the prvious expression added;
        	BinaryExpression addTo=null; //Will tell where to add
        	expression =new BinaryExpression(null,-1,null);
        	addTo=expression;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:911:2: (exp1= inclusive_or_expression ( '&&' expx= inclusive_or_expression )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:912:2: exp1= inclusive_or_expression ( '&&' expx= inclusive_or_expression )*
            {
            pushFollow(FOLLOW_inclusive_or_expression_in_logical_and_expression2930);
            exp1=inclusive_or_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) {
            		expression.setLhs((Expression)exp1);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:915:2: ( '&&' expx= inclusive_or_expression )*
            loop68:
            do {
                int alt68=2;
                int LA68_0 = input.LA(1);

                if ( (LA68_0==24) ) {
                    alt68=1;
                }


                switch (alt68) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:916:3: '&&' expx= inclusive_or_expression
            	    {
            	    match(input,24,FOLLOW_24_in_logical_and_expression2939); if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {expression.setOperator(BinaryOperator.LogicalOperator.AND);}

            	    pushFollow(FOLLOW_inclusive_or_expression_in_logical_and_expression2950);
            	    expx=inclusive_or_expression();

            	    state._fsp--;
            	    if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {
            	    			if(prev == null){
            	    				expression.setRhs((Expression) expx);
            	    			}
            	    			else{
            	    				addTo=createAndMergeBinaryExpression(null,-1,expression.getOperator(),prev,addTo,(Expression)expx);
            	    			}
            	    			prev=expx;
            	    			
            	    		}

            	    }
            	    break;

            	default :
            	    break loop68;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "logical_and_expression"



    // $ANTLR start "inclusive_or_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:931:1: inclusive_or_expression returns [BinaryExpression expression] : exp1= exclusive_or_expression ( '|' expx= exclusive_or_expression )* ;
    public final BinaryExpression inclusive_or_expression() throws RecognitionException {
        BinaryExpression expression = null;


        BinaryExpression exp1 =null;

        BinaryExpression expx =null;



        	Expression prev=null; //Will tell the prvious expression added;
        	BinaryExpression addTo=null; //Will tell where to add
        	expression =new BinaryExpression(null,-1,null);
        	addTo=expression;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:939:2: (exp1= exclusive_or_expression ( '|' expx= exclusive_or_expression )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:940:2: exp1= exclusive_or_expression ( '|' expx= exclusive_or_expression )*
            {
            pushFollow(FOLLOW_exclusive_or_expression_in_inclusive_or_expression2980);
            exp1=exclusive_or_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) {
            		expression.setLhs((Expression)exp1);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:944:2: ( '|' expx= exclusive_or_expression )*
            loop69:
            do {
                int alt69=2;
                int LA69_0 = input.LA(1);

                if ( (LA69_0==94) ) {
                    alt69=1;
                }


                switch (alt69) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:945:3: '|' expx= exclusive_or_expression
            	    {
            	    match(input,94,FOLLOW_94_in_inclusive_or_expression2991); if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {expression.setOperator(BinaryOperator.BitwiseOperator.OR);}

            	    pushFollow(FOLLOW_exclusive_or_expression_in_inclusive_or_expression3003);
            	    expx=exclusive_or_expression();

            	    state._fsp--;
            	    if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {
            	    			if(prev == null){
            	    				expression.setRhs((Expression) expx);
            	    			}
            	    			else{
            	    				addTo=createAndMergeBinaryExpression(null,-1,expression.getOperator(),prev,addTo,(Expression)expx);
            	    			}
            	    			prev=expx;
            	    			
            	    		}

            	    }
            	    break;

            	default :
            	    break loop69;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "inclusive_or_expression"



    // $ANTLR start "exclusive_or_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:960:1: exclusive_or_expression returns [BinaryExpression expression] : exp1= and_expression ( '^' expx= and_expression )* ;
    public final BinaryExpression exclusive_or_expression() throws RecognitionException {
        BinaryExpression expression = null;


        BinaryExpression exp1 =null;

        BinaryExpression expx =null;



        	Expression prev=null; //Will tell the prvious expression added;
        	BinaryExpression addTo=null; //Will tell where to add
        	expression =new BinaryExpression(null,-1,null);
        	addTo=expression;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:968:2: (exp1= and_expression ( '^' expx= and_expression )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:969:2: exp1= and_expression ( '^' expx= and_expression )*
            {
            pushFollow(FOLLOW_and_expression_in_exclusive_or_expression3033);
            exp1=and_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) {
            		expression.setLhs((Expression)exp1);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:973:2: ( '^' expx= and_expression )*
            loop70:
            do {
                int alt70=2;
                int LA70_0 = input.LA(1);

                if ( (LA70_0==67) ) {
                    alt70=1;
                }


                switch (alt70) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:974:3: '^' expx= and_expression
            	    {
            	    match(input,67,FOLLOW_67_in_exclusive_or_expression3044); if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {expression.setOperator(BinaryOperator.BitwiseOperator.XOR);}

            	    pushFollow(FOLLOW_and_expression_in_exclusive_or_expression3056);
            	    expx=and_expression();

            	    state._fsp--;
            	    if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {
            	    			if(prev == null){
            	    				expression.setRhs((Expression) expx);
            	    			}
            	    			else{
            	    				addTo=createAndMergeBinaryExpression(null,-1,expression.getOperator(),prev,addTo,(Expression)expx);
            	    			}
            	    			prev=expx;
            	    			
            	    		}

            	    }
            	    break;

            	default :
            	    break loop70;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "exclusive_or_expression"



    // $ANTLR start "and_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:989:1: and_expression returns [BinaryExpression expression] : exp1= equality_expression ( '&' expx= equality_expression )* ;
    public final BinaryExpression and_expression() throws RecognitionException {
        BinaryExpression expression = null;


        BinaryExpression exp1 =null;

        BinaryExpression expx =null;



        	Expression prev=null; //Will tell the prvious expression added;
        	BinaryExpression addTo=null; //Will tell where to add
        	expression =new BinaryExpression(null,-1,null);
        	addTo=expression;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:997:2: (exp1= equality_expression ( '&' expx= equality_expression )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:998:2: exp1= equality_expression ( '&' expx= equality_expression )*
            {
            pushFollow(FOLLOW_equality_expression_in_and_expression3086);
            exp1=equality_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) {
            		expression.setLhs((Expression)exp1);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1002:2: ( '&' expx= equality_expression )*
            loop71:
            do {
                int alt71=2;
                int LA71_0 = input.LA(1);

                if ( (LA71_0==25) ) {
                    alt71=1;
                }


                switch (alt71) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1003:3: '&' expx= equality_expression
            	    {
            	    match(input,25,FOLLOW_25_in_and_expression3097); if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {expression.setOperator(BinaryOperator.BitwiseOperator.AND);}

            	    pushFollow(FOLLOW_equality_expression_in_and_expression3108);
            	    expx=equality_expression();

            	    state._fsp--;
            	    if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {
            	    			if(prev == null){
            	    				expression.setRhs((Expression) expx);
            	    			}
            	    			else{
            	    				addTo=createAndMergeBinaryExpression(null,-1,expression.getOperator(),prev,addTo,(Expression)expx);
            	    			}
            	    			prev=expx;
            	    			
            	    		}

            	    }
            	    break;

            	default :
            	    break loop71;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "and_expression"



    // $ANTLR start "equality_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1018:1: equality_expression returns [BinaryExpression expression] : exp1= relational_expression ( ( '!=' | '==' ) expx= relational_expression )* ;
    public final BinaryExpression equality_expression() throws RecognitionException {
        BinaryExpression expression = null;


        BinaryExpression exp1 =null;

        BinaryExpression expx =null;



        	Expression prev=null; //Will tell the prvious expression added;
        	BinaryExpression addTo=null; //Will tell where to add
        	BinarySymbol op=null;
        	expression =new BinaryExpression(null,-1,null);
        	addTo=expression;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1027:2: (exp1= relational_expression ( ( '!=' | '==' ) expx= relational_expression )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1028:2: exp1= relational_expression ( ( '!=' | '==' ) expx= relational_expression )*
            {
            pushFollow(FOLLOW_relational_expression_in_equality_expression3138);
            exp1=relational_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) { 
            		expression.setLhs((Expression)exp1);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1032:2: ( ( '!=' | '==' ) expx= relational_expression )*
            loop73:
            do {
                int alt73=2;
                int LA73_0 = input.LA(1);

                if ( (LA73_0==21||LA73_0==48) ) {
                    alt73=1;
                }


                switch (alt73) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1033:3: ( '!=' | '==' ) expx= relational_expression
            	    {
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1033:3: ( '!=' | '==' )
            	    int alt72=2;
            	    int LA72_0 = input.LA(1);

            	    if ( (LA72_0==21) ) {
            	        alt72=1;
            	    }
            	    else if ( (LA72_0==48) ) {
            	        alt72=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return expression;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 72, 0, input);

            	        throw nvae;

            	    }
            	    switch (alt72) {
            	        case 1 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1034:4: '!='
            	            {
            	            match(input,21,FOLLOW_21_in_equality_expression3155); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.RelationalOperator.NOT_EQUAL;}

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1035:6: '=='
            	            {
            	            match(input,48,FOLLOW_48_in_equality_expression3165); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.RelationalOperator.EQUAL;}

            	            }
            	            break;

            	    }


            	    pushFollow(FOLLOW_relational_expression_in_equality_expression3182);
            	    expx=relational_expression();

            	    state._fsp--;
            	    if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {
            	    			if(prev == null){
            	    				expression.setOperator(op);
            	    				expression.setRhs((Expression) expx);
            	    			}
            	    			else{
            	    				addTo=createAndMergeBinaryExpression(null,-1,expression.getOperator(),prev,addTo,(Expression)expx);
            	    			}
            	    			prev=expx;
            	    			
            	    		}

            	    }
            	    break;

            	default :
            	    break loop73;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "equality_expression"



    // $ANTLR start "relational_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1051:1: relational_expression returns [BinaryExpression expression] : exp1= shift_expression ( ( '<' | '>' | '<=' | '>=' ) expx= shift_expression )* ;
    public final BinaryExpression relational_expression() throws RecognitionException {
        BinaryExpression expression = null;


        BinaryExpression exp1 =null;

        BinaryExpression expx =null;



        	Expression prev=null; //Will tell the prvious expression added;
        	BinaryExpression addTo=null; //Will tell where to add
        	BinarySymbol op=null;
        	expression =new BinaryExpression(null,-1,null);
        	addTo=expression;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1060:2: (exp1= shift_expression ( ( '<' | '>' | '<=' | '>=' ) expx= shift_expression )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1061:2: exp1= shift_expression ( ( '<' | '>' | '<=' | '>=' ) expx= shift_expression )*
            {
            pushFollow(FOLLOW_shift_expression_in_relational_expression3209);
            exp1=shift_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) { 
            		expression.setLhs((Expression)exp1);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1064:2: ( ( '<' | '>' | '<=' | '>=' ) expx= shift_expression )*
            loop75:
            do {
                int alt75=2;
                int LA75_0 = input.LA(1);

                if ( (LA75_0==43||LA75_0==46||(LA75_0 >= 49 && LA75_0 <= 50)) ) {
                    alt75=1;
                }


                switch (alt75) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1065:3: ( '<' | '>' | '<=' | '>=' ) expx= shift_expression
            	    {
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1065:3: ( '<' | '>' | '<=' | '>=' )
            	    int alt74=4;
            	    switch ( input.LA(1) ) {
            	    case 43:
            	        {
            	        alt74=1;
            	        }
            	        break;
            	    case 49:
            	        {
            	        alt74=2;
            	        }
            	        break;
            	    case 46:
            	        {
            	        alt74=3;
            	        }
            	        break;
            	    case 50:
            	        {
            	        alt74=4;
            	        }
            	        break;
            	    default:
            	        if (state.backtracking>0) {state.failed=true; return expression;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 74, 0, input);

            	        throw nvae;

            	    }

            	    switch (alt74) {
            	        case 1 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1066:4: '<'
            	            {
            	            match(input,43,FOLLOW_43_in_relational_expression3224); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.RelationalOperator.LESS;}

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1067:6: '>'
            	            {
            	            match(input,49,FOLLOW_49_in_relational_expression3234); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.RelationalOperator.GREATER;}

            	            }
            	            break;
            	        case 3 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1068:6: '<='
            	            {
            	            match(input,46,FOLLOW_46_in_relational_expression3244); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.RelationalOperator.LESS_EQUAL;}

            	            }
            	            break;
            	        case 4 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1069:6: '>='
            	            {
            	            match(input,50,FOLLOW_50_in_relational_expression3254); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.RelationalOperator.GREATER_EQUAL;}

            	            }
            	            break;

            	    }


            	    pushFollow(FOLLOW_shift_expression_in_relational_expression3271);
            	    expx=shift_expression();

            	    state._fsp--;
            	    if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {
            	    			if(prev == null){
            	    				expression.setOperator(op);
            	    				expression.setRhs((Expression) expx);
            	    			}
            	    			else{
            	    				addTo=createAndMergeBinaryExpression(null,-1,expression.getOperator(),prev,addTo,(Expression)expx);
            	    			}
            	    			prev=expx;
            	    			
            	    		}

            	    }
            	    break;

            	default :
            	    break loop75;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "relational_expression"



    // $ANTLR start "shift_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1085:1: shift_expression returns [BinaryExpression expression] : exp1= additive_expression ( ( '<<' | '>>' ) expx= shift_expression )* ;
    public final BinaryExpression shift_expression() throws RecognitionException {
        BinaryExpression expression = null;


        BinaryExpression exp1 =null;

        BinaryExpression expx =null;



        	Expression prev=null; //Will tell the prvious expression added;
        	BinaryExpression addTo=null; //Will tell where to add
        	BinarySymbol op=null;
        	expression =new BinaryExpression(null,-1,null);
        	addTo=expression;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1094:2: (exp1= additive_expression ( ( '<<' | '>>' ) expx= shift_expression )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1095:2: exp1= additive_expression ( ( '<<' | '>>' ) expx= shift_expression )*
            {
            pushFollow(FOLLOW_additive_expression_in_shift_expression3297);
            exp1=additive_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) { 
            		expression.setLhs((Expression)exp1);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1098:2: ( ( '<<' | '>>' ) expx= shift_expression )*
            loop77:
            do {
                int alt77=2;
                int LA77_0 = input.LA(1);

                if ( (LA77_0==44) ) {
                    int LA77_2 = input.LA(2);

                    if ( (synpred123_ObjectiveC()) ) {
                        alt77=1;
                    }


                }
                else if ( (LA77_0==51) ) {
                    int LA77_3 = input.LA(2);

                    if ( (synpred123_ObjectiveC()) ) {
                        alt77=1;
                    }


                }


                switch (alt77) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1099:3: ( '<<' | '>>' ) expx= shift_expression
            	    {
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1099:3: ( '<<' | '>>' )
            	    int alt76=2;
            	    int LA76_0 = input.LA(1);

            	    if ( (LA76_0==44) ) {
            	        alt76=1;
            	    }
            	    else if ( (LA76_0==51) ) {
            	        alt76=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return expression;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 76, 0, input);

            	        throw nvae;

            	    }
            	    switch (alt76) {
            	        case 1 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1100:4: '<<'
            	            {
            	            match(input,44,FOLLOW_44_in_shift_expression3312); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.ShiftOperator.SHL;}

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1101:6: '>>'
            	            {
            	            match(input,51,FOLLOW_51_in_shift_expression3322); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.ShiftOperator.SHR;}

            	            }
            	            break;

            	    }


            	    pushFollow(FOLLOW_shift_expression_in_shift_expression3338);
            	    expx=shift_expression();

            	    state._fsp--;
            	    if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {
            	    			if(prev == null){
            	    				expression.setOperator(op);
            	    				expression.setRhs((Expression) expx);
            	    			}
            	    			else{
            	    				addTo=createAndMergeBinaryExpression(null,-1,expression.getOperator(),prev,addTo,(Expression)expx);
            	    			}
            	    			prev=expx;
            	    			
            	    		}

            	    }
            	    break;

            	default :
            	    break loop77;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "shift_expression"



    // $ANTLR start "additive_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1117:1: additive_expression returns [BinaryExpression expression] : exp1= multiplicative_expression ( ( '+' | '-' ) expx= multiplicative_expression )* ;
    public final BinaryExpression additive_expression() throws RecognitionException {
        BinaryExpression expression = null;


        BinaryExpression exp1 =null;

        BinaryExpression expx =null;



        	Expression prev=null; //Will tell the prvious expression added;
        	BinaryExpression addTo=null; //Will tell where to add
        	BinarySymbol op=null;
        	expression =new BinaryExpression(null,-1,null);
        	addTo=expression;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1126:2: (exp1= multiplicative_expression ( ( '+' | '-' ) expx= multiplicative_expression )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1127:2: exp1= multiplicative_expression ( ( '+' | '-' ) expx= multiplicative_expression )*
            {
            pushFollow(FOLLOW_multiplicative_expression_in_additive_expression3365);
            exp1=multiplicative_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) { 
            		expression.setLhs((Expression)exp1);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1130:2: ( ( '+' | '-' ) expx= multiplicative_expression )*
            loop79:
            do {
                int alt79=2;
                int LA79_0 = input.LA(1);

                if ( (LA79_0==31||LA79_0==35) ) {
                    alt79=1;
                }


                switch (alt79) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1131:3: ( '+' | '-' ) expx= multiplicative_expression
            	    {
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1131:3: ( '+' | '-' )
            	    int alt78=2;
            	    int LA78_0 = input.LA(1);

            	    if ( (LA78_0==31) ) {
            	        alt78=1;
            	    }
            	    else if ( (LA78_0==35) ) {
            	        alt78=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return expression;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 78, 0, input);

            	        throw nvae;

            	    }
            	    switch (alt78) {
            	        case 1 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1132:4: '+'
            	            {
            	            match(input,31,FOLLOW_31_in_additive_expression3380); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.ArithOperator.ADD;}

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1133:5: '-'
            	            {
            	            match(input,35,FOLLOW_35_in_additive_expression3389); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.ArithOperator.SUB;}

            	            }
            	            break;

            	    }


            	    pushFollow(FOLLOW_multiplicative_expression_in_additive_expression3405);
            	    expx=multiplicative_expression();

            	    state._fsp--;
            	    if (state.failed) return expression;

            	    if ( state.backtracking==0 ) {
            	    			if(prev == null){
            	    				expression.setOperator(op);
            	    				expression.setRhs((Expression) expx);
            	    			}
            	    			else{
            	    				addTo=createAndMergeBinaryExpression(null,-1,expression.getOperator(),prev,addTo,(Expression)expx);
            	    			}
            	    			prev=expx;
            	    			
            	    		}

            	    }
            	    break;

            	default :
            	    break loop79;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "additive_expression"



    // $ANTLR start "multiplicative_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1149:1: multiplicative_expression returns [BinaryExpression expression] : exp1= cast_expression ( ( '*' | '/' | '%' ) expx= cast_expression )* ;
    public final BinaryExpression multiplicative_expression() throws RecognitionException {
        BinaryExpression expression = null;


        Expression exp1 =null;

        Expression expx =null;



        	Expression prev=null; //Will tell the prvious expression added;
        	BinaryExpression addTo=null; //Will tell where to add
        	BinarySymbol op=null;
        	expression =new BinaryExpression(null,-1,null);
        	addTo=expression;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1158:2: (exp1= cast_expression ( ( '*' | '/' | '%' ) expx= cast_expression )* )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1159:2: exp1= cast_expression ( ( '*' | '/' | '%' ) expx= cast_expression )*
            {
            pushFollow(FOLLOW_cast_expression_in_multiplicative_expression3432);
            exp1=cast_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) { 
            		expression.setLhs((Expression)exp1);
            	}

            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1162:2: ( ( '*' | '/' | '%' ) expx= cast_expression )*
            loop81:
            do {
                int alt81=2;
                int LA81_0 = input.LA(1);

                if ( (LA81_0==22||LA81_0==29||LA81_0==39) ) {
                    alt81=1;
                }


                switch (alt81) {
            	case 1 :
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1163:3: ( '*' | '/' | '%' ) expx= cast_expression
            	    {
            	    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1163:3: ( '*' | '/' | '%' )
            	    int alt80=3;
            	    switch ( input.LA(1) ) {
            	    case 29:
            	        {
            	        alt80=1;
            	        }
            	        break;
            	    case 39:
            	        {
            	        alt80=2;
            	        }
            	        break;
            	    case 22:
            	        {
            	        alt80=3;
            	        }
            	        break;
            	    default:
            	        if (state.backtracking>0) {state.failed=true; return expression;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 80, 0, input);

            	        throw nvae;

            	    }

            	    switch (alt80) {
            	        case 1 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1164:4: '*'
            	            {
            	            match(input,29,FOLLOW_29_in_multiplicative_expression3446); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.ArithOperator.MUL;}

            	            }
            	            break;
            	        case 2 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1165:6: '/'
            	            {
            	            match(input,39,FOLLOW_39_in_multiplicative_expression3456); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.ArithOperator.DIV;}

            	            }
            	            break;
            	        case 3 :
            	            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1166:6: '%'
            	            {
            	            match(input,22,FOLLOW_22_in_multiplicative_expression3466); if (state.failed) return expression;

            	            if ( state.backtracking==0 ) {op=BinaryOperator.ArithOperator.MOD;}

            	            }
            	            break;

            	    }


            	    pushFollow(FOLLOW_cast_expression_in_multiplicative_expression3482);
            	    expx=cast_expression();

            	    state._fsp--;
            	    if (state.failed) return expression;

            	    if ( state.backtracking==0 ) { 
            	    			if(prev == null){
            	    				expression.setOperator(op);
            	    				expression.setRhs((Expression) expx);
            	    			}
            	    			else{
            	    				addTo=createAndMergeBinaryExpression(null,-1,expression.getOperator(),prev,addTo,(Expression)expx);
            	    			}
            	    			prev=expx;
            	    			
            	    		}

            	    }
            	    break;

            	default :
            	    break loop81;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "multiplicative_expression"



    // $ANTLR start "cast_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1182:1: cast_expression returns [Expression expression] : exp= unary_expression ;
    public final Expression cast_expression() throws RecognitionException {
        Expression expression = null;


        UnaryExpression exp =null;


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1184:2: (exp= unary_expression )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1185:2: exp= unary_expression
            {
            pushFollow(FOLLOW_unary_expression_in_cast_expression3507);
            exp=unary_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) {expression =exp;}

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "cast_expression"



    // $ANTLR start "unary_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1187:1: unary_expression returns [UnaryExpression expression] : (exp= postfix_expression | '++' exp= unary_expression | '--' exp= unary_expression |op= unary_operator exp= cast_expression );
    public final UnaryExpression unary_expression() throws RecognitionException {
        UnaryExpression expression = null;


        Expression exp =null;

        UnarySymbol op =null;



        	UnarySymbol operator=null;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1192:2: (exp= postfix_expression | '++' exp= unary_expression | '--' exp= unary_expression |op= unary_operator exp= cast_expression )
            int alt82=4;
            switch ( input.LA(1) ) {
            case CHARACTER_LITERAL:
            case DECIMAL_LITERAL:
            case FLOATING_POINT_LITERAL:
            case HEX_LITERAL:
            case IDENTIFIER:
            case OCTAL_LITERAL:
            case STRING_LITERAL:
            case 27:
                {
                alt82=1;
                }
                break;
            case 32:
                {
                alt82=2;
                }
                break;
            case 36:
                {
                alt82=3;
                }
                break;
            case 20:
            case 25:
            case 29:
            case 35:
            case 98:
                {
                alt82=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return expression;}
                NoViableAltException nvae =
                    new NoViableAltException("", 82, 0, input);

                throw nvae;

            }

            switch (alt82) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1193:4: exp= postfix_expression
                    {
                    pushFollow(FOLLOW_postfix_expression_in_unary_expression3534);
                    exp=postfix_expression();

                    state._fsp--;
                    if (state.failed) return expression;

                    if ( state.backtracking==0 ) {expression =new UnaryExpression(null,-1,null,(Expression)exp);}

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1194:4: '++' exp= unary_expression
                    {
                    match(input,32,FOLLOW_32_in_unary_expression3541); if (state.failed) return expression;

                    pushFollow(FOLLOW_unary_expression_in_unary_expression3545);
                    exp=unary_expression();

                    state._fsp--;
                    if (state.failed) return expression;

                    if ( state.backtracking==0 ) {expression =new UnaryExpression(null,-1,UnarySymbol.PRE_INC,(Expression)exp);}

                    }
                    break;
                case 3 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1195:4: '--' exp= unary_expression
                    {
                    match(input,36,FOLLOW_36_in_unary_expression3553); if (state.failed) return expression;

                    pushFollow(FOLLOW_unary_expression_in_unary_expression3557);
                    exp=unary_expression();

                    state._fsp--;
                    if (state.failed) return expression;

                    if ( state.backtracking==0 ) {expression =new UnaryExpression(null,-1,UnarySymbol.PRE_DEC,(Expression)exp);}

                    }
                    break;
                case 4 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1196:4: op= unary_operator exp= cast_expression
                    {
                    pushFollow(FOLLOW_unary_operator_in_unary_expression3567);
                    op=unary_operator();

                    state._fsp--;
                    if (state.failed) return expression;

                    if ( state.backtracking==0 ) {operator=op;}

                    pushFollow(FOLLOW_cast_expression_in_unary_expression3572);
                    exp=cast_expression();

                    state._fsp--;
                    if (state.failed) return expression;

                    if ( state.backtracking==0 ) {expression =new UnaryExpression(null,-1,operator,(Expression)exp);}

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "unary_expression"



    // $ANTLR start "unary_operator"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1199:1: unary_operator returns [UnarySymbol symbol] : ( '&' | '*' | '-' | '~' | '!' );
    public final UnarySymbol unary_operator() throws RecognitionException {
        UnarySymbol symbol = null;


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1201:2: ( '&' | '*' | '-' | '~' | '!' )
            int alt83=5;
            switch ( input.LA(1) ) {
            case 25:
                {
                alt83=1;
                }
                break;
            case 29:
                {
                alt83=2;
                }
                break;
            case 35:
                {
                alt83=3;
                }
                break;
            case 98:
                {
                alt83=4;
                }
                break;
            case 20:
                {
                alt83=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return symbol;}
                NoViableAltException nvae =
                    new NoViableAltException("", 83, 0, input);

                throw nvae;

            }

            switch (alt83) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1202:4: '&'
                    {
                    match(input,25,FOLLOW_25_in_unary_operator3594); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =UnaryOperator.UnarySymbol.REFERENCE;}

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1203:4: '*'
                    {
                    match(input,29,FOLLOW_29_in_unary_operator3602); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =UnaryOperator.UnarySymbol.DEREFERENCE;}

                    }
                    break;
                case 3 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1204:4: '-'
                    {
                    match(input,35,FOLLOW_35_in_unary_operator3609); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =UnaryOperator.UnarySymbol.SIGN_MINUS;}

                    }
                    break;
                case 4 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1205:4: '~'
                    {
                    match(input,98,FOLLOW_98_in_unary_operator3616); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =UnaryOperator.UnarySymbol.BITWISE_NOT;}

                    }
                    break;
                case 5 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1206:4: '!'
                    {
                    match(input,20,FOLLOW_20_in_unary_operator3623); if (state.failed) return symbol;

                    if ( state.backtracking==0 ) {symbol =UnaryOperator.UnarySymbol.LOGICAL_NOT;}

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return symbol;
    }
    // $ANTLR end "unary_operator"



    // $ANTLR start "postfix_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1210:1: postfix_expression returns [PostfixExpression expression] : exp1= primary_expression ;
    public final PostfixExpression postfix_expression() throws RecognitionException {
        PostfixExpression expression = null;


        PrimaryExpression exp1 =null;



        	expression =new PostfixExpression(null,-1);

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1215:2: (exp1= primary_expression )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1216:2: exp1= primary_expression
            {
            pushFollow(FOLLOW_primary_expression_in_postfix_expression3649);
            exp1=primary_expression();

            state._fsp--;
            if (state.failed) return expression;

            if ( state.backtracking==0 ) {expression.setPrimaryExpression(exp1);}

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "postfix_expression"



    // $ANTLR start "primary_expression"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1223:1: primary_expression returns [PrimaryExpression expression] : (id1= IDENTIFIER |id2= constant |id3= STRING_LITERAL | ( '(' exp= assignment_expression ')' ) );
    public final PrimaryExpression primary_expression() throws RecognitionException {
        PrimaryExpression expression = null;


        Token id1=null;
        Token id3=null;
        ObjectiveCParser.constant_return id2 =null;

        AssignmentExpression exp =null;


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1225:2: (id1= IDENTIFIER |id2= constant |id3= STRING_LITERAL | ( '(' exp= assignment_expression ')' ) )
            int alt84=4;
            switch ( input.LA(1) ) {
            case IDENTIFIER:
                {
                alt84=1;
                }
                break;
            case CHARACTER_LITERAL:
            case DECIMAL_LITERAL:
            case FLOATING_POINT_LITERAL:
            case HEX_LITERAL:
            case OCTAL_LITERAL:
                {
                alt84=2;
                }
                break;
            case STRING_LITERAL:
                {
                alt84=3;
                }
                break;
            case 27:
                {
                alt84=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return expression;}
                NoViableAltException nvae =
                    new NoViableAltException("", 84, 0, input);

                throw nvae;

            }

            switch (alt84) {
                case 1 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1226:4: id1= IDENTIFIER
                    {
                    id1=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_primary_expression3679); if (state.failed) return expression;

                    if ( state.backtracking==0 ) {expression =new PrimaryExpression(null,-1,(id1!=null?id1.getText():null),RefType.IDENTIFIER);}

                    }
                    break;
                case 2 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1227:4: id2= constant
                    {
                    pushFollow(FOLLOW_constant_in_primary_expression3688);
                    id2=constant();

                    state._fsp--;
                    if (state.failed) return expression;

                    if ( state.backtracking==0 ) {expression =new PrimaryExpression(null,-1,(id2!=null?input.toString(id2.start,id2.stop):null),RefType.CONSTANT);}

                    }
                    break;
                case 3 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1228:4: id3= STRING_LITERAL
                    {
                    id3=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_primary_expression3697); if (state.failed) return expression;

                    if ( state.backtracking==0 ) {expression =new PrimaryExpression(null,-1,(id3!=null?id3.getText():null),RefType.STRING);}

                    }
                    break;
                case 4 :
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1229:4: ( '(' exp= assignment_expression ')' )
                    {
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1229:4: ( '(' exp= assignment_expression ')' )
                    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1229:5: '(' exp= assignment_expression ')'
                    {
                    match(input,27,FOLLOW_27_in_primary_expression3705); if (state.failed) return expression;

                    pushFollow(FOLLOW_assignment_expression_in_primary_expression3709);
                    exp=assignment_expression();

                    state._fsp--;
                    if (state.failed) return expression;

                    match(input,28,FOLLOW_28_in_primary_expression3711); if (state.failed) return expression;

                    }


                    if ( state.backtracking==0 ) {expression =new PrimaryExpression(null,-1,exp);expression.setHasParanthesis(true);}

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return expression;
    }
    // $ANTLR end "primary_expression"


    public static class identifier_return extends ParserRuleReturnScope {
        public Identifier identifier;
    };


    // $ANTLR start "identifier"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1234:1: identifier returns [ Identifier identifier ] : v= IDENTIFIER ;
    public final ObjectiveCParser.identifier_return identifier() throws RecognitionException {
        ObjectiveCParser.identifier_return retval = new ObjectiveCParser.identifier_return();
        retval.start = input.LT(1);


        Token v=null;

        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1236:3: (v= IDENTIFIER )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1236:3: v= IDENTIFIER
            {
            v=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_identifier3737); if (state.failed) return retval;

            if ( state.backtracking==0 ) { retval.identifier = new Identifier( input.toString(retval.start,input.LT(-1)) ); }

            }

            retval.stop = input.LT(-1);


        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "identifier"


    public static class constant_return extends ParserRuleReturnScope {
    };


    // $ANTLR start "constant"
    // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1238:1: constant : ( DECIMAL_LITERAL | HEX_LITERAL | OCTAL_LITERAL | CHARACTER_LITERAL | FLOATING_POINT_LITERAL );
    public final ObjectiveCParser.constant_return constant() throws RecognitionException {
        ObjectiveCParser.constant_return retval = new ObjectiveCParser.constant_return();
        retval.start = input.LT(1);


        try {
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1238:9: ( DECIMAL_LITERAL | HEX_LITERAL | OCTAL_LITERAL | CHARACTER_LITERAL | FLOATING_POINT_LITERAL )
            // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:
            {
            if ( (input.LA(1) >= CHARACTER_LITERAL && input.LA(1) <= DECIMAL_LITERAL)||input.LA(1)==FLOATING_POINT_LITERAL||input.LA(1)==HEX_LITERAL||input.LA(1)==OCTAL_LITERAL ) {
                input.consume();
                state.errorRecovery=false;
                state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "constant"

    // $ANTLR start synpred2_ObjectiveC
    public final void synpred2_ObjectiveC_fragment() throws RecognitionException {
        ListDeclaration d =null;


        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:93:3: (d= declaration )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:93:3: d= declaration
        {
        pushFollow(FOLLOW_declaration_in_synpred2_ObjectiveC89);
        d=declaration();

        state._fsp--;
        if (state.failed) return ;

        }

    }
    // $ANTLR end synpred2_ObjectiveC

    // $ANTLR start synpred3_ObjectiveC
    public final void synpred3_ObjectiveC_fragment() throws RecognitionException {
        Statement stmt =null;


        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:94:5: (stmt= statement )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:94:5: stmt= statement
        {
        pushFollow(FOLLOW_statement_in_synpred3_ObjectiveC97);
        stmt=statement();

        state._fsp--;
        if (state.failed) return ;

        }

    }
    // $ANTLR end synpred3_ObjectiveC

    // $ANTLR start synpred6_ObjectiveC
    public final void synpred6_ObjectiveC_fragment() throws RecognitionException {
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:97:5: ( protocol_declaration )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:97:5: protocol_declaration
        {
        pushFollow(FOLLOW_protocol_declaration_in_synpred6_ObjectiveC119);
        protocol_declaration();

        state._fsp--;
        if (state.failed) return ;

        }

    }
    // $ANTLR end synpred6_ObjectiveC

    // $ANTLR start synpred10_ObjectiveC
    public final void synpred10_ObjectiveC_fragment() throws RecognitionException {
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:124:3: ( declaration )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:124:3: declaration
        {
        pushFollow(FOLLOW_declaration_in_synpred10_ObjectiveC214);
        declaration();

        state._fsp--;
        if (state.failed) return ;

        }

    }
    // $ANTLR end synpred10_ObjectiveC

    // $ANTLR start synpred11_ObjectiveC
    public final void synpred11_ObjectiveC_fragment() throws RecognitionException {
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:124:17: ( class_method_declaration )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:124:17: class_method_declaration
        {
        pushFollow(FOLLOW_class_method_declaration_in_synpred11_ObjectiveC218);
        class_method_declaration();

        state._fsp--;
        if (state.failed) return ;

        }

    }
    // $ANTLR end synpred11_ObjectiveC

    // $ANTLR start synpred12_ObjectiveC
    public final void synpred12_ObjectiveC_fragment() throws RecognitionException {
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:124:44: ( instance_method_declaration )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:124:44: instance_method_declaration
        {
        pushFollow(FOLLOW_instance_method_declaration_in_synpred12_ObjectiveC222);
        instance_method_declaration();

        state._fsp--;
        if (state.failed) return ;

        }

    }
    // $ANTLR end synpred12_ObjectiveC

    // $ANTLR start synpred20_ObjectiveC
    public final void synpred20_ObjectiveC_fragment() throws RecognitionException {
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:156:21: ( declarator )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:156:21: declarator
        {
        pushFollow(FOLLOW_declarator_in_synpred20_ObjectiveC360);
        declarator();

        state._fsp--;
        if (state.failed) return ;

        }

    }
    // $ANTLR end synpred20_ObjectiveC

    // $ANTLR start synpred24_ObjectiveC
    public final void synpred24_ObjectiveC_fragment() throws RecognitionException {
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:168:3: ( function_definition )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:168:3: function_definition
        {
        pushFollow(FOLLOW_function_definition_in_synpred24_ObjectiveC419);
        function_definition();

        state._fsp--;
        if (state.failed) return ;

        }

    }
    // $ANTLR end synpred24_ObjectiveC

    // $ANTLR start synpred25_ObjectiveC
    public final void synpred25_ObjectiveC_fragment() throws RecognitionException {
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:168:25: ( declaration )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:168:25: declaration
        {
        pushFollow(FOLLOW_declaration_in_synpred25_ObjectiveC423);
        declaration();

        state._fsp--;
        if (state.failed) return ;

        }

    }
    // $ANTLR end synpred25_ObjectiveC

    // $ANTLR start synpred36_ObjectiveC
    public final void synpred36_ObjectiveC_fragment() throws RecognitionException {
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:194:29: ( ( declarator )? )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:194:29: ( declarator )?
        {
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:194:29: ( declarator )?
        int alt85=2;
        int LA85_0 = input.LA(1);

        if ( (LA85_0==IDENTIFIER) ) {
            alt85=1;
        }
        switch (alt85) {
            case 1 :
                // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:194:29: declarator
                {
                pushFollow(FOLLOW_declarator_in_synpred36_ObjectiveC557);
                declarator();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }


        }

    }
    // $ANTLR end synpred36_ObjectiveC

    // $ANTLR start synpred52_ObjectiveC
    public final void synpred52_ObjectiveC_fragment() throws RecognitionException {
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:341:4: ( protocol_interface_declaration )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:341:4: protocol_interface_declaration
        {
        pushFollow(FOLLOW_protocol_interface_declaration_in_synpred52_ObjectiveC987);
        protocol_interface_declaration();

        state._fsp--;
        if (state.failed) return ;

        }

    }
    // $ANTLR end synpred52_ObjectiveC

    // $ANTLR start synpred88_ObjectiveC
    public final void synpred88_ObjectiveC_fragment() throws RecognitionException {
        Statement stmtElse =null;


        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:694:6: ( 'else' stmtElse= statement )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:694:6: 'else' stmtElse= statement
        {
        match(input,76,FOLLOW_76_in_synpred88_ObjectiveC2071); if (state.failed) return ;

        pushFollow(FOLLOW_statement_in_synpred88_ObjectiveC2080);
        stmtElse=statement();

        state._fsp--;
        if (state.failed) return ;

        }

    }
    // $ANTLR end synpred88_ObjectiveC

    // $ANTLR start synpred123_ObjectiveC
    public final void synpred123_ObjectiveC_fragment() throws RecognitionException {
        BinaryExpression expx =null;


        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1099:3: ( ( '<<' | '>>' ) expx= shift_expression )
        // C:\\Users\\line47\\workspace\\ObjCFYPA\\src\\com\\icona\\antlr\\main\\ObjectiveC.g:1099:3: ( '<<' | '>>' ) expx= shift_expression
        {
        if ( input.LA(1)==44||input.LA(1)==51 ) {
            input.consume();
            state.errorRecovery=false;
            state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }


        pushFollow(FOLLOW_shift_expression_in_synpred123_ObjectiveC3338);
        expx=shift_expression();

        state._fsp--;
        if (state.failed) return ;

        }

    }
    // $ANTLR end synpred123_ObjectiveC

    // Delegated rules

    public final boolean synpred25_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred25_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred36_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred36_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred24_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred24_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred88_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred88_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred6_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred10_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred10_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred11_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred11_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred123_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred123_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred20_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred20_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred12_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred12_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred52_ObjectiveC() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred52_ObjectiveC_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


 

    public static final BitSet FOLLOW_external_declaration_in_translation_unit66 = new BitSet(new long[]{0x218004192A129530L,0x000000043FFFEFE1L});
    public static final BitSet FOLLOW_EOF_in_translation_unit74 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_external_declaration89 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statement_in_external_declaration97 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_class_interface_in_external_declaration106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_class_implementation_in_external_declaration112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_protocol_declaration_in_external_declaration119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_protocol_declaration_list_in_external_declaration128 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_class_name141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_superclass_name149 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_56_in_class_interface167 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_class_name_in_class_interface173 = new BitSet(new long[]{0x0040020880000000L,0x000000002F7E69A1L});
    public static final BitSet FOLLOW_41_in_class_interface176 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_superclass_name_in_class_interface178 = new BitSet(new long[]{0x0040000880000000L,0x000000002F7E69A1L});
    public static final BitSet FOLLOW_instance_variables_in_class_interface187 = new BitSet(new long[]{0x0040000880000000L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_interface_declaration_list_in_class_interface195 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_54_in_class_interface204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_interface_declaration_list214 = new BitSet(new long[]{0x0000000880000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_class_method_declaration_in_interface_declaration_list218 = new BitSet(new long[]{0x0000000880000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_instance_method_declaration_in_interface_declaration_list222 = new BitSet(new long[]{0x0000000880000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_31_in_class_method_declaration237 = new BitSet(new long[]{0x0000020008001000L});
    public static final BitSet FOLLOW_method_declaration_in_class_method_declaration239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_instance_method_declaration251 = new BitSet(new long[]{0x0000020008001000L});
    public static final BitSet FOLLOW_method_declaration_in_instance_method_declaration253 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_method_type_in_method_declaration266 = new BitSet(new long[]{0x0000020000001000L});
    public static final BitSet FOLLOW_method_selector_in_method_declaration271 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_method_declaration273 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_93_in_instance_variables284 = new BitSet(new long[]{0x5C00020000001000L});
    public static final BitSet FOLLOW_instance_variable_declaration_in_instance_variables286 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_97_in_instance_variables288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_visibility_specification_in_instance_variable_declaration300 = new BitSet(new long[]{0x5C00020000001002L});
    public static final BitSet FOLLOW_struct_declarator_list_in_instance_variable_declaration304 = new BitSet(new long[]{0x0000000000000000L,0x0000000020000000L});
    public static final BitSet FOLLOW_instance_variables_in_instance_variable_declaration306 = new BitSet(new long[]{0x5C00020000001002L});
    public static final BitSet FOLLOW_struct_declarator_in_struct_declarator_list344 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_34_in_struct_declarator_list347 = new BitSet(new long[]{0x0000020000001000L});
    public static final BitSet FOLLOW_struct_declarator_in_struct_declarator_list349 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_declarator_in_struct_declarator360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declarator_in_struct_declarator364 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_41_in_struct_declarator367 = new BitSet(new long[]{0x0000000000008530L});
    public static final BitSet FOLLOW_constant_in_struct_declarator369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_55_in_class_implementation378 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_class_name_in_class_implementation384 = new BitSet(new long[]{0x0040020880000000L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_41_in_class_implementation388 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_superclass_name_in_class_implementation390 = new BitSet(new long[]{0x0040000880000000L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_implementation_definition_list_in_class_implementation398 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_54_in_class_implementation407 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_definition_in_implementation_definition_list419 = new BitSet(new long[]{0x0000000880000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_declaration_in_implementation_definition_list423 = new BitSet(new long[]{0x0000000880000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_class_method_definition_in_implementation_definition_list427 = new BitSet(new long[]{0x0000000880000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_instance_method_definition_in_implementation_definition_list431 = new BitSet(new long[]{0x0000000880000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_31_in_class_method_definition442 = new BitSet(new long[]{0x0000020008001000L});
    public static final BitSet FOLLOW_method_definition_in_class_method_definition444 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_instance_method_definition456 = new BitSet(new long[]{0x0000020008001000L});
    public static final BitSet FOLLOW_method_definition_in_instance_method_definition458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_method_type_in_method_definition471 = new BitSet(new long[]{0x0000020000001000L});
    public static final BitSet FOLLOW_method_selector_in_method_definition475 = new BitSet(new long[]{0x0000000000000000L,0x0000000020000000L});
    public static final BitSet FOLLOW_compound_statement_in_method_definition479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_method_selector489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyword_declarator_in_method_selector493 = new BitSet(new long[]{0x0000020000001002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_parameter_list_in_method_selector497 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_keyword_declarator511 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_41_in_keyword_declarator514 = new BitSet(new long[]{0x0000000008001000L});
    public static final BitSet FOLLOW_method_type_in_keyword_declarator516 = new BitSet(new long[]{0x0000000008001000L});
    public static final BitSet FOLLOW_IDENTIFIER_in_keyword_declarator519 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameter_declaration_list_in_parameter_list531 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_34_in_parameter_list535 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_38_in_parameter_list537 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_specifiers_in_parameter_declaration554 = new BitSet(new long[]{0x0000000000001002L,0x0000000000000002L});
    public static final BitSet FOLLOW_declarator_in_parameter_declaration557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_abstract_declarator_in_parameter_declaration562 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parameter_declaration_in_parameter_declaration_list577 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_34_in_parameter_declaration_list581 = new BitSet(new long[]{0x0000000000000000L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_parameter_declaration_in_parameter_declaration_list583 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_65_in_abstract_declarator639 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000004L});
    public static final BitSet FOLLOW_constant_expression_in_abstract_declarator653 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_abstract_declarator667 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_abstract_declarator_suffix689 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000004L});
    public static final BitSet FOLLOW_constant_expression_in_abstract_declarator_suffix691 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_abstract_declarator_suffix694 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_abstract_declarator_suffix700 = new BitSet(new long[]{0x0000000010000000L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_parameter_declaration_list_in_abstract_declarator_suffix703 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_abstract_declarator_suffix706 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_selector716 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_method_type723 = new BitSet(new long[]{0x0000000000000000L,0x000000000F364981L});
    public static final BitSet FOLLOW_type_name_in_method_type725 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_method_type727 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_specifier_qualifier_list_in_type_name750 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_abstract_declarator_in_type_name760 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_specifier_in_specifier_qualifier_list794 = new BitSet(new long[]{0x0000000000000002L,0x000000000F364981L});
    public static final BitSet FOLLOW_type_qualifier_in_specifier_qualifier_list811 = new BitSet(new long[]{0x0000000000000002L,0x000000000F364981L});
    public static final BitSet FOLLOW_declaration_specifiers_in_function_definition853 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_declarator_in_function_definition857 = new BitSet(new long[]{0x0000000000000000L,0x0000000020000000L});
    public static final BitSet FOLLOW_compound_statement_in_function_definition865 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interface_declaration_list_in_protocol_interface_declaration883 = new BitSet(new long[]{0x8200000880000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_qualified_protocol_interface_declaration_in_protocol_interface_declaration886 = new BitSet(new long[]{0x8200000000000002L});
    public static final BitSet FOLLOW_57_in_qualified_protocol_interface_declaration901 = new BitSet(new long[]{0x0000000880000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_interface_declaration_list_in_qualified_protocol_interface_declaration903 = new BitSet(new long[]{0x0000000880000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_63_in_qualified_protocol_interface_declaration912 = new BitSet(new long[]{0x0000000880000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_interface_declaration_list_in_qualified_protocol_interface_declaration914 = new BitSet(new long[]{0x0000000880000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_61_in_protocol_declaration952 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_protocol_name_in_protocol_declaration962 = new BitSet(new long[]{0x8240080880000000L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_protocol_reference_list_in_protocol_declaration975 = new BitSet(new long[]{0x8240000880000000L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_protocol_interface_declaration_in_protocol_declaration987 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_54_in_protocol_declaration996 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_61_in_protocol_declaration_list1011 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_protocol_list_in_protocol_declaration_list1013 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_protocol_declaration_list1014 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_43_in_protocol_reference_list1044 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_protocol_list_in_protocol_reference_list1052 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_protocol_reference_list1055 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_protocol_name_in_protocol_list1075 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_34_in_protocol_list1078 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_protocol_name_in_protocol_list1080 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_protocol_name1104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_specifiers_in_declaration1138 = new BitSet(new long[]{0x0000040000001000L});
    public static final BitSet FOLLOW_init_declarator_list_in_declaration1150 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_declaration1161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_storage_class_specifier_in_declaration_specifiers1190 = new BitSet(new long[]{0x0000000000000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_type_specifier_in_declaration_specifiers1206 = new BitSet(new long[]{0x0000000000000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_type_qualifier_in_declaration_specifiers1221 = new BitSet(new long[]{0x0000000000000002L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_init_declarator_in_init_declarator_list1257 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_34_in_init_declarator_list1268 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_init_declarator_in_init_declarator_list1272 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_declarator_in_init_declarator1315 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_47_in_init_declarator1323 = new BitSet(new long[]{0x000000192A129530L,0x0000000420000000L});
    public static final BitSet FOLLOW_initializer_in_init_declarator1331 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignment_expression_in_initializer1367 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_93_in_initializer1374 = new BitSet(new long[]{0x000000192A129530L,0x0000000420000000L});
    public static final BitSet FOLLOW_initializer_in_initializer1378 = new BitSet(new long[]{0x0000000400000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_34_in_initializer1387 = new BitSet(new long[]{0x000000192A129530L,0x0000000420000000L});
    public static final BitSet FOLLOW_initializer_in_initializer1391 = new BitSet(new long[]{0x0000000400000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_97_in_initializer1405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_direct_declarator_in_declarator1434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_direct_declarator1467 = new BitSet(new long[]{0x0000000008000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_declarator_suffix_in_direct_declarator1486 = new BitSet(new long[]{0x0000000008000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_declarator_suffix1531 = new BitSet(new long[]{0x0000000000008530L,0x0000000000000004L});
    public static final BitSet FOLLOW_constant_in_declarator_suffix1543 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_declarator_suffix1557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_declarator_suffix1572 = new BitSet(new long[]{0x0000000010000000L,0x000000000F7E69A1L});
    public static final BitSet FOLLOW_parameter_list_in_declarator_suffix1574 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_declarator_suffix1577 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_90_in_type_specifier1608 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_71_in_type_specifier1615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_84_in_type_specifier1623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_82_in_type_specifier1629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_81_in_type_specifier1637 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_78_in_type_specifier1644 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_75_in_type_specifier1651 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_64_in_type_specifier1658 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_85_in_type_specifier1665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_89_in_type_specifier1672 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_88_in_type_specifier1679 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_72_in_type_qualifier1703 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_91_in_type_qualifier1711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_69_in_storage_class_specifier1735 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_83_in_storage_class_specifier1743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_86_in_storage_class_specifier1751 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_77_in_storage_class_specifier1759 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selection_statement_in_statement1791 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_statement1808 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_statement1810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_compound_statement_in_statement1821 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iteration_statement_in_statement1832 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_labeled_statement_in_statement1845 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_statement1857 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_statement1867 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifier_in_labeled_statement1896 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_41_in_labeled_statement1905 = new BitSet(new long[]{0x000004192A129530L,0x000000043FFFEFE1L});
    public static final BitSet FOLLOW_statement_in_labeled_statement1924 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_70_in_labeled_statement1938 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_constant_expression_in_labeled_statement1951 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_41_in_labeled_statement1961 = new BitSet(new long[]{0x000004192A129530L,0x000000043FFFEFE1L});
    public static final BitSet FOLLOW_statement_in_labeled_statement1972 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_73_in_labeled_statement1986 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_41_in_labeled_statement1994 = new BitSet(new long[]{0x000004192A129530L,0x000000043FFFEFE1L});
    public static final BitSet FOLLOW_statement_in_labeled_statement2005 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_80_in_selection_statement2036 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_selection_statement2038 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_expression_in_selection_statement2046 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_selection_statement2055 = new BitSet(new long[]{0x000004192A129530L,0x000000043FFFEFE1L});
    public static final BitSet FOLLOW_statement_in_selection_statement2063 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_76_in_selection_statement2071 = new BitSet(new long[]{0x000004192A129530L,0x000000043FFFEFE1L});
    public static final BitSet FOLLOW_statement_in_selection_statement2080 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_87_in_selection_statement2099 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_selection_statement2108 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_expression_in_selection_statement2126 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_selection_statement2145 = new BitSet(new long[]{0x000004192A129530L,0x000000043FFFEFE1L});
    public static final BitSet FOLLOW_statement_in_selection_statement2156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_92_in_iteration_statement2189 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_iteration_statement2200 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_expression_in_iteration_statement2215 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_iteration_statement2223 = new BitSet(new long[]{0x000004192A129530L,0x000000043FFFEFE1L});
    public static final BitSet FOLLOW_statement_in_iteration_statement2236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_74_in_iteration_statement2247 = new BitSet(new long[]{0x000004192A129530L,0x000000043FFFEFE1L});
    public static final BitSet FOLLOW_statement_in_iteration_statement2265 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
    public static final BitSet FOLLOW_92_in_iteration_statement2278 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_iteration_statement2291 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_expression_in_iteration_statement2307 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_iteration_statement2321 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_iteration_statement2334 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_79_in_iteration_statement2353 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_iteration_statement2365 = new BitSet(new long[]{0x000004192A129530L,0x000000040F7E69A1L});
    public static final BitSet FOLLOW_expression_in_iteration_statement2388 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_declaration_in_iteration_statement2396 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_iteration_statement2413 = new BitSet(new long[]{0x000004192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_expression_in_iteration_statement2430 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_iteration_statement2446 = new BitSet(new long[]{0x000000193A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_expression_in_iteration_statement2463 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_iteration_statement2478 = new BitSet(new long[]{0x000004192A129530L,0x000000043FFFEFE1L});
    public static final BitSet FOLLOW_statement_in_iteration_statement2493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_93_in_compound_statement2518 = new BitSet(new long[]{0x000004192A129530L,0x000000063FFFEFE1L});
    public static final BitSet FOLLOW_statement_list_in_compound_statement2532 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_97_in_compound_statement2547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statement_in_statement_list2570 = new BitSet(new long[]{0x000004192A129532L,0x000000043FFFEFE1L});
    public static final BitSet FOLLOW_assignment_expression_in_expression2604 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_34_in_expression2615 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_assignment_expression_in_expression2619 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_conditional_expression_in_assignment_expression2649 = new BitSet(new long[]{0x0010A12244800002L,0x0000000080000010L});
    public static final BitSet FOLLOW_assignment_operator_in_assignment_expression2663 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_assignment_expression_in_assignment_expression2674 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_47_in_assignment_operator2704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_30_in_assignment_operator2711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_assignment_operator2718 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_assignment_operator2725 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_33_in_assignment_operator2732 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_37_in_assignment_operator2739 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_45_in_assignment_operator2746 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_52_in_assignment_operator2753 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_assignment_operator2760 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_68_in_assignment_operator2767 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_95_in_assignment_operator2774 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logical_or_expression_in_conditional_expression2801 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_53_in_conditional_expression2812 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_logical_or_expression_in_conditional_expression2816 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_41_in_conditional_expression2825 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_logical_or_expression_in_conditional_expression2829 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditional_expression_in_constant_expression2856 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logical_and_expression_in_logical_or_expression2879 = new BitSet(new long[]{0x0000000000000002L,0x0000000100000000L});
    public static final BitSet FOLLOW_96_in_logical_or_expression2890 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_logical_and_expression_in_logical_or_expression2902 = new BitSet(new long[]{0x0000000000000002L,0x0000000100000000L});
    public static final BitSet FOLLOW_inclusive_or_expression_in_logical_and_expression2930 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_24_in_logical_and_expression2939 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_inclusive_or_expression_in_logical_and_expression2950 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_exclusive_or_expression_in_inclusive_or_expression2980 = new BitSet(new long[]{0x0000000000000002L,0x0000000040000000L});
    public static final BitSet FOLLOW_94_in_inclusive_or_expression2991 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_exclusive_or_expression_in_inclusive_or_expression3003 = new BitSet(new long[]{0x0000000000000002L,0x0000000040000000L});
    public static final BitSet FOLLOW_and_expression_in_exclusive_or_expression3033 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_exclusive_or_expression3044 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_and_expression_in_exclusive_or_expression3056 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_equality_expression_in_and_expression3086 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_25_in_and_expression3097 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_equality_expression_in_and_expression3108 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_relational_expression_in_equality_expression3138 = new BitSet(new long[]{0x0001000000200002L});
    public static final BitSet FOLLOW_21_in_equality_expression3155 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_48_in_equality_expression3165 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_relational_expression_in_equality_expression3182 = new BitSet(new long[]{0x0001000000200002L});
    public static final BitSet FOLLOW_shift_expression_in_relational_expression3209 = new BitSet(new long[]{0x0006480000000002L});
    public static final BitSet FOLLOW_43_in_relational_expression3224 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_49_in_relational_expression3234 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_46_in_relational_expression3244 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_50_in_relational_expression3254 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_shift_expression_in_relational_expression3271 = new BitSet(new long[]{0x0006480000000002L});
    public static final BitSet FOLLOW_additive_expression_in_shift_expression3297 = new BitSet(new long[]{0x0008100000000002L});
    public static final BitSet FOLLOW_44_in_shift_expression3312 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_51_in_shift_expression3322 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_shift_expression_in_shift_expression3338 = new BitSet(new long[]{0x0008100000000002L});
    public static final BitSet FOLLOW_multiplicative_expression_in_additive_expression3365 = new BitSet(new long[]{0x0000000880000002L});
    public static final BitSet FOLLOW_31_in_additive_expression3380 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_35_in_additive_expression3389 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_multiplicative_expression_in_additive_expression3405 = new BitSet(new long[]{0x0000000880000002L});
    public static final BitSet FOLLOW_cast_expression_in_multiplicative_expression3432 = new BitSet(new long[]{0x0000008020400002L});
    public static final BitSet FOLLOW_29_in_multiplicative_expression3446 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_39_in_multiplicative_expression3456 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_22_in_multiplicative_expression3466 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_cast_expression_in_multiplicative_expression3482 = new BitSet(new long[]{0x0000008020400002L});
    public static final BitSet FOLLOW_unary_expression_in_cast_expression3507 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_postfix_expression_in_unary_expression3534 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_unary_expression3541 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_unary_expression_in_unary_expression3545 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_unary_expression3553 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_unary_expression_in_unary_expression3557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unary_operator_in_unary_expression3567 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_cast_expression_in_unary_expression3572 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_unary_operator3594 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_unary_operator3602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_unary_operator3609 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_98_in_unary_operator3616 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_20_in_unary_operator3623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_expression_in_postfix_expression3649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_primary_expression3679 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constant_in_primary_expression3688 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_primary_expression3697 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_primary_expression3705 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_assignment_expression_in_primary_expression3709 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_primary_expression3711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_identifier3737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_synpred2_ObjectiveC89 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statement_in_synpred3_ObjectiveC97 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_protocol_declaration_in_synpred6_ObjectiveC119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_synpred10_ObjectiveC214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_class_method_declaration_in_synpred11_ObjectiveC218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_instance_method_declaration_in_synpred12_ObjectiveC222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declarator_in_synpred20_ObjectiveC360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_definition_in_synpred24_ObjectiveC419 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_synpred25_ObjectiveC423 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declarator_in_synpred36_ObjectiveC557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_protocol_interface_declaration_in_synpred52_ObjectiveC987 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_76_in_synpred88_ObjectiveC2071 = new BitSet(new long[]{0x000004192A129530L,0x000000043FFFEFE1L});
    public static final BitSet FOLLOW_statement_in_synpred88_ObjectiveC2080 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred123_ObjectiveC3307 = new BitSet(new long[]{0x000000192A129530L,0x0000000400000000L});
    public static final BitSet FOLLOW_shift_expression_in_synpred123_ObjectiveC3338 = new BitSet(new long[]{0x0000000000000002L});

}

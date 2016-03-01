// $ANTLR 3.4 C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g 2012-09-25 16:56:53
 package com.kevinchard.ipa.lang; 

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class IpaLexer extends Lexer {
    public static final int EOF=-1;
    public static final int T__11=11;
    public static final int T__12=12;
    public static final int T__13=13;
    public static final int T__14=14;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__19=19;
    public static final int T__20=20;
    public static final int T__21=21;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int T__24=24;
    public static final int T__25=25;
    public static final int DIGIT=4;
    public static final int ID=5;
    public static final int LETTER=6;
    public static final int NUMBER=7;
    public static final int STRING=8;
    public static final int TYPE=9;
    public static final int WHITESPACE=10;

    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public IpaLexer() {} 
    public IpaLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public IpaLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g"; }

    // $ANTLR start "T__11"
    public final void mT__11() throws RecognitionException {
        try {
            int _type = T__11;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:4:7: ( '(' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:4:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__11"

    // $ANTLR start "T__12"
    public final void mT__12() throws RecognitionException {
        try {
            int _type = T__12;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:5:7: ( ')' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:5:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__12"

    // $ANTLR start "T__13"
    public final void mT__13() throws RecognitionException {
        try {
            int _type = T__13;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:6:7: ( ',' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:6:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__13"

    // $ANTLR start "T__14"
    public final void mT__14() throws RecognitionException {
        try {
            int _type = T__14;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:7:7: ( '->' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:7:9: '->'
            {
            match("->"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__14"

    // $ANTLR start "T__15"
    public final void mT__15() throws RecognitionException {
        try {
            int _type = T__15;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:8:7: ( ':' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:8:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__15"

    // $ANTLR start "T__16"
    public final void mT__16() throws RecognitionException {
        try {
            int _type = T__16;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:9:7: ( ':=' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:9:9: ':='
            {
            match(":="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__16"

    // $ANTLR start "T__17"
    public final void mT__17() throws RecognitionException {
        try {
            int _type = T__17;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:10:7: ( 'L' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:10:9: 'L'
            {
            match('L'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__17"

    // $ANTLR start "T__18"
    public final void mT__18() throws RecognitionException {
        try {
            int _type = T__18;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:11:7: ( 'M' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:11:9: 'M'
            {
            match('M'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__18"

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:12:7: ( 'S' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:12:9: 'S'
            {
            match('S'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:13:7: ( '[' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:13:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:14:7: ( ']' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:14:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:15:7: ( 'return' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:15:9: 'return'
            {
            match("return"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:16:7: ( '{' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:16:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:17:7: ( '|' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:17:9: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:18:7: ( '}' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:18:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "TYPE"
    public final void mTYPE() throws RecognitionException {
        try {
            int _type = TYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:72:6: ( 'Num' | 'Str' | 'List' | 'Set' | 'Map' | 'Sym' | 'Func' | 'Any' )
            int alt1=8;
            switch ( input.LA(1) ) {
            case 'N':
                {
                alt1=1;
                }
                break;
            case 'S':
                {
                switch ( input.LA(2) ) {
                case 't':
                    {
                    alt1=2;
                    }
                    break;
                case 'e':
                    {
                    alt1=4;
                    }
                    break;
                case 'y':
                    {
                    alt1=6;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 1, 2, input);

                    throw nvae;

                }

                }
                break;
            case 'L':
                {
                alt1=3;
                }
                break;
            case 'M':
                {
                alt1=5;
                }
                break;
            case 'F':
                {
                alt1=7;
                }
                break;
            case 'A':
                {
                alt1=8;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;

            }

            switch (alt1) {
                case 1 :
                    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:72:8: 'Num'
                    {
                    match("Num"); 



                    }
                    break;
                case 2 :
                    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:72:16: 'Str'
                    {
                    match("Str"); 



                    }
                    break;
                case 3 :
                    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:72:24: 'List'
                    {
                    match("List"); 



                    }
                    break;
                case 4 :
                    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:72:33: 'Set'
                    {
                    match("Set"); 



                    }
                    break;
                case 5 :
                    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:72:41: 'Map'
                    {
                    match("Map"); 



                    }
                    break;
                case 6 :
                    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:72:49: 'Sym'
                    {
                    match("Sym"); 



                    }
                    break;
                case 7 :
                    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:72:57: 'Func'
                    {
                    match("Func"); 



                    }
                    break;
                case 8 :
                    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:72:66: 'Any'
                    {
                    match("Any"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "TYPE"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:74:4: ( ( LETTER ( LETTER | DIGIT )* '.' )? LETTER ( LETTER | DIGIT )* )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:74:8: ( LETTER ( LETTER | DIGIT )* '.' )? LETTER ( LETTER | DIGIT )*
            {
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:74:8: ( LETTER ( LETTER | DIGIT )* '.' )?
            int alt3=2;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:74:9: LETTER ( LETTER | DIGIT )* '.'
                    {
                    mLETTER(); 


                    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:74:16: ( LETTER | DIGIT )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( ((LA2_0 >= '0' && LA2_0 <= '9')||(LA2_0 >= 'A' && LA2_0 <= 'Z')||(LA2_0 >= 'a' && LA2_0 <= 'z')) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:
                    	    {
                    	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop2;
                        }
                    } while (true);


                    match('.'); 

                    }
                    break;

            }


            mLETTER(); 


            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:74:48: ( LETTER | DIGIT )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0 >= '0' && LA4_0 <= '9')||(LA4_0 >= 'A' && LA4_0 <= 'Z')||(LA4_0 >= 'a' && LA4_0 <= 'z')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ID"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:76:8: ( '\\\"' ( . )* '\\\"' )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:76:10: '\\\"' ( . )* '\\\"'
            {
            match('\"'); 

            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:76:15: ( . )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\"') ) {
                    alt5=2;
                }
                else if ( ((LA5_0 >= '\u0000' && LA5_0 <= '!')||(LA5_0 >= '#' && LA5_0 <= '\uFFFF')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:76:15: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:78:8: ( ( DIGIT )+ )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:78:10: ( DIGIT )+
            {
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:78:10: ( DIGIT )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0 >= '0' && LA6_0 <= '9')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NUMBER"

    // $ANTLR start "LETTER"
    public final void mLETTER() throws RecognitionException {
        try {
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:80:17: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LETTER"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:82:16: ( ( '0' .. '9' ) )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DIGIT"

    // $ANTLR start "WHITESPACE"
    public final void mWHITESPACE() throws RecognitionException {
        try {
            int _type = WHITESPACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:84:12: ( ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' | ';' )+ )
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:84:14: ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' | ';' )+
            {
            // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:84:14: ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' | ';' )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( ((LA7_0 >= '\t' && LA7_0 <= '\n')||(LA7_0 >= '\f' && LA7_0 <= '\r')||LA7_0==' '||LA7_0==';') ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:
            	    {
            	    if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||(input.LA(1) >= '\f' && input.LA(1) <= '\r')||input.LA(1)==' '||input.LA(1)==';' ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);


             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WHITESPACE"

    public void mTokens() throws RecognitionException {
        // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:8: ( T__11 | T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | TYPE | ID | STRING | NUMBER | WHITESPACE )
        int alt8=20;
        switch ( input.LA(1) ) {
        case '(':
            {
            alt8=1;
            }
            break;
        case ')':
            {
            alt8=2;
            }
            break;
        case ',':
            {
            alt8=3;
            }
            break;
        case '-':
            {
            alt8=4;
            }
            break;
        case ':':
            {
            int LA8_5 = input.LA(2);

            if ( (LA8_5=='=') ) {
                alt8=6;
            }
            else {
                alt8=5;
            }
            }
            break;
        case 'L':
            {
            switch ( input.LA(2) ) {
            case 'i':
                {
                int LA8_24 = input.LA(3);

                if ( (LA8_24=='s') ) {
                    int LA8_36 = input.LA(4);

                    if ( (LA8_36=='t') ) {
                        int LA8_45 = input.LA(5);

                        if ( (LA8_45=='.'||(LA8_45 >= '0' && LA8_45 <= '9')||(LA8_45 >= 'A' && LA8_45 <= 'Z')||(LA8_45 >= 'a' && LA8_45 <= 'z')) ) {
                            alt8=17;
                        }
                        else {
                            alt8=16;
                        }
                    }
                    else {
                        alt8=17;
                    }
                }
                else {
                    alt8=17;
                }
                }
                break;
            case '.':
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
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                {
                alt8=17;
                }
                break;
            default:
                alt8=7;
            }

            }
            break;
        case 'M':
            {
            switch ( input.LA(2) ) {
            case 'a':
                {
                int LA8_26 = input.LA(3);

                if ( (LA8_26=='p') ) {
                    int LA8_37 = input.LA(4);

                    if ( (LA8_37=='.'||(LA8_37 >= '0' && LA8_37 <= '9')||(LA8_37 >= 'A' && LA8_37 <= 'Z')||(LA8_37 >= 'a' && LA8_37 <= 'z')) ) {
                        alt8=17;
                    }
                    else {
                        alt8=16;
                    }
                }
                else {
                    alt8=17;
                }
                }
                break;
            case '.':
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
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                {
                alt8=17;
                }
                break;
            default:
                alt8=8;
            }

            }
            break;
        case 'S':
            {
            switch ( input.LA(2) ) {
            case 't':
                {
                int LA8_28 = input.LA(3);

                if ( (LA8_28=='r') ) {
                    int LA8_38 = input.LA(4);

                    if ( (LA8_38=='.'||(LA8_38 >= '0' && LA8_38 <= '9')||(LA8_38 >= 'A' && LA8_38 <= 'Z')||(LA8_38 >= 'a' && LA8_38 <= 'z')) ) {
                        alt8=17;
                    }
                    else {
                        alt8=16;
                    }
                }
                else {
                    alt8=17;
                }
                }
                break;
            case 'e':
                {
                int LA8_29 = input.LA(3);

                if ( (LA8_29=='t') ) {
                    int LA8_39 = input.LA(4);

                    if ( (LA8_39=='.'||(LA8_39 >= '0' && LA8_39 <= '9')||(LA8_39 >= 'A' && LA8_39 <= 'Z')||(LA8_39 >= 'a' && LA8_39 <= 'z')) ) {
                        alt8=17;
                    }
                    else {
                        alt8=16;
                    }
                }
                else {
                    alt8=17;
                }
                }
                break;
            case 'y':
                {
                int LA8_30 = input.LA(3);

                if ( (LA8_30=='m') ) {
                    int LA8_40 = input.LA(4);

                    if ( (LA8_40=='.'||(LA8_40 >= '0' && LA8_40 <= '9')||(LA8_40 >= 'A' && LA8_40 <= 'Z')||(LA8_40 >= 'a' && LA8_40 <= 'z')) ) {
                        alt8=17;
                    }
                    else {
                        alt8=16;
                    }
                }
                else {
                    alt8=17;
                }
                }
                break;
            case '.':
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
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'z':
                {
                alt8=17;
                }
                break;
            default:
                alt8=9;
            }

            }
            break;
        case '[':
            {
            alt8=10;
            }
            break;
        case ']':
            {
            alt8=11;
            }
            break;
        case 'r':
            {
            int LA8_11 = input.LA(2);

            if ( (LA8_11=='e') ) {
                int LA8_32 = input.LA(3);

                if ( (LA8_32=='t') ) {
                    int LA8_41 = input.LA(4);

                    if ( (LA8_41=='u') ) {
                        int LA8_47 = input.LA(5);

                        if ( (LA8_47=='r') ) {
                            int LA8_49 = input.LA(6);

                            if ( (LA8_49=='n') ) {
                                int LA8_50 = input.LA(7);

                                if ( (LA8_50=='.'||(LA8_50 >= '0' && LA8_50 <= '9')||(LA8_50 >= 'A' && LA8_50 <= 'Z')||(LA8_50 >= 'a' && LA8_50 <= 'z')) ) {
                                    alt8=17;
                                }
                                else {
                                    alt8=12;
                                }
                            }
                            else {
                                alt8=17;
                            }
                        }
                        else {
                            alt8=17;
                        }
                    }
                    else {
                        alt8=17;
                    }
                }
                else {
                    alt8=17;
                }
            }
            else {
                alt8=17;
            }
            }
            break;
        case '{':
            {
            alt8=13;
            }
            break;
        case '|':
            {
            alt8=14;
            }
            break;
        case '}':
            {
            alt8=15;
            }
            break;
        case 'N':
            {
            int LA8_15 = input.LA(2);

            if ( (LA8_15=='u') ) {
                int LA8_33 = input.LA(3);

                if ( (LA8_33=='m') ) {
                    int LA8_42 = input.LA(4);

                    if ( (LA8_42=='.'||(LA8_42 >= '0' && LA8_42 <= '9')||(LA8_42 >= 'A' && LA8_42 <= 'Z')||(LA8_42 >= 'a' && LA8_42 <= 'z')) ) {
                        alt8=17;
                    }
                    else {
                        alt8=16;
                    }
                }
                else {
                    alt8=17;
                }
            }
            else {
                alt8=17;
            }
            }
            break;
        case 'F':
            {
            int LA8_16 = input.LA(2);

            if ( (LA8_16=='u') ) {
                int LA8_34 = input.LA(3);

                if ( (LA8_34=='n') ) {
                    int LA8_43 = input.LA(4);

                    if ( (LA8_43=='c') ) {
                        int LA8_48 = input.LA(5);

                        if ( (LA8_48=='.'||(LA8_48 >= '0' && LA8_48 <= '9')||(LA8_48 >= 'A' && LA8_48 <= 'Z')||(LA8_48 >= 'a' && LA8_48 <= 'z')) ) {
                            alt8=17;
                        }
                        else {
                            alt8=16;
                        }
                    }
                    else {
                        alt8=17;
                    }
                }
                else {
                    alt8=17;
                }
            }
            else {
                alt8=17;
            }
            }
            break;
        case 'A':
            {
            int LA8_17 = input.LA(2);

            if ( (LA8_17=='n') ) {
                int LA8_35 = input.LA(3);

                if ( (LA8_35=='y') ) {
                    int LA8_44 = input.LA(4);

                    if ( (LA8_44=='.'||(LA8_44 >= '0' && LA8_44 <= '9')||(LA8_44 >= 'A' && LA8_44 <= 'Z')||(LA8_44 >= 'a' && LA8_44 <= 'z')) ) {
                        alt8=17;
                    }
                    else {
                        alt8=16;
                    }
                }
                else {
                    alt8=17;
                }
            }
            else {
                alt8=17;
            }
            }
            break;
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'T':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        case 'Z':
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
            {
            alt8=17;
            }
            break;
        case '\"':
            {
            alt8=18;
            }
            break;
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
            {
            alt8=19;
            }
            break;
        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
        case ';':
            {
            alt8=20;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("", 8, 0, input);

            throw nvae;

        }

        switch (alt8) {
            case 1 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:10: T__11
                {
                mT__11(); 


                }
                break;
            case 2 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:16: T__12
                {
                mT__12(); 


                }
                break;
            case 3 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:22: T__13
                {
                mT__13(); 


                }
                break;
            case 4 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:28: T__14
                {
                mT__14(); 


                }
                break;
            case 5 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:34: T__15
                {
                mT__15(); 


                }
                break;
            case 6 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:40: T__16
                {
                mT__16(); 


                }
                break;
            case 7 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:46: T__17
                {
                mT__17(); 


                }
                break;
            case 8 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:52: T__18
                {
                mT__18(); 


                }
                break;
            case 9 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:58: T__19
                {
                mT__19(); 


                }
                break;
            case 10 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:64: T__20
                {
                mT__20(); 


                }
                break;
            case 11 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:70: T__21
                {
                mT__21(); 


                }
                break;
            case 12 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:76: T__22
                {
                mT__22(); 


                }
                break;
            case 13 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:82: T__23
                {
                mT__23(); 


                }
                break;
            case 14 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:88: T__24
                {
                mT__24(); 


                }
                break;
            case 15 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:94: T__25
                {
                mT__25(); 


                }
                break;
            case 16 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:100: TYPE
                {
                mTYPE(); 


                }
                break;
            case 17 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:105: ID
                {
                mID(); 


                }
                break;
            case 18 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:108: STRING
                {
                mSTRING(); 


                }
                break;
            case 19 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:115: NUMBER
                {
                mNUMBER(); 


                }
                break;
            case 20 :
                // C:\\dev\\workspaces\\lang\\ipa\\src\\main\\com\\kevinchard\\ipa\\lang\\Ipa.g:1:122: WHITESPACE
                {
                mWHITESPACE(); 


                }
                break;

        }

    }


    protected DFA3 dfa3 = new DFA3(this);
    static final String DFA3_eotS =
        "\1\uffff\1\2\1\uffff\1\2\1\uffff";
    static final String DFA3_eofS =
        "\5\uffff";
    static final String DFA3_minS =
        "\1\101\1\56\1\uffff\1\56\1\uffff";
    static final String DFA3_maxS =
        "\2\172\1\uffff\1\172\1\uffff";
    static final String DFA3_acceptS =
        "\2\uffff\1\2\1\uffff\1\1";
    static final String DFA3_specialS =
        "\5\uffff}>";
    static final String[] DFA3_transitionS = {
            "\32\1\6\uffff\32\1",
            "\1\4\1\uffff\12\3\7\uffff\32\3\6\uffff\32\3",
            "",
            "\1\4\1\uffff\12\3\7\uffff\32\3\6\uffff\32\3",
            ""
    };

    static final short[] DFA3_eot = DFA.unpackEncodedString(DFA3_eotS);
    static final short[] DFA3_eof = DFA.unpackEncodedString(DFA3_eofS);
    static final char[] DFA3_min = DFA.unpackEncodedStringToUnsignedChars(DFA3_minS);
    static final char[] DFA3_max = DFA.unpackEncodedStringToUnsignedChars(DFA3_maxS);
    static final short[] DFA3_accept = DFA.unpackEncodedString(DFA3_acceptS);
    static final short[] DFA3_special = DFA.unpackEncodedString(DFA3_specialS);
    static final short[][] DFA3_transition;

    static {
        int numStates = DFA3_transitionS.length;
        DFA3_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA3_transition[i] = DFA.unpackEncodedString(DFA3_transitionS[i]);
        }
    }

    class DFA3 extends DFA {

        public DFA3(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 3;
            this.eot = DFA3_eot;
            this.eof = DFA3_eof;
            this.min = DFA3_min;
            this.max = DFA3_max;
            this.accept = DFA3_accept;
            this.special = DFA3_special;
            this.transition = DFA3_transition;
        }
        public String getDescription() {
            return "74:8: ( LETTER ( LETTER | DIGIT )* '.' )?";
        }
    }
 

}

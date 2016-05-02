/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */
package com.caucho.quercus.parser;

import com.caucho.quercus.Location;
import com.caucho.quercus.QuercusContext;
import com.caucho.quercus.QuercusRuntimeException;
import com.caucho.quercus.env.*;
import com.caucho.quercus.expr.*;
import com.caucho.quercus.function.*;
import com.caucho.quercus.program.*;
import com.caucho.quercus.statement.*;
import com.caucho.util.CharBuffer;
import com.caucho.util.IntMap;
import com.caucho.util.L10N;
import com.caucho.vfs.*;

import java.io.CharConversionException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Parses a PHP program.
 */
public class QuercusParser {

   private final static L10N L = new L10N(QuercusParser.class);
   private final static int M_STATIC = 0x1;
   private final static int M_PUBLIC = 0x2;
   private final static int M_PROTECTED = 0x4;
   private final static int M_PRIVATE = 0x8;
   private final static int M_FINAL = 0x10;
   private final static int M_ABSTRACT = 0x20;
   private final static int M_INTERFACE = 0x40;
   private final static int IDENTIFIER = 256;
   private final static int STRING = 257;
   private final static int LONG = 258;
   private final static int DOUBLE = 259;
   private final static int LSHIFT = 260;
   private final static int RSHIFT = 261;
   private final static int PHP_END = 262;
   private final static int EQ = 263;
   private final static int DEREF = 264;
   private final static int LEQ = 268;
   private final static int GEQ = 269;
   private final static int NEQ = 270;
   private final static int EQUALS = 271;
   private final static int NEQUALS = 272;
   private final static int C_AND = 273;
   private final static int C_OR = 274;
   private final static int PLUS_ASSIGN = 278;
   private final static int MINUS_ASSIGN = 279;
   private final static int APPEND_ASSIGN = 280;
   private final static int MUL_ASSIGN = 281;
   private final static int DIV_ASSIGN = 282;
   private final static int MOD_ASSIGN = 283;
   private final static int AND_ASSIGN = 284;
   private final static int OR_ASSIGN = 285;
   private final static int XOR_ASSIGN = 286;
   private final static int LSHIFT_ASSIGN = 287;
   private final static int RSHIFT_ASSIGN = 288;
   private final static int INCR = 289;
   private final static int DECR = 290;
   private final static int SCOPE = 291;
   private final static int ESCAPED_STRING = 292;
   private final static int HEREDOC = 293;
   private final static int ARRAY_RIGHT = 294;
   private final static int SIMPLE_STRING_ESCAPE = 295;
   private final static int COMPLEX_STRING_ESCAPE = 296;
   private final static int BINARY = 297;
   private final static int SIMPLE_BINARY_ESCAPE = 298;
   private final static int COMPLEX_BINARY_ESCAPE = 299;
   private final static int FIRST_IDENTIFIER_LEXEME = 512;
   private final static int ECHO = 512;
   private final static int NULL = 513;
   private final static int IF = 514;
   private final static int WHILE = 515;
   private final static int FUNCTION = 516;
   private final static int CLASS = 517;
   private final static int NEW = 518;
   private final static int RETURN = 519;
   private final static int VAR = 520;
   private final static int PRIVATE = 521;
   private final static int PROTECTED = 522;
   private final static int PUBLIC = 523;
   private final static int FOR = 524;
   private final static int DO = 525;
   private final static int BREAK = 526;
   private final static int CONTINUE = 527;
   private final static int ELSE = 528;
   private final static int EXTENDS = 529;
   private final static int STATIC = 530;
   private final static int INCLUDE = 531;
   private final static int REQUIRE = 532;
   private final static int INCLUDE_ONCE = 533;
   private final static int REQUIRE_ONCE = 534;
   private final static int UNSET = 535;
   private final static int FOREACH = 536;
   private final static int AS = 537;
   private final static int TEXT = 538;
   private final static int ISSET = 539;
   private final static int SWITCH = 540;
   private final static int CASE = 541;
   private final static int DEFAULT = 542;
   private final static int EXIT = 543;
   private final static int GLOBAL = 544;
   private final static int ELSEIF = 545;
   private final static int PRINT = 546;
   private final static int SYSTEM_STRING = 547;
   private final static int SIMPLE_SYSTEM_STRING = 548;
   private final static int COMPLEX_SYSTEM_STRING = 549;
   private final static int TEXT_ECHO = 550;
   private final static int ENDIF = 551;
   private final static int ENDWHILE = 552;
   private final static int ENDFOR = 553;
   private final static int ENDFOREACH = 554;
   private final static int ENDSWITCH = 555;
   private final static int XOR_RES = 556;
   private final static int AND_RES = 557;
   private final static int OR_RES = 558;
   private final static int LIST = 559;
   private final static int THIS = 560;
   private final static int TRUE = 561;
   private final static int FALSE = 562;
   private final static int CLONE = 563;
   private final static int INSTANCEOF = 564;
   private final static int CONST = 565;
   private final static int ABSTRACT = 566;
   private final static int FINAL = 567;
   private final static int DIE = 568;
   private final static int THROW = 569;
   private final static int TRY = 570;
   private final static int CATCH = 571;
   private final static int INTERFACE = 572;
   private final static int IMPLEMENTS = 573;
   private final static int IMPORT = 574;
   private final static int TEXT_PHP = 575;
   private final static int NAMESPACE = 576;
   private final static int USE = 577;
   private final static int LAST_IDENTIFIER_LEXEME = 1024;
   private final static IntMap _insensitiveReserved = new IntMap();
   private final static IntMap _reserved = new IntMap();
   private QuercusContext _quercus;
   private Path _sourceFile;
   private int _sourceOffset; // offset into the source file for the first line
   private ParserLocation _parserLocation = new ParserLocation();
   private ExprFactory _factory;
   private boolean _hasCr;
   private int _peek = -1;
   private ReadStream _is;
   private String _encoding;
   private CharBuffer _sb = new CharBuffer();
   private String _namespace = "";
   private HashMap<String, String> _namespaceUseMap = new HashMap<String, String>();
   private int _peekToken = -1;
   private String _lexeme = "";
   private String _heredocEnd = null;
   private GlobalScope _globalScope;
   private boolean _returnsReference = false;
   private Scope _scope;
   private InterpretedClassDef _classDef;
   private FunctionInfo _function;
   private boolean _isTop;
   private boolean _isNewExpr;
   private boolean _isIfTest;
   private int _classesParsed;
   private int _functionsParsed;
   private ArrayList<String> _loopLabelList = new ArrayList<String>();
   private int _labelsCreated;
   private String _comment;

   public QuercusParser(QuercusContext quercus) {
      _quercus = quercus;

      if (quercus == null) {
         _factory = ExprFactory.create();
      } else {
         _factory = quercus.createExprFactory();
      }

      _globalScope = new GlobalScope(_factory);
      _scope = _globalScope;
   }

   public QuercusParser(QuercusContext quercus,
           Path sourceFile,
           ReadStream is) {
      this(quercus);

      if (quercus == null || quercus.isUnicodeSemantics()) {
         init(sourceFile, is, "UTF-8");
      } else {
         init(sourceFile, is, "ISO-8859-1");
      }
   }

   private void init(Path sourceFile)
           throws IOException {
      init(sourceFile, sourceFile.openRead(), "UTF-8");
   }

   private void init(Path sourceFile, ReadStream is, String encoding) {
      _is = is;
      _encoding = encoding;

      if (sourceFile != null) {
         _parserLocation.setFileName(sourceFile);
         _sourceFile = sourceFile;
      } else {
         _parserLocation.setFileName("eval:");

         // php/2146
         _sourceFile = new NullPath("eval:");
      }

      _parserLocation.setLineNumber(1);

      _peek = -1;
      _peekToken = -1;
   }

   public void setLocation(String fileName, int line) {
      _parserLocation.setFileName(fileName);
      _parserLocation.setLineNumber(line);

      if (line > 0) {
         _sourceOffset = 1 - line;
      }
   }

   public static QuercusProgram parse(QuercusContext quercus,
           Path path,
           String encoding)
           throws IOException {
      ReadStream is = path.openRead();

      try {
         is.setEncoding(encoding);

         QuercusParser parser;
         parser = new QuercusParser(quercus, path, is);

         return parser.parse();
      } finally {
         is.close();
      }
   }

   public static QuercusProgram parse(QuercusContext quercus,
           Path path,
           String encoding,
           String fileName,
           int line)
           throws IOException {
      ReadStream is = path.openRead();

      try {
         is.setEncoding(encoding);

         QuercusParser parser;
         parser = new QuercusParser(quercus, path, is);

         if (fileName != null && line >= 0) {
            parser.setLocation(fileName, line);
         }

         return parser.parse();
      } finally {
         is.close();
      }
   }

   public static QuercusProgram parse(QuercusContext quercus,
           ReadStream is)
           throws IOException {
      QuercusParser parser;
      parser = new QuercusParser(quercus, is.getPath(), is);

      return parser.parse();
   }

   public static QuercusProgram parse(QuercusContext quercus,
           Path path, ReadStream is)
           throws IOException {
      return new QuercusParser(quercus, path, is).parse();
   }

   public static QuercusProgram parseEval(QuercusContext quercus, String str)
           throws IOException {
      Path path = new StringPath(str);

      QuercusParser parser = new QuercusParser(quercus, path, path.openRead());

      return parser.parseCode();
   }

   public static QuercusProgram parseEvalExpr(QuercusContext quercus, String str)
           throws IOException {
      Path path = new StringPath(str);

      QuercusParser parser = new QuercusParser(quercus, path, path.openRead());

      return parser.parseCode().createExprReturn();
   }

   public static AbstractFunction parseFunction(QuercusContext quercus,
           String name,
           String args,
           String code)
           throws IOException {
      Path argPath = new StringPath(args);
      Path codePath = new StringPath(code);

      QuercusParser parser = new QuercusParser(quercus);

      Function fun = parser.parseFunction(name, argPath, codePath);

      parser.close();

      return fun;
   }

   public boolean isUnicodeSemantics() {
      return _quercus != null && _quercus.isUnicodeSemantics();
   }

   public static Expr parse(QuercusContext quercus, String str)
           throws IOException {
      Path path = new StringPath(str);

      return new QuercusParser(quercus, path, path.openRead()).parseExpr();
   }

   public static Expr parseDefault(String str) {
      try {
         Path path = new StringPath(str);

         return new QuercusParser(null, path, path.openRead()).parseExpr();
      } catch (IOException e) {
         throw new QuercusRuntimeException(e);
      }
   }

   public static Expr parseDefault(ExprFactory factory, String str) {
      try {
         Path path = new StringPath(str);

         QuercusParser parser = new QuercusParser(null, path, path.openRead());

         parser._factory = factory;

         return parser.parseExpr();
      } catch (IOException e) {
         throw new QuercusRuntimeException(e);
      }
   }

   /**
    * Returns the current filename.
    */
   public String getFileName() {
      if (_sourceFile == null) {
         return null;
      } else {
         return _sourceFile.getPath();
      }
   }

   /**
    * Returns the current class name
    */
   public String getClassName() {
      if (_classDef != null) {
         return _classDef.getName();
      } else {
         return null;
      }
   }

   /**
    * Returns the current line
    */
   public int getLine() {
      return _parserLocation.getLineNumber();
   }

   public ExprFactory getExprFactory() {
      return _factory;
   }

   public ExprFactory getFactory() {
      return _factory;
   }

   public QuercusProgram parse()
           throws IOException {
      ClassDef globalClass = null;

      _function = getFactory().createFunctionInfo(_quercus, globalClass, "");
      _function.setPageMain(true);

      // quercus/0b0d
      _function.setVariableVar(true);
      _function.setUsesSymbolTable(true);

      Statement stmt = parseTop();

      QuercusProgram program = new QuercusProgram(_quercus, _sourceFile,
              _globalScope.getFunctionMap(),
              _globalScope.getFunctionList(),
              _globalScope.getClassMap(),
              _globalScope.getClassList(),
              _function,
              stmt);
      return program;

      /*
      com.caucho.vfs.WriteStream out = com.caucho
      .vfs.Vfs.lookup("stdout:").openWrite();
      out.setFlushOnNewline(true);
      stmt.debug(new JavaWriter(out));
       */
   }

   QuercusProgram parseCode()
           throws IOException {
      ClassDef globalClass = null;

      _function = getFactory().createFunctionInfo(_quercus, globalClass, "eval");
      // TODO: need param or better function name for non-global?
      _function.setGlobal(false);

      Location location = getLocation();

      ArrayList<Statement> stmtList = parseStatementList();

      return new QuercusProgram(_quercus, _sourceFile,
              _globalScope.getFunctionMap(),
              _globalScope.getFunctionList(),
              _globalScope.getClassMap(),
              _globalScope.getClassList(),
              _function,
              _factory.createBlock(location, stmtList));
   }

   public Function parseFunction(String name, Path argPath, Path codePath)
           throws IOException {
      ClassDef globalClass = null;

      _function = getFactory().createFunctionInfo(_quercus, globalClass, name);
      _function.setGlobal(false);
      _function.setPageMain(true);

      init(argPath);

      Arg[] args = parseFunctionArgDefinition();

      close();

      init(codePath);

      Statement[] statements = parseStatements();

      Function fun = _factory.createFunction(Location.UNKNOWN,
              name,
              _function,
              args,
              statements);

      close();

      return fun;
   }

   /**
    * Parses the top page.
    */
   Statement parseTop()
           throws IOException {
      _isTop = true;

      ArrayList<Statement> statements = new ArrayList<Statement>();

      Location location = getLocation();

      int token = parsePhpText();

      if (_lexeme.length() > 0) {
         statements.add(_factory.createText(location, _lexeme));
      }

      if (token == TEXT_ECHO) {
         parseEcho(statements);
      } else if (token == TEXT_PHP) {
         _peekToken = parseToken();

         if (_peekToken == IDENTIFIER && _lexeme.equalsIgnoreCase("php")) {
            _peekToken = -1;
         }
      }

      statements.addAll(parseStatementList());

      return _factory.createBlock(location, statements);
   }

   /*
    * Parses a statement list.
    */
   private Statement[] parseStatements()
           throws IOException {
      ArrayList<Statement> statementList = parseStatementList();

      Statement[] statements = new Statement[statementList.size()];

      statementList.toArray(statements);

      return statements;
   }

   /**
    * Parses a statement list.
    */
   private ArrayList<Statement> parseStatementList()
           throws IOException {
      ArrayList<Statement> statementList = new ArrayList<Statement>();

      while (true) {
         Location location = getLocation();

         int token = parseToken();

         switch (token) {
            case -1:
               return statementList;

            case ';':
               break;

            case ECHO:
               parseEcho(statementList);
               break;

            case PRINT:
               statementList.add(parsePrint());
               break;

            case UNSET:
               parseUnset(statementList);
               break;

            case ABSTRACT:
            case FINAL: {
               _peekToken = token;

               int modifiers = 0;
               do {
                  token = parseToken();

                  switch (token) {
                     case ABSTRACT:
                        modifiers |= M_ABSTRACT;
                        break;
                     case FINAL:
                        modifiers |= M_FINAL;
                        break;
                     case CLASS:
                        statementList.add(parseClassDefinition(modifiers));
                        break;
                     default:
                        throw error(L.l("expected 'class' at {0}",
                                tokenName(token)));
                  }
               } while (token != CLASS);
            }
            break;

            case FUNCTION: {
               Location functionLocation = getLocation();

               Function fun = parseFunctionDefinition(M_STATIC);

               if (!_isTop) {
                  statementList.add(
                          _factory.createFunctionDef(functionLocation, fun));
               }
            }
            break;

            case CLASS:
               // parseClassDefinition(0);
               statementList.add(parseClassDefinition(0));
               break;

            case INTERFACE:
               // parseClassDefinition(M_INTERFACE);
               statementList.add(parseClassDefinition(M_INTERFACE));
               break;

            case CONST:
               statementList.addAll(parseConstDefinition());
               break;

            case IF:
               statementList.add(parseIf());
               break;

            case SWITCH:
               statementList.add(parseSwitch());
               break;

            case WHILE:
               statementList.add(parseWhile());
               break;

            case DO:
               statementList.add(parseDo());
               break;

            case FOR:
               statementList.add(parseFor());
               break;

            case FOREACH:
               statementList.add(parseForeach());
               break;

            case PHP_END:
               return statementList;

            case RETURN:
               statementList.add(parseReturn());
               break;

            case THROW:
               statementList.add(parseThrow());
               break;

            case BREAK:
               statementList.add(parseBreak());
               break;

            case CONTINUE:
               statementList.add(parseContinue());
               break;

            case GLOBAL:
               statementList.add(parseGlobal());
               break;

            case STATIC:
               statementList.add(parseStatic());
               break;

            case TRY:
               statementList.add(parseTry());
               break;

            case NAMESPACE:
               statementList.addAll(parseNamespace());
               break;

            case USE:
               parseUse();
               break;

            case '{': {
               ArrayList<Statement> enclosedStatementList = parseStatementList();

               expect('}');

               statementList.addAll(enclosedStatementList);
            }
            break;

            case '}':
            case CASE:
            case DEFAULT:
            case ELSE:
            case ELSEIF:
            case ENDIF:
            case ENDWHILE:
            case ENDFOR:
            case ENDFOREACH:
            case ENDSWITCH:
               _peekToken = token;
               return statementList;

            case TEXT:
               if (_lexeme.length() > 0) {
                  statementList.add(_factory.createText(location, _lexeme));
               }
               break;

            case TEXT_PHP:
               if (_lexeme.length() > 0) {
                  statementList.add(_factory.createText(location, _lexeme));
               }

               _peekToken = parseToken();

               if (_peekToken == IDENTIFIER && _lexeme.equalsIgnoreCase("php")) {
                  _peekToken = -1;
               }
               break;

            case TEXT_ECHO:
               if (_lexeme.length() > 0) {
                  statementList.add(_factory.createText(location, _lexeme));
               }

               parseEcho(statementList);

               break;

            default:
               _peekToken = token;

               statementList.add(parseExprStatement());
               break;
         }
      }
   }

   private Statement parseStatement()
           throws IOException {
      Location location = getLocation();

      int token = parseToken();

      switch (token) {
         case ';':
            return _factory.createNullStatement();

         case '{':
            location = getLocation();

            ArrayList<Statement> statementList = parseStatementList();

            expect('}');

            return _factory.createBlock(location, statementList);

         case IF:
            return parseIf();

         case SWITCH:
            return parseSwitch();

         case WHILE:
            return parseWhile();

         case DO:
            return parseDo();

         case FOR:
            return parseFor();

         case FOREACH:
            return parseForeach();

         case TRY:
            return parseTry();

         case TEXT:
            if (_lexeme.length() > 0) {
               return _factory.createText(location, _lexeme);
            } else {
               return parseStatement();
            }

         case TEXT_PHP: {
            Statement stmt = null;

            if (_lexeme.length() > 0) {
               stmt = _factory.createText(location, _lexeme);
            }

            _peekToken = parseToken();

            if (_peekToken == IDENTIFIER && _lexeme.equalsIgnoreCase("php")) {
               _peekToken = -1;
            }

            if (stmt == null) {
               stmt = parseStatement();
            }

            return stmt;
         }

         default:
            Statement stmt = parseStatementImpl(token);

            token = parseToken();
            if (token != ';') {
               _peekToken = token;
            }

            return stmt;
      }
   }

   /**
    * Parses statements that expect to be terminated by ';'.
    */
   private Statement parseStatementImpl(int token)
           throws IOException {
      switch (token) {
         case ECHO: {
            Location location = getLocation();

            ArrayList<Statement> statementList = new ArrayList<Statement>();
            parseEcho(statementList);

            return _factory.createBlock(location, statementList);
         }

         case PRINT:
            return parsePrint();

         case UNSET:
            return parseUnset();

         case GLOBAL:
            return parseGlobal();

         case STATIC:
            return parseStatic();

         case BREAK:
            return parseBreak();

         case CONTINUE:
            return parseContinue();

         case RETURN:
            return parseReturn();

         case THROW:
            return parseThrow();

         case TRY:
            return parseTry();

         default:
            _peekToken = token;
            return parseExprStatement();

         /*
         default:
         throw error(L.l("unexpected token {0}.", tokenName(token)));
          */
      }
   }

   /**
    * Parses the echo statement.
    */
   private void parseEcho(ArrayList<Statement> statements)
           throws IOException {
      Location location = getLocation();

      while (true) {
         Expr expr = parseTopExpr();

         createEchoStatements(location, statements, expr);

         int token = parseToken();

         if (token != ',') {
            _peekToken = token;
            return;
         }
      }
   }

   /**
    * Creates echo statements from an expression.
    */
   private void createEchoStatements(Location location,
           ArrayList<Statement> statements,
           Expr expr) {
      if (expr == null) {
         // since AppendExpr.getNext() can be null.
      } else if (expr instanceof BinaryAppendExpr) {
         BinaryAppendExpr append = (BinaryAppendExpr) expr;

         // TODO: children of append print differently?

         createEchoStatements(location, statements, append.getValue());
         createEchoStatements(location, statements, append.getNext());
      } else if (expr instanceof LiteralStringExpr) {
         LiteralStringExpr string = (LiteralStringExpr) expr;

         Statement statement = _factory.createText(location, string.evalConstant().toString());

         statements.add(statement);
      } else {
         Statement statement = _factory.createEcho(location, expr);

         statements.add(statement);
      }
   }

   /**
    * Parses the print statement.
    */
   private Statement parsePrint()
           throws IOException {
      return _factory.createExpr(getLocation(), parsePrintExpr());
   }

   /**
    * Parses the print statement.
    */
   private Expr parsePrintExpr()
           throws IOException {
      ArrayList<Expr> args = new ArrayList<Expr>();
      args.add(parseTopExpr());

      return _factory.createCall(this, "print", args);
   }

   /**
    * Parses the global statement.
    */
   private Statement parseGlobal()
           throws IOException {
      ArrayList<Statement> statementList = new ArrayList<Statement>();

      Location location = getLocation();

      while (true) {
         Expr expr = parseTopExpr();

         if (expr instanceof VarExpr) {
            VarExpr var = (VarExpr) expr;

            _function.setUsesGlobal(true);

            // php/323c
            // php/3a6g, php/3a58
            //var.getVarInfo().setGlobal();

            statementList.add(_factory.createGlobal(location, var));
         } else if (expr instanceof VarVarExpr) {
            VarVarExpr var = (VarVarExpr) expr;

            statementList.add(_factory.createVarGlobal(location, var));
         } else {
            throw error(L.l("unknown expr {0} to global", expr));
         }

         // statementList.add(new ExprStatement(expr));

         int token = parseToken();

         if (token != ',') {
            _peekToken = token;
            return _factory.createBlock(location, statementList);
         }
      }
   }

   /**
    * Parses the static statement.
    */
   private Statement parseStatic()
           throws IOException {
      ArrayList<Statement> statementList = new ArrayList<Statement>();

      Location location = getLocation();

      while (true) {
         expect('$');

         String name = parseIdentifier();

         VarExpr var = _factory.createVar(_function.createVar(name));

         Expr init = null;

         int token = parseToken();

         if (token == '=') {
            init = parseExpr();
            token = parseToken();
         }

         // var.getVarInfo().setReference();

         if (_classDef != null) {
            statementList.add(_factory.createClassStatic(location,
                    _classDef.getName(),
                    var,
                    init));
         } else {
            statementList.add(_factory.createStatic(location, var, init));
         }

         if (token != ',') {
            _peekToken = token;
            return _factory.createBlock(location, statementList);
         }
      }
   }

   /**
    * Parses the unset statement.
    */
   private Statement parseUnset()
           throws IOException {
      Location location = getLocation();

      ArrayList<Statement> statementList = new ArrayList<Statement>();
      parseUnset(statementList);

      return _factory.createBlock(location, statementList);
   }

   /**
    * Parses the unset statement.
    */
   private void parseUnset(ArrayList<Statement> statementList)
           throws IOException {
      Location location = getLocation();

      int token = parseToken();

      if (token != '(') {
         _peekToken = token;

         statementList.add(parseTopExpr().createUnset(_factory, location));

         return;
      }

      do {
         // TODO: statementList.add(
         //    parseTopExpr().createUnset(_factory, getLocation()));

         Expr topExpr = parseTopExpr();

         statementList.add(topExpr.createUnset(_factory, getLocation()));
      } while ((token = parseToken()) == ',');

      _peekToken = token;
      expect(')');
   }

   /**
    * Parses the if statement
    */
   private Statement parseIf()
           throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      try {
         Location location = getLocation();

         expect('(');

         _isIfTest = true;
         Expr test = parseExpr();
         _isIfTest = false;

         expect(')');

         int token = parseToken();

         if (token == ':') {
            return parseAlternateIf(test, location);
         } else {
            _peekToken = token;
         }

         Statement trueBlock = null;

         trueBlock = parseStatement();

         Statement falseBlock = null;

         token = parseToken();

         if (token == ELSEIF) {
            falseBlock = parseIf();
         } else if (token == ELSE) {
            falseBlock = parseStatement();
         } else {
            _peekToken = token;
         }

         return _factory.createIf(location, test, trueBlock, falseBlock);

      } finally {
         _isTop = oldTop;
      }
   }

   /**
    * Parses the if statement
    */
   private Statement parseAlternateIf(Expr test, Location location)
           throws IOException {
      Statement trueBlock = null;

      trueBlock = _factory.createBlock(location, parseStatementList());

      Statement falseBlock = null;

      int token = parseToken();

      if (token == ELSEIF) {
         Location subLocation = getLocation();

         Expr subTest = parseExpr();
         expect(':');

         falseBlock = parseAlternateIf(subTest, subLocation);
      } else if (token == ELSE) {
         expect(':');

         falseBlock = _factory.createBlock(getLocation(), parseStatementList());

         expect(ENDIF);
      } else {
         _peekToken = token;
         expect(ENDIF);
      }

      return _factory.createIf(location, test, trueBlock, falseBlock);
   }

   /**
    * Parses the switch statement
    */
   private Statement parseSwitch()
           throws IOException {
      Location location = getLocation();

      boolean oldTop = _isTop;
      _isTop = false;

      String label = pushSwitchLabel();

      try {
         expect('(');

         Expr test = parseExpr();

         expect(')');

         boolean isAlternate = false;

         int token = parseToken();

         if (token == ':') {
            isAlternate = true;
         } else if (token == '{') {
            isAlternate = false;
         } else {
            _peekToken = token;

            expect('{');
         }

         ArrayList<Expr[]> caseList = new ArrayList<Expr[]>();
         ArrayList<BlockStatement> blockList = new ArrayList<BlockStatement>();

         ArrayList<Integer> fallThroughList = new ArrayList<Integer>();
         BlockStatement defaultBlock = null;

         while ((token = parseToken()) == CASE || token == DEFAULT) {
            Location caseLocation = getLocation();

            ArrayList<Expr> valueList = new ArrayList<Expr>();
            boolean isDefault = false;

            while (token == CASE || token == DEFAULT) {
               if (token == CASE) {
                  Expr value = parseExpr();

                  valueList.add(value);
               } else {
                  isDefault = true;
               }

               token = parseToken();
               if (token == ':') {
               } else if (token == ';') {
                  // TODO: warning?
               } else {
                  throw error("expected ':' at " + tokenName(token));
               }

               token = parseToken();
            }

            _peekToken = token;

            Expr[] values = new Expr[valueList.size()];
            valueList.toArray(values);

            ArrayList<Statement> newBlockList = parseStatementList();

            for (int fallThrough : fallThroughList) {
               BlockStatement block = blockList.get(fallThrough);

               boolean isDefaultBlock = block == defaultBlock;

               block = block.append(newBlockList);

               blockList.set(fallThrough, block);

               if (isDefaultBlock) {
                  defaultBlock = block;
               }
            }

            BlockStatement block = _factory.createBlockImpl(caseLocation, newBlockList);

            if (values.length > 0) {
               caseList.add(values);

               blockList.add(block);
            }

            if (isDefault) {
               defaultBlock = block;
            }

            if (blockList.size() > 0
                    && !fallThroughList.contains(blockList.size() - 1)) {
               fallThroughList.add(blockList.size() - 1);
            }

            if (block.fallThrough() != Statement.FALL_THROUGH) {
               fallThroughList.clear();
            }
         }

         _peekToken = token;

         if (isAlternate) {
            expect(ENDSWITCH);
         } else {
            expect('}');
         }

         return _factory.createSwitch(location, test,
                 caseList, blockList,
                 defaultBlock, label);
      } finally {
         _isTop = oldTop;

         popLoopLabel();
      }
   }

   /**
    * Parses the 'while' statement
    */
   private Statement parseWhile()
           throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      String label = pushWhileLabel();

      try {
         Location location = getLocation();

         expect('(');

         _isIfTest = true;
         Expr test = parseExpr();
         _isIfTest = false;

         expect(')');

         Statement block;

         int token = parseToken();

         if (token == ':') {
            block = _factory.createBlock(getLocation(), parseStatementList());

            expect(ENDWHILE);
         } else {
            _peekToken = token;

            block = parseStatement();
         }

         return _factory.createWhile(location, test, block, label);
      } finally {
         _isTop = oldTop;

         popLoopLabel();
      }
   }

   /**
    * Parses the 'do' statement
    */
   private Statement parseDo()
           throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      String label = pushDoLabel();

      try {
         Location location = getLocation();

         Statement block = parseStatement();

         expect(WHILE);
         expect('(');

         _isIfTest = true;
         Expr test = parseExpr();
         _isIfTest = false;

         expect(')');

         return _factory.createDo(location, test, block, label);
      } finally {
         _isTop = oldTop;

         popLoopLabel();
      }
   }

   /**
    * Parses the 'for' statement
    */
   private Statement parseFor()
           throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      String label = pushForLabel();

      try {
         Location location = getLocation();

         expect('(');

         Expr init = null;

         int token = parseToken();
         if (token != ';') {
            _peekToken = token;
            init = parseTopCommaExpr();
            expect(';');
         }

         Expr test = null;

         token = parseToken();
         if (token != ';') {
            _peekToken = token;

            _isIfTest = true;
            test = parseTopCommaExpr();
            _isIfTest = false;

            expect(';');
         }

         Expr incr = null;

         token = parseToken();
         if (token != ')') {
            _peekToken = token;
            incr = parseTopCommaExpr();
            expect(')');
         }

         Statement block;

         token = parseToken();

         if (token == ':') {
            block = _factory.createBlock(getLocation(), parseStatementList());

            expect(ENDFOR);
         } else {
            _peekToken = token;

            block = parseStatement();
         }

         return _factory.createFor(location, init, test, incr, block, label);
      } finally {
         _isTop = oldTop;

         popLoopLabel();
      }
   }

   /**
    * Parses the 'foreach' statement
    */
   private Statement parseForeach()
           throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      String label = pushForeachLabel();

      try {
         Location location = getLocation();

         expect('(');

         Expr objExpr = parseTopExpr();

         expect(AS);

         boolean isRef = false;

         int token = parseToken();
         if (token == '&') {
            isRef = true;
         } else {
            _peekToken = token;
         }

         AbstractVarExpr valueExpr = (AbstractVarExpr) parseLeftHandSide();

         AbstractVarExpr keyVar = null;
         AbstractVarExpr valueVar;

         token = parseToken();

         if (token == ARRAY_RIGHT) {
            if (isRef) {
               throw error(L.l("key reference is forbidden in foreach"));
            }

            keyVar = valueExpr;

            token = parseToken();

            if (token == '&') {
               isRef = true;
            } else {
               _peekToken = token;
            }

            valueVar = (AbstractVarExpr) parseLeftHandSide();

            token = parseToken();
         } else {
            valueVar = valueExpr;
         }

         if (token != ')') {
            throw error(L.l("expected ')' in foreach"));
         }

         Statement block;

         token = parseToken();

         if (token == ':') {
            block = _factory.createBlock(getLocation(), parseStatementList());

            expect(ENDFOREACH);
         } else {
            _peekToken = token;

            block = parseStatement();
         }

         return _factory.createForeach(location, objExpr, keyVar,
                 valueVar, isRef, block, label);
      } finally {
         _isTop = oldTop;

         popLoopLabel();
      }
   }

   /**
    * Parses the try statement
    */
   private Statement parseTry()
           throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      try {
         Location location = getLocation();

         Statement block = null;

         try {
            block = parseStatement();
         } finally {
            //  _scope = oldScope;
         }

         TryStatement stmt = _factory.createTry(location, block);

         int token = parseToken();

         while (token == CATCH) {
            expect('(');

            String id = parseNamespaceIdentifier();

            AbstractVarExpr lhs = parseLeftHandSide();

            expect(')');

            block = parseStatement();

            stmt.addCatch(id, lhs, block);

            token = parseToken();
         }

         _peekToken = token;

         return stmt;
      } finally {
         _isTop = oldTop;
      }
   }

   /**
    * Parses a function definition
    */
   private Function parseFunctionDefinition(int modifiers)
           throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      boolean oldReturnsReference = _returnsReference;
      FunctionInfo oldFunction = _function;

      boolean isAbstract = (modifiers & M_ABSTRACT) != 0;
      boolean isStatic = (modifiers & M_STATIC) != 0;

      if (_classDef != null && _classDef.isInterface()) {
         isAbstract = true;
      }

      try {
         _returnsReference = false;

         int token = parseToken();

         String comment = _comment;
         _comment = null;

         if (token == '&') {
            _returnsReference = true;
         } else {
            _peekToken = token;
         }

         String name;

         name = parseIdentifier();

         if (_classDef == null) {
            name = resolveIdentifier(name);
         }

         if (isAbstract && !_scope.isAbstract()) {
            if (_classDef != null) {
               throw error(L.l(
                       "'{0}' may not be abstract because class {1} is not abstract.",
                       name, _classDef.getName()));
            } else {
               throw error(L.l(
                       "'{0}' may not be abstract. Abstract functions are only "
                       + "allowed in abstract classes.",
                       name));
            }
         }

         boolean isConstructor = false;

         if (_classDef != null
                 && (name.equals(_classDef.getName())
                 || name.equals("__constructor"))) {
            if (isStatic) {
               throw error(L.l(
                       "'{0}:{1}' may not be static because class constructors "
                       + "may not be static",
                       _classDef.getName(), name));
            }

            isConstructor = true;
         }

         _function = getFactory().createFunctionInfo(_quercus, _classDef, name);
         _function.setPageStatic(oldTop);
         _function.setConstructor(isConstructor);

         _function.setReturnsReference(_returnsReference);

         Location location = getLocation();

         expect('(');

         Arg[] args = parseFunctionArgDefinition();

         expect(')');

         if (_classDef != null
                 && "__call".equals(name)
                 && args.length != 2) {
            throw error(L.l("{0}::{1} must have exactly two arguments defined",
                    _classDef.getName(), name));
         }

         Function function;

         if (isAbstract) {
            expect(';');

            function = _factory.createMethodDeclaration(location,
                    _classDef, name,
                    _function, args);
         } else {
            expect('{');

            Statement[] statements = null;

            Scope oldScope = _scope;
            try {
               _scope = new FunctionScope(_factory, oldScope);
               statements = parseStatements();
            } finally {
               _scope = oldScope;
            }

            expect('}');

            if (_classDef != null) {
               function = _factory.createObjectMethod(location,
                       _classDef,
                       name, _function,
                       args, statements);
            } else {
               function = _factory.createFunction(location, name,
                       _function, args,
                       statements);
            }
         }

         function.setGlobal(oldTop);
         function.setStatic((modifiers & M_STATIC) != 0);
         function.setFinal((modifiers & M_FINAL) != 0);

         function.setParseIndex(_functionsParsed++);
         function.setComment(comment);

         if ((modifiers & M_PROTECTED) != 0) {
            function.setVisibility(Visibility.PROTECTED);
         } else if ((modifiers & M_PRIVATE) != 0) {
            function.setVisibility(Visibility.PRIVATE);
         }

         _scope.addFunction(name, function, oldTop);

         /*
         com.caucho.vfs.WriteStream out = com.caucho.vfs
         .Vfs.lookup("stdout:").openWrite();
         out.setFlushOnNewline(true);
         function.debug(new JavaWriter(out));
          */

         return function;
      } finally {
         _returnsReference = oldReturnsReference;
         _function = oldFunction;
         _isTop = oldTop;
      }
   }

   /**
    * Parses a function definition
    */
   private Expr parseClosure()
           throws IOException {
      boolean oldTop = _isTop;
      _isTop = false;

      boolean oldReturnsReference = _returnsReference;
      FunctionInfo oldFunction = _function;

      try {
         _returnsReference = false;

         int token = parseToken();

         String comment = null;

         if (token == '&') {
            _returnsReference = true;
         } else {
            _peekToken = token;
         }

         String name = "__quercus_closure_" + _functionsParsed;

         ClassDef classDef = null;
         _function = getFactory().createFunctionInfo(_quercus, classDef, name);
         _function.setReturnsReference(_returnsReference);
         _function.setClosure(true);

         Location location = getLocation();

         expect('(');

         Arg[] args = parseFunctionArgDefinition();

         expect(')');

         Arg[] useArgs;
         ArrayList<VarExpr> useVars = new ArrayList<VarExpr>();

         token = parseToken();

         if (token == USE) {
            expect('(');

            useArgs = parseFunctionArgDefinition();

            for (Arg arg : useArgs) {
               VarExpr var = _factory.createVar(
                       oldFunction.createVar(arg.getName()));

               useVars.add(var);
            }

            expect(')');
         } else {
            useArgs = new Arg[0];

            _peekToken = token;
         }

         expect('{');

         Statement[] statements = null;

         Scope oldScope = _scope;
         try {
            _scope = new FunctionScope(_factory, oldScope);
            statements = parseStatements();
         } finally {
            _scope = oldScope;
         }

         expect('}');

         Function function = _factory.createFunction(location, name,
                 _function, args,
                 statements);

         function.setParseIndex(_functionsParsed++);
         function.setComment(comment);
         function.setClosure(true);
         function.setClosureUseArgs(useArgs);

         _globalScope.addFunction(name, function, oldTop);

         return _factory.createClosure(location, function, useVars);
      } finally {
         _returnsReference = oldReturnsReference;
         _function = oldFunction;
         _isTop = oldTop;
      }
   }

   private Arg[] parseFunctionArgDefinition()
           throws IOException {
      LinkedHashMap<String, Arg> argMap = new LinkedHashMap<String, Arg>();

      while (true) {
         int token = parseToken();
         boolean isReference = false;

         // php/076b, php/1c02
         // TODO: save arg type for type checking upon function call
         String expectedClass = null;
         if (token != ')'
                 && token != '&'
                 && token != '$'
                 && token != -1) {
            _peekToken = token;
            expectedClass = parseIdentifier();
            token = parseToken();
         }

         if (token == '&') {
            isReference = true;
            token = parseToken();
         }

         if (token != '$') {
            _peekToken = token;
            break;
         }

         String argName = parseIdentifier();
         Expr defaultExpr = _factory.createRequired();

         token = parseToken();
         if (token == '=') {
            // TODO: actually needs to be primitive
            defaultExpr = parseUnary(); // parseTerm(false);

            token = parseToken();
         }

         Arg arg = new Arg(argName, defaultExpr, isReference, expectedClass);

         if (argMap.get(argName) != null && _quercus.isStrict()) {
            throw error(L.l("aliasing of function argument '{0}'", argName));
         }

         argMap.put(argName, arg);

         VarInfo var = _function.createVar(argName);

         if (token != ',') {
            _peekToken = token;
            break;
         }
      }

      Arg[] args = new Arg[argMap.size()];

      argMap.values().toArray(args);

      return args;
   }

   /**
    * Parses the 'return' statement
    */
   private Statement parseBreak()
           throws IOException {
      // commented out for adodb (used by Moodle and others)
      // TODO: should only throw fatal error if break statement is reached
      //      during execution

      if (!_isTop && _loopLabelList.size() == 0 && !_quercus.isLooseParse()) {
         throw error(L.l("cannot 'break' inside a function"));
      }

      Location location = getLocation();

      int token = parseToken();

      switch (token) {
         case ';':
            _peekToken = token;

            return _factory.createBreak(location,
                    null,
                    (ArrayList<String>) _loopLabelList.clone());

         default:
            _peekToken = token;

            Expr expr = parseTopExpr();

            return _factory.createBreak(location,
                    expr,
                    (ArrayList<String>) _loopLabelList.clone());
      }
   }

   /**
    * Parses the 'return' statement
    */
   private Statement parseContinue()
           throws IOException {
      if (!_isTop && _loopLabelList.size() == 0 && !_quercus.isLooseParse()) {
         throw error(L.l("cannot 'continue' inside a function"));
      }

      Location location = getLocation();

      int token = parseToken();

      switch (token) {
         case TEXT_PHP:
         case ';':
            _peekToken = token;

            return _factory.createContinue(location,
                    null,
                    (ArrayList<String>) _loopLabelList.clone());

         default:
            _peekToken = token;

            Expr expr = parseTopExpr();

            return _factory.createContinue(location,
                    expr,
                    (ArrayList<String>) _loopLabelList.clone());
      }
   }

   /**
    * Parses the 'return' statement
    */
   private Statement parseReturn()
           throws IOException {
      Location location = getLocation();

      int token = parseToken();

      switch (token) {
         case ';':
            _peekToken = token;

            return _factory.createReturn(location, _factory.createNull());

         default:
            _peekToken = token;

            Expr expr = parseTopExpr();

            /*
            if (_returnsReference)
            expr = expr.createRef();
            else
            expr = expr.createCopy();
             */

            if (_returnsReference) {
               return _factory.createReturnRef(location, expr);
            } else {
               return _factory.createReturn(location, expr);
            }
      }
   }

   /**
    * Parses the 'throw' statement
    */
   private Statement parseThrow()
           throws IOException {
      Location location = getLocation();

      Expr expr = parseExpr();

      return _factory.createThrow(location, expr);
   }

   /**
    * Parses a class definition
    */
   private Statement parseClassDefinition(int modifiers)
           throws IOException {
      String name = parseIdentifier();

      name = resolveIdentifier(name);

      String comment = _comment;

      String parentName = null;

      ArrayList<String> ifaceList = new ArrayList<String>();

      int token = parseToken();
      if (token == EXTENDS) {
         if ((modifiers & M_INTERFACE) != 0) {
            do {
               ifaceList.add(parseNamespaceIdentifier());

               token = parseToken();
            } while (token == ',');
         } else {
            parentName = parseNamespaceIdentifier();

            token = parseToken();
         }
      }

      if ((modifiers & M_INTERFACE) == 0 && token == IMPLEMENTS) {
         do {
            ifaceList.add(parseNamespaceIdentifier());

            token = parseToken();
         } while (token == ',');
      }

      _peekToken = token;

      InterpretedClassDef oldClass = _classDef;
      Scope oldScope = _scope;

      try {
         _classDef = oldScope.addClass(getLocation(),
                 name, parentName, ifaceList,
                 _classesParsed++,
                 _isTop);

         _classDef.setComment(comment);

         if ((modifiers & M_ABSTRACT) != 0) {
            _classDef.setAbstract(true);
         }
         if ((modifiers & M_INTERFACE) != 0) {
            _classDef.setInterface(true);
         }
         if ((modifiers & M_FINAL) != 0) {
            _classDef.setFinal(true);
         }

         _scope = new ClassScope(_classDef);

         expect('{');

         parseClassContents();

         expect('}');

         return _factory.createClassDef(getLocation(), _classDef);
      } finally {
         _classDef = oldClass;
         _scope = oldScope;
      }
   }

   /**
    * Parses a statement list.
    */
   private void parseClassContents()
           throws IOException {
      while (true) {
         _comment = null;

         int token = parseToken();

         switch (token) {
            case ';':
               break;

            case FUNCTION: {
               Function fun = parseFunctionDefinition(0);
               fun.setStatic(false);
               break;
            }

            case CLASS:
               parseClassDefinition(0);
               break;

            /* quercus/0260
            case VAR:
            parseClassVarDefinition(false);
            break;
             */

            case CONST:
               parseClassConstDefinition();
               break;

            case PUBLIC:
            case PRIVATE:
            case PROTECTED:
            case STATIC:
            case FINAL:
            case ABSTRACT: {
               _peekToken = token;
               int modifiers = parseModifiers();

               int token2 = parseToken();

               if (token2 == FUNCTION) {
                  Function fun = parseFunctionDefinition(modifiers);
               } else {
                  _peekToken = token2;

                  parseClassVarDefinition(modifiers);
               }
            }
            break;

            case IDENTIFIER:
               if (_lexeme.equals("var")) {
                  parseClassVarDefinition(0);
               } else {
                  _peekToken = token;
                  return;
               }
               break;

            case -1:
            case '}':
            default:
               _peekToken = token;
               return;
         }
      }
   }

   /**
    * Parses a function definition
    */
   private void parseClassVarDefinition(int modifiers)
           throws IOException {
      int token;

      do {
         expect('$');

         String comment = _comment;

         String name = parseIdentifier();

         token = parseToken();

         Expr expr = null;

         if (token == '=') {
            expr = parseExpr();
         } else {
            _peekToken = token;
            expr = _factory.createNull();
         }

         StringValue nameV = createStringValue(name);

         if ((modifiers & M_STATIC) != 0) {
            ((ClassScope) _scope).addStaticVar(nameV, expr, _comment);
         } else if ((modifiers & M_PRIVATE) != 0) {
            ((ClassScope) _scope).addVar(nameV,
                    expr,
                    FieldVisibility.PRIVATE,
                    comment);
         } else if ((modifiers & M_PROTECTED) != 0) {
            ((ClassScope) _scope).addVar(nameV,
                    expr,
                    FieldVisibility.PROTECTED,
                    comment);
         } else {
            ((ClassScope) _scope).addVar(nameV,
                    expr,
                    FieldVisibility.PUBLIC,
                    comment);
         }

         token = parseToken();
      } while (token == ',');

      _peekToken = token;
   }

   /**
    * Parses a const definition
    */
   private ArrayList<Statement> parseConstDefinition()
           throws IOException {
      ArrayList<Statement> constList = new ArrayList<Statement>();

      int token;

      do {
         String name = parseNamespaceIdentifier();

         expect('=');

         Expr expr = parseExpr();

         ArrayList<Expr> args = new ArrayList<Expr>();
         args.add(_factory.createString(name));
         args.add(expr);

         Expr fun = _factory.createCall(this, "define", args);

         constList.add(_factory.createExpr(getLocation(), fun));
         // _scope.addConstant(name, expr);

         token = parseToken();
      } while (token == ',');

      _peekToken = token;

      return constList;
   }

   /**
    * Parses a const definition
    */
   private void parseClassConstDefinition()
           throws IOException {
      int token;

      do {
         String name = parseIdentifier();

         expect('=');

         Expr expr = parseExpr();

         ((ClassScope) _scope).addConstant(name, expr);

         token = parseToken();
      } while (token == ',');

      _peekToken = token;
   }

   private int parseModifiers()
           throws IOException {
      int token;
      int modifiers = 0;

      while (true) {
         token = parseToken();

         switch (token) {
            case PUBLIC:
               modifiers |= M_PUBLIC;
               break;

            case PRIVATE:
               modifiers |= M_PRIVATE;
               break;

            case PROTECTED:
               modifiers |= M_PROTECTED;
               break;

            case FINAL:
               modifiers |= M_FINAL;
               break;

            case STATIC:
               modifiers |= M_STATIC;
               break;

            case ABSTRACT:
               modifiers |= M_ABSTRACT;
               break;

            default:
               _peekToken = token;
               return modifiers;
         }
      }
   }

   private ArrayList<Statement> parseNamespace()
           throws IOException {
      int token = parseToken();

      String var = "";

      if (token == IDENTIFIER) {
         var = _lexeme;

         token = parseToken();
      }

      if (var.startsWith("\\")) {
         var = var.substring(1);
      }

      String oldNamespace = _namespace;

      _namespace = var;

      if (token == '{') {
         ArrayList<Statement> statementList = parseStatementList();

         expect('}');

         _namespace = oldNamespace;

         return statementList;
      } else if (token == ';') {
         return new ArrayList<Statement>();
      } else {
         throw error(L.l("namespace must be followed by '{' or ';'"));
      }
   }

   private void parseUse()
           throws IOException {
      int token = parseNamespaceIdentifier(read());

      String name = _lexeme;

      int ns = name.lastIndexOf('\\');

      String tail;
      if (ns >= 0) {
         tail = name.substring(ns + 1);
      } else {
         tail = name;
      }

      if (name.startsWith("\\")) {
         name = name.substring(1);
      }

      token = parseToken();

      if (token == ';') {
         _namespaceUseMap.put(tail, name);
         return;
      } else if (token == AS) {
         do {
            tail = parseIdentifier();

            _namespaceUseMap.put(tail, name);
         } while ((token = parseToken()) == ',');
      }

      _peekToken = token;

      expect(';');
   }

   /**
    * Parses an expression statement.
    */
   private Statement parseExprStatement()
           throws IOException {
      Location location = getLocation();

      Expr expr = parseTopExpr();

      Statement statement = _factory.createExpr(location, expr);

      int token = parseToken();
      _peekToken = token;

      switch (token) {
         case -1:
         case ';':
         case '}':
         case PHP_END:
         case TEXT:
         case TEXT_PHP:
         case TEXT_ECHO:
            break;

         default:
            expect(';');
            break;
      }

      return statement;
   }

   /**
    * Parses a top expression.
    */
   private Expr parseTopExpr()
           throws IOException {
      return parseExpr();
   }

   /**
    * Parses a top expression.
    */
   private Expr parseTopCommaExpr()
           throws IOException {
      return parseCommaExpr();
   }

   /**
    * Parses a comma expression.
    */
   private Expr parseCommaExpr()
           throws IOException {
      Expr expr = parseExpr();

      while (true) {
         int token = parseToken();

         switch (token) {
            case ',':
               expr = _factory.createComma(expr, parseExpr());
               break;
            default:
               _peekToken = token;
               return expr;
         }
      }
   }

   /**
    * Parses an expression with optional '&'.
    */
   private Expr parseRefExpr()
           throws IOException {
      int token = parseToken();

      boolean isRef = token == '&';

      if (!isRef) {
         _peekToken = token;
      }

      Expr expr = parseExpr();

      if (isRef) {
         expr = _factory.createRef(expr);
      }

      return expr;
   }

   /**
    * Parses an expression.
    */
   private Expr parseExpr()
           throws IOException {
      return parseWeakOrExpr();
   }

   /**
    * Parses a logical xor expression.
    */
   private Expr parseWeakOrExpr()
           throws IOException {
      Expr expr = parseWeakXorExpr();

      while (true) {
         int token = parseToken();

         switch (token) {
            case OR_RES:
               expr = _factory.createOr(expr, parseWeakXorExpr());
               break;
            default:
               _peekToken = token;
               return expr;
         }
      }
   }

   /**
    * Parses a logical xor expression.
    */
   private Expr parseWeakXorExpr()
           throws IOException {
      Expr expr = parseWeakAndExpr();

      while (true) {
         int token = parseToken();

         switch (token) {
            case XOR_RES:
               expr = _factory.createXor(expr, parseWeakAndExpr());
               break;
            default:
               _peekToken = token;
               return expr;
         }
      }
   }

   /**
    * Parses a logical and expression.
    */
   private Expr parseWeakAndExpr()
           throws IOException {
      Expr expr = parseConditionalExpr();

      while (true) {
         int token = parseToken();

         switch (token) {
            case AND_RES:
               expr = _factory.createAnd(expr, parseConditionalExpr());
               break;
            default:
               _peekToken = token;
               return expr;
         }
      }
   }

   /**
    * Parses a conditional expression.
    */
   private Expr parseConditionalExpr()
           throws IOException {
      Expr expr = parseOrExpr();

      while (true) {
         int token = parseToken();

         switch (token) {
            case '?':
               token = parseToken();

               if (token == ':') {
                  expr = _factory.createShortConditional(expr, parseOrExpr());
               } else {
                  _peekToken = token;

                  Expr trueExpr = parseExpr();
                  expect(':');
                  // php/33c1
                  expr = _factory.createConditional(expr, trueExpr, parseOrExpr());
               }
               break;
            default:
               _peekToken = token;
               return expr;
         }
      }
   }

   /**
    * Parses a logical or expression.
    */
   private Expr parseOrExpr()
           throws IOException {
      Expr expr = parseAndExpr();

      while (true) {
         int token = parseToken();

         switch (token) {
            case C_OR:
               expr = _factory.createOr(expr, parseAndExpr());
               break;
            default:
               _peekToken = token;
               return expr;
         }
      }
   }

   /**
    * Parses a logical and expression.
    */
   private Expr parseAndExpr()
           throws IOException {
      Expr expr = parseBitOrExpr();

      while (true) {
         int token = parseToken();

         switch (token) {
            case C_AND:
               expr = _factory.createAnd(expr, parseBitOrExpr());
               break;
            default:
               _peekToken = token;
               return expr;
         }
      }
   }

   /**
    * Parses a bit or expression.
    */
   private Expr parseBitOrExpr()
           throws IOException {


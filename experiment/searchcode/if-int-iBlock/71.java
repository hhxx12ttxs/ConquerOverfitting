package javaparser;

import lpg.lpgjavaruntime.*;

public class JavaParser extends PrsStream implements RuleAction
{
    private static ParseTable prs = new JavaParserprs();
    private DeterministicParser dtParser;

    public DeterministicParser getParser() { return dtParser; }
    private void setResult(Object object) { dtParser.setSym1(object); }
    public Object getRhsSym(int i) { return dtParser.getSym(i); }

    public int getRhsTokenIndex(int i) { return dtParser.getToken(i); }
    public IToken getRhsIToken(int i) { return super.getIToken(getRhsTokenIndex(i)); }
    
    public int getRhsFirstTokenIndex(int i) { return dtParser.getFirstToken(i); }
    public IToken getRhsFirstIToken(int i) { return super.getIToken(getRhsFirstTokenIndex(i)); }

    public int getRhsLastTokenIndex(int i) { return dtParser.getLastToken(i); }
    public IToken getRhsLastIToken(int i) { return super.getIToken(getRhsLastTokenIndex(i)); }

    public int getLeftSpan() { return dtParser.getFirstToken(); }
    public IToken getLeftIToken()  { return super.getIToken(getLeftSpan()); }

    public int getRightSpan() { return dtParser.getLastToken(); }
    public IToken getRightIToken() { return super.getIToken(getRightSpan()); }

    public int getRhsErrorTokenIndex(int i)
    {
        int index = dtParser.getToken(i);
        IToken err = super.getIToken(index);
        return (err instanceof ErrorToken ? index : 0);
    }
    public ErrorToken getRhsErrorIToken(int i)
    {
        int index = dtParser.getToken(i);
        IToken err = super.getIToken(index);
        return (ErrorToken) (err instanceof ErrorToken ? err : null);
    }

    public JavaParser(LexStream lexStream)
    {
        super(lexStream);

        try
        {
            super.remapTerminalSymbols(orderedTerminalSymbols(), JavaParserprs.EOFT_SYMBOL);
        }
        catch(NullExportedSymbolsException e) {
        }
        catch(NullTerminalSymbolsException e) {
        }
        catch(UnimplementedTerminalsException e)
        {
            java.util.ArrayList unimplemented_symbols = e.getSymbols();
            System.out.println("The Lexer will not scan the following token(s):");
            for (int i = 0; i < unimplemented_symbols.size(); i++)
            {
                Integer id = (Integer) unimplemented_symbols.get(i);
                System.out.println("    " + JavaParsersym.orderedTerminalSymbols[id.intValue()]);               
            }
            System.out.println();                        
        }
        catch(UndefinedEofSymbolException e)
        {
            throw new Error(new UndefinedEofSymbolException
                                ("The Lexer does not implement the Eof symbol " +
                                 JavaParsersym.orderedTerminalSymbols[JavaParserprs.EOFT_SYMBOL]));
        } 
    }

    public String[] orderedTerminalSymbols() { return JavaParsersym.orderedTerminalSymbols; }
    public String getTokenKindName(int kind) { return JavaParsersym.orderedTerminalSymbols[kind]; }            
    public int getEOFTokenKind() { return JavaParserprs.EOFT_SYMBOL; }
    public PrsStream getParseStream() { return (PrsStream) this; }

    public Ast parser()
    {
        return parser(null, 0);
    }
        
    public Ast parser(Monitor monitor)
    {
        return parser(monitor, 0);
    }
        
    public Ast parser(int error_repair_count)
    {
        return parser(null, error_repair_count);
    }
        
    public Ast parser(Monitor monitor, int error_repair_count)
    {
        try
        {
            dtParser = new DeterministicParser(monitor, (TokenStream)this, prs, (RuleAction)this);
        }
        catch (NotDeterministicParseTableException e)
        {
            throw new Error(new NotDeterministicParseTableException
                                ("Regenerate JavaParserprs.java with -NOBACKTRACK option"));
        }
        catch (BadParseSymFileException e)
        {
            throw new Error(new BadParseSymFileException("Bad Parser Symbol File -- JavaParsersym.java. Regenerate JavaParserprs.java"));
        }

        try
        {
            return (Ast) dtParser.parse();
        }
        catch (BadParseException e)
        {
            reset(e.error_token); // point to error token

            DiagnoseParser diagnoseParser = new DiagnoseParser(this, prs);
            diagnoseParser.diagnose(e.error_token);
        }

        return null;
    }


    public IToken getDocComment(IToken token)
    {
        int token_index = token.getTokenIndex();
        IToken[] adjuncts = getPrecedingAdjuncts(token_index);
        int i = adjuncts.length - 1;
        return (i >= 0 && adjuncts[i].getKind() == JavaParsersym.TK_DocComment
                        ? adjuncts[i]
                        : null);
    }
    
    static public abstract class Ast
    {
        protected IToken leftIToken,
                         rightIToken;
        public IToken getLeftIToken() { return leftIToken; }
        public IToken getRightIToken() { return rightIToken; }
        public IToken[] getPrecedingAdjuncts() { return leftIToken.getPrecedingAdjuncts(); }
        public IToken[] getFollowingAdjuncts() { return rightIToken.getPrecedingAdjuncts(); }
        public String toString()
        {
            PrsStream prsStream = leftIToken.getPrsStream();
            return new String(prsStream.getInputChars(),
                              leftIToken.getStartOffset(),
                              rightIToken.getEndOffset() - leftIToken.getStartOffset() + 1);
        }
        public Ast(IToken token) { this.leftIToken = this.rightIToken = token; }
        public Ast(IToken leftIToken, IToken rightIToken)
        {
            this.leftIToken = leftIToken;
            this.rightIToken = rightIToken;
        }
        void initialize() {}
        /**
     * Since the Ast type has no children, any two instances of it are equal.
     */
    public boolean equals(Object o) { return o instanceof Ast; }
        public abstract int hashCode();
        public abstract void accept(Visitor v);
        public abstract void accept(ArgumentVisitor v, Object o);
        public abstract Object accept(ResultVisitor v);
        public abstract Object accept(ResultArgumentVisitor v, Object o);
    }

    static public abstract class AstList extends Ast
    {
        private boolean leftRecursive;
        private java.util.ArrayList list;
        public int size() { return list.size(); }
        public Ast getElementAt(int i) { return (Ast) list.get(leftRecursive ? i : list.size() - 1 - i); }
        public java.util.ArrayList getArrayList()
        {
            if (! leftRecursive) // reverse the list 
            {
                for (int i = 0, n = list.size() - 1; i < n; i++, n--)
                {
                    Object ith = list.get(i),
                           nth = list.get(n);
                    list.set(i, nth);
                    list.set(n, ith);
                }
                leftRecursive = true;
            }
            return list;
        }
        public void add(Ast element)
        {
            list.add(element);
            if (leftRecursive)
                 rightIToken = element.getRightIToken();
            else leftIToken = element.getLeftIToken();
        }

        public AstList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken);
            this.leftRecursive = leftRecursive;
            list = new java.util.ArrayList();
        }

        public AstList(Ast element, boolean leftRecursive)
        {
            this(element.getLeftIToken(), element.getRightIToken(), leftRecursive);
            list.add(element);
        }
    
    public abstract boolean equals(Object o);
        public abstract int hashCode();
    }

    static public class AstToken extends Ast implements IAstToken
    {
        public AstToken(IToken token) { super(token); }
        public IToken getIToken() { return leftIToken; }
        public String toString() { return leftIToken.toString(); }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof AstToken)) return false;
            AstToken other = (AstToken    ) o;
            return toString().equals(other.toString());
        }

        public int hashCode()
        {
            int hash = 7;
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     * is always implemented by <b>AstToken</b>. It is also implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>BooleanType
     *<li>ByteType
     *<li>ShortType
     *<li>IntType
     *<li>LongType
     *<li>CharType
     *<li>FloatType
     *<li>DoubleType
     *<li>SimpleName
     *<li>EmptyDeclaration
     *<li>PublicModifier
     *<li>ProtectedModifier
     *<li>PrivateModifier
     *<li>StaticModifier
     *<li>AbstractModifier
     *<li>FinalModifier
     *<li>NativeModifier
     *<li>StrictfpModifier
     *<li>SynchronizedModifier
     *<li>TransientModifier
     *<li>VolatileModifier
     *<li>ClassDeclaration
     *<li>FieldDeclaration
     *<li>MethodDeclaration
     *<li>EmptyMethodBody
     *<li>InterfaceDeclaration
     *<li>AbstractMethodDeclaration
     *<li>Block
     *<li>EmptyStatement
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>EqualOperator
     *<li>MultiplyEqualOperator
     *<li>DivideEqualOperator
     *<li>ModEqualOperator
     *<li>PlusEqualOperator
     *<li>MinusEqualOperator
     *<li>LeftShiftEqualOperator
     *<li>RightShiftEqualOperator
     *<li>UnsignedRightShiftEqualOperator
     *<li>AndEqualOperator
     *<li>ExclusiveOrEqualOperator
     *<li>OrEqualOperator
     *<li>Commaopt
     *<li>IDENTIFIERopt
     *</ul>
     *</b>
     */
    public interface IAstToken
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>CompilationUnit</b>
     */
    public interface ICompilationUnit
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *</ul>
     *</b>
     */
    public interface ILiteral extends IAstToken, IPrimaryNoNewArray {}

    /**
     * is always implemented by <b>AstToken</b>. It is also implemented by:
     *<b>
     *<ul>
     *<li>TrueLiteral
     *<li>FalseLiteral
     *</ul>
     *</b>
     */
    public interface IBooleanLiteral extends ILiteral, IAstToken {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>BooleanType
     *<li>ByteType
     *<li>ShortType
     *<li>IntType
     *<li>LongType
     *<li>CharType
     *<li>FloatType
     *<li>DoubleType
     *<li>PrimitiveArrayType
     *<li>ClassOrInterfaceArrayType
     *<li>SimpleName
     *<li>QualifiedName
     *</ul>
     *</b>
     */
    public interface IType
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is always implemented by <b>AstToken</b>. It is also implemented by:
     *<b>
     *<ul>
     *<li>BooleanType
     *<li>ByteType
     *<li>ShortType
     *<li>IntType
     *<li>LongType
     *<li>CharType
     *<li>FloatType
     *<li>DoubleType
     *</ul>
     *</b>
     */
    public interface IPrimitiveType extends IType, IAstToken {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>PrimitiveArrayType
     *<li>ClassOrInterfaceArrayType
     *<li>SimpleName
     *<li>QualifiedName
     *</ul>
     *</b>
     */
    public interface IReferenceType extends IType {}

    /**
     * is always implemented by <b>AstToken</b>. It is also implemented by:
     *<b>
     *<ul>
     *<li>ByteType
     *<li>ShortType
     *<li>IntType
     *<li>LongType
     *<li>CharType
     *<li>FloatType
     *<li>DoubleType
     *</ul>
     *</b>
     */
    public interface INumericType extends IPrimitiveType {}

    /**
     * is always implemented by <b>AstToken</b>. It is also implemented by:
     *<b>
     *<ul>
     *<li>ByteType
     *<li>ShortType
     *<li>IntType
     *<li>LongType
     *<li>CharType
     *</ul>
     *</b>
     */
    public interface IIntegralType extends INumericType, IAstToken {}

    /**
     * is always implemented by <b>AstToken</b>. It is also implemented by:
     *<b>
     *<ul>
     *<li>FloatType
     *<li>DoubleType
     *</ul>
     *</b>
     */
    public interface IFloatingPointType extends INumericType, IAstToken {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>SimpleName
     *<li>QualifiedName
     *</ul>
     *</b>
     */
    public interface IClassOrInterfaceType extends IReferenceType, IClassType, IInterfaceType {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>PrimitiveArrayType
     *<li>ClassOrInterfaceArrayType
     *</ul>
     *</b>
     */
    public interface IArrayType extends IReferenceType {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>SimpleName
     *<li>QualifiedName
     *</ul>
     *</b>
     */
    public interface IName extends IClassOrInterfaceType, IPostfixExpression, ILeftHandSide {}

    /**
     * is implemented by <b>DimList</b>
     */
    public interface IDims
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>SimpleName
     *<li>QualifiedName
     *</ul>
     *</b>
     */
    public interface IClassType
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>SimpleName
     *<li>QualifiedName
     *</ul>
     *</b>
     */
    public interface IInterfaceType
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is always implemented by <b>AstToken</b>. It is also implemented by <b>SimpleName</b>
     */
    public interface ISimpleName extends IName, IAstToken {}

    /**
     * is implemented by <b>QualifiedName</b>
     */
    public interface IQualifiedName extends IName {}

    /**
     * is implemented by <b>PackageDeclaration</b>
     */
    public interface IPackageDeclarationopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>ImportDeclarationList</b>
     */
    public interface IImportDeclarationsopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>TypeDeclarationList</b>
     */
    public interface ITypeDeclarationsopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>ImportDeclarationList</b>
     */
    public interface IImportDeclarations
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>SingleTypeImportDeclaration
     *<li>TypeImportOnDemandDeclaration
     *</ul>
     *</b>
     */
    public interface IImportDeclaration
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>TypeDeclarationList</b>
     */
    public interface ITypeDeclarations
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>EmptyDeclaration
     *<li>ClassDeclaration
     *<li>InterfaceDeclaration
     *</ul>
     *</b>
     */
    public interface ITypeDeclaration extends IAstToken {}

    /**
     * is implemented by <b>PackageDeclaration</b>
     */
    public interface IPackageDeclaration extends IPackageDeclarationopt {}

    /**
     * is implemented by <b>SingleTypeImportDeclaration</b>
     */
    public interface ISingleTypeImportDeclaration extends IImportDeclaration {}

    /**
     * is implemented by <b>TypeImportOnDemandDeclaration</b>
     */
    public interface ITypeImportOnDemandDeclaration extends IImportDeclaration {}

    /**
     * is implemented by <b>ClassDeclaration</b>
     */
    public interface IClassDeclaration extends ITypeDeclaration, IClassMemberDeclaration, IInterfaceMemberDeclaration, IBlockStatement {}

    /**
     * is implemented by <b>InterfaceDeclaration</b>
     */
    public interface IInterfaceDeclaration extends ITypeDeclaration, IClassMemberDeclaration, IInterfaceMemberDeclaration {}

    /**
     * is implemented by <b>ModifierList</b>
     */
    public interface IModifiers
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is always implemented by <b>AstToken</b>. It is also implemented by:
     *<b>
     *<ul>
     *<li>PublicModifier
     *<li>ProtectedModifier
     *<li>PrivateModifier
     *<li>StaticModifier
     *<li>AbstractModifier
     *<li>FinalModifier
     *<li>NativeModifier
     *<li>StrictfpModifier
     *<li>SynchronizedModifier
     *<li>TransientModifier
     *<li>VolatileModifier
     *</ul>
     *</b>
     */
    public interface IModifier extends IAstToken {}

    /**
     * is implemented by <b>ModifierList</b>
     */
    public interface IModifiersopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>Super</b>
     */
    public interface ISuperopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>InterfaceTypeList</b>
     */
    public interface IInterfacesopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>ClassBody</b>
     */
    public interface IClassBody extends IClassBodyopt {}

    /**
     * is implemented by <b>Super</b>
     */
    public interface ISuper extends ISuperopt {}

    /**
     * is implemented by <b>InterfaceTypeList</b>
     */
    public interface IInterfaces
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>InterfaceTypeList</b>
     */
    public interface IInterfaceTypeList
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>ClassBodyDeclarationList</b>
     */
    public interface IClassBodyDeclarationsopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>ClassBodyDeclarationList</b>
     */
    public interface IClassBodyDeclarations
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>EmptyDeclaration
     *<li>ClassDeclaration
     *<li>FieldDeclaration
     *<li>MethodDeclaration
     *<li>StaticInitializer
     *<li>ConstructorDeclaration
     *<li>InterfaceDeclaration
     *<li>Block
     *</ul>
     *</b>
     */
    public interface IClassBodyDeclaration
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>EmptyDeclaration
     *<li>ClassDeclaration
     *<li>FieldDeclaration
     *<li>MethodDeclaration
     *<li>InterfaceDeclaration
     *</ul>
     *</b>
     */
    public interface IClassMemberDeclaration extends IClassBodyDeclaration, IAstToken {}

    /**
     * is implemented by <b>StaticInitializer</b>
     */
    public interface IStaticInitializer extends IClassBodyDeclaration {}

    /**
     * is implemented by <b>ConstructorDeclaration</b>
     */
    public interface IConstructorDeclaration extends IClassBodyDeclaration {}

    /**
     * is implemented by <b>Block</b>
     */
    public interface IBlock extends IClassBodyDeclaration, IMethodBody, IConstructorBody, IStatementWithoutTrailingSubstatement {}

    /**
     * is implemented by <b>FieldDeclaration</b>
     */
    public interface IFieldDeclaration extends IClassMemberDeclaration, IConstantDeclaration {}

    /**
     * is implemented by <b>MethodDeclaration</b>
     */
    public interface IMethodDeclaration extends IClassMemberDeclaration {}

    /**
     * is implemented by <b>VariableDeclaratorList</b>
     */
    public interface IVariableDeclarators
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>VariableDeclarator
     *<li>VariableDeclaratorId
     *</ul>
     *</b>
     */
    public interface IVariableDeclarator
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>VariableDeclaratorId</b>
     */
    public interface IVariableDeclaratorId extends IVariableDeclarator {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ArrayInitializer
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *<li>EqualExpression
     *<li>NotEqualExpression
     *<li>AndExpression
     *<li>ExclusiveOrExpression
     *<li>InclusiveOrExpression
     *<li>ConditionalAndExpression
     *<li>ConditionalOrExpression
     *<li>ConditionalExpression
     *<li>Assignment
     *</ul>
     *</b>
     */
    public interface IVariableInitializer
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>DimList</b>
     */
    public interface IDimsopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *<li>EqualExpression
     *<li>NotEqualExpression
     *<li>AndExpression
     *<li>ExclusiveOrExpression
     *<li>InclusiveOrExpression
     *<li>ConditionalAndExpression
     *<li>ConditionalOrExpression
     *<li>ConditionalExpression
     *<li>Assignment
     *</ul>
     *</b>
     */
    public interface IExpression extends IVariableInitializer, IConstantExpression, IExpressionopt {}

    /**
     * is implemented by <b>ArrayInitializer</b>
     */
    public interface IArrayInitializer extends IVariableInitializer {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>TypedMethodHeader
     *<li>VoidMethodHeader
     *</ul>
     *</b>
     */
    public interface IMethodHeader
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>EmptyMethodBody
     *<li>Block
     *</ul>
     *</b>
     */
    public interface IMethodBody extends IAstToken {}

    /**
     * is implemented by <b>MethodDeclarator</b>
     */
    public interface IMethodDeclarator
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>ClassTypeList</b>
     */
    public interface IThrowsopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>FormalParameterList</b>
     */
    public interface IFormalParameterListopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>FormalParameterList</b>
     */
    public interface IFormalParameterList
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>FormalParameter</b>
     */
    public interface IFormalParameter
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>ClassTypeList</b>
     */
    public interface IThrows
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>ClassTypeList</b>
     */
    public interface IClassTypeList
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>ConstructorDeclarator</b>
     */
    public interface IConstructorDeclarator
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>ConstructorBody
     *<li>Block
     *</ul>
     *</b>
     */
    public interface IConstructorBody
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>ThisCall
     *<li>SuperCall
     *</ul>
     *</b>
     */
    public interface IExplicitConstructorInvocation extends IExplicitConstructorInvocationopt {}

    /**
     * is implemented by <b>BlockStatementList</b>
     */
    public interface IBlockStatementsopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>ExpressionList</b>
     */
    public interface IArgumentListopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *</ul>
     *</b>
     */
    public interface IPrimary extends IPostfixExpression {}

    /**
     * is implemented by <b>InterfaceTypeList</b>
     */
    public interface IExtendsInterfacesopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>InterfaceBody</b>
     */
    public interface IInterfaceBody
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>InterfaceTypeList</b>
     */
    public interface IExtendsInterfaces
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>InterfaceMemberDeclarationList</b>
     */
    public interface IInterfaceMemberDeclarationsopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>InterfaceMemberDeclarationList</b>
     */
    public interface IInterfaceMemberDeclarations
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>EmptyDeclaration
     *<li>ClassDeclaration
     *<li>FieldDeclaration
     *<li>InterfaceDeclaration
     *<li>AbstractMethodDeclaration
     *</ul>
     *</b>
     */
    public interface IInterfaceMemberDeclaration extends IAstToken {}

    /**
     * is implemented by <b>FieldDeclaration</b>
     */
    public interface IConstantDeclaration extends IInterfaceMemberDeclaration {}

    /**
     * is implemented by <b>AbstractMethodDeclaration</b>
     */
    public interface IAbstractMethodDeclaration extends IInterfaceMemberDeclaration {}

    /**
     * is implemented by <b>VariableInitializerList</b>
     */
    public interface IVariableInitializersopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is always implemented by <b>AstToken</b>. It is also implemented by <b>Commaopt</b>
     */
    public interface ICommaopt extends IAstToken {}

    /**
     * is implemented by <b>VariableInitializerList</b>
     */
    public interface IVariableInitializers
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>BlockStatementList</b>
     */
    public interface IBlockStatements
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>ClassDeclaration
     *<li>Block
     *<li>LocalVariableDeclarationStatement
     *<li>EmptyStatement
     *<li>LabeledStatement
     *<li>ExpressionStatement
     *<li>IfStatement
     *<li>SwitchStatement
     *<li>WhileStatement
     *<li>DoStatement
     *<li>ForStatement
     *<li>BreakStatement
     *<li>ContinueStatement
     *<li>ReturnStatement
     *<li>ThrowStatement
     *<li>SynchronizedStatement
     *<li>TryStatement
     *</ul>
     *</b>
     */
    public interface IBlockStatement
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>LocalVariableDeclarationStatement</b>
     */
    public interface ILocalVariableDeclarationStatement extends IBlockStatement {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>Block
     *<li>EmptyStatement
     *<li>LabeledStatement
     *<li>ExpressionStatement
     *<li>IfStatement
     *<li>SwitchStatement
     *<li>WhileStatement
     *<li>DoStatement
     *<li>ForStatement
     *<li>BreakStatement
     *<li>ContinueStatement
     *<li>ReturnStatement
     *<li>ThrowStatement
     *<li>SynchronizedStatement
     *<li>TryStatement
     *</ul>
     *</b>
     */
    public interface IStatement extends IBlockStatement {}

    /**
     * is implemented by <b>LocalVariableDeclaration</b>
     */
    public interface ILocalVariableDeclaration extends IForInit {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>Block
     *<li>EmptyStatement
     *<li>ExpressionStatement
     *<li>SwitchStatement
     *<li>DoStatement
     *<li>BreakStatement
     *<li>ContinueStatement
     *<li>ReturnStatement
     *<li>ThrowStatement
     *<li>SynchronizedStatement
     *<li>TryStatement
     *</ul>
     *</b>
     */
    public interface IStatementWithoutTrailingSubstatement extends IStatement, IStatementNoShortIf {}

    /**
     * is implemented by <b>LabeledStatement</b>
     */
    public interface ILabeledStatement extends IStatement {}

    /**
     * is implemented by <b>IfStatement</b>
     */
    public interface IIfThenStatement extends IStatement {}

    /**
     * is implemented by <b>IfStatement</b>
     */
    public interface IIfThenElseStatement extends IStatement {}

    /**
     * is implemented by <b>WhileStatement</b>
     */
    public interface IWhileStatement extends IStatement {}

    /**
     * is implemented by <b>ForStatement</b>
     */
    public interface IForStatement extends IStatement {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>Block
     *<li>EmptyStatement
     *<li>LabeledStatement
     *<li>ExpressionStatement
     *<li>IfStatement
     *<li>SwitchStatement
     *<li>WhileStatement
     *<li>DoStatement
     *<li>ForStatement
     *<li>BreakStatement
     *<li>ContinueStatement
     *<li>ReturnStatement
     *<li>ThrowStatement
     *<li>SynchronizedStatement
     *<li>TryStatement
     *</ul>
     *</b>
     */
    public interface IStatementNoShortIf
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>LabeledStatement</b>
     */
    public interface ILabeledStatementNoShortIf extends IStatementNoShortIf {}

    /**
     * is implemented by <b>IfStatement</b>
     */
    public interface IIfThenElseStatementNoShortIf extends IStatementNoShortIf {}

    /**
     * is implemented by <b>WhileStatement</b>
     */
    public interface IWhileStatementNoShortIf extends IStatementNoShortIf {}

    /**
     * is implemented by <b>ForStatement</b>
     */
    public interface IForStatementNoShortIf extends IStatementNoShortIf {}

    /**
     * is always implemented by <b>AstToken</b>. It is also implemented by <b>EmptyStatement</b>
     */
    public interface IEmptyStatement extends IStatementWithoutTrailingSubstatement, IAstToken {}

    /**
     * is implemented by <b>ExpressionStatement</b>
     */
    public interface IExpressionStatement extends IStatementWithoutTrailingSubstatement {}

    /**
     * is implemented by <b>SwitchStatement</b>
     */
    public interface ISwitchStatement extends IStatementWithoutTrailingSubstatement {}

    /**
     * is implemented by <b>DoStatement</b>
     */
    public interface IDoStatement extends IStatementWithoutTrailingSubstatement {}

    /**
     * is implemented by <b>BreakStatement</b>
     */
    public interface IBreakStatement extends IStatementWithoutTrailingSubstatement {}

    /**
     * is implemented by <b>ContinueStatement</b>
     */
    public interface IContinueStatement extends IStatementWithoutTrailingSubstatement {}

    /**
     * is implemented by <b>ReturnStatement</b>
     */
    public interface IReturnStatement extends IStatementWithoutTrailingSubstatement {}

    /**
     * is implemented by <b>SynchronizedStatement</b>
     */
    public interface ISynchronizedStatement extends IStatementWithoutTrailingSubstatement {}

    /**
     * is implemented by <b>ThrowStatement</b>
     */
    public interface IThrowStatement extends IStatementWithoutTrailingSubstatement {}

    /**
     * is implemented by <b>TryStatement</b>
     */
    public interface ITryStatement extends IStatementWithoutTrailingSubstatement {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>ClassInstanceCreationExpression
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>Assignment
     *</ul>
     *</b>
     */
    public interface IStatementExpression
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>Assignment</b>
     */
    public interface IAssignment extends IStatementExpression, IAssignmentExpression {}

    /**
     * is implemented by <b>PreIncrementExpression</b>
     */
    public interface IPreIncrementExpression extends IStatementExpression, IUnaryExpression {}

    /**
     * is implemented by <b>PreDecrementExpression</b>
     */
    public interface IPreDecrementExpression extends IStatementExpression, IUnaryExpression {}

    /**
     * is implemented by <b>PostIncrementExpression</b>
     */
    public interface IPostIncrementExpression extends IStatementExpression, IPostfixExpression {}

    /**
     * is implemented by <b>PostDecrementExpression</b>
     */
    public interface IPostDecrementExpression extends IStatementExpression, IPostfixExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *</ul>
     *</b>
     */
    public interface IMethodInvocation extends IStatementExpression, IPrimaryNoNewArray {}

    /**
     * is implemented by <b>ClassInstanceCreationExpression</b>
     */
    public interface IClassInstanceCreationExpression extends IStatementExpression, IPrimaryNoNewArray {}

    /**
     * is implemented by <b>SwitchBlock</b>
     */
    public interface ISwitchBlock
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>SwitchLabelList</b>
     */
    public interface ISwitchLabelsopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>SwitchBlockStatementList</b>
     */
    public interface ISwitchBlockStatements
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>SwitchBlockStatement</b>
     */
    public interface ISwitchBlockStatement
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>SwitchLabelList</b>
     */
    public interface ISwitchLabels
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>CaseLabel
     *<li>DefaultLabel
     *</ul>
     *</b>
     */
    public interface ISwitchLabel
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *<li>EqualExpression
     *<li>NotEqualExpression
     *<li>AndExpression
     *<li>ExclusiveOrExpression
     *<li>InclusiveOrExpression
     *<li>ConditionalAndExpression
     *<li>ConditionalOrExpression
     *<li>ConditionalExpression
     *<li>Assignment
     *</ul>
     *</b>
     */
    public interface IConstantExpression
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>LocalVariableDeclaration
     *<li>StatementExpressionList
     *</ul>
     *</b>
     */
    public interface IForInitopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *<li>EqualExpression
     *<li>NotEqualExpression
     *<li>AndExpression
     *<li>ExclusiveOrExpression
     *<li>InclusiveOrExpression
     *<li>ConditionalAndExpression
     *<li>ConditionalOrExpression
     *<li>ConditionalExpression
     *<li>Assignment
     *</ul>
     *</b>
     */
    public interface IExpressionopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>StatementExpressionList</b>
     */
    public interface IForUpdateopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>LocalVariableDeclaration
     *<li>StatementExpressionList
     *</ul>
     *</b>
     */
    public interface IForInit extends IForInitopt {}

    /**
     * is implemented by <b>StatementExpressionList</b>
     */
    public interface IStatementExpressionList extends IForInit {}

    /**
     * is implemented by <b>StatementExpressionList</b>
     */
    public interface IForUpdate
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is always implemented by <b>AstToken</b>. It is also implemented by <b>IDENTIFIERopt</b>
     */
    public interface IIDENTIFIERopt extends IAstToken {}

    /**
     * is implemented by <b>CatchClauseList</b>
     */
    public interface ICatches
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>CatchClauseList</b>
     */
    public interface ICatchesopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>Finally</b>
     */
    public interface IFinally
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>CatchClause</b>
     */
    public interface ICatchClause
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *</ul>
     *</b>
     */
    public interface IPrimaryNoNewArray extends IPrimary, IAstToken {}

    /**
     * is implemented by <b>ArrayCreationExpression</b>
     */
    public interface IArrayCreationExpression extends IPrimary {}

    /**
     * is implemented by <b>ArrayAccess</b>
     */
    public interface IArrayAccess extends IPrimaryNoNewArray, ILeftHandSide {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *</ul>
     *</b>
     */
    public interface IFieldAccess extends IPrimaryNoNewArray, ILeftHandSide {}

    /**
     * is implemented by <b>ClassBody</b>
     */
    public interface IClassBodyopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>ExpressionList</b>
     */
    public interface IArgumentList
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>DimExprList</b>
     */
    public interface IDimExprs
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>DimExpr</b>
     */
    public interface IDimExpr
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>Dim</b>
     */
    public interface IDim
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *</ul>
     *</b>
     */
    public interface IPostfixExpression extends IUnaryExpressionNotPlusMinus {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *</ul>
     *</b>
     */
    public interface IUnaryExpression extends IMultiplicativeExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *</ul>
     *</b>
     */
    public interface IUnaryExpressionNotPlusMinus extends IUnaryExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *</ul>
     *</b>
     */
    public interface ICastExpression extends IUnaryExpressionNotPlusMinus {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *</ul>
     *</b>
     */
    public interface IMultiplicativeExpression extends IAdditiveExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *</ul>
     *</b>
     */
    public interface IAdditiveExpression extends IShiftExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *</ul>
     *</b>
     */
    public interface IShiftExpression extends IRelationalExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *</ul>
     *</b>
     */
    public interface IRelationalExpression extends IEqualityExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *<li>EqualExpression
     *<li>NotEqualExpression
     *</ul>
     *</b>
     */
    public interface IEqualityExpression extends IAndExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *<li>EqualExpression
     *<li>NotEqualExpression
     *<li>AndExpression
     *</ul>
     *</b>
     */
    public interface IAndExpression extends IExclusiveOrExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *<li>EqualExpression
     *<li>NotEqualExpression
     *<li>AndExpression
     *<li>ExclusiveOrExpression
     *</ul>
     *</b>
     */
    public interface IExclusiveOrExpression extends IInclusiveOrExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *<li>EqualExpression
     *<li>NotEqualExpression
     *<li>AndExpression
     *<li>ExclusiveOrExpression
     *<li>InclusiveOrExpression
     *</ul>
     *</b>
     */
    public interface IInclusiveOrExpression extends IConditionalAndExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *<li>EqualExpression
     *<li>NotEqualExpression
     *<li>AndExpression
     *<li>ExclusiveOrExpression
     *<li>InclusiveOrExpression
     *<li>ConditionalAndExpression
     *</ul>
     *</b>
     */
    public interface IConditionalAndExpression extends IConditionalOrExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *<li>EqualExpression
     *<li>NotEqualExpression
     *<li>AndExpression
     *<li>ExclusiveOrExpression
     *<li>InclusiveOrExpression
     *<li>ConditionalAndExpression
     *<li>ConditionalOrExpression
     *</ul>
     *</b>
     */
    public interface IConditionalOrExpression extends IConditionalExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *<li>EqualExpression
     *<li>NotEqualExpression
     *<li>AndExpression
     *<li>ExclusiveOrExpression
     *<li>InclusiveOrExpression
     *<li>ConditionalAndExpression
     *<li>ConditionalOrExpression
     *<li>ConditionalExpression
     *</ul>
     *</b>
     */
    public interface IConditionalExpression extends IAssignmentExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>IntegerLiteral
     *<li>LongLiteral
     *<li>FloatLiteral
     *<li>DoubleLiteral
     *<li>BooleanLiteral
     *<li>CharacterLiteral
     *<li>StringLiteral
     *<li>NullLiteral
     *<li>TrueLiteral
     *<li>FalseLiteral
     *<li>SimpleName
     *<li>QualifiedName
     *<li>ParenthesizedExpression
     *<li>PrimaryThis
     *<li>PrimaryClassLiteral
     *<li>PrimaryVoidClassLiteral
     *<li>ClassInstanceCreationExpression
     *<li>ArrayCreationExpression
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>MethodInvocation
     *<li>PrimaryMethodInvocation
     *<li>SuperMethodInvocation
     *<li>ArrayAccess
     *<li>PostIncrementExpression
     *<li>PostDecrementExpression
     *<li>PlusUnaryExpression
     *<li>MinusUnaryExpression
     *<li>PreIncrementExpression
     *<li>PreDecrementExpression
     *<li>UnaryComplementExpression
     *<li>UnaryNotExpression
     *<li>PrimitiveCastExpression
     *<li>ClassCastExpression
     *<li>MultiplyExpression
     *<li>DivideExpression
     *<li>ModExpression
     *<li>AddExpression
     *<li>SubtractExpression
     *<li>LeftShiftExpression
     *<li>RightShiftExpression
     *<li>UnsignedRightShiftExpression
     *<li>LessExpression
     *<li>GreaterExpression
     *<li>LessEqualExpression
     *<li>GreaterEqualExpression
     *<li>InstanceofExpression
     *<li>EqualExpression
     *<li>NotEqualExpression
     *<li>AndExpression
     *<li>ExclusiveOrExpression
     *<li>InclusiveOrExpression
     *<li>ConditionalAndExpression
     *<li>ConditionalOrExpression
     *<li>ConditionalExpression
     *<li>Assignment
     *</ul>
     *</b>
     */
    public interface IAssignmentExpression extends IExpression {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>SimpleName
     *<li>QualifiedName
     *<li>FieldAccess
     *<li>SuperFieldAccess
     *<li>ArrayAccess
     *</ul>
     *</b>
     */
    public interface ILeftHandSide
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is always implemented by <b>AstToken</b>. It is also implemented by:
     *<b>
     *<ul>
     *<li>EqualOperator
     *<li>MultiplyEqualOperator
     *<li>DivideEqualOperator
     *<li>ModEqualOperator
     *<li>PlusEqualOperator
     *<li>MinusEqualOperator
     *<li>LeftShiftEqualOperator
     *<li>RightShiftEqualOperator
     *<li>UnsignedRightShiftEqualOperator
     *<li>AndEqualOperator
     *<li>ExclusiveOrEqualOperator
     *<li>OrEqualOperator
     *</ul>
     *</b>
     */
    public interface IAssignmentOperator extends IAstToken {}

    /**
     * is implemented by:
     *<b>
     *<ul>
     *<li>ThisCall
     *<li>SuperCall
     *</ul>
     *</b>
     */
    public interface IExplicitConstructorInvocationopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     * is implemented by <b>SwitchBlockStatementList</b>
     */
    public interface ISwitchBlockStatementsopt
    {
        public IToken getLeftIToken();
        public IToken getRightIToken();

        void accept(Visitor v);
        void accept(ArgumentVisitor v, Object o);
        Object accept(ResultVisitor v);
        Object accept(ResultArgumentVisitor v, Object o);
    }

    /**
     *<b>
     *<li>Rule 1:  Literal ::= IntegerLiteral
     *</b>
     */
    static public class IntegerLiteral extends AstToken implements ILiteral
    {
        public IntegerLiteral(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 2:  Literal ::= LongLiteral
     *</b>
     */
    static public class LongLiteral extends AstToken implements ILiteral
    {
        public LongLiteral(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 3:  Literal ::= FloatingPointLiteral
     *</b>
     */
    static public class FloatLiteral extends AstToken implements ILiteral
    {
        public FloatLiteral(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 4:  Literal ::= DoubleLiteral
     *</b>
     */
    static public class DoubleLiteral extends AstToken implements ILiteral
    {
        public DoubleLiteral(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 5:  Literal ::= BooleanLiteral
     *</b>
     */
    static public class BooleanLiteral extends Ast implements ILiteral
    {
        private IBooleanLiteral _BooleanLiteral;

        public IBooleanLiteral getBooleanLiteral() { return _BooleanLiteral; }

        public BooleanLiteral(IToken leftIToken, IToken rightIToken,
                              IBooleanLiteral _BooleanLiteral)
        {
            super(leftIToken, rightIToken);

            this._BooleanLiteral = _BooleanLiteral;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof BooleanLiteral)) return false;
            BooleanLiteral other = (BooleanLiteral    ) o;
            if (! _BooleanLiteral.equals(other.getBooleanLiteral())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getBooleanLiteral().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 6:  Literal ::= CharacterLiteral
     *</b>
     */
    static public class CharacterLiteral extends AstToken implements ILiteral
    {
        public CharacterLiteral(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 7:  Literal ::= StringLiteral
     *</b>
     */
    static public class StringLiteral extends AstToken implements ILiteral
    {
        public StringLiteral(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 8:  Literal ::= null$null_literal
     *</b>
     */
    static public class NullLiteral extends AstToken implements ILiteral
    {
        public IToken getnull_literal() { return leftIToken; }

        public NullLiteral(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 9:  BooleanLiteral ::= true
     *</b>
     */
    static public class TrueLiteral extends AstToken implements IBooleanLiteral
    {
        public TrueLiteral(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 10:  BooleanLiteral ::= false
     *</b>
     */
    static public class FalseLiteral extends AstToken implements IBooleanLiteral
    {
        public FalseLiteral(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 14:  PrimitiveType ::= boolean
     *</b>
     */
    static public class BooleanType extends AstToken implements IPrimitiveType
    {
        public BooleanType(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 17:  IntegralType ::= byte
     *</b>
     */
    static public class ByteType extends AstToken implements IIntegralType
    {
        public ByteType(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 18:  IntegralType ::= short
     *</b>
     */
    static public class ShortType extends AstToken implements IIntegralType
    {
        public ShortType(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 19:  IntegralType ::= int
     *</b>
     */
    static public class IntType extends AstToken implements IIntegralType
    {
        public IntType(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 20:  IntegralType ::= long
     *</b>
     */
    static public class LongType extends AstToken implements IIntegralType
    {
        public LongType(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 21:  IntegralType ::= char
     *</b>
     */
    static public class CharType extends AstToken implements IIntegralType
    {
        public CharType(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 22:  FloatingPointType ::= float
     *</b>
     */
    static public class FloatType extends AstToken implements IFloatingPointType
    {
        public FloatType(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 23:  FloatingPointType ::= double
     *</b>
     */
    static public class DoubleType extends AstToken implements IFloatingPointType
    {
        public DoubleType(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 27:  ArrayType ::= PrimitiveType Dims
     *</b>
     */
    static public class PrimitiveArrayType extends Ast implements IArrayType
    {
        private IPrimitiveType _PrimitiveType;
        private DimList _Dims;

        public IPrimitiveType getPrimitiveType() { return _PrimitiveType; }
        public DimList getDims() { return _Dims; }

        public PrimitiveArrayType(IToken leftIToken, IToken rightIToken,
                                  IPrimitiveType _PrimitiveType,
                                  DimList _Dims)
        {
            super(leftIToken, rightIToken);

            this._PrimitiveType = _PrimitiveType;
            this._Dims = _Dims;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof PrimitiveArrayType)) return false;
            PrimitiveArrayType other = (PrimitiveArrayType    ) o;
            if (! _PrimitiveType.equals(other.getPrimitiveType())) return false;
            if (! _Dims.equals(other.getDims())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getPrimitiveType().hashCode());
            hash = hash * 31 + (getDims().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 28:  ArrayType ::= Name Dims
     *</b>
     */
    static public class ClassOrInterfaceArrayType extends Ast implements IArrayType
    {
        private IName _Name;
        private DimList _Dims;

        public IName getName() { return _Name; }
        public DimList getDims() { return _Dims; }

        public ClassOrInterfaceArrayType(IToken leftIToken, IToken rightIToken,
                                         IName _Name,
                                         DimList _Dims)
        {
            super(leftIToken, rightIToken);

            this._Name = _Name;
            this._Dims = _Dims;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ClassOrInterfaceArrayType)) return false;
            ClassOrInterfaceArrayType other = (ClassOrInterfaceArrayType    ) o;
            if (! _Name.equals(other.getName())) return false;
            if (! _Dims.equals(other.getDims())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getName().hashCode());
            hash = hash * 31 + (getDims().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 33:  SimpleName ::= IDENTIFIER
     *</b>
     */
    static public class SimpleName extends AstToken implements ISimpleName
    {
        public SimpleName(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 34:  QualifiedName ::= Name . IDENTIFIER
     *</b>
     */
    static public class QualifiedName extends Ast implements IQualifiedName
    {
        private IName _Name;

        public IName getName() { return _Name; }

        public QualifiedName(IToken leftIToken, IToken rightIToken,
                             IName _Name)
        {
            super(leftIToken, rightIToken);

            this._Name = _Name;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof QualifiedName)) return false;
            QualifiedName other = (QualifiedName    ) o;
            if (! _Name.equals(other.getName())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getName().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 35:  CompilationUnit ::= PackageDeclarationopt ImportDeclarationsopt TypeDeclarationsopt
     *</b>
     */
    static public class CompilationUnit extends Ast implements ICompilationUnit
    {
        private PackageDeclaration _PackageDeclarationopt;
        private ImportDeclarationList _ImportDeclarationsopt;
        private TypeDeclarationList _TypeDeclarationsopt;

        /**
         * The value returned by <b>getPackageDeclarationopt</b> may be <b>null</b>
         */
        public PackageDeclaration getPackageDeclarationopt() { return _PackageDeclarationopt; }
        public ImportDeclarationList getImportDeclarationsopt() { return _ImportDeclarationsopt; }
        public TypeDeclarationList getTypeDeclarationsopt() { return _TypeDeclarationsopt; }

        public CompilationUnit(IToken leftIToken, IToken rightIToken,
                               PackageDeclaration _PackageDeclarationopt,
                               ImportDeclarationList _ImportDeclarationsopt,
                               TypeDeclarationList _TypeDeclarationsopt)
        {
            super(leftIToken, rightIToken);

            this._PackageDeclarationopt = _PackageDeclarationopt;
            this._ImportDeclarationsopt = _ImportDeclarationsopt;
            this._TypeDeclarationsopt = _TypeDeclarationsopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof CompilationUnit)) return false;
            CompilationUnit other = (CompilationUnit    ) o;
            if (_PackageDeclarationopt == null && other.getPackageDeclarationopt() != null) return false;
            else if (! _PackageDeclarationopt.equals(other.getPackageDeclarationopt())) return false;
            if (! _ImportDeclarationsopt.equals(other.getImportDeclarationsopt())) return false;
            if (! _TypeDeclarationsopt.equals(other.getTypeDeclarationsopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getPackageDeclarationopt() == null ? 0 : getPackageDeclarationopt().hashCode());
            hash = hash * 31 + (getImportDeclarationsopt().hashCode());
            hash = hash * 31 + (getTypeDeclarationsopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 36:  ImportDeclarations ::= ImportDeclaration
     *<li>Rule 37:  ImportDeclarations ::= ImportDeclarations ImportDeclaration
     *<li>Rule 314:  ImportDeclarationsopt ::= $Empty
     *<li>Rule 315:  ImportDeclarationsopt ::= ImportDeclarations
     *</b>
     */
    static public class ImportDeclarationList extends AstList implements IImportDeclarations, IImportDeclarationsopt
    {
        public IImportDeclaration getImportDeclarationAt(int i) { return (IImportDeclaration) getElementAt(i); }

        public ImportDeclarationList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public ImportDeclarationList(IImportDeclaration _ImportDeclaration, boolean leftRecursive)
        {
            super((Ast) _ImportDeclaration, leftRecursive);
            initialize();
        }

        public void add(IImportDeclaration _ImportDeclaration)
        {
            super.add((Ast) _ImportDeclaration);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof ImportDeclarationList)) return false;
            ImportDeclarationList other = (ImportDeclarationList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                IImportDeclaration element = getImportDeclarationAt(i);
                    if (! element.equals(other.getImportDeclarationAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getImportDeclarationAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getImportDeclarationAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getImportDeclarationAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getImportDeclarationAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getImportDeclarationAt(i).accept(v, o); return null; }
    }

    /**
     *<b>
     *<li>Rule 38:  TypeDeclarations ::= TypeDeclaration
     *<li>Rule 39:  TypeDeclarations ::= TypeDeclarations TypeDeclaration
     *<li>Rule 316:  TypeDeclarationsopt ::= $Empty
     *<li>Rule 317:  TypeDeclarationsopt ::= TypeDeclarations
     *</b>
     */
    static public class TypeDeclarationList extends AstList implements ITypeDeclarations, ITypeDeclarationsopt
    {
        public ITypeDeclaration getTypeDeclarationAt(int i) { return (ITypeDeclaration) getElementAt(i); }

        public TypeDeclarationList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public TypeDeclarationList(ITypeDeclaration _TypeDeclaration, boolean leftRecursive)
        {
            super((Ast) _TypeDeclaration, leftRecursive);
            initialize();
        }

        public void add(ITypeDeclaration _TypeDeclaration)
        {
            super.add((Ast) _TypeDeclaration);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof TypeDeclarationList)) return false;
            TypeDeclarationList other = (TypeDeclarationList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                ITypeDeclaration element = getTypeDeclarationAt(i);
                    if (! element.equals(other.getTypeDeclarationAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getTypeDeclarationAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getTypeDeclarationAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getTypeDeclarationAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getTypeDeclarationAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getTypeDeclarationAt(i).accept(v, o); return null; }
    }

    /**
     *<b>
     *<li>Rule 40:  PackageDeclaration ::= package Name ;
     *</b>
     */
    static public class PackageDeclaration extends Ast implements IPackageDeclaration
    {
        private IName _Name;

        public IName getName() { return _Name; }

        public PackageDeclaration(IToken leftIToken, IToken rightIToken,
                                  IName _Name)
        {
            super(leftIToken, rightIToken);

            this._Name = _Name;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof PackageDeclaration)) return false;
            PackageDeclaration other = (PackageDeclaration    ) o;
            if (! _Name.equals(other.getName())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getName().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 43:  SingleTypeImportDeclaration ::= import Name ;
     *</b>
     */
    static public class SingleTypeImportDeclaration extends Ast implements ISingleTypeImportDeclaration
    {
        private IName _Name;

        public IName getName() { return _Name; }

        public SingleTypeImportDeclaration(IToken leftIToken, IToken rightIToken,
                                           IName _Name)
        {
            super(leftIToken, rightIToken);

            this._Name = _Name;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof SingleTypeImportDeclaration)) return false;
            SingleTypeImportDeclaration other = (SingleTypeImportDeclaration    ) o;
            if (! _Name.equals(other.getName())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getName().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 44:  TypeImportOnDemandDeclaration ::= import Name . * ;
     *</b>
     */
    static public class TypeImportOnDemandDeclaration extends Ast implements ITypeImportOnDemandDeclaration
    {
        private IName _Name;

        public IName getName() { return _Name; }

        public TypeImportOnDemandDeclaration(IToken leftIToken, IToken rightIToken,
                                             IName _Name)
        {
            super(leftIToken, rightIToken);

            this._Name = _Name;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof TypeImportOnDemandDeclaration)) return false;
            TypeImportOnDemandDeclaration other = (TypeImportOnDemandDeclaration    ) o;
            if (! _Name.equals(other.getName())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getName().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 47:  TypeDeclaration ::= ;
     *<li>Rule 77:  ClassMemberDeclaration ::= ;
     *<li>Rule 117:  InterfaceMemberDeclaration ::= ;
     *</b>
     */
    static public class EmptyDeclaration extends AstToken implements ITypeDeclaration, IClassMemberDeclaration, IInterfaceMemberDeclaration
    {
        public EmptyDeclaration(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 48:  Modifiers ::= Modifier
     *<li>Rule 49:  Modifiers ::= Modifiers Modifier
     *<li>Rule 320:  Modifiersopt ::= $Empty
     *<li>Rule 321:  Modifiersopt ::= Modifiers
     *</b>
     */
    static public class ModifierList extends AstList implements IModifiers, IModifiersopt
    {
        public IModifier getModifierAt(int i) { return (IModifier) getElementAt(i); }

        public ModifierList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public ModifierList(IModifier _Modifier, boolean leftRecursive)
        {
            super((Ast) _Modifier, leftRecursive);
            initialize();
        }

        public void add(IModifier _Modifier)
        {
            super.add((Ast) _Modifier);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof ModifierList)) return false;
            ModifierList other = (ModifierList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                IModifier element = getModifierAt(i);
                    if (! element.equals(other.getModifierAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getModifierAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getModifierAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getModifierAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getModifierAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getModifierAt(i).accept(v, o); return null; }
    }

    /**
     *<b>
     *<li>Rule 50:  Modifier ::= public
     *</b>
     */
    static public class PublicModifier extends AstToken implements IModifier
    {
        public PublicModifier(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 51:  Modifier ::= protected
     *</b>
     */
    static public class ProtectedModifier extends AstToken implements IModifier
    {
        public ProtectedModifier(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 52:  Modifier ::= private
     *</b>
     */
    static public class PrivateModifier extends AstToken implements IModifier
    {
        public PrivateModifier(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 53:  Modifier ::= static
     *</b>
     */
    static public class StaticModifier extends AstToken implements IModifier
    {
        public StaticModifier(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 54:  Modifier ::= abstract
     *</b>
     */
    static public class AbstractModifier extends AstToken implements IModifier
    {
        public AbstractModifier(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 55:  Modifier ::= final
     *</b>
     */
    static public class FinalModifier extends AstToken implements IModifier
    {
        public FinalModifier(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 56:  Modifier ::= native
     *</b>
     */
    static public class NativeModifier extends AstToken implements IModifier
    {
        public NativeModifier(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 57:  Modifier ::= strictfp
     *</b>
     */
    static public class StrictfpModifier extends AstToken implements IModifier
    {
        public StrictfpModifier(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 58:  Modifier ::= synchronized
     *</b>
     */
    static public class SynchronizedModifier extends AstToken implements IModifier
    {
        public SynchronizedModifier(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 59:  Modifier ::= transient
     *</b>
     */
    static public class TransientModifier extends AstToken implements IModifier
    {
        public TransientModifier(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 60:  Modifier ::= volatile
     *</b>
     */
    static public class VolatileModifier extends AstToken implements IModifier
    {
        public VolatileModifier(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 61:  ClassDeclaration ::= Modifiersopt class IDENTIFIER$Name Superopt Interfacesopt ClassBody
     *</b>
     */
    static public class ClassDeclaration extends Ast implements IClassDeclaration
    {
        private JavaParser environment;
        public JavaParser getEnvironment() { return environment; }

        private ModifierList _Modifiersopt;
        private AstToken _Name;
        private Super _Superopt;
        private InterfaceTypeList _Interfacesopt;
        private ClassBody _ClassBody;

        public ModifierList getModifiersopt() { return _Modifiersopt; }
        public AstToken getName() { return _Name; }
        /**
         * The value returned by <b>getSuperopt</b> may be <b>null</b>
         */
        public Super getSuperopt() { return _Superopt; }
        public InterfaceTypeList getInterfacesopt() { return _Interfacesopt; }
        public ClassBody getClassBody() { return _ClassBody; }

        public ClassDeclaration(JavaParser environment, IToken leftIToken, IToken rightIToken,
                                ModifierList _Modifiersopt,
                                AstToken _Name,
                                Super _Superopt,
                                InterfaceTypeList _Interfacesopt,
                                ClassBody _ClassBody)
        {
            super(leftIToken, rightIToken);

            this.environment = environment;
            this._Modifiersopt = _Modifiersopt;
            this._Name = _Name;
            this._Superopt = _Superopt;
            this._Interfacesopt = _Interfacesopt;
            this._ClassBody = _ClassBody;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ClassDeclaration)) return false;
            ClassDeclaration other = (ClassDeclaration    ) o;
            if (! _Modifiersopt.equals(other.getModifiersopt())) return false;
            if (! _Name.equals(other.getName())) return false;
            if (_Superopt == null && other.getSuperopt() != null) return false;
            else if (! _Superopt.equals(other.getSuperopt())) return false;
            if (! _Interfacesopt.equals(other.getInterfacesopt())) return false;
            if (! _ClassBody.equals(other.getClassBody())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getModifiersopt().hashCode());
            hash = hash * 31 + (getName().hashCode());
            hash = hash * 31 + (getSuperopt() == null ? 0 : getSuperopt().hashCode());
            hash = hash * 31 + (getInterfacesopt().hashCode());
            hash = hash * 31 + (getClassBody().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }

        private IToken docComment;
        public IToken getDocComment() { return docComment; }
        
        public void initialize()
        {
            docComment = environment.getDocComment(getLeftIToken());
        }
    }

    /**
     *<b>
     *<li>Rule 62:  Super ::= extends ClassType
     *</b>
     */
    static public class Super extends Ast implements ISuper
    {
        private IClassType _ClassType;

        public IClassType getClassType() { return _ClassType; }

        public Super(IToken leftIToken, IToken rightIToken,
                     IClassType _ClassType)
        {
            super(leftIToken, rightIToken);

            this._ClassType = _ClassType;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof Super)) return false;
            Super other = (Super    ) o;
            if (! _ClassType.equals(other.getClassType())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getClassType().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 63:  Interfaces ::= implements InterfaceTypeList
     *<li>Rule 64:  InterfaceTypeList ::= InterfaceType
     *<li>Rule 65:  InterfaceTypeList ::= InterfaceTypeList , InterfaceType
     *<li>Rule 109:  ExtendsInterfaces ::= extends InterfaceTypeList
     *<li>Rule 334:  Interfacesopt ::= $Empty
     *<li>Rule 335:  Interfacesopt ::= Interfaces
     *<li>Rule 342:  ExtendsInterfacesopt ::= $Empty
     *<li>Rule 343:  ExtendsInterfacesopt ::= ExtendsInterfaces
     *</b>
     */
    static public class InterfaceTypeList extends AstList implements IInterfaces, IInterfaceTypeList, IExtendsInterfaces, IInterfacesopt, IExtendsInterfacesopt
    {
        public IInterfaceType getInterfaceTypeAt(int i) { return (IInterfaceType) getElementAt(i); }

        public InterfaceTypeList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public InterfaceTypeList(IInterfaceType _InterfaceType, boolean leftRecursive)
        {
            super((Ast) _InterfaceType, leftRecursive);
            initialize();
        }

        public void add(IInterfaceType _InterfaceType)
        {
            super.add((Ast) _InterfaceType);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof InterfaceTypeList)) return false;
            InterfaceTypeList other = (InterfaceTypeList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                IInterfaceType element = getInterfaceTypeAt(i);
                    if (! element.equals(other.getInterfaceTypeAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getInterfaceTypeAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getInterfaceTypeAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getInterfaceTypeAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getInterfaceTypeAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getInterfaceTypeAt(i).accept(v, o); return null; }
    }

    /**
     *<b>
     *<li>Rule 66:  ClassBody ::= { ClassBodyDeclarationsopt }
     *</b>
     */
    static public class ClassBody extends Ast implements IClassBody
    {
        private ClassBodyDeclarationList _ClassBodyDeclarationsopt;

        public ClassBodyDeclarationList getClassBodyDeclarationsopt() { return _ClassBodyDeclarationsopt; }

        public ClassBody(IToken leftIToken, IToken rightIToken,
                         ClassBodyDeclarationList _ClassBodyDeclarationsopt)
        {
            super(leftIToken, rightIToken);

            this._ClassBodyDeclarationsopt = _ClassBodyDeclarationsopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ClassBody)) return false;
            ClassBody other = (ClassBody    ) o;
            if (! _ClassBodyDeclarationsopt.equals(other.getClassBodyDeclarationsopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getClassBodyDeclarationsopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 67:  ClassBodyDeclarations ::= ClassBodyDeclaration
     *<li>Rule 68:  ClassBodyDeclarations ::= ClassBodyDeclarations ClassBodyDeclaration
     *<li>Rule 318:  ClassBodyDeclarationsopt ::= $Empty
     *<li>Rule 319:  ClassBodyDeclarationsopt ::= ClassBodyDeclarations
     *</b>
     */
    static public class ClassBodyDeclarationList extends AstList implements IClassBodyDeclarations, IClassBodyDeclarationsopt
    {
        public IClassBodyDeclaration getClassBodyDeclarationAt(int i) { return (IClassBodyDeclaration) getElementAt(i); }

        public ClassBodyDeclarationList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public ClassBodyDeclarationList(IClassBodyDeclaration _ClassBodyDeclaration, boolean leftRecursive)
        {
            super((Ast) _ClassBodyDeclaration, leftRecursive);
            initialize();
        }

        public void add(IClassBodyDeclaration _ClassBodyDeclaration)
        {
            super.add((Ast) _ClassBodyDeclaration);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof ClassBodyDeclarationList)) return false;
            ClassBodyDeclarationList other = (ClassBodyDeclarationList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                IClassBodyDeclaration element = getClassBodyDeclarationAt(i);
                    if (! element.equals(other.getClassBodyDeclarationAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getClassBodyDeclarationAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getClassBodyDeclarationAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getClassBodyDeclarationAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getClassBodyDeclarationAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getClassBodyDeclarationAt(i).accept(v, o); return null; }
    }

    /**
     *<b>
     *<li>Rule 78:  FieldDeclaration ::= Modifiersopt Type VariableDeclarators ;
     *</b>
     */
    static public class FieldDeclaration extends Ast implements IFieldDeclaration
    {
        private JavaParser environment;
        public JavaParser getEnvironment() { return environment; }

        private ModifierList _Modifiersopt;
        private IType _Type;
        private VariableDeclaratorList _VariableDeclarators;

        public ModifierList getModifiersopt() { return _Modifiersopt; }
        public IType getType() { return _Type; }
        public VariableDeclaratorList getVariableDeclarators() { return _VariableDeclarators; }

        public FieldDeclaration(JavaParser environment, IToken leftIToken, IToken rightIToken,
                                ModifierList _Modifiersopt,
                                IType _Type,
                                VariableDeclaratorList _VariableDeclarators)
        {
            super(leftIToken, rightIToken);

            this.environment = environment;
            this._Modifiersopt = _Modifiersopt;
            this._Type = _Type;
            this._VariableDeclarators = _VariableDeclarators;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof FieldDeclaration)) return false;
            FieldDeclaration other = (FieldDeclaration    ) o;
            if (! _Modifiersopt.equals(other.getModifiersopt())) return false;
            if (! _Type.equals(other.getType())) return false;
            if (! _VariableDeclarators.equals(other.getVariableDeclarators())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getModifiersopt().hashCode());
            hash = hash * 31 + (getType().hashCode());
            hash = hash * 31 + (getVariableDeclarators().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }

        private IToken docComment;
        public IToken getDocComment() { return docComment; }
        
        public void initialize()
        {
            docComment = environment.getDocComment(getLeftIToken());
        }
    }

    /**
     *<b>
     *<li>Rule 79:  VariableDeclarators ::= VariableDeclarator
     *<li>Rule 80:  VariableDeclarators ::= VariableDeclarators , VariableDeclarator
     *</b>
     */
    static public class VariableDeclaratorList extends AstList implements IVariableDeclarators
    {
        public IVariableDeclarator getVariableDeclaratorAt(int i) { return (IVariableDeclarator) getElementAt(i); }

        public VariableDeclaratorList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public VariableDeclaratorList(IVariableDeclarator _VariableDeclarator, boolean leftRecursive)
        {
            super((Ast) _VariableDeclarator, leftRecursive);
            initialize();
        }

        public void add(IVariableDeclarator _VariableDeclarator)
        {
            super.add((Ast) _VariableDeclarator);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof VariableDeclaratorList)) return false;
            VariableDeclaratorList other = (VariableDeclaratorList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                IVariableDeclarator element = getVariableDeclaratorAt(i);
                    if (! element.equals(other.getVariableDeclaratorAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getVariableDeclaratorAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getVariableDeclaratorAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getVariableDeclaratorAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getVariableDeclaratorAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getVariableDeclaratorAt(i).accept(v, o); return null; }
    }

    /**
     *<em>
     *<li>Rule 81:  VariableDeclarator ::= VariableDeclaratorId
     *</em>
     *<p>
     *<b>
     *<li>Rule 82:  VariableDeclarator ::= VariableDeclaratorId = VariableInitializer
     *</b>
     */
    static public class VariableDeclarator extends Ast implements IVariableDeclarator
    {
        private VariableDeclaratorId _VariableDeclaratorId;
        private IVariableInitializer _VariableInitializer;

        public VariableDeclaratorId getVariableDeclaratorId() { return _VariableDeclaratorId; }
        public IVariableInitializer getVariableInitializer() { return _VariableInitializer; }

        public VariableDeclarator(IToken leftIToken, IToken rightIToken,
                                  VariableDeclaratorId _VariableDeclaratorId,
                                  IVariableInitializer _VariableInitializer)
        {
            super(leftIToken, rightIToken);

            this._VariableDeclaratorId = _VariableDeclaratorId;
            this._VariableInitializer = _VariableInitializer;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof VariableDeclarator)) return false;
            VariableDeclarator other = (VariableDeclarator    ) o;
            if (! _VariableDeclaratorId.equals(other.getVariableDeclaratorId())) return false;
            if (! _VariableInitializer.equals(other.getVariableInitializer())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getVariableDeclaratorId().hashCode());
            hash = hash * 31 + (getVariableInitializer().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 83:  VariableDeclaratorId ::= IDENTIFIER Dimsopt
     *</b>
     */
    static public class VariableDeclaratorId extends Ast implements IVariableDeclaratorId
    {
        private DimList _Dimsopt;

        public DimList getDimsopt() { return _Dimsopt; }

        public VariableDeclaratorId(IToken leftIToken, IToken rightIToken,
                                    DimList _Dimsopt)
        {
            super(leftIToken, rightIToken);

            this._Dimsopt = _Dimsopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof VariableDeclaratorId)) return false;
            VariableDeclaratorId other = (VariableDeclaratorId    ) o;
            if (! _Dimsopt.equals(other.getDimsopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getDimsopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 86:  MethodDeclaration ::= MethodHeader MethodBody
     *</b>
     */
    static public class MethodDeclaration extends Ast implements IMethodDeclaration
    {
        private JavaParser environment;
        public JavaParser getEnvironment() { return environment; }

        private IMethodHeader _MethodHeader;
        private IMethodBody _MethodBody;

        public IMethodHeader getMethodHeader() { return _MethodHeader; }
        public IMethodBody getMethodBody() { return _MethodBody; }

        public MethodDeclaration(JavaParser environment, IToken leftIToken, IToken rightIToken,
                                 IMethodHeader _MethodHeader,
                                 IMethodBody _MethodBody)
        {
            super(leftIToken, rightIToken);

            this.environment = environment;
            this._MethodHeader = _MethodHeader;
            this._MethodBody = _MethodBody;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof MethodDeclaration)) return false;
            MethodDeclaration other = (MethodDeclaration    ) o;
            if (! _MethodHeader.equals(other.getMethodHeader())) return false;
            if (! _MethodBody.equals(other.getMethodBody())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getMethodHeader().hashCode());
            hash = hash * 31 + (getMethodBody().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }

        private IToken docComment;
        public IToken getDocComment() { return docComment; }
        
        public void initialize()
        {
            docComment = environment.getDocComment(getLeftIToken());
        }
    }

    /**
     *<b>
     *<li>Rule 87:  MethodHeader ::= Modifiersopt Type MethodDeclarator Throwsopt
     *</b>
     */
    static public class TypedMethodHeader extends Ast implements IMethodHeader
    {
        private ModifierList _Modifiersopt;
        private IType _Type;
        private MethodDeclarator _MethodDeclarator;
        private ClassTypeList _Throwsopt;

        public ModifierList getModifiersopt() { return _Modifiersopt; }
        public IType getType() { return _Type; }
        public MethodDeclarator getMethodDeclarator() { return _MethodDeclarator; }
        public ClassTypeList getThrowsopt() { return _Throwsopt; }

        public TypedMethodHeader(IToken leftIToken, IToken rightIToken,
                                 ModifierList _Modifiersopt,
                                 IType _Type,
                                 MethodDeclarator _MethodDeclarator,
                                 ClassTypeList _Throwsopt)
        {
            super(leftIToken, rightIToken);

            this._Modifiersopt = _Modifiersopt;
            this._Type = _Type;
            this._MethodDeclarator = _MethodDeclarator;
            this._Throwsopt = _Throwsopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof TypedMethodHeader)) return false;
            TypedMethodHeader other = (TypedMethodHeader    ) o;
            if (! _Modifiersopt.equals(other.getModifiersopt())) return false;
            if (! _Type.equals(other.getType())) return false;
            if (! _MethodDeclarator.equals(other.getMethodDeclarator())) return false;
            if (! _Throwsopt.equals(other.getThrowsopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getModifiersopt().hashCode());
            hash = hash * 31 + (getType().hashCode());
            hash = hash * 31 + (getMethodDeclarator().hashCode());
            hash = hash * 31 + (getThrowsopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 88:  MethodHeader ::= Modifiersopt void MethodDeclarator Throwsopt
     *</b>
     */
    static public class VoidMethodHeader extends Ast implements IMethodHeader
    {
        private ModifierList _Modifiersopt;
        private MethodDeclarator _MethodDeclarator;
        private ClassTypeList _Throwsopt;

        public ModifierList getModifiersopt() { return _Modifiersopt; }
        public MethodDeclarator getMethodDeclarator() { return _MethodDeclarator; }
        public ClassTypeList getThrowsopt() { return _Throwsopt; }

        public VoidMethodHeader(IToken leftIToken, IToken rightIToken,
                                ModifierList _Modifiersopt,
                                MethodDeclarator _MethodDeclarator,
                                ClassTypeList _Throwsopt)
        {
            super(leftIToken, rightIToken);

            this._Modifiersopt = _Modifiersopt;
            this._MethodDeclarator = _MethodDeclarator;
            this._Throwsopt = _Throwsopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof VoidMethodHeader)) return false;
            VoidMethodHeader other = (VoidMethodHeader    ) o;
            if (! _Modifiersopt.equals(other.getModifiersopt())) return false;
            if (! _MethodDeclarator.equals(other.getMethodDeclarator())) return false;
            if (! _Throwsopt.equals(other.getThrowsopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getModifiersopt().hashCode());
            hash = hash * 31 + (getMethodDeclarator().hashCode());
            hash = hash * 31 + (getThrowsopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 89:  MethodDeclarator ::= IDENTIFIER ( FormalParameterListopt ) Dimsopt
     *</b>
     */
    static public class MethodDeclarator extends Ast implements IMethodDeclarator
    {
        private FormalParameterList _FormalParameterListopt;
        private DimList _Dimsopt;

        public FormalParameterList getFormalParameterListopt() { return _FormalParameterListopt; }
        public DimList getDimsopt() { return _Dimsopt; }

        public MethodDeclarator(IToken leftIToken, IToken rightIToken,
                                FormalParameterList _FormalParameterListopt,
                                DimList _Dimsopt)
        {
            super(leftIToken, rightIToken);

            this._FormalParameterListopt = _FormalParameterListopt;
            this._Dimsopt = _Dimsopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof MethodDeclarator)) return false;
            MethodDeclarator other = (MethodDeclarator    ) o;
            if (! _FormalParameterListopt.equals(other.getFormalParameterListopt())) return false;
            if (! _Dimsopt.equals(other.getDimsopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getFormalParameterListopt().hashCode());
            hash = hash * 31 + (getDimsopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 90:  FormalParameterList ::= FormalParameter
     *<li>Rule 91:  FormalParameterList ::= FormalParameterList , FormalParameter
     *<li>Rule 332:  FormalParameterListopt ::= $Empty
     *<li>Rule 333:  FormalParameterListopt ::= FormalParameterList
     *</b>
     */
    static public class FormalParameterList extends AstList implements IFormalParameterList, IFormalParameterListopt
    {
        public FormalParameter getFormalParameterAt(int i) { return (FormalParameter) getElementAt(i); }

        public FormalParameterList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public FormalParameterList(FormalParameter _FormalParameter, boolean leftRecursive)
        {
            super((Ast) _FormalParameter, leftRecursive);
            initialize();
        }

        public void add(FormalParameter _FormalParameter)
        {
            super.add((Ast) _FormalParameter);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof FormalParameterList)) return false;
            FormalParameterList other = (FormalParameterList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                FormalParameter element = getFormalParameterAt(i);
                    if (! element.equals(other.getFormalParameterAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getFormalParameterAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) v.visit(getFormalParameterAt(i)); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) v.visit(getFormalParameterAt(i), o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) v.visit(getFormalParameterAt(i)); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) v.visit(getFormalParameterAt(i), o); return null; }
    }

    /**
     *<b>
     *<li>Rule 92:  FormalParameter ::= Modifiersopt Type VariableDeclaratorId
     *</b>
     */
    static public class FormalParameter extends Ast implements IFormalParameter
    {
        private ModifierList _Modifiersopt;
        private IType _Type;
        private VariableDeclaratorId _VariableDeclaratorId;

        public ModifierList getModifiersopt() { return _Modifiersopt; }
        public IType getType() { return _Type; }
        public VariableDeclaratorId getVariableDeclaratorId() { return _VariableDeclaratorId; }

        public FormalParameter(IToken leftIToken, IToken rightIToken,
                               ModifierList _Modifiersopt,
                               IType _Type,
                               VariableDeclaratorId _VariableDeclaratorId)
        {
            super(leftIToken, rightIToken);

            this._Modifiersopt = _Modifiersopt;
            this._Type = _Type;
            this._VariableDeclaratorId = _VariableDeclaratorId;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof FormalParameter)) return false;
            FormalParameter other = (FormalParameter    ) o;
            if (! _Modifiersopt.equals(other.getModifiersopt())) return false;
            if (! _Type.equals(other.getType())) return false;
            if (! _VariableDeclaratorId.equals(other.getVariableDeclaratorId())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getModifiersopt().hashCode());
            hash = hash * 31 + (getType().hashCode());
            hash = hash * 31 + (getVariableDeclaratorId().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 93:  Throws ::= throws ClassTypeList
     *<li>Rule 94:  ClassTypeList ::= ClassType
     *<li>Rule 95:  ClassTypeList ::= ClassTypeList , ClassType
     *<li>Rule 330:  Throwsopt ::= $Empty
     *<li>Rule 331:  Throwsopt ::= Throws
     *</b>
     */
    static public class ClassTypeList extends AstList implements IThrows, IClassTypeList, IThrowsopt
    {
        public IClassType getClassTypeAt(int i) { return (IClassType) getElementAt(i); }

        public ClassTypeList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public ClassTypeList(IClassType _ClassType, boolean leftRecursive)
        {
            super((Ast) _ClassType, leftRecursive);
            initialize();
        }

        public void add(IClassType _ClassType)
        {
            super.add((Ast) _ClassType);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof ClassTypeList)) return false;
            ClassTypeList other = (ClassTypeList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                IClassType element = getClassTypeAt(i);
                    if (! element.equals(other.getClassTypeAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getClassTypeAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getClassTypeAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getClassTypeAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getClassTypeAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getClassTypeAt(i).accept(v, o); return null; }
    }

    /**
     *<b>
     *<li>Rule 97:  MethodBody ::= ;
     *</b>
     */
    static public class EmptyMethodBody extends AstToken implements IMethodBody
    {
        public EmptyMethodBody(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 98:  StaticInitializer ::= static Block
     *</b>
     */
    static public class StaticInitializer extends Ast implements IStaticInitializer
    {
        private Block _Block;

        public Block getBlock() { return _Block; }

        public StaticInitializer(IToken leftIToken, IToken rightIToken,
                                 Block _Block)
        {
            super(leftIToken, rightIToken);

            this._Block = _Block;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof StaticInitializer)) return false;
            StaticInitializer other = (StaticInitializer    ) o;
            if (! _Block.equals(other.getBlock())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getBlock().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 99:  ConstructorDeclaration ::= Modifiersopt ConstructorDeclarator Throwsopt ConstructorBody
     *</b>
     */
    static public class ConstructorDeclaration extends Ast implements IConstructorDeclaration
    {
        private JavaParser environment;
        public JavaParser getEnvironment() { return environment; }

        private ModifierList _Modifiersopt;
        private ConstructorDeclarator _ConstructorDeclarator;
        private ClassTypeList _Throwsopt;
        private IConstructorBody _ConstructorBody;

        public ModifierList getModifiersopt() { return _Modifiersopt; }
        public ConstructorDeclarator getConstructorDeclarator() { return _ConstructorDeclarator; }
        public ClassTypeList getThrowsopt() { return _Throwsopt; }
        public IConstructorBody getConstructorBody() { return _ConstructorBody; }

        public ConstructorDeclaration(JavaParser environment, IToken leftIToken, IToken rightIToken,
                                      ModifierList _Modifiersopt,
                                      ConstructorDeclarator _ConstructorDeclarator,
                                      ClassTypeList _Throwsopt,
                                      IConstructorBody _ConstructorBody)
        {
            super(leftIToken, rightIToken);

            this.environment = environment;
            this._Modifiersopt = _Modifiersopt;
            this._ConstructorDeclarator = _ConstructorDeclarator;
            this._Throwsopt = _Throwsopt;
            this._ConstructorBody = _ConstructorBody;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ConstructorDeclaration)) return false;
            ConstructorDeclaration other = (ConstructorDeclaration    ) o;
            if (! _Modifiersopt.equals(other.getModifiersopt())) return false;
            if (! _ConstructorDeclarator.equals(other.getConstructorDeclarator())) return false;
            if (! _Throwsopt.equals(other.getThrowsopt())) return false;
            if (! _ConstructorBody.equals(other.getConstructorBody())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getModifiersopt().hashCode());
            hash = hash * 31 + (getConstructorDeclarator().hashCode());
            hash = hash * 31 + (getThrowsopt().hashCode());
            hash = hash * 31 + (getConstructorBody().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }

        private IToken docComment;
        public IToken getDocComment() { return docComment; }
        
        public void initialize()
        {
            docComment = environment.getDocComment(getLeftIToken());
        }
    }

    /**
     *<b>
     *<li>Rule 100:  ConstructorDeclarator ::= IDENTIFIER ( FormalParameterListopt )
     *</b>
     */
    static public class ConstructorDeclarator extends Ast implements IConstructorDeclarator
    {
        private FormalParameterList _FormalParameterListopt;

        public FormalParameterList getFormalParameterListopt() { return _FormalParameterListopt; }

        public ConstructorDeclarator(IToken leftIToken, IToken rightIToken,
                                     FormalParameterList _FormalParameterListopt)
        {
            super(leftIToken, rightIToken);

            this._FormalParameterListopt = _FormalParameterListopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ConstructorDeclarator)) return false;
            ConstructorDeclarator other = (ConstructorDeclarator    ) o;
            if (! _FormalParameterListopt.equals(other.getFormalParameterListopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getFormalParameterListopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<em>
     *<li>Rule 101:  ConstructorBody ::= Block
     *</em>
     *<p>
     *<b>
     *<li>Rule 102:  ConstructorBody ::= { ExplicitConstructorInvocation BlockStatementsopt }
     *</b>
     */
    static public class ConstructorBody extends Ast implements IConstructorBody
    {
        private IExplicitConstructorInvocation _ExplicitConstructorInvocation;
        private BlockStatementList _BlockStatementsopt;

        public IExplicitConstructorInvocation getExplicitConstructorInvocation() { return _ExplicitConstructorInvocation; }
        public BlockStatementList getBlockStatementsopt() { return _BlockStatementsopt; }

        public ConstructorBody(IToken leftIToken, IToken rightIToken,
                               IExplicitConstructorInvocation _ExplicitConstructorInvocation,
                               BlockStatementList _BlockStatementsopt)
        {
            super(leftIToken, rightIToken);

            this._ExplicitConstructorInvocation = _ExplicitConstructorInvocation;
            this._BlockStatementsopt = _BlockStatementsopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ConstructorBody)) return false;
            ConstructorBody other = (ConstructorBody    ) o;
            if (! _ExplicitConstructorInvocation.equals(other.getExplicitConstructorInvocation())) return false;
            if (! _BlockStatementsopt.equals(other.getBlockStatementsopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getExplicitConstructorInvocation().hashCode());
            hash = hash * 31 + (getBlockStatementsopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 103:  ExplicitConstructorInvocation ::= this ( ArgumentListopt ) ;
     *<li>Rule 104:  ExplicitConstructorInvocation ::= Primary . this ( ArgumentListopt ) ;
     *</b>
     */
    static public class ThisCall extends Ast implements IExplicitConstructorInvocation
    {
        private ExpressionList _ArgumentListopt;
        private IPrimary _Primary;

        public ExpressionList getArgumentListopt() { return _ArgumentListopt; }
        /**
         * The value returned by <b>getPrimary</b> may be <b>null</b>
         */
        public IPrimary getPrimary() { return _Primary; }

        public ThisCall(IToken leftIToken, IToken rightIToken,
                        ExpressionList _ArgumentListopt,
                        IPrimary _Primary)
        {
            super(leftIToken, rightIToken);

            this._ArgumentListopt = _ArgumentListopt;
            this._Primary = _Primary;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ThisCall)) return false;
            ThisCall other = (ThisCall    ) o;
            if (! _ArgumentListopt.equals(other.getArgumentListopt())) return false;
            if (_Primary == null && other.getPrimary() != null) return false;
            else if (! _Primary.equals(other.getPrimary())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getArgumentListopt().hashCode());
            hash = hash * 31 + (getPrimary() == null ? 0 : getPrimary().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 105:  ExplicitConstructorInvocation ::= super ( ArgumentListopt ) ;
     *<li>Rule 106:  ExplicitConstructorInvocation ::= Primary$expression . super ( ArgumentListopt ) ;
     *<li>Rule 107:  ExplicitConstructorInvocation ::= Name$expression . super ( ArgumentListopt ) ;
     *</b>
     */
    static public class SuperCall extends Ast implements IExplicitConstructorInvocation
    {
        private ExpressionList _ArgumentListopt;
        private IPostfixExpression _expression;

        public ExpressionList getArgumentListopt() { return _ArgumentListopt; }
        /**
         * The value returned by <b>getexpression</b> may be <b>null</b>
         */
        public IPostfixExpression getexpression() { return _expression; }

        public SuperCall(IToken leftIToken, IToken rightIToken,
                         ExpressionList _ArgumentListopt,
                         IPostfixExpression _expression)
        {
            super(leftIToken, rightIToken);

            this._ArgumentListopt = _ArgumentListopt;
            this._expression = _expression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof SuperCall)) return false;
            SuperCall other = (SuperCall    ) o;
            if (! _ArgumentListopt.equals(other.getArgumentListopt())) return false;
            if (_expression == null && other.getexpression() != null) return false;
            else if (! _expression.equals(other.getexpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getArgumentListopt().hashCode());
            hash = hash * 31 + (getexpression() == null ? 0 : getexpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 108:  InterfaceDeclaration ::= Modifiersopt interface IDENTIFIER$Name ExtendsInterfacesopt InterfaceBody
     *</b>
     */
    static public class InterfaceDeclaration extends Ast implements IInterfaceDeclaration
    {
        private JavaParser environment;
        public JavaParser getEnvironment() { return environment; }

        private ModifierList _Modifiersopt;
        private AstToken _Name;
        private InterfaceTypeList _ExtendsInterfacesopt;
        private InterfaceBody _InterfaceBody;

        public ModifierList getModifiersopt() { return _Modifiersopt; }
        public AstToken getName() { return _Name; }
        public InterfaceTypeList getExtendsInterfacesopt() { return _ExtendsInterfacesopt; }
        public InterfaceBody getInterfaceBody() { return _InterfaceBody; }

        public InterfaceDeclaration(JavaParser environment, IToken leftIToken, IToken rightIToken,
                                    ModifierList _Modifiersopt,
                                    AstToken _Name,
                                    InterfaceTypeList _ExtendsInterfacesopt,
                                    InterfaceBody _InterfaceBody)
        {
            super(leftIToken, rightIToken);

            this.environment = environment;
            this._Modifiersopt = _Modifiersopt;
            this._Name = _Name;
            this._ExtendsInterfacesopt = _ExtendsInterfacesopt;
            this._InterfaceBody = _InterfaceBody;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof InterfaceDeclaration)) return false;
            InterfaceDeclaration other = (InterfaceDeclaration    ) o;
            if (! _Modifiersopt.equals(other.getModifiersopt())) return false;
            if (! _Name.equals(other.getName())) return false;
            if (! _ExtendsInterfacesopt.equals(other.getExtendsInterfacesopt())) return false;
            if (! _InterfaceBody.equals(other.getInterfaceBody())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getModifiersopt().hashCode());
            hash = hash * 31 + (getName().hashCode());
            hash = hash * 31 + (getExtendsInterfacesopt().hashCode());
            hash = hash * 31 + (getInterfaceBody().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }

        private IToken docComment;
        public IToken getDocComment() { return docComment; }
        
        public void initialize()
        {
            docComment = environment.getDocComment(getLeftIToken());
        }
    }

    /**
     *<b>
     *<li>Rule 110:  InterfaceBody ::= { InterfaceMemberDeclarationsopt }
     *</b>
     */
    static public class InterfaceBody extends Ast implements IInterfaceBody
    {
        private InterfaceMemberDeclarationList _InterfaceMemberDeclarationsopt;

        public InterfaceMemberDeclarationList getInterfaceMemberDeclarationsopt() { return _InterfaceMemberDeclarationsopt; }

        public InterfaceBody(IToken leftIToken, IToken rightIToken,
                             InterfaceMemberDeclarationList _InterfaceMemberDeclarationsopt)
        {
            super(leftIToken, rightIToken);

            this._InterfaceMemberDeclarationsopt = _InterfaceMemberDeclarationsopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof InterfaceBody)) return false;
            InterfaceBody other = (InterfaceBody    ) o;
            if (! _InterfaceMemberDeclarationsopt.equals(other.getInterfaceMemberDeclarationsopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getInterfaceMemberDeclarationsopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 111:  InterfaceMemberDeclarations ::= InterfaceMemberDeclaration
     *<li>Rule 112:  InterfaceMemberDeclarations ::= InterfaceMemberDeclarations InterfaceMemberDeclaration
     *<li>Rule 336:  InterfaceMemberDeclarationsopt ::= $Empty
     *<li>Rule 337:  InterfaceMemberDeclarationsopt ::= InterfaceMemberDeclarations
     *</b>
     */
    static public class InterfaceMemberDeclarationList extends AstList implements IInterfaceMemberDeclarations, IInterfaceMemberDeclarationsopt
    {
        public IInterfaceMemberDeclaration getInterfaceMemberDeclarationAt(int i) { return (IInterfaceMemberDeclaration) getElementAt(i); }

        public InterfaceMemberDeclarationList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public InterfaceMemberDeclarationList(IInterfaceMemberDeclaration _InterfaceMemberDeclaration, boolean leftRecursive)
        {
            super((Ast) _InterfaceMemberDeclaration, leftRecursive);
            initialize();
        }

        public void add(IInterfaceMemberDeclaration _InterfaceMemberDeclaration)
        {
            super.add((Ast) _InterfaceMemberDeclaration);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof InterfaceMemberDeclarationList)) return false;
            InterfaceMemberDeclarationList other = (InterfaceMemberDeclarationList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                IInterfaceMemberDeclaration element = getInterfaceMemberDeclarationAt(i);
                    if (! element.equals(other.getInterfaceMemberDeclarationAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getInterfaceMemberDeclarationAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getInterfaceMemberDeclarationAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getInterfaceMemberDeclarationAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getInterfaceMemberDeclarationAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getInterfaceMemberDeclarationAt(i).accept(v, o); return null; }
    }

    /**
     *<b>
     *<li>Rule 119:  AbstractMethodDeclaration ::= MethodHeader ;
     *</b>
     */
    static public class AbstractMethodDeclaration extends Ast implements IAbstractMethodDeclaration
    {
        private JavaParser environment;
        public JavaParser getEnvironment() { return environment; }

        private IMethodHeader _MethodHeader;

        public IMethodHeader getMethodHeader() { return _MethodHeader; }

        public AbstractMethodDeclaration(JavaParser environment, IToken leftIToken, IToken rightIToken,
                                         IMethodHeader _MethodHeader)
        {
            super(leftIToken, rightIToken);

            this.environment = environment;
            this._MethodHeader = _MethodHeader;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof AbstractMethodDeclaration)) return false;
            AbstractMethodDeclaration other = (AbstractMethodDeclaration    ) o;
            if (! _MethodHeader.equals(other.getMethodHeader())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getMethodHeader().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }

        private IToken docComment;
        public IToken getDocComment() { return docComment; }
        
        public void initialize()
        {
            docComment = environment.getDocComment(getLeftIToken());
        }
    }

    /**
     *<b>
     *<li>Rule 120:  ArrayInitializer ::= { VariableInitializersopt Commaopt }
     *</b>
     */
    static public class ArrayInitializer extends Ast implements IArrayInitializer
    {
        private VariableInitializerList _VariableInitializersopt;
        private Commaopt _Commaopt;

        public VariableInitializerList getVariableInitializersopt() { return _VariableInitializersopt; }
        /**
         * The value returned by <b>getCommaopt</b> may be <b>null</b>
         */
        public Commaopt getCommaopt() { return _Commaopt; }

        public ArrayInitializer(IToken leftIToken, IToken rightIToken,
                                VariableInitializerList _VariableInitializersopt,
                                Commaopt _Commaopt)
        {
            super(leftIToken, rightIToken);

            this._VariableInitializersopt = _VariableInitializersopt;
            this._Commaopt = _Commaopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ArrayInitializer)) return false;
            ArrayInitializer other = (ArrayInitializer    ) o;
            if (! _VariableInitializersopt.equals(other.getVariableInitializersopt())) return false;
            if (_Commaopt == null && other.getCommaopt() != null) return false;
            else if (! _Commaopt.equals(other.getCommaopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getVariableInitializersopt().hashCode());
            hash = hash * 31 + (getCommaopt() == null ? 0 : getCommaopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 121:  VariableInitializers ::= VariableInitializer
     *<li>Rule 122:  VariableInitializers ::= VariableInitializers , VariableInitializer
     *<li>Rule 346:  VariableInitializersopt ::= $Empty
     *<li>Rule 347:  VariableInitializersopt ::= VariableInitializers
     *</b>
     */
    static public class VariableInitializerList extends AstList implements IVariableInitializers, IVariableInitializersopt
    {
        public IVariableInitializer getVariableInitializerAt(int i) { return (IVariableInitializer) getElementAt(i); }

        public VariableInitializerList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public VariableInitializerList(IVariableInitializer _VariableInitializer, boolean leftRecursive)
        {
            super((Ast) _VariableInitializer, leftRecursive);
            initialize();
        }

        public void add(IVariableInitializer _VariableInitializer)
        {
            super.add((Ast) _VariableInitializer);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof VariableInitializerList)) return false;
            VariableInitializerList other = (VariableInitializerList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                IVariableInitializer element = getVariableInitializerAt(i);
                    if (! element.equals(other.getVariableInitializerAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getVariableInitializerAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getVariableInitializerAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getVariableInitializerAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getVariableInitializerAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getVariableInitializerAt(i).accept(v, o); return null; }
    }

    /**
     *<b>
     *<li>Rule 123:  Block ::= { BlockStatementsopt }
     *</b>
     */
    static public class Block extends Ast implements IBlock
    {
        private BlockStatementList _BlockStatementsopt;

        public BlockStatementList getBlockStatementsopt() { return _BlockStatementsopt; }

        public Block(IToken leftIToken, IToken rightIToken,
                     BlockStatementList _BlockStatementsopt)
        {
            super(leftIToken, rightIToken);

            this._BlockStatementsopt = _BlockStatementsopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof Block)) return false;
            Block other = (Block    ) o;
            if (! _BlockStatementsopt.equals(other.getBlockStatementsopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getBlockStatementsopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 124:  BlockStatements ::= BlockStatement
     *<li>Rule 125:  BlockStatements ::= BlockStatements BlockStatement
     *<li>Rule 324:  BlockStatementsopt ::= $Empty
     *<li>Rule 325:  BlockStatementsopt ::= BlockStatements
     *</b>
     */
    static public class BlockStatementList extends AstList implements IBlockStatements, IBlockStatementsopt
    {
        public IBlockStatement getBlockStatementAt(int i) { return (IBlockStatement) getElementAt(i); }

        public BlockStatementList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public BlockStatementList(IBlockStatement _BlockStatement, boolean leftRecursive)
        {
            super((Ast) _BlockStatement, leftRecursive);
            initialize();
        }

        public void add(IBlockStatement _BlockStatement)
        {
            super.add((Ast) _BlockStatement);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof BlockStatementList)) return false;
            BlockStatementList other = (BlockStatementList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                IBlockStatement element = getBlockStatementAt(i);
                    if (! element.equals(other.getBlockStatementAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getBlockStatementAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getBlockStatementAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getBlockStatementAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getBlockStatementAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getBlockStatementAt(i).accept(v, o); return null; }
    }

    /**
     *<b>
     *<li>Rule 129:  LocalVariableDeclarationStatement ::= LocalVariableDeclaration ;
     *</b>
     */
    static public class LocalVariableDeclarationStatement extends Ast implements ILocalVariableDeclarationStatement
    {
        private LocalVariableDeclaration _LocalVariableDeclaration;

        public LocalVariableDeclaration getLocalVariableDeclaration() { return _LocalVariableDeclaration; }

        public LocalVariableDeclarationStatement(IToken leftIToken, IToken rightIToken,
                                                 LocalVariableDeclaration _LocalVariableDeclaration)
        {
            super(leftIToken, rightIToken);

            this._LocalVariableDeclaration = _LocalVariableDeclaration;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof LocalVariableDeclarationStatement)) return false;
            LocalVariableDeclarationStatement other = (LocalVariableDeclarationStatement    ) o;
            if (! _LocalVariableDeclaration.equals(other.getLocalVariableDeclaration())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getLocalVariableDeclaration().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 130:  LocalVariableDeclaration ::= Modifiers Type VariableDeclarators
     *<li>Rule 131:  LocalVariableDeclaration ::= Type VariableDeclarators
     *</b>
     */
    static public class LocalVariableDeclaration extends Ast implements ILocalVariableDeclaration
    {
        private JavaParser environment;
        public JavaParser getEnvironment() { return environment; }

        private ModifierList _Modifiers;
        private IType _Type;
        private VariableDeclaratorList _VariableDeclarators;

        /**
         * The value returned by <b>getModifiers</b> may be <b>null</b>
         */
        public ModifierList getModifiers() { return _Modifiers; }
        public IType getType() { return _Type; }
        public VariableDeclaratorList getVariableDeclarators() { return _VariableDeclarators; }

        public LocalVariableDeclaration(JavaParser environment, IToken leftIToken, IToken rightIToken,
                                        ModifierList _Modifiers,
                                        IType _Type,
                                        VariableDeclaratorList _VariableDeclarators)
        {
            super(leftIToken, rightIToken);

            this.environment = environment;
            this._Modifiers = _Modifiers;
            this._Type = _Type;
            this._VariableDeclarators = _VariableDeclarators;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof LocalVariableDeclaration)) return false;
            LocalVariableDeclaration other = (LocalVariableDeclaration    ) o;
            if (_Modifiers == null && other.getModifiers() != null) return false;
            else if (! _Modifiers.equals(other.getModifiers())) return false;
            if (! _Type.equals(other.getType())) return false;
            if (! _VariableDeclarators.equals(other.getVariableDeclarators())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getModifiers() == null ? 0 : getModifiers().hashCode());
            hash = hash * 31 + (getType().hashCode());
            hash = hash * 31 + (getVariableDeclarators().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }

        public void initialize()
        {
            if (_Modifiers == null)
                if (_Modifiers == null)
                {
                    IToken left = getLeftIToken(),
                        right = getRightIToken();
                    _Modifiers = new ModifierList(left, right, true);
                }
        }
    }

    /**
     *<b>
     *<li>Rule 154:  EmptyStatement ::= ;
     *</b>
     */
    static public class EmptyStatement extends AstToken implements IEmptyStatement
    {
        public EmptyStatement(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 155:  LabeledStatement ::= IDENTIFIER : Statement
     *<li>Rule 156:  LabeledStatementNoShortIf ::= IDENTIFIER : StatementNoShortIf$Statement
     *</b>
     */
    static public class LabeledStatement extends Ast implements ILabeledStatement, ILabeledStatementNoShortIf
    {
        private Ast _Statement;

        public Ast getStatement() { return _Statement; }

        public LabeledStatement(IToken leftIToken, IToken rightIToken,
                                Ast _Statement)
        {
            super(leftIToken, rightIToken);

            this._Statement = _Statement;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof LabeledStatement)) return false;
            LabeledStatement other = (LabeledStatement    ) o;
            if (! _Statement.equals(other.getStatement())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getStatement().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 157:  ExpressionStatement ::= StatementExpression ;
     *</b>
     */
    static public class ExpressionStatement extends Ast implements IExpressionStatement
    {
        private IStatementExpression _StatementExpression;

        public IStatementExpression getStatementExpression() { return _StatementExpression; }

        public ExpressionStatement(IToken leftIToken, IToken rightIToken,
                                   IStatementExpression _StatementExpression)
        {
            super(leftIToken, rightIToken);

            this._StatementExpression = _StatementExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ExpressionStatement)) return false;
            ExpressionStatement other = (ExpressionStatement    ) o;
            if (! _StatementExpression.equals(other.getStatementExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getStatementExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 165:  IfThenStatement ::= if ( Expression ) Statement$thenStmt
     *<li>Rule 166:  IfThenElseStatement ::= if ( Expression ) StatementNoShortIf$thenStmt else Statement$elseStmt
     *<li>Rule 167:  IfThenElseStatementNoShortIf ::= if ( Expression ) StatementNoShortIf$thenStmt else StatementNoShortIf$elseStmt
     *</b>
     */
    static public class IfStatement extends Ast implements IIfThenStatement, IIfThenElseStatement, IIfThenElseStatementNoShortIf
    {
        private IExpression _Expression;
        private Ast _thenStmt;
        private Ast _elseStmt;

        public IExpression getExpression() { return _Expression; }
        public Ast getthenStmt() { return _thenStmt; }
        /**
         * The value returned by <b>getelseStmt</b> may be <b>null</b>
         */
        public Ast getelseStmt() { return _elseStmt; }

        public IfStatement(IToken leftIToken, IToken rightIToken,
                           IExpression _Expression,
                           Ast _thenStmt,
                           Ast _elseStmt)
        {
            super(leftIToken, rightIToken);

            this._Expression = _Expression;
            this._thenStmt = _thenStmt;
            this._elseStmt = _elseStmt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof IfStatement)) return false;
            IfStatement other = (IfStatement    ) o;
            if (! _Expression.equals(other.getExpression())) return false;
            if (! _thenStmt.equals(other.getthenStmt())) return false;
            if (_elseStmt == null && other.getelseStmt() != null) return false;
            else if (! _elseStmt.equals(other.getelseStmt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getExpression().hashCode());
            hash = hash * 31 + (getthenStmt().hashCode());
            hash = hash * 31 + (getelseStmt() == null ? 0 : getelseStmt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 168:  SwitchStatement ::= switch ( Expression ) SwitchBlock
     *</b>
     */
    static public class SwitchStatement extends Ast implements ISwitchStatement
    {
        private IExpression _Expression;
        private SwitchBlock _SwitchBlock;

        public IExpression getExpression() { return _Expression; }
        public SwitchBlock getSwitchBlock() { return _SwitchBlock; }

        public SwitchStatement(IToken leftIToken, IToken rightIToken,
                               IExpression _Expression,
                               SwitchBlock _SwitchBlock)
        {
            super(leftIToken, rightIToken);

            this._Expression = _Expression;
            this._SwitchBlock = _SwitchBlock;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof SwitchStatement)) return false;
            SwitchStatement other = (SwitchStatement    ) o;
            if (! _Expression.equals(other.getExpression())) return false;
            if (! _SwitchBlock.equals(other.getSwitchBlock())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getExpression().hashCode());
            hash = hash * 31 + (getSwitchBlock().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 169:  SwitchBlock ::= { SwitchLabelsopt }
     *<li>Rule 170:  SwitchBlock ::= { SwitchBlockStatements SwitchLabelsopt }
     *</b>
     */
    static public class SwitchBlock extends Ast implements ISwitchBlock
    {
        private JavaParser environment;
        public JavaParser getEnvironment() { return environment; }

        private SwitchLabelList _SwitchLabelsopt;
        private SwitchBlockStatementList _SwitchBlockStatements;

        public SwitchLabelList getSwitchLabelsopt() { return _SwitchLabelsopt; }
        /**
         * The value returned by <b>getSwitchBlockStatements</b> may be <b>null</b>
         */
        public SwitchBlockStatementList getSwitchBlockStatements() { return _SwitchBlockStatements; }

        public SwitchBlock(JavaParser environment, IToken leftIToken, IToken rightIToken,
                           SwitchLabelList _SwitchLabelsopt,
                           SwitchBlockStatementList _SwitchBlockStatements)
        {
            super(leftIToken, rightIToken);

            this.environment = environment;
            this._SwitchLabelsopt = _SwitchLabelsopt;
            this._SwitchBlockStatements = _SwitchBlockStatements;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof SwitchBlock)) return false;
            SwitchBlock other = (SwitchBlock    ) o;
            if (! _SwitchLabelsopt.equals(other.getSwitchLabelsopt())) return false;
            if (_SwitchBlockStatements == null && other.getSwitchBlockStatements() != null) return false;
            else if (! _SwitchBlockStatements.equals(other.getSwitchBlockStatements())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getSwitchLabelsopt().hashCode());
            hash = hash * 31 + (getSwitchBlockStatements() == null ? 0 : getSwitchBlockStatements().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }

        public void initialize()
        {
	        if (_SwitchBlockStatements == null)
	        {
	            IToken left = getLeftIToken(),
	            		right = getRightIToken();
	            _SwitchBlockStatements = new SwitchBlockStatementList(left, right, true);
	        }
        }
    }

    /**
     *<b>
     *<li>Rule 171:  SwitchBlockStatements ::= SwitchBlockStatement
     *<li>Rule 172:  SwitchBlockStatements ::= SwitchBlockStatements SwitchBlockStatement
     *<li>Rule 348:  SwitchBlockStatementsopt ::= $Empty
     *<li>Rule 349:  SwitchBlockStatementsopt ::= SwitchBlockStatements
     *</b>
     */
    static public class SwitchBlockStatementList extends AstList implements ISwitchBlockStatements, ISwitchBlockStatementsopt
    {
        public SwitchBlockStatement getSwitchBlockStatementAt(int i) { return (SwitchBlockStatement) getElementAt(i); }

        public SwitchBlockStatementList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public SwitchBlockStatementList(SwitchBlockStatement _SwitchBlockStatement, boolean leftRecursive)
        {
            super((Ast) _SwitchBlockStatement, leftRecursive);
            initialize();
        }

        public void add(SwitchBlockStatement _SwitchBlockStatement)
        {
            super.add((Ast) _SwitchBlockStatement);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof SwitchBlockStatementList)) return false;
            SwitchBlockStatementList other = (SwitchBlockStatementList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                SwitchBlockStatement element = getSwitchBlockStatementAt(i);
                    if (! element.equals(other.getSwitchBlockStatementAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getSwitchBlockStatementAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) v.visit(getSwitchBlockStatementAt(i)); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) v.visit(getSwitchBlockStatementAt(i), o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) v.visit(getSwitchBlockStatementAt(i)); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) v.visit(getSwitchBlockStatementAt(i), o); return null; }
    }

    /**
     *<b>
     *<li>Rule 173:  SwitchBlockStatement ::= SwitchLabels BlockStatements
     *</b>
     */
    static public class SwitchBlockStatement extends Ast implements ISwitchBlockStatement
    {
        private SwitchLabelList _SwitchLabels;
        private BlockStatementList _BlockStatements;

        public SwitchLabelList getSwitchLabels() { return _SwitchLabels; }
        public BlockStatementList getBlockStatements() { return _BlockStatements; }

        public SwitchBlockStatement(IToken leftIToken, IToken rightIToken,
                                    SwitchLabelList _SwitchLabels,
                                    BlockStatementList _BlockStatements)
        {
            super(leftIToken, rightIToken);

            this._SwitchLabels = _SwitchLabels;
            this._BlockStatements = _BlockStatements;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof SwitchBlockStatement)) return false;
            SwitchBlockStatement other = (SwitchBlockStatement    ) o;
            if (! _SwitchLabels.equals(other.getSwitchLabels())) return false;
            if (! _BlockStatements.equals(other.getBlockStatements())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getSwitchLabels().hashCode());
            hash = hash * 31 + (getBlockStatements().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 174:  SwitchLabels ::= SwitchLabel
     *<li>Rule 175:  SwitchLabels ::= SwitchLabels SwitchLabel
     *<li>Rule 350:  SwitchLabelsopt ::= $Empty
     *<li>Rule 351:  SwitchLabelsopt ::= SwitchLabels
     *</b>
     */
    static public class SwitchLabelList extends AstList implements ISwitchLabels, ISwitchLabelsopt
    {
        public ISwitchLabel getSwitchLabelAt(int i) { return (ISwitchLabel) getElementAt(i); }

        public SwitchLabelList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public SwitchLabelList(ISwitchLabel _SwitchLabel, boolean leftRecursive)
        {
            super((Ast) _SwitchLabel, leftRecursive);
            initialize();
        }

        public void add(ISwitchLabel _SwitchLabel)
        {
            super.add((Ast) _SwitchLabel);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof SwitchLabelList)) return false;
            SwitchLabelList other = (SwitchLabelList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                ISwitchLabel element = getSwitchLabelAt(i);
                    if (! element.equals(other.getSwitchLabelAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getSwitchLabelAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getSwitchLabelAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getSwitchLabelAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getSwitchLabelAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getSwitchLabelAt(i).accept(v, o); return null; }
    }

    /**
     *<b>
     *<li>Rule 176:  SwitchLabel ::= case ConstantExpression :
     *</b>
     */
    static public class CaseLabel extends Ast implements ISwitchLabel
    {
        private IConstantExpression _ConstantExpression;

        public IConstantExpression getConstantExpression() { return _ConstantExpression; }

        public CaseLabel(IToken leftIToken, IToken rightIToken,
                         IConstantExpression _ConstantExpression)
        {
            super(leftIToken, rightIToken);

            this._ConstantExpression = _ConstantExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof CaseLabel)) return false;
            CaseLabel other = (CaseLabel    ) o;
            if (! _ConstantExpression.equals(other.getConstantExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getConstantExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 177:  SwitchLabel ::= default :
     *</b>
     */
    static public class DefaultLabel extends Ast implements ISwitchLabel
    {
        public DefaultLabel(IToken leftIToken, IToken rightIToken)
        {
            super(leftIToken, rightIToken);

            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof DefaultLabel)) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 178:  WhileStatement ::= while ( Expression ) Statement
     *<li>Rule 179:  WhileStatementNoShortIf ::= while ( Expression ) StatementNoShortIf$Statement
     *</b>
     */
    static public class WhileStatement extends Ast implements IWhileStatement, IWhileStatementNoShortIf
    {
        private IExpression _Expression;
        private Ast _Statement;

        public IExpression getExpression() { return _Expression; }
        public Ast getStatement() { return _Statement; }

        public WhileStatement(IToken leftIToken, IToken rightIToken,
                              IExpression _Expression,
                              Ast _Statement)
        {
            super(leftIToken, rightIToken);

            this._Expression = _Expression;
            this._Statement = _Statement;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof WhileStatement)) return false;
            WhileStatement other = (WhileStatement    ) o;
            if (! _Expression.equals(other.getExpression())) return false;
            if (! _Statement.equals(other.getStatement())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getExpression().hashCode());
            hash = hash * 31 + (getStatement().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 180:  DoStatement ::= do Statement while ( Expression ) ;
     *</b>
     */
    static public class DoStatement extends Ast implements IDoStatement
    {
        private IStatement _Statement;
        private IExpression _Expression;

        public IStatement getStatement() { return _Statement; }
        public IExpression getExpression() { return _Expression; }

        public DoStatement(IToken leftIToken, IToken rightIToken,
                           IStatement _Statement,
                           IExpression _Expression)
        {
            super(leftIToken, rightIToken);

            this._Statement = _Statement;
            this._Expression = _Expression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof DoStatement)) return false;
            DoStatement other = (DoStatement    ) o;
            if (! _Statement.equals(other.getStatement())) return false;
            if (! _Expression.equals(other.getExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getStatement().hashCode());
            hash = hash * 31 + (getExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 181:  ForStatement ::= for ( ForInitopt ; Expressionopt ; ForUpdateopt ) Statement
     *<li>Rule 182:  ForStatementNoShortIf ::= for ( ForInitopt ; Expressionopt ; ForUpdateopt ) StatementNoShortIf$Statement
     *</b>
     */
    static public class ForStatement extends Ast implements IForStatement, IForStatementNoShortIf
    {
        private IForInitopt _ForInitopt;
        private IExpressionopt _Expressionopt;
        private StatementExpressionList _ForUpdateopt;
        private Ast _Statement;

        /**
         * The value returned by <b>getForInitopt</b> may be <b>null</b>
         */
        public IForInitopt getForInitopt() { return _ForInitopt; }
        /**
         * The value returned by <b>getExpressionopt</b> may be <b>null</b>
         */
        public IExpressionopt getExpressionopt() { return _Expressionopt; }
        public StatementExpressionList getForUpdateopt() { return _ForUpdateopt; }
        public Ast getStatement() { return _Statement; }

        public ForStatement(IToken leftIToken, IToken rightIToken,
                            IForInitopt _ForInitopt,
                            IExpressionopt _Expressionopt,
                            StatementExpressionList _ForUpdateopt,
                            Ast _Statement)
        {
            super(leftIToken, rightIToken);

            this._ForInitopt = _ForInitopt;
            this._Expressionopt = _Expressionopt;
            this._ForUpdateopt = _ForUpdateopt;
            this._Statement = _Statement;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ForStatement)) return false;
            ForStatement other = (ForStatement    ) o;
            if (_ForInitopt == null && other.getForInitopt() != null) return false;
            else if (! _ForInitopt.equals(other.getForInitopt())) return false;
            if (_Expressionopt == null && other.getExpressionopt() != null) return false;
            else if (! _Expressionopt.equals(other.getExpressionopt())) return false;
            if (! _ForUpdateopt.equals(other.getForUpdateopt())) return false;
            if (! _Statement.equals(other.getStatement())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getForInitopt() == null ? 0 : getForInitopt().hashCode());
            hash = hash * 31 + (getExpressionopt() == null ? 0 : getExpressionopt().hashCode());
            hash = hash * 31 + (getForUpdateopt().hashCode());
            hash = hash * 31 + (getStatement().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 185:  ForUpdate ::= StatementExpressionList
     *<li>Rule 186:  StatementExpressionList ::= StatementExpression
     *<li>Rule 187:  StatementExpressionList ::= StatementExpressionList , StatementExpression
     *<li>Rule 340:  ForUpdateopt ::= $Empty
     *<li>Rule 341:  ForUpdateopt ::= ForUpdate
     *</b>
     */
    static public class StatementExpressionList extends AstList implements IForUpdate, IStatementExpressionList, IForUpdateopt
    {
        public IStatementExpression getStatementExpressionAt(int i) { return (IStatementExpression) getElementAt(i); }

        public StatementExpressionList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public StatementExpressionList(IStatementExpression _StatementExpression, boolean leftRecursive)
        {
            super((Ast) _StatementExpression, leftRecursive);
            initialize();
        }

        public void add(IStatementExpression _StatementExpression)
        {
            super.add((Ast) _StatementExpression);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof StatementExpressionList)) return false;
            StatementExpressionList other = (StatementExpressionList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                IStatementExpression element = getStatementExpressionAt(i);
                    if (! element.equals(other.getStatementExpressionAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getStatementExpressionAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getStatementExpressionAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getStatementExpressionAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getStatementExpressionAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getStatementExpressionAt(i).accept(v, o); return null; }
    }

    /**
     *<b>
     *<li>Rule 188:  BreakStatement ::= break IDENTIFIERopt ;
     *</b>
     */
    static public class BreakStatement extends Ast implements IBreakStatement
    {
        private IDENTIFIERopt _IDENTIFIERopt;

        /**
         * The value returned by <b>getIDENTIFIERopt</b> may be <b>null</b>
         */
        public IDENTIFIERopt getIDENTIFIERopt() { return _IDENTIFIERopt; }

        public BreakStatement(IToken leftIToken, IToken rightIToken,
                              IDENTIFIERopt _IDENTIFIERopt)
        {
            super(leftIToken, rightIToken);

            this._IDENTIFIERopt = _IDENTIFIERopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof BreakStatement)) return false;
            BreakStatement other = (BreakStatement    ) o;
            if (_IDENTIFIERopt == null && other.getIDENTIFIERopt() != null) return false;
            else if (! _IDENTIFIERopt.equals(other.getIDENTIFIERopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getIDENTIFIERopt() == null ? 0 : getIDENTIFIERopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 189:  ContinueStatement ::= continue IDENTIFIERopt ;
     *</b>
     */
    static public class ContinueStatement extends Ast implements IContinueStatement
    {
        private IDENTIFIERopt _IDENTIFIERopt;

        /**
         * The value returned by <b>getIDENTIFIERopt</b> may be <b>null</b>
         */
        public IDENTIFIERopt getIDENTIFIERopt() { return _IDENTIFIERopt; }

        public ContinueStatement(IToken leftIToken, IToken rightIToken,
                                 IDENTIFIERopt _IDENTIFIERopt)
        {
            super(leftIToken, rightIToken);

            this._IDENTIFIERopt = _IDENTIFIERopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ContinueStatement)) return false;
            ContinueStatement other = (ContinueStatement    ) o;
            if (_IDENTIFIERopt == null && other.getIDENTIFIERopt() != null) return false;
            else if (! _IDENTIFIERopt.equals(other.getIDENTIFIERopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getIDENTIFIERopt() == null ? 0 : getIDENTIFIERopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 190:  ReturnStatement ::= return Expressionopt ;
     *</b>
     */
    static public class ReturnStatement extends Ast implements IReturnStatement
    {
        private IExpressionopt _Expressionopt;

        /**
         * The value returned by <b>getExpressionopt</b> may be <b>null</b>
         */
        public IExpressionopt getExpressionopt() { return _Expressionopt; }

        public ReturnStatement(IToken leftIToken, IToken rightIToken,
                               IExpressionopt _Expressionopt)
        {
            super(leftIToken, rightIToken);

            this._Expressionopt = _Expressionopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ReturnStatement)) return false;
            ReturnStatement other = (ReturnStatement    ) o;
            if (_Expressionopt == null && other.getExpressionopt() != null) return false;
            else if (! _Expressionopt.equals(other.getExpressionopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getExpressionopt() == null ? 0 : getExpressionopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 191:  ThrowStatement ::= throw Expression ;
     *</b>
     */
    static public class ThrowStatement extends Ast implements IThrowStatement
    {
        private IExpression _Expression;

        public IExpression getExpression() { return _Expression; }

        public ThrowStatement(IToken leftIToken, IToken rightIToken,
                              IExpression _Expression)
        {
            super(leftIToken, rightIToken);

            this._Expression = _Expression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ThrowStatement)) return false;
            ThrowStatement other = (ThrowStatement    ) o;
            if (! _Expression.equals(other.getExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 192:  SynchronizedStatement ::= synchronized ( Expression ) Block
     *</b>
     */
    static public class SynchronizedStatement extends Ast implements ISynchronizedStatement
    {
        private IExpression _Expression;
        private Block _Block;

        public IExpression getExpression() { return _Expression; }
        public Block getBlock() { return _Block; }

        public SynchronizedStatement(IToken leftIToken, IToken rightIToken,
                                     IExpression _Expression,
                                     Block _Block)
        {
            super(leftIToken, rightIToken);

            this._Expression = _Expression;
            this._Block = _Block;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof SynchronizedStatement)) return false;
            SynchronizedStatement other = (SynchronizedStatement    ) o;
            if (! _Expression.equals(other.getExpression())) return false;
            if (! _Block.equals(other.getBlock())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getExpression().hashCode());
            hash = hash * 31 + (getBlock().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 193:  TryStatement ::= try Block Catches$Catchesopt
     *<li>Rule 194:  TryStatement ::= try Block Catchesopt Finally
     *</b>
     */
    static public class TryStatement extends Ast implements ITryStatement
    {
        private Block _Block;
        private Ast _Catchesopt;
        private Finally _Finally;

        public Block getBlock() { return _Block; }
        public Ast getCatchesopt() { return _Catchesopt; }
        /**
         * The value returned by <b>getFinally</b> may be <b>null</b>
         */
        public Finally getFinally() { return _Finally; }

        public TryStatement(IToken leftIToken, IToken rightIToken,
                            Block _Block,
                            Ast _Catchesopt,
                            Finally _Finally)
        {
            super(leftIToken, rightIToken);

            this._Block = _Block;
            this._Catchesopt = _Catchesopt;
            this._Finally = _Finally;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof TryStatement)) return false;
            TryStatement other = (TryStatement    ) o;
            if (! _Block.equals(other.getBlock())) return false;
            if (! _Catchesopt.equals(other.getCatchesopt())) return false;
            if (_Finally == null && other.getFinally() != null) return false;
            else if (! _Finally.equals(other.getFinally())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getBlock().hashCode());
            hash = hash * 31 + (getCatchesopt().hashCode());
            hash = hash * 31 + (getFinally() == null ? 0 : getFinally().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 195:  Catches ::= CatchClause
     *<li>Rule 196:  Catches ::= Catches CatchClause
     *<li>Rule 344:  Catchesopt ::= $Empty
     *<li>Rule 345:  Catchesopt ::= Catches
     *</b>
     */
    static public class CatchClauseList extends AstList implements ICatches, ICatchesopt
    {
        public CatchClause getCatchClauseAt(int i) { return (CatchClause) getElementAt(i); }

        public CatchClauseList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public CatchClauseList(CatchClause _CatchClause, boolean leftRecursive)
        {
            super((Ast) _CatchClause, leftRecursive);
            initialize();
        }

        public void add(CatchClause _CatchClause)
        {
            super.add((Ast) _CatchClause);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof CatchClauseList)) return false;
            CatchClauseList other = (CatchClauseList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                CatchClause element = getCatchClauseAt(i);
                    if (! element.equals(other.getCatchClauseAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getCatchClauseAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) v.visit(getCatchClauseAt(i)); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) v.visit(getCatchClauseAt(i), o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) v.visit(getCatchClauseAt(i)); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) v.visit(getCatchClauseAt(i), o); return null; }
    }

    /**
     *<b>
     *<li>Rule 197:  CatchClause ::= catch ( FormalParameter ) Block
     *</b>
     */
    static public class CatchClause extends Ast implements ICatchClause
    {
        private FormalParameter _FormalParameter;
        private Block _Block;

        public FormalParameter getFormalParameter() { return _FormalParameter; }
        public Block getBlock() { return _Block; }

        public CatchClause(IToken leftIToken, IToken rightIToken,
                           FormalParameter _FormalParameter,
                           Block _Block)
        {
            super(leftIToken, rightIToken);

            this._FormalParameter = _FormalParameter;
            this._Block = _Block;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof CatchClause)) return false;
            CatchClause other = (CatchClause    ) o;
            if (! _FormalParameter.equals(other.getFormalParameter())) return false;
            if (! _Block.equals(other.getBlock())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getFormalParameter().hashCode());
            hash = hash * 31 + (getBlock().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 198:  Finally ::= finally Block
     *</b>
     */
    static public class Finally extends Ast implements IFinally
    {
        private Block _Block;

        public Block getBlock() { return _Block; }

        public Finally(IToken leftIToken, IToken rightIToken,
                       Block _Block)
        {
            super(leftIToken, rightIToken);

            this._Block = _Block;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof Finally)) return false;
            Finally other = (Finally    ) o;
            if (! _Block.equals(other.getBlock())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getBlock().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 206:  PrimaryNoNewArray ::= ( Expression )
     *</b>
     */
    static public class ParenthesizedExpression extends Ast implements IPrimaryNoNewArray
    {
        private IExpression _Expression;

        public IExpression getExpression() { return _Expression; }

        public ParenthesizedExpression(IToken leftIToken, IToken rightIToken,
                                       IExpression _Expression)
        {
            super(leftIToken, rightIToken);

            this._Expression = _Expression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ParenthesizedExpression)) return false;
            ParenthesizedExpression other = (ParenthesizedExpression    ) o;
            if (! _Expression.equals(other.getExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 207:  PrimaryNoNewArray ::= this
     *<li>Rule 208:  PrimaryNoNewArray ::= Name . this
     *</b>
     */
    static public class PrimaryThis extends Ast implements IPrimaryNoNewArray
    {
        private IName _Name;

        /**
         * The value returned by <b>getName</b> may be <b>null</b>
         */
        public IName getName() { return _Name; }

        public PrimaryThis(IToken leftIToken, IToken rightIToken,
                           IName _Name)
        {
            super(leftIToken, rightIToken);

            this._Name = _Name;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof PrimaryThis)) return false;
            PrimaryThis other = (PrimaryThis    ) o;
            if (_Name == null && other.getName() != null) return false;
            else if (! _Name.equals(other.getName())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getName() == null ? 0 : getName().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 209:  PrimaryNoNewArray ::= Type . class
     *</b>
     */
    static public class PrimaryClassLiteral extends Ast implements IPrimaryNoNewArray
    {
        private IType _Type;

        public IType getType() { return _Type; }

        public PrimaryClassLiteral(IToken leftIToken, IToken rightIToken,
                                   IType _Type)
        {
            super(leftIToken, rightIToken);

            this._Type = _Type;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof PrimaryClassLiteral)) return false;
            PrimaryClassLiteral other = (PrimaryClassLiteral    ) o;
            if (! _Type.equals(other.getType())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getType().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 210:  PrimaryNoNewArray ::= void . class
     *</b>
     */
    static public class PrimaryVoidClassLiteral extends Ast implements IPrimaryNoNewArray
    {
        public PrimaryVoidClassLiteral(IToken leftIToken, IToken rightIToken)
        {
            super(leftIToken, rightIToken);

            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof PrimaryVoidClassLiteral)) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 211:  ClassInstanceCreationExpression ::= new ClassType ( ArgumentListopt ) ClassBodyopt
     *<li>Rule 212:  ClassInstanceCreationExpression ::= Primary$expression . new SimpleName$ClassType ( ArgumentListopt ) ClassBodyopt
     *<li>Rule 213:  ClassInstanceCreationExpression ::= Name$expression . new SimpleName$ClassType ( ArgumentListopt ) ClassBodyopt
     *</b>
     */
    static public class ClassInstanceCreationExpression extends Ast implements IClassInstanceCreationExpression
    {
        private IClassType _ClassType;
        private ExpressionList _ArgumentListopt;
        private ClassBody _ClassBodyopt;
        private IPostfixExpression _expression;

        public IClassType getClassType() { return _ClassType; }
        public ExpressionList getArgumentListopt() { return _ArgumentListopt; }
        /**
         * The value returned by <b>getClassBodyopt</b> may be <b>null</b>
         */
        public ClassBody getClassBodyopt() { return _ClassBodyopt; }
        /**
         * The value returned by <b>getexpression</b> may be <b>null</b>
         */
        public IPostfixExpression getexpression() { return _expression; }

        public ClassInstanceCreationExpression(IToken leftIToken, IToken rightIToken,
                                               IClassType _ClassType,
                                               ExpressionList _ArgumentListopt,
                                               ClassBody _ClassBodyopt,
                                               IPostfixExpression _expression)
        {
            super(leftIToken, rightIToken);

            this._ClassType = _ClassType;
            this._ArgumentListopt = _ArgumentListopt;
            this._ClassBodyopt = _ClassBodyopt;
            this._expression = _expression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ClassInstanceCreationExpression)) return false;
            ClassInstanceCreationExpression other = (ClassInstanceCreationExpression    ) o;
            if (! _ClassType.equals(other.getClassType())) return false;
            if (! _ArgumentListopt.equals(other.getArgumentListopt())) return false;
            if (_ClassBodyopt == null && other.getClassBodyopt() != null) return false;
            else if (! _ClassBodyopt.equals(other.getClassBodyopt())) return false;
            if (_expression == null && other.getexpression() != null) return false;
            else if (! _expression.equals(other.getexpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getClassType().hashCode());
            hash = hash * 31 + (getArgumentListopt().hashCode());
            hash = hash * 31 + (getClassBodyopt() == null ? 0 : getClassBodyopt().hashCode());
            hash = hash * 31 + (getexpression() == null ? 0 : getexpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 214:  ArgumentList ::= Expression
     *<li>Rule 215:  ArgumentList ::= ArgumentList , Expression
     *<li>Rule 328:  ArgumentListopt ::= $Empty
     *<li>Rule 329:  ArgumentListopt ::= ArgumentList
     *</b>
     */
    static public class ExpressionList extends AstList implements IArgumentList, IArgumentListopt
    {
        public IExpression getExpressionAt(int i) { return (IExpression) getElementAt(i); }

        public ExpressionList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public ExpressionList(IExpression _Expression, boolean leftRecursive)
        {
            super((Ast) _Expression, leftRecursive);
            initialize();
        }

        public void add(IExpression _Expression)
        {
            super.add((Ast) _Expression);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof ExpressionList)) return false;
            ExpressionList other = (ExpressionList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                IExpression element = getExpressionAt(i);
                    if (! element.equals(other.getExpressionAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getExpressionAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) getExpressionAt(i).accept(v); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getExpressionAt(i).accept(v, o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) getExpressionAt(i).accept(v); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) getExpressionAt(i).accept(v, o); return null; }
    }

    /**
     *<b>
     *<li>Rule 216:  ArrayCreationExpression ::= new PrimitiveType$Type DimExprs Dimsopt
     *<li>Rule 217:  ArrayCreationExpression ::= new ClassOrInterfaceType$Type DimExprs Dimsopt
     *<li>Rule 218:  ArrayCreationExpression ::= new ArrayType$Type ArrayInitializer
     *</b>
     */
    static public class ArrayCreationExpression extends Ast implements IArrayCreationExpression
    {
        private IType _Type;
        private DimExprList _DimExprs;
        private DimList _Dimsopt;
        private ArrayInitializer _ArrayInitializer;

        public IType getType() { return _Type; }
        /**
         * The value returned by <b>getDimExprs</b> may be <b>null</b>
         */
        public DimExprList getDimExprs() { return _DimExprs; }
        /**
         * The value returned by <b>getDimsopt</b> may be <b>null</b>
         */
        public DimList getDimsopt() { return _Dimsopt; }
        /**
         * The value returned by <b>getArrayInitializer</b> may be <b>null</b>
         */
        public ArrayInitializer getArrayInitializer() { return _ArrayInitializer; }

        public ArrayCreationExpression(IToken leftIToken, IToken rightIToken,
                                       IType _Type,
                                       DimExprList _DimExprs,
                                       DimList _Dimsopt,
                                       ArrayInitializer _ArrayInitializer)
        {
            super(leftIToken, rightIToken);

            this._Type = _Type;
            this._DimExprs = _DimExprs;
            this._Dimsopt = _Dimsopt;
            this._ArrayInitializer = _ArrayInitializer;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ArrayCreationExpression)) return false;
            ArrayCreationExpression other = (ArrayCreationExpression    ) o;
            if (! _Type.equals(other.getType())) return false;
            if (_DimExprs == null && other.getDimExprs() != null) return false;
            else if (! _DimExprs.equals(other.getDimExprs())) return false;
            if (_Dimsopt == null && other.getDimsopt() != null) return false;
            else if (! _Dimsopt.equals(other.getDimsopt())) return false;
            if (_ArrayInitializer == null && other.getArrayInitializer() != null) return false;
            else if (! _ArrayInitializer.equals(other.getArrayInitializer())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getType().hashCode());
            hash = hash * 31 + (getDimExprs() == null ? 0 : getDimExprs().hashCode());
            hash = hash * 31 + (getDimsopt() == null ? 0 : getDimsopt().hashCode());
            hash = hash * 31 + (getArrayInitializer() == null ? 0 : getArrayInitializer().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 219:  DimExprs ::= DimExpr
     *<li>Rule 220:  DimExprs ::= DimExprs DimExpr
     *</b>
     */
    static public class DimExprList extends AstList implements IDimExprs
    {
        public DimExpr getDimExprAt(int i) { return (DimExpr) getElementAt(i); }

        public DimExprList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public DimExprList(DimExpr _DimExpr, boolean leftRecursive)
        {
            super((Ast) _DimExpr, leftRecursive);
            initialize();
        }

        public void add(DimExpr _DimExpr)
        {
            super.add((Ast) _DimExpr);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof DimExprList)) return false;
            DimExprList other = (DimExprList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                DimExpr element = getDimExprAt(i);
                    if (! element.equals(other.getDimExprAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getDimExprAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) v.visit(getDimExprAt(i)); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) v.visit(getDimExprAt(i), o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) v.visit(getDimExprAt(i)); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) v.visit(getDimExprAt(i), o); return null; }
    }

    /**
     *<b>
     *<li>Rule 221:  DimExpr ::= [ Expression ]
     *</b>
     */
    static public class DimExpr extends Ast implements IDimExpr
    {
        private IExpression _Expression;

        public IExpression getExpression() { return _Expression; }

        public DimExpr(IToken leftIToken, IToken rightIToken,
                       IExpression _Expression)
        {
            super(leftIToken, rightIToken);

            this._Expression = _Expression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof DimExpr)) return false;
            DimExpr other = (DimExpr    ) o;
            if (! _Expression.equals(other.getExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 222:  Dims ::= Dim
     *<li>Rule 223:  Dims ::= Dims Dim
     *<li>Rule 326:  Dimsopt ::= $Empty
     *<li>Rule 327:  Dimsopt ::= Dims
     *</b>
     */
    static public class DimList extends AstList implements IDims, IDimsopt
    {
        public Dim getDimAt(int i) { return (Dim) getElementAt(i); }

        public DimList(IToken leftIToken, IToken rightIToken, boolean leftRecursive)
        {
            super(leftIToken, rightIToken, leftRecursive);
            initialize();
        }

        public DimList(Dim _Dim, boolean leftRecursive)
        {
            super((Ast) _Dim, leftRecursive);
            initialize();
        }

        public void add(Dim _Dim)
        {
            super.add((Ast) _Dim);
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (! (o instanceof DimList)) return false;
            DimList other = (DimList    ) o;
            if (size() != other.size()) return false;
            for (int i = 0; i < size(); i++)
            {
                Dim element = getDimAt(i);
                    if (! element.equals(other.getDimAt(i))) return false;
            }
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            for (int i = 0; i < size(); i++)
                hash = hash * 31 + (getDimAt(i).hashCode());
            return hash;
        }

        public void accept(Visitor v) { for (int i = 0; i < size(); i++) v.visit(getDimAt(i)); }
        public void accept(ArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) v.visit(getDimAt(i), o); }
        public Object accept(ResultVisitor v) { for (int i = 0; i < size(); i++) v.visit(getDimAt(i)); return null; }
        public Object accept(ResultArgumentVisitor v, Object o) { for (int i = 0; i < size(); i++) v.visit(getDimAt(i), o); return null; }
    }

    /**
     *<b>
     *<li>Rule 224:  Dim ::= [ ]
     *</b>
     */
    static public class Dim extends Ast implements IDim
    {
        public Dim(IToken leftIToken, IToken rightIToken)
        {
            super(leftIToken, rightIToken);

            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof Dim)) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 225:  FieldAccess ::= Primary . IDENTIFIER
     *</b>
     */
    static public class FieldAccess extends Ast implements IFieldAccess
    {
        private IPrimary _Primary;

        public IPrimary getPrimary() { return _Primary; }

        public FieldAccess(IToken leftIToken, IToken rightIToken,
                           IPrimary _Primary)
        {
            super(leftIToken, rightIToken);

            this._Primary = _Primary;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof FieldAccess)) return false;
            FieldAccess other = (FieldAccess    ) o;
            if (! _Primary.equals(other.getPrimary())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getPrimary().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 226:  FieldAccess ::= super . IDENTIFIER
     *<li>Rule 227:  FieldAccess ::= Name . super . IDENTIFIER
     *</b>
     */
    static public class SuperFieldAccess extends Ast implements IFieldAccess
    {
        private IName _Name;

        /**
         * The value returned by <b>getName</b> may be <b>null</b>
         */
        public IName getName() { return _Name; }

        public SuperFieldAccess(IToken leftIToken, IToken rightIToken,
                                IName _Name)
        {
            super(leftIToken, rightIToken);

            this._Name = _Name;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof SuperFieldAccess)) return false;
            SuperFieldAccess other = (SuperFieldAccess    ) o;
            if (_Name == null && other.getName() != null) return false;
            else if (! _Name.equals(other.getName())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getName() == null ? 0 : getName().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 228:  MethodInvocation ::= Name ( ArgumentListopt )
     *</b>
     */
    static public class MethodInvocation extends Ast implements IMethodInvocation
    {
        private IName _Name;
        private ExpressionList _ArgumentListopt;

        public IName getName() { return _Name; }
        public ExpressionList getArgumentListopt() { return _ArgumentListopt; }

        public MethodInvocation(IToken leftIToken, IToken rightIToken,
                                IName _Name,
                                ExpressionList _ArgumentListopt)
        {
            super(leftIToken, rightIToken);

            this._Name = _Name;
            this._ArgumentListopt = _ArgumentListopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof MethodInvocation)) return false;
            MethodInvocation other = (MethodInvocation    ) o;
            if (! _Name.equals(other.getName())) return false;
            if (! _ArgumentListopt.equals(other.getArgumentListopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getName().hashCode());
            hash = hash * 31 + (getArgumentListopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 229:  MethodInvocation ::= Primary . IDENTIFIER ( ArgumentListopt )
     *</b>
     */
    static public class PrimaryMethodInvocation extends Ast implements IMethodInvocation
    {
        private IPrimary _Primary;
        private ExpressionList _ArgumentListopt;

        public IPrimary getPrimary() { return _Primary; }
        public ExpressionList getArgumentListopt() { return _ArgumentListopt; }

        public PrimaryMethodInvocation(IToken leftIToken, IToken rightIToken,
                                       IPrimary _Primary,
                                       ExpressionList _ArgumentListopt)
        {
            super(leftIToken, rightIToken);

            this._Primary = _Primary;
            this._ArgumentListopt = _ArgumentListopt;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof PrimaryMethodInvocation)) return false;
            PrimaryMethodInvocation other = (PrimaryMethodInvocation    ) o;
            if (! _Primary.equals(other.getPrimary())) return false;
            if (! _ArgumentListopt.equals(other.getArgumentListopt())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getPrimary().hashCode());
            hash = hash * 31 + (getArgumentListopt().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 230:  MethodInvocation ::= super . IDENTIFIER ( ArgumentListopt )
     *<li>Rule 231:  MethodInvocation ::= Name . super . IDENTIFIER ( ArgumentListopt )
     *</b>
     */
    static public class SuperMethodInvocation extends Ast implements IMethodInvocation
    {
        private ExpressionList _ArgumentListopt;
        private IName _Name;

        public ExpressionList getArgumentListopt() { return _ArgumentListopt; }
        /**
         * The value returned by <b>getName</b> may be <b>null</b>
         */
        public IName getName() { return _Name; }

        public SuperMethodInvocation(IToken leftIToken, IToken rightIToken,
                                     ExpressionList _ArgumentListopt,
                                     IName _Name)
        {
            super(leftIToken, rightIToken);

            this._ArgumentListopt = _ArgumentListopt;
            this._Name = _Name;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof SuperMethodInvocation)) return false;
            SuperMethodInvocation other = (SuperMethodInvocation    ) o;
            if (! _ArgumentListopt.equals(other.getArgumentListopt())) return false;
            if (_Name == null && other.getName() != null) return false;
            else if (! _Name.equals(other.getName())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getArgumentListopt().hashCode());
            hash = hash * 31 + (getName() == null ? 0 : getName().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 232:  ArrayAccess ::= Name$Base [ Expression ]
     *<li>Rule 233:  ArrayAccess ::= PrimaryNoNewArray$Base [ Expression ]
     *</b>
     */
    static public class ArrayAccess extends Ast implements IArrayAccess
    {
        private IPostfixExpression _Base;
        private IExpression _Expression;

        public IPostfixExpression getBase() { return _Base; }
        public IExpression getExpression() { return _Expression; }

        public ArrayAccess(IToken leftIToken, IToken rightIToken,
                           IPostfixExpression _Base,
                           IExpression _Expression)
        {
            super(leftIToken, rightIToken);

            this._Base = _Base;
            this._Expression = _Expression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ArrayAccess)) return false;
            ArrayAccess other = (ArrayAccess    ) o;
            if (! _Base.equals(other.getBase())) return false;
            if (! _Expression.equals(other.getExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getBase().hashCode());
            hash = hash * 31 + (getExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 238:  PostIncrementExpression ::= PostfixExpression ++
     *</b>
     */
    static public class PostIncrementExpression extends Ast implements IPostIncrementExpression
    {
        private IPostfixExpression _PostfixExpression;

        public IPostfixExpression getPostfixExpression() { return _PostfixExpression; }

        public PostIncrementExpression(IToken leftIToken, IToken rightIToken,
                                       IPostfixExpression _PostfixExpression)
        {
            super(leftIToken, rightIToken);

            this._PostfixExpression = _PostfixExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof PostIncrementExpression)) return false;
            PostIncrementExpression other = (PostIncrementExpression    ) o;
            if (! _PostfixExpression.equals(other.getPostfixExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getPostfixExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 239:  PostDecrementExpression ::= PostfixExpression --
     *</b>
     */
    static public class PostDecrementExpression extends Ast implements IPostDecrementExpression
    {
        private IPostfixExpression _PostfixExpression;

        public IPostfixExpression getPostfixExpression() { return _PostfixExpression; }

        public PostDecrementExpression(IToken leftIToken, IToken rightIToken,
                                       IPostfixExpression _PostfixExpression)
        {
            super(leftIToken, rightIToken);

            this._PostfixExpression = _PostfixExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof PostDecrementExpression)) return false;
            PostDecrementExpression other = (PostDecrementExpression    ) o;
            if (! _PostfixExpression.equals(other.getPostfixExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getPostfixExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 243:  UnaryExpression ::= + UnaryExpression
     *</b>
     */
    static public class PlusUnaryExpression extends Ast implements IUnaryExpression
    {
        private IUnaryExpression _UnaryExpression;

        public IUnaryExpression getUnaryExpression() { return _UnaryExpression; }

        public PlusUnaryExpression(IToken leftIToken, IToken rightIToken,
                                   IUnaryExpression _UnaryExpression)
        {
            super(leftIToken, rightIToken);

            this._UnaryExpression = _UnaryExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof PlusUnaryExpression)) return false;
            PlusUnaryExpression other = (PlusUnaryExpression    ) o;
            if (! _UnaryExpression.equals(other.getUnaryExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getUnaryExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 244:  UnaryExpression ::= - UnaryExpression
     *</b>
     */
    static public class MinusUnaryExpression extends Ast implements IUnaryExpression
    {
        private IUnaryExpression _UnaryExpression;

        public IUnaryExpression getUnaryExpression() { return _UnaryExpression; }

        public MinusUnaryExpression(IToken leftIToken, IToken rightIToken,
                                    IUnaryExpression _UnaryExpression)
        {
            super(leftIToken, rightIToken);

            this._UnaryExpression = _UnaryExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof MinusUnaryExpression)) return false;
            MinusUnaryExpression other = (MinusUnaryExpression    ) o;
            if (! _UnaryExpression.equals(other.getUnaryExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getUnaryExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 245:  PreIncrementExpression ::= ++ UnaryExpression
     *</b>
     */
    static public class PreIncrementExpression extends Ast implements IPreIncrementExpression
    {
        private IUnaryExpression _UnaryExpression;

        public IUnaryExpression getUnaryExpression() { return _UnaryExpression; }

        public PreIncrementExpression(IToken leftIToken, IToken rightIToken,
                                      IUnaryExpression _UnaryExpression)
        {
            super(leftIToken, rightIToken);

            this._UnaryExpression = _UnaryExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof PreIncrementExpression)) return false;
            PreIncrementExpression other = (PreIncrementExpression    ) o;
            if (! _UnaryExpression.equals(other.getUnaryExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getUnaryExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 246:  PreDecrementExpression ::= -- UnaryExpression
     *</b>
     */
    static public class PreDecrementExpression extends Ast implements IPreDecrementExpression
    {
        private IUnaryExpression _UnaryExpression;

        public IUnaryExpression getUnaryExpression() { return _UnaryExpression; }

        public PreDecrementExpression(IToken leftIToken, IToken rightIToken,
                                      IUnaryExpression _UnaryExpression)
        {
            super(leftIToken, rightIToken);

            this._UnaryExpression = _UnaryExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof PreDecrementExpression)) return false;
            PreDecrementExpression other = (PreDecrementExpression    ) o;
            if (! _UnaryExpression.equals(other.getUnaryExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getUnaryExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 249:  UnaryExpressionNotPlusMinus ::= ~ UnaryExpression
     *</b>
     */
    static public class UnaryComplementExpression extends Ast implements IUnaryExpressionNotPlusMinus
    {
        private IUnaryExpression _UnaryExpression;

        public IUnaryExpression getUnaryExpression() { return _UnaryExpression; }

        public UnaryComplementExpression(IToken leftIToken, IToken rightIToken,
                                         IUnaryExpression _UnaryExpression)
        {
            super(leftIToken, rightIToken);

            this._UnaryExpression = _UnaryExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof UnaryComplementExpression)) return false;
            UnaryComplementExpression other = (UnaryComplementExpression    ) o;
            if (! _UnaryExpression.equals(other.getUnaryExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getUnaryExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 250:  UnaryExpressionNotPlusMinus ::= ! UnaryExpression
     *</b>
     */
    static public class UnaryNotExpression extends Ast implements IUnaryExpressionNotPlusMinus
    {
        private IUnaryExpression _UnaryExpression;

        public IUnaryExpression getUnaryExpression() { return _UnaryExpression; }

        public UnaryNotExpression(IToken leftIToken, IToken rightIToken,
                                  IUnaryExpression _UnaryExpression)
        {
            super(leftIToken, rightIToken);

            this._UnaryExpression = _UnaryExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof UnaryNotExpression)) return false;
            UnaryNotExpression other = (UnaryNotExpression    ) o;
            if (! _UnaryExpression.equals(other.getUnaryExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getUnaryExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 251:  CastExpression ::= ( PrimitiveType Dimsopt ) UnaryExpression
     *</b>
     */
    static public class PrimitiveCastExpression extends Ast implements ICastExpression
    {
        private IPrimitiveType _PrimitiveType;
        private DimList _Dimsopt;
        private IUnaryExpression _UnaryExpression;

        public IPrimitiveType getPrimitiveType() { return _PrimitiveType; }
        public DimList getDimsopt() { return _Dimsopt; }
        public IUnaryExpression getUnaryExpression() { return _UnaryExpression; }

        public PrimitiveCastExpression(IToken leftIToken, IToken rightIToken,
                                       IPrimitiveType _PrimitiveType,
                                       DimList _Dimsopt,
                                       IUnaryExpression _UnaryExpression)
        {
            super(leftIToken, rightIToken);

            this._PrimitiveType = _PrimitiveType;
            this._Dimsopt = _Dimsopt;
            this._UnaryExpression = _UnaryExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof PrimitiveCastExpression)) return false;
            PrimitiveCastExpression other = (PrimitiveCastExpression    ) o;
            if (! _PrimitiveType.equals(other.getPrimitiveType())) return false;
            if (! _Dimsopt.equals(other.getDimsopt())) return false;
            if (! _UnaryExpression.equals(other.getUnaryExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getPrimitiveType().hashCode());
            hash = hash * 31 + (getDimsopt().hashCode());
            hash = hash * 31 + (getUnaryExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 252:  CastExpression ::= ( Expression$Name ) UnaryExpressionNotPlusMinus
     *<li>Rule 253:  CastExpression ::= ( Name$Name Dims ) UnaryExpressionNotPlusMinus
     *</b>
     */
    static public class ClassCastExpression extends Ast implements ICastExpression
    {
        private IExpression _Name;
        private IUnaryExpressionNotPlusMinus _UnaryExpressionNotPlusMinus;
        private DimList _Dims;

        public IExpression getName() { return _Name; }
        public IUnaryExpressionNotPlusMinus getUnaryExpressionNotPlusMinus() { return _UnaryExpressionNotPlusMinus; }
        /**
         * The value returned by <b>getDims</b> may be <b>null</b>
         */
        public DimList getDims() { return _Dims; }

        public ClassCastExpression(IToken leftIToken, IToken rightIToken,
                                   IExpression _Name,
                                   IUnaryExpressionNotPlusMinus _UnaryExpressionNotPlusMinus,
                                   DimList _Dims)
        {
            super(leftIToken, rightIToken);

            this._Name = _Name;
            this._UnaryExpressionNotPlusMinus = _UnaryExpressionNotPlusMinus;
            this._Dims = _Dims;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ClassCastExpression)) return false;
            ClassCastExpression other = (ClassCastExpression    ) o;
            if (! _Name.equals(other.getName())) return false;
            if (! _UnaryExpressionNotPlusMinus.equals(other.getUnaryExpressionNotPlusMinus())) return false;
            if (_Dims == null && other.getDims() != null) return false;
            else if (! _Dims.equals(other.getDims())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getName().hashCode());
            hash = hash * 31 + (getUnaryExpressionNotPlusMinus().hashCode());
            hash = hash * 31 + (getDims() == null ? 0 : getDims().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 255:  MultiplicativeExpression ::= MultiplicativeExpression * UnaryExpression
     *</b>
     */
    static public class MultiplyExpression extends Ast implements IMultiplicativeExpression
    {
        private IMultiplicativeExpression _MultiplicativeExpression;
        private IUnaryExpression _UnaryExpression;

        public IMultiplicativeExpression getMultiplicativeExpression() { return _MultiplicativeExpression; }
        public IUnaryExpression getUnaryExpression() { return _UnaryExpression; }

        public MultiplyExpression(IToken leftIToken, IToken rightIToken,
                                  IMultiplicativeExpression _MultiplicativeExpression,
                                  IUnaryExpression _UnaryExpression)
        {
            super(leftIToken, rightIToken);

            this._MultiplicativeExpression = _MultiplicativeExpression;
            this._UnaryExpression = _UnaryExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof MultiplyExpression)) return false;
            MultiplyExpression other = (MultiplyExpression    ) o;
            if (! _MultiplicativeExpression.equals(other.getMultiplicativeExpression())) return false;
            if (! _UnaryExpression.equals(other.getUnaryExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getMultiplicativeExpression().hashCode());
            hash = hash * 31 + (getUnaryExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 256:  MultiplicativeExpression ::= MultiplicativeExpression / UnaryExpression
     *</b>
     */
    static public class DivideExpression extends Ast implements IMultiplicativeExpression
    {
        private IMultiplicativeExpression _MultiplicativeExpression;
        private IUnaryExpression _UnaryExpression;

        public IMultiplicativeExpression getMultiplicativeExpression() { return _MultiplicativeExpression; }
        public IUnaryExpression getUnaryExpression() { return _UnaryExpression; }

        public DivideExpression(IToken leftIToken, IToken rightIToken,
                                IMultiplicativeExpression _MultiplicativeExpression,
                                IUnaryExpression _UnaryExpression)
        {
            super(leftIToken, rightIToken);

            this._MultiplicativeExpression = _MultiplicativeExpression;
            this._UnaryExpression = _UnaryExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof DivideExpression)) return false;
            DivideExpression other = (DivideExpression    ) o;
            if (! _MultiplicativeExpression.equals(other.getMultiplicativeExpression())) return false;
            if (! _UnaryExpression.equals(other.getUnaryExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getMultiplicativeExpression().hashCode());
            hash = hash * 31 + (getUnaryExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 257:  MultiplicativeExpression ::= MultiplicativeExpression % UnaryExpression
     *</b>
     */
    static public class ModExpression extends Ast implements IMultiplicativeExpression
    {
        private IMultiplicativeExpression _MultiplicativeExpression;
        private IUnaryExpression _UnaryExpression;

        public IMultiplicativeExpression getMultiplicativeExpression() { return _MultiplicativeExpression; }
        public IUnaryExpression getUnaryExpression() { return _UnaryExpression; }

        public ModExpression(IToken leftIToken, IToken rightIToken,
                             IMultiplicativeExpression _MultiplicativeExpression,
                             IUnaryExpression _UnaryExpression)
        {
            super(leftIToken, rightIToken);

            this._MultiplicativeExpression = _MultiplicativeExpression;
            this._UnaryExpression = _UnaryExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ModExpression)) return false;
            ModExpression other = (ModExpression    ) o;
            if (! _MultiplicativeExpression.equals(other.getMultiplicativeExpression())) return false;
            if (! _UnaryExpression.equals(other.getUnaryExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getMultiplicativeExpression().hashCode());
            hash = hash * 31 + (getUnaryExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 259:  AdditiveExpression ::= AdditiveExpression + MultiplicativeExpression
     *</b>
     */
    static public class AddExpression extends Ast implements IAdditiveExpression
    {
        private IAdditiveExpression _AdditiveExpression;
        private IMultiplicativeExpression _MultiplicativeExpression;

        public IAdditiveExpression getAdditiveExpression() { return _AdditiveExpression; }
        public IMultiplicativeExpression getMultiplicativeExpression() { return _MultiplicativeExpression; }

        public AddExpression(IToken leftIToken, IToken rightIToken,
                             IAdditiveExpression _AdditiveExpression,
                             IMultiplicativeExpression _MultiplicativeExpression)
        {
            super(leftIToken, rightIToken);

            this._AdditiveExpression = _AdditiveExpression;
            this._MultiplicativeExpression = _MultiplicativeExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof AddExpression)) return false;
            AddExpression other = (AddExpression    ) o;
            if (! _AdditiveExpression.equals(other.getAdditiveExpression())) return false;
            if (! _MultiplicativeExpression.equals(other.getMultiplicativeExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getAdditiveExpression().hashCode());
            hash = hash * 31 + (getMultiplicativeExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 260:  AdditiveExpression ::= AdditiveExpression - MultiplicativeExpression
     *</b>
     */
    static public class SubtractExpression extends Ast implements IAdditiveExpression
    {
        private IAdditiveExpression _AdditiveExpression;
        private IMultiplicativeExpression _MultiplicativeExpression;

        public IAdditiveExpression getAdditiveExpression() { return _AdditiveExpression; }
        public IMultiplicativeExpression getMultiplicativeExpression() { return _MultiplicativeExpression; }

        public SubtractExpression(IToken leftIToken, IToken rightIToken,
                                  IAdditiveExpression _AdditiveExpression,
                                  IMultiplicativeExpression _MultiplicativeExpression)
        {
            super(leftIToken, rightIToken);

            this._AdditiveExpression = _AdditiveExpression;
            this._MultiplicativeExpression = _MultiplicativeExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof SubtractExpression)) return false;
            SubtractExpression other = (SubtractExpression    ) o;
            if (! _AdditiveExpression.equals(other.getAdditiveExpression())) return false;
            if (! _MultiplicativeExpression.equals(other.getMultiplicativeExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getAdditiveExpression().hashCode());
            hash = hash * 31 + (getMultiplicativeExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 262:  ShiftExpression ::= ShiftExpression << AdditiveExpression
     *</b>
     */
    static public class LeftShiftExpression extends Ast implements IShiftExpression
    {
        private IShiftExpression _ShiftExpression;
        private IAdditiveExpression _AdditiveExpression;

        public IShiftExpression getShiftExpression() { return _ShiftExpression; }
        public IAdditiveExpression getAdditiveExpression() { return _AdditiveExpression; }

        public LeftShiftExpression(IToken leftIToken, IToken rightIToken,
                                   IShiftExpression _ShiftExpression,
                                   IAdditiveExpression _AdditiveExpression)
        {
            super(leftIToken, rightIToken);

            this._ShiftExpression = _ShiftExpression;
            this._AdditiveExpression = _AdditiveExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof LeftShiftExpression)) return false;
            LeftShiftExpression other = (LeftShiftExpression    ) o;
            if (! _ShiftExpression.equals(other.getShiftExpression())) return false;
            if (! _AdditiveExpression.equals(other.getAdditiveExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getShiftExpression().hashCode());
            hash = hash * 31 + (getAdditiveExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 263:  ShiftExpression ::= ShiftExpression >> AdditiveExpression
     *</b>
     */
    static public class RightShiftExpression extends Ast implements IShiftExpression
    {
        private IShiftExpression _ShiftExpression;
        private IAdditiveExpression _AdditiveExpression;

        public IShiftExpression getShiftExpression() { return _ShiftExpression; }
        public IAdditiveExpression getAdditiveExpression() { return _AdditiveExpression; }

        public RightShiftExpression(IToken leftIToken, IToken rightIToken,
                                    IShiftExpression _ShiftExpression,
                                    IAdditiveExpression _AdditiveExpression)
        {
            super(leftIToken, rightIToken);

            this._ShiftExpression = _ShiftExpression;
            this._AdditiveExpression = _AdditiveExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof RightShiftExpression)) return false;
            RightShiftExpression other = (RightShiftExpression    ) o;
            if (! _ShiftExpression.equals(other.getShiftExpression())) return false;
            if (! _AdditiveExpression.equals(other.getAdditiveExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getShiftExpression().hashCode());
            hash = hash * 31 + (getAdditiveExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 264:  ShiftExpression ::= ShiftExpression >>> AdditiveExpression
     *</b>
     */
    static public class UnsignedRightShiftExpression extends Ast implements IShiftExpression
    {
        private IShiftExpression _ShiftExpression;
        private IAdditiveExpression _AdditiveExpression;

        public IShiftExpression getShiftExpression() { return _ShiftExpression; }
        public IAdditiveExpression getAdditiveExpression() { return _AdditiveExpression; }

        public UnsignedRightShiftExpression(IToken leftIToken, IToken rightIToken,
                                            IShiftExpression _ShiftExpression,
                                            IAdditiveExpression _AdditiveExpression)
        {
            super(leftIToken, rightIToken);

            this._ShiftExpression = _ShiftExpression;
            this._AdditiveExpression = _AdditiveExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof UnsignedRightShiftExpression)) return false;
            UnsignedRightShiftExpression other = (UnsignedRightShiftExpression    ) o;
            if (! _ShiftExpression.equals(other.getShiftExpression())) return false;
            if (! _AdditiveExpression.equals(other.getAdditiveExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getShiftExpression().hashCode());
            hash = hash * 31 + (getAdditiveExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 266:  RelationalExpression ::= RelationalExpression < ShiftExpression
     *</b>
     */
    static public class LessExpression extends Ast implements IRelationalExpression
    {
        private IRelationalExpression _RelationalExpression;
        private IShiftExpression _ShiftExpression;

        public IRelationalExpression getRelationalExpression() { return _RelationalExpression; }
        public IShiftExpression getShiftExpression() { return _ShiftExpression; }

        public LessExpression(IToken leftIToken, IToken rightIToken,
                              IRelationalExpression _RelationalExpression,
                              IShiftExpression _ShiftExpression)
        {
            super(leftIToken, rightIToken);

            this._RelationalExpression = _RelationalExpression;
            this._ShiftExpression = _ShiftExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof LessExpression)) return false;
            LessExpression other = (LessExpression    ) o;
            if (! _RelationalExpression.equals(other.getRelationalExpression())) return false;
            if (! _ShiftExpression.equals(other.getShiftExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getRelationalExpression().hashCode());
            hash = hash * 31 + (getShiftExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 267:  RelationalExpression ::= RelationalExpression > ShiftExpression
     *</b>
     */
    static public class GreaterExpression extends Ast implements IRelationalExpression
    {
        private IRelationalExpression _RelationalExpression;
        private IShiftExpression _ShiftExpression;

        public IRelationalExpression getRelationalExpression() { return _RelationalExpression; }
        public IShiftExpression getShiftExpression() { return _ShiftExpression; }

        public GreaterExpression(IToken leftIToken, IToken rightIToken,
                                 IRelationalExpression _RelationalExpression,
                                 IShiftExpression _ShiftExpression)
        {
            super(leftIToken, rightIToken);

            this._RelationalExpression = _RelationalExpression;
            this._ShiftExpression = _ShiftExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof GreaterExpression)) return false;
            GreaterExpression other = (GreaterExpression    ) o;
            if (! _RelationalExpression.equals(other.getRelationalExpression())) return false;
            if (! _ShiftExpression.equals(other.getShiftExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getRelationalExpression().hashCode());
            hash = hash * 31 + (getShiftExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 268:  RelationalExpression ::= RelationalExpression <= ShiftExpression
     *</b>
     */
    static public class LessEqualExpression extends Ast implements IRelationalExpression
    {
        private IRelationalExpression _RelationalExpression;
        private IShiftExpression _ShiftExpression;

        public IRelationalExpression getRelationalExpression() { return _RelationalExpression; }
        public IShiftExpression getShiftExpression() { return _ShiftExpression; }

        public LessEqualExpression(IToken leftIToken, IToken rightIToken,
                                   IRelationalExpression _RelationalExpression,
                                   IShiftExpression _ShiftExpression)
        {
            super(leftIToken, rightIToken);

            this._RelationalExpression = _RelationalExpression;
            this._ShiftExpression = _ShiftExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof LessEqualExpression)) return false;
            LessEqualExpression other = (LessEqualExpression    ) o;
            if (! _RelationalExpression.equals(other.getRelationalExpression())) return false;
            if (! _ShiftExpression.equals(other.getShiftExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getRelationalExpression().hashCode());
            hash = hash * 31 + (getShiftExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 269:  RelationalExpression ::= RelationalExpression >= ShiftExpression
     *</b>
     */
    static public class GreaterEqualExpression extends Ast implements IRelationalExpression
    {
        private IRelationalExpression _RelationalExpression;
        private IShiftExpression _ShiftExpression;

        public IRelationalExpression getRelationalExpression() { return _RelationalExpression; }
        public IShiftExpression getShiftExpression() { return _ShiftExpression; }

        public GreaterEqualExpression(IToken leftIToken, IToken rightIToken,
                                      IRelationalExpression _RelationalExpression,
                                      IShiftExpression _ShiftExpression)
        {
            super(leftIToken, rightIToken);

            this._RelationalExpression = _RelationalExpression;
            this._ShiftExpression = _ShiftExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof GreaterEqualExpression)) return false;
            GreaterEqualExpression other = (GreaterEqualExpression    ) o;
            if (! _RelationalExpression.equals(other.getRelationalExpression())) return false;
            if (! _ShiftExpression.equals(other.getShiftExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getRelationalExpression().hashCode());
            hash = hash * 31 + (getShiftExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 270:  RelationalExpression ::= RelationalExpression instanceof ReferenceType
     *</b>
     */
    static public class InstanceofExpression extends Ast implements IRelationalExpression
    {
        private IRelationalExpression _RelationalExpression;
        private IReferenceType _ReferenceType;

        public IRelationalExpression getRelationalExpression() { return _RelationalExpression; }
        public IReferenceType getReferenceType() { return _ReferenceType; }

        public InstanceofExpression(IToken leftIToken, IToken rightIToken,
                                    IRelationalExpression _RelationalExpression,
                                    IReferenceType _ReferenceType)
        {
            super(leftIToken, rightIToken);

            this._RelationalExpression = _RelationalExpression;
            this._ReferenceType = _ReferenceType;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof InstanceofExpression)) return false;
            InstanceofExpression other = (InstanceofExpression    ) o;
            if (! _RelationalExpression.equals(other.getRelationalExpression())) return false;
            if (! _ReferenceType.equals(other.getReferenceType())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getRelationalExpression().hashCode());
            hash = hash * 31 + (getReferenceType().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 272:  EqualityExpression ::= EqualityExpression == RelationalExpression
     *</b>
     */
    static public class EqualExpression extends Ast implements IEqualityExpression
    {
        private IEqualityExpression _EqualityExpression;
        private IRelationalExpression _RelationalExpression;

        public IEqualityExpression getEqualityExpression() { return _EqualityExpression; }
        public IRelationalExpression getRelationalExpression() { return _RelationalExpression; }

        public EqualExpression(IToken leftIToken, IToken rightIToken,
                               IEqualityExpression _EqualityExpression,
                               IRelationalExpression _RelationalExpression)
        {
            super(leftIToken, rightIToken);

            this._EqualityExpression = _EqualityExpression;
            this._RelationalExpression = _RelationalExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof EqualExpression)) return false;
            EqualExpression other = (EqualExpression    ) o;
            if (! _EqualityExpression.equals(other.getEqualityExpression())) return false;
            if (! _RelationalExpression.equals(other.getRelationalExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getEqualityExpression().hashCode());
            hash = hash * 31 + (getRelationalExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 273:  EqualityExpression ::= EqualityExpression != RelationalExpression
     *</b>
     */
    static public class NotEqualExpression extends Ast implements IEqualityExpression
    {
        private IEqualityExpression _EqualityExpression;
        private IRelationalExpression _RelationalExpression;

        public IEqualityExpression getEqualityExpression() { return _EqualityExpression; }
        public IRelationalExpression getRelationalExpression() { return _RelationalExpression; }

        public NotEqualExpression(IToken leftIToken, IToken rightIToken,
                                  IEqualityExpression _EqualityExpression,
                                  IRelationalExpression _RelationalExpression)
        {
            super(leftIToken, rightIToken);

            this._EqualityExpression = _EqualityExpression;
            this._RelationalExpression = _RelationalExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof NotEqualExpression)) return false;
            NotEqualExpression other = (NotEqualExpression    ) o;
            if (! _EqualityExpression.equals(other.getEqualityExpression())) return false;
            if (! _RelationalExpression.equals(other.getRelationalExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getEqualityExpression().hashCode());
            hash = hash * 31 + (getRelationalExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<em>
     *<li>Rule 274:  AndExpression ::= EqualityExpression
     *</em>
     *<p>
     *<b>
     *<li>Rule 275:  AndExpression ::= AndExpression & EqualityExpression
     *</b>
     */
    static public class AndExpression extends Ast implements IAndExpression
    {
        private IAndExpression _AndExpression;
        private IEqualityExpression _EqualityExpression;

        public IAndExpression getAndExpression() { return _AndExpression; }
        public IEqualityExpression getEqualityExpression() { return _EqualityExpression; }

        public AndExpression(IToken leftIToken, IToken rightIToken,
                             IAndExpression _AndExpression,
                             IEqualityExpression _EqualityExpression)
        {
            super(leftIToken, rightIToken);

            this._AndExpression = _AndExpression;
            this._EqualityExpression = _EqualityExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof AndExpression)) return false;
            AndExpression other = (AndExpression    ) o;
            if (! _AndExpression.equals(other.getAndExpression())) return false;
            if (! _EqualityExpression.equals(other.getEqualityExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getAndExpression().hashCode());
            hash = hash * 31 + (getEqualityExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<em>
     *<li>Rule 276:  ExclusiveOrExpression ::= AndExpression
     *</em>
     *<p>
     *<b>
     *<li>Rule 277:  ExclusiveOrExpression ::= ExclusiveOrExpression ^ AndExpression
     *</b>
     */
    static public class ExclusiveOrExpression extends Ast implements IExclusiveOrExpression
    {
        private IExclusiveOrExpression _ExclusiveOrExpression;
        private IAndExpression _AndExpression;

        public IExclusiveOrExpression getExclusiveOrExpression() { return _ExclusiveOrExpression; }
        public IAndExpression getAndExpression() { return _AndExpression; }

        public ExclusiveOrExpression(IToken leftIToken, IToken rightIToken,
                                     IExclusiveOrExpression _ExclusiveOrExpression,
                                     IAndExpression _AndExpression)
        {
            super(leftIToken, rightIToken);

            this._ExclusiveOrExpression = _ExclusiveOrExpression;
            this._AndExpression = _AndExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ExclusiveOrExpression)) return false;
            ExclusiveOrExpression other = (ExclusiveOrExpression    ) o;
            if (! _ExclusiveOrExpression.equals(other.getExclusiveOrExpression())) return false;
            if (! _AndExpression.equals(other.getAndExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getExclusiveOrExpression().hashCode());
            hash = hash * 31 + (getAndExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<em>
     *<li>Rule 278:  InclusiveOrExpression ::= ExclusiveOrExpression
     *</em>
     *<p>
     *<b>
     *<li>Rule 279:  InclusiveOrExpression ::= InclusiveOrExpression | ExclusiveOrExpression
     *</b>
     */
    static public class InclusiveOrExpression extends Ast implements IInclusiveOrExpression
    {
        private IInclusiveOrExpression _InclusiveOrExpression;
        private IExclusiveOrExpression _ExclusiveOrExpression;

        public IInclusiveOrExpression getInclusiveOrExpression() { return _InclusiveOrExpression; }
        public IExclusiveOrExpression getExclusiveOrExpression() { return _ExclusiveOrExpression; }

        public InclusiveOrExpression(IToken leftIToken, IToken rightIToken,
                                     IInclusiveOrExpression _InclusiveOrExpression,
                                     IExclusiveOrExpression _ExclusiveOrExpression)
        {
            super(leftIToken, rightIToken);

            this._InclusiveOrExpression = _InclusiveOrExpression;
            this._ExclusiveOrExpression = _ExclusiveOrExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof InclusiveOrExpression)) return false;
            InclusiveOrExpression other = (InclusiveOrExpression    ) o;
            if (! _InclusiveOrExpression.equals(other.getInclusiveOrExpression())) return false;
            if (! _ExclusiveOrExpression.equals(other.getExclusiveOrExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getInclusiveOrExpression().hashCode());
            hash = hash * 31 + (getExclusiveOrExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<em>
     *<li>Rule 280:  ConditionalAndExpression ::= InclusiveOrExpression
     *</em>
     *<p>
     *<b>
     *<li>Rule 281:  ConditionalAndExpression ::= ConditionalAndExpression && InclusiveOrExpression
     *</b>
     */
    static public class ConditionalAndExpression extends Ast implements IConditionalAndExpression
    {
        private IConditionalAndExpression _ConditionalAndExpression;
        private IInclusiveOrExpression _InclusiveOrExpression;

        public IConditionalAndExpression getConditionalAndExpression() { return _ConditionalAndExpression; }
        public IInclusiveOrExpression getInclusiveOrExpression() { return _InclusiveOrExpression; }

        public ConditionalAndExpression(IToken leftIToken, IToken rightIToken,
                                        IConditionalAndExpression _ConditionalAndExpression,
                                        IInclusiveOrExpression _InclusiveOrExpression)
        {
            super(leftIToken, rightIToken);

            this._ConditionalAndExpression = _ConditionalAndExpression;
            this._InclusiveOrExpression = _InclusiveOrExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ConditionalAndExpression)) return false;
            ConditionalAndExpression other = (ConditionalAndExpression    ) o;
            if (! _ConditionalAndExpression.equals(other.getConditionalAndExpression())) return false;
            if (! _InclusiveOrExpression.equals(other.getInclusiveOrExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getConditionalAndExpression().hashCode());
            hash = hash * 31 + (getInclusiveOrExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<em>
     *<li>Rule 282:  ConditionalOrExpression ::= ConditionalAndExpression
     *</em>
     *<p>
     *<b>
     *<li>Rule 283:  ConditionalOrExpression ::= ConditionalOrExpression || ConditionalAndExpression
     *</b>
     */
    static public class ConditionalOrExpression extends Ast implements IConditionalOrExpression
    {
        private IConditionalOrExpression _ConditionalOrExpression;
        private IConditionalAndExpression _ConditionalAndExpression;

        public IConditionalOrExpression getConditionalOrExpression() { return _ConditionalOrExpression; }
        public IConditionalAndExpression getConditionalAndExpression() { return _ConditionalAndExpression; }

        public ConditionalOrExpression(IToken leftIToken, IToken rightIToken,
                                       IConditionalOrExpression _ConditionalOrExpression,
                                       IConditionalAndExpression _ConditionalAndExpression)
        {
            super(leftIToken, rightIToken);

            this._ConditionalOrExpression = _ConditionalOrExpression;
            this._ConditionalAndExpression = _ConditionalAndExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ConditionalOrExpression)) return false;
            ConditionalOrExpression other = (ConditionalOrExpression    ) o;
            if (! _ConditionalOrExpression.equals(other.getConditionalOrExpression())) return false;
            if (! _ConditionalAndExpression.equals(other.getConditionalAndExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getConditionalOrExpression().hashCode());
            hash = hash * 31 + (getConditionalAndExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<em>
     *<li>Rule 284:  ConditionalExpression ::= ConditionalOrExpression
     *</em>
     *<p>
     *<b>
     *<li>Rule 285:  ConditionalExpression ::= ConditionalOrExpression ? Expression : ConditionalExpression
     *</b>
     */
    static public class ConditionalExpression extends Ast implements IConditionalExpression
    {
        private IConditionalOrExpression _ConditionalOrExpression;
        private IExpression _Expression;
        private IConditionalExpression _ConditionalExpression;

        public IConditionalOrExpression getConditionalOrExpression() { return _ConditionalOrExpression; }
        public IExpression getExpression() { return _Expression; }
        public IConditionalExpression getConditionalExpression() { return _ConditionalExpression; }

        public ConditionalExpression(IToken leftIToken, IToken rightIToken,
                                     IConditionalOrExpression _ConditionalOrExpression,
                                     IExpression _Expression,
                                     IConditionalExpression _ConditionalExpression)
        {
            super(leftIToken, rightIToken);

            this._ConditionalOrExpression = _ConditionalOrExpression;
            this._Expression = _Expression;
            this._ConditionalExpression = _ConditionalExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof ConditionalExpression)) return false;
            ConditionalExpression other = (ConditionalExpression    ) o;
            if (! _ConditionalOrExpression.equals(other.getConditionalOrExpression())) return false;
            if (! _Expression.equals(other.getExpression())) return false;
            if (! _ConditionalExpression.equals(other.getConditionalExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getConditionalOrExpression().hashCode());
            hash = hash * 31 + (getExpression().hashCode());
            hash = hash * 31 + (getConditionalExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 288:  Assignment ::= LeftHandSide AssignmentOperator AssignmentExpression
     *</b>
     */
    static public class Assignment extends Ast implements IAssignment
    {
        private ILeftHandSide _LeftHandSide;
        private IAssignmentOperator _AssignmentOperator;
        private IAssignmentExpression _AssignmentExpression;

        public ILeftHandSide getLeftHandSide() { return _LeftHandSide; }
        public IAssignmentOperator getAssignmentOperator() { return _AssignmentOperator; }
        public IAssignmentExpression getAssignmentExpression() { return _AssignmentExpression; }

        public Assignment(IToken leftIToken, IToken rightIToken,
                          ILeftHandSide _LeftHandSide,
                          IAssignmentOperator _AssignmentOperator,
                          IAssignmentExpression _AssignmentExpression)
        {
            super(leftIToken, rightIToken);

            this._LeftHandSide = _LeftHandSide;
            this._AssignmentOperator = _AssignmentOperator;
            this._AssignmentExpression = _AssignmentExpression;
            initialize();
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            //
        // The supers call is not required for now because Ast nodes
        // can only extend the root Ast, AstToken and AstList and none
        // of these nodes contain children.
        //
        // if (! super.equals(o)) return false;
        //
            if (! (o instanceof Assignment)) return false;
            Assignment other = (Assignment    ) o;
            if (! _LeftHandSide.equals(other.getLeftHandSide())) return false;
            if (! _AssignmentOperator.equals(other.getAssignmentOperator())) return false;
            if (! _AssignmentExpression.equals(other.getAssignmentExpression())) return false;
            return true;
        }

        public int hashCode()
        {
            int hash = 7;
            hash = hash * 31 + (getLeftHandSide().hashCode());
            hash = hash * 31 + (getAssignmentOperator().hashCode());
            hash = hash * 31 + (getAssignmentExpression().hashCode());
            return hash;
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 292:  AssignmentOperator ::= =
     *</b>
     */
    static public class EqualOperator extends AstToken implements IAssignmentOperator
    {
        public EqualOperator(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 293:  AssignmentOperator ::= *=
     *</b>
     */
    static public class MultiplyEqualOperator extends AstToken implements IAssignmentOperator
    {
        public MultiplyEqualOperator(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 294:  AssignmentOperator ::= /=
     *</b>
     */
    static public class DivideEqualOperator extends AstToken implements IAssignmentOperator
    {
        public DivideEqualOperator(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 295:  AssignmentOperator ::= %=
     *</b>
     */
    static public class ModEqualOperator extends AstToken implements IAssignmentOperator
    {
        public ModEqualOperator(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 296:  AssignmentOperator ::= +=
     *</b>
     */
    static public class PlusEqualOperator extends AstToken implements IAssignmentOperator
    {
        public PlusEqualOperator(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 297:  AssignmentOperator ::= -=
     *</b>
     */
    static public class MinusEqualOperator extends AstToken implements IAssignmentOperator
    {
        public MinusEqualOperator(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 298:  AssignmentOperator ::= <<=
     *</b>
     */
    static public class LeftShiftEqualOperator extends AstToken implements IAssignmentOperator
    {
        public LeftShiftEqualOperator(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 299:  AssignmentOperator ::= >>=
     *</b>
     */
    static public class RightShiftEqualOperator extends AstToken implements IAssignmentOperator
    {
        public RightShiftEqualOperator(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 300:  AssignmentOperator ::= >>>=
     *</b>
     */
    static public class UnsignedRightShiftEqualOperator extends AstToken implements IAssignmentOperator
    {
        public UnsignedRightShiftEqualOperator(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 301:  AssignmentOperator ::= &=
     *</b>
     */
    static public class AndEqualOperator extends AstToken implements IAssignmentOperator
    {
        public AndEqualOperator(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 302:  AssignmentOperator ::= ^=
     *</b>
     */
    static public class ExclusiveOrEqualOperator extends AstToken implements IAssignmentOperator
    {
        public ExclusiveOrEqualOperator(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<b>
     *<li>Rule 303:  AssignmentOperator ::= |=
     *</b>
     */
    static public class OrEqualOperator extends AstToken implements IAssignmentOperator
    {
        public OrEqualOperator(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<em>
     *<li>Rule 352:  Commaopt ::= $Empty
     *</em>
     *<p>
     *<b>
     *<li>Rule 353:  Commaopt ::= ,
     *</b>
     */
    static public class Commaopt extends AstToken implements ICommaopt
    {
        public Commaopt(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    /**
     *<em>
     *<li>Rule 354:  IDENTIFIERopt ::= $Empty
     *</em>
     *<p>
     *<b>
     *<li>Rule 355:  IDENTIFIERopt ::= IDENTIFIER
     *</b>
     */
    static public class IDENTIFIERopt extends AstToken implements IIDENTIFIERopt
    {
        public IDENTIFIERopt(IToken token) { super(token); initialize(); }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public void accept(Visitor v) { v.visit(this); }
        public void accept(ArgumentVisitor v, Object o) { v.visit(this, o); }
        public Object accept(ResultVisitor v) { return v.visit(this); }
        public Object accept(ResultArgumentVisitor v, Object o) { return v.visit(this, o); }
    }

    public interface Visitor
    {
        void visit(AstToken n);
        void visit(IntegerLiteral n);
        void visit(LongLiteral n);
        void visit(FloatLiteral n);
        void visit(DoubleLiteral n);
        void visit(BooleanLiteral n);
        void visit(CharacterLiteral n);
        void visit(StringLiteral n);
        void visit(NullLiteral n);
        void visit(TrueLiteral n);
        void visit(FalseLiteral n);
        void visit(BooleanType n);
        void visit(ByteType n);
        void visit(ShortType n);
        void visit(IntType n);
        void visit(LongType n);
        void visit(CharType n);
        void visit(FloatType n);
        void visit(DoubleType n);
        void visit(PrimitiveArrayType n);
        void visit(ClassOrInterfaceArrayType n);
        void visit(SimpleName n);
        void visit(QualifiedName n);
        void visit(CompilationUnit n);
        void visit(ImportDeclarationList n);
        void visit(TypeDeclarationList n);
        void visit(PackageDeclaration n);
        void visit(SingleTypeImportDeclaration n);
        void visit(TypeImportOnDemandDeclaration n);
        void visit(EmptyDeclaration n);
        void visit(ModifierList n);
        void visit(PublicModifier n);
        void visit(ProtectedModifier n);
        void visit(PrivateModifier n);
        void visit(StaticModifier n);
        void visit(AbstractModifier n);
        void visit(FinalModifier n);
        void visit(NativeModifier n);
        void visit(StrictfpModifier n);
        void visit(SynchronizedModifier n);
        void visit(TransientModifier n);
        void visit(VolatileModifier n);
        void visit(ClassDeclaration n);
        void visit(Super n);
        void visit(InterfaceTypeList n);
        void visit(ClassBody n);
        void visit(ClassBodyDeclarationList n);
        void visit(FieldDeclaration n);
        void visit(VariableDeclaratorList n);
        void visit(VariableDeclarator n);
        void visit(VariableDeclaratorId n);
        void visit(MethodDeclaration n);
        void visit(TypedMethodHeader n);
        void visit(VoidMethodHeader n);
        void visit(MethodDeclarator n);
        void visit(FormalParameterList n);
        void visit(FormalParameter n);
        void visit(ClassTypeList n);
        void visit(EmptyMethodBody n);
        void visit(StaticInitializer n);
        void visit(ConstructorDeclaration n);
        void visit(ConstructorDeclarator n);
        void visit(ConstructorBody n);
        void visit(ThisCall n);
        void visit(SuperCall n);
        void visit(InterfaceDeclaration n);
        void visit(InterfaceBody n);
        void visit(InterfaceMemberDeclarationList n);
        void visit(AbstractMethodDeclaration n);
        void visit(ArrayInitializer n);
        void visit(VariableInitializerList n);
        void visit(Block n);
        void visit(BlockStatementList n);
        void visit(LocalVariableDeclarationStatement n);
        void visit(LocalVariableDeclaration n);
        void visit(EmptyStatement n);
        void visit(LabeledStatement n);
        void visit(ExpressionStatement n);
        void visit(IfStatement n);
        void visit(SwitchStatement n);
        void visit(SwitchBlock n);
        void visit(SwitchBlockStatementList n);
        void visit(SwitchBlockStatement n);
        void visit(SwitchLabelList n);
        void visit(CaseLabel n);
        void visit(DefaultLabel n);
        void visit(WhileStatement n);
        void visit(DoStatement n);
        void visit(ForStatement n);
        void visit(StatementExpressionList n);
        void visit(BreakStatement n);
        void visit(ContinueStatement n);
        void visit(ReturnStatement n);
        void visit(ThrowStatement n);
        void visit(SynchronizedStatement n);
        void visit(TryStatement n);
        void visit(CatchClauseList n);
        void visit(CatchClause n);
        void visit(Finally n);
        void visit(ParenthesizedExpression n);
        void visit(PrimaryThis n);
        void visit(PrimaryClassLiteral n);
        void visit(PrimaryVoidClassLiteral n);
        void visit(ClassInstanceCreationExpression n);
        void visit(ExpressionList n);
        void visit(ArrayCreationExpression n);
        void visit(DimExprList n);
        void visit(DimExpr n);
        void visit(DimList n);
        void visit(Dim n);
        void visit(FieldAccess n);
        void visit(SuperFieldAccess n);
        void visit(MethodInvocation n);
        void visit(PrimaryMethodInvocation n);
        void visit(SuperMethodInvocation n);
        void visit(ArrayAccess n);
        void visit(PostIncrementExpression n);
        void visit(PostDecrementExpression n);
        void visit(PlusUnaryExpression n);
        void visit(MinusUnaryExpression n);
        void visit(PreIncrementExpression n);
        void visit(PreDecrementExpression n);
        void visit(UnaryComplementExpression n);
        void visit(UnaryNotExpression n);
        void visit(PrimitiveCastExpression n);
        void visit(ClassCastExpression n);
        void visit(MultiplyExpression n);
        void visit(DivideExpression n);
        void visit(ModExpression n);
        void visit(AddExpression n);
        void visit(SubtractExpression n);
        void visit(LeftShiftExpression n);
        void visit(RightShiftExpression n);
        void visit(UnsignedRightShiftExpression n);
        void visit(LessExpression n);
        void visit(GreaterExpression n);
        void visit(LessEqualExpression n);
        void visit(GreaterEqualExpression n);
        void visit(InstanceofExpression n);
        void visit(EqualExpression n);
        void visit(NotEqualExpression n);
        void visit(AndExpression n);
        void visit(ExclusiveOrExpression n);
        void visit(InclusiveOrExpression n);
        void visit(ConditionalAndExpression n);
        void visit(ConditionalOrExpression n);
        void visit(ConditionalExpression n);
        void visit(Assignment n);
        void visit(EqualOperator n);
        void visit(MultiplyEqualOperator n);
        void visit(DivideEqualOperator n);
        void visit(ModEqualOperator n);
        void visit(PlusEqualOperator n);
        void visit(MinusEqualOperator n);
        void visit(LeftShiftEqualOperator n);
        void visit(RightShiftEqualOperator n);
        void visit(UnsignedRightShiftEqualOperator n);
        void visit(AndEqualOperator n);
        void visit(ExclusiveOrEqualOperator n);
        void visit(OrEqualOperator n);
        void visit(Commaopt n);
        void visit(IDENTIFIERopt n);
    }
    public interface ArgumentVisitor
    {
        void visit(AstToken n, Object o);
        void visit(IntegerLiteral n, Object o);
        void visit(LongLiteral n, Object o);
        void visit(FloatLiteral n, Object o);
        void visit(DoubleLiteral n, Object o);
        void visit(BooleanLiteral n, Object o);
        void visit(CharacterLiteral n, Object o);
        void visit(StringLiteral n, Object o);
        void visit(NullLiteral n, Object o);
        void visit(TrueLiteral n, Object o);
        void visit(FalseLiteral n, Object o);
        void visit(BooleanType n, Object o);
        void visit(ByteType n, Object o);
        void visit(ShortType n, Object o);
        void visit(IntType n, Object o);
        void visit(LongType n, Object o);
        void visit(CharType n, Object o);
        void visit(FloatType n, Object o);
        void visit(DoubleType n, Object o);
        void visit(PrimitiveArrayType n, Object o);
        void visit(ClassOrInterfaceArrayType n, Object o);
        void visit(SimpleName n, Object o);
        void visit(QualifiedName n, Object o);
        void visit(CompilationUnit n, Object o);
        void visit(ImportDeclarationList n, Object o);
        void visit(TypeDeclarationList n, Object o);
        void visit(PackageDeclaration n, Object o);
        void visit(SingleTypeImportDeclaration n, Object o);
        void visit(TypeImportOnDemandDeclaration n, Object o);
        void visit(EmptyDeclaration n, Object o);
        void visit(ModifierList n, Object o);
        void visit(PublicModifier n, Object o);
        void visit(ProtectedModifier n, Object o);
        void visit(PrivateModifier n, Object o);
        void visit(StaticModifier n, Object o);
        void visit(AbstractModifier n, Object o);
        void visit(FinalModifier n, Object o);
        void visit(NativeModifier n, Object o);
        void visit(StrictfpModifier n, Object o);
        void visit(SynchronizedModifier n, Object o);
        void visit(TransientModifier n, Object o);
        void visit(VolatileModifier n, Object o);
        void visit(ClassDeclaration n, Object o);
        void visit(Super n, Object o);
        void visit(InterfaceTypeList n, Object o);
        void visit(ClassBody n, Object o);
        void visit(ClassBodyDeclarationList n, Object o);
        void visit(FieldDeclaration n, Object o);
        void visit(VariableDeclaratorList n, Object o);
        void visit(VariableDeclarator n, Object o);
        void visit(VariableDeclaratorId n, Object o);
        void visit(MethodDeclaration n, Object o);
        void visit(TypedMethodHeader n, Object o);
        void visit(VoidMethodHeader n, Object o);
        void visit(MethodDeclarator n, Object o);
        void visit(FormalParameterList n, Object o);
        void visit(FormalParameter n, Object o);
        void visit(ClassTypeList n, Object o);
        void visit(EmptyMethodBody n, Object o);
        void visit(StaticInitializer n, Object o);
        void visit(ConstructorDeclaration n, Object o);
        void visit(ConstructorDeclarator n, Object o);
        void visit(ConstructorBody n, Object o);
        void visit(ThisCall n, Object o);
        void visit(SuperCall n, Object o);
        void visit(InterfaceDeclaration n, Object o);
        void visit(InterfaceBody n, Object o);
        void visit(InterfaceMemberDeclarationList n, Object o);
        void visit(AbstractMethodDeclaration n, Object o);
        void visit(ArrayInitializer n, Object o);
        void visit(VariableInitializerList n, Object o);
        void visit(Block n, Object o);
        void visit(BlockStatementList n, Object o);
        void visit(LocalVariableDeclarationStatement n, Object o);
        void visit(LocalVariableDeclaration n, Object o);
        void visit(EmptyStatement n, Object o);
        void visit(LabeledStatement n, Object o);
        void visit(ExpressionStatement n, Object o);
        void visit(IfStatement n, Object o);
        void visit(SwitchStatement n, Object o);
        void visit(SwitchBlock n, Object o);
        void visit(SwitchBlockStatementList n, Object o);
        void visit(SwitchBlockStatement n, Object o);
        void visit(SwitchLabelList n, Object o);
        void visit(CaseLabel n, Object o);
        void visit(DefaultLabel n, Object o);
        void visit(WhileStatement n, Object o);
        void visit(DoStatement n, Object o);
        void visit(ForStatement n, Object o);
        void visit(StatementExpressionList n, Object o);
        void visit(BreakStatement n, Object o);
        void visit(ContinueStatement n, Object o);
        void visit(ReturnStatement n, Object o);
        void visit(ThrowStatement n, Object o);
        void visit(SynchronizedStatement n, Object o);
        void visit(TryStatement n, Object o);
        void visit(CatchClauseList n, Object o);
        void visit(CatchClause n, Object o);
        void visit(Finally n, Object o);
        void visit(ParenthesizedExpression n, Object o);
        void visit(PrimaryThis n, Object o);
        void visit(PrimaryClassLiteral n, Object o);
        void visit(PrimaryVoidClassLiteral n, Object o);
        void visit(ClassInstanceCreationExpression n, Object o);
        void visit(ExpressionList n, Object o);
        void visit(ArrayCreationExpression n, Object o);
        void visit(DimExprList n, Object o);
        void visit(DimExpr n, Object o);
        void visit(DimList n, Object o);
        void visit(Dim n, Object o);
        void visit(FieldAccess n, Object o);
        void visit(SuperFieldAccess n, Object o);
        void visit(MethodInvocation n, Object o);
        void visit(PrimaryMethodInvocation n, Object o);
        void visit(SuperMethodInvocation n, Object o);
        void visit(ArrayAccess n, Object o);
        void visit(PostIncrementExpression n, Object o);
        void visit(PostDecrementExpression n, Object o);
        void visit(PlusUnaryExpression n, Object o);
        void visit(MinusUnaryExpression n, Object o);
        void visit(PreIncrementExpression n, Object o);
        void visit(PreDecrementExpression n, Object o);
        void visit(UnaryComplementExpression n, Object o);
        void visit(UnaryNotExpression n, Object o);
        void visit(PrimitiveCastExpression n, Object o);
        void visit(ClassCastExpression n, Object o);
        void visit(MultiplyExpression n, Object o);
        void visit(DivideExpression n, Object o);
        void visit(ModExpression n, Object o);
        void visit(AddExpression n, Object o);
        void visit(SubtractExpression n, Object o);
        void visit(LeftShiftExpression n, Object o);
        void visit(RightShiftExpression n, Object o);
        void visit(UnsignedRightShiftExpression n, Object o);
        void visit(LessExpression n, Object o);
        void visit(GreaterExpression n, Object o);
        void visit(LessEqualExpression n, Object o);
        void visit(GreaterEqualExpression n, Object o);
        void visit(InstanceofExpression n, Object o);
        void visit(EqualExpression n, Object o);
        void visit(NotEqualExpression n, Object o);
        void visit(AndExpression n, Object o);
        void visit(ExclusiveOrExpression n, Object o);
        void visit(InclusiveOrExpression n, Object o);
        void visit(ConditionalAndExpression n, Object o);
        void visit(ConditionalOrExpression n, Object o);
        void visit(ConditionalExpression n, Object o);
        void visit(Assignment n, Object o);
        void visit(EqualOperator n, Object o);
        void visit(MultiplyEqualOperator n, Object o);
        void visit(DivideEqualOperator n, Object o);
        void visit(ModEqualOperator n, Object o);
        void visit(PlusEqualOperator n, Object o);
        void visit(MinusEqualOperator n, Object o);
        void visit(LeftShiftEqualOperator n, Object o);
        void visit(RightShiftEqualOperator n, Object o);
        void visit(UnsignedRightShiftEqualOperator n, Object o);
        void visit(AndEqualOperator n, Object o);
        void visit(ExclusiveOrEqualOperator n, Object o);
        void visit(OrEqualOperator n, Object o);
        void visit(Commaopt n, Object o);
        void visit(IDENTIFIERopt n, Object o);
    }
    public interface ResultVisitor
    {
        Object visit(AstToken n);
        Object visit(IntegerLiteral n);
        Object visit(LongLiteral n);
        Object visit(FloatLiteral n);
        Object visit(DoubleLiteral n);
        Object visit(BooleanLiteral n);
        Object visit(CharacterLiteral n);
        Object visit(StringLiteral n);
        Object visit(NullLiteral n);
        Object visit(TrueLiteral n);
        Object visit(FalseLiteral n);
        Object visit(BooleanType n);
        Object visit(ByteType n);
        Object visit(ShortType n);
        Object visit(IntType n);
        Object visit(LongType n);
        Object visit(CharType n);
        Object visit(FloatType n);
        Object visit(DoubleType n);
        Object visit(PrimitiveArrayType n);
        Object visit(ClassOrInterfaceArrayType n);
        Object visit(SimpleName n);
        Object visit(QualifiedName n);
        Object visit(CompilationUnit n);
        Object visit(ImportDeclarationList n);
        Object visit(TypeDeclarationList n);
        Object visit(PackageDeclaration n);
        Object visit(SingleTypeImportDeclaration n);
        Object visit(TypeImportOnDemandDeclaration n);
        Object visit(EmptyDeclaration n);
        Object visit(ModifierList n);
        Object visit(PublicModifier n);
        Object visit(ProtectedModifier n);
        Object visit(PrivateModifier n);
        Object visit(StaticModifier n);
        Object visit(AbstractModifier n);
        Object visit(FinalModifier n);
        Object visit(NativeModifier n);
        Object visit(StrictfpModifier n);
        Object visit(SynchronizedModifier n);
        Object visit(TransientModifier n);
        Object visit(VolatileModifier n);
        Object visit(ClassDeclaration n);
        Object visit(Super n);
        Object visit(InterfaceTypeList n);
        Object visit(ClassBody n);
        Object visit(ClassBodyDeclarationList n);
        Object visit(FieldDeclaration n);
        Object visit(VariableDeclaratorList n);
        Object visit(VariableDeclarator n);
        Object visit(VariableDeclaratorId n);
        Object visit(MethodDeclaration n);
        Object visit(TypedMethodHeader n);
        Object visit(VoidMethodHeader n);
        Object visit(MethodDeclarator n);
        Object visit(FormalParameterList n);
        Object visit(FormalParameter n);
        Object visit(ClassTypeList n);
        Object visit(EmptyMethodBody n);
        Object visit(StaticInitializer n);
        Object visit(ConstructorDeclaration n);
        Object visit(ConstructorDeclarator n);
        Object visit(ConstructorBody n);
        Object visit(ThisCall n);
        Object visit(SuperCall n);
        Object visit(InterfaceDeclaration n);
        Object visit(InterfaceBody n);
        Object visit(InterfaceMemberDeclarationList n);
        Object visit(AbstractMethodDeclaration n);
        Object visit(ArrayInitializer n);
        Object visit(VariableInitializerList n);
        Object visit(Block n);
        Object visit(BlockStatementList n);
        Object visit(LocalVariableDeclarationStatement n);
        Object visit(LocalVariableDeclaration n);
        Object visit(EmptyStatement n);
        Object visit(LabeledStatement n);
        Object visit(ExpressionStatement n);
        Object visit(IfStatement n);
        Object visit(SwitchStatement n);
        Object visit(SwitchBlock n);
        Object visit(SwitchBlockStatementList n);
        Object visit(SwitchBlockStatement n);
        Object visit(SwitchLabelList n);
        Object visit(CaseLabel n);
        Object visit(DefaultLabel n);
        Object visit(WhileStatement n);
        Object visit(DoStatement n);
        Object visit(ForStatement n);
        Object visit(StatementExpressionList n);
        Object visit(BreakStatement n);
        Object visit(ContinueStatement n);
        Object visit(ReturnStatement n);
        Object visit(ThrowStatement n);
        Object visit(SynchronizedStatement n);
        Object visit(TryStatement n);
        Object visit(CatchClauseList n);
        Object visit(CatchClause n);
        Object visit(Finally n);
        Object visit(ParenthesizedExpression n);
        Object visit(PrimaryThis n);
        Object visit(PrimaryClassLiteral n);
        Object visit(PrimaryVoidClassLiteral n);
        Object visit(ClassInstanceCreationExpression n);
        Object visit(ExpressionList n);
        Object visit(ArrayCreationExpression n);
        Object visit(DimExprList n);
        Object visit(DimExpr n);
        Object visit(DimList n);
        Object visit(Dim n);
        Object visit(FieldAccess n);
        Object visit(SuperFieldAccess n);
        Object visit(MethodInvocation n);
        Object visit(PrimaryMethodInvocation n);
        Object visit(SuperMethodInvocation n);
        Object visit(ArrayAccess n);
        Object visit(PostIncrementExpression n);
        Object visit(PostDecrementExpression n);
        Object visit(PlusUnaryExpression n);
        Object visit(MinusUnaryExpression n);
        Object visit(PreIncrementExpression n);
        Object visit(PreDecrementExpression n);
        Object visit(UnaryComplementExpression n);
        Object visit(UnaryNotExpression n);
        Object visit(PrimitiveCastExpression n);
        Object visit(ClassCastExpression n);
        Object visit(MultiplyExpression n);
        Object visit(DivideExpression n);
        Object visit(ModExpression n);
        Object visit(AddExpression n);
        Object visit(SubtractExpression n);
        Object visit(LeftShiftExpression n);
        Object visit(RightShiftExpression n);
        Object visit(UnsignedRightShiftExpression n);
        Object visit(LessExpression n);
        Object visit(GreaterExpression n);
        Object visit(LessEqualExpression n);
        Object visit(GreaterEqualExpression n);
        Object visit(InstanceofExpression n);
        Object visit(EqualExpression n);
        Object visit(NotEqualExpression n);
        Object visit(AndExpression n);
        Object visit(ExclusiveOrExpression n);
        Object visit(InclusiveOrExpression n);
        Object visit(ConditionalAndExpression n);
        Object visit(ConditionalOrExpression n);
        Object visit(ConditionalExpression n);
        Object visit(Assignment n);
        Object visit(EqualOperator n);
        Object visit(MultiplyEqualOperator n);
        Object visit(DivideEqualOperator n);
        Object visit(ModEqualOperator n);
        Object visit(PlusEqualOperator n);
        Object visit(MinusEqualOperator n);
        Object visit(LeftShiftEqualOperator n);
        Object visit(RightShiftEqualOperator n);
        Object visit(UnsignedRightShiftEqualOperator n);
        Object visit(AndEqualOperator n);
        Object visit(ExclusiveOrEqualOperator n);
        Object visit(OrEqualOperator n);
        Object visit(Commaopt n);
        Object visit(IDENTIFIERopt n);
    }
    public interface ResultArgumentVisitor
    {
        Object visit(AstToken n, Object o);
        Object visit(IntegerLiteral n, Object o);
        Object visit(LongLiteral n, Object o);
        Object visit(FloatLiteral n, Object o);
        Object visit(DoubleLiteral n, Object o);
        Object visit(BooleanLiteral n, Object o);
        Object visit(CharacterLiteral n, Object o);
        Object visit(StringLiteral n, Object o);
        Object visit(NullLiteral n, Object o);
        Object visit(TrueLiteral n, Object o);
        Object visit(FalseLiteral n, Object o);
        Object visit(BooleanType n, Object o);
        Object visit(ByteType n, Object o);
        Object visit(ShortType n, Object o);
        Object visit(IntType n, Object o);
        Object visit(LongType n, Object o);
        Object visit(CharType n, Object o);
        Object visit(FloatType n, Object o);
        Object visit(DoubleType n, Object o);
        Object visit(PrimitiveArrayType n, Object o);
        Object visit(ClassOrInterfaceArrayType n, Object o);
        Object visit(SimpleName n, Object o);
        Object visit(QualifiedName n, Object o);
        Object visit(CompilationUnit n, Object o);
        Object visit(ImportDeclarationList n, Object o);
        Object visit(TypeDeclarationList n, Object o);
        Object visit(PackageDeclaration n, Object o);
        Object visit(SingleTypeImportDeclaration n, Object o);
        Object visit(TypeImportOnDemandDeclaration n, Object o);
        Object visit(EmptyDeclaration n, Object o);
        Object visit(ModifierList n, Object o);
        Object visit(PublicModifier n, Object o);
        Object visit(ProtectedModifier n, Object o);
        Object visit(PrivateModifier n, Object o);
        Object visit(StaticModifier n, Object o);
        Object visit(AbstractModifier n, Object o);
        Object visit(FinalModifier n, Object o);
        Object visit(NativeModifier n, Object o);
        Object visit(StrictfpModifier n, Object o);
        Object visit(SynchronizedModifier n, Object o);
        Object visit(TransientModifier n, Object o);
        Object visit(VolatileModifier n, Object o);
        Object visit(ClassDeclaration n, Object o);
        Object visit(Super n, Object o);
        Object visit(InterfaceTypeList n, Object o);
        Object visit(ClassBody n, Object o);
        Object visit(ClassBodyDeclarationList n, Object o);
        Object visit(FieldDeclaration n, Object o);
        Object visit(VariableDeclaratorList n, Object o);
        Object visit(VariableDeclarator n, Object o);
        Object visit(VariableDeclaratorId n, Object o);
        Object visit(MethodDeclaration n, Object o);
        Object visit(TypedMethodHeader n, Object o);
        Object visit(VoidMethodHeader n, Object o);
        Object visit(MethodDeclarator n, Object o);
        Object visit(FormalParameterList n, Object o);
        Object visit(FormalParameter n, Object o);
        Object visit(ClassTypeList n, Object o);
        Object visit(EmptyMethodBody n, Object o);
        Object visit(StaticInitializer n, Object o);
        Object visit(ConstructorDeclaration n, Object o);
        Object visit(ConstructorDeclarator n, Object o);
        Object visit(ConstructorBody n, Object o);
        Object visit(ThisCall n, Object o);
        Object visit(SuperCall n, Object o);
        Object visit(InterfaceDeclaration n, Object o);
        Object visit(InterfaceBody n, Object o);
        Object visit(InterfaceMemberDeclarationList n, Object o);
        Object visit(AbstractMethodDeclaration n, Object o);
        Object visit(ArrayInitializer n, Object o);
        Object visit(VariableInitializerList n, Object o);
        Object visit(Block n, Object o);
        Object visit(BlockStatementList n, Object o);
        Object visit(LocalVariableDeclarationStatement n, Object o);
        Object visit(LocalVariableDeclaration n, Object o);
        Object visit(EmptyStatement n, Object o);
        Object visit(LabeledStatement n, Object o);
        Object visit(ExpressionStatement n, Object o);
        Object visit(IfStatement n, Object o);
        Object visit(SwitchStatement n, Object o);
        Object visit(SwitchBlock n, Object o);
        Object visit(SwitchBlockStatementList n, Object o);
        Object visit(SwitchBlockStatement n, Object o);
        Object visit(SwitchLabelList n, Object o);
        Object visit(CaseLabel n, Object o);
        Object visit(DefaultLabel n, Object o);
        Object visit(WhileStatement n, Object o);
        Object visit(DoStatement n, Object o);
        Object visit(ForStatement n, Object o);
        Object visit(StatementExpressionList n, Object o);
        Object visit(BreakStatement n, Object o);
        Object visit(ContinueStatement n, Object o);
        Object visit(ReturnStatement n, Object o);
        Object visit(ThrowStatement n, Object o);
        Object visit(SynchronizedStatement n, Object o);
        Object visit(TryStatement n, Object o);
        Object visit(CatchClauseList n, Object o);
        Object visit(CatchClause n, Object o);
        Object visit(Finally n, Object o);
        Object visit(ParenthesizedExpression n, Object o);
        Object visit(PrimaryThis n, Object o);
        Object visit(PrimaryClassLiteral n, Object o);
        Object visit(PrimaryVoidClassLiteral n, Object o);
        Object visit(ClassInstanceCreationExpression n, Object o);
        Object visit(ExpressionList n, Object o);
        Object visit(ArrayCreationExpression n, Object o);
        Object visit(DimExprList n, Object o);
        Object visit(DimExpr n, Object o);
        Object visit(DimList n, Object o);
        Object visit(Dim n, Object o);
        Object visit(FieldAccess n, Object o);
        Object visit(SuperFieldAccess n, Object o);
        Object visit(MethodInvocation n, Object o);
        Object visit(PrimaryMethodInvocation n, Object o);
        Object visit(SuperMethodInvocation n, Object o);
        Object visit(ArrayAccess n, Object o);
        Object visit(PostIncrementExpression n, Object o);
        Object visit(PostDecrementExpression n, Object o);
        Object visit(PlusUnaryExpression n, Object o);
        Object visit(MinusUnaryExpression n, Object o);
        Object visit(PreIncrementExpression n, Object o);
        Object visit(PreDecrementExpression n, Object o);
        Object visit(UnaryComplementExpression n, Object o);
        Object visit(UnaryNotExpression n, Object o);
        Object visit(PrimitiveCastExpression n, Object o);
        Object visit(ClassCastExpression n, Object o);
        Object visit(MultiplyExpression n, Object o);
        Object visit(DivideExpression n, Object o);
        Object visit(ModExpression n, Object o);
        Object visit(AddExpression n, Object o);
        Object visit(SubtractExpression n, Object o);
        Object visit(LeftShiftExpression n, Object o);
        Object visit(RightShiftExpression n, Object o);
        Object visit(UnsignedRightShiftExpression n, Object o);
        Object visit(LessExpression n, Object o);
        Object visit(GreaterExpression n, Object o);
        Object visit(LessEqualExpression n, Object o);
        Object visit(GreaterEqualExpression n, Object o);
        Object visit(InstanceofExpression n, Object o);
        Object visit(EqualExpression n, Object o);
        Object visit(NotEqualExpression n, Object o);
        Object visit(AndExpression n, Object o);
        Object visit(ExclusiveOrExpression n, Object o);
        Object visit(InclusiveOrExpression n, Object o);
        Object visit(ConditionalAndExpression n, Object o);
        Object visit(ConditionalOrExpression n, Object o);
        Object visit(ConditionalExpression n, Object o);
        Object visit(Assignment n, Object o);
        Object visit(EqualOperator n, Object o);
        Object visit(MultiplyEqualOperator n, Object o);
        Object visit(DivideEqualOperator n, Object o);
        Object visit(ModEqualOperator n, Object o);
        Object visit(PlusEqualOperator n, Object o);
        Object visit(MinusEqualOperator n, Object o);
        Object visit(LeftShiftEqualOperator n, Object o);
        Object visit(RightShiftEqualOperator n, Object o);
        Object visit(UnsignedRightShiftEqualOperator n, Object o);
        Object visit(AndEqualOperator n, Object o);
        Object visit(ExclusiveOrEqualOperator n, Object o);
        Object visit(OrEqualOperator n, Object o);
        Object visit(Commaopt n, Object o);
        Object visit(IDENTIFIERopt n, Object o);
    }
    static public abstract class AbstractVisitor implements Visitor, ArgumentVisitor
    {
        public abstract void unimplementedVisitor(String s);

        public void visit(AstToken n) { unimplementedVisitor("visit(AstToken)"); }
        public void visit(AstToken n, Object o) { unimplementedVisitor("visit(AstToken, Object)"); }

        public void visit(IntegerLiteral n) { unimplementedVisitor("visit(IntegerLiteral)"); }
        public void visit(IntegerLiteral n, Object o) { unimplementedVisitor("visit(IntegerLiteral, Object)"); }

        public void visit(LongLiteral n) { unimplementedVisitor("visit(LongLiteral)"); }
        public void visit(LongLiteral n, Object o) { unimplementedVisitor("visit(LongLiteral, Object)"); }

        public void visit(FloatLiteral n) { unimplementedVisitor("visit(FloatLiteral)"); }
        public void visit(FloatLiteral n, Object o) { unimplementedVisitor("visit(FloatLiteral, Object)"); }

        public void visit(DoubleLiteral n) { unimplementedVisitor("visit(DoubleLiteral)"); }
        public void visit(DoubleLiteral n, Object o) { unimplementedVisitor("visit(DoubleLiteral, Object)"); }

        public void visit(BooleanLiteral n) { unimplementedVisitor("visit(BooleanLiteral)"); }
        public void visit(BooleanLiteral n, Object o) { unimplementedVisitor("visit(BooleanLiteral, Object)"); }

        public void visit(CharacterLiteral n) { unimplementedVisitor("visit(CharacterLiteral)"); }
        public void visit(CharacterLiteral n, Object o) { unimplementedVisitor("visit(CharacterLiteral, Object)"); }

        public void visit(StringLiteral n) { unimplementedVisitor("visit(StringLiteral)"); }
        public void visit(StringLiteral n, Object o) { unimplementedVisitor("visit(StringLiteral, Object)"); }

        public void visit(NullLiteral n) { unimplementedVisitor("visit(NullLiteral)"); }
        public void visit(NullLiteral n, Object o) { unimplementedVisitor("visit(NullLiteral, Object)"); }

        public void visit(TrueLiteral n) { unimplementedVisitor("visit(TrueLiteral)"); }
        public void visit(TrueLiteral n, Object o) { unimplementedVisitor("visit(TrueLiteral, Object)"); }

        public void visit(FalseLiteral n) { unimplementedVisitor("visit(FalseLiteral)"); }
        public void visit(FalseLiteral n, Object o) { unimplementedVisitor("visit(FalseLiteral, Object)"); }

        public void visit(BooleanType n) { unimplementedVisitor("visit(BooleanType)"); }
        public void visit(BooleanType n, Object o) { unimplementedVisitor("visit(BooleanType, Object)"); }

        public void visit(ByteType n) { unimplementedVisitor("visit(ByteType)"); }
        public void visit(ByteType n, Object o) { unimplementedVisitor("visit(ByteType, Object)"); }

        public void visit(ShortType n) { unimplementedVisitor("visit(ShortType)"); }
        public void visit(ShortType n, Object o) { unimplementedVisitor("visit(ShortType, Object)"); }

        public void visit(IntType n) { unimplementedVisitor("visit(IntType)"); }
        public void visit(IntType n, Object o) { unimplementedVisitor("visit(IntType, Object)"); }

        public void visit(LongType n) { unimplementedVisitor("visit(LongType)"); }
        public void visit(LongType n, Object o) { unimplementedVisitor("visit(LongType, Object)"); }

        public void visit(CharType n) { unimplementedVisitor("visit(CharType)"); }
        public void visit(CharType n, Object o) { unimplementedVisitor("visit(CharType, Object)"); }

        public void visit(FloatType n) { unimplementedVisitor("visit(FloatType)"); }
        public void visit(FloatType n, Object o) { unimplementedVisitor("visit(FloatType, Object)"); }

        public void visit(DoubleType n) { unimplementedVisitor("visit(DoubleType)"); }
        public void visit(DoubleType n, Object o) { unimplementedVisitor("visit(DoubleType, Object)"); }

        public void visit(PrimitiveArrayType n) { unimplementedVisitor("visit(PrimitiveArrayType)"); }
        public void visit(PrimitiveArrayType n, Object o) { unimplementedVisitor("visit(PrimitiveArrayType, Object)"); }

        public void visit(ClassOrInterfaceArrayType n) { unimplementedVisitor("visit(ClassOrInterfaceArrayType)"); }
        public void visit(ClassOrInterfaceArrayType n, Object o) { unimplementedVisitor("visit(ClassOrInterfaceArrayType, Object)"); }

        public void visit(SimpleName n) { unimplementedVisitor("visit(SimpleName)"); }
        public void visit(SimpleName n, Object o) { unimplementedVisitor("visit(SimpleName, Object)"); }

        public void visit(QualifiedName n) { unimplementedVisitor("visit(QualifiedName)"); }
        public void visit(QualifiedName n, Object o) { unimplementedVisitor("visit(QualifiedName, Object)"); }

        public void visit(CompilationUnit n) { unimplementedVisitor("visit(CompilationUnit)"); }
        public void visit(CompilationUnit n, Object o) { unimplementedVisitor("visit(CompilationUnit, Object)"); }

        public void visit(ImportDeclarationList n) { unimplementedVisitor("visit(ImportDeclarationList)"); }
        public void visit(ImportDeclarationList n, Object o) { unimplementedVisitor("visit(ImportDeclarationList, Object)"); }

        public void visit(TypeDeclarationList n) { unimplementedVisitor("visit(TypeDeclarationList)"); }
        public void visit(TypeDeclarationList n, Object o) { unimplementedVisitor("visit(TypeDeclarationList, Object)"); }

        public void visit(PackageDeclaration n) { unimplementedVisitor("visit(PackageDeclaration)"); }
        public void visit(PackageDeclaration n, Object o) { unimplementedVisitor("visit(PackageDeclaration, Object)"); }

        public void visit(SingleTypeImportDeclaration n) { unimplementedVisitor("visit(SingleTypeImportDeclaration)"); }
        public void visit(SingleTypeImportDeclaration n, Object o) { unimplementedVisitor("visit(SingleTypeImportDeclaration, Object)"); }

        public void visit(TypeImportOnDemandDeclaration n) { unimplementedVisitor("visit(TypeImportOnDemandDeclaration)"); }
        public void visit(TypeImportOnDemandDeclaration n, Object o) { unimplementedVisitor("visit(TypeImportOnDemandDeclaration, Object)"); }

        public void visit(EmptyDeclaration n) { unimplementedVisitor("visit(EmptyDeclaration)"); }
        public void visit(EmptyDeclaration n, Object o) { unimplementedVisitor("visit(EmptyDeclaration, Object)"); }

        public void visit(ModifierList n) { unimplementedVisitor("visit(ModifierList)"); }
        public void visit(ModifierList n, Object o) { unimplementedVisitor("visit(ModifierList, Object)"); }

        public void visit(PublicModifier n) { unimplementedVisitor("visit(PublicModifier)"); }
        public void visit(PublicModifier n, Object o) { unimplementedVisitor("visit(PublicModifier, Object)"); }

        public void visit(ProtectedModifier n) { unimplementedVisitor("visit(ProtectedModifier)"); }
        public void visit(ProtectedModifier n, Object o) { unimplementedVisitor("visit(ProtectedModifier, Object)"); }

        public void visit(PrivateModifier n) { unimplementedVisitor("visit(PrivateModifier)"); }
        public void visit(PrivateModifier n, Object o) { unimplementedVisitor("visit(PrivateModifier, Object)"); }

        public void visit(StaticModifier n) { unimplementedVisitor("visit(StaticModifier)"); }
        public void visit(StaticModifier n, Object o) { unimplementedVisitor("visit(StaticModifier, Object)"); }

        public void visit(AbstractModifier n) { unimplementedVisitor("visit(AbstractModifier)"); }
        public void visit(AbstractModifier n, Object o) { unimplementedVisitor("visit(AbstractModifier, Object)"); }

        public void visit(FinalModifier n) { unimplementedVisitor("visit(FinalModifier)"); }
        public void visit(FinalModifier n, Object o) { unimplementedVisitor("visit(FinalModifier, Object)"); }

        public void visit(NativeModifier n) { unimplementedVisitor("visit(NativeModifier)"); }
        public void visit(NativeModifier n, Object o) { unimplementedVisitor("visit(NativeModifier, Object)"); }

        public void visit(StrictfpModifier n) { unimplementedVisitor("visit(StrictfpModifier)"); }
        public void visit(StrictfpModifier n, Object o) { unimplementedVisitor("visit(StrictfpModifier, Object)"); }

        public void visit(SynchronizedModifier n) { unimplementedVisitor("visit(SynchronizedModifier)"); }
        public void visit(SynchronizedModifier n, Object o) { unimplementedVisitor("visit(SynchronizedModifier, Object)"); }

        public void visit(TransientModifier n) { unimplementedVisitor("visit(TransientModifier)"); }
        public void visit(TransientModifier n, Object o) { unimplementedVisitor("visit(TransientModifier, Object)"); }

        public void visit(VolatileModifier n) { unimplementedVisitor("visit(VolatileModifier)"); }
        public void visit(VolatileModifier n, Object o) { unimplementedVisitor("visit(VolatileModifier, Object)"); }

        public void visit(ClassDeclaration n) { unimplementedVisitor("visit(ClassDeclaration)"); }
        public void visit(ClassDeclaration n, Object o) { unimplementedVisitor("visit(ClassDeclaration, Object)"); }

        public void visit(Super n) { unimplementedVisitor("visit(Super)"); }
        public void visit(Super n, Object o) { unimplementedVisitor("visit(Super, Object)"); }

        public void visit(InterfaceTypeList n) { unimplementedVisitor("visit(InterfaceTypeList)"); }
        public void visit(InterfaceTypeList n, Object o) { unimplementedVisitor("visit(InterfaceTypeList, Object)"); }

        public void visit(ClassBody n) { unimplementedVisitor("visit(ClassBody)"); }
        public void visit(ClassBody n, Object o) { unimplementedVisitor("visit(ClassBody, Object)"); }

        public void visit(ClassBodyDeclarationList n) { unimplementedVisitor("visit(ClassBodyDeclarationList)"); }
        public void visit(ClassBodyDeclarationList n, Object o) { unimplementedVisitor("visit(ClassBodyDeclarationList, Object)"); }

        public void visit(FieldDeclaration n) { unimplementedVisitor("visit(FieldDeclaration)"); }
        public void visit(FieldDeclaration n, Object o) { unimplementedVisitor("visit(FieldDeclaration, Object)"); }

        public void visit(VariableDeclaratorList n) { unimplementedVisitor("visit(VariableDeclaratorList)"); }
        public void visit(VariableDeclaratorList n, Object o) { unimplementedVisitor("visit(VariableDeclaratorList, Object)"); }

        public void visit(VariableDeclarator n) { unimplementedVisitor("visit(VariableDeclarator)"); }
        public void visit(VariableDeclarator n, Object o) { unimplementedVisitor("visit(VariableDeclarator, Object)"); }

        public void visit(VariableDeclaratorId n) { unimplementedVisitor("visit(VariableDeclaratorId)"); }
        public void visit(VariableDeclaratorId n, Object o) { unimplementedVisitor("visit(VariableDeclaratorId, Object)"); }

        public void visit(MethodDeclaration n) { unimplementedVisitor("visit(MethodDeclaration)"); }
        public void visit(MethodDeclaration n, Object o) { unimplementedVisitor("visit(MethodDeclaration, Object)"); }

        public void visit(TypedMethodHeader n) { unimplementedVisitor("visit(TypedMethodHeader)"); }
        public void visit(TypedMethodHeader n, Object o) { unimplementedVisitor("visit(TypedMethodHeader, Object)"); }

        public void visit(VoidMethodHeader n) { unimplementedVisitor("visit(VoidMethodHeader)"); }
        public void visit(VoidMethodHeader n, Object o) { unimplementedVisitor("visit(VoidMethodHeader, Object)"); }

        public void visit(MethodDeclarator n) { unimplementedVisitor("visit(MethodDeclarator)"); }
        public void visit(MethodDeclarator n, Object o) { unimplementedVisitor("visit(MethodDeclarator, Object)"); }

        public void visit(FormalParameterList n) { unimplementedVisitor("visit(FormalParameterList)"); }
        public void visit(FormalParameterList n, Object o) { unimplementedVisitor("visit(FormalParameterList, Object)"); }

        public void visit(FormalParameter n) { unimplementedVisitor("visit(FormalParameter)"); }
        public void visit(FormalParameter n, Object o) { unimplementedVisitor("visit(FormalParameter, Object)"); }

        public void visit(ClassTypeList n) { unimplementedVisitor("visit(ClassTypeList)"); }
        public void visit(ClassTypeList n, Object o) { unimplementedVisitor("visit(ClassTypeList, Object)"); }

        public void visit(EmptyMethodBody n) { unimplementedVisitor("visit(EmptyMethodBody)"); }
        public void visit(EmptyMethodBody n, Object o) { unimplementedVisitor("visit(EmptyMethodBody, Object)"); }

        public void visit(StaticInitializer n) { unimplementedVisitor("visit(StaticInitializer)"); }
        public void visit(StaticInitializer n, Object o) { unimplementedVisitor("visit(StaticInitializer, Object)"); }

        public void visit(ConstructorDeclaration n) { unimplementedVisitor("visit(ConstructorDeclaration)"); }
        public void visit(ConstructorDeclaration n, Object o) { unimplementedVisitor("visit(ConstructorDeclaration, Object)"); }

        public void visit(ConstructorDeclarator n) { unimplementedVisitor("visit(ConstructorDeclarator)"); }
        public void visit(ConstructorDeclarator n, Object o) { unimplementedVisitor("visit(ConstructorDeclarator, Object)"); }

        public void visit(ConstructorBody n) { unimplementedVisitor("visit(ConstructorBody)"); }
        public void visit(ConstructorBody n, Object o) { unimplementedVisitor("visit(ConstructorBody, Object)"); }

        public void visit(ThisCall n) { unimplementedVisitor("visit(ThisCall)"); }
        public void visit(ThisCall n, Object o) { unimplementedVisitor("visit(ThisCall, Object)"); }

        public void visit(SuperCall n) { unimplementedVisitor("visit(SuperCall)"); }
        public void visit(SuperCall n, Object o) { unimplementedVisitor("visit(SuperCall, Object)"); }

        public void visit(InterfaceDeclaration n) { unimplementedVisitor("visit(InterfaceDeclaration)"); }
        public void visit(InterfaceDeclaration n, Object o) { unimplementedVisitor("visit(InterfaceDeclaration, Object)"); }

        public void visit(InterfaceBody n) { unimplementedVisitor("visit(InterfaceBody)"); }
        public void visit(InterfaceBody n, Object o) { unimplementedVisitor("visit(InterfaceBody, Object)"); }

        public void visit(InterfaceMemberDeclarationList n) { unimplementedVisitor("visit(InterfaceMemberDeclarationList)"); }
        public void visit(InterfaceMemberDeclarationList n, Object o) { unimplementedVisitor("visit(InterfaceMemberDeclarationList, Object)"); }

        public void visit(AbstractMethodDeclaration n) { unimplementedVisitor("visit(AbstractMethodDeclaration)"); }
        public void visit(AbstractMethodDeclaration n, Object o) { unimplementedVisitor("visit(AbstractMethodDeclaration, Object)"); }

        public void visit(ArrayInitializer n) { unimplementedVisitor("visit(ArrayInitializer)"); }
        public void visit(ArrayInitializer n, Object o) { unimplementedVisitor("visit(ArrayInitializer, Object)"); }

        public void visit(VariableInitializerList n) { unimplementedVisitor("visit(VariableInitializerList)"); }
        public void visit(VariableInitializerList n, Object o) { unimplementedVisitor("visit(VariableInitializerList, Object)"); }

        public void visit(Block n) { unimplementedVisitor("visit(Block)"); }
        public void visit(Block n, Object o) { unimplementedVisitor("visit(Block, Object)"); }

        public void visit(BlockStatementList n) { unimplementedVisitor("visit(BlockStatementList)"); }
        public void visit(BlockStatementList n, Object o) { unimplementedVisitor("visit(BlockStatementList, Object)"); }

        public void visit(LocalVariableDeclarationStatement n) { unimplementedVisitor("visit(LocalVariableDeclarationStatement)"); }
        public void visit(LocalVariableDeclarationStatement n, Object o) { unimplementedVisitor("visit(LocalVariableDeclarationStatement, Object)"); }

        public void visit(LocalVariableDeclaration n) { unimplementedVisitor("visit(LocalVariableDeclaration)"); }
        public void visit(LocalVariableDeclaration n, Object o) { unimplementedVisitor("visit(LocalVariableDeclaration, Object)"); }

        public void visit(EmptyStatement n) { unimplementedVisitor("visit(EmptyStatement)"); }
        public void visit(EmptyStatement n, Object o) { unimplementedVisitor("visit(EmptyStatement, Object)"); }

        public void visit(LabeledStatement n) { unimplementedVisitor("visit(LabeledStatement)"); }
        public void visit(LabeledStatement n, Object o) { unimplementedVisitor("visit(LabeledStatement, Object)"); }

        public void visit(ExpressionStatement n) { unimplementedVisitor("visit(ExpressionStatement)"); }
        public void visit(ExpressionStatement n, Object o) { unimplementedVisitor("visit(ExpressionStatement, Object)"); }

        public void visit(IfStatement n) { unimplementedVisitor("visit(IfStatement)"); }
        public void visit(IfStatement n, Object o) { unimplementedVisitor("visit(IfStatement, Object)"); }

        public void visit(SwitchStatement n) { unimplementedVisitor("visit(SwitchStatement)"); }
        public void visit(SwitchStatement n, Object o) { unimplementedVisitor("visit(SwitchStatement, Object)"); }

        public void visit(SwitchBlock n) { unimplementedVisitor("visit(SwitchBlock)"); }
        public void visit(SwitchBlock n, Object o) { unimplementedVisitor("visit(SwitchBlock, Object)"); }

        public void visit(SwitchBlockStatementList n) { unimplementedVisitor("visit(SwitchBlockStatementList)"); }
        public void visit(SwitchBlockStatementList n, Object o) { unimplementedVisitor("visit(SwitchBlockStatementList, Object)"); }

        public void visit(SwitchBlockStatement n) { unimplementedVisitor("visit(SwitchBlockStatement)"); }
        public void visit(SwitchBlockStatement n, Object o) { unimplementedVisitor("visit(SwitchBlockStatement, Object)"); }

        public void visit(SwitchLabelList n) { unimplementedVisitor("visit(SwitchLabelList)"); }
        public void visit(SwitchLabelList n, Object o) { unimplementedVisitor("visit(SwitchLabelList, Object)"); }

        public void visit(CaseLabel n) { unimplementedVisitor("visit(CaseLabel)"); }
        public void visit(CaseLabel n, Object o) { unimplementedVisitor("visit(CaseLabel, Object)"); }

        public void visit(DefaultLabel n) { unimplementedVisitor("visit(DefaultLabel)"); }
        public void visit(DefaultLabel n, Object o) { unimplementedVisitor("visit(DefaultLabel, Object)"); }

        public void visit(WhileStatement n) { unimplementedVisitor("visit(WhileStatement)"); }
        public void visit(WhileStatement n, Object o) { unimplementedVisitor("visit(WhileStatement, Object)"); }

        public void visit(DoStatement n) { unimplementedVisitor("visit(DoStatement)"); }
        public void visit(DoStatement n, Object o) { unimplementedVisitor("visit(DoStatement, Object)"); }

        public void visit(ForStatement n) { unimplementedVisitor("visit(ForStatement)"); }
        public void visit(ForStatement n, Object o) { unimplementedVisitor("visit(ForStatement, Object)"); }

        public void visit(StatementExpressionList n) { unimplementedVisitor("visit(StatementExpressionList)"); }
        public void visit(StatementExpressionList n, Object o) { unimplementedVisitor("visit(StatementExpressionList, Object)"); }

        public void visit(BreakStatement n) { unimplementedVisitor("visit(BreakStatement)"); }
        public void visit(BreakStatement n, Object o) { unimplementedVisitor("visit(BreakStatement, Object)"); }

        public void visit(ContinueStatement n) { unimplementedVisitor("visit(ContinueStatement)"); }
        public void visit(ContinueStatement n, Object o) { unimplementedVisitor("visit(ContinueStatement, Object)"); }

        public void visit(ReturnStatement n) { unimplementedVisitor("visit(ReturnStatement)"); }
        public void visit(ReturnStatement n, Object o) { unimplementedVisitor("visit(ReturnStatement, Object)"); }

        public void visit(ThrowStatement n) { unimplementedVisitor("visit(ThrowStatement)"); }
        public void visit(ThrowStatement n, Object o) { unimplementedVisitor("visit(ThrowStatement, Object)"); }

        public void visit(SynchronizedStatement n) { unimplementedVisitor("visit(SynchronizedStatement)"); }
        public void visit(SynchronizedStatement n, Object o) { unimplementedVisitor("visit(SynchronizedStatement, Object)"); }

        public void visit(TryStatement n) { unimplementedVisitor("visit(TryStatement)"); }
        public void visit(TryStatement n, Object o) { unimplementedVisitor("visit(TryStatement, Object)"); }

        public void visit(CatchClauseList n) { unimplementedVisitor("visit(CatchClauseList)"); }
        public void visit(CatchClauseList n, Object o) { unimplementedVisitor("visit(CatchClauseList, Object)"); }

        public void visit(CatchClause n) { unimplementedVisitor("visit(CatchClause)"); }
        public void visit(CatchClause n, Object o) { unimplementedVisitor("visit(CatchClause, Object)"); }

        public void visit(Finally n) { unimplementedVisitor("visit(Finally)"); }
        public void visit(Finally n, Object o) { unimplementedVisitor("visit(Finally, Object)"); }

        public void visit(ParenthesizedExpression n) { unimplementedVisitor("visit(ParenthesizedExpression)"); }
        public void visit(ParenthesizedExpression n, Object o) { unimplementedVisitor("visit(ParenthesizedExpression, Object)"); }

        public void visit(PrimaryThis n) { unimplementedVisitor("visit(PrimaryThis)"); }
        public void visit(PrimaryThis n, Object o) { unimplementedVisitor("visit(PrimaryThis, Object)"); }

        public void visit(PrimaryClassLiteral n) { unimplementedVisitor("visit(PrimaryClassLiteral)"); }
        public void visit(PrimaryClassLiteral n, Object o) { unimplementedVisitor("visit(PrimaryClassLiteral, Object)"); }

        public void visit(PrimaryVoidClassLiteral n) { unimplementedVisitor("visit(PrimaryVoidClassLiteral)"); }
        public void visit(PrimaryVoidClassLiteral n, Object o) { unimplementedVisitor("visit(PrimaryVoidClassLiteral, Object)"); }

        public void visit(ClassInstanceCreationExpression n) { unimplementedVisitor("visit(ClassInstanceCreationExpression)"); }
        public void visit(ClassInstanceCreationExpression n, Object o) { unimplementedVisitor("visit(ClassInstanceCreationExpression, Object)"); }

        public void visit(ExpressionList n) { unimplementedVisitor("visit(ExpressionList)"); }
        public void visit(ExpressionList n, Object o) { unimplementedVisitor("visit(ExpressionList, Object)"); }

        public void visit(ArrayCreationExpression n) { unimplementedVisitor("visit(ArrayCreationExpression)"); }
        public void visit(ArrayCreationExpression n, Object o) { unimplementedVisitor("visit(ArrayCreationExpression, Object)"); }

        public void visit(DimExprList n) { unimplementedVisitor("visit(DimExprList)"); }
        public void visit(DimExprList n, Object o) { unimplementedVisitor("visit(DimExprList, Object)"); }

        public void visit(DimExpr n) { unimplementedVisitor("visit(DimExpr)"); }
        public void visit(DimExpr n, Object o) { unimplementedVisitor("visit(DimExpr, Object)"); }

        public void visit(DimList n) { unimplementedVisitor("visit(DimList)"); }
        public void visit(DimList n, Object o) { unimplementedVisitor("visit(DimList, Object)"); }

        public void visit(Dim n) { unimplementedVisitor("visit(Dim)"); }
        public void visit(Dim n, Object o) { unimplementedVisitor("visit(Dim, Object)"); }

        public void visit(FieldAccess n) { unimplementedVisitor("visit(FieldAccess)"); }
        public void visit(FieldAccess n, Object o) { unimplementedVisitor("visit(FieldAccess, Object)"); }

        public void visit(SuperFieldAccess n) { unimplementedVisitor("visit(SuperFieldAccess)"); }
        public void visit(SuperFieldAccess n, Object o) { unimplementedVisitor("visit(SuperFieldAccess, Object)"); }

        public void visit(MethodInvocation n) { unimplementedVisitor("visit(MethodInvocation)"); }
        public void visit(MethodInvocation n, Object o) { unimplementedVisitor("visit(MethodInvocation, Object)"); }

        public void visit(PrimaryMethodInvocation n) { unimplementedVisitor("visit(PrimaryMethodInvocation)"); }
        public void visit(PrimaryMethodInvocation n, Object o) { unimplementedVisitor("visit(PrimaryMethodInvocation, Object)"); }

        public void visit(SuperMethodInvocation n) { unimplementedVisitor("visit(SuperMethodInvocation)"); }
        public void visit(SuperMethodInvocation n, Object o) { unimplementedVisitor("visit(SuperMethodInvocation, Object)"); }

        public void visit(ArrayAccess n) { unimplementedVisitor("visit(ArrayAccess)"); }
        public void visit(ArrayAccess n, Object o) { unimplementedVisitor("visit(ArrayAccess, Object)"); }

        public void visit(PostIncrementExpression n) { unimplementedVisitor("visit(PostIncrementExpression)"); }
        public void visit(PostIncrementExpression n, Object o) { unimplementedVisitor("visit(PostIncrementExpression, Object)"); }

        public void visit(PostDecrementExpression n) { unimplementedVisitor("visit(PostDecrementExpression)"); }
        public void visit(PostDecrementExpression n, Object o) { unimplementedVisitor("visit(PostDecrementExpression, Object)"); }

        public void visit(PlusUnaryExpression n) { unimplementedVisitor("visit(PlusUnaryExpression)"); }
        public void visit(PlusUnaryExpression n, Object o) { unimplementedVisitor("visit(PlusUnaryExpression, Object)"); }

        public void visit(MinusUnaryExpression n) { unimplementedVisitor("visit(MinusUnaryExpression)"); }
        public void visit(MinusUnaryExpression n, Object o) { unimplementedVisitor("visit(MinusUnaryExpression, Object)"); }

        public void visit(PreIncrementExpression n) { unimplementedVisitor("visit(PreIncrementExpression)"); }
        public void visit(PreIncrementExpression n, Object o) { unimplementedVisitor("visit(PreIncrementExpression, Object)"); }

        public void visit(PreDecrementExpression n) { unimplementedVisitor("visit(PreDecrementExpression)"); }
        public void visit(PreDecrementExpression n, Object o) { unimplementedVisitor("visit(PreDecrementExpression, Object)"); }

        public void visit(UnaryComplementExpression n) { unimplementedVisitor("visit(UnaryComplementExpression)"); }
        public void visit(UnaryComplementExpression n, Object o) { unimplementedVisitor("visit(UnaryComplementExpression, Object)"); }

        public void visit(UnaryNotExpression n) { unimplementedVisitor("visit(UnaryNotExpression)"); }
        public void visit(UnaryNotExpression n, Object o) { unimplementedVisitor("visit(UnaryNotExpression, Object)"); }

        public void visit(PrimitiveCastExpression n) { unimplementedVisitor("visit(PrimitiveCastExpression)"); }
        public void visit(PrimitiveCastExpression n, Object o) { unimplementedVisitor("visit(PrimitiveCastExpression, Object)"); }

        public void visit(ClassCastExpression n) { unimplementedVisitor("visit(ClassCastExpression)"); }
        public void visit(ClassCastExpression n, Object o) { unimplementedVisitor("visit(ClassCastExpression, Object)"); }

        public void visit(MultiplyExpression n) { unimplementedVisitor("visit(MultiplyExpression)"); }
        public void visit(MultiplyExpression n, Object o) { unimplementedVisitor("visit(MultiplyExpression, Object)"); }

        public void visit(DivideExpression n) { unimplementedVisitor("visit(DivideExpression)"); }
        public void visit(DivideExpression n, Object o) { unimplementedVisitor("visit(DivideExpression, Object)"); }

        public void visit(ModExpression n) { unimplementedVisitor("visit(ModExpression)"); }
        public void visit(ModExpression n, Object o) { unimplementedVisitor("visit(ModExpression, Object)"); }

        public void visit(AddExpression n) { unimplementedVisitor("visit(AddExpression)"); }
        public void visit(AddExpression n, Object o) { unimplementedVisitor("visit(AddExpression, Object)"); }

        public void visit(SubtractExpression n) { unimplementedVisitor("visit(SubtractExpression)"); }
        public void visit(SubtractExpression n, Object o) { unimplementedVisitor("visit(SubtractExpression, Object)"); }

        public void visit(LeftShiftExpression n) { unimplementedVisitor("visit(LeftShiftExpression)"); }
        public void visit(LeftShiftExpression n, Object o) { unimplementedVisitor("visit(LeftShiftExpression, Object)"); }

        public void visit(RightShiftExpression n) { unimplementedVisitor("visit(RightShiftExpression)"); }
        public void visit(RightShiftExpression n, Object o) { unimplementedVisitor("visit(RightShiftExpression, Object)"); }

        public void visit(UnsignedRightShiftExpression n) { unimplementedVisitor("visit(UnsignedRightShiftExpression)"); }
        public void visit(UnsignedRightShiftExpression n, Object o) { unimplementedVisitor("visit(UnsignedRightShiftExpression, Object)"); }

        public void visit(LessExpression n) { unimplementedVisitor("visit(LessExpression)"); }
        public void visit(LessExpression n, Object o) { unimplementedVisitor("visit(LessExpression, Object)"); }

        public void visit(GreaterExpression n) { unimplementedVisitor("visit(GreaterExpression)"); }
        public void visit(GreaterExpression n, Object o) { unimplementedVisitor("visit(GreaterExpression, Object)"); }

        public void visit(LessEqualExpression n) { unimplementedVisitor("visit(LessEqualExpression)"); }
        public void visit(LessEqualExpression n, Object o) { unimplementedVisitor("visit(LessEqualExpression, Object)"); }

        public void visit(GreaterEqualExpression n) { unimplementedVisitor("visit(GreaterEqualExpression)"); }
        public void visit(GreaterEqualExpression n, Object o) { unimplementedVisitor("visit(GreaterEqualExpression, Object)"); }

        public void visit(InstanceofExpression n) { unimplementedVisitor("visit(InstanceofExpression)"); }
        public void visit(InstanceofExpression n, Object o) { unimplementedVisitor("visit(InstanceofExpression, Object)"); }

        public void visit(EqualExpression n) { unimplementedVisitor("visit(EqualExpression)"); }
        public void visit(EqualExpression n, Object o) { unimplementedVisitor("visit(EqualExpression, Object)"); }

        public void visit(NotEqualExpression n) { unimplementedVisitor("visit(NotEqualExpression)"); }
        public void visit(NotEqualExpression n, Object o) { unimplementedVisitor("visit(NotEqualExpression, Object)"); }

        public void visit(AndExpression n) { unimplementedVisitor("visit(AndExpression)"); }
        public void visit(AndExpression n, Object o) { unimplementedVisitor("visit(AndExpression, Object)"); }

        public void visit(ExclusiveOrExpression n) { unimplementedVisitor("visit(ExclusiveOrExpression)"); }
        public void visit(ExclusiveOrExpression n, Object o) { unimplementedVisitor("visit(ExclusiveOrExpression, Object)"); }

        public void visit(InclusiveOrExpression n) { unimplementedVisitor("visit(InclusiveOrExpression)"); }
        public void visit(InclusiveOrExpression n, Object o) { unimplementedVisitor("visit(InclusiveOrExpression, Object)"); }

        public void visit(ConditionalAndExpression n) { unimplementedVisitor("visit(ConditionalAndExpression)"); }
        public void visit(ConditionalAndExpression n, Object o) { unimplementedVisitor("visit(ConditionalAndExpression, Object)"); }

        public void visit(ConditionalOrExpression n) { unimplementedVisitor("visit(ConditionalOrExpression)"); }
        public void visit(ConditionalOrExpression n, Object o) { unimplementedVisitor("visit(ConditionalOrExpression, Object)"); }

        public void visit(ConditionalExpression n) { unimplementedVisitor("visit(ConditionalExpression)"); }
        public void visit(ConditionalExpression n, Object o) { unimplementedVisitor("visit(ConditionalExpression, Object)"); }

        public void visit(Assignment n) { unimplementedVisitor("visit(Assignment)"); }
        public void visit(Assignment n, Object o) { unimplementedVisitor("visit(Assignment, Object)"); }

        public void visit(EqualOperator n) { unimplementedVisitor("visit(EqualOperator)"); }
        public void visit(EqualOperator n, Object o) { unimplementedVisitor("visit(EqualOperator, Object)"); }

        public void visit(MultiplyEqualOperator n) { unimplementedVisitor("visit(MultiplyEqualOperator)"); }
        public void visit(MultiplyEqualOperator n, Object o) { unimplementedVisitor("visit(MultiplyEqualOperator, Object)"); }

        public void visit(DivideEqualOperator n) { unimplementedVisitor("visit(DivideEqualOperator)"); }
        public void visit(DivideEqualOperator n, Object o) { unimplementedVisitor("visit(DivideEqualOperator, Object)"); }

        public void visit(ModEqualOperator n) { unimplementedVisitor("visit(ModEqualOperator)"); }
        public void visit(ModEqualOperator n, Object o) { unimplementedVisitor("visit(ModEqualOperator, Object)"); }

        public void visit(PlusEqualOperator n) { unimplementedVisitor("visit(PlusEqualOperator)"); }
        public void visit(PlusEqualOperator n, Object o) { unimplementedVisitor("visit(PlusEqualOperator, Object)"); }

        public void visit(MinusEqualOperator n) { unimplementedVisitor("visit(MinusEqualOperator)"); }
        public void visit(MinusEqualOperator n, Object o) { unimplementedVisitor("visit(MinusEqualOperator, Object)"); }

        public void visit(LeftShiftEqualOperator n) { unimplementedVisitor("visit(LeftShiftEqualOperator)"); }
        public void visit(LeftShiftEqualOperator n, Object o) { unimplementedVisitor("visit(LeftShiftEqualOperator, Object)"); }

        public void visit(RightShiftEqualOperator n) { unimplementedVisitor("visit(RightShiftEqualOperator)"); }
        public void visit(RightShiftEqualOperator n, Object o) { unimplementedVisitor("visit(RightShiftEqualOperator, Object)"); }

        public void visit(UnsignedRightShiftEqualOperator n) { unimplementedVisitor("visit(UnsignedRightShiftEqualOperator)"); }
        public void visit(UnsignedRightShiftEqualOperator n, Object o) { unimplementedVisitor("visit(UnsignedRightShiftEqualOperator, Object)"); }

        public void visit(AndEqualOperator n) { unimplementedVisitor("visit(AndEqualOperator)"); }
        public void visit(AndEqualOperator n, Object o) { unimplementedVisitor("visit(AndEqualOperator, Object)"); }

        public void visit(ExclusiveOrEqualOperator n) { unimplementedVisitor("visit(ExclusiveOrEqualOperator)"); }
        public void visit(ExclusiveOrEqualOperator n, Object o) { unimplementedVisitor("visit(ExclusiveOrEqualOperator, Object)"); }

        public void visit(OrEqualOperator n) { unimplementedVisitor("visit(OrEqualOperator)"); }
        public void visit(OrEqualOperator n, Object o) { unimplementedVisitor("visit(OrEqualOperator, Object)"); }

        public void visit(Commaopt n) { unimplementedVisitor("visit(Commaopt)"); }
        public void visit(Commaopt n, Object o) { unimplementedVisitor("visit(Commaopt, Object)"); }

        public void visit(IDENTIFIERopt n) { unimplementedVisitor("visit(IDENTIFIERopt)"); }
        public void visit(IDENTIFIERopt n, Object o) { unimplementedVisitor("visit(IDENTIFIERopt, Object)"); }
    }
    static public abstract class AbstractResultVisitor implements ResultVisitor, ResultArgumentVisitor
    {
        public abstract Object unimplementedVisitor(String s);

        public Object visit(AstToken n) { return unimplementedVisitor("visit(AstToken)"); }
        public Object visit(AstToken n, Object o) { return  unimplementedVisitor("visit(AstToken, Object)"); }

        public Object visit(IntegerLiteral n) { return unimplementedVisitor("visit(IntegerLiteral)"); }
        public Object visit(IntegerLiteral n, Object o) { return  unimplementedVisitor("visit(IntegerLiteral, Object)"); }

        public Object visit(LongLiteral n) { return unimplementedVisitor("visit(LongLiteral)"); }
        public Object visit(LongLiteral n, Object o) { return  unimplementedVisitor("visit(LongLiteral, Object)"); }

        public Object visit(FloatLiteral n) { return unimplementedVisitor("visit(FloatLiteral)"); }
        public Object visit(FloatLiteral n, Object o) { return  unimplementedVisitor("visit(FloatLiteral, Object)"); }

        public Object visit(DoubleLiteral n) { return unimplementedVisitor("visit(DoubleLiteral)"); }
        public Object visit(DoubleLiteral n, Object o) { return  unimplementedVisitor("visit(DoubleLiteral, Object)"); }

        public Object visit(BooleanLiteral n) { return unimplementedVisitor("visit(BooleanLiteral)"); }
        public Object visit(BooleanLiteral n, Object o) { return  unimplementedVisitor("visit(BooleanLiteral, Object)"); }

        public Object visit(CharacterLiteral n) { return unimplementedVisitor("visit(CharacterLiteral)"); }
        public Object visit(CharacterLiteral n, Object o) { return  unimplementedVisitor("visit(CharacterLiteral, Object)"); }

        public Object visit(StringLiteral n) { return unimplementedVisitor("visit(StringLiteral)"); }
        public Object visit(StringLiteral n, Object o) { return  unimplementedVisitor("visit(StringLiteral, Object)"); }

        public Object visit(NullLiteral n) { return unimplementedVisitor("visit(NullLiteral)"); }
        public Object visit(NullLiteral n, Object o) { return  unimplementedVisitor("visit(NullLiteral, Object)"); }

        public Object visit(TrueLiteral n) { return unimplementedVisitor("visit(TrueLiteral)"); }
        public Object visit(TrueLiteral n, Object o) { return  unimplementedVisitor("visit(TrueLiteral, Object)"); }

        public Object visit(FalseLiteral n) { return unimplementedVisitor("visit(FalseLiteral)"); }
        public Object visit(FalseLiteral n, Object o) { return  unimplementedVisitor("visit(FalseLiteral, Object)"); }

        public Object visit(BooleanType n) { return unimplementedVisitor("visit(BooleanType)"); }
        public Object visit(BooleanType n, Object o) { return  unimplementedVisitor("visit(BooleanType, Object)"); }

        public Object visit(ByteType n) { return unimplementedVisitor("visit(ByteType)"); }
        public Object visit(ByteType n, Object o) { return  unimplementedVisitor("visit(ByteType, Object)"); }

        public Object visit(ShortType n) { return unimplementedVisitor("visit(ShortType)"); }
        public Object visit(ShortType n, Object o) { return  unimplementedVisitor("visit(ShortType, Object)"); }

        public Object visit(IntType n) { return unimplementedVisitor("visit(IntType)"); }
        public Object visit(IntType n, Object o) { return  unimplementedVisitor("visit(IntType, Object)"); }

        public Object visit(LongType n) { return unimplementedVisitor("visit(LongType)"); }
        public Object visit(LongType n, Object o) { return  unimplementedVisitor("visit(LongType, Object)"); }

        public Object visit(CharType n) { return unimplementedVisitor("visit(CharType)"); }
        public Object visit(CharType n, Object o) { return  unimplementedVisitor("visit(CharType, Object)"); }

        public Object visit(FloatType n) { return unimplementedVisitor("visit(FloatType)"); }
        public Object visit(FloatType n, Object o) { return  unimplementedVisitor("visit(FloatType, Object)"); }

        public Object visit(DoubleType n) { return unimplementedVisitor("visit(DoubleType)"); }
        public Object visit(DoubleType n, Object o) { return  unimplementedVisitor("visit(DoubleType, Object)"); }

        public Object visit(PrimitiveArrayType n) { return unimplementedVisitor("visit(PrimitiveArrayType)"); }
        public Object visit(PrimitiveArrayType n, Object o) { return  unimplementedVisitor("visit(PrimitiveArrayType, Object)"); }

        public Object visit(ClassOrInterfaceArrayType n) { return unimplementedVisitor("visit(ClassOrInterfaceArrayType)"); }
        public Object visit(ClassOrInterfaceArrayType n, Object o) { return  unimplementedVisitor("visit(ClassOrInterfaceArrayType, Object)"); }

        public Object visit(SimpleName n) { return unimplementedVisitor("visit(SimpleName)"); }
        public Object visit(SimpleName n, Object o) { return  unimplementedVisitor("visit(SimpleName, Object)"); }

        public Object visit(QualifiedName n) { return unimplementedVisitor("visit(QualifiedName)"); }
        public Object visit(QualifiedName n, Object o) { return  unimplementedVisitor("visit(QualifiedName, Object)"); }

        public Object visit(CompilationUnit n) { return unimplementedVisitor("visit(CompilationUnit)"); }
        public Object visit(CompilationUnit n, Object o) { return  unimplementedVisitor("visit(CompilationUnit, Object)"); }

        public Object visit(ImportDeclarationList n) { return unimplementedVisitor("visit(ImportDeclarationList)"); }
        public Object visit(ImportDeclarationList n, Object o) { return  unimplementedVisitor("visit(ImportDeclarationList, Object)"); }

        public Object visit(TypeDeclarationList n) { return unimplementedVisitor("visit(TypeDeclarationList)"); }
        public Object visit(TypeDeclarationList n, Object o) { return  unimplementedVisitor("visit(TypeDeclarationList, Object)"); }

        public Object visit(PackageDeclaration n) { return unimplementedVisitor("visit(PackageDeclaration)"); }
        public Object visit(PackageDeclaration n, Object o) { return  unimplementedVisitor("visit(PackageDeclaration, Object)"); }

        public Object visit(SingleTypeImportDeclaration n) { return unimplementedVisitor("visit(SingleTypeImportDeclaration)"); }
        public Object visit(SingleTypeImportDeclaration n, Object o) { return  unimplementedVisitor("visit(SingleTypeImportDeclaration, Object)"); }

        public Object visit(TypeImportOnDemandDeclaration n) { return unimplementedVisitor("visit(TypeImportOnDemandDeclaration)"); }
        public Object visit(TypeImportOnDemandDeclaration n, Object o) { return  unimplementedVisitor("visit(TypeImportOnDemandDeclaration, Object)"); }

        public Object visit(EmptyDeclaration n) { return unimplementedVisitor("visit(EmptyDeclaration)"); }
        public Object visit(EmptyDeclaration n, Object o) { return  unimplementedVisitor("visit(EmptyDeclaration, Object)"); }

        public Object visit(ModifierList n) { return unimplementedVisitor("visit(ModifierList)"); }
        public Object visit(ModifierList n, Object o) { return  unimplementedVisitor("visit(ModifierList, Object)"); }

        public Object visit(PublicModifier n) { return unimplementedVisitor("visit(PublicModifier)"); }
        public Object visit(PublicModifier n, Object o) { return  unimplementedVisitor("visit(PublicModifier, Object)"); }

        public Object visit(ProtectedModifier n) { return unimplementedVisitor("visit(ProtectedModifier)"); }
        public Object visit(ProtectedModifier n, Object o) { return  unimplementedVisitor("visit(ProtectedModifier, Object)"); }

        public Object visit(PrivateModifier n) { return unimplementedVisitor("visit(PrivateModifier)"); }
        public Object visit(PrivateModifier n, Object o) { return  unimplementedVisitor("visit(PrivateModifier, Object)"); }

        public Object visit(StaticModifier n) { return unimplementedVisitor("visit(StaticModifier)"); }
        public Object visit(StaticModifier n, Object o) { return  unimplementedVisitor("visit(StaticModifier, Object)"); }

        public Object visit(AbstractModifier n) { return unimplementedVisitor("visit(AbstractModifier)"); }
        public Object visit(AbstractModifier n, Object o) { return  unimplementedVisitor("visit(AbstractModifier, Object)"); }

        public Object visit(FinalModifier n) { return unimplementedVisitor("visit(FinalModifier)"); }
        public Object visit(FinalModifier n, Object o) { return  unimplementedVisitor("visit(FinalModifier, Object)"); }

        public Object visit(NativeModifier n) { return unimplementedVisitor("visit(NativeModifier)"); }
        public Object visit(NativeModifier n, Object o) { return  unimplementedVisitor("visit(NativeModifier, Object)"); }

        public Object visit(StrictfpModifier n) { return unimplementedVisitor("visit(StrictfpModifier)"); }
        public Object visit(StrictfpModifier n, Object o) { return  unimplementedVisitor("visit(StrictfpModifier, Object)"); }

        public Object visit(SynchronizedModifier n) { return unimplementedVisitor("visit(SynchronizedModifier)"); }
        public Object visit(SynchronizedModifier n, Object o) { return  unimplementedVisitor("visit(SynchronizedModifier, Object)"); }

        public Object visit(TransientModifier n) { return unimplementedVisitor("visit(TransientModifier)"); }
        public Object visit(TransientModifier n, Object o) { return  unimplementedVisitor("visit(TransientModifier, Object)"); }

        public Object visit(VolatileModifier n) { return unimplementedVisitor("visit(VolatileModifier)"); }
        public Object visit(VolatileModifier n, Object o) { return  unimplementedVisitor("visit(VolatileModifier, Object)"); }

        public Object visit(ClassDeclaration n) { return unimplementedVisitor("visit(ClassDeclaration)"); }
        public Object visit(ClassDeclaration n, Object o) { return  unimplementedVisitor("visit(ClassDeclaration, Object)"); }

        public Object visit(Super n) { return unimplementedVisitor("visit(Super)"); }
        public Object visit(Super n, Object o) { return  unimplementedVisitor("visit(Super, Object)"); }

        public Object visit(InterfaceTypeList n) { return unimplementedVisitor("visit(InterfaceTypeList)"); }
        public Object visit(InterfaceTypeList n, Object o) { return  unimplementedVisitor("visit(InterfaceTypeList, Object)"); }

        public Object visit(ClassBody n) { return unimplementedVisitor("visit(ClassBody)"); }
        public Object visit(ClassBody n, Object o) { return  unimplementedVisitor("visit(ClassBody, Object)"); }

        public Object visit(ClassBodyDeclarationList n) { return unimplementedVisitor("visit(ClassBodyDeclarationList)"); }
        public Object visit(ClassBodyDeclarationList n, Object o) { return  unimplementedVisitor("visit(ClassBodyDeclarationList, Object)"); }

        public Object visit(FieldDeclaration n) { return unimplementedVisitor("visit(FieldDeclaration)"); }
        public Object visit(FieldDeclaration n, Object o) { return  unimplementedVisitor("visit(FieldDeclaration, Object)"); }

        public Object visit(VariableDeclaratorList n) { return unimplementedVisitor("visit(VariableDeclaratorList)"); }
        public Object visit(VariableDeclaratorList n, Object o) { return  unimplementedVisitor("visit(VariableDeclaratorList, Object)"); }

        public Object visit(VariableDeclarator n) { return unimplementedVisitor("visit(VariableDeclarator)"); }
        public Object visit(VariableDeclarator n, Object o) { return  unimplementedVisitor("visit(VariableDeclarator, Object)"); }

        public Object visit(VariableDeclaratorId n) { return unimplementedVisitor("visit(VariableDeclaratorId)"); }
        public Object visit(VariableDeclaratorId n, Object o) { return  unimplementedVisitor("visit(VariableDeclaratorId, Object)"); }

        public Object visit(MethodDeclaration n) { return unimplementedVisitor("visit(MethodDeclaration)"); }
        public Object visit(MethodDeclaration n, Object o) { return  unimplementedVisitor("visit(MethodDeclaration, Object)"); }

        public Object visit(TypedMethodHeader n) { return unimplementedVisitor("visit(TypedMethodHeader)"); }
        public Object visit(TypedMethodHeader n, Object o) { return  unimplementedVisitor("visit(TypedMethodHeader, Object)"); }

        public Object visit(VoidMethodHeader n) { return unimplementedVisitor("visit(VoidMethodHeader)"); }
        public Object visit(VoidMethodHeader n, Object o) { return  unimplementedVisitor("visit(VoidMethodHeader, Object)"); }

        public Object visit(MethodDeclarator n) { return unimplementedVisitor("visit(MethodDeclarator)"); }
        public Object visit(MethodDeclarator n, Object o) { return  unimplementedVisitor("visit(MethodDeclarator, Object)"); }

        public Object visit(FormalParameterList n) { return unimplementedVisitor("visit(FormalParameterList)"); }
        public Object visit(FormalParameterList n, Object o) { return  unimplementedVisitor("visit(FormalParameterList, Object)"); }

        public Object visit(FormalParameter n) { return unimplementedVisitor("visit(FormalParameter)"); }
        public Object visit(FormalParameter n, Object o) { return  unimplementedVisitor("visit(FormalParameter, Object)"); }

        public Object visit(ClassTypeList n) { return unimplementedVisitor("visit(ClassTypeList)"); }
        public Object visit(ClassTypeList n, Object o) { return  unimplementedVisitor("visit(ClassTypeList, Object)"); }

        public Object visit(EmptyMethodBody n) { return unimplementedVisitor("visit(EmptyMethodBody)"); }
        public Object visit(EmptyMethodBody n, Object o) { return  unimplementedVisitor("visit(EmptyMethodBody, Object)"); }

        public Object visit(StaticInitializer n) { return unimplementedVisitor("visit(StaticInitializer)"); }
        public Object visit(StaticInitializer n, Object o) { return  unimplementedVisitor("visit(StaticInitializer, Object)"); }

        public Object visit(ConstructorDeclaration n) { return unimplementedVisitor("visit(ConstructorDeclaration)"); }
        public Object visit(ConstructorDeclaration n, Object o) { return  unimplementedVisitor("visit(ConstructorDeclaration, Object)"); }

        public Object visit(ConstructorDeclarator n) { return unimplementedVisitor("visit(ConstructorDeclarator)"); }
        public Object visit(ConstructorDeclarator n, Object o) { return  unimplementedVisitor("visit(ConstructorDeclarator, Object)"); }

        public Object visit(ConstructorBody n) { return unimplementedVisitor("visit(ConstructorBody)"); }
        public Object visit(ConstructorBody n, Object o) { return  unimplementedVisitor("visit(ConstructorBody, Object)"); }

        public Object visit(ThisCall n) { return unimplementedVisitor("visit(ThisCall)"); }
        public Object visit(ThisCall n, Object o) { return  unimplementedVisitor("visit(ThisCall, Object)"); }

        public Object visit(SuperCall n) { return unimplementedVisitor("visit(SuperCall)"); }
        public Object visit(SuperCall n, Object o) { return  unimplementedVisitor("visit(SuperCall, Object)"); }

        public Object visit(InterfaceDeclaration n) { return unimplementedVisitor("visit(InterfaceDeclaration)"); }
        public Object visit(InterfaceDeclaration n, Object o) { return  unimplementedVisitor("visit(InterfaceDeclaration, Object)"); }

        public Object visit(InterfaceBody n) { return unimplementedVisitor("visit(InterfaceBody)"); }
        public Object visit(InterfaceBody n, Object o) { return  unimplementedVisitor("visit(InterfaceBody, Object)"); }

        public Object visit(InterfaceMemberDeclarationList n) { return unimplementedVisitor("visit(InterfaceMemberDeclarationList)"); }
        public Object visit(InterfaceMemberDeclarationList n, Object o) { return  unimplementedVisitor("visit(InterfaceMemberDeclarationList, Object)"); }

        public Object visit(AbstractMethodDeclaration n) { return unimplementedVisitor("visit(AbstractMethodDeclaration)"); }
        public Object visit(AbstractMethodDeclaration n, Object o) { return  unimplementedVisitor("visit(AbstractMethodDeclaration, Object)"); }

        public Object visit(ArrayInitializer n) { return unimplementedVisitor("visit(ArrayInitializer)"); }
        public Object visit(ArrayInitializer n, Object o) { return  unimplementedVisitor("visit(ArrayInitializer, Object)"); }

        public Object visit(VariableInitializerList n) { return unimplementedVisitor("visit(VariableInitializerList)"); }
        public Object visit(VariableInitializerList n, Object o) { return  unimplementedVisitor("visit(VariableInitializerList, Object)"); }

        public Object visit(Block n) { return unimplementedVisitor("visit(Block)"); }
        public Object visit(Block n, Object o) { return  unimplementedVisitor("visit(Block, Object)"); }

        public Object visit(BlockStatementList n) { return unimplementedVisitor("visit(BlockStatementList)"); }
        public Object visit(BlockStatementList n, Object o) { return  unimplementedVisitor("visit(BlockStatementList, Object)"); }

        public Object visit(LocalVariableDeclarationStatement n) { return unimplementedVisitor("visit(LocalVariableDeclarationStatement)"); }
        public Object visit(LocalVariableDeclarationStatement n, Object o) { return  unimplementedVisitor("visit(LocalVariableDeclarationStatement, Object)"); }

        public Object visit(LocalVariableDeclaration n) { return unimplementedVisitor("visit(LocalVariableDeclaration)"); }
        public Object visit(LocalVariableDeclaration n, Object o) { return  unimplementedVisitor("visit(LocalVariableDeclaration, Object)"); }

        public Object visit(EmptyStatement n) { return unimplementedVisitor("visit(EmptyStatement)"); }
        public Object visit(EmptyStatement n, Object o) { return  unimplementedVisitor("visit(EmptyStatement, Object)"); }

        public Object visit(LabeledStatement n) { return unimplementedVisitor("visit(LabeledStatement)"); }
        public Object visit(LabeledStatement n, Object o) { return  unimplementedVisitor("visit(LabeledStatement, Object)"); }

        public Object visit(ExpressionStatement n) { return unimplementedVisitor("visit(ExpressionStatement)"); }
        public Object visit(ExpressionStatement n, Object o) { return  unimplementedVisitor("visit(ExpressionStatement, Object)"); }

        public Object visit(IfStatement n) { return unimplementedVisitor("visit(IfStatement)"); }
        public Object visit(IfStatement n, Object o) { return  unimplementedVisitor("visit(IfStatement, Object)"); }

        public Object visit(SwitchStatement n) { return unimplementedVisitor("visit(SwitchStatement)"); }
        public Object visit(SwitchStatement n, Object o) { return  unimplementedVisitor("visit(SwitchStatement, Object)"); }

        public Object visit(SwitchBlock n) { return unimplementedVisitor("visit(SwitchBlock)"); }
        public Object visit(SwitchBlock n, Object o) { return  unimplementedVisitor("visit(SwitchBlock, Object)"); }

        public Object visit(SwitchBlockStatementList n) { return unimplementedVisitor("visit(SwitchBlockStatementList)"); }
        public Object visit(SwitchBlockStatementList n, Object o) { return  unimplementedVisitor("visit(SwitchBlockStatementList, Object)"); }

        public Object visit(SwitchBlockStatement n) { return unimplementedVisitor("visit(SwitchBlockStatement)"); }
        public Object visit(SwitchBlockStatement n, Object o) { return  unimplementedVisitor("visit(SwitchBlockStatement, Object)"); }

        public Object visit(SwitchLabelList n) { return unimplementedVisitor("visit(SwitchLabelList)"); }
        public Object visit(SwitchLabelList n, Object o) { return  unimplementedVisitor("visit(SwitchLabelList, Object)"); }

        public Object visit(CaseLabel n) { return unimplementedVisitor("visit(CaseLabel)"); }
        public Object visit(CaseLabel n, Object o) { return  unimplementedVisitor("visit(CaseLabel, Object)"); }

        public Object visit(DefaultLabel n) { return unimplementedVisitor("visit(DefaultLabel)"); }
        public Object visit(DefaultLabel n, Object o) { return  unimplementedVisitor("visit(DefaultLabel, Object)"); }

        public Object visit(WhileStatement n) { return unimplementedVisitor("visit(WhileStatement)"); }
        public Object visit(WhileStatement n, Object o) { return  unimplementedVisitor("visit(WhileStatement, Object)"); }

        public Object visit(DoStatement n) { return unimplementedVisitor("visit(DoStatement)"); }
        public Object visit(DoStatement n, Object o) { return  unimplementedVisitor("visit(DoStatement, Object)"); }

        public Object visit(ForStatement n) { return unimplementedVisitor("visit(ForStatement)"); }
        public Object visit(ForStatement n, Object o) { return  unimplementedVisitor("visit(ForStatement, Object)"); }

        public Object visit(StatementExpressionList n) { return unimplementedVisitor("visit(StatementExpressionList)"); }
        public Object visit(StatementExpressionList n, Object o) { return  unimplementedVisitor("visit(StatementExpressionList, Object)"); }

        public Object visit(BreakStatement n) { return unimplementedVisitor("visit(BreakStatement)"); }
        public Object visit(BreakStatement n, Object o) { return  unimplementedVisitor("visit(BreakStatement, Object)"); }

        public Object visit(ContinueStatement n) { return unimplementedVisitor("visit(ContinueStatement)"); }
        public Object visit(ContinueStatement n, Object o) { return  unimplementedVisitor("visit(ContinueStatement, Object)"); }

        public Object visit(ReturnStatement n) { return unimplementedVisitor("visit(ReturnStatement)"); }
        public Object visit(ReturnStatement n, Object o) { return  unimplementedVisitor("visit(ReturnStatement, Object)"); }

        public Object visit(ThrowStatement n) { return unimplementedVisitor("visit(ThrowStatement)"); }
        public Object visit(ThrowStatement n, Object o) { return  unimplementedVisitor("visit(ThrowStatement, Object)"); }

        public Object visit(SynchronizedStatement n) { return unimplementedVisitor("visit(SynchronizedStatement)"); }
        public Object visit(SynchronizedStatement n, Object o) { return  unimplementedVisitor("visit(SynchronizedStatement, Object)"); }

        public Object visit(TryStatement n) { return unimplementedVisitor("visit(TryStatement)"); }
        public Object visit(TryStatement n, Object o) { return  unimplementedVisitor("visit(TryStatement, Object)"); }

        public Object visit(CatchClauseList n) { return unimplementedVisitor("visit(CatchClauseList)"); }
        public Object visit(CatchClauseList n, Object o) { return  unimplementedVisitor("visit(CatchClauseList, Object)"); }

        public Object visit(CatchClause n) { return unimplementedVisitor("visit(CatchClause)"); }
        public Object visit(CatchClause n, Object o) { return  unimplementedVisitor("visit(CatchClause, Object)"); }

        public Object visit(Finally n) { return unimplementedVisitor("visit(Finally)"); }
        public Object visit(Finally n, Object o) { return  unimplementedVisitor("visit(Finally, Object)"); }

        public Object visit(ParenthesizedExpression n) { return unimplementedVisitor("visit(ParenthesizedExpression)"); }
        public Object visit(ParenthesizedExpression n, Object o) { return  unimplementedVisitor("visit(ParenthesizedExpression, Object)"); }

        public Object visit(PrimaryThis n) { return unimplementedVisitor("visit(PrimaryThis)"); }
        public Object visit(PrimaryThis n, Object o) { return  unimplementedVisitor("visit(PrimaryThis, Object)"); }

        public Object visit(PrimaryClassLiteral n) { return unimplementedVisitor("visit(PrimaryClassLiteral)"); }
        public Object visit(PrimaryClassLiteral n, Object o) { return  unimplementedVisitor("visit(PrimaryClassLiteral, Object)"); }

        public Object visit(PrimaryVoidClassLiteral n) { return unimplementedVisitor("visit(PrimaryVoidClassLiteral)"); }
        public Object visit(PrimaryVoidClassLiteral n, Object o) { return  unimplementedVisitor("visit(PrimaryVoidClassLiteral, Object)"); }

        public Object visit(ClassInstanceCreationExpression n) { return unimplementedVisitor("visit(ClassInstanceCreationExpression)"); }
        public Object visit(ClassInstanceCreationExpression n, Object o) { return  unimplementedVisitor("visit(ClassInstanceCreationExpression, Object)"); }

        public Object visit(ExpressionList n) { return unimplementedVisitor("visit(ExpressionList)"); }
        public Object visit(ExpressionList n, Object o) { return  unimplementedVisitor("visit(ExpressionList, Object)"); }

        public Object visit(ArrayCreationExpression n) { return unimplementedVisitor("visit(ArrayCreationExpression)"); }
        public Object visit(ArrayCreationExpression n, Object o) { return  unimplementedVisitor("visit(ArrayCreationExpression, Object)"); }

        public Object visit(DimExprList n) { return unimplementedVisitor("visit(DimExprList)"); }
        public Object visit(DimExprList n, Object o) { return  unimplementedVisitor("visit(DimExprList, Object)"); }

        public Object visit(DimExpr n) { return unimplementedVisitor("visit(DimExpr)"); }
        public Object visit(DimExpr n, Object o) { return  unimplementedVisitor("visit(DimExpr, Object)"); }

        public Object visit(DimList n) { return unimplementedVisitor("visit(DimList)"); }
        public Object visit(DimList n, Object o) { return  unimplementedVisitor("visit(DimList, Object)"); }

        public Object visit(Dim n) { return unimplementedVisitor("visit(Dim)"); }
        public Object visit(Dim n, Object o) { return  unimplementedVisitor("visit(Dim, Object)"); }

        public Object visit(FieldAccess n) { return unimplementedVisitor("visit(FieldAccess)"); }
        public Object visit(FieldAccess n, Object o) { return  unimplementedVisitor("visit(FieldAccess, Object)"); }

        public Object visit(SuperFieldAccess n) { return unimplementedVisitor("visit(SuperFieldAccess)"); }
        public Object visit(SuperFieldAccess n, Object o) { return  unimplementedVisitor("visit(SuperFieldAccess, Object)"); }

        public Object visit(MethodInvocation n) { return unimplementedVisitor("visit(MethodInvocation)"); }
        public Object visit(MethodInvocation n, Object o) { return  unimplementedVisitor("visit(MethodInvocation, Object)"); }

        public Object visit(PrimaryMethodInvocation n) { return unimplementedVisitor("visit(PrimaryMethodInvocation)"); }
        public Object visit(PrimaryMethodInvocation n, Object o) { return  unimplementedVisitor("visit(PrimaryMethodInvocation, Object)"); }

        public Object visit(SuperMethodInvocation n) { return unimplementedVisitor("visit(SuperMethodInvocation)"); }
        public Object visit(SuperMethodInvocation n, Object o) { return  unimplementedVisitor("visit(SuperMethodInvocation, Object)"); }

        public Object visit(ArrayAccess n) { return unimplementedVisitor("visit(ArrayAccess)"); }
        public Object visit(ArrayAccess n, Object o) { return  unimplementedVisitor("visit(ArrayAccess, Object)"); }

        public Object visit(PostIncrementExpression n) { return unimplementedVisitor("visit(PostIncrementExpression)"); }
        public Object visit(PostIncrementExpression n, Object o) { return  unimplementedVisitor("visit(PostIncrementExpression, Object)"); }

        public Object visit(PostDecrementExpression n) { return unimplementedVisitor("visit(PostDecrementExpression)"); }
        public Object visit(PostDecrementExpression n, Object o) { return  unimplementedVisitor("visit(PostDecrementExpression, Object)"); }

        public Object visit(PlusUnaryExpression n) { return unimplementedVisitor("visit(PlusUnaryExpression)"); }
        public Object visit(PlusUnaryExpression n, Object o) { return  unimplementedVisitor("visit(PlusUnaryExpression, Object)"); }

        public Object visit(MinusUnaryExpression n) { return unimplementedVisitor("visit(MinusUnaryExpression)"); }
        public Object visit(MinusUnaryExpression n, Object o) { return  unimplementedVisitor("visit(MinusUnaryExpression, Object)"); }

        public Object visit(PreIncrementExpression n) { return unimplementedVisitor("visit(PreIncrementExpression)"); }
        public Object visit(PreIncrementExpression n, Object o) { return  unimplementedVisitor("visit(PreIncrementExpression, Object)"); }

        public Object visit(PreDecrementExpression n) { return unimplementedVisitor("visit(PreDecrementExpression)"); }
        public Object visit(PreDecrementExpression n, Object o) { return  unimplementedVisitor("visit(PreDecrementExpression, Object)"); }

        public Object visit(UnaryComplementExpression n) { return unimplementedVisitor("visit(UnaryComplementExpression)"); }
        public Object visit(UnaryComplementExpression n, Object o) { return  unimplementedVisitor("visit(UnaryComplementExpression, Object)"); }

        public Object visit(UnaryNotExpression n) { return unimplementedVisitor("visit(UnaryNotExpression)"); }
        public Object visit(UnaryNotExpression n, Object o) { return  unimplementedVisitor("visit(UnaryNotExpression, Object)"); }

        public Object visit(PrimitiveCastExpression n) { return unimplementedVisitor("visit(PrimitiveCastExpression)"); }
        public Object visit(PrimitiveCastExpression n, Object o) { return  unimplementedVisitor("visit(PrimitiveCastExpression, Object)"); }

        public Object visit(ClassCastExpression n) { return unimplementedVisitor("visit(ClassCastExpression)"); }
        public Object visit(ClassCastExpression n, Object o) { return  unimplementedVisitor("visit(ClassCastExpression, Object)"); }

        public Object visit(MultiplyExpression n) { return unimplementedVisitor("visit(MultiplyExpression)"); }
        public Object visit(MultiplyExpression n, Object o) { return  unimplementedVisitor("visit(MultiplyExpression, Object)"); }

        public Object visit(DivideExpression n) { return unimplementedVisitor("visit(DivideExpression)"); }
        public Object visit(DivideExpression n, Object o) { return  unimplementedVisitor("visit(DivideExpression, Object)"); }

        public Object visit(ModExpression n) { return unimplementedVisitor("visit(ModExpression)"); }
        public Object visit(ModExpression n, Object o) { return  unimplementedVisitor("visit(ModExpression, Object)"); }

        public Object visit(AddExpression n) { return unimplementedVisitor("visit(AddExpression)"); }
        public Object visit(AddExpression n, Object o) { return  unimplementedVisitor("visit(AddExpression, Object)"); }

        public Object visit(SubtractExpression n) { return unimplementedVisitor("visit(SubtractExpression)"); }
        public Object visit(SubtractExpression n, Object o) { return  unimplementedVisitor("visit(SubtractExpression, Object)"); }

        public Object visit(LeftShiftExpression n) { return unimplementedVisitor("visit(LeftShiftExpression)"); }
        public Object visit(LeftShiftExpression n, Object o) { return  unimplementedVisitor("visit(LeftShiftExpression, Object)"); }

        public Object visit(RightShiftExpression n) { return unimplementedVisitor("visit(RightShiftExpression)"); }
        public Object visit(RightShiftExpression n, Object o) { return  unimplementedVisitor("visit(RightShiftExpression, Object)"); }

        public Object visit(UnsignedRightShiftExpression n) { return unimplementedVisitor("visit(UnsignedRightShiftExpression)"); }
        public Object visit(UnsignedRightShiftExpression n, Object o) { return  unimplementedVisitor("visit(UnsignedRightShiftExpression, Object)"); }

        public Object visit(LessExpression n) { return unimplementedVisitor("visit(LessExpression)"); }
        public Object visit(LessExpression n, Object o) { return  unimplementedVisitor("visit(LessExpression, Object)"); }

        public Object visit(GreaterExpression n) { return unimplementedVisitor("visit(GreaterExpression)"); }
        public Object visit(GreaterExpression n, Object o) { return  unimplementedVisitor("visit(GreaterExpression, Object)"); }

        public Object visit(LessEqualExpression n) { return unimplementedVisitor("visit(LessEqualExpression)"); }
        public Object visit(LessEqualExpression n, Object o) { return  unimplementedVisitor("visit(LessEqualExpression, Object)"); }

        public Object visit(GreaterEqualExpression n) { return unimplementedVisitor("visit(GreaterEqualExpression)"); }
        public Object visit(GreaterEqualExpression n, Object o) { return  unimplementedVisitor("visit(GreaterEqualExpression, Object)"); }

        public Object visit(InstanceofExpression n) { return unimplementedVisitor("visit(InstanceofExpression)"); }
        public Object visit(InstanceofExpression n, Object o) { return  unimplementedVisitor("visit(InstanceofExpression, Object)"); }

        public Object visit(EqualExpression n) { return unimplementedVisitor("visit(EqualExpression)"); }
        public Object visit(EqualExpression n, Object o) { return  unimplementedVisitor("visit(EqualExpression, Object)"); }

        public Object visit(NotEqualExpression n) { return unimplementedVisitor("visit(NotEqualExpression)"); }
        public Object visit(NotEqualExpression n, Object o) { return  unimplementedVisitor("visit(NotEqualExpression, Object)"); }

        public Object visit(AndExpression n) { return unimplementedVisitor("visit(AndExpression)"); }
        public Object visit(AndExpression n, Object o) { return  unimplementedVisitor("visit(AndExpression, Object)"); }

        public Object visit(ExclusiveOrExpression n) { return unimplementedVisitor("visit(ExclusiveOrExpression)"); }
        public Object visit(ExclusiveOrExpression n, Object o) { return  unimplementedVisitor("visit(ExclusiveOrExpression, Object)"); }

        public Object visit(InclusiveOrExpression n) { return unimplementedVisitor("visit(InclusiveOrExpression)"); }
        public Object visit(InclusiveOrExpression n, Object o) { return  unimplementedVisitor("visit(InclusiveOrExpression, Object)"); }

        public Object visit(ConditionalAndExpression n) { return unimplementedVisitor("visit(ConditionalAndExpression)"); }
        public Object visit(ConditionalAndExpression n, Object o) { return  unimplementedVisitor("visit(ConditionalAndExpression, Object)"); }

        public Object visit(ConditionalOrExpression n) { return unimplementedVisitor("visit(ConditionalOrExpression)"); }
        public Object visit(ConditionalOrExpression n, Object o) { return  unimplementedVisitor("visit(ConditionalOrExpression, Object)"); }

        public Object visit(ConditionalExpression n) { return unimplementedVisitor("visit(ConditionalExpression)"); }
        public Object visit(ConditionalExpression n, Object o) { return  unimplementedVisitor("visit(ConditionalExpression, Object)"); }

        public Object visit(Assignment n) { return unimplementedVisitor("visit(Assignment)"); }
        public Object visit(Assignment n, Object o) { return  unimplementedVisitor("visit(Assignment, Object)"); }

        public Object visit(EqualOperator n) { return unimplementedVisitor("visit(EqualOperator)"); }
        public Object visit(EqualOperator n, Object o) { return  unimplementedVisitor("visit(EqualOperator, Object)"); }

        public Object visit(MultiplyEqualOperator n) { return unimplementedVisitor("visit(MultiplyEqualOperator)"); }
        public Object visit(MultiplyEqualOperator n, Object o) { return  unimplementedVisitor("visit(MultiplyEqualOperator, Object)"); }

        public Object visit(DivideEqualOperator n) { return unimplementedVisitor("visit(DivideEqualOperator)"); }
        public Object visit(DivideEqualOperator n, Object o) { return  unimplementedVisitor("visit(DivideEqualOperator, Object)"); }

        public Object visit(ModEqualOperator n) { return unimplementedVisitor("visit(ModEqualOperator)"); }
        public Object visit(ModEqualOperator n, Object o) { return  unimplementedVisitor("visit(ModEqualOperator, Object)"); }

        public Object visit(PlusEqualOperator n) { return unimplementedVisitor("visit(PlusEqualOperator)"); }
        public Object visit(PlusEqualOperator n, Object o) { return  unimplementedVisitor("visit(PlusEqualOperator, Object)"); }

        public Object visit(MinusEqualOperator n) { return unimplementedVisitor("visit(MinusEqualOperator)"); }
        public Object visit(MinusEqualOperator n, Object o) { return  unimplementedVisitor("visit(MinusEqualOperator, Object)"); }

        public Object visit(LeftShiftEqualOperator n) { return unimplementedVisitor("visit(LeftShiftEqualOperator)"); }
        public Object visit(LeftShiftEqualOperator n, Object o) { return  unimplementedVisitor("visit(LeftShiftEqualOperator, Object)"); }

        public Object visit(RightShiftEqualOperator n) { return unimplementedVisitor("visit(RightShiftEqualOperator)"); }
        public Object visit(RightShiftEqualOperator n, Object o) { return  unimplementedVisitor("visit(RightShiftEqualOperator, Object)"); }

        public Object visit(UnsignedRightShiftEqualOperator n) { return unimplementedVisitor("visit(UnsignedRightShiftEqualOperator)"); }
        public Object visit(UnsignedRightShiftEqualOperator n, Object o) { return  unimplementedVisitor("visit(UnsignedRightShiftEqualOperator, Object)"); }

        public Object visit(AndEqualOperator n) { return unimplementedVisitor("visit(AndEqualOperator)"); }
        public Object visit(AndEqualOperator n, Object o) { return  unimplementedVisitor("visit(AndEqualOperator, Object)"); }

        public Object visit(ExclusiveOrEqualOperator n) { return unimplementedVisitor("visit(ExclusiveOrEqualOperator)"); }
        public Object visit(ExclusiveOrEqualOperator n, Object o) { return  unimplementedVisitor("visit(ExclusiveOrEqualOperator, Object)"); }

        public Object visit(OrEqualOperator n) { return unimplementedVisitor("visit(OrEqualOperator)"); }
        public Object visit(OrEqualOperator n, Object o) { return  unimplementedVisitor("visit(OrEqualOperator, Object)"); }

        public Object visit(Commaopt n) { return unimplementedVisitor("visit(Commaopt)"); }
        public Object visit(Commaopt n, Object o) { return  unimplementedVisitor("visit(Commaopt, Object)"); }

        public Object visit(IDENTIFIERopt n) { return unimplementedVisitor("visit(IDENTIFIERopt)"); }
        public Object visit(IDENTIFIERopt n, Object o) { return  unimplementedVisitor("visit(IDENTIFIERopt, Object)"); }
    }

    public void ruleAction(int ruleNumber)
    {
        switch (ruleNumber)
        {
 
            //
            // Rule 1:  Literal ::= IntegerLiteral
            //
            case 1: {
                setResult(
                    new IntegerLiteral(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 2:  Literal ::= LongLiteral
            //
            case 2: {
                setResult(
                    new LongLiteral(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 3:  Literal ::= FloatingPointLiteral
            //
            case 3: {
                setResult(
                    new FloatLiteral(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 4:  Literal ::= DoubleLiteral
            //
            case 4: {
                setResult(
                    new DoubleLiteral(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 5:  Literal ::= BooleanLiteral
            //
            case 5: {
                setResult(
                    new BooleanLiteral(getLeftIToken(), getRightIToken(),
                                       (IBooleanLiteral)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 6:  Literal ::= CharacterLiteral
            //
            case 6: {
                setResult(
                    new CharacterLiteral(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 7:  Literal ::= StringLiteral
            //
            case 7: {
                setResult(
                    new StringLiteral(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 8:  Literal ::= null$null_literal
            //
            case 8: {
                setResult(
                    new NullLiteral(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 9:  BooleanLiteral ::= true
            //
            case 9: {
                setResult(
                    new TrueLiteral(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 10:  BooleanLiteral ::= false
            //
            case 10: {
                setResult(
                    new FalseLiteral(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 11:  Type ::= PrimitiveType
            //
            case 11:
                break; 
            //
            // Rule 12:  Type ::= ReferenceType
            //
            case 12:
                break; 
            //
            // Rule 13:  PrimitiveType ::= NumericType
            //
            case 13:
                break; 
            //
            // Rule 14:  PrimitiveType ::= boolean
            //
            case 14: {
                setResult(
                    new BooleanType(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 15:  NumericType ::= IntegralType
            //
            case 15:
                break; 
            //
            // Rule 16:  NumericType ::= FloatingPointType
            //
            case 16:
                break; 
            //
            // Rule 17:  IntegralType ::= byte
            //
            case 17: {
                setResult(
                    new ByteType(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 18:  IntegralType ::= short
            //
            case 18: {
                setResult(
                    new ShortType(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 19:  IntegralType ::= int
            //
            case 19: {
                setResult(
                    new IntType(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 20:  IntegralType ::= long
            //
            case 20: {
                setResult(
                    new LongType(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 21:  IntegralType ::= char
            //
            case 21: {
                setResult(
                    new CharType(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 22:  FloatingPointType ::= float
            //
            case 22: {
                setResult(
                    new FloatType(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 23:  FloatingPointType ::= double
            //
            case 23: {
                setResult(
                    new DoubleType(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 24:  ReferenceType ::= ClassOrInterfaceType
            //
            case 24:
                break; 
            //
            // Rule 25:  ReferenceType ::= ArrayType
            //
            case 25:
                break; 
            //
            // Rule 26:  ClassOrInterfaceType ::= Name
            //
            case 26:
                break; 
            //
            // Rule 27:  ArrayType ::= PrimitiveType Dims
            //
            case 27: {
                setResult(
                    new PrimitiveArrayType(getLeftIToken(), getRightIToken(),
                                           (IPrimitiveType)getRhsSym(1),
                                           (DimList)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 28:  ArrayType ::= Name Dims
            //
            case 28: {
                setResult(
                    new ClassOrInterfaceArrayType(getLeftIToken(), getRightIToken(),
                                                  (IName)getRhsSym(1),
                                                  (DimList)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 29:  ClassType ::= ClassOrInterfaceType
            //
            case 29:
                break; 
            //
            // Rule 30:  InterfaceType ::= ClassOrInterfaceType
            //
            case 30:
                break; 
            //
            // Rule 31:  Name ::= SimpleName
            //
            case 31:
                break; 
            //
            // Rule 32:  Name ::= QualifiedName
            //
            case 32:
                break; 
            //
            // Rule 33:  SimpleName ::= IDENTIFIER
            //
            case 33: {
                setResult(
                    new SimpleName(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 34:  QualifiedName ::= Name . IDENTIFIER
            //
            case 34: {
                setResult(
                    new QualifiedName(getLeftIToken(), getRightIToken(),
                                      (IName)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 35:  CompilationUnit ::= PackageDeclarationopt ImportDeclarationsopt TypeDeclarationsopt
            //
            case 35: {
                setResult(
                    new CompilationUnit(getLeftIToken(), getRightIToken(),
                                        (PackageDeclaration)getRhsSym(1),
                                        (ImportDeclarationList)getRhsSym(2),
                                        (TypeDeclarationList)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 36:  ImportDeclarations ::= ImportDeclaration
            //
            case 36: {
                setResult(
                    new ImportDeclarationList((IImportDeclaration)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 37:  ImportDeclarations ::= ImportDeclarations ImportDeclaration
            //
            case 37: {
                ((ImportDeclarationList)getRhsSym(1)).add((IImportDeclaration)getRhsSym(2));
                break;
            } 
            //
            // Rule 38:  TypeDeclarations ::= TypeDeclaration
            //
            case 38: {
                setResult(
                    new TypeDeclarationList((ITypeDeclaration)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 39:  TypeDeclarations ::= TypeDeclarations TypeDeclaration
            //
            case 39: {
                ((TypeDeclarationList)getRhsSym(1)).add((ITypeDeclaration)getRhsSym(2));
                break;
            } 
            //
            // Rule 40:  PackageDeclaration ::= package Name ;
            //
            case 40: {
                setResult(
                    new PackageDeclaration(getLeftIToken(), getRightIToken(),
                                           (IName)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 41:  ImportDeclaration ::= SingleTypeImportDeclaration
            //
            case 41:
                break; 
            //
            // Rule 42:  ImportDeclaration ::= TypeImportOnDemandDeclaration
            //
            case 42:
                break; 
            //
            // Rule 43:  SingleTypeImportDeclaration ::= import Name ;
            //
            case 43: {
                setResult(
                    new SingleTypeImportDeclaration(getLeftIToken(), getRightIToken(),
                                                    (IName)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 44:  TypeImportOnDemandDeclaration ::= import Name . * ;
            //
            case 44: {
                setResult(
                    new TypeImportOnDemandDeclaration(getLeftIToken(), getRightIToken(),
                                                      (IName)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 45:  TypeDeclaration ::= ClassDeclaration
            //
            case 45:
                break; 
            //
            // Rule 46:  TypeDeclaration ::= InterfaceDeclaration
            //
            case 46:
                break; 
            //
            // Rule 47:  TypeDeclaration ::= ;
            //
            case 47: {
                setResult(
                    new EmptyDeclaration(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 48:  Modifiers ::= Modifier
            //
            case 48: {
                setResult(
                    new ModifierList((IModifier)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 49:  Modifiers ::= Modifiers Modifier
            //
            case 49: {
                ((ModifierList)getRhsSym(1)).add((IModifier)getRhsSym(2));
                break;
            } 
            //
            // Rule 50:  Modifier ::= public
            //
            case 50: {
                setResult(
                    new PublicModifier(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 51:  Modifier ::= protected
            //
            case 51: {
                setResult(
                    new ProtectedModifier(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 52:  Modifier ::= private
            //
            case 52: {
                setResult(
                    new PrivateModifier(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 53:  Modifier ::= static
            //
            case 53: {
                setResult(
                    new StaticModifier(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 54:  Modifier ::= abstract
            //
            case 54: {
                setResult(
                    new AbstractModifier(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 55:  Modifier ::= final
            //
            case 55: {
                setResult(
                    new FinalModifier(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 56:  Modifier ::= native
            //
            case 56: {
                setResult(
                    new NativeModifier(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 57:  Modifier ::= strictfp
            //
            case 57: {
                setResult(
                    new StrictfpModifier(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 58:  Modifier ::= synchronized
            //
            case 58: {
                setResult(
                    new SynchronizedModifier(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 59:  Modifier ::= transient
            //
            case 59: {
                setResult(
                    new TransientModifier(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 60:  Modifier ::= volatile
            //
            case 60: {
                setResult(
                    new VolatileModifier(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 61:  ClassDeclaration ::= Modifiersopt class IDENTIFIER$Name Superopt Interfacesopt ClassBody
            //
            case 61: {
                setResult(
                    new ClassDeclaration(JavaParser.this, getLeftIToken(), getRightIToken(),
                                         (ModifierList)getRhsSym(1),
                                         new AstToken(getRhsIToken(3)),
                                         (Super)getRhsSym(4),
                                         (InterfaceTypeList)getRhsSym(5),
                                         (ClassBody)getRhsSym(6))
                );
                break;
            } 
            //
            // Rule 62:  Super ::= extends ClassType
            //
            case 62: {
                setResult(
                    new Super(getLeftIToken(), getRightIToken(),
                              (IClassType)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 63:  Interfaces ::= implements InterfaceTypeList
            //
            case 63: {
                setResult((InterfaceTypeList)getRhsSym(2));
                break;
            } 
            //
            // Rule 64:  InterfaceTypeList ::= InterfaceType
            //
            case 64: {
                setResult(
                    new InterfaceTypeList((IInterfaceType)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 65:  InterfaceTypeList ::= InterfaceTypeList , InterfaceType
            //
            case 65: {
                ((InterfaceTypeList)getRhsSym(1)).add((IInterfaceType)getRhsSym(3));
                break;
            } 
            //
            // Rule 66:  ClassBody ::= { ClassBodyDeclarationsopt }
            //
            case 66: {
                setResult(
                    new ClassBody(getLeftIToken(), getRightIToken(),
                                  (ClassBodyDeclarationList)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 67:  ClassBodyDeclarations ::= ClassBodyDeclaration
            //
            case 67: {
                setResult(
                    new ClassBodyDeclarationList((IClassBodyDeclaration)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 68:  ClassBodyDeclarations ::= ClassBodyDeclarations ClassBodyDeclaration
            //
            case 68: {
                ((ClassBodyDeclarationList)getRhsSym(1)).add((IClassBodyDeclaration)getRhsSym(2));
                break;
            } 
            //
            // Rule 69:  ClassBodyDeclaration ::= ClassMemberDeclaration
            //
            case 69:
                break; 
            //
            // Rule 70:  ClassBodyDeclaration ::= StaticInitializer
            //
            case 70:
                break; 
            //
            // Rule 71:  ClassBodyDeclaration ::= ConstructorDeclaration
            //
            case 71:
                break; 
            //
            // Rule 72:  ClassBodyDeclaration ::= Block
            //
            case 72:
                break; 
            //
            // Rule 73:  ClassMemberDeclaration ::= FieldDeclaration
            //
            case 73:
                break; 
            //
            // Rule 74:  ClassMemberDeclaration ::= MethodDeclaration
            //
            case 74:
                break; 
            //
            // Rule 75:  ClassMemberDeclaration ::= ClassDeclaration
            //
            case 75:
                break; 
            //
            // Rule 76:  ClassMemberDeclaration ::= InterfaceDeclaration
            //
            case 76:
                break; 
            //
            // Rule 77:  ClassMemberDeclaration ::= ;
            //
            case 77: {
                setResult(
                    new EmptyDeclaration(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 78:  FieldDeclaration ::= Modifiersopt Type VariableDeclarators ;
            //
            case 78: {
                setResult(
                    new FieldDeclaration(JavaParser.this, getLeftIToken(), getRightIToken(),
                                         (ModifierList)getRhsSym(1),
                                         (IType)getRhsSym(2),
                                         (VariableDeclaratorList)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 79:  VariableDeclarators ::= VariableDeclarator
            //
            case 79: {
                setResult(
                    new VariableDeclaratorList((IVariableDeclarator)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 80:  VariableDeclarators ::= VariableDeclarators , VariableDeclarator
            //
            case 80: {
                ((VariableDeclaratorList)getRhsSym(1)).add((IVariableDeclarator)getRhsSym(3));
                break;
            } 
            //
            // Rule 81:  VariableDeclarator ::= VariableDeclaratorId
            //
            case 81:
                break; 
            //
            // Rule 82:  VariableDeclarator ::= VariableDeclaratorId = VariableInitializer
            //
            case 82: {
                setResult(
                    new VariableDeclarator(getLeftIToken(), getRightIToken(),
                                           (VariableDeclaratorId)getRhsSym(1),
                                           (IVariableInitializer)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 83:  VariableDeclaratorId ::= IDENTIFIER Dimsopt
            //
            case 83: {
                setResult(
                    new VariableDeclaratorId(getLeftIToken(), getRightIToken(),
                                             (DimList)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 84:  VariableInitializer ::= Expression
            //
            case 84:
                break; 
            //
            // Rule 85:  VariableInitializer ::= ArrayInitializer
            //
            case 85:
                break; 
            //
            // Rule 86:  MethodDeclaration ::= MethodHeader MethodBody
            //
            case 86: {
                setResult(
                    new MethodDeclaration(JavaParser.this, getLeftIToken(), getRightIToken(),
                                          (IMethodHeader)getRhsSym(1),
                                          (IMethodBody)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 87:  MethodHeader ::= Modifiersopt Type MethodDeclarator Throwsopt
            //
            case 87: {
                setResult(
                    new TypedMethodHeader(getLeftIToken(), getRightIToken(),
                                          (ModifierList)getRhsSym(1),
                                          (IType)getRhsSym(2),
                                          (MethodDeclarator)getRhsSym(3),
                                          (ClassTypeList)getRhsSym(4))
                );
                break;
            } 
            //
            // Rule 88:  MethodHeader ::= Modifiersopt void MethodDeclarator Throwsopt
            //
            case 88: {
                setResult(
                    new VoidMethodHeader(getLeftIToken(), getRightIToken(),
                                         (ModifierList)getRhsSym(1),
                                         (MethodDeclarator)getRhsSym(3),
                                         (ClassTypeList)getRhsSym(4))
                );
                break;
            } 
            //
            // Rule 89:  MethodDeclarator ::= IDENTIFIER ( FormalParameterListopt ) Dimsopt
            //
            case 89: {
                setResult(
                    new MethodDeclarator(getLeftIToken(), getRightIToken(),
                                         (FormalParameterList)getRhsSym(3),
                                         (DimList)getRhsSym(5))
                );
                break;
            } 
            //
            // Rule 90:  FormalParameterList ::= FormalParameter
            //
            case 90: {
                setResult(
                    new FormalParameterList((FormalParameter)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 91:  FormalParameterList ::= FormalParameterList , FormalParameter
            //
            case 91: {
                ((FormalParameterList)getRhsSym(1)).add((FormalParameter)getRhsSym(3));
                break;
            } 
            //
            // Rule 92:  FormalParameter ::= Modifiersopt Type VariableDeclaratorId
            //
            case 92: {
                setResult(
                    new FormalParameter(getLeftIToken(), getRightIToken(),
                                        (ModifierList)getRhsSym(1),
                                        (IType)getRhsSym(2),
                                        (VariableDeclaratorId)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 93:  Throws ::= throws ClassTypeList
            //
            case 93: {
                setResult((ClassTypeList)getRhsSym(2));
                break;
            } 
            //
            // Rule 94:  ClassTypeList ::= ClassType
            //
            case 94: {
                setResult(
                    new ClassTypeList((IClassType)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 95:  ClassTypeList ::= ClassTypeList , ClassType
            //
            case 95: {
                ((ClassTypeList)getRhsSym(1)).add((IClassType)getRhsSym(3));
                break;
            } 
            //
            // Rule 96:  MethodBody ::= Block
            //
            case 96:
                break; 
            //
            // Rule 97:  MethodBody ::= ;
            //
            case 97: {
                setResult(
                    new EmptyMethodBody(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 98:  StaticInitializer ::= static Block
            //
            case 98: {
                setResult(
                    new StaticInitializer(getLeftIToken(), getRightIToken(),
                                          (Block)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 99:  ConstructorDeclaration ::= Modifiersopt ConstructorDeclarator Throwsopt ConstructorBody
            //
            case 99: {
                setResult(
                    new ConstructorDeclaration(JavaParser.this, getLeftIToken(), getRightIToken(),
                                               (ModifierList)getRhsSym(1),
                                               (ConstructorDeclarator)getRhsSym(2),
                                               (ClassTypeList)getRhsSym(3),
                                               (IConstructorBody)getRhsSym(4))
                );
                break;
            } 
            //
            // Rule 100:  ConstructorDeclarator ::= IDENTIFIER ( FormalParameterListopt )
            //
            case 100: {
                setResult(
                    new ConstructorDeclarator(getLeftIToken(), getRightIToken(),
                                              (FormalParameterList)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 101:  ConstructorBody ::= Block
            //
            case 101:
                break; 
            //
            // Rule 102:  ConstructorBody ::= { ExplicitConstructorInvocation BlockStatementsopt }
            //
            case 102: {
                setResult(
                    new ConstructorBody(getLeftIToken(), getRightIToken(),
                                        (IExplicitConstructorInvocation)getRhsSym(2),
                                        (BlockStatementList)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 103:  ExplicitConstructorInvocation ::= this ( ArgumentListopt ) ;
            //
            case 103: {
                setResult(
                    new ThisCall(getLeftIToken(), getRightIToken(),
                                 (ExpressionList)getRhsSym(3),
                                 (IPrimary)null)
                );
                break;
            } 
            //
            // Rule 104:  ExplicitConstructorInvocation ::= Primary . this ( ArgumentListopt ) ;
            //
            case 104: {
                setResult(
                    new ThisCall(getLeftIToken(), getRightIToken(),
                                 (ExpressionList)getRhsSym(5),
                                 (IPrimary)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 105:  ExplicitConstructorInvocation ::= super ( ArgumentListopt ) ;
            //
            case 105: {
                setResult(
                    new SuperCall(getLeftIToken(), getRightIToken(),
                                  (ExpressionList)getRhsSym(3),
                                  (IPostfixExpression)null)
                );
                break;
            } 
            //
            // Rule 106:  ExplicitConstructorInvocation ::= Primary$expression . super ( ArgumentListopt ) ;
            //
            case 106: {
                setResult(
                    new SuperCall(getLeftIToken(), getRightIToken(),
                                  (ExpressionList)getRhsSym(5),
                                  (IPostfixExpression)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 107:  ExplicitConstructorInvocation ::= Name$expression . super ( ArgumentListopt ) ;
            //
            case 107: {
                setResult(
                    new SuperCall(getLeftIToken(), getRightIToken(),
                                  (ExpressionList)getRhsSym(5),
                                  (IPostfixExpression)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 108:  InterfaceDeclaration ::= Modifiersopt interface IDENTIFIER$Name ExtendsInterfacesopt InterfaceBody
            //
            case 108: {
                setResult(
                    new InterfaceDeclaration(JavaParser.this, getLeftIToken(), getRightIToken(),
                                             (ModifierList)getRhsSym(1),
                                             new AstToken(getRhsIToken(3)),
                                             (InterfaceTypeList)getRhsSym(4),
                                             (InterfaceBody)getRhsSym(5))
                );
                break;
            } 
            //
            // Rule 109:  ExtendsInterfaces ::= extends InterfaceTypeList
            //
            case 109: {
                setResult((InterfaceTypeList)getRhsSym(2));
                break;
            } 
            //
            // Rule 110:  InterfaceBody ::= { InterfaceMemberDeclarationsopt }
            //
            case 110: {
                setResult(
                    new InterfaceBody(getLeftIToken(), getRightIToken(),
                                      (InterfaceMemberDeclarationList)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 111:  InterfaceMemberDeclarations ::= InterfaceMemberDeclaration
            //
            case 111: {
                setResult(
                    new InterfaceMemberDeclarationList((IInterfaceMemberDeclaration)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 112:  InterfaceMemberDeclarations ::= InterfaceMemberDeclarations InterfaceMemberDeclaration
            //
            case 112: {
                ((InterfaceMemberDeclarationList)getRhsSym(1)).add((IInterfaceMemberDeclaration)getRhsSym(2));
                break;
            } 
            //
            // Rule 113:  InterfaceMemberDeclaration ::= ConstantDeclaration
            //
            case 113:
                break; 
            //
            // Rule 114:  InterfaceMemberDeclaration ::= AbstractMethodDeclaration
            //
            case 114:
                break; 
            //
            // Rule 115:  InterfaceMemberDeclaration ::= ClassDeclaration
            //
            case 115:
                break; 
            //
            // Rule 116:  InterfaceMemberDeclaration ::= InterfaceDeclaration
            //
            case 116:
                break; 
            //
            // Rule 117:  InterfaceMemberDeclaration ::= ;
            //
            case 117: {
                setResult(
                    new EmptyDeclaration(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 118:  ConstantDeclaration ::= FieldDeclaration
            //
            case 118:
                break; 
            //
            // Rule 119:  AbstractMethodDeclaration ::= MethodHeader ;
            //
            case 119: {
                setResult(
                    new AbstractMethodDeclaration(JavaParser.this, getLeftIToken(), getRightIToken(),
                                                  (IMethodHeader)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 120:  ArrayInitializer ::= { VariableInitializersopt Commaopt }
            //
            case 120: {
                setResult(
                    new ArrayInitializer(getLeftIToken(), getRightIToken(),
                                         (VariableInitializerList)getRhsSym(2),
                                         (Commaopt)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 121:  VariableInitializers ::= VariableInitializer
            //
            case 121: {
                setResult(
                    new VariableInitializerList((IVariableInitializer)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 122:  VariableInitializers ::= VariableInitializers , VariableInitializer
            //
            case 122: {
                ((VariableInitializerList)getRhsSym(1)).add((IVariableInitializer)getRhsSym(3));
                break;
            } 
            //
            // Rule 123:  Block ::= { BlockStatementsopt }
            //
            case 123: {
                setResult(
                    new Block(getLeftIToken(), getRightIToken(),
                              (BlockStatementList)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 124:  BlockStatements ::= BlockStatement
            //
            case 124: {
                setResult(
                    new BlockStatementList((IBlockStatement)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 125:  BlockStatements ::= BlockStatements BlockStatement
            //
            case 125: {
                ((BlockStatementList)getRhsSym(1)).add((IBlockStatement)getRhsSym(2));
                break;
            } 
            //
            // Rule 126:  BlockStatement ::= LocalVariableDeclarationStatement
            //
            case 126:
                break; 
            //
            // Rule 127:  BlockStatement ::= Statement
            //
            case 127:
                break; 
            //
            // Rule 128:  BlockStatement ::= ClassDeclaration
            //
            case 128:
                break; 
            //
            // Rule 129:  LocalVariableDeclarationStatement ::= LocalVariableDeclaration ;
            //
            case 129: {
                setResult(
                    new LocalVariableDeclarationStatement(getLeftIToken(), getRightIToken(),
                                                          (LocalVariableDeclaration)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 130:  LocalVariableDeclaration ::= Modifiers Type VariableDeclarators
            //
            case 130: {
                setResult(
                    new LocalVariableDeclaration(JavaParser.this, getLeftIToken(), getRightIToken(),
                                                 (ModifierList)getRhsSym(1),
                                                 (IType)getRhsSym(2),
                                                 (VariableDeclaratorList)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 131:  LocalVariableDeclaration ::= Type VariableDeclarators
            //
            case 131: {
                setResult(
                    new LocalVariableDeclaration(JavaParser.this, getLeftIToken(), getRightIToken(),
                                                 (ModifierList)null,
                                                 (IType)getRhsSym(1),
                                                 (VariableDeclaratorList)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 132:  Statement ::= StatementWithoutTrailingSubstatement
            //
            case 132:
                break; 
            //
            // Rule 133:  Statement ::= LabeledStatement
            //
            case 133:
                break; 
            //
            // Rule 134:  Statement ::= IfThenStatement
            //
            case 134:
                break; 
            //
            // Rule 135:  Statement ::= IfThenElseStatement
            //
            case 135:
                break; 
            //
            // Rule 136:  Statement ::= WhileStatement
            //
            case 136:
                break; 
            //
            // Rule 137:  Statement ::= ForStatement
            //
            case 137:
                break; 
            //
            // Rule 138:  StatementNoShortIf ::= StatementWithoutTrailingSubstatement
            //
            case 138:
                break; 
            //
            // Rule 139:  StatementNoShortIf ::= LabeledStatementNoShortIf
            //
            case 139:
                break; 
            //
            // Rule 140:  StatementNoShortIf ::= IfThenElseStatementNoShortIf
            //
            case 140:
                break; 
            //
            // Rule 141:  StatementNoShortIf ::= WhileStatementNoShortIf
            //
            case 141:
                break; 
            //
            // Rule 142:  StatementNoShortIf ::= ForStatementNoShortIf
            //
            case 142:
                break; 
            //
            // Rule 143:  StatementWithoutTrailingSubstatement ::= Block
            //
            case 143:
                break; 
            //
            // Rule 144:  StatementWithoutTrailingSubstatement ::= EmptyStatement
            //
            case 144:
                break; 
            //
            // Rule 145:  StatementWithoutTrailingSubstatement ::= ExpressionStatement
            //
            case 145:
                break; 
            //
            // Rule 146:  StatementWithoutTrailingSubstatement ::= SwitchStatement
            //
            case 146:
                break; 
            //
            // Rule 147:  StatementWithoutTrailingSubstatement ::= DoStatement
            //
            case 147:
                break; 
            //
            // Rule 148:  StatementWithoutTrailingSubstatement ::= BreakStatement
            //
            case 148:
                break; 
            //
            // Rule 149:  StatementWithoutTrailingSubstatement ::= ContinueStatement
            //
            case 149:
                break; 
            //
            // Rule 150:  StatementWithoutTrailingSubstatement ::= ReturnStatement
            //
            case 150:
                break; 
            //
            // Rule 151:  StatementWithoutTrailingSubstatement ::= SynchronizedStatement
            //
            case 151:
                break; 
            //
            // Rule 152:  StatementWithoutTrailingSubstatement ::= ThrowStatement
            //
            case 152:
                break; 
            //
            // Rule 153:  StatementWithoutTrailingSubstatement ::= TryStatement
            //
            case 153:
                break; 
            //
            // Rule 154:  EmptyStatement ::= ;
            //
            case 154: {
                setResult(
                    new EmptyStatement(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 155:  LabeledStatement ::= IDENTIFIER : Statement
            //
            case 155: {
                setResult(
                    new LabeledStatement(getLeftIToken(), getRightIToken(),
                                         (Ast)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 156:  LabeledStatementNoShortIf ::= IDENTIFIER : StatementNoShortIf$Statement
            //
            case 156: {
                setResult(
                    new LabeledStatement(getLeftIToken(), getRightIToken(),
                                         (Ast)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 157:  ExpressionStatement ::= StatementExpression ;
            //
            case 157: {
                setResult(
                    new ExpressionStatement(getLeftIToken(), getRightIToken(),
                                            (IStatementExpression)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 158:  StatementExpression ::= Assignment
            //
            case 158:
                break; 
            //
            // Rule 159:  StatementExpression ::= PreIncrementExpression
            //
            case 159:
                break; 
            //
            // Rule 160:  StatementExpression ::= PreDecrementExpression
            //
            case 160:
                break; 
            //
            // Rule 161:  StatementExpression ::= PostIncrementExpression
            //
            case 161:
                break; 
            //
            // Rule 162:  StatementExpression ::= PostDecrementExpression
            //
            case 162:
                break; 
            //
            // Rule 163:  StatementExpression ::= MethodInvocation
            //
            case 163:
                break; 
            //
            // Rule 164:  StatementExpression ::= ClassInstanceCreationExpression
            //
            case 164:
                break; 
            //
            // Rule 165:  IfThenStatement ::= if ( Expression ) Statement$thenStmt
            //
            case 165: {
                setResult(
                    new IfStatement(getLeftIToken(), getRightIToken(),
                                    (IExpression)getRhsSym(3),
                                    (Ast)getRhsSym(5),
                                    (Ast)null)
                );
                break;
            } 
            //
            // Rule 166:  IfThenElseStatement ::= if ( Expression ) StatementNoShortIf$thenStmt else Statement$elseStmt
            //
            case 166: {
                setResult(
                    new IfStatement(getLeftIToken(), getRightIToken(),
                                    (IExpression)getRhsSym(3),
                                    (Ast)getRhsSym(5),
                                    (Ast)getRhsSym(7))
                );
                break;
            } 
            //
            // Rule 167:  IfThenElseStatementNoShortIf ::= if ( Expression ) StatementNoShortIf$thenStmt else StatementNoShortIf$elseStmt
            //
            case 167: {
                setResult(
                    new IfStatement(getLeftIToken(), getRightIToken(),
                                    (IExpression)getRhsSym(3),
                                    (Ast)getRhsSym(5),
                                    (Ast)getRhsSym(7))
                );
                break;
            } 
            //
            // Rule 168:  SwitchStatement ::= switch ( Expression ) SwitchBlock
            //
            case 168: {
                setResult(
                    new SwitchStatement(getLeftIToken(), getRightIToken(),
                                        (IExpression)getRhsSym(3),
                                        (SwitchBlock)getRhsSym(5))
                );
                break;
            } 
            //
            // Rule 169:  SwitchBlock ::= { SwitchLabelsopt }
            //
            case 169: {
                setResult(
                    new SwitchBlock(JavaParser.this, getLeftIToken(), getRightIToken(),
                                    (SwitchLabelList)getRhsSym(2),
                                    (SwitchBlockStatementList)null)
                );
                break;
            } 
            //
            // Rule 170:  SwitchBlock ::= { SwitchBlockStatements SwitchLabelsopt }
            //
            case 170: {
                setResult(
                    new SwitchBlock(JavaParser.this, getLeftIToken(), getRightIToken(),
                                    (SwitchLabelList)getRhsSym(3),
                                    (SwitchBlockStatementList)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 171:  SwitchBlockStatements ::= SwitchBlockStatement
            //
            case 171: {
                setResult(
                    new SwitchBlockStatementList((SwitchBlockStatement)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 172:  SwitchBlockStatements ::= SwitchBlockStatements SwitchBlockStatement
            //
            case 172: {
                ((SwitchBlockStatementList)getRhsSym(1)).add((SwitchBlockStatement)getRhsSym(2));
                break;
            } 
            //
            // Rule 173:  SwitchBlockStatement ::= SwitchLabels BlockStatements
            //
            case 173: {
                setResult(
                    new SwitchBlockStatement(getLeftIToken(), getRightIToken(),
                                             (SwitchLabelList)getRhsSym(1),
                                             (BlockStatementList)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 174:  SwitchLabels ::= SwitchLabel
            //
            case 174: {
                setResult(
                    new SwitchLabelList((ISwitchLabel)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 175:  SwitchLabels ::= SwitchLabels SwitchLabel
            //
            case 175: {
                ((SwitchLabelList)getRhsSym(1)).add((ISwitchLabel)getRhsSym(2));
                break;
            } 
            //
            // Rule 176:  SwitchLabel ::= case ConstantExpression :
            //
            case 176: {
                setResult(
                    new CaseLabel(getLeftIToken(), getRightIToken(),
                                  (IConstantExpression)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 177:  SwitchLabel ::= default :
            //
            case 177: {
                setResult(
                    new DefaultLabel(getLeftIToken(), getRightIToken())
                );
                break;
            } 
            //
            // Rule 178:  WhileStatement ::= while ( Expression ) Statement
            //
            case 178: {
                setResult(
                    new WhileStatement(getLeftIToken(), getRightIToken(),
                                       (IExpression)getRhsSym(3),
                                       (Ast)getRhsSym(5))
                );
                break;
            } 
            //
            // Rule 179:  WhileStatementNoShortIf ::= while ( Expression ) StatementNoShortIf$Statement
            //
            case 179: {
                setResult(
                    new WhileStatement(getLeftIToken(), getRightIToken(),
                                       (IExpression)getRhsSym(3),
                                       (Ast)getRhsSym(5))
                );
                break;
            } 
            //
            // Rule 180:  DoStatement ::= do Statement while ( Expression ) ;
            //
            case 180: {
                setResult(
                    new DoStatement(getLeftIToken(), getRightIToken(),
                                    (IStatement)getRhsSym(2),
                                    (IExpression)getRhsSym(5))
                );
                break;
            } 
            //
            // Rule 181:  ForStatement ::= for ( ForInitopt ; Expressionopt ; ForUpdateopt ) Statement
            //
            case 181: {
                setResult(
                    new ForStatement(getLeftIToken(), getRightIToken(),
                                     (IForInitopt)getRhsSym(3),
                                     (IExpressionopt)getRhsSym(5),
                                     (StatementExpressionList)getRhsSym(7),
                                     (Ast)getRhsSym(9))
                );
                break;
            } 
            //
            // Rule 182:  ForStatementNoShortIf ::= for ( ForInitopt ; Expressionopt ; ForUpdateopt ) StatementNoShortIf$Statement
            //
            case 182: {
                setResult(
                    new ForStatement(getLeftIToken(), getRightIToken(),
                                     (IForInitopt)getRhsSym(3),
                                     (IExpressionopt)getRhsSym(5),
                                     (StatementExpressionList)getRhsSym(7),
                                     (Ast)getRhsSym(9))
                );
                break;
            } 
            //
            // Rule 183:  ForInit ::= StatementExpressionList
            //
            case 183:
                break; 
            //
            // Rule 184:  ForInit ::= LocalVariableDeclaration
            //
            case 184:
                break; 
            //
            // Rule 185:  ForUpdate ::= StatementExpressionList
            //
            case 185:
                break; 
            //
            // Rule 186:  StatementExpressionList ::= StatementExpression
            //
            case 186: {
                setResult(
                    new StatementExpressionList((IStatementExpression)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 187:  StatementExpressionList ::= StatementExpressionList , StatementExpression
            //
            case 187: {
                ((StatementExpressionList)getRhsSym(1)).add((IStatementExpression)getRhsSym(3));
                break;
            } 
            //
            // Rule 188:  BreakStatement ::= break IDENTIFIERopt ;
            //
            case 188: {
                setResult(
                    new BreakStatement(getLeftIToken(), getRightIToken(),
                                       (IDENTIFIERopt)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 189:  ContinueStatement ::= continue IDENTIFIERopt ;
            //
            case 189: {
                setResult(
                    new ContinueStatement(getLeftIToken(), getRightIToken(),
                                          (IDENTIFIERopt)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 190:  ReturnStatement ::= return Expressionopt ;
            //
            case 190: {
                setResult(
                    new ReturnStatement(getLeftIToken(), getRightIToken(),
                                        (IExpressionopt)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 191:  ThrowStatement ::= throw Expression ;
            //
            case 191: {
                setResult(
                    new ThrowStatement(getLeftIToken(), getRightIToken(),
                                       (IExpression)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 192:  SynchronizedStatement ::= synchronized ( Expression ) Block
            //
            case 192: {
                setResult(
                    new SynchronizedStatement(getLeftIToken(), getRightIToken(),
                                              (IExpression)getRhsSym(3),
                                              (Block)getRhsSym(5))
                );
                break;
            } 
            //
            // Rule 193:  TryStatement ::= try Block Catches$Catchesopt
            //
            case 193: {
                setResult(
                    new TryStatement(getLeftIToken(), getRightIToken(),
                                     (Block)getRhsSym(2),
                                     (Ast)getRhsSym(3),
                                     (Finally)null)
                );
                break;
            } 
            //
            // Rule 194:  TryStatement ::= try Block Catchesopt Finally
            //
            case 194: {
                setResult(
                    new TryStatement(getLeftIToken(), getRightIToken(),
                                     (Block)getRhsSym(2),
                                     (Ast)getRhsSym(3),
                                     (Finally)getRhsSym(4))
                );
                break;
            } 
            //
            // Rule 195:  Catches ::= CatchClause
            //
            case 195: {
                setResult(
                    new CatchClauseList((CatchClause)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 196:  Catches ::= Catches CatchClause
            //
            case 196: {
                ((CatchClauseList)getRhsSym(1)).add((CatchClause)getRhsSym(2));
                break;
            } 
            //
            // Rule 197:  CatchClause ::= catch ( FormalParameter ) Block
            //
            case 197: {
                setResult(
                    new CatchClause(getLeftIToken(), getRightIToken(),
                                    (FormalParameter)getRhsSym(3),
                                    (Block)getRhsSym(5))
                );
                break;
            } 
            //
            // Rule 198:  Finally ::= finally Block
            //
            case 198: {
                setResult(
                    new Finally(getLeftIToken(), getRightIToken(),
                                (Block)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 199:  Primary ::= PrimaryNoNewArray
            //
            case 199:
                break; 
            //
            // Rule 200:  Primary ::= ArrayCreationExpression
            //
            case 200:
                break; 
            //
            // Rule 201:  PrimaryNoNewArray ::= Literal
            //
            case 201:
                break; 
            //
            // Rule 202:  PrimaryNoNewArray ::= MethodInvocation
            //
            case 202:
                break; 
            //
            // Rule 203:  PrimaryNoNewArray ::= ArrayAccess
            //
            case 203:
                break; 
            //
            // Rule 204:  PrimaryNoNewArray ::= ClassInstanceCreationExpression
            //
            case 204:
                break; 
            //
            // Rule 205:  PrimaryNoNewArray ::= FieldAccess
            //
            case 205:
                break; 
            //
            // Rule 206:  PrimaryNoNewArray ::= ( Expression )
            //
            case 206: {
                setResult(
                    new ParenthesizedExpression(getLeftIToken(), getRightIToken(),
                                                (IExpression)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 207:  PrimaryNoNewArray ::= this
            //
            case 207: {
                setResult(
                    new PrimaryThis(getLeftIToken(), getRightIToken(),
                                    (IName)null)
                );
                break;
            } 
            //
            // Rule 208:  PrimaryNoNewArray ::= Name . this
            //
            case 208: {
                setResult(
                    new PrimaryThis(getLeftIToken(), getRightIToken(),
                                    (IName)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 209:  PrimaryNoNewArray ::= Type . class
            //
            case 209: {
                setResult(
                    new PrimaryClassLiteral(getLeftIToken(), getRightIToken(),
                                            (IType)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 210:  PrimaryNoNewArray ::= void . class
            //
            case 210: {
                setResult(
                    new PrimaryVoidClassLiteral(getLeftIToken(), getRightIToken())
                );
                break;
            } 
            //
            // Rule 211:  ClassInstanceCreationExpression ::= new ClassType ( ArgumentListopt ) ClassBodyopt
            //
            case 211: {
                setResult(
                    new ClassInstanceCreationExpression(getLeftIToken(), getRightIToken(),
                                                        (IClassType)getRhsSym(2),
                                                        (ExpressionList)getRhsSym(4),
                                                        (ClassBody)getRhsSym(6),
                                                        (IPostfixExpression)null)
                );
                break;
            } 
            //
            // Rule 212:  ClassInstanceCreationExpression ::= Primary$expression . new SimpleName$ClassType ( ArgumentListopt ) ClassBodyopt
            //
            case 212: {
                setResult(
                    new ClassInstanceCreationExpression(getLeftIToken(), getRightIToken(),
                                                        (IClassType)getRhsSym(4),
                                                        (ExpressionList)getRhsSym(6),
                                                        (ClassBody)getRhsSym(8),
                                                        (IPostfixExpression)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 213:  ClassInstanceCreationExpression ::= Name$expression . new SimpleName$ClassType ( ArgumentListopt ) ClassBodyopt
            //
            case 213: {
                setResult(
                    new ClassInstanceCreationExpression(getLeftIToken(), getRightIToken(),
                                                        (IClassType)getRhsSym(4),
                                                        (ExpressionList)getRhsSym(6),
                                                        (ClassBody)getRhsSym(8),
                                                        (IPostfixExpression)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 214:  ArgumentList ::= Expression
            //
            case 214: {
                setResult(
                    new ExpressionList((IExpression)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 215:  ArgumentList ::= ArgumentList , Expression
            //
            case 215: {
                ((ExpressionList)getRhsSym(1)).add((IExpression)getRhsSym(3));
                break;
            } 
            //
            // Rule 216:  ArrayCreationExpression ::= new PrimitiveType$Type DimExprs Dimsopt
            //
            case 216: {
                setResult(
                    new ArrayCreationExpression(getLeftIToken(), getRightIToken(),
                                                (IType)getRhsSym(2),
                                                (DimExprList)getRhsSym(3),
                                                (DimList)getRhsSym(4),
                                                (ArrayInitializer)null)
                );
                break;
            } 
            //
            // Rule 217:  ArrayCreationExpression ::= new ClassOrInterfaceType$Type DimExprs Dimsopt
            //
            case 217: {
                setResult(
                    new ArrayCreationExpression(getLeftIToken(), getRightIToken(),
                                                (IType)getRhsSym(2),
                                                (DimExprList)getRhsSym(3),
                                                (DimList)getRhsSym(4),
                                                (ArrayInitializer)null)
                );
                break;
            } 
            //
            // Rule 218:  ArrayCreationExpression ::= new ArrayType$Type ArrayInitializer
            //
            case 218: {
                setResult(
                    new ArrayCreationExpression(getLeftIToken(), getRightIToken(),
                                                (IType)getRhsSym(2),
                                                (DimExprList)null,
                                                (DimList)null,
                                                (ArrayInitializer)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 219:  DimExprs ::= DimExpr
            //
            case 219: {
                setResult(
                    new DimExprList((DimExpr)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 220:  DimExprs ::= DimExprs DimExpr
            //
            case 220: {
                ((DimExprList)getRhsSym(1)).add((DimExpr)getRhsSym(2));
                break;
            } 
            //
            // Rule 221:  DimExpr ::= [ Expression ]
            //
            case 221: {
                setResult(
                    new DimExpr(getLeftIToken(), getRightIToken(),
                                (IExpression)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 222:  Dims ::= Dim
            //
            case 222: {
                setResult(
                    new DimList((Dim)getRhsSym(1), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 223:  Dims ::= Dims Dim
            //
            case 223: {
                ((DimList)getRhsSym(1)).add((Dim)getRhsSym(2));
                break;
            } 
            //
            // Rule 224:  Dim ::= [ ]
            //
            case 224: {
                setResult(
                    new Dim(getLeftIToken(), getRightIToken())
                );
                break;
            } 
            //
            // Rule 225:  FieldAccess ::= Primary . IDENTIFIER
            //
            case 225: {
                setResult(
                    new FieldAccess(getLeftIToken(), getRightIToken(),
                                    (IPrimary)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 226:  FieldAccess ::= super . IDENTIFIER
            //
            case 226: {
                setResult(
                    new SuperFieldAccess(getLeftIToken(), getRightIToken(),
                                         (IName)null)
                );
                break;
            } 
            //
            // Rule 227:  FieldAccess ::= Name . super . IDENTIFIER
            //
            case 227: {
                setResult(
                    new SuperFieldAccess(getLeftIToken(), getRightIToken(),
                                         (IName)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 228:  MethodInvocation ::= Name ( ArgumentListopt )
            //
            case 228: {
                setResult(
                    new MethodInvocation(getLeftIToken(), getRightIToken(),
                                         (IName)getRhsSym(1),
                                         (ExpressionList)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 229:  MethodInvocation ::= Primary . IDENTIFIER ( ArgumentListopt )
            //
            case 229: {
                setResult(
                    new PrimaryMethodInvocation(getLeftIToken(), getRightIToken(),
                                                (IPrimary)getRhsSym(1),
                                                (ExpressionList)getRhsSym(5))
                );
                break;
            } 
            //
            // Rule 230:  MethodInvocation ::= super . IDENTIFIER ( ArgumentListopt )
            //
            case 230: {
                setResult(
                    new SuperMethodInvocation(getLeftIToken(), getRightIToken(),
                                              (ExpressionList)getRhsSym(5),
                                              (IName)null)
                );
                break;
            } 
            //
            // Rule 231:  MethodInvocation ::= Name . super . IDENTIFIER ( ArgumentListopt )
            //
            case 231: {
                setResult(
                    new SuperMethodInvocation(getLeftIToken(), getRightIToken(),
                                              (ExpressionList)getRhsSym(7),
                                              (IName)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 232:  ArrayAccess ::= Name$Base [ Expression ]
            //
            case 232: {
                setResult(
                    new ArrayAccess(getLeftIToken(), getRightIToken(),
                                    (IPostfixExpression)getRhsSym(1),
                                    (IExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 233:  ArrayAccess ::= PrimaryNoNewArray$Base [ Expression ]
            //
            case 233: {
                setResult(
                    new ArrayAccess(getLeftIToken(), getRightIToken(),
                                    (IPostfixExpression)getRhsSym(1),
                                    (IExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 234:  PostfixExpression ::= Primary
            //
            case 234:
                break; 
            //
            // Rule 235:  PostfixExpression ::= Name
            //
            case 235:
                break; 
            //
            // Rule 236:  PostfixExpression ::= PostIncrementExpression
            //
            case 236:
                break; 
            //
            // Rule 237:  PostfixExpression ::= PostDecrementExpression
            //
            case 237:
                break; 
            //
            // Rule 238:  PostIncrementExpression ::= PostfixExpression ++
            //
            case 238: {
                setResult(
                    new PostIncrementExpression(getLeftIToken(), getRightIToken(),
                                                (IPostfixExpression)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 239:  PostDecrementExpression ::= PostfixExpression --
            //
            case 239: {
                setResult(
                    new PostDecrementExpression(getLeftIToken(), getRightIToken(),
                                                (IPostfixExpression)getRhsSym(1))
                );
                break;
            } 
            //
            // Rule 240:  UnaryExpression ::= PreIncrementExpression
            //
            case 240:
                break; 
            //
            // Rule 241:  UnaryExpression ::= PreDecrementExpression
            //
            case 241:
                break; 
            //
            // Rule 242:  UnaryExpression ::= UnaryExpressionNotPlusMinus
            //
            case 242:
                break; 
            //
            // Rule 243:  UnaryExpression ::= + UnaryExpression
            //
            case 243: {
                setResult(
                    new PlusUnaryExpression(getLeftIToken(), getRightIToken(),
                                            (IUnaryExpression)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 244:  UnaryExpression ::= - UnaryExpression
            //
            case 244: {
                setResult(
                    new MinusUnaryExpression(getLeftIToken(), getRightIToken(),
                                             (IUnaryExpression)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 245:  PreIncrementExpression ::= ++ UnaryExpression
            //
            case 245: {
                setResult(
                    new PreIncrementExpression(getLeftIToken(), getRightIToken(),
                                               (IUnaryExpression)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 246:  PreDecrementExpression ::= -- UnaryExpression
            //
            case 246: {
                setResult(
                    new PreDecrementExpression(getLeftIToken(), getRightIToken(),
                                               (IUnaryExpression)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 247:  UnaryExpressionNotPlusMinus ::= PostfixExpression
            //
            case 247:
                break; 
            //
            // Rule 248:  UnaryExpressionNotPlusMinus ::= CastExpression
            //
            case 248:
                break; 
            //
            // Rule 249:  UnaryExpressionNotPlusMinus ::= ~ UnaryExpression
            //
            case 249: {
                setResult(
                    new UnaryComplementExpression(getLeftIToken(), getRightIToken(),
                                                  (IUnaryExpression)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 250:  UnaryExpressionNotPlusMinus ::= ! UnaryExpression
            //
            case 250: {
                setResult(
                    new UnaryNotExpression(getLeftIToken(), getRightIToken(),
                                           (IUnaryExpression)getRhsSym(2))
                );
                break;
            } 
            //
            // Rule 251:  CastExpression ::= ( PrimitiveType Dimsopt ) UnaryExpression
            //
            case 251: {
                setResult(
                    new PrimitiveCastExpression(getLeftIToken(), getRightIToken(),
                                                (IPrimitiveType)getRhsSym(2),
                                                (DimList)getRhsSym(3),
                                                (IUnaryExpression)getRhsSym(5))
                );
                break;
            } 
            //
            // Rule 252:  CastExpression ::= ( Expression$Name ) UnaryExpressionNotPlusMinus
            //
            case 252: {
                setResult(
                    new ClassCastExpression(getLeftIToken(), getRightIToken(),
                                            (IExpression)getRhsSym(2),
                                            (IUnaryExpressionNotPlusMinus)getRhsSym(4),
                                            (DimList)null)
                );
                break;
            } 
            //
            // Rule 253:  CastExpression ::= ( Name$Name Dims ) UnaryExpressionNotPlusMinus
            //
            case 253: {
                setResult(
                    new ClassCastExpression(getLeftIToken(), getRightIToken(),
                                            (IExpression)getRhsSym(2),
                                            (IUnaryExpressionNotPlusMinus)getRhsSym(5),
                                            (DimList)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 254:  MultiplicativeExpression ::= UnaryExpression
            //
            case 254:
                break; 
            //
            // Rule 255:  MultiplicativeExpression ::= MultiplicativeExpression * UnaryExpression
            //
            case 255: {
                setResult(
                    new MultiplyExpression(getLeftIToken(), getRightIToken(),
                                           (IMultiplicativeExpression)getRhsSym(1),
                                           (IUnaryExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 256:  MultiplicativeExpression ::= MultiplicativeExpression / UnaryExpression
            //
            case 256: {
                setResult(
                    new DivideExpression(getLeftIToken(), getRightIToken(),
                                         (IMultiplicativeExpression)getRhsSym(1),
                                         (IUnaryExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 257:  MultiplicativeExpression ::= MultiplicativeExpression % UnaryExpression
            //
            case 257: {
                setResult(
                    new ModExpression(getLeftIToken(), getRightIToken(),
                                      (IMultiplicativeExpression)getRhsSym(1),
                                      (IUnaryExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 258:  AdditiveExpression ::= MultiplicativeExpression
            //
            case 258:
                break; 
            //
            // Rule 259:  AdditiveExpression ::= AdditiveExpression + MultiplicativeExpression
            //
            case 259: {
                setResult(
                    new AddExpression(getLeftIToken(), getRightIToken(),
                                      (IAdditiveExpression)getRhsSym(1),
                                      (IMultiplicativeExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 260:  AdditiveExpression ::= AdditiveExpression - MultiplicativeExpression
            //
            case 260: {
                setResult(
                    new SubtractExpression(getLeftIToken(), getRightIToken(),
                                           (IAdditiveExpression)getRhsSym(1),
                                           (IMultiplicativeExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 261:  ShiftExpression ::= AdditiveExpression
            //
            case 261:
                break; 
            //
            // Rule 262:  ShiftExpression ::= ShiftExpression << AdditiveExpression
            //
            case 262: {
                setResult(
                    new LeftShiftExpression(getLeftIToken(), getRightIToken(),
                                            (IShiftExpression)getRhsSym(1),
                                            (IAdditiveExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 263:  ShiftExpression ::= ShiftExpression >> AdditiveExpression
            //
            case 263: {
                setResult(
                    new RightShiftExpression(getLeftIToken(), getRightIToken(),
                                             (IShiftExpression)getRhsSym(1),
                                             (IAdditiveExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 264:  ShiftExpression ::= ShiftExpression >>> AdditiveExpression
            //
            case 264: {
                setResult(
                    new UnsignedRightShiftExpression(getLeftIToken(), getRightIToken(),
                                                     (IShiftExpression)getRhsSym(1),
                                                     (IAdditiveExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 265:  RelationalExpression ::= ShiftExpression
            //
            case 265:
                break; 
            //
            // Rule 266:  RelationalExpression ::= RelationalExpression < ShiftExpression
            //
            case 266: {
                setResult(
                    new LessExpression(getLeftIToken(), getRightIToken(),
                                       (IRelationalExpression)getRhsSym(1),
                                       (IShiftExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 267:  RelationalExpression ::= RelationalExpression > ShiftExpression
            //
            case 267: {
                setResult(
                    new GreaterExpression(getLeftIToken(), getRightIToken(),
                                          (IRelationalExpression)getRhsSym(1),
                                          (IShiftExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 268:  RelationalExpression ::= RelationalExpression <= ShiftExpression
            //
            case 268: {
                setResult(
                    new LessEqualExpression(getLeftIToken(), getRightIToken(),
                                            (IRelationalExpression)getRhsSym(1),
                                            (IShiftExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 269:  RelationalExpression ::= RelationalExpression >= ShiftExpression
            //
            case 269: {
                setResult(
                    new GreaterEqualExpression(getLeftIToken(), getRightIToken(),
                                               (IRelationalExpression)getRhsSym(1),
                                               (IShiftExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 270:  RelationalExpression ::= RelationalExpression instanceof ReferenceType
            //
            case 270: {
                setResult(
                    new InstanceofExpression(getLeftIToken(), getRightIToken(),
                                             (IRelationalExpression)getRhsSym(1),
                                             (IReferenceType)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 271:  EqualityExpression ::= RelationalExpression
            //
            case 271:
                break; 
            //
            // Rule 272:  EqualityExpression ::= EqualityExpression == RelationalExpression
            //
            case 272: {
                setResult(
                    new EqualExpression(getLeftIToken(), getRightIToken(),
                                        (IEqualityExpression)getRhsSym(1),
                                        (IRelationalExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 273:  EqualityExpression ::= EqualityExpression != RelationalExpression
            //
            case 273: {
                setResult(
                    new NotEqualExpression(getLeftIToken(), getRightIToken(),
                                           (IEqualityExpression)getRhsSym(1),
                                           (IRelationalExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 274:  AndExpression ::= EqualityExpression
            //
            case 274:
                break; 
            //
            // Rule 275:  AndExpression ::= AndExpression & EqualityExpression
            //
            case 275: {
                setResult(
                    new AndExpression(getLeftIToken(), getRightIToken(),
                                      (IAndExpression)getRhsSym(1),
                                      (IEqualityExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 276:  ExclusiveOrExpression ::= AndExpression
            //
            case 276:
                break; 
            //
            // Rule 277:  ExclusiveOrExpression ::= ExclusiveOrExpression ^ AndExpression
            //
            case 277: {
                setResult(
                    new ExclusiveOrExpression(getLeftIToken(), getRightIToken(),
                                              (IExclusiveOrExpression)getRhsSym(1),
                                              (IAndExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 278:  InclusiveOrExpression ::= ExclusiveOrExpression
            //
            case 278:
                break; 
            //
            // Rule 279:  InclusiveOrExpression ::= InclusiveOrExpression | ExclusiveOrExpression
            //
            case 279: {
                setResult(
                    new InclusiveOrExpression(getLeftIToken(), getRightIToken(),
                                              (IInclusiveOrExpression)getRhsSym(1),
                                              (IExclusiveOrExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 280:  ConditionalAndExpression ::= InclusiveOrExpression
            //
            case 280:
                break; 
            //
            // Rule 281:  ConditionalAndExpression ::= ConditionalAndExpression && InclusiveOrExpression
            //
            case 281: {
                setResult(
                    new ConditionalAndExpression(getLeftIToken(), getRightIToken(),
                                                 (IConditionalAndExpression)getRhsSym(1),
                                                 (IInclusiveOrExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 282:  ConditionalOrExpression ::= ConditionalAndExpression
            //
            case 282:
                break; 
            //
            // Rule 283:  ConditionalOrExpression ::= ConditionalOrExpression || ConditionalAndExpression
            //
            case 283: {
                setResult(
                    new ConditionalOrExpression(getLeftIToken(), getRightIToken(),
                                                (IConditionalOrExpression)getRhsSym(1),
                                                (IConditionalAndExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 284:  ConditionalExpression ::= ConditionalOrExpression
            //
            case 284:
                break; 
            //
            // Rule 285:  ConditionalExpression ::= ConditionalOrExpression ? Expression : ConditionalExpression
            //
            case 285: {
                setResult(
                    new ConditionalExpression(getLeftIToken(), getRightIToken(),
                                              (IConditionalOrExpression)getRhsSym(1),
                                              (IExpression)getRhsSym(3),
                                              (IConditionalExpression)getRhsSym(5))
                );
                break;
            } 
            //
            // Rule 286:  AssignmentExpression ::= ConditionalExpression
            //
            case 286:
                break; 
            //
            // Rule 287:  AssignmentExpression ::= Assignment
            //
            case 287:
                break; 
            //
            // Rule 288:  Assignment ::= LeftHandSide AssignmentOperator AssignmentExpression
            //
            case 288: {
                setResult(
                    new Assignment(getLeftIToken(), getRightIToken(),
                                   (ILeftHandSide)getRhsSym(1),
                                   (IAssignmentOperator)getRhsSym(2),
                                   (IAssignmentExpression)getRhsSym(3))
                );
                break;
            } 
            //
            // Rule 289:  LeftHandSide ::= Name
            //
            case 289:
                break; 
            //
            // Rule 290:  LeftHandSide ::= FieldAccess
            //
            case 290:
                break; 
            //
            // Rule 291:  LeftHandSide ::= ArrayAccess
            //
            case 291:
                break; 
            //
            // Rule 292:  AssignmentOperator ::= =
            //
            case 292: {
                setResult(
                    new EqualOperator(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 293:  AssignmentOperator ::= *=
            //
            case 293: {
                setResult(
                    new MultiplyEqualOperator(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 294:  AssignmentOperator ::= /=
            //
            case 294: {
                setResult(
                    new DivideEqualOperator(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 295:  AssignmentOperator ::= %=
            //
            case 295: {
                setResult(
                    new ModEqualOperator(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 296:  AssignmentOperator ::= +=
            //
            case 296: {
                setResult(
                    new PlusEqualOperator(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 297:  AssignmentOperator ::= -=
            //
            case 297: {
                setResult(
                    new MinusEqualOperator(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 298:  AssignmentOperator ::= <<=
            //
            case 298: {
                setResult(
                    new LeftShiftEqualOperator(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 299:  AssignmentOperator ::= >>=
            //
            case 299: {
                setResult(
                    new RightShiftEqualOperator(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 300:  AssignmentOperator ::= >>>=
            //
            case 300: {
                setResult(
                    new UnsignedRightShiftEqualOperator(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 301:  AssignmentOperator ::= &=
            //
            case 301: {
                setResult(
                    new AndEqualOperator(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 302:  AssignmentOperator ::= ^=
            //
            case 302: {
                setResult(
                    new ExclusiveOrEqualOperator(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 303:  AssignmentOperator ::= |=
            //
            case 303: {
                setResult(
                    new OrEqualOperator(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 304:  Expression ::= AssignmentExpression
            //
            case 304:
                break; 
            //
            // Rule 305:  ConstantExpression ::= Expression
            //
            case 305:
                break; 
            //
            // Rule 306:  PackageDeclarationopt ::= $Empty
            //
            case 306: {
                setResult(null);
                break;
            } 
            //
            // Rule 307:  PackageDeclarationopt ::= PackageDeclaration
            //
            case 307:
                break; 
            //
            // Rule 308:  Superopt ::= $Empty
            //
            case 308: {
                setResult(null);
                break;
            } 
            //
            // Rule 309:  Superopt ::= Super
            //
            case 309:
                break; 
            //
            // Rule 310:  Expressionopt ::= $Empty
            //
            case 310: {
                setResult(null);
                break;
            } 
            //
            // Rule 311:  Expressionopt ::= Expression
            //
            case 311:
                break; 
            //
            // Rule 312:  ClassBodyopt ::= $Empty
            //
            case 312: {
                setResult(null);
                break;
            } 
            //
            // Rule 313:  ClassBodyopt ::= ClassBody
            //
            case 313:
                break; 
            //
            // Rule 314:  ImportDeclarationsopt ::= $Empty
            //
            case 314: {
                setResult(
                    new ImportDeclarationList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 315:  ImportDeclarationsopt ::= ImportDeclarations
            //
            case 315:
                break; 
            //
            // Rule 316:  TypeDeclarationsopt ::= $Empty
            //
            case 316: {
                setResult(
                    new TypeDeclarationList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 317:  TypeDeclarationsopt ::= TypeDeclarations
            //
            case 317:
                break; 
            //
            // Rule 318:  ClassBodyDeclarationsopt ::= $Empty
            //
            case 318: {
                setResult(
                    new ClassBodyDeclarationList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 319:  ClassBodyDeclarationsopt ::= ClassBodyDeclarations
            //
            case 319:
                break; 
            //
            // Rule 320:  Modifiersopt ::= $Empty
            //
            case 320: {
                setResult(
                    new ModifierList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 321:  Modifiersopt ::= Modifiers
            //
            case 321:
                break; 
            //
            // Rule 322:  ExplicitConstructorInvocationopt ::= $Empty
            //
            case 322: {
                setResult(null);
                break;
            } 
            //
            // Rule 323:  ExplicitConstructorInvocationopt ::= ExplicitConstructorInvocation
            //
            case 323:
                break; 
            //
            // Rule 324:  BlockStatementsopt ::= $Empty
            //
            case 324: {
                setResult(
                    new BlockStatementList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 325:  BlockStatementsopt ::= BlockStatements
            //
            case 325:
                break; 
            //
            // Rule 326:  Dimsopt ::= $Empty
            //
            case 326: {
                setResult(
                    new DimList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 327:  Dimsopt ::= Dims
            //
            case 327:
                break; 
            //
            // Rule 328:  ArgumentListopt ::= $Empty
            //
            case 328: {
                setResult(
                    new ExpressionList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 329:  ArgumentListopt ::= ArgumentList
            //
            case 329:
                break; 
            //
            // Rule 330:  Throwsopt ::= $Empty
            //
            case 330: {
                setResult(
                    new ClassTypeList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 331:  Throwsopt ::= Throws
            //
            case 331:
                break; 
            //
            // Rule 332:  FormalParameterListopt ::= $Empty
            //
            case 332: {
                setResult(
                    new FormalParameterList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 333:  FormalParameterListopt ::= FormalParameterList
            //
            case 333:
                break; 
            //
            // Rule 334:  Interfacesopt ::= $Empty
            //
            case 334: {
                setResult(
                    new InterfaceTypeList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 335:  Interfacesopt ::= Interfaces
            //
            case 335:
                break; 
            //
            // Rule 336:  InterfaceMemberDeclarationsopt ::= $Empty
            //
            case 336: {
                setResult(
                    new InterfaceMemberDeclarationList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 337:  InterfaceMemberDeclarationsopt ::= InterfaceMemberDeclarations
            //
            case 337:
                break; 
            //
            // Rule 338:  ForInitopt ::= $Empty
            //
            case 338: {
                setResult(null);
                break;
            } 
            //
            // Rule 339:  ForInitopt ::= ForInit
            //
            case 339:
                break; 
            //
            // Rule 340:  ForUpdateopt ::= $Empty
            //
            case 340: {
                setResult(
                    new StatementExpressionList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 341:  ForUpdateopt ::= ForUpdate
            //
            case 341:
                break; 
            //
            // Rule 342:  ExtendsInterfacesopt ::= $Empty
            //
            case 342: {
                setResult(
                    new InterfaceTypeList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 343:  ExtendsInterfacesopt ::= ExtendsInterfaces
            //
            case 343:
                break; 
            //
            // Rule 344:  Catchesopt ::= $Empty
            //
            case 344: {
                setResult(
                    new CatchClauseList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 345:  Catchesopt ::= Catches
            //
            case 345:
                break; 
            //
            // Rule 346:  VariableInitializersopt ::= $Empty
            //
            case 346: {
                setResult(
                    new VariableInitializerList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 347:  VariableInitializersopt ::= VariableInitializers
            //
            case 347:
                break; 
            //
            // Rule 348:  SwitchBlockStatementsopt ::= $Empty
            //
            case 348: {
                setResult(
                    new SwitchBlockStatementList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 349:  SwitchBlockStatementsopt ::= SwitchBlockStatements
            //
            case 349:
                break; 
            //
            // Rule 350:  SwitchLabelsopt ::= $Empty
            //
            case 350: {
                setResult(
                    new SwitchLabelList(getLeftIToken(), getRightIToken(), true /* left recursive */)
                );
                break;
            } 
            //
            // Rule 351:  SwitchLabelsopt ::= SwitchLabels
            //
            case 351:
                break; 
            //
            // Rule 352:  Commaopt ::= $Empty
            //
            case 352: {
                setResult(null);
                break;
            } 
            //
            // Rule 353:  Commaopt ::= ,
            //
            case 353: {
                setResult(
                    new Commaopt(getRhsIToken(1))
                );
                break;
            } 
            //
            // Rule 354:  IDENTIFIERopt ::= $Empty
            //
            case 354: {
                setResult(null);
                break;
            } 
            //
            // Rule 355:  IDENTIFIERopt ::= IDENTIFIER
            //
            case 355: {
                setResult(
                    new IDENTIFIERopt(getRhsIToken(1))
                );
                break;
            }
    
            default:
                break;
        }
        return;
    }
}



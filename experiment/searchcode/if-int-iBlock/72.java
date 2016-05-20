//
// A copyright notice must contain a "C" enclosed in parentheses: (C) 
//

package expandedjavaparser.Ast;

import lpg.lpgjavaruntime.*;

/**
 *<b>
 *<li>Rule 123:  Block ::= { BlockStatementsopt }
 *</b>
 */
public class Block extends Ast implements IBlock
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
        Block other = (Block) o;
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




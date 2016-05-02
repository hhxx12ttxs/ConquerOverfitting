// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public final class FunctionDef extends stmtType {
    public NameTokType name;
    public argumentsType args;
    public stmtType[] body;
    public decoratorsType[] decs;
    public exprType returns;

    public FunctionDef(NameTokType name, argumentsType args, stmtType[] body, decoratorsType[]
    decs, exprType returns) {
        this.name = name;
        this.args = args;
        this.body = body;
        this.decs = decs;
        this.returns = returns;
    }

    public FunctionDef(NameTokType name, argumentsType args, stmtType[] body, decoratorsType[]
    decs, exprType returns, SimpleNode parent) {
        this(name, args, body, decs, returns);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public FunctionDef createCopy() {
        stmtType[] new0;
        if(this.body != null){
        new0 = new stmtType[this.body.length];
        for(int i=0;i<this.body.length;i++){
            new0[i] = (stmtType) (this.body[i] != null? this.body[i].createCopy():null);
        }
        }else{
            new0 = this.body;
        }
        decoratorsType[] new1;
        if(this.decs != null){
        new1 = new decoratorsType[this.decs.length];
        for(int i=0;i<this.decs.length;i++){
            new1[i] = (decoratorsType) (this.decs[i] != null? this.decs[i].createCopy():null);
        }
        }else{
            new1 = this.decs;
        }
        FunctionDef temp = new FunctionDef(name!=null?(NameTokType)name.createCopy():null,
        args!=null?(argumentsType)args.createCopy():null, new0, new1,
        returns!=null?(exprType)returns.createCopy():null);
        temp.beginLine = this.beginLine;
        temp.beginColumn = this.beginColumn;
        if(this.specialsBefore != null){
            for(Object o:this.specialsBefore){
                if(o instanceof commentType){
                    commentType commentType = (commentType) o;
                    temp.getSpecialsBefore().add(commentType.createCopy());
                }
            }
        }
        if(this.specialsAfter != null){
            for(Object o:this.specialsAfter){
                if(o instanceof commentType){
                    commentType commentType = (commentType) o;
                    temp.getSpecialsAfter().add(commentType.createCopy());
                }
            }
        }
        return temp;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("FunctionDef[");
        sb.append("name=");
        sb.append(dumpThis(this.name));
        sb.append(", ");
        sb.append("args=");
        sb.append(dumpThis(this.args));
        sb.append(", ");
        sb.append("body=");
        sb.append(dumpThis(this.body));
        sb.append(", ");
        sb.append("decs=");
        sb.append(dumpThis(this.decs));
        sb.append(", ");
        sb.append("returns=");
        sb.append(dumpThis(this.returns));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitFunctionDef(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (name != null){
            name.accept(visitor);
        }
        if (args != null){
            args.accept(visitor);
        }
        if (body != null) {
            for (int i = 0; i < body.length; i++) {
                if (body[i] != null){
                    body[i].accept(visitor);
                }
            }
        }
        if (decs != null) {
            for (int i = 0; i < decs.length; i++) {
                if (decs[i] != null){
                    decs[i].accept(visitor);
                }
            }
        }
        if (returns != null){
            returns.accept(visitor);
        }
    }

}


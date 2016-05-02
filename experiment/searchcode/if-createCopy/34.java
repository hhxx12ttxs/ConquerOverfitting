// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public final class decoratorsType extends SimpleNode {
    public exprType func;
    public exprType[] args;
    public keywordType[] keywords;
    public exprType starargs;
    public exprType kwargs;

    public decoratorsType(exprType func, exprType[] args, keywordType[] keywords, exprType
    starargs, exprType kwargs) {
        this.func = func;
        this.args = args;
        this.keywords = keywords;
        this.starargs = starargs;
        this.kwargs = kwargs;
    }

    public decoratorsType(exprType func, exprType[] args, keywordType[] keywords, exprType
    starargs, exprType kwargs, SimpleNode parent) {
        this(func, args, keywords, starargs, kwargs);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public decoratorsType createCopy() {
        exprType[] new0;
        if(this.args != null){
        new0 = new exprType[this.args.length];
        for(int i=0;i<this.args.length;i++){
            new0[i] = (exprType) (this.args[i] != null? this.args[i].createCopy():null);
        }
        }else{
            new0 = this.args;
        }
        keywordType[] new1;
        if(this.keywords != null){
        new1 = new keywordType[this.keywords.length];
        for(int i=0;i<this.keywords.length;i++){
            new1[i] = (keywordType) (this.keywords[i] != null? this.keywords[i].createCopy():null);
        }
        }else{
            new1 = this.keywords;
        }
        decoratorsType temp = new decoratorsType(func!=null?(exprType)func.createCopy():null, new0,
        new1, starargs!=null?(exprType)starargs.createCopy():null,
        kwargs!=null?(exprType)kwargs.createCopy():null);
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
        StringBuffer sb = new StringBuffer("decorators[");
        sb.append("func=");
        sb.append(dumpThis(this.func));
        sb.append(", ");
        sb.append("args=");
        sb.append(dumpThis(this.args));
        sb.append(", ");
        sb.append("keywords=");
        sb.append(dumpThis(this.keywords));
        sb.append(", ");
        sb.append("starargs=");
        sb.append(dumpThis(this.starargs));
        sb.append(", ");
        sb.append("kwargs=");
        sb.append(dumpThis(this.kwargs));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        traverse(visitor);
        return null;
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (func != null){
            func.accept(visitor);
        }
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null){
                    args[i].accept(visitor);
                }
            }
        }
        if (keywords != null) {
            for (int i = 0; i < keywords.length; i++) {
                if (keywords[i] != null){
                    keywords[i].accept(visitor);
                }
            }
        }
        if (starargs != null){
            starargs.accept(visitor);
        }
        if (kwargs != null){
            kwargs.accept(visitor);
        }
    }

}


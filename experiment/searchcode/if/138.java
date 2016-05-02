// Autogenerated AST node
package org.python.pydev.parser.jython.ast;

import org.python.pydev.parser.jython.SimpleNode;
import java.util.Arrays;

public final class If extends stmtType {
    public exprType test;
    public stmtType[] body;
    public suiteType orelse;

    public If(exprType test, stmtType[] body, suiteType orelse) {
        this.test = test;
        this.body = body;
        this.orelse = orelse;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((test == null) ? 0 : test.hashCode());
        result = prime * result + Arrays.hashCode(body);
        result = prime * result + ((orelse == null) ? 0 : orelse.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        If other = (If) obj;
        if (test == null) {
            if (other.test != null)
                return false;
        } else if (!test.equals(other.test))
            return false;
        if (!Arrays.equals(body, other.body))
            return false;
        if (orelse == null) {
            if (other.orelse != null)
                return false;
        } else if (!orelse.equals(other.orelse))
            return false;
        return true;
    }

    public If createCopy() {
        return createCopy(true);
    }

    public If createCopy(boolean copyComments) {
        stmtType[] new0;
        if (this.body != null) {
            new0 = new stmtType[this.body.length];
            for (int i = 0; i < this.body.length; i++) {
                new0[i] = (stmtType) (this.body[i] != null ? this.body[i].createCopy(copyComments) : null);
            }
        } else {
            new0 = this.body;
        }
        If temp = new If(test != null ? (exprType) test.createCopy(copyComments) : null, new0,
                orelse != null ? (suiteType) orelse.createCopy(copyComments) : null);
        temp.beginLine = this.beginLine;
        temp.beginColumn = this.beginColumn;
        if (this.specialsBefore != null && copyComments) {
            for (Object o : this.specialsBefore) {
                if (o instanceof commentType) {
                    commentType commentType = (commentType) o;
                    temp.getSpecialsBefore().add(commentType.createCopy(copyComments));
                }
            }
        }
        if (this.specialsAfter != null && copyComments) {
            for (Object o : this.specialsAfter) {
                if (o instanceof commentType) {
                    commentType commentType = (commentType) o;
                    temp.getSpecialsAfter().add(commentType.createCopy(copyComments));
                }
            }
        }
        return temp;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("If[");
        sb.append("test=");
        sb.append(dumpThis(this.test));
        sb.append(", ");
        sb.append("body=");
        sb.append(dumpThis(this.body));
        sb.append(", ");
        sb.append("orelse=");
        sb.append(dumpThis(this.orelse));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitIf(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (test != null) {
            test.accept(visitor);
        }
        if (body != null) {
            for (int i = 0; i < body.length; i++) {
                if (body[i] != null) {
                    body[i].accept(visitor);
                }
            }
        }
        if (orelse != null) {
            orelse.accept(visitor);
        }
    }

}


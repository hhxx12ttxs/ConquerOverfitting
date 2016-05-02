package miui.app.screenelement.data;

import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import miui.app.screenelement.ScreenElementLoadException;
import miui.app.screenelement.util.IndexedNumberVariable;
import miui.app.screenelement.util.IndexedStringVariable;
import miui.app.screenelement.util.Utils;
import miui.app.screenelement.util.Variable;

public abstract class Expression
{
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "Expression";
    private static String[] mOperatorsPriority = arrayOfString;

    static
    {
        String[] arrayOfString = new String[2];
        arrayOfString[0] = "+-";
        arrayOfString[1] = "*/%";
    }

    public static Expression build(String paramString)
    {
        Object localObject2;
        if (TextUtils.isEmpty(paramString.trim()))
        {
            localObject2 = null;
            return localObject2;
        }
        Tokenizer localTokenizer = new Tokenizer(paramString);
        Object localObject1 = null;
        Stack localStack1 = new Stack();
        Stack localStack2 = new Stack();
        int i = 0;
        Expression.Tokenizer.Token localToken;
        while (true)
        {
            localToken = localTokenizer.getToken();
            if (localToken == null)
                break;
            switch (1.$SwitchMap$miui$app$screenelement$data$Expression$Tokenizer$TokenType[localToken.type.ordinal()])
            {
            default:
                label112: localObject1 = localToken;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            }
        }
        Expression localExpression2 = null;
        label168: Object localObject3;
        switch (1.$SwitchMap$miui$app$screenelement$data$Expression$Tokenizer$TokenType[localToken.type.ordinal()])
        {
        default:
            localObject3 = localExpression2;
            label172: if (i == 0)
                break;
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        }
        for (Object localObject4 = new UnaryExpression((Expression)localObject3, "-"); ; localObject4 = localObject3)
        {
            localStack2.push(localObject4);
            break label112;
            localObject3 = new NumberVariableExpression(localToken.token);
            break label172;
            localObject3 = new StringVariableExpression(localToken.token);
            break label172;
            StringBuilder localStringBuilder = new StringBuilder();
            if (i != 0);
            for (String str = "-"; ; str = "")
            {
                NumberExpression localNumberExpression = new NumberExpression(str + localToken.token);
                i = 0;
                localObject3 = localNumberExpression;
                break;
            }
            localObject3 = new StringExpression(localToken.token);
            break label172;
            localExpression2 = buildBracket(localToken, localStack1);
            if (localExpression2 != null)
                break label168;
            localObject2 = null;
            break;
            if ((localToken.token.equals("-")) && ((localObject1 == null) || (localObject1.type == Expression.Tokenizer.TokenType.OPE)))
            {
                i = 1;
                break label112;
            }
            do
            {
                Expression localExpression1 = (Expression)localStack2.pop();
                localStack2.push(new BinaryExpression((Expression)localStack2.pop(), localExpression1, ((Expression.Tokenizer.Token)localStack1.pop()).token));
                if ((localStack1.size() <= 0) || (cmpOpePri(localToken.token, ((Expression.Tokenizer.Token)localStack1.peek()).token) > 0))
                    break;
            }
            while (localStack2.size() >= 2);
            Log.e("Expression", "fail to buid: invalid operation position:" + paramString);
            localObject2 = null;
            break;
            localStack1.push(localToken);
            i = 0;
            break label112;
            localStack1.push(localToken);
            break label112;
            if (localStack2.size() != 1 + localStack1.size())
            {
                Log.e("Expression", "fail to buid: invalid expression:" + paramString);
                localObject2 = null;
                break;
            }
            for (localObject2 = (Expression)localStack2.pop(); localStack1.size() > 0; localObject2 = new BinaryExpression((Expression)localStack2.pop(), (Expression)localObject2, ((Expression.Tokenizer.Token)localStack1.pop()).token));
            break;
        }
    }

    private static Expression buildBracket(Expression.Tokenizer.Token paramToken, Stack<Expression.Tokenizer.Token> paramStack)
    {
        Expression[] arrayOfExpression = buildMultiple(paramToken.token);
        Object localObject;
        try
        {
            if ((!paramStack.isEmpty()) && (((Expression.Tokenizer.Token)paramStack.peek()).type == Expression.Tokenizer.TokenType.FUN))
                localObject = new FunctionExpression(arrayOfExpression, ((Expression.Tokenizer.Token)paramStack.pop()).token);
            else if (arrayOfExpression.length == 1)
                localObject = arrayOfExpression[0];
        }
        catch (ScreenElementLoadException localScreenElementLoadException)
        {
            localScreenElementLoadException.printStackTrace();
            Log.e("Expression", "fail to buid: multiple expressions in brackets, but seems no function presents:" + paramToken.token);
            localObject = null;
        }
        return localObject;
    }

    public static Expression[] buildMultiple(String paramString)
    {
        int i = 0;
        int j = 0;
        int k = 0;
        ArrayList localArrayList = new ArrayList();
        int m = 0;
        if (m < paramString.length())
        {
            int n = paramString.charAt(m);
            if (j == 0)
            {
                if ((n == 44) && (i == 0))
                {
                    localArrayList.add(build(paramString.substring(k, m)));
                    k = m + 1;
                }
            }
            else
                label71: if (n == 39)
                    if (j != 0)
                        break label116;
            label116: for (j = 1; ; j = 0)
            {
                m++;
                break;
                if (n == 40)
                {
                    i++;
                    break label71;
                }
                if (n != 41)
                    break label71;
                i--;
                break label71;
            }
        }
        localArrayList.add(build(paramString.substring(k)));
        return (Expression[])localArrayList.toArray(new Expression[localArrayList.size()]);
    }

    private static int cmpOpePri(String paramString1, String paramString2)
    {
        return getPriority(paramString1) - getPriority(paramString2);
    }

    private static int getPriority(String paramString)
    {
        int i = 0;
        if (i < mOperatorsPriority.length)
            if (mOperatorsPriority[i].indexOf(paramString) < 0);
        while (true)
        {
            return i;
            i++;
            break;
            i = -1;
        }
    }

    private static boolean isDigitChar(char paramChar)
    {
        if (((paramChar >= '0') && (paramChar <= '9')) || (paramChar == '.'));
        for (boolean bool = true; ; bool = false)
            return bool;
    }

    private static boolean isFunctionChar(char paramChar)
    {
        if (((paramChar >= 'a') && (paramChar <= 'z')) || ((paramChar >= 'A') && (paramChar <= 'Z')));
        for (boolean bool = true; ; bool = false)
            return bool;
    }

    private static boolean isVariableChar(char paramChar)
    {
        if (((paramChar >= 'a') && (paramChar <= 'z')) || ((paramChar >= 'A') && (paramChar <= 'Z')) || (paramChar == '_') || (paramChar == '.') || ((paramChar >= '0') && (paramChar <= '9')));
        for (boolean bool = true; ; bool = false)
            return bool;
    }

    public abstract double evaluate(Variables paramVariables);

    public String evaluateStr(Variables paramVariables)
    {
        return null;
    }

    public boolean isNull(Variables paramVariables)
    {
        return false;
    }

    private static class FunctionExpression extends Expression
    {
        private static HashMap<String, FunctionDesc> sFunMap = new HashMap();
        private Fun mFun;
        private Expression[] mParaExps;

        static
        {
            sFunMap.put("sin", new FunctionDesc(Fun.SIN, 1));
            sFunMap.put("cos", new FunctionDesc(Fun.COS, 1));
            sFunMap.put("tan", new FunctionDesc(Fun.TAN, 1));
            sFunMap.put("asin", new FunctionDesc(Fun.ASIN, 1));
            sFunMap.put("acos", new FunctionDesc(Fun.ACOS, 1));
            sFunMap.put("atan", new FunctionDesc(Fun.ATAN, 1));
            sFunMap.put("sinh", new FunctionDesc(Fun.SINH, 1));
            sFunMap.put("cosh", new FunctionDesc(Fun.COSH, 1));
            sFunMap.put("sqrt", new FunctionDesc(Fun.SQRT, 1));
            sFunMap.put("abs", new FunctionDesc(Fun.ABS, 1));
            sFunMap.put("len", new FunctionDesc(Fun.LEN, 1));
            sFunMap.put("round", new FunctionDesc(Fun.ROUND, 1));
            sFunMap.put("int", new FunctionDesc(Fun.INT, 1));
            sFunMap.put("isnull", new FunctionDesc(Fun.ISNULL, 1));
            sFunMap.put("not", new FunctionDesc(Fun.NOT, 1));
            sFunMap.put("min", new FunctionDesc(Fun.MIN, 2));
            sFunMap.put("max", new FunctionDesc(Fun.MAX, 2));
            sFunMap.put("digit", new FunctionDesc(Fun.DIGIT, 2));
            sFunMap.put("eq", new FunctionDesc(Fun.EQ, 2));
            sFunMap.put("ne", new FunctionDesc(Fun.NE, 2));
            sFunMap.put("ge", new FunctionDesc(Fun.GE, 2));
            sFunMap.put("gt", new FunctionDesc(Fun.GT, 2));
            sFunMap.put("le", new FunctionDesc(Fun.LE, 2));
            sFunMap.put("lt", new FunctionDesc(Fun.LT, 2));
            sFunMap.put("ifelse", new FunctionDesc(Fun.IFELSE, 3));
            sFunMap.put("eqs", new FunctionDesc(Fun.EQS, 2));
            sFunMap.put("substr", new FunctionDesc(Fun.SUBSTR, 2));
        }

        public FunctionExpression(Expression[] paramArrayOfExpression, String paramString)
            throws ScreenElementLoadException
        {
            this.mParaExps = paramArrayOfExpression;
            parseFunction(paramString);
        }

        private int digit(int paramInt1, int paramInt2)
        {
            int i = -1;
            if (paramInt2 <= 0);
            while (true)
            {
                return i;
                for (int j = 0; (paramInt1 > 0) && (j < paramInt2 - 1); j++)
                    paramInt1 /= 10;
                if (paramInt1 > 0)
                    i = paramInt1 % 10;
            }
        }

        private int len(int paramInt)
        {
            for (int i = 1; paramInt >= 10; i++)
                paramInt /= 10;
            return i;
        }

        private void parseFunction(String paramString)
            throws ScreenElementLoadException
        {
            boolean bool1 = true;
            FunctionDesc localFunctionDesc = (FunctionDesc)sFunMap.get(paramString);
            boolean bool2;
            if (localFunctionDesc != null)
            {
                bool2 = bool1;
                Utils.asserts(bool2, "invalid function:" + paramString);
                this.mFun = localFunctionDesc.fun;
                if (this.mParaExps.length < localFunctionDesc.params)
                    break label94;
            }
            while (true)
            {
                Utils.asserts(bool1, "parameters count not matching for function: " + paramString);
                return;
                bool2 = false;
                break;
                label94: bool1 = false;
            }
        }

        public double evaluate(Variables paramVariables)
        {
            double d1 = 1.0D;
            double d2 = 0.0D;
            double d3 = this.mParaExps[0].evaluate(paramVariables);
            switch (Expression.1.$SwitchMap$miui$app$screenelement$data$Expression$FunctionExpression$Fun[this.mFun.ordinal()])
            {
            default:
                Log.e("Expression", "fail to evalute FunctionExpression, invalid function: " + this.mFun.toString());
                d1 = d2;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            }
            while (true)
            {
                return d1;
                d1 = Math.sin(d3);
                continue;
                d1 = Math.cos(d3);
                continue;
                d1 = Math.tan(d3);
                continue;
                d1 = Math.asin(d3);
                continue;
                d1 = Math.acos(d3);
                continue;
                d1 = Math.atan(d3);
                continue;
                d1 = Math.sinh(d3);
                continue;
                d1 = Math.cosh(d3);
                continue;
                d1 = Math.sqrt(d3);
                continue;
                d1 = Math.abs(d3);
                continue;
                d1 = this.mParaExps[0].evaluateStr(paramVariables).length();
                continue;
                d1 = Math.round(d3);
                continue;
                d1 = (int)d3;
                continue;
                if (!this.mParaExps[0].isNull(paramVariables))
                {
                    d1 = d2;
                    continue;
                    if (d3 > d2);
                    while (true)
                    {
                        d1 = d2;
                        break;
                        d2 = d1;
                    }
                    d1 = Math.min(d3, this.mParaExps[1].evaluate(paramVariables));
                    continue;
                    d1 = Math.max(d3, this.mParaExps[1].evaluate(paramVariables));
                    continue;
                    d1 = digit((int)d3, (int)this.mParaExps[1].evaluate(paramVariables));
                    continue;
                    if (d3 != this.mParaExps[1].evaluate(paramVariables))
                    {
                        d1 = d2;
                        continue;
                        if (d3 == this.mParaExps[1].evaluate(paramVariables))
                        {
                            d1 = d2;
                            continue;
                            if (d3 < this.mParaExps[1].evaluate(paramVariables))
                            {
                                d1 = d2;
                                continue;
                                if (d3 <= this.mParaExps[1].evaluate(paramVariables))
                                {
                                    d1 = d2;
                                    continue;
                                    if (d3 > this.mParaExps[1].evaluate(paramVariables))
                                    {
                                        d1 = d2;
                                        continue;
                                        if (d3 >= this.mParaExps[1].evaluate(paramVariables))
                                        {
                                            d1 = d2;
                                            continue;
                                            int i = this.mParaExps.length;
                                            if (i % 2 != 1)
                                            {
                                                Log.e("Expression", "function parameter number should be 2*n+1: " + this.mFun.toString());
                                                d1 = d2;
                                            }
                                            else
                                            {
                                                for (int j = 0; ; j++)
                                                {
                                                    if (j >= (i - 1) / 2)
                                                        break label657;
                                                    if (this.mParaExps[(j * 2)].evaluate(paramVariables) > d2)
                                                    {
                                                        d1 = this.mParaExps[(1 + j * 2)].evaluate(paramVariables);
                                                        break;
                                                    }
                                                }
                                                label657: d1 = this.mParaExps[(i - 1)].evaluate(paramVariables);
                                                continue;
                                                if (!TextUtils.equals(this.mParaExps[0].evaluateStr(paramVariables), this.mParaExps[1].evaluateStr(paramVariables)))
                                                {
                                                    d1 = d2;
                                                    continue;
                                                    d1 = Utils.stringToDouble(evaluateStr(paramVariables), d2);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        public String evaluateStr(Variables paramVariables)
        {
            Object localObject = null;
            switch (Expression.1.$SwitchMap$miui$app$screenelement$data$Expression$FunctionExpression$Fun[this.mFun.ordinal()])
            {
            case 26:
            default:
                localObject = Utils.doubleToString(evaluate(paramVariables));
            case 25:
            case 27:
            }
            while (true)
            {
                return localObject;
                int k = this.mParaExps.length;
                if (k % 2 != 1)
                {
                    Log.e("Expression", "function parameter number should be 2*n+1: " + this.mFun.toString());
                }
                else
                {
                    for (int m = 0; ; m++)
                    {
                        if (m >= (k - 1) / 2)
                            break label158;
                        if (this.mParaExps[(m * 2)].evaluate(paramVariables) > 0.0D)
                        {
                            localObject = this.mParaExps[(1 + m * 2)].evaluateStr(paramVariables);
                            break;
                        }
                    }
                    label158: localObject = this.mParaExps[(k - 1)].evaluateStr(paramVariables);
                    continue;
                    String str1 = this.mParaExps[0].evaluateStr(paramVariables);
                    if (str1 != null)
                    {
                        int i = this.mParaExps.length;
                        int j = (int)this.mParaExps[1].evaluate(paramVariables);
                        if (i >= 3);
                        try
                        {
                            localObject = str1.substring(j, j + (int)this.mParaExps[2].evaluate(paramVariables));
                            continue;
                            String str2 = str1.substring(j);
                            localObject = str2;
                        }
                        catch (IndexOutOfBoundsException localIndexOutOfBoundsException)
                        {
                        }
                    }
                }
            }
        }

        private static class FunctionDesc
        {
            Expression.FunctionExpression.Fun fun;
            int params;

            public FunctionDesc(Expression.FunctionExpression.Fun paramFun, int paramInt)
            {
                this.fun = paramFun;
                this.params = paramInt;
            }
        }

        private static enum Fun
        {
            static
            {
                COS = new Fun("COS", 2);
                TAN = new Fun("TAN", 3);
                ASIN = new Fun("ASIN", 4);
                ACOS = new Fun("ACOS", 5);
                ATAN = new Fun("ATAN", 6);
                SINH = new Fun("SINH", 7);
                COSH = new Fun("COSH", 8);
                SQRT = new Fun("SQRT", 9);
                ABS = new Fun("ABS", 10);
                LEN = new Fun("LEN", 11);
                ROUND = new Fun("ROUND", 12);
                INT = new Fun("INT", 13);
                MIN = new Fun("MIN", 14);
                MAX = new Fun("MAX", 15);
                DIGIT = new Fun("DIGIT", 16);
                EQ = new Fun("EQ", 17);
                NE = new Fun("NE", 18);
                GE = new Fun("GE", 19);
                GT = new Fun("GT", 20);
                LE = new Fun("LE", 21);
                LT = new Fun("LT", 22);
                ISNULL = new Fun("ISNULL", 23);
                NOT = new Fun("NOT", 24);
                IFELSE = new Fun("IFELSE", 25);
                EQS = new Fun("EQS", 26);
                SUBSTR = new Fun("SUBSTR", 27);
                Fun[] arrayOfFun = new Fun[28];
                arrayOfFun[0] = INVALID;
                arrayOfFun[1] = SIN;
                arrayOfFun[2] = COS;
                arrayOfFun[3] = TAN;
                arrayOfFun[4] = ASIN;
                arrayOfFun[5] = ACOS;
                arrayOfFun[6] = ATAN;
                arrayOfFun[7] = SINH;
                arrayOfFun[8] = COSH;
                arrayOfFun[9] = SQRT;
                arrayOfFun[10] = ABS;
                arrayOfFun[11] = LEN;
                arrayOfFun[12] = ROUND;
                arrayOfFun[13] = INT;
                arrayOfFun[14] = MIN;
                arrayOfFun[15] = MAX;
                arrayOfFun[16] = DIGIT;
                arrayOfFun[17] = EQ;
                arrayOfFun[18] = NE;
                arrayOfFun[19] = GE;
                arrayOfFun[20] = GT;
                arrayOfFun[21] = LE;
                arrayOfFun[22] = LT;
                arrayOfFun[23] = ISNULL;
                arrayOfFun[24] = NOT;
                arrayOfFun[25] = IFELSE;
                arrayOfFun[26] = EQS;
                arrayOfFun[27] = SUBSTR;
            }
        }
    }

    private static class BinaryExpression extends Expression
    {
        private Expression mExp1;
        private Expression mExp2;
        private Ope mOpe = Ope.INVALID;

        public BinaryExpression(Expression paramExpression1, Expression paramExpression2, String paramString)
        {
            this.mExp1 = paramExpression1;
            this.mExp2 = paramExpression2;
            this.mOpe = parseOperator(paramString);
            if (this.mOpe == Ope.INVALID)
                Log.e("Expression", "BinaryExpression: invalid operator:" + paramString);
        }

        public static Ope parseOperator(String paramString)
        {
            Ope localOpe;
            if (paramString.equals("+"))
                localOpe = Ope.ADD;
            while (true)
            {
                return localOpe;
                if (paramString.equals("-"))
                    localOpe = Ope.MIN;
                else if (paramString.equals("*"))
                    localOpe = Ope.MUL;
                else if (paramString.equals("/"))
                    localOpe = Ope.DIV;
                else if (paramString.equals("%"))
                    localOpe = Ope.MOD;
                else
                    localOpe = Ope.INVALID;
            }
        }

        public double evaluate(Variables paramVariables)
        {
            double d;
            switch (Expression.1.$SwitchMap$miui$app$screenelement$data$Expression$BinaryExpression$Ope[this.mOpe.ordinal()])
            {
            default:
                Log.e("Expression", "fail to evalute BinaryExpression, invalid operator");
                d = 0.0D;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            }
            while (true)
            {
                return d;
                d = this.mExp1.evaluate(paramVariables) + this.mExp2.evaluate(paramVariables);
                continue;
                d = this.mExp1.evaluate(paramVariables) - this.mExp2.evaluate(paramVariables);
                continue;
                d = this.mExp1.evaluate(paramVariables) * this.mExp2.evaluate(paramVariables);
                continue;
                d = this.mExp1.evaluate(paramVariables) / this.mExp2.evaluate(paramVariables);
                continue;
                d = this.mExp1.evaluate(paramVariables) % this.mExp2.evaluate(paramVariables);
            }
        }

        public String evaluateStr(Variables paramVariables)
        {
            String str1 = this.mExp1.evaluateStr(paramVariables);
            String str2 = this.mExp2.evaluateStr(paramVariables);
            switch (Expression.1.$SwitchMap$miui$app$screenelement$data$Expression$BinaryExpression$Ope[this.mOpe.ordinal()])
            {
            default:
                Log.e("Expression", "fail to evalute string BinaryExpression, invalid operator");
                str2 = null;
            case 1:
            }
            while (true)
            {
                return str2;
                if ((str1 == null) && (str2 == null))
                    str2 = null;
                else if (str1 != null)
                    if (str2 == null)
                        str2 = str1;
                    else
                        str2 = str1 + str2;
            }
        }

        public boolean isNull(Variables paramVariables)
        {
            boolean bool1 = false;
            boolean bool2 = true;
            switch (Expression.1.$SwitchMap$miui$app$screenelement$data$Expression$BinaryExpression$Ope[this.mOpe.ordinal()])
            {
            default:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            }
            while (true)
            {
                return bool2;
                if ((!this.mExp1.isNull(paramVariables)) || (!this.mExp2.isNull(paramVariables)))
                {
                    bool2 = false;
                    continue;
                    if ((this.mExp1.isNull(paramVariables)) || (this.mExp2.isNull(paramVariables)))
                        bool1 = bool2;
                    bool2 = bool1;
                }
            }
        }

        public static enum Ope
        {
            static
            {
                ADD = new Ope("ADD", 1);
                MIN = new Ope("MIN", 2);
                MUL = new Ope("MUL", 3);
                DIV = new Ope("DIV", 4);
                MOD = new Ope("MOD", 5);
                Ope[] arrayOfOpe = new Ope[6];
                arrayOfOpe[0] = INVALID;
                arrayOfOpe[1] = ADD;
                arrayOfOpe[2] = MIN;
                arrayOfOpe[3] = MUL;
                arrayOfOpe[4] = DIV;
                arrayOfOpe[5] = MOD;
            }
        }
    }

    private static class UnaryExpression extends Expression
    {
        private Expression mExp;
        private Ope mOpe = Ope.INVALID;

        public UnaryExpression(Expression paramExpression, String paramString)
        {
            this.mExp = paramExpression;
            this.mOpe = parseOperator(paramString);
            if (this.mOpe == Ope.INVALID)
                Log.e("Expression", "BinaryExpression: invalid operator:" + paramString);
        }

        public static Ope parseOperator(String paramString)
        {
            if (paramString.equals("-"));
            for (Ope localOpe = Ope.MIN; ; localOpe = Ope.INVALID)
                return localOpe;
        }

        public double evaluate(Variables paramVariables)
        {
            switch (Expression.1.$SwitchMap$miui$app$screenelement$data$Expression$UnaryExpression$Ope[this.mOpe.ordinal()])
            {
            default:
                Log.e("Expression", "fail to evalute UnaryExpression, invalid operator");
            case 1:
            }
            for (double d = this.mExp.evaluate(paramVariables); ; d = 0.0D - this.mExp.evaluate(paramVariables))
                return d;
        }

        public String evaluateStr(Variables paramVariables)
        {
            return Utils.doubleToString(evaluate(paramVariables));
        }

        public boolean isNull(Variables paramVariables)
        {
            return this.mExp.isNull(paramVariables);
        }

        public static enum Ope
        {
            static
            {
                Ope[] arrayOfOpe = new Ope[2];
                arrayOfOpe[0] = INVALID;
                arrayOfOpe[1] = MIN;
            }
        }
    }

    private static class StringExpression extends Expression
    {
        private String mValue;

        public StringExpression(String paramString)
        {
            this.mValue = paramString;
        }

        public double evaluate(Variables paramVariables)
        {
            try
            {
                double d2 = Double.valueOf(Double.parseDouble(this.mValue)).doubleValue();
                d1 = d2;
                return d1;
            }
            catch (NumberFormatException localNumberFormatException)
            {
                while (true)
                    double d1 = 0.0D;
            }
        }

        public String evaluateStr(Variables paramVariables)
        {
            return this.mValue;
        }
    }

    private static class NumberExpression extends Expression
    {
        private String mString;
        private double mValue;

        public NumberExpression(String paramString)
        {
            try
            {
                this.mValue = Double.parseDouble(paramString);
                return;
            }
            catch (NumberFormatException localNumberFormatException)
            {
                while (true)
                {
                    Log.e("Expression", "invalid NumberExpression:" + paramString);
                    localNumberFormatException.printStackTrace();
                }
            }
        }

        public double evaluate(Variables paramVariables)
        {
            return this.mValue;
        }

        public String evaluateStr(Variables paramVariables)
        {
            if (this.mString == null)
                this.mString = Utils.doubleToString(this.mValue);
            return this.mString;
        }
    }

    private static class StringVariableExpression extends Expression.VariableExpression
    {
        private IndexedStringVariable mIndexedVar;

        public StringVariableExpression(String paramString)
        {
            super();
        }

        private void ensureVar(Variables paramVariables)
        {
            if (this.mIndexedVar == null)
                this.mIndexedVar = new IndexedStringVariable(this.mVar.getObjName(), this.mVar.getPropertyName(), paramVariables);
        }

        public double evaluate(Variables paramVariables)
        {
            double d1 = 0.0D;
            String str = evaluateStr(paramVariables);
            if (str == null);
            while (true)
            {
                return d1;
                try
                {
                    double d2 = Double.valueOf(Double.parseDouble(str)).doubleValue();
                    d1 = d2;
                }
                catch (NumberFormatException localNumberFormatException)
                {
                }
            }
        }

        public String evaluateStr(Variables paramVariables)
        {
            ensureVar(paramVariables);
            return this.mIndexedVar.get();
        }

        public boolean isNull(Variables paramVariables)
        {
            ensureVar(paramVariables);
            if (this.mIndexedVar.get() == null);
            for (boolean bool = true; ; bool = false)
                return bool;
        }
    }

    private static class NumberVariableExpression extends Expression.VariableExpression
    {
        private IndexedNumberVariable mIndexedVar;

        public NumberVariableExpression(String paramString)
        {
            super();
        }

        private void ensureVar(Variables paramVariables)
        {
            if (this.mIndexedVar == null)
                this.mIndexedVar = new IndexedNumberVariable(this.mVar.getObjName(), this.mVar.getPropertyName(), paramVariables);
        }

        public double evaluate(Variables paramVariables)
        {
            ensureVar(paramVariables);
            Double localDouble = this.mIndexedVar.get();
            if (localDouble == null);
            for (double d = 0.0D; ; d = localDouble.doubleValue())
                return d;
        }

        public String evaluateStr(Variables paramVariables)
        {
            return Utils.doubleToString(evaluate(paramVariables));
        }

        public boolean isNull(Variables paramVariables)
        {
            ensureVar(paramVariables);
            if (this.mIndexedVar.get() == null);
            for (boolean bool = true; ; bool = false)
                return bool;
        }
    }

    private static abstract class VariableExpression extends Expression
    {
        protected Variable mVar;

        public VariableExpression(String paramString)
        {
            this.mVar = new Variable(paramString);
        }
    }

    private static class Tokenizer
    {
        private int mPos;
        private String mString;

        public Tokenizer(String paramString)
        {
            this.mString = paramString;
            reset();
        }

        public Token getToken()
        {
            Token localToken = null;
            int i = 0;
            int j = -1;
            int k = this.mPos;
            char c;
            int m;
            if (k < this.mString.length())
            {
                c = this.mString.charAt(k);
                if (i == 0)
                    if ((c == '#') || (c == '@'))
                    {
                        m = k + 1;
                        label60: if ((m >= this.mString.length()) || (!Expression.isVariableChar(this.mString.charAt(m))))
                        {
                            if (m != k + 1)
                                break label132;
                            Log.e("Expression", "invalid variable name:" + this.mString);
                        }
                    }
            }
            while (true)
            {
                return localToken;
                m++;
                break label60;
                label132: this.mPos = m;
                if (c == '#');
                for (TokenType localTokenType = TokenType.VAR; ; localTokenType = TokenType.VARSTR)
                {
                    localToken = new Token(localTokenType, this.mString.substring(k + 1, m));
                    break;
                }
                if (Expression.isDigitChar(c))
                    for (int i4 = k + 1; ; i4++)
                        if ((i4 >= this.mString.length()) || (!Expression.isDigitChar(this.mString.charAt(i4))))
                        {
                            this.mPos = i4;
                            localToken = new Token(TokenType.NUM, this.mString.substring(k, i4));
                            break;
                        }
                if (Expression.isFunctionChar(c))
                    for (int i3 = k + 1; ; i3++)
                        if ((i3 >= this.mString.length()) || (!Expression.isFunctionChar(this.mString.charAt(i3))))
                        {
                            this.mPos = i3;
                            localToken = new Token(TokenType.FUN, this.mString.substring(k, i3));
                            break;
                        }
                if (Expression.BinaryExpression.parseOperator(String.valueOf(c)) != Expression.BinaryExpression.Ope.INVALID)
                {
                    this.mPos = (k + 1);
                    localToken = new Token(TokenType.OPE, String.valueOf(c));
                }
                else if (c == '\'')
                {
                    int n = 0;
                    int i1 = k + 1;
                    int i2;
                    if (i1 < this.mString.length())
                    {
                        i2 = this.mString.charAt(i1);
                        if ((n != 0) || (i2 != 39));
                    }
                    else
                    {
                        this.mPos = (i1 + 1);
                        localToken = new Token(TokenType.STR, this.mString.substring(k + 1, i1).replace("\\'", "'"));
                        continue;
                    }
                    if (i2 == 92);
                    for (n = 1; ; n = 0)
                    {
                        i1++;
                        break;
                    }
                }
                else
                {
                    if (c == '(')
                    {
                        if (i == 0)
                            j = k + 1;
                        i++;
                    }
                    do
                    {
                        do
                        {
                            k++;
                            break;
                        }
                        while (c != ')');
                        i--;
                    }
                    while (i != 0);
                    this.mPos = (k + 1);
                    localToken = new Token(TokenType.BRACKET, this.mString.substring(j, k));
                    continue;
                    if (i != 0)
                        Log.e("Expression", "mismatched bracket:" + this.mString);
                }
            }
        }

        public void reset()
        {
            this.mPos = 0;
        }

        public class Token
        {
            public String token;
            public Expression.Tokenizer.TokenType type = Expression.Tokenizer.TokenType.INVALID;

            public Token(Expression.Tokenizer.TokenType paramString, String arg3)
            {
                this.type = paramString;
                Object localObject;
                this.token = localObject;
            }
        }

        public static enum TokenType
        {
            static
            {
                NUM = new TokenType("NUM", 3);
                STR = new TokenType("STR", 4);
                OPE = new TokenType("OPE", 5);
                FUN = new TokenType("FUN", 6);
                BRACKET = new TokenType("BRACKET", 7);
                TokenType[] arrayOfTokenType = new TokenType[8];
                arrayOfTokenType[0] = INVALID;
                arrayOfTokenType[1] = VAR;
                arrayOfTokenType[2] = VARSTR;
                arrayOfTokenType[3] = NUM;
                arrayOfTokenType[4] = STR;
                arrayOfTokenType[5] = OPE;
                arrayOfTokenType[6] = FUN;
                arrayOfTokenType[7] = BRACKET;
            }
        }
    }
}

/* Location:                     /home/lithium/miui/chameleon/2.11.16/android.policy_dex2jar.jar
 * Qualified Name:         miui.app.screenelement.data.Expression
 * JD-Core Version:        0.6.2
 */

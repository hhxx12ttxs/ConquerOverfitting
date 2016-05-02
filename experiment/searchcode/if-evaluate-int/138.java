package parser;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class Interpreter extends JTextArea{

ArrayList<myToken> token = new ArrayList<myToken>();
ArrayList<Object> tempToken = new ArrayList<Object>();
ArrayList<myVariable> myVar = new ArrayList<myVariable>();
ArrayList<myFunction> myFunc = new ArrayList<myFunction>();
ArrayList<String> Scope = new ArrayList<String>();
ArrayList<returnFunc> returning = new ArrayList<returnFunc>();
private String tracking="";
private int trackLines=0;
private String readVal;

public String getTracking()
{
    return tracking;
}

public int getTrackLines()
{
    return trackLines;
}


public ArrayList<myVariable> getVars()
{
    return myVar;
}

public void add(ArrayList<String> temp)
{
    tempToken.addAll(temp);
}

public boolean isReturnType(String str)
{
    if(str.equals("void") || str.equals("char") || str.equals("int") || str.equals("float") || str.equals("string"))
        return true;
    return false;
}

public boolean checkDuplicateFunc(String name)
{
    for(int i=0; i<myFunc.size(); i++)
        if(myFunc.get(i).getName().equals(name))
            return false;
    return true;
}

public boolean checkDuplicateVar(String name)
{
    for(int i=0; i<myVar.size(); i++)
        if(myVar.get(i).getVarName().equals(name) && (myVar.get(i).getScope().equals(Scope.get(Scope.size()-1)) || myVar.get(i).getScope().equals("0")))
        {
            return false;
        }
    return true;
}

    @SuppressWarnings("empty-statement")
public void start()
{   System.out.println("=======Start Interpreter============");
    //System.out.println("Interpreter tokens:"+tempToken);
    tracking+="Start Interpreter\n";
    trackLines++;
    //System.out.println("Start Interpreter");
    int j=0;
    int parenCount=0;
    Scope.add("0");
    while(tempToken.get(j).toString().equals("#define"))
    {
        j++;
        if(tempToken.get(j).toString().equals("int") || tempToken.get(j).toString().equals("String") ||
            tempToken.get(j).toString().equals("char") ||tempToken.get(j).toString().equals("float"))
        {
            j= varDec(j, tempToken.get(j).toString(), tempToken);
        }
        j++;
    }
    Scope.remove(0);
    while(!tempToken.get(j).toString().equals("main()") && j < tempToken.size())
    {
        if(j > 0 && isReturnType(tempToken.get(j-1).toString()) && tempToken.get(j+1).toString().equals("("))
        {
            myFunction f = new myFunction();
            if(tempToken.get(j-1).toString().equals("int"))
                f.setReturnType(1);
            else if(tempToken.get(j-1).toString().equals("char"))
                f.setReturnType(2);
            else if(tempToken.get(j-1).toString().equals("float"))
                f.setReturnType(3);
            else if(tempToken.get(j-1).toString().equals("string"))
                f.setReturnType(4);
            else if(tempToken.get(j-1).toString().equals("void"))
                f.setReturnType(0);
            
            f.setName(tempToken.get(j).toString());
            j+=2;
            while(!tempToken.get(j).toString().equals(")"))
            {
                if(tempToken.get(j).toString().equals("int"))
                {
                    f.addArgs(1);
                    j++;
                    f.addVar(tempToken.get(j).toString());
                }
                else if(tempToken.get(j).toString().equals("char"))
                {
                    f.addArgs(2);
                    j++;
                    f.addVar(tempToken.get(j).toString());
                }
                else if(tempToken.get(j).toString().equals("float"))
                {
                    f.addArgs(3);
                    j++;
                    f.addVar(tempToken.get(j).toString());
                }
                else if(tempToken.get(j).toString().equals("String"))
                {
                    f.addArgs(4);
                    j++;
                    f.addVar(tempToken.get(j).toString());
                }
                j++;
            }
            j+=2;
            boolean bParen=true;
            while(bParen)
            {
                f.addTokens(tempToken.get(j).toString());
                if(tempToken.get(j).toString().equals("{"))
                    parenCount++;
                else if(tempToken.get(j).toString().equals("}"))
                    parenCount--;
                j++;
                if(tempToken.get(j).toString().equals("}") && parenCount==0)
                    bParen=false;
            }
            if(checkDuplicateFunc(f.getName()))
                myFunc.add(f);
            else
            {
                tracking+="Function " + f.getName() + " has already been declared once";
                trackLines++;
                //System.out.println("Function " + f.getName() + " has already been declared once");
            }
        }
        else if(j > 0 && isReturnType(tempToken.get(j-1).toString()) && tempToken.get(j+1).toString().equals("()"))
        {
            myFunction f = new myFunction();
            f.setName(tempToken.get(j).toString());
            j+=3;
            boolean bParen=true;
            while(bParen)
            {
                f.addTokens(tempToken.get(j).toString());
                if(tempToken.get(j).toString().equals("{"))
                    parenCount++;
                else if(tempToken.get(j).toString().equals("}"))
                    parenCount--;
                j++;
                if(tempToken.get(j).toString().equals("}") && parenCount==0)
                    bParen=false;
            }
            if(checkDuplicateFunc(f.getName()))
                myFunc.add(f);
            else
            {
                tracking+="Function " + f.getName() + " has already been declared once";
                trackLines++;
                //System.out.println("Function " + f.getName() + " has already been declared once");
            }
        }
        j++;
    }
    j++;
    Scope.add("main");
for(int i=j; i<tempToken.size(); i++)
{
    if(tempToken.get(i).toString().equals("print"))
    {
       i= doPrint(i, tempToken);
    }
    else if(tempToken.get(i).toString().equals("int") || tempToken.get(i).toString().equals("String") ||
            tempToken.get(i).toString().equals("char") ||tempToken.get(i).toString().equals("float"))
    {
        i= varDec(i, tempToken.get(i).toString(), tempToken);
    }

    //for if statements
    else if(tempToken.get(i).toString().equals("if"))
    {
//        ArrayList<Object> ifTokens = new ArrayList<Object>();
//
//        for(int x=i+1; (!tempToken.get(x).toString().equals("{") &&
//                !tempToken.get(x).toString().equals(";")); x++)
//        {
//            ifTokens.add(tempToken.get(x));
//        }
//        ifTokens.add("end");
//        System.out.println(ifTokens);
        i = evalIF(i, tempToken);

    }

    else if(tempToken.get(i).toString().equals("else"))
    {//ignore else
        System.out.println("HERE");
        for(int y=i; !tempToken.get(y).toString().equals("}");y++)
            i=y;

        i++;
    }
    else if(tempToken.get(i).toString().equals("="))
    {
        i = evalAssignment(tempToken, i);
    }
    else if(tempToken.get(i).toString().equals("()") || tempToken.get(i).toString().equals("("))
    {
        i = functionCall(tempToken, i);
        //System.out.println("after function: " + tempToken.get(i));
    }
    else if(tempToken.get(i).toString().equals("while"))
    {
        //System.out.println("ewanewanewanewanewan");
        i = whileCall(tempToken, i);
    }
    else if(tempToken.get(i).toString().equals("for"))
    {
        i = forCall(tempToken, i);
    }
    else if(tempToken.get(i).toString().equals("do"))
    {
        i = doCall(tempToken, i);
    }
    else if(tempToken.get(i).toString().equals("scanINT"))
    {
        i=scanInt(i, tempToken);
    }
    else if(tempToken.get(i).toString().equals("scanFLOAT"))
    {
        i=scanFloat(i, tempToken);
    }
    else if(tempToken.get(i).toString().equals("scanCHAR"))
    {
        i=scanCHAR(i, tempToken);
    }
    else if(tempToken.get(i).toString().equals("scanSTRING"))
    {
        i=scanSTRING(i, tempToken);
    }
}
    //System.out.println("done: " + myFunc.size());
//    System.out.println("\n");
//    for(int i=0; i<myVar.size(); i++)
//    {
//        System.out.println("VARS: " + myVar.get(i).getVarName());
//    }//*/
    /*for(int i=0; i<myFunc.size(); i++)
        System.out.println("Functions: " + myFunc.get(i).getName());//*/

tracking+="\n";
trackLines++;
System.out.print("\n");
}

public int doCall(ArrayList<Object> tokens, int index)
{
    index++;
    ArrayList<String> doTokens = new ArrayList<String>();
    ArrayList<String> boolExp = new ArrayList<String>();
    if(tokens.get(index).toString().equals("{"))
    {
        index++;
        int nParen=0;
        boolean bParen=true;
        while(bParen)
        {
            if(tokens.get(index).toString().equals("{"))
                nParen++;
            else if(tokens.get(index).toString().equals("}"))
                nParen--;
            doTokens.add(tokens.get(index).toString());
            index++;
            if(tokens.get(index).toString().equals("}") && nParen==0)
                bParen = false;
        }
    }
    else
    {
        while(!tokens.get(index-1).toString().equals(";"))
        {
            doTokens.add(tokens.get(index).toString());
            index++;
        }
    }
    index+=3;
    while(!tokens.get(index).toString().equals(")"))
    {
        boolExp.add(tokens.get(index).toString());
        index++;
    }
    System.out.println("");
    evalDoWhile(doTokens, boolExp);
    index++;
    return index;
}

public int forCall(ArrayList<Object> tokens, int index)
{
    //System.out.println("forcall: " + tokens);
    index+=2;
    ArrayList<String> Assign= new ArrayList<String>();
    ArrayList<String> forAssign= new ArrayList<String>();
    ArrayList<String> forTokens= new ArrayList<String>();
    ArrayList<String> boolExp = new ArrayList<String>();
    while(!tokens.get(index).toString().equals(";"))
    {
        Assign.add(tokens.get(index).toString());
        index++;
    }
    index++;
    while(!tokens.get(index).toString().equals(";"))
    {
        boolExp.add(tokens.get(index).toString());
        index++;
    }
    index++;
    while(!tokens.get(index).toString().equals(")"))
    {
        forAssign.add(tokens.get(index).toString());
        index++;
    }
    index++;
    if(tokens.get(index).toString().equals("{"))
    {
        index++;
        int nParen=0;
        boolean bParen=true;
        while(bParen)
        {
            if(tokens.get(index).toString().equals("{"))
                nParen++;
            else if(tokens.get(index).toString().equals("}"))
                nParen--;
            forTokens.add(tokens.get(index).toString());
            index++;
            if(tokens.get(index).toString().equals("}") && nParen==0)
                bParen = false;
        }
    }
    else
    {
        while(!tokens.get(index-1).toString().equals(";"))
        {
            forTokens.add(tokens.get(index).toString());
            index++;
        }
    }
    //System.out.println("forTokens: " + forTokens);
    evalFor(forTokens, boolExp, Assign, forAssign);
    return index;
}

public int whileCall(ArrayList<Object> tokens, int index)
{
    index+=2;
    ArrayList<String> bExp = new ArrayList<String>();
    while(!tokens.get(index).toString().equals(")")) //boolean expression
    {
        bExp.add(tokens.get(index).toString());
        index++;
    }
    index++;
    ArrayList<String> whileToken = new ArrayList<String>();
    if(tokens.get(index).toString().equals("{")) //tokens inside of while
    {
        index++;
        int nParen=0;
        boolean bParen=true;
        while(bParen)
        {
            if(tokens.get(index).toString().equals("{"))
                nParen++;
            else if(tokens.get(index).toString().equals("}"))
                nParen--;
            whileToken.add(tokens.get(index).toString());
            index++;
            if(tokens.get(index).toString().equals("}") && nParen==0)
                bParen = false;
        }
    }
    else
    {
        while(!tokens.get(index-1).toString().equals(";"))//para kasama yng ';' sa whileToken
        {
            whileToken.add(tokens.get(index).toString());
            index++;
        }
    }
    //System.out.println("whileToken: " + whileToken);
    evalWhile(whileToken, bExp);
    return index;
}

public int functionCall(ArrayList<Object> tokens, int index)
{
    String funcName = tokens.get(index-1).toString();
    if(checkFunctions(funcName))
    {
        Scope.add(funcName);
        myFunction func = new myFunction();
        int k=0;
        while(!myFunc.get(k).getName().equals(funcName))
            k++;
        func=myFunc.get(k);
        returnFunc rf = new returnFunc();
        rf.setRType(func.getReturnType());
        returning.add(rf);
        ArrayList<Object>paramValues=new ArrayList<Object>();
        if(tokens.get(index).toString().equals("()"))
        {
            evalFunction(func, paramValues);
        }
        else if(tokens.get(index).toString().equals("("))
        {
            index++;
            boolean bEnd=false;
            while(!tokens.get(index-1).toString().equals(")"))
            {
                paramValues.add(tokens.get(index));
                index++;
            }
            //System.out.println("evaluating parameters: " + paramValues.toString());
            evalFunction(func, paramValues);
        }
    }
    Scope.remove(Scope.size()-1);
    return index;
}

public void evalWhile(ArrayList<String> whileToken, ArrayList<String> boolExp)
{
    boolExp.add(0, "(");
    boolExp.add(0, "while");
    boolExp.add(")");
    boolExp.add("{");
    while(evalLoop((ArrayList<Object>) boolExp.clone()))
    {
        evalStatements((ArrayList<Object>) whileToken.clone());
    }
}

public void evalDoWhile(ArrayList<String> doWhileToken, ArrayList<String> boolExp)
{
    boolExp.add(0, "(");
    boolExp.add(0, "while");
    boolExp.add(")");
    boolExp.add("{");
    evalStatements((ArrayList<Object>) doWhileToken.clone());
    System.out.println("bboooooooooool: " + boolExp);
    while(evalLoop((ArrayList<Object>) boolExp.clone()))
    {
        evalStatements((ArrayList<Object>) doWhileToken.clone());
    }
}

public void evalFor(ArrayList<String> forToken, ArrayList<String> boolExp, ArrayList<String> assignment, ArrayList<String> finAssignment)
{
    boolExp.add(0, "(");
    boolExp.add(0, "for");
    boolExp.add(")");
    boolExp.add("{");
    assignment.add(";");
    finAssignment.add(";");
    evalAssignment((ArrayList<Object>) assignment.clone(), 1);
    while(evalLoop((ArrayList<Object>) boolExp.clone()))
    {
        evalStatements((ArrayList<Object>) forToken.clone());
        evalStatements((ArrayList<Object>) finAssignment.clone());
    }
}

public int evalAssignment(ArrayList<Object> assignToken, int index)
{
    if(checkVar(assignToken.get(index-1).toString()))
    {
    String varName = assignToken.get(index-1).toString();
    int varIndex=-1, varType=-1;

    varIndex =getVarIndex(varName, Scope.get(Scope.size()-1));
    varType=varTypeTemp;
    index++;
    if(varType==1)//int
    {
        ArrayList<String>MathToken = new ArrayList<String>();
        while(!assignToken.get(index).toString().equals(";"))
        {
            MathToken.add(assignToken.get(index).toString());
            index++;
        }

        myVar.get(varIndex).setValue((int)evalExpression(MathToken));

    }
    else if(varType==2)//char
    {
       if(assignToken.get(index).toString().charAt(0) == '\'')
           myVar.get(varIndex).setValue(assignToken.get(index).toString().substring(1, 2));
       else if(checkVar(assignToken.get(index).toString()))
       {
           int l=0;
           boolean charFin=false;
           while(!charFin)
           {
               if(myVar.get(l).getVarName().equals(assignToken.get(index).toString()) && myVar.get(l).getScope().equals(Scope.get(Scope.size()-1)) && myVar.get(l).getType()==2)
               {
                   myVar.get(varIndex).setValue(myVar.get(l).getValue());
                   charFin=true;
               }
               l++;
           }
       }
       index++;
    }
    else if(varType==3)//float
    {
        ArrayList<String>MathToken = new ArrayList<String>();
        while(!assignToken.get(index).toString().equals(";"))
        {
            MathToken.add(assignToken.get(index).toString());
            index++;
        }
        myVar.get(varIndex).setValue((int)evalExpression(MathToken));

    }
    else if(varType==4)//String
    {
        if(assignToken.get(index).toString().charAt(0) == '"')
            myVar.get(varIndex).setValue(assignToken.get(index).toString().substring(1, tempToken.get(index).toString().length()));
        else if(checkVar(assignToken.get(index).toString()))
        {
        int l=0;
        boolean charFin=false;
        while(!charFin)
        {
            if(myVar.get(l).getVarName().equals(assignToken.get(index).toString()) && myVar.get(l).getScope().equals(Scope.get(Scope.size()-1)) && myVar.get(l).getType()==4)
            {
                myVar.get(varIndex).setValue(myVar.get(l).getValue());
                charFin=true;
            }
            l++;
        }
        }
        index++;
    }
    }
    else
    {
    tracking+="Invalid assignment statement: " + assignToken.get(index-1);
    trackLines++;
    //System.out.println("Invalid assignment statement: " + tempToken.get(i-1));
    }
    return index;
}

int evalIF(int index, ArrayList<Object> tokens)
{
int i=0;

boolean bDet = false;
Object o1, o2;
String cOper;
ArrayList<Boolean> arrBoolean = new ArrayList<Boolean>();
ArrayList<String> bOper = new ArrayList<String>();
ArrayList<String> MathTokens = new ArrayList<String>();

try{
for(i=index+2; (!tokens.get(i+1).toString().equals("{") &&
            !tokens.get(i+1).toString().equals(";")); i++)
{ 
if(!isBOper(tokens.get(i).toString()))
{
    if(isCOper(tokens.get(i).toString()))
    { //for Literal:Literal and
      // Id:Id only
        if(isMathOper(tokens.get(i-2).toString()))
        {
            MathTokens = getBeforeLogic(i, tokens);
            //System.out.println("O1:"+MathTokens);
            o1 = evalExpression(MathTokens);
            //System.out.println(o1);
            MathTokens.clear();
        }
        else
        { o1 = tokens.get(i-1);
           if(isIdentifier(o1))
           {
               myVariable mv = new myVariable();
               mv = getVarValue(o1.toString());
               if(mv != null)
               {
                    if(mv.type == 1) //int
                        o1 = (Object)Float.parseFloat(mv.value.toString());
                    else if(mv.type == 2) //char
                        o1 = (Object) mv.value.toString().charAt(0);
                    else if(mv.type == 3) //float
                        o1 = (Object)Float.parseFloat(mv.value.toString());
                    else if(mv.type == 4) //string
                        o1 = (Object)mv.value.toString();
               }
               else
                    System.out.println(o1+" might not have been declared");
           }
       //System.out.println("o1 is "+o1);
        }
        cOper = tokens.get(i).toString();
        
        if(isMathOper(tokens.get(i+2).toString()))
        { //System.out.println("Heyyy==="+tokens.get(i+1));
            MathTokens = getMathToken(i+1, tokens);
            
            MathTokens.remove(MathTokens.size()-1);
            
            o2 =evalExpression(MathTokens);
            
            MathTokens.clear();
        }
        else
        { o2 = tokens.get(i+1);
            if(isIdentifier(o2))
           {
               myVariable mv = new myVariable();
               mv = getVarValue(o2.toString());
               if(mv != null)
               {
                    if(mv.type == 1) //int
                        o2 = (Object)Float.parseFloat(mv.value.toString());
                    else if(mv.type == 2) //char
                        o2 = (Object) mv.value.toString().charAt(0);
                    else if(mv.type == 3) //float
                        o2 = (Object)Float.parseFloat(mv.value.toString());
                    else if(mv.type == 4) //string
                        o2 = (Object)mv.value.toString();
               }
               else
                    System.out.println(o2+" might not have been declared");
           }
        }
        //System.out.println(o1+";"+o2+";"+cOper);
       
        arrBoolean.add(evalBoolean(o1,o2,cOper));
        
    }

    else //probably boolean or function call
    {
        if(tokens.get(i).toString().equals("false"))
            arrBoolean.add(Boolean.FALSE);

        else if(tokens.get(i).toString().equals("true"))
            arrBoolean.add(Boolean.TRUE);

    }
}
else if(tokens.get(i).toString().equals("&&") || tokens.get(i).toString().equals("||"))
{   
        bOper.add(tokens.get(i).toString());
    
}

}
}catch(Exception e){System.out.println(e);}
//System.out.println(arrBoolean);
//System.out.println(bOper);
bDet = evalMultipleBoolean(arrBoolean, bOper);
//System.out.println("Final bDet:" + bDet);

///////////////////////////////////////////////////////
//if Final bDet == true, evaluate statements inside if

if(bDet == true)
{
 //get the statement(s) inside if
 ArrayList<Object> ifBody = new ArrayList<Object>();
 for(int j=i+2; !tokens.get(j).toString().equals("}");j++)
 {
     ifBody.add(tokens.get(j));
     i=j;
 }

i+=2;

    evalStatements(ifBody);



}

//else look for else statements
else 
{ 
 //get the statement(s) inside if
 ArrayList<Object> ifBody = new ArrayList<Object>();
 for(int j=i+2; !tokens.get(j).toString().equals("}");j++)
 {
     ifBody.add(tokens.get(j));
     i=j;
 }
i+=2;
if(tokens.get(i).toString().equals("else") && bDet == false)
{
//recopied this to get the index after the body of IF
ArrayList<Object> elseBody = new ArrayList<Object>();
for(int j=i+2; !tokens.get(j).toString().equals("}");j++)
{
    elseBody.add(tokens.get(j));
    i=j;
}
i+=2;

evalStatements(elseBody);
}
}

return i;
}

boolean isMathOper(String str)
{
    boolean bDet = false;

    if(str.equals("+") || str.equals("-") || str.equals("*") ||
            str.equals("/"))
        bDet = true;

    return bDet;
}

boolean isCOper(String str)
{
    boolean bDet = false;

    if(str.equals(">") || str.equals(">=") || str.equals("<") ||
            str.equals("<=") || str.equals("=="))
        bDet = true;

    return bDet;

}

boolean evalMultipleBoolean(ArrayList<Boolean> arrBoolean, ArrayList<String> bOper)
{
    boolean bDet = false;
    int j = 2;

    //first evaluation
    if(!bOper.isEmpty())
    {
    if(bOper.get(0).equals("&&"))
        bDet = arrBoolean.get(0) && arrBoolean.get(1);
    
    else if(bOper.get(0).equals("||"))
        bDet = arrBoolean.get(0) || arrBoolean.get(1);
    

    for(int i=1; i<bOper.size();i++)
    {   
        if(bOper.get(i).equals("&&"))
            bDet = bDet && arrBoolean.get(j);
        else if(bOper.get(i).equals("||"))
            bDet = bDet || arrBoolean.get(j);

        j++;
    }
    }
    else
       if(!arrBoolean.isEmpty()) //baka function call or boolean 0,1 lng laman e
       bDet = arrBoolean.get(0);

    return bDet;
}


boolean isBOper(String str)
{
    boolean bDet = false;
    if(str.equals("&&") || str.equals("||"))
    bDet = true;

    return bDet;
}


boolean evalBoolean(Object o1, Object o2, String oper)
{
    boolean bDet=false;

    if(isDigit(o1.toString()) && isDigit(o2.toString()))
    {   
       bDet = evalBooleanOper(o1, o2, oper, 1);
       tracking+=bDet+"\n";
       trackLines++;
       //System.out.println(bDet);
    }
    else if(isCharacter(o1) && isCharacter(o2))
    {
       bDet = evalBooleanOper(o1, o2, oper, 2);
       tracking+=bDet+"\n";
       trackLines++;
       //System.out.println(bDet);
    }
    else if(isStringLiteral(o1) && isStringLiteral(o2))
    {
        bDet = evalBooleanOper(o1, o2, oper, 3);
        tracking+=bDet+"\n";
        trackLines++;
        //System.out.println(bDet);
    }
    else if(isIdentifier(o1) && isIdentifier(o2))
    {
        bDet = evalBooleanOper(o1, o2, oper, 4);
        tracking+=bDet+"\n";
        trackLines++;
        //System.out.println(bDet);
    }
    else
    {
        tracking+="Error: Comparing incompatible data types.\n";
        trackLines++;
        System.out.println("Error: Comparing incompatible data types.");
        bDet = false;
    }

    
    return bDet;
}

boolean evalBooleanOper(Object o1, Object o2, String oper, int type)
{
    boolean bDet = false;
    myVariable var1 = null, var2 = null;
    //type = 1 Digit
    //type = 2 Char
    //type = 3 String
    //type = 4 Identifier
    if(oper.equals(">"))
    {
        if(type == 1)
        {
            bDet = Float.parseFloat(o1.toString()) > Float.parseFloat(o2.toString());
            
        }
        else if(type == 2)
        {
            bDet = o1.toString().charAt(1) > o2.toString().charAt(1);
        }
        else if(type == 3)
        {
            bDet = false;
            System.out.println("Operation > cannot be applied to String.");
            
        }
        else //type == 4
        {
            try{
            var1 = getVarValue(o1.toString());
            var2 = getVarValue(o2.toString());
            if(var1 != null && var2 != null)//declared
            {
                if(var1.type == 1 && var2.type == 1) //int
                    bDet = Float.parseFloat(var1.value.toString()) > Float.parseFloat(var2.value.toString());
                    
                else if(var1.type == 2 && var2.type == 2) //char
                     bDet = var1.value.toString().charAt(0) > var2.value.toString().charAt(0);
                    
                else if(var1.type == 3 && var2.type == 3) //float
                   bDet = Float.parseFloat(var1.value.toString()) > Float.parseFloat(var2.value.toString());
                    
                else if(var1.type == 4 && var2.type == 4) //string
                    {
                    bDet = false;
                    System.out.println("Operation > cannot be applied to String.");
                    }
            }
            else
            {
                if(var1 == null)
                    System.out.println(o1.toString() + " might not have been declared.");
                else if(var2 == null)
                    System.out.println(o2.toString() + " might not have been declared.");
            }
            }catch(Exception e){System.out.println("Illegal boolean operation for the data type.");}
        }

    }
    else if(oper.equals("<"))
    {
        if(type == 1)
        {
            bDet = Float.parseFloat(o1.toString()) < Float.parseFloat(o2.toString());
        }
        else if(type == 2)
        {
            bDet = o1.toString().charAt(1) < o2.toString().charAt(1);
        }
        else if(type == 3)
        {
            bDet = false;
            System.out.println("Operation < cannot be applied to String.");

        }
        else //type == 4
        {
            try{
            var1 = getVarValue(o1.toString());
            var2 = getVarValue(o2.toString());
            if(var1 != null && var2 != null)//declared
            {
                if(var1.type == 1 && var2.type == 1) //int
                    bDet = Float.parseFloat(var1.value.toString()) < Float.parseFloat(var2.value.toString());

                else if(var1.type == 2 && var2.type == 2) //char
                     bDet = var1.value.toString().charAt(0) < var2.value.toString().charAt(0);

                else if(var1.type == 3 && var2.type == 3) //float
                   bDet = Float.parseFloat(var1.value.toString()) < Float.parseFloat(var2.value.toString());

                else if(var1.type == 4 && var2.type == 4) //string
                    {
                    bDet = false;
                    System.out.println("Operation < cannot be applied to String.");
                    }
            }
            else
            {
                if(var1 == null)
                    System.out.println(o1.toString() + " might not have been declared.");
                else if(var2 == null)
                    System.out.println(o2.toString() + " might not have been declared.");
            }
            }catch(Exception e){System.out.println("Illegal boolean operation for the data type.");}
        }

    }
    else if(oper.equals(">="))
    {
        if(type == 1)
        {
            bDet = Float.parseFloat(o1.toString()) >= Float.parseFloat(o2.toString());
            
        }
        else if(type == 2)
        {
            bDet = o1.toString().charAt(1) >= o2.toString().charAt(1);
        }
        else if(type == 3)
        {
            bDet = false;
            System.out.println("Operation >= cannot be applied to String.");

        }
        else //type == 4
        {
            try{
            var1 = getVarValue(o1.toString());
            var2 = getVarValue(o2.toString());
            if(var1 != null && var2 != null)//declared
            {
                if(var1.type == 1 && var2.type == 1) //int
                    bDet = Float.parseFloat(var1.value.toString()) >= Float.parseFloat(var2.value.toString());

                else if(var1.type == 2 && var2.type == 2) //char
                     bDet = var1.value.toString().charAt(0) >= var2.value.toString().charAt(0);

                else if(var1.type == 3 && var2.type == 3) //float
                   bDet = Float.parseFloat(var1.value.toString()) >= Float.parseFloat(var2.value.toString());

                else if(var1.type == 4 && var2.type == 4) //string
                    {
                    bDet = false;
                    System.out.println("Operation >= cannot be applied to String.");
                    }
            }
            else
            {
                if(var1 == null)
                    System.out.println(o1.toString() + " might not have been declared.");
                else if(var2 == null)
                    System.out.println(o2.toString() + " might not have been declared.");
            }
            }catch(Exception e){System.out.println("Illegal boolean operation for the data type.");}
        }

    }
    else if(oper.equals("<="))
    {
        if(type == 1)
        {
            bDet = Float.parseFloat(o1.toString()) <= Float.parseFloat(o2.toString());
        }
        else if(type == 2)
        {
            bDet = o1.toString().charAt(1) <= o2.toString().charAt(1);
            
        }
        else if(type == 3)
        {
            bDet = false;
            System.out.println("Operation <= cannot be applied to String.");

        }
        else //type == 4
        {
            try{
            var1 = getVarValue(o1.toString());
            var2 = getVarValue(o2.toString());
            if(var1 != null && var2 != null)//declared
            {
                if(var1.type == 1 && var2.type == 1) //int
                    bDet = Float.parseFloat(var1.value.toString()) <= Float.parseFloat(var2.value.toString());

                else if(var1.type == 2 && var2.type == 2) //char
                     bDet = var1.value.toString().charAt(0) <= var2.value.toString().charAt(0);

                else if(var1.type == 3 && var2.type == 3) //float
                   bDet = Float.parseFloat(var1.value.toString()) <= Float.parseFloat(var2.value.toString());

                else if(var1.type == 4 && var2.type == 4) //string
                    {
                    bDet = false;
                    System.out.println("Operation <= cannot be applied to String.");
                    }
            }
            else
            {
                if(var1 == null)
                    System.out.println(o1.toString() + " might not have been declared.");
                else if(var2 == null)
                    System.out.println(o2.toString() + " might not have been declared.");
            }
            }catch(Exception e){System.out.println("Illegal boolean operation for the data type.");}
        }

    }
    else if(oper.equals("=="))
    {
        if(type == 1)
        {
            bDet = Float.parseFloat(o1.toString()) == Float.parseFloat(o2.toString());
        }
        else if(type == 2)
        {
            bDet = o1.toString().charAt(1) == o2.toString().charAt(1);
        }
        else if(type == 3)
        {
           bDet = o1.toString().equals(o2.toString());

        }
        else //type == 4
        {
            try{
            var1 = getVarValue(o1.toString());
            var2 = getVarValue(o2.toString());
            if(var1 != null && var2 != null)//declared
            {
                if(var1.type == 1 && var2.type == 1) //int
                    bDet = Float.parseFloat(var1.value.toString()) == Float.parseFloat(var2.value.toString());

                else if(var1.type == 2 && var2.type == 2) //char
                     bDet = var1.value.toString().charAt(0) == var2.value.toString().charAt(0);

                else if(var1.type == 3 && var2.type == 3) //float
                   bDet = Float.parseFloat(var1.value.toString()) == Float.parseFloat(var2.value.toString());

                else if(var1.type == 4 && var2.type == 4) //string
                    {
                    bDet = var1.value.toString().equals(var2.value.toString());
                    
                    }
            }
            else
            {
                if(var1 == null)
                    System.out.println(o1.toString() + " might not have been declared.");
                else if(var2 == null)
                    System.out.println(o2.toString() + " might not have been declared.");
            }
            }catch(Exception e){System.out.println("Illegal boolean operation for the data type.");}
        }
        
    }

    return bDet;

}

myVariable getVarValue(String varName2)
{
    myVariable o=null;
    for(int i=0; i<myVar.size();i++)
    {//System.out.println(myVar.get(i).varName + myVar.get(i).type + myVar.get(i).scope + myVar.get(i).value);
        if(myVar.get(i).varName.equals(varName2))
        {o = myVar.get(i);

        }


    }

    return o;
}

int varTypeTemp=-1;
public int getVarIndex(String varName, String varScope)
{
    int k=0;
    boolean bFin=false;
    while(!bFin)
    {
        if(k == myVar.size())
            return -1;
        if(myVar.get(k).getVarName().equals(varName) && myVar.get(k).getScope().equals(varScope))
        {
            bFin =true;
            varTypeTemp= myVar.get(k).getType();
        }
        k++;   
    }
    k--;
    return k;
}



public int varDec(int index, String varType, ArrayList<Object> tokens)
{
    index++;
    boolean pass = true;
    if(varType.equals("int"))
    {
        myVariable var = new myVariable();
        while(!tokens.get(index).toString().equals(";"))
        {
            var.setScope(Scope.get(Scope.size()-1));
            var.setType(1);
            var.setVarName(tokens.get(index).toString());
            index++;

            if(tokens.get(index).equals("="))
            {
                index++;
                ArrayList<String> MathToken = new ArrayList<String>();
                MathToken = getMathToken(index, tokens);
                //System.out.println("math token: " + MathToken);
                int f= (int)evalExpression(MathToken);
                var.setValue(f);
                pass=true;
                while(pass)
                {
                    if(tokens.get(index).toString().equals(";") || tokens.get(index).toString().equals(","))
                        pass=false;
                    else
                        index++;
                }
            }
            if(checkDuplicateVar(var.getVarName()))
                myVar.add(var);
            else
                System.out.println("Variable has already " + var.getVarName() + " been declared");
            if(tokens.get(index).toString().equals(","))
            {
                var = new myVariable();
                index++;
            }
            
        }
    }
    else if(varType.equals("char"))
    {
        myVariable var = new myVariable();
        while(!tokens.get(index).toString().equals(";"))
        {
            var.setScope(Scope.get(Scope.size()-1));
            var.setType(2);
            var.setVarName(tokens.get(index).toString());
            index++;
            
            if(tokens.get(index).equals("="))
            {
                index++;
                if(tokens.get(index).toString().charAt(0) == '\'')
                {
                    var.setValue(tokens.get(index).toString().substring(1, 2));
                    index++;
                }
                else if(checkVar(tempToken.get(index).toString()))
                {
                    int l=0;
                    boolean charFin=false;
                    
                    while(!charFin)
                    {
                       if(myVar.get(l).getVarName().equals(tempToken.get(index).toString()) && myVar.get(l).getScope().equals(Scope.get(Scope.size()-1)) && myVar.get(l).getType()==2)
                       {
                           var.setValue(myVar.get(l).getValue());
                           charFin=true;
                       }
                       l++;
                    }
                }
            }
            if(checkDuplicateVar(var.getVarName()))
                myVar.add(var);
            else
                System.out.println("Variable has already " + var.getVarName() + " been declared");
            if(tokens.get(index).toString().equals(","))
            {
                var = new myVariable();
                index++;
            }
        }
    }
    else if(varType.equals("float"))
    {
        myVariable var = new myVariable();
        while(!tokens.get(index).toString().equals(";"))
        {

            var.setScope(Scope.get(Scope.size()-1));
            var.setType(3);
            var.setVarName(tokens.get(index).toString());
            index++;

            if(tokens.get(index).equals("="))
            {
                index++;
                ArrayList<String> MathToken = new ArrayList<String>();
                MathToken = getMathToken(index, tokens);
                float f= evalExpression(MathToken);
                var.setValue(f);
                pass=true;
                while(pass)
                {
                    if(tokens.get(index).toString().equals(";") || tokens.get(index).toString().equals(","))
                        pass=false;
                    else
                        index++;
                }
            }
            if(checkDuplicateVar(var.getVarName()))
                myVar.add(var);
            else
                System.out.println("Variable has already " + var.getVarName() + " been declared");
            if(tokens.get(index).toString().equals(","))
            {
                var = new myVariable();
                index++;
            }
        }
    }
    else if(varType.equals("String"))
    {
        myVariable var = new myVariable();
        while(!tokens.get(index).toString().equals(";"))
        {
            var.setScope(Scope.get(Scope.size()-1));
            var.setType(4);
            var.setVarName(tokens.get(index).toString());
            index++;

            if(tokens.get(index).equals("="))
            {
                index++;
                if(isStringLiteral(tokens.get(index)))
                {
                    var.setValue(tokens.get(index).toString().substring(1, tokens.get(index).toString().length()-1));
                    index++;
                }
                else if(checkVar(tempToken.get(index).toString()))
                {
                    int l=0;
                    boolean charFin=false;

                    while(!charFin)
                    {
                       if(myVar.get(l).getVarName().equals(tempToken.get(index).toString()) && myVar.get(l).getScope().equals(Scope.get(Scope.size()-1)) && myVar.get(l).getType()==4)
                       {
                           var.setValue(myVar.get(l).getValue());
                           charFin=true;
                       }
                       l++;
                    }
                }
            }
            if(checkDuplicateVar(var.getVarName()))
                myVar.add(var);
            else
                System.out.println("Variable has already " + var.getVarName() + " been declared");
            if(tokens.get(index).toString().equals(","))
            {
                var = new myVariable();
                index++;
            }
        }
    }
    return index;
}
public ArrayList<String> getMathToken(int index, ArrayList<Object> evalToken)
{
    ArrayList<String> MathToken = new ArrayList<String>();
    boolean bPass=true;
  
    while(bPass)
    {
        if(evalToken.get(index).toString().equals(",") || evalToken.get(index).toString().equals(";") || evalToken.get(index).toString().equals("{")
            || isBOper(evalToken.get(index).toString()) || isCOper(evalToken.get(index).toString()))
            bPass=false;
        MathToken.add(evalToken.get(index).toString());
        index++;
    }

    return MathToken;
}
//ndi p tapos... plans on using my midterm assignment... recently figured out that scanner tokens lng yng nappass
int tempCurrent, tempIndex;
public float evalExpression(ArrayList<String> MathToken)
{
    ArrayList<String> evaluate;
    ArrayList<Integer> stack;
    int mathIndex, mathCurrent;
    mathIndex=0; mathCurrent=0;
    ArrayList<myVariable> declared = new ArrayList<myVariable>();
    for(int i=0; i< myVar.size(); i++)
        if(myVar.get(i).getScope().equals(Scope.get(Scope.size()-1)))
        {
            declared.add(myVar.get(i));
        }
    stack = new ArrayList<Integer>();
    evaluate = new ArrayList<String>();
    stack.add(1);
    while(mathIndex < MathToken.size() && stack.size()>0)
    {
        int action = stack.remove(stack.size()-1);
        //System.out.println("ACTION: " + action);
        switch(action)
        {
            case 0: Factor(MathToken, evaluate, stack, mathIndex, mathCurrent);
                mathIndex=tempIndex;
                mathCurrent=tempCurrent;
                break;
            case 1: Expression(MathToken, evaluate, stack, mathIndex, mathCurrent);
                mathIndex=tempIndex;
                mathCurrent=tempCurrent;
                break;
            case 2: ExpressionP(MathToken, evaluate, stack, mathIndex, mathCurrent);
                mathIndex=tempIndex;
                mathCurrent=tempCurrent;
                break;
            case 3: Term(MathToken, evaluate, stack, mathIndex, mathCurrent);
                mathIndex=tempIndex;
                mathCurrent=tempCurrent;
                break;
            case 4: TermP(MathToken, evaluate, stack, mathIndex, mathCurrent);
                mathIndex=tempIndex;
                mathCurrent=tempCurrent;
                break;
            case 5: if(MathToken.get(mathIndex).equals(")")) mathIndex++; break;
        }
    }
    //System.out.println("Answer: " + evaluate.get(0));
    return Float.parseFloat(evaluate.remove(0));

}

public boolean isOperator(String str)
{
    if(str.equals("+") || str.equals("-") || str.equals("*") || str.equals("/") || str.equals("%"))
        return true;
    return false;
}

public boolean isDigit(String str)
{
    try{
        Float.parseFloat(str);
    } catch(NumberFormatException e)
    {
        return false;
    }
    return true;
}

public void Expression(ArrayList<String> MathToken, ArrayList<String> evaluate, ArrayList<Integer> stack, int mathIndex, int mathCurrent)
{
    tempCurrent=0; tempIndex=0;
    if(isDigit(MathToken.get(mathIndex)) || checkVar(MathToken.get(mathIndex)) || checkFunctions(MathToken.get(mathIndex)))
    {
        //System.out.println("ewan");
        stack.add(2);//E'
        stack.add(3);//T
    }
    else if(MathToken.get(mathIndex).equals("("))
    {
        //current+=3;
        stack.add(2);//E'
        stack.add(3);//T
    }
    else
    {
        tracking+="Cannot handle error1";
        trackLines++;
        //System.out.println("Cannot handle error1");
    }
    tempCurrent = mathCurrent;
    tempIndex = mathIndex;
}

public void ExpressionP(ArrayList<String> MathToken, ArrayList<String> evaluate, ArrayList<Integer> stack, int mathIndex, int mathCurrent)
{
    tempCurrent=0; tempIndex=0;
    if(MathToken.get(mathIndex).equals("+") || MathToken.get(mathIndex).equals("-"))
    {
        //System.out.println("ewan 3");
        evaluate.add(MathToken.get(mathIndex) +""+ (mathCurrent + 1));
        stack.add(2);
        stack.add(3);
        mathIndex++;
    }
    else if(MathToken.get(mathIndex).equals(")"))
    {
        //System.out.println("weeeeeeeeeeeeeeeeeeeeeeeeeeeeeird: " + current);
        mathCurrent-=3;
        mathIndex++;
    }
    tempCurrent = mathCurrent;
    tempIndex = mathIndex;
}

public void Term(ArrayList<String> MathToken, ArrayList<String> evaluate, ArrayList<Integer> stack, int mathIndex, int mathCurrent)
{
    tempCurrent=0; tempIndex=0;
    if(isDigit(MathToken.get(mathIndex)) || checkVar(MathToken.get(mathIndex)) || checkFunctions(MathToken.get(mathIndex)))
    {
        //System.out.println("ewan2");
        //stack.remove(stack.size()-1);
        stack.add(4);//T'
        stack.add(0);//F
    }
    else if(MathToken.get(mathIndex).equals("("))
    {
        //current+=3;
        stack.add(4);//T'
        stack.add(0);//F
    }
    else
    {
        tracking+="Cannot handle error2\n";
        trackLines++;
        //System.out.println("Cannot handle error2");
    }
    tempCurrent = mathCurrent;
    tempIndex = mathIndex;
}

public void TermP(ArrayList<String> MathToken, ArrayList<String> evaluate, ArrayList<Integer> stack, int mathIndex, int mathCurrent)
{
    tempCurrent=0; tempIndex=0;
    if(MathToken.get(mathIndex).equals("*") || MathToken.get(mathIndex).equals("/") || MathToken.get(mathIndex).equals("%"))
    {
        //System.out.println("ewan6");
        evaluate.add(MathToken.get(mathIndex) +""+ (mathCurrent + 2));
        stack.add(4);//T'
        stack.add(0);//F
        mathIndex++;
    }
    else if (MathToken.get(mathIndex).equals("+") || MathToken.get(mathIndex).equals("-"))
    {

    }
    else if(MathToken.get(mathIndex).equals(")"))
    {
        //System.out.println("weeeeeeeeeeeeeeeeeeeeeeeeeeeeeird2: " + current);
        mathCurrent-=3;
        mathIndex++;
        //stack.add(4);//T'
    }
    tempCurrent = mathCurrent;
    tempIndex = mathIndex;
}

public void Factor(ArrayList<String> MathToken, ArrayList<String> evaluate, ArrayList<Integer> stack, int mathIndex, int mathCurrent)
{
    tempCurrent=0; tempIndex=0;
    if(mathIndex == MathToken.size() || MathToken.get(mathIndex).equals(")") || isOperator(MathToken.get(mathIndex)))
    {
        tracking+="Skipping stack";
        trackLines++;
        //System.out.println("Skipping stack");
        stack.remove(stack.size()-1);
    }
    if(MathToken.get(mathIndex).equals("("))
    {
        mathCurrent+=3;
        stack.add(5);//)
        stack.add(1);//E
        mathIndex++;
        //Expression(index+1);
    }
    else if(isDigit(MathToken.get(mathIndex)))
    {
        if(evaluate.size()>1)
        {
            //System.out.println("ewan5");
            Evaluate(MathToken, evaluate, mathIndex, mathCurrent);
        }
        else
        {
            evaluate.add(MathToken.get(mathIndex));
        }
        mathIndex++;
        //return Integer.parseInt(tokens.get(index).m_text);
    }
    else if(checkVar(MathToken.get(mathIndex)))
    {
        if(evaluate.size()>1)
        {
            Evaluate(MathToken, evaluate, mathIndex, mathCurrent);
        }
        else
        {
            int varIndex= getVarIndex(MathToken.get(mathIndex), Scope.get(Scope.size()-1));
            if(varTypeTemp == 1 || varTypeTemp ==3)
                evaluate.add(myVar.get(varIndex).getValue().toString());
            else
            {
                String output="";
                if(varTypeTemp == 2)
                    output="char";
                else output="string";
                tracking+="Cannot evaluate expression because of variable: " + MathToken.get(mathIndex) + " of type " + output + "\n";
                trackLines++;
                //System.out.println("Cannot evaluate expression because of variable: " + MathToken.get(mathIndex) + " of type " + output);
                evaluate.add("0");
            }
            tracking+="EVALUATION: " + myVar.get(varIndex).getValue().toString()+"\n";
            trackLines++;
            //System.out.println("EVALUATION: " + myVar.get(varIndex).getValue().toString());
        }
        mathIndex++;
    }
    else if(checkFunctions(MathToken.get(mathIndex)))
    {
        //System.out.println("ewan ko nnman b tlga ganon lng e");
        if(evaluate.size()>1)
        {
            Evaluate(MathToken, evaluate, mathIndex, mathCurrent);
        }
        else
        {
            int funcIndex= getFuncIndex(MathToken.get(mathIndex));
            myFunction f = myFunc.get(funcIndex);
            //System.out.println("data type: " + f.getReturnType());
            mathIndex++;
            mathIndex = functionCall((ArrayList<Object>) MathToken.clone(), mathIndex);

            if(f.getReturnType() == 1 || f.getReturnType() ==3)
            {
                //System.out.println("ewan nnman");
                evaluate.add(returning.get(returning.size()-1).getValue().toString());
            }
            else
            {
                String output="";
                if(varTypeTemp == 2)
                    output="char";
                else output="string";
                tracking+="Cannot evaluate expression because of function: " + MathToken.get(mathIndex) + " returning " + output + "\n";
                trackLines++;
                //System.out.println("Cannot evaluate expression because of variable: " + MathToken.get(mathIndex) + " of type " + output);
                evaluate.add("0");
            }
            tracking+="EVALUATION: " + myVar.get(funcIndex).getValue().toString()+"\n";
            trackLines++;
            //System.out.println("EVALUATION: " + myVar.get(funcIndex).getValue().toString());
        }
    }
    tempCurrent = mathCurrent;
    tempIndex = mathIndex;
}

public int getFuncIndex(String funcName)
{
    
    for(int i=0; i< myFunc.size(); i++)
        if(myFunc.get(i).getName().equals(funcName))
            return i;
    return -1;
}

public void Evaluate(ArrayList<String> MathToken, ArrayList<String> evaluate, int mathIndex, int mathCurrent)
{
    float num2, num;
	  if(evaluate.size()%2==0)
		  num2 = Float.parseFloat(MathToken.get(mathIndex));

	  else
		  num2 = Float.parseFloat(evaluate.remove(evaluate.size()-1));

	  double fin=0;
	  String operator = evaluate.get(evaluate.size()-1);
	  char Op = operator.charAt(0);
	  //System.out.println("Operator: " + operator);
	  int preOp = Integer.parseInt(operator.substring(1));
	  boolean moveOn = false;

	  if(mathIndex>=MathToken.size()-2)
		  moveOn=true;
	  else if(preOp >= Precedence(MathToken.get(mathIndex+1), MathToken, mathIndex, mathCurrent) + mathCurrent)
		  moveOn=true;
	  if(moveOn)
	  {
		  Op = evaluate.remove(evaluate.size()-1).charAt(0);
		  num = Float.parseFloat(evaluate.remove(evaluate.size()-1));
		  //System.out.println("Number: " + num);
		  switch(Op)
		  {
		  case '+': fin = num + num2; break;
		  case '-': fin = num - num2; break;
		  case '*': fin = num * num2; break;
		  case '/': fin = num / num2; break;
		  case '%': fin = num % num2; break;
		  }
		  int nFinal = (int) fin;
		  //System.out.println("Final: " + nFinal);
		  evaluate.add(nFinal+"");
		  //System.out.println("EVAL SIZE: " + eval.size());
		  if(evaluate.size()>1)
			  Evaluate(MathToken, evaluate, mathIndex, mathCurrent);
	  }
	  else
		  evaluate.add(num2+"");
}

public int Precedence(String str, ArrayList<String> MathToken, int mathIndex, int mathCurrent)
{
    //System.out.println("str: " + str);
    char c = str.charAt(0);
    switch(c)
    {
        case '+':
        case '-': return 1;
        case '*':
        case '/':
        case '%': return 2;
        case ')': if(mathIndex < MathToken.size()-2)
        if(MathToken.get(mathIndex+1).equals(")"))
        {	mathIndex++; mathCurrent-=3;
            return (Precedence(MathToken.get(mathIndex), MathToken, mathIndex, mathCurrent) - 3);
        } return -3;
    }
    return 0;
}

public void evalFunction(myFunction f, ArrayList<Object> paramValue)
{
    ArrayList<ArrayList<String>> finParam = new ArrayList<ArrayList<String>>();
    for(int i=0; i<paramValue.size(); i++)
    {
        boolean bEnd=false;
        ArrayList<String> tokens = new ArrayList<String>();
        while(!bEnd)
        {
            if(paramValue.get(i).toString().equals(",") || paramValue.get(i).toString().equals(")"))
                bEnd=true;
            else
            {
                tokens.add(paramValue.get(i).toString());
                i++;
            }
        }
        finParam.add(tokens);
    }
    
    for(int i=0; i< f.getVars().size(); i++)
    {
        int tempType;
        myVariable var= new myVariable();
        var.setScope(Scope.get(Scope.size()-1));
        var.setVarName(f.getVars().get(i));

        if(f.getTypes().get(i) == 1)
        {
            tempType = isIntParam(finParam.get(i));
            var.setType(1);
            if(tempType ==1)
                var.setValue(Integer.parseInt(finParam.get(i).get(0)));
            else if(tempType ==2)
            {
                for(int j=0; j<myVar.size(); j++)
                {
                    if(myVar.get(j).getVarName().equals(finParam.get(i).get(0)) && myVar.get(j).getScope().equals(Scope.get(Scope.size()-2)))
                        var.setValue(myVar.get(j).getValue());
                }
            }
            else if(tempType==3) //function call
            {
                
            }
            else
            {
                var.setValue("0");
                System.out.println("Incompatible data type " + finParam.get(i).get(0) + " set as a parameter for function: " + Scope.get(Scope.size()-1));
            }
        }
        else if(f.getTypes().get(i) == 2)
        {
            tempType = isIntParam(finParam.get(i));
            var.setType(2);
            if(tempType ==1)
                var.setValue(finParam.get(i).get(0));
            else if(tempType ==2)
            {
                for(int j=0; j<myVar.size(); j++)
                {
                    if(myVar.get(j).getVarName().equals(finParam.get(i).get(0)) && myVar.get(j).getScope().equals(Scope.get(Scope.size()-2)))
                        var.setValue(myVar.get(j).getValue());
                }
            }
            else if(tempType==3) //function call
            {

            }
            else
            {
                var.setValue("0");
                System.out.println("Incompatible data type " + finParam.get(i).get(0) + " set as a parameter for function: " + Scope.get(Scope.size()-1));
            }
        }
        else if(f.getTypes().get(i) == 3)
        {
            tempType = isIntParam(finParam.get(i));
            var.setType(3);
            if(tempType ==1)
                var.setValue(Float.parseFloat(finParam.get(i).get(0)));
            else if(tempType ==2)
            {
                for(int j=0; j<myVar.size(); j++)
                {
                    if(myVar.get(j).getVarName().equals(finParam.get(i).get(0)) && myVar.get(j).getScope().equals(Scope.get(Scope.size()-2)))
                        var.setValue(myVar.get(j).getValue());
                }
            }
            else if(tempType==3) //function call
            {

            }
            else
            {
                var.setValue("0");
                System.out.println("Incompatible data type " + finParam.get(i).get(0) + " set as a parameter for function: " + Scope.get(Scope.size()-1));
            }
        }
        else if(f.getTypes().get(i) == 4)
        {
            tempType = isIntParam(finParam.get(i));
            var.setType(4);
            if(tempType ==1)
                var.setValue(finParam.get(i).get(0));
            else if(tempType ==2)
            {
                for(int j=0; j<myVar.size(); j++)
                {
                    if(myVar.get(j).getVarName().equals(finParam.get(i).get(0)) && myVar.get(j).getScope().equals(Scope.get(Scope.size()-2)))
                        var.setValue(myVar.get(j).getValue());
                }
            }
            else if(tempType==3) //function call
            {

            }
            else
            {
                var.setValue("0");
                System.out.println("Incompatible data type " + finParam.get(i).get(0) + " set as a parameter for function: " + Scope.get(Scope.size()-1));
            }
        }
        if(checkDuplicateVar(var.getVarName()))
            myVar.add(var);
        else
            System.out.println("The parameter name for function: " + Scope.get(Scope.size()-1) + " is a constant");
    }
    ArrayList<Object> a = (ArrayList<Object>) f.getTokens().clone();
    //System.out.println("------------" + a);
    evalStatements(a);
}

public ArrayList<String> getBeforeLogic(int index, ArrayList<Object> tokens)
{
 int nParen=0;
    boolean bParen=true;
    ArrayList<String> mathTokens = new ArrayList<String>();
    index--;
    while(bParen)
    {
        if(tokens.get(index).toString().equals(")"))
            nParen++;
        else if(tokens.get(index).toString().equals("("))
            nParen--;
        mathTokens.add(0, tokens.get(index).toString());
        index--;
        if(tokens.get(index).toString().equals("(") && nParen==0 || tokens.get(index).toString().equals("||") || tokens.get(index).toString().equals("&&"))
            bParen = false;
    }
    return mathTokens;
}

public int isIntParam(ArrayList<String> toks)
{
    //System.out.println("intparam: " + toks.get(0));
    if(isDigit(toks.get(0)))
        return 1;
    else if(checkVar(toks.get(0), Scope.get(Scope.size()-2)))
    {
        for(int i=0; i<myVar.size(); i++)
        {
            if(myVar.get(i).getVarName().equals(toks.get(0)) && myVar.get(i).getType() == 1)
                return 2;
        }
    }
    else if(checkFunctions(toks.get(0)))
    {
        for(int i=0; i<myFunc.size(); i++)
            if(myFunc.get(i).getName().equals(toks.get(0)) && myFunc.get(i).getReturnType() == 1)
                return 3;
    }
    return 0;

}

public int isCharParam(ArrayList<String> toks)
{
    if(isCharacter(toks.get(0)))
        return 1;
    else if(checkVar(toks.get(0), Scope.get(Scope.size()-2)))
    {
        for(int i=0; i<myVar.size(); i++)
            if(myVar.get(i).getVarName().equals(toks.get(0)) && myVar.get(i).getType() == 2)
                return 2;
    }
    else if(checkFunctions(toks.get(0)))
    {
        for(int i=0; i<myFunc.size(); i++)
            if(myFunc.get(i).getName().equals(toks.get(0)) && myFunc.get(i).getReturnType() == 2)
                return 3;
    }
    return 0;
}

public int isFloatParam(ArrayList<String> toks)
{
    if(isDigit(toks.get(0)))
        return 1;
    else if(checkVar(toks.get(0), Scope.get(Scope.size()-2)))
    {
        for(int i=0; i<myVar.size(); i++)
            if(myVar.get(i).getVarName().equals(toks.get(0)) && myVar.get(i).getType() == 3)
                return 2;
    }
    else if(checkFunctions(toks.get(0)))
    {
        for(int i=0; i<myFunc.size(); i++)
            if(myFunc.get(i).getName().equals(toks.get(0)) && myFunc.get(i).getReturnType() == 3)
                return 3;
    }
    return 0;
}

public int isStringParam(ArrayList<String> toks)
{
    if(isStringLiteral(toks.get(0)))
        return 1;
    else if(checkVar(toks.get(0), Scope.get(Scope.size()-2)))
    {
        for(int i=0; i<myVar.size(); i++)
            if(myVar.get(i).getVarName().equals(toks.get(0)) && myVar.get(i).getType() == 4)
                return 2;
    }
    else if(checkFunctions(toks.get(0)))
    {
        for(int i=0; i<myFunc.size(); i++)
            if(myFunc.get(i).getName().equals(toks.get(0)) && myFunc.get(i).getReturnType() == 4)
                return 3;
    }
    return 0;
}

public void evalStatements(ArrayList<Object> evaltokens)
{
    for(int i=0; i<evaltokens.size(); i++)
    {
        //System.out.println("eval token: " + evaltokens.get(i));
        if(evaltokens.get(i).toString().equals("print"))
        {
           i= doPrint(i, evaltokens);
        }
        else if(evaltokens.get(i).toString().equals("int") || evaltokens.get(i).toString().equals("String") ||
                evaltokens.get(i).toString().equals("char") ||evaltokens.get(i).toString().equals("float"))
        {
            //i=variableDec(i, tempToken.get(i).toString());
            i= varDec(i, evaltokens.get(i).toString(), evaltokens);
            //System.out.println("Index is now: " + i);
        }

        else if(evaltokens.get(i).toString().equals("if"))
        {
            i = evalIF(i, evaltokens);

        }
        else if(evaltokens.get(i).toString().equals("="))
        {
            i = evalAssignment(evaltokens, i);
        }
        else if(evaltokens.get(i).toString().equals("()") || evaltokens.get(i).toString().equals("("))
        {
            i = functionCall(evaltokens, i);
            //System.out.println("after function: " + tempToken.get(i));
        }
        else if(evaltokens.get(i).toString().equals("while"))
        {
            i = whileCall(evaltokens, i);
        }
        else if(evaltokens.get(i).toString().equals("for"))
        {
            i = forCall(evaltokens, i);
        }
        else if(evaltokens.get(i).toString().equals("do"))
        {
            i = doCall(evaltokens, i);
        }
        else if(evaltokens.get(i).toString().equals("return"))
        {
            //System.out.println("laaaaaaaaast");
            i++;
            returnFunc rf = new returnFunc();
            rf = returning.get(returning.size()-1);
            ArrayList<String> mathTok = new ArrayList<String>();
            if(rf.getRType() ==1 || rf.getRType() ==3)
            {
                mathTok.add(evaltokens.get(i).toString());
                returning.get(returning.size()-1).setValue(evalExpression(mathTok));
            }
            break;
        }
    }
}

public boolean checkMathFunc(String name)
{
    for(int i=0; i<myFunc.size(); i++)
        if(myFunc.get(i).getName().equals(name) && (myFunc.get(i).getReturnType() == 1 || myFunc.get(i).getReturnType() == 3))
            return true;
    return false;
}

public boolean checkFunctions(String name)
{
    for(int i=0; i<myFunc.size(); i++)
        if(myFunc.get(i).getName().equals(name))
            return true;
    return false;
}

public boolean checkVar(String name)
{
    for(int i=0; i<myVar.size(); i++)
        if(myVar.get(i).getVarName().equals(name) && myVar.get(i).getScope().equals(Scope.get(Scope.size()-1)))
            return true;
    return false;
}

public boolean checkVar(String name, String varScope)
{
    for(int i=0; i<myVar.size(); i++)
        if(myVar.get(i).getVarName().equals(name) && myVar.get(i).getScope().equals(varScope))
            return true;
    return false;
}

public boolean checkDec(ArrayList<myVariable> vars, String name)
{
    for(int i=0; i<vars.size(); i++)
        if(vars.get(i).getVarName().equals(name))
            return true;
    return false;
}

public int scanInt(int index, ArrayList<Object> tokens)
{
    tracking+="Entered Scan...\n";
    trackLines++;
    final String varName = tokens.get(index+2).toString();
    tracking+="Variable name: "+varName+"\n";
    trackLines++;
    if(!checkDec(myVar, varName))
    {
        tracking+="Variable "+varName+" is not declared\n";
        trackLines++;
    } else
    {
        if(getVariable(varName).getType()!=1)
        {    tracking+="Incompatible types... Variable must be int\n";
             trackLines++;
        } else //Sana editable lang sa line na un...
        {
            readVal = (String)JOptionPane.showInputDialog("","");
            if(readVal==null) readVal="";
            if(isInt(readVal))
            {
                updateVar(varName, readVal);
                this.append(readVal+"\n");
                tracking+="The value inputted: "+readVal+"\n";
                trackLines++;
            } else {
                tracking+="The value inputted, \""+readVal+"\" is not an integer\n";
                trackLines++;
            }
        }
    }
    return index+4;
}

    private boolean isFloat(String readVal) {
        try {
    Float.parseFloat(readVal);
    } catch(NumberFormatException ex) {
    return false;
    }
    return true;
    }

    private int scanCHAR(int i, ArrayList<Object> token) {
       tracking+="Entered Scan...\n";
        trackLines++;
        final String varName = token.get(i+2).toString();
        tracking+="Variable name: "+varName+"\n";
        trackLines++;
        if(!checkDec(myVar, varName))
        {
            tracking+="Variable "+varName+" is not declared\n";
            trackLines++;
        } else
        {
            if(getVariable(varName).getType()!=2)
            {    tracking+="Incompatible types... Variable must be char\n";
                 trackLines++;
            } else //Sana editable lang sa line na un...
            {
                readVal = (String)JOptionPane.showInputDialog("","");
                if(readVal==null) readVal="";
                if(readVal.length()<=1)
                {
                    updateVar(varName, readVal);
                    this.append(readVal+"\n");
                    tracking+="The value inputted: "+readVal+"\n";
                    trackLines++;
                } else {
                    tracking+="The value inputted, \""+readVal+"\" is not a character\n";
                    trackLines++;
                }
            }
        }
    return i+4;
    }

    private int scanFloat(int i, ArrayList<Object> token) {
        tracking+="Entered Scan...\n";
        trackLines++;
        final String varName = token.get(i+2).toString();
        tracking+="Variable name: "+varName+"\n";
        trackLines++;
        if(!checkDec(myVar, varName))
        {
            tracking+="Variable "+varName+" is not declared\n";
            trackLines++;
        } else
        {
            if(getVariable(varName).getType()!=3)
            {    tracking+="Incompatible types... Variable must be float\n";
                 trackLines++;
            } else //Sana editable lang sa line na un...
            {
                readVal = (String)JOptionPane.showInputDialog("","");
                if(readVal==null) readVal="";
                if(isFloat(readVal))
                {
                    updateVar(varName, readVal);
                    this.append(readVal+"\n");
                    tracking+="The value inputted: "+readVal+"\n";
                    trackLines++;
                } else {
                    tracking+="The value inputted, \""+readVal+"\" is not a float\n";
                    trackLines++;
                }
            }

        }

    return i+4;
    }

    private int scanSTRING(int i, ArrayList<Object> token) {
        tracking+="Entered Scan...\n";
        trackLines++;
        final String varName = token.get(i+2).toString();
        tracking+="Variable name: "+varName+"\n";
        trackLines++;
        if(!checkDec(myVar, varName))
        {
            tracking+="Variable "+varName+" is not declared\n";
            trackLines++;
        } else
        {
            if(getVariable(varName).getType()!=3)
            {    tracking+="Incompatible types... Variable must be a string\n";
                 trackLines++;
            } else //Sana editable lang sa line na un...
            {
                readVal = (String)JOptionPane.showInputDialog("","");
                if(readVal==null) readVal="";
                updateVar(varName, readVal);
                this.append(readVal+"\n");
                tracking+="The value inputted: "+readVal+"\n";
                trackLines++;
            }
        }

    return i+4;
    }

private void updateVar(String varName, String readVal) {

    int x=0;
    boolean isFound=false;
    while(!isFound)
    {
        if(myVar.get(x).getVarName().equals(varName) && myVar.get(x).getScope().equals(Scope.get(Scope.size()-1)))
        {
            myVar.get(x).setValue(readVal);
            isFound=true;
        }
        x++;
    }
}

public boolean isInt(String readVal)
{
    try {
    Integer.parseInt(readVal);
    } catch(NumberFormatException ex) {
    return false;
    }
    return true;
}

public myVariable getVariable(String variableName)
{
    for (myVariable v : myVar) {
        if(v.getVarName().equals(variableName) && v.getScope().equals(Scope.get(Scope.size()-1)))
           return  v;
    }

    return null;
}

public int doPrint(int index, ArrayList<Object> printToken)
{   ArrayList<Object> print = new ArrayList<Object>();
    int newIndex, i;
    //index+2 to skip the open parenthesis, aim is to fill print with
    //contents to be printed
    for(i=index+2; !printToken.get(i).equals(")");i++)
    {   //dagdag din for character literals
        if(isStringLiteral(printToken.get(i)))
            print.add(printToken.get(i).toString().substring(1, printToken.get(i).toString().length()-1));
        else //assume muna na identifier if not literals
        { boolean isVarDec = false;
            for(int j=0; j<myVar.size();j++)
            {
                if(printToken.get(i).toString().equals(myVar.get(j).getVarName()) && (myVar.get(j).getScope().equals(Scope.get(Scope.size()-1)) || myVar.get(j).getScope().equals("0")))
                {print.add(myVar.get(j).getValue());
                 isVarDec = true;
                }

            }
          if(isVarDec == false)
              print.add("Variable "+printToken.get(i)+" might not be declared.");
        }
    }
    newIndex = i + 1; //for the ';'
    //output into console the string literals found in print (temporary limitatioN)
    //System.out.println("ewan ko kng bkt: " + print.size());
    for(int j=0; j<print.size();j++)
    {   if(print.get(j).toString().equals("ln"))
        {
            
            System.out.print("\n");
        }
        else
        {
            this.append(print.get(j)+"");
            System.out.print(print.get(j));
        }
    }
    return newIndex;
}

public boolean isStringLiteral(Object o)
{
    if(o.toString().charAt(0) == '"') //kung sa start meron na '"'
    {
        return true;
    }
    else return false;
}

public boolean isCharacter(Object o)
{
    if(o.toString().charAt(0) == '\'') //kung sa start meron na '"'
    {
        return true;
    }
    else return false;
}

public boolean isIdentifier(Object o)
{
    if(Character.isLetter(o.toString().charAt(0)))
        return true;
    else return false;

}

boolean evalLoop(ArrayList<Object> tokens)
{
    int i=0;
    boolean bDet = false;
    Object o1, o2;
    String cOper;
    ArrayList<Boolean> arrBoolean = new ArrayList<Boolean>();
    ArrayList<String> bOper = new ArrayList<String>();
    ArrayList<String> MathTokens = new ArrayList<String>();
 
    try{
    for(i=2; (!tokens.get(i+1).toString().equals("{") &&
                !tokens.get(i+1).toString().equals(";")); i++)
    { //System.out.println(tokens.get(i).toString());
    if(!isBOper(tokens.get(i).toString()))
    {
        if(isCOper(tokens.get(i).toString()))
        { //for Literal:Literal and
          // Id:Id only
            if(isMathOper(tokens.get(i-2).toString()))
            {//bat ayaw mo nlng ievaluate pati yng after ng >; pumasa n sya ng grammar e
                MathTokens = getBeforeLogic(i, tokens);
                //System.out.println("O1:"+MathTokens);
                o1 = evalExpression(MathTokens);
                //System.out.println(o1);
                MathTokens.clear();
            }
            else
            { o1 = tokens.get(i-1);
               if(isIdentifier(o1))
               {
                   myVariable mv = new myVariable();
                   mv = getVarValue(o1.toString());
                   if(mv != null)
                   {
                        if(mv.type == 1) //int
                            o1 = (Object)Float.parseFloat(mv.value.toString());
                        else if(mv.type == 2) //char
                            o1 = (Object) mv.value.toString().charAt(0);
                        else if(mv.type == 3) //float
                            o1 = (Object)Float.parseFloat(mv.value.toString());
                        else if(mv.type == 4) //string
                            o1 = (Object)mv.value.toString();
                   }
                   else
                        System.out.println(o1+" might not have been declared");
               }
            }
            
            cOper = tokens.get(i).toString();
          
            if(isMathOper(tokens.get(i+2).toString()))
            { 
                MathTokens = getMathToken(i+1, tokens);
                
                MathTokens.remove(MathTokens.size()-1);
               
                o2 =evalExpression(MathTokens);
                //System.out.println(o2);
                MathTokens.clear();
            }
            else
            { 
                o2 = tokens.get(i+1);
                if(isIdentifier(o2))
               {
                   myVariable mv = new myVariable();
                   mv = getVarValue(o2.toString());
                   if(mv != null)
                   {
                        if(mv.type == 1) //int
                            o2 = (Object)Float.parseFloat(mv.value.toString());
                        else if(mv.type == 2) //char
                            o2 = (Object) mv.value.toString().charAt(0);
                        else if(mv.type == 3) //float
                            o2 = (Object)Float.parseFloat(mv.value.toString());
                        else if(mv.type == 4) //string
                            o2 = (Object)mv.value.toString();
                   }
                   else
                        System.out.println(o2+" might not have been declared");
               }
            }
            //System.out.println(o1+";"+o2+";"+cOper);

            arrBoolean.add(evalBoolean(o1,o2,cOper));

        }

        else //probably boolean or function call
        {
            if(tokens.get(i).toString().equals("false"))
                arrBoolean.add(Boolean.FALSE);

            else if(tokens.get(i).toString().equals("true"))
                arrBoolean.add(Boolean.TRUE);

        }
    }
    else if(tokens.get(i).toString().equals("&&") || tokens.get(i).toString().equals("||"))
    {
            bOper.add(tokens.get(i).toString());

    }

    }
    }catch(Exception e){System.out.println(e);}
    //System.out.println(arrBoolean);
    //System.out.println(bOper);
    bDet = evalMultipleBoolean(arrBoolean, bOper);
    //System.out.println("Final bDet:" + bDet);

    return bDet;
}

}


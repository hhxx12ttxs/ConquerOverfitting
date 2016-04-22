package ru.tsu.inf.cdel.semantical.type;

import java.util.HashMap;
import ru.tsu.inf.cdel.semantical.Operator;
import ru.tsu.inf.cdel.semantical.SemanticalError;

public class PrimitiveTypeManager {
    private static HashMap< String, PrimitiveType> primitiveMap;
    private static HashMap< Integer, PrimitiveType> primitiveIdentMap;

    static PrimitiveTypeManager _instance = null;
    public static PrimitiveTypeManager getInstance() {
        if (_instance == null) {
            _instance = new PrimitiveTypeManager();
        }
        return _instance;
    }
    
    private Integer[][][] binaryOperatorApplication;
    private Integer[][] unaryOperatorApplication;
    
    private PrimitiveTypeManager() {
        primitiveMap = new HashMap<>();
        primitiveIdentMap = new HashMap<>();

        primitiveMap.put("integer", new PrimitiveType(PrimitiveType.INTEGER));
        primitiveMap.put("string", new PrimitiveType(PrimitiveType.STRING));
        primitiveMap.put("double", new PrimitiveType(PrimitiveType.DOUBLE));
        primitiveMap.put("bool", new PrimitiveType(PrimitiveType.BOOL));
        
        for (PrimitiveType t : primitiveMap.values()) {
            primitiveIdentMap.put(t.getTypeId(), t);
        }
        
        binaryOperatorApplication = new Integer[20][10][10];
        unaryOperatorApplication = new Integer[20][10];
        
        Integer[] compOps = new Integer[] {Operator.EQUALS, Operator.NOTEQUALS, Operator.MORE, Operator.LESS, Operator.MOREOREQUAL, Operator.LESSOREQUAL};
        Integer[] numbers = new Integer[] {PrimitiveType.DOUBLE, PrimitiveType.INTEGER};
        Integer[] all = new Integer[] {PrimitiveType.DOUBLE, PrimitiveType.INTEGER, PrimitiveType.BOOL, PrimitiveType.STRING};
        
        for (int a1 : all) {
            binaryOperatorApplication[Operator.EQUALS][a1][a1] = binaryOperatorApplication[Operator.NOTEQUALS][a1][a1] = PrimitiveType.BOOL;
        }
        
        for (int op1 : numbers) {
            for (int op : compOps) {
                binaryOperatorApplication[op][op1][op1] = PrimitiveType.BOOL;
            }
            int resType = op1;
            binaryOperatorApplication[Operator.MINUS][op1][op1] = resType;
            binaryOperatorApplication[Operator.PLUS][op1][op1] = resType;
            binaryOperatorApplication[Operator.MULTIPLY][op1][op1] = resType;
            binaryOperatorApplication[Operator.DIVIDE][op1][op1] = resType;
            unaryOperatorApplication[Operator.UNARY_MINUS][op1]=op1;
        }
        
        binaryOperatorApplication[Operator.PLUS][PrimitiveType.STRING][PrimitiveType.STRING] = PrimitiveType.STRING;
        
        binaryOperatorApplication[Operator.DIVIDE][PrimitiveType.INTEGER][PrimitiveType.INTEGER] = PrimitiveType.DOUBLE;
        binaryOperatorApplication[Operator.DIV][PrimitiveType.INTEGER][PrimitiveType.INTEGER] = PrimitiveType.INTEGER;
        binaryOperatorApplication[Operator.MOD][PrimitiveType.INTEGER][PrimitiveType.INTEGER] = PrimitiveType.INTEGER;
        
        unaryOperatorApplication[Operator.NOT][PrimitiveType.BOOL]=PrimitiveType.BOOL;
        
        binaryOperatorApplication[Operator.AND][PrimitiveType.BOOL][PrimitiveType.BOOL] = PrimitiveType.BOOL;
        binaryOperatorApplication[Operator.OR][PrimitiveType.BOOL][PrimitiveType.BOOL] = PrimitiveType.BOOL;
    }
    
    public Type getTypeById(int id) {
        return primitiveIdentMap.get(id);
    }
    
    public Type getTypeByName(String ident) {
        if (primitiveMap.containsKey(ident.toLowerCase())) {
            return primitiveMap.get(ident.toLowerCase());
        }
        
        throw new SemanticalError("wrong type " + ident);
    }
    
    public Type getTypeOfBinaryOperatorApplication(Operator op, Integer t1, Integer t2) {
        Integer t=binaryOperatorApplication[op.getType()][t1][t2];
        if (t==null) {
            return null;
        }
        return primitiveIdentMap.get(t);
    }
    
    public Type getTypeOfUnaryOperatorApplication(Operator op, Integer t1) {
        Integer t=unaryOperatorApplication[op.getType()][t1];
        if (t==null) {
            return null;
        }
        return primitiveIdentMap.get(t);
    }
}


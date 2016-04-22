package edu.uiowa.csense.compiler.types;

import edu.uiowa.csense.compiler.CompilerException;
import edu.uiowa.csense.compiler.types.constraints.Constraint;
import edu.uiowa.csense.runtime.types.ByteVector;
import edu.uiowa.csense.runtime.types.CharVector;
import edu.uiowa.csense.runtime.types.DoubleVector;
import edu.uiowa.csense.runtime.types.FloatVector;
import edu.uiowa.csense.runtime.types.ShortVector;
import edu.uiowa.csense.runtime.types.TypeInfo;

public class FrameTypeC extends BaseTypeC {
    protected PrimitiveType _baseType;
    
    // these values are determined when
    protected boolean materialized = false;
    protected int[] multiplier = new int[2];

    
    public FrameTypeC(Class messageType) {
	super(messageType);
	
	if (messageType == ByteVector.class) {
	    _elementSize = 1;
	    _baseType = new PrimitiveType(PrimitiveType.PRIMITIVE_BYTE);
	} else if (messageType == CharVector.class) {
	    _elementSize = 2;
	    _baseType = new PrimitiveType(PrimitiveType.PRIMITIVE_CHAR);
	} else if (messageType == ShortVector.class) {
	    _elementSize = 2;
	    _baseType = new PrimitiveType(PrimitiveType.PRIMITIVE_SHORT);
	} else if (messageType == DoubleVector.class) {	    
	    _elementSize = 8;
	    _baseType = new PrimitiveType(PrimitiveType.PRIMITIVE_DOUBLE);
	} else if (messageType == FloatVector.class) {	    
	    _elementSize = 4;
	    _baseType = new PrimitiveType(PrimitiveType.PRIMITIVE_FLOAT);
	} 
	/*else if (messageType == DoubleMatrix.class) {
	    _elementSize = 8;
	    _baseType = new PrimitiveType(PrimitiveType.PRIMITIVE_DOUBLE);
	} else if (messageType == FloatMatrix.class) {
	    _elementSize = 4;
	    _baseType = new PrimitiveType(PrimitiveType.PRIMITIVE_FLOAT);
	} else if (messageType == ShortMatrix.class) {
	    _elementSize = 2;
	    _baseType = new PrimitiveType(PrimitiveType.PRIMITIVE_SHORT);
	} */  
	else {	
	    throw new IllegalArgumentException("Unknown type " + messageType);
	}
    }

    public FrameTypeC(FrameTypeC type) {
	super(type.getMessageType());
	_elementSize = type.getElementSize();
	_rows = type.getRows();	
	_columns = type.getColumns();	
	_baseType = type.getBaseType();
    }
    
    @Override
    public FrameTypeC clone() {
	return new FrameTypeC(this);
    }
      
    public void setConstraint(Constraint constraint) {
	addConstraint(constraint);
    }

//    public Constraint getConstraint() {
//	if (_constraints.size() > 1) {
//	    //TODO: allow for multiple constraints
//	    throw new IllegalStateException();
//	}
//	if (_constraints.size() == 0) return null;
//	return _constraints.get(0);
//    }

    public void validate() {
	materialized = true;
    }

    public PrimitiveType getBaseType() {
	return _baseType;
    }

    public TypeInfo getTypeInfo() {
	TypeInfo info = new TypeInfo(getMessageType(), getElementSize(), getRows(), getColumns(), true, false);
	return info;
    }

    @Override
    public void setMultiplier(int rows, int columns) {
	this.multiplier[Constraint.ROW_DIMENSION] = rows;
	this.multiplier[Constraint.COLUMN_DIMENSION] = columns;
    }

    @Override
    public int getMultipler(int dim) {
	return this.multiplier[dim];
    }

    public TypeMaterialization simpleSolution() throws CompilerException {
	TypeMaterialization m = new TypeMaterialization();
	if (_constraints == null) {
	    throw new CompilerException("Unconstrained type");
	}
	
	m.setRows(1);
	if (_constraints.size() == 1) {
	    m.setColumns( _constraints.get(0).getValue());	    
	} else {
	    for (Constraint constraint : _constraints) {
		// handle all constraints as equal
		m.materialize(constraint.getDimension(), constraint.getValue());
//		if (constraint instanceof Equal) {
//		    Equal equal = (Equal) constraint;
//		    if (equal.getDimension() == Constraint.ROW_DIMENSION) {
//			m.setRows(equal.getValue());
//		    } else if (equal.getDimension() == Constraint.COLUMN_DIMENSION) {
//			m.setColumns(equal.getValue());
//		    } else {
//			throw new CompilerException("Invalid dimension");
//		    }
//		} else if (constraint instanceof MultipleOf) {		    
//		    m.materialize(constraint.getDimension(), constraint.getValue());
//		} else {
//		    throw new CompilerException("Cannot handle constraint");
//		}
	    }
	}
	return m;
    }
    
    public boolean isMaterialized() {
	return materialized;
    }

    @Override
    public int getNumberOfElements() throws CompilerException {	
	return _rows * _columns;
    }
    
    @Override
    public String toString() {
	return _baseType.toString() + ": " + _rows + " x " + _columns + " constraints: " + _constraints; 
    }

}


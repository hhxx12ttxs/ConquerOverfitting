class ArrayType extends Type
{
	PrimitiveType primitiveType;
	ArrayIndexDeclaration arrayIndexDeclaration;

	ArrayType(PrimitiveType p, ArrayIndexDeclaration a)
	{
		primitiveType = p;
		arrayIndexDeclaration = a;
	}

	void printParseTree(String indent)
	{
		String indent1 = indent + " ";

		IO.displayln(indent + indent.length() + " <array type>");
		primitiveType.printParseTree(indent1);
		arrayIndexDeclaration.printParseTree(indent1);
	}
	
	Array_Type typeVal()
	{
		Primitive_Type primitive_Type = primitiveType.typeVal();

		// count the # of dimensions of this array
		int dim = 0;
		RangeList p = arrayIndexDeclaration.rangeList;
		while ( p.getClass() == MultipleRange.class )
		{
			dim++;
			p = ((MultipleRange)p).rangeList;
		}
		dim++;

		int[] rangeList = new int[dim*2];
		int lowerBound, upperBound;
		boolean arrayBoundsError = false;

		int i = 0;
		p = arrayIndexDeclaration.rangeList;
		while ( p.getClass() == MultipleRange.class )
		{
			lowerBound = ((MultipleRange)p).range.lowerBound.val;
			upperBound = ((MultipleRange)p).range.upperBound.val;
			if ( lowerBound > upperBound )
			{
				IO.displayln("Error: array index lower bound "+lowerBound+" exceeds upper bound "+upperBound);
				arrayBoundsError = true;
			}
			rangeList[i] = lowerBound;
			i++;
			rangeList[i] = upperBound;
			i++;
			p = ((MultipleRange)p).rangeList;
		}
		lowerBound = ((Range)p).lowerBound.val; // p is the last Range
		upperBound = ((Range)p).upperBound.val;
		if ( lowerBound > upperBound )
		{
			IO.displayln("Error: array index lower bound "+lowerBound+" exceeds upper bound "+upperBound);
			arrayBoundsError = true;
		}
		rangeList[i] = lowerBound;
		rangeList[++i] = upperBound;

		if ( arrayBoundsError )
			return null;
		else
			return new Array_Type(primitive_Type, dim, rangeList);
	}
}

import java.util.*;
import alice.tuprolog.*;
import edu.pdx.cecs.suppl.*;

public class NewLibrary extends alice.tuprolog.Library {

    // This is a way to install Prolog code automatically when
    // the library is loaded, avoiding the need to externally
    // load a file.  The string returned by 'getTheory' will
    // be automatically loaded into the interpreter as Prolog code.

    static final String libTheory = "";

    public String getTheory() { return libTheory; }

    SupplEngine engine;

    public NewLibrary(SupplEngine engine) throws InvalidLibraryException {
	this.engine = engine;
    }

    // in this case, the io mode implementation also works for mode ii
    public boolean primOp_ii_2( Term arg1, Term arg2 ) {
	return primOp_io_2(arg1,arg2);
    }

    public boolean primOp_io_2( Term arg1, Term arg2 ) {
	// RULE #1 !!
	arg1 = arg1.getTerm();

	// build some new term
	Struct x = new Struct("mkAsdf", arg1);

	// Make some noise!
	engine.stdOutput("Inside primop! " + x.toString() + "\n");

	// try to unify x with arg2
	return engine.unify(x, arg2);
    }


    // Implement the complex numbers as an abstract data type
    class Complex {
	double real, imaginary;

	public Complex(double real, double imaginary) {
	    this.real = real;
	    this.imaginary = imaginary;
	}

	public double getReal() { return real; }
	public double getImaginary() { return imaginary; }
	
	public Complex add( Complex other ) {
	    return new Complex( this.real + other.real,
				this.imaginary + other.imaginary );
	}
    }
    
    // This primitive holds if the first two arguments represent the
    // complex number given as the third argument
    public boolean complex_coordinates_iii_3( Term arg1, Term arg2, Term arg3 ) {
	arg1 = arg1.getTerm();
	arg2 = arg2.getTerm();
	arg3 = arg3.getTerm();

	if( !((arg1 instanceof alice.tuprolog.Number) 
              && (arg2 instanceof alice.tuprolog.Number)) ) {
	    return false;
	}

	double real = ((alice.tuprolog.Number) arg1).doubleValue();
	double imaginary = ((alice.tuprolog.Number) arg2).doubleValue();

	try {
	    Object ob3 = engine.getRegisteredObject((Struct) arg3);
	    if (!(ob3 instanceof Complex)) { return false; }

	    Complex cpx = (Complex) ob3;

	    // now test the components
	    return ( real == cpx.getReal() && imaginary == cpx.getImaginary() );

	} catch( alice.tuprolog.lib.InvalidObjectIdException ex ) {
	    return false;
	}
    }

    // This primitive predicate constructs a new Complex object from two numeric
    // arguments represeting the real and imaginary coordinates.
    public boolean complex_coordinates_iio_3( Term arg1, Term arg2, Term arg3 ) {
	arg1 = arg1.getTerm();
	arg2 = arg2.getTerm();

	if( !((arg1 instanceof alice.tuprolog.Number) 
              && (arg2 instanceof alice.tuprolog.Number)) ) {
	    return false;
	}

	double real = ((alice.tuprolog.Number) arg1).doubleValue();
	double imaginary = ((alice.tuprolog.Number) arg2).doubleValue();

	Complex cpx = new Complex(real, imaginary);
	Term cpxTerm = engine.registerObject( cpx );

	return engine.unify( arg3, cpxTerm );
    }


    // This primitive decompose the complex number in the third argument
    // unifying arguments 1 and 2 with the real and imaginary parts.
    public boolean complex_coordinates_ooi_3( Term arg1, Term arg2, Term arg3 ) {
	arg1 = arg1.getTerm();
	arg2 = arg2.getTerm();
	arg3 = arg3.getTerm();

	try {
	    Object ob3 = engine.getRegisteredObject((Struct) arg3);
	    if (!(ob3 instanceof Complex)) { return false; }

	    Complex cpx = (Complex) ob3;

	    return engine.unify(arg1, new alice.tuprolog.Double( cpx.getReal() ))
                && engine.unify(arg2, new alice.tuprolog.Double( cpx.getImaginary() ));

	} catch( alice.tuprolog.lib.InvalidObjectIdException ex ) {
	    return false;
	}
    }

    

    // This primitive predicate adds the values of the complex numbers
    // represented by the first two arguments and returns the result
    // in the third argument.
    public boolean complex_add_iio_3( Term arg1, Term arg2, Term arg3 ) {
	arg1 = arg1.getTerm();
	arg2 = arg2.getTerm();

	if( !(arg1 instanceof Struct &&
	      arg2 instanceof Struct) ) {
	    return false;
	}

	try {
	    Object ob1 = engine.getRegisteredObject((Struct) arg1);
	    Object ob2 = engine.getRegisteredObject((Struct) arg2);

	    if (!(ob1 instanceof Complex &&
                  ob2 instanceof Complex)) {
		return false;
	    }

	    Complex c1 = (Complex) ob1;
	    Complex c2 = (Complex) ob2;

	    Complex x = c1.add( c2 );
	    
	    Term xtm = engine.registerObject( x );

	    return engine.unify( xtm, arg3 );

	} catch( alice.tuprolog.lib.InvalidObjectIdException ex ) {
	    return false;
	}
    }

    // This primitive predicate always returns true on objects of the correct type
    // (in other words, this predicate should never fail when run with well-typed
    // policy programs). It has the side-effect of printing the coordinates of the
    // complex object for debugging purposes.
    public boolean print_complex_i_1( Term arg1 ) {
	arg1 = arg1.getTerm();

	if( !(arg1 instanceof Struct) ) {
	    return false;
	}

	try {
	    Object ob1 = engine.getRegisteredObject((Struct) arg1);

	    if (!(ob1 instanceof Complex)) {
		return false;
	    }

	    Complex c1 = (Complex) ob1;
	    
	    engine.stdOutput("inside 'print_complex' ");
	    engine.stdOutput("real: "+ c1.getReal() + "  imaginary: "+ c1.getImaginary() + "\n");

	    return true;
	
	} catch( alice.tuprolog.lib.InvalidObjectIdException ex ) {
	    return false;
	}
    }
}


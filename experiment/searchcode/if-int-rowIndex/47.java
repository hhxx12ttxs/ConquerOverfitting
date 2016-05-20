import java.util.Vector;

/**
 *
 *  This class implements a Matrix class especialy used for 
 *  the Quadratic Sieve Algorithm. This class is in this state 
 *  not useful for further purposes.
 *
 *  @author Kristijan Dragicevic
 *  @version 05.12.2004
 *
 *
 */

class Matrix {
    
    Vector column = new Vector();
    Vector row = new Vector();
    Number initial;
    Vector newSol;
    boolean isOne;

    public int howMuch1 = 0;
    public int i1 = 0;
    public int help1 = 0;
    public int nSsize = 0;

    public Vector Matrix() {        // constructor implemented for special purpose in QuadAlg.java
	row.addElement(column);
	init(1, 1, new Integer(0));
	return row;
    }

    public void setSize(int rows, int columns) { // sets size of matrix to (rows x columns)
	Vector help = new Vector();              // previous elements get cut if they are not in range anymore
	int helpi;                               // additional elements are set to null
	help.addElement(initial);
	if (rows < row.size())
	    row.setSize(rows);
	else {
	    for (int i = row.size(); i != rows; i++) 
		row.addElement(help);
	}

	for (int j = 0; j != row.size(); j++) {
	    helpi = ((Vector)row.elementAt(j)).size();
	    if (columns < helpi)
		((Vector)row.elementAt(j)).setSize(columns);
	    else {
		for (int k = helpi; k != columns; k++) 
		    ((Vector)row.elementAt(j)).addElement(initial);
	    }
	}
    }

    public Number get(int rowIndex, int columnIndex) {  // get value at element(rowIndex, columnIndex)
	return (Number)((Vector)row.elementAt(rowIndex)).get(columnIndex);
    }

    public Vector getRow(int rowIndex) {  //get whole row at index rowIndex
	return (Vector)row.get(rowIndex);
    }

    public void setRow(int rowIndex, Vector vec) { // set whole row at index rowIndex to vec; 
	row.set(rowIndex, vec);                    // there is no exception for Csize() != vec.size() !!!
    }

    public void set(int rowIndex, int columnIndex, Number value) { // set elementAt(rowIndex, columnIndex) to value
	Vector help = new Vector((Vector)row.elementAt(rowIndex));
	help.setElementAt(value, columnIndex);
	row.setElementAt(help, rowIndex);
    }

    public void clear() {           // set to null- matrix
	row.clear();
    }

    public int Rsize() {            // number of rows
	return row.size();
    }

    public int Csize() {            // number of columns
	return ((Vector)row.firstElement()).size();
    }

    public void ZERO {            // sets all elements to zero
	for (int i = 0; i != Rsize(); i++) {
	    for (int j = 0; j != Csize(); j++) 
		set(i, j, new Integer(0));
	}
    }

    public boolean isZeroRow(int index) {     // checks wether row with index index is zero-vector
	if (getRow(index) == zeroVector()) 
	    return true;
	else 
	    return false;
    }

    public void init(int r, int c, Number o) { // see constructor
	initial = o;
	clear();
	Vector help = new Vector();
	for (int i = 0; i != r; i++) {
	    for (int j = 0; j != c; j++)
		help.addElement(o);
	    row.addElement(new Vector(help));
	    help.clear();
	}
    }

    public void nullToValue(Number o) {       // sets all elements in matrix whixh are null to o
	for (int i = 0; i != Rsize(); i++) {
	    for (int j = 0; j != Csize(); j++) {
		if (get(i, j) == null)
		    set(i, j, o);
	    }
	}
    }

    public void scale(Number n) {             // scales whole Matrix by n
	Number x;
	for (int i = 0; i != Rsize(); i++) {
	    for (int j = 0; j != Csize(); j++) {
		x = get(i, j);
		x = mulNums(x, n);
		set(i, j, x);
	    }
	}
    }

    public void scaleRow(int r, Number n) {   // scales row with index r by n
	Number x;
	for (int i = 0; i != Csize(); i++) {
	    x = get(r, i);
	    x = mulNums(x, n);
	    set(r, i, x);
	}
    }

    public void addScaledRow(Number s, int r1, int r2) {  // adds row with index r1 scaled by s to r2
	Number x;
	Vector help = new Vector((Vector)row.elementAt(r1));

	if (s.doubleValue() != 1) {
	    for (int j = 0; j != help.size(); j++) {
		x = mulNums((Number)help.elementAt(j), s);
		help.setElementAt(x, j);
	    }
	}

	for (int i = 0; i != Csize(); i++) {
	    x = (Number)help.elementAt(i);
	    x = addNums(x, get(r2, i));
	    set(r2, i, x);
	}
    }

    public Matrix add(Matrix m) {      // returns sum of m and current matrix
	Matrix res = new Matrix();
	Number o1, o2;
	res.setSize(Rsize(), Csize());
	for (int i = 0; i != Rsize(); i++) {
	    for (int j = 0; j != Csize(); j++) {
		o1 = get(i, j);
		o2 = m.get(i, j);
		res.set(i, j, addNums(o1, o2));
	    }
	}
	return res;
    }

    public Matrix mulFromRight(Matrix m) {  
	Matrix res = new Matrix();
	Number o2, o3;
	res.setSize(Rsize(), m.Csize());
	for (int i = 0; i != res.Rsize(); i++) {
	    for (int j = 0; j != res.Csize(); j++) {
		o3 = mulNums(get(i, 0), m.get(0, j));
		for (int k = 1; k != Csize(); k++) {
		    o2 = mulNums(get(i, k), m.get(k, j));
		    o3 = addNums(o3, o2);
		}
		res.set(i, j, o3);
	    }
	}
	return res;
    }

    public void triangle() {
	Integer i = new Integer(1);
	Double d = new Double(0.5);
	if (getBaseClass() == i.getClass())
	    intTriangle();
	if(getBaseClass() == d.getClass());
    }

    public void transpon() {
	int helpMax = max(Rsize(), Csize());
	int helpCsize = Csize();
	int helpRsize = Rsize();
	Integer help;

	setSize(helpMax, helpMax);
	for (int i = 1; i != helpMax; i++) {
	    for (int j = 0; j != i; j++) {
		help = ((Integer)get(i, j));
		set(i, j, (Integer)get(j, i));
		set(j, i, help);
	    }
	}
	setSize(helpCsize, helpRsize);
    }

    private void intTriangle() {
	Number firstElement, secondElement, GrCoDiv, mul1, mul2;
	Vector help = new Vector();
	int till = min(Rsize(), Csize()), z = 0;

	for (int i = 0; i != till; i++) {
	     for (int j = i + 1; j < Rsize(); j++) {
		     secondElement = (get(j, i));
		     firstElement = (get(i, i));	
	
		if (secondElement.intValue() == 0);
		else if(firstElement.intValue() == 0) {
		    help = new Vector((Vector)row.get(j));
		    row.set(j, (Vector)row.get(i));
		    row.set(i, help);
		}
		else {
		    GrCoDiv = euclide(firstElement, secondElement);
		    mul1 = divNums(firstElement, GrCoDiv);
		    mul2 = divNums(secondElement, GrCoDiv);		
		    for(int k = i; k < Csize(); k++) {
			firstElement = get(i, k);
			secondElement = get(j, k);
			secondElement = mulNums(secondElement, mul1);
			secondElement = subNums(secondElement, 
						mulNums(firstElement, mul2));
			set(j, k, secondElement);
		    }
		}
	     }
	     removeEquals(i);
	}
	lowAbs();
    }

    public void dualTriangle() {
	modulo(new Integer(2));
	Number firstElement, secondElement;
	Vector help = new Vector();
	int till = min(Rsize(), Csize()), z = 0;

	for (int i = 0; i != till; i++) {
	     for (int j = i + 1; j < Rsize(); j++) {
		     secondElement = (get(j, i));
		     firstElement = (get(i, i));	
	
		if (secondElement.intValue() == 0);
		else if(firstElement.intValue() == 0) {
		    help = new Vector((Vector)row.get(j));
		    row.set(j, (Vector)row.get(i));
		    row.set(i, help);
		}
		else {
		    for(int k = i; k < Csize(); k++) {
			firstElement = get(i, k);
			secondElement = get(j, k);
			secondElement = addNums(firstElement, secondElement);
			if (secondElement.intValue() == 2)
			    secondElement = new Integer(0);
			set(j, k, secondElement);
		    }
		}
	     }
	     removeEquals(i);
	}
	lowAbs();
    }

    public void modTriangle(Number a) {
	intTriangle();
	if (a.intValue() != 0)
	    modulo(a);
       	intTriangle();
	if (a.intValue() != 0)
	modulo(a);
    }

    public Matrix dualSol() {
	Matrix solution = new Matrix();
	Vector currentSol = new Vector();
	Integer Int = new Integer(2);
	int combs = 1, prevCombs, compIndex;
	boolean done = false;

	dualTriangle();
	Int = new Integer(-1);
	solution.setSize(combs, Csize());
	solution.nullToValue(Int);

	for (int i = Rsize() - 1; i != -1; i--) {
	    prevCombs = solution.Rsize();
	    for (int j = 0; j != prevCombs; j++) {
		combs = getDualNumSol(getRow(i), solution.getRow(j));
		if(!done && combs != 0) {
		    solution.setSize(combs * prevCombs, Csize());
		    done = !done;
		}
		for (int k = 0; k != combs; k++) {
		    currentSol = findComb(k, solution.getRow(j));
		    if (k != combs - 1) {
			compIndex = prevCombs + (j * combs) + k - j;
			solution.setRow(compIndex, currentSol);
		    }
		    else 
			solution.setRow(j, currentSol);

		}
	    }
	    if (done)
		done = !done;
	}

	for (int i = 0; i != solution.Csize(); i++) {
	    if (((Integer)solution.get(1, i)).intValue() == -1 ) {
		prevCombs = solution.Rsize();
		for (int j = 0; j != prevCombs; j++) {
		    combs = 2;
		    if(!done) {
			solution.setSize(combs * prevCombs, Csize());
			done = !done;
		    }
		    for (int k = 0; k != combs; k++) {
			currentSol = modifyNewSol(i, solution.getRow(j), k);
			if (k != 1) {
			    compIndex = prevCombs + ( j * combs ) + k - j;
			    solution.setRow(compIndex, currentSol);
			}
			else
			    solution.setRow(j, currentSol);
		    }
		}
	    }
	    if (done)
		done = !done;
	}
	solution.order();
	return solution;
    }

    public void order() {
	int helpSum;
	Vector help = new Vector();
	for (int i = 0; i != Rsize() - 1; i++) {
	    for (int j = i + 1; j != Rsize(); j++) {
		if (rowSum(i) > rowSum(j)) {
		    help = getRow(i);
		    setRow(i, getRow(j));
		    setRow(j, help);
		}
	    }
	}

    }

    private Vector modifyNewSol(int i, Vector solution, int k) {
	Vector res = new Vector();
	for (int l = 0; l != solution.size(); l++) {
	    res.addElement((Integer)solution.get(l));
	}
	if (k == 0)
	    res.set(i, new Integer(0));
	else
	    res.set(i, new Integer(1));
	return res;
    }

    
    private int rowSum(int index) {
	int sum = 0;
	for (int i = 0; i != Csize(); i++) 
	    sum += ((Integer)get(index, i)).intValue();
	return sum;
    }


    private int getDualNumSol(Vector solution, Vector remain) {
	int count = 0, res;
	newSol = new Vector();
	isOne = false;

	for (int i = 0; i != solution.size(); i++) {
	    if ( ((Integer)solution.elementAt(i)).intValue() == 1 ) {
		if ( ((Integer)remain.elementAt(i)).intValue() == -1 ) {
		    count++;
		    newSol.addElement(new Integer(i));
		}
		if( ((Integer)remain.elementAt(i)).intValue() == 1 ) 
		    isOne = !isOne;
	    }
	}
	if (count == 0)
	    res = 0;
	if (count == 1)
	    res = 1;
	else
	    res = power(2, count - 1);
	return res;
    }

    private Vector findComb(int i, Vector old) {
	Vector vec = new Vector();
	Vector res = new Vector(old);
	int howMuch, z = 0, help = 0;
	Integer help2 = new Integer(0);

	if (isOne) { 
	    howMuch = 1;
	    help = ueber(newSol.size(), howMuch);
	    while ( help < i + 1) {
		howMuch += 2;
		z = help;
		help += ueber(newSol.size(), howMuch);
	    }
	    vec = take(howMuch, i + 1 - z, newSol);
	}

	else {
	    howMuch = 0;
	    help = ueber(newSol.size(), howMuch);
	    while (help < i + 1) {
		howMuch += 2;
		z = help;
		help = ueber(newSol.size(), howMuch) + help;
	    }
	    vec = take(howMuch, i + 1 - z, newSol);
	}

	for(int j = 0; j != newSol.size(); j++) {
	    help2 = (Integer)newSol.get(j);

	    if (vec.contains(help2))
		res.set(help2.intValue(), new Integer(1));
	    else 
		res.set(help2.intValue(), new Integer(0));
	}

	return res;
    }

    public void lowAbs() {
	Number GrCoDiv, help;
	for (int i = 0; i != Rsize(); i++) {
	    GrCoDiv = get(i, 0);
	    for (int j = 1; j != Csize(); j++) {
		help = get(i, j);
		GrCoDiv = euclide(GrCoDiv, help);
	    }
	    if (GrCoDiv.intValue() != 1 && GrCoDiv.intValue() != 0) {
		for (int k = 0; k != Csize(); k++) {
		    help = get(i, k);
		    help = divNums(help, GrCoDiv);
		    set(i, k, help);
		}
	    }
	}
    }

    public void modulo(Number a) {
	Number help;
	for (int i = 0; i != Rsize(); i++) {
	    for (int j = 0; j != Csize(); j++){
		help = get(i, j);
		help = modNums(help, a);
		while (help.intValue() < 0)
		    help = addNums(help, a);
		set(i, j, help);
	    }
	}
    }


    public Class getBaseClass() {
	Object o;
	o = get(0, 0);
	return o.getClass();
    }

    private Number addNums(Number a, Number b) {
      	Integer Int = new Integer(0); 
	Double Dou = new Double(0.0);
	if (a.getClass() == Int.getClass()) {
	    Int = new Integer (a.intValue() + b.intValue());
	    return Int;
	}
	if (a.getClass() == Dou.getClass()) {
	    Dou = new Double (a.doubleValue() + b.doubleValue());
	    return Dou;
	}
	return Int;
    }

    private Number subNums(Number a, Number b) {
	Integer Int = new Integer(0); 
	Double Dou = new Double(0.0);
	if (a.getClass() == Int.getClass()) {
	    Int = new Integer(-b.intValue());
	    return addNums(a, Int);
	}
	if (a.getClass() == Dou.getClass()) {
	    Dou = new Double (-b.doubleValue());
	    return addNums(a, Dou);
	}
	return Int;
    }

    private Number mulNums(Number a, Number b) {
      	Integer Int = new Integer(0); 
	Double Dou = new Double(0.0);
	if (a.getClass() == Int.getClass()) {
	    Int = new Integer (a.intValue() * b.intValue());
	    return Int;
	}
	if (a.getClass() == Dou.getClass()) {
	    Dou = new Double (a.doubleValue() * b.doubleValue());
	    return Dou;
	}
	return Int;
    }

    private Number divNums(Number a, Number b) {
	Integer Int = new Integer(0);
	Double Dou = new Double(0.0);
	if (a.getClass() == Int.getClass()) {
	    Int = new Integer (a.intValue() / b.intValue());
	    return Int;
	}
	if (a.getClass() == Dou.getClass()) {
	    Dou = new Double (a.doubleValue() / b.doubleValue());
	    return Dou;
	}
	return Int;
    }

    private Number modNums(Number a, Number b) {
	Integer Int = new Integer(a.intValue() % b.intValue());
	return Int;
    }

    private int power(int a, int b) {
	double help;
	help = Math.pow((double)a, (double)b);
	return (int)help;
    }

    private int fakultaet(int a) {
	int res = 1;
	if (a == 0 || a == 1) 
	    return 1;
	for (int i = 2; i < a + 1; i++)
	    res *= i;
	return res;
    }

    private int ueber(int a, int b) {
	int res, help = 1;

	if (b == 0 || b == a)
	    return 1;
	if (b == 1 || b == a - 1)
	    return a;

	if (b < a - b) {
	    for(int i = a - b + 1; i != a + 1; i++)
		help *= i;
	    res = help / fakultaet(b);
	}
	else {
	    for (int i = b + 1; i != a + 1; i++)
		help *= i;
	    res = help / fakultaet(a - b);
	}
	return res;
    }

    private Vector take(int howMuch, int whichComb, Vector vec) {
	Vector help = new Vector(), res = new Vector();
	int Int, Int1;
	Integer lastHelp = new Integer(0);

	if (howMuch == 0)
	    return res;

	for (int i = 0; i != vec.size(); i++) {
	    if (i < howMuch)
		help.addElement(new Integer(1));
	    else
		help.addElement(new Integer(0));
	}

	for (int j = 1; j != whichComb; j++) {
	    Int = help.lastIndexOf(new Integer(1));
	    if (Int != vec.size() - 1) {
		help.set(Int, new Integer(0));
		help.set(Int + 1, new Integer(1));
	    }
	    else {
		Int = help.lastIndexOf(new Integer(0));
		Int1 = help.size() - Int;
		Int = help.lastIndexOf(new Integer(1), Int);
		help.set(Int, new Integer(0));

		for (int k = Int + 1; k != help.size(); k++) 
		    if (k - Int1 <= Int)
			help.set(k, new Integer(1));
		    else 
			help.set(k, new Integer(0));
	    }
	}

	for (int i = 0; i != vec.size(); i++) {
	    if ( ((Integer)help.elementAt(i)).intValue() == 1 ) {
		lastHelp = (Integer)vec.get(i);
		res.addElement(lastHelp);
	    }
	}

	return res;
    }

    private Number euclide(Number a, Number b){
	int x1 = 0, x2 = 1, y1 = 1, y2 = 0, q, r, x, y;
	int amb = a.intValue(), num = b.intValue();

	if(amb == 0)
	    return new Integer(num);

	if (amb < 0)
	    amb = -amb;

	if (num < 0)
	    num = -num;

	while(amb > 0){
	    q = num / amb;
	    r = num - q * amb;
	    x = x2 - q * x1;
	    y = y2 - q * y1;
	    num = amb;
	    amb = r;
	    x2 = x1;
	    x1 = x;
	    y2 = y1;
	    y1 = y;
	}
	return new Integer(num);
    }

    private int min(int a, int b) {
	if (a < b)
	    return a;
	else
	    return b;
    }

    private int max(int a, int b) {
	if (a < b)
	    return b;
	else 
	    return a;
    }

    private void removeEquals(int from) {
	for (int i = from; i != Rsize() - 1; i++) {
	    for (int j = i + 1; j != Rsize(); j++) {
		if (equalVec(getRow(i), getRow(j))) {
		    for (int k = j; k != Rsize() - 1; k++)
			setRow(k, getRow(k + 1));
		    setRow(Rsize() - 1, zeroVector());
		}
	    }
	}
    }    

    private boolean equalVec(Vector v1, Vector v2) {
	for (int i = 0; i != v1.size(); i++) {
	    if (((Integer)v1.get(i)).intValue() 
		!= ((Integer)v2.get(i)).intValue())
		return false;
	}
	return true;
    }

    private Vector zeroVector() {
	Vector vec = new Vector();
	for (int i = 0; i != Csize(); i++) 
	    vec.addElement(new Integer(0));
	return vec;
    }
}




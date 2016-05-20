package Practice.Math;

public class Matrix4f {

	/**
	 * 4x4 Matrix stored as an array of floats from values 0-15 where
	 * Matrix M = [ 0 4  8 12 ]
	 *            [ 1 5  9 13 ]
	 *            [ 2 6 10 14 ]
	 *            [ 3 7 11 15 ]
	 */
	public float[] matrix = new float[16];
	private final static float PI_VALUE = 3.141592654f;

	/**
	 * Constructor, creates a new empty Matrix with just the identity matrix loaded.
	 */
	public Matrix4f() {
		setIdentity();
	}
	
	/**
	 * Constructor that creates a new matrix with the same values of the Matrix4f object passed in.
	 * @param m Matrix4f
	 */
	public Matrix4f(Matrix4f m) {
		this(m.matrix[0], m.matrix[1], m.matrix[2], m.matrix[3], m.matrix[4],
				m.matrix[5], m.matrix[6], m.matrix[7], m.matrix[8],
				m.matrix[9], m.matrix[10], m.matrix[11], m.matrix[12],
				m.matrix[13], m.matrix[14], m.matrix[15]);
	}

	/**
	 * Constructor that creates a new matrix with values specified by an array of floats passed in.
	 * @param m float[]
	 */
	public Matrix4f(float[] m) {
		this(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8], m[9], m[10],
				m[11], m[12], m[13], m[14], m[15]);
	}
	
	/**
	 * Constructor that creates a new matrix with each value passed in as a float value.
	 * 
	 */
	public Matrix4f(float r11, float r12, float r13, float r14, float r21,
			float r22, float r23, float r24, float r31, float r32, float r33,
			float r34, float r41, float r42, float r43, float r44) {

		matrix[0] = r11; matrix[4] = r21; matrix[8] = r31;  matrix[12] = r41;
		matrix[1] = r12; matrix[5] = r22; matrix[9] = r32;  matrix[13] = r42;
		matrix[2] = r13; matrix[6] = r23; matrix[10] = r33; matrix[14] = r43;
		matrix[3] = r14; matrix[7] = r24; matrix[11] = r34; matrix[15] = r44;
	}
	
	public void set(int x, int y, float value) {
		this.matrix[x+(y*4)] = value;
	}

	/**
	 * Reset the matrix by loading 1's across the diagonal.
	 * [ 1 0 0 0 ]
	 * [ 0 1 0 0 ]
	 * [ 0 0 1 0 ]
	 * [ 0 0 0 1 ]
	 */
	public void setIdentity() {
		matrix[0] = 1.0f; matrix[4] = 0.0f; matrix[8] = 0.0f;  matrix[12] = 0.0f;
		matrix[1] = 0.0f; matrix[5] = 1.0f; matrix[9] = 0.0f;  matrix[13] = 0.0f;
		matrix[2] = 0.0f; matrix[6] = 0.0f; matrix[10] = 1.0f; matrix[14] = 0.0f;
		matrix[3] = 0.0f; matrix[7] = 0.0f; matrix[11] = 0.0f; matrix[15] = 1.0f;
	}
	
	// Matrix Addition Functions
	
	public static Matrix4f addMatrix(Matrix4f m1, Matrix4f m2){
		Matrix4f mSum = new Matrix4f();
		mSum.matrix[0] = m1.matrix[0] + m2.matrix[0];
		mSum.matrix[1] = m1.matrix[1] + m2.matrix[1];
		mSum.matrix[2] = m1.matrix[2] + m2.matrix[2];
		mSum.matrix[3] = m1.matrix[3] + m2.matrix[3];
		mSum.matrix[4] = m1.matrix[4] + m2.matrix[4];
		mSum.matrix[5] = m1.matrix[5] + m2.matrix[5];
		mSum.matrix[6] = m1.matrix[6] + m2.matrix[6];
		mSum.matrix[7] = m1.matrix[7] + m2.matrix[7];
		mSum.matrix[8] = m1.matrix[8] + m2.matrix[8];
		mSum.matrix[9] = m1.matrix[9] + m2.matrix[9];
		mSum.matrix[10] = m1.matrix[10] + m2.matrix[10];
		mSum.matrix[11] = m1.matrix[11] + m2.matrix[11];
		mSum.matrix[12] = m1.matrix[12] + m2.matrix[12];
		mSum.matrix[13] = m1.matrix[13] + m2.matrix[13];
		mSum.matrix[14] = m1.matrix[14] + m2.matrix[14];
		mSum.matrix[15] = m1.matrix[15] + m2.matrix[15];
		
		return mSum;
	}
	
	public Matrix4f addMatrix(Matrix4f m1){
		Matrix4f mSum = new Matrix4f();
		mSum.matrix[0] = this.matrix[0] + m1.matrix[0];
		mSum.matrix[1] = this.matrix[1] + m1.matrix[1];
		mSum.matrix[2] = this.matrix[2] + m1.matrix[2];
		mSum.matrix[3] = this.matrix[3] + m1.matrix[3];
		mSum.matrix[4] = this.matrix[4] + m1.matrix[4];
		mSum.matrix[5] = this.matrix[5] + m1.matrix[5];
		mSum.matrix[6] = this.matrix[6] + m1.matrix[6];
		mSum.matrix[7] = this.matrix[7] + m1.matrix[7];
		mSum.matrix[8] = this.matrix[8] + m1.matrix[8];
		mSum.matrix[9] = this.matrix[9] + m1.matrix[9];
		mSum.matrix[10] = this.matrix[10] + m1.matrix[10];
		mSum.matrix[11] = this.matrix[11] + m1.matrix[11];
		mSum.matrix[12] = this.matrix[12] + m1.matrix[12];
		mSum.matrix[13] = this.matrix[13] + m1.matrix[13];
		mSum.matrix[14] = this.matrix[14] + m1.matrix[14];
		mSum.matrix[15] = this.matrix[15] + m1.matrix[15];
		
		return mSum;
	}
	
	public Matrix4f addtoMatrix(float num){
		Matrix4f mSum = new Matrix4f();
		mSum.matrix[0] = this.matrix[0] + num;
		mSum.matrix[1] = this.matrix[1] + num;
		mSum.matrix[2] = this.matrix[2] + num;
		mSum.matrix[3] = this.matrix[3] + num;
		mSum.matrix[4] = this.matrix[4] + num;
		mSum.matrix[5] = this.matrix[5] + num;
		mSum.matrix[6] = this.matrix[6] + num;
		mSum.matrix[7] = this.matrix[7] + num;
		mSum.matrix[8] = this.matrix[8] + num;
		mSum.matrix[9] = this.matrix[9] + num;
		mSum.matrix[10] = this.matrix[10] + num;
		mSum.matrix[11] = this.matrix[11] + num;
		mSum.matrix[12] = this.matrix[12] + num;
		mSum.matrix[13] = this.matrix[13] + num;
		mSum.matrix[14] = this.matrix[14] + num;
		mSum.matrix[15] = this.matrix[15] + num;
		
		return mSum;
	}
	
	// Matrix Subtraction Functions
	
	public static Matrix4f subtractMatrix(Matrix4f mLeft, Matrix4f mRight){
		Matrix4f mMatrix = new Matrix4f();
		mMatrix.matrix[0] = mLeft.matrix[0] - mRight.matrix[0];
		mMatrix.matrix[1] = mLeft.matrix[1] - mRight.matrix[1];
		mMatrix.matrix[2] = mLeft.matrix[2] - mRight.matrix[2];
		mMatrix.matrix[3] = mLeft.matrix[3] - mRight.matrix[3];
		mMatrix.matrix[4] = mLeft.matrix[4] - mRight.matrix[4];
		mMatrix.matrix[5] = mLeft.matrix[5] - mRight.matrix[5];
		mMatrix.matrix[6] = mLeft.matrix[6] - mRight.matrix[6];
		mMatrix.matrix[7] = mLeft.matrix[7] - mRight.matrix[7];
		mMatrix.matrix[8] = mLeft.matrix[8] - mRight.matrix[8];
		mMatrix.matrix[9] = mLeft.matrix[9] - mRight.matrix[9];
		mMatrix.matrix[10] = mLeft.matrix[10] - mRight.matrix[10];
		mMatrix.matrix[11] = mLeft.matrix[11] - mRight.matrix[11];
		mMatrix.matrix[12] = mLeft.matrix[12] - mRight.matrix[12];
		mMatrix.matrix[13] = mLeft.matrix[13] - mRight.matrix[13];
		mMatrix.matrix[14] = mLeft.matrix[14] - mRight.matrix[14];
		mMatrix.matrix[15] = mLeft.matrix[15] - mRight.matrix[15];
		
		return mMatrix;
	}
	
	public Matrix4f subtractMatrix(Matrix4f mRight){
		Matrix4f mMatrix = new Matrix4f();
		mMatrix.matrix[0] = this.matrix[0] - mRight.matrix[0];
		mMatrix.matrix[1] = this.matrix[1] - mRight.matrix[1];
		mMatrix.matrix[2] = this.matrix[2] - mRight.matrix[2];
		mMatrix.matrix[3] = this.matrix[3] - mRight.matrix[3];
		mMatrix.matrix[4] = this.matrix[4] - mRight.matrix[4];
		mMatrix.matrix[5] = this.matrix[5] - mRight.matrix[5];
		mMatrix.matrix[6] = this.matrix[6] - mRight.matrix[6];
		mMatrix.matrix[7] = this.matrix[7] - mRight.matrix[7];
		mMatrix.matrix[8] = this.matrix[8] - mRight.matrix[8];
		mMatrix.matrix[9] = this.matrix[9] - mRight.matrix[9];
		mMatrix.matrix[10] = this.matrix[10] - mRight.matrix[10];
		mMatrix.matrix[11] = this.matrix[11] - mRight.matrix[11];
		mMatrix.matrix[12] = this.matrix[12] - mRight.matrix[12];
		mMatrix.matrix[13] = this.matrix[13] - mRight.matrix[13];
		mMatrix.matrix[14] = this.matrix[14] - mRight.matrix[14];
		mMatrix.matrix[15] = this.matrix[15] - mRight.matrix[15];
		
		return mMatrix;
	}
	
	public Matrix4f subtractfromMatrix(float num){
		Matrix4f mMatrix = new Matrix4f();
		mMatrix.matrix[0] = this.matrix[0] - num;
		mMatrix.matrix[1] = this.matrix[1] - num;
		mMatrix.matrix[2] = this.matrix[2] - num;
		mMatrix.matrix[3] = this.matrix[3] - num;
		mMatrix.matrix[4] = this.matrix[4] - num;
		mMatrix.matrix[5] = this.matrix[5] - num;
		mMatrix.matrix[6] = this.matrix[6] - num;
		mMatrix.matrix[7] = this.matrix[7] - num;
		mMatrix.matrix[8] = this.matrix[8] - num;
		mMatrix.matrix[9] = this.matrix[9] - num;
		mMatrix.matrix[10] = this.matrix[10] - num;
		mMatrix.matrix[11] = this.matrix[11] - num;
		mMatrix.matrix[12] = this.matrix[12] - num;
		mMatrix.matrix[13] = this.matrix[13] - num;
		mMatrix.matrix[14] = this.matrix[14] - num;
		mMatrix.matrix[15] = this.matrix[15] - num;
		
		return mMatrix;
	}
	

	public Matrix4f multiplyMatrix(Matrix4f m) {
		// Return the value of this Matrix * m.
		return new Matrix4f(matrix[0] * m.matrix[0] + matrix[4] * m.matrix[1]
				+ matrix[8] * m.matrix[2] + matrix[12] * m.matrix[3], matrix[1]
				* m.matrix[0] + matrix[5] * m.matrix[1] + matrix[9]
				* m.matrix[2] + matrix[13] * m.matrix[3], matrix[2]
				* m.matrix[0] + matrix[6] * m.matrix[1] + matrix[10]
				* m.matrix[2] + matrix[14] * m.matrix[3], matrix[3]
				* m.matrix[0] + matrix[7] * m.matrix[1] + matrix[11]
				* m.matrix[2] + matrix[15] * m.matrix[3], matrix[0]
				* m.matrix[4] + matrix[4] * m.matrix[5] + matrix[8]
				* m.matrix[6] + matrix[12] * m.matrix[7], matrix[1]
				* m.matrix[4] + matrix[5] * m.matrix[5] + matrix[9]
				* m.matrix[6] + matrix[13] * m.matrix[7], matrix[2]
				* m.matrix[4] + matrix[6] * m.matrix[5] + matrix[10]
				* m.matrix[6] + matrix[14] * m.matrix[7], matrix[3]
				* m.matrix[4] + matrix[7] * m.matrix[5] + matrix[11]
				* m.matrix[6] + matrix[15] * m.matrix[7], matrix[0]
				* m.matrix[8] + matrix[4] * m.matrix[9] + matrix[8]
				* m.matrix[10] + matrix[12] * m.matrix[11], matrix[1]
				* m.matrix[8] + matrix[5] * m.matrix[9] + matrix[9]
				* m.matrix[10] + matrix[13] * m.matrix[11], matrix[2]
				* m.matrix[8] + matrix[6] * m.matrix[9] + matrix[10]
				* m.matrix[10] + matrix[14] * m.matrix[11], matrix[3]
				* m.matrix[8] + matrix[7] * m.matrix[9] + matrix[11]
				* m.matrix[10] + matrix[15] * m.matrix[11], matrix[0]
				* m.matrix[12] + matrix[4] * m.matrix[13] + matrix[8]
				* m.matrix[14] + matrix[12] * m.matrix[15], matrix[1]
				* m.matrix[12] + matrix[5] * m.matrix[13] + matrix[9]
				* m.matrix[14] + matrix[13] * m.matrix[15], matrix[2]
				* m.matrix[12] + matrix[6] * m.matrix[13] + matrix[10]
				* m.matrix[14] + matrix[14] * m.matrix[15], matrix[3]
				* m.matrix[12] + matrix[7] * m.matrix[13] + matrix[11]
				* m.matrix[14] + matrix[15] * m.matrix[15]);

	}

	public Matrix4f divideMatrix(Matrix4f m) {
		// Return the value of this Matrix / m.
		return new Matrix4f(matrix[0] / m.matrix[0] + matrix[4] / m.matrix[1]
				+ matrix[8] / m.matrix[2] + matrix[12] / m.matrix[3], matrix[1]
				/ m.matrix[0] + matrix[5] / m.matrix[1] + matrix[9]
				/ m.matrix[2] + matrix[13] / m.matrix[3], matrix[2]
				/ m.matrix[0] + matrix[6] / m.matrix[1] + matrix[10]
				/ m.matrix[2] + matrix[14] / m.matrix[3], matrix[3]
				/ m.matrix[0] + matrix[7] / m.matrix[1] + matrix[11]
				/ m.matrix[2] + matrix[15] / m.matrix[3], matrix[0]
				/ m.matrix[4] + matrix[4] / m.matrix[5] + matrix[8]
				/ m.matrix[6] + matrix[12] / m.matrix[7], matrix[1]
				/ m.matrix[4] + matrix[5] / m.matrix[5] + matrix[9]
				/ m.matrix[6] + matrix[13] / m.matrix[7], matrix[2]
				/ m.matrix[4] + matrix[6] / m.matrix[5] + matrix[10]
				/ m.matrix[6] + matrix[14] / m.matrix[7], matrix[3]
				/ m.matrix[4] + matrix[7] / m.matrix[5] + matrix[11]
				/ m.matrix[6] + matrix[15] / m.matrix[7], matrix[0]
				/ m.matrix[8] + matrix[4] / m.matrix[9] + matrix[8]
				/ m.matrix[10] + matrix[12] / m.matrix[11], matrix[1]
				/ m.matrix[8] + matrix[5] / m.matrix[9] + matrix[9]
				/ m.matrix[10] + matrix[13] / m.matrix[11], matrix[2]
				/ m.matrix[8] + matrix[6] / m.matrix[9] + matrix[10]
				/ m.matrix[10] + matrix[14] / m.matrix[11], matrix[3]
				/ m.matrix[8] + matrix[7] / m.matrix[9] + matrix[11]
				/ m.matrix[10] + matrix[15] / m.matrix[11], matrix[0]
				/ m.matrix[12] + matrix[4] / m.matrix[13] + matrix[8]
				/ m.matrix[14] + matrix[12] / m.matrix[15], matrix[1]
				/ m.matrix[12] + matrix[5] / m.matrix[13] + matrix[9]
				/ m.matrix[14] + matrix[13] / m.matrix[15], matrix[2]
				/ m.matrix[12] + matrix[6] / m.matrix[13] + matrix[10]
				/ m.matrix[14] + matrix[14] / m.matrix[15], matrix[3]
				/ m.matrix[12] + matrix[7] / m.matrix[13] + matrix[11]
				/ m.matrix[14] + matrix[15] / m.matrix[15]);

	}

	public Matrix4f subtract(float f) {
		return new Matrix4f(matrix[0] - f, matrix[1] - f, matrix[2] - f,
				matrix[3] - f, matrix[4] - f, matrix[5] - f, matrix[6] - f,
				matrix[7] - f, matrix[8] - f, matrix[9] - f, matrix[10] - f,
				matrix[11] - f, matrix[12] - f, matrix[13] - f, matrix[14] - f,
				matrix[15] - f);
	}


	public Matrix4f multiply(float f) {
		return new Matrix4f(matrix[0] * f, matrix[1] * f, matrix[2] * f,
				matrix[3] * f, matrix[4] * f, matrix[5] * f, matrix[6] * f,
				matrix[7] * f, matrix[8] * f, matrix[9] * f, matrix[10] * f,
				matrix[11] * f, matrix[12] * f, matrix[13] * f, matrix[14] * f,
				matrix[15] * f);
	}

	public Matrix4f divide(float f) {
		// Return the value of this vector / f. We do this by multiplying the
		// recip.
		if (f == 0)
			f = 1;

		f = 1 / f;

		return new Matrix4f(matrix[0] * f, matrix[1] * f, matrix[2] * f,
				matrix[3] * f, matrix[4] * f, matrix[5] * f, matrix[6] * f,
				matrix[7] * f, matrix[8] * f, matrix[9] * f, matrix[10] * f,
				matrix[11] * f, matrix[12] * f, matrix[13] * f, matrix[14] * f,
				matrix[15] * f);
	}


	public boolean equals(Matrix4f m) {
		// Return true if all equal each other, false if one or more don't.
		for (int i = 0; i < 16; i++) {
			if (matrix[i] != m.matrix[i])
				return false;
		}

		return true;
	}

	public void zero() {
		// To set the matrix to zero you set all the values in the matrix like
		// so...
		matrix[0] = 0.0f;
		matrix[1] = 0.0f;
		matrix[2] = 0.0f;
		matrix[3] = 0.0f;
		matrix[4] = 0.0f;
		matrix[5] = 0.0f;
		matrix[6] = 0.0f;
		matrix[7] = 0.0f;
		matrix[8] = 0.0f;
		matrix[9] = 0.0f;
		matrix[10] = 0.0f;
		matrix[11] = 0.0f;
		matrix[12] = 0.0f;
		matrix[13] = 0.0f;
		matrix[14] = 0.0f;
		matrix[15] = 0.0f;
	}

	public void translate(Vector4f translate) {
		setIdentity();

		// To translate a 4x4 matrix you must replace the bottom row values. The
		// first
		// which is matrix[12] is for x, [13] is the y, and so on. The last one
		// is set to 1.0.
		matrix[12] = translate.x;
		matrix[13] = translate.y;
		matrix[14] = translate.z;
		matrix[15] = 1.0f;
	}

	public void translate(float x, float y, float z) {
		setIdentity();

		// To translate a 4x4 matrix you must replace the bottom row values. The
		// first
		// which is matrix[12] is for x, [13] is the y, and so on. The last one
		// is set to 1.0.
		matrix[12] = x;
		matrix[13] = y;
		matrix[14] = z;
		matrix[15] = 1.0f;
	}

	public void rotate(float angle, int x, int y, int z) {
		float sine = (float) Math.sin(PI_VALUE * angle / 180);
		float cosine = (float) Math.cos(PI_VALUE * angle / 180);

		if (x >= 1) {
			matrix[5] = cosine;
			matrix[6] = sine;
			matrix[9] = -sine;
			matrix[10] = cosine;
		}

		if (y >= 1) {
			matrix[0] = cosine;
			matrix[2] = -sine;
			matrix[8] = sine;
			matrix[10] = cosine;
		}

		if (z >= 1) {
			matrix[0] = cosine;
			matrix[1] = sine;
			matrix[4] = -sine;
			matrix[5] = cosine;
		}
	}

	public boolean inverseMatrix(Matrix4f m) {
		float[] tempMatrix = new float[16];

		float d12, d13, d23, d24, d34, d41;

		d12 = m.matrix[2] * m.matrix[7] - m.matrix[3] * m.matrix[6];
		d13 = m.matrix[2] * m.matrix[11] - m.matrix[3] * m.matrix[10];
		d23 = m.matrix[6] * m.matrix[11] - m.matrix[7] * m.matrix[10];
		d24 = m.matrix[6] * m.matrix[15] - m.matrix[7] * m.matrix[14];
		d34 = m.matrix[10] * m.matrix[15] - m.matrix[11] * m.matrix[14];
		d41 = m.matrix[14] * m.matrix[3] - m.matrix[15] * m.matrix[2];

		tempMatrix[0] = m.matrix[5] * d34 - m.matrix[9] * d24 + m.matrix[13]
				* d23;
		tempMatrix[1] = -(m.matrix[1] * d34 + m.matrix[9] * d41 + m.matrix[13]
				* d13);
		tempMatrix[2] = m.matrix[1] * d24 + m.matrix[5] * d41 + m.matrix[13]
				* d12;
		tempMatrix[3] = -(m.matrix[1] * d23 - m.matrix[5] * d13 + m.matrix[9]
				* d12);

		// Calculate the determinant.
		float determinant = m.matrix[0] * tempMatrix[0] + m.matrix[4]
				* tempMatrix[1] + m.matrix[8] * tempMatrix[2] + m.matrix[12]
				* tempMatrix[3];

		// Clear if the determinant is equal to zero. 0 means matrix have no
		// inverse.
		if (determinant == 0.0) {
			setIdentity();
			return false;
		}

		float invDeterminant = 1.0f / determinant;

		// Compute rest of inverse.
		tempMatrix[0] *= invDeterminant;
		tempMatrix[1] *= invDeterminant;
		tempMatrix[2] *= invDeterminant;
		tempMatrix[3] *= invDeterminant;

		tempMatrix[4] = -(m.matrix[4] * d34 - m.matrix[8] * d24 + m.matrix[12]
				* d23)
				* invDeterminant;
		tempMatrix[5] = m.matrix[0] * d34 + m.matrix[8] * d41 + m.matrix[12]
				* d13 * invDeterminant;
		tempMatrix[6] = -(m.matrix[0] * d24 + m.matrix[4] * d41 + m.matrix[12]
				* d12)
				* invDeterminant;
		tempMatrix[7] = m.matrix[0] * d23 - m.matrix[4] * d13 + m.matrix[8]
				* d12 * invDeterminant;

		// Pre-compute 2x2 dets for first two rows when computing cofactors
		// of last two rows.
		d12 = m.matrix[0] * m.matrix[5] - m.matrix[1] * m.matrix[12];
		d13 = m.matrix[0] * m.matrix[9] - m.matrix[1] * m.matrix[8];
		d23 = m.matrix[4] * m.matrix[9] - m.matrix[5] * m.matrix[8];
		d24 = m.matrix[4] * m.matrix[13] - m.matrix[5] * m.matrix[12];
		d34 = m.matrix[8] * m.matrix[13] - m.matrix[9] * m.matrix[12];
		d41 = m.matrix[12] * m.matrix[1] - m.matrix[13] * m.matrix[0];

		tempMatrix[8] = m.matrix[7] * d34 - m.matrix[11] * d24 + m.matrix[15]
				* d23 * invDeterminant;
		tempMatrix[9] = -(m.matrix[3] * d34 + m.matrix[11] * d41 + m.matrix[15]
				* d13)
				* invDeterminant;
		tempMatrix[10] = m.matrix[3] * d24 + m.matrix[7] * d41 + m.matrix[15]
				* d12 * invDeterminant;
		tempMatrix[11] = -(m.matrix[3] * d23 - m.matrix[7] * d13 + m.matrix[11]
				* d12)
				* invDeterminant;
		tempMatrix[12] = -(m.matrix[6] * d34 - m.matrix[10] * d24 + m.matrix[14]
				* d23)
				* invDeterminant;
		tempMatrix[13] = m.matrix[2] * d34 + m.matrix[10] * d41 + m.matrix[14]
				* d13 * invDeterminant;
		tempMatrix[14] = -(m.matrix[2] * d24 + m.matrix[6] * d41 + m.matrix[14]
				* d12)
				* invDeterminant;
		tempMatrix[15] = m.matrix[2] * d23 - m.matrix[6] * d13 + m.matrix[10]
				* d12 * invDeterminant;

		// Save the temp matrix to our matrix.
		matrix[0] = tempMatrix[0];
		matrix[1] = tempMatrix[1];
		matrix[2] = tempMatrix[2];
		matrix[3] = tempMatrix[3];
		matrix[4] = tempMatrix[4];
		matrix[5] = tempMatrix[5];
		matrix[6] = tempMatrix[6];
		matrix[7] = tempMatrix[7];
		matrix[8] = tempMatrix[8];
		matrix[9] = tempMatrix[9];
		matrix[10] = tempMatrix[10];
		matrix[11] = tempMatrix[11];
		matrix[12] = tempMatrix[12];
		matrix[13] = tempMatrix[13];
		matrix[14] = tempMatrix[14];
		matrix[15] = tempMatrix[15];

		return true;
	}

	public boolean invertMatrix(Matrix4f m) {
		// Transpose rotation
		matrix[0] = m.matrix[0];
		matrix[1] = m.matrix[4];
		matrix[2] = m.matrix[8];
		matrix[4] = m.matrix[1];
		matrix[5] = m.matrix[5];
		matrix[6] = m.matrix[9];
		matrix[8] = m.matrix[2];
		matrix[9] = m.matrix[6];
		matrix[10] = m.matrix[10];

		// Clear shearing terms
		matrix[3] = 0.0f;
		matrix[7] = 0.0f;
		matrix[11] = 0.0f;
		matrix[15] = 1.0f;

		// Translation is minus the dot of tranlation and rotations
		matrix[12] = -(m.matrix[12] * m.matrix[0])
				- (m.matrix[13] * m.matrix[1]) - (m.matrix[14] * m.matrix[2]);
		matrix[13] = -(m.matrix[12] * m.matrix[4])
				- (m.matrix[13] * m.matrix[5]) - (m.matrix[14] * m.matrix[6]);
		matrix[14] = -(m.matrix[12] * m.matrix[8])
				- (m.matrix[13] * m.matrix[9]) - (m.matrix[14] * m.matrix[10]);

		return true;
	}

	public Vector4f vectorMatrixMultiply(Vector4f v) {
		Vector4f out = new Vector4f();

		out.x = (v.x * matrix[0]) + (v.y * matrix[4]) + (v.z * matrix[8])
				+ matrix[12];
		out.y = (v.x * matrix[1]) + (v.y * matrix[5]) + (v.z * matrix[9])
				+ matrix[13];
		out.z = (v.x * matrix[2]) + (v.y * matrix[6]) + (v.z * matrix[10])
				+ matrix[14];

		return out;
	}

	public Vector4f vectorMatrixMultiply3x3(Vector4f v) {
		Vector4f out = new Vector4f();

		out.x = (v.x * matrix[0]) + (v.y * matrix[4]) + (v.z * matrix[8]);
		out.y = (v.x * matrix[1]) + (v.y * matrix[5]) + (v.z * matrix[9]);
		out.z = (v.x * matrix[2]) + (v.y * matrix[6]) + (v.z * matrix[10]);

		return out;
	}

	public Vector4f transformPoint(Vector4f v) {
		float x = v.x;
		float y = v.y;
		float z = v.z;

		v.x = x * matrix[0] + y * matrix[4] + z * matrix[8] + matrix[12];

		v.y = x * matrix[1] + y * matrix[5] + z * matrix[9] + matrix[13];

		v.z = x * matrix[2] + y * matrix[6] + z * matrix[10] + matrix[14];

		v.w = x * matrix[2] + y * matrix[7] + z * matrix[11] + matrix[15];

		return v;
	}

	public String toString() {
		return "|" + matrix[0] + " " + matrix[1] + " " + matrix[2] + " "
				+ matrix[3] + "|";

	}

}

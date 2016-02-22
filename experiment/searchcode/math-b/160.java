import java.math.BigInteger;
import java.security.*;
import java.util.*;
//ModSquareMatrix objects inherit all methods from ModMatrix
public class ModSquareMatrix extends ModMatrix {

   //Creates a square matrix with random entries
   //Or, it creates a matrix with all zeros
   //Another parameter specifies whether or not you wish the random
   //matrix to be invertible; if NOT, matrix may still be invertible by accident
   public ModSquareMatrix(int s,BigInteger m,boolean makeZero,boolean makeInvertible) throws MatricesNonConformableException {
         //Call a auperconstructor from ModMatrix-make the zero matrix, or a matrix with random entries
         super(s,s,m,makeZero);

         //Zero matrix is not invertible
         if (makeZero&&makeInvertible) throw new IllegalArgumentException ("Zero matrix cannot be inverted!");

         //A random invertible matrix is desired
         if (makeInvertible) {
            Random r=new Random();
            SecureRandom sr=new SecureRandom();
            boolean done=false;
            //Do this until the matrix inverts
            while (!done) {
               try {
                  //Try to take the inverse-may throw an exception if not invertible
                  this.inverse();
                  done=true;
               } catch (SingularMatrixException sme) {
                  //Change a random entry in the matrix
                  int row=Math.abs(r.nextInt())%numRows+1;
                  int col=Math.abs(r.nextInt())%numCols+1;
                  BigInteger value=new BigInteger(modulus.bitLength(),sr).mod(modulus);
                  this.setElement(row,col,value);
               } catch (ArithmeticException ae) {
                  //Change a random entry in the matrix
                  int row=Math.abs(r.nextInt())%numRows+1;
                  int col=Math.abs(r.nextInt())%numCols+1;
                  BigInteger value=new BigInteger(modulus.bitLength(),sr).mod(modulus);
                  this.setElement(row,col,value);
               }
            }
         }
   }

   //Makes a square matrix from a 1D array of values
   public ModSquareMatrix(int s,BigInteger[] a,BigInteger m) {
         super(s,s,a,m);
   }

   //Makes a copy of a matrix
   public ModSquareMatrix(ModSquareMatrix m) {
         array=new BigInteger[m.numRows+1][m.numCols+1];
         numRows=m.numRows;
         numCols=m.numCols;
         modulus=m.modulus;
         for (int i=1;i<=m.numRows;i++) {
             for (int j=1;j<=m.numCols;j++) {
                 array[i][j]=new BigInteger(m.array[i][j].toString());
             }
         }
   }

   //Method which uses Gaussian elimination to solve AX=B mod m for X
   //A is the ModSquarematrix calling the method
   //B is the Modmatrix constants - need not be a Vector
   //X is the ModMatrix returned
   public ModMatrix gaussianSolve(ModMatrix constants) throws MatricesNonConformableException,SingularMatrixException {
      //This method only works when the modulus is prime
      if (!modulus.isProbablePrime(16)) throw new IllegalArgumentException("Gaussian elimination method currently requires modulus to be prime!");
      //Copy the matrices and modify the copies
      ModSquareMatrix mat=new ModSquareMatrix(this);
      ModMatrix b;

      //If the ModMatrix constants is square, the answer should also be a ModSquareMatrix object (not just a ModMatrix)
      //Check for this here
      if (constants instanceof ModSquareMatrix) b=new ModSquareMatrix((ModSquareMatrix)constants);
      else b=new ModMatrix(constants);

      //Check if matrices are of compatible size first
      if (b.numRows!=mat.numRows) throw new MatricesNonConformableException("Matrix of coefficients and matrix of constants have different # of rows!");

      //Work the rows, starting with the first row
      int currentRow=1;
      while (currentRow<=mat.numRows) {
         int i=currentRow;
         //Make sure diagonal element is nonzero, if possible, by swapping
         while (i<=mat.numRows&&mat.array[i][currentRow].equals(BigIntegerMath.ZERO)) i++;
         if (i>mat.numRows) throw new SingularMatrixException("Linear dependence exists here!");
         //Swap with a row not having zero in diagonal position
         if (currentRow!=i) swapRows(mat,b,currentRow,i);
         //Now, you must produce all zeros below and above the diagonal element
         i=1;
         //Multiply each row by the proper scalar
         while (i<=mat.numRows) {
            if (i!=currentRow) {
               BigInteger scalar=mat.array[i][currentRow];
               if (!scalar.equals(BigIntegerMath.ZERO)) {
                  multiplyRow(mat,b,i,mat.array[currentRow][currentRow]);
                  multiplyRow(mat,b,currentRow,scalar);
                  //Replace row i with row i minus diagonal row
                  subtractRow(mat,b,i,currentRow);
               }
            }
            i++;
         }
         currentRow++;
      }
      //Now, produce 1's along main diagonal by multiplying by an inverse
      for (int index=1;index<=mat.numRows;index++) {
         multiplyRow(mat,b,index,mat.array[index][index].modInverse(modulus));
      }
      //Remember, b may be a square matrix-polymorphism takes care of this here
      return b;
   }

   //This method exists in case the answer is actually a square matrix
   public ModSquareMatrix gaussianSolve(ModSquareMatrix constants) throws MatricesNonConformableException,SingularMatrixException {
      return (ModSquareMatrix) gaussianSolve((ModMatrix)constants);
   }

   //Used by gaussianSolve to multiply a row by some scalar
   private void multiplyRow(ModSquareMatrix mat,ModMatrix b,int i,BigInteger scalar) {
      //Multiplies row i by scalar-answer replaces i-th row
      for (int k=1;k<=mat.numCols;k++) mat.array[i][k]=BigIntegerMath.lnr(mat.array[i][k].multiply(scalar),mat.modulus);
      for (int k=1;k<=b.numCols;k++) b.array[i][k]=BigIntegerMath.lnr(b.array[i][k].multiply(scalar),mat.modulus);
   }

   //Used by gaussianSolve to subtract one row from another
   private void subtractRow(ModSquareMatrix mat,ModMatrix b,int i,int j) {
      //Subtracts row j from row i; answer replaces row i
      for (int k=1;k<=mat.numCols;k++) mat.array[i][k]=BigIntegerMath.lnr(mat.array[i][k].subtract(mat.array[j][k]),mat.modulus);
      for (int k=1;k<=b.numCols;k++) b.array[i][k]=BigIntegerMath.lnr(b.array[i][k].subtract(b.array[j][k]),mat.modulus);
   }

   //Used by gaussianSolve to swap two rows
   private void swapRows(ModSquareMatrix mat,ModMatrix b,int r1,int r2) {
      BigInteger temp;
      for (int j=1;j<=mat.numCols;j++) {
         temp=mat.array[r1][j];
         mat.array[r1][j]=mat.array[r2][j];
         mat.array[r2][j]=temp;
      }
      for (int j=1;j<=b.numCols;j++) {
         temp=b.array[r1][j];
         b.array[r1][j]=b.array[r2][j];
         b.array[r2][j]=temp;
      }
   }

   //Method produces an inverse of A (if possible) by using gaussianSolve on AX=I mod m
   //where I is an identity matrix
   public ModSquareMatrix inverse() throws MatricesNonConformableException, SingularMatrixException {
      //See the ModIdentityMatrix class-subclass of ModSquareMatrix
      return gaussianSolve(new ModIdentityMatrix(numRows,modulus));
   }
}



public class Int {

   //Records if Int negative/nonnegative
   boolean negative=false;

   //Digits are stored as decimal digits, highest order digit first
   int[] digits;

   //Declare some constants
   final public static Int ZERO=new Int();
   //Records position of 0 (zero) in the character set
   final private static int zeroPos='0';

   //Constructors follow

   //This one produces an array of one int containing 0
   public Int() {
      negative=false;
      digits=new int[1];
      digits[0]=0;
   }

   //Produces an Int object from an int
   public Int(int n) {
      //Produce the array-an int can not have more than 10 decimal digits
      int[] temp=new int[10];
      //zero is a special case
      if (n==0) {
         negative=false;
         digits=new int[1];
         digits[0]=0;
         return;
      }
      //Negative int n-set negative to true, take absolute value of n
      if (n<0) {
         negative=true;
         n=Math.abs(n);
      }
      int count=10;
      //Divide by 10 until nothing left
      while (n>0) {
         //Remainder is the count-th digit in the array
         temp[--count]=n%10;
         n/=10;
      }
      //Remove any leading zeros-make new array and copy
      digits=new int[temp.length-count];
      for (int i=0;i<digits.length;i++) digits[i]=temp[count+i];
   }

   //Copy an Int object
   public Int(Int n) {
      negative=n.negative;
      digits=new int[n.digits.length];
      for (int i=0;i<digits.length;i++) digits[i]=n.digits[i];
   }

   //This constructor converts a String to an Int.  May throw an Exception
   //if the String cannot be converted to an Int.
   public Int(String s) throws IntException {
      //Place the string into an array of characters
      char[] temp=s.trim().toCharArray();

      //Parse the array.
      //First character may be a sign
      //firstDigitLoc records index of first digit
      int firstDigitLoc=0;
      //If "-" sign symbol encountered, make negative Int, move to next index
      if (temp[0]=='-') {
         negative=true;
         firstDigitLoc++;
      //If "+" just move to next symbol
      } else if (temp[0]=='+') {
         firstDigitLoc++;
      }
      int index=firstDigitLoc;

      //Check if remaining characters are digits-record # leading zeros
      boolean significantDigitFound=false;
      while (index<temp.length&&Character.isDigit(temp[index])) {
         if (!significantDigitFound) {
            //Skip any leading zeros
            if (temp[index]=='0') firstDigitLoc++;
            else significantDigitFound=true;
         }
         index++;
      }

      //Throw an exception if nondigit found
      if (index<temp.length) throw new IntException("This is not a valid integer!");

      //If no significant digit found, this was a string of all zeros
      //Make the zero Int and return
      if (!significantDigitFound) {
         negative=false;
         digits=new int[1];
         digits[0]=0;
         return;
      }

      //This parsed as an integer-store it, ignoring leading zeros
      char[] c=s.trim().substring(firstDigitLoc,s.length()).toCharArray();
      digits=new int[c.length];
      //Subtract zeroPos from the character-this gives the corresponding int
      for (int i=0;i<c.length;i++) digits[i]=(int)c[i]-zeroPos;
   }

   //Methods to perform arithmetic on Int objects

   public Int absoluteValue() {
      //Make a new Int by copying this Int
      Int answer=new Int(this);
      //Set negative to false
      answer.negative=false;
      return answer;
   }

   public Int negative() {
      Int answer=new Int(this);
      //Flip the negative value
      answer.negative=!this.negative;
      return answer;
   }

   public Int add(Int other) {
      Int ans;
      //zero is a special case-nothing to do but return a copy of the nonzero Int
      if (this.equals(ZERO)) return new Int(other);
      else if (other.equals(ZERO)) return new Int(this);
      else if (this.equals(other.negative())) return new Int();
      //If they are the same sign, perform the addition; add carries
      else if (negative==other.negative) {
         ans=addDigits(other);
         ans.negative=negative;
      }
      //If they are of different signs, determine the larger
      //(magnitude-wise) and subtract the smaller from it.
      //Result has same sign as first (larger) operand.
      else if (this.absoluteValue().lessThan(other.absoluteValue())) {
         ans=other.subtractDigits(this);
         ans.negative=other.negative;
      } else {
         ans=this.subtractDigits(other);
         ans.negative=this.negative;
      }
      //Trim leading zeros and return
      return ans.trimZeros();
   }

   public Int subtract(Int other) {
      //To subtract, we add the negative
      return this.add(other.negative());
   }

   private Int addDigits(Int other) {
      int top1=this.digits.length-1;
      int top2=other.digits.length-1;
      int top3=Math.max(this.digits.length,other.digits.length)+1;
      Int answer=new Int();
      answer.digits=new int[top3];
      top3--;
      int carry=0; int sum=0;
      while (top1>=0&&top2>=0) {
         sum=this.digits[top1]+other.digits[top2]+carry;
         if (sum>9) {sum%=10; carry=1;} else carry=0;
         answer.digits[top3]=sum;
         top1--;top2--;top3--;
      }
      if (top1<0&&top2<0) {
         answer.digits[0]=carry;
      } else if (top1<0) {
         while (top2>=0) {
            sum=other.digits[top2]+carry;
            if (sum>9) {sum%=10; carry=1;} else carry=0;
            answer.digits[top3]=sum;
            top2--;top3--;
         }
         answer.digits[top3]=carry;
      } else {
         while (top1>=0) {
            sum=this.digits[top1]+carry;
            if (sum>9) {sum%=10; carry=1;} else carry=0;
            answer.digits[top3]=sum;
            top1--;top3--;
         }
         answer.digits[top3]=carry;
      }
      return answer;
   }

   private Int subtractDigits(Int other) {
      Int answer=new Int();
      Int copy=new Int(this);
      answer.digits=new int[this.digits.length];
      int top1=this.digits.length-1;
      int top2=other.digits.length-1;
      while (top2>=0) {
         if (copy.digits[top1]<other.digits[top2]) {
            borrow(copy,top1-1);
            copy.digits[top1]+=10;
         }
         answer.digits[top1]=copy.digits[top1]-other.digits[top2];
         top1--; top2--;
      }
      while (top1>=0) {
         answer.digits[top1]=copy.digits[top1];
         top1--;
      }
      return answer;
   }

   //Method to "borrow" for subtraction
   private void borrow(Int n,int pos) {
      while (n.digits[pos]==0) {
         n.digits[pos]=9;
         pos--;
      }
      n.digits[pos]--;
   }

   //Method to chop off any leading zeros
   private Int trimZeros() {
      int i;
      //Look for first nonzero in the array
      for (i=0;i<this.digits.length;i++)
         if (this.digits[i]!=0)
            break;
      Int answer=new Int();
      answer.negative=this.negative;
      //Make a (possibly) smaller array for answer
      answer.digits=new int[this.digits.length-i];
      //Copy the nonzero digits over, and return answer
      for (int j=0;j<answer.digits.length;j++) answer.digits[j]=this.digits[j+i];
      return answer;
   }

   public Int multiply(Int other) {
      //Initialize the answer to 0
      Int result=new Int();
      //If either operand is 0, return 0
      if (this.equals(ZERO)||other.equals(ZERO)) return result;
      //Now, multiply the first operand by each digit in the
      //second operand, shifting left each answer by a power of ten
      //as we pass through the digits, adding each time to result.
      for (int i=0; i<other.digits.length; i++) {
         result=result.add(this.multiply(other.digits[i]).appendZeros(other.digits.length-1-i));
      }
      //If operands are same sign, result is positive
      //otherwise, the result is negative; 0 is already taken care of
      if (this.negative==other.negative) result.negative=false;
      else result.negative=true;
      //Return the result
      return result.trimZeros();
   }

   //Method to multiply an Int by a single decimal digit
   //Called repeatedly by multiply(Int) method
   private Int multiply(int otherDigit) {
      //Make a new Int for the answer
      Int result=new Int();
      //If digit to multiply by is 0, return 0
      if (otherDigit==0) return result;
      //Make the answer array one longer than the first operand,
      //in case there is a carry
      result.digits=new int[this.digits.length+1];
      int carry=0;
      int tempInteger;
      int i;
      for (i=this.digits.length-1;i>=0;i--) {
         //i+1th digit of result is the ith digit of the first operand
         //times the digit in the second operand.  If this is more than
         //10, we must keep only the least significant digit.
         //We also add any previous carries
         tempInteger=this.digits[i]*otherDigit+carry;
         result.digits[i+1]=tempInteger%10;
         //If the product is more than 10, we must set carry
         //for the next round
         carry=tempInteger/10;
      }
      //Possibility of one last carry; do the final digit.
      result.digits[0]=carry;
      return result;
   }

   private Int appendZeros(int places) {
	//Make a new Int object
      Int result=new Int();
	//If this equals 0, return 0; no need to append
      if (this.equals(ZERO)) return result;
	//Make the resuting array larger
      result.digits=new int[this.digits.length+places];
	//Shift the digits into the new array
      for (int i=0;i<this.digits.length;i++) {
         result.digits[i]=this.digits[i];
      }
      return result;
   }

   public Int[] divideAndRemainder(Int other) {return null;}
   //Above returns the quotient and remainder of integer division-YOU WRITE THIS!

   public Int divide(Int other) {return null;}
   //Above returns the quotient integer division-YOU WRITE THIS!

   public Int Remainder(Int other) {return null;}
   //Above returns the remainder of integer division-YOU WRITE THIS!

   public boolean equals(Int other) {
      boolean answer=true;
      //Check if same sign
      if (negative!=other.negative) answer=false;
      //Check if different lengths
      else if (digits.length!=other.digits.length) answer=false;
      //If same length and sign, compare each digit
      else for (int i=0;i<digits.length;i++)
         //Any nonmatching digit sets answer to false
         if (digits[i]!=other.digits[i]) answer=false;
      return answer;
   }

   public boolean lessThan(Int other) {
      //Start by assuming this is less than other
      boolean answer=false;
      //Both Ints are nonnegative here
      if (!negative&&!other.negative) {
         //If they are the same length, must compare the digits
         if (digits.length==other.digits.length) {
            int i=0;
            while (i<this.digits.length&&digits[i]==other.digits[i]) i++;
            //Each digit of this was less than each digit of other
            if (i<this.digits.length)
               if (digits[i]<other.digits[i])
                  answer=true;
            //this has smaller length than other-must be less than
         } else if (digits.length<other.digits.length) answer=true;
      //If both Ints negative, do the reverse of the above comparisons
      } else if (negative&&other.negative) {
         if (digits.length==other.digits.length) {
            int i=0;
            while (i<this.digits.length&&digits[i]==other.digits[i]) i++;
            if (i<this.digits.length)
               if (digits[i]>other.digits[i])
                  answer=true;
         } else if (other.digits.length<digits.length) answer=true;
      //If this is negative and other nonnegative, must be less than
      } else if (negative&&!other.negative) answer=true;
      //Otherwise, this is nonnegative and other negative
      //Return answer, which was initialized to false
      return answer;
   }

   //Returns the Int as a String, mainly for output purposes
   public String toString() {
      //Use a StringBuffer for efficiency
      StringBuffer answer=new StringBuffer("");
      //Put a "-" symbol in front if negative
      if (negative) answer.append("-");
      //Append each digit to the StringBuffer and return it as a String
      for (int i=0;i<digits.length;i++) {
         answer.append(new Integer(digits[i]).toString());
      }
      return new String(answer);
   }

}


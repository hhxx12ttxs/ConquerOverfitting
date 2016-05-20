/**
 * Run the MainClass to run the whole program.
 * 
 * The values need to be changed in the init function of the BinaryConvertor class.
 * denaryNumber will be converted to binary and binaryArray to denary.
 * 
 * Binary array should have one index for each position of a binary string, where 
 * the index 0, would be the furthest right digit if it was a normal string (e.g.
 * binaryArray[0] for 100010101 would be 1).
 * 
 * The conversion to binary will always be an integer with 1 as the first digit
 * because the int type does not respect leading 0s. Always treat is as a regular
 * positive binary number.
 * 
 * The main method in the main class prints the response to convertDenaryToBinary
 * which runFunctions returns. runFunction itself prints the output of
 * convertBinaryToDenary. This way you can see both functions in action.
 * 
 * @author 
 */
class MainClass
{
    /** 
     * @param String[] a 
     */
    public static void main (String[] a)
    {
        System.out.println(((new BinaryConvertor()).init()).runFunctions());
    }
}

class BinaryConvertor 
{
    
    int[] binaryArray;
    
    int denaryNumber;

    /** 
     * Initialises the object and sets the internal list variable.
     * @return IntegerList
     */
    public BinaryConvertor init() 
    {
        denaryNumber = 1024;
        
        binaryArray = new int [8];
        //10101101
        binaryArray[0] = 1;
        binaryArray[1] = 0;
        binaryArray[2] = 1;
        binaryArray[3] = 1;
        binaryArray[4] = 1;
        binaryArray[5] = 1;
        binaryArray[6] = 0;
        binaryArray[7] = 1;

        return this;
    }
    
    /** 
     * Runs both conversion functions and prints the output of binary to denary
     * conversion and returns the output of denary to binary. It does it like this
     * so that it has something to return and the MainClass has to print something.
     * @return int 
     */
    public int runFunctions () 
    {
        System.out.println(this.convertBinaryToDenary());
        return this.convertDenaryToBinary();
    }
    
    /** 
     * Converts a positive integer into a binary string. Because the return type
     * has to be int, the first digit will always be 1. This does not mean
     * it is a negative binary number. Always assume positive integer.
     * @return int
     */
    public int convertDenaryToBinary()
    {
        int remainder;
        int highestPowerOfTwo;
        int rem;
        int answer;
        int tmpPower;
        
        remainder = denaryNumber;
        highestPowerOfTwo = this.findHighestBinaryIndex(denaryNumber);
        answer = 0;
        
        while (this.greaterThanEqualTo(highestPowerOfTwo, 0)) {
            tmpPower = this.power(2, highestPowerOfTwo);
            rem = this.mod(remainder, tmpPower);
            if (this.equal(rem, remainder)) {
                answer = answer * 10;
            } else {
                answer = answer * 10;
                answer = answer + 1;
            }
            remainder = rem;
            highestPowerOfTwo = highestPowerOfTwo - 1;
        }
        return answer;
    }
    
    /** 
     * Finds the highest power of 2 that the denary number can divide into.
     * @param int denary
     * @return int
     */
    public int findHighestBinaryIndex(int denary)
    {
        int highestPower;
        
        highestPower = 0;
        
        while (this.lessThanEqualTo(this.power(2, highestPower), denary)) {
            highestPower = highestPower + 1;
        }
        
        return highestPower;
    }
    
    /** 
     * Converts the binary array into a decimal number.
     * @return int
     */
    public int convertBinaryToDenary() 
    {
        int denary;
        int highestPower; 
        int placeValue;
        int powerOfTwo;
        int binaryValue;
        
        denary = 0;
        highestPower = binaryArray.length;
        highestPower = highestPower - 1;
        
        while (this.greaterThanEqualTo(highestPower, 0)) {
            powerOfTwo = this.power(2,highestPower);
            binaryValue = binaryArray[highestPower];
            placeValue =  powerOfTwo * binaryValue;
            denary = denary + placeValue;
            highestPower = highestPower - 1;
        }
        
        return denary;
    }
    
    /** 
     * This acts as a >= function. x >= y
     * would be written as greaterThanEqualTo(x,y).
     * @param int operand1
     * @param int operand2
     * @return boolean 
     */
    public boolean greaterThanEqualTo (int operand1, int operand2)
    {
        boolean gtEq;
        
        if (this.greaterThan(operand1, operand2)) {
            gtEq = true;
        } else {
            if (this.equal(operand1, operand2)) {
                gtEq = true;
            } else {
                gtEq = false;
            }
        }
        
        return gtEq;
    }
    
    /** 
     * This acts as a <= function. x <= y
     * would be written as lessThanEqualTo(x,y).
     * @param int operand1
     * @param int operand2
     * @return boolean 
     */
    public boolean lessThanEqualTo (int operand1, int operand2)
    {
        boolean ltEq;
        
        if (operand1 < operand2) {
            ltEq = true;
        } else {
            if (this.equal(operand1, operand2)) {
                ltEq = true;
            } else {
                ltEq = false;
            }
        }
        
        return ltEq;
    }
    
    /** 
     * This acts as a > function. x > y
     * would be written as greaterThan(x,y).
     * @param int operand1
     * @param int operand2
     * @return boolean 
     */
    public boolean greaterThan (int operand1, int operand2)
    {
        boolean gt;
        if (operand1 < operand2) {
            gt = false;
        } else {
            if (this.equal(operand1, operand2)) {
                gt = false;
            } else {
                gt = true;
            }
        }
        return gt;
    }
    
    /** 
     * This acts as a == function. x == y
     * would be written as equal(x,y).
     * @param int operand1
     * @param int operand2
     * @return boolean 
     */
    public boolean equal (int operand1, int operand2)
    {
        boolean eq;
        int tmpSum;
        
        tmpSum = operand1 - operand2;
        
        if (tmpSum < 0) {
            eq = false;
        } else {
            if (0 < tmpSum) {
                eq = false;
            } else {
                eq = true;
            }
        }
        return eq;
    }
    
    /** 
     * This acts as a ^ function. x to the power of y (x^y)
     * would be written as power(x,y).
     * @param int operand1
     * @param int operand2
     * @return int 
     */
    public int power (int operand1, int operand2)
    {
        int answer;
        
        if (this.equal(operand2, 0)) {
            answer = 1;
        } else {
            if (this.equal(operand2, 1)) {
                answer = operand1;
            } else {
                answer = operand1;
                while (this.greaterThan(operand2, 1)) {
                    answer = answer * operand1;
                    operand2 = operand2 - 1;
                }
            
            }
        }
        return answer;
    }
        
    /** 
     * This acts as a mod function. x mod y would be written as mod(x,y).
     * @param int operand1
     * @param int operand2
     * @return int 
     */
    public int mod (int operand1, int operand2)
    {
        int answer;
        answer = operand1;
        while (this.greaterThanEqualTo(answer, operand2)){
            answer = answer - operand2;
        }
        return answer;
    }
}

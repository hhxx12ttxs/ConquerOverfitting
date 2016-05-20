package com.example.arithmetic;

/**
 * User: Nixy
 * Date: 25.03.13
 * Time: 18:55
 */
public class BinaryNumber extends Number {
    private boolean negative = false;

    public int getWeightCoeff() {
        return weightCoeff;
    }

    private double numbeWithoutWeightr;
    private int  weightCoeff;
    private double numberWithWeight;
    private int integer;

    public int getBit(int index){
        if (index > -1 & index < rank){
            return number[index];
        }
        return -1;
    }

    public int[] getBitArray(){
        return number;
    }

    public  boolean  isNegative(){
        return negative;
    }

    public void setNegative(boolean negative){
        numberWithWeight = -Math.abs(numberWithWeight);
        numbeWithoutWeightr = -Math.abs(numbeWithoutWeightr);
        this.negative = negative;
    }
    public int getRank() {
        return rank;
    }

    public double getNumberWithWeight(){
        double num = 0;

        if (isNegative()){
            inverseNumber();
        }

        for (int i = 1 ; i < weightCoeff+1  ; i++){
            num += number[i]  * Math.pow(2,weightCoeff - i);
        }
        for (int j = 1 , i = weightCoeff+1 ; i < rank ; i++,j++){
            num += number[i]   *  Math.pow(2,-j);
        }

        if (isNegative()){
            inverseNumber();
            num *= -1;
        }

        return num;
    }

    public int getInteger(){
        int num = 0;
        if (isNegative()){
            inverseNumber();
        }
        for (int  i = rank - 1,j = 0 ; i > 0 ; i--,j++){
            num += number[i]  *  Math.pow(2,j);
        }
        if (isNegative()){
            inverseNumber();
            num *= -1;
        }
        return num;
    }

    public double getNumberWithoutWeight(){
        double num = 0;
        if (isNegative()){
            inverseNumber();
        }
        for (int  i = 1 ; i < rank ; i++){
            num += number[i]  *  Math.pow(2,-i);
        }
        if (isNegative()){
            inverseNumber();
            num *= -1;
        }
        return num;
    }

    public void moveRight(){
        for (int i = rank - 1; i > 1 ; i--)
            number[i] = number[i-1];
        number[1] = 0;
        numberWithWeight /= 2;
        numbeWithoutWeightr /= 2;
        integer /= 2;
    }

    public void moveLeft(){
        numberWithWeight *= 2;
        numbeWithoutWeightr *= 2;
        integer *= 2;
        for (int i = 1 ; i < rank-1 ; i++)
            number[i] = number[i+1];
        number[rank-1] = 0;

    }


    public void inverseNumber(){

        for (int i = 0 ; i < rank ; i++){
           number[i] = number[i] > 0 ? 0 : 1;
        }
        number[rank-1] += 1;

        for (int i = rank-1 ; i > 1  ; i--){
            if (this.number[i] > 1){
                this.number[i] %= 2;
                this.number[i-1] ++;
            } else
                break;

        }
    }



    public  BinaryNumber(BinaryNumber number){

        this.rank = number.rank;
        this.number = new int[rank];
        this.weightCoeff = number.weightCoeff;
        for (int i = 0 ; i < rank - 1 ; i++){
            this.number[i] = number.number[i];
        }
        this.negative = this.number[0] == 1;
        numberWithWeight = getNumberWithWeight();
        numbeWithoutWeightr = getNumberWithoutWeight();
        integer = getInteger();
    }


    public BinaryNumber(int[] number,int rank,int weightCoeff){
        this.number = new int[rank];
        this.weightCoeff = weightCoeff;
        this.rank = rank;
        if (number[0] == 1){
            setNegative(true);
        }

        for (int i = number.length-1,j = rank-1 ; i > 0 ; i--,j--){
            this.number[j] = number[i] > 0 ? 1 : 0;
        }

        numberWithWeight = getNumberWithWeight();
        numbeWithoutWeightr = getNumberWithoutWeight();
        integer = getInteger();
    }

    public BinaryNumber(double number,int rank,int weightCoeff){
        this.rank = rank;
        this.number = new int[rank];
        this.weightCoeff = weightCoeff;

        int numer[] = new int[weightCoeff];
        int num = (int) Math.abs( number );
        double den = Math.abs(number) - num;
        int counter = 0;
        while (num > 0){
            if (!(counter < weightCoeff)){
                Exeptions.getInstance().e(Exeptions.OUT_OF_RANGE);
                return;
            }
             numer[counter] = num%2;
             num /= 2;
             counter++;
        }
        counter = 1;
        for (int i = weightCoeff-1 ; i > -1  ; i-- ){
            this.number[weightCoeff-i] = numer[i];
            counter++;
        }
        while (den != 0){
             num = (int) (den*2);
             this.number[counter] = num;
             den = den * 2 - num;
             ++counter;
             if (counter == rank)
                {
                    num = (int) (den*2);
                    this.number[counter-1] += num;
                    break;
                }
            }
        for (int i = rank-1 ; i > 2  ; i--){
            if (this.number[i] > 1){
                this.number[i] %= 2;
                this.number[i-1] ++;
            } else  break;
        }
        if (number < 0) {
           negative = true;
           inverseNumber();
        }
        numberWithWeight = getNumberWithWeight();
        numbeWithoutWeightr = getNumberWithoutWeight();
        integer = getInteger();
    }


}


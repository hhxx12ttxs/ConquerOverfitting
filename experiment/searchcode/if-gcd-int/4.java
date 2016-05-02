package NumberTheory;

/**
 * Author: karthikabinav
 * Date: 10/20/13
 * Time: 2:54 PM
 */
public class GCD {
    //The two numbers whose GCD you want to find
    private int a;
    private int b;

    public GCD(int a,int b)
    {
        this.a = a;
        this.b = b;
    }
    public int getGCD()
    {
        if(this.a>this.b)
        {
            int temp = this.a;
            this.a = this.b;
            this.b = temp;
        }

        if( this.a == 0 )
            return this.b;

        int temp = this.a;
        this.a = this.b%this.a;
        this.b = temp;

        return getGCD();
    }


}


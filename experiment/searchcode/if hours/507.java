import hsa.*;
public class EmployeePay {

    public static void main(String[] args) {
        Console c=new Console(40,80);
        double[] pay, reg, ot;
        pay=new double [6];
        reg=new double [6];
        ot=new double [6];
        int[] hours;
        hours=new int [6];
            for(int x=1; x<=5; x++){
                while(1>hours[x]||hours[x]>70){
            c.print("Enter the hours for employee "+x+": ");
            hours[x]=c.readInt();
                }
            }
            for(int x=1; x<=5; x++){
            if(40>=hours[x]){
                pay[x]=hours[x]*15;
                ot[x]=0;
                reg[x]=pay[x];
            }
            else{
                pay[x]=40*15+((hours[x]-40)*22.5);
                reg[x]=600;
                ot[x]=pay[x]-600;
            }
            }
            c.println("");
    for(int x=1; x<=5; x++){
    c.println("Employee:      "+x+"\nHours:         "+hours[x]+"\nRegular Pay:  $"+reg[x]+"\nOvertime Pay: $"+ot[x]+"\nTotal:        $"+pay[x]+"\n");
    
}
    
}
    }


    
    



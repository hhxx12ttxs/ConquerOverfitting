import hsa.*;

public class employeePay {

    public static void main(String[] args) {
        Console c = new Console(40,80);
        int[] hours;
        double [] totalpay;
        double [] overpay;
        hours = new int[5];
        totalpay = new double[5];
        overpay = new double[5];
        //asking for hours
        for(int x = 0;x<=4;x++){
            while(true){
                c.print("Enter number of hours worked by employee #"+(x+1)+"(1-70): ");
                hours [x]= c.readInt();
                if (hours[x]>= 1 && hours[x]<=70) {
                    break;
                }
                else{
                    c.print("The number of hours must be from 1 to 70!\n");
                }
            }
        }
        c.clear();
        c.print("Employee Summary\n===================================\n");
        for(int x = 0;x<=4 ;x++){
            if(hours[x]>40){
                hours[x]-=40;
                overpay[x]=hours[x]*15*1.5;
                totalpay[x]=overpay[x]+600;
            }
            else{
                totalpay[x]=hours[x]*15;
            }
            
            c.print("Employee #"+(x+1)+"\n");
            c.print("Hours Worked: "+hours[x]+"\n");
            c.print("Regular pay: $"+ (totalpay[x]-overpay[x])+"\n");
            c.print("Over pay: $"+ overpay[x]+"\n");
            c.print("Total pay: $"+totalpay[x]+"\n\n");
        }
    }
    
}


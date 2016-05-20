/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package demos;
import java.net.*;
import java.io.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author hpfamily
 */
public class demoS {
    
        public static   int numclient = 4;  //s? l??ng client c?n ?? ?? ch?i
        public static   int port = 3200;
        public static   int turn = 13;
        
        public static   PrintWriter out = null;
        public static   BufferedReader in = null;
        
        public static   List<String> chiabai1 = new ArrayList<String>(13);
        public static   List<String> chiabai2 = new ArrayList<String>(13);
        public static   List<String> chiabai3 = new ArrayList<String>(13);
        public static   List<String> chiabai4 = new ArrayList<String>(13);
        
        public  static  String chuoibai1 = new String();
        public static   String chuoibai2 = new String();
        public static   String chuoibai3 = new String();
        public static   String chuoibai4 = new String();
        
        public static   String[] QuanBai = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "q", "k", "a"};
        public static   String[] LoaiQuan ={"s", "c", "d", "h"};
        
        public static   List<String> Bai = new ArrayList<String>(52);
              
        public static   List<Socket> lstConnect = new ArrayList<Socket>(numclient);  //ch?a danh sách các client ??ng ký t?i
        public static   List<String> lstName = new ArrayList<String>(numclient);

        public static   int diem1 = 0;
        public static   int diem2 = 0;
        public static   int diem3 = 0;
        public static   int diem4 = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

      
        int countStep1 = 0;  //??m s? client
        int countStep2 = 0;
        int countStep3 = 0;
        int countStep4 = 0;
        int countStep5 = 0;
        int countStep6 = 0;
        int countStep7 = 0;
        int countStep8 = 0;
        int countStep9 = 0;
        int countStep10 = 0;
        int countStep11 = 0;

        try
        {
            Bai = new demoS().TaoBai();
            new demoS().XaoBai();
            new demoS().chiaBai();
            System.out.println(Bai.size());
            System.out.println(chiabai1.size());
        }
        catch(Exception ex)
        {
            System.out.println("error tao bai");
        }
        
        try{

             ServerSocket socket1 = new ServerSocket(port);          //m? socket ??i các client
             System.out.println("MultipleSocketServer Initialized"); //hi?n th? chu?i báo server ?ă s?n sŕng

             do
             {
                 Socket connection = socket1.accept();              //ch?p nh?n client ??ng ký vŕo
                 lstConnect.add(connection);                        //add client v?a k?t n?i vŕo list
                 System.out.println("tiep nhan thanh cong client thu " + (countStep1 +1));  //xu?t chu?i thông báo ti?p nh?n thŕnh công client th? m?y

                 countStep1 = countStep1 +1;                      //bi?n ??m s? client t?ng lęn 1
             }while(countStep1<numclient);  //m? socket ch?p nh?n các client sao cho ?? s?
            
           
            //g?i cho các client bi?t ?ă k?t n?i thŕnh công ??n server
             do
             {
                 String returnCode = "ket noi thanh cong";
                 new demoS().SentMess(lstConnect.get(countStep2), returnCode);
                 countStep2 = countStep2+1;    //bi?n ??m t?ng 1
             }
              while(countStep2<numclient);

            //nh?n nick ??ng ký t? client

             do
             {
                 //nh?n nick
                 String nickTemp = new demoS().ReceiveMess(lstConnect.get(countStep3));
                 lstName.add(nickTemp);
                 countStep3 = countStep3 + 1;
             }
             while(countStep3 < numclient);

             
             //g?i tęn c?a 4 ng??i ch?i qua cho m?i client
              do
             {
                 
                  String returnCode = "nguoi choi " + 1 + " " + lstName.get(0) +
                                           " nguoi choi " + 2 + " " + lstName.get(1)+
                                           " nguoi choi " + 3 + " " + lstName.get(2)+
                                           " nguoi choi " + 4 + " " + lstName.get(3);
                
                 new demoS().SentMess(lstConnect.get(countStep4), returnCode);
                 countStep4 = countStep4 + 1;
                //  System.out.println(returnCode);
             }
             while(countStep4 < numclient);

            //nh?n tín hi?u b?t ??u t? client
             do
             {
                 String start = new demoS().ReceiveMess(lstConnect.get(countStep5));
                 System.out.println(start);

                 countStep5 = countStep5 + 1;
             }
             while(countStep5 < numclient);

             
             // T?o b? bŕi m?i, ch? t?o m?t l?n duy nh?t
             Bai = new demoS().TaoBai();
             
             // vňng do while cho t?i khi có ng??i tręn 100 ?i?m
             do{
                 
                // Chia bŕi cho 4 ng??i ch?i
                new demoS().XaoBai();
                
                // Chia bŕi cho 4 ng??i ch?i 
                chiabai1 = new ArrayList<String>();
                chiabai2 = new ArrayList<String>();
                chiabai3 = new ArrayList<String>();
                chiabai4 = new ArrayList<String>();
                new demoS().chiaBai();

                // chuy?n các lá bŕi c?a ng??i ch?i vę chu?i
                chuoibai1 = new String();
                chuoibai2 = new String();
                chuoibai3 = new String();
                chuoibai4 = new String();
                new demoS().ListToString();

                 System.out.println(chuoibai1);
                 System.out.println(chuoibai2);
                 System.out.println(chuoibai3);
                 System.out.println(chuoibai4);
         
                 //g?i chu?i bŕi ??u tięn cho các client
                 do
                 {
                     String temp ="";
                     switch (countStep11)
                     {
                         case 0:
                         {
                             temp = chuoibai1.concat(" |").concat(chuoibai2).concat(" |").concat(chuoibai3).concat(" |").concat(chuoibai4);
                             break;
                         }
                         case 1:
                         {
                             temp = chuoibai2.concat(" |").concat(chuoibai3).concat(" |").concat(chuoibai4).concat(" |").concat(chuoibai1);
                             break;
                         }
                         case 2:
                         {
                             temp = chuoibai3.concat(" |").concat(chuoibai4).concat(" |").concat(chuoibai1).concat(" |").concat(chuoibai2);
                             break;
                         }
                         case 3:
                         {
                             temp = chuoibai4.concat(" |").concat(chuoibai1).concat(" |").concat(chuoibai2).concat(" |").concat(chuoibai3);
                             break;
                         }
                     }

                     new demoS().SentMess(lstConnect.get(countStep11), temp);
                     countStep11 = countStep11 + 1;
                  }
                    while(countStep11<numclient);


              /*   int luoti = 0;
                 do{
                     
                     luoti++;
                 }while(luoti < turn);*/
                 
                for(int luot = 0; luot < turn; luot ++)
                {
                    String quanbaiclient = new String();
                    
                    //nh?n 4 lá bŕi mŕ các client g?i
                    countStep6 = 0;
                    countStep7 = 0;
                    countStep8 = 0;
                    countStep9 = 0;
                    countStep10 = 0;
                     do
                     {
                         String receivedMessage = new demoS().ReceiveMess(lstConnect.get(countStep6));
                         String temp = new String();
                         temp = " " + receivedMessage;
                         quanbaiclient.concat(temp);

                         System.out.println("luot chia bai thu " + (luot +1) + " player " + (countStep6 +1) + " chon " + receivedMessage);
                         countStep6 = countStep6 + 1;
                      }
                      while(countStep6<numclient);

                      //g?i 4 lá bŕi tr? l?i cho 4 client
                     do
                     {
                         new demoS().SentMess(lstConnect.get(countStep7), quanbaiclient);
                         countStep7 = countStep7 + 1;
                      }
                      while(countStep7<numclient);
                     
                     //nh?n ?i?m c?a 4 client
                      do
                      {
                         int Score = 0;
                         String receivedMessage = new demoS().ReceiveMess(lstConnect.get(countStep8));
                         switch (countStep8)
                         {
                             case 0:
                                 Score =  Integer.parseInt(receivedMessage);
                                 diem1 += Score;
                                 break;
                             case 1:
                                 Score = Integer.parseInt(receivedMessage);
                                  diem2 += Score;
                                 break;
                             case 2:
                                 Score = Integer.parseInt(receivedMessage);
                                 diem3 += Score;
                                 break;
                             case 3:
                                 Score = Integer.parseInt(receivedMessage);
                                 diem4 += Score;
                                 break;
                         }

                     System.out.println("luot chia bai thu " + (luot +1) + " player " + (countStep8 +1) + " duoc " + receivedMessage + " diem");
                     countStep8 = countStep8 + 1;
                  }
                  while(countStep8<numclient);


                    //n?u t?i l??t 13 thě s? g?i chu?i xong van có format: XongVan ten1 diem1 ten2 diem2...
                     if(turn == 12)
                     {
                        String temp = "XongVan" + " " + lstName.get(countStep9)+ " " +  Integer.toString(diem1) + " " + lstName.get(countStep9 + 1) + " " + Integer.toString(diem2)+ " " + lstName.get(countStep9 + 2) + " " + Integer.toString(diem3) + " " + lstName.get(countStep9 + 3) + " " + Integer.toString(diem4) ;
                         do
                         {
                            new demoS().SentMess(lstConnect.get(countStep9), temp);
                            countStep9 = countStep9 + 1;
                         }
                         while(countStep9<numclient);
                     }
                     else
                     {
                            //g?i tin xong l??t b?t ??u l??t m?i
                            String temp = "XongLuot";
                            do
                            {
                                new demoS().SentMess(lstConnect.get(countStep10), temp);
                                countStep10 = countStep10 + 1;
                            }
                            while(countStep10<numclient);
                     }

                 }
      
             }while(diem1 < 100 && diem2 < 100 && diem3 < 100 && diem4 < 100);
             
             // Thông báo có ng??i th?ng cu?c,k?t thúc game
        }
        catch (Exception e) {}
    }

         public String ReceiveMess(Socket socket)
    {
        BufferedReader in = null;
        String mess = "";
        try
        {
             in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             mess = in.readLine();
             
        }
         catch (Exception e)
         {

         }

        return  mess;
    }

    public void SentMess(Socket socket, String string)
    {
        PrintWriter out = null;
        try
        {
             out = new PrintWriter(socket.getOutputStream(), true);
             out.println(string);
        }
        catch (Exception e)
         {
              System.out.println("There're some error");
         }
    }

    public void chiaBai()
    {
       
        for(int i = 0; i< Bai.size(); i += 4)
        {
            chiabai1.add(Bai.get(i) );
            chiabai2.add(Bai.get(i+1));
            chiabai3.add(Bai.get(i+2) );
            chiabai4.add(Bai.get(i+3));
        }
    }
  
    public void XaoBai ( )
    {
        Random r = new Random();
        for(int i =0; i< Bai.size(); i++)
        {
            int j = r.nextInt(Bai.size());
            if(i != j)
            {
               String temp = new String();
               temp = Bai.get(i);
               Bai.set(i, Bai.get(j));
               Bai.set(j, temp);

            }
        }
        
    }
    public List<String> TaoBai()
    {
        Bai = new ArrayList<String>();

            for(int i=0; i< QuanBai.length; i++)
            {
              for(int j = 0; j<LoaiQuan.length; j++)
              {
                  String temp = new String();
                  temp = QuanBai[i] + LoaiQuan[j];
                  Bai.add(temp);
              }
            }

        return  Bai;
    }
    
    // Chuy?n list bŕi vŕo string
    public void ListToString()
    {

         for(int i = 0; i < 13; i++)
         {
            chuoibai1 = chuoibai1 + " " + chiabai1.get(i);
            chuoibai2 = chuoibai2 + " " + chiabai2.get(i);
            chuoibai3 = chuoibai3 + " " + chiabai3.get(i);
            chuoibai4 = chuoibai4 + " " + chiabai4.get(i);
          }
    }
}


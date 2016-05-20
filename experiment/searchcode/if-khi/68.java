/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package smtsolver;

import java.io.*;
import java.net.Socket;

/**
 *
 * @author Hoang The Tung
 */
public class WriteThread extends Thread {

    public boolean active = false;
    //Trang thai cua luong ghi

    public String SID = new String();
    //sessionID cua luong ghi

    private Process proc;
    //Tien trinh thuc thi

    private Socket sk;
    //Socket ket noi.

    public WriteThread(Socket sk){
        //Ham khoi ttao
        this.active = false;
        this.sk = sk;
    }

    public void setSID(String sid){
        //Thiet lap sessionID cho luong lam viec
        this.SID = sid;
    }
    public void setActive(boolean status){
        //Thiet lap trang thai lam viec cho luong
        this.active= status;
    }

    public void interruptSolver(){
        //Ngat tien trinh dang thuc thi
        this.proc.destroy();
        this.active = false;
        //Tra ve trang thai ranh cho tien trinh
    }

    public void setProc(Process pr){
        //thiet lap tien trinh thuc thi
        this.proc = pr;
    }

  
    private void sendFile(){
        //Qua trinh gui file ket qua cho may chu
        try{

            PrintWriter pw = new PrintWriter(this.sk.getOutputStream(),true);

            pw.println(this.SID+"|"+config._tag_result);
            //Gui tin hieu gui ket qua

            String filename = config.pathFile + this.SID+".txt";
            //Xac dinh tep tin can gui

            File f = new File(filename);

            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));


            while(dis.available() !=0) {
                //Qua trinh gui ket qua
                pw.println(this.SID+"|"+dis.readLine());
            }

            pw.println(this.SID+"|"+config._closetag_result);
            //Gui tin hieu ket thuc gui ket qua
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public void run(){
        try{

            BufferedReader Resultset = new BufferedReader(new InputStreamReader (this.proc.getInputStream()));

            String line = "";
            while (!line.equals("done!")) {
                //Cho tien trinh goi solver thuc thi xong
                line = Resultset.readLine();
                System.out.println(line);
            }
            if (line.equals("done!")){
                //Gui tep tin ket qua sau khi thuc thi xong
                sendFile();

                this.active = false;
                //Tra lai trang thai ranh cho tien trinh ghi du lieu

                this.stop();
                //dung tien trinh ghi du lieu tuong ung vs sessionID da co ket qua tra ve
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}


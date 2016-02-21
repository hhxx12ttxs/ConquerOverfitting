package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import java.io.*;
import java.util.List;

/**
 * Created by yanrunfa on 2016/2/20.
 */
public class ShellUtils {

    /**
     *
     * @param p the process
     * @return the shell out
     * @throws IOException
     */
    public static String getShellOut(Process p) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedInputStream in = null;
        BufferedReader br = null;
        try {
            Thread t=new Thread(new ErrorStreamRunnable(p.getErrorStream(),"ErrorStream"));
            t.start();
            in = new BufferedInputStream(p.getInputStream());
            br = new BufferedReader(new InputStreamReader(in));
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(System.getProperty("line.separator"));
                sb.append(s);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (br != null){
                br.close();
            }
            if (in != null){
                in.close();
            }
        }
        return sb.toString();
    }

    /**
     *
     * @param args the args to be run in the shell
     * @return the result print of the run
     * @throws IOException
     */
    public static String shellRun(List<String> args) throws IOException{
        String fileName;
        String cmd;
        if (System.getProperty("os.name").toLowerCase().startsWith("win")){
            fileName = System.getProperty("user.dir")+"/temp/"+"/args.bat";
            cmd = System.getProperty("user.dir")+"/temp/"+"/args.bat";
        }
        else {
            fileName = System.getProperty("user.dir")+"/temp/"+"/args.sh";
            cmd = "bash " + System.getProperty("user.dir")+"/temp/"+"/args.sh";
        }
        File batFile = new File(fileName);
        if (!batFile.exists()){
            boolean result = batFile.createNewFile();
            if (!result){
                throw new IOException("Cannot Create bat file:" + fileName);
            }
        }
        FileOutputStream outputStream = new FileOutputStream(batFile);
        for (String arg: args){
            outputStream.write(arg.getBytes());
        }
        outputStream.close();
        batFile.deleteOnExit();
        Process process= Runtime.getRuntime().exec(cmd);
        return ShellUtils.getShellOut(process);
    };
}


class ErrorStreamRunnable implements Runnable
{
    BufferedReader bReader=null;
    String type=null;
    public ErrorStreamRunnable(InputStream is, String _type)
    {
        try
        {
            bReader=new BufferedReader(new InputStreamReader(new BufferedInputStream(is),"UTF-8"));
            type=_type;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    public void run()
    {
        String line;
        try
        {
            while((line=bReader.readLine())!=null)
            {
                System.out.println(line);
            }
            bReader.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
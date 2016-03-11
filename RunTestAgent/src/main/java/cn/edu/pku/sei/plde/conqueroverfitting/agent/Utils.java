package cn.edu.pku.sei.plde.conqueroverfitting.agent;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 2016/2/20.
 */
public class Utils {

    /**
     *
     * @param tempJavaName
     * @param tempClassName
     * @param classPath
     * @param srcPath
     * @param className
     * @param targetLine
     * @param addingCode
     * @return
     * @throws IOException
     */
    public static byte[] AddCodeToSource(String tempJavaName, String tempClassName, String classPath, String srcPath, String className, int targetLine, String addingCode) throws IOException{
        File tempJavaFile = new File(tempJavaName);
        try {
            FileOutputStream outputStream = new FileOutputStream(tempJavaFile);
            BufferedReader reader = new BufferedReader(new FileReader(srcPath+"/"+className.replace(".","/")+".java"));
            String lineString = null;
            int line = 0;
            boolean writed = false;
            while ((lineString = reader.readLine()) != null) {
                line++;
                if (line == targetLine-1){
                    if ((!lineString.contains(";") && !lineString.contains(":") && !lineString.contains("{") && !lineString.contains("}"))|| lineString.contains("return ") || lineString.contains("if (")){
                        outputStream.write(addingCode.getBytes());
                        writed = true;
                    }
                }
                if (line == targetLine && !writed){
                    outputStream.write(addingCode.getBytes());
                }
                outputStream.write((lineString+"\n").getBytes());
                if (lineString.startsWith("package")){
                    outputStream.write("import java.util.Arrays;\n".getBytes());
                    outputStream.write("import java.lang.System;\n".getBytes());
                }
            }
            outputStream.close();
            reader.close();
            String result = Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -cp "+ classPath+" "+ tempJavaName));
            System.out.println(result);
        } catch (FileNotFoundException e){
            System.out.println("ERROR: Cannot Find Source File: "+className+" in Source Path: "+ srcPath);
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return getBytesFromFile(tempClassName);
    }


    /**
     *
     * @param fileName
     * @return
     */
    public static byte[] getBytesFromFile(String fileName) {
        try {
            FileInputStream in=new FileInputStream(fileName);
            ByteArrayOutputStream out=new ByteArrayOutputStream(1024);
            byte[] temp=new byte[1024];
            int size=0;
            while((size=in.read(temp))!=-1)
            {
                out.write(temp,0,size);
            }
            in.close();
            byte[] bytes=out.toByteArray();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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
        return Utils.getShellOut(process);
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
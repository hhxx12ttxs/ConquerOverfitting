package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.main.Config;
import com.gzoltar.core.GZoltar;

import java.io.*;
import java.util.List;
import java.util.concurrent.*;

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
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<String> future = service.submit(new ReadShellProcess(p));
        try {
            return future.get(Config.SHELL_RUN_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e){
            future.cancel(true);
            e.printStackTrace();
            return "";
        } catch (TimeoutException e){
            future.cancel(true);
            e.printStackTrace();
            return "";
        } catch (ExecutionException e){
            future.cancel(true);
            e.printStackTrace();
            return "";
        }
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
            fileName = System.getProperty("user.dir")+"/temp"+"/args.bat";
            cmd = System.getProperty("user.dir")+"/temp"+"/args.bat";
        }
        else {
            fileName = System.getProperty("user.dir")+"/temp"+"/args.sh";
            cmd = "bash " + System.getProperty("user.dir")+"/temp"+"/args.sh";
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


class ReadShellProcess implements Callable<String> {
    public Process p;

    public ReadShellProcess(Process p) {
        this.p = p;
    }

    public synchronized String call() {
        StringBuilder sb = new StringBuilder();
        BufferedInputStream in = null;
        BufferedReader br = null;
        try {
            String s;
            in = new BufferedInputStream(p.getInputStream());
            br = new BufferedReader(new InputStreamReader(in));
            while ((s = br.readLine()) != null && s.length()!=0) {
                if (sb.length() < 1000000){
                    System.out.println(s);
                    sb.append(System.getProperty("line.separator"));
                    sb.append(s);
                }
            }
            br.close();
            in.close();
            in = new BufferedInputStream(p.getErrorStream());
            br = new BufferedReader(new InputStreamReader(in));
            while ((s = br.readLine()) != null && s.length()!=0) {
                if (sb.length() < 1000000){
                    System.out.println(s);
                    sb.append(System.getProperty("line.separator"));
                    sb.append(s);
                }
            }
            br.close();
            in.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        return sb.toString();
    }
}

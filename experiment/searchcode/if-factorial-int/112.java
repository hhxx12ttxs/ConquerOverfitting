package net.sanjayts.snippets.threading;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sanjayts.snippets.db.ConnectionUtils;

/**
 * A test class which demonstrates stopping of a thread/runnable in different conditions
 * e.g. waiting for some network operation, performing a long running calculation etc.
 *
 * References:
 *  https://www.securecoding.cert.org/confluence/display/java/THI06-J.+Ensure+that+threads+performing+blocking+operations+can+be+terminated
 *  http://stackoverflow.com/questions/3590000/thread-interrupt-what-does-it-do
 */
public class ThreadStopTest {

    public static void main(String[] args) {
        testSqlThreadStop();
    }
    
    private static void testSqlThreadStop() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        LongRunningDbTask task = new LongRunningDbTask(); 
        Future<Integer> future = executor.submit(task);
        try {
            Integer result = future.get(1, TimeUnit.SECONDS);
            System.out.println("Computation complete; result: " + result);
        } catch(TimeoutException te) {
            future.cancel(true);
            task.cleanupAfterCancel();
            System.out.println("Computation cancelled");
        } catch(Exception e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }
        
    private static void testSocketReadStop() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        SocketTask task = new SocketTask("http://www.yahoo.com", 80);
        Future<Integer> future = executor.submit(task);
        try {
            Integer result = future.get(1, TimeUnit.SECONDS);
            System.out.println("Computation complete; result: " + result);
        } catch(TimeoutException te) {
            future.cancel(true);
            task.cleanupAfterCancel();
            System.out.println("Computation cancelled");
        } catch(Exception e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }
    
    private static void testFileReadStop() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        final File[] files = new File[] { new File("c:/b.txt"), new File("c:/b.txt") };
        final List<Future<Integer>> futures = new ArrayList<Future<Integer>>(files.length);
        for(final File file : files) {
            FileReadTask task = new FileReadTask(file);
            Future<Integer> future = executor.submit(task);
            futures.add(future);
        }
        for(Future<Integer> future : futures) {
            try {
                Integer result = future.get(3, TimeUnit.SECONDS);
                System.out.println("Computation complete; result: " + result);
            } catch(TimeoutException te) {
                future.cancel(true);
                System.out.println("Computation cancelled");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
    }
    
    private static void testFactorialStop() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        final int[] arr = new int[] { 12345 };
        final List<Future<BigInteger>> futures = new ArrayList<Future<BigInteger>>(arr.length);
        for(int seed : arr) {
            Factorial fact = new Factorial(seed);
            Future<BigInteger> future = executor.submit(fact);
            futures.add(future);
        }
        for(Future<BigInteger> future : futures) {
            try {
                BigInteger result = future.get(3, TimeUnit.SECONDS);
                System.out.println("Computation complete; result: " + result);
            } catch(TimeoutException te) {
                System.out.println("Computation cancelled");
                future.cancel(true);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
    }

}

class Factorial implements Callable<BigInteger> {

    private final int seed;
    
    public Factorial(int seed) {
        this.seed = seed;
    }
    
    @Override
    public BigInteger call() throws Exception {
        if(this.seed < 0) {
            throw new IllegalArgumentException("Can't calculate factorial for negative numbers");
        }
        if(this.seed == 0 || this.seed == 1) {
            return BigInteger.ONE;
        }
        BigInteger result = BigInteger.ONE;
        int tmp = this.seed;
        while(tmp > 0) {
            if(Thread.interrupted()) {
                Thread.currentThread().interrupt();
                break;
            }
            result = result.multiply(BigInteger.valueOf(tmp--));
            System.out.printf("Result [%s]: %s%n", Integer.toString(this.seed), result);
        }
        return result;
    }
    
}

/**
 * Read a given File and return the number of lines read
 * 
 * @author SANJAY
 *
 */
class FileReadTask implements Callable<Integer> {

    private final File file;
    
    public FileReadTask(final File file) {
        this.file = file;
    }
    
    @Override
    public Integer call() throws Exception {
        Scanner reader = new Scanner(this.file);
        int count = 0;
        try {
            while(true) {
                // Code for handling interruption/stopping thread
                if(Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
                if(reader.hasNextLine()) {
                    String line = reader.nextLine();
                    ++count;
                    System.out.println(count + "\t" + line);
                } else {
                    break;
                }
            }
        } finally {
            reader.close();
        }
        return Integer.valueOf(count);
    }
    
}

/**
 * Interface mainly implemented by Callables which intend to cleanup
 * resources or close the resources ASAP technically not possible
 * by just interrupting the task. E.g. blocking I/O can only be
 * cancelled by closing the underlying socket since no InterruptedException
 * is thrown by InputStream's read method. 
 * 
 * @author SANJAY
 *
 * @param <T> The return type of this task
 */
interface CleanableTask<T> extends Callable<T> {
    
    void cleanupAfterCancel();
    
}

class SocketTask implements CleanableTask<Integer> {

    private final String host;
    
    private final int port;
    
    private Socket socket;
    
    public SocketTask(final String host, final int port) {
        this.host = host;
        this.port = port;
    }
    
    @Override
    public Integer call() throws Exception {
        InputStream in = null;
        int bytesRead = 0;
        try {
            this.socket = new Socket(this.host, this.port);
            in = this.socket.getInputStream();
            byte[] bytes = new byte[1000000];
            System.out.println("Started reading bytes");
            
            // The below behavior of waiting for a forceful close can be avoided
            // if we modify the FutureTask class (the default Future impl)
            // by passing in a CleanupHandler whose cleanup() method would be
            // invoked after invoking the `cancel` method or by making all 
            // your tasks implement a CancelledTask interface which has a 
            // `cleanupAfterCancel` method to do the same. :)
            try {
                in.read(bytes);
            } catch(SocketException se) {
                if(Thread.currentThread().isInterrupted()) {
                    System.out.println("All OK; this socket was forcefully closed");
                } else {
                    se.printStackTrace();   // something was seriously wrong
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(in != null)  in.close();
        }
        return Integer.valueOf(bytesRead);
    }

    @Override
    public void cleanupAfterCancel() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
}

class LongRunningDbTask implements CleanableTask<Integer> {
    
    private Connection conn;
    
    private PreparedStatement pStmt;
    
    @Override
    public void cleanupAfterCancel() {
        ConnectionUtils.closeSqlResource(this.pStmt);
        ConnectionUtils.closeSqlResource(this.conn);
    }

    @Override
    public Integer call() throws Exception {
        this.conn = ConnectionUtils.getLocalDerbyConnection();
        this.conn.setAutoCommit(false);
        final String stmtStr = "insert into mytmp values(?, ?, ?, ?, ?)";
        this.pStmt = this.conn.prepareStatement(stmtStr);
        for(int i = 0; i < 65000; ++i) {
            this.pStmt.setString(1, "My First " + i);
            this.pStmt.setString(2, "My Middle" + i);
            this.pStmt.setString(3, "My Last" + i);
            this.pStmt.setInt(4, i);
            this.pStmt.setString(5, "My Edu" + i);
            
            this.pStmt.addBatch();
        }
        System.out.println("Start: " + new Date());
        int[] lengths = this.pStmt.executeBatch();
        lengths = this.pStmt.executeBatch();
        this.conn.commit();
        System.out.println("End: " + new Date());
        return lengths.length;
    }
}    


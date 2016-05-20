package com.level3.meanwhile;

import com.level3.meanwhile.base.BaseChain;
import com.level3.meanwhile.base.BaseTask;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Base class for Meanwhile tests - Timing utilities and useful implementations of Task class
 * 
 * @author Jonathan Griggs <Jonathan.Griggs@Level3.com>
 * @since 0.1
 */
public class MeanwhileTest {
    protected TaskQueue manager;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
       
    }
    
    @Before
    public void setUp() {
        manager = new TaskQueue(10,10);
    }
    
    @After
    public void tearDown() {
        manager.shutdown();
    }
    
    
    public StopWatch stopWatch = new StopWatch();
    
    public class StopWatch {
        private long startTime = 0;
        
        public void start() {
            startTime = System.currentTimeMillis();
        }
        
        public boolean hasElapsed(long millis) {
            if(currentElapsed() < millis) {
                return false;
            }
            return true;
        }
        
        public void block(long millis) {
            start();
            while(!hasElapsed(millis)) {
                //Do work!
            }
        }
        
        public long currentElapsed() {
            return System.currentTimeMillis()-startTime;
        }
    }
    
    public class TimerTask extends RetryTask {
        public long startTime;
        public long queueTime;
        public long endTime;
        public long executionTime = 0;
        public StopWatch stopWatch = new StopWatch();
        
        public TimerTask() {
            super(0);
        }
        
        public TimerTask(int maxRetries) {
            super(maxRetries);
        }
        
        public TimerTask(long exec) {
            super(0);
            executionTime = exec;
        }
        
        public TimerTask(long exec, int maxRetries) {
            super(maxRetries);
            executionTime = exec;
        }
        
        @Override
        public boolean execute() {
            stopWatch.start();
            while(!stopWatch.hasElapsed(executionTime)) {
                //System.out.println(this.getUUID());
            }
            return super.execute();
        }
        
        @Override
        public void onSubmit() {
            super.onSubmit();
            queueTime = System.currentTimeMillis();
        }
        
        @Override
        public void onStart() {
            super.onStart();
            startTime = System.currentTimeMillis();
        }
        
        @Override
        public void onComplete() {
            super.onComplete();
            endTime = System.currentTimeMillis();
        }
    }
    
    class SuccessTask extends BaseChain {
        
        @Override
        public boolean execute() {
            return true;
        }
    }
    
    class FailTask extends BaseChain {
        
        @Override
        public boolean execute() {
            return false;
        }
    }
    
    class LifecycleTask extends BaseTask {
        public boolean executed = false;
        public boolean submitted = false;
        public boolean canceled = false;
        public boolean started = false;
        public boolean completed = false;
        public boolean succeeded = false;
        public boolean failed = false;
        
        @Override
        public boolean execute() {
            executed = true;
            return true;
        }

        @Override
        public void onSubmit() {
            submitted = true;
            super.onSubmit();
        }

        @Override
        public void onStart() {
            started = true;
            super.onStart();
        }

        @Override
        public void onComplete() {
            completed = true;
            super.onComplete();
        }

        @Override
        public void onSuccess() {
            succeeded = true;
            super.onSuccess();
        }

        @Override
        public void onFailure() {
            failed = true;
            super.onFailure();
        }
        
        @Override
        public void onCancel() {
            failed = true;
            super.onCancel();
        }
    }
    
    class RetryTask extends BaseChain {
        public int tryNum = 0;
        int maxRetries = 200;
        private boolean retry = false;
        
        public RetryTask(int maxTries) {
            super();
            maxRetries = maxTries;
        }

        public boolean execute() {
            if(tryNum<maxRetries) {
                retry = true;
                return false;
            }
            return true;
        }
        
        @Override
        public boolean retryOnFail() {
            return retry;
        }

        @Override
        public void onRetry() {
            super.onRetry();
            tryNum++;
        } 
    };
    
    
    class CounterTask extends TimerTask {
        public int submitCount = 0;
        public int startCount = 0;
        public int completeCount = 0;
        public int failureCount = 0;
        public int executeCount = 0;
        public int cancelCount = 0;
        public int retryCount = 0;
        public String name;
        public boolean success = true;
        
        public boolean retry = false;
        
        public CounterTask(String name) {
            super(0L);
            this.name = name;
        }
        
        public CounterTask(String name, boolean success) {
            super(0L);
            this.name = name;
            this.success = success;
        }
        
        public CounterTask(String name, boolean success,long millis) {
            super(millis);
            this.name = name;
            this.success = success;
        }
      

        public boolean execute() {
            executeCount++;
            super.execute();
            return success;
        }
        
        @Override
        public void onCancel() {
            super.onCancel();
            cancelCount++;
        }
        
        @Override
        public void onFailure() {
            super.onFailure();
            failureCount++;
        }
        
        @Override
        public void onComplete() {
            super.onComplete();
            completeCount++;
        }
        
        @Override
        public void onStart() {
            super.onStart();
            startCount++;
        }
        
        @Override
        public void onSubmit() {
            super.onSubmit();
            //System.out.append("Submit");
            submitCount++;
        }
        
        @Override
        public boolean retryOnFail() {
            return retry;
        }

        @Override
        public void onRetry() {
            super.onRetry();
            retryCount++;
        } 
        
        @Override
        public String toString() {
            return name;
        }
    };
    
    class CounterStage extends Stage {
        public int submitCount = 0;
        public int startCount = 0;
        public int completeCount = 0;
        public int failureCount = 0;
        public int executeCount = 0;
        public int cancelCount = 0;
        public int retryCount = 0;
        public String name;
        public boolean success = true;
        
        public boolean retry = false;
        
        public CounterStage(String name) {
            this.name = name;
        }
        
        @Override
        public void onCancel() {
            super.onCancel();
            cancelCount++;
        }
        
        @Override
        public void onFailure() {
            super.onFailure();
            failureCount++;
        }
        
        @Override
        public void onComplete() {
            super.onComplete();
            completeCount++;
        }
        
        @Override
        public void onStart() {
            super.onStart();
            startCount++;
        }
        
        @Override
        public void onSubmit() {
            super.onSubmit();
            //System.out.append("Submit");
            submitCount++;
        }
        
        @Override
        public boolean retryOnFail() {
            return retry;
        }

        @Override
        public void onRetry() {
            super.onRetry();
            retryCount++;
        } 
        
        @Override
        public String toString() {
            return name;
        }
    };
}


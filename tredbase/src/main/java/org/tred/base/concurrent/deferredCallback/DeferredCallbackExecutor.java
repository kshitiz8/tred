package org.tred.base.concurrent.deferredCallback;


import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import static org.tred.base.concurrent.deferredCallback.TestProducer.sout;
/**
 * Design and implement a thread-safe class that allows registeration of callback methods that are executed after a
 * user specified time interval in seconds has elapsed.
 * https://www.educative.io/module/lesson/java-multithreading-interview/BnxVQgRkoRJ
 */

public class DeferredCallbackExecutor {


    public static class CallBack implements Runnable {
        private final String id;
        private final Long registrationTime;
        private final Long tte;
        private final Long executeTime;
        private final BiFunction callback;
        private final Object input;

        public CallBack(Integer tteInSeconds, BiFunction callback, Object input) {
            this.id = UUID.randomUUID().toString();
            this.tte = tteInSeconds * 1000L;
            this.registrationTime = System.currentTimeMillis();
            this.executeTime = this.registrationTime + this.tte;
            this.callback = callback;
            this.input = input;
        }

        public String getId() {
            return this.id;
        }

        public Long getRegistrationTime() {
            return registrationTime;
        }

        public Long getTte() {
            return tte;
        }

        public Long getExecuteTime() {
            return executeTime;
        }

        @Override
        public void run() {
            try {
                sout("[" + this.id + "]: " + "Execution in progress");
                Object o = callback.apply(input, this); // TODO: do we need to return or save the response somewhere
                sout("[" + this.id + "]: " + "Execution finished");
            } catch (Exception e) {
                e.printStackTrace(); //TODO: do something with error to report back
            }

        }

        @Override
        public String toString() {
            return "CallBack{" +
                    "id='" + id + '\'' +
                    ", registrationTime=" + registrationTime +
                    ", tte=" + tte +
                    ", executeTime=" + executeTime +
                    '}';
        }
    }

    private final ExecutorService runnerService;
    private final Thread pollerThread;
    private final PriorityQueue<CallBack> callbackQueue;

    private Boolean drainAndShutDown = false;
    private Boolean shutDown = false;
    Lock lock = new ReentrantLock(true);
    Condition nonEmptyQueue = lock.newCondition();
    Condition emptyQueue = lock.newCondition();

    public DeferredCallbackExecutor() {
        pollerThread = new Thread(this::poller, "Poller");
        runnerService = Executors.newFixedThreadPool(5, r -> {
            Thread t = new Thread(r, "Runner");
            return t;
        });
        callbackQueue = new PriorityQueue<>((c1, c2) -> (int) (c1.getExecuteTime() - c2.getExecuteTime()));
    }

    public void start() {
        sout("Starting Poller");
        pollerThread.start();
    }

    /**
     * Called by Poller Thread
     */
    private void poller() {

        lock.lock();
        while (!shutDown) {
            sout("Awake, Let's see if any job became current");
            while (callbackQueue.size() > 0 && callbackQueue.peek().getExecuteTime() <= System.currentTimeMillis()) {
                CallBack cb = callbackQueue.remove();
                sout("Starting " + cb.getId());
                runnerService.execute(cb);
            }
            if (callbackQueue.size() > 0) {
                long waitTime = callbackQueue.peek().getExecuteTime() - System.currentTimeMillis();
                if(waitTime<=0)continue;
                sout("Poller is Waiting for " + waitTime + "ms");
                try {
                    nonEmptyQueue.await(waitTime, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                }
            }else{
                while(!shutDown && callbackQueue.size()==0) {
                    sout("Poller is waiting until something is inserted");
                    emptyQueue.signalAll();
                    try {
                        nonEmptyQueue.await();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        lock.unlock();


    }

    /**
     * called by Producer Thread
     *
     * @param callback
     */
    public void registerCallback(CallBack callback) {

        lock.lock();

        if (!shutDown && !drainAndShutDown) {
            sout("adding to the job queue:" + callback.getId());
            callbackQueue.add(callback);  //accept if shutdown not called
            nonEmptyQueue.signalAll();

        }
        lock.unlock();

    }

    /**
     * If drainQueue is true, shutdowns after all queue tasks are finished.
     * Else will shutdown after all task which are current are done. No future task is picked.
     * Called by main thread
     */
    public void shutdown(boolean drainQueue) {

        lock.lock();
        if (drainQueue) {
            sout("draining");
            drainAndShutDown = true;
            while (callbackQueue.size() > 0) {
                emptyQueue.awaitUninterruptibly();
            }
        }
        sout("shutting down");
        shutDown = true;
        runnerService.shutdown();
        nonEmptyQueue.signalAll();
        lock.unlock();

    }

    /**
     * shutdown immediately
     */
    public synchronized void shutdownNow() {

    }

}

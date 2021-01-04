package org.tred.base.concurrent.deferredCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestProducer implements  Runnable{
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static void sout(Object message) {
        String tName = Thread.currentThread().getName();
        switch (tName){
            case "Poller":
                sout( ANSI_RED , tName, message);
                break;
            case "Runner":
                sout( ANSI_GREEN , tName, message);
                break;
            case "producers":
                sout( ANSI_BLUE , tName, message);
                break;
            default:
                sout( ANSI_BLACK , tName, message);


        }
    }
    public static void sout(String color, String tName,Object message ){
        System.out.println(color + "["+System.currentTimeMillis()+"]\t"+tName + ": " + message + ANSI_RESET);
    }

    final DeferredCallbackExecutor deferredCallbackExecutor;
    final int maxTime;
    final int delay;

    public TestProducer(DeferredCallbackExecutor deferredCallbackExecutor, int maxTime, int delay) {
        this.deferredCallbackExecutor = deferredCallbackExecutor;
        this.maxTime = maxTime;
        this.delay = delay;
    }



    public void run(){
        Map<String, String> context = new HashMap<>();
        int tte = Math.abs(new Random().nextInt())%maxTime + delay;
        context.put("tte", ""+tte);
        sout("added job with time to execution = " + tte + " seconds");
        DeferredCallbackExecutor.CallBack callBack = new DeferredCallbackExecutor.CallBack(
                tte,
                (o,u) -> {
                    Map<String, String> map = (Map<String, String>) o;
                    DeferredCallbackExecutor.CallBack callBack1 =  (DeferredCallbackExecutor.CallBack)u;
                    sout("Actual Invocation From Consumer: " + callBack1 + " at: " + System.currentTimeMillis() );
                    return null;
                },
                context
        );
        deferredCallbackExecutor.registerCallback(callBack);
    }



    public static void main(String[] args) throws InterruptedException {
        DeferredCallbackExecutor dc = new DeferredCallbackExecutor();
        dc.start();

        ExecutorService producerService = Executors.newFixedThreadPool(5, r->{
            Thread t = new Thread(r,"producers");
            return t;
        }) ;


        for(int i=0;i<100; i++) {
            producerService.execute(new TestProducer(dc, 10, 10));
            Thread.sleep((Math.abs(new Random().nextInt()))%2000);
        }
        producerService.shutdown();
        try {
            producerService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread.sleep(3000);
        dc.shutdown(true);

    }

}

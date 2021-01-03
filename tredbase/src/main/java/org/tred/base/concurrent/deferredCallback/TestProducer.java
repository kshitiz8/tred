package org.tred.base.concurrent.deferredCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestProducer implements  Runnable{
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
        System.out.println(Thread.currentThread().getName() + ":  added job with time to execution = " + tte + " seconds");
        DeferredCallbackExecutor.CallBack callBack = new DeferredCallbackExecutor.CallBack(
                tte,
                (o,u) -> {
                    Map<String, String> map = (Map<String, String>) o;
                    DeferredCallbackExecutor.CallBack callBack1 =  (DeferredCallbackExecutor.CallBack)u;
                    System.out.println(Thread.currentThread().getName() + ": Actual Invocation From Consumer: " + callBack1 + " at: " + System.currentTimeMillis() );
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

        System.out.print("queueing jobs ");
        for(int i=0;i<8; i++) {
            producerService.execute(new TestProducer(dc, 10, 10));
        }
        producerService.shutdown();
        try {
            producerService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread.sleep(30000);
        dc.shutdown(true);

    }

}

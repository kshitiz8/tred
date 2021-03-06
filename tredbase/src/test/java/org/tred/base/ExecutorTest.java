package org.tred.base;

import java.util.concurrent.*;

public class ExecutorTest {
    ExecutorService exS = Executors.newSingleThreadExecutor(r ->{
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

     public int call(int val){
         Future<Integer> f = exS.submit(()-> 5+val);
         try {
             return f.get(10, TimeUnit.MILLISECONDS );
         } catch (InterruptedException e) {
             e.printStackTrace();
             return -1;
         } catch (ExecutionException e) {
             e.printStackTrace();
             return -2;
         } catch (TimeoutException e) {
             e.printStackTrace();
             return -3;
         }
     }

    public int call1(int val){
        ExecutorService exSLocal = Executors.newSingleThreadExecutor();
        Future<Integer> f = exSLocal.submit(()-> 5+val);
        try {
            return f.get(10, TimeUnit.MILLISECONDS );
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return -2;
        } catch (TimeoutException e) {
            e.printStackTrace();
            return -3;
        }finally {
            exSLocal.shutdownNow();
        }
    }


    public static void main(String[] args) {
        long st = System.currentTimeMillis();
        ExecutorTest t= new ExecutorTest();

        for(int i=0; i<10000; i++){
            int j = t.call(i);

        }
        System.out.println("finished");
        System.out.println(System.currentTimeMillis() - st);
    }
}

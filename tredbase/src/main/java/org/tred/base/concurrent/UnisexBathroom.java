package org.tred.base.concurrent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.*;



public class UnisexBathroom {
    static class Demonstration {
        public static void main( String args[] ) throws InterruptedException {
            UnisexBathroom.runTest();
        }
    }
    public static int MALE=0;
    public static int FEMALE=1;

    int maxSlots=3;
    int[] slotsUsedList=new int[]{0,0};


    Lock lock = new ReentrantLock();
    Condition vacated = lock.newCondition();
    // Condition acquired = lock.newCondition();


    public void acquireBathroom(int gender, char id) throws InterruptedException{
        int otherGender = (gender+1)%2;
        lock.lock();
        while(slotsUsedList[otherGender] > 0 || slotsUsedList[gender] == maxSlots){
            System.out.println((gender==0?"Male ":"Female ")+ id + " is waiting");
            vacated.await();
        }
        slotsUsedList[gender]++;
        System.out.println((gender==0?"Male ":"Female ")+ id + " acquired bathroom");
        // acquired.signalAll();
        lock.unlock();


    }

    public void vacateBathroom(int gender, char id){
        lock.lock();
        slotsUsedList[gender]--;
        System.out.println((gender==0?"Male ":"Female ")+ id + " vacated bathroom");
        vacated.signalAll();
        lock.unlock();
    }




    public static void runTest() throws InterruptedException {


        // Genrating starvation condition;

        final UnisexBathroom unisexBathroom = new UnisexBathroom();
        Thread[] maleThreads = new Thread[10];
        Thread[] femaleThreads = new Thread[1];
        for(int i=0;i<10;i++){
            int id = i;
            maleThreads[i] = new Thread(()->{
                try {
                    unisexBathroom.acquireBathroom(MALE, (char)('A'+id));
                    Thread.sleep(Math.abs(new Random().nextInt())%200);
                    unisexBathroom.vacateBathroom(MALE, (char)('A'+id));
                } catch (InterruptedException ie) {

                }
            });
        }

        for(int i=0;i<1;i++){
            int id = i;
            femaleThreads[i] = new Thread(()->{
                try {
                    unisexBathroom.acquireBathroom(FEMALE, (char)('A'+id));
                    Thread.sleep(100);
                    unisexBathroom.vacateBathroom(FEMALE, (char)('A'+id));
                } catch (InterruptedException ie) {

                }
            });
        }
        maleThreads[0].start();
        femaleThreads[0].start();
        for(int i=1;i<10;i++){
            maleThreads[i].start();
        }
        for(int i=0;i<1;i++){
            femaleThreads[i].join();
        }
        for(int i=0;i<10;i++){
            maleThreads[i].join();
        }
    }
}

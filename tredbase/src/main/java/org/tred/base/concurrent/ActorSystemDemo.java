package org.tred.base.concurrent;

import java.util.concurrent.locks.*;
import java.util.concurrent.*;
import java.util.*;

class Actor implements Runnable{

    static class Payload{
        String message;
        String responseId;
        Payload(String message, String responseId){
            this.message = message;
            this.responseId = responseId;
        }
    }

    final String id;

    final Lock lock;
    final Condition done;
    final Condition queueNonEmpty;

    final Map<String, Object> responses;
    final Queue<Payload> jobQueue;

    volatile boolean shutdownTrigger =false;


    public Actor(String id){
        this.id = id;
        lock = new ReentrantLock();
        done = lock.newCondition();
        queueNonEmpty = lock.newCondition();
        this.responses = new HashMap<>();
        this.jobQueue = new LinkedList<>();
    }



    public  String offerAsk(String message){
        try{
            lock.lock();
            String responseId = UUID.randomUUID().toString();
            jobQueue.add(new Payload(message, responseId));
            responses.put(responseId, null);
            queueNonEmpty.signalAll();
            return responseId;
        }finally{
            lock.unlock();
        }

    }

    public  void offerTell(String message){
        lock.lock();
        jobQueue.add(new Payload(message, ""));
        queueNonEmpty.signalAll();
        lock.unlock();
    }


    public Object get(String responseId){
        try{
            lock.lock();
            while(responses.get(responseId) == null){
                try{
                    done.await();
                }catch(InterruptedException e){
                }
            }

            return responses.get(responseId);
        }finally{
            lock.unlock();
        }
    }





    public Object receive(String message) {
        // process
        // return result
        ActorSystemDemo.sout(id + " - received message -  " + message);
        return message + " processed";
    }

    // public Object receive(String message, State map) {
    // // process
    // // return result
    //     return message + " processed";
    // }


    public void run(){
        while(!shutdownTrigger){
            try{
                lock.lock();
                while(jobQueue.isEmpty() && !shutdownTrigger){
                    try{
                        queueNonEmpty.await();
                    }catch(InterruptedException e){

                    }
                }
                if(shutdownTrigger) break;
                Payload p = jobQueue.remove();
                if(!p.responseId.isEmpty()){
                    lock.lock();
                    responses.put(p.responseId, receive(p.message));
                    done.signalAll();
                    lock.unlock();
                }else{
                    receive(p.message);
                }
            }finally{
                lock.unlock();
            }

        }
    }
    public void shutdown(){
        lock.lock();
        shutdownTrigger = true;
        queueNonEmpty.signalAll();
        lock.unlock();
    }
}

class ActorSystem{

    Map<String, Actor> actorMap = new HashMap<>();

    ExecutorService es;

    ActorSystem() {
        es = Executors.newFixedThreadPool(10, r->{
            Thread t = new Thread(r, "Actor");

            return t;
        });
    }
    public void add(Actor actor){
        actorMap.put(actor.id, actor);
        es.execute(actor);

    }

    public void start(){
    }

    public void tell(Actor actor, String message){
        actor.offerTell(message);
    }


    public Object ask(Actor actor, String message){
        String responseId  = actor.offerAsk(message);
        return actor.get(responseId);
    }

    //graceful shutdown
    public void shutdown(){
        es.shutdown();
        actorMap.values().forEach(Actor::shutdown);


    }
}

public class ActorSystemDemo{
    public static void sout(Object o){
        System.out.println(Thread.currentThread().getName() + ":" + o);
    }
    public static void main(String[] args){
        ActorSystem as = new ActorSystem();
        Actor actor1 =new Actor("1");
        Actor actor2 =new Actor("2");

        as.add(actor1);
        as.add(actor2);

        as.tell(actor1, "hello");
        as.tell(actor2, "hello");
        sout(as.ask(actor2, "value"));

        as.shutdown();
    }
}

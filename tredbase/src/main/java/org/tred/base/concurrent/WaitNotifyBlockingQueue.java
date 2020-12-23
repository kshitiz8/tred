package org.tred.base.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class WaitNotifyBlockingQueue<E> implements BlockingQueue<E> {
    final int capacity;
    final Object[] arr;
    int takeFrom = 0;
    int putTo = 0;
    int count = 0;

    public WaitNotifyBlockingQueue(int capacity){
        this.capacity = capacity;
        this.arr = new Object[capacity];


    }
    private synchronized boolean enqueue(E e, long timeout, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(e);

        if(timeout == -1){
            if(count ==capacity){
                return false;
            }

        }else {
            long nanos = unit.toNanos(timeout);
            long millis = unit.toMillis(timeout);
            long remainingNanos = nanos - millis * 1000000;

            System.out.println("enqueue:" + count + ":" + capacity);
            while (count == capacity) {
                System.out.println("waiting in enqueue");
                wait(millis, (int) remainingNanos);
            }

        }

        arr[putTo] = e;
        putTo = (putTo + 1) %capacity;
        count++;
        notifyAll();
        return true;
    }

    @Override
    public  boolean add(E e) {
        if(offer(e)) return true;
        throw new IllegalStateException();
    }

    @Override
    public  boolean offer(E e) {
        try {
            return enqueue(e, -1, null);
        } catch (InterruptedException e1) {
            return false;
        }

    }

    @Override
    public void put(E e) throws InterruptedException {
        enqueue(e, 0, TimeUnit.MILLISECONDS);


    }

    @Override
    public  boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return enqueue(e, timeout, unit);
    }

    // -----------------deque  -------------------------


    private synchronized E dequeue(long timeout, TimeUnit unit) throws InterruptedException {

        if(timeout == -1){
            if(count == 0 ){
                return null;
            }
        }else {
            long nanos = unit.toNanos(timeout);
            long millis = unit.toMillis(timeout);
            long remainingNanos = nanos - millis * 1000000;

            while (count == 0) {
                System.out.println("deque: "+count);
                wait(millis, (int) remainingNanos);
            }

        }

        E ret = (E) arr[takeFrom];
        takeFrom = (takeFrom + 1) %capacity;
        count--;
        notifyAll();
        return ret;
    }

    @Override
    public E remove() {
        E ret = poll();
        if(ret == null){
            throw new NoSuchElementException();
        }
        return ret;
    }

    @Override
    public E poll() {
        try {
            return dequeue(-1, null);
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public synchronized E element() {
        if(count==0){
            throw  new NoSuchElementException();
        }
        return (E) arr[takeFrom];
    }

    @Override
    public synchronized E peek() {
        if(count == 0){
            return null;
        }
        return (E)arr[takeFrom];
    }



    @Override
    public E take() throws InterruptedException {
        return dequeue(0, TimeUnit.MILLISECONDS);
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return dequeue(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized int remainingCapacity() {
        return capacity - count;
    }

    @Override
    public synchronized boolean remove(Object o) {
        throw new RuntimeException("Not Implemented");

    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> c) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public synchronized void clear() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public synchronized int size() {
        return count;
    }

    @Override
    public synchronized boolean isEmpty() {
        return count ==0;
    }

    @Override
    public synchronized boolean contains(Object o) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public synchronized Iterator<E> iterator() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public synchronized Object[] toArray() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public synchronized int drainTo(Collection<? super E> c) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public synchronized int drainTo(Collection<? super E> c, int maxElements) {
        throw new RuntimeException("Not Implemented");
    }
}


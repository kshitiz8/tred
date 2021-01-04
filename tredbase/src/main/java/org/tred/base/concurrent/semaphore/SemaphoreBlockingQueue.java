package org.tred.base.concurrent.semaphore;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreBlockingQueue<E> implements BlockingQueue<E>{

    final Object [] arr;
    final Semaphore filled, empty;
    final Object l1 = new Object();
    final Object l2 = new Object();
    int start = 0;
    int end = 0;
    final private int capacity;
    final boolean fair;



    public SemaphoreBlockingQueue(int capacity) {
        this(capacity, false);
    }
    public SemaphoreBlockingQueue(int capacity, boolean fair) {
        this.capacity = capacity;
        this.fair = fair;
        arr = new Object[capacity];
        filled = new Semaphore(0, fair);
        empty = new Semaphore(capacity, fair);
    }

    @Override
    public boolean add(E e) {
        if(offer(e)) return true;
        throw new IllegalStateException();
    }

    @Override
    public boolean offer(E e) {
        try {
            return offer(e, 0, TimeUnit.SECONDS);
        } catch (InterruptedException e1) {
            return false;
        }
    }

    @Override
    public void put(E e) throws InterruptedException {
        empty.acquire();
        synchronized (l1){ //doesn't honor fairness
            arr[end] = e;
            end = (end +1)%capacity;
        }
        filled.release();


    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if(empty.tryAcquire(timeout, unit)){
            synchronized (l1){
                arr[end] = e;
                end = (end +1)%capacity;
            }
            filled.release();
            return true;
        }
        return false;
    }


    @Override
    public E remove() {
        E ret = poll();
        if(ret == null) throw new NoSuchElementException();
        return ret;
    }

    @Override
    public E poll() {
        try {
            return poll(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }

    }





    @Override
    public E take() throws InterruptedException {
        filled.acquire();
        E ret ;
        synchronized (l2){
            ret = (E) arr[start];
            start = (start +1)%capacity;
        }
        empty.release();
        return ret;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        if(filled.tryAcquire(timeout, unit)){
            E ret;
            synchronized (l2){
                ret = (E) arr[start];
                start = (start +1)%capacity;
            }
            empty.release();
            return ret;
        }
        return null;
    }

    @Override
    public int remainingCapacity() {
        return empty.availablePermits();
    }

    @Override
    public int size() {
        return filled.availablePermits();
    }

    @Override
    public boolean isEmpty() {
        return size()==0;
    }



    //////////

    @Override
    public E element() {
        E ret = peek();
        if(ret == null) throw  new NoSuchElementException();
        return ret;
    }

    @Override
    public E peek() {
//        if(filled.tryAcquire()){
//            E ret;
//            ret = (E) arr[start];  /// should this be locked ??
//            return ret;
//        }
        return null;
    }


    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }



    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }
}

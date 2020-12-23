package org.tred.base.itrators;


import org.junit.Test;
import org.tred.base.concurrent.SemaphoreBlockingQueue;
import org.tred.base.concurrent.WaitNotifyBlockingQueue;
import org.tred.base.iterators.FileIterator;
import org.tred.base.iterators.PrefetchingFileIterator;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

public class TestIterator {
    @Test
    public void testIterators() throws Exception {

        FileIterator fItertor = new FileIterator(new File("src/test/resources/data"));
        PrefetchingFileIterator pfItertor1 = new PrefetchingFileIterator(new File("src/test/resources/data"),new ArrayBlockingQueue<String>(10000));
        PrefetchingFileIterator pfItertor2 = new PrefetchingFileIterator(new File("src/test/resources/data"),new SemaphoreBlockingQueue<String>(10000));
        PrefetchingFileIterator pfItertor3 = new PrefetchingFileIterator(new File("src/test/resources/data"),new WaitNotifyBlockingQueue<>(10000));

        System.out.println("Measuring simple file iterator");
        measure(fItertor);
        System.out.println();


        System.out.println("Measuring file iterator with Prefetching with WaitNotifyBlockingQueue");
        measure(pfItertor3);


        System.out.println("Measuring file iterator with Prefetching with ArrayBlockingQueue");
        measure(pfItertor1);
        System.out.println("Measuring file iterator with Prefetching with SemaphoreBlockingQueue");
        measure(pfItertor2);

    }

    private void measure(Iterator<String> iterator) throws Exception {

        long st = System.currentTimeMillis();
        long length=0;
        while (iterator.hasNext()){
            String line =  iterator.next();
            if(line != null){
                length += line.length();
            }
        }
        System.out.println("Records: "+length);
        System.out.println("TIme taken by FileIterator: " + (System.currentTimeMillis() - st));
    }
}

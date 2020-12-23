package org.tred.base.iterators;

import org.tred.base.concurrent.SemaphoreBlockingQueue;

import java.io.*;
import java.util.Iterator;
import java.util.function.Consumer;

public class FileIterator implements Iterator<String>{
    final File file;
    String nextLine;
    BufferedReader bufferedReader;
    public FileIterator(File file) throws IOException {
        this.file = file;
        bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        nextLine = bufferedReader.readLine();
    }



    @Override
    public boolean hasNext() {
        return nextLine != null;

    }

    @Override
    public String next() {
        String ret = nextLine;
        try {
            nextLine = bufferedReader.readLine();
//            Thread.sleep(10);
        } catch (IOException e) {
            e.printStackTrace();
            nextLine = null;
        }
        return ret;
    }
}

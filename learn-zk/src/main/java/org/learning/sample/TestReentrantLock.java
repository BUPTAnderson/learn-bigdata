package org.learning.sample;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Anderson on 2019/2/18
 */
public class TestReentrantLock {
    ReadWriteLock lock = new ReentrantReadWriteLock();

    public void getReadLock() {
        Lock readLock = null;
        Lock writeLock = null;
        try{
//            writeLock = lock.writeLock();
//            writeLock.lock();
//            System.out.println("get write lock");

            readLock = lock.readLock();
            readLock.lock();
            System.out.println("get read lock");

            writeLock.lock();
            System.out.println("get write lock");
        } finally {
            if (readLock != null) {
                readLock.unlock();
            }
            if (writeLock != null) {
                writeLock.unlock();
            }
        }
    }
    public static void main(String[] args) {
        TestReentrantLock testReentrantLock = new TestReentrantLock();
        testReentrantLock.getReadLock();
    }
}

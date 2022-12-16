package org.example.ch12;

import java.util.concurrent.Semaphore;

public class BoundedBuffer<E>{
    private final Semaphore availableItems; //버퍼 내부에서 뽑아낼 수 있는 항목의 갯수
    private final Semaphore availableSpaces; //버퍼에 추가할 수 있는 항목의 갯수
    private final E[] items;
    private int putPosition = 0;
    private int takePosition = 0;

    public BoundedBuffer(final int capacity) {
        this.availableItems = new Semaphore(0);
        this.availableSpaces = new Semaphore(capacity);
        this.items = (E[]) new Object[capacity];
    }

    public boolean isEmpty() {
        return availableItems.availablePermits() == 0;
    }

    public boolean isFull() {
        return availableSpaces.availablePermits() == 0;
    }

    public void put(E x) throws InterruptedException {
        availableSpaces.acquire();
        doInsert(x);
        availableItems.release();
    }

    public E take() throws InterruptedException {
        availableItems.acquire();
        E item = doExtract();
        availableSpaces.release();
        return item;
    }

    private synchronized void doInsert(final E x) {
        items[putPosition] = x;
        putPosition = (putPosition + 1 == items.length) ? 0 : putPosition;
    }

    private synchronized E doExtract() {
        final E item = items[takePosition];
        items[takePosition] = null;
        takePosition = (takePosition + 1 == items.length) ? 0 : takePosition;
        return item;
    }
}

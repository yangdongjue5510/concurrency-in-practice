package org.example.ch12;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BoundedBufferTest {

    /**
     * 가장 기본적인 유닛 테스트는 순차적인 개념으로 테스트를 작성해도 문제가 되지 않는다. 이런 기본적인 테스트는 문제가 생겼을 시 클래스 자체의 문제인지 멀티 스레드 환경에서 생긴 문제인지 판단할 때
     * 유용하다.
     */
    @Test
    void testIsEmptyWhenConstructed() {
        BoundedBuffer<Integer> bb = new BoundedBuffer<>(10);
        assertTrue(bb.isEmpty());
        assertFalse(bb.isFull());
    }

    @Test
    void testIsFullAfterPuts() throws InterruptedException {
        BoundedBuffer<Integer> bb = new BoundedBuffer<>(10);
        for (int i = 0; i < 10; i++) {
            bb.put(i);
        }
        assertTrue(bb.isFull());
        assertFalse(bb.isEmpty());
    }

    /**
     * 블로킹 메서드 테스트는 해당 스레드가 대기 상태인지 테스트하기 위해 인터럽트를 발생시킨다.
     * Thread.getState()메서드는 JVM 구현에 따라 스핀 대기 기법을 사용할 수 있기 때문에 대기 상태 때 항상 WAITING, TIME_WAITING 상태가 아닐 수 있다.
     */
    @Test
    void testTakeBlockWhenEmpty() {
        final BoundedBuffer<Integer> bb = new BoundedBuffer<>(10);
        Thread taker = new Thread(() -> {
            try {
                int unused = bb.take();
                throw new IllegalArgumentException("대기하지 않고 진행하면 테스트 실패");
            } catch (InterruptedException e) {
                // 테스트 통과
            }});
        try {
            taker.start();
            taker.interrupt();
            taker.join();
            assertFalse(taker.isAlive());
        } catch (Exception e) {
            throw new IllegalArgumentException("예외가 발생하면 테스트 실패");
        }
    }
}

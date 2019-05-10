package becomejavasenior.task1;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Есть счетчик, подсчитывающий количество вызовов.
 * <p>
 * Почему счетчик показывает разные значения и не считает до конца?
 * Как это можно исправить не используя synchronized?
 * <p>
 * Попробуйте закомментировать обращение к yield().
 * Измениться ли значение?
 */
public class AtomicCounterTutor {
    //AtomicInteger counter1 = new AtomicInteger(0);
    volatile int counter = 0;

    class TestThread implements Runnable {
        String threadName;

        public TestThread(String threadName) {
            this.threadName = threadName;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10000; i++) {
                synchronized (AtomicCounterTutor.class) {
                    counter++;
                }
                //counter1.incrementAndGet();
                Thread.yield();
            }
        }
    }

    @Test
    public void testThread() {
        long ts = System.nanoTime();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threads.add(new Thread(new TestThread("t" + i)));
        }
        System.out.println("Starting threads");
        for (int i = 0; i < 100; i++) {
            threads.get(i).start();
        }
        try {
            for (int i = 0; i < 100; i++) {
                threads.get(i).join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Counter=" + counter);
        System.out.println("Duration = " + (System.nanoTime() - ts) / 1000000.0);
    }
}
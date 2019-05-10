package becomejavasenior.task1;

import org.junit.Test;
import sun.security.krb5.internal.TGSRep;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Есть счетчик, подсчитывающий количество вызовов.
 * <p>
 * Почему счетчик показывает разные значения и не считает до конца?
 * Как это можно исправить не используя synchronized?
 * <p>
 * Попробуйте закомментировать обращение к yield().
 * Измениться ли значение?
 */
public class AtomicCounterTutor2 {
    /*
    Решение задачи через рефакторинг
    Синхронизация метода работает быстрее, чем синхронизация блока. Поэтому это решение лучше

   Однвко from journaldev
   When a method is synchronized, it locks the Object, if method is static it locks the Class,
   so it’s always best practice to use synchronized block to lock the only sections of method that needs synchronization.

    Интересное наблюдение - после остановки потока можно вызывать его методы
     */

    class ManipulateThread implements Runnable {

        String threadName;
        IncrementThread inc;

        public ManipulateThread(String threadName, IncrementThread inc) {
            this.threadName = threadName;
            this.inc = inc;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10000; i++) {
                inc.increment();
            }
        }
    }

    class IncrementThread extends Thread {
        volatile int counter = 0;
        String threadName;

        public IncrementThread(String threadName) {
            this.threadName = threadName;
        }

        public synchronized void increment() {
            counter++;
        }

        public int getCount() {
            return counter;
        }

        @Override
        public void run() {
            do {
                //Thread.yield();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    this.interrupt();
                }
            } while (true);
        }
    }

    @Test
    public void testThread() {
        long ts = System.nanoTime();
        List<Thread> threads = new ArrayList<>();
        IncrementThread inc = new IncrementThread("main thread");
        for (int i = 0; i < 100; i++) {
            threads.add(new Thread(new ManipulateThread("t" + i, inc)));
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
        inc.interrupt();
        System.out.println("Counter=" + inc.getCount());
        System.out.println("Duration = " + (System.nanoTime() - ts) / 1000000.0);
    }
}
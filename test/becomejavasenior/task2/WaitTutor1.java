package becomejavasenior.task2;

import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Как сделать так, чтобы потоки вызывались по очереди?
 * <p>
 * Часто необходимо упорядочить потоки, т.к. результат одного потока
 * понадобится другому, и нужно дождаться, когда первый поток сделает свою работу.
 * <p>
 * Задача: добавьте еще один поток, который будет выводить в лог сообщения о
 * значениях счетчика, кратных 10, например 10, 20, 30...
 * При этом такие сообщения должны выводиться после того, как все потоки преодолели
 * кратность 10, но до того, как какой-либо поток двинулся дальше.
 */

/*
Работает, но получилось черезчур сложно и не прозрачно
 */
public class WaitTutor1 {
    Object monitor = new Object();
    Object logMonitor1 = new Object();
    Object logMonitor2 = new Object();
    int runningThreadNumber = 2;
    volatile int t1Counter = 0;
    volatile int t2Counter = 0;

    class LogThread implements Runnable {

        private BlockingQueue<String> queue;

        LogThread(BlockingQueue<String> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            int currentMessageNumber = 0;
            int number;
            while (true) {
                try {
                    Thread.sleep(200);
                    number = Integer.valueOf(queue.take());
                    currentMessageNumber++;
                    //System.out.println("Message has been received. currentMessageNumber = " + currentMessageNumber);
                    if (currentMessageNumber == runningThreadNumber) {
                        currentMessageNumber = 0;
                        System.out.println("Current progress is " + number + "%");
                        synchronized (logMonitor1) {
                            logMonitor1.notify();
                        }
                        synchronized (logMonitor2) {
                            logMonitor2.notify();
                        }
                    }
                } catch (InterruptedException e) {
//                    e.printStackTrace();
                }

            }
        }
    }

    class TestThread implements Runnable {
        private BlockingQueue<String> queue;
        String threadName;
        int n;

        public TestThread(BlockingQueue<String> queue, String threadName, int n) {
            this.queue = queue;
            this.threadName = threadName;
            this.n = n;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100;) {
                i++;
//                System.out.println(threadName + ":" + i);
                if (i % 10 == 0) {
                    try {
//                        System.out.println("i [in thread " + n + "] % 10 === 0");
                        if (n == 1) {
                            synchronized (logMonitor1) {
                                queue.put(Integer.toString(i));
//                                System.out.println("Message has been send. Thread number = " + n);
                                logMonitor1.wait();
                            }
                        }
                        if (n == 2) {
                            synchronized (logMonitor2) {
                                queue.put(Integer.toString(i));
//                                System.out.println("Message has been send. Thread number = " + n);
                                logMonitor2.wait();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (monitor) {
                    if (n == 1) t1Counter = i;
                    if (n == 2) t2Counter = i;
                    monitor.notify();
                    //Thread.yield();
                    try {
                        if (n == 1) {
                            if (i > t2Counter) {
//                                System.out.println("t1 is ahead with i=" + i + ", wait for t2Counter=" + t2Counter);
                                monitor.wait();
                            }
                        }
                        if (n == 2) {
                            if (i > t1Counter) {
//                                System.out.println("t2 is ahead with i=" + i + ", wait for t1Counter=" + t1Counter);
                                monitor.wait();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Thread.yield();
            }
        }
    }

    @Test
    public void testThread() {
        Thread t1, t2, t3;
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        t1 = new Thread(new TestThread(queue, "t1", 1));
        t2 = new Thread(new TestThread(queue, "t2", 2));
        t3 = new Thread(new LogThread(queue));
        System.out.println("Starting threads");
        t1.start();
        t2.start();
        t3.start();

        System.out.println("Waiting for threads");
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t3.interrupt();
    }
}
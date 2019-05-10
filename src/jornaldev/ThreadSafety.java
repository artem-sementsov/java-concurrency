package jornaldev;

public class ThreadSafety {

    public static void main(String[] args) throws InterruptedException {

        ProcessingThread pt = new ProcessingThread();
        Thread t1 = new Thread(pt, "t1");
        t1.start();
        Thread t2 = new Thread(pt, "t2");
        t2.start();
        //wait for threads to finish processing
        t1.join();
        t2.join();
        System.out.println("Processing count="+pt.getCount());
    }

}

class ProcessingThread implements Runnable{
    // общая переменная для всех потоков
    private int count;
    private Object monitor = new Object();

    @Override
    public void run() {
        for(int i=1; i < 5; i++){
            processSomething(i);
            synchronized (monitor) {
                count++;
            }
        }
    }

    public int getCount() {
        return this.count;
    }

    private void processSomething(int i) {
        // processing some job
        try {
            Thread.sleep(i*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

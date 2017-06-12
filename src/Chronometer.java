import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

/**
 * Created by pavel on 12.06.17.
 */
public class Chronometer {

    private static final Random random = new Random();
    private int duplicates[] = new int [100];
    private volatile boolean stop = false;
    private Object monitor = new Object();

    Thread randomGegerator = new Thread(new Runnable() {

        @Override
        public void run() {
            while (!stop) {
                int rnd = random.nextInt(100);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (monitor) {
                    incrementDuplicateCounter(rnd);
                    monitor.notifyAll();
                }
            }
        }
    });

    private void incrementDuplicateCounter(int index) {
        duplicates[index]++;
    }

    Thread duplicateCounter = new Thread(new Runnable() {

        private int invocationNumber;

        @Override
        public void run() {
            while (!stop) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    invocationNumber++;
                    if (invocationNumber == 5) {
                        showDuplicates();
                        invocationNumber = 0;
                    }
                }
            }
        }
    });

    private void showDuplicates() {
        System.out.println("All duplicates:");
        for (int duplicateNumber = 0; duplicateNumber < 100; duplicateNumber++) {
            int currentNum = duplicates[duplicateNumber];
            if (currentNum != 0) {
                System.out.printf("%d : %d\n", duplicateNumber, currentNum);
                if (currentNum == 5) stop = true;
            }
        }
    }

    public void startProcessing() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(randomGegerator);
        executor.execute(duplicateCounter);
        executor.shutdown();
        while (!executor.isTerminated()) {}
    }
}

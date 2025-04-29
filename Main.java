import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

class ProcessThread extends Thread {
    int pid, arrivalTime, burstTime, priority;

    public ProcessThread(int pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
    }

    public void run() {
        try {
            Thread.sleep(arrivalTime * 1000L); // simulate arrival time
        } catch (InterruptedException e) {}

        System.out.println("[Process " + pid + "] Arrived at " + arrivalTime + 
            ", Priority: " + priority + ", Burst: " + burstTime + "s - Started.");

        try {
            Thread.sleep(burstTime * 1000L); // simulate CPU burst
        } catch (InterruptedException e) {}

        System.out.println("[Process " + pid + "] Finished.");
    }
}

// --- Producer-Consumer remains unchanged ---
class BoundedBuffer {
    private final Queue<Integer> buffer = new LinkedList<>();
    private final int capacity;
    private final Semaphore full;
    private final Semaphore empty;
    private final Lock mutex;

    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
        full = new Semaphore(0);
        empty = new Semaphore(capacity);
        mutex = new ReentrantLock();
    }

    public void produce(int item, int producerId) throws InterruptedException {
        System.out.println("[Producer " + producerId + "] Waiting to produce...");
        empty.acquire();
        mutex.lock();
        try {
            buffer.add(item);
            System.out.println("[Producer " + producerId + "] Produced item " + item);
        } finally {
            mutex.unlock();
            full.release();
        }
    }

    public int consume(int consumerId) throws InterruptedException {
        System.out.println("[Consumer " + consumerId + "] Waiting to consume...");
        full.acquire();
        mutex.lock();
        try {
            int item = buffer.remove();
            System.out.println("[Consumer " + consumerId + "] Consumed item " + item);
            return item;
        } finally {
            mutex.unlock();
            empty.release();
        }
    }
}

class Producer extends Thread {
    private final BoundedBuffer buffer;
    private final int id;
    private final int produceCount;

    public Producer(BoundedBuffer buffer, int id, int produceCount) {
        this.buffer = buffer;
        this.id = id;
        this.produceCount = produceCount;
    }

    public void run() {
        for (int i = 0; i < produceCount; i++) {
            try {
                buffer.produce(i, id);
                Thread.sleep(500); // simulate production time
            } catch (InterruptedException e) {}
        }
    }
}

class Consumer extends Thread {
    private final BoundedBuffer buffer;
    private final int id;
    private final int consumeCount;

    public Consumer(BoundedBuffer buffer, int id, int consumeCount) {
        this.buffer = buffer;
        this.id = id;
        this.consumeCount = consumeCount;
    }

    public void run() {
        for (int i = 0; i < consumeCount; i++) {
            try {
                buffer.consume(id);
                Thread.sleep(800); // simulate consumption time
            } catch (InterruptedException e) {}
        }
    }
}

// --- Main Execution ---
public class Main {
    public static void main(String[] args) throws IOException {
        List<ProcessThread> processThreads = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("processes.txt"))) {
            String line;
            boolean skipHeader = true;
            while ((line = br.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String[] parts = line.trim().split("\\s+");
                if (parts.length < 4) continue;

                int pid = Integer.parseInt(parts[0]);
                int arrival = Integer.parseInt(parts[1]);
                int burst = Integer.parseInt(parts[2]);
                int priority = Integer.parseInt(parts[3]);

                processThreads.add(new ProcessThread(pid, arrival, burst, priority));
            }
        }

        for (ProcessThread pt : processThreads) {
            pt.start();
        }

        // Synchronization Example: Producer-Consumer
        int bufferCapacity = 2;
        BoundedBuffer buffer = new BoundedBuffer(bufferCapacity);

        Producer producer = new Producer(buffer, 1, 4); // produce 4 items
        Consumer consumer1 = new Consumer(buffer, 1, 2);
        Consumer consumer2 = new Consumer(buffer, 2, 2);

        producer.start();
        consumer1.start();
        consumer2.start();

        // Join all threads
        for (ProcessThread pt : processThreads) {
            try { pt.join(); } catch (InterruptedException e) {}
        }
        try {
            producer.join();
            consumer1.join();
            consumer2.join();
        } catch (InterruptedException e) {}
    }
}
import java.io.*;
import java.util.*;

public class ProcessSimulator {
    public static void main(String[] args) {
        List<ProcessThread> processes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("processes.txt"))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header
                    continue;
                }

                String[] parts = line.trim().split("\\s+");
                if (parts.length < 3) continue;

                int pid = Integer.parseInt(parts[0]);
                int burstTime = Integer.parseInt(parts[2]); // Burst_Time is 3rd column

                processes.add(new ProcessThread(pid, burstTime));
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        // Start all threads
        for (ProcessThread pt : processes) {
            pt.start();
        }

        // Wait for all to finish
        for (ProcessThread pt : processes) {
            try {
                pt.join();
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }

        System.out.println("All processes completed.");
    }
}


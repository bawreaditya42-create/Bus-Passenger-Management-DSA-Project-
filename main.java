import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class main {
    static final int CAPACITY = 40;
    static final String FILE_NAME = "passengers.txt";

    static class Passenger {
        String id;
        String name;
        String destination;
        String status;

        Passenger(String id, String name, String destination, String status) {
            this.id = id;
            this.name = name;
            this.destination = destination;
            this.status = status;
        }

        String toFileLine() {
            return id + "|" + name + "|" + destination + "|" + status;
        }
    }

    static class PassengerQueue {
        private Passenger[] data = new Passenger[10];
        private int front = 0;
        private int size = 0;

        boolean isEmpty() {
            return size == 0;
        }

        int size() {
            return size;
        }

        void enqueue(Passenger p) {
            if (size == data.length) {
                grow();
            }
            int rear = (front + size) % data.length;
            data[rear] = p;
            size++;
        }

        Passenger dequeue() {
            if (isEmpty()) {
                return null;
            }
            Passenger p = data[front];
            data[front] = null;
            front = (front + 1) % data.length;
            size--;
            return p;
        }

        Passenger get(int index) {
            return data[(front + index) % data.length];
        }

        private void grow() {
            Passenger[] bigger = new Passenger[data.length * 2];
            for (int i = 0; i < size; i++) {
                bigger[i] = data[(front + i) % data.length];
            }
            data = bigger;
            front = 0;
        }
    }

    static PassengerQueue waitingQueue = new PassengerQueue();
    static ArrayList<Passenger> boardedList = new ArrayList<>();
    static int nextIdNumber = 101;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        loadFromFile();

        int choice;
        do {
            printMenu();
            choice = readInt(sc);

            switch (choice) {
                case 1:
                    registerPassenger(sc);
                    break;
                case 2:
                    boardNextPassenger();
                    break;
                case 3:
                    displayWaitingQueue();
                    break;
                case 4:
                    searchByName(sc);
                    break;
                case 5:
                    saveToFile();
                    System.out.println("Data saved to " + FILE_NAME + ". Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1-5.");
            }
        } while (choice != 5);

        sc.close();
    }

    static void printMenu() {
        System.out.println("\n===== Bus Passenger Management =====");
        System.out.println("1. Register Passenger");
        System.out.println("2. Board Next Passenger");
        System.out.println("3. Display Waiting Queue");
        System.out.println("4. Search by Name");
        System.out.println("5. Exit");
        System.out.print("Enter choice: ");
    }

    static int readInt(Scanner sc) {
        String line = sc.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static void registerPassenger(Scanner sc) {
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Destination: ");
        String destination = sc.nextLine().trim();

        if (name.isEmpty() || destination.isEmpty()) {
            System.out.println("Name and destination cannot be empty.");
            return;
        }

        name = name.replace("|", "/");
        destination = destination.replace("|", "/");

        String id = "PS" + nextIdNumber;
        nextIdNumber++;

        Passenger p = new Passenger(id, name, destination, "Waiting");
        waitingQueue.enqueue(p);

        System.out.println("Passenger registered. ID: " + p.id + " | " + p.name
                + " | " + p.destination + " | Queue position: " + waitingQueue.size());
    }

    static void boardNextPassenger() {
        if (boardedList.size() >= CAPACITY) {
            System.out.println("Bus is full. Boarding not possible.");
            return;
        }
        if (waitingQueue.isEmpty()) {
            System.out.println("No passengers waiting.");
            return;
        }
        Passenger p = waitingQueue.dequeue();
        p.status = "Boarded";
        boardedList.add(p);
        System.out.println("Passenger " + p.id + " boarded. Seats left: "
                + (CAPACITY - boardedList.size()));
    }

    static void displayWaitingQueue() {
        if (waitingQueue.isEmpty()) {
            System.out.println("No passengers waiting.");
            return;
        }
        System.out.println("--- Waiting Queue ---");
        for (int i = 0; i < waitingQueue.size(); i++) {
            Passenger p = waitingQueue.get(i);
            System.out.println((i + 1) + ". " + p.id + " | " + p.name + " | " + p.destination);
        }
    }

    static void searchByName(Scanner sc) {
        System.out.print("Enter name to search: ");
        String target = sc.nextLine().trim();
        boolean found = false;

        for (int i = 0; i < waitingQueue.size(); i++) {
            Passenger p = waitingQueue.get(i);
            if (namesMatch(p.name, target)) {
                printFound(p);
                found = true;
            }
        }

        for (int i = 0; i < boardedList.size(); i++) {
            Passenger p = boardedList.get(i);
            if (namesMatch(p.name, target)) {
                printFound(p);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Passenger not found.");
        }
    }

    static boolean namesMatch(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        for (int i = 0; i < a.length(); i++) {
            char c1 = Character.toLowerCase(a.charAt(i));
            char c2 = Character.toLowerCase(b.charAt(i));
            if (c1 != c2) {
                return false;
            }
        }
        return true;
    }

    static void printFound(Passenger p) {
        System.out.println("Found: " + p.id + " | " + p.name + " | " + p.destination
                + " | Status: " + p.status);
    }

    static void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }
        int maxId = 100;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|");
                if (parts.length < 4) {
                    continue;
                }
                String id = parts[0];
                String name = parts[1];
                String dest = parts[2];
                boolean boarded = parts[3].equalsIgnoreCase("Boarded");

                Passenger p = new Passenger(id, name, dest, boarded ? "Boarded" : "Waiting");
                if (boarded) {
                    boardedList.add(p);
                } else {
                    waitingQueue.enqueue(p);
                }

                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (Exception ignored) {
                }
            }
            nextIdNumber = maxId + 1;
        } catch (IOException e) {
            System.out.println("Could not read " + FILE_NAME + ": " + e.getMessage());
        }
    }

    static void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < boardedList.size(); i++) {
                pw.println(boardedList.get(i).toFileLine());
            }
            for (int i = 0; i < waitingQueue.size(); i++) {
                pw.println(waitingQueue.get(i).toFileLine());
            }
        } catch (IOException e) {
            System.out.println("Could not save " + FILE_NAME + ": " + e.getMessage());
        }
    }
}
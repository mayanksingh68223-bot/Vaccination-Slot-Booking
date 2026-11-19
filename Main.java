import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Vaccination Slot Booking System
 * College: Rungta International Skills University
 *
 * A console application using a circular queue, HashMap and file handling.
 */
public class Main {
    private static final int CAPACITY = 3;
    private static final String FILE_NAME = "bookings.txt";

    private static final class Booking {
        private final String id;
        private final String name;
        private final int age;

        Booking(String id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return id + " | " + name + " | Age: " + age;
        }
    }

    /** Fixed-size circular queue implemented with an array. */
    private static final class CircularQueue {
        private final Booking[] queue = new Booking[CAPACITY];
        private int front = -1;
        private int rear = -1;
        private int count = 0;

        boolean isFull() {
            return count == CAPACITY;
        }

        boolean isEmpty() {
            return count == 0;
        }

        int size() {
            return count;
        }

        boolean enqueue(Booking booking) {
            if (isFull()) {
                return false;
            }
            if (isEmpty()) {
                front = 0;
            }
            rear = (rear + 1) % CAPACITY;
            queue[rear] = booking;
            count++;
            return true;
        }

        Booking dequeue() {
            if (isEmpty()) {
                return null;
            }
            Booking removed = queue[front];
            queue[front] = null;
            count--;

            if (count == 0) {
                front = -1;
                rear = -1;
            } else {
                front = (front + 1) % CAPACITY;
            }
            return removed;
        }

        Booking getAt(int position) {
            if (position < 0 || position >= count) {
                return null;
            }
            return queue[(front + position) % CAPACITY];
        }
    }

    private final CircularQueue queue = new CircularQueue();
    // Contains current, active bookings. Vaccinated bookings are removed.
    private final Map<String, Booking> bookingMap = new HashMap<>();
    private int nextBookingNumber = 101;

    public static void main(String[] args) {
        Main app = new Main();
        app.loadBookings();

        try (Scanner scanner = new Scanner(System.in)) {
            app.runMenu(scanner);
        }

        app.saveBookings();
        System.out.println("Thank you. All current bookings have been saved.");
    }

    private void runMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            System.out.println("\n========== VACCINATION SLOT BOOKING ==========");
            System.out.println("1. Book a slot");
            System.out.println("2. Vaccinate next person");
            System.out.println("3. View queue");
            System.out.println("4. Search booking by ID");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    bookSlot(scanner);
                    break;
                case "2":
                    vaccinateNext();
                    break;
                case "3":
                    viewQueue();
                    break;
                case "4":
                    searchBooking(scanner);
                    break;
                case "5":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid menu choice. Please enter 1 to 5.");
            }
        }
    }

    private void bookSlot(Scanner scanner) {
        if (queue.isFull()) {
            System.out.println("Slots full. Please try the next session.");
            return;
        }

        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Name cannot be empty. Booking not done.");
            return;
        }
        // Keep the text-file format safe and easy to read.
        name = name.replace('|', ' ').replace('\n', ' ').trim();

        System.out.print("Enter age: ");
        String ageInput = scanner.nextLine().trim();
        int age;

        try {
            age = Integer.parseInt(ageInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid age. Booking not done.");
            return;
        }

        if (age < 18) {
            System.out.println("Invalid age. Booking not done.");
            return;
        }

        String bookingId = "V" + nextBookingNumber;
        Booking booking = new Booking(bookingId, name, age);

        if (queue.enqueue(booking)) {
            bookingMap.put(bookingId, booking);
            nextBookingNumber++;
            System.out.println("Slot booked. ID: " + bookingId + " | " + name
                    + " | Position: " + queue.size());
        } else {
            // Defensive check; the full-queue case is handled above.
            System.out.println("Slots full. Please try the next session.");
        }
    }

    private void vaccinateNext() {
        Booking booking = queue.dequeue();

        if (booking == null) {
            System.out.println("No bookings available.");
            return;
        }

        bookingMap.remove(booking.id);
        System.out.println(booking.name + " vaccinated. Slot freed.");
    }

    private void viewQueue() {
        if (queue.isEmpty()) {
            System.out.println("No bookings in the queue.");
            return;
        }

        System.out.println("\nCurrent queue (FIFO order):");
        for (int i = 0; i < queue.size(); i++) {
            Booking booking = queue.getAt(i);
            System.out.println((i + 1) + ". " + booking.id + " | "
                    + booking.name + " | Age: " + booking.age);
        }
    }

    private void searchBooking(Scanner scanner) {
        System.out.print("Enter booking ID (e.g., V101): ");
        String id = scanner.nextLine().trim().toUpperCase();
        Booking booking = bookingMap.get(id);

        if (booking == null) {
            System.out.println("Booking ID not found among current bookings.");
        } else {
            System.out.println("Booking found: " + booking);
        }
    }

    /** Load active bookings and the next ID from bookings.txt at startup. */
    private void loadBookings() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("No previous booking file found. Starting a new session.");
            return;
        }

        int loaded = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\\\|", -1);

                if (parts.length == 2 && parts[0].equals("NEXT_ID")) {
                    try {
                        nextBookingNumber = Math.max(101, Integer.parseInt(parts[1]));
                    } catch (NumberFormatException ignored) {
                        System.out.println("Warning: Invalid next ID in file; using generated ID.");
                    }
                } else if (parts.length == 4 && parts[0].equals("BOOKING")) {
                    try {
                        String id = parts[1].trim();
                        String name = parts[2].trim();
                        int age = Integer.parseInt(parts[3].trim());

                        if (!id.matches("V\\\\d+") || name.isEmpty() || age < 18
                                || queue.isFull() || bookingMap.containsKey(id)) {
                            continue;
                        }

                        Booking booking = new Booking(id, name, age);
                        queue.enqueue(booking);
                        bookingMap.put(id, booking);
                        loaded++;

                        int numericId = Integer.parseInt(id.substring(1));
                        nextBookingNumber = Math.max(nextBookingNumber, numericId + 1);
                    } catch (NumberFormatException ignored) {
                        // Skip malformed booking records and continue loading.
                    }
                }
            }
            System.out.println(loaded + " active booking(s) loaded from " + FILE_NAME + ".");
        } catch (IOException e) {
            System.out.println("Could not load bookings: " + e.getMessage());
        }
    }

    /** Save current active bookings and the next ID when the application exits. */
    private void saveBookings() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            writer.println("NEXT_ID|" + nextBookingNumber);
            for (int i = 0; i < queue.size(); i++) {
                Booking booking = queue.getAt(i);
                writer.println("BOOKING|" + booking.id + "|" + booking.name
                        + "|" + booking.age);
            }
            System.out.println("Bookings saved to " + FILE_NAME + ".");
        } catch (IOException e) {
            System.out.println("Could not save bookings: " + e.getMessage());
        }
    }
}

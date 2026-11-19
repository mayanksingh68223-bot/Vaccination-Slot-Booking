# Vaccination Slot Booking System

## Project details
- Language: Java
- Type: Console-based application
- Data structures: Array-based Circular Queue and HashMap
- File: `bookings.txt`
- Queue capacity: 3 active bookings
- Minimum age: 18 years

## Requirements
Install JDK 8 or later. Open a terminal in this folder.

## Compile and run
```bash
javac Main.java
java Main
```

The application creates `bookings.txt` in the current working directory when it saves on exit. Keep this file in the same folder when you want to restore saved bookings.

## Menu
1. Book a slot
2. Vaccinate next person
3. View queue
4. Search booking by ID
5. Exit

## Data structure explanation
- The circular queue stores up to three active bookings. `front`, `rear`, and `count` track the queue.
- Enqueue inserts at the rear; dequeue removes from the front, preserving FIFO order.
- The HashMap maps each active booking ID to its Booking object for quick lookup.
- After a person is vaccinated, their active booking is removed from the HashMap and the queue.
- The file stores the next booking number and the active queue records, so the queue order and next ID are restored at startup.

## File format
```text
NEXT_ID|103
BOOKING|V101|Riya|25
BOOKING|V102|Dev|30
```

## Important
The program saves current active bookings on normal exit. If the program is forcibly closed before exit, the latest changes may not be saved.

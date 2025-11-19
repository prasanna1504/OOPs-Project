package com.smartpark.main;

import java.io.Console;
import java.util.Scanner;

import com.smartpark.model.*;
import com.smartpark.service.*;
import com.smartpark.exceptions.SlotNotAvailableException;

/*
 * Main entry point for the Smart Parking System application.
 * Handles the Command Line Interface (CLI), user input, and high-level flow control.
 */
public class Main {

    public static void main(String[] args) {

        // Scanner for standard input (fallback if Console is unavailable)
        Scanner inputScanner = new Scanner(System.in);

        // Initialize services
        UserService userService = new UserService();
        ParkingSystem parkingSystem = new ParkingSystem();

        // Pre-populate the parking lot with some slots for demonstration
        parkingSystem.addSlot(Constants.SLOT_COMPACT);
        parkingSystem.addSlot(Constants.SLOT_REGULAR);
        parkingSystem.addSlot(Constants.SLOT_LARGE);
        parkingSystem.addSlot(Constants.SLOT_HANDICAPPED);

        // Ensure a default admin exists (username: admin, password: admin)
        userService.ensureDefaultAdmin();
        
        // Session variable to track the currently logged-in user
        User loggedInUser = null;

        while (true) {
            // Display the main menu
            System.out.println("\nSMART PARKING SYSTEM");
            
            // Show current user status
            if (loggedInUser != null) {
                System.out.println("Logged in as: " + loggedInUser.getUsername() + " [" + loggedInUser.getRole() + "]");
            } else {
                System.out.println("Status: Guest");
            }
            System.out.println("1. Register (New User)");
            System.out.println("2. Login");
            System.out.println("3. Reserve Slot (Users Only)");
            System.out.println("4. Mark Entry (Attendant/Admin)");
            System.out.println("5. Mark Exit (Attendant/Admin)");
            System.out.println("6. Show Slots (Staff Only)");
            System.out.println("7. Show Parking Fees");
            System.out.println("8. Save Bookings (Admin Only)");
            System.out.println("9. Load Bookings (Admin Only)");
            System.out.println("10. Register Staff (Admin Only)");
            System.out.println("11. View My History (Users Only)");
            System.out.println("12. Logout");
            System.out.println("13. Exit Application");

            int choice = readInt(inputScanner, "Enter choice: ");

            // OPTION 1: REGISTER
            if (choice == 1) {
                System.out.print("Enter new username: ");
                String newUsername = inputScanner.nextLine();
                
                // Securely read password
                String newPassword = readPasswordSecurely(inputScanner, "Enter new password: ");

                User createdUser = userService.register(newUsername, newPassword);
                if (createdUser != null)
                    System.out.println("Success: User registered with ID " + createdUser.getUserId());
                else
                    System.out.println("Error: Username already taken or invalid.");

            // OPTION 2: LOGIN
            } else if (choice == 2) {
                System.out.print("Username: ");
                String loginUsername = inputScanner.nextLine();
                
                // Securely read password
                String loginPassword = readPasswordSecurely(inputScanner, "Password: ");

                User result = userService.login(loginUsername, loginPassword);
                if (result != null) {
                    loggedInUser = result;
                    System.out.println("Login successful! Welcome, " + loggedInUser.getUsername());
                } else {
                    System.out.println("Login failed. Invalid credentials.");
                }

            // OPTION 3: RESERVE SLOT (User Only)
            } else if (choice == 3) {
                if (loggedInUser == null || !Constants.ROLE_USER.equals(loggedInUser.getRole())) {
                    System.out.println("Access Denied: Only logged-in Users can reserve slots.");
                    continue;
                }

                // EXPIRE OLD BOOKINGS: Trigger cleanup before checking availability
                parkingSystem.processExpirations();

                int targetSlotId = readInt(inputScanner, "Enter slot ID to reserve: ");

                try {
                    Booking newBooking = parkingSystem.reserveSlot(targetSlotId, loggedInUser);
                    System.out.println("Success: Booking created with ID: " + newBooking.getBookingId());
                    System.out.println("NOTE: You have 1 minute to arrive before this booking expires.");
                } catch (SlotNotAvailableException e) {
                    System.out.println("Reservation failed: " + e.getMessage());
                }

            // OPTION 4: MARK ENTRY (Attendant/Admin Only)
            } else if (choice == 4) {
                if (loggedInUser == null || 
                   (!Constants.ROLE_ATTENDANT.equals(loggedInUser.getRole()) && !Constants.ROLE_ADMIN.equals(loggedInUser.getRole()))) {
                    System.out.println("Access Denied: Only Attendants or Admins can mark entries.");
                    continue;
                }

                int bookingId = readInt(inputScanner, "Enter booking ID to mark entry: ");

                Booking booking = parkingSystem.findBookingById(bookingId);
                if (booking == null) {
                    System.out.println("Error: Booking not found.");
                } else {
                    // Mark entry (timestamps handled inside Attendant class)
                    parkingSystem.getAttendant().markEntry(booking);
                    System.out.println("Entry recorded. Gate opened.");
                }

            // OPTION 5: MARK EXIT (Attendant/Admin Only)
            } else if (choice == 5) {
                if (loggedInUser == null || 
                   (!Constants.ROLE_ATTENDANT.equals(loggedInUser.getRole()) && !Constants.ROLE_ADMIN.equals(loggedInUser.getRole()))) {
                    System.out.println("Access Denied: Only Attendants or Admins can mark exits.");
                    continue;
                }

                int bookingId = readInt(inputScanner, "Enter booking ID to mark exit: ");

                Booking booking = parkingSystem.findBookingById(bookingId);
                if (booking == null) {
                    System.out.println("Error: Booking not found.");
                } else {
                    // Mark exit (Duration calculation and Billing happens here)
                    parkingSystem.getAttendant().markExit(booking);
                    System.out.println("Exit recorded. Duration calculated.");
                    System.out.printf("TOTAL AMOUNT DUE: $%.2f%n", booking.getAmount());
                }

            // OPTION 6: SHOW SLOTS (Staff Only)
            } else if (choice == 6) {
                 // Check: Regular users shouldn't see global slot status (privacy/security)
                 if (loggedInUser == null || Constants.ROLE_USER.equals(loggedInUser.getRole())) {
                     System.out.println("Access Denied: Only Staff can view the master slot list.");
                     continue;
                 }

                // EXPIRE OLD BOOKINGS: Ensure the list is up to date
                parkingSystem.processExpirations();

                System.out.println("\nLive Slot Status");
                System.out.printf("%-5s | %-15s | %-10s%n", "ID", "Type", "Status");
                
                ParkingSystem.ParkingSlot[] slots = parkingSystem.getSlotsArray();
                
                for (ParkingSystem.ParkingSlot slot : slots) {
                    if (slot != null) {
                        String status = slot.isOccupied() ? "OCCUPIED" : "FREE";
                        System.out.printf("%-5d | %-15s | %-10s%n", slot.getId(), slot.getType(), status);
                    }
                }

            // OPTION 7: SHOW FEES (Public)
            } else if (choice == 7) {
                System.out.println("\nCurrent Parking Fees (Per Minute)");
                System.out.printf("%-20s : $%6.2f / min%n", "Compact Slot", Constants.RATE_COMPACT_FEE);
                System.out.printf("%-20s : $%6.2f / min%n", "Regular Slot", Constants.RATE_REGULAR_FEE);
                System.out.printf("%-20s : $%6.2f / min%n", "Large Slot", Constants.RATE_LARGE_FEE);
                System.out.printf("%-20s : $%6.2f / min%n", "Handicapped Slot", Constants.RATE_HANDICAPPED_FEE);

            // OPTION 8: SAVE (Admin Only)
            } else if (choice == 8) {
                if (loggedInUser == null || !Constants.ROLE_ADMIN.equals(loggedInUser.getRole())) {
                    System.out.println("Access Denied: Only Administrators can save system data.");
                    continue;
                }
                parkingSystem.saveBookingsToFile(Constants.FILE_BOOKINGS_TEXT);
                System.out.println("System State saved to " + Constants.FILE_BOOKINGS_TEXT);

            // OPTION 9: LOAD (Admin Only)
            } else if (choice == 9) {
                if (loggedInUser == null || !Constants.ROLE_ADMIN.equals(loggedInUser.getRole())) {
                    System.out.println("Access Denied: Only Administrators can load system data.");
                    continue;
                }
                parkingSystem.loadBookingsFromFile(Constants.FILE_BOOKINGS_TEXT);
                System.out.println("System State loaded from " + Constants.FILE_BOOKINGS_TEXT);
                
             //OPTION 10: REGISTER STAFF (Admin Only)
            } else if (choice == 10) {
                    // Only Admins can create new staff/admin accounts
                if (loggedInUser == null || !Constants.ROLE_ADMIN.equals(loggedInUser.getRole())) {
                    System.out.println("Access Denied: Only Administrators can register staff.");
                    continue;
                }

                System.out.print("Enter new staff username: ");
                String staffUsername = inputScanner.nextLine();
                    
                String staffPassword = readPasswordSecurely(inputScanner, "Enter password for staff: ");
                    
                String selectedRole;
                selectedRole = Constants.ROLE_ATTENDANT;

                // Call the new overloaded register method
                User createdStaff = userService.register(staffUsername, staffPassword, selectedRole);
                
                if (createdStaff != null) {
                    System.out.println("Success: Staff user registered with role: " + createdStaff.getRole());
                } else {
                    System.out.println("Error: Username already taken or invalid.");
                }

            // OPTION 11: VIEW HISTORY (User Only)
            } else if (choice == 11) {
                 if (loggedInUser == null || !Constants.ROLE_USER.equals(loggedInUser.getRole())) {
                    System.out.println("Access Denied: Only Users can view their personal history.");
                    continue;
                 }
                 
                 System.out.println("\nMy Booking History");
                 int[] myBookingIds = loggedInUser.getBookingIds();
                 boolean found = false;
                 if (myBookingIds != null) {
                     for (int id : myBookingIds) {
                         Booking b = parkingSystem.findBookingById(id);
                         if (b != null) {
                             System.out.println(b.toString()); // Prints booking details
                             found = true;
                         }
                     }
                 }
                 if (!found) {
                     System.out.println("No bookings found.");
                 }

            // OPTION 12: LOGOUT
            } else if (choice == 12) {
                if (loggedInUser != null) {
                    System.out.println("User " + loggedInUser.getUsername() + " successfully logged out.");
                    loggedInUser = null; // Resets the session
                } else {
                    System.out.println("No user is currently logged in.");
                }

            // OPTION 13: EXIT
            } else if (choice == 13) {
                System.out.println("Shutting down system. Goodbye!");
                break;

            } else {
                System.out.println("Unknown choice. Please select a number from the menu.");
            }
        }

        inputScanner.close();
    }

    /*
     * Helper method to read integers safely from the scanner.
     * Prevents the application from crashing if non-numeric input is entered.
     */
    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    /*
     * Helper method to read passwords securely.
     * Uses System.console() to mask characters if available (Command Prompt).
     * Falls back to standard Scanner input if running in an IDE (where Console is null).
     */
    private static String readPasswordSecurely(Scanner scanner, String prompt) {
        System.out.print(prompt);
        
        // Attempt to get the system console
        Console systemConsole = System.console();
        
        if (systemConsole != null) {
            char[] passwordArray = systemConsole.readPassword();
            if (passwordArray != null) {
                return new String(passwordArray);
            }
            return "";
        } else {
            return scanner.nextLine();
        }
    }
}
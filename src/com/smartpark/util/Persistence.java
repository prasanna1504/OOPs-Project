package com.smartpark.util;

import com.smartpark.model.Booking;

import java.io.*;

/*
 * Utility class handling the saving and loading of Booking data to/from a text file.
 * This class ensures system state is persistent across application runs.
 */
public final class Persistence {

    private Persistence() { }

    private static int lastLoadedBookingCount = 0;

    /*
     * Saves the current array of Booking objects to the specified filename.
     */
    public static void saveBookings(Booking[] bookings, int bookingCount, String filename) throws IOException {
        File file = new File(filename);

        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
        	
            String fmt = "%-10s, %-20s, %-10s, %-15s, %-10s, %-15s, %-15s, %-15s%n";

            // Print the headers
            pw.printf(fmt, "bookingId", "username", "slotId", "status", "amount", 
                             "creationTime", "entryTime", "exitTime");

            for (int i = 0; i < bookingCount && i < bookings.length; i++) {
                Booking b = bookings[i];
                if (b == null) continue;

                String idStr = (b.getBookingId() == null) ? "" : String.valueOf(b.getBookingId());
                String user = (b.getUsername() == null) ? "" : b.getUsername();
                String slot = String.valueOf(b.getSlotId());
                String status = (b.getStatus() == null) ? "" : b.getStatus();
                String amount = (b.getAmount() == null) ? "" : String.valueOf(b.getAmount());

                // Capture the new timestamp values (longs converted to String)
                String creationTimeStr = String.valueOf(b.getCreationTime());
                String entryTimeStr = String.valueOf(b.getEntryTime());
                String exitTimeStr = String.valueOf(b.getExitTime());

                // Print all 8 fields
                pw.printf(fmt, idStr, user, slot, status, amount, creationTimeStr, entryTimeStr, exitTimeStr);
            }
        }
    }

    /*
     * Loads Booking objects from the specified filename.
     */
    public static Booking[] loadBookings(int capacity, String filename) throws IOException {
        Booking[] result = new Booking[capacity];
        int filled = 0;

        File file = new File(filename);

        if (!file.exists()) {
            file.createNewFile();
            lastLoadedBookingCount = 0;
            return result;
        }

        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    // Skip header row regardless of how many fields it contains
                    if (line.startsWith("bookingId")) {
                        continue;
                    }
                }

                line = line.trim();
                if (line.length() == 0) continue;

                // Split by comma
                String[] t = line.split(",", -1);
                // Check for minimum required fields (now 8 total)
                if (t.length < 8) continue; 

                // 1. Load Original Fields
                Integer id = null;
                if (t[0].trim().length() > 0) {
                    try { id = Integer.parseInt(t[0].trim()); }
                    catch (NumberFormatException e) { continue; }
                }

                String username = t[1].trim();

                int slotId;
                try { slotId = Integer.parseInt(t[2].trim()); }
                catch (NumberFormatException e) { continue; }

                String status = t[3].trim();

                Double amount = null;
                if (t[4].trim().length() > 0) {
                    try { amount = Double.parseDouble(t[4].trim()); }
                    catch (NumberFormatException e) { amount = null; }
                }
                
                // 2. Load New Timestamp Fields
                long creationTime = 0;
                long entryTime = 0;
                long exitTime = 0;
                
                // Field t[5]: creationTime
                if (t[5].trim().length() > 0) {
                    try { creationTime = Long.parseLong(t[5].trim()); }
                    catch (NumberFormatException e) { creationTime = 0; }
                }
                // Field t[6]: entryTime
                if (t[6].trim().length() > 0) {
                    try { entryTime = Long.parseLong(t[6].trim()); }
                    catch (NumberFormatException e) { entryTime = 0; }
                }
                // Field t[7]: exitTime
                if (t[7].trim().length() > 0) {
                    try { exitTime = Long.parseLong(t[7].trim()); }
                    catch (NumberFormatException e) { exitTime = 0; }
                }

                // 3. Construct Booking Object and Set Timestamps
                // Note: Using the two-argument constructor for simplicity and then setting fields manually
                Booking b = new Booking(id, username, slotId);
                b.setStatus(status);
                b.setAmount(amount);
                
                // Set the loaded timestamps 
                b.setCreationTime(creationTime);
                b.setEntryTime(entryTime);
                b.setExitTime(exitTime);

                if (filled < capacity) {
                    result[filled] = b;
                    filled++;
                }
            }
        }

        lastLoadedBookingCount = filled;
        return result;
    }

    public static int getLastLoadedBookingCount() {
        return lastLoadedBookingCount;
    }
}
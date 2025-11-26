
-----

# **Smart Parking Management System**

**Course:** ECOM F213 - Object Oriented Programming
**Project:** Q4 - Smart Parking Management System
-----

## **1. Project Overview**

The **Smart Parking Management System** is a comprehensive Java-based application designed to automate the operations of a parking facility. The system manages the entire lifecycle of vehicle parking, from user registration and slot reservation to real-time vehicle entry/exit tracking and automated billing calculation.

The project was built to demonstrate robust software design principles, replacing manual tracking with a type-safe, object-oriented solution. It supports role-based access (Admins, Attendants, and Users), handles multiple vehicle types, and ensures data persistence across sessions using file I/O.

## **2. Object-Oriented Design Analysis**

The system architecture is built upon the four pillars of Object-Oriented Programming:

  * **Encapsulation:** All data models (e.g., `User`, `Booking`, `ParkingSlot`) use private fields to protect internal state. Access is controlled via public getters and setters, ensuring data integrity (e.g., a booking status cannot be changed to "COMPLETED" without passing validation checks).
  * **Abstraction:** We utilized **Interfaces** (`Authenticator`, `Payment`, `EntryExitHandler`) and **Abstract Classes** (`AbstractVehicle`) to define contracts. This decouples the implementation details from the high-level logic.
  * **Inheritance:** The system uses **Hierarchical Inheritance** to model vehicles. `Car` and `Motorcycle` classes inherit shared properties (license plate, owner) from `AbstractVehicle`, reducing code duplication.
  * **Polymorphism:** The system employs **Method Overloading** (compile-time polymorphism) for flexibility in object creation and method calls. Additionally, the `Attendant` inner class demonstrates polymorphism by implementing multiple interfaces, allowing it to be treated as both a payment processor and a gate handler.

-----

## **3. Rubrics of Usage (Requirement Checklist)**

The following table confirms the implementation of the 12 mandatory constraints specified in the project instructions.

| **\#** | **Component Category** | **Implementation Details** | **Count** | **File / Location** |
| :--- | :--- | :--- | :--- | :--- |
| **1** | **Nested Classes** | Used `static class ParkingSlot` (data) and `inner class Attendant` (logic). | 2 | `ParkingSystem.java` |
| **2** | **Abstract Class** | `AbstractVehicle` serves as the base template for all vehicles. | 1 | `AbstractVehicle.java` |
| **3** | **Interface** | Defined `Authenticator`, `Payment`, and `EntryExitHandler`. | 3 | `interfaces/` package |
| **4** | **Hierarchical Inheritance** | `Car` and `Motorcycle` both extend `AbstractVehicle`. | 1 | `model/` package |
| **5** | **Multiple Inheritance** | The `Attendant` class implements `EntryExitHandler` AND `Payment` interfaces. | 1 | `ParkingSystem.java` |
| **6** | **Package** | organized into `com.smartpark.model`, `service`, `interfaces`, `util`, `exceptions`. | 5 | All files |
| **7** | **Exception Handling** | `SlotNotAvailableException` (custom), `IOException`, `NumberFormatException`. | 3 | `exceptions/`, `Main.java` |
| **8** | **I/O Operations** | `Scanner` for CLI input, `FileWriter`/`FileReader` for booking persistence. | 2 | `Main.java`, `Persistence.java` |
| **9** | **Overloaded Methods** | `addSlot(String)` vs `addSlot(String...)`; `register` methods. | \>2 | `ParkingSystem.java`, `UserService.java` |
| **10** | **Overloaded Constructors** | `Booking` and `User` classes have multiple constructors for flexibility. | 2 | `Booking.java`, `User.java` |
| **11** | **Vararg Overloading** | `addSlot(String...)` and `addBookingId(int...)` handle variable inputs. | 2 | `ParkingSystem.java`, `User.java` |
| **12** | **Wrappers** | Extensive use of `Integer` (IDs) and `Double` (Amounts). | Many | `Booking.java`, `User.java` |

-----

## **4. Feature Implementation Snippets**

### **1. Nested Classes**

```java
// In ParkingSystem.java
public class ParkingSystem {
    // Static Nested Class: Memory efficient for storing slot data
    public static class ParkingSlot {
        private int id;
        private String type;
        // ...
    }

    // Inner Class: Accesses outer class state for operations
    public class Attendant implements EntryExitHandler, Payment {
        // ...
    }
}
```

### **2. Abstract Class**

```java
// In AbstractVehicle.java
public abstract class AbstractVehicle {
    protected String licensePlate;
    // Abstract method forces subclasses to define their type
    public abstract String getType();
}
```

### **3. Interface**

```java
// In Authenticator.java
public interface Authenticator {
    User login(String username, String password);
}
```

### **4. Hierarchical Inheritance**

```java
// In Car.java
public class Car extends AbstractVehicle {
    public Car(String plate) { super(plate); }
    @Override
    public String getType() { return "Car"; }
}

// In Motorcycle.java
public class Motorcycle extends AbstractVehicle {
    public Motorcycle(String plate) { super(plate); }
    @Override
    public String getType() { return "Motorcycle"; }
}
```

### **5. Multiple Inheritance (via Interfaces)**

```java
// In ParkingSystem.java
// Implements methods from two distinct interfaces
public class Attendant implements EntryExitHandler, Payment {
    @Override
    public void markEntry(Booking booking) { /* Logic */ }

    @Override
    public boolean pay(Booking booking, Double amount) { /* Logic */ }
}
```

### **7. Exception Handling**

```java
// Custom Checked Exception
public class SlotNotAvailableException extends Exception {
    public SlotNotAvailableException(String message) { super(message); }
}

// Usage in ParkingSystem.java
if (slot.isOccupied()) {
    throw new SlotNotAvailableException("Slot is already occupied.");
}
```

### **8. I/O Operations**

```java
// In Persistence.java (File Handling)
try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
    pw.printf(fmt, idStr, user, slot, status, amount);
} catch (IOException e) {
    System.out.println("Error saving file");
}
```

### **9. Overloaded Methods**

```java
// In ParkingSystem.java
public void addSlot(String type) { ... } // Single
public void addSlot(String... types) { ... } // Multiple (Varargs)
```

### **10. Overloaded Constructors**

```java
// In Booking.java
public Booking(String user, int slot) { ... } // New Booking
public Booking(Integer id, String user, int slot) { ... } // Loading from File
```

### **11. Vararg Overloading**

```java
// In User.java
public void addBookingId(int... multipleBookingIds) {
    for (int id : multipleBookingIds) {
        addBookingId(id);
    }
}
```

### **12. Wrappers**

```java
// Using Integer object instead of primitive int to allow null values
private Integer bookingId; 
private Double amount;
```

-----


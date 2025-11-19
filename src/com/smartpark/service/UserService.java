package com.smartpark.service;

import com.smartpark.interfaces.Authenticator;
import com.smartpark.model.User;
import com.smartpark.model.Constants;

/*
 * Simple user manager implementing Authenticator.
 * Handles user storage, registration, and login validation.
 */
public class UserService implements Authenticator {

    private User[] users; // Simple array to hold users
    private int userCount;

    private Integer nextUserId; // Next user id to assign

    /*
     * Default constructor creates space for a small number of users.
     */
    public UserService() {
        this.users = new User[20]; // can hold up to 20 users initially
        this.userCount = 0;
        this.nextUserId = 1; // start IDs from 1
    }

    /**
     * Overloaded constructor: allow specifying initial capacity.
     */
    public UserService(int initialCapacity) {
        if (initialCapacity <= 0) {
            initialCapacity = 20;
        }
        this.users = new User[initialCapacity];
        this.userCount = 0;
        this.nextUserId = 1;
    }

    /*
     * REGISTRATION METHODS (Overloaded)
     */
    
    /*
     * PRIMARY REGISTRATION METHOD: Creates a user with a specified role.
     * This is used by the Admin to create staff/management accounts.
     * Returns the created User object or null if username already exists.
     */
    public User register(String username, String password, String role) {
        if (username == null || username.trim().length() == 0) {
            return null; // invalid username
        }

        // Check if username already exists
        if (findUserByUsername(username) != null) {
            return null; // already registered
        }

        // Ensure capacity, resize if needed
        if (userCount == users.length) {
            User[] larger = new User[users.length * 2];
            for (int i = 0; i < users.length; i++) {
                larger[i] = users[i];
            }
            users = larger;
        }

        // Create user and assign role based on parameter
        User u = new User(username, password);
        u.setUserId(nextUserId);
        nextUserId = nextUserId + 1;
        
        u.setRole(role); 

        users[userCount] = u;
        userCount++;

        return u;
    }
    
    /*
     * Register with username only. Password defaults to empty string.
     * Delegates to the primary method, setting the default role to USER.
     */
    public User register(String username) {
        return register(username, "", Constants.ROLE_USER);
    }

    /*
     * Overloaded register method: username + password.
     * Delegates to the primary method, setting the default role to USER.
     */
    public User register(String username, String password) {
        return register(username, password, Constants.ROLE_USER);
    }

    /*
     * AUTHENTICATION & LOOKUP
     */

    /*
     * Attempt to log in. Returns the User object on success, null on failure.
     */
    @Override
    public User login(String username, String password) {
        if (username == null) return null;

        User u = findUserByUsername(username);
        if (u == null) return null;

        if (u.checkPassword(password)) {
            return u; // Successful login
        }

        return null;
    }

    /*
     * Find a user by username. Returns null if not found.
     */
    public User findUserByUsername(String username) {
        if (username == null) return null;
        for (int i = 0; i < userCount; i++) {
            User u = users[i];
            if (u != null && username.equals(u.getUsername())) {
                return u;
            }
        }
        return null;
    }

    /*
     * Find user by ID.
     */
    public User findUserById(Integer id) {
        if (id == null) return null;
        for (int i = 0; i < userCount; i++) {
            User u = users[i];
            if (u != null && u.getUserId() != null && u.getUserId().equals(id)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Create a simple admin user if none exists — convenience for testing.
     * Username: admin, password: admin
     */
    public void ensureDefaultAdmin() {
        User existing = findUserByUsername("admin");
        if (existing == null) {
            User admin = register("admin", "admin"); 
            if (admin != null) {
                admin.setRole(Constants.ROLE_ADMIN);
            }
        }
    }
    
    // Getters for array access remain unchanged

    public User[] getUsers() {
        return users;
    }

    public int getUserCount() {
        return userCount;
    }
}
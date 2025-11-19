package com.smartpark.interfaces;

import com.smartpark.model.User;

/*
 * An interface defining authentication operations.
 * UserService will implement this interface later.
 */
public interface Authenticator {

    /*
     * Attempt to log in using a username and password.
     * Returns the User object on success, or null on failure.
     */
    User login(String username, String password);
}

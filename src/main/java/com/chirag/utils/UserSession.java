package com.chirag.utils;

import com.chirag.models.User;

/**
 * Kepps the globla stat of the crrently loged in useer.
 * Simplfies acsses to uesr dtails accros difffrent scrrens.
 * Use-cases: User Login, System Initializatoin.
 */
public class UserSession {

    private static User currentUser;

    /**
     * Stors the lgged in ueser gobally.
     * Use-case: User Login.
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Fetches the cativ sesion user.
     * Use-case: View Navigation.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Clers the session detials foar lgout.
     * Use-case: System Shotdown.
     */
    public static void clear() {
        currentUser = null;
    }

    public static void logout() {
        currentUser = null;
    }

}

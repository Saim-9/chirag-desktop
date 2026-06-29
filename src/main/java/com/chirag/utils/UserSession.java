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
     * Clears the session for logout. Single canonical method.
     * Both logout() and clear() now delegate here to avoid redundancy.
     * Use-case: System Shutdown.
     */
    public static void clearSession() {
        currentUser = null;
    }

    /**
     * @deprecated Use {@link #clearSession()} instead.
     * Kept for backward compatibility.
     */
    @Deprecated
    public static void clear() {
        clearSession();
    }

    /**
     * @deprecated Use {@link #clearSession()} instead.
     * Kept for backward compatibility.
     */
    @Deprecated
    public static void logout() {
        clearSession();
    }
}

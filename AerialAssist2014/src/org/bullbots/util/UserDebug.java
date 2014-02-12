package org.bullbots.util;

/**
 * @author Clay Kuznia & Noah Peterson
 */
public class UserDebug {
    
    /**
     * Prints a debug message for the user to see.
     * @param message the message to display.
     */
    public static void print(String message) {
	System.out.println("[User Prompt] --> " + message);
    }
}

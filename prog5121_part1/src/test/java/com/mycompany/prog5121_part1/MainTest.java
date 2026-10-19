package com.mycompany.prog5121_part1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private static final String REGISTRATION = "Kyle\nSmith\nkyl_1\nCh&&sec@ke99!\n+27838968976\n";
    private static final String LOGIN = "kyl_1\nCh&&sec@ke99!\n";

    private String runConsole(String input) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            Main.main(new String[0]);
            return output.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test void failedLoginCannotAccessQuickChat() {
        String output = runConsole(REGISTRATION + "kyl_1\nWrongPass123!\n");
        assertTrue(output.contains("Username or password incorrect, please try again."));
        assertFalse(output.contains("Welcome to QuickChat."));
    }

    @Test void failedRegistrationCannotAccessQuickChat() {
        String output = runConsole("Kyle\nSmith\ninvalid\nCh&&sec@ke99!\n+27838968976\n");
        assertTrue(output.contains("Cannot proceed to login."));
        assertFalse(output.contains("Welcome to QuickChat."));
    }

    @Test void registrationRetriesPasswordAndPhoneBeforeMovingOn() {
        String output = runConsole("Kyle\nSmith\nkyl_1\npassword\nshort\nCh&&sec@ke99!\n"
                + "08575975889\ninvalid\n+27838968976\n" + LOGIN + "0\n3\n");
        assertEquals(3, output.split("Enter a password", -1).length - 1);
        assertEquals(3, output.split("Enter your South African cell phone number", -1).length - 1);
        assertTrue(output.indexOf("Password successfully captured.")
                < output.indexOf("Enter your South African cell phone number"));
        assertTrue(output.indexOf("Cell phone number successfully added.")
                < output.indexOf("--- REGISTRATION STATUS ---"));
        assertTrue(output.contains("Registration successful."));
        assertTrue(output.contains("Welcome to QuickChat."));
    }

    @Test void menuRepeatsComingSoonThenQuits() {
        String output = runConsole(REGISTRATION + LOGIN + "0\n2\n2\n3\n");
        assertTrue(output.contains("Welcome to QuickChat."));
        assertEquals(2, output.split("Coming Soon\\.", -1).length - 1);
        assertEquals(3, output.split("3\\. Quit", -1).length - 1);
        assertTrue(output.trim().endsWith("Total messages sent: 0"));
    }

    @Test void forLoopLimitsEntriesAndNumbersIncrement() {
        String output = runConsole(REGISTRATION + LOGIN
                + "2\n1\n+27718693002\nHi Mike, can you join us for dinner tonight?\n1\n"
                + "+27718693002\nHi Keegan, did you receive the payment?\n2\n0\n1\n3\n");
        assertTrue(output.contains("Message number: 0"));
        assertTrue(output.contains("Message number: 1"));
        assertFalse(output.contains("Message number: 2"));
        assertEquals(2, output.split("Message ID generated:", -1).length - 1);
        assertTrue(output.contains("Press 0 to delete the message."));
        assertTrue(output.contains("You have entered your chosen number of messages."));
        assertTrue(output.trim().endsWith("Total messages sent: 1"));
        int detailsStart = output.indexOf("Message ID:");
        int hashStart = output.indexOf("Message Hash:", detailsStart);
        int recipientStart = output.indexOf("Recipient:", hashStart);
        int textStart = output.indexOf("Message: Hi Mike", recipientStart);
        assertTrue(detailsStart >= 0 && hashStart > detailsStart
                && recipientStart > hashStart && textStart > recipientStart);
    }

    @Test void invalidInputsRetryWithoutUsingAnotherMessageSlot() {
        String output = runConsole(REGISTRATION + LOGIN
                + "bad\n-1\n1\n9\n1\n08575975889\n+27718693002\n"
                + "a".repeat(251) + "\nHi Mike, can you join us for dinner tonight?\n7\n1\n3\n");
        assertTrue(output.contains("Please enter a number from 0 to 10000."));
        assertTrue(output.contains("Please enter a number from 1 to 3."));
        assertTrue(output.contains("Cell phone number is incorrectly formatted"));
        assertTrue(output.contains("Message exceeds 250 characters by 1; please reduce the size."));
        assertEquals(1, output.split("Message ID generated:", -1).length - 1);
        assertTrue(output.trim().endsWith("Total messages sent: 1"));
    }

    @Test void zeroCountAllowsNoMessageEntries() {
        String output = runConsole(REGISTRATION + LOGIN + "0\n1\n3\n");
        assertFalse(output.contains("Enter recipient"));
        assertTrue(output.trim().endsWith("Total messages sent: 0"));
    }
}

package com.mycompany.prog5121_part1;

import java.util.Scanner;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        //  a Scanner object to read input
        Scanner inputReader = new Scanner(System.in);

        //  Login object to access our validation methods
        Login accountManager = new Login();

        System.out.println(" REGISTRATION ");

        // Collect Personal Details
        System.out.print("Enter your first name: ");
        String fName = inputReader.nextLine();

        System.out.print("Enter your last name: ");
        String lName = inputReader.nextLine();

        // Collect and verify Username
        System.out.print("Enter a username (max 5 chars, must contain '_'): ");
        String uName = inputReader.nextLine();

        // feedback
        if (accountManager.checkUserName(uName)) {
            System.out.println("Username successfully captured.");
        } else {
            System.out.println(
                    "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.");
        }

        // Repeat password entry until all complexity requirements are met.
        String pwd = "";
        for (boolean validPassword = false; !validPassword;) {
            System.out.print("Enter a password (min 8 chars, 1 capital, 1 number, 1 special): ");
            pwd = inputReader.nextLine();
            validPassword = accountManager.checkPasswordComplexity(pwd);

            if (validPassword) {
                System.out.println("Password successfully captured.");
            } else {
                System.out.println(
                        "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.");
            }
        }

        // Only move to registration after a valid cell phone number is entered.
        String contactNum = "";
        for (boolean validCellNumber = false; !validCellNumber;) {
            System.out.print("Enter your South African cell phone number (e.g., +27...): ");
            contactNum = inputReader.nextLine();
            validCellNumber = accountManager.checkCellPhoneNumber(contactNum);

            if (validCellNumber) {
                System.out.println("Cell phone number successfully added.");
            } else {
                System.out.println("Cell phone number incorrectly formatted or does not contain international code.");
            }
        }

        // Registration
        System.out.println("\n--- REGISTRATION STATUS ---");
        // pass all the collected data into our registerUser method
        String regStatus = accountManager.registerUser(uName, pwd, fName, lName, contactNum);
        System.out.println(regStatus);

        // Proceed to Login ONLY if registration was completely successful
        if (regStatus.equals("Registration successful.")) {

            System.out.println("\n--- LOGIN ---");
            System.out.print("Enter your username: ");
            String loginAttemptName = inputReader.nextLine();

            System.out.print("Enter your password: ");
            String loginAttemptPwd = inputReader.nextLine();

            // Verify Login details against the registered data
            boolean isAuthValid = accountManager.loginUser(loginAttemptName, loginAttemptPwd);

            // Display Final Login Status
            System.out.println(accountManager.returnLoginStatus(isAuthValid));
            if (isAuthValid) {
                runQuickChat(inputReader);
            }

        } else {
            // Stop the program gracefully if registration failed
            System.out.println("Cannot proceed to login. Please run the program again and provide valid details.");
        }

        inputReader.close();
    }
    // The while loop keeps the menu open; the for loop limits message entries.
    static void runQuickChat(Scanner inputReader) {
        Message.startSession();
        System.out.println("Welcome to QuickChat.");
        int messageCount = readNumber(inputReader, "How many messages do you want to enter?", 0, 10000);
        boolean running = true;
        boolean messagesEntered = false;
        Message lastMessage = null;
        while (running) {
            System.out.println("1. Send Messages");
            System.out.println("2. Show recently sent messages");
            System.out.println("3. Quit");
            int option = readNumber(inputReader, "Choose an option:", 1, 3);
            if (option == 1) {
                if (messagesEntered) {
                    System.out.println("You have entered your chosen number of messages.");
                    continue;
                }
                for (int messageNumber = 0; messageNumber < messageCount; messageNumber++) {
                    System.out.println("Message number: " + messageNumber);
                    System.out.println("Enter recipient (international format, e.g. +27718693002):");
                    String recipient = inputReader.nextLine();
                    Message currentMessage = new Message(messageNumber, recipient, "");
                    while (!currentMessage.checkRecipientCell().equals("Cell phone number successfully captured.")) {
                        System.out.println(currentMessage.checkRecipientCell());
                        recipient = inputReader.nextLine();
                        currentMessage.setRecipient(recipient);
                    }
                    System.out.println(currentMessage.checkRecipientCell());
                    System.out.println("Enter message (maximum 250 characters):");
                    String messageText = inputReader.nextLine();
                    currentMessage.setMessage(messageText);
                    while (!currentMessage.checkMessageLength().equals("Message ready to send.")) {
                        System.out.println(currentMessage.checkMessageLength());
                        messageText = inputReader.nextLine();
                        currentMessage.setMessage(messageText);
                    }
                    System.out.println(currentMessage.checkMessageLength());
                    System.out.println("Message ID generated: " + currentMessage.getMessageId());
                    boolean processed = false;
                    while (!processed) {
                        int action = readNumber(inputReader,
                                "1. Send Message\n2. Disregard Message\n3. Store Message", 1, 3);
                        currentMessage.setAction(action);
                        try {
                            System.out.println(currentMessage.SentMessage());
                            if (action == 1) {
                                System.out.println(currentMessage.getMessageDetails());
                            } else if (action == 2) {
                                readNumber(inputReader, "Confirm deletion:", 0, 0);
                            }
                            processed = true;
                        } catch (IOException exception) {
                            System.out.println("Message could not be stored: " + exception.getMessage());
                            System.out.println("Please choose an action again.");
                        }
                    }
                    lastMessage = currentMessage;
                }
                messagesEntered = true;
                System.out.println("Total messages sent: "
                        + (lastMessage == null ? 0 : lastMessage.returnTotalMessagess()));
            } else if (option == 2) {
                System.out.println("Coming Soon.");
            } else {
                running = false;
            }
        }
        System.out.println("Total messages sent: "
                + (lastMessage == null ? 0 : lastMessage.returnTotalMessagess()));
    }

    private static int readNumber(Scanner inputReader, String prompt, int minimum, int maximum) {
        while (true) {
            System.out.println(prompt);
            try {
                int number = Integer.parseInt(inputReader.nextLine().trim());
                if (number >= minimum && number <= maximum) {
                    return number;
                }
            } catch (NumberFormatException exception) {
                // Invalid numeric input is retried rather than crashing the menu.
            }
            System.out.println("Please enter a number from " + minimum + " to " + maximum + ".");
        }
    }
}

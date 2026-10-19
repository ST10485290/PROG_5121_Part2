package com.mycompany.prog5121_part1;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Random;

public class Message {
    private String messageId;
    private int messageNumber;
    private String recipient;
    private String message;
    private String messageHash;
    private int action;
    private String status = "Pending";
    private Path storageDirectory;

    private static int totalMessagesSent;
    private static String sentMessages = "";
    private static String usedIds = "|";
    private static final Random RANDOM = new Random();

    public Message(int messageNumber, String recipient, String message) {
        this(generateMessageId(messageNumber), messageNumber, recipient, message, Path.of("stored-messages"));
    }

    Message(String messageId, int messageNumber, String recipient, String message, Path directory) {
        this.messageId = messageId;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.message = message;
        this.storageDirectory = directory;
        this.messageHash = createMessageHash();
    }

    private static String generateMessageId(int messageNumber) {
        if (messageNumber < 0 || messageNumber > 99999) {
            throw new IllegalArgumentException("Message number must be between 0 and 99999.");
        }
        String numberText = "00000" + messageNumber;
        String counterDigits = numberText.substring(numberText.length() - 5);
        String candidate;
        do {
            candidate = "";
            for (int digit = 0; digit < 5; digit++) {
                candidate += RANDOM.nextInt(10);
            }
            candidate += counterDigits;
        } while (usedIds.contains("|" + candidate + "|"));
        usedIds += candidate + "|";
        return candidate;
    }

    public boolean checkMessageID() {
        return messageId != null && messageId.matches("[0-9]{10}");
    }

    public String checkRecipientCell() {
        // Reuse the supplied Login validator. The brief's valid +27 example has
        // nine national digits, so interpret its ten-character limit after +27.
        if (recipient != null && new Login().checkCellPhoneNumber(recipient)
                && recipient.startsWith("+27") && recipient.substring(3).length() <= 10) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    public String checkMessageLength() {
        if (message == null || message.trim().isEmpty()) {
            return "Please enter a message.";
        }
        if (message.length() > 250) {
            return "Message exceeds 250 characters by " + (message.length() - 250)
                    + "; please reduce the size.";
        }
        return "Message ready to send.";
    }

    public String createMessageHash() {
        if (!checkMessageID() || message == null || message.trim().isEmpty()) {
            return "";
        }
        String[] words = message.trim().split("\\s+");
        String firstWord = words[0].replaceAll("^\\p{Punct}+|\\p{Punct}+$", "");
        String lastWord = words[words.length - 1].replaceAll("^\\p{Punct}+|\\p{Punct}+$", "");
        return (messageId.substring(0, 2) + ":" + messageNumber + ":"
                + firstWord + lastWord).toUpperCase(Locale.ROOT);
    }

    public void setRecipient(String recipient) {
        if (status.equals("Pending")) {
            this.recipient = recipient;
        }
    }

    public void setMessage(String message) {
        if (status.equals("Pending")) {
            this.message = message;
            this.messageHash = createMessageHash();
        }
    }

    public void setAction(int action) {
        this.action = action;
    }

    // Main collects the numeric choice and sets it before calling this required method.
    public String SentMessage() throws IOException {
        if (!status.equals("Pending")) {
            return "Message already processed.";
        }
        if (action == 2) {
            status = "Disregarded";
            return "Press 0 to delete the message.";
        }
        if (action != 1 && action != 3) {
            return "Please choose 1, 2 or 3.";
        }
        if (!checkMessageID()) {
            return "Invalid Message ID.";
        }
        if (!checkRecipientCell().equals("Cell phone number successfully captured.")) {
            return checkRecipientCell();
        }
        if (!checkMessageLength().equals("Message ready to send.")) {
            return checkMessageLength();
        }
        if (action == 3) {
            storeMessage();
            return "Message successfully stored.";
        }
        status = "Sent";
        totalMessagesSent++;
        sentMessages += getMessageDetails() + "\n\n";
        return "Message successfully sent.";
    }

    public String getMessageDetails() {
        return "Message ID: " + messageId + "\nMessage Hash: " + messageHash
                + "\nRecipient: " + recipient + "\nMessage: " + message;
    }

    public String printMessages() {
        return sentMessages;
    }

    public int returnTotalMessagess() {
        return totalMessagesSent;
    }

    public void storeMessage() throws IOException {
        if (!status.equals("Pending") || !checkMessageID()
                || !checkRecipientCell().equals("Cell phone number successfully captured.")
                || !checkMessageLength().equals("Message ready to send.")) {
            throw new IOException("Only a valid, pending message can be stored.");
        }
        // JSON research: Google, Gson User Guide, 'Object Examples' and
        // 'Compact Vs. Pretty Printing for JSON Output Format', accessed 18 Oct 2026.
        // https://google.github.io/gson/UserGuide.html
        // Gson handles quotes, newlines and backslashes instead of manual escaping.
        JsonObject data = new JsonObject();
        data.addProperty("messageId", messageId);
        data.addProperty("messageNumber", messageNumber);
        data.addProperty("recipient", recipient);
        data.addProperty("message", message);
        data.addProperty("messageHash", messageHash);
        data.addProperty("status", "Stored");
        Files.createDirectories(storageDirectory);
        Path file = storageDirectory.resolve(messageId + ".json");
        Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(data),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        // Only report success after the file has been written. Never overwrite an existing ID.
        status = "Stored";
    }

    public String getMessageId() {
        return messageId;
    }

    public static void startSession() {
        totalMessagesSent = 0;
        sentMessages = "";
        usedIds = "|";
    }
}

package com.mycompany.prog5121_part1;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
    private static final String FIRST = "Hi Mike, can you join us for dinner tonight?";
    private static final String SECOND = "Hi Keegan, did you receive the payment?";
    @TempDir Path directory;

    @BeforeEach
    void resetSession() {
        Message.startSession();
    }

    private Message firstMessage() {
        return new Message("0012345678", 0, "+27718693002", FIRST, directory);
    }

    @Test void validMessageIsReady() {
        assertEquals("Message ready to send.", firstMessage().checkMessageLength());
    }

    @Test void exactly250CharactersAccepted() {
        Message message = firstMessage();
        message.setMessage("a".repeat(250));
        assertEquals("Message ready to send.", message.checkMessageLength());
    }

    @Test void excessCharactersCalculated() {
        Message message = firstMessage();
        message.setMessage("a".repeat(257));
        assertEquals("Message exceeds 250 characters by 7; please reduce the size.", message.checkMessageLength());
    }

    @Test void emptyMessageRejected() {
        Message message = firstMessage();
        message.setMessage("   ");
        assertEquals("Please enter a message.", message.checkMessageLength());
        assertEquals("", message.createMessageHash());
    }

    @Test void requiredRecipientAccepted() {
        assertEquals("Cell phone number successfully captured.", firstMessage().checkRecipientCell());
    }

    @Test void requiredInvalidRecipientRejected() {
        Message message = new Message(1, "08575975889", SECOND);
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.", message.checkRecipientCell());
    }

    @Test void tooManyNationalDigitsRejected() {
        Message message = new Message(0, "+2712345678901", FIRST);
        assertFalse(message.checkRecipientCell().equals("Cell phone number successfully captured."));
    }

    @Test void suppliedHashMatchesActualLogic() {
        assertEquals("00:0:HITONIGHT", firstMessage().createMessageHash());
    }

    @Test void hashesAndNumbersCheckedInLoop() {
        String[] texts = {FIRST, SECOND, "  hello   world!  ", "Hello"};
        String[] endings = {"HITONIGHT", "HIPAYMENT", "HELLOWORLD", "HELLOHELLO"};
        for (int number = 0; number < texts.length; number++) {
            Message message = new Message("1234567890", number, "+27718693002", texts[number], directory);
            assertEquals("12:" + number + ":" + endings[number], message.createMessageHash());
        }
    }

    @Test void generatedIdsHaveTenDigitsAndUseCounter() {
        String previousIds = "|";
        for (int number = 0; number < 100; number++) {
            Message message = new Message(number, "+27718693002", FIRST);
            String id = message.getMessageId();
            assertNotNull(id);
            assertEquals(10, id.length());
            assertTrue(message.checkMessageID());
            assertFalse(previousIds.contains("|" + id + "|"));
            assertEquals(number, Integer.parseInt(id.substring(5)));
            previousIds += id + "|";
        }
    }

    @Test void invalidIdsRejected() {
        String[] ids = {"", "12345678901", "abcdefghij", "123", null};
        for (String id : ids) {
            assertFalse(new Message(id, 0, "+27718693002", FIRST, directory).checkMessageID());
        }
    }

    @Test void sendResponseAndTotal() throws IOException {
        Message message = firstMessage();
        message.setAction(1);
        assertEquals("Message successfully sent.", message.SentMessage());
        assertEquals(1, message.returnTotalMessagess());
        assertEquals("Message already processed.", message.SentMessage());
        assertEquals(1, message.returnTotalMessagess());
    }

    @Test void suppliedDiscardCaseDoesNotCountAsSent() throws IOException {
        Message message = new Message(1, "08575975889", SECOND);
        message.setAction(2);
        assertEquals("Press 0 to delete the message.", message.SentMessage());
        assertEquals(0, message.returnTotalMessagess());
        assertEquals("", message.printMessages());
    }

    @Test void storeResponseAndJsonContents() throws IOException {
        Message message = firstMessage();
        message.setAction(3);
        assertEquals("Message successfully stored.", message.SentMessage());
        JsonObject json = JsonParser.parseString(Files.readString(directory.resolve("0012345678.json"))).getAsJsonObject();
        assertEquals("0012345678", json.get("messageId").getAsString());
        assertEquals(0, json.get("messageNumber").getAsInt());
        assertEquals("+27718693002", json.get("recipient").getAsString());
        assertEquals(FIRST, json.get("message").getAsString());
        assertEquals("00:0:HITONIGHT", json.get("messageHash").getAsString());
        assertEquals("Stored", json.get("status").getAsString());
        assertEquals(0, message.returnTotalMessagess());
    }

    @Test void multipleStoredMessagesAndEscapingPreserved() throws IOException {
        firstMessage().storeMessage();
        String text = "Hi \"Mike\"\\home\nSee you tonight!";
        Message second = new Message("1234567890", 1, "+27718693002", text, directory);
        second.storeMessage();
        assertTrue(Files.exists(directory.resolve("0012345678.json")));
        JsonObject json = JsonParser.parseString(Files.readString(directory.resolve("1234567890.json"))).getAsJsonObject();
        assertEquals(text, json.get("message").getAsString());
    }

    @Test void failedStorageDoesNotClaimSuccessAndCanRetryAction() throws IOException {
        Path blockedDirectory = directory.resolve("file");
        Files.writeString(blockedDirectory, "existing contents");
        Message message = new Message("0012345678", 0, "+27718693002", FIRST, blockedDirectory);
        message.setAction(3);
        try {
            message.SentMessage();
            fail("A file cannot be used as a storage directory.");
        } catch (IOException expected) {
            assertEquals(0, message.returnTotalMessagess());
        }
        message.setAction(1);
        assertEquals("Message successfully sent.", message.SentMessage());
    }

    @Test void existingJsonNeverOverwritten() throws IOException {
        firstMessage().storeMessage();
        Path file = directory.resolve("0012345678.json");
        String original = Files.readString(file);
        try {
            firstMessage().storeMessage();
            fail("Duplicate file must not overwrite the original.");
        } catch (IOException expected) {
            assertEquals(original, Files.readString(file));
        }
    }

    @Test void sentHistoryHasAllSentMessagesInRequiredOrder() throws IOException {
        Message first = firstMessage();
        first.setAction(1);
        first.SentMessage();
        Message second = new Message("1234567890", 1, "+27718693002", SECOND, directory);
        second.setAction(1);
        second.SentMessage();
        assertEquals("Message ID: 0012345678\nMessage Hash: 00:0:HITONIGHT\nRecipient: +27718693002\nMessage: "
                + FIRST + "\n\nMessage ID: 1234567890\nMessage Hash: 12:1:HIPAYMENT\nRecipient: +27718693002\nMessage: "
                + SECOND + "\n\n", second.printMessages());
        assertEquals(2, second.returnTotalMessagess());
    }

    @Test void mixedActionsCountOnlySent() throws IOException {
        Message first = firstMessage();
        first.setAction(1);
        first.SentMessage();
        Message discarded = new Message(1, "08575975889", SECOND);
        discarded.setAction(2);
        discarded.SentMessage();
        Message stored = new Message("1234567890", 2, "+27718693002", SECOND, directory);
        stored.setAction(3);
        stored.SentMessage();
        assertEquals(1, stored.returnTotalMessagess());
        assertFalse(stored.printMessages().contains(SECOND));
    }

    @Test void invalidRecipientCannotBeSent() throws IOException {
        Message message = new Message(1, "08575975889", SECOND);
        message.setAction(1);
        assertEquals(message.checkRecipientCell(), message.SentMessage());
        assertEquals(0, message.returnTotalMessagess());
    }

    @Test void invalidLengthCannotBeSent() throws IOException {
        Message message = firstMessage();
        message.setMessage("a".repeat(251));
        message.setAction(1);
        assertEquals(message.checkMessageLength(), message.SentMessage());
        assertEquals(0, message.returnTotalMessagess());
    }
}

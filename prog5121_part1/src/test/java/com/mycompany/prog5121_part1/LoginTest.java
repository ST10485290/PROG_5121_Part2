package com.mycompany.prog5121_part1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {

    Login testLoginSystem = new Login();

    // USERNAME TESTS

    @Test
    public void verifyValidUserName() {
        // Test Data: "kyl_1"
        // Expected Result: True
        boolean actualResult = testLoginSystem.checkUserName("kyl_1");
        assertTrue(actualResult, "Username 'kyl_1' should be valid.");
    }

    @Test
    public void verifyInvalidUserName() {
        // Test Data: "kyle!!!!!!!"
        // Expected Result: False
        boolean actualResult = testLoginSystem.checkUserName("kyle!!!!!!!");
        assertFalse(actualResult, "Username 'kyle!!!!!!!' should be invalid.");
    }

    // PASSWORD TESTS

    @Test
    public void verifyValidPasswordComplexity() {
        // Test Data: "Ch&&sec@ke99!"
        // Expected Result: True
        boolean actualResult = testLoginSystem.checkPasswordComplexity("Ch&&sec@ke99!");
        assertTrue(actualResult, "Password 'Ch&&sec@ke99!' should meet complexity requirements.");
    }

    @Test
    public void verifyInvalidPasswordComplexity() {
        // Test Data: "password"
        // Expected Result: False
        boolean actualResult = testLoginSystem.checkPasswordComplexity("password");
        assertFalse(actualResult, "Password 'password' should fail complexity requirements.");
    }

    // CELL PHONE TESTS

    @Test
    public void verifyValidCellPhoneNumber() {
        // Test Data: "+27838968976"
        // Expected Result: True
        boolean actualResult = testLoginSystem.checkCellPhoneNumber("+27838968976");
        assertTrue(actualResult, "Cell number '+27838968976' should be valid.");
    }

    @Test
    public void verifyInvalidCellPhoneNumber() {
        // Test Data: "08966553"
        // Expected Result: False
        boolean actualResult = testLoginSystem.checkCellPhoneNumber("08966553");
        assertFalse(actualResult, "Cell number '08966553' should be invalid.");
    }

    // REGISTRATION STATUS MESSAGE TESTS

    @Test
    public void verifyRegUserInvalidUsernameOutput() {
        String expectedMessage = "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
        String actualMessage = testLoginSystem.registerUser("kyle!!!!!!!", "Ch&&sec@ke99!", "John", "Doe",
                "+27838968976");

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void verifyRegUserInvalidPasswordOutput() {
        String expectedMessage = "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        String actualMessage = testLoginSystem.registerUser("kyl_1", "password", "John", "Doe", "+27838968976");

        assertEquals(expectedMessage, actualMessage);
    }

    // LOGIN AUTHENTICATION TESTS

    @Test
    public void verifyLoginSuccessState() {
        // First, we must register a valid user so the system has something to check
        // against
        testLoginSystem.registerUser("kyl_1", "Ch&&sec@ke99!", "John", "Doe", "+27838968976");

        // Then, we attempt to log in with those exact details. Expected Result: True
        boolean actualResult = testLoginSystem.loginUser("kyl_1", "Ch&&sec@ke99!");
        assertTrue(actualResult);
    }

    @Test
    public void verifyLoginFailedState() {
        // Register a valid user
        testLoginSystem.registerUser("kyl_1", "Ch&&sec@ke99!", "John", "Doe", "+27838968976");

        // Attempt to log in with a WRONG password. Expected Result: False
        boolean actualResult = testLoginSystem.loginUser("kyl_1", "WrongPass123!");
        assertFalse(actualResult);
    }

    @Test
    public void verifyReturnLoginStatusSuccessOutput() {
        testLoginSystem.registerUser("kyl_1", "Ch&&sec@ke99!", "Kyle", "Smith", "+27838968976");

        String expectedMessage = "Welcome Kyle, Smith it is great to see you again.";
        String actualMessage = testLoginSystem.returnLoginStatus(true);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void verifyReturnLoginStatusFailedOutput() {
        String expectedMessage = "Username or password incorrect, please try again.";
        String actualMessage = testLoginSystem.returnLoginStatus(false);

        assertEquals(expectedMessage, actualMessage);
    }
}
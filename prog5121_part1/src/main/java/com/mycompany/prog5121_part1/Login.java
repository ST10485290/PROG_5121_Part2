package com.mycompany.prog5121_part1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login {

    // Variables to store the registered user's details
    private String savedUsername;
    private String savedPassword;
    private String givenName;
    private String surname;
    private String contactNumber;

    // VALIDATION METHODS

    public boolean checkUserName(String inputUser) {
        // The length must be 5 or less, AND it must contain an underscore
        if (inputUser.length() <= 5 && inputUser.contains("_")) {
            return true;
        }
        return false;
    }


     //Checks if the password meets complexity requirements:
     //- At least 8 characters
     //- Contains a capital letter
     //- Contains a number
     // - Contains a special character

    public boolean checkPasswordComplexity(String inputPass) {
        if (inputPass.length() < 8) {
            return false;
        }

        boolean containsUpper = false;
        boolean containsDigit = false;
        boolean containsSpecial = false;

        // Loop through each character to check what type it is
        for (int index = 0; index < inputPass.length(); index++) {
            char charItem = inputPass.charAt(index);

            if (Character.isUpperCase(charItem)) {
                containsUpper = true;
            } else if (Character.isDigit(charItem)) {
                containsDigit = true;
            } else if (!Character.isLetterOrDigit(charItem)) {
                containsSpecial = true;
            }
        }

        // Only returns true if all three conditions are met
        return containsUpper && containsDigit && containsSpecial;
    }

    // Checks if the cell phone number contains an international code and is
    // correctly sized.
    public boolean checkCellPhoneNumber(String phoneInput) {
        // Regex pattern: Starts with '+' followed by 1 to 3 digits (country code),
        // then up to 10 more digits.
        String phoneRegex = "^\\+\\d{1,3}\\d{7,10}$";

        Pattern compiledPattern = Pattern.compile(phoneRegex);
        Matcher patternMatcher = compiledPattern.matcher(phoneInput);

        return patternMatcher.matches();
    }

    // REGISTRATION & LOGIN STATUS METHODS

    /**
     * Attempts to register the user by validating all inputs.
     * If valid, it saves the details to the class variables.
     */
    public String registerUser(String reqUser, String reqPass, String reqFirst, String reqLast, String reqPhone) {
        if (!checkUserName(reqUser)) {
            return "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
        }

        if (!checkPasswordComplexity(reqPass)) {
            return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        }

        if (!checkCellPhoneNumber(reqPhone)) {
            return "Cell phone number incorrectly formatted or does not contain international code.";
        }

        // If all checks pass, store the details
        this.savedUsername = reqUser;
        this.savedPassword = reqPass;
        this.givenName = reqFirst;
        this.surname = reqLast;
        this.contactNumber = reqPhone;

        return "Registration successful.";
    }

    // Verifies if the provided login details match the registered details.
    public boolean loginUser(String attemptUser, String attemptPass) {
        // Check if the inputs match the stored variables exactly
        if (attemptUser.equals(savedUsername) && attemptPass.equals(savedPassword)) {
            return true;
        }
        return false;
    }

    // Returns the login status message.
    public String returnLoginStatus(boolean authSuccess) {
        if (authSuccess) {
            return "Welcome " + givenName + ", " + surname + " it is great to see you again.";
        } else {
            return "Username or password incorrect, please try again.";
        }
    }

}
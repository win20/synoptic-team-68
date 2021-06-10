package sample;

import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Controller {
    int user_id = 0;    // used for storing user, the id is used to easily refer to a specific user

    // Increments userID every time a new user is added
    public void incUserID() {
        int tmp = DatabaseHandler.getLastUserId("users.csv");
        user_id = tmp + 1;
    }

    @FXML TextField fNameField, lNameField, usernameField, passwordField, confirmPassField, loginUsernameField, loginPassField;
    @FXML Text validationText;
    @FXML TabPane tabPane;

    boolean isRegisterFormComplete = false;

    public void registerOnClickEvent(MouseEvent mouseEvent) throws IOException, CsvValidationException {
        String fnameInput = fNameField.getText();
        String lnameInput = lNameField.getText();
        String usernameInput = usernameField.getText();
        String passwordInput = passwordField.getText();
        String confirmPassInput = confirmPassField.getText();

        String alertMsg = "";

        // if user file exists, increment user id, else initialise the database (headers)
        File tmpFile = new File("users.csv");
        if (tmpFile.exists()) {
            incUserID();
        } else {
            try {
                DatabaseHandler.InitDatabase("users.csv");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Alert registerAlert = new Alert(Alert.AlertType.INFORMATION);
        registerAlert.setTitle("Registration information");
        registerAlert.setHeaderText(null);

        // check if every field is completed
        isRegisterFormComplete = !fnameInput.equals("") && !lnameInput.equals("") && !usernameInput.equals("") && !passwordInput.equals("")
                && !confirmPassInput.equals("");

        // check each field against validation rules and set the alert pop up accordingly
        // if all fields are valid store user, reset fields and take user back to login screen
        if (isRegisterFormComplete) {
            if (!isValidName(fnameInput)) {
                alertMsg = "Please enter a valid first name";
                fNameField.setStyle("-fx-text-box-border: #e81a2e; -fx-focus-color: #e81a2e;");
            }
            else if (!isValidName(lnameInput)) {
                alertMsg = "Please enter a valid last name";
                lNameField.setStyle("-fx-text-box-border: #e81a2e; -fx-focus-color: #e81a2e;");
            }
            else if (!isValidPassword(passwordInput)) {
                alertMsg = "Please enter a valid password:\n" +
                        "minimum 1 number\n" +
                        "minimum 1 capital letter\n" +
                        "minimum 9 characters";
                passwordField.setStyle("-fx-text-box-border: #e81a2e; -fx-focus-color: #e81a2e;");
            }
            else if (!passwordInput.equals(confirmPassInput)) {
                alertMsg = "Passwords do not match";
            }
            else if (DatabaseHandler.CheckUsernameExist(usernameInput, "users.csv")) {
                alertMsg = "Username already registered, please enter a different one";
                usernameField.setStyle("-fx-text-box-border: #e81a2e; -fx-focus-color: #e81a2e;");

            }
            else {
                fNameField.setStyle(null);
                lNameField.setStyle(null);
                usernameField.setStyle(null);
                passwordField.setStyle(null);

                UserAccount userAccount = new UserAccount(user_id, fnameInput, lnameInput, usernameInput, passwordInput);
                System.out.println(userAccount.toString());
                try {
                    DatabaseHandler.WriteToCSV(userAccount);
                    DatabaseHandler.storePassAndSalt(userAccount);
                } catch (IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                alertMsg = "Registration Complete!";
                tabPane.getSelectionModel().select(0);

                fNameField.setText("");
                lNameField.setText("");
                usernameField.setText("");
                passwordField.setText("");
                confirmPassField.setText("");

            }

        } else {
            System.out.println("Form incomplete");
            alertMsg = "Registration failed... please complete the form.";
        }

        registerAlert.setContentText(alertMsg);
        registerAlert.showAndWait();
    }

    public void loginOnClickEvent(MouseEvent event) throws IOException, CsvException {
        Alert loginAlert = new Alert(Alert.AlertType.INFORMATION);
        boolean isLoginSuccess = UserAccount.Login(loginUsernameField.getText(), loginPassField.getText());
        loginAlert.setTitle("Login Information");
        loginAlert.setHeaderText(null);

        if (isLoginSuccess) {
            loginAlert.setContentText("Login Successful");
        } else {
            loginAlert.setContentText("Login Failed");
        }

        loginAlert.showAndWait();
    }

    // ******* Methods used for validating registration fields ********
    public static boolean isValidName(String s) {
        String pattern = "[A-Za-z\\s]+";
        return s.matches(pattern);
    }

    public static boolean isValidPassword(String s) {
        String pattern = "^(?=.*[0-9])(?=.*[A-Z]).{8,}$";
        return s.matches(pattern);
    }
    // *****************************************************************


    public void newMemberOnClickEvent(MouseEvent event) {
        tabPane.getSelectionModel().select(1);
    }
}

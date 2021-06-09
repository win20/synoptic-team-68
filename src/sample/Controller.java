package sample;

import com.opencsv.exceptions.CsvException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

//import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Controller {
    int user_id = 0;

    public void incUserID() {
        int tmp = DatabaseHandler.getLastUserId("users.csv");
        System.out.println("last id: " + tmp);
        user_id = tmp + 1;
    }

    @FXML TextField fNameField, lNameField, usernameField, passwordField, confirmPassField, loginUsernameField, loginPassField;

    public void registerOnClickEvent(MouseEvent mouseEvent) {
        String fnameInput = fNameField.getText();
        String lnameInput = lNameField.getText();
        String usernameInput = usernameField.getText();
        String passwordInput = passwordField.getText();
        String confirmPassInput = confirmPassField.getText();

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

        UserAccount userAccount = new UserAccount(user_id, fnameInput, lnameInput, usernameInput, passwordInput);
        System.out.println(userAccount.toString());
        try {
            DatabaseHandler.WriteToCSV(userAccount);
            DatabaseHandler.storePassAndSalt(userAccount);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
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
}

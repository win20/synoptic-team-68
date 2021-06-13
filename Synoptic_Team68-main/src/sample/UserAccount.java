package sample;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class UserAccount {
    public static byte[] saltOut;
    public static byte[] saltIn;

    private int id;
    private String fname, lname, username, password;

    public int getUserId() { return this.id; }
    public String getFname() { return this.fname; }
    public String getLname() { return this.lname; }
    public String getUsername() { return this.username; }
    public String getPassword() { return this.password; }

    public void setUserId(int userId) { this.id = userId; }
    public void setFname(String fname) { this.fname = fname; }
    public void setLname(String lname) { this.lname = lname; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }

    UserAccount(int id, String fname, String lname, String username, String password) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.username = username;
        this.password = password;
    }

    UserAccount(){}

    @Override
    public String toString() {
        return "UserAccount{" +
                "id=" + id +
                ", fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public String getUserInfo() {
        return id + "," + fname + "," + lname + "," + username ;
    }

    static String returnHashedPassword(String passToHash) throws IOException, NoSuchAlgorithmException {
        saltOut = PasswordHasher.getSalt();
        System.out.println("Register: " + Arrays.toString(saltOut));
        String hashedPass = PasswordHasher.hash(passToHash, saltOut);

        StringBuilder formattedSalt = new StringBuilder();
        for (byte b : saltOut) {
            formattedSalt.append(String.format("%02X", b));
        }

        return hashedPass + "," + formattedSalt;
    }

    static boolean Login(String usernameEntered, String passwordEntered) throws IOException, CsvException {
        int id = DatabaseHandler.returnUserId(usernameEntered);
        System.out.println("ID: " + id);

        String[] str = DatabaseHandler.readPasswordAndHash(id);
        String passwordFromDB = str[1];
        String saltHex = str[2];

        saltIn = PasswordHasher.hexToByteArray(saltHex);

        if (id != -1 && PasswordHasher.checkPassword(passwordFromDB, passwordEntered, saltIn)) {
            return true;
        } else {
            return false;
        }
    }
}

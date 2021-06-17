package sample;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseHandler {

    // Initialise user database with headers
    public static void InitDatabase(String path) throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter(path, false));

        String[] heading = ("ID,first_name,last_name,username").split(",");
        writer.writeNext(heading);
        writer.close();
    }

    // Write user data in the database upon registration
    public static void WriteToCSV(UserAccount userAccount) throws IOException {
        String csv = "users.csv";
        CSVWriter writer = new CSVWriter(new FileWriter(csv, true));

        String[] userRecord = userAccount.getUserInfo().split(",");
        writer.writeNext(userRecord);
        writer.close();
    }

    // Method reads last line from csv file without stepping through the whole file, used to increment userId
    // Reference: https://stackoverflow.com/questions/686231/quickly-read-the-last-line-of-a-text-file/7322581#7322581
    public static String tail( File file ) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile( file, "r" );
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for(long filePointer = fileLength; filePointer != -1; filePointer--){
                fileHandler.seek( filePointer );
                int readByte = fileHandler.readByte();

                if( readByte == 0xA ) {
                    if( filePointer == fileLength ) {
                        continue;
                    }
                    break;

                } else if( readByte == 0xD ) {
                    if( filePointer == fileLength - 1 ) {
                        continue;
                    }
                    break;
                }

                sb.append( ( char ) readByte );
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch( java.io.FileNotFoundException e ) {
            e.printStackTrace();
            System.out.println("File Not found");
            return null;
        } catch(java.io.IOException e ) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null )
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    /* ignore */
                }
        }
    }

    public static int getLastUserId(String filePath) {
        String tail = tail(new File(filePath));
        String trimTail = tail.replace("\"", "").trim();

        String[] tailAsArray = trimTail.split(",");
        return Integer.parseInt(tailAsArray[0]);
    }

    public static void storePassAndSalt(UserAccount userAccount) throws IOException, NoSuchAlgorithmException {
        String filePath = "passAndSalt.csv";
        String userIDAsString = String.valueOf(userAccount.getUserId());

        if (new File(filePath).exists()) {
            CSVWriter writer = new CSVWriter(new FileWriter(filePath, true));
            String[] data = (userIDAsString + "," +
                    UserAccount.returnHashedPassword(userAccount.getPassword())).split(",");
            writer.writeNext(data);
            writer.close();
            System.out.println("appended");
        } else {
            CSVWriter writer = new CSVWriter(new FileWriter(filePath, false));
            String[] heading = "UserID,Password,Salt".split(",");
            String[] data = (userIDAsString + "," +
                    UserAccount.returnHashedPassword(userAccount.getPassword())).split(",");
            writer.writeNext(heading);
            writer.writeNext(data);
            writer.close();
            System.out.println("new file created");
        }
    }

    public static int returnUserId(String usernameToSearch) {
        try {
            // Create an object of filereader
            // class with CSV file as a parameter.
            FileReader filereader = new FileReader("users.csv");

            // create csvReader object passing
            // file reader as a parameter
            CSVReader csvReader = new CSVReader(filereader);
            String[] nextRecord;

            // we are going to read data line by line
            while ((nextRecord = csvReader.readNext()) != null) {
//                System.out.println(Arrays.toString(nextRecord));
                if (nextRecord[3].equals(usernameToSearch)) {
                    return Integer.parseInt(nextRecord[0]);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static String[] readPasswordAndHash(int id) throws IOException, CsvException {
        try {
            // Create an object of filereader
            // class with CSV file as a parameter.
            FileReader filereader = new FileReader("passAndSalt.csv");

            // create csvReader object passing
            // file reader as a parameter
            CSVReader csvReader = new CSVReader(filereader);
            String[] nextRecord;

            // we are going to read data line by line
            int i = -1;
            while ((nextRecord = csvReader.readNext()) != null) {
                if (i == id) {
                    return nextRecord;
                }
                i += 1;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static UserAccount returnUserAccount(int id) throws IOException, CsvValidationException {
        FileReader fileReader = new FileReader("users.csv");
        CSVReader csvReader = new CSVReader(fileReader);
        String[] nextRecord;
        UserAccount userAccount = new UserAccount();

        int i = -1;
        while ((nextRecord = csvReader.readNext()) != null) {
            if (i == id) {
                System.out.println(Arrays.toString(nextRecord));
                userAccount.setUserId(Integer.parseInt(nextRecord[0]));
                userAccount.setFname(nextRecord[1]);
                userAccount.setLname(nextRecord[2]);
                userAccount.setUsername(nextRecord[3]);
                userAccount.setBalance(Integer.parseInt(nextRecord[4]));
            }
            i += 1;
        }
        return userAccount;
    }

    public static void UpdateRecord(int userId, UserAccount userAccount) {
        try {
            // Create an object of filereader
            // class with CSV file as a parameter.
            FileReader filereader = new FileReader("users.csv");

            // create csvReader object passing
            // file reader as a parameter
            CSVReader csvReader = new CSVReader(filereader);
            String[] nextRecord;

            String csv = "tmp.csv";
            CSVWriter writer = new CSVWriter(new FileWriter(csv, true));

            // we are going to read data line by line
            while ((nextRecord = csvReader.readNext()) != null) {

                if (nextRecord[0].equals(String.valueOf(userId))) {
                    String[] userRecord = userAccount.getUserInfo().split(",");
                    writer.writeNext(userRecord);

                } else {
                    String[] userRecord = nextRecord;
                    writer.writeNext(userRecord);
                }
            }

            File file = new File("users.csv");
            file.delete();
            File file1 = new File(csv);
            file1.renameTo(new File("users.csv"));
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean CheckID(int id, String path) throws IOException, CsvValidationException {

        if (!new File(path).exists()) return false;

        FileReader fileReader = new FileReader(path);
        CSVReader csvReader = new CSVReader(fileReader);

        boolean returnValue = false;
        String[] nextRecord;
        while ((nextRecord = csvReader.readNext()) != null) {
            if (Integer.parseInt(nextRecord[0]) == id) returnValue = true;
        }

        return returnValue;
    }

    public static boolean CheckUsernameExist(String username, String path) throws IOException, CsvValidationException {

        if (!new File(path).exists()) return false;

        FileReader fileReader = new FileReader(path);
        CSVReader csvReader = new CSVReader(fileReader);

        boolean returnValue = false;
        String[] nextRecord;
        while ((nextRecord = csvReader.readNext()) != null) {
            if (nextRecord[3].equals(username)) returnValue = true;
//            System.out.println(nextRecord[3]);
        }

        return returnValue;
    }

    public static void StoreMarketData(ArrayList<Item> itemsInMarket) throws IOException {
        String csvPath = "marketData.csv";
        CSVWriter appendWriter = new CSVWriter(new FileWriter(csvPath, true));

        for (Item item : itemsInMarket) {
            String[] record = item.returnItemData().split(",");
            appendWriter.writeNext(record);
        }

        appendWriter.close();
    }

    public static ArrayList<Item> LoadMarketData() throws IOException, CsvValidationException {
        FileReader fileReader = new FileReader("marketData.csv");
        CSVReader CSVreader = new CSVReader(fileReader);
        String[] nextRecord;

        ArrayList<Item> returnedList = new ArrayList<>();
        while ((nextRecord = CSVreader.readNext()) != null) {
            Item item = new Item(nextRecord[0], nextRecord[1], nextRecord[2], Integer.parseInt(nextRecord[3]),
                    Integer.parseInt(nextRecord[4]), nextRecord[5]);
            if (item.getStock() != 0) {
                returnedList.add(item);
            }
        }

        return returnedList;
    }

    public static byte[] salt;
    public static void main(String[] args) throws Exception
    {
        CheckUsernameExist("test", "users.csv");

//        System.out.println(MainPageController.itemsInMarket);
    }
}

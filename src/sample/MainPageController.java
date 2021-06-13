package sample;

import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;

public class MainPageController {

    @FXML Button searchBtn;
    @FXML TabPane tabPane;
    @FXML TextField searchBarTF;
    @FXML GridPane itemGridPane;
    @FXML ComboBox<String> sortModeCBX;
    @FXML Text nameTxt;
    public static UserAccount userAccount = new UserAccount();

    Stage stage;

    public void initialize() {
        nameTxt.setText(userAccount.getFname() + " " + userAccount.getLname());

        //TODO temporary test items remove later
        dbItems.add(new Item("Lobster","a lobster", "owner1",80,5));
        dbItems.add(new Item("Salmon", "a salmon", "owner2", 30, 20));
        dbItems.add(new Item("Anchovy", "an anchovy", "owner2", 5, 300));
        dbItems.add(new Item("Squid", "a squid", "owner3", 20, 10));
        sortMarketItems();
    }

    public void switchToWelcomeScreen(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("welcomePage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    public void LogOutOnClick(MouseEvent event) throws IOException {
        switchToWelcomeScreen(event);
    }

    public void onItemClickEvent(MouseEvent event) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        VBox dialogVbox = new VBox(20);

        String buttonId = ((Node) event.getSource()).getId(); //use button to determine what item this is
        Item thisItem = null;
        for (Item i : dbItems) {
            if (i.getItemID().equals(buttonId)) { //search item DB
                thisItem = i;
                break;
            }
        }
        if (thisItem == null) {
            Text notFound = new Text("Item not found. It may have been removed.");
            dialogVbox.getChildren().add(notFound);
            return;
        }
        //set all the text fields for the product
        Text itemName = new Text(thisItem.getItemName());
        Text itemPrice = new Text("Price: " + thisItem.getItemCost());
        itemPrice.setLayoutY(40);
        Text itemStock = new Text("Stock: " + thisItem.getStock());
        itemStock.setLayoutY(80);
        Text itemDesc = new Text("Description: " + thisItem.getItemDesc());
        itemDesc.setLayoutY(120);
        dialogVbox.getChildren().addAll(itemName, itemPrice, itemStock, itemDesc);
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    public ArrayList<Item> dbItems = new ArrayList<>(); //replace with list of products
    public ArrayList<Item> searchItems = new ArrayList<>(); //for holding query results
    public boolean searched = false; //confirm if a search is active
    public int display_count = 9; //how many products are displayed on a single page

    public void loadMarketItems() {
        //TODO for loading additional pages
    }
    public void sortMarketItems() {
        String s = sortModeCBX.getValue();
        int max_display = display_count;
        //new item list = get item list from database, up until public counter var
        ArrayList<Item> newItems;
        itemGridPane.getChildren().clear();
        if (searchItems.isEmpty() && searched) {
            System.out.println("No search results.");
            return;
        }
        else if (searchItems.isEmpty()) {
            newItems = dbItems;
        }
        else {
            newItems = searchItems;
        }
        if (display_count > newItems.size()) { max_display = newItems.size(); }
        switch (s) {
            case "Alphabetical (Ascending)":
                newItems.sort(new AlphabeticalNameSort());
                break;
            case "Alphabetical (Descending)":
                newItems.sort(new AlphabeticalNameSort().reversed());
                break;
            case "Price (Ascending)":
                newItems.sort(new PriceSort());
                break;
            case "Price (Descending)":
                newItems.sort(new PriceSort().reversed());
                break;
            default:
                newItems = dbItems;
                break;
        }
        for (int i = 0; i < max_display; i++) {
            System.out.println(newItems.get(i)); //testing
            Item item =  newItems.get(i);

            //configuring elements for item and creating pane
            Text itemName = new Text(item.getItemName());
            itemName.setLayoutX(9.0); itemName.setLayoutY(113.0);
            Button itemButton = new Button();
            itemButton.setId(item.getItemID());
            itemButton.setLayoutX(150.0); itemButton.setLayoutY(113.0);
            itemButton.setText("View");
            itemButton.setOnMouseClicked(this::onItemClickEvent);
            Pane newItem = new Pane(itemName, itemButton);
            itemGridPane.add(newItem, i % 3, (int) Math.floor(i/3));
        }
    }

    public void searchMarketItems() {
        searchItems.clear();
        String s = searchBarTF.getText();
        if (s == null) {
            searched = false;
            return;
        }
        searched = true;
        for (Item i : dbItems) {
            if (i.getItemName().toLowerCase().contains(s.toLowerCase())) {
                searchItems.add(i);
            }
        }
        if (searchItems.isEmpty()) {
            System.out.println("No search results for " + s);
            s = null;
        }
        sortMarketItems();
    }

    public static void main(String[] args) {
    }
}

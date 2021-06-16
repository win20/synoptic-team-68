package sample;

import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class MainPageController {

    // References to elements on the screen
    @FXML Text nameTxt;
    @FXML TextField itemNameField, itemDescField, itemPriceField;
    @FXML GridPane marketGridPane, basketGridPane;
    @FXML Spinner<Integer> stockSpinner;
    @FXML Button searchBtn;
    @FXML TextField searchBarTF;
    SpinnerValueFactory<Integer> svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);

    @FXML Text balanceTxt, usernameTxt;
    @FXML TextField topUpTxt;
    @FXML ComboBox<String> marketComboBox;
    static final String ALPHABETICAL_SORT_TXT = "Alphabetical (Ascending)";
    static final String ALPHABETICAL_REVERSE_SORT_TXT = "Alphabetical (Descending)";
    static final String PRICE_SORT_TXT = "Price (Ascending)";
    static final String PRICE_SORT_REVERSE_TXT = "Price (Descending)";

    @FXML Text totalCostTxt;

    // Store items added to market
    ArrayList<Item> itemsInMarket = new ArrayList<>();
    ArrayList<Item> tmpItemsList = new ArrayList<>();
    ArrayList<Item> itemsInBasket = new ArrayList<>();

    // User account currently loaded and signed into
    public static UserAccount userAccount = new UserAccount();

    int basketItemQty = 0;
    int basketTotalPrice = 0;

    public static Stage stage;

    // Used to decide whether or not database should be updated
    Boolean isMarketUpdated;

    // Initialize elements that need to be loaded when scene is first shown
    public void initialize() throws IOException, CsvValidationException {
        isMarketUpdated = false;
        tmpItemsList.clear();
        nameTxt.setText(userAccount.getFname() + " " + userAccount.getLname());
        stockSpinner.setValueFactory(svf);
        balanceTxt.setText(String.valueOf(userAccount.getBalance()));
        basketTotalPrice = 0;

        marketComboBox.getItems().addAll(ALPHABETICAL_SORT_TXT, ALPHABETICAL_REVERSE_SORT_TXT, PRICE_SORT_TXT, PRICE_SORT_REVERSE_TXT);

        // Load market data from database and populate the grid
        if (new File("marketData.csv").exists()) {
            itemsInMarket = DatabaseHandler.LoadMarketData();
            for (int i = 0; i < itemsInMarket.size(); i++) {
                Tuple2<Integer, Integer> coordinates = convertIndexToGridCoord(i, 3);

                Pane pane = (Pane) getNodeByCoordinate(marketGridPane, coordinates.getX(), coordinates.getY());

                for (Node node : pane.getChildren()) {
                    try {
                        if (node.getId().equals("nameTxt" + (i + 1))) {
                            ((Text) node).setText(itemsInMarket.get(i).getItemName());
                        }
                    } catch (NullPointerException ignored) {
                    }
                }
            }
        }
        sortMarketItems();
        usernameTxt.setText(userAccount.getUsername());
    }

    // Switch to welcome screen
    public void switchToWelcomeScreen(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("welcomePage.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    // Method is called when user clicks on logout button
    public void LogOutOnClick(MouseEvent event) throws IOException {
        if (isMarketUpdated) {
            DatabaseHandler.StoreMarketData(tmpItemsList);
        }
        switchToWelcomeScreen(event);
    }

    // Method brings up a pop up dialog that displays the information for that item
    public void onViewClickEvent(MouseEvent event) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        VBox dialogVbox = new VBox(20);

        String buttonId = ((Node) event.getSource()).getId(); //use button to determine what item this is
        Item thisItem = null;
        for (Item i : itemsInMarket) {
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
        Text titleTxt = new Text(thisItem.getItemName());
        titleTxt.setTextAlignment(TextAlignment.CENTER);
        titleTxt.setStyle("-fx-font-size: 18px; -fx-font-weight: 800;");

        Text descTxt = new Text("Description: " + thisItem.getItemDesc());
        descTxt.setTextAlignment(TextAlignment.CENTER);

        Text stockTxt = new Text("Stock: " + thisItem.getStock());
        stockTxt.setTextAlignment(TextAlignment.CENTER);

        Text sellerTxt = new Text("Seller: " + thisItem.getItemOwner());
        sellerTxt.setTextAlignment(TextAlignment.CENTER);

        Text priceTxt = new Text("Price: S/" + thisItem.getItemCost());
        priceTxt.setTextAlignment(TextAlignment.CENTER);

        dialogVbox.getChildren().addAll(titleTxt, descTxt, stockTxt, sellerTxt, priceTxt);
        dialogVbox.setAlignment(Pos.CENTER);
        
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    // Method called when sell button is clicked
    // Creates Item object from information entered
    // Adds the item to itemsInMarket ArrayList
    // Add item to marketplace grid
    public void sellOnclickEvent(MouseEvent event) {
        Item item = new Item(itemNameField.getText(), itemDescField.getText(), userAccount.getUsername(),
                Integer.parseInt(itemPriceField.getText()), stockSpinner.getValue());

        itemsInMarket.add(item);
        tmpItemsList.add(item);

        searchMarketItems();

        isMarketUpdated = true;
    }

    // Method used to updated the grid whenever the itemsInMarket ArrayList is updated
    // Used when an item needs to be removed from grid or when sorting is applied
    public void updateMarketGrid() {
        Tuple2<Integer, Integer> coordinates;
        Pane pane;

        for (int i = 0; i < itemsInMarket.size(); i++) {
            coordinates = convertIndexToGridCoord(i, 3);
            pane = (Pane) getNodeByCoordinate(marketGridPane, coordinates.getX(), coordinates.getY());

            Item item = itemsInMarket.get(i);
            for (Node node : pane.getChildren()) {
                try {
                    if (node.getId().equals("nameTxt" + (i + 1))) {
                        ((Text) node).setText(item.getItemName());
                    }
                } catch (NullPointerException ignored) {
                }
            }
        }

        for (int i = itemsInMarket.size(); i < marketGridPane.getChildren().size(); i++) {
            Pane pane1 = (Pane) marketGridPane.getChildren().get(i);
            for (Node node : pane1.getChildren()) {
                try {
                    if (node.getId().equals("nameTxt" + (i + 1))) {
                        ((Text) node).setText("Empty slot...");
                    }
                } catch (NullPointerException ignored) {
                }
            }

        }
    }

    // Returns a Tuple2 item containing coordinates x and y calculated from a flat index.
    public static Tuple2<Integer, Integer> convertIndexToGridCoord(int idx, int numOfRows) {
//        final int numOfRows = 3;

        float tmp = (float) idx / numOfRows;
        int x = idx % numOfRows;
        int y = (int) Math.floor(tmp);

        return new Tuple2<>(x, y);
    }

    // Method returns a Node object from its coordinates within the grid, used for linking Item in pane to..
    // ..correct item in ItemsInMarket ArrayList
    public Node getNodeByCoordinate(GridPane gridPane, Integer x, Integer y) {
        for (Node n : gridPane.getChildren()) {
            if (GridPane.getRowIndex(n) == y && GridPane.getColumnIndex(n) == x) {
                return n;
            }
        }
        return null;
    }

    // Method called when user clicks on buy item
    // Finds correct Item in ItemsInMarket to update and decreases stock by one..
    // * simple implementation but need to add amount selector to allow for adding multiples item to basket *
    public void onBuyClickEvent(MouseEvent event) {
        String buttonId = ((Node) event.getSource()).getId(); //use button to determine what item this is
        Item thisItem = null;

        for (Item i : itemsInMarket) {
            if (i.getItemID().equals(buttonId)) { //search item DB
                thisItem = i;
                break;
            }
        }
        if (thisItem == null) {
            return;
        }

        try {
            addItemtoBasket(thisItem);
        } catch (IndexOutOfBoundsException e) {
            Alert noItemAlert = new Alert(Alert.AlertType.INFORMATION);
            noItemAlert.setHeaderText(null);
            noItemAlert.setTitle(null);
            noItemAlert.setContentText("This slot is empty...");
            noItemAlert.showAndWait();
        }

        try {
            thisItem.removeStock(1);
            DatabaseHandler.StoreMarketData(itemsInMarket);
            if (thisItem.getStock() == 0) {
                itemsInMarket.remove(thisItem);
            }
            searchMarketItems();
        } catch (IndexOutOfBoundsException | IOException e) {
            System.out.println("No item in this slot");
        }


    }

    // Adds onto user's account balance
    public void topUpOnClickEvent(MouseEvent event) {
        int topUpAmount = Integer.parseInt(topUpTxt.getText());
        userAccount.topUpBalance(topUpAmount);
        balanceTxt.setText(String.valueOf(userAccount.getBalance()));
        DatabaseHandler.UpdateRecord(userAccount.getUserId(), userAccount);
    }

    // Controls sorting comboBox, updates grid
    public void comboOnAction(ActionEvent actionEvent) {
        if (marketComboBox.getValue().equals(ALPHABETICAL_SORT_TXT)) {
            itemsInMarket.sort(new AlphabeticalNameSort());
        } else {
            itemsInMarket.sort(new PriceSort());
        }
        updateMarketGrid();
    }

    // creates new pane in basket grid or increases qty if already in basket
    public void addItemtoBasket(Item item) {
        // if item is already in the basket
        if (itemsInBasket.contains(item)) {
            basketItemQty += 1;
            for (int i = 0; i < itemsInBasket.size(); i++) {
                if (itemsInBasket.get(i) == item) {
                    itemsInBasket.get(i).incrementAmountInBasket(1);
                    Pane pane = (Pane) basketGridPane.getChildren().get(i);

                    for (Node node : pane.getChildren()) {
                        try {
                            if (node.getId().equals("priceTxt" + (i + 1))) {
                                int currentVal = Integer.parseInt(((Text) node).getText());
                                ((Text) node).setText(String.valueOf(currentVal + item.getItemCost()));
                            }
                            if (node.getId().equals("qtyTxt" + (i + 1))) {
                                int currentVal = Integer.parseInt(((Text) node).getText());

                                ((Text) node).setText(String.valueOf(currentVal + 1));
                            }
                        } catch (NullPointerException ignored) {
                        }
                    }
                }
            }

        } else {
            itemsInBasket.add(item);
            basketItemQty = 1;
            int itemToAddIdx = itemsInBasket.size() - 1; // get index of item to convert into grid coordinates
            itemsInBasket.get(itemToAddIdx).setAmountInBasket(1);

//            // create tuple of coordinates from index
//            Tuple2<Integer, Integer> coordinates = convertIndexToGridCoord(itemToAddIdx, 5);

            Pane pane = (Pane) basketGridPane.getChildren().get(itemToAddIdx);

            // get Text element that needs to be updated with the name of the item
            for (Node node : pane.getChildren()) {
                try {
                    if (node.getId().equals("bNameTxt" + (itemToAddIdx + 1))) {
                        ((Text) node).setText(item.getItemName());
                    }
                    if (node.getId().equals("priceTxt" + (itemToAddIdx + 1))) {
                        ((Text) node).setText(String.valueOf(item.getItemCost()));
                    }
                    if (node.getId().equals("qtyTxt" + (itemToAddIdx + 1))) {
                        ((Text) node).setText("1");
                    }
                } catch (NullPointerException ignored) { }
            }
        }

        int tmp = 0;
        for (Item item1 : itemsInBasket) {
            tmp += (item1.getItemCost() * item1.getAmountInBasket());
        }

        basketTotalPrice = tmp;
        totalCostTxt.setText(String.valueOf(basketTotalPrice));
    }

    // Checks whether user has enough balance, if yes subtract total cost and reset the basket tab and clear itemsInBasket
    public void CheckoutOnClickEvent(MouseEvent event) {
        int totalCost = Integer.parseInt(totalCostTxt.getText());

        Alert checkoutAlert = new Alert(Alert.AlertType.INFORMATION);
        checkoutAlert.setTitle("Checkout information");
        checkoutAlert.setHeaderText(null);
        if ((userAccount.getBalance() - totalCost) < 0) {
            checkoutAlert.setContentText("Your balance is too low...please top up");
        } else {
            userAccount.deductBalance(totalCost);
            balanceTxt.setText(String.valueOf(userAccount.getBalance()));

            itemsInBasket.clear();

            for (int i = 0; i < basketGridPane.getChildren().size(); i++) {
                Pane pane = (Pane) basketGridPane.getChildren().get(i);

                for (Node node : pane.getChildren()) {
                    try {
                        if (node.getId().equals("bNameTxt" + (i + 1))) {
                            ((Text) node).setText("");
                            System.out.println(node.getId());
                        }
                        if (node.getId().equals("priceTxt" + (i + 1))) {
                            ((Text) node).setText("");
                            System.out.println(node.getId());
                        }
                        if (node.getId().equals("qtyTxt" + (i + 1))) {
                            ((Text) node).setText("0");
                            System.out.println(node.getId());
                        }
                    } catch (NullPointerException ignored) { }
                }
            }

            totalCostTxt.setText("0");
            DatabaseHandler.UpdateRecord(userAccount.getUserId(), userAccount);
            checkoutAlert.setContentText("Payment accepted, " + totalCost + " has been deducted from your account.");
        }

        checkoutAlert.showAndWait();
    }

    public ArrayList<Item> searchItems = new ArrayList<>(); //for holding query results
    public boolean searched = false; //confirm if a search is active
    public int display_count = 9; //how many products are displayed on a single page

    public void sortMarketItems() {
        String s = marketComboBox.getValue();
        int max_display = display_count;
        //new item list = get item list from database, up until public counter var
        ArrayList<Item> newItems;
        marketGridPane.getChildren().clear();
        if (searchItems.isEmpty() && searched) {
            System.out.println("No search results.");
            return;
        }
        else if (searchItems.isEmpty()) {
            newItems = itemsInMarket;
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
            itemButton.setOnMouseClicked(this::onViewClickEvent);
            Button buyButton = new Button();
            buyButton.setId(item.getItemID());
            buyButton.setLayoutX(150.0); buyButton.setLayoutY(80.0);
            buyButton.setText("Buy");
            buyButton.setOnMouseClicked(this::onBuyClickEvent);
            Pane newItem = new Pane(itemName, itemButton, buyButton);
            marketGridPane.add(newItem, i % 3, (int) Math.floor(i/3));
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
        for (Item i : itemsInMarket) {
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

    // Custom Tuple2 class used to store grid coordinates
    public static class Tuple2<K, V> {

        private K x;
        private V y;

        // if x or y is 0 convert it to null, null is used when searching for correct pane using coordinates..
        // ..method used to find pane stores 0s as null to save space
        public Tuple2(K x, V y){
            if ((int)x == 0) this.x = null;
            else this.x = x;

            if ((int)y == 0) this.y = null;
            else this.y = y;
        }

        @Override
        public String toString() {
            return "Tuple2{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

        // getters and setters
        public K getX() {
            return x;
        }

        public V getY() {
            return y;
        }

        public void setX(K x) {
            if ((int)x == 0) this.x = null;
            else this.x = x;
        }

        public void setY(V y) {
            if ((int)y == 0) this.y = null;
            else this.y = y;
        }
    }
}

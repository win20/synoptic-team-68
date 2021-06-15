package sample;

import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
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
    @FXML GridPane marketGridPane;
    @FXML Spinner<Integer> stockSpinner;
    SpinnerValueFactory<Integer> svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);

    @FXML Button buyBtn1;

    // Store items added to market
    ArrayList<Item> itemsInMarket = new ArrayList<>();
    ArrayList<Item> tmpItemsList = new ArrayList<>();

    // User account currently loaded and signed into
    public static UserAccount userAccount = new UserAccount();


    public static Stage stage;

    Boolean isMarketUpdated;

    // Initialize elements that need to be loaded when scene is shown
    public void initialize() throws IOException, CsvValidationException {
        isMarketUpdated = false;
        tmpItemsList.clear();
        nameTxt.setText(userAccount.getFname() + " " + userAccount.getLname());
        stockSpinner.setValueFactory(svf);

        if (new File("marketData.csv").exists()) {
            itemsInMarket = DatabaseHandler.LoadMarketData();
            for (int i = 0; i < itemsInMarket.size(); i++) {
                Tuple2<Integer, Integer> coordinates = convertIndexToGridCoord(i);

                Pane pane = (Pane) getNodeByCoordinate(coordinates.getX(), coordinates.getY());

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
        System.out.println(itemsInMarket);
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
        Pane pane = (Pane) ((Node) event.getSource()).getParent(); // get relevant pane element

        // **** calculate the Item index in the items arrayList to look for ****
        int row;
        int column;

        if (GridPane.getRowIndex(pane) == null) row = 0;
        else row = GridPane.getRowIndex(pane);

        if (GridPane.getColumnIndex(pane) == null) column = 0;
        else column = GridPane.getColumnIndex(pane);

        int itemToUpdateIdx = 3 * row + column;  // convert grid coordinates to flat index

        Item itemToUpdate = new Item();
        boolean isFilled = false;
        try {
            itemToUpdate = itemsInMarket.get(itemToUpdateIdx);
            isFilled = true;
        } catch (IndexOutOfBoundsException e) {
            System.out.println("No item to view...");
            isFilled = false;
        }

        // *************

        if (isFilled) {
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(stage);
            VBox dialogVbox = new VBox(20);

            Text titleTxt = new Text(itemToUpdate.getItemName());
            titleTxt.setTextAlignment(TextAlignment.CENTER);
            titleTxt.setStyle("-fx-font-size: 18px; -fx-font-weight: 800;");

            Text descTxt = new Text("Description: " + itemToUpdate.getItemDesc());
            descTxt.setTextAlignment(TextAlignment.CENTER);

            Text stockTxt = new Text("Stock: " + itemToUpdate.getStock());
            stockTxt.setTextAlignment(TextAlignment.CENTER);

            Text sellerTxt = new Text("Seller: " + itemToUpdate.getItemOwner());
            sellerTxt.setTextAlignment(TextAlignment.CENTER);

            Text priceTxt = new Text("Price: S/" + itemToUpdate.getItemCost());
            priceTxt.setTextAlignment(TextAlignment.CENTER);

            dialogVbox.getChildren().addAll(titleTxt, descTxt, stockTxt, sellerTxt, priceTxt);
            dialogVbox.setAlignment(Pos.CENTER);

            Scene dialogScene = new Scene(dialogVbox, 300, 200);
            dialog.setScene(dialogScene);
            dialog.show();
        } else {
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(stage);
            VBox dialogVbox = new VBox(20);

            Text mainTxt = new Text("No item in this slot...");
            mainTxt.setStyle("-fx-font-size: 24px; -fx-font-weight: 800;");

            dialogVbox.setAlignment(Pos.CENTER);
            dialogVbox.getChildren().add(mainTxt);

            Scene dialogScene = new Scene(dialogVbox, 300, 200);
            dialog.setScene(dialogScene);
            dialog.show();
        }
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
        int itemToAddIdx = itemsInMarket.size() - 1; // get index of item to convert into grid coordinates

        // create tuple of coordinates from index
        Tuple2<Integer, Integer> coordinates = convertIndexToGridCoord(itemToAddIdx);

        // get pane to be added to
        Pane pane = (Pane) getNodeByCoordinate(coordinates.getX(), coordinates.getY());

        // get Text element that needs to be updated with the name of the item
        for (Node node : pane.getChildren()) {
            try {
                if (node.getId().equals("nameTxt" + (itemToAddIdx + 1))) {
                    ((Text) node).setText(item.getItemName());
                }
            } catch (NullPointerException ignored) {
            }
        }

        isMarketUpdated = true;
    }

    public void updateMarketGrid() {
        Tuple2<Integer, Integer> coordinates;
        Pane pane;

        for (int i = 0; i < itemsInMarket.size(); i++) {
            coordinates = convertIndexToGridCoord(i);
            pane = (Pane) getNodeByCoordinate(coordinates.getX(), coordinates.getY());

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

//        for (int i = 0; i < marketGridPane.getChildren().size(); i++) {
//            Pane pane1 = (Pane) marketGridPane.getChildren().get(i);
//            for (Node node : pane1.getChildren()) {
//                try {
//                    if (node.getId().equals("nameTxt" + (i + 1))) {
//                        ((Text) node).setText(item.getItemName());
//                    }
//                } catch (NullPointerException ignored) {
//                }
//            }
//
//        }

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
    public static Tuple2<Integer, Integer> convertIndexToGridCoord(int idx) {
        final int numOfRows = 3;

        float tmp = (float) idx / numOfRows;
        int x = idx % numOfRows;
        int y = (int) Math.floor(tmp);

        return new Tuple2<>(x, y);
    }

    // Method returns a Node object from its coordinates within the grid, used for linking Item in pane to..
    // ..correct item in ItemsInMarket ArrayList
    public Node getNodeByCoordinate(Integer x, Integer y) {
        for (Node n : marketGridPane.getChildren()) {
            if (GridPane.getRowIndex(n) == y && GridPane.getColumnIndex(n) == x) {
                return n;
            }
        }
        return null;
    }

    // Method to test removing last item in market  *** TESTING USE ***
    public void removeOnClickEvent(MouseEvent event) {
        itemsInMarket.remove(itemsInMarket.size() - 1);
        System.out.println();
        System.out.println(itemsInMarket);
    }

    // Method called when user clicks on buy item
    // Finds correct Item in ItemsInMarket to update and decreases stock by one..
    // * simple implementation but need to add amount selector to allow for adding multiples item to basket *
    public void onBuyClickEvent(MouseEvent event) {
        Node pane = ((Node) event.getSource()).getParent();

        // **** Find index to update from pane's coordinates ****
        int row;
        int column;

        if (GridPane.getRowIndex(pane) == null) row = 0;
        else row = GridPane.getRowIndex(pane);

        if (GridPane.getColumnIndex(pane) == null) column = 0;
        else column = GridPane.getColumnIndex(pane);

        int itemToRemoveIdx = 3 * row + column;
        // ****************

        Tuple2<Integer, Integer> coordinates = new Tuple2<>(column, row);

        Item item = new Item();

        try {
            item = itemsInMarket.get(itemToRemoveIdx);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("No item to buy in this slot...");
        }

        try {
            item.removeStock(1);
            if (item.getStock() == 0) {
                itemsInMarket.remove(itemToRemoveIdx);

                for (Node node : ((Pane)pane).getChildren()) {
                    try {
                        if (node.getId().equals("nameTxt" + (itemToRemoveIdx + 1))) {
                            updateMarketGrid();
                            DatabaseHandler.StoreMarketData(itemsInMarket);
                        }
                    } catch (NullPointerException ignored) {
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("No item in this slot");
        }
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
//            this.x = x;
//            this.y = y;
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
//            this.x = x;
        }

        public void setY(V y) {
            if ((int)y == 0) this.y = null;
            else this.y = y;
//            this.y = y;
        }
    }
}

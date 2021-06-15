//13/06/2021 Item Class
/*
Things That need including
Prehaps photo Storage (unsure)
Basket Class (intermediate)
How we are going to store the Items (intermediate ArrayList?)
 */

package sample;

public class Item {
    //Variables
    //ItemID is unqiue, combination of itemName and ItemOwner

    private String itemName, itemDesc, itemOwner, itemID ;
    private int itemCost, stock;

    // Default constructor for initialising empty Items
    public Item() {}

    public Item(String ItemName,String ItemDesc,String ItemOwner,int ItemCost, int Stock){
        this.itemName = ItemName;
        this.itemDesc = ItemDesc;
        this.itemOwner = ItemOwner;
        this.itemCost = ItemCost;
        this.stock = Stock;
        this.itemID = ItemName+ItemOwner;
    }

    //constructor that reads from csv
    public Item(String ItemName,String ItemDesc,String ItemOwner,int ItemCost, int Stock,String ItemID){
        this.itemName = ItemName;
        this.itemDesc = ItemDesc;
        this.itemOwner = ItemOwner;
        this.itemCost = ItemCost;
        this.stock = Stock;
        this.itemID = ItemID;
    }

    //accessors
    public String getItemName() {
        return itemName;
    }
    public String getItemID() {
        return itemID;
    }
    public int getItemCost() {
        return itemCost;
    }
    public int getStock() {
        return stock;
    }
    public String getItemDesc() {
        return itemDesc;
    }
    public String getItemOwner() { return itemOwner; }

    //Mutators
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    public void setItemID(String itemID) {
        this.itemID = itemID;
    }
    public void setItemCost(int itemCost) {
        this.itemCost = itemCost;
    }
    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }
    public void setItemOwner(String itemOwner) {
        this.itemOwner = itemOwner;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }

    //Tostring
    @Override
    public String toString() {
        return "Item{" +
                "itemName='" + itemName + '\'' +
                ", itemDesc='" + itemDesc + '\'' +
                ", itemOwner='" + itemOwner + '\'' +
                ", itemID='" + itemID + '\'' +
                ", itemCost=" + itemCost +
                ", stock=" + stock +
                '}';
    }

    // Returns item data in specific format to write to CSV
    public String returnItemData() {
        return itemName + "," + itemDesc + "," + itemOwner + "," + itemCost + "," + stock + "," + itemID;
    }


    //Methods Add And Remove Stock

    //Returns New Stock Level
    public int removeStock(int Amount){
        if(getStock()-Amount < 0){
            System.out.println("##ERROR## : Balance Too Low " );
            return this.stock;
        }
        else{
            this.stock = this.stock-Amount;
            return this.stock;
        }
    }

    //Add Stock

    public int addStock(int Amount){
        setStock(getStock()+Amount);
        return getStock();
    }




    //Testing
    public static void main(String[] args) {
//        System.out.println("Item Class Says Hello!");
//        Item testItem = new Item("BigFish","A big bloody Fish", "Bob",10,10);
//        System.out.println(testItem.toString());
//
//        //Remove Stock
//        testItem.removeStock(5);
//        System.out.println("Removed 5 Stock So should be 5 " +testItem.getStock());
//        testItem.removeStock(6);
//        System.out.println("Removed 6 from 5 So should not work so should eb 5 : " + testItem.getStock());
//
//        //Add Stock
//        testItem.addStock(5);
//        System.out.println("Have added 5 to stock so should be 10 : " + testItem.getStock());

          MainPageController.Tuple2<Integer, Integer> coord =  MainPageController.convertIndexToGridCoord(0);
          System.out.println(coord.toString());

    }
}
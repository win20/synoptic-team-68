package sample;

import java.util.ArrayList;
import java.util.Comparator;

public class PriceSort implements Comparator<Item>
{
    @Override
    public int compare(Item i1, Item i2) {
        if (i1.getItemCost() == i2.getItemCost()) {
            return 0;
        }
        else if (i1.getItemCost() > i2.getItemCost()) {
            return 1;
        }
        return -1;
    }

    public static void main(String[] args) {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Item("BigFish","A big bloody Fish", "Bob",45,10));
        items.add(new Item("Tuna","A big bloody Fish", "Bob",34,10));
        items.add(new Item("Salmon","A big bloody Fish", "Bob",66,10));
        items.add(new Item("Cod","A big bloody Fish", "Bob",12,10));

        System.out.println("Before sorting: ");
        for (Item item : items) {
            System.out.println(item);
        }

        items.sort(new PriceSort());

        System.out.println("After sorting: ");
        for (Item item : items) {
            System.out.println(item);
        }

    }
}
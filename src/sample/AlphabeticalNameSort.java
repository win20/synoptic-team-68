package sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AlphabeticalNameSort implements Comparator<Item>
{
    @Override
    public int compare(Item i1, Item i2) {
        return i1.getItemName().compareToIgnoreCase(i2.getItemName());
    }

    // ** TESTING **
    public static void main(String[] args) {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Item("BigFish","A big bloody Fish", "Bob",10,10));
        items.add(new Item("Tuna","A big bloody Fish", "Bob",10,10));
        items.add(new Item("Salmon","A big bloody Fish", "Bob",10,10));
        items.add(new Item("Cod","A big bloody Fish", "Bob",10,10));

        System.out.println("Before sorting: ");
        for (Item item : items) {
            System.out.println(item);
        }

        items.sort(new AlphabeticalNameSort());

        System.out.println("After sorting: ");
        for (Item item : items) {
            System.out.println(item);
        }

    }
}
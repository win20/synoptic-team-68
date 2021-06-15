package sample;

import java.util.Comparator;

public class AlphabeticalNameSort implements Comparator<Item>
{
    @Override
    public int compare(Item i1, Item i2) {
        return i1.getItemName().compareToIgnoreCase(i2.getItemName());
    }
}
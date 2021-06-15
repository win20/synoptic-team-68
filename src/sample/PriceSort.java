package sample;

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
}
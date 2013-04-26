package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Simple Index Structure to Index words or labels or any type represented as a String.
 */
public class Index implements Serializable {

    private final HashMap<String, Integer> itemToIndex;
    private final ArrayList<String> indexToItem;

    public Index() {
        this.itemToIndex = new HashMap<String, Integer>();
        this.indexToItem = new ArrayList<String>();
    }

    /**
     * Return the id of a given item;
     *
     * @param item: the item to query.
     * @return the index of the item.
     */
    public Integer getId (String item) {
        return itemToIndex.get(item);
    }

    /**
     * Return the item that corresponds to an id;
     *
     * @param id: the id to query;
     * @return the item corresponding to the id;
     */
    public String getItem (int id) {
        return indexToItem.get(id);
    }

    public ArrayList<String> items () {
        return indexToItem;
    }

    public Integer size () {
        return itemToIndex.size();
    }

    /**
     * Add a new item to the index.
     *
     * @param item: the item to add;
     */
    public void put (String item) {
        if (getId(item) == null) {
            itemToIndex.put(item, itemToIndex.size());
            indexToItem.add(item);
        }
    }
}

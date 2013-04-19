package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

// Simple Index Structure to Index words or labels
public class Index implements Serializable {

    private final HashMap<String, Integer> itemToIndex;
    private final ArrayList<String> indexToItem;

    public Index() {
        this.itemToIndex = new HashMap<String, Integer>();
        this.indexToItem = new ArrayList<String>();
    }

    public Integer getId (String word) {
        return itemToIndex.get(word);
    }

    public String getItem (int id) {
        return indexToItem.get(id);
    }

    public ArrayList<String> items () {
        return indexToItem;
    }

    public Integer size () {
        return itemToIndex.size();
    }

    public void put (String word) {
        if (getId(word) == null) {
            itemToIndex.put(word, itemToIndex.size());
            indexToItem.add(word);
        }
    }
}

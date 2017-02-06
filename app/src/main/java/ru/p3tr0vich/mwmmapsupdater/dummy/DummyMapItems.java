package ru.p3tr0vich.mwmmapsupdater.dummy;

import java.util.ArrayList;
import java.util.List;

import ru.p3tr0vich.mwmmapsupdater.Models.MapItem;

public class DummyMapItems {

    public static final List<MapItem> ITEMS = new ArrayList<>();

    private static final int COUNT = 25;

    static {
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(MapItem item) {
        ITEMS.add(item);
    }

    private static MapItem createDummyItem(int position) {
        MapItem mapItem = new MapItem("id_ " + String.valueOf(position));

        mapItem.setName("Name " + String.valueOf(position));
        mapItem.setDescription("Description " + String.valueOf(position));

        return mapItem;
    }
}
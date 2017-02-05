package ru.p3tr0vich.mwmmapsupdater.dummy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ru.p3tr0vich.mwmmapsupdater.Models.MapItem;

public class DummyContent {

    private static final Random RANDOM = new Random();

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
        MapItem mapItem = new MapItem("Name " + String.valueOf(position));

        mapItem.setDateLocal(new Date(RANDOM.nextLong()));
        mapItem.setDateServer(new Date(RANDOM.nextLong()));

        return mapItem;
    }
}
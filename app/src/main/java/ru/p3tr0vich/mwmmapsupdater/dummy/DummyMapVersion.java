package ru.p3tr0vich.mwmmapsupdater.dummy;

import android.text.format.DateUtils;

import java.util.Date;
import java.util.Random;

import ru.p3tr0vich.mwmmapsupdater.Models.MapVersion;

public class DummyMapVersion {

    private static final Random RANDOM = new Random();

    public static final MapVersion VERSION = createDummyItem();

    private static MapVersion createDummyItem() {
        MapVersion mapVersion = new MapVersion();

        long now = System.currentTimeMillis();

        mapVersion.setDateLocal(new Date(now + Math.round(DateUtils.YEAR_IN_MILLIS * RANDOM.nextDouble())));
        mapVersion.setDateServer(new Date(now + (long) (DateUtils.YEAR_IN_MILLIS * RANDOM.nextDouble())));

        return mapVersion;
    }
}
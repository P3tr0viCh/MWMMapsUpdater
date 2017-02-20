package ru.p3tr0vich.mwmmapsupdater.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MapItem {

    private final String mId;

    private final String mName;
    private final String mDescription;

    public MapItem(@NonNull String id, @Nullable String name, @Nullable String description) {
        mId = id;
        mName = name;
        mDescription = description;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getName() {
        return mName;
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    @Override
    public String toString() {
        return "MapItem{" +
                "mId='" + mId + '\'' +
                ", mName='" + mName + '\'' +
                ", mDescription='" + mDescription + '\'' +
                '}';
    }
}
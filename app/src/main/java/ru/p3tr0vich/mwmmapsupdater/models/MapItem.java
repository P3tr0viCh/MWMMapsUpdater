package ru.p3tr0vich.mwmmapsupdater.models;

public class MapItem {

    private String mId;

    private String mName;
    private String mDescription;

    public MapItem(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
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
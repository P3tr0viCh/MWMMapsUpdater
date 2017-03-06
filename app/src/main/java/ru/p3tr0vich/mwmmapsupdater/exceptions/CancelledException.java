package ru.p3tr0vich.mwmmapsupdater.exceptions;

public class CancelledException extends InterruptedException {
    public CancelledException() {
        super("Cancelled");
    }
}
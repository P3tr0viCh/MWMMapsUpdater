package ru.p3tr0vich.mwmmapsupdater.exceptions;

import java.io.IOException;

public class InternetException extends IOException {
    public InternetException(String message) {
        super(message);
    }
}
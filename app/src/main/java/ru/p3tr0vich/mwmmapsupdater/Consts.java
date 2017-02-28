package ru.p3tr0vich.mwmmapsupdater;

import java.util.regex.Pattern;

public interface Consts {

    /**
     * Ошибочное время.
     */
    int BAD_DATETIME = 0;

    Pattern MAP_SUB_DIR_NAME_PATTERN = Pattern.compile("\\d{6}", Pattern.CASE_INSENSITIVE);

    String MAP_FILE_NAME_EXT = ".mwm";
    Pattern MAP_FILE_NAME_PATTERN = Pattern.compile("^(.+)(\\.mwm)$", Pattern.CASE_INSENSITIVE);
    int MAP_FILE_NAME_PATTERN_GROUP_INDEX = 1;
}
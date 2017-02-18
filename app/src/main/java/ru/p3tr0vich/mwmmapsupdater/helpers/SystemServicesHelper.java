package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.accounts.AccountManager;
import android.content.Context;
import android.net.ConnectivityManager;

public class SystemServicesHelper {

    private SystemServicesHelper() {
    }

    public static AccountManager getAccountManager(Context context) {
        return (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
    }

    public static ConnectivityManager getConnectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}
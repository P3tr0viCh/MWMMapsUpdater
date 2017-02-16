package ru.p3tr0vich.mwmmapsupdater.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import ru.p3tr0vich.mwmmapsupdater.AccountAuthenticator;

public class AuthenticatorService extends Service {

    private AccountAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator = new AccountAuthenticator(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
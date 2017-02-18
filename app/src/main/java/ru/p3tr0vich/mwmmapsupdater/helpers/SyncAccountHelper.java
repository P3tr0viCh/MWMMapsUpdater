package ru.p3tr0vich.mwmmapsupdater.helpers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;

import ru.p3tr0vich.mwmmapsupdater.R;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class SyncAccountHelper {

    private static final String TAG = "SyncAccountHelper";

    private static SyncAccountHelper instance;

    private final AccountManager mAccountManager;

    private final String mAuthority;
    private final String mAccountName;
    private final String mAccountType;

    private final Account mAccount;

    private SyncAccountHelper(@NonNull Context context) {
        mAccountManager = SystemServicesHelper.getAccountManager(context);

        mAuthority = context.getString(R.string.sync_authority);
        mAccountName = context.getString(R.string.sync_account_name);
        mAccountType = context.getString(R.string.sync_account_type);

        mAccount = createAccount();

        // TODO: отключить по умолчанию, включать в настройках
        setIsSyncable(true);
        setSyncAutomatically(true);
    }

    public static synchronized SyncAccountHelper getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new SyncAccountHelper(context.getApplicationContext());
        }

        return instance;
    }

    private Account createAccount() {
        Account account;

        Account accounts[] = mAccountManager.getAccountsByType(getAccountType());

        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = new Account(getAccountName(), getAccountType());

            if (mAccountManager.addAccountExplicitly(account, null, null)) {
                UtilsLog.d(true, TAG, "createAccount", "addAccountExplicitly return true");
            } else {
                UtilsLog.d(true, TAG, "createAccount", "addAccountExplicitly return false");
            }
        }

        return account;
    }

    public String getAuthority() {
        return mAuthority;
    }

    private String getAccountName() {
        return mAccountName;
    }

    private String getAccountType() {
        return mAccountType;
    }

    public Account getAccount() {
        return mAccount;
    }

    public boolean isSyncActive() {
        return ContentResolver.isSyncActive(getAccount(), getAuthority());
    }

    private void setIsSyncable(Account account, boolean syncable) {
        ContentResolver.setIsSyncable(account, getAuthority(), syncable ? 1 : 0);
    }

    public void setIsSyncable(boolean syncable) {
        setIsSyncable(getAccount(), syncable);
    }

    public void setSyncAutomatically(boolean sync) {
        ContentResolver.setSyncAutomatically(getAccount(), getAuthority(), sync);
    }
}
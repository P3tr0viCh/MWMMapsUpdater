package ru.p3tr0vich.mwmmapsupdater;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.NonNull;

import ru.p3tr0vich.mwmmapsupdater.helpers.ContentResolverHelper;
import ru.p3tr0vich.mwmmapsupdater.helpers.SystemServicesHelper;
import ru.p3tr0vich.mwmmapsupdater.utils.UtilsLog;

public class AppAccount {

    private static final String TAG = "AppAccount";

    private final AccountManager mAccountManager;

    private final String mAuthority;
    private final String mAccountName;
    private final String mAccountType;

    private Account mAccount;

    public AppAccount(@NonNull Context context) {
        mAccountManager = SystemServicesHelper.getAccountManager(context);

        mAuthority = context.getString(R.string.sync_authority);
        mAccountName = context.getString(R.string.sync_account_name);
        mAccountType = context.getString(R.string.sync_account_type);

        if (createAccount()) {
            UtilsLog.d(true, TAG, "Account added");

            // TODO: отключить по умолчанию, включать в настройках
            ContentResolverHelper.setIsSyncable(this, true);
            ContentResolverHelper.setSyncAutomatically(this, true);
            ContentResolverHelper.addPeriodicSync(this, ContentResolverHelper.POLL_FREQUENCY_6_HRS);
        } else {
            UtilsLog.d(true, TAG, "Account exists");
        }
    }

    private boolean createAccount() {
        Account accounts[] = mAccountManager.getAccountsByType(getAccountType());

        if (accounts.length > 0) {
            mAccount = accounts[0];

            return false;
        } else {
            mAccount = new Account(getAccountName(), getAccountType());

            return mAccountManager.addAccountExplicitly(mAccount, null, null);
        }
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
}
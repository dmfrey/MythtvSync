package org.mythtv.android.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.mythtv.android.provider.MythtvProvider;
import org.mythtv.android.service.AccountService;

/**
 * Created by dmfrey on 10/24/13.
 */
public class SyncUtils {

    private static final String TAG = SyncUtils.class.getSimpleName();

    private static final long SYNC_FREQUENCY = 60 * 10;  // 10 minutes (in seconds)
    private static final String CONTENT_AUTHORITY = MythtvProvider.AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    public static void CreateSyncAccount( Context context ) {

        boolean newAccount = false;
        boolean setupComplete = PreferenceManager.getDefaultSharedPreferences( context ).getBoolean( PREF_SETUP_COMPLETE, false );

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = AccountService.GetAccount();
        AccountManager accountManager = (AccountManager) context.getSystemService( Context.ACCOUNT_SERVICE );
        if( accountManager.addAccountExplicitly( account, null, null ) ) {

            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable( account, CONTENT_AUTHORITY, 1 );

            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically( account, CONTENT_AUTHORITY, true );

            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync( account, CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY );

            newAccount = true;
        }

        if( newAccount || !setupComplete ) {
            TriggerRefresh();
            PreferenceManager.getDefaultSharedPreferences( context ).edit().putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }

    }

    public static void TriggerRefresh() {

        Bundle b = new Bundle();

        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean( ContentResolver.SYNC_EXTRAS_MANUAL, true );
        b.putBoolean( ContentResolver.SYNC_EXTRAS_EXPEDITED, true );

        ContentResolver.requestSync(
            AccountService.GetAccount(),        // Sync account
            MythtvProvider.AUTHORITY,           // Content authority
            b
        );                                 // Extras
    }

}

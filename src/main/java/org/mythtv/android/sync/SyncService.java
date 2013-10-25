package org.mythtv.android.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by dmfrey on 10/24/13.
 */
public class SyncService extends Service {

    private static final String TAG = SyncService.class.getSimpleName();

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d( TAG, "onCreate : enter");
        super.onCreate();

        synchronized ( sSyncAdapterLock ) {
            if( null == sSyncAdapter ) {
                sSyncAdapter = new SyncAdapter( getApplicationContext(), true );

                Log.i( TAG, "onCreate : Service created");
            }
        }

        Log.d( TAG, "onCreate : exit");
    }

    @Override
    public void onDestroy() {
        Log.d( TAG, "onDestroy : enter" );
        super.onDestroy();

        Log.i( TAG, "onDestroy : Service destroyed" );

        Log.d( TAG, "onDestroy : exit" );
    }

    @Override
    public IBinder onBind( Intent intent ) {
        Log.d( TAG, "onBind : enter" );
        Log.d( TAG, "onBind : exit" );
        return sSyncAdapter.getSyncAdapterBinder();
    }

}

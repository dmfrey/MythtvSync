package org.mythtv.android.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.mythtv.android.db.dvr.RecordedHelperV27;
import org.mythtv.android.db.locationProfile.LocationProfile;
import org.mythtv.android.db.locationProfile.LocationProfileDaoHelper;

/**
 * Created by dmfrey on 10/24/13.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();

    private final Context mContext;
    private final ContentResolver mContentResolver;

    private final LocationProfileDaoHelper mLocationProfileDaoHelper = LocationProfileDaoHelper.getInstance();

    private LocationProfile mLocationProfile;

    public SyncAdapter( Context context, boolean autoInitialize ) {
        super( context, autoInitialize );

        mContext = context;
        mContentResolver = context.getContentResolver();

        mLocationProfile = mLocationProfileDaoHelper.findConnectedProfile( context );
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter( Context context, boolean autoInitialize, boolean allowParallelSyncs ) {
        super( context, autoInitialize, allowParallelSyncs );

        mContext = context;
        mContentResolver = context.getContentResolver();

        mLocationProfile = mLocationProfileDaoHelper.findConnectedProfile( context );
    }

    @Override
    public void onPerformSync( Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult ) {
        Log.i( TAG, "onPerformSync : Beginning network synchronization" );

        RecordedHelperV27.getInstance().process( mContext, mLocationProfile );

        Log.i(TAG, " onPerformSync : Network synchronization complete" );
    }

}

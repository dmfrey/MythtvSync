package org.mythtv.android.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;

import org.mythtv.android.R;
import org.mythtv.android.db.dvr.ProgramConstants;
import org.mythtv.android.db.locationProfile.LocationProfile;
import org.mythtv.android.db.locationProfile.LocationProfileDaoHelper;
import org.mythtv.android.sync.SyncUtils;

/**
 * Created by dmfrey on 10/23/13.
 */
public class RecordingsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = RecordingsFragment.class.getSimpleName();
    private static final String[] projection = new String[] { ProgramConstants._ID, ProgramConstants.FIELD_TITLE, ProgramConstants.FIELD_SUB_TITLE };
    private static final String[] fromFields = new String[] { ProgramConstants.FIELD_TITLE, ProgramConstants.FIELD_SUB_TITLE };
    private static final int[] toViews = { android.R.id.text1, android.R.id.text2 };

    private final LocationProfileDaoHelper mLocationProfileDaoHelper = LocationProfileDaoHelper.getInstance();

    private SimpleCursorAdapter adapter;

    private LocationProfile mLocationProfile;

    @Override
    public Loader<Cursor> onCreateLoader( int id, Bundle args ) {
        Log.v( TAG, "onCreateLoader : enter" );

        String selection = ProgramConstants.FIELD_MASTER_HOSTNAME + " = ?";
        String[] selectionArgs = new String[] { mLocationProfile.getHostname() };
        String sortOrder = ProgramConstants.FIELD_END_TIME + " DESC";

        CursorLoader cursorLoader = new CursorLoader( getActivity(), ProgramConstants.CONTENT_URI_RECORDED, projection, selection, selectionArgs, sortOrder );

        Log.v( TAG, "onCreateLoader : exit" );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished( Loader<Cursor> loader, Cursor cursor ) {
        Log.v( TAG, "onLoadFinished : enter" );

        adapter.swapCursor( cursor );

        getListView().setFastScrollEnabled( true );

        Log.v( TAG, "onLoadFinished : exit" );
    }

    @Override
    public void onLoaderReset( Loader<Cursor> loader ) {
        Log.v( TAG, "onLoaderReset : enter" );

        adapter.swapCursor( null );

        Log.v(TAG, "onLoaderReset : exit");
    }

    @Override
    public void onAttach( Activity activity ) {
        Log.i( TAG, "onAttach : enter" );
        super.onAttach( activity );

        SyncUtils.CreateSyncAccount(activity);

        Log.i( TAG, "onAttach : exit" );
    }

    @Override
    public void onViewCreated( View view, Bundle savedInstanceState ) {
        Log.i( TAG, "onViewCreated : enter" );
        super.onViewCreated( view, savedInstanceState );

        mLocationProfile = mLocationProfileDaoHelper.findConnectedProfile( getActivity() );

        adapter = new SimpleCursorAdapter( getActivity(), android.R.layout.simple_list_item_activated_2, null, fromFields, toViews, 0 );

        setListAdapter( adapter );
        setEmptyText( getResources().getString( R.string.msg_empty ) );

        getLoaderManager().initLoader( 0, null, this );

        Log.i( TAG, "onViewCreated : exit" );
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.i( TAG, "onActivityCreated : enter" );
        super.onActivityCreated(savedInstanceState);

        Log.i( TAG, "onActivityCreated : exit" );
    }

}
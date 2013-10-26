package org.mythtv.android.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.mythtv.android.R;
import org.mythtv.android.db.locationProfile.LocationProfile;
import org.mythtv.android.db.locationProfile.LocationProfileDaoHelper;
import org.mythtv.android.sync.SyncUtils;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final LocationProfileDaoHelper mLocationProfileDaoHelper = LocationProfileDaoHelper.getInstance();

    private FragmentManager mFragmentManager;
    private FrameLayout mRecordingsFrameLayout;
    private Button mBtnHome, mBtnAway, mBtnDisconnect;
    private TextView mTextView;

    private Menu mOptionsMenu;

    private LocationProfile mLocationProfile;
    private RecordingsFragment mRecordingsFragment;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        Log.i( TAG, "onCreate : enter" );
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_main );

        mFragmentManager = getFragmentManager();

        mRecordingsFrameLayout = (FrameLayout) findViewById( R.id.frame_layout_recordings );

        mBtnHome = (Button) findViewById( R.id.btn_home );
        mBtnAway = (Button) findViewById( R.id.btn_away );
        mBtnDisconnect = (Button) findViewById( R.id.btn_disconnect );
        mTextView = (TextView) findViewById( R.id.text_view );

        mBtnHome.setEnabled( true );
        mBtnAway.setEnabled( true );
        mBtnDisconnect.setEnabled(false);

        Log.i( TAG, "onCreate : exit" );
    }

    @Override
    protected void onResume() {
        Log.i( TAG, "onResume : enter" );
        super.onResume();

        mLocationProfileDaoHelper.resetConnectedProfiles(this);

        updateView();

        Log.i( TAG, "onResume : exit" );
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy : enter");

        mLocationProfileDaoHelper.resetConnectedProfiles( this );

        super.onDestroy();
        Log.i( TAG, "onDestroy : exit" );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {

        super.onCreateOptionsMenu( menu );

        mOptionsMenu = menu;

        getMenuInflater().inflate( R.menu.main, menu );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {

        switch( item.getItemId() ) {

            case R.id.menu_refresh :

                SyncUtils.TriggerRefresh();

                return true;

            case R.id.menu_preferences :

                showDialog();

                return true;
        }

        return super.onOptionsItemSelected( item );
    }

    public void clickHome( View v ) {
        Log.i( TAG, "clickHome : enter" );

        mLocationProfile = mLocationProfileDaoHelper.findSelectedHomeProfile( this );

        updateView();

        Log.i( TAG, "clickHome : exit" );
    }

    public void clickAway( View v ) {
        Log.i( TAG, "clickAway : enter" );

        mLocationProfile = mLocationProfileDaoHelper.findSelectedAwayProfile( this );

        updateView();

        Log.i( TAG, "clickAway : exit" );
    }

    public void clickDisconnect( View v ) {
        Log.i( TAG, "clickDisconnect : enter" );

        mLocationProfile = null;

        updateView();

        Log.i( TAG, "clickDisconnect : exit" );
    }

    // internal helpers

    private void updateView() {
        Log.d( TAG, "updateView : enter" );

        if( null != mLocationProfile ) {
            Log.v( TAG, "updateView : updating with profile" );
            
            mLocationProfileDaoHelper.setConnectedLocationProfile( this, mLocationProfile.getId() );
            mTextView.setText( mLocationProfile.getHostname() + ":" + mLocationProfile.getUrl() );

            mBtnHome.setEnabled( false );
            mBtnAway.setEnabled( false );
            mBtnDisconnect.setEnabled( true );

            loadRecordingsFragment();

        } else {
            Log.v( TAG, "updateView : resetting with no profile" );

            mLocationProfileDaoHelper.resetConnectedProfiles( this );
            mTextView.setText(getResources().getString(R.string.msg_not_connected));

            mBtnHome.setEnabled( true );
            mBtnAway.setEnabled( true );
            mBtnDisconnect.setEnabled( false );

            removeRecordingsFragment();
        }

        Log.d( TAG, "updateView : exit" );
    }

    private void loadRecordingsFragment() {
        Log.d( TAG, "loadRecordingsFragment : enter" );

        removeRecordingsFragment();

        mRecordingsFragment = (RecordingsFragment) RecordingsFragment.instantiate( this, RecordingsFragment.class.getName() );

        mFragmentManager
                .beginTransaction()
                .replace( R.id.frame_layout_recordings, mRecordingsFragment, RecordingsFragment.class.getName() ).commit();


        Log.d( TAG, "loadRecordingsFragment : exit" );
    }

    private void removeRecordingsFragment() {
        Log.d( TAG, "removeRecordingsFragment : enter" );

        if( null != mRecordingsFragment ) {
            Log.v( TAG, "removeRecordingsFragment : removing fragment" );

            mFragmentManager
                .beginTransaction()
                .remove( mRecordingsFragment ).commit();

        }

        Log.d( TAG, "removeRecordingsFragment : exit" );
    }

    private void showDialog() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag( "dialog" );
        if( null != prev ) {
            ft.remove( prev );
        }
        ft.addToBackStack( null );

        // Create and show the dialog.
        LocationProfileDialogFragment newFragment = LocationProfileDialogFragment.newInstance();
        newFragment.show( ft, "dialog" );

    }

}

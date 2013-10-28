package org.mythtv.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.mythtv.android.R;
import org.mythtv.android.db.locationProfile.LocationProfile;
import org.mythtv.android.db.locationProfile.LocationProfileDaoHelper;

/**
 * Created by dmfrey on 10/25/13.
 */
public class LocationProfileDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = LocationProfileDialogFragment.class.getSimpleName();

    private final LocationProfileDaoHelper mLocationProfileDaoHelper = LocationProfileDaoHelper.getInstance();

    private Spinner mLocationProfileTypeSpinner;
    private EditText mName, mUrl, mHostname;

    private LocationProfile mLocationProfile;
    private LocationProfile.LocationType mSelectedLocationType = LocationProfile.LocationType.HOME;

    public static LocationProfileDialogFragment newInstance() {
        LocationProfileDialogFragment f = new LocationProfileDialogFragment();

        return f;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        Log.i(TAG, "onActivityCreated : enter");
        super.onActivityCreated( savedInstanceState );

        mLocationProfile = mLocationProfileDaoHelper.findSelectedHomeProfile( getActivity() );

        updateFields();

        Log.i( TAG, "onActivityCreated : exit" );
    }

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState ) {

        View v = LayoutInflater.from(getActivity()).inflate( R.layout.fragment_location_profile_dialog, null);

        mLocationProfileTypeSpinner = (Spinner) v.findViewById( R.id.location_profile_type_spinner );

        mName = (EditText) v.findViewById( R.id.name_editText );
        mUrl = (EditText) v.findViewById( R.id.url_editText );
        mHostname = (EditText) v.findViewById( R.id.hostname_editText );

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource( getActivity(), R.array.location_profile_types_array, android.R.layout.simple_spinner_item );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLocationProfileTypeSpinner.setAdapter(adapter);
        mLocationProfileTypeSpinner.setOnItemSelectedListener( this );

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.lbl_location_profile_dialog_title)
                .setView(v)
                .setPositiveButton(R.string.btn_save,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                save();
                            }
                        }
                )
                .setNegativeButton(R.string.btn_close,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
    }

    public void onItemSelected( AdapterView<?> parent, View view, int pos, long id ) {
        Log.d(TAG, "onItemSelected : enter");

        String type = (String) parent.getItemAtPosition( pos );

        switch ( LocationProfile.LocationType.valueOf( type ) ) {

            case AWAY :

                mLocationProfile = mLocationProfileDaoHelper.findSelectedAwayProfile( getActivity() );

                mSelectedLocationType = LocationProfile.LocationType.AWAY;

                break;

            case HOME :

                mLocationProfile = mLocationProfileDaoHelper.findSelectedHomeProfile(getActivity());

                mSelectedLocationType = LocationProfile.LocationType.HOME;

                break;
        }

        updateFields();

        Log.d(TAG, "onItemSelected : exit");
    }

    public void onNothingSelected( AdapterView<?> parent ) {
        Log.d( TAG, "onNothingSelected : enter" );

        Log.d( TAG, "onNothingSelected : exit" );
    }

    private void updateFields() {

        mName.setText( mLocationProfile.getName() );
        mUrl.setText( mLocationProfile.getUrl() );
        mHostname.setText( mLocationProfile.getHostname() );

    }

    private void save() {

        mLocationProfile.setName( mName.getText().toString() );
        mLocationProfile.setUrl( mUrl.getText().toString() );
        mLocationProfile.setHostname( mHostname.getText().toString() );

        mLocationProfileDaoHelper.save( getActivity(), mLocationProfile );
    }

}

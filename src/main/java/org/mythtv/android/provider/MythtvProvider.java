package org.mythtv.android.provider;

import static android.provider.BaseColumns._ID;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.mythtv.android.db.DatabaseHelper;
import org.mythtv.android.db.dvr.ProgramConstants;
import org.mythtv.android.db.locationProfile.LocationProfileConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmfrey on 10/22/13.
 */
public class MythtvProvider extends ContentProvider {

    private static final String TAG = MythtvProvider.class.getSimpleName();

    public static final String AUTHORITY = "org.mythtv.android.frontend";

    private static final UriMatcher URI_MATCHER;

    private static final String LOCATION_PROFILE_CONTENT_TYPE               = "vnd.mythtv.cursor.dir/org.mythtv.android.locationProfile";
    private static final String LOCATION_PROFILE_CONTENT_ITEM_TYPE          = "vnd.mythtv.cursor.item/org.mythtv.android.locationProfile";
    private static final int LOCATION_PROFILE 		                        = 1000;
    private static final int LOCATION_PROFILE_ID 			                = 1001;

    private static final String RECORDED_CONTENT_TYPE                       = "vnd.mythtv.cursor.dir/org.mythtv.android.recorded";
    private static final String RECORDED_CONTENT_ITEM_TYPE                  = "vnd.mythtv.cursor.item/org.mythtv.android.recorded";
    private static final int RECORDED 					                    = 2000;
    private static final int RECORDED_ID 					                = 2001;

    static {
        URI_MATCHER = new UriMatcher( UriMatcher.NO_MATCH );

        URI_MATCHER.addURI( AUTHORITY, LocationProfileConstants.TABLE_NAME, LOCATION_PROFILE );
        URI_MATCHER.addURI( AUTHORITY, LocationProfileConstants.TABLE_NAME + "/#", LOCATION_PROFILE_ID );

        URI_MATCHER.addURI( AUTHORITY, ProgramConstants.TABLE_NAME_RECORDED, RECORDED );
        URI_MATCHER.addURI( AUTHORITY, ProgramConstants.TABLE_NAME_RECORDED + "/#", RECORDED_ID );

    }

    private DatabaseHelper database = null;

    @Override
    public boolean onCreate() {
		Log.v( TAG, "onCreate : enter" );

        database = new DatabaseHelper( getContext() );

		Log.v( TAG, "onCreate : exit" );
        return ( null == database ? false : true );
    }

    @Override
    public String getType( Uri uri ) {
		Log.v( TAG, "getType : enter" );

        switch( URI_MATCHER.match( uri ) ) {

            case LOCATION_PROFILE:
                Log.v( TAG, "getType : exit, Location Profile" );

                return LOCATION_PROFILE_CONTENT_TYPE;

            case LOCATION_PROFILE_ID:
                Log.v( TAG, "getType : exit, Location Profile Item" );

                return LOCATION_PROFILE_CONTENT_ITEM_TYPE;

            case RECORDED:
                Log.v( TAG, "getType : exit, Recorded" );

                return RECORDED_CONTENT_TYPE;

            case RECORDED_ID:
                Log.v( TAG, "getType : exit, Recorded Item" );

                return RECORDED_CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

    }

    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder ) {
        Log.v( TAG, "query : enter" );

        final SQLiteDatabase db = database.getReadableDatabase();

        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        StringBuilder sb = new StringBuilder();

        Cursor cursor = null;

        switch( URI_MATCHER.match( uri ) ) {

            case LOCATION_PROFILE:

                cursor = db.query( LocationProfileConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );
                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case LOCATION_PROFILE_ID:
                selection = LocationProfileConstants.TABLE_NAME + "." + appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( LocationProfileConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );
                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case RECORDED:
                cursor = db.query( ProgramConstants.TABLE_NAME_RECORDED, projection, selection, selectionArgs, null, null, sortOrder );
                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case RECORDED_ID:
                selection = ProgramConstants.TABLE_NAME_RECORDED + "." + appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );

                cursor = db.query( ProgramConstants.TABLE_NAME_RECORDED, projection, selection, selectionArgs, null, null, sortOrder );
                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

        }

        return null;
    }

    @Override
    public Uri insert( Uri uri, ContentValues values ) {
        Log.v( TAG, "insert : enter" );

        final SQLiteDatabase db = database.getWritableDatabase();

        Uri newUri = null;

        switch( URI_MATCHER.match( uri ) ) {

            case LOCATION_PROFILE:
                newUri = ContentUris.withAppendedId( LocationProfileConstants.CONTENT_URI, db.insertWithOnConflict( LocationProfileConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            case RECORDED:
                newUri = ContentUris.withAppendedId( ProgramConstants.CONTENT_URI_RECORDED, db.insertWithOnConflict( ProgramConstants.TABLE_NAME_RECORDED, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );

        }

    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs ) {
        Log.v( TAG, "delete : enter" );

        final SQLiteDatabase db = database.getWritableDatabase();

        int deleted;

        switch( URI_MATCHER.match( uri ) ) {

            case LOCATION_PROFILE:

                deleted = db.delete( LocationProfileConstants.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case LOCATION_PROFILE_ID:

                deleted = db.delete( LocationProfileConstants.TABLE_NAME, LocationProfileConstants._ID
                        + "="
                        + Long.toString( ContentUris.parseId(uri) )
                        + ( !TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "" ), selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case RECORDED:

                deleted = db.delete( ProgramConstants.TABLE_NAME_RECORDED, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case RECORDED_ID:

                deleted = db.delete( ProgramConstants.TABLE_NAME_RECORDED, ProgramConstants._ID
                        + "="
                        + Long.toString( ContentUris.parseId( uri ) )
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "" ), selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }

    }

    @Override
    public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs ) {
        Log.v( TAG, "update : enter" );

        final SQLiteDatabase db = database.getWritableDatabase();

        int affected = 0;

        switch( URI_MATCHER.match( uri ) ) {

            case LOCATION_PROFILE:
                affected = db.update( LocationProfileConstants.TABLE_NAME, values, selection , selectionArgs );
                break;

            case LOCATION_PROFILE_ID:
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                affected = db.update( LocationProfileConstants.TABLE_NAME, values, selection , selectionArgs );
                break;

            case RECORDED:
                affected = db.update( ProgramConstants.TABLE_NAME_RECORDED, values, selection , selectionArgs );
                break;

            case RECORDED_ID:
                selection = appendRowId( selection, Long.parseLong( uri.getPathSegments().get( 1 ) ) );
                affected = db.update( ProgramConstants.TABLE_NAME_RECORDED, values, selection , selectionArgs );
                break;

            default:
                throw new IllegalArgumentException( "Unknown URI: " + uri );

        }

        getContext().getContentResolver().notifyChange( uri, null );

        return affected;
    }

    // internal helpers

    protected String appendRowId( String selection, long id ) {
        return _ID
                + "="
                + id
                + (!TextUtils.isEmpty( selection ) ? " AND (" + selection + ')' : "");
    }

}

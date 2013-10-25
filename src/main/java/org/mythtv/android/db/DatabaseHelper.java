package org.mythtv.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.joda.time.DateTime;
import org.mythtv.android.db.dvr.ProgramConstants;
import org.mythtv.android.db.locationProfile.LocationProfileConstants;

/**
 * Created by dmfrey on 10/22/13.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "mythtvdb";
    private static final int DATABASE_VERSION = 4;

    public DatabaseHelper( Context context ) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
        Log.v( TAG, "initialize : enter" );

        Log.v( TAG, "initialize : exit" );
    }

    /* (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onOpen(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onOpen( SQLiteDatabase db ) {
        Log.v( TAG, "onOpen : enter" );
        super.onOpen( db );

        if( !db.isReadOnly() ) {
            Log.i( TAG, "onOpen : turning on referencial integrity" );

            db.execSQL( "PRAGMA foreign_keys = ON;" );
        }

        Log.v( TAG, "onOpen : exit" );
    }

    /* (non-Javadoc)
 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
 */
    @Override
    public void onCreate( SQLiteDatabase db ) {
        Log.v( TAG, "onCreate : enter" );

        dropLocationProfiles( db );
        createLocationProfiles( db );

        dropProgram( db, ProgramConstants.TABLE_NAME_RECORDED );
        createProgram( db, ProgramConstants.TABLE_NAME_RECORDED );

        Log.v( TAG, "onCreate : exit" );
    }

    /* (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
        Log.v( TAG, "onUpgrade : enter" );

        if( oldVersion < 4 ) {
            Log.v( TAG, "onUpgrade : upgrading to db version 4" );

            onCreate( db );

        }

        Log.v( TAG, "onUpgrade : exit" );
    }

    // internal helpers

    private void createLocationProfiles( SQLiteDatabase db ) {
        Log.v( TAG, "createLocationProfiles : enter" );

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append( "CREATE TABLE " + LocationProfileConstants.TABLE_NAME + " (" );
        sqlBuilder.append( LocationProfileConstants._ID ).append( " " ).append( LocationProfileConstants.FIELD_ID_DATA_TYPE ).append( " " ).append( LocationProfileConstants.FIELD_ID_PRIMARY_KEY ).append( ", " );
        sqlBuilder.append( LocationProfileConstants.FIELD_TYPE ).append( " " ).append( LocationProfileConstants.FIELD_TYPE_DATA_TYPE ).append( ", " );
        sqlBuilder.append( LocationProfileConstants.FIELD_NAME ).append( " " ).append( LocationProfileConstants.FIELD_NAME_DATA_TYPE ).append( ", " );
        sqlBuilder.append( LocationProfileConstants.FIELD_URL ).append( " " ).append( LocationProfileConstants.FIELD_URL_DATA_TYPE ).append( ", " );
        sqlBuilder.append( LocationProfileConstants.FIELD_SELECTED ).append( " " ).append( LocationProfileConstants.FIELD_SELECTED_DATA_TYPE ).append( " default " ).append( LocationProfileConstants.FIELD_SELECTED_DEFAULT ).append( ", " );
        sqlBuilder.append( LocationProfileConstants.FIELD_CONNECTED ).append( " " ).append( LocationProfileConstants.FIELD_CONNECTED_DATA_TYPE ).append( " default " ).append( LocationProfileConstants.FIELD_CONNECTED_DEFAULT ).append( ", " );
        sqlBuilder.append( LocationProfileConstants.FIELD_VERSION ).append( " " ).append( LocationProfileConstants.FIELD_VERSION_DATA_TYPE ).append( ", " );
        sqlBuilder.append( LocationProfileConstants.FIELD_PROTOCOL_VERSION ).append( " " ).append( LocationProfileConstants.FIELD_PROTOCOL_VERSION_DATA_TYPE ).append( ", " );
        sqlBuilder.append( LocationProfileConstants.FIELD_WOL_ADDRESS ).append( " " ).append( LocationProfileConstants.FIELD_WOL_ADDRESS_DATA_TYPE ).append( ", " );
        sqlBuilder.append( LocationProfileConstants.FIELD_HOSTNAME ).append( " " ).append( LocationProfileConstants.FIELD_HOSTNAME_DATA_TYPE ).append( ", " );
        sqlBuilder.append( LocationProfileConstants.FIELD_NEXT_MYTHFILLDATABASE ).append( " " ).append( LocationProfileConstants.FIELD_NEXT_MYTHFILLDATABASE_DATA_TYPE );
        sqlBuilder.append( ");" );
        String sql = sqlBuilder.toString();
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createLocationProfiles : sql=" + sql );
        }
        db.execSQL( sql );

		ContentValues values = new ContentValues();
		values.put( LocationProfileConstants.FIELD_TYPE, "HOME" );
		values.put( LocationProfileConstants.FIELD_NAME, "Home" );
		values.put( LocationProfileConstants.FIELD_URL, "http://192.168.10.200:6544/" );
		values.put( LocationProfileConstants.FIELD_SELECTED, 1 );
        values.put( LocationProfileConstants.FIELD_CONNECTED, 0 );
		values.put( LocationProfileConstants.FIELD_VERSION, "0.27" );
		values.put( LocationProfileConstants.FIELD_PROTOCOL_VERSION, "77" );
		values.put( LocationProfileConstants.FIELD_WOL_ADDRESS, "50:e5:49:d9:02:db" );
		values.put( LocationProfileConstants.FIELD_HOSTNAME, "mythcenter" );
        values.put( LocationProfileConstants.FIELD_NEXT_MYTHFILLDATABASE, new DateTime().getMillis() );
		db.insert( LocationProfileConstants.TABLE_NAME, null, values );

        values = new ContentValues();
        values.put( LocationProfileConstants.FIELD_TYPE, "AWAY" );
        values.put( LocationProfileConstants.FIELD_NAME, "Tunnel" );
        values.put( LocationProfileConstants.FIELD_URL, "http://10.0.2.2:6544/" );
        values.put( LocationProfileConstants.FIELD_SELECTED, 1 );
        values.put( LocationProfileConstants.FIELD_CONNECTED, 0 );
        values.put( LocationProfileConstants.FIELD_VERSION, "0.27" );
        values.put( LocationProfileConstants.FIELD_PROTOCOL_VERSION, "77" );
        values.put( LocationProfileConstants.FIELD_WOL_ADDRESS, "" );
        values.put( LocationProfileConstants.FIELD_HOSTNAME, "mythcenter" );
        values.put( LocationProfileConstants.FIELD_NEXT_MYTHFILLDATABASE, new DateTime().getMillis() );
        db.insert( LocationProfileConstants.TABLE_NAME, null, values );

        Log.v( TAG, "createLocationProfiles : exit" );
    }

    private void dropLocationProfiles( SQLiteDatabase db ) {
        Log.v( TAG, "dropLocationProfiles : enter" );

        db.execSQL( "DROP TABLE IF EXISTS " + LocationProfileConstants.TABLE_NAME );

        Log.v( TAG, "dropLocationProfiles : exit" );
    }

    private void createProgram( SQLiteDatabase db, String tableName ) {
        Log.v( TAG, "createProgram : enter" );

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append( "CREATE TABLE " + tableName + " (" );
        sqlBuilder.append( ProgramConstants._ID ).append( " " ).append( ProgramConstants.FIELD_ID_DATA_TYPE ).append( " " ).append( ProgramConstants.FIELD_ID_PRIMARY_KEY ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_START_TIME ).append( " " ).append( ProgramConstants.FIELD_START_TIME_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_END_TIME ).append( " " ).append( ProgramConstants.FIELD_END_TIME_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_TITLE ).append( " " ).append( ProgramConstants.FIELD_TITLE_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_SUB_TITLE ).append( " " ).append( ProgramConstants.FIELD_SUB_TITLE_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_CATEGORY ).append( " " ).append( ProgramConstants.FIELD_CATEGORY_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_CATEGORY_TYPE ).append( " " ).append( ProgramConstants.FIELD_CATEGORY_TYPE_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_REPEAT ).append( " " ).append( ProgramConstants.FIELD_REPEAT_DATA_TYPE ).append( " default " ).append( ProgramConstants.FIELD_REPEAT_DEFAULT ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_VIDEO_PROPS ).append( " " ).append( ProgramConstants.FIELD_VIDEO_PROPS_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_AUDIO_PROPS ).append( " " ).append( ProgramConstants.FIELD_AUDIO_PROPS_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_SUB_PROPS ).append( " " ).append( ProgramConstants.FIELD_SUB_PROPS_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_SERIES_ID ).append( " " ).append( ProgramConstants.FIELD_SERIES_ID_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_PROGRAM_ID ).append( " " ).append( ProgramConstants.FIELD_PROGRAM_ID_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_STARS ).append( " " ).append( ProgramConstants.FIELD_STARS_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_FILE_SIZE ).append( " " ).append( ProgramConstants.FIELD_FILE_SIZE_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_LAST_MODIFIED ).append( " " ).append( ProgramConstants.FIELD_LAST_MODIFIED_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_PROGRAM_FLAGS ).append( " " ).append( ProgramConstants.FIELD_PROGRAM_FLAGS_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_HOSTNAME ).append( " " ).append( ProgramConstants.FIELD_HOSTNAME_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_FILENAME ).append( " " ).append( ProgramConstants.FIELD_FILENAME_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_AIR_DATE ).append( " " ).append( ProgramConstants.FIELD_AIR_DATE_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_DESCRIPTION ).append( " " ).append( ProgramConstants.FIELD_DESCRIPTION_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_INETREF ).append( " " ).append( ProgramConstants.FIELD_INETREF_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_SEASON ).append( " " ).append( ProgramConstants.FIELD_SEASON_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_EPISODE ).append( " " ).append( ProgramConstants.FIELD_EPISODE_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_CHANNEL_ID ).append( " " ).append( ProgramConstants.FIELD_CHANNEL_ID_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_RECORD_ID ).append( " " ).append( ProgramConstants.FIELD_RECORD_ID_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_IN_ERROR ).append( " " ).append( ProgramConstants.FIELD_IN_ERROR_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_MASTER_HOSTNAME ).append( " " ).append( ProgramConstants.FIELD_MASTER_HOSTNAME_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_LAST_MODIFIED_DATE ).append( " " ).append( ProgramConstants.FIELD_LAST_MODIFIED_DATE_DATA_TYPE ).append( ", " );
        sqlBuilder.append( ProgramConstants.FIELD_LAST_MODIFIED_TAG ).append( " " ).append( ProgramConstants.FIELD_LAST_MODIFIED_TAG_DATA_TYPE ).append( ", " );
        sqlBuilder.append( "UNIQUE(" ).append( ProgramConstants.FIELD_CHANNEL_ID ).append( ", " ).append( ProgramConstants.FIELD_START_TIME ).append( ", " ).append( ProgramConstants.FIELD_MASTER_HOSTNAME ).append( ") ON CONFLICT REPLACE " );
        sqlBuilder.append( ");" );
        String sql = sqlBuilder.toString();
        if( Log.isLoggable( TAG, Log.VERBOSE ) ) {
            Log.v( TAG, "createProgram : sql=" + sql );
        }
        db.execSQL( sql );

        Log.v( TAG, "createProgram : exit" );
    }

    private void dropProgram( SQLiteDatabase db, String tableName ) {
        Log.v( TAG, "dropProgram : enter" );

        db.execSQL( "DROP TABLE IF EXISTS " + tableName );

        Log.v( TAG, "dropProgram : exit" );
    }

}

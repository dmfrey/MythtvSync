/**
 * This file is part of MythTV Android Frontend
 *
 * MythTV Android Frontend is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MythTV Android Frontend is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MythTV Android Frontend.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This software can be found at <https://github.com/MythTV-Clients/MythTV-Android-Frontend/>
 */
/**
 * 
 */
package org.mythtv.android.db.dvr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.mythtv.android.db.locationProfile.LocationProfile;
import org.mythtv.android.db.AbstractBaseHelper;
import org.mythtv.android.db.dvr.ProgramConstants;
import org.mythtv.android.utils.NetworkHelper;
import org.mythtv.services.api.ApiVersion;
import org.mythtv.services.api.ETagInfo;
import org.mythtv.services.api.MythServiceApiRuntimeException;
import org.mythtv.services.api.connect.MythAccessFactory;
import org.mythtv.services.api.v027.MythServicesTemplate;
import org.mythtv.services.api.v027.beans.Program;
import org.mythtv.services.api.v027.beans.ProgramList;
import org.mythtv.services.utils.ArticleCleaner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

/**
 * @author Daniel Frey
 *
 */
public class RecordedHelperV27 extends AbstractBaseHelper {

	private static final String TAG = RecordedHelperV27.class.getSimpleName();
	
	private static final ApiVersion mApiVersion = ApiVersion.v027;
	
	private static MythServicesTemplate mMythServicesTemplate;

	private static RecordedHelperV27 singleton;
	
	/**
	 * Returns the one and only RecordedHelperV27. init() must be called before 
	 * any 
	 * @return
	 */
	public static RecordedHelperV27 getInstance() {
		if( null == singleton ) {
			
			synchronized( RecordedHelperV27.class ) {

				if( null == singleton ) {
					singleton = new RecordedHelperV27();
				}
			
			}
			
		}
		
		return singleton;
	}
	
	/**
	 * Constructor. No one but getInstance() can do this.
	 */
	private RecordedHelperV27() { }

	public boolean process( final Context context, final LocationProfile locationProfile ) {
		Log.v( TAG, "process : enter" );
		
		if( !NetworkHelper.getInstance().isMasterBackendConnected( context, locationProfile ) ) {
			Log.w( TAG, "process : Master Backend '" + locationProfile.getHostname() + "' is unreachable" );
			
			return false;
		}
		
		mMythServicesTemplate = (MythServicesTemplate) MythAccessFactory.getServiceTemplateApiByVersion( mApiVersion, locationProfile.getUrl() );
		if( null == mMythServicesTemplate ) {
			Log.w( TAG, "process : Master Backend '" + locationProfile.getHostname() + "' is unreachable" );
			
			return false;
		}
		
		boolean passed = true;

		try {

			downloadRecorded( context, locationProfile );
			
		} catch( Exception e ) {

			if( e.toString().contains( "Invalid UTF-8" ) ) {
				Log.e( TAG, "process : INVALID UTF-8! Start mythbackend with valid LANG & LC_ALL (e.g. en_US.UTF-8" );
			}
			else {
				Log.e( TAG, "process : non UTF-8 exception ", e );
			}

			passed = false;
		}

		Log.v( TAG, "process : exit" );
		return passed;
	}
	
	public Integer countRecordedByTitle( final Context context, final LocationProfile locationProfile, String title ) {
		Log.v( TAG, "countRecordedByTitle : enter" );
		
		Integer count = ProgramHelperV27.getInstance().countProgramsByTitle( context, locationProfile, ProgramConstants.CONTENT_URI_RECORDED, ProgramConstants.TABLE_NAME_RECORDED, title );
//		Log.v( TAG, "countRecordedByTitle : count=" + count );
		
		Log.v( TAG, "countRecordedByTitle : exit" );
		return count;
	}
	
	public Program findRecorded( final Context context, final LocationProfile locationProfile, Integer channelId, DateTime startTime ) {
		Log.v( TAG, "findRecorded : enter" );
		
		Program program = ProgramHelperV27.getInstance().findProgram( context, locationProfile, ProgramConstants.CONTENT_URI_RECORDED, ProgramConstants.TABLE_NAME_RECORDED, channelId, startTime );
		
		Log.v( TAG, "findRecorded : enter" );
		return program;
	}
	
	public boolean deleteRecorded( final Context context, final LocationProfile locationProfile, Integer channelId, DateTime startTime, Integer recordId ) {
		Log.v( TAG, "deleteRecorded : enter" );
		
		boolean removed = false;
		
		ProgramHelperV27 programHelper = ProgramHelperV27.getInstance();
		
		Program program = programHelper.findProgram( context, locationProfile, ProgramConstants.CONTENT_URI_RECORDED, ProgramConstants.TABLE_NAME_RECORDED, channelId, startTime );
		if( null != program ) {
			Log.v( TAG, "deleteRecorded : program found!" );
			
			String title = program.getTitle();
			
			removed = programHelper.deleteProgram( context, locationProfile, ProgramConstants.CONTENT_URI_RECORDED, ProgramConstants.TABLE_NAME_RECORDED, program.getChannel().getChanId(), program.getStartTime(), program.getRecording().getStartTs(), recordId );
			if( removed ) {
//				Log.v( TAG, "deleteRecorded : program removed from backend" );
				
			}
		
		}
		
		Log.v( TAG, "deleteRecorded : exit" );
		return removed;
	}

	// internal helpers
	
	private void downloadRecorded( final Context context, final LocationProfile locationProfile ) throws MythServiceApiRuntimeException, RemoteException, OperationApplicationException {
		Log.v( TAG, "downloadRecorded : enter" );
	
//		EtagInfoDelegate etag = mEtagDaoHelper.findByEndpointAndDataId( context, locationProfile, "GetRecordedList", "" );
//		Log.d( TAG, "downloadRecorded : etag=" + etag.getValue() );

		ResponseEntity<ProgramList> responseEntity = mMythServicesTemplate.dvrOperations().getRecordedList( Boolean.FALSE, null, null, null, null, null, ETagInfo.createEmptyETag() );

		DateTime date = new DateTime( DateTimeZone.UTC );
		if( responseEntity.getStatusCode().equals( HttpStatus.OK ) ) {
			Log.i( TAG, "download : GetRecordedList returned 200 OK" );
			ProgramList programList = responseEntity.getBody();

			if( null != programList.getPrograms() ) {

				load( context, locationProfile, programList.getPrograms() );	

//				if( null != etag.getValue() ) {
//					Log.i( TAG, "download : saving etag: " + etag.getValue() );
//
//					etag.setEndpoint( "GetRecordedList" );
//					etag.setDate( date );
//					etag.setMasterHostname( locationProfile.getHostname() );
//					etag.setLastModified( date );
//					mEtagDaoHelper.save( context, locationProfile, etag );
//				}

			}

		}

		if( responseEntity.getStatusCode().equals( HttpStatus.NOT_MODIFIED ) ) {
			Log.i( TAG, "download : GetRecordedList returned 304 Not Modified" );

//			if( null != etag.getValue() ) {
//				Log.i( TAG, "download : saving etag: " + etag.getValue() );
//
//				etag.setLastModified( date );
//				mEtagDaoHelper.save( context, locationProfile, etag );
//			}

		}

		Log.v( TAG, "downloadRecorded : exit" );
	}
	
	private int load( final Context context, final LocationProfile locationProfile, final Program[] programs ) throws RemoteException, OperationApplicationException {
		Log.d( TAG, "load : enter" );
		
		if( null == context ) 
			throw new RuntimeException( "RecordedHelperV27 is not initialized" );
		
		String tag = UUID.randomUUID().toString();
		int processed = -1;
		int count = 0;
		
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		
		boolean inError;

		List<Integer> channelsChecked = new ArrayList<Integer>();
		
		for( Program program : programs ) {

			if( null != program.getRecording() && "livetv".equalsIgnoreCase( program.getRecording().getRecGroup() )  && !"deleted".equalsIgnoreCase( program.getRecording().getRecGroup() ) ) {
				Log.w( TAG, "load : program has no recording or program is in livetv or deleted recording groups:" + program.getTitle() + ":" + program.getSubTitle() + ":" + program.getChannel().getChanId() + ":" + program.getStartTime() + ":" + program.getHostName() + " (" + ( null == program.getRecording() ? "No Recording" : ( "livetv".equalsIgnoreCase( program.getRecording().getRecGroup() ) ? "LiveTv" : "Deleted" ) ) + ")" );

				continue;
			}
			
			if( null == program.getStartTime() || null == program.getEndTime() ) {
				Log.w( TAG, "load : null starttime and or endtime" );
			
				inError = true;
			} else {
				inError = false;
			}

			ProgramHelperV27.getInstance().processProgram( context, locationProfile, ProgramConstants.CONTENT_URI_RECORDED, ProgramConstants.TABLE_NAME_RECORDED, ops, program, tag );
			count++;
			
			if( count > BATCH_COUNT_LIMIT ) {
				Log.i( TAG, "load : applying batch for '" + count + "' transactions" );
				
				processBatch( context, ops, processed, count );

				count = 0;
				
			}
			
		}

		if( !ops.isEmpty() ) {
			Log.i( TAG, "load : applying final batch for '" + count + "' transactions" );
			
			processBatch( context, ops, processed, count );
		}

		ProgramHelperV27.getInstance().deletePrograms( context, locationProfile, ProgramConstants.CONTENT_URI_RECORDED, ProgramConstants.TABLE_NAME_RECORDED, tag );
//		RecordingHelperV27.getInstance().deleteRecordings( context, locationProfile, ops, RecordingConstants.ContentDetails.RECORDED, lastModified );

		if( !ops.isEmpty() ) {
			Log.i( TAG, "load : applying delete batch for transactions" );
			
			processBatch( context, ops, processed, count );
		}
		
//		Log.v( TAG, "load : exit" );
		return processed;
	}

}

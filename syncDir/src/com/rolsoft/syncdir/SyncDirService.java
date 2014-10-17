/**
 * Service to synchronize folders in background
 */
package com.rolsoft.syncdir;

import java.io.File;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * @author z002t9wj
 *
 */
public class SyncDirService extends IntentService implements ConnectionCallbacks {
	
	private static final String TAG = "syncDir-syncDirService";
	
	private GoogleApiClient mGoogleApiClient = null;
	
	private DriveId folderId = null;

	public SyncDirService(String name) {
		super(name);

		if (mGoogleApiClient == null)
		{
			createGoogleApiClient();
		}
		
		mGoogleApiClient.connect();
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent workIntent) {
		// Get data from the incoming Intent
		Bundle data = workIntent.getExtras();
		String localPath = data.getString(getString(R.string.LOCAL_PATH_KEY));
		String drivePathIdEncodeded = data.getString(getString(R.string.DRIVE_PATH_KEY));
		
		String filesInLocalFolder = getFilesInLocalFolder(localPath);
		String filesInDriveFolder = getFilesInDriveFolder(drivePathIdEncodeded);
		
	}

	private String getFilesInDriveFolder(String drivePathIdEncodeded) {
		driveId = DriveId.decodeFromString(drivePathIdEncodeded);
		
	}

	private String getFilesInLocalFolder(String localPath) {
		String ret = "";
		File fileLocalFolder = new File(localPath);
		if (fileLocalFolder.isDirectory()) {
			for (String fileName: fileLocalFolder.list()) {
				ret += fileName + " ";
			}

		}
		return ret;
	}

	private void createGoogleApiClient() {
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
			.addApi(Drive.API)
			.addScope(Drive.SCOPE_FILE)
			.addScope(Drive.SCOPE_APPFOLDER)
			.addConnectionCallbacks(this)
			.build();
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.i(TAG, "GoogleApiClient connected");
		
		if (folderId != null) {
			
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}

}

package com.rolsoft.syncdir;

import net.bgreco.DirectoryPicker;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.OpenFileActivityBuilder;

public class MainActivity extends Activity implements ConnectionCallbacks,
OnConnectionFailedListener {

	private static final String TAG = "syncDir-MainActivity";

	/**
	 * Code for Google Drive opener activity
	 */
	private static final int REQUEST_CODE_DRIVE_OPENER = 1;

	/**
	 * Code for Google API resolution in connection
	 */
	private static final int REQUEST_CODE_RESOLUTION = 3;

	/**
	 * Google Api client
	 */
	private GoogleApiClient mGoogleApiClient;

	/**
	 * Is synchronization turned on 
	 */
	private boolean mSyncOn = false;

	/**
	 * Uri for local folder 
	 */
	private Uri mlocalFolder = null;

	/**
	 * DriveId for folder on Google Drive 
	 */
	private String mdriveFolderEncodedId = null;

	private Button buttonLocalPath = null;

	private Button buttonDrivePath = null;

	private Switch switchSync = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		buttonLocalPath = (Button) findViewById(R.id.buttonLocalPath);
		buttonLocalPath.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Choose local folder to sync
				showFileChooser();
			}
		});

		buttonDrivePath = (Button) findViewById(R.id.buttonDrivePath);
		buttonDrivePath.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get DriveId for folder that should be synchronized with local folder
				IntentSender intentSender = Drive.DriveApi
						.newOpenFileActivityBuilder()
						.setMimeType(new String[] {DriveFolder.MIME_TYPE})
						.build(mGoogleApiClient);

				try {
					startIntentSenderForResult(
							intentSender, REQUEST_CODE_DRIVE_OPENER, null, 
							0, 0, 0);
				} catch (SendIntentException e) {
					Log.w(TAG, "Unable to send intent", e);
				}

			}
		});

		switchSync = (Switch) findViewById(R.id.switchSync);
		switchSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mSyncOn = isChecked;

				checkSyncAndControllsState();

			}
		});
	}


	private void showFileChooser() {
		final Intent folderPicker = new Intent(this, DirectoryPicker.class);
		folderPicker.putExtra(DirectoryPicker.ONLY_DIRS, true);
		startActivityForResult(folderPicker, DirectoryPicker.PICK_DIRECTORY);		
	}

	@Override
	protected void onResume() {
		super.onResume();

		readPreferences();

		createGoogleApiClient();
		if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
			mGoogleApiClient.connect();
		}

		checkSyncAndControllsState();
	}

	private void createGoogleApiClient() {
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
			.addApi(Drive.API)
			.addScope(Drive.SCOPE_FILE)
			.addScope(Drive.SCOPE_APPFOLDER)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.build();
		}
	}

	@Override
	protected void onPause() {

		savePreferences();

		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
		super.onPause();
	}

	private void savePreferences() {
		SharedPreferences sharedPref = getApplication().getSharedPreferences(
				getString(R.string.PREFERENCE_FILE_KEY), 
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(getString(R.string.LOCAL_PATH_KEY), mlocalFolder.toString());
		editor.putString(getString(R.string.DRIVE_PATH_KEY), mdriveFolderEncodedId);
		editor.putBoolean(getString(R.string.SYNC_STATE_KEY), mSyncOn);

		editor.commit();
	}


	private void readPreferences() {
		SharedPreferences sharedPref = getApplication().getSharedPreferences(
				getString(R.string.PREFERENCE_FILE_KEY), 
				Context.MODE_PRIVATE);
		mlocalFolder = Uri.parse(sharedPref.getString(getString(R.string.LOCAL_PATH_KEY), Uri.EMPTY.toString()));
		mdriveFolderEncodedId = sharedPref.getString(getString(R.string.DRIVE_PATH_KEY), "");
		mSyncOn = sharedPref.getBoolean(getString(R.string.SYNC_STATE_KEY), false);

		updateButtonTitle(R.id.buttonLocalPath, getString(R.string.localPathTitle) 
				+ mlocalFolder.toString());

	}

	private void updateButtonTitle(int id, String text) {
		final Button buttonLocalPath = (Button) findViewById(id);
		buttonLocalPath.setText(text);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {

		case DirectoryPicker.PICK_DIRECTORY:
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				String path = (String) extras.get(DirectoryPicker.CHOSEN_DIRECTORY);
				mlocalFolder = Uri.parse(path);

				showMessage("Local folder is " + mlocalFolder.toString());
			}
			break;

		case REQUEST_CODE_DRIVE_OPENER:
			// Set {@code driveFolderId} to selected folder on the drive
			if (resultCode == RESULT_OK) {
				mdriveFolderEncodedId = ((DriveId) data.getParcelableExtra(
						OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID)).encodeToString();
			}
			break;
		case REQUEST_CODE_RESOLUTION:
			if (resultCode == RESULT_OK) {
				mGoogleApiClient.connect();
			}
			break;		

		default:
			finish();	
		}

		savePreferences();

		checkSyncAndControllsState();
	}

	private void checkSyncAndControllsState() {
		if (mGoogleApiClient.isConnected()) {
			// connected, check if paths set
			if (!mlocalFolder.equals(Uri.EMPTY)) {

				// set local button name
				updateButtonTitle(R.id.buttonLocalPath, getString(R.string.localPathTitle) 
						+ mlocalFolder.toString());

				if (!mdriveFolderEncodedId.isEmpty())
				{
					// driveButton title set in onActivityResult
					// paths set
					switchSync.setEnabled(true);
					switchSync.setChecked(mSyncOn);

					if (mSyncOn) {
						// disable buttons
						disableControlls(false);
						return;
					}
				}
			}
		} else {
			// not connected, disable all
			disableControlls(true);
			return;
		}

		// enable buttons
		buttonLocalPath.setEnabled(true);
		buttonDrivePath.setEnabled(true);

	}

	private void disableControlls(boolean withSwitch) {
		buttonDrivePath.setEnabled(false);
		buttonLocalPath.setEnabled(false);

		if (withSwitch) {
			switchSync.setEnabled(false);
		}
	}

	/**
	 * Shows a toast message.
	 */
	public void showMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.i(TAG, "Api client connected.");

		if (!mdriveFolderEncodedId.isEmpty()) {

			DriveFolder driveFolder = Drive.DriveApi.getFolder(mGoogleApiClient, 
					DriveId.decodeFromString(mdriveFolderEncodedId));
			driveFolder.getMetadata(mGoogleApiClient).setResultCallback(
					new ResultCallback<DriveResource.MetadataResult>() {

						@Override
						public void onResult(DriveResource.MetadataResult metaData) {

							updateButtonTitle(R.id.buttonDrivePath, getString(R.string.drivePathTitle) 
									+ metaData.getMetadata().getTitle());

							showMessage("Selected folder title is: " + metaData.getMetadata().getTitle());

						}

					});
		}

		checkSyncAndControllsState();

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		disableControlls(true);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Called if connection failed
		disableControlls(true);

		Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
		if (!result.hasResolution()) {
			// show the localized error dialog
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
			return;
		}

		// has resolution, usually authorization issue
		try {
			result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
		} catch (SendIntentException e) {
			Log.e(TAG, "Exception while starting resolution activity", e);
		}
	}
}

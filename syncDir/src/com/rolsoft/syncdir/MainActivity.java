package com.rolsoft.syncdir;

import net.bgreco.DirectoryPicker;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
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
	 * Code for local folder opener activity
	 */
	private static final int REQUEST_CODE_LOCAL_FOLDER_SELECTOR = 2;

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
	private boolean bSyncOn = false;

	/**
	 * Uri for local folder 
	 */
	private Uri localFolder = null;

	/**
	 * DriveId for folder on Google Drive 
	 */
	private DriveId driveFolderId = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Button buttonLocalPath = (Button) findViewById(R.id.buttonLocalPath);
		buttonLocalPath.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Choose local folder to sync
				showFileChooser();
			}
		});

		final Button buttonDrivePath = (Button) findViewById(R.id.buttonDrivePath);
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

		final Switch switchSync = (Switch) findViewById(R.id.switchSync);
		switchSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				bSyncOn = isChecked;
			}
		});
	}


	private void showFileChooser() {
		final Intent folderPicker = new Intent(this, DirectoryPicker.class);
		folderPicker.putExtra(DirectoryPicker.ONLY_DIRS, true);
		startActivityForResult(folderPicker, DirectoryPicker.PICK_DIRECTORY);		
	}

	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		// Inflate the menu; this adds items to the action bar if it is present.
	//		getMenuInflater().inflate(R.menu.main, menu);
	//		return true;
	//	}

	@Override
	protected void onResume() {
		super.onResume();

		createGoogleApiClient();
		if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
			mGoogleApiClient.connect();
		}
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
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
		super.onPause();
	}

	//	@Override
	//	public boolean onOptionsItemSelected(MenuItem item) {
	//		// Handle action bar item clicks here. The action bar will
	//		// automatically handle clicks on the Home/Up button, so long
	//		// as you specify a parent activity in AndroidManifest.xml.
	//		int id = item.getItemId();
	//		if (id == R.id.action_settings) {
	//			return true;
	//		}
	//		return super.onOptionsItemSelected(item);
	//	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {

		case DirectoryPicker.PICK_DIRECTORY:
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				String path = (String) extras.get(DirectoryPicker.CHOSEN_DIRECTORY);
				localFolder = Uri.parse(path);
				showMessage("Local folder is " + localFolder.toString());
			}

		case REQUEST_CODE_DRIVE_OPENER:
			// Set {@code driveFolderId} to selected folder on the drive
			if (resultCode == RESULT_OK) {
				driveFolderId = (DriveId) data.getParcelableExtra(
						OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
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

		final Button buttonDrivePath = (Button) findViewById(R.id.buttonDrivePath);
		buttonDrivePath.setEnabled(true);

		final Button buttonLocalPath = (Button) findViewById(R.id.buttonLocalPath);
		buttonLocalPath.setEnabled(true);

		final Switch switchSync = (Switch) findViewById(R.id.switchSync);
		switchSync.setEnabled(true);

		if (driveFolderId != null) {

			DriveFolder driveFolder = Drive.DriveApi.getFolder(mGoogleApiClient, driveFolderId);
			driveFolder.getMetadata(mGoogleApiClient).setResultCallback(
					new ResultCallback<DriveResource.MetadataResult>() {

						@Override
						public void onResult(DriveResource.MetadataResult metaData) {
							showMessage("Selected folder title is: " + metaData.getMetadata().getTitle());

						}

					});
		}


	}

	@Override
	public void onConnectionSuspended(int arg0) {
		final Button buttonDrivePath = (Button) findViewById(R.id.buttonDrivePath);
		buttonDrivePath.setEnabled(false);

		final Button buttonLocalPath = (Button) findViewById(R.id.buttonLocalPath);
		buttonLocalPath.setEnabled(false);

		final Switch switchSync = (Switch) findViewById(R.id.switchSync);
		switchSync.setEnabled(false);

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Called if connection failed

		final Button buttonDrivePath = (Button) findViewById(R.id.buttonDrivePath);
		buttonDrivePath.setEnabled(false);

		final Button buttonLocalPath = (Button) findViewById(R.id.buttonLocalPath);
		buttonLocalPath.setEnabled(false);

		final Switch switchSync = (Switch) findViewById(R.id.switchSync);
		switchSync.setEnabled(false);

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

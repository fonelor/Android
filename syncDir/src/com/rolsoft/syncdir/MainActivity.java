package com.rolsoft.syncdir;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends Activity implements ConnectionCallbacks,
	OnConnectionFailedListener {
	
	private static final String TAG = "syncDir-MainActivity";
	private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
	private static final int REQUEST_CODE_CREATOR = 2;
	private static final int REQUEST_CODE_RESOLUTION = 3;
	
	private GoogleApiClient mGoogleApiClient;
	private boolean bSyncOn = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final Button buttonLocalPath = (Button) findViewById(R.id.buttonLocalPath);
		buttonLocalPath.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		final Button buttonDrivePath = (Button) findViewById(R.id.buttonDrivePath);
		buttonDrivePath.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
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
			case REQUEST_CODE_CAPTURE_IMAGE:
				break;
			case REQUEST_CODE_RESOLUTION:
				if (resultCode == RESULT_OK) {
					mGoogleApiClient.connect();
				}
				break;			
		}
		
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
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
		final Button buttonDrivePath = (Button) findViewById(R.id.buttonDrivePath);
		buttonDrivePath.setEnabled(false);
		
		final Button buttonLocalPath = (Button) findViewById(R.id.buttonLocalPath);
		buttonLocalPath.setEnabled(false);
		
		final Switch switchSync = (Switch) findViewById(R.id.switchSync);
		switchSync.setEnabled(false);
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		final Button buttonDrivePath = (Button) findViewById(R.id.buttonDrivePath);
		buttonDrivePath.setEnabled(false);
		
		final Button buttonLocalPath = (Button) findViewById(R.id.buttonLocalPath);
		buttonLocalPath.setEnabled(false);
		
		final Switch switchSync = (Switch) findViewById(R.id.switchSync);
		switchSync.setEnabled(false);
		
		// Called if connection failed
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

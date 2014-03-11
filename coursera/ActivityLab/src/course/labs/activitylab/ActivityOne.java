package course.labs.activitylab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ActivityOne extends Activity {

	private static final String RESTART_KEY = "restart";
	private static final String RESUME_KEY = "resume";
	private static final String START_KEY = "start";
	private static final String CREATE_KEY = "create";

	// String for LogCat documentation
	private final static String TAG = "Lab-ActivityOne";
	
	// Lifecycle counters

	private int mCreate, mRestart, mResume, mStart;
	
	// TODO:
	// Create counter variables for onCreate(), onRestart(), onStart() and
	// onResume(), called mCreate, etc.
	// You will need to increment these variables' values when their
	// corresponding lifecycle methods get called
	
	private TextView mTvCreate, mTvRestart, mTvResume, mTvStart;
	
	// TODO: Create variables for each of the TextViews, called
        // mTvCreate, etc. 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_one);
		
		// TODO: Assign the appropriate TextViews to the TextView variables
		// Hint: Access the TextView by calling Activity's findViewById()
		// textView1 = (TextView) findViewById(R.id.textView1);

		mTvCreate = (TextView) findViewById(R.id.create);
		mTvRestart = (TextView) findViewById(R.id.restart);
		mTvResume = (TextView) findViewById(R.id.resume);
		mTvStart = (TextView) findViewById(R.id.start);

		
		Button launchActivityTwoButton = (Button) findViewById(R.id.bLaunchActivityTwo); 
		launchActivityTwoButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO:
				// Launch Activity Two
				// Hint: use Context's startActivity() method

				// Create an intent stating which Activity you would like to start

				Intent act_2 = new Intent(getApplicationContext(), ActivityTwo.class);
				
				// Launch the Activity using the intent
				
				startActivity(act_2);
			
			}
		});
		
		// Check for previously saved state
		if (savedInstanceState != null) {

			// TODO:
			// Restore value of counters from saved state
			// Only need 4 lines of code, one for every count variable
			mCreate = savedInstanceState.getInt(CREATE_KEY);
			mRestart = savedInstanceState.getInt(RESTART_KEY);
			mStart = savedInstanceState.getInt(START_KEY);
			mResume = savedInstanceState.getInt(RESUME_KEY);
		}

		// TODO: Emit LogCat message
		Log.i(TAG, "onCreate executed");

		// TODO:
		mCreate++; // Update the appropriate count variable
		displayCounts(); // Update the user interface via the displayCounts() method
	}

	// Lifecycle callback overrides

	@Override
	public void onStart() {
		super.onStart();

		Log.i(TAG, "onStart executed");// TODO: Emit LogCat message
		
		// TODO:
		mStart++;// Update the appropriate count variable
		displayCounts();// Update the user interface


	}

	@Override
	public void onResume() {
		super.onResume();

		Log.i(TAG, "onResume executed");// TODO: Emit LogCat message


		// TODO:
		mResume++;// Update the appropriate count variable
		displayCounts();// Update the user interface


	}

	@Override
	public void onPause() {
		super.onPause();

		Log.i(TAG, "onPause executed");// TODO: Emit LogCat message

	}

	@Override
	public void onStop() {
		super.onStop();

		Log.i(TAG, "onStop executed");// TODO: Emit LogCat message

	}

	@Override
	public void onRestart() {
		super.onRestart();

		Log.i(TAG, "onRestart executed");// TODO: Emit LogCat message


		// TODO:
		mRestart++;// Update the appropriate count variable
		displayCounts();// Update the user interface



	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.i(TAG, "onDestroy executed");// TODO: Emit LogCat message


	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// TODO:
		// Save state information with a collection of key-value pairs
		// 4 lines of code, one for every count variable
		savedInstanceState.putInt(CREATE_KEY, mCreate);
		savedInstanceState.putInt(RESTART_KEY, mRestart);
		savedInstanceState.putInt(RESUME_KEY, mResume);
		savedInstanceState.putInt(START_KEY, mStart);





	}
	
	// Updates the displayed counters
	public void displayCounts() {

		mTvCreate.setText("onCreate() calls: " + mCreate);
		mTvStart.setText("onStart() calls: " + mStart);
		mTvResume.setText("onResume() calls: " + mResume);
		mTvRestart.setText("onRestart() calls: " + mRestart);
	
	}
}

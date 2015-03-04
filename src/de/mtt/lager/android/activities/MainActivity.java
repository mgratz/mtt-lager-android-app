package de.mtt.lager.android.activities;


import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

import de.mtt.lager.android.R;
import de.mtt.lager.android.app.LagerApp;
import de.mtt.lager.android.camera.CameraManager;
import de.mtt.lager.android.decodes.DecodeFormatManager;
import de.mtt.lager.android.decodes.DecodeHintManager;
import de.mtt.lager.android.decodes.Intents;
import de.mtt.lager.android.fragments.NavigationDrawerFragment;
import de.mtt.lager.android.fragments.ResultDialogFragment;
import de.mtt.lager.android.utils.Constants;
import de.mtt.lager.android.views.ViewfinderView;


public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, SurfaceHolder.Callback {

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		/**
		 * The fragment argument representing the section number for this fragment.
		 */
		private static final String	ARG_SECTION_NUMBER	= "section_number";


		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}


		public PlaceholderFragment() {
		}


		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
		}


		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}
	}

	private static final String	TAG = MainActivity.class.getSimpleName();


	private CameraManager	cameraManager;


	private String characterSet;


	private Collection<BarcodeFormat> decodeFormats;


	private Map<DecodeHintType,?> decodeHints;


	private CaptureHandler	handler;


	private boolean	hasSurface;


	private Result	lastResult;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private NavigationDrawerFragment	mNavigationDrawerFragment;
	/**
	 * Used to store the last screen title. For use in {@link #restoreActionBar()}.
	 */
	private CharSequence				mTitle;

	private IntentSource	source;


	private TextView	statusView;


	private ViewfinderView	viewfinderView;


	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	private int getCurrentOrientation() {
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_90:
			return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		default:
			return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
		}
	}

	public Handler getHandler() {
		return handler;
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}
	/**
	 * A valid barcode has been found, so give an indication of success and show the results.
	 *
	 * @param rawResult The contents of the barcode.
	 * @param scaleFactor amount by which thumbnail was scaled
	 * @param barcode   A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		if(rawResult.getText()!=null){
			LagerApp.getModule().getBackend().getArticleById(rawResult.getText());
			DialogFragment fragment = (DialogFragment) getFragmentManager().findFragmentByTag("Dialog");
			if(fragment!=null){
				if(fragment.isVisible()){
					fragment.dismiss();
					fragment=null;
				}

			}
			fragment = ResultDialogFragment.newInstance(rawResult.getText());
			fragment.show(getFragmentManager(), "Dialog");
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					resetStatusView();
					reset();

				}
			}, 500);
		}
	}
	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a RuntimeException.
			if (handler == null) {
				handler = new CaptureHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
			}
			//decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(
				R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}




	@Override
	public void onNavigationDrawerItemSelected(int position) {
		getFragmentManager();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onPause() {

		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
		// want to open the camera driver and measure the screen size if we're going to show the help on
		// first launch. That led to bugs where the scanning rectangle was the wrong size and partially
		// off screen.
		cameraManager = new CameraManager(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		statusView = (TextView) findViewById(R.id.status_view);

		decodeFormats = null;
		characterSet = null;

		handler = null;
		lastResult = null;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (prefs.getBoolean(Constants.KEY_DISABLE_AUTO_ORIENTATION, true)) {
			setRequestedOrientation(getCurrentOrientation());
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}

		resetStatusView();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the camera.
			surfaceHolder.addCallback(this);
		}

		Intent intent = getIntent();

		source = IntentSource.NONE;
		decodeFormats = null;
		characterSet = null;

		if (intent != null) {

			String action = intent.getAction();
			intent.getDataString();

			if (Intents.Scan.ACTION.equals(action)) {

				// Scan the formats the intent requested, and return the result to the calling activity.
				source = IntentSource.NATIVE_APP_INTENT;
				decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
				decodeHints = DecodeHintManager.parseDecodeHints(intent);

				if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
					int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
					int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
					if (width > 0 && height > 0) {
						cameraManager.setManualFramingRect(width, height);
					}
				}

				if (intent.hasExtra(Intents.Scan.CAMERA_ID)) {
					int cameraId = intent.getIntExtra(Intents.Scan.CAMERA_ID, -1);
					if (cameraId >= 0) {
						cameraManager.setManualCameraId(cameraId);
					}
				}

				String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
				if (customPromptMessage != null) {
					statusView.setText(customPromptMessage);
				}

			} /*else if (dataString != null &&
					dataString.contains("http://www.google") &&
					dataString.contains("/m/products/scan")) {

				// Scan only products and send the result to mobile Product Search.
				source = IntentSource.PRODUCT_SEARCH_LINK;
				//sourceUrl = dataString;
				decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;

			}*/ /*else if (isZXingURL(dataString)) {

				// Scan formats requested in query string (all formats if none specified).
				// If a return URL is specified, send the results there. Otherwise, handle it ourselves.
				source = IntentSource.ZXING_LINK;
				sourceUrl = dataString;
				Uri inputUri = Uri.parse(dataString);
				scanFromWebPageManager = new ScanFromWebPageManager(inputUri);
				decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri);
				// Allow a sub-set of the hints to be specified by the caller.
				decodeHints = DecodeHintManager.parseDecodeHints(inputUri);

			}*/

			characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

		}
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		}
	}
	private void reset(){
		onPause();
		onResume();
	}
	public void resetStatusView() {
		statusView.setText(R.string.msg_default_status);
		statusView.setVisibility(View.VISIBLE);
		viewfinderView.setVisibility(View.VISIBLE);
		lastResult = null;
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}



	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (holder == null) {
			Log.e(TAG , "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		hasSurface = false;
	}

}

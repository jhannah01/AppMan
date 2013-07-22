package com.blueodin.appman.fragments;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.blueodin.appman.R;
import com.blueodin.appman.content.AppListLoader.AppEntry;

public class AppDetailFragment extends SherlockFragment {
	private static final String TAG = "AppDetailFragment";
	
	public static final String ARG_APP_PACKAGE_NAME = "arg_app_package_name";

	private AppEntry mAppEntry = null;
	
	public AppDetailFragment() { }
	
	public static AppDetailFragment newInstance(AppEntry appEntry) {
		if(appEntry == null)
			return new AppDetailFragment();
		
		return newInstance(appEntry.getPackageName());
	}
	
	public static AppDetailFragment newInstance(String packageName) {
		AppDetailFragment f = new AppDetailFragment();
		
		Bundle args = new Bundle();
		args.putString(ARG_APP_PACKAGE_NAME, packageName);
		f.setArguments(args);
		
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if((savedInstanceState != null) && savedInstanceState.containsKey(ARG_APP_PACKAGE_NAME))
			loadAppEntry(savedInstanceState.getString(ARG_APP_PACKAGE_NAME));
		else {
			Bundle args = getArguments();
			if((args != null) && args.containsKey(ARG_APP_PACKAGE_NAME))
				loadAppEntry(args.getString(ARG_APP_PACKAGE_NAME));
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(mAppEntry == null) {
			TextView textEmpty = new TextView(getActivity());
			textEmpty.setText("No application selected.");
			textEmpty.setGravity(Gravity.CENTER);
			return textEmpty;
		}
		
		View view = inflater.inflate(R.layout.fragment_app_detail, container, false);
		
		((ImageView)view.findViewById(R.id.image_app_detail_icon)).setImageDrawable(mAppEntry.getIcon());
		((TextView)view.findViewById(R.id.text_app_detail_label)).setText(mAppEntry.getLabel());
		((TextView)view.findViewById(R.id.text_app_detail_package)).setText(mAppEntry.getPackageName());
		((TextView)view.findViewById(R.id.text_app_detail_is_system_app)).setText(mAppEntry.isSystemApp() ? "Yes" : "No");
		((TextView)view.findViewById(R.id.text_app_detail_is_enabled)).setText(mAppEntry.isEnabled() ? "Yes" : "No");
		
		Button uninstallButton = (Button)view.findViewById(R.id.button_app_detail_uninstall);
		Button launchButton = (Button)view.findViewById(R.id.button_app_detail_launch);
		
		uninstallButton.setEnabled(!mAppEntry.isSystemApp());
		launchButton.setEnabled(mAppEntry.isEnabled());
		
		uninstallButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getActivity(), String.format("Uninstall request for %s", mAppEntry.toString()), Toast.LENGTH_SHORT).show();
			}
		});
		
		launchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(mAppEntry.getLaunchIntent());
			}
		});
		
		return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if(mAppEntry != null)
			outState.putString(ARG_APP_PACKAGE_NAME, mAppEntry.getPackageName());
	}

	private void loadAppEntry(String packageName) {
		PackageManager packageManager = getActivity().getPackageManager();
		
		try {
			mAppEntry = new AppEntry(packageManager, packageManager.getApplicationInfo(packageName, 0));
		} catch(NameNotFoundException ex) {
			ex.printStackTrace();
			Log.w(TAG, String.format("Unable to find specified application package: %s", packageName));
			mAppEntry = null;
		}
	}
}

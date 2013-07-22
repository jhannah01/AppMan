package com.blueodin.appman.content;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;
import com.activeandroid.ActiveAndroid;
import java.util.ArrayList;
import java.util.List;

public class UpdateAppDbTask extends AsyncTask<Void, Void, List<Application>> {
	private static final String TAG = "UpdateAppDbTask";
	
	private PackageManager mPackageManager;
	
	public UpdateAppDbTask(PackageManager packageManager) {
		super();
		mPackageManager = packageManager;
	}
	
	@Override
	protected List<Application> doInBackground(Void... params) {
		List<Application> applications = new ArrayList<Application>();

		long ts = System.currentTimeMillis();
		
		try {
			Log.d(TAG, "Beginning application update transaction.");
			
			ActiveAndroid.beginTransaction();
			
			for(ApplicationInfo appInfo : mPackageManager.getInstalledApplications(0)) {
				String label = appInfo.loadLabel(mPackageManager).toString();
				Application app = new Application(appInfo.packageName, label, appInfo.sourceDir.startsWith("/system"));
				app.save();
				applications.add(app);
			}

			ActiveAndroid.setTransactionSuccessful();
		} finally {
			ActiveAndroid.endTransaction();
			Log.d(TAG, String.format("Application update transaction complete. [Took %d seconds]", (System.currentTimeMillis() - ts) / 1000));
		}
		
		return applications;
	}
}
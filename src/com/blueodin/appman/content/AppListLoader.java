package com.blueodin.appman.content;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppListLoader extends AsyncTaskLoader<List<AppListLoader.AppEntry>> {
	private static final String TAG = "AppListLoader";
	
	private final PackageManager mPackageManager;
	private final boolean mUseDatabase;
	
	private List<AppEntry> mApps;
	private PackageIntentReceiver mPackageObserver;

	public AppListLoader(Context context, boolean useDatabase) {
		super(context);		
		mPackageManager = context.getPackageManager();
		mUseDatabase = useDatabase;
	}

	@Override
	public List<AppEntry> loadInBackground() {
		List<AppEntry> entries = new ArrayList<AppListLoader.AppEntry>();
		
		if(!mUseDatabase) {
			Log.d(TAG, "Building application list from live information");
			for(ApplicationInfo appInfo : mPackageManager.getInstalledApplications(0))
				entries.add(new AppEntry(mPackageManager, appInfo));
		} else {
			Log.d(TAG, "Building application list from stored information");
			
			List<Application> dbApps = new Select()
				.from(Application.class)
				.orderBy("label DESC")
				.execute();
			
			if(dbApps.size() == 0) {
				Log.i(TAG, "Couldn't find any applications in the database. Using live information and persisting.");
				
				for(ApplicationInfo appInfo : mPackageManager.getInstalledApplications(0))
					entries.add(new AppEntry(mPackageManager, appInfo));
				
				try {
					Log.d(TAG, "Beginning package update transaction");
					ActiveAndroid.beginTransaction();
					
					for(AppEntry entry : entries)
						new Application(entry.getPackageName(), entry.getLabel(), entry.isSystemApp()).save();
					
					ActiveAndroid.setTransactionSuccessful();
				} finally {
					ActiveAndroid.endTransaction();
					Log.d(TAG, String.format("Package update transaction is complete. Stored %d packages.", entries.size()));
				}
			} else {
				Log.d(TAG, String.format("Found %d applications", dbApps.size()));
				
				for(Application app : dbApps) {
					try {
						ApplicationInfo appInfo = mPackageManager.getApplicationInfo(app.packageName, 0);
						entries.add(new AppEntry(mPackageManager, appInfo));
					} catch(NameNotFoundException ex) {
						ex.printStackTrace();
						Log.i(TAG, String.format("Could not find application '%s' despite the fact it was stored in the database. Assuming it is uninstalled and removing.", app.packageName));
						new Delete()
							.from(Application.class)
							.where("packageName = ?", app.packageName)
							.execute();
					}
				}
			}
		}
		
		Collections.sort(entries, ALPHA_COMPARATOR);
		
		Log.d(TAG, "Entries have been loaded.");
		
		return entries;
	}
	
	@Override
	public void deliverResult(List<AppEntry> data) {
		if(isReset())
			return;
		
		mApps = data;
		
		if(isStarted())
			super.deliverResult(data);
	}
	
	@Override
	protected void onStartLoading() {
		if(mApps != null)
			deliverResult(mApps);
		
		if(mPackageObserver == null)
			mPackageObserver = new PackageIntentReceiver(this);

		if (takeContentChanged() || (mApps == null))
			forceLoad();
	}
	
	@Override
	protected void onStopLoading() {
		cancelLoad();
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		
		onStopLoading();
		
		if(mApps != null)
			mApps = null;
		
		if(mPackageObserver != null) {
			getContext().unregisterReceiver(mPackageObserver);
			mPackageObserver = null;
		}
	}
	
	public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
	    private final Collator sCollator = Collator.getInstance();
	    
	    @Override
	    public int compare(AppEntry lhs, AppEntry rhs) {
	        return sCollator.compare(lhs.getLabel(), rhs.getLabel());
	    }
	};
	
	public static class AppEntry {
		private final ApplicationInfo mAppInfo;
		private final String mLabel;
		private final Drawable mIcon;
		private final Intent mLaunchIntent;
		
		public AppEntry(PackageManager packageManager, ApplicationInfo appInfo) {
			mAppInfo = appInfo;
			mLabel = mAppInfo.loadLabel(packageManager).toString();
			mIcon = packageManager.getApplicationIcon(mAppInfo);
			mLaunchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName);
		}
		
		public String getLabel() {
			return mLabel;
		}
		
		public Drawable getIcon() {
			return mIcon;
		}
		
		public String getPackageName() {
			return mAppInfo.packageName;
		}
		
		public ApplicationInfo getAppInfo() {
			return mAppInfo;
		}
		
		public Intent getLaunchIntent() {
			return mLaunchIntent;
		}
		
		public boolean isSystemApp() {
			return mAppInfo.sourceDir.startsWith("/system");
		}
		
		public boolean isEnabled() {
			return mAppInfo.enabled;
		}
		
		@Override
		public String toString() {
			return String.format("%s [%s]", getLabel(), getPackageName());
		}
	}
}
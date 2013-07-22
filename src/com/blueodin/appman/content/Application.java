package com.blueodin.appman.content;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Column.ConflictAction;

import java.util.ArrayList;
import java.util.List;

public class Application extends Model implements Parcelable, Comparable<Application> {
	private static final String TAG = "Application";

	@Column(name="packageName", unique=true, onUniqueConflict=ConflictAction.REPLACE)
	public String packageName;
	
	@Column(name="label")
	public String label;
	
	@Column(name="isSystemApp")
	public boolean isSystemApp;
	
	private Drawable mIcon;
	
	public Application() {
		super();
	}
	
	public Application(String packageName, String label, boolean isSystemApp) {
		this();
		this.packageName = packageName;
		this.label = label;
		this.isSystemApp = isSystemApp;
	}
	
	private Application(Parcel source) {
		this(source.readString(), source.readString(), (source.readInt() == 0) ? false : true);
	}
	
	public Drawable getIcon(PackageManager packageManager) {
		if(mIcon == null) {
			try {
				mIcon = packageManager.getApplicationIcon(packageName);
			} catch (NameNotFoundException ex) {
				ex.printStackTrace();
				Log.w(TAG, String.format("Couldn't find application icon for %s (%s)", label, packageName));
			}
		}
		
		return mIcon;
	}
	
	public static Parcelable.Creator<Application> CREATOR = new Parcelable.Creator<Application>() {
		@Override
		public Application[] newArray(int size) {
			return new Application[size];
		}
		
		@Override
		public Application createFromParcel(Parcel source) {
			return new Application(source);
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(packageName);
		dest.writeString(label);
		dest.writeInt(isSystemApp ? 1 : 0);
	}

	@Override
	public int compareTo(Application another) {
		return this.label.compareTo(another.label);
	}
	
	public static class UpdateTask extends AsyncTask<Void, Void, List<Application>> {
		private static final String TAG = "Application.UpdateTask";
		
		private final PackageManager mPackageManager;
		private OnUpdateComplete mCallback;
		
		public interface OnUpdateComplete {
			public void onUpdateComplete(List<Application> results);	
		}
		
		public UpdateTask(Context context) {
			this(context, null);
		}
		
		public UpdateTask(Context context, OnUpdateComplete callback) {
			super();
			mPackageManager = context.getPackageManager();
			mCallback = callback;
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
		
		@Override
		protected void onPostExecute(List<Application> results) {
			if(mCallback != null)
				mCallback.onUpdateComplete(results);
		}
	}
}

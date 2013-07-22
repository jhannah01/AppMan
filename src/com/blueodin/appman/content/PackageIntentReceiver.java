package com.blueodin.appman.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class PackageIntentReceiver extends BroadcastReceiver {
	private final AppListLoader mLoader;
	
	public PackageIntentReceiver(AppListLoader loader) {
		mLoader = loader;
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        
        mLoader.getContext().registerReceiver(this, filter);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mLoader.onContentChanged();
	}
}
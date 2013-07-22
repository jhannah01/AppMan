package com.blueodin.appman;

import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.blueodin.appman.content.AppListLoader.AppEntry;
import com.blueodin.appman.content.Application;
import com.blueodin.appman.content.Application.UpdateTask.OnUpdateComplete;
import com.blueodin.appman.fragments.AppDetailFragment;
import com.blueodin.appman.fragments.AppListFragment;
import com.blueodin.appman.fragments.AppListFragment.OnAppListInteraction;

import java.util.List;

public class MainActivity extends SherlockFragmentActivity implements OnAppListInteraction {
	private AppListFragment mAppListFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mAppListFragment = new AppListFragment();
		
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.frame_main_list, mAppListFragment)
			.replace(R.id.frame_main_content, new AppDetailFragment())
			.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSherlock().getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_refresh_db:
			updateAppDatabase();
			return true;
		case R.id.action_settings:
			// TODO: Add Settings
			return true;
		case R.id.action_exit:
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	private void updateAppDatabase() {
		final long ts = System.currentTimeMillis();
		
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.frame_main_content, new AppDetailFragment())
			.commit();
		
		(new Application.UpdateTask(this, new OnUpdateComplete() {
			@Override
			public void onUpdateComplete(List<Application> results) {
				mAppListFragment.refresh();
				Toast.makeText(MainActivity.this, String.format("Updated the database with %d applications in %d seconds", results.size(), (System.currentTimeMillis() - ts)/1000), Toast.LENGTH_SHORT).show();
			}
		})).execute();
	}

	@Override
	public void onAppSelected(AppEntry appEntry) {
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.frame_main_content, AppDetailFragment.newInstance(appEntry))
			.commit();
	}
}

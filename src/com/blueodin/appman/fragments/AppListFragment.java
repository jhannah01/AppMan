package com.blueodin.appman.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.blueodin.appman.R;
import com.blueodin.appman.content.AppListLoader;
import com.blueodin.appman.content.AppListLoader.AppEntry;

import java.util.List;

public class AppListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<AppListLoader.AppEntry>> {
	private static final int LOADER_ID = 0;
	
	private OnAppListInteraction mCallback = null;
	private AppListAdapter mListAdapter;
	
	public AppListFragment() { }

	public interface OnAppListInteraction {
		public void onAppSelected(AppListLoader.AppEntry appEntry);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if(activity instanceof OnAppListInteraction)
			mCallback = ((OnAppListInteraction) activity);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setEmptyText("No Applications Found...");

		mListAdapter = new AppListAdapter(getActivity());
		setListAdapter(mListAdapter);

		setListShown(false);

		getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mCallback == null)
			return;

		AppEntry appEntry = mListAdapter.getItem(position);

		mCallback.onAppSelected(appEntry);
	}

	@Override
	public Loader<List<AppListLoader.AppEntry>> onCreateLoader(int id, Bundle args) {
		return new AppListLoader(getActivity(), true);
	}

	@Override
	public void onLoadFinished(Loader<List<AppListLoader.AppEntry>> loader,
			List<AppListLoader.AppEntry> data) {
		mListAdapter.setData(data);
		
		if (isResumed())
			setListShown(true);
		else
			setListShownNoAnimation(true);
	}

	@Override
	public void onLoaderReset(Loader<List<AppListLoader.AppEntry>> loader) {
		mListAdapter.setData(null);
	}

	public void refresh() {
		getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
	}
	
	public static class AppListAdapter extends ArrayAdapter<AppEntry> {
		private final LayoutInflater mInflater;

		public AppListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_2);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setData(List<AppEntry> entries) {
			clear();
			
			if(entries != null)
				addAll(entries);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView;
			AppEntry entry = getItem(position);
			
			if (convertView == null)
				rowView = mInflater.inflate(R.layout.app_list_row, parent, false);
			else
				rowView = convertView;
			
			((ImageView) rowView.findViewById(R.id.image_app_icon)).setImageDrawable(entry.getIcon());
			((TextView) rowView.findViewById(R.id.text_app_label)).setText(entry.getLabel());
			((TextView) rowView.findViewById(R.id.text_app_package)).setText(entry.getPackageName());

			return rowView;
		}
	}
}
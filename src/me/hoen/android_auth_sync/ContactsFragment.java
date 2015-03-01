package me.hoen.android_auth_sync;

import java.util.ArrayList;

import me.hoen.android_auth_sync.db.Contact;
import me.hoen.android_auth_sync.db.ContactDataSource;
import me.hoen.android_auth_sync.sync.SyncAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

public class ContactsFragment extends Fragment {
	protected ContactAdapter adapter;

	protected ProgressBar progress;

	private BroadcastReceiver syncStartReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(MainActivity.TAG, "start sync broadcast received");
			progress.setVisibility(View.VISIBLE);
		}
	};

	private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(MainActivity.TAG, "end sync broadcast received");
			progress.setVisibility(View.GONE);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_contacts, container,
				false);

		progress = (ProgressBar) rootView.findViewById(R.id.progress);

		ArrayList<Contact> list = new ArrayList<Contact>();
		ListView listLv = (ListView) rootView.findViewById(R.id.contacts);
		adapter = new ContactAdapter(getActivity(), R.layout.item_contact, list);
		listLv.setAdapter(adapter);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivity().registerReceiver(syncStartReceiver,
				new IntentFilter(SyncAdapter.SYNC_START));
		getActivity().registerReceiver(syncFinishedReceiver,
				new IntentFilter(SyncAdapter.SYNC_END));

		loadData();
	}

	@Override
	public void onPause() {
		super.onPause();

		getActivity().unregisterReceiver(syncStartReceiver);
		getActivity().unregisterReceiver(syncFinishedReceiver);
	}

	protected void loadData() {
		ContactDataSource eds = new ContactDataSource(getActivity());
		ArrayList<Contact> list = eds.getContacts();
		eds.close();

		adapter.clear();
		adapter.addAll(list);
		adapter.notifyDataSetChanged();
	}
}

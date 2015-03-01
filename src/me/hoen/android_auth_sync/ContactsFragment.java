package me.hoen.android_auth_sync;

import java.util.ArrayList;

import me.hoen.android_auth_sync.auth.AccountGeneral;
import me.hoen.android_auth_sync.db.Contact;
import me.hoen.android_auth_sync.db.ContactDataSource;
import me.hoen.android_auth_sync.sync.LoadContactsTask;
import me.hoen.android_auth_sync.sync.SyncAdapter;
import me.hoen.android_auth_sync.sync.SyncTaskCompleted;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ContactsFragment extends Fragment {
	protected ContactAdapter adapter;
	protected SwipeRefreshLayout swipeRefreshLayout;

	private BroadcastReceiver syncStartReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(MainActivity.TAG, "start sync broadcast received");
			swipeRefreshLayout.setRefreshing(true);
		}
	};

	private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(MainActivity.TAG, "end sync broadcast received");
			swipeRefreshLayout.setRefreshing(false);
			loadData();
		}
	};

	private SyncTaskCompleted syncCallback = new SyncTaskCompleted() {

		@Override
		public void onSyncCompleted() {
			swipeRefreshLayout.setRefreshing(false);
			loadData();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_contacts, container,
				false);

		swipeRefreshLayout = (SwipeRefreshLayout) rootView
				.findViewById(R.id.swipe_container);
		swipeRefreshLayout.setOnRefreshListener(getSyncListener());
		swipeRefreshLayout.setColorSchemeResources(R.color.holo_blue_bright,
				R.color.holo_green_light, R.color.holo_orange_light,
				R.color.holo_red_light);

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

	protected SwipeRefreshLayout.OnRefreshListener getSyncListener() {
		return new SwipeRefreshLayout.OnRefreshListener() {

			@Override
			public void onRefresh() {
				new LoadContactsTask(getActivity(), syncCallback).execute("");
			}
		};
	}

	protected void loadData() {
		ContactDataSource eds = new ContactDataSource(getActivity());
		ArrayList<Contact> list = eds.getContacts();
		eds.close();

		adapter.clear();
		adapter.addAll(list);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.contacts, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.profile:
			Log.d(MainActivity.TAG, "profile");
			Fragment f = new ProfileFragment();

			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(android.R.id.content, f, "profile").commit();
			// seems to prevent fragment to be sometimes displayed one over the
			// other.
			fragmentManager.executePendingTransactions();
			return true;

		case R.id.sync:
			swipeRefreshLayout.setRefreshing(true);
			new LoadContactsTask(getActivity(), syncCallback).execute("");
			return true;

		case R.id.logout:
			new AsyncTask<String, Void, Void>() {

				@Override
				protected Void doInBackground(String... params) {
					try {
						// empty contact table, otherwise, there will still be
						// previous contacts
						ContactDataSource cds = new ContactDataSource(
								getActivity());
						cds.emptyTable();
						cds.close();

						AccountManager am = AccountManager.get(getActivity());
						Account[] accounts = am
								.getAccountsByType(getString(R.string.account_type));

						// invalidating token
						String authToken = am
								.blockingGetAuthToken(
										accounts[0],
										AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,
										true);
						am.invalidateAuthToken(
								getString(R.string.account_type), authToken);

						// deleting account from account manager
						am.removeAccount(accounts[0], null, null);
						am.clearPassword(accounts[0]);

						getActivity().finish();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			}.execute("");

			return true;

		}

		return super.onOptionsItemSelected(item);
	}
}

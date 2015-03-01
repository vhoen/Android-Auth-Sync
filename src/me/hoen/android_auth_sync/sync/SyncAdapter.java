package me.hoen.android_auth_sync.sync;

import me.hoen.android_auth_sync.MainActivity;
import me.hoen.android_auth_sync.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
	static public String SYNC_START = "me.hoen.android_auth_sync.sync.start";
	static public String SYNC_END = "me.hoen.android_auth_sync.sync.end";

	private final AccountManager am;
	protected Context context;

	protected SyncTaskCompleted callback = new SyncTaskCompleted() {

		@Override
		public void onSyncCompleted() {
			Intent i = new Intent(SYNC_END);
			context.sendBroadcast(i);
			
			Log.d(MainActivity.TAG, "send end sync broadcast");
		}
	};

	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);

		this.am = AccountManager.get(context);
		this.context = context;
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		try {
			Intent i = new Intent(SYNC_START);
			context.sendBroadcast(i);

			String authToken = am.blockingGetAuthToken(account,
					context.getString(R.string.auth_token_type), true);

			Log.d(MainActivity.TAG, "Sync auth token : " + authToken);

			new LoadContactsTask(getContext(), callback).execute(authToken);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

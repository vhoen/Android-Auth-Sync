package me.hoen.android_auth_sync.sync;

import me.hoen.android_auth_sync.MainActivity;
import me.hoen.android_auth_sync.auth.Authenticator;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
	private final AccountManager am;
	protected Context context;

	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		
		this.am = AccountManager.get(context);
		this.context = context;
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		try {
			String authToken = am.blockingGetAuthToken(account,
					Authenticator.AUTHTOKEN_TYPE, true);
			
			Log.d(MainActivity.TAG, "Sync auth token : " + authToken);
			
			new LoadContactsTask(getContext()).execute(authToken);
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

}

package me.hoen.android_auth_sync.sync;

import me.hoen.android_auth_sync.R;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

public class Utils {
	static public void requestSync(Account account, Context context){
		final Bundle requestSyncExtras = new Bundle(1);
		requestSyncExtras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED,
				true);
		requestSyncExtras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(account, context.getString(R.string.sync_provider),
				requestSyncExtras);
	}
}

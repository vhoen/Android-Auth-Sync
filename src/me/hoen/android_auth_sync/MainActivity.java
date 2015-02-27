package me.hoen.android_auth_sync;

import me.hoen.android_auth_sync.auth.LoginActivity;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class MainActivity extends ActionBarActivity {
	public static final String TAG = "me.example";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Fragment f = new ContactsFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(android.R.id.content, f, "home").commit();
		fragmentManager.executePendingTransactions();

	}

	@Override
	protected void onResume() {
		super.onResume();

		AccountManager am = AccountManager.get(this);

		String accountType = LoginActivity.ARG_ACCOUNT_TYPE;
		String authTokenType = LoginActivity.ARG_AUTH_TYPE;

		am.getAuthTokenByFeatures(accountType, authTokenType, null, this, null,
				null, new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> future) {
						try {
							Bundle bnd = null;
							bnd = future.getResult();
							String keyAuthToken = AccountManager.KEY_AUTHTOKEN;
							String authToken = bnd.getString(keyAuthToken);

							Log.d(MainActivity.TAG, "AuthToken : " + authToken);
						} catch (OperationCanceledException e) {
							finish();
							e.printStackTrace();
						} catch (AuthenticatorException e) {
							finish();
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, null);
	}

}

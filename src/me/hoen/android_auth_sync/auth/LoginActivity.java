package me.hoen.android_auth_sync.auth;

import me.hoen.android_auth_sync.AuthOnTaskCompleted;
import me.hoen.android_auth_sync.R;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends AccountAuthenticatorActivity {
	public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
	public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
	public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
	public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";
	public static final String KEY_ERROR_MESSAGE = "ERR_MSG";
	public final static String PARAM_USER_PASS = "USER_PASS";
	private final int REQ_SIGNUP = 1;
	private AccountManager mAccountManager;
	private String mAuthTokenType;

	protected ProgressDialog progress;

	protected AuthOnTaskCompleted loginCallback = new AuthOnTaskCompleted() {

		@Override
		public void onTaskCompleted(Intent intent) {
			finishLogin(intent);

			if (progress.isShowing()) {
				progress.hide();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		progress = new ProgressDialog(this);

		mAccountManager = AccountManager.get(getBaseContext());

		mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
		if (mAuthTokenType == null)
			mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;

		Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
		mEmailSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				submit();
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
			finishLogin(data);
		} else
			super.onActivityResult(requestCode, resultCode, data);
	}

	public void submit() {
		final String username = ((TextView) findViewById(R.id.email)).getText()
				.toString();
		final String userpass = ((TextView) findViewById(R.id.password))
				.getText().toString();
		final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

		progress.show();
		new AuthenticationTask(getApplicationContext(), loginCallback).execute(
				username, userpass, accountType);
	}

	private void finishLogin(Intent intent) {
		String accountName = intent
				.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
		String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
		final Account account = new Account(accountName,
				intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

		ContentResolver.setIsSyncable(account,
				getString(R.string.sync_provider), 1);
		ContentResolver.setSyncAutomatically(account,
				getString(R.string.sync_provider), true);

		final Bundle requestSyncExtras = new Bundle(1);
		requestSyncExtras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED,
				true);
		requestSyncExtras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(account, getString(R.string.sync_provider),
				requestSyncExtras);

		if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
			String authtoken = intent
					.getStringExtra(AccountManager.KEY_AUTHTOKEN);
			String authtokenType = mAuthTokenType;

			mAccountManager
					.addAccountExplicitly(account, accountPassword, null);

			mAccountManager.setAuthToken(account, authtokenType, authtoken);
		} else {
			mAccountManager.setPassword(account, accountPassword);
		}
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();
	}
}

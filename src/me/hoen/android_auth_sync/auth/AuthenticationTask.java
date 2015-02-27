package me.hoen.android_auth_sync.auth;

import me.hoen.android_auth_sync.AuthOnTaskCompleted;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

public class AuthenticationTask extends AsyncTask<String, Void, Intent> {
	protected Context context;
	protected AuthOnTaskCompleted callback;

	public AuthenticationTask(Context context, AuthOnTaskCompleted callback) {
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected Intent doInBackground(String... params) {
		String authToken = null;
		Bundle data = new Bundle();

		try {
			String username = params[0];
			String password = params[1];
			String accountType = params[2];
			
			authToken = new ServerAuthenticate(context).userSignIn(username, password);

			data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
			data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
			data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
			data.putString(LoginActivity.PARAM_USER_PASS, password);
		} catch (Exception e) {
			data.putString(LoginActivity.KEY_ERROR_MESSAGE, e.getMessage());

			e.printStackTrace();
		}

		final Intent res = new Intent();
		res.putExtras(data);
		return res;
	}

	@Override
	protected void onPostExecute(Intent intent) {
		if (intent.hasExtra(LoginActivity.KEY_ERROR_MESSAGE)) {
			Toast.makeText(context,
					intent.getStringExtra(LoginActivity.KEY_ERROR_MESSAGE),
					Toast.LENGTH_SHORT).show();
		} else {
			if (callback != null) {
				callback.onTaskCompleted(intent);
			}
		}
		
	}

}

package me.hoen.android_auth_sync.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {

	private static final Object lock = new Object();
	private Authenticator auth;

	@Override
	public void onCreate() {
		synchronized (lock) {
			if (auth == null) {
				auth = new Authenticator(this);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return auth.getIBinder();
	}

}

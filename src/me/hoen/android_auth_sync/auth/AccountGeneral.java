package me.hoen.android_auth_sync.auth;

import android.content.Context;

public class AccountGeneral {
	public static final String ACCOUNT_NAME = "AuthSync";
	public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
	public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an AuthSync account";

	protected Context context;
	protected ServerAuthenticate serverAuthenticate;

	public AccountGeneral(Context context) {
		super();
		this.context = context;
	}

	public ServerAuthenticate getServerAuthenticate() {
		if (this.serverAuthenticate == null) {
			serverAuthenticate = new ServerAuthenticate(context);
		}

		return serverAuthenticate;
	}
}

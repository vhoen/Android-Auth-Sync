package me.hoen.android_auth_sync.auth;

import android.content.Context;

public class AccountGeneral {

	/**
	 * Account type id
	 */
	public static final String ACCOUNT_TYPE = "me.hoen.android_auth_sync.auth";
	/**
	 * Account name
	 */
	public static final String ACCOUNT_NAME = "AuthSync";
	/**
	 * Auth token types
	 */
	// public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
	// public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL =
	// "Read only access to an AuthSync account";
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

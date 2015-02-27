## Android Authenticator

- [ServerAuthenticate](./src/me/hoen/android_auth_sync/auth/ServerAuthenticate.java)
```java
public class ServerAuthenticate {
    protected Context context;

    public ServerAuthenticate(Context context) {
        super();
        this.context = context;
    }

    public String userSignIn(String username, String password) throws Exception {

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(
                    context.getString(R.string.login_path));

            HttpParams postParams = new BasicHttpParams();
            postParams.setParameter("username", username);
            postParams.setParameter("password", password);
            httpPost.setParams(postParams);

            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);

            JSONObject json = new JSONObject(result);

            // using dummy data as token
            String authToken = json.getJSONArray("results").getJSONObject(0)
                    .getString("seed");

            Log.d(MainActivity.TAG, "Authentication auth token : " + authToken);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
```

- [AccountGeneral](./src/me/hoen/android_auth_sync/auth/AccountGeneral.java)
```java
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
```

- [LoginActivity.java](./src/me/hoen/android_auth_sync/auth/LoginActivity.java)
```java
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

    protected AuthOnTaskCompleted loginCallback = new AuthOnTaskCompleted() {

        @Override
        public void onTaskCompleted(Intent intent) {
            finishLogin(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        new AuthenticationTask(getApplicationContext(), loginCallback).execute(
                username, userpass, accountType);
    }

    private void finishLogin(Intent intent) {
        String accountName = intent
                .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName,
                intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
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
```

- [User](./src/me/hoen/android_auth_sync/auth/User.java)
```java
public class User implements Serializable {
    String token;
    String username;

    public User(String username, String token) {
        this.username = username;
        this.token = token;
    }

    @Override
    public String toString() {
        return "User [token=" + token + ", username=" + username + "]";
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
```

- [Authenticator](./src/me/hoen/android_auth_sync/auth/Authenticator.java)
```java
public class Authenticator extends AbstractAccountAuthenticator {
    public static final String ACCOUNT_TYPE = "me.hoen.android_auth_sync.auth";
    public static final String AUTHTOKEN_TYPE = "me.hoen.android_auth_sync.auth";
    
    private Context mContext;

    public Authenticator(Context context) {
        super(context);

        this.mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
            String accountType, String authTokenType,
            String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(LoginActivity.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
            Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
            String accountType) {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {

        if (!authTokenType.equals(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE,
                    "invalid authTokenType");
            return result;
        }

        final AccountManager am = AccountManager.get(mContext);

        String authToken = am.peekAuthToken(account, authTokenType);

        if (TextUtils.isEmpty(authToken)) {
            final String password = am.getPassword(account);
            if (password != null) {
                try {
                    authToken = new ServerAuthenticate(mContext)
                            .userSignIn(account.name, password);
                    User user = new User(account.name, authToken);
                    if (user != null) {
                        authToken = user.getToken();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        final Intent intent = new Intent(mContext, Authenticator.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                response);
        intent.putExtra("ACCOUNT_TYPE", account.type);
        intent.putExtra("AUTH_TYPE", authTokenType);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
            return AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        else
            return authTokenType + " (Label)";
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
            Account account, String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        return null;
    }
}
```

- [AuthenticatorService](./src/me/hoen/android_auth_sync/auth/AuthenticatorService.java)
```java
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
```

- [authenticator.xml](./res/xml/authenticator.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<account-authenticator xmlns:android="http://schemas.android.com/apk/res/android"
    android:accountType="lstech.aos.auth.authentication"
    android:icon="@drawable/ic_launcher"
    android:label="@string/app_name"
    android:smallIcon="@drawable/ic_launcher" />
```
- [Android Manifest](./AndroidManifest.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="me.hoen.android_auth_sync"
    android:versionCode="1"
    android:versionName="1.0" >

    <uses-sdk
        android:minSdkVersion="8"
        android:targetSdkVersion="21" />

    <uses-permission android:name="android.permission.AUTHENTICATE_ACCOUNTS" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />
    <uses-permission android:name="android.permission.MANAGE_ACCOUNTS" />
    <uses-permission android:name="android.permission.USE_CREDENTIALS" />

    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme" >
       
        <activity
            android:name="me.hoen.android_auth_sync.auth.LoginActivity"
            android:clearTaskOnLaunch="true"
            android:label="@string/title_activity_login" >
        </activity>

        <service
            android:name="me.hoen.android_auth_sync.auth.AuthenticatorService"
            android:exported="false" >
            <intent-filter>
                <action android:name="android.accounts.AccountAuthenticator" />
            </intent-filter>

            <meta-data
                android:name="android.accounts.AccountAuthenticator"
                android:resource="@xml/authenticator" />
        </service>

        <provider
            android:name="me.hoen.android_auth_sync.sync.SyncContentProvider"
            android:authorities="me.hoen.android_auth_sync.provider"
            android:exported="false"
            android:syncable="true" />

        <service
            android:name="me.hoen.android_auth_sync.sync.SyncService"
            android:exported="false" >
            <intent-filter>
                <action android:name="android.content.SyncAdapter" />
            </intent-filter>

            <meta-data
                android:name="android.content.SyncAdapter"
                android:resource="@xml/syncadapter" />
        </service>
    </application>

</manifest>
```

- [MainActivity](./src/me/hoen/android_auth_sync/MainActivity.java)
```java
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

        String accountType = Authenticator.ACCOUNT_TYPE;
        String authTokenType = Authenticator.AUTHTOKEN_TYPE;

        am.getAuthTokenByFeatures(accountType, authTokenType, null, this, null,
                null, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            Bundle bnd = future.getResult();
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
```
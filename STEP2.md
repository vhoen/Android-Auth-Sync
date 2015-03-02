## Android Authenticator

- [strings.xml](./res/values/strings.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <string name="app_name">Android-Auth-Sync</string>
    <string name="desc">Desc</string>
    <string name="login_path">http://api.randomuser.me/?results=1</string>
    <string name="sync_path">http://api.randomuser.me/?results=3</string>
    <string name="account_type">me.hoen.android_auth_sync.auth</string>
    <string name="auth_token_type">@string/account_type</string>
    <string name="sync_provider">me.hoen.android_auth_sync.provider</string>
    <string name="profile">Profile</string>
    <string name="sync">Synchronize</string>
    <string name="logout">Logout</string>
</resources>
```

- [ServerAuthenticate](./src/me/hoen/android_auth_sync/auth/ServerAuthenticate.java)
```java
public class ServerAuthenticate {
    protected Context context;

    public ServerAuthenticate(Context context) {
        super();
        this.context = context;
    }

    public User userSignIn(String username, String password) throws Exception {

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

            User u = User.fromJson(json);
        
            UserManager.getInstance().addUser(u);
            
            String authToken = u.getToken();

            Log.d(MainActivity.TAG, "Authentication auth token : " + authToken);
            return u;
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

        // set auto-sync
        ContentResolver.setIsSyncable(account,
                getString(R.string.sync_provider), 1);
        ContentResolver.setSyncAutomatically(account,
                getString(R.string.sync_provider), true);

        // request sync to start asap
        Utils.requestSync(account, getApplicationContext());

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            String authtoken = intent
                    .getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            mAccountManager
                    .addAccountExplicitly(account, accountPassword, null);

            User u = UserManager.getInstance().getUser(accountName);
            if (u != null) {
                User.saveToAccount(u, account, getApplicationContext());
            }
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
    String username;
    String email;
    String password;

    String title;
    String firstName;
    String lastName;
    String thumbnail;

    String token;

    public User() {

    }

    public User(String username, String token) {
        this.username = username;
        this.token = token;
    }

    @Override
    public String toString() {
        return "User [token=" + token + ", username=" + username + "]";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    static public User fromJson(JSONObject json) {
        try {
            JSONArray results = json.getJSONArray("results");
            if (results.length() > 0) {
                JSONObject jsonUser = results.getJSONObject(0).getJSONObject(
                        "user");

                User u = new User();

                u.setUsername(jsonUser.getString("username"));
                u.setEmail(jsonUser.getString("email"));
                u.setPassword(jsonUser.getString("password"));

                u.setTitle(jsonUser.getJSONObject("name").getString("title"));
                u.setFirstName(jsonUser.getJSONObject("name")
                        .getString("first"));
                u.setLastName(jsonUser.getJSONObject("name").getString("last"));
                u.setThumbnail(jsonUser.getJSONObject("picture").getString(
                        "thumbnail"));

                u.setToken(jsonUser.getString("sha256"));

                return u;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    static public User fromAccount(Account account, Context context) {
        User u = new User();

        AccountManager am = AccountManager.get(context);

        u.setUsername(am.getUserData(account, "username"));
        u.setEmail(am.getUserData(account, "email"));
        u.setPassword(am.getUserData(account, "password"));

        u.setTitle(am.getUserData(account, "title"));
        u.setFirstName(am.getUserData(account, "firstName"));
        u.setLastName(am.getUserData(account, "lastName"));
        u.setThumbnail(am.getUserData(account, "thumbnail"));

        u.setToken(am.getUserData(account, "token"));

        return u;
    }

    static public User saveAccount(JSONObject result, Account account, Context context) {
        User u = User.fromJson(result);

        if (u != null) {
            saveToAccount(u, account, context);
        }

        return u;
    }

    static public void saveToAccount(User user, Account account, Context context) {
        AccountManager am = AccountManager.get(context);

        am.setUserData(account, "username", user.getUsername());
        am.setUserData(account, "email", user.getEmail());
        am.setUserData(account, "password", user.getPassword());

        am.setUserData(account, "title", user.getTitle());
        am.setUserData(account, "firstName", user.getFirstName());
        am.setUserData(account, "lastName", user.getLastName());
        am.setUserData(account, "thumbnail", user.getThumbnail());

        am.setUserData(account, "token", user.getToken());
    }
}
```

- [Authenticator](./src/me/hoen/android_auth_sync/auth/Authenticator.java)
```java
public class Authenticator extends AbstractAccountAuthenticator {

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
                    User u = new ServerAuthenticate(mContext).userSignIn(
                            account.name, password);

                    if (u != null) {
                        User.saveToAccount(u, account, this.mContext);
                        authToken = u.getToken();
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
    android:accountType="@string/account_type"
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

        String accountType = getString(R.string.account_type);
        String authTokenType = getString(R.string.auth_token_type);

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
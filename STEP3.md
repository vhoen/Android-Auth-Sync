## Android Sync Adapter 
At this point, we could display the contact list if we had any contact. That's where the Sync Adapter comes into play. It will fetch a few contacts and store them into a database, and once done, it will tell the fragment that it can refresh its data.

- [LoadContactsTask](./src/me/hoen/android_auth_sync/sync/LoadContactsTask.java)
```java
public class LoadContactsTask  extends AsyncTask<String, Void, JSONObject> {
    protected Context context;

    public LoadContactsTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(
                    context.getString(R.string.sync_path));
            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);

            return new JSONObject(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(JSONObject json) {
        try {
            if (json != null) {
                if (json != null && json.has("results")) {
                    JSONArray usersList = json.getJSONArray("results");

                    for (int i = 0; i < usersList.length(); i++) {
                        JSONObject result = usersList.getJSONObject(i);
                        JSONObject user = result.getJSONObject("user");

                        Contact c = Contact.fromJsonObject(user);
                        
                        ContactDataSource cds = new ContactDataSource(context);
                        cds.create(c);
                        cds.close();
                        
                        Log.d(MainActivity.TAG, "New Contact : " + c);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

- [SyncAdapter](./src/me/hoen/android_auth_sync/sync/SyncAdapter.java)
```java
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
```

- [SyncService](./src/me/hoen/android_auth_sync/sync/SyncService.java)
```java
public class SyncService  extends Service {

    private static SyncAdapter syncAdapter = null;
    private static final Object syncAdapterLock = new Object();
    
    @Override
    public void onCreate() {
    
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
```

- [SyncContentProvider](./src/me/hoen/android_auth_sync/sync/SyncContentProvider.java)
```java
public class SyncContentProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return 0;
    }

}
```

- [syncadapter.xml](./res/xml/syncadapter.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<sync-adapter xmlns:android="http://schemas.android.com/apk/res/android"
    android:accountType="@string/account_type"
    android:contentAuthority="me.hoen.android_auth_sync.provider"
    android:supportsUploading="true"
    android:userVisible="true" />
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

    <uses-permission android:name="android.permission.READ_SYNC_SETTINGS" />
    <uses-permission android:name="android.permission.WRITE_SYNC_SETTINGS" />
    <uses-permission android:name="android.permission.READ_SYNC_STATS" />

    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme" >
        
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

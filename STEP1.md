## Display a list of contacts
Let's start not too complicated, let's set up the part where our contacts will be displayed.

- [AndroidManifest](./AndroidManifest.xml)
```xml 
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="me.hoen.android_auth_sync"
    android:versionCode="1"
    android:versionName="1.0" >

    <uses-sdk
        android:minSdkVersion="8"
        android:targetSdkVersion="21" />

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme" >
        <activity
            android:name=".MainActivity"
            android:label="@string/app_name" >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
       
    </application>

</manifest>
```

- [SqliteHelper](./src/me/hoen/android_auth_sync/db/SqliteHelper.java)
```java
public class SqliteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "auth_sync.db";

    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_CONTACTS = "contacts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_URL = "url";

    protected SQLiteDatabase database;
    protected Context context;

    public SqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        this.database = database;
        createContactsTable();
    }

    protected void createContactsTable() {
        String sql = "CREATE TABLE '" + SqliteHelper.TABLE_CONTACTS
                + "' ( '_id' INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "'title' VARCHAR(10)," + "'first_name' VARCHAR ( 200 ),"
                + "'last_name' VARCHAR ( 200 ), " + "'url' TEXT)";
        database.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }

}
```

- [Contact](./src/me/hoen/android_auth_sync/db/Contact.java)
```java
public class Contact {
    protected int id;
    protected String title;
    protected String firstName;
    protected String lastName;
    protected String url;

    public static Contact fromJsonObject(JSONObject json) {
        try {
            Contact c = new Contact();
            c.setTitle(json.getJSONObject("name").getString("title"));
            c.setFirstName(json.getJSONObject("name").getString("first"));
            c.setLastName(json.getJSONObject("name").getString("last"));
            c.setUrl(json.getJSONObject("picture").getString("thumbnail"));

            return c;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    static public Contact fromCursor(Cursor cursor) {
        if (cursor != null && cursor.getCount() != 0) {
            Contact c = new Contact();
            c.setId(cursor.getInt(cursor.getColumnIndex(SqliteHelper.COLUMN_ID)));
            c.setTitle(cursor.getString(cursor
                    .getColumnIndex(SqliteHelper.COLUMN_TITLE)));
            c.setFirstName(cursor.getString(cursor
                    .getColumnIndex(SqliteHelper.COLUMN_FIRST_NAME)));
            c.setLastName(cursor.getString(cursor
                    .getColumnIndex(SqliteHelper.COLUMN_LAST_NAME)));
            c.setUrl(cursor.getString(cursor
                    .getColumnIndex(SqliteHelper.COLUMN_URL)));
            return c;
        }
        return null;
    }

}
```

- [ContactDataSource](./src/me/hoen/android_auth_sync/db/ContactDataSource.java)
```java
public class ContactDataSource {
    private String[] allColumns = { SqliteHelper.COLUMN_ID,
            SqliteHelper.COLUMN_TITLE, SqliteHelper.COLUMN_FIRST_NAME,
            SqliteHelper.COLUMN_LAST_NAME, SqliteHelper.COLUMN_URL };
    private Context context;
    protected SQLiteDatabase database;
    protected SqliteHelper dbHelper;

    public ContactDataSource(Context context) {
        this.context = context;
        initSqliteHelper();
        open();
    }

    protected void initSqliteHelper() {
        dbHelper = new SqliteHelper(context);
    }

    protected void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        database.close();
        dbHelper.close();
    }

    public void create(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(SqliteHelper.COLUMN_TITLE, contact.getTitle());
        values.put(SqliteHelper.COLUMN_FIRST_NAME, contact.getFirstName());
        values.put(SqliteHelper.COLUMN_LAST_NAME, contact.getLastName());
        values.put(SqliteHelper.COLUMN_URL, contact.getUrl());
        database.insert(SqliteHelper.TABLE_CONTACTS, null, values);
    }

    public ArrayList<Contact> getContacts() {
        ArrayList<Contact> list = new ArrayList<Contact>();
        String orderBy = SqliteHelper.COLUMN_ID + " DESC";
        Cursor cursor = database.query(SqliteHelper.TABLE_CONTACTS, allColumns,
                null, null, null, null, orderBy);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(Contact.fromCursor(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return list;
    }
}
```

- [ContactAdapter](./src/me/hoen/android_auth_sync/ContactAdapter.java)
```java
public class ContactAdapter extends ArrayAdapter<Contact> {
    protected Activity activity;
    protected ArrayList<Contact> list;
    protected int textViewResourceId;

    public ContactAdapter(Activity activity, int textViewResourceId,
            ArrayList<Contact> list) {
        super(activity, textViewResourceId, list);
        this.activity = activity;
        this.textViewResourceId = textViewResourceId;
        this.list = list;
    }

    public int getCount() {
        return list.size();
    }

    public Contact getItem(Contact position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView picture;
        TextView title;
        TextView firstName;
        TextView lastName;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(textViewResourceId, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.picture = (ImageView) rowView.findViewById(R.id.picture);
            viewHolder.title = (TextView) rowView.findViewById(R.id.title);
            viewHolder.firstName = (TextView) rowView
                    .findViewById(R.id.firstName);
            viewHolder.lastName = (TextView) rowView
                    .findViewById(R.id.lastName);

            rowView.setTag(viewHolder);
        }

        Contact c = list.get(position);

        ViewHolder holder = (ViewHolder) rowView.getTag();
        
        new DownloadImageTask(holder.picture).execute(c.getUrl());
        
        holder.title.setText(c.getTitle());
        holder.firstName.setText(c.getFirstName());
        holder.lastName.setText(c.getLastName());
        

        return rowView;
    }

    
    static public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
```

- [ContactsFragment](./src/me/hoen/android_auth_sync/ContactsFragment.java)
```java
public class ContactsFragment extends Fragment {
    protected ContactAdapter adapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container,
                false);
        
        ArrayList<Contact> list = new ArrayList<Contact>();
        ListView listLv = (ListView) rootView.findViewById(R.id.contacts);
        adapter = new ContactAdapter(getActivity(), R.layout.item_contact, list);
        listLv.setAdapter(adapter);
        
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    protected void loadData() {
        ContactDataSource eds = new ContactDataSource(getActivity());
        ArrayList<Contact> list = eds.getContacts();
        eds.close();
        
        adapter.clear();
        adapter.addAll(list);
        adapter.notifyDataSetChanged();
    }
}
```

- [MainActivity](./src/me/hoen/android_auth_sync/MainActivity.java)
```java
public class MainActivity extends ActionBarActivity {

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
}
```


Right now, you have every pieces needed to display our contact list, unfortunately, you don't have any data to display. That will come later. Right now to make sure that you will get your very own data, you need to set up authentication on your app.
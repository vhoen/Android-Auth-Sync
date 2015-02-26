package me.hoen.android_auth_sync.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

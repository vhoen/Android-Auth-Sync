package me.hoen.android_auth_sync.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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
		// make sure to close the cursor
		cursor.close();
		return list;
	}
}

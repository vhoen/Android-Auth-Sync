package me.hoen.android_auth_sync.db;

import org.json.JSONObject;

import android.database.Cursor;

public class Contact {
	protected int id;
	protected String title;
	protected String firstName;
	protected String lastName;
	protected String url;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "Contact [id=" + id + ", title=" + title + ", firstName="
				+ firstName + ", lastName=" + lastName + ", url=" + url + "]";
	}

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

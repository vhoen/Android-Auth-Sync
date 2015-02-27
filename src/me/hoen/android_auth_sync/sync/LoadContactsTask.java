package me.hoen.android_auth_sync.sync;

import me.hoen.android_auth_sync.MainActivity;
import me.hoen.android_auth_sync.R;
import me.hoen.android_auth_sync.db.Contact;
import me.hoen.android_auth_sync.db.ContactDataSource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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
package me.hoen.android_auth_sync.auth;

import me.hoen.android_auth_sync.MainActivity;
import me.hoen.android_auth_sync.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

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

package me.hoen.android_auth_sync.auth;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

@SuppressWarnings("serial")
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

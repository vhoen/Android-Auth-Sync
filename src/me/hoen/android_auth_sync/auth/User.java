package me.hoen.android_auth_sync.auth;

import java.io.Serializable;

@SuppressWarnings("serial")
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

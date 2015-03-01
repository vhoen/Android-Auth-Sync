package me.hoen.android_auth_sync.auth;

import java.util.HashMap;

public class UserManager {
	private static UserManager instance = new UserManager();

	private HashMap<String, User> users = new HashMap<String, User>();

	private UserManager() {

	}

	public static UserManager getInstance() {
		return instance;
	}

	public void addUser(User u) {
		users.put(u.getUsername(), u);
	}

	public User getUser(String username) {
		if (users.containsKey(username)) {
			return users.get(username);
		}

		return null;
	}
}

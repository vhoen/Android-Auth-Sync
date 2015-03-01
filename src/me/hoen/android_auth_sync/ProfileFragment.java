package me.hoen.android_auth_sync;

import me.hoen.android_auth_sync.auth.User;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_profile, container,
				false);

		AccountManager am = AccountManager.get(getActivity());
		Account[] accounts = am
				.getAccountsByType(getString(R.string.account_type));
		if (accounts.length > 0) {
			Account account = accounts[0];
			User u = User.fromAccount(account, getActivity());

			if (u != null) {
				ImageView pictureIv = (ImageView) rootView
						.findViewById(R.id.picture);
				new ContactAdapter.DownloadImageTask(pictureIv).execute(u
						.getThumbnail());

				TextView titleTv = (TextView) rootView.findViewById(R.id.title);
				titleTv.setText(u.getTitle());

				TextView firstNameTv = (TextView) rootView
						.findViewById(R.id.firstName);
				firstNameTv.setText(u.getFirstName());

				TextView lastNameTv = (TextView) rootView
						.findViewById(R.id.lastName);
				lastNameTv.setText(u.getLastName());
			}
		}

		return rootView;
	}
}

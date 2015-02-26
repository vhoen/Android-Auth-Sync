package me.hoen.android_auth_sync;

import java.util.ArrayList;

import me.hoen.android_auth_sync.db.Contact;
import me.hoen.android_auth_sync.db.ContactDataSource;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

package me.hoen.android_auth_sync;

import java.io.InputStream;
import java.util.ArrayList;

import me.hoen.android_auth_sync.db.Contact;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAdapter extends ArrayAdapter<Contact> {
	protected Activity activity;
	protected ArrayList<Contact> list;
	protected int textViewResourceId;

	public ContactAdapter(Activity activity, int textViewResourceId,
			ArrayList<Contact> list) {
		super(activity, textViewResourceId, list);
		this.activity = activity;
		this.textViewResourceId = textViewResourceId;
		this.list = list;
	}

	public int getCount() {
		return list.size();
	}

	public Contact getItem(Contact position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	static class ViewHolder {
		ImageView picture;
		TextView title;
		TextView firstName;
		TextView lastName;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			rowView = inflater.inflate(textViewResourceId, parent, false);

			ViewHolder viewHolder = new ViewHolder();
			viewHolder.picture = (ImageView) rowView.findViewById(R.id.picture);
			viewHolder.title = (TextView) rowView.findViewById(R.id.title);
			viewHolder.firstName = (TextView) rowView
					.findViewById(R.id.firstName);
			viewHolder.lastName = (TextView) rowView
					.findViewById(R.id.lastName);

			rowView.setTag(viewHolder);
		}

		Contact c = list.get(position);

		ViewHolder holder = (ViewHolder) rowView.getTag();
		
		new DownloadImageTask(holder.picture).execute(c.getUrl());
		
		holder.title.setText(c.getTitle());
		holder.firstName.setText(c.getFirstName());
		holder.lastName.setText(c.getLastName());
		

		return rowView;
	}

	
	static public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
		}
	}
}

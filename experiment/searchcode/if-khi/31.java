package src.dbs;

import java.net.MalformedURLException;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class dbsvideo extends Activity {

	private static final int STREAMING = Menu.FIRST;
	private static final int HELP = Menu.FIRST + 2;
	private static EditText editText;

	ArrayList<videoInfo> array;
	ListVideoAdapter arrayAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		array = new ArrayList<videoInfo>();
		arrayAdapter = new ListVideoAdapter(this, R.layout.list, array);

		final Button buttonSearch = (Button) findViewById(R.id.button_search);
		final Button buttonUpdate = (Button) findViewById(R.id.button_update);
		editText = (EditText) findViewById(R.id.name_enter);

		final ListView list = (ListView) findViewById(R.id.list);
		list.setAdapter(arrayAdapter);
		update();

		// --------------Xu ly su kien khi nguoi dung click vao button
		// Search------------
		buttonSearch.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String result = null;
				String nameSearch = null;
				nameSearch = (editText.getText().toString());
				Log.i("error", nameSearch);
				if (nameSearch.equals("")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							dbsvideo.this);
					builder.setTitle("Error");
					builder.setMessage("Enter name!!!");
					builder.setPositiveButton("Close",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							});
					builder.setTitle("Error");
					builder.show();
				} else {
					readData reader = new readData();
					String key = "name";
					try {
						result = reader.getData("Content", key, nameSearch); // lay
																				// du
																				// lieu
																				// tu
																				// tren
																				// server
						Log.i("test", result);
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (result.length() > 0) {
						int l = result.length();
						int c = 0;
						int id = 0;
						String name = null;
						String link = null;
						long l1 = 0;
						long l2 = 0;
						Log.i("error", "abc" + " " + result);
						while (c < l - 1) {

							String t = "";
							while (result.charAt(c) != '|') {
								t = t + result.charAt(c);
								c++;
							}
							// ----------------------
							Log.i("error", "abc" + t);
							c = c + 2;
							id = Integer.parseInt(t.trim());
							t = "";
							while (result.charAt(c) != '|') {
								t = t + result.charAt(c);
								c++;
							}
							Log.i("error", "abc" + t);
							c = c + 2;
							name = t;
							t = "";
							// ----------------------
							while (result.charAt(c) != '|') {
								t = t + result.charAt(c);
								c++;
							}
							Log.i("error", "abc" + t);
							c = c + 2;
							link = t;
							t = "";
							// -----------------------
							while (result.charAt(c) != '|') {
								t = t + result.charAt(c);
								c++;
							}
							Log.i("error", "abc" + t);
							c = c + 2;
							l1 = Long.parseLong(t.trim());
							t = "";
							// ------------------------
							while (result.charAt(c) != '|') {
								t = t + result.charAt(c);
								c++;
							}
							Log.i("error", "abc" + t);
							c = c + 2;
							l2 = Long.parseLong(t.trim());
							t = "";
							Log.i("error", "abc" + Integer.toString(c) + "  "
									+ Integer.toString(l));
						}
						Log.i("error", name + "" + link);
						videoInfo v = new videoInfo(id, name, link, l1, l2);
						array.add(v);
						arrayAdapter.notifyDataSetChanged();
					}
				}
			}
		});

		// Xu ly su kien khi nguoi dung click vao nut Update
		buttonUpdate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				update();
			}
		});
	}

	/*
	 * Lop xu ly su lien update, duoc goi khi chay chuong trinh lan dau, hoac
	 * nguoi dung muon update nhung video moi tu tren server
	 */
	public void update() {
		int max = 0;
		String result = null, key = "id";
		if (array.size() > 0) {
			for (int i = 0; i < array.size(); i++)
				if (array.get(i).getVideoId() > max)
					max = array.get(i).getVideoId();
		}
		Log.i("test", "test" + " " + Integer.toString(max));
		readData reader = new readData();
		String k = "id";
		try {
			result = reader.getData("Update", k, Integer.toString(max)); // lay
																			// du
																			// lieu
																			// tu
																			// tren
																			// server
			Log.i("test", result);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result.length() > 0) {
			int l = result.length();
			int c = 0;
			int id = 0;
			String name = null;
			String link = null;
			long l1 = 0;
			long l2 = 0;
			Log.i("error", "abc" + " " + result);
			while (c < l - 1) {

				String t = "";
				while (result.charAt(c) != '|') {
					t = t + result.charAt(c);
					c++;
				}
				// ----------------------
				Log.i("error", "abc" + t);
				c = c + 2;
				id = Integer.parseInt(t.trim());
				t = "";
				while (result.charAt(c) != '|') {
					t = t + result.charAt(c);
					c++;
				}
				Log.i("error", "abc" + t);
				c = c + 2;
				name = t;
				t = "";
				// ----------------------
				while (result.charAt(c) != '|') {
					t = t + result.charAt(c);
					c++;
				}
				Log.i("error", "abc" + t);
				c = c + 2;
				link = t;
				t = "";
				// -----------------------
				while (result.charAt(c) != '|') {
					t = t + result.charAt(c);
					c++;
				}
				Log.i("error", "abc" + t);
				c = c + 2;
				l1 = Long.parseLong(t.trim());
				t = "";
				// ------------------------
				while (result.charAt(c) != '|') {
					t = t + result.charAt(c);
					c++;
				}
				Log.i("error", "abc" + t);
				c = c + 2;
				l2 = Long.parseLong(t.trim());
				t = "";
				Log.i("error", "abc" + Integer.toString(c) + "  "
						+ Integer.toString(l));
			}
			Log.i("error", name + "" + link);
			videoInfo v = new videoInfo(id, name, link, l1, l2);
			array.add(v);
			arrayAdapter.notifyDataSetChanged();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu) Ham tao
	 * ra menu va cac lua chon cua no
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, STREAMING, 0, "Stream").setTitle("Streaming");
		menu.add(0, HELP, 0, "About").setTitle("Help");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * Ham xu ly cac su kien khi chon cac lua chon trong menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case STREAMING: {

			if (array.size() > 0) {
				for (int i = 0; i < array.size(); i++) {
					if (array.get(i).isChecked()) {
						Log.i("abc", "error" + array.get(i).getVideoLink());
						videoInfo video = array.get(i);
						String link = array.get(i).getVideoLink().toString();
						String name = array.get(i).getVideoName().toString();
						Bundle sendBundle = new Bundle(); // tao mot Bundle de
						// gui du lieu bo
						// sung
						sendBundle.putString("value", link);// dua du lieu bo
						// sung vao mot khoa
						// mang gia tri
						// value
						sendBundle.putString("name", name);// dua du lieu bo
						// sung vao mot khoa
						// mang gia tri name
						Intent intent = new Intent(dbsvideo.this,
								VideoPlayerDemo.class);// khoi tao mot Intent
						// moi

						intent.putExtras(sendBundle);//
						startActivity(intent);

						finish();

					}
				}
			}

			break;
		}
		case HELP: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Coltech");
			builder.setMessage("AUTHOR:" + "\n" + "  Bui Hoang Hung" + "\n"
					+ "Dep:" + "\n" + "  Computer Science\n"
					+ "+Neu ban muon xem video,\n"
					+ " ban phai lua chon video\n" + " muon xem, roi an vao\n"
					+ " menu->chon Stream\n" + "Neu ban muon tim kiem video\n"
					+ " theo ten, ban go ten\n" + " can tim vao EditTex roi\n"
					+ " chon Search" + "Nwu ban muon cap nhap\n"
					+ " video moi, ban chon Update\n" + "Thank you!!!");
			builder.setPositiveButton("Close",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			builder.setTitle("Help");
			builder.show();
			break;
		}
		}
		return true;
	}
}


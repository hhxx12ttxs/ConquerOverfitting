package com.example.ting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;

public class AllSongsFragment extends Fragment {

	class MySimpleExpandableListAdapter extends SimpleExpandableListAdapter{

		public MySimpleExpandableListAdapter(Context context, List<? extends Map<String, ?>> groupData,
				int expandedGroupLayout, int collapsedGroupLayout, String[] groupFrom, int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData, int childLayout, String[] childFrom,
				int[] childTo) {
			super(context, groupData, expandedGroupLayout, collapsedGroupLayout, groupFrom, groupTo, childData, childLayout,
					childFrom, childTo);
		}

		public MySimpleExpandableListAdapter(Context context, List<? extends Map<String, ?>> groupData,
				int groupLayout, String[] groupFrom, int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData, int childLayout, String[] childFrom,
				int[] childTo) {
			super(context, groupData, groupLayout, groupFrom, groupTo, childData, childLayout, childFrom, childTo);
		}

		public MySimpleExpandableListAdapter(Context context, List<? extends Map<String, ?>> groupData,
				int expandedGroupLayout, int collapsedGroupLayout, String[] groupFrom, int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData, int childLayout, int lastChildLayout,
				String[] childFrom, int[] childTo) {
			super(context, groupData, expandedGroupLayout, collapsedGroupLayout, groupFrom, groupTo, childData, childLayout,
					lastChildLayout, childFrom, childTo);
		}
		
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View view = super.getGroupView(groupPosition, isExpanded, convertView, parent);
			ExpandableListView listView = (ExpandableListView) parent;
			listView.expandGroup(groupPosition);
			return view;
		}
	}
	
	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			if (msg.what == 0x01) {
				if(dialog.isShowing()){
					dialog.dismiss();
				}
				
				Map<Character, ArrayList<HashMap<String, String>>> map = (Map<Character, ArrayList<HashMap<String, String>>>) msg.obj;

				List<HashMap<String, String>> groupData = new ArrayList<HashMap<String, String>>();
				List<ArrayList<HashMap<String, String>>> childData = new ArrayList<ArrayList<HashMap<String, String>>>();

				for (Character ch : map.keySet()) {
					HashMap<String, String> map1 = new HashMap<String, String>();
					map1.put("capital", String.valueOf(ch));
					groupData.add(map1);
					ArrayList<HashMap<String, String>> data = map.get(ch);
					Collections.sort(data, new Comparator<HashMap<String, String>>() {
						@Override
						public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
							String s1 = lhs.get("name");
							String s2 = rhs.get("name");
							if (s1 == null || s2 == null)
								return 0;// 错误比较
							return s1.compareTo(s2);
						}

					});
					childData.add(data);
				}
				adapter = new MySimpleExpandableListAdapter(getActivity(), groupData, R.layout.group_layout,
						new String[] { "capital" }, new int[] { R.id.textView1 }, childData, R.layout.child_layout,
						new String[] { "realname", "singer" }, new int[] { R.id.textView1, R.id.textView2 });

				listView.setAdapter(adapter);

			}
		}
	}

	Handler handler;
	SimpleExpandableListAdapter adapter;
	ExpandableListView listView;
	AlertDialog dialog; 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new MyHandler();
		
		View view = getLayoutInflater(savedInstanceState).inflate(R.layout.waiting_dialog, null);

		dialog = new AlertDialog.Builder(getActivity()).setCustomTitle(view).create();
		dialog.show();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				scanMusicFile();
			}
		}).start();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_all_songs, container, false);

		listView = (ExpandableListView) view.findViewById(R.id.expandableListView1);

		return view;
	}

	static String[] PROJECTION = new String[] { MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST };

	public void scanMusicFile() {
		Log.d("scanMusicFile", "start");
		ContentResolver cr = getActivity().getContentResolver();
		Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

		Log.d("scanMusicFile", cursor.getCount() + "");
		Map<Character, ArrayList<HashMap<String, String>>> map = new TreeMap<Character, ArrayList<HashMap<String, String>>>();

		for (cursor.moveToNext(); !cursor.isAfterLast(); cursor.moveToNext()) {
			String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
			addChildData(map, title, artist);
		}

		Message msg = new Message();
		msg.what = 0x01;
		msg.obj = map;
		handler.sendMessage(msg);
	}

	void addChildData(Map<Character, ArrayList<HashMap<String, String>>> map, String string, String singer) {
		String name = convertToHanyu(string);
		if (name == null)
			return;
		Character first = name.charAt(0);
		ArrayList<HashMap<String, String>> list = map.get(first);
		if (list == null) {
			list = new ArrayList<HashMap<String, String>>();
			map.put(first, list);
		}
		HashMap<String, String> map1 = new HashMap<String, String>();
		map1.put("name", name);
		map1.put("singer", singer);
		map1.put("realname", string);
		list.add(map1);
	}

	boolean isLetter(char ch){
		if(ch>='a' && ch <='z' || ch>='A' && ch<='Z')
			return true;
		return false;
	}
	
	boolean isDigit(char ch){
		if(ch>='0' && ch <='9' )
			return true;
		return false;
	}
	
	String convertToHanyu(String string) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			// 考虑是数字或是字母的情况
			if (Character.isDigit(ch) || isLetter(ch) || ch=='_' || ch=='-') {
				// Log.d("string", title.charAt(0) + "-----" + title);
				// continue;
				sb.append(Character.toLowerCase(ch));
				continue;
			}

			HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
			format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			format.setCaseType(HanyuPinyinCaseType.LOWERCASE);

			try {
				String[] temp = PinyinHelper.toHanyuPinyinStringArray(ch, format);
				if (temp != null) {
					sb.append(temp[0]);
				}
			} catch (BadHanyuPinyinOutputFormatCombination e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

}


package com.android.qiushi.set;

import java.util.List;

import com.android.qiushi.R;
import com.android.qiushi.Control.ControlThread;
import com.android.qiushi.Control.Global;
import com.android.qiushi.R.drawable;
import com.android.qiushi.R.id;
import com.android.qiushi.R.layout;
import com.android.qiushi.set.SetAdapter.GridItemView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.sax.StartElementListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class SceneSetNameAdapter extends BaseAdapter{
	 private LayoutInflater listContainer;
	 private Context context; 
	 private Activity mActivity;
	 public List<String> mList;
	 private Builder builder=null;
	 private int mIndex;
	 
	 public final class GridItemView{                //自定义控件集合     
         public ImageView image;     
         public TextView name;     
         public Button button;
         public Button changeBackGround;
     }
	 public SceneSetNameAdapter(Activity activity,int index){
		 	this.mActivity = activity;
			this.context = mActivity.getBaseContext(); 
	        listContainer = LayoutInflater.from(context);
	        //mList = list;
	        mIndex = index;
		}
	@Override  
	public int getCount() {
		// TODO Auto-generated method stub
		return Global.rooms[mIndex].sceneList.length;
	}
	
	public String getName(int num){
		return (String)mList.get(num);
	}
	
	public void setName(int num,String str){
		String name=(String)mList.get(num);
		name = str;
		mList.set(num, name);
	}
	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}
	
	public List<String> getList(){
		return mList;
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		GridItemView gridItemView=null;
        if (convertView == null) { 
        	gridItemView = new GridItemView();
        	convertView = listContainer.inflate(R.layout.set_layout_btn_item, null);   
        	convertView.setPadding(20, 0, 20, 0);
            //获取控件对象   
        	gridItemView.image = (ImageView)convertView.findViewById(R.id.btnImage);   
        	gridItemView.name = (TextView)convertView.findViewById(R.id.btnName);   
        	gridItemView.button = (Button)convertView.findViewById(R.id.changeName);
        	gridItemView.changeBackGround = (Button)convertView.findViewById(R.id.changeBackGround);
            //设置控件集到convertView   
            convertView.setTag(gridItemView);   
        }else {   
        	gridItemView = (GridItemView)convertView.getTag();   
        } 
		if(Global.rooms[mIndex].sceneList[position].scBitmap==null){
			gridItemView.image.setBackgroundResource(R.drawable.imagejpg);
		}else{
			gridItemView.image.setBackgroundDrawable(new BitmapDrawable(Global.rooms[mIndex].sceneList[position].scBitmap));
		}
		
		if(Global.rooms[mIndex].sceneList[position].scNm!=null){
			gridItemView.name.setText(Global.rooms[mIndex].sceneList[position].scNm);
		}
		else{
			gridItemView.name.setText("");
		}
       
//        gridItemView.image.setBackgroundResource(R.drawable.imagejpg);
//        gridItemView.name.setText(getName(position));
//        
        //gridItemView.button.setFocusable(false);
        //gridItemView.button.setFocusableInTouchMode(false);
        
        //gridItemView.changeBackGround.setFocusable(false);
        //gridItemView.changeBackGround.setFocusableInTouchMode(false);
        
        gridItemView.button.setOnClickListener(new ChangeNameListener(position));
        gridItemView.changeBackGround.setOnClickListener(new ChangeBackgroundListener(position));
        
		
		return convertView;
	} 
	
	public class ChangeNameListener implements OnClickListener{
		private int id;
		
		public ChangeNameListener(int num) {
			// TODO Auto-generated constructor stub
			this.id=num;
		}
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			final EditText input = new EditText(mActivity);
			//input.setText(getName(v.getId()));
			builder=new AlertDialog.Builder(mActivity);
			
			builder.setTitle("更改名称");
			
			builder.setView(input);
			builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					//setName(id, input.getText().toString());
					Global.rooms[mIndex].sceneList[id].scNm=input.getText().toString();
					ControlThread.updateSceneNM(Global.rooms[mIndex].sceneList[id].idKey, input.getText().toString());
					notifyDataSetChanged();
				}
			});
			builder.setNegativeButton("取消", null);
			builder.create().show();
		}
		
	}
	
	  
	public class ChangeBackgroundListener implements OnClickListener{
		private int id;
		public ChangeBackgroundListener(int num) {
			// TODO Auto-generated constructor stub
			this.id = num;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			builder=new AlertDialog.Builder(mActivity);
			builder.setTitle("更换背景");
			builder.setItems(new String[]{"本地上传","本地拍照","取消"}, new DialogInterface.OnClickListener() {
 				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
 					Global.listId = id;
					if(which==0){
						mActivity.startActivityForResult(new Intent(context,SetBackImage.class), 0);
						//context.startActivity();
						
					}else if(which==1){
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						//mActivity.
						
						mActivity.startActivityForResult(intent, 1);
					}else{
						dialog.dismiss();
					}
				}
			});
			builder.create().show();
		}
		
	}
	
	
}


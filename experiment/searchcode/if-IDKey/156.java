package com.android.qiushi.set;

import java.util.ArrayList;
import java.util.List;

import com.android.qiushi.MainActivity;
import com.android.qiushi.R;
import com.android.qiushi.Control.ControlThread;
import com.android.qiushi.Control.Global;
import com.android.qiushi.Control.Room;
import com.android.qiushi.Control.Scene;
import com.android.qiushi.R.drawable;
import com.android.qiushi.R.id;
import com.android.qiushi.R.layout;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SceneSetName extends ListActivity{
	private DragAndDropListView mDragAndDropListView;
	private SceneSetNameAdapter mSetNameAdapter;
	private int index=0;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drop_set_list);
		
		getActionBar().setLogo(R.drawable.logo);
		Intent intent = getIntent();
		index = intent.getIntExtra("name", 0);
		
		mSetNameAdapter = new SceneSetNameAdapter(this,index);
		//mListView=(ListView)findViewById(R.id.setList);
		mDragAndDropListView = (DragAndDropListView) getListView();
        
        mDragAndDropListView.setCacheColorHint(0);
        mDragAndDropListView.setDivider(null);
       // mDragAndDropListView.setSelector(R.drawable.icon);
        mDragAndDropListView.setDropListener(mDropListener);
        mDragAndDropListView.setRemoveListener(mRemoveListener);
		
		setListAdapter(mSetNameAdapter);
		
	}
	
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if(mSetNameAdapter!=null)
			mSetNameAdapter.notifyDataSetChanged();
		super.onResume();
	}



	private DragAndDropListView.DropListener mDropListener = new DragAndDropListView.DropListener() {
        public void drop(int from, int to) {
        	switchStr(Global.rooms[index].sceneList, from, to);
        	mSetNameAdapter.notifyDataSetChanged();
        	mDragAndDropListView.invalidateViews();
        	
        }
    };
    
    private void switchStr(Scene sc[],int from,int to){
    	Scene tempRm=sc[from];
    	
    	if(from<to){
	    	for(int i=from;i<to;i++){
	    		sc[i]=sc[i+1];
	    	}
    	}else{
    		for(int i=from;i>to;i--){
	    		sc[i]=sc[i-1];
	    	}
    	}
    	sc[to]=tempRm;
    }
    
    private DragAndDropListView.RemoveListener mRemoveListener =  new DragAndDropListView.RemoveListener() {
        public void remove(int which) {
        	
        }
    };
	
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
        
	        case android.R.id.home: {
	        	finish();
	        	break;  
	        }
	        case 1:
	        	startActivity(new Intent(this,MainActivity.class));
        		break;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
		menu.add(0,1,0,"返回标签").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(data!=null){
			if(requestCode==0){
				
				Global.rooms[index].sceneList[Global.listId].scBitmap = Global.decodeFile(data.getStringExtra("path"), 100, 100);
				ControlThread.updateSceneImg(Global.rooms[index].sceneList[Global.listId].idKey, Global.decodeFile(data.getStringExtra("path"), 100, 100));
				if(Global.room.roomId==Global.rooms[Global.listId].roomId){
					//Global.room.sceneList[Global.listId].scBitmap = Global.decodeFile(data.getStringExtra("path"), 100, 100);
				}
				
				
			}else if(requestCode==1){
				
			Bundle extras = data.getExtras();
			if(extras!=null){
		        Bitmap myBitmap = (Bitmap) extras.getParcelable("data");
		        Global.rooms[index].sceneList[Global.listId].scBitmap = myBitmap;
		        ControlThread.updateSceneImg(Global.rooms[index].sceneList[Global.listId].idKey,myBitmap);
		//            if(Global.room.roomId==Global.rooms[Global.listId].roomId){
		//				Global.room.sceneList[Global.listId].scBitmap = myBitmap;
		//			}
				}
			}
		}
	}
	

}


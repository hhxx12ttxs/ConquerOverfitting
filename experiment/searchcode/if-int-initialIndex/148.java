package com.example.scr_ani.Kafelek;



import com.example.scr_ani.R;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import dnd.DragRecognizer;
import dnd.OnDropListener;


public class TileRackDropTarget implements OnDropListener {

   protected ViewGroup dragLayer;
   protected int initialIndex;
   protected DragRecognizer recognizer;
   protected ViewGroup tileRack;
   protected LayoutParams tileRackLayoutParams;


   public TileRackDropTarget(DragRecognizer var1, ViewGroup var2, ViewGroup var3, LayoutParams var4) {
      this.dragLayer = var2;
      this.tileRack = var3;
      this.tileRackLayoutParams = var4;
      this.recognizer = var1;
   }

   public void onDrag(View var1, View var2, MotionEvent var3) {
      this.onDragSlide(var1, var2, var3);
   }

   public void onDragCancel(View var1, View var2, MotionEvent var3) {}

   public void onDragEnter(View var1, View var2, MotionEvent var3) {}

   public void onDragExit(View var1, View var2, MotionEvent var3) {
      Drawable var4 = ((ImageView)var2).getDrawable();
      int wysokosc= var4.getIntrinsicWidth();
      int szerokosc = var4.getIntrinsicHeight();
      ImageView var7 = new ImageView(var2.getContext());
      var7.setAlpha(192);
      var7.setImageDrawable(var4);
      var7.measure(wysokosc, szerokosc);
      var7.layout(0, 0, wysokosc, szerokosc);
      this.dragLayer.removeView(var2);
      this.dragLayer.addView(var7);
      this.recognizer.setActive(var7);
      Point var8 = this.recognizer.getOffset();
      this.recognizer.setActiveLocation((int)var3.getRawX() + var8.x - this.dragLayer.getLeft(), (int)var3.getRawY() + var8.y - this.dragLayer.getTop());
      var7.invalidate();
   }

   public void onDragSlide(View var1, View var2, MotionEvent var3) {
      if(var2.getParent() == this.dragLayer) {
         var2.setPadding(0, 0, 0, 0);
      }

      int var4 = this.tileRack.indexOfChild(var2);
      int var5 = ((ImageView)var2).getDrawable().getIntrinsicWidth();
      int var6 = (this.tileRack.getWidth() - var5 * this.tileRack.getChildCount()) / 2;
      int var7 = ((int)var3.getRawX() - var6) / var5;
      if(var7 != var4) {
         if(var2.getParent() != null) {
            ((ViewGroup)var2.getParent()).removeView(var2);
         }

         if(var7 < 0) {
            this.tileRack.addView(var2, 0, this.tileRackLayoutParams);
         } else if(var7 > this.tileRack.getChildCount()) {
            this.tileRack.addView(var2, this.tileRackLayoutParams);
         } else {
            this.tileRack.addView(var2, var7, this.tileRackLayoutParams);
         }
      }

   }

   	public boolean onDrop(View var1, View var2, MotionEvent var3) {
   		boolean var4;
   		//jesli postawilismy kafelek na stojaku
   		if(var2.getParent() == this.tileRack) {
   			ImageView var5 = (ImageView)var2;
   			var5.setAlpha(255);
   			KafelekDrawable var6 = (KafelekDrawable)var5.getDrawable();
   			if(var6.czyBlank())
   				var6.ustawTekst((String)null);
   			Drawable var7 = this.tileRack.getResources().getDrawable(R.drawable.tile3);
   			var7.setBounds(0, 0, var7.getIntrinsicWidth(), var7.getIntrinsicHeight());
   			var6.ustawIkone(var7);
   			var4 = true;
   		}else{
   			var4 = false;
   		}
   		return var4;
   	}
}


package com.example.scr_ani.Kafelek;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import dnd.OnDragListener;

public class TileRackDragSource implements OnDragListener {

   protected int activeWidth;
   protected int dragAlpha = 255;
   protected ViewGroup dragLayer;
   protected int initialIndex;
   protected LayoutParams layoutParams;
   protected ViewGroup tileRack;

   public TileRackDragSource(ViewGroup var1, ViewGroup var2) {
      this.dragLayer = var1;
      this.tileRack = var2;
   }

   public int getDragAlpha() {
      return this.dragAlpha;
   }

   public void onDragCancel(View var1, View var2, MotionEvent var3) {
      var2.setPadding(0, 0, 0, 0);
      if(var2.getParent() != null) {
         ((ViewGroup)var2.getParent()).removeView(var2);
      }

      if(var2 instanceof ImageView) {
         ((ImageView)var2).setAlpha(255);
      }

      this.tileRack.addView(var2, this.initialIndex, this.layoutParams);
      var2.requestLayout();
   }

   public void onDrop(View var1, View var2, MotionEvent var3) {}

   public View onStartDrag(View var1, MotionEvent var2, Point var3) {
      Rect var4 = new Rect();
      int var5 = (int)var2.getX();
      int var6 = (int)var2.getY();
      int var7 = 0;
      View var8;
      while(true) {
         if(var7 >= this.tileRack.getChildCount()) {
            var8 = null;
            break;
         }
         View var9 = this.tileRack.getChildAt(var7);
         var9.getHitRect(var4);
         if(var4.contains(var5, var6)) {
            this.initialIndex = var7;
            this.activeWidth = var9.getWidth();
            var3.x = var9.getLeft() - var5;
            var3.y = var9.getTop() - var6;
            if(var9 instanceof ImageView) {
               ((ImageView)var9).setAlpha(this.dragAlpha);
            }
            this.layoutParams = var9.getLayoutParams();
            var8 = var9;
            break;
         }

         ++var7;
      }

      return var8;
   }
   public void setDragAlpha(int var1) {
      this.dragAlpha = var1;
   }
}


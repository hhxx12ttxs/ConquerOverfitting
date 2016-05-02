/**
 *      This file is part of Dijon Parking <http://code.google.com/p/dijon-parking/>
 *      
 *      Dijon Parking is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *      
 *      Dijon Parking is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *      
 *      You should have received a copy of the GNU General Public License
 *      along with Dijon Parking.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dijonparking.gui;

import greendroid.app.GDMapActivity;
import greendroid.graphics.drawable.DrawableStateSet;
import greendroid.graphics.drawable.MapPinDrawable;
import greendroid.widget.ActionBarItem;
import greendroid.widget.LoaderActionBarItem;
import greendroid.widget.ActionBarItem.Type;

import java.util.ArrayList;
import java.util.List;

import org.dijonparking.R;
import org.dijonparking.xml.DownloaderAndParser;
import org.dijonparking.xml.Parking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class Map extends GDMapActivity {
	private static final int[] PRESSED_STATE = {android.R.attr.state_pressed};
	private static final int E6 = (int) Math.pow(10,6);
	
	private MapView map;
	
	private ArrayList<Parking> parkings;
	private List<MyLocationOverlay> myLocOver;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.map);
        getActionBar().addItem(Type.Refresh);
        getActionBar().addItem(Type.Help);
        
        map = (MapView) findViewById(R.id.map);
        map.setBuiltInZoomControls(true);
        map.getController().setZoom(15);
        //Carte centrĂŠe sur Dijon au dĂŠmarrage
        map.getController().setCenter(new GeoPoint(47322769, 5042562));
        
        parkings = (ArrayList<Parking>) getIntent().getExtras().get("parkings");
        
        myLocOver = new ArrayList<MyLocationOverlay>(1);
        myLocOver.add(new MyLocationOverlay(this, map));
        myLocOver.get(0).enableMyLocation();
        map.getOverlays().add(myLocOver.get(0));
        
        updateMapPin();
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (position) {
		case 0:
			((LoaderActionBarItem) item).setLoading(false);
			new DownloadAndParseTask(this).execute();
			return true;
		case 1:
			AlertDialog.Builder help = new AlertDialog.Builder(this);
			help.setTitle(R.string.significationcouleurpoint);
			View changes = getLayoutInflater().inflate(R.layout.help, null);
			help.setView(changes).show();
			return true;
		default:
			return super.onHandleActionBarItemClick(item, position);
		}
	}
	
	private class DownloadAndParseTask extends DownloaderAndParser {

		public DownloadAndParseTask(Context context) {
			super(context);
		}

		@Override
		protected void finalOperations(ArrayList<Parking> listParking) {
			parkings = listParking;
			updateMapPin();
		}

		@Override
		protected void restartTask() {
			new DownloadAndParseTask(getContext()).execute();
		}
		
	}
	
	private void updateMapPin() {
		final Resources r = getResources();
		
		map.getOverlays().retainAll(myLocOver);
		
		for (Parking parking : parkings) {
			double ratio = -1;
			
			if (parking.getNbPlaceDispoTotal() >= 0 && parking.getCapTotale() >= 0)
				ratio = (double) parking.getNbPlaceDispoTotal() / (double) parking.getCapTotale();
			
			BasicItemizedOverlay itemizedOverlay = new BasicItemizedOverlay(new MapPinDrawable(r, getColorStateList(ratio), getColorStateList(ratio)));
			final GeoPoint pos = new GeoPoint((int) (parking.getLatitude()*E6), (int) (parking.getLongitude()*E6));

			itemizedOverlay.addOverlay(new OverlayItem(pos, parking.getNom(), null), parking);
			
			map.getOverlays().add(itemizedOverlay);
		}
		System.out.println(map.getOverlays().size());
	}
	
	private class BasicItemizedOverlay extends ItemizedOverlay<OverlayItem> {

        private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
        private ArrayList<Parking> mParkings = new ArrayList<Parking>();

        public BasicItemizedOverlay(Drawable defaultMarker) {
            super(boundCenterBottom(defaultMarker));
        }

        public void addOverlay(OverlayItem overlay, Parking parking) {
            mOverlays.add(overlay);
            mParkings.add(parking);
            populate();
        }

        @Override
        protected OverlayItem createItem(int i) {
            return mOverlays.get(i);
        }

        @Override
        public int size() {
            return mOverlays.size();
        }

        @Override
        protected boolean onTap(int index) {
        	Intent it = new Intent(getApplicationContext(), InfoParking.class);
    		it.putExtra("parking", mParkings.get(index));
    		startActivity(it);
            return true;
        }

    }
	
	private ColorStateList getColorStateList(double ratio) {
		int[][] states = new int[2][];
        int[] colors = new int[2];

        final int color = getColor(ratio);

        states[0] = PRESSED_STATE;
        colors[0] = addRGB(color, -50);

        states[1] = DrawableStateSet.EMPTY_STATE_SET;
        colors[1] = color;

        return new ColorStateList(states, colors);
	}
	
	private static final int getColor(double ratio) {
		int r, g, b;
		
		if (ratio < 0)
			r = g = b = 171;
		else if (ratio < 0.2) {
			r = 222;
			g = 0;
			b = 56;
		}
		else if (ratio < 0.4) {
			r = 255;
			g = 140;
			b = 0;
		}
		else {
			r = 113;
			g = 211;
			b = 0;
		}
		
		return Color.rgb(r, g, b);
	}

	private static int addRGB(int color, int amount) {
        int r = constrain(Color.red(color) + amount, 0, 255);
        int g = constrain(Color.green(color) + amount, 0, 255);
        int b = constrain(Color.blue(color) + amount, 0, 255);
        return Color.rgb(r, g, b);
    }
	
	private static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }
}

package com.example.hotellocator;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

public class SearchableActivity extends ListActivity {

	final Hotel[] hotels = {
			new Hotel("Pegasus", "7 Knutsford Boulevard", "Lorem Ipsum is simply dummy text of the printing and typesetting industry.", "Kingston", "Kingston", new LatLng(25.002856, -76.795659)),
			new Hotel("Akwaaba", "Silver Sands Estates, Duncans","Nice scenery", "Duncans", "Trelawney", new LatLng(-78.3671, 18.26805)),
    		new Hotel("Alamanda Inn", "39 Salem Beach, P.O. Box 65", "Lovely Beaches", "Runaway Bay", "St. Ann", new LatLng(-78.3669,18.27206)),
    		new Hotel("Alfred Ocean Place",	"Norman Manley Blvd. P.O. Box", "Luscious Palm Trees", "Negril", "Westmoreland", new LatLng(-78.3669, 18.27165)),
    		new Hotel("Alhambra Inn", "1 Tucker Avenue", "Close to city", "Kingston 3", "Kingston", new LatLng(-78.3666,18.26627)),
    		new Hotel("All Seasons Beach Resort", "Barrett  Hall, Greenwood, Lit", "Nice sandy beaches", "Greenwood", "St. James", new LatLng(-78.3666,18.26356)),
    		new Hotel("Almond Tree", "Arawak P.O. Box 332", "Lovely weather", "Mammee Bay", "St. Ann", new LatLng(-78.3665,18.26398)),
    		new Hotel("Altamont Court Hotel", "1-3 Altamont Terrace", "5 Swimming pools", "Kingston 5", "Kinston", new LatLng(-78.3665,18.27204)),
    		new Hotel("Hotel Four Seasons", "18 Ruthven Road", "Kalooki every Tuesday", "Kingston 10", "Kingston", new LatLng(-77.9041,18.3639)),
    		new Hotel("Hotel Riu", "Tropical Bay Bloody Bay Negril", "Caters to Spanish speakers as well", "Negril", "Hanover", new LatLng(-77.8975,18.47607)),
    		new Hotel("Blue Harbour","P.O. Box 50", "Best Jerk Spot within 5 minutes", "Port Maria","St. Mary",new LatLng(-78.354,18.24547)),
    		new Hotel("Seaview Villa","Seaview District, Southfield", "Single and Double Occupany available", "Southfield",	"St. Elizabeth", new LatLng(-77.163,18.4268)),
    		new Hotel("Tia Maria Villa", "136 Silver Sands Estate, P.O.", "Dancing every other Wednesday", "Duncans", "Trelawney", new LatLng(-77.0723, 18.41486)),
    		new Hotel("Honey Comb",	"Silver Sands Estate", "Complimentary Continental Breakfast", "Duncans", "Trelawney", new LatLng(-77.9114, 18.46007))
    };
	
	final Attraction[] attractions = {
    		new Attraction("Xayamaca", "fun","Montego Bay", "Mr. Paul Hastings", "Dalton Hastings", "Howard Cooke Blvd, Freeport", "Montego Bay", "St. James", new String[]{"844-9935"}, new String[]{"Plant Centre", "Complex Grounds", "Gift Shop", "Juice Bar"}),
    		new Attraction("White River Valley", "boring", "Ocho Rios", "Daniel Melville", "Vaneka McKenzie", "Cascade", "Endevour", "St. Mary", new String[]{"974-2018","382-6907"}, new String[]{"River Tubing", "Horseback Riding", "Kayaking"} ),
    		new Attraction("Veronica Park", "nice food", "Montego Bay", "Mr. Brasco Lee", "Michael Lee Chin", "Allsides District", "Waita Bit P.O.", "Trelawney", new String[]{"538-8940", "468-9449"}, null),
    		new Attraction("Two Sister's Cave", "scary", "Kingston", "UCD", "Mrs. Winsome Roache", "Hellshire", null, "St. Catherine", null, new String[]{"Arawak Cave"}),
    		new Attraction("Bob Marley Museum", "Historic", "Kingston", "Jacqueline Stewart", "Jacqueline Stewart", "56 Hope Road", "Kingston 6", "St. Andrew", new String[]{"927-9152"}, new String[]{"Tour of the former home of Bob Marley"}),
    		new Attraction("Caymanas Track Ltd.", "Caymanas Park is Jamaica's only race track.", "Kingston", "Mr. Michael Sibbles"," Mr. Michael Sibbles", "Caymanas Park Complex", "Gregory Park P.O.", "St. Catherine", new String[]{"988-2523", "988-2524", "988-2525", "988-2526"}, new String[]{"Horse Racing", "Children Entertainment", "Dining"}),
    		new Attraction("KOOL RUNNINGS WATER PARK", "Delightful Negril vacation spot", "Negril",	"POINCIANA RESORTS Ltd", "MRS. NEKEISHA MYRIE-KOZER", "NORMAN MANLEY BLVD", null, null, new String[]{"957-5400", "957-5620"}, new String[]{"WATER SLIDES", "GO KARTS", "DINING", "CONFERENCE", "PAINT BALL", "RAFTING"})
    };
	
	ArrayAdapter<String> listAdapter;
	ArrayList<String> found;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchable_results);
		
		found = getIntent().getStringArrayListExtra("names");
		
			listAdapter = new ArrayAdapter<String>(this,
			        android.R.layout.simple_list_item_1, found);
			
			setListAdapter(listAdapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
			 //TODO Auto-generated method stub
			 //Go to details of hotel
			String type = null;
			boolean typeFound=false;
			for(int i=0;i<hotels.length;i++)
			{
				if(hotels[i].getHotelName().equals(found.get(position)))
				{
					type="hotel";
					typeFound=true;
					break;
				}
			}
			if(typeFound==false){
			for(int i=0;i<attractions.length;i++)
			{
				if(attractions[i].getName().equals(found.get(position)))
				{
					type="attraction";
					break;
				}
			}}
			Intent myIntent = new Intent();
			myIntent.setClass(this, DetailsFragment.class);
			myIntent.putExtra("name", found.get(position));
			myIntent.putExtra("type", type);
		    startActivity(myIntent);
		}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.searchable, menu);
		return true;
	}

}


package com.example.hotellocator;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class FeaturedFragment extends ListActivity {

	TextView tBox1;
	TextView name;
	TextView tBox2;
	Button seeMore;
	String type;
	
	String desc = "This hotel in Jamaica has an enviable reputation as the Caribbean's premier business and leisure hotel. They are in the vicinity of major businesses, attractions and entertainment in Jamaica." + 
			" The world-class facilities, competitive rates and warm staff make it the first choice for leisure and business." + 
			" Captivating and carefree, richly appointed and steeped in tradition, they take you away and bring you closer together. Long one of the most beautiful and elegant boutique resorts in the Caribbean, they cater, with impeccable service and warm Jamaican hospitality, to the needs of its sophisticated guestsfamilies, couples, and friends.";

	ArrayList<String> featuredList = new ArrayList<String>();
	ArrayAdapter<String> listAdapter;
	
	final Hotel[] featuredHotels = {
			new Hotel("Pegasus", "7 Knutsford Boulevard", desc, "Kingston", "Kingston", new LatLng(25.002856, -76.795659)),
    		new Hotel("Alhambra Inn", "1 Tucker Avenue", "Close to city", "Kingston 3", "Kingston", new LatLng(-78.3666,18.26627)),
    		new Hotel("Almond Tree", "Arawak P.O. Box 332", "Lovely weather", "Mammee Bay", "St. Ann", new LatLng(-78.3665,18.26398)),
    		new Hotel("Altamont Court Hotel", "1-3 Altamont Terrace", "5 Swimming pools", "Kingston 5", "Kinston", new LatLng(-78.3665,18.27204)),
    		new Hotel("Hotel Four Seasons", "18 Ruthven Road", "Kalooki every Tuesday", "Kingston 10", "Kingston", new LatLng(-77.9041,18.3639)),
    		new Hotel("Seaview Villa","Seaview District, Southfield", "Single and Double Occupany available", "Southfield",	"St. Elizabeth", new LatLng(-77.163,18.4268)),
    		new Hotel("Honey Comb",	"Silver Sands Estate", "Complimentary Continental Breakfast", "Duncans", "Trelawney", new LatLng(-77.9114, 18.46007))
    };
	
	final Attraction[] featuredAttractions = {
    		new Attraction("Xayamaca", "The countrys name is derived from an Arawak (aboriginal Indian) word Xaymaca, meaning land of wood and water", "Montego Bay", "Mr. Paul Hastings", "Dalton Hastings", "Howard Cooke Blvd, Freeport", "Montego Bay", "St. James", new String[]{"844-9935"}, new String[]{"Plant Centre", "Complex Grounds", "Gift Shop", "Juice Bar"}),
    		new Attraction("White River Valley", "There is a hidden paradise seven miles outside of bustling Ocho Rios, Jamaica. As you begin your descent, feel peace and pleasure all in one place. This is White River Valley. ", "Ocho Rios", "Daniel Melville", "Vaneka McKenzie", "Cascade", "Endevour", "St. Mary", new String[]{"974-2018","382-6907"}, new String[]{"River Tubing", "Horseback Riding", "Kayaking"} ),
    		new Attraction("Veronica Park", "Veronica Park is available for general public use and offers a wide variety of classes, specialty rooms, a pool and place for clubs and classes to meet", "Montego Bay", "Mr. Brasco Lee", "Michael Lee Chin", "Allsides District", "Waita Bit P.O.", "Trelawney", new String[]{"538-8940", "468-9449"}, null),
    };

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_featured);
      	
		name = (TextView)findViewById(R.id.name);
      	tBox1 = (TextView)findViewById(R.id.desBox1);
      	tBox2 = (TextView)findViewById(R.id.desBox2);
      	
      	type = getIntent().getStringExtra("type");
      	
      	if(type.equals("hotel")){	
			name.setText(featuredHotels[0].getHotelName());
			tBox1.setText(featuredHotels[0].getDescription());
			tBox2.setVisibility(View.INVISIBLE);
			
			for (int i=1;i<featuredHotels.length;i++){
				featuredList.add(featuredHotels[i].getHotelName());
			}
      	}
      	else{
      		if(type.equals("attraction"))
      		{
      			name.setText(featuredAttractions[0].getName());
    			tBox2.setText(featuredAttractions[0].getDescription());
    			String s = "";
    			for(int i=0;i<featuredAttractions[0].getThemes().length;i++)
    			{
    				s = s + featuredAttractions[0].getThemes()[i] + "\n";
    			}
    			tBox1.setText(s);
    			
    			for (int i=1;i<featuredAttractions.length;i++){
    				featuredList.add(featuredAttractions[i].getName());
    			}
      		}
      	}
			
			listAdapter = new ArrayAdapter<String>(this,
			        android.R.layout.simple_list_item_1, featuredList);
			
			setListAdapter(listAdapter);
		
		seeMore = (Button)findViewById(R.id.button1);
		seeMore.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent myIntent = new Intent();
				myIntent.setClass(FeaturedFragment.this, DetailsFragment.class);
				if(type.equals("hotel")){
					myIntent.putExtra("name", featuredHotels[0].getHotelName());
					myIntent.putExtra("type", type);}
				else
					if(type.equals("attraction")){
						myIntent.putExtra("name", featuredAttractions[0].getName());
						myIntent.putExtra("type", type);}
			    startActivity(myIntent);
			}
			
		});
	}
		
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
				 //TODO Auto-generated method stub
				 //Go to details of hotel
				Intent myIntent = new Intent();
				myIntent.setClass(this, DetailsFragment.class);
				if(type.equals("hotel")){
					myIntent.putExtra("name", featuredHotels[position+1].getHotelName());
					myIntent.putExtra("type", "hotel");}
				else
					if(type.equals("attraction")){
						myIntent.putExtra("name", featuredAttractions[position+1].getName());
						myIntent.putExtra("type", "attraction");}
				
			    startActivity(myIntent);
			}
	}


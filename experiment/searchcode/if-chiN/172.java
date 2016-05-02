package com.example.hotellocator;

import com.google.android.gms.maps.model.LatLng;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class DetailsFragment extends Activity {

	String name,type;
	String desc = "This hotel in Jamaica has an enviable reputation as the Caribbean's premier business and leisure hotel. They are in the vicinity of major businesses, attractions and entertainment in Jamaica." + 
	" The world-class facilities, competitive rates and warm staff make it the first choice for leisure and business." + 
	" Captivating and carefree, richly appointed and steeped in tradition, they take you away and bring you closer together. Long one of the most beautiful and elegant boutique resorts in the Caribbean, they cater, with impeccable service and warm Jamaican hospitality, to the needs of its sophisticated guestsfamilies, couples, and friends.";
	TextView featureName;
	TextView description;
	TextView address;
	TextView themes;
	TextView rating;
	Button mapButton, callButton;
	RatingBar rbar;
	int position = -1;
	ImageView image;
	
	final Hotel[] hotels = {
			new Hotel("Pegasus", "7 Knutsford Boulevard", desc, "Kingston", "Kingston", new LatLng(25.002856, -76.795659)),
			new Hotel("Akwaaba", "Silver Sands Estates, Duncans",desc, "Duncans", "Trelawney", new LatLng(-78.3671, 18.26805)),
    		new Hotel("Alamanda Inn", "39 Salem Beach, P.O. Box 65", desc, "Runaway Bay", "St. Ann", new LatLng(-78.3669,18.27206)),
    		new Hotel("Alfred Ocean Place",	"Norman Manley Blvd. P.O. Box", desc, "Negril", "Westmoreland", new LatLng(-78.3669, 18.27165)),
    		new Hotel("Alhambra Inn", "1 Tucker Avenue", "Close to city", "Kingston 3", "Kingston", new LatLng(-78.3666,18.26627)),
    		new Hotel("All Seasons Beach Resort", "Barrett  Hall, Greenwood, Lit", "Nice sandy beaches", "Greenwood", "St. James", new LatLng(-78.3666,18.26356)),
    		new Hotel("Almond Tree", "Arawak P.O. Box 332", "Lovely weather", "Mammee Bay", "St. Ann", new LatLng(-78.3665,18.26398)),
    		new Hotel("Altamont Court Hotel", "1-3 Altamont Terrace", "5 Swimming pools", "Kingston 5", "Kinston", new LatLng(-78.3665,18.27204)),
    		new Hotel("Hotel Four Seasons", "18 Ruthven Road", desc, "Kingston 10", "Kingston", new LatLng(-77.9041,18.3639)),
    		new Hotel("Hotel Riu", "Tropical Bay Bloody Bay Negril", "Caters to Spanish speakers as well", "Negril", "Hanover", new LatLng(-77.8975,18.47607)),
    		new Hotel("Blue Harbour","P.O. Box 50", "Best Jerk Spot within 5 minutes", "Port Maria","St. Mary",new LatLng(-78.354,18.24547)),
    		new Hotel("Seaview Villa","Seaview District, Southfield", "Single and Double Occupany available", "Southfield",	"St. Elizabeth", new LatLng(-77.163,18.4268)),
    		new Hotel("Tia Maria Villa", "136 Silver Sands Estate, P.O.", "Dancing every other Wednesday", "Duncans", "Trelawney", new LatLng(-77.0723, 18.41486)),
    		new Hotel("Honey Comb",	"Silver Sands Estate", "Complimentary Continental Breakfast", "Duncans", "Trelawney", new LatLng(-77.9114, 18.46007))
    };
	
	final Attraction[] attractions = {
    		new Attraction("Xayamaca", "The countrys name is derived from an Arawak (aboriginal Indian) word Xaymaca, meaning land of wood and water","Montego Bay", "Mr. Paul Hastings", "Dalton Hastings", "Howard Cooke Blvd, Freeport", "Montego Bay", "St. James", new String[]{"844-9935"}, new String[]{"Plant Centre", "Complex Grounds", "Gift Shop", "Juice Bar"}),
    		new Attraction("White River Valley", "There is a hidden paradise seven miles outside of bustling Ocho Rios, Jamaica. As you begin your descent, feel peace and pleasure all in one place. This is White River Valley. ", "Ocho Rios", "Daniel Melville", "Vaneka McKenzie", "Cascade", "Endevour", "St. Mary", new String[]{"974-2018","382-6907"}, new String[]{"River Tubing", "Horseback Riding", "Kayaking"} ),
    		new Attraction("Veronica Park", "Veronica Park is available for general public use and offers a wide variety of classes, specialty rooms, a pool and place for clubs and classes to meet", "Montego Bay", "Mr. Brasco Lee", "Michael Lee Chin", "Allsides District", "Waita Bit P.O.", "Trelawney", new String[]{"538-8940", "468-9449"}, null),
    		new Attraction("Two Sister's Cave", "scary", "Kingston", "UCD", "Mrs. Winsome Roache", "Hellshire", null, "St. Catherine", null, new String[]{"Arawak Cave"}),
    		new Attraction("Bob Marley Museum", "Historic", "Kingston", "Jacqueline Stewart", "Jacqueline Stewart", "56 Hope Road", "Kingston 6", "St. Andrew", new String[]{"927-9152"}, new String[]{"Tour of the former home of Bob Marley"}),
    		new Attraction("Caymanas Track Ltd.", "Caymanas Park is Jamaica's only race track.", "Kingston", "Mr. Michael Sibbles"," Mr. Michael Sibbles", "Caymanas Park Complex", "Gregory Park P.O.", "St. Catherine", new String[]{"988-2523", "988-2524", "988-2525", "988-2526"}, new String[]{"Horse Racing", "Children Entertainment", "Dining"}),
    		new Attraction("KOOL RUNNINGS WATER PARK", "Delightful Negril vacation spot", "Negril",	"POINCIANA RESORTS Ltd", "MRS. NEKEISHA MYRIE-KOZER", "NORMAN MANLEY BLVD", null, null, new String[]{"957-5400", "957-5620"}, new String[]{"WATER SLIDES", "GO KARTS", "DINING", "CONFERENCE", "PAINT BALL", "RAFTING"})
    };
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		
		
		type = getIntent().getStringExtra("type");
		name = getIntent().getStringExtra("name");
		if(type.equals("hotel")){
			for(int i=0;i<hotels.length;i++)
			{
				if(hotels[i].getHotelName().equals(name))
				{
					position=i;
					break;
				}
			}
		}
		else{
			System.out.println(type);
			for(int i=0;i<attractions.length;i++)
			{
				if(attractions[i].getName().equals(name))
				{
					position=i;
					break;
				}
			}
		}
		
		if(position>-1)
		{
			featureName = (TextView)findViewById(R.id.name);
			description = (TextView)findViewById(R.id.hotelDescription);
			address = (TextView)findViewById(R.id.hotelAddress);
			rating = (TextView)findViewById(R.id.hotelRating);
			rbar = (RatingBar) findViewById(R.id.ratingBar1);
			themes = (TextView)findViewById(R.id.themes);
			image = (ImageView)findViewById(R.id.imageView1);
			
			if(type.equals("hotel"))
			{
				featureName.setText(hotels[position].getHotelName());
				description.setText(hotels[position].getDescription());
				address.setText(hotels[position].getAddress());
				rbar.setRating(hotels[position].getAverageUserRating());
				int imagePath = setImage(name);
				if(imagePath!=0)
					image.setImageResource(imagePath);
			}
			else
				if(type.equals("attraction")){
					featureName.setText(attractions[position].getName());
					description.setText(attractions[position].getDescription());
					address.setText("Address: " + attractions[position].getAddress());
					String s = "Attractions:";
					if(attractions[position].getThemes()!=null){
	    			for(int i=0;i<attractions[position].getThemes().length;i++)
	    			{
	    				s = s + "\n" + attractions[position].getThemes()[i];
	    			}
	    			themes.setText(s);}
					rbar.setRating(0);
			}}
			
			mapButton = (Button)findViewById(R.id.mapButton);
			callButton = (Button)findViewById(R.id.contactButton);
			addListenerOnRatingBar(position);
			
			//rbar initially user rating
			
		mapButton = (Button)findViewById(R.id.mapButton);
		mapButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//send LatLng at hotel
				double coord[] = {hotels[position].getHotelCoordinates().latitude,hotels[position].getHotelCoordinates().longitude};
				Bundle b = new Bundle();
				b.putDoubleArray("latlng", coord) ;
				Intent myIntent = new Intent(DetailsFragment.this,
						GoogleMapActivity.class).putExtras(b);
			    startActivity(myIntent);	
			}
		});

//		mapButton.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				//send LatLng at hotel
//				Intent myIntent = new Intent(DetailsFragment.this,
//						GoogleMapActivity.class).putExtra("latlng", hotels[position].getHotelCoordinates());
//			    startActivity(myIntent);	
//			}
//		});

		mapButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//send LatLng at hotel
				Intent myIntent = new Intent(DetailsFragment.this,
						GoogleMapActivity.class).putExtra("latlng", hotels[position].getHotelCoordinates());
			    startActivity(myIntent);	
			}
		});

		callButton.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//send LatLng at hotel
				if(type.equals("hotel")){
					Toast.makeText(DetailsFragment.this,
							"No contact number",
								Toast.LENGTH_SHORT).show();
				}
				else{
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:"+attractions[position].getContact()[0])); 
				startActivity(callIntent);
				}
				
			}
			
		});
		}
	
	public int setImage(String name)
	{
		if (name.equals("Pegasus"))
			return R.drawable.pegasus;
		else
			if(name.equals("Four Seasons"))
				return R.drawable.four_seasons;
			else
				if(name.equals("Hotel Riu"))
					return R.drawable.riu;
				else
					return 0;
	}
	
	public void addListenerOnRatingBar(final int position) {
		 
		//if rating value is changed,
		//display the current rating value in the result (textview) automatically
		rbar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			public void onRatingChanged(RatingBar ratingBar, float rating,
				boolean fromUser) {
				
				Toast.makeText(DetailsFragment.this,
						"Your feedback is appreciated",
							Toast.LENGTH_SHORT).show();
				float avgRating = ((ratingBar.getRating() + (hotels[position].getAverageUserRating() * hotels[position].getNumUserRatings()))/(hotels[position].getNumUserRatings()+1));
				hotels[position].setAverageUserRating(avgRating);
			}
		});
	  }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.details, menu);
		return true;
	}

}


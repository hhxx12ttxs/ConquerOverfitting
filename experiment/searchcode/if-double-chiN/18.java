package com.example.hotellocator;

import java.util.Locale;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Configure the search info and add any event listeners
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.

        	Fragment fragment = null;
        	if(position==0)
        		fragment = new HomeFragment();
        	else
        		if(position==1)
        			fragment = new SearchFragment();
        		else
        			fragment = new Favourites();
//	            	Bundle args = new Bundle();
//	            	args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
//	            	((Fragment) fragment).setArguments(args);
//        	switch(position){
//        		case 0: fragment = new HomeFragment();
//        				break;
//        		case 1:	fragment = new SearchFragment();
//        				break;
//        		case 2: fragment = new GoogleMapFragment();
//        				break;
//        		default:fragment = new DummySectionFragment();
//                	Bundle args = new Bundle();
//                	args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
//                	((Fragment) fragment).setArguments(args);
 //       	}
        	//            
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    //return getString(R.string.title_section1).toUpperCase(l);
                    return "Home";
                case 1:
                    return "All";
                case 2:
                	return "Favourites";
            }
            return null;
        }
    }
    
    public static class DummySectionFragment extends Fragment {
      /**
       * The fragment argument representing the section number for this
       * fragment.
       */
      public static final String ARG_SECTION_NUMBER = "section_number";

      public DummySectionFragment() {
      }

      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container,
              Bundle savedInstanceState) {
      	
      	View rootView = null;
      	rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
          	TextView dummyTextView = (TextView) rootView.findViewById(R.id.textView1);
          	dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
      	return rootView;
      }
  }

}


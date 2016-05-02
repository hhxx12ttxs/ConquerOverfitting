package uk.co.tradejack.henchman;

import java.util.Random;

import uk.co.tradejack.henchman.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class DiceBagActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice_bag);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_dice_bag, menu);
        return true;
    }
    
    public void onRoll(View view)
    {
    	try {
        	int numDice = Integer.valueOf(((EditText) findViewById(R.id.numDice)).getText().toString());
        	int diceType = Integer.valueOf(((EditText) findViewById(R.id.diceType)).getText().toString());
        	if(numDice > 0 && diceType > 1)
        	{
        		StringBuffer result = new StringBuffer("(");
        		int total = 0;
        		Random r = new Random();  // HACK awful.
        		for(int i = 0; i < numDice; i++)  // I miss Python.
        		{
        			int roll = 1 + r.nextInt(diceType);
        			result.append(roll).append(", ");
        			total += roll;
        		}
        		result.append(") ").append(total);
        		TextView rollResult = (TextView) findViewById(R.id.rollResult);
        		rollResult.setText(result.toString());
        	}
    	}
    	catch(NumberFormatException nfx) {
    		throw new IllegalStateException("Oh my! " + nfx.getMessage()); // Do nothing. No skin off our nose. (This is bad [D]esign.)
    	}
    }
}


package ba.bitcamp.bitNavigator.lists;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ba.bitcamp.bitNavigator.models.WorkingHours;

/**
 * Created by hajrudin.sehic on 29/10/15.
 */
public class WorkingHoursList {

    private static ba.bitcamp.bitNavigator.lists.WorkingHoursList mHoursList;

    public static ba.bitcamp.bitNavigator.lists.WorkingHoursList getInstance() {
        if (mHoursList == null) {
            mHoursList = new ba.bitcamp.bitNavigator.lists.WorkingHoursList();
        }
        return mHoursList;
    }

    private List<WorkingHours> hoursList;

    private WorkingHoursList() {
        hoursList = new ArrayList<>();
    }

    public void add(WorkingHours hours) {
        hoursList.add(hours);
    }

    public WorkingHours get(int index) {
        return hoursList.get(index);
    }

    public int getSize() {
        return hoursList.size();
    }

    public List<WorkingHours> getHoursList(){
        return hoursList;
    }

    public WorkingHours getByPlaceId(Integer id){
        WorkingHours hours;
        Log.e("s", mHoursList.getSize() + "");
        for(int i = 0; i < mHoursList.getSize(); i++){
            if(mHoursList.getHoursList().get(i).getPlace_id().equals(id)){
                hours = mHoursList.getHoursList().get(i);
                return hours;
            }
        }
        return  null;
    }




}


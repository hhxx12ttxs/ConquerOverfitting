package fr.insa.whatodo.model;

/**
 * Created by Segolene on 16/03/2015.
 */
public class HourFilter extends Filter {

    protected int mBeginHours;
    protected int mBeginMinutes;
    protected int mEndHours;
    protected int mEndMinutes;

    public HourFilter() {
        super(FilterType.AGE);
        mBeginHours =0;
        mBeginMinutes =0;
        mEndHours =23;
        mEndMinutes =59;
    }


    public int getBeginHours() {
        return mBeginHours;
    }

    public int getBeginMinutes() {
        return mBeginMinutes;
    }

    public int getEndHours() {
        return mEndHours;
    }

    public int getEndMinutes() {
        return mEndMinutes;
    }

    public void setBeginHours(int beginHours, int beginMinutes) {

        mBeginHours = beginHours;
        mBeginMinutes = beginMinutes;

        if(mEndHours < mBeginHours || (mEndHours == mBeginHours && mEndMinutes < mBeginMinutes)){
            mEndHours = mBeginHours;
            mEndMinutes = mBeginMinutes;
        }
    }



    public boolean setEndHours(int endHours, int endMinutes) {
        if(endHours> mBeginHours || (endHours== mBeginHours && endMinutes > mBeginMinutes)){
            mEndHours = endHours;
            return true;
        }
        mEndHours= mBeginHours;
        mEndMinutes= mBeginMinutes;
        return false;
    }


}


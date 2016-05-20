/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dslab1.Auction;

import java.util.Date;

/**
 * hello flo
 * @author daniel
 */
public class Auction {
    
    
    
    private long mId;
    private String mDescription;
    private String mOwnerName;
    private Date mDate;             /*is the cration date of the auction, at the time when the server received the auction*/
    private int mDuration;
    private Double mHighestBid;
    private String mHighestBidderName;
    
    private long mLastBidTime = 0;
    
    public Auction(long id, String description, String ownerName, Date date, int duration){
        mId = id;
        mDescription = description;
        mOwnerName = ownerName;
        mDate = date;
        mDuration = duration;
        
        mHighestBid = 0.00;
        mHighestBidderName = "none";
    }

    
    public long getLastBidTime(){
    	return mLastBidTime;
    }
    
    public long getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }


    public String getOwnerName() {
        return mOwnerName;
    }

    public Date getDate() {
        return mDate;
    }


    public int getDuration() {
        return mDuration;
    }


    public String getHighestBidString(){
        if(mHighestBid == 0){
            return "0.00";
        }else{
            return mHighestBid.toString();
        }
    }
    public Double getHighestBid() {
        return mHighestBid;
    }

    public void setHighestBid(Double highestBid) {
        mLastBidTime = 0;
    	this.mHighestBid = highestBid;
        
    }

    public void setHighestBidAndBidTime(Double highestBid, long bidTime) {
        this.mHighestBid = highestBid;
        mLastBidTime = bidTime;
    }
    
    public String getHighestBidderName() {
        return mHighestBidderName;
    }

    

    public Date getEndDate() {
        return new Date(mDate.getTime() + 1000*mDuration);
    }


    public void setHighestBidderName(String name) {
        mHighestBidderName = name;
    }
    
    
    
}


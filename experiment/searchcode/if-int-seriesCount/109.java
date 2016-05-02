package com.intelerad.imageviewer.gwt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A basic study class containing the search result info at the study level.
 */
@SuppressWarnings("serial")
public class GwtBasicStudy<S extends GwtBasicSeries> implements Serializable
{
    protected String mPatientId;
    protected String mPatientName;
    protected String mPatientBirthDate;
    protected String mPatientSex;
    protected String mStudyInstanceUid;
    protected String mAccessionNumber;
    protected Date   mStudyDate;
    protected String mModality;
    protected int    mSeriesCount;
    protected String mStudyDescription;
    protected String mReferringPhysician;
    protected String mOrganization;
    protected String mRequestedProcedureId;
    protected List<S> mSeriesList;
    protected String mDestination;
    protected boolean mMoveResult;
    
    // The most recently saved presentation state for this study.
    protected GwtBasicSeries mMostRecentlySavedPsSeries;
    
    // Whether or not the study has been manually granted to the current user
    private boolean mManualGrant;
    
    public GwtBasicStudy()
    {
        mSeriesList = new ArrayList<S>();
    }
    
    public GwtBasicStudy( String studyInstanceUid )
    {
        this();
        mStudyInstanceUid = studyInstanceUid;
    }
     
    public GwtBasicStudy( GwtBasicStudy<S> basicStudy )
    {
        this();
        copyStudyInfo( basicStudy );
        
        mSeriesList = new ArrayList<S>( basicStudy.getSeriesList() );
        setMostRecentlySavedPsSeries();
    }
    
    public void copyStudyInfo( GwtBasicStudy basicStudy  )
    {
        mPatientId = basicStudy.getPatientId();
        mPatientName = basicStudy.getPatientName();
        mPatientBirthDate = basicStudy.getPatientBirthDate();
        mPatientSex = basicStudy.getPatientSex();
        mStudyInstanceUid = basicStudy.getStudyInstanceUid();
        mAccessionNumber = basicStudy.getAccessionNumber();
        mStudyDate = basicStudy.getStudyDate();
        mModality = basicStudy.getModality();
        mSeriesCount = basicStudy.getSeriesCount();
        mStudyDescription = basicStudy.getStudyDescription();
        mReferringPhysician = basicStudy.getReferringPhysician();
        mOrganization = basicStudy.getOrganization();
        mRequestedProcedureId = basicStudy.getRequestedProcedureId();
        mMostRecentlySavedPsSeries = null;
        mSeriesList = new ArrayList<S>();
        mDestination = basicStudy.getDestination();
        mMoveResult = basicStudy.getMoveResult();
    }
    
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        
        GwtBasicStudy<S> other = (GwtBasicStudy<S>) obj;
        return mStudyInstanceUid.equals( other.mStudyInstanceUid );
    }
    
    @Override
    public int hashCode()
    {
        return mStudyInstanceUid.hashCode();
    }
    
    public String getPatientId()
    {
        return mPatientId;
    }

    public void setPatientId( String patientId )
    {
        mPatientId = patientId;
    }

    public String getPatientName()
    {
        return mPatientName;
    }

    public void setPatientName( String patientName )
    {
        mPatientName = patientName;
    }

    public String getAccessionNumber()
    {
        return mAccessionNumber;
    }

    public void setAccessionNumber( String accessionNumber )
    {
        mAccessionNumber = accessionNumber;
    }

    public Date getStudyDate()
    {
        return mStudyDate;
    }

    public void setStudyDate( Date studyDate )
    {
        mStudyDate = studyDate;
    }

    public String getPatientBirthDate()
    {
        return mPatientBirthDate;
    }

    public void setPatientBirthDate( String patientBirthDate )
    {
        mPatientBirthDate = patientBirthDate;
    }

    public String getPatientSex()
    {
        return mPatientSex;
    }

    public void setPatientSex( String patientSex )
    {
        mPatientSex = patientSex;
    }

    public String getModality()
    {
        return mModality;
    }

    public void setModality( String modality )
    {
        mModality = modality;
    }

    public int getSeriesCount()
    {
        return mSeriesCount;
    }

    public void setSeriesCount( int seriesCount )
    {
        mSeriesCount = seriesCount;
    }

    public String getStudyDescription()
    {
        return mStudyDescription;
    }

    public void setStudyDescription( String studyDescription )
    {
        mStudyDescription = studyDescription;
    }

    public String getStudyInstanceUid()
    {
        return mStudyInstanceUid;
    }

    public void setStudyInstanceUid( String studyInstanceUid )
    {
        mStudyInstanceUid = studyInstanceUid;
    }

    public List<S> getSeriesList()
    {
        return mSeriesList;
    }

    public void setSeriesList( List<S> seriesList )
    {
        mSeriesList = seriesList;
        setMostRecentlySavedPsSeries();
    }
    
    public String getReferringPhysician()
    {
        return mReferringPhysician;
    }

    public void setReferringPhysician( String referringPhysician )
    {
        mReferringPhysician = referringPhysician;
    }

    public String getOrganization()
    {
        return mOrganization;
    }

    public void setOrganization( String organization )
    {
        mOrganization = organization;
    }

    public String getRequestedProcedureId()
    {
        return mRequestedProcedureId;
    }

    public void setRequestedProcedureId( String requestedProcedureId )
    {
        mRequestedProcedureId = requestedProcedureId;
    }

    /**
     * @return Whether or not the study has been manually granted to the current user.
     */
    public boolean isManualGrant()
    {
        return mManualGrant;
    }

    /**
     * Set whether or not the study has been manually granted to the current user.
     */
    public void setManualGrant( boolean manualGrant )
    {
        mManualGrant = manualGrant;
    }
    
    public Set<String> getAllSeriesInstanceUids()
    {
        Set<String> allSeriesInstanceUids = new HashSet<String>();
        for ( GwtBasicSeries series : mSeriesList )
        {
            allSeriesInstanceUids.add( series.getSeriesInstanceUid() );
        }
        return allSeriesInstanceUids;
    }
    
    /**
     * Find the most recently saved presentation state series.
     * 
     * @return null if the study has no presentation state series
     */
    public String getMostRecentlySavedPsSeriesInsUid()
    {
        if ( mMostRecentlySavedPsSeries != null )
            return mMostRecentlySavedPsSeries.getSeriesInstanceUid();
        return null;
    }
    
    /**
     * Updates the presentation state series that is associated with this study.
     * 
     * @param series A candidate for most recently saved presentation state series.
     */
    private void setMostRecentlySavedPsSeries()
    {
        GwtBasicSeries mostRecent = null;
        
        for ( GwtBasicSeries series : mSeriesList )
        {
            if ( series.isPresentationStateSeries() && series.getSeriesDate() != null )
            {
                if ( mostRecent == null )
                {
                    mostRecent = series;
                }
                else
                {
                    if ( series.getSeriesDate().after( mostRecent.getSeriesDate() ) )
                        mostRecent = series;
                }
            }
        }
        
        mMostRecentlySavedPsSeries = mostRecent;
    }
    
    public void setDestination( String destination )
    {
    	mDestination = destination;
    }
    
    public String getDestination()
    {
    	return mDestination;
    }
    
    public void setMoveResult( boolean success )
    {
    	mMoveResult = success;
    }
    
    public boolean getMoveResult()
    {
    	return mMoveResult;
    }
    
    
    public static GwtStudyComparator getDefaultComparator()
    {
        return new GwtStudyComparator();
    }
}


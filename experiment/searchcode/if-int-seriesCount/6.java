package com.intelerad.android.portal.rpc;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.intelerad.android.portal.PortalApplication;
import com.intelerad.android.portal.R;
import com.intelerad.android.portal.models.Session;
import com.intelerad.datamodels.reportworkflow.PriorityLevelColor;
import com.intelerad.datamodels.reportworkflow.portal.EventType;
import com.intelerad.datamodels.reportworkflow.portal.NotificationCounts;
import com.intelerad.imageviewer.gwt.model.DefaultImageUrlEncoder;
import com.intelerad.imageviewer.gwt.model.GwtDataset;
import com.intelerad.imageviewer.gwt.model.GwtImageRenderParams;
import com.intelerad.imageviewer.gwt.model.GwtOverlayData;
import com.intelerad.imageviewer.gwt.model.GwtPacsImage;
import com.intelerad.imageviewer.gwt.model.GwtSeries;
import com.intelerad.imageviewer.gwt.model.GwtStudy;
import com.intelerad.imageviewer.gwt.model.GwtStudyGroup;
import com.intelerad.web.lib.gwt.model.GwtAge;
import com.intelerad.web.lib.gwt.model.GwtAge.GwtAgeUnit;
import com.intelerad.web.lib.gwt.model.GwtCriticalResult;
import com.intelerad.web.lib.gwt.model.GwtImpression;
import com.intelerad.web.lib.gwt.model.GwtPriorityCode;
import com.intelerad.web.lib.gwt.model.GwtPriorityLevel;
import com.intelerad.web.lib.gwt.model.hl7.GwtBasicOrder;
import com.intelerad.web.lib.gwt.model.hl7.GwtBasicOrder.OrderStatus;
import com.intelerad.web.lib.gwt.model.hl7.GwtOrder;
import com.intelerad.web.lib.gwt.model.hl7.GwtOrder.ImageAvailability;
import com.intelerad.web.lib.gwt.model.hl7.GwtPatient;
import com.intelerad.web.lib.gwt.model.hl7.GwtProcedure;
import com.intelerad.web.lib.gwt.model.hl7.GwtReport;
import com.intelerad.web.portal.gwt.model.GwtDictation;
import com.intelerad.web.portal.gwt.model.GwtNotification;
import com.intelerad.web.portal.gwt.model.GwtPortalCase;
import com.intelerad.web.portal.gwt.model.GwtPortalUser;
import com.intelerad.web.portal.gwt.model.NotificationFilter;
import com.intelerad.web.portal.gwt.model.NotificationResults;
import com.intelerad.web.portal.gwt.model.SearchResults;

public class FakeSource implements DataSource
{
    private static final long SLEEP_TIME = 2000;
    
    private final Map<Integer, GwtPortalUser> mRadiologistMap = new HashMap<Integer, GwtPortalUser>();
    private final Map<Integer, GwtPortalUser> mReferringPhysMap = new HashMap<Integer, GwtPortalUser>();
    private final Iterator<String> PATIENT_IDS = FakeData.PATIENT_IDS.iterator();
    private final Iterator<String> ACCESSION = FakeData.ACCESSION_NUMBERS.iterator();
    
    private final Map<String, Session> mSessions = new HashMap<String, Session>();
    private final Map<String, GwtPatient> mPatientMap = new HashMap<String, GwtPatient>();
    private final Map<String, GwtOrder> mOrderMap = new HashMap<String, GwtOrder>();
    private final Map<String, GwtStudy> mStudyMap = new HashMap<String, GwtStudy>();

//    private final Map<String, GwtPortalCase> mCaseMap = new HashMap<String, GwtPortalCase>();
    private final LinkedList<GwtNotification> mAllNotifications = new LinkedList<GwtNotification>();
    
    public FakeSource()
    {
        int numberOfUsers = 10;
        int numberOfPatients = 30; //FakeData.PATIENT_IDS.size();
        
        for ( int index = 0; index < numberOfUsers; index++ )
        {
            GwtPortalUser user = createBasicUser();
            if ( index % 2 == 0 )
                mRadiologistMap.put( user.getUserId(), user );
            else
                mReferringPhysMap.put( user.getUserId(), user );
        }
        
        for ( int index = 0; index < numberOfPatients; index++ )
        {
            // populate the maps with fake data
            GwtPatient patient = createRandomPatient();
            mPatientMap.put( patient.getPatientId(), patient );
            
            for ( int i = 0; i < 2; i++ )
            {
                GwtOrder order = createRandomOrder( patient );
                mOrderMap.put( order.getAccessionNumber(), order );
                
                GwtNotification notification = createNotification( order );
                if ( notification != null )
                    mAllNotifications.add( notification );
                
                ImageAvailability availability = order.getImageAvailability();
                if ( ! ImageAvailability.NO_IMAGES.equals( availability ) )
                {
                    GwtStudy study = createRandomStudy( order );
                    mStudyMap.put( study.getAccessionNumber(), study );
                }
            }
        }
    }
    
    @Override
    public Session login( String username, String password, String spec, String host ) throws Exception
    {
        Thread.sleep( SLEEP_TIME );
        
        Session session = mSessions.get( username );
        if ( session != null )
            return session;
            
        session = new Session.Builder( spec,
                                       host,
                                       username,
                                       password,
                                       "permutation",
                                       "strongName",
                                       Arrays.asList( "sessionId" ) ).build();

        mSessions.put( username, session );
        
        return session;
    }

    @Override
    public GwtStudyGroup getStudies( Session session, String accessionNumber ) throws Exception
    {
        Thread.sleep( SLEEP_TIME );
        checkSession( session );
        
        GwtStudyGroup group = new GwtStudyGroup();
        GwtStudy study = mStudyMap.get( accessionNumber );

        if ( study != null )
            group.addStudy( study );
        
        return group;
    }

    /**
     * Returns results based on a patient's id, and name
     */
    @Override
    public SearchResults getSearchResults( Session session, String searchString ) throws Exception
    {
        Thread.sleep( SLEEP_TIME );
        checkSession( session );
        
        List<GwtPatient> patients = new ArrayList<GwtPatient>();
        for ( GwtPatient patient : mPatientMap.values() )
        {
            if ( matches( patient.getPatientId(), searchString ) || matches( patient.getFirstName(), searchString ) || matches( patient.getLastName(), searchString )  )
                patients.add( patient );
        }
            
        SearchResults results = new SearchResults();
        results.setPatients( patients );
        results.setTotalResultCount( patients.size() );
        
        return results;
    }

    @Override
    public GwtPatient getPatient( Session session, String patientId ) throws Exception
    {
        Thread.sleep( SLEEP_TIME );
        checkSession( session );
        
        return mPatientMap.get( patientId );
    }

    /**
     * TODO: The returned {@link GwtPortalCase} can be improved
     */
    @Override
    public GwtPortalCase getCase( Session session, String accessionNumber ) throws Exception
    {
        Thread.sleep( SLEEP_TIME );
        checkSession( session );
        
        GwtOrder order = mOrderMap.get( accessionNumber );
        if ( order == null )
            return null;
        
        GwtPortalCase portalCase = new GwtPortalCase();
        portalCase.setCannedImpressions( new ArrayList<String>() );
        portalCase.setDiagnosticWarningDisplayed( false );
        portalCase.setDictations( new ArrayList<GwtDictation>() );
        portalCase.setOrder( order );
        portalCase.setPatientOrderCount( 0 );
        GwtReport report = new GwtReport();
        report.setObservationValue( "" );
        portalCase.setReport( report );
        
        return portalCase;
    }

    /**
     * TODO: The returned {@link NotificationResults} needs to be improved to contain some coherent
     * data
     */
    @Override
    public NotificationResults getNotificationResults( Session session,
                                                       int itemCount,
                                                       NotificationFilter filter ) throws Exception
    {
        Thread.sleep( SLEEP_TIME );
        checkSession( session );
        
        NotificationResults results = new NotificationResults();
        NotificationCounts counts = new NotificationCounts();
        counts.setCriticalCount( 0 );
        counts.setCriticalPendingCount( 0 );
        counts.setTotalCount( mAllNotifications.size() );
        counts.setUnviewedCount( mAllNotifications.size() );
        results.setNotificationCounts( counts );
        
        results.setNotifications( mAllNotifications );
        
        return results;
    }
    
    @Override
    public byte[] getImage( Session session, String url ) throws Exception
    {
        Thread.sleep( 33 );
        checkSession( session );
        
        Context context = PortalApplication.getAppContext();
        Drawable drawable = context.getResources().getDrawable( R.drawable.series );
        Bitmap bitmap = ( (BitmapDrawable) drawable ).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, stream );

        return stream.toByteArray();
    }
    
    /********************************************************************************************/
    
    private boolean matches( String input, String search )
    {
        return input.toLowerCase().indexOf( search.toLowerCase() ) != -1;
    }
    
    private void checkSession( Session session )
    {
        if ( mSessions.containsKey( session.getUsername() ) )
            return;
        
        throw new IllegalStateException( "The session has become invalid " + session );
    }
    
    private String getAccessionNumber()
    {
        return next( ACCESSION );
    }    

    private String getPatientId()
    {
        return next( PATIENT_IDS );
    }

    private GwtNotification createNotification( GwtOrder order )
    {
        int number = getRandomNumber( 100 );
        if ( number < 85 )
            return null;
        
        GwtNotification notification = new GwtNotification();
        notification.setEventTime( getRandomDate() );
        notification.setOrder( order );
        notification.setViewed( false );
        EventType eventType = EventType.values()[getRandomNumber( EventType.values().length )];
        notification.setEventType( eventType );
        
        return notification;
    }
    
    private GwtPatient createRandomPatient()
    {
        GwtPatient patient = new GwtPatient();
        patient.setFirstName( getName( FakeData.FIRST_NAMES ) );
        patient.setLastName( getName( FakeData.LAST_NAMES ) );
        patient.setMiddleName( getName( FakeData.FIRST_NAMES ) );
        
        patient.setAge( new GwtAge( 1 + getRandomNumber( 100 ), GwtAgeUnit.YEAR ) );
        patient.setDateOfBirth( "" );
        patient.setGender( getRandomSex() );
        patient.setOrders( new ArrayList<GwtBasicOrder>() );
        patient.setPatientId( getPatientId() );
        
        return patient;
    }

    private static String getName( String[] list )
    {
        StringBuilder s1 = new StringBuilder( getRandom( list ) );
        s1.replace( 0, s1.length(), s1.toString().toLowerCase() );
        s1.setCharAt( 0, Character.toTitleCase( s1.charAt( 0 ) ) );
        
        return s1.toString();
    }
    
    /**
     * TODO: Improve the location and organization
     * TODO: Improve the critical results
     * TODO: Improve the Impressions
     * TODO: Improve the order status and priority 
     * TODO: Reason for study can be improved
     */
    private GwtOrder createRandomOrder( GwtPatient patient )
    {
        GwtOrder order = new GwtOrder();

        String accessionNumber = getAccessionNumber();
        order.setAccessionNumber( accessionNumber );
        
        order.setFirstName( patient.getFirstName() );
        order.setMiddleName( patient.getMiddleName() );
        order.setLastName( patient.getLastName() );
        order.setPatient( patient );
        order.setPatientId( patient.getPatientId() );
        order.setDateOfBirth( patient.getDateOfBirth() );
        order.setGender( patient.getGender() );
        
        GwtCriticalResult criticalResult = createRandomCriticalResult( accessionNumber );
        order.setCriticalResult( criticalResult );
        List<GwtImpression> impressions = createRandomImpressions();
        order.setImpressions( impressions );
        order.setPriorityCode( createRandomPriorityCode() );
        order.setProcedures( createRandomProcedure() );
        
        order.setLocation( "IMS" );
        order.setOrganization( "IMS" );
        
        OrderStatus orderStatus = OrderStatus.values()[ getRandomNumber( OrderStatus.values().length )];
        order.setOrderStatus( orderStatus );
        order.setStatus( orderStatus.getCode() );
        
        order.setPrimaryReferringPhysician( getRandomUser( mReferringPhysMap )  );
        order.setRadiologist( getRandomUser( mRadiologistMap ) );
//        order.setTotalImages( totalImages );
        
        order.setImageAvailability( ImageAvailability.values()[getRandomNumber( ImageAvailability.values().length )] );
        order.setStudyDate( getRandomDate() );
        order.setFillerOrderNumber( getRandomNumber( 1234 ) + "" );
        order.setPlacerOrderNumber( getRandomNumber( 10000 ) + "" );
        order.setVisitNumber( getRandomNumber( 10000 ) + "" );
        order.addReasonForStudy( order.getProcedures().get( 0 ).getStudyDescription() );
        
        patient.getOrders().add( order );
        
        return order;
    }

    private List<GwtImpression> createRandomImpressions()
    {
        int number = getRandomNumber( 100 );
        if ( number < 65 )
            return new ArrayList<GwtImpression>();
        
        GwtImpression impression = new GwtImpression();
        impression.setAuthor( getRandomUser( mRadiologistMap ) );
        impression.setDate( getRandomDate() );
        impression.setDiscrepancy( false );
        impression.setText( getRandom( FakeData.IMPRESSIONS ) );
        
        return Arrays.asList( impression );
    }

    private GwtPortalUser getRandomUser( Map<Integer, GwtPortalUser> map )
    {
        int index = getRandomNumber( map.size() );
        int count = 0;
        for ( GwtPortalUser user : map.values() )
        {
            if ( count == index )
                return user;
            
            count++;
        }
        
        return null;
    }
    
    /**
     * TODO: Improve the randomness of this algorithm
     * TODO: Improve some fields in this object 
     */
    private GwtCriticalResult createRandomCriticalResult( String accessionNumber )
    {
        int number = getRandomNumber( 100 );
        if ( number < 80 )
            return null;
            
        GwtCriticalResult result = new GwtCriticalResult();
        result.setAccessionNumber( accessionNumber );
        result.setAcknowledged( false );
        result.setComment( "" );
        result.setCommunicatedTo( "" );
        result.setFinding( "" );
        result.setPending( true );
        result.setEnteredTime( getRandomDate() );
        result.setCompletedTime( getRandomDate() );
        result.setLastModifiedTime( getRandomDate() );
        
        PriorityLevelColor color = PriorityLevelColor.values()[getRandomNumber( PriorityLevelColor.values().length )];
        result.setLevel( new GwtPriorityLevel( color.name(), color, getRandomNumber( 1000 ) ) );
        
        return result;
    }

    /**
     * TODO: Improve Added Roles and Specialties
     * TODO: Improve address, email, phone number 
     */
    private GwtPortalUser createBasicUser()
    {
        GwtPortalUser user = new GwtPortalUser();
        user.setAddress( "" );
        user.setEmailAddress( "" );
        user.setPhoneNumber( "" );
        user.setIsInteleradUser( false );
        
        user.setRoleNames( new ArrayList<String>() );
        user.setSpecialties( new HashSet<String>() );
        
        String firstName = getName( FakeData.FIRST_NAMES );
        String lastName = getName( FakeData.LAST_NAMES );
        user.setUsername( firstName.toLowerCase() + "." + lastName.toLowerCase() );
        user.setRisId( getRandomNumber( 2000 ) + "" );
        user.setUserId( getRandomNumber( 10000 ) );
        user.setFirstName( firstName );
        user.setLastName( lastName );
        user.setMiddleName( getName( FakeData.FIRST_NAMES ) );
        
        return user;
    }

    private GwtPriorityCode createRandomPriorityCode()
    {
        GwtPriorityCode priorityCode = new GwtPriorityCode();
        String code = getRandom( FakeData.PRIORITY );
        priorityCode.setCode( code );
        priorityCode.setLabel( code );
        priorityCode.setIndex( 1 );
        
        return priorityCode;
    }
    
    private List<GwtProcedure> createRandomProcedure()
    {
        GwtProcedure procedure = new GwtProcedure();
        
        procedure.setModality( getRandom( FakeData.MODALITIES ) );
        procedure.setPriority( getRandom( FakeData.PRIORITY ) );
        procedure.setRequestedProcId( getRandomNumber( 3654789 ) + "" );
        procedure.setSchedProcStepId( getRandomNumber( 1154749 ) + "" );
        procedure.setStudyDescription( getRandom( FakeData.MODALITY_DESCRIPTIONS.get( procedure.getModality() ) ) );
        
        return Arrays.asList( procedure );
    }

    /**
     * TODO: The destination can be improved 
     * TODO: Referring physician can be improved
     * TODO: RPID can be improved
     */
    private GwtStudy createRandomStudy( GwtOrder order )
    {
        GwtStudy study = new GwtStudy();
        study.setAccessionNumber( order.getAccessionNumber() );
        study.setDestination( "destination" );
        study.setManualGrant( false );
        study.setModality( order.getModalityString() );
        study.setMoveResult( true );
        study.setOrganization( order.getOrganization() );
        study.setPatientBirthDate( order.getPatient().getDateOfBirth() );
        study.setPatientId( order.getPatientId() );
        study.setPatientName( order.getPatient().getPrintableName() );
        study.setPatientSex( order.getPatient().getGender() );
        study.setReferringPhysician( order.getReferringPhysicians().get( 0 ).getPrintableName() );
        study.setRequestedProcedureId( order.getRpids().get( 0 ) );
        
        int seriesCount = 1 + getRandomNumber( 15 );
        study.setSeriesCount( seriesCount );
        study.setSeriesList( createRandomSeriesList( order, study, seriesCount ) );
        study.setStudyDate( order.getStudyDate() );
        study.setStudyDescription( order.getStudyDescString() );
        study.setStudyInstanceUid( UUID.randomUUID().toString() );
        
        return study;
    }
    
    /**
     * TODO: destination can be improved
     * TODO: Is never a key series or a presentation state
     * TODO: Owner can be improved
     * TODO: Only adds one dataset to the series
     */
    private List<GwtSeries> createRandomSeriesList( GwtOrder order, GwtStudy study, int seriesCount )
    {
        List<GwtSeries> seriesList = new ArrayList<GwtSeries>();
        
        for ( int count = 0; count < seriesCount; count ++ )
        {
            GwtSeries series = new GwtSeries();
            // Blah, this needs to be the first thing that gets set :(
            series.setSeriesInstanceUid( UUID.randomUUID().toString() ); 
            series.setAvailable( true );
            GwtDataset dataset = createDataset( series );
            series.addDataset( dataset );
            series.setDestination( "destination" );
            series.setImageCount( dataset.getImageCount() );
            series.setKeyImageSeries( false );
            series.setModality( order.getModalityString() );
            series.setMoveResult( true );
            series.setOnline( true );
            series.setOverlayData( createOverlayData( order ) );
            series.setOwner( "owner" );
            series.setParentStudy( study );
            series.setPresentationStateSeries( false );
            series.setSeriesDate( order.getStudyDate() );
            series.setSeriesDescription( order.getReasonForStudy() );
            series.setSeriesNumber( count + "" );
            series.setSourceAe( "sourceAe" );
            seriesList.add( series );
        }
        
        return seriesList;
    }

    private GwtOverlayData createOverlayData( GwtOrder order )
    {
        GwtOverlayData data = new GwtOverlayData();
        data.setPatientBirthdateSexAge( order.getPatient().getDateOfBirth().toString() );
        data.setPatientName( order.getPatient().getPrintableName() );
        data.setStudyDate( order.getStudyDate().toString() );
        data.setStudyDescription( order.getStudyDescString() );
        
        return data;
    }

    private GwtDataset createDataset( GwtSeries series )
    {
        GwtImageRenderParams renderParams = new GwtImageRenderParams();
        renderParams.setActivePsSeriesInsUid( "" );
        renderParams.setActivePsSopInsUid( "" );
        renderParams.setPanX( 0 );
        renderParams.setPanY( 0 );
        renderParams.setWindowCenter( 0.0 );
        renderParams.setWindowWidth( 0.0 );
        renderParams.setZoomFactor( 0.0 );
        
        GwtDataset dataset = new GwtDataset();
        dataset.setParentSeries( series );
        dataset.setPsRenderParams( null );
        dataset.setImages( createImages( dataset ) );
        dataset.setDefaultRenderParams( renderParams );
        
        return dataset;
    }

    private List<GwtPacsImage> createImages( GwtDataset dataset )
    {
        List<GwtPacsImage> images = new ArrayList<GwtPacsImage>();
        for ( int count = 0; count < 1; count++ )
        {
            GwtPacsImage image = new GwtPacsImage( dataset,
                                                   UUID.randomUUID().toString(),
                                                   UUID.randomUUID().toString(),
                                                   new DefaultImageUrlEncoder() );
            images.add( image );
        }
        
        return images;
    }

    private Date getRandomDate()
    {
        long time = System.currentTimeMillis();
        return new Date((long) ( Math.random() * time ));
    }
    
    private String next( Iterator<String> iterator )
    {
        if ( ! iterator.hasNext() )
            return getRandomNumber( 123456 ) + "";
        
        String id = iterator.next();
        iterator.remove();
        
        return id;   
    }
    
    private static String getRandom( String[] listOfNames )
    {
        return listOfNames[ getRandomNumber( listOfNames.length ) ];
    }
    
    public static int getRandomNumber( int max )
    {
        return (int) ( Math.random() * max );
    }
    
    public static String getRandomSex()
    {
        return ( getRandomNumber( 2 ) == 0 ? "M" : "F" );
    }    
}


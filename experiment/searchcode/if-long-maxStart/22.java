/*--------------------------------------------------------------------------*
 | Copyright (C) 2006  Christopher Kohlhaas                                 |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/

package org.rapla.gui.internal.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.rapla.components.util.Assert;
import org.rapla.components.util.DateTools;
import org.rapla.components.xmlbundle.I18nBundle;
import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.Named;
import org.rapla.entities.RaplaObject;
import org.rapla.entities.RaplaType;
import org.rapla.entities.User;
import org.rapla.entities.configuration.CalendarModelConfiguration;
import org.rapla.entities.configuration.Preferences;
import org.rapla.entities.configuration.RaplaMap;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.AppointmentBlock;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.dynamictype.Classifiable;
import org.rapla.entities.dynamictype.Classification;
import org.rapla.entities.dynamictype.ClassificationFilter;
import org.rapla.entities.dynamictype.DynamicType;
import org.rapla.entities.dynamictype.DynamicTypeAnnotations;
import org.rapla.facade.ClientFacade;
import org.rapla.facade.Conflict;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.framework.RaplaLocale;
import org.rapla.plugin.abstractcalendar.AbstractHTMLCalendarPage;
import org.rapla.plugin.autoexport.AutoExportPlugin;
import org.rapla.plugin.export2ical.Export2iCalPlugin;
import org.rapla.plugin.weekview.WeekViewFactory;

public class CalendarModelImpl implements CalendarSelectionModel
{

    Date startDate;
    Date endDate;
    Date selectedDate;
    List selectedObjects = new ArrayList();
    String title;
    int columnSize = 100;
    ClientFacade m_facade;
    String selectedView;
    I18nBundle i18n;
    RaplaContext context;
    RaplaLocale raplaLocale;
    User user;
    Map optionMap = new HashMap();

    boolean defaultEventTypes = true;
    boolean defaultResourceTypes = true;
    
    Map<DynamicType,ClassificationFilter> reservationFilter = new LinkedHashMap<DynamicType, ClassificationFilter>();
    Map<DynamicType,ClassificationFilter> allocatableFilter = new LinkedHashMap<DynamicType, ClassificationFilter>();
    
    public CalendarModelImpl(RaplaContext sm, User user) throws RaplaException {
        this.context = sm;
        this.raplaLocale = (RaplaLocale) sm.lookup(RaplaLocale.ROLE);
        i18n = (I18nBundle)sm.lookup(I18nBundle.ROLE + "/org.rapla.RaplaResources");
        m_facade = (ClientFacade) sm.lookup(ClientFacade.ROLE);
        if ( m_facade.isSessionActive()) {
            user = m_facade.getUser();
        }
        setSelectedDate( m_facade.today());
        DynamicType[] types = m_facade.getDynamicTypes( DynamicTypeAnnotations.VALUE_RESOURCE_CLASSIFICATION);
        if ( types.length == 0 ) {
            types = m_facade.getDynamicTypes( DynamicTypeAnnotations.VALUE_PERSON_CLASSIFICATION);
        }
        setSelectedObjects( Collections.singletonList( types[0]) );
        setViewId( WeekViewFactory.WEEK_VIEW);
        this.user = user;
        if ( user != null && !user.isAdmin()) {
            selectUser( user );
        }
        optionMap.put( AbstractHTMLCalendarPage.SAVE_SELECTED_DATE, "false");
        resetExports();
    }
    
    public void resetExports() 
    {
        setTitle(null);
        setOption( AbstractHTMLCalendarPage.SHOW_NAVIGATION_ENTRY, "true");
        setOption(AutoExportPlugin.HTML_EXPORT, "false");
        setOption(Export2iCalPlugin.ICAL_EXPORT, "false");                       
    }


    private boolean setConfiguration(CalendarModelConfiguration config, final Map alternativOptions) throws RaplaException {
        selectedObjects = new ArrayList();
        allocatableFilter.clear();
        reservationFilter.clear();
        if ( config == null)
        {
            defaultEventTypes = true;
            defaultResourceTypes = true;
            return true;
        }
        else
        {
            defaultEventTypes = config.isDefaultEventTypes();
            defaultResourceTypes = config.isDefaultResourceTypes();
        }
        boolean couldResolveAllEntities = true;
       
        // get filter
        title = config.getTitle();
        selectedView = config.getView();
        //selectedObjects
        optionMap = new HashMap();
        if ( config.getOptionMap() != null)
        {
            optionMap.putAll( config.getOptionMap());
        }
        if (alternativOptions != null )
        {
            optionMap.putAll( alternativOptions);
        } 
        ClassificationFilter[] filter = config.getFilter();
        final String saveDate = (String)optionMap.get( AbstractHTMLCalendarPage.SAVE_SELECTED_DATE);
        if ( config.getSelectedDate() != null && (saveDate == null || saveDate.equals("true"))) {
            setSelectedDate( config.getSelectedDate() );
        }
        else
        {
            setSelectedDate( m_facade.today());
        }
        if ( config.getStartDate() != null) {
            setStartDate( config.getStartDate() );
        }
        if ( config.getEndDate() != null) {
            setEndDate( config.getEndDate() );
        }
        selectedObjects.addAll( config.getSelected());
        for ( ClassificationFilter f:filter)
        {
            final DynamicType type = f.getType();
            final String annotation = type.getAnnotation(DynamicTypeAnnotations.KEY_CLASSIFICATION_TYPE);
            boolean eventType = annotation != null &&annotation.equals( DynamicTypeAnnotations.VALUE_RESERVATION_CLASSIFICATION);
            Map<DynamicType,ClassificationFilter> map = eventType ? reservationFilter : allocatableFilter;
            map.put(type, f);
        }
        
        return couldResolveAllEntities;
    }

    public User getUser() {
        return user;
    }

    public CalendarModelConfiguration createConfiguration() throws RaplaException {
        ClassificationFilter[] allocatableFilter = getAllocatableFilter();
        ClassificationFilter[] eventFilter = getReservationFilter();
        return createConfiguration(allocatableFilter, eventFilter);
    }
    
    public CalendarModelConfiguration createConfiguration(ClassificationFilter[] allocatableFilter, ClassificationFilter[] eventFilter) throws RaplaException {
        String viewName = selectedView;
        for (Iterator it = selectedObjects.iterator();it.hasNext();) {
            if ( it.next() instanceof Conflict) {
                throw new RaplaException("Storing the conflict view is not possible with Rapla.");
            }
        }

        Set selected = new HashSet( selectedObjects);
        final Date selectedDate = getSelectedDate();
        final Date startDate = getStartDate();
        final Date endDate = getEndDate();
        return m_facade.newRaplaCalendarModel( m_facade.newRaplaMap(selected), allocatableFilter,eventFilter, title, startDate, endDate, selectedDate, viewName, m_facade.newRaplaMap(optionMap));
    }

    public void setReservationFilter(ClassificationFilter[] array) {
        reservationFilter.clear();
        try {
            defaultEventTypes = createConfiguration(null,array).isDefaultEventTypes();
        } catch (RaplaException e) {
            // DO Not set the types
        }
        for (ClassificationFilter entry: array)
        {   
            final DynamicType type = entry.getType();
            reservationFilter.put( type, entry);
        }
    }

    public void setAllocatableFilter(ClassificationFilter[] array) {
        allocatableFilter.clear();
        try {
            defaultResourceTypes = createConfiguration(array,null).isDefaultResourceTypes();
        } catch (RaplaException e) {
            // DO Not set the types
        }
        for (ClassificationFilter entry: array)
        {   
            final DynamicType type = entry.getType();
            allocatableFilter.put( type, entry);
        }
    }
   
    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#getSelectedDate()
	 */
    public Date getSelectedDate() {
        return selectedDate;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#setSelectedDate(java.util.Date)
	 */
    public void setSelectedDate(Date date) {
        if ( date == null)
            throw new IllegalStateException("Date can't be null");
        this.selectedDate = date;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#getStartDate()
	 */
    public Date getStartDate() {
        return startDate;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#setStartDate(java.util.Date)
	 */
    public void setStartDate(Date date) {
        this.startDate = date;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#getEndDate()
	 */
    public Date getEndDate() {
        return endDate;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#setEndDate(java.util.Date)
	 */
    public void setEndDate(Date date) {
        this.endDate = date;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#getTitle()
	 */
    public String getTitle() {
        return title;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#setTitle(java.lang.String)
	 */
    public void setTitle(String title) {
        this.title = title;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#setView(java.lang.String)
	 */
    public void setViewId(String view) {
        this.selectedView = view;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#getView()
	 */
    public String getViewId() {
        return this.selectedView;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#getNonEmptyTitle()
	 */
    public String getNonEmptyTitle() {
        if (getTitle() != null && getTitle().trim().length()>0)
            return getTitle();


        String types = "";
        /*
        String dateString = getRaplaLocale().formatDate(getSelectedDate());
        if  ( isListingAllocatables()) {
            try {
                Collection list = getSelectedObjectsAndChildren();
                if (list.size() == 1) {
                    Object obj = list.iterator().next();
                    if (!( obj instanceof DynamicType))
                    {
                        types = getI18n().format("allocation_view",getName( obj ),dateString);
                    }
                }

            } catch (RaplaException ex) {
            }
            if ( types == null )
                types = getI18n().format("allocation_view",  getI18n().getString("resources_persons"));
        } else if ( isListingReservations()) {
             types =  getI18n().getString("reservations");
        } else {
            types = "unknown";
        }
        */

        return types;
    }

    public String getName(Object object) {
        if (object == null)
            return "";
        if (object instanceof Named) {
            String name = ((Named) object).getName(getI18n().getLocale());
            return (name != null) ? name : "";
        }
        return object.toString();
    }

    public int getSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }


    private Collection getFilteredAllocatables() throws RaplaException {
        List list = new ArrayList();
        Allocatable[] all = m_facade.getAllocatables();
        for (int i=0;i<all.length;i++) {
            if ( isInFilter( all[i]) ) {
                list.add( all[i]);
            }
        }
        return list;
    }

    private boolean isInFilter( Allocatable classifiable) {
        final Classification classification = classifiable.getClassification();
        final DynamicType type = classification.getType();
        final ClassificationFilter classificationFilter = allocatableFilter.get( type);
        if ( classificationFilter != null)
        {
            final boolean matches = classificationFilter.matches(classification);
            return matches;
        }
        else
        {
            return defaultResourceTypes;
        }
    }
    
    public Collection getSelectedObjectsAndChildren() throws RaplaException
    {
        Assert.notNull(selectedObjects);

        ArrayList dynamicTypes = new ArrayList();
        for (Iterator it = selectedObjects.iterator();it.hasNext();)
        {
            Object obj = it.next();
            if (obj instanceof DynamicType) {
                dynamicTypes.add (obj);
            }
        }

        HashSet result = new HashSet();
        result.addAll( selectedObjects );
        
        boolean allAllocatablesSelected = selectedObjects.contains( ALLOCATABLES_ROOT);
        
        Collection filteredList = getFilteredAllocatables();
        for (Iterator it = filteredList.iterator();it.hasNext();)
        {
            Object oneSelectedItem =  it.next();
            if ( selectedObjects.contains(oneSelectedItem)) {
                continue;
            }
            if ( oneSelectedItem instanceof Classifiable ) {
                Classification classification = ((Classifiable)oneSelectedItem).getClassification();
                if ( classification == null)
                {
                	continue;
                }
                 if ( allAllocatablesSelected || dynamicTypes.contains(classification.getType()))
                 {
                    result.add( oneSelectedItem );
                    continue;
                }
            }

        }

        return result;
    }


    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#setSelectedObjects(java.util.List)
	 */
    public void setSelectedObjects(Collection selectedObjects) {
        this.selectedObjects = retainRaplaObjects(selectedObjects);
    }

    private List retainRaplaObjects(Collection list ){
        List result = new ArrayList();
        for ( Iterator it = list.iterator();it.hasNext();) {
            Object obj = it.next();
            if ( obj instanceof RaplaObject) {
                result.add( obj );
            }
        }
        return result;
    }



    public Collection getSelectedObjects()
    {
        return selectedObjects;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#getReservationFilter()
	 */
    public ClassificationFilter[] getReservationFilter() throws RaplaException 
    {
        Collection<ClassificationFilter> filter ;
        if ( isDefaultEventTypes())
        {
            filter = new ArrayList<ClassificationFilter>();
            for (DynamicType type :m_facade.getDynamicTypes( DynamicTypeAnnotations.VALUE_RESERVATION_CLASSIFICATION))
            {
                filter.add( type.newClassificationFilter());
            }
        }
        else
        {
            filter = reservationFilter.values();
        }
        return filter.toArray(ClassificationFilter.CLASSIFICATIONFILTER_ARRAY);
    }

    /* (non-Javadoc)
     * @see org.rapla.calendarview.CalendarModel#getAllocatableFilter()
     */
    public ClassificationFilter[] getAllocatableFilter() throws RaplaException {
        Collection<ClassificationFilter> filter ;
        if ( isDefaultResourceTypes())
        {
            filter = new ArrayList<ClassificationFilter>();
            for (DynamicType type :m_facade.getDynamicTypes( DynamicTypeAnnotations.VALUE_RESOURCE_CLASSIFICATION))
            {
                filter.add( type.newClassificationFilter());
            }
            for (DynamicType type :m_facade.getDynamicTypes( DynamicTypeAnnotations.VALUE_PERSON_CLASSIFICATION))
            {
                filter.add( type.newClassificationFilter());
            }
            
        }
        else
        {
            filter = allocatableFilter.values();
        }
        return filter.toArray(ClassificationFilter.CLASSIFICATIONFILTER_ARRAY);
    }

    public Object clone()  {
        CalendarModelImpl clone;
        try
        {
            clone = (CalendarModelImpl )super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            throw new IllegalStateException( e.getMessage() );
        }
        return clone;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#getReservations(java.util.Date, java.util.Date)
	 */
    public Reservation[] getReservations() throws RaplaException {
        return getReservations( getStartDate(), getEndDate() );
    }

    public Reservation[] getReservations(Date startDate, Date endDate) throws RaplaException {
        return (Reservation[]) getReservationsAsList( startDate, endDate ).toArray( Reservation.RESERVATION_ARRAY);
    }


    private boolean hasConflict( Reservation reservation,Conflict[] conflict ) {
        for (int j=0;j<conflict.length;j++) {
            Conflict allocatable = conflict[j];
            if (reservation.equals( allocatable.getReservation1() ) || reservation.equals( allocatable.getReservation2())) {
               return true;
            }
        }
        return false;
    }


    private void restrict(Collection reservations,User[] users) throws RaplaException {
        HashSet usersSet = new HashSet(Arrays.asList(users));
        for ( Iterator it = reservations.iterator();it.hasNext();) {
            Reservation event = (Reservation)it.next();
            if ( !usersSet.contains( event.getOwner() )) {
                it.remove();
            }
        }
    }

    private void restrict(Collection reservations,Conflict[] conflicts) throws RaplaException {
        for ( Iterator it = reservations.iterator();it.hasNext();) {
            Reservation event = (Reservation)it.next();
            if ( !hasConflict( event, conflicts )) {
                it.remove();
            }
        }
    }

    private Collection getRestrictedReservations( Allocatable[] allocatables,Date start, Date end) throws RaplaException {
    	Collection selectedReservations = getSelected( Reservation.TYPE);
        if ( selectedReservations.size() > 0) {
            return selectedReservations;
        }
        ClassificationFilter[] reservationFilter2 = getReservationFilter();
        if ( isDefaultEventTypes())
        {
            reservationFilter2 = null;
        }
        
        Reservation[] reservations =m_facade.getReservationsForAllocatable(allocatables, start, end,reservationFilter2 );
        return Arrays.asList( reservations );

    }

    private List getReservationsAsList(Date start, Date end) throws RaplaException {
        
    	Allocatable[] allocatables = getSelectedAllocatables();
    	if ( allocatables.length == 0)
    	{
    		allocatables = null;
    	}
    	List reservations = new ArrayList(getRestrictedReservations( allocatables, start, end));
//        Set reservationTypes = getSelectedTypes( DynamicTypeAnnotations.VALUE_RESERVATION_CLASSIFICATION);
//        if ( reservationTypes.size() > 0 ) {
//            restrictReservationTypes( reservations, reservationTypes);
//        }
        
 
        User[] users = getSelectedUsers();
        if ( users.length> 0 || isOnlyCurrentUserSelected()) {
            restrict( reservations, users );
        }
        Conflict[] conflicts = getSelectedConflicts();
        if ( conflicts.length > 0) {
            restrict( reservations, conflicts );
        }
        return reservations;
    }

    /* (non-Javadoc)
	 * @see org.rapla.calendarview.CalendarModel#getAllocatables()
	 */
    public Allocatable[] getSelectedAllocatables() throws RaplaException {
        Collection result = new HashSet();
        Iterator it = getSelectedObjectsAndChildren().iterator();
        while (it.hasNext()) {
            RaplaObject object  = (RaplaObject) it.next();
            if ( object.getRaplaType().equals( Allocatable.TYPE )) {
                result.add( object  );
            }
            if ( object.getRaplaType().equals( Conflict.TYPE )) {
                result.add( ((Conflict)object).getAllocatable() );
            }
        }
         return (Allocatable[]) result.toArray(Allocatable.ALLOCATABLE_ARRAY);
   }

    public User[] getSelectedUsers()  {
        User currentUser = getUser();
        if ( currentUser != null && !m_facade.canReadReservationsFromOthers( currentUser))
        {
            return new User[] {currentUser};
        }
        return (User[]) getSelected(User.TYPE).toArray(User.USER_ARRAY);
   }

    public Conflict[] getSelectedConflicts()  throws RaplaException {
        return (Conflict[]) getSelected(Conflict.TYPE).toArray(Conflict.CONFLICT_ARRAY);
   }

    public Set getSelectedTypes(String classificationType) throws RaplaException {
        Set result = new HashSet();
        Iterator it = getSelectedObjectsAndChildren().iterator();
        while (it.hasNext()) {
            RaplaObject object  = (RaplaObject) it.next();
            if ( object.getRaplaType().equals( DynamicType.TYPE )) {
                if (classificationType == null || (( DynamicType) object).getAnnotation( DynamicTypeAnnotations.KEY_CLASSIFICATION_TYPE).equals( classificationType))
                {
                    result.add( object  );
                }
            }
        }
         return result;
   }
    
    public Set getSelected(RaplaType type)  {
        Set result = new HashSet();
        Iterator it = getSelectedObjects().iterator();
        while (it.hasNext()) {
            RaplaObject object  = (RaplaObject) it.next();
            if ( object.getRaplaType().equals( type )) {
                result.add( object  );
            }
        }
         return result;
   }

    public Date[] getConflictDates() throws RaplaException {
        ArrayList list = new ArrayList();
        Conflict[] conflicts = getSelectedConflicts();
        for ( int i=0;i<conflicts.length;i++)
        {
            Date start = getFirstConflictDate( conflicts[i]);
            if ( start != null)
            {
                list.add( start );
            }
        }
        Collections.sort( list );
        return (Date[]) list.toArray( new Date[]{});
    }

    public Date getFirstConflictDate(Conflict conflict) throws RaplaException {
        Appointment a1  =conflict.getAppointment1();
        Appointment a2  =conflict.getAppointment2();
        Date minEnd =  a1.getMaxEnd();
        if ( a1.getMaxEnd() != null && a2.getMaxEnd() != null && a2.getMaxEnd().before( a1.getMaxEnd())) {
            minEnd = a2.getMaxEnd();
        }
        Date maxStart = a1.getStart();
        if ( a2.getStart().after( a1.getStart())) {
            maxStart = a2.getStart();
        }
        // Jetzt berechnen wir fuer 2 Jahre
        if ( minEnd == null)
            minEnd = new Date(maxStart.getTime() + DateTools.MILLISECONDS_PER_WEEK * 100);

        List<AppointmentBlock> listA = new ArrayList<AppointmentBlock>();
        a1.createBlocks(maxStart, minEnd, listA );
        List<AppointmentBlock> listB = new ArrayList<AppointmentBlock>();
        a2.createBlocks( maxStart, minEnd, listB );
        for ( int i=0, j=0;i<listA.size() && j<listB.size();) {
            long s1 = listA.get( i).getStart();
            long s2 = listB.get( j).getStart();
            long e1 = listA.get( i).getEnd();
            long e2 = listB.get( j).getEnd();
            if ( s1< e2 && s2 < e1) {
                return new Date( Math.max( s1, s2));
            }
            if ( s1> s2)
               j++;
            else
               i++;
        }
        return null;
    }

    protected I18nBundle getI18n() {
        return i18n;
    }

    protected RaplaLocale getRaplaLocale() {
        return raplaLocale;
    }

    public boolean isOnlyCurrentUserSelected() {
        User[] users = getSelectedUsers();
        User currentUser = getUser();

    
            
        return ( users.length == 1 && users[0].equals( currentUser));
    }

    public void selectUser(User user) {
        for (Iterator it = selectedObjects.iterator();it.hasNext();) {
            RaplaObject obj =(RaplaObject) it.next();
            if (obj.getRaplaType().equals( User.TYPE) ) {
                it.remove();
            }
        }
        if ( user != null)
        {
            selectedObjects.add( user );
        }
    }

    public Object getOption( String name )
    {
        return optionMap.get(  name );
    }

    public void setOption( String name, RaplaObject object )
    {
        optionMap.put( name, object);
    }

    public void setOption( String name, String string )
    {
        optionMap.put( name, string);
    }

    public boolean isDefaultEventTypes() 
    {
        return defaultEventTypes;
    }

    public boolean isDefaultResourceTypes() 
    {
        return defaultResourceTypes;
    }

    public void save(final String filename) throws RaplaException,
            EntityNotFoundException {
        final CalendarModelConfiguration conf = createConfiguration();
        
        Preferences clone = (Preferences) m_facade.edit(m_facade.getPreferences(user));

        if ( filename.equals(i18n.getString("default")))
        {
            clone.putEntry( CalendarModelConfiguration.CONFIG_ENTRY, conf);
        }
        else
        {
            Map exportMap= ((RaplaMap)clone.getEntry(AutoExportPlugin.PLUGIN_ENTRY));
            Map newMap;
            if ( exportMap == null)
                newMap = new TreeMap();
            else
                newMap = new TreeMap( exportMap);
            newMap.put(filename, conf);
            clone.putEntry( AutoExportPlugin.PLUGIN_ENTRY, m_facade.newRaplaMap( newMap ));
        }
        m_facade.store(clone);
    }

    public void load(final String filename)
    throws RaplaException, EntityNotFoundException {
        final CalendarModelConfiguration modelConfig;
        final Preferences preferences = m_facade.getPreferences(user);
        final boolean isDefault = filename == null || filename.equals(i18n.getString("default"));
        if ( isDefault )
        {
            modelConfig = (CalendarModelConfiguration)preferences.getEntry(CalendarModelConfiguration.CONFIG_ENTRY);
          
        }
        else
        {
            Map exportMap= ((RaplaMap)preferences.getEntry(AutoExportPlugin.PLUGIN_ENTRY));
            modelConfig = (CalendarModelConfiguration)exportMap.get(filename);
            
        }
        if ( modelConfig == null && filename != null && !isDefault)
        {
            throw new EntityNotFoundException("Saved calendar with name '" + filename + "' not found.");
        }
        else
        {
            Map alternativeOptions = new HashMap();
            if (modelConfig != null && modelConfig.getOptionMap() != null)
            {
                // All old default calendars have no selected date
                if (isDefault && (modelConfig.getOptionMap().get( AbstractHTMLCalendarPage.SAVE_SELECTED_DATE) == null))
                {
                    alternativeOptions.put(AbstractHTMLCalendarPage.SAVE_SELECTED_DATE , "false");
                }
                // All old calendars are exported
                if ( !isDefault && modelConfig.getOptionMap().get(AutoExportPlugin.HTML_EXPORT) == null)
                {
                    alternativeOptions.put(AutoExportPlugin.HTML_EXPORT,"true");
                }
            }
            setConfiguration(modelConfig, alternativeOptions);
        }
    }
}




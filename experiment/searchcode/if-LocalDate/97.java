/*
 * Copyright (c) 2006, Atlassian Software Systems Pty Ltd
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of "Atlassian" nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.atlassian.confluence.extra.calendar.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.extra.calendar.CalendarManager;
import com.atlassian.confluence.extra.calendar.CalendarUtils;
import com.atlassian.confluence.extra.calendar.model.CalendarException;
import com.atlassian.confluence.extra.calendar.model.CalendarGroup;
import com.atlassian.confluence.extra.calendar.util.ContentPermissionWrapper;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.actions.AbstractPageAction;
import com.atlassian.confluence.pages.actions.PageAware;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.setup.settings.Settings;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.spring.container.ContainerManager;
import com.opensymphony.webwork.ServletActionContext;

public abstract class AbstractCalendarAction extends AbstractPageAction implements PageAware {
    protected static final String PAGE_NOT_PERMITTED = "pagenotpermitted";

    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    private static final int HTTP_PORT = 80;

    private static final int HTTPS_PORT = 443;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern( "dd-MMM-yyyy" );

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern( "dd-MMM-yyyy h:mm a" );

    // private static final DateTimeFormatter DISPLAYDATE_FORMATTER =
    // DATE_FORMATTER.withLocale(Locale.ENGLISH);

    private CalendarManager calendarManager;

    protected String calendarId;

    private CalendarGroup _calendar;

    private String _contextPath;

    private String fullContextPath;

    private boolean requireEditPermission;

    private String domain;

    private CalendarUtils calendarUtils = new CalendarUtils();

    private Settings settings;

    private long lastSourceRead = 0;

    /** The period after which a calendar will be reread from the source */
    private long REREAD_THRESHOLD = 30 * 1000;

    protected AbstractCalendarAction( boolean requireEditPermission ) {
        this.requireEditPermission = requireEditPermission;
    }

    public boolean isPermitted() {
        final ContentPermissionWrapper contentPermissionWrapper = ( ContentPermissionWrapper ) ContainerManager
                .getInstance().getContainerContext().getComponent( "contentPermissionWrapper" );
        // if (requireEditPermission && !(isSpaceAdmin() ||
        // getPage().hasPermission(getRemoteUser(),
        // ContentPermission.EDIT_PERMISSION, contentPermissionManager)))
        if ( requireEditPermission
                && !( isSpaceAdmin() || contentPermissionManager.hasContentLevelPermission( getRemoteUser(),
                        ( String ) contentPermissionWrapper.getStaticFieldValue( "EDIT_PERMISSION" ), getPage() ) ) )
            return false;

        return super.isPermitted();
    }

    protected List getEditPermissionTypes() {
        List permissionTypes = super.getPermissionTypes();
        addPermissionTypeTo( SpacePermission.CREATEEDIT_PAGE_PERMISSION, permissionTypes );
        return permissionTypes;
    }

    public boolean isEditPermitted() {
        if ( GeneralUtil.isSuperUser( getRemoteUser() ) )
            return true;

        return ( spacePermissionManager.hasPermission( getEditPermissionTypes(), getSpace(), getRemoteUser() ) && hasEditPagePermission() );
    }

    protected boolean hasEditPagePermission() {
        if ( getPage() instanceof Page ) {
            final ContentPermissionWrapper contentPermissionWrapper = ( ContentPermissionWrapper ) ContainerManager
                    .getInstance().getContainerContext().getComponent( "contentPermissionWrapper" );
            return contentPermissionManager.hasContentLevelPermission( getRemoteUser(),
                    ( String ) contentPermissionWrapper.getStaticFieldValue( "EDIT_PERMISSION" ), getPage() );
        }
        return true;
    }

    public String getFullContextPath() {
        if ( fullContextPath == null ) {
            String domain = getDomainName();
            fullContextPath = domain + getContextPath();
        }
        return fullContextPath;
    }

    public String getDomainName() {
        if ( domain == null ) {
            domain = getSettings().getBaseUrl();

            if ( domain != null && domain.trim().length() > 0 ) {
                // Check if the domain needs to be trimmed
                int start = domain.indexOf( "://" );
                if ( start >= 0 ) {
                    int end = domain.indexOf( '/', start + 3 );
                    if ( end > 0 ) {
                        domain = domain.substring( 0, end );
                    }
                } else {
                    domain = null;
                }
            }

            if ( domain == null ) // Generate it from the request, if
                                    // possible.
            {
                HttpServletRequest req = ServletActionContext.getRequest();

                if ( req != null ) {
                    String scheme = req.getScheme();
                    String name = req.getServerName();
                    int port = req.getServerPort();
                    if ( HTTP.equals( scheme ) && port == HTTP_PORT || HTTPS.equals( scheme )
                            && port == HTTPS_PORT )
                        port = -1;

                    domain = scheme + "://" + name;
                    if ( port >= 0 )
                        domain += ":" + port;
                } else {
                    domain = "";
                }
            }
        }
        return domain;
    }

    private Settings getSettings() {
        if ( settings == null )
            settings = settingsManager.getGlobalSettings();
        return settings;
    }

    /**
     * The path to the root of the Confluence web application.
     * 
     * @return the context path.
     */
    public String getContextPath() {
        if ( _contextPath == null ) {
            HttpServletRequest req = ServletActionContext.getRequest();

            if ( req != null )
                _contextPath = req.getContextPath();

            else
                _contextPath = getBootstrapManager().getWebAppContextPath();
        }
        return _contextPath;
    }

    public CalendarManager getCalendarManager() {
        if ( calendarManager == null )
            calendarManager = CalendarManager.getInstance();
        return calendarManager;
    }

    public ContentEntityObject getContent() {
        return getPage();
    }

    public CalendarGroup getCalendar() {
        if ( _calendar == null ) {
            ContentEntityObject content = getContent();
            if ( getCalendarManager() != null && content != null && calendarId != null && calendarId.length() > 0 )
                _calendar = ( CalendarGroup ) getCalendarManager().getCalendar( content, calendarId, null );
        }
        return _calendar;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId( String calendarId ) {
        this.calendarId = calendarId;
    }

    protected String saveCalendar() {
        try {
            getCalendarManager().saveCalendar( getContent(), getCalendarId(), getCalendar() );
            return SUCCESS;
        } catch ( CalendarException e ) {
            e.printStackTrace();
            addActionError( "A problem occurred while saving the calendar: " + e.getMessage() );
            return ERROR;
        }
    }

    public boolean isAnonymousViewable() {
        return permissionManager.hasPermission( null, Permission.VIEW, getPage() );
    }

    public String formatDate( ReadableInstant instant ) {
        return DATE_FORMATTER.print( instant );
    }

    public String formatDate( ReadablePartial partial ) {
        return DATE_FORMATTER.print( partial );
    }

    public String formatDateTime( ReadableInstant instant ) {
        return DATETIME_FORMATTER.print( instant );
    }

    public String formatDateTime( ReadablePartial partial ) {
        return DATETIME_FORMATTER.print( partial );
    }

    public String formatDisplayDate( LocalDate localDate ) {
        // return DISPLAYDATE_FORMATTER.print(partial);
        return String.valueOf( DateTimeUtils.getInstantMillis( localDate.toDateMidnight( DateTimeZone.UTC ) ) );
    }

    public String formatDisplayDate( LocalDateTime localDateTime ) {
        // return DISPLAYDATE_FORMATTER.print(partial);
        return String.valueOf( DateTimeUtils.getInstantMillis( localDateTime.toDateTime( DateTimeZone.UTC ) ) );
    }

    /**
     * Returns the list of available timezone IDs.
     * 
     * @return The timezone list.
     */
    public List getTimeZones() {
        return CalendarManager.getInstance().getTimeZones();
    }

    public String getTimeZoneLabel( DateTimeZone zone ) {
        return CalendarManager.getInstance().getTimeZoneLabel( zone );
    }

    /**
     * Returns the offset as a '[+/-]XXXX' string (eg. "+1000")
     * 
     * @param zone
     *            The timezone.
     * @return the offset value.
     */
    public String getTimeZoneOffset( DateTimeZone zone ) {
        return CalendarManager.getInstance().getTimeZoneOffset( zone );
    }

    public CalendarUtils getCalendarUtils() {
        return calendarUtils;
    }

    public void setSettings( Settings settings ) {
        this.settings = settings;
    }
}


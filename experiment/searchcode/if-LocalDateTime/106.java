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
package com.atlassian.confluence.extra.calendar.ical.model;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

/**
 * Represents a complete id for an ICalEvent.
 */
public class EventId {
	
    private static final String OVERRIDE_DATE = ";odt:";

    private static final String INSTANCE_DATE = ";idt:";

    private static final String TIMEZONE = ";tz:";

    private String uid;

    private LocalDateTime overrideDate;

    private LocalDateTime instanceDate;

    private DateTimeZone timeZone;

    public EventId( String id ) {
        int odtIndex = id.indexOf( OVERRIDE_DATE );
        int idtIndex = id.indexOf( INSTANCE_DATE );
        int tzIndex = id.indexOf( TIMEZONE );

        if ( odtIndex != -1 ) {
            uid = id.substring( 0, odtIndex );
            overrideDate = new LocalDateTime( id.substring( odtIndex + OVERRIDE_DATE.length(), idtIndex ) );
        } else {
            uid = id.substring( 0, idtIndex );
        }

        instanceDate = new LocalDateTime( id.substring( idtIndex + INSTANCE_DATE.length(),
                ( tzIndex >= 0 ? tzIndex : id.length() ) ) );

        if ( tzIndex != -1 )
            timeZone = DateTimeZone.forID( id.substring( tzIndex + TIMEZONE.length() ) );
    }

    public EventId( EventDetails details, LocalDateTime instanceDate, DateTimeZone timeZone ) {
        this( details.getUid(), ( details instanceof OverrideEventDetails ) ? ( ( OverrideEventDetails ) details )
                .getOverrideDate() : null, instanceDate, timeZone );
    }

    public EventId( String uid, LocalDateTime overrideDate, LocalDateTime instanceDate, DateTimeZone timeZone ) {
        this.uid = uid;
        this.overrideDate = overrideDate;
        this.instanceDate = instanceDate;
        this.timeZone = timeZone;
    }

    public String getUid() {
        return uid;
    }

    public LocalDateTime getOverrideDate() {
        return overrideDate;
    }

    public LocalDateTime getInstanceDate() {
        return instanceDate;
    }

    public DateTimeZone getTimeZone() {
        return timeZone;
    }

    public String toString() {
        StringBuffer id = new StringBuffer();
        id.append( uid );

        if ( overrideDate != null )
            id.append( OVERRIDE_DATE ).append( overrideDate );

        id.append( INSTANCE_DATE ).append( instanceDate );

        if ( timeZone != null )
            id.append( TIMEZONE ).append( timeZone );

        return id.toString();
    }
}


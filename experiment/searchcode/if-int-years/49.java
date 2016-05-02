package com.atlassian.confluence.extra.cal2.action;

import com.atlassian.confluence.core.Administrative;
import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.extra.cal2.mgr.CalendarManager;

public class SetCalendarIndexInterval extends ConfluenceActionSupport implements Administrative {
    
    private int years;

    public int getYears() {
        return years;
    }

    public void setYears(int years) {
        this.years = years;
    }

    public String doDefault() throws Exception {
//        setYears(CalendarManager.getInstance().getCalendarIndexingInterval());
        return SUCCESS;
    }

    public String execute() throws Exception {
//        if (isCanceled()) /* This should not be needed in Confluence 2.9 since it has a CancellingInterceptor */
//            return CANCEL;
//
//        CalendarManager.getInstance().setCalendarIndexingInterval(getYears());
        return SUCCESS;
    }

    public void validate() {
//        int years;
//
//        if (isCanceled())
//            return;
//
//        if (0 >= (years = getYears()))
//            addFieldError("years", getText("calendar.administration.indexer.error.years.too-small"));
//
//        if (CalendarManager.MAX_CALENDAR_INDEX_INTERVAL < years)
//            addFieldError("years",
//                    getText(
//                            "calendar.administration.indexer.error.years.too-big",
//                            new Object[] { new Integer(years), new Integer(CalendarManager.MAX_CALENDAR_INDEX_INTERVAL) }
//                    )
//            );
    }
}


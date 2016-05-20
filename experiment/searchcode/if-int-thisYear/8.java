/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.chl.group9.tomaxus.controller;

import edu.chl.group9.tomaxus.model.BlogEvent;
import edu.chl.group9.tomaxus.model.Event;
import edu.chl.group9.tomaxus.model.TUser;
import edu.chl.group9.tomaxus.model.Tomaxus;
import edu.chl.group9.tomaxus.model.dbaccess.IBlogRegistry;
import edu.chl.group9.tomaxus.model.dbaccess.IEventRegistry;
import edu.chl.group9.tomaxus.model.dbaccess.ITUserRegistry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@RequestScoped
@Named
public class HomeBackingBean {

    private IEventRegistry reg;
    private IBlogRegistry reg1;
    private TUser currentUser;
    private List<Event> events;
    private Date currentDate;
    private int thisDay;
    private int thisMonth;
    private int thisYear;

    public HomeBackingBean() {
        currentUser = (TUser) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userid");
        events = new ArrayList<>();
        currentDate = Calendar.getInstance().getTime();
        thisDay = currentDate.getDate();
        thisMonth = currentDate.getMonth();
        thisYear = currentDate.getYear();
        reg = Tomaxus.INSTANCE.getEventRegistry();
        reg1 = Tomaxus.INSTANCE.getBlogRegistry();
    }

    public List<Event> getAllEvents() {
        events.clear();
        List<Event> ev = reg.getAll();
        List<TUser> par;
        for (Event e : ev) {
            par = e.getParticipants();
            for (TUser t : par) {
                if (t.equals(currentUser)) {
                    events.add(e);
                }
            }
        }
        return events;
    }

    public void newSite(long id) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("event.xhtml?id=" + 3);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<Event> getBlogsChanged() {
        List<Event> event = new ArrayList<>();
        for (Event e : events) {
            List<BlogEvent> blogs = reg1.getBlogsForEvent(e.getId());
            for (BlogEvent ev : blogs) {
                if (ev.getTimeCreated().getYear() == thisYear) {
                    if (ev.getTimeCreated().getMonth() == thisMonth) {
                        if ((ev.getTimeCreated().getDate() == thisDay) || ((thisDay - 1) == ev.getTimeCreated().getDate())) {
                            if (!(event.contains(e))) {
                                event.add(e);
                            }
                        }
                    } else if ((ev.getTimeCreated().getMonth() + 1) == thisMonth) {
                        if ((ev.getTimeCreated().getDate() == 31) && (thisDay == 1)) {
                            if (!(event.contains(e))) {
                                event.add(e);
                            }
                        }
                    }
                } else if ((ev.getTimeCreated().getYear() + 1) == thisYear) {
                    if ((ev.getTimeCreated().getMonth() == 11) && (thisMonth == 0)) {
                        if ((ev.getTimeCreated().getDate() == 31) && (thisDay == 1)) {
                            if (!(event.contains(e))) {
                                event.add(e);
                            }
                        }
                    }
                }
            }
        }
        return event;


    }

    public String blogs(Event evt) {
        StringBuilder sb = new StringBuilder();
        List<TUser> us = blogsChangedBy(evt);
        for (TUser u : us) {
            if (!(us.get(us.size() - 1).equals(u))) {
                sb.append(u.getFname());
                sb.append(" & ");
            } else {
                sb.append(u.getFname());
            }

        }
        return sb.toString();

    }
    

    public List<TUser> blogsChangedBy(Event evt) {
        List<TUser> users = new ArrayList<>();
        List<BlogEvent> blogs = reg1.getBlogsForEvent(evt.getId());
        for (BlogEvent ev : blogs) {
            if (ev.getTimeCreated().getYear() == thisYear) {
                if (ev.getTimeCreated().getMonth() == thisMonth) {
                    if ((ev.getTimeCreated().getDate() == thisDay) || ((thisDay - 1) == ev.getTimeCreated().getDate())) {
                        if (!(users.contains(ev.getAuthor()))) {
                            users.add(ev.getAuthor());
                        }
                    }
                } else if ((ev.getTimeCreated().getMonth() + 1) == thisMonth) {
                    if ((ev.getTimeCreated().getDate() == 31) && (thisDay == 1)) {
                        if (!(users.contains(ev.getAuthor()))) {
                            users.add(ev.getAuthor());
                        }
                    }
                }
            } else if ((ev.getTimeCreated().getYear() + 1) == thisYear) {
                if ((ev.getTimeCreated().getMonth() == 11) && (thisMonth == 0)) {
                    if ((ev.getTimeCreated().getDate() == 31) && (thisDay == 1)) {
                        if (!(users.contains(ev.getAuthor()))) {
                            users.add(ev.getAuthor());
                        }
                    }
                }
            }
        }

        return users;


    }

    public TUser getCurrentUser() {
        return currentUser;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public int getThisDay() {
        return thisDay;
    }

    public int getThisMonth() {
        return thisMonth;
    }

    public int getThisYear() {
        return thisYear;
    }
}


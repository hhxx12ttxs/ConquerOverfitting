/*    
Gestures, Link Recommendation Service
Copyright (C) 2011  Cluetail Ltd.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, 
or(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package fi.eleet;

import authFiles.webSession;
import fi.eleet.dbModels.linkModel;
import fi.eleet.postgredb.databaseAPI;
import java.util.List;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
/**
 *
 * @author Cluetail
 */
@AuthorizeInstantiation("USER")
public class resultsPanel extends Panel {
    private FeedbackPanel fbPanel;
    private String UID;
    private databaseAPI API;
    
    public resultsPanel(String name, databaseAPI api){
        super(name);
        API = api;
        fbPanel = new FeedbackPanel("Messages");
        add(fbPanel);
        UID = webSession.get().getUserId();
        
        List<linkModel> lista;
        try{
            lista = API.getSystemLinks(Integer.parseInt(UID));
        }
        catch(NumberFormatException x){
            lista = null;
        }
        if(lista != null){
            info("");
        ListView listview = new ListView("listview",lista){
            protected void populateItem(ListItem item){
                linkModel li = (linkModel) item.getModelObject();
                    double score = li.getRelevance();
                    String title = li.getTitle();
                    String ti = title;
                if(title.length() < 3) title = "No available title";
                if(title.length() > 50) title = title.substring(0, 49)+"...";
                    String ll = li.getUrl();
                    String sl;
                    if(ll != null){
                        if(!ll.contains("http://"))ll = "http://"+ll;
                        sl = ll;
                    }
                    else{
                        sl = "";
                        ll = "no url found";
                    }
                    if(ll.length() > 40) sl = sl.substring(0, 39)+"...";
                PopupSettings p = new PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | PopupSettings.STATUS_BAR);
                item.add(new Label("Title",title).add(new SimpleAttributeModifier("onclick", "openLink('"+ll+"');")));
                ExternalLink l = new ExternalLink("URL",ll,sl);
                    l.setPopupSettings(p);
                item.add(l);
                    DecimalFormat df = new DecimalFormat("##0");
                item.add(new Label("Score",df.format(score).toString()));
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    long t = li.getDate().getTime();
                    Date time = new Date(t);
                item.add(new Label("Date",sdf.format(time)));
                    final int id = li.getID();
                    final int uid = Integer.parseInt(UID);
                    List<Integer> votes = API.getUserVotedLinks(uid);
                    boolean found = false;
                    if(votes != null){
                            for(int i = 0; i < votes.size();i++)
                                if(id == votes.get(i).intValue()) found = true;
                        }
                    setOutputMarkupPlaceholderTag(true);
                    Label news;
                    
                    String loc = webSession.get().getLocale().getCountry();
                    if(found){
                        if(loc.equals("US")){
                            news = new Label("ns","Thank you for your vote!");
                        }
                        else{
                            news = new Label("ns","Kiitos äänestäsi!");
                        }
                        
                    }
                    else {
                        if(loc.equals("US"))
                        {
                            news = new Label("ns","Is this link news?");
                        }
                        else{
                            news = new Label("ns","Onko tämä linkki uutinen?");
                        }
                    }
                    
                    item.add(news);
                    Link y = new Link("yes")
                        {
                        @Override
                        public void onClick(){
                            API.addVote(uid, id, 1);
                            getRequestCycle().setRedirect(true);
                            setResponsePage(main.class);
                        }
                    };
                    if(found) y.setVisible(false);
                    item.add(y);
                    Link n = new Link("no"){
                        @Override
                        public void onClick(){
                            API.addVote(uid, id, -1);
                            getRequestCycle().setRedirect(true);
                            setResponsePage(main.class);
                        }
                    };
                    if(found) n.setVisible(false);
                    item.add(n);
                    
                item.add(new Link("remove"){
                    @Override
                    public void onClick(){
                        boolean succ = true;
                            succ = API.insertLinkToRemovedDB(uid, id);
                            succ = API.removeSysLink(id, uid);
                            if(succ == true) {
                                System.out.println("link removed successfully!");
                            }
                            getRequestCycle().setRedirect(true);
                            setResponsePage(main.class);
                    }
                    }
                );
                if(ll.contains("youtube")||ll.contains("vimeo")||ll.contains("dailymotion")){
                    item.add(new SimpleAttributeModifier("class", "link3"));
                }
                else{
                    item.add(new SimpleAttributeModifier("class", "link0"));
                }
                StringBuilder str = new StringBuilder(256);
                str.append("<script>function fbs_click()"
                        + " {u=location.href;t=document.title;window.open('http://www.facebook.com/sharer.php?u='+encodeURIComponent(u)"
                        + "+'&t='+encodeURIComponent(t),'sharer','toolbar=0,status=0,width=626,height=436');"
                        + "return false;}"
                        + "</script><style> html .fb_share_link "
                        + "{ padding:2px 0 0 20px; height:16px; background:url(http://static.ak.facebook.com/images/share/facebook_share_icon.gif?6:26981) "
                        + "no-repeat top left; }</style><a rel='nofollow' "
                        + "href='http://www.facebook.com/share.php?u="+ll+"&t="+ti+" "
                        + "onclick='return fbs_click()' target='_blank' class='fb_share_link'>Share on Facebook</a>");
                String s = str.toString();
                Label like = new Label("like", s);
                like.setEscapeModelStrings(false);
                item.add(like);
            }         
        };
        add(listview);
        }
        
        else{
            lista = null;
            lista = API.getRecommendedLinks();
            if(lista == null){
                System.err.println("Lista was null!");
                lista = new ArrayList<linkModel>();
                lista.add(new linkModel(0,new Timestamp(0l),"","",""));
            }
            info("Currently we base our matching on your Facebook actions.\n"
                +"It can take a while before we have collected enough data in order to offer you recommended links.\n\n"
                +"The more news links you share on Facebook, the better Eleet will work for you.\n"
                +"In the meantime, you may be interested in the most popular links among Eleet users, as below:");
            System.err.println("Lista size is: "+lista.size());
        
        ListView listview = new ListView("listview",lista)
        {
        protected void populateItem(ListItem item){
        linkModel li = (linkModel) item.getModelObject();
                    String title = li.getTitle();
                    if(title == null) title = "";
                    String ti = title;
                if(title.length() < 3) title = "No available title";
                if(title.length() > 50) title = title.substring(0, 49)+"...";
                    String ll = li.getUrl();
                    String sl;
                    if(ll != null){
                        if(!ll.contains("http://"))ll = "http://"+ll;
                        sl = ll;
                    }
                    else{
                        sl = "";
                        ll = "no url found";
                    }
                    if(ll.length() > 40) sl = sl.substring(0, 39)+"...";
                PopupSettings p = new PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | PopupSettings.STATUS_BAR);
                item.add(new Label("Title",title).add(new SimpleAttributeModifier("onclick", "openLink('"+ll+"');")));
                ExternalLink l = new ExternalLink("URL",ll,sl);
                    l.setPopupSettings(p);
                item.add(l);
                item.add(new Label("Score",Double.toString(0)));
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    long t = li.getDate().getTime();
                    Date time = new Date(t);
                    item.add(new Label("Date",sdf.format(time)));
                    Label news;
                    news = new Label("ns","");

                    item.add(news);
                    Link y = new Link("yes")
                        {
                        @Override
                        public void onClick(){
                        }
                    };
                    y.setVisible(false);
                    item.add(y);
                    Link n = new Link("no"){
                        @Override
                        public void onClick(){
                        }
                    };
                    n.setVisible(false);
                    item.add(n);
                    Link lin =  new Link("remove"){
                    @Override
                    public void onClick(){
                    }
                    };      
                lin.setVisible(false);
                item.add(lin);
                if(ll.contains("youtube")||ll.contains("vimeo")||ll.contains("dailymotion")){
                    item.add(new SimpleAttributeModifier("class", "link3"));
                }
                else{
                    item.add(new SimpleAttributeModifier("class", "link0"));
                }
                StringBuilder str = new StringBuilder(256);
                str.append("<script>function fbs_click()"
                        + " {u=location.href;t=document.title;window.open('http://www.facebook.com/sharer.php?u='+encodeURIComponent(u)"
                        + "+'&t='+encodeURIComponent(t),'sharer','toolbar=0,status=0,width=626,height=436');"
                        + "return false;}"
                        + "</script><style> html .fb_share_link "
                        + "{ padding:2px 0 0 20px; height:16px; background:url(http://static.ak.facebook.com/images/share/facebook_share_icon.gif?6:26981) "
                        + "no-repeat top left; }</style><a rel='nofollow' "
                        + "href='http://www.facebook.com/share.php?u="+ll+"&t="+ti+" "
                        + "onclick='return fbs_click()' target='_blank' class='fb_share_link'>Share on Facebook</a>");
                String s = str.toString();
                Label like = new Label("like", s);
                like.setEscapeModelStrings(false);
                item.add(like);
            }         
        };
        System.err.println(listview.size());
        add(listview);
        }
    }
}


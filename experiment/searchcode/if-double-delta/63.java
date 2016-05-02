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

package fi.eleet.core;
import fi.eleet.dbModels.linkModel;
import fi.eleet.dbModels.systemlinkModel;
import fi.eleet.dbModels.userlinkModel;
import fi.eleet.models.matchedUserModel;
import fi.eleet.models.userModel;
import fi.eleet.postgredb.databaseAPI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 *
 * @author Cluetail
 */

public class coreModule implements coreInterface {
    private userModel me;
    private databaseAPI API;
    
    public Long countDelta(Long start){
        Long delta = new Date().getTime() - start;
        delta = delta /1000;
        return delta;
    }
    
    @Override
    public void initEngine(){
        Long start = new Date().getTime();
        System.err.println("Started a round!"); 
        //Getting all the users (uid's got from userLinks)
        List<userModel> users = objectizeAllUsers();
        //for(int n = 0; n < users.size();n++)
        for(userModel n : users)
        {
            int uid = n.getUid(); //Gettings the uid of currently processed user
            List<matchedUserModel> matchedUsers = getMatchingUsers(users, uid);
            matchedUsers = orderMatchedUsers(matchedUsers);
            handleSystemLinks(matchedUsers);
        }
        Long delta = countDelta(start);
        System.err.println("Finnished a round in "+delta+" seconds!");
        if(API != null) API = null;
    }
    
    @Override
    public List<userModel> objectizeAllUsers()
    {
        Long start = new Date().getTime();
        API = new databaseAPI();
        List<Integer> users = API.getULUsers();
        List<userModel> linkList = new ArrayList<userModel>();
        for(int n = 0;n < users.size();n++)
        {
            int UID = users.get(n).intValue();
            List<userlinkModel> ulinks = API.getUserLinks(UID); //Getting all the user links
            List<linkModel> linkit = API.getUserLData(UID); //Getting the data of the userlinks
            if(ulinks != null && linkit != null){
                //.println("Added necessary links!");
                linkList.add(new userModel(UID,linkit,ulinks));
            }
        }
        Long delta = countDelta(start);
        System.err.println("objectized users in "+delta+" seconds!");
        if(linkList != null) return linkList;
        else{
            //.println("Error while objectizing users!");
            return null;
        }
    }

    @Override
    public List<matchedUserModel> getMatchingUsers(List<userModel> userList,int uid){
        Long start = new Date().getTime();
        me = null;
        if(userList == null){
            ////.println("Error while getting userList!");
            return null;
        }
        //Get userObject me to which all the other users are compared
        for(int i = 0;i < userList.size();i++)
        {
            userModel u = userList.get(i);
            if(u.getUid() == uid){
                me = new userModel(u);
                break;
            }
        }
        if(me == null){
            ////.println("Error while objectizing user!");
            return null;
        }
        //Going through rest of the users and comparing links with them
        //Returns a list of users that have at least 1 matching link
        //List contains matchedUser objects that contain user Objects and
        //Common link count
        List<matchedUserModel> matchedUsers = new ArrayList<matchedUserModel>();
        //int mC = API.countUserLinks(me.getUid());
        int mC = me.getLinks().size();
        
        // Going through the userlist
        for(int i = 0;i < userList.size();i++)
        {
            int count = 0;
            //going through all the links
            for(int n = 0;n < mC;n++){
                int chkUser = userList.get(i).getULinks().size(); 
                //API.countUserLinks(userList.get(i).getUid());
                for(int m = 0;m < chkUser;m++){
                    //Counting link matches
                    if(me.getLinks().get(n).getID() == userList.get(i).getLinks().get(m).getID() 
                        && userList.get(i).getUid() != me.getUid()){ count++;}
                }
            }
            //If link count is bigger than 0 add the user in MatchedUser list
            if(count > 0 && userList.get(i).getUid() != me.getUid()){
                if(userList.get(i).getLinks().size() > 1){
                    matchedUserModel m = new matchedUserModel(new userModel(userList.get(i)),count,0);
                    matchedUsers.add(m);
                }
            }
        }

        if(matchedUsers == null){
            ////.println("Error while getting matched Users!");
            return null;
        }
        Long delta = countDelta(start);
        System.err.println("got matched users in "+delta+" seconds!");
        return matchedUsers;
    }

    @Override
    public List<matchedUserModel> orderMatchedUsers(List<matchedUserModel> users)
    {
        Long start = new Date().getTime();
        //A new list where users are added in correct order
        List<matchedUserModel> orderedList = new ArrayList<matchedUserModel>();
        double count = 0;
        matchedUserModel n = null;
        //Counting match score
        for(int i = 0;i < users.size();i++)
        {
            double r = 0;
            int b = users.get(i).getCommonCount();
            int a = users.get(i).getUser().getLinks().size();
            r = countMatchScore(a,b);
            users.get(i).setMatchScore(r);
            //System.err.println("User "+users.get(i).getUser().getUid()+" Score "+r);
        }
        while(!users.isEmpty())
        {
            for(int i = 0;i < users.size();i++)
            {
                matchedUserModel m = users.get(i);
                if(count <= m.getMatchScore()){
                    n = users.get(i);
                    count = n.getMatchScore();
                }
            }
            if(n != null){
                orderedList.add(new matchedUserModel(n));
                count = 0;
                users.remove(n);
            }
            n = null;
        }
        Long delta = countDelta(start);
        System.err.println("ordered matched users in "+delta+" seconds!");
        if(orderedList == null) return null;
        else return orderedList;
    }

    @Override
    public void handleSystemLinks(List<matchedUserModel> users){
        Long start = new Date().getTime();
        //How many links added to the system
        int linksHandled = 0;
        //While counter
        int i = 0;
        //Links of the user that is compared to reference user
        //A while loop that ends if it runs out of reference users or
        //over 100 links are added to systemLinks
        List<Integer> us = API.getRSSUsers();
        List<Integer> removed = API.getRemovedLinks(me.getUid());
        List<systemlinkModel> li = new ArrayList<systemlinkModel>();
        int linkAmount = 0;
        while(i < users.size() && linksHandled < 100){
            //Temp list used to contain all links of currently checked user
            List<linkModel> links = new ArrayList<linkModel>();
            //Getting current reference user
            matchedUserModel m = users.get(i);
            //nested loop, comparing all the links together and finding new ones
            for(int x = 0;x < m.getUser().getLinks().size();x++){
                //Boolean value to remember if a matching link has been found
                boolean match = false;
                /*
                 * Going through all the links again and adding links that don't
                 * match the links that our user already has
                 */
                for(int n = 0;n < me.getLinks().size();n++)
                {
                    if(m.getUser().getLinks().get(x).getID() == (me.getLinks().get(n).getID()))
                    {
                         match = true;
                         break;
                    }
                }
                //If no matching link has been found, it it's added to links List
                if(match == false)
                {
                    boolean f = false;
                    linkModel n = m.getUser().getLinks().get(x);
                    if(removed != null){
                        for(int e = 0; e < removed.size();e++){
                            if(removed.get(e).intValue() == n.getID()) f = true;
                        }
                    }
                    if(f == false)
                    {
                        links.add(n);
                        linkAmount++;
                    }
                    
                }
                if(linkAmount == 16){
                    linkAmount = 0;
                    break;
                }
                
            }
            //Going through added links
            for(int x = 0;x < links.size();x++)
            {
                /*
                 * A loop that is done for each link
                 */

                //--- RE-WRITING HERE -----
                //Variables to store scores per link 
                double linkScore = 0;
                double d = m.getMatchScore();
                //Getting the currently processed link
                //String link = links.get(x);
                //Variable to remember how often the same link appears among reference users
                int appearance = 0;
                //Going through users again to count appearances
                for(int l = 0;l < users.size();l++)
                {
                    //Currently processed user
                    matchedUserModel u = users.get(l);
                    //Going through all the links of currently processed user
                    for(int t = 0;t < u.getUser().getLinks().size();t++)
                    {
                        //If link is found from the current user, appearance
                        //is incremented and loop broken
                        if(links.get(x).getID() == u.getUser().getLinks().get(t).getID())
                        {
                            appearance++;
                            break;
                        }
                    }
                }              
                
                /*
                 * Calling the score calculations
                 */
                linkScore = countLinkScore(users.size(),appearance,m.getMatchScore());
               
                //System.err.println(linkScore);
                //Adding new link to system links
                li.add(new systemlinkModel(me.getUid(),links.get(x).getID(),linkScore));
                linksHandled++;
            }
            i++;

        }
        if(!us.contains(me.getUid())){
            if(li.size() > 0) updateScores(li);
        }
        Long delta = countDelta(start);
        System.err.println("handled syslinks in "+delta+" seconds!");
    }
    
    public double countScore(double matchScore, int appearance, long time)
    {
                /*
                 * NEW FORMULA July 11th 2011
                 * d = points from formula 1
                 * f = appearance of the link amog reference users
                 * g = update interval
                 * h = hours passed since submission into system(if it's bigger than than g, h is set
                 * automatically equal to g
                 * Formula: d * f * {0.9 * [( g - h ) / g] + 0.1}
                 */
            //linkScore = ((m.getMatchScore()*(double)100)*(double)appearance)*((double)0.9*((hoursPassed)/(double)interval)+(double)0.1);
            //An update interval that is set to 24 hours
            double interval = 24;
            //Getting current time
            long now = new Date().getTime();
            //long hours = (((added/(long)1000)/(long)60)/(long)60);
            long delta = now - time;
            //////.println("Now - "+now+" added - "+added);
            long hours = delta/(long)3600000;
            double hoursPassed;
            if((double)hours < interval) hoursPassed = interval - (double)hours;
            else hoursPassed = interval;
            double linkScore = ((matchScore*(double)100)*(double)appearance)*((double)0.9*((hoursPassed)/(double)interval)+(double)0.1);
            return linkScore;
    }
    
    public double countMatchScore(int linksInDB, int linksInCommon){
        /*
         * a = the number of links that the reference user has in the DB
         * b = the number of common links between the logged-on user and the user in the system
         */
        double a = (double)linksInDB;
        double b = (double)linksInCommon;
        double score = 0;
        /*
         * Score for the user
         * IF there is only one link in common, the score is:
         * [0.5*(ln 2/ln b)]*100
         */
        if(a == 1 && b == 1) return 0;
        if(linksInCommon == 0) return 0;
        else if(linksInCommon == 1){
            System.err.println("Only one link in common!");
            double s = Math.log(2);
            a = Math.log(a);
            score = (0.5*(s/a))*100;
        }
        /* Else
         * {(ln a/ln b)}*100 = (log(b)a)*100
         */
        else {
            a = Math.log(a);
            b = Math.log(b);
            System.err.println("More than one link in common!");
            score = (b/a)*100;
        }
        return score;
    }

    public double countLinkScore(int numberOfRefUsers, int numb, double formula1){
        //Number of other reference users -1 currently checked user
        double c = (double) numberOfRefUsers;
        //Number of other reference users with same link in common
        double d = (double) numb;
        //Score frorm countMatchScore
        double refScore = formula1;
        double result = 0;
        //System.err.println("Items: "+c+" "+d+" "+refScore);
        //double result = 100.0*(d/c)+(1.0-(d/c))*refScore;
        if(c > 1)
            {
            result = refScore+((100.0-refScore)*(d/c));
        }
        else{
            result = refScore;
        }
        //System.err.println("Link score: "+result);
        return result;
    }
            
    public boolean updateScores(List<systemlinkModel> lista){
                Long start = new Date().getTime();
                API.removeSysLinks(me.getUid());
                for(int i = 0;i < lista.size();i++){
                    systemlinkModel l = lista.get(i);
                    //System.err.println(l.getRelevance());
                    double sLD = API.sysLinkLookup(l.getUID(),l.getLinkID());
                    if(sLD == -1)
                    {
                        boolean sID = API.addSysLink(l);

                    }
                    else
                    {
                        if(sLD < l.getRelevance())
                        {
                            API.updateSysLink(l.getLinkID(),l.getUID(),l.getRelevance());
                        }
                    }
                }
                Long delta = countDelta(start);
                System.err.println("added sysLinks in "+delta+" seconds!");
                return true;
    }
}


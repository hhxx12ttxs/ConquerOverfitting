/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ccsim;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Käyttäjä
 */
public class CCSim {
    
    public static ArrayList<Queue> queues = new ArrayList<Queue>();
    public static ArrayList<Call> incomingCalls = new ArrayList<Call>();
    public static int incomimgcallsTEMP;
    
    public static ArrayList<AgentGroup> agentGroups = new ArrayList<AgentGroup>();
    
    public static ArrayList<List<Integer>> combis = new ArrayList<List<Integer>>();
    
    public static int acceptableAnswerTime = 15;
    public static int callsAnsweredIndesiredTime = 0;
    public static int callsNotAnsweredIndesiredtime = 0;
    public static ArrayList<CombinationInfo> combinationInfos = new ArrayList<CombinationInfo>();
    
    public static int simLength = 1800;
    public static int rounds = 100;
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Luetaan asetukset
        readSettings();
        
        combis = makeCombinations(agentGroups);
        
//        for(int i = 0; i < combis.size(); i++) {
//            for(int j = 0; j < combis.get(i).size(); j++) {
//                System.out.print(combis.get(i).get(j) + " ");
//            }
//            System.out.println();
//        }
        
//        int[] combi1 = new int[2];
//        combi1[0] = 2;
//        combi1[1] = 0;
//        int[] combi2 = new int[2];
//        combi2[0] = 3;
//        combi2[1] = 2;
//        combis.add(combi1);
//        combis.add(combi2);
        
        for(int k = 0; k < combis.size(); k++) {
            System.out.println();
            System.out.println("Combination " + (k+1));
            int[] combi = returnIntArray(combis.get(k));
            
            makeAgentsIntoGroups(combi);
            
            CombinationInfo combInfo = new CombinationInfo();
            combInfo.setCombi(combi);
            combinationInfos.add(combInfo);
            // this many rounds per combination
            for(int i = 0; i < rounds; i++) {
                //System.out.println("Round " + (i+1) + ":");
                makeCalls();
                // run teh simulation for this combination of agents
                runCombination(combi);
                calculateAndSaveRoundInfo(i, combi);
                reset();
            }
            calculateAndSaveCombinationInfo();
            resetCombination();
        }
        
    } // main end
    
    private static void readSettings() {
        // Luetaan sitte filu
        
        /*
         * Luodaan jonot kun on luettu puhelutyyppien määrä
         */
//        for(int i = 0; i < 2; i++) {
//            Queue q = new Queue("A");
//            queues.add(q);
//        }
        
        // Luodaan agentit ku o luettu tarpeelliset jutut
        int groupCount = 1;
        for(int k = 0; k < groupCount; k++) {
            AgentGroup ag = new AgentGroup("S" + Integer.toString(k+1));
            ag.setCost(9.5);
            ArrayList<String> skills = new ArrayList<String>();
            ArrayList<Double> lambdas = new ArrayList<Double>();
            String ski = "A";
            skills.add(ski);
            lambdas.add(new Double(240));            
            ag.setSkills(skills);
            ag.setLambdas(lambdas);
            ag.setAgentsMin(1);
            ag.setAgentsMax(55);
            agentGroups.add(ag);
            
            System.out.println("Agentgroup " + ag.getName() + " created.");
            Queue q = new Queue(ski);
            queues.add(q);
            ski = "B";
        }
        //makeAgentsIntoGroups(groupCount);
    }
    
    private static ArrayList<List<Integer>> makeCombinations(ArrayList<AgentGroup> groups) {
        ArrayList<List<Integer>> ret = new ArrayList<List<Integer>>();
        
        ImmutableList toCartesian = null;
        
        ArrayList<Set<Integer>> tmpset = new ArrayList<Set<Integer>>();
        
        for(int i = 0; i < groups.size(); i++) {
            AgentGroup gr = groups.get(i);
            Set<Integer> t = new TreeSet<Integer>();
            
            for(int j = gr.getAgentsMin(); j <= gr.getAgentsMax(); j++) {
                t.add(j);
            }
            tmpset.add(t);
        }
        
        toCartesian = ImmutableList.builder().addAll(tmpset).build();
        Set<List<Integer>> tmpper = Sets.cartesianProduct(toCartesian);
        
        Iterator it = tmpper.iterator();
        
        while(it.hasNext()) {
            List<Integer> ob = (List<Integer>)it.next();
            ret.add(ob);
        }
        
        return ret;
    }
    
    private static void makeCalls() {
        Call c;
        int calltime = 0;
        
        while(calltime < simLength) {
            c = new Call("A");
            c.setArrivalRate(5);
            // c.setLength((i+1)*2);
            //calltime += (i+i*2)+2;
            calltime += calculateCallTime(c.getArrivalRate());
            
            if(calltime > simLength) {
                break;
            }
            
            c.setCallTime(calltime);
            c.setAvgLen(240.0);
            incomingCalls.add(c);
            //System.out.println("calltime: " + calltime);
        }
        incomimgcallsTEMP = incomingCalls.size();
        // sortataan käännettyyn järjestykseen
        Collections.sort(incomingCalls, new sortByCalltime());
        
        /*
        for(int i = 0; i < incomingCalls.size(); i++) {
            System.out.println(incomingCalls.get(i).getCallTime());
        }
        */
        
        //System.out.println("In total " + incomingCalls.size() + " calls generated.");
    } // makeCallss end
    
    private static void makeAgentsIntoGroups(int[] combi) {
        
        for(int k = 0; k < combi.length; k++) {            
            //System.out.println("combi k: " + combi[k]);
            AgentGroup agr = agentGroups.get(k);
            
            for(int i = 0; i < combi[k]; i++) {
                Agent a = new Agent();
                agr.addAgent(a);
            }            
            
            System.out.println(agr.getAgents().size() + " agents created "
                    + "for group " + agr.getName());
        }
    }
    
    // this is where the magic happens
    private static void runCombination(int[] combi) {
        int simCurrentTime = 0;
        
        while(simCurrentTime < simLength) {
            
            /*
             * Tarkista jonot, jos puheluita, koitetaan vastata niihin
             * ennen uusia puheluita.
             */
            for(int i = 0; i < queues.size();i++) {
                Queue q = queues.get(i);
                String calltype = q.callType;
                for(int j = 0; j < q.getCallCount(); j++) {
                    Call call = q.getCalls().get(j);
                    boolean noFreeAgents = false;
                    for(int k = 0; k < agentGroups.size(); k++) {
                        AgentGroup agr = agentGroups.get(k);
                        if(agr.getSkills().contains(calltype)) {
                            Agent ag = selectAgent(agr);
                            if(ag != null) {
                                double lambda = returnLambda(agr, calltype);
                                int callLen = calculateCallLength(lambda); // call.getAvgLen()
                                //System.out.println("callLen from queue " + callLen);
                                ag.setCallRemainingInSecs(callLen);
                                ag.setAvailable(Boolean.FALSE);
                                
                                int waitTime = simCurrentTime - call.getCallTime();
                                
                                if(waitTime <= acceptableAnswerTime) {
                                    callsAnsweredIndesiredTime++;
                                }
                                else {
                                    callsNotAnsweredIndesiredtime++;
                                }
                                //System.out.println("Jonosta vastatun puh calltime: " + call.getCallTime()
                                //        + " ja kesto: " + callLen + " jonotusaika: " + waitTime);
                                //System.out.println("puheluita jonossa " + q.getCalls().size()
                                //        + " j: " + j);
                                
                                q.getCalls().remove(j);
                                if(q.getCallCount() == 0) {
                                    noFreeAgents = true;
                                }
                            }
                            else {
                                // Breakkaa pois pitkälle koska ei vapaita ag
                                //noFreeAgents = true;
                                break;
                            }
                        }
                        if(noFreeAgents) {
                            break;
                        }
                    }
                }
            }
            
            ArrayList<Call> callsThisSecond = new ArrayList<Call>();
            /*
             * Hae kaikki puhelut joidenka calltime on kuluva sekunti,
             * poista löytyneet incomingcalls-listasta
             */
            for(int i = incomingCalls.size()-1; i >= 0; i--){
                Call c = incomingCalls.get(i);
                int calltime = c.getCallTime();
                if(calltime > simCurrentTime) {
                    break;
                }
                else if(calltime == simCurrentTime) {
                    callsThisSecond.add(c);
                    incomingCalls.remove(i);
                }
            }
            
            /*
             * Käy läpi puhelut ja etsi niille agentti,
             * jos ei löydy vastaajaa, lisää puhelu oikeaan jonoon.
             */ 
            for(int i = 0; i < callsThisSecond.size(); i++) {
                Call call = callsThisSecond.get(i);
                String calltype = call.getType();
                for(int j = 0; j < agentGroups.size(); j++) {
                    AgentGroup agr = agentGroups.get(j);
                    if(agr.getSkills().contains(calltype)) {
                        // Kutsutaan metodia joka tekee asetusten mukaisen
                        // valinnan agentin valintaan
                        Agent ag = selectAgent(agr);
                        if(ag != null) {
                            double lambda = returnLambda(agr, calltype);
                            int callLen = calculateCallLength(lambda); // call.getAvgLen()
                            //System.out.println("callLen " + callLen);
                            ag.setCallRemainingInSecs(callLen);
                            ag.setAvailable(Boolean.FALSE);
                            //System.out.println("Heti vastatun puh calltime: " + call.getCallTime()
                            //        + " ja kesto: " + callLen);
                            callsAnsweredIndesiredTime++;
                        }
                        else {
                            // Laita puhelu jonoon
                            insertCallToCorrectQueue(call);
                        }
                    }
                }
            }
            
            /*
             * Poista meneillään olevista puheluista (eli varatuista agenteista)
             * sekunti.
             * Tarkista myös jos puhelu loppunut edellisellä sekunnilla
             * ja aseta agentti vapaaksi.
             */
            for(int i = 0; i < agentGroups.size(); i++) {
                AgentGroup agr = agentGroups.get(i);
                ArrayList<Agent> ags = agr.getAgents();
                for(int j = 0; j < ags.size(); j++) {
                    Agent ag = ags.get(j);
                    if(ag.getCallRemainingInSecs() > 0) { // !ag.getAvailable() && 
                        ag.setCallRemainingInSecs(ag.getCallRemainingInSecs()-1);
                    }
                    else if(ag.getCallRemainingInSecs() == 0) {
                        ag.setAvailable(Boolean.TRUE);
                        ag.setCallRemainingInSecs(-1);                        
                    }
                }
            }
            
            //System.out.println("Aika: " + simCurrentTime + " puheluita tulossa: " + incomingCalls.size());
            
            // sekunti etiäpäin
            simCurrentTime++;
        } // simulointi while end
        
        /*
         * Juttujen printtailua
         */
        //System.out.println("Puhelut joihin vastattiin alle AWT: " + callsAnsweredIndesiredTime);
        //System.out.println("Puhelut joihin vastattiin yli AWT: " + callsNotAnsweredIndesiredtime);
        
        //System.out.println("Calls in queue after this combi round: " + queues.get(0).getCallCount());
        
        //System.out.println();
        
    } // runcombi end
    
    private static void reset() {
        callsAnsweredIndesiredTime = 0;
        callsNotAnsweredIndesiredtime = 0;
        //queues = new ArrayList<Queue>();
        
        for(int i = 0; i < queues.size(); i++) {
            queues.get(i).emptyQueue();
        }
        
        incomingCalls = new ArrayList<Call>();
        
        // Aseta agentit vapaiksi ja nollaa kestot
        for(int i = 0; i < agentGroups.size(); i++) {
            ArrayList<Agent> agr = agentGroups.get(i).getAgents();
            for(int j = 0; j < agr.size(); j++) {
                Agent ag = agr.get(j);
                ag.setAvailable(Boolean.TRUE);
                ag.setCallRemainingInSecs(-1);
            }
        }
        
        //System.out.println();
    }
    
    private static void resetCombination() {
        for(int i = 0; i < agentGroups.size(); i++) {
            AgentGroup agr = agentGroups.get(i);
            agr.removeAgents();
        }
    }
    
    private static void calculateAndSaveRoundInfo(int roundNum, int[] combi) {
        // Katotaan jonoon jääneet puhelut
        int tmp = 0;
        for(int i = 0; i < queues.size(); i++) {
            Queue q = queues.get(i);
            tmp += q.getCallCount();
        }
        //System.out.println("Jonoon jaaneet puhelut: " + tmp);
        
        
        CombinationInfo comp = combinationInfos.get(combinationInfos.size()-1);
        //comp.setCombi(combi);
        
        RoundInfo round = new RoundInfo();
        round.setRoundNumber(roundNum);
        callsNotAnsweredIndesiredtime += tmp;
        round.setCallsOverAWT(callsNotAnsweredIndesiredtime);
        round.setCallsUnderAWT(callsAnsweredIndesiredTime);
        round.calculateServicelevel();
        //System.out.println("SL for this round: " + round.getServiceLevel());
        
        round.setCallsTotal(incomimgcallsTEMP);
        
        comp.addRound(round);
        
        //combinationInfos.add(comp);
        //System.out.println("Roundinfo added to combination info.");
    }
    
    private static void calculateAndSaveCombinationInfo() {
        // Lasketaan combinationInfos listan vikan elementin avg SL
        // ja jotain
        
        CombinationInfo comb = combinationInfos.get(combinationInfos.size()-1);
        
        // Lasketaan totalcost
        
        
        // Lasketaan avg SL
        double tmp = 0.0;
        ArrayList<RoundInfo> ro = comb.getRounds();
        System.out.println("Rounds size in combi: " + ro.size());
        for(int i = 0; i < ro.size(); i++) {
            RoundInfo r = ro.get(i);
            tmp += r.getServiceLevel();
        }
        double sl = tmp / (double)ro.size();
        comb.getAvgSL();
        System.out.println("combi avgcalls: " + comb.avgNumberOfCalls());
        System.out.println("combi SL: " + sl);
        
    }
    
    private static double returnLambda(AgentGroup agr, String calltype) {
        double ret = -1.0;
        int index = -1;
        for(int i = 0; i < agr.getSkills().size(); i++) {
            String skill = agr.getSkills().get(i);
            
            if(skill.equals(calltype)) {
                index = i;
                break;
            }
        }
        
        ret = agr.getLambdas().get(index).doubleValue();
        
        return ret;
    }
    
    private static int calculateCallLength(double avg) {
        int ret = -1;
        
        ret = (int)(Math.floor(Math.random()*(avg+(avg/2)))+(avg-(avg/2)));
        
        return ret;
    }
    
    private static int calculateCallTime(double arr) {
        int ret = -1;
        ret = (int)(Math.floor(Math.random()*(arr+(arr/2)))+(arr-(arr/2)));
        return ret;
    }
    
    private static void insertCallToCorrectQueue(Call call) {
        for(int i = 0; i < queues.size(); i++) {
            Queue q = queues.get(i);
            if(q.getCallType().equals(call.getType())) {
                q.addCallToQueue(call);
                break;
            }
        }
    }
    
    private static Agent selectAgent(AgentGroup agr) {
        Agent ret;
        ret = selectFirstAvailable(agr);
        return ret;
    }
    
    private static Agent selectFirstAvailable(AgentGroup agr) {
        Agent ret = null;
        ArrayList<Agent> ags = agr.getAgents();
        for(int i = 0; i < ags.size(); i++) {
            Agent ag = ags.get(i);
            if(ag.getAvailable()) {
                ret = ag;
            }
        }
        
        return ret;
    }
    
    private static int[] returnIntArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        
        for(int i = 0; i < list.size(); i++) {
            ret[i] = list.get(i).intValue();
        }
        
        return ret;
    }
}


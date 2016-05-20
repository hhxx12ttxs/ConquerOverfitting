package net.jacobandreas.steph.tournament;

import java.util.*;

public class Team {

    private String name;
    private int number;

    private LinkedList<Team> program;
    
    private LinkedList<Ballot> ballots;

    private int rank;
    private double sowRank;
    private double solRank;

    /**
     * Creates a new Team with the given name and number
     * @param name The human-readable team name
     * @param number The unique team number
     */
    public Team(String name, int number) {

        this.name = name;
        this.number = number;

        program = new LinkedList<Team>();

        ballots = new LinkedList<Ballot>();

        rank = 0;
        sowRank = 0;
        solRank = 0;

    }

    public String getName() { return name; }
    public int getNumber() { return number; }
    public int getPointDifferential() { return getPointDifferentialDropping(0); }
    public int getPoints() { return getPointsDropping(0); }
    public int getRank() { return rank; }
    public double getSOWRank() { return sowRank; }
    public double getSOLRank() { return solRank; }
    public double getSOORank() { return getSOWRank() + getSOLRank(); }

    public Collection<Team> getProgram() { return Collections.unmodifiableCollection(program); }

    public List<Ballot> getBallots() { return Collections.unmodifiableList(ballots); }

    /**
     * Gets a list of all the teams this team can't hit (i.e. teams from the same
     * school and teams it's already hit)
     * @return a list of teams this team can't hit.
     */
    public Collection<Team> getImpermissibles() {
        LinkedList<Team> imps = new LinkedList<Team>();
        imps.addAll(getHistory());
        imps.addAll(getProgram());
        return Collections.unmodifiableCollection(imps);
    }

    /**
     * Calculates the number of ballots won by this team's opponents.
     * @return this team's CS
     */
    public double getCombinedStrength() {
        double cs = 0;
        for(Team t : getHistory()) {
            cs += t.getWins();
        }
        return cs;
    }

    /**
     * Calculates the number of ballots won by teams this team has beaten or
     * tied.
     * @return This team's strength of win
     */
    public double getStrengthOfWin() {
        double sow = 0;
        for(Ballot b : getBallots()) {
            int bpd = b.getPDFor(this);
            if(bpd > 0) {
                sow += b.getOpponentFor(this).getWins();
            } else if(bpd == 0) {
                // for a tie, we count each ballot as half a point for SOW
                sow += b.getOpponentFor(this).getWins() / 2;
            }
        }
        return sow;
    }

    /**
     * Calculates the number of ballots won by teams this team has lost to.
     * @return This team's strength of loss
     */
    public double getStrengthOfLoss() {
        double sol = 0;
        for(Ballot b : getBallots()) {
            int bpd = b.getPDFor(this);
            if(bpd < 0) {
                sol += b.getOpponentFor(this).getWins();
            } else if(bpd == 0) {
                // for a tie, we count each ballot as half a point for SOL
                sol += b.getOpponentFor(this).getWins() / 2;
            }
        }
        return sol;
    }

    /**
     * Gets this team's total point differential, dropping the count highest and
     * lowest scores.
     * @param count the number of ballots to drop from each end
     * @return the cumulative point differential without the dropped ballots
     */
    public int getPointDifferentialDropping(int count) {
        int pd = 0;
        for(int i = count; i < getBallots().size() - count; i++) {
            pd += getBallots().get(i).getPDFor(this);
        }
        return pd;
    }

    /**
     * Gets this team's raw point total, dropping the count highest and lowest
     * scores.
     * @param count the number of ballots to drop from either end
     * @return the cumulative point total without the dropped ballots
     */
    public int getPointsDropping(int count) {
        int points = 0;
        for(int i = count; i < getBallots().size() - count; i++) {
            points += getBallots().get(i).getTotalFor(this);
        }
        return points;
    }

    /**
     * Gets this team's win record, with each ballot won counting as 1 point and
     * each tie counting as .5
     * @return The team's win record
     */
    public double getWins() {
        double wins = 0;
        for(Ballot b : getBallots()) {
            int pd = b.getPDFor(this);
            if(pd == 0) {
                wins += .5;
            } else if(pd > 0) {
                wins += 1;
            }
        }
        return wins;
    }

    /**
     * Gets the teams this team has hit in the past.
     * @return this team's history
     */
    public List<Team> getHistory() {
        LinkedList<Team> history = new LinkedList<Team>();
        for(Ballot b : getBallots()) {
            Team opp = b.getOpponentFor(this);
            if(!history.contains(opp)) {
                history.add(opp);
            }
        }
        return Collections.unmodifiableList(history);
    }

    /**
     * Gets the side of the case this team last competed on.
     * @return this team's last side
     */
    public int getLastSide() {
        return getBallots().get(getBallots().size() - 1).getSideFor(this);
    }

    /**
     * Add another team to this team's program (the other teams from the same
     * school). This will NOT add this team to t's program, so make sure to
     * do that as well.
     * @param t the other program member.
     */
    public void addProgram(Team t) {
        program.add(t);
    }

    /**
     * Remove a team from this team's program
     * @param t the team to remove.
     */
    public void removeProgram(Team t) {
        program.remove(t);
    }

    /**
     * Adds a ballot to this team's record
     * @param b the ballot to add
     */
    public void addBallot(Ballot b) {
        ballots.add(b);
    }

    /**
     * Removes a ballot from this team's record
     * @param b the ballot to remove
     */
    public void removeBallot(Ballot b) {
        ballots.remove(b);
    }

    /**
     * Determines whether this team can hit another team
     * @param t the team to check
     * @return true if a match between t and this team is permitted, and false
     * otherwise
     */
    public boolean canHit(Team t) {
        return !(getProgram().contains(t) || getHistory().contains(t));
    }

    /**
     * Set this team's rank (i.e. the rank number on this team's tab card).
     * @param rank the rank to set
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Set this team's strength of win rank (that is, the ranking of its SOW
     * compared to other teams with the same PD and CS).
     * @param sowRank the SOW rank to set
     */
    public void setSOWRank(double sowRank) {
        this.sowRank = sowRank;
    }

    /**
     * Set this team's strength of loss rank (that is, the ranking of its SOL
     * compared to other teams with the same PD and CS).
     * @param sowRank the SOL rank to set
     */
    public void setSOLRank(double solRank) {
        this.solRank = solRank;
    }

    @Override
    public String toString() {
        return getNumber() + " (" + getName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(o.getClass() != Team.class) {
            return false;
        }
        Team t = (Team)o;
        return t.getNumber() == number;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.number;
        return hash;
    }

}


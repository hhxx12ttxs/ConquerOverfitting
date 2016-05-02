import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {

  public static ArrayList<Double> listBuilder(List<Tuple> values) {
    ArrayList<Double> population = new ArrayList<Double>();
    for(Tuple value : values) {
      for(int i=0; i<value.x; i++) {
        population.add(value.y);
      }
    }
    return population;
  }

  public static class Tuple {
    public final int x;
    public final double y;
    public Tuple(int x, double y) {
      this.x = x;
      this.y = y;
    }
  }

  public static ArrayList<Party> partyHistory = new ArrayList<Party>();

  // returns a list of lists of indexes where there are parties, each outer list being
  // a party
  // also do not consider parties that have less than two people in them total
  public static ArrayList<Party> detectParties(int currentIteration, List<Double> opinions) {
    ArrayList<Party> parties = new ArrayList<Party>();
    ArrayList<Integer> currentParty = new ArrayList<Integer>();
    int currentPeak = 0;
    boolean movingDown = false, movingUp = false;
    for(int i=0; i<opinions.size() - 1; i++) {
      double currentOpinion = opinions.get(i);
      double nextOpinion = opinions.get(i+1);
      double diff = nextOpinion - currentOpinion;
      if(Math.abs(diff) > Constants.partyChangeThreshold) {
        if(diff > 0) {
          movingUp = true;
          // moving up
          if(movingDown) {
            // if were previously moving down, then we are at a local minimum, or the cutoff between
            // parties and their influence tails
            parties.add(new Party(currentParty, currentPeak, currentIteration));
            currentParty = new ArrayList<Integer>();
            currentPeak = 0;
            movingDown = false;
          }
        } else {
          // moving down
          movingDown = true;
          // at a peak
          if(movingUp) {
            movingUp = false;
            currentPeak = i;
          }
        }
      }
      currentParty.add(i);
    }
    // make sure to add the last party at the end
    parties.add(new Party(currentParty, currentPeak, currentIteration));

    // don't consider parties (with a mass less than 2?) that don't have any opinions with more than
    // partyMaxThreshold people - this means that party is very spread out
    for(int p=0; p<parties.size(); p++) {
      Party party = parties.get(p);
      ArrayList<Integer> indexes = party.indexes;
      if(partyMax(indexes, opinions) < Constants.partyMaxThreshold) {
        parties.remove(party);
      }
    }

    // remove tails of parties
    for(int p=0; p<parties.size(); p++) {
      Party party = parties.get(p);
      ArrayList<Integer> indexes = party.indexes;
      double mass = partyMass(indexes, opinions);
      for(int pi=0; pi<indexes.size(); pi++) {
        int index = indexes.get(pi);
        if(opinions.get(index)/mass < Constants.partyMassThreshold) {
          indexes.remove(pi);
          // need to do this because just removed a value, so latter values shifted down,
          // so need to look at current index again so can check it
          pi--;
        }
      }
      parties.set(p, party);
    }

    // initialize parties or check for continuing ones
    if(partyHistory.size() == 0) {
      partyHistory.addAll(parties);
    } else {
      // check if any new parties have peaks within the cutoff for same party of any of the previous
      // parties (also update the peak)
      // if any previous parties are not in this, end them
      // if any new parties are not in the previous ones, add them

      // iterate over new parties, checking if same as old, else add to, then afterwards end any non-updated and
      // non-new parties
      for(Party newParty : parties) {
        int closestPeakDiff = -1;
        Party matchingOldParty = null;
        int matchingOldPartyIndex = -1;
        for(int p=0; p<partyHistory.size(); p++) {
          Party oldParty = partyHistory.get(p);
          if(!oldParty.isEnded()) {
            // check same party
            int peakDiff = Math.abs(oldParty.getPeak() - newParty.getPeak());
            if(peakDiff <= Constants.peakDiffThreshold) {
              // if within range, check if closest yet; if closest, set as such, else ignore
              if(closestPeakDiff == -1 || peakDiff < closestPeakDiff) {
                closestPeakDiff = peakDiff;
                matchingOldParty = oldParty;
                matchingOldPartyIndex = p;
                newParty.modified = true;
              }
            }
          }
        }
        // if modified new party then it matches old so need to set the old party to having its new peak
        if(newParty.modified) {
          matchingOldParty.setPeak(newParty.getPeak());
          matchingOldParty.modified = true;
          partyHistory.set(matchingOldPartyIndex, matchingOldParty);
        }

        // didn't match any old ones, add as new
        if(!newParty.modified) {
          // so doesn't get ended
          newParty.modified = true;
          partyHistory.add(newParty);
        }
      }
      for(int p=0; p<partyHistory.size(); p++) {
        Party oldParty = partyHistory.get(p);
        if(!oldParty.isEnded() && !oldParty.modified) {
          oldParty.end(currentIteration);
        }
        oldParty.modified = false;
      }
    }
//    System.out.println("NEW HISTORY = " + Arrays.toString(partyHistory.toArray()));
    return parties;
  }

  private static double partyMass(ArrayList<Integer> party, List<Double> opinions) {
    double mass = 0.0;
    for(int i : party) {
      mass += opinions.get(i);
    }
    return mass;
  }

  private static double partyMax(ArrayList<Integer> party, List<Double> opinions) {
    double max = -1.0;
    for(int i : party) {
      double opinion = opinions.get(i);
      if(opinion > max) {
        max = opinion;
      }
    }
    return max;
  }

  public static <T> ArrayList<T> flatten(ArrayList<ArrayList<T>> list) {
    ArrayList<T> flat = new ArrayList<T>();
    for(ArrayList<T> sublist : list) {
      flat.addAll(sublist);
    }
    return flat;
  }
}

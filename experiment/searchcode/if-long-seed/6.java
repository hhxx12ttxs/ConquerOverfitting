* Seed (levels generation)
*
* @author Lucas LAZARE
*/
public class Seed {
private long alphaSeed;
private long betaSeed;
public Seed() {
Random random = new Random();
this.alphaSeed = random.nextLong();
this.betaSeed = random.nextLong();


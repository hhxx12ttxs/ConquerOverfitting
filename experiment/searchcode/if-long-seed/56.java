* Making sure, that seeds are unique.
*/
public class DuplicateCheck implements SeedAccepter {

private Set<Long> collected = new HashSet<>();

/**
* @throws if <code>seed</code> was already supplied.


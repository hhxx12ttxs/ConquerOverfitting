import model.domains.MazeGameDomain;

public class DomainFactory {

public static SearchDomain createDomain (String domain) {

if (domain.equals(&quot;Maze&quot;))
return new MazeGameDomain();

else if (domain.equals(&quot;8puzzle&quot;))


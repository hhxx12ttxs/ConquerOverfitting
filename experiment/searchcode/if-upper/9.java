import org.bukkit.entity.Player;

public class Zone {
int lowerX;
int upperX;
int lowerY;
int upperY;
int lowerZ;
int upperZ;
Boolean isPlayerInside(Player player){
Location playerLocation = player.getLocation();
if (
playerLocation.getX() < upperX &amp;&amp; playerLocation.getX() > lowerX &amp;&amp;


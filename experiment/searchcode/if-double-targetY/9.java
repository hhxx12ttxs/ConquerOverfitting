public double targetX = 0;
public double targetY = 0;

public Snake targetPlayer;

public MoveToTargetController(Slither slither) {
public void render(Graphics g, double ox, double oy) {
if (targetPlayer != null) {
targetX = targetPlayer.x + targetPlayer.fx;


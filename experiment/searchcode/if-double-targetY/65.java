public static DijkstraNode findClosestNode(HashMap<DijkstraNode, DijkstraNode> nodes, int targetX, int targetY){
double smallestDistance = 100000;
double distance = findDistance(node.getCurrentX(), node.getCurrentY(), targetX, targetY);
if (distance <= smallestDistance){
smallestDistance = distance;


private Marker[] marker;

public Student(boolean[] blackboard, int prime) {
this.blackboard = blackboard;
@Override
public void run() {

if (blackboard[prime]) {

int markerCounter = 0;
int i = prime + 1;


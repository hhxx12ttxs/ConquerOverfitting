private float _lastLength = 0;
private int _lastIndex = 0;

public List<SegmentRange> getSegments(float[] accelerationValues)
List<SegmentRange> oscillations = new LinkedList<SegmentRange>();

_spike = new SingleSpike(accelerationValues[0]);

int initialIndex = 0;

for (int index = 0; index < accelerationValues.length; ++index)


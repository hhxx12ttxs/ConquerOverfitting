public static final int PLAYING = 4;
public static final int CUED = 8;

private int mode = STOPPED;
private int nsteps = 32;
@SuppressWarnings(&quot;unchecked&quot;)
public PatternRecorder(AbletonTracks obj, int nsteps){
this.co = obj;
this.nsteps = nsteps;


@Column(name = &quot;TS_TABLEID&quot;)
private int tsTableid;

@Column(name = &quot;TS_LASTID&quot;)
private Integer tsLastid;
public TsLastids(Integer tsId) {
this.tsId = tsId;
}

public TsLastids(Integer tsId, String tsName, int tsTableid) {


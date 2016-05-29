private long findIdByMarker(Marker marker){
if(mMarkerIdMap.containsKey(marker)){
return mMarkerIdMap.get(marker);
}

Set<Entry<Long, Marker>> entries = mMarkerMap.entrySet();
Marker m = entry.getValue();
if(m.equals(marker)){
mMarkerIdMap.put(marker, entry.getKey());
return entry.getKey();


List<TbTrack> tbTrackList = tbTrackDAO.findByTrackPartName(content);
List<Track> trackList = null;
if(tbTrackList.size() > 0){
List<TbTrack> tbOtherTrackList = tbTrackDAO.findByTrackAlbumId(tbCurrentTrack.getTrackAlbumId());
if(tbOtherTrackList.size() > 0){
otherTrackList = new ArrayList<Track>();
for(TbTrack tbTrack : tbOtherTrackList){


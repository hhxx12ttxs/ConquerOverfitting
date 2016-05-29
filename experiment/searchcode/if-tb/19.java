List<TbArtist> tbArtistList = tbArtistDAO.findByArtistPartName(content);
List<Artist> artistList = null;
if(tbArtistList.size() > 0){
artistList = new ArrayList<Artist>();
List<TbArtistlist> tbArtistTrackList = tbArtistlistDAO.findByArtistId(artistId);
if(tbArtistTrackList.size() > 0){
artistTrackList = new ArrayList<Track>();


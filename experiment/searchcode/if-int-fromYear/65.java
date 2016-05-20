/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package net.sourceforge.subsonic.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import net.sourceforge.subsonic.Logger;
import net.sourceforge.subsonic.ajax.ChatService;
import net.sourceforge.subsonic.ajax.LyricsInfo;
import net.sourceforge.subsonic.ajax.LyricsService;
import net.sourceforge.subsonic.command.UserSettingsCommand;
import net.sourceforge.subsonic.dao.AlbumDao;
import net.sourceforge.subsonic.dao.ArtistDao;
import net.sourceforge.subsonic.dao.MediaFileDao;
import net.sourceforge.subsonic.domain.Album;
import net.sourceforge.subsonic.domain.Artist;
import net.sourceforge.subsonic.domain.MediaFile;
import net.sourceforge.subsonic.domain.MusicFolder;
import net.sourceforge.subsonic.domain.MusicIndex;
import net.sourceforge.subsonic.domain.Player;
import net.sourceforge.subsonic.domain.PlayerTechnology;
import net.sourceforge.subsonic.domain.Playlist;
import net.sourceforge.subsonic.domain.PodcastChannel;
import net.sourceforge.subsonic.domain.PodcastEpisode;
import net.sourceforge.subsonic.domain.RandomSearchCriteria;
import net.sourceforge.subsonic.domain.SearchCriteria;
import net.sourceforge.subsonic.domain.SearchResult;
import net.sourceforge.subsonic.domain.Share;
import net.sourceforge.subsonic.domain.TranscodeScheme;
import net.sourceforge.subsonic.domain.TransferStatus;
import net.sourceforge.subsonic.domain.User;
import net.sourceforge.subsonic.domain.UserSettings;
import net.sourceforge.subsonic.service.AudioScrobblerService;
import net.sourceforge.subsonic.service.JukeboxService;
import net.sourceforge.subsonic.service.MediaFileService;
import net.sourceforge.subsonic.service.PlayerService;
import net.sourceforge.subsonic.service.PlaylistService;
import net.sourceforge.subsonic.service.PodcastService;
import net.sourceforge.subsonic.service.RatingService;
import net.sourceforge.subsonic.service.SearchService;
import net.sourceforge.subsonic.service.SecurityService;
import net.sourceforge.subsonic.service.SettingsService;
import net.sourceforge.subsonic.service.ShareService;
import net.sourceforge.subsonic.service.StatusService;
import net.sourceforge.subsonic.service.TranscodingService;
import net.sourceforge.subsonic.util.StringUtil;
import net.sourceforge.subsonic.util.XMLBuilder;

import static net.sourceforge.subsonic.security.RESTRequestParameterProcessingFilter.decrypt;
import static net.sourceforge.subsonic.util.XMLBuilder.Attribute;
import static net.sourceforge.subsonic.util.XMLBuilder.AttributeSet;

/**
 * Multi-controller used for the REST API.
 * <p/>
 * For documentation, please refer to api.jsp.
 *
 * @author Sindre Mehus
 */
public class RESTController extends MultiActionController {

    private static final Logger LOG = Logger.getLogger(RESTController.class);

    private SettingsService settingsService;
    private SecurityService securityService;
    private PlayerService playerService;
    private MediaFileService mediaFileService;
    private TranscodingService transcodingService;
    private DownloadController downloadController;
    private CoverArtController coverArtController;
    private AvatarController avatarController;
    private UserSettingsController userSettingsController;
    private LeftController leftController;
    private HomeController homeController;
    private StatusService statusService;
    private StreamController streamController;
    private ShareService shareService;
    private PlaylistService playlistService;
    private ChatService chatService;
    private LyricsService lyricsService;
    private net.sourceforge.subsonic.ajax.PlaylistService playlistControlService;
    private JukeboxService jukeboxService;
    private AudioScrobblerService audioScrobblerService;
    private PodcastService podcastService;
    private RatingService ratingService;
    private SearchService searchService;
    private MediaFileDao mediaFileDao;
    private ArtistDao artistDao;
    private AlbumDao albumDao;
            
    public void ping(HttpServletRequest request, HttpServletResponse response) throws Exception {
        XMLBuilder builder = createXMLBuilder(request, response, true).endAll();
        response.getWriter().print(builder);
    }

    public void getLicense(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);

        String email = settingsService.getLicenseEmail();
        String key = settingsService.getLicenseCode();
        Date date = settingsService.getLicenseDate();
        boolean valid = settingsService.isLicenseValid();

        AttributeSet attributes = new AttributeSet();
        attributes.add("valid", valid);
        if (valid) {
            attributes.add("email", email);
            attributes.add("key", key);
            attributes.add("date", StringUtil.toISO8601(date));
        }

        builder.add("license", attributes, true);
        builder.endAll();
        response.getWriter().print(builder);
    }

    public void getMusicFolders(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);
        builder.add("musicFolders", false);

        for (MusicFolder musicFolder : settingsService.getAllMusicFolders()) {
            AttributeSet attributes = new AttributeSet();
            attributes.add("id", musicFolder.getId());
            attributes.add("name", musicFolder.getName());
            builder.add("musicFolder", attributes, true);
        }
        builder.endAll();
        response.getWriter().print(builder);
    }

    public void getIndexes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);

        long ifModifiedSince = ServletRequestUtils.getLongParameter(request, "ifModifiedSince", 0L);
        long lastModified = leftController.getLastModified(request);

        if (lastModified <= ifModifiedSince) {
            builder.endAll();
            response.getWriter().print(builder);
            return;
        }

        builder.add("indexes", "lastModified", lastModified, false);

        List<MusicFolder> musicFolders = settingsService.getAllMusicFolders();
        Integer musicFolderId = ServletRequestUtils.getIntParameter(request, "musicFolderId");
        if (musicFolderId != null) {
            for (MusicFolder musicFolder : musicFolders) {
                if (musicFolderId.equals(musicFolder.getId())) {
                    musicFolders = Arrays.asList(musicFolder);
                    break;
                }
            }
        }

        List<MediaFile> shortcuts = leftController.getShortcuts(musicFolders, settingsService.getShortcutsAsArray());
        for (MediaFile shortcut : shortcuts) {
            builder.add("shortcut", true,
                    new Attribute("name", shortcut.getName()),
                    new Attribute("id", shortcut.getId()));
        }

        SortedMap<MusicIndex, SortedSet<MusicIndex.Artist>> indexedArtists = leftController.getMusicFolderContent(musicFolders).getIndexedArtists();

        for (Map.Entry<MusicIndex, SortedSet<MusicIndex.Artist>> entry : indexedArtists.entrySet()) {
            builder.add("index", "name", entry.getKey().getIndex(), false);

            for (MusicIndex.Artist artist : entry.getValue()) {
                for (MediaFile mediaFile : artist.getMediaFiles()) {
                    if (mediaFile.isDirectory()) {
                        builder.add("artist", true,
                                new Attribute("name", artist.getName()),
                                new Attribute("id", mediaFile.getId()));
                    }
                }
            }
            builder.end();
        }

        // Add children
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);
        List<MediaFile> singleSongs = leftController.getSingleSongs(musicFolders);

        for (MediaFile singleSong : singleSongs) {
            builder.add("child", createAttributesForMediaFile(player, singleSong, username), true);
        }

        builder.endAll();
        response.getWriter().print(builder);
    }

    public void getArtists(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);
        String username = securityService.getCurrentUsername(request);

        builder.add("artists", false);

        List<Artist> artists = artistDao.getAlphabetialArtists(0, Integer.MAX_VALUE);
        for (Artist artist : artists) {
            AttributeSet attributes = createAttributesForArtist(artist, username);
            builder.add("artist", attributes, true);
        }

        builder.endAll();
        response.getWriter().print(builder);
    }

    private AttributeSet createAttributesForArtist(Artist artist, String username) {
        AttributeSet attributes = new AttributeSet();
        attributes.add("id", artist.getId());
        attributes.add("name", artist.getName());
        if (artist.getCoverArtPath() != null) {
            attributes.add("coverArt", CoverArtController.ARTIST_COVERART_PREFIX + artist.getId());
        }
        attributes.add("albumCount", artist.getAlbumCount());
        attributes.add("starred", StringUtil.toISO8601(artistDao.getArtistStarredDate(artist.getId(), username)));
        return attributes;
    }

    public void getArtist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);

        String username = securityService.getCurrentUsername(request);
        Artist artist;
        try {
            int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
            artist = artistDao.getArtist(id);
            if (artist == null) {
                throw new Exception();
            }
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.NOT_FOUND, "Artist not found.");
            return;
        }

        builder.add("artist", createAttributesForArtist(artist, username), false);
        for (Album album : albumDao.getAlbumsForArtist(artist.getName())) {
            builder.add("album", createAttributesForAlbum(album, username), true);
        }

        builder.endAll();
        response.getWriter().print(builder);
    }

    private AttributeSet createAttributesForAlbum(Album album, String username) {
        AttributeSet attributes;
        attributes = new AttributeSet();
        attributes.add("id", album.getId());
        attributes.add("name", album.getName());
        attributes.add("artist", album.getArtist());
        if (album.getArtist() != null) {
            Artist artist = artistDao.getArtist(album.getArtist());
            if (artist != null) {
                attributes.add("artistId", artist.getId());
            }
        }
        if (album.getCoverArtPath() != null) {
            attributes.add("coverArt", CoverArtController.ALBUM_COVERART_PREFIX + album.getId());
        }
        attributes.add("songCount", album.getSongCount());
        attributes.add("duration", album.getDurationSeconds());
        attributes.add("created", StringUtil.toISO8601(album.getCreated()));
        attributes.add("starred", StringUtil.toISO8601(albumDao.getAlbumStarredDate(album.getId(), username)));

        return attributes;
    }

    public void getAlbum(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);

        Album album;
        try {
            int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
            album = albumDao.getAlbum(id);
            if (album == null) {
                throw new Exception();
            }
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.NOT_FOUND, "Album not found.");
            return;
        }

        builder.add("album", createAttributesForAlbum(album, username), false);
        for (MediaFile mediaFile : mediaFileDao.getSongsForAlbum(album.getArtist(), album.getName())) {
            builder.add("song", createAttributesForMediaFile(player, mediaFile, username) , true);
        }

        builder.endAll();
        response.getWriter().print(builder);
    }

    public void getSong(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);

        MediaFile song;
        try {
            int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
            song = mediaFileDao.getMediaFile(id);
            if (song == null || song.isDirectory()) {
                throw new Exception();
            }
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.NOT_FOUND, "Song not found.");
            return;
        }

        builder.add("song", createAttributesForMediaFile(player, song, username), true);

        builder.endAll();
        response.getWriter().print(builder);
    }

    public void getMusicDirectory(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        MediaFile dir;
        try {
            int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
            dir = mediaFileService.getMediaFile(id);
            if (dir == null) {
                throw new Exception();
            }
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.NOT_FOUND, "Directory not found");
            return;
        }

        XMLBuilder builder = createXMLBuilder(request, response, true);
        builder.add("directory", false,
                new Attribute("id", dir.getId()),
                new Attribute("name", dir.getName()));

        for (MediaFile child : mediaFileService.getChildrenOf(dir, true, true, true)) {
            AttributeSet attributes = createAttributesForMediaFile(player, child, username);
            builder.add("child", attributes, true);
        }
        builder.endAll();
        response.getWriter().print(builder);
    }

    @Deprecated
    public void search(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        String any = request.getParameter("any");
        String artist = request.getParameter("artist");
        String album = request.getParameter("album");
        String title = request.getParameter("title");

        StringBuilder query = new StringBuilder();
        if (any != null) {
            query.append(any).append(" ");
        }
        if (artist != null) {
            query.append(artist).append(" ");
        }
        if (album != null) {
            query.append(album).append(" ");
        }
        if (title != null) {
            query.append(title);
        }

        SearchCriteria criteria = new SearchCriteria();
        criteria.setQuery(query.toString().trim());
        criteria.setCount(ServletRequestUtils.getIntParameter(request, "count", 20));
        criteria.setOffset(ServletRequestUtils.getIntParameter(request, "offset", 0));

        SearchResult result = searchService.search(criteria, SearchService.IndexType.SONG);
        builder.add("searchResult", false,
                new Attribute("offset", result.getOffset()),
                new Attribute("totalHits", result.getTotalHits()));

        for (MediaFile mediaFile : result.getMediaFiles()) {
            AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
            builder.add("match", attributes, true);
        }
        builder.endAll();
        response.getWriter().print(builder);
    }

    public void search2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        builder.add("searchResult2", false);

        String query = request.getParameter("query");
        SearchCriteria criteria = new SearchCriteria();
        criteria.setQuery(StringUtils.trimToEmpty(query));
        criteria.setCount(ServletRequestUtils.getIntParameter(request, "artistCount", 20));
        criteria.setOffset(ServletRequestUtils.getIntParameter(request, "artistOffset", 0));
        SearchResult artists = searchService.search(criteria, SearchService.IndexType.ARTIST);
        for (MediaFile mediaFile : artists.getMediaFiles()) {
            builder.add("artist", true,
                    new Attribute("name", mediaFile.getName()),
                    new Attribute("id", mediaFile.getId()));
        }

        criteria.setCount(ServletRequestUtils.getIntParameter(request, "albumCount", 20));
        criteria.setOffset(ServletRequestUtils.getIntParameter(request, "albumOffset", 0));
        SearchResult albums = searchService.search(criteria, SearchService.IndexType.ALBUM);
        for (MediaFile mediaFile : albums.getMediaFiles()) {
            AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
            builder.add("album", attributes, true);
        }

        criteria.setCount(ServletRequestUtils.getIntParameter(request, "songCount", 20));
        criteria.setOffset(ServletRequestUtils.getIntParameter(request, "songOffset", 0));
        SearchResult songs = searchService.search(criteria, SearchService.IndexType.SONG);
        for (MediaFile mediaFile : songs.getMediaFiles()) {
            AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
            builder.add("song", attributes, true);
        }

        builder.endAll();
        response.getWriter().print(builder);
    }
    
    public void search3(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        builder.add("searchResult3", false);

        String query = request.getParameter("query");
        SearchCriteria criteria = new SearchCriteria();
        criteria.setQuery(StringUtils.trimToEmpty(query));
        criteria.setCount(ServletRequestUtils.getIntParameter(request, "artistCount", 20));
        criteria.setOffset(ServletRequestUtils.getIntParameter(request, "artistOffset", 0));
        SearchResult searchResult = searchService.search(criteria, SearchService.IndexType.ARTIST_ID3);
        for (Artist artist : searchResult.getArtists()) {
            builder.add("artist", createAttributesForArtist(artist, username), true);
        }

        criteria.setCount(ServletRequestUtils.getIntParameter(request, "albumCount", 20));
        criteria.setOffset(ServletRequestUtils.getIntParameter(request, "albumOffset", 0));
        searchResult = searchService.search(criteria, SearchService.IndexType.ALBUM_ID3);
        for (Album album : searchResult.getAlbums()) {
            builder.add("album", createAttributesForAlbum(album, username), true);
        }

        criteria.setCount(ServletRequestUtils.getIntParameter(request, "songCount", 20));
        criteria.setOffset(ServletRequestUtils.getIntParameter(request, "songOffset", 0));
        searchResult = searchService.search(criteria, SearchService.IndexType.SONG);
        for (MediaFile song : searchResult.getMediaFiles()) {
            builder.add("song", createAttributesForMediaFile(player, song, username), true);
        }

        builder.endAll();
        response.getWriter().print(builder);
    }

    public void getPlaylists(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);

        builder.add("playlists", false);

        for (File playlist : playlistService.getSavedPlaylists()) {
            String id = StringUtil.utf8HexEncode(playlist.getName());
            String name = FilenameUtils.getBaseName(playlist.getName());
            builder.add("playlist", true, new Attribute("id", id), new Attribute("name", name));
        }
        builder.endAll();
        response.getWriter().print(builder);
    }

    public void getPlaylist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        XMLBuilder builder = createXMLBuilder(request, response, true);

        try {
            String id = StringUtil.utf8HexDecode(ServletRequestUtils.getRequiredStringParameter(request, "id"));
            File file = playlistService.getSavedPlaylist(id);
            if (file == null) {
                error(request, response, ErrorCode.NOT_FOUND, "Playlist not found: " + id);
                return;
            }
            Playlist playlist = new Playlist();
            playlistService.loadPlaylist(playlist, id);

            builder.add("playlist", false, new Attribute("id", StringUtil.utf8HexEncode(playlist.getName())),
                    new Attribute("name", FilenameUtils.getBaseName(playlist.getName())));
            List<MediaFile> result;
            synchronized (playlist) {
                result = playlist.getFiles();
            }
            for (MediaFile mediaFile : result) {
                AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
                builder.add("entry", attributes, true);
            }
            builder.endAll();
            response.getWriter().print(builder);
        } catch (ServletRequestBindingException x) {
            error(request, response, ErrorCode.MISSING_PARAMETER, getErrorMessage(x));
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
        }
    }

    public void jukeboxControl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request, true);

        User user = securityService.getCurrentUser(request);
        if (!user.isJukeboxRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to use jukebox.");
            return;
        }

        try {
            boolean returnPlaylist = false;
            String action = ServletRequestUtils.getRequiredStringParameter(request, "action");
            if ("start".equals(action)) {
                playlistControlService.doStart(request, response);
            } else if ("stop".equals(action)) {
                playlistControlService.doStop(request, response);
            } else if ("skip".equals(action)) {
                int index = ServletRequestUtils.getRequiredIntParameter(request, "index");
                int offset = ServletRequestUtils.getIntParameter(request, "offset", 0);
                playlistControlService.doSkip(request, response, index, offset);
            } else if ("add".equals(action)) {
                int[] ids = ServletRequestUtils.getIntParameters(request, "id");
                List<String> paths = new ArrayList<String>(ids.length);
                for (int id : ids) {
                    paths.add(mediaFileService.getMediaFile(id).getPath());
                }
                playlistControlService.doAdd(request, response, paths);
            } else if ("set".equals(action)) {
                int[] ids = ServletRequestUtils.getIntParameters(request, "id");
                List<String> paths = new ArrayList<String>(ids.length);
                for (int id : ids) {
                    paths.add(mediaFileService.getMediaFile(id).getPath());
                }
                playlistControlService.doSet(request, response, paths);
            } else if ("clear".equals(action)) {
                playlistControlService.doClear(request, response);
            } else if ("remove".equals(action)) {
                int index = ServletRequestUtils.getRequiredIntParameter(request, "index");
                playlistControlService.doRemove(request, response, index);
            } else if ("shuffle".equals(action)) {
                playlistControlService.doShuffle(request, response);
            } else if ("setGain".equals(action)) {
                float gain = ServletRequestUtils.getRequiredFloatParameter(request, "gain");
                jukeboxService.setGain(gain);
            } else if ("get".equals(action)) {
                returnPlaylist = true;
            } else if ("status".equals(action)) {
                // No action necessary.
            } else {
                throw new Exception("Unknown jukebox action: '" + action + "'.");
            }

            XMLBuilder builder = createXMLBuilder(request, response, true);

            Player player = playerService.getPlayer(request, response);
            String username = securityService.getCurrentUsername(request);
            Player jukeboxPlayer = jukeboxService.getPlayer();
            boolean controlsJukebox = jukeboxPlayer != null && jukeboxPlayer.getId().equals(player.getId());
            Playlist playlist = player.getPlaylist();

            List<Attribute> attrs = new ArrayList<Attribute>(Arrays.asList(
                    new Attribute("currentIndex", controlsJukebox && !playlist.isEmpty() ? playlist.getIndex() : -1),
                    new Attribute("playing", controlsJukebox && !playlist.isEmpty() && playlist.getStatus() == Playlist.Status.PLAYING),
                    new Attribute("gain", jukeboxService.getGain()),
                    new Attribute("position", controlsJukebox && !playlist.isEmpty() ? jukeboxService.getPosition() : 0)));

            if (returnPlaylist) {
                builder.add("jukeboxPlaylist", attrs, false);
                List<MediaFile> result;
                synchronized (playlist) {
                    result = playlist.getFiles();
                }
                for (MediaFile mediaFile : result) {
                    AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
                    builder.add("entry", attributes, true);
                }
            } else {
                builder.add("jukeboxStatus", attrs, false);
            }

            builder.endAll();
            response.getWriter().print(builder);

        } catch (ServletRequestBindingException x) {
            error(request, response, ErrorCode.MISSING_PARAMETER, getErrorMessage(x));
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
        }
    }

    public void createPlaylist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request, true);

        User user = securityService.getCurrentUser(request);
        if (!user.isPlaylistRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to create playlists.");
            return;
        }

        try {

            String playlistId = request.getParameter("playlistId");
            String name = request.getParameter("name");
            if (playlistId == null && name == null) {
                error(request, response, ErrorCode.MISSING_PARAMETER, "Playlist ID or name must be specified.");
                return;
            }

            Playlist playlist = new Playlist();
            playlist.setName(playlistId != null ? StringUtil.utf8HexDecode(playlistId) : name);

            int[] ids = ServletRequestUtils.getIntParameters(request, "songId");
            for (int id : ids) {
                playlist.addFiles(true, mediaFileService.getMediaFile(id));
            }
            playlistService.savePlaylist(playlist);

            XMLBuilder builder = createXMLBuilder(request, response, true);
            builder.endAll();
            response.getWriter().print(builder);

        } catch (ServletRequestBindingException x) {
            error(request, response, ErrorCode.MISSING_PARAMETER, getErrorMessage(x));
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
        }
    }

    public void deletePlaylist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request, true);

        User user = securityService.getCurrentUser(request);
        if (!user.isPlaylistRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to delete playlists.");
            return;
        }

        try {
            String id = StringUtil.utf8HexDecode(ServletRequestUtils.getRequiredStringParameter(request, "id"));
            playlistService.deletePlaylist(id);

            XMLBuilder builder = createXMLBuilder(request, response, true);
            builder.endAll();
            response.getWriter().print(builder);

        } catch (ServletRequestBindingException x) {
            error(request, response, ErrorCode.MISSING_PARAMETER, getErrorMessage(x));
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
        }
    }

    public void getAlbumList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        XMLBuilder builder = createXMLBuilder(request, response, true);
        builder.add("albumList", false);

        try {
            int size = ServletRequestUtils.getIntParameter(request, "size", 10);
            int offset = ServletRequestUtils.getIntParameter(request, "offset", 0);
            size = Math.max(0, Math.min(size, 500));
            String type = ServletRequestUtils.getRequiredStringParameter(request, "type");

            List<HomeController.Album> albums;
            if ("highest".equals(type)) {
                albums = homeController.getHighestRated(offset, size);
            } else if ("frequent".equals(type)) {
                albums = homeController.getMostFrequent(offset, size);
            } else if ("recent".equals(type)) {
                albums = homeController.getMostRecent(offset, size);
            } else if ("newest".equals(type)) {
                albums = homeController.getNewest(offset, size);
            } else if ("starred".equals(type)) {
                albums = homeController.getStarred(offset, size, username);
            } else if ("alphabetical".equals(type)) {
                albums = homeController.getAlphabetical(offset, size);
            } else if ("random".equals(type)) {
                albums = homeController.getRandom(size);
            } else {
                throw new Exception("Invalid list type: " + type);
            }

            for (HomeController.Album album : albums) {
                MediaFile mediaFile = mediaFileService.getMediaFile(album.getPath());
                AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
                builder.add("album", attributes, true);
            }
            builder.endAll();
            response.getWriter().print(builder);
        } catch (ServletRequestBindingException x) {
            error(request, response, ErrorCode.MISSING_PARAMETER, getErrorMessage(x));
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
        }
    }

    public void getAlbumList2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        XMLBuilder builder = createXMLBuilder(request, response, true);
        builder.add("albumList2", false);

        try {
            int size = ServletRequestUtils.getIntParameter(request, "size", 10);
            int offset = ServletRequestUtils.getIntParameter(request, "offset", 0);
            size = Math.max(0, Math.min(size, 500));
            String type = ServletRequestUtils.getRequiredStringParameter(request, "type");
            String username = securityService.getCurrentUsername(request);

            List<Album> albums;
            if ("frequent".equals(type)) {
                albums = albumDao.getMostFrequentlyPlayedAlbums(offset, size);
            } else if ("recent".equals(type)) {
                albums = albumDao.getMostRecentlyPlayedAlbums(offset, size);
            } else if ("newest".equals(type)) {
                albums = albumDao.getNewestAlbums(offset, size);
            } else if ("alphabetical".equals(type)) {
                albums = albumDao.getAlphabetialAlbums(offset, size);
            } else if ("starred".equals(type)) {
                albums = albumDao.getStarredAlbums(offset, size, securityService.getCurrentUser(request).getUsername());
            } else if ("random".equals(type)) {
                albums = searchService.getRandomAlbumsId3(size);
            } else {
                throw new Exception("Invalid list type: " + type);
            }
            for (Album album : albums) {
                builder.add("album", createAttributesForAlbum(album, username), true);
            }
            builder.endAll();
            response.getWriter().print(builder);
        } catch (ServletRequestBindingException x) {
            error(request, response, ErrorCode.MISSING_PARAMETER, getErrorMessage(x));
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
        }
    }

    public void getRandomSongs(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        XMLBuilder builder = createXMLBuilder(request, response, true);
        builder.add("randomSongs", false);

        try {
            int size = ServletRequestUtils.getIntParameter(request, "size", 10);
            size = Math.max(0, Math.min(size, 500));
            String genre = ServletRequestUtils.getStringParameter(request, "genre");
            Integer fromYear = ServletRequestUtils.getIntParameter(request, "fromYear");
            Integer toYear = ServletRequestUtils.getIntParameter(request, "toYear");
            Integer musicFolderId = ServletRequestUtils.getIntParameter(request, "musicFolderId");
            RandomSearchCriteria criteria = new RandomSearchCriteria(size, genre, fromYear, toYear, musicFolderId);

            for (MediaFile mediaFile : searchService.getRandomSongs(criteria)) {
                AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
                builder.add("song", attributes, true);
            }
            builder.endAll();
            response.getWriter().print(builder);
        } catch (ServletRequestBindingException x) {
            error(request, response, ErrorCode.MISSING_PARAMETER, getErrorMessage(x));
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
        }
    }
    
    public void getVideos(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        XMLBuilder builder = createXMLBuilder(request, response, true);
        builder.add("videos", false);
        try {
            int size = ServletRequestUtils.getIntParameter(request, "size", Integer.MAX_VALUE);
            int offset = ServletRequestUtils.getIntParameter(request, "offset", 0);

            for (MediaFile mediaFile : mediaFileDao.getVideos(size, offset)) {
                builder.add("video", createAttributesForMediaFile(player, mediaFile, username), true);
            }
            builder.endAll();
            response.getWriter().print(builder);
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
        }
    }

    public void getNowPlaying(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);
        builder.add("nowPlaying", false);

        for (TransferStatus status : statusService.getAllStreamStatuses()) {

            Player player = status.getPlayer();
            File file = status.getFile();
            if (player != null && player.getUsername() != null && file != null) {

                String username = player.getUsername();
                UserSettings userSettings = settingsService.getUserSettings(username);
                if (!userSettings.isNowPlayingAllowed()) {
                    continue;
                }

                MediaFile mediaFile = mediaFileService.getMediaFile(file);

                long minutesAgo = status.getMillisSinceLastUpdate() / 1000L / 60L;
                if (minutesAgo < 60) {
                    AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
                    attributes.add("username", username);
                    attributes.add("playerId", player.getId());
                    attributes.add("playerName", player.getName());
                    attributes.add("minutesAgo", minutesAgo);
                    builder.add("entry", attributes, true);
                }
            }
        }

        builder.endAll();
        response.getWriter().print(builder);
    }

    private AttributeSet createAttributesForMediaFile(Player player, MediaFile mediaFile, String username) {
        MediaFile parent = mediaFileService.getParentOf(mediaFile);
        AttributeSet attributes = new AttributeSet();
        attributes.add("id", mediaFile.getId());
        try {
            if (!mediaFileService.isRoot(parent)) {
                attributes.add("parent", parent.getId());
            }
        } catch (SecurityException x) {
            // Ignored.
        }
        attributes.add("title", mediaFile.getName());
        attributes.add("album", mediaFile.getAlbumName());
        attributes.add("artist", mediaFile.getArtist());
        attributes.add("isDir", mediaFile.isDirectory());
        attributes.add("coverArt", findCoverArt(mediaFile, parent));
        attributes.add("created", StringUtil.toISO8601(mediaFile.getCreated()));
        attributes.add("starred", StringUtil.toISO8601(mediaFileDao.getMediaFileStarredDate(mediaFile.getId(), username)));
        attributes.add("userRating", ratingService.getRatingForUser(username, mediaFile));
        attributes.add("averageRating", ratingService.getAverageRating(mediaFile));

        if (mediaFile.isFile()) {
            attributes.add("duration", mediaFile.getDurationSeconds());
            attributes.add("bitRate", mediaFile.getBitRate());
            attributes.add("track", mediaFile.getTrackNumber());
            attributes.add("discNumber", mediaFile.getDiscNumber());
            attributes.add("year", mediaFile.getYear());
            attributes.add("genre", mediaFile.getGenre());
            attributes.add("size", mediaFile.getFileSize());
            String suffix = mediaFile.getFormat();
            attributes.add("suffix", suffix);
            attributes.add("contentType", StringUtil.getMimeType(suffix));
            attributes.add("isVideo", mediaFile.isVideo());
            attributes.add("path", getRelativePath(mediaFile));

            if (mediaFile.getArtist() != null && mediaFile.getAlbumName() != null) {
                Album album = albumDao.getAlbum(mediaFile.getArtist(), mediaFile.getAlbumName());
                if (album != null) {
                    attributes.add("albumId", album.getId());
                }
            }
            if (mediaFile.getArtist() != null) {
                Artist artist = artistDao.getArtist(mediaFile.getArtist());
                if (artist != null) {
                    attributes.add("artistId", artist.getId());
                }
            }
            switch (mediaFile.getMediaType()) {
                case MUSIC:
                    attributes.add("type", "music");
                    break;
                case PODCAST:
                    attributes.add("type", "podcast");
                    break;
                case AUDIOBOOK:
                    attributes.add("type", "audiobook");
                    break;
                default:
                    break;
            }

            if (transcodingService.isTranscodingRequired(mediaFile, player)) {
                String transcodedSuffix = transcodingService.getSuffix(player, mediaFile, null);
                attributes.add("transcodedSuffix", transcodedSuffix);
                attributes.add("transcodedContentType", StringUtil.getMimeType(transcodedSuffix));
            }
        }
        return attributes;
    }

    private Integer findCoverArt(MediaFile mediaFile, MediaFile parent) {
        MediaFile dir = mediaFile.isDirectory() ? mediaFile : parent;
        if (dir != null && dir.getCoverArtPath() != null) {
            return dir.getId();
        }
        return null;
    }

    private String getRelativePath(MediaFile musicFile) {

        String filePath = musicFile.getPath();

        // Convert slashes.
        filePath = filePath.replace('\\', '/');

        String filePathLower = filePath.toLowerCase();

        List<MusicFolder> musicFolders = settingsService.getAllMusicFolders(false, true);
        for (MusicFolder musicFolder : musicFolders) {
            String folderPath = musicFolder.getPath().getPath();
            folderPath = folderPath.replace('\\', '/');
            String folderPathLower = folderPath.toLowerCase();

            if (filePathLower.startsWith(folderPathLower)) {
                String relativePath = filePath.substring(folderPath.length());
                return relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
            }
        }

        return null;
    }

    public ModelAndView download(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isDownloadRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to download files.");
            return null;
        }

        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        long lastModified = downloadController.getLastModified(request);

        if (ifModifiedSince != -1 && lastModified != -1 && lastModified <= ifModifiedSince) {
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return null;
        }

        if (lastModified != -1) {
            response.setDateHeader("Last-Modified", lastModified);
        }

        return downloadController.handleRequest(request, response);
    }

    public ModelAndView stream(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        User user = securityService.getCurrentUser(request);
        if (!user.isStreamRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to play files.");
            return null;
        }

        streamController.handleRequest(request, response);
        return null;
    }

    public void scrobble(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);

        Player player = playerService.getPlayer(request, response);

        if (!settingsService.getUserSettings(player.getUsername()).isLastFmEnabled()) {
            error(request, response, ErrorCode.GENERIC, "Scrobbling is not enabled for " + player.getUsername() + ".");
            return;
        }

        try {
            int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
            MediaFile file = mediaFileService.getMediaFile(id);
            if (file == null) {
                error(request, response, ErrorCode.NOT_FOUND, "File not found: " + id);
                return;
            }
            boolean submission = ServletRequestUtils.getBooleanParameter(request, "submission", true);
            audioScrobblerService.register(file, player.getUsername(), submission);
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
            return;
        }

        builder.endAll();
        response.getWriter().print(builder);
    }

    public void star(HttpServletRequest request, HttpServletResponse response) throws Exception {
        starOrUnstar(request, response, true);
    }

    public void unstar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        starOrUnstar(request, response, false);
    }

    private void starOrUnstar(HttpServletRequest request, HttpServletResponse response, boolean star) throws Exception {
        request = wrapRequest(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);

        try {
            String username = securityService.getCurrentUser(request).getUsername();
            for (int id : ServletRequestUtils.getIntParameters(request, "id")) {
                MediaFile mediaFile = mediaFileDao.getMediaFile(id);
                if (mediaFile == null) {
                    error(request, response, ErrorCode.NOT_FOUND, "Media file not found: " + id);
                    return;
                }
                if (star) {
                    mediaFileDao.starMediaFile(id, username);
                } else {
                    mediaFileDao.unstarMediaFile(id, username);
                }
            }
            for (int albumId : ServletRequestUtils.getIntParameters(request, "albumId")) {
                Album album = albumDao.getAlbum(albumId);
                if (album == null) {
                    error(request, response, ErrorCode.NOT_FOUND, "Album not found: " + albumId);
                    return;
                }
                if (star) {
                    albumDao.starAlbum(albumId, username);
                } else {
                    albumDao.unstarAlbum(albumId, username);
                }
            }
            for (int artistId : ServletRequestUtils.getIntParameters(request, "artistId")) {
                Artist artist = artistDao.getArtist(artistId);
                if (artist == null) {
                    error(request, response, ErrorCode.NOT_FOUND, "Artist not found: " + artistId);
                    return;
                }
                if (star) {
                    artistDao.starArtist(artistId, username);
                } else {
                    artistDao.unstarArtist(artistId, username);
                }
            }
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
            return;
        }

        builder.endAll();
        response.getWriter().print(builder);
    }

    public void getPodcasts(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        XMLBuilder builder = createXMLBuilder(request, response, true);
        builder.add("podcasts", false);

        for (PodcastChannel channel : podcastService.getAllChannels()) {
            AttributeSet channelAttrs = new AttributeSet();
            channelAttrs.add("id", channel.getId());
            channelAttrs.add("url", channel.getUrl());
            channelAttrs.add("status", channel.getStatus().toString().toLowerCase());
            channelAttrs.add("title", channel.getTitle());
            channelAttrs.add("description", channel.getDescription());
            channelAttrs.add("errorMessage", channel.getErrorMessage());
            builder.add("channel", channelAttrs, false);

            List<PodcastEpisode> episodes = podcastService.getEpisodes(channel.getId(), false);
            for (PodcastEpisode episode : episodes) {
                AttributeSet episodeAttrs = new AttributeSet();

                String path = episode.getPath();
                if (path != null) {
                    MediaFile mediaFile = mediaFileService.getMediaFile(path);
                    episodeAttrs.addAll(createAttributesForMediaFile(player, mediaFile, username));
                    episodeAttrs.add("streamId", mediaFile.getId());
                }

                episodeAttrs.add("id", episode.getId());  // Overwrites the previous "id" attribute.
                episodeAttrs.add("status", episode.getStatus().toString().toLowerCase());
                episodeAttrs.add("title", episode.getTitle());
                episodeAttrs.add("description", episode.getDescription());
                episodeAttrs.add("publishDate", episode.getPublishDate());

                builder.add("episode", episodeAttrs, true);
            }

            builder.end(); // <channel>
        }
        builder.endAll();
        response.getWriter().print(builder);
    }

    public void getShares(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        User user = securityService.getCurrentUser(request);
        XMLBuilder builder = createXMLBuilder(request, response, true);

        builder.add("shares", false);
        for (Share share : shareService.getSharesForUser(user)) {
            builder.add("share", createAttributesForShare(share), false);

            for (MediaFile mediaFile : shareService.getSharedFiles(share.getId())) {
                AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
                builder.add("entry", attributes, true);
            }

            builder.end();
        }
        builder.endAll();
        response.getWriter().print(builder);
    }

    public void createShare(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        Player player = playerService.getPlayer(request, response);
        String username = securityService.getCurrentUsername(request);

        User user = securityService.getCurrentUser(request);
        if (!user.isShareRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, user.getUsername() + " is not authorized to share media.");
            return;
        }

        if (!settingsService.isUrlRedirectionEnabled()) {
            error(request, response, ErrorCode.GENERIC, "Sharing is only supported for *.subsonic.org domain names.");
            return;
        }

        XMLBuilder builder = createXMLBuilder(request, response, true);

        try {

            List<MediaFile> files = new ArrayList<MediaFile>();
            for (int id : ServletRequestUtils.getRequiredIntParameters(request, "id")) {
                files.add(mediaFileService.getMediaFile(id));
            }

            // TODO: Update api.jsp

            Share share = shareService.createShare(request, files);
            share.setDescription(request.getParameter("description"));
            long expires = ServletRequestUtils.getLongParameter(request, "expires", 0L);
            if (expires != 0) {
                share.setExpires(new Date(expires));
            }
            shareService.updateShare(share);

            builder.add("shares", false);
            builder.add("share", createAttributesForShare(share), false);

            for (MediaFile mediaFile : shareService.getSharedFiles(share.getId())) {
                AttributeSet attributes = createAttributesForMediaFile(player, mediaFile, username);
                builder.add("entry", attributes, true);
            }

            builder.endAll();
            response.getWriter().print(builder);

        } catch (ServletRequestBindingException x) {
            error(request, response, ErrorCode.MISSING_PARAMETER, getErrorMessage(x));
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
        }
    }

    public void deleteShare(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            request = wrapRequest(request);
            User user = securityService.getCurrentUser(request);
            int id = ServletRequestUtils.getRequiredIntParameter(request, "id");

            Share share = shareService.getShareById(id);
            if (share == null) {
                error(request, response, ErrorCode.NOT_FOUND, "Shared media not found.");
                return;
            }
            if (!user.isAdminRole() && !share.getUsername().equals(user.getUsername())) {
                error(request, response, ErrorCode.NOT_AUTHORIZED, "Not authorized to delete shared media.");
                return;
            }

            shareService.deleteShare(id);
            XMLBuilder builder = createXMLBuilder(request, response, true).endAll();
            response.getWriter().print(builder);

        } catch (ServletRequestBindingException x) {
            error(request, response, ErrorCode.MISSING_PARAMETER, getErrorMessage(x));
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
        }
    }

    public void updateShare(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            request = wrapRequest(request);
            User user = securityService.getCurrentUser(request);
            int id = ServletRequestUtils.getRequiredIntParameter(request, "id");

            Share share = shareService.getShareById(id);
            if (share == null) {
                error(request, response, ErrorCode.NOT_FOUND, "Shared media not found.");
                return;
            }
            if (!user.isAdminRole() && !share.getUsername().equals(user.getUsername())) {
                error(request, response, ErrorCode.NOT_AUTHORIZED, "Not authorized to modify shared media.");
                return;
            }

            share.setDescription(request.getParameter("description"));
            String expiresString = request.getParameter("expires");
            if (expiresString != null) {
                long expires = Long.parseLong(expiresString);
                share.setExpires(expires == 0L ? null : new Date(expires));
            }
            shareService.updateShare(share);
            XMLBuilder builder = createXMLBuilder(request, response, true).endAll();
            response.getWriter().print(builder);

        } catch (ServletRequestBindingException x) {
            error(request, response, ErrorCode.MISSING_PARAMETER, getErrorMessage(x));
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
        }
    }

    private List<Attribute> createAttributesForShare(Share share) {
        List<Attribute> attributes = new ArrayList<Attribute>();

        attributes.add(new Attribute("id", share.getId()));
        attributes.add(new Attribute("url", shareService.getShareUrl(share)));
        attributes.add(new Attribute("username", share.getUsername()));
        attributes.add(new Attribute("created", StringUtil.toISO8601(share.getCreated())));
        attributes.add(new Attribute("visitCount", share.getVisitCount()));
        attributes.add(new Attribute("description", share.getDescription()));
        attributes.add(new Attribute("expires", StringUtil.toISO8601(share.getExpires())));
        attributes.add(new Attribute("lastVisited", StringUtil.toISO8601(share.getLastVisited())));

        return attributes;
    }

    public ModelAndView videoPlayer(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        Map<String, Object> map = new HashMap<String, Object>();
        int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
        MediaFile file = mediaFileService.getMediaFile(id);

        int timeOffset = ServletRequestUtils.getIntParameter(request, "timeOffset", 0);
        timeOffset = Math.max(0, timeOffset);
        Integer duration = file.getDurationSeconds();
        if (duration != null) {
            map.put("skipOffsets", VideoPlayerController.createSkipOffsets(duration));
            timeOffset = Math.min(duration, timeOffset);
            duration -= timeOffset;
        }

        map.put("id", request.getParameter("id"));
        map.put("u", request.getParameter("u"));
        map.put("p", request.getParameter("p"));
        map.put("c", request.getParameter("c"));
        map.put("v", request.getParameter("v"));
        map.put("video", file);
        map.put("maxBitRate", ServletRequestUtils.getIntParameter(request, "maxBitRate", VideoPlayerController.DEFAULT_BIT_RATE));
        map.put("duration", duration);
        map.put("timeOffset", timeOffset);
        map.put("bitRates", VideoPlayerController.BIT_RATES);
        map.put("autoplay", ServletRequestUtils.getBooleanParameter(request, "autoplay", true));

        ModelAndView result = new ModelAndView("rest/videoPlayer");
        result.addObject("model", map);
        return result;
    }

    public ModelAndView getCoverArt(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        return coverArtController.handleRequest(request, response);
    }

    public ModelAndView getAvatar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        return avatarController.handleRequest(request, response);
    }

    public void changePassword(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);
        try {

            String username = ServletRequestUtils.getRequiredStringParameter(request, "username");
            String password = decrypt(ServletRequestUtils.getRequiredStringParameter(request, "password"));

            User authUser = securityService.getCurrentUser(request);
            if (!authUser.isAdminRole() && !username.equals(authUser.getUsername())) {
                error(request, response, ErrorCode.NOT_AUTHORIZED, authUser.getUsername() + " is not authorized to change password for " + username);
                return;
            }

            User user = securityService.getUserByName(username);
            user.setPassword(password);
            securityService.updateUser(user);

            XMLBuilder builder = createXMLBuilder(request, response, true).endAll();
            response.getWriter().print(builder);
        } catch (ServletRequestBindingException x) {
            error(request, response, ErrorCode.MISSING_PARAMETER, getErrorMessage(x));
        } catch (Exception x) {
            LOG.warn("Error in REST API.", x);
            error(request, response, ErrorCode.GENERIC, getErrorMessage(x));
        }
    }

    public void getUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request = wrapRequest(request);

        String username;
        try {
            username = ServletRequestUtils.getRequiredStringParameter(request, "username");
        } catch (ServletRequestBindingException x) {
            error(request, response, ErrorCode.MISSING_PARAMETER, getErrorMessage(x));
            return;
        }

        User currentUser = securityService.getCurrentUser(request);
        if (!username.equals(currentUser.getUsername()) && !currentUser.isAdminRole()) {
            error(request, response, ErrorCode.NOT_AUTHORIZED, currentUser.getUsername() + " is not authorized to get details for other users.");
            return;
        }

        User requestedUser = securityService.getUserByName(username);
        if (requestedUser == null) {
            error(request,

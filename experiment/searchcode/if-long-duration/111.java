package jp.co.kayo.android.localplayer.util.bean;
/***
 * Copyright (c) 2010-2012 yokmama. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */
import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaData implements Parcelable, Serializable {
    private static final long serialVersionUID = 8531245739641223373L;
    public static final int NOTPLAYED = 0;
    public static final int PLAYED = 1;
    public static final int PLAYING = 2;

    public long id = -1;
    public long mediaId = -1;
    public int state = NOTPLAYED;
    public String data = null;
    private long duration = 0;
    private String title = null;
    private String album = null;
    private String artist = null;
    
    public MediaData(long mediaid, String data) {
        this.mediaId = mediaid;
        this.data = data;
    }
    
    public MediaData(long id, long mediaid, int state, String data) {
        this.id = id;
        this.mediaId = mediaid;
        this.data = data;
        this.state = state;
    }

    public MediaData(long id, long mediaid, int state, long duration, String data) {
        this.id = id;
        this.mediaId = mediaid;
        this.duration = duration;
        this.data = data;
        this.state = state;
    }

    public MediaData(long id, long mediaid, int state, long duration, String title, String album,
            String artist, String data) {
        this.id = id;
        this.mediaId = mediaid;
        this.duration = duration;
        this.data = data;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.state = state;
    }
    
    public MediaData(Parcel per) {
        id = per.readLong();
        mediaId = per.readLong();
        title = per.readString();
        album = per.readString();
        artist = per.readString();
        duration = per.readLong();
        state = per.readInt();
        data = per.readString();
    }
    
    public static final Parcelable.Creator<MediaData> CREATOR = new Parcelable.Creator<MediaData>() {
        public MediaData createFromParcel(Parcel in) {
            return new MediaData(in);
        }

        public MediaData[] newArray(int size) {
            return new MediaData[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(mediaId);
        dest.writeString(title);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeLong(duration);
        dest.writeInt(state);
        dest.writeString(data);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
    
    
}


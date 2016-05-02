// Copyright 2011 NPR
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.npr.android.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.npr.android.news.Playable;
import org.npr.android.util.PlaylistProvider.Items;
import org.npr.api.Story;

public class PlaylistRepository {
  private final Context applicationContext;
  private final ContentResolver contentResolver;
  private static final String LOG_TAG = PlaylistProvider.class.getName();
  public static final String PLAYLIST_CHANGED =
      "org.npr.android.news.PLAYLIST_CHANGE";
  public static final String PLAYLIST_CHANGE = "PLAYLIST_CHANGE";
  public static final String PLAYLIST_ITEM_ADDED = "PLAYLIST_ITEM_ADDED";
  public static final String PLAYLIST_ITEM_REMOVED = "PLAYLIST_ITEM_REMOVED";
  public static final String PLAYLIST_CLEAR = "PLAYLIST_CLEAR";

  public PlaylistRepository(Context applicationContext,
                            ContentResolver contentResolver) {
    this.applicationContext = applicationContext;
    this.contentResolver = contentResolver;
  }

  public long add(Story story) {
    ContentValues values = new ContentValues();
    values.put(Items.NAME, story.getTitle());
    values.put(Items.URL, story.getPlayableUrl());
    values.put(Items.IS_READ, false);
    values.put(Items.STORY_ID, story.getId());
    Story.Audio audio = story.getPlayable();
    if (audio != null) {
      values.put(Items.DURATION, audio.getDuration());
    }
    Log.d(LOG_TAG, "Adding playlist item to db");
    Uri uri = contentResolver.insert(PlaylistProvider.CONTENT_URI, values);

    Intent playlistChanged = new Intent(PLAYLIST_CHANGED);
    playlistChanged.putExtra(PLAYLIST_CHANGE, PLAYLIST_ITEM_ADDED);
    applicationContext.sendBroadcast(playlistChanged);

    return ContentUris.parseId(uri);
  }

  public long insert(Story story, int position) {

    Cursor c = contentResolver.query(PlaylistProvider.CONTENT_URI,
        null, null, null, PlaylistProvider.Items.PLAY_ORDER + " ASC");
    if (c.moveToFirst()) {
      do {
        int order = c.getInt(c.getColumnIndex(PlaylistProvider.Items.PLAY_ORDER));
        if (order >= position) {
          long id = (long) c.getInt(c.getColumnIndex(PlaylistProvider
              .Items._ID));
          updateItemOrder(id, order + 1);
        }
      } while (c.moveToNext());
    }
    c.close();

    ContentValues values = new ContentValues();
    values.put(Items.NAME, story.getTitle());
    values.put(Items.URL, story.getPlayableUrl());
    values.put(Items.IS_READ, false);
    values.put(Items.STORY_ID, story.getId());
    values.put(Items.PLAY_ORDER, position);
    Story.Audio audio = story.getPlayable();
    if (audio != null) {
      values.put(Items.DURATION, audio.getDuration());
    }
    Log.d(LOG_TAG, "Adding playlist item to db");
    Uri uri = contentResolver.insert(PlaylistProvider.CONTENT_URI, values);

    Intent playlistChanged = new Intent(PLAYLIST_CHANGED);
    playlistChanged.putExtra(PLAYLIST_CHANGE, PLAYLIST_ITEM_ADDED);
    applicationContext.sendBroadcast(playlistChanged);

    return ContentUris.parseId(uri);
  }

  public void markAsRead(long id) {
    Uri update = ContentUris.withAppendedId(PlaylistProvider.CONTENT_URI, id);
    ContentValues values = new ContentValues();
    values.put(Items.IS_READ, true);
    contentResolver.update(update, values, null, null);
  }

  private PlaylistEntry retrievePlaylistItem(String selection,
                                             String[] selectionArgs, String sort) {
    Cursor c = contentResolver.query(PlaylistProvider.CONTENT_URI,
        null, selection, selectionArgs, sort);

    String title, url, storyID;
    long id;
    int order;
    if (c.moveToFirst()) {
      id = c.getInt(c.getColumnIndex(PlaylistProvider.Items._ID));
      title = c.getString(c.getColumnIndex(PlaylistProvider.Items.NAME));
      url = c.getString(c.getColumnIndex(PlaylistProvider.Items.URL));
      order = c.getInt(c.getColumnIndex(PlaylistProvider.Items.PLAY_ORDER));
      storyID = c.getString(c.getColumnIndex(PlaylistProvider.Items.STORY_ID));
      c.close();
      return new PlaylistEntry(id, url, title, false, order, storyID);
    }
    c.close();
    return null;
  }

  public int getItemCount() {
    Cursor c = contentResolver.query(PlaylistProvider.CONTENT_URI, null,
        null, null, null);
    int count = c.getCount();
    c.close();
    return count;
  }

  public int getReadCount() {
    String selection = PlaylistProvider.Items.IS_READ + " = ?";
    String[] selectionArgs = new String[1];
    selectionArgs[0] = "1";
    Cursor c = contentResolver.query(PlaylistProvider.CONTENT_URI, null,
        selection, selectionArgs, null);
    int count = c.getCount();
    c.close();
    return count;
  }

  public PlaylistEntry getPlaylistItemFromId(long id) {
    return getPlaylistItemFromId(Long.toString(id));
  }

  public PlaylistEntry getPlaylistItemFromId(String id) {
    if (id == null || id.equals("-1")) {
      return null;
    }
    String selection = PlaylistProvider.Items._ID + " = ?";
    String[] selectionArgs = new String[1];
    selectionArgs[0] = id;
    return retrievePlaylistItem(selection, selectionArgs, null);
  }

  public PlaylistEntry getPlaylistItemFromStoryId(String storyId) {
    String selection = PlaylistProvider.Items.STORY_ID + " = ?";
    String[] selectionArgs = new String[1];
    selectionArgs[0] = storyId;
    return retrievePlaylistItem(selection, selectionArgs, null);
  }

  public Playable getFirstUnreadEntry() {
    String selection = PlaylistProvider.Items.IS_READ + " = ?";
    String[] selectionArgs = new String[1];
    selectionArgs[0] = "0";
    String sort = PlaylistProvider.Items.PLAY_ORDER + " asc";
    PlaylistEntry playlistEntry = retrievePlaylistItem(selection,
        selectionArgs, sort);
    if (playlistEntry == null) {
      return null;
    } else {
      return Playable.PlayableFactory.fromPlaylistEntry(playlistEntry);
    }
  }

  public Playable getPreviousEntry(long id) {
    PlaylistEntry entry = getPlaylistItemFromId(id);
    if (entry == null || entry.playOrder == 0) {
      return null;
    }

    String selection = PlaylistProvider.Items.PLAY_ORDER + " = ?";
    String[] selectionArgs = new String[1];
    selectionArgs[0] = Integer.toString(entry.playOrder - 1);
    PlaylistEntry playlistEntry = retrievePlaylistItem(selection,
        selectionArgs, null);
    if (playlistEntry == null) {
      return null;
    } else {
      return Playable.PlayableFactory.fromPlaylistEntry(playlistEntry);
    }
  }

  public Playable getNextEntry(long id) {
    PlaylistEntry entry = getPlaylistItemFromId(id);
    if (entry == null) {
      return null;
    }

    String selection = PlaylistProvider.Items.PLAY_ORDER + " = ?";
    String[] selectionArgs = new String[1];
    selectionArgs[0] = Integer.toString(entry.playOrder + 1);
    PlaylistEntry playlistEntry = retrievePlaylistItem(selection,
        selectionArgs, null);
    if (playlistEntry == null) {
      return null;
    } else {
      return Playable.PlayableFactory.fromPlaylistEntry(playlistEntry);
    }
  }

  private void updateItemOrder(long id, int newOrder) {
    Uri update = ContentUris.withAppendedId(PlaylistProvider.CONTENT_URI, id);
    ContentValues values = new ContentValues();
    values.put(Items.PLAY_ORDER, newOrder);
    contentResolver.update(update, values, null, null);
    applicationContext.sendBroadcast(new Intent(PLAYLIST_CHANGED));
  }

  synchronized public void move(int from, int to) {
    Cursor c = contentResolver.query(PlaylistProvider.CONTENT_URI,
        null, null, null, PlaylistProvider.Items.PLAY_ORDER + " ASC");
    int itemCt = c.getCount();
    if (from < 0 || from >= itemCt || from == to) {
      return;
    }

    if (to < 0) {
      to = 0;
    } else if (to >= itemCt) {
      to = itemCt - 1;
    }

    if (c.moveToFirst()) {
      do {
        long id = (long) c.getInt(c.getColumnIndex(PlaylistProvider
            .Items._ID));
        int order = c.getInt(c.getColumnIndex(PlaylistProvider.Items.PLAY_ORDER));
        if (order == from) {
          updateItemOrder(id, to);
        } else {
          if ((order >= from && order <= to) ||
              (order >= to && order <= from)) {
            updateItemOrder(id, from > to ? order + 1 : order - 1);
          }
        }
      } while (c.moveToNext());
    }
    c.close();
  }

  public void clearAll() {
    contentResolver.delete(PlaylistProvider.CONTENT_URI, null, null);
    Intent playlistChanged = new Intent(PLAYLIST_CHANGED);
    playlistChanged.putExtra(PLAYLIST_CHANGE, PLAYLIST_CLEAR);
    applicationContext.sendBroadcast(playlistChanged);
  }

  public void clearPlayed() {
    contentResolver.delete(PlaylistProvider.CONTENT_URI,
        Items.IS_READ + "=1", null);
    Intent playlistChanged = new Intent(PLAYLIST_CHANGED);
    playlistChanged.putExtra(PLAYLIST_CHANGE, PLAYLIST_CLEAR);
    applicationContext.sendBroadcast(playlistChanged);
  }

  public void delete(PlaylistEntry entry) {
    contentResolver.delete(PlaylistProvider.CONTENT_URI,
        Items._ID + "=?", new String[]{String.valueOf(entry.id)});
    Intent playlistChanged = new Intent(PLAYLIST_CHANGED);
    playlistChanged.putExtra(PLAYLIST_CHANGE, PLAYLIST_ITEM_REMOVED);
    applicationContext.sendBroadcast(playlistChanged);
  }

  public boolean isFirstEntry(String id) {
    if (id == null) {
      return false;
    }
    PlaylistEntry entry = getPlaylistItemFromId(id);
    if (entry == null) {
      return false;
    }

    String selection = PlaylistProvider.Items.PLAY_ORDER + " < ?";
    String[] selectionArgs = new String[1];
    selectionArgs[0] = Integer.toString(entry.playOrder);
    Cursor c = contentResolver.query(PlaylistProvider.CONTENT_URI, null,
        selection, selectionArgs, null);
    if (c.getCount() > 0) {
      c.close();
      return false;
    } else {
      c.close();
      return true;
    }
  }

  public boolean isLastEntry(String id) {
    if (id == null) {
      return false;
    }
    PlaylistEntry entry = getPlaylistItemFromId(id);
    if (entry == null) {
      return false;
    }

    String selection = PlaylistProvider.Items.PLAY_ORDER + " > ?";
    String[] selectionArgs = new String[1];
    selectionArgs[0] = Integer.toString(entry.playOrder);
    Cursor c = contentResolver.query(PlaylistProvider.CONTENT_URI, null,
        selection, selectionArgs, null);
    if (c.getCount() > 0) {
      c.close();
      return false;
    } else {
      c.close();
      return true;
    }
  }
}


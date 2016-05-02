/*
<<<<<<< HEAD
 * Copyright (C) 2014 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.profiles.actions.item;

import android.app.StreamSettings;
import android.content.Context;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.profiles.actions.ItemListAdapter;

public class VolumeStreamItem implements Item {
    private int mStreamId;
    private StreamSettings mStreamSettings;

    public VolumeStreamItem(int streamId, StreamSettings streamSettings) {
        mStreamId = streamId;
        mStreamSettings = streamSettings;
    }

    @Override
    public ItemListAdapter.RowType getRowType() {
        return ItemListAdapter.RowType.VOLUME_STREAM_ITEM;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = inflater.inflate(R.layout.list_two_line_item, parent, false);
            // Do some initialization
        } else {
            view = convertView;
        }

        Context context = inflater.getContext();
        final AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        TextView text = (TextView) view.findViewById(R.id.title);
        text.setText(getNameForStream(mStreamId));

        TextView desc = (TextView) view.findViewById(R.id.summary);
        int denominator = mStreamSettings.getValue();
        int numerator = am.getStreamMaxVolume(mStreamId);
        if (mStreamSettings.isOverride()) {
            desc.setText(context.getResources().getString(R.string.volume_override_summary,
                    denominator, numerator));
        } else {
            desc.setText(context.getString(R.string.volume_override_summary_no_override));
        }

        return view;
    }

    public static int getNameForStream(int stream) {
        switch (stream) {
            case AudioManager.STREAM_ALARM:
                return R.string.alarm_volume_title;
            case AudioManager.STREAM_MUSIC:
                return R.string.media_volume_title;
            case AudioManager.STREAM_RING:
                return R.string.incoming_call_volume_title;
            case AudioManager.STREAM_NOTIFICATION:
                return R.string.notification_volume_title;
            default: return 0;
        }
    }

    public int getStreamType() {
        return mStreamId;
    }

    public StreamSettings getSettings() {
        return mStreamSettings;
=======
 * Copyright (C) 2009 by Eric Lambert <Eric.Lambert@sun.com>
 * Use and distribution licensed under the BSD license.  See
 * the COPYING file in the parent directory for full text.
 */
package org.gearman.worker;

import java.util.HashSet;
import java.util.Set;

import org.gearman.client.GearmanIOEventListener;
import org.gearman.client.GearmanJobResult;
import org.gearman.client.GearmanJobResultImpl;
import org.gearman.common.GearmanPacket;
import org.gearman.common.GearmanPacketImpl;
import org.gearman.common.GearmanPacketMagic;
import org.gearman.common.GearmanPacketType;
import org.gearman.util.ByteUtils;

public abstract class AbstractGearmanFunction implements GearmanFunction {

    protected final String name;
    protected Object data;
    protected byte[] jobHandle;
    protected Set<GearmanIOEventListener> listeners;

    public AbstractGearmanFunction() {
        this(null);
    }

    public AbstractGearmanFunction(String name) {
        listeners = new HashSet<GearmanIOEventListener>();
        jobHandle = new byte[0];
        if (name == null) {
            this.name = this.getClass().getCanonicalName();
        } else {
            this.name = name;
        }
    }

    public String getName() {                                                   
        return name;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setJobHandle(byte[] handle) throws IllegalArgumentException {
        if (handle == null) {
            throw new IllegalArgumentException("handle can not be null");
        }
        if (handle.length == 0) {
            throw new IllegalArgumentException("handle can not be empty");
        }
        jobHandle = new byte[handle.length];
        System.arraycopy(handle, 0, jobHandle, 0, handle.length);
    }

    public byte[] getJobHandle() {                                              
        byte[] rt = new byte[jobHandle.length];
        System.arraycopy(jobHandle, 0, rt, 0, jobHandle.length);
        return rt;
    }

    public void registerEventListener(GearmanIOEventListener listener)
            throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("listener can not be null");
        }
        listeners.add(listener);
    }

    public void fireEvent(GearmanPacket event)
            throws IllegalArgumentException {
        if (event == null) {
            throw new IllegalArgumentException("event can not be null");
        }
        for (GearmanIOEventListener listener : listeners) {
            listener.handleGearmanIOEvent(event);
        }
    }

    public void sendData(byte[] data) {
        fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                GearmanPacketType.WORK_DATA,
                GearmanPacketImpl.generatePacketData(jobHandle, data)));

    }

    public void sendWarning(byte[] warning) {
        fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                GearmanPacketType.WORK_WARNING,
                GearmanPacketImpl.generatePacketData(jobHandle, warning)));
    }

    public void sendException(byte[] exception) {
        fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                GearmanPacketType.WORK_EXCEPTION,
                GearmanPacketImpl.generatePacketData(jobHandle, exception)));
    }

    public void sendStatus(int denominator, int numerator) {
        fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                GearmanPacketType.WORK_STATUS,
                GearmanPacketImpl.generatePacketData(jobHandle,
                ByteUtils.toUTF8Bytes(String.valueOf(numerator)),
                ByteUtils.toUTF8Bytes(String.valueOf(denominator)))));
    }

    public abstract GearmanJobResult executeFunction();

    public GearmanJobResult call() {
        GearmanPacket event = null;
        GearmanJobResult result = null;
        Exception thrown = null;
        try {
            result = executeFunction();
        } catch (Exception e) {
            thrown = e;
        }
        if (result == null) {
            String message = thrown == null ? "function returned null result" :
                thrown.getMessage();
            fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                    GearmanPacketType.WORK_EXCEPTION,
                    GearmanPacketImpl.generatePacketData(jobHandle,
                    message.getBytes())));
            result = new GearmanJobResultImpl(jobHandle, false, new byte[0],
                    new byte[0], new byte[0], -1, -1);
        }

        if (result.jobSucceeded()) {
            event = new GearmanPacketImpl(GearmanPacketMagic.REQ,
                    GearmanPacketType.WORK_COMPLETE,
                    GearmanPacketImpl.generatePacketData(jobHandle,
                    result.getResults()));
        } else {
            event = new GearmanPacketImpl(GearmanPacketMagic.REQ,
                    GearmanPacketType.WORK_FAIL, jobHandle);

        }
        fireEvent(event);
        return result;
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}


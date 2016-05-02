/*
 * Copyright (C) 2012-2013. Aktive Cortex
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aktivecortex.core.serializer.schema.jodatime;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.base.BaseDateTime;
import org.joda.time.chrono.GregorianChronology;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;
import java.lang.reflect.Field;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

public final class DateTimeSchema implements Schema<DateTime> {

    public static final int SCALE_DAYS = 0;

    public static final int SCALE_HOURS = 1;

    public static final int SCALE_MINUTES = 2;

    public static final int SCALE_SECONDS = 3;

    public static final int SCALE_MILLISECONDS = 4;

    public static final long TICKS_PER_MILLISECONDS = 10000;


    // http://msdn.microsoft.com/en-us/library/system.timespan.ticks.aspx
    public static final int SCALE_TICKS = 5;

    private static final int SS_2_MILLIS = 1000;

    private static final int MI_2_MILLIS = SS_2_MILLIS * 60;

    private static final int HH_2_MILLIS = MI_2_MILLIS * 60;

    private static final int DD_2_MILLIS = HH_2_MILLIS * 24;

    private static final DateTimeSchema DATETIME_SCHEMA = new DateTimeSchema();

	static {
		RuntimeSchema.register(DateTime.class, DATETIME_SCHEMA);
	}


    public String getFieldName(int number) {
		return null;
	}

	public int getFieldNumber(String name) {
		return 0;
	}

	public boolean isInitialized(DateTime message) {
		return true;
	}

	public DateTime newMessage() {
		return new DateTime(0);
	}

	public String messageName() {
		return null;
	}

	public String messageFullName() {
		return null;
	}

	public Class<? super DateTime> typeClass() {
		return DateTime.class;
	}

	public void mergeFrom(Input input, DateTime message) throws IOException {
		int number = input.readFieldNumber(this);
		long value = 0;
		int scale = 0;
		while (number > 0) {
			if (number == 1) {
				value = input.readSInt64();
			} else if (number == 2) {
				scale = input.readInt32();
			}
			number = input.readFieldNumber(this);
		}
		long millis = 0;
		if (scale == DateTimeSchema.SCALE_TICKS) {
			millis = value / DateTimeSchema.TICKS_PER_MILLISECONDS;
		} else if (scale == DateTimeSchema.SCALE_MILLISECONDS) {
			millis = value;
		} else if (scale == DateTimeSchema.SCALE_SECONDS) {
			millis = value * SS_2_MILLIS;
		} else if (scale == DateTimeSchema.SCALE_MINUTES) {
			millis = value * MI_2_MILLIS;
		} else if (scale == DateTimeSchema.SCALE_HOURS) {
			millis = value * HH_2_MILLIS;
		} else if (scale == DateTimeSchema.SCALE_DAYS) {
			millis = value * DD_2_MILLIS;
		}
		try {
			Field field = BaseDateTime.class.getDeclaredField("iMillis");
			field.setAccessible(true);
			field.set(message, GregorianChronology.getInstance().getZone().convertUTCToLocal(millis));
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	public void writeTo(Output output, DateTime message) throws IOException {
		output.writeSInt64(1,
				message.getZone().convertLocalToUTC(message.getMillis(), true)
						* DateTimeSchema.TICKS_PER_MILLISECONDS, false);
		output.writeInt32(2, DateTimeSchema.SCALE_TICKS, false);
	}

}


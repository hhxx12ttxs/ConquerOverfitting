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
import java.lang.reflect.Field;

import org.joda.time.DateMidnight;
import org.joda.time.base.BaseDateTime;
import org.joda.time.chrono.GregorianChronology;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;

public class DateMidnightSchema implements Schema<DateMidnight> {

	@Override
	public String getFieldName(int i) {
		return null;
	}

	@Override
	public int getFieldNumber(String s) {
		return 0;
	}

	@Override
	public boolean isInitialized(DateMidnight arg0) {
		return true;
	}

	@Override
	public String messageFullName() {
		return null;
	}

	@Override
	public String messageName() {
		return null;
	}

	@Override
	public DateMidnight newMessage() {
		return new DateMidnight(0);
	}

	@Override
	public Class<? super DateMidnight> typeClass() {
		return DateMidnight.class;
	}

	@Override
	public void mergeFrom(Input input, DateMidnight message) throws IOException {
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
			millis = value * 1000;
		} else if (scale == DateTimeSchema.SCALE_MINUTES) {
			millis = value * 1000 * 60;
		} else if (scale == DateTimeSchema.SCALE_HOURS) {
			millis = value * 1000 * 60 * 60;
		} else if (scale == DateTimeSchema.SCALE_DAYS) {
			millis = value * 1000 * 60 * 60 * 24;
		}
		try {
			Field field = BaseDateTime.class.getDeclaredField("iMillis");
			field.setAccessible(true);
			field.set(message, GregorianChronology.getInstance().getZone().convertUTCToLocal(millis));
		} catch (Exception ex) {
			throw new IOException(ex);
		}

	}

	@Override
	public void writeTo(Output output, DateMidnight message) throws IOException {
		output.writeSInt64(1,
				message.getZone().convertLocalToUTC(message.getMillis(), true)
						* DateTimeSchema.TICKS_PER_MILLISECONDS, false);
		output.writeInt32(2, DateTimeSchema.SCALE_TICKS, false);
	}



	/*@Override
	public void mergeFrom(Input input, DateMidnight message) throws IOException {
		String date = "";
		int number = input.readFieldNumber(this);

		while (number > 0) {
			if (number == 1) {
				date = input.readString();
			}
			number = input.readFieldNumber(this);
		}
		try {
			Field field = BaseDateTime.class.getDeclaredField("iMillis");
			field.setAccessible(true);
			field.set(message, JodaTimeConverter.parseDateTime(date).getMillis());
		} catch (Exception ex) {
			throw new IOException(ex);
		}


	}

	@Override
	public void writeTo(Output output, DateMidnight message) throws IOException {
		if (message != null)
			output.writeString(1, JodaTimeConverter.printDateTime(message), false);
	} */



}


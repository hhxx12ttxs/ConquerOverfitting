/*
 * Created by Daniel Marell 14-11-28 07:23
 */
package se.marell.dvesta.ioscan.resources;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import se.marell.dvesta.ioscan.FloatInput;

import java.io.IOException;

public class FloatInputSerializer extends JsonSerializer<FloatInput> {

    @Override
    public void serialize(FloatInput value, JsonGenerator jgen,
                          SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        IoDeviceSerializer.serializeFields(value, jgen);
        if (!Float.isNaN(value.getValue())) {
            jgen.writeNumberField("value", value.getValue());
        }
        jgen.writeNumberField("numDecimals", value.getNumDecimals());
        jgen.writeNumberField("overrideValue", value.getOverrideValue());
        if (!Float.isInfinite(value.getMin())) {
            jgen.writeNumberField("min", value.getMin());
        }
        if (!Float.isInfinite(value.getMax())) {
            jgen.writeNumberField("max", value.getMax());
        }
        jgen.writeEndObject();
    }
}


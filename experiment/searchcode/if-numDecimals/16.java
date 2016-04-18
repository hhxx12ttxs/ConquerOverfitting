/*
 * Created by Daniel Marell Feb 10, 2010 9:43:32 PM
 */
package se.marell.dvesta.ioscan.impl;

import org.jetbrains.annotations.NotNull;
import se.marell.dcommons.util.MutableElementQueue;
import se.marell.dvesta.ioscan.FloatInput;
import se.marell.dvesta.ioscan.FloatSample;
import se.marell.dvesta.ioscan.IoType;

public class IoScanFloatInput extends AbstractIoDevice implements FloatInput {
    private static final long serialVersionUID = 1;
    protected float value;
    protected int numDecimals;
    protected float min;
    protected float max;
    protected boolean overrideEnable;
    protected float overrideValue;
    protected transient MutableElementQueue<FloatSample> samples;
    private String printFormat;

    public IoScanFloatInput(@NotNull String name, @NotNull String unit, float unmappedValue, int numDecimals, float min, float max) {
        this(IoType.ANALOG_INPUT, name, unit, unmappedValue, numDecimals, min, max);
    }

    protected IoScanFloatInput(IoType type, @NotNull String name, @NotNull String unit, float unmappedValue, int numDecimals, float min, float max) {
        super(type, name, unit);
        this.value = unmappedValue;
        this.numDecimals = numDecimals;
        this.min = min;
        this.max = max;
        samples = new MutableElementQueue<FloatSample>(
                new FloatSample[getSamplesBufferSize(name)],
                new MutableElementQueue.Initializer<FloatSample>() {
                    @Override
                    public FloatSample create() {
                        return new FloatSample();
                    }
                });
        printFormat = String.format("%%.%df", numDecimals);
    }

    @Override
    public String getValueAsString() {
        return String.format(printFormat, value);
    }

    @Override
    public float getValue() {
        return overrideEnable ? overrideValue : value;
    }

    @Override
    public int getNumDecimals() {
        return numDecimals;
    }

    @Override
    public float getMin() {
        return min;
    }

    @Override
    public float getMax() {
        return max;
    }

    @Override
    public synchronized void setStatus(long timestamp, float value) {
        super.setIoStatus(timestamp, true);
        this.value = value;
        addSample();
    }

    protected void addSample() {
        if (samples.isFull()) {
            samples.getFirst();
        }
        FloatSample s = samples.putLast();
        s.timestamp = getTimestamp();
        s.value = value;
    }

    @Override
    public boolean isOverrideEnable() {
        return overrideEnable;
    }

    @Override
    public void setOverrideEnable(boolean overrideEnable) {
        this.overrideEnable = overrideEnable;
    }

    @Override
    public float getOverrideValue() {
        return overrideValue;
    }

    @Override
    public void setOverrideValue(float overrideValue) {
        this.overrideValue = overrideValue;
    }

    public MutableElementQueue<FloatSample> getSamples() {
        return samples;
    }
}

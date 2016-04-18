/*
 * Created by Daniel Marell Feb 10, 2010 9:48:32 PM
 */
package se.marell.dvesta.ioscan.impl;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.marell.dvesta.ioscan.*;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class IoScanIoMapper implements IoMapper {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private Collection<IoDevice> ioDevices = new ArrayList<>();
    private Collection<BitInput> bitInputs = new ArrayList<>();
    private Collection<BitOutput> bitOutputs = new ArrayList<>();
    private Collection<FloatInput> floatInputs = new ArrayList<>();
    private Collection<FloatOutput> floatOutputs = new ArrayList<>();
    private Collection<IntegerInput> integerInputs = new ArrayList<>();
    private Collection<IntegerOutput> integerOutputs = new ArrayList<>();
    private Collection<AlarmInput> alarmInputs = new ArrayList<>();

    @PostConstruct
    private void activate() {
        log.info("IoScanIoMapper started");
    }

    @NotNull
    public Collection<IoDevice> getIoDevices() {
        return ioDevices;
    }

    @NotNull
    public Collection<BitInput> getBitInputs() {
        return bitInputs;
    }

    @NotNull
    @Override
    public BitInput findBitInput(@NotNull String name) {
        return findIoDeviceByName(bitInputs, name);
    }

    @NotNull
    public Collection<BitOutput> getBitOutputs() {
        return bitOutputs;
    }

    @NotNull
    @Override
    public BitOutput findBitOutput(@NotNull String name) {
        return findIoDeviceByName(bitOutputs, name);
    }

    @NotNull
    public Collection<FloatInput> getFloatInputs() {
        return floatInputs;
    }

    @NotNull
    @Override
    public FloatInput findFloatInput(@NotNull String name) {
        return findIoDeviceByName(floatInputs, name);
    }

    @NotNull
    public Collection<FloatOutput> getFloatOutputs() {
        return floatOutputs;
    }

    @NotNull
    @Override
    public FloatOutput findFloatOutput(@NotNull String name) {
        return findIoDeviceByName(floatOutputs, name);
    }

    @NotNull
    public Collection<IntegerInput> getIntegerInputs() {
        return integerInputs;
    }

    @NotNull
    @Override
    public IntegerInput findIntegerInput(@NotNull String name) {
        return findIoDeviceByName(integerInputs, name);
    }

    @NotNull
    public Collection<IntegerOutput> getIntegerOutputs() {
        return integerOutputs;
    }

    @NotNull
    @Override
    public IntegerOutput findIntegerOutput(@NotNull String name) {
        return findIoDeviceByName(integerOutputs, name);
    }

    @NotNull
    public BitInput getBitInput(@NotNull String name, boolean unmappedState) {
        BitInput d = findIoDeviceByName(bitInputs, name);
        if (d == null) {
            d = new IoScanBitInput(name, "", unmappedState);
            bitInputs.add(d);
            ioDevices.add(d);
        }
        assert d.getUnit().length() == 0; // N/A for BitInput
        return d;
    }

    @NotNull
    public BitOutput getBitOutput(@NotNull String name, boolean unmappedState) {
        BitOutput d = findIoDeviceByName(bitOutputs, name);
        if (d == null) {
            d = new IoScanBitOutput(name, "", unmappedState);
            bitOutputs.add(d);
            ioDevices.add(d);
        }
        assert d.getUnit().length() == 0; // N/A for BitOutput
        return d;
    }

    @NotNull
    public FloatInput getFloatInput(@NotNull String name, @NotNull String unit, float unmappedValue, int numDecimals, float min, float max) throws IoMappingException {
        FloatInput d = findIoDeviceByName(floatInputs, name);
        if (d == null) {
            d = new IoScanFloatInput(name, unit, unmappedValue, numDecimals, min, max);
            floatInputs.add(d);
            ioDevices.add(d);
        } else {
            if (!d.getUnit().equals(unit)) {
                throw new WrongUnitException("Float input device " + name + ": Expected unit '" + unit + "' found '" + d.getUnit() + "'");
            }
        }
        return d;
    }

    @NotNull
    @Override
    public FloatInput getFloatInput(@NotNull String name, @NotNull String unit, float unmappedValue, int numDecimals) throws IoMappingException {
        return getFloatInput(name, unit, unmappedValue, numDecimals, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    }

    @NotNull
    public FloatOutput getFloatOutput(@NotNull String name, @NotNull String unit, float unmappedValue, int numDecimals, float min, float max) throws IoMappingException {
        FloatOutput d = findIoDeviceByName(floatOutputs, name);
        if (d == null) {
            d = new IoScanFloatOutput(name, unit, unmappedValue, numDecimals, min, max);
            floatOutputs.add(d);
            ioDevices.add(d);
        } else {
            if (!d.getUnit().equals(unit)) {
                throw new WrongUnitException("Float output device " + name + ": Expected unit '" + unit + "' found '" + d.getUnit() + "'");
            }
        }
        return d;
    }

    @NotNull
    public IntegerInput getIntegerInput(@NotNull String name, @NotNull String unit, int unmappedValue, int min, int max) throws IoMappingException {
        IntegerInput d = findIoDeviceByName(integerInputs, name);
        if (d == null) {
            d = new IoScanIntegerInput(name, unit, unmappedValue, min, max);
            integerInputs.add(d);
            ioDevices.add(d);
        } else {
            if (!d.getUnit().equals(unit)) {
                throw new WrongUnitException("Integer input device " + name + ": Expected unit '" + unit + "' found '" + d.getUnit() + "'");
            }
        }
        return d;
    }

    @NotNull
    public IntegerOutput getIntegerOutput(@NotNull String name, @NotNull String unit, int unmappedValue, int min, int max) throws IoMappingException {
        IntegerOutput d = findIoDeviceByName(integerOutputs, name);
        if (d == null) {
            d = new IoScanIntegerOutput(name, unit, unmappedValue, min, max);
            integerOutputs.add(d);
            ioDevices.add(d);
        } else {
            if (!d.getUnit().equals(unit)) {
                throw new WrongUnitException("Integer output device " + name + ": Expected unit '" + unit + "' found '" + d.getUnit() + "'");
            }
        }
        return d;
    }

    @Override
    public Collection<AlarmInput> getAlarmInputs() {
        return alarmInputs;
    }

    @Override
    public AlarmInput getAlarmInput(@NotNull String name) throws IoMappingException {
        AlarmInput d = findIoDeviceByName(alarmInputs, name);
        if (d == null) {
            d = new IoScanAlarmInput(name);
            alarmInputs.add(d);
            ioDevices.add(d);
        }
        return d;
    }

    @Nullable
    @Override
    public AlarmInput findAlarmInput(@NotNull String name) {
        return findIoDeviceByName(alarmInputs, name);
    }

    public void mapBitInput(@NotNull String name, @NotNull String deviceAddress, @NotNull String addressDescription) {
        IoDevice d = getBitInput(name, false);
        d.mapDevice(deviceAddress, addressDescription);
    }

    public void mapBitOutput(@NotNull String name, @NotNull String deviceAddress, @NotNull String addressDescription) {
        IoDevice d = getBitOutput(name, false);
        d.mapDevice(deviceAddress, addressDescription);
    }

    @Override
    public void mapFloatInput(@NotNull String name, @NotNull String deviceAddress, @NotNull String addressDescription, @NotNull String unit, int numDecimals, float min, float max) throws IoMappingException {
        IoDevice d = getFloatInput(name, unit, Float.NaN, numDecimals, min, max);
        d.mapDevice(deviceAddress, addressDescription);
    }

    @Override
    public void mapFloatOutput(@NotNull String name, @NotNull String deviceAddress, @NotNull String addressDescription, @NotNull String unit, int numDecimals, float min, float max) throws IoMappingException {
        IoDevice d = getFloatOutput(name, unit, Float.NaN, numDecimals, min, max);
        d.mapDevice(deviceAddress, addressDescription);
    }

    @Override
    public void mapIntegerInput(@NotNull String name, @NotNull String deviceAddress, @NotNull String addressDescription, @NotNull String unit, int min, int max) throws IoMappingException {
        IoDevice d = getIntegerInput(name, unit, Integer.MIN_VALUE, min, max);
        d.mapDevice(deviceAddress, addressDescription);
    }

    @Override
    public void mapIntegerOutput(@NotNull String name, @NotNull String deviceAddress, @NotNull String addressDescription, @NotNull String unit, int min, int max) throws IoMappingException {
        IoDevice d = getIntegerOutput(name, unit, Integer.MIN_VALUE, min, max);
        d.mapDevice(deviceAddress, addressDescription);
    }

    @Override
    public void mapAlarmInput(@NotNull String name, @NotNull String deviceAddress, @NotNull String addressDescription) throws IoMappingException {
        IoDevice d = getAlarmInput(name);
        d.mapDevice(deviceAddress, addressDescription);
    }

    private <T extends IoDevice> T findIoDeviceByName(@NotNull Collection<T> devices, @NotNull String name) {
        for (T d : devices) {
            if (d.getName().equals(name)) {
                return d;
            }
        }
        return null;
    }
}


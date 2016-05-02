package sk.wladimiiir.intellihome.model.themometer;

import sk.wladimiiir.intellihome.device.OneWireDevice;
import sk.wladimiiir.intellihome.device.exception.OneWireDeviceError;
import sk.wladimiiir.intellihome.model.exception.ThermometerException;
import sk.wladimiiir.intellihome.model.thermometer.Thermometer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wladimiiir
 * @since 7/4/15
 */
public class DS18B20Thermometer implements Thermometer {
    private static final Pattern TEMPERATURE_VALUE_PATTERN = Pattern.compile("t=(-?\\d+)");

    private final String id;
    private final String name;
    private final OneWireDevice thermometerDevice;

    private float temperature;
    private Instant nextReadTime = Instant.now();

    public DS18B20Thermometer(String id, String name, OneWireDevice thermometerDevice) {
        this.id = id;
        this.name = name;
        this.thermometerDevice = thermometerDevice;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getTemperature() throws ThermometerException {
        if (Instant.now().isBefore(nextReadTime)) {
            return temperature;
        }

        try {
            temperature = parseTemperature(thermometerDevice.readValue());
            nextReadTime = Instant.now().plus(1, ChronoUnit.SECONDS);
            return temperature;
        } catch (OneWireDeviceError oneWireDeviceError) {
            return cannotReadException(oneWireDeviceError);
        }
    }

    private float parseTemperature(String value) throws ThermometerException {
        if (!value.contains("YES")) {
            return temperature;
        }

        final Matcher matcher = TEMPERATURE_VALUE_PATTERN.matcher(value);
        if (!matcher.find()) {
            return temperature;
        }

        return Integer.parseInt(matcher.group(1)) / 1000f;
    }

    private float cannotReadException(Throwable cause) throws ThermometerException {
        throw new ThermometerException("Cannot read from DS18B20 with serial: " + thermometerDevice.getSerialNumber(), cause);
    }
}


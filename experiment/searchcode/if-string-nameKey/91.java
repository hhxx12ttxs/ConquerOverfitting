package cc.clearcode.nudanachacie.facebook.parameters;

import android.os.Bundle;

public class CreateEventParameters {

    private Bundle bundle;
    private static final String nameKey = "name";
    private static final String startTimeKey = "start_time";
    private static final String endTimeKey = "end_time";
    private static final String descriptionKey = "description";
    private static final String locationKey = "location";
    private static final String privacyTypeKey = "privacy_type";

    public CreateEventParameters(String name, String startTime, String endTime,
                                String description, String location, String privacyType) {
        super();
        this.bundle = new Bundle();
        if (name != null)
            bundle.putString(nameKey, name);
        if (startTime != null)
            bundle.putString(startTimeKey, startTime);
        if (endTime != null)
            bundle.putString(endTimeKey, endTime);
        if (description != null)
            bundle.putString(descriptionKey, description);
        if (location != null)
            bundle.putString(locationKey, location);
        if (privacyType != null)
            bundle.putString(privacyTypeKey, privacyType);
    }

    public Bundle getBundle() {
        return bundle;
    }
}


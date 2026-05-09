package com.yuelutraffic.location;

import com.yuelutraffic.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    public static final double MIN_LATITUDE = 28.1200;
    public static final double MAX_LATITUDE = 28.2100;
    public static final double MIN_LONGITUDE = 112.8800;
    public static final double MAX_LONGITUDE = 112.9900;

    public void requireInsidePilotArea(double latitude, double longitude) {
        if (!isInsidePilotArea(latitude, longitude)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Location is outside the Central South University and Lushan South Road pilot area");
        }
    }

    public boolean isInsidePilotArea(double latitude, double longitude) {
        return latitude >= MIN_LATITUDE
                && latitude <= MAX_LATITUDE
                && longitude >= MIN_LONGITUDE
                && longitude <= MAX_LONGITUDE;
    }
}

package com.yuelutraffic.location;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LocationServiceTest {

    private final LocationService service = new LocationService();

    @Test
    void centralSouthUniversityPilotCoordinateIsInsideArea() {
        assertTrue(service.isInsidePilotArea(28.1703, 112.9388));
    }

    @Test
    void distantCoordinateIsOutsideArea() {
        assertFalse(service.isInsidePilotArea(27.9000, 112.7000));
    }
}

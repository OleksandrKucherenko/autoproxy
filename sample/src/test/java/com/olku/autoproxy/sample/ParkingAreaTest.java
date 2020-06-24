package com.olku.autoproxy.sample;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by developer on 12/20/17.
 */
public class ParkingAreaTest {
    @Test
    public void testToBuilder() throws Exception {
        final ParkingArea first = ParkingArea.builder().build();

        final ParkingArea second = first.toBuilder().build();

        // confirm: this is different instances, fields copied, runtime data copied by reference
        assertTrue(second.id() == first.id());
        assertFalse(second.hashCode() != first.hashCode());
        assertTrue(second.runtimeData == first.runtimeData);
    }

}
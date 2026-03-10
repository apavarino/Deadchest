package me.crylonz.deadchest.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeadChestUpdaterVersionTest {

    @Test
    void compareVersionPartsTreatsFiveZeroZeroAsNewerThanFourTwentyFourZero() {
        int comparison = DeadChestUpdater.compareVersionParts(
                DeadChestUpdater.parseVersionParts("5.0.0"),
                DeadChestUpdater.parseVersionParts("4.24.0")
        );

        assertEquals(1, comparison);
    }

    @Test
    void compareVersionPartsTreatsMultiDigitSegmentsNumerically() {
        int comparison = DeadChestUpdater.compareVersionParts(
                DeadChestUpdater.parseVersionParts("4.10.0"),
                DeadChestUpdater.parseVersionParts("4.9.9")
        );

        assertEquals(1, comparison);
    }

    @Test
    void compareVersionPartsTreatsMissingSegmentsAsZero() {
        int comparison = DeadChestUpdater.compareVersionParts(
                DeadChestUpdater.parseVersionParts("5"),
                DeadChestUpdater.parseVersionParts("5.0.0")
        );

        assertEquals(0, comparison);
    }

    @Test
    void parseVersionPartsSupportsSuffixesAfterNumericSegments() {
        List<Integer> parts = DeadChestUpdater.parseVersionParts("5.0.0-RC1");

        assertEquals(List.of(5, 0, 0), parts);
    }

    @Test
    void parseVersionPartsRejectsVersionsWithoutNumericSegments() {
        assertThrows(IllegalArgumentException.class, () -> DeadChestUpdater.parseVersionParts("SNAPSHOT"));
    }
}

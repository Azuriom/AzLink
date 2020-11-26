package com.azuriom.azlink.common;

import com.azuriom.azlink.common.utils.UpdateChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateCheckerTest {

    @Test
    public void testCompareVersions() {
        assertEquals(0, UpdateChecker.compareVersions("1.2", "1.2.0"));
        assertEquals(0, UpdateChecker.compareVersions("1.2.1", "1.2.1"));
        assertEquals(1, UpdateChecker.compareVersions("1.2.1", "1.2.0"));
        assertEquals(1, UpdateChecker.compareVersions("1.2.1", "1.2"));
        assertEquals(1, UpdateChecker.compareVersions("1.2.1", "0.8.1"));
        assertEquals(-1, UpdateChecker.compareVersions("0.9", "1.0.1"));
        assertEquals(-1, UpdateChecker.compareVersions("1.0.1", "1.10"));
        assertEquals(-1, UpdateChecker.compareVersions("1.0.1", "1.10.0"));
        assertEquals(-1, UpdateChecker.compareVersions("1.1.2", "1.2.0"));
    }
}

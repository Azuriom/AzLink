package com.azuriom.azlink.common;

import com.azuriom.azlink.common.utils.UpdateChecker;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateCheckerTest {

    @ParameterizedTest
    @CsvSource({
            "0, 1.2, 1.2.0",
            "0, 1.2.1, 1.2.1",
            "1, 1.2.1, 1.2.0",
            "1, 1.2.1, 1.2",
            "1, 1.2.1, 0.8.1",
            "-1, 0.9, 1.0.1",
            "-1, 1.0.1, 1.10.0",
            "-1, 1.0.1, 1.10.0",
            "-1, 1.1.2, 1.2.0",
    })
    void testCompareVersions(int expected, String ver1, String ver2) {
        assertEquals(expected, UpdateChecker.compareVersions(ver1, ver2));
    }
}

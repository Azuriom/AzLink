package com.azuriom.azlink.common.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstantAdapterTest {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .create();

    private static final Instant INSTANT = LocalDateTime.of(2019, 2, 1, 3, 45, 27)
            .toInstant(ZoneOffset.UTC);

    @Test
    void testDeserialize() {
        assertEquals(INSTANT, GSON.fromJson("\"2019-02-01T03:45:27+00:00\"", Instant.class));
    }

    @Test
    void testSerialize() {
        assertEquals("\"2019-02-01T03:45:27Z\"", GSON.toJson(INSTANT));
    }
}

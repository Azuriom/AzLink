package com.azuriom.azlink.fabric.command;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.text.Text;

public final class TextAdapter {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .extractUrls()
            .build();

    private TextAdapter() {
        throw new UnsupportedOperationException();
    }

    public static Text toText(String message) {
        TextComponent component = LEGACY_SERIALIZER.deserialize(message);
        String json = GsonComponentSerializer.gson().serialize(component);

        return Text.Serializer.fromJson(json);
    }
}

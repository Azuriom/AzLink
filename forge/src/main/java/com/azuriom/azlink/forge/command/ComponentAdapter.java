package com.azuriom.azlink.forge.command;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.network.chat.Component;

public final class ComponentAdapter {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .extractUrls()
            .build();

    private ComponentAdapter() {
        throw new UnsupportedOperationException();
    }

    public static Component toComponent(String message) {
        TextComponent component = LEGACY_SERIALIZER.deserialize(message);
        String json = GsonComponentSerializer.gson().serialize(component);
        Component parsed = Component.Serializer.fromJson(json);
        return parsed != null ? parsed : Component.literal(message);
    }
}

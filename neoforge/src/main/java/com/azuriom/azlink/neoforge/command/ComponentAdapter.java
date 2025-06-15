package com.azuriom.azlink.neoforge.command;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.core.RegistryAccess;
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
        JsonElement json = GsonComponentSerializer.gson().serializeToTree(component);

        return Component.Serializer.fromJson(json, RegistryAccess.EMPTY);    }
}

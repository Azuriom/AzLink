package com.azuriom.azlink.neoforge.command;

import com.mojang.serialization.JsonOps;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

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

        // Code from LuckPerms, under MIT License
        // https://github.com/LuckPerms/LuckPerms
        return ComponentSerialization.CODEC.decode(
                RegistryAccess.EMPTY.createSerializationContext(JsonOps.INSTANCE),
                GsonComponentSerializer.gson().serializeToTree(component)
        ).getOrThrow(IllegalArgumentException::new).getFirst();
    }
}

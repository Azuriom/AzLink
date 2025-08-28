package com.azuriom.azlink.fabric.command;

import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

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

        // Code from LuckPerms, under MIT License
        // https://github.com/LuckPerms/LuckPerms
        return TextCodecs.CODEC.decode(
                DynamicRegistryManager.EMPTY.getOps(JsonOps.INSTANCE),
                GsonComponentSerializer.gson().serializeToTree(component)
        ).getOrThrow(JsonParseException::new).getFirst();
    }
}

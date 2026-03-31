package com.azuriom.azlink.forge;

import com.azuriom.azlink.common.chat.TextColor;
import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.chat.TextDecoration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.EnumMap;
import java.util.Map;

/**
 * Adapter to convert {@link TextComponent} to Minecraft {@link Component}.
 */
public final class MinecraftComponentAdapter {

    private static final Map<TextColor, ChatFormatting> COLOR_MAP = new EnumMap<>(TextColor.class);
    private static final Map<TextDecoration, ChatFormatting> DECORATION_MAP = new EnumMap<>(TextDecoration.class);

    static {
        for (TextColor color : TextColor.values()) {
            COLOR_MAP.put(color, ChatFormatting.valueOf(color.name()));
        }

        for (TextDecoration decoration : TextDecoration.values()) {
            DECORATION_MAP.put(decoration, ChatFormatting.valueOf(decoration.name()));
        }
    }

    private MinecraftComponentAdapter() {
        throw new UnsupportedOperationException();
    }

    public static Component toComponent(TextComponent component) {
        MutableComponent result = Component.literal(component.content());

        if (component.color() != null) {
            result.withStyle(COLOR_MAP.get(component.color()));
        }

        for (TextDecoration decoration : component.decorations()) {
            result.withStyle(DECORATION_MAP.get(decoration));
        }

        if (component.url() != null) {
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, component.url());
            result.withStyle(Style.EMPTY.withClickEvent(clickEvent));
        }

        for (TextComponent child : component.children()) {
            result.append(toComponent(child));
        }

        return result;
    }
}

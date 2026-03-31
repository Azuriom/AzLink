package com.azuriom.azlink.fabric;

import com.azuriom.azlink.common.chat.TextColor;
import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.chat.TextDecoration;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.EnumMap;
import java.util.Map;

/**
 * Adapter to convert {@link TextComponent} to Minecraft {@link Text}.
 */
public final class MinecraftTextAdapter {

    private static final Map<TextColor, Formatting> COLOR_MAP = new EnumMap<>(TextColor.class);
    private static final Map<TextDecoration, Formatting> DECORATION_MAP = new EnumMap<>(TextDecoration.class);

    static {
        for (TextColor color : TextColor.values()) {
            COLOR_MAP.put(color, Formatting.valueOf(color.name()));
        }

        for (TextDecoration decoration : TextDecoration.values()) {
            DECORATION_MAP.put(decoration, Formatting.valueOf(decoration.name()));
        }
    }

    private MinecraftTextAdapter() {
        throw new UnsupportedOperationException();
    }

    public static Text toText(TextComponent component) {
        MutableText result = Text.literal(component.content());

        if (component.color() != null) {
            result.formatted(COLOR_MAP.get(component.color()));
        }

        for (TextDecoration decoration : component.decorations()) {
            result.formatted(DECORATION_MAP.get(decoration));
        }

        if (component.url() != null) {
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, component.url());
            result.fillStyle(Style.EMPTY.withClickEvent(clickEvent));
        }

        for (TextComponent child : component.children()) {
            result.append(toText(child));
        }

        return result;
    }
}

package com.azuriom.azlink.common.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Index;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter to convert {@link TextComponent} to Kyori Adventure {@link Component}.
 */
public final class AdventureComponentAdapter {

    private static final Map<TextColor, NamedTextColor> COLOR_MAP = new EnumMap<>(TextColor.class);
    private static final Map<TextDecoration, net.kyori.adventure.text.format.TextDecoration> DECORATION_MAP =
            new EnumMap<>(TextDecoration.class);

    static {
        for (TextColor color : TextColor.values()) {
            COLOR_MAP.put(color, NamedTextColor.NAMES.value(color.name().toLowerCase(Locale.ROOT)));
        }

        for (TextDecoration decoration : TextDecoration.values()) {
            Index<String, net.kyori.adventure.text.format.TextDecoration> decorations = net.kyori.adventure.text.format.TextDecoration.NAMES;
            DECORATION_MAP.put(decoration, decorations.value(decoration.name().toLowerCase(Locale.ROOT)));
        }
    }

    private AdventureComponentAdapter() {
        throw new UnsupportedOperationException();
    }

    public static Component toAdventure(TextComponent component) {
        Component result = Component.text(component.content(), adaptColor(component.color()));

        for (TextDecoration decoration : component.decorations()) {
            result = result.decorate(DECORATION_MAP.get(decoration));
        }

        if (component.url() != null) {
            result = result.clickEvent(ClickEvent.openUrl(component.url()));
        }

        for (TextComponent child : component.children()) {
            result = result.append(toAdventure(child));
        }

        return result;
    }

    private static NamedTextColor adaptColor(TextColor color) {
        return color != null ? COLOR_MAP.get(color) : null;
    }
}

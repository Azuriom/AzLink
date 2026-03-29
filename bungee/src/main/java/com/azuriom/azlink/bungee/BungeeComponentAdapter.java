package com.azuriom.azlink.bungee;

import com.azuriom.azlink.common.chat.TextColor;
import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.chat.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;

import java.util.EnumMap;
import java.util.Map;

/**
 * Adapter to convert {@link TextComponent} to BungeeCord {@link BaseComponent}.
 */
public final class BungeeComponentAdapter {

    private static final Map<TextColor, ChatColor> COLORS = new EnumMap<>(TextColor.class);

    static {
        for (TextColor color : TextColor.values()) {
            COLORS.put(color, ChatColor.getByChar(color.legacyCode()));
        }
    }

    private BungeeComponentAdapter() {
        throw new UnsupportedOperationException();
    }

    public static BaseComponent toBungee(TextComponent component) {
        BaseComponent result = new net.md_5.bungee.api.chat.TextComponent(component.content());
        result.setColor(adaptColor(component.color()));

        component.decorations().forEach(d -> applyDecoration(result, d));
        component.children().forEach(c -> result.addExtra(toBungee(c)));

        if (component.url() != null) {
            result.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, component.url()));
        }

        return result;
    }

    private static ChatColor adaptColor(TextColor color) {
        return color != null ? COLORS.get(color) : null;
    }

    private static void applyDecoration(BaseComponent component, TextDecoration decoration) {
        switch (decoration) {
            case BOLD:
                component.setBold(true);
                break;
            case ITALIC:
                component.setItalic(true);
                break;
            case UNDERLINE:
                component.setUnderlined(true);
                break;
            default:
                break;
        }
    }
}

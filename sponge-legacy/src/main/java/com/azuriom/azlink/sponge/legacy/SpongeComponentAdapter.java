package com.azuriom.azlink.sponge.legacy;

import com.azuriom.azlink.common.chat.TextColor;
import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.chat.TextDecoration;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

public final class SpongeComponentAdapter {

    private static final Map<TextColor, org.spongepowered.api.text.format.TextColor> COLOR_MAP =
            new EnumMap<>(TextColor.class);
    private static final Map<TextDecoration, TextStyle> FORMAT_MAP = new EnumMap<>(TextDecoration.class);

    static {
        COLOR_MAP.put(TextColor.BLACK, TextColors.BLACK);
        COLOR_MAP.put(TextColor.DARK_BLUE, TextColors.DARK_BLUE);
        COLOR_MAP.put(TextColor.DARK_GREEN, TextColors.DARK_GREEN);
        COLOR_MAP.put(TextColor.DARK_AQUA, TextColors.DARK_AQUA);
        COLOR_MAP.put(TextColor.DARK_RED, TextColors.DARK_RED);
        COLOR_MAP.put(TextColor.DARK_PURPLE, TextColors.DARK_PURPLE);
        COLOR_MAP.put(TextColor.GOLD, TextColors.GOLD);
        COLOR_MAP.put(TextColor.GRAY, TextColors.GRAY);
        COLOR_MAP.put(TextColor.DARK_GRAY, TextColors.DARK_GRAY);
        COLOR_MAP.put(TextColor.BLUE, TextColors.BLUE);
        COLOR_MAP.put(TextColor.GREEN, TextColors.GREEN);
        COLOR_MAP.put(TextColor.AQUA, TextColors.AQUA);
        COLOR_MAP.put(TextColor.RED, TextColors.RED);
        COLOR_MAP.put(TextColor.LIGHT_PURPLE, TextColors.LIGHT_PURPLE);
        COLOR_MAP.put(TextColor.YELLOW, TextColors.YELLOW);
        COLOR_MAP.put(TextColor.WHITE, TextColors.WHITE);

        FORMAT_MAP.put(TextDecoration.BOLD, TextStyles.BOLD);
        FORMAT_MAP.put(TextDecoration.ITALIC, TextStyles.ITALIC);
        FORMAT_MAP.put(TextDecoration.UNDERLINE, TextStyles.UNDERLINE);
    }

    private SpongeComponentAdapter() {
        throw new UnsupportedOperationException();
    }

    public static Text toSponge(TextComponent component) {
        Text.Builder builder = Text.builder();

        if (component.color() != null) {
            builder = builder.color(COLOR_MAP.get(component.color()));
        }

        for (TextDecoration decoration : component.decorations()) {
            builder = builder.style(FORMAT_MAP.get(decoration));
        }

        if (component.url() != null) {
            try {
                URL url = URI.create(component.url()).toURL();
                builder = builder.onClick(TextActions.openUrl(url));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }

        for (TextComponent child : component.children()) {
            builder = builder.append(toSponge(child));
        }

        return builder.build();
    }
}

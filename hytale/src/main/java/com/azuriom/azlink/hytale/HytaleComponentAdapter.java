package com.azuriom.azlink.hytale;

import com.azuriom.azlink.common.chat.TextComponent;
import com.azuriom.azlink.common.chat.TextDecoration;
import com.hypixel.hytale.server.core.Message;

public final class HytaleComponentAdapter {

    private HytaleComponentAdapter() {
        throw new UnsupportedOperationException();
    }

    public static Message toHytale(TextComponent component) {
        Message result = Message.raw(component.content());

        if (component.color() != null) {
            result = result.color(component.color().hex());
        }

        for (TextDecoration decoration : component.decorations()) {
            result = applyDecoration(result, decoration);
        }

        if (component.url() != null) {
            result = result.link(component.url());
        }

        for (TextComponent child : component.children()) {
            result = result.insert(toHytale(child));
        }

        return result;
    }

    private static Message applyDecoration(Message message, TextDecoration decoration) {
        switch (decoration) {
            case BOLD:
                return message.bold(true);
            case ITALIC:
                return message.italic(true);
            case UNDERLINE:
                return message; //.underlined(true); TODO missing method in Hytale
            default:
                return message;
        }
    }
}

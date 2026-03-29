package com.azuriom.azlink.common.chat;

/**
 * Represents text decorations (formatting) that can be applied to text components.
 */
public enum TextDecoration {
    BOLD('l'),
    ITALIC('o'),
    UNDERLINE('s');

    private final char legacyCode;

    TextDecoration(char legacyCode) {
        this.legacyCode = legacyCode;
    }

    public char legacyCode() {
        return this.legacyCode;
    }
}

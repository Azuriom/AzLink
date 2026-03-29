package com.azuriom.azlink.common.chat;

/**
 * Represents the standard 16 colors available in Minecraft chat, and their associated hex color.
 */
public enum TextColor {
    BLACK('0', "#000000"),
    DARK_BLUE('1', "#0000aa"),
    DARK_GREEN('2', "#00aa00"),
    DARK_AQUA('3', "#00aaaa"),
    DARK_RED('4', "#aa0000"),
    DARK_PURPLE('5', "#aa00aa"),
    GOLD('6', "#ffaa00"),
    GRAY('7', "#aaaaaa"),
    DARK_GRAY('8', "#555555"),
    BLUE('9', "#5555ff"),
    GREEN('a', "#55ff55"),
    AQUA('b', "#55ffff"),
    RED('c', "#ff5555"),
    LIGHT_PURPLE('d', "#ff55ff"),
    YELLOW('e', "#ffff55"),
    WHITE('f', "#ffffff");

    private final char legacyCode;
    private final String hex;

    TextColor(char legacyCode, String hex) {
        this.legacyCode = legacyCode;
        this.hex = hex;
    }

    public char legacyCode() {
        return this.legacyCode;
    }

    public String hex() {
        return this.hex;
    }
}

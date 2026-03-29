package com.azuriom.azlink.common.chat;

import java.util.*;

/**
 * Represents a game-agnostic (Minecraft or Hytale) immutable text component with
 * optional color, URL, decorations, and child components.
 * Inspired by Kyori's Adventure API (MIT licensed).
 *
 * @see <a href="https://github.com/KyoriPowered/adventure">Adventure API</a>
 */
public final class TextComponent {

    private static final char MINECRAFT_LEGACY_CHAR = '&';

    private final String content;
    private final TextColor color;
    private final String url;
    private final Set<TextDecoration> decorations;
    private final List<TextComponent> children;

    private TextComponent(String content, TextColor color, String url,
                          Set<TextDecoration> decorations, List<TextComponent> children) {
        this.content = content;
        this.color = color;
        this.url = url;
        this.decorations = decorations.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(decorations));
        this.children = children.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(children));
    }

    /**
     * Creates a new text component with the given text.
     *
     * @param text the text content
     * @return a new text component
     */
    public static TextComponent text(String text) {
        return new TextComponent(text, null, null, EnumSet.noneOf(TextDecoration.class), Collections.emptyList());
    }

    /**
     * Creates a new colored text component.
     *
     * @param text  the text content
     * @param color the text color
     * @return a new text component
     */
    public static TextComponent text(String text, TextColor color) {
        return new TextComponent(text, color, null, EnumSet.noneOf(TextDecoration.class), Collections.emptyList());
    }

    /**
     * Creates a clickable link component with the URL as the display text.
     *
     * @param url the URL to link to
     * @return a new text component with a click event
     */
    public static TextComponent link(String url) {
        return text(url).url(url);
    }

    /**
     * Creates a new empty text component.
     *
     * @return a new empty text component
     */
    public static TextComponent empty() {
        return text("");
    }

    /**
     * Returns a new component with the specified color.
     *
     * @param color the text color
     * @return a new text component with the color applied
     */
    public TextComponent color(TextColor color) {
        return new TextComponent(this.content, color, this.url, this.decorations, this.children);
    }

    /**
     * Returns a new component with the specified URL click event.
     *
     * @param url the URL to open when clicked
     * @return a new text component with the URL applied
     */
    public TextComponent url(String url) {
        return new TextComponent(this.content, this.color, url, this.decorations, this.children);
    }

    /**
     * Returns a new component with the specified decoration added.
     *
     * @param decoration the decoration to add
     * @return a new text component with the decoration applied
     */
    public TextComponent decoration(TextDecoration decoration) {
        Set<TextDecoration> newDecorations = EnumSet.copyOf(this.decorations);
        newDecorations.add(decoration);
        return new TextComponent(this.content, this.color, this.url, newDecorations, this.children);
    }

    /**
     * Returns a new component with all specified decorations added.
     *
     * @param decorations the decorations to add
     * @return a new text component with the decorations applied
     */
    public TextComponent decorate(TextDecoration... decorations) {
        Set<TextDecoration> newDecorations = EnumSet.copyOf(this.decorations);
        Collections.addAll(newDecorations, decorations);

        return new TextComponent(this.content, this.color, this.url, newDecorations, this.children);
    }

    /**
     * Returns a new component with the specified child component appended.
     *
     * @param child the child component to append
     * @return a new text component with the child appended
     */
    public TextComponent append(TextComponent child) {
        List<TextComponent> newChildren = new ArrayList<>(this.children.size() + 1);
        newChildren.addAll(this.children);
        newChildren.add(child);

        return new TextComponent(this.content, this.color, this.url, this.decorations, newChildren);
    }

    /**
     * Returns a new component with all specified child components appended.
     *
     * @param children the child components to append
     * @return a new text component with the children appended
     */
    public TextComponent append(TextComponent... children) {
        List<TextComponent> newChildren = new ArrayList<>(this.children.size() + children.length);
        newChildren.addAll(this.children);
        Collections.addAll(newChildren, children);

        return new TextComponent(this.content, this.color, this.url, this.decorations, newChildren);
    }

    /**
     * Gets the text content of this component.
     *
     * @return the text content
     */
    public String content() {
        return this.content;
    }

    /**
     * Gets the color of this component.
     *
     * @return the color, or null if no color is set
     */
    public TextColor color() {
        return this.color;
    }

    /**
     * Gets the URL click event of this component.
     *
     * @return the URL, or null if no URL is set
     */
    public String url() {
        return this.url;
    }

    /**
     * Gets the decorations applied to this component.
     *
     * @return an immutable set of decorations
     */
    public Set<TextDecoration> decorations() {
        return this.decorations;
    }

    /**
     * Gets the child components of this component.
     *
     * @return an immutable list of children
     */
    public List<TextComponent> children() {
        return this.children;
    }

    /**
     * Checks if this component has the specified decoration.
     *
     * @param decoration the decoration to check
     * @return true if the decoration is present
     */
    public boolean hasDecoration(TextDecoration decoration) {
        return this.decorations.contains(decoration);
    }

    /**
     * Converts this component to Minecraft legacy format (with {@code '&'} char).
     *
     * @return the legacy formatted string
     */
    public String toMinecraftLegacy() {
        StringBuilder buffer = new StringBuilder();

        if (this.color != null) {
            buffer.append(MINECRAFT_LEGACY_CHAR).append(this.color.legacyCode());
        }

        for (TextDecoration decoration : this.decorations) {
            buffer.append(MINECRAFT_LEGACY_CHAR).append(decoration.legacyCode());
        }

        buffer.append(this.content);

        for (TextComponent child : this.children) {
            buffer.append(child.toMinecraftLegacy());
        }

        return buffer.toString();
    }

    /**
     * Returns a plain text representation without formatting.
     *
     * @return the plain text content
     */
    @Override
    public String toString() {
        if (this.children.isEmpty()) {
            return this.content;
        }

        StringBuilder buffer = new StringBuilder(this.content);
        for (TextComponent child : this.children) {
            buffer.append(child.toString());
        }
        return buffer.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TextComponent that = (TextComponent) o;

        return Objects.equals(this.content, that.content) &&
                this.color == that.color &&
                Objects.equals(this.url, that.url) &&
                Objects.equals(this.decorations, that.decorations) &&
                Objects.equals(this.children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.content, this.color, this.url, this.decorations, this.children);
    }
}

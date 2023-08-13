package net.reworlds.modifiedbosses.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ComponentUtils {

    public static String plainText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static Component gradient(String hex1, String hex2, String text) {
        return MiniMessage.miniMessage().deserialize("<gradient:%1$s:%2$s>%3$s</gradient>".formatted(hex1, hex2, text));
    }

    public static Component gradient(String hex1, String hex2, Component text) {
        return MiniMessage.miniMessage().deserialize("<gradient:%1$s:%2$s>%3$s</gradient>".formatted(hex1, hex2, plainText(text)));
    }

    public static Component rainbow(String text) {
        return MiniMessage.miniMessage().deserialize("<rainbow>%s</rainbow>".formatted(text));
    }

    public static Component rainbow(Component text) {
        return MiniMessage.miniMessage().deserialize("<rainbow>%s</rainbow>".formatted(plainText(text)));
    }
}

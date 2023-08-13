package net.reworlds.modifiedbosses.bestiary;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BestiaryBook {

    private static final Component[] pages = new Component[]{
            Component.text(
                            """
                                            Боссы
                                    """)
                    .appendNewline().append(Component.text("• Эндер-Дракон")
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/bestiary dragon"))
                            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Эндер-Дракон"))))
                    .appendNewline().appendNewline().append(Component.text("• Джелу, Погибель Дьявола")
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/bestiary gelu"))
                            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Джелу, Погибель Дьявола"))))
                    .append(Component.text("\n\n• Скоро..."))
    };
    private static ItemStack book;

    public static ItemStack generateBook() {
        if (book != null) {
            return book;
        }

        book = new ItemStack(Material.WRITTEN_BOOK);

        BookMeta itemMeta = (BookMeta) book.getItemMeta();
        itemMeta.setTitle("Бестиарий");
        itemMeta.setAuthor("Утерян");
        itemMeta.setGeneration(BookMeta.Generation.TATTERED);
        itemMeta.setDisplayName("Бестиарий");

        itemMeta.addPages(pages);

        book.setItemMeta(itemMeta);

        return book;
    }

    public static void open(Player player) {
        player.openBook(generateBook());
    }
}

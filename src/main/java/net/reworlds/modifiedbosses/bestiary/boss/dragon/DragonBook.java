package net.reworlds.modifiedbosses.bestiary.boss.dragon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class DragonBook {

    private static final Component[] pages = new Component[]{
            Component.text(
                    """
                                 Эндер-Дракон
                                 
                            Усложненная версия обычного дракона, предназначенная для группы из 3 и более игроков.
                                                        
                            Не закрывает портал, не восстанавливает столбы и не спавнит яйцо после сметри.
                            """).appendNewline().append(Component.text("• §nДроп§r")
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/bestiary dragon drop"))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Посмотреть дроп")))),
            Component.text(
                    """
                            Характеристики:
                            • 2000 ХП
                            • Дроп со 100+ Урона
                            • Не получает урон от взрывов
                            • Неуязвим, если рядом кристаллы
                            • Дракон и кристаллы подсвечиваются
                            • Накладывает эффект слабости
                            • У дракона 2 фазы - до и после 50% ХП
                            • Респавн 15 минут
                            """
            ),
            Component.text(
                    """
                            Способности:
                            4. Драконий Рёв
                            4. Взгляд Тьмы
                            5. Космический Луч
                            6. Дыхание Смерти
                            6-7. Бомба Души
                            7-8. Кипящая кровь
                            9. Чума Порченной Крови
                            """
            ),
            Component.text(
                    """
                            Драконий Рёв - На первой фазе подкидывает всех атакующих на небольшой расстояние вверх. на второй фазе подкидывает выше.
                                                        
                            Взгляд Тьмы - Все атакующие получают эффект тьмы и слепоты на 10 секунд.
                            """
            ),
            Component.text(
                    """
                            Космический луч - Дракон целится способностью во всех атакующих в течение 5 секунд.
                            После, через секунду, наносит урон в радиусе 6 блоков на месте последней точки прицеливания.
                            
                            На первой фазе наносит 12 ХП, на второй - 24 ХП.
                            """
            ),
            Component.text(
                    """
                            Дыхание Смерти - Дракон спавнит в каждого атакующего 10 драконих фаерболов.
                            
                            Бомба души - Каждый 8 атакующий отмечаются способностью, при которой они и область вокруг них светятся синим цветом.
                            """
            ),
            Component.text(
                    """
                            Отмеченным игрокам необходимо подойти к тиммейтам, чтобы распределить урон. Урон распределяется по формуле 50 / N игроков в радиусе.
                            
                            Кипящая Кровь - Все атакующие отмечаются способностью, при которой они и область вокруг них
                            """
            ),
            Component.text(
                    """
                            светится красным цветом.
                            
                            Отмеченным игрокам необходимо отбежать друг от друга, чтобы получить меньше урона. Урон распределяется по формуле 10 * N игроков в радиусе.
                            """
            ),
            Component.text(
                    """
                            Чума Порченой Крови - Каждый 9 атакующий отмечается способностью, при которой они и область вокруг них светятся зеленым цветом.
                            Всем остальным игрокам необходимо подбежать к отмеченным игрокам, чтобы не умереть.
                            """
            ),

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

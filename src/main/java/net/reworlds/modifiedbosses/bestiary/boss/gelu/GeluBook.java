package net.reworlds.modifiedbosses.bestiary.boss.gelu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class GeluBook {

    private static final Component[] pages = new Component[]{
            Component.text(
                    """
                            Джелу, Погибель Дьявола
                                 
                            Легендарный воин, от которого ныне остались только кости, жаждет мести за предательство.
                            
                            Является групповым боссом для 2 и более игроков.
                            """).appendNewline().append(Component.text("• §nДроп§r")
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/bestiary gelu drop"))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Посмотреть дроп")))),
            Component.text(
                    """
                            Характеристики:
                            • 4000 ХП
                            • Дроп с 200+ Урона
                            • Меняет оружие и очень быстрый
                            • Атакует ближайшего атакующего
                            • Дебаффы: слабость
                            • 3 фазы - >90%, >50%, <50% ХП
                            • Откидывает игроков если рядом >4
                            • Респавн 15 минут: Энд - 91 75 29
                            """
            ),
            Component.text(
                    """
                            Способности:
                            4. Анализ
                            5. Шаг Бога
                            6. Ледяные Мины
                            7. Энергетические кристаллы
                            8. Призыв помощи
                            9. Конец Судьбы
                            
                            §c⚠ Чем дальше вы от босса, тем больше урона от стрел ⚠
                            """
            ),
            Component.text(
                    """
                            Анализ - Все игроки должны отбежать от босса на дальнее расстояние.
                                                        
                            В противном случае босс получает прибавку к урону на минуту, что визуально наблюдается сменой цвета свечения.
                            """
            ),
            Component.text(
                    """
                            Шаг Бога - Босс исчезает в портале, а через 5 секунд появляется около случайного игрока в радиусе 5 блоков, беря его в таргет.
                            """
            ),
            Component.text(
                    """
                            Ледяные мины - Джелу спавнит снежки около игроков, которые светятся как босс. их касание приводит к детонации, нанося 12 урона и откидывая игрока в случайном направлении. 
                            Мины остаются до конца боя либо до детонации, что усложняет сражение.
                            """
            ),
            Component.text(
                    """
                            Не действуют на игроков, которые не сражаются.
                            
                            Энергетические кристаллы - босс спавнит 5 кристаллов, которые нужно сломать, нанеся 20 урона каждому.
                            Если кристаллы не сломаны через 20 секунд, босс наносит
                            """
            ),
            Component.text(
                    """
                            летальный урон всем игрокам.
                            
                            Призыв помощи - босс призывает 5 скелетов помощников, которые имеют небольшие прибавки к урону и скорости.
                            """
            ),
            Component.text(
                    """
                            Конец Судьбы - §kЧувак, ты думал здесь что-то будет? Ооооо, нет. Я люблю страдания и эта способность останется неизвестной до конца. А те, кто узнают что это за способность, будут передавать её из поколения в поколения как легенду.
                            """
            )
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

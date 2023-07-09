package net.reworlds.modifiedbosses.utils;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import lombok.Getter;
import org.bukkit.entity.Player;

public class PacketUtil {
    @Getter
    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public static void sendPacket(Player player, PacketContainer packet) {
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

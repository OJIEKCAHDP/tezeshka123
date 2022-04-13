package dev.ojiekcahdp.tezeshka.chat;

import dev.ojiekcahdp.tezeshka.Main;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final Chat chat = Main.getInstance().getChat();

    private final String formatGlobal = Main.getInstance().getConfig().getString("chat-style.global");
    private final String formatLocal = Main.getInstance().getConfig().getString("chat-style.local");

    private final int radius = Main.getInstance().getConfig().getInt("local-chat-radius");

    @EventHandler
    public void on(AsyncChatEvent e) {
        String message = LegacyComponentSerializer.legacySection().serialize(e.message());
        String format = formatLocal;

        boolean global = message.startsWith("!");

        if (global) {
            message = message.replaceFirst("!", "");
            format = formatGlobal;
        }

        assert format != null;
        Component component = Component.text(format.
                replace("${prefix}", this.chat.getPlayerPrefix(e.getPlayer())).
                replace("${player}", e.getPlayer().getName()).
                replace("${suffix}", this.chat.getPlayerSuffix(e.getPlayer())).
                replace("${color}", getColor(e.getPlayer())).
                replace("${message}", (e.getPlayer().hasPermission("chat.color") ? ChatColor.translateAlternateColorCodes('&', message) : message)));

        if (global) {
            e.setCancelled(true);
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> e.getPlayer().getLocation().getNearbyPlayers(this.radius).forEach(player -> player.sendMessage(component)));
            return;
        }
        e.renderer((player, c, component1, audience) -> component);


    }

    private String getColor(Player player) {
        String group = this.chat.getPrimaryGroup(player);

        String color = Main.getInstance().getConfig().getString("message-color." + group);

        return (color != null ? ChatColor.translateAlternateColorCodes('&', color) : "");
    }

}

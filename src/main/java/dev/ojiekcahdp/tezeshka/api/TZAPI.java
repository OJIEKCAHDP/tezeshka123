package dev.ojiekcahdp.tezeshka.api;

import dev.ojiekcahdp.tezeshka.Main;
import dev.ojiekcahdp.tezeshka.messages.PrivateMessages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class TZAPI {

    public static PrivateMessages privateMessages() {
        return Main.getInstance().getPrivateMessages();
    }

    public static void sendChatMessage(Player player, String message) {
        player.chat(message);
    }

    public static Economy economy() {
        return Main.getInstance().getEconomy();
    }

}

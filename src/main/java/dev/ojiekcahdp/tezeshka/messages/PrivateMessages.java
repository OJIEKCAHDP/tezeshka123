package dev.ojiekcahdp.tezeshka.messages;

import dev.ojiekcahdp.tezeshka.Main;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PrivateMessages {

    private final Chat chat = Main.getInstance().getChat();

    private final String format = Main.getInstance().getConfig().getString("private-messages");

    private final HashMap<Player, Player> lastMessages = new HashMap<>();

    public boolean sendPrivateMessage(Player sender, Player recipient, String message) {

        if (recipient == null) {
            return false;
        }

        assert format != null;
        Component component = Component.text(format.
                replace("${sender_prefix}", this.chat.getPlayerPrefix(sender)).
                replace("${sender}", sender.getName()).
                replace("${sender_suffix}", this.chat.getPlayerSuffix(sender)).
                replace("${recipient_prefix}", this.chat.getPlayerPrefix(recipient)).
                replace("${recipient}", recipient.getName()).
                replace("${recipient_suffix}", this.chat.getPlayerSuffix(recipient)).
                replace("${message}", message));

        sender.sendMessage(component);
        recipient.sendMessage(component);

        lastMessages.put(sender, recipient);
        lastMessages.put(recipient, sender);

        return true;

    }

    public boolean reply(Player sender, String message) {
        if (lastMessages.get(sender) == null) return false;

        return sendPrivateMessage(sender, lastMessages.get(sender), message);
    }


}

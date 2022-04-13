package dev.ojiekcahdp.tezeshka;

import dev.ojiekcahdp.tezeshka.chat.ChatListener;
import dev.ojiekcahdp.tezeshka.database.MySQL;
import dev.ojiekcahdp.tezeshka.economy.CustomEconomy;
import dev.ojiekcahdp.tezeshka.economy.EconomyEntity;
import dev.ojiekcahdp.tezeshka.messages.PrivateMessages;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Getter
    private Chat chat;

    @Getter
    private final MySQL mySQL = new MySQL(getConfig().getString("mysql.host"), getConfig().getString("mysql.user"), getConfig().getString("mysql.password"), getConfig().getString("mysql.database"));

    @Getter
    private PrivateMessages privateMessages;
    @Getter
    private CustomEconomy economy;

    private static @Getter
    Main instance;

    @SneakyThrows
    public void onEnable() {

        Main.instance = this;

        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().severe("Не установлен Vault, плагин не может продолжать свою работу.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        saveDefaultConfig();

        mySQL.executeUpdate("CREATE TABLE IF NOT EXISTS `economy` (`name` VARCHAR(50) NOT NULL,`balance` DOUBLE NOT NULL DEFAULT 0)");
        mySQL.executeUpdate("CREATE TABLE IF NOT EXISTS `economy_logs` (`name` VARCHAR(50) NOT NULL,`value` VARCHAR(50) NOT NULL,`amount` DOUBLE NOT NULL DEFAULT 0)");

        privateMessages = new PrivateMessages();

        if (setupChat()) {
            Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
        } else {
            Bukkit.getLogger().severe("Не могу инициализировать чат.");
        }


        Bukkit.getPluginCommand("msg").setExecutor((sender, cmd, label, args) -> {

            StringBuilder message = new StringBuilder();

            for (int i = 1; i < args.length; i++) {
                message.append(args[i]).append(" ");
            }

            if (label.equalsIgnoreCase("msg")) {

                privateMessages.sendPrivateMessage((Player) sender, Bukkit.getPlayer(args[0]), message.toString());

            } else {

                privateMessages.reply((Player) sender, message.toString());

            }

            return true;
        });

        Bukkit.getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void on(PlayerJoinEvent e) {
                new EconomyEntity(e.getPlayer().getName());
            }

            @EventHandler
            public void on(PlayerQuitEvent e) {
                EconomyEntity.get(e.getPlayer().getName()).save();
            }

        }, this);

        setupEconomy();

        Bukkit.getPluginCommand("eco").setExecutor((sender, cmd, label, args) -> {

            if (args.length < 3) {
                sender.sendMessage(Component.text("Используйте: /eco [give,take,pay] [игрок] [сумма]"));
                return false;
            }

            if (sender.hasPermission("eco.admin"))
            if (args[0].equalsIgnoreCase("give")) {
                EconomyEntity.get(args[1]).deposit(Double.parseDouble(args[2]));
                sender.sendMessage(Component.text("Успешнр"));
                return true;
            } else if (args[0].equalsIgnoreCase("take")) {
               if (EconomyEntity.get(args[1]).withdraw(Double.parseDouble(args[2]))) {
                   sender.sendMessage(Component.text("Успешно"));
               } else {
                   sender.sendMessage(Component.text("У него нет денег"));
               }
               return true;
            }

            if (args[0].equalsIgnoreCase("pay")) {
                if (EconomyEntity.get(sender.getName()).withdraw(Double.parseDouble(args[2]))) {
                    EconomyEntity.get(args[1]).deposit(Double.parseDouble(args[2]));
                    sender.sendMessage(Component.text("Успешно"));
                } else {
                    sender.sendMessage(Component.text("У вас нет денег"));
                }
                return true;
            }

            sender.sendMessage(Component.text("Используйте: /eco [give,take,pay] [игрок] [сумма]"));

            return true;
        });

    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        assert rsp != null;
        this.chat = rsp.getProvider();
        return true;
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            this.getServer().getServicesManager().unregister(economyProvider.getProvider());
        }
        economy = new CustomEconomy();
        this.getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.Highest);
    }

}

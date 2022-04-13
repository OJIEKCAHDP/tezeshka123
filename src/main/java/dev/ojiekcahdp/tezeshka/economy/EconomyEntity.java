package dev.ojiekcahdp.tezeshka.economy;

import dev.ojiekcahdp.tezeshka.Main;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;;

import java.sql.ResultSet;
import java.util.HashMap;

@Getter
@Setter
public class EconomyEntity {

    private static HashMap<String, EconomyEntity> entities = new HashMap<>();

    private String player;
    private double balance;

    @SneakyThrows
    public EconomyEntity(String player) {
        this.player = player;
        entities.put(player, this);

        ResultSet rs = Main.getInstance().getMySQL().executeQuery("SELECT * FROM `economy` WHERE name='" + player + "' LIMIT 1;");
        if (!rs.next()) {
            Main.getInstance().getMySQL().executeUpdate("INSERT INTO `economy` (name) VALUES ('" + player + "')");
            this.balance = 0;
        } else {
            this.balance = rs.getDouble("balance");
        }

    }

    @SneakyThrows
    public boolean withdraw(double amount) {

        if (balance >= amount) {
            balance -= amount;
            Main.getInstance().getMySQL().executeUpdate("INSERT INTO `economy_logs` (name, value, amount) VALUES ('" + player + "', 'withdraw', '" + amount + "')");
            if (Bukkit.getPlayer(player) == null) save();
            return true;
        }

        return false;
    }

    @SneakyThrows
    public void deposit(double amount) {
        balance += amount;
        if (Bukkit.getPlayer(player) == null) save();
        Main.getInstance().getMySQL().executeUpdate("INSERT INTO `economy_logs` (name, value, amount) VALUES ('" + player + "', 'deposit', '" + amount + "')");
    }

    @SneakyThrows
    public void save() {
        Main.getInstance().getMySQL().executeUpdate("UPDATE `economy` SET `balance`='" + balance + "' LIMIT 1;");
    }

    public static EconomyEntity get(String player) {
        EconomyEntity economyEntity = entities.get(player);
        return (economyEntity != null ? economyEntity : new EconomyEntity(player));
    }


}

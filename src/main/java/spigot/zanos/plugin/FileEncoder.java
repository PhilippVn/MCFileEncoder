package spigot.zanos.plugin;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import spigot.zanos.plugin.commands.CommandDecode;
import spigot.zanos.plugin.commands.CommandEncode;
import spigot.zanos.plugin.listeners.PlayerJoinListener;

import java.io.File;
import java.util.Objects;

public class FileEncoder extends JavaPlugin {

    public static final int MAX_ROW_WIDTH = 64; // has to be diviseable by 8
    public static final int MAX_COLUMN_HEIGHT = 50;
    public static final int X_OFFSET = 5; // offset from the player

    @Override
    public void onLoad() {
        Bukkit.getLogger().info(ChatColor.GRAY + "Loaded " + this.getName());
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(ChatColor.GREEN + "Disabled " + this.getName());
        unregisterListeners();
    }

    @Override
    public void onEnable() {
        Bukkit.getLogger().info(ChatColor.RED + "Enabled" + this.getName());

        // init plugin folder if it doesn't exist yet
        File f = new File(FileEncoder.getPlugin(this.getClass()).getDataFolder() + "/");
        if(!f.exists())
            f.mkdir();


        // register Listeners
        registerListeners();
        // register Commands
        Objects.requireNonNull(this.getCommand(CommandEncode.name)).setExecutor(new CommandEncode());
        Objects.requireNonNull(this.getCommand(CommandDecode.name)).setExecutor(new CommandDecode());
    }

    private void unregisterListeners() {
        HandlerList.unregisterAll();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }
}

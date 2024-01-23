package spigot.zanos.plugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Bukkit.broadcastMessage("Welcome! Ready to encode some files " + e.getPlayer().getDisplayName() +  "?");
    }
}

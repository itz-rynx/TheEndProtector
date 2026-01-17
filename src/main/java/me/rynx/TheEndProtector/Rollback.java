package me.rynx.TheEndProtector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

public class Rollback implements Runnable {
	private final Main plugin;
	private final int protectionRadius;
	private final boolean shouldRespawnDragon;
	
	public Rollback(Main plugin, int protectionRadius, boolean shouldRespawnDragon) {
		this.plugin = plugin;
		this.protectionRadius = protectionRadius;
		this.shouldRespawnDragon = shouldRespawnDragon;
	}
	
	@Override
	public void run() {
		try {
			CoreProtectAPI CoreProtect = getCoreProtect();
			if (CoreProtect != null) { // Ensure we have access to the API
				
				// Get the rollback timestamp from file. This was saved during the mob spawn event.
				String mobSpawnTimeString = "";
				try {
					File dataFile = new File(plugin.getDataFolder(), "the_end_protector.dat");
					mobSpawnTimeString = new String(Files.readAllBytes(dataFile.toPath()));
		        } 
		        catch (IOException e) {
		            e.printStackTrace();
		            
		            Bukkit.getLogger().info("Could not read the_end_protector.dat, stopping rollback.");
		            return;
		        }
				
				// Convert the time into an Integer to be able to calculate with it.
				Integer mobSpawnTime = 0;
				try {
					mobSpawnTime = Integer.parseInt(mobSpawnTimeString.trim());
				}
				catch (Exception e) {
					Bukkit.getLogger().info("Could not convert the value in the_end_protector to an Integer, stopping rollback.");
					return;
				}

				// Check that the mobSpawnTime value is a plausible value.
				if (mobSpawnTime < 1577836800) { // 1577836800 = 1 Jan 2020
					Bukkit.getLogger().info("The mob spawn time read from the_end_protector.dat was before 1 Jan 2020, this is probably not right, so stopping rollback.");
					return;
				}
				
				
				// Get the current unix timestamp into an Integer.
				Integer currentTime = (int) Instant.now().getEpochSecond();
				
				// Subtract the current time from the mobSpawnTime
				Integer rollbackSeconds = currentTime - mobSpawnTime;
				
				//Bukkit.getLogger().info("Rolling back " + rollbackSeconds + " seconds.");
				
				// Rollback with configured radius from the middle of The End.
				World theEnd = Bukkit.getServer().getWorld("world_the_end");
				if (theEnd == null) {
					Bukkit.getLogger().info("Could not find The End world, stopping rollback.");
					return;
				}
				CoreProtect.performRollback(rollbackSeconds, null, null, null, null, null, protectionRadius, new Location(theEnd, 0, 80, 0));
				
				// Spawn lại Ender Dragon nếu được bật (chỉ cho vanilla)
				if (shouldRespawnDragon) {
					// Sử dụng Bukkit scheduler để chạy trên main thread sau khi rollback xong
					Bukkit.getScheduler().runTask(plugin, () -> {
						plugin.respawnDragonAfterRollback();
					});
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private CoreProtectAPI getCoreProtect() {
		
		
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");
     
        // Check that CoreProtect is loaded
        if (plugin == null || !(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (CoreProtect.isEnabled() == false) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        //if (CoreProtect.APIVersion() < 6) {
       //     return null;
        //}

        return CoreProtect;
	}
}

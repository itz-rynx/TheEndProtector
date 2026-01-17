package me.rynx.TheEndProtector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;

/**
 * TheEndProtector - Plugin bảo vệ và tự động reset hòn đảo chính trong The End
 * Hỗ trợ cả Vanilla EnderDragon và MythicMobs
 */
public class Main extends JavaPlugin implements Listener {	
	// Thế giới The End
	private World theEnd;
	
	// Debug mode - hiển thị thông tin debug trong console
	private Boolean debugMessages = false;
	
	// Danh sách các scheduled task IDs để kiểm tra người chơi
	private ArrayList<Integer> checkPlayersScheduledTaskIds;
	
	// Số phút không có người chơi trên đảo chính
	private int amountOfMinutesNoPlayersFound = 0;
	
	// ========== Config values ==========
	// Loại mob đang sử dụng: "vanilla" hoặc "mythicmobs"
	private String mobType;
	
	// Tên mob (cho vanilla: "EnderDragon", cho mythicmobs: tên mob trong MythicMobs)
	private String mobName;
	
	// Bán kính bảo vệ đảo chính (blocks từ 0,0)
	private int protectionRadius;
	
	// Thời gian chờ trước khi rollback (giây)
	private int rollbackDelay;

	// Thông báo countdown trước khi rollback (giây)
	private List<Integer> rollbackNotifications;

	// Nội dung thông báo rollback
	private String rollbackNotificationMessage;

	// Thời gian không có người chơi trước khi tự động xóa mob và rollback (phút)
	private int autoRollbackMinutes;
	// Cho phép auto rollback khi không có người chơi (true/false)
	private boolean autoRollbackEnabled;
	
	// Nếu true thì cho phép đặt/phá block trên đảo chính khi mob chưa hồi sinh
	private boolean allowBlockChangesWhenMobDead;
	// Thông điệp khi block action bị chặn (supports legacy color codes and {mob} placeholder)
	private String blockDeniedMessage;
	
	// Tự động hồi sinh Ender Dragon sau khi rollback (chỉ cho vanilla)
	private boolean autoRespawnDragon;
	
	// Cấu hình vanilla
	private boolean vanillaEnabled;
	
	// Cấu hình mythicmobs
	private boolean mythicMobsEnabled;
	
	/**
	 * Khởi động plugin
	 */
	@Override
	public void onEnable() {
		// Load config từ file config.yml
		saveDefaultConfig();
		reloadConfig();
		loadConfig();
		
		// Kiểm tra nếu sử dụng MythicMobs thì cần plugin MythicMobs
		if (mobType.equals("mythicmobs") && !Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
			getLogger().severe("MythicMobs is not loaded! Disabling MythicMobs mode. Using Vanilla mode instead.");
			// Fallback về vanilla nếu MythicMobs không có
			loadVanillaConfig();
		}
		
		// Đăng ký event listeners
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		// Tìm thế giới The End
		theEnd = getTheEnd();
		
		if (theEnd == null) {
			getLogger().severe("Error: Cannot find the The End world! Plugin will not function properly.");
			return;
		}
		
		// Khởi tạo danh sách scheduled tasks
		checkPlayersScheduledTaskIds = new ArrayList<>(); // Sử dụng diamond operator (Java 7+)
		
		// Kiểm tra nếu mob đang sống khi server khởi động, thì bắt đầu vòng lặp kiểm tra người chơi
		if (mobIsAlive()) {
			startPlayersCheckLoop();
		}
		
		getLogger().info("TheEndProtector enabled! Mode: " + mobType + ", Mob: " + mobName);
		getLogger().info("Protection radius: " + protectionRadius + ", Rollback delay: " + rollbackDelay + "s, Auto rollback: " + autoRollbackMinutes + "min");
	}
	
	/**
	 * Load cấu hình từ config.yml
	 */
	private void loadConfig() {
		FileConfiguration config = getConfig();
		
		// Load cấu hình vanilla
		vanillaEnabled = config.getBoolean("vanilla.enabled", true);
		// Vanilla chỉ có EnderDragon trong The End, không cần đọc từ config
		int vanillaProtectionRadius = config.getInt("vanilla.protection-radius", 150);
		int vanillaRollbackDelay = config.getInt("vanilla.rollback-delay", 5);
		List<Integer> vanillaRollbackNotifications = config.getIntegerList("vanilla.rollback-notifications");
		if (vanillaRollbackNotifications.isEmpty()) {
			vanillaRollbackNotifications = Arrays.asList(60, 30, 10, 5); // Default values
		}
		String vanillaRollbackNotificationMessage = config.getString("vanilla.rollback-notification-message", "&cThe End sẽ làm mới trong {seconds} giây!");
		boolean vanillaAllowBlocksWhenDead = config.getBoolean("vanilla.allow-blocks-when-mob-dead", false);
		String vanillaBlockDenyMessage = config.getString("vanilla.block-deny-message", "&cCannot adjust blocks on the main island as the {mob} is not alive.");
		int vanillaAutoRollbackMinutes = config.getInt("vanilla.auto-rollback-minutes", 5);
		boolean vanillaAutoRollbackEnabled = config.getBoolean("vanilla.auto-rollback-enabled", true);
		boolean vanillaAutoRespawnDragon = config.getBoolean("vanilla.auto-respawn-dragon", true);
		
		// Load cấu hình mythicmobs
		mythicMobsEnabled = config.getBoolean("mythicmobs.enabled", false);
		String mythicMobType = config.getString("mythicmobs.type", "ENDER_DRAGON");
		int mythicProtectionRadius = config.getInt("mythicmobs.protection-radius", 150);
		int mythicRollbackDelay = config.getInt("mythicmobs.rollback-delay", 5);
		List<Integer> mythicRollbackNotifications = config.getIntegerList("mythicmobs.rollback-notifications");
		if (mythicRollbackNotifications.isEmpty()) {
			mythicRollbackNotifications = Arrays.asList(60, 30, 10, 5); // Default values
		}
		String mythicRollbackNotificationMessage = config.getString("mythicmobs.rollback-notification-message", "&cThe End sẽ làm mới trong {seconds} giây!");
		boolean mythicAllowBlocksWhenDead = config.getBoolean("mythicmobs.allow-blocks-when-mob-dead", false);
		String mythicBlockDenyMessage = config.getString("mythicmobs.block-deny-message", "&cCannot adjust blocks on the main island as the {mob} is not alive.");
		int mythicAutoRollbackMinutes = config.getInt("mythicmobs.auto-rollback-minutes", 5);
		boolean mythicAutoRollbackEnabled = config.getBoolean("mythicmobs.auto-rollback-enabled", true);
		
		// Chọn loại mob dựa trên enabled flag (ưu tiên mythicmobs nếu cả hai đều enabled)
		if (mythicMobsEnabled) {
			mobType = "mythicmobs";
			// Loại bỏ prefix "MYTHICMOBS:" nếu có
			mobName = mythicMobType.replace("MYTHICMOBS:", "").trim();
			protectionRadius = mythicProtectionRadius;
			rollbackDelay = mythicRollbackDelay;
			rollbackNotifications = mythicRollbackNotifications;
			rollbackNotificationMessage = mythicRollbackNotificationMessage;
			allowBlockChangesWhenMobDead = mythicAllowBlocksWhenDead;
			blockDeniedMessage = mythicBlockDenyMessage;
			autoRollbackMinutes = mythicAutoRollbackMinutes;
			autoRollbackEnabled = mythicAutoRollbackEnabled;
			autoRespawnDragon = false; // Chỉ áp dụng cho vanilla
		} else if (vanillaEnabled) {
			mobType = "vanilla";
			mobName = "EnderDragon"; // Hardcode vì vanilla chỉ có EnderDragon trong The End
			protectionRadius = vanillaProtectionRadius;
			rollbackDelay = vanillaRollbackDelay;
			rollbackNotifications = vanillaRollbackNotifications;
			rollbackNotificationMessage = vanillaRollbackNotificationMessage;
			allowBlockChangesWhenMobDead = vanillaAllowBlocksWhenDead;
			blockDeniedMessage = vanillaBlockDenyMessage;
			autoRollbackMinutes = vanillaAutoRollbackMinutes;
			autoRollbackEnabled = vanillaAutoRollbackEnabled;
			autoRespawnDragon = vanillaAutoRespawnDragon;
		} else {
			// Nếu cả hai đều disabled, mặc định dùng vanilla
			mobType = "vanilla";
			mobName = "EnderDragon"; // Hardcode vì vanilla chỉ có EnderDragon trong The End
			protectionRadius = vanillaProtectionRadius;
			rollbackDelay = vanillaRollbackDelay;
			rollbackNotifications = vanillaRollbackNotifications;
			rollbackNotificationMessage = vanillaRollbackNotificationMessage;
			allowBlockChangesWhenMobDead = vanillaAllowBlocksWhenDead;
			blockDeniedMessage = vanillaBlockDenyMessage;
			autoRollbackMinutes = vanillaAutoRollbackMinutes;
			autoRespawnDragon = vanillaAutoRespawnDragon;
			getLogger().warning("Both vanilla and mythicmobs are disabled! Using vanilla as default.");
		}
	}
	
	/**
	 * Load cấu hình vanilla từ config (dùng khi fallback)
	 */
	private void loadVanillaConfig() {
		FileConfiguration config = getConfig();
		mythicMobsEnabled = false;
		vanillaEnabled = true;
		mobType = "vanilla";
		mobName = "EnderDragon"; // Hardcode vì vanilla chỉ có EnderDragon trong The End
		protectionRadius = config.getInt("vanilla.protection-radius", 150);
		rollbackDelay = config.getInt("vanilla.rollback-delay", 5);
		rollbackNotifications = config.getIntegerList("vanilla.rollback-notifications");
		if (rollbackNotifications.isEmpty()) {
			rollbackNotifications = Arrays.asList(60, 30, 10, 5); // Default values
		}
		rollbackNotificationMessage = config.getString("vanilla.rollback-notification-message", "&cThe End sẽ làm mới trong {seconds} giây!");
		autoRollbackMinutes = config.getInt("vanilla.auto-rollback-minutes", 5);
		autoRespawnDragon = config.getBoolean("vanilla.auto-respawn-dragon", true);
	}
	
	/**
	 * Xử lý các lệnh của plugin
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Chỉ người chơi mới có thể dùng lệnh
		if (!(sender instanceof Player)) {
			sender.sendMessage(Component.text("This command can only be used by players.").color(NamedTextColor.RED));
			return true;
		}

		Player p = (Player) sender;

		// Xử lý lệnh chính /theendprotector
		if (label.equalsIgnoreCase("theendprotector") || label.equalsIgnoreCase("tep") ||
			label.equalsIgnoreCase("endprotector") || label.equalsIgnoreCase("theend")) {

			// Kiểm tra quyền
			if (!p.isOp()) {
				p.sendMessage(Component.text("You have no permissions to use this command.").color(NamedTextColor.RED));
				return true;
			}

			// Nếu không có arguments, hiển thị help
			if (args.length == 0) {
				showHelp(p);
				return true;
			}

			String subCommand = args[0].toLowerCase();

			switch (subCommand) {
				case "help":
				case "h":
					showHelp(p);
					break;

				case "reload":
				case "r":
					reloadConfig();
					loadConfig();
					p.sendMessage(Component.text("Configuration reloaded successfully!").color(NamedTextColor.GREEN));
					break;

				case "status":
				case "s":
					showStatus(p);
					break;

				case "test":
				case "t":
					if (!mobIsAlive()) {
						p.sendMessage(Component.text("No mob is currently alive. Spawn a mob first to test rollback.").color(NamedTextColor.RED));
						return true;
					}
					p.sendMessage(Component.text("Rollback will start in " + rollbackDelay + " seconds.").color(NamedTextColor.YELLOW));
					performRollback();
					break;

				case "info":
				case "i":
					showInfo(p);
					break;

				default:
					p.sendMessage(Component.text("Unknown subcommand. Use /theendprotector help for available commands.").color(NamedTextColor.RED));
					break;
			}
			return true;
		}

		// Xử lý các lệnh cũ (deprecated)
		if (label.equalsIgnoreCase("rollbacktest")) {
			if (!p.isOp()) {
				p.sendMessage(Component.text("You have no permissions to use this command.").color(NamedTextColor.RED));
				return true;
			}
			p.sendMessage(Component.text("⚠️ This command is deprecated. Use /theendprotector test instead.").color(NamedTextColor.YELLOW));
			if (!mobIsAlive()) {
				p.sendMessage(Component.text("No mob is currently alive. Spawn a mob first to test rollback.").color(NamedTextColor.RED));
				return true;
			}
			p.sendMessage(Component.text("Rollback will start in " + rollbackDelay + " seconds.").color(NamedTextColor.YELLOW));
			performRollback();
		}

		if (label.equalsIgnoreCase("reloadconfig")) {
			if (!p.isOp()) {
				p.sendMessage(Component.text("You have no permissions to use this command.").color(NamedTextColor.RED));
				return true;
			}
			p.sendMessage(Component.text("⚠️ This command is deprecated. Use /theendprotector reload instead.").color(NamedTextColor.YELLOW));
			reloadConfig();
			loadConfig();
			p.sendMessage(Component.text("Configuration reloaded successfully!").color(NamedTextColor.GREEN));
		}

		return true;
	}
	
	/**
	 * Event handler khi người chơi phá block
	 */
	@EventHandler
	public void onPlace(BlockBreakEvent event) {	
		Player p = event.getPlayer();
		
		// Kiểm tra xem có nên cancel event này không
		if (shouldBlockEventBeCancelled(p, event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Event handler khi người chơi đặt block
	 */
	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		
		// Kiểm tra xem có nên cancel event này không
		if (shouldBlockEventBeCancelled(p, event.getBlock())) {
			event.setCancelled(true);
		}
	}

	/**
	 * Kiểm tra xem có nên cancel block event không
	 * @param p Người chơi thực hiện hành động
	 * @param block Block bị ảnh hưởng
	 * @return true nếu nên cancel, false nếu không
	 */
	private boolean shouldBlockEventBeCancelled(Player p, Block block) {
		// Nếu block event nằm ngoài đảo chính, không block
		if (!locationIsMainIsland(block.getLocation())) {
			debugMessage("BlockEvent -> shouldBlockEventBeCancelled: Event was not on the main island so not cancelled.");
			return false;
		}
		
		// Nếu mob còn sống, cho phép chỉnh sửa block
		if (mobIsAlive()) {
			debugMessage("BlockEvent -> shouldBlockEventBeCancelled: Mob is alive, so not cancelled.");
			return false;
		}
		
		// Khi đặt End Crystal cuối cùng để spawn mob, có một BlockPlaceEvent đặt bedrock tại 3,58,1
		// được trigger tự động. Cho phép điều này xảy ra.
		if (block.getType() == Material.BEDROCK) {
			return false;
		}
		
		// Nếu mob không còn sống và block nằm trong đảo chính, kiểm tra cấu hình cho phép/thực hiện block
		debugMessage("BlockEvent -> shouldBlockEventBeCancelled: Mob is not alive.");
		if (allowBlockChangesWhenMobDead) {
			debugMessage("BlockEvent -> allowBlockChangesWhenMobDead is true, allowing block action.");
			return false;
		}

		debugMessage("BlockEvent -> shouldBlockEventBeCancelled: BlockEvent cancelled.");
		// Build deny message (supports {mob} placeholder and legacy color codes)
		String msgTemplate = (blockDeniedMessage != null && !blockDeniedMessage.isEmpty())
				? blockDeniedMessage
				: "&cCannot adjust blocks on the main island as the {mob} is not alive.";
		String formatted = msgTemplate.replace("{mob}", mobName);
		Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(formatted);
		p.sendMessage(message);
		return true;
	}
	
	/**
	 * Event handler khi người chơi tương tác với block
	 * Ngăn chặn đặt End Crystal lên Obsidian (chỉ cho phép đặt lên Bedrock)
	 */
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		// Nếu tương tác nằm ngoài đảo chính, không xử lý
		if (!locationIsMainIsland(event.getPlayer().getLocation())) {
			debugMessage("onPlayerInteract: Event was not on the main island so not cancelled.");
			return;
		}
				
		// Chỉ xử lý khi click chuột phải vào block
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
	    
		// Chỉ xử lý khi click vào Obsidian
		if (event.getClickedBlock().getType() != Material.OBSIDIAN) {
			return;
		}
	    
		// Chỉ xử lý khi đang cầm End Crystal
		if (event.getMaterial() != Material.END_CRYSTAL) {
			return;
		}
        
		// Chạy task sau 1 tick để đảm bảo End Crystal đã được spawn
		Bukkit.getScheduler().runTask(this, () -> {
			// Lấy tất cả entities gần người chơi (bán kính 4 blocks)
			List<Entity> entities = event.getPlayer().getNearbyEntities(4, 4, 4);

			// Duyệt qua các entities
			for (Entity entity : entities) {
				// Chỉ xử lý End Crystal - sử dụng instanceof thay vì EntityType enum (tương thích 1.21)
				if (!(entity instanceof EnderCrystal)) {
					continue;
				}
				
				EnderCrystal crystal = (EnderCrystal) entity;
				// Lấy block bên dưới End Crystal
				Block belowCrystal = crystal.getLocation().getBlock().getRelative(BlockFace.DOWN);

				// Nếu End Crystal được đặt trên Obsidian (block được click), xóa nó
				if (event.getClickedBlock().equals(belowCrystal)) {
					// Paper 1.21 API - remove() vẫn hoạt động, có thể dùng remove(Entity.RemovalReason.CUSTOM) nếu cần
					entity.remove();
					break;
				}
			}
		});
	}
	
	/**
	 * Event handler khi mob chết
	 * Tự động rollback đảo chính khi mob chết
	 */
	@EventHandler
	public void onMobDeath(EntityDeathEvent e){
		// Xử lý cho Vanilla EnderDragon
		if (mobType.equals("vanilla") && e.getEntity() instanceof EnderDragon) {
			debugMessage("Vanilla EnderDragon has died");
			
			// Dừng vòng lặp kiểm tra người chơi
			cancelAllCheckPlayersScheduledTasks();
			
			// Rollback đảo chính
			performRollback();
			return;
		}
		
		// Xử lý cho MythicMobs
		if (mobType.equals("mythicmobs")) {
			// Kiểm tra xem entity có phải là MythicMob với tên đã cấu hình không
			ActiveMob activeMob = MythicBukkit.inst().getMobManager().getActiveMob(e.getEntity().getUniqueId()).orElse(null);
			if (activeMob != null && activeMob.getType().getInternalName().equals(mobName)) {
				debugMessage("MythicMob " + mobName + " has died");
				
				// Dừng vòng lặp kiểm tra người chơi
				cancelAllCheckPlayersScheduledTasks();
				
				// Rollback đảo chính
				performRollback();
			}
		}
	}
	
	/**
	 * Event handler khi mob spawn
	 * Lưu timestamp và bắt đầu vòng lặp kiểm tra người chơi
	 */
	@EventHandler
	public void onSpawn(CreatureSpawnEvent event){
		// Chỉ xử lý trong The End
		if (event.getLocation().getWorld().getEnvironment() != Environment.THE_END) {
			return;
		}
		
		// Xử lý cho Vanilla EnderDragon
		if (mobType.equals("vanilla") && event.getEntity() instanceof EnderDragon) {
			debugMessage("Vanilla EnderDragon has been spawned.");
			
			// Lưu timestamp để rollback (trừ 1 phút để rollback về trạng thái sạch)
			saveRollbackTimestamp();
			
			// Bắt đầu vòng lặp kiểm tra người chơi
			startPlayersCheckLoop();
			return;
		}
		
		// Xử lý cho MythicMobs
		if (mobType.equals("mythicmobs")) {
			// Kiểm tra xem entity có phải là MythicMob với tên đã cấu hình không
			ActiveMob activeMob = MythicBukkit.inst().getMobManager().getActiveMob(event.getEntity().getUniqueId()).orElse(null);
			if (activeMob != null && activeMob.getType().getInternalName().equals(mobName)) {
				debugMessage("MythicMob " + mobName + " has been spawned.");
				
				// Lưu timestamp để rollback (trừ 1 phút để rollback về trạng thái sạch)
				saveRollbackTimestamp();
				
				// Bắt đầu vòng lặp kiểm tra người chơi
				startPlayersCheckLoop();
			}
		}
	}
	
	/**
	 * Lưu timestamp vào file để rollback sau này
	 */
	private void saveRollbackTimestamp() {
		// Lưu thời gian hiện tại trừ 1 phút (để rollback về trạng thái sạch)
		String unix_timestamp = String.valueOf(Instant.now().getEpochSecond() - 60);
		try {
			File dataFile = new File(getDataFolder(), "the_end_protector.dat");
			// Tạo thư mục nếu chưa tồn tại
			if (!getDataFolder().exists()) {
				getDataFolder().mkdirs();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));
			writer.write(unix_timestamp);
			writer.close();
		}
		catch (Exception ex) {
			getLogger().warning("Could not save the current timestamp to file.");
		}
	}
	
	/**
	 * Kiểm tra xem vị trí có nằm trong đảo chính không
	 * @param l Vị trí cần kiểm tra
	 * @return true nếu nằm trong đảo chính, false nếu không
	 */
	private boolean locationIsMainIsland(Location l) {
		// Kiểm tra xem có phải trong The End không
		if (l.getWorld().getEnvironment() != Environment.THE_END) {
			return false;
		}
		
		// Kiểm tra tọa độ X và Z có nằm trong bán kính bảo vệ không
		return l.getX() < protectionRadius && l.getX() > -protectionRadius && 
			   l.getZ() < protectionRadius && l.getZ() > -protectionRadius;
	}
	
	/**
	 * Kiểm tra xem mob có còn sống không
	 * @return true nếu mob còn sống, false nếu không
	 */
	private boolean mobIsAlive(){
		// Xử lý cho Vanilla EnderDragon
		if (mobType.equals("vanilla")) {
			List<LivingEntity> livingEntities = theEnd.getLivingEntities();
			for (LivingEntity entity : livingEntities) {
				if (entity instanceof EnderDragon) {
					return true;
				}
			}
			return false;
		}
		
		// Xử lý cho MythicMobs
		if (mobType.equals("mythicmobs")) {
			Collection<ActiveMob> activeMobs = MythicBukkit.inst().getMobManager().getActiveMobs();
			for (ActiveMob activeMob : activeMobs) {
				if (activeMob.getType().getInternalName().equals(mobName)) {
					// Kiểm tra xem mob có ở trong The End không
					if (activeMob.getEntity().getBukkitEntity().getWorld().getEnvironment() == Environment.THE_END) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Xóa mob (không kích hoạt rollback)
	 * @return Số lượng mob đã xóa
	 */
	private Integer removeMob(){
		Integer removedMobs = 0;
		
		// Xử lý cho Vanilla EnderDragon
		if (mobType.equals("vanilla")) {
			List<LivingEntity> livingEntities = theEnd.getLivingEntities();
			for (LivingEntity entity : livingEntities) {
				if (entity instanceof EnderDragon) {
					// Paper 1.21 API - remove() vẫn hoạt động
					entity.remove();
					removedMobs++;
				}
			}
			return removedMobs;
		}
		
		// Xử lý cho MythicMobs
		if (mobType.equals("mythicmobs")) {
			Collection<ActiveMob> activeMobs = MythicBukkit.inst().getMobManager().getActiveMobs();
			for (ActiveMob activeMob : activeMobs) {
				if (activeMob.getType().getInternalName().equals(mobName)) {
					// Kiểm tra xem mob có ở trong The End không
					if (activeMob.getEntity().getBukkitEntity().getWorld().getEnvironment() == Environment.THE_END) {
						// Paper 1.21 API - remove() vẫn hoạt động
						activeMob.getEntity().getBukkitEntity().remove();
						removedMobs++;
					}
				}
			}
		}
		
		return removedMobs;
	}
	
	/**
	 * Giết mob (kích hoạt rollback)
	 * @return Số lượng mob đã giết
	 */
	private Integer killMob(){
		Integer killedMobs = 0;
		
		// Xử lý cho Vanilla EnderDragon
		if (mobType.equals("vanilla")) {
			List<LivingEntity> livingEntities = theEnd.getLivingEntities();
			for (LivingEntity entity : livingEntities) {
				if (entity instanceof EnderDragon) {
					// Paper 1.21 API - setHealth(0) vẫn hoạt động để giết mob
					entity.setHealth(0);
					killedMobs++;
				}
			}
			return killedMobs;
		}
		
		// Xử lý cho MythicMobs
		if (mobType.equals("mythicmobs")) {
			Collection<ActiveMob> activeMobs = MythicBukkit.inst().getMobManager().getActiveMobs();
			for (ActiveMob activeMob : activeMobs) {
				if (activeMob.getType().getInternalName().equals(mobName)) {
					// Kiểm tra xem mob có ở trong The End không
					if (activeMob.getEntity().getBukkitEntity().getWorld().getEnvironment() == Environment.THE_END) {
						LivingEntity entity = (LivingEntity) activeMob.getEntity().getBukkitEntity();
						// Paper 1.21 API - setHealth(0) vẫn hoạt động để giết mob
						entity.setHealth(0);
						killedMobs++;
					}
				}
			}
		}
		
		return killedMobs;
	}
	
	/**
	 * Spawn mob tại vị trí chỉ định
	 * @param location Vị trí spawn
	 * @return true nếu spawn thành công, false nếu thất bại
	 */
	private boolean spawnMob(Location location) {
		// Xử lý cho Vanilla EnderDragon
		if (mobType.equals("vanilla")) {
			try {
				EnderDragon dragon = theEnd.spawn(location, EnderDragon.class);
				dragon.setAI(true); // Bật AI cho dragon
				return true;
			} catch (Exception e) {
				getLogger().warning("Failed to spawn Vanilla EnderDragon");
				getLogger().warning("Error: " + e.getMessage());
				return false;
			}
		}
		
		// Xử lý cho MythicMobs
		if (mobType.equals("mythicmobs")) {
			try {
				ActiveMob mob = MythicBukkit.inst().getMobManager().spawnMob(mobName, location);
				return mob != null;
			} catch (Exception e) {
				getLogger().warning("Failed to spawn MythicMob: " + mobName);
				getLogger().warning("Error: " + e.getMessage());
				return false;
			}
		}
		
		return false;
	}

	/**
	 * Gửi debug message nếu debug mode được bật
	 * @param msg Thông điệp debug
	 */
	private void debugMessage(String msg) {
		if (!debugMessages) {
			return;
		}
		
		getLogger().info(msg);
	}
	
	/**
	 * Bắt đầu vòng lặp kiểm tra người chơi mỗi phút
	 * Được gọi khi mob spawn
	 * Vòng lặp sẽ dừng khi mob bị giết
	 */
	private void startPlayersCheckLoop() {
		// If auto-rollback is disabled in config, do not start the players check loop.
		if (!autoRollbackEnabled) {
			debugMessage("Auto rollback is disabled; players check loop will not start.");
			return;
		}
		// Tạo scheduled task chạy mỗi phút (1200 ticks = 60 giây)
		// Sử dụng lambda expression (Java 21)
		Integer checkPlayersScheduledTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			// Kiểm tra xem có người chơi trên đảo chính không
			if (thereAreNoPlayersOnTheMainIsland()) {
				// Tăng counter nếu không có người chơi
				amountOfMinutesNoPlayersFound++;
				debugMessage("There is no player on the main island for " + amountOfMinutesNoPlayersFound + " minutes");
			} else {
				// Reset counter nếu có người chơi
				debugMessage("There is a player on the main island.");
				amountOfMinutesNoPlayersFound = 0;
			}
			
			// Nếu không có người chơi trong thời gian cấu hình, tự động xóa mob và rollback
			if (amountOfMinutesNoPlayersFound >= autoRollbackMinutes) {
				debugMessage("No players on main island for " + autoRollbackMinutes + " minutes, removing mob and rolling back.");
				
				// Xóa mob
				removeMob();
				
				// Hàm removeMob không trigger event mob chết, nên phải gọi rollback trực tiếp
				performRollback();
				
				// Dừng vòng lặp
				cancelAllCheckPlayersScheduledTasks();
			}
		}, 1200, 1200); // 20 ticks = 1 giây, 1200 ticks = 1 phút
		
		// Thêm task ID vào danh sách để quản lý
		checkPlayersScheduledTaskIds.add(checkPlayersScheduledTaskId);
	}
	
	/**
	 * Hủy tất cả scheduled tasks kiểm tra người chơi
	 */
	private void cancelAllCheckPlayersScheduledTasks() {
		debugMessage("Amount of timers " + checkPlayersScheduledTaskIds.size());
		
		// Hủy từng task
		for (Integer i : checkPlayersScheduledTaskIds) {
			debugMessage("Stopped scheduled task id " + i);
			Bukkit.getScheduler().cancelTask(i);
		}
		
		// Xóa danh sách
		checkPlayersScheduledTaskIds.clear();
	}
	
	/**
	 * Kiểm tra xem có người chơi nào trên đảo chính không
	 * @return true nếu không có người chơi, false nếu có
	 */
	private Boolean thereAreNoPlayersOnTheMainIsland() {
		// Duyệt qua tất cả người chơi online
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			// Nếu có người chơi trên đảo chính, return false
			if (locationIsMainIsland(player.getLocation())) {
				return false;
			}
		}
		
		// Không có người chơi nào trên đảo chính
		return true;
	}
	
	/**
	 * Gửi thông báo đến tất cả người chơi trong The End
	 * @param message Thông điệp cần gửi
	 */
	private void broadcastToEndPlayers(Component message) {
		// Duyệt qua tất cả người chơi online
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			// Chỉ gửi cho người chơi đang ở trong The End
			if (player.getWorld().getEnvironment() == Environment.THE_END) {
				player.sendMessage(message);
			}
		}
	}

	/**
	 * Thực hiện rollback đảo chính
	 * Sử dụng CoreProtect để rollback về thời điểm trước khi mob spawn
	 */
	private void performRollback() {
		debugMessage("Rolling back the main island");

		// Hiển thị thông báo countdown trước khi rollback
		for (Integer secondsLeft : rollbackNotifications) {
			// Chỉ hiển thị thông báo nếu thời gian còn lại nhỏ hơn hoặc bằng rollback delay
			if (secondsLeft <= rollbackDelay && !rollbackNotificationMessage.isEmpty()) {
				long delayTicks = (rollbackDelay - secondsLeft) * 20L; // Chuyển đổi giây sang ticks
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
					String formattedMessage = rollbackNotificationMessage.replace("{seconds}", secondsLeft.toString());
					Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(formattedMessage);
					broadcastToEndPlayers(message);
					debugMessage("Sent rollback countdown notification: " + secondsLeft + " seconds remaining");
				}, delayTicks);
			}
		}

		// Chờ một khoảng thời gian (theo config) trước khi rollback
		// Sử dụng lambda expression (Java 21)
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
			// Chạy rollback trong thread riêng để không block main thread
			Runnable runnable = new Rollback(Main.this, protectionRadius, autoRespawnDragon && mobType.equals("vanilla"));
			Thread thread = new Thread(runnable);
			thread.start();
		}, rollbackDelay * 20L); // Chuyển đổi giây sang ticks (1 giây = 20 ticks)
	}
	
	/**
	 * Hồi sinh Ender Dragon sau khi rollback xong
	 * Được gọi từ Rollback class sau khi rollback hoàn tất
	 */
	public void respawnDragonAfterRollback() {
		if (!mobType.equals("vanilla")) {
			return; // Chỉ áp dụng cho vanilla
		}
		
		debugMessage("Respawning Ender Dragon after rollback");
		
		// Spawn Ender Dragon tại vị trí 0, 80, 0 trong The End
		Location spawnLocation = new Location(theEnd, 0, 80, 0);
		if (spawnMob(spawnLocation)) {
			// Lưu timestamp để rollback sau này (trừ 1 phút để rollback về trạng thái sạch)
			saveRollbackTimestamp();
			// Bắt đầu vòng lặp kiểm tra người chơi
			startPlayersCheckLoop();
			getLogger().info("Ender Dragon has been respawned after rollback.");
		} else {
			getLogger().warning("Failed to respawn Ender Dragon after rollback.");
		}
	}
	
	/**
	 * Tìm và trả về thế giới The End
	 * @return World The End, hoặc null nếu không tìm thấy
	 */
	private World getTheEnd() {
		// Duyệt qua tất cả các thế giới
	    for (World w: Bukkit.getServer().getWorlds()) {
	        // Kiểm tra xem có phải The End không
	        if(w.getEnvironment().equals(World.Environment.THE_END)) {
	            return w;
	        }
	    }

	    return null;
	}

	/**
	 * Hiển thị help menu cho người chơi
	 * @param player Người chơi cần hiển thị help
	 */
	private void showHelp(Player player) {
		player.sendMessage(Component.text("=== TheEndProtector Commands ===").color(NamedTextColor.GOLD));

		player.sendMessage(Component.text("/theendprotector help").color(NamedTextColor.AQUA)
			.append(Component.text(" - Show this help menu").color(NamedTextColor.WHITE)));
		player.sendMessage(Component.text("/theendprotector reload").color(NamedTextColor.AQUA)
			.append(Component.text(" - Reload configuration").color(NamedTextColor.WHITE)));
		player.sendMessage(Component.text("/theendprotector status").color(NamedTextColor.AQUA)
			.append(Component.text(" - Show current plugin status").color(NamedTextColor.WHITE)));
		player.sendMessage(Component.text("/theendprotector test").color(NamedTextColor.AQUA)
			.append(Component.text(" - Test rollback functionality").color(NamedTextColor.WHITE)));
		player.sendMessage(Component.text("/theendprotector info").color(NamedTextColor.AQUA)
			.append(Component.text(" - Show plugin information").color(NamedTextColor.WHITE)));

		player.sendMessage(Component.text("\nAliases: ").color(NamedTextColor.GRAY)
			.append(Component.text("/tep, /endprotector, /theend").color(NamedTextColor.YELLOW)));
	}

	/**
	 * Hiển thị trạng thái hiện tại của plugin
	 * @param player Người chơi cần hiển thị status
	 */
	private void showStatus(Player player) {
		player.sendMessage(Component.text("=== TheEndProtector Status ===").color(NamedTextColor.GOLD));

		// Mode information
		String modeText = mobType.equals("vanilla") ? "Vanilla EnderDragon" : "MythicMobs (" + mobName + ")";
		player.sendMessage(Component.text("Mode: ").color(NamedTextColor.AQUA)
			.append(Component.text(modeText).color(NamedTextColor.WHITE)));

		// Mob status
		boolean mobAlive = mobIsAlive();
		NamedTextColor mobColor = mobAlive ? NamedTextColor.GREEN : NamedTextColor.RED;
		String mobStatus = mobAlive ? "Alive" : "Dead/Not Present";
		player.sendMessage(Component.text("Mob Status: ").color(NamedTextColor.AQUA)
			.append(Component.text(mobStatus).color(mobColor)));

		// Protection status
		String protectionStatus = mobAlive ? "Disabled (Mob Alive)" : "Enabled (Mob Dead)";
		NamedTextColor protectionColor = mobAlive ? NamedTextColor.GREEN : NamedTextColor.YELLOW;
		player.sendMessage(Component.text("Island Protection: ").color(NamedTextColor.AQUA)
			.append(Component.text(protectionStatus).color(protectionColor)));

		// Protection radius
		player.sendMessage(Component.text("Protection Radius: ").color(NamedTextColor.AQUA)
			.append(Component.text(protectionRadius + " blocks").color(NamedTextColor.WHITE)));

		// Player count on island
		int playersOnIsland = 0;
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if (locationIsMainIsland(onlinePlayer.getLocation())) {
				playersOnIsland++;
			}
		}
		player.sendMessage(Component.text("Players on Main Island: ").color(NamedTextColor.AQUA)
			.append(Component.text(String.valueOf(playersOnIsland)).color(NamedTextColor.WHITE)));

		// Auto rollback status
		if (mobAlive && amountOfMinutesNoPlayersFound > 0) {
			player.sendMessage(Component.text("Auto Rollback: ").color(NamedTextColor.AQUA)
				.append(Component.text("Will trigger in " + (autoRollbackMinutes - amountOfMinutesNoPlayersFound) + " minutes").color(NamedTextColor.YELLOW)));
		}
	}

	/**
	 * Hiển thị thông tin plugin
	 * @param player Người chơi cần hiển thị info
	 */
	private void showInfo(Player player) {
		player.sendMessage(Component.text("=== TheEndProtector Info ===").color(NamedTextColor.GOLD));

		player.sendMessage(Component.text("Version: ").color(NamedTextColor.AQUA)
			.append(Component.text(getPluginMeta().getVersion()).color(NamedTextColor.WHITE)));
		player.sendMessage(Component.text("Author: ").color(NamedTextColor.AQUA)
			.append(Component.text(getPluginMeta().getAuthors().get(0)).color(NamedTextColor.WHITE)));
		player.sendMessage(Component.text("API Version: ").color(NamedTextColor.AQUA)
			.append(Component.text("1.21+").color(NamedTextColor.WHITE)));

		// Dependencies status
		player.sendMessage(Component.text("CoreProtect: ").color(NamedTextColor.AQUA)
			.append(Component.text(Bukkit.getPluginManager().isPluginEnabled("CoreProtect") ? "✓ Installed" : "✗ Not Found").color(
				Bukkit.getPluginManager().isPluginEnabled("CoreProtect") ? NamedTextColor.GREEN : NamedTextColor.RED)));

		player.sendMessage(Component.text("MythicMobs: ").color(NamedTextColor.AQUA)
			.append(Component.text(Bukkit.getPluginManager().isPluginEnabled("MythicMobs") ? "✓ Installed" : "✗ Not Found").color(
				Bukkit.getPluginManager().isPluginEnabled("MythicMobs") ? NamedTextColor.GREEN : NamedTextColor.YELLOW)));

		player.sendMessage(Component.text("\nFor more information, visit the plugin documentation.").color(NamedTextColor.GRAY));
	}
}

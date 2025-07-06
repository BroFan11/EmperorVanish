package me.brofan11.emperorVanish

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot

import java.io.File
import java.util.*
import java.util.regex.Pattern

class EmperorVanish : JavaPlugin(), Listener {
    private val vanishedPlayers = mutableSetOf<UUID>()
    private lateinit var messages: YamlConfiguration
    private val hexPattern = Pattern.compile("<#([A-Fa-f0-9]{6})>")

    override fun onEnable() {
        // Save default configurations
        saveDefaultConfig()
        saveResource("messages.yml", false)
        
        // Load messages
        messages = YamlConfiguration.loadConfiguration(File(dataFolder, "messages.yml"))
        
        // Register events
        server.pluginManager.registerEvents(this, this)
        
        // Start scoreboard update task
        if (config.getBoolean("scoreboard.enabled", true)) {
            startScoreboardTask()
        }
        
        logger.info("EmperorVanish enabled successfully!")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name.equals("emperorvanish", ignoreCase = true)) {
            if (sender !is Player) {
                sender.sendMessage(colorize(messages.getString("prefix", "") + "&cOnly players can use this command!"))
                return true
            }

            if (!sender.hasPermission("emperorvanish.use")) {
                sender.sendMessage(colorize(messages.getString("prefix", "") + (messages.getString("no-permission") ?: "&cNo permission!")))
                return true
            }

            toggleVanish(sender)
            return true
        }
        return false
    }

    private fun toggleVanish(player: Player) {
        if (player.uniqueId in vanishedPlayers) {
            vanishedPlayers.remove(player.uniqueId)
            player.sendMessage(colorize(messages.getString("prefix", "") + (messages.getString("vanish-disabled") ?: "&cYou are now visible!")))
            showPlayer(player)
        } else {
            vanishedPlayers.add(player.uniqueId)
            player.sendMessage(colorize(messages.getString("prefix", "") + (messages.getString("vanish-enabled") ?: "&aYou are now vanished!")))
            hidePlayer(player)
        }
        updateScoreboards()
    }

    private fun hidePlayer(player: Player) {
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.hasPermission("emperorvanish.see")) {
                onlinePlayer.hidePlayer(this, player)
            }
        }
    }

    private fun showPlayer(player: Player) {
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(this, player)
        }
    }

    private fun startScoreboardTask() {
        val plugin = this
        server.scheduler.runTaskTimer(plugin, Runnable {
            updateScoreboards()
        }, 0L, config.getLong("scoreboard.update-interval", 20L))
    }

    private fun updateScoreboards() {
        for (player in Bukkit.getOnlinePlayers()) {
            updateScoreboard(player)
        }
    }

    private fun updateScoreboard(player: Player) {
        val scoreboardManager = Bukkit.getScoreboardManager()
        val scoreboard = scoreboardManager.newScoreboard
        val objective = scoreboard.registerNewObjective(
            "vanish",
            "dummy",
            Component.text(colorize(messages.getString("scoreboard.title", "&6&lEMPEROR &e&lVANISH")))
        )
        objective.displaySlot = DisplaySlot.SIDEBAR

        val lines = messages.getStringList("scoreboard.lines").ifEmpty {
            listOf(
                "&7&m----------------------",
                "&fOnline Staff:",
                "{staff_list}",
                "",
                "&fVanished Players:",
                "{vanished_list}",
                "",
                "&fOnline Players: &e{online_players}",
                "&7&m----------------------"
            )
        }
        var score = lines.size

        for (line in lines) {
            var processedLine = line
            
            // Replace placeholders
            processedLine = processedLine
                .replace("{staff_list}", getStaffList())
                .replace("{vanished_list}", getVanishedList())
                .replace("{online_players}", Bukkit.getOnlinePlayers().size.toString())
            
            // Apply internal placeholders first
            processedLine = applyInternalPlaceholders(player, processedLine)
            
            // Apply PlaceholderAPI placeholders if available
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                processedLine = PlaceholderAPI.setPlaceholders(player, processedLine)
            }
            
            scoreboard.getObjective("vanish")?.getScore(colorize(processedLine))?.score = score--
        }

        player.scoreboard = scoreboard
    }

    private fun getStaffList(): String {
        val staffPlayers = Bukkit.getOnlinePlayers()
            .filter { it.hasPermission(config.getString("staff.permission") ?: "emperorvanish.see") }
            .map { it.name }
        
        return if (staffPlayers.isEmpty()) {
            messages.getString("formats.no-players") ?: "&7None"
        } else {
            staffPlayers.joinToString(
                separator = messages.getString("formats.list-separator") ?: "&7, ",
                transform = { name ->
                    val format = messages.getString("formats.staff-format") ?: "&e{player}"
                    format.replace("{player}", name)
                }
            )
        }
    }

    private fun getVanishedList(): String {
        val vanishedNames = vanishedPlayers
            .mapNotNull { Bukkit.getPlayer(it)?.name }
        
        return if (vanishedNames.isEmpty()) {
            messages.getString("formats.no-players") ?: "&7None"
        } else {
            vanishedNames.joinToString(
                separator = messages.getString("formats.list-separator") ?: "&7, ",
                transform = { name ->
                    val format = messages.getString("formats.vanished-format") ?: "&7{player}"
                    format.replace("{player}", name)
                }
            )
        }
    }

    private fun applyInternalPlaceholders(player: Player, text: String): String {
        var result = text
        result = result.replace("{player}", player.name)
        result = result.replace("{player_uuid}", player.uniqueId.toString())
        result = result.replace("{player_health}", player.health.toInt().toString())
        result = result.replace("{player_max_health}", player.maxHealth.toInt().toString())
        result = result.replace("{player_food}", player.foodLevel.toString())
        result = result.replace("{player_exp}", player.exp.toInt().toString())
        result = result.replace("{player_level}", player.level.toString())
        result = result.replace("{player_gamemode}", player.gameMode.name.lowercase())
        result = result.replace("{player_world}", player.world.name)
        result = result.replace("{player_x}", player.location.blockX.toString())
        result = result.replace("{player_y}", player.location.blockY.toString())
        result = result.replace("{player_z}", player.location.blockZ.toString())
        result = result.replace("{server_online}", Bukkit.getOnlinePlayers().size.toString())
        result = result.replace("{server_max_online}", Bukkit.getMaxPlayers().toString())
        result = result.replace("{server_tps}", "20.0") // Placeholder, could be enhanced with actual TPS
        result = result.replace("{server_time}", System.currentTimeMillis().toString())
        result = result.replace("{staff_count}", Bukkit.getOnlinePlayers().count { it.hasPermission("emperorvanish.see") }.toString())
        result = result.replace("{vanished_count}", vanishedPlayers.size.toString())
        return result
    }

    private fun colorize(text: String?): String {
        if (text == null) return ""
        
        var result = text!!
        val matcher = hexPattern.matcher(result)
        while (matcher.find()) {
            val color = matcher.group(1)
            result = result.replace("<#$color>", "§x§${color[0]}§${color[1]}§${color[2]}§${color[3]}§${color[4]}§${color[5]}")
        }
        
        return result.replace("&", "§")
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!event.player.hasPermission("emperorvanish.see")) {
            vanishedPlayers.forEach { uuid ->
                Bukkit.getPlayer(uuid)?.let { vanishedPlayer ->
                    event.player.hidePlayer(this, vanishedPlayer)
                }
            }
        }
        
        // Update scoreboards for all players
        updateScoreboards()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Hide quit message if player is vanished
        if (event.player.uniqueId in vanishedPlayers && config.getBoolean("vanish.hide-messages", true)) {
            event.quitMessage = Component.empty().toString()
        }
        
        // Update scoreboards for remaining players
        updateScoreboards()
    }

    override fun onDisable() {
        // Show all vanished players before disabling
        vanishedPlayers.forEach { uuid ->
            Bukkit.getPlayer(uuid)?.let { player ->
                showPlayer(player)
            }
        }
        logger.info("EmperorVanish disabled successfully!")
    }
}

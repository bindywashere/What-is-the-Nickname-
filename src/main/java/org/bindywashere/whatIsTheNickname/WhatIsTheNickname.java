package org.bindywashere.whatIsTheNickname;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.HashSet;
import java.util.Set;

public class WhatIsTheNickname extends JavaPlugin implements Listener {
    private boolean hideAll = false;
    private final Set<String> visiblePlayers = new HashSet<>();
    private String language = "ru";
    private Team hiddenTeam;
    private String nameFormat;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        language = config.getString("language", "ru");
        nameFormat = config.getString("nameFormat", "{NAME}");

        if (language == "ru") {
            ConsoleCommandSender console = Bukkit.getConsoleSender();
            console.sendMessage(ChatColor.YELLOW + "If you wanna change the language to English: /setlanguage en");
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        hiddenTeam = scoreboard.getTeam("hidden_nicks");
        if (hiddenTeam == null) {
            hiddenTeam = scoreboard.registerNewTeam("hidden_nicks");
        }
        hiddenTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + (language.equals("ru") ? "У вас нет прав!" : "You don't have permission!"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("togglevisibility")) {
            if (args.length == 0) {
                hideAll = !hideAll;
                String message = language.equals("ru") ? "Скрытие никнеймов: " : "Nickname visibility: ";
                sender.sendMessage(ChatColor.AQUA + message + (hideAll ? "включено" : "выключено"));

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (hideAll) {
                        hiddenTeam.addEntry(player.getName());
                    } else {
                        hiddenTeam.removeEntry(player.getName());
                    }
                }
                return true;
            } else if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + (language.equals("ru") ? "Игрок не найден!" : "Player not found!"));
                    return true;
                }

                if (hiddenTeam.hasEntry(target.getName())) {
                    hiddenTeam.removeEntry(target.getName());
                    sender.sendMessage(ChatColor.AQUA + (language.equals("ru") ? "Ник отображается для " : "Nickname visible for ") + target.getName());
                } else {
                    hiddenTeam.addEntry(target.getName());
                    sender.sendMessage(ChatColor.AQUA + (language.equals("ru") ? "Ник скрыт для " : "Nickname hidden for ") + target.getName());
                }
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("setlanguage")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("ru") || args[0].equalsIgnoreCase("en")) {
                    language = args[0].toLowerCase();
                    getConfig().set("language", language);
                    saveConfig();
                    sender.sendMessage(ChatColor.AQUA + (language.equals("ru") ? "Язык изменён на русский" : "Language changed to English"));
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid language! Use 'ru' or 'en'");
                }
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }
        Player player = event.getPlayer();
        Player target = (Player) event.getRightClicked();

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            String formattedName = ChatColor.translateAlternateColorCodes('&', nameFormat.replace("{NAME}", target.getName()));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formattedName));
        }
    }
}


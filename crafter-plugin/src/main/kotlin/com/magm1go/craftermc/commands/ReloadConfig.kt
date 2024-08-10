package com.magm1go.craftermc.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadConfig : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        Bukkit.getPluginManager().getPlugin("CrafterDiscord")!!.reloadConfig()
        sender.sendMessage("Конфиг перезагружен.")

        return true
    }

}
package com.crafter.implementation.rcon

import com.crafter.discord.commands.SlashCommand
import com.crafter.structure.database.repositories.RCONRepository
import com.crafter.discord.t9n.text
import com.crafter.structure.minecraft.rcon.RconController
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

// This command can be singleton, because everything here static
object RconCommand : SlashCommand("rcon", "rcon.description") {
    private val ignoredIps = listOf("255.255.255.255", "0.0.0.0", "::1")
    private val ipRegex = """\b((25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})\b""".toRegex()
    private val portRegex = """\b(6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[0-9]{1,4})\b""".toRegex()

    private fun isUnsafePassword(password: String): Boolean =
        ClassLoader.getSystemClassLoader().getResource("unsafe_passwords.txt")!!.readText()
            .contains(password, ignoreCase = true)

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "setup" -> rconSetup(event)
            "execute" -> rconExecuteCommand(event)
        }
    }

    private suspend fun rconSetup(event: SlashCommandInteractionEvent) {
        val (ip, port, password) = Triple(
            event.getOption("ip")!!.asString,
            event.getOption("port")!!.asString,
            event.getOption("password")!!.asString
        )

        if (isUnsafePassword(password)) {
            event.reply(
                text(
                    "rcon.setup.unsafe_password",
                    "Please change your RCON password. Your password is unsafe.",
                    event.userLocale
                )
            ).queue()
            return
        }

        if (!ip.matches(ipRegex) || ip.startsWith("127") || ip in ignoredIps) {
            event.reply(text("rcon.setup.invalid_ip", "You provided invalid RCON IP", event.userLocale)).queue()
        } else if (!port.matches(portRegex)) {
            event.reply(text("rcon.setup.invalid_port", "You provided invalid RCON port", event.userLocale)).queue()
        } else {
            RCONRepository.upsert(
                mapOf(
                    "guild_id" to event.interaction.guild!!.id,
                    "ip" to ip,
                    "port" to port.toInt(),
                    "password" to password
                )
            )

            event.reply(text("rcon.setup.success", "Your RCON settings was saved.", event.userLocale)).queue()
        }
    }

    private suspend fun rconExecuteCommand(event: SlashCommandInteractionEvent) {
        val data = RCONRepository.get(event.interaction.guild!!.id)!!
        val rcon = RconController(
            data["ip"].toString(),
            data["port"] as Int,
            RCONRepository.getRconPassword(data["password"].toString())
        )

        val responses = rcon.send(event.getOption("command")!!.asString)
        event.reply(
            text("rcon.execute.server_response", "Server response: ", event.userLocale) +
                    "```markdown\n" +
                    responses.joinToString { response -> "# ${response.message}" } +
                    "```"
        ).queue()
    }

    init {
        commandData.addSubcommands(
            SubcommandData("setup", "Setting up your RCON")
                .addOptions(
                    OptionData(OptionType.STRING, "ip", "Your RCON IP"),
                    OptionData(OptionType.STRING, "port", "Your RCON port"),
                    OptionData(OptionType.STRING, "password", "Your RCON password")
                ),
            SubcommandData("execute", "Execute RCON command")
                .addOption(OptionType.STRING, "command", "Command that should be executed on server.")
        )
    }
}
package com.crafter.implementation.rcon

import com.crafter.structure.database.repositories.RCONRepository
import com.crafter.discord.commands.slash
import com.crafter.discord.t9n.text
import com.crafter.structure.minecraft.rcon.RconController
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

val ignoredIps = listOf("255.255.255.255", "0.0.0.0", "::1")
val ipRegex = """\b((25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})\b""".toRegex()
val portRegex =  """\b(6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[0-9]{1,4})\b""".toRegex()

val options = listOf(
    OptionData(OptionType.STRING, "ip", "Your RCON IP", true),
    OptionData(OptionType.INTEGER, "port", "Your RCON port", true),
    OptionData(OptionType.STRING, "password", "Your RCON password", true),
)

fun isUnsafePassword(password: String): Boolean =
    ClassLoader.getSystemClassLoader().getResource("unsafe_passwords.txt")!!.readText()
        .contains(password, ignoreCase = true)

suspend fun rconSetup(event: SlashCommandInteractionEvent) {
    val (ip, port, password) = Triple(
        event.getOption("ip")!!.asString,
        event.getOption("port")!!.asString,
        event.getOption("password")!!.asString
    )

    if (isUnsafePassword(password)) {
        event.reply(text(
            "rcon.setup.unsafe_password",
            "Please change your RCON password. Your password is unsafe.",
            event.userLocale
        )).queue()
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

val commandInstance = slash("rcon", "An RCON client") {
    when (it.subcommandName) {
        "setup" -> rconSetup(it)
        "execute" -> {
            val data = RCONRepository.get(it.interaction.guild!!.id)!!

            val rcon = RconController(
                data["ip"].toString(),
                data["port"] as Int,
                RCONRepository.getRconPassword(data["password"].toString())
            )

            val responses = rcon.send(it.getOption("command")!!.asString)
            it.reply(
                text("rcon.execute.server_response", "Server response: ", it.userLocale) +
                        "```markdown\n" +
                        responses.joinToString { response -> "# ${response.message}" } +
                        "```"
            ).queue()
        }
    }
}

val rconCommandInstance = commandInstance
    .addSubCommand(
        SubcommandData("setup", "Setting up your RCON")
            .addOptions(options),
        SubcommandData("execute", "Execute RCON command")
            .addOption(OptionType.STRING, "command", "Command that should be executed on server.")
    )
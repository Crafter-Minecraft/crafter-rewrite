package com.crafter.implementation

import com.crafter.discord.commands.SlashCommand
import com.crafter.structure.database.repositories.RCONRepository
import com.crafter.discord.t9n.text
import com.crafter.structure.database.repositories.RCONRestrictionRepository
import com.crafter.structure.minecraft.Helper
import com.crafter.structure.minecraft.rcon.RconController
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.io.IOException

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
            "restrict" -> rconRestrict(event)
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

        if (!RCONRestrictionRepository.isUserExists(event.guild!!.id, event.user.id)) {
            event.reply(text(
                "rcon.execute.missing_permissions",
                "Sorry, but you don't have enough permissions to execute RCON commands",
                event.userLocale
            )).queue()
            return
        }

        try {
            val responses = RconController(
                data["ip"].toString(),
                data["port"] as Int,
                RCONRepository.getRconPassword(data["password"].toString())
            ).use {
                it.send(event.getOption("command")!!.asString)
            }

            event.reply(
                text("rcon.execute.server_response", "Server response: ", event.userLocale) +
                        "```markdown\n" +
                        responses.joinToString { response -> "# ${response.message}" } +
                        "```"
            ).queue()
        } catch (e: IOException) {
            event.reply(text("rcon.execute.cant_connect", "Sorry, but I can't connect to RCON.", event.userLocale)).queue()
        }
    }

    private suspend fun rconRestrict(event: SlashCommandInteractionEvent) {
        val repository = RCONRestrictionRepository
        val guildId = event.guild!!.id
        val userId = event.getOption("user")!!.asUser.id

        if (repository.isUserExists(guildId, userId)) {
            repository.deleteUser(guildId, userId)
            event.reply(text(
                "rcon.restrict.user_deleted",
                "User was deleted and now does not have permissions to execute RCON commands",
                event.userLocale
            ))
            return
        }

        repository.upsert(mapOf(
            "guildId" to guildId,
            "userId" to userId
        ))

        event.reply(text("rcon.restrict.user_added", "User added.", event.userLocale)).queue()
    }

    override fun autoComplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>> {
        return listOf("command" to Helper.AVAILABLE_MINECRAFT_COMMANDS)
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
                .addOption(OptionType.STRING, "command", "Command that should be executed on server.", true, true),
            SubcommandData("restrict", "Restrict users who can execute RCON commands.")
                .addOption(OptionType.USER, "user", "User that should can execute commands.", true)
        )

        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
    }
}
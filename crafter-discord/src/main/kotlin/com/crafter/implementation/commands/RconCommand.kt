package com.crafter.implementation.commands

import com.crafter.Helper
import com.crafter.discord.t9n.text
import com.crafter.discord.core.child.SlashCommand
import com.crafter.discord.core.child.Subcommand
import com.crafter.rcon.RconController
import com.crafter.structure.database.repositories.RCONRepository
import com.crafter.structure.database.repositories.RCONRestrictionRepository
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import java.io.IOException

object RconCommand : SlashCommand("rcon", "Main RCON command", {
    defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
    subCommand(RestrictRconCommand::class)
    subCommand(SetupRconCommand::class)
    subCommand(ExecuteRconCommand::class)
}) {
    private var unsafePasswords: String? = null

    private val ignoredIps: List<String> = listOf(
        "255.255.255.255", "0.0.0.0",
        "::1", "::"
    )
    private val ipRegex: Regex = """\b((25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})\b""".toRegex()
    private val portRegex: Regex = """\b(6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[0-9]{1,4})\b""".toRegex()

    private fun isUnsafePassword(password: String): Boolean {
        if (unsafePasswords.isNullOrEmpty()) {
            unsafePasswords = ClassLoader.getSystemClassLoader().getResource("unsafe_passwords.txt")?.readText()
                ?: throw IllegalStateException("Can't read unsafe_passwords.txt.")
        }

        return unsafePasswords?.contains(password, ignoreCase = true) ?: false
    }

    override suspend fun invoke(event: SlashCommandInteractionEvent) {}
    override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null

    object SetupRconCommand : Subcommand("setup", "Setting up your RCON", {
        argument<String>("ip", "Your RCON ip", true)
        argument<String>("port", "Your RCON port", true)
        argument<String>("password", "Your RCON password", true)
    }) {
        override suspend fun invoke(event: SlashCommandInteractionEvent) {
            val (ip, port, password) = Triple(
                event.getOption("ip")!!.asString,
                event.getOption("port")!!.asString,
                event.getOption("password")!!.asString
            )

            if (isUnsafePassword(password)) {
                event.reply(
                    text(
                        "rcon.setup.unsafe_password",
                        event.userLocale
                    )
                ).setEphemeral(true).queue()
                return
            }

            if (!ip.matches(ipRegex) || ip.startsWith("127") || ip in ignoredIps) {
                event.reply(text("rcon.setup.invalid_ip", event.userLocale))
                    .setEphemeral(true)
                    .queue()
            } else if (!port.matches(portRegex)) {
                event.reply(text("rcon.setup.invalid_port", event.userLocale))
                    .setEphemeral(true)
                    .queue()
            } else {
                RCONRepository.upsert(
                    mapOf(
                        "guild_id" to event.interaction.guild!!.id,
                        "ip" to ip,
                        "port" to port.toInt(),
                        "password" to password
                    )
                )

                event.reply(text(
                    "rcon.setup.success",
                    event.userLocale
                )).setEphemeral(true).queue()
            }
        }

        override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null
    }

    object ExecuteRconCommand : Subcommand("execute", "Execute RCON command", {
        argument<String>("command", "Command that should be executed on server.", true, true)
    }) {
        override suspend fun invoke(event: SlashCommandInteractionEvent) {
            event.deferReply().queue()

            val guildId = event.interaction.guild!!.id
            val userId = event.user.id
            val userLocale = event.userLocale

            val data = RCONRepository.get(guildId)

            if (data == null) {
                event.replyWithMessage("rcon.execute.null_data", userLocale)
                return
            }

            if (!RCONRestrictionRepository.isUserExists(guildId, userId)) {
                event.replyWithMessage("rcon.execute.missing_permissions", userLocale)
                return
            }

            val command = event.getOption("command")?.asString
            if (command.isNullOrEmpty()) {
                event.replyWithMessage("rcon.execute.empty_command", userLocale)
                return
            }

            try {
                val rconController = RconController(
                    data["ip"].toString(),
                    data["port"] as Int,
                    RCONRepository.getRconPassword(data["password"].toString())
                )

                val responses = rconController.use { it.send(command) }
                val responseText = responses.joinToString(separator = "\n") { response -> "# ${response.message}" }

                if (responseText.isBlank()) {
                    event.replyWithMessage("rcon.execute.command_issued", userLocale)
                } else {
                    event.replyWithMessage("rcon.execute.server_response", userLocale) {
                        append("```markdown\n$responseText\n```")
                    }
                }
            } catch (e: IOException) {
                event.replyWithMessage("rcon.execute.cant_connect", userLocale)
            } catch (e: Exception) {
                event.replyWithMessage("rcon.execute.error", userLocale)
            }
        }

        override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? =
            listOf("command" to Helper.AVAILABLE_MINECRAFT_COMMANDS)
    }

    object RestrictRconCommand : Subcommand("restrict", "Restrict users who can execute RCON commands.", {
        argument<User>("user", "User that should can execute commands.", true)
    }) {
        override suspend fun invoke(event: SlashCommandInteractionEvent) {
            val repository = RCONRestrictionRepository
            val guildId = event.guild!!.id
            val userId = event.getOption("user")!!.asUser.id

            if (repository.isUserExists(guildId, userId)) {
                repository.deleteUser(guildId, userId)
                event.replyLocalized(
                    "rcon.restrict.user_deleted",
                    event.userLocale
                )
                return
            }

            repository.upsert(mapOf(
                "guildId" to guildId,
                "userId" to userId
            ))

            event.replyLocalized("rcon.restrict.user_added", event.userLocale)
        }

        override suspend fun autocomplete(event: CommandAutoCompleteInteractionEvent): List<Pair<String, List<String>>>? = null
    }
}
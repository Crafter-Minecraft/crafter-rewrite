package com.crafter.discord.core

import com.crafter.discord.core.child.Subcommand
import com.crafter.discord.core.child.SubcommandGroup
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

class CommandSetup {
    val optionContainer = mutableListOf<OptionData>()
    val subCommandGroupContainer = mutableMapOf<SubcommandGroup, SubcommandGroupData>()
    val subCommandContainer = mutableMapOf<Subcommand, SubcommandData>()
    var defaultPermissions: DefaultMemberPermissions = DefaultMemberPermissions.ENABLED
        get() = field
        set(value) { field = value }

    inline fun <reified T> argument(
        name: String,
        description: String,
        isRequired: Boolean = false,
        autoComplete: Boolean = false,
        choices: Map<String, String> = emptyMap(),
    ): CommandSetup {
        val option = OptionData(genericToOptionType<T>(), name, description, isRequired, autoComplete)

        if (T::class.isSubclassOf(Channel::class)) {
            option.setChannelTypes(argumentChannelType<T>())
        }

        choices.forEach { choice, value -> option.addChoice(choice, value) }

        optionContainer.add(option)
        return this
    }

    inline fun <reified T> argumentChannelType(): ChannelType = when (T::class) {
        TextChannel::class -> ChannelType.TEXT
        VoiceChannel::class -> ChannelType.VOICE
        StageChannel::class -> ChannelType.STAGE
        ForumChannel::class -> ChannelType.FORUM
        Category::class -> ChannelType.CATEGORY
        else -> throw IllegalStateException("Unknown channel type")
    }

    inline fun <reified S : Subcommand> subCommand(subCommandClass: KClass<S>): CommandSetup {
        if (!subCommandClass.isSubclassOf(S::class)) {
            throw IllegalStateException("Class must be subcommand or subgroup")
        }

        val instance = if (subCommandClass.objectInstance == null) {
            subCommandClass.createInstance()
        } else {
            subCommandClass.objectInstance as Subcommand
        }

        subCommandContainer[instance] = instance.commandData
        return this
    }

    inline fun <reified G : SubcommandGroup> subGroup(subGroupClass: KClass<G>): CommandSetup {
        if (!subGroupClass.isSubclassOf(G::class)) {
            throw IllegalStateException("Class must be subcommand or subgroup")
        }

        val instance = if (subGroupClass.objectInstance == null) {
            subGroupClass.createInstance()
        } else {
            subGroupClass.objectInstance as SubcommandGroup
        }

        subCommandGroupContainer[instance] = instance.groupData
        return this
    }

    inline fun <reified T> genericToOptionType(): OptionType = when (T::class) {
        String::class -> OptionType.STRING
        Int::class -> OptionType.INTEGER
        Boolean::class -> OptionType.BOOLEAN
        Number::class -> OptionType.NUMBER
        User::class -> OptionType.USER
        Channel::class,
        TextChannel::class, VoiceChannel::class, StageChannel::class,
        ForumChannel::class, Category::class -> OptionType.CHANNEL
        else -> throw IllegalStateException("Invalid type")
    }
}
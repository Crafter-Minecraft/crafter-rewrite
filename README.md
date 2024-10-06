Sure! Here’s the updated README with the specified configuration details included:

# Crafter Bot

Crafter is a Discord bot that specializes in RCON and creates a bridge between your Minecraft server and Discord. It provides convenient features for server management and player interaction.

## Features

- **RCON**: Remote management of your Minecraft server via RCON.
- **Discord Integration**: Receive updates about server events directly in Discord.
- **Additional Features**: Handy commands for admins and players that enhance interaction.

## Installation

### Prerequisites

- Java 21 or higher
- Gradle

### Building

To build the project, use the following command:

```bash
./gradlew shadowJar
```

After a successful build, the JAR file can be found in the `build/libs` directory.

## Configuration

1. Download and set up the JAR file on your server.
2. Copy the `application.properties.example` file to `application.properties` and configure it by filling in the necessary parameters:

**Make sure to remove the `.example` suffix from the filename.**

3. Start the bot:

```bash
java -jar path/to/your/crafter-bot.jar
```

## Contributing

If you would like to contribute to the project, feel free to create pull requests or leave your ideas in the Issues section.

## License

This project is licensed under the GNU General Public License v3. See the [LICENSE](LICENSE) file for more details.

---

If you have any questions or issues, don't hesitate to reach out!
package everyos.bot.luwu.run.command.modules.levelling;

import everyos.bot.luwu.core.command.CommandContainer;

public class LevelCommands {
	public static void installTo(CommandContainer commands) {
		commands.registerCommand("command.level", new LevelCommand());
	}
}

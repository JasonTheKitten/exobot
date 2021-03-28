package everyos.bot.luwu.run.command.modules.privacy;

import everyos.bot.chat4j.entity.ChatPermission;
import everyos.bot.luwu.core.client.ArgumentParser;
import everyos.bot.luwu.core.command.CommandData;
import everyos.bot.luwu.run.command.CommandBase;
import reactor.core.publisher.Mono;

public class GuildResetCommand extends CommandBase {

	public GuildResetCommand() {
		super("command.resetguild", e->true, ChatPermission.SEND_MESSAGES, ChatPermission.MANAGE_GUILD);
	}

	@Override
	public Mono<Void> execute(CommandData data, ArgumentParser parser) {
		return null;
	}
	
}

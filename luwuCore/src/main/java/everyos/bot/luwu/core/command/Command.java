package everyos.bot.luwu.core.command;

import everyos.bot.luwu.core.client.ArgumentParser;
import everyos.bot.luwu.core.entity.Client;
import reactor.core.publisher.Mono;

public interface Command {
	default Mono<Void> run(CommandData data, ArgumentParser parser) {
		return execute(data, parser);
	};
	
	Mono<Void> execute(CommandData data, ArgumentParser parser);
	
	//String getID();
	default String getID() {
		return null;
	}

	default boolean isSupported(Client client) {
		return true;
	};
}

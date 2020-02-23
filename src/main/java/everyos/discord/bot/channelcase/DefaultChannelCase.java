package everyos.discord.bot.channelcase;

import java.util.HashMap;

import discord4j.core.object.entity.Message;
import everyos.discord.bot.adapter.MessageAdapter;
import everyos.discord.bot.commands.ICommand;
import everyos.discord.bot.commands.LocalizedCommandWrapper;
import everyos.discord.bot.commands.fun.CurrencyCommand;
import everyos.discord.bot.commands.info.UptimeCommand;
import everyos.discord.bot.commands.moderation.BanCommand;
import everyos.discord.bot.commands.moderation.KickCommand;
import everyos.discord.bot.localization.Localization;
import everyos.discord.bot.parser.ArgumentParser;

public class DefaultChannelCase implements IChannelCase {
	private static HashMap<String, LocalizedCommandWrapper> commands;
	static {
		ICommand currencyCommand = new CurrencyCommand();
		ICommand banCommand = new BanCommand();
		ICommand kickCommand = new KickCommand();
		ICommand uptimeCommand = new UptimeCommand();
		
		commands = new HashMap<String, LocalizedCommandWrapper>();
		commands.put("feth", new LocalizedCommandWrapper(currencyCommand, Localization.en_US));
		commands.put("ban", new LocalizedCommandWrapper(banCommand, Localization.en_US));
		commands.put("kick", new LocalizedCommandWrapper(kickCommand, Localization.en_US));
		commands.put("uptime", new LocalizedCommandWrapper(uptimeCommand, Localization.en_US));
	}
	
	@Override public void execute(Message message, MessageAdapter adapter) {
        ArgumentParser.ifPrefix(message.getContent().orElse(""), adapter.getPreferredPrefix(), content->{
            String command = ArgumentParser.getCommand(content);
            String argument = ArgumentParser.getArgument(content);
            if (commands.containsKey(command))
                commands.get(command).execute(message, adapter, argument);
        });
	}
}
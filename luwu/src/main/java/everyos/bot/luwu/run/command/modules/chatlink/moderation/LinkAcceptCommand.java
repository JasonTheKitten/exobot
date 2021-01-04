package everyos.bot.luwu.run.command.modules.chatlink.moderation;

import everyos.bot.luwu.core.client.ArgumentParser;
import everyos.bot.luwu.core.command.Command;
import everyos.bot.luwu.core.command.CommandData;
import everyos.bot.luwu.core.entity.Channel;
import everyos.bot.luwu.core.entity.ChannelID;
import everyos.bot.luwu.core.entity.Locale;
import everyos.bot.luwu.core.entity.Message;
import everyos.bot.luwu.core.entity.UserID;
import everyos.bot.luwu.core.exception.TextException;
import everyos.bot.luwu.core.functionality.channel.ChannelTextInterface;
import everyos.bot.luwu.run.command.CommandBase;
import everyos.bot.luwu.run.command.modules.chatlink.ChatLink;
import everyos.bot.luwu.run.command.modules.chatlink.ChatLinkChannel;
import reactor.core.publisher.Mono;

public class LinkAcceptCommand extends CommandBase {
	public LinkAcceptCommand() {
		super("command.link.accept");
	}

	private static LinkAcceptCommand instance;

	@Override public Mono<Void> execute(CommandData data, ArgumentParser parser) {
		//Get the link
		//Check that we have permissions
		//Parse the channel ID
		//Get the link channel, and check that the link IDs match
		//Accept the channel
		Locale locale = data.getLocale();
		
		return data.getChannel().as(ChatLinkChannel.type)
			.flatMap(clchannel->clchannel.getLink())
			.flatMap(link->{
				return checkPerms(link, data.getChannel().getID(), data.getInvoker().getID(), locale)
					.then(parseArguments(parser, locale))
					.flatMap(id->data.getConnection().getChannelByID(id.getLong()))
					.flatMap(channel->channel.as(ChatLinkChannel.type))
					
					.filter(channel->channel.getLinkID()==link.getID())
					.switchIfEmpty(Mono.error(new TextException(locale.localize("command.link.notjoining"))))
					.flatMap(channel->verify(channel, data.getChannel(), locale)
						.then(sendSystemMessage(link, channel, locale)));
			})
			.then();
	}

	private Mono<ChannelID> parseArguments(ArgumentParser parser, Locale locale) {
		if (!parser.couldBeChannelID()) {
			String got = parser.getRemaining();
			if (got.isEmpty()) got = locale.localize("command.error.nothing");
			return Mono.error(new TextException(locale.localize("command.error.usage",
				"expected", locale.localize("command.error.channelid"),
				"got", got)));
		}
		return Mono.just(new ChannelID(parser.eatChannelID()));
	}
	
	private Mono<Void> checkPerms(ChatLink link, ChannelID channelID, UserID userID, Locale locale) {
		return Mono.just(link.isAdmin(channelID))
			.filter(v->!v)
			.flatMap(v->Mono.error(new TextException(locale.localize("command.link.permsmissing"))))
			.then();
	}
	
	private Mono<Message> verify(ChatLinkChannel clchannel, Channel invokingChannel, Locale locale) {
		ChannelTextInterface textGrip = invokingChannel.getInterface(ChannelTextInterface.class);
		return clchannel.edit(spec->spec.setVerified(true))
			.then(clchannel.getServer())
			.flatMap(server->{
				return textGrip.send(locale.localize("command.link.verified",
					"id", clchannel.getID().toString(),
					"server", server.getName(),
					"name", clchannel.getName()));
			});
	}
	
	private Mono<Void> sendSystemMessage(ChatLink link, ChatLinkChannel clchannel, Locale locale) {
		return clchannel.getServer().flatMap(server->{
			return link.sendSystemMessage(locale.localize("command.link.systemverified",
				"id", clchannel.getID().toString(),
				"server", server.getName(),
				"name", clchannel.getName()));
		});
	}

	static {
		instance = new LinkAcceptCommand();
	}
	
	public static Command get() {
		return instance;
	}
}

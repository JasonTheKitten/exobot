package everyos.discord.bot.command.utility;

import java.util.HashMap;

import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import everyos.discord.bot.adapter.GuildAdapter;
import everyos.discord.bot.annotation.Help;
import everyos.discord.bot.command.CategoryEnum;
import everyos.discord.bot.command.CommandData;
import everyos.discord.bot.command.ICommand;
import everyos.discord.bot.command.IGroupCommand;
import everyos.discord.bot.localization.Localization;
import everyos.discord.bot.localization.LocalizedString;
import everyos.discord.bot.parser.ArgumentParser;
import everyos.discord.bot.util.PermissionUtil;
import everyos.storage.database.DBObject;
import reactor.core.publisher.Mono;

@Help(help=LocalizedString.RoleCommandHelp, ehelp = LocalizedString.RoleCommandExtendedHelp, category=CategoryEnum.Utility)
public class RoleCommand implements IGroupCommand {
	private HashMap<Localization, HashMap<String, ICommand>> lcommands;

	public RoleCommand() {
	 	HashMap<String, ICommand> commands;
	    lcommands = new HashMap<Localization, HashMap<String, ICommand>>();

	    //Commands
	    ICommand roleCreateCommand = new RoleCreateCommand();
	    ICommand roleTakeCommand = new RoleTakeCommand();
	    ICommand roleRemoveCommand = new RoleRemoveCommand();

	    //en_US
	    commands = new HashMap<String, ICommand>();
	    commands.put("create", roleCreateCommand);
	    commands.put("take", roleTakeCommand);
	    commands.put("remove", roleRemoveCommand);
	    //set, delete, list
	    lcommands.put(Localization.en_US, commands);
	}
	
	@Override public Mono<?> execute(Message message, CommandData data, String argument) {
		if (argument.equals("")) argument = "";

		String cmd = ArgumentParser.getCommand(argument);
		String arg = ArgumentParser.getArgument(argument);

		ICommand command = lcommands.get(data.locale.locale).get(cmd);

		if (command==null)
			return message.getChannel().flatMap(c->c.createMessage(data.locale.localize(LocalizedString.NoSuchSubcommand)));

		return command.execute(message, data, arg);
	}
	
	@Override public HashMap<String, ICommand> getCommands(Localization locale) { return lcommands.get(locale); }
}

class RoleCreateCommand implements ICommand {
	@Override public Mono<?> execute(Message message, CommandData data, String argument) {
		return message.getChannel().flatMap(channel->{
			return message.getAuthorAsMember()
				.flatMap(member->PermissionUtil.check(member, channel, data.locale, Permission.MANAGE_ROLES))
				.flatMap(o->message.getGuild())
				.flatMap(guild->{
					return GuildAdapter.of(data.shard, guild).getData((obj, doc)->{
						//TODO: Limit creatable roles, for the sanity of my disk
						if (obj.getOrDefaultObject("roles", new DBObject()).has(argument)) {
							return channel.createMessage(data.localize(LocalizedString.RoleAlreadyExists));
						}
						return guild.createRole(spec->{
							spec.setName(argument);
							spec.setMentionable(false);
						}).flatMap(role->{
							obj.getOrCreateObject("roles", ()->new DBObject()).set(argument, role.getId().asString());
							doc.save();
							
							return channel.createMessage(data.localize(LocalizedString.RoleCreated));
						});
					});
			});
		});
	}
}

class RoleTakeCommand implements ICommand {
	@Override public Mono<?> execute(Message message, CommandData data, String argument) {
		return message.getChannel().flatMap(channel->{
			return message.getGuild().flatMap(guild->{
				String roleID = GuildAdapter.of(data.shard, guild).getData(obj->{
					DBObject roles = obj.getOrDefaultObject("roles", null);
					if (roles == null) return null;
					return roles.getOrDefaultString(argument, null);
				});
				if (roleID==null) return channel.createMessage(data.localize(LocalizedString.NoSuchRole));
				return message.getAuthorAsMember().flatMap(member->member.addRole(Snowflake.of(roleID)))
					.then(channel.createMessage(data.localize(LocalizedString.RoleGiven)));
			});
		});
		//TODO: On-Error
	}
}

class RoleRemoveCommand implements ICommand {
	@Override public Mono<?> execute(Message message, CommandData data, String argument) {
		return message.getChannel().flatMap(channel->{
			return message.getGuild().flatMap(guild->{
				String roleID = GuildAdapter.of(data.shard, guild).getData(obj->{
					DBObject roles = obj.getOrDefaultObject("roles", null);
					if (roles == null) return null;
					return roles.getOrDefaultString(argument, null);
				});
				if (roleID==null) return channel.createMessage(data.localize(LocalizedString.NoSuchRole));
				return message.getAuthorAsMember().flatMap(member->member.removeRole(Snowflake.of(roleID)))
					.then(channel.createMessage(data.localize(LocalizedString.RoleRemoved)));
			});
		});
		//TODO: On-Error
	}
}
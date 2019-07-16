package us.myles_selim.cp_ladder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;
import us.myles_selim.cp_ladder.commands.CommandCredits;
import us.myles_selim.cp_ladder.commands.CommandPing;
import us.myles_selim.cp_ladder.commands.CommandTest;
import us.myles_selim.cp_ladder.commands.registry.PrimaryCommandHandler;
import us.myles_selim.cp_ladder.commands.registry.java.JavaCommandHandler;
import us.myles_selim.cp_ladder.commands.selim_pm.SelimPMCommandHandler;
import us.myles_selim.cp_ladder.lua.LuaEventHandler;
import us.myles_selim.cp_ladder.lua.commands.CommandUploadScript;
import us.myles_selim.cp_ladder.lua.commands.LuaCommandHandler;

public class CPLadder {

	private static DiscordClient CLIENT;
	private static final Properties PROPERTIES = new Properties();

	public final static boolean DEBUG = false;
	public static boolean IS_DEV;
	public static boolean FULLY_STARTED = false;
	public final static String BOT_NAME = "CP Ladder";

	public static PrimaryCommandHandler COMMAND_HANDLER;
	// public static ReactionMessageRegistry REACTION_MESSAGES_REGISTRY;
	public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		// register shutdown stuff
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				System.out.println("running shutdown thread");
				EXECUTOR.shutdown();
			}
		});

		try {
			try {
				PROPERTIES.load(new FileInputStream("cp_ladder.properties"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			DiscordClientBuilder clientBuilder = new DiscordClientBuilder(
					PROPERTIES.getProperty("token"));
			CLIENT = clientBuilder.build();
			Mono<Void> loginMono = CLIENT.login();
			CLIENT.updatePresence(Presence.invisible());
			if (CLIENT == null) {
				System.err.println("Failed to login, exiting");
				return;
			}
			EventDispatcher dispatcher = CLIENT.getEventDispatcher();
			dispatcher.on(MessageCreateEvent.class).subscribe((event) -> {
				System.out.println(event.getMessage());
			});

			COMMAND_HANDLER = new PrimaryCommandHandler(CLIENT);
			// REACTION_MESSAGES_REGISTRY = new ReactionMessageRegistry(CLIENT);

			System.out.println("registering commands");

			JavaCommandHandler jCmdHandler = new JavaCommandHandler(CLIENT);
			COMMAND_HANDLER.registerCommandHandler(jCmdHandler);
			registerCommands(jCmdHandler);

			SelimPMCommandHandler.init();
			System.out.println("starting threads and loading settings...");

			COMMAND_HANDLER.setup(dispatcher);

			System.out.println("v" + LadderConstants.VERSION + (DEBUG || IS_DEV ? "d" : ""));
			StatusUpdater statusUpdater = new StatusUpdater(CLIENT);
			statusUpdater.addPresence(Presence.online(
					Activity.playing("v" + LadderConstants.VERSION + (DEBUG || IS_DEV ? "d" : ""))));
			statusUpdater.start();

			new LuaEventHandler().setup(dispatcher);
			new LuaCommandHandler(CLIENT).setup(dispatcher);

			FULLY_STARTED = true;

			loginMono.block();
		} catch (Exception e) {
			System.err.println("+-------------------------------------------------------------------+");
			System.err.println("| Starota failed to start properly. Printing exception then exiting |");
			System.err.println("+-------------------------------------------------------------------+");
			e.printStackTrace();
			Runtime.getRuntime().exit(0);
		}
	}

	private static void registerCommands(JavaCommandHandler jCmdHandler) {
		jCmdHandler.registerDefaultCommands();

		jCmdHandler.registerCommand(new CommandCredits());
		jCmdHandler.registerCommand(new CommandPing());
		if (IS_DEV)
			jCmdHandler.registerCommand("Debug", new CommandTest());

		jCmdHandler.registerCommand("Lua", new CommandUploadScript());
	}

	public static DiscordClient getClient() {
		return CLIENT;
	}

	public static Guild getGuild(long guildId) {
		return CLIENT.getGuildById(Snowflake.of(guildId)).block();
	}

	public static Channel getChannel(long guildId, long channelId) {
		return getGuild(guildId).getChannelById(Snowflake.of(channelId)).block();
	}

	public static Channel getChannel(long channelId) {
		return CLIENT.getChannelById(Snowflake.of(channelId)).block();
	}

	public static User getUser(long userId) {
		return CLIENT.getUserById(Snowflake.of(userId)).block();
	}

	public static User getOurUser() {
		return CLIENT.getSelf().block();
	}

	public static String getOurName(long guildId) {
		return getOurName(getGuild(guildId));
	}

	public static String getOurName(Guild guild) {
		if (CLIENT == null)
			return null;
		if (guild == null)
			return getOurUser().getUsername();
		return getOurUser().asMember(guild.getId()).block().getDisplayName();
	}

	public static User findUser(String name) {
		return findUser(-1, name);
	}

	public static User findUser(long serverId, String name) {
		try {
			long userId = Long.parseLong(name);
			User user = getUser(userId);
			if (user != null)
				return user;
		} catch (NumberFormatException e) {}
		String user = name.replaceAll("@", "").replaceAll("#\\d{4}", "");
		if (user.matches("<\\d{18}>")) {
			long userId = Long.parseLong(user.substring(1, 19));
			Guild guild = CPLadder.getGuild(serverId);
			if (guild != null) {
				User userD = guild.getMemberById(Snowflake.of(userId)).block();
				if (userD != null)
					return userD;
			}
		}
		String discrim = null;
		if (name.matches(".*#\\d{4}")) {
			int hash = name.indexOf("#");
			discrim = name.substring(hash + 1, hash + 5);
		} else if (name.matches("<@!\\d*>")) {
			try {
				long id = Long.parseLong(name.substring(3, name.length() - 1));
				User idUser = getUser(id);
				if (idUser != null) {
					return idUser;
				}
			} catch (NumberFormatException e) {}
		}
		DiscordClient client = CPLadder.getClient();
		List<User> users;
		if (serverId != -1)
			users = MiscUtils.convertList(MiscUtils
					.getMembersByName(client.getGuildById(Snowflake.of(serverId)).block(), user, false));
		else
			users = MiscUtils.getUsersByName(client, user, false);
		for (User u : users)
			if (discrim == null || u.getDiscriminator().equals(discrim))
				return u;
		if (serverId != -1) {
			Guild server = client.getGuildById(Snowflake.of(serverId)).block();
			for (Member u : server.getMembers().collectList().block()) {
				String nickname = u.getDisplayName();
				if (u.getUsername().equalsIgnoreCase(user) || u.getDisplayName().equalsIgnoreCase(user)
						|| (nickname != null && nickname.equalsIgnoreCase(user)))
					return u;
			}
		}
		return null;
	}

	public static Channel findChannel(String name) {
		return findChannel(-1, name);
	}

	public static Channel findChannel(long serverId, String name) {
		if (name == null)
			return null;
		if (name.matches("<#\\d*>")) {
			try {
				long id = Long.parseLong(name.substring(2, name.length() - 1));
				Channel idChannel = getChannel(id);
				if (idChannel != null)
					return idChannel;
			} catch (NumberFormatException e) {}
		}
		name = name.substring(1);
		DiscordClient client = CPLadder.getClient();
		List<GuildChannel> channels;
		if (serverId != -1)
			channels = MiscUtils.getChannelsByName(client.getGuildById(Snowflake.of(serverId)).block(),
					name);
		else {
			// channels = client.getChannels(true);
			// for (Channel ch : channels)
			// if (ch.getName().equalsIgnoreCase(name))
			// return ch;
		}
		if (serverId != -1) {
			Guild server = client.getGuildById(Snowflake.of(serverId)).block();
			for (GuildChannel ch : server.getChannels().collectList().block())
				if (ch.getName().equalsIgnoreCase(name))
					return ch;
		}
		return null;
	}

	public static void submitError(Throwable e) {
		submitError(null, e);
	}

	public static void submitError(String message, Throwable e) {
		if (IS_DEV)
			e.printStackTrace();
		if (IS_DEV || CLIENT == null || !CLIENT.isConnected())
			return;
		long reportChannelId = 522805019326677002L;
		Channel reportChannel = CLIENT.getChannelById(Snowflake.of(reportChannelId)).block();
		if (reportChannel == null)
			return;
		EmbedBuilder builder = new EmbedBuilder();
		builder.withTitle("An " + e.getClass().getSimpleName() + " has been thrown"
				+ (message == null ? "" : ": " + message));
		String body = e.getClass().getName() + ": " + e.getLocalizedMessage();
		for (StackTraceElement t : e.getStackTrace()) {
			String line = t.toString();
			if (body.length() + line.length() > 2048)
				break;
			body += line + "\n";
		}
		builder.appendDesc(body);
		((TextChannel) reportChannel).createEmbed(builder.build());
	}

}

package us.myles_selim.cp_ladder.lua;

import java.io.InputStream;
import java.util.HashMap;

import org.squiddev.cobalt.Constants;
import org.squiddev.cobalt.LuaError;
import org.squiddev.cobalt.LuaNil;
import org.squiddev.cobalt.LuaState;
import org.squiddev.cobalt.LuaTable;
import org.squiddev.cobalt.LuaUserdata;
import org.squiddev.cobalt.LuaValue;
import org.squiddev.cobalt.ValueFactory;
import org.squiddev.cobalt.compiler.LuaC;
import org.squiddev.cobalt.function.LuaFunction;
import org.squiddev.cobalt.lib.BaseLib;
import org.squiddev.cobalt.lib.CoroutineLib;
import org.squiddev.cobalt.lib.MathLib;
import org.squiddev.cobalt.lib.StringLib;
import org.squiddev.cobalt.lib.TableLib;
import org.squiddev.cobalt.lib.platform.AbstractResourceManipulator;

import discord4j.core.event.domain.guild.GuildEvent;
import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import us.myles_selim.cp_ladder.enums.EnumPokemon;
import us.myles_selim.cp_ladder.lua.conversion.AutoConverter;
import us.myles_selim.cp_ladder.lua.conversion.ConversionHandler;
import us.myles_selim.cp_ladder.lua.events.LuaEvent;
import us.myles_selim.cp_ladder.lua.libraries.DiscordEventLib;
import us.myles_selim.cp_ladder.lua.libraries.DiscordLib;
import us.myles_selim.cp_ladder.lua.libraries.StarotaLib;
import us.myles_selim.ebs.EBStorage;

public class LuaUtils {

	private final static HashMap<Long, LuaState> STATES = new HashMap<>();

	private static boolean registeredConverters = false;

	public static void registerConverters() {
		if (registeredConverters)
			return;
		registeredConverters = true;

		ConversionHandler.registerAutoConverter(EnumPokemon.class);

		ConversionHandler.registerAutoConverter(Category.class);
		ConversionHandler.registerAutoConverter(TextChannel.class);
		ConversionHandler.registerAutoConverter(Message.class);
		ConversionHandler.registerAutoConverter(Role.class);
		ConversionHandler.registerAutoConverter(Guild.class);
		ConversionHandler.registerAutoConverter(User.class);
	}

	public static LuaState getState(Guild guild) {
		if (STATES.containsKey(guild.getId().asLong()))
			return STATES.get(guild.getId().asLong());
		LuaState state = new LuaState(new AbstractResourceManipulator() {

			@Override
			public InputStream findResource(String filename) {
				return null;
			}
		});
		// state.stdout = null;
		LuaTable _G = new LuaTable();
		state.setupThread(_G);
		_G.load(state, new BaseLib());
		// _G.load(state, new PackageLib());
		_G.load(state, new TableLib());
		_G.load(state, new StringLib());
		_G.load(state, new CoroutineLib());
		_G.load(state, new MathLib());
		// _G.load(state, new JseIoLib());
		// _G.load(state, new OsLib());

		_G.load(state, new DiscordLib(guild));
		_G.load(state, new StarotaLib(guild));
		_G.load(state, new DiscordEventLib(guild));
		_G.rawset("dofile", Constants.NIL);
		_G.rawset("loadfile", Constants.NIL);
		LuaC.install(state);
		STATES.put(guild.getId().asLong(), state);
		return state;
	}

	public static boolean isInitialized(Guild server) {
		return STATES.containsKey(server.getId().asLong());
	}

	public static void clearEventHandlers(Guild server) {
		if (STATES.containsKey(server.getId().asLong()))
			clearEventHandlers(getState(server));
	}

	public static void clearEventHandlers(LuaState state) {
		LuaTable env = state.getMainThread().getfenv();
		env.rawset("events", new LuaTable());
	}

	public static LuaValue objToValue(LuaState state, Object obj) {
		if (obj == null)
			return Constants.NIL;
		if (obj instanceof Boolean)
			return ValueFactory.valueOf((Boolean) obj);
		if (obj instanceof Integer)
			return ValueFactory.valueOf((Integer) obj);
		if (obj instanceof Double)
			return ValueFactory.valueOf((Double) obj);
		if (obj instanceof String)
			return ValueFactory.valueOf((String) obj);
		if (obj instanceof byte[])
			return ValueFactory.valueOf((byte[]) obj);
		if (obj instanceof EBStorage)
			return StarotaLib.storageToValue((EBStorage) obj);
		return ConversionHandler.convertToLua(state, obj);
	}

	public static Object valueToObj(LuaState state, LuaValue val) {
		if (val == null || val instanceof LuaNil)
			return null;
		if (val.isBoolean())
			return val.toBoolean();
		if (val.isString())
			return val.toString();
		if (val.isInteger())
			return val.toInteger();
		if (val.isNumber())
			return val.toDouble();
		return ConversionHandler.convertToJava(state, val);
	}

	public static LuaValue getEvent(LuaState state, Object event) {
		if (event instanceof GuildEvent)
			return getEvent(state, (GuildEvent) event);
		if (event instanceof LuaEvent)
			return ((LuaEvent) event).toLua(state);
		throw new RuntimeException(
				"event must be a LuaEvent or GuildEvent, got " + event.getClass().getName());
	}

	private static LuaValue getEvent(LuaState state, GuildEvent event) {
		try {
			return AutoConverter.INSTANCE.toLua(state, event);
		} catch (LuaError e) {
			return null;
		}
	}

	public static void handleEvent(Guild guild, LuaValue event) {
		LuaState state = STATES.get(guild.getId().asLong());
		if (state == null)
			return;
		handleEvent(guild, event, state, event);
	}

	private static void handleEvent(Guild server, Object event, LuaState state, LuaValue eventL) {
		LuaTable _G = state.getMainThread().getfenv();
		String functName = "on" + event.getClass().getSimpleName();
		TextChannel errorChannel;
		if (eventL.isTable() && ((LuaTable) eventL).rawget("channel") instanceof LuaUserdata)
			errorChannel = (TextChannel) ((LuaUserdata) ((LuaTable) eventL).rawget("channel")).instance;
		else
			errorChannel = null;
		try {
			LuaTable eventLib = _G.rawget(DiscordEventLib.KEY).checkTable();
			LuaValue funcV = eventLib.get(state, functName);
			// for (LuaValue v : eventLib.keys()) {
			// System.out.println("event handler: " + v);
			// }
			// System.out.println("event fired: " + functName);
			if (funcV == null || !funcV.isFunction())
				return;
			((LuaFunction) funcV).call(state, eventLib, eventL);
		} catch (LuaError e) {
			System.out.println("event fired: " + functName + " on server: " + server.getName());
			if (errorChannel != null)
				errorChannel.createMessage("There was an error when processing a " + functName
						+ " event: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	// protected static LuaValue getRole(Guild server, Role role) {
	// if (role == null)
	// return Constants.NIL;
	// LuaTable roleT = new LuaTable();
	// roleT.rawset("getId", new ZeroArgFunction() {
	//
	// @Override
	// public LuaValue call(LuaState state) throws LuaError {
	// return ValueFactory.valueOf(role.getId().asString());
	// }
	// });
	// roleT.rawset("getName", new ZeroArgFunction() {
	//
	// @Override
	// public LuaValue call(LuaState state) throws LuaError {
	// return ValueFactory.valueOf(role.getName());
	// }
	// });
	// roleT.rawset("getColor", new ZeroArgFunction() {
	//
	// @Override
	// public LuaValue call(LuaState state) throws LuaError {
	// return ValueFactory.valueOf(role.getColor().getRGB());
	// }
	// });
	// return roleT;
	// }
	//
	// protected static LuaValue getChannel(Guild server, TextChannel channel) {
	// if (channel == null)
	// return Constants.NIL;
	// LuaTable channelT = new LuaTable();
	// channelT.rawset("getName", new ZeroArgFunction() {
	//
	// @Override
	// public LuaValue call(LuaState state) throws LuaError {
	// return ValueFactory.valueOf(channel.getName());
	// }
	// });
	// channelT.rawset("createMessage", new OneArgFunction() {
	//
	// @Override
	// public LuaValue call(LuaState state, LuaValue arg) throws LuaError {
	// if (arg instanceof LuaNil)
	// return Constants.FALSE;
	// if (channel.getModifiedPermission(Starota.getOurUser())
	// .contains(Permission.SEND_MESSAGES))
	// return RequestBuffer.request(() -> {
	// try {
	// channel.createMessage(arg.toString());
	// } catch (DiscordException e) {
	// return Constants.FALSE;
	// }
	// return Constants.TRUE;
	// }).get();
	// else
	// return Constants.FALSE;
	// }
	// });
	// channelT.rawset("getCategory", new ZeroArgFunction() {
	//
	// @Override
	// public LuaValue call(LuaState state) throws LuaError {
	// Category cat = channel.getCategory();
	// if (cat == null)
	// return Constants.NIL;
	// return ValueFactory.valueOf(cat.getName());
	// }
	// });
	// channelT.rawset("getUsersHere", new ZeroArgFunction() {
	//
	// @Override
	// public LuaValue call(LuaState state) throws LuaError {
	// LuaTable tbl = new LuaTable();
	// List<IUser> users = channel.getUsersHere();
	// for (int i = 0; i < users.size(); i++)
	// tbl.rawset(i, getUser(server, users.get(i)));
	// return tbl;
	// }
	// });
	// return channelT;
	// }

}

package us.myles_selim.cp_ladder.lua.libraries;

import org.squiddev.cobalt.LuaState;
import org.squiddev.cobalt.LuaTable;
import org.squiddev.cobalt.LuaValue;

import discord4j.core.object.entity.Guild;
import us.myles_selim.cp_ladder.lua.ScriptManager;

public class DiscordEventLib extends DiscordLib {

	public static final String KEY = "events";

	public DiscordEventLib(Guild server) {
		super(server);
	}

	@Override
	public LuaValue add(LuaState state, LuaTable env) {
		super.add(state, env);
		LuaTable eventHandler = new LuaTable();
		env.rawset(KEY, eventHandler);
		ScriptManager.executeEventScript(state, getGuild());
		return env;
	}

}

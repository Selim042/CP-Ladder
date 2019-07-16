package us.myles_selim.cp_ladder.lua.libraries;

import org.squiddev.cobalt.Constants;
import org.squiddev.cobalt.LuaError;
import org.squiddev.cobalt.LuaNil;
import org.squiddev.cobalt.LuaState;
import org.squiddev.cobalt.LuaTable;
import org.squiddev.cobalt.LuaValue;
import org.squiddev.cobalt.ValueFactory;
import org.squiddev.cobalt.function.OneArgFunction;
import org.squiddev.cobalt.function.TwoArgFunction;
import org.squiddev.cobalt.lib.LuaLibrary;

import discord4j.core.object.entity.Guild;
import us.myles_selim.cp_ladder.LadderConstants;
import us.myles_selim.cp_ladder.enums.EnumPokemon;
import us.myles_selim.cp_ladder.lua.LuaUtils;
import us.myles_selim.cp_ladder.lua.conversion.ConversionHandler;
import us.myles_selim.ebs.EBStorage;

public class StarotaLib implements LuaLibrary {

	private final Guild server;

	public StarotaLib(Guild server) {
		this.server = server;
	}

	@Override
	public LuaValue add(LuaState state, LuaTable env) {
		env.rawset("_STAROTA_VERSION", ValueFactory.valueOf(LadderConstants.VERSION));
		env.rawset("getPokemon", new OneArgFunction() {

			@Override
			public LuaValue call(LuaState state, LuaValue arg) throws LuaError {
				if (arg.isIntExact())
					return ConversionHandler.convertToLua(state,
							EnumPokemon.getPokemon(arg.checkInteger()));
				return ConversionHandler.convertToLua(state, EnumPokemon.getPokemon(arg.toString()));
			}
		});
		return env;
	}

	public static LuaValue storageToValue(EBStorage stor, String... lockedKeys) {
		LuaTable options = new LuaTable();
		// options.rawset("getKeys", new ZeroArgFunction() {
		//
		// @Override
		// public LuaValue call(LuaState state) throws LuaError {
		// LuaTable ret = new LuaTable();
		// int i = 0;
		// for (String k : stor.getKeys())
		// ret.rawset(i++, ValueFactory.valueOf(k));
		// return ret;
		// }
		// });
		options.rawset("hasKey", new OneArgFunction() {

			@Override
			public LuaValue call(LuaState state, LuaValue arg) throws LuaError {
				if (arg instanceof LuaNil)
					return Constants.NIL;
				return ValueFactory.valueOf(stor.containsKey(arg.toString()));
			}
		});
		options.rawset("getValue", new OneArgFunction() {

			@Override
			public LuaValue call(LuaState state, LuaValue arg) throws LuaError {
				if (arg instanceof LuaNil)
					return Constants.NIL;
				return LuaUtils.objToValue(state, stor.get(arg.toString()));
			}
		});
		options.rawset("setValue", new TwoArgFunction() {

			@Override
			public LuaValue call(LuaState state, LuaValue arg1, LuaValue arg2) throws LuaError {
				if (arg1 instanceof LuaNil || arg1 instanceof LuaNil
						|| arrCont(lockedKeys, arg1.toString()))
					return Constants.NIL;
				stor.set(arg1.toString(), LuaUtils.valueToObj(state, arg2));
				return Constants.NIL;
			}
		});
		options.rawset("clearValue", new OneArgFunction() {

			@Override
			public LuaValue call(LuaState state, LuaValue arg) throws LuaError {
				if (arg instanceof LuaNil || arrCont(lockedKeys, arg.toString()))
					return Constants.NIL;
				stor.clearKey(arg.toString());
				return Constants.NIL;
			}
		});
		return options;
	}

	private static boolean arrCont(String[] sss, String ss) {
		for (String s : sss)
			if (s != null && s.equals(ss))
				return true;
		return false;
	}

}

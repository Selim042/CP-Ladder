package us.myles_selim.cp_ladder;

import discord4j.core.object.util.Snowflake;

public class LadderConstants {

	/** Starota version */
	public final static String VERSION = "1.0.0";

	/** CP Ladder user ID */
	public static final Snowflake LADDER_ID = Snowflake.of(600510757662359553L);
	/** Selim user ID */
	public static final Snowflake SELIM_USER_ID = Snowflake.of(134855940938661889L);

	/** User agent for all HTTP requests */
	public static final String HTTP_USER_AGENT = "Mozilla/5.0; Starota/" + VERSION;

}

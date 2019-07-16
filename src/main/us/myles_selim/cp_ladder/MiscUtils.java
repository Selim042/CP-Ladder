package us.myles_selim.cp_ladder;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import us.myles_selim.cp_ladder.enums.EnumPokemon;

public class MiscUtils {

	@SuppressWarnings("unchecked")
	public static <P extends S, S> List<S> convertList(List<P> parCol) {
		try {
			List<S> subCol = parCol.getClass().getConstructor().newInstance();
			for (P p : parCol)
				subCol.add((S) p);
			return subCol;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public static String fixCharacters(String in) {
		if (in == null)
			return null;
		return in.replaceAll("’", "'").replaceAll("é", "e").replaceAll("—", "-").replaceAll("×", "x");
	}

	public static String getTrainerCodeString(long trainerCode) {
		String codeS = String.format("%012d", trainerCode);
		return codeS.substring(0, 4) + " " + codeS.substring(4, 8) + " " + codeS.substring(8, 12);
	}

	public static boolean arrContains(int[] tt, int tv) {
		for (int t : tt)
			if (t == tv)
				return true;
		return false;
	}

	public static <T> boolean arrContains(T[] tt, T tv) {
		for (T t : tt)
			if (t != null && t.equals(tv))
				return true;
		return false;
	}

	public static boolean stringArrayContainsIgnoreCase(String[] arr, String val) {
		for (String v : arr)
			if (v == val || v.equalsIgnoreCase(val))
				return true;
		return false;
	}

	public static List<Member> getMembersHere(GuildChannel ch) {
		Guild guild = ch.getGuild().block();
		return guild.getMembers().filter((user) -> {
			PermissionSet permissions = ch.getEffectivePermissions(user.getId()).block();
			return hasPermission(Permission.VIEW_CHANNEL, generatePermissionNumber(permissions), true);
		}).collect(Collectors.toList()).block();
	}

	public static boolean hasPermission(Permission perm, int permissionsNumber, boolean checkAdmin) {
		if ((perm.getValue() & permissionsNumber) > 0)
			return true;
		else if (!perm.equals(Permission.ADMINISTRATOR) && checkAdmin)
			return hasPermission(Permission.ADMINISTRATOR, permissionsNumber);
		return false;
	}

	public static boolean hasPermission(Permission perm, int permissionsNumber) {
		return hasPermission(perm, permissionsNumber, true);
	}

	public static boolean hasRole(Member member, Role role) {
		return hasRole(member, role.getId());
	}

	public static boolean hasRole(Member member, Snowflake roleId) {
		return member.getRoles().any((r) -> r.getId().equals(roleId)).block();
	}

	public static PermissionSet getAllowedPermissionForNumber(int permissionsNumber) {
		PermissionSet permissionsSet = PermissionSet.none();
		for (Permission permission : PermissionSet.all())
			if (hasPermission(permission, permissionsNumber))
				permissionsSet.add(permission);
		return permissionsSet;
	}

	public static PermissionSet getDeniedPermissionForNumber(int permissionsNumber) {
		PermissionSet permissionsSet = PermissionSet.none();
		for (Permission permission : PermissionSet.all())
			if (hasPermission(permission, permissionsNumber, false))
				permissionsSet.add(permission);
		return permissionsSet;
	}

	public static int generatePermissionNumber(PermissionSet permissions) {
		if (permissions == null)
			permissions = PermissionSet.none();
		int number = 0;
		for (Permission permission : permissions)
			number |= (permission.getValue());
		return number;
	}

	public static <V> V getValueIgnoreCase(Map<String, V> map, String key) {
		for (String s : map.keySet())
			if (s.equalsIgnoreCase(key))
				return map.get(s);
		return null;
	}

	public static List<GuildEmoji> getEmojiByName(Guild guild, String name, boolean caseSensitive) {
		return guild.getEmojis()
				.collect(() -> new LinkedList<>(), (List<GuildEmoji> l, GuildEmoji u) -> {
					if (caseSensitive) {
						if (u.getName().equals(name))
							l.add(u);
					} else if (u.getName().equalsIgnoreCase(name))
						l.add(u);
				}).block();
	}

	public static List<User> getUsersByName(DiscordClient client, String name, boolean caseSensitive) {
		return client.getUsers().collect(() -> new LinkedList<>(), (List<User> l, User u) -> {
			if (caseSensitive) {
				if (u.getUsername().equals(name))
					l.add(u);
			} else if (u.getUsername().equalsIgnoreCase(name))
				l.add(u);
		}).block();
	}

	public static List<Member> getMembersByRole(Guild guild, Role role) {
		return getMembersByRole(guild, role.getId());
	}

	public static List<Member> getMembersByRole(Guild guild, Snowflake roleId) {
		return guild.getMembers().collect(() -> new LinkedList<>(), (List<Member> l, Member m) -> {
			if (MiscUtils.hasRole(m, roleId))
				l.add(m);
		}).block();
	}

	public static List<Member> getMembersByName(Guild guild, String name) {
		return getMembersByName(guild, name, false);
	}

	public static List<Member> getMembersByName(Guild guild, String name, boolean includeNickname) {
		return guild.getMembers().collect(() -> new LinkedList<>(), (List<Member> l, Member m) -> {
			if (includeNickname && m.getDisplayName().equals(name))
				l.add(m);
			else if (m.getUsername().equals(name))
				l.add(m);
		}).block();
	}

	public static List<Member> getMembersByNameIgnoreCase(Guild guild, String name) {
		return getMembersByNameIgnoreCase(guild, name, false);
	}

	public static List<Member> getMembersByNameIgnoreCase(Guild guild, String name,
			boolean includeNickname) {
		return guild.getMembers().collect(() -> new LinkedList<>(), (List<Member> l, Member m) -> {
			if (includeNickname && m.getDisplayName().equalsIgnoreCase(name))
				l.add(m);
			else if (m.getUsername().equalsIgnoreCase(name))
				l.add(m);
		}).block();
	}

	public static List<GuildChannel> getChannelsByName(Guild guild, String name) {
		return guild.getChannels()
				.collect(() -> new LinkedList<>(), (List<GuildChannel> l, GuildChannel m) -> {
					if (m.getName().equals(name))
						l.add(m);
				}).block();
	}

	public static List<GuildChannel> getChannelsByNameIgnoreCase(Guild guild, String name) {
		return guild.getChannels()
				.collect(() -> new LinkedList<>(), (List<GuildChannel> l, GuildChannel m) -> {
					if (m.getName().equalsIgnoreCase(name))
						l.add(m);
				}).block();
	}

	public static EnumPokemon[] getSuggestedPokemon(String input, int count) {
		List<DistancedEnum<EnumPokemon>> suggestions = new ArrayList<>();
		for (EnumPokemon e : EnumPokemon.values()) {
			suggestions.add(new DistancedEnum<>(calculateDistance(e.name(), input), e));
			suggestions.add(new DistancedEnum<>(calculateDistance(e.toString(), input), e));
		}
		suggestions.sort(null);
		EnumPokemon[] out = new EnumPokemon[count];
		int index = 0;
		for (DistancedEnum<EnumPokemon> e : suggestions) {
			if (index >= count)
				break;
			EnumPokemon sug = e.en;
			if (!MiscUtils.arrContains(out, sug) && !sug.isAvailable())
				out[index++] = sug;
		}
		return out;
	}

	public static <E extends Enum<E>> E[] getSuggestionsEnum(Class<E> enumClass, String input,
			int count) {
		if (!enumClass.isEnum())
			throw new IllegalArgumentException("enumClass must be a class of an enum");
		List<DistancedEnum<E>> suggestions = new ArrayList<>();
		for (E e : enumClass.getEnumConstants()) {
			suggestions.add(new DistancedEnum<>(calculateDistance(e.name(), input), e));
			suggestions.add(new DistancedEnum<>(calculateDistance(e.toString(), input), e));
		}
		suggestions.sort(null);
		@SuppressWarnings("unchecked")
		E[] out = (E[]) Array.newInstance(enumClass, count);
		int index = 0;
		for (DistancedEnum<E> e : suggestions) {
			if (index >= count)
				break;
			E sug = e.en;
			if (!MiscUtils.arrContains(out, sug))
				out[index++] = sug;
		}
		return out;
	}

	public static String[] getSuggestions(String[] possibleStrings, String input, int count) {
		List<DistancedString> suggestions = new ArrayList<>();
		for (String str : possibleStrings) {
			DistancedString dc = new DistancedString(calculateDistance(str, input), str);
			if (!suggestions.contains(dc))
				suggestions.add(dc);
		}
		suggestions.sort(null);
		String[] out = new String[count];
		for (int i = 0; i < count; i++)
			out[i] = suggestions.get(i).str;
		return out;
	}

	private static class DistancedEnum<E> implements Comparable<DistancedEnum<E>> {

		public int dist;
		public E en;

		public DistancedEnum(int dist, E en) {
			this.dist = dist;
			this.en = en;
		}

		@Override
		public int compareTo(DistancedEnum<E> o) {
			return Integer.compare(dist, o.dist);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof DistancedEnum))
				return false;
			return en.equals(((DistancedEnum<E>) o).en);
		}
	}

	private static class DistancedString implements Comparable<DistancedString> {

		public int dist;
		public String str;

		public DistancedString(int dist, String str) {
			this.dist = dist;
			this.str = str;
		}

		@Override
		public int compareTo(DistancedString o) {
			return Integer.compare(dist, o.dist);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof DistancedString))
				return false;
			return str.equals(((DistancedString) o).str);
		}
	}

	private static int calculateDistance(String x, String y) {
		int[][] dp = new int[x.length() + 1][y.length() + 1];
		for (int i = 0; i <= x.length(); i++) {
			for (int j = 0; j <= y.length(); j++) {
				if (i == 0) {
					dp[i][j] = j;
				} else if (j == 0) {
					dp[i][j] = i;
				} else {
					dp[i][j] = min(
							dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
							dp[i - 1][j] + 1, dp[i][j - 1] + 1);
				}
			}
		}
		return dp[x.length()][y.length()];
	}

	private static int costOfSubstitution(char a, char b) {
		return a == b ? 0 : 1;
	}

	private static int min(int... numbers) {
		return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
	}

	public static EnumPermissionType getPermissionType(Permission perm) {
		switch (perm) {
		case ADMINISTRATOR:
		case VIEW_AUDIT_LOG:
		case MANAGE_GUILD:
		case KICK_MEMBERS:
		case BAN_MEMBERS:
		case CHANGE_NICKNAME:
		case MANAGE_NICKNAMES:
		case MANAGE_EMOJIS:
		case CONNECT:
		case SPEAK:
		case MUTE_MEMBERS:
		case DEAFEN_MEMBERS:
		case MOVE_MEMBERS:
		case USE_VAD:
			return EnumPermissionType.SERVER;
		case CREATE_INSTANT_INVITE:
		case MANAGE_CHANNELS:
		case MANAGE_ROLES:
		case MANAGE_WEBHOOKS:
		case VIEW_CHANNEL:
		case SEND_MESSAGES:
		case SEND_TTS_MESSAGES:
		case MANAGE_MESSAGES:
		case EMBED_LINKS:
		case ATTACH_FILES:
		case READ_MESSAGE_HISTORY:
		case MENTION_EVERYONE:
		case USE_EXTERNAL_EMOJIS:
		case ADD_REACTIONS:
			return EnumPermissionType.EITHER;
		// return EnumPermissionType.CHANNEL;
		default:
			throw new IllegalArgumentException("permission " + perm + " not assinged");
		}
	}

	private static enum EnumPermissionType {
		CHANNEL,
		SERVER,
		EITHER;
	}

}

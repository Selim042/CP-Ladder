package us.myles_selim.cp_ladder;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.DiscordClient;
import discord4j.core.object.presence.Presence;

public class StatusUpdater extends Thread {

	private final DiscordClient client;
	private final List<Presence> presences = new ArrayList<>();
	private int statusIndex = 0;

	public StatusUpdater(DiscordClient client) {
		super("StatusUpdater-" + client.getApplicationInfo().block().getName());
		this.client = client;
	}

	public void addPresence(Presence presence) {
		if (presence != null)
			this.presences.add(presence);
	}

	@Override
	public void run() {
		while (true) {
			if (statusIndex >= this.presences.size())
				statusIndex = 0;
			Presence presence = this.presences.get(statusIndex++);
			this.client.updatePresence(presence);
			if (CPLadder.IS_DEV)
				System.out.println("updating presence: " + presence.getStatus() + ", "
						+ presence.getActivity() + ", " + presence.getActivity().get().getName());
			try {
				Thread.sleep(300000); // 5 mins
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

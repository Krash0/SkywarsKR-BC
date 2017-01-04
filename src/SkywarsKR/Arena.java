package SkywarsKR;

import java.util.ArrayList;
import java.util.HashMap;

public class Arena {
	public Arena(String name, boolean activated, int minPlayers, int maxPlayers) {
		this.name = name;
		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
		this.activated = activated;
	}

	String name;
	int minPlayers;
	int maxPlayers;
	boolean activated;
	boolean playing = false;
	boolean countdown = false;
	boolean finished = false;
	HashMap<String, PlayerInGame> players = new HashMap<String, PlayerInGame>();
	ArrayList<Integer> slots = new ArrayList<Integer>();
}
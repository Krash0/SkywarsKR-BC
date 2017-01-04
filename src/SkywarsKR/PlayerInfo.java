package SkywarsKR;

public class PlayerInfo {
	public PlayerInfo(String name, Integer points, Integer kills, Integer wins) {
		this.name = name;
		this.points = points;
		this.kills = kills;
		this.wins = wins;
	}

	String name;
	String kitName = null;
	Integer points;
	Integer kills;
	Integer wins;
}

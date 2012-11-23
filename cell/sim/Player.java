package cell.sim;

public interface Player {

	// directions
	public enum Direction {W, E, NW, N, S, SE}

	// name of player
	String name();

	// next movement
	Direction move(int[][] map, int[] location, int[] sack,
	               int[][] players, int[][] traders);

	// trade with leprechaun
	void trade(double[] rate, int[] request, int[] give);
}

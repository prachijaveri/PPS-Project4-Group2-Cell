package cell.g2;

import java.util.Random;

public class Player implements cell.sim.Player 
{
	private Random gen = new Random();
	private int[] savedSack;
	private static int versions = 0;
	private int version = ++versions;

	public String name() 
	{ 
		return "g2" + (version != 1 ? " v" + version : ""); 
	}
	
	public Direction move(int[][] board, int[] location, int[] sack, int[][] players, int[][] traders)
	{
		//Board[][] contains color of each of the squares in the map
		//location[] contains our location
		//sack[] contains number of ball of each color
		//player[][] contains location of all players at that point of the game when u are moving
		//ie if you are the last player of the game player[][] will contain the location of the players after they have moved for the turn
		//traders[][] contains location of all lep when it is our turn to move
		try
		{
			Floyd.getShortestPaths(board);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		//		Print.printStatement("\n");
//		Print.printStatement("\n");
//		for(int i=0;i<location.length;i++)
//			Print.printStatement(location[i]+"\t");
//		Print.printStatement("\n");
//		Print.printStatement("\n");
		savedSack = copyI(sack);
		for (;;) 
		{
			
			Direction dir = randomDirection();
			int[] new_location = move(location, dir);
			int color = color(new_location, board);
			if (color >= 0 && sack[color] != 0) 
			{
				savedSack[color]--;
				return dir;
			}
		}
	}

	private Direction randomDirection()
	{
		switch(gen.nextInt(6)) 
		{
			case 0: return Direction.E;
			case 1: return Direction.W;
			case 2: return Direction.SE;
			case 3: return Direction.S;
			case 4: return Direction.N;
			case 5: return Direction.NW;
			default: return null;
		}
	}

	public void trade(double[] rate, int[] request, int[] give)
	{
		for (int r = 0 ; r != 6 ; ++r)
			request[r] = give[r] = 0;
		double rv = 0.0, gv = 0.0;
		for (int i = 0 ; i != 10 ; ++i) 
		{
			int j = gen.nextInt(6);
			if (give[j] == savedSack[j]) break;
			give[j]++;
			gv += rate[j];
		}
		for (;;) 
		{
			int j = gen.nextInt(6);
			if (rv + rate[j] >= gv) break;
			request[j]++;
			rv += rate[j];
		}
	}

	private static int[] move(int[] location, Player.Direction dir)
	{
		int di, dj;
		int i = location[0];
		int j = location[1];
		if (dir == Player.Direction.W) 
		{
			di = 0;
			dj = -1;
		} 
		else if (dir == Player.Direction.E) 
		{
			di = 0;
			dj = 1;
		} 
		else if (dir == Player.Direction.NW) 
		{
			di = -1;
			dj = -1;
		} 
		else if (dir == Player.Direction.N) 
		{
			di = -1;
			dj = 0;
		} 
		else if (dir == Player.Direction.S) 
		{
			di = 1;
			dj = 0;
		} else if (dir == Player.Direction.SE) 
		{
			di = 1;
			dj = 1;
		} 
		else return null;
		int[] new_location = {i + di, j + dj};
		return new_location;
	}

	private static int color(int[] location, int[][] board)
	{
		int i = location[0];
		int j = location[1];
		int dim2_1 = board.length;
		if (i < 0 || i >= dim2_1 || j < 0 || j >= dim2_1)
			return -1;
		return board[i][j];
	}

	private int[] copyI(int[] a)
	{
		int[] b = new int [a.length];
		for (int i = 0 ; i != a.length ; ++i)
			b[i] = a[i];
		return b;
	}
}

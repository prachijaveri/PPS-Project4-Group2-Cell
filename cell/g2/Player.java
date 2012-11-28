package cell.g2;

import java.util.Random;
import java.util.Vector;

public class Player implements cell.sim.Player 
{
	private Random gen = new Random();
	private int[] savedSack;
	private static int versions = 0;
	private int version = ++versions;
	private Floyd shortest;
	private Trader t;
	int turn_number = 1;
	static int board_size=0;
//	private int threshold[] = new int[6];
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
		int next_location[]=new int[2];
//		if(turn_number == 1)
//		{
//			try
//			{
//				shortest = new Floyd();
//				shortest.getShortestPaths(board);
//				board_size = (board.length+1)/2;
//			}
//			catch(Exception e)
//			{
//				System.out.println("SHORTEST : "+e);
//			}
//		}
		Direction d;
		savedSack = copyI(sack);
		shortest = new Floyd();
		shortest.getShortestPaths(board);
		board_size = (board.length+1)/2;
		t = new Trader(traders);
		int op_trader = t.getBestTrader(location , shortest);
		int next_node = getBestPath(location, traders[op_trader][0], traders[op_trader][1]);
		Print.printStatement("next : "+next_node+"\n");
		next_location = shortest.getCoordinates(next_node);
		Print.printStatement("SRC "+location[0]+"  "+location[1]+"\n");
		Print.printStatement("DEST "+next_location[0]+"  "+next_location[1]+"\n");
		turn_number++;
		d = getDirection(location[0],location[1],next_location[0],next_location[1]);
		return d;	
	}
	
	int getBestPath(int src_location[],int dest_location1,int dest_location2)
	{
		//Need to check for availability of colors marbles
		//Need to add another function to calculate another path in case the shortest path requires a color for which we do not contain the marble
		int next_node = 0;
		Vector<Integer> v = shortest.getShortestPath(src_location[0], src_location[1], dest_location1, dest_location2);
		Print.printStatement("Vector"+v);
		if(v.size() == 0)
			next_node = shortest.getMapping(dest_location1, dest_location2);
		else
		{
			next_node = (Integer)v.elementAt(0);
		}
		return next_node;
	}
	
	private Direction getDirection(int x1,int y1,int x2,int y2)
	{
		Print.printStatement(x1+"   "+y1+"\n"+x2+"  "+y2);
		if(x1 == x2 && y1+1 == y2)
			return Direction.E;
		else if(x1 == x2 && y1-1 == y2)
			return Direction.W;
		else if(x1+1 == x2 && y1+1 == y2)
			return Direction.SE;
		else if(x1+1 == x2 && y1 == y2)
			return Direction.S;
		else if(x1-1 == x2 && y1 == y2)
			return Direction.N;
		else //if(x1-1 == x2 && y1-1 == y2)
			return Direction.NW;
//		return null;
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
}

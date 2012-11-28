package cell.g2;

import java.util.Random;

import cell.sim.Player.Direction;
import java.util.ArrayList;
import java.util.Collections;

public class Player implements cell.sim.Player 
{
	private Random gen = new Random();
	private int[] savedSack;
	private static int versions = 0;
	private int version = ++versions;
	private Floyd shortest = new Floyd();
	int turn_number = 1;
	private int threshold[] = new int[6];;
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
		if(turn_number == 1)
		{
			try
			{
				shortest.getShortestPaths(board);
				turn_number++;
			}
			catch(Exception e)
			{
				System.out.println("SHORTEST : "+e);
			}
		}
		//		Print.printStatement("\n");
//		Print.printStatement("\n");
//		for(int i=0;i<location.length;i++)
//			Print.printStatement(location[i]+"\t");
//		Print.printStatement("\n");
//		Print.printStatement("\n");
		savedSack = copyI(sack);
		for (;;) {
			Direction dir = randomDirection();
			int[] new_location = move(location, dir);
			int color = color(new_location, board);
			if (color >= 0 && sack[color] != 0) {
				savedSack[color]--;
				return dir;
			}
		}
	}

	private Direction randomDirection()//int x1,int y1,int x2,int y2)
	{
		switch(gen.nextInt(6)) {
		case 0: return Direction.E;
		case 1: return Direction.W;
		case 2: return Direction.SE;
		case 3: return Direction.S;
		case 4: return Direction.N;
		case 5: return Direction.NW;
		default: return null;
	}
//		if(x1 == x2 && y1+1 == y2)
//			return Direction.E;
//		if(x1 == x2 && y1-1 == y2)
//			return Direction.W;
//		if(x1+1 == x2 && y1+1 == y2)
//			return Direction.SE;
//		if(x1+1 == x2 && y1 == y2)
//			return Direction.S;
//		if(x1-1 == x2 && y1 == y2)
//			return Direction.N;
//		if(x1-1 == x2 && y1-1 == y2)
//			return Direction.NW;
//		return null;
	}
        private class Rate_Pair implements Comparable
        {
            int i, j;
            double delta;
            private Rate_Pair(int i, int j, double delta)
            {
                this.i = i;
                this.j = j;
                this.delta = delta;
            }
            public int compareTo(Object t) 
            {
                if(((Rate_Pair)t).delta == this.delta)
                    return 0;
                else if(this.delta < ((Rate_Pair)t).delta)
                    return 1;
                else
                    return -1;
            }
        }
        public void getMappingData(ArrayList<Rate_Pair> deltaListOverall, ArrayList<Rate_Pair> [] deltaListSpecific, double [] rate)
        {
            ArrayList<Rate_Pair>deltaList = new ArrayList();
            
            for(int i = 0; i < rate.length; i++)
            {
                ArrayList<Rate_Pair> temp = new ArrayList();
                for(int j = 0; j < rate.length; j++)
                {
                    double deltaValue = 0;
                    if(i == j)
                        continue;
                    
                    deltaValue = Math.abs(rate[i] - rate[j]);
                    Rate_Pair x = new Rate_Pair(i,j,deltaValue);
                    deltaListOverall.add(x);
                    temp.add(x);
                }
                deltaListSpecific[i] = temp;
                Collections.sort(deltaListSpecific[i]);
            }
            Collections.sort(deltaListOverall);           
        }
	public void trade(double[] rate, int[] request, int[] give)
	{
            ArrayList<Rate_Pair> deltaListOverall = new ArrayList();
            ArrayList<Rate_Pair> [] deltaListSpecific = new ArrayList[rate.length];

            getMappingData(deltaListOverall,deltaListSpecific,rate);
          
            Rate_Pair temp = deltaListOverall.get(0);
            
            
            /* does some dumb code for now */
            int highest = 0;
            int lowest = 0;
            double giveValue  = 0;
            double requestValue = 0;
            
            for(int i = 0; i < rate.length ; i++)
            {
                if(rate[highest] < rate[i])
                    highest = i;
            }
            for(int i = 0; i < rate.length ; i++)
            {
                if(rate[lowest] > rate[i])
                    lowest = i;
            }
            give[highest]  = 3;
            request[lowest] = 3;
            giveValue = rate[highest] * 3;
            requestValue = rate[lowest] * 3;
            for(;;)
            {
                if(requestValue < giveValue)
                {
                    request[lowest]++;
                    requestValue += rate[lowest];
                }
                if(requestValue > giveValue)
                {
                    request[lowest]--;
                    break;
                }
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

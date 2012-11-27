package cell.g2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Floyd 
{
	protected int shortest_path[][];
	protected int mapping[][];
	protected int board_as_graph[][];
	protected int vertices = 0;
	
	void getGraph(int board[][])
	{
		try
		{
			System.out.println("1******");
			mapping = new int[board.length][board.length];
			for(int i=0;i<board.length;i++)
			{
				for(int j=0;j<board[i].length;j++)
				{
					if(board[i][j] != -1)
					{
						mapping[i][j]=vertices;
						vertices++;
					}
					else
						mapping[i][j]= -1;
				}
			}
			System.out.println("1******");
			board_as_graph = new int[vertices][vertices];
			
			for(int i=0;i<board.length;i++)
			{
				for(int j=0;j<board.length;j++)
				{
					int src = mapping[i][j];
					if(src == -1)
						continue;
					int dest,x,y;
					//W
					x=i;
					y=j-1;
					if(y>=0)
					{
						dest = mapping[i][j-1];
						if(dest != -1)
							board_as_graph[src][dest] = 1 ;
					}
					//E
					x=i;
					y=j+1;
					if(y<board.length)
					{
						dest=mapping[i][j+1];
						if(dest != -1)
							board_as_graph[src][dest] = 1 ;
					}
					//NW
					x=i-1;
					y=j-1;
					if(x>=0 && y>=0)
					{
						dest=mapping[i-1][j-1];
						if(dest != -1)
							board_as_graph[src][dest] = 1 ;
					}
					//N
					x=i-1;
					y=j;
					if(x>=0)
					{
						dest=mapping[i-1][j];
						if(dest != -1)
							board_as_graph[src][dest] = 1 ;
					}
					//S
					x=i+1;
					y=j;
					if(x<board.length)
					{
						dest=mapping[i+1][j];
						if(dest != -1)
							board_as_graph[src][dest] = 1 ;
					}
					//SE
					x=i+1;
					y=j+1;
					if(x< board.length && y<board.length)
					{
						dest=mapping[i+1][j+1];
						if(dest != -1)
							board_as_graph[src][dest] = 1 ;
					}
				}
			}
			System.out.println("1******");
		}
		catch(Exception e)
		{
			System.out.println("GET GRAPH "+e);
		}
	}
	
    public void getShortestPaths(int board[][])
    {
		try
		{
			getGraph(board);
//			System.out.print("\nEnter the destination file name-->");
			String fileName = "shortest.txt";
//			System.out.print("\nEnter directory name of destination file-->");
//			String dirName = Tools.GetString();
			File outputfile = new File(fileName);
//			System.out.print("How many vertices do you wish to create -->");
//			int vertices = board_as_graph.length;		
//			board_as_graph = new int[vertices][vertices];
			int[][] ShortestDistance = new int[vertices][vertices];
			//This matrix will contain the shortes path between i and j
			shortest_path = new int[vertices][vertices];
			//If ShortestPath[i][j] = k it means that the shortes path between i and j is i --> k  --> j		
			int n, m;
			for ( n = 0; n < vertices; n++)
				for ( m = 0; m < vertices; m++)
				{
					board_as_graph[m][n] = board_as_graph[m][n];
					if(board_as_graph[m][n] == -1 || m==n)
						board_as_graph[m][n] = 0;
					else 
						board_as_graph[m][n]=1;
						
				}
			//Floyd's algorithm starts here	
			//We first initialize the matrices. -1 in the path matrix means go directly.
			for ( n = 0; n < vertices; n++)
				for ( m = 0; m < vertices; m++)
				{
					ShortestDistance[m][n] = board_as_graph[n][m];
					shortest_path[m][n] = -1;
				}
			for (int x = 0; x < vertices; x++)
				for (int y = 0; y < vertices; y++)
					for (int z = 0; z < vertices; z++)
						if (ShortestDistance[y][z] > ShortestDistance[y][x] + ShortestDistance[x][z])
						{
							ShortestDistance[y][z] = ShortestDistance[y][x] + ShortestDistance[x][z];
							shortest_path[y][z] = x;
						}

			PrintWriter output = new  PrintWriter(new FileWriter(outputfile));
			output.print("The initial cost matrix is: \n\n");
			for ( n = 0; n < vertices; n++) 
			{
				if (n < 10) output.print(" "+n+":  ");
				else output.print(n+":  ");
				for ( m = 0; m < vertices; m++)
					if (board_as_graph[n][m] < 10)
						output.print(board_as_graph[n][m] + "   ");
					else if (board_as_graph[n][m] < 100)
						output.print(board_as_graph[n][m] + "  ");	
					else output.print(board_as_graph[n][m] + " ");
				output.println();
			}// for
			output.print("\n\nThe Shortest Distance is: \n\n");
			for ( n = 0; n < vertices; n++) 
			{
				if (n < 10) output.print(" "+n+":  ");
				else output.print(n+":  ");
				for ( m = 0; m < vertices; m++)
					if (ShortestDistance[n][m] < 10)
						output.print(ShortestDistance[n][m] + "   ");
					else if (ShortestDistance[n][m] < 100)		
						output.print(ShortestDistance[n][m] + "  ");
					else
						output.print(ShortestDistance[n][m] + " ");
				output.println();
			}// for
			output.print("\n\nThe Shortest Path is: \n\n");
			for ( n = 0; n < vertices; n++) 
			{
				if (n < 10) output.print(" "+n+":  ");
				else output.print(n+":  ");
				for ( m = 0; m < vertices; m++)
					if (shortest_path[n][m] < 10 && shortest_path[n][m]> -1)
						output.print("  " + shortest_path[n][m]);
					else
						output.print(" " + shortest_path[n][m]);
				output.println();
			}// for
			output.close();
		} //try
		catch (IOException e)
		{
			System.err.println("Error opening file" +e);
			return;
		}	
    }// end of main method
}
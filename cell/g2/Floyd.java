package cell.g2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Floyd 
{
    public static void getShortestPaths(int board[][]) throws IOException 
    {
		System.out.println("This program will generate a weighted graph and find the all pairs shortes distance in this graph.\n");
		System.out.print("The graph, the all pairs shortest distance and the shortest path matrix will be written to a file.\n");
		try
		{
			System.out.print("\nEnter the destination file name-->");
			String fileName = "shortest.txt";
//			System.out.print("\nEnter directory name of destination file-->");
//			String dirName = Tools.GetString();
			File outputfile = new File(fileName);
			System.out.print("How many vertices do you wish to create -->");
			int vertices = board.length;		
			int[][] Graph = new int[vertices][vertices];
			int[][] ShortestDistance = new int[vertices][vertices];
			//This matrix will contain the shortes path between i and j
			int[][] ShortestPath = new int[vertices][vertices];
			//If ShortestPath[i][j] = k it means that the shortes path between i and j is i --> k  --> j		
			int n, m;
			for ( n = 0; n < vertices; n++)
				for ( m = 0; m < vertices; m++)
				{
					Graph[m][n] = board[m][n];
					if(Graph[m][n] == -1 || m==n)
						Graph[m][n] = 0;
					else 
						Graph[m][n]=1;
						
				}
			//Floyd's algorithm starts here	
			//We first initialize the matrices. -1 in the path matrix means go directly.
			for ( n = 0; n < vertices; n++)
				for ( m = 0; m < vertices; m++)
				{
					ShortestDistance[m][n] = Graph[n][m];
					ShortestPath[m][n] = -1;
				}
			for (int x = 0; x < vertices; x++)
				for (int y = 0; y < vertices; y++)
					for (int z = 0; z < vertices; z++)
						if (ShortestDistance[y][z] > ShortestDistance[y][x] + ShortestDistance[x][z])
						{
							ShortestDistance[y][z] = ShortestDistance[y][x] + ShortestDistance[x][z];
							ShortestPath[y][z] = x;
						}

			PrintWriter output = new  PrintWriter(new FileWriter(outputfile));
			output.print("The initial cost matrix is: \n\n");
			for ( n = 0; n < vertices; n++) 
			{
				if (n < 10) output.print(" "+n+":  ");
				else output.print(n+":  ");
				for ( m = 0; m < vertices; m++)
					if (Graph[n][m] < 10)
						output.print(Graph[n][m] + "   ");
					else if (Graph[n][m] < 100)
						output.print(Graph[n][m] + "  ");	
					else output.print(Graph[n][m] + " ");
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
					if (ShortestPath[n][m] < 10 && ShortestPath[n][m]> -1)
						output.print("  " + ShortestPath[n][m]);
					else
						output.print(" " + ShortestPath[n][m]);
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
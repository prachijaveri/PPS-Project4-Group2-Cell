package cell.g2;

import java.util.Vector;

public class Floyd 
{
	protected Vector<Integer> shortest_path[][];
	protected Vector<Integer> possible_path[][];
	protected int mapping[][];
	protected int graph[][];
	protected int graph1[][];
	protected int board_as_graph[][];
	protected int vertices = 0;
	protected int [][] next;
	protected int [][] next1;
	protected int board_copy[][];
	void getGraph(int board[][])
	{
		try
		{
			Print.printStatement("1******\n");
			mapping = new int[board.length][board.length];
			board_copy = new int[board.length][board.length];
			board_copy = board;
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
			Print.printStatement("1******\n");
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
			Print.printStatement("1******\n");
		}
		catch(Exception e)
		{
			Print.printStatement("GET GRAPH "+e+"\n");
		}
	}

	protected void getShortestPaths(int board[][]) 
	{
		getGraph(board);
		graph = new int[vertices][vertices];
		Print.printStatement("+++++++++\n");
		for(int i=0;i<vertices;i++)
		{
			for(int j=0;j<vertices;j++)
				graph[i][j] = board_as_graph[i][j];
		}
		Print.printStatement("+++++++++\n");
		int i, j, k;
		for (i = 0; i < vertices; ++i) 
		{
			for (j = 0; j < vertices; ++j) 
			{
				if (graph[i][j] != 1 && i != j)
				{
					graph[i][j] = Integer.MAX_VALUE;
				}
			}
		}
		Print.printStatement("+++++++++\n");
		next = new int[vertices][vertices];
		for (i = 0; i < vertices; i++) 
		{
			for (j = 0; j < vertices; j++) 
			{
				next[i][j] = -1;
			}
		}
		Print.printStatement("+++++++++\n");
		for (k = 0; k < vertices; k++) 
		{
			for (i = 0; i < vertices; i++) 
			{
				for (j = 0; j < vertices; j++) 
				{
					if (graph[i][k] == Integer.MAX_VALUE || graph[k][j] == Integer.MAX_VALUE) 
					{
						continue;
					}
					if (graph[i][k] + graph[k][j] < graph[i][j]) 
					{
						graph[i][j] = graph[i][k] + graph[k][j];
						next[i][j] = k;
					}
				}
			}
		}
		Print.printStatement("+++++++++\n");
		buildPath();
		Print.printStatement("+++++++++\n");
		Print.printStatement("---------------------------------------------\n");
		for(int d=0;d<vertices;d++)
		{
			for(int e=0;e<vertices;e++)
				Print.printStatement(shortest_path[d][e]+"");
			Print.printStatement("\n");
		}
		Print.printStatement("---------------------------------------------\n");

	}

	
	@SuppressWarnings("unchecked")
	private void buildPath() 
	{
		shortest_path = new Vector[vertices][vertices];
		int i, j;
		for (i = 0; i < vertices; i++) 
		{
			for (j = 0; j < vertices; j++) 
			{
				shortest_path[i][j] = new Vector<Integer>();
			}
		}
		for (i = 0; i < vertices; i++) 
		{
			for (j = 0; j < vertices; j++) 
			{
				setPath(i, j, i, j);
			}
		}
	}

	private void setPath(int i, int j, int index_i, int index_j) 
	{
		if (graph[i][j] == Integer.MAX_VALUE) 
		{
			System.err.println("Graph is not strongly connected!");
		}
		int m = next[i][j];
		if (m != -1) 
		{
			int id = m;
			setPath(i, id, index_i, index_j);
			shortest_path[index_i][index_j].add(m);
			setPath(id, j, index_i, index_j);
		}
	}

	protected Vector<Integer> getShortestPath(int i_src, int j_src, int i_dest ,int j_dest )
	{
		int src=mapping[i_src][j_src];
		int dest = mapping[i_dest][j_dest];
//		Print.printStatement("SHORTEST "+src+"   "+dest+"\n");
		return shortest_path[src][dest];
	}

	protected int getMapping(int i ,int j)
	{
		return mapping[i][j];
	}
	protected int[] getCoordinates(int m)
	{
		int coordinates[]= new int[2];
		for(int i=0;i<mapping.length;i++)
		{
			coordinates[0] = i;
			for(int j=0;j<mapping[i].length;j++)
			{
				coordinates[1]=j;
				if(mapping[i][j] == m)
					return coordinates;
			}
		}
		return coordinates;
	}
	
	protected int[] getThreshold()
	{
		int[] threshold = new int[6];
		for (int i=0; i<vertices; ++i)
		{
			for (int j=0; j<vertices; ++j) 
			{
				int[] count = new int[6];
				for (int k=0; k<shortest_path[i][j].size(); ++k)
				{
					int pos = shortest_path[i][j].get(k);
					int x = getCoordinates(pos)[0];
					int y = getCoordinates(pos)[1];
					count[board_copy[x][y]]++;
				}
				for (int m=0; m<threshold.length; ++m)
				{
					if (count[m] > threshold[m])
						threshold[m] = count[m];
				}
			}
		}
		return threshold;
	}
	
	protected void getPossiblePaths(int board[][], int sack[]) 
	{
		//getGraph(board);
		graph1 = new int[vertices][vertices];
		Print.printStatement("+++++++++\n");
		for(int i=0;i<vertices;i++)
		{
			for(int j=0;j<vertices;j++)
				graph1[i][j] = board_as_graph[i][j];
		}
		Print.printStatement("+++++++++\n");
		int i, j, k;
		for (i = 0; i < vertices; ++i) 
		{
			for (j = 0; j < vertices; ++j) 
			{
				if (graph1[i][j] != 1 && i != j)
				{
					graph1[i][j] = Integer.MAX_VALUE;
				}
			}
		}
		Print.printStatement("+++++++++\n");
		next1 = new int[vertices][vertices];
		for (i = 0; i < vertices; i++) 
		{
			for (j = 0; j < vertices; j++) 
			{
				next1[i][j] = -1;
			}
		}
		Print.printStatement("+++++++++\n");
		for (k = 0; k < vertices; k++) 
		{
			for (i = 0; i < vertices; i++) 
			{
				for (j = 0; j < vertices; j++) 
				{
					if (graph1[i][k] == Integer.MAX_VALUE || graph1[k][j] == Integer.MAX_VALUE) 
					{
						continue;
					}
					if (graph1[i][k] + graph1[k][j] < graph1[i][j]) 
					{
						graph1[i][j] = graph1[i][k] + graph1[k][j];
						next1[i][j] = k;
					}
				}
			}
		}
		Print.printStatement("+++++++++\n");
		buildPath1(sack);
		Print.printStatement("+++++++++\n");
		Print.printStatement("---------------------------------------------\n");
		for(int d=0;d<vertices;d++)
		{
			for(int e=0;e<vertices;e++)
				Print.printStatement(possible_path[d][e]+"");
			Print.printStatement("\n");
		}
		Print.printStatement("---------------------------------------------\n");

	}
	
	
	@SuppressWarnings("unchecked")
	private void buildPath1(int sack[]) 
	{
		possible_path = new Vector[vertices][vertices];
		int i, j;
		for (i = 0; i < vertices; i++) 
		{
			for (j = 0; j < vertices; j++) 
			{
				possible_path[i][j] = new Vector<Integer>();
			}
		}
		for (i = 0; i < vertices; i++) 
		{
			for (j = 0; j < vertices; j++) 
			{
				setPath1(i, j, i, j, sack);
			}
		}
	}
	
	private void setPath1(int i, int j, int index_i, int index_j, int balls_left[]) 
	{
		if (graph1[i][j] == Integer.MAX_VALUE) 
		{
			System.err.println("Graph is not strongly connected!");
		}
		int m = next1[i][j];
		int[] coor = getCoordinates(m);
		int x = coor[0];
		int y = coor[1];
		int color = board_copy[x][y];
		if (m != -1) 
		{
			int id = m;
			setPath1(i, id, index_i, index_j, balls_left);
			possible_path[index_i][index_j].add(m);
			//balls_left[color]--;
			/*if (color != -1 && balls_left[color] != 0) {
				possible_path[index_i][index_j].add(m);
				balls_left[color]--;
			}
			else {
				findAltPath(possible_path[index_i][index_j].get(possible_path.length), m, balls_left);
			}*/
			setPath1(id, j, index_i, index_j, balls_left);
		}
	}
	
	private int getNextMove(int i, int j, int balls_left[]) {
		int next_move = next1[i][j];
		int[] coor = getCoordinates(next_move);
		int x = coor[0];
		int y = coor[1];
		int color = board_copy[x][y];
		if (color == -1 || balls_left[color] == 0) {
			for (int a=0; a<vertices; ++a) {
				if (board_as_graph[i][a] == 1) {
					coor = getCoordinates(a);
					x = coor[0];
					y = coor[1];
					color = board_copy[x][y];
					if (color == -1 || balls_left[color] == 0) {
						return a;
					}
				}
			}
		}
		return next_move;
	}
	
	protected Vector<Integer> getShortestPossiblePath(int i_src, int j_src, int i_dest ,int j_dest )
	{
		int src=mapping[i_src][j_src];
		int dest = mapping[i_dest][j_dest];
//		Print.printStatement("SHORTEST "+src+"   "+dest+"\n");
		return possible_path[src][dest];
	}
}
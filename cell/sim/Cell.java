package cell.sim;

import java.io.*;
import java.util.*;
import javax.tools.*;

public class Cell {

	// configuration info that varies less
	private static boolean gui = true;
	private static int turns = 100;
	private static int traders = 5;
	private static int marbles = 10;
	private static int dim = 3;
	private static String mapPath = "cell/map/small.txt";
	private static String playerPath = "cell/players.list";

	// return game turns
	public static int gameTurns() { return turns; }

	// list files below a certain directory
	// can filter those having a specific extension constraint
	private static List <File> directoryFiles(String path, String extension) {
		List <File> allFiles = new ArrayList <File> ();
		allFiles.add(new File(path));
		int index = 0;
		while (index != allFiles.size()) {
			File currentFile = allFiles.get(index);
			if (currentFile.isDirectory()) {
				allFiles.remove(index);
				for (File newFile : currentFile.listFiles())
					allFiles.add(newFile);
			} else if (!currentFile.getPath().endsWith(extension))
				allFiles.remove(index);
			else index++;
		}
		return allFiles;
	}

	// compile and load players dynamically
	private static Player[] loadPlayers(String txtPath) {
		// list of players
		List <Player> playersList = new LinkedList <Player> ();
		try {
			// get file of players
			BufferedReader in = new BufferedReader(new FileReader(new File(txtPath)));
			// get tools
			ClassLoader loader = ClassLoader.getSystemClassLoader();
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
			// load players
			String group;
			while ((group = in.readLine()) != null) {
				System.err.println("Group: " + group);
				// delete all class files
				List <File> classFiles = directoryFiles("cell/" + group, ".class");
				System.err.print("Deleting " + classFiles.size() + " class files...   ");
				for (File file : classFiles)
					file.delete();
				System.err.println("OK");
				// compile all files
				List <File> javaFiles = directoryFiles("cell/" + group, ".java");
				System.err.print("Compiling " + javaFiles.size() + " source files...   ");
				Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(javaFiles);
				boolean ok = compiler.getTask(null, fileManager, null, null, null, units).call();
				if (!ok) throw new Exception("compile error");
				System.err.println("OK");
				// load class
				System.err.print("Loading player class...   ");
				Class playerClass = loader.loadClass("cell." + group + ".Player");
				System.err.println("OK");
				// set name of player and append on list
				Player player = (Player) playerClass.newInstance();
				playersList.add(player);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
		return playersList.toArray(new Player[0]);
	}

	// load map
	private static int[][] loadMap(String mapPath) throws Exception
	{
		BufferedReader in = new BufferedReader(new FileReader(new File(mapPath)));
		String line;
		int[][] board = null;
		int x = -1, i = 0;
		int dim = -1, dim2_1 = -1;
		while ((line = in.readLine()) != null) {
			if (i == dim2_1)
				throw new Exception("Invalid map format");
			String[] parts = line.split(",");
			if (dim == -1) {
				dim2_1 = parts.length;
				dim = (dim2_1 + 1) >> 1;
				board = new int[dim2_1][dim2_1];
			} else if (dim2_1 != parts.length)
				throw new Exception("Invalid map format");
			for (int j = 0 ; j != dim2_1 ; ++j)
				if (i != dim - 1 && abs(i - j) >= dim) {
					if (!parts[j].equals("X"))
						throw new Exception("Invalid map format");
					board[i][j] = -1;
				} else {
					try {
						x = Integer.parseInt(parts[j]);
					} catch (NumberFormatException e) {
						throw new Exception("Invalid map format");
					}
					if (x < 1 || x > 6)
						throw new Exception("Invalid map format");
					board[i][j] = x - 1;
				}
			i++;
		}
		if (i != dim2_1)
			throw new Exception("Invalid map format");
		return board;
	}

	// names of groups
	private static String[] loadTeamNames(String txtPath)
	{
		LinkedList <String> names = new LinkedList <String> ();
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(txtPath)));
			String line;
			while ((line = in.readLine()) != null)
				names.add(line);
			in.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
		return names.toArray(new String[0]);
	}

	public static void main(String[] args) throws Exception
	{
		// board size
		if (args.length > 1)
			dim = Integer.parseInt(args[1]);
		// starting marbles
		if (args.length > 2)
			marbles = Integer.parseInt(args[2]);
		// traders
		if (args.length > 3)
			traders = Integer.parseInt(args[3]);
		// game turns
		if (args.length > 4)
			turns = Integer.parseInt(args[4]);
		// map path
		if (args.length > 5)
			mapPath = args[5];
		// players path
		if (args.length > 6)
			playerPath = args[6];
		Cell game = new Cell();
		game.printPlayers();
		game.printTraders();
		HTTPServer server = null;
		int refresh = 0;
		char req = 'X';
		if (gui) {
			server = new HTTPServer();
			while ((req = server.nextRequest(0)) == 'I');
			if (req != 'B')
				throw new Exception("Invalid first request");
		}
		for (int t = 1 ; t <= turns; ++t) {
			boolean f = true;
			if (server != null) do {
				if (!f) refresh = 0;
				server.replyState(game.state(), refresh);
				while ((req = server.nextRequest(0)) == 'I');
				if (req == 'S') refresh = 0;
				else if (req == 'P') refresh = 1;
				f = false;
			} while (req == 'B');
			boolean end = game.next();
			FileOutputStream out = new FileOutputStream("cell/sim/webpages/" + t + ".html");
			out.write(game.state().getBytes());
			out.close();
			game.printPlayers();
			if (end) break;
		}
		game.rank();
		if (server != null) {
			server.replyState(game.state(), refresh);
			while ((req = server.nextRequest(2000)) == 'I');
			if (req != 'X')
				throw new Exception("Invalid last request");
		}
	}

	private Player[] players;
	private int[][] sacks;
	private int[][] board;
	private Random gen;
	private int[][] trader_location;
	private int[][] player_location;
	private double[][] trader_rates;
	private int[] round;
	private int[] count;
	private boolean[] in;
	private int turn;
	private int trades;
	private int conflicts;

	private static int abs(int x) { return x < 0 ? -x : x; }

	private String state()
	{
		int pixels = 800;
		String title = "PPS Cell";
		StringBuffer buf = new StringBuffer("");
		buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		buf.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\" lang=\"en-US\" xml:lang=\"en\">\n");
		buf.append("<head>\n");
		buf.append(" <title>" + title + "</title>\n");
		buf.append(" <style>a:hover {color:red;}</style>\n");
		buf.append("</head>\n");
		buf.append("<body>\n");
		buf.append(" <div style=\"width:" + (pixels + 550) + "px; margin-left:auto; margin-right: auto;\">\n");
		buf.append("  <div style=\"width: 200px; float: left;\">\n");
		buf.append("   <div style=\"width: 200px; height: 120px;\"></div>\n");
		buf.append("   <div style=\"clear:both;\"></div>\n");
		buf.append("   <a href=\"play\"><div style=\"width: 200px; height: 70px; float:left; cursor: pointer; text-align: center; font-size: 40px;");
		buf.append("                              color: black; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">Play</div></a>\n");
		buf.append("   <div style=\"clear:both;\"></div>\n");
		buf.append("   <a href=\"stop\"><div style=\"width: 200px; height: 70px; float:left; cursor: pointer; text-align: center; font-size: 40px;");
		buf.append("                              color: black; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">Stop</div></a>\n");
		buf.append("   <div style=\"clear:both;\"></div>\n");
		buf.append("   <a href=\"step\"><div style=\"width: 200px; height: 70px; float:left; cursor: pointer; text-align: center; font-size: 40px;");
		buf.append("                              color: black; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">Step</div></a>\n");
		buf.append("  </div>\n");
		buf.append("  <div style=\"width:" + pixels + "px; float: left;\">\n");
		String[] colors = {"red", "green", "blue", "yellow", "purple", "orange"};
		int dim2_1 = board.length;
		int dim = (dim2_1 + 1) >> 1;
		int b = (pixels / dim2_1) - 6;
		for (int i = 0 ; i != dim2_1 ; ++i) {
			buf.append("   <div style=\"height:" + b + "px; border: 3px solid none;\">\n");
			int margin = 0;
			for (int j = 0 ; j != dim2_1 ; ++j)
				if (i != dim - 1 && abs(i - j) >= dim)
					margin += b >> 1;
			boolean first = true;
			for (int j = 0 ; j != dim2_1 ; ++j) {
				if (i != dim - 1 && abs(i - j) >= dim)
					continue;
				// extract items in block
				int[] location = {i, j};
				String items = "";
				for (int p = 0 ; p != players.length ; ++p)
					if (in[p] && same_location(player_location[p], location))
						items += "" + (p + 1);
				for (int t = 0 ; t != traders ; ++t)
					if (same_location(trader_location[t], location))
						items += "L";
				// create style of block
				buf.append("    <div style=\"");
				if (first) {
					buf.append("margin-left:" + margin + "px; ");
					first = false;
				}
				buf.append("width:" + b + "px; ");
				buf.append("height:" + b + "px; ");
				buf.append("border: 3px solid black; ");
				buf.append("background-color: " + colors[board[i][j]] + "; ");
				buf.append("float:left; ");
				if (!items.isEmpty()) {
					buf.append("\n                text-align: center; ");
					buf.append("font-size: " + (int) (b * 0.7) + "px; ");
					buf.append("font-weight: bold; ");
					buf.append("font-family: 'Comic Sans MS', cursive, sans-serif");
				}
				// write data in block
				buf.append("\">" + items + "</div>\n");
			}
			buf.append("   </div>\n");
			buf.append("   <div style=\"clear:both;\"></div>\n");
		}
		buf.append("  </div>\n");
		buf.append("  <div style=\"width: 350px; float: left;\">\n");
		buf.append("   <div style=\"width: 350px; height: 50px; float:left;\"></div>");
		buf.append("   <div style=\"clear:both;\"></div>\n");
		buf.append("   <div style=\"width: 50px; height: 50px; float:left;\"></div>");
		for (String color : colors)
			buf.append("   <div style=\"width: 44px; height: 44px; float:left; border: 3px solid black; background-color: " + color + "\"></div>");
		buf.append("   <div style=\"clear:both;\"></div>\n");
		for (int p = 0 ; p != players.length ; ++p) {
			buf.append("   <div style=\"width: 44px; height: 44px; float:left; border: 3px solid black; text-align: center;");
			buf.append("               font-size: 25px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + (p + 1) + "</div>\n");
			buf.append("   <div style=\"width: 294px; height: 44px; float: left; border: 3px solid black;\">\n");
			for (int r = 0 ; r != 6 ; ++r) {
				buf.append("    <div style=\"width: 49px; height: 44px; float:left; text-align: center; font-size: 20px;");
				buf.append("                font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + sacks[p][r] + "</div>\n");
			}
			buf.append("   </div>\n");
			buf.append("   <div style=\"clear:both;\"></div>\n");
		}
		buf.append("  </div>\n");
		buf.append(" </div>\n");
		buf.append("</body>\n");
		buf.append("</html>\n");
		return buf.toString();
	}

	private void printPlayers()
	{
		System.err.println("---------------");
		for (int p = 0 ; p != players.length ; ++p) {
			int[] location = player_location[p];
			int i = location[0];
			int j = location[1];
			System.err.print("Player " + p + ": " + i + "," + j + " [" + sacks[p][0]);
			for (int r = 1 ; r != 6 ; ++r)
				System.err.print("," + sacks[p][r]);
			System.err.println("]");
		}
		System.err.println("---------------");
	}

	private void printTraders()
	{
		System.err.println("---------------");
		for (int t = 0 ; t != traders ; ++t) {
			int[] location = trader_location[t];
			int i = location[0];
			int j = location[1];
			System.err.println("Trader " + t + ": " + i + "," + j);
		}
		System.err.println("---------------");
	}

	private void printBoard()
	{
		System.err.println("Board:");
		int dim2_1 = board.length;
		for (int i = 0 ; i != dim2_1 ; ++i) {
			for (int j = 0 ; j != dim2_1 ; ++j) {
				String x = (board[i][j] < 0 ? "X" : "" + board[i][j]);
				System.err.print(x + " ");
			}
			System.err.println("");
		}
	}

	private Cell() throws Exception
	{
		// load players
		players = loadPlayers(playerPath);
		turn = trades = conflicts = 0;
		// generate board
		board = loadMap(mapPath);
		int dim2_1 = board.length;
		dim = (dim2_1 + 1) >> 1;
		System.err.println("Loaded a map with dimension: " + dim);
		printBoard();
		gen = new Random();
		// generate sacks
		sacks = new int [players.length][6];
		for (int p = 0 ; p != players.length ; ++p)
			for (int r = 0 ; r != 6 ; ++r)
				sacks[p][r] = marbles;
		// place players on board
		in = new boolean [players.length];
		round = new int [players.length];
		player_location = new int [players.length][];
		for (int p = 0 ; p != players.length ; ++p) {
			int i = gen.nextInt(dim2_1);
			int j = gen.nextInt(dim2_1);
			if (board[i][j] == -1) {
				p--;
				continue;
			}
			int[] location = {i, j};
			player_location[p] = location;
			in[p] = true;
		}
		// place traders on board
		trader_location = new int [traders][];
		trader_rates = new double [traders][6];
		for (int t = 0 ; t != traders ; ++t) {
			int i = gen.nextInt(dim2_1);
			int j = gen.nextInt(dim2_1);
			int[] location = {i, j};
			boolean valid = board[i][j] >= 0;
			for (int p = 0 ; valid && p != players.length ; ++p)
				if (same_location(player_location[p], location))
					valid = false;
			for (int p = 0 ; valid && p != t ; ++p)
				if (same_location(trader_location[p], location))
					valid = false;
			if (!valid) {
				t--;
				continue;
			}
			trader_location[t] = location;
			// generate trader rates
			double[] rates = trader_rates[t];
			for (int r = 0 ; r != 6 ; ++r)
				rates[r] = gen.nextDouble() + 1.0;
		}
	}

	private int[] copyI(int[] a)
	{
		int[] b = new int [a.length];
		for (int i = 0 ; i != a.length ; ++i)
			b[i] = a[i];
		return b;
	}

	private int[][] copyII(int[][] a)
	{
		int[][] b = new int [a.length][];
		for (int i = 0 ; i != a.length ; ++i)
			b[i] = copyI(a[i]);
		return b;
	}

	private double[] copyD(double[] a)
	{
		double[] b = new double [a.length];
		for (int i = 0 ; i != a.length ; ++i)
			b[i] = a[i];
		return b;
	}

	private boolean next() throws Exception
	{
		turn++;
		int dim2_1 = board.length;
		// game turns
		Player.Direction dirs[] =
		{Player.Direction.W,  Player.Direction.E,
		 Player.Direction.NW, Player.Direction.N,
		 Player.Direction.S, Player.Direction.SE};
		// move players
		for (int p = 0 ; p != players.length ; ++p) {
			if (!in[p]) continue;
			// location
			int[] location = player_location[p];
			int[] sack = sacks[p];
			// check if player can move
			int possible_moves = 0;
			System.err.print("Possible moves for player " + p + ":");
			for (Player.Direction dir : dirs) {
				int[] new_location = move(location, dir);
				int color = color(new_location, board);
				if (color >= 0 && sack[color] > 0) {
					System.err.print(" " + dir);
					possible_moves++;
				}
			}
			System.err.println(" (" + possible_moves + ")");
			if (possible_moves == 0) {
				in[p] = false;
				round[p] = -turn;
				continue;
			}
			// move player
			Player.Direction dir = players[p].move(copyII(board), copyI(player_location[p]), copyI(sacks[p]),
			                                       copyII(player_location), copyII(trader_location));
			System.err.println("Player " + p + " moves " + dir);
			int[] new_location = move(location, dir);
			int color = color(new_location, board);
			if (color < 0 || sack[color] == 0) {
				System.err.println("Player " + p + ": invalid move");
				in[p] = false;
				continue;
			}
			sack[color]--;
			player_location[p] = new_location;
		}
		// trade with leprechauns
		for (int t = 0 ; t != traders ; ++t) {
			// count players in location
			int[] location = trader_location[t];
			int buyers = 0, buyer = -1;
			for (int p = 0 ; p != players.length ; ++p)
				if (in[p] && same_location(player_location[p], location)) {
					buyers++;
					buyer = p;
				}
			// no buyer for trader
			if (buyers == 0) continue;
			// do the trade only if single buyer
			if (buyers != 1) conflicts++;
			else {
				trades++;
				// ask for trade
				double[] rates = trader_rates[t];
				int[] request = new int [6];
				int[] give = new int[6];
				int p = buyer;
				int[] sack = sacks[p];
				players[p].trade(copyD(rates), request, give);
				System.err.println("Player " + p + " trades with trader " + t);
				System.err.print("Rates: [" + rates[0]);
				for (int r = 1 ; r != 6 ; ++r)
					System.err.print("," + rates[r]);
				System.err.println("]");
				System.err.print("Request: [" + request[0]);
				for (int r = 1 ; r != 6 ; ++r)
					System.err.print("," + request[r]);
				System.err.println("]");
				System.err.print("Give: [" + give[0]);
				for (int r = 1 ; r != 6 ; ++r)
					System.err.print("," + give[r]);
				System.err.println("]");
				System.err.print("Sack: [" + sack[0]);
				for (int r = 1 ; r != 6 ; ++r)
					System.err.print("," + sack[r]);
				System.err.println("]");
				// check if player has enough in sack
				boolean valid = true;
				for (int r = 0 ; valid && r != 6 ; ++r)
					if (give[r] < 0 || request[r] < 0 || give[r] > sack[r])
						valid = false;
				if (valid) {
					// compute total value of player trade
					double request_value = 0.0;
					double give_value = 0.0;
					for (int r = 0 ; r != 6 ; ++r) {
						request_value += rates[r] * request[r];
						give_value += rates[r] * give[r];
					}
					System.err.println("Request value: " + request_value);
					System.err.println("Give value: " + give_value);
					if (request_value > give_value)
						valid = false;
				}
				if (valid) {
					// update marbles in sack
					for (int r = 0 ; r != 6 ; ++r)
						sack[r] += request[r] - give[r];
					// check if done
					boolean finished = true;
					for (int r = 0 ; finished && r != 6 ; ++r)
						if (sack[r] < marbles * 4)
							finished = false;
					if (finished) {
						in[p] = false;
						round[p] = turn;
					}
				} else {
					System.err.println("Player " + p + ": invalid trade");
					in[p] = false;
				}
				System.err.println("Transaction is done!");
				System.err.print("Sack: [" + sack[0]);
				for (int r = 1 ; r != 6 ; ++r)
					System.err.print("," + sack[r]);
				System.err.println("]");
				// trader changes his rates
				for (int r = 0 ; r != 6 ; ++r)
					rates[r] = gen.nextDouble() + 1.0;
			}
			// trader shows up in new location
			for (;;) {
				int i = gen.nextInt(dim2_1);
				int j = gen.nextInt(dim2_1);
				int[] new_location = {i, j};
				boolean valid = board[i][j] >= 0;
				for (int p = 0 ; valid && p != traders ; ++p)
					if (same_location(trader_location[p], new_location))
						valid = false;
				for (int p = 0 ; p != players.length ; ++p)
					if (in[p] && same_location(player_location[p], new_location))
						valid = false;
				if (valid) {
					trader_location[t] = new_location;
					break;
				}
				System.err.println("Trader moved to " + i + "," + j);
			}
		}
		int in_game = 0;
		for (int p = 0 ; p != players.length ; ++p)
			if (in[p]) in_game++;
		return in_game == 0;
	}

	private void rank()
	{
		// count marbles of remaining players
		int[] count = new int [players.length];
		for (int p = 0 ; p != players.length ; ++p)
			if (round[p] == 0)
				for (int r = 0 ; r != 6 ; ++r)
					count[p] += sacks[p][r];
		// get rank of players
		int n = round.length;
		int[] rank = new int [n];
		for (int p = 0 ; p != n ; ++p) {
			rank[p] = 1;
			// finished the game
			if (round[p] > 0) {
				for (int i = 0 ; i != n ; ++i)
					if (round[i] > 0 && round[i] < round[p])
						rank[p]++;
			// lost the marbles
			} else if (round[p] < 0) {
				for (int i = 0 ; i != n ; ++i)
					if (round[i] >= 0 || round[i] < round[p])
						rank[p]++;
			// remaining in the game
			} else
				for (int i = 0 ; i != n ; ++i)
					if (round[i] > 0 || (round[i] == 0 && count[i] > count[p]))
						rank[p]++;
		}
		// print ranking of players
		System.err.println("Trades: " + trades);
		System.err.println("Conflicts: " + conflicts);
		for (int r = 1 ; r <= players.length ; ++r)
			for (int p = 0 ; p != players.length ; ++p)
				if (rank[p] == r) {
					System.err.print(r + ": " + players[p].name() + ": ");
					if (round[p] > 0)
						System.err.println("finished at round " + round[p]);
					else if (round[p] < 0)
						System.err.println("lost marbles at round " + -round[p]);
					else
						System.err.println("remained in game with " + count[p] + " marbles");
				}
	}

	private static int[] move(int[] location, Player.Direction dir) throws Exception
	{
		int di, dj;
		int i = location[0];
		int j = location[1];
		if (dir == Player.Direction.W) {
			di = 0;
			dj = -1;
		} else if (dir == Player.Direction.E) {
			di = 0;
			dj = 1;
		} else if (dir == Player.Direction.NW) {
			di = -1;
			dj = -1;
		} else if (dir == Player.Direction.N) { // is north if flipped
			di = -1;
			dj = 0;
		} else if (dir == Player.Direction.S) { // is south if flipped
			di = 1;
			dj = 0;
		} else if (dir == Player.Direction.SE) {
			di = 1;
			dj = 1;
		} else
			throw new Exception("Invalid movement");
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

	private static boolean same_location(int[] l1, int[] l2)
	{
		return l1[0] == l2[0] && l1[1] == l2[1];
	}
}

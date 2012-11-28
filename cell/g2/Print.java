package cell.g2;

public class Print 
{
	static boolean debug_flag = false;
	static void printStatement(String s)
	{
		if(debug_flag)
			System.out.print(s);
	}
}

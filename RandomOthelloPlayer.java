/**
 * Extends OthelloPlayer to provide interface for a human player.
 *
 * @author  T.Sergeant
 * @version for AI
 *
*/
import java.util.Scanner;

public class HumanOthelloPlayer extends OthelloPlayer
{
	private Scanner kb;  // for getting input from keyboard

	/**
	 * Setup board for user with the given token.
	 */
	public HumanOthelloPlayer(Board board, char token)
	{
		super(board,token);
		kb= new Scanner(System.in);
	}

	/**
	 * Return token and that this is a human player.
	 */
	@Override
	public String toString()
	{
		return ""+token+" (Human Player)";
	}


	/**
	 * If moves are possible, list them and let user pick one.
	 *
	 * @return a Move object returned by board.makeMove()
	 *
	 * <p>We do no display the board here. Do make the board show up
	 * during the game make sure the OthelloGame object has verbose
	 * set to true.</p>
	 */
	@Override
	public Move makeMove()
	{
		int possible;
		String ans;

		possible= board.countPossibleMoves(this.token,true);
		if (possible==0) return null;

		do {
			System.out.print("Enter move: ");
			ans= kb.nextLine();
		} while (!board.canMove(this.token,ans));
		return board.makeMove(this.token,ans);
	}
}

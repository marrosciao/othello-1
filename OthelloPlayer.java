/**
 * Defines a base OthelloPlayer class that specifies the board and token.
 *
 * @author  T.Sergeant
 * @version For AI
 *
*/
public abstract class OthelloPlayer
{
	protected Board board; // the board the player is using
	protected char token;  // the token the player is using

	/**
	 * Set the board and token.
	 */
	public OthelloPlayer(Board board, char token)
	{
		this.board= board;
		this.token= token;
	}

	/**
	 * We represent the player by their token.
	 */
	@Override
	public String toString()
	{
		return ""+token;
	}

	/**
	 * Every OthelloPlayer needs to specify how they will make a move.
	 *
	 * <p>This method will need to call board.makeMove() as part of it's
	 * action to update the board and to get a valid Move object.</p>
	 *
	 * @return a Move object generated by board.makeMove()
	 */
	public abstract Move makeMove();
}

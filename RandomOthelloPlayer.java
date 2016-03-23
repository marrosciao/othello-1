/**
 * Extends OthelloPlayer to provide interface for a human player.
 *
 * @author  T.Sergeant
 * @version for AI
 *
*/
import java.util.Scanner;

public class RandomOthelloPlayer extends OthelloPlayer
{
	private Scanner kb;  // for getting input from keyboard

	/**
	 * Setup board for user with the given token.
	 */
	public RandomOthelloPlayer(Board board, char token)
	{
		super(board,token);
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
	 
	public char[][] boardCopy(Board board) {
        int rows = board.grid.length;
        int cols = board.grid[0].length;
        char[][] copy = new char[rows][cols];

        for (int row = 0; row < copy.length; row++) {
            for (int col = 0; col < copy[0].length; col++) {
                copy[row][col] = board.grid[row][col];
            }
        }

        return copy;
    }
	 
	@Override
	public Move makeMove()
	{
		int possible;

		possible= board.countPossibleMoves(this.token,true);
		if (possible==0){
		    return null;
        }
		else {
            AlphaBetaBoard current = new AlphaBetaBoard(boardCopy(board), 0);
            String moveStr = current.getMoves(this.token);
            String[] moves = moveStr.split(" ");
            
            int random = (int) (Math.random() * moves.length);
            
            return board.makeMove(this.token, moves[random]);
		}
	}
}

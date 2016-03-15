/**
 * Represents a single reversi move keeping track of captures that resulted.
 *
 * @author  T.Sergeant
 * @version for AI
*/

public class Move
{
	int i,j;             // the row and column of board on which the token is place
	char token;          // the piece that was placed: 'X' or 'O'
	Capture [] captures; // which pieces were captured?
	int n;               // how many directions were captures made?

	/**
	 * Set up a new move.
	 *
	 * @param i row in which piece is being placed
	 * @param j column in which piece is being placed
	 * @param token the piece being placed ('X' or 'O')
	 */
	public Move(int i, int j, char token) {
		this.i= i;
		this.j= j;
		this.token= token;
		this.captures= new Capture[8];
		n= 0;
	}

	/**
	 * Add a new capture to the array.
	 */
	public void addCapture(int ioff, int joff, int num) {
		captures[n++]= new Capture(ioff,joff,num);
	}
}

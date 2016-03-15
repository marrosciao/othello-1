/**
 * Plays Othello/Reversi against computer with a alpha-beta search.
 *
 * @author  T.Sergeant
 * @version For AI
 *
*/

public class OthelloGame
{
	private Board board;                    // the playing surface
	private final char [] TOKEN= {'X','O'}; // list of tokens
	private OthelloPlayer [] player;        // array of (2) players
	private boolean verbose;                // determines amount of output
	private int start;                      // who starts?


	/**
	 * Creates a new game betwen the specified player types.
	 *
	 * @param p1type type of player 1
	 * @param p2type type of player 2
	 * @param verbose generates lots of output if true
	 * @param start who should start (-1 for random, 0, or 1)
	 *
	 * <p>See createPlayer() for documention about player types.</p>
	 */
	public OthelloGame(int p1type, int p2type, boolean verbose, int start)
	{
		board= new Board();
		player= new OthelloPlayer[2];
		this.verbose= verbose;
		player[0]= createPlayer(p1type,TOKEN[0]);
		player[1]= createPlayer(p2type,TOKEN[1]);
		this.start= start;
	}


	/**
	 * Create a new game with specified types of players, verbose output, and
	 * random start.
	 *
	 * @param p1type type of player 1
	 * @param p2type type of player 2
	 */
	public OthelloGame(int p1type, int p2type)
	{
		this(p1type,p2type,true,-1);
	}


	/**
	 * Create a new game with human vs human, verbose
	 * output, and random start.
	 */
	public OthelloGame()
	{
		this(0,0,true,-1);
	}


	/**
	 * Create new player of the specified type using the specified token.
	 *
	 * @param type the type of player to create
	 * @param token the token the player will be using
	 * @return OthelloPlayer object of given type (null if type is not valid)
	 *
	 * <p>Types are as follows:</p>
	 * <ul>
	 * <li>0 = human</li>
	 * </ul>
	 */
	private OthelloPlayer createPlayer(int type, char token)
	{
		if (type==0)
			return new HumanOthelloPlayer(board,token);
		return null;
	}


	/**
	 * Play a game of Othello/Reversi.
	 */
	public void play()
	{
		int turn;

		// we want to remember who started for reportin purposes
		// if start was -1 it is a random start
		if (start==-1) {
			turn= (int) (Math.random()*2.0);
			start= turn;
		}
		else
			turn= start;

		do {
			if (verbose) {
				board.display();
				System.out.println("It is "+TOKEN[turn]+"'s move ... ("+board.countPossibleMoves(TOKEN[turn],false)+" moves available)");
			}
			player[turn].makeMove();
			turn= (turn+1)%2;
		} while (!board.gameOver());
		if (verbose) board.display();

		// we show this part whether verbose output is specified or not
		System.out.println(player[0]+" vs. "+player[1]);
		System.out.println(player[start]+" started");
		board.showResults();
	}
}

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
	private MattOthelloPlayerPrime [] player;        // array of (2) players
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
	 /*
	public OthelloGame(int p1type, int p2type, boolean verbose, int start)
	{
		board= new Board();
		player= new MattOthelloP[2];
		this.verbose= verbose;
		player[0]= createPlayer(p1type,TOKEN[0]);
		player[1]= createPlayer(p2type,TOKEN[1]);
		this.start= start;
	}
*/
    public OthelloGame(MattOthelloPlayerPrime p1type, MattOthelloPlayerPrime p2type, boolean verbose) {
        board= new Board();
        p1type.board = board;
        p2type.board = board;
        player= new MattOthelloPlayerPrime[2];
        this.verbose= verbose;
        player[0]= p1type;
        player[1]= p2type;
        this.start= 0; // Player 1 always goes first
    }

	/**
	 * Create a new game with specified types of players, verbose output, and
	 * random start.
	 *
	 * @param p1type type of player 1
	 * @param p2type type of player 2
	 */
/*	
	public OthelloGame(int p1type, int p2type)
	{
		this(p1type,p2type,true,-1);
	}
*/

	/**
	 * Create a new game with human vs human, verbose
	 * output, and random start.
	 */
/*
	public OthelloGame()
	{
		this(0,1,true,-1);
	}
*/

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
		if (type==0) {
			return new MattOthelloPlayer(this.board,token);
		}
		else {
			return new RandomOthelloPlayer(this.board, token);
		}
	}

    public int tokenCount(char[][] grid, char targetToken) {
        int count = 0;

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] == targetToken) {
                    count++;
                }
            }
        }

        return count;
    }

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
		
		// update scores
		AlphaBetaBoard current = new AlphaBetaBoard(boardCopy(board), 0);
        current.xcount = tokenCount(current.grid, 'X');
        current.ocount = tokenCount(current.grid, 'O');
        
        if (current.xcount > current.ocount) {
            player[0].wins++;
            player[0].genWins++;
            player[1].losses++;
            player[1].genLosses++;
        }
        else if (current.xcount < current.ocount) {
            player[1].wins++;
            player[1].genWins++;
            player[0].losses++;
            player[0].genLosses++;
        }
	}
}

/**
 * Defines a base OthelloPlayer class that specifies the board and token.
 *
 * @author  T.Sergeant
 * @version For AI
 *
*/
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.net.SocketException;
//import java.util.Scanner;
//import java.net.URLConnection;
//import java.net.URL;

public class MattOthelloPlayerPrime extends OthelloPlayer implements Comparable<MattOthelloPlayerPrime> {
    //protected Board board; // the board the player is using
    //protected char token;  // the token the player is using
    int nummoves;
    //Socket socket;
    //PrintWriter out;
    //Scanner in;
    int overCount;
    double tWeight;
    double wWeight;
    //double sWeight;
    double mWeight;
    long genWins;
    long genLosses;
    int genScore;
    long wins;
    long losses;
    int score;
    int x;
    int y;
    int z;
    boolean seen;
    boolean moved;
    
    int[][] boardWeights = {
        {100, -10, 11,  6,  6, 11, -10, 100},
        {-10, -20,  1,  2,  2,  1, -20, -10},
        { 10,   1,  5,  4,  4,  5,   1,  10},
        {  6,   2,  4,  2,  2,  4,   2,   6},
        {  6,   2,  4,  2,  2,  4,   2,   6},
        { 10,   1,  5,  4,  4,  5,   1,  10},
        {-10, -20,  1,  2,  2,  1, -20, -10},
        {100, -10, 11,  6,  6, 11, -10, 100}
    };

    /**
     * Set the board and token.
     */
    public MattOthelloPlayerPrime(Board board, char token, double t, double w, double m, long wi, long lo, long gw, long gl, int i, int j, int k) {
        super(board, token);

        if (token == 'X') {
            nummoves = 0;
        }
        else {
            nummoves = 1;
        }
        
        // Genes
        tWeight = t;
        wWeight = w;
        mWeight = m;
        //sWeight = 1.0;
        
        // Records
        wins = wi;
        losses = lo;
        //score = wins - losses;
        
        genWins = gw;
        genLosses = gl;
        //genScore = genWins - genLosses;
        
        // Position on the carpet
        x = i;
        y = j;
        z = k;
        
        // For later
        seen = false;
        moved = false;
        
        /* This code was to connect to a server. I'll save it for later
        System.out.println("Initializing MattOthelloPlayer...");
        try {
            URLConnection connection = new URL("http://csci.hsutx.edu/~mchastings/othello/").openConnection();
            String url = "http://example.com";
            String charset = "UTF-8";
            connection.setRequestProperty("Accept-Charset", charset);
            Scanner scan = new Scanner(connection.getInputStream());
            String ip = scan.nextLine();
            socket = new Socket("localhost", 12321);
            out = new PrintWriter(socket.getOutputStream());
            in = new Scanner(socket.getInputStream());
            System.out.println("Done.");
        }
        catch (IOException e) {
            System.out.println("Error initializing player:");
            e.printStackTrace();
        }
        */
    }

    /**
     * We represent the player by their token.
     */
    @Override
    public String toString()
    {
        return ""+token;
    }

    public String genes() {
        return "" + tWeight + " " + wWeight + " " + mWeight + " " + wins + " " + losses + " " + genWins + " " + genLosses + " " + x + " " + y + " " + z;
    }

    // We want the behavior reversed so that the highest values are at the top of the queue
    public int compareTo(MattOthelloPlayerPrime other) {
        return other.genScore - genScore;
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

    public char[][] boardCopy(AlphaBetaBoard board) {
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
    
    public String getBoardStr(AlphaBetaBoard current) {
        String boardStr = "";
        
        for (int i = 0; i < current.grid.length; i++) {
            boardStr += String.valueOf(current.grid[i]);
        }
        
        boardStr += "_";
        boardStr += String.valueOf(current.sockToken);
        boardStr += "_";
        boardStr += String.valueOf(current.moveCount);
        
        return boardStr;
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
    
    public boolean allZeros() {
        return tWeight == 0 && wWeight == 0 && mWeight == 0;
    }
    
    // Always from X's perspective
    public int getWeight(char[][] grid) {
        int weight = 0;
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (grid[i][j] == 'X') {
                    weight += boardWeights[i][j];
                }
                else if (grid[i][j] == 'O') {
                    weight -= boardWeights[i][j];
                }
            }
        }
        
        return weight;
    }

    public int tokenVal(AlphaBetaBoard board) {
        int h = 0; // H for heuristic value

        if (token == 'X') {
            h = board.xcount - board.ocount;
        }
        else {
            h = board.ocount - board.xcount;
        }

        return h;
    }
    
    public int weightVal(AlphaBetaBoard board) {
        int h = board.boardWeight;
        
        if (token == 'O') {
            h = -h;
        }
        
        return h;
    }
    
    public int mobileVal(AlphaBetaBoard board) {
        int h = 0;
        
        int xmoves = board.countPossibleMoves('X', false);
        int omoves = board.countPossibleMoves('O', false);
        
        if (token == 'X') {
            h = xmoves - omoves;
        }
        else {
            h = omoves - xmoves;
        }
        
        return h;
    }

    public double sbe(AlphaBetaBoard board) {
        double tval = (double) tokenVal(board);
        double wval = (double) weightVal(board);
        double mval = (double) mobileVal(board);
        return (tWeight * tval) + (wWeight * wval) + (mWeight * mval);
    }

    // This returns the SBE of the best move
    public double alphabeta(AlphaBetaBoard board, int ply, double alpha, double beta, boolean maxLevel, boolean lastNoMoves) {
        /*
         * I don't need to check for a terminal state because the game is over when all
         * the spaces are filled and alphabeta will not check beyond this because ply
         * is no greater than the number of moves remaining because of conditions in
         * makeMove
         */
        if (ply == 0) {
            return sbe(board);
        }
        else {
            double bestSoFar = 0.0;
            //AlphaBetaBoard current = new AlphaBetaBoard(board.boardCopy(), board.moveCount); // might not need moves
            double abresult;
            int bestIndex = -1;

            if (maxLevel) {
                bestSoFar = Double.MIN_VALUE;
                // Generate all possible moves;
                String moveStr = board.getMoves(token);
                String[] moves = moveStr.split(" ");
                String optionStr = "";
                
                // The board isn't full but the game is over. We just care about the pieces.
                if (moves.length == 0 && lastNoMoves) {
                    double tval = tokenVal(board);
                    
                    if (tval > 0.0) { // We are winning
                        return 1000.0;
                    }
                    else if (tval < 0.0) { // We are losing
                        return -1000.0;
                    }
                    
                    return 0.0;
                }

                //System.out.println("In alphabeta at a maximizing level. The ply is " + ply + ". These are the moves I can make: " + moveStr);

                boolean runOnce = true; // Might not need this.

                // If the player cannot move then this loop doesn't run
                for (int i = 0; i < moves.length /*&& (alpha <= beta || runOnce)*/; i++) {
                    //AlphaBetaBoard next = new AlphaBetaBoard(current.boardCopy(), current.moveCount);
                    Move move = null;

                    if (moves[i].length() != 0) {
                        move = board.makeMove(token, moves[i]); // Make the move with our token
                    }

                    abresult = alphabeta(board, ply - 1, alpha, beta, false, false);
                    
                    if (moves[i].length() != 0) {
                        board.unmakeMove(move);
                    }
                    
                    optionStr += abresult + " ";

                    if (abresult > bestSoFar) {
                        bestSoFar = abresult;
                        bestIndex = i;
                    }

                    if (bestSoFar > alpha) {
                        alpha = bestSoFar;
                    }

                    if (beta < alpha) { /*System.out.println("Breaking");*/ break; }
                }

                if (bestIndex != -1) {
                    //System.out.println("The options I had at ply " + ply + " were " + moveStr);
                    //System.out.println("(Maximizing) They had these values: " + optionStr);
                    //System.out.println("I picked " + moves[bestIndex] + " with value " + bestSoFar);
                }
                else { // The only winning move is not to play
                    bestSoFar = alphabeta(board, ply - 1, alpha, beta, false, true);
                }
            }
            else {
                bestSoFar = Double.MAX_VALUE;
                // We need the other player's token
                char otherToken = (token == 'X') ? 'O' : 'X';

                // Generate all possible moves;
                String moveStr = board.getMoves(otherToken);
                String[] moves = moveStr.split(" ");
                String optionStr = "";
                
                // The board isn't full but the game is over. We just care about the pieces.
                if (moves.length == 0 && lastNoMoves) {
                    double tval = tokenVal(board);
                    
                    if (tval > 0.0) { // We are winning
                        return 1000.0;
                    }
                    else if (tval < 0.0) { // We are losing
                        return -1000.0;
                    }
                    
                    return 0.0;
                }

                //System.out.println("In alphabeta at a minimizing level. The ply is " + ply + ". These are the moves you can make: " + moveStr);

                boolean runOnce = true; // Might not need this.

                // If the player cannot move then this loop doesn't run
                for (int i = 0; i < moves.length /*&& (alpha <= beta || runOnce)*/; i++) {
                    //AlphaBetaBoard next = new AlphaBetaBoard(current.boardCopy(), current.moveCount);
                    Move move = null;

                    if (moves[i].length() != 0) {
                        move = board.makeMove(otherToken, moves[i]); // Make the move with their token
                    }

                    abresult = alphabeta(board, ply - 1, alpha, beta, true, false);
                    
                    if (moves[i].length() != 0) {
                        board.unmakeMove(move);
                    }
                    
                    optionStr += abresult + " ";

                    if (abresult < bestSoFar) {
                        bestSoFar = abresult;
                        bestIndex = i;
                    }

                    if (bestSoFar < beta) {
                        beta = bestSoFar;
                    }

                    if (beta < alpha) { /*System.out.println("Breaking");*/ break; }
                }

                if (bestIndex != -1) {
                    //System.out.println("The options you had at ply " + ply + " were " + moveStr);
                    //System.out.println("(Minimizing) They had these values: " + optionStr);
                    //System.out.println("You picked " + moves[bestIndex] + " with value " + bestSoFar);
                }
                else { // The only winning move is not to play
                    bestSoFar = alphabeta(board, ply - 1, alpha, beta, true, true);
                }


            }

            return bestSoFar;
        }
    }

    public String makeMove(AlphaBetaBoard board) {
        int ply = 4; // initial ply.

        if (60 - board.moveCount < ply) { // You don't want to look more if the game will be over.
            ply = 60 - board.moveCount;

            if (ply % 2 == 1) { // You don't want the ply to be odd because why would you do that?
                ply--;
            }
        }

        //AlphaBetaBoard current = new AlphaBetaBoard(board.boardCopy(), board.moveCount);

        double alpha = Double.MIN_VALUE;
        double beta = Double.MAX_VALUE;
        double bestSoFar = -Double.MAX_VALUE;
        int bestIndex = -1;
        double abresult;
        // Generate all possible moves;
        String moveStr = board.getMoves(token);
        //System.out.println("The board has this heuristic value: " + sbe(board));
        //System.out.println("I am " + token + " at the top level of ply " + ply + " and I can move in these places: " + moveStr);
        String[] moves = moveStr.split(" ");
        // There exists the posibility that you might not be able to make a move. In that case return null.
        if (moves.length == 0) {
            board.moveCount += 2;
            return "";
        }
        else if (moves.length == 1) { // If there is only one move then you don't want to waste time looking ahead.
            board.moveCount += 2;
            return moves[0];
        }

        // Every once in a while at the end of the game the AI will try to make a move at ply zero which will cause a stack overflow error.
        if (ply <= 0 && moves.length > 0) {
            
            for (int i = 0; i < moves.length; i++) {
                //AlphaBetaBoard next = new AlphaBetaBoard(current.boardCopy(), board.moveCount);
                Move move = board.makeMove(token, moves[i]);
                
                double sberesult = sbe(board);
                board.unmakeMove(move);
                
                if (sberesult > bestSoFar) {
                    bestSoFar = sberesult;
                    bestIndex = i;
                }
            }
            
            return moves[bestIndex];
        }
        
        String optionStr = "";

        // This will look through all the top moves at the very least because alpha is always less than beta.
        for (int i = 0; i < moves.length; i++) {
            //AlphaBetaBoard next = new AlphaBetaBoard(current.boardCopy(), board.moveCount);
            Move move = board.makeMove(token, moves[i]); // Make the move with our token
            abresult = alphabeta(board, ply - 1, alpha, beta, false, false);
            //System.out.println("abresult: " + abresult + " bestSoFar: " + bestSoFar);
            board.unmakeMove(move);
            optionStr += abresult + " ";

            if (abresult > bestSoFar) {
                bestSoFar = abresult;
                bestIndex = i;
            }/*
            else if (abresult == bestSoFar) { // Keep things interesting
                int random = (int) (Math.random() * 2.0);
                
                if (random == 1 && !(moves[i].equals("a1") || moves[i].equals("h1") || moves[i].equals("a8") || moves[i].equals("h8"))) { // Don't pass up a corner
                    bestIndex = i;
                }
            }
*/
            if (bestSoFar > alpha) {
                alpha = bestSoFar;
            }

            if (beta <= alpha) { /*System.out.println("Breaking")*/; break; }
        }
        
        // We have the move
        return moves[bestIndex];
    }

    /**
     * Every OthelloPlayer needs to specify how they will make a move.
     *
     * <p>This method will need to call board.makeMove() as part of it's
     * action to update the board and to get a valid Move object.</p>
     *
     * @return a Move object generated by board.makeMove()
     */
    public Move makeMove() {
        long startTime = System.currentTimeMillis();
        
        AlphaBetaBoard current = new AlphaBetaBoard(boardCopy(this.board), nummoves);
        current.xcount = tokenCount(current.grid, 'X');
        current.ocount = tokenCount(current.grid, 'O');
        current.boardWeight = getWeight(current.grid);
        //System.out.println("The weight before the choice is: " + current.boardWeight);
        String choice = makeMove(current);
        //System.out.println("I chose " + choice);
        //System.out.println("xcount: " + current.xcount + " ocount: " + current.ocount);
        /*
        current.sockToken = this.token;
        
        String chosenOne = "";
             
        System.out.println("Sending board.");
        
        String boardStr = getBoardStr(current);
        
        out.println(boardStr);
        out.flush();
        
        // Get the results.
        System.out.println("Done. Waiting for results...");

        chosenOne = in.nextLine();
        */
        //System.out.println("I picked " + choice);

        // Add 2 to moves to keep an accurate count of moves made.        
        nummoves += 2;
        long endTime = System.currentTimeMillis();
        long timeTaken = (endTime - startTime);
        //timeTaken /= 1000.0;
        //System.out.println("Choice made in " + timeTaken + "ms");
        
        Move bestChoice;
        int afterweight;
        
        if (!choice.equals("")) {
            bestChoice = board.makeMove(token, choice);
            current.makeMove(token, choice);
            afterweight = getWeight(current.grid);
            //System.out.println("The weight after the choice is: " + afterweight);
            //System.out.println("xcount: " + current.xcount + " ocount: " + current.ocount);
        }
        else {
            bestChoice = null;
        }

        if (timeTaken > 10000) {
            overCount++;
        }
        
        //System.out.println("Went over " + overCount + " times.");
        
        

        return bestChoice;
    }
}

class AlphaBetaBoard {
    int moveCount;
    char[][] grid;
    char sockToken;
    int xcount;
    int ocount;
    int boardWeight;
    int[][] boardWeights = {
        {100, -10, 11,  6,  6, 11, -10, 100},
        {-10, -20,  1,  2,  2,  1, -20, -10},
        { 10,   1,  5,  4,  4,  5,   1,  10},
        {  6,   2,  4,  2,  2,  4,   2,   6},
        {  6,   2,  4,  2,  2,  4,   2,   6},
        { 10,   1,  5,  4,  4,  5,   1,  10},
        {-10, -20,  1,  2,  2,  1, -20, -10},
        {100, -10, 11,  6,  6, 11, -10, 100}
    };

    public AlphaBetaBoard(char[][] g, int mc) {
        grid = g;
        moveCount = mc;
    }

	/**
	 * Display the grid along with a key for aiding user input.
	 */
    public void display()
	{
		int i,j;
		System.out.print("   ");
		for (i=0; i<8; i++)
			System.out.printf(" %c",(char)(i+97));
		System.out.println();

		for (i=0; i<8; i++) {
			System.out.printf(" %d",i+1);
			for (j=0; j<8; j++)
				System.out.printf(" %c",grid[i][j]);
			System.out.printf("  %d",i+1);
			System.out.println();
		}

		System.out.print("   ");
		for (i=0; i<8; i++)
			System.out.printf(" %c",(char)(i+97));
		System.out.println();
	}


	/**
	 * Attempt to make a move in the given position.
	 *
	 * @param token the token/piece to be placed on the grid if a good request.
	 * @param i the row in which we are placing the piece
	 * @param j the column in which we are placing the piece
	 * @return a Move object that contains details of the successful move; null
	 * if the move is not possible
	 *
	 * <p>This method name is overloaded with various parameter options, all of
	 * which call this version of the method.</p>
	 */
	public Move makeMove(char token, int i, int j)
	{
		int totalChange;
		Move move= new Move(i,j,token);

		totalChange= 0;
		totalChange+= capture(move,+1,+0); // down
		totalChange+= capture(move,-1,+0); // up
		totalChange+= capture(move,+0,+1); // right
		totalChange+= capture(move,+0,-1); // left
		totalChange+= capture(move,-1,+1); // diag up-right
		totalChange+= capture(move,+1,+1); // diag down-right
		totalChange+= capture(move,-1,-1); // diag up-left
		totalChange+= capture(move,+1,-1); // diag down-left

		if (totalChange>0) {
			grid[i][j]= token;
			
			if (token == 'X') {
			    xcount++;
			    boardWeight += boardWeights[i][j];
			}
			else {
			    ocount++;
			    boardWeight -= boardWeights[i][j];
			}
			
			moveCount++;
			return move;
		}
		return null;
	}



	/**
	 * Attempt to make a move in the given position.
	 *
	 * @param token the token/piece to be placed on the grid if a good request.
	 * @param theMove is a string with letter/number combination (e.g., c4)
	 * @return a Move object that contains details of the successful move; null
	 * if the move is not possible
	 */
	public Move makeMove(char token, String theMove)
	{
		char col= Character.toLowerCase(theMove.charAt(0));
		return makeMove(token,(int)theMove.charAt(1)-(int)'0'-1,(int) col - (int) 'a');
	}

    public void unmakeMove(Move move)
    {
        int a,b,i,n;
        int ioff,joff;
        char otherToken= move.token=='X' ? 'O' : 'X';

        grid[move.i][move.j]= ' ';
        
        if (move.token == 'X') {
            xcount--;
            boardWeight -= boardWeights[move.i][move.j];
        }
        else {
            ocount--;
            boardWeight += boardWeights[move.i][move.j];
        }
        
        for (i=0; i<8 && move.captures[i]!=null; i++) {
            n= move.captures[i].n;
            ioff= move.captures[i].ioff;
            joff= move.captures[i].joff;
            for (a=move.i+ioff, b=move.j+joff; n>0; a+=ioff, b+=joff) {
                grid[a][b]= otherToken;
                n--;
                
                if (otherToken == 'X') { // The move made was O's
                    xcount++;
                    ocount--;
                    boardWeight += 2 * boardWeights[a][b];
                }
                else { // The move made was X's
                    ocount++;
                    xcount--;
                    boardWeight -= 2 * boardWeights[a][b];
                }
            }
        }
        moveCount--;
    }

	/**
	 * Attempt to capture pieces in the specified direction.
	 *
	 * @param move a Move object representing the move from which we are capturing
	 * @param ioff row-wise direction change (+1, 0, or -1) for capture direction
	 * @param joff col-wise direction change (+1, 0, or -1) for capture direction
	 * @return the number of pieces successfully captured; 0 if capture not possible
	 */
	private int capture(Move move, int ioff, int joff)
	{
		int a,b,n=0;
		char otherToken= move.token=='X' ? 'O' : 'X';

		// try capturing in the current direction, changing discs as we go
		for (a=move.i+ioff, b=move.j+joff; a>=0 && a<8 && b>=0 && b<8 && grid[a][b]==otherToken; a+=ioff, b+=joff) { // This flips pieces
			grid[a][b]= move.token;
			n++;
			
			if (move.token == 'X') {
			    ocount--;
			    xcount++;
			    boardWeight += 2 * boardWeights[a][b];
			}
			else {
			    xcount--;
			    ocount++;
			    boardWeight -= 2 * boardWeights[a][b];
			}
		}

		// did we run off edge of grid or encounter a blank when
		// we were turning over pieces? If so, we have to restore
		// those slots to what they were before.
		if (a<0 || a>7 || b<0 || b>7 || grid[a][b]==' ') {
			for (a-=ioff, b-=joff; n>0; a-=ioff, b-=joff, n--) {
				grid[a][b]= otherToken;
				
				if (otherToken == 'X') {
				    xcount++;
				    ocount--;
				}
				else {
				    ocount++;
				    xcount--;
				}
			}
		}

		if (n>0) move.addCapture(ioff,joff,n);
		return n;
	}

    // Just in case I need these

    public int countCaptures(char otherToken, int i, int j, int ioff, int joff) {
        int a,b,n=0;

        for (a=i+ioff, b=j+joff; a>=0 && a<8 && b>=0 && b<8 && this.grid[a][b]==otherToken; a+=ioff, b+=joff)
            n++;
        if (a<0 || a>7 || b<0 || b>7 || this.grid[a][b]==' ') return 0;
        return n;
    }
    
	/**
	 * Determine how many moves are available to given player.
	 *
	 * @param token the token used by player we are evaluating
	 * @param showMoves lists moves as we find them if true; no output if false
	 * @return number of moves available to token
	 */
	public int countPossibleMoves(char token, boolean showMoves)
	{
		int i,j;
		char otherToken= token=='X' ? 'O' : 'X';
		int n,sum=0;

		if (showMoves)
			System.out.print("Possible Moves:");
		for (i=0; i<8; i++) {
			for (j=0; j<8; j++) {
				if (grid[i][j]==' ') {
					n= 0;
					n+= countCaptures(otherToken,i,j,+1,+1);
					n+= countCaptures(otherToken,i,j,+1,+0);
					n+= countCaptures(otherToken,i,j,+1,-1);
					n+= countCaptures(otherToken,i,j,+0,-1);
					n+= countCaptures(otherToken,i,j,+0,+1);
					n+= countCaptures(otherToken,i,j,-1,+1);
					n+= countCaptures(otherToken,i,j,-1,+0);
					n+= countCaptures(otherToken,i,j,-1,-1);
					if (showMoves && n>0)
						System.out.print(" "+translateMove(i,j));
					sum+= n>0 ? 1 : 0;
				}
			}
		}
		if (showMoves)
			System.out.println();
		return sum;
	}

	/**
	 * Determine whether specified player can move at position.
	 *
	 * @param token the token used by player we are evaluating
	 * @param i row of position
	 * @param j column of position
	 * @return true if token can be placed at i,j; false if not
	 */
	public boolean canMove(char token, int i, int j)
	{
		char otherToken= token=='X' ? 'O' : 'X';
		int n=0;

		if (grid[i][j]==' ') {
			n+= countCaptures(otherToken,i,j,+1,+1);
			n+= countCaptures(otherToken,i,j,+1,+0);
			n+= countCaptures(otherToken,i,j,+1,-1);
			n+= countCaptures(otherToken,i,j,+0,-1);
			n+= countCaptures(otherToken,i,j,+0,+1);
			n+= countCaptures(otherToken,i,j,-1,+1);
			n+= countCaptures(otherToken,i,j,-1,+0);
			n+= countCaptures(otherToken,i,j,-1,-1);
		}
		return n>0;
	}


	/**
	 * Determine whether specified player can move at position.
	 *
	 * @param token the token used by player we are evaluating
	 * @param theMove is a string with letter/number combination (e.g., c4)
	 * @return true if token can be placed at i,j; false if not
	 */
	public boolean canMove(char token, String theMove)
	{
		char col= Character.toLowerCase(theMove.charAt(0));
		return canMove(token,(int)theMove.charAt(1)-(int)'0'-1,(int) col - (int) 'a');
	}


	/**
	 * Given row/col values turn into letter/number combination.
	 *
	 * @param i row of position
	 * @param j column of position
	 * @return letter/number equivalent of position (i,j)
	 */
	public static String translateMove(int i, int j)
	{
		return ""+(char)((char)j+(char)'a')+(char)((char)(i+1)+(char)'0');
	}

    public String getMoves(char targetToken) {
        char otherToken= targetToken=='X' ? 'O' : 'X';
        String moves = "";
        int n;

        for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
                if (this.grid[i][j]==' ') {
	                n= 0;
	                n+= countCaptures(otherToken,i,j,+1,+1);
	                n+= countCaptures(otherToken,i,j,+1,+0);
	                n+= countCaptures(otherToken,i,j,+1,-1);
	                n+= countCaptures(otherToken,i,j,+0,-1);
	                n+= countCaptures(otherToken,i,j,+0,+1);
	                n+= countCaptures(otherToken,i,j,-1,+1);
	                n+= countCaptures(otherToken,i,j,-1,+0);
	                n+= countCaptures(otherToken,i,j,-1,-1);

                    if (n>0) {
		                moves += " " + translateMove(i,j);
                    }
                }
            }
        }
        
        try {
            moves = moves.substring(1, moves.length());
        }
        catch (StringIndexOutOfBoundsException e) {
            moves = "";
        }

        return moves;
    }

    public char[][] boardCopy() {
        char[][] copy = new char[grid.length][grid[0].length];

        for (int row = 0; row < copy.length; row++) {
            for (int col = 0; col < copy[0].length; col++) {
                copy[row][col] = grid[row][col];
            }
        }

        return copy;
    }
}

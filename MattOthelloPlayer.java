/**
 * Defines a base OthelloPlayer class that specifies the board and token.
 *
 * @author  T.Sergeant
 * @version For AI
 *
*/
public class MattOthelloPlayer extends OthelloPlayer
{
    //protected Board board; // the board the player is using
    //protected char token;  // the token the player is using
    int nummoves;

    // My own version of board with more public methods to get around the fact that I can't edit Board.java
    

    /**
     * Set the board and token.
     */
    public MattOthelloPlayer(Board board, char token) {
        super(board, token);

        if (token == 'X') {
            nummoves = 0;
        }
        else {
            nummoves = 1;
        }
    }

    /**
     * We represent the player by their token.
     */
    @Override
    public String toString()
    {
        return ""+token;
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

    public int tokenCount(char[][] grid, char token) {
        int count = 0;

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] == token) {
                    count++;
                }
            }
        }

        return count;
    }

    public int sbe(Board board) {
        int xcount = tokenCount(board.grid, 'X');
        int ocount = tokenCount(board.grid, 'O');
        int h = 0; // H for heuristic value

        if (token == 'X') {
            h = xcount - ocount;
        }
        else {
            h = ocount - xcount;
        }

        return h;
    }

    // This returns the SBE of the best move
    public int alphabeta(Board board, int ply, int alpha, int beta, boolean maxLevel) {
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
            int bestSoFar = 0;
            AlphaBetaBoard current = new AlphaBetaBoard(boardCopy(board), nummoves); // might not need moves
            int abresult;
            int bestIndex = -1;

            if (maxLevel) {
                bestSoFar = Integer.MIN_VALUE;
                // Generate all possible moves;
                String moveStr = current.getMoves(token);
                String[] moves = moveStr.split(" ");
                String optionStr = "";

                //System.out.println("In alphabeta at a maximizing level. The ply is " + ply + ". These are the moves I can make: " + moveStr);

                boolean runOnce = true; // Might not need this.

                // If the player cannot move then this loop doesn't run
                for (int i = 0; i < moves.length /*&& (alpha <= beta || runOnce)*/; i++) {
                    AlphaBetaBoard next = new AlphaBetaBoard(current.boardCopy(), nummoves);

                    if (moves[i].length() != 0) {
                        next.makeMove(token, moves[i]); // Make the move with our token
                    }

                    abresult = alphabeta(next, ply - 1, alpha, beta, false);
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
                    bestSoFar = alphabeta(current, ply - 1, alpha, beta, false);
                }
            }
            else {
                bestSoFar = Integer.MAX_VALUE;
                // We need the other player's token
                char otherToken = (token == 'X') ? 'O' : 'X';

                // Generate all possible moves;
                String moveStr = current.getMoves(otherToken);
                String[] moves = moveStr.split(" ");
                String optionStr = "";

                //System.out.println("In alphabeta at a minimizing level. The ply is " + ply + ". These are the moves you can make: " + moveStr);

                boolean runOnce = true; // Might not need this.

                // If the player cannot move then this loop doesn't run
                for (int i = 0; i < moves.length /*&& (alpha <= beta || runOnce)*/; i++) {
                    AlphaBetaBoard next = new AlphaBetaBoard(current.boardCopy(), nummoves);

                    if (moves[i].length() != 0) {
                        next.makeMove(otherToken, moves[i]); // Make the move with their token
                    }

                    abresult = alphabeta(next, ply - 1, alpha, beta, true);
                    optionStr += abresult + " ";

                    if (abresult < bestSoFar) {
                        bestSoFar = abresult;
                        bestIndex = i;
                    }

                    if (bestSoFar < beta) {
                        beta = bestSoFar;
                    }

                    if (beta <= alpha) { /*System.out.println("Breaking");*/ break; }
                }

                if (bestIndex != -1) {
                    //System.out.println("The options you had at ply " + ply + " were " + moveStr);
                    //System.out.println("(Minimizing) They had these values: " + optionStr);
                    //System.out.println("You picked " + moves[bestIndex] + " with value " + bestSoFar);
                }
                else { // The only winning move is not to play
                    bestSoFar = alphabeta(current, ply - 1, alpha, beta, true);
                }


            }

            return bestSoFar;
        }
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
        int ply = 10; // initial ply.

        if (60 - nummoves < ply) { // You don't want to look more if the game will be over.
            ply = 60 - nummoves;

            if (ply % 2 == 1) { // You don't want the ply to be odd because why would you do that?
                ply--;
            }
        }

        AlphaBetaBoard current = new AlphaBetaBoard(boardCopy(this.board), nummoves);

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int bestSoFar = Integer.MIN_VALUE;
        int bestIndex = -1;
        int abresult;
        // Generate all possible moves;
        String moveStr = current.getMoves(token);
        //System.out.println("The board has this heuristic value: " + sbe(current));
        //System.out.println("I am " + token + " at the top level of ply " + ply + " and I can move in these places: " + moveStr);
        String[] moves = moveStr.split(" ");

        // There exists the posibility that you might not be able to make a move. In that case return null.
        if (moves.length == 0) {
            nummoves += 2;
            return null;
        }
        else if (moves.length == 1) { // If there is only one move then you don't want to waste time looking ahead.
            nummoves += 2;
            return board.makeMove(token, moves[0]);
        }

        String optionStr = "";

        // This will look through all the top moves at the very least because alpha is always less than beta.
        for (int i = 0; i < moves.length; i++) {
            AlphaBetaBoard next = new AlphaBetaBoard(current.boardCopy(), nummoves);
            next.makeMove(token, moves[i]); // Make the move with our token
            abresult = alphabeta(next, ply - 1, alpha, beta, false);
            optionStr += abresult + " ";

            if (abresult > bestSoFar) {
                bestSoFar = abresult;
                bestIndex = i;
            }

            if (bestSoFar > alpha) {
                alpha = bestSoFar;
            }

            if (beta <= alpha) { /*System.out.println("Breaking")*/; break; }
        }
        
        // We have the move
        //System.out.println("The options I had at ply " + ply + " were " + moveStr);
        //System.out.println("(Maximizing) They had these values: " + optionStr);
        System.out.println("I picked " + moves[bestIndex] + " with value " + bestSoFar);
        Move bestChoice = board.makeMove(token, moves[bestIndex]);
        // This is the end of the method. Add 2 to moves to keep an accurate count of moves made.
        nummoves += 2;
        long endTime = System.currentTimeMillis();
        long timeTaken = (endTime - startTime);
        //timeTaken /= 1000.0;
        System.out.println("Choice made in " + timeTaken + "ms");
        return bestChoice;
    }
}

class AlphaBetaBoard extends Board {
    //char[][] grid;
    int moveCount;

    public AlphaBetaBoard(char[][] g, int mc) {
        this.grid = g;
        moveCount = mc;
    }

    // Just in case I need these

    public int countCaptures(char otherToken, int i, int j, int ioff, int joff) {
        int a,b,n=0;

        for (a=i+ioff, b=j+joff; a>=0 && a<8 && b>=0 && b<8 && this.grid[a][b]==otherToken; a+=ioff, b+=joff)
            n++;
        if (a<0 || a>7 || b<0 || b>7 || this.grid[a][b]==' ') return 0;
        return n;
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
        char[][] copy = new char[this.grid.length][this.grid[0].length];

        for (int row = 0; row < copy.length; row++) {
            for (int col = 0; col < copy[0].length; col++) {
                copy[row][col] = this.grid[row][col];
            }
        }

        return copy;
    }
}

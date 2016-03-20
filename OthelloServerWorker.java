import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.EOFException;

public class OthelloServerWorker implements Runnable {
    
    public Socket socket;
    public ObjectInputStream in;
    public PrintWriter out;
    public char token;

    public OthelloServerWorker(Socket s) throws IOException {
        socket = s;
        in = new ObjectInputStream(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream());
    }
    
    public void run() {
        while (true) {
            try {                
                // Get the board
                AlphaBetaBoard board = (AlphaBetaBoard) in.readObject();
                token = board.sockToken;
                System.out.println("Got a board.");
                board.display();
                
                // Make the choice
                String choice = makeMove(board);
                System.out.println("I chose " + choice);
                // Send the choice
                System.out.println("Sending choice...");
                out.println(choice);
                out.flush();
            }
            catch (EOFException eof) {
                try {
                    in.close();
                    out.close();
                    socket.close();
                    System.out.println("Game over");
                    break;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
            catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
    }

    // Done
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
    // Done
    public int sbe(AlphaBetaBoard board) {
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
    public int alphabeta(AlphaBetaBoard board, int ply, int alpha, int beta, boolean maxLevel) {
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
            AlphaBetaBoard current = new AlphaBetaBoard(board.boardCopy(), board.moveCount); // might not need moves
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
                    AlphaBetaBoard next = new AlphaBetaBoard(current.boardCopy(), current.moveCount);

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
                    AlphaBetaBoard next = new AlphaBetaBoard(current.boardCopy(), current.moveCount);

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

    public String makeMove(AlphaBetaBoard board) {
        int ply = 10; // initial ply.

        if (60 - board.moveCount < ply) { // You don't want to look more if the game will be over.
            ply = 60 - board.moveCount;

            if (ply % 2 == 1) { // You don't want the ply to be odd because why would you do that?
                ply--;
            }
        }

        AlphaBetaBoard current = new AlphaBetaBoard(board.boardCopy(), board.moveCount);

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
            board.moveCount += 2;
            return "";
        }
        else if (moves.length == 1) { // If there is only one move then you don't want to waste time looking ahead.
            board.moveCount += 2;
            return moves[0];
        }

        String optionStr = "";

        // This will look through all the top moves at the very least because alpha is always less than beta.
        for (int i = 0; i < moves.length; i++) {
            AlphaBetaBoard next = new AlphaBetaBoard(current.boardCopy(), board.moveCount);
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
        return moves[bestIndex];
    }
}

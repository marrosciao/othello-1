import java.io.*;
import java.net.*;
import java.util.*;

public class OthelloTestClient {

    public static AlphaBetaBoard board;
    public static char token = 'X';

    public static void main(String[] args) {
        char[][] grid = {{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                         {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                         {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                         {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                         {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                         {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                         {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                         {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}};
        grid[3][3]= 'X';
		grid[4][4]= 'X';
		grid[3][4]= 'O';
		grid[4][3]= 'O';
            
        board = new AlphaBetaBoard(grid, 0);
        board.display();
        board.sockToken = token;
        Scanner in;
        //ObjectOutputStream out;
        PrintWriter out;
        
        try {
            Socket socket = new Socket("localhost", 12321);
            System.out.println("Connected.");
            //out = new ObjectOutputStream(socket.getOutputStream());
            out = new PrintWriter(socket.getOutputStream());
            System.out.println("Sending board.");
            //board.display();
            //out.writeObject(board);
            
            // Construct the board string
            String boardStr = "";
            
            for (int i = 0; i < board.grid.length; i++) {
                boardStr += String.valueOf(board.grid[i]);
            }
            
            boardStr += "_";
            boardStr += String.valueOf(board.sockToken);
            boardStr += "_";
            boardStr += String.valueOf(board.moveCount);
            
            // The string is ready to send
            out.println(boardStr);
            
            // Get the results.
            //System.out.println("Done. Waiting for results...");
            //in = new Scanner(socket.getInputStream());
            //String move = in.nextLine();
            //System.out.println("The move is " + move);
            //in.close();
            out.close();
            socket.close();
        }
        catch (SocketException se) {
            se.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}

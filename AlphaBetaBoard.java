/*
 * An older programmer once told me that object oriented programming was the work of satan. I now understand.
 */

public class AlphaBetaBoard {
    int moveCount;
    char[][] grid;
    char sockToken;

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

	/**
	 * Attempt to make a move given a Move object.
	 *
	 * @param move a Move object containing the move to be made
	 * @return a Move object that contains details of the successful move; null
	 * if the move is not possible
	 */
	public Move makeMove(Move move)
	{
		return makeMove(move.token,move.i,move.j);
	}


	/**
	 * Undo all captures and remove piece from a board.
	 *
	 * @param move a Move object containing the move to be unmade
	 */
	public void unMakeMove(Move move)
	{
		int a,b,i,n;
		int ioff,joff;
		char otherToken= move.token=='X' ? 'O' : 'X';

		grid[move.i][move.j]= ' ';
		for (i=0; i<8 && move.captures[i]!=null; i++) {
			n= move.captures[i].n;
			ioff= move.captures[i].ioff;
			joff= move.captures[i].joff;
			for (a=move.i+ioff, b=move.j+joff; n>0; a+=ioff, b+=joff) {
				grid[a][b]= otherToken;
				n--;
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
		for (a=move.i+ioff, b=move.j+joff; a>=0 && a<8 && b>=0 && b<8 && grid[a][b]==otherToken; a+=ioff, b+=joff) {
			grid[a][b]= move.token;
			n++;
		}

		// did we run off edge of grid or encounter a blank when
		// we were turning over pieces? If so, we have to restore
		// those slots to what they were before.
		if (a<0 || a>7 || b<0 || b>7 || grid[a][b]==' ') {
			for (a-=ioff, b-=joff; n>0; a-=ioff, b-=joff, n--) {
				grid[a][b]= otherToken;
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
	 * Display results of game (so far or at end of game).
	 */
	public void showResults()
	{
		int i,j;
		int xcount, ocount;

		if (!gameOver())
			System.out.print("Current Standing: ");
		else
			System.out.print("Final Results   : ");

		xcount= ocount= 0;

		for (i=0; i<8; i++)
			for (j=0; j<8; j++)
				if (grid[i][j]=='X')
					xcount++;
				else if (grid[i][j]=='O')
					ocount++;
		System.out.println("X("+xcount+") - O("+ocount+")");
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


	/**
	 * Determine if game is over or not.
	 *
	 * @return true if board is full or no moves possible; false otherwise
	 */
	public boolean gameOver()
	{
		return moveCount==64 || (countPossibleMoves('O',false)==0 && countPossibleMoves('X',false)==0);
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

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stddef.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <pthread.h>

int false = 0;
int true = 1;

typedef struct {
    char s[64];
    char * sp;
    int length;
} String;

typedef struct {
    char grid[8][8];
} Grid;

// Capture class and methods
typedef struct {
    int ioff;
    int joff;
    int n;
} Capture;

Capture new_capture(int ioff, int joff, int n) {
    Capture new_c;
    new_c.ioff = ioff;
    new_c.joff = joff;
    new_c.n = n;
    
    return new_c;
}

// Move class and methods
typedef struct {
    int i;
    int j;
    char token;
    Capture captures[8];
    int n;
} Move;

Move new_move(int i, int j, char token) {
    Move new_m;
    new_m.i = i;
    new_m.j = j;
    new_m.token = token;
    new_m.n = 0;
    
    return new_m;
}

void move_add_capture(Move * this, int ioff, int joff, int num) {
    this->captures[this->n] = new_capture(ioff, joff, num);
    this->n++;
}

// AlphaBetaBoard class and methods
typedef struct {
    Grid grid;
    int moveCount;
} AlphaBetaBoard;

AlphaBetaBoard new_AlphaBetaBoard(Grid g, int mc) {
    AlphaBetaBoard board;
    board.grid = g;
    board.moveCount = mc;
    
    return board;
}

void AlphaBetaBoard_display(AlphaBetaBoard * this) {
    int i,j;
    printf("   ");
    
    for (i=0; i<8; i++)
        printf(" %c",(char)(i+97));
    
    printf("\n");

    for (i=0; i<8; i++) {
        printf(" %d",i+1);
        for (j=0; j<8; j++)
            printf(" %c",this->grid.grid[i][j]);
        printf("  %d",i+1);
        printf("\n");
    }

    printf("   ");
    for (i=0; i<8; i++)
        printf(" %c",(char)(i+97));
    printf("\n");
}

Grid AlphaBetaBoard_board_copy(AlphaBetaBoard * this) {
    Grid grid;
    int row, col;

    for (row = 0; row < 8; row++) {
        for (col = 0; col < 8; col++) {
            grid.grid[row][col] = this->grid.grid[row][col];
        }
    }

    return grid;
}

int AlphaBetaBoard_count_captures(AlphaBetaBoard * this, char otherToken, int i, int j, int ioff, int joff) {
    int a,b,n=0;

    for (a=i+ioff, b=j+joff; a>=0 && a<8 && b>=0 && b<8 && this->grid.grid[a][b]==otherToken; a+=ioff, b+=joff)
        n++;
        
    if (a<0 || a>7 || b<0 || b>7 || this->grid.grid[a][b]==' ') return 0;
    return n;
}

int AlphaBetaBoard_capture(AlphaBetaBoard * this, Move * move, int ioff, int joff) {
    int a,b,n=0;
    char otherToken= move->token=='X' ? 'O' : 'X';

    // try capturing in the current direction, changing discs as we go
    for (a=move->i+ioff, b=move->j+joff; a>=0 && a<8 && b>=0 && b<8 && this->grid.grid[a][b]==otherToken; a+=ioff, b+=joff) {
        this->grid.grid[a][b]= move->token;
        n++;
    }

    // did we run off edge of grid or encounter a blank when
    // we were turning over pieces? If so, we have to restore
    // those slots to what they were before.
    if (a<0 || a>7 || b<0 || b>7 || this->grid.grid[a][b]==' ') {
        for (a-=ioff, b-=joff; n>0; a-=ioff, b-=joff, n--) {
            this->grid.grid[a][b]= otherToken;
        }
    }

    if (n>0) move_add_capture(move, ioff,joff,n);
    return n;
}

String AlphaBetaBoard_translate_move(int i, int j) {
    String moveStr;
    moveStr.s[0] = (char)((char)j+(char)'a');
    moveStr.s[1] = (char)((char)(i+1)+(char)'0');
    moveStr.length = 2;
    
    return moveStr;
}

String AlphaBetaBoard_get_moves(AlphaBetaBoard * this, char targetToken) {
    char otherToken= targetToken=='X' ? 'O' : 'X';
    String moves;
    int n;
    int numMoves = 0;
    int i, j;
    //Grid grid = this->grid;
    
    for (i=0; i<8; i++) {
        for (j=0; j<8; j++) {
            if (this->grid.grid[i][j]==' ') {
                n= 0;
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+1,+1);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+1,+0);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+1,-1);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+0,-1);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+0,+1);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,-1,+1);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,-1,+0);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,-1,-1);
                
                if (n>0) {
                    String tmp = AlphaBetaBoard_translate_move(i,j);
                    if (numMoves != 0) {
                        strncat(moves.s, " ", 1);
                        moves.length++;
                    }
                    
                    strncat(moves.s, tmp.s, 2);
                    moves.length += 2;
                    numMoves++;
                }
            }
        }
    }

    // If this method finishes and there were no moves then we want the entire string to be spaces.

    if (numMoves == 0) {
        moves.s[0] = ' ';
        moves.s[1] = ' ';
        moves.length = 2;
    }
    printf("moves: %*.*s\n", moves.length, moves.length, moves.s);
    
    return moves;
}

Move * AlphaBetaBoard_make_move_ij(AlphaBetaBoard * this, char token, int i, int j) {
    int totalChange;
    Move move = new_move(i,j,token);
    Move * move_ptr = &move;

    totalChange= 0;
    totalChange+= AlphaBetaBoard_capture(this, move_ptr,+1,+0); // down
    totalChange+= AlphaBetaBoard_capture(this, move_ptr,-1,+0); // up
    totalChange+= AlphaBetaBoard_capture(this, move_ptr,+0,+1); // right
    totalChange+= AlphaBetaBoard_capture(this, move_ptr,+0,-1); // left
    totalChange+= AlphaBetaBoard_capture(this, move_ptr,-1,+1); // diag up-right
    totalChange+= AlphaBetaBoard_capture(this, move_ptr,+1,+1); // diag down-right
    totalChange+= AlphaBetaBoard_capture(this, move_ptr,-1,-1); // diag up-left
    totalChange+= AlphaBetaBoard_capture(this, move_ptr,+1,-1); // diag down-left

    if (totalChange>0) {
        this->grid.grid[i][j]= token;
        this->moveCount++;
        return move_ptr;
    }
    
    return NULL;
}

Move * AlphaBetaBoard_make_move_str(AlphaBetaBoard * this, char token, String theMove) {
    char col = theMove.s[0];
    return AlphaBetaBoard_make_move_ij(this, token, (int)theMove.s[1]-(int)'0'-1, (int) col - (int) 'a');
}

// Worker Methods

int tokenCount(Grid grid, char targetToken) {
    int count = 0;
    int row, col;

    for (row = 0; row < 8; row++) {
        for (col = 0; col < 8; col++) {
            if (grid.grid[row][col] == targetToken) {
                count++;
            }
        }
    }

    return count;
}

int sbe(char token, AlphaBetaBoard * board) {
    int xcount = tokenCount(board->grid, 'X');
    int ocount = tokenCount(board->grid, 'O');
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
int alphabeta(char token, AlphaBetaBoard * board, int ply, int alpha, int beta, int maxLevel) {
    if (ply == 0) {
        return sbe(token, board);
    }
    else {
        int bestSoFar = 0;
        AlphaBetaBoard current = new_AlphaBetaBoard(AlphaBetaBoard_board_copy(board), board->moveCount); // might not need moves
        int abresult;
        int bestIndex = -1;

        if (maxLevel) {
            bestSoFar = -100000;
            // Generate all possible moves;
            String moveStr = AlphaBetaBoard_get_moves(&current, token);
            int numMoves = (moveStr.length + 1) / 3;
            //String optionStr = "";

            //System.out.println("In alphabeta at a maximizing level. The ply is " + ply + ". These are the moves I can make: " + moveStr);

            // If the player cannot move then this loop doesn't run
            int i;
            for (i = 0; i < numMoves; i++) {
                AlphaBetaBoard next = new_AlphaBetaBoard(AlphaBetaBoard_board_copy(&current), current.moveCount);
                int movePos = i * 3;
                String thisMove;
                thisMove.s[0] = moveStr.s[movePos];
                thisMove.s[1] = moveStr.s[movePos+1];
                thisMove.length = 2;

                if (thisMove.s[0] != ' ' && thisMove.s[1] != ' ') { // If the move was just spaces then there was no move to make and this doesn't happen
                    AlphaBetaBoard_make_move_str(&next, token, thisMove); // Make the move with our token
                }

                abresult = alphabeta(token, &next, ply - 1, alpha, beta, false);
                //optionStr += abresult + " ";

                if (abresult > bestSoFar) {
                    bestSoFar = abresult;
                    bestIndex = i;
                }

                if (bestSoFar > alpha) {
                    alpha = bestSoFar;
                }

                if (beta < alpha) { break; }
            }

            if (bestIndex != -1) {
                //System.out.println("The options I had at ply " + ply + " were " + moveStr);
                //System.out.println("(Maximizing) They had these values: " + optionStr);
                //System.out.println("I picked " + moves[bestIndex] + " with value " + bestSoFar);
            }
            else { // The only winning move is not to play
                bestSoFar = alphabeta(token, &current, ply - 1, alpha, beta, false);
            }
        }
        else {
            bestSoFar = 100000;
            // We need the other player's token
            char otherToken = (token == 'X') ? 'O' : 'X';

            // Generate all possible moves;
            String moveStr = AlphaBetaBoard_get_moves(&current, otherToken);
            int numMoves = (moveStr.length + 1) / 3;
            //String optionStr = "";

            //System.out.println("In alphabeta at a minimizing level. The ply is " + ply + ". These are the moves you can make: " + moveStr);

            // If the player cannot move then this loop doesn't run
            int i;
            for (i = 0; i < numMoves; i++) {
                AlphaBetaBoard next = new_AlphaBetaBoard(AlphaBetaBoard_board_copy(&current), current.moveCount);
                int movePos = i * 3;
                String thisMove;
                thisMove.s[0] = moveStr.s[movePos];
                thisMove.s[1] = moveStr.s[movePos+1];
                thisMove.length = 2;
                
                if (thisMove.s[0] != ' ' && thisMove.s[1] != ' ') { // If the move was just spaces then there was no move to make and this doesn't happen
                    AlphaBetaBoard_make_move_str(&next, otherToken, thisMove); // Make the move with their token
                }

                abresult = alphabeta(token, &next, ply - 1, alpha, beta, true);
                //optionStr += abresult + " ";

                if (abresult < bestSoFar) {
                    bestSoFar = abresult;
                    bestIndex = i;
                }

                if (bestSoFar < beta) {
                    beta = bestSoFar;
                }

                if (beta <= alpha) { break; }
            }

            if (bestIndex != -1) {
                //System.out.println("The options you had at ply " + ply + " were " + moveStr);
                //System.out.println("(Minimizing) They had these values: " + optionStr);
                //System.out.println("You picked " + moves[bestIndex] + " with value " + bestSoFar);
            }
            else { // The only winning move is not to play
                bestSoFar = alphabeta(token, &current, ply - 1, alpha, beta, true);
            }


        }

        return bestSoFar;
    }
}

String makeMove(char token, AlphaBetaBoard * board) {
    int ply = 10; // initial ply.

    if (60 - board->moveCount < ply) { // You don't want to look more if the game will be over.
        ply = 60 - board->moveCount;

        if (ply % 2 == 1) { // You don't want the ply to be odd because why would you do that?
            ply--;
        }
    }

    AlphaBetaBoard current = new_AlphaBetaBoard(AlphaBetaBoard_board_copy(board), board->moveCount);

    int alpha = -100000;
    int beta = 100000;
    int bestSoFar = 100000;
    int bestIndex = -1;
    int abresult;
    
    String chosenOne;

    // Generate all possible moves;
    String moveStr = AlphaBetaBoard_get_moves(&current, token);
    int numMoves = (moveStr.length + 1) / 3;
    //printf("The board has this heuristic value: %d\n", sbe(token, &current));
    //System.out.println("I am " + token + " at the top level of ply " + ply + " and I can move in these places: " + moveStr);
    return chosenOne;
    // There exists the posibility that you might not be able to make a move. In that case return null.
    if (numMoves == 0) {
        board->moveCount += 2;
        chosenOne.s[0] = ' ';
        chosenOne.s[1] = ' ';
        chosenOne.length = 2;
        return chosenOne;
    }
    else if (numMoves == 1) { // If there is only one move then you don't want to waste time looking ahead.
        board->moveCount += 2;
        chosenOne.s[0] = moveStr.s[0];
        chosenOne.s[1] = moveStr.s[1];
        chosenOne.length = 2;
        return chosenOne;
    }

    //String optionStr = "";
    
    printf("Looking through moves...\n");
    // This will look through all the top moves at the very least because alpha is always less than beta.
    int i;
    for (i = 0; i < numMoves; i++) {
        AlphaBetaBoard next = new_AlphaBetaBoard(AlphaBetaBoard_board_copy(&current), current.moveCount);
        int movePos = i * 3;
        String thisMove;
        thisMove.s[0] = moveStr.s[movePos];
        thisMove.s[1] = moveStr.s[movePos+1];
        thisMove.length = 2;
        AlphaBetaBoard_make_move_str(&next, token, thisMove);
        abresult = alphabeta(token, &next, ply - 1, alpha, beta, false);

        if (abresult > bestSoFar) {
            bestSoFar = abresult;
            bestIndex = i;
        }/*
        else if (abresult == bestSoFar) { // Keep things interesting
            int random = (int) (Math.random() * 2.0);
            
            if (random == 1 && !(moves[i].equals("a1") || moves[i].equals("h1") || moves[i].equals("a8") || moves[i].equals("h8"))) { // Don't pass up a corner
                bestIndex = i;
            }
        }*/

        if (bestSoFar > alpha) {
            alpha = bestSoFar;
        }

        if (beta <= alpha) { break; }
    }
    
    printf("bestIndex: %d", bestIndex);
    int chosenIndex = bestIndex * 3;
    chosenOne.s[0] = moveStr.s[chosenIndex];
    chosenOne.s[1] = moveStr.s[chosenIndex+1];
    printf("First char: %c", moveStr.s[chosenIndex]);
    printf("Second char: %c", moveStr.s[chosenIndex+1]);
    chosenOne.length = 2;
    
    // We have the move
    return chosenOne;
}


// This function is just like the constructor and run methods in OthelloServerWorker
void serv_worker(void * socket_handle) {
    int client_desc = *(int*) socket_handle;
    int read_size = 0;
    char data[69];
    int index, row, col;
    char token;
    int numMoves;
    Grid grid;
    AlphaBetaBoard board;
    String chosenOne;

    //while (true) {
        // Get the board
        read_size = recv(client_desc, data, 100, 0);
    
        //printf("Read size is %d\n", read_size);
    
        data[read_size] = '\0';
        //puts(data);
    
        if (read_size == -1) {
            perror("recv failed");
            //break;
        }
        else if (read_size != 0) {
            // Parse the data

            // Get the board
            for (index = 0; index < 64; index++) {
                row = index / 8;
                col = index % 8;

                grid.grid[row][col] = data[index];
            }

            //printf("This should be an underscore: %c\n", data[index]);
            index++;
            token = data[index];
            //index++;
            //printf("This should be an underscore: %c\n", data[index]);
            //index++;
            index += 2;
            //printf("This is the first digit: %c\n", data[index]);
            numMoves = (int) (data[index] - '0');
            //printf("numMoves so far: %d\n", numMoves);
            index++;

            if (data[index] >= '0' && data[index] <= '9') {
                numMoves *= 10;
                numMoves += (int) (data[index] - '0');
                //printf("This is the second digit: %c\n", data[index]);
            }

            printf("Token: %c\nNumMoves: %d\n", token, numMoves);

            board = new_AlphaBetaBoard(grid, numMoves);
    
            printf("The board:\n");
            AlphaBetaBoard_display(&board);

            chosenOne = makeMove(token, &board);
            chosenOne.s[2] = '\n';

            printf("I chose %s", chosenOne.s);
            write(client_desc, chosenOne.s, 3);
        }
    //}
}

int main() {
    int status = 0;
    int server_desc;
    int client_desc;
    int c; // I don't know what this is.

    struct sockaddr_in server;
    struct sockaddr_in client;
    
    server_desc = socket(AF_INET, SOCK_STREAM, 0);
    
    if (server_desc == -1) {
        puts("Could not create server socket");
        status = 1;
    }
    else {
        puts("Server socket made.");
        server.sin_family = AF_INET;
        server.sin_addr.s_addr = INADDR_ANY;
        server.sin_port = htons(12321);
        
        // Bind
        if (bind(server_desc, (struct sockaddr *)&server, sizeof(server)) < 0) {
            puts("Bind failed");
            status = 2;
        }
        else {
            puts("Socket is bound.");
            
            listen(server_desc, 3);
            
            // Wait for incoming connections
            c = sizeof(struct sockaddr_in);
            
            client_desc = accept(server_desc, (struct sockaddr *)&client, (socklen_t*)&c);
            
            if (!client_desc) {
                puts("Connect failed");
                status = 3;
            }
            else { // Start a thread for that socket.
                serv_worker((void*) &client_desc);
            }
        }
    }
    
    return status;
}

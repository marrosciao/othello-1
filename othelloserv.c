#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stddef.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <pthread.h>

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
    int moveCount;
    char grid[8][8];
    char sockToken;
} AlphaBetaBoard;

AlphaBetaBoard new_AlphaBetaBoard(char * data_p) {
    AlphaBetaBoard new_abb;
    int i;
    int row = 0;
    int col = 0;
    
    // The first 64 characters will be the board
    for (i = 0; i < 64; i++) {
        row = i / 8;
        col = i % 8;
        
        new_abb.grid[row][col] = *data_p;
        data_p++;
    }
    
    // i = 64 which is an underscore so move the pointer up another
    data_p++;
    
    // The next character will be the token
    new_abb.sockToken = *data_p;
    data_p++;
    
    // The next at most 2 characters will be a number
    int moveCount = (*data_p - 30);
    data_p++;
    
    if ((*data_p) != '\0') {
        moveCount *= 10;
        moveCount += *data_p;
    }
    
    new_abb.moveCount = moveCount;
    
    return new_abb;
}

void AlphaBetaBoard_display(AlphaBetaBoard * this) {
    int i;
    int j;
    
    printf("   ");
    
    for (i=0; i<8; i++) {
        printf(" %c", (char)(i+97));
    }
    
    printf("\n");

    for (i=0; i<8; i++) {
        printf(" %d", i+1);
        
        for (j=0; j<8; j++) {
            printf(" %c", this->grid[i][j]);
        }
        
        printf("  %d\n", i+1);
    }

    printf("   ");
    
    for (i=0; i<8; i++) {
	    printf(" %c", (char)(i+97));
	}
	
    printf("\n");
}

int AlphaBetaBoard_capture(AlphaBetaBoard * this, Move * move, int ioff, int joff) {
    int a,b,n=0;
    char otherToken= move->token=='X' ? 'O' : 'X';

    // try capturing in the current direction, changing discs as we go
    for (a=move->i+ioff, b=move->j+joff; a>=0 && a<8 && b>=0 && b<8 && this->grid[a][b]==otherToken; a+=ioff, b+=joff) {
        this->grid[a][b]= move->token;
        n++;
    }

    // did we run off edge of grid or encounter a blank when
    // we were turning over pieces? If so, we have to restore
    // those slots to what they were before.
    if (a<0 || a>7 || b<0 || b>7 || this->grid[a][b]==' ') {
        for (a-=ioff, b-=joff; n>0; a-=ioff, b-=joff, n--) {
            this->grid[a][b]= otherToken;
        }
    }

    if (n>0) move_add_capture(move, ioff,joff,n);
    return n;
}

int AlphaBetaBoard_count_captures(AlphaBetaBoard * this, char otherToken, int i, int j, int ioff, int joff) {
    int a,b,n=0;

    for (a=i+ioff, b=j+joff; a>=0 && a<8 && b>=0 && b<8 && this->grid[a][b]==otherToken; a+=ioff, b+=joff)
        n++;
    if (a<0 || a>7 || b<0 || b>7 || this->grid[a][b]==' ') return 0;
    return n;
}

Move * AlphaBetaBoard_make_move_ij(AlphaBetaBoard * this, char token, int i, int j) {
    int totalChange;
    Move moveobj = new_move(i, j, token);
    Move * move = &moveobj;

    totalChange= 0;
    totalChange+= AlphaBetaBoard_capture(this, move,+1,+0); // down
    totalChange+= AlphaBetaBoard_capture(this, move,-1,+0); // up
    totalChange+= AlphaBetaBoard_capture(this, move,+0,+1); // right
    totalChange+= AlphaBetaBoard_capture(this, move,+0,-1); // left
    totalChange+= AlphaBetaBoard_capture(this, move,-1,+1); // diag up-right
    totalChange+= AlphaBetaBoard_capture(this, move,+1,+1); // diag down-right
    totalChange+= AlphaBetaBoard_capture(this, move,-1,-1); // diag up-left
    totalChange+= AlphaBetaBoard_capture(this, move,+1,-1); // diag down-left

    if (totalChange>0) {
        this->grid[i][j]= token;
        this->moveCount++;
        return move;
    }
    
    return NULL;
}

Move * AlphaBetaBoard_make_move_str(AlphaBetaBoard * this, char token, char * theMove) {
    char col = *theMove;
    theMove++;
    int i = ((int)*theMove) - ((int)'0') - 1;
    theMove--;
    int j = ((int) col) - ((int)'a');
    return AlphaBetaBoard_make_move_ij(this, token, i, j);
}

Move * AlphaBetaBoard_make_move_move(AlphaBetaBoard * this, Move * move) {
    return AlphaBetaBoard_make_move_ij(this, move->token, move->i, move->j);
}

void AlphaBetaBoard_unmake_move(AlphaBetaBoard * this, Move * move) {
    int a,b,i,n;
    int ioff,joff;
    char otherToken= move->token=='X' ? 'O' : 'X';

    this->grid[move->i][move->j]= ' ';
    for (i=0; i<8 && &(move->captures[i])!=NULL; i++) { // Be careful here
        n= move->captures[i].n;
        ioff= move->captures[i].ioff;
        joff= move->captures[i].joff;
        for (a=move->i+ioff, b=move->j+joff; n>0; a+=ioff, b+=joff) {
            this->grid[a][b]= otherToken;
            n--;
        }
    }
    this->moveCount--;
}

int AlphaBetaBoard_can_move_ij(AlphaBetaBoard * this, char token, int i, int j) {
    char otherToken= token=='X' ? 'O' : 'X';
    int n=0;

    if (this->grid[i][j]==' ') {
        n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+1,+1);
        n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+1,+0);
        n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+1,-1);
        n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+0,-1);
        n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+0,+1);
        n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,-1,+1);
        n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,-1,+0);
        n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,-1,-1);
    }
    return n>0;
}

int AlphaBetaBoard_can_move_str(AlphaBetaBoard * this, char token, char * theMove) {
    char col = *theMove;
    theMove++;
    int i = ((int)*theMove) - ((int)'0') - 1;
    theMove--;
    int j = ((int) col) - ((int)'a');
    return AlphaBetaBoard_can_move_ij(this, token, i, j);
}

char * AlphaBetaBoard_translate_move(int i, int j) {
    char * translation = "";
    char firstChar = (char)((char)j+(char)'a');
    char secondChar = (char)((char)(i+1)+(char)'0');
    strcat(translation, &firstChar);
    strcat(translation, &secondChar);
    
    return translation;
}

int AlphaBetaBoard_count_possible_moves(AlphaBetaBoard * this, char token, int showMoves) {
    int i,j;
    char otherToken= token=='X' ? 'O' : 'X';
    int n,sum=0;

    if (showMoves)
        printf("Possible Moves:");
    for (i=0; i<8; i++) {
        for (j=0; j<8; j++) {
            if (this->grid[i][j]==' ') {
                n= 0;
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+1,+1);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+1,+0);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+1,-1);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+0,-1);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,+0,+1);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,-1,+1);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,-1,+0);
                n+= AlphaBetaBoard_count_captures(this, otherToken,i,j,-1,-1);
                if (showMoves && n>0) {
                    char * transMove = AlphaBetaBoard_translate_move(i,j);
                    printf(" ");
                    printf("%s", transMove);
                }
                
                sum+= n>0 ? 1 : 0;
            }
        }
    }
    if (showMoves)
        printf("\n");
    return sum;
}

int AlphaBetaBoard_game_over(AlphaBetaBoard * this) {
    return this->moveCount==64 || (AlphaBetaBoard_count_possible_moves(this, 'O', 0)==0 && AlphaBetaBoard_count_possible_moves(this, 'X', 0)==0);
}

void AlphaBetaBoard_show_results(AlphaBetaBoard * this) {
    int i,j;
    int xcount, ocount;

    if (!AlphaBetaBoard_game_over(this))
        printf("Current Standing: ");
    else
        printf("Final Results   : ");

    xcount= ocount= 0;

    for (i=0; i<8; i++)
        for (j=0; j<8; j++)
            if (this->grid[i][j]=='X')
                xcount++;
            else if (this->grid[i][j]=='O')
                ocount++;
    printf("X(%d) - O(%d)\n", xcount, ocount);
}

char * AlphaBetaBoard_board_copy(AlphaBetaBoard * this) {
    char copy[8][8];
    char[][] * copy_p = &copy;
    int row;
    int col;
    
    for (row = 0; row < 8; row++) {
        for (col = 0; col < 8; col++) {
            copy[row][col] = this->grid[row][col];
        }
    }

    return copy_p;
}

// This function is just like the constructor and run methods in OthelloServerWorker
void * serv_worker(void * socket_handle) {
    int client_desc = *(int*) socket_handle;
    int read_size = 0;
    char data[69];

    // Get the board
    read_size = recv(client_desc, data, 100, 0);
    
    printf("Read size is %d\n", read_size);
    
    data[read_size] = '\0';
    puts(data);
    
    if (read_size == 0) {
        puts("No more data");
    }
    else if (read_size == -1) {
        perror("recv failed");
    }
    else {

    }
    
    return;
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

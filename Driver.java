/**
 * Driver for the game.
 */

public class Driver {
    public static void main(String [] args) {
        Board board = new Board();
        MattOthelloPlayerPrime p1 = new MattOthelloPlayerPrime(null, 'X', 1.0, 1.0, 1.0, 0, 0, 0, 0, 1, 2, 3);
        MattOthelloPlayerPrime p2 = new MattOthelloPlayerPrime(null, 'O', 1.0, 1.0, 1.0, 0, 0, 0, 0, 4, 5, 6);
        OthelloGame game= new OthelloGame(p1, p2, false);
        long startTime = System.currentTimeMillis();
        game.play();
        long endTime = System.currentTimeMillis();
        long timeTaken = (endTime - startTime);
        //timeTaken /= 1000.0;
        System.out.println("Game played in " + timeTaken + "ms");
    }
}

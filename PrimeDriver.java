/**
 * Driver for the game.
 */

import java.io.*;
import java.util.Scanner;
import java.util.PriorityQueue;

public class PrimeDriver {

    public static boolean isGood(int[] xyz) {
        return xyz[0] >= 0 && xyz[1] >= 0 && xyz[2] >= 0 && xyz[0] < 11 && xyz[1] < 11 && xyz[2] < 11;
    }
    
    public static void average(MattOthelloPlayerPrime high, MattOthelloPlayerPrime low) {
        low.tWeight = (low.tWeight + high.tWeight) / 2.0;
        low.mWeight = (low.mWeight + high.mWeight) / 2.0;
        low.wWeight = (low.wWeight + high.wWeight) / 2.0;
    }

    public static void main(String [] args) throws FileNotFoundException {
        Scanner inGen = new Scanner(new File("peaks.txt"));
        PrintWriter outGen = new PrintWriter("prime.txt");
        MattOthelloPlayerPrime[] population = new MattOthelloPlayerPrime[1810];
        String line;
        int index = 0;
        long startTime = System.currentTimeMillis();
        // Load the population
        while (inGen.hasNextLine()) {
            line = inGen.nextLine();
            String[] ls = line.split(" ");
            population[index] = new MattOthelloPlayerPrime(null, (char) 0, Double.parseDouble(ls[0]), Double.parseDouble(ls[1]), Double.parseDouble(ls[2]), Long.parseLong(ls[3]), Long.parseLong(ls[4]), 0, 0, Integer.parseInt(ls[7]), Integer.parseInt(ls[8]), Integer.parseInt(ls[9]));
            index++;
        }
        
        OthelloGame game;
        
        for (int i = 0; i < population.length; i++) {
            for (int j = 0; j < population.length; j++) {
                if (i != j && !population[i].allZeros() && !population[j].allZeros()) {
                    population[i].token = 'X';
                    population[j].token = 'O';
                    System.out.println("i = " + i);
                    game= new OthelloGame(population[i], population[j], false);
                    game.play();
                }
            }
        }
        
        PriorityQueue<MattOthelloPlayerPrime> q = new PriorityQueue<MattOthelloPlayerPrime>();
        
        for (int i = 0; i < population.length; i++) {
            population[i].genScore =(int) (population[i].genWins - population[i].genLosses);
            population[i].score = (int) (population[i].wins - population[i].losses);
            q.add(population[i]);
        }
        
        // Of all the peaks this one did the best
        MattOthelloPlayerPrime prime = q.poll();
        outGen.println(prime.genes());

        outGen.flush();
        outGen.close();
        inGen.close();
        
        //game.play();
        long endTime = System.currentTimeMillis();
        long timeTaken = (endTime - startTime);
        double seconds = (double) timeTaken / 1000.0;
        System.out.println("Generation run in " + seconds + " seconds");
    }
}

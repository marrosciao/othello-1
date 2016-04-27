/**
 * Driver for the game.
 */

import java.io.*;
import java.util.Scanner;
import java.util.PriorityQueue;

public class Driver {

    public static boolean isGood(int[] xyz) {
        return xyz[0] >= 0 && xyz[1] >= 0 && xyz[2] >= 0 && xyz[0] < 11 && xyz[1] < 11 && xyz[2] < 11;
    }
    
    public static void average(MattOthelloPlayerPrime high, MattOthelloPlayerPrime low) {
        low.tWeight = (low.tWeight + high.tWeight) / 2.0;
        low.mWeight = (low.mWeight + high.mWeight) / 2.0;
        low.wWeight = (low.wWeight + high.wWeight) / 2.0;
    }

    public static void main(String [] args) throws FileNotFoundException {
        Scanner inGen = new Scanner(new File("gen" + (Integer.parseInt(args[0])) + ".txt"));
        PrintWriter outGen = new PrintWriter("gen" + (Integer.parseInt(args[0]) + 1) + ".txt");
        PrintWriter peaks = new PrintWriter("peak" + args[0] + ".txt");
        MattOthelloPlayerPrime[] population = new MattOthelloPlayerPrime[1331];
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
        
        // Use averaging to move pieces closer to the peaks.
        MattOthelloPlayerPrime[][][] carpet = new MattOthelloPlayerPrime[11][11][11];
        
        for (int i = 0; i < population.length; i++) {
            carpet[population[i].x][population[i].y][population[i].z] = population[i];
        }
        
        int[][] offsets = {
            {-1, -1, -1},
            {-1, -1, 0},
            {-1, -1, 1},
            {-1, 0, -1},
            {-1, 0, 0},
            {-1, 0, 1},
            {-1, 1, -1},
            {-1, 1, 0},
            {-1, 1, 1},
            {0, -1, -1},
            {0, -1, 0},
            {0, -1, 1},
            {0, 0, -1},
            {0, 0, 1},
            {0, 1, -1},
            {0, 1, 0},
            {0, 1, 1},
            {1, -1, -1},
            {1, -1, 0},
            {1, -1, 1},
            {1, 0, -1},
            {1, 0, 0},
            {1, 0, 1},
            {1, 1, -1},
            {1, 1, 0},
            {1, 1, 1}
        };
        
        while (q.peek() != null) {
            MattOthelloPlayerPrime current = q.poll();
            MattOthelloPlayerPrime[] neighbors = new MattOthelloPlayerPrime[26];
            boolean seenNeighbor = false;
            
            // Visit the neighbors
            index = 0;
            
            for (int[] offset : offsets) {
                int[] neighborPos = {current.x + offset[0], current.y + offset[1], current.z + offset[2]};
                
                if (isGood(neighborPos)) { // No negative coordinates
                    neighbors[index] = carpet[neighborPos[0]][neighborPos[1]][neighborPos[2]];
                    
                    if (neighbors[index].seen) {
                        seenNeighbor = true;
                    }
                    
                    index++;
                }
            }
            
            // If it has been seen then it has been polled in the queue
            // If it has been moved then something moved it
            
            if (!current.moved) {
                if (!seenNeighbor) { // It is a peak so move all the neighbors closer
                    peaks.println("Found a peak: " + current.genes());
                    for (int i = 0; i < neighbors.length && neighbors[i] != null; i++) {
                        if (!neighbors[i].moved) {
                            average(current, neighbors[i]);
                            neighbors[i].moved = true;
                        }
                    }
                }
                else { // It is not a peak so move it closer to its strongest neighbor
                    int[] highestPos = {0, 0, 0};
                    int highest = Integer.MIN_VALUE;
                    // Find the strongest neighbor
                    for (int i = 0; i < neighbors.length && neighbors[i] != null; i++) {
                        if (neighbors[i].seen && neighbors[i].genScore > highest) {
                            highest = neighbors[i].genScore;
                            highestPos[0] = neighbors[i].x;
                            highestPos[1] = neighbors[i].y;
                            highestPos[2] = neighbors[i].z;
                        }
                    }
                    
                    average(carpet[highestPos[0]][highestPos[1]][highestPos[2]], current);
                }
                
                current.moved = true;
                current.seen = true;
            }
            else {
                current.seen = true;
            }
        }
        
        // Write the next generation to specified text file
        for (int i = 0; i < population.length; i++) {
            outGen.println(population[i].genes());
        }
        
        outGen.flush();
        outGen.close();
        peaks.flush();
        peaks.close();
        inGen.close();
        
        //game.play();
        long endTime = System.currentTimeMillis();
        long timeTaken = (endTime - startTime);
        double seconds = (double) timeTaken / 1000.0;
        System.out.println("Generation run in " + seconds + " seconds");
    }
}

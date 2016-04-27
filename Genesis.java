import java.io.*;

public class Genesis {

    public static void main(String[] args) throws FileNotFoundException {
        //double[] genes = {-10.0, -8.0, -6.0, -4.0, -2.0, -1.0, -0.8, -0.6, -0.4, -0.2, 0.0, 0.2, 0.4, 0.6, 0.8, 1.0, 2.0, 4.0, 6.0, 8.0, 10.0};
        double[] genes = {-10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0};
        PrintWriter population0 = new PrintWriter("gen0.txt");
        
        for (int x = 0; x < genes.length; x++) {
            for (int y = 0; y < genes.length; y++) {
                for (int z = 0; z < genes.length; z++) {
                    String member = "" + genes[x] + " " + genes[y] + " " + genes[z] + " 0 0 0 0 " + x + " " + y + " " + z;
                    population0.println(member);
                }
            }
        }
        
        population0.flush();
        population0.close();
    }

}

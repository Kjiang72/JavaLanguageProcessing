import java.io.FileReader;
import java.io.IOException;

/**
 * Train algorithm to detect language
 * @author Kjiang72
 * @version 1.0
 *
 */

public class SourceModel {
    /**
     *
     * @param source name of the source model
     * @param corpus name of the corpus
     * @throws IOException
     */
    private String name;
    private double[][] pMatrix = new double[26][26];
    public SourceModel(String source, String corpus) throws IOException {
        this.name = source;
        int[][] charCounts = new int[26][26];
        System.out.print("Training " + source + " model ...");
        FileReader inputStream = null;
        try {
            inputStream = new FileReader(corpus);
            int c;
            char prev = ' ';
            while ((c = inputStream.read()) != -1) {
                if (Character.isAlphabetic((char) c)) {
                    c = (int) Character.toLowerCase((char) c);
                    if (prev != ' ') {
                        charCounts[prev - 'a'][c - ((int) 'a')]++;
                        prev = (char) c;
                    } else {
                        prev = (char) c;
                    }
                }
            }
        } finally {
            double currentSum;
            double empty;
            if (inputStream != null) {
                inputStream.close();
                for (int row = 0; row < 26; row++) {
                    currentSum = 0;
                    empty = 0;
                    for (int column = 0; column < 26; column++) {
                        currentSum += charCounts[row][column];
                        if (charCounts[row][column] == 0) {
                            empty++;
                            pMatrix[row][column] = .01;
                        }
                    } //System.out.println(currentSum);
                    for (int pColumn = 0; pColumn < 26; pColumn++) {
                        if (currentSum > 0) {
                            if (charCounts[row][pColumn] != 0) {
                                pMatrix[row][pColumn] = ((charCounts[row][pColumn]) / currentSum)
                                        * (1.0 - (.01 * empty));
                            }
                        } else {
                            pMatrix[row][pColumn] = 1.0 / 26;
                        }
                    }
                }
            }
            System.out.println(" done");
        }
    }

    /**
     *
     * @return this methods SourceModel name
     */
    public String getName() {
        return name;
    }
    /**
     * returns a matrix representation of the first order markov chain
     */
    public String toString() {
        String res = "     ";
        for (int i = 0; i < 26; i++) {
            res += String.format("%-4s ", ((char) (i + 'a')));
        } res += "\n";
        for (int row = 0; row < 26; row++) {
            res += ((char) (row + 'a')) + " ";
            for (int column = 0; column < 26; column++) {
                res += String.format("%.2f", pMatrix[row][column]) + " ";
            } res += "\n";
        } return res;
    }

    public double probability(String test) {
        char prev = ' ';
        double probability = 1.0;
        test = test.toLowerCase();
        for (int j = 0; j < test.length(); j++) {
            if (prev != ' ') {
                if (Character.isAlphabetic(test.charAt(j))) {
                    probability *= pMatrix[prev - 'a'][test.charAt(j) - 'a'];
                    prev = test.charAt(j);
                }
            } else {
                prev = test.charAt(0);
            }
        } return probability;
    }
    /**
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String[] split = new String[3];
        String[] probabilitiesS = new String[args.length];
        double[] probabilities = new double[args.length];
        String testString = null;
        double total = 0;
        for (int x = 0; x < (args.length - 1); x++) {
            testString = args[args.length - 1];
            split = args[x].split("[.]");
            SourceModel newSource = new SourceModel(split[0], args[x]);
            //System.out.println(newSource.toString());
            probabilities[x] = newSource.probability(testString);
            probabilitiesS[x] = "Probability that test string is "
                    + String.format("%9s", split[0]) + ": ";
            total += probabilities[x];
        }
        System.out.println("Analyzing: " + testString);
        for (int g = 0; g < (probabilitiesS.length - 1); g++) {
            probabilities[g] = (probabilities[g] / total);
            System.out.println(probabilitiesS[g] + String.format("%.2f", probabilities[g]));
        }
    }
}

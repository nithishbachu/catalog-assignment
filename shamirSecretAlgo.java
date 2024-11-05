import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ShamirSecretSharing {

    // Class to store points (x, y)
    static class Point {
        int x;
        BigInteger y;

        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    // Function to decode a value from the given base (base can vary)
    public static BigInteger decodeValue(String value, int base) {
        return new BigInteger(value, base);
    }

    // Function to manually parse the JSON from a string
    public static String readJsonFromFile(String fileName) {
        StringBuilder jsonData = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonData.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonData.toString();
    }

    // Function to perform Lagrange interpolation and find the constant term (secret)
    public static BigInteger interpolateConstant(List<Point> points, int k) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < k; i++) {
            BigInteger term = points.get(i).y;
            BigInteger denom = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    int xi = points.get(i).x;
                    int xj = points.get(j).x;

                    term = term.multiply(BigInteger.valueOf(-xj));
                    denom = denom.multiply(BigInteger.valueOf(xi - xj));
                }
            }

            term = term.divide(denom); // No modulus required here
            result = result.add(term);
        }

        return result;
    }

    public static void main(String[] args) {
        try {
            // Read the JSON input from the file
            String jsonString = readJsonFromFile("input.json");

            // Step 1: Extract the 'n' and 'k' values manually
            int n = Integer.parseInt(extractValue(jsonString, "\"n\":"));
            int k = Integer.parseInt(extractValue(jsonString, "\"k\":"));

            // Step 2: Extract the points (x, y) from the JSON
            List<Point> points = new ArrayList<>();
            String pointsSection = jsonString.substring(jsonString.indexOf("\"1\":"));
            String[] entries = pointsSection.split("\\},"); // Split each entry
            
            for (String entry : entries) {
                if (entry.contains("\"base\":") && entry.contains("\"value\":")) {
                    String baseString = extractValue(entry, "\"base\":");
                    String valueString = extractValue(entry, "\"value\":");

                    int base = Integer.parseInt(baseString);
                    BigInteger decodedValue = decodeValue(valueString, base);

                    // Extract the x value from the key (in this case, it's the number before the colon)
                    String xString = entry.substring(1, entry.indexOf(":"));
                    int x = Integer.parseInt(xString);

                    points.add(new Point(x, decodedValue));
                }
            }

            // Step 3: Use Lagrange interpolation to find the secret (constant term)
            BigInteger secret = interpolateConstant(points, k);
            System.out.println("The secret (constant term) is: " + secret);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper function to extract values from the JSON string (like n, k, base, and value)
    public static String extractValue(String jsonString, String key) {
        int startIndex = jsonString.indexOf(key) + key.length();
        int endIndex = jsonString.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = jsonString.indexOf("}", startIndex);
        }
        return jsonString.substring(startIndex, endIndex).replaceAll("[^0-9]", "");
    }
}

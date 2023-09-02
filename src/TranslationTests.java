import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class TranslationTests {

    public static void main(String[] args) {

        testFormatting("tests/testBasic");
        testFormatting("tests/testNUL");
        testFormatting("tests/testIds");
        testFormatting("tests/testRealFormat1");
        testFormatting("tests/testSplit");
        testFormatting("tests/testRealFormat2");

        System.out.println("PASSED!");
    }


    /**
     * Ensures that the splitIntoChunksAndFormat() and unformat() methods works as intended giving
     * correct results on test cases specified by the path in the parameter
     * @param filename path to file containing the input text.
     */
    public static void testFormatting(String filename) {
        boolean pass = false;
        try {
            BadTranslation.splitTextIntoChunksAndFormat(filename + ".txt", filename + "_temp1.txt");
            pass = assertEqualsFile(filename + "_temp1.txt", filename + "_a.txt");
            if (!pass) {
                System.out.println("FAILED TEST: splitTextIntoChunksAndFormat");
                System.exit(0);
            }


            BadTranslation.unformat(filename+ "_temp1.txt", filename + "_temp2.txt");
            pass = assertEqualsFile(filename + "_temp2.txt",filename + ".txt");
            if (!pass) {
                System.out.println("FAILED TEST: unformat");
                System.exit(0);
            }


            BadTranslation.deleteTextFile(filename + "_temp1.txt");
            BadTranslation.deleteTextFile(filename + "_temp2.txt");

        }
        catch(IOException e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Helper for testing. Reads from two given file names, and returns whether the
     * two files have equal contents.
     *
     * @param matchFileName1 first file to compare
     * @param matchFileName2 name of file containing contents to compare to matchFileName1
     * @return true if file contents equals the matchText string, false if not.
     */
    public static boolean assertEqualsFile(String matchFileName1, String matchFileName2) {
        Scanner scanner1 = null;
        Scanner scanner2 = null;

        File inFile1 = new File(matchFileName1);
        File inFile2 = new File(matchFileName2);

        StringBuilder strBuilder1 = new StringBuilder();
        StringBuilder strBuilder2 = new StringBuilder();

        try {
            scanner1 = new Scanner(inFile1);
            scanner2 = new Scanner(inFile2);
        }
        catch(IOException e) {
            System.out.println("assertEqualsFile: Could not open file "  + ", e: "
                    + e.getMessage());
        }

        while (scanner1.hasNextLine()) {
            strBuilder1.append(scanner1.nextLine());
            strBuilder1.append("\n");
        }
        while (scanner2.hasNextLine()) {
            strBuilder2.append(scanner2.nextLine());
            strBuilder2.append("\n");
        }

        String fileString1 = strBuilder1.toString();
        String fileString2 = strBuilder2.toString();

        System.out.println("**FileString 1:** \n" + fileString1);
        System.out.println("**FileString 2:** \n" + fileString2);

        scanner1.close();
        scanner2.close();

        return fileString1.equals(fileString2);

    }
}

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/*
 * ============== BadTranslations.java =============
 *
 * This class is responsible for badly translating the game "Super Paper Mario"
 * for the Nintendo Wii. This is done by taking the text present within the game
 * and running it through Google Translate over and over and over through many
 * languages, starting with English, and eventually translated back to English.
 * Very humorous dialogue and conversations result from this.
 *
 * Super Paper Mario has ten text files used for dialogue in the game:
 * global.txt
 * machi.txt
 * stg1.txt
 * stg2.txt
 * stg3.txt
 * stg4.txt
 * stg5.txt
 * stg6.txt
 * stg7.txt
 * stg8.txt
 * These contain all the dialogue on the game, and can be run through this program
 * to perform the translations.
 *
 *
 * ============== Known Problems =============
 * Some of the produced lines of text are so long that they run off the screen.
 * this is mostly due to google translate's replacement of some > tags with
 * different characters, breaking some of the code in unformat(), which comes
 * after newline method. So the newline method misses some potential spots
 * to place new lines.
 *
 * Dialogue boxes asking the player to select between certain options are not properly
 * aligned - but since there are not that many overall this was fixed manually within the
 * text files.
 *
 */

public class BadTranslation {

    /*
     * Default language acting as the original input language, and
     * the final output language.
     */
    private static final String DEFAULT_LANG = "en"; /* English */

    /* Below are the languages that the text will be translated into before
     * being translated back into english. These can be changed at any point
     * which will produce a different translated result!
     */
    private static final String[] LANGS = {
            "af",           /* Afrikaans */
            "pt",           /* Portuguese */
            "sw",           /* Swahili */
            "ru",           /* Russian */
            "ar",           /* Arabic */
            "yi",           /* Yiddish */
            "fr",           /* French */
            "zh-TW",        /* Chinese traditional */
            "la",           /* Latin */
            "es",           /* Spanish */
            "ja",           /* Japanese */
            "la",           /* Latin - again */
            "hr",           /* Croatian */
            "ga",           /* Irish */
            "it"            /* Italian */
    };

    /* SPM_TEXT Can be - global, machi, stg1 - stg8 (all within text_files directory)*/
    private static final String SPM_TEXT = "text_files/stg7"; /* game text file to work with  */
    private static final String SPM_TEXT_FORMATTED = SPM_TEXT + "_formatted.txt"; /* Formatted text game file */
    private static final String SPM_TEXT_TRANSLATED = SPM_TEXT + "_translated.txt"; /* Translated formatted text */
    private static final int MAX_CHARS_PER_LINE = 26; /* Maximum allowed characters per line in output text file */
    private static final int MAX_CHARS_PER_CHUNK = 950; /* GoogleTranslate has a 5000 char limit, 950 to be safe. */

    /*****************************************
     * METHODS
     *****************************************/



    public static void main(String[] args) throws IOException {


        String res;
        StringBuilder resBuilder = new StringBuilder();
        String currLine;

        File inFile;
        FileWriter writer = null;
        BufferedReader scanner = null;

        /* Format the original file to protect important identifiers and such from translation */
        splitTextIntoChunksAndFormat(SPM_TEXT + ".txt", SPM_TEXT_FORMATTED);

        try {
            inFile = new File(SPM_TEXT_FORMATTED);
            File outFile = new File("output.txt");
            scanner = new BufferedReader(new FileReader(inFile));

            writer = new FileWriter(SPM_TEXT_TRANSLATED);
        }
        catch (IOException e) {
            System.out.println("Error in Main while setting up IO: " + e.getMessage());
        }

        while ((currLine = scanner.readLine()) != null) {

            resBuilder.append(currLine);
            System.out.println(currLine);


            /* If at end of chunk or file, begin translating the data in resBuilder */
            /* scanner seems to remove the newline character, so empty string */
            if (currLine.equals("") || !scanner.ready()) {
                res = multipleTranslate(resBuilder.toString());

                /*
                 * The entire text is now in one line. Separate the text into more than one line, using a
                 * limit per line
                 */
                res = setNewLines(res);
                resBuilder = new StringBuilder(); /* Reset for next string */
                writer.append(res);
            }


        }

        scanner.close();
        writer.close();

        /* Now reformat the translated result back to the original's, so the game can read
         * the text file properly
         */
        unformat(SPM_TEXT_TRANSLATED, SPM_TEXT + "_finalTranslation.txt");

        /* Delete the formatted file since it was just temporary for input for the translator */
       if (!deleteTextFile(SPM_TEXT_FORMATTED)) {
            System.out.println("Oh no! Failed to delete: " + SPM_TEXT_FORMATTED);
       }
        if (!deleteTextFile(SPM_TEXT_TRANSLATED)) {
            System.out.println("Oh no! Failed to delete: " + SPM_TEXT_TRANSLATED);
        }
    }

    /**
     * Performs a translation on a string of text from one language to another using Google Translate API.
     * @param langFrom language of input string, i.e. translated from.
     * @param langTo language to translate the string to i.e. translate to.
     * @param text String to perform the translation on.
     * @return a string that represents the translated text.
     * @throws IOException upon an issue in making a connection to the translation script and API
     */
    private static String translate(String langFrom, String langTo, String text) throws IOException {

        String urlStr = Config.Script1_URL +
                "?q=" + URLEncoder.encode(text, "UTF-8") +
                "&target=" + langTo +
                "&source=" + langFrom;
        URL url = new URL(urlStr);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();

    }

    /**
     * Calls the translate() function multiple times.
     * @param text text being translated
     * @return the resulting text that has been translated many times
     * @throws IOException upon an issue in making a connection to the translation script and API
     */
    private static String multipleTranslate(String text) throws IOException {
        String res = "error";

        try {

            /* Translate from english to the first language in array using given text */
            res = translate(DEFAULT_LANG, LANGS[0], text);

            /* Translate from/to each language in LANGS array */
            for (int lang_i = 0; lang_i < LANGS.length - 1; lang_i++) {
                res = translate(LANGS[lang_i], LANGS[lang_i+1], res);
            }

            /* Translate from final language in LANGS back to english */
            res = translate(LANGS[LANGS.length - 1], DEFAULT_LANG, res);

        }
        catch (IOException e) {
            System.out.println("=========ERROR: " + e.getMessage());
            System.out.println("for the text : " + text);
            return text;
        }


        return res; /* Return resulting string of the multiple translations */
    }


    /**
     * After translation, each chunk will be on the same line.
     * The game will theoretically still work without new lines present,
     * but the a lot of the text will appear off of the screen.
     *
     * This function determines when a new line should be added
     * consecutive identifers and such enclosed in <tags> staying
     * on one line is fine, but new lines should be added in character
     * dialogues so that the text is spread out within the text boxes
     * instead of being on one line so they continue off the limits of
     * the screen. It is also important to avoid situations where
     * something in <tags> is not separated by new line e.g.
     * <ta
     * gs> which will cause some problems.
     *
     * @param chunk translated text to add new lines to.
     * @return chunk string but with formatted new lines
     */
    public static String setNewLines(String chunk) {

        int strPos = 0;
        int nestedBracketCount = 0;
        int nonTaggedCountInLine = 0;
        boolean previousWasTag = true;
        StringBuilder strBuilder = new StringBuilder();

        strBuilder.append("\n");
        while (strPos < chunk.length()) {

            char currChar = chunk.charAt(strPos);

            if (currChar == '<') {
                nestedBracketCount++;
                if (!previousWasTag) strBuilder.append("\n");
                previousWasTag = true;
                nonTaggedCountInLine = 0;
            }
            if (currChar == '>') {
                nestedBracketCount--;
            }

            if (nestedBracketCount == 0 && currChar != '>') {
                previousWasTag = false;
                nonTaggedCountInLine++;

                if (nonTaggedCountInLine >= MAX_CHARS_PER_LINE) {
                    int endPos = strPos + 1;

                    /* Forward strPos if it does not end on a space, to prevent
                     * cases where words are cut off between lines like
                     *
                     * "Hello, how are you doing tod
                     * ay?
                     */
                    while (endPos < chunk.length() &&
                        chunk.charAt(endPos) != ' ' && chunk.charAt(endPos) != '<') {
                        endPos++;
                    }


                    strBuilder.append(chunk.substring(strPos,endPos));
                    strBuilder.append("\n");
                    strPos = endPos;
                    nonTaggedCountInLine = 0;
                }

                else {
                    strBuilder.append(currChar);
                    strPos++;
                }
            }

            else {
                strBuilder.append(currChar);
                strPos++;
            }



        }

        return strBuilder.toString();
    }



    /**
     * Google translate has a 5,000 character limit for translation. The input files
     * will be much over this limit, so the purpose of this function is to split the
     * file into separate chunks of up to 3,350 characters (to account for an increase
     * of characters due to a different language translation), separate using newlines.
     *
     * Most strings between null characters in the file are used as important identifiers
     * in the game, which we do not want translated to prevent problems. So this method also
     * encloses those strings within <> brackets so the translator ignores them. Since such
     * identifiers all contain underlines (in_format_like_this) it is easy to tell them apart.
     *
     * Furthermore, there are null characters present in the files enclosing most identifiers
     * important for the game to process. Google translate gets rid of these bytes, which
     * will cause crashes. So null characters are replaced with <NUL> tag which will be
     * untouched by Translate. Other existing tags such as <p> in the files causes strange
     * behavior as well, so they are replaced by <placeholder>. These tag replacements are
     * then replaced again by the original characters after translation.
     *
     * @param originalFileName Name of the file
     * @param newFileName
     */
    public static void splitTextIntoChunksAndFormat(String originalFileName, String newFileName)
        throws IOException{

        File inFile;
        BufferedReader scanner = null;
        FileWriter writer = null;

        try {
            inFile = new File(originalFileName);
            scanner = new BufferedReader(new FileReader(inFile));
            writer = new FileWriter(newFileName);
        }
        catch (IOException e) {
            System.out.println("splitTextIntoChunksFormat() error while setting I/O: " + e.getMessage());
        }

        /* RegEx to find identifiers. (any number of any char)_(any number of any char) */
        Pattern underscorePattern = Pattern.compile("\\w*_\\w*");
        Matcher matcher;

        int charCount = 0;
        String line;
        while ((line = scanner.readLine()) != null) {

            //String line = scanner.nextLine();
            String identifier;
            System.out.println(line);

            /* Enclose strings between null bytes in brackets */
            matcher = underscorePattern.matcher(line);
            while (matcher.find()) {
                identifier = matcher.group(0);
                line = line.replaceFirst(identifier, "<" + identifier + ">");
            }


            /* Replace all null chars with <NUL> */
            line = line.replaceAll("\0", "<NUL>");
            /* Replace all <p> with <placeholder> */
            line = line.replaceAll("<p>", "<placeholder>");

            /* Character count check, if addition of this line surpasses the char count, then
             * append a new line and begin the next chunk.
             */
            if (charCount + line.length() > MAX_CHARS_PER_CHUNK && charCount != 0) {
                writer.append("\n");
                charCount = 0;
            }

            charCount += line.length();
            writer.append(line);
            writer.append("\n");
        }

        scanner.close();
        writer.close();

    }



    /**
     * Undoes the effects of splitTextIntoChunksAndFormat() on the recently-translated file
     * for the game to be able to read.
     * <placeholder> is replaced with <p>
     * <NULL> is replaced with the null character.
     * All new lines are deleted.
     *
     * @param inFileName name of the file to read from
     * @param outFileName name of the file to write to
     */
    public static void unformat(String inFileName, String outFileName)
        throws IOException {
        File inFile = new File(inFileName);
        BufferedReader scanner = null;
        FileWriter writer = null;

        try {
            scanner = new BufferedReader(new FileReader(inFile));
            writer = new FileWriter(outFileName);
        }
        catch (IOException e) {
            System.out.println("unformat() error while setting I/O: " + e.getMessage());
        }

        /* RegEx to find identifiers. >(any number of any char)_(any number of any char)< */
        Pattern underscorePattern = Pattern.compile("<\\w*_\\w*>");
        Matcher matcher;
        String line;
        while ((line = scanner.readLine()) != null) {

            String identifier;

            /* Ignores new line separations created by splitTextIntoChunks */
            if (!line.equals("")) {

                /* remove the <> tags from identifiers */
                matcher = underscorePattern.matcher(line);
                while (matcher.find()) {
                    identifier = matcher.group(0);
                    line = line.replaceFirst(identifier, identifier.substring(1, identifier.length() - 1));
                }

                /* Replace all <NUL> with null char */
                line = line.replaceAll("<NUL>", "\0");
                /* Replace all <p> with <placeholder> */
                line = line.replaceAll("<placeholder>", "<p>");

                /* For some reason Google translate replaces apostrophes with " &#39; "
                *  reverse this effect. */
                line = line.replaceAll("&#39;", "'");

                /* Similar as above, fixing strange output of google translate for quotations */
                line = line.replaceAll("&quot;", "\"");

                /* Replace " &gt;" and "&gt;f" with > */
                line = line.replaceAll("&gt;f", ">");
                line = line.replaceAll(" &gt;", ">");
                line = line.replaceAll("&gt;", ">");

                writer.append(line);
                writer.append("\n");
            }

        }

        writer.close();
        scanner.close();
    }


    /**
     * Deletes a file with the given name.
     *
     * @param fileNameToDelete name of the file to delete.
     * @return true if successful deletion, false if not.
     */
    public static boolean deleteTextFile(String fileNameToDelete) {

        File file = new File(fileNameToDelete);

        return file.delete();

    }


}
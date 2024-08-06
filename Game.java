import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Game {
    public static final int WORD_SIZE = 5;
    public static final int GUESSES = 6;

    private final String answer;
    private int guessNumber;

    public Game(String answer) {
        this.answer = answer;
        guessNumber = 0;
    }

    public int getGuessNumber() {
        return guessNumber;
    }

    public String getAnswer() {
        return answer;
    }

    /**
     * Calculates the output of the game when the player guesses a word.
     * @param guess the player's guess ({@link #WORD_SIZE} letters)
     * @return a {@code char[]} that contains the game's output after the player guesses a word,
     *         with each index in the array representing the output of the corresponding index letter
     *         in the guess.
     *         <ul>
     *             <li>B (Black) — the character in that position in the guess is <i>not</i> in the answer</li>
     *             <li>Y (Yellow) — the character in that position in the guess is <i>somewhere</i> in the answer
     *                 but not in that exact position</li>
     *             <li>G (Green) – the character in that position in the guess is in the <i>same</i> place in
     *                 the answer</li>
     *         </ul>
     * @throws IllegalArgumentException if the guess is not {@link #WORD_SIZE} letters
     * @throws NotAWordException if the guess is not a word
     */
    public char[] guess(String guess) {
        if (guess.length() != WORD_SIZE) {
            throw new IllegalArgumentException("Guess must be " + WORD_SIZE + " letters.");
        }

        if (!isWord(guess)) {
            throw new NotAWordException();
        }

        guessNumber++;

        char[] output = new char[5];

        for (int i = 0; i < WORD_SIZE; i++) {
            char answerC = answer.charAt(i);
            char guessC = guess.charAt(i);

            if (answerC == guessC) {
                output[i] = 'G';
            }
        }

        for (int i = 0; i < WORD_SIZE; i++) {
            char answerC = answer.charAt(i);
            char guessC = guess.charAt(i);

            if (output[i] != 'G') {
                if (answer.contains(String.valueOf(guessC))
                        && numOf(guessC, guess.substring(0, i + 1)) + numOfGreens(guess, guessC, i + 1) <= numOf(guessC, answer)) {
                    output[i] = 'Y';
                } else {
                    output[i] = 'B';
                }
            }
        }

        return output;
    }

    private static boolean isWord(String word) {
        try (Scanner reader = new Scanner(new File("dictionary.txt"))) {
            while (reader.hasNext()) {
                if (reader.next().equalsIgnoreCase(word)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int numOf(char letter, String word) {
        int count = 0;
        for (char c : word.toCharArray()) {
            if (c == letter) {
                count++;
            }
        }
        return count;
    }

    private int numOfGreens(String guess, char letter, int startIndex) {
        ArrayList<Integer> occurences = new ArrayList<>();
        for (int i = startIndex; i < guess.length(); i++) {
            if (guess.charAt(i) == letter) {
                occurences.add(i);
            }
        }
        int output = 0;
        for (int index : occurences) {
            if (answer.charAt(index) == letter) {
                output++;
            }
        }
        return output;
    }

    public static void main(String[] args) {
    }
}

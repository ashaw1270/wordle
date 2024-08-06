import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Graphic extends JFrame {
    private static final int FRAME_HEIGHT = 800;
    private static final int GAP_SIZE = (int) (.0125 * FRAME_HEIGHT);
    private static final int FRAME_WIDTH = (int) ((GAP_SIZE + Game.WORD_SIZE * FRAME_HEIGHT) / (double) Game.GUESSES);

    private Game game;
    private final JPanel[][] panels;
    private int letterNumber;
    private String currentGuess;

    public Graphic() {
        newGame();
        panels = new JPanel[Game.GUESSES][Game.WORD_SIZE];
        letterNumber = 0;
        currentGuess = "";

        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        getContentPane().setBackground(Color.WHITE);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel inner = new JPanel();
        inner.setBounds(GAP_SIZE, GAP_SIZE, getWidth() - 2 * GAP_SIZE, getHeight() - 2 * GAP_SIZE);
        inner.setLayout(new GridLayout(Game.GUESSES, Game.WORD_SIZE, GAP_SIZE, GAP_SIZE));
        inner.setBackground(Color.WHITE);
        //inner.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        inner.setVisible(true);
        add(inner);


        for (int row = 0; row < Game.GUESSES; row++) {
            for (int col = 0; col < Game.WORD_SIZE; col++) {
                JPanel panel = new JPanel();

                JLabel label = new JLabel();
                int panelSize = (int) ((getHeight() - (Game.GUESSES + 1) * GAP_SIZE) / 6.);
                label.setFont(new Font("Helvetica Neue", Font.BOLD, (int) (3./4 * panelSize)));
                label.setSize(panelSize, panelSize);
                label.setVisible(true);

                panel.add(label);

                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createLineBorder(new Color(215, 215, 215), (int) (.005 * FRAME_HEIGHT)));
                panel.setVisible(true);
                panels[row][col] = panel;
                inner.add(panel);
            }
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int guessNumber = game.getGuessNumber();

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (letterNumber > 0) {
                        ((JLabel) panels[guessNumber][letterNumber-- - 1].getComponent(0)).setText("");
                        currentGuess = currentGuess.substring(0, currentGuess.length() - 1);
                    }
                } else if (letterNumber < Game.WORD_SIZE) {
                    if (Character.isAlphabetic(e.getKeyChar())) {
                        ((JLabel) panels[guessNumber][letterNumber++].getComponent(0)).setText(String.valueOf(e.getKeyChar()).toUpperCase());
                        currentGuess += String.valueOf(e.getKeyChar()).toUpperCase();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        // Game#guess method throws NotAWordException if the guess is not a valid word
                        char[] colors = game.guess(currentGuess);
                        setColors(colors);
                        currentGuess = "";
                        letterNumber = 0;
                        if (won(colors)) {
                            new Thread(() -> {
                                try {Thread.sleep(2250);} catch (InterruptedException ex) {ex.printStackTrace();}
                                SwingUtilities.invokeLater(() -> newGame());
                            }).start();
                        } else if (guessNumber == Game.GUESSES - 1) {
                            new Thread(() -> {
                                try {Thread.sleep(2250);} catch (InterruptedException ex) {ex.printStackTrace();}
                                SwingUtilities.invokeLater(() -> {
                                    System.out.println(game.getAnswer());
                                    newGame();
                                });
                            }).start();
                        }
                    } catch (NotAWordException ex) {
                        flashRed();
                    }
                }
            }
        });

        setVisible(true);
    }

    private void flashRed() {
        int guessNumber = game.getGuessNumber();

        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < Game.WORD_SIZE; i++) {
                JPanel panel = panels[guessNumber][i];
                panel.setBackground(Color.RED);
                panel.getComponent(0).setForeground(Color.WHITE);
                panel.setBorder(BorderFactory.createLineBorder(Color.RED, (int) (.005 * FRAME_HEIGHT)));
            }
        });

        new Thread(() -> {
            try {Thread.sleep(500);} catch (InterruptedException ignored) {}

            SwingUtilities.invokeLater(() -> {
                for (int i = 0; i < Game.WORD_SIZE; i++) {
                    JPanel panel = panels[guessNumber][i];
                    panel.setBackground(Color.WHITE);
                    panel.getComponent(0).setForeground(Color.BLACK);
                    panel.setBorder(BorderFactory.createLineBorder(new Color(215, 215, 215), (int) (.005 * FRAME_HEIGHT)));
                }
            });
        }).start();
    }


    private boolean won(char[] colors) {
        for (char c : colors) {
            if (c != 'G') {
                return false;
            }
        }
        return true;
    }

    public void setColors(char[] colors) {
        new Thread(() -> {
            for (int i = 0; i < colors.length; i++) {
                final int index = i;
                SwingUtilities.invokeLater(() -> {
                    JPanel panel = panels[game.getGuessNumber() != 0 ? game.getGuessNumber() - 1 : 0][index];
                    panel.getComponent(0).setForeground(Color.WHITE);
                    Color color = switch (colors[index]) {
                        case 'B' -> new Color(120, 124, 127);
                        case 'Y' -> new Color(200, 182, 83);
                        case 'G' -> new Color(108, 169, 101);
                        default -> throw new RuntimeException("Invalid color.");
                    };
                    panel.setBackground(color);
                    panel.setBorder(BorderFactory.createLineBorder(color, (int) (.005 * FRAME_HEIGHT)));
                });

                try {Thread.sleep(250);} catch (InterruptedException ignored) {}
            }
        }).start();
    }

    public void newGame() {
        String newWord = "";
        try (Scanner dictionary = new Scanner(new File("WordleWords.txt"))) {
            int rand = (int) (Math.random() * 2315);
            for (int i = 0; i < rand; i++) {
                dictionary.next();
            }
            newWord = dictionary.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {clear();} catch (NullPointerException ignored) {};
        game = new Game(newWord.toUpperCase());
    }

    private void clear() {
        for (int row = 0; row < Game.GUESSES; row++) {
            for (int col = 0; col < Game.WORD_SIZE; col++) {
                JPanel panel = panels[row][col];
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createLineBorder(new Color(215, 215, 215), (int) (.005 * FRAME_HEIGHT)));
                JLabel label = (JLabel) panel.getComponent(0);
                label.setText("");
                label.setForeground(Color.BLACK);
            }
        }
    }

    public static void main(String[] args) {
        new Graphic();
    }
}

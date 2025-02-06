package snakepackage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.*;

import enums.GridSize;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jd-
 *
 */

public class SnakeApp {

    private static SnakeApp app;
    public static final int MAX_THREADS = 8;
    Snake[] snakes = new Snake[MAX_THREADS];
    private static final Cell[] spawn = {
            new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(GridSize.GRID_WIDTH - 2,
                    3 * (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
            new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
            new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
            new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2,
                    GridSize.GRID_HEIGHT - 2)};
    private static JFrame frame;
    private static Board board;
    int nr_selected = 0;
    Thread[] thread = new Thread[MAX_THREADS];
    private final Object pauseLock = new Object();
    private JLabel stats;


    private JPanel startScreen;

    private JPanel actionsBPabel;

    public SnakeApp() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame("The Snake Race");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 17,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 40);
        frame.setLocation(dimension.width / 2 - frame.getWidth() / 2,
                dimension.height / 2 - frame.getHeight() / 2);


        startScreen = createStartScreen();

        frame.add(startScreen, BorderLayout.CENTER);
        stats = new JLabel("Estadísticas del Juego");
        frame.add(stats, BorderLayout.NORTH);


        actionsBPabel = new JPanel();
        actionsBPabel.setLayout(new FlowLayout());

        JButton inicio = new JButton("Inicio");
        JButton pausar = new JButton("Pausar");
        JButton reanudar = new JButton("Reanudar");

        actionsBPabel.add(inicio);
        actionsBPabel.add(pausar);
        actionsBPabel.add(reanudar);


        actionsBPabel.setVisible(false);

        frame.add(actionsBPabel, BorderLayout.SOUTH);

        inicio.addActionListener(e -> startGame(inicio, pausar, reanudar));

        pausar.addActionListener(e -> {
            try {
                pauseGame(inicio, pausar, reanudar);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        reanudar.addActionListener(e -> resumeGame(inicio, pausar, reanudar));
    }


    private JPanel createStartScreen() {
        JPanel startPanel = new JPanel();
        startPanel.setLayout(new BorderLayout());
        startPanel.setBackground(new Color(144, 238, 144));

        JLabel titleLabel = new JLabel("Snake Game", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
        titleLabel.setForeground(Color.BLACK);

        JButton startButton = new JButton("Iniciar Juego");
        startButton.setFont(new Font("Arial", Font.PLAIN, 20));
        startButton.addActionListener(e -> startGameOnStartScreen());

        startPanel.add(titleLabel, BorderLayout.CENTER);
        startPanel.add(startButton, BorderLayout.SOUTH);

        return startPanel;
    }


    private void startGameOnStartScreen() {
        frame.remove(startScreen);
        board = new Board();
        frame.add(board, BorderLayout.CENTER);
        frame.repaint();
        frame.revalidate();


        actionsBPabel.setVisible(true);


        for (int i = 0; i < MAX_THREADS; i++) {
            snakes[i] = new Snake(i + 1, spawn[i], i + 1);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
            thread[i].start();
        }
    }

    private void startGame(JButton startButton, JButton pauseButton, JButton resumeButton) {
        if (thread[0] != null && thread[0].isAlive()) {
            return;
        }

        for (int i = 0; i < MAX_THREADS; i++) {
            snakes[i] = new Snake(i + 1, spawn[i], i + 1);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
            thread[i].start();
        }

        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
    }

    private void pauseGame(JButton inicio, JButton pauseButton, JButton resumeButton) throws InterruptedException {
        synchronized (pauseLock) {
            for (int i = 0; i < MAX_THREADS; i++) {
                if (snakes[i] != null) {
                    snakes[i].pause();
                }
            }
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(true);
            inicio.setEnabled(true);

            showGameStats();
        }
    }

    private void resumeGame(JButton inicio, JButton pauseButton, JButton resumeButton) {
        synchronized (pauseLock) {
            for (int i = 0; i < MAX_THREADS; i++) {
                if (snakes[i] != null) {
                    snakes[i].resume();
                }
            }
            pauseButton.setEnabled(true);
            resumeButton.setEnabled(false);
            inicio.setEnabled(true);
            pauseLock.notifyAll();
        }
    }

    private void showGameStats() {
        Snake longestSnake = null;
        Snake firstDeadSnake = null;

        for (Snake snake : snakes) {
            if (longestSnake == null || snake.getBodyLength() > longestSnake.getBodyLength()) {
                longestSnake = snake;
            }

            if (firstDeadSnake == null || (snake.isSnakeEnd() && snake.getDeadTime() < firstDeadSnake.getDeadTime())) {
                firstDeadSnake = snake;
            }
        }

        if (longestSnake != null && firstDeadSnake != null) {
            stats.setText("<html>Serpiente viva más larga: " + longestSnake.getIdt() + "<br>" +
                    "Tamaño de la serpiente viva mas larga: " + longestSnake.getBodyLength() + "<br>" +
                    "Peor serpiente (primera en morir): " + firstDeadSnake.getIdt() + "</html>");
        }
    }

    public static void main(String[] args) {
        app = new SnakeApp();
        frame.setVisible(true);
    }

    public static SnakeApp getApp() {
        return app;
    }
}

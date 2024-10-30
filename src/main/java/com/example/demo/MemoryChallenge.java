package com.example.demo;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
public class MemoryChallenge extends Application {
    private static final int GRID_SIZE = 6; // Изменено для 30 карточек (5x6 grid)
    private static final int PAIRS = 15;
    private GameBoard gameBoard;
    private Scoreboard scoreboard;
    private GameManager gameManager; // Добавляем это поле
    private GridPane gridPane;
    @Override
    public void start(Stage primaryStage) {
        // Создание диалогового окна для ввода имени игрока
        TextInputDialog dialog = new TextInputDialog("Player");
        dialog.setTitle("Welcome to Memory Challenge");
        dialog.setHeaderText("Enter Your Name");
        Optional<String> result = dialog.showAndWait();

        // Если введено имя, инициализация объекта GameManager
        result.ifPresent(name -> gameManager = new GameManager(new GameBoard(GRID_SIZE, PAIRS), new Scoreboard(), new Player(name)));

        // Инициализация основных компонентов интерфейса
        gameBoard = new GameBoard(GRID_SIZE, PAIRS);
        scoreboard = new Scoreboard();
        gridPane = new GridPane();

        // Установка цвета фона GridPane
        gridPane.setStyle("-fx-background-color: #ADD8E6;");

        // Инициализация кнопок для игрового поля
        initializeButtons();

        // Создание сцены и отображение основного окна
        Scene scene = new Scene(gridPane);
        primaryStage.setTitle("Memory Challenge");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeButtons() {
        // Инициализация строк и столбцов в GridPane
        for (int i = 0; i < GRID_SIZE-1; i++) {
            gridPane.getRowConstraints().add(new RowConstraints(220)); // Высота каждой строки
            gridPane.getColumnConstraints().add(new ColumnConstraints(220)); // Ширина каждого столбца
        }

        // Создание кнопок и карт для игрового поля
        for (int i = 0; i < GRID_SIZE-1; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                final int row = i;
                final int col = j;
                Card card = gameBoard.getCard(row, col);
                Button button = new Button();
                button.setPrefSize(250, 250);
                button.setStyle("-fx-background-color: #C8A2C8; " +
                        "-fx-border-color: #9c7b9c; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 5px;");
                button.setOnAction(e -> handleCardClick(row, col, button));
                gridPane.add(button, col, row);
            }
        }
    }

    private void handleCardClick(int i, int j, Button button) {
        // Обработка нажатия на кнопку с картой
        Card card = gameBoard.getCard(i, j);

        // Проверка, можно ли открыть карту
        if (card.isClickable() && !card.isOpen() && gameBoard.getOpenCardCount() < 2) {
            gameBoard.openCard(card);

            // Отображение изображения на кнопке
            Image img = new Image(getClass().getResourceAsStream(card.getImagePath()));
            ImageView view = new ImageView(img);
            view.setFitHeight(200);
            view.setFitWidth(200);
            view.setPreserveRatio(true);
            button.setGraphic(view);

            // Проверка, совпадают ли открытые карты
            if (gameBoard.getOpenCardCount() == 2) {
                // Пауза перед проверкой совпадения
                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(e -> {
                    if (gameBoard.areOpenCardsMatching()) {
                        Pairs_num++; // Увеличение счетчика найденных пар
                        System.out.println(Pairs_num);
                        if (Pairs_num == 15) {
                            gameManager.endGame(true); // Завершение игры, если найдены все пары
                        }
                        gameBoard.removeMatchedPair();
                    } else {
                        gameBoard.closeOpenCards();
                    }
                    updateButtons(); // Обновление состояния кнопок независимо от результата
                });
                pause.play();
            }
        } else if (card.isOpen() && gameBoard.getOpenCardCount() < 2) {
            // Закрытие открытой карты при повторном клике
            gameBoard.closeOpenCards();
            updateButtons();
        }
    }
    private int Pairs_num = 0; // Переменная для подсчета найденных пар

    private void updateButtons() {
        // Обновление состояния кнопок на игровом поле
        for (Node node : gridPane.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                Integer rowIndex = GridPane.getRowIndex(node);
                Integer colIndex = GridPane.getColumnIndex(node);
                rowIndex = rowIndex == null ? 0 : rowIndex;
                colIndex = colIndex == null ? 0 : colIndex;
                Card card = gameBoard.getCard(rowIndex, colIndex);

                // Проверка состояния карты и установка соответствующего состояния кнопки
                if (card != null && !card.isOpen() && card.isClickable()) {
                    button.setGraphic(null); // Удаление изображения
                    button.setDisable(false);
                } else if (card != null && card.isOpen() && card.isClickable()) {
                    // Ничего не меняем, если нужно оставить изображение на открытой карте
                } else if (card != null && !card.isClickable()) {
                    gameBoard.closeOpenCards();
                    button.setGraphic(null); // Удаление изображения
                    button.setDisable(true);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
class Card {
    private String content = "C:\\Users\\Пк\\IdeaProjects\\demo\\src\\main\\resources\\images\\";
    private boolean isOpen;
    private boolean clickable;
    private boolean isMatched;

    // Геттеры и сеттеры для полей класса
    public boolean isMatched() {
        return isMatched;
    }
    public String getImagePath() {
        return content;
    }
    public void setMatched(boolean matched) {
        isMatched = matched;
    }
    public Card(String content) {
        this.content = content;
        this.isOpen = false;
        this.clickable = true; // Карта изначально кликабельна
    }
    public String getContent() {
        return content;
    }
    public boolean isOpen() {
        return isOpen;
    }
    public void setOpen(boolean open) {
        isOpen = open;
    }
    public boolean isClickable() {
        return clickable;
    }
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
}
class GameBoard {
    // Поля класса GameBoard
    private Card[][] cards;
    private CardPair currentPair;
    private CardPair matchedPair;
    private int gridSize;
    private int pairs;
    // Метод получения количества открытых карт
    public int getOpenCardCount() {
        int count = 0;
        for (Card[] row : cards) {
            for (Card card : row) {
                if (card != null && card.isOpen()) {
                    count++;
                }
            }
        }
        return count;
    }
    // Метод закрытия открытых карт
    public void closeOpenCards() {
        for (Card[] row : cards) {
            for (Card card : row) {
                if (card != null && card.isOpen() && !card.isMatched()) {
                    card.setOpen(false);
                }
            }
        }
        // Обнуляем текущую пару
        currentPair = null;
    }
    // Метод проверки совпадения открытых карт
    public boolean areOpenCardsMatching() {
        Card[] openCards = new Card[2];
        int index = 0;
        for (Card[] row : cards) {
            for (Card card : row) {
                if (card != null && card.isOpen()) {
                    openCards[index++] = card;
                }
            }
        }
        return openCards[0] != null && openCards[1] != null &&
                openCards[0].getContent().equals(openCards[1].getContent());
    }
    // Метод удаления совпавшей пары
    public void removeMatchedPair() {
        if (currentPair != null && currentPair.isMatched()) {
            // Устанавливаем карты как совпавшие и некликабельные
            currentPair.getFirstCard().setMatched(true);
            currentPair.getFirstCard().setClickable(false);
            currentPair.getFirstCard().setMatched(false);
            currentPair.getSecondCard().setMatched(true);
            currentPair.getSecondCard().setClickable(false);
            currentPair.getSecondCard().setMatched(false);
            // Обнуляем текущую пару
            currentPair = null;
        }
    }
    // Конструктор класса GameBoard
    public GameBoard(int gridSize, int pairs) {
        this.gridSize = gridSize;
        this.pairs = pairs;
        initializeCards(gridSize, pairs);
    }
    private void initializeCards(int gridSize, int pairs) {
        cards = new Card[gridSize][gridSize];
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < pairs; i++) {
            String imagePath = "images/image" + (i + 1) + ".jpg";
            Card card1 = new Card(imagePath);
            Card card2 = new Card(imagePath);
            deck.add(card1);
            deck.add(card2);
        }
        Collections.shuffle(deck);
        int index = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (index < deck.size()) {
                    cards[i][j] = deck.get(index++);
                }
            }
        }
    }
    public Card getCard(int row, int col) {
        if (row >= 0 && row < gridSize && col >= 0 && col < gridSize) {
            return cards[row][col];
        } else {
            return null; // или обработать ошибку
        }
    }
    public boolean openCard(Card card) {
        if (card.isOpen()) {
            // Карта уже открыта, открывать её снова не нужно
            return false;
        } else if (currentPair == null) {
            // Ни одна карта еще не выбрана, это будет первая карта в паре
            currentPair = new CardPair(card, null);
            card.setOpen(true);
            return true;
        } else if (currentPair.getFirstCard() != null && currentPair.getSecondCard() == null) {
            // Первая карта выбрана, но вторая - нет, это будет вторая карта в паре
            currentPair.setSecondCard(card);
            card.setOpen(true);
            // Проверяем, совпадают ли карты
            currentPair.checkAndMarkAsMatched();
            return true;
        } else {
            // Уже есть две выбранные карты, необходимо сбросить текущую пару
            resetPair();
            currentPair = new CardPair(card, null);
            card.setOpen(true);
            return true;
        }
    }
    public void resetPair() {
        if (currentPair != null && !currentPair.isMatched()) {
            currentPair.getFirstCard().setOpen(false);
            if (currentPair.getSecondCard() != null) {
                currentPair.getSecondCard().setOpen(false);
            }
        }
        currentPair = null;
    }
}
class GameManager {
    private GameBoard gameBoard;
    private Scoreboard scoreboard;
    private Player currentPlayer;
    private boolean isGameActive;
    private Timeline timeline;
    private int secondsPassed = 0;
    private int timer;
    // Конструктор с тремя параметрами
    public GameManager(GameBoard gameBoard, Scoreboard scoreboard, Player currentPlayer) {
        this.gameBoard = gameBoard;
        this.scoreboard = scoreboard;
        this.currentPlayer = currentPlayer;
        this.isGameActive = true;
        this.timer = 5000; // Начальное значение таймера
        startGame(); // Запуск игры и таймера
    }
    private void startGame() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (isGameActive) {
                secondsPassed++;
                // Можно обновлять UI, показывая время
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    private void saveScore(String name, int score) {
        try (FileWriter writer = new FileWriter("scores.txt", true)) {
            writer.write(name + ": " + score + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private List<PlayerScore> readAndSortScoresFromFile() {
        List<PlayerScore> scores = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("scores.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(": ");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    int score = Integer.parseInt(parts[1].trim());
                    scores.add(new PlayerScore(name, score));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(scores);
        return scores;
    }
    private class PlayerScore implements Comparable<PlayerScore> {
        String name;
        int score;
        public PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
        @Override
        public int compareTo(PlayerScore other) {
            return Integer.compare(this.score, other.score);
        }
    }
    private void displayScoreboard(Stage stage, String currentPlayerName) {
        List<PlayerScore> scores = readAndSortScoresFromFile();
        // Создаем текстовую область для вывода результатов
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        StringBuilder content = new StringBuilder();
        for (PlayerScore score : scores) {
            if (score.name.equals(currentPlayerName)) {
                content.append("*").append(score.name).append(": ").append(score.score).append("*\n");
            } else {
                content.append(score.name).append(": ").append(score.score).append("\n");
            }
        }
        textArea.setText(content.toString());
        // Создаем новое окно для отображения результатов
        Scene scene = new Scene(textArea, 400, 600);
        Stage newStage = new Stage();
        newStage.setTitle("Scoreboard");
        newStage.setScene(scene);
        newStage.show();
    }
    void endGame(boolean isWin) {
        isGameActive = false;
        timeline.stop(); // Останавливаем таймер
        currentPlayer.setScore(secondsPassed);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText("Score: " + currentPlayer.getScore());
            alert.showAndWait();
            saveScore(currentPlayer.getName(), currentPlayer.getScore());
            // Отображаем таблицу результатов
            displayScoreboard(new Stage(), currentPlayer.getName());
        });
    }
}
class Scoreboard {
    private int score;
    public Scoreboard() {
        this.score = 0;
    }
}
class CardPair {
    private Card firstCard;
    private Card secondCard;
    private boolean isMatched;
    public CardPair(Card firstCard, Card secondCard) {
        this.firstCard = firstCard;
        this.secondCard = secondCard;
        this.isMatched = false;
    }
    public void setSecondCard(Card secondCard) {
        this.secondCard = secondCard;
    }
    public boolean isPair() {
        return firstCard.getContent().equals(secondCard.getContent());
    }
    public void checkAndMarkAsMatched() {
        if (isPair()) {
            firstCard.setOpen(true);
            secondCard.setOpen(true);
            isMatched = true;
        }
    }
    public boolean isMatched() {
        return isMatched;
    }
    public Card getFirstCard() {
        return firstCard;
    }
    public Card getSecondCard() {
        return secondCard;
    }
}
class Player {
    private String name;
    private int score;
    private int lives;
    private List<CardPair> matchedPairs;
    public Player(String name) {
        this.name = name;
        this.score = 0; // Инициализируем score
        this.lives = 5; // Начальное количество жизней может быть изменено
        this.matchedPairs = new ArrayList<>();
    }
    public void setScore(int score) {
        this.score = score;
    }
    public int getScore() {
        return score;
    }
    public String getName() {
        return name;
    }
    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", score=" + score +
                ", lives=" + lives +
                ", matchedPairs=" + matchedPairs.size() +
                '}';
    }
}
package com.example.mazecellbtl;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

public class MazeGame {
    public int[][] maze;
    public enum Direction { UP, DOWN, LEFT, RIGHT }
    public enum MoveStatus { QUESTION, INVALID_MOVE, WIN, GAME_OVER }

    public static class Question {
        String questionText;
        String[] answers;
        int correctAnswerIndex;

        public Question(String questionText, String[] answers, int correctAnswerIndex) {
            this.questionText = questionText;
            this.answers = answers;
            this.correctAnswerIndex = correctAnswerIndex;
        }
    }

    public static class MoveResult {
        public MoveStatus status;
        public Question question;

        public MoveResult(MoveStatus status, Question question) {
            this.status = status;
            this.question = question;
        }
    }

    private int playerX, playerY;
    private List<Question> questions;
    private int previousX, previousY;
    private DatabaseHelper dbHelper;
    private Map<String, Question> questionPositions;
    private Set<Integer> usedQuestionPositions;
    private List<Question> availableQuestions;

    public int[][] getMaze() {
        return maze;
    }
    public MazeGame(Context context) {
        dbHelper = new DatabaseHelper(context);
        usedQuestionPositions = new HashSet<>();
        initializeMaze();
        initializeQuestions();
        playerX = 0;
        playerY = 0;
    }

    private void initializeMaze() {
        maze = new int[5][5];
        Random random = new Random();

        // 1. Tạo mê cung ngẫu nhiên với nhiều đường đi
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                // 70% cơ hội là đường đi, 30% là tường
                maze[i][j] = (random.nextDouble() < 0.7) ? 1 : 0;
            }
        }

        // Đảm bảo điểm bắt đầu và kết thúc là đường đi
        maze[0][0] = 1; // Start
        maze[4][4] = 1; // End

        // 2. Tìm tất cả đường đi có thể đến đích
        List<List<int[]>> allPaths = findAllPaths();

        // 3. Nếu không có đường đi nào đến đích, tạo lại mê cung
        while (allPaths.isEmpty()) {
            // Reset và tạo lại mê cung
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    maze[i][j] = (random.nextDouble() < 0.7) ? 1 : 0;
                }
            }
            maze[0][0] = 1;
            maze[4][4] = 1;
            allPaths = findAllPaths();
        }

        // 4. Lấy câu hỏi từ database
        questions = dbHelper.getAllQuestions();
        questionPositions = new HashMap<>();
        List<Question> questionsCopy = new ArrayList<>(questions);

        // 5. Đặt câu hỏi trên các đường đi
        Set<String> questionCells = new HashSet<>();
        for (List<int[]> path : allPaths) {
            // Chọn ít nhất một vị trí trên mỗi đường đi để đặt câu hỏi
            int randomIndex = 1 + random.nextInt(path.size() - 2); // Không đặt ở start hoặc end
            int[] pos = path.get(randomIndex);
            String posKey = pos[0] + "," + pos[1];

            if (!questionCells.contains(posKey) && !questionsCopy.isEmpty()) {
                questionCells.add(posKey);
                maze[pos[0]][pos[1]] = 2;
                Question question = questionsCopy.remove(0);
                questionPositions.put(posKey, question);
            }
        }

        // Đặt các câu hỏi còn lại vào các vị trí khác trên đường đi
        for (int i = 0; i < 5 && !questionsCopy.isEmpty(); i++) {
            for (int j = 0; j < 5 && !questionsCopy.isEmpty(); j++) {
                String posKey = i + "," + j;
                if (maze[i][j] == 1 && !questionCells.contains(posKey)
                        && !(i == 0 && j == 0) && !(i == 4 && j == 4)) {
                    maze[i][j] = 2;
                    questionPositions.put(posKey, questionsCopy.remove(0));
                }
            }
        }
    }

    private List<List<int[]>> findAllPaths() {
        List<List<int[]>> allPaths = new ArrayList<>();
        boolean[][] visited = new boolean[5][5];
        List<int[]> currentPath = new ArrayList<>();
        findPaths(0, 0, visited, currentPath, allPaths);
        return allPaths;
    }

    private void findPaths(int x, int y, boolean[][] visited,
                           List<int[]> currentPath, List<List<int[]>> allPaths) {
        if (x < 0 || x >= 5 || y < 0 || y >= 5 ||
                maze[x][y] == 0 || visited[x][y]) {
            return;
        }

        currentPath.add(new int[]{x, y});
        visited[x][y] = true;

        if (x == 4 && y == 4) {
            allPaths.add(new ArrayList<>(currentPath));
        } else {
            findPaths(x + 1, y, visited, currentPath, allPaths); // xuống
            findPaths(x - 1, y, visited, currentPath, allPaths); // lên
            findPaths(x, y + 1, visited, currentPath, allPaths); // phải
            findPaths(x, y - 1, visited, currentPath, allPaths); // trái
        }

        currentPath.remove(currentPath.size() - 1);
        visited[x][y] = false;
    }

    private void initializeQuestions() {
        // Lấy tất cả câu hỏi từ database
        availableQuestions = new ArrayList<>(dbHelper.getAllQuestions());
        questionPositions = new HashMap<>();
        
        // Nếu không có câu hỏi trong DB, thêm câu hỏi mặc định
        if (availableQuestions.isEmpty()) {
            dbHelper.addQuestion(
                "Thủ đô của Việt Nam là gì?",
                "Hà Nội", "Hồ Chí Minh", "Đà Nẵng",
                0
            );
            availableQuestions = dbHelper.getAllQuestions();
        }

        // Xáo trộn danh sách câu hỏi
        Collections.shuffle(availableQuestions);
        
        // Tạo một bản sao của danh sách câu hỏi để sử dụng
        List<Question> unusedQuestions = new ArrayList<>(availableQuestions);
        
        // Đếm số ô câu hỏi trong mê cung
        List<int[]> questionCells = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (maze[i][j] == 2) {
                    questionCells.add(new int[]{i, j});
                }
            }
        }

        // Nếu có nhiều ô hơn số câu hỏi, giảm số ô câu hỏi
        if (questionCells.size() > unusedQuestions.size()) {
            // Chuyển một số ô câu hỏi thành đường đi bình thường
            int excess = questionCells.size() - unusedQuestions.size();
            for (int i = 0; i < excess; i++) {
                int[] cell = questionCells.get(i);
                maze[cell[0]][cell[1]] = 1; // Chuyển thành đường đi
            }
            // Cập nhật lại danh sách ô câu hỏi
            questionCells = questionCells.subList(excess, questionCells.size());
        }

        // Gán câu hỏi vào các ô
        for (int i = 0; i < questionCells.size() && i < unusedQuestions.size(); i++) {
            int[] cell = questionCells.get(i);
            String posKey = cell[0] + "," + cell[1];
            Question question = unusedQuestions.get(i);
            questionPositions.put(posKey, question);
            
            Log.d("MazeGame", "Placed question at " + posKey + ": " + question.questionText);
        }

        Log.d("MazeGame", "Total questions placed: " + questionPositions.size());
        Log.d("MazeGame", "Available questions: " + unusedQuestions.size());
    }

    public Question getQuestionAt(int x, int y) {
        String posKey = x + "," + y;
        return questionPositions.get(posKey);
    }

    // Thêm phương thức để kiểm tra xem có phải ô câu hỏi không
    public boolean isQuestionCell(int x, int y) {
        return maze[x][y] == 2;
    }

    public MoveResult movePlayer(Direction direction) {
        previousX = playerX;
        previousY = playerY;

        int newX = playerX;
        int newY = playerY;

        switch (direction) {
            case UP: newX--; break;
            case DOWN: newX++; break;
            case LEFT: newY--; break;
            case RIGHT: newY++; break;
        }

        // Kiểm tra xem vị trí mới có hợp lệ không
        if (newX < 0 || newX >= maze.length || newY < 0 || newY >= maze[0].length) {
            return new MoveResult(MoveStatus.INVALID_MOVE, null);
        }

        // Kiểm tra nếu ô đó là tường
        if (maze[newX][newY] == 0) {
            return new MoveResult(MoveStatus.INVALID_MOVE, null);
        }

        // Kiểm tra nếu có câu hỏi
        if (maze[newX][newY] == 2) {
            Question currentQuestion = getQuestionForCell(newX, newY);
            if (currentQuestion != null) {
                playerX = newX;
                playerY = newY;
                return new MoveResult(MoveStatus.QUESTION, currentQuestion);
            }
        }

        playerX = newX;
        playerY = newY;

        if (playerX == 4 && playerY == 4) {
            return new MoveResult(MoveStatus.WIN, null);
        }

        return new MoveResult(MoveStatus.QUESTION, null);
    }

    public boolean checkAnswer(Question question, int selectedAnswerIndex) {
        if (selectedAnswerIndex == question.correctAnswerIndex) {
            maze[playerX][playerY] = 1;
            return true;
        } else {
            maze[playerX][playerY] = 0;
            playerX = previousX;
            playerY = previousY;
            return false;
        }
    }

    private Question getQuestionForCell(int x, int y) {
        // Kiểm tra nếu ô có chướng ngại vật (có câu hỏi)
        if (maze[x][y] == 2) { // Ví dụ: giá trị 2 có thể đại diện cho các ô có câu hỏi
            return questions.isEmpty() ? null : questions.get(new Random().nextInt(questions.size()));
        }

        // Nếu không có câu hỏi ở ô này, trả về null
        return null;
    }

    public boolean isValidPath(int x, int y) {
        // Xác định các con đường hợp lệ
        return maze[x][y] == 1;
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public int getPreviousX() {
        return previousX;
    }

    public int getPreviousY() {
        return previousY;
    }

    public boolean isGameOver() {
        return !hasPathToEnd();
    }

    private boolean canMove(int x, int y) {
        if (x < 0 || x >= maze.length || y < 0 || y >= maze[0].length) {
            return false;
        }
        
        return maze[x][y] == 1 || maze[x][y] == 2;
    }

    private boolean findPath(int x, int y, boolean[][] visited) {
        if (x < 0 || x >= 5 || y < 0 || y >= 5 || maze[x][y] == 0 || visited[x][y]) {
            return false;
        }

        if (x == 4 && y == 4) {
            return true;
        }

        visited[x][y] = true;

        boolean hasPath = findPath(x + 1, y, visited) || // xuống
                         findPath(x - 1, y, visited) || // lên
                         findPath(x, y + 1, visited) || // phải
                         findPath(x, y - 1, visited);   // trái

        return hasPath;
    }

    // Thêm phương thức kiểm tra còn đường đi đến đích không
    public boolean hasPathToEnd() {
        boolean[][] visited = new boolean[5][5];
        return findPath(playerX, playerY, visited);
    }
}
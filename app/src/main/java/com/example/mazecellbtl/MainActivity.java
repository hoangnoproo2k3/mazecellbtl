package com.example.mazecellbtl;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

public class MainActivity extends AppCompatActivity {
    private MazeGame mazeGame;
    private GridLayout mazeGridLayout;
    private TextView statusTextView;
    private Button upButton, downButton, leftButton, rightButton;
    private AppCompatTextView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mazeGridLayout = findViewById(R.id.mazeGridLayout);
        statusTextView = findViewById(R.id.statusTextView);
        upButton = findViewById(R.id.upButton);
        downButton = findViewById(R.id.downButton);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);

        mazeGame = new MazeGame(this);

        setupDirectionButtons();
        setupMazeGrid();
        drawMaze();

        // Thêm xử lý sự kiện cho nút Quản lý câu hỏi
        Button btnManageQuestions = findViewById(R.id.btnManageQuestions);
        btnManageQuestions.setOnClickListener(v -> showAdminLoginDialog());

        // Nút Chơi game (có thể thêm logic reset game nếu cần)
        Button btnRestart = findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(v -> {
            // Khởi tạo lại game
            mazeGame = new MazeGame(this);
            setupMazeGrid();
            drawMaze();
        });
    }

    private void setupMazeGrid() {
        mazeGridLayout.removeAllViews();
        // Tạo lưới 5x5
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                AppCompatTextView cellView = new AppCompatTextView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.columnSpec = GridLayout.spec(j, 1f);
                params.rowSpec = GridLayout.spec(i, 1f);
                params.setMargins(2, 2, 2, 2);

                cellView.setLayoutParams(params);
                cellView.setGravity(Gravity.CENTER);
                cellView.setTextSize(16);

                // Màu nền cho các ô
                if (mazeGame.getMaze()[i][j] == 1) {
                    cellView.setBackgroundColor(Color.LTGRAY);  // Đường đi
                } else if (mazeGame.getMaze()[i][j] == 0) {
                    cellView.setBackgroundColor(Color.DKGRAY);  // Tường
                } else if (mazeGame.getMaze()[i][j] == 2) {
                    // Thay đổi màu cho ô câu hỏi - chọn 1 trong các phương án sau:

                    // Phương án 1: Màu xanh nhạt
                    cellView.setBackgroundColor(Color.rgb(173, 216, 230));  // Light Blue

                    // Phương án 2: Màu cam nhạt
                    // cellView.setBackgroundColor(Color.rgb(255, 218, 185));  // PeachPuff

                    // Phương án 3: Màu xanh lá nhạt
                    // cellView.setBackgroundColor(Color.rgb(144, 238, 144));  // Light Green

                    // Phương án 4: Thêm biểu tượng câu hỏi
                    cellView.setText("❓");
                    cellView.setGravity(Gravity.CENTER);
                    cellView.setTextSize(20);
                }

                // Thêm biểu tượng kho báu ở điểm đích
                if (i == 4 && j == 4) {
                    cellView.setText("🏆");
                    cellView.setTextSize(20);
                    cellView.setGravity(Gravity.CENTER);
                }

                mazeGridLayout.addView(cellView);
            }
        }
    }

    private void setupDirectionButtons() {
        upButton.setOnClickListener(v -> movePlayer(MazeGame.Direction.UP));
        downButton.setOnClickListener(v -> movePlayer(MazeGame.Direction.DOWN));
        leftButton.setOnClickListener(v -> movePlayer(MazeGame.Direction.LEFT));
        rightButton.setOnClickListener(v -> movePlayer(MazeGame.Direction.RIGHT));
    }

    private void movePlayer(MazeGame.Direction direction) {
        // Di chuyển nhân vật và lấy kết quả di chuyển
        MazeGame.MoveResult result = mazeGame.movePlayer(direction);

        // Kiểm tra kết quả di chuyển và xử lý tương ứng
        switch (result.status) {
            case QUESTION:
                if (result.question != null) {
                    // Nếu có câu hỏi, hiển thị dialog câu hỏi
                    showQuestionDialog(result.question);
                }
                break;
            case INVALID_MOVE:
                // Thông báo không thể di chuyển nếu không hợp lệ
                Toast.makeText(this, "Không thể di chuyển theo hướng này", Toast.LENGTH_SHORT).show();
                break;
            case WIN:
                showWinDialog();
                break;
            case GAME_OVER:
                showGameOverDialog();
                break;
        }

        // Vẽ lại mê cung để cập nhật vị trí nhân vật
        drawMaze();
    }

    private void drawMaze() {
        // Xóa nhân vật cũ
        if (playerView != null) {
            mazeGridLayout.removeView(playerView);
        }

        // Tạo và thêm nhân vật mới
        playerView = new AppCompatTextView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 0;
        params.columnSpec = GridLayout.spec(mazeGame.getPlayerY(), 1f);
        params.rowSpec = GridLayout.spec(mazeGame.getPlayerX(), 1f);
        params.setMargins(2, 2, 2, 2);

        playerView.setLayoutParams(params);

        // Màu sắc tùy theo trạng thái ô
        int currentCell = mazeGame.getMaze()[mazeGame.getPlayerX()][mazeGame.getPlayerY()];
        if (currentCell == 1) {
            playerView.setBackgroundColor(Color.LTGRAY); // Đường đi
        } else if (currentCell == 0) {
            playerView.setBackgroundColor(Color.DKGRAY); // Tường
        } else if (currentCell == 2) {
            playerView.setBackgroundColor(Color.YELLOW); // Ô có câu hỏi
        }

        playerView.setText("🧑");
        playerView.setGravity(Gravity.CENTER);
        playerView.setTextSize(24);

        mazeGridLayout.addView(playerView);

        // Cập nhật trạng thái
        statusTextView.setText("Vị trí: (" + mazeGame.getPlayerX() + ", " + mazeGame.getPlayerY() + ")");
    }

    private void showQuestionDialog(MazeGame.Question question) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_question, null);
        builder.setView(dialogView);

        TextView questionText = dialogView.findViewById(R.id.questionText);
        Button btnAnswer1 = dialogView.findViewById(R.id.btnAnswer1);
        Button btnAnswer2 = dialogView.findViewById(R.id.btnAnswer2);
        Button btnAnswer3 = dialogView.findViewById(R.id.btnAnswer3);

        questionText.setText(question.questionText);
        btnAnswer1.setText(question.answers[0]);
        btnAnswer2.setText(question.answers[1]);
        btnAnswer3.setText(question.answers[2]);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Ngăn người dùng tắt dialog bằng cách nhấn ra ngoài

        btnAnswer1.setOnClickListener(v -> {
            checkQuestionAnswer(question, 0);
            dialog.dismiss();
        });

        btnAnswer2.setOnClickListener(v -> {
            checkQuestionAnswer(question, 1);
            dialog.dismiss();
        });

        btnAnswer3.setOnClickListener(v -> {
            checkQuestionAnswer(question, 2);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void checkQuestionAnswer(MazeGame.Question question, int selectedAnswer) {
        boolean isCorrect = mazeGame.checkAnswer(question, selectedAnswer);

        if (isCorrect) {
            showSuccessDialog("Tuyệt vời!", "Câu trả lời chính xác, hãy tiếp tục!");
        } else {
            if (!mazeGame.hasPathToEnd()) {
                showGameOverDialog();
            } else {
                showErrorDialog("Rất tiếc!", 
                    "Câu trả lời không chính xác. Con đường này đã bị chặn, hãy tìm đường khác!");
            }
        }

        setupMazeGrid();
        drawMaze();
    }

    private void showSuccessDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
               .setMessage(message)
               .setIcon(R.drawable.ic_success) // Thêm icon success vào res/drawable
               .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
               .show();
    }

    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
               .setMessage(message)
               .setIcon(R.drawable.ic_warning) // Thêm icon warning vào res/drawable
               .setPositiveButton("Đã hiểu", (dialog, which) -> dialog.dismiss())
               .show();
    }

    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_over, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        Button btnRestart = dialogView.findViewById(R.id.btnRestart);
        Button btnExit = dialogView.findViewById(R.id.btnExit);

        btnRestart.setOnClickListener(v -> {
            mazeGame = new MazeGame(this);
            setupMazeGrid();
            drawMaze();
            dialog.dismiss();
        });

        btnExit.setOnClickListener(v -> finish());

        dialog.show();
    }

    private void showWinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎉 Chúc mừng!");
        builder.setMessage("Bạn đã chiến thắng! Bạn đã tìm được kho báu!");
        builder.setPositiveButton("Chơi lại", (dialog, which) -> {
            // Khởi tạo lại game
            mazeGame = new MazeGame(this);
            setupMazeGrid();
            drawMaze();
        });
        builder.setNegativeButton("Thoát", (dialog, which) -> {
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showAdminLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_login, null);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);

        builder.setView(dialogView)
                .setTitle("Đăng nhập Admin")
                .setPositiveButton("Đăng nhập", (dialog, which) -> {
                    String password = passwordInput.getText().toString();
                    // Mật khẩu mặc định là "admin123"
                    if (password.equals("admin123")) {
                        Intent intent = new Intent(MainActivity.this, QuestionManagementActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Sai mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_manage_questions) {
            // Chuyển sang màn hình quản lý câu hỏi
            Intent intent = new Intent(this, QuestionManagementActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
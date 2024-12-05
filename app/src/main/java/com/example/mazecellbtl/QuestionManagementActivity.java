package com.example.mazecellbtl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class QuestionManagementActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ListView listViewQuestions;
    private List<MazeGame.Question> questionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_management);

        dbHelper = new DatabaseHelper(this);
        listViewQuestions = findViewById(R.id.listViewQuestions);
        Button btnAddQuestion = findViewById(R.id.btnAddQuestion);
        Button btnBack = findViewById(R.id.btnBack);

        btnAddQuestion.setOnClickListener(v -> showAddQuestionDialog());
        btnBack.setOnClickListener(v -> {
            finish(); // Đóng activity hiện tại và quay lại MainActivity
        });
        loadQuestions();
    }

    private void loadQuestions() {
        questionList = dbHelper.getAllQuestions();
        QuestionAdapter adapter = new QuestionAdapter();
        listViewQuestions.setAdapter(adapter);
    }

    private void showAddQuestionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_question, null);
        
        EditText editQuestion = dialogView.findViewById(R.id.editQuestion);
        EditText editAnswer1 = dialogView.findViewById(R.id.editAnswer1);
        EditText editAnswer2 = dialogView.findViewById(R.id.editAnswer2);
        EditText editAnswer3 = dialogView.findViewById(R.id.editAnswer3);
        EditText editCorrectAnswer = dialogView.findViewById(R.id.editCorrectAnswer);

        builder.setView(dialogView)
                .setTitle("Thêm câu hỏi mới")
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String question = editQuestion.getText().toString();
                    String answer1 = editAnswer1.getText().toString();
                    String answer2 = editAnswer2.getText().toString();
                    String answer3 = editAnswer3.getText().toString();
                    int correctAnswer = Integer.parseInt(editCorrectAnswer.getText().toString());

                    dbHelper.addQuestion(question, answer1, answer2, answer3, correctAnswer);
                    loadQuestions();
                    Toast.makeText(this, "Đã thêm câu hỏi", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private class QuestionAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return questionList.size();
        }

        @Override
        public Object getItem(int position) {
            return questionList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.question_list_item, parent, false);
            }

            MazeGame.Question question = questionList.get(position);
            TextView textViewQuestion = convertView.findViewById(R.id.textViewQuestion);
            Button btnEdit = convertView.findViewById(R.id.btnEdit);
            Button btnDelete = convertView.findViewById(R.id.btnDelete);

            textViewQuestion.setText(question.questionText);

            btnEdit.setOnClickListener(v -> showEditQuestionDialog(position));
            btnDelete.setOnClickListener(v -> showDeleteConfirmation(position));

            return convertView;
        }
    }

    private void showEditQuestionDialog(int position) {
        MazeGame.Question question = questionList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_question, null);

        EditText editQuestion = dialogView.findViewById(R.id.editQuestion);
        EditText editAnswer1 = dialogView.findViewById(R.id.editAnswer1);
        EditText editAnswer2 = dialogView.findViewById(R.id.editAnswer2);
        EditText editAnswer3 = dialogView.findViewById(R.id.editAnswer3);
        EditText editCorrectAnswer = dialogView.findViewById(R.id.editCorrectAnswer);

        // Điền dữ liệu hiện tại
        editQuestion.setText(question.questionText);
        editAnswer1.setText(question.answers[0]);
        editAnswer2.setText(question.answers[1]);
        editAnswer3.setText(question.answers[2]);
        editCorrectAnswer.setText(String.valueOf(question.correctAnswerIndex));

        builder.setView(dialogView)
                .setTitle("Sửa câu hỏi")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    // Cập nhật câu hỏi trong database
                    dbHelper.updateQuestion(
                        position + 1,
                        editQuestion.getText().toString(),
                        editAnswer1.getText().toString(),
                        editAnswer2.getText().toString(),
                        editAnswer3.getText().toString(),
                        Integer.parseInt(editCorrectAnswer.getText().toString())
                    );
                    loadQuestions();
                    Toast.makeText(this, "Đã cập nhật câu hỏi", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteConfirmation(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa câu hỏi này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteQuestion(position + 1);
                    loadQuestions();
                    Toast.makeText(this, "Đã xóa câu hỏi", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
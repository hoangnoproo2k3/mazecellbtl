package com.example.mazecellbtl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MazeGameDB";
    private static final int DATABASE_VERSION = 1;

    // Bảng Questions
    private static final String TABLE_QUESTIONS = "questions";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_QUESTION = "question_text";
    private static final String COLUMN_ANSWER1 = "answer1";
    private static final String COLUMN_ANSWER2 = "answer2";
    private static final String COLUMN_ANSWER3 = "answer3";
    private static final String COLUMN_CORRECT_ANSWER = "correct_answer";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_QUESTIONS_TABLE = "CREATE TABLE " + TABLE_QUESTIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_QUESTION + " TEXT,"
                + COLUMN_ANSWER1 + " TEXT,"
                + COLUMN_ANSWER2 + " TEXT,"
                + COLUMN_ANSWER3 + " TEXT,"
                + COLUMN_CORRECT_ANSWER + " INTEGER" + ")";
        db.execSQL(CREATE_QUESTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
        onCreate(db);
    }

    // Thêm câu hỏi mới
    public long addQuestion(String question, String answer1, String answer2, 
                          String answer3, int correctAnswer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUESTION, question);
        values.put(COLUMN_ANSWER1, answer1);
        values.put(COLUMN_ANSWER2, answer2);
        values.put(COLUMN_ANSWER3, answer3);
        values.put(COLUMN_CORRECT_ANSWER, correctAnswer);
        return db.insert(TABLE_QUESTIONS, null, values);
    }

    // Lấy tất cả câu hỏi
    public List<MazeGame.Question> getAllQuestions() {
        List<MazeGame.Question> questionList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_QUESTIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    String questionText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUESTION));
                    String answer1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANSWER1));
                    String answer2 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANSWER2));
                    String answer3 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANSWER3));
                    int correctAnswer = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CORRECT_ANSWER));

                    String[] answers = new String[]{answer1, answer2, answer3};
                    
                    // In log để debug
                    Log.d("DatabaseHelper", "Loaded question: " + questionText);
                    Log.d("DatabaseHelper", "Answers: " + answer1 + ", " + answer2 + ", " + answer3);
                    
                    questionList.add(new MazeGame.Question(questionText, answers, correctAnswer));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error loading questions: " + e.getMessage());
        } finally {
            cursor.close();
        }

        Log.d("DatabaseHelper", "Total questions loaded: " + questionList.size());
        
        return questionList;
    }

    // Xóa câu hỏi
    public void deleteQuestion(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_QUESTIONS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // Cập nhật câu hỏi
    public int updateQuestion(int id, String question, String answer1, 
                            String answer2, String answer3, int correctAnswer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUESTION, question);
        values.put(COLUMN_ANSWER1, answer1);
        values.put(COLUMN_ANSWER2, answer2);
        values.put(COLUMN_ANSWER3, answer3);
        values.put(COLUMN_CORRECT_ANSWER, correctAnswer);
        return db.update(TABLE_QUESTIONS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }
} 
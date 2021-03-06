package com.dispedia.work.dispedia;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Spinner;
import java.util.ArrayList;
import sqlite.SqliteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

public class EditActivity extends AppCompatActivity {

    protected String initWordName;
    protected String initReadName;
    protected String initContent;
    protected String changedWordName;
    protected String changedReadName;
    protected String changedContent;
    protected String id;
    protected String word;
    protected String kana;
    protected String content;

    private SqliteOpenHelper helper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);


        TextView flatLine = findViewById(R.id.flatLine);
        flatLine.setVisibility(View.GONE);

        Intent intent = this.getIntent();
        String mode = intent.getStringExtra("mode");

        RadioGroup radioEditMode = findViewById(R.id.radioEditMode);
        RadioButton register = findViewById(R.id.registerMode);

        // TODO デバッグ用コード
        // 現在登録ボタンからしか遷移できず、mode = "R"しか設定されない為、動作確認用にmodeを設定
        //mode = "E";

        // 新規登録の場合
        if ("R".equals(mode)) {
            radioEditMode.check(R.id.registerMode);
            radioEditMode.setVisibility(View.GONE);

            // 編集の場合
        } else if ("E".equals(mode)) {
            register.setVisibility(View.GONE);
            radioEditMode.check(R.id.editMode);

            EditText inputWordName = findViewById(R.id.inputWordName);
            EditText inputReadName = findViewById(R.id.inputReadName);
            EditText inputContent = findViewById(R.id.inputContent);
            // TODO デバッグ用コード
            // 本来は詳細画面から値を受け取り設定する
//            id = intent.getStringExtra("id");
//            initWordName = intent.getStringExtra("word");
//            initReadName = intent.getStringExtra("kana");
//            initContent = intent.getStringExtra("content");
            id = "0005";
            initWordName = "HTML5";
            initReadName = "えいちてぃーえむえるふぁいぶ";
            initContent = "HyperText Markup Languageの第5版。";
            inputWordName.setText(initWordName);
            inputReadName.setText(initReadName);
            inputContent.setText(initContent);
        }

        radioEditMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton edit = findViewById(R.id.editMode);
                RadioButton delete = findViewById(R.id.deleteMode);
                EditText inputWordName = findViewById(R.id.inputWordName);
                EditText inputReadName = findViewById(R.id.inputReadName);
                EditText inputContent = findViewById(R.id.inputContent);
                Spinner spinnerRelationTag = findViewById(R.id.spinnerRelationTag);

                // 編集を選択した場合
                if (edit.isChecked()) {
                    inputWordName.setFocusable(true);
                    inputReadName.setFocusable(true);
                    inputContent.setFocusable(true);
                    spinnerRelationTag.setFocusable(true);
                    inputWordName.setEnabled(true);
                    inputReadName.setEnabled(true);
                    inputContent.setEnabled(true);
                    spinnerRelationTag.setEnabled(true);

                    inputWordName.setText(changedWordName);
                    inputReadName.setText(changedReadName);
                    inputContent.setText(changedContent);

                // 削除を選択した場合
                } else if (delete.isChecked()) {
                    inputWordName.setEnabled(false);
                    inputReadName.setEnabled(false);
                    inputContent.setEnabled(false);
                    spinnerRelationTag.setEnabled(false);
                    inputWordName.setFocusable(false);
                    inputReadName.setFocusable(false);
                    inputContent.setFocusable(false);
                    spinnerRelationTag.setFocusable(false);

                    changedWordName = inputWordName.getText().toString();
                    changedReadName = inputReadName.getText().toString();
                    changedContent = inputContent.getText().toString();

                    inputWordName.setText(initWordName);
                    inputReadName.setText(initReadName);
                    inputContent.setText(initContent);
                }
            }
        });

        Button executeButton = findViewById(R.id.executeButton);
        executeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // SQLite接続のための初期処理
                if(helper == null){
                    helper = new SqliteOpenHelper(getApplicationContext());
                }
                if(db == null){
                    db = helper.getWritableDatabase();
                }
                TextView msg = findViewById(R.id.errorMsg);
                //msg.setVisibility(View.GONE);

                // モードにより処理を分岐する
                RadioButton register = findViewById(R.id.registerMode);
                RadioButton edit = findViewById(R.id.editMode);
                RadioButton delete = findViewById(R.id.deleteMode);

                // 項目を変数に格納
                EditText inputWordName = findViewById(R.id.inputWordName);
                EditText inputReadName = findViewById(R.id.inputReadName);
                EditText inputContent = findViewById(R.id.inputContent);
                word = inputWordName.getText().toString();
                kana = inputReadName.getText().toString();
                content = inputContent.getText().toString();

                // 登録の場合
                if (register.isChecked()) {
                    boolean isError = isNullError(word, kana, content);
                    if (!isError) {
                        long insertData = insertData(db, id, word, kana, content);

                        // 正常の場合、詳細画面に遷移し登録完了のメッセージを表示する。
                        if (insertData != -1) {
                            Intent intent = new Intent(getApplication(), DetailActivity.class);

                            intent.putExtra("id", id);
                            intent.putExtra("word", word);
                            intent.putExtra("kana", kana);
                            intent.putExtra("content", content);
                            intent.putExtra("mode", "R");

                            startActivity(intent);

                            // DBの通信処理が不正の場合、エラーメッセージを表示する。
                        } else {
                            setMsg(msg, "登録に失敗しました。", Color.RED);
                        }
                    }

                    // 編集の場合
                } else if(edit.isChecked()) {
                    boolean isError = isNullError(word, kana, content);
                    if (!isError) {

                        int updateData = updateData(db, id, word, kana, content);

                        // 正常の場合、詳細画面に遷移し登録完了のメッセージを表示する。
                        // SQLiteDatabase.insert の戻り値は更新した行数のため、1行の場合正常
                        if (updateData == 1) {
                            Intent intent = new Intent(getApplication(), DetailActivity.class);
                            // TODO 登録時にidを生成していない
                            // TODO idを詳細画面に引き渡せていない
                            intent.putExtra("id", id);
                            intent.putExtra("word", word);
                            intent.putExtra("kana", kana);
                            intent.putExtra("content", content);
                            intent.putExtra("mode", "E");

                            startActivity(intent);

                            // DBの通信処理が不正の場合、エラーメッセージを表示する。
                        } else {
                            setMsg(msg, "登録に失敗しました。", Color.RED);
                        }
                    }

                    // 削除の場合
                } else if(delete.isChecked()) {
                    // TODO

                }

            }
        });
    }

    private int updateData(SQLiteDatabase db,String id, String word, String kana, String content) {

        ContentValues values = new ContentValues();
        // values.put("ITEM_ID", id);
        values.put("ITEM_NAME", word);
        values.put("ITEM_KANA", kana);
        // values.put("ITEM_INDEX", int 0); // index + 1 取得する
        values.put("CONTENT", content);
        values.put("CATEGORY", "");
        values.put("TAG1", "");
        values.put("TAG2", "");
        values.put("TAG3", "");
        values.put("TAG4", "");
        values.put("TAG5", "");
        values.put("DELETE_FLG", "");
        values.put("REGIST_DATE", "");
        values.put("UPDATE_DATE", "");

        String whereClause = "ITEM_ID = ?";
        String[] whereArgs = new String[]{id};

        return db.update("DP_DICTIONARY", values, whereClause, whereArgs);
}

    private long insertData(SQLiteDatabase db,String id, String word, String kana, String content) {

        ContentValues values = new ContentValues();
        values.put("ITEM_ID", id);
        values.put("ITEM_NAME", word);
        values.put("ITEM_KANA", kana);
        // values.put("ITEM_INDEX", int 0); // index + 1 を取得する必要あり？
        values.put("CONTENT", content);
        values.put("CATEGORY", "");
        values.put("TAG1", "");
        values.put("TAG2", "");
        values.put("TAG3", "");
        values.put("TAG4", "");
        values.put("TAG5", "");
        values.put("DELETE_FLG", "");
        values.put("REGIST_DATE", "");
        values.put("UPDATE_DATE", "");

        return db.insert("DP_DICTIONARY", null, values);
    }

    private boolean isNullError(String word, String kana, String content) {
        boolean isError = false;
        ArrayList<String> messages = new ArrayList<String>();
        TextView msg = findViewById(R.id.errorMsg);
        String errMsg = "";

        if(word.isEmpty()) {
            messages.add("単語名");
            isError = true;
        }
        if(kana.isEmpty()) {
            messages.add("よみ");
            isError = true;
        }
        if(content.isEmpty()) {
            messages.add("意味");
            isError = true;
        }

        if(isError) {
            //Intent intent = new Intent(getApplication(), EditActivity.class);
            //intent.putExtra("errorMsg", msg);
            //startActivity(intent);
            // エラーメッセージを格納
            for (int i = 0; i < messages.size(); i++) {
                errMsg = errMsg + messages.get(i) + '、';
                if (i == messages.size() - 1) {
                    errMsg = errMsg.substring(0, errMsg.length() - 1) + "が未入力です。";
                }
            }
            setMsg(msg, errMsg, Color.RED);
        }

        return isError;
    }

    private void setMsg(TextView msg, String msgText, int msgColor) {
        msg.setText(msgText);
        msg.setTextColor(msgColor);
    }
}

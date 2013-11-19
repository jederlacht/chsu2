package com.jederlacht.chsu2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;


public class chsu_exam_view extends Activity {


    // Объявление класса предмета
    public class TExam implements Comparable<Object> {
        public Date dateDateExam;
        public String stringDayOfWeek;
        public String stringTime;
        public String stringSubject;
        public String stringTeacher;
        public String stringLocation;

        public TExam() {
            dateDateExam = new Date();
            stringDayOfWeek = "";
            stringTime = "";
            stringSubject = "";
            stringTeacher = "";
            stringLocation = "";
        }

        public int compareTo(Object obj) {
            TExam tmp = (TExam) obj;
            return this.dateDateExam.compareTo(tmp.dateDateExam);
        }


    }

    String stringCurrentGroup;
    String stringCurrentTerm;
    Byte byteCurrentWeek;
    Date dateCurrentDate;

    ArrayList<TExam> listExams = new ArrayList<TExam>();


    private class MyTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog spinner;

        @Override
        protected void onPreExecute() {
            // Вначале мы покажем пользователю ProgressDialog
            // чтобы он понимал что началась загрузка
            // этот метод выполняется в UI потоке
            spinner = new ProgressDialog(chsu_exam_view.this);
            spinner.setMessage("Загрузка расписания...");
            spinner.show();


        }

        @Override
        protected Void doInBackground(Void... text) {

            // Объявляем соединение и буфер для данных
            HttpURLConnection UrlConnection = null;
            BufferedReader BufferReader = null;
            try {
                // Указываем адрес
                URL url = new URL("http://rasp.chsu.ru/_student.php");
                // Устанавливаем соединение и параметры
                UrlConnection = (HttpURLConnection) url.openConnection();
                UrlConnection.setRequestProperty("METHOD", "POST");
                UrlConnection.setDoInput(true);
                UrlConnection.setDoOutput(true);

                // Устанавливаем переменную с передаваемыми параметрами
                StringBuilder ContentParametres = new StringBuilder();
                ContentParametres.append("gr=").append(stringCurrentGroup);
                ContentParametres.append("&ss=").append(stringCurrentTerm);
                ContentParametres.append("&mode=").append("Расписание экзаменов");

                // Добавляем параметры к исходящему потоку
                OutputStream OutputStreamParam = UrlConnection.getOutputStream();
                OutputStreamParam.write(ContentParametres.toString().getBytes("CP1251"));
                OutputStreamParam.close();


                // Получаем запрошенную страницу в буфер
                BufferReader = new BufferedReader(new InputStreamReader(UrlConnection.getInputStream(),
                        "windows-1251"), 4096);

                // Создаем строку для контента
                String InputLine = "";


                while ((InputLine = BufferReader.readLine()) != null) {
                    // Проверяем, нет ли элемента строки в строке документа исходного
                    if (InputLine.contains("bgcolor=#ddddee>&nbsp;")) {

                        // Создаем объект предмета и заполняем его поля
                        TExam subject = new TExam();

                        Pattern pattern = Pattern.compile("&nbsp;(\\d+).\\d+.\\d+&nbsp;");
                        Matcher matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.dateDateExam.setDate(Integer.parseInt(matcher.group(1)));
                        }
                        pattern = Pattern.compile("&nbsp;\\d+.(\\d+).\\d+&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.dateDateExam.setMonth(Integer.parseInt(matcher.group(1)) - 1);
                        }
                        pattern = Pattern.compile("&nbsp;\\d+.\\d+.(\\d+)&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.dateDateExam.setYear(Integer.parseInt(matcher.group(1)) - 1900);
                        }

                        InputLine = BufferReader.readLine();
                        pattern = Pattern.compile("&nbsp;(\\w+)&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.stringDayOfWeek = matcher.group(1);
                        }

                        InputLine = BufferReader.readLine();
                        pattern = Pattern.compile("&nbsp;(\\S+ - \\S+)&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.stringTime = matcher.group(1);
                        }

                        InputLine = BufferReader.readLine();
                        pattern = Pattern.compile("&nbsp;(.*)&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.stringSubject = matcher.group(1);
                        }


                        InputLine = BufferReader.readLine();
                        pattern = Pattern.compile("&nbsp;(.*)&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.stringTeacher = matcher.group(1);
                        }

                        InputLine = BufferReader.readLine();
                        pattern = Pattern.compile("&nbsp;(.*)&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.stringLocation = matcher.group(1);
                        }

                        listExams.add(subject);
                    }
                }

                Collections.sort(listExams);

            } catch (Exception e) {
                Log.e("GetData", Log.getStackTraceString(e));
            } finally {
                if (BufferReader != null) {
                    try {

                        BufferReader.close();
                    } catch (IOException e) {
                        Log.e("GetSite", Log.getStackTraceString(e));
                    }
                }
                // Разрываем подключение
                if (UrlConnection != null) {
                    ((HttpURLConnection) UrlConnection).disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Загрузка закончена. Закроем ProgressDialog.
            // этот метод выполняется в UI потоке
            ShowData();
            spinner.dismiss();
        }

    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scrolltable);

        Bundle extra = getIntent().getExtras();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        stringCurrentGroup = extra.getString("CURRENT_GROUP");
        stringCurrentTerm = extra.getString("CURRENT_TERM");


    }


    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);  // Add this method.
        new MyTask().execute();
        //  ShowData();
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);  // Add this method.
    }

    // Показываем предмет
    public void ShowExam(TExam subject) {

        // Находим слой таблицы для работы с ним
        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayoutStudent);

        // Выводим дату и время
        TableRow tableRowTimeSubject = new TableRow(this);
        tableRowTimeSubject.setGravity(Gravity.CENTER);
        tableRowTimeSubject.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TextView textViewTimeSubject = new TextView(this);
        textViewTimeSubject.setGravity(Gravity.CENTER);
        textViewTimeSubject.setTextColor(Color.BLUE);
        StringBuilder stringTimeSubject = new StringBuilder(String.valueOf(subject.dateDateExam.getDate()));
        stringTimeSubject.append(".");
        stringTimeSubject.append(String.valueOf(subject.dateDateExam.getMonth() + 1));
        stringTimeSubject.append(".");
        stringTimeSubject.append(String.valueOf(subject.dateDateExam.getYear() + 1900));
        stringTimeSubject.append(", ");
        stringTimeSubject.append(subject.stringTime);
        textViewTimeSubject.setText(stringTimeSubject.toString());
        tableRowTimeSubject.addView(textViewTimeSubject, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tableLayout.addView(tableRowTimeSubject);

        // Выводим день недели
        TableRow tableRowDayOfWeek = new TableRow(this);
        tableRowDayOfWeek.setGravity(Gravity.CENTER);
        tableRowDayOfWeek.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TextView textViewDayOfWeek = new TextView(this);
        textViewDayOfWeek.setGravity(Gravity.CENTER);
        textViewDayOfWeek.setTextColor(Color.BLUE);
        textViewDayOfWeek.setText(subject.stringDayOfWeek);
        tableRowDayOfWeek.addView(textViewDayOfWeek);
        tableLayout.addView(tableRowDayOfWeek);

        // Выводим название предмета
        TableRow tableRowSubject = new TableRow(this);
        tableRowSubject.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TextView textViewSubject = new TextView(this);
        textViewSubject.setText(subject.stringSubject);
        tableRowSubject.addView(textViewSubject, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tableLayout.addView(tableRowSubject);


        // Выводим место занятия и преподавателя
        TableRow tableRowLocationTeacher = new TableRow(this);
        tableRowLocationTeacher.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TextView textViewLocationTeacher = new TextView(this);
        StringBuilder stringLocationTeacher = new StringBuilder(subject.stringLocation);
        stringLocationTeacher.append(", ");
        stringLocationTeacher.append(subject.stringTeacher);
        textViewLocationTeacher.setText(stringLocationTeacher.toString());
        tableRowLocationTeacher.addView(textViewLocationTeacher, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tableLayout.addView(tableRowLocationTeacher);

        // Выводим разделитель
        TableRow tableRowEmpty = new TableRow(this);
        tableRowEmpty.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        tableRowEmpty.setGravity(Gravity.CENTER);
        TextView textViewEmpty = new TextView(this);
        textViewEmpty.setTextColor(Color.BLUE);
        textViewEmpty.setGravity(Gravity.CENTER);
        textViewEmpty.setText("-----------");
        tableRowEmpty.addView(textViewEmpty);
        tableLayout.addView(tableRowEmpty);
    }


    // Показываем данные
    public void ShowData() {
        try {
            setContentView(R.layout.scrolltable);

            // Выводим в цикле все элементы предметов, создавая для них элементы интерфейса
            for (TExam subject : listExams) {

                ShowExam(subject);

            }

        } catch (Exception e) {
            Log.e("ShowData", Log.getStackTraceString(e));
        }
    }


}

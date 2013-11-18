package com.jederlacht.chsu2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;





public class chsu_main extends Activity {

    // Объявляем переменные
    WebView mWebView;
    Button BtnOpenTable;
    Button BtnOpenTableExams;

    SharedPreferences.Editor SettingsEditor;
    ArrayList<String> arrayStringCurrentGroups = new ArrayList<String>();
    String stringCurrentGroup;
    String stringCurrentTerm;
    Byte byteCurrentWeek;
    String stringCurrentDate;
    TextView textCurrentGroup;
    TextView textCurrentTerm;
    TextView textCurrentWeek;
    TextView textCurrentDate;
    //private ProgressDialog spinner;
    AlertDialog.Builder ad;
    Context context;
    Document doc;

    private class MyTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog spinner;
        String day = null;
        String week = null;
        @Override
        protected void onPreExecute() {
            // Вначале мы покажем пользователю ProgressDialog
            // чтобы он понимал что началась загрузка
            // этот метод выполняется в UI потоке
            spinner = new ProgressDialog(chsu_main.this);
            spinner.setMessage("Идет загрузка...");
            spinner.show();


        }

        @Override
        protected Void doInBackground(Void... text) {

            //GetDataStart();
            // тут мы делаем основную работу по загрузке данных
            // этот метод выполяется в другом потоке
            // Объявляем соединение и буфер для данных
            HttpURLConnection UrlConnection = null;
            BufferedReader BufferReader = null;


            try {
                URL url = new URL("http://ya.ru");
               doc=Jsoup.parse (url,1000);
                //Element e = doc.getElementsByAttribute("font[size$=\"5\"").first();
                //week=e.text();
                week="7";
/*
                // Указываем адрес
                URL url = new URL("http://rasp.chsu.ru/_student.php");
                // Устанавливаем соединение и параметры
                UrlConnection = (HttpURLConnection) url.openConnection();
                UrlConnection.setRequestProperty("METHOD", "POST");
                UrlConnection.setDoInput(true);
                UrlConnection.setDoOutput(true);


                // Получаем запрошенную страницу в буфер
                BufferReader = new BufferedReader(new InputStreamReader(UrlConnection.getInputStream(),
                        "windows-1251"), 4096);

                // Создаем строку для контента
                String InputLine = "";
                // Заполняем переменную контентом
                while ((InputLine = BufferReader.readLine()) != null) {

/*                    // Проверяем, нет ли элемента строки в строке документа исходного
                    if (InputLine.contains("учебная неделя")) {

                        Pattern pattern = Pattern.compile(">(\\d+)<");
                        Matcher matcher = pattern.matcher(InputLine);

                        if(matcher.find())
                        {
                            week = matcher.group(1);
                            //StringBuilder string = new StringBuilder(week);
                            //string.append(" неделя");


                        }
                    }

                    // Проверяем, нет ли элемента строки в строке документа исходного
                    if (InputLine.contains("Сегодня")) {

                        Pattern pattern = Pattern.compile("(\\d{2}.\\d{2}.\\d{4})");
                        Matcher matcher = pattern.matcher(InputLine);

                        if(matcher.find())
                        {
                            day = matcher.group(1);

                        }
                    }

                    // Составляем список групп
                    if (InputLine.contains("option")) {
                        Pattern pattern = Pattern.compile("value=\"(\\w+\\(?\\w?\\)?\\w?-\\w+)\"");
                        Matcher matcher = pattern.matcher(InputLine);
                        Pattern pattern1 = Pattern.compile("value=\"(\\d{6}.\\d{2}-\\d{2}-\\w+)\"");
                        Matcher matcher1 = pattern1.matcher(InputLine);
                        if(matcher.find())
                        {
                            String group = null;
                            group = matcher.group(1).toString();
                            arrayStringCurrentGroups.add(group);
                        }
                        else if(matcher1.find())
                        {
                            String group = null;
                            group = matcher1.group(1).toString();
                            arrayStringCurrentGroups.add(group);
                        }

                    }

                  }*/

            } catch (Exception e) {
                spinner.setMessage(Log.getStackTraceString(e));
                Log.e("GetDataStart", Log.getStackTraceString(e));
            } finally {
                if (BufferReader != null) {
                    try {
                        BufferReader.close();
                    } catch (IOException e) {
                        spinner.setMessage(Log.getStackTraceString(e));
                        Log.e("GetSite", Log.getStackTraceString(e));
                    }
                }
                // Разрываем подключение
                if (UrlConnection != null) {
                    ((HttpURLConnection)UrlConnection).disconnect();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textCurrentDate.setText(day);
            stringCurrentDate = day;

            textCurrentWeek.setText(week + " неделя");
            byteCurrentWeek = Byte.parseByte(week);
            // Загрузка закончена. Закроем ProgressDialog.
            // этот метод выполняется в UI потоке
            spinner.dismiss();
        }

    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Открываем лэйаут по умолчанию
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        // Инициализируем переменную изменения настроек
        SettingsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        // Находим кнопку
        BtnOpenTable = (Button)findViewById(R.id.buttonOpenTable);
        BtnOpenTable.setText(R.string.open_table_command);
        BtnOpenTableExams = (Button)findViewById(R.id.buttonOpenTableExams);
        BtnOpenTableExams.setText(R.string.open_table_exam_command);

        // Находим поля, чтобы потом заполнять их
        textCurrentGroup = (TextView)findViewById(R.id.TextViewGroup);
        textCurrentTerm = (TextView)findViewById(R.id.textViewTerm);
        textCurrentDate = (TextView)findViewById(R.id.textViewDate);
        textCurrentWeek = (TextView)findViewById(R.id.textViewWeek);

        context = chsu_main.this;
        //GetDataStart();// Задержка!
        if (isOnline()){
            new MyTask().execute();
        }
        else {
            CharSequence text = "Нет соединения с сетью!!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();


        }


        // Нажатие на кнопку "Основное расписание"
        BtnOpenTable.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(chsu_main.this, chsu_view.class);
                intent.putExtra("CURRENT_GROUP", stringCurrentGroup);
                intent.putExtra("CURRENT_TERM", stringCurrentTerm);
                intent.putExtra("CURRENT_WEEK", byteCurrentWeek);
                startActivity(intent);

            }
        });

        // Нажатие на кнопку "Расписание экзаменов"
        BtnOpenTableExams.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(chsu_main.this, chsu_exam_view.class);
                intent.putExtra("CURRENT_GROUP", stringCurrentGroup);
                intent.putExtra("CURRENT_TERM", stringCurrentTerm);
                intent.putExtra("CURRENT_WEEK", byteCurrentWeek);
                startActivity(intent);

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();


        // Читаем настройки запомненные
        SharedPreferences SavedSettings = PreferenceManager.getDefaultSharedPreferences(this);
        stringCurrentGroup = SavedSettings.getString("sCurrentGroup", "Выберите в настройках");
        stringCurrentTerm = SavedSettings.getString("sCurrentTerm", "группу и ");

        // Устанавливаем значения в текстовые поля

        textCurrentGroup.setText(stringCurrentGroup);
        StringBuilder string = new StringBuilder(stringCurrentTerm);
        string.append(" семестр");
        textCurrentTerm.setText(string);
    }


    // Создаем меню
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Выбор пункта меню
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item1) {
            // По нажатию кнопки открываем настройки
        	/*Intent intent = new Intent(chsu_main.this, chsu_settings.class);
        	intent.putStringArrayListExtra("CURRENT_GROUPS", arrayStringCurrentGroups);
        	startActivity(intent);
        	finish();*/
            Intent settingsActivity = new Intent(this, chsu_preferences.class);
            settingsActivity.putStringArrayListExtra("CURRENT_GROUPS", arrayStringCurrentGroups);
            startActivity(settingsActivity);
            //finish();
        }
        return super.onOptionsItemSelected(item);
    }





    // Получаем общие данные
    public  void GetDataStart() {
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


            // Получаем запрошенную страницу в буфер
            BufferReader = new BufferedReader(new InputStreamReader(UrlConnection.getInputStream(),
                    "windows-1251"), 4096);

            // Создаем строку для контента
            String InputLine = "";
            // Заполняем переменную контентом
            while ((InputLine = BufferReader.readLine()) != null) {

                // Проверяем, нет ли элемента строки в строке документа исходного
                if (InputLine.contains("учебная неделя")) {

                    Pattern pattern = Pattern.compile(">(\\d+)<");
                    Matcher matcher = pattern.matcher(InputLine);
                    String week = null;
                    if(matcher.find())
                    {
                        week = matcher.group(1);
                        StringBuilder string = new StringBuilder(week);
                        string.append(" неделя");
                        textCurrentWeek.setText(string);
                        byteCurrentWeek = Byte.parseByte(week);

                    }
                }

                // Проверяем, нет ли элемента строки в строке документа исходного
                if (InputLine.contains("Сегодня")) {

                    Pattern pattern = Pattern.compile("(\\d{2}.\\d{2}.\\d{4})");
                    Matcher matcher = pattern.matcher(InputLine);
                    String day = null;
                    if(matcher.find())
                    {
                        day = matcher.group(1);
                        textCurrentDate.setText(day);
                        stringCurrentDate = day;
                    }
                }

                // Составляем список групп
                if (InputLine.contains("option")) {
                    Pattern pattern = Pattern.compile("value=\"(\\w+-\\w+)\"");
                    Matcher matcher = pattern.matcher(InputLine);
                    if(matcher.find())
                    {
                        String group = null;
                        group = matcher.group(1).toString();
                        arrayStringCurrentGroups.add(group);
                    }

                }

            }

        } catch (Exception e) {
            Log.e("GetDataStart", Log.getStackTraceString(e));
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
                ((HttpURLConnection)UrlConnection).disconnect();
            }
        }
    }







}
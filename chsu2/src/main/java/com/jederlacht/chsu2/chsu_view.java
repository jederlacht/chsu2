package com.jederlacht.chsu2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.SpinnerAdapter;
import android.widget.ArrayAdapter;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class chsu_view extends Activity {//implements OnTouchListener

    static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
    static final long MILLISECONDS_PER_WEEK = 7 * MILLISECONDS_PER_DAY;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    // Перечисление типов показа расписания
    enum EnumType {
        all, week, date
    }

    // Массив дней недели
    String stringsDaysOfWeek[] = {"воскресенье", "понедельник", "вторник", "среда", "четверг", "пятница", "суббота"};

    // Перечисление типов недель
    enum EnumWeekType {
        even, noteven, every
    }

    // Объявление класса предмета
    public class TSubject {
        public String stringDayOfWeek;
        public String stringTime;
        public Byte byteStartWeek;
        public Byte byteEndWeek;
        public EnumWeekType weekType;
        public String stringSubject;
        public String stringTeacher;
        public String stringLocation;

        public TSubject() {
            weekType = EnumWeekType.every;
            stringDayOfWeek = "";
            stringTime = "";
            byteStartWeek = 0;
            byteEndWeek = 0;
            stringSubject = "";
            stringTeacher = "";
            stringLocation = "";
        }
    }

    String stringCurrentGroup;
    String stringCurrentTerm;
    Byte byteCurrentWeek;
    Date dateCurrentDate;
    EnumType currentTypeView;
    ArrayList<TSubject> listSubjects = new ArrayList<>();
    OnNavigationListener mOnNavigationListener;
    ActionBar actionBar;

    private class MyTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog spinner;

        @Override
        protected void onPreExecute() {
            // Вначале мы покажем пользователю ProgressDialog
            // чтобы он понимал что началась загрузка
            // этот метод выполняется в UI потоке
            spinner = new ProgressDialog(chsu_view.this);
            spinner.setMessage("Загрузка расписания...");
            spinner.show();


        }

        @Override
        protected Void doInBackground(Void... text) {

            // Объявляем соединение и буфер для данных
            HttpURLConnection UrlConnection = null;
            BufferedReader BufferReader = null;
            Document doc = null;
            try {
                try {
                    doc = Jsoup.connect("https://rasp.chsu.ru/_student.php")
                            .data("gr", stringCurrentGroup)
                            .data("ss", stringCurrentTerm)
                            .data("mode", "Расписание занятий")
                            .postDataCharset("CP1251")
                            .post();
                } catch (IOException e) {

                    e.printStackTrace();
                }

                Elements subjects = doc.select("body > div:nth-child(7) > table > tbody > tr");

                URL url = new URL("http://rasp.chsu.ru/_student.php");
                // Устанавливаем соединение и параметры
                UrlConnection = (HttpURLConnection) url.openConnection();
                UrlConnection.setRequestProperty("METHOD", "POST");
                UrlConnection.setDoInput(true);
                UrlConnection.setDoOutput(true);

                // Устанавливаем переменную с передаваемыми параметрами
                String ContentParametres = "gr=" + stringCurrentGroup +
                        "&ss=" + stringCurrentTerm +
                        "&mode=" + "Расписание занятий";

                // Добавляем параметры к исходящему потоку
                OutputStream OutputStreamParam = UrlConnection.getOutputStream();
                OutputStreamParam.write(ContentParametres.getBytes("CP1251"));
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
                        TSubject subject = new TSubject();

                        Pattern pattern = Pattern.compile("&nbsp;(\\w+)&nbsp;");
                        Matcher matcher = pattern.matcher(InputLine);
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
                        pattern = Pattern.compile("&nbsp;([\\S\\s]*)&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.stringSubject = matcher.group(1);
                        }

                        InputLine = BufferReader.readLine();
                        pattern = Pattern.compile("&nbsp;\\w+ (\\d+) \\w+ \\d+&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.byteStartWeek = Byte.parseByte(matcher.group(1));
                        }
                        pattern = Pattern.compile("&nbsp;\\w+ \\d+ \\w+ (\\d+)&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.byteEndWeek = Byte.parseByte(matcher.group(1));
                        }

                        InputLine = BufferReader.readLine();
                        pattern = Pattern.compile("&nbsp;(\\w+)&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            String typeWeek = matcher.group(1);
                            switch (typeWeek) {
                                case "чет":
                                    subject.weekType = EnumWeekType.even;
                                    break;
                                case "нечет":
                                    subject.weekType = EnumWeekType.noteven;
                                    break;
                                default:
                                    subject.weekType = EnumWeekType.every;
                                    break;
                            }

                        }

                        InputLine = BufferReader.readLine();
                        pattern = Pattern.compile("&nbsp;([\\S\\s]*)&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.stringTeacher = matcher.group(1);
                        }

                        InputLine = BufferReader.readLine();
                        pattern = Pattern.compile("&nbsp;([\\S\\s]*)&nbsp;");
                        matcher = pattern.matcher(InputLine);
                        if (matcher.find()) {
                            subject.stringLocation = matcher.group(1);
                        }

                        listSubjects.add(subject);
                    }
                }

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
                    UrlConnection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Загрузка закончена. Закроем ProgressDialog.
            // этот метод выполняется в UI потоке
            spinner.dismiss();
            ShowData(currentTypeView);
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "chsu_view Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.jederlacht.chsu2/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "chsu_view Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.jederlacht.chsu2/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.scrolltable);

        // Читаем настройки запомненные
        SharedPreferences SavedSettings = PreferenceManager.getDefaultSharedPreferences(this);
        //final SharedPreferences.Editor SettingsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        ChangeDate(SavedSettings.getLong("longCurrentDate", new Date().getTime()));
        currentTypeView = EnumType.values()[SavedSettings.getInt("TypeView", 0)];

        Bundle extra = getIntent().getExtras();

        stringCurrentGroup = extra.getString("CURRENT_GROUP");
        stringCurrentTerm = extra.getString("CURRENT_TERM");
        byteCurrentWeek = extra.getByte("CURRENT_WEEK");
        actionBar = getActionBar();
       /* final ActionBar actionBar =  getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.Modes,
                android.R.layout.simple_spinner_dropdown_item);

        mOnNavigationListener = new OnNavigationListener() {
            // Get the same strings provided for the drop-down's ArrayAdapter
            // String[] strings = getResources().getStringArray(R.array.Modes);
            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {


                if (position == 0) {
                    currentTypeView = EnumType.all;
                    //Clear();
                    ShowData(currentTypeView);
                } else if (position == 1) {
                    currentTypeView = EnumType.week;
                    //Clear();
                    ShowData(currentTypeView);
                } else if (position == 2) {
                    currentTypeView = EnumType.date;
                    //Clear();
                    ShowData(currentTypeView);
                } else if (position == 3) {
                    currentTypeView = EnumType.date;
                    //Clear();
                    ChangeDate(new Date().getTime());
                    ShowData(currentTypeView);
                }
                // Занесение значения режима работы в настройки
                //SettingsEditor.putInt("TypeView", currentTypeView.ordinal());
                //SettingsEditor.commit();
                return true;
            }

            ;

        };

        actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);*/
        //if (listSubjects.size()==0) 
        new MyTask().execute();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    // Создаем меню
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem;
        // Кнопка показа всего расписания
        menuItem = menu.add(Menu.NONE, 0, Menu.NONE, "Все");
        menuItem.setIcon(android.R.drawable.ic_menu_today);
        // Кнопка показа расписания на неделю
        menuItem = menu.add(Menu.NONE, 1, Menu.NONE, "Неделя");
        menuItem.setIcon(android.R.drawable.ic_menu_week);
        // Кнопка показа расписания на день
        menuItem = menu.add(Menu.NONE, 2, Menu.NONE, "День");
        menuItem.setIcon(android.R.drawable.ic_menu_day);
        // Кнопка показа расписания на день
        menuItem = menu.add(Menu.NONE, 3, Menu.NONE, "Сегодня");
        menuItem.setIcon(android.R.drawable.ic_menu_my_calendar);


        // getMenuInflater().inflate(R.menu.modes, menu);


        return super.onCreateOptionsMenu(menu);
    }


    // Выбор пункта меню
    public boolean onOptionsItemSelected(MenuItem item) {
        // Инициализируем переменную изменения настроек
        SharedPreferences.Editor SettingsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (item.getItemId() == 0) {
            currentTypeView = EnumType.all;

            actionBar.setTitle("Расписание");


        } else if (item.getItemId() == 1) {
            currentTypeView = EnumType.week;

            actionBar.setTitle("Расписание на неделю");

        } else if (item.getItemId() == 2) {
            currentTypeView = EnumType.date;

            actionBar.setTitle("Расписание на день");

        } else if (item.getItemId() == 3) {
            currentTypeView = EnumType.date;

            actionBar.setTitle("Расписание на сегодня");
            ChangeDate(new Date().getTime());

        }

        // Занесение значения режима работы в настройки
        SettingsEditor.putInt("TypeView", currentTypeView.ordinal());
        SettingsEditor.commit();
        ShowData(currentTypeView);
        return super.onOptionsItemSelected(item);
    }


    // Показываем предмет
    public void ShowSubject(TSubject subject) {


        // Находим слой таблицы для работы с ним
        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayoutStudent);
//        tableLayout.setOnTouchListener(this) ;        
        // Выводим название предмета
        TableRow tableRowSubject = new TableRow(this);
        tableRowSubject.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TextView textViewSubject = new TextView(this);
        textViewSubject.setText(subject.stringSubject);
        tableRowSubject.addView(textViewSubject, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tableLayout.addView(tableRowSubject);


        // Выводим недели, их тип и время
        TableRow tableRowTimeSubject = new TableRow(this);
        tableRowTimeSubject.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TextView textViewTimeSubject = new TextView(this);
        StringBuilder stringTimeSubject = new StringBuilder("c ");
        stringTimeSubject.append(subject.byteStartWeek.toString());
        stringTimeSubject.append(" по ");
        stringTimeSubject.append(subject.byteEndWeek.toString());

        switch (subject.weekType) {
            case even:
                stringTimeSubject.append(" по четным, ");
                break;
            case noteven:
                stringTimeSubject.append(" по нечетным, ");
                break;
            case every:
                stringTimeSubject.append(" еженедельно, ");
                break;
        }

        stringTimeSubject.append(subject.stringTime);

        textViewTimeSubject.setText(stringTimeSubject.toString());
        tableRowTimeSubject.addView(textViewTimeSubject, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tableLayout.addView(tableRowTimeSubject);


        // Выводим место занятия и преподавателя
        TableRow tableRowLocationTeacher = new TableRow(this);
        tableRowLocationTeacher.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TextView textViewLocationTeacher = new TextView(this);
        String stringLocationTeacher = subject.stringLocation + ", " +
                subject.stringTeacher;
        textViewLocationTeacher.setText(stringLocationTeacher);
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
    public void ShowData(EnumType type) {

        try {
            Clear();
            setContentView(R.layout.scrolltable);

            // Находим слой таблицы для работы с ним
            TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayoutStudent);
//            tableLayout.setOnTouchListener(this) ;

            ArrayList<String> listDaysOfWeek = new ArrayList<>();

            // Выводим номер недели, дату, элементы управления
            if (type.equals(EnumType.week)) {
                TableRow tableRowCurrentWeek = new TableRow(this);
                tableRowCurrentWeek.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                tableRowCurrentWeek.setGravity(Gravity.CENTER);

                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setGravity(Gravity.CENTER);


                Button buttonPrev = new Button(this);
                buttonPrev.setText("<<");
                buttonPrev.setWidth(60);
                buttonPrev.setHeight(30);
                buttonPrev.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        ChangeWeek((byte) (byteCurrentWeek - 1));
                        Clear();
                        ShowData(EnumType.week);
                    }
                });
                linearLayout.addView(buttonPrev, 0);

                TextView textViewCurrentWeek = new TextView(this);
                textViewCurrentWeek.setTextColor(Color.RED);
                textViewCurrentWeek.setGravity(Gravity.CENTER);
                textViewCurrentWeek.setPadding(5, 5, 5, 5);
                textViewCurrentWeek.setText(byteCurrentWeek.toString() + " неделя");
                linearLayout.addView(textViewCurrentWeek, 1);

                Button buttonNext = new Button(this);
                buttonNext.setText(">>");
                buttonNext.setWidth(60);
                buttonNext.setHeight(50);
                buttonNext.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        ChangeWeek((byte) (byteCurrentWeek + 1));
                        Clear();
                        ShowData(EnumType.week);
                    }
                });
                linearLayout.addView(buttonNext, 2);

                tableRowCurrentWeek.addView(linearLayout);
                tableLayout.addView(tableRowCurrentWeek);
            }
            // Выводим дату, если расписание на день
            else if (type.equals(EnumType.date)) {
                TableRow tableRowCurrentDate = new TableRow(this);
                tableRowCurrentDate.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                tableRowCurrentDate.setGravity(Gravity.CENTER);

                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setGravity(Gravity.CENTER);


                Button buttonPrev = new Button(this);
                buttonPrev.setText("<<");
                buttonPrev.setWidth(60);
                buttonPrev.setHeight(30);
                buttonPrev.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        ChangeDate(dateCurrentDate.getTime() - MILLISECONDS_PER_DAY);
                        Clear();
                        ShowData(EnumType.date);
                    }
                });
                linearLayout.addView(buttonPrev, 0);

                TextView textViewCurrentDate = new TextView(this);
                textViewCurrentDate.setTextColor(Color.RED);
                textViewCurrentDate.setGravity(Gravity.CENTER);
                textViewCurrentDate.setPadding(5, 5, 5, 5);
                String string = String.valueOf(dateCurrentDate.getDate()) + "." +
                        String.valueOf(dateCurrentDate.getMonth() + 1) +
                        "." +
                        String.valueOf(dateCurrentDate.getYear() + 1900) +
                        ", " +
                        String.valueOf(byteCurrentWeek) +
                        " нед.";
                textViewCurrentDate.setText(string);
                linearLayout.addView(textViewCurrentDate, 1);

                Button buttonNext = new Button(this);
                buttonNext.setText(">>");
                buttonNext.setWidth(60);
                buttonNext.setHeight(50);
                buttonNext.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        ChangeDate(dateCurrentDate.getTime() + MILLISECONDS_PER_DAY);
                        Clear();
                        ShowData(EnumType.date);
                    }
                });
                linearLayout.addView(buttonNext, 2);

                tableRowCurrentDate.addView(linearLayout);
                tableLayout.addView(tableRowCurrentDate);
            }

            // Выводим в цикле все элементы предметов, создавая для них элементы интерфейса
            for (TSubject subject : listSubjects) {
                boolean isAll = type.equals(EnumType.all);
                boolean isWeek = type.equals(EnumType.week);
                boolean isCorrectWeekRange = (byteCurrentWeek >= subject.byteStartWeek)
                        && (byteCurrentWeek <= subject.byteEndWeek);
                boolean isCorrectWeek = ((subject.weekType.equals(EnumWeekType.every)) || ((subject.weekType.equals(EnumWeekType.even)) && ((byteCurrentWeek % 2) == 0))
                        || ((subject.weekType.equals(EnumWeekType.noteven)) && ((byteCurrentWeek % 2) != 0)));
                boolean isDate = type.equals(EnumType.date);
                boolean isCorrectDay = subject.stringDayOfWeek.equals(stringsDaysOfWeek[dateCurrentDate.getDay()]);

                if (isAll || (isWeek && isCorrectWeekRange && isCorrectWeek) || (isDate && isCorrectWeekRange && isCorrectWeek && isCorrectDay)) {

                    // Выводим день недели
                    if (!listDaysOfWeek.contains(subject.stringDayOfWeek)) {
                        TableRow tableRowDayOfWeek = new TableRow(this);
                        tableRowDayOfWeek.setGravity(Gravity.CENTER);
                        tableRowDayOfWeek.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                        TextView textViewDayOfWeek = new TextView(this);
                        textViewDayOfWeek.setGravity(Gravity.CENTER);
                        textViewDayOfWeek.setTextColor(Color.BLUE);
                        textViewDayOfWeek.setText(subject.stringDayOfWeek);
                        tableRowDayOfWeek.addView(textViewDayOfWeek);
                        tableLayout.addView(tableRowDayOfWeek);
                        listDaysOfWeek.add(subject.stringDayOfWeek);
                    }

                    ShowSubject(subject);

                }
            }


        } catch (Exception e) {
            Log.e("ShowData", Log.getStackTraceString(e));
        }
    }


    // Удаляем существующие элементы управления
    public void Clear() {
        // Находим слой таблицы для работы с ним
        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayoutStudent);
        tableLayout.removeAllViews();
    }


    // Получение номера учебной недели
    public byte GetWeek(Date date) {
        // Задаем точку отсчета: если посленовогодняя дата, то устанавливаем 1 сентября предыдущего года
        Date fromday = new Date(date.getMonth() < 8 ? date.getYear() - 1 : date.getYear(), 8, 1);

        long longBeetweenDays = date.getTime() - fromday.getTime();
        // Определяем длину первой недели, чтобы вычесть ее из общей суммы для целого расчета
        long longTimeOfFirstWeek = ((7 - (fromday.getDay() == 0 ? 6 : fromday.getDay() - 1)) * MILLISECONDS_PER_DAY);
        // Если нужная дата меньше длины первой недели, то это первая неделя, иначе же высчитываем
        //byte byteWeekNum = (byte) (longBeetweenDays < longTimeOfFirstWeek ? 1 : ((longBeetweenDays - longTimeOfFirstWeek) / (MILLISECONDS_PER_WEEK) + 2));

        // Попытка реализовать отсчет недель с 0
        byte byteWeekNum = (byte) (longBeetweenDays < longTimeOfFirstWeek ? 0 : ((longBeetweenDays - longTimeOfFirstWeek) / (MILLISECONDS_PER_WEEK) + 1));

        return byteWeekNum;
    }

    // Устанавливаем требуемую дату
    public void ChangeDate(long time) {
        // Инициализируем переменную изменения настроек
        SharedPreferences.Editor SettingsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        // Занесение значения даты в настройки приложения
        SettingsEditor.putLong("longCurrentDate", time);
        SettingsEditor.commit();

        dateCurrentDate = new Date(time);
        byteCurrentWeek = GetWeek(dateCurrentDate);
    }

    // Устанавливаем требуемый номер недели
    public void ChangeWeek(byte week) {
        // Задаем точку отсчета: если посленовогодняя дата, то устанавливаем 1 сентября предыдущего года
        Date fromday = new Date(dateCurrentDate.getMonth() < 8 ? dateCurrentDate.getYear() - 1 : dateCurrentDate.getYear(), 8, 1);
        // Определяем длину первой недели, чтобы вычесть ее из общей суммы для целого расчета
        long longTimeOfFirstWeek = ((7 - (fromday.getDay() == 0 ? 6 : fromday.getDay() - 1)) * MILLISECONDS_PER_DAY + MILLISECONDS_PER_DAY / 4);
        //long longNewDate = ( week == 1 ? fromday.getTime() : fromday.getTime() + longTimeOfFirstWeek + (week - 2) * MILLISECONDS_PER_WEEK );
        // Попытка реализовать отсчет недель с 0
        long longNewDate = (week == 0 ? fromday.getTime() : fromday.getTime() + longTimeOfFirstWeek + (week - 1) * MILLISECONDS_PER_WEEK);
        ChangeDate(longNewDate);
    }


}

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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.SpinnerAdapter;
import android.widget.ArrayAdapter;


public class chsu_view extends Activity {//implements OnTouchListener

    static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
    static final long MILLISECONDS_PER_WEEK = 7 * MILLISECONDS_PER_DAY;

    // ������������ ����� ������ ����������
    enum EnumType {
        all, week, date
    }

    ;

    // ������ ���� ������
    String stringsDaysOfWeek[] = {"�����������", "�����������", "�������", "�����", "�������", "�������", "�������"};

    // ������������ ����� ������
    enum EnumWeekType {
        even, noteven, every
    }

    ;

    // ���������� ������ ��������
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
    ArrayList<TSubject> listSubjects = new ArrayList<TSubject>();
    OnNavigationListener mOnNavigationListener;

    private class MyTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog spinner;

        @Override
        protected void onPreExecute() {
            // ������� �� ������� ������������ ProgressDialog
            // ����� �� ������� ��� �������� ��������
            // ���� ����� ����������� � UI ������
            spinner = new ProgressDialog(chsu_view.this);
            spinner.setMessage("�������� ����������...");
            spinner.show();


        }

        @Override
        protected Void doInBackground(Void... text) {

            // ��������� ���������� � ����� ��� ������
            HttpURLConnection UrlConnection = null;
            BufferedReader BufferReader = null;
            try {
                // ��������� �����
                URL url = new URL("http://rasp.chsu.ru/_student.php");
                // ������������� ���������� � ���������
                UrlConnection = (HttpURLConnection) url.openConnection();
                UrlConnection.setRequestProperty("METHOD", "POST");
                UrlConnection.setDoInput(true);
                UrlConnection.setDoOutput(true);

                // ������������� ���������� � ������������� �����������
                StringBuilder ContentParametres = new StringBuilder();
                ContentParametres.append("gr=").append(stringCurrentGroup);
                ContentParametres.append("&ss=").append(stringCurrentTerm);
                ContentParametres.append("&mode=").append("���������� �������");

                // ��������� ��������� � ���������� ������
                OutputStream OutputStreamParam = UrlConnection.getOutputStream();
                OutputStreamParam.write(ContentParametres.toString().getBytes("CP1251"));
                OutputStreamParam.close();


                // �������� ����������� �������� � �����
                BufferReader = new BufferedReader(new InputStreamReader(UrlConnection.getInputStream(),
                        "windows-1251"), 4096);

                // ������� ������ ��� ��������
                String InputLine = "";


                while ((InputLine = BufferReader.readLine()) != null) {
                    // ���������, ��� �� �������� ������ � ������ ��������� ���������
                    if (InputLine.contains("bgcolor=#ddddee>&nbsp;")) {

                        // ������� ������ �������� � ��������� ��� ����
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
                            if (typeWeek.equals("���")) {
                                subject.weekType = EnumWeekType.even;
                            } else if (typeWeek.equals("�����")) {
                                subject.weekType = EnumWeekType.noteven;
                            } else
                                subject.weekType = EnumWeekType.every;

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
                // ��������� �����������
                if (UrlConnection != null) {
                    ((HttpURLConnection) UrlConnection).disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // �������� ���������. ������� ProgressDialog.
            // ���� ����� ����������� � UI ������
            spinner.dismiss();
            ShowData(currentTypeView);
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        //Clear();

    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.scrolltable);

        // ������ ��������� �����������
        SharedPreferences SavedSettings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor SettingsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        ChangeDate(SavedSettings.getLong("longCurrentDate", new Date().getTime()));
        currentTypeView = EnumType.values()[SavedSettings.getInt("TypeView", 0)];

        Bundle extra = getIntent().getExtras();

        stringCurrentGroup = extra.getString("CURRENT_GROUP");
        stringCurrentTerm = extra.getString("CURRENT_TERM");
        byteCurrentWeek = extra.getByte("CURRENT_WEEK");
        final ActionBar actionBar =  getActionBar();
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
                    Clear();
                    ShowData(currentTypeView);
                } else if (position == 1) {
                    currentTypeView = EnumType.week;
                    Clear();
                    ShowData(currentTypeView);
                } else if (position == 2) {
                    currentTypeView = EnumType.date;
                    Clear();
                    ShowData(currentTypeView);
                } else if (position == 3) {
                    currentTypeView = EnumType.date;
                    Clear();
                    ChangeDate(new Date().getTime());
                    ShowData(currentTypeView);
                }
                // ��������� �������� ������ ������ � ���������
                //SettingsEditor.putInt("TypeView", currentTypeView.ordinal());
                //SettingsEditor.commit();
                return true;
            }

            ;

        };

        actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
        //if (listSubjects.size()==0) 
        new MyTask().execute();


    }



    // ������� ����
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem;
        // ������ ������ ����� ����������
        menuItem = menu.add(Menu.NONE, 0, Menu.NONE, "���");
        menuItem.setIcon(android.R.drawable.ic_menu_today);
        // ������ ������ ���������� �� ������
        menuItem = menu.add(Menu.NONE, 1, Menu.NONE, "������");
        menuItem.setIcon(android.R.drawable.ic_menu_week);
        // ������ ������ ���������� �� ����
        menuItem = menu.add(Menu.NONE, 2, Menu.NONE, "����");
        menuItem.setIcon(android.R.drawable.ic_menu_day);
        // ������ ������ ���������� �� ����
        menuItem = menu.add(Menu.NONE, 3, Menu.NONE, "�������");
        menuItem.setIcon(android.R.drawable.ic_menu_my_calendar);

        return super.onCreateOptionsMenu(menu);
    }


    // ����� ������ ����
    public boolean onOptionsItemSelected(MenuItem item) {
        // �������������� ���������� ��������� ��������
        SharedPreferences.Editor SettingsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (item.getItemId() == 0) {
            currentTypeView = EnumType.all;
            Clear();
            ShowData(currentTypeView);
        } else if (item.getItemId() == 1) {
            currentTypeView = EnumType.week;
            Clear();
            ShowData(currentTypeView);
        } else if (item.getItemId() == 2) {
            currentTypeView = EnumType.date;
            Clear();
            ShowData(currentTypeView);
        } else if (item.getItemId() == 3) {
            currentTypeView = EnumType.date;
            Clear();
            ChangeDate(new Date().getTime());
            ShowData(currentTypeView);
        }
        // ��������� �������� ������ ������ � ���������
        SettingsEditor.putInt("TypeView", currentTypeView.ordinal());
        SettingsEditor.commit();
        return super.onOptionsItemSelected(item);
    }


    // ���������� �������
    public void ShowSubject(TSubject subject) {


        // ������� ���� ������� ��� ������ � ���
        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayoutStudent);
//        tableLayout.setOnTouchListener(this) ;        
        // ������� �������� ��������
        TableRow tableRowSubject = new TableRow(this);
        tableRowSubject.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TextView textViewSubject = new TextView(this);
        textViewSubject.setText(subject.stringSubject);
        tableRowSubject.addView(textViewSubject, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tableLayout.addView(tableRowSubject);


        // ������� ������, �� ��� � �����
        TableRow tableRowTimeSubject = new TableRow(this);
        tableRowTimeSubject.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TextView textViewTimeSubject = new TextView(this);
        StringBuilder stringTimeSubject = new StringBuilder("c ");
        stringTimeSubject.append(subject.byteStartWeek.toString());
        stringTimeSubject.append(" �� ");
        stringTimeSubject.append(subject.byteEndWeek.toString());

        switch (subject.weekType) {
            case even:
                stringTimeSubject.append(" �� ������, ");
                break;
            case noteven:
                stringTimeSubject.append(" �� ��������, ");
                break;
            case every:
                stringTimeSubject.append(" �����������, ");
                break;
        }

        stringTimeSubject.append(subject.stringTime);

        textViewTimeSubject.setText(stringTimeSubject.toString());
        tableRowTimeSubject.addView(textViewTimeSubject, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tableLayout.addView(tableRowTimeSubject);


        // ������� ����� ������� � �������������
        TableRow tableRowLocationTeacher = new TableRow(this);
        tableRowLocationTeacher.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TextView textViewLocationTeacher = new TextView(this);
        StringBuilder stringLocationTeacher = new StringBuilder(subject.stringLocation);
        stringLocationTeacher.append(", ");
        stringLocationTeacher.append(subject.stringTeacher);
        textViewLocationTeacher.setText(stringLocationTeacher.toString());
        tableRowLocationTeacher.addView(textViewLocationTeacher, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tableLayout.addView(tableRowLocationTeacher);


        // ������� �����������
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


    // ���������� ������
    public void ShowData(EnumType type) {
        try {
            setContentView(R.layout.scrolltable);

            // ������� ���� ������� ��� ������ � ���
            TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayoutStudent);
//            tableLayout.setOnTouchListener(this) ;

            ArrayList<String> listDaysOfWeek = new ArrayList<String>();

            // ������� ����� ������, ����, �������� ����������
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
                StringBuilder string = new StringBuilder(byteCurrentWeek.toString());
                string.append(" ������");
                textViewCurrentWeek.setText(string.toString());
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
            // ������� ����, ���� ���������� �� ����
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
                StringBuilder string = new StringBuilder(String.valueOf(dateCurrentDate.getDate()));
                string.append(".");
                string.append(String.valueOf(dateCurrentDate.getMonth() + 1));
                string.append(".");
                string.append(String.valueOf(dateCurrentDate.getYear() + 1900));
                string.append(", ");
                string.append(String.valueOf(byteCurrentWeek));
                string.append(" ���.");
                textViewCurrentDate.setText(string.toString());
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

            // ������� � ����� ��� �������� ���������, �������� ��� ��� �������� ����������
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

                    // ������� ���� ������
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


    // ������� ������������ �������� ����������
    public void Clear() {
        // ������� ���� ������� ��� ������ � ���
        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayoutStudent);
        tableLayout.removeAllViews();
    }


    // ��������� ������ ������� ������
    public byte GetWeek(Date date) {
        // ������ ����� �������: ���� ��������������� ����, �� ������������� 1 �������� ����������� ����
        Date fromday = new Date(date.getMonth() < 8 ? date.getYear() - 1 : date.getYear(), 8, 1);

        long longBeetweenDays = date.getTime() - fromday.getTime();
        // ���������� ����� ������ ������, ����� ������� �� �� ����� ����� ��� ������ �������
        long longTimeOfFirstWeek = ((7 - (fromday.getDay() == 0 ? 6 : fromday.getDay() - 1)) * MILLISECONDS_PER_DAY);
        // ���� ������ ���� ������ ����� ������ ������, �� ��� ������ ������, ����� �� �����������
        //byte byteWeekNum = (byte) (longBeetweenDays < longTimeOfFirstWeek ? 1 : ((longBeetweenDays - longTimeOfFirstWeek) / (MILLISECONDS_PER_WEEK) + 2));

        // ������� ����������� ������ ������ � 0
        byte byteWeekNum = (byte) (longBeetweenDays < longTimeOfFirstWeek ? 0 : ((longBeetweenDays - longTimeOfFirstWeek) / (MILLISECONDS_PER_WEEK) + 1));

        return byteWeekNum;
    }

    // ������������� ��������� ����
    public void ChangeDate(long time) {
        // �������������� ���������� ��������� ��������
        SharedPreferences.Editor SettingsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        // ��������� �������� ���� � ��������� ����������
        SettingsEditor.putLong("longCurrentDate", time);
        SettingsEditor.commit();

        dateCurrentDate = new Date(time);
        byteCurrentWeek = GetWeek(dateCurrentDate);
    }

    // ������������� ��������� ����� ������
    public void ChangeWeek(byte week) {
        // ������ ����� �������: ���� ��������������� ����, �� ������������� 1 �������� ����������� ����
        Date fromday = new Date(dateCurrentDate.getMonth() < 8 ? dateCurrentDate.getYear() - 1 : dateCurrentDate.getYear(), 8, 1);
        // ���������� ����� ������ ������, ����� ������� �� �� ����� ����� ��� ������ �������
        long longTimeOfFirstWeek = ((7 - (fromday.getDay() == 0 ? 6 : fromday.getDay() - 1)) * MILLISECONDS_PER_DAY + MILLISECONDS_PER_DAY / 4);
        //long longNewDate = ( week == 1 ? fromday.getTime() : fromday.getTime() + longTimeOfFirstWeek + (week - 2) * MILLISECONDS_PER_WEEK );
        // ������� ����������� ������ ������ � 0
        long longNewDate = (week == 0 ? fromday.getTime() : fromday.getTime() + longTimeOfFirstWeek + (week - 1) * MILLISECONDS_PER_WEEK);
        ChangeDate(longNewDate);
    }


}

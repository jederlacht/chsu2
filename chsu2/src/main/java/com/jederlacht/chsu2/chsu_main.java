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

    // ��������� ����������
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
            // ������� �� ������� ������������ ProgressDialog
            // ����� �� ������� ��� �������� ��������
            // ���� ����� ����������� � UI ������
            spinner = new ProgressDialog(chsu_main.this);
            spinner.setMessage("���� ��������...");
            spinner.show();


        }

        @Override
        protected Void doInBackground(Void... text) {

            //GetDataStart();
            // ��� �� ������ �������� ������ �� �������� ������
            // ���� ����� ���������� � ������ ������
            // ��������� ���������� � ����� ��� ������
            HttpURLConnection UrlConnection = null;
            BufferedReader BufferReader = null;


            try {
                URL url = new URL("http://ya.ru");
               doc=Jsoup.parse (url,1000);
                //Element e = doc.getElementsByAttribute("font[size$=\"5\"").first();
                //week=e.text();
                week="7";
/*
                // ��������� �����
                URL url = new URL("http://rasp.chsu.ru/_student.php");
                // ������������� ���������� � ���������
                UrlConnection = (HttpURLConnection) url.openConnection();
                UrlConnection.setRequestProperty("METHOD", "POST");
                UrlConnection.setDoInput(true);
                UrlConnection.setDoOutput(true);


                // �������� ����������� �������� � �����
                BufferReader = new BufferedReader(new InputStreamReader(UrlConnection.getInputStream(),
                        "windows-1251"), 4096);

                // ������� ������ ��� ��������
                String InputLine = "";
                // ��������� ���������� ���������
                while ((InputLine = BufferReader.readLine()) != null) {

/*                    // ���������, ��� �� �������� ������ � ������ ��������� ���������
                    if (InputLine.contains("������� ������")) {

                        Pattern pattern = Pattern.compile(">(\\d+)<");
                        Matcher matcher = pattern.matcher(InputLine);

                        if(matcher.find())
                        {
                            week = matcher.group(1);
                            //StringBuilder string = new StringBuilder(week);
                            //string.append(" ������");


                        }
                    }

                    // ���������, ��� �� �������� ������ � ������ ��������� ���������
                    if (InputLine.contains("�������")) {

                        Pattern pattern = Pattern.compile("(\\d{2}.\\d{2}.\\d{4})");
                        Matcher matcher = pattern.matcher(InputLine);

                        if(matcher.find())
                        {
                            day = matcher.group(1);

                        }
                    }

                    // ���������� ������ �����
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
                // ��������� �����������
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

            textCurrentWeek.setText(week + " ������");
            byteCurrentWeek = Byte.parseByte(week);
            // �������� ���������. ������� ProgressDialog.
            // ���� ����� ����������� � UI ������
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
        // ��������� ������ �� ���������
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        // �������������� ���������� ��������� ��������
        SettingsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        // ������� ������
        BtnOpenTable = (Button)findViewById(R.id.buttonOpenTable);
        BtnOpenTable.setText(R.string.open_table_command);
        BtnOpenTableExams = (Button)findViewById(R.id.buttonOpenTableExams);
        BtnOpenTableExams.setText(R.string.open_table_exam_command);

        // ������� ����, ����� ����� ��������� ��
        textCurrentGroup = (TextView)findViewById(R.id.TextViewGroup);
        textCurrentTerm = (TextView)findViewById(R.id.textViewTerm);
        textCurrentDate = (TextView)findViewById(R.id.textViewDate);
        textCurrentWeek = (TextView)findViewById(R.id.textViewWeek);

        context = chsu_main.this;
        //GetDataStart();// ��������!
        if (isOnline()){
            new MyTask().execute();
        }
        else {
            CharSequence text = "��� ���������� � �����!!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();


        }


        // ������� �� ������ "�������� ����������"
        BtnOpenTable.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(chsu_main.this, chsu_view.class);
                intent.putExtra("CURRENT_GROUP", stringCurrentGroup);
                intent.putExtra("CURRENT_TERM", stringCurrentTerm);
                intent.putExtra("CURRENT_WEEK", byteCurrentWeek);
                startActivity(intent);

            }
        });

        // ������� �� ������ "���������� ���������"
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


        // ������ ��������� �����������
        SharedPreferences SavedSettings = PreferenceManager.getDefaultSharedPreferences(this);
        stringCurrentGroup = SavedSettings.getString("sCurrentGroup", "�������� � ����������");
        stringCurrentTerm = SavedSettings.getString("sCurrentTerm", "������ � ");

        // ������������� �������� � ��������� ����

        textCurrentGroup.setText(stringCurrentGroup);
        StringBuilder string = new StringBuilder(stringCurrentTerm);
        string.append(" �������");
        textCurrentTerm.setText(string);
    }


    // ������� ����
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // ����� ������ ����
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item1) {
            // �� ������� ������ ��������� ���������
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





    // �������� ����� ������
    public  void GetDataStart() {
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


            // �������� ����������� �������� � �����
            BufferReader = new BufferedReader(new InputStreamReader(UrlConnection.getInputStream(),
                    "windows-1251"), 4096);

            // ������� ������ ��� ��������
            String InputLine = "";
            // ��������� ���������� ���������
            while ((InputLine = BufferReader.readLine()) != null) {

                // ���������, ��� �� �������� ������ � ������ ��������� ���������
                if (InputLine.contains("������� ������")) {

                    Pattern pattern = Pattern.compile(">(\\d+)<");
                    Matcher matcher = pattern.matcher(InputLine);
                    String week = null;
                    if(matcher.find())
                    {
                        week = matcher.group(1);
                        StringBuilder string = new StringBuilder(week);
                        string.append(" ������");
                        textCurrentWeek.setText(string);
                        byteCurrentWeek = Byte.parseByte(week);

                    }
                }

                // ���������, ��� �� �������� ������ � ������ ��������� ���������
                if (InputLine.contains("�������")) {

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

                // ���������� ������ �����
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
            // ��������� �����������
            if (UrlConnection != null) {
                ((HttpURLConnection)UrlConnection).disconnect();
            }
        }
    }







}
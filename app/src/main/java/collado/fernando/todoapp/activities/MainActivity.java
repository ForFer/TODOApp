package collado.fernando.todoapp.activities;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import collado.fernando.todoapp.R;
import collado.fernando.todoapp.adapters.MySection;
import collado.fernando.todoapp.helpers.AlarmReceiver;
import collado.fernando.todoapp.helpers.DBHelper;
import collado.fernando.todoapp.models.Task;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

@TargetApi(16)
public class MainActivity extends AppCompatActivity {

    protected DBHelper db;
    private HashMap<String, ArrayList<Task>> tasks_by_day;
    private ArrayList<MySection> mySections = new ArrayList<>();
    private RecyclerView sectionHeader;
    private String _bannerDate;
    SharedPreferences preferences;

    //private String[] TAGS = new String[]{"No tag", "Personal", "Android", "WICE", "Ejercicio", "Work", "TFG", "Free time"};
    private String[] TAGS;
    private String NIGHT_NOTIFICATION_TEXT = "Remember to set your tasks for the next day";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DBHelper(this);

        TAGS = db.getAllTags();

        FloatingActionButton addTask = findViewById(R.id.addTask);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = MainActivity.this.setCustomAlertDialog();
                builder.show();
            }
        });

        this.setLinearLayoutManager();
        this.setBanner(getDate());
        this.setNotifications();

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_analytics) {
            Intent statsIntent = new Intent(this, Stats.class);
            startActivity(statsIntent);
            return true;
        }

        if (id == R.id.action_tag) {
            Intent intent = new Intent(this, EditTags.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateBanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBanner();
    }

    protected void updateBanner(){
        /**
         * Check if banner has to be updated and do so
         */
        String[] date = getDate();
        if(!(_bannerDate.equals(date[1]))){
            setBanner(date);
        }
    }

    protected void setBanner(String[] date){
        /**
         * Set today's date at the banner (below the "let's do this" text)
         */
        TextView bannerDay = (TextView) findViewById(R.id.bannerDay);
        TextView bannerDate = (TextView) findViewById(R.id.bannerDate);

        bannerDay.setText(date[0]);

        _bannerDate = date[1];
        bannerDate.setText(_bannerDate);

    }

    protected String[] getDate() {

        String[] _date = new String[2];

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        _date[0] = sdf.format(d);

        long date = System.currentTimeMillis();
        sdf = new SimpleDateFormat("dd/MM/yyyy");
        _date[1] = sdf.format(date);

        return _date;
    }

    private void setNotifications(){
        /**
         * Set daily notifications
         * TODO: Change according to settings
         * Current behaviour: 2 daily notifications:
         *   - One at night to remind me to set my tasks for the next day
         *   - The other one ~ 5pm, to remind me to check the app and do stuff
         */

        boolean notif1 = preferences.getBoolean("switch_notif_1", true);

        if(notif1){

            String[] date = preferences.getString("notification_time_1", "22:30:00").split(":");
            Calendar calendar = calendarFromDate(date);

            String big_text = NIGHT_NOTIFICATION_TEXT ;
            setNotification(big_text,"", "1",calendar);
        }

        boolean notif2 = preferences.getBoolean("switch_notif_2", true);

        if(notif2) {

            String[] date = preferences.getString("notification_time_2", "17:30:00").split(":");
            Calendar calendar = calendarFromDate(date);

            String big_text = "Time to get stuff done!";
            int undoneTasks = db.getTodayUndoneTasks();
            String big_content_title = "You have " + undoneTasks + " tasks to do today";
            setNotification(big_text,big_content_title,"2", calendar);
        }
    }

    private Calendar calendarFromDate(String[] date){

        Calendar calendar = Calendar.getInstance();

        int[] time = {0, 0, 0};
        for (int i = 0; i < 3; i++) {
            if (date.length > i) time[i] = Integer.parseInt(date[i]);
        }

        calendar.set(Calendar.HOUR_OF_DAY, time[0]);
        calendar.set(Calendar.MINUTE, time[1]);
        calendar.set(Calendar.SECOND, time[2]);

        return calendar;
    }

    private void setNotification(String big_text, String big_content_title, String channel_id, Calendar calendar) {
        /**
         * Set daily notifications
         */
        //TODO: FIX putExtra -> values not being received at AlarmReceiver

        Intent notificationIntent = new Intent(this, AlarmReceiver.class);
        notificationIntent.putExtra("BIG_CONTENT_TITLE", big_content_title);
        notificationIntent.putExtra("BIG_TEXT", big_text);
        notificationIntent.putExtra("CHANNEL_ID", channel_id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                Integer.parseInt(channel_id),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) this.getSystemService(MainActivity.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void setLinearLayoutManager(){
        /**
         * Sets a LinearLayoutManager with a SectionedRecyclerView
         * Iterates over all the content of the DB to create sections (days)
         * with tasks each section
         */
        sectionHeader = findViewById(R.id.task_list);

        LinearLayoutManager mLinMan = new LinearLayoutManager(this);

        sectionHeader.setLayoutManager(mLinMan);
        sectionHeader.setHasFixedSize(true);

        setAdapterView();
    }

    private AlertDialog.Builder setCustomAlertDialog(){
        /**
         * Add Task handler
         * Creates an AlertDialog with a text and a DatePicker input
         * Stores in DB
         * Updates value of HashMap
         * And calls to notifyDataSetChanged in the section that the change was made
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add TODO");

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.add_task_dialog, null);
        final EditText input = alertLayout.findViewById(R.id.add_task_dialog);
        final DatePicker datePicker = alertLayout.findViewById(R.id.date_picker_dialog);
        final Spinner dropdown = alertLayout.findViewById(R.id.add_tag);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TAGS);
        dropdown.setAdapter(adapter);

        builder.setView(alertLayout);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString().trim();

                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;
                int year = datePicker.getYear();

                String str_day = day<=9?"0"+String.valueOf(day):String.valueOf(day);
                String str_month = month<=9?"0"+String.valueOf(month):String.valueOf(month);
                String current_date = String.valueOf(year) + "-" + str_month + "-" +  str_day ;
                long timestamp = System.currentTimeMillis();

                Task newTask = new Task(name, timestamp, current_date, dropdown.getSelectedItem().toString());
                db.addTask(newTask);

                setAdapterView();

                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder;
    }

    public void setAdapterView(){
        /**
         * Handles setting the data for SectionedRecyclerViewAdapter
         */
        SectionedRecyclerViewAdapter sectionAdapter = new SectionedRecyclerViewAdapter();
        tasks_by_day = db.getAllTasks();
        mySections = new ArrayList<>();

        for(Map.Entry<String,ArrayList<Task>> entry : tasks_by_day.entrySet()){
            String section_d = entry.getKey();
            MySection section = new MySection(section_d,entry.getValue(), sectionAdapter, this, TAGS);
            sectionAdapter.addSection(section);
            mySections.add(section);
        }

        sectionHeader.setAdapter(sectionAdapter);
    }
}

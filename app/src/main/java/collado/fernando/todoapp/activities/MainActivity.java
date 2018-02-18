package collado.fernando.todoapp.activities;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.DatePicker;
import android.widget.EditText;
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
import collado.fernando.todoapp.models.Stat;
import collado.fernando.todoapp.models.Task;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

@TargetApi(16)
public class MainActivity extends AppCompatActivity {

    protected DBHelper db;
    private HashMap<String, ArrayList<Task>> tasks_by_day;
    private ArrayList<MySection> mySections = new ArrayList<>();
    private RecyclerView sectionHeader;
    private String _bannerDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DBHelper(this);

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
            return true;
        }

        if (id == R.id.action_analytics) {
            ArrayList<Stat> allStats = db.getAllStats();
            Intent statsIntent = new Intent(this, Stats.class);
            statsIntent.putExtra("stats", allStats);
            startActivity(statsIntent);
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


        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 00);

        String big_text = "Remember to set your tasks for the next day";
        setNotification(big_text,"","", "1",calendar);

        /*
        calendar.set(Calendar.HOUR_OF_DAY, 13);

        calendar.set(Calendar.MINUTE, 04);
        calendar.set(Calendar.SECOND, 40);
        big_text = "Time to get stuff done!";
        int undoneTasks = db.getTodayUndoneTasks();
        String big_content_title = "You have " + undoneTasks + " tasks to do today";
        setNotification(big_text,"","", "2", calendar);
        */
    }

    private void setNotification(String big_text, String big_content_title, String summary_text, String channel_id, Calendar calendar) {
        /**
         * Set daily notifications
         */
        //TODO: FIX putExtra -> values not being received at AlarmReceiver

        Intent notificationIntent = new Intent(this, AlarmReceiver.class);
        notificationIntent.putExtra("BIG_CONTENT_TITLE", big_content_title);
        notificationIntent.putExtra("BIG_TEXT", big_text);
        notificationIntent.putExtra("SUMMARY_TEXT", summary_text);
        notificationIntent.putExtra("CHANNEL_ID", channel_id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                Integer.parseInt(channel_id),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

    }

    private void setLinearLayoutManager(){
        /**
         * Sets a LinearLayoutManager with a SectionedRecyclerView
         * Iterates over all the content of the DB to create sections (days)
         * with tasks each section
         */
        sectionHeader = (RecyclerView)findViewById(R.id.task_list);

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
        View alertLayout = inflater.inflate(R.layout.custom_date_picker, null);
        final EditText input = alertLayout.findViewById(R.id.add_task_dialog);
        final DatePicker datePicker = alertLayout.findViewById(R.id.date_picker_dialog);

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

                Task newTask = new Task(name, timestamp, current_date);
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
        SectionedRecyclerViewAdapter sectionAdapter = new SectionedRecyclerViewAdapter();
        tasks_by_day = db.getAllTasks();
        mySections = new ArrayList<>();

        for(Map.Entry<String,ArrayList<Task>> entry : tasks_by_day.entrySet()){
            String section_d = entry.getKey();
            MySection section = new MySection(section_d,entry.getValue(), sectionAdapter);
            sectionAdapter.addSection(section);
            mySections.add(section);
        }
        sectionHeader.setAdapter(sectionAdapter);
    }


}

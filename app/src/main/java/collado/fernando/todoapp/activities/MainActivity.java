package collado.fernando.todoapp.activities;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
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
import collado.fernando.todoapp.helpers.DBHelper;
import collado.fernando.todoapp.helpers.NotificationPublisher;
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
        //this.setNotification(getNotification("x TODOS to do today", "TODO"), (int)System.currentTimeMillis());
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
        sdf = new SimpleDateFormat("dd/MM/yyy");
        _date[1] = sdf.format(date);

        return _date;
    }

    private void setNotification(Notification notification, int notification_id) {
        /**
         * Set daily notifications (WiP)
         */
        Intent myIntent = new Intent(this , NotificationPublisher.class);
        myIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notification_id);
        myIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 20);
        calendar.set(Calendar.SECOND, 00);


        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000 , pendingIntent);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+5000, pendingIntent);

    }


    private Notification getNotification(String content, String title){
        Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle(title)
                    .setContentText(content);
        builder.setSmallIcon(R.drawable.ic_small_clipboard);
        return builder.build();
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

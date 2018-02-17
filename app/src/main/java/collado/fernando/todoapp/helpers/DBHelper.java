package collado.fernando.todoapp.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import collado.fernando.todoapp.models.Stat;
import collado.fernando.todoapp.models.Task;

/**
 * Created by Fernando on 3/02/18.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 11;

    private static final String DATABASE_NAME = "tasks.db";
    private static final String TAG = DBHelper.class.getSimpleName().toString();

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){

        String CREATE_TASK_TABLE = "CREATE TABLE IF NOT EXISTS tasks ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "task TEXT," +
                "done INTEGER," +
                "date DATE," +
                "timestamp INTEGER )";
        db.execSQL(CREATE_TASK_TABLE);

        CREATE_TASK_TABLE = "CREATE TABLE IF NOT EXISTS stats ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "total INTEGER," +
                "done INTEGER," +
                "date DATE )";
        db.execSQL(CREATE_TASK_TABLE);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVer){
        this.deleteAllTasks(db);
        this.deleteAllStats(db);
        this.onCreate(db);
    }

    public void deleteAllTasks(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS tasks");
    }

    public void deleteAllStats(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS stats");
    }

    public void addTask(Task task){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = taskToContentValues(task);
        db.insert(task.TABLE, null, value);
        db.close();

        updateStats(task, 0);

    }

    public Task getTask(int id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query("tasks", // a. table
                        Task.COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        if (cursor != null  && cursor.getCount()>0)
            cursor.moveToFirst();

        Task task = cursorToTask(cursor);

        return task;
    }

    public HashMap<String, ArrayList<Task>> getAllTasks(){
        /**
         * Create a LinkedHashMap<section_name, section_content> from all the tasks in DB
         * First grouping and ordering by date, then a query for each of those dates to
         * populate the HashMap
         */
        LinkedHashMap<String, ArrayList<Task>> tasks_by_day = new LinkedHashMap<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String date_query = "SELECT date FROM tasks group by date order by date DESC";
        Cursor cursor = db.rawQuery(date_query, null);

        if(cursor.moveToFirst()){
            do {
                String current_date = cursor.getString(0);
                String query = "SELECT * FROM tasks where date = ? order by timestamp DESC";
                Cursor inner_cursor = db.rawQuery(query, new String[] {current_date});

                ArrayList<Task> tasks = new ArrayList<>();
                Task task;
                if(inner_cursor.moveToFirst()){
                    do {
                        task = cursorToTask(inner_cursor);
                        tasks.add(task);
                    } while(inner_cursor.moveToNext());
                    tasks_by_day.put(current_date, tasks);
                }
            } while(cursor.moveToNext());
        }
        return tasks_by_day;
    }

    public int updateTask(Task task, boolean previousState){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = taskToContentValues(task);
        int i = db.update(Task.TABLE,
                value,
                Task.KEY_ID + " = ? ",
                new String[] { String.valueOf(task.getTaskId())}
                );

        db.close();

        updateStats(task, 2, previousState);

        return i;
    }

    public void deleteTask(Task task){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(Task.TABLE,
                Task.KEY_ID + " = ?",
                new String[] { String.valueOf(task.getTaskId()) }
                );
        db.close();

        updateStats(task, 1);
    }

    public Task cursorToTask(Cursor cursor){
        /**
         * Column order --> id, task, done, date, timestamp
         */
        Task task = new Task(cursor.getString(1), Long.parseLong(cursor.getString(4)), cursor.getString(3));

        task.setTaskId(Integer.parseInt(cursor.getString(0)));
        task.setDone(Integer.parseInt(cursor.getString(2)) == 1);

        return task;
    }

    public ContentValues taskToContentValues(Task task){
        /**
         * Create VALUES to insert from task
         */
        ContentValues value = new ContentValues();
        value.put(task.KEY_NAME, task.getName());
        value.put(task.KEY_DONE, task.isDone() == true ? 1 : 0);
        value.put(task.KEY__TIMESTAMP, task.get_timestamp());
        value.put(task.KEY_DATE, task.getDate());

        return value;
    }


    public Stat cursorToStat(Cursor cursor){
        /**
         * Column order --> id, total, done, date
         */
        Stat stat = new Stat(Integer.parseInt(cursor.getString(1)), Integer.parseInt(cursor.getString(2)), cursor.getString(3));
        stat.setID(Integer.parseInt(cursor.getString(0)));

        return stat;
    }

    public ContentValues statToContentValues(Stat stat){
        /**
         * Create VALUES to insert from stat
         */
        ContentValues value = new ContentValues();
        value.put(stat.KEY_DATE, stat.getDate());
        value.put(stat.KEY_DONE, stat.getDone());
        value.put(stat.KEY_TOTAL, stat.getTotal());

        return value;
    }

    public void addStat(Stat stat){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = statToContentValues(stat);
        db.insert(stat.TABLE, null, value);
        db.close();

    }

    public int updateStat(Stat stat){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = statToContentValues(stat);
        int i = db.update(Stat.TABLE,
                value,
                Stat.KEY_ID + " = ? ",
                new String[] { String.valueOf(stat.getId())}
        );

        db.close();

        return i;
    }



    public void updateStats(Task task, int option){
        /**
         * option 0 -> adding task
         * option 1 -> removing task
         */

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM stats where date = ?";
        Cursor cursor = db.rawQuery(query, new String[] {task.getDate()});

        Log.d("CURSOR", cursor.toString());

        if (cursor != null  && cursor.getCount()>0){ // If there is a stat for that day, get value, update, and store
            cursor.moveToFirst();
            Stat oldStat = cursorToStat(cursor);
            int total,totalDone;

            if(option == 0){
                total = oldStat.getTotal() + 1;
                totalDone = oldStat.getDone();
            }
            else{
                total = oldStat.getTotal() - 1;
                totalDone = task.isDone() ? oldStat.getDone() -1 : oldStat.getDone();
            }

            oldStat.setDone(totalDone);
            oldStat.setTotal(total);
            updateStat(oldStat);
        }
        else { // If there is no stat for that day, create value if adding task
            if(option == 0){
                Log.d("UPDATE_STATS","no cursor, and option = 0");
                Stat stat = new Stat(1, task.isDone() ? 1 : 0, task.getDate());
                addStat(stat);
            }
        }
    }

    public void updateStats(Task task, int option, boolean previousState){
        /**
         * case for when a task is being updated (from undone to done, and vice versa)
         *
         */

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM stats where date = ?";
        Cursor cursor = db.rawQuery(query, new String[] {task.getDate()});

        if (cursor != null  && cursor.getCount()>0) {
            cursor.moveToFirst();
            Stat oldStat = cursorToStat(cursor);
            // If previousState was true (done), subtract 1 from the done tasks
            int totalDone = previousState ? -1 : 1 + oldStat.getDone();
            oldStat.setDone(totalDone);
            updateStat(oldStat);
        }

    }

    public ArrayList<Stat> getAllStats(){

        ArrayList<Stat> stats = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();

        String date_query = "SELECT * FROM stats order by date DESC";
        Cursor cursor = db.rawQuery(date_query, null);

        if(cursor.moveToFirst()){
            Stat stat;
            do {
                stat = cursorToStat(cursor);
                stats.add(stat);
            } while(cursor.moveToNext());
        }
        return stats;

    }

}

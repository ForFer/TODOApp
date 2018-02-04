package collado.fernando.todoapp.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import collado.fernando.todoapp.models.Task;

/**
 * Created by root on 3/02/18.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 6;

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
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVer){
        this.deleteAllTasks(db);
        this.onCreate(db);
    }

    public void deleteAllTasks(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS tasks");
    }

    public void addTask(Task task){
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = taskToContentValues(task);
        db.insert(task.TABLE, null, value);
        db.close();
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

        if (cursor != null)
            cursor.moveToFirst();

        Task task = cursorToTask(cursor);

        return task;
    }

    public HashMap<String, ArrayList<Task>> getAllTasks(){

        LinkedHashMap<String, ArrayList<Task>> tasks_by_day = new LinkedHashMap<String, ArrayList<Task>>();
        SQLiteDatabase db = this.getWritableDatabase();

        String date_query = "SELECT date FROM tasks group by date order by date DESC";
        Cursor cursor = db.rawQuery(date_query, null);

        if(cursor.moveToFirst()){
            do {
                String current_date = cursor.getString(0);
                String query = "SELECT * FROM tasks where date = ? "; // + " order by timestamp ";
                Cursor inner_cursor = db.rawQuery(query, new String[] {current_date});

                ArrayList<Task> tasks = new ArrayList<Task>();
                Task task = null;
                if(inner_cursor.moveToFirst()){
                    do {
                        task = cursorToTask(inner_cursor);
                        tasks.add(task);
                    } while(inner_cursor.moveToNext());
                    tasks_by_day.put(dateFormat(current_date), tasks);
                }
            } while(cursor.moveToNext());
        }
        return tasks_by_day;
    }

    public int updateTask(Task task){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = taskToContentValues(task);
        int i = db.update(Task.TABLE,
                value,
                Task.KEY_ID + " = ? ",
                new String[] { String.valueOf(task.getTaskId())}
                );

        db.close();

        return i;
    }

    public void deleteTask(Task task){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(Task.TABLE,
                Task.KEY_ID + " = ?",
                new String[] { String.valueOf(task.getTaskId()) }
                );
        db.close();

    }

    public Task cursorToTask(Cursor cursor){
        //id, task, done, date, timestamp
        Task task = new Task(cursor.getString(1), Long.parseLong(cursor.getString(4)), dateFormat(cursor.getString(3)));

        task.setTaskId(Integer.parseInt(cursor.getString(0)));
        task.setDone(Integer.parseInt(cursor.getString(2)) == 1);

        return task;
    }

    public ContentValues taskToContentValues(Task task){
        ContentValues value = new ContentValues();
        value.put(task.KEY_NAME, task.getName());
        value.put(task.KEY_DONE, task.isDone() == true ? 1 : 0);
        value.put(task.KEY__TIMESTAMP, task.get_timestamp());
        value.put(task.KEY_DATE, task.getDate());

        return value;
    }

    public String dateFormat(String unformated_date){
        return unformated_date.substring(8,10) + '-' + unformated_date.substring(5,7) + '-' + unformated_date.substring(0,4);
    }

}

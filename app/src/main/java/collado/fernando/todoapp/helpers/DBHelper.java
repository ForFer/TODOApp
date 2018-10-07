package collado.fernando.todoapp.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import collado.fernando.todoapp.models.Stat;
import collado.fernando.todoapp.models.Tag;
import collado.fernando.todoapp.models.Task;

/**
 * Created by Fernando on 3/02/18.
 *
 * Functions
 * - onCreate
 * - onUpdate
 * - deleteAll* (one for every table)
 *
 * For every table T, in this order:
 * - addT
 * - getT (and getAllT,...)
 * - updateT (and updateAllT,...)
 * - deleteT
 * - cursorToT
 * - TtoContentValues
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 12;

    private static final String DATABASE_NAME = "tasks.db";

    private static final String STATS_QUERY_LIMIT = "1000";
    private static final String TASKS_QUERY_LIMIT = "1000";

    private static final String NO_TAG = "No tag";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){

        String CREATE_TASK_TABLE = "CREATE TABLE IF NOT EXISTS tasks ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "task TEXT," +
                "done INTEGER," +
                "date DATE," +
                "timestamp INTEGER," +
                "tag TEXT )";

        String CREATE_STAT_TABLE = "CREATE TABLE IF NOT EXISTS stats ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "total INTEGER," +
                "done INTEGER," +
                "date DATE )";


        String CREATE_TAG_TABLE = "CREATE TABLE IF NOT EXISTS tags ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tag TEXT )";

        db.execSQL(CREATE_TASK_TABLE);
        db.execSQL(CREATE_STAT_TABLE);
        db.execSQL(CREATE_TAG_TABLE);

    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVer){
        this.deleteAllTasks(db);
        this.deleteAllStats(db);
        this.deleteAllTags(db);
        this.onCreate(db);
    }

    public void deleteAllTasks(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS tasks");
    }

    public void deleteAllStats(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS stats");
    }

    public void deleteAllTags(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS tags");
    }

    public void addTask(Task task){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = taskToContentValues(task);
        long i = db.insert(task.TABLE, null, value);
        db.close();

        if(i > -1) updateStats(task, 0);
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

        String date_query = "SELECT date FROM tasks group by date order by date DESC LIMIT " + TASKS_QUERY_LIMIT;
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
        /**
         * If task done status change, update its value in the DB
         * and update stats accordingly
         */
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = taskToContentValues(task);
        int i = db.update(Task.TABLE,
                value,
                Task.KEY_ID + " = ? ",
                new String[] { String.valueOf(task.getTaskId())}
                );

        db.close();

        if(i > 0) updateStats(task, 2, previousState);

        return i;
    }

    public int updateTask(Task task){
        /**
         * If task name, or tag are changed, update its value in DB
         */
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

        int i = db.delete(Task.TABLE,
                Task.KEY_ID + " = ?",
                new String[] { String.valueOf(task.getTaskId()) }
                );
        db.close();

        if(i > 0) updateStats(task, 1);
    }

    public Task cursorToTask(Cursor cursor){
        /**
         * Converts a Cursor to Task
         * Column order --> id, task, done, date, timestamp, tag
         */
        Task task = new Task(cursor.getString(1), Long.parseLong(cursor.getString(4)), cursor.getString(3), cursor.getString(5));

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
        value.put(task.KEY_DONE, task.isDone() ? 1 : 0);
        value.put(task.KEY__TIMESTAMP, task.get_timestamp());
        value.put(task.KEY_DATE, task.getDate());
        value.put(task.KEY_TAG, task.getTag());

        return value;
    }

    public void addStat(Stat stat){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = statToContentValues(stat);
        db.insert(stat.TABLE, null, value);

    }

    public ArrayList<Stat> getAllStats(){
        /**
         * Get all stats up to STATS_QUERY_LIMIT number,
         * that fulfill the following two conditions
         *   - Has to be before today
         *   - Has to have at least 1 task in the "total" field
         */

        ArrayList<Stat> stats = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();

        String date_query = "SELECT * FROM stats order by date ASC LIMIT " + STATS_QUERY_LIMIT;
        Cursor cursor = db.rawQuery(date_query, null);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();

        if(cursor.moveToFirst()){
            Stat stat;
            do {
                stat = cursorToStat(cursor);
                try {
                    Date date = format.parse(stat.getDate());
                    if(today.compareTo(date) >= 0 && stat.getTotal() > 0) stats.add(stat);
                }
                catch (ParseException e){
                    // Nothing
                }
            } while(cursor.moveToNext());
        }

        return stats;

    }

    public int getTodayUndoneTasks(){
        /**
         * Get all undone tasks filtered by today's date
         */

        SQLiteDatabase db = this.getWritableDatabase();

        Calendar c = Calendar.getInstance();

        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH)+1;
        int year = c.get(Calendar.YEAR);

        String str_day = day<=9?"0"+String.valueOf(day):String.valueOf(day);
        String str_month = month<=9?"0"+String.valueOf(month):String.valueOf(month);
        String current_date = String.valueOf(year) + "-" + str_month + "-" +  str_day ;

        String query = "SELECT * FROM stats where date = ?";
        Cursor cursor = db.rawQuery(query, new String[] { current_date });

        Stat stat = new Stat(0,0,current_date);
        if(cursor.moveToNext()){
            stat = cursorToStat(cursor);
        }

        db.close();

        return stat.getUndone();
    }

    public int updateStat(Stat stat){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = statToContentValues(stat);
        int i = db.update(Stat.TABLE,
                value,
                Stat.KEY_DATE + " = ? ",
                new String[] { String.valueOf(stat.getDate())}
        );

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

        if (cursor != null  && cursor.getCount() > 0){ // If there is a stat for that day, get value, update, and store
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
                Stat stat = new Stat(1, task.isDone() ? 1 : 0, task.getDate());
                addStat(stat);
            }
        }
    }

    public void updateStats(Task task, int option, boolean previousState){
        /**
         * When a task is being updated (from undone to done, and vice versa)
         */

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM stats where date = ?";
        Cursor cursor = db.rawQuery(query, new String[] {task.getDate()});

        if (cursor != null  && cursor.getCount() > 0) {
            cursor.moveToFirst();
            Stat oldStat = cursorToStat(cursor);
            // If previousState was true (done), subtract 1 from the done tasks
            if(previousState){
                int totalDone = oldStat.getDone() - 1;
                if(totalDone < 0) oldStat.setDone(0);
                else oldStat.setDone(totalDone);
            }
            else{
                oldStat.setDone(oldStat.getDone() + 1);
            }
            updateStat(oldStat);
        }
    }

    public void populateStats(){
        /**
         * Deletes all stats, and populates the table again
         */

        SQLiteDatabase db = this.getWritableDatabase();

        deleteAllStats(db);
        onCreate(db);

        String date_query = "SELECT date FROM tasks group by date order by date ASC";
        Cursor cursor = db.rawQuery(date_query, null);

        Cursor inner_cursor;

        if(cursor.moveToFirst()){
            do {
                String current_date = cursor.getString(0);
                Stat stat = new Stat(0,0,current_date);

                String query = "SELECT * FROM tasks where date = ?";
                inner_cursor = db.rawQuery(query, new String[] {current_date});

                Task task;
                if(inner_cursor.moveToFirst()){
                    int total = 0;
                    int done = 0;
                    do {
                        task = cursorToTask(inner_cursor);
                        total++;
                        if(task.isDone()) done++;
                    } while(inner_cursor.moveToNext());

                    stat.setDone(done);
                    stat.setTotal(total);

                    addStat(stat);
                }
            } while(cursor.moveToNext());
        }
    }

    public Stat cursorToStat(Cursor cursor){
        /**
         * Converts a Cursor to Stat
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

    public void addTag(Tag tag){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = tagToContentValues(tag);
        db.insert(tag.TABLE, null, value);
        db.close();
    }

    public String[] getAllTags(){
        /**
         * Get all tags from DB, adds NO_TAG in the first position and
         * returns String[]
         */

        SQLiteDatabase db = this.getWritableDatabase();

        String[] tags;

        String query = "SELECT COUNT(tag) FROM tags";
        Cursor cursor = db.rawQuery(query, new String[] {});
        if(cursor.moveToFirst()) {
            int totalTags = cursor.getInt(0);

            tags = new String[totalTags + 1];
            tags[0] = NO_TAG;

            query = "SELECT * FROM tags ORDER BY tag";
            Cursor inner_cursor = db.rawQuery(query, new String[]{});

            int i = 1;

            if (inner_cursor .moveToFirst()) {
                do {
                    Tag _tag = cursorToTag(inner_cursor );
                    tags[i++] = _tag.getName();
                } while (inner_cursor .moveToNext());
            }
        }
        else{
            tags = new String[1];
            tags[0] = NO_TAG;
        }
        return tags;
    }

    public int updateTag(Tag tag, String previousName){
        /**
         * Given a tag, update its value, and update on cascade
         * all tasks that had that tag
         */

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = tagToContentValues(tag);

        int i = db.update(Tag.TABLE,
                value,
                Tag.KEY_NAME + " = ? ",
                new String[] { String.valueOf(previousName)}
        );

        db.close();

        // If any update was made
        if(i > 0) updateAllTags(tag.getName(), previousName);

        return i;
    }

    public void updateAllTags(String toTag, String fromTag){
        /**
         * Update all tasks that have as tag fromTag,
         * with the tag toTag
         */

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM tasks where tag = ?";
        Cursor cursor = db.rawQuery(query, new String[] {fromTag});

        if(cursor.moveToFirst()){
            do {
                Task task = cursorToTask(cursor);
                task.setTag(toTag);
                updateTask(task);

            } while(cursor.moveToNext());
        }
    }

    public void deleteTag(Tag tag){
        /**
         * Delete tag and update tasks with that tag to the
         * tag NO_TAG, unless it's the one they already had
         */

        SQLiteDatabase db = this.getWritableDatabase();
        String tagName = tag.getName();
        int i = db.delete(Tag.TABLE,
                Tag.KEY_NAME + " = ?",
                new String[] { String.valueOf(tagName) }
        );

        if(i > 0) updateAllTags(NO_TAG, tagName);
    }

    public Tag cursorToTag(Cursor cursor){
        /**
         * Converts a Cursor to Tag
         * Column order --> id, tag
         */
        Tag tag = new Tag(cursor.getString(1));
        return tag;
    }

    public ContentValues tagToContentValues(Tag tag){
        /**
         * Create VALUES to insert from task
         */
        ContentValues value = new ContentValues();
        value.put(tag.KEY_NAME, tag.getName());
        return value;
    }

    public ArrayList<Task> customTaskQuery(String query, String[] query_args){
        /**
         * Execute custom query from query + args
         * TODO:
         * Generalize for any query, including a third parameter Class c, and checking
         * the name to convert cursor to that class
         */

        ArrayList<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, query_args);

        if(cursor.moveToFirst()){
            Task task;
            do {
                task = cursorToTask(cursor);
                tasks.add(task);
            } while(cursor.moveToNext());
        }

        return tasks;
    }

    public ArrayList<Task> customTaskQuery(String query){
        /**
         * Execute custom query from query with no additional (or hardcoded) arguments
         * TODO:
         * Generalize for any query, including a third parameter Class c, and checking
         * the name to convert cursor to that class
         */
        ArrayList<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            Task task;
            do {
                task = cursorToTask(cursor);
                tasks.add(task);
            } while(cursor.moveToNext());
        }

        return tasks;
    }
}

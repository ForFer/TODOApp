package collado.fernando.todoapp.models;

/**
 * Created by Fernando on 3/02/18.
 */

public class Task {

    public static final String TAG = Task.class.getSimpleName();
    public static final String TABLE = "tasks";

    // Labels Table Column names
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "task";
    public static final String KEY_DATE = "date";
    public static final String KEY__TIMESTAMP = "timestamp";
    public static final String KEY_DONE = "done";
    public static final String[] COLUMNS = {KEY_ID, KEY_NAME, KEY_DATE, KEY_DONE, KEY__TIMESTAMP};

    private int taskId;
    private String name;
    private boolean done;
    private long _timestamp;
    private String date;

    public Task(String name, long _timestamp, String date){
        this.name = name;
        this.done = false;
        this._timestamp = _timestamp;
        this.date = date;
    }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public long get_timestamp() { return _timestamp; }

    public int getTaskId() { return taskId; }

    public void setTaskId(int taskId) { this.taskId = taskId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public void set_timestamp(long _timestamp) { this._timestamp = _timestamp; }

    public boolean isDone() { return done; }

    public void setDone(boolean done) { this.done = done; }

    @Override
    public String toString() {
        return "Id:" + this.taskId + " Task:" + this.name +
                " done: " + this.done + " date: " + this.date;
    }
}

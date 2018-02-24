package collado.fernando.todoapp.models;

/**
 * Created by Fernando on 24/02/18.
 */

public class Tag {

    public static final String TAG = Task.class.getSimpleName();
    public static final String TABLE = "tags";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "tag";
    public static final String[] COLUMNS = {KEY_ID, KEY_NAME};

    private String name;
    private int id;

    public Tag(String tag){
        this.name = tag;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "Tag: " + this.getName();
    }
}
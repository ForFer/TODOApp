package collado.fernando.todoapp.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by root on 11/02/18.
 */

public class Stat implements Parcelable {

    public static final String TAG = Stat.class.getSimpleName();
    public static final String TABLE = "stats";

    // Labels Table Column names
    public static final String KEY_ID = "id";
    public static final String KEY_TOTAL = "total";
    public static final String KEY_DATE = "date";
    public static final String KEY_DONE = "done";
    public static final String[] COLUMNS = {KEY_ID, KEY_TOTAL, KEY_DATE, KEY_DONE};

    private int done;
    private int total;
    private String date;
    private int id;

    public Stat(Parcel in){
        this.total = in.readInt();
        this.done = in.readInt();
        this.date = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(total);
        parcel.writeInt(done);
        parcel.writeString(date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Stat> CREATOR = new Parcelable.Creator<Stat>() {
        public Stat createFromParcel(Parcel in) {
            return new Stat(in);
        }

        @Override
        public Stat[] newArray(int i) {
            return new Stat[0];
        }
    };

    public Stat(int total, int done, String date){
        this.done = done;
        this.total = total;
        this.date = date;
    }

    public int getId() { return id; }

    public void setID(int id) { this.id = id; }

    public int getDone() { return done; }

    public void setDone(int done) { this.done = done; }

    public int getTotal() { return total; }

    public void setTotal(int total) { this.total = total; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }
}

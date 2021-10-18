package ir.mahdiparastesh.mbcounter.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Report implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "time")
    public long time;
    @ColumnInfo(name = "notes")
    public String notes;

    public Report(long time, String notes) {
        this.time = time;
        this.notes = notes;
    }

    public Report(Parcel in) {
        this.id = in.readLong();
        this.time = in.readLong();
        this.notes = in.readString();
    }

    public Report setId(long id) {
        this.id = id;
        return this;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        public Report[] newArray(int size) {
            return new Report[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.time);
        dest.writeString(this.notes);
    }
}

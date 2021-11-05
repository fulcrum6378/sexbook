package ir.mahdiparastesh.sexbook.data;

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
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "type")
    public Byte type;
    @ColumnInfo(name = "desc")
    public String desc;

    public Report(long time, String name, Byte type, String desc) {
        this.time = time;
        this.name = name;
        this.type = type;
        this.desc = desc;
    }

    public Report(Parcel in) {
        this.id = in.readLong();
        this.time = in.readLong();
        this.name = in.readString();
        this.type = in.readByte();
        this.desc = in.readString();
    }

    public Report setId(long id) {
        this.id = id;
        return this;
    }

    @SuppressWarnings("rawtypes")
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
        dest.writeString(this.name);
        dest.writeByte(this.type);
        dest.writeString(this.desc);
    }
}

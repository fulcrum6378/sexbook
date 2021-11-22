package ir.mahdiparastesh.sexbook.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Comparator;

@Entity
public class Crush {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "key")
    public String key;
    @ColumnInfo(name = "first_name")
    public String fName;
    @ColumnInfo(name = "last_name")
    public String lName;
    @ColumnInfo(name = "masculine")
    public boolean masculine;
    @ColumnInfo(name = "real")
    public boolean real;
    @ColumnInfo(name = "height")
    public short height;
    @ColumnInfo(name = "birth_year")
    public short birthYear;
    @ColumnInfo(name = "birth_month")
    public byte birthMonth;
    @ColumnInfo(name = "birth_day")
    public byte birthDay;
    @Nullable
    @ColumnInfo(name = "location")
    public String location;
    @Nullable
    @ColumnInfo(name = "instagram")
    public String instagram;
    @Nullable
    @ColumnInfo(name = "contact_id")
    public String contactId;
    @ColumnInfo(name = "notify_birth")
    public boolean notifyBirth;

    public Crush(@NonNull String key, String fName, String lName, boolean masculine, boolean real,
                 short height, short birthYear, byte birthMonth, byte birthDay,
                 @Nullable String location, @Nullable String instagram, @Nullable String contactId,
                 boolean notifyBirth) {
        this.key = key;
        this.fName = fName;
        this.lName = lName;
        this.masculine = masculine;
        this.real = real;
        this.height = height;
        this.birthYear = birthYear;
        this.birthMonth = birthMonth;
        this.birthDay = birthDay;
        this.location = location;
        this.instagram = instagram;
        this.contactId = contactId;
        this.notifyBirth = notifyBirth;
    }

    public String completeName() {
        return this.fName + " " + this.lName;
    }

    public static class Sort implements Comparator<Crush> {
        @Override
        public int compare(Crush a, Crush b) {
            return a.completeName().compareTo(b.completeName());
        }
    }
}

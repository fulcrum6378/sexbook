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
    public boolean masc;
    @ColumnInfo(name = "real")
    public boolean real;
    @ColumnInfo(name = "height")
    public float height;
    @ColumnInfo(name = "birth_year")
    public short bYear;
    @ColumnInfo(name = "birth_month")
    public byte bMonth;
    @ColumnInfo(name = "birth_day")
    public byte bDay;
    @Nullable
    @ColumnInfo(name = "location")
    public String locat;
    @Nullable
    @ColumnInfo(name = "instagram")
    public String insta;
    @ColumnInfo(name = "notify_birth")
    public boolean notifyBirth;

    public Crush(@NonNull String key, String fName, String lName, boolean masc, boolean real,
                 float height, short bYear, byte bMonth, byte bDay, @Nullable String locat,
                 @Nullable String insta, boolean notifyBirth) {
        this.key = key;
        this.fName = fName;
        this.lName = lName;
        this.masc = masc;
        this.real = real;
        this.height = height;
        this.bYear = bYear;
        this.bMonth = bMonth;
        this.bDay = bDay;
        this.locat = locat;
        this.insta = insta;
        this.notifyBirth = notifyBirth;
    }

    public boolean hasF() {
        return this.fName != null && !this.fName.equals("");
    }

    public boolean hasL() {
        return this.lName != null && !this.lName.equals("");
    }

    public String visName() {
        if (!this.hasF() || !this.hasL()) {
            if (this.hasF())
                return this.fName;
            else if (this.hasL())
                return this.lName;
            else return this.key;
        } else return this.fName + " " + this.lName;
    }

    public boolean hasFullBirth() {
        return bYear != 1 && bMonth != 1 && bDay != -1;
    }

    public static class Sort implements Comparator<Crush> {
        @Override
        public int compare(Crush a, Crush b) {
            return a.visName().compareTo(b.visName());
        }
    }
}

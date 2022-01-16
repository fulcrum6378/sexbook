package ir.mahdiparastesh.sexbook.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Comparator;

@Entity
public class Place {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "latitude")
    public double latitude;
    @ColumnInfo(name = "longitude")
    public double longitude;
    @Ignore
    public long sum = -1L;

    public Place(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Place setId(long id) {
        this.id = id;
        return this;
    }

    public static class Sort implements Comparator<Place> {
        public static final byte SUM = 0, NAME = 1;
        public byte by;

        public Sort(byte by) {
            this.by = by;
        }

        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        @Override
        public int compare(Place a, Place b) {
            switch (by) {
                case NAME:
                    return a.name.compareTo(b.name);
                default:
                    return (int) (b.sum - a.sum);
            }
        }
    }
}

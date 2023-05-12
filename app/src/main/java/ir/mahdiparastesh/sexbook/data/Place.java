package ir.mahdiparastesh.sexbook.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Comparator;

@Entity
public class Place {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public double latitude;
    public double longitude;
    @Ignore
    public transient long sum = -1L;

    @Ignore
    public Place(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Place() {
        this.name = "";
        this.latitude = -1.0;
        this.longitude = -1.0;
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
            return switch (by) {
                case NAME -> a.name.compareTo(b.name);
                default -> (int) (b.sum - a.sum);
            };
        }
    }
}

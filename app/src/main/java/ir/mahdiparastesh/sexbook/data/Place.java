package ir.mahdiparastesh.sexbook.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
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
        @Override
        public int compare(Place a, Place b) {
            return a.name.compareTo(b.name);
        }
    }
}

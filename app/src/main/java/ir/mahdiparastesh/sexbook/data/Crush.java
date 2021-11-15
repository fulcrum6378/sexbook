package ir.mahdiparastesh.sexbook.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
    @ColumnInfo(name = "height")
    public short height;
    @ColumnInfo(name = "instagram")
    public String instagram;

    public Crush(@NonNull String key, String fName, String lName, boolean masculine, short height,
                 String instagram) {
        this.key = key;
        this.fName = fName;
        this.lName = lName;
        this.masculine = masculine;
        this.height = height;
        this.instagram = instagram;
    }
}

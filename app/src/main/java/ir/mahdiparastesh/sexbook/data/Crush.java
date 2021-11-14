package ir.mahdiparastesh.sexbook.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Crush {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "key_name")
    public String keyName;
    @ColumnInfo(name = "first_name")
    public String fName;
    @ColumnInfo(name = "last_name")
    public String lName;
    @ColumnInfo(name = "masculine")
    public boolean masculine;

    public Crush(String keyName, String fName, String lName, boolean masculine) {
        this.keyName = keyName;
        this.fName = fName;
        this.lName = lName;
        this.masculine = masculine;
    }
}

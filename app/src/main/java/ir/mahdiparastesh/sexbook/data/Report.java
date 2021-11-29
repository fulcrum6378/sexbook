package ir.mahdiparastesh.sexbook.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Report {
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
    @ColumnInfo(name = "prox")
    public long prox;

    public Report(long time, String name, Byte type, String desc, long prox) {
        this.time = time;
        this.name = name;
        this.type = type;
        this.desc = desc;
        this.prox = prox;
    }

    public Report setId(long id) {
        this.id = id;
        return this;
    }
}

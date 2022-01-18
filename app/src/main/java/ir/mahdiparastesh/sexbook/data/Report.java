package ir.mahdiparastesh.sexbook.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
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
    public byte type;
    @ColumnInfo(name = "desc")
    public String desc;
    @ColumnInfo(name = "accu")
    public boolean acur;
    @ColumnInfo(name = "plac")
    public long plac;
    @Ignore
    private transient boolean estimated;

    public Report(long time, String name, byte type, String desc, boolean acur, long plac) {
        this.time = time;
        this.name = name;
        this.type = type;
        this.desc = desc;
        this.acur = acur;
        this.plac = plac;
    }

    @Ignore
    public Report(long time, String name, byte type, long plac) {
        this.estimated = true;
        this.time = time;
        this.name = name;
        this.type = type;
        this.plac = plac;
    }

    public Report setId(long id) {
        this.id = id;
        return this;
    }

    public boolean isReal() {
        return !estimated;
    }
}

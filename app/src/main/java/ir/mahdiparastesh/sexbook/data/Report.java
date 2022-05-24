package ir.mahdiparastesh.sexbook.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Report {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long time;
    public String name;
    public byte type;
    public String desc;
    public boolean acur;
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

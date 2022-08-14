package ir.mahdiparastesh.sexbook.data;

import androidx.annotation.Nullable;
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
    public boolean accu;
    public long plac;
    @Ignore
    private transient boolean estimated;

    public Report(long time, String name, byte type, String desc, boolean accu, long plac) {
        this.time = time;
        this.name = name;
        this.type = type;
        this.desc = desc;
        this.accu = accu;
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

    public boolean isReal() {
        return !estimated;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Report)) return false;
        return id == ((Report) obj).id;
    }
}

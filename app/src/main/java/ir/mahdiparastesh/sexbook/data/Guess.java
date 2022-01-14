package ir.mahdiparastesh.sexbook.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Comparator;

@Entity
public class Guess {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "sinc")
    public long sinc;
    @ColumnInfo(name = "till")
    public long till;
    @ColumnInfo(name = "freq")
    public byte cors;
    @ColumnInfo(name = "type")
    public byte type;
    @ColumnInfo(name = "desc")
    public String desc;
    @ColumnInfo(name = "plac")
    public long plac;

    public Guess(long sinc, long till, byte cors, byte type, String desc, long plac) {
        this.sinc = sinc;
        this.till = till;
        this.cors = cors;
        this.type = type;
        this.desc = desc;
        this.plac = plac;
    }

    public Guess setId(long id) {
        this.id = id;
        return this;
    }

    public static class Sort implements Comparator<Guess> {
        @Override
        public int compare(Guess a, Guess b) {
            return (int) (a.sinc - b.sinc);
        }
    }
}

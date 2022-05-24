package ir.mahdiparastesh.sexbook.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Guess {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long sinc;
    public long till;
    public float freq;
    public byte type;
    public String desc;
    public long plac;

    public Guess(long sinc, long till, float freq, byte type, String desc, long plac) {
        this.sinc = sinc;
        this.till = till;
        this.freq = freq;
        this.type = type;
        this.desc = desc;
        this.plac = plac;
    }

    public Guess setId(long id) {
        this.id = id;
        return this;
    }

    public boolean checkValid() {
        return sinc > -1L && till > -1L && freq > 0 &&
                till > sinc;
    }
}

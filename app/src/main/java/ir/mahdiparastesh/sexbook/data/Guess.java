package ir.mahdiparastesh.sexbook.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.Nullable;

@Entity
public class Guess {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @Nullable
    public String crsh;
    public long sinc;
    public long till;
    public float freq;
    public byte type;
    public String desc;
    public long plac;
    public boolean able;

    @Ignore
    public Guess(@Nullable String crsh, long sinc, long till, float freq, byte type, String desc,
                 long plac, boolean able) {
        this.crsh = crsh;
        this.sinc = sinc;
        this.till = till;
        this.freq = freq;
        this.type = type;
        this.desc = desc;
        this.plac = plac;
        this.able = able;
    }

    public Guess() {
        this.crsh = null;
        this.sinc = -1L;
        this.till = -1L;
        this.freq = 0f;
        this.type = 1;
        this.desc = "";
        this.plac = -1L;
        this.able = true;
    }

    public boolean checkValid() {
        return sinc > -1L && till > -1L && freq > 0 &&
                till > sinc;
    }
}

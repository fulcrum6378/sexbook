package ir.mahdiparastesh.sexbook.data;

import androidx.room.Entity;
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

    public Guess(@Nullable String crsh, long sinc, long till, float freq, byte type, String desc, long plac) {
        this.crsh = crsh;
        this.sinc = sinc;
        this.till = till;
        this.freq = freq;
        this.type = type;
        this.desc = desc;
        this.plac = plac;
    }

    public boolean checkValid() {
        return sinc > -1L && till > -1L && freq > 0 &&
                till > sinc;
    }
}

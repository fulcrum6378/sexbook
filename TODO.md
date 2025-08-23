# Sexbook To-Do List

### 📊 Data Access & Analysis

* Fortuna's SearchDialog for searching through `Report`s in their names and descriptions
* Number range filters for birth year, birth month, height, zodiac sign, first met year, and sum
  for `Screening`
* Screening using chackboxes rather than spinners
* Hide crushes with "Last Orgasm" older than...
* Yearly time series charts for `Singular` and `Mixture`
* An ultimate report out of `Taste` data
* Only statisticise "the enabled" crushes
* Non-orgasm reports may never need to be statisticised
* `Durability` statistics for Crushes
* Classify crushes to be either short-term or long-term? And predict future of current crushes?!?
* Apply `Hide disappeared people` 🚨

### 📝 Data Entry

* Handle the situation if someone puts a crazy frequency value in Guess
* UI for selecting `hue` for `Crush`es
* More body attributes? { nose size?, voice type?, lip shape?, eyebrow shape?, upper body shape?, }
* Main social media select: { Instagram, Tiktok, X, Facebook, ... }
* An `unidentified` status?
* Record `Crush` activations and deactivations on each Report like Git?

### 🚀 UI Ease of Use

* A checkbox for saving `Screening` filters in SP
* Don't discard the user's selected date in `Settings`:`statSince`; instead uncheck the checkbox.
* Altering `statSince` doesn't refresh `Main`
* Jump to `Identify` from Fortuna
* Turning off birthday notifications was not possible!
* Undo for all important actions

### 🔮 UI Clarity

* Tooltips for Nav + guides on their Toolbars
* Better UI for random crush suggestion and remember a suggestion till the end of the day

### 🕰 Compatibility

* Implement `onBackPressed` compatibility in `BaseActivity`

### ⚡ Performance & App Size

* Vectorise all other icons

### ✨ UI Aesthetics

* Elevation shadows for the tops of `SummaryDialog` and `RecencyDialog`' lists and the bottom of the
  former

---

> ### 💻 Notes for the future Compose Multiplatform app
>
> We won't need something like MCDTP in the new Sexbook; just a simple field-based date time picker
> is enough.

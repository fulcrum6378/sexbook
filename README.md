# Sexbook

A fun Android app for recording one's sexual activities. One can note their orgasm just in time or sometime later than
that using a date picker. It has a yellow theme with a banana icon.

This app uses [Multi-Calendar Date Time Picker](https://github.com/fulcrum6378/mcdtp), meaning you can record your
sexual activities in the Gregorian calendar or any other one, but the app currently supports only Persian and Indian
calendars. The default calendar can be easily changed in *Settings*.

### Main Activity

The [**Main.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/Main.kt) Activity contains 2 fragments:

1. [**PageSex.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/PageSex.kt) :
   which list sex records and their data, including date, time, name of the sexual partner, the place where the sexual
   intercourse happened and description.

   It uses the [**Report.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/data/Report.kt) database model.
   Keywords like *"and"*, *"&"* and *"+"* can be used for multiple sexual partners in masturbations or triple sexual
   intercourses.
   Sexual intercourses have types with specific icons, ranging from a simple Wet Dream to Vaginal Sex,
   including Masturbation, Oral Sex and Anal Sex.
2. [**PageLove.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/PageLove.kt) :
   which lists sexual partners, there is an *Identification* section for each, and all fields are quite optional.
   The fields are first, middle and last names, male/female, height, birth date, location, Instagram address and a
   switch button for notifying the user of their birthdate (requires notification permission in Android 13+).

   It uses the [**Crush.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/data/Crush.kt) database model.

### Statistics

The statistics are accessible via the navigation menu in Main or PageLove.
The related codes are in the subpackage [**stat**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat).
Statistics can be easily filtered in [**Settings**](app/src/kotlin/ir/mahdiparastesh/sexbook/Settings.kt).
The statistical charts are provided by [**HelloCharts**](https://github.com/fulcrum6378/HelloCharts).

- [**Summary**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/Summary.kt) :
  shows a summary of one's sexual partners (crushes) and number of times they had sexual intercourses.
  when clicked on a partner, their [**Singular**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/Singular.kt)
  line chart will pop up.
- [**Recency**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/Recency.kt) :
  lists sexual partners by the last time they've had a sexual intercourse.
  The clicking behaviour is the same as the Summary list.
- [**Adorability**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/Adorability.kt) :
  makes a line chart of sexual partners using the number of sexual intercourses in each month.
- [**Growth**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/Growth.kt) :
  same as Adorability, but this one shows the numbers additively.
- [**Mixture**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/Mixture.kt) :
  shows months by the number of total sexual intercourses, regardless of sexual partners.

### Other Activity instances

excluding Settings and statistical activities.

- [**Estimation.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/Estimation.kt) :
  this activity lets you estimate your sexual history with specific sexual partners, date ranges and places.

  It uses the [**Guess.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/data/Guess.kt) database model.
- [**Places.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/Places.kt) :
  this activity records places in which sexual intercourses happened.

  It uses the [**Place.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/data/Place.kt) database model.
  It can be linked to several rows in Report table. (It was designed to take latitude and longitude columns too,
  but since it didn't seem to be a good idea, I didn't implement anything for them.)

### Other subpackages

- [**data**](app/src/kotlin/ir/mahdiparastesh/sexbook/data) : everything related to database.
- [**list**](app/src/kotlin/ir/mahdiparastesh/sexbook/list) : all RecyclerView adapters.
- [**more**](app/src/kotlin/ir/mahdiparastesh/sexbook/more) : miscellaneous utilities.
- [**stat**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat) : everything related to statistics (as mentioned above).

### JSON Data Export/Import

Databases can be exported a **sexbook.json** file, and be sent everywhere by the user.
Therefore, the four database model classes should be exempted from [obfuscation](app/proguard-rules.pro).
The navigation menu in *Main* has these 3 options:

- Export to Json
- Import from Json
- Send Json to

### Other Features

- [*Last Orgasm*](app/src/kotlin/ir/mahdiparastesh/sexbook/more/LastOrgasm.kt) is an app widget which shows
  the number of hours past since one's last orgasm!
- Putting crush birthdays in the system calendar, which can be tweaked in *Settings*.
- A [*quick settings tile*](app/src/kotlin/ir/mahdiparastesh/sexbook/more/SexTileService.kt) and an
  [*app shortcut*](app/src/res/xml/shortcuts.xml) for recording an orgasm right away!

### Localisation

This app currently supports only the below languages.
The statistics are from Google Play Console (334 active users, updated at 2023.07.07),
but there are more users from Galaxy Store.

| Language        | Active Users        |
|:----------------|:--------------------|
| English (en-GB) | 131(US) & 24+26(GB) |
| Czech (cz)      | 8                   |
| German (de)     | 37+3                |
| Dutch (nl)      | 5+4                 |
| Russian (ru)    | 17+1                |

I may add translations for Spanish(1+14) and French(11+4) later.

### Publishing

Using this [privacy policy](https://mahdiparastesh.ir/welcome/privacy/sexbook.html),
it is now available for free and without ads in:

- [Google Play](https://play.google.com/store/apps/details?id=ir.mahdiparastesh.sexbook)
- [Galaxy Store](https://galaxystore.samsung.com/detail/ir.mahdiparastesh.sexbook)
- [ApkPure](https://apkpure.com/p/ir.mahdiparastesh.sexbook)

The app contains AdMob codes for displaying ads all over it, but they're all commented for the time being!

### License

```
Copyright © Mahdi Parastesh - All Rights Reserved.
```

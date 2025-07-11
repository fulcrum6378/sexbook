# Sexbook

A fun Android app for tracking data about one's sexual activities.
A user can note their orgasm just in time or later than that using a date picker.
This app has a yellow theme with a banana icon.

This app uses [Multi-Calendar Date Time Picker](https://github.com/fulcrum6378/mcdtp);
meaning you can track your sexual activities in the Gregorian calendar or any other one,
but this app currently supports only Gregorian, Persian and Indian calendars.
The default calendar can be easily changed in *Settings*.

### Main Activity

The [**Main.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/page/Main.kt) Activity contains 2 fragments:

1. [**PageSex.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/page/PageSex.kt) :
   which list sex records and their data, including date, time, name of the sexual partner,
   the place where the sexual intercourse happened and description.

   It uses the [**Report.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/data/Report.kt) database
   model.
   Keywords like *"and"*, *"&"* and *"+"* can be used for multiple sexual partners in masturbations
   or triple sexual intercourses.
   Sexual intercourses have types with specific icons, ranging from a simple Wet Dream to
   Vaginal Sex, including Masturbation, Oral Sex and Anal Sex.

2. [**PageLove.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/page/PageLove.kt) :
   which lists sexual partners, there is an *Identification* section for each,
   and all fields are quite optional. The fields are first, middle and last names, male/female,
   height, birth date, location, Instagram address and a switch button for notifying the user
   of their birthdate.

   It uses the [**Crush.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/data/Crush.kt) database
   model.

### Subpackages

- [**base**](app/src/kotlin/ir/mahdiparastesh/sexbook/base) : abstract classes
- [**ctrl**](app/src/kotlin/ir/mahdiparastesh/sexbook/ctrl) : responsible for controlling data (
  controller of MVC)
- [**data**](app/src/kotlin/ir/mahdiparastesh/sexbook/data) : data models of the database (model of
  MVC)
- [**list**](app/src/kotlin/ir/mahdiparastesh/sexbook/list) : all RecyclerView adapters
- [**list**](app/src/kotlin/ir/mahdiparastesh/sexbook/page) : all pages of this application
- [**stat**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat) :
  everything related to statistics (as mentioned above)
- [**misc**](app/src/kotlin/ir/mahdiparastesh/sexbook/util) : UX-related utilities
- [**view**](app/src/kotlin/ir/mahdiparastesh/sexbook/view) : UI-related utilities

### Statistics

The statistics are accessible via the navigation menu in Main or PageLove.
The related codes are in the subpackage [**stat**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat).
Statistics can be easily filtered in
[**Settings**](app/src/kotlin/ir/mahdiparastesh/sexbook/page/Settings.kt).
The statistical charts are provided by the forked library
[**HelloCharts**](https://github.com/fulcrum6378/HelloCharts).

- [**Summary**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/Summary.kt) :
  shows a summary of one's sexual partners (crushes) and the number of times they had sexual
  intercourses. When clicked on a partner, their monthly statistics will pop up as a line chart.
- [**Recency**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/RecencyDialog.kt) :
  lists sexual partners by the last time they've had a sexual intercourse.
  The clicking behaviour is the same as the Summary list.
- [**Adorability**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/Adorability.kt) :
  makes a multiple charts of sexual partners measuring their numbers of sexual intercourses in
  each month.
- [**Mixture**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/Mixture.kt) :
  shows months by the number of total sexual intercourses, regardless of sexual partners.
- [**Intervals**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/Intervals.kt) :
  statisticises delays between orgasms in terms of hours.
- [**Taste**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/Taste.kt) :
  creates numerous charts suggesting the user's sexual taste.
- [**Singular**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/Singular.kt) :
  creates a line chart singularly for a specific sexual partner.
- [**CrushesStat**](app/src/kotlin/ir/mahdiparastesh/sexbook/stat/CrushesStat.kt) :
  statisticises the features of people; like gender, body characteristics, height and age.

### Other Activities

- [**Estimation.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/page/Estimation.kt) :
  this activity lets you estimate your sexual history with specific sexual partners,
  date ranges and places.

  It uses the [**Guess.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/data/Guess.kt) database model.

- [**Places.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/page/Places.kt) :
  this activity tracks places in which sexual intercourses happened.

  It uses the [**Place.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/data/Place.kt) database model.
  It can be linked to several rows in Report table. (It was designed to take latitude and longitude
  columns too, but since it didn't seem to be a good idea, I didn't implement anything for them.)

- [**Settings.kt**](app/src/kotlin/ir/mahdiparastesh/sexbook/page/Settings.kt) :
  this activity controls settings and stores them as shared preferences.
  It also can wipe out the database and/or shared preferences.

### JSON Data Export/Import

Databases can be exported a **sexbook.json** file, and be sent everywhere by the user.
Therefore, the four database model classes should be exempted from
[obfuscation](app/proguard-rules.pro).
The navigation menu in *Main* has these 3 options:

- Export to Json
- Import from Json
- Send Json to

### Other Features

- [*Last Orgasm*](app/src/kotlin/ir/mahdiparastesh/sexbook/ctrl/LastOrgasm.kt) is an app widget
  which shows the number of hours past since one's last orgasm!
- Putting crush birthdays in the system calendar, which can be tweaked in *Settings*.
- An [*app shortcut*](app/src/res/xml/shortcuts.xml) for tracking an orgasm right away!

### [**Source-Available License**](https://github.com/fulcrum6378/sexbook/blob/master/LICENSE.md)

```
Sexbook - a full-fledged sexual diary
Written in 2020-2025 by Mahdi Parastesh <fulcrum1378@gmail.com>

This source code is made available under the "Sexbook License"
(a source-available, NON-open-source license).

See the full text here:
<https://github.com/fulcrum6378/sexbook/blob/master/LICENSE.md>
```

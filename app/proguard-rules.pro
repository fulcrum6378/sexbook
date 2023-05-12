-keep class ir.mahdiparastesh.sexbook.data.Exporter$Exported { <fields>; }
-keep class ir.mahdiparastesh.sexbook.data.Report { <fields>; }
-keep class ir.mahdiparastesh.sexbook.data.Crush { <fields>; }
-keep class ir.mahdiparastesh.sexbook.data.Place { <fields>; }
-keep class ir.mahdiparastesh.sexbook.data.Guess { <fields>; }
-keep class ir.mahdiparastesh.sexbook.more.HumanistIranianCalendar

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

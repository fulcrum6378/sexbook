-keep class ir.mahdiparastesh.sexbook.ctrl.Exporter$Exported { <fields>; }
-keep class ir.mahdiparastesh.sexbook.util.HumanistIranianCalendar

# used in MultiChartActivity
-keepclassmembers class ir.mahdiparastesh.hellocharts.view.LineChartView { public <init>(***); }
-keepclassmembers class ir.mahdiparastesh.hellocharts.view.PieChartView { public <init>(***); }

# inheritance for CrushAttrChart (initially: public java.lang.Integer preferredColour(int);)
-keepclassmembers interface ir.mahdiparastesh.sexbook.stat.base.CrushAttrChart { *; }
-keepclassmembers interface * extends ir.mahdiparastesh.sexbook.stat.base.CrushAttrChart { *; }

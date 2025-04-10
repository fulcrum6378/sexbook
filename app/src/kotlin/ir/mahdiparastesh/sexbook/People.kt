package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.ctrl.Screening
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.PeopleBinding
import ir.mahdiparastesh.sexbook.list.PersonAdap
import ir.mahdiparastesh.sexbook.stat.CrushesStat
import ir.mahdiparastesh.sexbook.util.Delay
import ir.mahdiparastesh.sexbook.view.Lister
import kotlin.experimental.and

class People : BaseActivity(), Toolbar.OnMenuItemClickListener, Lister {
    lateinit var b: PeopleBinding
    val mm: MyModel by viewModels()

    override var countBadge: BadgeDrawable? = null

    class MyModel : ViewModel() {
        lateinit var visPeople: ArrayList<String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PeopleBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.people)
    }

    override fun onResume() {
        super.onResume()
        arrangeList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        b.toolbar.inflateMenu(R.menu.people)
        b.toolbar.setOnMenuItemClickListener(this)
        updateFilterIcon()
        return true
    }

    fun updateFilterIcon() {
        b.toolbar.menu.findItem(R.id.filter)?.setIcon(
            if (c.screening?.any() == true) R.drawable.filtered else R.drawable.filter
        )
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(Crush.Sort.findSortMenuItemId(c.sp.getInt(Settings.spPeopleSortBy, 0)))
            ?.isChecked = true
        menu?.findItem(
            if (c.sp.getBoolean(Settings.spPeopleSortAsc, true))
                R.id.sortAsc else R.id.sortDsc
        )?.isChecked = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.chart -> if (c.people.isNotEmpty()) CrushesStat().apply {
                arguments = Bundle().apply { putInt(CrushesStat.BUNDLE_WHICH_LIST, 0) }
                show(supportFragmentManager, CrushesStat.TAG)
            }
            R.id.filter -> if (c.people.isNotEmpty())
                Screening().show(supportFragmentManager, "screening")
            else -> {
                Crush.Sort.sort(item.itemId)?.also { value ->
                    item.isChecked = true
                    c.sp.edit().apply {
                        if (value is Int) putInt(Settings.spPeopleSortBy, value)
                        else if (value is Boolean) putBoolean(Settings.spPeopleSortAsc, value)
                    }.apply()
                    arrangeList()
                }
            }
        }
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun arrangeList() {
        val hideUnsafe = c.sp.getBoolean(Settings.spHideUnsafePeople, true) && c.unsafe.isNotEmpty()
        val filters = c.screening
        mm.visPeople = ArrayList(
            when {
                filters?.any() == true -> c.people.filter { p ->

                    // Crush::status
                    if (filters.gender != 0 &&
                        filters.gender != (p.value.status and Crush.STAT_GENDER).toInt()
                    ) return@filter false
                    if (filters.fiction != 0 &&
                        (filters.fiction - 1) != (p.value.status and Crush.STAT_FICTION).toInt() shr 3
                    ) return@filter false
                    if (filters.safety != 0 &&
                        (filters.safety - 1) != (p.value.status and Crush.STAT_UNSAFE_PERSON).toInt() shr 5
                    ) return@filter false

                    // sum of assigned Reports
                    if (filters.minSum > 0 &&
                        p.value.getSum(c) < filters.minSum
                    ) return@filter false

                    // Crush::body
                    val body = p.value.body
                    if (filters.bodySkinColour != 0 && filters.bodySkinColour !=
                        (body and Crush.BODY_SKIN_COLOUR.first) shr Crush.BODY_SKIN_COLOUR.second
                    ) return@filter false
                    if (filters.bodyHairColour != 0 && filters.bodyHairColour !=
                        (body and Crush.BODY_HAIR_COLOUR.first) shr Crush.BODY_HAIR_COLOUR.second
                    ) return@filter false
                    if (filters.bodyEyeColour != 0 && filters.bodyEyeColour !=
                        (body and Crush.BODY_EYE_COLOUR.first) shr Crush.BODY_EYE_COLOUR.second
                    ) return@filter false
                    if (filters.bodyEyeShape != 0 && filters.bodyEyeShape !=
                        (body and Crush.BODY_EYE_SHAPE.first) shr Crush.BODY_EYE_SHAPE.second
                    ) return@filter false
                    if (filters.bodyFaceShape != 0 && filters.bodyFaceShape !=
                        (body and Crush.BODY_FACE_SHAPE.first) shr Crush.BODY_FACE_SHAPE.second
                    ) return@filter false
                    if (filters.bodyFat != 0 && filters.bodyFat !=
                        (body and Crush.BODY_FAT.first) shr Crush.BODY_FAT.second
                    ) return@filter false
                    if (filters.bodyMuscle != 0 && filters.bodyMuscle !=
                        (body and Crush.BODY_MUSCLE.first) shr Crush.BODY_MUSCLE.second
                    ) return@filter false
                    if (filters.bodyBreasts != 0 && filters.bodyBreasts !=
                        (body and Crush.BODY_BREASTS.first) shr Crush.BODY_BREASTS.second
                    ) return@filter false
                    if (filters.bodyPenis != 0 && filters.bodyPenis !=
                        (body and Crush.BODY_PENIS.first) shr Crush.BODY_PENIS.second
                    ) return@filter false
                    if (filters.bodySexuality != 0 && filters.bodySexuality !=
                        (body and Crush.BODY_SEXUALITY.first) shr Crush.BODY_SEXUALITY.second
                    ) return@filter false

                    return@filter true
                }
                hideUnsafe -> c.people.filter { p -> !p.value.unsafe() }
                else -> c.people
            }.keys)
        mm.visPeople.sortWith(Crush.Sort(c, Settings.spPeopleSortBy, Settings.spPeopleSortAsc))

        if (b.list.adapter == null) b.list.adapter = PersonAdap(this@People)
        else b.list.adapter?.notifyDataSetChanged()
        b.empty.isVisible = mm.visPeople.isEmpty()
        Delay(100L) { count(mm.visPeople.size) }
    }
}

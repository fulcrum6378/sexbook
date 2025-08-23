package ir.mahdiparastesh.sexbook.page

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import com.google.android.material.badge.BadgeDrawable
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.Lister
import ir.mahdiparastesh.sexbook.ctrl.Screening
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.PeopleBinding
import ir.mahdiparastesh.sexbook.list.PersonAdap
import ir.mahdiparastesh.sexbook.stat.CrushesStat
import ir.mahdiparastesh.sexbook.util.Delay

/**
 * This Activity lists and controls the [Crush] table in the database.
 */
class People : BaseActivity(), Toolbar.OnMenuItemClickListener, Lister {
    lateinit var b: PeopleBinding
    val vm: Model by viewModels()

    override var countBadge: BadgeDrawable? = null

    class Model : ViewModel() {
        lateinit var visPeople: ArrayList<String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!c.dbLoaded) {
            onBackPressedDispatcher.onBackPressed()
            return; }

        super.onCreate(savedInstanceState)
        b = PeopleBinding.inflate(layoutInflater)
        setContentView(b.root)
        configureToolbar(b.toolbar, R.string.people)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            menu?.setGroupDividerEnabled(true)
        menu?.findItem(
            Crush.Sort.findSortMenuItemId(
                c.sp.getInt(Settings.spPeopleSortBy, 0)
            )
        )?.isChecked = true
        menu?.findItem(
            if (c.sp.getBoolean(Settings.spPeopleSortAsc, true))
                R.id.sortAsc else R.id.sortDsc
        )?.isChecked = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.chart -> if (c.people.isNotEmpty()) CrushesStat.create(this, 0)
            R.id.filter -> if (c.people.isNotEmpty()) Screening.create(this)

            else -> {
                Crush.Sort.sort(item.itemId)?.also { value ->
                    item.isChecked = true
                    c.sp.edit().apply {
                        if (value is Int) putInt(Settings.spPeopleSortBy, value)
                        else if (value is Boolean) putBoolean(Settings.spPeopleSortAsc, value)
                    }.apply()
                    arrangeList()
                } ?: return false
            }
        }
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun arrangeList() {
        val hideUnsafe = c.hideUnsafe()
        val filters = c.screening
        vm.visPeople = ArrayList(
            when {
                filters?.any() == true -> c.people.filter { p ->

                    // search query
                    if (filters.search.isNotBlank()) {
                        val queriesPresence = arrayListOf<Boolean>()
                        for (q in filters.search.trim().split(" ")) {
                            val presenceInScopes = arrayListOf<Boolean>()
                            presenceInScopes.add(p.value.key.contains(q, true))
                            presenceInScopes.add(
                                p.value.first_name
                                    ?.contains(q, true) == true
                            )
                            presenceInScopes.add(
                                p.value.middle_name
                                    ?.contains(q, true) == true
                            )
                            presenceInScopes.add(
                                p.value.last_name
                                    ?.contains(q, true) == true
                            )
                            presenceInScopes.add(
                                p.value.address
                                    ?.contains(q, true) == true
                            )
                            presenceInScopes.add(
                                p.value.instagram
                                    ?.contains(q, true) == true
                            )
                            queriesPresence.add(presenceInScopes.any { it })
                        }
                        if (!queriesPresence.all { it }) return@filter false
                    }

                    // Crush::status
                    if (filters.presence != 0 && filters.presence != p.value.presence())
                        return@filter false
                    if (filters.gender != 0 && filters.gender != p.value.gender())
                        return@filter false
                    if (filters.safety != 0 &&
                        (filters.safety - 1) != (if (p.value.unsafe()) 1 else 0)
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

                    return@filter true
                }
                hideUnsafe -> c.people.filter { p -> !p.value.unsafe() }
                else -> c.people
            }.keys)
        vm.visPeople.sortWith(
            Crush.Sort(
                c, Settings.spPeopleSortBy, Settings.spPeopleSortAsc
            )
        )

        if (b.list.adapter == null) b.list.adapter = PersonAdap(this@People)
        else b.list.adapter?.notifyDataSetChanged()
        b.empty.isVisible = vm.visPeople.isEmpty()
        Delay(100L) { count(vm.visPeople.size) }
    }
}

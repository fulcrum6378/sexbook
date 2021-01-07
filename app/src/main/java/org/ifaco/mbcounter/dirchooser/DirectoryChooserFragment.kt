package org.ifaco.mbcounter.dirchooser

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.os.FileObserver
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.gu.option.Option
import org.ifaco.mbcounter.R
import java.io.File
import java.util.*

class DirectoryChooserFragment : DialogFragment() {
    private var mNewDirectoryName: String? = null
    private var mInitialDirectory: String? = null
    private var mListener = Option.none<OnFragmentInteractionListener?>()
    private var mBtnConfirm: Button? = null
    private var mBtnNavUp: ImageButton? = null
    private var mBtnCreateFolder: ImageButton? = null
    private var mTxtvSelectedFolder: TextView? = null
    private var mListDirectoriesAdapter: ArrayAdapter<String>? = null
    private var mFilenames: MutableList<String>? = null
    private var mSelectedDir: File? = null
    private var mFilesInDir: Array<File?>? = null
    private var mFileObserver: FileObserver? = null
    private var mConfig: DirectoryChooserConfig? = null
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mSelectedDir != null)
            outState.putString(KEY_CURRENT_DIRECTORY, mSelectedDir!!.absolutePath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireNotNull(arguments) { "You must create DirectoryChooserFragment via newInstance()." }
        mConfig = arguments!!.getParcelable(ARG_CONFIG)
        if (mConfig == null) throw NullPointerException("No ARG_CONFIG provided for DirectoryChooserFragment creation.")
        mNewDirectoryName = mConfig!!.newDirectoryName()
        mInitialDirectory = mConfig!!.initialDirectory()
        if (savedInstanceState != null)
            mInitialDirectory = savedInstanceState.getString(KEY_CURRENT_DIRECTORY)
        if (showsDialog) setStyle(STYLE_NO_TITLE, 0) else setHasOptionsMenu(true)
        require(
            !(!mConfig!!.allowNewDirectoryNameModification() && TextUtils.isEmpty(mNewDirectoryName))
        ) {
            "New directory name must have a strictly positive " +
                    "length (not zero) when user is not allowed to modify it."
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.directory_chooser, container, false)
        mBtnConfirm = view.findViewById(R.id.btnConfirm)
        val mBtnCancel = view.findViewById<Button>(R.id.btnCancel)
        mBtnNavUp = view.findViewById(R.id.btnNavUp)
        mBtnCreateFolder = view.findViewById(R.id.btnCreateFolder)
        mTxtvSelectedFolder = view.findViewById(R.id.txtvSelectedFolder)
        val mListDirectories = view.findViewById<ListView>(R.id.directoryList)
        mBtnConfirm?.setOnClickListener {
            if (isValidFile(mSelectedDir)) returnSelectedFolder()
        }
        mBtnCancel.setOnClickListener { mListener!!.foreach { obj: OnFragmentInteractionListener? -> obj!!.onCancelChooser() } }
        mListDirectories.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                debug("Selected index: %d", position)
                if (mFilesInDir != null && position >= 0 && position < mFilesInDir!!.size)
                    changeDirectory(mFilesInDir!![position])
            }
        mBtnNavUp?.setOnClickListener {
            var parent: File? = null
            if (mSelectedDir != null && mSelectedDir!!.parentFile.also { parent = it } != null)
                changeDirectory(parent)
        }
        mBtnCreateFolder?.setOnClickListener { openNewFolderDialog() }
        if (!showsDialog) mBtnCreateFolder?.visibility = View.GONE
        adjustResourceLightness()
        mFilenames = ArrayList()
        mListDirectoriesAdapter =
            ArrayAdapter(activity!!, android.R.layout.simple_list_item_1, mFilenames!!)
        mListDirectories.adapter = mListDirectoriesAdapter
        changeDirectory(if (!TextUtils.isEmpty(mInitialDirectory) && isValidFile(File(mInitialDirectory!!)))
            File(mInitialDirectory!!) else Environment.getExternalStorageDirectory())
        return view
    }

    private fun adjustResourceLightness() {
        var color = 0xFFFFFF
        val theme = activity?.theme
        if (theme != null) {
            val backgroundAttributes =
                theme.obtainStyledAttributes(intArrayOf(android.R.attr.colorBackground))
            color = backgroundAttributes.getColor(0, 0xFFFFFF)
            backgroundAttributes.recycle()
        }

        // convert to greyscale and check if < 128
        if (color != 0xFFFFFF && 0.21 * Color.red(color) + 0.72 * Color.green(color) + 0.07 *
            Color.blue(color) < 128
        ) {
            mBtnNavUp!!.setImageResource(R.drawable.navigation_up_light)
            mBtnCreateFolder!!.setImageResource(R.drawable.ic_action_create_light)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener)
            mListener = Option.some(context as OnFragmentInteractionListener)
        else if (targetFragment is OnFragmentInteractionListener)
            mListener = Option.some(targetFragment as OnFragmentInteractionListener?)
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onPause() {
        super.onPause()
        if (mFileObserver != null) mFileObserver!!.stopWatching()
    }

    override fun onResume() {
        super.onResume()
        if (mFileObserver != null) mFileObserver!!.startWatching()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.directory_chooser, menu)
        val menuItem = menu.findItem(R.id.new_folder_item) ?: return
        menuItem.isVisible = isValidFile(mSelectedDir) && mNewDirectoryName != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.new_folder_item) {
            openNewFolderDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openNewFolderDialog() {
        val dialogView = activity?.layoutInflater?.inflate(R.layout.dialog_new_folder, null)
        val msgView = dialogView?.findViewById<TextView>(R.id.msgText)
        val editText = dialogView?.findViewById<EditText>(R.id.editText)
        editText?.setText(mNewDirectoryName)
        msgView?.text = getString(R.string.create_folder_msg, mNewDirectoryName)
        val alertDialog = AlertDialog.Builder(activity!!)
            .setTitle(R.string.create_folder_label)
            .setView(dialogView)
            .setNegativeButton(R.string.cancel_label) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .setPositiveButton(R.string.confirm_label) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                mNewDirectoryName = editText?.text.toString()
                val msg = createFolder()
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
            }.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
            editText?.text?.isNotEmpty() ?: false
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
                val textNotEmpty = charSequence.isNotEmpty()
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = textNotEmpty
                msgView?.text = getString(R.string.create_folder_msg, charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        editText?.visibility =
            if (mConfig!!.allowNewDirectoryNameModification()) View.VISIBLE else View.GONE
    }

    private fun changeDirectory(dir: File?) {
        if (dir == null) debug("Could not change folder: dir was null")
        else if (!dir.isDirectory) debug("Could not change folder: dir is no directory")
        else {
            val contents = dir.listFiles()
            if (contents != null) {
                var numDirectories = 0
                for (f in contents) if (f.isDirectory) numDirectories++
                mFilesInDir = arrayOfNulls(numDirectories)
                mFilenames!!.clear()
                var i = 0
                var counter = 0
                while (i < numDirectories) {
                    if (contents[counter].isDirectory) {
                        mFilesInDir!![i] = contents[counter]
                        mFilenames!!.add(contents[counter].name)
                        i++
                    }
                    counter++
                }
                try {
                    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                    Arrays.sort(mFilesInDir)
                } catch (ignored: Exception) {
                }
                mFilenames?.sort()
                mSelectedDir = dir
                mTxtvSelectedFolder!!.text = dir.absolutePath
                mListDirectoriesAdapter!!.notifyDataSetChanged()
                mFileObserver = createFileObserver(dir.absolutePath)
                mFileObserver!!.startWatching()
                debug("Changed directory to %s", dir.absolutePath)
            } else debug("Could not change folder: contents of dir were null")
        }
        refreshButtonState()
    }

    private fun refreshButtonState() {
        if (activity != null && mSelectedDir != null) {
            mBtnConfirm!!.isEnabled = isValidFile(mSelectedDir)
            activity!!.invalidateOptionsMenu()
        }
    }

    private fun refreshDirectory() {
        if (mSelectedDir != null) changeDirectory(mSelectedDir)
    }

    private fun createFileObserver(path: String) =
        object : FileObserver(File(path), CREATE or DELETE or MOVED_FROM or MOVED_TO) {
            override fun onEvent(event: Int, path: String?) {
                debug("FileObserver received event %d", event)
                activity?.runOnUiThread { refreshDirectory() }
            }
        }

    private fun returnSelectedFolder() {
        if (mSelectedDir != null) {
            debug("Returning %s as result", mSelectedDir!!.absolutePath)
            mListener!!.foreach { f: OnFragmentInteractionListener? ->
                f!!.onSelectDirectory(mSelectedDir!!.absolutePath)
            }
        } else mListener!!.foreach { obj: OnFragmentInteractionListener? -> obj!!.onCancelChooser() }
    }

    private fun createFolder(): Int {
        return if (mNewDirectoryName != null && mSelectedDir != null && mSelectedDir!!.canWrite()) {
            val newDir = File(mSelectedDir, mNewDirectoryName!!)
            if (newDir.exists()) R.string.create_folder_error_already_exists else {
                val result = newDir.mkdir()
                if (result) R.string.create_folder_success else R.string.create_folder_error
            }
        } else if (mSelectedDir != null && !mSelectedDir!!.canWrite()) R.string.create_folder_error_no_write_access else R.string.create_folder_error
    }

    private fun isValidFile(file: File?) =
        file != null && file.isDirectory && file.canRead() && (mConfig!!.allowReadOnlyDirectory() || file.canWrite())

    var directoryChooserListener: OnFragmentInteractionListener?
        get() = mListener!!.get()
        set(listener) {
            mListener = Option.option(listener)
        }

    interface OnFragmentInteractionListener {
        fun onSelectDirectory(path: String)
        fun onCancelChooser()
    }

    companion object {
        const val KEY_CURRENT_DIRECTORY = "CURRENT_DIRECTORY"
        private const val ARG_CONFIG = "CONFIG"
        private val TAG = DirectoryChooserFragment::class.java.simpleName

        fun newInstance(config: DirectoryChooserConfig): DirectoryChooserFragment {
            val fragment = DirectoryChooserFragment()
            val args = Bundle()
            args.putParcelable(ARG_CONFIG, config)
            fragment.arguments = args
            return fragment
        }

        private fun debug(message: String, vararg args: Any) {
            Log.d(TAG, String.format(message, *args))
        }
    }
}

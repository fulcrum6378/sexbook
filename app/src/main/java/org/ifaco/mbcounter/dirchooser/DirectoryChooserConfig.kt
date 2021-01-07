package org.ifaco.mbcounter.dirchooser

import android.os.Parcelable
import auto.parcel.AutoParcel

@AutoParcel
abstract class DirectoryChooserConfig : Parcelable {
    abstract fun newDirectoryName(): String?
    abstract fun initialDirectory(): String?
    abstract fun allowReadOnlyDirectory(): Boolean
    abstract fun allowNewDirectoryNameModification(): Boolean

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun newDirectoryName(s: String?): Builder?
        abstract fun initialDirectory(s: String?): Builder?
        abstract fun allowReadOnlyDirectory(b: Boolean): Builder?
        abstract fun allowNewDirectoryNameModification(b: Boolean): Builder?
        abstract fun build(): DirectoryChooserConfig?
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = AutoParcel_DirectoryChooserConfig.Builder()
            .initialDirectory("")!!
            .allowNewDirectoryNameModification(false)!!
            .allowReadOnlyDirectory(false)!!
    }
}

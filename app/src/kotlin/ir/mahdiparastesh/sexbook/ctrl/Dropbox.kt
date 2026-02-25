package ir.mahdiparastesh.sexbook.ctrl

import android.app.Activity
import androidx.core.content.edit
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.page.Settings
import java.io.File
import java.io.FileInputStream

/**
 * Wrapper for the Dropbox Java API which can log in to Dropbox and upload files.
 *
 * @see <a href="https://github.com/dropbox/dropbox-sdk-java/">dropbox-sdk-java</a>
 */
@Suppress("RedundantSuspendModifier")
class Dropbox(private val c: Sexbook, private val exporter: Exporter) {

    private val appKey = "6oakyikndodzqpz"
    private var awaitingLogin: (() -> Unit)? = null


    fun requestConfig() = DbxRequestConfig("db-$appKey")

    fun login(c: Activity, listener: () -> Unit) {
        Auth.startOAuth2PKCE(
            c, appKey, requestConfig(), listOf("files.content.write")
        )
        awaitingLogin = listener
    }

    fun onResume() {
        if (awaitingLogin == null) return
        Auth.getDbxCredential()?.also { credential ->
            c.sp.edit {
                putString(
                    Settings.spDropboxCredential,
                    DbxCredential.Writer.writeToString(credential)
                )
            }
        }
        awaitingLogin!!()
        awaitingLogin = null
    }

    fun credential(): DbxCredential? = try {
        c.sp.getString(Settings.spDropboxCredential, null)
            ?.let { DbxCredential.Reader.readFully(it) }
    } catch (_: Exception) {
        removeCredential()
        null
    }

    fun isAuthenticated() = credential() != null

    fun removeCredential() {
        c.sp.edit { remove(Settings.spDropboxCredential) }
    }

    fun client() = DbxClientV2(requestConfig(), credential())

    /** @return true if the backup was successful */
    suspend fun backup(): Boolean {
        if (!isAuthenticated()) return false
        if (exporter.export(false)) return false
        val fis = FileInputStream(File(c.cacheDir, exporter.exportName))
        val success = try {
            client()
                .files()
                .uploadBuilder("/" + exporter.exportName)
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(fis)
            true
        } catch (_: DbxException) {  // includes when no network is available
            false
        }
        fis.close()
        return success
    }

    suspend fun logout() {
        if (!isAuthenticated()) return
        try {
            client().auth().tokenRevoke()
        } catch (_: DbxException) {
        }
        removeCredential()
    }
}

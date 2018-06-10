package work.airz.primanager

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore

interface SaveTicket {
    fun getStoredData()
    fun saveData()
}

class SavePhoto {
    companion object {
        val CAMERA_CAPTURE = 1
        val CROP_PIC = 2

        fun getCaptureIntent(temp: Uri, context: Context): Intent {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra("return-data", true)
                putExtra(MediaStore.EXTRA_OUTPUT, temp)
            }
            grantUriPermission(temp, intent, context)
            return intent
        }

        fun getCropIntent(temp: Uri, context: Context): Intent {
            val intent = Intent("com.android.camera.action.CROP").apply {
                setDataAndType(temp, "image/*")
                putExtra("crop", "true")
                putExtra("aspectX", 1)
                putExtra("aspectY", 1)
                putExtra("outputX", 512)
                putExtra("outputY", 512)
                putExtra("return-data", true)
                putExtra(MediaStore.EXTRA_OUTPUT, temp)
            }
            grantUriPermission(temp, intent, context)
            return intent
        }

        private fun grantUriPermission(temp: Uri, intent: Intent, context: Context) {
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).forEach {
                context.grantUriPermission(it.activityInfo.packageName, temp, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }
    }
}
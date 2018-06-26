package work.airz.primanager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore

class TicketUtils {
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

            fun getCropIntent(temp: Uri, context: Context, aspectx: Int = 1, aspecty: Int = 1, sizex: Int = 512, sizey: Int = 512): Intent {
                val intent = Intent("com.android.camera.action.CROP").apply {
                    setDataAndType(temp, "image/*")
                    putExtra("crop", "true")
                    putExtra("aspectX", aspectx)
                    putExtra("aspectY", aspecty)
                    putExtra("outputX", sizex)
                    putExtra("outputY", sizey)
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

    class TicketItemFormat(val title: String, val description: String, val thumbnail: Bitmap, val raw: String)
    class TicketOutlineFormat(val title: String, val description: String, val raw: String)
}
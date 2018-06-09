package work.airz.primanager

interface SaveQR {
    fun getStoredData()
    fun saveData()
}

class SaveConstants {
   companion object {
       val CAMERA_CAPTURE = 1
       val CROP_PIC = 2
   }
}
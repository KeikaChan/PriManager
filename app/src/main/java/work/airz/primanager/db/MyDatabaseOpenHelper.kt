package work.airz.primanager.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class MyDatabaseOpenHelper(context: Context) : ManagedSQLiteOpenHelper(context, "pri.db", null, 1) {
    val FOLLOW_TICKET_TABLE = "follow"
    val COORD_TICKET_TABLE = "coordinate"

    companion object {
        private var instance: MyDatabaseOpenHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): MyDatabaseOpenHelper {
            if (instance == null) {
                instance = MyDatabaseOpenHelper(ctx.getApplicationContext())
            }
            return instance!!
        }
    }

    override fun onCreate(p0: SQLiteDatabase) {
        p0.createTable(COORD_TICKET_TABLE, true,
                "raw" to TEXT + PRIMARY_KEY,
                "coord_id" to TEXT,
                "coord_name" to TEXT,
                "rarity" to TEXT,
                "brand" to TEXT,
                "color" to TEXT,
                "arcade_series" to TEXT,
                "date" to TEXT,
                "which_account" to TEXT,
                "image" to BLOB,
                "memo" to TEXT)
        p0.createTable(FOLLOW_TICKET_TABLE, true,
                "raw" to TEXT + PRIMARY_KEY,
                "user_id" to TEXT,
                "name" to TEXT,
                "date" to TEXT,
                "follow" to INTEGER,
                "follower" to INTEGER,
                "coordinate" to TEXT,
                "arcade_series" to TEXT,
                "which_account" to TEXT,
                "image" to BLOB,
                "memo" to TEXT)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val Context.database: MyDatabaseOpenHelper
        get() = MyDatabaseOpenHelper.getInstance(applicationContext)
}
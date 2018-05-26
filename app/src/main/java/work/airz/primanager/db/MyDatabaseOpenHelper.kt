package work.airz.primanager.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class MyDatabaseOpenHelper(context: Context) : ManagedSQLiteOpenHelper(context, "pri.db", null, 1) {
    val FOLLOW_TICKET_TABLE = "Follow"
    val CODE_TICKET_TABLE = "Code"

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
        p0.createTable(CODE_TICKET_TABLE, true,
                "raw" to TEXT + PRIMARY_KEY + UNIQUE,
                "code_id" to TEXT,
                "date" to TEXT,
                "image" to BLOB,
                "type" to TEXT,
                "brand" to TEXT,
                "rarity" to TEXT,
                "memo" to TEXT)
        p0.createTable(FOLLOW_TICKET_TABLE, true,
                "raw" to TEXT + PRIMARY_KEY + UNIQUE,
                "user_id" to TEXT,
                "image" to BLOB,
                "date" to TEXT,
                "memo" to TEXT)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val Context.database: MyDatabaseOpenHelper
        get() = MyDatabaseOpenHelper.getInstance(applicationContext)
}
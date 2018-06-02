package work.airz.primanager.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class MyDatabaseOpenHelper(context: Context) : ManagedSQLiteOpenHelper(context, "pri.db", null, 1) {


    companion object {
        private var instance: MyDatabaseOpenHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): MyDatabaseOpenHelper {
            if (instance == null) {
                instance = MyDatabaseOpenHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(p0: SQLiteDatabase) {
        p0.createTable(DBConstants.COORD_TICKET_TABLE, true,
                DBConstants.RAW to TEXT + PRIMARY_KEY,
                DBConstants.COORD_ID to TEXT,
                DBConstants.COORD_NAME to TEXT,
                DBConstants.RARITY to TEXT,
                DBConstants.BRAND to TEXT,
                DBConstants.COLOR to TEXT,
                DBConstants.ARCADE_SERIES to TEXT,
                DBConstants.DATE to TEXT,
                DBConstants.WHICH_ACCOUNT to TEXT,
                DBConstants.IMAGE to BLOB,
                DBConstants.MEMO to TEXT)
        p0.createTable(DBConstants.FOLLOW_TICKET_TABLE, true,
                DBConstants.RAW to TEXT + PRIMARY_KEY,
                DBConstants.USER_ID to TEXT,
                DBConstants.USER_NAME to TEXT,
                DBConstants.DATE to TEXT,
                DBConstants.FOLLOW to INTEGER,
                DBConstants.FOLLOWER to INTEGER,
                DBConstants.COORDINATE to TEXT,
                DBConstants.ARCADE_SERIES to TEXT,
                DBConstants.IMAGE to BLOB,
                DBConstants.MEMO to TEXT)

        p0.createTable(DBConstants.USER_TABLE, true,
                DBConstants.RAW to TEXT + PRIMARY_KEY,
                DBConstants.USER_NAME to TEXT,
                DBConstants.USER_CARD_ID to TEXT,
                DBConstants.WHICH_ACCOUNT to TEXT)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
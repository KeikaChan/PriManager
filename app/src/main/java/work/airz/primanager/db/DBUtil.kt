package work.airz.primanager.db

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.jetbrains.anko.db.*
import java.io.*
import java.util.*

class DBUtil(private val context: Context) {
    private val database: MyDatabaseOpenHelper
        get() = MyDatabaseOpenHelper.getInstance(context)

    /**
     * フォローデータのリスト取得用
     * @return フォローチケットのリスト
     */
    fun getFollowTicketList(): List<FollowTicket> {
        return database.use {
            select(DBConstants.FOLLOW_TICKET_TABLE).exec {
                parseList(rowParser { raw: String, userId: String, userName: String, date: String, follow: Int, follower: Int, coordinate: String, arcade_series: String, image: ByteArray, memo: String ->
                    FollowTicket(raw, userId, userName, date, follow, follower, coordinate, arcade_series, byteArrayToBitmap(image), memo)
                })
            }
        }
    }

    /**
     * フォロチケデータ追加
     * 更新も同様にできる
     */
    fun putFollowTicketData(followTicket: FollowTicket) {
        database.use {
            replace(DBConstants.FOLLOW_TICKET_TABLE,
                    DBConstants.RAW to followTicket.raw,
                    DBConstants.USER_ID to followTicket.userId,
                    DBConstants.USER_NAME to followTicket.userName,
                    DBConstants.DATE to followTicket.date,
                    DBConstants.FOLLOW to followTicket.follow,
                    DBConstants.FOLLOWER to followTicket.follower,
                    DBConstants.COORDINATE to followTicket.coordinate,
                    DBConstants.ARCADE_SERIES to followTicket.arcade_series,
                    DBConstants.IMAGE to bitmapToByteArray(followTicket.image),
                    DBConstants.MEMO to followTicket.memo)
        }
    }

    fun addUser(user: User) {
        checkEnpty(user.raw, user.followTableName)

        database.use {
            replace(DBConstants.USER_TABLE,
                    DBConstants.RAW to user.raw,
                    DBConstants.USER_NAME to user.userName,
                    DBConstants.USER_CARD_ID to user.userCardId,
                    DBConstants.FOLLOWS_TABLE_NAME to user.followTableName)

            //動的にフォローユーザのテーブルを作る
            createTable(user.followTableName, true,
                    DBConstants.USER_ID to TEXT + PRIMARY_KEY,
                    DBConstants.USER_NAME to TEXT,
                    DBConstants.DATE to TEXT,
                    DBConstants.MEMO to TEXT)
        }
    }

    private fun countUsers(): Int {
        return database.use {
            select(DBConstants.USER_TABLE).column("count(${DBConstants.RAW})").exec {
                parseSingle(rowParser { count: Int ->
                    count
                })
            }
        }
    }

    /**
     * コーデチケットのリスト取得用
     * @return コーデチケットのリスト
     */
    fun getCoordTicketList(): List<CoordTicket> {
        return database.use {
            select(DBConstants.COORD_TICKET_TABLE).exec {
                parseList(rowParser { raw: String, coordId: String, coordName: String, rarity: String, brand: String, color: String, arcadeSeries: String, date: String, whichAccount: String, image: ByteArray, memo: String ->
                    CoordTicket(raw, coordId, coordName, rarity, brand, color, arcadeSeries, date, whichAccount, byteArrayToBitmap(image), memo)
                })
            }
        }
    }

    /**
     * コーデチケット追加
     * 更新も同様にできる
     */
    fun putCoordTicketData(coodTicket: CoordTicket) {
        database.use {
            replace(DBConstants.COORD_TICKET_TABLE,
                    DBConstants.RAW to coodTicket.raw,
                    DBConstants.COORD_ID to coodTicket.coordId,
                    DBConstants.COORD_NAME to coodTicket.coordName,
                    DBConstants.RARITY to coodTicket.rarity,
                    DBConstants.BRAND to coodTicket.brand,
                    DBConstants.COLOR to coodTicket.color,
                    DBConstants.ARCADE_SERIES to coodTicket.arcadeSeries,
                    DBConstants.DATE to coodTicket.date,
                    DBConstants.WHICH_ACCOUNT to coodTicket.whichAccount,
                    DBConstants.IMAGE to bitmapToByteArray(coodTicket.image),
                    DBConstants.MEMO to coodTicket.memo)
        }
    }


    /**
     * ユーザ一覧を返す
     */
    fun getUsers(): List<User> {
        return database.use {
            select(DBConstants.USER_TABLE).exec {
                parseList(rowParser { raw: String, userName: String, userCardId: String, follows: String ->
                    User(raw, userName, userCardId, follows)
                })
            }
        }
    }

    /**
     * ユーザデータを参照して対象の会員を既にフォローしているかチェックする
     * TODO:DBデータ変更後の修正
     */
    fun isFollowed(myUserRawData: String, targetUserId: String): Boolean {
        return database.use {
            select(DBConstants.USER_TABLE, DBConstants.RAW, DBConstants.WHICH_ACCOUNT).whereArgs("${DBConstants.RAW} = {arg}", "arg" to myUserRawData).exec {
                parseList(rowParser { _: String, whichAccount: String ->
                    whichAccount.split(",").any { it == targetUserId } //一回しか処理が通らないはず
                })
            }.first()
        }
    }

    /**
     * 対象のデータがすでにあるかの確認用
     * @return true:ある　false:ない
     */
    fun isDuplicate(table: String, primaryKeyData: String): Boolean {
        return database.use {
            select(table, DBConstants.RAW).whereArgs("${DBConstants.RAW} = {arg}", "arg" to primaryKeyData).exec {
                parseList(rowParser { _: String -> })
            }.isNotEmpty()
        }
    }


    /**
     * 画像をByteArrayへ変換
     * DB保存用
     * @param bitmap サムネイル画像
     * @return 変換済みデータ
     */
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
        return bos.toByteArray()
    }

    /**
     * ByteArray形式になっている画像データをBitmapに直す
     * DB読み込み用
     * @param byteArray 画像データ
     * @return 変換済みデータ
     */
    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    /**
     * 値の空白チェック用
     */
    private fun checkEnpty(vararg checkString: String): Boolean {
        if (checkString.any { it.isEmpty() }) throw IllegalArgumentException("should set values.")
        return true
    }


    class FollowTicket(
            val raw: String,
            val userId: String,
            val userName: String,
            val date: String,
            val follow: Int,
            val follower: Int,
            val coordinate: String,
            val arcade_series: String,
            val image: Bitmap,
            val memo: String)

    class CoordTicket(
            val raw: String,
            val coordId: String,
            val coordName: String,
            val rarity: String,
            val brand: String,
            val color: String,
            val arcadeSeries: String,
            val date: String,
            val whichAccount: String,
            val image: Bitmap,
            val memo: String)

    class User(
            val raw: String,
            val userName: String,
            val userCardId: String,
            val followTableName: String)

    class UserFollow(
            val userId: String,
            val userName: String,
            val date: String,
            val memo: String)
}
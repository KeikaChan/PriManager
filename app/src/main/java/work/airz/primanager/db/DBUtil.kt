package work.airz.primanager.db

import android.content.Context
import android.graphics.Bitmap
import org.jetbrains.anko.db.replace
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select

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
                parseList(rowParser { raw: String, userId: String, userName: String, date: String, follow: Int, follower: Int, coordinate: String, arcade_series: String, whichAccount: String, image: Bitmap, memo: String ->
                    FollowTicket(raw, userId, userName, date, follow, follower, coordinate, arcade_series, whichAccount, image, memo)
                })
            }
        }
    }

    /**
     * フォロチケデータ追加
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
                    DBConstants.IMAGE to followTicket.image,
                    DBConstants.MEMO to followTicket.memo)
        }
    }



    /**
     * コーデチケットのリスト取得用
     * @return コーデチケットのリスト
     */
    fun getCoordTicketList(): List<CoordTicket> {
        return database.use {
            select(DBConstants.COORD_TICKET_TABLE).exec {
                parseList(rowParser { raw: String, coordId: String, coordName: String, rarity: String, brand: String, color: String, arcadeSeries: String, date: String, whichAccount: String, image: Bitmap, memo: String ->
                    CoordTicket(raw, coordId, coordName, rarity, brand, color, arcadeSeries, date, whichAccount, image, memo)
                })
            }
        }
    }

    /**
     * コーデチケット追加
     */
    fun putCoordTicketData(coodTicket: CoordTicket){
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
                    DBConstants.IMAGE to coodTicket.image,
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
     */
    fun isFollowed(myUserRawData: String, targetUserId: String): Boolean {
        return database.use {
            select(DBConstants.USER_TABLE, DBConstants.RAW, DBConstants.WHICH_ACCOUNT).whereArgs("${DBConstants.RAW} = ${myUserRawData}").exec {
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
            select(table, DBConstants.RAW).whereArgs("${DBConstants.RAW} = ${primaryKeyData}").exec {
                parseList(rowParser { _: String -> })
            }.isNotEmpty()
        }
    }


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

    class FollowTicket(
            val raw: String,
            val userId: String,
            val userName: String,
            val date: String,
            val follow: Int,
            val follower: Int,
            val coordinate: String,
            val arcade_series: String,
            val whichAccount: String,
            val image: Bitmap,
            val memo: String)

    class User(
            val raw: String,
            val userName: String,
            val userCardId: String,
            val follows: String)
}
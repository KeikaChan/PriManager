package work.airz.primanager.db

import android.content.Context
import android.graphics.Bitmap
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
    fun getFollowList(): List<FollowTicket> {
        return database.use {
            select(DBConstants.FOLLOW_TICKET_TABLE).exec {
                parseList(rowParser { raw: String, user_id: String, name: String, date: String, follow: Int, follower: Int, coordinate: String, arcade_series: String, which_account: String, image: Bitmap, memo: String ->
                    FollowTicket(raw, user_id, name, date, follow, follower, coordinate, arcade_series, which_account, image, memo)
                })
            }
        }
    }

    /**
     * コーデチケットのリスト取得用
     * @return コーデチケットのリスト
     */
    fun getCoordList(): List<CoordTicket> {
        return database.use {
            select(DBConstants.COORD_TICKET_TABLE).exec {
                parseList(rowParser { raw: String, codeID: String, codeName: String, rarity: String, brand: String, color: String, arcadeSeries: String, date: String, whichAccount: String, image: Bitmap, memo: String ->
                    CoordTicket(raw, codeID, codeName, rarity, brand, color, arcadeSeries, date, whichAccount, image, memo)
                })
            }
        }
    }

    /**
     * ユーザ一覧を返す
     */
    fun getUsers(): List<User> {
        return database.use {
            select(DBConstants.USER_TABLE).exec {
                parseList(rowParser { raw: String, user_name: String, user_card_id: String, follows: String ->
                    User(raw, user_name, user_card_id, follows)
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
            val codeID: String,
            val codeName: String,
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
            val user_id: String,
            val name: String,
            val date: String,
            val follow: Int,
            val follower: Int,
            val coordinate: String,
            val arcade_series: String,
            val which_account: String,
            val image: Bitmap,
            val memo: String)

    class User(
            val raw: String,
            val user_name: String,
            val user_card_id: String,
            val follows: String)
}
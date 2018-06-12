package work.airz.primanager.db

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.jetbrains.anko.db.*
import work.airz.primanager.db.DBFormat.*
import work.airz.primanager.qr.QRUtil
import java.io.*
import kotlin.math.absoluteValue

/**
 * TODO: 関数の整理
 * TODO: 会員証の作り直しでQRのみ変わる場合の関数を作る
 */
class DBUtil(private val context: Context) {
    private val database: MyDatabaseOpenHelper
        get() = MyDatabaseOpenHelper.getInstance(context)

    companion object {
        private lateinit var userList: List<User> //毎回アクセスすると時間がかかるのでインスタンス化したときにRAM上に保持する
    }

    init {
        userList = getUsers()
    }

    /**
     * ユーザリストの取得のみで他クラスで書き換え不可にする
     */
    fun getUserList(): List<User> {
        return userList.toList()
    }

    /**
     * 特定のユーザデータを取得する
     */
    fun getUser(rawData: String): User? {
        val search = hashMapOf<String, User>()
        userList.forEach { search[it.raw] = it }
        return search[rawData]
    }


    /**
     * フォローデータ取得用
     * @return フォローチケット
     */
    fun getFollowTicket(rawData: String): FollowTicket {
        return database.use {
            select(DBConstants.FOLLOW_TICKET_TABLE).whereArgs("${DBConstants.RAW} = {arg}", "arg" to rawData).exec {
                parseSingle(rowParser { raw: String, qrFormat: String, userId: String, userName: String, date: String, follow: Int, follower: Int, coordinate: String, arcade_series: String, image: ByteArray, memo: String ->
                    FollowTicket(raw, QRUtil.QRFormat.parseString(qrFormat), userId, userName, date, follow, follower, coordinate, arcade_series, byteArrayToBitmap(image), memo)
                })
            }
        }
    }

    /**
     * フォローデータのリスト取得用
     * @return フォローチケットのリスト
     */
    fun getFollowTicketList(): List<FollowTicket> {
        return database.use {
            select(DBConstants.FOLLOW_TICKET_TABLE).exec {
                parseList(rowParser { raw: String, qrFormat: String, userId: String, userName: String, date: String, follow: Int, follower: Int, coordinate: String, arcade_series: String, image: ByteArray, memo: String ->
                    FollowTicket(raw, QRUtil.QRFormat.parseString(qrFormat), userId, userName, date, follow, follower, coordinate, arcade_series, byteArrayToBitmap(image), memo)
                })
            }
        }
    }

    /**
     * フォロチケデータ追加
     * 更新も同様にできる
     */
    fun addFollowTicketData(followTicket: FollowTicket) {
        database.use {
            replace(DBConstants.FOLLOW_TICKET_TABLE,
                    DBConstants.RAW to followTicket.raw,
                    DBConstants.QR_FOMAT to followTicket.qrFormat.toString(),
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

    /**
     * フォロチケデータの削除
     * @param followTicket 削除するフォロチケ
     */
    fun removeFollowTicketData(followTicket: FollowTicket) {
        database.use {
            delete(DBConstants.FOLLOW_TICKET_TABLE, "${DBConstants.RAW} = {arg}", "arg" to followTicket.raw)
        }
    }

    /**
     * ユーザの追加
     * @param user 追加するユーザ
     */
    fun addUser(user: User) {
        checkEnpty(user.raw, user.followTableName)

        database.use {
            replace(DBConstants.USER_TABLE,
                    DBConstants.RAW to user.raw,
                    DBConstants.QR_FOMAT to user.qrFormat.toString(),
                    DBConstants.USER_NAME to user.userName,
                    DBConstants.USER_CARD_ID to user.userCardId,
                    DBConstants.IMAGE to bitmapToByteArray(user.image),
                    DBConstants.DATE to user.date,
                    DBConstants.MEMO to user.memo,
                    DBConstants.FOLLOWS_TABLE_NAME to user.followTableName)

            //動的にフォローユーザのテーブルを作る
            //TODO: テーブル名衝突時の処理を入れる
            createTable(user.followTableName, true,
                    DBConstants.USER_ID to TEXT + PRIMARY_KEY,
                    DBConstants.USER_NAME to TEXT,
                    DBConstants.DATE to TEXT,
                    DBConstants.MEMO to TEXT)
            updateUsers() // thread safe
        }
    }

    /**
     * ユーザ削除
     * @param user 削除するユーザ
     */
    fun removeUser(user: User) {
        database.use {
            delete(DBConstants.USER_TABLE, "${DBConstants.RAW} = {arg}", "arg" to user.raw)
            delete(user.followTableName)
            updateUsers() // thread safe
        }
    }

    /**
     * コーデチケット取得用
     * @return コーデチケット
     */
    fun getCoordTicket(rawData: String): CoordTicket {
        return database.use {
            select(DBConstants.COORD_TICKET_TABLE).whereArgs("${DBConstants.RAW} = {arg}", "arg" to rawData).exec {
                parseSingle(rowParser { raw: String, qrFormat: String, coordId: String, coordName: String, rarity: String, brand: String, color: String, category: String, genre: String, like: Int, arcadeSeries: String, date: String, image: ByteArray, memo: String ->
                    CoordTicket(raw, QRUtil.QRFormat.parseString(qrFormat), coordId, coordName, rarity, brand, color, category, genre, like, arcadeSeries, date, byteArrayToBitmap(image), memo)
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
                parseList(rowParser { raw: String, qrFormat: String, coordId: String, coordName: String, rarity: String, brand: String, color: String, category: String, genre: String, like: Int, arcadeSeries: String, date: String, image: ByteArray, memo: String ->
                    CoordTicket(raw, QRUtil.QRFormat.parseString(qrFormat), coordId, coordName, rarity, brand, color, category, genre, like, arcadeSeries, date, byteArrayToBitmap(image), memo)
                })
            }
        }
    }

    /**
     * コーデチケット追加
     * 更新も同様にできる
     */
    fun addCoordTicketData(coodTicket: CoordTicket) {
        database.use {
            replace(DBConstants.COORD_TICKET_TABLE,
                    DBConstants.RAW to coodTicket.raw,
                    DBConstants.QR_FOMAT to coodTicket.qrFormat.toString(),
                    DBConstants.COORD_ID to coodTicket.coordId,
                    DBConstants.COORD_NAME to coodTicket.coordName,
                    DBConstants.RARITY to coodTicket.rarity,
                    DBConstants.BRAND to coodTicket.brand,
                    DBConstants.COLOR to coodTicket.color,
                    DBConstants.CATEGORY to coodTicket.category,
                    DBConstants.GENRE to coodTicket.genre,
                    DBConstants.LIKE to coodTicket.like,
                    DBConstants.ARCADE_SERIES to coodTicket.arcadeSeries,
                    DBConstants.DATE to coodTicket.date,
                    DBConstants.IMAGE to bitmapToByteArray(coodTicket.image),
                    DBConstants.MEMO to coodTicket.memo)
        }
    }

    /**
     * コーデデータの削除
     */
    fun removeCoordTicketData(coordTicket: CoordTicket) {
        database.use {
            delete(DBConstants.COORD_TICKET_TABLE, "${DBConstants.RAW} = {arg}", "arg" to coordTicket.raw)
        }
    }

    /**
     * テーブルが存在するかの確認
     */
    fun isTableExists(tableName: String): Boolean {
        return database.use {
            select("sqlite_master", "name")
                    .whereArgs("type = {argtype} AND name = {argname}", "argtype" to "table", "argname" to tableName).exec {
                        parseList(rowParser { _: String ->
                            true
                        })
                    }
        }.isNotEmpty()
    }

    /**
     * 指定したユーザがフォローしているデータの一覧を返してくれる
     */
    fun getUserFollowList(rawData: String): List<UserFollow> {
        return database.use {
            select(getUserTableName(rawData)).exec {
                parseList(rowParser { userId: String, userName: String, date: String, memo: String ->
                    UserFollow(userId, userName, date, memo)
                })
            }
        }
    }

    /**
     * ユーザのフォロー用
     * ユーザの更新も同時にします
     * @param my フォローするユーザのデータ
     * @param target フォローするユーザデータ
     */
    fun followUser(my: User, target: UserFollow) {
        database.use {
            replace(my.followTableName,
                    DBConstants.USER_ID to target.userId,
                    DBConstants.USER_NAME to target.userName,
                    DBConstants.DATE to target.date,
                    DBConstants.MEMO to target.memo)
        }
    }

    /**
     * ユーザデータを参照して対象の会員を既にフォローしているかチェックする
     * @param my ユーザデータ
     */
    fun isFollowed(my: User, targetUserId: String): Boolean {
        return database.use {
            select(my.followTableName, DBConstants.USER_ID).whereArgs("${DBConstants.USER_ID} = {arg}", "arg" to targetUserId).exec {
                parseList(rowParser { _: String ->
                    true
                })
            }.isNotEmpty()
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
     * テーブル名作成用。
     * ユーザのQR情報からハッシュ値を作成してそれをテーブル名にする
     * "-"文字が入る関係で正の数のみ
     */
    fun getUserHashString(userRawData: String): String {
        return userRawData.hashCode().absoluteValue.toString()
    }

    /**
     * ユーザのテーブル名を取得するやつ
     * ユーザ登録とかで色々使うかも
     */
    private fun getUserTableName(rawData: String): String {
        val tableName = database.use {
            select(DBConstants.USER_TABLE, DBConstants.RAW, DBConstants.FOLLOWS_TABLE_NAME)
                    .whereArgs("${DBConstants.RAW} = {arg}", "arg" to rawData).exec {
                        parseSingle(rowParser { _: String, tableName: String ->
                            tableName
                        })
                    }

        }
        if (tableName.isEmpty()) throw IllegalArgumentException("user not found!!")
        return tableName
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

    /**
     * ユーザ一覧を返す
     */
    private fun getUsers(): List<User> {
        return database.use {
            select(DBConstants.USER_TABLE).exec {
                parseList(rowParser { raw: String, qrFormat: String, userName: String, userCardId: String, image: ByteArray, date: String, memo: String, follows: String ->
                    User(raw, QRUtil.QRFormat.parseString(qrFormat), userName, userCardId, byteArrayToBitmap(image), date, memo, follows)
                })
            }
        }
    }

    /**
     * ユーザデータの更新
     */
    @Synchronized
    private fun updateUsers() {
        userList = getUsers()
    }
}
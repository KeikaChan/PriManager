package work.airz.primanager.db

class DBConstants {
    companion object {

        //table name
        const val FOLLOW_TICKET_TABLE = "follow"
        const val COORD_TICKET_TABLE = "coordinate"
        const val USER_TABLE = "user"

        //column name
        //共通
        const val RAW = "raw" //QRコードの生データ
        const val ARCADE_SERIES = "arcade_series" //どのアーケードシリーズか プリリズ/プリパラ/プリチャン
        const val DATE = "date" //日付
        const val IMAGE = "image" //サムネイル用
        const val MEMO = "memo" //メモ

        //コーデ用
        const val COORD_ID = "coord_id"  //コーデID PCH1-001 とか
        const val COORD_NAME = "coord_name" // コーデ名 "キラッとワンピ"とか
        const val RARITY = "rarity" //レア度 KR とか
        const val BRAND = "brand" //ブランド名 Sweet Honey とか
        const val COLOR = "color" //色　みどり とか
        const val CATEGORY ="category" //ワンピースとかシューズとかヘアアクセとか
        const val GENRE ="genre" //ラブリーとかポップとかクールとか
        const val LIKE="like" //いいね数

        //フォロチケ用
        const val USER_ID = "user_id" //ユーザID フォロチケの後ろにある固有値のこと
        const val USER_NAME = "user_name" //ユーザ名
        const val FOLLOW = "follow" //フォロー数
        const val FOLLOWER = "follower" //フォロワー数
        const val COORDINATE = "coordinate" // どんな格好をしているか。参考程度に 例：第一弾キラッとコーデフル

        //ユーザ用
        const val USER_CARD_ID = "user_card_id" //会員証に印刷されているID　フォロチケや会員証データとは別
        const val FOLLOWS_TABLE_NAME = "follow_table_name" // フォローしているユーザのIDの一覧、カンマ区切り

        const val PRICHAN = "プリチャン"
        const val OTHERS = "その他"
    }
}
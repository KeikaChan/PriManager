package work.airz.primanager.db

import android.graphics.Bitmap

class DBFormat {
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
            val category: String,
            val genre: String,
            val like: Int,
            val arcadeSeries: String,
            val date: String,
            val image: Bitmap,
            val memo: String)

    class User(
            val raw: String,
            val userName: String,
            val userCardId: String,
            val image: Bitmap,
            val date: String,
            val memo: String,
            val followTableName: String)

    class UserFollow(
            val userId: String,
            val userName: String,
            val date: String,
            val memo: String)

}
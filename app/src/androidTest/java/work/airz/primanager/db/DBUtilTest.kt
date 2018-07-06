package work.airz.primanager.db

import android.graphics.BitmapFactory
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.test.ActivityInstrumentationTestCase
import android.test.ActivityInstrumentationTestCase2
import android.test.ActivityTestCase
import android.test.InstrumentationTestCase
import android.util.Log
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import work.airz.primanager.MainActivity
import work.airz.primanager.R
import work.airz.primanager.qr.QRUtil

@RunWith(AndroidJUnit4::class)
class DBUtilTest {
    private val context = InstrumentationRegistry.getTargetContext()
    private val dbUtil = DBUtil(context)
    private val FOLLOW_RAW = "50A20342E8260CD6D985B8DF915FCF2EE54A70B41BA3F2753E8878AD2DDBB4CEAE423C4B6C113C39AA5D114EF918DFFA384486A42040DE7037858CEF4714ED7A35973C7323E252106426EAD7A6BDF67A98D4F96FBB38F208E126EAD7A6BDF67A98D4F96FBB38F208E1EF0D7E2D96A607225588583DFF344060FF"
    private val COORD_RAW = "50A203B105916A41903A42C4881887C64565F7E1C1574E3A8F59"
    private val QRFORMAT = QRUtil.QRFormat(ErrorCorrectionLevel.M, 1, false, 2)
    private val USER_ID = "EF0D7E2D96A607225588583DFF344060"
    private val USER_NAME = "えもちゃん"
    private val DATE_STRING = "2018/2/22"
    private val MEMO = "this is \nunit test"
    private val TEST_IMAGE = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)

    @Test
    fun addFollowTicketData() {
        val followTicket = DBFormat.FollowTicket(FOLLOW_RAW
                , QRFORMAT, USER_ID, USER_NAME, DATE_STRING, 1, 1, "GY-01のみ", "プリチャン", TEST_IMAGE, MEMO)
        dbUtil.addFollowTicketData(followTicket)
    }

    @Test
    fun addUser() {
        val userTicket = DBFormat.User(COORD_RAW,QRFORMAT,USER_NAME,"0000-ffff-1111-1234",TEST_IMAGE,DATE_STRING,MEMO,"follow_teable1")
        dbUtil.addUser(userTicket)
    }

    @Test
    fun removeUser() {
    }

    @Test
    fun addCoordTicketData() {
    }
}
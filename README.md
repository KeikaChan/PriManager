# [WIP] PriManager
Pri Ticket Manager.

Management Pri☆Chan Follow/Coorde ticket.

フォロワーの管理とコーデ管理を行うためのAndroidアプリを作っています。

## Building
### Environment
- android studio
- java
- maven (for building ZXing library)

### How to build
It need to build customized zxing/android-embedded library.

1. Pull [this zxing repo](https://github.com/Khromium/zxing) and create jar file using maven.  
`$ mvn package #in /zxing folder`

2. Pull [this zxing-android-embedded repo](https://github.com/Khromium/zxing-android-embedded) and move /zxing/core/target/core-\*.\*.\*-SNAPSHOT.jar to /zxing-android-embedded/zxing-android-embedded/libs/ 

3. Build and create aar file from zxing-android-embedded using Android Stuidio

4. Move aar file to PriManager/zxing-android-embedded/


Now PriManager can build normally.

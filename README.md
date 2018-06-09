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



## Support
プルリクエスト等も歓迎です。   
Contact me → [@keyka_p](https://twitter.com/keyka_p)  

Kyash ID: money   
Monacoin : [MUu78YZDtsqv7jo6EG55hjUnhgyCJuFgVE](https://monappy.jp/u/khrom)  

## License

Licensed under the [Apache License 2.0]

	Copyright (C) 2012-2018 ZXing authors, Journey Mobile, Khromium

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	    http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


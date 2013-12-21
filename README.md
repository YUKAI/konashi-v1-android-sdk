# konashi SDK for Android

----

### 進捗
- PIOのdigitalWriteでLED制御するところまでできた
- PIOの入力変化のnotificationを受信できた 12/18/2013

### 使い方
- KonashiLibをライブラリプロジェクトとして追加 
- KonashiManagerをnewする or Konashiシングルトン。

### 要検証
- konashi2台使う時、KonashiManager2つとかでいけるのかな。 多分いけると思うけど


----

### javadoc 生成
以下のantコマンドを実行すると、docsディレクトリにjavadocのhtmlが生成される。
```
$ ant javadoc
```

### 動作環境
- Android4.3以降 (SDK Version >= 18)
- Bluetooth Low Energy (Bluetooth4.0) 対応端末

### Getting started

#### まず konashi-android-sdk をダウンロード
```
$ git clone git@github.com:YUKAI/konashi-android-sdk.git
```

#### eclipse でライブラリのプロジェクトとしてインポート
`File -> Import`で Import ウィンドウを開く。<br/>
そして、`Android -> Existing Android Code into Workspace`を選択。<br/>
Import Projects ウィンドウが開くので、Root Directory にさきほどダウンロードした `konashi-android-sdk/Konashi`ディレクトリを指定して、Finishをクリック。

eclipse のプロジェクトに KonashiLib というライブラリプロジェクトが追加されているか確認してください。

##### サンプルプロジェクトを作成
eclipse でAndroidプロジェクトを作成してください。

#### konashi-android-sdk ライブラリをサンプルプロジェクトに追加
`Package Explorer`にて、サンプルプロジェクトを右クリックして`Properties`を選択。<br/>
Properties ウィンドウが開いたら左のプロパティリストの中にある`Android`という項目を選択。<br/>
Library の `Add` を押し、`KonashiLib`を選択。

これで konashi ライブラリを使えるようになりました。

#### BLEのパーミッション追加
Bluetooth を使えるように、AndroidManifest.xml に以下のパーミッションを追加。

```xml:AndroidManifest.xml
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
```

もし、BLE機能を持っているAndroidのみインストール可能にするには、AndroidManifest.xml に以下も追加。

```xml:AndroidManifest.xml
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
```

#### コードを書く
comming soon

#### 実機で動かす
comming soon

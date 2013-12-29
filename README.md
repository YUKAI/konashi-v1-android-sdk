# konashi SDK for Android

## 進捗
- ハードウェア系のfunction実装完了 12/29/2013
- AIO系のfunction実装完了 12/29/2013
- PIO, PWM系のfunction実装完了 12/23/2013
- PIOの入力変化のnotificationを受信できた 12/18/2013
- PIOのdigitalWriteでLED制御するところまでできた

## 使い方
- KonashiLibをライブラリプロジェクトとして追加 
- KonashiManagerをnewする or Konashiシングルトン。

## 要検証
- konashi2台使う時、KonashiManager2つとかでいけるのかな。 多分いけると思うけど


----

## ant使う系
ルートディレクトリにbuild.xmlがあります。

### ant使う前に
- Konashi ディレクトリにて以下のコマンドを実行して、local.propertiesを生成。これを元にしていろいろやります。

```
Konashi $ android update project -p .
```

### jar 生成
以下の ant コマンドを実行すると、libs ディレクトリに konashi-${versino}.jar が生成される。

```
$ ant jar
```

### javadoc 生成
以下の ant コマンドを実行すると、docs ディレクトリに javadoc の html が生成される。

```
$ ant javadoc
```


## 国内のBLE対応Androidに関して
### Android4.3以降が公式に提供されている端末
- Nexus 7(2013)
- Nexus 5
- GALAXY J
- GALAXY Note 3

### カスタムROMを焼くことで4.3に移行できるであろう端末
- GALAXY S III
- GALAXY S4
- GALAXY Note II
- Xperia Z
- Xperia Z1
- HTC J One
- HTC J butterfly
- LG Optimus G
- LG Optimus G Pro
- LG G2
- その他、海外でよく使われている端末(国産スマホ以外 & 日本向けにカスタマイズされたグローバル端末以外)であれば、Android4.3以降のカスタムROMが有志によって作られています

### キャリアのBluetoothのスペック(4.0,BLE or not)
#### au
- http://www.au.kddi.com/developer/android/kishu/bluetooth/

## 動作環境
- Android4.3以降 (SDK Version >= 18)
- Bluetooth Low Energy (Bluetooth4.0) 対応端末

## Getting started

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


## ライセンス
konashi のソフトウェアのソースコード、ハードウェアに関するドキュメント・ファイルのライセンスは以下です。

- ソフトウェア
  - konashi-ios-sdk のソースコードは [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) のもと公開されています。
- ハードウェア
  - konashi の回路図などハードウェア関連のドキュメント・ファイルのライセンスは [クリエイティブ・コモンズ・ライセンス「表示-継承 2.1 日本」](http://creativecommons.org/licenses/by-sa/2.1/jp/deed.ja)です。これに従う場合に限り、自由に複製、頒布、二次的著作物を作成することができます。
  - 回路図のデータ(eagleライブラリ)は3月上旬公開予定です。
- konashi のBLEモジュールのファームウェアは [csr社](http://www.csr.com/) とのNDAのため公開しておりません。

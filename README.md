# BiomePainter
Bukkit用バイオームコピペプラグイン


## 使い方
クリエイティブモードで矢を手に持って各操作
- ブロックを左クリック → バイオームを読み込む
- ブロックを右クリック → 読み込んだバイオームを適用
- スニーク状態でブロックを右クリック → バイオーム情報をチャットに表示
- スニーク状態でホイールぐりぐり → 視線の先のバイオームを順次切り替え

## コマンド
- `/bpaint` : 現在設定されてるバイオーム情報を表示(空中右クリックと同等)
- `/bpaint set <BiomeID or Name>` : ツールに指定したバイオームを設定
- `/bpaint give` : BiomePainter用のツールアイテムを取得
- `/bpaint list <page>` or `/bpaint biomes <page>` : バイオームの一覧を表示

## パーミッション
- `biomepainter.tool.give`: `/bpaint give`コマンドを使えるかどうか
- `biomepainter.tool.check`: バイオーム情報の表示操作を使えるかどうか
- `biomepainter.tool.pickup`: バイオーム情報の取得操作(左クリック)ができるかどうか
- `biomepainter.tool.paint`: バイオーム情報の書き換え操作(右クリック)とホイールぐりぐりができるかどうか
- `biomepainter.tool.list`: `/bpaint list`と`/bpaint biomes`コマンドを使えるかどうか

## 気づいてる不具合
- ホイールぐりぐり時に半ブロや階段とかの透過部分を視線が通過しない(Bukkit側で処理するから仕様)

## ダウンロード
ぎっはぶのリリースからどぞ
https://github.com/sasrai/bukkit_biomepainter/releases


## 動作確認環境
CraftBukkit version `git-Spigot-1.7.9-R0.2-208-ge0f2e95` (MC: 1.7.10) (Implementing API version 1.7.10-R0.1-SNAPSHOT)


## Build
`mvn package`


## License
MIT

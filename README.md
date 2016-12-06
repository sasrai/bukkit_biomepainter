# BiomePainter
Bukkit用バイオームコピペプラグイン


## 使い方
矢を手に持って各クリック
- ブロックを左クリック → バイオームを読み込む
- ブロックを右クリック → 読み込んだバイオームを適用
- スニーク状態でブロックを右クリック → バイオーム情報をチャットに表示
- スニーク状態でホイールぐりぐり → 視線の先のバイオームを順次切り替え


## 気づいてる不具合
- ホイールぐりぐり時に半ブロや階段とかの透過部分を視線が通過しない
- プラグインのreload時に読み込んだバイオーム情報が消える
- コマンド処理が未実装(コマンド自体は存在)


## Build
`mvn package`


## License
MIT

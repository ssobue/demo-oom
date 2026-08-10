# demo-oom

Java ヒープを意図的に使い切り、`java.lang.OutOfMemoryError` を発生させる Spring Boot のコマンドラインアプリケーションです。

## 警告

このアプリケーションは、利用可能なヒープをすべて消費します。

必ず、ヒープ上限を意図的に小さく設定した使い捨ての環境で実行してください。

本番環境、共有マシン、重要なデータを扱うプロセスでは OOM モードを実行するわけにはいきません。

実験を終えるときは `Ctrl-C` でプロセスを停止してください。

## 必要な環境

- JDK 25 以降
- リポジトリに含まれる Gradle Wrapper

## ビルド

```sh
./gradlew clean build
```

ビルドとテストは OOM モードを開始しないため、通常どおり完了します。

## OOM を発生させる

実行可能 JAR を作成してから、ヒープ上限を小さくして起動します。

```sh
java -Xmx64m -jar build/libs/demo-oom-0.0.1-SNAPSHOT.jar --oom
```

アプリケーションは、1 MiB の `byte[]` をループごとに作成します。

作成した配列をリストから参照し続けるため、ガベージコレクションは確保済みの配列を回収できません。

最終的に、次のようなエラーでプロセスが終了します。

```text
java.lang.OutOfMemoryError: Java heap space
```

エラーが発生するまでの確保量は、JDK、JVM オプション、起動時のほかのメモリ使用量によって変わります。

## オプション

| オプション | 既定値 | 説明 |
| --- | ---: | --- |
| `--oom` | 無効 | 意図的にヒープを使い切るループを有効にします。 |
| `--chunk-mb` | `1` | 1 回に保持する `byte[]` のサイズを MiB で指定します。 |
| `--report-every-mb` | `16` | MiB 単位の進捗表示間隔を指定します。 |

次の例では、4 MiB ずつ確保し、32 MiB ごとに進捗を表示します。

```sh
java -Xmx128m -jar build/libs/demo-oom-0.0.1-SNAPSHOT.jar \
  --oom --chunk-mb=4 --report-every-mb=32
```

`--oom` を付けない場合、アプリケーションはメッセージを表示して終了し、実験用のデータを確保しません。

```sh
java -jar build/libs/demo-oom-0.0.1-SNAPSHOT.jar
```

英語版は [README.md](README.md) です。

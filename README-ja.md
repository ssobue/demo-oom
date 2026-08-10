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
- 監視例には Docker Compose v2

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
| `--delay-ms` | `0` | 確保 1 回ごとの待機時間をミリ秒で指定します。 |

次の例では、4 MiB ずつ確保し、32 MiB ごとに進捗を表示します。

```sh
java -Xmx128m -jar build/libs/demo-oom-0.0.1-SNAPSHOT.jar \
  --oom --chunk-mb=4 --report-every-mb=32 --delay-ms=100
```

`--oom` を付けない場合、アプリケーションは HTTP サーバーを起動してメッセージを表示し、実験用のデータを確保せずに待機します。

```sh
java -jar build/libs/demo-oom-0.0.1-SNAPSHOT.jar
```

## Docker Compose で監視する

Docker Compose は、OOM デモと Prometheus を同じネットワーク上で起動します。

Compose の設定では、アプリケーションのヒープ上限を 256 MiB に設定し、確保ごとに 1 秒待機します。

これにより、アプリケーションが `OutOfMemoryError` に到達するまで数分間のサンプルを取得し、ヒープ増加を追跡できます。

次のコマンドで両方のサービスを起動します。

```sh
docker compose up --build -d
```

別のターミナルでアプリケーションのログを確認できます。

```sh
docker compose logs -f app
```

デモの実行中は、次の URL を開いてください。

| URL | 用途 |
| --- | --- |
| http://localhost:8080 | Spring Boot アプリケーションのポート |
| http://localhost:8081/actuator/prometheus | Prometheus 形式のメトリクスエンドポイント |
| http://localhost:9090 | Prometheus の Web UI |

Prometheus の Web UI では、次のクエリを実行できます。

```promql
jvm_memory_used_bytes{area="heap"}
jvm_memory_committed_bytes{area="heap"}
jvm_memory_max_bytes{area="heap"}
```

Prometheus は、Compose ネットワーク内のサービス名 `app` と管理ポート `8081` を使って `app:8081` をスクレイプします。

アプリケーションは最終的に `OutOfMemoryError` で停止しますが、Prometheus は動作し続けるため、取得済みのサンプルを確認できます。

サービスを停止するときは、次のコマンドを実行します。

```sh
docker compose down
```

英語版は [README.md](README.md) です。

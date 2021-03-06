# libgenaro-android

Asynchronous Android library and CLI for encrypted file transfer on the Genaro network. It's compatible with [libgenaro](https://github.com/GenaroNetwork/libgenaro).

## Feature Finished

- Load wallet from json file with password
- Use wallet to sign request for user authentication
- Delete bucket/rename bucket/list buckets/list files/delete file/upload file/download file
- Asynchronous I/O with concurrent peer-to-peer network requests for shards
- File encryption with AES-256-CTR
- File name and bucket name encryption with AES-256-GCM
- Asynchronous progress updates
- Retry several times when upload/download is failed
- Seed based file encryption key for portability between devices
- File integrity and authenticity verified with HMAC-SHA512
- Erasure encoding with reed solomon for data reliability
- Exchange report with bridge
- Command line interface
- Mock bridge and farmer, and continous integration

## Feature Todo

- File encryption key be provided to decrypt encrypted file
- String literal be encrypted with AES-256-CTR and directly stored to a bucket

## Issues

- Upload or download file of large size(>256MB) will not use Reed-Solomon algorithm, or it may cause an OutOfMemoryError, becasue the Reed-Solomon algorithm doesn't support memory mapped files for now.

## 3rd party dependencies

- [Spongy Castle](https://rtyley.github.io/spongycastle/) for crypto algorithms.
- [dnsjava](http://www.xbill.org/dnsjava/) for base16 encoding.
- [web3j](https://web3j.io/) for wallet managment, BIP39 and Interaction with blockchain.
- [JavaReedSolomon](https://github.com/Backblaze/JavaReedSolomon) for reed solomon algorithm.
- [jackson](https://github.com/FasterXML/jackson) for JSON parse/compose.
- [okhttp3](https://github.com/square/okhttp) as HTTP client.
- [java-getopt](https://www.urbanophile.com/arenn/hacking/download.html) a Java command line option parser that is compatible with GNU getopt.
- [guava](https://github.com/google/guava) and [apache.commons](https://commons.apache.org/) as utility.
- [testng](https://testng.org/doc/index.html) for testing.

## Used as 3rd party package

1. Import libgenaro-android as a module of your app

2. Add libgenaro-android as a dependency of your app, something like: 

```xml
dependencies {
    implementation project(':libgenaro-android')
}
```

3. Add repository for dnsjava to build.gradle of your app:

```gradle
repositories {
    maven { url "http://www.jabylon.org/maven/" }
}
```

4. Add READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE and INTERNET permissions to AndroidManifest.xml of your app:

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.INTERNET" />
```

5. Your app should request for READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permission before upload and download, such as:

```java
ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_REQUEST_CODE);
```

(PS: the JDK version of your app must be 1.8 or higher)

## Example Usage

Initialize:

```java
String bridgeUrl = "http://47.100.33.60:8080";
String V3JSON = "{ \"address\": \"aaad65391d2d2eafda9b2732000001d52a6a3dc8\",
        \"crypto\": { \"cipher\": \"aes-128-ctr\",
        \"ciphertext\": \"e968751f3d60827b6e6000006c024ecc82f33a6c55428be33249c83edba444ca\",
        \"cipherparams\": { \"iv\": \"e80d9ec9b00000a143c756ec78066ad9\" }, \"kdf\": \"scrypt\",
        \"kdfparams\": { \"dklen\": 32, \"n\": 262144, \"p\": 1, \"r\": 8, \"salt\":
        \"ea7cb2b004db67d000003790caced7a96b636762f280b243e794fb5bef8ef74b\" },
        \"mac\": \"ceb3789e77be8f2a7ab4d000001b54e048ad3f5b080b96e07759de7442e050d2\" },
        \"id\": \"e28f31b4-1f43-428b-9b12-ab58000004b1\", \"version\": 3 }";
String passwd = "xxxxxx";
Genaro api;
try {
    api = new Genaro(bridgeUrl, V3JSON, passwd);
} catch (Exception e) {
    return;
}
```

List buckets:

```java
CompletableFuture<Void> fu = api.getBuckets(new GetBucketsCallback() {
    @Override
    public void onFinish(Bucket[] buckets) { }
    @Override
    public void onFail(String error) { }
});

// getBuckets is Non-Blocking, if you want to wait until it is finished, call fu.join()
```

Delete bucket:

```java
String bucketId = "5bfcf77cea9b6322c5abd929";
CompletableFuture<Void> fu = api.deleteBucket(bucketId, new DeleteBucketCallback() {
    @Override
    public void onFinish() { }
    @Override
    public void onFail(String error) { }
}));

// deleteBucket is Non-Blocking, if you want to wait until it is finished, call fu.join()
```

Rename bucket:

```java
String bucketId = "5bfcf77cea9b6322c5abd929";
String newName = "abc";
CompletableFuture<Void> fu = api.renameBucket(bucketId, newName, new RenameBucketCallback() {
    @Override
    public void onFinish() { }
    @Override
    public void onFail(String error) { }
}

// renameBucket is Non-Blocking, if you want to wait until it is finished, call fu.join()
```

List files:

```java
String bucketId = "5bfcf77cea9b6322c5abd929";
CompletableFuture<Void> fu = api.listFiles(bucketId, new ListFilesCallback() {
    @Override
    public void onFinish(GenaroFile[] files) { }
    @Override
    public void onFail(String error) { }
}

// listFiles is Non-Blocking, if you want to wait until it is finished, call fu.join()
```

Delete file:

```java
String bucketId = "5bfcf77cea9b6322c5abd929";
String fileId = "5c0e1289bbdd6f2d157dd8b2";
CompletableFuture<Void> fu = api.deleteFile(bucketId, fileId, new DeleteFileCallback() {
    @Override
    public void onFinish() { }
    @Override
    public void onFail(String error) { }
}

// deleteFile is Non-Blocking, if you want to wait until it is finished, call fu.join()
```

Upload file:

```java
String bucketId = "5bfcf4ea7991d267f4eb53b4";
String filePath = "xxxxxx";
String fileName = "abc.txt";
boolean rs = false;
Uploader uploader = api.storeFile(rs, filePath, fileName, bucketId, new StoreFileCallback() {
    @Override
    public void onBegin(long fileSize) { }
    @Override
    public void onFail(String error) { }
    @Override
    public void onFinish(String fileId) { }
    @Override
    public void onProgress(float progress) { }
});

// storeFile is Non-Blocking, if you want to wait until it is finished, call uploader.join()
// if you want to cancel it, call uploader.cancel()
```

Download file:

```java
String bucketId = "5bfcf4ea7991d267f4eb53b4";
String fileId = "5c0103fd5a158a5612e67461";
String filePath = "xxxxxx";
Downloader downloader = api.resolveFile(bucketId, fileId, filePath, new ResolveFileCallback() {
    @Override
    public void onBegin() { }
    @Override
    public void onFail(String error) { }
    @Override
    public void onFinish() { }
    @Override
    public void onProgress(float progress) { }
});

// resolveFile is Non-Blocking, if you want to wait until it is finished, call downloader.join()
// if you want to cancel it, call downloader.cancel()
```

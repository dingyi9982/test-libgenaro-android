package genaro.test.testgenaro;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import network.genaro.storage.Genaro;
import network.genaro.storage.GenaroCallback;
import network.genaro.storage.Uploader;

public class UploadActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener {
    //    private static final String V3JSON = "{\"version\":3,\"id\":\"b3d00298-275f-4f09-96d0-2da6000f2a04\",\"address\":\"aaad65391d2d2eafda9b27326d1e80002a6a3dc8\",\"crypto\":{\"ciphertext\":\"c362de15e57e1fd0ca66b6c2483292ed260000000065164e875eebece257702e\",\"cipherparams\":{\"iv\":\"934b7985f4c60000000f97f89a101ee7\"},\"cipher\":\"aes-128-ctr\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"salt\":\"f5e2b5075600000003c66191656e03cfb19b5e537dcb117ad4fbc1fda46f61c5\",\"n\":262144,\"r\":8,\"p\":1},\"mac\":\"0b8c0000000b9e9de24357bbe74b68baf576ac31e9bfebe3f3d48c5474703df9\"},\"name\":\"Wallet 0\"}";
    private static final String V3JSON = "{\"version\":3,\"id\":\"b3d60298-275f-4f09-96d0-2da65acf2a04\",\"address\":\"fbad65391d2d2eafda9b27326d1e81d52a6a3dc8\",\"crypto\":{\"ciphertext\":\"c362de15e57e1fd0ca66b6c2483292ed26bd0536d065164e875eebece257702e\",\"cipherparams\":{\"iv\":\"934b7985f4c6e6b1ffef97f89a101ee7\"},\"cipher\":\"aes-128-ctr\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"salt\":\"f5e2b50756acf30cf3c66191656e03cfb19b5e537dcb117ad4fbc1fda46f61c5\",\"n\":262144,\"r\":8,\"p\":1},\"mac\":\"0b8c544236fb9e9de24357bbe74b68baf576ac31e9bfebe3f3d48c5474703df9\"},\"name\":\"Wallet 0\"}";

    //    private static String testBridgeUrl = "http://118.31.61.119:8080";
//        private static String testBridgeUrl = "http://127.0.0.1:8080";
    private static String testBridgeUrl = "http://120.77.247.10:8080";
    //    private static String testBridgeUrl = "http://47.100.33.60:8080";
//        private static final String testbucketId = "5bfcf4ea7991d267f4eb53b4";
//    private static final String testbucketId = "b5e9bd5fd6f571beee9b035f";
    private static final String testbucketId = "5ba341402e49103d8787e52d";
//    private static final String testbucketId = "5c0e5a8b312cfa12ae9f5bf3";

    private Genaro genaro;

    private Button browseButton;
    private EditText upMsgEditText;

    private static int READ_REQUEST_CODE = 1;

    private void storeFile(String filePath, String fileName, String bucketId) {
        if (false == Utils.checkPermission(this, false, 1)) {
            Toast.makeText(this, "请开通访问外部存储器的权限！", Toast.LENGTH_SHORT).show();
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                upMsgEditText.setText("");
            }
        });

        Uploader uploader = genaro.storeFile(true, filePath, fileName, bucketId, new GenaroCallback.StoreFileCallback() {
            @Override
            public void onBegin(long fileSize) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        upMsgEditText.append("Upload started\n");
                    }
                });
            }

            @Override
            public void onProgress(float progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        upMsgEditText.append(String.format("Upload progress: %.1f%%\n", progress * 100));
                    }
                });
            }

            @Override
            public void onFail(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        upMsgEditText.append("Upload failed, reason: " + (error != null ? error : "Unknown"));
                    }
                });
            }

            @Override
            public void onCancel() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        upMsgEditText.append("Upload is cancelled");
                    }
                });
            }

            @Override
            public void onFinish(String fileId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        upMsgEditText.append("Upload finished, fileId: " + fileId);
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        try {
            genaro = new Genaro(testBridgeUrl, V3JSON, "111111");
        } catch (Exception e) {
            return;
        }

        browseButton = findViewById(R.id.browseButton);

        upMsgEditText = findViewById(R.id.upMsgEditText);
        upMsgEditText.setFocusable(false);
        upMsgEditText.setCursorVisible(false);
        upMsgEditText.setShowSoftInputOnFocus(false);
        upMsgEditText.setClickable(true);

        myGesture = new GestureDetector(this, this);

        upMsgEditText.setOnTouchListener(this);

        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                // browser.
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                // Filter to show only images, using the image MIME data type.
                // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                // To search for all documents available via installed storage providers,
                // it would be "*/*".
                intent.setType("*/*");

                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });

        Utils.checkPermission(this, true, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                String prefix = "/document/raw:";
                String filePath = resultData.getData().getPath();

                if (filePath.startsWith(prefix)) {
                    filePath = filePath.substring(filePath.indexOf(prefix) + prefix.length() + 1);
                }
                String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
                storeFile(filePath, fileName, testbucketId);
            }
        }
    }

    private GestureDetector myGesture;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        myGesture.onTouchEvent(event);
        return false;
    }

    @Override

    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float xDelta = Math.abs(e2.getX() - e1.getX());
        float yDelta = Math.abs(e2.getY() - e1.getY());

        if (xDelta > 2 * yDelta && (xDelta / getResources().getDisplayMetrics().widthPixels >= 0.2f)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;

    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
}

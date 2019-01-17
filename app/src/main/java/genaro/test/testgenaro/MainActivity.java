package genaro.test.testgenaro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import network.genaro.storage.Bucket;
import network.genaro.storage.Genaro;
import network.genaro.storage.GenaroCallback;
import network.genaro.storage.GenaroFile;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener {
//    private static final String V3JSON = "{\"version\":3,\"id\":\"b3d00298-275f-4f09-96d0-2da6000f2a04\",\"address\":\"aaad65391d2d2eafda9b27326d1e80002a6a3dc8\",\"crypto\":{\"ciphertext\":\"c362de15e57e1fd0ca66b6c2483292ed260000000065164e875eebece257702e\",\"cipherparams\":{\"iv\":\"934b7985f4c60000000f97f89a101ee7\"},\"cipher\":\"aes-128-ctr\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"salt\":\"f5e2b5075600000003c66191656e03cfb19b5e537dcb117ad4fbc1fda46f61c5\",\"n\":262144,\"r\":8,\"p\":1},\"mac\":\"0b8c0000000b9e9de24357bbe74b68baf576ac31e9bfebe3f3d48c5474703df9\"},\"name\":\"Wallet 0\"}";
    private static final String V3JSON = "{\"version\":3,\"id\":\"b3d60298-275f-4f09-96d0-2da65acf2a04\",\"address\":\"fbad65391d2d2eafda9b27326d1e81d52a6a3dc8\",\"crypto\":{\"ciphertext\":\"c362de15e57e1fd0ca66b6c2483292ed26bd0536d065164e875eebece257702e\",\"cipherparams\":{\"iv\":\"934b7985f4c6e6b1ffef97f89a101ee7\"},\"cipher\":\"aes-128-ctr\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"salt\":\"f5e2b50756acf30cf3c66191656e03cfb19b5e537dcb117ad4fbc1fda46f61c5\",\"n\":262144,\"r\":8,\"p\":1},\"mac\":\"0b8c544236fb9e9de24357bbe74b68baf576ac31e9bfebe3f3d48c5474703df9\"},\"name\":\"Wallet 0\"}";

//    private static String testBridgeUrl = "http://118.31.61.119:8080";
//    private static String testBridgeUrl = "http://127.0.0.1:8080";
//    private static String testBridgeUrl = "http://120.77.247.10:8080";
    private static String testBridgeUrl = "http://47.100.33.60:8080";

    private Genaro genaro;

    ListView bucketsView;
    ListView filesView;
    EditText downMsgEditText;
    Button browseButton;
    ArrayList<BucketItem> bucketsList;
    ArrayAdapter<BucketItem> bucketsAdapter;
    ArrayList<FileItem> filesList;
    ArrayAdapter<FileItem> filesAdapter;

//    public void showdialog(String text){
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("dialog 1");
//
//        builder.setMessage(text);
//        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                Toast.makeText(MainActivity.this, "yes",Toast.LENGTH_SHORT).show();
//            }
//        });
//        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                Toast.makeText(MainActivity.this, "no",Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        builder.show();
//    }

    private class BucketItem {
        private String id;
        private String name;

        private BucketItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        String getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class FileItem {
        private String bucket;
        private String id;
        private String filename;

        private FileItem(String bucket, String id, String filename) {
            this.bucket = bucket;
            this.id = id;
            this.filename = filename;
        }

        String getBucket() {
            return bucket;
        }

        String getId() {
            return id;
        }

        String getFilename() {
            return filename;
        }

        @Override
        public String toString() {
            return filename;
        }
    }

    private void listBuckets() {
        bucketsList.clear();
        genaro.getBuckets(new GenaroCallback.GetBucketsCallback() {
            @Override
            public void onFinish(Bucket[] buckets) {
                for (Bucket b : buckets) {
                    bucketsList.add(new BucketItem(b.getId(), b.getName()));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bucketsAdapter.notifyDataSetChanged();
                    }
                });
            }
            @Override
            public void onFail(String error) {
            }
        });
    }

    private void listFiles(String bucketId) {
        filesList.clear();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                filesAdapter.notifyDataSetChanged();
            }
        });

        genaro.listFiles(bucketId, new GenaroCallback.ListFilesCallback() {
            @Override
            public void onFinish(GenaroFile[] files) {
                for (GenaroFile b : files) {
                    filesList.add(new FileItem(b.getBucket(), b.getId(), b.getFilename()));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        filesAdapter.notifyDataSetChanged();
                    }
                });
            }
            @Override
            public void onFail(String error) {
            }
        });
    }

    private void resolveFile(String bucketId, String fileId, String filePath) {
        if (false == Utils.checkPermission(this, false, 1)) {
            Toast.makeText(this, "请打开外部存储器访问权限！", Toast.LENGTH_SHORT).show();
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downMsgEditText.setText("");
            }
        });

        genaro.resolveFile(bucketId, fileId, filePath, true, new GenaroCallback.ResolveFileCallback() {
            @Override
            public void onBegin() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downMsgEditText.append("Download started\n");
                    }
                });
            }
            @Override
            public void onProgress(final float progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downMsgEditText.append(String.format("Download progress: %.1f%%\n", progress * 100));
                    }
                });
            }
            @Override
            public void onFail(final String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downMsgEditText.append("Download failed, reason: " + (error != null ? error : "Unknown") + "\n");
                    }
                });
            }
            @Override
            public void onCancel() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downMsgEditText.append("Download is cancelled");
                    }
                });
            }
            @Override
            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downMsgEditText.append("Download finished");
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            genaro = new Genaro(testBridgeUrl, V3JSON, "111111");
        } catch (Exception e) {
            return;
        }

        bucketsView = findViewById(R.id.bucketsView);
        bucketsList = new ArrayList<>();
        bucketsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bucketsList);
        bucketsView.setAdapter(bucketsAdapter);
        bucketsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BucketItem bi = (BucketItem) ((ListView)parent).getAdapter().getItem(position);
                listFiles(bi.getId());
            }
        });

        filesView = findViewById(R.id.filesView);
        filesList = new ArrayList<>();
        filesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_gallery_item, filesList);
        filesView.setAdapter(filesAdapter);
        filesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileItem bi = (FileItem) ((ListView)parent).getAdapter().getItem(position);
                String filePath = getExternalFilesDir("").getAbsolutePath() + "/" + bi.getFilename();
                resolveFile(bi.getBucket(), bi.getId(), filePath);
            }
        });

        listBuckets();

        downMsgEditText = findViewById(R.id.downMsgEditText);
        downMsgEditText.setFocusable(false);
        downMsgEditText.setCursorVisible(false);
        downMsgEditText.setShowSoftInputOnFocus(false);
        downMsgEditText.setClickable(true);

        myGesture = new GestureDetector(this, this);

        downMsgEditText.setOnTouchListener(this);

        Utils.checkPermission(this, true, 1);
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
            Intent intent = new Intent(this, UploadActivity.class);
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

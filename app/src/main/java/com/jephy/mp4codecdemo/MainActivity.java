package com.jephy.mp4codecdemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.jephy.mp4codecdemo.util.PermissionUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String TEST_INPUT_FILE_PATH = SDCARD_PATH + "/EncodeResource/test1.mp4";
    private static final String TEST_OUTPUT_FILE_PATH = SDCARD_PATH + "/EncodeResource/output.mp4";

    private static final String VIDEO_MEDIA_TYPE = "video/";//视频的轨道类型

    private static String TAG = "decode_test";
    @BindView(R.id.start_decode_bt)
    Button startDecodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        PermissionUtils.performCodeWithStoragePermission(this, new PermissionUtils
                .PermissionCallback() {
            @Override
            public void hasPermission() {

            }
        });

    }

    @OnClick(R.id.start_decode_bt)
    public void startDecode(Button button) {
        Log.d(TAG, "点击开始解码");
    }




}

package com.jephy.mp4codecdemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.jephy.mp4codecdemo.util.PermissionUtils;

import java.nio.ByteBuffer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String TEST_INPUT_FILE_PATH = SDCARD_PATH + "/CodecResource/test3.mp4";
    private static final String TEST_OUTPUT_FILE_PATH = SDCARD_PATH + "/CodecResource/output.mp4";

//    private static final String TEST_INPUT_FILE_PATH = SDCARD_PATH + "/1/aa/d11.mp4";
//    private static final String TEST_OUTPUT_FILE_PATH = SDCARD_PATH + "/1/aa/d3.mp4";

    private static final String VIDEO_MEDIA_TYPE = "video/";//视频的轨道类型

    private static String TAG = "decode_test";
    @BindView(R.id.start_decode_bt)
    Button startDecodeButton;

    private static int BYTE_BUFFER_LENGTH = 1024 * 1024;

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

//        syncDecode();
//        VideoDecoder videoDecoder = new VideoDecoder(TEST_INPUT_FILE_PATH);
        VideoDecoder videoDecoder = new VideoDecoder(TEST_INPUT_FILE_PATH);

        videoDecoder.setOnFrameListener(new VideoDecoder.OnFrameListener() {
            @Override
            public void onFrame(ByteBuffer byteBuffer, byte[] imuData) {

                Log.d(TAG, "byteBuffer.remaining() = "+byteBuffer.remaining() + "imuData = " + imuData.length);


            }
        });

        videoDecoder.startDecode();

    }
}

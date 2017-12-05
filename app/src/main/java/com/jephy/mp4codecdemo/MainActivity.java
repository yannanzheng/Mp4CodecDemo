package com.jephy.mp4codecdemo;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.jephy.mp4codecdemo.util.PermissionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.media.MediaFormat.KEY_MIME;

public class MainActivity extends AppCompatActivity {
    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String TEST_INPUT_FILE_PATH = SDCARD_PATH + "/CodecResource/test2.mp4";
    private static final String TEST_OUTPUT_FILE_PATH = SDCARD_PATH + "/CodecResource/output.mp4";

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

        String string = stringFromJNI();
        Log.d(TAG, "jni, string = "+string);

    }

    @OnClick(R.id.start_decode_bt)
    public void startDecode(Button button) {
        Log.d(TAG, "点击开始解码");

        syncDecode();

    }

    private void syncDecode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isFileExists(TEST_INPUT_FILE_PATH)) {
                    Log.d(TAG, "mp4文件存在");
                    MediaExtractor mediaExtractor = new MediaExtractor();
                    try {
                        mediaExtractor.setDataSource(TEST_INPUT_FILE_PATH);
                        int mVideoTrackIndex = getMediaTrackIndex(mediaExtractor, VIDEO_MEDIA_TYPE);
                        mediaExtractor.selectTrack(mVideoTrackIndex);

                        Log.d(TAG, "mp4文件存在,mVideoTrackIndex = " + mVideoTrackIndex);
                        if (mVideoTrackIndex >= 0) {
                            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(mVideoTrackIndex);
                            int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
                            int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
                            Log.d(TAG, "width = " + width + ", height = " + height);
                            mediaExtractor.selectTrack(mVideoTrackIndex);

                            MediaCodec decoder = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
                            decoder.configure(mediaFormat, null, null, 0);
                            decoder.start();

                            ByteBuffer inputBuffer = ByteBuffer.allocate(BYTE_BUFFER_LENGTH);
                            while (true) {
                                long presentationTimeUs = mediaExtractor.getSampleTime();//pts以微秒计算

                                int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);//读取样本数据并存放到inputBuffer中
                                if (sampleSize < 0) {
                                   break;
                                }

                                int inputBufferIndex = decoder.dequeueInputBuffer(sampleSize);
                                Log.d(TAG,"inputBufferIndex = "+inputBufferIndex);

                                if (inputBufferIndex > 0) {
                                    ByteBuffer inputBuffer1 = decoder.getInputBuffer(inputBufferIndex);
                                    inputBuffer1.put(inputBuffer);
                                    ByteBuffer imuData = getIMUData(inputBuffer);

                                    Log.d(TAG, "imuData" + imuData);
                                    if(imuData == null)
                                    {
                                        Log.d(MainActivity.class.getName(),"imu 未获取到.");
                                    }else
                                    {
                                        Log.d(MainActivity.class.getName(),"imu 获取到.");
                                    }

                                    decoder.queueInputBuffer(inputBufferIndex,0,sampleSize,presentationTimeUs,0);
                                }

                                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                                int outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo,presentationTimeUs);
                                if (outputBufferIndex >= 0) {
                                    ByteBuffer outputBuffer = decoder.getOutputBuffer(outputBufferIndex);
                                    Log.d("decode_output", "输出字节成功，bufferInfo = " + bufferInfo + ", outputBuffer.remaining() = " + outputBuffer.remaining() + ", pts = " + presentationTimeUs/1000);

                                    decoder.releaseOutputBuffer(outputBufferIndex,false);
                                }

                                Log.d(TAG, "bufferInfo = " + bufferInfo + "outputBufferIndex = " + outputBufferIndex);

                                Log.d(TAG, "presentationTimeUs = " + presentationTimeUs / 1000 + ", sampleSize = " + sampleSize);
                                mediaExtractor.advance();
                            }
                            Log.d("decode_output","解码结束");

                            mediaExtractor.release();
                            mediaExtractor = null;

                            decoder.stop();
                            decoder.release();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    //获取指定类型媒体文件所在轨道
    private int getMediaTrackIndex(MediaExtractor videoExtractor, String mediaType) {
        int trackIndex = -1;
        for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
            //获取视频所在轨道
            MediaFormat mediaFormat = videoExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(KEY_MIME);
            if (mime.startsWith(mediaType)) {
                trackIndex = i;
                break;
            }
        }
        return trackIndex;
    }

    private boolean isFileExists(String filePath) {
        //文件是否存在
        boolean exists = new File(filePath).exists();
        if (!exists) {
            Toast.makeText(this, "文件不存在。。。。", Toast.LENGTH_SHORT).show();
        }
        return exists;
    }

    public native String stringFromJNI();

    public native ByteBuffer getIMUData(ByteBuffer packet);

    static {
        System.loadLibrary("native-lib");
    }

}

package com.jephy.mp4codecdemo;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.jephy.mp4codecdemo.util.PermissionUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import encode.OnVideoEncodeListener;
import encode.VideoConfiguration;
import encode.YUVInputVideoController;

import static android.media.MediaFormat.KEY_MIME;

public class MainActivity extends AppCompatActivity {
    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
//    private static final String TEST_INPUT_FILE_PATH = SDCARD_PATH + "/CodecResource/test3.mp4";
//    private static final String TEST_OUTPUT_FILE_PATH = SDCARD_PATH + "/CodecResource/output.mp4";

    private static final String TEST_INPUT_FILE_PATH = SDCARD_PATH + "/1/aa/d11.mp4";
    private static final String TEST_OUTPUT_FILE_PATH = SDCARD_PATH + "/1/aa/d3.mp4";

    private static final String VIDEO_MEDIA_TYPE = "video/";//视频的轨道类型

    private static String TAG = "decode_test";
    @BindView(R.id.start_decode_bt)
    Button startDecodeButton;

    private static int BYTE_BUFFER_LENGTH = 1024 * 1024;
    private MediaMuxer mediaMuxer;

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

        final MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(TEST_INPUT_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }


        final int mVideoTrackIndex = getMediaTrackIndex(extractor, VIDEO_MEDIA_TYPE);

        MediaFormat format = extractor.getTrackFormat(mVideoTrackIndex);
        final int width = format.getInteger(MediaFormat.KEY_WIDTH);
        final int height = format.getInteger(MediaFormat.KEY_HEIGHT);

        mediaMuxer = null;
        try {
            mediaMuxer = new MediaMuxer(TEST_OUTPUT_FILE_PATH, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaMuxer.addTrack(format);
        mediaMuxer.start();

        final YUVInputVideoController yuvInputEncoder = new YUVInputVideoController(format);
        VideoConfiguration configuration = new VideoConfiguration.Builder().setSize(width, height).build();
        Log.i(TAG, "videoWidth: " + configuration.width + " videoHeight: " + configuration.height);
        yuvInputEncoder.setVideoConfiguration(configuration);
        yuvInputEncoder.resume();

        yuvInputEncoder.setVideoEncoderListener(new OnVideoEncodeListener() {
            @Override
            public void onVideoEncode(ByteBuffer bb, MediaCodec.BufferInfo bi) {
                Log.d("yuvInputEncoder", "编码， byteBuffer.remaining() = "+bb.remaining() +", bufferInfo.pts = "+bi.presentationTimeUs);

                mediaMuxer.writeSampleData(mVideoTrackIndex,bb,bi);
            }
        });

        yuvInputEncoder.start();

        VideoDecoder videoDecoder = new VideoDecoder(TEST_INPUT_FILE_PATH);
        videoDecoder.setOnFrameListener(new VideoDecoder.OnFrameListener() {
            @Override
            public void onFrame(ByteBuffer byteBuffer, byte[] imuData , MediaCodec.BufferInfo bufferInfo) {
//                Log.d(TAG, "byteBuffer.remaining() = "+byteBuffer.remaining() + "imuData = " + imuData.length);
                Log.d(TAG, "byteBuffer.remaining() = "+byteBuffer.remaining() + "imuData = "+imuData+", bufferInfo.pts = "+bufferInfo.presentationTimeUs);
//                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM )!= 0) {
//                    yuvInputEncoder.pause();
//                    yuvInputEncoder.stop();
//                    mediaMuxer.stop();
//                    mediaMuxer.release();
//                    Log.d(TAG,"onFrame, release" );
//                }

                yuvInputEncoder.queueBufferInfo(byteBuffer,width,height,bufferInfo);

            }
        });

        videoDecoder.setOnDecodeCompleteListener(new VideoDecoder.OnDecodeCompleteListener() {
            @Override
            public void onComplete() {
                    yuvInputEncoder.pause();
                    yuvInputEncoder.stop();
                    mediaMuxer.stop();
                    mediaMuxer.release();
                    Log.d(TAG,"onFrame, release" );
            }
        });

        videoDecoder.startDecode();
    }

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
}

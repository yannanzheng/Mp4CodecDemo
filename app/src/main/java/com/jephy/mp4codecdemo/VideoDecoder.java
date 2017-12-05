package com.jephy.mp4codecdemo;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaFormat.KEY_MIME;

/**
 * Created by jfyang on 12/5/17.
 */

public class VideoDecoder {
    private static String TAG = "VideoDecoder";
    private static final String VIDEO_MEDIA_TYPE = "video/";//视频的轨道类型
    private static int BYTE_BUFFER_LENGTH = 1024 * 1024;

    private String videoFilePath;

    public native String stringFromJNI();

    public native byte[] getIMUData(byte[] packet);

    static {
        System.loadLibrary("native-lib");
    }

    public VideoDecoder(String videoFilePath) {
        this.videoFilePath = videoFilePath;
        String string = stringFromJNI();
        Log.d(TAG, "jni, string = "+string);
    }

    public void startDecode(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isFileExists(videoFilePath)) {
                    Log.d(TAG, "mp4文件存在");
                    MediaExtractor mediaExtractor = new MediaExtractor();
                    try {
                        mediaExtractor.setDataSource(videoFilePath);
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

                                byte[] bytes = new byte[inputBuffer.remaining()];
                                inputBuffer.get(bytes);

                                byte[] imuData = getIMUData(bytes);

                                Log.d(TAG, "输入数据，inputBuffer.remaining = " + inputBuffer.remaining() + ", imuData = " + imuData);

                                if (imuData == null) {
                                    Log.d(TAG, "imu 未获取到.");
                                } else {
                                    Log.e(TAG, "imu 获取到.");
                                }

                                ByteBuffer newInputByffer = ByteBuffer.wrap(bytes);

                                int inputBufferIndex = decoder.dequeueInputBuffer(sampleSize);

                                Log.d(TAG,"inputBufferIndex = "+inputBufferIndex+", inputBuffer.remaining = "+newInputByffer.remaining());

                                if (inputBufferIndex > 0) {

                                    ByteBuffer inputBuffer1 = decoder.getInputBuffer(inputBufferIndex);
                                    inputBuffer1.put(newInputByffer);
                                    decoder.queueInputBuffer(inputBufferIndex,0,sampleSize,presentationTimeUs,0);
                                }

                                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                                int outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo,presentationTimeUs);
                                if (outputBufferIndex >= 0) {
                                    ByteBuffer outputBuffer = decoder.getOutputBuffer(outputBufferIndex);
                                    Log.d("decode_output", "输出字节成功，bufferInfo = " + bufferInfo + ", outputBuffer.remaining() = " + outputBuffer.remaining() + ", pts = " + presentationTimeUs/1000);
                                    if (onFrameListener != null) {
                                        onFrameListener.onFrame(outputBuffer,imuData, bufferInfo);
                                    }
                                    decoder.releaseOutputBuffer(outputBufferIndex,false);
                                }

                                try {
                                    Thread.sleep(33);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
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

                            if (onDecodeCompleteListener != null) {
                                onDecodeCompleteListener.onComplete();
                            }



                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

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

    private boolean isFileExists(String filePath) {
        return new File(filePath).exists();
    }


    public void setOnFrameListener(OnFrameListener onFrameListener) {
        this.onFrameListener = onFrameListener;
    }

    private OnFrameListener onFrameListener;

    public interface OnFrameListener{
        void onFrame(ByteBuffer byteBuffer, byte[] imuData, MediaCodec.BufferInfo bufferInfo);
    }

    public void setOnDecodeCompleteListener(OnDecodeCompleteListener onDecodeCompleteListener) {
        this.onDecodeCompleteListener = onDecodeCompleteListener;
    }

    private OnDecodeCompleteListener onDecodeCompleteListener;

    public interface OnDecodeCompleteListener {
        void onComplete();
    }
}

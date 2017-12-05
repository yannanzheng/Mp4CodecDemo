package encode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

public class YUVInputVideoController {
    private VideoConfiguration mVideoConfiguration = VideoConfiguration.createDefault();
    private YUVInputEncoder mEncoder;
    private OnVideoEncodeListener mListener;
    public MediaFormat mMediaFormat;

    public YUVInputVideoController(MediaFormat mMediaFormat) {
        this.mMediaFormat = mMediaFormat;
    }

    public void start() {
        mEncoder = new YUVInputEncoder(mVideoConfiguration);
        mEncoder.start();
        mEncoder.setOnVideoEncodeListener(mListener);

    }

    public int getEncoderInputBufferInfoIndex() {
        if (mEncoder != null) {
            return mEncoder.getBufferInfoIndex();
        }
        Log.e("xxx","mEncoder:"+mEncoder);
        return -1;
    }

    public ByteBuffer getEncoderInputBufferInfo(int index) {
        if (mEncoder != null) {
            return mEncoder.getBufferInfo(index);
        }
        return null;
    }

    public void queueBufferInfo(int index, ByteBuffer buffer, MediaCodec.BufferInfo bi) {
        if (mEncoder != null) {
            mEncoder.queueInputBufferInfo(index, buffer,bi);
        }
    }


    public void stop() {
        Log.d("gsliu","decodeCount[0] : stop");
        if (mEncoder != null) {
            mEncoder.setOnVideoEncodeListener(null);
            mEncoder.stop();
            mEncoder = null;
        }
    }

    public void pause() {
        if (mEncoder != null) {
            mEncoder.setPause(true);
        }
    }

    public void resume() {
        if (mEncoder != null) {
            mEncoder.setPause(false);
        }
    }

    public boolean setVideoBps(int bps) {
        //重新设置硬编bps，在低于19的版本需要重启编码器
        boolean result = false;
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//            //由于重启硬编编码器效果不好，此次不做处理
//            SopCastLog.d(SopCastConstant.TAG, "Bps need change, but MediaCodec do not support.");
//        } else {
//            if (mEncoder != null) {
//                SopCastLog.d(SopCastConstant.TAG, "Bps change, current bps: " + bps);
//                mEncoder.setRecorderBps(bps);
//                result = true;
//            }
//        }
        return result;
    }

//    @Override
    public void setVideoEncoderListener(OnVideoEncodeListener listener) {
        mListener = listener;
    }

//    @Override
    public void setVideoConfiguration(VideoConfiguration configuration) {
        mVideoConfiguration = configuration;
    }

    public void queueBufferInfo(ByteBuffer y, ByteBuffer u, ByteBuffer v, long width, long height, MediaCodec.BufferInfo bi) {
        int index = getEncoderInputBufferInfoIndex();
        if (index > 0) {
            ByteBuffer byteBuffer = getEncoderInputBufferInfo(index);
            if (byteBuffer != null) {
                byteBuffer.put(y);
                byteBuffer.put(u);
                byteBuffer.put(v);
                queueBufferInfo(index, byteBuffer,bi);
            }
        }
    }

    public void queueBufferInfo(ByteBuffer yRef, ByteBuffer uvRef, long width, long height, MediaCodec.BufferInfo bi) {
        int index = getEncoderInputBufferInfoIndex();
        Log.e("xxx","index:"+index);
        if (index > 0) {
            ByteBuffer byteBuffer = getEncoderInputBufferInfo(index);
            if (byteBuffer != null) {
                byteBuffer.put(yRef);
                byteBuffer.put(uvRef);
                queueBufferInfo(index, byteBuffer,bi);
            }
        }
    }

    public void queueBufferInfo(ByteBuffer nv12Ref, long width, long height, MediaCodec.BufferInfo bi) {
        int index = getEncoderInputBufferInfoIndex();
        Log.i("gsliu","index:"+index);
        if (index > 0) {
            ByteBuffer byteBuffer = getEncoderInputBufferInfo(index);
            if (byteBuffer != null) {
                byteBuffer.put(nv12Ref);
                queueBufferInfo(index, byteBuffer, bi);
            }
        }
    }

}

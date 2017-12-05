package encode;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

@TargetApi(18)
public class YUVInputEncoder {

    private MediaCodec mMediaCodec;
    private OnVideoEncodeListener mListener;
    private boolean mPause;
    private HandlerThread mHandlerThread;
    private Handler mEncoderHandler;
    private VideoConfiguration mConfiguration;
    private MediaCodec.BufferInfo mBufferInfo;
    private volatile boolean isStarted;
    private ReentrantLock encodeLock = new ReentrantLock();

    public YUVInputEncoder(VideoConfiguration configuration) {
        mConfiguration = configuration;
        mMediaCodec = VideoMediaCodec.getVideoMediaCodec(mConfiguration);
    }

    public void setOnVideoEncodeListener(OnVideoEncodeListener listener) {
        mListener = listener;
    }

    public void setPause(boolean pause) {
        mPause = pause;
    }

    public void start() {
        mHandlerThread = new HandlerThread("LFEncode");
        mHandlerThread.start();
        mEncoderHandler = new Handler(mHandlerThread.getLooper());
        mBufferInfo = new MediaCodec.BufferInfo();
        mMediaCodec.start();
        mEncoderHandler.post(swapDataRunnable);
        isStarted = true;
    }


    public int getBufferInfoIndex() {
        if (mMediaCodec != null) {
            return mMediaCodec.dequeueInputBuffer(1200);
        }
        return -1;
    }


    public ByteBuffer getBufferInfo(int index) {
        if (mMediaCodec != null && index >= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return mMediaCodec.getInputBuffer(index);
            } else {
                return mMediaCodec.getInputBuffers()[index];
            }
        }
        return null;
    }

    public void queueInputBufferInfo(int index, ByteBuffer buffer) {
        if (mMediaCodec != null) {
            mMediaCodec.queueInputBuffer(index, 0, buffer.capacity(), System.currentTimeMillis(), 0);
        }
    }


    private Runnable swapDataRunnable = new Runnable() {
        @Override
        public void run() {
            drainEncoder();
        }
    };

    public void stop() {
        isStarted = false;
        mEncoderHandler.removeCallbacks(null);
        mHandlerThread.quit();
        encodeLock.lock();
        try {
            if(mMediaCodec != null) {
                mMediaCodec.signalEndOfInputStream();
            }
        } catch (IllegalStateException exception) {
            exception.printStackTrace();
        }
        releaseEncoder();
        encodeLock.unlock();
    }

    private void releaseEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setRecorderBps(int bps) {
        if (mMediaCodec == null) {
            return;
        }
        Bundle bitrate = new Bundle();
        bitrate.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, bps * 1024);
        mMediaCodec.setParameters(bitrate);
    }

    private void drainEncoder() {
        ByteBuffer[] outBuffers = mMediaCodec.getOutputBuffers();
        while (isStarted) {
            encodeLock.lock();
            if (mMediaCodec != null) {
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 12000);
                if (outBufferIndex >= 0) {
                    ByteBuffer bb = outBuffers[outBufferIndex];
                    if (mListener != null && !mPause) {
                        mListener. onVideoEncode(bb, bufferInfo);
                    }
                    mMediaCodec.releaseOutputBuffer(outBufferIndex, false);
                } else {
                    try {
                        // wait 10ms
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                encodeLock.unlock();
            } else {
                encodeLock.unlock();
                break;
            }
        }
    }


}

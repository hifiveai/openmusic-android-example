package com.longyuan.hifive.manager;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.longyuan.hifive.model.AudioVolumeInfo;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * #duxiaxing
 * #date: 2022/7/25
 */
public class AudioVolumeInfoUtil {
    private static AudioVolumeInfoUtil audioVolumeInfoUtil;

    public static AudioVolumeInfoUtil getInstance() {
        if (audioVolumeInfoUtil == null) {
            audioVolumeInfoUtil = new AudioVolumeInfoUtil();
        }
        return audioVolumeInfoUtil;
    }

    public void getInfo(String mediaPath, OnActionListener<AudioVolumeInfo> onActionListener) {
        File f = new File(mediaPath);
        if (!f.exists() || f.length() == 0 || f.isDirectory()) {
            if (onActionListener != null) {
                onActionListener.onFail("文件异常");
            }
            return;
        }
        readFile(f, onActionListener);
    }

    private void readFile(File inputFile, OnActionListener<AudioVolumeInfo> onActionListener) {
        showLog("ReadFile");
        if (onActionListener != null) {
            onActionListener.onStart();
        }
        MediaExtractor extractor = null;
        MediaCodec codec = null;
        try {
            AudioVolumeInfo audioVolumeInfo = new AudioVolumeInfo();
            extractor = new MediaExtractor();
            MediaFormat format = null;
            int i;

            //String[] components = inputFile.getPath().split("\\.");
            //audioVolumeInfo.mFileType = components[components.length - 1];
            int mFileSize = (int) inputFile.length();

            extractor.setDataSource(inputFile.getPath());
            int numTracks = extractor.getTrackCount();
            // 查找并选择文件中存在的第一个音轨。
            for (i = 0; i < numTracks; i++) {
                format = extractor.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                    extractor.selectTrack(i);
                    break;
                }
            }
            showLog("i = " + i);
            showLog("numTracks = " + numTracks);
            if (format == null || i == numTracks) {
                if (onActionListener != null) {
                    onActionListener.onFail("文件中找不到音频");
                }
                return;
            }
            int mChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            int mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            showLog("mChannels = " + mChannels);
            showLog("mSampleRate = " + mSampleRate);
            // 每个通道的期望样本总数。
            int expectedNumSamples =
                    (int) ((format.getLong(MediaFormat.KEY_DURATION) / 100.f) * mSampleRate + 0.5f);
            codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
            codec.configure(format, null, null, 0);
            codec.start();

            int decodedSamplesSize = 0;  //包含解码样本的输出缓冲区的大小。
            byte[] decodedSamples = null;
            ByteBuffer[] inputBuffers = codec.getInputBuffers();
            ByteBuffer[] outputBuffers = codec.getOutputBuffers();
            int sample_size;
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            long presentation_time;
            int tot_size_read = 0;
            boolean done_reading = false;

            // Set the size of the decoded samples buffer to 1MB (~6sec of a stereo stream at 44.1kHz).
            // For longer streams, the buffer size will be increased later on, calculating a rough
            // estimate of the total size needed to store all the samples in order to resize the buffer
            // only once.
            // Raw audio data
            ByteBuffer mDecodedBytes = ByteBuffer.allocate(1 << 20);
            boolean firstSampleData = true;

            long start1 = System.currentTimeMillis();
            int count = 0;
            //读取数据时跳过的帧数，如果数据太长耗时过久，且进度条不需要那么多数据
            int skipCount = -1;
            while (true) {
                count++;
                // 从文件中读取数据并将其提供给解码器输入缓冲区。
                int inputBufferIndex = codec.dequeueInputBuffer(100);
                if (!done_reading && inputBufferIndex >= 0) {
                    //把指定轨道中的数据，按照偏移量读取到 ByteBuffer 中
                    sample_size = extractor.readSampleData(inputBuffers[inputBufferIndex], 0);
                    if (skipCount == -1){
                        int totalCount = mFileSize/sample_size;
                        //以800此为基准，计算跳过的次数
                        skipCount = totalCount/800;
                        if (skipCount < 1)skipCount = 1;
                        showLog("计算大概次数：" + totalCount + ";每次循环跳过的帧数：" + skipCount);
                    }
                    if (firstSampleData
                            && format.getString(MediaFormat.KEY_MIME).equals("audio/mp4a-latm")
                            && sample_size == 2) {
                        // For some reasons on some devices (e.g. the Samsung S3) you should not
                        // provide the first two bytes of an AAC stream, otherwise the MediaCodec will
                        // crash. These two bytes do not contain music data but basic info on the
                        // stream (e.g. channel configuration and sampling frequency), and skipping them
                        // seems OK with other devices (MediaCodec has already been configured and
                        // already knows these parameters).
                        extractor.advance();//读取下一帧数据
                        tot_size_read += sample_size;
                    } else if (sample_size < 0) {
                        // 已读取所有样本。
                        codec.queueInputBuffer(
                                inputBufferIndex, 0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        done_reading = true;
                    } else {
                        presentation_time = extractor.getSampleTime();
                        codec.queueInputBuffer(inputBufferIndex, 0, sample_size, presentation_time, 0);
                        for (int k = 0; k < skipCount; k++) {
                            extractor.advance();
                            tot_size_read += sample_size;
                        }
                        if (onActionListener != null && onActionListener.isNeedProgress()) {
                            float progress = (float) (tot_size_read) / mFileSize;
                            int progressInt = (int) (progress * 100);
                            if (progressInt > 100) progressInt = 100;
                            onActionListener.onProgress(progressInt);
                        }
                    }
                    firstSampleData = false;
                }

                // 从解码器输出缓冲区获取解码流。
                int outputBufferIndex = codec.dequeueOutputBuffer(info, 100);
                if (outputBufferIndex >= 0 && info.size > 0) {
                    if (decodedSamplesSize < info.size) {
                        decodedSamplesSize = info.size;
                        decodedSamples = new byte[decodedSamplesSize];
                    }
                    outputBuffers[outputBufferIndex].get(decodedSamples, 0, info.size);
                    outputBuffers[outputBufferIndex].clear();
                    // 检查缓冲区是否足够大。 如果它太小，请调整它的大小。
                    if (mDecodedBytes.remaining() < info.size) {
                        // 粗略估计总大小，多分配 20%,
                        // 确保分配至少比初始大小多 5MB.
                        int position = mDecodedBytes.position();
                        int newSize = (int)((position * (1.0 * mFileSize / tot_size_read)) * 1.2);
                        if (newSize - position < info.size + 5 * (1<<20)) {
                            newSize = position + info.size + 5 * (1<<20);
                        }
                        ByteBuffer newDecodedBytes = null;
                        // 尝试分配内存
                        int retry = 10;
                        while(retry > 0) {
                            try {
                                newDecodedBytes = ByteBuffer.allocate(newSize);
                                break;
                            } catch (OutOfMemoryError oome) {
                                // setting android:largeHeap="true" in <application> seem to help not
                                // reaching this section.
                                retry--;
                            }
                        }
                        if (retry == 0) {
                            // Failed to allocate memory... Stop reading more data and finalize the
                            // instance with the data decoded so far.
                            break;
                        }
                        //ByteBuffer newDecodedBytes = ByteBuffer.allocate(newSize);
                        mDecodedBytes.rewind();
                        newDecodedBytes.put(mDecodedBytes);
                        mDecodedBytes = newDecodedBytes;
                        mDecodedBytes.position(position);
                    }
                    mDecodedBytes.put(decodedSamples, 0, info.size);
                    codec.releaseOutputBuffer(outputBufferIndex, false);
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    outputBuffers = codec.getOutputBuffers();
                }
//                else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                    //后续数据将遵循新的格式。
//                    // 可以检查codec.getOutputFormat()，这是需要的新的输出格式，
//                }
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
                        || (mDecodedBytes.position() / (2 * mChannels)) >= expectedNumSamples) {
                    //我们从解码器中获得了所有的解码数据。停止在这里。
                    // 理论上dequeueOutputBuffer(info，…)应该设置info。flags是MediaCodec.BUFFER_FLAG_END_OF_STREAM时，
                    // 一些手机(如三星S3)不会对某些文件(例如单声道AAC文件)这样做，在这种情况下，后续调用dequeueOutputBuffer可能导致应用程序崩溃
                    // 即使抛出一个异常…于是就有了第二个检测。
                    // (对于单AAC文件，S3实际上会加倍每个样本，就像流是立体声。最终的结果流只有预期的一半，而且有很多低音。
                    break;
                }
            }
            showLog("读取音乐源文件耗时================>" + (System.currentTimeMillis() - start1) + "ms");
            showLog("实际循环读取次数================>" + count);
            audioVolumeInfo.mNumSamples = mDecodedBytes.position() / (mChannels * 2);  // One sample = 2 bytes.
            mDecodedBytes.rewind();
            mDecodedBytes.order(ByteOrder.LITTLE_ENDIAN);
            ShortBuffer mDecodedSamples = mDecodedBytes.asShortBuffer();
            //audioVolumeInfo.mAvgBitRate = (int) ((audioVolumeInfo.mFileSize * 8) * ((float) mSampleRate / audioVolumeInfo.mNumSamples) / 1000);

            extractor.release();
            extractor = null;
            codec.stop();
            codec.release();
            codec = null;

            showLog("mNumSamples = " + audioVolumeInfo.mNumSamples);
            int samplesPerFrame = count/4;//320,512
            showLog("samplesPerFrame = " + samplesPerFrame);
            // Temporary hack to make it work with the old version.
            audioVolumeInfo.mNumFrames = audioVolumeInfo.mNumSamples / samplesPerFrame;
            if (audioVolumeInfo.mNumSamples % samplesPerFrame != 0) {
                audioVolumeInfo.mNumFrames++;
            }
            audioVolumeInfo.mFrameGains = new int[audioVolumeInfo.mNumFrames];
            int j;
            int gain, value;
            for (i = 0; i < audioVolumeInfo.mNumFrames; i++) {
                gain = -1;
                for (j = 0; j < samplesPerFrame; j++) {
                    value = 0;
                    for (int k = 0; k < mChannels; k++) {
                        if (mDecodedSamples.remaining() > 0) {
                            value += Math.abs(mDecodedSamples.get());
                        }
                    }
                    value /= mChannels;
                    if (gain < value) {
                        gain = value;
                    }
                }
                audioVolumeInfo.mFrameGains[i] = (int) Math.sqrt(gain);  // here gain = sqrt(max value of 1st channel)...
            }
            mDecodedSamples.rewind();

            prepareForView(audioVolumeInfo);
            if (onActionListener != null) {
                onActionListener.onSuccess(audioVolumeInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }
            if (codec != null) {
                codec.stop();
                codec.release();
                codec = null;
            }
        }
    }

    private void prepareForView(AudioVolumeInfo audioVolumeInfo) {
        int numFrames = audioVolumeInfo.getNumFrames();
        int[] frameGains = audioVolumeInfo.getFrameGains();
        showLog("numFrames = " + numFrames);
        //showLog("frameGains = " + Arrays.toString(frameGains));
        double[] smoothedGains = new double[numFrames];
        if (numFrames == 1) {
            smoothedGains[0] = frameGains[0];
        } else if (numFrames == 2) {
            smoothedGains[0] = frameGains[0];
            smoothedGains[1] = frameGains[1];
        } else if (numFrames > 2) {
            smoothedGains[0] = (frameGains[0] / 2.0) +
                    (frameGains[1] / 2.0);
            for (int i = 1; i < numFrames - 1; i++) {
                smoothedGains[i] = (frameGains[i - 1] / 3.0) +
                        (frameGains[i] / 3.0) +
                        (frameGains[i + 1] / 3.0);
            }
            smoothedGains[numFrames - 1] = (frameGains[numFrames - 2] / 2.0) +
                    (frameGains[numFrames - 1] / 2.0);
        }

        double maxGain = 1.0;
        for (int i = 0; i < numFrames; i++) {
            if (smoothedGains[i] > maxGain) {
                maxGain = smoothedGains[i];
            }
        }
        double scaleFactor = 1.0;
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain;
        }

        maxGain = 0;
        int[] gainHist = new int[256];
        for (int i = 0; i < numFrames; i++) {
            int smoothedGain = (int) (smoothedGains[i] * scaleFactor);
            if (smoothedGain < 0)
                smoothedGain = 0;
            if (smoothedGain > 255)
                smoothedGain = 255;
            if (smoothedGain > maxGain)
                maxGain = smoothedGain;

            gainHist[smoothedGain]++;
        }

        double minGain = 0;
        int sum = 0;
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[(int) minGain];
            minGain++;
        }

        sum = 0;
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[(int) maxGain];
            maxGain--;
        }
        if (maxGain <= 50) {
            maxGain = 80;
        } else if (maxGain > 50 && maxGain < 120) {
            maxGain = 142;
        } else {
            maxGain += 10;
        }


        double[] heights = new double[numFrames];
        double range = maxGain - minGain;
        for (int i = 0; i < numFrames; i++) {
            double value = (smoothedGains[i] * scaleFactor - minGain) / range;
            if (value < 0.0)
                value = 0.0;
            if (value > 1.0)
                value = 1.0;
            heights[i] = value * value;
        }

        audioVolumeInfo.mNumZoomLevels = 5;
        int[] mLenByZoomLevel = new int[5];
        double[][] mValuesByZoomLevel = new double[5][];

        // Level 0 is doubled, with interpolated values
        mLenByZoomLevel[0] = numFrames * 2;
        mValuesByZoomLevel[0] = new double[mLenByZoomLevel[0]];
        if (numFrames > 0) {
            mValuesByZoomLevel[0][0] = 0.5 * heights[0];
            mValuesByZoomLevel[0][1] = heights[0];
        }
        for (int i = 1; i < numFrames; i++) {
            mValuesByZoomLevel[0][2 * i] = 0.5 * (heights[i - 1] + heights[i]);
            mValuesByZoomLevel[0][2 * i + 1] = heights[i];
        }

        // Level 1 is normal
        mLenByZoomLevel[1] = numFrames;
        mValuesByZoomLevel[1] = new double[mLenByZoomLevel[1]];
        for (int i = 0; i < mLenByZoomLevel[1]; i++) {
            mValuesByZoomLevel[1][i] = heights[i];
        }

        // 3 more levels are each halved
        for (int j = 2; j < 5; j++) {
            mLenByZoomLevel[j] = mLenByZoomLevel[j - 1] / 2;
            mValuesByZoomLevel[j] = new double[mLenByZoomLevel[j]];
            for (int i = 0; i < mLenByZoomLevel[j]; i++) {
                mValuesByZoomLevel[j][i] =
                        0.5 * (mValuesByZoomLevel[j - 1][2 * i] +
                                mValuesByZoomLevel[j - 1][2 * i + 1]);
            }
        }


        if (numFrames > 5000) {
            audioVolumeInfo.mZoomLevel = 3;
        } else if (numFrames > 1000) {
            audioVolumeInfo.mZoomLevel = 2;
        } else if (numFrames > 300) {
            audioVolumeInfo.mZoomLevel = 1;
        } else {
            audioVolumeInfo.mZoomLevel = 0;
        }


        showLog("mZoomLevel = " + audioVolumeInfo.mZoomLevel);
        //showLog("mLenByZoomLevel = " + Arrays.toString(audioVolumeInfo.mLenByZoomLevel));
        //showLog("mValuesByZoomLevel = " + Arrays.deepToString(audioVolumeInfo.mValuesByZoomLevel));
        audioVolumeInfo.mHeightsAtThisZoomLevel = new double[mLenByZoomLevel[audioVolumeInfo.mZoomLevel]];
        for (int i = 0; i < mLenByZoomLevel[audioVolumeInfo.mZoomLevel]; i++) {
            audioVolumeInfo.mHeightsAtThisZoomLevel[i] =
                    mValuesByZoomLevel[audioVolumeInfo.mZoomLevel][i];
        }
        showLog("mHeightsAtThisZoomLevel length= " + audioVolumeInfo.mHeightsAtThisZoomLevel.length);
    }


    private void showLog(String str) {
        Log.e("audio volume", "音频:::" + str);
    }

}
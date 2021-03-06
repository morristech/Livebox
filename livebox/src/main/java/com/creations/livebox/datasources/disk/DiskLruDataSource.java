package com.creations.livebox.datasources.disk;

import com.creations.livebox.datasources.LocalDataSource;
import com.creations.livebox.util.Optional;
import com.creations.livebox.util.io.Utils;
import com.creations.livebox_common.serializers.Serializer;
import com.creations.livebox_common.util.Logger;
import com.instagram.igdiskcache.EditorOutputStream;
import com.instagram.igdiskcache.IgDiskCache;
import com.instagram.igdiskcache.OptionalStream;
import com.instagram.igdiskcache.SnapshotInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.concurrent.Executors;

import okio.BufferedSource;

import static com.creations.livebox.Livebox.TAG;
import static com.creations.livebox.util.Objects.nonNull;
import static com.creations.livebox_common.util.OkioUtils.bufferedSource;
import static com.creations.livebox_common.util.OkioUtils.copy;

/**
 * @author Sérgio Serra on 25/08/2018.
 * Criations
 * sergioserra99@gmail.com
 */
public class DiskLruDataSource<I, O> implements LocalDataSource<I, O> {

    private static DiskLruDataSource.Config mDiskCacheConfig;
    private LiveboxDiskCache mDiskCache;
    private Serializer mSerializer;
    private Type mType;

    private DiskLruDataSource(Serializer serializer, Type type) {
        mDiskCache = LiveboxDiskCache.getInstance(mDiskCacheConfig);
        mSerializer = serializer;
        mType = type;
    }

    public static <I, O> DiskLruDataSource<I, O> create(Serializer serializer, Type type) {
        return new DiskLruDataSource<>(serializer, type);
    }

    public static void setConfig(Config config) {
        mDiskCacheConfig = config;
    }

    @Override
    public Type getType() {
        return mType;
    }

    @Override
    public Optional<O> read(String key) {
        OptionalStream<SnapshotInputStream> iis = mDiskCache.get(key);
        Logger.d(TAG, "Read from disk cache is present: %s with key: %s", iis.isPresent(), key);
        if (iis.isPresent()) {
            O data = mSerializer.deserialize(bufferedSource(iis.get()), mType);
            Logger.d(TAG, "Data read from disk %s", data);
            return Optional.ofNullable(data);
        }
        return Optional.empty();
    }

    @Override
    public void save(String key, I input) {
        OptionalStream<EditorOutputStream> oos = mDiskCache.edit(key);
        Logger.d(TAG, "Save to disk cache is present: %s with key: %s", oos.isPresent(), key);
        if (oos.isPresent()) {
            try {
                BufferedSource s = mSerializer.serialize(input, mType);
                if (nonNull(s)) {
                    writeToCacheOutputStream(s, oos.get());
                    oos.get().commit();
                }
            } finally {
                oos.get().abortUnlessCommitted();
            }
        }
    }

    @Override
    public void clear(String key) {
        Logger.d(TAG, "Clear key: %s", key);
        mDiskCache.clear(key);
    }

    private void writeToCacheOutputStream(BufferedSource input, EditorOutputStream output) {
        OutputStream os = new BufferedOutputStream(output);
        try {
            copy(input.inputStream(), os);
            Logger.d(TAG, "---> Success data saved in diskLruDataSource.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeQuietly(input);
            Utils.closeQuietly(os);
        }
    }

    @Override
    public String toString() {
        return "DiskLruDataSource";
    }

    public static class Config {
        private File mCacheDir;
        private long mCacheSize;

        public Config(File cacheDir, long cacheSize) {
            mCacheDir = cacheDir;
            mCacheSize = Math.max(0, cacheSize);
        }

        File getCacheDir() {
            return mCacheDir;
        }

        long getCacheSize() {
            return mCacheSize;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "CacheDir=" + mCacheDir +
                    ", CacheSize=" + mCacheSize +
                    '}';
        }
    }

    private static class LiveboxDiskCache {

        private static LiveboxDiskCache instance;

        private Config mConfig;
        private IgDiskCache mDiskCache;

        private LiveboxDiskCache(Config config) {
            mConfig = config;
        }

        static LiveboxDiskCache getInstance(Config config) {
            if (instance == null) {
                synchronized (LiveboxDiskCache.class) {
                    if (instance == null) {
                        instance = new LiveboxDiskCache(config);
                    }
                }
            }
            return instance;
        }

        OptionalStream<EditorOutputStream> edit(String key) {
            return getDiskCache().edit(key);
        }

        OptionalStream<SnapshotInputStream> get(String key) {
            return getDiskCache().get(key);
        }

        void clear(String key) {
            mDiskCache.remove(key);
        }

        private IgDiskCache getDiskCache() {
            // lazy initialization of IgDiskCache to avoid calling it from the main UI thread.
            if (mDiskCache == null) {
                mDiskCache = new IgDiskCache(
                        mConfig.getCacheDir(),
                        mConfig.getCacheSize(),
                        Executors.newSingleThreadExecutor()
                );
            }
            return mDiskCache;
        }
    }

}

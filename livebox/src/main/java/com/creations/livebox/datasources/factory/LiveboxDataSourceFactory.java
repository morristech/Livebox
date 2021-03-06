package com.creations.livebox.datasources.factory;

import com.creations.livebox.datasources.LocalDataSource;
import com.creations.livebox.datasources.disk.DiskLruDataSource;
import com.creations.livebox.datasources.disk.DiskPersistentDataSource;
import com.creations.livebox.util.Optional;
import com.creations.livebox_common.serializers.Serializer;

import java.lang.reflect.Type;

import static com.creations.livebox.datasources.factory.LiveboxDataSourceFactory.Sources.DISK_LRU;
import static com.creations.livebox.datasources.factory.LiveboxDataSourceFactory.Sources.DISK_PERSISTENT;

/**
 * @author Sérgio Serra on 26/08/2018.
 * Criations
 * sergioserra99@gmail.com
 */
public final class LiveboxDataSourceFactory<I> implements DataSourceFactory<I> {

    private Serializer mSerializer;
    private Type mType;

    public LiveboxDataSourceFactory(Serializer serializer, Type type) {
        mType = type;
        mSerializer = serializer;
    }

    @Override
    public <T> Optional<LocalDataSource<I, T>> get(int id) {
        LocalDataSource<I, T> dataSource = null;
        switch (id) {
            case DISK_LRU:
                dataSource = DiskLruDataSource.create(mSerializer, mType);
                break;
            case DISK_PERSISTENT:
                dataSource = DiskPersistentDataSource.create(mSerializer, mType);
                break;
        }
        return Optional.ofNullable(dataSource);
    }

    public static abstract class Sources {
        public static final int DISK_LRU = 2000;
        public static final int DISK_PERSISTENT = 3000;
    }

}

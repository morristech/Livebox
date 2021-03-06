package com.creations.serializer_gson;

import com.creations.livebox_common.serializers.Serializer;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.InputStreamReader;
import java.lang.reflect.Type;

import okio.BufferedSource;

import static com.creations.livebox_common.util.OkioUtils.bufferedSource;
import static com.creations.livebox_common.util.OkioUtils.readerInputStreamUtf8;

/**
 * @author Sérgio Serra on 25/08/2018.
 * Criations
 * sergioserra99@gmail.com
 */
public class LiveboxGsonSerializer implements Serializer {

    private Gson mGson;

    private LiveboxGsonSerializer(Gson gson) {
        mGson = gson;
    }

    public static Serializer create() {
        return new LiveboxGsonSerializer(new Gson());
    }

    public static Serializer create(Gson gson) {
        return new LiveboxGsonSerializer(gson);
    }

    @Override
    public <T> BufferedSource serialize(T input, Type type) {
        try {
            return bufferedSource(readerInputStreamUtf8(mGson.toJson(input, type)));
        } catch (JsonIOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T deserialize(BufferedSource source, Type type) {
        if (source == null) {
            return null;
        }

        try {
            return mGson.fromJson(new JsonReader(new InputStreamReader(source.inputStream())), type);
        } catch (JsonIOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }
}

package com.olku.autoproxy.sample.builder;

import androidx.annotation.NonNull;

import com.ryanharter.auto.value.moshi.MoshiAdapterFactory;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonQualifier;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.ToJson;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/** Class to create moshi adapter factory for autoValue */
@MoshiAdapterFactory
@proguard.annotation.Keep
public abstract class MoshiFactory implements JsonAdapter.Factory {
    /** Static factory method to access the package, private generated implementation */
    @NonNull
    public static JsonAdapter.Factory create() {
        return new AutoValueMoshi_MoshiFactory();
    }

    /** Convert {@code null} value of JSON field/property to {@link #NONE_DOUBLE} or {@link #NONE_LONG} value. */
    @Retention(RetentionPolicy.RUNTIME)
    @JsonQualifier
    @proguard.annotation.Keep
    public @interface NullToNone {
        double NONE_DOUBLE = -1;
        long NONE_LONG = -1;
    }

    /** Skip/Ignore JSON field serialization if its value set to empty (zero, null). Only for Long and Double data types. */
    @Retention(RetentionPolicy.RUNTIME)
    @JsonQualifier
    @proguard.annotation.Keep
    public @interface SkipEmpty {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @JsonQualifier
    public @interface SkipMe {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @JsonQualifier
    public @interface ParseAsString {
    }

    /** @see <a href="https://github.com/square/moshi/issues/114">Null for primitive types</a> */
    @SuppressWarnings("unused")
    @proguard.annotation.Keep
    public static class CustomAdapters {
        @ToJson
        public void toJsonLong(final JsonWriter writer, @NullToNone long value) throws IOException {
            if (NullToNone.NONE_LONG == value) {
                writer.nullValue();
            } else {
                writer.value(value);
            }
        }

        @FromJson
        @NullToNone
        public long fromJsonLong(@NonNull final JsonReader reader) throws IOException {
            if (reader.peek() == JsonReader.Token.NUMBER) {
                return reader.nextLong();
            } else if (reader.peek() == JsonReader.Token.NULL) {
                reader.nextNull();
            }

            return NullToNone.NONE_LONG;
        }

        @ToJson
        public void toJsonDouble(final JsonWriter writer, @NullToNone double value) throws IOException {
            if (NullToNone.NONE_DOUBLE == value) {
                writer.nullValue();
            } else {
                writer.value(value);
            }
        }

        @FromJson
        @NullToNone
        public double fromJsonDouble(@NonNull final JsonReader reader) throws IOException {
            if (reader.peek() == JsonReader.Token.NUMBER) {
                return reader.nextDouble();
            } else if (reader.peek() == JsonReader.Token.NULL) {
                reader.nextNull();
            }

            return NullToNone.NONE_DOUBLE;
        }

        @ToJson
        public void toJsonEmptyDouble(final JsonWriter writer, @SkipEmpty final double value) throws IOException {
            if (value != 0.0) {
                writer.value(String.valueOf(value));
            } else {
                writer.nullValue();
            }
        }

        @FromJson
        @SkipEmpty
        public double fromJsonEmptyDouble(@NonNull final JsonReader reader) throws IOException {
            return reader.nextDouble();
        }

        @ToJson
        public void toJsonEmptyLong(final JsonWriter writer, @SkipEmpty final long value) throws IOException {
            if (value != 0) {
                writer.value(String.valueOf(value));
            } else {
                writer.nullValue();
            }
        }

        @FromJson
        @SkipEmpty
        public long fromJsonEmptyLong(@NonNull final JsonReader reader) throws IOException {
            return reader.nextLong();
        }

        @ToJson
        public void toJsonEmptyString(final JsonWriter writer, @SkipEmpty final String value) throws IOException {
            if (value != null) {
                writer.value(String.valueOf(value));
            } else {
                writer.nullValue();
            }
        }

        @FromJson
        @SkipEmpty
        public String fromJsonEmptyString(@NonNull final JsonReader reader) throws IOException {
            return reader.nextString();
        }

        @ToJson
        public void toJsonGeoJsonList(@NonNull final JsonWriter writer, @SkipMe final List<GeoJson> list) throws IOException {
            writer.nullValue();
        }

        @FromJson
        @SkipMe
        public List<GeoJson> fromJsonGeoJsonList(@NonNull final JsonReader reader) {
            return new ArrayList<>();
        }

        @ToJson
        public void toJsonGeoFeature(@NonNull final JsonWriter writer, @SkipMe final List<GeoJsonFeature> list) throws IOException {
            writer.nullValue();
        }

        @FromJson
        @SkipMe
        public List<GeoJsonFeature> fromJsonGeoFeature(@NonNull final JsonReader reader) {
            return new ArrayList<>();
        }

        @ToJson
        String toJsonCharSequence(@ParseAsString final CharSequence charSequence) {
            return charSequence.toString();
        }

        @FromJson
        @ParseAsString
        CharSequence fromJsonCharSequence(final String string) {
            return string;
        }
    }

}

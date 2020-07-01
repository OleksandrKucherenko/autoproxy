package com.olku.autoproxy.sample.builder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Web API error report from server side.
 *
 * @see <a href="http://imgur.com/bx8w6XZ"><img src="http://i.imgur.com/bx8w6XZ.png" title="source: imgur.com" /></a>
 * @see <a href="http://bytes.babbel.com/en/articles/2016-03-16-retrofit2-rxjava-error-handling.html">Another approach</a>
 */
public class ErrorResponse {
    /** Should be used instead of NULL. */
    public static final ErrorResponse UNKNOWN_DATA = new ErrorResponse();

    /** Capture error. */
    @Json(name = "error")
    @Nullable
    public InnerError error;

    /** Is error fields empty?! */
    public boolean isErrorEmpty() {
        return null == error || null == error.message;
    }

    /** compose inner error from code and message. */
    @NonNull
    public static InnerError error(final long code, @Nullable final String message) {
        return error(code, message, null);
    }

    /** compose inner error from code, message and URL. */
    @NonNull
    public static InnerError error(final long code, @Nullable final String message, @Nullable final String url) {
        final InnerError instance = new InnerError();
        instance.code = code;
        instance.message = message;
        instance.url = url;

        return instance;
    }

    /** compose inner error from exception information. */
    @NonNull
    public static InnerError error(@NonNull final Throwable exception) {
        final InnerError instance = new InnerError();
        instance.code = -1;
        instance.message = exception.getMessage();

        if (null != exception.getCause()) {
            instance.reasons = new LinkedList<>();
            instance.reasons.add(error(exception.getCause()));
        }

        return instance;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error=" + error +
                '}';
    }

    /** Inner error structure that we can receive from server side. */
    public static class InnerError {
        /** Error numeric code. */
        @Json(name = "code")
        public long code;
        /** Error message in english. */
        @Json(name = "message")
        public String message;

        /** Optional. Link to more details. Often a link to API documentation. */
        @Json(name = "url")
        public String url;
        /** Optional. Inner reasons of failure. */
        @Json(name = "reasons")
        public List<InnerError> reasons;
        /** Optional. Message in multiple languages. */
        @Json(name = "localization")
        public Map<String, String> localization;

        @Json(name = "params")
        public Params params;

        @Nullable
        public StartParkingError getStartParkingErrorParams() {
            if (params instanceof StartParkingError) {
                return (StartParkingError) params;
            }
            return null;
        }

        @Override
        public String toString() {
            return "InnerError{" +
                    "code=" + code +
                    ", message='" + message + '\'' +
                    ", url='" + url + '\'' +
                    ", reasons=" + reasons +
                    ", localization=" + localization +
                    ", params=" + params +
                    '}';
        }
    }
}

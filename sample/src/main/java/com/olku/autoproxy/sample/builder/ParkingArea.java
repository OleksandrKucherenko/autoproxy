package com.olku.autoproxy.sample.builder;

import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.olku.annotations.AutoProxy;
import com.olku.annotations.Returns;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/** @see <a href="https://goo.gl/2PnUoy">Sample Data</a> */
@SuppressWarnings({"NullableProblems", "unused"})
@AutoValue
public abstract class ParkingArea extends ErrorResponse implements Parcelable {
    /** Parking area ID associated with NOT existing area. used by {@link #NOT_FOUND}. */
    public static final long NO_AREA = 0;
    /** ID that used by {@link #ZOOMED_OUT} instance. */
    public static final long WRONG_ZOOM_LEVEL = -200;

    /** Empty instance that used as a stub for cache logic. */
    public static final ParkingArea NOT_FOUND = ParkingArea.builder().id(NO_AREA).build();
    /** instance for error condition triggering. */
    public static final ParkingArea ZOOMED_OUT = ParkingArea.builder().id(WRONG_ZOOM_LEVEL).build();

    /** Area type. */
    private static final String ON_STREET = "OnStreet";
    /** Area type. */
    private static final String GARAGE = "UndergroundGarage";
    /** Area type. */
    private static final String CAMERA_PARK = "CameraParkArea";
    /** Area type. */
    private static final String QUICK_CARD = "QuickCardArea";
    /** EVC */
    private static final String EVC = "EVC";
    /** Sticker required. */
    public static final String STICKER = "EASYPARK_STICKER_REQUIRED";
    /** Handwritten node required. */
    public static final String HANDWRITTEN = "HANDWRITTEN_NOTE_REQUIRED";
    /** Inactive status. */
    public static final String INACTIVE = "INACTIVE";
    /** compare Parking areas by size. */
    public static final Comparator<ParkingArea> BY_SIZE = (o1, o2) -> Double.compare(o1.getSize(), o2.getSize());

    public transient RuntimeData runtime = new RuntimeData();

    @Json(name = "id")
    public abstract long id();

    @Json(name = "areaNo")
    public abstract long areaNumber();

    @Json(name = "areaName")
    @Nullable
    public abstract String areaName();

    @Nullable // FIXME (olku): temporary
    @Json(name = "status")
    public abstract String areaStatus();

    @Nullable // FIXME (olku): temporary
    @Json(name = "areaType")
    public abstract String areaType();

    @Nullable // FIXME (olku): temporary
    @Json(name = "countryCode")
    public abstract String areaCountryCode();

    @Nullable
    @Json(name = "accessType")
    public abstract String areaAccessType();

    @Nullable
    @Json(name = "displayPoint")
    public abstract GeoPoint areaDisplayPoint();

    @Nullable
    @Json(name = "areaInSquareMeters")
    public abstract String areaInSqm();

    @Nullable
    @Json(name = "stickerInWindowType")
    public abstract String stickerInWindowType();

    @Nullable
    @Json(name = "parkingOperatorStickerType")
    public abstract String parkingOperatorStickerType();

    @Nullable
    @Json(name = "geoJson")
    public abstract List<String> geoJson();

    @Nullable
    @Json(name = "geoJsonUrl")
    public abstract String geoJsonUrl();

    @Nullable
    @Json(name = "kmlUrl")
    public abstract String geoKmlUrl();

    @MoshiFactory.NullToNone
    @Json(name = "parkingOperatorId")
    public abstract long operatorId();

    @Json(name = "parkingOperatorName")
    @Nullable
    public abstract String operatorName();

    @Nullable
    @Json(name = "parkingTypes")
    public abstract List<ParkingType> parkingTypes();

    @Json(name = "multipleChoice")
    public abstract boolean isMultiChoice();

    @Json(name = "hasParkingSpots")
    public abstract boolean hasParkingSpots();

    @Nullable
    @Json(name = "parkingSpots")
    public abstract List<ParkingAreaSpot> parkingSpots();

    @Nullable
    @Json(name = "multipleChoiceDetails")
    public abstract MultiDetails multiOptions();

    @Json(name = "showPopUpMessage")
    public abstract boolean showPopUpMessage();

    @Nullable
    @Json(name = "popUpMessageKey")
    public abstract String popUpMessageKey();

    @Nullable
    @Json(name = "popUpMessage")
    public abstract String popUpMessage();

    @Nullable
    @Json(name = "city")
    public abstract String city();

    @Nullable
    @Json(name = "priceInfo")
    public abstract String priceInfo();

    @Json(name = "gatedAnprAccess")
    public abstract boolean gatedAnprAccess();

    @Json(name = "automaticStartAllowed")
    public abstract boolean automaticStartAllowed();

    @Json(name = "requestChildAreas")
    public abstract boolean requestChildAreas();

    @Json(name = "externallyRated")
    public abstract boolean isExternallyRated();

    /** Create builder from current paring area. */
    abstract Builder innerToBuilder();

    /** Get instance of builder from current instance. */
    @NonNull
    public final Builder toBuilder() {
        return new WithRuntimeDataBuilder(this);
    }

    /** To/From JSON serialization adapter. */
    @NonNull
    public static JsonAdapter<ParkingArea> json(@NonNull final Moshi moshi) {
        return new AutoValue_ParkingArea.MoshiJsonAdapter(moshi);
    }

    /** Create instance of builder. */
    @NonNull
    public static Builder builder() {
        // required non-null default values
        return new $AutoValue_ParkingArea.Builder()
                .id(-1)
                .areaName("")
                .areaNumber(-1)
                .operatorId(-1)
                .isMultiChoice(false)
                .hasParkingSpots(false)
                .parkingSpots(new ArrayList<>())
                .showPopUpMessage(false)
                .popUpMessage("")
                .gatedAnprAccess(false)
                .automaticStartAllowed(false)
                .requestChildAreas(false)
                .isExternallyRated(false);
    }

    /** Check if provided type can be recognized as 'on street' parking area type. */
    public static boolean isOnStreet(@Nullable final String type) {
        return ON_STREET.equalsIgnoreCase(type);
    }

    /** Check if provided type can be recognized as 'garage' parking area type. */
    public static boolean isGarage(@Nullable final String type) {
        return GARAGE.equalsIgnoreCase(type);
    }

    /** Check if provided type can be recognized as 'camera park' parking area type. */
    public static boolean isCameraPark(@Nullable final String type) {
        return CAMERA_PARK.equalsIgnoreCase(type);
    }

    /** Check if provided type can be recognized as 'quick card' parking area type. */
    public static boolean isQuickCard(@Nullable final String type) {
        return QUICK_CARD.equalsIgnoreCase(type);
    }

    /** Check if provided type can be recognized as 'EVC' parking area type. */
    public static boolean isEVC(@Nullable final String type) {
        return EVC.equalsIgnoreCase(type);
    }

    /** Check if an are ID is valid parking area id */
    public static boolean isValidAreaId(final long areaId) {
        return areaId > 0;
    }

    /** Is Parking area Inactive. */
    public boolean isInactive() {
        return INACTIVE.equalsIgnoreCase(areaStatus());
    }

    /** Is sticker required for this parking area or not? */
    public boolean isSticker() {
        return STICKER.equalsIgnoreCase(stickerInWindowType());
    }

    /** Is handwritten note required for this parking area or not? */
    public boolean isHandwritten() {
        return HANDWRITTEN.equalsIgnoreCase(stickerInWindowType());
    }

    /** Is parking area has ANPR parking type tag or not. */
    public boolean isAnpr() {
        throw new AssertionError("Not implemented");
    }

    /** Does the parking area have bucket parking type or not */
    public boolean isBucket() {
        throw new AssertionError("Not implemented");
    }

    /** Is parking are treated as empty. */
    public boolean isEmpty() {
        return NOT_FOUND == this || NO_AREA == id();
    }

    /** Is parking area type 'on street' or not. */
    public boolean isOnStreet() {
        throw new AssertionError("Not implemented");
    }

    /** Calculate size of the parking area based on it largest geo-polygon. */
    public double getSize() {
        throw new AssertionError("Not implemented");
    }

    /** Is provided bounds intersects with parking area polygons. Fast but not accurate. */
    public boolean intersectsFast(@NonNull final LatLngBounds bounds, final ParkingArea.Config config) {
        throw new AssertionError("Not implemented");
    }

    /** Is provided point is inside of any polygon defined in the parking area. Slow, accurate. */
    public boolean containsExact(@NonNull final LatLng point, final ParkingArea.Config config) {
        throw new AssertionError("Not implemented");
    }

    /** Removes UNKNOWN parking type from collection and return new instance of parking area with left parking types. */
    @NonNull
    public ParkingArea withSafeParkingTypes() {
        throw new AssertionError("Not implemented");
    }

    /** Is parking types collection not empty. */
    public boolean hasParkingTypes() {
        throw new AssertionError("Not implemented");
    }

    public List<ParkingAreaSpot> getParkingSpots() {
        throw new AssertionError("Not implemented");
    }

    public boolean hasParkingSpotsList() {
        throw new AssertionError("Not implemented");
    }

    @NonNull
    public List<MultiOption> innerMultiOptions() {
        throw new AssertionError("Not implemented");
    }

    /** Is any extra price information  exist. */
    public boolean hasPriceInfo() {
        return !TextUtils.isEmpty(priceInfo());
    }

    public boolean hasTariffDetails() {
        throw new AssertionError("Not implemented");
    }

    /** Get the parsed geo-json polygons. */
    @NonNull
    public synchronized List<GeoJson> parsedGeoJson() {
        throw new AssertionError("Not implemented");
    }

    /** Get the parsed geo-json polygons as features. */
    @NonNull
    public synchronized List<GeoJsonFeature> parsedFeatures(@NonNull final ParkingArea.Config config) {
        throw new AssertionError("Not implemented");
    }

    @Override
    public String toString() {
        return "ParkingArea{" +
                "  id: " + id() +
                ", number: " + areaNumber() +
                ", name:" + areaName() +
                ", type:" + areaType() +
                ", country:" + areaCountryCode() +
                ", geo-json: " + (null == geoJson() ? "null" : geoJson().size()) +
                ", size:" + areaInSqm() + "/" + getSize() +
                ", sticker:" + parkingOperatorStickerType() +
                ", in-window:" + stickerInWindowType() +
                ", parking-types:" + (null == parkingTypes() ? "null" : Arrays.toString(parkingTypes().toArray())) +
                ", parking-spots:" + (null == parkingSpots() ? "null" : Arrays.toString(parkingSpots().toArray())) +
                "}";
    }

    /** Builder of parking area. */
    @AutoValue.Builder
    @AutoProxy(defaultYield = Returns.THIS, flags = AutoProxy.Flags.AFTER_CALL)
    public static abstract class Builder {

        @NonNull
        @AutoProxy.Yield // override defaultYield to THROWS
        public abstract ParkingArea build();

        @NonNull
        public abstract Builder id(final long id);

        @NonNull
        public abstract Builder areaNumber(final long areaNumber);

        @NonNull
        public abstract Builder areaName(final String areaName);

        @NonNull
        public abstract Builder operatorId(final long operatorId);

        @NonNull
        public abstract Builder operatorName(final String operatorName);

        @NonNull
        public abstract Builder areaType(final String areaType);

        @NonNull
        public abstract Builder areaCountryCode(final String areaCountryCode);

        @NonNull
        public abstract Builder areaInSqm(final String areaInSqm);

        @NonNull
        public abstract Builder geoJson(final List<String> geoJson);

        @NonNull
        public abstract Builder geoJsonUrl(final String geoJsonUrl);

        @NonNull
        public abstract Builder geoKmlUrl(final String geoKmlUrl);

        @NonNull
        public abstract Builder areaStatus(final String status);

        @NonNull
        public abstract Builder areaAccessType(final String areaAccessType);

        @NonNull
        public abstract Builder areaDisplayPoint(final GeoPoint areaDisplayPoint);

        @NonNull
        public abstract Builder parkingTypes(final List<ParkingType> parkingTypes);

        @NonNull
        public abstract Builder parkingOperatorStickerType(final String parkingOperatorStickerType);

        @NonNull
        public abstract Builder stickerInWindowType(final String stickerInWindowType);

        @NonNull
        public abstract Builder isMultiChoice(final boolean isMultiChoice);

        @NonNull
        public abstract Builder hasParkingSpots(final boolean hasParkingSpots);

        @NonNull
        public abstract Builder parkingSpots(final List<ParkingAreaSpot> spots);

        @NonNull
        public abstract Builder multiOptions(final MultiDetails data);

        @NonNull
        public abstract Builder city(final String city);

        @NonNull
        public abstract Builder priceInfo(final String priceInfo);

        @NonNull
        public abstract Builder showPopUpMessage(final boolean showPopUpMessage);

        @NonNull
        public abstract Builder popUpMessageKey(final String popUpMessageKey);

        @NonNull
        public abstract Builder popUpMessage(final String popUpMessage);

        @NonNull
        public abstract Builder gatedAnprAccess(final boolean gatedAnprAccess);

        @NonNull
        public abstract Builder automaticStartAllowed(final boolean automaticStartAllowed);

        @NonNull
        public abstract Builder requestChildAreas(final boolean requestChildAreas);

        @NonNull
        public abstract Builder isExternallyRated(final boolean isExternallyRated);
    }

    /** Runtime data of the parking area. */
    public class RuntimeData {
        /** Geo features. */
        public final List<GeoJsonFeature> features = new ArrayList<>();
        /** Geo JSON parsed instances. */
        public final List<GeoJson> geoJson = new ArrayList<>();
        /** Runtime Parking Types. */
        public final List<ParkingType> parkingTypes = new ArrayList<>();
        /** Runtime tariffs. */
        public final List<Tariff> tariffs = new ArrayList<>();
    }

    /** Parking Area geo-polygon processing configuration. */
    public interface Config {
        double getRadius(@NonNull final ParkingArea area);

        double getRadius(@NonNull final GeoJsonFeature feature);
    }

    /** Builder with inject of runtime data. */
    @SuppressWarnings("unchecked")
    private static class WithRuntimeDataBuilder extends Proxy_ParkingArea$Builder {
        /** Instance of parking area. */
        private final ParkingArea instance;

        /** Inject constructor. */
        WithRuntimeDataBuilder(@NonNull final ParkingArea instance) {
            super(instance.innerToBuilder());

            this.instance = instance;
        }

        /** Allow all calls. */
        @Override
        public boolean predicate(@NonNull final String methodName, final Object... args) {
            return true; /* allow all calls */
        }

        /** capture 'build' call and inject runtime data from previous instance. */
        @Override
        public <R> R afterCall(@NonNull final String methodName, final R result) {
            if (M.BUILD.equals(methodName) && result instanceof ParkingArea) {
                ((ParkingArea) result).runtime = instance.runtime;

                return result;
            }

            return (R) this; // return this instance of Builder
        }
    }
}


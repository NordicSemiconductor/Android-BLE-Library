package no.nordicsemi.android.ble.common.callback.cgm;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.common.profile.cgm.CGMTypes;

/** @noinspection unused*/
public class ContinuousGlucoseMeasurementResponseItem implements Parcelable {
    private float glucoseConcentration;
    @Nullable
    private Float trend;
    @Nullable
    private Float quality;
    @Nullable
    private CGMTypes.CGMStatus status;
    private int timeOffset;

    public ContinuousGlucoseMeasurementResponseItem(
            float glucoseConcentration,
            @Nullable Float trend,
            @Nullable Float quality,
            @Nullable CGMTypes.CGMStatus status,
            int timeOffset
    ) {
        this.glucoseConcentration = glucoseConcentration;
        this.trend = trend;
        this.quality = quality;
        this.status = status;
        this.timeOffset = timeOffset;
    }


    public float getGlucoseConcentration() {
        return glucoseConcentration;
    }

    @Nullable
    public Float getTrend() {
        return trend;
    }

    @Nullable
    public Float getQuality() {
        return quality;
    }

    @Nullable
    public CGMTypes.CGMStatus getStatus() {
        return status;
    }

    public int getTimeOffset() {
        return timeOffset;
    }

    public void setGlucoseConcentration(float glucoseConcentration) {
        this.glucoseConcentration = glucoseConcentration;
    }

    public void setTrend(@Nullable Float trend) {
        this.trend = trend;
    }

    public void setQuality(@Nullable Float quality) {
        this.quality = quality;
    }

    public void setStatus(@Nullable CGMTypes.CGMStatus status) {
        this.status = status;
    }

    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }

    ContinuousGlucoseMeasurementResponseItem(final Parcel in) {
        glucoseConcentration = in.readFloat();
        if (in.readByte() == 0) {
            trend = null;
        } else {
            trend = in.readFloat();
        }
        if (in.readByte() == 0) {
            quality = null;
        } else {
            quality = in.readFloat();
        }
        if (in.readByte() == 0) {
            status = null;
        } else {
            final int warningStatus = in.readInt();
            final int calibrationTempStatus = in.readInt();
            final int sensorStatus = in.readInt();
            status = new CGMTypes.CGMStatus(warningStatus, calibrationTempStatus, sensorStatus);
        }
        timeOffset = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(glucoseConcentration);
        if (trend == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeFloat(trend);
        }
        if (quality == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeFloat(quality);
        }
        if (status == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(status.warningStatus);
            dest.writeInt(status.calibrationTempStatus);
            dest.writeInt(status.sensorStatus);
        }
        dest.writeInt(timeOffset);
    }

    public static final Creator<ContinuousGlucoseMeasurementResponseItem> CREATOR = new Creator<>() {
        @Override
        public ContinuousGlucoseMeasurementResponseItem createFromParcel(final Parcel in) {
            return new ContinuousGlucoseMeasurementResponseItem(in);
        }

        @Override
        public ContinuousGlucoseMeasurementResponseItem[] newArray(final int size) {
            return new ContinuousGlucoseMeasurementResponseItem[size];
        }
    };
}

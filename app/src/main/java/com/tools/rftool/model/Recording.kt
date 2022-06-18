package com.tools.rftool.model

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDateTime
import java.time.ZoneOffset

data class Recording(val date: LocalDateTime, val fileName: String, val sampleRate: Int, val centerFrequency: Int, val size: Long) : Parcelable {
    companion object CREATOR: Parcelable.Creator<Recording?> {
        override fun createFromParcel(source: Parcel): Recording? {
            return Recording(
                LocalDateTime.ofEpochSecond(source.readLong(), 0, ZoneOffset.UTC),
                source.readString() ?: "",
                source.readInt(),
                source.readInt(),
                source.readLong()
            )
        }

        override fun newArray(size: Int): Array<Recording?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(date.toEpochSecond(ZoneOffset.UTC))
        dest.writeString(fileName)
        dest.writeInt(sampleRate)
        dest.writeInt(centerFrequency)
        dest.writeLong(size)
    }
}
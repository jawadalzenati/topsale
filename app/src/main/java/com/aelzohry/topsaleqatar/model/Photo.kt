package com.aelzohry.topsaleqatar.model

import android.os.Parcelable
import com.aelzohry.topsaleqatar.helper.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Photo(
    val _id: String,
    val org: String,
    val thumb: String
): Parcelable {
    val orgUrl: String
        get() = this.org.toFilePath()

    val thumbUrl: String
        get() = this.thumb.toFilePath()
}
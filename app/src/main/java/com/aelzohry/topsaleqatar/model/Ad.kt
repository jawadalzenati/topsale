package com.aelzohry.topsaleqatar.model

import android.content.Context
import android.os.Parcelable
import com.aelzohry.topsaleqatar.R
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ad(
    val _id: String,
    val title: String,
    val details: String?,
    val price: Double,
    val isFixed: Boolean,
    var isLiked: Boolean,
    var isActive: Boolean?,
    @SerializedName("status") val _status: String,
    val rejectReason: LocalizedModel?,
    var isFavourite: Boolean,
    val canRepublish: Boolean,
    var commentsCount: Int,
    var viewsCount: Int,
    var likesCount: Int,
    val publishedAt: String,
    val numOfRooms: Int?,
    val numOfBathroom: Int?,
    val location:String?,
    val user: User?,
    val category: Category?,
    val region: LocalStanderModel?,
    val carMake: StanderModel?,
    val carModel: StanderModel?,
    val carYear: String?,
    val km: String?,
    val type: LocalStanderModel?,
    val subcategory: LocalStanderModel?,
    val photos: ArrayList<Photo>
) : Parcelable, AdBanner(_id) {
    val imageUrl: String?
        get() = this.photos.firstOrNull()?.thumbUrl

    val status: AdStatus
        get() = AdStatus.getBy(_status)

}

enum class AdStatus {
    ACTIVE, NOTACTIVE, PENDINGAPPROVAL, REJECTED;

    fun getTitle(context: Context): String {
        return context.getString(
            when (this) {
                ACTIVE -> R.string.active
                NOTACTIVE -> R.string.not_active
                PENDINGAPPROVAL -> R.string.pending_approval
                REJECTED -> R.string.rejected
            }
        )
    }

    companion object {
        fun getBy(status: String): AdStatus {
            return when (status) {
                "not_active" -> NOTACTIVE
                "pending_approval" -> PENDINGAPPROVAL
                "rejected" -> REJECTED
                else -> ACTIVE
            }
        }
    }
}
package com.easy.trip.models
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Bookmarks",
    indices = [
        Index(name="idx_place_id", value = ["placeId"], unique = true),
        Index(name="idx_location", value = ["latitude", "longitude"])
    ]
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var placeId: String?,
    var name: String?,
    var address: String?,
    var phone: String?,
    var latitude: Double,
    var longitude: Double,
    var comment: String?
): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeLong(id)
        dest?.writeString(placeId)
        dest?.writeString(name)
        dest?.writeString(address)
        dest?.writeString(phone)
        dest?.writeDouble(latitude)
        dest?.writeDouble(longitude)
        dest?.writeString(comment)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<Bookmark> {
        override fun createFromParcel(parcel: Parcel): Bookmark {
            return Bookmark(parcel)
        }

        override fun newArray(size: Int): Array<Bookmark?> {
            return arrayOfNulls(size)
        }
    }
}
import com.google.android.gms.maps.model.LatLng

data class ItemCard(
    val id:Int=0,
    var item_name: String?=null,
    var item_image_url: String? = null,
    var item_date: String? = null,
    var latLng: LatLng? = null,
    var googleMapsDirectionsUrl: String? = null
)
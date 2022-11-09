package edu.ap.pooclueapplication.ui.map

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import edu.ap.pooclueapplication.databinding.FragmentMapBinding
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.log

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val map = binding.mapview
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
                locationOverlay.enableMyLocation()
                locationOverlay.enableFollowLocation()
                map.overlays.add(locationOverlay)
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
                locationOverlay.enableMyLocation()
                locationOverlay.enableFollowLocation()
                map.overlays.add(locationOverlay)
            } else -> {
            // No location access granted.
        }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Configuration.getInstance().setUserAgentValue("Poo Clue")
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val map = binding.mapview
        map.setMultiTouchControls(true);
        map.setClickable(true);
        map.setUseDataConnection(true);
        map.controller.setZoom(18.0)
        map.minZoomLevel = 10.0




        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val map = binding.mapview

        try
        {
            val assetManager = requireContext().assets
            val inputStream = assetManager.open("openbaar_toilet.geojson")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)
            val jsonObject = JSONObject(json)
            val features = jsonObject.getJSONArray("features")
            for (i in 0 until features.length()) {
                val geometry = features.getJSONObject(i).getJSONObject("geometry")
                val coordinates = geometry.getJSONArray("coordinates")

                // LONGITUDE: COORDINATES[0]
                // LATITUDE: COORDINATES[1]

                Log.d("LATITUDE", "coordinates: ${coordinates[1]}")
                Log.d("LONGITUDE", "coordinates: ${coordinates[0]}")

                // place marker on osm map
                val marker = Marker(map)
                marker.position = GeoPoint(coordinates[1].toString().toDouble(), coordinates[0].toString().toDouble())
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                map.overlays.add(marker)



            }
        }
        catch (e: Exception)
        {
            Log.e("ERROR", "ERROR WHILE LOADING GEOJSON");
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
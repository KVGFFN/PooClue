package edu.ap.pooclueapplication.ui.map

import android.Manifest
import android.os.Bundle
import android.os.StrictMode
import android.provider.BaseColumns
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import edu.ap.pooclueapplication.ToiletContract
import edu.ap.pooclueapplication.ToiletDbHelper
import edu.ap.pooclueapplication.databinding.FragmentMapBinding
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val map = binding.mapview
        map.controller.setCenter(GeoPoint(51.22036305695485, 4.401488873168448))
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
        val dbHelper = ToiletDbHelper(this.requireContext())

        try
        {
            if (dbHelper.readToilets().count==0) {
                var json = ""
                val policy = StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
                StrictMode.setThreadPolicy(policy)
                val url = URL("https://geodata.antwerpen.be/arcgissql/rest/services/P_Portal/portal_publiek1/MapServer/8/query?outFields=*&where=1%3D1&f=geojson")
                val connection = url.openConnection()
                BufferedReader(InputStreamReader(connection.getInputStream())).use { inp ->
                    var line: String?
                    while (inp.readLine().also { line = it } != null) {
                        json += line.toString()
                    }
                }
                val jsonObject = JSONObject(json)
                val features = jsonObject.getJSONArray("features")
                for (i in 0 until features.length()) {
                    val geometry = features.getJSONObject(i).getJSONObject("geometry")
                    val coordinates = geometry.getJSONArray("coordinates")
                    dbHelper.writeToilet(
                        coordinates[0].toString().toDouble(),
                        coordinates[1].toString().toDouble()
                    )
                }
            }
            val cursor = dbHelper.readToilets()
            with(cursor) {
                while (moveToNext()) {
                    val longitude = getDouble(getColumnIndex(ToiletContract.ToiletEntry.COLUMN_NAME_LONGITUDE))
                    val latitude = getDouble(getColumnIndex(ToiletContract.ToiletEntry.COLUMN_NAME_LATITUDE))
                    val marker = Marker(map)
                    marker.position = GeoPoint(latitude, longitude)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    map.overlays.add(marker)
                }
            }
            cursor.close()
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
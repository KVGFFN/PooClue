package edu.ap.pooclueapplication.ui.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.get
import androidx.core.view.plusAssign
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.ap.pooclueapplication.R
import edu.ap.pooclueapplication.ToiletContract
import edu.ap.pooclueapplication.ToiletDbHelper
import edu.ap.pooclueapplication.databinding.FragmentListBinding
import edu.ap.pooclueapplication.ui.map.MapFragment.Companion.hasLocation
import edu.ap.pooclueapplication.ui.map.MapFragment.Companion.location
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("Range")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?





    ): View {
        val listViewModel =
            ViewModelProvider(this).get(ListViewModel::class.java)

        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // get current location via GPS
        val currentLocation = location

        var listAddresses = binding.listAddresses
        val dbHelper = ToiletDbHelper(requireContext());
        var arrayAdapter: ArrayAdapter<*>
        var addresses = arrayOf<String>()

        val cursor = dbHelper.readToilets()
        with(cursor) {
            while (moveToNext()) {
                val address = getString(getColumnIndex(ToiletContract.ToiletEntry.COLUMN_NAME_ADDRESS))
                val long = getString(getColumnIndex(ToiletContract.ToiletEntry.COLUMN_NAME_LONGITUDE))
                val lat = getString(getColumnIndex(ToiletContract.ToiletEntry.COLUMN_NAME_LATITUDE))

                if (hasLocation && currentLocation != null) {
                    var distance = currentLocation?.distanceToAsDouble(GeoPoint(lat.toDouble(), long.toDouble()))
                    // round distance up
                    addresses += address.toString() + "\n" + distance?.toInt().toString() + " meters away";
                }
                else
                {
                    addresses += address.toString()
                }

                arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, addresses)
                listAddresses.adapter = arrayAdapter
            }
        }
        cursor.close()
        binding.swiperefresh.setOnRefreshListener {
            dbHelper.clearDatabase()

            var json = ""
            val policy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
            StrictMode.setThreadPolicy(policy)
            val url =
                URL("https://geodata.antwerpen.be/arcgissql/rest/services/P_Portal/portal_publiek1/MapServer/8/query?outFields=*&where=1%3D1&f=geojson")
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
                    coordinates[1].toString().toDouble(),
                    features.getJSONObject(i).getJSONObject("properties")
                        .getString("DOELGROEP"),
                    features.getJSONObject(i).getJSONObject("properties")
                        .getString("INTEGRAAL_TOEGANKELIJK"),
                    features.getJSONObject(i).getJSONObject("properties")
                        .getString("LUIERTAFEL"),
                    features.getJSONObject(i).getJSONObject("properties")
                        .getString("STRAAT") + " "
                            + features.getJSONObject(i).getJSONObject("properties")
                        .getString("HUISNUMMER") + " "
                            + features.getJSONObject(i).getJSONObject("properties")
                        .getString("POSTCODE"),
                    )
            }

            binding.swiperefresh.isRefreshing = false
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
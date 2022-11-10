package edu.ap.pooclueapplication.ui.list

import android.annotation.SuppressLint
import android.os.Bundle
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
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

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



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
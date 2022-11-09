package edu.ap.pooclueapplication.ui.filter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.ap.pooclueapplication.ToiletContract
import edu.ap.pooclueapplication.ToiletDbHelper
import edu.ap.pooclueapplication.databinding.FragmentFilterBinding

class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStop() {
        val manCheckBox = binding.manCheckBox
        val womanCheckBox = binding.womanCheckBox
        val manWomanCheckBox = binding.manWomanCheckBox
        val wheelchairCheckBox = binding.wheelchairCheckBox

        ToiletDbHelper.selection = ""
        ToiletDbHelper.selectionArgs = arrayOf()

        if (manCheckBox.isChecked)
        {
            ToiletDbHelper.selection += "OR ${ToiletContract.ToiletEntry.COLUMN_NAME_TARGET} = ? "
            ToiletDbHelper.selectionArgs = ToiletDbHelper.selectionArgs.plus("man")
            addWheelchairAccess(wheelchairCheckBox.isChecked)
        }
        if (womanCheckBox.isChecked)
        {
            ToiletDbHelper.selection += "OR ${ToiletContract.ToiletEntry.COLUMN_NAME_TARGET} = ? "
            ToiletDbHelper.selectionArgs = ToiletDbHelper.selectionArgs.plus("vrouw")
            addWheelchairAccess(wheelchairCheckBox.isChecked)
        }
        if (manWomanCheckBox.isChecked)
        {
            ToiletDbHelper.selection += "OR ${ToiletContract.ToiletEntry.COLUMN_NAME_TARGET} = ? "
            ToiletDbHelper.selectionArgs = ToiletDbHelper.selectionArgs.plus("man/vrouw")
            addWheelchairAccess(wheelchairCheckBox.isChecked)
        }
        super.onStop()
    }

    fun addWheelchairAccess(wheelchair: Boolean)
    {
        if (wheelchair)
        {
            ToiletDbHelper.selection += "AND ${ToiletContract.ToiletEntry.COLUMN_NAME_WHEELCHAIR} = ? "
            ToiletDbHelper.selectionArgs = ToiletDbHelper.selectionArgs.plus("ja")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
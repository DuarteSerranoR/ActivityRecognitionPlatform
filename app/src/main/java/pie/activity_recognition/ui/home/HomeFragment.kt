package pie.activity_recognition.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import pie.activity_recognition.R
import pie.activity_recognition.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //private val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*
        val homeServiceStatusBool: CheckBox = binding.homeServiceStatusBool
        homeViewModel.activeServiceBool.observe(viewLifecycleOwner) {
            homeServiceStatusBool.isChecked = it
        }
         */


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*
    @Suppress("UNUSED_PARAMETER")
    fun resumeReadings(view: View) {
        val cb = binding.root.findViewById<CheckBox>(R.id.home_service_status_bool)
        cb.isChecked = true
        println("yes")
        /*binding.
        mSensorsService.resumeReading()
        updateCurrentValues()
         */
    }

    @Suppress("UNUSED_PARAMETER")
    @SuppressLint("SetTextI18n")
    fun pauseReadings(view: View) {
        val cb = binding.root.findViewById<CheckBox>(R.id.home_service_status_bool)
        cb.isChecked = false
        println("no")
        /*
        mSensorsService.pauseReading()
        updateNaNValues()
         */
    }
     */
}
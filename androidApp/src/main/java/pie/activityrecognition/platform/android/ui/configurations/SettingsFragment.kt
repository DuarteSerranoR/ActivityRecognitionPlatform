package pie.activityrecognition.platform.android.ui.configurations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import pie.activityrecognition.platform.android.databinding.FragmentConfigurationsBinding

class ConfigurationsFragment : Fragment() {

    private var _binding: FragmentConfigurationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val configurationsViewModel =
        //    ViewModelProvider(this)[ConfigurationsViewModel::class.java]

        _binding = FragmentConfigurationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*
        val textView: TextView = binding.textConfigurations
        configurationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
         */
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
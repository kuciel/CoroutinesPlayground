package com.example.coroutinesplayground

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.coroutinesplayground.coroutines.ChannelRunner
import com.example.coroutinesplayground.coroutines.CoroutinesRunner
import com.example.coroutinesplayground.coroutines.FlowRunner
import com.example.coroutinesplayground.coroutines.StateFlowRunner
import com.example.coroutinesplayground.databinding.FragmentFirstBinding
import kotlinx.coroutines.flow.flow

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //runCoroutines()
        //runFlows()
        //runChannels()
        runStateFlow()

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun runCoroutines() {
        val coroutinesRunner = CoroutinesRunner()
        coroutinesRunner.runCoroutine()
    }

    private fun runFlows() {
        val flowRunner = FlowRunner()
        flowRunner.run()
    }

    private fun runChannels() {
        val channelsRunner = ChannelRunner()
        channelsRunner.runChannelSamples()
    }

    private fun runStateFlow() {
        val stateFlowRunner = StateFlowRunner()
        stateFlowRunner.launchStateFlowRunner()
    }
}
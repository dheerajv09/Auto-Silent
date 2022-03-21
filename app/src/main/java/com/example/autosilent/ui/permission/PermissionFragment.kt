package com.example.autosilent.ui.permission

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.autosilent.R
import com.example.autosilent.databinding.FragmentPermissionBinding
import com.example.autosilent.util.ExtensionFunctions.observeOnce
import com.example.autosilent.util.Permissions
import com.example.autosilent.viewmodels.SharedViewModel
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class PermissionFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentPermissionBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPermissionBinding.inflate(inflater, container, false)

        binding.continueButton.setOnClickListener {

            if (Permissions.hasLocationPermission(requireContext())) {
                checkFirstLaunch()
            } else {
                Permissions.requestsLocationPermission(this)
            }
        }

        return binding.root
    }

    private fun checkFirstLaunch() {
        sharedViewModel.readFirstLaunch.observeOnce(viewLifecycleOwner) { firstLaunch ->
            if (firstLaunch) {
                findNavController().navigate(R.id.action_permissionFragment_to_showPlacesFragment)
                sharedViewModel.saveFirstLaunch(false)
            } else {
                findNavController().navigate(R.id.action_permissionFragment_to_showPlacesFragment)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            Permissions.requestsLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(
            requireContext(),
            "Permission Granted! Tap on 'Continue' button to proceed.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
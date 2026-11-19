package com.example.contactlist

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.contactlist.databinding.FragmentDetailContactBinding

class DetailContactFragment : Fragment() {

    companion object {
        private const val ARG_CONTACT_ID = "arg_contact_id"

        fun newInstance(contactId: String): DetailContactFragment {
            val fragment = DetailContactFragment()
            val args = Bundle().apply {
                putString(ARG_CONTACT_ID, contactId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentDetailContactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by activityViewModels()

    private var contactId: String? = null
    private var contact: Contact? = null

    private var currentPhotoUri: String? = null
    private var tempCameraUri: Uri? = null
    private var tempCameraFilePath: String? = null

    // Gallery Picker Launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val ctx = context ?: return@registerForActivityResult
        if (uri != null) {
            val savedPath = ImageUtils.saveImageToInternalStorage(ctx, uri)
            if (savedPath != null) {
                currentPhotoUri = savedPath
                updatePhotoDisplay()
                Toast.makeText(ctx, R.string.msg_photo_updated, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(ctx, R.string.msg_photo_cancelled, Toast.LENGTH_SHORT).show()
        }
    }

    // Camera Capture Launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val ctx = context ?: return@registerForActivityResult
        if (success && tempCameraFilePath != null) {
            currentPhotoUri = tempCameraFilePath
            updatePhotoDisplay()
            Toast.makeText(ctx, R.string.msg_photo_updated, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(ctx, R.string.msg_photo_cancelled, Toast.LENGTH_SHORT).show()
        }
    }

    // Permission Launcher for Camera
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        val ctx = context ?: return@registerForActivityResult
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(ctx, R.string.error_camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactId = arguments?.getString(ARG_CONTACT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.updateTitle(getString(R.string.title_contact_detail), showBack = true)

        contact = contactId?.let { viewModel.getContactById(it) }

        if (contact == null) {
            Toast.makeText(requireContext(), "Erreur : Contact introuvable", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        currentPhotoUri = contact?.photoUri

        populateFields()
        setupListeners()
    }

    private fun populateFields() {
        val c = contact ?: return
        binding.etNom.setText(c.nom)
        binding.etPrenom.setText(c.prenom)
        binding.etTelephone.setText(c.telephone)
        binding.etEmail.setText(c.email)

        binding.etLatitude.setText(c.latitude?.toString().orEmpty())
        binding.etLongitude.setText(c.longitude?.toString().orEmpty())

        updatePhotoDisplay()
    }

    private fun updatePhotoDisplay() {
        val c = contact ?: return
        val ctx = context ?: return
        ImageUtils.loadContactPhoto(
            context = ctx,
            photoUriStr = currentPhotoUri,
            photoResId = c.photoResId,
            imageView = binding.ivDetailPhoto
        )
    }

    private fun setupListeners() {
        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnCamera.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        binding.btnCall.setOnClickListener {
            makePhoneCall()
        }

        binding.btnEmail.setOnClickListener {
            sendEmail()
        }

        binding.btnViewMap.setOnClickListener {
            openMapFragment()
        }

        binding.btnSave.setOnClickListener {
            saveContact()
        }

        binding.btnCancel.setOnClickListener {
            cancelAndExit()
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        val ctx = context ?: return
        if (!ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(ctx, R.string.error_no_camera, Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val ctx = context ?: return
        try {
            val (contentUri, filePath) = ImageUtils.createTempCameraUri(ctx)
            tempCameraUri = contentUri
            tempCameraFilePath = filePath
            cameraLauncher.launch(contentUri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(ctx, R.string.error_no_camera, Toast.LENGTH_SHORT).show()
        }
    }

    private fun makePhoneCall() {
        val phone = binding.etTelephone.text.toString().trim()
        if (phone.isEmpty()) {
            binding.inputLayoutTelephone.error = getString(R.string.error_field_required)
            Toast.makeText(requireContext(), R.string.error_field_required, Toast.LENGTH_SHORT).show()
            return
        }
        binding.inputLayoutTelephone.error = null

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }

        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.error_no_dialer_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.inputLayoutEmail.error = getString(R.string.error_field_required)
            Toast.makeText(requireContext(), R.string.error_field_required, Toast.LENGTH_SHORT).show()
            return
        }
        binding.inputLayoutEmail.error = null

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }

        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.error_no_email_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseCoordinates(): Pair<Double?, Double?>? {
        val latStr = binding.etLatitude.text.toString().trim()
        val lngStr = binding.etLongitude.text.toString().trim()

        binding.inputLayoutLatitude.error = null
        binding.inputLayoutLongitude.error = null

        if (latStr.isEmpty() && lngStr.isEmpty()) {
            return Pair(null, null)
        }

        var isValid = true

        val lat = latStr.toDoubleOrNull()
        if (latStr.isNotEmpty() && (lat == null || lat !in -90.0..90.0)) {
            binding.inputLayoutLatitude.error = getString(R.string.error_invalid_latitude)
            isValid = false
        }

        val lng = lngStr.toDoubleOrNull()
        if (lngStr.isNotEmpty() && (lng == null || lng !in -180.0..180.0)) {
            binding.inputLayoutLongitude.error = getString(R.string.error_invalid_longitude)
            isValid = false
        }

        if (!isValid) return null

        return Pair(lat, lng)
    }

    private fun openMapFragment() {
        val coords = parseCoordinates()
        if (coords == null) {
            Toast.makeText(requireContext(), R.string.msg_no_gps_coordinates, Toast.LENGTH_SHORT).show()
            return
        }

        val c = contact ?: return
        c.latitude = coords.first
        c.longitude = coords.second

        if (!c.hasValidCoordinates) {
            Toast.makeText(requireContext(), R.string.msg_no_gps_coordinates, Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateContact(c)

        val fragment = CarteContactFragment.newInstance(c.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun saveContact() {
        val nom = binding.etNom.text.toString().trim()
        val prenom = binding.etPrenom.text.toString().trim()
        val telephone = binding.etTelephone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()

        var isValid = true

        if (nom.isEmpty()) {
            binding.inputLayoutNom.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            binding.inputLayoutNom.error = null
        }

        if (prenom.isEmpty()) {
            binding.inputLayoutPrenom.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            binding.inputLayoutPrenom.error = null
        }

        if (telephone.isEmpty()) {
            binding.inputLayoutTelephone.error = getString(R.string.error_field_required)
            isValid = false
        } else {
            binding.inputLayoutTelephone.error = null
        }

        val coords = parseCoordinates()
        if (coords == null) {
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(requireContext(), R.string.error_field_required, Toast.LENGTH_SHORT).show()
            return
        }

        val c = contact ?: return
        c.nom = nom
        c.prenom = prenom
        c.telephone = telephone
        c.email = email
        c.photoUri = currentPhotoUri
        c.latitude = coords?.first
        c.longitude = coords?.second

        viewModel.updateContact(c)

        Toast.makeText(requireContext(), R.string.msg_contact_updated, Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun cancelAndExit() {
        Toast.makeText(requireContext(), R.string.msg_changes_cancelled, Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

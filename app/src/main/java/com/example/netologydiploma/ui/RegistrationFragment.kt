package com.example.netologydiploma.ui

import android.app.Activity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.FragmentRegistrationBinding
import com.example.netologydiploma.util.AndroidUtils
import com.example.netologydiploma.viewModel.LoginRegistrationViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar

class RegistrationFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var binding: FragmentRegistrationBinding
    private val viewModel: LoginRegistrationViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        navController = findNavController()
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)


        viewModel.isSignedIn.observe(viewLifecycleOwner) { isSignedId ->
            if (isSignedId) {
                binding.progressBar.visibility = View.GONE
                AndroidUtils.hideKeyboard(requireView())
                findNavController().popBackStack()
                viewModel.invalidateSignedInState()
            }
        }

        setOnUseExistingAccountListener()

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state.isLoading

            if (state.hasError) {
                val msg = getString(state.errorMessage ?: R.string.common_error_message)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.ok_action), {})
                    .show()
                viewModel.invalidateDataState()
            }
        }

        binding.signUpBt.setOnClickListener {
            val login = binding.userLoginEt.text.toString().trim()
            val userName = binding.userNameEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            val passConfirmation = binding.confirmPasswordEt.text.toString().trim()

            if (userName.isEmpty()) {
                binding.userNameEt.error =
                    getString(R.string.empty_field_error_registration_fragment)
                binding.userNameEt.requestFocus()
                return@setOnClickListener
            }
            if (login.isEmpty()) {
                binding.userLoginEt.error =
                    getString(R.string.empty_field_error_registration_fragment)
                binding.userLoginEt.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(login).matches()) {
                binding.userLoginEt.error = getString(R.string.invalid_email_registration)
                binding.userLoginEt.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.passwordEt.error =
                    (getString(R.string.password_too_short_error_registration_fragment))
                binding.passwordEt.requestFocus()
                return@setOnClickListener
            }

            if (password != passConfirmation) {
                binding.confirmPasswordEt.error =
                    getString(R.string.passwords_not_match_error_registration)
                binding.confirmPasswordEt.requestFocus()
                return@setOnClickListener
            }
            viewModel.onSignUp(login, password, userName)
            navController.popBackStack()
        }

        val handlePhotoResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val resultCode = activityResult.resultCode
                val data = activityResult.data

                if (resultCode == Activity.RESULT_OK) {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    viewModel.changePhoto(fileUri, fileUri.toFile())
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Snackbar.make(
                        binding.root,
                        ImagePicker.getError(activityResult.data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

        binding.cVAvatarContainer.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .galleryOnly()
                .galleryMimeTypes(arrayOf("image/png", "image/jpeg"))
                .createIntent { intent ->
                    handlePhotoResult.launch(intent)
                }
        }

        viewModel.photo.observe(viewLifecycleOwner) { photoModel ->
            if (photoModel.uri == null) {
                binding.iVSetAvatar.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_no_avatar_user,
                        null
                    )
                )
                return@observe
            }

            binding.iVSetAvatar.setImageURI(photoModel.uri)


        }

        return binding.root
    }

    private fun setOnUseExistingAccountListener() {
        val spanActionText = getString(R.string.tv_signIn_span_action_registration_fragment)
        val createAccClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {

                navController.navigate(R.id.action_registrationFragment_to_logInFragment)
            }
        }
        SpannableString(spanActionText).apply {
            setSpan(
                createAccClickableSpan,
                0,
                spanActionText.lastIndex + 1,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            binding.tvCreateNewAccount.text = this
            // The TextView delegates handling of key events, trackball motions and touches to the
            // movement method for purposes of content navigation.
            binding.tvCreateNewAccount.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    override fun onDestroyView() {
        viewModel.changePhoto(null, null)
        super.onDestroyView()
    }
}
package com.example.netologydiploma.ui

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.FragmentLogInBinding
import com.example.netologydiploma.util.AndroidUtils
import com.example.netologydiploma.viewModel.LoginRegistrationViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogInFragment : Fragment() {

    companion object {
        const val LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESSFUL"
    }

    private lateinit var savedStateHandle: SavedStateHandle


    private val viewModel: LoginRegistrationViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private lateinit var binding: FragmentLogInBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLogInBinding.inflate(
            inflater,
            container,
            false
        )



        // use nav saved state to tell other activities whether the
        // logg in procedure went well
        // https://developer.android.com/guide/navigation/navigation-conditional
        savedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        savedStateHandle.set(LOGIN_SUCCESSFUL, false)


        binding.signInBt.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val login = binding.loginEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            viewModel.onSignIn(login, password)
        }

        viewModel.isSignedIn.observe(viewLifecycleOwner) { isSignedId ->
            if (isSignedId) {
                binding.progressBar.visibility = View.GONE
                AndroidUtils.hideKeyboard(requireView())
                savedStateHandle.set(LOGIN_SUCCESSFUL, true)
                findNavController().popBackStack()
                viewModel.invalidateSignedInState()
            }
        }

        setOnCreateNewAccountListener()

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state.isLoading

            if (state.hasError) {
                val msg = getString(state.errorMessage ?: R.string.common_error_message)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT)
                    .setAction("Ok", {})
                    .show()
                viewModel.invalidateDataState()
            }
        }

        binding.authLaterBt.setOnClickListener {
            findNavController().popBackStack(R.id.nav_posts_fragment, false)
        }

        return binding.root
    }

    private fun setOnCreateNewAccountListener() {
        val spanActionText = getString(R.string.tv_create_account_span_action_login_fragment)
        val createAccClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                binding.loginEt.text.clear()
                binding.passwordEt.text.clear()
                findNavController().navigate(R.id.action_logInFragment_to_registrationFragment)
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

}
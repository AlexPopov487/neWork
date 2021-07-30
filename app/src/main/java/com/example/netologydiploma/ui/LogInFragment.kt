package com.example.netologydiploma.ui

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.FragmentLogInBinding
import com.example.netologydiploma.util.AndroidUtils
import com.example.netologydiploma.viewModel.SignInUpViewModel

class LogInFragment : Fragment() {

    private val upViewModel: SignInUpViewModel by viewModels(
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

        binding.signInBt.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val login = binding.loginEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            upViewModel.onSignIn(login, password)
        }

        setOnCreateNewAccountListener()

        upViewModel.isSignedIn.observe(viewLifecycleOwner) { isSignedId ->
            if (isSignedId) {
                binding.progressBar.visibility = View.GONE
                AndroidUtils.hideKeyboard(requireView())
                findNavController().popBackStack()
                upViewModel.invalidateSignedInState()
            }
        }


        return binding.root
    }

    private fun setOnCreateNewAccountListener() {
        val spanActionText = getString(R.string.tv_create_account_span_action_login_fragment)
        val createAccClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
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
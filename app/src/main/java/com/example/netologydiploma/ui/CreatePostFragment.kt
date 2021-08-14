package com.example.netologydiploma.ui

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.FragmentCreatePostBinding
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.util.AndroidUtils
import com.example.netologydiploma.viewModel.PostViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

const val MEDIA_REQUEST_CODE = 101


@AndroidEntryPoint
@ExperimentalPagingApi
class CreatePostFragment : Fragment() {

    private lateinit var binding: FragmentCreatePostBinding
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        viewModel.editedPost.observe(viewLifecycleOwner) { editedPost ->
            editedPost?.let {

                (activity as MainActivity?)?.setActionBarTitle(getString(R.string.change_post_fragment_title))
                binding.eTPostContent.setText(editedPost.content)
                binding.eTPostContent.requestFocus(
                    binding.eTPostContent.text.lastIndex
                )
                AndroidUtils.showKeyboard(binding.eTPostContent)
            }
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


        binding.btPickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(arrayOf("image/png", "image/jpeg"))
                .createIntent { intent ->
                    handlePhotoResult.launch(intent)
                }
        }

        binding.btTakePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .provider(ImageProvider.CAMERA)
                .createIntent { intent ->
                    handlePhotoResult.launch(intent)
                }
        }


//        val mediaPlayer = SimpleExoPlayer.Builder(requireContext())
//            .build()
//            .also {
//                binding.ivPhoto.player = it
//            }
//
        
//        binding.btPickVideo.setOnClickListener {
//            val intent = Intent(
//                Intent.ACTION_PICK,
//                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//            )
//            startActivityForResult(intent, 20)
//        }

        binding.btRemovePhoto.setOnClickListener {
            viewModel.changePhoto(null, null)
        }


        viewModel.photo.observe(viewLifecycleOwner) { photoModel ->
            if (photoModel.uri == null) {
                binding.layoutPhotoContainer.visibility = View.GONE
                return@observe
            }

            binding.layoutPhotoContainer.visibility = View.VISIBLE
            binding.ivPhoto.setImageURI(photoModel.uri)
//            val mediaItem = MediaItem.fromUri(photoModel.uri)
//            mediaPlayer.setMediaItem(mediaItem)

        }

        return binding.root
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK){
//            if (requestCode == 20){
//                val selectedVideoUri = data?.data!!
//                val selectedVideoPath = getRealPathFromUri(selectedVideoUri)
//                if (selectedVideoPath != null) {
//                    Toast.makeText(requireContext(), selectedVideoUri.path + "   |   "+ selectedVideoPath, Toast.LENGTH_LONG).show()
//                    viewModel.changePhoto(selectedVideoUri, File(selectedVideoPath))
//                }
//            }
//        }
//    }
//
//    private fun getRealPathFromUri(uri: Uri) : String? {
//        var cursor: Cursor? = null
//        return try {
//            val projection = arrayOf(MediaStore.Images.Media.DATA)
//            cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
//            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//            cursor?.moveToFirst()
//            cursor?.getString(columnIndex!!)
//        } finally {
//            cursor?.close()
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_create_edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                val content = binding.eTPostContent.text.toString()
                if (content.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.error_blank_post_content),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return false
                }

                // if editedPost is not null, we are to rewrite an existing post.
                // Otherwise, save a new one
                viewModel.editedPost.value?.let {
                    viewModel.savePost(it.copy(content = content))
                } ?: viewModel.savePost(Post(content = content))
                AndroidUtils.hideKeyboard(requireView())
                findNavController().popBackStack()
                true
            }
            else -> false
        }
    }


}
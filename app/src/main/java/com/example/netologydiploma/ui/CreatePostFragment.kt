package com.example.netologydiploma.ui

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.example.netologydiploma.R
import com.example.netologydiploma.databinding.FragmentCreatePostBinding
import com.example.netologydiploma.dto.AttachmentType
import com.example.netologydiploma.dto.Post
import com.example.netologydiploma.util.AndroidUtils
import com.example.netologydiploma.util.PermissionsManager
import com.example.netologydiploma.util.loadImage
import com.example.netologydiploma.viewModel.PostViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
@ExperimentalPagingApi
class CreatePostFragment : Fragment() {

    private lateinit var binding: FragmentCreatePostBinding
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private var mediaPlayer: SimpleExoPlayer? = null

    private val permissionsRequestCode = 123
    lateinit var permissionManager: PermissionsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        val permissions = listOf<String>(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        permissionManager =
            PermissionsManager(requireActivity(), permissions, permissionsRequestCode)

        /** User wants to edit an existing post */
        viewModel.editedPost.observe(viewLifecycleOwner) { editedPost ->
            editedPost?.let {

                (activity as MainActivity?)?.setActionBarTitle(getString(R.string.change_post_fragment_title))
                binding.eTPostContent.setText(editedPost.content)
                binding.eTPostContent.requestFocus(
                    binding.eTPostContent.text.lastIndex
                )
                AndroidUtils.showKeyboard(binding.eTPostContent)

                // get and display attachment (if any)
                it.attachment?.let { attachment ->
                    val type = attachment.type
                    val attachmentUri = attachment.url
                    viewModel.changeMedia(attachmentUri.toUri(), null, type)
                    //disable media removal
                    binding.btRemoveMedia.visibility = View.GONE

                    if (type == AttachmentType.IMAGE) {
                        binding.ivPhoto.loadImage(attachmentUri)
                    }
                }
            }
        }


        val handlePhotoResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val resultCode = activityResult.resultCode
                val data = activityResult.data

                if (resultCode == Activity.RESULT_OK) {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    viewModel.changeMedia(fileUri, fileUri.toFile(), AttachmentType.IMAGE)
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Snackbar.make(
                        binding.root,
                        ImagePicker.getError(activityResult.data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }


        val handleVideoResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val resultCode = activityResult.resultCode
                val data = activityResult.data

                if (resultCode == Activity.RESULT_OK) {
                    val selectedVideoUri = data?.data!!
                    val selectedVideoPath = getRealPathFromUri(selectedVideoUri)
                    if (selectedVideoPath != null) {

                        viewModel.changeMedia(
                            selectedVideoUri,
                            File(selectedVideoPath),
                            AttachmentType.VIDEO
                        )
                    }
                }
            }

        val handleAudioResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val resultCode = activityResult.resultCode
                val data = activityResult.data

                if (resultCode == Activity.RESULT_OK) {
                    val selectedVideoUri = data?.data!!
                    val selectedVideoPath = getRealPathFromUri(selectedVideoUri)
                    if (selectedVideoPath != null) {

                        viewModel.changeMedia(
                            selectedVideoUri,
                            File(selectedVideoPath),
                            AttachmentType.AUDIO
                        )
                    }
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

        binding.btPickVideo.setOnClickListener {
            if (!permissionManager.checkPermissions()) {
                permissionManager.requestPermissions()
                Snackbar.make(
                    binding.root,
                    getString(R.string.grant_storage_permissions_dialog_message),
                    Snackbar.LENGTH_LONG
                )
                    .setAnchorView(binding.bottomPanelLayout)
                    .setAction(getString(R.string.ok_action), {})
                    .show()
                return@setOnClickListener
            }


            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            handleVideoResult.launch(intent)
        }

        binding.btPickAudio.setOnClickListener {
            if (!permissionManager.checkPermissions()) {
                permissionManager.requestPermissions()
                Snackbar.make(
                    binding.root,
                    getString(R.string.grant_storage_permissions_dialog_message),
                    Snackbar.LENGTH_LONG
                )
                    .setAnchorView(binding.bottomPanelLayout)
                    .setAction(getString(R.string.ok_action), {})
                    .show()
                return@setOnClickListener
            }
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            )
            handleAudioResult.launch(intent)
        }

        binding.btRemoveMedia.setOnClickListener {
            viewModel.changeMedia(null, null, null)
        }

        viewModel.media.observe(viewLifecycleOwner) { mediaModel ->
            if (mediaModel.uri == null) {
                binding.layoutPhotoContainer.visibility = View.GONE
                binding.ivPhoto.visibility = View.GONE
                binding.videoPlayerView.visibility = View.GONE
                return@observe
            }
            when (mediaModel.type) {
                AttachmentType.IMAGE -> {
                    binding.ivPhoto.visibility = View.VISIBLE
                    binding.videoPlayerView.visibility = View.GONE
                    binding.layoutPhotoContainer.visibility = View.VISIBLE

                    binding.ivPhoto.setImageURI(mediaModel.uri)
                }
                AttachmentType.VIDEO -> {
                    binding.ivPhoto.visibility = View.GONE
                    binding.videoPlayerView.visibility = View.VISIBLE
                    binding.layoutPhotoContainer.visibility = View.VISIBLE

                    val mediaItem = MediaItem.fromUri(mediaModel.uri)
                    mediaPlayer?.setMediaItem(mediaItem)
                }
                AttachmentType.AUDIO -> {
                    binding.ivPhoto.visibility = View.GONE
                    binding.videoPlayerView.visibility = View.VISIBLE
                    binding.layoutPhotoContainer.visibility = View.VISIBLE

                    val mediaItem = MediaItem.fromUri(mediaModel.uri)
                    mediaPlayer?.setMediaItem(mediaItem)
                }
                null -> Snackbar.make(
                    binding.root,
                    getString(R.string.no_media_loaded_error),
                    Snackbar.LENGTH_SHORT
                ).setAction(getString(R.string.ok_action), {})
                    .show()
            }
        }
        return binding.root
    }


    private fun getRealPathFromUri(uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            cursor?.getString(columnIndex!!)
        } finally {
            cursor?.close()
        }
    }

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
                    // change media so if there were any in the edited post earlier, it wouldn't
                    // affect  viewModel.savePost() function
                    viewModel.changeMedia(null, null, null)
                    viewModel.savePost(it.copy(content = content))
                } ?: viewModel.savePost(Post(content = content))
                AndroidUtils.hideKeyboard(requireView())
                findNavController().popBackStack()
                true
            }
            else -> false
        }
    }

    override fun onStart() {
        super.onStart()
        if (com.google.android.exoplayer2.util.Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (com.google.android.exoplayer2.util.Util.SDK_INT < 24 || mediaPlayer == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (com.google.android.exoplayer2.util.Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (com.google.android.exoplayer2.util.Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        mediaPlayer = SimpleExoPlayer.Builder(requireContext())
            .build()
            .also {
                binding.videoPlayerView.player = it
            }
    }

    private fun releasePlayer() {
        mediaPlayer?.run {
            release()
        }
        mediaPlayer = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showAlert()
                }
            })
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.discard_changes_dialog_title))
        builder.setMessage(getString(R.string.discard_changes_dialog_body))
        builder.setPositiveButton(
            getString(R.string.action_leave_dialog_fragment),
            DialogInterface.OnClickListener { dialog, which ->
                viewModel.invalidateEditPost()
                viewModel.changeMedia(null, null, null)
                findNavController().navigateUp()
            })
        builder.setNeutralButton(getString(R.string.action_cancel_dialog_fragment),
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
        val dialog = builder.create()
        dialog.show()
    }

}
package ltd.evilcorp.atox.ui.create_profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentProfileBinding
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.User

private const val IMPORT = 42

class CreateProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {
    private val viewModel: CreateProfileViewModel by viewModels { vmFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            toolbar.updatePadding(left = insets.left, top = insets.top, right = insets.right)
            content.updatePadding(left = insets.left, right = insets.right)
            compat
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            activity?.finish()
        }

        btnCreate.setOnClickListener {
            btnCreate.isEnabled = false

            viewModel.startTox()
            val user = User(
                publicKey = viewModel.publicKey.string(),
                name = if (username.text.isNotEmpty()) username.text.toString() else "aTox user"
            )
            viewModel.create(user)

            findNavController().popBackStack()
        }

        btnImport.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }

            startActivityForResult(intent, IMPORT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode != IMPORT || resultCode != Activity.RESULT_OK) {
            return
        }

        resultData?.data?.let { uri ->
            Log.e("ProfileFragment", "Importing file $uri")
            viewModel.tryImportToxSave(uri)?.also { save ->
                if (viewModel.startTox(save)) {
                    viewModel.verifyUserExists(viewModel.publicKey)
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.import_tox_save_failed,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

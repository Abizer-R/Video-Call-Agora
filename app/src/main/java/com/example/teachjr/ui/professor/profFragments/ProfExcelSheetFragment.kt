package com.example.teachjr.ui.professor.profFragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.teachjr.databinding.FragmentProfExcelSheetBinding
import com.example.teachjr.ui.viewmodels.professorViewModels.ProfAtdReportViewModel
import com.example.teachjr.utils.Adapter_ViewModel_Utils
import com.example.teachjr.utils.Constants
import com.example.teachjr.utils.excelReadWrite.WriteExcel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class ProfExcelSheetFragment : Fragment() {

    private val TAG = ProfExcelSheetFragment::class.java.simpleName
    private lateinit var binding: FragmentProfExcelSheetBinding

    private val atdReportViewModel by activityViewModels<ProfAtdReportViewModel>()

//        private val permissionRequestLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//            val granted = permissions.entries.all {
//                it.value == true
//            }
//            if(granted) {
//                openExcelFile()
//            } else {
//                Toast.makeText(context, "not GRANTED", Toast.LENGTH_SHORT).show()
//            }
//        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfExcelSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkExistingFile()
        setupButtons()
//        setupObservers()
    }

    private fun checkExistingFile() {
        lifecycleScope.launch {
            getExcelFileName()
            if(atdReportViewModel.fileName == Constants.FILE_NOT_FOUND) {
                showCreateLayout()
            } else {
                showUpdateLayout()
            }
        }
    }

    private fun setupButtons() {
        binding.btnCreateFile.setOnClickListener {
            lifecycleScope.launch {
                showSavingLayout()

                val newFileName = Adapter_ViewModel_Utils.generateFileName()
                val isSaved = saveExcelFile(newFileName)

                handleFileSaved(isSaved, newFileName)
            }
        }

        binding.btnOpenFile.setOnClickListener {
            openExcelFile()
        }

        binding.btnUpdateFile.setOnClickListener {
            lifecycleScope.launch {
                showSavingLayout()
                Log.i("TAG", "testing: Calling deleteExcelFile()")
                val isDeleted = deleteExcelFile()
                if(isDeleted) {
                    Log.i("TAG", "testing: old file deleted")
                    val newFileName = Adapter_ViewModel_Utils.generateFileName()
                    val isSaved = saveExcelFile(newFileName)

                    handleFileSaved(isSaved, newFileName)

                } else {
                    Toast.makeText(context, "Couldn't Update File", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleFileSaved(isSaved: Boolean, newFileName: String) {
        if(isSaved) {
            atdReportViewModel.updateFileName("$newFileName.xls")
            showUpdateLayout()
        } else {
            showCreateLayout()
            Toast.makeText(context, "Failed to save file in storage.", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun deleteExcelFile(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.i("TAG", "testing_deleteExcelFile: called")
                requireContext().deleteFile(atdReportViewModel.fileName)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private suspend fun getExcelFileName() {
        return withContext(Dispatchers.IO) {
            val files = requireContext().filesDir.listFiles()
            // We only want excel files, so we filter
            for(file in files!!) {
                if(file.canRead() && file.isFile && file.name.startsWith(Constants.REPORT_FILE_NAME_PREFIX) && file.name.endsWith(".xls")) {
                    atdReportViewModel.updateFileName(file.name)
                    break
                }
            }
            Log.i("TAG", "Testing_getExcelFileName: filename = ${atdReportViewModel.fileName}")
//            fileName
        }
    }

    private suspend fun saveExcelFile(newFileName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val workbook = WriteExcel.getWorkbookWithData(
                    path = requireContext().filesDir.path,
                    fileName = newFileName,
                    atdDetails = atdReportViewModel.atdDetails.value!!.data!!)

                if(workbook != null) {
                    val fileOutputStream = requireContext().openFileOutput("$newFileName.xls",
                        AppCompatActivity.MODE_PRIVATE
                    )
                    workbook.write(fileOutputStream)

                    fileOutputStream.flush()
                    fileOutputStream.close()

                    true

                } else {
                    false
                }

            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun openExcelFile() {
        try {
            if(atdReportViewModel.fileName != "FILE_NOT_FOUND") {

                val sheet = File(requireContext().filesDir, atdReportViewModel.fileName)

                val contentUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.teachjr.fileProvider",
                    sheet
                )
                Log.i("TAG", "TESTING, URI: ${contentUri.toString()}")

                val intent = Intent(Intent.ACTION_VIEW)
                if(contentUri.toString().contains(".xls") || contentUri.toString().contains(".xlsx")) {
                    intent.setDataAndType(contentUri, "application/vnd.ms-excel")
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)

            }
        } catch (e: ActivityNotFoundException) {
                    binding.tvNoAppToOpenExcel.visibility = View.VISIBLE
            Toast.makeText(context, "Excel file can't be opened", Toast.LENGTH_LONG).show()
        }
        catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showCreateLayout() {
        binding.layoutCreateFile.visibility = View.VISIBLE
        binding.layoutExistingFile.visibility = View.GONE
        binding.cvSaving.visibility = View.GONE

        binding.btnCreateFile.isEnabled = true
    }

    private fun showUpdateLayout() {
        binding.layoutExistingFile.visibility = View.VISIBLE
        binding.layoutExistingFile.alpha = 1F
        binding.cvSaving.visibility = View.GONE
        binding.layoutCreateFile.visibility = View.GONE

        binding.btnOpenFile.isEnabled = true
        binding.btnUpdateFile.isEnabled = true

        binding.tvFileName.text = atdReportViewModel.fileName
    }

    private fun showSavingLayout() {
        binding.cvSaving.visibility = View.VISIBLE
//        binding.layoutExistingFile.visibility = View.GONE
//        binding.layoutCreateFile.visibility = View.GONE

        binding.layoutExistingFile.alpha = 0.5F
        binding.layoutCreateFile.alpha = 0.5F

        // Disabling Buttons
        binding.btnCreateFile.isEnabled = false
        binding.btnOpenFile.isEnabled = false
        binding.btnUpdateFile.isEnabled = false
    }

}
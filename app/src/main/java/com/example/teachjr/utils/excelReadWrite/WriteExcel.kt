package com.example.teachjr.utils.excelReadWrite

import android.util.Log
import com.example.teachjr.data.model.ProfAttendanceDetails
import com.example.teachjr.utils.Adapter_ViewModel_Utils
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.sealedClasses.Response
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


object WriteExcel {

    private val TAG = WriteExcel::class.java.simpleName

    suspend fun getWorkbookWithData(path: String, fileName: String, atdDetails: ProfAttendanceDetails) : Workbook? {
        return suspendCoroutine { continuation ->
            Log.i(TAG, "TESTING : downloadFolderPath = $path")
            try {
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet(fileName)

                addEnrollmentColumn(sheet, atdDetails.studentList)  // First Column
                addRows(sheet, atdDetails)

                continuation.resume(workbook)

            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resume(null)
            }
        }
    }


    private fun addEnrollmentColumn(sheet: Sheet, stdList: List<String>) {
        // TODO: Try setting manual width

        // Setting col width
        val fontUnit = 256
        sheet.setColumnWidth(0, (stdList[0].length+2) * fontUnit)

        for(i in 1..stdList.size) {
            val row = sheet.createRow(i+1)  // including space row
            val cell = row.createCell(0)
            cell.setCellValue(stdList[i-1])

            cell.cellStyle = getHeaderStyle(cell.sheet.workbook)
        }
    }

    private fun addRows(sheet: Sheet, atdDetails: ProfAttendanceDetails) {

        val row = sheet.createRow(0)
        val stdList = atdDetails.studentList
        val lecList = atdDetails.lectureList

        for(col in 1..lecList.size) {
            val currLec = lecList[col-1]
            val lecDate = Adapter_ViewModel_Utils.getFormattedDate(currLec.timestamp)



            // First Row (Lecture num and date)
            val headerCell = row.createCell(col)
            val text = "Lec-$col\n($lecDate)"
            headerCell.setCellValue(text)

            // Setting col width
            val fontUnit = 256
            headerCell.sheet.setColumnWidth(col, (lecDate.length+3) * fontUnit)

            // cellStyle
            headerCell.cellStyle = getHeaderStyle(headerCell.sheet.workbook)
            // The heading should be of 2 lines
            headerCell.row.heightInPoints = headerCell.sheet.defaultRowHeightInPoints * 2


            val presentList = currLec.presentList.toHashSet()
            for(stdRowIdx in 1..stdList.size) {
                val stdRow = sheet.getRow(stdRowIdx+1) // including spaceRow
                val stdCell = stdRow.createCell(col)
                if(presentList.contains(stdList[stdRowIdx-1])) {
                    stdCell.setCellValue("PS")
                } else {
                    stdCell.setCellValue("ABS")
                }

                stdCell.cellStyle = getCellStyle(stdCell.sheet.workbook)
            }
        }

//        // Autosizing the column to fit all cell
//        for(col in 0..lecList.size) {
//            sheet.autoSizeColumn(col)
//        }
    }

    private fun getHeaderStyle(workbook: Workbook): CellStyle {
        val cellStyle: CellStyle = workbook.createCellStyle()

        // setting font
        val boldFont: XSSFFont = workbook.createFont() as XSSFFont
        boldFont.bold = true
        boldFont.boldweight = XSSFFont.BOLDWEIGHT_BOLD
        cellStyle.setFont(boldFont)

        cellStyle.alignment = CellStyle.ALIGN_CENTER
        cellStyle.wrapText = true   // to allow a line break

        return cellStyle
    }

    private fun getCellStyle(workbook: Workbook): CellStyle {
        val cellStyle: CellStyle = workbook.createCellStyle()

        // This is the only thing we need right now
        cellStyle.alignment = CellStyle.ALIGN_CENTER

        return cellStyle
    }
}
package com.example.teachjr.data.model

data class RvAtdReportListItem (

    // TODO: We can use same adapter for both lists
    /**
     * ---------- First TextView (Left) ----------
     * Lecture Tab: "$lectureNo ($lecDate)"     EXAMPLE: "Lec-1 (01/10/22)"
     * Student Tab: "$enrollmentNo"             EXAMPLE: "0818CS201007"
     *
     * ---------- Secons TextView (Right) ----------
     * Lecture Tab: "$Percentage"               EXAMPLE: "50%"
     * Student Tab: "$Percentage"             EXAMPLE: "75%"
     */


    val description: String = "",
    val percentage: String = ""
)
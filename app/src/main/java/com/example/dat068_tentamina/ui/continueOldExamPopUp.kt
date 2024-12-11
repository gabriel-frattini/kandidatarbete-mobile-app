package com.example.dat068_tentamina.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.dat068_tentamina.viewmodel.ExamInfo

@Composable
fun continueOldExamPopUp(examInfo: ExamInfo){
    if (true)
    {
        AlertDialog(
            onDismissRequest = {  },
            title = { Text("Important!") },
            text = { Text("There is an already started exam for this user and Exam ID.\n Would you like to continue the started exam?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        //examInfo.continueAlreadyStartedExam()
                        //TODO: Remove popup
                    }
                ) {
                    Text("Continue on old exam")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // TODO: overwrite the exam (which should be done automatically at next save of current to SD)?????
                        //TODO: remove popUp
                    }
                ) {
                    Text("Start New Exam")
                }

            }
        )
    }
}
package com.sndrlabs.calidadbubble

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle

class FilePickerActivity : Activity() {

    companion object {
        const val EXTRA_MIME_TYPE = "EXTRA_MIME_TYPE"
        private const val REQUEST_CODE_FILE = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mimeType = intent?.getStringExtra(EXTRA_MIME_TYPE) ?: "*/*"
        val fileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = if (mimeType.contains("json", ignoreCase = true)) "application/json" else "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/*", "*/*"))
        }
        try {
            startActivityForResult(
                Intent.createChooser(fileIntent, "Seleccionar archivo de respaldo JSON"),
                REQUEST_CODE_FILE
            )
        } catch (e: Throwable) {
            BubbleService.fileUploadCallback?.onReceiveValue(null)
            BubbleService.fileUploadCallback = null
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FILE) {
            val result: Array<Uri>? = if (resultCode == RESULT_OK && data != null) {
                if (data.data != null) {
                    arrayOf(data.data!!)
                } else if (data.clipData != null) {
                    val count = data.clipData!!.itemCount
                    Array(count) { i -> data.clipData!!.getItemAt(i).uri }
                } else {
                    null
                }
            } else {
                null
            }
            BubbleService.fileUploadCallback?.onReceiveValue(result)
            BubbleService.fileUploadCallback = null
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BubbleService.fileUploadCallback != null) {
            BubbleService.fileUploadCallback?.onReceiveValue(null)
            BubbleService.fileUploadCallback = null
        }
    }
}

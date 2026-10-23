package com.sndrlabs.calidadbubble

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback

class FilePickerActivity : Activity() {

    companion object {
        const val EXTRA_MIME_TYPE = "EXTRA_MIME_TYPE"
        private const val REQUEST_CODE_FILE = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mimeType = intent?.getStringExtra(EXTRA_MIME_TYPE) ?: "*/*"
        AppLogManager.log("PICKER", "FilePickerActivity iniciada (mimeType=$mimeType)")

        val fileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/*", "text/plain", "application/json", "*/*"))
        }

        val chooserIntent = Intent.createChooser(fileIntent, "Seleccionar archivo de respaldo").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivityForResult(chooserIntent, REQUEST_CODE_FILE)
        } catch (e: Throwable) {
            AppLogManager.log("ERROR", "Error al abrir selector de archivos: ${e.message}")
            BubbleService.fileUploadCallback?.onReceiveValue(null)
            BubbleService.fileUploadCallback = null
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                val result: Array<Uri>? = if (data.data != null) {
                    val uri = data.data!!
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: Throwable) {}
                    AppLogManager.log("PICKER", "URI recibida: $uri")
                    arrayOf(uri)
                } else if (data.clipData != null) {
                    val count = data.clipData!!.itemCount
                    val uris = Array(count) { i ->
                        val u = data.clipData!!.getItemAt(i).uri
                        try {
                            contentResolver.takePersistableUriPermission(
                                u,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: Throwable) {}
                        u
                    }
                    AppLogManager.log("PICKER", "$count URIs recibidas: ${uris.joinToString()}")
                    uris
                } else {
                    AppLogManager.log("PICKER", "Selección sin datos")
                    null
                }
                BubbleService.fileUploadCallback?.onReceiveValue(result)
            } else {
                AppLogManager.log("PICKER", "Selección cancelada")
                BubbleService.fileUploadCallback?.onReceiveValue(null)
            }
            BubbleService.fileUploadCallback = null
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BubbleService.fileUploadCallback != null) {
            AppLogManager.log("PICKER", "FilePickerActivity destruida con callback pendiente -> resolviendo con null")
            BubbleService.fileUploadCallback?.onReceiveValue(null)
            BubbleService.fileUploadCallback = null
        }
    }
}

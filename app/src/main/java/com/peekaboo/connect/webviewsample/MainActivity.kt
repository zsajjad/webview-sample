package com.peekaboo.connect.webviewsample

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebMessage
import android.webkit.WebMessagePort.WebMessageCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.webkit.JavaScriptReplyProxy


class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private var pendingPermissionRequest: PermissionRequest? = null

    var myListener: WebMessageCallback = object : WebMessageCallback() {
        @SuppressLint("RequiresFeature")
        fun onPostMessage(
            view: WebView?, message: WebMessage?, sourceOrigin: Uri?,
            isMainFrame: Boolean, replyProxy: JavaScriptReplyProxy,
        ) {
            Log.d("WebView", message?.data.toString())
            // do something about view, message, sourceOrigin and isMainFrame.
            replyProxy.postMessage("Got it!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                Log.d("WebView", "Permission requested for: ${request.resources.joinToString()}")

                if (request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(
                            "Web View",
                            "Camera permission already granted, grant WebView permission"
                        )
                        // Camera permission already granted, grant WebView permission
                        request.grant(request.resources)

                    } else {
                        Log.d("Web View", "Save the request and ask for camera permission")
                        // Save the request and ask for camera permission
                        pendingPermissionRequest = request
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(Manifest.permission.CAMERA),
                            CAMERA_PERMISSION_REQUEST_CODE
                        )
                    }
                }
            }

            override fun onPermissionRequestCanceled(request: PermissionRequest) {
                Log.d("WebView", "Permission request canceled")
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d("WebView", consoleMessage.message())
                return true
            }
        }

        // Allow webview to request for location permission
        webView.settings.setGeolocationEnabled(true)
        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.databaseEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        // Set webview to full height
        webView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        webView.loadUrl("https://pkb-redemption-stage.vercel.app/?entityId=436&dealId=116269&associationId=1&lat=24.876793&long=67.062026&userId=CXU-C3AEOGM0M7&timestamp=1736164879517&signature=1OIjUxusIQstVU7ZCfdMpxnLPnSjoveiHSj22XHbtsE%3D") // Replace with your URL
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                Log.d("WebView", "Grant WebView access")
                pendingPermissionRequest?.grant(pendingPermissionRequest!!.resources) // Grant WebView access
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                Log.d("WebView", "Deny WebView access")
                pendingPermissionRequest?.deny()
            }
            Log.d("WebView", "pending permission req --> null")
            pendingPermissionRequest = null // Reset pending request
        }
    }
}
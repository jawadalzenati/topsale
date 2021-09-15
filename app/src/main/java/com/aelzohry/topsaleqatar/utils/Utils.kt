package com.aelzohry.topsaleqatar.utils

import android.app.Activity
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import com.aelzohry.topsaleqatar.App
import com.aelzohry.topsaleqatar.R
import com.aelzohry.topsaleqatar.helper.Helper
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    fun isNetworkConnected(): Boolean {
        var isConnected: Boolean = true // Initial Value
        val connectivityManager =
            App.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        if (activeNetwork != null && activeNetwork.isConnected)
            isConnected = false
        return isConnected
    }

//    fun initFilePart(context: Context, partName: String, uri: Uri): MultipartBody.Part {
//        val projection = arrayOf(MediaStore.Images.Media.DATA)
//        val cursor =
//            context.contentResolver.query(uri, projection, null, null, null) ?: return null!!
//        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//        cursor.moveToFirst()
//        val s = cursor.getString(column_index)
//        cursor.close()
//        val file = File(s)
//        val requestFile =
//            RequestBody.create(MediaType.parse(context.contentResolver.getType(uri)!!), file)
//        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
//    }
//
//    fun initStringPart(text: String): RequestBody =
//        RequestBody.create(MediaType.parse("text/plain"), text)

    fun loginView(view: View) {
        Snackbar.make(view, R.string.youMustLogin, Snackbar.LENGTH_LONG)
            .setAction(R.string.login) {
                //                view.context.startActivity(Intent(view.context, LoginActivity::class.java))
            }
            .show()
    }



    fun getTime(time: String): String {
        return try {
            DateFormat.format("hh:mm a", SimpleDateFormat("hh:mm:ss").parse(time)).toString()
        } catch (ex: Exception) {
            time
        }
    }


    fun printKeyHash(context: Context) {
        try {
            var info = context.packageManager.getPackageInfo(
                "com.businessCycle.fz3h",
                PackageManager.GET_SIGNATURES
            )

            info.signatures.forEach {

                var md = MessageDigest.getInstance("SHA")
                md.update(it.toByteArray())
                Log.d(
                    "KeyHash:",
                    android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT)
                )
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("KeyHash:", e.toString());
        } catch (e: NoSuchAlgorithmException) {
            Log.e("KeyHash:", e.toString());
        }
    }

    fun clearNotifications(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    fun enablePermission(context: Activity, PERMISSION_REQUEST: Int) {
        val packageName = context.packageName

        try {
            //Open the specific App Info page:
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            context.startActivityForResult(intent, PERMISSION_REQUEST)

        } catch (e: ActivityNotFoundException) {
            //e.printStackTrace();
            //Open the generic Apps page:
            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            context.startActivityForResult(intent, PERMISSION_REQUEST)

        }

    }

    fun formatTime(inputDateString: String): String {
        val inputDateFormat = SimpleDateFormat("HH:mm", Locale.US)
        val outputDateFormat = SimpleDateFormat("hh:mm a", Locale(Helper.languageCode))
        return try {
            val date = inputDateFormat.parse(inputDateString)
            outputDateFormat.format(date)
        } catch (ex: Exception) {
            inputDateString
        }
    }

    fun formatDate(inputDateString: String): String {
        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputDateFormat = SimpleDateFormat("EEE dd-MMM-yyyy", Locale(Helper.languageCode))
        return try {
            val date = inputDateFormat.parse(inputDateString)
            outputDateFormat.format(date)
        } catch (ex: Exception) {
            inputDateString
        }
    }
}
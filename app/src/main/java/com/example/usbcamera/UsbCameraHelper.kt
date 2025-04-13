package com.example.usbcamera

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import android.view.Surface
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.IDeviceConnectCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.usb.USBMonitor.UsbControlBlock


class UsbCameraHelper(private val context: Context) {
    private val Tag = "UsbCameraHelper"

    private val trySizes = listOf( 1280 to 720, 640 to 480)

    private var multiCameraClient: MultiCameraClient? = null
    var usbCamera: CameraUVC? = null

    init {
        multiCameraClient = createMultiCameraClient()
        multiCameraClient!!.register()
    }

    fun startPreview(surface: Surface) {
        for ((w, h) in trySizes) {
            try {
                usbCamera?.openCamera(surface, getCameraRequest(w, h))
                setICameraStateCallBack()
                usbCamera?.setRenderSize(w, h)
                break
            } catch (e: Exception) {
                // 실패시 다음 사이즈
            }
        }
    }

    fun close (){
        usbCamera?.closeCamera()
        usbCamera = null

        multiCameraClient?.unRegister()
    }

    private fun createMultiCameraClient () : MultiCameraClient {
        return MultiCameraClient(context, object : IDeviceConnectCallBack {
            override fun onAttachDev(device: UsbDevice?) {
                Log.i(Tag, "Camera ${device?.productName} attached")
                if (device != null) {
                    multiCameraClient?.requestPermission(device)
                }
            }
            override fun onConnectDev(device: UsbDevice?, ctrlBlock: UsbControlBlock?) {
                if (ctrlBlock != null) {
                    // CameraUVC 객체 생성
                    usbCamera = CameraUVC(context, device!!)
                    // ControlBlock 설정
                    usbCamera?.setUsbControlBlock(ctrlBlock)

                    Log.i(Tag, "Camera ${device.productName} connected")
                } else {
                    Log.e(Tag, "Camera ${device?.productName} connect failed")
                }
            }
            override fun onDisConnectDec(device: UsbDevice?, ctrlBlock: UsbControlBlock?) {
                Log.i(Tag, "Camera ${device?.productName} disconnected")
            }
            override fun onCancelDev(device: UsbDevice?) {
                Log.i(Tag, "Camera ${device?.productName} cancel dev")
            }
            override fun onDetachDec(device: UsbDevice?){
                Log.i(Tag, "Camera ${device?.productName} detached")
            }
        })
    }

    private fun setICameraStateCallBack () {
        usbCamera?.setCameraStateCallBack(object : ICameraStateCallBack {
            override fun onCameraState(
                self: MultiCameraClient.ICamera,
                code: ICameraStateCallBack.State,
                msg: String?) {

                val deviceName = usbCamera?.device?.productName ?: "Unknown"

                when (code) {
                    ICameraStateCallBack.State.OPENED -> {
                        Log.i(Tag, "Camera $deviceName opened")
                        usbCamera?.captureStreamStart()
                    }
                    ICameraStateCallBack.State.CLOSED ->
                        Log.i(Tag, "Camera $deviceName closed")
                    ICameraStateCallBack.State.ERROR ->
                        Log.e(Tag, "Camera $deviceName error : $msg")
                }
            }
        })
    }

    private fun getCameraRequest (width: Int, height: Int): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(width) // camera preview width
            .setPreviewHeight(height) // camera preview height
            .setRenderMode(CameraRequest.RenderMode.OPENGL) // camera render mode
            .setDefaultRotateType(RotateType.ANGLE_0) // rotate camera image when opengl mode
            .setAudioSource(CameraRequest.AudioSource.NONE) // set audio source
            .setAspectRatioShow(true) // aspect render,default is true
            .setCaptureRawImage(false) // capture raw image picture when opengl mode
            .setRawPreviewData(true)  // preview raw image when opengl mode
            .create()
    }
}
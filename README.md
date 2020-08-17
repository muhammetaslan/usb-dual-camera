
# usb-dual-camera
dual usb camera android app for STB

// start the capture frame 
usbCamera->Cameras.java-> onConnect()

// capture
AbstractUVCCaneraHandler-> handleCaptureStill

#1 How to share Internet (WiFi) over Ethernet using LAN cable (without Router)

https://www.youtube.com/watch?v=4ka-MAbalxY

Ethernet ile adb bağlantısı kurma 

$adb tcpip 5555
$adb connect <ip_address_of_stb>

//IFrameCallback
AbstractUVCCameraHandler.java -> line 646

//setFrameCallback
UVCCamera.java -Z 385


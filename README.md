#1 How to share Internet (WiFi) over Ethernet using LAN cable (without Router)
https://www.youtube.com/watch?v=4ka-MAbalxY

Ethernet ile adb bağlantısı kurma 
$adb tcpip 5555
$adb connect <ip_address_of_stb>


#2 usb-dual-camera
dual usb camera android app for STB

// start the capture frame 
usbCamera->Cameras.java-> onConnect()

// capture
AbstractUVCCaneraHandler-> handleCaptureStill

//IFrameCallback
// bu fonsiyon ile frame veya bitmap verisini alabilirsiniz
AbstractUVCCameraHandler.java -> line 697

//setFrameCallback
UVCCamera.java -Z 385

# ndk version
ndk.dir=/home/muhammetas/Android/Sdk/ndk-r13b

belirtildiği üzere ndk-r13b ile çalışmaktadır.



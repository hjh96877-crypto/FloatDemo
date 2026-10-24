#!/system/bin/sh
# 用法:
#   sh float.sh              显示代码雨 + 自动打开前置摄像头
#   sh float.sh show red     只显示代码雨,指定颜色
#   sh float.sh camera       显示代码雨并打开前置摄像头
#   sh float.sh hide         关闭悬浮窗
PKG=com.example.floatdemo
DIR=$(dirname "$0")
SVC=$PKG/.FloatService

# 没装就先装(FloatDemo.apk 和脚本放同一文件夹)
pm list packages | grep -q "package:$PKG$" || pm install -r "$DIR/FloatDemo.apk"

case "$1" in
  hide)
    am start-foreground-service -n $SVC --es cmd hide
    ;;
  show)
    appops set $PKG SYSTEM_ALERT_WINDOW allow
    am start-foreground-service -n $SVC --es cmd show --es color ${2:-green}
    ;;
  *)
    appops set $PKG SYSTEM_ALERT_WINDOW allow
    am start-foreground-service -n $SVC --es cmd camera --es color green
    ;;
esac
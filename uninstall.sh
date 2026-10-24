#!/system/bin/sh
for p in com.levelinfinite.sgameGlobal com.levelinfinite.sgameGlobal.midaspay; do
  echo "卸载 $p"
  pm uninstall $p || pm uninstall --user 0 $p
done
 #!/bin/bash

set -e

# manual install
# dpkg -i <deb.file>
# uninstall
# sudo apt remove <package>
# list files(?)
# dpkg -L <package>
# /opt/quantumjava/bin/QuantumJava
# -Djavafx.verbose=true -Dprism.verbose=true

PACKAGER="jpackage"

MODS=`jdeps --ignore-missing-deps --module-path /home/mjh/Documents/openjfx-25.0.1_linux-x64_bin-sdk/javafx-sdk-25.0.1/lib --print-module-deps input/qcapp.jar`
echo $MODS

${PACKAGER} \
	--verbose \
	--input input \
	--linux-shortcut \
	--icon strangelogo.png \
	--name QuantumJava \
	--main-jar qcapp.jar \
	--main-class us.hall.qcapp.QuantumJava \
	--java-options "--enable-native-access=javafx.graphics" \
	--jlink-options "--strip-debug --no-man-pages --no-header-files" \
	--module-path /home/mjh/Documents/openjfx-25.0.1_linux-x64_bin-jmods/javafx-jmods-25.0.1 \
	--add-modules "jdk.compiler,java.xml,java.logging,javafx.controls,javafx.fxml"
	

	

	
	

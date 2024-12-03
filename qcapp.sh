 #!/bin/bash

set -e

#PACKAGER=`/usr/libexec/java_home -v 19.0.2`/bin/jpackage
PACKAGER="jpackage"
DEVELOPER_ID_APPLICATION="Developer ID Application: Michael Hall (5X6BXQB3Q7)"
SIGNING_CERT=$DEVELOPER_ID_APPLICATION

MODS=`jdeps --ignore-missing-deps --module-path ~/Documents/javafx-sdk-21.0.1/lib --print-module-deps input/qcapp.jar`
echo $MODS

rm -r us
rm -r QuantumJava.app
#`/usr/libexec/java_home -v 19.0.2`/bin/javac -cp input/book.jar:input/strange-0.1.4.jar:input/strangefx-0.1.5.jar -d . --module-path ~/Documents/javafx-sdk-19/lib --add-modules javafx.graphics,javafx.controls,javafx.fxml,jdk.compiler src/*.java
javac -cp input/book.jar:input/strange-0.1.4.jar:input/strangefx-0.1.5.jar:input/stringtemplate-3.2.1.jar:input/py4j0.10.9.7.jar -d . --module-path ~/Documents/javafx-sdk-21.0.1/lib --add-modules javafx.graphics,javafx.controls,javafx.fxml,jdk.compiler src/*.java
cp split.fxml us/hall/qcapp/split.fxml
#cp import.fxml us/hall/qcapp/import.fxml
#cp export.fxml us/hall/qcapp/export.fxml
cp strangelogo.png us/hall/qcapp/strangelogo.png
cp strange.stg us/hall/qcapp/strange.stg
cp qiskit.stg us/hall/qcapp/qiskit.stg
cp styles.css us/hall/qcapp/styles.css

jar -cvf qcapp.jar us
mv qcapp.jar input/qcapp.jar

${PACKAGER} \
	--verbose \
	--input input \
	--type "app-image" \
	--name QuantumJava \
	--main-jar qcapp.jar \
	--main-class us.hall.qcapp.QuantumJava \
	--java-options -Dapple.awt.application.name=QuantumJava \
	--mac-package-identifier "us.hall.quantumjava" \
	--mac-sign \
	--mac-signing-key-user-name "$SIGNING_CERT" \
	--module-path ~/Documents/javafx-jmods-21.0.1 \
	--add-modules ${MODS},jdk.compiler,javafx.controls,javafx.graphics,java.logging \
	--jlink-options '--strip-debug --no-man-pages --no-header-files' 
	

	
	

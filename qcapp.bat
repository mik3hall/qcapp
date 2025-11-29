set PATH=C:\Program Files\jdk-25.0.1\bin;%PATH%

jpackage ^
	--verbose ^
	--input input ^
	--name QuantumJava ^
	--win-menu ^
	--win-shortcut ^
	--win-shortcut-prompt ^
	--main-jar qcapp.jar ^
	--main-class us.hall.qcapp.QuantumJava ^
	--module-path ..\javafx-jmods-25.0.1 ^
	--jlink-options "--strip-debug --no-man-pages --no-header-files" ^
	--add-modules jdk.compiler,java.xml,javafx.controls,javafx.fxml,java.logging 

	

	
	

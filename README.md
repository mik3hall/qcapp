# qcapp

A quantum computing simulation application originally built on the java 'strange' API indicated in the immediately following section. It has been extended with an XML import/export format for
quantum circuits. With the current update there is additional support for using the IBM Qiskit API in python.<br/>

**11/28/25** No code changes to mention. Mostly have been trying to get VirtualBox Unix and Windows installers for the application that I added to the releases. I still have to figure out python and how to hook it up on these platforms. Windows doesn't seem as if it should be all that
different. For the jpackage invocations, qcapp_deb.sh - Unix and qcapp.bat - Windows.
 
_____________________________________
**11/16/25** Cirq scripts can be run. bell_state_cirq.py is the only current test case. That may have an extra Hadamard initially set for the target qubit. It occurred to me that this only ensures an entangled state if that qubit is initially in a 0 state. There doesn't seem to be a 'zero' gate, that deterministically ensures this. It does seem to be an assumption of all the API's that the qubits start out in a known 0 state. This seems a little odd to me. The application rendering the target qubit offset is a bug. Cirq scripts can't get generated from XML yet. The 'Open...' option from the application will try to determine whether a .py file is Qiskit or Cirq. Some more GUI control of the interchange options, saving program source or XML resulting from actions will probably be in the next upcoming update. Some additional cleanup has been done.

_______________________________________
This update involves the IBM qiskit python quantum computing api and the XML support. You can run qiskit code using the File menu 
'Open...' option. Qiskit is installed to a python virtual environment. This takes a while. Subsequent executions of qiskit are faster.
There is now has a wrapper script that activates the qiskit python virtual environment and then finds and converts the qiskit QuantumCircuit 
to XML using python introspecition. Following that a java StrangeProgram is built from that XML to render the circuit to the application display area. Standard output from the python still appears in the console pane. 

This demonstrates the use of XML as an interchange format for quantum circuit api's. It could also be used as a code generator from the XML quantum circuit representation to any of these API's. So far, that would have to be only from qiskit XML to Strange, or Strange to Strange. The round trip from XML to Qiskit code still needs to be done. Once that is complete some way to indicate which API to generate code for needs to be provided. Either through the GUI interface or application specific XML meta data. 
I don't remember exactly what I was looking at for either of these options. So, both are 
subject to change. There is currently only basic boilerplate for any code not involving the 
construction of the quantum circuit. 

The application is written in Java but is being developed and currently only tested on MacOS. A Unix version should be doable, but I'm not sure 
how some of the shell script based would be handled on Windows. I'll add having a linux app version to the to-do list.

Anyhow it's still very much a work in progress that I'm not going to try and do a lot of 
cleanup or explaining now. 

_______________________________________

This is based on the book "Quantum Computing in Action" - Johan Vos, and related code. 

[Quantum Computing in Action - Amazon](https://www.amazon.com/Quantum-Computing-Developers-Johan-Vos/dp/1617296325)

Related GitHub projects:<br/>
[quantumjava](https://github.com/johanvos/quantumjava)<br/>
[strange](https://github.com/redfx-quantum/strange) <br/>
[strangefx](https://github.com/redfx-quantum/strangefx)

Please refer to these concerning appropriate licenses for changes I have made.

This also makes use of QIS-XML for converting the quantum programs/circuits to and from XML.

[QIS-XML](https://arxiv.org/pdf/1106.2684)<br/>

A couple, I think slightly different versions, of the PDF are included here.

This attempts to make the original somewhat more IDE-like and extensible. The idea is you can open and run strange quantum java code. You can export that to an XML format. That reading that XML back in and generating the equivalent java source code. 

Or, the thought would be, export something else to the same common, language independent, XML format. Also, import the XML back into something other than strange java. Currently I am thinking trying to support IBM Qiskit python next. 

[qiskit](https://www.ibm.com/quantum/qiskit)

For now it does a reasonably well at exporting an executed strange programs QubitBoard to XML. Import back to strange java source. It seemed like this could use some application specific meta information. Should it be rendered? What should be the program display name? What should it's classname be? That's java specific which seems necessary here. The place to include this I think should be meta data in the XML file. That part hasn't been done yet. 

I have been working on modifying the GUI to allow setting this meta information. That still has rendering issues. My test instances are Main.java and Main.xml. These are versions the quantumjava chapter 7 randombitdebug example. Qubit 4 which should be a Pauli X gate doesn't appear. The label is there as well as the measurement UI. All the QubitFlow between isn't showing. 

Not being used to JavaFX the use of Group as the QubitBoard class has given me difficulties. It took me some time to realize it had to have the entire scene width. To add the meta UI it again seemed a problem. I ended up taking the children from the QubitBoard and adding them to a VBox. Then the current rendering problems. 

To import the XML to Java I also make use of...<br/>
[stringtemplate](https://www.stringtemplate.org)

I'll have to see more what the JDK provides for this to use it.

What changes I have made to the strange and strangefx github projects are in the changes directory. These didn't seem to merit my own branches. 

I found the display for ProbablitiesGate sort of difficult to interpret. I added something so if you click on one of the cells you get a pop up I think is easier to understand. I have considered using something similar for the MeasurementUI but haven't.

The "Book" menu examples all run in their own scene with their own windows. The java in the bookcode directory shows how these can be changed to run in the application display.

The dmg contains an old version. I will probably provide a release this way sometime.

I think this mostly explains the project current contents and it's current status. 

1. 
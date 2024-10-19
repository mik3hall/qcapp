# qcapp

A application related to quantum computing.<br/>
Incomplete at this point.

Written in Java but developed and currently only tested on MacOS.

This is based on the book "Quantum Computing in Action" - Johan Vos, and related code. 

[Quantum Computing in Action - Amazon](https://www.amazon.com/Quantum-Computing-Developers-Johan-Vos/dp/1617296325)

Related GitHub projects:<br/>
[quantumjava](https://github.com/johanvos/quantumjava)<br/>
[strange](https://github.com/redfx-quantum/strange) <br/>
[strangefx](https://github.com/redfx-quantum/strangefx)

Please refer to these concerning appropriate licenses for changes I have made.

This also makes use of QIS-XML for converting the quantum programs/circuits to and from XML.

[QIS-XML](https://arxiv.org/pdf/1106.2684)<br/>

A couple, I think slightly different versios, of the PDF are included here.

This attempts to make the original somewhat more IDE-like and extensible. The idea is you can open and run strange quantum java code. You can export that to an XML format. Currently in progress is reading that XML back in and generating the equivalent java source code. 

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


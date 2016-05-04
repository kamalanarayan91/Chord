all:Utilities.class Node.class FixFingersThread.class StabilizationThread.class 

%.class: %.java
	javac $<

clean:
	rm -f *.class

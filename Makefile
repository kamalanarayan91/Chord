all:Utilities.class Node.class FixFingersThread.class StabilizationThread.class Hasher.class TestSuite.class

%.class: %.java
	javac $<

clean:
	rm -f *.class

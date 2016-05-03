all: Node.class 

%.class: %.java
	javac $<

clean:
	rm -f *.class

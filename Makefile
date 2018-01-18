CLASSPATH := Lib/antlr-4.7.jar:.

ANTLR:=Micro.g4
compiler:
	rm -rf build
	mkdir build
	java -cp $(CLASSPATH) org.antlr.v4.Tool -o build $(ANTLR)
	rm -rf classes
	mkdir classes
	javac -d classes -bootclasspath /usr/lib/jvm/jre-1.7.0/lib/rt.jar -source 1.7 -target 1.7 -cp $(CLASSPATH) build/*.java src/*.java

clean:
	rm -rf build classes
team:
	@echo
	@echo "Team: EzpzCompiler"
	@echo
	@echo "Yuchen Sun"
	@echo "nicksun9"
	@echo
	@echo "Rishab Verma"
	@echo "verma24"
	@echo

.PHONY: team

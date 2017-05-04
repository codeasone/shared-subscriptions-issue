clean:
	mvn clean

build: clean
	mvn package

run:
	mvn org.codehaus.mojo:exec-maven-plugin:1.5.0:java -Dexec.mainClass="io.github.codeasone.SharedSubscriptionsIssue"

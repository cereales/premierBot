CC=javac
CLASSPATH=-classpath target/classes/
SOURCEPATH=-sourcepath src/
DESTPATH=-d target/classes/
GAMEPATH=src/main/java/game
DIR=jarDirectory

# Debug
all: init


### Internal
init:
	rm -rf $(DIR)
	mkdir $(DIR)
	cp classes/artifacts/JDA_jar/JDA.jar $(DIR)
	cp database.txt $(DIR)
	cp databaseUsers.txt $(DIR)

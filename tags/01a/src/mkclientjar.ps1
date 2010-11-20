# I'am add option -Xlint:unchecked because compilet notice me what "some input files use unchecked or unsafe operations".
javac -encoding "UTF-8" -Xlint:unchecked client\*.java
jar cmf MANIFEST.MF WD.jar img client/*.class

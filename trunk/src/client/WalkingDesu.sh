#!/bin/sh
echo `pwd`
javac WalkingDesu.java Sprites.java
cd ..
java client/WalkingDesu

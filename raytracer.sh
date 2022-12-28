#!/bin/sh

if [ "$#" -gt "1" ];  then
	echo "usage: $0 <image count>"
	exit 1
fi

COUNT=1
if [ "$#" -eq "1" ]; then
	COUNT="$1"
fi

mkdir -p output
MAVEN_OPTS="-ea" mvn -q -e exec:java -Dexec.mainClass="App" -Dexec.args="compile raytracer.calc" || exit 1
seq $COUNT | xargs -P0 -I{} sh -c "java -Xss16m -cp calc_target/ Main > output/{}.ppm"
cd output
ls -1 *.ppm | xargs -I{} basename {} .ppm | xargs -P0 -I{} convert "{}.ppm" "{}.png"

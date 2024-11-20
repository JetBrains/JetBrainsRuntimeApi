# $1 - Mode (process/dev/full/<version>)
# $2 - Boot JDK path (Java 18+)
# $3 - Output path - *not supported on Windows

# When running on Windows (WSL/Cygwin),
# all paths must be Unix-style!

# Modes explained:
#   process   - run annotation processor, check for errors, output messages & API diff.
#   dev       - build jbr-api-SNAPSHOT.jar.
#   full      - build jbr-api-<version>.jar, jbr-api-<version>-sources.jar, jbr-api-<version>-javadoc.jar and jbr-api-<version>.pom.
#   <version> - full build with manually overridden version.

# Init variables.
RUN_DIR="`pwd`"

if [ "$1" = "process" ] || [ "$1" = "dev" ] || [ "$1" = "full" ] ; then
  MODE="$1"
  OVERRIDE_API_VERSION=
else
  MODE="full"
  OVERRIDE_API_VERSION="-Aversion=$1"
fi

if [ "x$2" = "x" ] ; then
  JAVAC="javac"
  JAR="jar"
  JSHELL="jshell"
  JAVADOC="javadoc"
else
  cd "$2/bin"
  JAVA_BIN="`pwd`"
  JAVAC="$JAVA_BIN/javac"
  JAR="$JAVA_BIN/jar"
  JSHELL="$JAVA_BIN/jshell"
  JAVADOC="$JAVA_BIN/javadoc"
  cd "$RUN_DIR"
fi

if [ "x$3" = "x" ] ; then
  OUT="out"
  cd "`dirname $0`/.."
  mkdir -p "$OUT"
else
  mkdir -p "$3"
  cd "$3"
  OUT="`pwd`"
  cd "$RUN_DIR"
  cd "`dirname $0`/.."
fi

# Check javac.
$JAVAC --help &> /dev/null || JAVAC="${JAVAC}.exe"
$JAVAC --help &> /dev/null || {
  echo "javac not found"
  exit 1
}

# Prepare output directories.
TOOLS_OUT="$OUT/classes/tools"
mkdir -p "$TOOLS_OUT/META-INF/services"
echo "ApiProcessor" > "$TOOLS_OUT/META-INF/services/javax.annotation.processing.Processor"
echo "CompatibilityPlugin" > "$TOOLS_OUT/META-INF/services/com.sun.source.util.Plugin"

# Generic compiler options.
SMALLJAVA_FLAGS="-J-XX:+UseSerialGC -J-Xms32M -J-Xmx512M -J-XX:TieredStopAtLevel=1"
JAVAC="$JAVAC $SMALLJAVA_FLAGS -g -Xlint:all -implicit:none -Xprefer:source -XDignore.symbol.file=true -encoding ascii"
if [ "$MODE" != "dev" ] ; then
  JAVAC="$JAVAC -Werror"
fi

# Compile tools.
$JAVAC -d $TOOLS_OUT \
  --add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
  --add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
  --add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
  tools/javac/*.java || exit 1

# Configure options for using tools.
JAVAC="$JAVAC
  -J--add-exports -Jjdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
  -J--add-exports -Jjdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
  -J--add-exports -Jjdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
  -sourcepath src -processorpath $TOOLS_OUT"

# -proc:only skips analyzing step, so some compile errors and DocLint warnings are not reported.
# Therefore do real compilation to make sure we get all possible warnings & errors.

# How to debug compiler plugin/processor?
#JAVAC="$JAVAC -J-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"

# Compile twice for Java 9 and Java 8 bytecode.
rm -rf "$OUT/classes/8"
rm -rf "$OUT/classes/9"
mkdir -p "$OUT/gensrc"
$JAVAC -d $OUT/classes/9 -s $OUT/gensrc --release 9 -Xdoclint:all/protected -Aoutput="$OUT" $OVERRIDE_API_VERSION src/com/jetbrains/*.java src/module-info.java || {
  echo -e "\u2757 Compilation failed, see log for the details." > "$OUT/message.txt"
  exit 1
}
if [ "$MODE" = "process" ] ; then
  exit 0
fi
$JAVAC -d $OUT/classes/8 -proc:none -Xlint:-options --release 8 -Xplugin:"CompatibilityPlugin $OUT/classes/8 $OUT/classes/9" "@$OUT/sourcelist8.txt" || {
  echo -e "\u2757 Compilation failed, see log for the details." > "$OUT/message.txt"
  exit 1
}

# Read API version.
if [ "$MODE" = "dev" ] ; then
  # Use SNAPSHOT version in dev mode.
  echo -n "SNAPSHOT" > $OUT/version.txt
fi
API_VERSION="`cat $OUT/version.txt`"
JAR_OUT="$OUT/jbr-api-$API_VERSION.jar"
SOURCES_OUT="$OUT/jbr-api-$API_VERSION-sources.jar"
JAVADOC_OUT="$OUT/jbr-api-$API_VERSION-javadoc.jar"
POM_OUT="$OUT/jbr-api-$API_VERSION.pom"

# Check jar.
$JAR --help &> /dev/null || JAR="${JAR}.exe"
$JAR --help &> /dev/null || {
  echo "jar not found"
  exit 1
}
JAR="$JAR $SMALLJAVA_FLAGS"

# Create jar.
if [ "$API_VERSION" = "SNAPSHOT" ] ; then
  JAR_MODULE_VERSION=
else
  JAR_MODULE_VERSION="--module-version=$API_VERSION"
fi
$JAR --create --file="$JAR_OUT" --manifest="tools/templates/MANIFEST.MF" $JAR_MODULE_VERSION \
  -C "$OUT/classes/8" . --release 9 -C "$OUT/classes/9" . || {
  echo -e "\u2757 JAR creation failed, see log for the details." >> "$OUT/message.txt"
  exit 1
}
if [ "$MODE" = "dev" ] ; then
  exit 0
fi

# Verify reported API version.
$JSHELL --help &> /dev/null || JSHELL="${JSHELL}.exe"
$JSHELL --help &> /dev/null || {
  echo "jshell not found"
  exit 1
}
echo -e "System.out.println(com.jetbrains.JBR.getApiVersion());\n/exit" > "$OUT/version.jsh"
REPORTED_VERSION=`$JSHELL --module-path "$JAR_OUT" --add-modules jetbrains.runtime.api --feedback silent "$OUT/version.jsh"`
if [ "$API_VERSION" != "$REPORTED_VERSION" ] ; then
  echo "Invalid API version, expected: $API_VERSION, reported: $REPORTED_VERSION"
  exit 1
fi

# Create source jar.
$JAR --create --file="$SOURCES_OUT" -C "src" . -C "$OUT/gensrc" . || {
  echo -e "\u2757 Source JAR creation failed, see log for the details." >> "$OUT/message.txt"
  exit 1
}

# Check javadoc.
$JAVADOC --help &> /dev/null || JAVADOC="${JAVADOC}.exe"
$JAVADOC --help &> /dev/null || {
  echo "javadoc not found"
  exit 1
}
JAVADOC="$JAVADOC $SMALLJAVA_FLAGS"

# Generate javadoc.
JAVADOC_TAGS="
-tag param
-tag return
-tag throws
-tag since
-tag see
"
FONT_MONO='"DejaVu Sans Mono"'
rm -rf "$OUT/javadoc"
$JAVADOC -d "$OUT/javadoc" -public --release 9 -Xdoclint:all -encoding ascii -quiet $JAVADOC_TAGS \
  -doctitle "JBR API v$API_VERSION" \
  -windowtitle "JBR API v$API_VERSION" \
  -header "<h1 style='margin:10px 0px 0px 0px;font-family:$FONT_MONO;'>JBR API v$API_VERSION</h1>" \
   "src/module-info.java" "@$OUT/sourcelist8.txt" || {
  echo -e "\u2757 Javadoc generation failed, see log for the details." >> "$OUT/message.txt"
  exit 1
}

# Fix fonts.
mkdir -p "$OUT/javadoc/resources/fonts"
cp "tools/templates/dejavu.css" "$OUT/javadoc/resources/fonts"

# Create javadoc JAR.
$JAR --create --file="$JAVADOC_OUT" -C "$OUT/javadoc" . || {
  echo -e "\u2757 Javadoc JAR creation failed, see log for the details." >> "$OUT/message.txt"
  exit 1
}

# Create pom.
cp "tools/templates/pom.xml" "$POM_OUT"
sed -i -e "s/{{version}}/$API_VERSION/g" "$POM_OUT"

# When reading message.txt, redirect stderr to /dev/null just in case file doesn't exist.
#echo "`cat "$OUT/message.txt" 2> /dev/null`"

echo "Calculating checksums"
# The output is a line with checksum,
# a character indicating type ('*' for --binary, ' ' for --text),
# and the supplied file argument (hence dropping the "$OUT/" prefix)
cd "$OUT"
sha256sum --binary "${JAR_OUT#"$OUT/"}" > "${JAR_OUT#"$OUT/"}.sha256"
sha256sum --binary "${SOURCES_OUT#"$OUT/"}" > "${SOURCES_OUT#"$OUT/"}.sha256"
sha256sum --binary "${JAVADOC_OUT#"$OUT/"}" > "${JAVADOC_OUT#"$OUT/"}.sha256"
sha256sum --binary "${POM_OUT#"$OUT/"}" > "${POM_OUT#"$OUT/"}.sha256"
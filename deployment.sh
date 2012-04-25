THE_CLASSPATH=
PROGRAM_NAME=./com/cleandroid/CleanDroid.java
EXECUTE_NAME=com/cleandroid/CleanDroid

cd src
for i in `ls ../lib/*.jar`
  do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done

javac -d ../bin -classpath  ".:${THE_CLASSPATH}" $PROGRAM_NAME

if [ $? -eq 0 ]
then
  echo "compile worked!"
fi

cd ..
cd bin
echo $THE_CLASSPATH
java -classpath ".:${THE_CLASSPATH}" $EXECUTE_NAME

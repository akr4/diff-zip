diff-zip
========================
List different files between two zip files.

Usage
------------------------------
::

  Usage: diff-zip [OPTIONS] ZIP1 ZIP2
  
  OPTIONS
  
  -d
  --detect-dir  Detect directory change
  
  PARAMETERS
  
  ZIP1  zip file 1
  
  ZIP2  zip file 2

Example
------------------
::

  $ sbt one-jar
  
  $ java -jar **/diff-zip_*-one-jar.jar commons-lang-2.5.jar commons-lang-2.6.jar
  Files exist only in commons-lang/2.5/commons-lang-2.5.jar
  ===========================================
  
  Files exist only in commons-lang/2.6/commons-lang-2.6.jar
  ===========================================
  org/apache/commons/lang/exception/CloneFailedException.class
  
  Files exist in both but not same
  ===========================================
  META-INF/MANIFEST.MF
  META-INF/NOTICE.txt
  META-INF/maven/commons-lang/commons-lang/pom.properties
  META-INF/maven/commons-lang/commons-lang/pom.xml
  org/apache/commons/lang/ArrayUtils.class
  org/apache/commons/lang/BooleanUtils.class
  org/apache/commons/lang/CharRange$CharacterIterator.class
  ...

License
---------
Copyright 2012 Akira Ueda

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0

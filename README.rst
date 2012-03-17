diff-zip
========================
List different files between two zip files.

Usage
------------------------------

  $ sbt
  sbt> stage
  
  $ ./target/start commons-lang-2.5.jar commons-lang-2.6.jar
  Exists only in commons-lang-2.5.jar
  ===========================================
  
  Exists only in commons-lang-2.6.jar
  ===========================================
  org/apache/commons/lang/exception/CloneFailedException.class
  
  Exists in both but not same
  ===========================================
  org/apache/commons/lang/time/DurationFormatUtils.class
  org/apache/commons/lang/StringEscapeUtils.class
  org/apache/commons/lang/Entities$ArrayEntityMap.class
  org/apache/commons/lang/math/LongRange.class
  org/apache/commons/lang/time/FastDateFormat$NumberRule.class
  ...

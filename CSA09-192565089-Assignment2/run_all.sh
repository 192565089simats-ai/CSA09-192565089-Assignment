#!/usr/bin/env bash
# Compiles and runs every program, saving the console output next to the source as output.txt
# Usage:  bash run_all.sh        (needs a JDK: javac + java, Java 17 or later)
set -e
cd "$(dirname "$0")"
run() {            # run <dir> <MainClass> <source.java> <out-file>
  d="$1"; main="$2"; src="$3"; out="$4"
  mkdir -p "$d/.classes"
  javac -d "$d/.classes" "$d/$src"
  java -cp "$d/.classes" "$main" | tee "$d/$out"
  rm -rf "$d/.classes"
  echo "---- saved $d/$out ----"
}
run Task01          Q1Ticket        Q1Ticket.java       output.txt
run Task02          Q2Playlist      Q2Playlist.java     output.txt
run Task03          Q3Experiments   Q3Experiments.java  output.txt
run Task03/before   Attendance      Attendance.java     output.txt
run Task03/after    Attendance      Attendance.java     output.txt
run Task04          Q4Pharmacy      Q4Pharmacy.java     output.txt
run Task05/design   Q5Design        Q5Design.java       output.txt
run Task05/complete LeaderboardDemo LeaderboardDemo.java output.txt
run Task06/design   Q6Triage        Q6Triage.java       output.txt
run Task06/complete Triage          Triage.java         output.txt
run Task07          Q7Redesign      Q7Redesign.java     output.txt

#!/usr/bin/env bash
# One commit per task. Run ONE block at a time, as you finish and re-check each task on your own computer,
# so that the commit history shows real progressive work (a single upload on the last day is not accepted).
# First-time setup (run once):
#   git init && git branch -M main
#   git config user.name  "RITHISH KUMAR D"
#   git config user.email "<your e-mail>"
#   git remote add origin https://github.com/<your-username>/CSA09-192565089-Assignment2.git

# ---- Task 1 ----
git add README.md run_all.sh Task01 && git commit -m "Q1: String vs StringBuilder vs StringBuffer - code, output, analysis"
# ---- Task 2 ----
git add Task02 && git commit -m "Q2: ArrayList vs LinkedList vs ArrayDeque experiments"
# ---- Task 3 ----
git add Task03 && git commit -m "Q3: hashCode-equals contract experiments and Attendance (before/after TODO 1)"
# ---- Task 4 ----
git add Task04 && git commit -m "Q4: HashMap vs TreeMap vs LinkedHashMap pharmacy inventory"
# ---- Task 5 ----
git add Task05 && git commit -m "Q5: generic Leaderboard, comparator design and LeaderboardDemo"
# ---- Task 6 ----
git add Task06 && git commit -m "Q6: PriorityQueue triage design, fairness rule and Triage program"
# ---- Task 7 ----
git add Task07 && git commit -m "Q7: diagnose, redesign and validate (sorted-index inventory)"
# ---- Report ----
git add REPORT.md git_commits.sh && git commit -m "Add consolidated report"
git push -u origin main

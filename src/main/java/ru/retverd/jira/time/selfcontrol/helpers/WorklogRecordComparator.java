package ru.retverd.jira.time.selfcontrol.helpers;

import java.util.Comparator;

public class WorklogRecordComparator implements Comparator<WorklogRecord> {
	public int compare(WorklogRecord wr1, WorklogRecord wr2) {
		return wr1.getIssueKey().compareTo(wr2.getIssueKey());
	}
}
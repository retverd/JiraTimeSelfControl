package ru.retverd.jira.time.selfcontrol.helpers;

import java.io.PrintWriter;

public class WorklogRecord {
	private String issueKey;
	private String issueDescr;
	private String comment;
	private int minutesSpent;

	public WorklogRecord(String issueKey, String issueDescr, String comment, int minutesSpent) {
		this.issueKey = issueKey;
		this.issueDescr = issueDescr;
		this.minutesSpent = minutesSpent;
		this.comment = comment;
	}

	public String getIssueKey() {
		return issueKey;
	}

	public int getMinutesSpent() {
		return minutesSpent;
	}

	public void saveWorklog(PrintWriter writer) {
		writer.format("%s: %s; Hours spent: %.2f; Comment: %s\r\n", issueKey, issueDescr, ((double) minutesSpent / 60), comment);
	}
}
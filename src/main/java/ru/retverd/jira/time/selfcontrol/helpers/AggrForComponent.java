package ru.retverd.jira.time.selfcontrol.helpers;

import java.io.PrintWriter;

public class AggrForComponent {
	private int minutesSpent;

	public AggrForComponent() {
		this.minutesSpent = 0;
	}

	public int getMinutesSpent() {
		return minutesSpent;
	}

	public void addMinutesSpent(int minutesSpent) {
		this.minutesSpent += minutesSpent;
	}

	public void saveAggrComponentReport(PrintWriter writer) {
		writer.format("; Hours spent: %.2f\r\n", ((double) minutesSpent / 60));
	}
}
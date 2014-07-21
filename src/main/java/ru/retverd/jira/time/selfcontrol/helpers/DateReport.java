package ru.retverd.jira.time.selfcontrol.helpers;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Worklog;

public class DateReport {
	private TreeMap<String, PersonalReport> personalReportsList;

	public DateReport() {
		personalReportsList = new TreeMap<String, PersonalReport>();
	}

	public void addWorklog(Issue issue, String moduleName, String componentName, Worklog worklog) throws Exception {
		if (!personalReportsList.containsKey(worklog.getAuthor().getName())) {
			personalReportsList.put(worklog.getAuthor().getName(), new PersonalReport());
		}
		personalReportsList.get(worklog.getAuthor().getName()).addWorklog(issue, moduleName, componentName, worklog);
	}

	public void saveDateReport(PrintWriter writer) {
		if (personalReportsList.size() == 0) {
			writer.println("No data found for specified date!");
		} else {
			for (Map.Entry<String, PersonalReport> entry : personalReportsList.entrySet()) {
				writer.println("Time spent by " + entry.getKey());
				entry.getValue().savePersonalReport(writer);
			}
		}
	}
}
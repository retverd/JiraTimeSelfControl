package ru.retverd.jira.time.selfcontrol.helpers;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Worklog;

public class PersonalReport {
	private ArrayList<WorklogRecord> worklogsList;
	private TreeMap<String, AggrForModule> modulesList;
	private int totalSpentMinutes = 0;

	public PersonalReport() {
		worklogsList = new ArrayList<WorklogRecord>();
		modulesList = new TreeMap<String, AggrForModule>();
	}

	public int getTotalSpentMinutes() {
		return totalSpentMinutes;
	}

	public void addWorklog(Issue issue, String moduleName, String componentName, Worklog worklog) throws Exception {
		WorklogRecord record = new WorklogRecord(issue.getKey(), issue.getSummary(), worklog.getComment(), worklog.getMinutesSpent());
		worklogsList.add(record);

		if (!modulesList.containsKey(moduleName)) {
			modulesList.put(moduleName, new AggrForModule());
		}
		totalSpentMinutes += record.getMinutesSpent();
		modulesList.get(moduleName).addWorklog(componentName, worklog);
	}

	public void savePersonalReport(PrintWriter writer) {
		if (worklogsList.size() == 0) {
			writer.println("No data found for specified person!");
		} else {
			Collections.sort(worklogsList, new WorklogRecordComparator());
			writer.println("Aggreration for reports");
			for (Map.Entry<String, AggrForModule> entry : modulesList.entrySet()) {
				writer.println("Module: \"" + entry.getKey() + "\"");
				entry.getValue().saveAggrModuleReport(writer);
			}

			writer.println("Details");
			for (WorklogRecord worklog : worklogsList) {
				worklog.saveWorklog(writer);
			}
			writer.format("Total hours spent: %.2f\r\n\r\n", ((double) totalSpentMinutes / 60));
		}
	}
}
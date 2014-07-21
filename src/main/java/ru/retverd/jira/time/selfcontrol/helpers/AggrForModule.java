package ru.retverd.jira.time.selfcontrol.helpers;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.rest.client.api.domain.Worklog;

public class AggrForModule {
	private TreeMap<String, AggrForComponent> componentsList;

	public AggrForModule() {
		componentsList = new TreeMap<String, AggrForComponent>();
	}

	public void addWorklog(String componentName, Worklog worklog) throws Exception {
		if (!componentsList.containsKey(componentName)) {
			componentsList.put(componentName, new AggrForComponent());
		}
		componentsList.get(componentName).addMinutesSpent(worklog.getMinutesSpent());
	}

	public void saveAggrModuleReport(PrintWriter writer) {
		for (Map.Entry<String, AggrForComponent> entry : componentsList.entrySet()) {
			writer.format("\tComponent: \"" + entry.getKey() + "\"");
			entry.getValue().saveAggrComponentReport(writer);
		}
	}
}
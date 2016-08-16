/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package remote.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author tom
 */
public class TaskUtils {
	public static void createTestExtProperties(
		Path filePath, String dbType) throws IOException {

		Properties properties = PropertiesUtil.loadProperties(
			Paths.get("build.properties"));

		StringBuilder sb = new StringBuilder();

		sb.append("liferay.home=");
		sb.append(properties.getProperty("liferay.home"));
		sb.append('\n');
		sb.append("lp.plugins.dir=");
		sb.append(properties.getProperty("lp.plugins.dir"));
		sb.append('\n');
		sb.append("jdbc.default.driverClassName=");
		sb.append(
			properties.getProperty(dbType + ".jdbc.default.driverClassName"));
		sb.append('\n');
		sb.append("jdbc.default.url=");
		sb.append(properties.getProperty(dbType + ".jdbc.default.url"));
		sb.append('\n');
		sb.append("jdbc.default.username=");
		sb.append(properties.getProperty(dbType + ".jdbc.default.username"));
		sb.append('\n');
		sb.append("jdbc.default.password=");
		sb.append(properties.getProperty(dbType + ".jdbc.default.password"));
		sb.append('\n');

		if (Files.exists(filePath)) {
			List<String> lines = Files.readAllLines(filePath);

			for (String line : lines) {
				if (!line.startsWith("jdbc") &&
					!line.startsWith("liferay.home") &&
					!line.startsWith("lp.plugins.dir")) {

					sb.append(line);
					sb.append('\n');
				}
			}

			Files.delete(filePath);
		}

		if (!Files.exists(filePath)) {
			Files.createFile(filePath);
		}

		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(
				filePath, Charset.defaultCharset(), StandardOpenOption.APPEND))
		{
			bufferedWriter.append(sb);
		}
	}

	public static void runDeployDaoDB(Path gradlewPath, Path portalPath)
		throws IOException {

		List<String> deployTask = new ArrayList<>();

		deployTask.add(gradlewPath.toString());
		deployTask.add("deploy");

		ProcessBuilder processBuilder = new ProcessBuilder(deployTask);

		Path daoDBPath = portalPath.resolve(
			Paths.get("modules/private/apps/foundation/portal/portal-dao-db"));

		processBuilder.directory(daoDBPath.toFile());

		Process process = processBuilder.start();

		String line;

		try(BufferedReader br = new BufferedReader(
			new InputStreamReader(process.getInputStream()))) {

			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		}

		try(BufferedReader br = new BufferedReader(
			new InputStreamReader(process.getErrorStream()))) {

			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		}
	}

	public static void startTunnel(String dbType) throws Exception {
		Properties properties = PropertiesUtil.loadProperties(
			Paths.get("build.properties"));

		List<String> tunnelTask = new ArrayList<>();

		String remoteHost = properties.getProperty("remote.host");

		tunnelTask.add("ssh");
		tunnelTask.add("-f");
		tunnelTask.add(
			properties.getProperty("remote.username") + "@" + remoteHost);
		tunnelTask.add("-L");
		tunnelTask.add(
			"9999:" + remoteHost + ":" +
				properties.getProperty(dbType + ".port"));
		tunnelTask.add("-N");

		ProcessBuilder processBuilder = new ProcessBuilder(tunnelTask);

		Process process = processBuilder.start();

		process.waitFor();
	}
}

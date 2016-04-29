/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package remote.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import remote.util.PropertiesUtil;
import remote.util.TaskUtils;

/**
 * @author Tom Wang
 */
public class TestTask extends Task {

	@Override
	public void execute() throws BuildException {
		try {
			Properties properties = PropertiesUtil.loadProperties(
			Paths.get("build.properties"));

			TaskUtils.startTunnel(_dbType);

			Path fullPath = _findTest(properties);

			if (fullPath != null) {
				Path portalDir = Paths.get(
					properties.getProperty("portal.dir"));

				Path portalImplPath = portalDir.resolve("portal-impl");

				if (fullPath.startsWith(portalImplPath)) {
					_runPortalImplTests(portalImplPath, properties);
				}

				if (fullPath.startsWith(portalDir.resolve("modules"))) {
					_runModuleTests(fullPath, properties);
				}
			}
			else {
				System.out.println(_testClass + " is not found.");
			}
		}
		catch (Exception ex) {
			Logger.getLogger(
				TestTask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void setDBType(String dbType) {
		_dbType = dbType;
	}

	public void setTestClass(String testClass) {
        _testClass = testClass;
    }

	private Path _findTest(Properties properties) throws IOException {
		final String blackListDirs = properties.getProperty(
				"blackListDirs");

		Path portalDir = Paths.get(properties.getProperty("portal.dir"));

		Files.walkFileTree(portalDir, new SimpleFileVisitor<Path>() {
			@Override
				public FileVisitResult preVisitDirectory(
						Path path, BasicFileAttributes basicFileAttributes)
					throws IOException {

					Path pathFileName= path.getFileName();

					if (blackListDirs.contains(pathFileName.toString())) {
						return FileVisitResult.SKIP_SUBTREE;
					}

					return FileVisitResult.CONTINUE;
				}

			@Override
			public FileVisitResult visitFile(
				Path path, BasicFileAttributes basicFileAttributes) {

				Path pathFilename = path.getFileName();

				String pathFilenameString = pathFilename.toString();

				if (pathFilenameString.equals(_testClass + ".java")) {
					_setPath(path);
				}
				return FileVisitResult.CONTINUE;
			}

		});

		if (_path == null) {
			return null;
		}

		return _path;
	}

	private Path _resolveModulePath(Path fullPath) {
		Path currentPath = fullPath;
		Path currentPathName = currentPath.getFileName();

		String currentPathNameString = currentPathName.toString();

		while (!currentPathNameString.equals("src")) {
			currentPath = currentPath.getParent();
			currentPathName = currentPath.getFileName();
			currentPathNameString = currentPathName.toString();
		}

		return currentPath.getParent();
	}

	private void _runModuleTests(Path fullPath, Properties properties)
		throws Exception {

		Path implSrcPath = Paths.get(
			properties.getProperty("portal.dir"), "portal-impl/src");

		Path portalExtPath = implSrcPath.resolve("portal-ext.properties");

		Path backupFilePath = implSrcPath.resolve(
			"portal-ext.properties.backup");

		if (Files.exists(portalExtPath)) {
			Files.copy(
				portalExtPath, backupFilePath,
				StandardCopyOption.REPLACE_EXISTING);
		}

		TaskUtils.createTestExtProperties(portalExtPath, _dbType);

		Path tomcatPortalExtPath = Paths.get(
			properties.getProperty("tomcat.dir"),
			"webapps/ROOT/WEB-INF/classes/portal-ext.properties");

		Files.copy(
				portalExtPath, tomcatPortalExtPath,
				StandardCopyOption.REPLACE_EXISTING);

		Path portalPath = Paths.get(properties.getProperty("portal.dir"));

		Path gradlewPath = portalPath.resolve("gradlew");

		if (_dbType.equals("db2") || _dbType.equals("oracle") ||
			_dbType.equals("sybase")) {
			TaskUtils.runDeployDaoDB(gradlewPath, portalPath);
		}

		Path modulePath = _resolveModulePath(fullPath);

		List<String> testTask = new ArrayList<>();

		testTask.add(gradlewPath.toString());
		testTask.add("testIntegration");
		testTask.add("--tests");
		testTask.add("*." + _testClass);

		ProcessBuilder processBuilder = new ProcessBuilder(testTask);

		processBuilder.directory(modulePath.toFile());

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

		Files.delete(tomcatPortalExtPath);

		if (Files.exists(backupFilePath)) {
			Files.move(
				backupFilePath, portalExtPath,
				StandardCopyOption.REPLACE_EXISTING);

			Files.copy(
				portalExtPath, tomcatPortalExtPath,
				StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private void _runPortalImplTests(Path portalImplPath, Properties properties)
			throws IOException {

		Path portalImplTestPath = Paths.get(
			properties.getProperty("portal.dir"), "portal-impl/test");

		Path backupFilePath = portalImplTestPath.resolve(
			"portal-test-ext.properties.backup");

		Path portalTestExtPath = portalImplTestPath.resolve(
			"portal-test-ext.properties");

		if (Files.exists(portalTestExtPath)) {
			Files.copy(
				portalTestExtPath, backupFilePath,
				StandardCopyOption.REPLACE_EXISTING);
		}

		TaskUtils.createTestExtProperties(portalTestExtPath, _dbType);

		List<String> testTask = new ArrayList<>();

		testTask.add("ant");
		testTask.add("test-class");
		testTask.add("-Dtest.class=" + _testClass);

		boolean debug = Boolean.valueOf(properties.getProperty("debug"));

		if (debug) {
			testTask.add("-Djvm.debug=true");
		}

		ProcessBuilder processBuilder = new ProcessBuilder(testTask);

		processBuilder.directory(portalImplPath.toFile());

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

		if (Files.exists(backupFilePath)) {
			Files.move(
				backupFilePath, portalTestExtPath,
				StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private void _setPath(Path path) {
		_path = path;
	}

	private String _dbType;
	private Path _path;
	private String _testClass;
}
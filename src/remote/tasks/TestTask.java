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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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

	private void _setPath(Path path) {
		_path = path;
	}

	private String _dbType;
	private Path _path;
	private String _testClass;
}
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import remote.util.PropertiesUtil;
import remote.util.TaskUtils;

/**
 * @author tom
 */
public class CreatePropertiesTask extends Task{

	@Override
	public void execute() throws BuildException {
		try {
			Properties properties = PropertiesUtil.loadProperties(
				Paths.get("build.properties"));

			Path portalPath = Paths.get(properties.getProperty("portal.dir"));

			Path portalImplTestPath = portalPath.resolve("portal-impl/test");

			Path portalTestExtBackup = portalImplTestPath.resolve(
				"portal-test-ext.properties.backup");

			Path portalTestExtPath = portalImplTestPath.resolve(
				"portal-test-ext.properties");

			if (Files.exists(portalTestExtPath)) {
				Files.copy(
					portalTestExtPath, portalTestExtBackup,
					StandardCopyOption.REPLACE_EXISTING);
			}

			TaskUtils.createTestExtProperties(portalTestExtPath, _dbType);

			Path portalImplSrcPath = Paths.get(
			properties.getProperty("portal.dir"), "portal-impl/src");

			Path portalExtPath = portalImplSrcPath.resolve(
				"portal-ext.properties");

			Path portalExtBackup = portalImplSrcPath.resolve(
				"portal-ext.properties.backup");

			if (Files.exists(portalExtPath)) {
				Files.copy(
					portalExtPath, portalExtBackup,
					StandardCopyOption.REPLACE_EXISTING);
			}

			TaskUtils.createTestExtProperties(portalExtPath, _dbType);

			Path tomcatPortalExtPath = Paths.get(
				properties.getProperty("tomcat.dir"),
				"webapps/ROOT/WEB-INF/classes/portal-ext.properties");

			Files.copy(
					portalExtPath, tomcatPortalExtPath,
					StandardCopyOption.REPLACE_EXISTING);
		}
		catch (Exception ex) {
			Logger.getLogger(CreatePropertiesTask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void setDBType(String dbType) {
		_dbType = dbType;
	}

	private String _dbType;

}

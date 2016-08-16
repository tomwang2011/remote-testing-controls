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
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class DeployDaoDBTask extends Task {
	@Override
	public void execute() throws BuildException {
		try {
			Properties properties = PropertiesUtil.loadProperties(
				Paths.get("build.properties"));

			Path portalPath = Paths.get(properties.getProperty("portal.dir"));

			Path gradlewPath = portalPath.resolve("gradlew");

			if (_dbType.equals("db2") || _dbType.equals("oracle") ||
				_dbType.equals("sybase")) {

				TaskUtils.runDeployDaoDB(gradlewPath, portalPath);
			}
		} catch (IOException ex) {
			Logger.getLogger(DeployDaoDBTask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void setDBType(String dbType) {
		_dbType = dbType;
	}

	private String _dbType;
}

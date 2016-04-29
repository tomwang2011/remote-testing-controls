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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import remote.util.TaskUtils;

/**
 * @author tom
 */
public class CreateTunnelTask extends Task{

	@Override
	public void execute() throws BuildException {
		try {
			TaskUtils.startTunnel(_dbType);
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

/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.db.upgrade;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.jdbc.core.RowMapper;

import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.dao.UserCommentDao;

/**
 * Add ID to UserComments Table with Primary Keys, XIDs
 * 
 * 
 * @author Terry Packer
 */
public class Upgrade14 extends DBUpgrade{
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.upgrade.DBUpgrade#upgrade()
	 */
	@Override
	protected void upgrade() throws Exception {
		OutputStream os = createUpdateLogOutputStream();
		
	    // Run the scripts to add the ID Column
        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.DERBY.name(), derbyIdColumnScript);
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysqlIdColumnScript);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssqlIdColumnScript);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), h2IdColumnScript);
        runScript(scripts, os);
        
        //Run the scripts to create the XID column but not restrict it to NOT NULL
        scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.DERBY.name(), derbyXidColumnScript);
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysqlXidColumnScript);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssqlXidColumnScript);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), h2XidColumnScript);
        runScript(scripts, os);
        
        //Set XIDs on all the user comments, ugh
        AtomicInteger count = new AtomicInteger();
        UserCommentDao.instance.query("SELECT id FROM userComments", new Object[]{}, new RowMapper<Integer>(){
			@Override
			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getInt(1);
			}
        }, new MappedRowCallback<Integer>(){
			@Override
			public void row(Integer item, int index) {
				ejt.update("UPDATE userComments SET xid=? WHERE id=?", new Object[]{UserCommentDao.instance.generateUniqueXid(), item});
				count.incrementAndGet();
			}
        });
        
        String upgradedString = new String("Updated " + count.get() + " user comments with XIDs.\n");
        os.write(upgradedString.getBytes(Common.UTF8_CS));
        
        //Run the scripts to restrict the XID to NOT NULL
        scripts.put(DatabaseProxy.DatabaseType.DERBY.name(), derbyXidNotNullColumnScript);
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysqlXidNotNullColumnScript);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssqlXidNotNullColumnScript);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), h2XidNotNullColumnScript);
        runScript(scripts, os);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.db.upgrade.DBUpgrade#getNewSchemaVersion()
	 */
	@Override
	protected String getNewSchemaVersion() {
		return "15";
	}
	
	//*
	//* ID Column Scripts
	//* 
	private final String[] mssqlIdColumnScript = {
			"ALTER TABLE userComments ADD COLUMN id INT;", 
			"ALTER TABLE userComments ADD PRIMARY KEY(id);",
			"ALTER TABLE userComments ALTER COLUMN id INT NOT NULL;",
	    };
	private final String[] derbyIdColumnScript = {
	    	"ALTER TABLE userComments ADD COLUMN id INT NOT NULL generated by default as identity (start with 1, increment by 1);",
	    };
	private final String[] mysqlIdColumnScript = {
			"ALTER TABLE userComments ADD id int NOT NULL AUTO_INCREMENT primary key FIRST;"
	    };
	private final String[] h2IdColumnScript = {
			"ALTER TABLE userComments ADD COLUMN id INT NOT NULL auto_increment;",
	    };
	
	//*
	//* XID Column Create Scripts
	//*
	private final String[] mssqlXidColumnScript = {
			"ALTER TABLE userComments ADD COLUMN xid nvarchar(50);", 
			"ALTER TABLE userComments ADD CONSTRAINT userCommentsUn1 UNIQUE (xid);",
	    };
	private final String[] derbyXidColumnScript = {
	    	"ALTER TABLE userComments ADD COLUMN xid varchar(50);",
	    	"ALTER TABLE userComments ADD CONSTRAINT userCommentsUn1 UNIQUE (xid);"
	    };
	private final String[] mysqlXidColumnScript = {
			"ALTER TABLE userComments ADD xid varchar(50);",
			"ALTER TABLE userComments ADD CONSTRAINT userCommentsUn1 UNIQUE (xid);"
	    };
	private final String[] h2XidColumnScript = {
			"ALTER TABLE userComments ADD COLUMN xid varchar(50);",
			"ALTER TABLE userComments ADD CONSTRAINT userCommentsUn1 UNIQUE (xid);"
	    };
	
	//*
	//* XID Column NOT NULL Scripts
	//*
	private final String[] mssqlXidNotNullColumnScript = {
			"ALTER TABLE userComments ALTER COLUMN xid nvarchar(50) NOT NULL;", 
	    };
	private final String[] derbyXidNotNullColumnScript = {
	    	"ALTER TABLE userComments ALTER COLUMN xid varchar(50) NOT NULL;",
	    };
	private final String[] mysqlXidNotNullColumnScript = {
			"ALTER TABLE userComments CHANGE COLUMN xid varchar(50) NOT NULL;",
	    };
	private final String[] h2XidNotNullColumnScript = {
			"ALTER TABLE userComments ALTER COLUMN xid varchar(50) NOT NULL;",
	    };
	
}

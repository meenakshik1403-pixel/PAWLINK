package com.pawlink.common.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton Pattern implementation wrapping the application's {@link DataSource}.
 * Provides centralized, thread-safe access to underlying JDBC connections.
 *
 * Pattern: Classic Double-Checked Locking Singleton.
 */
public class DBConnectionManager {

    private static volatile DBConnectionManager instance;
    private DataSource dataSource;

    // Private constructor to prevent direct instantiation
    private DBConnectionManager() {
    }

    /**
     * Retrieves the global singleton instance.
     * Uses double-checked locking to guarantee thread safety while avoiding
     * synchronization overhead after initialization.
     *
     * @return the singleton instance of DBConnectionManager
     */
    public static DBConnectionManager getInstance() {
        if (instance == null) {
            synchronized (DBConnectionManager.class) {
                if (instance == null) {
                    instance = new DBConnectionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the manager with the Spring-managed DataSource.
     *
     * @param dataSource the configured javax.sql.DataSource
     */
    public synchronized void initialize(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Returns the underlying DataSource.
     *
     * @return the active DataSource
     */
    public DataSource getDataSource() {
        if (this.dataSource == null) {
            throw new IllegalStateException("DBConnectionManager has not been initialized with a DataSource.");
        }
        return this.dataSource;
    }

    /**
     * Obtains a new database connection from the pooled DataSource.
     *
     * @return an active {@link Connection}
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Checks whether the DataSource has been bound.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return this.dataSource != null;
    }
}

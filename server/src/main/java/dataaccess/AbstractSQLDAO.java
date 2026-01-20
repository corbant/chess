package dataaccess;

import java.sql.Connection;

public abstract class AbstractSQLDAO {

    public AbstractSQLDAO() {
        configureTable();
    }

    protected abstract String[] getTableConfig();

    private void configureTable() {
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : getTableConfig()) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error configuring database");
        }
    }
}

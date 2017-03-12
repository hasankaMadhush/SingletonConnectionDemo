/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package singletonconnection;

import java.beans.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Hasanka
 */
public class Database_Connection {

    public Connection connection;
    private PreparedStatement pstmt;
    private Statement stmt;
    private volatile static Database_Connection uniqueDatabaseInstance;

    private Database_Connection() {

        String url = "jdbc:mysql://localhost:3306/";
        String dbName = "demo";    //Database Name
        String driver = "com.mysql.jdbc.Driver";
        String userName = "root";
        String password = "";

        try {
            Class.forName(driver).newInstance();
            this.connection = (Connection) DriverManager.getConnection(url + dbName,
                    userName, password);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException sqle) {

        }
    }

    /**
     *
     * @return Creating Database connection object
     */
    public static synchronized Database_Connection getDBConnection() {
        if (uniqueDatabaseInstance == null) {
            synchronized (Database_Connection.class) {
                if (uniqueDatabaseInstance == null) {
                    uniqueDatabaseInstance = new Database_Connection();
                }
            }
        }
        return uniqueDatabaseInstance;
        
    }

    /**
     *
     * @param query
     * @return
     */
    public PreparedStatement prepareStatement(String query) {
        try {
            pstmt = (PreparedStatement) uniqueDatabaseInstance.connection.prepareStatement(query);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
        }
        return pstmt;
    }

    public Statement createStatement() {
        try {
            stmt = (Statement) uniqueDatabaseInstance.connection.createStatement();
        } catch (SQLException e) {     
        }
        return stmt;
    }
    
    //
    public static boolean runQuery(String query) {
        boolean isSuccess = false;

        PreparedStatement preparedStmt = null;

        try {
            preparedStmt = Database_Connection.getDBConnection().prepareStatement(query);

            preparedStmt.executeUpdate();

            //Connection close after insert
            preparedStmt.close();

            isSuccess = true;
        } catch (Exception err) {
            isSuccess = false;
            System.out.println("Error in Inserting: " + err.getMessage());
        }

        return isSuccess;
    }
    
     public static ResultSet runSearchQuery(String query) {
        ResultSet rs = null;

        PreparedStatement preparedStmt = null;

        try {
            preparedStmt = Database_Connection.getDBConnection().prepareStatement(query);

            rs = preparedStmt.executeQuery();

        } catch (Exception err) {

            System.out.println("Error in Retriveing Data: " + err.getMessage());
        }

        return rs;
    }
    public static boolean runQueries(List<String> queries) throws SQLException {

        boolean isSuccess = false;
        PreparedStatement preparedStmt = null;
        Connection local_Connection;
        
        local_Connection = Database_Connection.getDBConnection().connection;
        

        try {
            local_Connection.setAutoCommit(false);

            for (int i = 0; i < queries.size(); i++) {
                preparedStmt = Database_Connection.getDBConnection().prepareStatement(queries.get(i));
                preparedStmt.executeUpdate();
                //Connection close after insert
                preparedStmt.close();
            }

            local_Connection.commit();

            isSuccess = true;
        } catch (Exception e) {
            local_Connection.rollback();
        } finally {
            local_Connection.setAutoCommit(true);
            preparedStmt.close();
        }

        return isSuccess;
    }

   

}

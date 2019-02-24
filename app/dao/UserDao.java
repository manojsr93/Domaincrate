package dao;

import javax.inject.Singleton;

import org.mindrot.jbcrypt.BCrypt;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Domain;
import models.TestEbean;
import models.User;

import java.sql.*;
import play.db.*;
import util.DbConnection;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class UserDao {
	
	public String passEncrypt(String password)
	{
		String encryptedPassword= BCrypt.hashpw(password, BCrypt.gensalt());
		return encryptedPassword;
		
	}
	
	public boolean checkPass(String plain_password, String hashed_password)
	{
		 if(BCrypt.checkpw(plain_password, hashed_password))
			 return true;
		 else
			 return false;
	}
	
	public List<User>findAll() throws SQLException{
		DbConnection dbconnection= DbConnection.getConnection();
		List<User> userList= new ArrayList<>();
		PreparedStatement preparedStatement = dbconnection.conn.prepareStatement("select * from user_master_table;");
		preparedStatement.executeQuery();
		ResultSet rs= preparedStatement.getResultSet();
		while(rs.next())
		{
			User user= new User();
			user.setPrimary_email(rs.getString("primary_email"));
			user.setName(rs.getString("name"));
			user.setActive(rs.getBoolean("active"));
			user.setVerified(rs.getBoolean("verified"));
			userList.add(user);
		}
		return userList;
	}
	
//	public User findOne(String primary_email) throws SQLException {
//		DbConnection dbconnection= DbConnection.getConnection();
//		User user= new User();
//		String query= "select * from user_master_table where primary_email=?;";
//		PreparedStatement preparedStatement = dbconnection.conn.prepareStatement(query);
//		preparedStatement.setString(1, primary_email.toString());
//		ResultSet rs= preparedStatement.executeQuery();
//		while(rs.next()) {
//			user.setUser_id(rs.getString("user_id"));
//			user.setPrimary_email(rs.getString("primary_email"));
//			user.setName(rs.getString("name"));
//			user.setActive(rs.getBoolean("active"));
//			user.setVerified(rs.getBoolean("verified"));
//		}
//		return user;
//	}
	
	public void bulkDomainInsert(ArrayList<Domain>domainList) throws SQLException
	{
		DbConnection dbconnection= DbConnection.getConnection();
		dbconnection.conn.setAutoCommit(false);
		String query= "insert into domain_table(user_id,registered_email,domain_name) values (?,?,?)";
		PreparedStatement preparedStatement = dbconnection.conn.prepareStatement(query);
		for(int i= 0;i<domainList.size()-1;i++)
		{
			preparedStatement.setInt(1, 1);
			preparedStatement.setString(2, domainList.get(i).getRegistered_email());
			preparedStatement.setString(3, domainList.get(i).getDomain_name());
			preparedStatement.addBatch();
		}
		preparedStatement.executeBatch();
	}
	
	public void automaticInsert(ArrayList<String> nameList) throws SQLException
	{
		EbeanServer server = Ebean.getDefaultServer();
		
		for(int i= 0;i<nameList.size();i++)
		{
			TestEbean testbean= new TestEbean();
			testbean.name=nameList.get(i);
			System.out.println(testbean.name);
			server.save(testbean);
		}
	}
	
	public User create_user(User new_user) throws SQLException
	{
		new_user.setPassword(passEncrypt(new_user.getPassword()));
		System.out.println("after encryption of password");
		EbeanServer server = Ebean.getDefaultServer();
		server.save(new_user);
		return new_user;
	}
	
	public User fetchUserByEmail(String email)
	{
		EbeanServer server = Ebean.getDefaultServer();
		User user= server.find(User.class).where().eq("primary_email", email).findOne();
		return user;
	}
	
	public User fetchUserById(int id)
	{
		EbeanServer server = Ebean.getDefaultServer();
		User user= server.find(User.class).where().eq("user_id", id).findOne();
		return user;
	}
	
	public User validateCredentials(String email, String password)
	{
		EbeanServer server = Ebean.getDefaultServer();
		User user= server.find(User.class).where().eq("primary_email", email).findOne();
		if(checkPass(password, user.getPassword()))
		{
			return user;
		}
		else
			return null;
	}
}

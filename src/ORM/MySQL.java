package ORM;

import ORM.Exceptions.Unchecked.DBConexionException;
import ORM.Exceptions.Unchecked.DBConfigInexistenteException;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class MySQL{

	private Connection   conn = null;
	private String 	     nombre_db;
	final private String db_properties_path;

	MySQL(){
		this("db.properties");
	}

	public MySQL(String db_properties_path){
		this.db_properties_path = db_properties_path;

		final var shutdown_hook = new Thread(
			this::__closeConn,
			"mysql_close_connection_hook"
		);

		Runtime.getRuntime().addShutdownHook(shutdown_hook);

	}

	private void conectar(){
		var config_file = DB.class.getResourceAsStream(db_properties_path);
		if(config_file == null) throw new DBConfigInexistenteException();

		var db_config = new Properties();

		try{
			db_config.load(config_file);
		}catch(IOException e){
			throw new DBConfigInexistenteException();
		}

		var host = db_config.getProperty("host");
		var port = db_config.getProperty("port");
		var db   = db_config.getProperty("db");
		var usr  = db_config.getProperty("usr");
		var pwd  = db_config.getProperty("pwd");

		try{
			final var url_template = "jdbc:mysql://%s:%s/%s";
			conn = DriverManager.getConnection(
				url_template.formatted(host, port, db), usr, pwd
			);
		}catch(SQLException e){
			throw new DBConexionException(e);
		}

		nombre_db = db;
	}

	private Connection conn() throws SQLException{

		//sigue viva la conexion?
		if(conn != null){
			if(conn.isValid(1)) return conn;
			conn.close();
		}

		//reconstruyemoslas
		conectar();
		return conn;
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException{
		return conn().prepareStatement(sql);
	}

	public PreparedStatement preparedStatementWithGeneratedKeys(String sql) throws SQLException{
		return conn().prepareStatement(
			sql,
			Statement.RETURN_GENERATED_KEYS //necesario para poder hidratar la PK
		);
	}

	public Statement createStatement() throws SQLException{
		return conn().createStatement();
	}

	public String getCatalog() throws SQLException{
		return conn().getCatalog();
	}

	public DatabaseMetaData getMetaData() throws SQLException{
		return conn().getMetaData();
	}

	public String nombreDB(){
		return nombre_db;
	}


	/**
	 *  No hace falta llamar a esto manualmente, lo llama el ORM
	 *  */
	public void __closeConn(){
		try{
			conn.close();
		}catch(SQLException _){} //que me importa que tires error tomatela te dije
	}


}

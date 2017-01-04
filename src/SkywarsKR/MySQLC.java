package SkywarsKR;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class MySQLC {
	public static MySQLC instancia;
	public static String status = "desativado";

	private MySQLC() {
	}

	Connection connection = null;

	public static MySQLC getInstancia() {
		if (instancia == null) {
			instancia = new MySQLC();
		}
		return instancia;
	}

	public void startMySQL() {
		connection = startConnectionMySQL();
		DatabaseMetaData metadados;
		ResultSet tabela;
		try {
			metadados = connection.getMetaData();
			tabela = metadados.getTables(null, null, "players", null);
			Statement st = connection.createStatement();
			if (tabela.next()) {
				return;
			}

			st.executeUpdate(
					"CREATE TABLE players (`ID` int(11) NOT NULL AUTO_INCREMENT,`Name` varchar(50) NOT NULL,`Points` int(11) NOT NULL, `Kills` int(11) NOT NULL, `Wins` int(11) NOT NULL, PRIMARY KEY (`ID`))");
		} catch (SQLException e) {

		}
	}

	// Pegar conexão//
	public Connection getConnectionMySQL() {
		return connection;
	}

	// Método de Conexão//
	public Connection startConnectionMySQL() {
		try {

			// Carregando o JDBC Driver padrão

			String driverName = "com.mysql.jdbc.Driver";

			Class.forName(driverName);

			// Configurando a nossa conexão com um banco de dados//

			String serverName = Main.pl.getConfig().getString("MySQL.ServerName"); // caminho
																					// do
																					// servidor
																					// do
																					// BD

			String mydatabase = Main.pl.getConfig().getString("MySQL.Database"); // nome
																					// do
																					// seu
																					// banco
																					// de
																					// dados

			String url = "jdbc:mysql://" + serverName + "/" + mydatabase;

			String username = Main.pl.getConfig().getString("MySQL.Username"); // nome
																				// de
																				// um
																				// usuário
																				// de
																				// seu
																				// BD

			String password = Main.pl.getConfig().getString("MySQL.Password"); // sua
																				// senha
																				// de
																				// acesso
			connection = DriverManager.getConnection(url, username, password);

			// Testa sua conexão//

			if (connection != null) {
				status = ("Conectado com sucesso!");

			} else {
				status = ("Não foi possivel realizar conexão.");
			}
			return connection;
		} catch (ClassNotFoundException e) {
			System.out.println("O driver expecificado nao foi encontrado.");
			return null;

		} catch (SQLException e) {
			System.out.println("Nao foi possivel conectar ao Banco de Dados.");
			return null;
		}
	}

	public HashMap<String, String> getStatusHashMap(String query) {
		HashMap<String, String> objects = new HashMap<String, String>();

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			ResultSet result = preparedStatement.executeQuery();
			while (result.next()) {
				String Name = result.getString("Name");
				String Status = result.getString("Points") + ":" + result.getString("Kills") + ":"
						+ result.getString("Wins");
				objects.put(Name, Status);
			}
			return objects;
		} catch (SQLException e) {

		}

		return new HashMap<String, String>();
	}

	public boolean containsMySQL(String Colum, String like) {
		try {
			String sql = "SELECT * FROM players WHERE ? = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, Colum);
			preparedStatement.setString(2, like);
			ResultSet result = preparedStatement.executeQuery();

			if (result.next()) {
				return true;
			}
		} catch (SQLException e) {
			return false;
		}
		return false;
	}

	public void executeUpdateMySQL(String query) {
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// Método que retorna o status da sua conexão//
	public String statusConnection() {
		return status;
	}

	// Método que fecha sua conexão//
	public boolean closeConnection() {
		try {
			getConnectionMySQL().close();
			return true;

		} catch (SQLException e) {
			return false;

		}
	}

	// Método que reinicia sua conexão//
	public Connection restartConnection() {
		closeConnection();
		return getConnectionMySQL();
	}
}
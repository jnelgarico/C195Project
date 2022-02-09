package dbaccess;

import dbconnection.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import model.*;
import java.sql.*;


public class DBAccess {

    /**
     * Validates login credentials given by the user.
     *
     * @param username the username provided
     * @param password the password provided
     * @return true if login credentials are valid, false if login credentials are not valid
     */
    public static boolean validateLogin(String username,String password) {
        boolean isLoginValid = true;//FIX ME
        try{
            String query = "SELECT * from users";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next() && !isLoginValid) {
                String myUser = rs.getString("User_Name");
                String myPassword = rs.getString("Password");
                isLoginValid = username.equals(myUser) && password.equals(myPassword);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return isLoginValid;
    }

    public static ObservableList<Customer> addDBCustomers() {
        ObservableList<Customer> list = FXCollections.observableArrayList();
        try {
            String query = "SELECT Customer_ID,Customer_Name,Address,Postal_Code,Phone,Division_ID FROM customers";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int customerID = rs.getInt("Customer_ID");
                String customerName = rs.getString("Customer_Name");
                String address = rs.getString("Address");
                String postalCode = rs.getString("Postal_Code");
                String phone = rs.getString("Phone");
                int divisionID = rs.getInt("Division_ID");
                //Create new Customer
                Customer customer = new Customer(customerID,customerName,address,postalCode,phone,divisionID);
                //Add customer to list
                list.add(customer);
            }

        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static int deleteCustomer(int customerID) {
        int deletedAppointments = 0;
        try {
            String querySumAppointments= "SELECT COUNT(Appointment_ID) from appointments where Customer_ID="
                    + customerID +";";
            String queryAppointments = "DELETE FROM appointments WHERE Customer_ID=" + customerID + ";";
            String queryCustomers = "DELETE FROM customers WHERE Customer_ID=" + customerID + ";";
            PreparedStatement psSumAppointments = DBConnection.getConnection().prepareStatement(querySumAppointments);
            PreparedStatement psAppointments = DBConnection.getConnection().prepareStatement(queryAppointments);
            PreparedStatement psCustomers = DBConnection.getConnection().prepareStatement(queryCustomers);
            ResultSet rs = psSumAppointments.executeQuery();
            rs.next();
            deletedAppointments = rs.getInt(1);
            psAppointments.execute();
            psCustomers.execute();
        } catch (SQLException e) { e.printStackTrace();}
            return deletedAppointments;
    }

    public static void addCustomer(Customer customer) {
        String name = customer.getCustomerName();
        String address = customer.getAddress();
        String phone = customer.getPhoneNumber();
        String postalCode = customer.getPostalCode();
        int divisionId = customer.getDivisionId();
        try {
            String query = "INSERT INTO customers VALUES(NULL, ?, ?, ?, ?, NOW(), 'script', NOW(), 'script', ?)";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,name);
            ps.setString(2,address);
            ps.setString(3,postalCode);
            ps.setString(4,phone);
            ps.setInt(5,divisionId);
            ps.execute();
        } catch (SQLException e) { e.printStackTrace(); }
    }




    public static ObservableList<Country> getCountries() {
        ObservableList<Country> list = FXCollections.observableArrayList();
        try {
            String query = "SELECT Country_ID,Country FROM countries;";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int countryId = rs.getInt("Country_ID");
                String countryName = rs.getString("Country");
                Country country = new Country(countryId,countryName);
                list.add(country);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static ObservableList<FirstLevelDivision> getFirstLevelDivision(int countryId) {
        ObservableList<FirstLevelDivision> list = FXCollections.observableArrayList();
        try {
            String query = "SELECT Division_ID,Division FROM first_level_divisions WHERE Country_ID ="+countryId+";";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int divisionId = rs.getInt("Division_ID");
                String name = rs.getString("Division");
                FirstLevelDivision division = new FirstLevelDivision(divisionId, name, countryId);
                list.add(division);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static FirstLevelDivision getFirstLevelDivisionByID(int divisionId) {
        FirstLevelDivision division = new FirstLevelDivision();
        try {
            String query = "SELECT Division,Country_ID FROM first_level_divisions WHERE Division_ID="+divisionId+";";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            rs.next();
            String name = rs.getString("Division");
            int countryId = rs.getInt("Country_ID");

            division = new FirstLevelDivision(divisionId,name,countryId);
        } catch (SQLException e) { e.printStackTrace(); }
        return division;
    }

    public static Country getCountryByID(int countryID) {
        Country country = new Country();
        try {
            String query = "SELECT Country,Country_ID FROM countries WHERE Country_ID="+countryID+";";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int countryId = rs.getInt("Country_ID");
            String name = rs.getString("Country");

            country = new Country(countryId,name);
        } catch (SQLException e) { e.printStackTrace(); }
        return country;
    }

    public static void editCustomer(Customer customer) {
        int id = customer.getCustomerId();
        String query = "UPDATE customers SET Customer_Name=?, Address=?,Postal_Code=?,Phone=?,Last_Update=NOW(),"
                +"Last_Updated_By='user',Division_ID=? WHERE Customer_ID="+id+";";
        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(query);

            ps.setString(1,customer.getCustomerName());
            ps.setString(2,customer.getAddress());
            ps.setString(3, customer.getPostalCode());
            ps.setString(4, customer.getPhoneNumber());
            ps.setInt(5,customer.getDivisionId());

            Boolean updateSuccess = ps.execute();
            if (updateSuccess) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Update successful.");
                alert.setContentText("Customer with ID number " + id + " has been updated.");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Given a contactId, returns a Contact with information from associated contact from the database.
     *
     * @param contactId contactId to find
     * @return Contact with associated contactId
     */
    public static Contact getContactfromId(int contactId) {
        String query = "SELECT * from contacts WHERE Contact_ID=" + contactId + ";";
        Contact contact = new Contact();
        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String name = rs.getString("Contact_Name");
            String email = rs.getString("Email");
            contact = new Contact(contactId, name, email);
        } catch(SQLException e) { e.printStackTrace(); }
        return contact;
    }

    public static ObservableList<Contact> getAllContacts() {
        ObservableList<Contact> list = FXCollections.observableArrayList();
        String query = "SELECT * from contacts;";
        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int contactId = rs.getInt("Contact_ID");
                String name = rs.getString("Contact_Name");
                String email = rs.getString("Email");
                Contact contact = new Contact(contactId, name, email);
                list.add(contact);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

}
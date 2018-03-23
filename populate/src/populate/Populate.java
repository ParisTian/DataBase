/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package populate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author yuwentia
 */
public class Populate {
    static final  String URL = "jdbc:oracle:thin:@" + "localhost" + ":" + "1521" + ":" + "ParisDB";
    static final  String USERNAME = "system";
    static final  String PASSWORD = "1234";
    static final  int MAX_RECORD = Integer.MAX_VALUE;
    static final  int verbose = 1;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
        final int N = args.length;
        String[] files_name = new String[N];
        Connection connect = connectionToDB();
        for(int i = N-1; i < N; i++){
            files_name[i] = args[i];
            switch(files_name[i]) {
                case "yelp_business.json" :
                    if((verbose & 1) == 1) {
                        System.out.println("create TABLE for business. ");
                    }
                    createTable(connect, "BUSINESS");
                    createTable(connect, "BCATEGORY");
                    break;
                case "yelp_review.json" :
                    if((verbose & 1) == 1) {
                        System.out.println("create TABLE for review. ");
                    }
                    createTable(connect, "REVIEW");
                    break;
                case "yelp_checkin.json" :
                    if((verbose & 1) == 1) {
                        System.out.println("create TABLE for checkin. ");
                    }
                    createTable(connect, "CHECKIN");
                    break;
                case "yelp_user.json" :
                    if((verbose & 1) == 1) {
                        System.out.println("create TABLE for user. ");
                    }
                    createTable(connect, "YUSER");
                    break;
                default:
                    System.out.println("ERROR: unsurpport file.");
            }
            if((verbose & 1) == 1) {
                System.out.println("INSERT tuples: ");
            }
            populateFile(connect, files_name[i]);
            //System.out.println(files_name[i]);
        }
        closeConnection(connect);
    }
    
    private static void createTable(Connection connect, String table_name){
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();        
        try {
            DatabaseMetaData db_meta = connect.getMetaData();
            Statement statement = connect.createStatement();
            ResultSet find_table = db_meta.getTables(null, null, table_name, null);
            String sql_str = new String();
            if(find_table.next() == true) {
                sql_str = "DROP TABLE " + table_name;
                System.out.println("SQL command: \n\t" + sql_str);
                statement.executeUpdate(sql_str);
                if ((verbose & 1) == 1) {
                    System.out.println("    Table exist. Drop table: "+table_name);
                }
            }
            sql_str = "CREATE TABLE " + table_name + " ( \n\t" + getSchema(table_name) + ")";
            // sql_str = "CREATE TABLE A (type VARCHAR2(20))";
            if ((verbose & 1) == 1) {
                System.out.println("SQL command: \n" + sql_str);
            }
            statement.execute(sql_str);
            if ((verbose & 2) == 2) {
                System.out.println("Table: "+table_name+" was created!!!");
                ResultSet result = statement.executeQuery("SELECT * FROM " + table_name);
                ResultSetMetaData tb_meta = result.getMetaData();
                for( int col = 1; col <= tb_meta.getColumnCount(); col++) {
                    System.out.println("Col " + col + ": " + tb_meta.getCatalogName(col) + '\t'
                                        + tb_meta.getColumnTypeName(col));
                }
            }
        } catch (SQLException ex) {
            System.out.println("ERROR from func: - " + func_name);
        }
    }
    private static String getSchema(String table_name) {
        String col_list = new String();
        int i = 0;
        switch (table_name) {
            case "BUSINESS" :
                for(i = 0; i < BUS_COLS.length-1; i++) {
                    col_list = col_list + BUS_COLS[i].name + " " + BUS_COLS[i].type + ", \n\t" ;
                }
                col_list = col_list + BUS_COLS[i].name + " " + BUS_COLS[i].type;
                return  col_list;
            case "YUSER"     :
                col_list = new String();
                i = 0;
                for(i = 0; i < USER_COLS.length-1; i++) {
                    col_list = col_list + USER_COLS[i].name + " " + USER_COLS[i].type + ", \n\t" ;
                }
                col_list = col_list + USER_COLS[i].name + " " + USER_COLS[i].type;
                return  col_list;
            case "REVIEW"   :
                col_list = new String();
                i = 0;
                for(i = 0; i < REVIEW_COLS.length-1; i++) {
                    col_list = col_list + REVIEW_COLS[i].name + " " + REVIEW_COLS[i].type + ", \n\t" ;
                }
                col_list = col_list + REVIEW_COLS[i].name + " " + REVIEW_COLS[i].type;
                return  col_list;
            case "CHECKIN"  :
                col_list = new String();
                i = 0;
                for(i = 0; i < CHECKIN_COLS.length-1; i++) {
                    col_list = col_list + CHECKIN_COLS[i].name + " " + CHECKIN_COLS[i].type + ", \n\t" ;
                }
                col_list = col_list + CHECKIN_COLS[i].name + " " + CHECKIN_COLS[i].type;
                return  col_list;
            case "BCATEGORY" :
                col_list = new String();
                i = 0;
                for(i = 0; i < CAT_COLS.length-1; i++) {
                    col_list = col_list + CAT_COLS[i].name + " " + CAT_COLS[i].type + ", \n\t" ;
                }
                col_list = col_list + CAT_COLS[i].name + " " + CAT_COLS[i].type;
                return  col_list;
                
            default:
                System.out.println("ERROR: Unsupported table_name");
                return null;
        }
    }
    private static Connection connectionToDB(){
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        System.out.println("CONNECT to Oracle Database:");
        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            Connection c = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            return c;
        } catch (SQLException ex) {
           System.out.println("Exception from func: - " + func_name);
           return null;
        }
    }
    private static void closeConnection(Connection connect){
        System.out.println("CLOSE connection with Oracle Database:");
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        try {
            connect.close();
        } catch (SQLException ex) {
            System.out.println("ERROR from func: - " + func_name);
        }
    }
    private static void populateFile(Connection connect, String file_name){
        BufferedReader file_br; 
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        try{
            file_br = new BufferedReader(new FileReader(file_name));
            switch(file_name) {
                case "yelp_business.json" :
                    populateHelper(connect, file_br, "BUSINESS", BUS_COLS);
                    file_br = new BufferedReader(new FileReader(file_name));
                    populateCategory(connect, file_br, "BCATEGORY", CAT_COLS);
                    break;
                case "yelp_review.json" :
                    populateHelper(connect, file_br, "REVIEW", REVIEW_COLS);
                    break;
                case "yelp_checkin.json" :
                    populateHelper(connect, file_br, "CHECKIN", CHECKIN_COLS);
                    break;
                case "yelp_user.json" :
                    populateHelper(connect, file_br, "YUSER", USER_COLS);
                    break;
                default:
                    System.out.println("ERROR from " + func_name + ": unsurpport file.");
            }

            file_br.close();
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR from func: - " + func_name + " File not found: " + file_name);
        } catch (IOException ex) {
            System.out.println("ERROR from func: - " + func_name + " File close error: " + file_name);
        }
    }
    private static class colNode{
        String name;
        String type;
        colNode(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }
    final static colNode[] CAT_COLS = new colNode[]{    new colNode("business_id", "VARCHAR2(22)"),
                                                        new colNode("main_cat", "VARCHAR2(150)"),
                                                        new colNode("sub_cat", "VARCHAR2(150)")};
    
    final static colNode[] BUS_COLS = new colNode[]{    new colNode("business_id", "VARCHAR2(22) NOT NULL PRIMARY KEY "),
                                                        new colNode("full_address", "VARCHAR2(150)"),
                                                        //new colNode("hours", "VARCHAR2"),
                                                        //new colNode("open", "VARCHAR2"),
                                                        //new colNode("categories", "VARCHAR2"),
                                                        new colNode("city", "VARCHAR2(30)"),
                                                        new colNode("state", "VARCHAR2(20)"),
                                                        new colNode("name", "VARCHAR2(70)"),
                                                        //new colNode("neighborhoods", "VARCHAR2"),
                                                        //new colNode("attributes", "VARCHAR2"),
                                                        new colNode("type", "VARCHAR2(20)"),
                                                        new colNode("stars", "FLOAT"),
                                                        new colNode("review_count", "NUMBER")};

    final static colNode[] REVIEW_COLS = new colNode[]{
                                                        new colNode("votes", "NUMBER"),
                                                        new colNode("user_id", "VARCHAR2(22)"),
                                                        new colNode("review_id", "VARCHAR2(22) NOT NULL PRIMARY KEY"),
                                                        new colNode("stars", "NUMBER"),
                                                        new colNode("r_date", "DATE"),
                                                        new colNode("text", "VARCHAR2(1000)"),
                                                        new colNode("type", "VARCHAR2(20)"),
                                                        new colNode("business_id", "VARCHAR2(22)")};

    final static colNode[] USER_COLS = new colNode[]{
                                                        new colNode("yelping_since", "DATE"),
                                                        // new colNode("votes", "NUMBER"),
                                                        new colNode("review_count", "NUMBER"),
                                                        new colNode("name", "VARCHAR2(60)"),
                                                        new colNode("user_id", "VARCHAR2(22) NOT NULL PRIMARY KEY"),
                                                        new colNode("friends", "NUMBER"),
                                                        new colNode("fans", "NUMBER"),
                                                        new colNode("average_stars", "FLOAT"),
                                                        new colNode("type", "VARCHAR2(20)"),
                                                        // new colNode("compliments", "VARCHAR2(400)") object
                                                        //new colNode("elite", "") array
                                                        };
    final static colNode[] CHECKIN_COLS = new colNode[]{
                                                        //new colNode("day", "NUMBER"),
                                                        new colNode("hour", "NUMBER"),
                                                        new colNode("checkin_count", "NUMBER"),        
                                                        new colNode("type", "VARCHAR2(20)"),
                                                        new colNode("business_id", "VARCHAR2(22)")
                                                        };
  
    final static String[] MCAT_ARRAY = new String[]{"Active Life", "Arts & Entertainment", "Automotive",
                                                    "Car Rental",  "Cafes",                "Beauty & Spas",
                                                    "Convenience Stores", "Dentists", "Doctors",
                                                    "Drugstores", "Department Stores", "Education",
                                                    "Event Planning & Services", "Flowers & Gifts", "Food",
                                                    "Health & Medical", "Home Services", "Home & Garden",
                                                    "Hospitals", "Hotels & Travel", "Hardware Stores",
                                                    "Grocery", "Medical Centers", "Nurseries & Gardening",
                                                    "Nightlife", "Restaurants", "Shopping",
                                                    "Transportation"};
    private static void populateHelper(Connection connect, BufferedReader file_br, String table_name, colNode[] col_info){
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        int count = 0;
        String line = null;
        JSONParser json_p = new JSONParser();
        String sql_cmd = "INSERT INTO " + table_name  + " VALUES (";
        for(int i = 0; i < col_info.length; i ++) {
            sql_cmd = sql_cmd + "?,";
        }
        sql_cmd = sql_cmd.substring(0, sql_cmd.length()-1) + ")";
        System.out.println(sql_cmd);
        JSONObject json_o = new JSONObject();
        try {
            PreparedStatement prepare_s = connect.prepareStatement(sql_cmd);
            while((line = file_br.readLine()) != null && count < MAX_RECORD) {
                //System.out.println(count + " " + line);
                count ++;
                if(count % 1000 == 0) {
                    System.out.println("Processing up to tuple " + count);
                }
                json_o = (JSONObject)json_p.parse(line); 
                if( table_name.equals("CHECKIN")) {
                    populateCheckInInfo(line, json_o, prepare_s);
                    continue;
                }
                for(int i = 0; i < col_info.length; i++) {
                    //System.out.println(col_info[i].name);
                    if(table_name.equals("REVIEW") && col_info[i].name.equals("votes")) {
                        int col_value = 0;
                        JSONObject vote_info = (JSONObject) json_o.get("votes");
                        for(Iterator iterator = vote_info.keySet().iterator(); iterator.hasNext(); ) {
                            String key = (String) iterator.next();
                            long vote_cnt = (long) vote_info.get(key);
                            col_value += (int)vote_cnt;
                        }
                        prepare_s.setInt(i+1, (int)col_value);
                        continue;
                    }
                    if(col_info[i].type.substring(0, 4).equals("VARC")) {
                        String col_value = (String)json_o.get(col_info[i].name);
                        //System.out.println(col_value);
                        if(col_value.length() > 250) {
                            col_value = col_value.substring(0, 250);
                        }
                        prepare_s.setString(i+1, col_value);
                    } else if(col_info[i].type.equals("NUMBER")) {
                        //System.out.println();
                        if(col_info[i].name.equals("friends")) {
                            JSONArray friends_name = (JSONArray) json_o.get("friends");
                            int num_friends = friends_name.size();
                            prepare_s.setInt(i+1, (int)num_friends);                            
                        } else {
                            long col_value = (Long)json_o.get(col_info[i].name);
                            prepare_s.setInt(i+1, (int)col_value);   
                        }
                    } else if(col_info[i].type.equals("FLOAT")){
                        double col_value = (Double)json_o.get(col_info[i].name);
                        prepare_s.setFloat(i+1, (float)col_value);
                    } else if(col_info[i].type.equals("DATE")) {
                        String col_value = new String();
                        if(table_name.equals("REVIEW")) {
                            col_value = (String)json_o.get("date");
                        } else if( table_name.equals("YUSER")){
                            col_value = (String)json_o.get(col_info[i].name);
                            col_value = col_value + "-01";
                        }
                        prepare_s.setDate(i+1, Date.valueOf(col_value));
                    }
                }
                //System.out.println("before--" );
                prepare_s.executeUpdate();
                //System.out.println("after--");           
            }
        } catch (IOException ex) {
            System.out.println("ERROR from func: - " + func_name + " Read file error.");
        } catch (SQLException ex) {
            System.out.println("ERROR from func: - " + func_name + " SQL error.");
        } catch (ParseException ex) {
            System.out.println("ERROR from func: - " + func_name + " Parsing JSON error.");
        }
    }
    private static void populateCheckInInfo(String line, JSONObject json_o, PreparedStatement prepare_s) {
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        JSONObject checin_info = (JSONObject) json_o.get("checkin_info");
        String     type        = (String)json_o.get("type");
        String     business_id = (String)json_o.get("business_id");
        for(Iterator iterator = checin_info.keySet().iterator(); iterator.hasNext(); ) {
            int i = 1;
            String key = (String) iterator.next();
            long checkin_cnt = (long) checin_info.get(key);
            String[] hour_day = key.split("-");
            int hour = Integer.valueOf(hour_day[1]) * 24 + Integer.valueOf(hour_day[0]);
            try {
                prepare_s.setInt(i++, (int)hour);
                prepare_s.setInt(i++, (int)checkin_cnt);
                prepare_s.setString(i++, type);
                prepare_s.setString(i++, business_id);
                prepare_s.executeUpdate();
            } catch (SQLException ex) {
                System.out.println("ERROR from func: - " + func_name + " SQL error.");
            }
        }        
    }
    
    private static void populateCategory(Connection connect, BufferedReader file_br, String table_name, colNode[] col_info){
        String func_name = new Object(){}.getClass().getEnclosingMethod().getName();
        int count = 0;
        HashSet<String> category_set = new HashSet<>();
        for(int i = 0; i < MCAT_ARRAY.length; i++){
            category_set.add(MCAT_ARRAY[i]);
        }
        String line = null;
        JSONParser json_p = new JSONParser();
        String sql_cmd = "INSERT INTO " + table_name  + " VALUES (";
        for(int i = 0; i < col_info.length; i ++) {
            sql_cmd = sql_cmd + "?,";
        }
        sql_cmd = sql_cmd.substring(0, sql_cmd.length()-1) + ")";
        System.out.println(sql_cmd);
        JSONObject json_o = new JSONObject();
        try {
            PreparedStatement prepare_s = connect.prepareStatement(sql_cmd);

            while((line = file_br.readLine()) != null && count < MAX_RECORD) {
                //System.out.println(line);
                count ++;
                if(count % 1000 == 0) {
                    System.out.println("Processing up to tuple " + count);
                }
                json_o = (JSONObject)json_p.parse(line);      
                // business_id
                String col_value = (String)json_o.get(col_info[0].name);
                prepare_s.setString(1, col_value);
                
                // categories: main, sub
                JSONArray col_array = (JSONArray)json_o.get("categories");
                String main_cat = new String("#");
                String sub_cat  = new String("#");
                for(int i = 0; i < col_array.size(); i++) {
                    if(category_set.contains((String)col_array.get(i))) {
                        main_cat = main_cat + (String)col_array.get(i) + "#";
                    } else {
                        sub_cat = sub_cat + (String)col_array.get(i) + "#";
                    }
                }
                if(main_cat.equals("#")) {
                    System.out.println("NO MAIN category: " + line);
                }
                prepare_s.setString(2, main_cat);
                prepare_s.setString(3, sub_cat);

                //System.out.println("before--" );
                prepare_s.executeUpdate();
                //System.out.println("after--");           
            }
        } catch (IOException ex) {
            System.out.println("ERROR from func: - " + func_name + " Read file error.");
        } catch (SQLException ex) {
            System.out.println("ERROR from func: - " + func_name + " SQL error.");
        } catch (ParseException ex) {
            System.out.println("ERROR from func: - " + func_name + " Parsing JSON error.");
        }
    }
}

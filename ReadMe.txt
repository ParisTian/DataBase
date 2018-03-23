populate: 
----------------
Download the Yelp dataset from web site. Look at each JSON file and understand what information the
JSON objects provide. Pay attention to the data items in JSON objects that you will need for your
application (For example, categories, attributes,…etc.)
Populate my database with the Yelp data. Generate INSERT statements for your tables and run those to
insert data into your DB.

yelp_database
-----------------
Implement the application for searching local businesses. 
- Write the SQL queries to search your database.
- Establish connectivity with the DBMS.
- Embed/execute queries in/from the code. Retrieve query results and parse the 
	returned results to generate the
output that will be displayed on the GUI.
- Implement a GUI where the user can,
		o Search for either a business or users that match the criteria given. 
		o Browse through main and sub-categories for the businesses; 
		o Search for the businesses that belong to the main and sub-categories 
			that user specifies along with checkin/review information
		o Search for users with attributes. (The application should be able to search for the
			users that have either all the specified attributes (AND condition) or that 
			have any of the attributesspecified (OR condition))
		o Select a certain business in the search results and list all the reviews for
			that business.
		o Select a user in the search results and list all the reviews for that user.


yelp_dataset:
----------------
The original JSON files which will be populated into my ORACLE Database. The JSON dataset
is over 800M. It's not suitable to put the whole dataset on GitHub. I will only put a small
section of the dataset as a reference.
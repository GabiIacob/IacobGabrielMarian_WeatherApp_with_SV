import sqlite3
import os

from sqlite3 import Error

class DatabaseManager:
    def __init__(self, db_file):
        self.db_file = db_file
        self.connection = None
        print(f"Crearea bazei de date la: {os.path.abspath(self.db_file)}")  
        self.connect()  
        self.create_users_table() 

    def connect(self):
        try:
            self.connection = sqlite3.connect(self.db_file)
            print(f"Connected to database: {self.db_file}")
        except Error as e:
            print(f"Failed connection to database: {e}")

    def create_users_table(self):
        create_table_query = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT NOT NULL,
                password TEXT NOT NULL,
                city TEXT NOT NULL
            )
        """
        try:
            cursor = self.connection.cursor()
            cursor.execute(create_table_query)
            print("Table 'users' ensured to exist.")
        except Error as e:
            print(f"Error creating table: {e}")

    def add_user(self, email, password, city):
        insert_query = """
            INSERT INTO users (email, password, city)
            VALUES (?, ?, ?)
        """
        try:
            cursor = self.connection.cursor()
            cursor.execute(insert_query, (email, password, city))
            self.connection.commit()
            print(f"User '{email}' added successfully.")
        except Error as e:
            print(f"Error adding user: {e}")

    def fetch_users(self):
        fetch_query = "SELECT * FROM users"
        try:
            cursor = self.connection.cursor()
            cursor.execute(fetch_query)
            return cursor.fetchall()
        except Error as e:
            print(f"Error fetching users: {e}")
            return []

    def close(self):
        if self.connection:
            self.connection.close()
            print("Database connection closed.")

# utilizare
if __name__ == "__main__":
    db_manager = DatabaseManager("weather_app.db")

    users = db_manager.fetch_users()
    for user in users:
        print(user)

    db_manager.close()

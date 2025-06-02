# database/connector.py
import mysql.connector
from pymongo import MongoClient
import os
from dotenv import load_dotenv

load_dotenv() 
class DatabaseConnector:
    """Database connector cho MySQL và MongoDB"""
    def __init__(self):
        self.mysql_config = {
            'host': os.getenv('mysql_host'),
            'user': os.getenv('mysql_user'),
            'password': os.getenv('mysql_password'),
            'database': os.getenv('mysql_database'),
        }
        
        self.mongo_client = MongoClient(os.getenv('mongo_uri'))
        self.mongo_db = self.mongo_client[os.getenv('mongo_database')]
        self.products_collection = self.mongo_db[os.getenv('mongo_collection')]
    
    def get_mysql_connection(self):
        return mysql.connector.connect(**self.mysql_config)
    
    def close_connections(self):
        if hasattr(self, 'mongo_client'):
            self.mongo_client.close()
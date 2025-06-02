# database/connector.py
import mysql.connector
from pymongo import MongoClient

class DatabaseConnector:
    """Database connector cho MySQL và MongoDB"""
    def __init__(self):
        self.mysql_config = {
            'host': 'localhost',
            'user': 'root',
            'password': '123456',
            'database': 'orderdb'
        }
        
        self.mongo_client = MongoClient('mongodb+srv://22520991:22520991@cluster0.3fj4b.mongodb.net/productdb?retryWrites=true&w=majority&appName=Cluster0')
        self.mongo_db = self.mongo_client['productdb']
        self.products_collection = self.mongo_db['product']
    
    def get_mysql_connection(self):
        return mysql.connector.connect(**self.mysql_config)
    
    def close_connections(self):
        if hasattr(self, 'mongo_client'):
            self.mongo_client.close()
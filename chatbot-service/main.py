# main.py
import json
from datetime import datetime
from connector import DatabaseConnector
from vector_db import VectorDatabaseManager
from data_querier import EnhancedDataQuerier
from ai_processor import EnhancedAIProcessor
from data_record import DataRecord
from typing import Dict, List

class EnhancedAIChatbot:
    """Enhanced AI Chatbot với VectorDB và RAG"""
    
    def __init__(self, vector_db_path: str = "./vector_db"):
        self.db_connector = DatabaseConnector()
        self.vector_db = VectorDatabaseManager(vector_db_path)
        self.data_querier = EnhancedDataQuerier(self.db_connector, self.vector_db)
        self.ai_processor = EnhancedAIProcessor(self.data_querier)
        
    def chat(self, user_query: str) -> str:

        response = self.ai_processor.generate_rag_response(user_query)
        return response
    
    def get_vector_db_stats(self) -> Dict:
        try:
            stats = {
                'orders_count': self.vector_db.orders_collection.count(),
                'products_count': self.vector_db.products_collection.count(), 
                'summaries_count': self.vector_db.summaries_collection.count(),
                'total_records': 0
            }
            stats['total_records'] = sum([stats['orders_count'], stats['products_count'], stats['summaries_count']])
            return stats
        except Exception as e:
            return {'error': str(e)}
    
    def close(self):
        self.db_connector.close_connections()

def main():

    chatbot = EnhancedAIChatbot()
    
    
    vector_stats = chatbot.get_vector_db_stats()
    print(f"\n📊 VectorDB Stats: {json.dumps(vector_stats, indent=2)}")
    
    
    while True:
        try:
            user_input = input("\n💬 Bạn: ").strip()
            
            if user_input.lower() in ['quit', 'exit', 'bye', 'thoát']:
                print("👋 Tạm biệt!")
                break
                
            if user_input.lower() == 'stats':
                stats = chatbot.get_vector_db_stats()
                print(f"📊 VectorDB Stats: {json.dumps(stats, indent=2)}")
                continue
            
            if not user_input:
                continue
            
            response = chatbot.chat(user_input)
            print(f"\n🤖 AI Assistant (RAG): {response}")
            
        except KeyboardInterrupt:
            print("\n👋 Tạm biệt!")
            break
        except Exception as e:
            print(f"\n❌ Lỗi: {e}")
    
    chatbot.close()

if __name__ == "__main__": 
    main()
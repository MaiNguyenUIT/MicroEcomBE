# AI Chatbot với VectorDB - Enhanced RAG System
# Sử dụng Vector Database để lưu trữ và tìm kiếm dữ liệu thông minh

import os
import json
import mysql.connector
from pymongo import MongoClient
import openai
import numpy as np
from datetime import datetime
from typing import Dict, List, Any, Tuple
import re
import hashlib
from dataclasses import dataclass, asdict
import chromadb
from chromadb.utils import embedding_functions
import pandas as pd

@dataclass
class DataRecord:
    """Class để đại diện cho một record dữ liệu"""
    id: str
    content: str
    metadata: Dict[str, Any]
    data_type: str  # 'order', 'product', 'summary'
    timestamp: str
    embedding_model: str = "text-embedding-ada-002"

class VectorDatabaseManager:
    """Quản lý Vector Database sử dụng ChromaDB"""
    
    def __init__(self, persist_directory: str = "./vector_db"):
        self.client = chromadb.PersistentClient(path=persist_directory)
        
        # Sử dụng OpenAI embeddings
        openai_ef = embedding_functions.OpenAIEmbeddingFunction(
            api_key=os.getenv('OPENAI_API_KEY', 'your-openai-api-key-here'),
            model_name="text-embedding-ada-002"
        )
        
        # Tạo collections cho từng loại dữ liệu
        self.orders_collection = self.client.get_or_create_collection(
            name="orders_data",
            embedding_function=openai_ef
        )
        
        self.products_collection = self.client.get_or_create_collection(
            name="products_data", 
            embedding_function=openai_ef
        )
        
        self.summaries_collection = self.client.get_or_create_collection(
            name="data_summaries",
            embedding_function=openai_ef
        )
        
        print("✅ Vector Database initialized with ChromaDB")
    
    def add_records(self, records: List[DataRecord], collection_type: str = "auto"):
        """Thêm records vào vector database"""
        try:
            for record in records:
                collection = self._get_collection(record.data_type if collection_type == "auto" else collection_type)
                
                collection.add(
                    documents=[record.content],
                    metadatas=[record.metadata],
                    ids=[record.id]
                )
            
            print(f"✅ Added {len(records)} records to vector database")
            
        except Exception as e:
            print(f"❌ Error adding records: {e}")
    
    def search_similar(self, query: str, data_type: str = "all", n_results: int = 5) -> List[Dict]:
        """Tìm kiếm dữ liệu tương tự dựa trên semantic search"""
        try:
            results = []
            
            collections_to_search = []
            if data_type == "all":
                collections_to_search = [
                    ("orders", self.orders_collection),
                    ("products", self.products_collection), 
                    ("summaries", self.summaries_collection)
                ]
            else:
                collection = self._get_collection(data_type)
                collections_to_search = [(data_type, collection)]
            
            for coll_name, collection in collections_to_search:
                try:
                    search_results = collection.query(
                        query_texts=[query],
                        n_results=min(n_results, collection.count())
                    )
                    
                    if search_results['documents'] and search_results['documents'][0]:
                        for i, doc in enumerate(search_results['documents'][0]):
                            results.append({
                                'content': doc,
                                'metadata': search_results['metadatas'][0][i],
                                'distance': search_results['distances'][0][i] if 'distances' in search_results else 0,
                                'collection': coll_name
                            })
                except Exception as e:
                    print(f"Warning: Error searching in {coll_name}: {e}")
            
            # Sort by distance (similarity)
            results.sort(key=lambda x: x['distance'])
            return results[:n_results]
            
        except Exception as e:
            print(f"❌ Error in similarity search: {e}")
            return []
    
    def _get_collection(self, data_type: str):
        """Lấy collection phù hợp dựa trên data type"""
        collections = {
            'order': self.orders_collection,
            'orders': self.orders_collection,
            'product': self.products_collection,
            'products': self.products_collection,
            'summary': self.summaries_collection,
            'summaries': self.summaries_collection
        }
        return collections.get(data_type.lower(), self.summaries_collection)

class EnhancedDataQuerier:
    """Enhanced Data Querier với VectorDB integration"""
    
    def __init__(self, db_connector, vector_db: VectorDatabaseManager):
        self.db_connector = db_connector
        self.vector_db = vector_db
        self.cache = {}  # Simple cache for frequent queries
    
    def query_with_context(self, user_query: str) -> Dict[str, Any]:
        """Truy vấn dữ liệu với context từ VectorDB"""
        
        # 1. Tìm kiếm context tương tự trong VectorDB
        similar_contexts = self.vector_db.search_similar(user_query, n_results=3)
        
        # 2. Truy vấn dữ liệu mới từ database
        fresh_data = self._execute_fresh_queries(user_query)
        
        # 3. Kết hợp context và fresh data
        combined_result = {
            'fresh_data': fresh_data,
            'similar_contexts': similar_contexts,
            'query': user_query,
            'timestamp': datetime.now().isoformat()
        }
        
        # 4. Lưu kết quả mới vào VectorDB
        self._store_query_result(user_query, combined_result)
        
        return combined_result
    
    def _execute_fresh_queries(self, user_query: str) -> Dict:
        """Thực thi truy vấn mới từ database"""
        result = {
            'orders': [],
            'products': [],
            'statistics': {}
        }
        
        try:
            # Basic statistics (có thể cache)
            cache_key = "basic_stats"
            if cache_key not in self.cache:
                result['statistics']['orders'] = self._get_order_statistics()
                result['statistics']['products'] = self._get_product_statistics()
                self.cache[cache_key] = result['statistics']
            else:
                result['statistics'] = self.cache[cache_key]
            
            # Specific queries based on user intent
            if any(keyword in user_query.lower() for keyword in ['đơn hàng', 'order', 'doanh thu']):
                result['orders'] = self._query_orders_advanced(user_query)
            
            if any(keyword in user_query.lower() for keyword in ['sản phẩm', 'product', 'hàng hóa']):
                result['products'] = self._query_products_advanced(user_query)
                
        except Exception as e:
            print(f"❌ Error in fresh queries: {e}")
            result['error'] = str(e)
        
        return result
    
    def _query_orders_advanced(self, query: str) -> List[Dict]:
        """Advanced order queries"""
        try:
            conn = self.db_connector.get_mysql_connection()
            cursor = conn.cursor(dictionary=True)
            
            # Determine query type based on keywords
            if 'pending' in query.lower():
                cursor.execute("SELECT * FROM orders WHERE status = 'pending'")
            elif 'completed' in query.lower():
                cursor.execute("SELECT * FROM orders WHERE status = 'completed'")
            elif 'revenue' in query.lower() or 'doanh thu' in query.lower():
                cursor.execute("""
                    SELECT status, SUM(total_amount) as revenue, COUNT(*) as count 
                    FROM orders 
                    GROUP BY status
                """)
            else:
                cursor.execute("SELECT * FROM orders LIMIT 10")
            
            result = cursor.fetchall()
            cursor.close()
            conn.close()
            return result
            
        except Exception as e:
            print(f"❌ Order query error: {e}")
            return []
    
    def _query_products_advanced(self, query: str) -> List[Dict]:
        """Advanced product queries"""
        try:
            filter_dict = {}
            
            # Determine filter based on keywords
            if 'fashion' in query.lower():
                filter_dict['category'] = 'Fashion'
            elif 'electronics' in query.lower():
                filter_dict['category'] = 'Electronics'
            elif 'expensive' in query.lower() or 'đắt' in query.lower():
                # Find products with price > average
                avg_result = list(self.db_connector.products_collection.aggregate([
                    {"$group": {"_id": None, "avg_price": {"$avg": "$price"}}}
                ]))
                if avg_result:
                    avg_price = avg_result[0]['avg_price']
                    filter_dict['price'] = {"$gte": avg_price}
            
            result = list(self.db_connector.products_collection.find(filter_dict).limit(10))
            
            # Convert ObjectId to string
            for item in result:
                if '_id' in item:
                    item['_id'] = str(item['_id'])
            
            return result
            
        except Exception as e:
            print(f"❌ Product query error: {e}")
            return []
    
    def _get_order_statistics(self) -> Dict:
        """Get order statistics"""
        try:
            conn = self.db_connector.get_mysql_connection()
            cursor = conn.cursor(dictionary=True)
            
            queries = {
                'total_orders': "SELECT COUNT(*) as count FROM orders",
                'total_revenue': "SELECT SUM(total_amount) as revenue FROM orders",
                'avg_order_value': "SELECT AVG(total_amount) as avg_value FROM orders",
                'status_breakdown': "SELECT status, COUNT(*) as count, SUM(total_amount) as revenue FROM orders GROUP BY status"
            }
            
            stats = {}
            for key, query in queries.items():
                cursor.execute(query)
                if key == 'status_breakdown':
                    stats[key] = cursor.fetchall()
                else:
                    result = cursor.fetchone()
                    stats[key] = result if result else {}
            
            cursor.close()
            conn.close()
            return stats
            
        except Exception as e:
            print(f"❌ Order stats error: {e}")
            return {}
    
    def _get_product_statistics(self) -> Dict:
        """Get product statistics"""  
        try:
            pipeline = [
                {"$group": {
                    "_id": None,
                    "total_products": {"$sum": 1},
                    "avg_price": {"$avg": "$price"},
                    "min_price": {"$min": "$price"},
                    "max_price": {"$max": "$price"},
                    "categories": {"$addToSet": "$category"}
                }}
            ]
            
            result = list(self.db_connector.products_collection.aggregate(pipeline))
            if result:
                stats = result[0]
                stats['total_categories'] = len(stats.get('categories', []))
                return stats
            return {}
            
        except Exception as e:
            print(f"❌ Product stats error: {e}")
            return {}
    
    def _store_query_result(self, query: str, result: Dict):
        """Lưu kết quả truy vấn vào VectorDB"""
        try:
            # Create content summary for embedding
            content_parts = []
            
            if result['fresh_data'].get('orders'):
                content_parts.append(f"Orders data: {len(result['fresh_data']['orders'])} records")
            
            if result['fresh_data'].get('products'):
                content_parts.append(f"Products data: {len(result['fresh_data']['products'])} records")
            
            if result['fresh_data'].get('statistics'):
                stats = result['fresh_data']['statistics']
                if 'orders' in stats:
                    order_stats = stats['orders']
                    if 'total_orders' in order_stats:
                        content_parts.append(f"Total orders: {order_stats['total_orders'].get('count', 0)}")
                    if 'total_revenue' in order_stats:
                        content_parts.append(f"Total revenue: {order_stats['total_revenue'].get('revenue', 0)}")
            
            if not content_parts:
                content_parts.append("Query executed with no specific results")
            
            content = f"Query: {query}\nResults: {', '.join(content_parts)}"
            
            # Create record
            record_id = hashlib.md5(f"{query}_{datetime.now().isoformat()}".encode()).hexdigest()
            
            record = DataRecord(
                id=record_id,
                content=content,
                metadata={
                    'query': query,
                    'result_summary': json.dumps({
                        'orders_count': len(result['fresh_data'].get('orders', [])),
                        'products_count': len(result['fresh_data'].get('products', [])),
                        'has_statistics': bool(result['fresh_data'].get('statistics'))
                    }),
                    'timestamp': result['timestamp']
                },
                data_type='summary',
                timestamp=result['timestamp']
            )
            
            self.vector_db.add_records([record])
            
        except Exception as e:
            print(f"❌ Error storing query result: {e}")

class DatabaseConnector:
    """Database connector cho MySQL và MongoDB"""
    def __init__(self):
        self.mysql_config = {
            'host': 'localhost',
            'user': 'root', 
            'password': 'password',
            'database': 'ecommerce'
        }
        
        self.mongo_client = MongoClient('mongodb://localhost:27017/')
        self.mongo_db = self.mongo_client['ecommerce']
        self.products_collection = self.mongo_db['products']
    
    def get_mysql_connection(self):
        return mysql.connector.connect(**self.mysql_config)
    
    def close_connections(self):
        if hasattr(self, 'mongo_client'):
            self.mongo_client.close()

class EnhancedAIProcessor:
    """Enhanced AI Processor với RAG capabilities"""
    
    def __init__(self, data_querier: EnhancedDataQuerier):
        self.data_querier = data_querier
        openai.api_key = os.getenv('OPENAI_API_KEY', 'your-openai-api-key-here')
    
    def generate_rag_response(self, user_query: str) -> str:
        """Generate response using RAG (Retrieval-Augmented Generation)"""
        
        # 1. Retrieve relevant context and fresh data
        query_result = self.data_querier.query_with_context(user_query)
        
        # 2. Prepare context for AI
        context = self._prepare_context(query_result)
        
        # 3. Generate response using AI with context
        response = self._generate_contextual_response(user_query, context)
        
        return response
    
    def _prepare_context(self, query_result: Dict) -> str:
        """Chuẩn bị context từ kết quả truy vấn"""
        context_parts = []
        
        # Add similar contexts from vector DB
        if query_result.get('similar_contexts'):
            context_parts.append("RELEVANT HISTORICAL DATA:")
            for i, ctx in enumerate(query_result['similar_contexts'][:2], 1):
                context_parts.append(f"{i}. {ctx['content'][:200]}...")
        
        # Add fresh data
        if query_result.get('fresh_data'):
            fresh_data = query_result['fresh_data']
            context_parts.append("\nCURRENT DATA:")
            
            # Statistics
            if fresh_data.get('statistics'):
                stats = fresh_data['statistics']
                if 'orders' in stats:
                    context_parts.append(f"Order Statistics: {json.dumps(stats['orders'], ensure_ascii=False, indent=2)}")
                if 'products' in stats:
                    context_parts.append(f"Product Statistics: {json.dumps(stats['products'], ensure_ascii=False, indent=2)}")
            
            # Specific data
            if fresh_data.get('orders'):
                context_parts.append(f"Recent Orders: {json.dumps(fresh_data['orders'][:5], ensure_ascii=False, indent=2)}")
            
            if fresh_data.get('products'):
                context_parts.append(f"Products: {json.dumps(fresh_data['products'][:5], ensure_ascii=False, indent=2)}")
        
        return "\n".join(context_parts)
    
    def _generate_contextual_response(self, user_query: str, context: str) -> str:
        """Generate AI response với context"""
        prompt = f"""
        Bạn là một AI assistant chuyên về phân tích dữ liệu kinh doanh. 
        Dựa trên context được cung cấp, hãy trả lời câu hỏi của người dùng một cách chi tiết và hữu ích.

        CONTEXT (Dữ liệu tham khảo):
        {context}

        QUESTION: {user_query}

        YÊU CẦU:
        - Trả lời bằng tiếng Việt
        - Sử dụng dữ liệu từ context để đưa ra câu trả lời chính xác
        - Đưa ra insights và phân tích từ dữ liệu
        - Định dạng số liệu dễ đọc (sử dụng dấu phẩy cho hàng nghìn)
        - Nếu có dữ liệu lịch sử tương tự, so sánh và đưa ra nhận xét
        - Kết thúc bằng gợi ý hành động hoặc câu hỏi tiếp theo nếu phù hợp
        """
        
        try:
            response = openai.ChatCompletion.create(
                model="gpt-3.5-turbo",
                messages=[{"role": "user", "content": prompt}],
                temperature=0.7,
                max_tokens=800
            )
            
            return response.choices[0].message.content.strip()
            
        except Exception as e:
            print(f"❌ AI response error: {e}")
            return f"Đã tìm thấy dữ liệu liên quan đến câu hỏi của bạn, nhưng gặp lỗi khi tạo câu trả lời chi tiết. Lỗi: {str(e)}"

class EnhancedAIChatbot:
    """Enhanced AI Chatbot với VectorDB và RAG"""
    
    def __init__(self, vector_db_path: str = "./vector_db"):
        self.db_connector = DatabaseConnector()
        self.vector_db = VectorDatabaseManager(vector_db_path)
        self.data_querier = EnhancedDataQuerier(self.db_connector, self.vector_db)
        self.ai_processor = EnhancedAIProcessor(self.data_querier)
        
        # Initialize with sample data
        self.setup_sample_data()
        
    def setup_sample_data(self):
        """Setup sample data và populate VectorDB"""
        print("🔧 Setting up sample data and populating VectorDB...")
        
        # Sample data records for VectorDB
        sample_records = [
            DataRecord(
                id="order_summary_1",
                content="Order Statistics: Total orders: 150, Total revenue: 5,500,000 VND, Average order value: 36,667 VND. Status breakdown: Completed (120), Pending (20), Cancelled (10)",
                metadata={
                    "data_type": "order_statistics",
                    "total_orders": 150,
                    "total_revenue": 5500000,
                    "date_range": "2024-01-01 to 2024-12-31"
                },
                data_type="summary",
                timestamp=datetime.now().isoformat()
            ),
            DataRecord(
                id="product_summary_1", 
                content="Product Catalog: 45 products across 3 categories (Fashion: 20, Electronics: 15, Sports: 10). Price range: 15,000 - 150,000 VND. Average price: 45,000 VND",
                metadata={
                    "data_type": "product_statistics",
                    "total_products": 45,
                    "categories": ["Fashion", "Electronics", "Sports"],
                    "avg_price": 45000
                },
                data_type="summary",
                timestamp=datetime.now().isoformat()
            ),
            DataRecord(
                id="trend_analysis_1",
                content="Sales Trend Analysis: Fashion category shows 25% growth, Electronics stable, Sports declining 10%. Peak sales in December with 40% increase from average monthly sales",
                metadata={
                    "data_type": "trend_analysis",
                    "fashion_growth": 0.25,
                    "electronics_growth": 0.0,
                    "sports_growth": -0.10,
                    "peak_month": "December"
                },
                data_type="summary", 
                timestamp=datetime.now().isoformat()
            )
        ]
        
        # Add sample records to VectorDB
        self.vector_db.add_records(sample_records)
        print("✅ Sample data và VectorDB đã được setup!")
    
    def chat(self, user_query: str) -> str:
        """Main chat function với RAG"""
        print(f"\n🤖 Đang xử lý câu hỏi: {user_query}")
        print("🔍 Tìm kiếm context tương tự...")
        print("📊 Truy vấn dữ liệu mới...")
        print("🧠 Tạo câu trả lời với AI...")
        
        response = self.ai_processor.generate_rag_response(user_query)
        return response
    
    def get_vector_db_stats(self) -> Dict:
        """Lấy thống kê VectorDB"""
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
        """Cleanup resources"""
        self.db_connector.close_connections()

# Demo Usage
def main():
    print("🚀 Enhanced AI Chatbot với VectorDB và RAG System")
    print("=" * 70)
    
    # Initialize enhanced chatbot
    chatbot = EnhancedAIChatbot()
    
    # Show VectorDB stats
    vector_stats = chatbot.get_vector_db_stats()
    print(f"\n📊 VectorDB Stats: {json.dumps(vector_stats, indent=2)}")
    
    # Sample queries
    sample_queries = [
        "Tổng doanh thu và số đơn hàng hiện tại như thế nào?",
        "So sánh doanh thu giữa các danh mục sản phẩm",
        "Xu hướng bán hàng gần đây ra sao?", 
        "Sản phẩm nào bán chạy nhất?",
        "Phân tích tình hình đơn hàng pending",
        "Dự đoán xu hướng bán hàng tháng tới"
    ]
    
    print(f"\n📝 Các câu hỏi mẫu (hệ thống sẽ sử dụng RAG để trả lời):")
    for i, query in enumerate(sample_queries, 1):
        print(f"{i}. {query}")
    
    print(f"\n💡 Lợi ích của VectorDB + RAG:")
    print("✅ Semantic search - tìm context tương tự thông minh")
    print("✅ Lưu trữ historical insights")  
    print("✅ Kết hợp dữ liệu cũ và mới cho câu trả lời chính xác hơn")
    print("✅ Học từ các câu hỏi trước đó")
    print("✅ Giảm thời gian truy vấn database")
    
    print("\n" + "="*70)
    
    # Interactive chat
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
            
            # Process with RAG
            response = chatbot.chat(user_input)
            print(f"\n🤖 AI Assistant (RAG): {response}")
            
        except KeyboardInterrupt:
            print("\n👋 Tạm biệt!")
            break
        except Exception as e:
            print(f"\n❌ Lỗi: {e}")
    
    # Cleanup
    chatbot.close()

if __name__ == "__main__":
    # Dependencies
    print("""
    📦 Cài đặt dependencies cho VectorDB version:
    pip install mysql-connector-python pymongo openai chromadb pandas numpy
    
    🔧 Cấu hình:
    1. MySQL database 'ecommerce' với bảng orders
    2. MongoDB với collection products  
    3. OPENAI_API_KEY environment variable
    4. ChromaDB sẽ tự động tạo local vector database
    
    🚀 Tính năng mới:
    - Vector Database với ChromaDB
    - RAG (Retrieval-Augmented Generation)
    - Semantic search cho context tương tự
    - Lưu trữ và tái sử dụng insights
    - Kết hợp dữ liệu historical và real-time
    """)
    
    main()
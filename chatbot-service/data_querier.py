# processor/data_querier.py
import mysql.connector
from datetime import datetime
import hashlib
import json
from typing import Dict, List, Any
from vector_db import VectorDatabaseManager
from connector import DatabaseConnector
from data_record import DataRecord
from decimal import Decimal

class EnhancedDataQuerier:
    """Enhanced Data Querier với VectorDB integration"""
    
    def __init__(self, db_connector: DatabaseConnector, vector_db: VectorDatabaseManager):
        self.db_connector = db_connector
        self.vector_db = vector_db
        self.cache = {}
    
    def query_with_context(self, user_query: str) -> Dict[str, Any]:
        similar_contexts = self.vector_db.search_similar(user_query, n_results=3)
        fresh_data = self._execute_fresh_queries(user_query)
        
        combined_result = {
            'fresh_data': fresh_data,
            'similar_contexts': similar_contexts,
            'query': user_query,
            'timestamp': datetime.now().isoformat()
        }
        
        self._store_query_result(user_query, combined_result)
        return combined_result
    
    def _execute_fresh_queries(self, user_query: str) -> Dict:
        result = {
            'orders': [],
            'products': [],
            'statistics': {}
        }
        
        try:
            cache_key = "basic_stats"
            if cache_key not in self.cache:
                result['statistics']['orders'] = self._get_order_statistics()
                result['statistics']['products'] = self._get_product_statistics()
                self.cache[cache_key] = result['statistics']
            else:
                result['statistics'] = self.cache[cache_key]
            
            if any(keyword in user_query.lower() for keyword in ['đơn hàng', 'order', 'doanh thu']):
                result['orders'] = self._query_orders_advanced(user_query)
            
            if any(keyword in user_query.lower() for keyword in ['sản phẩm', 'product', 'hàng hóa']):
                result['products'] = self._query_products_advanced(user_query)
                
        except Exception as e:
            print(f"❌ Error in fresh queries: {e}")
            result['error'] = str(e)
        
        return result
    
    def _query_orders_advanced(self, query: str) -> List[Dict]:
        print("🔍 _query_orders_advanced called with query:", query)
        try:
            conn = self.db_connector.get_mysql_connection()
            cursor = conn.cursor(dictionary=True)
            
            if 'pending' in query.lower():
                cursor.execute("SELECT * FROM orderdb.orders WHERE order_status = 'PENDING'")
            elif 'completed' in query.lower():
                cursor.execute("SELECT * FROM orderdb.orders WHERE order_status = 'COMPLETED'")
            elif 'revenue' in query.lower() or 'doanh thu' in query.lower():
                cursor.execute("""
                    SELECT order_status AS status, 
                    SUM(order_amount) AS revenue, 
                    COUNT(*) AS count 
                    FROM orderdb.orders 
                    GROUP BY order_status
                """)
            else:
                cursor.execute("SELECT * FROM orders LIMIT 10")
            
            result = cursor.fetchall()
            cursor.close()
            conn.close()
            print(f"✅ Orders query executed: {len(result)} records found")
            return result
            
        except Exception as e:
            print(f"❌ Order query error: {e}")
            return []
    
    
    def _get_order_statistics(self) -> Dict:
        try:
            conn = self.db_connector.get_mysql_connection()
            cursor = conn.cursor(dictionary=True)
            
            queries = {
               'total_orders': "SELECT COUNT(*) as count FROM orders",
               'total_revenue': "SELECT SUM(order_amount) as revenue FROM orders",
               'avg_order_value': "SELECT AVG(order_amount) as avg_value FROM orders",
               'status_breakdown': "SELECT order_status as status, COUNT(*) as count, SUM(order_amount) as revenue FROM orders GROUP BY order_status"
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
    
    def _query_products_advanced(self) -> List[Dict]:
        try:
        
            result = list(self.db_connector.products_collection.find().limit(10))
            for item in result:
                if '_id' in item:
                    item['_id'] = str(item['_id'])

            return result

        except Exception as e:
            print(f"❌ Product query error: {e}")
            return []

    def _get_product_statistics(self) -> Dict:
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
    
    def _convert_decimals(self, obj):
       if isinstance(obj, dict):
         return {k: self._convert_decimals(v) for k, v in obj.items()}
       elif isinstance(obj, list):
         return [self._convert_decimals(i) for i in obj]
       elif isinstance(obj, Decimal):
        return float(obj)
       else:
        return obj
    
    def _store_query_result(self, query: str, result: Dict):
        
        try:
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
            
            record_id = hashlib.md5(f"{query}_{datetime.now().isoformat()}".encode()).hexdigest()
            
            result_summary = {
                'orders_count': len(result['fresh_data'].get('orders', [])),
                'products_count': len(result['fresh_data'].get('products', [])),
                'has_statistics': bool(result['fresh_data'].get('statistics'))
            }
            
            result_summary_clean = self._convert_decimals(result_summary)
            
            metadata = {
                    'query': query,
                    'result_summary': json.dumps(result_summary_clean),
                    'timestamp': result['timestamp']
            }
            
            
            record = DataRecord(
                id=record_id,
                content=content,
                metadata=metadata,
                data_type='summary',
                timestamp=result['timestamp']
            )
            
            self.vector_db.add_records([record])
            
        except Exception as e:
            print(f"❌ Error storing query result: {e}")
           
    
    

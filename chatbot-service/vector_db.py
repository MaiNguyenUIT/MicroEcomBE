# database/vector_db.py
import chromadb
from chromadb.utils import embedding_functions
from data_record import DataRecord
from typing import List, Dict

class VectorDatabaseManager:
    """Quản lý Vector Database sử dụng ChromaDB"""
    
    def __init__(self, persist_directory: str = "./vector_db"):
        self.client = chromadb.PersistentClient(path=persist_directory)
        
        hf_ef = embedding_functions.SentenceTransformerEmbeddingFunction(
            model_name="all-MiniLM-L6-v2"  
        )
        
        self.orders_collection = self.client.get_or_create_collection(
            name="orders_data",
            embedding_function=hf_ef
        )
        
        self.products_collection = self.client.get_or_create_collection(
            name="products_data", 
            embedding_function=hf_ef
        )
        
        self.summaries_collection = self.client.get_or_create_collection(
            name="data_summaries",
            embedding_function=hf_ef
        )
        
        

    
    def add_records(self, records: List[DataRecord], collection_type: str = "auto"):
        try:
            for record in records:
                collection = self._get_collection(record.data_type if collection_type == "auto" else collection_type)
                
                collection.add(
                    documents=[record.content],
                    metadatas=[record.metadata],
                    ids=[record.id]
                )
            
        except Exception as e:
            print(f"❌ Error adding records: {e}")
    
    def search_similar(self, query: str, data_type: str = "all", n_results: int = 5) -> List[Dict]:
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
            
            results.sort(key=lambda x: x['distance'])
            return results[:n_results]
            
        except Exception as e:
            print(f"❌ Error in similarity search: {e}")
            return []
    
    def _get_collection(self, data_type: str):
        collections = {
            'order': self.orders_collection,
            'orders': self.orders_collection,
            'product': self.products_collection,
            'products': self.products_collection,
            'summary': self.summaries_collection,
            'summaries': self.summaries_collection
        }
        return collections.get(data_type.lower(), self.summaries_collection)

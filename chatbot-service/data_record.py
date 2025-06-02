# models/data_record.py
from dataclasses import dataclass
from typing import Dict, Any

@dataclass
class DataRecord:
    """Class để đại diện cho một record dữ liệu"""
    id: str
    content: str
    metadata: Dict[str, Any]
    data_type: str  # 'order', 'product', 'summary'
    timestamp: str
    embedding_model: str = "text-embedding-ada-002"
# processor/ai_processor.py
import openai
import os
import json
from typing import Dict
from data_querier import EnhancedDataQuerier
from huggingface_hub import InferenceClient
from decimal import Decimal
from typing import Any
from dotenv import load_dotenv

load_dotenv() 
class EnhancedAIProcessor:
    """Enhanced AI Processor với RAG capabilities"""
    
    def __init__(self, data_querier: EnhancedDataQuerier, model_name="HuggingFaceH4/zephyr-7b-beta"):
        self.data_querier = data_querier
        self.client = InferenceClient(model=model_name, token=os.getenv("hugging-face_token"))
    
    def generate_rag_response(self, user_query: str) -> str:
        query_result = self.data_querier.query_with_context(user_query)
        context = self._prepare_context(query_result)
        response = self._generate_contextual_response(user_query, context)
        return response
    
    def _convert_decimals(self, obj: Any) -> Any:
        try:
            if isinstance(obj, dict):
                return {k: self._convert_decimals(v) for k, v in obj.items()}
            elif isinstance(obj, list):
                return [self._convert_decimals(i) for i in obj]
            elif isinstance(obj, Decimal):
                return float(obj)
            return obj
        except Exception as e:
            print(f"Error converting decimal: {e}")
            return obj
    
    def _prepare_context(self, query_result: Dict) -> str:
        context_parts = []
        
        if query_result.get('similar_contexts'):
            context_parts.append("RELEVANT HISTORICAL DATA:")
            for i, ctx in enumerate(query_result['similar_contexts'][:2], 1):
                context_parts.append(f"{i}. {ctx['content'][:200]}...")
        
        if query_result.get('fresh_data'):
            fresh_data = query_result['fresh_data']
            context_parts.append("\nCURRENT DATA:")
            
            if fresh_data.get('statistics'):
                stats = fresh_data['statistics']
                if 'orders' in stats:
                    clean_orders_stats = self._convert_decimals(stats['orders'])
                    context_parts.append(f"Order Statistics: {json.dumps(clean_orders_stats, ensure_ascii=False, indent=2)}")
                if 'products' in stats:
                    clean_products_stats = self._convert_decimals(stats['products'])
                    context_parts.append(f"Product Statistics: {json.dumps(clean_products_stats, ensure_ascii=False, indent=2)}")
            
            for data_type in ['orders', 'products']:
                if fresh_data.get(data_type):
                    clean_data = self._convert_decimals(fresh_data[data_type][:5])
                    label = "Recent Orders" if data_type == 'orders' else "Products"
                    context_parts.append(f"{label}: {json.dumps(clean_data, ensure_ascii=False, indent=2)}")

        return "\n".join(context_parts)
    
    def _generate_contextual_response(self, user_query: str, context: str) -> str:
        prompt = f"""
        Bạn là một AI assistant chuyên về phân tích dữ liệu kinh doanh. 
        Dựa trên context được cung cấp, hãy trả lời câu hỏi của người dùng một cách ngắn gọn, đơn giản và hữu ích.

        CONTEXT (Dữ liệu tham khảo):
        {context}

        QUESTION: {user_query}

        YÊU CẦU:
        - Trả lời bằng tiếng Việt
        - Sử dụng dữ liệu từ context để đưa ra câu trả lời chính xác
        - Đưa ra insights và phân tích đơn giản từ dữ liệu
        - Định dạng số liệu dễ đọc (sử dụng dấu phẩy cho hàng nghìn)
        """
        print(f"📝 AI Prompt:\n{prompt[:500]}...")
        try:
            response = self.client.text_generation(
                prompt=prompt,
                max_new_tokens=512,
                temperature=0.7,
                top_p=0.95,
                stop_sequences=["</s>"]
            )
            cleaned_response = response.strip()

            if cleaned_response.startswith("RESPONSE:"):
                cleaned_response = cleaned_response.replace("RESPONSE:", "").strip()
            return cleaned_response

            
        except Exception as e:
            print(f"❌ AI response error: {e}")
            return f"Đã tìm thấy dữ liệu liên quan đến câu hỏi của bạn, nhưng gặp lỗi khi tạo câu trả lời chi tiết. Lỗi: {str(e)}"